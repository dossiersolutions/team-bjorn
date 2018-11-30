require 'socket'
include Socket::Constants

ButtonMessage = Struct.new(:button_id, :counter, :button_state, :pot_state, :pot_q_state) do
  def readable
    to_a.join(", ")
  end
end

class Controls
  def initialize
    @button_target_angle = 0
    @time_since_last_button_use = 100_000
    @message = nil

    Thread.new do
      server = TCPServer.open(38911) 

      loop do
        sock = server.accept()
        @message = ButtonMessage.new(*sock.read(10).unpack("n*"))
        @time_since_last_button_use = 0
        @button_target_angle = @message.pot_state # TODO
        sock.close()
      end
    end

  end

  attr_reader :button_target_angle, :time_since_last_button_use

  def update(dt)
    @time_since_last_button_use += dt

    @button_target_angle -= 0.3 * dt if Gosu.button_down?(Gosu::KB_A)
    @button_target_angle += 0.3 * dt if Gosu.button_down?(Gosu::KB_D)

    if Gosu.button_down?(Gosu::KB_SPACE) || Gosu.button_down?(Gosu::KB_A) || Gosu.button_down?(Gosu::KB_D)
      @time_since_last_button_use = 0
    end
  end

  def keyboard_gas?
    Gosu.button_down?(Gosu::KB_UP)
  end

  def button_gas?
    Gosu.button_down?(Gosu::KB_SPACE) || @message && @message.button_state == 1
  end

  def gas?
    button_gas? || keyboard_gas?
  end

end