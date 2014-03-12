# config valid only for Capistrano 3.1
lock '3.1.0'

set :application, 'prizeword'
set :repo_url, 'git@github.com:aipmedia-org/prizeword-server.git'

# Default branch is :master
# ask :branch, proc { `git rev-parse --abbrev-ref HEAD`.chomp }
# set :branch, :capistrano

# Default deploy_to directory is /var/www/my_app
set :deploy_to, '/home/prizeword'

set :unicorn_env, 'production'
set :unicorn_config_path, "#{release_path}/unicorn.rb"
set :unicorn_pid, "/var/run/prizeword/unicorn.pid"
# Default value for :scm is :git
# set :scm, :git

# Default value for :format is :pretty
# set :format, :pretty

# Default value for :log_level is :debug
# set :log_level, :debug

# Default value for :pty is false
# set :pty, true

# Default value for :linked_files is []
set :linked_files, %w{config/app.yml config/newrelic.yml}

# Default value for linked_dirs is []
set :linked_dirs, %w{log tmp/pids}

# Default value for default_env is {}
# set :default_env, { path: "/opt/ruby/bin:$PATH" }

# Default value for keep_releases is 5
# set :keep_releases, 5

set :rbenv_type, :user # or :system, depends on your rbenv setup
set :rbenv_ruby, '1.9.3-p429'
set :rbenv_prefix, "RBENV_ROOT=#{fetch(:rbenv_path)} RBENV_VERSION=#{fetch(:rbenv_ruby)} #{fetch(:rbenv_path)}/bin/rbenv exec"
set :rbenv_map_bins, %w{rake gem bundle ruby rails}
set :rbenv_roles, :all # default value

namespace :deploy do

  desc 'Restart application'
  task :restart do
    on roles(:app), in: :sequence, wait: 5 do
      # Your restart mechanism here, for example:
      # execute :touch, release_path.join('tmp/restart.txt')
    end
  end

  after :publishing, :restart

  after :restart, :clear_cache do
    on roles(:web), in: :groups, limit: 3, wait: 10 do
      # Here we can do anything such as:
      # within release_path do
      #   execute :rake, 'cache:clear'
      # end
    end
  end

end
