#encoding: utf-8

require 'model/puzzle'
require 'model/puzzle_set'

module Middleware
  class Sets < Sinatra::Base

    helpers do
      def current_user
        @current_user ||= env['token_auth'].user.user_data
      end
    end

    before do
      content_type 'application/json'
    end

    get '/published_sets' do
      env['token_auth'].authorize!
      mode = (['short', 'full'] & [params['mode'], 'full']).first
      user_sets = if current_user['sets']
                    Hash[current_user['sets'].map{ |o| [o['id'], o]}]
                  else
                    {}
                  end

      args = []
      if params['year'] && params['month']
        args << params['year'].to_i
        args << params['month'].to_i
      end

      sets = PuzzleSet.storage(env['redis']).published_for(*args)
        .map(&:to_hash)
        .map{ |o| user_sets[o['id']] || o}
        .map{ |o| o['bought'] = !!user_sets[o['id']]; o }

      sets.each do |set|
        set['puzzles'] = set['puzzles'].map{ |o| o['id'] }
      end if mode == 'short'

      sets.to_json
    end
  end
end
