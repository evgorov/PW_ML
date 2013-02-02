require 'friends_fetcher'
require 'rack/test'
require 'vcr'
require 'json'


describe FriendsFetcher do

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

  it 'should fetch friends for facebook' do
    VCR.use_cassette('facebook_friends_fetch') do
      result = subject.fetch_facebook_friends('access_token')
      result.empty?.should == false
      result.first['id'].should_not == nil
    end
  end

  it 'should fetch friends for vkontakte' do
    VCR.use_cassette('vkontakte_friends_fetch') do
      result = subject.fetch_vkontakte_friends('access_token')
      result.empty?.should == false
      result.first['id'].should_not == nil
    end
  end

end
