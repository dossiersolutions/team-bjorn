class ExhaustCloud
  LIFESPAN = 500.0
  def initialize(position, giving_gas=false)
    @position = position
    @age = 0
    @giving_gas = giving_gas
    @dark =@giving_gas && Gosu.random(0, 100) > 80
    @lightness = if @dark then Gosu.random(40, 100) else Gosu.random(120, 180) end
    @mode = if @dark then :multiply else :add end
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

  def draw(millis)
    color = Gosu::Color::argb([norm_age * 400, (1-norm_age) * 400, 80].min, @lightness, @lightness, @lightness)
    draw_triangle(@position, norm_age * (if @giving_gas then 20 else 15 end), color, 30, @mode)
  end
end

class Player
  def initialize
    @position = VIEWPORT_CENTER
    @velocity = Vector[0, 0.0000001]
    @facing_angle = 0
    @controls = Controls.new
    @car_scale = 75 / Assets::SUV.height
    @exhaust_time_consumed = 0
    @time = 0
  end

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

    friction = @velocity.normalize * [-0.0005, @velocity.magnitude * -0.0003].min

    # acceleration

    @force = Vector[0, 0]
    possible_acceleration = Vector.from_angle(@facing_angle, 0.001)
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
    CAMERA.focus_of_attention = @position + possible_acceleration * (@velocity.magnitude + 2) * 50000 + @velocity * 200 * Math.sqrt(@velocity.magnitude)
    CAMERA.zoom = 1.0 / (@velocity.magnitude * (@velocity.magnitude) * 0.3 + 1)

    # export data used by ui
    DATA[:player_kph] = @velocity.magnitude * 70
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
    @entities << CAMERA
    @entities << PROCEDURAL_MAP_GENERATOR
  end

  def draw
    ground_color = Gosu::Color::rgb(0, 100, 20)
    Gosu::draw_quad(
      0,                   0,                     ground_color,
      Gosu::screen_width,  0,                     ground_color,
      0,                   Gosu::screen_height,   ground_color,
      Gosu::screen_width,  Gosu::screen_height,   ground_color,
      -10000
    )
    CAMERA.apply do
      super
    end

    Assets::UI_FONT.draw_text("#{DATA[:player_kph].to_i} kph", *UI_TEXT_TOP_LEFT, 10000, 1.0, 1.0, Gosu::Color::argb(100, 255, 255, 255))
  end
end