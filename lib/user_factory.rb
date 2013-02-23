require 'httparty'
require 'json'
require 'model/user'
require 'model/user_data'

module UserFactory

  class FacebookException < Exception; end
  class VkontakteException < Exception; end
  class AlreadyRegistred < Exception; end

  class << self

    def find_or_create_facebook_user(storage, access_token)
      fb_data = facebook_user_info(access_token)
      begin
        user = FacebookUser.storage(storage).load("facebook##{fb_data['id']}")
        user_data = user.user_data
        user_data['facebook_access_token'] = access_token
        user_data.save
        return user
      rescue User::NotFound
      end

      FacebookUser.new.tap do |u|
        user_data = UserData.new
        user_data['facebook_id'] = u['facebook_id'] = fb_data['id']
        user_data['facebook_access_token'] = access_token.to_s
        user_data['name'] = fb_data['first_name']
        user_data['surname'] = fb_data['last_name']
        user_data['email'] = fb_data['email']
        month, day, year = fb_data['birthday'] && fb_data['birthday'].split('/')
        user_data['birthdate'] = [year, month, day].join('-')
        user_data['city'] = fb_data['hometown'] && fb_data['hometown']['name']
        user_data['userpic'] = "http://graph.facebook.com/#{fb_data['id']}/picture?width=85&height=85"
        user_data.storage(storage).save
        u['user_data_id'] = user_data.id
        u.storage(storage).save
      end
    end

    def find_or_create_vkontakte_user(storage, access_token)
      vk_data = vkontakte_user_info(access_token)
      begin
        user = VkontakteUser.storage(storage).load("vkontakte##{vk_data['uid']}")
        user_data = user.user_data
        user_data['vkontakte_access_token'] = access_token.to_s
        user_data.save
        return user
      rescue User::NotFound
      end

      VkontakteUser.new.tap do |u|
        user_data = UserData.new
        user_data['vkontakte_id'] = u['vkontakte_id'] = vk_data['uid']
        user_data['name'] = vk_data['first_name']
        user_data['vkontakte_access_token'] = access_token.to_s
        user_data['surname'] = vk_data['last_name']
        user_data['userpic'] = vk_data['photo_medium']
        user_data.storage(storage).save
        u['user_data_id'] = user_data.id
        u.storage(storage).save
      end
    end

    def create_user(storage, data)
      begin
        raise AlreadyRegistred if RegisteredUser.storage(storage).load(data['email'])
      rescue User::NotFound
      end

      RegisteredUser.new.tap do |u|
        u['email'] = data['email']
        u['password'] = data.delete('password')
        user_data = UserData.new
        user_data.merge_fields_user_can_change!(data)
        user_data.storage(storage).save
        u['user_data_id'] = user_data.id
        u.storage(storage).save
      end
    end

    private

    def vkontakte_user_info(access_token)
      response = HTTParty.get('https://api.vk.com/method/getProfiles.json',
                              query: { 'access_token' => access_token,
                                       'fields' => 'photo_medium,first_name,last_name,city' },
                              timeout: 10)
      raise VkontakteException unless response.code == 200
      response.to_hash['response'].first.tap { |h|
        raise VkontakteException.new('Missing id') unless h['uid']
      }
    end

    def facebook_user_info(access_token)
      response = HTTParty.get('https://graph.facebook.com/me',
                              query: { 'access_token' => access_token },
                              timeout: 10)
      raise FacebookException unless response.code == 200
      response.to_hash.tap { |h|
        raise FacebookException.new('Missing id') unless h['id']
      }
    end
  end
end
