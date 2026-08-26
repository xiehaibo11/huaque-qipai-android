local MainView = class("MainView", XH.ViewBase)

--local defaultPanelHeight = 155
--XH.viewManager:openView("TeaHouseRecallMain")

function MainView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/BusinessAct/RecallAct/RecallActMain.csb"
end

function MainView:getBindingInfo()
    return {
        ["_KW_SCROLLVIEW"] = { varName = "_scrollView" },
        ["_KW_PANEL_MODEL"] = { varName = "_panelModel" },
        ["_KW_BTN_CHOSE_FK"] = { varName = "_btnChoseFk" },
        ["_KW_IMG_QIPAO"] = { varName = "_imgQiPao" },
        ["_KW_PANEL_CHOSE"] = { varName = "_KW_PANEL_CHOSE", onTouchEnded = "onTouchEventChoseState" },
        ["_KW_BTN_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KW_BTN_RECORDS"] = { varName = "_btnGoRecords", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventRecords" },
        ["_KW_BTN_ALL_PRIZES"] = { varName = "_btnGetAllPrize", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventGetAllPrizes" },
        ["_KW_TEXT_DEADLINE"] = { varName = "_deadLineTime" },
        ["_KW_LOADINGBAR"] = { varName = "_loadingBar" },
        ["_KW_PANEL_MODEL_NEW"] = { varName = "_panelModelNew" },
        ["_KW_RECALL_NUM"] = { varName = "_textRecallNum" },
        ["_KW_BTN_GO_RECALL"] = { varName = "_btnGoRecall", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventGoRecall" },
    }
end

function MainView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("TeaHouseBusinessAct"), eventKeyName = "EVENT_TEAOWNER_MSG", callBack = "onEventTeaOwnerMsg" },
        { module = XH.lobby:getModule("TeaHouseBusinessAct"), eventKeyName = "EVENT_REFRESH_CHOSE_STATE", callBack = "onEventRefreshChoseState" },
        { module = XH.lobby:getModule("TeaHouseBusinessAct"), eventKeyName = "EVENT_TEAOWNER_AWARD_REFRESH", callBack = "onEventGetAwardRefresh" },
    }
end

function MainView:ctor(param)
    MainView.super.ctor(self,param)
    self._awardMsg = nil
    self:initChoseState()
    self:initQiPaoState()
    XH.lobby:getModule("TeaHouseBusinessAct"):reqUnRecallsLists()
end

function MainView:initChoseState()
    local haveChoseState = XH.lobby:getModule("TeaHouseBusinessAct"):getNumberWithTeaidKey()
    if haveChoseState ~= 0 then
        self._btnChoseFk:setVisible(haveChoseState == XH.lobby:getModule("TeaHouseBusinessAct").KW_SET_CHOSE_STATE.GET_OWN_CARDS)
    end
end

function MainView:onEventGetAwardRefresh(msg)
    if msg then
        self._awardMsg = XH.lobby:getModule("TeaHouseBusinessAct"):getTeaOwnerMsg()
        self:initUI()
        self:refreshPlayerData()
    end
end

function MainView:onEventTeaOwnerMsg(msg)
    if msg then
        self._awardMsg = msg.data
        self:initUI()
    end
end

function MainView:initUI()
    if self._awardMsg then
        self._deadLineTime:setText(self._awardMsg.actTime)
        self._textRecallNum:setString("目前召回:" .. #self._awardMsg.recallLists)
        --if #self._scrollView:getChildren() == #self._awardMsg.rewardList then
        --    for i = 1, #self._awardMsg.rewardList do
        --        local childPanel = XH.UITool.seekWidgetByName(self._scrollView, "panelModel" .. i)
        --        if childPanel then
        --            self:setPersonMsg(childPanel, i)
        --        end
        --    end
        --    return
        --end
        --self._scrollView:removeAllChildren()
        --for i = 1, #self._awardMsg.rewardList do
        --    local panelModel = self._panelModel:clone()
        --    self:setPersonMsg(panelModel, i)
        --    panelModel:setName("panelModel" .. i)
        --    panelModel:setVisible(true)
        --    self._scrollView:addChild(panelModel)
        --end
        --self._scrollView:setInnerContainerSize(cc.size(self._panelModel:getContentSize().width, defaultPanelHeight * #self._awardMsg.rewardList))
        if #self._loadingBar:getChildren() == #self._awardMsg.rewardList then
            for i = 1, #self._awardMsg.rewardList do
                local childPanel = XH.UITool.seekWidgetByName(self._loadingBar, "panelModel" .. i)
                if childPanel then
                    self:setPersonMsgNew(childPanel, i)
                    childPanel:setPosition(self._loadingBar:getContentSize().width * (self._awardMsg.taskList[i]/self._awardMsg.taskList[#self._awardMsg.rewardList]),10)
                end
            end
            self._loadingBar:setPercent((#self._awardMsg.recallLists/#self._awardMsg.rewardList)*100)
            return
        end 
        self._loadingBar:removeAllChildren()
        for i = 1, #self._awardMsg.rewardList do
            local panelModel = self._panelModelNew:clone()
            self:setPersonMsgNew(panelModel, i)
            panelModel:setName("panelModel" .. i)
            panelModel:setVisible(true)
            self._loadingBar:addChild(panelModel)
            panelModel:setPosition(self._loadingBar:getContentSize().width * (self._awardMsg.taskList[i]/self._awardMsg.taskList[#self._awardMsg.rewardList]),10)
        end
        self._loadingBar:setPercent((#self._awardMsg.recallLists/#self._awardMsg.rewardList)*100)
    end
end

function MainView:setPersonMsgNew(panelModel, i)
    local awardData = XH.UITool.seekWidgetByName(panelModel, "_KW_TEXT_AWARD_DATA")
    local percentData = XH.UITool.seekWidgetByName(panelModel, "_KW_RECALL_NUMS")
    local light = XH.UITool.seekWidgetByName(panelModel, "_KW_AWARD_FLIGHT")
    local have = XH.UITool.seekWidgetByName(panelModel, "_KW_IMG_HAVE")
    light:setVisible(false)
    if awardData then
        awardData:setText("x" .. self._awardMsg.rewardList[i])
    end
    if percentData then
        percentData:setText(self._awardMsg.taskList[i])
    end
    local node = XH.UITool.seekWidgetByName(panelModel, "_KW_IMG_FIT_HEAD")
    if node and self._awardMsg.awardurl[i] then
        XH.UITool.reloadNodeRemoteImage(node, self._awardMsg.awardurl[i], "award_img", false)
        local awardImg = XH.UITool.seekNodeByName(node, "award_img")
        awardImg:setMaxContentSize(node:getContentSize())
    end
    if #self._awardMsg.recallLists >= self._awardMsg.taskList[i] then
        if self._awardMsg.teaOwnerPer then
            for _, value in pairs(self._awardMsg.teaOwnerPer) do
                if tonumber(value) == i then
                    have:setVisible(true)
                    return
                end
            end
        end
        light:setVisible(true)
    end
end

function MainView:setPersonMsg(panelModel, i)
    local ruleData = XH.UITool.seekWidgetByName(panelModel, "_KW_RECALL_RULES")
    local awardData = XH.UITool.seekWidgetByName(panelModel, "_KW_TEXT_AWARD_DATA")
    local percentData = XH.UITool.seekWidgetByName(panelModel, "_KW_RECALL_NUMS")
    if ruleData then
        if self._awardMsg.taskList[i] == 1 then
            ruleData:setText("召回玩家1人并完成1场对局")
        else
            ruleData:setText("召回玩家" .. self._awardMsg.taskList[i] .. "人各完成1场对局")
        end
    end
    if awardData then
        awardData:setText("x" .. self._awardMsg.rewardList[i])
    end
    if percentData then
        percentData:setText(#self._awardMsg.recallLists .. "/" .. self._awardMsg.taskList[i])
    end
    self:judgePrizeState(panelModel, #self._awardMsg.recallLists, self._awardMsg.taskList[i], i)
end

function MainView:judgePrizeState(node, percent, allPercent, level)
    local getPrizeBtn = XH.UITool.seekWidgetByName(node, "_KW_BTN_GET_PRIZE")
    if getPrizeBtn then
        getPrizeBtn:setName(level)
    else
        getPrizeBtn = XH.UITool.seekWidgetByName(node, level)
    end
    if getPrizeBtn then
        if percent >= allPercent then
            if self._awardMsg.teaOwnerPer then
                for _, value in pairs(self._awardMsg.teaOwnerPer) do
                    if tonumber(value) == level then
                        getPrizeBtn:loadTexture("RecallAct_ylq.png", ccui.TextureResType.plistType)
                        getPrizeBtn:addTouchEventListener(handler(self, self.onTouchEventHavePrize))
                        return
                    end
                end
            end
            getPrizeBtn:loadTexture("RecallAct_getPrize.png", ccui.TextureResType.plistType)
            getPrizeBtn:addTouchEventListener(handler(self, self.onTouchEventGetPrize))
        else
            getPrizeBtn:loadTexture("RecallAct_gobtn.png", ccui.TextureResType.plistType)
            getPrizeBtn:addTouchEventListener(handler(self, self.onTouchEventGoRecall))
        end
    end
end

function MainView:reqTeaOwnerMsg()
    XH.lobby:getModule("TeaHouseBusinessAct"):reqPersonMsg()
end

function MainView:onTouchEventGoRecall(send, eventType)
    self:btnState(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.viewManager:openView("TeaHouseRecallLists")
end

function MainView:onTouchEventRecords(send, eventType)
    XH.viewManager:openView("TeaHouseRecallRecord")
end

function MainView:onEventRefreshChoseState(msg)
    local haveChoseState = XH.lobby:getModule("TeaHouseBusinessAct"):getNumberWithTeaidKey()
    self._btnChoseFk:setVisible(XH.lobby:getModule("TeaHouseBusinessAct").KW_SET_CHOSE_STATE.GET_OWN_CARDS == haveChoseState)
    if haveChoseState ~= 0 then
        self._imgQiPao:setVisible(false)
    end
end

function MainView:initQiPaoState()
    local haveChoseState = XH.lobby:getModule("TeaHouseBusinessAct"):getNumberWithTeaidKey()
    self._imgQiPao:setVisible(haveChoseState == 0)
end

function MainView:btnState(send, eventType)
    if eventType == ccui.TouchEventType.began then
        XH.audioManager:play("BUTTON_CLICK")
        send:setColor(cc.c3b(160,160,160))
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setColor(cc.c3b(255, 255, 255))
    end
end

function MainView:onTouchEventHavePrize(send, eventType)
    self:btnState(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.TipTool.showToast("已领取奖励，继续加油哦~", 3)
end

function MainView:onTouchEventGetPrize(send, eventType)
    self:btnState(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if tonumber(send:getName()) then
        local haveIgnoreState = XH.lobby:getModule("TeaHouseBusinessAct"):getIgnoreWithTeaidKey()
        if haveIgnoreState == 0 then
            XH.viewManager:openView("TeaHouseRecallTitle", nil, { data = "own", level = tonumber(send:getName()) })
        else
            XH.lobby:getModule("TeaHouseBusinessAct"):reqGetPrize(XH.lobby:getModule("TeaHouseBusinessAct").KW_GET_PRIZE_STATE.GET_PERCENT_PRIZE, tonumber(send:getName()))
        end
    end
end

function MainView:onTouchEventChoseState(send, eventType)
   -- local haveIgnoreState = XH.lobby:getModule("TeaHouseBusinessAct"):getIgnoreWithTeaidKey()
   -- if haveIgnoreState == 0 then
   --     return
   -- end
    self._btnChoseFk:setVisible(not self._btnChoseFk:isVisible())
    if self._btnChoseFk:isVisible() then
        XH.lobby:getModule("TeaHouseBusinessAct"):setNumberWithTeaidKey(XH.lobby:getModule("TeaHouseBusinessAct").KW_SET_CHOSE_STATE.GET_OWN_CARDS)
    else
        XH.lobby:getModule("TeaHouseBusinessAct"):setNumberWithTeaidKey(XH.lobby:getModule("TeaHouseBusinessAct").KW_SET_CHOSE_STATE.GET_TEAHOUSE_CARDS)
    end
end

function MainView:onTouchEventGetAllPrizes(send, eventType)
    self:btnState(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local canGetPrize = 0
    local teaOwnerMsg = XH.lobby:getModule("TeaHouseBusinessAct"):getTeaOwnerMsg()
    for i = 1, #teaOwnerMsg.taskList do
        if #teaOwnerMsg.recallLists >= teaOwnerMsg.taskList[i] then
            canGetPrize = canGetPrize + 1
        end
    end
    if teaOwnerMsg and #teaOwnerMsg.teaOwnerPer == #teaOwnerMsg.rewardList then
        XH.TipTool.showToast("恭喜您已完成所有任务~", 3)
        return
    end
    if canGetPrize <= #teaOwnerMsg.teaOwnerPer then
        XH.TipTool.showToast("先完成任务才可以领取奖励哦~", 3)
        return
    end
    local haveIgnoreState = XH.lobby:getModule("TeaHouseBusinessAct"):getIgnoreWithTeaidKey()
    if haveIgnoreState == 0 then
        XH.viewManager:openView("TeaHouseRecallTitle", nil, { data = "all" })
    else
        XH.lobby:getModule("TeaHouseBusinessAct"):reqGetPrize(XH.lobby:getModule("TeaHouseBusinessAct").KW_GET_PRIZE_STATE.GET_ALL_PRIZE, 0)
    end
end

function MainView:refreshPlayerData()
    XH.playerData:flushPlayerDrop()
    XH.playerData:flushGoldCoin()
    -- refreshTeaHouse
    XH.teaHouseManager:refreshTeaHouseProp()
end

function MainView:onTouchEventClose(send, eventType)
    self:close()
end

return MainView
   93  