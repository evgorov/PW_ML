require 'httparty'
require 'json'

module WallPublisher
  class << self
    def post(access_token, message, attachmments)
      query({
              access_token: access_token,
              message: message,
              attachmments: attachmments
            })
    end

    def post_other(access_token, user_id, message, attachmments)
      query({
              owner_id: user_id,
              access_token: access_token,
              message: message,
              attachmments: attachmments
            })
    end

    private

    def query(params)
      HTTParty.get('https://api.vk.com/method/wall.post',
                   query: params,
                   timeout: 10)
    end
  end
end
