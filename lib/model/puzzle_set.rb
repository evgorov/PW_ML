require 'json'
require 'time'
require 'securerandom'
require 'ext/hash'
require 'model/basic_model'
require 'model/puzzle'
require 'digest/sha2'

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

  def after_load
    super
    self['puzzle_ids'] ||= []
    load_puzzles
  end

  def before_save
    super
    check_puzzle_ids
    @hash.delete('puzzles')
  end

  def set_defaults!
    self['puzzle_ids'] ||= []
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

  private

  def load_puzzles
    self['puzzles'] = self['puzzle_ids'].map do |o|
      Puzzle.storage(@storage).load(o)
    end
  end

  def check_puzzle_ids
    self['puzzle_ids'] = self['puzzle_ids'].reject do |o|
      begin
        Puzzle.storage(@storage).load(o)
        false
      rescue BasicModel::NotFound
        true
      end
    end
  end
end
