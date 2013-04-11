require 'httparty'
require 'storage'
require 'json'

module ItunesReceiptVerifier

  class ItunesReceiptError < Exception; end
  class ItunesInvalidUserError < Exception; end

  class << self
    def verify!(redis, receipt_data, user_id, product_id)
      response = HTTParty.post('https://sandbox.itunes.apple.com/verifyReceipt',
                               body: { 'receipt-data' => receipt_data }.to_json,
                               timeout: 10)

      raise ItunesReceiptError unless response.code == 200
      json = JSON.parse(response.body)
      raise ItunesReceiptError unless json['status'] == 0
      raise ItunesReceiptError unless json['receipt']['product_id'] == product_id
      verify_user!(redis, json['receipt']['original_transaction_id'], user_id)
    end

    private

    def verify_user!(redis, original_transaction_id, user_id)
      redis = redis.namespace('itunes_receipt_verifier')
      stored_user_id = redis.get(original_transaction_id)
      return redis.set(original_transaction_id, user_id) unless stored_user_id
      raise ItunesInvalidUserError unless stored_user_id == user_id.to_s
    end
  end
end