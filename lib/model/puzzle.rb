require 'json'
require 'time'
require 'securerandom'
require 'ext/hash'
require 'model/basic_model'

class Puzzle < BasicModel
  use_guuid!
end
