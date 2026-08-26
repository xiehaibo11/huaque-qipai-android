require("cocos.cocos2d.json")
local json = _G.json

local TaizhouState = {}

function TaizhouState.decode(payload)
    local ok, value = pcall(json.decode, payload or "")
    if not ok or type(value) ~= "table" then
        return nil, "牌局响应无法解析"
    end
    if value.ok ~= true then
        return nil, value.error or "牌局响应失败"
    end
    return value
end

function TaizhouState.hand(snapshot, seatNumber)
    local visible = snapshot.visibleRound
    if not visible or not visible.hands then
        return nil
    end
    for _, hand in ipairs(visible.hands) do
        if hand.seatNumber == seatNumber then
            return hand
        end
    end
    return nil
end

function TaizhouState.river(snapshot, seatNumber)
    local visible = snapshot.visibleRound
    if not visible or not visible.rivers then
        return nil
    end
    for _, river in ipairs(visible.rivers) do
        if river.seatNumber == seatNumber then
            return river
        end
    end
    return nil
end

function TaizhouState.meldsForSeat(snapshot, seatNumber)
    local result = {}
    for _, meld in ipairs(snapshot.melds or {}) do
        if meld.seat == seatNumber then
            result[#result + 1] = meld
        end
    end
    return result
end

function TaizhouState.isJoker(snapshot, tileValue)
    local visible = snapshot and snapshot.visibleRound
    for _, value in ipairs((visible and visible.jokerTiles) or {}) do
        if value == tileValue then
            return true
        end
    end
    return false
end

return TaizhouState
