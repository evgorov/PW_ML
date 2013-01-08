require 'sinatra/base'
require 'user_factory'
require 'model/user'

module Middleware
  class BasicRegistration < Sinatra::Base

    before do
      content_type 'application/json'
    end

    REQUIRED_FIELDS = %w[email name surname password]
    OPTIONAL_FIELDS = %w[birthdate userpic city]
    ALLOWED_FIELDS = REQUIRED_FIELDS + OPTIONAL_FIELDS

    helpers do
      def validate_signup_params!
        check_required_fileds!
        delete_not_matching_params!
      end

      def required_field_error(field)
        { message: "field #{field} id required" }.to_json
      end

      def check_required_fileds!
        REQUIRED_FIELDS.each do |field|
          halt(403, required_field_error(field)) unless params.has_key?(field)
        end
      end

      def delete_not_matching_params!
        params.delete_if { |k, v| !ALLOWED_FIELDS.include?(k) }
      end
    end

    error(BasicModel::NotFound) { halt(403, { message: 'Invalid username or password' }.to_json) }

    post '/signup' do
      validate_signup_params!
      user = UserFactory.create_user(env['redis'], params)
      session_key = env['token_auth'].create_token(user)
      { session_key: session_key, me: user }.to_json
    end

    post '/login' do
      user = RegisteredUser.storage(env['redis']).authenticate(params['email'], params['password'])
      session_key = env['token_auth'].create_token(user)
      { session_key: session_key, me: user }.to_json
    end
  end
end
