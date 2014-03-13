#encoding: utf-8

require 'model/user'
require 'model/user_data'
require 'itunes_receipt_verifier'
require 'android_receipt_verifier'

module Middleware
  class Store < Sinatra::Base
    helpers do
      def current_user
        @current_user ||= env['token_auth'].user.user_data
      end
    end

    before do
      content_type 'application/json'
    end

    error(ItunesReceiptVerifier::ItunesInvalidUserError) { halt(403, { message: 'Этот товар куплен для другого пользователя' }.to_json) }
    error(ItunesReceiptVerifier::ItunesDoubleTransactionError) { halt(403, { message: 'Этот товар уже был куплен ранее' }.to_json) }
    error(ItunesReceiptVerifier::ItunesReceiptError) { halt(403, { message: 'Ошибка валидации чека Itunes' }.to_json) }
    error(AndroidReceiptVerifier::AndroidReceiptError) { halt(403, { message: 'Ошибка валидации чека Play store' }.to_json) }
    
    get '/user_puzzles' do
      env['token_auth'].authorize!
      user_sets = current_user
        .sets
        .map { |o| o['puzzles'] }
        .flatten

      if params['ids']
        ids = params['ids'].split(',')
        user_sets.select{ |o| ids.include?(o['id']) }.to_json
      else
        user_sets.to_json
      end
    end

    post '/hints/buy' do
      env['token_auth'].authorize!
      if params['receipt_data'] || params['receipt-data']
        receipt_data = params['receipt_data'] || params['receipt-data']
        recipe = ItunesReceiptVerifier.verify!(env['redis'], receipt_data, current_user.id)
        hints = AppConfig.ios[:products][recipe['product_id'].to_sym]
        if hints
          current_user['hints'] += hints.to_i
          current_user.save
        end
      end
      
      { me: current_user }.to_json
    end

    post '/sets/:id/buy' do
      env['token_auth'].authorize!

      puzzle_set = PuzzleSet.storage(env['redis']).load(params['id'])
      if puzzle_set['type'] != 'free' && self.class.settings.environment != :test
        if params['receipt_data'] || params['receipt-data']
          receipt_data = params['receipt_data'] || params['receipt-data']
          ItunesReceiptVerifier.verify!(env['redis'], receipt_data, current_user.id, "com.prizeword.#{params['id']}")
        else
          AndroidReceiptVerifier.verify!(android_public_key, params['android_reciept'].gsub(/\\/,''), params['android_signature'])
        end
      end

      result = puzzle_set.to_hash.merge('bought' => true)
      current_user.add_set!(result)
      result.to_json
    end
    
  private
    def android_public_key
      AppConfig.android[:public_key]
    end
  end
end
