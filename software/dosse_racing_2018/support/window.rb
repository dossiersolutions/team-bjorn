
class Window < Gosu::Window
  def initialize(scene=LogoScene.new)
    super(*VIEWPORT_SIZE.map(&:to_i) , FULLSCREEN)
    self.caption = "DosseRacing 2018"
    @time = @old_time = Gosu::milliseconds()
    @scene = scene
  end
  
  def update
    @old_time = @time
    @time = Gosu::milliseconds()
    dt = @time - @old_time
    @scene = @scene.update(dt)

    close! if Gosu.button_down?(Gosu::KB_ESCAPE)
    close! if Gosu.button_down?(Gosu::KB_Q) # TODO
  end
  
  def draw
    @scene.draw()

    Assets::UI_FONT.draw_text(
      "#{Gosu::fps} fps #{DATA[:active_tiles]} tiles",
      *(UI_TEXT_TOP_LEFT + Vector[0, UI_TEXT_HEIGHT]), 10000, 1.0, 1.0, Gosu::Color::argb(100, 255, 255, 255))
  end
end