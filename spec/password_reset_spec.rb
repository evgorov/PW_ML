require 'middleware/password_reset'
require 'rack/test'
require 'json'
require './spec/fixtures'

describe Middleware::PasswordReset do
  include Rack::Test::Methods
  include_context 'fixtures'

  def last_response_should_be_json
    last_response.content_type.split(';')[0].should == 'application/json'
    lambda { JSON.parse(last_response.body) }.should_not raise_error
  end

  def app
    Middleware::PasswordReset.settings.environment = :test
    Middleware::PasswordReset.new
  end

  let(:redis){ stub(:redis).as_null_object }

  context 'not valid email' do

    let(:user) { mock(:user) }

    before(:each) do
      user.should_receive(:load_by_key).with('not_registered@example.org').and_raise(User::NotFound)
      RegisteredUser.stub(storage: user)
    end

    it 'POST /forgot_password return 404 if email is not registered' do
      post('/forgot_password',
           { email: 'not_registered@example.org' },
           { 'redis' => redis} )
      last_response.status.should == 404
    end
  end

  context 'valid user email' do

    let(:user){ mock(:user).as_null_object }

    before(:each) do
      user.stub(:[]).with('email'){ 'registered@example.org' }
      user.stub(:load_by_key).with('registered@example.org'){ user }
      RegisteredUser.stub(storage: user)
      Pony.stub(:mail)
    end

    it 'POST /forgot_password trigger email sending if user is registered' do
      Pony.should_receive(:mail) { |h| h[:to].should == 'registered@example.org' }
      user.should_receive(:load_by_key).with('registered@example.org').and_return(user)

      post('/forgot_password',
           { email: 'registered@example.org' },
           { 'redis' => redis })
      last_response.status.should == 200
      last_response_should_be_json
    end

    it 'POST /forgot_password creates token for password_reset' do
      token = nil
      redis.should_receive(:namespace)
      redis.should_receive(:set){ |t, email| token = t; email.should == 'registered@example.org' }
      Pony.should_receive(:mail) { |h| h[:html_body].should =~ /#{token}/ }

      post('/forgot_password',
           { email: 'registered@example.org' },
           { 'redis' => redis })
      token.should_not == nil
      last_response.status.should == 200
    end

    it 'GET /password_reset return 404 if token is not passed' do
      get('/password_reset',
           { },
           { 'redis' => redis })
      last_response.status.should == 404
    end

    it 'GET /password_reset return 404 if token not found' do
      redis.should_receive(:get).with('not_valid_token').and_return(nil)
      get('/password_reset',
           { token: 'not_valid_token' },
           { 'redis' => redis })
      last_response.status.should == 404
    end

    it 'GET /password_reset return 200 and html for valid token' do
      redis.should_receive(:get).with('valid_token').and_return('registered@example.org')
      get('/password_reset',
           { token: 'valid_token' },
           { 'redis' => redis })
      last_response.status.should == 200
      last_response.body.should_not == ""
    end

    it 'POST /password_reset return 404 if token not found' do
      redis.should_receive(:get).with('not_valid_token').and_return(nil)
      post('/password_reset',
           { token: 'not_valid_token', password: 'new_password' },
           { 'redis' => redis })
      last_response.status.should == 404
    end

    it 'POST /password_reset return 200 and html for valid token' do
      redis.should_receive(:get).with('valid_token').and_return('registered@example.org')
      user.should_receive(:[]=).with('password', 'new_password')
      user.should_receive(:save)

      post('/password_reset',
           { token: 'valid_token', password: 'new_password' },
           { 'redis' => redis })
      last_response.status.should == 200
    end

    it 'POST /password_reset deletes token after using it' do
      redis.should_receive(:get).with('valid_token').and_return('registered@example.org')
      redis.should_receive(:del).with('valid_token')

      post('/password_reset',
           { token: 'valid_token', password: 'new_password' },
           { 'redis' => redis })
      last_response.status.should == 200
    end

    it 'POST /password_reset return 403 if password is empty' do
      post('/password_reset',
           { token: 'valid_token' },
           { 'redis' => redis })
      last_response.status.should == 403
    end
  end
end
