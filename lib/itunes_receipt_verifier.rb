require 'httparty'
require 'json'

module ItunesReceiptVerifier

  class ItunesReceiptError < Exception; end

  class << self
    def verify!(receipt_data, product_id)
      response = HTTParty.post('https://sandbox.itunes.apple.com/verifyReceipt',
                               body: { 'receipt-data' => receipt_data }.to_json,
                               timeout: 10)

      raise ItunesReceiptError unless response.code == 200
      json = JSON.parse(response.body)
      raise ItunesReceiptError unless json['status'] == 0
      raise ItunesReceiptError unless json['receipt']['product_id'] == product_id
    end
  end
end
