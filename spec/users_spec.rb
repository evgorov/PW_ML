require 'middleware/users'
require 'rack/test'
require 'json'
require './spec/fixtures'

describe Middleware::Users do

  include Rack::Test::Methods
  include_context 'fixtures'

  def app
    Middleware::Users.settings.environment = :test
    Middleware::Users.new
  end

  def last_response_should_be_json
    last_response.content_type.split(';')[0].should == 'application/json'
    lambda { JSON.parse(last_response.body) }.should_not raise_error
  end

  it 'GET /me uses token_auth_strategy to authenticate' do
    token_auth = mock(:token_auth)
    token_auth.stub(:user)
    token_auth.should_receive(:authorize!).and_raise(Exception)
    get('/me',
        { session_key: 'invalid_session_key' },
        { 'token_auth' => token_auth })
  end

  it 'GET /me returns informaiton about users' do
    token_auth = mock(:token_auth).as_null_object
    user = { 'name' => 'John' }
    token_auth.should_receive(:user).and_return(user)
    get('/me',
        { session_key: 'valid_session_key' },
        { 'token_auth' => token_auth })
    last_response_should_be_json
    last_response.status.should == 200
    response_data = JSON.parse(last_response.body)
    response_data['me'].should == user
  end


  it 'POST /me uses token_auth_strategy to authenticate' do
    token_auth = mock
    token_auth.stub(:user)
    token_auth.should_receive(:authorize!).and_raise(Exception)
    post('/me',
        { session_key: 'invalid_session_key' },
        { 'token_auth' => token_auth })
  end


  it 'POST /me updates information about users' do
    user = mock(:user)
    user.stub(to_json: '"USER_AS_JSON"')
    user.should_receive(:merge_fields_user_can_change!)
    user.should_receive(:save)

    token_auth = mock(:token_auth)
    token_auth.stub(user: user)
    token_auth.should_receive(:update_user)
    token_auth.stub(:authorize!)

    post('/me',
         {
           session_key: 'valid_session_key',
           name: 'sir Artur',
           email: 'artur@example.org'
         },
         { 'token_auth' => token_auth })

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['me'].should == 'USER_AS_JSON'
  end

  it 'POST /score updates users score' do
    user = mock(:user)
    user.stub(to_json: '"USER_AS_JSON"')
    user.should_receive(:[]).with('month_score').and_return(10)
    user.should_receive(:[]=).with('month_score', 14)
    user.should_receive(:[]).with('solved').and_return(0)
    user.should_receive(:[]=).with('solved', 0)
    user.should_receive(:save)

    token_auth = stub(:token_auth)
    token_auth.stub(user: user)
    token_auth.stub(:authorize!)

    post('/score',
         {
           session_key: 'valid_session_key',
           score: '4'
         },
         { 'token_auth' => token_auth })

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['me'].should == 'USER_AS_JSON'
  end

  it 'POST /hints updtates hints count' do
    user = mock(:user)
    user.stub(to_json: '"USER_AS_JSON"')
    user.should_receive(:[]).with('hints').and_return(5)
    user.should_receive(:[]=).with('hints', 2)
    user.should_receive(:save)

    token_auth = stub(:token_auth)
    token_auth.stub(user: user)
    token_auth.stub(:authorize!)

    post('/hints',
         {
           session_key: 'valid_session_key',
           hints_change: '-3'
         },
         { 'token_auth' => token_auth })

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['me'].should == 'USER_AS_JSON'
  end

  it 'GET /users returns users currently on top of the rating' do
    User.stub(storage: User)
    User.should_receive(:users_by_rating).with(3).and_return(['USER1',
                                                              'USER2'])
    token_auth = stub(:token_auth).as_null_object
    token_auth.stub(user: 'ME')

    get('/users',
         {
           session_key: 'valid_session_key',
           page: 3
         },
         { 'token_auth' => token_auth })

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data['me'].should == 'ME'
    response_data['users'][0].should == 'USER1'
    response_data['users'][1].should == 'USER2'
  end

  it 'GET /sets_available returns list of sets available for this month' do
    # available sets
    available_sets = [{'id' => '1'}, { 'id' => '2'}]
    PuzzleSet.should_receive(:storage).and_return(PuzzleSet)
    PuzzleSet.should_receive(:published_for).with(2011, 11).and_return(available_sets)
    # users sets
    user_sets = [{'id' => '1'}]
    user = stub(:user).as_null_object
    user.stub(id: 'USER_ID')
    user.stub(:[]).with('sets').and_return(user_sets)
    # stub authorization
    token_auth = stub(:token_auth).as_null_object
    token_auth.stub(user: user)

    get('/sets_available',
        {
          month: 11,
          year: 2011,
          session_key: 'valid_session_key'
        },
        { 'token_auth' => token_auth })

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data.should == [{'id' => '1', 'bought' => true },
                             { 'id' => '2', 'bought' => false }]
  end

  it 'GET /sets/:id/buy adds set to user storage' do
    token_auth = stub(:token_auth).as_null_object

    user = stub(:user)
    user.as_null_object
    user.stub(id: 'USER_ID')
    user.stub(:[]).with('sets').and_return([])
    user.should_receive(:save)

    puzzle_set = stub(:puzzle_set).as_null_object
    puzzle_set.should_receive(:load)
    puzzle_set.stub(to_json: "{}")
    PuzzleSet.should_receive(:storage).and_return(puzzle_set)


    token_auth.stub(user: user)

    post('/sets/1234/buy',
         { session_key: 'valid_session_key' },
         { 'token_auth' => token_auth })

    last_response.status.should == 200
    last_response_should_be_json

  end

  it 'GET /puzzles/:id return puzzle information for user' do
    token_auth = mock
    token_auth.stub(:authorize!)
    user = stub(:user)
    token_auth.stub(user: user)
    res = { 'valid_json' => 'object' }
    user.should_receive(:[]).with('puzzle-data.1234').and_return(res)

    get('/puzzles/1234',
        { session_key: 'valid_session_key' },
        { 'token_auth' => token_auth })
    last_response_should_be_json
    last_response.status.should == 200
    response_data = JSON.parse(last_response.body)
    response_data.should == res
  end

  it 'PUT /puzzles/:id saves puzzle information for user' do
    token_auth = mock
    token_auth.stub(:authorize!)
    user = stub(:user)
    token_auth.stub(user: user)
    data = { 'valid_json' => 'object' }
    user.should_receive(:[]=).with('puzzle-data.1234', data.to_json)
    user.should_receive(:save)

    put('/puzzles/1234',
        {
          session_key: 'valid_session_key',
          puzzle_data: data.to_json
        },
        { 'token_auth' => token_auth })

    last_response_should_be_json
    last_response.status.should == 200
  end

  it 'PUT /puzzles/:id returns 403 if not valid json object is given' do
    put('/puzzles/1234',
        {
          session_key: 'valid_session_key',
          puzzle_data: 'INVALID_JSON_STRING'
        })

    last_response_should_be_json
    last_response.status.should == 403
  end

  it 'GET /puzzles returns puzzles for current user' do
    token_auth = stub(:token_auth).as_null_object
    puzzle_sets = [{'key' => 'value'}]
    user = stub(:user).as_null_object
    user.stub(id: 'USER_ID')
    user.should_receive(:[]).with('sets').and_return(puzzle_sets)
    token_auth.stub(user: user)

    get('/puzzles',
        {
          session_key: 'valid_session_key',
        },
        { 'token_auth' => token_auth })

    last_response.status.should == 200
    last_response_should_be_json
    response_data = JSON.parse(last_response.body)
    response_data.should == { 'sets' => puzzle_sets }
  end
end
