class ExhaustCloud
  LIFESPAN = 500.0

  def initialize(position, giving_gas=false)
    @position = position
    @age = 0
    @giving_gas = giving_gas
    @dark = @giving_gas && Gosu.random(0, 100) > 30
    @lightness = if @dark then Gosu.random(40, 100) else Gosu.random(120, 180) end
    @mode = if @dark then :multiply else :add end
    @image = if @dark then Assets::BLACK_SOFT else Assets::WHITE_SOFT end
  end

  def update(dt, entities)
    @age += dt
    @position.y -= dt * 0.1
    if @age > LIFESPAN
      entities.kill(self)
    end
  end

  def norm_age
    @age / LIFESPAN
  end

  def scale
    0.2 + norm_age * 0.1
  end

  def draw(millis)
    opacity = [norm_age * 400, (1-norm_age) * 400, 80].min
    # color = Gosu::Color::argb(, @lightness, @lightness, @lightness)
    scale = norm_age * (if @giving_gas then 20 else 15 end) * 0.1
    @image.draw(*@position, 30, scale, scale, Gosu::Color::argb(opacity, 255, 255, 255))
    # draw_triangle(@position, norm_age * (if @giving_gas then 20 else 15 end), color, 30, @mode)
  end
end
