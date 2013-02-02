require 'httparty'
require 'json'

module FriendsFetcher
  class << self
    def fetch_facebook_friends(access_token)
    response = HTTParty.get('https://graph.facebook.com/me',
                            query: {
                              access_token: access_token,
                              fields: 'friends',
                              limit: 2000
                            },
                            timeout: 10)
      if response['friends'] && response['friends']['data']
        response['friends']['data']
      else
        []
      end
    end

    def fetch_vkontakte_friends(access_token)
    response = HTTParty.get('https://api.vk.com/method/friends.get',
                            query: {
                              access_token: access_token,
                              fields: 'uid,first_name,last_name'
                            },
                            timeout: 10)

      if response['response']
        response['response'].map{ |h| id = h.delete('uid'); h['id'] = id; h }
      else
        []
      end
    end

  end
end
