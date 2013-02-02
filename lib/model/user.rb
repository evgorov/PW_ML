require 'bcrypt'
require 'json'
require 'ext/hash'
require 'model/basic_model'
require 'friends_fetcher'

class User < BasicModel

  REQUIRED_FIELDS_FOR_REGISTRATION = %w[email name surname password]
  FIELDS_USER_CAN_CHANGE = REQUIRED_FIELDS_FOR_REGISTRATION + %w[birthdate userpic city solved]
  FIELDS_USER_CAN_SEE = FIELDS_USER_CAN_CHANGE -
                        ['password'] +
                        %w[id position month_score high_score dynamics hints provider created_at]

  class << self

    def new(*a)
      case
      when self.name == 'User' && a.first.is_a?(Hash) && !a.first.empty?
        @@providers[a.first['provider']].new(*a)
      else
        super(*a)
      end
    end

    def inherited(subclass)
      @@providers ||= {}
      @@providers[subclass.new.provider_name] = subclass
    end

    def load_by_provider_and_key(storage, provider, key)
      @@providers[provider].storage(storage).load_by_key(key)
    end
  end

  def before_save
    super
    self['position'] = self['position'].to_i
    self['solved'] = self['solved'].to_i
    self['month_score'] = self['month_score'].to_i
    self['high_score'] = self['high_score'].to_i
    self['dynamics'] = self['dynamics'].to_i
    self['hints'] = self['hints'].to_i
  end

  def after_load
    super
    load_external_attributes
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
    %w[email provider name surname position solved role
       month_score high_score dynamics hints].each do |field|
      raise InvalidState.new("Missing required field: #{field}") unless self[field]
    end
  end

  def users_by_rating(page=1)
    self.collection_for_key('rating', page)
  end

  def load_by_key(key)
    self.load("#{self.provider_name}##{key}")
  end

  def save(*a)
    if self.class == User
      raise NotImplementedError.new("This class is an abstract class, "+
                                    "you must use it's children.")
    end
    self['provider'] = self.provider_name
    if self['month_score'].to_i > self['high_score'].to_i
      self['high_score'] = self['month_score']
    end
    super(*a)
    @storage.zadd('rating', self['month_score'].to_i, self.id)
    save_external_attributes
    load_external_attributes # get real position after object creation
    self
  end

  def delete(*a)
    # TODO: delete custom columns
    @storage.zrem('rating', self.id)
    super(*a)
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

  def fetch_friends
    self['friends'] ||= {}
    return self['friends'] unless %w[facebook vkontakte].include?(self['provider'])

    fetched_friends = FriendsFetcher.send("fetch_#{self['provider']}_friends", self['access_token'])
    Hash[fetched_friends.map { |h| [h['id'], h]}].each do |k, h|
      if self['friends'][k]
        self['friends'][k].merge!(h)
      else
        self['friends'][k] = h
      end
    end
    self['friends'].values.each { |h| h['invite_used'] = self.exist?("#{self['provider']}##{h['id']}") }
    self['friends'].values.each { |h| h['invite_sent'] = false unless h.has_key?('invite_sent') }
    self['friends'].values
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

  def load_external_attributes
    self['month_score'] = @storage.get("#{self['id']}#score##{current_period}").to_i
    current_position = @storage.zrevrank('rating', self['id']).to_i + 1
    @storage.set("#{self['id']}#position#{current_period}", current_position)
    # if user misses couple of monthes, it will get higher dynamics
    previous_position = @storage.get("#{self['id']}#position#{previous_position}")
    self['position'] = current_position || 9999
    self['dynamics'] = current_position.to_i - previous_position.to_i
  end

  def save_external_attributes
    @storage.set("#{self['id']}#score##{current_period}", self['month_score'])
  end

  def current_period
    "#{Time.now.year}##{Time.now.month}"
  end

  def previous_period
    time = Time.now - 60 * 60 * 24 * 31
    "#{time.year}##{time.month}"
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
    super
    raise InvalidState.new("Missing required field: vkontakte_id") unless self['vkontakte_id']
    raise InvalidState.new("Missing required field: access_token") unless self['access_token']
  end
end
