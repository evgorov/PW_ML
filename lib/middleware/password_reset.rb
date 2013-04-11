#encoding: utf-8

require 'sinatra/base'
require 'json'
require 'securerandom'
require 'pony'
require 'model/user'

module Middleware
  class PasswordReset < Sinatra::Base

    KEY_TTL = 60 * 60 * 24 * 7

    set :views, 'views/password_reset'
    error(BasicModel::NotFound) { halt(404) }

    helpers do
      def generate_html_body(token, name)
        Tilt.new(self.class.views + '/email_template.erb', default_encoding: 'utf-8').render(nil, token: token, name: name)
      end

      def redis
        env['redis'].namespace('PasswordReset')
      end
    end

    post '/forgot_password' do
      user = RegisteredUser.storage(env['redis']).load_by_key(params['email'])
      token = SecureRandom.uuid
      redis.setex(token, KEY_TTL, user['email'])
      Pony.mail(to: user['email'],
                from: 'noreply@prizeword.ru',
                subject: 'Prizeword password reset',
                html_body: generate_html_body(token, user['name']))
      content_type 'application/json'
      { 'message' => 'Email sent' }.to_json
    end

    get '/password_reset' do
      token = params['token']
      redirect "prizeword://#{token}"
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
