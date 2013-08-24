require 'sinatra/base'
require 'model/puzzle_set'
require 'model/service_message'
require 'model/coefficients'

module Middleware
  class Admin < Sinatra::Base

    before do
      content_type 'application/json'
    end

    helpers do
      def current_user
        @current_user ||= env['token_auth'].user.user_data
      end

      def authorize_admin!
        env['token_auth'].authorize!
        env['token_auth'].unauthorized! unless current_user['role'] == 'admin'
      end

      def set_puzzle_set_data(o, params)
        o['year'] = params['year'].to_i
        o['month'] = params['month'].to_i
        o['name'] = params['name']
        o['published'] = (params['published'] == 'true')
        o['type'] = params['type']
        if params['puzzle_ids']
          o['puzzle_ids'] =  JSON.parse(params['puzzle_ids'])
        end
      end

      def set_puzzle_data(o, params)
        o['base_score'] = params['base_score'].to_i
        o['height'] = params['height'].to_i
        o['width'] = params['width'].to_i
        o['issuedAt'] = params['issuedAt']
        o['name'] = params['name']
        if params['questions']
          o['questions'] =  JSON.parse(params['questions'])
        end
        o['time_given'] = params['time_given'].to_i
      end
    end

    error(BasicModel::NotFound) { halt(403, { message: 'Invalid username or password' }.to_json) }

    put '/service_messages' do
      authorize_admin!
      messages = params.extract('message1', 'message2', 'message3')
      messages.delete_if { |k,v| v.empty? }
      ServiceMessage.storage(env['redis']).messages = messages
      messages.to_json
    end

    put '/coefficients' do
      authorize_admin!
      coefficients = Hash[params.map{ |k,v| [k, v.to_i] }]
      coefficients.delete('session_key')
      Coefficients.storage(env['redis']).coefficients = coefficients
      coefficients.to_json
    end

    get '/users/paginate' do
      authorize_admin!
      users = UserData.storage(env['redis'])
      page = params[:page].to_i
      page = 1 if page == 0
      {
        users: users.all(page),
        total_pages: (users.size.to_f / UserData::PER_PAGE).ceil,
        current_page: page
      }.to_json
    end

    get '/devices' do
      authorize_admin!
      Device.new.storage(env['redis']).all(params['page'].to_i).to_json
    end

    post '/questions' do
      authorize_admin!
      Puzzle.new.storage(env['redis']).tap { |o|
        set_puzzle_data(o, params)
      }.save.to_json
    end

    put '/questions/:id' do
      authorize_admin!
      Puzzle.storage(env['redis']).load(params['id']).tap { |o|
        set_puzzle_data(o, params)
      }.save.to_json
    end

    get '/questions' do
      authorize_admin!
      { 'puzzles' => Puzzle.storage(env['redis']).all }.to_json
    end

    get '/sets' do
      authorize_admin!
      args = []
      if params['year'] && params['month']
        args << params['year'].to_i
        args << params['month'].to_i
      end
      { 'sets' => PuzzleSet.storage(env['redis']).all_for(*args) }.to_json
    end

    post '/sets' do
      authorize_admin!
      PuzzleSet.new.storage(env['redis']).tap { |o|
        set_puzzle_set_data(o, params)
      }.save.to_json
    end

    put '/sets/:id' do
      authorize_admin!
      PuzzleSet.storage(env['redis']).load(params['id']).tap { |o|
        set_puzzle_set_data(o, params)
      }.save.to_json
    end

    delete '/sets/:id' do
      authorize_admin!
      PuzzleSet.storage(env['redis']).load(params['id']).delete
      { 'message' => "Puzzle set deleted"}.to_json
    end

    get '/counters' do
      authorize_admin!
      days = (0..30).map{|i| Time.now - i * 60 * 60 * 24 }.reverse.map{ |o| o.strftime("%Y-%m-%d") }
      result = %w[logins sets_bought hints_bought scored].inject({'days' => days}) do  |acc, counter|
        acc[counter] = env['counter'].get(*days.map { |o| o + ':' + counter })
        acc
      end

      result.to_json
    end
  end
end
