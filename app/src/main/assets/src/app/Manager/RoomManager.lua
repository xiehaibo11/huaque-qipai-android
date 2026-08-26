
local RoomManager = class("RoomManager", XH.ModuleBase)
RoomManager.EVENT_GAME_SCENE_EXIT = "EVENT_GAME_SCENE_EXIT"
local LobbyConfig = require("lobby.Modules.Lobby.Config")

local GAME_ROOM_TYPE = {
	NONE = 0,
	OLD = 1,
	NEW = 2
}

function RoomManager:ctor()
	RoomManager.super.ctor(self)

	self._inGame = false
	self._lastEnterType = GAME_ROOM_TYPE.NONE
end

function RoomManager:isInGame()
	return self._inGame
end

function RoomManager:isNeedLocation(gameRule)
	if not gameRule then
		return false
	end

	if device.platform == "windows" then
		return false
	end

	local kewword = XH.areaData:getLocationGameRuleKeyword()

	if not kewword then
		return false
	end

	local find = string.find(gameRule, kewword.."='1'")
    local findEx = string.find(gameRule, kewword.."=1")
	if find or findEx then
		return true
	end

	return false
end

function RoomManager:onEnterGame(data,roomMode,reconnect,bInAppID)
	local param = {}
	param.appID = data.gameappid
	param.gameID = data.gameid	
	param.roomID = data.roomid or 0
	param.srsGroupID = data.srsgroupid
	-- 金币场 4，200，包厢 10 模式
	param.sRoomMode = data.roommode
	param.roomMode = roomMode
	param.roomMode = roomMode
	param.reconnect = reconnect or false
	param.roomflag = data.roomflag or 0
	param.chaircnt = data.chaircnt or 0
	
	if bInAppID then
		param.appID = data.inappid or data.gameappid
	end
	param.nMatchAppid = data.nMatchAppid or nil
	XH.SysTool.performDelayOnce(function()
		self:gameStart(param)
	end, 0.01)

	self._inGame = true
    if XH.areaData:isSupportFriendList() then
        XH.playerData:flushPlayerPosition()
    end
end

-------------------------------------------------- 新接口 --------------------------------------------------
-- 离开游戏
function RoomManager:reqLeaveRoom(appID,srsgroupId)
    local srsGroupID = srsgroupId or XH.areaData:getSrsGroupID()
    local reqLeaveRoom = XH.GameProtocol.ReqLeaveRoom:new()
	XH.netEngine:sendProtocol(reqLeaveRoom, XH.GameProtocol.ReqLeaveRoom.processid, appID, srsGroupID)
end

function RoomManager:addSearchPath(roomData)
	--加载游戏资源搜索路径
	local configer = require("app.Config.Parse.GameSubConfiger").new()
	local prePaht = cc.FileUtils:getInstance():getWritablePath().."/hotfix/"
	local pathList = configer:getSearchPathByGameId(roomData.gameID)
	if #pathList == 0 then
		XH.roomManager:reqLeaveRoom(roomData.appID,roomData.srsGroupID)
		XH.SysTool.performDelayOnce(function()
			XH.playerData:flushPlayerPosition()
		end, 0.3)
		XH.TipTool.showTip({
			type = XH.TIP_LAYER_TYPE.OK,
		}, "本游戏当前还未开放，敬请期待")
		XH.TipTool.hideLoading()
		return false
	end
	for _,v in pairs(pathList) do
		cc.FileUtils:getInstance():addSearchPath(prePaht..v)
		cc.FileUtils:getInstance():addSearchPath(v)
	end
	return true
end

function RoomManager:enterFunc(roomData)
	self:enterGameNew(roomData)
end

function RoomManager:reqLeaveDispatchQueue(startData)
	if startData._sRoomMode ~= XH.ROOM_TYPE.DISPATCH_QUEUE then
		return
	end
	XH.lobby:getModule("Gold"):stopHeartBeatWithDispatchQueue()
	local srsGroupID = startData._srsGroupID or XH.areaData:getSrsGroupID()
	local reqLeaveDispatch = XH.DispatchProtocol.ReqLeaveDispatchQueue:new()
	reqLeaveDispatch.askid = os.time()
	reqLeaveDispatch.gameid = startData._gameID
	reqLeaveDispatch.roomflag = startData._roomFlag or 0
	reqLeaveDispatch.chaircnt = startData._chaircnt or 0
	XH.netEngine:sendProtocol(reqLeaveDispatch, XH.DispatchProtocol.ReqLeaveDispatchQueue.processid, 0, srsGroupID)
end

function RoomManager:leaveRoomFunc(roomData,bCanBack)
	if not bCanBack then
		XH.roomManager:reqLeaveRoom(roomData._appID,roomData._srsGroupID)
	end
	self:reqLeaveDispatchQueue(roomData)
	self._inGame = false
	self:dispatchEvent({name = self.EVENT_GAME_SCENE_EXIT})
	XH.playerData:flushPlayerDrop()
	XH.lobby:getModule("Set"):reload()
	XH.SysTool.performDelayOnce(function()
		XH.playerData:flushPlayerPosition()
		XH.playerData:flushGoldCoin()
	end, 0.3)
	self:clearGameManager(roomData._gameID)
	--退出游戏清除游戏的srs（如何大厅一样则不清除）
	local gameSrsID = XH.areaData:getSrsGroupIDbyAppID(roomData._appID)
	if gameSrsID and gameSrsID ~= XH.areaData:getSrsGroupID() then
		XH.netEngine:removeSRS(gameSrsID)
	end

--    if XH.playerData:getIsRealNameTourist() then --游客登录是每次退出
--        XH.playerData:reqVisitorHeart()
--    end

    if cc.UserDefault:getInstance():getBoolForKey("KW_RN_TOURIST_LOGIN") and cc.UserDefault:getInstance():getBoolForKey("KW_RN_TOURIST_LOGIN_GAME_OVER") then
        local isShowYouKe = false
        if XH.playerData:getPlayerIsAgent() and XH.areaData:getAgentShowYouKe() then
            isShowYouKe = true
		end
		XH.login:getModule("Login"):clearLoginStates()
        XH.Controller:getInstance():enterLogin(isShowYouKe)
    end

	XH.SysTool.copyString("")
    XH.bugly.buglyLog("gameLeave", tostring(roomData._gameID) or "gameLeave")
end

--新游戏入口
function RoomManager:gameStart(roomData)
    if not roomData then return end

    XH.bugly.buglyLog("gameStartJoin", tostring(roomData.gameID) or "gameStartJoin")
    if roomData.gameID == LobbyConfig.LBGameID then
        self:gameStartLiangBang(roomData)
        return
    end
	local isHotUpdate = false
    local func = function()
		-- 旁观热更新的情况下，热更新成功之后，要重新请求一次旁观
		if roomData.isWatch and isHotUpdate then
			XH.lobby:getModule('Watch'):reqRealtimeGameRecord(roomData.roomID, 0, roomData.gameID, roomData.playercount)
			return
		end
        self:enterFunc(roomData)
        XH.playerData:flushPlayerPosition()
    end
    if XH.lobby:getModule("Lobby"):isNeedHotUpdata(roomData.gameID) then
		isHotUpdate = true
        lobby.gameHotUpdateSuccessCallBack = func
    else
        func()
    end
end

-- 旁观进入
function RoomManager:watchStart(roomData)
	if not roomData then return end
	roomData.isWatch = true
	self:gameStart(roomData)
end

--两帮特殊进入方式
function RoomManager:gameStartLiangBang(roomData)
    -- require("game.Common.init")
    XH.areaData:setSrsGroupIDbyAppID(roomData.appID, roomData.srsGroupID)
    local gameStartData = require("app.Data.GameStartData").new(roomData)
    -- local leaveRoomCallFunc = function(bCanBack)
    --     self:leaveRoomFunc(gameStartData, bCanBack)
    -- end
    -- XH.gameManager = require(GameManagerClass).new({ roomData = roomData })
    -- XH.gameManager:setLeaveGameFunc(leaveRoomCallFunc)
    -- XH.gameManager:enterGame()
    local gameScene = self:gameStartScene(roomData.appID, roomData.gameID)
    if gameScene == nil then
        return
    end
    local leaveRoomCallFunc = function(bCanBack)
        self:leaveRoomFunc(gameStartData, bCanBack)
    end
    if roomData.strRecordPath ~= nil and roomData.strRecordPath ~= "" then
        gameScene:playbackStart(roomData.gameID, roomData.roomMode, roomData.roomID, roomData.strRecordPath)
    else
        gameScene:start(roomData.appID, roomData.gameID, nil, roomData.roomMode, roomData.roomID, nil, roomData.chaircnt, nil, nil, "")
    end
    gameScene:addLeaveRoomCallFunc(leaveRoomCallFunc)
end

function RoomManager:gameStartScene(appID, gameID)
    -- local CellRoomHeartBeat = require("Lobby32.src.Common.GameTeaBusiness.CellRoomHeartBeat")
    -- CellRoomHeartBeat.start(0, 1, appID)
    -- 加载游戏区公共功能
    local writablePath = cc.FileUtils:getInstance():getWritablePath()
    cc.FileUtils:getInstance():addSearchPath(writablePath .. "hotfix/res/GameCommon")
    cc.FileUtils:getInstance():addSearchPath(writablePath .. "hotfix/res/LiangBang")
    cc.FileUtils:getInstance():addSearchPath("res/GameCommon")
    cc.FileUtils:getInstance():addSearchPath("res/LiangBang")
    -- cc.FileUtils:getInstance():addSearchPath(cc.FileUtils:getInstance():getWritablePath() .."../Resources/Sound")
    local GameSceneClass = require("LiangBang.Code.GameScene.GameScene")
    local _gameScene = GameSceneClass.new()
    return _gameScene
end

function RoomManager:clearGameManager(gameID)
	if self:clearGameManagerNew(gameID) then
		return
	end
	if XH.gameManager then
		if XH.gameManager.removeAllEventListeners then
			XH.gameManager:removeAllEventListeners()
		end
		if XH.gameManager.clearSelf then
			XH.gameManager:clearSelf()
		end
		XH.gameManager = nil
	end
end

function RoomManager:checkGameNeedAllClean(type)
	if self._lastEnterType ~= GAME_ROOM_TYPE.NONE and self._lastEnterType ~= type then
		if type == GAME_ROOM_TYPE.NEW then
			self:cleanNewGameCommon()
		end
	end
	self._lastEnterType = type
end

function RoomManager:cleanNewGameByGameID(gameID)
	local GameSubByConfID = require("app.Config.GameSub").GameSubByConfID
	if GameSubByConfID[gameID] then
		local gameType = GameSubByConfID[gameID].GameType
		for k, _ in pairs(package.loaded) do
			if string.find(k, "game." .. gameType)
			or string.find(k, "game.GameBase")
			or string.find(k, "game.BaseCardGame") then
				package.loaded[k] = nil
				print("clear loaded " .. k)
			end
		end
	end
end

function RoomManager:cleanNewGameCommon()
	for k, _ in pairs(package.loaded) do
		if string.find(k, "game.Common") then
			package.loaded[k] = nil
			print("clear game file " .. k)
		end
	end
end

function RoomManager:enterGameNew(roomData)
    local configer = require("app.Config.Parse.GameSubConfiger").new()
    if not configer:isNew(roomData.gameID) then
        return false
    end
	local GameManagerClass = configer:getNewManagerClassByGameId(roomData.gameID)
    if GameManagerClass then
        if not self:addSearchPath(roomData) then
            return false
		end
		self:checkGameNeedAllClean(GAME_ROOM_TYPE.NEW)
        require("game.Common.init")
		self:clearGameManagerNew(roomData.gameID)
        XH.areaData:setSrsGroupIDbyAppID(roomData.appID, roomData.srsGroupID)
        local gameStartData = require("app.Data.GameStartData").new(roomData)
		local leaveRoomCallFunc = function(bCanBack,gameLeaveData) 
			self:leaveRoomFunc(gameStartData,bCanBack,gameLeaveData)
			if gameLeaveData.changeLevel and gameLeaveData.changeLevel.confID and gameLeaveData.changeLevel.level then 
				XH.lobby:getModule("Gold"):reqJoinGoldRoom(gameLeaveData.changeLevel.confID, gameLeaveData.changeLevel.level)
			end
		end
        XH.gameManager = require(GameManagerClass).new({roomData = roomData})
        XH.gameManager:setLeaveGameFunc(leaveRoomCallFunc)
		XH.gameManager:enterGame()
        self:setBuglyLog(roomData)
	end
	return true
end

function RoomManager:setBuglyLog(roomData)
    if XH.bugly then
        local userdata = "appid:" .. roomData.appID .. "|gameid:" .. roomData.gameID .. "|roommode:" .. roomData.roomMode .. "|roomid:" .. roomData.roomID
        XH.bugly.setUserData(userdata, "EnterGame")
    end
end

function RoomManager:clearGameManagerNew(gameID)
	local configer = require("app.Config.Parse.GameSubConfiger").new()
    if not configer:isNew(gameID) then
        return false
    end
	if XH.gameManager then
		XH.gameManager:onDestroy()
		XH.gameManager = nil
		self:cleanNewGameByGameID(gameID)
	end
	return true
end

return RoomManager  �,  