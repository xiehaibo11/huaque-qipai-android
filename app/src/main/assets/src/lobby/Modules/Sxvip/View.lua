local SxvipView = class("SxvipView", XH.ViewBase)

local SxvipConfig = require("lobby.Modules.Sxvip.Config")

function SxvipView:getCSBPath()
    return "cocosStudio/hall/CSB/Sxvip/SxvipLayer.csb"
end

function SxvipView:getBindingInfo()
    return {
        ["_KW_BTN_CLOSE"] = { varName = "KW_BTN_CLOSE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "ON_KW_BTN_CLOSE"},
        ["_KW_LIST_PRODUCT_TYPE"] = { varName = "_menuList"},
        ["_KW_LIST_RIGHT"] = { varName = "_contentList"},
        ["_KW_BTN_TYPE_MOD"] = { varName = "_menuMod"},
        ["_KW_PANEL_MENU"] = {varName = "_menuPanel"},
        ["_KW_BTN_PAY"] = { varName = "KW_BTN_PAY", type = XH.UI_TYPE.BUTTON, onTouchEnded = "ON_KW_BTN_PAY"},
        ["_KW_BTN_OPEN"] = { varName = "KW_BTN_OPEN", type = XH.UI_TYPE.BUTTON, onTouchEnded = "ON_KW_BTN_OPEN"},
        ["_KW_BTN_GIVE"] = { varName = "KW_BTN_GIVE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "ON_KW_BTN_GIVE"},
    }
end

function SxvipView:getProxyEvents()
    return {
       { module = XH.lobby:getModule("Sxvip"), eventKeyName = "SXVIP_EVENT_CONFIG_SUCC", callBack = "onConfigSucc" },
       { module = XH.lobby:getModule("Sxvip"), eventKeyName = "SXVIP_EVENT_REMAINDER", callBack = "onRemainder" },
       { module = XH.lobby:getModule("Sxvip"), eventKeyName = "SXVIP_EVENT_MESSAGE_COUNT", callBack = "onUpdateMessage" },
    }
end

function SxvipView:ctor(params)
    SxvipView.super.ctor(self)
    self._defalutMenu = params and params.defalutMenu 
    if not self._defalutMenu then
        self._defalutMenu = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_SXVIP_DEFALUT_VIEW, SxvipConfig.MENU_DEFINE.VIP_PRIVILEGE , "Sxvip")
    end
    -- 进入会员界面后，重新请求vip状态
    XH.NewThrowDataManager:throwData(string.format('zyt202403280%s', self._defalutMenu + 1))
    XH.lobby:getModule("Sxvip"):reqConfig()
    XH.lobby:getModule("Sxvip"):reqRemainder()
    XH.lobby:getModule("Sxvip"):reqVipRedPointCount()
    self:initView()
    self:updateAllView()
end

function SxvipView:initView()
    self:initMenuList()
    self:initContentList()
    self:initTop()
end

function SxvipView:initTop()
    self._menuPanel:setVisible(false)
end

function SxvipView:updateAllView()
    self:updateMenu()
    self:updateContent()
    self:updateTop()
end

function SxvipView:onConfigSucc()
    self:updateMenu()
    self:updateTop()
    self:refreshSubPrivilegeView()
end

function SxvipView:refreshSubPrivilegeView()
    local privilegeItems = self._contentList:getItems()
    if privilegeItems and privilegeItems[1] and privilegeItems[1].customData and privilegeItems[1].customData.value == SxvipConfig.MENU_DEFINE.VIP_PRIVILEGE then
        privilegeItems[1]:refreshView()
    end
end

function SxvipView:onRemainder()
    if not XH.lobby:getModule("Sxvip"):isExpire() then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.zyt2024032807)
    end
    self:updateMenu()
    self:updateTop()
end

function SxvipView:updateTop()
    self._menuPanel:setVisible(true)
    local expireTime = XH.playerData:getSxvipRemainder()
    local openPanel = self._menuPanel:getChildByName("_KW_PANEL_OPEN")
    local closePanel = self._menuPanel:getChildByName("_KW_PANEL_CLOSE")
    local privilegeNums = XH.lobby:getModule("Sxvip"):getPrivilegeNum()
    if XH.lobby:getModule("Sxvip"):isExpire() then
        openPanel:setVisible(false)
        closePanel:setVisible(true)
        closePanel:getChildByName("_KW_TEXT_TIPS_1"):setString(privilegeNums .."个特权")
    else
        openPanel:setVisible(true)
        closePanel:setVisible(false)
        local dateTable = os.date("*t", expireTime)
        local formattedDate = string.format("%d年%d月%d日 %02d:%02d", dateTable.year, dateTable.month, dateTable.day, dateTable.hour, dateTable.min)
        openPanel:getChildByName("_KW_TEXT_TIPS_1"):setString(privilegeNums .."个特权")
        openPanel:getChildByName("_KW_TEXT_TIPS_4"):setString(formattedDate)
    end
    self:onUpdateMessage()
end

function SxvipView:getMenuData()
    for i = 1, #SxvipConfig.MENU do
        if self._defalutMenu == SxvipConfig.MENU[i].value then
            return SxvipConfig.MENU[i]
        end
    end
end

function SxvipView:updateContent()
    local data = self:getMenuData()
    if data then
        self._contentList:removeAllItems()
        local item = require(data.item_path).new()
        item.customData = data
        item:refreshView()
        self._contentList:pushBackCustomItem(item)
    end
end

function SxvipView:updateMenu()
    local items = self._menuList:getItems()
    for i = 1, #items do
        local item = items[i]
        local selectImg = item:getChildByName("KW_IMG_CHOSE_STATE")
        if item.customData and item.customData.value == self._defalutMenu  then
            selectImg:setVisible(true)
        else
            selectImg:setVisible(false)
        end
    end
end

function SxvipView:initContentList()
    self._contentList:setScrollBarEnabled(false)
    self._contentList:removeAllItems()
end

function SxvipView:initMenuList()
    self._menuList:setScrollBarEnabled(false)
    self._menuList:removeAllItems()
    for i = 1, #SxvipConfig.MENU do
        local menu = self._menuMod:clone()
        self:initMenu(menu , SxvipConfig.MENU[i])
        menu:setName("menu_" .. i)
        self._menuList:pushBackCustomItem(menu)
    end
end

function SxvipView:initMenu(menuItem , menuConfig)
    -- menu名字
    local menuImg = menuItem:getChildByName("KW_IMG_PRODUCT_NAME")
    menuImg:loadTexture(menuConfig.txt_normal_img, ccui.TextureResType.plistType)
    -- 事件
    menuItem:addTouchEventListener(handler(self, self.onMenuClick))
    -- 缓存数据
    menuItem.customData = menuConfig
end

function SxvipView:onMenuClick(sender , eventType)
    local customData = sender.customData
    if customData then
        if self._defalutMenu == customData.value then
            return
        end
        self._defalutMenu = customData.value
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_SXVIP_DEFALUT_VIEW, self._defalutMenu , "Sxvip")
        local throwDataStr = string.format('zyt202403280%s', customData.value + 1)
        XH.NewThrowDataManager:throwData(throwDataStr)
        self:updateMenu()
        self:updateContent()
    end
end

function SxvipView:ON_KW_BTN_CLOSE(sender , eventType)
    self._contentList:removeAllItems()
    self:close()
end

function SxvipView:ON_KW_BTN_PAY(sender , eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.zyt2024032808)
    if self._defalutMenu == SxvipConfig.MENU_DEFINE.BUY_VIP then
        XH.TipTool.showToast("请选择会员类型进行续费！")
        return
    end
    self._defalutMenu = SxvipConfig.MENU_DEFINE.BUY_VIP
    self:updateAllView()
end

function SxvipView:ON_KW_BTN_OPEN(sender , eventType)
    if self._defalutMenu == SxvipConfig.MENU_DEFINE.BUY_VIP then
        XH.TipTool.showToast(SxvipConfig:getOpenTips() or "")
        return
    else
        XH.TipTool.showToast("开通会员，即可使用特权！")        
    end
    self._defalutMenu = SxvipConfig.MENU_DEFINE.BUY_VIP
    self:updateAllView()
end

function SxvipView:ON_KW_BTN_GIVE(sender, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    -- 打开赠送页面
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyhz24051401)
    XH.viewManager:openView("SxvipGiveView")
end

function SxvipView:onCleanup()
    SxvipView.super.onCleanup(self)
    XH.lobby:getModule("Sxvip"):clearFriendInfoData()
    XH.lobby:getModule("Sxvip"):clearStatisticsData()
end

function SxvipView:onUpdateMessage()
    local messageCount = XH.lobby:getModule("Sxvip"):getMessageCount()
    if messageCount > 0 then
        local imgMessage = self.KW_BTN_GIVE:getChildByName("_KW_IMG_MESSAGE")
        imgMessage:setVisible(true)
        local messageStr = tostring(messageCount)
        if messageCount > 99 then
            messageStr = "99+"
        end
        XH.UITool.setText(imgMessage, "_KW_TEXT_MESSAGE_COUNT", messageStr)
    else
        XH.UITool.setVisible(self.KW_BTN_GIVE, "_KW_IMG_MESSAGE", false)
    end
end

return SxvipView  !  