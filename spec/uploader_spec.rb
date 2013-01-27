require 'rack/test'
require 'json'
require 'digest/sha2'
require 'middleware/uploader'

describe Middleware::Uploader do

  include Rack::Test::Methods

  context 'succesfull call of downstream app' do

    def app
      Middleware::Uploader.new(lambda { |env| @env = env; [200, {}, ['OK']] })
    end

    it 'uploads file to server with any route' do
      post '/any_route', :upload => Rack::Test::UploadedFile.new("fixtures/test_userpic.jpg", "image/jpeg")
      @env['Uploader.uploaded_files']['upload'].should_not == nil
      last_response.status.should == 200
    end

    it 'uses only file uploads' do
      post '/any_route', :string_param => '1234'
      last_response.status.should == 200
    end

    it 'read upload url file from any parameter' do
      post '/any_route', :another_param => Rack::Test::UploadedFile.new("fixtures/test_userpic.jpg", "image/jpeg")
      @env['Uploader.uploaded_files']['another_param'].should_not == nil
      last_response.status.should == 200
    end

    it 'should write file to assets dierctory' do
      post '/some_route', :upload => Rack::Test::UploadedFile.new("fixtures/test_userpic.jpg", "image/jpeg")
      File.exist?(File.join('public', @env['Uploader.uploaded_files']['upload'])).should == true
      last_response.status.should == 200
    end
  end
end

