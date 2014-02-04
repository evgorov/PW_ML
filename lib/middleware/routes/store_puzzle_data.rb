#encoding: utf-8

require 'time'
require 'ext/hash'
require 'sinatra/base'
require 'model/puzzle_data'

module Middleware
  class StorePuzzleData < Sinatra::Base

    helpers do
      def current_user
        @current_user ||= env['token_auth'].user.user_data
      end
    end

    before do
      content_type 'application/json'
    end

    get '/puzzles/:id' do
      env['token_auth'].authorize!
      begin
        PuzzleData.storage(env['redis']).for(current_user.id, params[:id])['data']
      rescue BasicModel::NotFound
        nil
      end.to_json
    end

    put '/puzzles/:id' do
      begin
        JSON.parse(params['puzzle_data'])
      rescue JSON::ParserError
        halt(403, { 'message' => 'invalid json data'}.to_json)
      end

      env['token_auth'].authorize!

      begin
        puzzle_data = PuzzleData.storage(env['redis']).for(current_user.id, params[:id])
      rescue BasicModel::NotFound
        puzzle_data = PuzzleData.new.storage(env['redis'])
        puzzle_data['user_id'] = current_user.id
        puzzle_data['puzzle_id'] = params[:id]
      end

      puzzle_data['data'] = params['puzzle_data']
      puzzle_data.save

      { "message" => "ok" }.to_json
    end
  end
end
