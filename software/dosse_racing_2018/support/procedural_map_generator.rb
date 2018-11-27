TILE_WIDTH = 1024
TILE_DIMENSIONS = Vector[TILE_WIDTH, TILE_WIDTH]
SAFETY_MARGIN = 1.5

class MapTile
  def initialize(topleft)
    @topleft = Vector[*topleft]
  end

  attr_reader :topleft
  attr_accessor :last_access

  def draw
    rng = Random.new(@topleft.hash)
    # draw_triangle(top_left, 40, 
    green = rng.rand * 80
    red = rng.rand * green
    blue = rng.rand * red
    color = Gosu::Color::argb(100, red, green, blue)
    pos = @topleft + TILE_DIMENSIONS * rng.rand
    Assets::WHITE_SOFT.draw(*pos, -100, 30.0, 30.0, color, :add)

    green = rng.rand * 80
    red = rng.rand * green
    blue = rng.rand * red
    color = Gosu::Color::argb(50, red, green, blue)
    pos = @topleft + TILE_DIMENSIONS * rng.rand
    Assets::WHITE_SOFT.draw(*pos, -100, 15.0, 15.0, color, :add)

    pos = @topleft + TILE_DIMENSIONS * rng.rand
    opacity = rng.rand * 80
    Assets::BLACK_SOFT.draw(*pos, -99, 30.0, 30.0, Gosu::Color::argb(opacity, 255, 255, 255))
  end
end

class ProceduralMapGenerator
  def initialize
    @tiles = {}
    @time = 0
    @active_tile_count = 0
  end

  def update(dt, entities)
    top_left = CAMERA.view_top_left - CAMERA.view_size_in_world * SAFETY_MARGIN
    bottom_right = CAMERA.view_bottom_right + CAMERA.view_size_in_world * SAFETY_MARGIN

    @first_tile_top_left = (top_left.map(&:to_i)     / TILE_WIDTH.to_i) * TILE_WIDTH * 1.0
    @last_tile_top_left  = (bottom_right.map(&:to_i) / TILE_WIDTH.to_i) * TILE_WIDTH * 1.0

    @time += dt

    # delete tiles not accessed during the last second
    @tiles.delete_if do |key, value|
      old = @time - value.last_access > 1000
      @active_tile_count -= 1 if old
      old
    end

    DATA[:active_tiles] = @active_tile_count

    # puts @first_tile_top_left
    # puts @last_tile_top_left
  end

  def draw(millis)
    # top_left = CAMERA.view_top_left
    bottom_right = CAMERA.view_bottom_right

    return if !@first_tile_top_left

    current_tile = Vector[*@first_tile_top_left]

    # n = 0

    # test viewport bunds computation
    # draw_triangle(CAMERA.view_top_left, 50, Gosu::Color::RED)
    # draw_triangle(CAMERA.view_bottom_right - Vector[100, 100], 50, Gosu::Color::RED)

    while current_tile.y < bottom_right.y
      while current_tile.x < bottom_right.x
        current_tile.x += TILE_WIDTH

        if !@tiles[current_tile.to_a]
          @tiles[current_tile.to_a] = MapTile.new(current_tile)
          @active_tile_count += 1
        end

        tile = @tiles[current_tile.to_a]
        tile.last_access = @time
        tile.draw
        # n += 1
      end
      current_tile.x = @first_tile_top_left.x
      current_tile.y += TILE_WIDTH
    end
    # puts "drew " + n.to_s
  end
end