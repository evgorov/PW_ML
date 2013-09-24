require 'sinatra/base'
require 'model/device'

module Middleware
  class APNPusher < Sinatra::Base

    def initialize(app, options)
      @options = options
      super(app)
    end

    before do
      content_type 'application/json'
    end

    helpers do
      def current_user
        @current_user ||= env['token_auth'].user.user_data
      end

      def authorize_admin!
        env['token_auth'].authorize!
        env['token_auth'].unauthorized! unless current_user['role'] == 'admin'
      end
    end

    post '/push_message' do
      authorize_admin!
      # Doing this in request thread for now, if will perform badly on
      # huge number of tokens, move to worker.
      Device.storage(env['redis']).send_notifications(params['message'], @options)
      AndroidDevice.storage(env['redis']).send_notifications(params['message'])
    end
  end
end
