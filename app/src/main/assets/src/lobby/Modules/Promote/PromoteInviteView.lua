local PromoteInviteView = class("PromoteInviteView", XH.ViewBase)
local PromoteDefine = require("lobby.Modules.Promote.Define")

function PromoteInviteView:ctor(teaNumber)
    PromoteInviteView.super.ctor(self)
    self._teaNumber = teaNumber
    XH.lobby:getModule("Promote"):reqSpreadInviteList(teaNumber, 0)
end

function PromoteInviteView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/Promote/PromoteInviteLayer.csb"
end

function PromoteInviteView:getProxyEvents()
    return {{module = XH.lobby:getModule("Promote"), eventKeyName = "EVENT_FULSH_PROMOTE_INVITE_LIST", callBack = "initUI"}, {module = XH.lobby:getModule("Promote"), eventKeyName = "EVENT_FULSH_PROMOTE_TASK", callBack = "flushInviteInfo"}}
end

function PromoteInviteView:getBindingInfo()
    return {
        --  
        ["_KW_BTN_CLOSE"] = {varName = "KW_BTN_CLOSE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickClose"},
        ["_KW_BTN_INVITE"] = {varName = "KW_BTN_INVITE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickInviteAll"},
        ["_KW_BTN_LEFT"] = {varName = "KW_BTN_LEFT", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickPageLeft"},
        ["_KW_BTN_RIGHT"] = {varName = "KW_BTN_RIGHT", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickPageRight"},
        ["_KW_TEXT_PAGE"] = {varName = "KW_TEXT_PAGE"},
        ["_KW_SCROLLVIEW"] = {varName = "KW_SCROLLVIEW"},
        ["_KW_PANEL_USER"] = {varName = "KW_PANEL_USER"},
        ["_KW_PANEL_BG"] = {varName = "KW_PANEL_BG"}
    }
end

function PromoteInviteView:startRunAction()
    local repeatAct2 = cc.RepeatForever:create(cc.Sequence:create(cc.CallFunc:create(function()
        local nodes = self.KW_SCROLLVIEW:getChildren()
        if #nodes == 0 then
            return
        end
        for i = 1, #nodes do
            local name = nodes[i]:getName()
            local nLeftTime = self:getLeftCDTime(name)
            nodes[i]:getChildByName("_KW_BTN_INVITE_S"):setVisible(nLeftTime == 0)
            nodes[i]:getChildByName("_KW_BTN_INVITE_B"):setVisible(nLeftTime > 0)
            nodes[i]:getChildByName("_KW_BTN_INVITE_B"):getChildByName("_KW_LEFT_TIME"):setString(nLeftTime .. "s")
        end
    end), cc.DelayTime:create(1)))
    self.KW_PANEL_BG:runAction(repeatAct2)
end

function PromoteInviteView:initUI()
    local KW_ICON_DEFAULT_PNG = "tea_house_member_item_head.png"
    local inviteInfo = XH.lobby:getModule("Promote"):getInviteInfo()
    if inviteInfo == nil then
        return
    end
    self.KW_PANEL_BG:stopAllActions()
    self.KW_SCROLLVIEW:removeAllChildren()
    self.KW_TEXT_PAGE:setString(inviteInfo.current_page .. "/" .. inviteInfo.last_page)
    self._current_page = inviteInfo.current_page
    self._last_page = inviteInfo.last_page

    local inviteData = inviteInfo.data
    local startX = 215
    local startY = 478
    local defaultWidth = 435
    local defaultHeight = 323
    for idx, v in pairs(inviteData) do
        local temp = self.KW_PANEL_USER:clone()
        local head = temp:getChildByName("_KW_HEAD")
        if head ~= nil then
            head:getChildByName("_KW_IMAGE"):setVisible(not (v.avatar ~= ""))
            if v.avatar ~= "" then
                local size = head:getChildByName("_KW_IMAGE"):getContentSize()
                local headImage = XH.RemoteImage.new():setPosition(head:getContentSize().width / 2, head:getContentSize().height / 2):ignoreContentAdaptWithSize(false):setContentSize(size.width, size.height):addTo(head)
                headImage:setUrl(v.avatar or "")
            end
        end
        temp:getChildByName("_LABEL_NAME"):setString(XH.StringTool.cutStringByLength(v.nickname))
        temp:getChildByName("_LABEL_NUM"):setString("ID:" .. v.numid)
        local nLeftTime = self:getLeftCDTime(v.numid)
        temp:getChildByName("_KW_BTN_INVITE_S"):setVisible(nLeftTime == 0)
        temp:getChildByName("_KW_BTN_INVITE_S"):addTouchEventListener(handler(self, self.onTouchEventInvite))
        temp:getChildByName("_KW_BTN_INVITE_B"):setVisible(nLeftTime > 0)
        temp:getChildByName("_KW_BTN_INVITE_B"):getChildByName("_KW_LEFT_TIME"):setString(nLeftTime .. "s")
        temp:setVisible(true)
        temp:setName(v.numid)
        self.KW_SCROLLVIEW:addChild(temp)
        idx = idx - 1
        local lineX = idx % 3
        local lineY = math.floor(idx / 3)
        temp:setPosition(cc.p(startX + lineX * defaultWidth, startY - defaultHeight * lineY))
    end
    self:startRunAction()
end

-- 没有符合的桌子会先创建一个桌子
function PromoteInviteView:onTouchEventInvite(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local node = send:getParent()
    if node == nil then
        return
    end
    local numid = node:getName()
    if self:getLeftCDTime(numid) > 0 then
        return
    end
    local toNumIDs = {}
    toNumIDs[#toNumIDs + 1] = tonumber(numid)
    self:sendInviteMsg(toNumIDs)
end

function PromoteInviteView:getLeftCDTime(numid)
    local cacheList = XH.lobby:getModule("Promote"):getCacheList()
    if cacheList == nil then
        return 0
    end
    if cacheList[self._teaNumber] == nil then
        return 0
    end
    if cacheList[self._teaNumber][tostring(numid)] ~= nil then
        local time = 60 + cacheList[self._teaNumber][tostring(numid)] - os.time()
        if time <= 0 then
            return 0
        end
        return time
    end
    return 0
end

function PromoteInviteView:onClickInviteAll()
    local count = self.KW_SCROLLVIEW:getChildrenCount()
    if count == 0 then
        return
    end
    local toNumIDs = {}
    local childrennode = self.KW_SCROLLVIEW:getChildren()
    for i = 1, count do
        local send = childrennode[i]
        if send ~= nil then
            local numid = send:getName()
            if self:getLeftCDTime(numid) == 0 then
                toNumIDs[#toNumIDs + 1] = tonumber(numid)
            end
        end
    end
    if #toNumIDs <= 0 then
        XH.TipTool.showToast("当前页无可邀请状态玩家")
        return
    end
    self:sendInviteMsg(toNumIDs)
end

function PromoteInviteView:flushInviteInfo()
    local toNumIDs = self._toNumIDs
    self._toNumIDs = nil
    if toNumIDs == nil then
        return
    end
    self:sendInviteMsg(toNumIDs, true)
end

function PromoteInviteView:sendInviteMsg(toNumIDs, isNext)
    local teaData = XH.teaHouseManager:getTeaHouseData()
    if teaData == nil then
        return
    end
    local msg = {}
    -- 先找桌子
    local fitTables = XH.lobby:getModule("Promote"):getActPlayTables()
    if fitTables == nil then
        -- 再找玩法
        local playModes = XH.lobby:getModule("Promote"):getActPlayModes()
        if playModes == nil then
            if isNext then
                return
            end
            -- 没有的话先强制创建，再邀请
            self._toNumIDs = toNumIDs
            XH.lobby:getModule("Promote"):quickCreatePlayMode(self._teaNumber)
            return
        else
            msg.modeId = playModes.nPlayModeId
            msg.gameName = playModes.acName
            msg.playerCont = playModes.anPlayerCount[1] -- 人数
            if playModes.nPlayerSize > 1 then
                for i = 1, playModes.nPlayerSize do
                    if playModes.anPlayerCount[i] == 2 then
                        msg.playerCont = 2
                        break
                    else
                        msg.playerCont = playModes.anPlayerCount[i]
                    end
                end
            end
            msg.cont = playModes.anCount[1] -- 局数
            msg.rule = XH.lobby:getModule("Promote"):analysisZhRuleEx(playModes.acData)
        end
    else
        msg.tableId = fitTables.tableid
        msg.gameName = fitTables.name
        msg.playerCont = fitTables.size -- 人数
        msg.cont = fitTables.count -- 局数
        msg.rule = XH.lobby:getModule("Promote"):analysisZhRuleEx(fitTables.data)
    end

    msg.type = PromoteDefine.TIP_TYPE.OWNER_INVITE
    msg.nick = XH.playerData:getNickName()
    msg.numid = XH.playerData:getNumberID()
    msg.teaPasswd = teaData:getTeaHousePwd()
    msg.url = XH.playerData:getHead()
    XH.teaHouseManager:reqPromoteInviteAll(XH.playerData:getNumberID(), toNumIDs, json.encode(msg))
    for i = 1, #toNumIDs do
        XH.lobby:getModule("Promote"):setCacheListTime(self._teaNumber, tostring(toNumIDs[i]))
    end
    XH.TipTool.showToast("邀请成功")
end

function PromoteInviteView:onClickPageLeft()
    if self._current_page == 1 then
        return
    end
    XH.lobby:getModule("Promote"):reqSpreadInviteList(self._teaNumber, self._current_page - 1)
end

function PromoteInviteView:onClickPageRight()
    if self._current_page >= self._last_page then
        return
    end
    XH.lobby:getModule("Promote"):reqSpreadInviteList(self._teaNumber, self._current_page + 1)
end

function PromoteInviteView:onClickClose()
    self:close()
end

return PromoteInviteView
 �#  