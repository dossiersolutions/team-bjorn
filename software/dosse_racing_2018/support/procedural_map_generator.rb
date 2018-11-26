class ProceduralMapGenerator
  TILE_WIDTH = 1024

  def initialize
  end

  def update(dt, entities)
    top_left = CAMERA.view_top_left
    bottom_right = CAMERA.view_bottom_right

    @first_tile_top_left = (top_left.map(&:to_i)     / TILE_WIDTH.to_i) * TILE_WIDTH * 1.0
    @last_tile_top_left  = (bottom_right.map(&:to_i) / TILE_WIDTH.to_i) * TILE_WIDTH * 1.0

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
        draw_triangle(current_tile, 40, Gosu::Color::GREEN)
        # n += 1
      end
      current_tile.x = @first_tile_top_left.x
      current_tile.y += TILE_WIDTH
    end
    # puts "drew " + n.to_s
  end
end