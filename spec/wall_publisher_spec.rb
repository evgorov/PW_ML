require 'wall_publisher'
require 'rack/test'
require 'vcr'
require 'json'

describe WallPublisher do

  before(:all) do
    VCR.configure do |c|
      c.allow_http_connections_when_no_cassette = true
      c.cassette_library_dir = 'fixtures/vcr_cassettes'
      c.hook_into :webmock
    end
    VCR.turn_on!
  end

  after(:all) do
    VCR.turn_off!
  end

  it 'should post messages to vkontakte wall' do
    VCR.use_cassette('vkontakte_wall_post') do
      s = subject.post('c1f2927a659696535a2f63658c7eed8ec9712bf1cfdcf5395b08d64e969c6e43c18c447383c39e5903f96', 'test message', 'http://imgs.xkcd.com/comics/tar.png')
      s.code.should == 200
    end
  end
end
