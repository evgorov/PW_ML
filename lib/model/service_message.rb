require 'storage'

class ServiceMessage

  def self.storage(storage)
    self.new(storage)
  end

  def initialize(storage)
    @storage = storage.namespace('ServiceMessage')
  end

  def message=(message)
    @storage.set('message', message)
  end

  def message
    @storage.get('message') || ""
  end
end

