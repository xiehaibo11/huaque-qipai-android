local NewPlayTypeToBViewNew = class("NewPlayTypeToBViewNew", XH.ViewBase)
local GameNewTypeConfiger = require("app.Config.Parse.GameNewTypeConfiger")

local KW_FISRT_AWARD_STATUS = {
    CANNOTGET = 0,
    CANGET = 1,
    GETED = 2
}

function NewPlayTypeToBViewNew:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/NewPlayType/NewPlayTypeBActNew.csb"
end

function NewPlayTypeToBViewNew:getBindingInfo()
    return {
        ["_KW_BTN_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KW_TXT_ACT_TIME"] = { varName = "_actTime" },
        ["_KW_TXT_FIRST_AWARD"] = { varName = "_firstAward" },
        ["_KW_BTN_GET_1"] = { varName = "_btnGetFirstAward" },
        ["_KW_AWARD_COVER"] = { varName = "_firstAawrdCover" },
        ["_KW_FIRST_AWARD_GETED_FLAG"] = { varName = "_firstAawrdFlag" },
        ["_KW_TXT_TODAY_CNT"] = { varName = "_todayPlayed" },
        ["_KW_TXT_TOTAL_AWARD"] = { varName = "_totalAward" },
        ["_KW_TXT_TOMORROW_AWARD"] = { varName = "_tomorrowAward" },
        ["_KW_TXT_EVERY_AWARD"] = { varName = "_everyAward" },
        ["_KW_BTN_GET_2"] = { varName = "_btnGetEveryDayAward" },
        ["_KW_BTN_SHARE"] = { varName = "_btnShare", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventShare" },
        ["_KW_BTN_CREATE_MODE"] = { varName = "_btnCreateMode", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventAutoCreate" },
        ["_KW_TIPS"] = { varName = "_createModeTips" },
        ["_KW_BTN_INTERDUCE"] = { varName = "_btnInterduce", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventInterduce" },
        ["_KW_BTN_RULE"] = { varName = "_btnRule", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventRule" },
        ["_KW_BTN_HISTORY"] = { varName = "_btnHistory", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventHistory" },
    }
end

function NewPlayTypeToBViewNew:getProxyEvents()
    return {
        { module = XH.lobby:getModule("NewPlayType"), eventKeyName = "EVENT_GET_ACT_INFO_NEW", callBack = "onEventActInfo" },
        { module = XH.lobby:getModule("NewPlayType"), eventKeyName = "EVENT_UPDATE_ACT_INFO", callBack = "onEventUpdateActInfo" },
    }
end

function NewPlayTypeToBViewNew:ctor(param)
    param = param or {}
    NewPlayTypeToBViewNew.super.ctor(self, param)

    self._info = param.info
    self:initUI()
end

function NewPlayTypeToBViewNew:onEventActInfo(event)
    if not event or not event.info then
        return
    end
    self._info = event.info
    self:initUI()
end

function NewPlayTypeToBViewNew:onEventUpdateActInfo(event)
    XH.lobby:getModule("NewPlayType"):reqNewPlayTypeActInfoNew(true)
end

function NewPlayTypeToBViewNew:initUI()
    self:initActTime()
    self:initAct1UI()
    self:initAct2UI()
    self:initBottomUI()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_ACT_B_NEW_PUSH, {item_id = "push"})
end

function NewPlayTypeToBViewNew:initActTime()
    if not self._info or not self._actTime then
        return
    end
    if not self._info.act_start_date or not self._info.act_end_date then
        return
    end
    self._actTime:setString("活动时间:" .. self._info.act_start_date .. "~" .. self._info.act_end_date)
end

function NewPlayTypeToBViewNew:initAct1UI()
    if not self._info or not self._info.act_1 then
        return
    end
    if self._firstAward then
        self._firstAward:setString(self._info.act_1.first_prize .. self._info.act_1.first_prize_name)
    end
    if self._btnGetFirstAward and self._firstAawrdCover and self._firstAawrdFlag then
        if self._info.act_1.get_prize_status == KW_FISRT_AWARD_STATUS.CANNOTGET then
            self._btnGetFirstAward:addTouchEventListener(handler(self, self.onTouchEventInvite1))
        elseif self._info.act_1.get_prize_status == KW_FISRT_AWARD_STATUS.CANGET then
            self._btnGetFirstAward:loadTexture("new_play_type_b_get_arwad.png", ccui.TextureResType.plistType)
            self._btnGetFirstAward:ignoreContentAdaptWithSize(true)
            self._btnGetFirstAward:addTouchEventListener(handler(self, self.getFisrtAward))
        elseif self._info.act_1.get_prize_status == KW_FISRT_AWARD_STATUS.GETED then
            self._btnGetFirstAward:setVisible(false)
            self._firstAawrdCover:setVisible(true)
            self._firstAawrdFlag:setVisible(true)
        end
    end
end

function NewPlayTypeToBViewNew:initAct2UI()
    if not self._info or not self._info.act_2 then
        return
    end
    if self._todayPlayed then
        self._todayPlayed:setString(self._info.act_2.today_count)
    end
    if self._totalAward then
        self._totalAward:setString(self._info.act_2.all_can_get)
    end
    if self._tomorrowAward then
        self._tomorrowAward:setString(self._info.act_2.tomorrow_can_get)
    end
    if self._everyAward then
        self._everyAward:setString(self._info.act_2.each_play_get)
    end
    if self._btnGetEveryDayAward then
        if self._info.act_2.all_can_get <= 0 then
            self._btnGetEveryDayAward:loadTexture("new_play_type_b_invite.png", ccui.TextureResType.plistType)
            self._btnGetEveryDayAward:ignoreContentAdaptWithSize(true)
            self._btnGetEveryDayAward:addTouchEventListener(handler(self, self.onTouchEventInvite2))
        else
            self._btnGetEveryDayAward:addTouchEventListener(handler(self, self.getEveryDayAward))
        end
    end
end

function NewPlayTypeToBViewNew:initBottomUI()
    if not self._btnShare or not self._btnCreateMode or not self._createModeTips then
        return
    end
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    if not teahouseData then
        return
    end
    if (teahouseData:getPlayModeInfoFlag() or not teahouseData:needShowCreateTable()) and teahouseData:getPlayModeInfosCount() > 0 and XH.teaHouseManager:checkAutoCreateNewPlayTypeModeCondition() then
        self._btnCreateMode:setVisible(true)
        local greetInfo = XH.lobby:getModule("NewPlayType"):getGreetInfo()
        if greetInfo and greetInfo.get_greet_prize_status == 0 then
            XH.UITool.setText(self._createModeTips, "_KW_TXT_CREATE_AWARD", greetInfo.greet_prize)
            self._createModeTips:setVisible(true)
        else
            self._createModeTips:setVisible(false)
        end
    else
        self._btnShare:setPositionX(self._btnShare:getParent():getContentSize().width / 2)
    end
end

function NewPlayTypeToBViewNew:onTouchEventClose(send, eventType)
    self:close()
end

function NewPlayTypeToBViewNew:onTouchEventShare(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_ACT_B_NEW_SHARE)
    self:startShare()
end

function NewPlayTypeToBViewNew:onTouchEventAutoCreate(send, eventType)
    local greetInfo = XH.lobby:getModule("NewPlayType"):getGreetInfo()
    if greetInfo and greetInfo.get_greet_prize_status then
        local nAutoFlag = 1
        if greetInfo.get_greet_prize_status == 1 then
            nAutoFlag = 0
        end
        XH.teaHouseManager:autoCreateNewPlayTypeMode(nAutoFlag, greetInfo.get_greet_prize_status)
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_ACT_B_NEW_CREAETE_MODE)
    self:close()
end

function NewPlayTypeToBViewNew:onTouchEventInterduce(send, eventType)
    XH.viewManager:openView("NewPlayTypeExtraRuleView", nil, GameNewTypeConfiger:getShowExtraRuleGameIDByLobbyID(XH.areaData:getLobbyID()))
    self:close()
end

function NewPlayTypeToBViewNew:onTouchEventRule(send, eventType)
    XH.viewManager:openView("NewPlayTypeActRuleView", nil, 3)
end

function NewPlayTypeToBViewNew:onTouchEventHistory(send, eventType)
    XH.viewManager:openView("NewPlayTypeAwardHistoryView")
end

function NewPlayTypeToBViewNew:getFisrtAward(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.lobby:getModule("NewPlayType"):reqGetFirstPrize()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_ACT_B_NEW_INVITE_OR_GET, {item_id = "1", label = "领奖"})
end

function NewPlayTypeToBViewNew:getEveryDayAward(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.lobby:getModule("NewPlayType"):reqGetPrize()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_ACT_B_NEW_INVITE_OR_GET, {item_id = "2", label = "领奖"})
end

function NewPlayTypeToBViewNew:onTouchEventInvite1(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_ACT_B_NEW_INVITE_OR_GET, {item_id = "1", label = "邀请玩家对局"})
    self:startShare()
end

function NewPlayTypeToBViewNew:onTouchEventInvite2(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_ACT_B_NEW_INVITE_OR_GET, {item_id = "2", label = "邀请玩家对局"})
    self:startShare()
end

function NewPlayTypeToBViewNew:startShare()
    local shareInfo = {
        textTitle = "浙江游戏大厅-丽水广式64张活动上新",
        url = "http://webactivity.yqyxcy.com/downloadview/index?id=323",
        type = "url",
        scene = "0",
        imageUrl = "com.xm.zjgamecenter",
        text = "广式64张/缺二色/吃牌可买马/手牌更顺，胡牌更快/免费下载",
        comment = "",
    }
    XH.sdkManager:startShare(shareInfo)
end

return NewPlayTypeToBViewNew g&  