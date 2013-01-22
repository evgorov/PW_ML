require 'sinatra/base'
require 'user_factory'
require 'model/user'
require 'model/service_message'

module Middleware
  class Users < Sinatra::Base

    before do
      content_type 'application/json'
    end

    get '/me' do
      env['token_auth'].authorize!
      { me: env['token_auth'].user }.to_json
    end

    get '/service_message' do
      env['token_auth'].authorize!
      message = ServiceMessage.storage(env['redis']).message
      { service_message: message }.to_json
    end

    post '/me' do
      env['token_auth'].authorize!
      user = env['token_auth'].user
      user.merge_fields_user_can_change!(params)
      user.save
      # We need to update user as it's email can change
      env['token_auth'].update_user(user)
      { me: user }.to_json
    end

    post '/score' do
      env['token_auth'].authorize!
      user = env['token_auth'].user
      user['month_score'] += params['score'].to_i
      user.save
      { me: user }.to_json
    end

    post '/hints' do
      env['token_auth'].authorize!
      user = env['token_auth'].user
      user['hints'] += params['hints_change'].to_i
      user.save
      { me: user }.to_json
    end

    get '/sets_available' do
      args = []
      if params['year'] && params['month']
        args << params['year'].to_i
        args << params['month'].to_i
      end

      PuzzleSet.storage(env['redis']).published_for(*args).to_json
    end

    get '/users' do
      result = {}
      result['users'] = User.storage(env['redis']).users_by_rating(params['page'].to_i)
      result['me'] = env['token_auth'].user if env['token_auth'].authorized?
      result.to_json
    end

    post '/hints/buy' do
      # need this to allow counters include hints_bought,
      { "message" => "ok" }.to_json
    end

    post '/sets/:id/buy' do
      env['token_auth'].authorize!
      puzzle_set = PuzzleSet.storage(env['redis']).load(params['id'])

      user = env['token_auth'].user
      user['sets'] ||= []
      user['sets'] = [puzzle_set.to_hash] | user['sets']
      user.save

      puzzle_set.to_json
    end

    get '/puzzles' do
      env['token_auth'].authorize!
      {
        sets: env['token_auth'].user['sets'] || []
      }.to_json
    end

    get '/puzzles/:id' do
      env['token_auth'].authorize!
      env['token_auth'].user["puzzle-data.#{params[:id]}"].to_json
    end

    put '/puzzles/:id' do
      begin
        JSON.parse(params['puzzle_data'])
      rescue JSON::ParserError
        halt(403, { 'message' => 'invalid json data'}.to_json)
      end

      env['token_auth'].authorize!
      env['token_auth'].user["puzzle-data.#{params[:id]}"] = params['puzzle_data']
    end
  end
end
