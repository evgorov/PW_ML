require 'securerandom'
require 'dummy_storage'
require 'json'

class BasicModel

  PER_PAGE = 50

  class NotFound < Exception; end
  class InvalidState < Exception; end
  class AlreadyExist < Exception; end

  class << self
    def storage(storage)
      new.storage(storage)
    end

    def use_guuid!; @use_guuid = true; end
    def guuid?; !!@use_guuid; end
    def uniq!; @uniq = true; end
    def uniq?; !!@uniq; end
  end

  def dup
    self.class.new(@hash.dup, @storage)
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
    page = 1 if page == 0
    self.collection_for_key('all', page)
  end

  def batches(&blk)
    page, items = 0, []
    yield items while !(items = self.all(page += 1)).empty?
  end

  def all_in_batches(&blk)
    batches { |arr| arr.each(&blk) }
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
    ids = @storage.zrevrange(key, (page - 1) * self.class::PER_PAGE, page * self.class::PER_PAGE - 1)
    data_list = case ids.size
                when 0; []
                when 1; [@storage.get(ids.first)]
                else; @storage.mget(*ids)
                end
    data_list.map { |data| self.class.new(JSON.parse(data), @storage).tap(&:after_load) }
  end

  def save(skip_validation = false)
    before_save
    self['created_at'] = Time.now if self.new? && !self['created_at']
    id = if self.class.guuid?
           self['id'] ||= "#{self.class.name}.#{SecureRandom.uuid}"
         else
           self['id'] = self.id
         end

    validate! unless skip_validation

    if self.class.uniq?
      raise AlreadyExist.new unless @storage.setnx(id, {}.merge(self).to_json)
    else
      @storage.set(id, {}.merge(self).to_json)
    end

    @storage.zadd("all", Time.now.to_i, id)
    @saved = true

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

  def exist?(id)
    @storage.get(id) != nil
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
    @hash.dup
  end

  def to_json(*a)
    self.to_hash.to_json(*a)
  end

  def new?
    !@saved && !@loaded
  end

  def id_changed?
    !!(@old_hash['id'] && @hash['id'] != @old_hash['id'])
  end
end
