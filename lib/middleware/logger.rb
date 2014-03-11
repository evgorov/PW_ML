class Logger
  def initialize(app)
    @app = app
    @logger = File.open('log/full.log', 'w')
  end

  def call(env)
   @logger.puts env['PATH_INFO']
   @logger.puts env['REQUEST_METHOD']
   @logger.puts env['QUERY_STRING']
   if env['rack.input']
     @logger.puts env['rack.input'].read
     env['rack.input'].rewind
   end
   res = @app.call(env)
   @logger.puts res.inspect
   @logger.puts '---'
   @logger.flush
   res
  end
end
