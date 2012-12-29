require 'middleware/basic_registration'
require 'rack/test'
require 'json'
require './spec/fixtures'

describe Middleware::BasicRegistration do

  include Rack::Test::Methods
  include_context 'fixtures'

  def last_response_should_be_json
    last_response.content_type.split(';')[0].should == 'application/json'
    lambda { JSON.parse(last_response.body) }.should_not raise_error
  end

  def app
    Middleware::BasicRegistration.settings.environment = :test
    Middleware::BasicRegistration.new
  end

  it 'POST /signup creates user' do
    storage = stub(:storage)
    UserFactory.should_receive(:create_user).with(storage, valid_user_data)
    post('/signup',
         valid_user_data,
         {
           'token_auth' => stub.as_null_object,
           'redis' => storage
         })
    last_response.status.should == 200
  end

  it 'POST /signup returs 403 when required fields are not set' do
    UserFactory.should_not_receive(:create_user)
    post '/signup', valid_user_data.tap { |o| o.delete('name') }
    last_response.status.should == 403
  end

  it 'POST /signup strips out keys that are not required' do
    storage = stub(:storage)
    UserFactory.should_receive(:create_user).with(storage, valid_user_data)
    post('/signup',
         valid_user_data.merge(additional_param: 1234),
         {
           'token_auth' => stub.as_null_object,
           'redis' => storage
         })
    last_response.status.should == 200
  end

  it 'POST /signup returns session token' do
    UserFactory.stub(create_user: 'USER')
    token_auth_strategy = mock(:token_auth_strategy)
    token_auth_strategy.should_receive(:create_token).with('USER').and_return('SESSION_KEY')
    post '/signup', valid_user_data, { 'token_auth' => token_auth_strategy }
    last_response.status.should == 200
    last_response_should_be_json
    response = JSON.parse(last_response.body)
    response['session_key'].should == 'SESSION_KEY'
  end

  it 'POST /signup returns created user' do
    UserFactory.stub(create_user: 'USER')
    post '/signup', valid_user_data, { 'token_auth' => stub.as_null_object }
    last_response.status.should == 200
    last_response_should_be_json
    response = JSON.parse(last_response.body)
    response['me'].should == 'USER'
  end

  it 'POST /login returns session key if matching user has been found' do
    RegisteredUser.should_receive(:storage).and_return(RegisteredUser)
    RegisteredUser.should_receive(:authenticate).with('john@example.org', '12345').and_return('USER')
    token_auth_strategy = mock(:token_auth_strategy)
    token_auth_strategy.should_receive(:create_token).with('USER').and_return('SESSION_KEY')

    post('/login',
         { email: 'john@example.org', password: '12345' },
         { 'token_auth' => token_auth_strategy })

    last_response.status.should == 200
    last_response_should_be_json
    response = JSON.parse(last_response.body)
    response['session_key'].should == 'SESSION_KEY'
  end

  it 'POST /returns user object' do
    RegisteredUser.stub(storage: RegisteredUser)
    RegisteredUser.stub(authenticate: 'USER')
    post('/login',
         { email: 'john@example.org', password: '12345' },
         { 'token_auth' => stub.as_null_object })
    last_response.status.should == 200
    last_response_should_be_json
    response = JSON.parse(last_response.body)
    response['me'].should == 'USER'
  end

  it 'POST /login returns 403 if user matching user hasn\'t been found' do
    RegisteredUser.should_receive(:storage).and_return(RegisteredUser)
    RegisteredUser.should_receive(:authenticate).with('john@example.org', '12345').and_raise(BasicModel::NotFound)
    post '/login', { email: 'john@example.org', password: '12345' }
    last_response.status.should == 403
  end

end
