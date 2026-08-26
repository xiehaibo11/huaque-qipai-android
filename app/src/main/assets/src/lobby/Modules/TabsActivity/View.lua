local ActivityView = class("ActivityView",XH.ViewBase)
local TabsActivityConfig = require("lobby.Modules.TabsActivity.Config")

local KW_TXT_ACT_NAME = "KW_TXT_ACT_NAME"
local KW_UI_ROOT_LAYER = "_KW_UI_ROOT_LAYER"
local KW_IMG_TXT = "_KW_IMG_TXT"
local KW_FLAG_CORNER = "_KW_FLAG_CORNER"
local KW_TXT_CORNER = "_KW_TXT_CORNER"

local KW_FIRST_TAB_CNT = 2
local KW_FIRST_TAB_TYPE = {
    NOTIFY = 0,
    ACTIVITY = 1,
}
local KW_FIRST_TAB_BG = {
    SELECTED = "activityTitle_select2.png",
    NOTSELECTED = "activityTitle_select1.png",
}
local KW_FIRST_TAB_TEXT = {
    [0] = {
        SELECTED = "activityTitle_gg1.png",
        NOTSELECTED = "activityTitle_gg2.png",
    },
    [1] = {
        SELECTED = "activityTitle_hd1.png",
        NOTSELECTED = "activityTitle_hd2.png",
    },
}
local UNIUN_GAME_NAME = {
    UNIUNGAME = "捕鱼送话费",
    REXUEUNIUNGAME = "复古三职业",
}

function ActivityView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/ActivityLayer.csb"
end

function ActivityView:getBindingInfo()
    return {
        ["_KW_BTN_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KW_BTN_AWARD_CENTER"] = {varName="_btnAwardCenter",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventAwardCenter"},
        ["_KW_BTN_ITEM"] = { varName = "_btnItem", type = XH.UI_TYPE.BUTTON },
        ["_KW_ACT_LIST_VIEW"] = { varName = "_actListView" },
        ["_KW_PANEL_ACT"] = { varName = "_actPanel" },
        ["_KW_BTN_ACTIVITY"] = { varName = "_btnActivity", onTouchEnded = "onTouchEventTitle" },
        ["_KW_BTN_NOTIFY"] = { varName = "_btnNotify", onTouchEnded = "onTouchEventTitle" },
        ["_KW_SCROLLVIEW_NOTIFY"] = {varName="_scrollviewNotify"},
        ["_KW_ANNOUNCE_TITLE"] = {varName="_announceTitle"},
        ["_KW_ANNOUNCE_CONTENT"] = {varName="_announceContent"},
    }
end

function ActivityView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_CHANGE_TAB", callBack = "onChangeTab" },
        { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_CLOSE_TAB_VIEW", callBack = "onCloseView" },
        { module = XH.lobby:getModule("Notify"), eventKeyName = "ENENT_NOTIFY_CHANGED_MSG", callBack = "onRefreshNotify" },
        { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_BTN_RED_POS", callBack = "flushBtnRedPos" },
        { module = XH.lobby:getModule("PersonalGiftPack"), eventKeyName = "EVENT_CONFIG_DATA_CHANGED", callBack = "onPersonalGiftPackInfo"},
        { module = XH.lobby:getModule("BackActivity"), eventKeyName = "KW_BACK_ACT_SHOW_ICON", callBack = "onBackActInfo" },
        { module = XH.lobby:getModule("LaborDayGift"), eventKeyName = "EVENT_FULSH_LABOR_DAY_ICON", callBack = "onEventShowLaborDay"},
    }
end

function ActivityView:ctor(param)
    param = param or {}
	ActivityView.super.ctor(self,param)
    self:initData(param)
end

function ActivityView:initData(param)
    self._webView = nil
    self._acts = { }
    self._firstTab = param.firstTab or KW_FIRST_TAB_TYPE.ACTIVITY
    self._defultActTab = self._firstTab == KW_FIRST_TAB_TYPE.ACTIVITY and param.tab or nil
    if not self._defultActTab then
        self._defultActTab = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_ACT_ICON_DEFULt, "")
    end
    self._defultNotifyTab = self._firstTab == KW_FIRST_TAB_TYPE.NOTIFY and param.tab or nil
    self:initFirstTab()
end

function ActivityView:dealSpecialTab()
    self:dealDailySignTab()
    self:dealPhoneBindTab()
    self:dealBackGiftTab()
    self:dealPhoneFeeTab()
    self:dealPersonalGiftTab()
    self:dealLaborDayGiftTab()
    self:dealFreeDrawGiftTab()
    self:dealUniunGameTab()
end

function ActivityView:dealDailySignTab()
    local bNewUser = XH.playerData:getIsDisplayNewUser()
    local lobbyID = XH.areaData:getLobbyID()
    if bNewUser and TabsActivityConfig.NewUserSignInActConfig and TabsActivityConfig.NewUserSignInActConfig[lobbyID] == 1 and XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_HAVE_PERMISSION, true) then
        bNewUser = true
    else
        bNewUser = false
    end
    for i = #self._acts, 1, -1 do
        if (not bNewUser and self._acts[i].icon_code == "NEWUSERSIGN") or (bNewUser and self._acts[i].icon_code == "DAILYSIGN") then
            table.remove(self._acts, i)
        elseif self._acts[i].icon_code == "DAILYSIGN" and XH.lobby:getModule("ToponAct"):getSignActId() == nil then
            -- 如果没有签到actid也移除
            table.remove(self._acts, i)
        end
    end
end

function ActivityView:dealPhoneBindTab()
    local bShow = XH.lobby:getModule("TabsActivity"):getPhoneActNeedShow()
    for i = #self._acts, 1, -1 do
        if not bShow and self._acts[i].icon_code == "PHONEBIND" then
            table.remove(self._acts, i)
        end
    end
end

function ActivityView:dealBackGiftTab()
    local bShow = XH.lobby:getModule("TabsActivity"):getShowBackAct()
    for i = #self._acts, 1, -1 do
        if not bShow and self._acts[i].icon_code == "BACKGIFT" then
            table.remove(self._acts, i)
        end
    end
end

function ActivityView:dealPhoneFeeTab()
    local bShow = XH.lobby:getModule("TabsActivity"):getShowPhoneFeeAct()
    for i = #self._acts, 1, -1 do
        if (not bShow or XH.playerData:isRechargePlayer()) and self._acts[i].icon_code == "TOPONPHONEFEE" then
            table.remove(self._acts, i)
        end
    end
end

function ActivityView:dealPersonalGiftTab()
    local bShow = XH.lobby:getModule("PopSystem"):getShowPersonalGiftAct()
    for i = #self._acts, 1, -1 do
        if not bShow and self._acts[i].icon_code == "PERSONALGIFT" then
            table.remove(self._acts, i)
        end
    end
end

function ActivityView:dealLaborDayGiftTab()
    local bShow = XH.lobby:getModule("TabsActivity"):getShowLaborDayGift()
    for i = #self._acts, 1, -1 do
        if not bShow and self._acts[i].icon_code == "LABORDAYGIFT" then
            table.remove(self._acts, i)
        end
    end
end

function ActivityView:dealFreeDrawGiftTab()
    local bShow = XH.lobby:getModule("TabsActivity"):getShowFreeDrawAct()
    for i = #self._acts, 1, -1 do
        if (not bShow or XH.playerData:isRechargePlayer()) and self._acts[i].icon_code == "FREEDRAW" then
            table.remove(self._acts, i)
        end
    end
end

function ActivityView:dealUniunGameTab()
    if device.platform ~= "android" or
     XH.ChannelTool.checkIsECarChannel() then
        for i = #self._acts, 1, -1 do
            if UNIUN_GAME_NAME[ self._acts[i].icon_code]then--代码优化
                table.remove(self._acts, i)
            end
            -- if  self._acts[i].icon_code == "UNIUNGAME" then
            --     table.remove(self._acts, i)
            -- end
            -- if  self._acts[i].icon_code == "REXUEUNIUNGAME" then
            --     table.remove(self._acts, i)
            -- end
        end
    else--安卓活动页签联运游戏曝光埋点
        for i = #self._acts, 1, -1 do
            if UNIUN_GAME_NAME[ self._acts[i].icon_code]then
                XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.UNIONGAME_PUSH,{label =  UNIUN_GAME_NAME[ self._acts[i].icon_code]})
            end
        end
    end
end

function ActivityView:initFirstTab()
    self:initTitleUI()
    self:updateDetailPanel()
end

function ActivityView:initTitleUI()
    if not self._btnActivity or not self._btnNotify then
        return
    end
    XH.UITool.ignoreContentAdaptWithSize(self._btnActivity, KW_IMG_TXT, true)
    XH.UITool.ignoreContentAdaptWithSize(self._btnNotify, KW_IMG_TXT, true)
    if self._firstTab == KW_FIRST_TAB_TYPE.ACTIVITY then
        self._btnActivity:loadTexture(KW_FIRST_TAB_BG.SELECTED, ccui.TextureResType.plistType)
        self._btnNotify:loadTexture(KW_FIRST_TAB_BG.NOTSELECTED, ccui.TextureResType.plistType)
        XH.UITool.loadTexture(self._btnActivity, KW_IMG_TXT, KW_FIRST_TAB_TEXT[KW_FIRST_TAB_TYPE.ACTIVITY].SELECTED, ccui.TextureResType.plistType)
        XH.UITool.loadTexture(self._btnNotify, KW_IMG_TXT, KW_FIRST_TAB_TEXT[KW_FIRST_TAB_TYPE.NOTIFY].NOTSELECTED, ccui.TextureResType.plistType)
        self._btnActivity:setTouchEnabled(false)
        self._btnNotify:setTouchEnabled(true)
    else
        self._btnActivity:loadTexture(KW_FIRST_TAB_BG.NOTSELECTED, ccui.TextureResType.plistType)
        self._btnNotify:loadTexture(KW_FIRST_TAB_BG.SELECTED, ccui.TextureResType.plistType)
        XH.UITool.loadTexture(self._btnActivity, KW_IMG_TXT, KW_FIRST_TAB_TEXT[KW_FIRST_TAB_TYPE.ACTIVITY].NOTSELECTED, ccui.TextureResType.plistType)
        XH.UITool.loadTexture(self._btnNotify, KW_IMG_TXT, KW_FIRST_TAB_TEXT[KW_FIRST_TAB_TYPE.NOTIFY].SELECTED, ccui.TextureResType.plistType)
        self._btnActivity:setTouchEnabled(true)
        self._btnNotify:setTouchEnabled(false)
    end
end

function ActivityView:updateDetailPanel()
    if self._firstTab == KW_FIRST_TAB_TYPE.ACTIVITY then
        self._scrollviewNotify:setVisible(false)
        if self._webView then
            self._webView:setVisible(false)
        end
        self._actPanel:setVisible(true)
        self._acts = clone(XH.lobby:getModule("TabsActivity"):getActsByLobbyID())
        self:dealSpecialTab()
        self:initUI(self._defultActTab)
    else
        self._actPanel:setVisible(false)
        self._scrollviewNotify:setVisible(true)
        XH.lobby:getModule("Notify"):reqNotifyInfo()
    end
    self:updateRedPoint()
end

function ActivityView:getTabTextImgName(title)
    if not title or title == "" then
        return nil
    end
    for _, nameConf in pairs(TabsActivityConfig.ActTabListNames) do
        if nameConf.NameStr == title then
            return nameConf.NameImg
        end
    end
    return nil
end

function ActivityView:setTabBtnText(newItem, text)
    XH.UITool.setText(newItem, KW_TXT_ACT_NAME, text)
    local nameImg = self:getTabTextImgName(text)
    if nameImg and nameImg ~= "" then
        cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/hall/Image/Activity/activityTitle.plist")
        XH.UITool.ignoreContentAdaptWithSize(newItem,"KW_IMG_ACT_NAME",true)
        XH.UITool.loadTexture(newItem, "KW_IMG_ACT_NAME", nameImg, ccui.TextureResType.plistType)
        XH.UITool.setVisible(newItem, "KW_IMG_ACT_NAME", true)
        XH.UITool.setVisible(newItem, KW_TXT_ACT_NAME, false)
    else
        XH.UITool.setVisible(newItem, KW_TXT_ACT_NAME, true)
        XH.UITool.setVisible(newItem, "KW_IMG_ACT_NAME", false)
    end
end

function ActivityView:initUI(selectTab)
    if not self._actListView or not self._btnItem then return end
    self._actListView:removeAllItems()
    self._actListView:setScrollBarEnabled(false)
    self._actListView:setItemModel(self._btnItem:clone())
    self._actListView:setBounceEnabled(false)
    local defultBtn
    local index
    if selectTab and selectTab == "DAILYSIGN" then
        local lobbyID = XH.areaData:getLobbyID()
        if XH.playerData:getIsDisplayNewUser() and TabsActivityConfig.NewUserSignInActConfig and TabsActivityConfig.NewUserSignInActConfig[lobbyID] == 1 and XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_HAVE_PERMISSION, true) then
            selectTab = "NEWUSERSIGN"
        end
    end
    for i = 1, #(self._acts) do
        self._actListView:pushBackDefaultItem()
        local items = self._actListView:getItems()
        local newItem = items[#items]
        newItem:setName(self._acts[i].icon_code)
        self:setTabBtnText(newItem, self._acts[i].icon_name)
        if self._acts[i].tag_name and self._acts[i].tag_name ~= "" then
            XH.UITool.setText(newItem, KW_TXT_CORNER, self._acts[i].tag_name)
            XH.UITool.setVisible(newItem, KW_FLAG_CORNER, true)
        else
            XH.UITool.setVisible(newItem, KW_FLAG_CORNER, false)
        end
        newItem:addTouchEventListener(handler(self, self.onTouchEvnetTab))
        newItem:setVisible(true)
        if not selectTab or selectTab == "" or selectTab == self._acts[i].icon_code then
            defultBtn = newItem
            index = self._actListView:getIndex(newItem)
            selectTab = self._acts[i].icon_code
        end
        if not defultBtn and self._acts[i].icon_code == "TASKSYS" then
            defultBtn = newItem
            index = self._actListView:getIndex(newItem)
        end
    end
    if defultBtn and index then
        self:onTouchEvnetTab(defultBtn, ccui.TouchEventType.ended)
        self._actListView:jumpToItem(index, cc.p(0, 0), cc.p(0, 0))
    else
        local items = self._actListView:getItems()
        if #items > 0 then
            defultBtn = items[1]
            index = self._actListView:getIndex(items[1])
            self:onTouchEvnetTab(defultBtn, ccui.TouchEventType.ended)
            self._actListView:jumpToItem(index, cc.p(0, 0), cc.p(0, 0))
        end
    end
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.TABS_ACTIVITY_OPEN)
end

function ActivityView:onRefreshNotify(event)
    if event.data.type == XH.Req.TYPE.SUCCESS then
        local notifyList = XH.lobby:getModule("Notify"):getNotifyList()
        if next(notifyList) then
            local function cmpFunc(a, b)
                return a.weight < b.weight
            end
            table.sort(notifyList, cmpFunc)
        end
        self:showAnnounceContant(notifyList)
    end
end


function ActivityView:showAnnounceContant(notifyList)
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_ACTIVITY_ANNOUNCE)
    if not self._actListView or not self._btnItem then return end
    self._actListView:removeAllItems()
    self._actListView:setScrollBarEnabled(false)
    self._actListView:setBounceEnabled(false)
    local lobbyId=XH.areaData:getLobbyID()
    for i = 1, #notifyList do
        if notifyList[i].page_url then
            if device.platform == "windows" then
                break
            end
            self:initWebView()
            self._webView:setVisible(false)
        end
    end
    for i, data in ipairs(notifyList) do
        local btnTab = self._btnItem:clone()
        self:setTabBtnText(btnTab, tostring(data.title))
        self._actListView:pushBackCustomItem(btnTab)
        btnTab:setVisible(true)
        btnTab:setTag(i)    
        btnTab:setName("_KW_BTN_ITEM_OFF"..i)
        local IsNeedRed =self:GetIsRed("_KW_BTN_ITEM_OFF"..i..lobbyId)
        if IsNeedRed ==false then
            XH.lobby:getModule("TabsActivity"):showIconRedPoint("_KW_BTN_ITEM_OFF"..i,true)
        end
        btnTab:addTouchEventListener(handler(self,self.onTouchChangeNotifyTab))
    end
    local childrens = self._actListView:getChildren()
    self:onTouchChangeNotifyTab(childrens[1], ccui.TouchEventType.ended)
end

function ActivityView:GetIsRed(name)
    local isneedRed =XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_ANNOUNCE, false,{key = name})
    return isneedRed
end

function ActivityView:showAnnounceContantSelect(notify)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.tx24060401, {block_label = notify.title or ""})
    -- 有WebView的页面，隐藏awardCenter按钮
    self._btnAwardCenter:setVisible(true)
    if notify.page_url then
        self._btnAwardCenter:setVisible(false)
    end
    if notify.sub_title then
        self._announceTitle:setString(notify.sub_title)
    end
    if notify.content then
        self._announceContent:setString(notify.content)
    end

    if self._webView  then
        if notify.page_url then
            self._webView:loadURL(notify.page_url)
            self._webView:setVisible(true)
            self._announceTitle:setString("")
            self._announceContent:setString("")
        else
            self._webView:setVisible(false)
        end
    end
end

function ActivityView:initWebView()
    if self._webView == nil then
        local webView = ccexp.WebView:create()
        webView:setContentSize(self._scrollviewNotify:getContentSize())
        webView:setScalesPageToFit(false)
        webView:setPosition(0, self._scrollviewNotify:getContentSize().height)
        webView:setAnchorPoint(cc.p(0, 1))
        webView:setBounces(false)
        webView:setVisible(false)
        self._scrollviewNotify:addChild(webView)
        self._webView = webView
        webView:setOnShouldStartLoading(function(sender,url)
            self._webView:setVisible(false)
            return true
        end)
        webView:setOnDidFinishLoading(function(sender,url)
            self._webView:setVisible(true)  
        end)
    end
end

function ActivityView:updateRedPoint()
    local activityRed = XH.lobby:getModule("TabsActivity"):getHuoDongRedShow()
    for activity, isShowRed in pairs(activityRed) do
        if isShowRed then
            self:showBtnRedPos(activity, isShowRed)
        end
    end
end

-- 刷新小红点
function ActivityView:flushBtnRedPos(msg)
    if msg then
        local activity = msg.data.activity
        local showRed = msg.data.showRed
        if self.showBtnRedPos then
            self:showBtnRedPos(activity, showRed)
        end
    end
end

function ActivityView:showBtnRedPos(activity, showRed)
    local items = self._actListView:getItems()
    for _, v in pairs(items) do
        if v:getName() == activity then
            if showRed then
                XH.UITool.setVisible(v, "KW_RED_POS", true)
            else
                XH.UITool.setVisible(v, "KW_RED_POS", false)
            end
            break
        end
    end
end

-- 将某个页签插入列表
function ActivityView:insertTabToListView(tab)
    if not tab or self._firstTab ~= KW_FIRST_TAB_TYPE.ACTIVITY then
        return
    end
    local tmpActs = clone(XH.lobby:getModule("TabsActivity"):getActsByLobbyID())
    local items = self._actListView:getItems()
    for _, v in pairs(items) do
        -- 已经存在 直接返回
        if v:getName() == tab then
            return
        end
    end
    for i = 1, #tmpActs do
        if tmpActs[i].icon_code == tab then
            local insertPos = (i - 1) >= #items and (#items - 1) or (i - 1)
            insertPos = insertPos < 0 and 0 or insertPos
            self._actListView:insertDefaultItem(insertPos)
            local insertItem = self._actListView:getItem(insertPos)
            if insertItem then
                insertItem:setName(tab)
                self:setTabBtnText(insertItem, tmpActs[i].icon_name)
                insertItem:addTouchEventListener(handler(self, self.onTouchEvnetTab))
                insertItem:setVisible(true)
                if self._defultActTab == tab then
                    self:onTouchEvnetTab(insertItem, ccui.TouchEventType.ended)
                end
            end
            break
        end
    end
end

-- 删除某个页签
function ActivityView:delTabToListView(tab)
    if not tab or self._firstTab ~= KW_FIRST_TAB_TYPE.ACTIVITY then
        return
    end
    local items = self._actListView:getItems()
    for _, v in pairs(items) do
        if v:getName() == tab then
            local index = self._actListView:getIndex(v)
            self._actListView:removeItem(index)
            if self._defultActTab == tab then
                self._defultActTab = nil
                self:onTouchEvnetTab(items[1], ccui.TouchEventType.ended)
            end
            break
        end
    end
end

-- 复原活动展示主界面
function ActivityView:restoreActPanel()
    if not self._actPanel then
        return
    end
    local view = self._actPanel:getChildren()[1]
    if view and view.close then
        view:close()
    end
    self._actPanel:removeAllChildren()
end

-- 刷新页签状态
function ActivityView:updateTabStatus(selectedTab)
    if not selectedTab or not self._actListView then
        return
    end
    local items = self._actListView:getItems()
    for _, v in pairs(items) do
        v:setEnabled(v ~= selectedTab)
        XH.UITool.setTextColor(v, KW_TXT_ACT_NAME, v == selectedTab and cc.c3b(255, 255, 255) or cc.c3b(191, 108, 29))
        XH.UITool.setColor(v, "KW_IMG_ACT_NAME", v == selectedTab and cc.c3b(255, 255, 255) or cc.c3b(191, 108, 29))
    end
end

function ActivityView:adaptActSize(node)
    node:setAnchorPoint(0.5, 0.5)
    local panelSize = self._actPanel:getContentSize()
    local nodeSize = XH.UITool.getContentSize(node, KW_UI_ROOT_LAYER) or node:getContentSize()
    node:setPosition(panelSize.width / 2, panelSize.height / 2)
    if nodeSize.width > panelSize.width or nodeSize.height > panelSize.height then
        local scaleTmp = math.min(panelSize.width / nodeSize.width, panelSize.height / nodeSize.height)
        node:setScale(scaleTmp)
    end
    self._actPanel:addChild(node)
end

function ActivityView:onChangeTab(event)
    if not event or not event.data then
        return
    end
    local tabTmp = event.data
    local items = self._actListView:getItems()
    for _, v in pairs(items) do
        if tabTmp == v:getName() then
            self:onTouchEvnetTab(v, ccui.TouchEventType.ended)
            break
        end
    end
end

function ActivityView:onCloseView(event)
    self:onTouchEventClose(self._btnClose, ccui.TouchEventType.ended)
end

function ActivityView:onPersonalGiftPackInfo(event)
    local confData = event.msg.configData or {}
    local bShow = #confData > 0
    XH.lobby:getModule("PopSystem"):setShowPersonalGiftAct(#confData > 0)
    if bShow then
        self:insertTabToListView("PERSONALGIFT")
    else
        self:delTabToListView("PERSONALGIFT")
    end
end

function ActivityView:onBackActInfo(event)
    local data = event.data
    if data.actStartTime and data.actStartTime > 0 then
        XH.lobby:getModule("TabsActivity"):setShowBackAct(true)
        self:insertTabToListView("BACKGIFT")
    else
        XH.lobby:getModule("TabsActivity"):setShowBackAct(false)
    end
end

function ActivityView:onEventShowLaborDay(event)
    if not event or not event.data then
        return
    end
    if event.data.isShow then
        self:insertTabToListView("LABORDAYGIFT")
    end
end

function ActivityView:onTouchEvnetTab(send, event)
    if event ~= ccui.TouchEventType.ended then
        return
    end
    self:restoreActPanel()
    self:updateTabStatus(send)
    local tabName = send:getName()
    if self._firstTab == KW_FIRST_TAB_TYPE.ACTIVITY then
        local viewTmp = XH.lobby:getModule("TabsActivity"):getActsViewPathByKey(tabName)
        if viewTmp then
            local node = require(viewTmp.path).new()
            self:adaptActSize(node)
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.ACTIVITYLAYER_ACT_PUSH,{label = tabName, block_label = tabName, item_id = tabName})
        end
        self._defultActTab = tabName
        -- 有WebView的页面，隐藏awardCenter按钮
        self._btnAwardCenter:setVisible(true)
        if self._defultActTab == "FANZHA" then
            self._btnAwardCenter:setVisible(false)
        end
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_ACT_ICON_DEFULt, self._defultActTab)
    else
        XH.lobby:getModule("Notify"):reqNotifyInfo()
        self._defultNotifyTab = tabName
    end
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.TABS_ACTIVITY_TAB,{tabName = tabName})
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.TABS_ACTIVITY_TAB,{label = tabName})
end

function ActivityView:onTouchEventAct(view, send, event)
    if event ~= ccui.TouchEventType.ended then
        return
    end
    if not view or not view.viewName then
        return
    end
    XH.viewManager:openView(view.viewName)
end

function ActivityView:onTouchEventAwardCenter(send, event)
    XH.viewManager:openView("AwardCenterView")
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.TABS_ACTIVITY_AWARD_CENTER)
end

function ActivityView:onTouchEventClose(send, event)
    XH.lobby:getModule("H5GameWidget"):close5GameWidget("activity")
    self:restoreActPanel()
    self:close()
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.TABS_ACTIVITY_CLOSE)
end

function ActivityView:onTouchEventTitle(send, event)
    if self._firstTab == KW_FIRST_TAB_TYPE.ACTIVITY and send == self._btnActivity then
        return
    end
    if self._firstTab == KW_FIRST_TAB_TYPE.NOTIFY and send == self._btnNotify then
        return
    end
    self:restoreActPanel()
    self._firstTab = math.fmod(self._firstTab + 1, KW_FIRST_TAB_CNT)
    self:initFirstTab()
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.TABS_ACTIVITY_FIRST_TAB,{tabType = self._firstTab})
end

function ActivityView:onTouchChangeNotifyTab(send, event)
    if event ~= ccui.TouchEventType.ended then
        return
    end
    if not send then 
        return
    end
    local curSelect = send:getTag()
    local lobbyId=XH.areaData:getLobbyID()
    local name = send:getName()..lobbyId
    self:updateTabStatus(send)
    local notifyList = XH.lobby:getModule("Notify"):getNotifyList()
    self:showAnnounceContantSelect(notifyList[curSelect])
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_ANNOUNCE, true, {key = name})
    XH.lobby:getModule("TabsActivity"):showIconRedPoint("_KW_BTN_ITEM_OFF"..curSelect,false)
end

return ActivityView`e  