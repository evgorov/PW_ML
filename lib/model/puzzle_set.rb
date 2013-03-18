require 'json'
require 'ext/hash'
require 'model/basic_model'
require 'securerandom'

class PuzzleSet < BasicModel

  def validate!
    %w[year month name type id].each do |field|
      raise InvalidState.new("Missing required field: #{field}") unless self[field]
    end
    self
  end

  def id
    self['id'] || "#{self['year']}_#{self['month']}_#{self['type']}_#{@storage.incr('SetCounter')}"
  end

  def save(*)
    super.tap { @storage.sadd("PuzzleSets:#{self['year']}##{self['month']}", self['id']) }
  end

  def delete(*)
    super.tap { @storage.srem("PuzzleSets:#{self['year']}##{self['month']}", self['id']) }
  end

  def before_save
    super
    return unless self.to_hash.has_key?('puzzles')
    self['puzzles'].each { |o| o['id'] = SecureRandom.uuid unless o.has_key?('id') }
  end

  def set_defaults!
    self['puzzles'] = []
    self['month'] = Time.now.month
    self['year'] = Time.now.year
    self['published'] = false
  end

  def published_for(year = Time.now.year, month = Time.now.month)
    self.all_for(year, month).
      select { |o| o['published'] }.
      sort_by { |o| Time.parse(o['created_at']) }
  end

  def all_for(year = Time.now.year, month = Time.now.month)
    keys = @storage.smembers("PuzzleSets:#{year}##{month}")
    keys.map{ |id| self.storage(@storage).load(id) }
  end
end
