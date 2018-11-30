class RoadSegment
  def initialize(position)
    @position = Vector[*position]
  end

  attr_accessor :next

  def update(dt, entities)
  end

  def draw(millis)
    draw_triangle(@position, 15, Gosu::Color::BLUE)
  end
end