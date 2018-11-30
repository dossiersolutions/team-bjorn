
class Tree
  SCALE = 4.0

  def initialize(position)
    @position = position
    scaled_dim = Assets::PINE.dimensions * SCALE
    @hitbox = [@position + Vector[0, scaled_dim.y * 0.5], @position + scaled_dim]
  end

  attr_reader :position, :hitbox

  def update(dt, entities)
  end

  def draw(millis)
    # draw_triangle(@position, 15, Gosu::Color::GREEN)
    color = Gosu::Color::rgb(255, 255, 255)
    Assets::PINE.draw(*@position, 100, SCALE, SCALE, color)
  end
end