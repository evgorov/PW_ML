require 'middleware/token_auth_strategy'
require './spec/fake_redis_middleware'
require 'rack/test'

describe Middleware::TokenAuthStrategy do

  include Rack::Test::Methods
  include_context 'fake_redis_middleware'

  before(:each) do
    subject.serialize_user_proc = nil
    subject.deserialize_user_proc = nil
  end

  context 'simple app' do

    def app
      app = lambda { |env| [200, {}, ["ok"]]}
      fake_redis_middleware.new(subject.new(app))
    end

    it 'works' do
      get '/'
      last_response.status.should == 200
      last_response.body.should == "ok"
    end

  end

  context 'with authorization check' do

    def app
      app = lambda do |env|
        if env['PATH_INFO'] == '/create_token'
          token = env['token_auth'].create_token
          [200, {}, [token]]
        else
          env['token_auth'].authorize!
          [200, {}, ["ok"]]
        end
      end

      fake_redis_middleware.new(subject.new(app))
    end

    it 'returns 401 when called without token' do
      get '/'
      last_response.status.should == 401
    end

    it 'returns 200 when called valid token' do
      get '/create_token'
      last_response.status.should == 200
      token = last_response.body
      get '/', { session_key: token }
      last_response.status.should == 200
      last_response.body.should == 'ok'
    end

    it 'returns 401 when called with invalid token' do
      get '/', { session_key: 'some_invalid_token' }
      last_response.status.should == 401
    end
  end

  context 'stores session in redis' do

    def app
      app = lambda do |env|
        if env['PATH_INFO'] == '/create_token'
          token = env['token_auth'].create_token
          [200, {}, [token]]
        else
          env['token_auth'].authorize!
          [200, {}, ["ok"]]
        end
      end

      fake_redis_middleware.new(subject.new(app))
    end

    it 'returns 200 when called valid token' do
      redis = fake_redis_middleware
      redis.should_receive(:setex)
      get '/create_token'
      last_response.status.should == 200
      token = last_response.body
      redis.should_receive(:get).and_return(token)
      get '/', { session_key: token }
      last_response.status.should == 200
      last_response.body.should == 'ok'
    end
  end

  context 'serialize user' do

    def app
      app = lambda do |env|
        case env['PATH_INFO']
        when '/create_token'
          token = env['token_auth'].create_token(@user)
          [200, {}, [token]]
        when '/update_user'
          env['token_auth'].authorize!
          token = env['token_auth'].update_user(@new_user)
          [200, {}, [token]]
        else
          env['token_auth'].authorize!
          @in_app_user = env['token_auth'].user
          [200, {}, ["ok"]]
        end
      end

      fake_redis_middleware.new(subject.new(app))
    end

    it 'serializes and deserializes user' do
      @user = stub
      @store_user = mock
      subject.serialize_user_proc = @store_user
      @store_user.should_receive(:call).with(anything, @user).and_return('1234')
      get '/create_token'
      last_response.status.should == 200
      token = last_response.body
      @deserialize_user = mock
      @deserialize_user.should_receive(:call).with(anything, '1234').and_return(@user)
      subject.deserialize_user_proc = @deserialize_user
      get '/', session_key: token
      last_response.status.should == 200
      @in_app_user.should == @user
    end

    it 'can update user without, while keeping the token' do
      @user = stub(:old_user)
      @store_user = mock(:store_user_proc)
      subject.serialize_user_proc = @store_user
      @store_user.should_receive(:call).with(anything, @user).and_return('1234')
      get '/create_token'
      last_response.status.should == 200
      token = last_response.body

      @deserialize_user = mock(:retrieve_user_proc)
      @deserialize_user.should_receive(:call).exactly(2).with(anything, '1234').and_return(@user)
      subject.deserialize_user_proc = @deserialize_user
      get '/', session_key: token
      last_response.status.should == 200
      @in_app_user.should == @user

      @new_user = stub(:new_user)
      @store_user.should_receive(:call).with(anything, @new_user).and_return('12345')
      get '/update_user', session_key: token

      @deserialize_user.should_receive(:call).with(anything, '12345').and_return(@new_user)
      get '/', session_key: token
      last_response.status.should == 200
      @in_app_user.should == @new_user
    end

    it 'cannot update user without token' do
      fake_redis_middleware.
        should_receive(:get).
        with('fake_token').
        and_return(nil)
      get '/update_user', session_key: 'fake_token'
      last_response.status.should == 401
    end
  end
end
