# Dosse Racing 2018 by Team Bjorn

Dir.chdir(File.dirname(__FILE__)) # chdir to script in case run from elsewhere

puts "Dosse Racing 2018"
puts "Loading..."
puts
puts "Loading can take some time the first time you run the game, because we have to install some Ruby packages."

require "set"
require "matrix" # vector math
require "bundler/inline"

gemfile do # this uses bundler to install external gems
  source "https://rubygems.org"
  gem "gosu", require: true # the game library
  gem "perlin", require: true
end

require "./support/math"
require "./support/constants"
require "./support/camera"
require "./support/procedural_map_generator.rb"
require "./support/draw"
require "./support/entity_system"
require "./support/controls"

CAMERA = Camera.new
PROCEDURAL_MAP_GENERATOR = ProceduralMapGenerator.new

require "./scenes/logo"
require "./scenes/intro"
require "./scenes/game_world"

require "./support/window"

window = nil

if SHORT_CIRCUIT
  # window = Window.new(IntroScene.new)
  window = Window.new(GameWorld.new)
else
  window = Window.new()
end

window.show