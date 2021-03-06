
class MapTile
  def initialize(topleft, entities)
    @topleft = Vector[*topleft]
    @forestness = PERLIN_FORESTNESS[*(@topleft *  0.00001)]
    @forest_amount = (@forestness + 1) * 0.5
    @flower_amount = (1 - @forestness) * 0.5
    @redness = @forest_amount * 50

    rng = Random.new(@topleft.hashcode)

    tree_count = [(@forest_amount * 200).to_i - 100, 0].max
    flower_count = [(@flower_amount * 200).to_i - 90, 0].max
    # puts tree_count

    @trees = (1..tree_count).map do
      tree = Tree.new(@topleft + TILE_DIMENSIONS.mult_each(Vector.rng_random(rng)))
      entities.add tree
      # SPATIAL_INDEX.add(tree)
      tree
    end

    @flowers = (1..flower_count).map do
      flower = Flower.new(@topleft + TILE_DIMENSIONS.mult_each(Vector.rng_random(rng)))
      entities.add flower
      # SPATIAL_INDEX.add(tree)
      flower
    end
  end

  def teardown(entities)
    @trees.each do |tree|
      # SPATIAL_INDEX.remove(tree)
      entities.kill tree
    end
    @flowers.each do |flower|
      # SPATIAL_INDEX.remove(tree)
      entities.kill flower
    end
  end

  attr_reader :topleft
  attr_accessor :last_access

  def update(dt, entities)
  end

  def draw(millis=0)
    rng = Random.new(@topleft.hashcode)
    # draw_triangle(top_left, 40, 
    green = rng.rand * (50 - @redness)
    red = rng.rand * (70) + @redness
    blue = rng.rand * [red, green].min
    color = Gosu::Color::argb(80 * (1-@forest_amount), red, green + @forest_amount * 255, blue)
    pos = @topleft + TILE_DIMENSIONS * rng.rand
    Assets::WHITE_SOFT.draw(*pos, -100, 30.0, 30.0, color, :add)

    green = rng.rand * 50
    red = rng.rand * 70
    blue = rng.rand * [red, green].min
    color = Gosu::Color::argb(40, red, green, blue)
    pos = @topleft + TILE_DIMENSIONS * rng.rand
    Assets::WHITE_SOFT.draw(*pos, -99, 15.0, 15.0, color, :add)

    pos = @topleft + TILE_DIMENSIONS * rng.rand
    opacity = rng.rand * 60 + @forest_amount * 10
    Assets::BLACK_SOFT.draw(*pos, -98, 30.0, 30.0, Gosu::Color::argb(opacity, 255, 255, 255))

    # draw_triangle(@topleft, 30, Gosu::Color::RED)

  end
end