local TileView = {}

local FACE_PLIST = "mahjong/assets/mahlayer_mah_face_2.plist"
local FACE_ATLAS = "mahjong/assets/mahlayer_mah_face_2.png"
local GROUND_PLIST = "mahjong/assets/mahlayer_mah_ground.plist"
local GROUND_ATLAS = "mahjong/assets/mahlayer_mah_ground.png"
local ICON_PLIST = "mahjong/assets/mahlayer_mah_icon.plist"
local ICON_ATLAS = "mahjong/assets/mahlayer_mah_icon.png"
local JOKER_FRAME = "mahlayer_mah_face_joker_circle.png"
local JOKER_Z = 4
local loaded = false

-- These bounds are the Cocos composition bounds from
-- OriginalMahjongTileGeometry.defaultTile().  Coordinates are left/bottom
-- relative to the composition origin, so they remain valid after the node's
-- Java-derived anchor point and scale are applied.
local POSE = {
    [1] = {width = 136, height = 192, back = {w = 136, h = 60, x = 0, y = 132, z = 1}, ground = {w = 136, h = 180, x = 0, y = 0, z = 2}, face = {w = 132.75, h = 146.15, x = 3.25, y = 9.425, scale = 0.925, rotation = 0}, joker = {x = 40.5, y = 134.5, rotation = 0}},
    [2] = {width = 136, height = 193, back = {w = 136, h = 181, x = 0, y = 0, z = 2}, ground = {w = 136, h = 60, x = 0, y = 133, z = 1}},
    [3] = {width = 76, height = 191, back = {w = 38, h = 191, x = 38, y = 0, z = 1}, ground = {w = 38, h = 191, x = 0, y = 0, z = 2}},
    [4] = {width = 76, height = 191, back = {w = 38, h = 191, x = 0, y = 0, z = 1}, ground = {w = 38, h = 191, x = 38, y = 0, z = 2}},
    [5] = {width = 136, height = 193, back = {w = 136, h = 61, x = 0, y = 0, z = 1}, ground = {w = 136, h = 181, x = 0, y = 12, z = 2}, face = {w = 132.75, h = 146.15, x = 3.25, y = 36.925, scale = 0.925, rotation = 0}, joker = {x = 40.5, y = 150.5, rotation = 0}},
    [6] = {width = 136, height = 193, back = {w = 136, h = 61, x = 0, y = 0, z = 1}, ground = {w = 136, h = 181, x = 0, y = 12, z = 2}, face = {w = 132.75, h = 146.15, x = 3.25, y = 36.925, scale = 0.925, rotation = 180}, joker = {x = 95.5, y = 57.5, rotation = 180}},
    [7] = {width = 173, height = 132, back = {w = 173, h = 60, x = -1, y = 0, z = 1}, ground = {w = 174, h = 120, x = -1, y = 12, z = 2}, face = {w = 131.535, h = 116.55, x = 20.7325, y = 21.225, scale = 0.8325, rotation = -90}, joker = {x = 42.5, y = 55.5, rotation = -90}},
    [8] = {width = 173, height = 132, back = {w = 173, h = 60, x = -1, y = 0, z = 1}, ground = {w = 174, h = 120, x = -1, y = 12, z = 2}, face = {w = 131.535, h = 116.55, x = 20.7325, y = 21.225, scale = 0.8325, rotation = 90}, joker = {x = 130.5, y = 91.5, rotation = 90}},
    [9] = {width = 136, height = 182, back = {w = 136, h = 170, x = 0, y = 12, z = 2}, ground = {w = 136, h = 60, x = 0, y = 0, z = 1}},
    [10] = {width = 175, height = 132, back = {w = 175, h = 120, x = 0, y = 12, z = 2}, ground = {w = 173, h = 60, x = 1, y = 0, z = 1}},
}

local function ensureFrames()
    if loaded then
        return
    end
    local cache = cc.SpriteFrameCache:getInstance()
    cache:addSpriteFrames(GROUND_PLIST, GROUND_ATLAS)
    cache:addSpriteFrames(FACE_PLIST, FACE_ATLAS)
    cache:addSpriteFrames(ICON_PLIST, ICON_ATLAS)
    loaded = true
end

local function suffix(pose)
    if pose == 1 then return "1_1" end
    if pose == 2 then return "1_2" end
    if pose == 3 then return "1_3" end
    if pose == 4 then return "1_4" end
    if pose == 5 or pose == 6 then return "2_1_1" end
    if pose == 7 or pose == 8 then return "2_1_2" end
    if pose == 9 then return "2_2_1" end
    return "2_2_2"
end

local function groundFrame(pose)
    return "mahlayer_mahface_circle_light_" .. suffix(pose) .. ".png"
end

local function backFrame(pose)
    return "mahlayer_mahback_circle_green_" .. suffix(pose) .. ".png"
end

local function addScaledSprite(parent, frameName, layer, originX, originY, scale, zOrder)
    local sprite = cc.Sprite:createWithSpriteFrameName(frameName)
    if not sprite then
        return nil
    end
    local sourceSize = sprite:getContentSize()
    local sourceWidth = math.max(1, sourceSize.width)
    local sourceHeight = math.max(1, sourceSize.height)
    sprite:setScaleX(layer.w / sourceWidth * scale)
    sprite:setScaleY(layer.h / sourceHeight * scale)
    sprite:setPosition(
        originX + (layer.x + layer.w / 2) * scale,
        originY + (layer.y + layer.h / 2) * scale)
    parent:addChild(sprite, layer.z or zOrder)
    return sprite
end

local function createTile(pose, scale, anchorX, anchorY, tileValue, joker)
    ensureFrames()
    local metrics = POSE[pose] or POSE[1]
    local tile = cc.Node:create()
    local originX = -anchorX * metrics.width
    local originY = -anchorY * metrics.height
    addScaledSprite(tile, backFrame(pose), metrics.back, originX, originY, scale, 1)
    addScaledSprite(tile, groundFrame(pose), metrics.ground, originX, originY, scale, 2)

    if metrics.face and tileValue and tileValue ~= 0x72 then
        local face = cc.Sprite:createWithSpriteFrameName(
            "mj_mah_face_2_" .. tostring(tileValue) .. ".png")
        if face then
            local sourceSize = face:getContentSize()
            if math.abs(metrics.face.rotation) == 90 then
                face:setScale(metrics.face.w / math.max(1, sourceSize.height) * scale)
            else
                face:setScaleX(metrics.face.w / math.max(1, sourceSize.width) * scale)
                face:setScaleY(metrics.face.h / math.max(1, sourceSize.height) * scale)
            end
            face:setRotation(metrics.face.rotation)
            face:setPosition(
                originX + (metrics.face.x + metrics.face.w / 2) * scale,
                originY + (metrics.face.y + metrics.face.h / 2) * scale)
            tile:addChild(face, 3)
        end
        if joker and metrics.joker then
            local icon = cc.Sprite:createWithSpriteFrameName(JOKER_FRAME)
            if icon then
                icon:setScale(scale)
                icon:setRotation(metrics.joker.rotation)
                icon:setPosition(
                    originX + metrics.joker.x * scale,
                    originY + metrics.joker.y * scale)
                tile:addChild(icon, JOKER_Z)
            end
        end
    end
    return tile
end

function TileView.face(tileValue, pose, scale, anchorX, anchorY, joker)
    return createTile(
        pose or 1,
        scale or 1.0,
        anchorX == nil and 0.5 or anchorX,
        anchorY == nil and 0.5 or anchorY,
        tileValue,
        joker == true)
end

function TileView.back(pose, scale, anchorX, anchorY)
    return createTile(
        pose or 2,
        scale or 1.0,
        anchorX == nil and 0.5 or anchorX,
        anchorY == nil and 0.5 or anchorY,
        nil)
end

function TileView.metrics(pose)
    local value = POSE[pose] or POSE[1]
    return value.width, value.height
end

function TileView.isLoaded()
    return loaded
end

return TileView
