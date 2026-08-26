local Geometry = {}

Geometry.DESIGN_WIDTH = 1920
Geometry.DESIGN_HEIGHT = 1080

local TILE = {
    [1] = {width = 136, height = 192, top = 135, side = 135, thickness = 27},
    [2] = {width = 136, height = 193, top = 135, side = 169, thickness = 24},
    [3] = {width = 76, height = 191, top = 116, side = 75, thickness = 77},
    [4] = {width = 76, height = 191, top = 116, side = 75, thickness = 77},
    [5] = {width = 136, height = 193, top = 135, side = 169, thickness = 27},
    [6] = {width = 136, height = 193, top = 135, side = 169, thickness = 27},
    [7] = {width = 173, height = 132, top = 108, side = 174, thickness = 27},
    [8] = {width = 173, height = 132, top = 108, side = 174, thickness = 27},
    [9] = {width = 136, height = 218, top = 137, side = 158, thickness = 24},
    [10] = {width = 175, height = 168, top = 108, side = 177, thickness = 24},
}

local HAND = {
    [1] = {x = 280, y = 1070, scale = 0.46, pose = 3, anchorX = 0, anchorY = 1, horizontal = false, direction = -1},
    [2] = {x = 5, y = 10, scale = 1.0, pose = 1, anchorX = 0, anchorY = 0, horizontal = true, direction = 1},
    [3] = {x = 1640, y = 205, scale = 0.46, pose = 4, anchorX = 1, anchorY = 0, horizontal = false, direction = 1},
    [4] = {x = 1385, y = 968, scale = 0.5, pose = 2, anchorX = 1, anchorY = 0, horizontal = true, direction = -1},
}

local RIVER = {
    [1] = {x = 565, y = 833.976, scale = 0.5, pose = 8, anchorX = 0, anchorY = 1, horizontal = false, dx = -1, dy = -1},
    [2] = {x = 704.032, y = 395, scale = 0.5, pose = 5, anchorX = 0, anchorY = 0, horizontal = true, dx = 1, dy = -1},
    [3] = {x = 1350, y = 324, scale = 0.5, pose = 7, anchorX = 1, anchorY = 0, horizontal = false, dx = 1, dy = 1},
    [4] = {x = 1219.968, y = 700, scale = 0.5, pose = 5, anchorX = 1, anchorY = 0, horizontal = true, dx = -1, dy = 1},
}

local MELD_RULE = {
    [1] = {x = 280, y = 1070, rootScale = 0.46, combScale = 0.9, anchorX = 0, anchorY = 1, horizontal = false, direction = -1, distance = 2, normalPose = 8, backPose = 10},
    [2] = {x = 5, y = 10, rootScale = 1.0, combScale = 0.837, anchorX = 0, anchorY = 0, horizontal = true, direction = 1, distance = 20, normalPose = 5, backPose = 9},
    [3] = {x = 1640, y = 205, rootScale = 0.46, combScale = 0.9, anchorX = 1, anchorY = 0, horizontal = false, direction = 1, distance = 2, normalPose = 7, backPose = 10},
    [4] = {x = 1385, y = 968, rootScale = 0.5, combScale = 0.9, anchorX = 1, anchorY = 0, horizontal = true, direction = -1, distance = 20, normalPose = 5, backPose = 9},
}

local ROTATE = {
    [1] = {{8, 0}, {6, 1}, {8, 2}, {5, 3}},
    [2] = {{7, 1}, {5, 0}, {8, 3}, {5, 2}},
    [3] = {{7, 2}, {6, 1}, {7, 0}, {5, 3}},
    [4] = {{7, 1}, {5, 2}, {8, 3}, {5, 0}},
}

local ALIGN = {
    [1] = {{2, 3, 1, 4, 6}, {2, 3, 4, 5, 6}, {2, 3, 1, 4, 6}, {2, 1, 4, 5, 6}},
    [2] = {{2, 1, 3, 4, 5}, {2, 3, 4, 5, 6}, {2, 1, 3, 4, 5}, {2, 1, 4, 5, 6}},
    [3] = {{2, 1, 3, 4, 5}, {2, 3, 4, 5, 6}, {2, 1, 3, 4, 5}, {2, 1, 4, 5, 6}},
    [4] = {{2, 3, 1, 4, 6}, {2, 3, 4, 5, 6}, {2, 3, 1, 4, 6}, {2, 1, 4, 5, 6}},
}

function Geometry.tileMetrics(pose)
    return TILE[pose] or TILE[1]
end

function Geometry.localSeat(seatNumber, mySeat, chairCount)
    local seat = ((seatNumber - mySeat + chairCount) % chairCount + 1) % chairCount + 1
    if chairCount == 2 and seat == 1 then
        return 4
    end
    return seat
end

function Geometry.handPosition(localSeat, zeroBasedIndex, meldCount, drawn, handCount)
    local rule = HAND[localSeat]
    local startOffset = localSeat == 2 and (meldCount or 0) * 405 or 0
    local step = TILE[rule.pose].top * rule.scale
    local advance = startOffset + zeroBasedIndex * step
    if drawn then
        advance = startOffset + handCount * step + 15
    end
    if rule.horizontal then
        return rule.x + rule.direction * advance, rule.y, rule.scale, rule.pose, rule.anchorX, rule.anchorY
    end
    return rule.x, rule.y + rule.direction * advance, rule.scale, rule.pose, rule.anchorX, rule.anchorY
end

local function riverLocal(rule, zeroBasedIndex, lineMax, layerMax)
    local layerCount = zeroBasedIndex % layerMax
    local layerIndex = math.floor((zeroBasedIndex + layerMax - 1) / layerMax)
    local x, y, z
    if layerCount > 0 then
        local previousX, previousY, previousZ = riverLocal(rule, zeroBasedIndex - 1, lineMax, layerMax)
        if layerCount % lineMax == 0 then
            x = rule.horizontal and 0 or (previousX + rule.dx * TILE[rule.pose].side)
            y = rule.horizontal and (previousY + rule.dy * TILE[rule.pose].side) or TILE[rule.pose].thickness * (layerIndex - 1)
        else
            x = rule.horizontal and (previousX + rule.dx * TILE[rule.pose].top) or previousX
            y = rule.horizontal and previousY or (previousY + rule.dy * TILE[rule.pose].top)
        end
        z = previousZ - rule.dy
    elseif zeroBasedIndex >= layerMax then
        x = 0
        y = TILE[rule.pose].thickness * layerIndex
        z = rule.dy > 0 and layerMax * 2 or layerMax + 1
    else
        x, y, z = 0, 0, 0
    end
    return x, y, z
end

function Geometry.riverPosition(localSeat, zeroBasedIndex, chairCount, maxLineCount)
    local rule = RIVER[localSeat]
    local lineMax = chairCount == 2 and 16 or 8
    local layerMax = lineMax * maxLineCount
    local x, y, z = riverLocal(rule, zeroBasedIndex, lineMax, layerMax)
    return rule.x + x * rule.scale, rule.y + y * rule.scale, rule.scale, rule.pose, rule.anchorX, rule.anchorY, z
end

local function displayValues(localSeat, meld)
    local values = {}
    if meld.combType == "CONCEALED_KONG" then
        values = {0x72, 0x72, 0x72, meld.tiles[1]}
    else
        for index, value in ipairs(meld.tiles or {}) do
            values[index] = value
        end
        if meld.combType == "CHOW" and (localSeat == 1 or localSeat == 4) then
            local reversed = {}
            for index = #values, 1, -1 do
                reversed[#reversed + 1] = values[index]
            end
            values = reversed
        end
    end
    return values
end

function Geometry.meldPlacements(localSeat, melds, mySeat, chairCount)
    local rule = MELD_RULE[localSeat]
    local placements = {}
    local cursor = 0
    for _, meld in ipairs(melds or {}) do
        local values = displayValues(localSeat, meld)
        local fromLocal = Geometry.localSeat(meld.fromSeat, mySeat, chairCount)
        local arrowIndex = 0
        local rotatePose = rule.normalPose
        if meld.combType ~= "CONCEALED_KONG" and fromLocal ~= localSeat then
            local rotate = ROTATE[localSeat][fromLocal]
            arrowIndex = rotate[2]
            rotatePose = rotate[1]
        end
        local centersX, centersY, poses = {}, {}, {}
        local advance, maxCross = 0, 0
        for index, value in ipairs(values) do
            local pose = value == 0x72 and rule.backPose or (index == arrowIndex and rotatePose or rule.normalPose)
            local tile = TILE[pose]
            poses[index] = pose
            if index <= 3 then
                if rule.horizontal then
                    centersX[index] = advance + tile.width / 2
                    centersY[index] = tile.height / 2
                    advance = advance + tile.width
                    maxCross = math.max(maxCross, tile.height)
                else
                    centersX[index] = tile.width / 2
                    centersY[index] = index == 1 and tile.height / 2 or advance - tile.height / 2 + tile.top
                    if index == 1 then
                        advance = tile.height
                    else
                        advance = advance + tile.top
                    end
                    maxCross = math.max(maxCross, tile.width)
                end
            else
                local align = ALIGN[localSeat][arrowIndex + 1][index - 3]
                centersX[index] = centersX[align]
                centersY[index] = centersY[align] + tile.thickness
            end
        end
        local contentWidth = rule.horizontal and advance or maxCross
        local contentHeight = rule.horizontal and maxCross or advance
        local anchorPos = cursor
        local originX = rule.anchorX == 1 and (rule.horizontal and anchorPos or 0) - contentWidth * rule.combScale or (rule.horizontal and anchorPos or 0)
        local originY = rule.anchorY == 1 and (rule.horizontal and 0 or anchorPos) - contentHeight * rule.combScale or (rule.horizontal and 0 or anchorPos)
        local effectiveScale = rule.combScale * rule.rootScale
        for index, value in ipairs(values) do
            placements[#placements + 1] = {
                value = value,
                pose = poses[index],
                x = rule.x + (originX + centersX[index] * rule.combScale) * rule.rootScale,
                y = rule.y + (originY + centersY[index] * rule.combScale) * rule.rootScale,
                scale = effectiveScale,
            }
        end
        local extent = rule.horizontal and contentWidth or contentHeight
        cursor = anchorPos + rule.direction * (extent * rule.combScale + rule.distance)
    end
    return placements
end

return Geometry
