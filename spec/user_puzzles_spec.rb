require 'fixtures'
require 'model/user_puzzles'

describe UserPuzzles do
  include_context 'fixtures'

  it 'has setters and getters for keys' do
    subject['name'] = 'G.'
    subject['name'].should == 'G.'
  end

  it 'should call namespace("UserPuzzles") on #storage' do
    storage = mock(:storage).as_null_object
    storage.should_receive(:namespace).with('UserPuzzles')
    subject.storage(storage)
  end

  it '#save existing set' do
    storage = mock(:storage).as_null_object
    storage.stub(namespace: storage)
    subject.merge!(user_puzzles_in_storage)
    storage.should_receive(:set).with("2012#10#registered#john@example.com", {}.merge(subject).to_json)
    subject.storage(storage).save
  end

  it '#save creates valid id' do
    valid_id = user_puzzles_in_storage.delete('id')
    storage = mock(:storage).as_null_object
    storage.stub(namespace: storage)
    subject.merge!(user_puzzles_in_storage)
    storage.should_receive(:set).with(valid_id, {}.merge(subject).merge('id' => valid_id).to_json)
    subject.storage(storage).save
    subject['id'].should == valid_id
  end

  it '#loads set' do
    storage = mock(:storage).as_null_object
    storage.stub(namespace: storage)
    storage.should_receive(:get).with("2012#10#registered#john@example.com").and_return(user_puzzles_in_storage.to_json)
    set = subject.storage(storage).load("2012#10#registered#john@example.com")
    set['name'] == user_puzzles_in_storage['score']
  end

  it 'it is possible to #delete set' do
    storage = mock(:storage).as_null_object
    storage.should_receive(:del).with("2012#10#registered#john@example.com")
    subject.merge!(user_puzzles_in_storage)
    subject.storage(storage).delete
  end

  it '#puzzles_for' do
    storage = mock(:storage).as_null_object
    storage.should_receive(:get).with('2012#10#USER_ID').and_return(user_puzzles_in_storage.to_json)
    UserPuzzles.storage(storage).puzzles_for('USER_ID', 2012, 10)
  end

  it '#puzzles_for creates new UserPuzzles when existing not found' do
    storage = mock(:storage).as_null_object
    storage.should_receive(:get).with('2012#10#USER_ID').and_return(nil)
    storage.should_receive(:set).with('2012#10#USER_ID', anything).and_return(user_puzzles_in_storage.to_json)
    UserPuzzles.storage(storage).puzzles_for('USER_ID', 2012, 10)
  end

  it '#add_set' do
    storage = mock(:storage).as_null_object
    storage.should_receive(:get).with('2012#10#USER_ID').and_return(user_puzzles_in_storage.to_json)
    puzzle_set = mock(:puzzles_set).as_null_object
    puzzle_set.stub(:[]).with('month').and_return('10')
    puzzle_set.stub(:[]).with('year').and_return('2012')
    puzzle_set.should_receive(:to_hash).and_return(set_in_storage)
    PuzzleSet.should_receive(:storage).with(storage).and_return(puzzle_set)
    user_puzzles = UserPuzzles.storage(storage).puzzles_for('USER_ID', 2012, 10)
    user_puzzles.add_set('1487')
    user_puzzles['sets'][0].should == set_in_storage
  end

  it '#add_set raises InvalidState when added puzzle with not matching date and month' do
    storage = mock(:storage).as_null_object
    storage.should_receive(:get).
      with('2012#12#USER_ID').
      and_return(user_puzzles_in_storage.merge('month' => 12).to_json)
    puzzle_set = mock(:puzzles_set).as_null_object
    puzzle_set.stub(:[]).with('month').and_return('10')
    puzzle_set.stub(:[]).with('year').and_return('2012')
    PuzzleSet.should_receive(:storage).with(storage).and_return(puzzle_set)
    user_puzzles = UserPuzzles.storage(storage).puzzles_for('USER_ID', 2012, 12)
    lambda { user_puzzles.add_set('1487') }.should raise_error(PuzzleSet::InvalidState)
  end

  it '#add_set does nothing when set given id already has been added' do
    storage = mock(:storage).as_null_object
    storage.should_receive(:get).with('2012#10#USER_ID').and_return(user_puzzles_in_storage.to_json)
    puzzle_set = stub(:puzzles_set).as_null_object
    puzzle_set.stub(:[]).with('month').and_return('10')
    puzzle_set.stub(:[]).with('year').and_return('2012')
    puzzle_set.stub(to_hash: set_in_storage)
    PuzzleSet.stub(storage: puzzle_set)
    user_puzzles = UserPuzzles.storage(storage).puzzles_for('USER_ID', 2012, 10)
    user_puzzles.add_set('1487')
    user_puzzles.add_set('1487')
    user_puzzles['sets'].size.should == 1
  end
end
