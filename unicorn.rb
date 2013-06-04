APP_ROOT = File.expand_path(File.dirname(File.dirname(__FILE__)))

worker_processes 4
working_directory APP_ROOT

preload_app true

timeout 30

listen "/tmp/unicorn.sock", :backlog => 64

pid APP_ROOT + "/tmp/pids/unicorn.pid"

stderr_path APP_ROOT + "/log/unicorn.stderr.log"
stdout_path APP_ROOT + "/log/unicorn.stdout.log"
