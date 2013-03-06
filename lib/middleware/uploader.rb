module Middleware
  class Uploader

    DEFAULT_OPTIONS = {
      serve_from: '/img/uploads',
      save_to: 'public/img/uploads'
    }

    def initialize(app, options = {})
      @options = DEFAULT_OPTIONS.merge(options)
      @app = app
    end

    def call(env)
      params = Rack::Request.new(env).params
      env['Uploader.uploaded_files'] = {}

      params.select { |_, v|
        v.respond_to?(:has_key?) && v.has_key?(:tempfile)
      }.map { |param_key, file|
        path = create_file_for_upload(file)
        serve_path = [@options[:serve_from], path].join('/')
        env['Uploader.uploaded_files'][param_key] = serve_path
      }

      @app.call(env)
    end

    private

    def create_file_for_upload(uploaded_file)
      content = uploaded_file[:tempfile]
      digest = Digest::SHA1.hexdigest(content.read)
      path = "#{digest}.#{uploaded_file[:type].split('/').last}"
      to = File.join(@options[:save_to], path)
      FileUtils.mv(content.path, to) unless File.exist?(to)
      FileUtils.chmod(0755, to)
      path
    end

  end
end
