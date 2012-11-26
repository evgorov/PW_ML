require 'redis'
require 'redis/namespace'

class Redis
  def namespace(key)
    Redis::Namespace.new(key, :redis => self)
  end
end

class Redis
  class Namespace
    def namespace(key)
      Redis::Namespace.new(key, self.redis)
    end
  end
end

module Middleware
  class RedisMiddleware
    def initialize(app, options = {}); @app, @options = app, options; end

    def call(env)
      env['redis'] = Redis.new(@options)
      @app.call(env)
    end
  end
end
