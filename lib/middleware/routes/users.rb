#encoding: utf-8

require 'time'
require 'ext/hash'
require 'sinatra/base'
require 'user_factory'
require 'model/user'
require 'model/user_data'
require 'model/user_score'
require 'wall_publisher'

module Middleware
  class Users < Sinatra::Base

    class CoefficientNotExist < Exception;  end

    helpers do
      def current_user
        @current_user ||= env['token_auth'].user.user_data
      end
    end

    before do
      content_type 'application/json'
    end

    error(BasicModel::NotFound) { halt(403, { message: 'Не возможно найти модель' }.to_json) }

    get '/me' do
      env['token_auth'].authorize!
      { me: current_user }.to_json
    end

    # FIXME: This really should be PUT '/me'
    post '/me' do
      env['token_auth'].authorize!
      user = env['token_auth'].user
      params['userpic'] = env['Uploader.uploaded_files']['userpic']

      user_data = user.user_data
      user_data.merge_fields_user_can_change!(params)
      user_data.save

      # We need to update user as it's email can change
      if params['email']
        user['email'] = params['email']
        user.save
        env['token_auth'].update_user(user)
      end

      { me: user_data }.to_json
    end

    post '/score_set_share' do
      env['token_auth'].authorize!
      halt(403, { 'message' => 'missing social_network'}.to_json) unless params['social_network']
      halt(403, { 'message' => 'missing set_id'}.to_json) unless params['set_id']
      type = PuzzleSet.storage(env['redis']).load(params['set_id'])['type']
      type = 'silver1' if type == 'silver'
      score = Coefficients.storage(env['redis']).coefficients["shared-#{type}-score"]

      raise CoefficientNotExist unless score
      user = current_user

      user["shared_#{type}_score"] += score

      case params['social_network']
      when 'facebook'
        user['count_fb_shared'] += 1
      when 'vkontakte'
        user['count_vk_shared'] += 1
      else
        halt(403, { 'message' => 'unknown social network'}.to_json)
      end

      source = "user_share##{params['social_network']}##{params['set_id']}"
      begin
        UserScore.storage(env['redis']).create(user.id, score.to_i, 0, source)
      rescue BasicModel::AlreadyExist
        return { me: user }.to_json
      end

      user.inc_month_score(score)
      user.save
      { me: user }.to_json
    end


    post '/score_app_rate' do
      env['token_auth'].authorize!
      score = Coefficients.storage(env['redis']).coefficients['user-rated-score']
      raise CoefficientNotExist unless score
      user = current_user
      return { me: user }.to_json if user['is_app_rated'] == true

      source = "user_app_rate"
      begin
        UserScore.storage(env['redis']).create(user.id, score.to_i, 0, source)
      rescue BasicModel::AlreadyExist
        return { me: user }.to_json
      end

      user.inc_month_score(score)
      user['is_app_rated'] = true
      user['rate_date'] = Time.now
      user.save

      { me: user }.to_json
    end

    post '/score' do
      env['token_auth'].authorize!
      halt(403, { 'message' => 'missing source'}.to_json) unless params['source']
      score, solved, source = params['score'].to_i, params['solved'].to_i, params['source']

      user = current_user

      begin
        UserScore.storage(env['redis']).create(user.id, score, solved, source)
      rescue BasicModel::AlreadyExist
        return { me: user }.to_json
      end

      user.inc_month_score(score)
      user['solved'] += solved
      user.save

      { me: user }.to_json
    end

    post '/hints' do
      env['token_auth'].authorize!
      user = current_user
      user['hints'] += params['hints_change'].to_i
      user.save
      { me: user }.to_json
    end

    get '/:provider/friends' do
      env['token_auth'].authorize!
      current_user.fetch_friends(params['provider'])
      current_user.save
      current_user["#{params['provider'].to_s}_friends"].values.to_json
    end

    post '/:provider/invite' do
      halt(403, { 'message' => 'missing ids' }.to_json) unless params['ids']
      env['token_auth'].authorize!
      current_user.fetch_friends(params['provider'])
      params['ids'].split(',').each do |id|
        current_user.invite(params['provider'], id)
        if params['provider'] == 'vkontakte'
          WallPublisher.post_other(current_user['vkontakte_access_token'], id, 'join me to play prizeword', nil)
        end
      end
      current_user.save
      { "message" => "ok" }.to_json
    end

    get '/:provider/invited_friends_this_month' do
      env['token_auth'].authorize!
      current_user["#{params['provider']}_friends"].values.select{ |o|
        o['invited_at'] &&
        Time.parse(o['invited_at']).month == Time.now.month &&
        Time.parse(o['invited_at']).year == Time.now.year
      }.to_json
    end

    post '/vkontakte/share' do
      unless token = params['access_token']
        env['token_auth'].authorize!
        token = current_user['access_token']
      end

      message = params['message'] || 'Приглашаю тебя поиграть в PrizeWord – увлекательную и полезную игру! Разгадывай сканворды, участвуй в рейтинге, побеждай!'
      WallPublisher.post(token, message, params['attachmments']).to_json
    end

    post '/link_accounts' do
      user1 = env['token_auth'].get_user_by_session_key(params['session_key1']) rescue nil
      user2 = env['token_auth'].get_user_by_session_key(params['session_key2']) rescue nil
      halt(403, { 'message' => 'cannot authorize session_key1'}.to_json) unless user1
      halt(403, { 'message' => 'cannot authorize session_key2'}.to_json) unless user2
      user_data1, user_data2 = user1.user_data, user2.user_data

      providers1 = user_data1['providers'].map { |o| o['provider_name'] }
      providers2 = user_data2['providers'].map { |o| o['provider_name'] }

      unless (providers = (providers1 & providers2)).empty?
        halt(403, { 'message' => "Ошибка! Этот аккаунт уже привязан к другой учетной записи" }.to_json)
      end

      user_data1.merge!(user_data2)
      user_data1.save
      user_data2.delete

      user2['user_data_id'] = user_data1.id
      user2.save

      { "message" => "ok" }.to_json
    end
  end
end
