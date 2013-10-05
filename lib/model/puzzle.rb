require 'json'
require 'time'
require 'securerandom'
require 'ext/hash'
require 'model/basic_model'

class Puzzle < BasicModel
  PER_PAGE = 20
  use_guuid!
end
