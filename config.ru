# encoding: utf-8
libdir = File.join(File.dirname(__FILE__), 'lib')
$LOAD_PATH.unshift(libdir) unless $LOAD_PATH.include?(libdir)

require "bundler/setup"

require 'rack/contrib/static_cache'
require 'rack/cache'
require 'middleware/apn_pusher'
require 'middleware/redis_middleware'
require 'middleware/basic_registration'
require 'middleware/token_auth_strategy'
require 'middleware/oauth_provider_authorization'
require 'middleware/password_reset'
require 'middleware/counter'
require 'middleware/uploader'
require 'middleware/routes/cascade'
require 'middleware/etag'

class IndexPage

  def initialize(app)
    @app = app
  end

  def call(env)
    env['PATH_INFO'] = '/index.html' if env['PATH_INFO'] =~ %r{^/[0-9]*$}
    @app.call(env)
  end
end

use Rack::Cache
use Etag

use IndexPage
use Rack::StaticCache, :urls => ["/css", "/img", "/js", "/favicon.ico", "/index.html"],
                       :root => "public", :versioning => false, :duration => 0

use Middleware::RedisMiddleware

use Middleware::Uploader, serve_from: '/img/uploads'

use Middleware::Counter, counter_mappings: {
  [200, %r{/login}] => 'logins',
  [200, %r{/sets/[^/]*/buy}] => 'sets_bought',
  [200, %r{/hints/buy}] => 'hints_bought',
  [200, %r{/score}] => 'scored'
}

Middleware::TokenAuthStrategy.serialize_user_proc = lambda { |env, u| [u.class.name, u.id].join('#') }
Middleware::TokenAuthStrategy.deserialize_user_proc = lambda do |env, key|
  klass, id = key.split('#', 2)
  Kernel.const_get(klass).storage(env['redis']).load(id)
end

use Middleware::TokenAuthStrategy

facebook_options = {
  client_id: 'FACEBOOK_CLIENT_ID',
  client_secret: 'FACEBOOK_CLIENT_SECRET',
  login_dialog_uri: 'https://facebook.com/dialog/oauth',
  access_token_uri: 'https://graph.facebook.com/oauth/access_token',
  scope: 'email,user_birthday,user_about_me,publish_stream'
}
facebook_provider = Middleware::OauthProviderAuthorization::Provider.new('facebook', facebook_options)
use Middleware::OauthProviderAuthorization, facebook_provider


vkontakte_options = {
  client_id: 'VKONTAKTE_CLIENT_ID',
  client_secret: 'VKONTAKTE_CLIENT_SECRET',
  redirect_uri: 'http://oauth.vk.com/blank.html',
  login_dialog_uri: 'https://vkontakte.com/dialog/oauth',
  access_token_uri: 'https://graph.vkontakte.com/oauth/access_token',
  scope: 'offline,email,user_birthday,user_about_me,wall'
}
vkontakte_provider = Middleware::OauthProviderAuthorization::Provider.new('vkontakte', vkontakte_options)
use Middleware::OauthProviderAuthorization, vkontakte_provider

pusher_params = {
  certificate: "/path/to/cert.pem",
  passphrase:  "",
  gateway:     "gateway.push.apple.com",
  port:        2195,
  retries:     3
}
use Middleware::APNPusher, pusher_params

use Middleware::BasicRegistration
use Middleware::PasswordReset

use Middleware::Cascade
use Rack::ContentLength
use Rack::ContentType, "application/json"

run lambda { |env|  [404, {} ,['{"message":"not found"}']] }
