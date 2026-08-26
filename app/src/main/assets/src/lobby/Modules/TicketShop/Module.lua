local TicketShopModule = class("TicketShopModule", XH.ModuleBase)
local TaskDefine = require("app.Define.TaskDefine")

TicketShopModule.EVNET_PROPS_CHANGED = "EVNET_PROPS_CHANGED"
TicketShopModule.GET_NOTHING_TICKETS_STORE = "GET_NOTHING_TICKETS_STORE"
TicketShopModule.EVENT_GET_EXCHANGEHISTORY = "EVENT_GET_EXCHANGEHISTORY"
TicketShopModule.EVENT_GET_EXCHANGEKAMI = "EVENT_GET_EXCHANGEKAMI"
TicketShopModule.EVNET_PROPS_TYPE_REFRESH = "EVNET_PROPS_TYPE_REFRESH"
TicketShopModule.EVNET_PROPS_JINGDONGKA = "EVNET_PROPS_JINGDONGKA"

TicketShopModule.ProductType = {
    NONE = 0,
    ALL = 1,
    VIRTURE = 2,
}

TicketShopModule.ProductTypeStr = {
    "",
    "prop",
}

-- 默认礼券商城页签列表，tag_id 页签序号 tag_name 页签名字 tag_key 页签标签
local KW_PRODUCTD_TYPE_DEFAULT_LIST = {
    {["tag_id"] = 1, ["tag_name"] = "全部", ["tag_key"] = ""},
}

local HUAFEI_SHOP_TYPE = 18

function TicketShopModule:ctor()
    TicketShopModule.super.ctor(self)
    self._allProps = {}
    self._propType = ""
    self:RequestSysTime()
    self._SysTime = 0 
end

function TicketShopModule:getReqConfig()
    return {
        ReqExchangeProductList = { reqPath = "lobby.Req.Shop.ReqExchangeProductList", callBack = self.respExchangeProductsInfo },
        ReqExchange = { reqPath = "lobby.Req.Shop.ReqExchange", callBack = self.respExchangeProduct },
        ReqExchangeFee = { reqPath = "lobby.Req.Shop.ReqExchangeFeeProduct", callBack = self.respExchangeFees },
        ReqPhoneFeeVerCode = { reqPath = "lobby.Req.Shop.ReqPhoneFeeVerCode", callBack = self.respFeesPhoneCheck },
        ReqExchangeHistory = { reqPath = "lobby.Req.Shop.ReqExchangeHistory", callBack = self.onExchangeHistoryCallBack },
        ReqExchangeJDKKami = { reqPath = "lobby.Req.Shop.ReqExchangeJDKKami", callBack = self.onExchangeKamiCallBack },
        ReqExchangeProductTypeList = { reqPath = "lobby.Req.Shop.ReqExchangeProductTypeList", callBack = self.respExchangeProductsTypeList},
        RequestSysTime = { reqPath = "app.Req.Task.ReqTaskPercent", callBack = self.onReqSysTemsCallBack },
        ReqHuaFeiExchangeProductList = { reqPath = "lobby.Req.Shop.ReqExchangeProductList", callBack = self.respHuaFeiExchangeProductsInfo },
        ReqExchangeHuaFei = { reqPath = "lobby.Req.Shop.ReqExchange", callBack = self.respExchangeHuaFei },
    }
end

-- 请求商品类型列表
function TicketShopModule:reqExchangeProductsTypeList()
    if XH.areaData:getCouponShopID() == nil then
        self:dispatchEvent({name = self.GET_NOTHING_TICKETS_STORE})
        return
    end
    XH.TipTool.showLoading()
    local data = {} 
    data.areaId = XH.playerData:getBrandID()
    data.gameid = XH.areaData:getConfigGameID()
    data.numId = XH.playerData:getNumberID()
    data.productTypeEx = XH.areaData:getCouponShopID() or 0
    data.windMsgId = XH.areaData:getAreaWindID()
    data.uid = un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID())

    self:startReq("ReqExchangeProductTypeList", data, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

-- 请求全部商品列表
function TicketShopModule:reqExchangeProductsInfo(typeTag)
    if XH.areaData:getCouponShopID() == nil then
        self:dispatchEvent({name = self.GET_NOTHING_TICKETS_STORE})
        return
    end
    XH.TipTool.showLoading()
    local numId = XH.playerData:getNumberID()
    local userId = un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID())
    local areaId = XH.playerData:getBrandID()
    local ticketShopId = XH.areaData:getCouponShopID() or 0
    self._propType = typeTag or self._propType
    self:startReq("ReqExchangeProductList", numId, userId, areaId, XH.areaData:getAreaWindID(), 5, XH.KW_CONFIG_LOGIC_TIME_OUT, typeTag, ticketShopId)
end

-- 请求兑换道具
-- productType 道具类型（金币或者房卡）
-- productId 道具ID
function TicketShopModule:reqExchangeProduct(productType, productId, phoneNum,paramExTable)
    XH.TipTool.showLoading()
    local numId = XH.playerData:getNumberID()
    local userId = un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID())
    local areaId = XH.playerData:getBrandID()
    local gameId = XH.areaData:getConfigGameID()
    local product = self:getProductInfoByID(productId)
    if product.type == "gold" then
        gameId = XH.areaData:getCommonGoldGameID()
    end
    local ticketShopId = XH.areaData:getCouponShopID() or 0

    self:startReq("ReqExchange", areaId, XH.areaData:getAreaWindID(), numId, userId, gameId, productId, 5, XH.KW_CONFIG_LOGIC_TIME_OUT, ticketShopId, phoneNum,nil, nil,nil,nil,paramExTable)
end

-- 请求兑换话费商品
function TicketShopModule:reqExchangeFees(phoneid, ischeckphone, codecheck, feesnum)
    XH.TipTool.showLoading()
    self:startReq("ReqExchangeFee", phoneid, ischeckphone, codecheck, feesnum, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

-- 兑换话费时请求获取验证码
function TicketShopModule:reqFeesPhoneCheck(strPhone)
    self:startReq("ReqPhoneFeeVerCode", strPhone, XH.KW_CONFIG_LOGIC_TIME_OUT)
end
----------回调处理-----------------------
-- 全部商品列表结果
function TicketShopModule:respExchangeProductsInfo(req, type, data)
    XH.TipTool.hideLoading()

    local areaId = XH.areaData:getAreaID()

    if type == XH.Req.TYPE.SUCCESS then
        if #data == 0 then
            self:dispatchEvent({name = self.GET_NOTHING_TICKETS_STORE})
        end
        self._allProps[areaId] = data
    elseif type == XH.Req.TYPE.FAIL then
        self._allProps[areaId] = { }
        self:dispatchEvent({name = self.GET_NOTHING_TICKETS_STORE})
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, req:getMessage())
    elseif type == XH.Req.TYPE.TIMEOUT then
        self._allProps[areaId] = { }
        self:dispatchEvent({name = self.GET_NOTHING_TICKETS_STORE})
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, req:getMessage())
    end
    self:dispatchEvent({name = self.EVNET_PROPS_CHANGED})
end

function TicketShopModule:respExchangeProductsTypeList(req, type, data)
    XH.TipTool.hideLoading()
    if data and next(data) ~= nil then
        self._tagTable = data.data
        self:dispatchEvent({name = self.EVNET_PROPS_TYPE_REFRESH,data = data})
    else
        self._tagTable = KW_PRODUCTD_TYPE_DEFAULT_LIST
        self:dispatchEvent({name = self.EVNET_PROPS_TYPE_REFRESH,data = self._tagTable})
    end
    
end

-- 请求兑换结果
function TicketShopModule:respExchangeProduct(req, type, data)
    XH.TipTool.hideLoading()

    if type == XH.Req.TYPE.SUCCESS then
        --兑换成功后刷新缓存
        self:reqExchangeProductsInfo(self._propType)
        XH.playerData:flushPlayerDrop()
        XH.playerData:flushGoldCoin()
    end
    XH.TipTool.showTip({
        type = XH.TIP_LAYER_TYPE.OK,
    }, data.message)
    self:dispatchEvent({name = self.EVNET_PROPS_CHANGED})
end

-- 请求兑换话费商品回调
function TicketShopModule:respExchangeFees(req, type, data)
    XH.TipTool.hideLoading()

    if type == XH.Req.TYPE.SUCCESS then
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, data.message)
        XH.userDefault:setValue(XH.userDefault.KEY_ID.EXCHANGE_FEE_PHONE, data.phone)
        XH.playerData:flushPlayerDrop()
        XH.playerData:flushGoldCoin()
    else
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, req:getMessage())
    end
end

-- 请求话费时候验证码回调
function TicketShopModule:respFeesPhoneCheck(req, type, data)
    XH.TipTool.hideLoading()
end
-------------对外接口-----------
-- 是否已经获取过道具列表
function TicketShopModule:isHanveProductList(productType)
    local isHanve = #self._allProps > 0
    return isHanve
end

-- 获取道具列表
function TicketShopModule:getProductList(productType)
    local areaId = XH.areaData:getAreaID()
    local totalProps = self._allProps[areaId] or {}
    return totalProps
--    if productType == TicketShopModule.ProductType.NONE then
--        return 
--    elseif productType == TicketShopModule.ProductType.ALL then
--        --如果是所有的则全部返回
--        return totalProps
--    end

--    local result = {}
--    local propsType = TicketShopModule.ProductTypeStr[productType]

--    for k,v in pairs(totalProps) do
--        if v.type == propsType then
--            result[#result + 1] = v
--        end
--    end
--    return result
end

-- 是否有足够的礼券兑换道具
function TicketShopModule:isHaveEnoughLiQuan(productId)
    local isEnough = false
    local useCurLiQuan = self:getCurLiQuan()

    local pro = self:getProductInfoByID(productId)
    if pro and pro.price <= useCurLiQuan then
        isEnough = true
    end
    return isEnough
end

-- 获得当前的礼券数目
function TicketShopModule:getCurLiQuan()
    local useCurLiQuan = XH.playerData:getLiQun()
    return useCurLiQuan
end

-- 获得当前的话费数目
function TicketShopModule:getCurHuaFei()
    local useCurHuaFei = (XH.playerData:getHuaFei() or 0) * 0.01
    return useCurHuaFei
end

-- 根据ID获取商品信息
function TicketShopModule:getProductInfoByID(productId)
    local areaId =  XH.areaData:getAreaID()

    local totalProps = self._allProps[areaId] or {}
    for _,v in pairs(totalProps) do
        if v.id == productId then
            return v
        end
    end
    return nil
end

function TicketShopModule:sendUserPhoneInfo()
--    local numId = XH.playerData:getNumberID()
--    local userId = un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID())
--    local areaId = XH.playerData:getBrandID()
--    local phoneNum = XH.playerData:getPlayerPhone()
end

--请求兑换记录
function TicketShopModule:onReqExchangeHistory()
    XH.TipTool.showLoading()
    self:startReq("ReqExchangeHistory", XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function TicketShopModule:onExchangeHistoryCallBack(cell, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent( { name = self.EVENT_GET_EXCHANGEHISTORY, data = data })
    else
        local message = cell:getMessage()
        print(message)
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        },message)
    end
end

--请求卡密
function TicketShopModule:onReqExchangeKami(orderid)
    XH.TipTool.showLoading()
    self:startReq("ReqExchangeJDKKami", orderid, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function TicketShopModule:onExchangeKamiCallBack(cell, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent( { name = self.EVENT_GET_EXCHANGEKAMI, data = data })
    else
        local message = cell:getMessage()
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        },message)
    end
end

function TicketShopModule:reqDownloadImage(image_url , force, obj, callBack)
    local downloadImage = require("app.Req.DownloadImage")
    local reqDownloadImage = downloadImage:new()
    reqDownloadImage:addReqCallBack(obj, callBack)
    reqDownloadImage:setUrl(image_url, false)
end

function TicketShopModule:RequestSysTime()
    self:startReq("RequestSysTime", XH.playerData:getNumberID(), 0, 120, TaskDefine.KW_SYSTEM_TIME, XH.areaData:getSrsGroupID(), 10)
end
    
function TicketShopModule:onReqSysTemsCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local timestamp = json.decode(data.acData).time
        if timestamp ~= nil then
            self._SysTime = timestamp
        end
    end
end

function TicketShopModule:getSysTime()
    return self._SysTime
end

-- 请求话费商城列表
function TicketShopModule:reqHuaFeiExchangeProductsInfo()
    XH.TipTool.showLoading()
    local numId = XH.playerData:getNumberID()
    local userId = un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID())
    local areaId = XH.playerData:getBrandID()
    self:startReq("ReqHuaFeiExchangeProductList", numId, userId, areaId, XH.areaData:getAreaWindID(),HUAFEI_SHOP_TYPE, XH.KW_CONFIG_LOGIC_TIME_OUT,"")
end

-- 全部商品列表结果
function TicketShopModule:respHuaFeiExchangeProductsInfo(req, type, data)
    XH.TipTool.hideLoading()
    self:dispatchEvent({name = self.EVNET_PROPS_JINGDONGKA,data = data})
end

function TicketShopModule:reqHuaFeiExchangeProduct(productId, phoneNum,richType,paramExTable, subItemType)
    XH.TipTool.showLoading()
    local numId = XH.playerData:getNumberID()
    local userId = un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID())
    local areaId = XH.playerData:getBrandID()
    local gameId = XH.areaData:getConfigGameID()
    self:startReq("ReqExchangeHuaFei", areaId, XH.areaData:getAreaWindID(), numId, userId, gameId, productId, HUAFEI_SHOP_TYPE, XH.KW_CONFIG_LOGIC_TIME_OUT, nil, phoneNum,richType, nil,nil,nil,paramExTable, subItemType)
end

function TicketShopModule:checkIsJingDongKa(subItemType)
    return subItemType and subItemType == "cty_gift_jd"
end

function TicketShopModule:respExchangeHuaFei(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        --兑换成功后刷新缓存
        if data.data and data.data.status == 1 then
            local tipStr = "兑换成功"
            if self:checkIsJingDongKa(data.subItemType) then
                tipStr = "兑换成功。系统将通过短信发送卡密到您填写的手机，请留意短信。"
            end
            XH.TipTool.showTip(
                {
                    type = XH.TIP_LAYER_TYPE.OK
                },
                tipStr
            )
            XH.playerData:flushPlayerDrop()
            XH.playerData:flushGoldCoin()
        end
    else
        XH.TipTool.showTip(
                {
                    type = XH.TIP_LAYER_TYPE.OK
                },
                req:getMessage()
            )
    end
end

return TicketShopModule  �7  