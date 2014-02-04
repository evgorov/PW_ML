require 'model/basic_model'
require 'digest/sha2'

class PuzzleVersion < BasicModel

  def id
    Digest::SHA256.hexdigest(self['data'])
  end

  def validate!
    raise InvalidState.new("Missing data") unless self['data']
  end

end
