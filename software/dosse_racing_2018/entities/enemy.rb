class Enemy
  def initialize(name, color)
    @name = name
    @data = deserialize_from_file(name + ".enemy")
    @time = 0
    @current_frame = @data.first
    @car_scale = 1
    @alpha_color = color
    @color = Gosu::Color::rgb(color.red, color.green, color.blue)
    @race_completed = false
    @race_time = 0.0
  end

  attr_reader :alpha_color, :color, :race_completed, :race_time, :name

  def update(dt, entities)
    @time += dt
    @current_index = [(@time / ENEMY_FRAME_LENGTH).to_i, @data.size - 1].min
    @current_frame = @data[@current_index]
    if @current_index == @data.size - 1
      @race_completed = true
    else
      @race_time += dt
    end
  end

  def position
    @current_frame[0]
  end

  def facing_angle
    @current_frame[1]
  end

  def progress
    @current_frame[2]
  end

  def draw(millis)
    Assets::SUV.draw_rot(*position, 50, facing_angle + 90, 0.5, 0.5, @car_scale, @car_scale, @alpha_color)
    # Assets::UI_FONT.draw_text(@name, *(@position), 10000, 1, 1, 1, 1, Gosu::Color::WHITE)
    Assets::UI_FONT.draw_text(@name, *position, 11000, 0.5, 0.5, @color)
  end
end