require 'securerandom'
require 'dummy_storage'
require 'json'

class BasicModel

  PER_PAGE = 50

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
    @hash, @old_hash, @storage = hash, hash.dup, storage
    set_defaults! if @hash.empty?
    self.merge!(@hash)
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
  def before_load; @loaded = true; self; end
  def after_load; self; end
  def before_save; self; end
  def after_save; self; end

  def storage(storage)
    @storage = storage.namespace(self.class.name)
    self
  end

  def all(page = 1)
    self.collection_for_key('all', page)
  end

  def size
    self.collection_size_for_key('all');
  end

  def collection_size_for_key(key)
    @storage.zcard(key)
  end

  def collection_for_key(key, page = 1)
    page = 1 if page == 0
    # this should be done with by: 'nosort' but buggix is not in all versions of redis
    ids = @storage.zrevrange(key, (page - 1) * PER_PAGE, page * PER_PAGE - 1)
    data_list = @storage.mget(*ids)
    data_list.map do |data|
      self.class.new(JSON.parse(data), @storage).tap(&:after_load)
    end
  end

  def save(skip_validation = false)
    before_save
    self['created_at'] = Time.now if self.new? && !self['created_at']
    id = if self.class.guuid?
           self['id'] ||= SecureRandom.uuid
         else
           self['id'] = self.id
         end

    validate! unless skip_validation

    @storage.set(id, {}.merge(self).to_json)
    @storage.zadd("all", Time.now.to_i, id)

    # delete old hash if id changed
    self.class.new(@old_hash, @storage).delete if self.id_changed?

    after_save
    self
  end

  def load(id)
    before_load
    response = @storage.get(id)
    raise NotFound unless response
    self.class.new(JSON.parse(response), @storage).tap(&:before_load).tap(&:after_load)
  end

  def delete
    return unless self.id
    @storage.del(self.id)
    @storage.zrem('all', self.id)
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

  def new?
    !@loaded
  end

  def id_changed?
    !!(@old_hash['id'] && @hash['id'] != @old_hash['id'])
  end
end
