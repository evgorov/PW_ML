require 'json'
require 'time'
require 'securerandom'
require 'ext/hash'
require 'model/basic_model'

class Puzzle < BasicModel
  PER_PAGE = 20
  use_guuid!

  def after_save
    super
    if self['set_id']
      @storage.zrem("free", self.id)
    else
      @storage.zadd("free", Time.now.to_i, self.id)
    end
  end

  def delete(*a)
    @storage.zrem("free", self.id)
    super(*a)
  end

  def by_sets(page=1)
    self.collection_for_key("rating##{current_period}", page)
  end
end
