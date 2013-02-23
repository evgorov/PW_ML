require 'vcr'
require 'webmock'

require 'itunes_receipt_verifier'
require './spec/fixtures'

describe ItunesReceiptVerifier do

  include_context 'fixtures'

  before(:all) do
    VCR.configure do |c|
      c.allow_http_connections_when_no_cassette = true
      c.cassette_library_dir = 'fixtures/vcr_cassettes'
      c.hook_into :webmock
    end
    VCR.turn_on!
  end

  after(:all) do
    VCR.turn_off!
  end

  it 'should verify reciepts' do
    VCR.use_cassette('itunes_receipt_verifier') do
      lambda {
        ItunesReceiptVerifier.verify!(receipt_data, 'ru.aipmedia.ios.prizeword.2013_2_silver2_5')
      }.should_not raise_error(ItunesReceiptVerifier::ItunesReceiptError)
    end
  end

  it 'should verify product id' do
    VCR.use_cassette('itunes_receipt_verifier') do
      lambda {
        ItunesReceiptVerifier.verify!(receipt_data, 'bad product id')
      }.should raise_error(ItunesReceiptVerifier::ItunesReceiptError)
    end
  end

  it 'should raise on bad receipts' do
    VCR.use_cassette('itunes_receipt_verifier_bad') do
      lambda {
        ItunesReceiptVerifier.verify!('bad receipt', 'ru.aipmedia.ios.prizeword.2013_2_silver2_5')
      }.should raise_error(ItunesReceiptVerifier::ItunesReceiptError)
    end
  end
end
