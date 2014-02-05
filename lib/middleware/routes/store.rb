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
    error(ItunesReceiptVerifier::ItunesReceiptError) { halt(403, { message: 'Ошибка валидации чека Itunes' }.to_json) }
    error(AndroidReceiptVerifier::AndroidReceiptError) { halt(403, { message: 'Ошибка валидации чека Play store' }.to_json) }

    ANDROID_PUBLIC_KEY = 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtspobFVSi6fZ6L3q5l64JVVcJaK19gVWllXQi5FxaN1V0Yti84O+Xzuw7fWWnrgleKLRNSMPrOd/rQrDAHhEm9kk7gq0PUzLwOzpqgnvWa9fsvQVc5jOi69O7B2Vn+KftNQ+VXReFXpEp4IA6DKIu3f0gNqha/szA2eq1uDyO+MXtU9Kpz2XeAedpVNSMn9OEDR2U4rN39GUqumg0NwidbpCkfhbmSGYoJOPAUOIXf5J1YIeR75pBV2GCUiT4d8fCGCv/UMunTbNkI+BjDov/hmzU4njk1sIlSSpz0a9pM4v6Q2dIrrKIrOsjSI7r+c/C2U2dqviAUZ96tYDS+bp7wIDAQAB'.freeze

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
      # need this to allow counters include hints_bought,
      { "message" => "ok" }.to_json
    end

    post '/sets/:id/buy' do
      env['token_auth'].authorize!

      puzzle_set = PuzzleSet.storage(env['redis']).load(params['id'])

      if puzzle_set['type'] != 'free' && self.class.settings.environment != :test
        if params['receipt_data'] || params['receipt-data']
          receipt_data = params['receipt_data'] || params['receipt-data']
          ItunesReceiptVerifier.verify!(env['redis'], receipt_data, current_user.id, "com.prizeword.#{params['id']}")
        else
          AndroidReceiptVerifier.verify!(ANDROID_PUBLIC_KEY, params['android_reciept'].gsub(/\\/,''), params['android_signature'])
        end
      end

      result = puzzle_set.to_hash.merge('bought' => true)
      current_user.add_set!(result)
      result.to_json
    end
  end
end
