class ExhaustCloud
  LIFESPAN = 500.0
  def initialize(position)
    @position = position
    @age = 0
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
    color = Gosu::Color::argb([norm_age * 800, (1-norm_age) * 400, 80].min, 200, 200, 200)
    draw_triangle(@position, norm_age * 15, color, 30, :add)
  end
end

class Player
  def initialize
    @position = VIEWPORT_CENTER
    @velocity = Vector[0, 0.0000001]
    @facing_angle = 0
    @controls = Controls.new
    @car_scale = UI_TEXT_HEIGHT * 1 / Assets::SUV.height
    @exhaust_time_consumed = 0
    @time = 0
  end

  def update(dt, entities)
    @controls.update(dt)
    @time += dt

    turn_amount = [@velocity.magnitude * 0.5, 0.1].min * dt
    @facing_angle -= turn_amount if Gosu.button_down?(Gosu::KB_LEFT)
    @facing_angle += turn_amount if Gosu.button_down?(Gosu::KB_RIGHT)

    if @controls.button_gas?
      if ((@controls.button_target_angle - @facing_angle) % 360) > 180
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

    while @exhaust_time_consumed < @time
      @exhaust_time_consumed += 100
      entities.add(ExhaustCloud.new(@position + Vector.from_angle(@facing_angle, -50)))
    end

    # focus towards the point where the care would be in this many milliseconds:
    CAMERA.focus_of_attention = @position + @velocity * 300
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
  end
end