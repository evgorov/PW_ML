require 'middleware/oauth_provider_authorization'
require 'rack/test'
require 'webmock/rspec'
require 'json'

describe Middleware::OauthProviderAuthorization::Provider do

  subject { Middleware::OauthProviderAuthorization::Provider }
  let(:params) { {
      client_id: '1234',
      client_secret: '12345',
      redirect_uri: 'http://example.org/redirect_uri',
      login_dialog_uri: 'http://example.org/dialog/oauth',
      access_token_uri: 'http://example.org/ouath/access_token',
      scope: 'email'
    } }

  it { lambda { subject.new('test', params) }.should_not raise_error }

  [:client_id,
   :client_secret,
   :login_dialog_uri,
   :access_token_uri,
   :scope].each do |attribute|
    it { subject.new('test', params).respond_to?(attribute).should be_true }
    it { lambda { subject.new('test', params.delete(attrubute)) }.should raise_error }
  end
end

describe Middleware::OauthProviderAuthorization do

  include Rack::Test::Methods

  def app
    Middleware::OauthProviderAuthorization.settings.environment = :test
    options = {
      client_id: 'CLIENT_ID',
      client_secret: 'CLIENT_SECRET',
      login_dialog_uri: 'https://example.org/dialog/oauth',
      access_token_uri: 'https://example.org/ouath/access_token',
      scope: 'email'
    }
    provider = Middleware::OauthProviderAuthorization::Provider.new('test', options)
    app = lambda { |env| [201, {}, []]}
    Middleware::OauthProviderAuthorization.new(app, provider)
  end

  def stub_token_request
    stub_request(:get, "https://example.org/ouath/access_token")
      .with(query: {
              'client_id' => 'CLIENT_ID',
              'redirect_uri' => 'http://example.org/test/authorize',
              'client_secret' => 'CLIENT_SECRET',
              'code' => 'CODE'
            })
      .to_return(:body => 'access_token=ACCESS_TOKEN',
                 :status => 200,
                 :headers => { 'Content-Length' => 12 } )
  end

  def last_response_should_be_json
    last_response.content_type.split(';')[0].should == 'application/json'
    lambda { JSON.parse(last_response.body) }.should_not raise_error
  end


  before(:each) do
    @token_auth_strategy = stub(:token_auth_strategy)
    @token_auth_strategy.stub(create_token: 'session_key')
    @request = stub_token_request
    @user = stub(:user).as_null_object
    @user.stub(to_json: '"USER_AS_JSON"')
    UserFactory.stub(:find_or_create_test_user).and_return(@user)
  end

  it 'skips requests to wrong provider' do
    get '/some_provider/login'
    last_response.status.should == 201
    get '/some_provider/authorize'
    last_response.status.should == 201
  end

  it 'GET /test/login redirects user to OAUTH Dialog page' do
    get '/test/login'
    last_response.status.should == 302
    uri = URI.parse(last_response.headers['Location'])
    uri.scheme.should == 'https'
    uri.host.should == 'example.org'
    uri.path.should == '/dialog/oauth'
    params = Rack::Utils.parse_query(uri.query)
    params['client_id'].should == 'CLIENT_ID'
    params['redirect_uri'].should == 'http://example.org/test/authorize'
  end

  it 'GET /test/authorize requires code' do
    get '/test/authorize'
    last_response.status.should == 403
  end

  it 'GET /test/authorize requests access_token from test' do
    get '/test/authorize', { code: 'CODE' }, { 'token_auth' => @token_auth_strategy }
    last_response.status.should == 200
    @request.should have_been_requested
  end

  it 'GET /test/authorize returns session_token' do
    token_auth_strategy = mock(:token_auth_strategy)
    token_auth_strategy.should_receive(:create_token).with(@user).and_return('SESSION_KEY')
    get '/test/authorize', { code: 'CODE' }, { 'token_auth' => token_auth_strategy }
    last_response.status.should == 200
    last_response_should_be_json
    response = JSON.parse(last_response.body)
    response['session_key'].should == 'SESSION_KEY'
  end

  it 'finds or creates user on authorize' do
    UserFactory.should_receive(:find_or_create_test_user).with(anything, 'ACCESS_TOKEN')
    get '/test/authorize', { code: 'CODE' }, { 'token_auth' => @token_auth_strategy }
    last_response.status.should == 200
    last_response_should_be_json
    response = JSON.parse(last_response.body)
    response['me'].should == "USER_AS_JSON"
  end

end
