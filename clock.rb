require 'clockwork'
require 'stalker'

include Clockwork
handler { |job| Stalker.enqueue(job) }

every 1.hour, 'update_positions'
