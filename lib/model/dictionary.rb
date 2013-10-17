require 'model/basic_model'

class Dictionary < BasicModel
  PER_PAGE = 100
  use_guuid!


  def validate!
    raise InvalidState.new("Missing title") unless self['title']
    raise InvalidState.new("Missing body") unless self['body']
  end

  def to_json(*a)
    self.to_hash.except('body').to_json(*a)
  end
end
