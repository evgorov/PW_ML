require 'sinatra/base'
require 'user_factory'
require 'user'
require 'user_puzzles'

module Middleware
  class Users < Sinatra::Base

    before do
      content_type 'application/json'
    end

    get '/me' do
      env['token_auth'].authorize!
      { me: env['token_auth'].user }.to_json
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

    post '/sets/:id/buy' do
      env['token_auth'].authorize!
      UserPuzzles.
        storage(env['redis']).
        puzzles_for(env['token_auth'].user.id).
        tap{ |o| o.add_set(params['id']) }.
        to_json
    end

    get '/puzzles' do
      env['token_auth'].authorize!
      args = [env['token_auth'].user.id]
      if params['year'] && params['month']
        args << params['year'].to_i
        args << params['month'].to_i
      end
      UserPuzzles.storage(env['redis']).puzzles_for(*args).to_json
    end

    get '/puzzles/:id' do
      env['token_auth'].authorize!
      env['token_auth'].user["puzzles.#{params[:id]}"].to_json
    end

    put '/puzzles/:id' do
      begin
        JSON.parse(params['puzzle_data'])
      rescue JSON::ParserError
        halt(403, { 'message' => 'invalid json data'}.to_json)
      end

      env['token_auth'].authorize!
      env['token_auth'].user["puzzles.#{params[:id]}"] = params['puzzle_data']
    end
  end
end
