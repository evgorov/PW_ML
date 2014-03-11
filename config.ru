# encoding: utf-8

ROOT_DIR = File.dirname(__FILE__)
libdir = File.join(ROOT_DIR, 'lib')
$LOAD_PATH.unshift(libdir) unless $LOAD_PATH.include?(libdir)

require 'app_config'
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
require 'middleware/index_page'
require 'middleware/routes/cascade'
require 'middleware/etag'
require 'newrelic_rpm'
require 'new_relic/rack/agent_hooks'
require 'new_relic/rack/error_collector'

AppConfig.load!("#{ROOT_DIR}/config/app.yml", :env => ENV['RACK_ENV'])

use NewRelic::Rack::AgentHooks
use NewRelic::Rack::ErrorCollector

use Rack::Cache
use Etag

use IndexPage
use Rack::StaticCache, :urls => ["/css", "/img", "/js", "/favicon.ico", "/index.html"],
                       :root => "public", :versioning => false, :duration => 0

use Middleware::RedisMiddleware

use Middleware::Uploader, serve_from: AppConfig.uploader[:path]

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

AppConfig.providers.each do |provider_name, provider_options|
  provider = Middleware::OauthProviderAuthorization::Provider.new( provider_name.to_s, provider_options)
  use Middleware::OauthProviderAuthorization, provider
end

# Push notification settings
GCM.host = AppConfig.android[:gcm_host]
GCM.format = :json
GCM.key = AppConfig.android[:gcm_key]
use Middleware::APNPusher, AppConfig.ios[:pusher]

use Middleware::BasicRegistration
use Middleware::PasswordReset

use Middleware::Cascade
use Rack::ContentLength
use Rack::ContentType, "application/json"

run lambda { |env|  [404, {} ,['{"message":"not found"}']] }
