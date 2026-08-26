
local ReadGameListAndRuleConfig = {}
ReadGameListAndRuleConfig.boxGameList = {}
ReadGameListAndRuleConfig.teahouseGameList = {}
ReadGameListAndRuleConfig.gameNameList = {}
ReadGameListAndRuleConfig.boxGameRuleList = {}
ReadGameListAndRuleConfig.teahouseGameRuleList = {}
ReadGameListAndRuleConfig.groupGameList = {}

function ReadGameListAndRuleConfig.getGameNameList(lobbyid)
    if not ReadGameListAndRuleConfig._everLoadGameNameList then
        ReadGameListAndRuleConfig.loadGameNameList()
    end

    return ReadGameListAndRuleConfig.gameNameList[lobbyid]
end

function ReadGameListAndRuleConfig.getBoxRoomGameList(lobbyid)
    if not ReadGameListAndRuleConfig._everLoadBoxRoomGameList then
        ReadGameListAndRuleConfig.loadBoxRoomGameList()
    end

    return clone(ReadGameListAndRuleConfig.boxGameList[lobbyid])
end

function ReadGameListAndRuleConfig.getTeaHouseRoomGameList(lobbyid)
    if not ReadGameListAndRuleConfig._everLoadTeaHouseRoomGameList then
        ReadGameListAndRuleConfig.loadTeaHouseRoomGameList()
    end

    return ReadGameListAndRuleConfig.teahouseGameList[lobbyid]
end

function ReadGameListAndRuleConfig.getGroupRoomGameList(lobbyid)
    if not ReadGameListAndRuleConfig._everLoadGroupRoomGameList then
        ReadGameListAndRuleConfig.loadGroupRoomGameList()
    end

    return ReadGameListAndRuleConfig.groupGameList[lobbyid]
end

function ReadGameListAndRuleConfig.loadBoxRoomGameList()
    local pathKey = "configure/boxRoomGameList.json";
	if not cc.FileUtils:getInstance():isFileExist(pathKey) then
       return 
    end

    local gameListJson  = cc.FileUtils:getInstance():getStringFromFile(pathKey)
	local jsonData = {}
	if gameListJson ~= nil and gameListJson ~= "" then	
        local ok, msg = pcall(function()
            jsonData = cjson.decode(gameListJson)
        end)

        if not ok then
            print("cjson failed: " .. msg)
            return {}
        end
	end
	local tmpJsonData = {}
	for lobbyid,data in pairs(jsonData) do
		tmpJsonData[tonumber(lobbyid)] = data
    end
    
    --外省渠道包包厢场调整游戏顺序(丽水区)
    if XH.ChannelTool.checkIsWaishengChannel() and XH.areaData:getLobbyID()== XH.LOBBY_ID.LISHUI then 
        local GameIDList = {
            302872,
            30287,
            30297,
            30308,
            30446,   --二人广式
            30301,
            30299,
            30304,
            30300,
            30303,
            30298,
            30302,
            30305,
        }   
       tmpJsonData[XH.LOBBY_ID.LISHUI] = GameIDList
    end
 
    for _, value in pairs(tmpJsonData) do
        for index = #value, 1, -1 do
            if ReadGameListAndRuleConfig.judgeIsCard13ByGameId(value[index]) then
                table.remove(value, index)
            end
        end
    end
    ReadGameListAndRuleConfig.boxGameList = tmpJsonData
    ReadGameListAndRuleConfig._everLoadBoxRoomGameList = true
	return ReadGameListAndRuleConfig.boxGameList
end

function ReadGameListAndRuleConfig.loadTeaHouseRoomGameList()
    local pathKey = "configure/teahouseGameList.json";
	if not cc.FileUtils:getInstance():isFileExist(pathKey) then
       return 
    end

    local gameListJson  = cc.FileUtils:getInstance():getStringFromFile(pathKey)
	local jsonData = {}
	if gameListJson ~= nil and gameListJson ~= "" then	
        local ok, msg = pcall(function()
            jsonData = cjson.decode(gameListJson)
        end)

        if not ok then
            print("cjson failed: " .. msg)
            return {}
        end
	end
	local tmpJsonData = {}
	for lobbyid,data in pairs(jsonData) do
		tmpJsonData[tonumber(lobbyid)] = {}
		for gameid,gameName in pairs(data) do
			tmpJsonData[tonumber(lobbyid)][tonumber(gameid)] = gameName
		end
    end
    for _, value in pairs(tmpJsonData) do
        for index = #value, 1, -1 do
            if ReadGameListAndRuleConfig.judgeIsCard13ByGameId(value[index]) then
                table.remove(value, index)
            end
        end
    end
    ReadGameListAndRuleConfig.teahouseGameList = tmpJsonData
    ReadGameListAndRuleConfig._everLoadTeaHouseRoomGameList = true
	return ReadGameListAndRuleConfig.teahouseGameList
end

function ReadGameListAndRuleConfig.loadGroupRoomGameList()
    local pathKey = "configure/groupGameList.json";
	if not cc.FileUtils:getInstance():isFileExist(pathKey) then
       return 
    end

    local gameListJson  = cc.FileUtils:getInstance():getStringFromFile(pathKey)
	local jsonData = {}
	if gameListJson ~= nil and gameListJson ~= "" then	
        local ok, msg = pcall(function()
            jsonData = cjson.decode(gameListJson)
        end)

        if not ok then
            print("cjson failed: " .. msg)
            return {}
        end
	end
	local tmpJsonData = {}
	for lobbyid,data in pairs(jsonData) do
		tmpJsonData[tonumber(lobbyid)] = {}
		for gameid,gameName in pairs(data) do
			tmpJsonData[tonumber(lobbyid)][tonumber(gameid)] = gameName
		end
    end
    for _, value in pairs(tmpJsonData) do
        for index = #value, 1, -1 do
            if ReadGameListAndRuleConfig.judgeIsCard13ByGameId(value[index]) then
                table.remove(value, index)
            end
        end
    end
    ReadGameListAndRuleConfig.groupGameList = tmpJsonData
    ReadGameListAndRuleConfig._everLoadGroupRoomGameList = true
	return ReadGameListAndRuleConfig.groupGameList
end

function ReadGameListAndRuleConfig.loadGameNameList()
    local pathKey = "configure/gameNameList.json";
	if not cc.FileUtils:getInstance():isFileExist(pathKey) then
       return 
    end

    local gameListJson  = cc.FileUtils:getInstance():getStringFromFile(pathKey)
	local jsonData = {}
	if gameListJson ~= nil and gameListJson ~= "" then	
        local ok, msg = pcall(function()
            jsonData = cjson.decode(gameListJson)
        end)

        if not ok then
            print("cjson failed: " .. msg)
            return {}
        end
	end
	local tmpJsonData = {}
	for lobbyid,data in pairs(jsonData) do
		tmpJsonData[tonumber(lobbyid)] = {}
		for gameid,gameName in pairs(data) do
			tmpJsonData[tonumber(lobbyid)][tonumber(gameid)] = gameName
		end
    end

    --外省渠道包包厢场调整游戏名称(丽水区)
    if XH.ChannelTool.checkIsWaishengChannel() and XH.areaData:getLobbyID()== XH.LOBBY_ID.LISHUI then 
        local lobbyid = XH.LOBBY_ID.LISHUI --丽水
        tmpJsonData[lobbyid][30308] = "跑得快"
        tmpJsonData[lobbyid][30297] = "双扣"
    end

    
    ReadGameListAndRuleConfig.gameNameList = tmpJsonData
    ReadGameListAndRuleConfig._everLoadGameNameList = true
	return ReadGameListAndRuleConfig.gameNameList
end

function ReadGameListAndRuleConfig.loadBoxGameRule(lobbyID, gameid)
    local pathKey = "configure/"..lobbyID.."/box/"..gameid..".json";
	if not cc.FileUtils:getInstance():isFileExist(pathKey) then
       return 
    end
	local gameListJson = cc.FileUtils:getInstance():getStringFromFile(pathKey)
	local jsonData = {}
	if gameListJson ~= nil and gameListJson ~= "" then	
        local ok, msg = pcall(function()
            jsonData = cjson.decode(gameListJson)
        end)

        if not ok then
            print("cjson failed: " .. msg)
            return {}
        end
	end
	ReadGameListAndRuleConfig.boxGameRuleList[gameid] = jsonData
	return ReadGameListAndRuleConfig.boxGameRuleList[gameid]
end

function ReadGameListAndRuleConfig.loadTeaHouseGameRule(lobbyID, gameid)
    local pathKey = "configure/"..lobbyID.."/teahouse/"..gameid..".json";
	if not cc.FileUtils:getInstance():isFileExist(pathKey) then
       return 
    end
	local gameListJson = cc.FileUtils:getInstance():getStringFromFile(pathKey)
	local jsonData = {}
	if gameListJson ~= nil and gameListJson ~= "" then	
        local ok, msg = pcall(function()
            jsonData = cjson.decode(gameListJson)
        end)

        if not ok then
            print("cjson failed: " .. msg)
            return {}
        end
	end
	ReadGameListAndRuleConfig.teahouseGameRuleList[gameid] = jsonData
	return ReadGameListAndRuleConfig.teahouseGameRuleList[gameid]
end

--通过gameid判断是否是Card13
function ReadGameListAndRuleConfig.judgeIsCard13ByGameId(gameid)
    local GameSubByConfID = require("app.Config.GameSub").GameSubByConfID
    for _, value in pairs(GameSubByConfID) do
        if value.ConfID == gameid then
            if value.GameType ~= "Card13" then
                return false
            else
                return true
            end
        end
    end
    return true
end


return ReadGameListAndRuleConfig�"  