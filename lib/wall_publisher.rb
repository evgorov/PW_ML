require 'httparty'
require 'json'

module WallPublisher
  class << self
    def post(access_token, message, attachmments)
      HTTParty.get('https://api.vk.com/method/wall.post',
                   query: {
                     access_token: access_token,
                     message: message,
                     attachmments: attachmments
                   },
                   timeout: 10)
    end
  end
end
