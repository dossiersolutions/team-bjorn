class Camera
  def initialize
    @focus_of_attention = VIEWPORT_CENTER
    @zoom_target        = 1.0
    @position           = VIEWPORT_CENTER
    @zoom               = 1.0
  end

  attr_accessor :focus_of_attention, :zoom, :zoom_target
  attr_reader :position

  def update(dt, entities)
    motion = (@focus_of_attention - @position) * 0.005 * dt
    @position += motion
    zoom_motion = (@zoom_target - @zoom) * 0.005 * dt
    @zoom += zoom_motion
    # lerp to focus of attention
  end

  def draw(millis)
    # draw_triangle(@focus_of_attention, 15, Gosu::Color::BLUE)
    # draw_triangle(@position, 15, Gosu::Color::GREEN)
  end

  def apply
    Gosu.translate(*VIEWPORT_CENTER) do
      Gosu.scale(@zoom * WORLD_ZOOM_VIEWPORT_ADJUSTMENT) do
        Gosu.translate(*(-@position)) do
          yield
        end
      end 
    end
  end

  def view_top_left
    @position - VIEWPORT_CENTER / (@zoom * WORLD_ZOOM_VIEWPORT_ADJUSTMENT)
  end

  def view_bottom_right
    @position + VIEWPORT_CENTER / (@zoom * WORLD_ZOOM_VIEWPORT_ADJUSTMENT)
  end

  def view_size_in_world
    VIEWPORT_SIZE / (@zoom * WORLD_ZOOM_VIEWPORT_ADJUSTMENT)
  end

  def within_viewport?(vector)
    distance_from_center = (vector - @position)
    (distance_from_center.x.abs < VIEWPORT_CENTER.x) && (distance_from_center.y.abs < VIEWPORT_CENTER.y)
  end
end