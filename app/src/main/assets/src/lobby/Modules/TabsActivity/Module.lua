--
-- 活动管理
--
local ActivityModule = class("ActivityModule", XH.ModuleBase)
local TaskDefine = require("app.Define.TaskDefine")
local LobbyConfig = require("lobby.Modules.Lobby.Config")
local TabsActivityConfig = require("lobby.Modules.TabsActivity.Config")

ActivityModule.ActivityType = {
    ActivityLuckyTurnTable = 1,
	ActivitySignIn = 2,
    ActivityShareHmq = 3,
    ActivityTask = 4,
    ActivitNewUserySignIn = 5,
    ActivityPhoneBind = 6,
    _KW_BTN_ITEM_OFF1 = 7,         --给公告栏预留红点位置
    _KW_BTN_ITEM_OFF2 = 8,
    _KW_BTN_ITEM_OFF3 = 9,
    _KW_BTN_ITEM_OFF4 = 10,
    _KW_BTN_ITEM_OFF5 = 11,
}

ActivityModule.showRed = {
    [ActivityModule.ActivityType.ActivityLuckyTurnTable] = false,
    [ActivityModule.ActivityType.ActivitySignIn] = false,
    [ActivityModule.ActivityType.ActivityShareHmq] = false,
    [ActivityModule.ActivityType.ActivityTask] = false,
    [ActivityModule.ActivityType.ActivitNewUserySignIn] = false,
    [ActivityModule.ActivityType.ActivityPhoneBind] = false,
    [ActivityModule.ActivityType._KW_BTN_ITEM_OFF1] = false,
    [ActivityModule.ActivityType._KW_BTN_ITEM_OFF2] = false,
    [ActivityModule.ActivityType._KW_BTN_ITEM_OFF3] = false,
    [ActivityModule.ActivityType._KW_BTN_ITEM_OFF4] = false,
    [ActivityModule.ActivityType._KW_BTN_ITEM_OFF5] = false,
}

ActivityModule.ActivityTaskID = {
   Task_SevenSign_ID = 41001,
   Task_BigWheel_ID = 41002,
   Task_Daily_Game_ID = 41003,
}

ActivityModule.PhoneBindServerId = {
    ["GetMsgOrBind"] = 1,
    ["GetPrize"] = 2,
}

ActivityModule.BindOrGetMsg = {
    ["Get"] = 0,
    ["Bind"] = 1,
}

ActivityModule.EVENT_SHARE_TO_SUCCESS = "EVENT_SHARE_TO_SUCCESS"
ActivityModule.EVENT_TEST_DISPATCH = "EVENT_TEST_DISPATCH"
ActivityModule.EVENT_FLUSH_TASK_LIST = "EVENT_FLUSH_TASK_LIST"
ActivityModule.EVENT_INIT_EVERY_SIGNIN = "EVENT_INIT_EVERY_SIGNIN"
ActivityModule.EVENT_EVERY_SIGNIN_LIST = "EVENT_EVERY_SIGNIN_LIST"
ActivityModule.EVENT_HUO_DONG_RED_POS = "EVENT_HUO_DONG_RED_POS"
-- 任务系统二期
ActivityModule.EVENT_WEB_TAST_LIST = "EVENT_WEB_TAST_LIST"

--金币破产礼包购买后返回
ActivityModule.EVENT_SHOW_BREAK_GIFT = "EVENT_SHOW_BREAK_GIFT"

--商城购买钻石自动兑换金币
ActivityModule.EVENT_EXCHANGE_DIAMOND_GOLD = "EVENT_EXCHANGE_DIAMOND_GOLD"

--删除互动
ActivityModule.EVENT_CLOSE_NEW_DAILY_LUCKY_DRAW = "EVENT_CLOSE_NEW_DAILY_LUCKY_DRAW"

--杭麻圈分享活动
ActivityModule.EVENT_HMQ_GET_SHARE_INFO = "EVENT_HMQ_GET_SHARE_INFO"
ActivityModule.EVENT_HMQ_GET_SHARE_AWARD = "EVENT_HMQ_GET_SHARE_AWARD"
ActivityModule.EVENT_SHARE_TO_HMQ_SUCCESS = "EVENT_SHARE_TO_HMQ_SUCCESS"

--关闭分发
ActivityModule.EVENT_CLOSE_TABS_ACTIVITY = "EVENT_CLOSE_TABS_ACTIVITY"

--手机绑定活动事件
ActivityModule.EVENT_PHONE_BIND_ACT_GETINFO = "EVENT_PHONE_BIND_ACT_GETINFO"
ActivityModule.EVENT_PHONE_BIND_ACT_GETPRIZE = "EVENT_PHONE_BIND_ACT_GETPRIZE"
ActivityModule.EVENT_PHONE_BIND_ACT_PRIZE_POOLS = "EVENT_PHONE_BIND_ACT_PRIZE_POOLS"
ActivityModule.EVENT_BTN_RED_POS = "EVENT_BTN_RED_POS"

--新手签到
ActivityModule.EVENT_NEW_USER_ACT_SIGNIN_LIST = "EVENT_NEW_USER_ACT_SIGNIN_LIST"
ActivityModule.EVENT_NEW_USER_ACT_SIGNIN_AWARD = "EVENT_NEW_USER_ACT_SIGNIN_AWARD"

ActivityModule.EVENT_CHANGE_TAB = "EVENT_CHANGE_TAB"
ActivityModule.EVENT_CLOSE_TAB_VIEW = "EVENT_CLOSE_TAB_VIEW"

--显示签到与红点
ActivityModule.EVENT_SHOW_SIGN_ICON = "EVENT_SHOW_SIGN_ICON"
ActivityModule.EVENT_SHOW_SIGN_REDPOINT = "EVENT_SHOW_SIGN_REDPOINT"

local ACTIVITY_TAB_REQ_PROTECTED_TIME = 300 --对于数据刷新要求不高的活动界面,发送task请求的间隔5分钟

function ActivityModule:ctor() 
    ActivityModule.super.ctor(self)
    self._needShowPLayAct = false
    self._needShowBackAct = false
    self._needShowPhoneFeeAct = false
    self._needShowLaborDayGift = false
    self._needShowFreeDrawAct = false
    -- 反诈奖励
    self._fanzhaActivityId = 0
    self._fanzhaAwardInfo = {}
end

function ActivityModule:getProxyEvents()
    return {
        { module = XH.netEngine, eventName = XH.MatchLinkProtocol.RespNotifyTaskPercent.event_key, callBack = "onRespNotifyTaskPercent" },
        { module = XH.login:getModule("Login"), eventKeyName = "LOGIN_EVENT_SUCCESS", callBack = "onLoginSuccessCallBack" },
    }
end

function ActivityModule:getReqConfig()
    return {
        ReqWebTaskProtocol = { reqPath = "app.Req.Task.ReqWebTaskProtocol", callBack = self.onReqWebTaskListCallBack },
        ReqMissionTagsNewState = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onGetMissionNewStateCallBack },
        ReqWebTaskState = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onGetMissionStateCallBack },
        ReqEverySignAwardList = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onReqEverySignTaskListCallBack },
        ReqEverySignInList = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onReqTaskSignInCallBack },    
        ReqShareInfoMsg = { reqPath = "lobby.Req.ShareActivityHmq.ReqGetShareInfo", callBack = self.onRespGetShareInfo },
        ReqShareAward = { reqPath = "lobby.Req.ShareActivityHmq.ReqGetShareAward", callBack = self.onRespGetShareAward },
        ReqPhoneBindActInfo = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onReqPhoneBindActInfoCallBack },
        ReqPhoneBindTaskConfig = { reqPath = "lobby.Req.TabsActivity.ReqPoolsByAid", callBack = self.onReqPhoneBindTaskConfigCallBack },    
        ReqNewUserActInfo = { reqPath = "app.Req.Task.ReqTaskPercent", callBack = self.onRespIsNewUserCallBack },
        ReqNewUserSignIn = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onRespNewUserSignInCallBack },
        ReqNewUserSignInExchange = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onRespNewUserSignInExchangeCallBack },
        -- 反诈奖励领取请求
        ReqPropsDiscount = { reqPath = "lobby.Req.GoldLaunch.ReqPropsDiscount", callBack = self.respPropsDiscount },
        ReqAcceptPresenter = { reqPath = "lobby.Req.GoldLaunch.ReqAcceptPresenter", callBack = self.respAcceptPresenter },
    }
end

function ActivityModule:onRespNotifyTaskPercent(event)
    local respData = XH.MatchLinkProtocol.RespNotifyTaskPercent:new()
    respData:bistream(event.msg.buff, event.msg.len)

    if respData.nTaskId == TaskDefine.TASK_ID.TASK_TYPE_QUICK_CHARGE_DIAMOND_GOLD then
        self:dispatchEvent( { name = self.EVENT_EXCHANGE_DIAMOND_GOLD, msg = respData.acData })
    end
end

function ActivityModule:getActivityTaskID()
    return self.ActivityTaskID
end

function ActivityModule:getUrlParam(aid)
    local lobbyID = XH.areaData:getLobbyID()
    local from = "pc"
    local appid = 0
    local secret = ""
    if TabsActivityConfig.LobbyActConfig[lobbyID] then
        appid = TabsActivityConfig.LobbyActConfig[lobbyID].appid or 0
        secret = TabsActivityConfig.LobbyActConfig[lobbyID].secret or ""
    end
    local timestamp = os.time()
    local numid = XH.playerData:getNumberID()
    local uid = un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID())
    local uuid = XH.areaData:getAreaID()..numid..timestamp..math.random(9999)
    local signatureStr = "app_id="..appid.."&numid="..numid.."&secret="..secret.."&timestamp="..timestamp.."&uuid="..uuid
    local signature = XH.SysTool:md5_encode(signatureStr)
    local param = "from=%s&app_id=%d&timestamp=%s&signature=%s&numid=%d&uid=%s&uuid=%s&aid=%d"
    param = string.format(param, from, appid, timestamp, signature, numid, uid, uuid, aid)
    return param
end

function ActivityModule:onLoginSuccessCallBack()
    --接入充值需要记录登录信息
    XH.sdkManager:xhCallOCFunc("XH_IAP", "LoginSuccess", "{}")

    local lobbyID = XH.areaData:getLobbyID()
    local todayLoginCount = XH.userDefault:getValue(XH.userDefault.KEY_ID.TODAY_LOGIN_COUNT, 0)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.TODAY_LOGIN_COUNT, todayLoginCount + 1)
    --浙江包登录
    if TabsActivityConfig.TaskRecordLogin and TabsActivityConfig.TaskRecordLogin[lobbyID] == 1 then
        local cellTaskPercent = require("app.Req.Task.ReqTaskPercent")
        local TaskPercent = cellTaskPercent:new()
        TaskPercent:addReqCallBack(self, self.onReqRecordLoginCallBack)
        local srsGroupID = XH.areaData:getSrsGroupID()
        TaskPercent:start(XH.playerData:getNumberID(), 0, 120, TaskDefine.TASK_ID.TASK_RECORD_STAY_LOGIN, srsGroupID, 10)
    end

    --新手签到活动
    self:reqNewUserSignInInfo()
end

function ActivityModule:onReqRecordLoginCallBack(cell, type, data)
    --if type == XH.Req.TYPE.SUCCESS then
        -- 浙江需要在判断了玩家上次登录时间再去验证是否为回归玩家
        --if XH.areaData:getLobbyID() == XH.LOBBY_ID.SHUKE then
            --XH.lobby:getModule("BackActivity"):reqBackActTaskProtocol(TaskDefine.TASK_ID.TASK_TYPE_BACK_ACT_LOGIN,"")
        --end
    --end
end
-------------------------------------------------------------------------

----------------------------------------- 任务系统 -----------------------------------------
--请求所有数据
function ActivityModule:reqWebTaskListData()
    self:startReq("ReqWebTaskProtocol", XH.areaData:getAreaWindID(), XH.SysTool:getChannelID(), 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function ActivityModule:onReqWebTaskListCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        self._allWebTasks = data
        self:reqMissionTagsNewState()
    end
end

-- 任务进度(TASK_TYPE_MISSION_SYS_MISSIONS_TAGS为新脚本，可以省掉其余脚本请求)
function ActivityModule:reqMissionTagsNewState()
    local dataString = ";wid = " .. XH.areaData:getAreaWindID() ..";channel = " .. XH.SysTool:getChannelID()
    local getLobbyID = XH.areaData:getLobbyID()
    if TabsActivityConfig.NewTaskSystem and TabsActivityConfig.NewTaskSystem[getLobbyID] == 1 then
        self:startReq("ReqMissionTagsNewState", TaskDefine.TASK_ID.TASK_TYPE_MISSION_SYS_MISSIONS_TAGS,0,0,XH.areaData:getSrsGroupID(),XH.KW_CONFIG_LOGIC_TIME_OUT,dataString)
    else
        self:startReq("ReqMissionTagsNewState", TaskDefine.TASK_ID.TASK_TYPE_MISSION_SYS_UPDATE_Q,0,0,XH.areaData:getSrsGroupID(),XH.KW_CONFIG_LOGIC_TIME_OUT,dataString)
    end
end

function ActivityModule:onGetMissionNewStateCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data.acData and data.acData ~= "" then
            local acData
            local ok = pcall(function()
                acData = cjson.decode(data.acData)
            end)
            if not ok then
                return
            end
            self:updateWebTasks(acData)
        end
    end
end

--修改为TASK来进行请求以及进行相关对应判断标签
function ActivityModule:updateWebTasks(data)
    local getLobbyID = XH.areaData:getLobbyID()
    if TabsActivityConfig.NewTaskSystem and TabsActivityConfig.NewTaskSystem[getLobbyID] == 1 then
        if data == 0 then
            data = {}
        end
        local needShowMission = {}
        for _, value in pairs(data) do
            for j = 1, #self._allWebTasks do
                if self._allWebTasks[j] and value.id == self._allWebTasks[j].id then
                    needShowMission[#needShowMission + 1] = self._allWebTasks[j]
                    needShowMission[#needShowMission].status = value.status
                    needShowMission[#needShowMission].finishcnt = value.finishcnt
                end
            end
        end
        self:dispatchEvent({name = self.EVENT_WEB_TAST_LIST, data = needShowMission})
    else
        local getByTagsId = data
        local getAllMissions = self._allWebTasks or {}
        local tmpMissions = {}
        for i = 1, #getAllMissions do
            for j = 1, #getByTagsId do
                if getAllMissions[i].id == getByTagsId[j] then
                    tmpMissions[#tmpMissions + 1] = getAllMissions[i]
                    break
                end
            end
        end
        self._realWebTasks = tmpMissions
        self:reqWebTaskState()
    end
end

function ActivityModule:reqWebTaskState()
    local dataString = ";wid = " .. XH.areaData:getAreaWindID() ..";channel = " .. XH.SysTool:getChannelID()
    self:startReq("ReqWebTaskState", TaskDefine.TASK_ID.TASK_TYPE_MISSION_SYS_STATE_NEW,0,0,XH.areaData:getSrsGroupID(),XH.KW_CONFIG_LOGIC_TIME_OUT,dataString)
end

function ActivityModule:onGetMissionStateCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        data = data.acData
        if not data or data == "0" or data == "" then
            XH.TipTool.showToast("任务状态请求失败！", 3)
            return
        end
        local backData
        local ok = pcall(function()
            backData = cjson.decode(data)
        end)
        if not ok then
            return
        end
        for j = #self._realWebTasks, 1, -1 do
            local bFind = false
            for _, v in pairs(backData) do
                if v.id == self._realWebTasks[j].id then
                    bFind = true
                    self._realWebTasks[j].status = v.status
                    self._realWebTasks[j].finishcnt = v.finishcnt
                end
            end
            if not bFind then
                table.remove(self._realWebTasks, j)
            end
        end
        self:dispatchEvent({name = self.EVENT_WEB_TAST_LIST, data = self._realWebTasks})
    end
end

------------------------EVERYSIGNIN
function ActivityModule:reqEverySignAwardList()
    local taskID = ActivityModule.ActivityTaskID.Task_SevenSign_ID
    self:startReq("ReqEverySignAwardList", taskID,TaskDefine.KW_SINGIN_REQ_AWARD_LIST,0,XH.areaData:getSrsGroupID(),XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function ActivityModule:onReqEverySignTaskListCallBack(cell, cellType, respProtocoldata)
    if cellType == XH.Req.TYPE.SUCCESS then
        local data = respProtocoldata.acData
        if data and data ~= "" then
            local jsonData
            local ok = pcall( function()
                jsonData = cjson.decode(data)
            end )
            if not ok then
                return
            end
            self:dispatchEvent({name = ActivityModule.EVENT_INIT_EVERY_SIGNIN, data = jsonData})
        end
    end
end

function ActivityModule:reqEverySignInList()
    local taskID = ActivityModule.ActivityTaskID.Task_SevenSign_ID
    self:startReq("ReqEverySignInList", taskID,TaskDefine.KW_SINGIN_REQ_SIGNIN,0,XH.areaData:getSrsGroupID(),XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function ActivityModule:onReqTaskSignInCallBack(cell, cellType, respProtocoldata)
    if cellType == XH.Req.TYPE.SUCCESS then
        local data = respProtocoldata.acData
        if data and data ~= "" then
            local jsonData
            local ok = pcall(function()
                jsonData = cjson.decode(data)
            end)
            if not ok then
                return
            end
            self:dispatchEvent({name = ActivityModule.EVENT_EVERY_SIGNIN_LIST, data = jsonData})
            local func = function()
                XH.viewManager:openView("GoldRewardView", nil, jsonData)
            end
            XH.SysTool.performDelayOnce(func, 0.6)
        end
    end
end

----------------TASKNODE

function ActivityModule:onShareToMomentByTask()
    if device.platform == "windows" then
        self:shareCallback()
        return
    end

    XH.lobby.getModule("Share"):share(self.shareActCallback,self)

    if XH.lobby:getModule("Lobby"):checkSendShareAddressMonitorPoint() then
        XH.lobby:getModule("Lobby"):setSendShareAddressMonitorPoint()
    end
end

--无论取消还是成功，都当成时分享成功处理
function ActivityModule:shareCallback(data)
    self:dispatchEvent({name = ActivityModule.EVENT_SHARE_TO_SUCCESS, data = data})
end

function ActivityModule:onDoTask()
    self:dispatchEvent({name = self.EVENT_CLOSE_TAB_VIEW})
end

-- ShareHmqNode
-- 获取分享信息
function ActivityModule:reqGetShareInfo()
    XH.TipTool.showLoading()
    self:startReq("ReqShareInfoMsg", XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function ActivityModule:onRespGetShareInfo(cell, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({name = ActivityModule.EVENT_HMQ_GET_SHARE_INFO, data = data})
    end
end

-- 获取分享奖励
function ActivityModule:reqGetShareAward(subid)
    XH.TipTool.showLoading()
    self:startReq("ReqShareAward", subid, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function ActivityModule:onRespGetShareAward(cell, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({name = ActivityModule.EVENT_HMQ_GET_SHARE_AWARD, data = data})
    end
end

-- 分享
function ActivityModule:onShareToMomentByHmq()
    self:shareActCallback()
    -- if device.platform == "windows" then
    --     self:shareActCallback()
    --     return
    -- end
    -- XH.lobby:getModule("Share"):share()
    -- if device.platform == "ios" and not XH.sdkManager:isCanShareToThird() then 
    --     self:shareActCallback()
    -- else
    --     local scheduleID
    --     scheduleID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
    --         if scheduleID ~= nil then
    --             cc.Director:getInstance():getScheduler():unscheduleScriptEntry(scheduleID)
    --         end
    --         self:shareActCallback()
    --     end, 1, false)
    -- end
end

-- 无论取消还是成功，都当成时分享成功处理
function ActivityModule:shareActCallback(data)
    self:dispatchEvent({name = ActivityModule.EVENT_SHARE_TO_HMQ_SUCCESS, data = data})
end

-- 对于所有分页场景进行监听关闭
function ActivityModule:closeScene()
    self:dispatchEvent({name = ActivityModule.EVENT_CLOSE_TABS_ACTIVITY})
end

-- 获取玩家信息
function ActivityModule:reqPhoneBindActPeoInfo()
    self._needShowPLayAct = false
    local dataString = ";nServerId = " .. XH.lobby:getModule("TabsActivity").PhoneBindServerId.GetMsgOrBind ..";nBindOrGet = " .. XH.lobby:getModule("TabsActivity").BindOrGetMsg.Get
    self:startReq("ReqPhoneBindActInfo", TaskDefine.TASK_ID.TASK_TYPE_ZHEJAING_PHONE_BIND_ACT, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
end

-- 请求绑定
function ActivityModule:reqPhoneBindActBindState()
    XH.TipTool.showLoading()
    local dataString = ";nServerId = " .. XH.lobby:getModule("TabsActivity").PhoneBindServerId.GetMsgOrBind ..";nBindOrGet = " .. XH.lobby:getModule("TabsActivity").BindOrGetMsg.Bind
    self:startReq("ReqPhoneBindActInfo", TaskDefine.TASK_ID.TASK_TYPE_ZHEJAING_PHONE_BIND_ACT, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
end

-- 请求奖励
function ActivityModule:reqPhoneBindActPrize(dataString)
    XH.TipTool.showLoading()
    self:startReq("ReqPhoneBindActInfo", TaskDefine.TASK_ID.TASK_TYPE_ZHEJAING_PHONE_BIND_ACT, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
end

-- 手机绑定活动
function ActivityModule:onReqPhoneBindActInfoCallBack(cell, cellType, data)
    XH.TipTool.hideLoading()
    if cellType == XH.Req.TYPE.SUCCESS then
        data = data.acData
        if data and data ~= "" then
            local jsonData
            local ok = pcall( function()
                jsonData = cjson.decode(data)
            end )
            if not ok then
                return
            end
            if jsonData.reqState == self.PhoneBindServerId.GetMsgOrBind then
                if XH.playerData:getPlayerPhone() ~= "" and jsonData.isBind == 0 then
                    self._needShowPLayAct = false
                else
                    self._needShowPLayAct = true
                end
                -- 是否为刚绑定的玩家
                if jsonData.isNewBindState then
                    XH.userDefault:setValue(XH.userDefault.KEY_ID.USERDEFAULT_ACTIVITY_TYPE, XH.lobby:getModule("TabsActivity").ActivityType.ActivityPhoneBind)
                    XH.viewManager:openView("TabsActivityView")
                end
                -- 分发小红点
                -- if jsonData.isBind == 1 and jsonData.isGetPrizes == 0 then
                --     self:showIconRedPoint("PHONEBIND", true)
                -- else
                --     self:showIconRedPoint("PHONEBIND", false)
                -- end
                self:dispatchEvent( { name = ActivityModule.EVENT_PHONE_BIND_ACT_GETINFO, data = jsonData })
            elseif jsonData.reqState == self.PhoneBindServerId.GetPrize then
                self:dispatchEvent( { name = ActivityModule.EVENT_PHONE_BIND_ACT_GETPRIZE, data = jsonData })
                if jsonData.info and jsonData.info.isBind == 1 and jsonData.info.isGetPrizes == 1 then
                    self:showIconRedPoint("PHONEBIND", false)
                end
            end
        end
    end
end

function ActivityModule:reqNewUserSignInInfo()
    --主动检查玩家是否有未兑换的时效道具的记录
    self:doNewUserSignInExchange()
    --检查玩家是否是新玩家或者已经完成任务
    if  self:canSendNewUserActReq() then
            self:startReq("ReqNewUserActInfo", XH.playerData:getNumberID(), TaskDefine.APPID, TaskDefine.PROCESSID, 
                TaskDefine.TASK_ID.TASK_NEW_USER_SIGN_IN_ACT, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT)
    else
        if self._newUserActControl and self._newUserActControl.actInfo then
            self:dispatchEvent({name = ActivityModule.EVENT_NEW_USER_ACT_SIGNIN_LIST,data = self._newUserActControl.actInfo})
        end
    end
end

function ActivityModule:canSendNewUserActReq()
    local lobbyID = XH.areaData:getLobbyID()
    if TabsActivityConfig.NewUserSignInActConfig and TabsActivityConfig.NewUserSignInActConfig[lobbyID] == 1 and XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_HAVE_PERMISSION, true) then
        if not self._newUserActControl then
            return true
        end
        if  self._userNumberID ~= XH.playerData:getNumberID() then
            self._newUserActControl = nil
            return true
        end
        if  self._newUserActControl.reqTime then
            if os.time() - self._newUserActControl.reqTime > ACTIVITY_TAB_REQ_PROTECTED_TIME then
                self._newUserActControl = nil
                return true
            end
        end
    end
    return false
end

function ActivityModule:onRespIsNewUserCallBack(cell,ntype,data)
    XH.playerData:setIsDisplayNewUser(false)
    if data and data.acData then
        local actInfo = json.decode(data.acData)
        if next(actInfo) then
            self._actInfo = actInfo
            XH.playerData:setIsDisplayNewUser(actInfo.isActive or false)
            if not actInfo.isActive then
                self:showIconRedPoint("DAILYSIGN", false)
            end
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_HAVE_PERMISSION, actInfo.isActive or false)
            if actInfo.isActive then
                --新手活动,每日需要主动弹出一次
                self._newUserActControl = {}
                self._userNumberID = XH.playerData:getNumberID()
                self._newUserActControl.reqTime = os.time()
                self._newUserActControl.actInfo = actInfo
                if not XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_SHOW_TODAY, false) and not (XH.lobby:getModule("BackActivity"):isShowLayer()) then
                    -- XH.viewManager:openView("TabsActivityView")
                    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_SHOW_TODAY, true)
                end
                XH.lobby:getModule("BackActivity"):needShowSign()
                self:dispatchEvent({name = ActivityModule.EVENT_NEW_USER_ACT_SIGNIN_LIST,data = self._newUserActControl.actInfo})
            end
        end
    end
end

-- 手机绑定活动请求奖池
function ActivityModule:reqPhongBindActTaskConfig(aid)
    XH.TipTool.showLoading()
    self:startReq("ReqPhoneBindTaskConfig", aid, nil, nil, 10)
end

-- 手机绑定活动奖池
function ActivityModule:onReqPhoneBindTaskConfigCallBack(cell, type, responce)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        if responce and responce.list then
            self:dispatchEvent( { name = ActivityModule.EVENT_PHONE_BIND_ACT_PRIZE_POOLS, data = responce.list })
	    end
    end
end

function ActivityModule:getPhoneActNeedShow()
    return self._needShowPLayAct
end

function ActivityModule:reqNewUserSignInAward(pid)
    XH.TipTool.showLoading()
    local specialStr = ";nTaskSubId=1000;nPoolID=" .. pid or 0
    self:startReq("ReqNewUserSignIn", TaskDefine.TASK_ID.TASK_NEW_USER_SIGN_IN_ACT, TaskDefine.PROTOID, 
        TaskDefine.APPID, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, specialStr)
end

function ActivityModule:onRespNewUserSignInCallBack(cell,ntype,data)
    XH.TipTool.hideLoading()
    if data and data.acData and data.acData ~= "" then
        local actInfo = json.decode(data.acData)
        if actInfo and actInfo.award then
            --刷新数据,减少访问的数量
            if self._newUserActControl and self._newUserActControl.actInfo then
                self._newUserActControl.actInfo.awardTimes = self._newUserActControl.actInfo.awardTimes + 1
                self._newUserActControl.actInfo.isSigned = true
            end
            -- 基于Task业务的局限性,需要客户端延迟调用
            if actInfo.award.is_time_prop then
                self:addNeedExchangeProp(actInfo.award.propid)
                self:doNewUserSignInExchange()
            end
            self:dispatchEvent({name = ActivityModule.EVENT_NEW_USER_ACT_SIGNIN_AWARD,data = actInfo.award})
        end
    end
end

function ActivityModule:reqNewUserSignInExchange(pid)
    local specialStr = ";nTaskSubId=1001;nPoolID=" .. pid or 0
    self:startReq("ReqNewUserSignInExchange", TaskDefine.TASK_ID.TASK_NEW_USER_SIGN_IN_ACT, TaskDefine.PROTOID, 
        TaskDefine.APPID, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, specialStr)
end

function ActivityModule:onRespNewUserSignInExchangeCallBack(cell,ntype,data)
    if data and data.acData and data.acData ~= "" then
        local actInfo = json.decode(data.acData)
        if actInfo.flag then
            if actInfo.exchangeId then
                self:delNeedExchangeProp(actInfo.exchangeId)
            end
        end
    end
end

function ActivityModule:doNewUserSignInExchange()
    local tableStr = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_TIME_PROP_DEAL,"")
    if tableStr ~= "" then
        local props = json.decode(tableStr)
        if next(props) then
            for i= 1,#props do
                if props[i].tryTimes > 0 then
                    props[i].tryTimes = props[i].tryTimes - 1
                    local propid = clone(props[i].propid)
                    XH.SysTool.performWithDelayGlobal(function ()
                        self:reqNewUserSignInExchange(propid)
                    end,2)
                end
            end
            self:updateNeedExchangeProp(props)
        end
    end
end

function ActivityModule:addNeedExchangeProp(propid)
    local props = {}
    local tableStr = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_TIME_PROP_DEAL,"")
    if tableStr ~= "" then
        props = json.decode(tableStr)
    end
    if next(props) then
        for i= 1,#props do
            if props[i].propid == propid then
                return
            end
        end
    end
    local prop = {}
    prop.propid = propid
    prop.tryTimes = 3 --默认尝试3次
    table.insert(props,prop)
    local msg = json.encode(props)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_TIME_PROP_DEAL,msg)
end

function ActivityModule:updateNeedExchangeProp(props)
    if props and next(props) then
        for i = #props,1,-1 do
            if props[i].tryTimes <= 0 then
                table.remove(props,i)
            end
        end
        local msg = json.encode(props)
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_TIME_PROP_DEAL,msg)
    end
end

function ActivityModule:delNeedExchangeProp(propid)
    local props = {}
    local tableStr = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_TIME_PROP_DEAL,"")
    if tableStr ~= "" then
        props = json.decode(tableStr)
    end
    if next(props) then
        for i = #props,1,-1 do
            if props[i].propid == propid then
                table.remove(props,i)
            end
        end
        local msg = json.encode(props)
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_TIME_PROP_DEAL,msg)
    end
end

function ActivityModule:onShowSginRedPoint(isShow)
    --if event.data then
        local bNewUser = XH.playerData:getIsDisplayNewUser()
        local lobbyID = XH.areaData:getLobbyID()
        if bNewUser and TabsActivityConfig.NewUserSignInActConfig and TabsActivityConfig.NewUserSignInActConfig[lobbyID] == 1 and XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_HAVE_PERMISSION, true) then
            bNewUser = true
        else
            bNewUser = false
        end
        if not bNewUser then
            self:showIconRedPoint("DAILYSIGN", isShow)
        end
        self:dispatchEvent( { name = ActivityModule.EVENT_SHOW_SIGN_ICON, data = {iconID = LobbyConfig.ICON_ID.SIGN_IN, bShow = not bNewUser} })
        self:dispatchEvent( { name = ActivityModule.EVENT_SHOW_SIGN_REDPOINT, data = {iconID = LobbyConfig.ICON_ID.SIGN_IN, bShow = isShow} })
    --end
end

function ActivityModule:showIconRedPoint(activityType, showRed)
    local data = {}
    data.showRed = showRed
    data.activity = activityType
    self:dispatchEvent( { name = ActivityModule.EVENT_BTN_RED_POS, data = data })
    ActivityModule.showRed[activityType] = showRed
    for _, isShow in pairs(ActivityModule.showRed) do
        if isShow then
            self:dispatchEvent( { name = ActivityModule.EVENT_HUO_DONG_RED_POS, data = true })
            return 
        end
    end
    self:dispatchEvent( { name = ActivityModule.EVENT_HUO_DONG_RED_POS, data = false })
end

function ActivityModule:getHuoDongRedShow()
    return ActivityModule.showRed
end

function ActivityModule:getActsByLobbyID()
    --TODO 从web获取当前区的活动信息
    local webData = XH.lobby:getModule("Lobby"):getWebTabsData()
    local tmpTabs = webData.page or {}
    table.sort(tmpTabs, function(a, b)
        return a.sort < b.sort
    end)
    if device.platform == "ios" then
        for i = #tmpTabs, 1, -1 do
            if tmpTabs[i].icon_code and tmpTabs[i].icon_code == "TOPONPHONEFEE" then
                table.remove(tmpTabs, i)
            end
        end
    end
    return tmpTabs
end

function ActivityModule:getActsViewPathByKey(key)
    if not key then
        return
    end
    return TabsActivityConfig.actsPath[key]
end

function ActivityModule:setShowBackAct(data)
    self._needShowBackAct = data
end

function ActivityModule:getShowBackAct()
    return self._needShowBackAct
end

function ActivityModule:setShowPhoneFeeAct(data)
    self._needShowPhoneFeeAct = data
end

function ActivityModule:getShowPhoneFeeAct()
    return self._needShowPhoneFeeAct
end

function ActivityModule:setShowFreeDrawAct(data)
    self._needShowFreeDrawAct = data
end

function ActivityModule:getShowFreeDrawAct()
    return self._needShowFreeDrawAct
end

function ActivityModule:setShowLaborDayGift(data)
    self._needShowLaborDayGift = data
end

function ActivityModule:getShowLaborDayGift()
    return self._needShowLaborDayGift
end

function ActivityModule:changeTabTo(tab)
    self:dispatchEvent( { name = ActivityModule.EVENT_CHANGE_TAB, data = tab })
end

-- 反诈奖励领取
-- 先请求ReqPropsDiscount接口，根据服务约定字段tags，返回是否有奖励，如果有奖励，
-- 则根据返回的activity_id字段，再请求ReqAcceptPresenter接口领取奖励
function ActivityModule:reqFanZhaGiftInfo()
    self:startReq("ReqPropsDiscount", {"fanzha_free"})
end

function ActivityModule:respPropsDiscount(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local firstKey, firstValue = next(data.reply.activitys)
        if firstKey == nil then
            return
        end
        if firstValue and firstValue.discounts and firstValue.discounts[1] then
            self._fanzhaAwardInfo = firstValue.discounts
        end
        self._fanzhaActivityId = tonumber(firstKey)
    end
end

function ActivityModule:getCanGetFanZhaAward()
    return #self._fanzhaAwardInfo > 0
end

function ActivityModule:reqFanZhaGift()
    self:startReq("ReqAcceptPresenter", self._fanzhaActivityId)
end

function ActivityModule:respAcceptPresenter(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        -- 领奖成功
        XH.playerData:flushPlayerDrop()
        if #self._fanzhaAwardInfo <= 0 then
            return
        end
        local awardInfo = {}
        awardInfo.prize_names = {}
        awardInfo.image_urls = {}
        for k,v in pairs(self._fanzhaAwardInfo) do
            local propId = v.propId
            local propCount = v.count
            -- 如果发奖的道具ID是小房卡或者是绑定房卡，需要除以对应的比例
            if propId == XH.areaData:getPropBindRoomCardID() then
                propCount = propCount/XH.areaData:getPropFreeRoomCardRatio()
            elseif propId == XH.areaData:getPropSmallRoomCardID() then
                propCount = propCount/XH.areaData:getPropSmallRoomCardRatio()
            end
            awardInfo.prize_names[#awardInfo.prize_names + 1] = "x" .. propCount
            local img_url = "http://palmastatic.wenzhoushuke.com/materials/images/20240605/10364986837.png"
            if propId == XH.areaData:getPropDiamndID() then
                img_url = "http://palmastatic.wenzhoushuke.com//materials/images/20230607/11463438206.png"
            end
            awardInfo.image_urls[#awardInfo.image_urls + 1] = img_url
            awardInfo.text = " "
        end
        XH.viewManager:openView("BackActivityAwardView", nil, nil, awardInfo)
        self._fanzhaAwardInfo = {}
    end
end

return ActivityModule
d�  