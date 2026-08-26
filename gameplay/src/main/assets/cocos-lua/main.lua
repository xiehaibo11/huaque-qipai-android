cc.FileUtils:getInstance():setPopupNotify(false)
require("cocos.init")

local ok, message = xpcall(function()
    require("mahjong.TaizhouScene"):create():run()
end, function(err)
    return debug.traceback(err, 2)
end)

if not ok then
    print(message)
end
