local luaj = require("cocos.cocos2d.luaj")

local CocosRuntime = {}
local BRIDGE = "com.nanbeiyule.game.CocosLuaBridge"

function CocosRuntime.roomNumber()
    local ok, value = luaj.callStaticMethod(BRIDGE, "getRoomNumber", {}, "()Ljava/lang/String;")
    return ok and value or ""
end

function CocosRuntime.requestSnapshot(roomNumber, callback)
    return luaj.callStaticMethod(
        BRIDGE,
        "requestSnapshot",
        {roomNumber or CocosRuntime.roomNumber(), callback},
        "(Ljava/lang/String;I)V")
end

function CocosRuntime.submitCommand(roomNumber, commandType, payloadJson, expectedRevision, callback)
    return luaj.callStaticMethod(
        BRIDGE,
        "submitCommand",
        {
            roomNumber or CocosRuntime.roomNumber(),
            commandType,
            payloadJson or "{}",
            tostring(expectedRevision or 0),
            callback,
        },
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V")
end

return CocosRuntime
