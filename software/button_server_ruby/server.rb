require 'socket'
require "json"

CFG = JSON.parse(File.read("config.json"))

MSG_FIELDS = [:button_id, :counter, :button_state, :pot_state, :pot_q_state]
Message = Struct.new(*MSG_FIELDS) do
  def readable
    to_a.join(", ")
  end
end

module CondEval
  def self.event(cond, msg, prev_msg)
    case cond["event"]
    when "button_down"
      return false if !prev_msg
      return (msg.button_state == 1) && (prev_msg.button_state == 0)
    else
      throw "invalid event kind #{cond["event"]}"
    end
  end

  def self.operator(cond, msg, prev_msg)
    field = cond["field"]
    case cond["operator"]
    when "=="  then msg[field] == cond["value"]
    when "!="  then msg[field] != cond["value"]
    when "<"   then msg[field] <  cond["value"]
    when ">"   then msg[field] >  cond["value"]
    when "<="  then msg[field] <= cond["value"]
    when ">="  then msg[field] >= cond["value"]
    when "and" then cond["conds"].all? { |sub_cond| eval_cond(sub_cond, msg, prev_msg) }
    when "or"  then cond["conds"].any? { |sub_cond| eval_cond(sub_cond, msg, prev_msg) }
    else throw "invalid operator!"
    end
  end
end

def interpolate_config_str(str, msg)
  result = MSG_FIELDS.inject(str) do |memo, field|
    memo.gsub("$" + field.to_s, msg.send(field).to_s)
  end
  result
end

module ActionEval
  def self.log(action, msg)
    puts interpolate_config_str(action["message"], msg)
  end

  def self.shell(action, msg)
    system interpolate_config_str(action["command"], msg)
  end
end

def eval_cond(cond, msg, prev_msg)
  type = if cond["type"]
    cond["type"].to_sym
  elsif cond["event"]
    :event
  elsif cond["operator"]
    :operator
  end
  CondEval.send(type, cond, msg, prev_msg)
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