
class EntitySystem
  def initialize
    @entities = Set[]
    @_time = 0
  end

  attr_reader :entities

  def update(dt)
    @birth_list = []
    @kill_list = []
    @_time += dt
    @entities.each {|e| e.update(dt, self) }
    @kill_list.each  {|e| @entities.delete(e) }
    @birth_list.each {|e| @entities << e }
    abort("too many entities created") if @birth_list.length > 100
    @birth_list = @kill_list = nil
    self
  end
  
  def draw
    @entities.each {|e| e.draw(@_time) }
  end

  def add(entity)
    @birth_list << entity
  end

  def kill(entity)
    @kill_list << entity
  end
end