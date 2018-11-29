
TILE_WIDTH = 1024
TILE_DIMENSIONS = Vector[TILE_WIDTH, TILE_WIDTH]
SAFETY_MARGIN = 1.5


class WorldGen
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
      if old
        value.teardown(entities)
        entities.kill(value)
      end
      old
    end

    DATA[:active_tiles] = @active_tile_count

    # puts @first_tile_top_left
    # puts @last_tile_top_left

    current_tile = Vector[*@first_tile_top_left]

    # n = 0

    # test viewport bunds computation
    # draw_triangle(CAMERA.view_top_left, 50, Gosu::Color::RED)
    # draw_triangle(CAMERA.view_bottom_right - Vector[100, 100], 50, Gosu::Color::RED)

    while current_tile.y < bottom_right.y
      while current_tile.x < bottom_right.x
        if !@tiles[current_tile.to_a]
          @tiles[current_tile.to_a] = tile = MapTile.new(current_tile, entities)
          entities.add(tile)
          @active_tile_count += 1
        end

        tile = @tiles[current_tile.to_a]
        tile.last_access = @time
        # n += 1

        current_tile.x += TILE_WIDTH
      end
      current_tile.x = @first_tile_top_left.x
      current_tile.y += TILE_WIDTH
    end
  end

  def draw(millis)
    # top_left = CAMERA.view_top_left
    bottom_right = CAMERA.view_bottom_right

    return if !@first_tile_top_left

    # puts "drew " + n.to_s
  end
end