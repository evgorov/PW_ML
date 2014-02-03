require 'model/device'
require 'model/android_device'

module Middleware
  class DeviceTracking < Sinatra::Base

    helpers do
      def current_user
        @current_user ||= env['token_auth'].user.user_data
      end
    end

    before do
      content_type 'application/json'
    end

    # TODO: Legacy route, duplicates +POST /ios/register_device+
    post '/register_device' do
      env['token_auth'].authorize!
      Device.new.storage(env['redis']).tap{ |o| o['id'] = params['id'] }.save
      { 'message' => 'ok' }.to_json
    end

    post '/ios/register_device' do
      env['token_auth'].authorize!
      Device.new.storage(env['redis']).tap{ |o| o['id'] = params['id'] }.save
      { 'message' => 'ok' }.to_json
    end

    post '/android/register_device' do
      env['token_auth'].authorize!
      AndroidDevice.new.storage(env['redis']).tap{ |o| o['id'] = params['id'] }.save
      { 'message' => 'ok' }.to_json
    end

  end
end
