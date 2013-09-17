require 'android_receipt_verifier'

describe AndroidReceiptVerifier do

  GOOD_RECIEPT = '{"orderId":"12999763169054705758.1344768013077112","packageName":"com.ltst.prizeword","productId":"2013_9_silver_19","purchaseTime":1379408078000,"purchaseState":0,"developerPayload":"4455322b-8c03-4076-bdcb-09b21eb4c7c5","purchaseToken":"axtuleurcotagwsqqmebjtit.AO-J1OwbWyJbPLW2CCAVSGtAhCZIl2JNarw9J11i5oSbmNj8MeY3wEuSRHiFxoWd6P9IZcLcnoXDTKF4m2cQLhcJjg4CFS3IwdRDDaIsWvySYa0nXRSWOidiQlFsxgR-AKorgo4-2JIO"}'

  GOOD_SIGNATURE = 'Xlaw4EahaPogQLI8yxmM/4c0JCGt7/KEKlOVfUj2hMzSy/ckb7cq9j4k6AWqVe0cQDl5b9RcICu+QIe+4/+1vyBT9I2pUxmYD2F+kXAtZaqG6/0gthiSy0+qozp+TX2aZ4Qiz7OakA5J5dLJdwQfKHbYph7q0YPq6arWWOD8trCfZMAqRj5DUS7Mk+lAvgd+SFyAQxfIQeJzQPj/PtAQv0D0WPH3/gD2iHbp0svVYfsrRkqvQ5BH/uc3cWTFWu5OfN411swRUP4AEAf8+Eoea0Um19Xe4vshTAC6+Ki8GIz9hScKf5l6hyDB8qQW6MMZ2S1wSar/PELOAI4heKZJ0w=='

  PUBLIC_KEY = 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtspobFVSi6fZ6L3q5l64JVVcJaK19gVWllXQi5FxaN1V0Yti84O+Xzuw7fWWnrgleKLRNSMPrOd/rQrDAHhEm9kk7gq0PUzLwOzpqgnvWa9fsvQVc5jOi69O7B2Vn+KftNQ+VXReFXpEp4IA6DKIu3f0gNqha/szA2eq1uDyO+MXtU9Kpz2XeAedpVNSMn9OEDR2U4rN39GUqumg0NwidbpCkfhbmSGYoJOPAUOIXf5J1YIeR75pBV2GCUiT4d8fCGCv/UMunTbNkI+BjDov/hmzU4njk1sIlSSpz0a9pM4v6Q2dIrrKIrOsjSI7r+c/C2U2dqviAUZ96tYDS+bp7wIDAQAB'.freeze

  it 'should return true on good reciepts' do
    AndroidReceiptVerifier.verify!(PUBLIC_KEY, GOOD_RECIEPT, GOOD_SIGNATURE).should == true
  end

  it 'should raise exception on bad reciepts ' do
    lambda {
      bad_reciept = GOOD_RECIEPT.dup
      bad_reciept[1..2] = "DFADS"
      AndroidReceiptVerifier.verify!(PUBLIC_KEY, bad_reciept, GOOD_SIGNATURE)
    }.should raise_error(AndroidReceiptVerifier::AndroidReceiptError)
  end
end
