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
        response['friends']['data'].map do |h|
          first, last = h.delete('name').split(' ', 2)
          h['first_name'] = first
          h['last_name'] = last
          h['userpic'] = "http://graph.facebook.com/#{h['id']}/picture?width=85&height=85"
          h
        end
      else
        []
      end
    end

    def fetch_vkontakte_friends(access_token)
    response = HTTParty.get('https://api.vk.com/method/friends.get',
                            query: {
                              access_token: access_token,
                              fields: 'uid,first_name,last_name,photo'
                            },
                            timeout: 10)

      if response['response']
        response['response'].map do |h|
          id = h.delete('uid').to_s
          h['id'] = id
          photo = h.delete('photo')
          h['userpic'] = photo
          h
        end
      else
        []
      end
    end

  end
end
