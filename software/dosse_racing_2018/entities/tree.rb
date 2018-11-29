
class Tree
  def initialize(position)
    @position = position
  end

  attr_reader :position

  def update(dt, entities)
  end

  def draw(millis)
    draw_triangle(@position, 15, Gosu::Color::GREEN)
  end
end