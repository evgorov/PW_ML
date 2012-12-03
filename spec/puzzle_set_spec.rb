require 'puzzle_set'
require 'fixtures'

describe PuzzleSet do
  include_context 'fixtures'

  it 'has setters and getters for keys' do
    subject['name'] = 'G.'
    subject['name'].should == 'G.'
  end

  it 'should call namespace("PuzzleSet") on #storage' do
    storage = mock(:storage).as_null_object
    storage.should_receive(:namespace).with('PuzzleSet')
    subject.storage(storage)
  end

  it '#save existing set' do
    storage = mock(:storage).as_null_object
    storage.stub(namespace: storage)
    subject.merge!(set_in_storage)
    storage.should_receive(:set).with("1487", {}.merge(subject).to_json)
    subject.storage(storage).save
  end

  it '#loads set' do
    storage = mock(:storage).as_null_object
    storage.stub(namespace: storage)
    storage.should_receive(:get).with("1487").and_return(set_in_storage.to_json)
    set = subject.storage(storage).load("1487")
    set['name'] == set_in_storage['name']
  end

  it 'it is possible to #delete set' do
    storage = mock(:storage).as_null_object
    storage.should_receive(:del).with("1487")
    subject.merge!(set_in_storage)
    subject.storage(storage).delete
  end

  it '#publish' do
    storage = mock(:storage).as_null_object
    subject.merge!(set_in_storage)
    stored_data = {}.merge(subject).merge('published' => true).to_json
    storage.should_receive(:set).with("1487", stored_data)
    storage.should_receive(:sadd).with("PuzzleSets:2012#10", "1487")
    subject.storage(storage).publish
  end

  it '#unpublish' do
    storage = mock(:storage).as_null_object
    subject.merge!(set_in_storage).merge('published' => true)
    stored_data = {}.merge(subject).merge('published' => false).to_json
    storage.should_receive(:set).with("1487", stored_data)
    storage.should_receive(:srem).with("PuzzleSets:2012#10", "1487")
    subject.storage(storage).unpublish
  end

  it '#published_for' do
    storage = mock(:storage).as_null_object
    storage.should_receive(:smembers).with("PuzzleSets:2012#10").and_return(['1', '2', '3'])
    storage.should_receive(:get).with("1").and_return('{}')
    storage.should_receive(:get).with("2").and_return('{}')
    storage.should_receive(:get).with("3").and_return('{}')
    subject.storage(storage).published_for(2012, 10)
  end

end
