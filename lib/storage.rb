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
