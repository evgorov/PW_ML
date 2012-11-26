require 'sinatra/base'
require 'httparty'
require 'json'

class Middleware::OauthProviderAuthorization < Sinatra::Base

  class Provider

    REQUIRED_ATTRIBUTES = [:client_id,
                           :client_secret,
                           :login_dialog_uri,
                           :access_token_uri,
                           :scope]

    attr_reader :name
    REQUIRED_ATTRIBUTES.each do |attr|
      define_method(attr) { @options[attr] }
    end

    def initialize(name, options)
      unless REQUIRED_ATTRIBUTES.all? { |o| options.has_key?(o) }
        raise ArgumentError
      end
      @name, @options = name, options
    end

  end

  def initialize(app, provider)
    @provider = provider
    super(app)
  end

  helpers do

    def build_redirect_uri(path, params = {})
      "#{path}?#{Rack::Utils.build_query(params)}"
    end

    def oauth_redirect_uri
      to("/#{@provider.name}/authorize")
    end

    def get_access_token(code)
      response = HTTParty.get(@provider.access_token_uri,
                   query: {
                     code: code,
                     client_id: @provider.client_id,
                     client_secret: @provider.client_secret,
                     redirect_uri: oauth_redirect_uri
                   },
                   timeout: 1)
      halt(403, { message: "Invalid code" }.to_json) unless response.code == 200
      Rack::Utils.parse_query(response.body)['access_token']
    end
  end

  before do
    content_type 'application/json'
  end

  before('/:provider/*') do
    halt(route_missing) unless params['provider'] == @provider.name
  end

  get '/:provider/login' do
    redirect build_redirect_uri(@provider.login_dialog_uri,
                                client_id: @provider.client_id,
                                redirect_uri: oauth_redirect_uri,
                                scope: @provider.scope)
  end

  get '/:provider_name/authorize' do
    halt 403 unless params.has_key?('code')
    access_token = get_access_token(params['code'])
    user = UserFactory.send("find_or_create_#{@provider.name}_user", env['redis'], access_token)
    session_key = env['token_auth'].create_token(user)
    { session_key: session_key, me: user }.to_json
  end
end
