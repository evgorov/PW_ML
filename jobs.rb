libdir = File.join(File.dirname(__FILE__), 'lib')
$LOAD_PATH.unshift(libdir) unless $LOAD_PATH.include?(libdir)
include Stalker

require 'storage'
require 'model/user_data'

job 'update_positions' do
  UserData.storage(Redis.new).all_in_batches do |user_data|
    user_data['dynamics'] = user_data['previous_position'].to_i <=> user_data['position'].to_i
    user_data['previous_position'] = user_data['position']
    user_data.save
  end
end
