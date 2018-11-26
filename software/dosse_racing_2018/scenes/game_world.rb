class Player
  def initialize
    @position = VIEWPORT_CENTER
    @velocity = Vector[0, 0.0000001]
    @facing_angle = 0
    @controls = Controls.new
    @car_scale = UI_TEXT_HEIGHT * 1 / Assets::SUV.height
  end

  def update(dt, birth_list, kill_list)
    @controls.update(dt)

    turn_amount = [@velocity.magnitude * 0.5, 0.1].min * dt
    @facing_angle -= turn_amount if Gosu.button_down?(Gosu::KB_LEFT)
    @facing_angle += turn_amount if Gosu.button_down?(Gosu::KB_RIGHT)

    if @controls.button_gas?
      if @facing_angle > @controls.button_target_angle
        @facing_angle -= [turn_amount, @controls.button_target_angle - @facing_angle].max
      else
        @facing_angle += [turn_amount, @facing_angle - @controls.button_target_angle].max
      end
    end

    friction = @velocity.normalize * -0.0005

    @force = Vector[0, 0]
    @force += Vector.from_angle(@facing_angle, 0.001) if @controls.keyboard_gas? || @controls.button_gas?
    @force += friction

    @velocity += @force * dt
    @position += @velocity * dt
  end

  def draw(millis)
    # Assets::SUV.draw_rot(@position.x, @position.y, 1, @velocity.angle + 90, 0.5, 0.5 @car_scale, @car_scale)
    Assets::SUV.draw_rot(@position.x, @position.y, 1, @facing_angle + 90, 0.5, 0.5, @car_scale, @car_scale)

    if @controls.button_gas?
    end
    target_indicator_pos = @position + Vector.from_angle(@controls.button_target_angle, @car_scale * 100)
    draw_triangle(target_indicator_pos, 15, Gosu::Color::argb(1000 - @controls.time_since_last_button_use * 0.3, 0, 255, 100))
  end
end

class GameWorld < EntitySystem
  def initialize
    super
    @entities << Player.new
  end
end