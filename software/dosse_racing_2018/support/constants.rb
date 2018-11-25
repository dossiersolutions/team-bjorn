MONITOR_RESOLUTION = Vector[Gosu::screen_width * 1.0, Gosu::screen_height * 1.0]
SCREEN_CENTER = MONITOR_RESOLUTION / 2
UI_TEXT_HEIGHT = MONITOR_RESOLUTION.y / 16
UI_TEXT_TOP_LEFT    = Vector[UI_TEXT_HEIGHT * 0.5, UI_TEXT_HEIGHT / 2]
UI_TEXT_BOTTOM_LEFT = Vector[UI_TEXT_HEIGHT * 0.5, Gosu::screen_height - UI_TEXT_HEIGHT * 1.5]

module Assets # preload all game assets into global constants
  SUV        = Gosu::Image.new("assets/suv.png", :tileable => false)
  LOGO       = Gosu::Image.new("assets/logo_fixed.jpg")
  STARS      = Gosu::Image.new("assets/stars.jpg")
  THEME_SONG = Gosu::Song.new("assets/theme.ogg")
  JINGLE     = Gosu::Song.new("assets/jingle.ogg")
  UI_FONT    = Gosu::Font.new(UI_TEXT_HEIGHT.to_i, name: "assets/retganon.ttf")
end