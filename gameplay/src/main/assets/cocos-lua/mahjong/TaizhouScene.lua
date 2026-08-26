local TaizhouScene = class("TaizhouScene", function()
    return cc.Scene:create()
end)

local CocosRuntime = require("app.CocosRuntime")
local TaizhouState = require("mahjong.TaizhouState")
local Geometry = require("mahjong.TaizhouGeometry")
local TileView = require("mahjong.TaizhouTileView")

local DESIGN_WIDTH = Geometry.DESIGN_WIDTH
local DESIGN_HEIGHT = Geometry.DESIGN_HEIGHT
local CENTER_ATLAS_PLIST = "mahjong/assets/mah_game_layer.plist"
local CENTER_ATLAS = "mahjong/assets/game_layer.png"
local TABLE_INFO_PLIST = "mahjong/assets/tableInfo.plist"
local TABLE_INFO_ATLAS = "mahjong/assets/tableInfo.png"
local COMMON_INFO_PLIST = "mahjong/assets/common_gamelayer.plist"
local COMMON_INFO_ATLAS = "mahjong/assets/common_gamelayer.png"
local AVATAR_ATLAS = "mahjong/assets/avatar_lobby_atlas.png"
local centerFramesLoaded = false

local PLAYER_SLOT = {
    [1] = {x = 80, topY = 295},
    [2] = {x = 80, topY = 720},
    [3] = {x = 1835, topY = 295},
    [4] = {x = 1455, topY = 80},
}

local CENTER_NODE = {
    background = {frame = "mah_clock_bg.png", x = 960.576, topY = 479.736},
    north = {frame = "mah_clock_north_1.png", x = 959.076, topY = 546.236},
    northLight = {frame = "mah_clock_north.png", x = 960.349, topY = 544.236},
    northPanel = {frame = "mah_clock_north_2.png", x = 960.349, topY = 544.236},
    northGlow = {frame = "mah_clock_north_3.png", x = 960.349, topY = 544.236},
    south = {frame = "mah_clock_south_1.png", x = 960.076, topY = 411.236},
    southLight = {frame = "mah_clock_south.png", x = 961.982, topY = 411.729},
    southPanel = {frame = "mah_clock_south_2.png", x = 961.982, topY = 411.729},
    southGlow = {frame = "mah_clock_south_3.png", x = 961.982, topY = 411.729},
    west = {frame = "mah_clock_west_1.png", x = 892.076, topY = 479.236},
    westLight = {frame = "mah_clock_west.png", x = 892.666, topY = 477.532},
    westPanel = {frame = "mah_clock_west_2.png", x = 892.666, topY = 477.532},
    westGlow = {frame = "mah_clock_west_3.png", x = 892.666, topY = 477.532},
    east = {frame = "mah_clock_east_1.png", x = 1028.076, topY = 479.236},
    eastLight = {frame = "mah_clock_east.png", x = 1027.904, topY = 477.591},
    eastPanel = {frame = "mah_clock_east_2.png", x = 1027.904, topY = 477.591},
    eastGlow = {frame = "mah_clock_east_3.png", x = 1027.904, topY = 477.591},
}

local function addText(parent, text, x, y, size, color, anchorX, anchorY)
    local label = cc.Label:createWithSystemFont(text or "", "sans", size or 28)
    label:setPosition(x, y)
    label:setAnchorPoint(cc.p(anchorX == nil and 0.5 or anchorX, anchorY == nil and 0.5 or anchorY))
    label:setColor(color or {r = 246, g = 224, b = 151})
    parent:addChild(label, 20)
    return label
end

local function cocosYFromTop(topY)
    return DESIGN_HEIGHT - topY
end

local function ensureCenterFrames()
    if centerFramesLoaded then
        return
    end
    local cache = cc.SpriteFrameCache:getInstance()
    cache:addSpriteFrames(CENTER_ATLAS_PLIST, CENTER_ATLAS)
    cache:addSpriteFrames(TABLE_INFO_PLIST, TABLE_INFO_ATLAS)
    cache:addSpriteFrames(COMMON_INFO_PLIST, COMMON_INFO_ATLAS)
    centerFramesLoaded = true
end

local function centerSprite(parent, node, active, zOrder)
    ensureCenterFrames()
    local frameName = node.frame
    if active and node.activeFrame then
        frameName = node.activeFrame
    end
    local sprite = cc.Sprite:createWithSpriteFrameName(frameName)
    if sprite then
        sprite:setPosition(node.x, cocosYFromTop(node.topY))
        parent:addChild(sprite, zOrder or 1)
    end
    return sprite
end

local function seatLabel(parent, seat, position)
    local slot = PLAYER_SLOT[position]
    if not slot then
        return
    end
    local centerY = cocosYFromTop(slot.topY)
    centerSprite(parent, {frame = "mah_head_bg.png", x = slot.x, topY = slot.topY}, false, 3)

    -- Java Canvas uses the lobby atlas's 120x120 default avatar as the
    -- fallback when the remote avatar has not been downloaded yet.
    local avatar = cc.Sprite:create(AVATAR_ATLAS, cc.rect(642, 1527, 120, 120))
    if avatar then
        avatar:setPosition(slot.x - 0.5, centerY + 0.3)
        avatar:setScaleX(98 / math.max(1, avatar:getContentSize().width))
        avatar:setScaleY(99 / math.max(1, avatar:getContentSize().height))
        parent:addChild(avatar, 4)
    end
    if seat.host then
        centerSprite(
            parent,
            {frame = "mah_host_flag.png", x = slot.x + 50, topY = slot.topY + 26},
            false,
            5)
    end
    addText(
        parent,
        tostring(seat.displayName or "") ,
        slot.x,
        centerY - 77,
        26,
        {r = 251, g = 222, b = 115})
    addText(
        parent,
        tostring(seat.score or 0),
        slot.x,
        centerY - 110,
        26,
        {r = 251, g = 222, b = 115})
end

local function handSize(hand)
    if not hand then
        return 0
    end
    return #(hand.concealedTiles or {}) + (hand.drawnTile and 1 or 0)
end

local function addTile(layer, value, pose, scale, x, y, anchorX, anchorY, zOrder, joker)
    local tile
    if value == 0x72 then
        tile = TileView.back(pose, scale, anchorX, anchorY)
    else
        tile = TileView.face(value, pose, scale, anchorX, anchorY, joker)
    end
    if tile then
        tile:setPosition(x, y)
        layer:addChild(tile, zOrder or 1)
    end
end

function TaizhouScene:ctor()
    local view = cc.Director:getInstance():getOpenGLView()
    if view then
        view:setDesignResolutionSize(DESIGN_WIDTH, DESIGN_HEIGHT, cc.ResolutionPolicy.SHOW_ALL)
    end

    local background = cc.Sprite:create("mahjong/assets/scene_background.jpg")
    if background then
        background:setPosition(DESIGN_WIDTH / 2, DESIGN_HEIGHT / 2)
        self:addChild(background, -20)
    else
        self:addChild(cc.LayerColor:create({r = 3, g = 75, b = 63, a = 255}), -20)
    end

    self.tableLayer = cc.Node:create()
    self:addChild(self.tableLayer, 1)
    self.riverLayer = cc.Node:create()
    self:addChild(self.riverLayer, 8)
    self.meldLayer = cc.Node:create()
    self:addChild(self.meldLayer, 10)
    self.handLayer = cc.Node:create()
    self:addChild(self.handLayer, 12)
    self.infoLayer = cc.Node:create()
    self:addChild(self.infoLayer, 20)

    local roomNumber = CocosRuntime.roomNumber()
    if roomNumber:match("^%d%d%d%d%d%d$") then
        CocosRuntime.requestSnapshot(roomNumber, function(payload)
            local ok, callbackError = xpcall(function()
                local snapshot, errorMessage = TaizhouState.decode(payload)
                if snapshot then
                    self:renderSnapshot(snapshot)
                    print("ZJYX Cocos snapshot rendered: revision=" .. tostring(snapshot.revision))
                else
                    addText(self.infoLayer, errorMessage, 960, 540, 26, {r = 255, g = 180, b = 150})
                    print("ZJYX Cocos snapshot error: " .. tostring(errorMessage))
                end
            end, function(errorValue)
                return debug.traceback(tostring(errorValue), 2)
            end)
            if not ok then
                addText(self.infoLayer, "牌桌渲染失败", 960, 540, 26, {r = 255, g = 180, b = 150})
                print("ZJYX Cocos snapshot render exception: " .. tostring(callbackError))
            end
            return 0
        end)
    else
        addText(self.infoLayer, "等待有效房间号", 960, 540, 26, {r = 255, g = 180, b = 150})
    end
end

function TaizhouScene:renderSnapshot(snapshot)
    self.handLayer:removeAllChildren()
    self.meldLayer:removeAllChildren()
    self.riverLayer:removeAllChildren()
    self.tableLayer:removeAllChildren()
    self.infoLayer:removeAllChildren()

    self:renderCenter(snapshot)
    self:renderTableInfo(snapshot)
    self:renderSeats(snapshot)
    self:renderHands(snapshot)
    self:renderRivers(snapshot)
end

function TaizhouScene:renderCenter(snapshot)
    centerSprite(self.tableLayer, CENTER_NODE.background, false, 1)
    local activeSeat = snapshot.activeSeat and Geometry.localSeat(
        snapshot.activeSeat,
        snapshot.mySeat or 1,
        snapshot.chairCount or 4) or 0
    local directions = {
        {dim = CENTER_NODE.north, light = CENTER_NODE.northLight, panel = CENTER_NODE.northPanel, glow = CENTER_NODE.northGlow, seat = 4},
        {dim = CENTER_NODE.south, light = CENTER_NODE.southLight, panel = CENTER_NODE.southPanel, glow = CENTER_NODE.southGlow, seat = 2},
        {dim = CENTER_NODE.west, light = CENTER_NODE.westLight, panel = CENTER_NODE.westPanel, glow = CENTER_NODE.westGlow, seat = 1},
        {dim = CENTER_NODE.east, light = CENTER_NODE.eastLight, panel = CENTER_NODE.eastPanel, glow = CENTER_NODE.eastGlow, seat = 3},
    }
    for _, direction in ipairs(directions) do
        local active = activeSeat == direction.seat
        if active then
            centerSprite(self.tableLayer, direction.panel, false, 2)
            centerSprite(self.tableLayer, direction.glow, false, 3)
            centerSprite(self.tableLayer, direction.light, false, 4)
        else
            centerSprite(self.tableLayer, direction.dim, false, 2)
        end
    end

    addText(self.tableLayer, tostring(snapshot.remainingWallCount or 0), 960, cocosYFromTop(500), 42, {r = 226, g = 247, b = 246})
end

function TaizhouScene:renderTableInfo(snapshot)
    centerSprite(
        self.infoLayer,
        {frame = "doublekou_roomnum_bg.png", x = 139.7, topY = 117},
        false,
        1)
    addText(self.infoLayer, "房间号", 124.2, cocosYFromTop(95.5), 30, {r = 236, g = 197, b = 123}, 1, 0.5)
    addText(self.infoLayer, ":", 127.2, cocosYFromTop(95.5), 30, {r = 236, g = 197, b = 123}, 0.5, 0.5)
    addText(self.infoLayer, tostring(snapshot.roomNumber or ""), 139.2, cocosYFromTop(95.5), 30, {r = 236, g = 197, b = 123}, 0, 0.5)
    addText(self.infoLayer, "剩    余", 124.2, cocosYFromTop(138.5), 30, {r = 236, g = 197, b = 123}, 1, 0.5)
    addText(self.infoLayer, ":", 127.2, cocosYFromTop(138.5), 30, {r = 236, g = 197, b = 123}, 0.5, 0.5)
    addText(self.infoLayer, tostring(snapshot.remainingWallCount or 0), 139.2, cocosYFromTop(138.5), 30, {r = 236, g = 197, b = 123}, 0, 0.5)
    if (snapshot.roundNumber or 0) == 0 then
        centerSprite(self.infoLayer, {frame = "mah_sp_roomnum.png", x = 960, topY = 440}, false, 1)
        addText(self.infoLayer, tostring(snapshot.roomNumber or ""), 960, cocosYFromTop(502), 30, {r = 236, g = 197, b = 123}, 0.5, 0.5)
    end
    addText(self.infoLayer, "台州麻将:" .. tostring(snapshot.gameRuleDisplay or ""), 960, cocosYFromTop(200), 30, {r = 0, g = 0, b = 0}, 0.5, 0.5)
    centerSprite(self.infoLayer, {frame = "jiankangyouxi.png", x = 960, topY = 615}, false, 1)
end

function TaizhouScene:renderSeats(snapshot)
    local chairCount = snapshot.chairCount or 4
    local mySeat = snapshot.mySeat or 1
    for _, seat in ipairs(snapshot.seats or {}) do
        local position = Geometry.localSeat(seat.seatNumber, mySeat, chairCount)
        seatLabel(self.infoLayer, seat, position)
    end
end

function TaizhouScene:renderHands(snapshot)
    local visible = snapshot.visibleRound
    if not visible then
        return
    end
    local chairCount = snapshot.chairCount or 4
    local mySeat = snapshot.mySeat or 1
    for seatNumber = 1, chairCount do
        local hand = TaizhouState.hand(snapshot, seatNumber)
        if hand then
            local position = Geometry.localSeat(seatNumber, mySeat, chairCount)
            local meldCount = hand.meldCount or 0
            if seatNumber == mySeat then
                for index, value in ipairs(hand.concealedTiles or {}) do
                    local x, y, scale, pose, anchorX, anchorY = Geometry.handPosition(
                        position, index - 1, meldCount, false, #hand.concealedTiles)
                    addTile(self.handLayer, value, pose, scale, x, y, anchorX, anchorY, index,
                        TaizhouState.isJoker(snapshot, value))
                end
                if hand.drawnTile then
                    local x, y, scale, pose, anchorX, anchorY = Geometry.handPosition(
                        position, 0, meldCount, true, #hand.concealedTiles)
                    addTile(self.handLayer, hand.drawnTile, pose, scale, x, y, anchorX, anchorY, 40,
                        TaizhouState.isJoker(snapshot, hand.drawnTile))
                end
            else
                for index = 1, handSize(hand) do
                    local x, y, scale, pose, anchorX, anchorY = Geometry.handPosition(
                        position, index - 1, 0, false, handSize(hand))
                    addTile(self.handLayer, 0x72, pose, scale, x, y, anchorX, anchorY, index, false)
                end
            end
            self:renderMelds(snapshot, seatNumber, position)
        end
    end
end

function TaizhouScene:renderMelds(snapshot, seatNumber, position)
    local melds = TaizhouState.meldsForSeat(snapshot, seatNumber)
    for index, placement in ipairs(Geometry.meldPlacements(position, melds, snapshot.mySeat or 1, snapshot.chairCount or 4)) do
        addTile(
            self.meldLayer,
            placement.value,
            placement.pose,
            placement.scale,
            placement.x,
            placement.y,
            0.5,
            0.5,
            index,
            TaizhouState.isJoker(snapshot, placement.value))
    end
end

function TaizhouScene:renderRivers(snapshot)
    local visible = snapshot.visibleRound
    if not visible then
        return
    end
    local chairCount = snapshot.chairCount or 4
    local mySeat = snapshot.mySeat or 1
    for _, river in ipairs(visible.rivers or {}) do
        local position = Geometry.localSeat(river.seatNumber, mySeat, chairCount)
        local maxLineCount = river.maxLineCount or (chairCount == 2 and 2 or 3)
        for index, value in ipairs(river.tiles or {}) do
            local x, y, scale, pose, anchorX, anchorY, z = Geometry.riverPosition(
                position, index - 1, chairCount, maxLineCount)
            addTile(self.riverLayer, value, pose, scale, x, y, anchorX, anchorY, z + index,
                TaizhouState.isJoker(snapshot, value))
        end
    end
end

function TaizhouScene:create()
    return TaizhouScene.new()
end

function TaizhouScene:run()
    cc.Director:getInstance():runWithScene(self)
end

return TaizhouScene
