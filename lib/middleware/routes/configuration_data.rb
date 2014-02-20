#encoding: utf-8

require 'sinatra/base'
require 'model/service_message'
require 'model/coefficients'

module Middleware
  class ConfigurationData < Sinatra::Base

    helpers do
      def current_user
        @current_user ||= env['token_auth'].user.user_data
      end
    end

    before do
      content_type 'application/json'
    end

    get '/service_messages' do
      env['token_auth'].authorize!
      m = ServiceMessage.storage(env['redis']).messages
      {
        message1: m['message1'],
        message2: m['message2'],
        message3: m['message3']
      }.to_json
    end

    get '/users' do
      expires 120, :public, :must_revalidate
      result = {}
      users = UserData.storage(env['redis']).users_by_rating(params['page'].to_i).map(&:as_json).map{ |o| o.except('providers', 'email', 'hints', 'birthdate')}
      result['users'] = users
      result['me'] = current_user if env['token_auth'].authorized?
      result.to_json
    end

    get '/coefficients' do
      env['token_auth'].authorize!
      Coefficients.storage(env['redis']).coefficients.to_json
    end
  end
end
