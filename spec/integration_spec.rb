require 'middleware/redis_middleware'
require 'middleware/password_reset'
require 'middleware/counter'
require 'middleware/uploader'
require 'middleware/basic_registration'
require 'middleware/token_auth_strategy'
require 'middleware/oauth_provider_authorization'
require 'middleware/users'
require 'middleware/admin'

require 'rack/test'
require 'vcr'
require 'webmock'
require 'json'

require './spec/fixtures'

# Lets add some speed!
BCrypt::Engine::DEFAULT_COST = 1

describe 'Integration spec' do

  before(:all) do
    VCR.configure do |c|
      c.allow_http_connections_when_no_cassette = true
      c.cassette_library_dir = 'fixtures/vcr_cassettes'
      c.hook_into :webmock
    end
    VCR.turn_on!
  end

  after(:all) do
    VCR.turn_off!
  end

  before(:each) do
    r = Redis.new
    r.flushdb
    Coefficients.storage(r).coefficients = {
      "brilliant-base-score" => 3000,
      "free-base-score" => 1000,
      "friend-bonus" => 1000,
      "gold-base-score" => 2000,
      "silver1-base-score" => 1500,
      "silver2-base-score" => 1500,
      "time-bonus" => 50
    }
  end

  include_context 'fixtures'
  include Rack::Test::Methods

  def last_response_should_be_json
    last_response.content_type.split(';')[0].should == 'application/json'
    lambda { JSON.parse(last_response.body) }.should_not raise_error
  end

  def app
    app = Rack::Builder.new do
      use Rack::Lint
      use Rack::ContentLength
      use Middleware::RedisMiddleware
      use Middleware::Uploader

      use Middleware::Counter, counter_mappings: {
        [200, %r{/login}] => 'logins',
        [200, %r{/sets/[^/]*/buy}] => 'sets_bought',
        [200, %r{/score}] => 'scored',
      }

      Middleware::TokenAuthStrategy.serialize_user_proc = lambda { |env, u| [u.class.name, u.id].join('#') }
      Middleware::TokenAuthStrategy.deserialize_user_proc = lambda do |env, key|
        klass, id = key.split('#', 2)
        Kernel.const_get(klass).storage(env['redis']).load(id)
      end
      use Middleware::TokenAuthStrategy

      Middleware::OauthProviderAuthorization.settings.environment = :test

      facebook_options = {
        client_id: '390010757745933',
        client_secret: '650d73bdb360c1e06719468b8e5eeddd',
        login_dialog_uri: 'https://facebook.com/dialog/oauth',
        access_token_uri: 'https://graph.facebook.com/oauth/access_token',
        scope: 'email,user_birthday,user_about_me,publish_stream'
      }
      facebook_provider = Middleware::OauthProviderAuthorization::Provider.new('facebook', facebook_options)
      use Middleware::OauthProviderAuthorization, facebook_provider

      vkontakte_options = {
        redirect_uri: 'http://oauth.vk.com/blank.html',
        scope: 'email,user_birthday,user_about_me,wall',
        client_id: '3392295',
        client_secret: '9hQhk0pKNEHOt0WikSZz',
        login_dialog_uri: 'https://oauth.vk.com/authorize',
        access_token_uri: 'https://oauth.vk.com/access_token'
      }
      vkontakte_provider = Middleware::OauthProviderAuthorization::Provider.new('vkontakte', vkontakte_options)
      use Middleware::OauthProviderAuthorization, vkontakte_provider

      Middleware::BasicRegistration.settings.environment = :test
      use Middleware::BasicRegistration

      Middleware::PasswordReset.settings.environment = :test
      use Middleware::PasswordReset

      Middleware::Users.settings.environment = :test
      use Middleware::Users

      Middleware::Admin.settings.environment = :test
      run Middleware::Admin
    end
  end

  let(:admin_user) do
    u = UserFactory.create_user(Redis.new, user_in_storage.merge('password' => user_in_storage_password))
    user_data = u.user_data
    user_data['role'] = 'admin'
    user_data.save
    u
  end

  it 'basic registration: register user with already existed email' do
    post '/signup', valid_user_data
    last_response.status.should == 200
    post '/signup', valid_user_data
    last_response.status.should == 403
  end

  it 'basic registration' do
    post '/signup', valid_user_data
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    session_key.should_not == nil
    response_data['me'].delete('created_at').should_not == nil
    response_data['me'].except('id').should == valid_user_data_user_as_json.except('id')

    get '/me', { session_key: session_key }
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['me'].delete('created_at').should_not == nil
    response_data['me'].except('id').should == valid_user_data_user_as_json.except('id')

    post '/login', email: valid_user_data['email'], password: valid_user_data['password']
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['session_key'].should_not == nil
    response_data['me']['name'].should == valid_user_data['name']
    post '/login', email: valid_user_data['email'], password: 'wrong_password'
    last_response_should_be_json
    last_response.status.should == 403
    new_session_key = response_data['session_key']
    new_session_key.should_not == session_key
    response_data['me'].delete('created_at').should_not == nil
    response_data['me'].except('id').should == valid_user_data_user_as_json.except('id')

    get '/me', { session_key: new_session_key }
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['me'].delete('created_at').should_not == nil
    response_data['me'].except('id').should == valid_user_data_user_as_json.except('id')
  end

  it 'updates basic_information' do
    post '/signup', valid_user_data
    post '/login', email: valid_user_data['email'], password: valid_user_data['password']
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    post '/me', { session_key: session_key, email: 'new_email@example.org', solved: 3 }
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    new_valid_data = valid_user_data_user_as_json.merge('email' => 'new_email@example.org',
                                                        'solved' => 3)
    response_data['me'].delete('created_at').should_not == nil
    response_data['me'].except('id').should == new_valid_data.except('id')

    get '/me', { session_key: session_key }
    response_data = JSON.parse(last_response.body)
    response_data['me'].delete('created_at').should_not == nil
    response_data['me'].except('id').should == new_valid_data.except('id')
  end

  it 'facebook registration' do
    VCR.use_cassette('facebook_success_login') do
      get '/facebook/login'
      last_response.status.should == 302
      # Here we assume user logs in into facebook and aproves our app
      get '/facebook/authorize', { code: 'AQALG4kdgy528xTY0KlG-WlKG9vzMyOZ3TGUhquw_yWMVYa0E7o0dyBvSd9uNFigtbhQaIMX7i33pL2G9DhIhIBk1LgEUfwALggMCXnT7ZFW-B7iS0_0giZEGllyJQgvpJPyLAXykWGMXt3S2l-Cm7AyEA9DsbmB646QhZVtIiKL-cGH9aS7omfCMgRpKZewJkbLvjdR7pfG5k6YT1iYAth6' }
      last_response_should_be_json
      last_response.status.should == 200
      response_data = JSON.parse(last_response.body)
      session_key = response_data['session_key']
      session_key.should_not == nil
      response_data['me'].delete('created_at').should_not == nil
      response_data['me'].except('id').should == valid_facebook_user_data_user_as_json.except('id')

      get '/me', { session_key: session_key }
      last_response_should_be_json
      last_response.status.should == 200
      response_data = JSON.parse(last_response.body)
      response_data['me'].delete('created_at').should_not == nil
      response_data['me'].except('id').should == valid_facebook_user_data_user_as_json.except('id')
    end
  end

  it 'basic rating' do
    20.times { |i| post '/signup', valid_user_data.merge('email' => "email#{i}@example.org") }

    post '/login', email: 'email4@example.org', password: valid_user_data['password']
    last_response.status.should == 200
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    post '/score', session_key: session_key, score: 100, solved: 10, source: 'some_reason'
    last_response.status.should == 200
    post '/score', session_key: session_key, score: 100, source: 'another_some_reason'
    last_response.status.should == 200

    post '/login', email: 'email2@example.org', password: valid_user_data['password']
    last_response.status.should == 200
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    post '/score', session_key: session_key, score: 150, source: 'yet_another_reason'
    last_response.status.should == 200

    post '/login', email: 'email10@example.org', password: valid_user_data['password']
    last_response.status.should == 200
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    post '/score', session_key: session_key, score: 80, source: 'and_another'

    post '/login', email: 'email1@example.org', password: valid_user_data['password']
    last_response.status.should == 200
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    post '/score', session_key: session_key, score: 70, source: 'and_some_more'
    last_response_should_be_json
    last_response.status.should == 200

    get '/users'
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['users'][0]['email'].should == 'email4@example.org'
    response_data['users'][0]['solved'].should == 10
    response_data['users'][0]['position'].should == 1
    response_data['users'][1]['email'].should == 'email2@example.org'
    response_data['users'][1]['position'].should == 2
    response_data['users'][2]['email'].should == 'email10@example.org'
    response_data['users'][2]['position'].should == 3
    response_data['users'][3]['email'].should == 'email1@example.org'
    response_data['users'][3]['position'].should == 4
  end

  it 'simple user cannot use admin' do
    post '/signup', valid_user_data
    post '/login', email: valid_user_data['email'], password: valid_user_data['password']
    last_response.status.should == 200
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']

    post '/sets'
    last_response.status.should == 401
  end

  it 'lists puzzles for user' do
    admin_user
    post '/login', email: user_in_storage['email'], password: user_in_storage_password
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']

    post('/sets',
         {
           year: Time.now.year,
           month: Time.now.month,
           name: 'Cool set',
           published: 'true',
           puzzles: [{ name: 'puzzle1' }, { name: 'puzzle2' }].to_json,
           type: 'golden',
           session_key: session_key
         })

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    admin_set_id = response_data['id']

    get('/sets_available', session_key: session_key)
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    set_id = response_data[0]['id']

    post '/signup', valid_user_data
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    user_id = response_data['me']['id']

    post("/sets/#{set_id}/buy", { session_key: session_key })
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)

    get("/puzzles", { session_key: session_key })
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['sets'][0]['id'].should == set_id

    get('/sets_available', session_key: session_key)
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data[0]['bought'].should == true
  end

  it 'reset password cannot use same token twice' do
    post '/signup', valid_user_data
    last_response.status.should == 200
    last_response_should_be_json

    token = nil
    Pony.should_receive(:mail) { |h| h[:html_body] =~ /token=([a-zA-Z0-9-]+)/; token = $1 }
    post '/forgot_password', email: valid_user_data['email']
    last_response.status.should == 200
    last_response_should_be_json
    last_response.body.should_not =~ /#{token}/ # developer sanity check

    get '/password_reset', token: token
    last_response.status.should == 200

    post '/password_reset', token: token, password: 'new_password'
    last_response.status.should == 200

    post '/password_reset', token: token, password: 'bad_new_password'
    last_response.status.should == 404

    post '/login', email: valid_user_data['email'], password: valid_user_data['password']
    last_response.status.should == 403

    post '/login', email: valid_user_data['email'], password: 'new_password'
    last_response.status.should == 200
  end

  it 'admin' do
    admin_user
    post '/login', email: user_in_storage['email'], password: user_in_storage_password

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']

    post('/sets',
         {
           year: Time.now.year,
           month: Time.now.month,
           name: 'Cool set',
           puzzles: [{ name: 'puzzle1' }, { name: 'puzzle2' }].to_json,
           type: 'golden',
           published: true,
           session_key: session_key
         })

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    admin_set_id1 = response_data['id']

    post('/sets',
         {
           year: Time.now.year,
           month: Time.now.month,
           name: 'Cool set2',
           puzzles: [{ name: 'puzzle1' }, { name: 'puzzle2' }].to_json,
           type: 'silver',
           published: false,
           session_key: session_key
         })

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    admin_set_id2 = response_data['id']

    get('/sets', { session_key: session_key })
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['sets'].size.should == 2

    delete("/sets/#{admin_set_id2}", { session_key: session_key })
    last_response.status.should == 200
    last_response_should_be_json

    get('/sets', { session_key: session_key })
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['sets'].size.should == 1
    response_data['sets'][0]['published'].should == true
  end

  it 'counters' do
    admin_user
    post '/login', email: user_in_storage['email'], password: user_in_storage_password
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    get '/counters', { session_key: session_key }
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['logins'].last.should == 1
  end

  it 'does service messages' do
    admin_user
    post '/login', email: user_in_storage['email'], password: user_in_storage_password
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']

    get '/service_messages', { session_key: session_key }
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['message1'].should == nil
    response_data['message2'].should == nil
    response_data['message3'].should == nil

    put '/service_messages', {
                              session_key: session_key,
                              message1: 'new message',
                              message2: 'another message',
                              message3: ''
                            }
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['message1'].should == 'new message'
    response_data['message2'].should == 'another message'
    response_data['message3'].should == nil


    get '/service_messages', { session_key: session_key }
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['message1'].should == 'new message'
    response_data['message2'].should == 'another message'
    response_data['message3'].should == nil
  end

  it 'admin user can change PUT /coefficients and users get /coefficients' do
    admin_user
    post '/login', email: user_in_storage['email'], password: user_in_storage_password
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']

    put('/coefficients',
         {
          score: 10,
          another_score: 20,
          session_key: session_key
        })
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['score'].should == 10
    response_data['another_score'].should == 20
    response_data['session_key'].should == nil

    get('/coefficients', session_key: session_key)
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['score'].should == 10
    response_data['another_score'].should == 20
    response_data['session_key'].should == nil
  end

  it '/puzzles can list all puzzles' do
    admin_user
    post '/login', email: user_in_storage['email'], password: user_in_storage_password
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']

    post('/sets',
         {
           year: Time.now.year,
           month: Time.now.month,
           name: 'Cool set',
           published: 'true',
           puzzles: [{ name: 'puzzle1' }, { name: 'puzzle2' }].to_json,
           type: 'golden',
           session_key: session_key
         })


    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    admin_set1_id = response_data['id']

    post('/sets',
         {
           year: Time.now.year,
           month: (Time.now - 60 * 60 * 24 * 31).month,
           name: 'another Cool set',
           published: 'true',
           puzzles: [{ name: 'puzzle3' }, { name: 'puzzle4' }].to_json,
           type: 'golden',
           session_key: session_key
         })

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    admin_set2_id = response_data['id']

    post '/signup', valid_user_data
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    user_id = response_data['me']['id']

    post("/sets/#{admin_set1_id}/buy", { session_key: session_key })
    last_response.status.should == 200
    post("/sets/#{admin_set2_id}/buy", { session_key: session_key })
    last_response.status.should == 200

    get("/puzzles", { session_key: session_key })
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['sets'][0]['id'].should == admin_set2_id
    response_data['sets'][1]['id'].should == admin_set1_id
  end

  it 'doesn\'t set invite_used when user is already registered' do
    VCR.use_cassette('facebook_login_and_friends') do
      get '/facebook/authorize', { access_code: 'access_token' }
      last_response.status.should == 200

      get '/facebook/authorize', { access_code: 'Asome_access_token' }
      last_response.status.should == 200
      response_data = JSON.parse(last_response.body)
      session_key = response_data['session_key']

      get("/facebook/friends", { session_key: session_key })
      last_response.status.should == 200
      last_response_should_be_json
      response_data = JSON.parse(last_response.body)
      id = response_data.first['id']
      id.should_not == nil
      response_data.first['status'].should == 'already_registered'

      post("/facebook/invite", { session_key: session_key, ids: id })
      last_response.status.should == 200

      get("/facebook/friends", { session_key: session_key })
      last_response.status.should == 200
      last_response_should_be_json
      response_data = JSON.parse(last_response.body)
      id = response_data.first['id']
      id.should_not == nil
      response_data.first['status'].should == 'already_registered'
    end
  end

  it 'invite facebook and register friends' do
    VCR.use_cassette('facebook_login_and_friends') do
      get '/facebook/authorize', { access_code: 'Asome_access_token' }
      last_response.status.should == 200
      response_data = JSON.parse(last_response.body)
      session_key = response_data['session_key']

      get("/facebook/friends", { session_key: session_key })
      last_response.status.should == 200
      last_response_should_be_json
      response_data = JSON.parse(last_response.body)
      id = response_data.first['id']
      id.should_not == nil
      response_data.first['status'].should == 'uninvited'

      post("/facebook/invite", { session_key: session_key, ids: id })
      last_response.status.should == 200

      get("/facebook/friends", { session_key: session_key })
      last_response.status.should == 200
      last_response_should_be_json
      response_data = JSON.parse(last_response.body)
      id = response_data.first['id']
      id.should_not == nil
      response_data.first['status'].should == 'invite_sent'

      get '/facebook/authorize', { access_code: 'access_token' }
      last_response.status.should == 200

      get("/facebook/friends", { session_key: session_key })
      last_response.status.should == 200
      last_response_should_be_json
      response_data = JSON.parse(last_response.body)
      id = response_data.first['id']
      id.should_not == nil
      response_data.first['status'].should == 'invite_used'

      get("/me", { session_key: session_key })
      last_response.status.should == 200
      last_response_should_be_json
      response_data = JSON.parse(last_response.body)
      response_data['me']['month_score'].should == 1000
    end
  end

  it 'invite vkontakte friends' do
    VCR.use_cassette('vkontakte_login_and_friends') do
      get '/vkontakte/authorize', { access_code: 'access_token' }
      last_response.status.should == 200
      response_data = JSON.parse(last_response.body)
      session_key = response_data['session_key']

      get("/vkontakte/friends", { session_key: session_key })
      last_response.status.should == 200
      last_response_should_be_json
      response_data = JSON.parse(last_response.body)
      id = response_data.first['id']
      id.should_not == nil
      response_data.first['status'].should == 'uninvited'

      post("/vkontakte/invite", { session_key: session_key, ids: id })
      last_response.status.should == 200

      get("/vkontakte/friends", { session_key: session_key })
      last_response.status.should == 200
      last_response_should_be_json
      response_data = JSON.parse(last_response.body)
      id = response_data.first['id']
      id.should_not == nil
      response_data.first['status'].should == 'invite_sent'
    end
  end

  it '/link_accounts for two registered accounts' do
    post '/signup', valid_user_data
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    session_key1 = response_data['session_key']

    post '/signup', another_valid_user_data
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    session_key2 = response_data['session_key']

    post '/score', session_key: session_key1, score: 150, solved: 10, source: 'reason1'
    post '/score', session_key: session_key1, score: 100, solved: 10, source: 'reason2'
    post '/score', session_key: session_key2, score: 150, solved: 10, source: 'reason3'
    post '/score', session_key: session_key2, score: 150, solved: 10, source: 'reason2'

    post '/link_accounts', { session_key1: session_key1, session_key2: session_key2 }
    last_response.status.should == 403
    last_response_should_be_json
  end

  it '/link_accounts for registered and facebook accounts' do
    VCR.use_cassette('facebook_success_login') do
      get '/facebook/login'
      last_response.status.should == 302
      # Here we assume user logs in into facebook and aproves our app
      get '/facebook/authorize', { code: 'AQALG4kdgy528xTY0KlG-WlKG9vzMyOZ3TGUhquw_yWMVYa0E7o0dyBvSd9uNFigtbhQaIMX7i33pL2G9DhIhIBk1LgEUfwALggMCXnT7ZFW-B7iS0_0giZEGllyJQgvpJPyLAXykWGMXt3S2l-Cm7AyEA9DsbmB646QhZVtIiKL-cGH9aS7omfCMgRpKZewJkbLvjdR7pfG5k6YT1iYAth6' }
      last_response_should_be_json
      last_response.status.should == 200
      response_data = JSON.parse(last_response.body)
      session_key1 = response_data['session_key']


      post '/signup', another_valid_user_data
      last_response.status.should == 200
      last_response_should_be_json
      response_data = JSON.parse(last_response.body)
      session_key2 = response_data['session_key']

      post '/score', session_key: session_key1, score: 150, solved: 10, source: 'reason1'
      post '/score', session_key: session_key1, score: 100, solved: 10, source: 'reason2'
      post '/score', session_key: session_key2, score: 150, solved: 10, source: 'reason3'
      post '/score', session_key: session_key2, score: 150, solved: 10, source: 'reason2'

      post '/link_accounts', { session_key1: session_key1, session_key2: session_key2 }
      last_response.status.should == 200
      last_response_should_be_json

      post '/score', session_key: session_key1, score: 150, solved: 10, source: 'reason1'

      get '/me', { session_key: session_key1 }
      last_response.status.should == 200
      last_response_should_be_json
      response_data = JSON.parse(last_response.body)
      response_data['me']['month_score'].should == 600

      get '/me', { session_key: session_key2 }
      last_response.status.should == 200
      last_response_should_be_json
      response_data = JSON.parse(last_response.body)
      response_data['me']['month_score'].should == 600
    end
  end

  it '/register_device adds device to list of avialible devices' do
    post '/signup', valid_user_data
    post '/login', email: valid_user_data['email'], password: valid_user_data['password']
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    post '/register_device', session_key: session_key, id: 'device1'
    last_response.status.should == 200

    admin_user
    post '/login', email: user_in_storage['email'], password: user_in_storage_password
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    get '/devices', session_key: session_key
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data.first['id'].should == 'device1'
  end
end
