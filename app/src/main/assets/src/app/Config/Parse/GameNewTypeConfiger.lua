local GameNewTypeCfg = require("app.Config.GameNewTypeConfig").GameNewTypeConfByGameID

local GameNewTypeConfiger = class("GameNewTypeConfiger")

function GameNewTypeConfiger:getIsNewPlayTypeArea(lobbyID)
    if lobbyID == 900038 then
        return false
    end
    if not lobbyID then
        return false
    end
	for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID then
            return true
        end
    end
    return false
end

function GameNewTypeConfiger:getIsNewPlayTypGame(gameID)
    if not gameID then
        return false
    end
	if GameNewTypeCfg[gameID] then
        return true
    end
    return false
end

function GameNewTypeConfiger:getShowExtraRuleGameIDByLobbyID(lobbyID)
    if not lobbyID then
        return 0
    end
    for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID and v.ShowExtraRule then
            return v.GameID
        end
    end
	return 
end

function GameNewTypeConfiger:getExtraRuleByGameID(gameID)
    if not gameID or not GameNewTypeCfg[gameID] then
        return { }
    end
	return GameNewTypeCfg[gameID].ExtraRulePng
end

function GameNewTypeConfiger:getShowExtraRule(gameID)
    if not gameID or not GameNewTypeCfg[gameID] then
        return false
    end
    return GameNewTypeCfg[gameID].ShowExtraRule
end

function GameNewTypeConfiger:getShowExtraRuleInCreate(gameID)
    if not gameID or not GameNewTypeCfg[gameID] then
        return false
    end
    return GameNewTypeCfg[gameID].ShowExtraRuleInCreate
end

function GameNewTypeConfiger:getDefultGameIDByLobbyID(lobbyID)
    if not lobbyID then
        return 0
    end
    local defultGameID = 0
    for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID and v.DefultChoose then
            defultGameID = v.GameID
            break
        end
    end
    return defultGameID
end

function GameNewTypeConfiger:getNewPlayTypeNodeByGameID(gameID)
    if not gameID or not GameNewTypeCfg[gameID] then
        return
    end
    return GameNewTypeCfg[gameID].NewPlayTypeNode
end

function GameNewTypeConfiger:getGuideTablesByLobbyID(lobbyID)
    if not lobbyID then
        return { }
    end
    local guideTables = { }
    for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID and v.ShowGuideTableInTea then
            guideTables[v.GameID] = v.ShowGuideTableInTeaWithGameID
        end
    end
    return guideTables
end

function GameNewTypeConfiger:getCreateTipsByGameIDAndScene(gameID, scene)
    if not gameID or not GameNewTypeCfg[gameID] or not scene then
        return
    end
    local tipsLuaString = GameNewTypeCfg[gameID].CreateTips
    if tipsLuaString == "" then
        return
    end
    local tipsTable
    local ok = pcall(function()
        tipsTable = loadstring("return " .. tipsLuaString)()
    end)
    if not ok then
        return
    end
    if tipsTable[scene] and tipsTable[scene] ~= "" then
        return tipsTable[scene]
    end
    return
end

function GameNewTypeConfiger:getCreateWithoutNewPlayTypeContentByGameID(gameID)
    if not gameID or not GameNewTypeCfg[gameID] then
        return ""
    end
    return GameNewTypeCfg[gameID].CreateWithoutNewPlayTypeTips
end

function GameNewTypeConfiger:getShowExtraRuleTipsByGameID(gameID)
    if not gameID or not GameNewTypeCfg[gameID] then
        return
    end
    return GameNewTypeCfg[gameID].ShowExtraRuleTip
end

function GameNewTypeConfiger:getShowHotFlagByGameID(gameID)
    if not gameID or not GameNewTypeCfg[gameID] then
        return
    end
    return GameNewTypeCfg[gameID].ShowHotFlag
end

function GameNewTypeConfiger:getExtraRuleTipsPosByGameIDAndPlayerCnt(gameID, playerCnt)
    if not gameID or not GameNewTypeCfg[gameID] or not playerCnt then
        return
    end
    local pos
    local ok = pcall(function()
        pos = loadstring("return " .. GameNewTypeCfg[gameID].ExtraRuleTipPos)()
    end)
    if ok and pos and pos[playerCnt] and next(pos[playerCnt]) then
        return pos[playerCnt]
    end
    return
end

function GameNewTypeConfiger:getHotFlagPosByGameID(gameID)
    if not gameID or not GameNewTypeCfg[gameID] then
        return
    end
    local pos
    local ok = pcall(function()
        pos = loadstring("return " .. GameNewTypeCfg[gameID].HotFlagPos)()
    end)
    if ok and pos and next(pos) then
        return pos
    end
    return
end

function GameNewTypeConfiger:getNewPlayTypeTextByLobbyID(lobbyID)
    if not lobbyID then
        return
    end
    for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID and v.NewPlayTypeText and next(v.NewPlayTypeText) then
            return v.NewPlayTypeText
        end
    end
    return
end

function GameNewTypeConfiger:getToBActRuleByLobbyID(lobbyID)
    if not lobbyID then
        return
    end
    for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID and v.ToBActRuleText and next(v.ToBActRuleText) then
            return v.ToBActRuleText
        end
    end
    return
end

function GameNewTypeConfiger:getToCActRuleByLobbyID(lobbyID)
    if not lobbyID then
        return
    end
    for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID and v.ToCActRuleText and next(v.ToCActRuleText) then
            return v.ToCActRuleText
        end
    end
    return
end

function GameNewTypeConfiger:getToBActRuleNewByLobbyID(lobbyID)
    if not lobbyID then
        return
    end
    for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID and v.ToBActRuleTextNew and next(v.ToBActRuleTextNew) then
            return v.ToBActRuleTextNew
        end
    end
    return
end

function GameNewTypeConfiger:getGuideTableChairsByGameID(gameID)
    if not gameID or not GameNewTypeCfg[gameID] then
        return 0
    end
    return GameNewTypeCfg[gameID].GuideTableChairs
end

function GameNewTypeConfiger:getNewPlayTypeLobbyTipsByLobbyID(lobbyID)
    if not lobbyID then
        return
    end
    for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID and v.LobbyTips and v.LobbyTips ~= "" then
            return v.LobbyTips
        end
    end
    return
end

function GameNewTypeConfiger:getNewPlayTypeApplyGamesByLobbyID(lobbyID)
    if not lobbyID then
        return
    end
    local str = ""
    for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID and v.GameName and v.GameName ~= "" then
            if str ~= "" then
                str = str .. "、"
            else
                str = str .. "适用游戏："
            end
            str = str .. v.GameName
        end
    end
    return str
end

function GameNewTypeConfiger:getShowNewPlayTypeTableGuideByGameID(gameID)
    if not gameID or not GameNewTypeCfg[gameID] then
        return false
    end
    if GameNewTypeCfg[gameID].ShowNewPlayTypeGuide then
        return GameNewTypeCfg[gameID].ShowNewPlayTypeGuide
    end
    return false
end

function GameNewTypeConfiger:getShowNewPlayTypeTableGuideTipsByGameID(gameID)
    if not gameID or not GameNewTypeCfg[gameID] then
        return ""
    end
    if GameNewTypeCfg[gameID].NewPlayTypeGuideTips then
        return GameNewTypeCfg[gameID].NewPlayTypeGuideTips
    end
    return ""
end

function GameNewTypeConfiger:getNewPlayTypeGameIndexByGameID(gameID)
    if not gameID or not GameNewTypeCfg[gameID] then
        return 0
    end
    for k, v in pairs(GameNewTypeCfg) do
        if gameID == v.GameID then
            return k
        end
    end
    return 0
end

function GameNewTypeConfiger:getAutoCreatePlayModeInfoByLobbyID(lobbyID)
    local res = {}
    if not lobbyID then
        return
    end
    for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID and v.AutoCreatePlayMode then
            res.gameID            = v.GameID
            res.gameName          = v.GameName
            res.showWithGame      = v.AutoCreatePlayModeWithGameID
            res.gameRule          = v.AutoPlayModeGameRule
            res.playCounts        = v.AutoPlayModePlayCounts
            res.playerCounts      = v.AutoPlayModePlayerCounts
            res.zhRule            = v.AutoPlayModeZhRule
            res.tableColor        = v.AutoPlayModeTableColor
            res.playModeID        = v.AutoPlayModeID
            res.playCountsLock    = v.AutoPlayModePlayCountsLock
            res.playerCountsLock  = v.AutoPlayModePlayerCountsLock
            break
        end
    end
    if next(res) then return res end
    return
end

function GameNewTypeConfiger:getMemberActIconByLobbyID(lobbyID)
    if not lobbyID then
        return
    end
    for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID and v.NewPlayTypeDrawActIcon then
            return v.NewPlayTypeDrawActIcon
        end
    end
    return
end

function GameNewTypeConfiger:getOwnerActIconByLobbyID(lobbyID)
    if not lobbyID then
        return
    end
    for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID and v.NewPlayTypeOwnerActIcon then
            return v.NewPlayTypeOwnerActIcon
        end
    end
    return
end

function GameNewTypeConfiger:getLobbyTeaFlagImgByLobbyID(lobbyID)
    if not lobbyID then
        return ""
    end
    for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID and v.LobbyTeaFlagImg then
            return v.LobbyTeaFlagImg
        end
    end
    return ""
end

function GameNewTypeConfiger:getLobbyBoxFlagImgByLobbyID(lobbyID)
    if not lobbyID then
        return ""
    end
    for _, v in pairs(GameNewTypeCfg) do
        if lobbyID == v.LobbyID and v.LobbyBoxFlagImg then
            return v.LobbyBoxFlagImg
        end
    end
    return ""
end

return GameNewTypeConfiger'  