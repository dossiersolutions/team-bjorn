Dir.chdir(File.dirname(__FILE__)) # chdir to script in case run from elsewhere

require "bundler/inline"

gemfile do # this uses bundler to install external gems
  source "https://rubygems.org"
  gem "net-ssh", require: "net/ssh"
end

puts "Deploying to tinkerboard..."
puts ""
puts "You can pass a git refspec as a parameter to this script to deploy a specific version."
puts ""
puts "[DEPLOY] Pushing changes..."
puts ""

system("git push")

puts ""
puts "[DEPLOY] Connecting via SSH to pull changes on server..."
puts ""

Net::SSH.start('192.168.29.205', 'root', :password => "dietpi") do |ssh|
  # capture all stderr and stdout output from a remote process
  puts ssh.exec!("cd /opt/team-bjorn && git pull #{ARGV.join(" ")}")
end

puts ""
puts "[DONE]"