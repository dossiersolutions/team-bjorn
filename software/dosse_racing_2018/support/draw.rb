
def draw_triangle(pos, size, color=Gosu::Color::WHITE, z=100, mode=:default)
  xoff = size * 0.5
  Gosu::draw_triangle(pos.x - xoff, pos.y, color, pos.x + xoff, pos.y, color, pos.x, pos.y+size, color, z, mode)
end