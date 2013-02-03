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
      env['token_auth'].authorize!
      user_sets = if env['token_auth'].user['sets']
                    Hash[env['token_auth'].user['sets'].map{ |o| [o['id'], o]}]
                  else
                    {}
                  end

      args = []
      if params['year'] && params['month']
        args << params['year'].to_i
        args << params['month'].to_i
      end

      sets_available = PuzzleSet.storage(env['redis']).published_for(*args)
      sets_available.each { |h| h['bought'] = !!user_sets[h['id']] }

      sets_available.to_json
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
      env['token_auth'].user.save

      { "message" => "ok" }.to_json
    end

    get '/:provider/friends' do
      env['token_auth'].authorize!
      env['token_auth'].user.fetch_friends
      env['token_auth'].user.save
      env['token_auth'].user['friends'].values.to_json
    end

    post '/:provider/invite' do
      halt(403, { 'message' => 'missing ids'}.to_json) unless params['ids']
      env['token_auth'].authorize!
      env['token_auth'].user.fetch_friends

      params['ids'].split(',').each do |id|
        if env['token_auth'].user['friends'][id]
          env['token_auth'].user['friends'][id]['invite_sent'] = true
        end
      end
      env['token_auth'].user.save
      { "message" => "ok" }.to_json
    end
  end
end
