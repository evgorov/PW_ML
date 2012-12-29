# encoding: utf-8
libdir = File.join(File.dirname(__FILE__), 'lib')
$LOAD_PATH.unshift(libdir) unless $LOAD_PATH.include?(libdir)

require "bundler/setup"

require 'rack/contrib/static_cache'
require 'middleware/redis_middleware'
require 'middleware/basic_registration'
require 'middleware/token_auth_strategy'
require 'middleware/oauth_provider_authorization'
require 'middleware/password_reset'
require 'middleware/users'
require 'middleware/admin'

class IndexPage

  def initialize(app)
    @app = app
  end

  def call(env)
    env['PATH_INFO'] = '/index.html' if env['PATH_INFO'] =~ %r{^/[0-9]*$}
    @app.call(env)
  end
end

use IndexPage
use Rack::StaticCache, :urls => ["/css", "/img", "/js", "/favicon.ico", "/index.html"],
                       :root => "public", :versioning => false

if !ENV["REDISTOGO_URL"]
  use Middleware::RedisMiddleware
else
  uri = URI.parse(ENV["REDISTOGO_URL"])
  use Middleware::RedisMiddleware, { :host => uri.host, :port => uri.port, :password => uri.password }
end

Middleware::TokenAuthStrategy.serialize_user_proc = lambda { |env, u| u.id }
Middleware::TokenAuthStrategy.deserialize_user_proc = lambda do |env, key|
  provider, id = key.split('#', 2)
  User.load_by_provider_and_key(env['redis'], provider, id)
end
use Middleware::TokenAuthStrategy

facebook_options = {
  client_id: 'FACEBOOK_CLIENT_ID',
  client_secret: 'FACEBOOK_CLIENT_SECRET',
  redirect_uri: '/redirect_uri',
  login_dialog_uri: 'https://facebook.com/dialog/oauth',
  access_token_uri: 'https://graph.facebook.com/oauth/access_token',
  scope: 'email,user_birthday,user_about_me,publish_stream'
}
facebook_provider = Middleware::OauthProviderAuthorization::Provider.new('facebook', facebook_options)
use Middleware::OauthProviderAuthorization, facebook_provider


vkontakte_options = {
  client_id: 'VKONTAKTE_CLIENT_ID',
  client_secret: 'VKONTAKTE_CLIENT_SECRET',
  redirect_uri: '/redirect_uri',
  login_dialog_uri: 'https://vkontakte.com/dialog/oauth',
  access_token_uri: 'https://graph.vkontakte.com/oauth/access_token',
  scope: 'email,user_birthday,user_about_me,publish_stream'
}
vkontakte_provider = Middleware::OauthProviderAuthorization::Provider.new('vkontakte', vkontakte_options)
use Middleware::OauthProviderAuthorization, vkontakte_provider

use Middleware::BasicRegistration
use Middleware::PasswordReset

use Middleware::Users

run Middleware::Admin
