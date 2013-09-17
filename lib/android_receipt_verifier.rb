require 'rubygems'
require 'openssl'
require 'base64'

module AndroidReceiptVerifier

  class AndroidReceiptError < Exception; end

  class << self
    def verify!(public_key, receipt_data, signature)
      key = OpenSSL::PKey::RSA.new(Base64.decode64(public_key))
      verified = key.verify(OpenSSL::Digest::SHA1.new, Base64.decode64(signature), receipt_data)
      raise AndroidReceiptError unless verified
      true
    end
  end
end
