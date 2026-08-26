local NewPlayTypeView = class("NewPlayTypeView", XH.ViewBase)
local GameNewTypeConfiger = require("app.Config.Parse.GameNewTypeConfiger")

local KW_BTN_TYPE = {
    EXPERIENCE = "EXPERIENCE",
    CLOSE = "CLOSE",
    NOTIFY = "NOTIFY",
}

local KW_PAGE_TYPE = {
    LOBBY = "P1063",
    TEA = "P1104"
}

local KW_BTN_IMG = {
    Known = "new_play_type_extra_rule_know.png",
    Notify = "new_play_type_extra_rule_notify_owner.png",
    Experience = "new_play_type_extra_rule_try.png"
}

function NewPlayTypeView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/NewPlayType/NewPlayTypeExtraRule.csb"
end

function NewPlayTypeView:getBindingInfo()
    return {
        ["_KW_PAGE_VIEW_RULE"] = { varName = "_pageView" },
        ["_KW_BTN_ARR_LEFT"] = { varName = "_btnLeft", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onArrLeftClicked" },
        ["_KW_BTN_ARR_RIGHT"] = { varName = "_btnRight", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onArrRightClicked" },
        ["_KW_BTN_DETAIL"] = { varName = "_btnDetail", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onDetailClicked" },
        ["_KW_BTN_GO"] = { varName = "_btnGo", type = XH.UI_TYPE.BUTTON },
        ["_KW_PANEL_ANI"] = { varName = "_leadAni" },
    }
end

function NewPlayTypeView:ctor(param)
    param = param or {}
    NewPlayTypeView.super.ctor(self, param)

    self._gameID = param or self:getShowExtraRuleGameID()
    self._ruleCnt = 0
    self._bExistNewTypePlayMode = false

    self:createViewItem()
    self:initUI()
    self._page = XH.teaHouseManager:isInTeaHouse() and "TEA" or "LOBBY"
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_EXTRA_RULE_PUSH, {page = KW_PAGE_TYPE[self._page]})
end

function NewPlayTypeView:initUI()
    self:initLeadAni()
    if not XH.teaHouseManager:isInTeaHouse() then
        self._btnGo:loadTexture(KW_BTN_IMG.Known, ccui.TextureResType.plistType)
        self._btnGo:addTouchEventListener(handler(self, self.onCloseClicked))
    else
        local teahouseData = XH.teaHouseManager:getTeaHouseData()
        if teahouseData then
            if teahouseData:getPlayModeInfoFlag() and teahouseData:getPlayModeInfosCount() > 0 then
                if teahouseData:isOwner() or self:checkNewPlaytypeMode() then
                    self._btnGo:loadTexture(KW_BTN_IMG.Known, ccui.TextureResType.plistType)
                    self._btnGo:addTouchEventListener(handler(self, self.onCloseClicked))
                else
                    self._btnGo:loadTexture(KW_BTN_IMG.Notify, ccui.TextureResType.plistType)
                    self._btnGo:addTouchEventListener(handler(self, self.onClickNotify))
                end
            elseif not teahouseData:needShowCreateTable() then
                if teahouseData:isOwner() or self:checkNewPlaytypeTables() then
                    self._btnGo:loadTexture(KW_BTN_IMG.Known, ccui.TextureResType.plistType)
                    self._btnGo:addTouchEventListener(handler(self, self.onCloseClicked))
                else
                    self._btnGo:loadTexture(KW_BTN_IMG.Notify, ccui.TextureResType.plistType)
                    self._btnGo:addTouchEventListener(handler(self, self.onClickNotify))
                end
            else
                self._btnGo:loadTexture(KW_BTN_IMG.Known, ccui.TextureResType.plistType)
                self._btnGo:addTouchEventListener(handler(self, self.onClickExperience))
            end
        else
            self._btnGo:addTouchEventListener(handler(self, self.onCloseClicked))
        end
    end
    self._btnGo:setVisible(true)
end

function NewPlayTypeView:initLeadAni()
    if not XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEW_TYPE_EXTRA_RULE_LEAD_CHANGE_PAGE, false) and self._ruleCnt > 1 then
        self._leadAni:setVisible(true)
    else
        self._leadAni:setVisible(false)
    end
end

function NewPlayTypeView:onClickExperience(send, eventType)
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    local teahouseManager = XH.teaHouseManager:getTeaHouseManager()
    if teahouseData then
        XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_CHOOSE_GAME_ID, self._gameID, { keyCacheInfo = "_teaHouse" })
        teahouseManager.viewManager:openView("TeaHouseCreateBoxRoomView", 0, {
            payModeType = teahouseData:getTeaHousePayMode(),
            payTypeTable = teahouseData:getPayTypeByID()
        } )
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_EXTRA_RULE_GO_CLICK, {label = KW_BTN_TYPE.EXPERIENCE, page = KW_PAGE_TYPE[self._page]})
    self:close()
end

function NewPlayTypeView:onClickNotify(send, eventType)
    XH.lobby:getModule("NewPlayType"):orderNewPlayTypeAndShare()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_EXTRA_RULE_GO_CLICK, {label = KW_BTN_TYPE.NOTIFY, page = KW_PAGE_TYPE[self._page]})
    self:close()
end

function NewPlayTypeView:onCloseClicked(send, eventType)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_EXTRA_RULE_GO_CLICK, {label = KW_BTN_TYPE.CLOSE, page = KW_PAGE_TYPE[self._page]})
    self:close()
end

function NewPlayTypeView:createViewItem()
    if not self._pageView then
        return
    end
    self._pageView:removeAllItems()
    local pageSize = self._pageView:getContentSize()
    local ruleImgs = GameNewTypeConfiger:getExtraRuleByGameID(self._gameID)
    self._ruleCnt = #ruleImgs
    for _, v in pairs(ruleImgs) do
        if cc.FileUtils:getInstance():isFileExist(v) then
            local tmpPage = ccui.ImageView:create(v, ccui.TextureResType.localType)
            tmpPage:setPosition(cc.p(pageSize.width/2, pageSize.height/2))
            tmpPage:setAnchorPoint(cc.p(0.5, 0.5))
            self._pageView:addPage(tmpPage)
        end
    end
    if self._ruleCnt <= 1 then
        self._btnLeft:setVisible(false)
        self._btnRight:setVisible(false)
        self._leadAni:setVisible(false)
    end
end

function NewPlayTypeView:onArrLeftClicked(send, eventType)
    local nowIndex = self._pageView:getCurrentPageIndex()
    nowIndex = nowIndex < 0 and 0 or nowIndex
    local showIndex = (nowIndex - 1) >= 0 and (nowIndex - 1) or (self._ruleCnt - 1)
    self._pageView:scrollToPage(showIndex)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_EXTRA_RULE_TURN_LEFT_CLICK, {page = KW_PAGE_TYPE[self._page]})
end

function NewPlayTypeView:onArrRightClicked(send, eventType)
    local nowIndex = self._pageView:getCurrentPageIndex()
    nowIndex = nowIndex < 0 and 0 or nowIndex
    local showIndex = (nowIndex + 1) < self._ruleCnt and (nowIndex + 1) or 0
    self._pageView:scrollToPage(showIndex)
    self._leadAni:setVisible(false)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_NEW_TYPE_EXTRA_RULE_LEAD_CHANGE_PAGE, true)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_EXTRA_RULE_TURN_RIGHT_CLICK, {page = KW_PAGE_TYPE[self._page]})
end

function NewPlayTypeView:onDetailClicked(send, eventType)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_CHOOSE_GAME_ID, self._gameID)
    XH.viewManager:openView("RuleView")
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_PLAY_TYPE_EXTRA_RULE_DETAIL_CLICK, {page = KW_PAGE_TYPE[self._page]})
end

function NewPlayTypeView:getShowExtraRuleGameID()
    return GameNewTypeConfiger:getShowExtraRuleGameIDByLobbyID(XH.areaData:getLobbyID())
end

function NewPlayTypeView:checkNewPlaytypeMode()
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    if not teahouseData then
        return
    end
    local GameNewTypeCfg = require("app.Config.GameNewTypeConfig").GameNewTypeConfByGameID
    if not GameNewTypeCfg then
        return
    end
    if teahouseData:getPlayModeInfoFlag() and teahouseData:getPlayModeInfosCount() > 0 then
        local playModeInfos = teahouseData:getPlayModeInfos()
        for _, playModeInfo in pairs(playModeInfos) do
            for _, v in pairs(GameNewTypeCfg) do
                if v.LobbyID == XH.areaData:getLobbyID() then
                    for _, newType in pairs(v.NewPlayTypeNode) do
                        local nodeInfo = loadstring("return " .. newType)()
                        if nodeInfo then
                            local nodeName = nodeInfo[1]
                            if string.find(nodeName, "playCount_") then
                                local playCount = XH.StringTool.getNumberSuffixByString(nodeName)
                                if XH.TableTool.isValueInTable(playCount, playModeInfo.anCount) then
                                    return true
                                end
                            elseif string.find(nodeName, "playerCount_") then
                                local playerCount = XH.StringTool.getNumberSuffixByString(nodeName)
                                if XH.TableTool.isValueInTable(playerCount, playModeInfo.anPlayerCount) then
                                    return true
                                end
                            elseif string.find(playModeInfo.acData, nodeName) then
                                return true
                            end
                        end
                    end
                end
            end
        end
    end
    return
end

function NewPlayTypeView:checkNewPlaytypeTables()
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    if not teahouseData then
        return
    end
    local GameNewTypeCfg = require("app.Config.GameNewTypeConfig").GameNewTypeConfByGameID
    if not GameNewTypeCfg then
        return
    end
    local tablesInfos = teahouseData:getTableInfos()
    if tablesInfos then
        for _, tableInfo in pairs(tablesInfos) do
            for _, v in pairs(GameNewTypeCfg) do
                if v.LobbyID == XH.areaData:getLobbyID() then
                    for _, newType in pairs(v.NewPlayTypeNode) do
                        local nodeInfo = loadstring("return " .. newType)()
                        if nodeInfo then
                            local nodeName = nodeInfo[1]
                            if string.find(nodeName, "playCount_") then
                                local playCount = XH.StringTool.getNumberSuffixByString(nodeName)
                                if tableInfo.count and playCount == tableInfo.count then
                                    return true
                                end
                            elseif string.find(nodeName, "playerCount_") then
                                local playerCount = XH.StringTool.getNumberSuffixByString(nodeName)
                                if tableInfo.size and playerCount == tableInfo.size then
                                    return true
                                end
                            elseif tableInfo.data and string.find(tableInfo.data, nodeName) then
                                return true
                            end
                        end
                    end
                end
            end
        end
    end
    return
end

return NewPlayTypeView,,  