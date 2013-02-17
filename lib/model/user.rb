require 'bcrypt'
require 'json'
require 'ext/hash'
require 'model/basic_model'
require 'model/user_data'
require 'friends_fetcher'

class AbstractClassError < NotImplementedError; end

class User < BasicModel

  def before_save
    super
    return if self['user_data_id']
    self['user_data_id'] = UserData.new({}, @storage).tap(&:save).id
  end

  def user_data
    UserData.storage(@storage).load(self['user_data_id'])
  end


  def []=(key, value)
    case(key)
    when 'password'; super('password_hash', BCrypt::Password.create(value)) if value
    else; super(key, value)
    end
  end

  def [](key)
    case(key)
    when 'password';
      return nil unless @hash.has_key?('password_hash')
      BCrypt::Password.new(super('password_hash'))
    else; super(key)
    end
  end

  def storage(storage)
    @storage = storage.namespace('User')
    self
  end

  def load_by_key(key)
    self.load("#{self.provider_name}##{key}")
  end

  def save(*a)
    raise AbstractClassError if self.class == User
    self['provider'] = self.provider_name
    super(*a)
    self
  end

  def provider_name
    raise AbstractClassError if self.class.name == 'User'
    self.class.name.chomp('User').downcase
  end

  # Forceds simple format to prevent accidental password leakage
  def to_json(*a)
    { id: self.id }
  end
end

class RegisteredUser < User

  def id
    raise InvalidState unless self['email']
    "registered##{self['email']}"
  end

  def authenticate(email, password)
    self.load_by_key(email).tap do |user|
      raise NotFound unless user['password'] == password
    end
  end

  def validate!
    super
    raise InvalidState.new("Missing required field: email") unless self['email']
    raise InvalidState.new("Missing required field: password") unless self['password']
    # all validation will be run with storage anyway when save is called
    return unless @storage
    if (self.new? || self.id_changed?) && @storage.get(self.id)
      raise InvalidState.new("Email is already taken")
    end
  end
end

class VkontakteUser < User

  def id
    raise InvalidState unless self['vkontakte_id']
    "vkontakte##{self['vkontakte_id']}"
  end

  def validate!
    raise InvalidState.new("Missing required field: vkontakte_id") unless self['vkontakte_id']
  end
end

class FacebookUser < User

  def id
    raise InvalidState unless self['facebook_id']
    "facebook##{self['facebook_id']}"
  end

  def validate!
    raise InvalidState.new("Missing required field: facebook_id") unless self['facebook_id']
    self
  end
end
