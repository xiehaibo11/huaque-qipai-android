local CouponSystemView = class("CouponSystemView", XH.ViewBase)

local DEFAULT_DOWNLOAD_IMAGE_DIR = device.writablePath .. "tmpImageCachDir/"

local CouponType = {
    COUPON_DISCOUNT = 1,
    COUPON_FULL_MINUS = 2,
    COUPON_FULL_PRESENT = 3,
}

function CouponSystemView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/CouponSystem/CouponDetail.csb"
end

function CouponSystemView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("CouponSystem"), eventKeyName = "EVENT_MAX_COUPON_INDEX", callBack = "showInstantCouponProp"},
        { module = XH.lobby:getModule("CouponSystem"), eventKeyName = "EVENT_CLOSE_COUPON_VIEW", callBack = "onCloseView"},
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVNET_SHOP_CLOSE_COUPON_VIEW", callBack = "onCloseView"},
    }
end

function CouponSystemView:getBindingInfo()
    return {
        ["_KW_IMG_BG"] = {varName = "_imgBG",onTouchEnded = "oncloseList"},
        ["_KW_BTN_CLOSE"] = {varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClose"},
        ["_KW_BTN_CONFIRM"] = {varName = "_btnConfirm", type = XH.UI_TYPE.BUTTON,onTouchEnded = "onConfirmBuy"},
        ["_KW_BTN_LATER_CHOOSE"] = {varName = "_btnLater", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClose"},
        ["_KW_PROP_ICON"] = {varName = "_imgProp"},
        ["_KW_RED_TIP"] = {varName = "_redTipPanel"},
        ["_KW_RED_TEXT"] = {varName = "_redText"},
        ["_KW_RED_PERCENT_TEXT"] = {varName = "_percentText"},
        ["_KW_ORIGINAL_PROP"] = {varName = "_originProp"},
        ["_KW_DIAMOND_ICON"] = {varName = "_diamondIcon"},
        ["_KW_PROP_PRICE"] = {varName = "_propPrice"},
        ["_KW_NEW_PROP_DETAIL"] = {varName = "_newPropDetail"},
        ["_KW_NEW_PRICE_DETAIL"] = {varName = "_newPriceDetail"},
        ["_KW_COVER_PROP_DETAIL"] = {varName = "_coverPropDetail"},
        ["_KW_COVER_ORIGIN_PRICE"] = {varName = "_coverOrignPrice"},
        ["_KW_CHOOSE_TEXT"] = {varName = "_chooseText"},
        ["_KW_COUPON_LIST"] = {varName = "_allCouponList"},
        ["_KW_COUPON_PROP"] = {varName = "_couponProp"},
        ["_KW_COUPON_NAME"] = {varName = "_couPropText"},
        ["_KW_COUPON_SHOW"] = {varName = "_couponShow" , onTouchEnded = "onShowCouponList"},
        ["_KW_MAX_DISCOUNT_TEXT"] = {varName = "_maxDiscountText"},
    }
end

function CouponSystemView:ctor(prop_detail,coupon_list,buyCallback)
    CouponSystemView.super.ctor(self)
    self._couponList = coupon_list
    self._buyCallback = buyCallback
    self.instantCoupon = {}
    self.MaxCouponIndex = 0


    self.instantProp = prop_detail
    self.PropNum = prop_detail.prop_count
    self.PropPrice = prop_detail.condition
    
    self.coupon_discount = {}--储存折扣卡券列表
    self.coupon_full_minus = {}--储存满减卡券列表
    self.coupon_full_present = {}--储存满赠卡券列表
    self:initPropUI(self.instantProp)
    self:initCouponList(self._couponList)
    XH.lobby:getModule("CouponSystem"):initData(self._couponList,self.instantProp)
    XH.lobby:getModule("CouponSystem"):calMaxDiscountCoupon()
end



function CouponSystemView:initPropUI(productData)--加载选中的道具UI
    local downloadImage = require("app.Req.DownloadImage")
    local reqDownloadImage = downloadImage:new()
    reqDownloadImage:addReqCallBack(self, function(node, cell, type)
        if type == XH.Req.TYPE.SUCCESS then
            local imageName = XH.StringTool.getImageNameByUrl(productData.image)
            local imagePath = DEFAULT_DOWNLOAD_IMAGE_DIR .. imageName
            XH.UITool.loadTexture(self._imgBG,"_KW_PROP_ICON", imagePath, ccui.TextureResType.localType)
        end
    end )
    reqDownloadImage:setUrl(productData.image, false)
    if productData.name then
        self._originProp:setString(productData.name)
    end
    if productData.condition then
        self._propPrice:setString(productData.condition)
    end

end


function CouponSystemView:initCouponList(couponList)--将优惠卡券显示在list中   克隆按钮
    for i,v in ipairs(couponList) do
        local tmpCoupon = self._couponProp:clone()
        tmpCoupon:setName(i)
        tmpCoupon:getChildByName("_KW_COUPON_NAME"):setString(v.propsname)
        tmpCoupon:addTouchEventListener(handler(self, self.onTouchCouponPropEvent))
        self._allCouponList:addChild(tmpCoupon)
    end
    local tmpCoupon = self._couponProp:clone()
    tmpCoupon:setName(#couponList + 1)
    tmpCoupon:getChildByName("_KW_COUPON_NAME"):setString("不使用优惠卡券")
    tmpCoupon:addTouchEventListener(handler(self, self.onTouchCouponPropEvent))
    self._allCouponList:addChild(tmpCoupon)
end

function CouponSystemView:onTouchCouponPropEvent(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._couponShow:setSelected(false)
    local text = send:getChildByName("_KW_COUPON_NAME"):getString()
    self._chooseText:setString(text)
    self:setCouponList(false)
    self:showInstantCouponProp(send:getName())
end

function CouponSystemView:onShowCouponList(send,eventType)
    if send:isBright() and send:isSelected() == true then
        self:setCouponList(true)
    elseif send:isBright() and send:isSelected() == false then
        self:setCouponList(false)
    end
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.Coupon_1)
end



function CouponSystemView:showInstantCouponProp(event)--显示当前优惠卡券的对应的道具折扣信息
    local showCpIndex 
    if event.data then
        showCpIndex = event.data
        self.MaxCouponIndex = event.data
    elseif tonumber(event) == (#self._couponList + 1) then
        self:setIconVisible(false,false,false,false,false)
        self.instantCoupon = {}
        self._maxDiscountText:setVisible(false)
        return
    else 
        showCpIndex = tonumber(event)
    end
    self.instantCoupon = self._couponList[showCpIndex]
    if self.instantCoupon and self.instantCoupon.coupon_type then
        if self.instantCoupon.coupon_type == CouponType.COUPON_DISCOUNT then
            self:setIconVisible(true,false,false,true,false)
            self:reduceCpDiscount(self.instantCoupon)
        elseif self.instantCoupon.coupon_type == CouponType.COUPON_FULL_MINUS then
            self:setIconVisible(true,false,false,true,false)
            self:reduceCpFullMinus(self.instantCoupon)
        elseif self.instantCoupon.coupon_type == CouponType.COUPON_FULL_PRESENT then
            if self.instantCoupon.coupon_rule.give_prop == "card" then
                self:setIconVisible(false,true,true,false,true)
            else
                self:setIconVisible(false,false,true,false,false)
            end
            self:reduceCpFullPresent(self.instantCoupon)
        end
    end
    if showCpIndex == self.MaxCouponIndex then
        self._maxDiscountText:setVisible(true)
        self._chooseText:setString(self.instantCoupon.propsname)
    else
        self._maxDiscountText:setVisible(false)
    end
end

function CouponSystemView:setIconVisible(val1,val2,val3,val4,val5)
    self._coverOrignPrice:setVisible(val1)
    self._coverPropDetail:setVisible(val2)
    self._newPropDetail:setVisible(val3)
    self._newPriceDetail:setVisible(val4)
    self._redTipPanel:setVisible(val5)
end

function CouponSystemView:reduceCpDiscount(instantCp)
    local tmpPrice = (tonumber(instantCp.coupon_rule.discount)/100) * self.PropPrice
    self._newPriceDetail:setString(tmpPrice)
end

function CouponSystemView:reduceCpFullMinus(instantCp)
    local tmpPrice = self.PropPrice - instantCp.coupon_rule.discount
    self._newPriceDetail:setString(tmpPrice)
end


function CouponSystemView:reduceCpFullPresent(instantCp)
    if self.instantCoupon.coupon_rule.give_prop == "card" then--满赠卡,如果送非房卡道具，显示修改
        self._percentText:setString(instantCp.coupon_rule.give.."张")
        self._newPropDetail:setString( (self.PropNum + instantCp.coupon_rule.give)..instantCp.coupon_rule.prop_name)
    else
        self._percentText:setString(instantCp.coupon_rule.give.."张")
        self._newPropDetail:setString("赠"..instantCp.coupon_rule.give..instantCp.coupon_rule.prop_name)
    end    
end

function CouponSystemView:onConfirmBuy(send,eventType)
    if next(self.instantCoupon) then--如果选择不适用优惠卡券，走老逻辑
        XH.lobby:getModule("CouponSystem"):useCouponToProp(self.instantCoupon)
    else
        self._buyCallback()
    end
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.Coupon_3)
end

function CouponSystemView:setCouponList(isShow)
    if isShow == false then
        self._couponShow:setSelected(false)
    end
    self._allCouponList:setVisible(isShow)
end


function CouponSystemView:oncloseList()
   self:setCouponList(false)
end

function CouponSystemView:onCloseView(event,data)
    self:close()
end

function CouponSystemView:onClose(send,eventType)
    self:close()
    if send then
        local name = send:getName()
        if name == "_KW_BTN_LATER_CHOOSE" then
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.Coupon_2)
        elseif name == "_KW_BTN_CLOSE" then
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.Coupon_4)
        end
    end
end

return CouponSystemView
   !%  