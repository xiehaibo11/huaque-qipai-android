local ShopView = class("ShopView", XH.ViewBase)
local ShopConfig = require("lobby.Modules.Shop.Config")
local UrlConf = require("app.Config.UrlConf")
local VipShopItem = require("lobby.Modules.NewVip.VipShopItem")
local SxvipShopItem = require("lobby.Modules.Sxvip.SxvipShopItem")

-- UI名字定义
-- 商品信息
local KW_IMG_LOGO = "KW_IMG_LOGO_"
local KW_TXT_NAME = "KW_TXT_NAME"
local KW_TXT_PRICE = "_KW_BMFONT_PRICE"
local KW_IMG_ACT_BG = "KW_IMG_ACT_BG"
local KW_IMAGE_FLAG = "KW_IMAGE_FLAG"
local KW_IMAGE_LAST_BUY = "KW_IMAGE_LAST_BUY"
local KW_IMG_BUY_ICO = "KW_IMG_BUY_ICO"
local KW_BTN_EXCHANGE = "KW_BTN_EXCHANGE"
local KW_EXCHANGE_TEXT = "KW_BMFONT_EXCHANGE"
local KW_IMG_CUT_LINE = "_KW_IMG_CUT_LINE"
local KW_LAYOUT_ANI_BG = "_KW_LAYOUT_ANI_BG"
local KW_PANEL_MORE_BUY = "KW_PANEL_MORE_BUY"
local KW_LAYOUT_ANI_POS = "_KW_LAYOUT_ANI_POS"
local KW_TXT_ITEMS_PRICE = "_KW_BMFONT_ITEMS_PRICE_"
local KW_TEXT_ACT_CONTENT = "KW_TEXT_ACT_CONTENT"
local KW_TEXT_ORIGIN_PRIZE = "_KW_TEXT_ORIGIN_PRIZE"
local KW_NODE_PRICE_CHOOSE = "KW_NODE_PRICE_CHOOSE"
local KW_PANEL_PRICE_ALONE = "KW_PANEL_PRICE_ALONE"
local KW_PANEL_DEFAULT_SIZE = "KW_PANEL_DEFAULT_SIZE"

local KW_BTN_TYPE = "KW_BTN_TYPE_"
local KW_IMAGE_RED_POS = "KW_IMAGE_RED_POS"
local KW_IMG_CHOSE_STATE = "KW_IMG_CHOSE_STATE"
local KW_IMG_PRODUCT_IMG = " KW_IMG_PRODUCT_IMG"
local KW_TXT_PRODUCT_NAME = "KW_TXT_PRODUCT_NAME"
local KW_CHAGRE_TIP_LIGHT = "KW_CHAGRE_TIP_LIGHT"
local KW_IMG_PRODUCT_IMG_BG = "_KW_IMG_PRODUCT_IMG_BG"
local KW_LEFT_BUTTON_CHARGE_TIP = "KW_LEFT_BUTTON_CHARGE_TIP"

local KW_BTN_SECOND_TYPE = "KW_BTN_SECOND_TYPE_"
local KW_IMG_SECOND_CHOSE = "KW_IMG_SECOND_CHOSE"
local KW_TEXT_SECOND_TITLE = "KW_TEXT_SECOND_TITLE"
local KW_TEXT_SECOND_CHOSE_TITLE = "KW_TEXT_SECOND_CHOSE_TITLE"

-- 其他定义
local KW_ITEM_BY_ITEM_DIS = 28 -- 单个item之间边距
local KW_HORIZONTAL_DIS = 33 -- 左右边距
local KW_HORIZONTAL_VIP_DIS = 30 -- 左右边距
local KW_VECTICAL_DIS = 30 -- 顶边距
local KW_VECTICAL_Y_DIS = 20 -- 每个item垂直间距
local KW_VECTICAL_VIP_DIS = 20 -- VIP顶边距

local KW_DEAFULT_CUT_LENGTH = 16
local ItemAniPosY = 60
local BANNER_ANI_DELAY_TIME = 5
local KW_DEFAULT_ITEM_ICO_SIZE = { width = 52, height = 54 }
local DEFAULT_DOWNLOAD_IMAGE_DIR = device.writablePath .. "tmpImageCachDir/"

-- 默认商品剩余数量
local KW_DEFAULT_LEST_SHOPS = 999
local KW_YEAR = 365

function ShopView:getCSBPath()
    return "cocosStudio/hall/CSB/ShopLayer.csb"
end

function ShopView:getBindingInfo()
    return {
        ["_KW_PANEL_SX_VIP"] = { varName = "_panelSxvip" },
        ["_KW_SCROLLVIEW_DISPLAY"] = { varName = "_displayList" },
        ["_KW_PANEL_PRODUCT_MOD"] = { varName = "_productMod" },
        ["_KW_BTN_TYPE_MOD"] = { varName = "_btnType" },
        ["_KW_LIST_PRODUCT_TYPE"] = { varName = "_leftBtnParent" },
        ["_KW_BTN_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KW_IMG_AREA"] = { varName = "_areaImage" },
        ["_KW_TEXT_AREA"] = { varName = "_areaText" },
        ["_KW_BMFONT_NUM_DIAMOND"] = { varName = "_textNumDiamond" },
        ["_KW_BMFONT_NUM_ROOM_CARD"] = { varName = "_textNumRoomCard" },
        ["_KW_BMFONT_NUM_GOLD"] = { varName = "_textNumGold" },
        ["_KW_TEXT_LIQUAN"] = { varName = "_textNumliquan" },
        ["_KW_BTN_BAG"] = { varName = "_btnBag", onTouchEnded = "onTouchEventOpenBagLayer" },
        ["_KW_PANEL_CZYD"] = { varName = "_btnGoLinkCharge", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventGoLinkCharge" },
        ["_KW_VIP_NOTICE"] = { varName = "_vipNoticePanel" },
        ["_KW_BTN_VIP_NOTICE"] = { varName = "_vipNoticeBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchVipNotice" },
        ["_KW_SCROLLVIEW_DISPLAY_VIP"] = { varName = "_displayVipList" },
        ["_KW_TEXT_NORMAL_NOTICE"] = { varName = "_normalNotice" },
        ["_KW_PANEL_ITEMS_BANNERBG"] = { varName = "_itemsBannerBg" },
        ["_KW_PANEL_ITEMS_NORMALBG"] = { varName = "_itemsNormalBg" },
        ["_KW_SCROLLVIEW_SECENDS"] = { varName = "_secondMenu" },
        ["_KW_PANEL_SECEND_TAG_MODEL"] = { varName = "_secondTagModel" },
        ["_KW_PANEL_BENNERS"] = { varName = "_panelBanners" },
        ["_KW_ACTION_BANNER"] = { varName = "_isBannerAction" },
        ["_KW_TEXT_NONE_PRIZE"] = { varName = "_nonePrizeItem" },
        ["_KW_CHARGE_GUIDE_LAYER"] = { varName = "_chargeGuideLayer" },
        ["_KW_IMG_LINGQI_MENGBAN"] = {varName="_linQiAniMb"},
        ["_KW_IMG_LINGQI"] = {varName="_linQiAniImg"},
        ["_KW_TEXT_LINGQI"] = {varName="_lingQiLabel"},
}
end

function ShopView:getProxyEvents()
    return {
        { module = XH.playerData, eventKeyName = "EVENT_PLAYER_DROP_CHANGED", callBack = "flushTopInfo" },
        { module = XH.playerData, eventKeyName = "EVENT_SR_CHANGED", callBack = "flushTopInfo" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVNET_SHOPMANAGER_SHOW_SHOP", callBack = "onRefresh" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVNET_SHOPMANAGER_HIDE_SHOP", callBack = "hide" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVENT_SHOW_DIAMOND_VIEW", callBack = "onShowDiamondView" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVENT_SHOW_CARD_VIEW", callBack = "onShowCardView" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVENT_SHOW_GOLD_VIEW", callBack = "onShowGoldView" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVENT_PAGE_PRODUCTS_CONFIG", callBack = "onEventGetPagProducts" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVENT_SHOP_NEW_ITEM", callBack = "onEventShowItemsRedPoint" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVENT_REFRESH_ACT_CLOSE", callBack = "onEventCloseActItem" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVENT_START_SHOW_PRODUCTS", callBack = "onEventSrartShowProducts" },
        { module = XH.lobby:getModule("CouponSystem"), eventKeyName = "EVENT_REFRESH_COUPON_DATA", callBack = "onEventRefreshCouponData" },
        { module = XH.lobby:getModule("CouponSystem"), eventKeyName = "EVENT_COUPON_SHOW_DIAMOND_VIEW", callBack = "onShowDiamondView" },
    }
end

function ShopView:ctor(param)
    param = param or { }
    ShopView.super.ctor(self, param)
    -- 右边页签按钮
    self._leftTabBtns = { }
    self._upSecendTabBtns = { }

    -- banner动画相关
    self._isNeedShowBanners = false
    self._bannerAniMoveTimes = 0

    -- 商品item
    self._productModSize = cc.p(0, 0)

    self._defaultShopType = param.productType
    self._showCardChargeTip = param.showCardChargeTip or false
    self._showGoldChargeTip = param.showGoldChargeTip or false
    XH.playerData:flushGoldCoin()
    self:initBagBtnVisible()
    XH.lobby:getModule("Shop"):reqShopBuyLogs()
    self:startShowUI()
    XH.lobby:getModule("H5GameWidget"):setGameData("","","商城界面","BMN001")
    XH.lobby:getModule("H5GameWidget"):addH5GameWidget("shop")
    XH.lobby:getModule("BagSysNew"):reqCouponListInfo()
    XH.taskManager:reqLastBuyProductID(XH.lobby:getModule("Shop").KW_REQ_LAST_BUY_PRODUCT_ID_TYPE.GET_LAST_BUY_ID)
    XH.sdkManager:callFunction("wechat_reset_pay_params")
    XH.lobby:getModule("PropEventTracker"):track(XH.NewThrowDataDefine.dj24080867, {type = self._displayProductType})
end

function ShopView:onEventSrartShowProducts()
    -- XH.lobby:getModule("Shop"):reqShopPageMarkTop()
end

function ShopView:onEventRefreshCouponData()
    XH.lobby:getModule("BagSysNew"):reqCouponListInfo()
    self:flushTopInfo()
end

function ShopView:setAreaImageVisible()
    if self._areaImage and self._areaText then
        self._areaText:setString("")
        self._areaImage:setVisible(false)
    end
end

function ShopView:initUI(productTag)
    -- 商城顶部的道具信息
    self:flushTopInfo()
    self:initSecondMenus()

    -- 防止多次初始化
    local pruductSpecial = XH.lobby:getModule("Shop"):getSecondTagsWithTag(self._displayProductType, self._displaySecondType)
    if productTag and productTag ~= pruductSpecial.tag then
        return
    end

    if XH.ChannelTool.judgeIsChannel() then 
        -- 隐藏当前区域
        if self._areaImage and self._areaText then
            self._areaText:setString("")
            self._areaImage:setVisible(false)
        end
    else
        -- 显示当前区域
        if self._areaImage and self._areaText then
            local areaString = XH.areaData:getAreaName()
            self._areaText:setString(areaString)
            self._areaImage:setVisible(true)
        end
    end

    --显示是否有充值引导 _chargeGuideLayer
    self:initChagreGuideLayer()

    -------------
    local _, goldSubId = XH.lobby:getModule("Shop"):getTagsWithName(ShopConfig.SpecialTag.GOLD)
    if goldSubId and not XH.lobby:getModule("Shop"):getShopItemsWithId(goldSubId) then
        XH.lobby:getModule("Shop"):reqShopProductByConfig(goldSubId)
    end
    local _, cardSubId = XH.lobby:getModule("Shop"):getTagsWithName(ShopConfig.SpecialTag.CARD)
    if cardSubId and not XH.lobby:getModule("Shop"):getShopItemsWithId(cardSubId) then
        XH.lobby:getModule("Shop"):reqShopProductByConfig(cardSubId)
    end
    local _, diamondSubId = XH.lobby:getModule("Shop"):getTagsWithName(ShopConfig.SpecialTag.DIAMOND)
    if diamondSubId and not XH.lobby:getModule("Shop"):getShopItemsWithId(diamondSubId) then
        XH.lobby:getModule("Shop"):reqProductList(ShopConfig.ProductType.DIAMOND)
    end
end

function ShopView:initChagreGuideLayer()
    self._chargeGuideLayer:addTouchEventListener(handler(self, self.onTouchEventChargeGuideLayer))
    if self._showCardChargeTip then
        self:updateLeftButtonChareGuide(true,"房卡","点此购买房卡，用于支付建房费用")
    end
    if self._showGoldChargeTip then
        self:updateLeftButtonChareGuide(true,"金币","点此购买金币，用于金币场畅玩")
    end
    if self._chargeGuideLayer then
        self._chargeGuideLayer:setVisible(self._showGoldChargeTip or self._showCardChargeTip)
        self._chargeGuideLayer:setSwallowTouches(false)
    end
end

function ShopView:onTouchEventChargeGuideLayer(send,eventType)
    if eventType == ccui.TouchEventType.began then
        if self._chargeGuideLayer then
            self._chargeGuideLayer:setVisible(false)    
        end
        if self._showCardChargeTip then
            self._showCardChargeTip = false
            self:updateLeftButtonChareGuide(false,"房卡")
        end
        if self._showGoldChargeTip then
            self._showGoldChargeTip = false
            self:updateLeftButtonChareGuide(false,"金币")
        end
        self._leftBtnParent:setContentSize(self._oriLeftBtnParentSize)
    end
end

function ShopView:updateLeftButtonChareGuide(show,name,tip)
    local button = self:getLeftButtonByName(name)
    if button then
        XH.UITool.setVisible(button,KW_CHAGRE_TIP_LIGHT,show)
        if show then
            self._leftBtnParent:setContentSize(cc.size(self._oriLeftBtnParentSize.width * 2,self._oriLeftBtnParentSize.height))
            if tip then
                XH.UITool.setText(button,KW_LEFT_BUTTON_CHARGE_TIP,tip)
            end
        end
        
    end
end

function ShopView:getLeftButtonByName(name)
    self._leftTabBtns = self._leftBtnParent:getChildren()
    for _,button in pairs(self._leftTabBtns) do
        local textNode = button:getChildByName(KW_TXT_PRODUCT_NAME) 
        if textNode then
            if textNode:getString() == name then
                return button
            end
        end
    end
    return nil
end

function ShopView:initSecondMenus()
    local secondMenu = XH.lobby:getModule("Shop"):getFirstTagsWithTag(self._displayProductType)
    self._secondMenu:removeAllChildren()
    if secondMenu.sub and #secondMenu.sub > 1 then
        for _, v in ipairs(secondMenu.sub) do
            local seceondMenuModel = self._secondTagModel:clone()
            XH.UITool.setText(seceondMenuModel, KW_TEXT_SECOND_TITLE, v.title)
            XH.UITool.setText(seceondMenuModel, KW_TEXT_SECOND_CHOSE_TITLE, v.title)
            seceondMenuModel:setName(KW_BTN_SECOND_TYPE .. v.id)
            seceondMenuModel:addTouchEventListener(handler(self, self.onSecondTabChanged))
            self._secondMenu:addChild(seceondMenuModel)
        end
        self._secondMenu:setVisible(true)
    else
        self._secondMenu:setVisible(false)
    end
    self._upSecendTabBtns = self._secondMenu:getChildren()
end

function ShopView:flushTopInfo()
    self._textNumGold:setText(XH.playerData:getSR())
    self._textNumRoomCard:setText(XH.playerData:getRoomCard())
    self._textNumDiamond:setText(XH.playerData:getDiamnd())
    self._textNumliquan:setText(XH.playerData:getLiQun())
end

function ShopView:flushItemBg(isHaveBanner)
    self._itemsBannerBg:setVisible(isHaveBanner)
    self._itemsNormalBg:setVisible(not isHaveBanner)
end

-- 获取当前scrollview下每行最多个数
function ShopView:getMaxCountLine()
    local modWidth = self._productModSize.width
    local displayContentSize = self._displayList:getInnerContainerSize()
    local withoutBoderWidt = displayContentSize.width - KW_HORIZONTAL_DIS * 2

    self:flushItemBg(self._isNeedShowBanners)
    if self._isNeedShowBanners then
        return math.floor(withoutBoderWidt / modWidth) -1
    else
        return math.floor(withoutBoderWidt / modWidth)
    end
end

-- 计算item的位置
function ShopView:getDisplayPos(index)
    local nextIndex = index or self:getProductCount()
    nextIndex = nextIndex - 1
    -- 计算位置从0开始
    local modHeight = self._productModSize.height
    local modWidth = self._productModSize.width
    local displayContentSize = self._displayList:getInnerContainerSize()
    local everyLineCount = self:getMaxCountLine()
    local lineIndex = math.floor(nextIndex / everyLineCount)

    local totalXDis = modWidth + KW_ITEM_BY_ITEM_DIS
    local totalYDis = modHeight + KW_VECTICAL_Y_DIS

    -- 起始的位置
    local startPosX = KW_HORIZONTAL_DIS + modWidth / 2
    local startPosY = KW_VECTICAL_DIS + modHeight / 2

    local posX = startPosX + totalXDis *(nextIndex % everyLineCount)
    local posY = displayContentSize.height -(startPosY + totalYDis * lineIndex)
    return posX, posY
end

-- 计算显示区域的大小的位置
function ShopView:resetDisPlayVipContentSize(productCount,size)
    if not productCount then
        return
    end
    local modWidth = size.width
    local displayContentSize = self._displayVipList:getContentSize()
    local miniTotalXDis = modWidth + KW_HORIZONTAL_VIP_DIS
    if productCount * miniTotalXDis > displayContentSize.width then
        -- 现有的滑动区域需要变化
        self._displayVipList:setInnerContainerSize(cc.size(productCount * miniTotalXDis,displayContentSize.height))
    end
    self._displayVipList:setScrollBarEnabled(false)
end

-- 计算显示区域的大小的位置
function ShopView:resetDisPlayContentSize(productCount)
    if not productCount then
        return
    end

    local displayContentSize = self._displayList:getContentSize()
    local everyLineCount = self:getMaxCountLine()
    local lineCount = math.ceil(productCount / everyLineCount)
    local averageHeight = displayContentSize.height / 2
    self._displayList:setInnerContainerSize(cc.size(displayContentSize.width, lineCount * averageHeight))
    self._displayList:setScrollBarEnabled(false)
end

-- 获得UI界面上item的总数
function ShopView:getProductCount()
    return self._displayList:getChildrenCount() or 0
end

-- 通过索引获得item
function ShopView:getProductByIndex(index)
    return self._displayList:getChildByName(tostring(index))
end

-- 清除所有显示的商品
function ShopView:clear()
    self._displayList:removeAllChildren()
end

function ShopView:createNewVipProduct(productData, index)
    local listSize = self._displayVipList:getContentSize()
    local vipShopItem = VipShopItem.new()
    vipShopItem:refreshView(productData.condition,tonumber(productData.value),productData.mark or false,index)
    vipShopItem:setPropType(self._displaySecondType)
    local size = vipShopItem:getContentSize()
    vipShopItem:setPosition(size.width * (index - 1) + KW_HORIZONTAL_VIP_DIS * index,listSize.height - size.height - KW_VECTICAL_VIP_DIS)
    self._displayVipList:addChild(vipShopItem)
    return vipShopItem
end

-- 添加一个item到界面上
function ShopView:createNewProduct(productData, index)
    local item = self._productMod:clone()
    if item then
        -- 挂载到ScrollView中去
        item:retain()
        item:removeFromParent()
        self._displayList:addChild(item)
        item:release()
    end
    return item
end

-- 初始化item
function ShopView:updateItemUI(item, productData, index, isFirst)
    cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/hall/Image/shop_new/new_shop_items.plist")
    isFirst = isFirst or false
    item:setName(tostring(index))
    item:setTag(productData.id or 0)
    item:setPosition(self:getDisplayPos(index))
    item:setVisible(true)

    -- 初始化子项
    if isFirst then
        XH.UITool.addTouchEventListener(item, KW_BTN_EXCHANGE, handler(self, self.onExchangeBtnTouchEvent))
        XH.UITool.addTouchEventListener(item, KW_PANEL_MORE_BUY, handler(self, self.onExchangeLookBtnTouchEvent))
    end

    -- 设置item属性
    XH.UITool.setTag(item, KW_BTN_EXCHANGE, index)
    XH.UITool.setTag(item, KW_PANEL_MORE_BUY, index)
    -- 按钮索引，点击的索引
    XH.UITool.setText(item, KW_TXT_NAME, productData.name)

    -- 设置商品图片
    self:setItemImageOrign(item, productData, index)
    -- 设置商品价格
    self:setItemPrizeImage(item, productData)
    -- 商品活动设置
    self:setIsActItem(item, productData)
    -- 设置商品动画
    self:onShowItemWithAction(item, productData) 
    -- 设置Item的购买文本UI
    self:setItemBuyName(item, productData)
end

function ShopView:isShowBanners()
    local bannerImages = { }
    local productAllData = XH.lobby:getModule("Shop"):getShopItemsWithId(self._displaySecondType)
    if productAllData and productAllData.pop_config and #productAllData.pop_config > 0 then
        bannerImages = productAllData.pop_config
    end
    self._panelBanners:removeAllChildren()
    if self._isNeedShowBanners then
        for i = 1, #bannerImages do
            local size = self._panelBanners:getContentSize()
            local needSetPostionX = size.width / 2
            if i > 1 then
                needSetPostionX = size.width + size.width / 2
            end
            local bannerImage = XH.RemoteImage.new()
            :setPosition(needSetPostionX, size.height / 2)
            :ignoreContentAdaptWithSize(false)
            :setContentSize(size.width, size.height)
            :addTo(self._panelBanners)
            bannerImage:setUrl(bannerImages[i].url, false)
            bannerImage:setTouchEnabled(true)
            bannerImage:setBright(true)
            bannerImage:setName(bannerImages[i].link)
            bannerImage:addTouchEventListener(handler(self, self.onBannerChosed))
        end
        if #bannerImages > 1 then
            self:startBannnerAni()
        end
        self._panelBanners:setVisible(true)
    else
        self._panelBanners:setVisible(false)
    end
end

-- Banner点击事件
function ShopView:onBannerChosed(send, event)
    if event ~= ccui.TouchEventType.ended then
        return
    end
    local spiltDatas = string.split(send:getName(), "-")
    if spiltDatas[1] and spiltDatas[2] then
        if spiltDatas[1] == "url" then
            cc.Application:getInstance():openURL(spiltDatas[2])
        elseif spiltDatas[1] == "view" then
            XH.viewManager:openView(spiltDatas[2])
        end
    end
end

-- Banner移动处理
function ShopView:bannersMoveOrRfresh(positionMove, positionFresh)
    if self._panelBanners then
        local panelChildren = self._panelBanners:getChildren()
        local size = self._panelBanners:getContentSize()
        if panelChildren and #panelChildren > 1 then
            if positionMove then
                self._bannerAniMoveTimes = self._bannerAniMoveTimes + 1
                for i = 1, 2 do
                    local getPanelNums = self._bannerAniMoveTimes %(#panelChildren) + i - 1
                    local moveBannerModel = panelChildren[getPanelNums] or panelChildren[#panelChildren]
                    if moveBannerModel then
                        if positionMove then
                            moveBannerModel:runAction(cc.MoveTo:create(1, cc.p(moveBannerModel:getPositionX() - size.width, size.height / 2)))
                        end
                    end
                end
            elseif positionFresh then
                local needRefreshPos = self._bannerAniMoveTimes %(#panelChildren)
                local hadMoveBanner = panelChildren[needRefreshPos] or panelChildren[#self._panelBanners:getChildren()]
                if hadMoveBanner then
                    hadMoveBanner:setPosition(cc.p(size.width / 2 + size.width, size.height / 2))
                end
            end
        end
    end
end

-- 设置Banner图动画
function ShopView:startBannnerAni()
    self._bannerAniMoveTimes = 0
    self:removeBannerAni()
    local moveFunc = cc.CallFunc:create(function()
        self:bannersMoveOrRfresh(true)
    end)
    local delayAct = cc.DelayTime:create(BANNER_ANI_DELAY_TIME)
    -- 判断是否刷新位置
    local positionRefresh = cc.CallFunc:create(function()
        self:bannersMoveOrRfresh(false, true)
    end)
    local sequenceAct = cc.Sequence:create(moveFunc, delayAct, positionRefresh)
    local repeatAct = cc.RepeatForever:create(sequenceAct)
    self._isBannerAction:runAction(repeatAct)
end

function ShopView:removeBannerAni()
    if self._isBannerAction then
        self._isBannerAction:stopAllActions()
    end
end

-- 设置活动角标
function ShopView:setActJiaoBiao(item, index, id)
    if ShopConfig.JiaoBiao[index] then
        XH.UITool.ignoreContentAdaptWithSize(item, KW_IMAGE_FLAG, true)
        XH.UITool.loadTexture(item, KW_IMAGE_FLAG, ShopConfig.JiaoBiaoBase .. ShopConfig.JiaoBiao[index].iconName .. ".png", ccui.TextureResType.plistType)
        XH.UITool.setPositionY(item, KW_IMAGE_FLAG, ShopConfig.JiaoBiao[index].posY or ShopConfig.DefaultPos.posY)
        XH.UITool.setPositionX(item, KW_IMAGE_FLAG, ShopConfig.JiaoBiao[index].posX or ShopConfig.DefaultPos.posX)
        XH.UITool.setVisible(item, KW_IMAGE_FLAG, true)
    else
        XH.UITool.setVisible(item, KW_IMAGE_FLAG, false)
    end
    if id and self._lastBuy then
        local bLastBuy
        for _, v in pairs(self._lastBuy) do
            if tonumber(v) == tonumber(id) then
                bLastBuy = true
                break
            end
        end
        if bLastBuy then
            XH.UITool.setVisible(item, KW_IMAGE_LAST_BUY, true)
            XH.UITool.setText(item, KW_EXCHANGE_TEXT, "再次购买")
            XH.UITool.loadTexture(item, "KW_IMG_EXCHANGE", "shop_new_text_buy_again.png", ccui.TextureResType.plistType)
        else
            XH.UITool.setVisible(item, KW_IMAGE_LAST_BUY, false)
            XH.UITool.setText(item, KW_EXCHANGE_TEXT, "立即购买")
            XH.UITool.loadTexture(item, "KW_IMG_EXCHANGE", "shop_new_text_buy_now.png", ccui.TextureResType.plistType)
        end
    end
end

-- 初始化商品信息
function ShopView:initShopItemState(item, state)
    for i = 1, 2 do
        XH.UITool.setVisible(item, KW_IMG_CUT_LINE .. i, state)
        XH.UITool.setVisible(item, KW_TEXT_ORIGIN_PRIZE .. i, state)
    end
    XH.UITool.setVisible(item, KW_IMG_CUT_LINE, state)
    XH.UITool.setVisible(item, KW_TEXT_ORIGIN_PRIZE, state)
    XH.UITool.setVisible(item, KW_IMG_ACT_BG, state)
end

-- 设置商品活动
function ShopView:setIsActItem(item, productData)
    self:initShopItemState(item, false)
    self:setActJiaoBiao(item, productData.corner_tag, productData.id)
    if productData.multi_goods_info then
        if #productData.multi_goods_info == 1 then
            if XH.lobby:getModule("Shop"):judgeIsActOut(productData.multi_goods_info[1].goods_act, productData, productData.multi_goods_info[1]) then
                self:setItemActivity(item, productData, productData.multi_goods_info[1].goods_act)
            end
        else
            local setPrizeConfig = productData.multi_goods_info[1]
            local otherPrizeConfig = productData.multi_goods_info[2]
            if productData.multi_goods_info[1].prop_count > productData.multi_goods_info[2].prop_count then
                setPrizeConfig = productData.multi_goods_info[2]
                otherPrizeConfig = productData.multi_goods_info[1]
            end
            if setPrizeConfig.goods_act and setPrizeConfig.goods_act ~= "" and XH.lobby:getModule("Shop"):judgeIsActOut(setPrizeConfig.goods_act, productData, setPrizeConfig) then
                self:setItemActivity(item, setPrizeConfig, setPrizeConfig.goods_act)
            elseif otherPrizeConfig.goods_act and otherPrizeConfig.goods_act ~= "" and XH.lobby:getModule("Shop"):judgeIsActOut(otherPrizeConfig.goods_act, productData, otherPrizeConfig) then
                self:setItemActivity(item, otherPrizeConfig, otherPrizeConfig.goods_act)
            elseif XH.lobby:getModule("Shop"):judgeIsActOut(setPrizeConfig.goods_act, productData, setPrizeConfig) then
                self:setItemActivity(item, setPrizeConfig, setPrizeConfig.goods_act)
            end
        end
    elseif XH.lobby:getModule("Shop"):judgeIsActOut(productData.goods_act, productData) then
        self:setItemActivity(item, productData, productData.goods_act)
    end
end

-- 设置砍价横线长度
function ShopView:setCutSize(item, cutName, fontSize, posName, prizeName, isOnlyPrize)
    local cutImage = XH.UITool.seekNodeByName(item, cutName)
    local cutSize = #tostring(fontSize)
    if cutImage then
        XH.UITool.setContentSize(item, cutName, KW_DEAFULT_CUT_LENGTH * cutSize, cutImage:getContentSize().height)
        --XH.UITool.setPositionX(item, posName, cutImage:getContentSize().width / 2)
        ---- 单道具商品和多道具商品设置锚点不同
        --if isOnlyPrize then
        --    local getPrizeContentSize = XH.UITool.getContentSize(item, prizeName)
        --    XH.UITool.setPositionX(item, cutName, getPrizeContentSize.width / 2)
        --else
        --    local getPositionX = XH.UITool.getPositionX(item, prizeName)
        --    XH.UITool.setPositionX(item, cutName, getPositionX)
        --end
    end
end

-- 多道具文案设置
function ShopView:setActTextItems(item, goodsAct, productGoods)
    local disCount = 100
    for i = 1, #productGoods do
        for j = 1, #goodsAct.act_config[goodsAct.act_type] do
            if goodsAct.act_config[goodsAct.act_type][j].type == productGoods[i].type and goodsAct.act_config[goodsAct.act_type][j].checked == "1" then
                local nowPrize = self:setMoreThanPrize(goodsAct.act_config[goodsAct.act_type][i].value)
                local originPrize = self:setMoreThanPrize(productGoods[i].value)
                XH.UITool.setText(item, KW_TXT_ITEMS_PRICE .. i, nowPrize)
                XH.UITool.setText(item, KW_TEXT_ORIGIN_PRIZE .. i, originPrize)
                XH.UITool.setVisible(item, KW_TEXT_ORIGIN_PRIZE .. i, true)
                self:setCutSize(item, KW_IMG_CUT_LINE .. i, originPrize, KW_TEXT_ORIGIN_PRIZE .. i, KW_TXT_ITEMS_PRICE .. i)
                XH.UITool.setVisible(item, KW_IMG_CUT_LINE .. i, true)
                if goodsAct.act_type == ShopConfig.ProductAct.DISCOUNT then
                    XH.UITool.setText(item, KW_TXT_ITEMS_PRICE .. i, tonumber(nowPrize) * tonumber(originPrize) / 100)
                    if disCount > tonumber(nowPrize) then
                        disCount = tonumber(nowPrize)
                    end
                end
            elseif goodsAct.act_config[goodsAct.act_type][j].type == productGoods[i].type then
                local originPrize = self:setMoreThanPrize(productGoods[i].value)
                XH.UITool.setText(item, KW_TXT_ITEMS_PRICE .. i, originPrize)
                XH.UITool.setText(item, KW_TEXT_ORIGIN_PRIZE .. i, originPrize)
            end
        end
    end
    if goodsAct.act_type == ShopConfig.ProductAct.DISCOUNT then
        XH.UITool.setText(item, KW_TEXT_ACT_CONTENT, "折扣：" .. disCount .. "%")
    end
end

-- 单道具文案设置
function ShopView:setAloneActTextItem(item, originPrize, nowPrize, goodsAct)
    XH.UITool.setText(item, KW_TXT_PRICE, nowPrize)
    XH.UITool.setText(item, KW_TEXT_ORIGIN_PRIZE, originPrize)
    if goodsAct.act_type == ShopConfig.ProductAct.DISCOUNT then
        local disCountPrize = tonumber(originPrize) * tonumber(nowPrize) / 100
        XH.UITool.setText(item, KW_TEXT_ACT_CONTENT, "折扣：-" .. nowPrize .. "%")
        XH.UITool.setText(item, KW_TXT_PRICE, disCountPrize)
        XH.UITool.setText(item, KW_TEXT_ORIGIN_PRIZE, originPrize)
    end
    XH.UITool.setVisible(item, KW_TEXT_ORIGIN_PRIZE, true)
    self:setCutSize(item, KW_IMG_CUT_LINE, originPrize, KW_TEXT_ORIGIN_PRIZE, KW_TXT_PRICE, true)
    XH.UITool.setVisible(item, KW_IMG_CUT_LINE, true)
end

-- 活动文案设置
function ShopView:setActItemCut(item, productData, goodsAct)
    if productData.multi_goods then
        self:setActTextItems(item, goodsAct, productData.multi_goods)
    else
        if productData.multi_score_config and #productData.multi_score_config > 1 then
            self:setActTextItems(item, goodsAct, productData.multi_score_config)
        elseif productData.multi_score_config and #productData.multi_score_config == 1 then
            for j = 1, #goodsAct.act_config[goodsAct.act_type] do
                if goodsAct.act_config[goodsAct.act_type][j].type == productData.multi_score_config[1].type and goodsAct.act_config[goodsAct.act_type][j].checked == "1" then
                    self:setAloneActTextItem(item, productData.multi_score_config[1].value, goodsAct.act_config[goodsAct.act_type][j].value, goodsAct)
                end
            end
        else
            self:setAloneActTextItem(item, productData.condition, goodsAct.act_config[goodsAct.act_type].value or goodsAct.act_config[goodsAct.act_type], goodsAct)
        end
    end
end

-- 活动设置
function ShopView:setItemActivity(item, productData, goodsAct)
    XH.UITool.setVisible(item, KW_IMG_ACT_BG, true)
    XH.UITool.setTextColor(item, KW_TEXT_ACT_CONTENT, cc.c3b(113, 21, 228))
    if goodsAct.act_type == ShopConfig.ProductAct.FIRST then
        XH.UITool.setText(item, KW_TEXT_ACT_CONTENT, goodsAct.act_config.description)
        self:setActItemCut(item, productData, goodsAct)
    elseif goodsAct.act_type == ShopConfig.ProductAct.PROMOTION then
        XH.UITool.setTextColor(item, KW_TEXT_ACT_CONTENT, cc.c3b(188, 22, 22))
        if goodsAct.act_config and tonumber(goodsAct.act_config.left_num) < KW_DEFAULT_LEST_SHOPS then
            XH.UITool.setText(item, KW_TEXT_ACT_CONTENT, "剩余：" .. goodsAct.act_config.left_num .. "件")
        else
            local time, isShowState = self:setItemContentWithTimestamp(goodsAct.promote_end)
            XH.UITool.setText(item, KW_TEXT_ACT_CONTENT, "剩余：" .. time)
            XH.UITool.setVisible(item, KW_IMG_ACT_BG, isShowState)
        end
        self:setActItemCut(item, productData, goodsAct)
    elseif goodsAct.act_type == ShopConfig.ProductAct.DISCOUNT then
        XH.UITool.setText(item, KW_TEXT_ACT_CONTENT, "折扣")
        self:setActItemCut(item, productData, goodsAct)
    elseif goodsAct.act_type == ShopConfig.ProductAct.SEND then
        XH.UITool.setText(item, KW_TEXT_ACT_CONTENT, "额外赠送" .. goodsAct.act_config[goodsAct.act_type].value .. goodsAct.act_config[goodsAct.act_type].prop_name)
    end
end

-- 设置图片来源
function ShopView:setItemImageOrign(item, productData, index)
    XH.UITool.setVisible(item, KW_IMG_PRODUCT_IMG, false)
    XH.UITool.ignoreContentAdaptWithSize(item, KW_IMG_BUY_ICO, true)
    XH.UITool.ignoreContentAdaptWithSize(item, KW_IMG_PRODUCT_IMG, true)
    local pruductSpecial = XH.lobby:getModule("Shop"):getSecondTagsWithTag(self._displayProductType, self._displaySecondType)
    if ShopConfig.ProductIcon and pruductSpecial.tag then
        local productIcon = nil
        if ShopConfig.ProductDefaultIcon[pruductSpecial.tag] then
            productIcon = string.format(ShopConfig.ProductDefaultIcon[pruductSpecial.tag], index < 5 and index or 4)
        elseif ShopConfig.ProductIconLocal[XH.areaData:getAreaID()] and ShopConfig.ProductIcon[pruductSpecial.tag] then
            productIcon = string.format(ShopConfig.ProductIcon[pruductSpecial.tag], index < 5 and index or 4)
        end
        if productIcon then
            XH.UITool.loadTexture(item, KW_IMG_PRODUCT_IMG, productIcon, ccui.TextureResType.plistType)
            self:AdaptWithSize(item)
            return
        end
    end
    self:downloadImage(item, productData)
end

-- 下载图片
function ShopView:downloadImage(item, productData)
    if productData and productData.image then
        XH.UITool.setVisible(item, KW_IMG_PRODUCT_IMG, false)
        local downloadImage = require("app.Req.DownloadImage")
        local reqDownloadImage = downloadImage:new()
        reqDownloadImage:addReqCallBack(self, function(node, cell, type)
            if type == XH.Req.TYPE.SUCCESS then
                local imageName = XH.StringTool.getImageNameByUrl(productData.image)
                local imagePath = DEFAULT_DOWNLOAD_IMAGE_DIR .. imageName
                XH.UITool.loadTexture(item, KW_IMG_PRODUCT_IMG, imagePath, ccui.TextureResType.localType)
                if self.AdaptWithSize then
                    self:AdaptWithSize(item)
                end
            end
        end )
        reqDownloadImage:setUrl(productData.image, false)
    end
end

-- 设置ITEM界面显示
function ShopView:setProductItemVisible(item, isShow)
    XH.UITool.setVisible(item, KW_NODE_PRICE_CHOOSE, isShow)
    XH.UITool.setVisible(item, KW_PANEL_PRICE_ALONE, not isShow)
end

-- 单道具商品界面展示
function ShopView:setOneItemShow(item, prize, image, isFit)
    local itemBuyIco = XH.UITool.seekNodeByName(item, KW_IMG_BUY_ICO)
    local itemBuyBg = XH.UITool.seekNodeByName(item, KW_PANEL_DEFAULT_SIZE)
    if image and itemBuyIco and itemBuyBg then
        itemBuyIco:loadTexture(image, ccui.TextureResType.plistType)
        if isFit ~= false then
            local getFitSize = XH.SysTool.getFitSizeWithDefault(itemBuyIco:getContentSize(), KW_DEFAULT_ITEM_ICO_SIZE)
            itemBuyBg:setScale(getFitSize)
        else
            local getFitSize = XH.SysTool.getFitSizeWithDefault(itemBuyBg:getContentSize(), KW_DEFAULT_ITEM_ICO_SIZE)
            itemBuyBg:setScale(getFitSize)
        end
    end
    XH.UITool.setText(item, KW_TXT_PRICE, prize)
    self:setProductItemVisible(item, false)
end

-- 商品>10000情况下转换
function ShopView:setMoreThanPrize(prize)
    if tonumber(prize) >= 10000 then
        prize = tonumber(prize) / 10000 .. "万"
    end
    return prize
end

-- 多道具商品价格显示
function ShopView:setMoreItemsPrize(item, prizeConfig)
    table.sort(prizeConfig, function(v1, v2)
        return tonumber(v1.value) < tonumber(v2.value)
    end )
    for i = 1, #prizeConfig do
        XH.UITool.setText(item, KW_TXT_ITEMS_PRICE .. i, self:setMoreThanPrize(prizeConfig[i].value))
        local imagePath = XH.lobby:getModule("Shop"):getMoreItemsShopPropImage(prizeConfig[i].type)
        if imagePath then
            XH.UITool.loadTexture(item, KW_IMG_LOGO .. i, imagePath, ccui.TextureResType.plistType)
            XH.UITool.ignoreContentAdaptWithSize(item, KW_IMG_LOGO .. i, true)
        end
    end
    self:setProductItemVisible(item, true)
end

-- 设置商品价格与图片显示
-- 单道具单规格 单道具多规格 多道具多规格
-- 目前支持上限为2个
function ShopView:setItemPrizeImage(item, productData)
    if productData.multi_score_config and #productData.multi_score_config > 0 then
        if productData.multi_goods_info == nil then
            if #productData.multi_score_config == 1 then
                local imagePath = XH.lobby:getModule("Shop"):getMoreItemsShopPropImage(productData.multi_score_config[1].type)
                self:setOneItemShow(item, tonumber(productData.multi_score_config[1].value), imagePath)
            elseif #productData.multi_score_config == 2 then
                self:setMoreItemsPrize(item, productData.multi_score_config)
            end
        else
            if #productData.multi_goods_info == 1 then
                self:setMoreItemsPrize(item, productData.multi_goods_info[1].multi_goods)
            elseif #productData.multi_goods_info == 2 then
                local setPrizeConfig = productData.multi_goods_info[1].multi_goods
                if productData.multi_goods_info[1].prop_count > productData.multi_goods_info[2].prop_count then
                    setPrizeConfig = productData.multi_goods_info[2].multi_goods
                end
                self:setMoreItemsPrize(item, setPrizeConfig)
            end
        end
    else
        local pruductSpecial = XH.lobby:getModule("Shop"):getSecondTagsWithTag(self._displayProductType, self._displaySecondType)
        if pruductSpecial.tag == ShopConfig.SpecialTag.DIAMOND then
            local propImage = ShopConfig.ProductDetailIcon.rmb
            self:setOneItemShow(item, tonumber(productData.price), propImage, false)
        else
            local propImage = XH.lobby:getModule("Shop"):getShopPropImage(productData.cur_shop_propid)
            self:setOneItemShow(item, tonumber(productData.condition), propImage)
        end
    end
end

-- 设置各个item动画
function ShopView:onShowItemWithAction(item, productData)
    self:clearAni(item)
    if XH.lobby:getModule("Shop"):getTagWithShop(productData, ShopConfig.ItemsModifyKey.YPQ) then
        self:showPropsDargonAni(item, ShopConfig.YaPaiQiAni)
        return
    end
    if XH.lobby:getModule("Shop"):getTagWithShop(productData, ShopConfig.ItemsModifyKey.FRAME) then
        self:showPropsAni(item, ShopConfig.PropsIdAni[tonumber(productData.value)], 0.8)
        return
    end
    if XH.lobby:getModule("Shop"):getTagWithShop(productData, ShopConfig.ItemsModifyKey.TABLEBG) then
        self:showPropsAni(item, ShopConfig.TableBgAni)
        return
    end
end

function ShopView:setItemBuyName(item, productData)
    local propName = XH.lobby:getModule("Shop"):getShopNameById(productData.cur_shop_propid)
    if propName == "couponID" then 
        XH.UITool.setText(item, KW_EXCHANGE_TEXT, "立即兑换")
        XH.UITool.loadTexture(item, "KW_IMG_EXCHANGE", "shop_new_text_exchange_now.png", ccui.TextureResType.plistType)
    end
end

function ShopView:onShowDiamondView()
    self:getProductTypeWithShow(ShopConfig.SpecialTag.DIAMOND)
end

function ShopView:onShowCardView()
    self:getProductTypeWithShow(ShopConfig.SpecialTag.CARD)
end

function ShopView:onShowGoldView()
    self:getProductTypeWithShow(ShopConfig.SpecialTag.GOLD)
end

-- 用新的数据重新刷新UI,UI复用
function ShopView:loadProducts(productTag)
    XH.TipTool.hideLoading()
    local productAllData = XH.lobby:getModule("Shop"):getShopItemsWithId(self._displaySecondType)
    local pruductSpecial = XH.lobby:getModule("Shop"):getSecondTagsWithTag(self._displayProductType, self._displaySecondType)
    self:judgeNoticeBytag()
    if productAllData then
        if productTag and productTag ~= pruductSpecial.tag then
            -- 非当前页签商品不处理
            return
        end
        local productList = productAllData.goods_list or { }
        if pruductSpecial.tag == ShopConfig.SpecialTag.DIAMOND then
            productList = productAllData
        end
        if productAllData.pop_config and #productAllData.pop_config > 0 then
            self._isNeedShowBanners = true
        else
            self._isNeedShowBanners = false
        end
        local aleardCreatCount = self:getProductCount()
        local totalNeedCount = #productList
        self._nonePrizeItem:setVisible(totalNeedCount == 0)
        -- 动态初始化滑动区域大小
        if pruductSpecial.tag == ShopConfig.SpecialTag.VIP then
            self:flushItemBg(false)
            self._displayVipList:setVisible(true)
            self._displayVipList:removeAllChildren()
            self._displayList:setVisible(false)
            self._panelSxvip:setVisible(false)
            for i = 1, totalNeedCount do
                local productData = productList[i]
                local item = self:createNewVipProduct(productData, i)
                item:setZOrder(totalNeedCount - i)
                self:resetDisPlayVipContentSize(totalNeedCount, item:getContentSize())
            end
            self._displayVipList:setTouchEnabled(true)
        elseif pruductSpecial.tag == ShopConfig.SpecialTag.SXVIP then
            self._itemsBannerBg:setVisible(false)
            self._itemsNormalBg:setVisible(false)
            self._displayVipList:setVisible(false)
            self._displayList:setVisible(false)
            self._panelSxvip:removeAllChildren()
            self._panelSxvip:setVisible(true)
            -- 添加时效会员item
            local listSize = self._displayVipList:getContentSize()
            local sxvipShopItem = SxvipShopItem.new()
            sxvipShopItem:refreshView("SHOP")
            local size = sxvipShopItem:getContentSize()
            self._panelSxvip:addChild(sxvipShopItem)
        else
            self._displayVipList:setVisible(false)
            self._panelSxvip:setVisible(false)
            self._displayList:setVisible(true)
            self:resetDisPlayContentSize(totalNeedCount)
            -- 创建商品UI
            self._lastBuy = {
                XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_LAST_BUY_PRODUCT_ID_DIAMOND, 0),
                XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_LAST_BUY_PRODUCT_ID_CARD, 0),
                XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_LAST_BUY_PRODUCT_ID_GOLD, 0),
            }
            -- self:clear()
            -- self._curIndex = 1
            -- self._totIndex = totalNeedCount
            -- local update = function()
            --     if self._curIndex <= self._totIndex then
            --         local item = self:createNewProduct()
            --         item:setZOrder(self._totIndex - self._curIndex)
            --         self:updateItemUI(item, productList[self._curIndex], self._curIndex, true)
            --     else
            --         if self._createProductTimer then
            --             cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._createProductTimer)
            --         end
            --     end
            --     self._curIndex = self._curIndex + 1
            -- end
            -- if not self._createProductTimer then
            --     self._createProductTimer = cc.Director:getInstance():getScheduler():scheduleScriptFunc(update, 0, false)
            -- end
            for i = 1, totalNeedCount do
                local isFirst = false
                local productData = productList[i]
                local item = self:getProductByIndex(i)
                if not item then
                    isFirst = true
                    item = self:createNewProduct(productData, i)
                end
                item:setZOrder(totalNeedCount - i)
                self:updateItemUI(item, productData, i, isFirst)
            end
            -- 隐藏多余的item
            for i = totalNeedCount + 1, aleardCreatCount do
                local item = self:getProductByIndex(i)
                if item then
                    item:setVisible(false)
                end
            end
        end
        -- 设置是否展示BANNER
        self:isShowBanners()
        if pruductSpecial.tag == ShopConfig.SpecialTag.FUQI then
            self._lingQiLabel:setVisible(true)
            self._linQiAniImg:setVisible(true)
        else
            self._lingQiLabel:setVisible(false)
            self._linQiAniImg:setVisible(false)
        end
    end
end

local TabTextDefaultColor = cc.c3b(112, 63, 15)
local TabTextSelectedColor = cc.c3b(255, 255, 255)
function ShopView:setTabStateChanged()
    for _, btn in pairs(self._leftTabBtns) do
        local nType = XH.StringTool.getLastCharAndToNum(btn:getName())
        local isSelect = nType == self._displayProductType
        btn:setTouchEnabled(not isSelect)
        local color = isSelect and TabTextSelectedColor or TabTextDefaultColor
        -- local fontSize = isSelect and 52 or 46
        XH.UITool.setVisible(btn, KW_IMG_CHOSE_STATE, isSelect)
        -- XH.UITool.setFontSize(btn, KW_TXT_PRODUCT_NAME, fontSize)
        XH.UITool.setTextColor(btn, KW_TXT_PRODUCT_NAME, color)
        XH.UITool.setColor(btn, "KW_IMG_PRODUCT_NAME", color)
    end
end

function ShopView:setSecondTabStateChanged()
    for _, btn in pairs(self._upSecendTabBtns) do
        local nType = XH.StringTool.getLastCharAndToNum(btn:getName())
        local isSelect = tonumber(nType) == self._displaySecondType
        btn:setTouchEnabled(not isSelect)
        XH.UITool.setVisible(btn, KW_IMG_SECOND_CHOSE, isSelect)
        XH.UITool.setVisible(btn, KW_TEXT_SECOND_CHOSE_TITLE, isSelect)
        XH.UITool.setVisible(btn, KW_TEXT_SECOND_TITLE, not isSelect)
    end
    XH.lobby:getModule("Shop"):judgeIsNewShopData(self._displaySecondType)
    self:updateMenuRedPoint()
end

------------------事件处理------------------------
-- 左边tab切换事件处理
function ShopView:onTabChanged(send, event)
    if event ~= ccui.TouchEventType.ended then
        return
    end
    self._displayProductType = XH.StringTool.getLastCharAndToNum(send:getName())
    XH.lobby:getModule("PropEventTracker"):track(XH.NewThrowDataDefine.dj24080867, {type = self._displayProductType})
    self:setTabStateChanged(send)
    local secondProductLists = XH.lobby:getModule("Shop"):getFirstTagsWithTag(self._displayProductType)
    if secondProductLists.sub and #secondProductLists.sub > 0 then
        self._displaySecondType = secondProductLists.sub[1].id
        XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.SHOP_LAYER_BUY_PRODUCT_LEFT,{buyName = secondProductLists.title})
        if secondProductLists.tag =="rec" then 
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_2)
        elseif secondProductLists.tag=="zs" then
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_5)
        elseif secondProductLists.tag=="fk" then
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_6)
        elseif secondProductLists.tag=="jb" then
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_7)
        elseif secondProductLists.title=="活动礼包" then
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_8)
        elseif secondProductLists.tag=="vip" then
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_9)
        elseif secondProductLists.title=="记牌|洗牌" or secondProductLists.title=="记牌器" then
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_10)
        elseif secondProductLists.title=="装扮道具" then
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_11)
        end
    end
    self:show()
end

function ShopView:onSecondTabChanged(send, event)
    if event ~= ccui.TouchEventType.ended then
        return
    end
    
    self._displaySecondType = XH.StringTool.getLastCharAndToNum(send:getName())
    local secondMenu = XH.lobby:getModule("Shop"):getFirstTagsWithTag(self._displayProductType)
    self:SecondTitleThrowdata(self._displaySecondType,secondMenu)
    self:setSecondTabStateChanged(send)
    self:show()
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.SHOP_LAYER_BUY_PRODUCT_TOP,{tabName = self._displaySecondType})
end

function ShopView:SecondTitleThrowdata(Scond_id,secondMenu)
    if secondMenu.sub then
        for _,v in pairs(secondMenu.sub) do
            if v.id == Scond_id and v.title =="最新上架" then
                XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_3)
                return
            elseif v.id == Scond_id and v.title =="热门购买" then
                XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_4)
                return
            elseif  v.id == Scond_id and v.title =="压牌器" then
                XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_12)
                return
            elseif v.id == Scond_id and v.title =="头像框" then
                XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_13)
                return
            elseif v.id == Scond_id and v.title =="牌背" then
                XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_14)
                return
            elseif v.id == Scond_id and v.title =="桌布" then
                XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_15)
                return
            elseif v.id == Scond_id and v.title =="入场动画" then
                XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_16)
                return
            elseif v.id == Scond_id and v.title =="记牌器" then
                XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_18)
                return
            elseif v.id == Scond_id and v.title =="洗牌券" then
                XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_19)
                return
            elseif v.id == Scond_id and v.title =="全部" then
                XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_20)
                return
            elseif v.id == Scond_id and v.title =="特惠礼包" then
                XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_21)
                return
            end
        end
    end    
    

end
-- 关闭按钮
function ShopView:onTouchEventClose(send, event)
    self:hide()
end

-- 背包按钮
function ShopView:onTouchEventOpenBagLayer(send, event)
    cc.UserDefault:getInstance():setIntegerForKey("IS_FIRST_CLICK_BAG_BTN", 1)
    self:hide()
    XH.viewManager:openView("BagMainViewNew")
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.SHOPPING_CENTER_1) 
end

-- 移除数据监听消息
function ShopView:hide()
    self:removeBannerAni()
    XH.lobby:getModule("H5GameWidget"):close5GameWidget("shop")
    self:close()
end

-- 单道具购买处理
function ShopView:onBuyBtnAloneEvent(send, curPropType, idx)
    local productLists = XH.lobby:getModule("Shop"):getShopItemsWithId(self._displaySecondType).goods_list
    local isCutLineVisible = XH.UITool.seekNodeByName(send:getParent(), KW_IMG_CUT_LINE)

    
    local couponList = XH.lobby:getModule("BagSysNew"):dealCouponData()
    --筛选当前商品是否有符合条件的可用优惠券，没有的话则走原来流程
    if next(couponList) and productLists and productLists[idx] then
        couponList = XH.lobby:getModule("CouponSystem"):selectAvalibleCoupon(couponList,productLists[idx])
    end
    local buyCallback = function()
        --todo
        if productLists and productLists[idx] then
            if XH.lobby:getModule("Shop"):getTagWithShop(productLists[idx], ShopConfig.SpecialTag.GIFT) then
                if isCutLineVisible and isCutLineVisible:isVisible() then
                    XH.lobby:getModule("Shop"):showGiftContent(curPropType, idx, self._displaySecondType, XH.UITool.getText(send:getParent(), KW_TXT_PRICE))
                else
                    XH.lobby:getModule("Shop"):showGiftContent(curPropType, idx, self._displaySecondType)
                end
                return
            end
        end
    
        local isHave
        local tipMsg
        local isHideTips
        if isCutLineVisible and isCutLineVisible:isVisible() then
            isHave, tipMsg, isHideTips = XH.lobby:getModule("Shop"):isHaveEnoughDiamond(curPropType, idx, XH.UITool.getText(send:getParent(), KW_TXT_PRICE))
        else
            isHave, tipMsg, isHideTips = XH.lobby:getModule("Shop"):isHaveEnoughDiamond(curPropType, idx)
        end
        if isHave then
            XH.lobby:getModule("Shop"):reqBuyProduct(curPropType, idx, nil, XH.UITool.getText(send:getParent(), KW_TXT_PRICE))
            return
        else
            if not isHideTips then
                XH.lobby:getModule("Shop"):sendCloseCouponView()
                XH.TipTool.showTip( {
                    type = XH.TIP_LAYER_TYPE.OK_CANCEL_NO_CLOSE,
                    funcOK = function()
                        if tipMsg == "ERROR_SHOP_ROOM_CARD_NOT_ENOUGH" then
                            self:getProductTypeWithShow(ShopConfig.SpecialTag.CARD)
                        else
                            self:getProductTypeWithShow(ShopConfig.SpecialTag.DIAMOND)
                        end
                    end
                } , tipMsg)
            end
        end
    end
    if next(couponList) then
        XH.viewManager:openView("CouponSystemView",nil,productLists[idx],couponList,buyCallback)
    else
        buyCallback()
    end
end

-- 获取页签名跳转
function ShopView:getProductTypeWithShow(tagName)
    local firstTag, secondTag = XH.lobby:getModule("Shop"):getTagsWithName(tagName)
    if firstTag and secondTag then
        self._displayProductType = firstTag
        self._displaySecondType = secondTag
        self:show()
    end
end

-- 预览按钮处理（目前仅针对礼包）
function ShopView:onExchangeLookBtnTouchEvent(send, event)
    self:onBtnTouchEvent(send, event)
    if event ~= ccui.TouchEventType.ended or not send then
        return
    end
    local idx = send:getTag()
    local productLists = XH.lobby:getModule("Shop"):getShopItemsWithId(self._displaySecondType).goods_list
    local curPropType = self._displaySecondType
    local isCutLineVisible = XH.UITool.seekNodeByName(send:getParent(), KW_IMG_CUT_LINE)
    if productLists and productLists[idx] then
        if XH.lobby:getModule("Shop"):getTagWithShop(productLists[idx], ShopConfig.SpecialTag.GIFT) then
            if isCutLineVisible and isCutLineVisible:isVisible() then
                XH.lobby:getModule("Shop"):showGiftContent(curPropType, idx, self._displaySecondType, XH.UITool.getText(send:getParent(), KW_TXT_PRICE))
            else
                XH.lobby:getModule("Shop"):showGiftContent(curPropType, idx, self._displaySecondType)
            end
        end
    end
end

-- 兑换按钮处理
function ShopView:onExchangeBtnTouchEvent(send, event)
    self:onBtnTouchEvent(send, event)
    if event ~= ccui.TouchEventType.ended or not send then
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
    local tmpText = XH.UITool.getText(send, "KW_EXCHANGE_TEXT")
    if tmpText == "再次购买" then
        XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.SHOP_BUY_AGAIN)
    end

    local idx = send:getTag()
    local curPropType = self._displaySecondType
    local dataLists = XH.lobby:getModule("Shop"):getShopItemsWithId(self._displaySecondType)
    local getShopTag = XH.lobby:getModule("Shop"):getSecondTagsWithTag(self._displayProductType, self._displaySecondType)
    
    if dataLists then   --商城点击购买相应道具ID埋点
        local tmpData ={}
        if dataLists[idx] then
            tmpData.propID=dataLists[idx].id
        else
            if dataLists.goods_list and dataLists.goods_list[idx] then
                tmpData.propID=dataLists.goods_list[idx].id
            end
        end
        XH.throwDataManager:throwData(XH.ThrowDataDefine.SHOPPING_CENTER_17, tmpData)
    end
    
    local costInfo = {}
    if getShopTag and getShopTag.tag == ShopConfig.SpecialTag.DIAMOND then  
        if XH.playerData:checkNewRealNameVisitor() ~= 0 then return end--游客登陆
		local areaString = XH.areaData:getAreaName()
		if XH.ChannelTool.checkIsWaishengChannel() then
			areaString = "全国"
		end
		local tipStr = "是否要在 " .. areaString .. " 大区充值钻石？"
		local props = XH.lobby:getModule("Shop"):getShopItemsWithId(curPropType)
		if props and props[idx] then
			tipStr = "是否要在 " .. areaString .. " 大区花 " .. tonumber(props[idx].price) .. " 元充值 ".. props[idx].value .. " 钻石？"
		end
		XH.TipTool.showTip( {
			type = XH.TIP_LAYER_TYPE.OK,
			funcOK = function()
				XH.lobby:getModule("Shop"):reqBuyDiamondProduct(curPropType, idx)
			end
		} , tipStr)
        costInfo = {propid=props[idx].propid, cnt=tonumber(props[idx].value), costPropid=-1}
    elseif getShopTag and getShopTag.tag == ShopConfig.SpecialTag.FUQI then
        local userRoomCard = XH.playerData:getRoomCard()
        local freeRoomCard = XH.playerData:getFreeRoomCard()
        if userRoomCard + freeRoomCard < dataLists.goods_list[idx].condition then
            XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK_CANCEL,
            funcOK = function()
                self:getProductTypeWithShow(ShopConfig.SpecialTag.CARD)
            end
        } , "您的道具不足，是否前往购买？")
        end
        costInfo = {propid=dataLists.goods_list[idx].name, cnt=dataLists.goods_list[idx].condition, costPropid=dataLists.goods_list[idx].cur_shop_propid}
    else
        if dataLists and dataLists.goods_list and dataLists.goods_list[idx] then
            local info = dataLists.goods_list[idx]
            costInfo = {propid=info.name, cnt=tonumber(info.condition), costPropid=info.cur_shop_propid}
            if info and info.multi_score_config and #info.multi_score_config > 0 then
                costInfo.cnt = tonumber(info.multi_score_config[1].value)
                costInfo.costPropid = string.match(info.multi_score_config[1].type, "%d+")
            end
            if dataLists.goods_list[idx].multi_score_config and #dataLists.goods_list[idx].multi_score_config > 1 then
                XH.viewManager:openView("ShopChooseView", nil, dataLists.goods_list[idx], curPropType)
            else
                self:onBuyBtnAloneEvent(send, curPropType, idx)
            end
        end
    end
    XH.lobby:getModule("PropEventTracker"):track(XH.NewThrowDataDefine.dj24080868, {costInfo = costInfo})
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.SHOP_LAYER_BUY_PRODUCT,{shopTag = getShopTag.tag,shopIndex = idx})
end

-- 刷新界面消息
function ShopView:onRefresh(event)
    XH.TipTool.hideLoading()
    local data = event and event.data or nil
    local specialTag = data and data.tag or nil
    self:flushTopInfo()
    self:loadProducts(specialTag)
    if data and data.isTip == true then
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK,
        } , data.msg)
    end
end

-- 请求系统时间
function ShopView:RequestSysTime()
    XH.lobby:getModule("Shop"):reqSystemTimes()
end

-- 依赖时间相关的展示函数
function ShopView:setItemContentWithTimestamp(deadTime, deadState)
    local deadStateIn = true
    local osTime = XH.lobby:getModule("Shop"):getSystemTime()
    local overdueTime = deadTime - osTime
    local overdueTime_D = math.floor(overdueTime / ShopConfig.HourSet.Day)
    local overdueTime_H = math.floor(overdueTime % ShopConfig.HourSet.Day / ShopConfig.HourSet.Hour)
    local overdueTime_M = math.floor(overdueTime % ShopConfig.HourSet.Hour / ShopConfig.HourSet.Minute)
    --local overdueTime_S = math.floor(overdueTime % ShopConfig.HourSet.Hour % ShopConfig.HourSet.Minute)
    local string = overdueTime_D .. "天" -- .. overdueTime_H .. "小时"
    if overdueTime_D >= KW_YEAR then
        deadStateIn = false
    end
    if overdueTime < ShopConfig.HourSet.Day then
        string = overdueTime_H .. "小时" -- .. overdueTime_M .. "分钟"
    end
    if overdueTime < ShopConfig.HourSet.Hour then
        string = overdueTime_M .. "分钟" -- .. overdueTime_S .. "秒"
    end
    if overdueTime < ShopConfig.HourSet.Minute then
        string = "1分钟" -- .. overdueTime_S .. "秒"
    end
    return string, deadStateIn
end

function ShopView:initBagBtnVisible()
    if not self._btnBag then 
        return
    end
    self._btnBag:setVisible(XH.lobby:getModule("BagSysNew"):isSupportBag())
    self._btnBag:stopAllActions()
    local isFirstClickBag = cc.UserDefault:getInstance():getIntegerForKey("IS_FIRST_CLICK_BAG_BTN", 0)
    if isFirstClickBag == 0 then
        self._btnBag:runAction(cc.RepeatForever:create(cc.Sequence:create(cc.DelayTime:create(2), cc.RotateTo:create(0.05, 20), cc.RotateTo:create(0.1, -20), cc.RotateTo:create(0.1, 20), cc.RotateTo:create(0.05, 0))))
    end
end

function ShopView:AdaptWithSize(item)
    local sender = XH.UITool.seekNodeByName(item, KW_IMG_PRODUCT_IMG)
    local imgBg = XH.UITool.seekNodeByName(item, KW_IMG_PRODUCT_IMG_BG)
    if sender and imgBg then
        local bgSize = imgBg:getContentSize()
        sender:runAction(cc.Sequence:create(cc.DelayTime:create(0.05), cc.CallFunc:create( function()
            local width = sender:getContentSize().width
            local height = sender:getContentSize().height
            local scaleNum = width
            if width < height then
                scaleNum = height
            end
            local scale = bgSize.width / scaleNum
            if scale > 1 then
                scale = 1
            end
            sender:setScale(scale)
            sender:setVisible(true)
        end )))
    end
end

function ShopView:onBtnTouchEvent(send, eventType)
    if eventType == ccui.TouchEventType.began then
        send:setScale(0.9)
        send:setColor(cc.c3b(160, 160, 160))
    elseif eventType == ccui.TouchEventType.ended or ccui.TouchEventType.canceled then
        send:setScale(1.0)
        send:setColor(cc.c3b(255, 255, 255))
    end
end

function ShopView:onTouchEventGoLinkCharge(send, eventType)
    local targetPlatform = cc.Application:getInstance():getTargetPlatform()
    if cc.PLATFORM_OS_ANDROID == targetPlatform then
        cc.Application:getInstance():openURL(UrlConf.HTTP_ID_CHONG_ZHI)
    else
        cc.Application:getInstance():openURL(UrlConf.HTTP_ID_CHONG_ZHI_IOS)
    end
end

function ShopView:onTouchVipNotice(send, eventType)
    XH.viewManager:openView("VipNoticeLayer")
end

function ShopView:clearAni(itemNode)
    local aniNode = XH.UITool.seekNodeByName(itemNode, KW_LAYOUT_ANI_POS)
    if aniNode then
        aniNode:removeAllChildren()
    end
    XH.UITool.setVisible(itemNode, KW_LAYOUT_ANI_BG, false)
end

-- 道具节点龙骨动画
function ShopView:showPropsDargonAni(itemNode, aniConfig)
    local aniNode = XH.UITool.seekNodeByName(itemNode, KW_LAYOUT_ANI_POS)
    if aniNode and aniConfig then
        local propAni = display.playDargonBonesAnimByTimes(aniConfig, 0)
        if propAni then
            XH.UITool.setVisible(itemNode, KW_LAYOUT_ANI_BG, true)
            aniNode:addChild(propAni)
        end
    end
end

-- 道具节点动画
function ShopView:showPropsAni(itemNode, aniConfig, scale)
    scale = scale or 1
    local aniNode = XH.UITool.seekNodeByName(itemNode, KW_LAYOUT_ANI_POS)
    if aniNode and aniConfig then
        pcall( function()
            if not cc.FileUtils:getInstance():isFileExist(aniConfig.path) then
                return
            end
            ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(aniConfig.path)
            local showArmature = ccs.Armature:create(aniConfig.amatureName)
            if showArmature then
                aniNode:addChild(showArmature)
                showArmature:setAnchorPoint(cc.p(0.5, 0.5))
                showArmature:getAnimation():play(aniConfig.animationName, -1, 1)
                showArmature:setScale(scale)
                showArmature:setPositionY(ItemAniPosY)
            end
        end )
    end
end

-- 获取标签列表
function ShopView:startShowUI()
    self:initLeftTypeList()
    local shopTags = XH.lobby:getModule("Shop"):getShopAllTags()
    if shopTags and #shopTags > 0 then
        self:setDisplayTypeChanged()
        if self._defaultShopType and self._defaultShopType == ShopConfig.SpecialTag.DIAMOND then
            XH.lobby:getModule("Shop"):reqProductList(ShopConfig.ProductType.DIAMOND)
        else
            XH.lobby:getModule("Shop"):reqShopProductByConfig(self._displaySecondType)
        end
        XH.lobby:getModule("Shop"):reqIsNewShopItem()
    end
end

function ShopView:setDisplayTypeChanged()
    local shopTags = XH.lobby:getModule("Shop"):getShopAllTags()
    if shopTags and #shopTags > 0 then
        self._displayProductType = shopTags[1].id
        self._displaySecondType = shopTags[1].sub[1].id
        for i = 1, #shopTags do
            for j = 1, #shopTags[i].sub do
                if shopTags[i].sub[j].is_default_show == 1 then
                    self._displayProductType = shopTags[i].id
                    self._displaySecondType = shopTags[i].sub[j].id
                    break
                end
            end
        end
        if self._defaultShopType then
            local defaultFirst, defaultSecond = XH.lobby:getModule("Shop"):getTagsWithName(self._defaultShopType)
            if defaultFirst and defaultSecond then
                self._displayProductType = defaultFirst
                self._displaySecondType = defaultSecond
            end
        end
    end
end

-- 获取商品列表
function ShopView:onEventGetPagProducts(event)
    local specialTag = event.data.tag
    self:show(specialTag)
end

-- 目前展示商品的逻辑
function ShopView:show(productTag)
    self:initUI(productTag)
    self:setTabStateChanged()
    self:setSecondTabStateChanged()
    if XH.lobby:getModule("Shop"):getShopItemsWithId(self._displaySecondType) then
        self:loadProducts(productTag)
    else
        if self:judgeSpecialTag() then
            XH.lobby:getModule("Shop"):reqProductList(ShopConfig.ProductType.DIAMOND)
            return
        else
            XH.lobby:getModule("Shop"):reqShopProductByConfig(self._displaySecondType)
        end
    end
end

function ShopView:judgeNoticeBytag()
    local pruductSpecial = XH.lobby:getModule("Shop"):getSecondTagsWithTag(self._displayProductType, self._displaySecondType)
    self._normalNotice:setVisible(not(pruductSpecial.tag == ShopConfig.SpecialTag.VIP or pruductSpecial.tag == ShopConfig.SpecialTag.SXVIP))
    self._vipNoticePanel:setVisible(pruductSpecial.tag == ShopConfig.SpecialTag.VIP)
end

-- 各类特殊标签处理情况
function ShopView:judgeSpecialTag()
    local pruductSpecial = XH.lobby:getModule("Shop"):getSecondTagsWithTag(self._displayProductType, self._displaySecondType)
    if pruductSpecial.tag == ShopConfig.SpecialTag.DIAMOND then
        return true
    end
    return false
end

-- 红点数据刷新
function ShopView:updateMenuRedPoint(data)
    local _, shopNewList = XH.lobby:getModule("Shop"):getNewShopData()
    for i = 1, #self._upSecendTabBtns do
        local nType = XH.StringTool.getLastCharAndToNum(self._upSecendTabBtns[i]:getName())
        if shopNewList[tostring(nType)] then
            XH.UITool.setVisible(self._upSecendTabBtns[i], KW_IMAGE_RED_POS, true)
        else
            XH.UITool.setVisible(self._upSecendTabBtns[i], KW_IMAGE_RED_POS, false)
        end
    end
    for i = 1, #self._leftTabBtns do
        local nType = XH.StringTool.getLastCharAndToNum(self._leftTabBtns[i]:getName())
        if shopNewList[tostring(nType)] then
            XH.UITool.setVisible(self._leftTabBtns[i], KW_IMAGE_RED_POS, true)
        else
            XH.UITool.setVisible(self._leftTabBtns[i], KW_IMAGE_RED_POS, false)
        end
    end
end

-- 红点数据刷新
function ShopView:onEventShowItemsRedPoint(event)
    self:updateMenuRedPoint()
end

function ShopView:getTabTextImgName(title)
    if not title or title == "" then
        return nil
    end
    for _, nameConf in pairs(ShopConfig.TabListNames) do
        if nameConf.NameStr == title then
            return nameConf.NameImg
        end
    end
    return nil
end

function ShopView:initLeftTypeList()
    local leftTabData = XH.lobby:getModule("Shop"):getShopAllTags()

    for k, v in ipairs(leftTabData) do
        local btnTab = self._btnType:clone()
        XH.UITool.setText(btnTab, KW_TXT_PRODUCT_NAME, v.title)
        local nameImg = self:getTabTextImgName(v.title)
        if nameImg and nameImg ~= "" then
            XH.UITool.ignoreContentAdaptWithSize(btnTab,"KW_IMG_PRODUCT_NAME",true)
            XH.UITool.loadTexture(btnTab, "KW_IMG_PRODUCT_NAME", nameImg, ccui.TextureResType.plistType)
            XH.UITool.setColor(btnTab, "KW_IMG_PRODUCT_NAME", TabTextDefaultColor)
            XH.UITool.setVisible(btnTab, "KW_IMG_PRODUCT_NAME", true)
            XH.UITool.setVisible(btnTab, KW_TXT_PRODUCT_NAME, false)
        else
            XH.UITool.setVisible(btnTab, "KW_IMG_PRODUCT_NAME", false)
            XH.UITool.setVisible(btnTab, KW_TXT_PRODUCT_NAME, true)
        end
        btnTab:setName(KW_BTN_TYPE .. v.id)
        btnTab:setTag(k)
        self._leftBtnParent:addChild(btnTab)
    end

    self._leftTabBtns = self._leftBtnParent:getChildren()
    self._leftBtnParent:setInnerContainerSize(cc.size(self._originalListViewWidth,#self._leftTabBtns * self._btnType:getContentSize().height))
    self._leftBtnParent:setScrollBarEnabled(false)
    self._oriLeftBtnParentSize = self._leftBtnParent:getContentSize()
    if self._productMod then
        self._productModSize = self._productMod:getContentSize()
    end

    for _, btn in pairs(self._leftTabBtns) do
        btn:addTouchEventListener(handler(self, self.onTabChanged))
    end
end

return ShopView _ 