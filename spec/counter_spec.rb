require 'middleware/counter'
require './spec/fake_redis_middleware'
require 'rack/test'

describe Middleware::Counter do

  subject { Middleware::Counter }

  include Rack::Test::Methods
  include_context 'fake_redis_middleware'

  context 'incrementing keys' do

    def app
      app = lambda do |env|
        env['counter'].incr('test_counter')
        [200, {}, ["ok"]]
      end
      fake_redis_middleware.new(subject.new(app))
    end

    it 'uses redis to increment counters' do
      Time.stub_chain(:now, :strftime).and_return('2012-10-11')
      fake_redis_middleware.should_receive(:incr).with('2012-10-11:test_counter')
      get '/'
      last_response.status.should == 200
      last_response.body.should == "ok"
    end
  end

  context 'fetching values' do

    def app
      app = lambda do |env|
        [200, {}, [env['counter'].get('2012-10-11:test_counter')]]
      end
      fake_redis_middleware.new(subject.new(app))
    end

    it 'uses redis to increment counters' do
      Time.stub_chain(:now, :strftime).and_return('2012-10-11')
      fake_redis_middleware.should_receive(:mget).with('2012-10-11:test_counter').and_return([123])
      get '/'
      last_response.status.should == 200
      last_response.body.should == "123"
    end
  end

  context 'counter map' do

    def app
      app = lambda { |env| [200, {}, ['ok']] }
      counter_mappings = {
        [:anything, %r{/url}] => 'counter1',
        [200, %r{\A/url2\Z}] => 'counter2',
        [302, %r{\A/url3\Z}] => 'counter3'
      }
      fake_redis_middleware.new(subject.new(app, counter_mappings: counter_mappings))
    end

    it 'uses redis to increment counters' do
      Time.stub_chain(:now, :strftime).and_return('2012-10-11')
      fake_redis_middleware.should_receive(:incr).exactly(3).with('2012-10-11:counter1')
      fake_redis_middleware.should_receive(:incr).with('2012-10-11:counter2')
      get '/url1'
      last_response.status.should == 200
      get '/url2'
      last_response.status.should == 200
      get '/url3'
      last_response.status.should == 200
      get '/some_other_url'
      last_response.status.should == 200
    end
  end
end
