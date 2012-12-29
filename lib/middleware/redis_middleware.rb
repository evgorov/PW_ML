require 'storage'

module Middleware
  class RedisMiddleware
    def initialize(app, options = {}); @app, @options = app, options; end

    def call(env)
      env['redis'] = Redis.new(@options)
      @app.call(env)
    end
  end
end
