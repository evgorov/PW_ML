require 'digest/sha1'

class Etag
  def initialize(app)
    @app = app
  end

  def call(env)
    if env['REQUEST_METHOD'] == 'GET'
      status, headers, body = @app.call(env)
      return [status, headers, body] unless (status.to_i / 100) == 2
      new_body = ''
      body.each{ |o| new_body += o }
      body.close if body.respond_to?(:close)
      sha1 = Digest::SHA1.hexdigest(new_body)
      [status, headers.merge('ETag' => sha1), [new_body]]
    else
      @app.call(env)
    end
  end
end
