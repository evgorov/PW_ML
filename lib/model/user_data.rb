require 'json'
require 'ext/hash'
require 'model/basic_model'
require 'friends_fetcher'

class UserData < BasicModel

  use_guuid!

  REQUIRED_FIELDS_FOR_REGISTRATION = %w[email name surname password]
  FIELDS_USER_CAN_CHANGE = REQUIRED_FIELDS_FOR_REGISTRATION + %w[birthdate userpic city solved]
  FIELDS_USER_CAN_SEE = FIELDS_USER_CAN_CHANGE - ['password'] +
                        %w[id position month_score high_score dynamics hints created_at providers]

  def before_save
    super
    self['providers'] = %w[facebook vkontakte].map{ |o|
      {
        'provider_name' => o,
        'provider_id' => self["#{o}_id"],
        'provider_token' => self["#{o}_access_token"]
      } if self["#{o}_id"]
    }.compact
    self['position'] = self['position'].to_i
    self['solved'] = self['solved'].to_i
    self['month_score'] = self['month_score'].to_i
    self['high_score'] = self['high_score'].to_i
    self['dynamics'] = self['dynamics'].to_i
    self['hints'] = self['hints'].to_i
    self['high_score'] = self['month_score'] if self['month_score'] > self['high_score']
  end

  def after_save
    super
    @storage.zadd('rating', self['month_score'].to_i, self.id)
    save_external_attributes
    load_external_attributes # get real position after object creation
  end

  def after_load
    super
    load_external_attributes
  end

  def merge!(other)
    case(other)
    when UserData
      result = other.dup.merge(self.to_hash)

      # merge friends
      result.to_hash.keys.grep(/_friends/).each do |k|
        result[k] = other[k].merge!(self[k]) { |k, h1, h2| h1['status'] =~ /\Ainvite/ ? h1 : h2 }
      end

      # merge scores
      user1_scores = UserScore.storage(@storage).scores_for(self.id)
      user2_scores = UserScore.storage(@storage).scores_for(other.id)
      combined_scores = (user1_scores + user2_scores).group_by { |o| o['source'] }.values
      solved, score = combined_scores.inject([0,0]) do |(solved, score), (u1, u2)|
        if !u2 || u1['score'] > u2['score']
          [solved + u1['solved'], score + u1['score']]
        else
          [solved + u2['solved'], score + u2['score']]
        end
      end
      result['solved'], result['month_score'] = solved, score
      self.merge!(result.to_hash)
    when Hash
      # force storing 'password' field with []=
      other = other.dup
      if password = other.delete('password')
        self['password'] = password
      end
      super(other)
      self
    else
      raise ArgumentError
    end
  end

  def users_by_rating(page=1)
    self.collection_for_key('rating', page)
  end

  def delete(*a)
    # TODO: delete custom columns
    @storage.zrem('rating', self.id)
    super(*a)
  end

  def merge_fields_user_can_change!(hash)
    clean_hash = hash.extract(*FIELDS_USER_CAN_CHANGE)
    self.merge!(clean_hash)
  end

  def to_json(*a)
    @hash.extract(*FIELDS_USER_CAN_SEE).to_json(*a)
  end

  def fetch_friends(provider)
    friends = self["#{provider}_friends"] ||= {}
    fetched_friends = case provider
    when 'facebook'; FriendsFetcher.fetch_facebook_friends(self['facebook_access_token'])
    when 'vkontakte'; FriendsFetcher.fetch_vkontakte_friends(self['vkontakte_access_token'])
    else; raise ArgumentError
    end

    friends.merge!(Hash[fetched_friends.map { |h| [h['id'], h] }]) { |k, h1, h2| h1.merge(h2) }

    # state machine
    friends.values.each do |h|
      exist = User.storage(@storage).exist?("#{provider}##{h['id']}")
      case
      when  h['status'] == nil && exist; h['status'] = 'already_registered'
      when  h['status'] == nil; h['status'] = 'uninvited'
      when  h['status'] == 'uninvited' && exist; h['status'] = 'already_registered'
      when  h['status'] == 'invite_sent' && exist; h['status'] = 'invite_used'
      end
    end

    friends.values
  end

  def invite(provider, friend_id)
    friends = self["#{provider}_friends"]
    return false if !friends[friend_id] || friends[friend_id]['status'] != 'uninvited'
    friends[friend_id]['status'] = 'invite_sent'
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
    self['dynamics'] = if !previous_position
                         0
                       else
                         current_position.to_i - previous_position.to_i
                       end
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
