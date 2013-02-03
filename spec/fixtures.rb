shared_context 'fixtures' do
  let(:valid_user_data) {
    {
      'email' => 'sherlock@example.org',
      'name' => 'Sherlock',
      'surname' => 'Holmes',
      'password' => 'jsdkfjdsl 32ds!!',
      'birthdate' => '1873-11-12',
      'userpic' => 'image data',
      'city' => 'London'
    }
  }

  let(:valid_user_data_user_as_json) {
    {
      'id' => 'registered#sherlock@example.org',
      'provider' => 'registered',
      'email' => 'sherlock@example.org',
      'name' => 'Sherlock',
      'surname' => 'Holmes',
      'position' => 1,
      'solved' => 0,
      'month_score' => 0,
      'high_score' => 0,
      'dynamics' => 0,
      'hints' => 0,
      'birthdate' => '1873-11-12',
      'userpic' => 'image data',
      'city' => 'London'
    }
  }

  let(:valid_facebook_user_data_user_as_json) {
    {
      'id' => 'facebook#123456789',
      'provider' => 'facebook',
      'email' => 'sherlock@example.org',
      'name' => 'Sherlock',
      'surname' => 'Holmes',
      'position' => 1,
      'solved' => 0,
      'month_score' => 0,
      'high_score' => 0,
      'dynamics' => 0,
      'hints' => 0,
      'birthdate' => '1873-11-12',
      'userpic' => 'http://graph.facebook.com/123456789/picture',
      'city' => 'London, UK'
    }
  }

  let(:user2_in_storage_key) { 'facebook#1234' }
  let(:user2_in_storage) {
    {
      'provider' => 'facebook',
      'facebook_id' => '1234',
      'role' => 'user',
      'position' => 1,
      'solved' => 2,
      'month_score' => 0,
      'high_score' => 1100,
      'dynamics' => 1,
      'hints' => 3,
      'name' => 'Sherlock',
      'surname' => 'Holmes',
      'email' => 'holmes@example.org'
    }
  }


  let(:user_in_storage_key) { 'registered#g@interpol.co.uk' }
  let(:user_in_storage_password) { '1234' }
  let(:user_in_storage) {
    {
      'id' => 'registered#g@interpol.co.uk',
      'provider' => 'registered',
      'position' => 1,
      'role' => 'user',
      'solved' => 0,
      'month_score' => 0,
      'high_score' => 0,
      'password_hash' => '$2a$04$Tb8kcdU0ZAEuzrEMQGVqQOAGh1kZ1neGAncRyNUfoossmITcD/DSe',
      'created_at' => '2012-12-29 10:42:21 +0400',
      'dynamics' => 1,
      'hints' => 0,
      'name' => 'G.',
      'surname' => 'Lestrade',
      'email' => 'g@interpol.co.uk'
    }
  }

  let(:data) {
    {
      'password' => '1234',
      'name' => 'G.',
      'surname' => 'Lestrade',
      'provider' => 'registered',
      'email' => 'g@interpol.co.uk',
      'position' => 100,
      'solved' => 1,
      'month_score' => 5,
      'high_score' => 5,
      'dynamics' => -1,
      'hints' => 10
    }
  }

  let(:facebook_data) {
    {
      'access_token' => '1234',
      'facebook_id' => '1',
      'name' => 'G.',
      'surname' => 'Lestrade',
      'provider' => 'facebook',
      'email' => 'g@interpol.co.uk'
    }
  }

  let(:vkontakte_data) {
    {
      'access_token' => '1234',
      'vkontakte_id' => '1',
      'name' => 'G.',
      'surname' => 'Lestrade',
      'provider' => 'vkontakte',
      'email' => 'g@interpol.co.uk'
    }
  }

  let(:valid_set_data) {
    {
      'year' => 2012,
      'month' => 10,
      'name' => 'Cool set',
      'type' => 'golden'
    }
  }

  let(:user_puzzles_in_storage) {
    {
      'id' => '2012#10#registered#john@example.com',
      'score' => 32,
      'sets' => [],
      'year' => '2012',
      'month' => '10',
      'created_at' => '2012-12-29 10:42:21 +0400',
      'user_id' => 'registered#john@example.com'
    }
  }


  let(:set_in_storage) {
    {
      'year' => 2012,
      'month' => 10,
      'name' => 'Cool set',
      'id' => '1487',
      'puzzles' => [],
      'published' => false,
      'type' => 'golden',
      'created_at' => '2012-12-29 10:42:21 +0400'
    }
  }


end
