require 'vcr'
require 'webmock'

require 'itunes_receipt_verifier'
require './spec/fixtures'

describe ItunesReceiptVerifier do

  include_context 'fixtures'

  before(:all) do
    r = Redis.new
    r.flushdb
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
        ItunesReceiptVerifier.verify!(Redis.new, receipt_data, 1, 'com.prizeword.2013_2_silver2_5')
      }.should_not raise_error(ItunesReceiptVerifier::ItunesReceiptError)
    end
  end

  describe "reciept without product_id" do
    it "should verify" do
      VCR.use_cassette('itunes_receipt_verifier_hints') do
        lambda {
          ItunesReceiptVerifier.verify!(Redis.new, receipt_data, 1)
        }.should_not raise_error(ItunesReceiptVerifier::ItunesReceiptError)
      end
    end

    it "user transaction should verify" do
      VCR.use_cassette('itunes_receipt_verifier_hints') do
        lambda {
          ItunesReceiptVerifier.verify!(Redis.new, receipt_data, 1)
        }.should_not raise_error(ItunesReceiptVerifier::ItunesInvalidUserError)
      end
    end

    it "transaction for enother user should not verify" do
      VCR.use_cassette('itunes_receipt_verifier_hints') do
        lambda {
          ItunesReceiptVerifier.verify!(Redis.new, receipt_data, 2)
        }.should raise_error(ItunesReceiptVerifier::ItunesInvalidUserError)
      end
    end
    it "should return recipe" do
      VCR.use_cassette('itunes_receipt_verifier_hints') do
        recipe = ItunesReceiptVerifier.verify!(Redis.new, receipt_data, 1)
        recipe['product_id'].should == 'com.prizeword.hints10'
      end
    end

  end
  it 'should verify product id' do
    VCR.use_cassette('itunes_receipt_verifier') do
      lambda {
        ItunesReceiptVerifier.verify!(Redis.new, receipt_data, 1, 'Bad_product_id')
      }.should raise_error(ItunesReceiptVerifier::ItunesReceiptError)
    end
  end

  it 'should raise on bad receipts' do
    VCR.use_cassette('itunes_receipt_verifier_bad') do
      lambda {
        ItunesReceiptVerifier.verify!(Redis.new, 'bad_receipt', 1, 'com.prizeword.2013_2_silver2_5')
      }.should raise_error(ItunesReceiptVerifier::ItunesReceiptError)
    end
  end

  it 'should verify user_id' do
    VCR.use_cassette('itunes_receipt_verifier') do
      lambda {
        ItunesReceiptVerifier.verify!(Redis.new, receipt_data, 1, 'com.prizeword.2013_2_silver2_5')
        ItunesReceiptVerifier.verify!(Redis.new, receipt_data, 1, 'com.prizeword.2013_2_silver2_5')
      }.should_not raise_error(ItunesReceiptVerifier::ItunesInvalidUserError)
    end
  end

  it 'should raise error on invalid user_id' do
    VCR.use_cassette('itunes_receipt_verifier') do
      lambda {
        ItunesReceiptVerifier.verify!(Redis.new, receipt_data, 1, 'com.prizeword.2013_2_silver2_5')
        ItunesReceiptVerifier.verify!(Redis.new, receipt_data, 2, 'com.prizeword.2013_2_silver2_5')
      }.should raise_error(ItunesReceiptVerifier::ItunesInvalidUserError)
    end
  end
end
