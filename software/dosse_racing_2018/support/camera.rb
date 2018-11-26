class Camera
  def initialize
    @focus_of_attention = VIEWPORT_CENTER
    @position           = VIEWPORT_CENTER
  end

  attr_accessor :focus_of_attention
  attr_reader :position

  def update(dt, entities)
    motion = (@focus_of_attention - @position) * 0.02 * dt
    @position += motion
    # lerp to focus of attention
  end

  def draw(millis)
    draw_triangle(@focus_of_attention, 15, Gosu::Color::BLUE)
    draw_triangle(@position, 15, Gosu::Color::GREEN)
  end

  def apply
    Gosu.translate(*(@position * -1 + VIEWPORT_CENTER)) do
      yield
    end 
  end

  def view_top_left
    @position - VIEWPORT_CENTER
  end

  def view_bottom_right
    @position + VIEWPORT_CENTER
  end

  def within_viewport?(vector)
    distance_from_center = (vector - @position)
    (distance_from_center.x.abs < VIEWPORT_CENTER.x) && (distance_from_center.y.abs < VIEWPORT_CENTER.y)
  end
end