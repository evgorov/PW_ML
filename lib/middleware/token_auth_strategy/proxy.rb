require 'securerandom'

module Middleware::TokenAuthStrategy
  class Proxy

    def initialize(env)
      @env = env
      @redis = env['redis'].namespace('Tokens')
      @request = Rack::Request.new(env)
    end

    def authorize!
      unauthorized! unless self.authorized?
    end

    def unauthorized!
      throw :token_auth_strategy, :failure
    end

    def authorized?
      @user_key || (@user_key = @redis.get(@request.params['session_key']))
    end

    def user
      self.authorize!
      if ::Middleware::TokenAuthStrategy.deserialize_user_proc
        ::Middleware::TokenAuthStrategy.deserialize_user_proc.call(@env, @user_key)
      else
        @user_key
      end
    end

    def create_token(user = true)
      SecureRandom.uuid.tap { |uuid| store_user(uuid, user) }
    end

    def update_user(user)
      self.authorize!
      store_user(@request.params['session_key'], user)
      @request.params['session_key']
    end

    private

    def store_user(key, user)
      value = if ::Middleware::TokenAuthStrategy.serialize_user_proc
                ::Middleware::TokenAuthStrategy.serialize_user_proc.call(@env, user)
              else
                user.to_s
              end

      @redis.set(key, value)
    end

  end
end
