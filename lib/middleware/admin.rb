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

    get '/users/paginate' do
      authorize!
      users = User.storage(env['redis'])
      page = params[:page].to_i
      page = 1 if page == 0
      {
        users: users.all(page),
        total_pages: (users.size.to_f / User::PER_PAGE).ceil,
        current_page: page
      }.to_json
    end
    get '/sets' do
      authorize!
      args = []
      if params['year'] && params['month']
        args << params['year'].to_i
        args << params['month'].to_i
      end
      { 'sets' => PuzzleSet.storage(env['redis']).all_for(*args) }.to_json
    end

    post '/sets' do
      authorize!
      PuzzleSet.new.storage(env['redis']).tap { |o|
        o['year'] = params['year'].to_i
        o['month'] = params['month'].to_i
        o['name'] = params['name']
        o['published'] = (params['published'] == 'true')
        o['type'] = params['type']
        o['puzzles'] = JSON.parse(params['puzzles'])
      }.save.to_json
    end

    put '/sets/:id' do
      authorize!
      PuzzleSet.storage(env['redis']).load(params['id']).tap { |o|
        o['year'] = params['year'].to_i
        o['moth'] = params['month'].to_i
        o['name'] = params['name']
        o['published'] = (params['published'] == 'true')
        o['type'] = params['type']
        o['puzzles'] = JSON.parse(params['puzzles'])
      }.save.to_json
    end

    delete '/sets/:id' do
      authorize!
      PuzzleSet.storage(env['redis']).load(params['id']).delete
      { 'message' => "Puzzle set deleted"}.to_json
    end

  end
end
