require 'model/user'
require 'model/user_data'
require 'model/user_score'
require 'fake_redis_middleware'
require 'digest/sha2'
require 'fixtures'

class ScreamingProxy

  def initialize(o)
    @o = o
  end

  def method_missing(*args, &block)
    puts args.inspect
    ScreamingProxy.new(@o.send(*args, &block))
  end
end

describe UserData do
  include_context 'fake_redis_middleware'
  include_context 'fixtures'

  it '#add_set!' do
    redis = fake_redis_middleware.new(nil)
    ud = UserData.storage(redis)
    ud.merge(user_in_storage)
    ud.save
    puzzle_data = {'id' => '1', 'data' => 'HABA' }
    digest = "set:#{Digest::SHA256.hexdigest(puzzle_data.to_json)}"
    ud.add_set!(puzzle_data)
    JSON.parse(redis.get(ud.id))['sets'].should == ['1']
    JSON.parse(redis.get(ud.id))['sets-versions'].should == { '1' => digest }
    redis.get(digest).should == puzzle_data.to_json
  end

  it '#sets empty' do
    redis = fake_redis_middleware.new(nil)
    ud = UserData.storage(redis)
    ud.merge(user_in_storage)
    ud.save
    ud.sets.should == []
  end

  it '#sets' do
    redis = fake_redis_middleware.new(nil)
    ud = UserData.storage(redis)
    ud.merge(user_in_storage)
    ud.save
    puzzle_data = {'id' => '1', 'data' => 'HABA' }
    ud.add_set!(puzzle_data)
    ud.sets.should == [{'id' => '1', 'data' => 'HABA' }]
  end

  it '#merge!' do

    user_data1_json = user_in_storage
      .merge(
             'id' => '1',
             'sets' => [set_in_storage, set_in_storage.merge('id' => '677')]
             )

    user_data2_json = valid_facebook_user_data_user_as_json
      .merge(
             'id' => '2',
             'sets' => [set_in_storage.merge('id' => '888')]
             )

    storage = mock(:storage)
    storage.stub(namespace: storage)
    storage.stub(zrevrank: 0)
    storage.stub(zrevrank: 0)
    UserScore.stub(storage: UserScore)
    UserScore.stub(scores_for: [])
    storage.should_receive(:get).with('1').and_return(user_data1_json.to_json)
    storage.should_receive(:get).with('2').and_return(user_data2_json.to_json)

    year, month = Time.now.year, Time.now.month
    storage.should_receive(:get).with("1#score##{year}##{month}").and_return(0)
    storage.should_receive(:get).with("2#score##{year}##{month}").and_return(0)
    storage.should_receive(:get).with("1#solved##{year}##{month}").and_return(0)
    storage.should_receive(:get).with("2#solved##{year}##{month}").and_return(0)
    storage.should_receive(:set).with("1#score##{year}##{month}", 0)

    storage.should_receive(:get).with("1#count_fb_shared##{year}##{month}").and_return(0)
    storage.should_receive(:get).with("1#count_vk_shared##{year}##{month}").and_return(0)

   storage.should_receive(:get).with("2#count_fb_shared##{year}##{month}").and_return(0)
    storage.should_receive(:get).with("2#count_vk_shared##{year}##{month}").and_return(0)

    user_data1 = UserData.storage(storage).load('1')
    user_data2 = UserData.storage(storage).load('2')
    user_data1.merge!(user_data2)

    user_data1['sets'].sort_by{ |o| o['id'] }.should == (user_data1_json['sets'] | user_data2_json['sets']).sort_by{ |o| o['id'] }
  end
end
