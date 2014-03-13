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

UserData.storage(redis).all_in_batches do |ud|
  ud['last_notification_time'] = Time.new(1970)
  ud['is_app_rated'] = false
  ud.save
  print '.'
end