local SaleGiftBagView = class("SaleGiftBagView", XH.ViewBase)
local DEFAULT_DOWNLOAD_IMAGE_DIR = device.writablePath .. "tmpImageCachDir/"

function SaleGiftBagView:getCSBPath()
	return "res/cocosStudio/hall/CSB/Activity/SaleGiftbag/SaleGiftbag.csb"
end
function SaleGiftBagView:getBindingInfo()
    return {
        ["_KW_INFO_DISCOUNT_TEXT"] = {varName="_disText"},
        ["_KW_INFO_VALUE_TEXT"] = {varName="_valueText"},
        ["_KW_IMAGE_AWARD"] = {varName="_imgAward"},
        ["_KW_TEXT_SURPLUS_TIME"] = {varName="_textSurplusTime"},
        ["_KW_BTN_BUY"] = {varName="_btnBuy",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventBuy"},
        ["_KW_BTN_BUY_D"] = {varName="_btnBuy",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventBuyDetail"},
        ["_KW_INFO_TEXT_1"] = {varName="_nameText"},
        ["_KW_INFO_TEXT_2"] = {varName="_numText"},
        ["_KW_INFO_AWARD_TEXT_1"] = {varName="_diaText"},
        ["_KW_INFO_AWARD_TEXT_2"] = {varName="_vText"},
        ["_KW_LABEL_LEFT_TIME"] = {varName="_leftTimeText"},
        ["_KW_TEXT_MONEY"] = {varName="_moneyText"},
        ["_KW_TEXT_PRICE"] = {varName="_priceText"},
        ["_KW_DETAIL_LABEL"] = {varName="_detailText"},

        ["_KW_BTN_CLOSE"] = {varName="_btnClose",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventClose"},
        ["_KW_BTN_CLOSE_D"] = {varName="_btnClose",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventCloseD"},
        ["_KW_IMAGE_BG_D"] = {varName="_detailNode"},
        ["_KW_PANEL_D_BG"] = {varName="_detailNodeBg"},
        ["_KW_COLLECT_GIFT_ITEM"] = {varName="_giftItem"},

        ["_KW_DETAIL_POS_1_1"] = {varName="_detail_pos_1_1"},
        ["_KW_DETAIL_POS_2_1"] = {varName="_detail_pos_2_1"},
        ["_KW_DETAIL_POS_2_2"] = {varName="_detail_pos_2_2"},
        ["_KW_DETAIL_POS_3_1"] = {varName="_detail_pos_3_1"},
        ["_KW_DETAIL_POS_3_2"] = {varName="_detail_pos_3_2"},
        ["_KW_DETAIL_POS_3_3"] = {varName="_detail_pos_3_3"},

        ["_KW_COLLECT_POS_1_1"] = {varName="_item_pos_1_1"},
        ["_KW_COLLECT_POS_2_1"] = {varName="_item_pos_2_1"},
        ["_KW_COLLECT_POS_2_2"] = {varName="_item_pos_2_2"},
        ["_KW_COLLECT_POS_3_1"] = {varName="_item_pos_3_1"},
        ["_KW_COLLECT_POS_3_2"] = {varName="_item_pos_3_2"},
        ["_KW_COLLECT_POS_3_3"] = {varName="_item_pos_3_3"},
    }
end

function SaleGiftBagView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("SaleGiftbag"), eventKeyName = "TASK_TYPE_ID_BUY_DISCOUNT", callBack = "onEventCanBuyGift"},
        { module = XH.lobby:getModule("SaleGiftbag"), eventKeyName = "EVENT_SHOW_DISCOUNT_GIFT", callBack = "onEventShowDiscountGift"},
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVNET_SHOPMANAGER_SHOW_SHOP", callBack = "onEventProductList" },
    }
end

local KW_COLLECT_IMAGE = "_KW_COLLECT_IMAGE"
local KW_COLLECT_TITLE = "_KW_COLLECT_TITLE"
local KW_IMAGE_SIZE = "_KW_IMAGE_SIZE"

function SaleGiftBagView:ctor(param)
    param = param or {}
	SaleGiftBagView.super.ctor(self, param)
    self:initUI(param)
    self:initData()
    self:initEvent()
end

function SaleGiftBagView:initUI(param)
    if param.leftTime then
        self._leftTime = param.leftTime 
        self:showLeftTime()
        if param.itemsInfo then
            self:showItemUI(param.itemsInfo)
        end
        self._moneyText:setString(param.moneyCost .. "y")
        self._priceText:setString(param.price .. "y")
        self._detailText:setString("本次支付将购买" .. param.moneyCost .. "元钻石")
    end
end
function SaleGiftBagView:showItemUI(param)
    for i = 1, #param do
        local itemPosNode = self["_item_pos_"..#param.."_"..i]
        if not itemPosNode then
            break
        end
        local item = self._giftItem:clone()
        itemPosNode:removeAllChildren()
        itemPosNode:addChild(item)
        item:setPosition(cc.p(0, 0))
        XH.UITool.setText(item, KW_COLLECT_TITLE, param[i].name)
        local downloadImage = require("app.Req.DownloadImage")
        local reqDownloadImage = downloadImage:new()
        XH.UITool.ignoreContentAdaptWithSize(item, KW_COLLECT_IMAGE, true)
        reqDownloadImage:addReqCallBack(self, function(node,cell, type1)
            if type1 == XH.Req.TYPE.SUCCESS then
                local imageUrl = param[i].url
                local imageName = XH.StringTool.getImageNameByUrl(imageUrl)
                local imagePath = DEFAULT_DOWNLOAD_IMAGE_DIR .. imageName  
                XH.UITool.loadTexture(item, KW_COLLECT_IMAGE, imagePath, ccui.TextureResType.localType)
                self:AdaptWithSize(item, KW_IMAGE_SIZE)
            end
        end)
        local imageUrl = param[i].url
        reqDownloadImage:setUrl(imageUrl, false)
    end
    for i = 1, #param do
        local itemPosNode = self["_detail_pos_"..#param.."_"..i]
        if not itemPosNode then
            break
        end
        local item = self._giftItem:clone()
        itemPosNode:removeAllChildren()
        itemPosNode:addChild(item)
        item:setPosition(cc.p(0, 0))
        XH.UITool.setText(item, KW_COLLECT_TITLE, param[i].name)
        local downloadImage = require("app.Req.DownloadImage")
        XH.UITool.ignoreContentAdaptWithSize(item, KW_COLLECT_IMAGE, true)
        local reqDownloadImage = downloadImage:new()
        reqDownloadImage:addReqCallBack(self, function(node,cell, type1)
            if type1 == XH.Req.TYPE.SUCCESS then
                local imageUrl = param[i].url
                local imageName = XH.StringTool.getImageNameByUrl(imageUrl)
                local imagePath = DEFAULT_DOWNLOAD_IMAGE_DIR .. imageName  
                XH.UITool.loadTexture(item, KW_COLLECT_IMAGE, imagePath, ccui.TextureResType.localType)
                self:AdaptWithSize(item, KW_IMAGE_SIZE)
            end
        end)
        local imageUrl = param[i].url
        reqDownloadImage:setUrl(imageUrl, false)
    end
end

---目前走task   后期需要走风信礼包系统
function SaleGiftBagView:initData()
    --请求礼包相关信息
    -- app.saleGiftManager:reqTaskConfig()
end

function SaleGiftBagView:initEvent()

end

function SaleGiftBagView:removeAllListen()
--    if self._saleGift then
--        self._saleGift:removeAllEventListeners()
--		self._saleGift = nil
--    end
--    if self._actEvent then
--        self._actEvent:removeAllEventListeners()
--		self._actEvent = nil
--    end
--    if self._propListern ~= nil then
--        XH.Event.removeListener(self._propListern)
--        self._propListern = nil
--    end
end

function SaleGiftBagView:closeLayer()
    self:stopShowLeftTime()
    self:close()
    if XH.gameManager then
        XH.gameManager:leaveGame()
    end
end

-----------------------------------ui-----------------------------

function SaleGiftBagView:flushGiftUI(event)
    if event == nil or event.msg == nil or event.msg == "" then return end
end

function SaleGiftBagView:runCountDownTime(time)
    if self._textSurplusTime == nil then
        return
    end
    time = tonumber(time) or 0
    self._textSurplusTime:stopAllActions()
    local cb = cc.CallFunc:create(function()
        time = time - 1
        if time >= 0 then
            local h = math.floor(time / 3600)
            local s = math.floor((time - h*3600)/60)
            local m = time - (h*3600 + s*60)
            local strTime = string.format("%02d:%02d:%02d", h, s, m) .. "后结束"
            self._textSurplusTime:setString(strTime)
        else
            self._textSurplusTime:stopAllActions()
        end
    end)
    self._textSurplusTime:runAction(cc.RepeatForever:create(cc.Sequence:create(cb,cc.DelayTime:create(1))))
end

-----------------------------------EventLisitener-----------------------------
function SaleGiftBagView:onEventDiscountGiftBag(event)

end
function SaleGiftBagView:onEventStartDiscountGiftBag(event)

end
function SaleGiftBagView:onEventBuyDiscountGiftBag(event)

end
function SaleGiftBagView:onEventProductList(data)
    if self._dataPropID then
        local ShopConfig = require("lobby.Modules.Shop.Config")
        local _, subId = XH.lobby:getModule("Shop"):getTagsWithName(ShopConfig.SpecialTag.DIAMOND)
        local allproDouct = XH.lobby:getModule("Shop"):getShopItemsWithId(subId) or { }
        local productInfo = {}
        for _,v in pairs(allproDouct) do
            if v.productId == self._dataPropID then
                productInfo = v
            end
        end
        if productInfo and next(productInfo) then
            --给task做区分的
            local giftType = 2
            XH.lobby:getModule("Shop"):reqBuyDiamond(productInfo, "deliver_award", giftType, nil, nil, nil, -1)
        else
            XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, XH.ConstString.getStr("GIFT_BAG_LAYER_CANNOTFIND"))
        end
        self._dataPropID = nil
    end
end

-----------------------------------touchEvent-----------------------------

function SaleGiftBagView:onTouchEventCloseD(send, eventType)
    if eventType == ccui.TouchEventType.began then
        send:setScale(0.9)
        send:setColor(cc.c3b(160,160,160))
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setScale(1.0)
        send:setColor(cc.c3b(255, 255, 255))
    end
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:showDetailLayer(false)
end
function SaleGiftBagView:onTouchEventClose(send, eventType)
    if eventType == ccui.TouchEventType.began then
        send:setScale(0.9)
        send:setColor(cc.c3b(160,160,160))
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setScale(1.0)
        send:setColor(cc.c3b(255, 255, 255))
    end
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    self:closeLayer()
end

function SaleGiftBagView:onTouchEventBuy(send, eventType)
    if eventType == ccui.TouchEventType.began then
        send:setScale(0.9)
        send:setColor(cc.c3b(160,160,160))
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setScale(1.0)
        send:setColor(cc.c3b(255, 255, 255))
    end
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    -- 未实名不能购买 海外玩家不用实名认证
    if XH.playerData:checkNewRealName() ~= 0 then return end--实名限制
    if XH.playerData:checkNewRealNameVisitor() ~= 0 then return end--游客登陆
    self:showDetailLayer(true)
end

function SaleGiftBagView:showDetailLayer(show)
    self._detailNode:setVisible(show)
    self._detailNodeBg:setVisible(show)
end
function SaleGiftBagView:onTouchEventBuyDetail(send, eventType)
    if eventType == ccui.TouchEventType.began then
        send:setScale(0.9)
        send:setColor(cc.c3b(160,160,160))
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setScale(1.0)
        send:setColor(cc.c3b(255, 255, 255))
    end
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    --看玩家是否符合购买资格
    XH.lobby:getModule("SaleGiftbag"):reqBuyDiscountGiftBag()
end

function SaleGiftBagView:onEventCanBuyGift(event)
    if event.msg == "" then 
        XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, "数据请求出错!")
        return 
    end
    local data = json.decode(event.msg)
    if data == nil or next(data) == nil then 
        XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, "数据请求出错!")
        return 
    end
    if not data.productid then 
        XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, XH.ConstString.getStr("GIFT_BAG_LAYER_PASS"))
        return
    end
    local ShopConfig = require("lobby.Modules.Shop.Config")
    local _, subId = XH.lobby:getModule("Shop"):getTagsWithName(ShopConfig.SpecialTag.DIAMOND)
    local allproDouct = XH.lobby:getModule("Shop"):getShopItemsWithId(subId)
    self._dataPropID = nil
    if not allproDouct then
        self._dataPropID = data.productid
        XH.lobby:getModule("Shop"):reqProductList(ShopConfig.ProductType.DIAMOND)
        return
    end
    local productInfo = {}
    for _,v in pairs(allproDouct) do
        if v.productId == data.productid then
            productInfo = v
            break
        end
    end
    if productInfo and next(productInfo) then
        --给task做区分的
        local giftType = 2
        XH.lobby:getModule("Shop"):reqBuyDiamond(productInfo, "deliver_award", giftType, nil, nil, nil, -1)
    else
        XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, XH.ConstString.getStr("GIFT_BAG_LAYER_CANNOTFIND"))
    end
end
function SaleGiftBagView:onEventShowDiscountGift(event)
    if event.msg ~= "" then
        local data = json.decode(event.msg)
        if data and next(data) then
            if data.backmsg ~= "" then
                XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, data.backmsg)
            end
        end
    end
    self:closeLayer()
    XH.taskManager:reqPlayerDropData()
end
function SaleGiftBagView:showLeftTime()
    if not self._leftTime or self._leftTime < 0 then
        return
    end
    local day = math.floor(self._leftTime / 60 / 60 / 24)
    local hour = math.floor(self._leftTime / 60 / 60) % 24
    local min = math.floor(self._leftTime / 60) % 60
    local sec = self._leftTime % 60
    local str = string.format("%d天%d时%d分", day, hour, min)
    if day == 0 then
        str = string.format("%d时%d分%d秒", hour, min, sec)
        if hour == 0 then
            str = string.format("%d分%d秒", min, sec)
        end
    end
    self._leftTimeText:setString("剩余时间："..str)
    self._leftTimeText:stopAllActions()
    self._leftTimeText:runAction(cc.Sequence:create(cc.DelayTime:create(1), cc.CallFunc:create(
        function()
            self._leftTime = self._leftTime - 1
            self:showLeftTime()
        end
    )))
end
function SaleGiftBagView:stopShowLeftTime()
    if self._leftTimeText then
        self._leftTimeText:stopAllActions()
    end
end
function SaleGiftBagView:AdaptWithSize(node, sizeNodeName)
    if not node or not sizeNodeName then
        return
    end
    local sizeNode = XH.UITool.seekNodeByName(node, sizeNodeName)
    local sender = XH.UITool.seekNodeByName(node, KW_COLLECT_IMAGE)
    if not sizeNode or not sender then
        return
    end
    local bgSize = sizeNode:getContentSize()
    sender:runAction(cc.Sequence:create(cc.DelayTime:create(0.1), cc.CallFunc:create(function() 
        sender:setVisible(true)
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
    end)))
end
return SaleGiftBagView C<  