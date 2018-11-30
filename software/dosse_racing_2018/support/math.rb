def nsin(theta) # normalized version
  Math::sin(theta) * 0.5 + 0.5
end

def ncos(theta) # normalized version
  Math::cos(theta) * 0.5 + 0.5
end

class Vector # add some more utilties to Ruby's default vector class
  def x; self[0]; end
  def y; self[1]; end
  def x=(v); self[0] = v; end
  def y=(v); self[1] = v; end

  def self.from_angle(angle, magnitude=1)
    self[Gosu.offset_x(angle, magnitude), Gosu.offset_y(angle, magnitude)]
  end

  def self.rng_random(rng)
    Vector[rng.rand, rng.rand]
  end

  def self.random(xmin, xmax=-xmin, ymin=xmin, ymax=xmax)
    Vector[Gosu::random(xmin, xmax), Gosu::random(ymin, ymax)]
  end

  def self.random_pixel
    self.random(0, Gosu::screen_width, 0, Gosu::screen_height)
  end

  def angle
    Math::atan2(y, x).radians_to_gosu
  end

  def rotate(angle)
    angle = angle.gosu_to_radians
    sn = Math.sin(angle)
    cs = Math.cos(angle)
    Vector[x * cs - y * sn, x * sn + y * cs]
  end

  def mult_each(other)
    Vector[x*other.x, y*other.y]
  end

  def div_each(other)
    Vector[x/other.x, y/other.y]
  end

  def outside_viewport?
    (x < 0) || (x > VIEWPORT_SIZE.x) || (y < 0) || (y > VIEWPORT_SIZE.y)
  end

  def hashcode
    x + y * 10499
  end
end

class Gosu::Image
  def dimensions
    Vector[width, height]
  end
end