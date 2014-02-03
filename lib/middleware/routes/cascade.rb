require 'rack/cascade'
require 'middleware/routes/users'
require 'middleware/routes/admin'
require 'middleware/routes/store'
require 'middleware/routes/sets'

module Middleware
  class Cascade
    def initialize(app = nil)
      cascade = [
                 Middleware::Users.new,
                 Middleware::Store.new,
                 Middleware::Admin.new,
                 Middleware::Sets.new
                ]
      cascade.unshift(app) if app
      @app = Rack::Cascade.new(cascade)
    end

    def call(env)
      @app.call(env)
    end
  end
end
