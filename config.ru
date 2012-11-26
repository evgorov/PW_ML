# encoding: utf-8
libdir = File.join(File.dirname(__FILE__), 'lib')
$LOAD_PATH.unshift(libdir) unless $LOAD_PATH.include?(libdir)

require "bundler/setup"

require 'middleware/redis_middleware'
require 'middleware/basic_registration'
require 'middleware/token_auth_strategy'
require 'middleware/oauth_provider_authorization'
require 'middleware/users'
require 'middleware/admin'

use Middleware::RedisMiddleware

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

use Middleware::Users

run Middleware::Admin
