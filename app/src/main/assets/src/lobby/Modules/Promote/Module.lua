local PromoteModule = class("PromoteModule", XH.ModuleBase)
local PromoteConfig = require("lobby.Modules.Promote.Config")
local LobbyConfig = require("lobby.Modules.Lobby.Config")
local PromoteDefine = require("lobby.Modules.Promote.Define")

PromoteModule.EVENT_FULSH_PROMOTE_DAY_ICON = "EVENT_FULSH_PROMOTE_DAY_ICON"
PromoteModule.EVENT_FULSH_PROMOTE_TASK = "EVENT_FULSH_PROMOTE_TASK"
PromoteModule.EVENT_FULSH_PROMOTE_LOG = "EVENT_FULSH_PROMOTE_LOG"
PromoteModule.EVENT_FULSH_PROMOTE_INVITE_LIST = "EVENT_FULSH_PROMOTE_INVITE_LIST"

function PromoteModule:getReqConfig()
    return {
        --
        ReqSpreadAward = {reqPath = "lobby.Req.Promote.ReqSpreadAward", callBack = self.respSpreadAward},
        ReqSpreadAwardList = {reqPath = "lobby.Req.Promote.ReqSpreadAwardList", callBack = self.respSpreadAwardList},
        ReqSpreadDraw = {reqPath = "lobby.Req.Promote.ReqSpreadDraw", callBack = self.respSpreadDraw},
        ReqSpreadInfo = {reqPath = "lobby.Req.Promote.ReqSpreadInfo", callBack = self.respSpreadInfo},
        ReqSpreadInviteClick = {reqPath = "lobby.Req.Promote.ReqSpreadInviteClick", callBack = self.respSpreadInviteClick},
        ReqSpreadInviteList = {reqPath = "lobby.Req.Promote.ReqSpreadInviteList", callBack = self.respSpreadInviteList},
        ReqSpreadTask = {reqPath = "lobby.Req.Promote.ReqSpreadTask", callBack = self.respSpreadTask}
    }
end

-- 活动是否开启
function PromoteModule:isOpenAct()
    if PromoteConfig[XH.areaData:getLobbyID()] == nil then
        return false
    end
    if self:getBaseInfo() == nil then
        return false
    end
    local isWhitePlayer = self:getBaseInfo().owner_join_act == 1 or self:getBaseInfo().player_join_act == 1
    if not isWhitePlayer then
        return false
    end
    local nowTime = os.time()
    if nowTime < self:getBaseInfo().start or nowTime > (self:getBaseInfo()["end"] + 86399) then
        return false
    end
    return true
end

-- 是否活动未下架
function PromoteModule:isOnlineAct()
    if PromoteConfig[XH.areaData:getLobbyID()] == nil then
        return false
    end
    if self:getBaseInfo() == nil then
        return false
    end
    local isWhitePlayer = self:getBaseInfo().owner_join_act == 1 or self:getBaseInfo().player_join_act == 1
    if not isWhitePlayer then
        return false
    end
    local nowTime = os.time()
    if nowTime < self:getBaseInfo().online_at or nowTime > (self:getBaseInfo().offline_at + 86399) then
        return false
    end
    return true
end

-- 领队完成奖励
function PromoteModule:reqSpreadAward(teaNumber, date, award_type)
    self:startReq("ReqSpreadAward", teaNumber, date, award_type, 10)
end

function PromoteModule:respSpreadAward(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        if #data.dataList > 0 then
            -- 通过propid汇总
            local awardSum = {}
            for _, award in pairs(data.dataList) do
                if awardSum[award.prop_id] ~= nil then
                    awardSum[award.prop_id].count = awardSum[award.prop_id].count + award.count
                else
                    awardSum[award.prop_id] = {}
                    awardSum[award.prop_id].count = award.count
                    awardSum[award.prop_id].image_url = award.image_url or ""
                end
            end
            -- 再区分数据
            local awardInfo = {}
            awardInfo.prize_names = {}
            awardInfo.image_urls = {}
            for _, award in pairs(awardSum) do
                awardInfo.prize_names[#awardInfo.prize_names + 1] = "x" .. award.count
                awardInfo.image_urls[#awardInfo.image_urls + 1] = award.image_url or ""
            end
            awardInfo.isPromote = true
            XH.viewManager:openView("BackActivityAwardView", nil, nil, awardInfo)
            self:reqSpreadTaskReal()
            XH.playerData:flushPlayerDrop()
            self:reqSpreadAwardList(nil, PromoteDefine.DRAW_TYPE.PERIOD)
        end
    end
end

function PromoteModule:getPendTeahouseInfo(nTeaNumber)
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    if teahouseData ~= nil then
        local teahouseInfo = teahouseData:getTeaHouseInfoByID(nTeaNumber)
        if teahouseInfo ~= nil then
            return teahouseInfo
        end
    end
    local teahouseLists = XH.lobby:getModule("TeaHouseList"):getTeaHousePendingList()
    if teahouseLists == nil then
        return nil
    end
    if #teahouseLists > 0 then
        for i = 1, #teahouseLists do
            if teahouseLists[i].nTeaNumber == nTeaNumber then
                return teahouseLists[i]
            end
        end
    end
    return nil
end

function PromoteModule:reqSpreadTaskReal()
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    if teahouseData == nil then
        return
    end
    local teahouseInfo = self:getPendTeahouseInfo(teahouseData:getTeaNumber())
    if not teahouseInfo or not teahouseInfo.nTeaOwnerNumid then
        return
    end
    self:reqSpreadTask(teahouseData:getTeaNumber(), teahouseInfo.nTeaOwnerNumid, teahouseData:isOwner(), teahouseData:isAdmin())
end

function PromoteModule:getPromoteTeaNumber()
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    if teahouseData ~= nil then
        return teahouseData:getTeaNumber()
    end
    return nil
end

function PromoteModule:reqSpreadAwardList(teaNumber, draw_type)
    if teaNumber == nil then
        teaNumber = self:getPromoteTeaNumber()
    end
    if teaNumber == nil then
        return
    end
    self:startReq("ReqSpreadAwardList", teaNumber, draw_type, 10)
end

function PromoteModule:respSpreadAwardList(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self._rewardLog = {}
        self._rewardLog[XH.playerData:getNumberID()] = data.list
        self:dispatchEvent({name = self.EVENT_FULSH_PROMOTE_LOG})
    end
end

-- 玩家完成奖励
function PromoteModule:reqSpreadDraw(teaNumber, draw_type)
    local nowDate = self:getTodayZeroTime()
    self:startReq("ReqSpreadDraw", teaNumber, draw_type, nowDate, 10)
end

function PromoteModule:respSpreadDraw(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        local awards = data.award
        local awardInfo = {}
        awardInfo.prize_names = {}
        awardInfo.image_urls = {}
        for _, award in pairs(awards) do
            local url = award.image_url or ""
            awardInfo.prize_names[#awardInfo.prize_names + 1] = "x" .. award.count
            awardInfo.image_urls[#awardInfo.image_urls + 1] = url
        end
        if data.draw_type == PromoteDefine.DRAW_TYPE.FIRST or data.draw_type == PromoteDefine.DRAW_TYPE.SECOND then
            local task = self:getTaskInfo()
            if task ~= nil then
                local award_type = PromoteDefine.AWARD_TYPE.PERIOD
                local type = 0
                if data.draw_type == PromoteDefine.DRAW_TYPE.FIRST and task.award.share_first == 0 then
                    award_type = PromoteDefine.AWARD_TYPE.DRAW_SHARE_FIRST
                    type = 1
                end
                if data.draw_type == PromoteDefine.DRAW_TYPE.SECOND and task.award.share_second == 0 then
                    award_type = PromoteDefine.AWARD_TYPE.DRAW_SHARE_SECOND
                    type = 2
                end
                local disableShareDouble = false -- todo 后期可配
                if award_type ~= PromoteDefine.AWARD_TYPE.PERIOD and not disableShareDouble then
                    awardInfo.isDouble = true
                    awardInfo.nowDate = data.nowDate
                    awardInfo.award_type = award_type
                    awardInfo.teaNumber = data.teaNumber
                end
            end
        end
        awardInfo.isPromote = true
        XH.viewManager:openView("BackActivityAwardView", nil, nil, awardInfo) -- todo 埋点
        self:reqSpreadTaskReal()
        XH.playerData:flushPlayerDrop()
    end
end

function PromoteModule:reqSpreadInfo()
    if PromoteConfig[XH.areaData:getLobbyID()] == nil then
        return
    end
    XH.lobby:getModule("TeaHouseList"):reqTeaHouseList()
    self:startReq("ReqSpreadInfo", 10)
end

function PromoteModule:respSpreadInfo(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self._baseInfo = {}
        self._baseInfo[XH.playerData:getNumberID()] = data
        -- 大厅弹窗
        if self:isOnlineAct() then
            local hallTiems = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_PROMOTE_HALL_DIALOG, 0)
            if hallTiems < PromoteConfig[XH.areaData:getLobbyID()].HALLTIMES then
                XH.viewManager:openView("PromoteHallView")
                XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PROMOTE_HALL_DIALOG, hallTiems + 1)
            end
        end
        self:flushPromoteActIcon()
    end
end

function PromoteModule:flushPromoteActIcon()
    self:dispatchEvent({name = self.EVENT_FULSH_PROMOTE_DAY_ICON, data = {icon = LobbyConfig.ICON_ID.PROMOTE_ACT, isShow = self:isOnlineAct()}})
end

function PromoteModule:reqSpreadInviteClick(teaNumber)
    self:startReq("ReqSpreadInviteClick", teaNumber, 10)
end

function PromoteModule:respSpreadInviteClick(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
    end
end

function PromoteModule:reqSpreadInviteList(teaNumber, page)
    self:startReq("ReqSpreadInviteList", teaNumber, page, 10)
end

function PromoteModule:respSpreadInviteList(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self._inviteInfo = {}
        self._inviteInfo[XH.playerData:getNumberID()] = data
        self:dispatchEvent({name = self.EVENT_FULSH_PROMOTE_INVITE_LIST})
    end
end

function PromoteModule:getPlayerType(isOwner, isAdmin)
    local player_type = PromoteDefine.PLAYER_TYPE.PLAYER
    if isOwner then
        player_type = PromoteDefine.PLAYER_TYPE.OWNER
    elseif isAdmin then
        player_type = PromoteDefine.PLAYER_TYPE.ADMIN
    end
    return player_type
end

-- 请求任务进度
function PromoteModule:reqSpreadTask(teaNumber, owner_numid, isOwner, isAdmin, isFirstInTea)
    local player_type = self:getPlayerType(isOwner, isAdmin)
    if player_type ~= PromoteDefine.PLAYER_TYPE.ADMIN then
        owner_numid = 0
    end
    self:startReq("ReqSpreadTask", teaNumber, player_type, owner_numid, isFirstInTea, 10)
end

function PromoteModule:flushPromoteTask()
    self:dispatchEvent({name = self.EVENT_FULSH_PROMOTE_TASK})
end

function PromoteModule:respSpreadTask(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self._taskInfo = {}
        self._taskInfo[XH.playerData:getNumberID()] = data
        self:updateTeaIcon()
        self:flushPromoteTask()
        if data.isFirstInTea then
            self:checkTeaHouseShow()
        end
    end
end

function PromoteModule:isFirstOpenDay()
    if PromoteConfig[XH.areaData:getLobbyID()] == nil then
        return false
    end
    if self:getBaseInfo() == nil then
        return false
    end
    local nowDate = self:getTodayZeroTime()
    if self:getBaseInfo().start == nowDate then
        return true
    end
    return false
end

function PromoteModule:getTaskRed()
    local baseInfo = self:getBaseInfo()
    local taskInfo = self:getTaskInfo()
    if baseInfo ~= nil and taskInfo ~= nil then
        if taskInfo.task_type == PromoteDefine.TASK_TYPE.USER then
            local userConfig = baseInfo.player_task_config
            local userRound = taskInfo.task.round or 0
            local isUserFirstRed = userRound >= userConfig.first_target and taskInfo.award.first ~= 1
            local isUserSecondRed = userRound >= userConfig.second_target and taskInfo.award.second ~= 1
            local isUserPeriodRed = taskInfo.task.complete_task >= userConfig.period_target and taskInfo.award.period ~= 1
            return isUserFirstRed or isUserSecondRed or isUserPeriodRed
        end
        local isLeftRed = false
        local isRightRed = false
        if taskInfo.first_task_type == PromoteDefine.TASK_TYPE.A then
            isLeftRed = taskInfo.task.total_round >= baseInfo.owner_first_task_times and taskInfo.award.first ~= 1
        end
        if taskInfo.task_type == PromoteDefine.TASK_TYPE.A then
            isRightRed = taskInfo.award.left_award > 0
        elseif taskInfo.task_type == PromoteDefine.TASK_TYPE.B then
            isRightRed = taskInfo.task.round >= taskInfo.task_config.task_target and taskInfo.award.period ~= 1
        elseif taskInfo.task_type == PromoteDefine.TASK_TYPE.C then
            isRightRed = taskInfo.award.left_award > 0
        end
        return isLeftRed or isRightRed
    end
    return false
end

function PromoteModule:updateTeaIcon()
    if self._scene == nil or self._btn == nil then
        return
    end
    if self:getBaseInfo() == nil or self:getTaskInfo() == nil then
        return
    end
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    if teahouseData == nil then
        return
    end
    local isFirstDay = self:isFirstOpenDay()
    local isOwner = teahouseData:isOwner()
    local red = self:getTaskRed() and isOwner
    local text = ""
    if self:getTaskInfo().task_type == PromoteDefine.TASK_TYPE.USER then
        if self:getTaskInfo().task.round <= self:getBaseInfo().player_task_config.second_target then
            text = self:getTaskInfo().task.round .. '/' .. self:getBaseInfo().player_task_config.second_target
        end

    elseif self:getTaskInfo().task_type == PromoteDefine.TASK_TYPE.A then
        if isOwner then
            if self:isOpenAct() and (isFirstDay or self:getTaskInfo().award.left_award <= 0) then
                text = '次日可领房卡\n' .. (self:getTaskInfo().task.round * self:getTaskInfo().task.task_award)
            else
                text = '可领房卡\n' .. self:getTaskInfo().award.left_award
            end
        else
            text = '今日场次\n' .. self:getTaskInfo().task.round
        end
    elseif self:getTaskInfo().task_type == PromoteDefine.TASK_TYPE.B then
        if isOwner then
            text = '总场次\n' .. self:getTaskInfo().task.round
        end
    elseif self:getTaskInfo().task_type == PromoteDefine.TASK_TYPE.C then
        if isOwner then
            text = '可领房卡\n' .. math.max(self:getTaskInfo().award.left_award, 0)
        else
            text = '今日场次\n' .. self:getTaskInfo().task.round
        end
    end
    self._btn:getChildByName("_KW_PROMOTE_REDPOINT"):setVisible(red)
    self._btn:getChildByName("_KW_PROMOTE_TEXT"):setString(text)
    self._btn:getChildByName("_KW_PROMOTE_OWNER"):setVisible(self:getTaskInfo().task_type ~= PromoteDefine.TASK_TYPE.USER)
    self._btn:getChildByName("_KW_PROMOTE_USER"):setVisible(self:getTaskInfo().task_type == PromoteDefine.TASK_TYPE.USER)
    self._btn:setVisible(true)
end

-- 今日0点时间戳
function PromoteModule:getTodayZeroTime()
    local time = os.time()
    local timestamp = time - ((time + 28800) % 86400)
    return timestamp
end

-- 修改外部ICON状态
function PromoteModule:setMainIcon(scene, btn)
    self._scene = scene
    self._btn = btn
    if self:isOpenAct() then
        self:updateTeaIcon()
    end
end

function PromoteModule:hasActPlayModes()
    if PromoteConfig[XH.areaData:getLobbyID()] == nil then
        return false
    end
    local playModes = XH.teaHouseManager:getPlayModeInfos()
    if playModes == nil then
        return false
    end
    for i = 1, #playModes do
        if playModes[i].nGameId == PromoteConfig[XH.areaData:getLobbyID()].GAMEID and playModes[i].bIsEnable and string.find(playModes[i].acData, PromoteConfig[XH.areaData:getLobbyID()].RULE) then
            return true
        end
    end
    return false
end

-- 获取符合的桌子信息
function PromoteModule:getActPlayTables()
    local fitTables = nil
    if PromoteConfig[XH.areaData:getLobbyID()] == nil then
        return fitTables
    end
    local teaData = XH.teaHouseManager:getTeaHouseData()
    if teaData == nil then
        return fitTables
    end
    -- 先查2人再查4人
    local fit2Tables = nil -- 2缺1人
    local tableInfos = teaData:getTableInfos()
    for tableid, info in pairs(tableInfos) do
        if info.state == 0 and info.gameid == PromoteConfig[XH.areaData:getLobbyID()].GAMEID and string.find(info.data, PromoteConfig[XH.areaData:getLobbyID()].RULE) and info.size == 2 then
            for i = 1, #info.nNumid do
                if info.nNumid[i] == 0 then
                    if fit2Tables ~= nil then
                        if fit2Tables.createTime < info.createTime then
                            fit2Tables = info
                        end
                    else
                        fit2Tables = info
                    end
                end
            end
        end
    end
    if fit2Tables ~= nil then
        return fit2Tables
    end
    local fit4Tables1 = nil -- 4缺1人
    local fit4Tables2 = nil -- 4缺2人
    local fit4Tables3 = nil -- 4缺3人
    local tableInfos = teaData:getTableInfos()
    for tableid, info in pairs(tableInfos) do
        if info.state == 0 and info.gameid == PromoteConfig[XH.areaData:getLobbyID()].GAMEID and string.find(info.data, PromoteConfig[XH.areaData:getLobbyID()].RULE) and info.size == 4 then
            local nLeft = 0
            for i = 1, #info.nNumid do
                if info.nNumid[i] == 0 then
                    nLeft = nLeft + 1
                end
            end
            if nLeft == 1 then
                if fit4Tables1 ~= nil then
                    if fit4Tables1.createTime < info.createTime then
                        fit4Tables1 = info
                    end
                else
                    fit4Tables1 = info
                end
            elseif nLeft == 2 then
                if fit4Tables2 ~= nil then
                    if fit4Tables2.createTime < info.createTime then
                        fit4Tables2 = info
                    end
                else
                    fit4Tables2 = info
                end
            elseif nLeft == 3 then
                if fit4Tables3 ~= nil then
                    if fit4Tables3.createTime < info.createTime then
                        fit4Tables3 = info
                    end
                else
                    fit4Tables3 = info
                end
            end
        end
    end
    if fit4Tables1 ~= nil then
        return fit4Tables1
    elseif fit4Tables2 ~= nil then
        return fit4Tables2
    elseif fit4Tables3 ~= nil then
        return fit4Tables3
    end
    return fitTables
end

-- 获取符合的玩法信息
function PromoteModule:getActPlayModes(playerCount)
    local fitModes = nil
    if PromoteConfig[XH.areaData:getLobbyID()] == nil then
        return fitModes
    end
    local playModes = XH.teaHouseManager:getPlayModeInfos()
    if playModes == nil then
        return fitModes
    end
    for i = 1, #playModes do
        if playModes[i].nGameId == PromoteConfig[XH.areaData:getLobbyID()].GAMEID and playModes[i].bIsEnable and string.find(playModes[i].acData, PromoteConfig[XH.areaData:getLobbyID()].RULE) then
            for j = 1, #playModes[i].anPlayerCount do
                if playModes[i].anPlayerCount[j] == 2 then
                    return playModes[i]
                elseif playModes[i].anPlayerCount[j] == 4 then
                    fitModes = playModes[i]
                end
            end
        end
    end
    return fitModes
end

function PromoteModule:getBaseInfo()
    if self._baseInfo ~= nil then
        return self._baseInfo[XH.playerData:getNumberID()]
    end
    return nil
end

function PromoteModule:getTaskInfo()
    if self._taskInfo ~= nil then
        return self._taskInfo[XH.playerData:getNumberID()]
    end
    return nil
end

function PromoteModule:getInviteInfo()
    if self._inviteInfo ~= nil then
        return self._inviteInfo[XH.playerData:getNumberID()]
    end
    return nil
end

function PromoteModule:getRewardLog()
    if self._rewardLog ~= nil then
        return self._rewardLog[XH.playerData:getNumberID()]
    end
    return nil
end

function PromoteModule:getCacheList()
    return self._cacheList
end

function PromoteModule:setCacheListTime(teanumber, numid)
    if self._cacheList == nil then
        self._cacheList = {}
    end
    if self._cacheList[teanumber] == nil then
        self._cacheList[teanumber] = {}
    end
    self._cacheList[teanumber][numid] = os.time()
end

function PromoteModule:shreToWx(type)
    if PromoteConfig[XH.areaData:getLobbyID()] == nil then
        return
    end
    local function shareCallback()
        XH.TipTool.showToast("发送邀请链接成功", 3)
    end
    local titile = PromoteConfig[XH.areaData:getLobbyID()].wxShareTitle
    local text = PromoteConfig[XH.areaData:getLobbyID()].wxShareText
    if type == 1 then
        titile = PromoteConfig[XH.areaData:getLobbyID()].wxNotifyTitle
        text = PromoteConfig[XH.areaData:getLobbyID()].wxNotifyText
    end
    local shareInfo = {
        textTitle = titile, -- 分享标题
        textTitleUrl = "", -- 微信不用
        site = "", -- 微信不用
        siteUrl = "", -- 微信不用
        imagePath = "", -- 微信不用
        url = PromoteConfig[XH.areaData:getLobbyID()].url, -- 微信网页模式分享的链接
        type = "url", -- word是文字分享，url网页模式分享，pic分享图片
        scene = "0", -- 0是分享到好友会话，1是分享到朋友圈
        imageUrl = "com.xm.zjgamecenter", -- 分享图片地址（分享网页模式，需要传安卓包名）
        text = text, -- 分享内容 
        comment = "download" -- 微信不用, 用以区分直接分享下载地址和游戏内link
    }
    XH.sdkManager:startShare(shareInfo, shareCallback, self)
end

function PromoteModule:quickCreatePlayMode(teaNumber)
    local setMode = {}
    local lastPlayMode = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_PROMOTE_LAST_PLAYMODE, "")
    if lastPlayMode == "" then
        setMode = json.decode(PromoteConfig[XH.areaData:getLobbyID()].DEFAULTRULE)
    else
        setMode = json.decode(lastPlayMode)
        if PromoteConfig[XH.areaData:getLobbyID()].DELRULE ~= nil and PromoteConfig[XH.areaData:getLobbyID()].DELRULE ~= "" then
            setMode.data = string.gsub(setMode.data, PromoteConfig[XH.areaData:getLobbyID()].DELRULE, "")
        end
        if string.find(setMode.data, PromoteConfig[XH.areaData:getLobbyID()].RULE) == nil then
            setMode.data = setMode.data .. PromoteConfig[XH.areaData:getLobbyID()].RULE
            setMode.data = string.gsub(setMode.data, "';TabeleColor", ",红中癞子';TabeleColor")
        end
    end
    local teaData = XH.teaHouseManager:getTeaHouseData()
    if teaNumber == nil then
        if teaData == nil then
            return
        else
            teaNumber = teaData:getTeaNumber()
        end
    end
    -- 这时候可能存在比赛场id没设置的情况
    TeaHouse.manager.teahouseSetup:setTeaNumber(teaNumber)
    TeaHouse.manager.teahouseSetup:setPlayModeInfo(setMode)
end

function PromoteModule:normalCreatePlayMode(teaNumber)
    local gameid = 0
    if PromoteConfig[XH.areaData:getLobbyID()] ~= nil then
        gameid = PromoteConfig[XH.areaData:getLobbyID()].GAMEID
    end
    local teaData = XH.teaHouseManager:getTeaHouseData()
    if teaData == nil then
        return
    end
    TeaHouse.manager.teahouseSetup:setTeaNumber(teaData:getTeaNumber())
    TeaHouse.manager.teahouseSetup:addPlayMode(nil, gameid, true)
end

function PromoteModule:checkTeaHouseShow()
    local position = XH.playerData:getPlayerPosition()
    if position.gameAppID and position.gameAppID ~= 0 then
        return
    end
    if not self:isOnlineAct() then
        return
    end
    local teahouseData = TeaHouse.manager.teahouseData
    if teahouseData == nil then
        return
    end
    local teahouseInfo = self:getPendTeahouseInfo(teahouseData:getTeaNumber())
    if not teahouseInfo or not teahouseInfo.nTeaOwnerNumid then
        return
    end
    local teaTimes = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_PROMOTE_TEA_DIALOG, 0)
    if teaTimes < TeaHouse.PromoteConfig[XH.areaData:getLobbyID()].TEATIMES then
        local worldPos = nil
        if self._scene ~= nil and self._btn ~= nil then
            local posX, posY = self._btn:getPosition()
            worldPos = self._btn:getParent():convertToWorldSpace(cc.p(posX, posY))
        end
        XH.viewManager:openView("PromoteView", nil, teahouseData:isAdmin(), teahouseData:isOwner(), teahouseData:getTeaNumber(), teahouseInfo.nTeaOwnerNumid, worldPos)
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PROMOTE_TEA_DIALOG, teaTimes + 1)
    end

    if not teahouseData:isOwner() then
        return
    end
    local taskInfo = self:getTaskInfo()
    if taskInfo == nil or self:hasActPlayModes() then
        return
    end
    if taskInfo.click_num >= PromoteConfig[XH.areaData:getLobbyID()].hallViewClickUserNum then
        local data = {}
        local showClubStr = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_PROMOTE_TEA_OWNER_CREATE, "")
        if showClubStr ~= "" then
            data = json.decode(showClubStr)
        end
        for i = 1, #data do
            if data[i] == teahouseData:getTeaNumber() then
                return
            end
        end
        data[#data + 1] = teahouseData:getTeaNumber()
        XH.viewManager:openView("PromoteTipView", nil, PromoteDefine.TIP_TYPE.TEA_TIP_OWNER)
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PROMOTE_TEA_OWNER_CREATE, json.encode(data))
    end
end

-- 打开玩家弹窗
function PromoteModule:openUserTipView()
    local teahouseData = TeaHouse.manager.teahouseData
    if teahouseData == nil then
        return
    end
    if teahouseData:isOwner() or teahouseData:isAdmin() then
        return
    end
    if not XH.lobby:getModule("Promote"):hasActPlayModes() then
        return
    end
    local data = {}
    local showClubStr = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_PROMOTE_USER_CREATE, "")
    if showClubStr ~= "" then
        data = json.decode(showClubStr)
    end
    for i = 1, #data do
        if data[i] == teahouseData:getTeaNumber() then
            return
        end
    end
    data[#data + 1] = teahouseData:getTeaNumber()
    XH.viewManager:openView("PromoteTipView", nil, PromoteDefine.TIP_TYPE.TEA_TIP_USER)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PROMOTE_USER_CREATE, json.encode(data))
end

-- 获取游戏规则
function PromoteModule:analysisZhRuleEx(data)
    local luaString = "return {" .. data .. "}"
    local luaFunction = loadstring(luaString)
    local ruleStr = ""
    if luaFunction then
        local rule = luaFunction()
        ruleStr = rule.ZhRule or ""
    end
    return ruleStr
end

-- 加入对应固定房间
function PromoteModule:joinModeRoom(teaNumber)
    if TeaHouse.manager.teahouseGeneral:checkIsInGame() then
        XH.TipTool.showToast("您目前为游戏暂离状态")
        return true
    end
    if not TeaHouse.manager.teahouseData:checkSelfPower("JoinTable") then
        XH.TipTool.showToast("无加入桌子权限")
        return true
    end
    if teaNumber == nil then
        local teahouseData = XH.teaHouseManager:getTeaHouseData()
        if teahouseData ~= nil then
            teaNumber = teahouseData:getTeaNumber()
        end
    end
    if teaNumber == nil then
        return
    end
    local teahouseList = XH.lobby:getModule("Promote"):getPendTeahouseInfo(teaNumber)
    if teahouseList == nil then
        return false
    end
    local fitTables = XH.lobby:getModule("Promote"):getActPlayTables()
    if fitTables ~= nil then
        XH.teaHouseManager:reJoinTeaHouse(teahouseList.nPasswd, fitTables.tableid)
        return true
    end
    local playModes = XH.lobby:getModule("Promote"):getActPlayModes()
    if playModes ~= nil then
        XH.teaHouseManager:reJoinTeaHouse(teahouseList.nPasswd, nil, nil, nil, playModes.nPlayModeId)
        return true
    end
    return false
end

return PromoteModule
  �q  