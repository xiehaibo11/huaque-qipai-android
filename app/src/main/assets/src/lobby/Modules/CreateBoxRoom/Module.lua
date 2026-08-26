local CreateBoxRoomModule = class("CreateBoxRoomModule", XH.ModuleBase)
local TaskDefine = require("app.Define.TaskDefine")
local MutiVersionGameConfig =  require("lobby.Config.MutiVersionGameConfig")
local GameNewTypeConfiger = require("app.Config.Parse.GameNewTypeConfiger")

local KW_LIMIT_DISTANCE = 200

CreateBoxRoomModule.EVENT_SYSTEM_TIME_SUCCESS = "EVENT_SYSTEM_TIME_SUCCESS"
CreateBoxRoomModule.EVENT_HIDE_CREATEBOXROOM = "EVENT_HIDE_CREATEBOXROOM"

function CreateBoxRoomModule:ctor()
    CreateBoxRoomModule.super.ctor(self)
	self._isCreateNeedLoaction = false
	self._timeStamp = 0
	self._isCreating = false
end

function CreateBoxRoomModule:onCreateView()
	self._timeStamp = os.time()
	self:RequestSysTime()
end

function CreateBoxRoomModule:getReqConfig()
    return {
        RequestSysTime = { reqPath = "app.Req.Task.ReqTaskPercent", callBack = self.onReqSysTemsCallBack },
        ReqCreate = { reqPath = "lobby.Req.Room.ReqCreate", callBack = self.onCreateBoxRoomCallBack },
    }
end

function CreateBoxRoomModule:getProxyEvents()
    return {
        { module = XH.sdkManager, eventKeyName = "EVENT_AMAP_CALLBACK", callBack = "onGetLocationCallBack" },
    }
end

function CreateBoxRoomModule:RequestSysTime()
    self:startReq("RequestSysTime", XH.playerData:getNumberID(), 0, 120, TaskDefine.KW_SYSTEM_TIME, XH.areaData:getSrsGroupID(), 10)
end
    
function CreateBoxRoomModule:onReqSysTemsCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local timestamp = json.decode(data.acData).time
        if timestamp ~= nil then
            self:dispatchEvent({name = CreateBoxRoomModule.EVENT_SYSTEM_TIME_SUCCESS, data = timestamp})
        end
    end
end

function CreateBoxRoomModule:reqCreateBoxRoom(gameID, gameConditions, gameRule, roomMode, playerCount, srsGroupID, vipPropId)
	if self._isCreating then return end --为了防止安卓点击多次创建按钮，进游戏后显示错误，没有房间信息
	self._isCreating = true
	XH.TipTool.showLoading()
	if gameID == MutiVersionGameConfig.MutiVersionGameID.LSMahjong2New then
		gameID = 30287
	end
	local Reqfunc = function(gameRuleEx)
		if gameRuleEx then
			gameRule = gameRule..gameRuleEx
		end
		local conditions = { }
		conditions.group = tostring(gameID)
		conditions.GamePlayerCount = tostring(playerCount)
		conditions.CanCreate = "1"
		conditions.roommode = XH.ROOM_TYPE.BOX_ROOM
		for k, v in pairs(gameConditions) do
			conditions[k] = v
		end
        self:startReq("ReqCreate", gameID, conditions, gameRule, roomMode, srsGroupID, vipPropId, 15)
		print("gameRule：", gameRule)
	end

	local createFunc = function ()
		self._isCreateNeedLoaction = XH.roomManager:isNeedLocation(gameRule)
		if self._isCreateNeedLoaction then
			XH.TipTool.showLoading()
			self._tempCreateCallback = Reqfunc
			if device.platform == "android" then
				XH.sdkManager:callFunction("user_initiative")
			end
			XH.sdkManager:startLocationByAmap(XH.sdkManager.AmapAccuracy.MID)
		else
			Reqfunc()
		end
        XH.lobby:getModule("ToponAct"):setHotUpdata(false)
	end

	if XH.lobby:getModule("Lobby"):isNeedHotUpdata(gameID) then
		lobby.gameHotUpdateSuccessCallBack = createFunc
        XH.lobby:getModule("ToponAct"):setHotUpdata(true)
	else
		createFunc()
	end
end

function CreateBoxRoomModule:onCreateBoxRoomCallBack(reqCreate, type, data)
	self._isCreating = false
	if type == XH.Req.TYPE.SUCCESS then
		self:dispatchEvent({name = CreateBoxRoomModule.EVENT_HIDE_CREATEBOXROOM})
		XH.roomManager:onEnterGame(data,XH.ROOM_MODE.BOXROOM)
	elseif type == XH.Req.TYPE.FAIL then
		local func = nil
		if data.errorcode == XH.RoomProtocol.ERRORCODE.NOT_ENOUGH_PROP then
			local Config = require("lobby.Modules.QuickRecharge.Config")
			XH.lobby:getModule("QuickRecharge"):reqSpecialSceneGoods(Config.SCENE_NAME.KW_BOX_CREATEROOM)
		else
			if data.errorcode == XH.RoomProtocol.ERRORCODE.ERROR_UNBINDWECHAT then
				func = function()
					XH.login:getModule("Login"):reqWXBind()
				end
			end
			XH.TipTool.showTip({
				type = XH.TIP_LAYER_TYPE.OK,
				funcOK = func
			}, data.msg)
		end
	elseif type == XH.Req.TYPE.TIMEOUT then
		XH.TipTool.showTip({
			type = XH.TIP_LAYER_TYPE.OK,
		}, "请求超时")
	end
	XH.TipTool.hideLoading()
end

function CreateBoxRoomModule:onGetLocationCallBack(event)
    local code = event.data.code
	local msg = event.data.msg
	
	XH.TipTool.hideLoading()
	local longitude, latitude, bOpenGPS = 0, 0, 0
    if code == XH.ThirdDefine.AMAP_CALLBACK_CODE.AMAP_SUCCESS then
        local tData

        local ok = pcall(function()
            tData = cjson.decode(msg)
        end)

        if not ok then
            return
        end
        
        longitude = tData["longitude"]
        latitude = tData["latitude"]
		bOpenGPS = 1
    else
		if self._isCreateNeedLoaction and self._tempCreateCallback then
			if not XH.sdkManager:guideOpenGpsServer() then
				XH.TipTool.showTip({
					type = XH.TIP_LAYER_TYPE.OK,
					funcOK = function()
            	        self._isCreating = false
						if device.platform == "ios" then
							XH.sdkManager:xhCallOCFunc("XH_Extern", "GolinkQx", "{}")
						elseif device.platform == "android" then
							XH.sdkManager:callFunction("jump_permission_system")
						end
            	    end,
            	    funcClose = function()
            	        self._isCreating = false
            	    end,
				}, "您已经关闭了位置权限,需要您在系统设置中重新开启")
			else
				self._isCreating = false
			end
			XH.TipTool.hideLoading()
		end
	end

	local strGPS = "longitude='" .. longitude .."';".. "latitude='" .. latitude .."';bOpenGPS='" .. bOpenGPS .."';"

	if self._isCreateNeedLoaction and bOpenGPS == 1 and self._tempCreateCallback then
		local gameRuleEx = "bCheckPos=" .. bOpenGPS  .. ";" .."distance='" .. KW_LIMIT_DISTANCE .. "';" .. strGPS
		self._tempCreateCallback(gameRuleEx)
		self._tempCreateCallback = nil
	end
	
	self._isCreateNeedLoaction = false
end

function CreateBoxRoomModule:getGameListData()
	local gameIdList = XH.areaData:getBoxRoomGameList()
	local gameNameList = XH.areaData:getAreaGameNameList()
    if not gameIdList or not gameNameList then
        return nil
	end
	self:sortGameList(gameIdList)
	local showGameList = {}
	for _, gameID in ipairs(gameIdList) do
		if gameNameList[gameID] then
			local singleGame = {}
			singleGame.gameID = gameID
			singleGame.gameName = gameNameList[gameID]
			table.insert(showGameList, singleGame)
		end
	end

	return showGameList
end

function CreateBoxRoomModule:getActivityLeftTime()
	local _, endTime = XH.areaData:getActivityTime()
	return endTime - self._timeStamp
end

--对标签排序，活动的放在最下面
function CreateBoxRoomModule:sortGameList(gameIdList)
    local boxActivityGame = XH.areaData:getBoxActivityGame()
	local GameSubByConfID = require("app.Config.GameSub").GameSubByConfID
    local tempList = {} --浙江包游戏可能少于企业包
	local DoubleKouList = {}
    for index = #gameIdList, 1, -1 do
        for tIndex = 1, #boxActivityGame do
            if gameIdList[index] == boxActivityGame[tIndex] and not GameNewTypeConfiger:getIsNewPlayTypGame(gameIdList[index]) then
                table.insert(tempList, gameIdList[index])
                table.remove(gameIdList, index)
            end
        end
		if XH.ChannelTool.checkIsDoubleKouGame() and GameSubByConfID[gameIdList[index]].GameType == "DoubleKou" then
			table.insert(DoubleKouList, gameIdList[index])
			table.remove(gameIdList, index)
		end
    end
    for index = 1, #tempList do
        table.insert(gameIdList,tempList[index])
    end
    for index = 1, #DoubleKouList do	-- 双扣渠道包，双扣游戏放在第一位
        table.insert(gameIdList , 1, DoubleKouList[index])
    end
end

--是否是限时活动游戏
function CreateBoxRoomModule:isActivityGame(gameid)
    local boxActivityGame = XH.areaData:getBoxActivityGame()
    local startTime, endTime = XH.areaData:getActivityTime()
    --活动是否开始
    if startTime > self._timeStamp or endTime < self._timeStamp then
        return false
    end
    for _, value in pairs(boxActivityGame) do
        if gameid == value then
            return true 
        end
    end
    return false
end

-- 是否只是显示角标
function CreateBoxRoomModule:isOnlyShowActGame(gameid)
	local showGameList = {30133,30140,30145,30153}
	for k, v in pairs(showGameList) do
		if v == gameid then
			return true
		end
	end
	return false
end

--乐清，麻将游戏，2020-7-1日至2020-7-4日 限免
function CreateBoxRoomModule:isLimitTimeFree()
	return XH.areaData:getLobbyID() == XH.LOBBY_ID.YUEQING and self._timeStamp >= 1593532800 and self._timeStamp <= 1593792000
end

return CreateBoxRoomModule #  