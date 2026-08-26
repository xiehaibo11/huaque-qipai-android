local TeaHouseManager = class("TeaHouseManager")
local PromoteDefine = require("lobby.Modules.Promote.Define")

TeaHouseManager.EVENT_TEA_HOUSE_UPDATE_START = "EVENT_TEA_HOUSE_UPDATE_START"
TeaHouseManager.EVENT_TEA_HOUSE_UPDATE_CHANGE = "EVENT_TEA_HOUSE_UPDATE_CHANGE"

function TeaHouseManager:ctor()
    cc(self):addComponent("components.behavior.EventProtocol"):exportMethods()

    self._srsGroupID = nil
    self._heartBeatScheduleID = nil
end

function TeaHouseManager:removeAllEventListeners()
    self:stopMatchLinkServer()
end

function TeaHouseManager:startMatchLinkServer()
    self:stopMatchLinkServer()
    self._srsGroupID = XH.areaData:getSrsGroupID()
    if not self._srsGroupID then
        return
    end
    XH.netEngine:addProtocolScriptFuncByObj(self, self.onReciveMatchLinkMsg, XH.MatchLinkProtocol.processid, self._srsGroupID)
    XH.netEngine:addProtocolScriptFuncByObj(self, self.onReciveAutoCardMsg, XH.TeaHouseProtocol.processid, self._srsGroupID)
    self:reqJoin()
    self._heartBeatScheduleID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        self:reqJoin()
    end, 30, false)
end

function TeaHouseManager:stopMatchLinkServer()
    if self._srsGroupID then
        XH.netEngine:removeProtocolScriptFuncByObj(self, self.onReciveMatchLinkMsg, self._srsGroupID)
        XH.netEngine:removeProtocolScriptFuncByObj(self, self.onReciveAutoCardMsg, self._srsGroupID)
        self._srsGroupID = nil
    end
    if self._heartBeatScheduleID ~= nil then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._heartBeatScheduleID)
        self._heartBeatScheduleID = nil
    end
end

function TeaHouseManager:onReciveMatchLinkMsg(XYID, buff, len)
    if XYID == XH.MatchLinkProtocol.RespInvite.XY_ID then
        self:onRespInvitePromote(XYID, buff, len)
        local position = XH.playerData:getPlayerPosition()
        if position.gameAppID and position.gameAppID ~= 0 and XH.gameManager and not XH.lobby:getModule("Im"):getData():getIsSeer() then
            return
        end
        self:onRespInvite(XYID, buff, len)
    end
end

function TeaHouseManager:onRespInvitePromote(XYID, buff, len)
    local respInvite = XH.MatchLinkProtocol.RespInvite:new()
    respInvite:bistream(buff, len)
    if respInvite.ucType == XH.MatchLinkProtocol.RespInvite.TYPE.TYPE_PROMOTE then
        local info = json.decode(respInvite.szMsg)
        if info.type == PromoteDefine.TIP_TYPE.PLAYER_NOTICE then
            -- 玩家通知领队/副领队创建玩法
            if self._lastPromoteTime ~= nil then
                if os.time() - self._lastPromoteTime < 20 then
                    return
                end
            end
            if XH.SceneManager:getInstance():getTopSceneTag() ~= XH.SCENE_TAG.GAME then
                self._lastPromoteTime = os.time()
                local str = "亲友圈（" .. info.teaName .. "）玩家请求添加红中癞子牌桌，是否稍后前往添加"
                XH.viewManager:openView("PromoteTipView", nil, PromoteDefine.TIP_TYPE.OWNER_CREATE, info.teaid, str)
            end
        elseif info.type == PromoteDefine.TIP_TYPE.OWNER_INVITE then
            -- 收到领队副领队邀请，已经有游戏房间不处理
            local position = XH.playerData:getPlayerPosition()
            if position.gameAppID and position.gameAppID ~= 0 then
                return
            end
            XH.viewManager:openView("PromoteUserInviteView", nil, info)
        end
    end
end

function TeaHouseManager:onRespInvite(XYID, buff, len)
    local respInvite = XH.MatchLinkProtocol.RespInvite:new()
    respInvite:bistream(buff, len)
    if respInvite.ucType == XH.MatchLinkProtocol.RespInvite.TYPE.TYPE_SEND then
        local reciveInfo = json.decode(respInvite.szMsg)
        local msg = "玩家"
        if reciveInfo.sFromNickName then
            msg = msg .. XH.StringTool.cutStringByLength(reciveInfo.sFromNickName) .. " 邀请您进入：\n"
        end
        if reciveInfo.boxRoomID then
            msg = msg .. "【" .. reciveInfo.boxRoomID .. "】"
            if reciveInfo.boxRoomName then
                msg = msg .. reciveInfo.boxRoomName
            end
            if reciveInfo.boxRoomRule then
                msg = msg .. "," .. reciveInfo.boxRoomRule
            end
        else
            if reciveInfo.sTeaHouseName then
                msg = msg .. "【" .. reciveInfo.sTeaHouseName .. "】\n" .. "等你来！"
            end
        end
        local tableInfo = {}
        tableInfo.game_roomid = reciveInfo.boxRoomID
        tableInfo.gameid = reciveInfo.gameID
        tableInfo.game_appid = reciveInfo.gameAppID
        local agreeFunc = function()
            -- 玩家有位置，并且没在游戏中
            local position = XH.playerData:getPlayerPosition()
            if position.gameAppID and position.gameAppID ~= 0 then
                if XH.gameManager and XH.lobby:getModule("Im"):getData():getIsSeer() then
                    XH.gameManager:leaveGame()
                else
                    local param = {}
                    param.appID = position.gameAppID
                    param.gameID = position.gameID
                    param.roomMode = position.roomMode
                    param.roomID = position.roomID
                    param.srsGroupID = position.srsGroupID
                    local gameStartData = require("app.Data.GameStartData").new(param)
                    XH.roomManager:leaveRoomFunc(gameStartData)
                end
            elseif XH.gameManager and XH.lobby:getModule("Im"):getData():getIsSeer() then
                XH.gameManager:leaveGame()
            end
            local teaHousePwd = reciveInfo.sTeaHouseID
            local tableID = nil
            if reciveInfo.boxRoomID then
                tableID = tonumber(string.match(tostring(reciveInfo.boxRoomID), "%d+"))
            end
            -- 延迟一会儿，清除玩家状态才能加入桌子
            XH.SysTool.performDelayOnce(function()
                self:reJoinTeaHouse(teaHousePwd, tableID, tableInfo)
                self:respInvite(respInvite.nToNumid, respInvite.nFromNumid, "", XH.MatchLinkProtocol.RespInvite.TYPE.TYPE_AGREE)
            end, 0.5)

            -- 抛送确定按钮数据
            local data = {
                block_item_id = "确定",
                block_label = "亲友圈-成员.最近对局"
            }
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyxt24052711, data)
        end
        local refuseFunc = function()
            local nickname = XH.StringTool.cutStringByLength(XH.playerData:getNickName())
            local refuseMsg = "玩家" .. nickname .. "现在不方便\n拒绝了您的邀请"
            self:respInvite(respInvite.nToNumid, respInvite.nFromNumid, refuseMsg, XH.MatchLinkProtocol.RespInvite.TYPE.TYPE_REFUSE)

            -- 抛送取消按钮数据
            local data = {
                block_item_id = "取消",
                block_label = "亲友圈-成员.最近对局"
            }
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyxt24052711, data)
        end
        XH.TipTool.showTip({
			type = XH.TIP_LAYER_TYPE.OK_CANCEL,
            funcOK = agreeFunc,
            funcCancel = refuseFunc
		}, msg)

        -- 抛送push邀请曝光数据
        local data = {
            block_label = "亲友圈-成员.最近对局"
        }
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyxt24052710, data)

    elseif respInvite.ucType == XH.MatchLinkProtocol.RespInvite.TYPE.TYPE_REFUSE then
        XH.TipTool.showTip({
			type = XH.TIP_LAYER_TYPE.OK,
		}, respInvite.szMsg)
    end
end

function TeaHouseManager:reset()
    self:startMatchLinkServer()

    if TeaHouse and TeaHouse.destroy then
        TeaHouse.destroy()
    end

    if TeaHouse and TeaHouse.destroy and TeaHouse.init then
        TeaHouse.destroy()
        TeaHouse.init()
    end
end

function TeaHouseManager:reqJoin()
    if not XH.areaData:getAreaID() then
        return
    end
    local srsGroupID = XH.areaData:getSrsGroupID()
    local reqJoin = XH.MatchLinkProtocol.ReqJoin:new()
    reqJoin.areaid = XH.playerData:getBrandID()
    reqJoin.numid = XH.playerData:getNumberID()
    XH.netEngine:sendProtocol(reqJoin, XH.MatchLinkProtocol.ReqInvite.processid, XH.areaData:getMatchLinkAppID() or 0, srsGroupID)
end

function TeaHouseManager:reqInvite(fromNumID, toNumID, msg)
    if not XH.areaData:getAreaID()  then
        return
    end
    local srsGroupID = XH.areaData:getSrsGroupID()
    local reqInvite = XH.MatchLinkProtocol.ReqInvite:new()
    reqInvite.nAskid = 0
    reqInvite.nFromAreaid = XH.areaData:getAreaID()
    reqInvite.nFromNumid = fromNumID
    reqInvite.nToAreaid = XH.areaData:getAreaID()
    reqInvite.nToNumid = toNumID
    reqInvite.ucType = XH.MatchLinkProtocol.ReqInvite.TYPE.TYPE_SEND
    reqInvite.szMsg = msg
    XH.netEngine:sendProtocol(reqInvite, XH.MatchLinkProtocol.ReqInvite.processid, XH.areaData:getMatchLinkAppID() or 0, srsGroupID)
    TeaHouse.TipTool.showToast("邀请已发送")
end

function TeaHouseManager:respInvite(fromNumID, toNumID, msg, type)
    if not XH.areaData:getAreaID() == nil then
        return
    end
    local srsGroupID = XH.areaData:getSrsGroupID()
    local reqInvite = XH.MatchLinkProtocol.ReqInvite:new()
    reqInvite.nAskid = 0
    reqInvite.nFromAreaid = XH.areaData:getAreaID()
    reqInvite.nFromNumid = fromNumID
    reqInvite.nToAreaid = XH.areaData:getAreaID()
    reqInvite.nToNumid = toNumID
    reqInvite.ucType = type
    reqInvite.szMsg = msg
    XH.netEngine:sendProtocol(reqInvite, XH.MatchLinkProtocol.ReqInvite.processid, XH.areaData:getMatchLinkAppID() or 0, srsGroupID)
end

function TeaHouseManager:reqInviteAll(fromNumID, toNumIDs, msg)
    local srsGroupID = XH.areaData:getSrsGroupID()
    local reqInvite = XH.MatchLinkProtocol.ReqInviteMore:new()
    reqInvite.nAskid = 0
    reqInvite.nFromAreaid = XH.areaData:getAreaID()
    reqInvite.nFromNumid = fromNumID
    reqInvite.nCount = #toNumIDs
    for i = 1, #toNumIDs do
        reqInvite.nToAreaid[i] = XH.areaData:getAreaID()
        reqInvite.nToNumid[i] = toNumIDs[i]
    end
    reqInvite.ucType = XH.MatchLinkProtocol.ReqInviteMore.TYPE.TYPE_SEND
    reqInvite.szMsg = msg
    XH.netEngine:sendProtocol(reqInvite, XH.MatchLinkProtocol.ReqInviteMore.processid, XH.areaData:getMatchLinkAppID() or 0, srsGroupID)
    TeaHouse.TipTool.showToast("邀请成功")
end

function TeaHouseManager:openTeaHouseList()
    if self:isHotUpdate() then
        self:initUpdate(true)
    else
        require("teahouse.init")
        if TeaHouse and TeaHouse.init and TeaHouse.openTeaHouseList then
            XH.bugly.buglyLog("onJoinTeaHouse", "onJoinTeaHouse")
            TeaHouse.init()
            TeaHouse.openTeaHouseList()
        end
    end
end

function TeaHouseManager:joinTeaHouse(teaHousePwd, tableID, inTeaFunc, playModeId)
    if self:isHotUpdate() then
        self:initUpdate(true)
    else
        require("teahouse.init")
        if TeaHouse and TeaHouse.init and TeaHouse.joinTeaHouse then
            TeaHouse.init()
            TeaHouse.joinTeaHouse(teaHousePwd, tableID, inTeaFunc, playModeId)
        end
    end
end

function TeaHouseManager:reJoinTeaHouse(teaHousePwd, tableID, tableInfo, inTeaFunc, playModeId)
    if XH.areaData:isSupportFriendsV2() and  tableInfo then
        XH.lobby:getModule("Im"):reqWillJoinTable(tableInfo,true)
    else
        if self:isHotUpdate() then
            self:initUpdate(true)
        else
            require("teahouse.init")
            if TeaHouse and TeaHouse.init and TeaHouse.reJoinTeaHouse then
                TeaHouse.init()
                TeaHouse.reJoinTeaHouse(teaHousePwd, tableID, inTeaFunc, playModeId)
            end
        end
    end
end

function TeaHouseManager:isHotUpdate()
    local TeaHouseResChecker = require("app.hotupdate.teahouse.TeaHouseResChecker")
    return TeaHouseResChecker.isHotUpdate()
end

function TeaHouseManager:initUpdate(isInit)
    local TeaHouseResChecker = require("app.hotupdate.teahouse.TeaHouseResChecker")
    if isInit then
        TeaHouseResChecker.setDelegate(self)
        self:dispatchEvent({ name = TeaHouseManager.EVENT_TEA_HOUSE_UPDATE_START, msg = { data = { isStart = true } } })
    else
        TeaHouseResChecker.setDelegate(nil)
    end
end

function TeaHouseManager:onStartUpdate(param)
    self:dispatchEvent({ name = TeaHouseManager.EVENT_TEA_HOUSE_UPDATE_START, msg = { data = param } })
    if not param.isStart then
        self:initUpdate(false)
    end
end

function TeaHouseManager:onPercentChanged(param)
    self:dispatchEvent({ name = TeaHouseManager.EVENT_TEA_HOUSE_UPDATE_CHANGE, msg = { data = param } })
end

function TeaHouseManager:checkClipboard(msg)
    local teaHouseID = nil
    local teaHouseTable = nil
    local strTeaHouseID = string.match(msg, "亲友圈口令：%d+") or string.match(msg, "比赛场口令：%d+")
    if strTeaHouseID then
        teaHouseID = tonumber(string.match(strTeaHouseID, "%d+"))
    end
    local strTeaHouseTable = string.match(msg, "桌号：%d+")
    if strTeaHouseTable then
        teaHouseTable = tonumber(string.match(strTeaHouseTable, "%d+"))
    end
    if teaHouseID then
        XH.SysTool.copyString("")
        self:joinTeaHouse(teaHouseID, teaHouseTable)
        return true
    end
    local teaHouseShareCode = nil
    local strTeaHouseShareCode = string.match(msg, "回放码：%d+")
    if strTeaHouseShareCode then
        teaHouseShareCode = string.match(strTeaHouseShareCode, "%d+")
    end
    return teaHouseShareCode ~= nil
end

function TeaHouseManager:isInTeaHouse()
    if TeaHouse and TeaHouse.isInTeahouse then
        return TeaHouse.isInTeahouse()
    end
    return false
end

function TeaHouseManager:getTeaHousePwd()
    if TeaHouse and TeaHouse.getTeaHousePwd then
        return TeaHouse.getTeaHousePwd()
    end
    return nil
end

function TeaHouseManager:isShowTeaHouseOnlineListInGame()
    if TeaHouse and TeaHouse.isShowTeaHouseOnlineListInGame then
        return TeaHouse.isShowTeaHouseOnlineListInGame()
    end
    return false
end

function TeaHouseManager:openTeaHouseOnlineListInGame(inviteOfflineFunc)
    if TeaHouse and TeaHouse.openTeaHouseOnlineListInGame then
        TeaHouse.openTeaHouseOnlineListInGame(inviteOfflineFunc)
    end
end

function TeaHouseManager:refreshTeaHouseProp()
    if TeaHouse and TeaHouse.refreshTeaHouseProp then
        TeaHouse.refreshTeaHouseProp()
    end
end

function TeaHouseManager:shareToFriends()
    if TeaHouse and TeaHouse.shareToTeaFriends then
        TeaHouse.shareToTeaFriends()
    end
end

function TeaHouseManager:isTeaHouseListViewExist()
    if TeaHouse and TeaHouse.manager.viewManager:getView("TeaHouseListView") then
        return true
    end
    return false
end

function TeaHouseManager:getTeaHouseOnlineModule()
    local module = nil
    if TeaHouse and TeaHouse.manager then
        module = TeaHouse.manager.teaHouseOnline
    end
    return module
end

function TeaHouseManager:onReciveAutoCardMsg(XYID, buff, len)
    if XYID == XH.TeaHouseProtocol.NotifyCardCount.XY_ID then
        local msgData = XH.TeaHouseProtocol.NotifyCardCount:new()
        msgData:bistream(buff, len)
        print("*****onReciveAutoCardMsg*****")
        if TeaHouse and TeaHouse.manager.viewManager:getView("TeaHouseSetupView") then
            return
        end
        dump(msgData)
        XH.SysTool.performDelayOnce(function()
            local topSceneTag = XH.SceneManager:getInstance():getTopSceneTag()
            if topSceneTag == XH.SCENE_TAG.TEAHOUSE or topSceneTag == XH.SCENE_TAG.GAME then
                if topSceneTag == XH.SCENE_TAG.GAME then
                    local popLayerManager = require("game.Common.Tip.PopLayerManager").new()
                    if popLayerManager and popLayerManager.getLayer then
                        if not popLayerManager:getLayer("BigWinLostUI") then
                            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_DATA_AUTO_CARD_NOTIFY_TYPE, msgData.cardtype)
                            return
                        end
                    end
                end
                XH.TipTool.showAutoCardTip(topSceneTag, msgData.cardtype)
                if msgData.cardtype == TeaHouse.TeaHouseProtocol.NotifyCardCount.ECARDTYPE.TEAHOUSE then
                    TeaHouse.manager.teahouseMain:dispatchEvent({ name = TeaHouse.manager.teahouseMain.EVENT_LACK_CARD_NOTIFY, show = true})
                end
            end
        end, 0.2)
    end
end

function TeaHouseManager:isTeaHouseRemodelListViewExist()
    if TeaHouse and TeaHouse.manager.viewManager:getView("TeaHouseRemodelListView") then
        return true
    end
    return false
end

function TeaHouseManager:onCreateTeaHouse(DefaultConfig)
    if TeaHouse and TeaHouse.onCreateTeaHouse then 
        local data = {}
        data.notesNum = DefaultConfig.notesNum or 123
        data.everyDayCostLimitNum = DefaultConfig.everyDayCostLimitNum
        data.newerNum = DefaultConfig.newerNum
        data.rechargeNum = DefaultConfig.rechargeNum
        data.bOthersHiden = DefaultConfig.bOthersHiden
        data.bAutoAddRoomCard = DefaultConfig.bAutoAddRoomCard
        data.nLowCardNum = DefaultConfig.nLowCardNum
        data.nTransferCardNum = DefaultConfig.nTransferCardNum
        data.nRemindLowCardNum = DefaultConfig.nRemindLowCardNum
        data.modeType = DefaultConfig.modeType
        data.payTypeTable = DefaultConfig.payTypeTable
        data.nLevel = DefaultConfig.nLevel or 0
        TeaHouse.onCreateTeaHouse(data)
    end
end

function TeaHouseManager:getPlayModeInfos()
    if TeaHouse and TeaHouse.manager and TeaHouse.manager.teahouseData then
        return TeaHouse.manager.teahouseData:getPlayModeInfos()
    end
    return
end

function TeaHouseManager:getTeaHouseData()
    if TeaHouse and TeaHouse.manager and TeaHouse.manager.teahouseData then
        return TeaHouse.manager.teahouseData
    end
    return
end

function TeaHouseManager:getTeaHouseManager()
    if TeaHouse and TeaHouse.manager and TeaHouse.manager.teahouseData then
        return TeaHouse.manager
    end
    return
end

function TeaHouseManager:autoCreateNewPlayTypeMode(nAutoFlag, nGetAward)
    if TeaHouse and TeaHouse.manager and TeaHouse.manager.teaHouseCreate then
        return TeaHouse.manager.teaHouseCreate:autoCreateNewPlayTypeMode(nAutoFlag, nGetAward)
    end
    return
end

function TeaHouseManager:getConfigManager()
    if TeaHouse and TeaHouse.manager and TeaHouse.manager.configManager then
        return TeaHouse.manager.configManager
    end
    return
end

function TeaHouseManager:getViewManager()
    if TeaHouse and TeaHouse.manager and TeaHouse.manager.viewManager then
        return TeaHouse.manager.viewManager
    end
    return
end

function TeaHouseManager:deletePlayModeEx(playModeId)
    if not playModeId then
        return
    end
    if TeaHouse and TeaHouse.manager and TeaHouse.manager.teahouseSetup then
        return TeaHouse.manager.teahouseSetup:deletePlayModeEx(playModeId)
    end
    return
end

function TeaHouseManager:checkAutoCreateNewPlayTypeModeCondition()
    if TeaHouse and TeaHouse.manager and TeaHouse.manager.teaHouseCreate then
        return TeaHouse.manager.teaHouseCreate:checkAutoCreateNewPlayTypeModeCondition()
    end
    return
end

function TeaHouseManager:reqPromoteInviteAll(fromNumID, toNumIDs, msg)
    local srsGroupID = XH.areaData:getSrsGroupID()
    local reqInvite = XH.MatchLinkProtocol.ReqInviteMore:new()
    reqInvite.nAskid = 0
    reqInvite.nFromAreaid = XH.areaData:getAreaID()
    reqInvite.nFromNumid = fromNumID
    reqInvite.nCount = #toNumIDs
    for i = 1, #toNumIDs do
        reqInvite.nToAreaid[i] = XH.areaData:getAreaID()
        reqInvite.nToNumid[i] = toNumIDs[i]
    end
    reqInvite.ucType = XH.MatchLinkProtocol.ReqInviteMore.TYPE.TYPE_PROMOTE
    reqInvite.szMsg = msg
    XH.netEngine:sendProtocol(reqInvite, XH.MatchLinkProtocol.ReqInviteMore.processid, XH.areaData:getMatchLinkAppID() or 0, srsGroupID)
end

return TeaHouseManager Q  