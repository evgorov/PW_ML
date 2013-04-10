require 'grocer'
require 'model/basic_model'

class Device < BasicModel
  def id
    self['id']
  end

  def validate!
    raise InvalidState.new("Missing device id") unless self['id']
  end

  def send_notifications(message, options)
    pusher = Grocer.pusher(options)
    self.all_in_batches { |item|  pusher.push(notification(item['id'], message)) }
  end

  private

  def notification(token, message)
    Grocer::Notification.new(
                             device_token: token,
                             alert:        message,
                             expiry:       Time.now + 60*60,
                             badge:        0,
                             sound:        "push.caf",
                             )
  end

end
