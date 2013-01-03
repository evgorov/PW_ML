require 'httparty'
require 'json'
require 'user'

module UserFactory

  class FacebookException < Exception; end
  class AlreadyRegistred < Exception; end

  class << self

    def find_or_create_facebook_user(storage, access_token)
      fb_data = facebook_user_info(access_token)
      begin
        user = FacebookUser.storage(storage).load(fb_data['id'])
        user['access_token'] = access_token
        user.save
        return user
      rescue User::NotFound
      end

      FacebookUser.new.tap do |u|
        u['access_token'] = access_token.to_s
        u['facebook_id'] = fb_data['id']
        u['name'] = fb_data['first_name']
        u['surname'] = fb_data['last_name']
        u['email'] = fb_data['email']
        month, day, year = fb_data['birthday'] && fb_data['birthday'].split('/')
        u['birthdate'] = [year, month, day].join('-')
        u['city'] = fb_data['hometown']['name']
        u['userpic'] = "http://graph.facebook.com/#{fb_data['id']}/picture"
        u.storage(storage).save
      end
    end

    def create_user(storage, user_data)
      begin
        raise AlreadyRegistred if RegisteredUser.storage(storage).load(user_data['email'])
      rescue User::NotFound
      end

      RegisteredUser.new.tap do |u|
        u.merge_fields_user_can_change!(user_data)
        u.storage(storage).save
      end
    end

    private

    def facebook_user_info(access_token)
      response = HTTParty.get('https://graph.facebook.com/me',
                              query: { 'access_token' => access_token },
                              timeout: 1)
      raise FacebookException unless response.code == 200
      response.to_hash.tap { |h|
        raise FacebookException.new('Missing id') unless h['id']
      }
    end
  end
end