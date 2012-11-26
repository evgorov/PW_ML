module Middleware
  module TokenAuthStrategy

    autoload :Middleware, 'middleware/token_auth_strategy/middleware'
    autoload :Proxy, 'middleware/token_auth_strategy/proxy'

    def self.new(*a); Middleware.new(*a); end

    class << self
      attr_accessor :serialize_user_proc, :deserialize_user_proc
    end

  end
end
