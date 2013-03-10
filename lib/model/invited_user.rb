require 'model/basic_model'

class InvitedUser < BasicModel

  def initialize(*)
    super
    self['invited_by'] ||= []
  end

  def id
    self['id']
  end

  def validate!
    raise InvalidState.new("Missing user id") unless self['id']
  end
end

