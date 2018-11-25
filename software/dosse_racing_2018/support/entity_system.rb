
class EntitySystem
  def initialize
    @entities = Set[]
    @_time = 0
  end

  attr_reader :entities

  def update(dt)
    birth_list = []
    kill_list = []
    @_time += dt
    @entities.each {|e| e.update(dt, birth_list, kill_list) }
    kill_list.each  {|e| @entities.delete(e) }
    birth_list.each {|e| @entities << e }
    abort("too many entities created") if birth_list.length > 100
    self
  end
  
  def draw
    @entities.each {|e| e.draw(@_time) }
  end
end