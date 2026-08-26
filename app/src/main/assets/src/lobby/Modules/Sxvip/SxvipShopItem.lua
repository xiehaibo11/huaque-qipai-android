local SxvipShopItem = class("SxvipShopItem", XH.ViewBase)

local SxvipConfig = require("lobby.Modules.Sxvip.Config")
local ShopConfig = require("lobby.Modules.Shop.Config")
local UrlConf = require("app.Config.UrlConf")

local KW_HORIZONTAL_DIS = 50 -- 左右边距
local KW_VECTICAL_DIS = 10 -- 顶边距
local KW_HORIZONTAL_X_DIS = 0 -- 每个item水平最小间距
local KW_VECTICAL_Y_DIS = 50 -- 每个item垂直间距
local KW_HORIZONTAL_WITH = 1430 --固定宽度显示，宽度
local KW_SCROLLVIEW_HEIGHT_OFFSET = 50 --scrollview内框高度偏移量

local KW_DEFAULT_ITEM_SIZE = cc.size(416, 500)

function SxvipShopItem:getBindingInfo()
    return {
        ["_KW_VIP_ROOT"] = {varName = "_vipRoot"},
        ["_KW_VIP_LIST_VIEW"] = {varName = "_displayVipList"},
        ["_KW_VIP_SCROLL_VIEW"] = {varName = "_vipScrollList"},
        ["_KW_BTN_RESTORE"] = {varName = "_btnRestore" , type = XH.UI_TYPE.BUTTON, onTouchEnded = "ON_KW_BTN_RESTORE"},
        ["_KW_BTN_VIP_PRIVIGATE"] = {varName = "_btnVipPrivigate" , type = XH.UI_TYPE.BUTTON, onTouchEnded = "ON_KW_BTN_VIP_PRIVIGATE"},
        -- ["_KW_CHECK_BOX"] = {varName = "_checkBox" , type = XH.UI_TYPE.CHECKBOX, onSelect = "ON_KW_CHECK_BOX"},
        ["_KW_PANEL_ITEM"] = {varName = "_vipItem"},
        -- ["_KWA_DESC_TIPS"] = {varName = "_descTips"},
        ["_KW_CHECK_PROTOCOL"] = {varName="_btnProtocol", onTouchEnded = "onTouchEventProtocol", type = XH.UI_TYPE.BUTTON},
        ["_KW_CHECK_VIPPROTOCOL"] = {varName="_btnVipProtocol", onTouchEnded = "onTouchEventVipProtocol", type = XH.UI_TYPE.BUTTON},
        ["_KW_CHECK_RENEWPROTOCOL"] = {varName="_btnRenewProtocol", onTouchEnded = "onTouchEventRenewProtocol", type = XH.UI_TYPE.BUTTON},
    }
end

function SxvipShopItem:getProxyEvents()
    return {
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVENT_PAGE_PRODUCTS_CONFIG", callBack = "onEventGetPagProducts" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVNET_SHOP_PAGE_MARK_TOP", callBack = "onEventMarkTop" },
        { module = XH.lobby:getModule("Sxvip"), eventKeyName = "EVENT_UPDATE_VIPSHOP_LIST", callBack = "onEventMarkTop" },
    }
end

function SxvipShopItem:getCSBPath()
    return "cocosStudio/hall/CSB/Sxvip/SxvipShopItem.csb"
end

function SxvipShopItem:ctor()
    SxvipShopItem.super.ctor(self)
    self._productData = nil
    self._selected = true  --默认选中
    self:initView()
end

function SxvipShopItem:initView()
    local size = self._vipRoot:getContentSize()
    self:setContentSize(size)
    local targetPlatform = cc.Application:getInstance():getTargetPlatform()
    if device.platform == "ios" or device.platform == "windows" then
        self._btnRestore:setVisible(true)
        self._btnProtocol:setVisible(true)
        self._btnVipProtocol:setVisible(true)
        self._btnRenewProtocol:setVisible(true)
    end
    -- self._descTips:setVisible(false)
    -- self._displayVipList:setScrollBarEnabled(false)
end

function SxvipShopItem:refreshView()
    -- local _, sxvipSubId = XH.lobby:getModule("Shop"):getTagsWithName(ShopConfig.SpecialTag.SXVIP)
    -- if sxvipSubId then
    --     local productData = XH.lobby:getModule("Shop"):getShopItemsWithId(sxvipSubId)
    --     if productData then
    --         self._productData = productData.goods_list
    --         self._displayVipList:removeAllChildren()
    --         self._descTips:setVisible(true)
    --         self:updateViplistView()
    --         self:updateCheckBox()
    --     else
    --         XH.lobby:getModule("Shop"):reqShopProductByConfig(sxvipSubId)
    --     end
    -- else
    --     XH.lobby:getModule("Shop"):reqShopPageMarkTop()
    -- end
    local productData = XH.lobby:getModule("Sxvip"):getVipShopList()
    if productData and productData.goods_list and #productData.goods_list > 0 then
        self._productData = productData.goods_list
        -- self._displayVipList:removeAllChildren()
        self._vipScrollList:removeAllChildren()
        -- self._descTips:setVisible(true)
        self:updateViplistView()
        -- self:updateCheckBox()
    else
        local prop_type = SxvipConfig:getSxvipShopId()
        XH.lobby:getModule("Sxvip"):reqExchangeProductsInfo(prop_type)
    end
end

function SxvipShopItem:onEventMarkTop(event)
    XH.TipTool.hideLoading()
    self:refreshView()
end

function SxvipShopItem:onEventGetPagProducts(event)
    local specialTag = event.data.tag
    if ShopConfig.SpecialTag.SXVIP == specialTag then
        XH.TipTool.hideLoading()
        self:refreshView()
    end
end

-- function SxvipShopItem:updateCheckBox()
--     self._checkBox:setSelected(self._selected)
-- end

function SxvipShopItem:updateViplistView()
    -- self._displayVipList:removeAllItems()
    -- for i = 1, #self._productData do
    --     local vipItem = self._vipItem:clone()
    --     self:updateItem(vipItem , self._productData[i])
    --     self._displayVipList:pushBackCustomItem(vipItem)
    -- end

    local totalNeedCount = #self._productData
    -- 动态初始化滑动区域大小
    self:resetDisPlayContentSize(totalNeedCount)

    -- 创建牌友UI
    for i = 1, totalNeedCount do
        local productData = self._productData[i]
        local item = self:getProductItemByIndex(i)
        if not item then
            item = self:createNewProductItem()
        end
        item:setZOrder(totalNeedCount - i)
        self:updateItem(item, productData, i)
    end
end


-- 通过索引获得item
function SxvipShopItem:getProductItemByIndex(index)
    return self._vipScrollList:getChildByName(tostring(index))
end

-- 添加一个item到界面上.挂载到ScrollView中去
function SxvipShopItem:createNewProductItem()
    local item = self._vipItem:clone()
    if item then
        item:retain()
        item:removeFromParent()
        self._vipScrollList:addChild(item)
        item:release()
    end
    return item
end


-- 计算显示区域的大小的位置
function SxvipShopItem:resetDisPlayContentSize(productCount)
    if not productCount then
        return
    end
    -- 每个item的大小
    local productModSize = KW_DEFAULT_ITEM_SIZE
    if self._vipItem then
        productModSize = self._vipItem:getContentSize()
    end

    local modWidth = productModSize.width
    local displayContentSize = self._vipScrollList:getInnerContainerSize()

    -- 计算最小间距下每行的个数
    local miniTotalXDis = modWidth + KW_HORIZONTAL_X_DIS
    local withoutBoderWidt = displayContentSize.width - KW_HORIZONTAL_DIS * 2
    local everyLineCount = math.floor(withoutBoderWidt / miniTotalXDis)
    local lineCount = math.ceil(productCount / everyLineCount)

    if lineCount ~= self._curLineCount then
        -- 现有的滑动区域需要变化
        displayContentSize = self._vipScrollList:getContentSize()
        self._vipScrollList:setInnerContainerSize(cc.size(displayContentSize.width, lineCount * (productModSize.height + KW_SCROLLVIEW_HEIGHT_OFFSET)))
        self._curLineCount = lineCount
    end
end

function SxvipShopItem:updateItem(vipItem , data, index)
    if tolua.isnull(vipItem) then
        return
    end
    vipItem:setName(tostring(index))
    vipItem:setPosition(self:getDisplayPos(index))
    vipItem:setVisible(true)

    local useData , useSubscription = self:getUseData(data)
    local btnBuy = vipItem:getChildByName("_KW_BTN_BUY")
    btnBuy:addTouchEventListener(handler(self, self.onBuy))
    -- 名字
    local txtTime = vipItem:getChildByName("_KW_TIME_TXT")
    txtTime:setString(useData.name)
    -- 角标
    local tag = vipItem:getChildByName("_KW_IMG_TAG")
    local tagConfig = SxvipConfig.TAG[useData.corner_tag]
    if tagConfig then
        tag:setVisible(true)
        tag:loadTexture(tagConfig.icon, ccui.TextureResType.plistType)
    else
        tag:setVisible(false)
    end
    -- icon
    local item = vipItem:getChildByName("_KW_ICON_PANEL")
    local itemIcon = item:getChildByName("ITEM_ICON")
    if itemIcon == nil then
        local size = item:getContentSize()
        itemIcon = XH.RemoteImage.new()
        :setPosition(size.width / 2, size.height / 2)
        :ignoreContentAdaptWithSize(false)
        :setContentSize(size.width, size.height)
        :setName("ITEM_ICON")
        :addTo(item)
    end
    itemIcon:setUrl(useData.image)
    btnBuy.customData = useData
    -- 价值
    local txtValue = vipItem:getChildByName("_KWA_VALUE")
    if txtValue then
        txtValue:setString("价值" .. useData.display_price .. "元")
    end
    -- 价格
    local txtPrice = btnBuy:getChildByName("_KW_TXT_PRICE")
    local price = useData.real_price or 0
    txtPrice:setString(useSubscription and "订阅价:".. price .. "元" or price .. "元")
end


-- 计算item的位置
function SxvipShopItem:getDisplayPos(index)
    local nextIndex = index or self:getProductCount()
    nextIndex = nextIndex - 1
    -- 计算位置从0开始

    -- 每个item的大小
    local productModSize = KW_DEFAULT_ITEM_SIZE
    if self._vipItem then
        productModSize = self._vipItem:getContentSize()
    end

    local modHeight = productModSize.height
    local modWidth = productModSize.width
    local displayContentSize = self._vipScrollList:getInnerContainerSize()

    -- 计算最小间距下每行的个数
    local miniTotalXDis = modWidth + KW_HORIZONTAL_X_DIS
    -- local withoutBoderWidt = displayContentSize.width - KW_HORIZONTAL_DIS * 2
    local withoutBoderWidt = KW_HORIZONTAL_WITH - KW_HORIZONTAL_DIS * 2
    local everyLineCount = math.floor(withoutBoderWidt / miniTotalXDis)

    -- 中间的水平分布
    local xDif =(withoutBoderWidt - everyLineCount * modWidth) /(everyLineCount - 1)
    local lineIndex = math.floor(nextIndex / everyLineCount)

    local totalXDis = modWidth + xDif
    local totalYDis = modHeight + KW_VECTICAL_Y_DIS

    -- 起始的位置
    local startPosX = KW_HORIZONTAL_DIS + modWidth / 2
    local startPosY = KW_VECTICAL_DIS + modHeight / 2

    local posX = startPosX + totalXDis *(nextIndex % everyLineCount)
    local posY = displayContentSize.height -(startPosY + totalYDis * lineIndex)

    return posX, posY
end

-- 获得UI界面上item的总数
function SxvipShopItem:getProductCount()
    return self._vipScrollList:getChildrenCount() or 0
end

-- 获取实际用的数据
function SxvipShopItem:getUseData(data)
    local product = data 
    local useSubscription = false 
    if data.tags and data.tags[1] and data.tags[1].key and data.tags[1].key == "Subscription" then
        useSubscription = true
    end
    return product , useSubscription
end

function SxvipShopItem:onBuy(sender , eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if not sender.customData then
        return
    end
    -- 未实名不能购买 海外玩家不用实名认证
    if XH.configerManager:getIsNewRealName() then
        if XH.playerData:checkNewRealName() ~= 0 then return end--实名限制
    else
        if not XH.lobby:getModule("Shop"):isAleardyRealName() then
            XH.TipTool.showTip( {
                type = XH.TIP_LAYER_TYPE.OK,
                funcOK = function()
                    XH.viewManager:openView("IdentityCheckView")
                end
            } , "PLEASE_REAL_NAME")
            return
        end 
    end
    if device.platform == "android" or device.platform == "windows" then
        XH.lobby:getModule("Sxvip"):reqBuy(sender.customData)
    else
        XH.viewManager:openView("SxvipInfoView", nil, sender.customData)
    end
end

function SxvipShopItem:ON_KW_BTN_RESTORE(sender , eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.sdkManager:xhCallOCFunc("XH_IAP", "restoreSub:", {})
end

function SxvipShopItem:ON_KW_BTN_VIP_PRIVIGATE(sender , eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if XH.viewManager:isViewExist("ShopView") then
        local shopView = XH.viewManager:getView(XH.SCENE_TAG.LOBBY , "ShopView")
        shopView:hide()
        local params = {
            defalutMenu = SxvipConfig.MENU_DEFINE.VIP_PRIVILEGE
        }
        XH.viewManager:openView("SxvipView" , nil , params)
    else
        -- 跳转到特权
        local sxvipView = XH.viewManager:getView(XH.SCENE_TAG.LOBBY , "SxvipView")
        if sxvipView then
            sxvipView._defalutMenu = SxvipConfig.MENU_DEFINE.VIP_PRIVILEGE
            sxvipView:updateAllView()
        end
    end
end

-- function SxvipShopItem:ON_KW_CHECK_BOX(sender , eventType)
--     if eventType == ccui.CheckBoxEventType.selected then
--         self._selected = true
--     elseif eventType == ccui.CheckBoxEventType.unselected then
--         self._selected = false
--     end
--     self:updateViplistView()
-- end
-- 用户协议
function SxvipShopItem:onTouchEventProtocol(send, event)
    local targetPlatform = cc.Application:getInstance():getTargetPlatform()
    if cc.PLATFORM_OS_ANDROID == targetPlatform then
        return
    end
    cc.Application:getInstance():openURL(UrlConf.URL_USER_SERVICE_IOS)
end

-- 会员服务协议
function SxvipShopItem:onTouchEventVipProtocol(send, event)
    local targetPlatform = cc.Application:getInstance():getTargetPlatform()
    if cc.PLATFORM_OS_ANDROID == targetPlatform then
        return
    end
    cc.Application:getInstance():openURL(UrlConf.URL_USER_VIPPROTOCOL_IOS)
end

-- 自动续费协议
function SxvipShopItem:onTouchEventRenewProtocol(send, event)
    local targetPlatform = cc.Application:getInstance():getTargetPlatform()
    if cc.PLATFORM_OS_ANDROID == targetPlatform then
        return
    end
    cc.Application:getInstance():openURL(UrlConf.URL_USER_RENEWPROTOCOL_IOS)
end

return SxvipShopItem   7  