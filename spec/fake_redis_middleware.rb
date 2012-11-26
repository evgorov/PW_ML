shared_context "fake_redis_middleware" do
  let(:fake_redis_middleware) do
    Module.new do
      class << self

        def new(app);
          @hash = {}
          @app = app;
          self
        end

        def reset!
          @hash = {}
        end

        def call(env)
          env['redis'] = self
          @app.call(env)
        end

        def namespace(prefix); self; end
        def set(k, v); @hash[k] = v; end
        def get(k); @hash[k]; end
        def del(k); @hash.delete(k); end

        def zadd(key, score, member)
          @zset ||= {}
          @zset[member] = score
        end

        def zrevrange(key, start=0, stop=-1)
          @zset.sort_by{ |k, v| v }.reverse.map{ |k,v| k}[start..stop]
        end

        def zrevrank(key, member)
          zrevrange(key).index(member)
        end

        def inspect
          "<FakeRedisMiddleware #{@hash && @hash.inspect}>"
        end

      end
    end
  end
end
