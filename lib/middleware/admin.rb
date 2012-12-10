require 'sinatra/base'
require 'puzzle_set'

module Middleware
  class Admin < Sinatra::Base

    before do
      content_type 'application/json'
    end

    helpers do
      def authorize!
        env['token_auth'].authorize!
        env['token_auth'].unauthorized! unless env['token_auth'].user['role'] == 'admin'
      end
    end

    get '/sets' do
      authorize!
      { 'sets' => PuzzleSet.storage(env['redis']).all(params[:page].to_i) }.to_json
    end

    post '/sets' do
      authorize!
      PuzzleSet.new.storage(env['redis']).tap { |o|
        o['year'] = params['year'].to_i
        o['moth'] = params['month'].to_i
        o['name'] = params['name']
        o['type'] = params['type']
        o['puzzles'] = JSON.parse(params['puzzles'])
      }.save.to_json
    end

    post '/sets/:id/publish' do
      authorize!
      PuzzleSet.storage(env['redis']).load(params['id']).tap{ |o| o.publish }.to_json
    end

    post '/sets/:id/unpublish' do
      authorize!
      PuzzleSet.storage(env['redis']).load(params['id']).tap{ |o| o.unpublish }.to_json
    end

    delete '/sets/:id' do
      authorize!
      PuzzleSet.storage(env['redis']).load(params['id']).delete
      { 'message' => "Puzzle set deleted"}
    end

  end
end
