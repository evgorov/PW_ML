require 'sinatra/base'
require 'model/puzzle_set'
require 'model/service_message'

module Middleware
  class Admin < Sinatra::Base

    before do
      content_type 'application/json'
    end

    helpers do
      def current_user
        @current_user ||= env['token_auth'].user.user_data
      end

      def authorize!
        env['token_auth'].authorize!
        env['token_auth'].unauthorized! unless current_user['role'] == 'admin'
      end
    end

    put '/service_message' do
      authorize!
      service_message = ServiceMessage.storage(env['redis'])
      service_message.message = params['service_message']
      { service_message: service_message.message }.to_json
    end

    get '/users/paginate' do
      authorize!
      users = UserData.storage(env['redis'])
      page = params[:page].to_i
      page = 1 if page == 0
      {
        users: users.all(page),
        total_pages: (users.size.to_f / UserData::PER_PAGE).ceil,
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
        o['month'] = params['month'].to_i
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

    get '/counters' do
      authorize!
      days = (0..30).map{|i| Time.now - i * 60 * 60 * 24 }.reverse.map{ |o| o.strftime("%Y-%m-%d") }
      result = %w[logins sets_bought hints_bought scored].inject({'days' => days}) do  |acc, counter|
        acc[counter] = env['counter'].get(*days.map { |o| o + ':' + counter })
        acc
      end

      result.to_json
    end
  end
end
