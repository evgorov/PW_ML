require 'middleware/admin'
require 'rack/test'
require 'json'
require './spec/fixtures'

describe Middleware::Admin do

  include Rack::Test::Methods
  include_context 'fixtures'

  def app
    Middleware::Admin.settings.environment = :test
    Middleware::Admin.new
  end

  def last_response_should_be_json
    last_response.content_type.split(';')[0].should == 'application/json'
    lambda { JSON.parse(last_response.body) }.should_not raise_error
  end

  it 'only admin can access administration endpoints' do
    token_auth = mock(:token_auth).as_null_object
    PuzzleSet.stub(new: stub.as_null_object)
    user = stub(:user)
    user.stub(:'[]' => 'user')
    token_auth.should_receive(:user).and_return(user)
    token_auth.should_receive(:unauthorized!)
    post('/sets',
         {
           year: 2012,
           month: 10,
           name: 'Cool set',
           puzzles: [].to_json,
           type: 'golden',
           session_key: 'valid_user_key'
         },
         {
           'token_auth' => token_auth,
           'redis' => mock(:redis).as_null_object
         })
  end

  it 'POST /sets' do
    token_auth = mock(:token_auth).as_null_object
    user = stub(:user)
    token_auth.should_receive(:authorize!).and_return(user)
    puzzle_set = stub(:puzzle_set).as_null_object
    puzzle_set.should_receive(:save)
    PuzzleSet.should_receive(:new).and_return(puzzle_set)
    post('/sets',
         {
           year: 2012,
           month: 10,
           name: 'Cool set',
           puzzles: [].to_json,
           type: 'golden',
           session_key: 'valid_session_key'
         },
         { 'token_auth' => token_auth })
    last_response.status.should == 200
  end

  it 'POST /sets/:id/publish' do
    token_auth = mock(:token_auth).as_null_object
    puzzle_set = mock(:puzzle_set).as_null_object
    puzzle_set.should_receive(:storage).and_return(puzzle_set)
    puzzle_set.should_receive(:load).with('1234').and_return(puzzle_set)
    puzzle_set.should_receive(:publish)
    PuzzleSet.should_receive(:new).and_return(puzzle_set)
    post('/sets/1234/publish',
         { session_key: 'valid_session_key' },
         { 'token_auth' => token_auth })
    last_response.status.should == 200
  end

  it 'POST /sets/:id/unpublish' do
    token_auth = mock(:token_auth).as_null_object
    puzzle_set = mock(:puzzle_set).as_null_object
    puzzle_set.should_receive(:storage).and_return(puzzle_set)
    puzzle_set.should_receive(:load).with('1234').and_return(puzzle_set)
    puzzle_set.should_receive(:unpublish)
    PuzzleSet.should_receive(:new).and_return(puzzle_set)
    post('/sets/1234/unpublish',
         { session_key: 'valid_session_key' },
         { 'token_auth' => token_auth })
    last_response.status.should == 200
  end
end
