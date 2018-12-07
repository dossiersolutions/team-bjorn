# Not used by game. Called from command line to convert files.

require "zlib"
require "json"

path = ARGV[0]

def deserialize(str)
  JSON.parse(Zlib::inflate(str))
end


data = JSON.parse(File.read(path))

compress(path, data.to_json)
