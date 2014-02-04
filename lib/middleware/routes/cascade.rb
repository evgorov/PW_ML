require 'rack/cascade'
require 'middleware/routes/users'
require 'middleware/routes/admin'
require 'middleware/routes/store'
require 'middleware/routes/sets'
require 'middleware/routes/puzzle_data'
require 'middleware/routes/configuration_data'
require 'middleware/routes/device_tracking'

module Middleware
  class Cascade
    def initialize(app = nil)
      cascade = [
                 Middleware::Store.new,
                 Middleware::Admin.new,
                 Middleware::Sets.new,
                 Middleware::ConfigurationData.new,
                 Middleware::DeviceTracking.new,
                 Middleware::PuzzleData.new,
                 Middleware::Users.new
                ]
      cascade.unshift(app) if app
      @app = Rack::Cascade.new(cascade)
    end

    def call(env)
      @app.call(env)
    end
  end
end
