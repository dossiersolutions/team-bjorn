class PathRecorder

  def initialize()
    @frames = []
    @time = 0
  end

  def update(dt, entities)
    @time += dt

    while @time > ENEMY_FRAME_LENGTH
      @time -= ENEMY_FRAME_LENGTH

      @frames << [PLAYER.position.to_a, PLAYER.facing_angle, PLAYER.progress]
    end

    if Gosu.button_down?(Gosu::KB_R)
      serialize_to_file("anaaam.enemy", @frames)
    end
  end

  def draw(time)
  end

end