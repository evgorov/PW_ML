require 'model/basic_model'

class UserScore < BasicModel

  uniq!

  PER_PAGE = 10000

  def create(user_id, score, solved, source)
    model = self.class.new.storage(@storage)
    model['user_id'] = user_id
    model['score'] = score
    model['solved'] = solved
    model['source'] = source
    model.save
  end

  def score_for(user_id, source)
    self.load("#{user_id}##{source}")
  end

  def scores_for(user_id)
    collection_for_key(user_id)
  end

  def after_save
    super
    @storage.zadd(self['user_id'], Time.now.to_i, id)
  end

  def id
    "#{self['user_id']}##{self['source']}"
  end
end
