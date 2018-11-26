class ProceduralMapGenerator
  TILE_WIDTH = UI_TEXT_HEIGHT * 8

  def initialize
  end

  def update(dt, entities)
    top_left = CAMERA.view_top_left
    bottom_right = CAMERA.view_bottom_right

    @first_tile_top_left = (top_left.map(&:to_i)     / TILE_WIDTH.to_i) * 1.0
    @last_tile_top_left  = (bottom_right.map(&:to_i) / TILE_WIDTH.to_i) * 1.0

    # puts @first_tile_top_left
    # puts @last_tile_top_left
  end

  def draw(millis)
    top_left = CAMERA.view_top_left
    bottom_right = CAMERA.view_bottom_right

    current_tile = @first_tile_top_left

    while current_tile.y < bottom_right.y
      while current_tile.x < bottom_right.x
        current_tile.x += TILE_WIDTH
        draw_triangle(current_tile, 15, Gosu::Color::GRAY)
      end
      current_tile.x = 0
      current_tile.y += TILE_WIDTH
    end
  end
end