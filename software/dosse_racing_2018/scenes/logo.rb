
class LogoScene
  def initialize
    Assets::JINGLE.play(false)
    @time = 0
    @starfield = Starfield.new
    @car_pos = Vector[-Assets::SUV.width - VIEWPORT_SIZE.x * 0.05, UI_TEXT_HEIGHT * 8]
  end

  def update(dt)
    @time += dt

    if (@time > 26_000) || CONTROLS.gas?
      return IntroScene.new 
    end

    @car_pos.x += dt * VIEWPORT_SIZE.x * 0.00005

    self
  end


  def draw

    # text_color = Gosu::Color.argb(255, 255, 255, 255)
    c1 = Gosu::Color::argb(@time / 50 - 100, nsin(@time*0.001) * 255, ncos(@time*0.001) * 255, nsin(@time*0.0005) * 255)
    c2 = Gosu::Color::argb(@time / 50 - 200, nsin(@time*0.001) * 255, ncos(@time*0.001) * 255, nsin(@time*0.0005) * 255)
    c3 = Gosu::Color::argb(@time / 50 - 300, 255, 255, 255)

    Assets::UI_FONT.draw_text("Team Bjorn", UI_TEXT_HEIGHT * 2, UI_TEXT_HEIGHT * 7, 1, 1.0, 1.0, c1)

    Assets::UI_FONT.draw_text("Dossier Developer Days 2018", UI_TEXT_HEIGHT * 5, UI_TEXT_HEIGHT * 8, 1, 1.0, 1.0, c2)

    Assets::UI_FONT.draw_text("presenting a thatButton enabled experience...", UI_TEXT_HEIGHT * 8, UI_TEXT_HEIGHT * 9, 1, 1.0, 1.0, c3)

    car_scale = UI_TEXT_HEIGHT * 1 / Assets::SUV.height

    Assets::SUV.draw_rot(@car_pos.x, @car_pos.y + Math::sin((@time)*0.001) * 25, -1, 180 - Math::sin((@time)*0.001) * 8, 0.5, 0.5, car_scale, car_scale, Gosu::Color::argb(255, 255, 255, 255))

  end
end