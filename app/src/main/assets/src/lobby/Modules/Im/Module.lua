local IMModule = class("IMModule", XH.ModuleBase)
local IMProtocol = require("app.Protocols.IMProtocol")
local IMData = require("lobby.Modules.Im.IMData")

local KEEP_ALIVE_TIME = 100 --心跳发送间隔时间
local MAX_NAME_LEN = 6      --最长昵称显示长度

IMModule.InviteColdTime = 30 --邀请冷却时间
IMModule.SendReqColdTime = 60 --发送数据请求冷却时间减轻服务压力

IMModule.MESSAGE_CONTENT = {
    [1] = "邀请您进入房间",
    [2] = "请求加入房间",
    [3] = "对您发起了预约",
}

IMModule.EVENT_HIDE_LISTLAYER = "EVENT_HIDE_LISTLAYER"
IMModule.EVENT_HIDE_MESSAGELAYER = "EVENT_HIDE_MESSAGELAYER"
IMModule.EVENT_NOTIFY_MESSAGE = "EVENT_NOTIFY_MESSAGE"
IMModule.EVENT_HIDE_NOTIFY_INVITE = "EVENT_HIDE_NOTIFY_INVITE"
IMModule.EVENT_HIDE_LIST_ENTRANCE = "EVENT_HIDE_LIST_ENTRANCE"
IMModule.EVENT_SHOW_LIST_ENTRANCE = "EVENT_SHOW_LIST_ENTRANCE"
IMModule.EVENT_HIDE_MSG_ENTRANCE = "EVENT_HIDE_MSG_ENTRANCE"
IMModule.EVENT_SHOW_MSG_ENTRANCE = "EVENT_SHOW_MSG_ENTRANCE"
IMModule.EVENT_NOTIFY_INVITE_ALL_SUCCESS = "EVENT_NOTIFY_INVITE_ALL_SUCCESS"
IMModule.EVENT_SHOW_MSG_LIST = "EVENT_SHOW_MSG_LIST"
IMModule.EVENT_HISTORY_FRIENDS = "EVENT_HISTORY_FRIENDS"
IMModule.EVENT_SEARCHING_VIEW_CLOSE = "EVENT_SEARCHING_VIEW_CLOSE"
IMModule.EVENT_SHOW_JOIN_NOW = "EVENT_SHOW_JOIN_NOW"
IMModule.EVENT_UPDATE_INVITE_INFO = "EVENT_UPDATE_INVITE_INFO"
IMModule.EVENT_SHOW_LISTLAYER = "EVENT_SHOW_LISTLAYER"
IMModule.EVENT_UPDATE_ADD_FRIEND_STATE = "EVENT_UPDATE_ADD_FRIEND_STATE"
IMModule.EVENT_APPLYLIST_CHANGE = "EVENT_APPLYLIST_CHANGE"
IMModule.EVENT_TEA_HOUSE_LIST_CHANGED = "EVENT_TEA_HOUSE_LIST_CHANGED"
IMModule.EVENT_MEMBERLIST_KICK = "EVENT_MEMBERLIST_KICK"
IMModule.EVENT_COST_UPDATE = "EVENT_COST_UPDATE"
IMModule.EVENT_PLAYERCNT_UPDATE = "EVENT_PLAYERCNT_UPDATE"
IMModule.EVENT_UPDATE_TEA_RECORD = "EVENT_UPDATE_TEA_RECORD"
IMModule.EVENT_SCORE_RANK_UPDATE = "EVENT_SCORE_RANK_UPDATE"
IMModule.EVENT_BIG_WINNER_RANK_UPDATE = "EVENT_BIG_WINNER_RANK_UPDATE"
IMModule.EVENT_COST_RANK_UPDATE = "EVENT_COST_RANK_UPDATE"
IMModule.EVENT_RANK_CUR_DATE_UPDATE = "EVENT_RANK_CUR_DATE_UPDATE"
IMModule.EVENT_GORUP_MEMBER_LIST_UPDATE = "EVENT_GORUP_MEMBER_LIST_UPDATE"
IMModule.EVENT_LATELY_LIST_CHANGED = "EVENT_LATELY_LIST_CHANGED"

local KW_LIMIT_DISTANCE = 200

function IMModule:ctor()
    IMModule.super.ctor(self)
    self:initData()
    self:reqScheduleAppID()
end

function IMModule:initData()
    if not self._imData then
        self._imData = IMData.new()
    end
    self._curFriendListPage = 0
    self._totalFriendListPage = 0
    self._curTableListPage = 0
    self._totalTableListPage = 0
    self._inviteCurPage = 0
    self._inviteTotalPage = 0
    self._reserveInfo = nil
    self._kickUserId = 0
    self._applyList = {}
    self._tmpInviteTable = {}

    self._bAccept = false
    self._reqAppidList = 0
end

function IMModule:getIMData()
    return self._imData
end

function IMModule:getProxyEvents()
    return {
        { module = XH.netEngine, eventName = IMProtocol.NotifyServerState.event_key, callBack = "onNotifyServerState" },
        { module = XH.netEngine, eventName = IMProtocol.NotifyPlayerInfo.event_key, callBack = "onNotifyPlayerInfo" },
        { module = XH.netEngine, eventName = IMProtocol.NotifyInviteInfo.event_key, callBack = "onNotifyInviteInfo" },
        { module = XH.netEngine, eventName = IMProtocol.NotifyReplyInviteGame.event_key, callBack = "onNotifyReplyInvite" },
        { module = XH.netEngine, eventName = IMProtocol.NotifyWillJoinTable.event_key, callBack = "onNotifyWillJoinTable" },
        { module = XH.netEngine, eventName = IMProtocol.NotifyReserveGame.event_key, callBack = "onNotifyReserveGame" },
        { module = XH.netEngine, eventName = IMProtocol.RespHistoryFriends.event_key, callBack = "onHistoryFriendsInfo" },
        { module = XH.netEngine, eventName = IMProtocol.NotifyFriendListChange.event_key, callBack = "onNotifyFriendListChange" },
        { module = XH.netEngine, eventName =  XH.TeaHouseProtocol.UserInfo.event_key, callBack = "onReqGroupMemberList" },
        { module = XH.sdkManager, eventKeyName = "EVENT_AMAP_CALLBACK", callBack = "onGetLocationCallBack" },
    }
end

function IMModule:reqScheduleAppID()
    if self._appIDScheduleID and self._appIDScheduleID ~= 0 then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._appIDScheduleID)  
        self._appIDScheduleID = 0
    end
    self._appIDScheduleID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        if not self then
            return
        end
        if self._reqAppidList > 0 then
            self._reqAppidList = 0
            self:reqAppID()
        end
    end, KEEP_ALIVE_TIME, false)
end

function IMModule:onNotifyServerState(event)
    local resp =  IMProtocol.NotifyServerState:new()
    resp:bistream(event.msg.buff, event.msg.len)
    self._reqAppidList = self._reqAppidList + 1
end

function IMModule:onNotifyPlayerInfo(event)
    if self:judgeInTeaHouse() and not self._imData:getIsSeer() then
        return
    end

    local resp =  IMProtocol.NotifyPlayerInfo:new()
    resp:bistream(event.msg.buff, event.msg.len)

    if self._imData:getLable() <= self._imData.KW_LIST_TYPE.PLAYERLIST then
        self._imData:updatePlayerInfo(resp.friendInfo)
    end
end

function IMModule:onNotifyInviteInfo(event)
    -- if self:judgeInTeaHouse() then
    --     return
    -- end

    local resp =  IMProtocol.NotifyInviteInfo:new()
    resp:bistream(event.msg.buff, event.msg.len)

    self._imData:addToInviteList(self:changeDataToMessage(resp.inviteId, resp.inviteAreaid, resp.invitePlayer, resp.beInviteAreaid,
    resp.beInvitePlayer, resp.askTime, resp.askName, resp.invitedd_nickname, XH.IMProtocol.MESSAGE_TYPE.INVITE), true)

    if XH.lobby:getModule("Mark"):isShowMarkGame(resp.gameid) then
        local markText = XH.lobby:getModule("Mark"):getMarkTextByGameID(resp.gameid)
        resp.gameName = markText.gameName
    end
    -- 金币场不显示邀请信息
    if XH.gameManager and not XH.gameManager:isBoxRoom() then
        return
    end
    if XH.gameManager and XH.gameManager:isPlayBack() then
        return
    end
    if  XH.viewManager:isViewExist("ImInviteView") then
        self:dispatchEvent({name = self.EVENT_UPDATE_INVITE_INFO, info = resp })
    else
        XH.viewManager:openView("ImInviteView", nil, {info = resp})
    end
end

function IMModule:onNotifyReplyInvite(event)
    -- if self:judgeInTeaHouse() then
    --     return
    -- end

    local resp =  IMProtocol.NotifyReplyInviteGame:new()
    resp:bistream(event.msg.buff, event.msg.len)

    if resp.ec == XH.IMProtocol.ERRORCODE.EC_WAITINT_PERIOD_OF_TIME then
        XH.TipTool.showToast(string.format(XH.IMProtocol.ERRORSTR[resp.ec], XH.StringTool.cutStringByLength(clone(resp.reply_nickname), MAX_NAME_LEN)))
    end
end

function IMModule:onNotifyWillJoinTable(event)
    -- if self:judgeInTeaHouse() then
    --     return
    -- end
    
    local resp =  IMProtocol.NotifyWillJoinTable:new()
    resp:bistream(event.msg.buff, event.msg.len)

    self._imData:addToInviteList(self:changeDataToMessage(0, resp.follow_areaid, resp.follow_numid, 0,
    0, resp.askTime, resp.askName, "", XH.IMProtocol.MESSAGE_TYPE.FOLLOW))

    self:dispatchEvent({name = self.EVENT_NOTIFY_MESSAGE, msg = {data = resp, type = IMProtocol.MESSAGE_TYPE.FOLLOW} })
end

function IMModule:onNotifyReserveGame(event)
    -- if self:judgeInTeaHouse() then
    --     return
    -- end

    local resp =  IMProtocol.NotifyReserveGame:new()
    resp:bistream(event.msg.buff, event.msg.len)

    self._imData:addToInviteList(self:changeDataToMessage(0, resp.areaid, resp.numid, 0,
    0, resp.askTime, resp.askName, "", XH.IMProtocol.MESSAGE_TYPE.ORDER))

--    self:dispatchEvent({name = self.EVENT_NOTIFY_MESSAGE, msg = {data = resp, type = IMProtocol.MESSAGE_TYPE.ORDER} })
    self._reserveInfo = resp
end

function IMModule:onHistoryFriendsInfo(event)
    if self:judgeInTeaHouse() and not self._imData:getIsSeer() then
        return
    end

    local resp =  IMProtocol.RespHistoryFriends:new()
    resp:bistream(event.msg.buff, event.msg.len)

    cc.UserDefault:getInstance():setBoolForKey("IMListLayer_SearchFriend_" .. XH.playerData:getNumberID(), true)

    if resp.ec ~= XH.IMProtocol.ERRORCODE.SUCCESS then
        self:dispatchEvent({ name = self.EVENT_SEARCHING_VIEW_CLOSE })
        XH.TipTool.showToast(XH.IMProtocol.ERRORSTR[resp.ec] or ("查找历史牌友信息失败" .. resp.ec))
    else
        self._imData:addHistoryFriendsInfo(resp.oldfriends)
        if resp.total == 0 then
            self:dispatchEvent({ name = self.EVENT_HISTORY_FRIENDS, bShowDetail = false })
        elseif resp.begin + resp.count == resp.total then
            self:dispatchEvent({ name = self.EVENT_HISTORY_FRIENDS, bShowDetail = true })
        end
    end
end

function IMModule:reqAppID()
    if self._keepAliveScheduleID and self._keepAliveScheduleID ~= 0 then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._keepAliveScheduleID)  
        self._keepAliveScheduleID = 0
    end
    self._connectCnt = 0
    local ReqAppidList = require("lobby.Req.Im.ReqAppidList")
    local reqAppidList = ReqAppidList:new()
    reqAppidList:addReqCallBack(self, self.onReqIMAppidCallBack)
    reqAppidList:start(XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqIMAppidCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        if data.appid then
            self._imData:setSvrAppidList(data.appid)
        end
        self:reqPlayerConnect()
    end
end

function IMModule:reqPlayerConnect()
    local appid = self._imData:getSvrAppidList()
    if appid and next(appid) then
        local ReqPlayerConnect = require("lobby.Req.Im.ReqPlayerConnect")
        for i = 1, #appid do
            local reqPlayerConnect = ReqPlayerConnect:new()
            reqPlayerConnect:addReqCallBack(self, self.onReqIMPlayerConnectCallBack)
            reqPlayerConnect:start(appid[i], XH.KW_CONFIG_LOGIC_TIME_OUT)
        end
    end
end

function IMModule:onReqIMPlayerConnectCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        self._imData:setProposalAppid(data.im_appid)
        self._connectCnt = self._connectCnt + 1
        if self._connectCnt == #(self._imData:getSvrAppidList()) then
            self:reqJoinIM()
            self:reqKeepAlive()
        end
    end
end

function IMModule:reqKeepAlive()
    self._keepAliveScheduleID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        local appid = self._imData:getSvrAppidList()
        if appid and type(appid) == "table" and next(appid) then
            local ReqKeepAlive = require("lobby.Req.Im.ReqKeepAlive")
            for i = 1, #appid do
                local reqKeepAlive = ReqKeepAlive:new()
                reqKeepAlive:addReqCallBack(self, self.onReqIMKeepAliveCallBack)
                reqKeepAlive:start(appid[i], XH.KW_CONFIG_LOGIC_TIME_OUT)
            end
        end
    end, KEEP_ALIVE_TIME, false)
end

function IMModule:onReqIMKeepAliveCallBack(cell, ctype, data)
end

function IMModule:reqBroadState(bOpen)
    local ReqBroadcastState = require("lobby.Req.Im.ReqBroadcastState")
    local reqBroadcastState = ReqBroadcastState:new()
    reqBroadcastState:addReqCallBack(self, function(node, cell, ctype, data)
    end)
    reqBroadcastState:start(self._imData:getProposalAppid(), bOpen, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:reqJoinIM()
    local ReqJoinIM = require("lobby.Req.Im.ReqJoinIM")
    local reqJoinIM = ReqJoinIM:new()
    reqJoinIM:addReqCallBack(self, self.onReqJoinIMCallBack)
    reqJoinIM:start(self._imData:getProposalAppid(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqJoinIMCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        if data.im_appid then
            self._imData:setProposalAppid(data.im_appid)
            self._imData:setThesHold(data.threshold)
            --请求打开列表可接收推送
            self:reqBroadState(true)
        end
    end
end

function IMModule:reqFriendListInfo()
    if self._curFriendListPage ~= 0 and self._curFriendListPage > self._totalFriendListPage - 1 then
        return
    end
    local ReqFriendList = require("lobby.Req.Im.ReqFriendList")
    local reqFriendList = ReqFriendList:new()
    reqFriendList:addReqCallBack(self, self.onReqFriendListCallBack)
    reqFriendList:start(self._imData:getProposalAppid(), self._curFriendListPage, XH.KW_CONFIG_LOGIC_TIME_OUT, self._curFriendListPage)
end

function IMModule:reqFriendListInfoEx(reqPage, askid)
    local ReqFriendList = require("lobby.Req.Im.ReqFriendList")
    local reqFriendList = ReqFriendList:new()
    reqFriendList:addReqCallBack(self, function(node, cell, ctype, data)
        if ctype == XH.Req.TYPE.SUCCESS then
            self._imData:addToFriendList(data.friendInfo)
        end
    end)
    reqFriendList:start(self._imData:getProposalAppid(), reqPage, XH.KW_CONFIG_LOGIC_TIME_OUT, askid)
end

function IMModule:reqFriendListInfoForGroup(reqPage, askid)
    local ReqFriendList = require("lobby.Req.Im.ReqFriendList")
    local reqFriendList = ReqFriendList:new()
    reqFriendList:addReqCallBack(self, function(node, cell, ctype, data)
        if ctype == XH.Req.TYPE.SUCCESS then
            self._imData:addToFriendListForGroup(data.friendInfo)
        end
    end)
    reqFriendList:start(self._imData:getProposalAppid(), reqPage, XH.KW_CONFIG_LOGIC_TIME_OUT, askid)
end

function IMModule:onReqFriendListCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        if data.cur_package == 0 then
            self._totalFriendListPage = data.total_package
            self._imData:clearFriendList()
        end
        if self._curFriendListPage <= data.total_package - 1 then
            self._curFriendListPage = data.cur_package + 1
        end
        self._imData:addToFriendList(data.friendInfo)
    end
end

function IMModule:reqInviteGame(playerInfo, roomid)
    local inviteAreaId = XH.areaData:getAreaID()
    local invitePlayer = XH.playerData:getNumberID()
    local beInvitedAreaId = playerInfo.areaid
    local beInvitedPlayer = playerInfo.numid
    local ReqInviteGame = require("lobby.Req.Im.ReqInviteGame")
    local reqInviteGame = ReqInviteGame:new()
    reqInviteGame:addReqCallBack(self, self.onReqInviteGameCallBack)
    reqInviteGame:start(self._imData:getProposalAppid(), inviteAreaId, invitePlayer, beInvitedAreaId, beInvitedPlayer, roomid, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqInviteGameCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        XH.TipTool.showToast("邀请已发送")
    elseif ctype == XH.Req.TYPE.FAIL then
        XH.TipTool.showToast(data.msg)
    end
end

function IMModule:reqMessageList(bShowList, bClearCache, bFirstReq)
    if bClearCache then
        self._inviteCurPage = 0
        self._inviteTotalPage = 0
        self._imData:clearInviteList()
    end
    local ReqAllMessageList = require("lobby.Req.Im.ReqAllMessageList")
    local reqAllMessageList = ReqAllMessageList:new()
    if bShowList then
        reqAllMessageList:addReqCallBack(self, function (node, cell, ctype, data)
            if ctype == XH.Req.TYPE.SUCCESS then
                self:dispatchEvent({name = self.EVENT_SHOW_MSG_LIST, msg = data.messageInfo })
                if next(data.messageInfo) ~= nil then
                    self._imData:addToInviteList(data.messageInfo)
                end
            end
        end)
    elseif bFirstReq then
        reqAllMessageList:addReqCallBack(self, function (node, cell, ctype, data)
            if ctype == XH.Req.TYPE.SUCCESS then
                self._imData:addToInviteList(data.messageInfo, true)
            end
        end)
    else
        reqAllMessageList:addReqCallBack(self, self.onReqMessageListCallBack)
    end
    reqAllMessageList:start(self._imData:getProposalAppid(), self._inviteCurPage, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqMessageListCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        if data.cur_package == 0 then
            self._inviteTotalPage = data.total_package
            self._imData:clearInviteList()
        end
        if self._inviteCurPage < data.total_package - 1 then
            self._inviteCurPage = data.cur_package + 1
        end
        self._imData:addToInviteList(data.messageInfo)
    end
end

function IMModule:reqReplyInvite(intviteId, replyType)
    local ReqReplyInviteGame = require("lobby.Req.Im.ReqReplyInviteGame")
    local reqReplyInviteGame = ReqReplyInviteGame:new()
    reqReplyInviteGame:addReqCallBack(self, self.onReqReplyInviteCallBack)
    reqReplyInviteGame:start(self._imData:getProposalAppid(), intviteId, replyType, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqReplyInviteCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        local roomid = data.game_roomid
        if roomid and roomid ~= 0 then
            self:joinRoom(roomid, data.ec == XH.IMProtocol.ERRORCODE.EC_ROOM_OWNER_CANT_AGREE_INVITE)
        end
    elseif ctype == XH.Req.TYPE.FAIL then
        XH.TipTool.showToast(data.msg)
        self:dispatchEvent({ name = self.EVENT_SHOW_JOIN_NOW, msg = {show = false} })
    end
end

function IMModule:reqTableListInfo()
    local ReqTableList = require("lobby.Req.Im.ReqFriendTableList")
    local reqTableList = ReqTableList:new()
    reqTableList:addReqCallBack(self, self.onReqTableListCallBack)
    reqTableList:start(self._imData:getProposalAppid(), self._curTableListPage, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqTableListCallBack(cell, ctype, data)
    XH.TipTool.hideLoading()
    if ctype == XH.Req.TYPE.SUCCESS then
        if data.cur_package == 0 then
            self._totalTableListPage = data.total_package
            self._imData:clearNormalTable()
        end
        if self._curTableListPage < data.total_package - 1 then
            self._curTableListPage = data.cur_package + 1
        end
        self._imData:addToTableList(data.friendTableInfo)
    end    
end

------------req mix table info start------------
function IMModule:reqMixTableListInfo()
    local ReqTeaList = require("lobby.Req.Im.ReqTeaList")
    local reqTeaList = ReqTeaList:new()
    reqTeaList:addReqCallBack(self, self.onReqTeaListCallBack)
    reqTeaList:start(XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqTeaListCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        if data and #data > 0 then
            self._imData:addTeaInfoToTableList(data)
            local tableListData = self._imData:getTableList()
            for _, v in pairs(tableListData) do
                if XH.areaData:isSupportFriendsV2() then 
                    if v.tableType and (v.tableType == self._imData.TABLEDATATYPE.TEAOPEN or v.tableType == self._imData.TABLEDATATYPE.TEANOTOPEN) then
                        self:reqTeaDetailTable(v.teaid)
                    end
                else
                    if v.tableType and v.tableType == self._imData.TABLEDATATYPE.TEAOPEN then
                        self:reqTeaDetailTable(v.teaid)
                    end
                end
            end
        else
            self._imData:clearTeaList()
        end
        self:reqTableListInfo()
    end
end

function IMModule:reqTeaDetailTable(teaid)
    if not teaid then
        return
    end
    local curPage = self._imData:getTeaTableCurPageByTeaId(teaid) or 0
    local ReqTeaDetailTable = require("lobby.Req.Im.ReqTeaDetailTable")
    local req = ReqTeaDetailTable:new()
    req:addReqCallBack(self, self.onReqTeaDetailTableCallBack)
    req:start(teaid, curPage, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqTeaDetailTableCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        if data.cur_package == 0 then
            self._imData:setTeaTableTotalPageByTeaId(data.teaid, data.total_package)
            self._imData:clearTeaTableByTeaId(data.teaid)
        end
        self._imData:setTeaTableCurPageByTeaId(data.teaid, data.cur_package + 1)
        self._imData:addTableToTea(data.teaid, data.tableInfos)
    end
end
------------req mix table info end------------

function IMModule:reqWillJoinTable(tableInfo,isNewProcess)
    XH.TipTool.showLoading()

    local gameid = tableInfo.gameid
    local gameAppid = tableInfo.game_appid
    local tableid = tableInfo.game_roomid
    self._isNewProcess = isNewProcess or false
    local ReqWillJoinTable = require("lobby.Req.Im.ReqWillJoinTable")
    local reqWillJoinTable = ReqWillJoinTable:new()
    reqWillJoinTable:addReqCallBack(self, self.onReqWillJoinTableCallBack)
    reqWillJoinTable:start(self._imData:getProposalAppid(), gameid, gameAppid, tableid, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqWillJoinTableCallBack(cell, ctype, data)
    XH.TipTool.hideLoading()
    if ctype == XH.Req.TYPE.SUCCESS then
        local roomid = data.tableid
        if roomid and roomid ~= 0 then
            self:joinRoom(roomid, data.ec == XH.IMProtocol.ERRORCODE.EC_ROOM_OWNER)
        end
    elseif ctype == XH.Req.TYPE.FAIL then
        XH.TipTool.showToast(data.msg)
    end
end

function IMModule:reqReplyWillJoinTable(areaid, numid, replyType)
    local ReqReplyWillJoinTable = require("lobby.Req.Im.ReqReplyWillJoinTable")
    local reqReplyWillJoinTable = ReqReplyWillJoinTable:new()
    reqReplyWillJoinTable:addReqCallBack(self, self.onReqReplyWillJoinTableCallBack)
    reqReplyWillJoinTable:start(self._imData:getProposalAppid(), areaid, numid, replyType, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqReplyWillJoinTableCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        XH.TipTool.showToast("请求已发送")
    elseif ctype == XH.Req.TYPE.FAIL then
        XH.TipTool.showToast(data.msg)
    end
end

function IMModule:reqReserveGame(areaid, numid)
    local ReqReserveGame = require("lobby.Req.Im.ReqReserveGame")
    local reqReserveGame = ReqReserveGame:new()
    reqReserveGame:addReqCallBack(self, self.onReqReserveGameCallBack)
    reqReserveGame:start(self._imData:getProposalAppid(), areaid, numid, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqReserveGameCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        XH.TipTool.showToast("预约已发送")
    elseif ctype == XH.Req.TYPE.FAIL then
        XH.TipTool.showToast(data.msg)
    end
end

function IMModule:reqReplyReserveGame(areaid, numid, replyType)
    local ReqReplyReserveGame = require("lobby.Req.Im.ReqReplyReserveGame")
    local reqReplyReserveGame = ReqReplyReserveGame:new()
    reqReplyReserveGame:addReqCallBack(self, self.onReqReplyReserveGameCallBack)
    reqReplyReserveGame:start(self._imData:getProposalAppid(), areaid, numid, replyType, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqReplyReserveGameCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        local roomid = data.roomid
        if roomid and roomid ~= 0 then
            self:joinRoom(roomid, data.ec == XH.IMProtocol.ERRORCODE.EC_ROOM_OWNER)
        end
    elseif ctype == XH.Req.TYPE.FAIL then
        XH.TipTool.showToast(data.msg)
    end
end

function IMModule:reqQuickInvite()
    local ReqQuickInvite = require("lobby.Req.Im.ReqQuickInvite")
    local reqQuickInvite = ReqQuickInvite:new()
    reqQuickInvite:addReqCallBack(self, self.onReqQuickInviteCallBack)
    reqQuickInvite:start(self._imData:getProposalAppid(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqQuickInviteCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        XH.TipTool.showToast("一键邀请已发送")
    elseif ctype == XH.Req.TYPE.FAIL then
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK_CANCEL,
        }, data.msg)
    end
end

function IMModule:reqTableInfo()
    local ReqTableInfo = require("lobby.Req.Im.ReqTableInfo")
    local reqTableInfo = ReqTableInfo:new()
    reqTableInfo:addReqCallBack(self, self.onReqTableInfoCallBack)
    reqTableInfo:start(self._imData:getProposalAppid(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqTableInfoCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        self:createShareInfo(data)
    elseif ctype == XH.Req.TYPE.FAIL then
        XH.TipTool.showToast(data.msg)
    end
end

function IMModule:reqHistoryFriends()
    self._imData:clearHistoryFriendsInfo()
    local ReqHistoryFriends = require("lobby.Req.Im.ReqHistoryFriends")
    local reqHistoryFriends = ReqHistoryFriends:new()
    reqHistoryFriends:start(self._imData:getProposalAppid(), XH.KW_CONFIG_LOGIC_TIME_OUT)

    XH.SysTool.performDelayOnce(function()
        self:dispatchEvent({ name = self.EVENT_HISTORY_FRIENDS, bShowDetail = false })
    end, 15)
end

function IMModule:reqShield(numID, bShield)
    local ReqShield = require("lobby.Req.Im.ReqShield")
    local reqShield = ReqShield:new()
    reqShield:addReqCallBack(self, self.onReqShieldCallBack)
    reqShield:start(self._imData:getProposalAppid(), numID, bShield, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqShieldCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        self._imData:setShieldState(data.numid, data.flag)
        self._imData:dispatch(self._imData.EVENT_LIST_DATA_CHANGED)
        XH.TipTool.showToast("操作成功")
    elseif ctype == XH.Req.TYPE.FAIL then
        XH.TipTool.showToast(data.msg)
    end
end

function IMModule:reqTargetFriend(numID)
    if not numID or not XH.areaData:isSupportFriendList() then
        return
    end
    local ReqTargetFriend = require("lobby.Req.Im.ReqTargetFriend")
    local reqTargetFriend = ReqTargetFriend:new()
    reqTargetFriend:addReqCallBack(self, self.onReqTargetFriendCallBack)
    reqTargetFriend:start(self._imData:getProposalAppid(), numID, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqTargetFriendCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        if data.bFriend == 1 then
            local sevenDays = 7*24*60*60
            if os.time() - data.info.last_fight_time > sevenDays then
                if not cc.UserDefault:getInstance():getBoolForKey("IMManager_play_warning" .. data.info.numid, false) then
                    local tmpName = XH.StringTool.cutStringByLength(clone(data.info.nickname))
                    XH.TipTool.showToast("请注意，与牌友" .. tmpName .. "的最后一次对局在7天前")
                    cc.UserDefault:getInstance():setBoolForKey("IMManager_play_warning" .. data.info.numid, true)
                else
                    cc.UserDefault:getInstance():setBoolForKey("IMManager_play_warning" .. data.info.numid, false)
                end
            end
        end
    end
end

function IMModule:reqApplyNum(maxtime)
    local ReqGetApplys = require("lobby.Req.Im.ReqGetApplys")
    local reqGetApplys = ReqGetApplys:new()
    reqGetApplys:addReqCallBack(self, self.onReqApplyNumCallBack)
    reqGetApplys:start(self._imData:getProposalAppid(), maxtime, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqApplyNumCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        self:getIMData():setApplyNum(data.totalcount or 0)
        if data.infocount ~= 0 then
            for _,v in ipairs(data.infos) do
                table.insert(self._applyList,v)
            end
            XH.SysTool.performWithDelayGlobal( function()
                self:reqApplyNum(data.mintime)
            end , 0.1)
        else
            self:dispatchEvent({ name = self.EVENT_APPLYLIST_CHANGE , info = self._applyList})  
            self._applyList = {}
        end
        XH.lobby:getModule("Im"):getIMData():dispatch(XH.lobby:getModule("Im"):getIMData().EVENT_LIST_DATA_CHANGED)
    end
end

function IMModule:reqFriendState(numId)
    local ReqAddFriendState = require("lobby.Req.Im.ReqAddFriendState")
    local reqAddFriendState = ReqAddFriendState:new()
    reqAddFriendState:addReqCallBack(self, self.onReqAddFriendStateCallBack)
    reqAddFriendState:start(self._imData:getProposalAppid(), numId, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqAddFriendStateCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        if data.playerinfo and data.playerinfo.numid ~= 0  then
            data.playerinfo.bFind = true
        else
            data.playerinfo.bFind = false
            XH.TipTool.showToast("该玩家信息不存在，请重试")
        end
        self:dispatchEvent({ name = self.EVENT_UPDATE_ADD_FRIEND_STATE , info = data})
    end
end

function IMModule:reqAddFriend(numId)
    local ReqAddFriend = require("lobby.Req.Im.ReqAddFriend")
    local reqAddFriend = ReqAddFriend:new()
    reqAddFriend:addReqCallBack(self, self.onReqAddFriendCallBack)
    reqAddFriend:start(self._imData:getProposalAppid(), numId, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqAddFriendCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        if data.flag == 0 then
            data.bChangeState = true
            XH.TipTool.showToast("申请牌友成功，等待对方同意")
            self:dispatchEvent({ name = self.EVENT_UPDATE_ADD_FRIEND_STATE , info = data})
            self:dispatchEvent({ name = self.EVENT_APPLYLIST_CHANGE})
        end
    end
end

function IMModule:reqDelFriend(numId)
    local ReqDelFriend = require("lobby.Req.Im.ReqDelFriend")
    local reqDelFriend = ReqDelFriend:new()
    reqDelFriend:addReqCallBack(self, self.onReqDelFriendCallBack)
    reqDelFriend:start(self._imData:getProposalAppid(), numId, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqDelFriendCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        if data.flag == 0 then
            self:dispatchEvent({ name = self.EVENT_APPLYLIST_CHANGE})
        end
    end
end

function IMModule:reqDelFriendApply(numId,bAccept)
    local ReqDelFriendApply = require("lobby.Req.Im.ReqDealFriendApply")
    local reqDelFriendApply = ReqDelFriendApply:new()
    self._bAccept = bAccept
    reqDelFriendApply:addReqCallBack(self, self.onReqDelFriendApply)
    reqDelFriendApply:start(self._imData:getProposalAppid(), numId , bAccept, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqDelFriendApply(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        if data.flag == 0 then
            self:dispatchEvent({ name = self.EVENT_APPLYLIST_CHANGE})
            if self._bAccept then
                XH.TipTool.showToast("已成功添加牌友~")
            else
                XH.TipTool.showToast("已拒绝该牌友~")
            end
            --请求牌友列表
            self:reqFriendListInfo()
        else
            XH.TipTool.showToast("操作失败，请重试~")
        end
    end
end

function IMModule:onNotifyFriendListChange(event)
    local resp =  IMProtocol.NotifyFriendListChange:new()
    resp:bistream(event.msg.buff, event.msg.len)
    if resp.flag == 1 then--同意添加通知
        if next(self:getData():getFriendList()) ~= nil then
            local tmpPage = XH.lobby:getModule("Im"):getCurFriendListPage() - 1 >= 0 and  XH.lobby:getModule("Im"):getCurFriendListPage() - 1 or 0
            for i = 0, tmpPage do
                XH.lobby:getModule("Im"):reqFriendListInfoEx(i, i)
            end
        else
            XH.lobby:getModule("Im"):reqFriendListInfo()
        end
    elseif resp.flag == 2 then--删除通知
        self:getIMData():refreshDealFriendList(resp.info,false)
    elseif resp.flag == 3 then--通知有申请，需要刷新牌友申请红点
        XH.lobby:getModule("Im"):reqApplyNum(0)
    else
        XH.TipTool.showToast("服务异常，请刷新重试")
    end
end

function IMModule:getData()
    if not self._imData then
        self._imData = XH.lobby:getModule("Im"):getIMData()
    end
    return self._imData
end

function IMModule:reqCreateGroup()
    local ReqCreateGroup = require("lobby.Req.Im.ReqOpenTeaHouse")
    local reqCreateGroup = ReqCreateGroup:new()
    reqCreateGroup:addReqCallBack(self, self.onReqCreateGroup)
    reqCreateGroup:start(XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onReqCreateGroup(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        if data.flag == 0 then
            XH.TipTool.showToast("分组创建完成，快去添加组员吧！")
            data.scene = "ADDGROUP"
            XH.viewManager:openView("IMAddGroupView", nil, {info = data})
            XH.lobby:getModule("TeaHouseList"):reqTeaHouseList()
        elseif data.flag == 1 then
            XH.TipTool.showToast("您没有权限")
        elseif data.flag == 2 then
            XH.TipTool.showToast("您身上的分组已达上限~")
        elseif data.flag == 6 then
            XH.TipTool.showToast("您已被封禁加入/创建亲友圈 ，无法进行该操作，如有疑问请联系客服。")
        end
    else
        XH.TipTool.showToast("操作失败，请重试~")
    end
end

function IMModule:reqInvitePlayer(teaid,numberId,reqCnt,isBegin)
    local ReqInvitePlayer = require("lobby.Req.Im.ReqInvitePlayer")
    local reqInvitePlayer = ReqInvitePlayer:new()
    if isBegin then
        self._reqInviteCnt = reqCnt
        self._totalInviteCnt =reqCnt
    end
    reqInvitePlayer:addReqCallBack(self, self.onRespInvitePlayer)
    reqInvitePlayer:start(teaid,numberId, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onRespInvitePlayer(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        self._reqInviteCnt = self._reqInviteCnt -1
        table.insert(self._tmpInviteTable,data)
        if self._reqInviteCnt == 0 then
            local successCnt = 0
            local failCnt = 0
            local failTable = {}
            for _,v in ipairs(self._tmpInviteTable) do
                if v.ucFlag == 0 then
                    successCnt = successCnt + 1
                elseif v.ucFlag == 2 then
                    failCnt = failCnt + 1
                    table.insert(failTable,v)
                end
            end
            if successCnt == self._totalInviteCnt then
                XH.TipTool.showToast("已成功添加组员~")
                self._tmpInviteTable = {}
                self:dispatchEvent({ name = self.EVENT_GORUP_MEMBER_LIST_UPDATE})
            elseif failCnt ~= 0 then
                local tipStr = ""
                for i=1,#failTable do
                    if i <= 3 then
                        if i == 3 then
                            tipStr = tipStr .. failTable[i].nNumid .. "等"..#failTable.."位玩家由于其分组数量已达上限，无法成功入组"
                            break
                        else
                            tipStr = tipStr .. failTable[i].nNumid                        
                            if failTable[i+1] then
                                tipStr = tipStr .."、"                                
                            end
                        end
                    end
                end
                if #failTable < 3 then
                    tipStr = tipStr .. "等"..#failTable.."位玩家由于其分组数量已达上限，无法成功入组"
                end
                XH.TipTool.showTip({
                    type = XH.TIP_LAYER_TYPE.OK,
                }, "您选择的"..tipStr)
                self._tmpInviteTable = {}
                self:dispatchEvent({ name = self.EVENT_GORUP_MEMBER_LIST_UPDATE})
            else
                XH.TipTool.showToast("服务错误，请重试")
            end 
        end

        -- if data.ucFlag == 0 then
        --     XH.TipTool.showToast("玩家"..numberId.."添加成功")
        --     self:dispatchEvent({ name = self.EVENT_GORUP_MEMBER_LIST_UPDATE})
        -- elseif data.ucFlag == 1 then
        --     XH.TipTool.showToast("玩家"..numberId.."添加失败\n没有权限")
        -- elseif data.ucFlag == 2 then
        --     XH.TipTool.showToast("玩家"..numberId.."添加失败\n分组人员已满")
        -- elseif data.ucFlag == 3 then
        --     XH.TipTool.showToast("玩家"..numberId.."添加失败\n分组玩家数满了")
        -- elseif data.ucFlag == 4 then
        --     XH.TipTool.showToast("玩家"..numberId.."添加失败\n已经是分组的成员了")
        -- elseif data.ucFlag == 5 then
        --     XH.TipTool.showToast("玩家"..numberId.."添加失败\n玩家不存在")
        -- end
    end
end

function IMModule:reqGroupMemberList(teaNumber)
    local ReqCreateGroup = require("lobby.Req.Im.ReqTeaMemberList")
    local reqCreateGroup = ReqCreateGroup:new()
    reqCreateGroup:addReqCallBack(self, self.onReqGroupMemberList)
    reqCreateGroup:start(teaNumber, XH.KW_CONFIG_LOGIC_TIME_OUT)--self._imData:getProposalAppid()
end

function IMModule:onReqGroupMemberList(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        if data then
            self:dispatchEvent({ name = self.EVENT_TEA_HOUSE_LIST_CHANGED,info = data})
        end
    end
end

function IMModule:reqCloseTeaHouse(teaNumber)
    local ReqCloseTeaHouse = require("lobby.Req.Im.ReqCloseTeaHouse")
    local reqCloseTeaHouse = ReqCloseTeaHouse:new()
    reqCloseTeaHouse:addReqCallBack(self, self.onRespCloseTeahouse)
    reqCloseTeaHouse:start(teaNumber, XH.KW_CONFIG_LOGIC_TIME_OUT)--self._imData:getProposalAppid()
end

function IMModule:onRespCloseTeahouse(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        if data.flag == 0 then
            XH.TipTool.showToast("分组已解散~")
            XH.lobby:getModule("TeaHouseList"):reqTeaHouseList()
        else
            XH.TipTool.showToast("操作失败，请重试~")
        end
    end
end

function IMModule:reqQuitTeaHouse(teaNumber)
    local ReqQuitTeaHouse = require("lobby.Req.Im.ReqQuitTeaHouse")
    local reqQuitTeaHouse = ReqQuitTeaHouse:new()
    reqQuitTeaHouse:addReqCallBack(self, self.onRespQuitTeahouse)
    reqQuitTeaHouse:start(teaNumber, XH.KW_CONFIG_LOGIC_TIME_OUT)--self._imData:getProposalAppid()
end

function IMModule:onRespQuitTeahouse(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        if data.state == 0 then
            XH.TipTool.showToast("已退出分组")
            XH.lobby:getModule("TeaHouseList"):reqTeaHouseList()
        else
            XH.TipTool.showToast("操作失败，请重试~")
        end
    end
end

function IMModule:reqKickMember(teaNumber,numId)
    local RespKickUser = require("lobby.Req.Im.ReqKickUser")
    local respKickUser = RespKickUser:new()
    self._kickUserId = numId
    respKickUser:addReqCallBack(self, self.onRespKickUser)
    respKickUser:start(teaNumber, numId,XH.KW_CONFIG_LOGIC_TIME_OUT)--self._imData:getProposalAppid()
end

function IMModule:onRespKickUser(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        if data.flag == 0 then
            XH.TipTool.showToast("已将玩家移出分组")
            self:dispatchEvent({ name = self.EVENT_MEMBERLIST_KICK,numberId = self._kickUserId})
        else
            XH.TipTool.showToast("服务异常，请刷新重试")
        end
    end
end

function IMModule:reqTeaHouseCost(teaNumber)
    local ReqTeaHouseCost = require("lobby.Req.Im.ReqTeaHouseCost")
    local reqTeaHouseCost = ReqTeaHouseCost:new()
    reqTeaHouseCost:addReqCallBack(self, self.onRespTeaHouseCost)
    reqTeaHouseCost:start(teaNumber,XH.KW_CONFIG_LOGIC_TIME_OUT)--self._imData:getProposalAppid()
end

function IMModule:onRespTeaHouseCost(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        local tmpData = data
        self:dispatchEvent({ name = self.EVENT_COST_UPDATE,info = tmpData })
    else
        XH.TipTool.showToast("服务异常，请刷新重试")
    end
end

function IMModule:reqUserInfoListCnt(teaNumber)
    local ReqUserInfoListCnt = require("lobby.Req.Im.ReqUserInfoListCnt")
    local reqUserInfoListCnt = ReqUserInfoListCnt:new()
    reqUserInfoListCnt:addReqCallBack(self, self.onRespUserInfoListCnt)
    reqUserInfoListCnt:start(teaNumber,XH.KW_CONFIG_LOGIC_TIME_OUT)--self._imData:getProposalAppid()
end

function IMModule:onRespUserInfoListCnt(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        local tmpData = data
        self:dispatchEvent({ name = self.EVENT_PLAYERCNT_UPDATE,info = tmpData })
    else
        XH.TipTool.showToast("服务异常，请刷新重试")
    end
end

function IMModule:reqTeaHouseLog(teaNumber)
    local ReqTeaHouseLog = require("lobby.Req.Im.ReqTeaHouseLog")
    local reqTeaHouseLog = ReqTeaHouseLog:new()
    reqTeaHouseLog:addReqCallBack(self, self.onRespTeaHouseLog)
    reqTeaHouseLog:start(teaNumber,XH.TeaHouseProtocol.ReqOperationHistory.TYPE.TEA,0,100,XH.KW_CONFIG_LOGIC_TIME_OUT)--self._imData:getProposalAppid()
end

function IMModule:onRespTeaHouseLog(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        local tmpData = data
        self:dispatchEvent({ name = self.EVENT_UPDATE_TEA_RECORD,info = tmpData })
    else
        XH.TipTool.showToast("服务异常，请刷新重试")
    end
end

function IMModule:reqRankUserInfo(teaNumber,startDay, endDay, gameID)
    local ReqTeaHouseLog = require("lobby.Req.Im.ReqRankUserInfo")
    local reqTeaHouseLog = ReqTeaHouseLog:new()
    reqTeaHouseLog:addReqCallBack(self, self.onRespRankUserInfo)
    reqTeaHouseLog:start(teaNumber,reqTeaHouseLog.RANK_TYPE.COST,startDay,endDay,false,gameID,XH.KW_CONFIG_LOGIC_TIME_OUT)--self._imData:getProposalAppid()
end

function IMModule:onRespRankUserInfo(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        self:dispatchEvent({ name = self.EVENT_UPDATE_TEA_RECORD,msg = { info = data }  })
    else
        XH.TipTool.showToast("服务异常，请刷新重试")
    end
end

function IMModule:reqCostRank(teaNumber,startDay, endDay, gameID)
    local ReqCostRank = require("lobby.Req.Im.ReqRankUserInfo")
    local reqCostRank = ReqCostRank:new()
    reqCostRank:addReqCallBack(self, self.onRespCostRank)
    reqCostRank:start(teaNumber,reqCostRank.RANK_TYPE.COST,startDay,endDay,false,gameID,XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onRespCostRank(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        self:dispatchEvent({ name = self.EVENT_COST_RANK_UPDATE,msg = { info = data }  })
    else
        XH.TipTool.showToast("服务异常，请刷新重试")
    end
end

function IMModule:reqScoreRank(teaNumber,startDay, endDay, gameID)
    local ReqScoreRank = require("lobby.Req.Im.ReqRankUserInfo")
    local reqScoreRank = ReqScoreRank:new()
    reqScoreRank:addReqCallBack(self, self.onRespScoreRank)
    reqScoreRank:start(teaNumber,reqScoreRank.RANK_TYPE.SCORE,startDay,endDay,false,gameID,XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onRespScoreRank(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        self:dispatchEvent({ name = self.EVENT_SCORE_RANK_UPDATE,msg = { info = data }  })
    else
        XH.TipTool.showToast("服务异常，请刷新重试")
    end
end

function IMModule:reqBigWinnerRank(teaNumber,startDay, endDay, gameID)
    local ReqBigWinnerRank = require("lobby.Req.Im.ReqRankUserInfo")
    local reqBigWinnerRank = ReqBigWinnerRank:new()
    reqBigWinnerRank:addReqCallBack(self, self.onRespBigWinnerRank)
    reqBigWinnerRank:start(teaNumber,reqBigWinnerRank.RANK_TYPE.BIG_WINNER,startDay,endDay,false,gameID,XH.KW_CONFIG_LOGIC_TIME_OUT)--self._imData:getProposalAppid()
end

function IMModule:onRespBigWinnerRank(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        self:dispatchEvent({ name = self.EVENT_BIG_WINNER_RANK_UPDATE,msg = { info = data }  })
    else
        XH.TipTool.showToast("服务异常，请刷新重试")
    end
end

function IMModule:reqCurTime()
    local ReqCurTime = require("lobby.Req.Im.ReqCurTime")
    local reqCurTime = ReqCurTime:new()
    reqCurTime:addReqCallBack(self, self.onRespCurTime)
    reqCurTime:start(XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onRespCurTime(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        self:dispatchEvent({ name = self.EVENT_RANK_CUR_DATE_UPDATE,msg = { nTime = data.nTime } })
    else
        XH.TipTool.showToast("服务异常，请刷新重试")
    end
end

function IMModule:reqSetTeaInfo(teaNum,teaName)
    local ReqSetTeaInfo = require("lobby.Req.Im.ReqSetTeaInfo")
    local reqSetTeaInfo = ReqSetTeaInfo:new()
    reqSetTeaInfo:addReqCallBack(self, self.onRespSetTeaInfo)
    reqSetTeaInfo:start(teaNum,teaName,XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onRespSetTeaInfo(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        if data.nErrorCode == 0 then 
            XH.TipTool.showToast("分组名称设置成功")
            XH.lobby:getModule("TeaHouseList"):reqTeaHouseList()
        else
            XH.TipTool.showToast("设置失败，请刷新重试")
        end
    else
        XH.TipTool.showToast("服务异常，请刷新重试")
    end
end

function IMModule:ReqRegisterAgent(nAgentAreaId,acPhoneNum,acHeadUrl,acNickName)
    local ReqRegisterAgent = require("lobby.Req.Im.ReqRegisterAgent")
    local reqRegisterAgent = ReqRegisterAgent:new()
    reqRegisterAgent:addReqCallBack(self, self.onRespRegisterAgent)
    reqRegisterAgent:start(nAgentAreaId,acPhoneNum,acHeadUrl,acNickName,XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function IMModule:onRespRegisterAgent(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        XH.areaData:setVirtualAgentState(true)
        XH.lobby:getModule("Im"):reqCreateGroup()
    else
        XH.TipTool.showToast(cell:getMessage())
    end
end

function IMModule:checkCanSendSelfFightInfoReq(teaNumber)
    if not self._sendSelfFightInfoTime then
        self._sendSelfFightInfoTime = {}
    end
    local nowTime = os.time()
    local lastTime = self._sendSelfFightInfoTime[teaNumber] or 0
    local leftTime = IMModule.SendReqColdTime - (nowTime - lastTime)
    if self._sendSelfFightInfoTime[teaNumber]  and leftTime > 0 then
        return false
    end
    self._sendSelfFightInfoTime[teaNumber] = nowTime
    return true
end

function IMModule:reqSelfFightInfo(teaNumber)
    local sendReq = function( )
        local ReqSelfFightInfo = require("lobby.Req.Im.ReqSelfFightInfo")
        local reqSelfFightInfo = ReqSelfFightInfo:new()
        reqSelfFightInfo:addReqCallBack(self, self.onRespReqSelfFightInfo)
        reqSelfFightInfo:start(teaNumber, nil, XH.KW_CONFIG_LOGIC_TIME_OUT)
    end
    if self:checkCanSendSelfFightInfoReq(teaNumber)  then
        sendReq()
    else
        if self._selfFightInfo then
            self:dispatchEvent({ name = self.EVENT_LATELY_LIST_CHANGED, msg = { info = self._selfFightInfo } })
        else
            sendReq()
        end
    end
end

function IMModule:onRespReqSelfFightInfo(req, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        self._selfFightInfo = data
        self:dispatchEvent({ name = self.EVENT_LATELY_LIST_CHANGED, msg = { info = data } })
    else
        XH.TipTool.showTip(req:getMessage())
    end
end

---------------------------------------------------

function IMModule:getPlayerPosition()
    local position = XH.playerData:getPlayerPosition()
    return position
end

function IMModule:judgePlayerInRoom()
    local playerPos = self:getPlayerPosition()
    if playerPos.gameAppID and playerPos.gameAppID == 0 then
        XH.viewManager:openView("IMTipsLayerView", nil, {ctype = "CreateTips"})
        return false
    end
    return true
end

function IMModule:judgePlayerInGame()
    local playerPos = self:getPlayerPosition()
    if playerPos.gameAppID and playerPos.gameAppID == 0 then
        return false
    end
    return true
end

function IMModule:changeTime2Str(time)
    local str = os.date("%H:%M", time)
    return str
end

function IMModule:joinRoom(roomid, bHost)
    local position = self:getPlayerPosition()
    if roomid == position.roomID then
        XH.TipTool.showToast("您已经在该房间中")
        return
    end
    if position.gameAppID and position.gameAppID ~= 0 then
        if bHost then
            if not cc.UserDefault:getInstance():getBoolForKey("IMManager_host_reply_invite_" .. XH.areaData:getLobbyID(), false) then
                XH.viewManager:openView("IMHostDismissView", nil, roomid)
            else
                self:joinRoomAfterDismiss(roomid)
            end
        else
            local doJoin = function()
                if XH.gameManager then
                    XH.gameManager:leaveGame()
                else
                    local gameStartData = self:getGameStartData()
                    XH.roomManager:leaveRoomFunc(gameStartData)
                end
                if self:judgeInTeaHouse() and TeaHouse then
                    TeaHouse.manager.teahouseGeneral:levealTeaHouse()
                    XH.SysTool.performDelayOnce(function()
                        XH.lobby:getModule("JoinBoxRoom"):reqJoinBoxRoom(roomid)
                    end, 0.5)
                else
                    XH.lobby:getModule("JoinBoxRoom"):reqJoinBoxRoom(roomid)
                end
            end
           if self._isNewProcess then
                if not cc.UserDefault:getInstance():getBoolForKey("IMListLayer_Skip_TipLayer",false) then
                    XH.viewManager:openView("IMTeaHouseConfirmView",nil,{doAction = doJoin,text = "您已有房间，必须退出当前房间才能加入新房间，是否退出？"})
                else
                    XH.TipTool.showToast("已经为您退出房间,并加入新房间")
                    doJoin()
                end
            else
                doJoin()
            end
        end
    elseif self._imData:getIsSeer() and XH.gameManager then
        XH.gameManager:leaveGame()
        if self:judgeInTeaHouse() and TeaHouse then
            TeaHouse.manager.teahouseGeneral:levealTeaHouse()
            XH.SysTool.performDelayOnce(function()
                XH.lobby:getModule("JoinBoxRoom"):reqJoinBoxRoom(roomid)
            end, 0.5)
        else
            XH.lobby:getModule("JoinBoxRoom"):reqJoinBoxRoom(roomid)
        end
    else
        XH.lobby:getModule("JoinBoxRoom"):reqJoinBoxRoom(roomid)
    end
end

function IMModule:joinRoomAfterDismiss(roomid)
    if XH.gameManager and XH.gameManager.sendRequestDismiss then
        local doJoin = function()
            XH.gameManager:sendRequestDismiss()
            XH.SysTool.performDelayOnce(function()
            XH.lobby:getModule("JoinBoxRoom"):reqJoinBoxRoom(roomid)
        end, 0.1)
        end
        if self._isNewProcess then 
            if not cc.UserDefault:getInstance():getBoolForKey("IMListLayer_Skip_TipLayer",false) then
                XH.viewManager:openView("IMTeaHouseConfirmView",nil,{doAction = doJoin,text = "您是房主，必须解散房间才能加入新房间，是否解散？"})
            else
                XH.TipTool.showToast("已经为您解散房间,并加入新房间")
                doJoin()
            end
        else
            doJoin()
        end
        
    else
        XH.TipTool.showToast("房主请先解散房间")
    end
end

function IMModule:getGameStartData()
    local pos = self:getPlayerPosition()
    local param = {}
    param.appID = pos.gameAppID
    param.gameID = pos.gameID
    param.roomMode = XH.ROOM_MODE.BOXROOM
    param.roomID = pos.roomID
    param.srsGroupID = pos.srsGroupID
    local gameStartData = require("app.Data.GameStartData").new(param)
    return gameStartData
end

function IMModule:judgeInTeaHouse()
    return XH.teaHouseManager:isInTeaHouse()
end

function IMModule:createShareInfo(data)
    local boxRoomId = data.roomid
    local maxPlayCount = data.maxcount
    local maxPlayerCount = data.chairs
    local teaHousePwd = data.teapwd
    local titleStr = data.gamename
    local strURL = XH.areaData:getXHLinkURL()
    local strText = "房间号:" .. boxRoomId .. " 局数:" .. maxPlayCount .. " 房间规则:" .. maxPlayerCount .. "人/"
    if XH.gameManager and XH.gameManager.getGameInfoData then
        strText = strText .. XH.gameManager:getGameInfoData():getGameRule()
    else
        if data.paytype == XH.IMProtocol.PAYTYPE.HOST then
            strText = strText .. "房主支付"
        elseif data.paytype == XH.IMProtocol.PAYTYPE.AA then
            strText = strText .. "平摊支付"
        else
            strText = strText .. "圈主支付"
        end
    end

    if teaHousePwd and teaHousePwd ~= 0 then
        strText = string.format("亲友圈口令:%06d ", teaHousePwd) .. strText
        strURL = strURL .. "?key=house_" .. teaHousePwd .. "_table_" .. boxRoomId
    else
        strURL = strURL .. "?key=" .. boxRoomId
    end

    local shareInfo = {
        textTitle = titleStr, --分享标题
        textTitleUrl = "", --微信不用
        site = "", --微信不用
        siteUrl = "", --微信不用
        imagePath = "", --微信不用
        url = strURL, --微信网页模式分享的链接
        type = "url", --word是文字分享，url网页模式分享，pic分享图片
        scene = "0", --0是分享到好友会话，1是分享到朋友圈
        imageUrl = "", --分享图片地址（分享网页模式，需要传安卓包名）
        text = strText, --分享内容 
        comment = "", --微信不用
        copyTextWhenNoWX = strText, --未安装微信时复制到剪贴板的内容
    }

    XH.sdkManager:startShare(shareInfo)
end

function IMModule:changeDataToMessage(id, fromAreaid, fromNumid, toAreaid, toNumid, time, fromNickName, toNickName, msgType)
    local msgInfo = {}
    msgInfo.id = id
    msgInfo.fromAreaid = fromAreaid
    msgInfo.fromNumid = fromNumid
    msgInfo.toAreaid = toAreaid
    msgInfo.toNumid = toNumid
    msgInfo.time = time
    msgInfo.fromNickName = fromNickName
    msgInfo.toNickName = toNickName
    msgInfo.msgType = msgType

    local result = {}
    table.insert(result, msgInfo)
    return result
end

function IMModule:removeMessageFromData(numid, msgType)
    self._imData:removeMessageFromData(numid, msgType)
end

function IMModule:getReplaceName(gameName)
    if (not string.find(gameName, "玩法")) and (string.find(gameName, "广式64张") or string.find(gameName, "红十") or string.find(gameName, "挖花") or string.find(gameName, "天台三阿磨") or string.find(gameName, "斗地主") or string.find(gameName, "推倒胡")) then
        return gameName .. "玩法"
    end
    if string.find(gameName, "桐乡麻将") or string.find(gameName, "嘉善麻将") or string.find(gameName, "平湖麻将") or string.find(gameName, "长兴麻将") or string.find(gameName, "织里麻将") or string.find(gameName, "胡数麻将") or string.find(gameName, "宁波麻将") or string.find(gameName, "慈溪麻将") or string.find(gameName, "推倒胡麻将") or string.find(gameName, "台炮麻将") or string.find(gameName, "杭州麻将") or string.find(gameName, "开化麻将") or string.find(gameName, "龙游麻将") or string.find(gameName, "江山麻将") or string.find(gameName, "常山麻将") or string.find(gameName, "遂昌麻将") or string.find(gameName, "建德麻将") or string.find(gameName, "定海麻将") or string.find(gameName, "沈家门麻将") or string.find(gameName, "岱山麻将") or string.find(gameName, "嵊泗麻将") or string.find(gameName, "温岭麻将") or string.find(gameName, "玉环麻将") or string.find(gameName, "三门麻将") or string.find(gameName, "大溪麻将") then
        return string.gsub(gameName, "麻将", "玩法")
    end
    return gameName
end

function IMModule:getRoomInfoStr(gameName, playerCnt, payType)
    local str = ""
    if gameName and gameName ~= "" then
        str = str .. self:getReplaceName(gameName)
    end
    if playerCnt then
        str = str .. ", " .. playerCnt .. "人"
    end
    if payType then
        if payType == XH.IMProtocol.PAYTYPE.HOST then
            str = str .. ", " .. "房主支付"
        elseif payType == XH.IMProtocol.PAYTYPE.AA then
            str = str .. ", " .. "平摊支付"
        else
            str = str .. ", " .. XH.StringTool.replaceMatchStr("圈主支付")
        end
    end
    return str
end

function IMModule:updateHeadImg(node, url, defaultImg)
    if not node then
        return 
    end
    local size = node:getContentSize()
    if not url or url == "" then
        ccui.ImageView:create(defaultImg or "friend_defult_head_img.png", ccui.TextureResType.plistType)
        :setPosition(size.width / 2, size.height / 2)
        :ignoreContentAdaptWithSize(false)
        :setContentSize(size.width, size.height)
        :addTo(node)
        return
    end

    local imageNode = XH.RemoteImage.new()
        :setPosition(size.width/2, size.height/2)
        :ignoreContentAdaptWithSize(false)
        :setContentSize(size.width, size.height)
        :addTo(node)

    imageNode:setUrl(url, true)
end

function IMModule:onTouchShare(callBackFunc)
    if device.platform == "windows" then
        self:onAwardRecallCallBack()
        return
    end

    local shareText = "马上开局就缺你了，您的好友【" .. XH.playerData:getNickName() .. "】邀请您上线游戏！"
    local url = XH.areaData:getXHLinkURL()--[["https://mmbizurl.cn/s/hCTWI5pUn"]]
    local shareInfo = {
        textTitle = "好友邀请通知", --分享标题
        textTitleUrl = "", --微信不用
        site = "", --微信不用
        siteUrl = "", --微信不用
        imagePath = "", --微信不用
        url = url, --微信网页模式分享的链接
        type = "url", --word是文字分享，url网页模式分享，pic分享图片
        scene = "0", --0是分享到好友会话，1是分享到朋友圈
        imageUrl = "com.xm.zjgamecenter", --分享图片地址（分享网页模式，需要传安卓包名）
        text = shareText, --分享内容 
        comment = "download", --微信不用, 用以区分直接分享下载地址和游戏内
        copyTextWhenNoWX = shareText, --未安装微信时复制到剪贴板的内容
    }
    
    if callBackFunc then
        XH.sdkManager:startShare(shareInfo,callBackFunc,self)
    else
        XH.sdkManager:startShare(shareInfo,self.shareCallback,self)
    end
end

--无论取消还是成功，都当成时分享成功处理
function IMModule:shareCallback(data)
    self:onShareSuccessCallBack()
end

function IMModule:onShareSuccessCallBack()
--    XH.taskManager:reqShareGiveRoomCard()
end

function IMModule:onAwardRecallCallBack()
    XH.taskManager:reqFriendAwardRecall()
end

function IMModule:setCurFriendListPage(page)
    self._curFriendListPage = page
end

function IMModule:getCurFriendListPage()
    return self._curFriendListPage
end

function IMModule:setTotalFriendListPage(page)
    self._totalFriendListPage = page
end

function IMModule:getTotalFriendListPage()
    return self._totalFriendListPage
end

function IMModule:setCurTableListPage(page)
    self._curTableListPage = page
end

function IMModule:getCurTableListPage()
    return self._curTableListPage
end

function IMModule:setTotalTableListPage(page)
    self._curTableListPage = page
end

function IMModule:getTotalTableListPage()
    return self._curTableListPage
end

--获取当前的默认游戏id
function IMModule:getAreaDefaultGameID()
    return XH.areaData:getConfigGameID()
end

--获取当前区的 唯一标识
function IMModule:getLobbyID()
    return XH.areaData:getLobbyID()
end

--获取srs组
function  IMModule:getSrsGroupID()
    return XH.areaData:getSrsGroupID()
end

function IMModule:getReserveInfo()
    return self._reserveInfo
end

function IMModule:clearReserveInfo()
    self._reserveInfo = nil
end

function IMModule:createBoxRoom(data)
    XH.TipTool.showLoading()
    local createFunc = function(gameRuleEx)
        local bNotAutoSit = false
        local ucCreateType = XH.TeaHouseProtocol.ReqCreateTableAutoSit.CREATETYPE.TY_TEA_PlAYER
        if data.bOwner  then
            ucCreateType = XH.TeaHouseProtocol.ReqCreateTableAutoSit.CREATETYPE.TY_PROXY
        end
        local info = {
            gameid = data.gameid, base = data.base, cont = data.cont, type = data.type,
            size = data.size, name = data.name, data = data.data, tableCnt = data.tableCnt,
            ucCreateType = ucCreateType, preCardID = 0, nPayType = data.payType, bNotAutoSit = bNotAutoSit,
            nModelIndex = data.nPlayModeId, nTeaid = data.nTeaid
        }

        if gameRuleEx ~= nil then
            info.data = info.data .. gameRuleEx
        end

        local ReqCreateTableAutoSit = require("lobby.Req.Im.ReqCreateTableAutoSit")
        local reqCreate = ReqCreateTableAutoSit:new()
        reqCreate:addReqCallBack(self, self.onNetReqCreateTableAutoSit)
        reqCreate:start(info,15)
    end

    self._isCreateNeedLoaction = XH.roomManager:isNeedLocation(data.data)
    if self._isCreateNeedLoaction and  device.platform ~= "windows" then
        XH.TipTool.showLoading()
        self._tempCreateCallback = createFunc
        if device.platform == "android" then
            XH.sdkManager:callFunction("user_initiative")
        end
        XH.sdkManager:startLocationByAmap(XH.sdkManager.AmapAccuracy.MID)
    else
        createFunc()
    end
end

function IMModule:onGetLocationCallBack(event)
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

function IMModule:onNetReqCreateTableAutoSit(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then 
        if data and #data == 1 then
            local tableInfo = data[1]
            if tableInfo.bIsNewProcedure then
                XH.SysTool.performWithDelayGlobal(function()
                    XH.TipTool.hideLoading()
                    self:gameStart({
                        appID = tableInfo.nGameAppId,
                        gameID = tableInfo.nGameId,
                        roomMode = XH.ROOM_MODE.BOXROOM,
                        roomID = tableInfo.acPasswd,
                        teaHousePwd = cell._nTeaHousePwd,
                        teaHouseTitle = cell._teaHouseTitle,
                        tablePwd = tableInfo.acPasswd,
                        isNewProcess = true
                    })
                end,0.05)
            end
        else
            XH.TipTool.hideLoading()
        end
    else
        XH.TipTool.hideLoading()
        XH.TipTool.showToast(cell:getMessage())
    end
end

function IMModule:gameStart(data)
    local param = {}
    param.appID = data.appID
    param.gameID = data.gameID
    param.roomMode = data.roomMode
    param.roomID = data.roomID
    XH.roomManager:gameStart(param)
end

function IMModule:isContainSelf(tableInfo)
    local players = tableInfo.players
    for _, v in pairs(players) do
        if v.numid == XH.playerData:getNumberID() then
            return true
        end
    end
    return false
end

function IMModule:checkIsGroup(teaid)
    local list = self._imData:getGroupList()
    for _, v in pairs(list) do
        if tonumber(teaid) == v.nPasswd then
            return true
        end
    end
    return false
end

function IMModule:checkCanInvite(numID)
    if not self._inviteTime then
        self._inviteTime = {}
    end
    numID = type(numID) == "table" and 0 or numID
    local nowTime = os.time()
    local lastTime = self._inviteTime[numID] or 0
    local leftTime = IMModule.InviteColdTime - (nowTime - lastTime)
    if self._inviteTime[numID] and leftTime > 0 then
        XH.TipTool.showToast("请" .. leftTime .. "S后重试")
        return false
    end
    self._inviteTime[numID] = nowTime
    return true
end


function IMModule:_reqInvite(fromNumID, toNumID, msg)
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
    XH.TipTool.showToast("邀请已发送")
end

function IMModule:_reqInviteAll(fromNumID, toNumIDs, msg)
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
    XH.TipTool.showToast("邀请成功")
end

function IMModule:_reqInviteUser(fromNumID, toNumID, msg)
    if type(toNumID) == "table" then
        self:_reqInviteAll(fromNumID, toNumID, msg)
    else 
        self:_reqInvite(fromNumID, toNumID, msg)
    end
end

function IMModule:_getEnterTeaHouseData() 
    local teaNumber = XH.lobby:getModule("Im"):getIMData():getEnterTeaHouseID()
    local data = self:getIMData():getGroupList()
    for _, v in pairs(data) do
        if v.nTeaNumber == teaNumber then
            return  v
        end
    end 
    return nil
end

function IMModule:reqInvite(numID)
    if not self:checkCanInvite(numID) then
        return
    end
    
    local teahouseData = self:_getEnterTeaHouseData()
    if teahouseData then
        local name = teahouseData.acOwnerNick
        local nameExt = teahouseData.szData
        local teahouseName
        if nameExt and #nameExt > 0 then
            teahouseName = string.format("牌友%s的组(%s)", XH.StringTool.getTrimName(name), nameExt)
        else
            teahouseName = string.format("牌友%s的组", XH.StringTool.getTrimName(name))
        end
        local msg = {
            sFromNickName = XH.playerData:getNickName(),
            sTeaHouseName = teahouseName,
            sTeaHouseID = teahouseData.nPasswd
        }
        local postion = XH.playerData:getPlayerPosition()
        msg.boxRoomID = postion.roomID
        msg.gameID = postion.gameID
        msg.gameAppID = postion.gameAppID
        msg.boxRoomName = ""
        local gameNames = XH.areaData:getAreaGameNameList()
        if gameNames[postion.gameID] then
            msg.boxRoomName = gameNames[postion.gameID]
        end
        msg.boxRoomRule = ""
        local tables = self:getIMData():getFilterTableListData(self:getIMData().GROUP_TYPE.GROUP,postion.teaID)
        for _, v in pairs(tables) do
            if self:isContainSelf(v) then
                if v.gameRule then
                    local luaString = "return {" .. v.gameRule .. "}"
                    local luaFunction = loadstring(luaString)
                    if luaFunction then
                        local rule = luaFunction()
                        if rule then
                            msg.boxRoomRule = rule.ZhRule
                        end
                    end
                end   
            end
        end
        self:_reqInviteUser(XH.playerData:getNumberID(), numID, json.encode(msg))
    else
        XH.TipTool.showToast("分组信息异常,无法邀请,请联系客服")
    end
end

function IMModule:newThrowData(eventName , extraData)
    local tmpData = {}
    if extraData then
        table.merge(tmpData,extraData)
    end
    if XH.lobby:getModule("Im"):judgePlayerInGame() then
        tmpData.page = "P1119"
    else
        tmpData.page = "P1063"        
    end
    XH.NewThrowDataManager:throwData(eventName,tmpData)
end

function IMModule:saveGameInfo(nTeaid,tableInfo)
    local createBox = {}
    createBox.bNotAutoSit = false
    createBox.cont = tableInfo.gameCount
    createBox.data = self:filterRoomRule(tableInfo.gameRule)
    local gameNames = XH.areaData:getAreaGameNameList()
    createBox.name= gameNames[tableInfo.gameid]
    createBox.nTeaid = nTeaid
    createBox.size = tableInfo.chairs
    createBox.gameid = tableInfo.gameid
    createBox.payType = tableInfo.paytype
    createBox.type = 0 
    createBox.base = 1
    createBox.tableCnt = 1
    cc.UserDefault:getInstance():setStringForKey("IMListLayer_Last_Game_Group_".. nTeaid,json.encode(createBox))
    if tableInfo.gameid then
        XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_QUICK_CREATEROOM,{item_id = tableInfo.gameid})
    end
    return createBox
end

local FILTER_KEY_WORD = {"demo_rich","distance","longitude","latitude","bOpenGPS","bCheckPos"}
-- 过滤房间信息,防止出现上抛数据过长
function IMModule:filterRoomRule(gameRule)
    if gameRule and gameRule ~= "" then
        local rule_table = string.split(gameRule,";")
        for i = #rule_table, 1,-1 do
            for j = 1,#FILTER_KEY_WORD do
                if string.find(rule_table[i],FILTER_KEY_WORD[j])  then
                    table.remove( rule_table,i)
                end
            end
        end
        local ruleStr = ""
        for _,v in pairs(rule_table) do
            if v ~= "" then
                ruleStr = ruleStr .. v .. ";"
            end
        end
        -- 检查下是否有双分号
        ruleStr = string.gsub(ruleStr,";;",";")
        return ruleStr
    end
    return gameRule
end

function IMModule:JudgeCanWatch(roomId)
    local ConfigurationDefine = require("lobby.Modules.Configuration.Define")
    local lobbyJsonData = XH.lobby:getModule("Configuration"):getConfigJsonData(ConfigurationDefine.Config.LOBBY, "lobby")
    local playerPos = self:getPlayerPosition()
    if lobbyJsonData and lobbyJsonData.openGPWatch then
        if playerPos.gameAppID and playerPos.gameAppID == 0 then
            return true
        end
        if XH.gameManager and not XH.gameManager:isPlayBack() and self._imData:getIsSeer() then
            if playerPos.roomID == roomId then
                return false
            else
                return true
            end
        end
        return false
    else
        if playerPos.gameAppID and playerPos.gameAppID ~= 0 then
            return false
        end
        return roomId ~= 0 and self._imData:getWatchRoomId() ~= roomId
    end
end

function IMModule:judgeSceneName()
    local sceneName = nil
    local inTeahouse = self:judgeInTeaHouse() 
    if inTeahouse then
        sceneName = XH.gameManager and "亲友圈房间" or "亲友圈"
    else
        sceneName = XH.gameManager and "包厢房间" or "大厅"
    end
    return sceneName
end

return IMModule
 # 