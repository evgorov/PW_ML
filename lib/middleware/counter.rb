require 'json'
require 'storage'

module Middleware

  class Counter
    class Proxy
      def initialize(redis)
        @redis = redis
      end

      def incr(key, time = Time.now.strftime("%Y-%m-%d"))
        @redis.incr([time, key].join(':'))
      end

      def get(*counters)
        result = @redis.mget(*counters).map(&:to_i)
        if counters.size == 1
          result.first
        else
          result
        end
      end
    end

    def initialize(app, options = {})
      @counter_mappings = options.delete(:counter_mappings)
      @app = app
    end

    def call(env)
      env['counter'] = Proxy.new(env['redis'].namespace('counter'))
      result = @app.call(env)
      increment_mappings(result, env) if @counter_mappings
      result
    end

    private

    def increment_mappings(result, env)
      code = result.first
      @counter_mappings.each do |(mappings_code, regexp), counter|
        if env['PATH_INFO'] =~ regexp && (mappings_code == :anything || mappings_code == code)
          env['counter'].incr(counter)
        end
      end
    end
  end
end
