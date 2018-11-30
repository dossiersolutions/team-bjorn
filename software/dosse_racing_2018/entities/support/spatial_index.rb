# class SpatialIndex
#   ZONE_WIDTH = 256
#   ZONE_DIMENSIONS = Vector[ZONE_WIDTH, ZONE_WIDTH]

#   def initialize
#     @zones = {}
#     @collideable_count = 0
#   end

#   def debug_str
#     "#{@zones.size} zones #{@collideable_count} collideables"
#   end

#   def get_zone_pos(pos)
#     (pos.map(&:to_i) / ZONE_WIDTH)
#   end

#   def get_zone(pos)
#     pos = get_zone_pos(pos).to_a
#     # puts pos.inspect
#     @zones[pos] ||= Set.new
#   end

#   def add(entity)
#     zone = get_zone(entity.position)
#     throw "already indexed" if zone.include?(entity)
#     zone.add(entity)
#     @collideable_count += 1
#   end

#   def remove(entity)
#     zone = get_zone(entity.position)
#     throw "cant remove nonexistent" if !zone.include?(entity)
#     zone.delete(entity)
#     @zones.delete(get_zone_pos(entity.position).to_a) if zone.empty?
#     @collideable_count -= 1
#   end

#   def each_neighbor(pos, radius)
#     center_zone = get_zone_pos(pos)
#     zone_radius = (radius / 512)
#     top_left = center_zone - Vector[2, 2]
#     bottom_right = center_zone + Vector[2, 2]

#     # puts zone_radius, top_left, bottom_right
#     #       puts @zones.keys.first.inspect
#     # draw_triangle(center_zone * ZONE_WIDTH, 30, Gosu::Color::RED)
#     # draw_triangle(top_left * ZONE_WIDTH, 30, Gosu::Color::FUCHSIA)
#     # draw_triangle(bottom_right * ZONE_WIDTH, 30, Gosu::Color::FUCHSIA)

#     current_zone = Vector[*top_left]

#     while current_zone.y < bottom_right.y
#       while current_zone.x < bottom_right.x
#         current_zone.x += 1

#         if zone = @zones[current_zone.to_a]
#           # draw_triangle(current_zone * ZONE_WIDTH, 30, Gosu::Color::RED)
#           # draw_triangle((current_zone + Vector[1, 1]) * ZONE_WIDTH, 40, Gosu::Color::RED)
#           zone.each do |entity|
#             # puts (entity.position).magnitude
#             if (pos - entity.position).magnitude < radius
#               yield entity
#             end
#           end
#         end

#       end
#       current_zone.x = top_left.x
#       current_zone.y += 1
#     end
#   end
# end