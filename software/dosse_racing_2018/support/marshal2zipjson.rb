# Not used by game. Called from command line to convert old path files to new format.

require "zlib"
require "json"

data = Marshal::load(ARGF.read)

STDOUT.write(Zlib::deflate((data.to_json)))