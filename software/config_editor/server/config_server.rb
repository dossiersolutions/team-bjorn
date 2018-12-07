# require "bundler/inline"

require "json"
require "sinatra"

# gemfile do # this uses bundler to install external gems
#   source "https://rubygems.org"
#   gem "sinatra", require: true
# end

set :bind, '0.0.0.0'
# set :port, 4567

before do
  headers "Access-Control-Allow-Origin" => "*"
end

FILE_NAME = ARGV[0]

put "/config" do
  config = JSON.parse(request.body.read)
  File.write FILE_NAME, config.to_json
  200
end

get "/config" do
  content_type :json
  File.read FILE_NAME
end