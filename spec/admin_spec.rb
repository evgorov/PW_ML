require 'model/user'
require 'model/user_data'
require 'middleware/routes/admin'
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
    user = stub(:user).as_null_object
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

  it 'GET /sets' do
    token_auth = mock(:token_auth).as_null_object
    puzzle_set = mock(:puzzle_set).as_null_object
    puzzle_set.should_receive(:all_for).and_return([{ 'k' => 'v' }])
    PuzzleSet.should_receive(:storage).and_return(puzzle_set)
    get('/sets',
         { session_key: 'valid_session_key' },
         { 'token_auth' => token_auth })
    last_response.status.should == 200
  end

  it 'PUT /sets/:id' do
    token_auth = mock(:token_auth).as_null_object
    puzzle_set = mock(:puzzle_set).as_null_object
    puzzle_set.should_receive(:load).with('1234').and_return(puzzle_set)
    puzzle_set.should_receive(:save)
    PuzzleSet.should_receive(:storage).and_return(puzzle_set)

    put('/sets/1234',
        {
          session_key: 'valid_session_key',
          year: '2012',
          month: '12',
          name: 'Test',
          type: 'Silver',
          puzzles: [].to_json
        },
         { 'token_auth' => token_auth })
    last_response.status.should == 200
  end

  it 'DELETE /sets/:id' do
    token_auth = mock(:token_auth).as_null_object
    puzzle_set = mock(:puzzle_set).as_null_object
    puzzle_set.should_receive(:load).with('1234').and_return(puzzle_set)
    puzzle_set.should_receive(:delete)
    PuzzleSet.should_receive(:storage).and_return(puzzle_set)

    delete('/sets/1234',
         { session_key: 'valid_session_key' },
         { 'token_auth' => token_auth })
    last_response.status.should == 200
  end

  it 'GET /users/paginate' do
    token_auth = mock(:token_auth).as_null_object
    user = mock(:user).as_null_object
    user.should_receive(:all).with(2)
    UserData.should_receive(:storage).and_return(user)

    get('/users/paginate',
        { session_key: 'valid_session_key', page: 2 },
        { 'token_auth' => token_auth })
    last_response.status.should == 200
  end
end
