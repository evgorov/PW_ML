require 'sinatra/base'
require 'json'
require 'securerandom'
require 'pony'

module Middleware
  class PasswordReset < Sinatra::Base

    set :views, 'views/password_reset'
    error(User::NotFound) { halt(404) }

    helpers do
      def generate_html_body(token)
        Tilt.new(self.class.views + '/email_template.erb').render(nil, token: token)
      end

      def redis
        env['redis'].namespace('PasswordReset')
      end
    end

    post '/forgot_password' do
      user = RegisteredUser.storage(env['redis']).load_by_key(params['email'])
      token = SecureRandom.uuid
      redis.set(token, user['email'])
      Pony.mail(to: user['email'],
                from: 'noreply@prizeword.ru',
                subject: 'Prizeword password reset',
                html_body: generate_html_body(token))
      content_type 'application/json'
      { 'message' => 'Email sent' }.to_json
    end

    get '/password_reset' do
      token = params['token']
      halt(404) if !token || !redis.get(token)
      erb :password_reset, locals: { token: token }
    end

    post '/password_reset' do
      token = params['token']
      halt(403) unless params['password']
      halt(404) if !token || !(email = redis.get(token))
      user = RegisteredUser.storage(env['redis']).load_by_key(email)
      user['password'] = params['password']
      user.save
      redis.del(token)
      erb :success
    end

  end
end
