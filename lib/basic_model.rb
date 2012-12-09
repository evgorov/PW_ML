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

  def initialize(hash = {}, storage = DummyStorage.new)
    @hash, @storage = {}, storage
    set_defaults!
    self.merge!(hash)
    self.old_id = self.id unless hash.empty?
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

  def id
    if self.class.guuid?
      self['id']
    else
      raise NotImplementedError
    end
  end


  def validate!; self; end
  def set_defaults!; self; end


  def storage(storage)
    @storage = storage.namespace(self.class.name)
    self
  end

  def all(page = 1)
    self.collection_for_key('all', page)
  end

  def collection_for_key(key, page = 1)
    page = 1 if page == 0
    per_page = 10
    # this should be done with by: 'nosort' but buggix is not in all versions of redis
    ids = @storage.zrevrange(key, (page - 1) * per_page, page * per_page)
    data_list = @storage.mget(*ids)
    data_list.map do |data|
      self.class.new(JSON.parse(data), @storage)
    end
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
    @storage.zadd("all", Time.now.to_i, id)
    self
  end

  def load(id)
    response = @storage.get(id)
    raise NotFound unless response
    self.class.new(JSON.parse(response), @storage)
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

  def to_json(*a)
    self.to_hash.to_json(*a)
  end

  protected

  def old_id=(id)
    @old_id = id
  end
end
