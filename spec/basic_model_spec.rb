require 'basic_model'

describe BasicModel do

  before(:all) do
    class BasicModelTest < BasicModel
      def id; self['key']; end
    end
  end

  after(:all) { Object.send(:remove_const, :BasicModelTest) }

  subject { BasicModelTest.new }

  let(:data) {
    {
      'key' => 'key_value',
      'test_field1' => 'test_value1',
      'test_field2' => 'test_value2'
    }
  }

  let(:data_lsit) {
    [
     { 'key' => 'key1'},
     { 'key' => 'key2'},
     { 'key' => 'key3'}
    ]
  }

  it 'has setters and getters for keys' do
    subject['name'] = 'G.'
    subject['name'].should == 'G.'
  end

  it 'should call namespace("BasicModelTest") on #storage' do
    storage = mock(:storage)
    storage.should_receive(:namespace).with('BasicModelTest')
    subject.storage(storage)
  end

  it '#save' do
    storage = mock(:storage).as_null_object
    storage.should_receive(:set).with(data['key'], data.merge('id' => data['key']).to_json)
    subject.storage(storage).merge(data).save
  end

  it '#save when key was changed' do
    storage = mock(:storage).as_null_object
    storage.should_receive(:get).with(data['key']).and_return(data.merge('id' => data['key']).to_json)
    storage.should_receive(:set).with('new_key', data.merge('id' => 'new_key', 'key' => 'new_key').to_json)
    storage.should_receive(:del).with(data['key'])
    u = subject.storage(storage).load(data['key'])
    u['key'] = 'new_key'
    u.storage(storage).save
  end

  it 'raises exception when trying to #save without storage' do
    subject.merge!(data)
    lambda { subject.save }.should raise_error(StorageNotSet)
  end

  it '#save stores list of objects to zset' do
    storage = mock(:storage).as_null_object
    storage.should_receive(:zadd).with('all', kind_of(Numeric), data['key'])
    subject.storage(storage).merge(data).save
  end

  it '#all returns all instances' do
    storage = mock(:storage)
    storage.stub(namespace: storage)
    keys = data_lsit.map{ |o| o['key'] }

    storage.should_receive(:zrevrange).with('all', 20, 30).and_return(keys)
    storage.should_receive(:mget).with(*keys).and_return(data_lsit.map(&:to_json))

    subject.class.storage(storage).all(3).size.should == 3
  end
end
