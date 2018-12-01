class RoadSegment
  def initialize(position)
    @position = Vector[*position]
    shade = Gosu::random(25, 35)
    @color = Gosu::Color::rgb(shade, shade, shade)
  end

  attr_reader :next, :position, :displacement, :color

  def next=(next_segment)
    @next = next_segment
    @displacement = (@position - next_segment.position).rotate(180).normalize * 120
  end

  def update(dt, entities)
  end

  def draw(millis)
    draw_triangle(@position, 15, Gosu::Color::BLUE)
    if @next
      Gosu.draw_quad(*@position - @displacement, @color, *@next.position - (@next.displacement || displacement), @next.color, *(@next.position + (@next.displacement || displacement)), @next.color, *(@position + @displacement), @color, 3)
    end
  end
end