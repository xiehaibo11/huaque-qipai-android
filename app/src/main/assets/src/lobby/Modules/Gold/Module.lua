local GoldModule = class("GoldModule", XH.ModuleBase)
local TaskDefine = require("app.Define.TaskDefine")
local ConfigurationDefine = require("lobby.Modules.Configuration.Define")

GoldModule.EVENT_REFRESH_GOLD_SYSTEMTIME = "EVENT_REFRESH_GOLD_SYSTEMTIME"
GoldModule.EVENT_CLOSE_NEW_USER_GUIDE_VIEW = "EVENT_CLOSE_NEW_USER_GUIDE_VIEW"
local inAppID = 0
function GoldModule:ctor()
    GoldModule.super.ctor(self)
    self._confID = nil
    self._roomFlag = -1
end

function GoldModule:getProxyEvents()
    return {
        {module = XH.login:getModule("Login"), eventKeyName = "EVENT_LOGIN_SUCCESS_CHANGE_GROUP", callBack = "onLoginCallback"},
        {module = XH.netEngine, eventName = XH.DispatchProtocol.RespDispatchAppIDList.event_key, callBack = "onRespDispatchAppIDList"}
    }
end

function GoldModule:getReqConfig()
    return {
        ReqSystemTimes = {reqPath = "app.Req.Task.ReqTaskPercent", callBack = self.onReqTaskPercentCallBack},
        ReqJoinGoldRoom = {reqPath = "lobby.Req.Room.ReqJoinGoldRoom", callBack = self.onJoinGoldRoomCallBack},
        ReqJoinDispatchQueue = {reqPath = "lobby.Req.Room.ReqJoinDispatchQueue", callBack = self.onJoinDispatchQueueCallBack},
        LinkReqJoinRoomGT = {reqPath = "lobby.Req.Room.LinkReqJoinRoomGT", callBack = self.onJoinRoomGTCallBack}
    }
end

function GoldModule:RequestSysTime()
    self:startReq("ReqSystemTimes", XH.playerData:getNumberID(), 0, 120, TaskDefine.KW_SYSTEM_TIME, XH.areaData:getSrsGroupID(), 10)
end

function GoldModule:onReqTaskPercentCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local timestamp = json.decode(data.acData).time
        if timestamp ~= nil then
            self:dispatchEvent({name = GoldModule.EVENT_REFRESH_GOLD_SYSTEMTIME, data = timestamp})
        end
    end
end

function GoldModule:reqJoinGoldRoom(confID,roomFlag,SrsGroupId)
	self._confID = confID or 0
	self._roomFlag = roomFlag or -1
    local roominfo = XH.goldConfigManager:getGoldRoomInfoByIndex(confID)
    if roominfo and roominfo.roomInfo and type(roominfo.roomInfo.SrsGroupId) == "table" then 
        SrsGroupId = SrsGroupId or roominfo.roomInfo.SrsGroupId[roomFlag]
    end
    local srsGroupID = SrsGroupId or XH.areaData:getGoldSrsGroupID()
    XH.TipTool.showLoading()

	if srsGroupID ~= XH.areaData:getSrsGroupID() then
        XH.login:getModule("Login"):changeGroupSessionLogin(srsGroupID)
    else
        self:startJoinGoldRoom(srsGroupID)
    end
end


function GoldModule:startJoinGoldRoom(srsGroupID)
	local goldRoomInfo = XH.goldConfigManager:getGoldRoomInfoByIndex(self._confID)
	srsGroupID = srsGroupID or XH.areaData:getGoldSrsGroupID()
	local gameId = goldRoomInfo.roomInfo.GameID
    if self._roomFlag > 0 and goldRoomInfo.roomLevelInfos[self._roomFlag] then 
        gameId = goldRoomInfo.roomLevelInfos[self._roomFlag].gameid
    end
	local areaTypeID = goldRoomInfo.roomInfo.AreaTypeID
	local chaircnt = goldRoomInfo.roomInfo.PlayerCount
	local goldMode = goldRoomInfo.roomInfo.GoldMode
	local roomnameflag = self._roomFlag
    if goldRoomInfo.roomInfo.SpecialPlayer and goldRoomInfo.roomInfo.SpecialPlayer ==1 and self._roomFlag > 0 and goldRoomInfo.roomLevelInfos[self._roomFlag] then
        roomnameflag = -1
        chaircnt = goldRoomInfo.roomLevelInfos[self._roomFlag].chaircnt
    end
    --如果SrsGroupId ~= XH.areaData:getGoldSrsGroupID()，请求一下位置信息
    --从这里开始写，但你好像还需要个SRS地址返回给你
    if goldRoomInfo.roomInfo.DispatchQueue and goldRoomInfo.roomInfo.DispatchQueue == 1 then
		self:reqDispatchAppidList(srsGroupID)
    elseif self._roomFlag > 0 and goldRoomInfo.roomLevelInfos and goldRoomInfo.roomLevelInfos[self._roomFlag] and goldRoomInfo.roomLevelInfos[self._roomFlag].area == "GameTea" then
        local rooms = goldRoomInfo.roomLevelInfos[self._roomFlag].room
        if inAppID ~= 0 then 
            local inRoom = nil
            for i = 1,#rooms do
                if rooms[i].appid == inAppID then
                    inRoom = {appid = rooms[i].appid,roomid = rooms[i].roomid}
                    break
                end
            end
            if inRoom then 
                rooms = {}
                rooms[1] = inRoom
            end
            inAppID = 0
        end
        self:startReq("LinkReqJoinRoomGT", gameId,rooms,chaircnt,srsGroupID,15)
    else 
        self:startReq("ReqJoinGoldRoom", gameId,areaTypeID,chaircnt,goldMode,srsGroupID,roomnameflag,15)
    end
end

function GoldModule:onLoginCallback(event)
    if self._confID and self._confID ~= 0 then
        self:startJoinGoldRoom(event.msg.srsGroupID)
    end
    XH.goldConfigManager:onLoginCallback()
end

function GoldModule:canChangeRoomLevel()
    local confID = self._confID
    local level = self._roomFlag
    local playerSR = XH.playerData:getSR() or 0

    local goldRoomInfo = XH.goldConfigManager:getGoldRoomInfoByIndex(confID)
    if #goldRoomInfo.roomLevelInfos < 2 then
        return false
    end
    local _levelRoom = goldRoomInfo.roomLevelInfos
    for _, roominfo in pairs(_levelRoom) do
        if level == roominfo.roomnameflag then
            if playerSR < roominfo.minrich then
                if self:changeRoomLevel(confID, level, true, _levelRoom, "金币过低,是否进入更低场次") then
                    return true
                end
            elseif playerSR > roominfo.maxrich then
                if self:changeRoomLevel(confID, level, false, _levelRoom, "土豪，当前场次已经不符合您高贵气质了，请前往更豪华场次！") then
                    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.GOLD_GAME_5)
                    return true
                end
            end
        end
    end
    return false
end

function GoldModule:changeRoomLevel(ConfID, level, bLow, _levelRoom, tipText)
    local resultLevel = self:selectMatchRoom(level, bLow, _levelRoom)
    local callFunc = function()
        self._roomFlag = resultLevel
        self:reqJoinGoldRoom(ConfID, resultLevel)
    end
    if resultLevel ~= level then
        XH.TipTool.showTip(
            {
                type = XH.TIP_LAYER_TYPE.OK,
                funcOK = callFunc
            },
            tipText
        )
        return true
    end
    return false
end

function GoldModule:recordLastRoomInfo()
    XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_JOIN_GOLD_LEVEL_WITH_LOBBY, self._roomFlag)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_JOIN_GOLD_INDEX, self._confID)
    local goldRoomInfo = XH.goldConfigManager:getLastGoldRoomInfo()
    if goldRoomInfo then
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_LAST_GOLD_GAMEID, goldRoomInfo.roomInfo.GameID)
    end
end

function GoldModule:clearLastRoomInfo()
    self._confID = nil
    self._roomFlag = -1
end

function GoldModule:onJoinGoldRoomCallBack(reqJoin, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        XH.roomManager:onEnterGame(data, XH.ROOM_MODE.MOBILE)
        self:recordLastRoomInfo()
    elseif type == XH.Req.TYPE.FAIL then
        if data.errorcode == XH.RoomProtocol.ERRORCODE.ERROR_INAPPID or
            data.errorcode == XH.RoomProtocol.ERRORCODE.NOT_FOUND_GAME_ID then
            XH.TipTool.showTip(
                {
                    type = XH.TIP_LAYER_TYPE.OK,
                    funcOK = function()
                        if data.errorcode == XH.RoomProtocol.ERRORCODE.NOT_FOUND_GAME_ID then 
                            inAppID = data.inappid
                            data = self:getRoomDataByAppid(data.inappid)
                            local index = XH.goldConfigManager:getIndexByGameid(data.belonggameid)
                            self:reqJoinGoldRoom(index,data.roomflag,data.srsgroupid)
                        else
                            XH.roomManager:onEnterGame(data, XH.ROOM_MODE.MOBILE, true, true)
                        end
                    end
                },
                "已在游戏房间中,点击确认返场"
            )
        elseif data.errorcode == XH.RoomProtocol.ERRORCODE.ERROR_IN_MATCH_LIST then
            XH.TipTool.showTip(
                {
                    type = XH.TIP_LAYER_TYPE.OK,
                    funcOK = function()
                        XH.roomManager:onEnterGame(data, XH.ROOM_MODE.MOBILE, false, true)
                    end
                },
                "已在匹配队列,点击确认返场"
            )
        elseif data.errorcode == XH.RoomProtocol.ERRORCODE.ERROR_NOT_ENOUGH_SR then
            self:judgeQuickRecharge()
        elseif data.errorcode == XH.RoomProtocol.ERRORCODE.ERROR_TOO_MUCH_SR then
            -- 是否需要转场
            if not self:canChangeRoomLevel() then
                XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, XH.ConstString.getStr("ERROR_TOO_MUCH_SR"))
            end
        else
            XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, data)
        end
    elseif type == XH.Req.TYPE.TIMEOUT then
        XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, "请求超时")
    end
    self:clearLastRoomInfo()
    XH.TipTool.hideLoading()
end

function GoldModule:onJoinRoomGTCallBack(reqJoin, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        data.cellData.roomflag = self._roomFlag
        XH.roomManager:onEnterGame(data.cellData, XH.ROOM_MODE.MOBILE)
        self:recordLastRoomInfo()
    elseif type == XH.Req.TYPE.FAIL then
        if data and #data > 0 then 
            XH.TipTool.showTip(
                {
                    type = XH.TIP_LAYER_TYPE.OK,
                    funcOK = function()
                        -- 这里不能直接进房间了，要把房间信息拿到
                        local inData = {}
                        local place = data[1]
                        inData.roomid = place.roomid
                        inData.gameappid =  place.gameappid 
                        inData.chairid = place.chairid
                        inData.nMatchAppid = place.gameappid 
                        inData.inappid = place.gameappid 
                        inData.tableid = place.tableid
                        inData.state = place.state
                        inData.chaircnt = place.chaircnt 
                        inData.srsgroupid = XH.areaData:getGoldSrsGroupID()
                        inData.askid = 0
                        inData.roommode = place.roommode
                        inData.gameid = place.gameid
                        if inData.gameid == 0 then 
                            inAppID = inData.inappid
                            inData = self:getRoomDataByAppid(inData.inappid)
                            local index = XH.goldConfigManager:getIndexByGameid(inData.belonggameid)
                            self:reqJoinGoldRoom(index,inData.roomflag,inData.srsgroupid)
                        else 
                            XH.roomManager:onEnterGame(inData, XH.ROOM_MODE.MOBILE, true, true)
                        end
                        --inData.srsgoupid = 0
                    end
                },
                "已在游戏房间中,点击确认返场"
            )
        else
            if self._confID and string.match(reqJoin:getMessage(), "金币过低") then
                local goldRoomInfo = XH.goldConfigManager:getGoldRoomInfoByIndex(self._confID)
                if goldRoomInfo and XH.lobby:getModule("YGiftBankruptcy"):isShowGiftEnter(goldRoomInfo.roomLevelInfos[self._roomFlag].gameid or goldRoomInfo.roomInfo.GameID, self._roomFlag) then
                else
                    XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, reqJoin:getMessage())
                end
            elseif string.match(reqJoin:getMessage(), "金币过高") or string.match(reqJoin:getMessage(), "金币高于") or string.match(reqJoin:getMessage(), "金币过多") then
                if not self:canChangeRoomLevel() then
                    XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, XH.ConstString.getStr("ERROR_TOO_MUCH_SR"))
                end
                -- 进房礼包逻辑
            else
                XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, reqJoin:getMessage())
            end
        end
        --[[if data.errorcode == XH.RoomProtocol.ERRORCODE.ERROR_INAPPID then
            XH.TipTool.showTip(
                {
                    type = XH.TIP_LAYER_TYPE.OK,
                    funcOK = function()
                        XH.roomManager:onEnterGame(data, XH.ROOM_MODE.MOBILE, true, true)
                    end
                },
                "已在游戏房间中,点击确认返场"
            )
        elseif data.errorcode == XH.RoomProtocol.ERRORCODE.ERROR_IN_MATCH_LIST then
            XH.TipTool.showTip(
                {
                    type = XH.TIP_LAYER_TYPE.OK,
                    funcOK = function()
                        XH.roomManager:onEnterGame(data, XH.ROOM_MODE.MOBILE, false, true)
                    end
                },
                "已在匹配队列,点击确认返场"
            )
        elseif data.errorcode == XH.RoomProtocol.ERRORCODE.ERROR_NOT_ENOUGH_SR then
            self:judgeQuickRecharge()
        elseif data.errorcode == XH.RoomProtocol.ERRORCODE.ERROR_TOO_MUCH_SR then
            -- 是否需要转场
            if not self:canChangeRoomLevel() then
                XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, XH.ConstString.getStr("ERROR_TOO_MUCH_SR"))
            end
        else
            XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, data)
        end--]]
    elseif type == XH.Req.TYPE.TIMEOUT then
        XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, "请求超时")
    end
    self:clearLastRoomInfo()
    XH.TipTool.hideLoading()
end



function GoldModule:onJoinDispatchQueueCallBack(reqJoin, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        XH.roomManager:onEnterGame(data, XH.ROOM_MODE.MOBILE)
        self:recordLastRoomInfo()
    elseif type == XH.Req.TYPE.FAIL then
        if data.errorcode == XH.DispatchProtocol.ERRORCODE.EXISTGAME then
            XH.TipTool.showTip(
                {
                    type = XH.TIP_LAYER_TYPE.OK,
                    funcOK = function()
                        -- 这里不能直接进房间了，要把房间信息拿到
                        local inData = self:getRoomDataByAppid(data.gameappid)
                        if inData then
                            inAppID = data.gameappid
                            local index = XH.goldConfigManager:getIndexByGameid(inData.belonggameid)
                            self:reqJoinGoldRoom(index,inData.roomflag,inData.srsgroupid)
                        else 
                            XH.roomManager:onEnterGame(data, XH.ROOM_MODE.MOBILE, true, true)
                        end

                    end
                },
                "已在游戏房间中,点击确认返场"
            )
        elseif data.errorcode == XH.DispatchProtocol.ERRORCODE.EXISTQUEUE then
            XH.TipTool.showTip(
                {
                    type = XH.TIP_LAYER_TYPE.OK,
                    funcOK = function()
                        XH.roomManager:onEnterGame(data, XH.ROOM_MODE.MOBILE, false, true)
                    end
                },
                "已在匹配队列,点击确认返场"
            )
        elseif data.errorcode == XH.DispatchProtocol.ERRORCODE.GOLDLOWLIMIT then
            self:judgeQuickRecharge()
        elseif data.errorcode == XH.DispatchProtocol.ERRORCODE.GOLDOVERLIMIT then
            -- 是否需要转场
            if not self:canChangeRoomLevel() then
                XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, XH.ConstString.getStr("ERROR_TOO_MUCH_SR"))
            end
        else
            XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, data)
        end
        self:stopHeartBeatWithDispatchQueue()
    elseif type == XH.Req.TYPE.TIMEOUT then
        XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, "请求超时")
        self:stopHeartBeatWithDispatchQueue()
    end
    self:clearLastRoomInfo()
    XH.TipTool.hideLoading()
end

-- 选择合适匹配的场次
function GoldModule:selectMatchRoom(level, bLow, _levelRoom)
    local resultLevel = level
    local playerSR = XH.playerData:getSR() or 0
    for _, roominfo in pairs(_levelRoom) do
        repeat
            if bLow then
                if roominfo.roomnameflag >= level then
                    break
                end
            else
                if roominfo.roomnameflag <= level then
                    break
                end
            end
            if (playerSR >= roominfo.minrich) and ((playerSR <= roominfo.maxrich) or (roominfo.maxrich == -1)) then
                resultLevel = roominfo.roomnameflag
            end
        until true
    end
    return resultLevel
end

function GoldModule:reqTaskGetAward(action)
    return false
end

-- 请求队列服务Appid列表
function GoldModule:reqDispatchAppidList(srsgroupId)
    local srsGroupID = srsgroupId or XH.areaData:getSrsGroupID()
    local reqAppID = XH.DispatchProtocol.ReqDispatchAppIDList:new()
    reqAppID.askid = os.time()
    self._dispatchGroupID = srsGroupID
    XH.netEngine:sendProtocol(reqAppID, XH.DispatchProtocol.ReqDispatchAppIDList.processid, 0, srsGroupID)
end

function GoldModule:onRespDispatchAppIDList(event)
    local resp = XH.DispatchProtocol.RespDispatchAppIDList:new()
    resp:bistream(event.msg.buff, event.msg.len)
    self:startHeartBeatWithDispatchQueue(resp.appids)
	local goldRoomInfo = XH.goldConfigManager:getLastGoldRoomInfo()
	if not goldRoomInfo then
		return
	end
	local srsGroupID = XH.areaData:getGoldSrsGroupID()
	local gameId = goldRoomInfo.roomInfo.GameID
	local chaircnt = goldRoomInfo.roomInfo.PlayerCount
	local roomnameflag = self._roomFlag
    if goldRoomInfo.roomInfo.SpecialPlayer and goldRoomInfo.roomInfo.SpecialPlayer == 1 and goldRoomInfo.roomLevelInfos[self._roomFlag] then
        roomnameflag = -1
        chaircnt = goldRoomInfo.roomLevelInfos[self._roomFlag].chaircnt
    end
	self:startReq("ReqJoinDispatchQueue", gameId,chaircnt,srsGroupID,roomnameflag,15)
end

-- 保持与队列服务的连接
function GoldModule:startHeartBeatWithDispatchQueue(appids)
    self:stopHeartBeatWithDispatchQueue()
    self._dispatchAppIds = appids
    self:checkActWithDispatch()
    self._heartBeatQueueScheduleID =
        cc.Director:getInstance():getScheduler():scheduleScriptFunc(
        function()
            self:checkActWithDispatch()
        end,
        5,
        false
    )
end

function GoldModule:checkActWithDispatch()
    print("checkActWithDispatch...........")
    local appids = self._dispatchAppIds
    for i = 1, #appids do
        local checkAction = XH.DispatchProtocol.CheckAct:new()
        XH.netEngine:sendProtocol(checkAction, checkAction.processid, appids[i], self._dispatchGroupID or 0)
    end
end

function GoldModule:stopHeartBeatWithDispatchQueue()
    if self._heartBeatQueueScheduleID ~= nil then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._heartBeatQueueScheduleID)
        self._heartBeatQueueScheduleID = nil
    end
end

function GoldModule:judgeQuickRecharge()
    local goldRoomInfo = XH.goldConfigManager:getGoldRoomInfoByIndex(self._confID)
    if goldRoomInfo and goldRoomInfo.roomInfo.GameID and XH.lobby:getModule("YGiftBankruptcy"):isShowGiftEnter(goldRoomInfo.roomInfo.GameID, self._roomFlag) then
        return
    end
    local isSupport = XH.areaData:isSupportGoldQuickRecharge() or false
    if (self._roomFlag == 2 or self._roomFlag == 3) and isSupport then
        XH.viewManager:openView("GoldRechargeView", nil, { levelCharge = self._roomFlag })
    elseif self._roomFlag == 1 or self._roomFlag == 4 then
        if not XH.playerData:getIsRealNameTourist() then--游客登陆
            XH.viewManager:openView("GoldBaseLiveView")
        else
            XH.TipTool.showTip(
            {
                type = XH.TIP_LAYER_TYPE.OK,
                funcOK = function()
                
                end
            } ,
            "您的金币不足，请进行充值"
            )
        end
    else
        XH.TipTool.showTip(
        {
            type = XH.TIP_LAYER_TYPE.OK,
            funcOK = function()
                local ShopConfig = require("lobby.Modules.Shop.Config")
                XH.viewManager:openView("ShopView", nil, { productType = ShopConfig.SpecialTag.GOLD })
            end
        } ,
        "您的金币不足，请进行充值"
        )
    end
end

function GoldModule:closeNewUserGameGuideView()
    self:dispatchEvent({name = GoldModule.EVENT_CLOSE_NEW_USER_GUIDE_VIEW})
end

function GoldModule:getRoomDataByAppid(appid)
    local levelCfg = XH.lobby:getModule("Configuration"):getConfigJsonData(ConfigurationDefine.Config.GOLD, "Level")
    if levelCfg and levelCfg.LevelInfo then 
        local levelInfoArray = levelCfg.LevelInfo
        for index , info in ipairs(levelInfoArray) do
            if info.room then 
                for _,room in ipairs(info.room) do 
                    if appid == room.appid then
                        local data = {}
                        data.gameid = info.gameid
                        data.roomid = room.roomid
                        data.roommode = info.roommode
                        data.gameappid = room.appid
                        data.inappid = room.appid
                        data.chaircnt = info.chaircnt
                        data.srsgroupid = XH.areaData:getGoldSrsGroupID()
                        data.nMatchAppid = room.appid
                        if levelCfg.Game and info.belonggameid and levelCfg.Game[tostring(info.belonggameid)] and levelCfg.Game[tostring(info.belonggameid)].icon_params then 
                            data.belonggameid = info.belonggameid
                            local params = levelCfg.Game[tostring(info.belonggameid)].icon_params
                            if params.roomFlag then 
                                data.roomflag = params.roomFlag[index] 
                            end
                            if params.SrsGroupId then 
                                data.srsgroupid = params.SrsGroupId[index] 
                            end
                        end
                        return data
                    end
                end
            end
            
        end
    end
    return nil
end

-- 获取额外拓展参数
function GoldModule:getExtDataInfo(roomID, appID)
    if self._confID then
        local data = self:getExtDataInfoByConfID(self._confID, roomID, appID)
        if data then
            return data
        end
    end
    local confID = XH.userDefault:getValue(XH.userDefault.KEY_ID.LAST_JOIN_GOLD_INDEX, -1)
    if confID ~= -1 then
        local data = self:getExtDataInfoByConfID(confID, roomID, appID)
        if data then
            return data
        end
    end
    return nil
end

function GoldModule:getExtDataInfoByConfID(confID, roomID, appID)
    local goldRoomInfo = XH.goldConfigManager:getGoldRoomInfoByIndex(confID)
    if goldRoomInfo then
        local _levelRoom = goldRoomInfo.roomLevelInfos
        for _, roominfo in pairs(_levelRoom) do
            if roominfo.room and #roominfo.room > 0 then
                for j = 1, #roominfo.room do
                    if roominfo.room[j].roomid == roomID and roominfo.room[j].appid == appID then
                        local info = {
                            minscore = roominfo.minrich,
                            maxscore = roominfo.maxrich,
                        }
                        return json.encode(info)
                    end
                end
            end
        end
    end
    return nil
end

return GoldModule
  V_  