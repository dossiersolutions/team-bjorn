require 'socket'


server = TCPServer.open(38911) 

puts "Team Bjørn mock server listening..."

loop do
   Thread.start(server.accept) do |sock|
      button_id      = sock.read(2).unpack("n")
      counter        = sock.read(2).unpack("n")
      button_state   = sock.read(2).unpack("n")
      pot_state      = sock.read(2).unpack("n")
      pot_q_state    = sock.read(2).unpack("n")
      sock.close

      puts [button_id, counter, button_state, pot_state, pot_q_state].join(", ")
   end
end