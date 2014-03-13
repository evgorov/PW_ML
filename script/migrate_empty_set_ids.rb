#!/usr/bin/ruby
# encoding: utf-8

libdir = File.join(File.dirname(__FILE__), '../lib')
$LOAD_PATH.unshift(libdir) unless $LOAD_PATH.include?(libdir)

require 'storage'
require 'model/puzzle'

Puzzle.storage(Redis.new).all_in_batches(&:save)
