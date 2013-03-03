require 'storage'
require 'json'

class ServiceMessage

  def self.storage(storage)
    self.new(storage)
  end

  def initialize(storage)
    @storage = storage.namespace('ServiceMessage')
  end

  def messages=(messages)
    @storage.set('message', messages.to_json)
  end

  def messages
    if m = @storage.get('message')
      JSON.parse(m)
    else
      {}
    end
  end
end

