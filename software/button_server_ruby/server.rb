require 'socket'
require "json"

CFG = JSON.parse(File.read("config.json"))

Message = Struct.new(:button_id, :counter, :button_state, :pot_state, :pot_q_state) do
  def readable
    to_a.join(", ")
  end
end

module CondEval
  def self.and(cond, msg, prev_msg)
    cond["conds"].all? do |sub_cond|
      eval_cond(sub_cond, msg, prev_msg)
    end
  end

  def self.button_down(cond, msg, prev_msg)
    return false if !prev_msg
    (msg.button_state == 1) && (prev_msg.button_state == 0)
  end

  def self.msg_field_equal(cond, msg, prev_msg)
    field = cond["field"]
    msg[field] == cond["value"]
  end
end

module ActionEval
  def self.log(action, msg)
    puts action["message"]
  end

  def self.shell(action, msg)
    system action["command"]
  end
end

def eval_cond(cond, msg, prev_msg)
  CondEval.send(cond["type"].to_sym, cond, msg, prev_msg)
end

def eval_actions(actions, msg)
  Thread.start(msg) do |msg|
    actions.each do |action|
      ActionEval.send(action["type"].to_sym, action, msg)
    end
  end
end

server = TCPServer.open(38911) 
puts "Team Bjørn mock server listening..."

msg = nil

loop do
  sock = server.accept()
  prev_msg = msg
  msg = Message.new(*sock.read(10).unpack("n*"))
  sock.close()

  if CFG["settings"]["logging"]["buttonStateMessages"]
    puts msg.readable
  end

  CFG["triggers"].each do |trigger|
    if eval_cond(trigger["cond"], msg, prev_msg)
      eval_actions(trigger["actions"], msg)
    end
  end

end