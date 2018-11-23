--
-- DosseSuv Simulator 2018
--
-- Work in progress...
-- Trying to implement this in lua using the Löve game engine..
--

local socket = require("socket")

function love.load()
  px  = 0 -- player position
  py  = 500
  pvx = 0 -- player velocity
  pvy = 0
  cx  = 0 -- camera x
  cy  = 0 -- camera y
  cs  = 1 -- camera scale

  ex  = 100
  ey  = 0

  suv_img = love.graphics.newImage("suv.png")
end

function love.update(dt)

  -- HANDLE KEYBOARD

  -- player control and physics
  player_force = 10 * dt
  max_velocity = 20
  friction = 5 * dt

  if love.keyboard.isDown("left")  then pvx = pvx - player_force end
  if love.keyboard.isDown("right") then pvx = pvx + player_force end
  if true then pvy = pvy - player_force * (-max_velocity - pvy) / -max_velocity end

  friction = friction + math.abs(pvx) * dt
  friction = friction + math.abs(pvx) * dt

  if pvx > 0 then
    if pvx > friction then pvx = pvx - friction else pvx = 0 end
  else
    if pvx < -friction then pvx = pvx + friction else pvx = 0 end
  end

  if pvy > 0 then
    if pvy > friction then pvy = pvy - friction else pvy = 0 end
  else
    if pvy < -friction then pvy = pvy + friction else pvy = 0 end
  end

  px = px + pvx
  py = py + pvy

  cx = -px + 500 -- camera follows player for now
  cy = -py + 500

  -- re-spawn enemies that leave the field
  if ey - 500 > -cy then
    ey = -cy
    ex = love.math.random() * -400
  end

end

function love.draw()

  -- draw world
  love.graphics.push()
  love.graphics.scale(cs)
  love.graphics.translate(cx, cy)
  love.graphics.setColor(0, 255, 0)
  love.graphics.circle("fill", ex, ey, 5)

  car_rot = math.atan2(pvx, -pvy) + math.pi / 2

  love.graphics.draw(suv_img, px, py, car_rot, 1, 1, 50, 20)
  love.graphics.pop()

  -- draw ui
  love.graphics.setColor(255, 255, 255)
  love.graphics.print(" " .. ey .. " " .. cy, 10, 10)
end
