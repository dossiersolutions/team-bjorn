class RoadSegment
  def initialize(position)
    @position = Vector[*position]
  end

  attr_reader :next, :position

  def next=(next_segment)
    @next = next_segment
    @displacement = (@position - next_segment.position).rotate(180).normalize * 80
    puts @displacement
  end

  def update(dt, entities)
  end

  def draw(millis)
    draw_triangle(@position, 15, Gosu::Color::BLUE)
    color = Gosu::Color::rgb(100, 100, 100)
    if @next
      Gosu.draw_quad(*@position - @displacement, color, *@next.position - @displacement, color, *(@next.position + @displacement), color, *(@position + @displacement), color, 3)
    end
  end
end