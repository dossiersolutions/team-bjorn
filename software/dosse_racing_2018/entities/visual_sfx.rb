class VisualSfx
  def initialize(position, text)
    @position = position
    @text = text
    @color = Gosu::Color::BLACK
    @age = 0
    @motion = Vector.random(1)
  end

  def update(dt, entities)
    @age += dt
    if @age > 1000
      entities.kill self
    end
  end

  def draw(millis)
    Assets::UI_FONT.draw_text(@text, *(@position + @motion * @age), 10000, 0.5, 0.5, @color)
  end
end