class PathRecorder
  FRAME_LENGTH = 17

  def initialize()
    @frames = []
    @time = 0
  end

  def update(dt, entities)
    @time += dt

    while @time > FRAME_LENGTH
      @time -= FRAME_LENGTH

      # @frames << [PLAYER.position.to_a, PLAYER.angle]
    end

    # if Gosu.button_down?(Gosu::KB_R)
    #   File.write("hanses.enemy", Marshal.dump(@frames))
    # end
  end

  def draw(time)
  end

end