require 'pushmeup/android'
require 'model/basic_model'

class AndroidDevice < BasicModel

  def id
    self['id']
  end

  def validate!
    raise InvalidState.new("Missing device id") unless self['id']
  end

  def send_notifications(message)
    self.batches do |arr|
      GCM.send_notification(arr.map(&:id), { message: message })
    end
  end
end
