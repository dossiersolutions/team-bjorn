class Starfield < EntitySystem
  class Star
    SPAWN_ZONE = MONITOR_RESOLUTION.y / 10

    def initialize(position = SCREEN_CENTER + Vector.random(-SPAWN_ZONE, +SPAWN_ZONE))
      @position = position
      @velocity = Vector[0,0]
      @force = (position - SCREEN_CENTER) * 0.0000001
      @age = 0
    end

    def update(dt, birth_list, kill_list)
      @velocity += @force * dt
      @position += @velocity * dt
      @age      += dt

      if @position.outside_screen?
        kill_list << self
      end
    end

    def draw(millis)
      draw_triangle(@position, 5, Gosu::Color::argb([millis * 0.002, 80].min, 255, 255, 255))
    end
  end

  def update(dt)
    super(dt)
    (0..1).each do
      @entities << Star.new
    end
  end
end