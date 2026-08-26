local NewPlayTypeAutoCreateTipsView = class("NewPlayTypeAutoCreateTipsView", XH.ViewBase)
local GameNewTypeConfiger = require("app.Config.Parse.GameNewTypeConfiger")

local KW_CONTENT_LINES = 4
local KW_MAX_BOOKERS = 4
local KW_CONTENT_STR = {
    [1] = {
        "新玩法见面礼",
        "创建【%s】固定玩法",
        "领取%d房卡奖励",
        "房卡奖励领取后，该固定玩法会保留3天",
    },
    [2] = {
        "玩家请求添加",
        "【%s】固定玩法",
        "还可领取%d房卡奖励",
        "房卡奖励领取后，该固定玩法会保留3天",
    },
    [3] = {
        "玩家请求添加",
        "【%s】固定玩法",
        "推广此玩法消耗可提升2.5倍",
        "",
    },
    [4] = {
        "新玩法见面礼",
        "创建【%s】固定玩法",
        "领取%d房卡奖励",
        "当前亲友圈固定玩法已满",
    },
}

function NewPlayTypeAutoCreateTipsView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/NewPlayType/NewPlayTypeAutoCreateTips.csb"
end

function NewPlayTypeAutoCreateTipsView:getBindingInfo()
    return {
        ["_KW_PANEL_COVER"] = { varName = "_panelCover" },
        ["_KW_BTN_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KW_PANEL_BOOK"] = { varName = "_panelBook" },
        ["_KW_PANEL_WITH_BOOKER_POS"] = { varName = "_withBookerPos" },
        ["_KW_PANEL_WITH_OUT_BOOKER_POS"] = { varName = "_noBookerPos" },
        ["_KW_PANEL_CONTENTS"] = { varName = "_panelContent" },
        ["_KW_TXT_CONTENT_"] = { varName = "_content", beginIndex = 1, endIndex = 4 },
        ["_KW_BTN_GIVE_UP"] = { varName = "_btnGiveUp", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventGiveUp" },
        ["_KW_BTN_GET"] = { varName = "_btnGetAward", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventGetAward" },
        ["_KW_BTN_CREATE_MODE"] = { varName = "_btnCreateMode", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventCreateMode" },
        ["_KW_BTN_CHARGE_MODE"] = { varName = "_btnChargeMode", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventChargeMode" },
        ["_KW_BOOK_PLAYER_ITEM"] = { varName = "_bookerItem" },

    }
end

function NewPlayTypeAutoCreateTipsView:ctor(param)
    param = param or {}
    NewPlayTypeAutoCreateTipsView.super.ctor(self, param)

    self._type = param.tType
    self._showCover = param.bShowCover
    self:initUI()
end

function NewPlayTypeAutoCreateTipsView:initUI()
    if self._type == XH.lobby:getModule("NewPlayType").AUTO_CREATE_TIPS_TYPE.NOTGETNOBOOK then
        self:initNotGetNoBookUI()
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_AUTO_CREATETIPS_PUSH_1)
    elseif self._type == XH.lobby:getModule("NewPlayType").AUTO_CREATE_TIPS_TYPE.NOTGETANDBOOK then
        self:initNotGetAndBookUI()
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_AUTO_CREATETIPS_PUSH_2)
    elseif self._type == XH.lobby:getModule("NewPlayType").AUTO_CREATE_TIPS_TYPE.GETEDAWARDWITHOUTMODE then
        self:initGetAwardWithoutModeUI()
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_AUTO_CREATETIPS_PUSH_3)
    elseif self._type == XH.lobby:getModule("NewPlayType").AUTO_CREATE_TIPS_TYPE.MODECOUNTMAX then
        self:initModeCountMaxUI()
    end
end

function NewPlayTypeAutoCreateTipsView:initNotGetNoBookUI()
    self:initContentByType()
    if self._panelContent and self._noBookerPos then
        self._panelContent:setPosition(self._noBookerPos:getPosition())
    end
    if self._btnGiveUp and self._btnGetAward then
        self._btnGiveUp:setVisible(true)
        self._btnGetAward:setVisible(true)
    end
    if self._panelCover then
        self._panelCover:setVisible(self._showCover or false)
    end
end

function NewPlayTypeAutoCreateTipsView:initNotGetAndBookUI()
    self:initContentByType()
    if self._panelContent and self._withBookerPos then
        self._panelContent:setPosition(self._withBookerPos:getPosition())
    end
    if self._btnGiveUp and self._btnGetAward then
        self._btnGiveUp:setVisible(true)
        self._btnGetAward:setVisible(true)
    end
    self:initBookerUI()
    if self._panelCover then
        self._panelCover:setVisible(self._showCover or false)
    end
end

function NewPlayTypeAutoCreateTipsView:initGetAwardWithoutModeUI()
    self:initContentByType()
    if self._btnCreateMode then
        self._btnCreateMode:setVisible(true)
    end
    local greetInfo = XH.lobby:getModule("NewPlayType"):getGreetInfo()
    if greetInfo and greetInfo.user_info and next(greetInfo.user_info) then
        if self._panelContent and self._withBookerPos then
            self._panelContent:setPosition(self._withBookerPos:getPosition())
        end
        self:initBookerUI()
    else
        if self._panelContent and self._noBookerPos then
            self._panelContent:setPosition(self._noBookerPos:getPosition())
        end
    end
end

function NewPlayTypeAutoCreateTipsView:initModeCountMaxUI()
    self:initContentByType()
    if self._btnChargeMode then
        self._btnChargeMode:setVisible(true)
    end
end

function NewPlayTypeAutoCreateTipsView:onTouchEventClose(send, eventType)
    self:close()
end

function NewPlayTypeAutoCreateTipsView:initContentByType()
    if not self._type or not KW_CONTENT_STR[self._type] then
        return
    end
    for i = 1, KW_CONTENT_LINES do
        if self["_content" .. i] then
            self["_content" .. i]:setString(XH.StringTool.replaceMatchStr(KW_CONTENT_STR[self._type][i]))
        end
    end
    local greetInfo = XH.lobby:getModule("NewPlayType"):getGreetInfo()
    if greetInfo and greetInfo.greet_prize then
        local str = self["_content" .. 3]:getString()
        self["_content" .. 3]:setString(string.format(str, greetInfo.greet_prize))
    end
    local autoPlayModeInfos = GameNewTypeConfiger:getAutoCreatePlayModeInfoByLobbyID(XH.areaData:getLobbyID())
    if autoPlayModeInfos and autoPlayModeInfos.gameName then
        local str = self["_content" .. 2]:getString()
        self["_content" .. 2]:setString(string.format(str, autoPlayModeInfos.gameName))
    end
end

function NewPlayTypeAutoCreateTipsView:initBookerUI()
    if not self._bookerItem or not self._panelBook then
        return
    end
    local greetInfo = XH.lobby:getModule("NewPlayType"):getGreetInfo()
    if greetInfo and greetInfo.user_info and next(greetInfo.user_info) then
        self._panelBook:removeAllChildren()
        local realCnt = math.min(#greetInfo.user_info, KW_MAX_BOOKERS)
        for i = 1, realCnt do
            local headPanel = self._bookerItem:clone()
            XH.UITool.setText(headPanel, "KW_TXT_NAME", XH.StringTool.cutStringByLength(clone(greetInfo.user_info[i].nickname), 8))
            self:updateHeadImg(ccui.Helper:seekWidgetByName(headPanel, "KW_IMG_HEAD"), greetInfo.user_info[i].head_img)
            self._panelBook:addChild(headPanel)
            headPanel:setPositionX((self._panelBook:getContentSize().width / (realCnt + 1)) * i)
            headPanel:setPositionY(self._panelBook:getContentSize().height / 2)
            headPanel:setVisible(true)
        end
        self._panelBook:setVisible(true)
    end
end

function NewPlayTypeAutoCreateTipsView:updateHeadImg(node, url)
    if not node or not url or url == "" then
        return 
    end

    if not node:getChildByName("KW_HEAD") then
        local size = node:getContentSize()
        XH.RemoteImage.new()
            :setTouchEnabled(false)
            :setPosition(size.width/2, size.height/2)
            :ignoreContentAdaptWithSize(false)
            :setContentSize(size.width, size.height)
            :setName("KW_HEAD")
            :addTo(node)
    end
    node:getChildByName("KW_HEAD"):setUrl(url, true)
end

function NewPlayTypeAutoCreateTipsView:onTouchEventGiveUp(send, eventType)
    if self._type == XH.lobby:getModule("NewPlayType").AUTO_CREATE_TIPS_TYPE.NOTGETNOBOOK then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_AUTO_CREATETIPS_GIVEUP_1)
    elseif self._type == XH.lobby:getModule("NewPlayType").AUTO_CREATE_TIPS_TYPE.NOTGETANDBOOK then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_AUTO_CREATETIPS_GIVEUP_2)
    end
    self:close()
end

function NewPlayTypeAutoCreateTipsView:onTouchEventGetAward(send, eventType)
    XH.lobby:getModule("NewPlayType"):getGreetPrize()
    if self._type == XH.lobby:getModule("NewPlayType").AUTO_CREATE_TIPS_TYPE.NOTGETNOBOOK then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_AUTO_CREATETIPS_GETAWARD_1)
    elseif self._type == XH.lobby:getModule("NewPlayType").AUTO_CREATE_TIPS_TYPE.NOTGETANDBOOK then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_AUTO_CREATETIPS_GETAWARD_2)
    end
    self:close()
end

function NewPlayTypeAutoCreateTipsView:onTouchEventCreateMode(send, eventType)
    XH.teaHouseManager:autoCreateNewPlayTypeMode(0, 1)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_AUTO_CREATETIPS_CREATE_MDOE)
    self:close()
end

function NewPlayTypeAutoCreateTipsView:onTouchEventChargeMode(send, eventType)
    self:openTeaHouseSetupView()
    self:close()
end

function NewPlayTypeAutoCreateTipsView:openTeaHouseSetupView()
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    local configManager = XH.teaHouseManager:getConfigManager()
    if teahouseData and configManager then
        local SetupConfig = configManager.SetupConfig
        local viewManager = XH.teaHouseManager:getViewManager()
        if SetupConfig and viewManager then
            if SetupConfig.IsOpenNewTeahouseSetupView then
                viewManager:openView("TeaHouseSetupViewNew", 0, {
                    nTeaNumber = teahouseData:getTeaNumber(),
                    szData = teahouseData:getTeaHouseNameExt(),
                    isOwner = teahouseData:isOwner(),
                    momentLevel = teahouseData:getTeaHouseLevel(),
                    useBuyedProp = teahouseData:getUseBuyedProp(),
                    selectTag = 2
                })
            else
                viewManager:openView("TeaHouseSetupView", 0, {
                    nTeaNumber = teahouseData:getTeaNumber(),
                    szData = teahouseData:getTeaHouseNameExt(),
                    isOwner = teahouseData:isOwner(),
                    momentLevel = teahouseData:getTeaHouseLevel(),
                    selectTag = 2
                })
            end
        end
    end
end

return NewPlayTypeAutoCreateTipsView   �*  