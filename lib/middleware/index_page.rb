class IndexPage

  def initialize(app)
    @app = app
  end

  def call(env)
    env['PATH_INFO'] = '/index.html' if env['PATH_INFO'] =~ %r{^/[0-9]*$}
    @app.call(env)
  end
end
