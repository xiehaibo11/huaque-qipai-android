local MainView = class("MainView", XH.ViewBase)

local PlayNode = import(".PlayNode")
local RechargeNode = import(".RechargeNode")

--local KW_NO_FINISH = 0
local KW_AWARD_FINISH = 1
--local KW_IS_FINISH = 2

function MainView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/BackActivity/BackActivityMain.csb"
end

function MainView:getBindingInfo()
    return {
        ["_KW_TEXT_ACT_TIME"] = { varName = "_textActTime" },
        ["_KW_BTN_BOX_PLAY"] = { varName = "_btnBoxPlay", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventShowBoxPlay" },
        ["_KW_BTN_RECHARGE"] = { varName = "_btnRecharge", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventShowRecharge" },
        ["_KW_BTN_GOLD_PLAY"] = { varName = "_btnGoldPlay", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventShowGoldPlay" },
        ["_KW_LISTVIEW_LABEL"] = { varName = "_listviewLabel" },
        ["_KW_BOX_PLAY_PANEL"] = { varName = "_panelBoxPlay" },
        ["_KW_GOLD_PLAY_PANEL"] = { varName = "_panelGoldPlay" },
        ["_KW_RECHARGE_PANEL"] = { varName = "_panelRecharge" },
        -- ["_KW_BTN_CLOSE"] = { varName = "KW_BTN_CLOSE", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KW_BOX_PLAY_HONGDIAN"] = { varName = "_playBoxRed" },
        ["_KW_RECHARGE_HONGDIAN"] = { varName = "_rechargeRed" },
        ["_KW_GOLD_PLAY_HONGDIAN"] = { varName = "_playGoldRed" },
        -- ["_KW_BTN_AWARD_CENTER"] = { varName = "KW_BTN_AWARD_CENTER", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventAwardCenter" },
        ["_KW_PANEL_AWARD_TIP"] = { varName = "_awardTip" },
        ["_KW_BTN_LEAVE_ACT"] = { varName = "_btnLeaveAct", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventLeaveAct" },
        ["_KW_BTN_BACK_ACT"] = { varName = "_btnBackAct", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventBackAct" },
    }
end

function MainView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("BackActivity"), eventName = "BackActClose", callBack = "close" },
        { module = XH.lobby:getModule("BackActivity"),eventName = "BackActivityFlushInfo", callBack = "flushActInfo"},
        { module = XH.lobby:getModule("BackActivity"),eventName = "EVENT_GET_ACT_INFO_BACK", callBack = "onGetActInfoBack" },
    }
end

function MainView:ctor(param)
    cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/hall/Image/Activity/BackActivity/backActivity.plist")
    param = param or { }
    MainView.super.ctor(self, param)
    self._panelBoxPlayNode = nil
    self._panelGoldPlayNode = nil
    self._panelRechargeNode = nil
    self._leftTabBtns = { self._btnBoxPlay, self._btnRecharge, self._btnGoldPlay }
    XH.lobby:getModule("BackActivity"):reqBackActProtocol()
    self._btnBoxPlay:retain()
    self._btnRecharge:retain()
    self._btnGoldPlay:retain()
end

function MainView:onGetActInfoBack(event)
    self:initView()
end

function MainView:initView()
    local actInfo = XH.lobby:getModule("BackActivity"):getActInfo()

    local boxPlayHaveAward = false
    boxPlayHaveAward = boxPlayHaveAward or(actInfo.loginBoxCnt[actInfo.loginDay] == KW_AWARD_FINISH)
    for i = 0, 3 do
        boxPlayHaveAward = boxPlayHaveAward or(actInfo.playBoxAaward[tostring(i)] == KW_AWARD_FINISH)
    end
    
    local goldPlayHaveAward = false
    goldPlayHaveAward = goldPlayHaveAward or(actInfo.loginGoldCnt[actInfo.loginDay] == KW_AWARD_FINISH)
    for i = 0, 3 do
        goldPlayHaveAward = goldPlayHaveAward or(actInfo.playGoldAaward[tostring(i)] == KW_AWARD_FINISH)
    end
    if actInfo.isGold then
        boxPlayHaveAward = false
    else
        goldPlayHaveAward = false
    end
    self._playBoxRed:setVisible(boxPlayHaveAward)
    self._playGoldRed:setVisible(goldPlayHaveAward)

    local rechargeHaveAward = false
    for i = 1, #actInfo.rechargeAaward do
        rechargeHaveAward = rechargeHaveAward or(actInfo.rechargeAaward[i] == KW_AWARD_FINISH)
    end
    self._rechargeRed:setVisible(rechargeHaveAward)

    self:initActTime(actInfo.actTime)
    self:initActLabel(actInfo.isGold,actInfo.loginDay)
end

function MainView:flushActInfo()
    local actInfo = XH.lobby:getModule("BackActivity"):getActInfo()

    local boxPlayHaveAward = false
    boxPlayHaveAward = boxPlayHaveAward or(actInfo.loginBoxCnt[actInfo.loginDay] == KW_AWARD_FINISH)
    for i = 0, 3 do
        boxPlayHaveAward = boxPlayHaveAward or(actInfo.playBoxAaward[tostring(i)] == KW_AWARD_FINISH)
    end
   
    local goldPlayHaveAward = false
    goldPlayHaveAward = goldPlayHaveAward or(actInfo.loginGoldCnt[actInfo.loginDay] == KW_AWARD_FINISH)
    for i = 0, 3 do
        goldPlayHaveAward = goldPlayHaveAward or(actInfo.playBoxAaward[tostring(i)] == KW_AWARD_FINISH)
    end

    if actInfo.isGold then
        boxPlayHaveAward = false
    else
        goldPlayHaveAward = false
    end

    self._playBoxRed:setVisible(boxPlayHaveAward)
    self._playGoldRed:setVisible(goldPlayHaveAward)

    local rechargeHaveAward = false
    for i = 1, #actInfo.rechargeAaward do
        rechargeHaveAward = rechargeHaveAward or(actInfo.rechargeAaward[i] == KW_AWARD_FINISH)
    end
    self._rechargeRed:setVisible(rechargeHaveAward)

    self._actTime = actInfo.actTime

    if self._panelBoxPlayNode ~= nil then
        self._panelBoxPlayNode:initNode("Box")
    end
    if self._panelGoldPlayNode ~= nil then
        self._panelGoldPlayNode:initNode("Gold")
    end
    if self._panelRechargeNode ~= nil then
        self._panelRechargeNode:initNode("Recharge")
    end
end

------------------------------------------------- init ---------------------------------------------
function MainView:initActTime(time)
    self._actTime = time
    local timeNode = self._textActTime
    if timeNode then
        local sequence = cc.Sequence:create(cc.CallFunc:create( function()
            if self._actTime == 0 then
                timeNode:setString("倒计时：0天00时00分")
            else
                local m_TimeD = math.floor(self._actTime /(3600 * 24))
                local m_TimeH = math.floor((self._actTime - m_TimeD * 3600 * 24) / 3600)
                local m_TimeM = math.floor((self._actTime - m_TimeD * 3600 * 24 - m_TimeH * 3600) / 60)
                local m_TimeS = math.floor(self._actTime -(m_TimeD * 3600 * 24 + m_TimeM * 60 + m_TimeH * 3600))
                m_TimeH = string.format("%02d", m_TimeH)
                m_TimeM = string.format("%02d", m_TimeM)
                m_TimeS = string.format("%02d", m_TimeS)
                if m_TimeD > 0 then
                    local timeString = "倒计时：" .. m_TimeD .. "天" .. m_TimeH .. "时" .. m_TimeM .. "分"
                    timeNode:setString(timeString)
                else
                    local timeString = "倒计时：" .. m_TimeH .. "时" .. m_TimeM .. "分" .. m_TimeS .. "秒"
                    timeNode:setString(timeString)
                end
                self._actTime = self._actTime - 1
            end
        end ), cc.DelayTime:create(1))
        local action = cc.RepeatForever:create(sequence)
        timeNode:runAction(action)
    end
end

function MainView:initActLabel(isGold,loginDay)
    if loginDay == 8 then
        self._listviewLabel:removeAllChildren()      
        self._listviewLabel:addChild(self._btnRecharge)
        self:showRechargeLayer()
    elseif isGold then
        self._listviewLabel:removeAllChildren()
        self._listviewLabel:addChild(self._btnGoldPlay)
        self._listviewLabel:addChild(self._btnRecharge)
        self._listviewLabel:addChild(self._btnBoxPlay)
        self:showGoldPlayLayer()
    else
        self._listviewLabel:removeAllChildren()
        self._listviewLabel:addChild(self._btnBoxPlay)
        self._listviewLabel:addChild(self._btnRecharge)
        self._listviewLabel:addChild(self._btnGoldPlay)
        self:showBoxPlayLayer()
    end
end

function MainView:showBoxPlayLayer()
    if self._panelBoxPlayNode == nil then
        self._panelBoxPlayNode = self:createBoxPlayNode()
    end

    for _, tabNode in pairs(self._leftTabBtns) do
        local name = tabNode:getName()
        local key = string.find(name, "BOX")
        local color = key ~= nil and cc.c3b(248, 237, 197) or cc.c3b(140, 83, 45)
        local tabName = tabNode:getChildByName("_KW_TEXT_BTN")
        tabName:setTextColor(color)
        tabNode:setBright(key == nil)
        tabNode:setEnabled(key == nil)
    end

    self._panelBoxPlay:setVisible(true)
    self._panelGoldPlay:setVisible(false)
    self._panelRecharge:setVisible(false)
end

function MainView:showGoldPlayLayer()
    if self._panelGoldPlayNode == nil then
        self._panelGoldPlayNode = self:createGoldPlayNode()
    end

    for _, tabNode in pairs(self._leftTabBtns) do
        local name = tabNode:getName()
        local key = string.find(name, "GOLD")
        local color = key ~= nil and cc.c3b(248, 237, 197) or cc.c3b(140, 83, 45)
        local tabName = tabNode:getChildByName("_KW_TEXT_BTN")
        tabName:setTextColor(color)
        tabNode:setBright(key == nil)
        tabNode:setEnabled(key == nil)
    end

    self._panelBoxPlay:setVisible(false)
    self._panelGoldPlay:setVisible(true)
    self._panelRecharge:setVisible(false)
end

function MainView:showRechargeLayer()
    if self._panelRechargeNode == nil then
        self._panelRechargeNode = self:createRechargeNode()
    end

    for _, tabNode in pairs(self._leftTabBtns) do
        local name = tabNode:getName()
        local key = string.find(name, "RECHARGE")
        local color = key ~= nil and cc.c3b(248, 237, 197) or cc.c3b(140, 83, 45)
        local tabName = tabNode:getChildByName("_KW_TEXT_BTN")
        tabName:setTextColor(color)
        tabNode:setBright(key == nil)
        tabNode:setEnabled(key == nil)
    end

    self._panelBoxPlay:setVisible(false)
    self._panelGoldPlay:setVisible(false)
    self._panelRecharge:setVisible(true)
end

function MainView:createBoxPlayNode()
    if self._panelBoxPlay then
        local node = PlayNode.new(nil, self, "Box")
        self._panelBoxPlay:addChild(node)
        node:setName("BoxPlayNode")
        return node
    end
end

function MainView:createGoldPlayNode()
    if self._panelGoldPlay then
        local node = PlayNode.new(nil, self, "Gold")
        self._panelGoldPlay:addChild(node)
        node:setName("GoldPlayNode")
        return node
    end
end

function MainView:createRechargeNode()
    if self._panelRecharge then
        local node = RechargeNode.new(nil, self, "Recharge")
        self._panelRecharge:addChild(node)
        node:setName("RechargeNode")
        return node
    end
end

--------------------------------------
function MainView:onTouchEventClose(send, eventType)
    local actInfo = XH.lobby:getModule("BackActivity"):getActInfo()

    local boxPlayHaveAward = false
    boxPlayHaveAward = boxPlayHaveAward or(actInfo.loginBoxCnt[actInfo.loginDay] == KW_AWARD_FINISH)
    for i = 0, 3 do
        boxPlayHaveAward = boxPlayHaveAward or(actInfo.playBoxAaward[tostring(i)] == KW_AWARD_FINISH)
    end

    local goldPlayHaveAward = false
    goldPlayHaveAward = goldPlayHaveAward or(actInfo.loginGoldCnt[actInfo.loginDay] == KW_AWARD_FINISH)
    for i = 0, 3 do
        goldPlayHaveAward = goldPlayHaveAward or(actInfo.playBoxAaward[tostring(i)] == KW_AWARD_FINISH)
    end

    if actInfo.isGold then
        boxPlayHaveAward = false
    else
        goldPlayHaveAward = false
    end

    local rechargeHaveAward = false
    for i = 1, #actInfo.rechargeAaward do
        rechargeHaveAward = rechargeHaveAward or(actInfo.rechargeAaward[i] == KW_AWARD_FINISH)
    end

    if boxPlayHaveAward or goldPlayHaveAward or rechargeHaveAward then
        self._awardTip:setVisible(true)
    else
        self:close()
    end
end

function MainView:onTouchEventShowBoxPlay(send, eventType)
    self:showBoxPlayLayer()
end

function MainView:onTouchEventShowGoldPlay(send, eventType)
    self:showGoldPlayLayer()
end

function MainView:onTouchEventShowRecharge(send, eventType)
    self:showRechargeLayer()
end

function MainView:onTouchEventAwardCenter(send, eventType)
    XH.viewManager:openView("AwardCenterView")
end

function MainView:onTouchEventLeaveAct(send, eventType)
    self:close()
end

function MainView:onTouchEventBackAct(send, eventType)
    self._awardTip:setVisible(false)
end

return MainView�1  