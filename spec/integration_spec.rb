require 'middleware/redis_middleware'
require 'middleware/basic_registration'
require 'middleware/token_auth_strategy'
require 'middleware/oauth_provider_authorization'
require 'middleware/users'
require 'middleware/admin'

require 'rack/test'
require 'webmock'
require 'json'

require './spec/fixtures'

# Lets add some speed!
BCrypt::Engine::DEFAULT_COST = 1

describe 'Integration spec' do

  before(:all) do
    require 'vcr'
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
    Redis.new.flushdb
  end

  include_context 'fixtures'
  include Rack::Test::Methods

  def last_response_should_be_json
    last_response.content_type.split(';')[0].should == 'application/json'
    lambda { JSON.parse(last_response.body) }.should_not raise_error
  end

  def app
    app = Rack::Builder.new do
      use Middleware::RedisMiddleware

      Middleware::TokenAuthStrategy.serialize_user_proc = lambda { |env, u| u.id }
      Middleware::TokenAuthStrategy.deserialize_user_proc = lambda do |env, key|
        provider, id = key.split('#', 2)
        User.load_by_provider_and_key(env['redis'], provider, id)
      end
      use Middleware::TokenAuthStrategy

      facebook_options = {
        client_id: '390010757745933',
        client_secret: '650d73bdb360c1e06719468b8e5eeddd',
        redirect_uri: '/redirect_uri',
        login_dialog_uri: 'https://facebook.com/dialog/oauth',
        access_token_uri: 'https://graph.facebook.com/oauth/access_token',
        scope: 'email,user_birthday,user_about_me,publish_stream'
      }
      facebook_provider = Middleware::OauthProviderAuthorization::Provider.new('facebook', facebook_options)
      Middleware::OauthProviderAuthorization.settings.environment = :test
      use Middleware::OauthProviderAuthorization, facebook_provider

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
    u = RegisteredUser.new
    u.merge!(user_in_storage)
    u['role'] = 'admin'
    u.storage(Redis.new).save
    u
  end

  it 'basic registration' do
    post '/signup', valid_user_data
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    session_key.should_not == nil
    response_data['me'].should == valid_user_data_user_as_json

    get '/me', { session_key: session_key }
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['me'].should == valid_user_data_user_as_json

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
    response_data['me'].should == valid_user_data_user_as_json

    get '/me', { session_key: new_session_key }
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['me'].should == valid_user_data_user_as_json
  end

  it 'updates basic_information' do
    post '/signup', valid_user_data
    post '/login', email: valid_user_data['email'], password: valid_user_data['password']
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    post '/me', { session_key: session_key, email: 'new_email@example.org' }
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    new_valid_data = valid_user_data_user_as_json.merge('email' => 'new_email@example.org',
                                                        'id' => 'registered#new_email@example.org')
    response_data['me'].should == new_valid_data
    get '/me', { session_key: session_key }
    response_data = JSON.parse(last_response.body)
    response_data['me'].should == new_valid_data
  end

  it 'facebook registration' do
    VCR.use_cassette('facebook_success_login') do
      get '/facebook/login'
      last_response.status.should == 302
      # Here we assume user logs in into our app
      get '/facebook/authorize', { code: 'AQALG4kdgy528xTY0KlG-WlKG9vzMyOZ3TGUhquw_yWMVYa0E7o0dyBvSd9uNFigtbhQaIMX7i33pL2G9DhIhIBk1LgEUfwALggMCXnT7ZFW-B7iS0_0giZEGllyJQgvpJPyLAXykWGMXt3S2l-Cm7AyEA9DsbmB646QhZVtIiKL-cGH9aS7omfCMgRpKZewJkbLvjdR7pfG5k6YT1iYAth6' }
      last_response_should_be_json
      last_response.status.should == 200
      response_data = JSON.parse(last_response.body)
      session_key = response_data['session_key']
      session_key.should_not == nil
      response_data['me'].should == valid_facebook_user_data_user_as_json

      get '/me', { session_key: session_key }
      last_response_should_be_json
      last_response.status.should == 200
      response_data = JSON.parse(last_response.body)
      response_data['me'].should == valid_facebook_user_data_user_as_json
    end
  end

  it 'basic rating' do
    20.times { |i| post '/signup', valid_user_data.merge('email' => "email#{i}@example.org") }

    post '/login', email: 'email4@example.org', password: valid_user_data['password']
    last_response.status.should == 200
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    post '/score', session_key: session_key, score: 100
    last_response.status.should == 200
    post '/score', session_key: session_key, score: 100
    last_response.status.should == 200

    post '/login', email: 'email2@example.org', password: valid_user_data['password']
    last_response.status.should == 200
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    post '/score', session_key: session_key, score: 150
    last_response.status.should == 200

    post '/login', email: 'email10@example.org', password: valid_user_data['password']
    last_response.status.should == 200
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    post '/score', session_key: session_key, score: 80

    post '/login', email: 'email1@example.org', password: valid_user_data['password']
    last_response.status.should == 200
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']
    post '/score', session_key: session_key, score: 70
    last_response_should_be_json
    last_response.status.should == 200

    get '/users'
    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['users'][0]['email'].should == 'email4@example.org'
    response_data['users'][1]['email'].should == 'email2@example.org'
    response_data['users'][2]['email'].should == 'email10@example.org'
    response_data['users'][3]['email'].should == 'email1@example.org'
  end

  it 'simple user cannot use admin' do
    post '/signup', valid_user_data
    post '/login', email: valid_user_data['email'], password: valid_user_data['password']
    last_response.status.should == 200
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']

    post '/sets/123/publish'
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
           year: 2012,
           month: 10,
           name: 'Cool set',
           puzzles: [{ name: 'puzzle1' }, { name: 'puzzle2' }].to_json,
           type: 'golden',
           session_key: session_key
         })

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    admin_set_id = response_data['id']

    post("/sets/#{admin_set_id}/publish", { session_key: session_key })
    last_response.status.should == 200
    last_response_should_be_json

    get('/sets_available')
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

    response_data['year'].should == Time.now.year
    response_data['month'].should == Time.now.month
    response_data['user_id'].should == user_id
    response_data['user_id'].should == user_id
    response_data['sets'][0]['id'].should == set_id
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

  xit 'admin' do
    admin_user
    post '/login', email: user_in_storage['email'], password: user_in_storage_password

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    session_key = response_data['session_key']

    post('/sets',
         {
           year: 2012,
           month: 10,
           name: 'Cool set',
           puzzles: [{ name: 'puzzle1' }, { name: 'puzzle2' }].to_json,
           type: 'golden',
           session_key: session_key
         })

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    admin_set_id1 = response_data['id']

    post("/sets/#{admin_set_id1}/publish", { session_key: session_key })
    last_response.status.should == 200
    last_response_should_be_json

    post('/sets',
         {
           year: 2012,
           month: 10,
           name: 'Cool set2',
           puzzles: [{ name: 'puzzle1' }, { name: 'puzzle2' }].to_json,
           type: 'silver',
           session_key: session_key
         })

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    admin_set_id2 = response_data['id']

    post("/sets/#{admin_set_id2}/publish", { session_key: session_key })
    last_response.status.should == 200
    last_response_should_be_json

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

  it 'invte users'

  it 'service messages'
end
