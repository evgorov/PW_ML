#!/usr/bin/ruby
# encoding: utf-8

libdir = File.join(File.dirname(__FILE__), '../lib')
$LOAD_PATH.unshift(libdir) unless $LOAD_PATH.include?(libdir)

require 'storage'
require 'user_factory'
require 'model/user'
require 'model/user_data'
require 'model/user_score'
require 'model/puzzle_data'

redis = Redis.new

# Migrate puzzle data
UserData.storage(redis).all_in_batches do |ud|
  keys = ud.to_hash.keys.grep(/puzzle-data./).map{ |o| o.gsub(/^puzzle-data./, '' ) }
  keys.each do |key|
    next if PuzzleData.storage(redis).exist?("puzzle-data:#{ud.id}:#{key}")
    data = ud["puzzle-data.#{key}"]
    puzzle_data = PuzzleData.new.storage(redis)
    puzzle_data['user_id'] = ud.id
    puzzle_data['puzzle_id'] = key
    puzzle_data['data'] = data
    puzzle_data.save
  end
end

# Migrate sets
UserData.storage(redis).all_in_batches do |ud|
  sets = ud['sets']
  ud['sets'] = []
  sets.each do |set|
    ud.add_set(set)
  end
end
