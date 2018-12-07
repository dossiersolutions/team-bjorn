Dir["./entities/**/*.rb"].each {|file| require file }

PLAYER = Player.new
HANSES  = Enemy.new("hanses", Gosu::Color::argb(150, 255, 255, 0))
ANAAAM  = Enemy.new("anaaam", Gosu::Color::argb(150, 255, 0, 255))
CAMERA = Camera.new
WORLDGEN = WorldGen.new
# SPATIAL_INDEX = SpatialIndex.new

COMPETITORS = [
  PLAYER,
  HANSES,
  ANAAAM
]

def reset_world
  PLAYER.send :initialize
  HANSES.send :initialize, "hanses", Gosu::Color::argb(150, 255, 255, 0)
  ANAAAM.send :initialize, "anaaam", Gosu::Color::argb(150, 255, 0, 255)
  WORLD_STATE.clear
end

class GameWorld < EntitySystem
  def initialize
    super
    @entities << PLAYER
    @entities << HANSES
    @entities << ANAAAM
    @entities << CAMERA
    @entities << WORLDGEN
    @entities << PathRecorder.new if DEV_MODE
  end

  def draw
    ground_color = Gosu::Color::rgb(20, 70, 30)
    Gosu::draw_quad(
      0,                   0,                     ground_color,
      Gosu::screen_width,  0,                     ground_color,
      0,                   Gosu::screen_height,   ground_color,
      Gosu::screen_width,  Gosu::screen_height,   ground_color,
      -10000
    )
    CAMERA.apply do
      super

      pine_adjusted_position = PLAYER.position + Vector[Assets::PINE.width * 4 * 0.5, -Assets::PINE.height * 4]

      # SPATIAL_INDEX.each_neighbor(pine_adjusted_position, 60.0) do |entity|
      #   fail "YOU DEAD. HIT BY TREE."
      # end
    end

    Assets::UI_FONT.draw_text("#{DATA[:player_kph].to_i} kph #{(DATA[:race_time] / 1000).to_i} secs", *UI_TEXT_TOP_LEFT, 10000, 1.0, 1.0, Gosu::Color::argb(100, 255, 255, 255))

    if PLAYER.race_completed
      COMPETITORS.each_with_index do |competitor, i|
        Assets::UI_FONT.draw_text(i.to_s + ". " + competitor.name, 0, VIEWPORT_SIZE.y * 0.2 + i * UI_TEXT_HEIGHT, 10000, 1.0, 1.0, Gosu::Color::argb(255, 255, 100, 0))
      end
    else
      if DATA[:big_text]
        Assets::UI_FONT.draw_text(DATA[:big_text], *VIEWPORT_CENTER_LEFT, 10000, 1.0, 1.0, Gosu::Color::argb(255, 255, 100, 0))
      elsif DATA[:off_road]
        Assets::UI_FONT.draw_text("Missing checkpoint, get back on track now!", *VIEWPORT_CENTER_LEFT, 10000, 0.7, 0.7, Gosu::Color::argb(255, 255, 0, 0))
      end
    end

    draw_progress(PLAYER, "Player")
    draw_progress(HANSES, "Hanses Oddvindsen", Assets::HANSES)
    draw_progress(ANAAAM, "Anaaam Jabars Abdul Nabi")
  end

  def draw_progress(enemy, name, portrait=nil)
    position = Vector[UI_TEXT_HEIGHT * 0.5, VIEWPORT_SIZE.y * (1 - enemy.progress)]
    Assets::UI_FONT.draw_text(name, UI_TEXT_HEIGHT, VIEWPORT_SIZE.y * (1 - enemy.progress), 10000, 0.5, 0.5, enemy.color)

    if !portrait.nil?
      scale = 40.0 / [portrait.width, portrait.height].max
      portrait.draw(*position, 100, scale, scale, Gosu::Color::WHITE)
    else
      draw_triangle(position, 30, enemy.color)
    end

  end
end