require 'securerandom'
require 'dummy_storage'

class BasicModel

  class NotFound < Exception; end
  class InvalidState < Exception; end

  class << self
    def storage(storage)
      new.storage(storage)
    end

    def use_guuid!; @use_guuid = true; end
    def guuid?; !!@use_guuid; end
  end

  def initialize
    @hash = {}
    @storage = DummyStorage.new
    set_defaults!
    super
  end

  def []=(key, value)
    @hash[key] = value
  end

  def [](key)
    @hash[key]
  end


  def merge(other)
    self.dup.merge!(other)
    self
  end

  def merge!(other)
    @hash.merge!(other)
    self
  end

  def id; raise NotImplementedError; end
  def validate!; self; end
  def set_defaults!; self; end


  def storage(storage)
    @storage = storage.namespace(self.class.name)
    self
  end

  def save(skip_validation = false)
    id = if self.class.guuid?
           self['id'] ||= SecureRandom.uuid
         else
           self['id'] = self.id
         end
    validate! unless skip_validation
    @storage.set(id, {}.merge(self).to_json)
    @storage.del(@old_id) if @old_id && @old_id != id
    self
  end

  def load(id)
    @old_id = id
    response = @storage.get(id)
    raise NotFound unless response
    hash = JSON.parse(response)
    self.merge!(hash)
    self
  end

  def delete
    return unless self['id']
    @storage.del(self['id'])
    self
  end


  def inspect
    "<#{self.class.name}: #{@hash.inspect}>"
  end

  def to_hash
    @hash
  end

  def to_json
    self.to_hash.to_json
  end
end
