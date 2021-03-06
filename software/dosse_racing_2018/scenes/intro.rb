require "./scenes/sub/starfield"

class IntroScene
  def initialize
    Assets::THEME_SONG.play(true)
    @time = 0
    @starfield = Starfield.new
  end

  def update(dt)
    @time += dt

    return GameWorld.new if (CONTROLS.gas? and (@time > 1000))

    @starfield.update(dt)

    self
  end

  def draw_logo
    image = Assets::LOGO
    image_scale = VIEWPORT_SIZE.x / image.width + ncos(@time * 0.0002) * 0.4
    image_pos = VIEWPORT_CENTER + Vector.from_angle(@time*0.011, 50)
    image_color = Gosu::Color::argb(@time / 60, 255, 255, 255)
    image.draw_rot(*image_pos, 0, Math.sin(@time * 0.00004)*15, 0.5, 0.4, image_scale, image_scale, image_color)
  end

  def draw_star_image
    image = Assets::STARS
    image_scale = (VIEWPORT_SIZE.y * 2.3 / image.height) + ncos(@time * 0.00011) * 0.7
    image_pos = VIEWPORT_CENTER + Vector.from_angle(@time * -0.007, 80)
    image_color = Gosu::Color::argb([@time / 200, 80].min, nsin(@time*0.001) * 255, ncos(@time*0.001) * 255, nsin(@time*0.0005) * 255)
    image.draw_rot(*image_pos, 0, @time * -0.0004, 0.5, 0.5, image_scale, image_scale, image_color, :add)
  end

  def draw

    text_color = Gosu::Color.argb(nsin(@time * 0.005) * 255 , 255, 255, 255)

    Assets::UI_FONT.draw_text("Press thatButton or space bar...", *UI_TEXT_BOTTOM_LEFT, 1, 1.0, 1.0, text_color)

    draw_logo
    draw_star_image

    @starfield.draw
    
    color = Gosu::Color::argb(100, 255, 255, 255)

    margin = VIEWPORT_SIZE / 4

    Gosu::draw_quad(
      margin.x, margin.y, color,
      Gosu::screen_width - margin.x, margin.y, color,
      margin.x, Gosu::screen_height - margin.y, color,
      Gosu::screen_width - margin.x, Gosu::screen_height - margin.y, color,
      10000
    )

    help_text = "CONTROLS:\n" +
    "thatButton, keyboard up - gas\n" +
    "thatKnob, keyboard left and right - steering\n" +
    "ESC - quit game\n" +
    "r - restart game\n" +
    "\n" +
    "#pushItLikeADossier\n" +
    "\n" +
    "- Team Bjorn"
    Assets::UI_FONT.draw_text(help_text, margin.x * 1.1, margin.y * 1.1, 10001, 0.7, 0.7, Gosu::Color::argb(255, 0, 0, 0))
  end
end