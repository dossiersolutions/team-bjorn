class Player
  def initialize
    @position = VIEWPORT_CENTER
    @velocity = Vector[0, 0.0000001]
    @facing_angle = 0
    @controls = Controls.new
    @car_scale = 1
    @exhaust_time_consumed = 0
    @time = 0
  end

  attr_reader :position

  def update(dt, entities)
    @controls.update(dt)
    @time += dt

    # steering

    turn_amount = [@velocity.magnitude * 0.5, 0.1].min * dt
    @facing_angle -= turn_amount if Gosu.button_down?(Gosu::KB_LEFT)
    @facing_angle += turn_amount if Gosu.button_down?(Gosu::KB_RIGHT)

    if @controls.button_gas? # turn towards pot controlled target
      degrees_difference = (@controls.button_target_angle - @facing_angle) % 360
      if degrees_difference > 180
        @facing_angle -= turn_amount if degrees_difference - turn_amount > 180
      else
        @facing_angle += turn_amount if degrees_difference + turn_amount < 180
      end
    end

    # steering when not sliding
    # if (@facing_angle % 360 - @velocity.angle).abs < 2
    #   ideal_angle = Vector.from_angle(@facing_angle, @velocity.magnitude)
    #   @velocity = ideal_angle
    # else
    #   (1..50).each do
    #     entities.add(Skidmark.new(@position))
    #   end
    # end

    friction = @velocity.normalize * [-0.00025, @velocity.magnitude * -0.001999].min

    # acceleration

    @force = Vector[0, 0]
    possible_acceleration = Vector.from_angle(@facing_angle, 0.0022)
    @force += possible_acceleration if @controls.keyboard_gas? || @controls.button_gas?
    @force += friction


    # integrate force

    @velocity += @force * dt
    @position += @velocity * dt

    # spawn exhaust fumes

    while @exhaust_time_consumed < @time
      giving_gas = @controls.keyboard_gas? || @controls.button_gas?
      @exhaust_time_consumed += if giving_gas then 10 else 100 end
      entities.add(ExhaustCloud.new(@position + Vector.from_angle(@facing_angle, -50 + Gosu.random(-10, 10)) + Vector.random(@velocity.magnitude * 10), giving_gas))
    end

    # pull camera towards the point where the car would be in 200 milliseconds at current velocity:
    CAMERA.focus_of_attention = @position + possible_acceleration * (@velocity.magnitude + 2) * 50000 + @velocity * 400 * Math.sqrt(@velocity.magnitude)
    CAMERA.zoom_target = 0.75 / (@velocity.magnitude * (@velocity.magnitude) * 0.6 + 1)

    # export data used by ui
    DATA[:player_kph] = @velocity.magnitude * 120
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