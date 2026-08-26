
local MenuBar = class("MenuBar", function ()
    return ccui.Layout:create()
 end)

local MenuIcon = require("lobby.Modules.Lobby.MenuBar.MenuIcon")
local LobbyConfig = require("lobby.Modules.Lobby.Config")

MenuBar.EVERY_MAX_DIS = 200   --水平最大间距
MenuBar.EVERY_MIN_DIS = 50    --水平最小间距

function MenuBar:ctor(param)
    self:initData(param)
    self:initView()
    self:refreshUI()
end

--初始化数据
function MenuBar:initData(param)
    param = param or {}
    self._adjustSize = param.adjustSize or cc.size(0, 0)
    self._maxDis = param.maxDis or self.EVERY_MAX_DIS
    self._minDis = param.minDis or self.EVERY_MIN_DIS
    self._contentSize = param.size or cc.size(0,0)
    self._bgResName = param.bg or ""
    self._curShowMenus = param.config or {}
    self._tipType = param.tipType
    self._bSmallIconBar = param.bSmallIconBar or false
    self._bAddForward = param.bAddForward or false -- 是否正向增长
    self._bFixDis = param.bFixDis or false -- 是否固定间距
    self._fixDis = param.fixDis -- 固定间距
    self._bFull = param.bFull or false  -- 是否铺满
    self._iconSize = param.iconSize
    self._lineResName = param.line or "" -- 分割线
    self._capInsets = param.capInsets or nil -- 九宫参数
    self._allMenuIcons = {}
    self._removeIDs = {}
    self:initRemoveIDs()
    self._notchWidth = self._bSmallIconBar and 0 or XH.UITool.getLiuHaiWidth()
end

function MenuBar:initRemoveIDs()
    for _, v in ipairs(self._curShowMenus) do
        local iconConf = LobbyConfig.MenuIconConfig[tonumber(v.IconID)]
        if iconConf and iconConf.IsDefaultHide then
            self._removeIDs[v.IconID] = true
        end
    end
end

--初始化
function MenuBar:initView()
    if self._bgResName ~= nil then
        self:setBackGroundImage(self._bgResName, ccui.TextureResType.plistType)
    end

    self:setContentSize(self._contentSize)
    self:setBackGroundImageScale9Enabled(true)
    if self._capInsets then
        self:setBackGroundImageCapInsets(self._capInsets)
    end
end

function MenuBar:createIcon(iconID)
    local menuItem = MenuIcon.createIcon(iconID, self._bSmallIconBar)
    menuItem:setClickCallBack(handler(self, self.onMenuTouched))
    return menuItem
end

function MenuBar:refreshUI()
    for i, v in ipairs(self._curShowMenus) do
        if self:isIconNeedShow(v.IconID) then
            self:addIcon(i, v)
        else
            self:removeIcon(v.IconID)
        end
    end
    table.sort(self._allMenuIcons, function(a, b)
        return a:getTag() < b:getTag()
    end)
    self:updateIconsPosition()
    for _,v in ipairs(self._curShowMenus) do
        if v and v.IconID then
            self:changeMenuIconTipImageRotation(v.IconID)
        end
    end
end

function MenuBar:updateIconsPosition()
    
end

function MenuBar:addIcon(index, iconConf)
    if self:isIconBeing(iconConf.IconID) then
        return
    end
    local menuIcon = self:createIcon(iconConf.IconID)
    if menuIcon then
        menuIcon:setTag(index)
        self:showTipText(menuIcon, iconConf.TipText, iconConf.TipDelay)
        self:showTipButtonByStyle(menuIcon, iconConf.TipButtonStyle, iconConf.TipButtonText)
        self:showTipImageByStyle(menuIcon, iconConf.TipImageStyle)
        self:addChild(menuIcon)
        table.insert(self._allMenuIcons, menuIcon)
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.LOBBY_MENUBAR_ICON_PUSH,{item_id = iconConf.IconID})
    end
end

function MenuBar:isIconNeedShow(iconID)
    local showMenus = self:getShowMenus()
    for _, iconConf in pairs(showMenus) do
        if iconConf.IconID == iconID then
            return true
        end
    end
    return false
end

function MenuBar:isIconBeing(iconID)
    for _, menuIcon in pairs(self._allMenuIcons) do
        if menuIcon:getName() == tostring(iconID) then
            return true
        end
    end
    return false
end

function MenuBar:removeIcon(iconID)
    for index, menuIcon in pairs(self._allMenuIcons) do
        if menuIcon:getName() == tostring(iconID) then
            menuIcon:removeSelf()
            table.remove(self._allMenuIcons, index)
            return
        end
    end
end

function MenuBar:checkIconIsOpen(iconID)
    local iconConf = LobbyConfig.MenuIconConfig[tonumber(iconID)]
    if not iconConf then
        return false
    end
    return iconConf.IsOpen
end

-- 验证json配置中的平台
function MenuBar:checkIconJsonConfPlatform(jsonConf)
    if not jsonConf then
        return false
    end
    return self:checkPlatform(jsonConf.DevicePlatform)
end

-- 验证lua配置中的平台
function MenuBar:checkIconPlatform(iconID)
    if not iconID then
        return false
    end
    local iconConf = LobbyConfig.MenuIconConfig[tonumber(iconID)]
    if not iconConf then
        return false
    end 
    return self:checkPlatform(iconConf.Platform)
end

function MenuBar:checkPlatform(devicePlatform)
    if not devicePlatform or devicePlatform == LobbyConfig.DEVICE_PLATFORM.ALL then
        return true
    end
    if device.platform == "android" then
        return devicePlatform == LobbyConfig.DEVICE_PLATFORM.ANDROID
    else
        return devicePlatform ~= LobbyConfig.DEVICE_PLATFORM.ANDROID
    end
end

function MenuBar:getShowMenus()
    local showMenus = {}
    for _, v in ipairs(self._curShowMenus) do
        if self:checkIconIsOpen(v.IconID) 
        and self:checkIconPlatform(v.IconID)
        and self:checkIconJsonConfPlatform(v)
        and not self._removeIDs[v.IconID] then
            table.insert(showMenus, v)
        end
    end
    return showMenus
end

--移除某个菜单
function MenuBar:removeMenuByID(id)
    if not id then
        return
    end
    local oldNum = #self:getShowMenus()
    self._removeIDs[id] = true
    local newNum = #self:getShowMenus()
    if oldNum ~= newNum then
        self:refreshUI()
    end
end

function MenuBar:recoveryMenu(id)
    if not id then
        return
    end
    local oldNum = #self:getShowMenus()
    if self._removeIDs[id] then
        self._removeIDs[id] = nil
    end
    local newNum = #self:getShowMenus()
    if oldNum ~= newNum then
        self:refreshUI()
    end
end

--点击事件
function MenuBar:onMenuTouched(send, eventType)
    if self._itemClickCallBack and self._bindTo then
        if eventType == ccui.TouchEventType.ended then
            local eventMsg = {}
            eventMsg.send = send
            eventMsg.eventType = eventType
            eventMsg.bind = self._bindTo
            self._itemClickCallBack(send, eventMsg)
        end
    end
end

function MenuBar:addItemClickListener(bindTo, callBack)
    self._bindTo = bindTo
    self._itemClickCallBack = callBack
end

--获取到某个菜单UI
function MenuBar:getMenuByID(id)
    local result = nil
    if id then
        result = self:getChildByName(tostring(id))
    end
    return result
end

function MenuBar:setRedPointVisible(id, show)
    if not id then
        return
    end

    local result = self:getChildByName(tostring(id))
    if not result then
        return
    end

    if show then
        result:showRedPoint()
    else
        result:hideRedPoint()
    end
end

function MenuBar:setTipNewsVisible(iconID, show)
    local menuIcon = self:getMenuByID(iconID)
    if not menuIcon then
        return
    end
    menuIcon:showTipNews(show or false)
end

function MenuBar:setTipNewAwardVisible(iconID, show)
    local menuIcon = self:getMenuByID(iconID)
    if not menuIcon then
        return
    end
    menuIcon:showTipNewAward(show or false)
end

function MenuBar:setIconTipText(iconID, tipText, tipDelay)
    local menuIcon = self:getMenuByID(iconID)
    if not menuIcon then
        return
    end
    self:showTipText(menuIcon, tipText, tipDelay)
end

function MenuBar:showTipText(menuItem, tipText, tipDelay)
    if self._tipType then
        menuItem:showTipText(self._tipType, tipText, tipDelay)
    end
end

function MenuBar:removeTipText(iconID)
    local menuIcon = self:getMenuByID(iconID)
    if not menuIcon then
        return
    end
    menuIcon:removeTipText()
end

function MenuBar:setTipTextImgVisible(iconID, bShow)
    local menuIcon = self:getMenuByID(iconID)
    if not menuIcon then
        return
    end
    menuIcon:setTipTextImgVisible(bShow)
end

function MenuBar:getAverageDis(length, menuCount)
    local averageDis = length / menuCount
    averageDis = math.max(math.min(averageDis, self._maxDis ), self._minDis)
    if self._bFixDis and self._fixDis then
        averageDis = self._fixDis
    end
    return averageDis
end

function MenuBar:showTipImageByStyle(menuIcon, imgStyle)
    menuIcon:showTipImageStyle(imgStyle)
end

function MenuBar:showTipButtonByStyle(menuIcon, btnStyle, btnText)
    menuIcon:showButtonByStyle(btnStyle, btnText)
    menuIcon:setButtonClickCallBack(handler(self, self.onTipButtonTouchEnd))
end

function MenuBar:addTipButtonClickListener(bindTo, callBack)
    self._bindTo = bindTo
    self._tipButtonClickCallBack = callBack
end

function MenuBar:onTipButtonTouchEnd(send, eventType)
    if self._tipButtonClickCallBack and self._bindTo then
        if eventType == ccui.TouchEventType.ended then
            local eventMsg = {}
            eventMsg.send = send
            eventMsg.eventType = eventType
            eventMsg.bind = self._bindTo
            self._tipButtonClickCallBack(send, eventMsg)
        end
    end
end

function MenuBar:changeMenuIconTipImageRotation(iconID)
    local menuIcon = self:getMenuByID(iconID)
    if not menuIcon then
        return
    end
    if menuIcon:judgeRotateTipImg() then
        menuIcon:flushTipImageRotation()
    end
end

function MenuBar:setExtendAnimVisible(id, show)
    if not id then
        return
    end

    local result = self:getChildByName(tostring(id))
    if not result then
        return
    end

    if show then
        result:showExtendAnim()
    else
        result:hideExtendAnim()
    end
end

return MenuBar W(  