require 'user'
require 'fixtures'

# Lets add some speed!
BCrypt::Engine::DEFAULT_COST = 1

describe User do

  include_context 'fixtures'

  context 'common' do

    subject { RegisteredUser.new }

    it 'has setters and getters for keys' do
      subject['name'] = 'G.'
      subject['name'].should == 'G.'
    end

    it 'should call namespace("User") on #storage' do
      storage = mock(:storage)
      storage.should_receive(:namespace).with('User')
      subject.storage(storage)
    end

    it '#save' do
      storage = mock(:storage).as_null_object
      subject.merge!(user_in_storage)
      storage.should_receive(:set).with(user_in_storage_key, {}.merge(subject).to_json)
      storage.should_receive(:get).with(user_in_storage_key).and_return(nil)
      subject.storage(storage).save
    end

    it '#save updates rating' do
      storage = mock(:storage).as_null_object

      subject.merge!(user_in_storage)
      subject['month_score'] += 100
      storage.should_receive(:set)
      storage.should_receive(:zadd).with('rating', 100, 'registered#g@interpol.co.uk')
      storage.should_receive(:get).and_return(nil)
      subject.storage(storage).save
    end

    it '#save when key was changed' do
      subject.merge!(user_in_storage)

      storage = mock(:storage).as_null_object
      storage.should_receive(:get).with(user_in_storage_key).and_return(user_in_storage.to_json)
      storage.should_receive(:del).with(user_in_storage_key)
      expected_data =
      storage.should_receive(:set).with do |key, data|
        key.should == 'registered#g@fbi.gov'
        JSON.parse(data).should == {}.merge(subject).merge('email' => 'g@fbi.gov', 'id' => 'registered#g@fbi.gov')
      end
      storage.should_receive(:get).and_return(nil)

      u = RegisteredUser.storage(storage).load_by_key(user_in_storage['email'])
      u['email'] = 'g@fbi.gov'
      u.storage(storage).save
    end

    it 'raises exception when trying to #save without storage' do
      subject.merge!(data)
      lambda { subject.save }.should raise_error(StorageNotSet)
    end

    it 'uses bcrypt for password fied' do
      subject['password']= 'love'
      subject['password'].should_not be_nil
      subject['password'].should_not == ''
      subject['password'].should == 'love'
      subject['password'].to_s.should_not == 'love'
      {}.merge(subject)['password'].should == nil
      {}.merge(subject)['password_hash'].should_not == nil
    end

    it '#merge should not save password as field' do
      subject.merge!(data)
      {}.merge(subject)['password'].should == nil
      {}.merge(subject)['password_hash'].should_not == nil
    end

    it 'raises exception when trying to save invalid data' do
      storage = stub.as_null_object
      invalid_data = data.dup
      invalid_data.delete('name')
      lambda { subject.merge(invalid_data).storage(storage).save }.should raise_error(User::InvalidState)
    end

    it 'not validating User when #save(false)' do
      storage = stub.as_null_object
      storage.stub(get: user_in_storage.to_json)
      invalid_data = data.dup
      invalid_data.delete('name')
      lambda { subject.merge(invalid_data).storage(storage).save(true) }.should_not raise_error
    end

    it '#to_json' do
      data_should_can_see = data.dup.tap { |o| o.delete('password') }
      json = subject.merge(data).to_json
      JSON.parse(json).should == data_should_can_see
    end

    it '#merge_fields_user_can_change!' do
      invalid_data = data.dup.tap do |h|
        h['password_hash'] = 'mailformed_hash'
        h['score'] = 100
        h['another_field'] = 'some_data'
      end

      subject.merge_fields_user_can_change!(invalid_data)
      subject['password_hash'].should_not == 'mailformed_hash'
      subject['rating'].should_not == 100
      subject['another_field'].should == nil
    end

    it '.load_by_provider_and_key' do
      storage = mock(:storage).as_null_object
      storage.should_receive(:get).with(user_in_storage_key).and_return(user_in_storage.to_json)
      u = User.load_by_provider_and_key(storage, 'registered', user_in_storage['email'])
      u.is_a?(User).should be_true
      u.to_hash.should == user_in_storage
    end

    it '#load' do
      storage = mock(:storage).as_null_object
      storage.should_receive(:get).with(user_in_storage_key).and_return(user_in_storage.to_json)
      u = RegisteredUser.storage(storage).load("registered##{user_in_storage['email']}")
      u.is_a?(User).should be_true
      u.to_hash.should == user_in_storage
    end

    it '#load not found' do
      storage = mock(:storage).as_null_object
      storage.should_receive(:get).with(user_in_storage_key).and_return(nil)
      lambda { RegisteredUser.storage(storage).load_by_key(user_in_storage['email']) }.should raise_error(User::NotFound)
    end

    it '#users_by_rating' do
      storage = stub(:storage).as_null_object

      storage.
        should_receive(:zrevrange).
        with('rating', 0, 10).
        and_return(['registered#g@interpol.co.uk', 'facebook#1234'])
      storage.should_receive(:mget).
        with(user_in_storage_key, user2_in_storage_key).
        and_return([user_in_storage.to_json, user2_in_storage.to_json])

      users = subject.storage(storage).users_by_rating
      users[0].to_hash.should == user_in_storage
      users[1].to_hash.should == user2_in_storage
    end
  end

  context 'registered' do

    subject { RegisteredUser.new }

    it '#id raises exception when email is not set' do
      lambda { subject.id }.should raise_error(User::InvalidState)
    end

    it '#id' do
      valid_id = 'registered#g@interpol.co.uk'
      subject.merge!(data)
      subject.id.should == valid_id
    end

    it '#authenticate' do
      storage = mock(:storage).as_null_object
      storage.should_receive(:get).with(user_in_storage_key).exactly(2).and_return(user_in_storage.to_json)
      u = RegisteredUser.storage(storage).authenticate(user_in_storage['email'], user_in_storage_password)
      u.to_hash.should == user_in_storage
      lambda {
        RegisteredUser.storage(storage).authenticate(user_in_storage['email'], 'wrong_password')
      }.should raise_error(User::NotFound)
    end

    it '#validate! for registered provider' do
      invalid_data = data.dup
      invalid_data.delete('email')
      lambda { subject.merge(invalid_data).validate! }.should raise_error(User::InvalidState)
      lambda { subject.merge(data).validate! }.should_not raise_error(User::InvalidState)
    end

    it '#save cannot create user when email is taken' do
      storage = mock(:storage).as_null_object
      subject.merge!(user_in_storage)
      storage.should_not_receive(:set).with(user_in_storage_key, {}.merge(subject).to_json)
      storage.should_receive(:get).with(user_in_storage_key).and_return(user_in_storage.to_json)
      lambda { subject.storage(storage).save }.should raise_error(User::InvalidState)
    end


    it '#save cannot change email to already registered email adress' do
      subject.merge!(user_in_storage)
      storage = mock(:storage).as_null_object
      storage.should_receive(:get).with(user_in_storage_key).and_return(user_in_storage.to_json)
      storage.should_receive(:get).with('registered#already_taken@fbi.gov').and_return(user_in_storage.to_json)
      storage.should_not_receive(:set).with('registered#already_taken@fbi.gov', anything)

      u = RegisteredUser.storage(storage).load_by_key(user_in_storage['email'])
      u['email'] = 'already_taken@fbi.gov'
      lambda { u.storage(storage).save }.should raise_error(User::InvalidState)
    end


  end

  context 'facebook' do

    subject { FacebookUser.new }

    it '#validate! for facebook provider' do
      invalid_data = facebook_data.dup
      invalid_data.delete('name')
      lambda { subject.merge(invalid_data).validate! }.should raise_error(User::InvalidState)
      lambda { subject.merge(facebook_data).validate! }.should_not raise_error(User::InvalidState)
    end

  end

  context 'vkontakte' do

    subject { VkontakteUser.new }

    it '#validate! for vkontakte provider' do
      invalid_data = vkontakte_data.dup
      invalid_data.delete('name')
      lambda { subject.merge(invalid_data).validate! }.should raise_error(User::InvalidState)
      lambda { subject.merge(vkontakte_data).validate! }.should_not raise_error(User::InvalidState)
    end

  end
end

