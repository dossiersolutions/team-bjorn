Dir["./entities/**/*.rb"].each {|file| require file }

PLAYER = Player.new
CAMERA = Camera.new
WORLDGEN = WorldGen.new
# SPATIAL_INDEX = SpatialIndex.new

class GameWorld < EntitySystem
  def initialize
    super
    @entities << PLAYER
    @entities << CAMERA
    @entities << WORLDGEN
    @entities << PathRecorder.new if DEV_MODE

    Assets::ROAD.map do |pos|
      segment = RoadSegment.new(pos)
      @entities << segment
      segment
    end.each_cons(2) do |a, b|
      a.next = b
    end
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

    Assets::UI_FONT.draw_text("#{DATA[:player_kph].to_i} kph", *UI_TEXT_TOP_LEFT, 10000, 1.0, 1.0, Gosu::Color::argb(100, 255, 255, 255))

    if DATA[:big_text]
      Assets::UI_FONT.draw_text(DATA[:big_text], *VIEWPORT_CENTER_LEFT, 10000, 1.0, 1.0, Gosu::Color::argb(255, 255, 100, 0))
    end
  end
end