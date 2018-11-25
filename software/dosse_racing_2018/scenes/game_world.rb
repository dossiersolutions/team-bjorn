class Player
  def initialize
    @position = Vector[50, 50]
    @velocity = Vector[0, 0]
    @facing_angle = 0
  end

  def update(dt, birth_list, kill_list)
    @facing_angle += 2 if Gosu.button_down?(Gosu::KB_RIGHT)
    @facing_angle -= 2 if Gosu.button_down?(Gosu::KB_LEFT)

    @force = Vector[0, 0]
    @force += Vector.from_angle(@facing_angle, 0.00001)

    @velocity += @force * dt
    @position += @velocity * dt
  end

  def draw(millis)
    Assets::SUV.draw_rot(@position.x, @position.y, 1, @velocity.angle + 90)
    Assets::SUV.draw_rot(@position.x, @position.y, 1, @facing_angle + 90)
  end
end

class GameWorld < EntitySystem
  def initialize
    super
    @entities << Player.new
  end
end