require 'json'
require 'ext/hash'
require 'model/basic_model'

class UserPuzzles < BasicModel

  def puzzles_for(user_id, year = Time.now.year, month = Time.now.month)
    self.load([year, month, user_id].join('#'))
  rescue NotFound
    user_puzzles = UserPuzzles.new
    user_puzzles['user_id'] = user_id
    user_puzzles['year'] = year
    user_puzzles['month'] = month
    user_puzzles.storage(@storage).save
  end

  def add_set(set_id)
    return if self['sets'].any? { |o| o['id'] == set_id }
    puzzle_set = PuzzleSet.storage(@storage).load(set_id)
    raise InvalidState if self['month'] != puzzle_set['month'] || self['year'] != puzzle_set['year']
    self['sets'] << puzzle_set.to_hash
    self.save
  end

  def id
    [self['year'], self['month'], self['user_id']].
      map{ |o| raise InvalidState unless o; o }.
      join('#')
  end

  def validate!
    %w[year month user_id sets].each do |field|
      raise InvalidState.new("Missing required field: #{field}") unless self[field]
    end
    self
  end

  def set_defaults!
    self['sets'] = []
  end
end
