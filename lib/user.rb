require 'bcrypt'
require 'json'
require 'ext/hash'
require 'basic_model'

class User < BasicModel

  REQUIRED_FIELDS_FOR_REGISTRATION = %w[email name surname password]
  FIELDS_USER_CAN_CHANGE = REQUIRED_FIELDS_FOR_REGISTRATION + %w[birthdate userpic city]
  FIELDS_USER_CAN_SEE = FIELDS_USER_CAN_CHANGE -
                        ['password'] +
                        %w[id position solved month_score high_score dynamics hints provider]

  class << self

    def new
      return super unless self.name == 'User'
      raise NotImplementedError.new("This class is an abstract class, "+
                                    "you must use it's children.")
    end

    def inherited(subclass)
      @providers ||= {}
      @providers[subclass.new.provider_name] = subclass
    end

    def load_by_provider_and_key(storage, provider, key)
      @providers[provider].storage(storage).load_by_key(key)
    end

  end

  def []=(key, value)
    case(key)
    when 'password'; super('password_hash', BCrypt::Password.create(value))
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

  def merge!(other)
    # force storing 'password' field with []=
    other = other.dup
    if password = other.delete('password')
      self['password'] = password
    end
    super(other)
    self
  end

  def storage(storage)
    @storage = storage.namespace('User')
    self
  end

  def validate!
    %w[email name surname position solved role
       month_score high_score dynamics hints].each do |field|
      raise InvalidState.new("Missing required field: #{field}") unless self[field]
    end
  end

  def users_by_rating(page=1)
    page = 1 if page == 0
    per_page = 10
    ids = @storage.zrevrange('rating', (page - 1) * per_page, page * per_page)
    ids.map do |o|
      provider, key = o.split('#', 2)
      User.load_by_provider_and_key(@storage, provider, key)
    end
  end

  def load_by_key(key)
    self.load("#{self.provider_name}##{key}")
  end

  def save(*a)
    self['provider'] = self.provider_name
    super(*a)
    @storage.zadd('rating', self['month_score'].to_i, self.id)
    self
  end

  def id
    raise NotImplementedError
  end

  def provider_name
    raise NotImplementedError if self.class.name == 'User'
    self.class.name.chomp('User').downcase
  end

  def has_keys?(*a)
    return false if keys.size == 0
    a.all?{ |o| self[o] }
  end

  def merge_fields_user_can_change!(hash)
    clean_hash = hash.extract(*FIELDS_USER_CAN_CHANGE)
    self.merge!(clean_hash)
  end

  def inspect
    "<User: #{@hash.inspect}>"
  end

  def to_hash
    @hash
  end

  def to_json(*a)
    @hash.extract(*FIELDS_USER_CAN_SEE).to_json(*a)
  end

  private

  def set_defaults!
    self.merge!({
                  'position' => 0,
                  'solved' => 0,
                  'month_score' => 0,
                  'high_score' => 0,
                  'dynamics' => 0,
                  'role' => 'user',
                  'hints' => 0
                })
  end
end

class FacebookUser < User

  def id
    raise InvalidState unless self['facebook_id']
    "facebook##{self['facebook_id']}"
  end

  def validate!
    super
    raise InvalidState.new("Missing required field: facebook_id") unless self['facebook_id']
    raise InvalidState.new("Missing required field: access_token") unless self['access_token']
    self
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
    case
    when @old_id && @old_id != self.id, !@old_id
      raise InvalidState.new("Email is already taken") if @storage.get(self.id)
    end
  end
end

class VkontakteUser < User

  def id
    raise InvalidState unless self['vkontakte_id']
    "vkontakte##{self['vkontakte_id']}"
  end

  def validate!
    super
    raise InvalidState.new("Missing required field: vkontakte_id") unless self['vkontakte_id']
    raise InvalidState.new("Missing required field: access_token") unless self['access_token']
  end
end
