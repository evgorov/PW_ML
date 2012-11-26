require 'middleware/redis_middleware'
require 'rack/test'

describe Middleware::RedisMiddleware do

  it 'sets env["redis"]' do
    redis = stub(:redis)
    options = stub(:options)
    app = lambda do |env|
      env['redis'].should == redis
      [200, {}, ['ok']]
    end
    Redis.should_receive(:new).and_return(redis)
    Middleware::RedisMiddleware.new(app, options).call({})
  end

end
