# Dosse Racing 2018 by Team Bjorn

Dir.chdir(File.dirname(__FILE__)) # chdir to script in case run from elsewhere

require "set"
require "matrix" # vector math
require "bundler/inline"

gemfile do # this uses bundler to install external gems
  source "https://rubygems.org"
  gem "gosu", require: true # the game library
end

def nsin(theta) # normalized version
  Math::sin(theta) * 0.5 + 0.5
end

def ncos(theta) # normalized version
  Math::cos(theta) * 0.5 + 0.5
end

class Vector # add some more utilties to Ruby's default vector class
  def x; self[0]; end
  def y; self[1]; end
  def x=(v); self[0] = v; end
  def y=(v); self[1] = v; end

  def self.from_angle(angle, magnitude=1)
    self[Gosu.offset_x(angle, magnitude), Gosu.offset_y(angle, magnitude)]
  end

  def angle
    Math::atan2(y, x).radians_to_gosu
  end

  def mult_each(other)
    Vector[x*other.x, y*other.y]
  end

  def div_each(other)
    Vector[x/other.x, y/other.y]
  end
end

class Gosu::Image
  def dimensions
    Vector[width, height]
  end
end

MONITOR_RESOLUTION = Vector[Gosu::screen_width * 1.0, Gosu::screen_height * 1.0]

UI_FONT_HEIGHT = MONITOR_RESOLUTION.y / 16

module Assets # preload all game assets into global constants
  SUV        = Gosu::Image.new("assets/suv.png", :tileable => false)
  LOGO       = Gosu::Image.new("assets/logo.jpg")
  THEME_SONG = Gosu::Song.new("assets/theme.ogg")
  UI_FONT    = Gosu::Font.new(UI_FONT_HEIGHT.to_i, name: "assets/retganon.ttf")
end

class Player
  def initialize
    @position = Vector[50, 50]
    @velocity = Vector[0, 0]
    @facing_angle = 0
  end

  def update(dt)
    @facing_angle += 2 if Gosu.button_down?(Gosu::KB_RIGHT)
    @facing_angle -= 2 if Gosu.button_down?(Gosu::KB_LEFT)

    @force = Vector[0, 0]
    @force += Vector.from_angle(@facing_angle, 0.00001)

    @velocity += @force * dt
    @position += @velocity * dt
  end

  def draw
    Assets::SUV.draw_rot(@position.x, @position.y, 1, @velocity.angle + 90)
    Assets::SUV.draw_rot(@position.x, @position.y, 1, @facing_angle + 90)
  end
end

class IntroLogoCinematic
  def initialize
    Assets::THEME_SONG.play(true)
    @time = 0
  end

  def update(dt)
    @time += dt

    return GameWorld.new if Gosu.button_down?(Gosu::KB_SPACE)

    self
  end

  def draw
    logo = Assets::LOGO
    growth = Math.cos(@time * 0.0002) * 0.2 + 0.2
    scale = MONITOR_RESOLUTION.x / logo.width + growth
    screen_center = MONITOR_RESOLUTION / 2
    draw_pos = screen_center + (Vector.from_angle(@time*0.011) * 50)

    logo_color = Gosu::Color::argb(@time / 30, 255, 255, 255)

    logo.draw_rot(*draw_pos, 0, Math.sin(@time * 0.00004)*15, 0.5, 0.4, scale, scale, logo_color)

    font_pos = Vector[UI_FONT_HEIGHT * 0.5, Gosu::screen_height - UI_FONT_HEIGHT * 1.5]

    text_color = Gosu::Color.argb(nsin(@time * 0.005) * 255 , 255, 255, 255)

    Assets::UI_FONT.draw_text("Press space bar...", *font_pos,
      1, 1.0, 1.0, *text_color)
  end
end

class GameWorld
  def initialize
    @entities = Set[
      Player.new
    ]
  end

  def update(dt)
    @entities.each {|e| e.update(dt) }
    self
  end
  
  def draw
    @entities.each(&:draw)
  end
end

class Window < Gosu::Window
  def initialize
    super(Gosu::screen_width, Gosu::screen_height, true)
    self.caption = "DosseRacing 2018"
    @time = @old_time = Gosu::milliseconds()
    @scene = IntroLogoCinematic.new
    # @scene = GameWorld.new
  end
  
  def update
    @old_time = @time
    @time = Gosu::milliseconds()
    dt = @time - @old_time
    @scene = @scene.update(dt)

    close! if Gosu.button_down?(Gosu::KB_ESCAPE)
    close! if Gosu.button_down?(Gosu::KB_Q) # TODO
  end
  
  def draw
    @scene.draw()
  end
end

Window.new.show