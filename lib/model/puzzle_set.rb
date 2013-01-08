require 'json'
require 'ext/hash'
require 'model/basic_model'

class PuzzleSet < BasicModel

  use_guuid!

  def validate!
    %w[year month name type id].each do |field|
      raise InvalidState.new("Missing required field: #{field}") unless self[field]
    end
    self
  end

  def save(*)
    super.tap { @storage.sadd("PuzzleSets:#{self['year']}##{self['month']}", self['id']) }
  end

  def delete(*)
    super.tap { @storage.srem("PuzzleSets:#{self['year']}##{self['month']}", self['id']) }
  end

  def set_defaults!
    self['puzzles'] = []
    self['month'] = Time.now.month
    self['year'] = Time.now.year
    self['published'] = false
  end

  def published_for(year = Time.now.year, month = Time.now.month)
    self.all_for(year, month).select { |o| o['published'] }
  end

  def all_for(year = Time.now.year, month = Time.now.month)
    keys = @storage.smembers("PuzzleSets:#{year}##{month}")
    keys.map{ |id| self.storage(@storage).load(id) }
  end
end
