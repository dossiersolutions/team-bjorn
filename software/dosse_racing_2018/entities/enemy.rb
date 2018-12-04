class Enemy
  def initialize(name, color)
    @name = name
    @data = deserialize(File.read(name + ".enemy"))
    @time = 0
    @current_frame = @data.first
    @car_scale = 1
    @alpha_color = color
    @color = Gosu::Color::rgb(color.red, color.green, color.blue)
  end

  attr_reader :alpha_color, :color

  def update(dt, entities)
    @time += dt
    @current_frame = @data[[(@time / ENEMY_FRAME_LENGTH).to_i, @data.size - 1].min]
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