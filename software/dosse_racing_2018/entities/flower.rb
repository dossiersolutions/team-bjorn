class Flower
  SCALE = 1.0

  def initialize(position)
    @position = position
    @image = Assets::FLOWERS[Random.rand(51)]
  end

  attr_reader :position

  def update(dt, entities)
  end

  def draw(millis)
    # draw_triangle(@position, 15, Gosu::Color::GREEN)
    color = Gosu::Color::WHITE
    @image.draw(*@position, 1, SCALE, SCALE, color)
  end
end