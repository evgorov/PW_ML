require 'storage'
require 'json'

class Coefficients

  def self.storage(storage)
    self.new(storage)
  end

  def initialize(storage)
    @storage = storage.namespace('Coefficients')
  end

  def coefficients=(messages)
    @storage.set('coefficients', messages.to_json)
  end

  def coefficients
    if m = @storage.get('coefficients')
      JSON.parse(m)
    else
      {}
    end
  end
end

