libdir = File.join(File.dirname(__FILE__), 'lib')
$LOAD_PATH.unshift(libdir) unless $LOAD_PATH.include?(libdir)
include Stalker

require 'storage'
require 'model/user_data'
require 'model/dictionary'
require 'model/puzzle'
require 'timeout'
require 'logger'

$logger = Logger.new('log/worker.log')
def log(message)
  puts(message)
  $logger.info(message)
end

def generate_formula
  log('generate_formula')
  fd = IO.popen(%w[generator/generate-formula/generate_formula 14 20 3 8 50 160 1000 3:14])
  begin
    Timeout::timeout(60) do
      Process.wait(fd.pid)
    end
    log('succesfully_generated_formula')
    fd.read
  rescue Timeout::Error => e
    Process.kill('SIGTERM', fd.pid)
    fd.close
    log('error_generated_formula')
    false
  end
end

def fill_formula(formula, dict)
  log('fill_formula')
  fd = IO.popen('generator/fill-formula/fill_puzzle', 'r+')
  fd.write(formula)
  fd.write(dict.encode('WINDOWS-1251', 'UTF-8'))
  fd.close_write
  begin
    Timeout::timeout(120) do
      Process.wait(fd.pid)
    end
    log('succesfully_filled_formula')
    fd.read.encode('UTF-8', 'WINDOWS-1251')
  rescue Timeout::Error => e
    Process.kill('SIGTERM', fd.pid)
    fd.close
    log('error_fill_formula')
    false
  end
end

def parse_question_line(question_line)
  x, y, _, position, direction, answer = question_line.split(',')
  direction = 'bottom' if direction == 'down'
  {
    'column' => x.to_i + 1,
    'row' => y.to_i + 1,
    'answer_position' => "#{position}:#{direction}",
    'question_text' => '.',
    'answer' => answer
  }
end

def parse_result(_input)
  input = _input.lines.to_a.map(&:chomp)
  height, width = input.shift.split(',').map(&:to_i)
  number_of_questions = input.shift.to_i
  questions = input[0..(number_of_questions-1)].map do |l|
    parse_question_line(l)
  end
  {
    'base_score' => 100,
    'height' => height,
    'width' => width,
    'name' => 'hail to the robots!',
    'questions' => questions,
    'time_given' => 100,
    'author' => 'paranoid android',
  }
end

def generate_puzzle_json(dict)
  formula = generate_formula
  return generate_puzzle_json(dict) unless formula
  puzzle = fill_formula(formula, dict)
  return generate_puzzle_json(dict) unless puzzle
  parse_result(puzzle)
end

job 'update_positions' do
  UserData.storage(Redis.new).all_in_batches do |user_data|
    user_data['dynamics'] = user_data['previous_position'].to_i <=> user_data['position'].to_i
    user_data['previous_position'] = user_data['position']
    user_data.save
  end
end

job 'generate_puzzle' do |params|
  dictionary = Dictionary.storage(Redis.new).load(params['dictionary'])
  puzzle_json = generate_puzzle_json(dictionary['body'])
  puzzle = Puzzle.storage(Redis.new).merge!(puzzle_json).save
end
