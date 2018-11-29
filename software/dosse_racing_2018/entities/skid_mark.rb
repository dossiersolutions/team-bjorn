class Skidmark
  LIFESPAN = 300.0

  def initialize(position)
    @position = position
    @age = 0
  end

  def norm_age
    @age / LIFESPAN
  end

  def update(dt, entities)
    @age += dt
    if @age > LIFESPAN
      entities.kill(self)
    end
  end

  def draw(millis)
    draw_triangle(@position, 10, Gosu::Color::BLACK, 0)
  end
end