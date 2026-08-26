local CouponSystemModule = class("CouponSystemModule", XH.ModuleBase)

CouponSystemModule.EVENT_MAX_COUPON_INDEX = "EVENT_MAX_COUPON_INDEX"
CouponSystemModule.EVENT_CLOSE_COUPON_VIEW = "EVENT_CLOSE_COUPON_VIEW"
CouponSystemModule.EVENT_REFRESH_COUPON_DATA = "EVENT_REFRESH_COUPON_DATA"
CouponSystemModule.EVENT_COUPON_SHOW_DIAMOND_VIEW = "EVENT_COUPON_SHOW_DIAMOND_VIEW"
local CouponType = {
    COUPON_DISCOUNT = 1,
    COUPON_FULL_MINUS = 2,
    COUPON_FULL_PRESENT = 3,
}

local MAX_NUMBER = 99999999

function CouponSystemModule:ctor()
    CouponSystemModule.super.ctor(self)
end


function CouponSystemModule:getReqConfig()
    return {
        ReqExchangeCouponList = { reqPath = "lobby.Req.Shop.ReqExchangeCouponProp", callBack = self.onReqExchangeCouponPropCallback },    
    }
end

function CouponSystemModule:initData(cp_list,prop_detail)
    self._couponList = cp_list
    self._coupon_discount = {}--储存折扣卡券列表
    self._coupon_full_minus = {}--储存满减卡券列表
    self._coupon_full_present = {}--储存满赠卡券列表
    
    self.instantProp = prop_detail
    self.PropNum =tonumber( prop_detail.prop_count)
    self.PropPrice =tonumber(prop_detail.condition)
end

function CouponSystemModule:getReqExchangeCouponProp(couponDetail)
    local ticketShopId = XH.areaData:getCouponShopID() or 0
    self:startReq("ReqExchangeCouponList",self.instantProp.id,couponDetail.propsid,ticketShopId)
end

function CouponSystemModule:selectAvalibleCoupon(cpList,productData)
    local avalibleCPList = {}
    if productData.type and productData.type == "prop" then--一期仅针对房卡道具会筛选优惠券，后续需拓展删除即可。
        for i,v in ipairs(cpList) do--筛选当前商品可用的优惠卡券
            if v.coupon_type == CouponType.COUPON_DISCOUNT then
                table.insert(avalibleCPList,cpList[i])
            elseif v.coupon_type == CouponType.COUPON_FULL_MINUS then
                if tonumber(v.coupon_rule.enough) <= tonumber(productData.condition) then
                    table.insert(avalibleCPList,cpList[i])
                end
            elseif v.coupon_type == CouponType.COUPON_FULL_PRESENT then
                if tonumber(v.coupon_rule.enough) <= tonumber(productData.condition) then
                    table.insert(avalibleCPList,cpList[i])
                end
            end
        end
        return avalibleCPList
    else
        return {}
    end
end

function CouponSystemModule:calMaxDiscountCoupon()
    self:devideCouponType()--对所有优惠卡券进行分类
    local coupon_discount = {}
    local coupon_full_minus = {}
    local coupon_full_present = {}
    if next(self._coupon_discount) then
        coupon_discount = self:selectMaxDiscount(self._coupon_discount)
    end
    if next(self._coupon_full_minus) then
        coupon_full_minus = self:selectMaxFullMinus(self._coupon_full_minus)
    end
    if next(self._coupon_full_present) then
        coupon_full_present = self:selectMaxFullPresent(self._coupon_full_present)
    end
    local maxDiscountCP = self:calSinglePrice(coupon_discount,coupon_full_minus,coupon_full_present)--计算三种最优优惠卡券中的单价，返回最优的一种
    for i,v in ipairs(self._couponList) do
        if v == maxDiscountCP then
            self.MaxCouponIndex = i
            self:dispatchEvent( { name = self.EVENT_MAX_COUPON_INDEX, data = self.MaxCouponIndex })
        end
    end
end



function CouponSystemModule:selectMaxDiscount(coupon_discount)
    local tmpCoupon ={
        couponindex = -1,
        couponprice = MAX_NUMBER,
        coupontime = MAX_NUMBER,
    }
    for i,v in ipairs(coupon_discount) do
        local tmpPrice = (tonumber(v.coupon_rule.discount)/100) * self.PropPrice
        local useableTime = v.endtime - v.addtime
        if tmpPrice < tmpCoupon.couponprice then
            tmpCoupon.couponindex = i
            tmpCoupon.couponprice = tmpPrice
            tmpCoupon.coupontime = useableTime
        elseif tmpPrice == tmpCoupon.couponprice then
            if useableTime < tmpCoupon.coupontime then
                tmpCoupon.couponindex = i
                tmpCoupon.couponprice = tmpPrice
                tmpCoupon.coupontime = useableTime
            end
        end
    end
    if tmpCoupon.couponindex ~= -1 then
        return coupon_discount[tonumber(tmpCoupon.couponindex)]
    else
        return {}
    end
end

function CouponSystemModule:selectMaxFullMinus(coupon_full_minus)
    local tmpCoupon ={
        couponindex = -1,
        couponprice = MAX_NUMBER,
        coupontime = MAX_NUMBER,
    }
    for i,v in ipairs(coupon_full_minus) do
        if tonumber(v.coupon_rule.enough) <= self.PropPrice then
            local tmpPrice = self.PropPrice - v.coupon_rule.discount
            local useableTime = v.endtime - v.addtime
            if tmpPrice < tmpCoupon.couponprice then
                tmpCoupon.couponindex = i
                tmpCoupon.couponprice = tmpPrice
                tmpCoupon.coupontime = useableTime
            elseif tmpPrice == tmpCoupon.couponprice then
                if useableTime < tmpCoupon.coupontime then
                    tmpCoupon.couponindex = i
                    tmpCoupon.couponprice = tmpPrice
                    tmpCoupon.coupontime = useableTime
                end
            end
        end
    end
    if tmpCoupon.couponindex ~= -1 then
        return coupon_full_minus[tonumber(tmpCoupon.couponindex)]
    else
        return {}
    end
end

function CouponSystemModule:selectMaxFullPresent(full_present_list)
    local tmpCoupon ={
    couponindex = -1,
    coupongive = -1,
    coupontime = MAX_NUMBER,
    }
    for i,v in ipairs(full_present_list) do
        if tonumber(v.coupon_rule.enough) <= self.PropPrice then
            local useableTime = v.endtime - v.addtime
            if tonumber(tmpCoupon.coupongive) < tonumber(v.coupon_rule.give) then
                tmpCoupon.couponindex = i
                tmpCoupon.coupongive = v.coupon_rule.give
                tmpCoupon.coupontime = useableTime
            elseif tonumber(tmpCoupon.coupongive) == tonumber(v.coupon_rule.give) then
                if useableTime < tmpCoupon.coupontime then
                    tmpCoupon.couponindex = i
                    tmpCoupon.coupongive = v.coupon_rule.give
                    tmpCoupon.coupontime = useableTime
                end
            end
        end
    end
    if tmpCoupon.couponindex ~= -1 then
        return full_present_list[tonumber(tmpCoupon.couponindex)]
    else
        return {}
    end
end

function CouponSystemModule:devideCouponType()
    for i,v in ipairs(self._couponList) do
        if v.coupon_type == CouponType.COUPON_DISCOUNT then
            table.insert(self._coupon_discount,self._couponList[i])
        elseif v.coupon_type == CouponType.COUPON_FULL_MINUS then
            table.insert(self._coupon_full_minus,self._couponList[i])
        elseif v.coupon_type == CouponType.COUPON_FULL_PRESENT then
            table.insert(self._coupon_full_present,self._couponList[i])
        end
    end
end

function CouponSystemModule:calSinglePrice(discountCP,FullMinesCP,FullPresentCP)
    local tmpTable = {}
    local discountCPPrice = MAX_NUMBER
    local FullMinesCPPrice = MAX_NUMBER
    local FullPresentCPPrice = MAX_NUMBER
    if next(discountCP) then
        local tmpPrice = (tonumber(discountCP.coupon_rule.discount)/100) * self.PropPrice
         discountCPPrice = tmpPrice / self.PropNum
         table.insert(tmpTable,discountCPPrice)
    end
    if next(FullMinesCP) then
        local tmpPrice = self.PropPrice - FullMinesCP.coupon_rule.discount
        FullMinesCPPrice = tmpPrice / self.PropNum
        table.insert(tmpTable,FullMinesCPPrice)
    end
    if next(FullPresentCP) then
        local tmpPrice = self.PropPrice
        FullPresentCPPrice = tmpPrice / (self.PropNum + tonumber(FullPresentCP.coupon_rule.give))
        if next(tmpTable) then
            local tmpMinCoupon = math.min(unpack(tmpTable))
            if tmpMinCoupon == FullPresentCPPrice then
                return FullPresentCP
            else
                table.insert(tmpTable,FullPresentCPPrice)
            end
        else
            table.insert(tmpTable,FullPresentCPPrice)
        end
    end

    local MinCoupon = math.min(unpack(tmpTable))
    if MinCoupon == discountCPPrice then
        return discountCP
    elseif MinCoupon == FullMinesCPPrice then
        return FullMinesCP
    elseif MinCoupon == FullPresentCPPrice then
        return FullPresentCP
    end
end

function CouponSystemModule:onReqExchangeCouponPropCallback(cell, type, response)
    if type == XH.Req.TYPE.SUCCESS then
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK,
            funcOK = function()
            end
        } , "兑换成功\n兑换记录可在大厅-活动-领奖中心处查询")
        self:dispatchEvent( { name = self.EVENT_CLOSE_COUPON_VIEW})
        self:dispatchEvent( { name = self.EVENT_REFRESH_COUPON_DATA})
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.Coupon_5)
    elseif type == XH.Req.TYPE.FAIL then
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK_CANCEL_NO_CLOSE,
            funcOK = function()
                self:dispatchEvent({name = "EVENT_COUPON_SHOW_DIAMOND_VIEW"})
                self:dispatchEvent({name = "EVENT_CLOSE_COUPON_VIEW"})
            end
        } , cell:getMessage())
    end
end

function CouponSystemModule:useCouponToProp(couponDetail)--兑换商品
    self:getReqExchangeCouponProp(couponDetail)
end

return CouponSystemModule



  �&  