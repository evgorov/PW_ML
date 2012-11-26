require 'user_factory'
require 'webmock/rspec'
require 'json'
require './spec/fixtures'

describe UserFactory do

  include_context 'fixtures'

  context 'facebook' do

    def stub_successful_facebook_request
      stub_request(:get, "https://graph.facebook.com/me")
        .with(query: { 'access_token' => '123' })
        .to_return(headers: { 'Content-Type' => 'application/json' },
                   body: {
                     "id" => "1688976362",
                     "name" => "Sherlock Homes",
                     "first_name" => "Sherlock",
                     "last_name" => "Homes",
                     "email" => "sherock@fakemail.org",
                     "link" => "https =>//www.facebook.com/sherlockhomes",
                     "birthday" => "08/21/1875",
                     "username" => "sherlock",
                     "hometown" => {
                       "id" => "115085015172389",
                       "name" => "London, UK"
                     },
                     "gender" => "male",
                     "timezone" => 4,
                     "locale" => "en_US",
                     "verified" => true,
                     "updated_time" => "2012-10-25T13:12:48+0000"
                   }.to_json)
    end

    def stub_unsuccessful_facebook_request
      stub_request(:get, "https://graph.facebook.com/me")
        .with(query: { 'access_token' => '123' })
        .to_return(status: 400, body: { error: 'sorry' }.to_json)
    end

    it 'requests facebook data' do
      req = stub_successful_facebook_request
      user = stub(:user)
      FacebookUser.stub(new: user)
      FacebookUser.stub(storage: user)
      user.stub(:load).and_raise(User::NotFound)
      user.as_null_object
      UserFactory.find_or_create_facebook_user(stub(:storage).as_null_object, '123')
      req.should have_been_requested
    end

    it 'raises UserFactory::FacebookException on facebook error' do
      FacebookUser.stub(load: nil)
      stub_unsuccessful_facebook_request
      lambda {
        UserFactory.find_or_create_facebook_user(stub(:storage), '123')
      }.should raise_error(UserFactory::FacebookException)
    end

    it '#find_or_create_facebook_user creates user' do
      stub_successful_facebook_request
      storage = stub(:storage)
      user = mock(:user)
      user.stub(:load).and_raise(User::NotFound)
      FacebookUser.should_receive(:new).and_return(user)
      FacebookUser.stub(storage: user)
      user.should_receive(:storage).and_return(user)
      [
       ['access_token', '123'],
       ['facebook_id', '1688976362'],
       ['name', 'Sherlock'],
       ['surname', 'Homes'],
       ['email', 'sherock@fakemail.org'],
       ['birthdate', '1875-08-21'],
       ['city', 'London, UK'],
       ['userpic', 'http://graph.facebook.com/1688976362/picture']
      ].each { |k, v| user.should_receive(:[]=).with(k, v) }
      user.should_receive(:save)
      u = UserFactory.find_or_create_facebook_user(storage, '123')
      u.should == user
    end

    it 'returns user data on relogin' do
      req = stub_successful_facebook_request
      storage = stub(:storage)
      user = mock(:user)
      FacebookUser.stub(storage: user)
      user.stub(load: user)
      user.should_receive(:[]=).with('access_token', '123')
      user.should_receive(:save)
      u = UserFactory.find_or_create_facebook_user(storage, '123')
      u.should == user
      req.should have_been_requested
    end
  end

  it 'creates user model' do
    storage = stub(:storage)
    user = mock(:user)
    user.should_receive(:merge_fields_user_can_change!)
    user.should_receive(:storage).and_return(user)
    user.should_receive(:save)
    user.should_receive(:load).and_raise(User::NotFound)
    RegisteredUser.should_receive(:new).and_return(user)
    RegisteredUser.should_receive(:storage).and_return(user)
    UserFactory.create_user(storage, valid_user_data)
  end

  it 'already registred' do
    storage = stub(:storage)
    storage.stub(namespace: storage)
    user = mock(:user)
    user.should_receive(:load).and_return(user)
    RegisteredUser.should_receive(:storage).and_return(user)
    lambda {
      UserFactory.create_user(storage, valid_user_data)
    }.should raise_error(UserFactory::AlreadyRegistred)
  end
end
