class Starfield < EntitySystem
  class Star
    SPAWN_ZONE = VIEWPORT_SIZE.y / 10

    def initialize(position = VIEWPORT_CENTER + Vector.random(-SPAWN_ZONE, +SPAWN_ZONE))
      @position = position
      @velocity = Vector[0,0]
      @force = (position - VIEWPORT_CENTER) * 0.0000001
      @age = 0
      @r = Gosu.random(0, 255)
      @g = Gosu.random(0, 255)
      @b = Gosu.random(0, 255)
    end

    def update(dt, birth_list, kill_list)
      @velocity += @force * dt
      @position += @velocity * dt
      @age      += dt

      if @position.outside_viewport?
        kill_list << self
      end
    end

    def draw(millis)
      draw_triangle(@position, 5, Gosu::Color::argb([millis * 0.002, 80].min, @r, @g, @b), 100, :add)
    end
  end

  def update(dt)
    super(dt)
    (0..1).each do
      @entities << Star.new
    end
  end
end