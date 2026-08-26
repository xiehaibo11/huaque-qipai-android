local NewPlayTypeToBView = class("NewPlayTypeToBView", XH.ViewBase)
local GameNewTypeConfiger = require("app.Config.Parse.GameNewTypeConfiger")

local KW_BTN_TYPE = {
    OPEN = "OPEN",
    GET = "GET",
}

local KW_ACT_STATE = {
    NOTOPEN = 0,
    OPEN = 1
}

function NewPlayTypeToBView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/NewPlayType/NewPlayTypeBAct.csb"
end

function NewPlayTypeToBView:getBindingInfo()
    return {
        ["_KW_PANEL_NOT_OPEN"] = { varName = "_panelNotOpen" },
        ["_KW_PANEL_OPEN"] = { varName = "_panelOpen" },
        ["_KW_BTN_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KW_BTN_KF_1"] = { varName = "_btnKF1", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventKF" },
        ["_KW_BTN_KF_2"] = { varName = "_btnKF2", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventKF" },
        ["_KW_BTN_RULE_1"] = { varName = "_btnRule1", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventRule" },
        ["_KW_BTN_RULE_2"] = { varName = "_btnRule2", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventRule" },
        ["_KW_TXT_OPEN_COUNT"] = { varName = "_openCount" },
        ["_KW_TXT_AWARD_COUNT"] = { varName = "_awardCount" },
        ["_KW_TXT_ORDER_COUNT"] = { varName = "_orderCount" },
        ["_KW_BTN_OPEN"] = { varName = "_btnOpen", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventOpen" },
        ["_KW_TXT_STAGE"] = { varName = "_stage" },
        ["_KW_TXT_STAGE_AWARD"] = { varName = "_stageAward" },
        ["_KW_PROCESS_BG"] = { varName = "_processBg" },
        ["_KW_PROCESS"] = { varName = "_process" },
        ["_KW_STAGE_ITEM"] = { varName = "_stageItem" },
        ["_KW_BTN_GET"] = { varName = "_btnGet", type = XH.UI_TYPE.BUTTON },
        ["_KW_CLOSEST_PANLE"] = { varName = "_closestPanel" },
        ["_KW_TXT_ACT_TIME"] = { varName = "_actTime" },
        ["_KW_FINISH_TITEL"] = { varName = "_finishTitle" },
        ["_KW_TXT_NEW_TYPE"] = { varName = "_txtNewPlayType" },
        ["_KW_IMG_APPLY_GAME"] = { varName = "_imgApplyGame" },
        ["_KW_TXT_APPLY_GAME"] = { varName = "_txtApplyGame" },
    }
end

function NewPlayTypeToBView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("NewPlayType"), eventKeyName = "EVENT_GET_ACT_INFO", callBack = "onEventActInfo" },
        { module = XH.lobby:getModule("NewPlayType"), eventKeyName = "EVENT_GET_AWARD_SUCCESS", callBack = "onEventGetAwardSuccess" },
    }
end

function NewPlayTypeToBView:ctor(param)
    param = param or {}
    NewPlayTypeToBView.super.ctor(self, param)

    self._info = param.info
    self._getStage = 0
    self._open = "0"

    self:initUI()
end

function NewPlayTypeToBView:onTouchEventClose(send, eventType)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_ACT_B_CLOSE, nil, {open_state = self._open})
    self:close()
end

function NewPlayTypeToBView:onTouchEventKF(send, eventType)
    XH.lobby:getModule("CusService"):oponCusServerView(nil,"NewPlayType")
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_ACT_B_KF, nil, {open_state = self._open})
end

function NewPlayTypeToBView:onTouchEventRule(send, eventType)
    XH.viewManager:openView("NewPlayTypeActRuleView", nil, 2)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_ACT_B_RULE, nil, {open_state = self._open})
end

function NewPlayTypeToBView:onTouchEventOpen(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    if teahouseData then
        if not teahouseData:needShowCreateTable() then
            local gameID = GameNewTypeConfiger:getDefultGameIDByLobbyID(XH.areaData:getLobbyID())
            XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_CHOOSE_GAME_ID, gameID, { keyCacheInfo = "_teaHouse" })
            local teahouseManager = XH.teaHouseManager:getTeaHouseManager()
            teahouseManager.viewManager:openView("TeaHouseCreateBoxRoomView", 0, {
                payModeType = teahouseData:getTeaHousePayMode(),
                payTypeTable = teahouseData:getPayTypeByID()
            } )
        else
            XH.lobby:getModule("NewPlayType"):reqTeaHousePayType()
        end
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_ACT_B_OPEN, {label = KW_BTN_TYPE.OPEN}, {open_state = self._open})
    self:close()
end

function NewPlayTypeToBView:onTouchEventGetAward(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local canGetStage = 0
    for _, v in pairs(self._info.stageAwards) do
        if v.status == XH.lobby:getModule("NewPlayType").KW_STAGE_STATE.ACHIEVE then
            canGetStage = v.id
            break
        end
    end
    if canGetStage ~= 0 then
        self._getStage = canGetStage
        XH.lobby:getModule("NewPlayType"):reqGetStageAward(canGetStage)
    else
        XH.TipTool.showToast("您的开桌数暂未达到领奖要求，立即邀请玩家开桌吧")
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_ACT_B_OPEN, {label = KW_BTN_TYPE.GET}, {open_state = self._open})
end

function NewPlayTypeToBView:onEventActInfo(event)
    if not event or not event.info then
        return
    end
    self._info = event.info
    self:initUI()
end

function NewPlayTypeToBView:onEventGetAwardSuccess(event)
    XH.TipTool.showTip({
        type = XH.TIP_LAYER_TYPE.OK
    }, "奖励已发放到当前亲友圈绑定房卡，扣卡模式切换到“扣圈主划入亲友圈的卡”进行查看")
    if self._getStage and self._getStage ~= 0 then
        for _, v in pairs(self._info.stageAwards) do
            if v.id == self._getStage then
                v.status = XH.lobby:getModule("NewPlayType").KW_STAGE_STATE.GETED
            end
        end
        self:initStage()
        self:initProgress()
    end
end

function NewPlayTypeToBView:initApplyGameTips()
    if not self._imgApplyGame or not self._txtApplyGame then
        return
    end
    local applyGameStr = GameNewTypeConfiger:getNewPlayTypeApplyGamesByLobbyID(XH.areaData:getLobbyID())
    if applyGameStr and applyGameStr ~= "" then
        self._txtApplyGame:setString(applyGameStr)
        local strSize = self._txtApplyGame:getContentSize()
        local bgSize = self._imgApplyGame:getContentSize()
        local whiteLen = 25
        self._imgApplyGame:setContentSize(strSize.width + whiteLen * 2, bgSize.height)
        self._imgApplyGame:setVisible(true)
    end
end

function NewPlayTypeToBView:initUI()
    self:initApplyGameTips()
    if self._info.isApply == KW_ACT_STATE.OPEN then
        self._open = "1"
        self:initOpenUI()
    else
        self._open = "0"
        self:initNotOpenUI()
    end
    self._panelNotOpen:setVisible(self._info.isApply == KW_ACT_STATE.NOTOPEN)
    self._panelOpen:setVisible(self._info.isApply == KW_ACT_STATE.OPEN)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_ACT_B_PUSH, nil, {open_state = self._open})
end

function NewPlayTypeToBView:initOpenUI()
    local closestStage, closestAward = self:getClosestStage()
    if not closestStage or not closestAward then
        self._closestPanel:setVisible(false)
        self._finishTitle:setVisible(true)
    else
        self._stage:setString(closestStage)
        self._stageAward:setString(closestAward)
    end
    if self._info.actStart and self._info.actEnd then
        self._actTime:setString("活动时间:" .. self._info.actStart .. " 至 " .. self._info.actEnd)
    end
    self:initStage()
    self:initProgress()
    local teahouseType = XH.lobby:getModule("NewPlayType"):checkTeahouseType()
    if not teahouseType then
        self._btnGet:setVisible(false)
        return
    end
    if teahouseType == XH.lobby:getModule("NewPlayType").TEA_HOUSE_TYPE.FREE or 
    teahouseType == XH.lobby:getModule("NewPlayType").TEA_HOUSE_TYPE.WITHNEWTYPEMODE then
        self._btnGet:loadTexture("new_play_type_b_get_award.png", ccui.TextureResType.plistType)
        self._btnGet:addTouchEventListener(handler(self, self.onTouchEventGetAward))
    else
        self._btnGet:loadTexture("new_play_type_b_open_mode.png", ccui.TextureResType.plistType)
        self._btnGet:addTouchEventListener(handler(self, self.onTouchEventOpen))
    end
    self._btnGet:setVisible(true)
end

function NewPlayTypeToBView:initNotOpenUI()
    self._openCount:setString(self._info.openOwnersCount)
    local stageAwardInfo = self._info.stageAwards
    local awardCount = 0
    local KW_MAX_AWARD_TEXT_WIDTH = 140
    for _, v in pairs(stageAwardInfo) do
        awardCount = awardCount + v.prize_count
    end
    self._awardCount:setString(awardCount)
    local awardCountTextSize = self._awardCount:getContentSize()
    while awardCountTextSize.width > KW_MAX_AWARD_TEXT_WIDTH do
        self._awardCount:setFontSize(self._awardCount:getFontSize() - 1)
        awardCountTextSize = self._awardCount:getContentSize()
    end
    self._orderCount:setString(self._info.bookUsersCount)
    self:initNewPlayTypeText()
end

function NewPlayTypeToBView:initNewPlayTypeText()
    local newPlayTypeText = GameNewTypeConfiger:getNewPlayTypeTextByLobbyID(XH.areaData:getLobbyID())
    if newPlayTypeText then
        local str = ""
        for i = 1, #newPlayTypeText do
            str = str .. newPlayTypeText[i]
            if i ~= #newPlayTypeText then
                str = str .. "\n"
            end
        end
        self._txtNewPlayType:setString(str)
    end
end

function NewPlayTypeToBView:getClosestStage()
    local haveGames = self._info.progress
    for _, v in pairs(self._info.stageAwards) do
        if v.status == XH.lobby:getModule("NewPlayType").KW_STAGE_STATE.INCONFORMITY then
            return v.games - haveGames, v.prize_count
        end
    end
    return
end

function NewPlayTypeToBView:initStage()
    local stageAwardInfo = self._info.stageAwards
    for _, v in pairs(stageAwardInfo) do
        local stageItem = self._stageItem:clone()
        XH.UITool.setText(stageItem, "KW_TXT_STAGE", v.games .. "桌")
        XH.UITool.setText(stageItem, "KW_TXT_AWARD", "x" .. v.prize_count)
        if v.status == XH.lobby:getModule("NewPlayType").KW_STAGE_STATE.GETED then
            XH.UITool.setVisible(stageItem, "KW_IMG_COVER", true)
            XH.UITool.loadTexture(stageItem, "KW_STAGE_POINT", "new_play_type_b_stage_1.png", ccui.TextureResType.plistType)
            if v.id == #stageAwardInfo then
                self._btnGet:loadTexture("new_play_type_b_geted_award.png", ccui.TextureResType.plistType)
                self._btnGet:setTouchEnabled(false)
                self._closestPanel:setVisible(false)
                self._finishTitle:setVisible(true)
            end
        end
        self._processBg:addChild(stageItem)
        stageItem:setPosition(cc.p(self._processBg:getContentSize().width / #stageAwardInfo * v.id, self._processBg:getContentSize().height / 2))
        stageItem:setVisible(true)
    end
end

function NewPlayTypeToBView:initProgress()
    local percent = 0
    local haveGames = self._info.progress
    local stageAwardInfo = self._info.stageAwards
    for i = 1, #stageAwardInfo do
        if stageAwardInfo[i].status == XH.lobby:getModule("NewPlayType").KW_STAGE_STATE.INCONFORMITY then
            if i > 1 then
                percent = percent + ((haveGames - stageAwardInfo[i - 1].games) / (stageAwardInfo[i].games - stageAwardInfo[i - 1].games)) * 100 / #stageAwardInfo
            else
                percent = haveGames / stageAwardInfo[i].games * 100 / #stageAwardInfo
            end
            self._process:setPercent(percent)
            return
        else
            percent = percent + (100 / #stageAwardInfo)
        end
    end
    self._process:setPercent(percent)
end

return NewPlayTypeToBView�/  