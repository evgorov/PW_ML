module Middleware::TokenAuthStrategy
  class Middleware

    def initialize(app)
      @app = app
    end

    def call(env)
      app_response = nil
      env['token_auth'] = Proxy.new(env)

      result = catch(:token_auth_strategy) {
        app_response = @app.call(env)
        :ok
      }

      if result == :failure
        unauthorized_reponse
      else
        app_response
      end
    end

    private

    def unauthorized_reponse
      response_message = "Unauthorized"
      [
       401,
       {'Content-Length' => response_message.size},
       [response_message]
      ]
    end
  end
end
