require 'model/basic_model'

class PuzzleData < BasicModel

  def for(user_id, puzzle_id)
    self.load(generate_id(user_id, puzzle_id))
  end

  def id
    generate_id(self['user_id'], self['puzzle_id'])
  end

  def validate!
    raise InvalidState.new("Missing user_id") unless self['user_id']
    raise InvalidState.new("Missing puzzle_id") unless self['puzzle_id']
  end

  private

  def generate_id(user_id, puzzle_id)
    "puzzle-data:#{user_id}:#{puzzle_id}"
  end

end
