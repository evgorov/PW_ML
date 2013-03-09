require 'model/basic_model'

class Device < BasicModel
  def id
    self['id']
  end

  def validate!
    raise InvalidState.new("Missing device id") unless self['id']
  end
end
