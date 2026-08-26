local LuckyTaskModule = class("LuckyTaskModule", XH.ModuleBase)
local LuckyTaskDefine = require("lobby.Modules.LuckyTask.Define")
local TaskDefine = require("app.Define.TaskDefine")
local LobbyConfig = require("lobby.Modules.Lobby.Config")
local YGiftBankruptcyDefine = require("lobby.Modules.YGiftBankruptcy.Define")
local ConfigurationDefine = require("lobby.Modules.Configuration.Define")

LuckyTaskModule.EVENT_NotifyLuckyTask = "EVENT_NotifyLuckyTask"
LuckyTaskModule.EVENT_NUIAddLobbyIcon = "EVENT_NUIAddLobbyIcon"
LuckyTaskModule.EVENT_NotifyPageStatus = "EVENT_NotifyPageStatus"
LuckyTaskModule.EVENT_NotifyLuckyClose = "EVENT_NotifyLuckyClose"
LuckyTaskModule.EVENT_NotifyLuckyTaskJoin = "EVENT_NotifyLuckyTaskJoin"
LuckyTaskModule.EVENT_NotifyLuckyTaskPage = "EVENT_NotifyLuckyTaskPage"
LuckyTaskModule.EVENT_NotifyLuckyTaskSYSTEMTIME = "EVENT_NotifyLuckyTaskSYSTEMTIME"
LuckyTaskModule.EVENT_NotifyLuckyTaskAwardRecord = "EVENT_NotifyLuckyTaskAwardRecord"
LuckyTaskModule.EVENT_NUIUpdateLobbyIconRedPoint = "EVENT_NUIUpdateLobbyIconRedPoint"
LuckyTaskModule.EVENT_NUIUpdateLobbyIconBubbleTip = "EVENT_NUIUpdateLobbyIconBubbleTip"
LuckyTaskModule.EVENT_NUIUpdateLobbyIconAnimation = "EVENT_NUIUpdateLobbyIconAnimation"

function LuckyTaskModule:getReqConfig()
    return {
        --
        ReqLuckTaskGetContent = {reqPath = "lobby.Req.LuckTask.ReqLuckTaskGetContent", callBack = self.respGetContent},
        ReqLuckTaskGetDrawLogs = {reqPath = "lobby.Req.LuckTask.ReqLuckTaskGetDrawLogs", callBack = self.respGetDrawLogs},
        ReqLuckTaskGetPage = {reqPath = "lobby.Req.LuckTask.ReqLuckTaskGetPage", callBack = self.respGetPage},
        ReqLuckTaskGetReward = {reqPath = "lobby.Req.LuckTask.ReqLuckTaskGetReward", callBack = self.respGetReward},
        ReqParticipate = {reqPath = "lobby.Req.LuckTask.ReqParticipate", callBack = self.respParticipate},
        ReqSystemTimes = {reqPath = "app.Req.Task.ReqTaskPercent", callBack = self.onReqTaskPercentCallBack},
        ReqActList = {reqPath = "lobby.Req.LuckTask.ReqActList", callBack = self.respActList}
    }
end

-- 获取基础任务
function LuckyTaskModule:reqActList()
    self:startReq("ReqActList", 10)
end

function LuckyTaskModule:respActList(req, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        XH.lobby:getModule("ToponAct"):setSignActId()
        XH.lobby:getModule("AoYun"):setActivityInfo()
        for _, info in pairs(data.act) do
            if info.type == "renwutuisong" then
                -- self:reqTaskList()
            elseif info.type == YGiftBankruptcyDefine.ACTLIST.Bankruptcy and self:isInActTime(info) then
                XH.lobby:getModule("Shop"):reqSystemTimes()
                XH.lobby:getModule("YGiftBankruptcy"):setActivityId(info.aid)
            elseif info.type == YGiftBankruptcyDefine.ACTLIST.Defeat and self:isInActTime(info) then
                XH.lobby:getModule("YGiftDefeat"):setActivityId(info.aid)
            elseif info.type == YGiftBankruptcyDefine.ACTLIST.Enter and self:isInActTime(info) then
                XH.lobby:getModule("YGiftEnter"):setActivityId(info.aid)
            elseif info.type == "sign_activity" then
                XH.lobby:getModule("ToponAct"):setSignActId(info.aid)
                XH.lobby:getModule("ToponAct"):reqSignDetail()
            elseif info.type == "aoyun_act" then
                XH.lobby:getModule("AoYun"):setActivityInfo(info)
            end
        end
    else
        print('ReqActList TIMEOUT')
    end
end

-- 判断活动是否在线
function LuckyTaskModule:isInActTime(info)
    if not info then
        return false
    end
    local time = XH.lobby:getModule("Shop"):getServerTime()
    if time < tonumber(info["end"]) and time > tonumber(info.start) and time > tonumber(info.online) and time < tonumber(info.offline) then
        return true
    end
    return false
end

function LuckyTaskModule:RequestSysTime()
    self:startReq("ReqSystemTimes", XH.playerData:getNumberID(), 0, 120, TaskDefine.KW_SYSTEM_TIME, XH.areaData:getSrsGroupID(), 10)
end

function LuckyTaskModule:onReqTaskPercentCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local timestamp = json.decode(data.acData).time
        if timestamp ~= nil then
            self:dispatchEvent({name = LuckyTaskModule.EVENT_NotifyLuckyTaskSYSTEMTIME, data = timestamp})
        end
    end
end

function LuckyTaskModule:getCurTask()
    return self._curTask
end

function LuckyTaskModule:setCurTask(task)
    self._curTask = task
    self:dispatchEvent({name = LuckyTaskModule.EVENT_NotifyLuckyTask, data = self._curTask})
end

function LuckyTaskModule:getDefaultTaskInfo()
    return self._defaultTaskInfo
end

function LuckyTaskModule:clearLuckyTask()
    self._luckyTasksMap = {}
    self:reqTaskList()
end

function LuckyTaskModule:reqDefabltContent()
    self:reqGetContent(self._defaultTaskInfo, true)
end

function LuckyTaskModule:getSameStatusInfo(status)
    if self._luckyTaskPage == nil then
        return
    end
    for i = 1, #self._luckyTaskPage.info do
        local tempInfo = self._luckyTaskPage.info[i]
        if LuckyTaskDefine.ContentStatus[tempInfo.status] == status then
            return {same = true, info = tempInfo}
        end
    end
    return {same = false, info = self._luckyTaskPage.info[1]}
end

function LuckyTaskModule:showLobbyTip()
    local tip = ''
    local redPoint = false
    local showTip = false
    local haveAward = self:getSameStatusInfo(LuckyTaskDefine.ContentStatus.AC_CAN_DRAW)
    local haveNew = self:getSameStatusInfo(LuckyTaskDefine.ContentStatus.AC_NEW)

    if haveAward.same then
        tip = '可以领奖了!'
        redPoint = true
        showTip = true
        self._defaultTaskInfo = haveAward.info
    elseif haveNew.same then
        -- tip = '有新任务了!'
        -- showTip = true
        self._defaultTaskInfo = haveNew.info
    else
        self._defaultTaskInfo = self._luckyTaskPage.info[1]
    end

    self:dispatchEvent({name = LuckyTaskModule.EVENT_NUIAddLobbyIcon, data = {icon = LobbyConfig.ICON_ID.LUCKY_TASK, isShow = true}})
    self:dispatchEvent({name = LuckyTaskModule.EVENT_NUIUpdateLobbyIconBubbleTip, data = {tip = tip, bShow = showTip}})
    self:flushTeaIconState(true, showTip, tip)
end

function LuckyTaskModule:getTabIndex()
    if self._luckyTaskPage == nil or self._defaultTaskInfo == nil then
        return 1
    end
    for i = 1, #self._luckyTaskPage.info do
        if self._defaultTaskInfo.id[1] == self._luckyTaskPage.info[i].id[1] then
            return i
        end
    end
    return 1
end

function LuckyTaskModule:onLuckyTaskClose()
    self:dispatchEvent({name = LuckyTaskModule.EVENT_NotifyLuckyClose, data = 1})
    self:dispatchEvent({name = LuckyTaskModule.EVENT_NUIAddLobbyIcon, data = {icon = LobbyConfig.ICON_ID.LUCKY_TASK, isShow = false}})
    self:flushTeaIconState(false)
end

-- 获取任务id
function LuckyTaskModule:reqTaskList()
    self:startReq("ReqLuckTaskGetPage", 10)
end

function LuckyTaskModule:respGetPage(req, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data.pages == nil then
            return
        end
        if #data.pages > 0 and #data.pages[1].info > 0 then
            -- 进入默认请求第一个页签配置内容
            self._luckyTaskPage = data.pages[1]
            self:showLobbyTip()
            self:checkShowLuckyView()
            self:dispatchEvent({name = LuckyTaskModule.EVENT_NotifyLuckyTaskPage, data = self._luckyTaskPage})
        else
            self._luckyTaskPage = data.pages[1]
            self:onLuckyTaskClose()
        end
    elseif type == XH.Req.TYPE.TIMEOUT then
        print('CellGetPageRequest TIMEOUT')
    else
        self:onLuckyTaskClose()
    end
end

function LuckyTaskModule:checkShowLuckyView()
    if not self:isOpenLuckyView() then
        return
    end
    XH.viewManager:openView("LuckyTaskView")
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_LUCKY_TASK_INFO_ID, 1)
end

function LuckyTaskModule:isOpenLuckyView()
    do return false end
    local count = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_LUCKY_TASK_INFO_ID, 0)
    if count > 0 then
        return false
    end
    return true
end

function LuckyTaskModule:checkLuckyTask(activity_id)
    if self._luckyTasksMap == nil then
        return false
    end
    local taskContent = self._luckyTasksMap[activity_id]
    if taskContent ~= nil then
        self:setCurTask(taskContent)
        return true
    end
    return false
end

-- 获取任务内容
function LuckyTaskModule:reqGetContent(pages, isCheck)
    if isCheck == true and self:checkLuckyTask(pages.id[1]) then
        return
    end
    local include = 0 -- 不用修改
    local activity_id = pages.id
    self:startReq("ReqLuckTaskGetContent", activity_id, include, 10)
end

function LuckyTaskModule:respGetContent(req, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data.box == nil then
            return
        end
        if self._luckyTasksMap == nil then
            self._luckyTasksMap = {}
        end
        self._luckyTasksMap[data.box[1].activityId] = data.box[1]
        self:setCurTask(data.box[1])
    end
end

-- 历史记录
function LuckyTaskModule:reqGetDrawLogs()
    XH.TipTool.showLoading()
    self:startReq("ReqLuckTaskGetDrawLogs", 10)
end

function LuckyTaskModule:respGetDrawLogs(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({name = LuckyTaskModule.EVENT_NotifyLuckyTaskAwardRecord, data = data.logs})
    end
end

function LuckyTaskModule:setShareTask(shareTask)
    self._shareTask = shareTask
end

-- 请求分享
function LuckyTaskModule:shareSuccess()
    if self._shareTask == nil then
        return
    end
    self:reqParticipate(self._shareTask.activityId)
end

function LuckyTaskModule:reqParticipate(activity_id)
    if self._lastTaskInfo == nil then
        self._lastTaskInfo = {}
    end
    self._lastTaskInfo.activityId = activity_id
    self:startReq('ReqParticipate', activity_id, 10)
end

function LuckyTaskModule:onRespParticipate(req, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        self:reqTaskList()
    end
end

-- 领奖
function LuckyTaskModule:reqGetRewards(activity_id, content_id)
    XH.TipTool.showLoading()
    self._lastTaskInfo = {activityId = activity_id, contentId = content_id}
    self:startReq("ReqLuckTaskGetReward", activity_id, content_id, 10)
end

function LuckyTaskModule:updateAwardTask()
    if self._luckyTasksMap == nil then
        return
    end
    local temp = self._luckyTasksMap[self._lastTaskInfo.activityId]
    if temp == nil then
        return
    end

    for i = 1, #temp.content do
        if temp.content[i].id == self._lastTaskInfo.contentId then
            temp.content[i].draw = 1
        end
    end
    self:setCurTask(temp)
end

-- 领奖
function LuckyTaskModule:showAwardView(data)
    local awardInfo = {}
    awardInfo.prize_names = {}
    awardInfo.image_urls = {}
    for _, award in pairs(data.rewards) do
        awardInfo.prize_names[#awardInfo.prize_names + 1] = "x" .. award.count
        awardInfo.image_urls[#awardInfo.image_urls + 1] = award.icon or ""
    end
    awardInfo.text = " "
    if #awardInfo.prize_names == 0 then
        return
    end
    XH.viewManager:openView("BackActivityAwardView", nil, nil, awardInfo)
end

function LuckyTaskModule:updateTaskList(activity_id, status)
    if self._luckyTaskPage == nil then
        return
    end
    for i = 1, #self._luckyTaskPage.info do
        if self._luckyTaskPage.info[i].id[1] == activity_id then
            self._luckyTaskPage.info[i].status = status
        end
    end
    self:dispatchEvent({name = LuckyTaskModule.EVENT_NotifyPageStatus, data = {activity_id = activity_id, status = status}})
    self:showLobbyTip()
end

function LuckyTaskModule:respGetReward(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self:showAwardView(data)
        self:updateAwardTask()
        self:updateTaskList(data.activityId, data.pageStatus)
        XH.playerData:flushPlayerDrop()
    end
end

function LuckyTaskModule:respParticipate(req, type, data)
    if type == XH.Req.TYPE.SUCCESS and data ~= nil and data.rewards ~= nil then
        self:showAwardView(data)
    end
end

function LuckyTaskModule:setTeaHouseMain(scene)
    self._scene = scene
end

-- 刷新比赛场数据
function LuckyTaskModule:flushTeaIconState(isShow, bShowTip, sTip)
    if self._scene == nil or self._scene._panelActListBg == nil then
        self._scene = nil
        return
    end
    if isShow then
        self._scene._panelActListBg:setVisible(true)
        self._scene._panelActList:setVisible(true)
        self._scene:showTeaHouseArrowRightAni(self._scene._actListAniNode, true)
        self._scene:addActivityIconToList(LobbyConfig.ICON_ID.LUCKY_TASK, self._scene.showLuckyTask)
    else
        self._scene:removeActivityIconToList(LobbyConfig.ICON_ID.LUCKY_TASK)
        self._scene._removeLuckTask = true
    end
    if self._scene._panelActList ~= nil and self._scene._panelActList:getChildrenCount() > 0 then
        for i = 1, self._scene._panelActList:getChildrenCount() do
            local node = self._scene._panelActList:getChildren()[i]
            if node:getTag() == LobbyConfig.ICON_ID.LUCKY_TASK and node:getChildrenCount() > 0 then
                self:flushAwardState(node:getChildren()[1], bShowTip)
            end
        end
    end
end

-- 去完成对应操作
function LuckyTaskModule:notifyLuckyTaskJoin(type, gameid)
    self:dispatchEvent({name = LuckyTaskModule.EVENT_NotifyLuckyTaskJoin, data = {type = type, gameid = gameid}})
end

-- 刷新icon领奖状态
function LuckyTaskModule:flushAwardState(node, bShow)
    if node == nil then
        return
    end
    if node:getChildrenCount() == 0 then
        if bShow then
            -- 增加节点
            cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/hall/Image/Activity/LuckTask/lucky_task.plist")
            local iconResName = "lucky_task_icon_2.png"
            local item = ccui.ImageView:create()
            item:loadTexture(iconResName, ccui.TextureResType.plistType)
            item:setVisible(true)
            item:setPosition(node:getContentSize().width / 2, node:getContentSize().height - 10)
            node:addChild(item)
        else
            return
        end
    elseif node:getChildrenCount() > 0 then
        node:getChildren()[1]:setVisible(bShow)
    end
end

-- 是否存在该活动
function LuckyTaskModule:isExistActId()
    local allJsonData = XH.lobby:getModule("Configuration"):getConfigJsonData(ConfigurationDefine.Config.ALL, "AllConfig")
    local lobbyID = XH.areaData:getLobbyID()
    local actId
    if allJsonData and allJsonData.DoubleCardAct then
        for id,data in pairs(allJsonData.DoubleCardAct) do
            if lobbyID == tonumber(id) then
                actId = data
                break
            end
        end
    end
    if not actId then
        return false
    end
    if not self._luckyTaskPage then
        return false
    end
    for i = 1, #self._luckyTaskPage.info do
        if self._luckyTaskPage.info[i].id and #self._luckyTaskPage.info[i].id > 0 then
            for j = 1, #self._luckyTaskPage.info[i].id do
                for k = 1, #actId do
                    if actId[k] == tonumber(self._luckyTaskPage.info[i].id[j]) then
                        return true
                    end
                end
            end
        end
    end
    return false
end

return LuckyTaskModule
 �>  