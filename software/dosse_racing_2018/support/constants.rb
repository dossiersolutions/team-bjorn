MONITOR_RESOLUTION = Vector[Gosu::screen_width * 1.0, Gosu::screen_height * 1.0]
SHORT_CIRCUIT = true
DEV_MODE = true
VIEWPORT_SIZE = MONITOR_RESOLUTION
FULLSCREEN = true
# VIEWPORT_SIZE = if SHORT_CIRCUIT then MONITOR_RESOLUTION * 0.5 else MONITOR_RESOLUTION end
# FULLSCREEN = !SHORT_CIRCUIT
UI_TEXT_HEIGHT = VIEWPORT_SIZE.y / 16
VIEWPORT_CENTER = VIEWPORT_SIZE / 2
VIEWPORT_CENTER_LEFT = Vector[UI_TEXT_HEIGHT, VIEWPORT_SIZE.y * 0.5]
WORLD_ZOOM_VIEWPORT_ADJUSTMENT = VIEWPORT_SIZE.y / 650
UI_TEXT_TOP_LEFT = Vector[UI_TEXT_HEIGHT * 0.5, UI_TEXT_HEIGHT / 2]
UI_TEXT_BOTTOM_LEFT = Vector[UI_TEXT_HEIGHT * 0.5, Gosu::screen_height - UI_TEXT_HEIGHT * 1.5]
SEED = 3
PERLIN_FORESTNESS = Perlin::Generator.new SEED, 1.0, 5

DATA = { # used by entities etc to communicate

}

module Assets # preload all game assets into global constants
  SUV        = Gosu::Image.new("assets/suv.png", :tileable => false)
  LOGO       = Gosu::Image.new("assets/logo_fixed.jpg")
  STARS      = Gosu::Image.new("assets/stars.jpg")
  PINE       = Gosu::Image.new("assets/pine.png")
  WHITE_SOFT = Gosu::Image.new("assets/white_soft_particle.png")
  BLACK_SOFT = Gosu::Image.new("assets/black_soft_particle.png")
  FLOWERS    = Gosu::Image.load_tiles("assets/flowers_and_stuff.png", 32, 32, retro: true)
  THEME_SONG = Gosu::Song.new("assets/theme.ogg")
  JINGLE     = Gosu::Song.new("assets/jingle.ogg")
  UI_FONT    = Gosu::Font.new(UI_TEXT_HEIGHT.to_i, name: "assets/retganon.ttf")

  ROAD       = Marshal::load(File.read("road.path"))
end

srand 1234

