local QuickRechargeModule = class("QuickRechargeModule", XH.ModuleBase)
local TaskDefine = require("app.Define.TaskDefine")

QuickRechargeModule.PROP_GOLD = -1

function QuickRechargeModule:ctor()
    QuickRechargeModule.super.ctor(self)
end

function QuickRechargeModule:getReqConfig()
    return {
        ReqSpecialSceneGoods = { reqPath = "lobby.Req.QuickRecharge.ReqSpecialSceneGoods", callBack = self.respSpecialSceneGoods },
        ReqExchangeGoods = { reqPath = "lobby.Req.QuickRecharge.ReqExchangeGoods", callBack = self.respExchangeGoods },
    }
end

function QuickRechargeModule:getProxyEvents()
    return {
        { module = XH.netEngine, eventName = XH.MatchLinkProtocol.RespNotifyTaskPercent.event_key, callBack = "onRespNotifyTaskPercent" },
    }
end

-- 检查道具是否足够
-- @param propID 道具ID
-- @param propNum 道具数量
-- @return Bool 是否足够
function QuickRechargeModule:checkPropEnough(propID, propNum)
    if not propID or not propNum then
        return false
    end
    local selfPropNum
    if propID == self.PROP_GOLD then
        -- 判断前先刷新玩家道具数量
        XH.playerData:flushGoldCoin()
        selfPropNum = XH.playerData:getSR()
    else
        XH.playerData:flushPlayerDrop()
        selfPropNum = XH.playerData:getPlayerDrop()[propID] or 0
    end
    return selfPropNum >= propNum
end

-- 请求特殊场景道具
-- @param scene 场景名称
-- @return 无
function QuickRechargeModule:reqSpecialSceneGoods(scene)
    self._scene = scene
    self:startReq("ReqSpecialSceneGoods", scene, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function QuickRechargeModule:respSpecialSceneGoods(req, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if not next(data) then
            XH.TipTool.showToast(XH.ConstString.getStr("QUICK_RECHARGE_NOT_ENOUGH"))
            return
        end
        self._specialSceneGood = data
        XH.viewManager:openView("QuickRechargeView", nil, data)
        if self._scene then
            self:recordDataToTask(TaskDefine.TASK_ID.TASK_ID_QUICK_RECHARGE_EXPOSURE, self._scene)
            self:recordDataToTask(TaskDefine.TASK_ID.TASK_ID_QUICK_RECHARGE_UNREPEATED, self._scene)
        end
    elseif type == XH.Req.TYPE.FAIL then
        XH.TipTool.showToast(XH.ConstString.getStr("QUICK_RECHARGE_NOT_ENOUGH"))
    end
end

-- 请求兑换特殊场景道具
-- @param itemID 商品id
-- @param propType 道具类型
-- @param sceneID 场景ID
-- @return 无
function QuickRechargeModule:reqExchangeGoods(itemID, propType, sceneID)
    XH.TipTool.showLoading()
    self._bSpecial = sceneID ~= nil
    self:startReq("ReqExchangeGoods", itemID, propType, sceneID, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function QuickRechargeModule:respExchangeGoods(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        if self._bSpecial then
            XH.TipTool.showToast(XH.ConstString.getStr("QUICK_RECHARGE_SUCCESS"))
            XH.playerData:flushGoldCoin()
            XH.playerData:flushPlayerDrop()
        else
            self:reqExchangeGoods(self._specialSceneGood.itemid, self._specialSceneGood.prop_type, self._specialSceneGood.id)
        end
    elseif type == XH.Req.TYPE.FAIL then
        XH.TipTool.showToast(req:getMessage())
    end
end

-- 数据埋点
-- @param taskID taskID记录曝光次数和去重玩家数
-- @return 无
function QuickRechargeModule:recordDataToTask(taskID, scene)
    if not taskID or not scene then
        return
    end
    local ReqTaskProtocol = require("app.Req.Task.ReqTaskProtocol")
    local reqTaskProtocol = ReqTaskProtocol:new()
    local dataString = ";sScene=\"" .. scene .. "\""
    reqTaskProtocol:start(taskID, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
end

-- 获取钻石商品信息
-- @param 无
-- @return 钻石商品信息
function QuickRechargeModule:getDiamondProduct()
    local ShopConfig = require("lobby.Modules.Shop.Config")
    local diamond = XH.lobby:getModule("Shop"):getProductList(ShopConfig.ProductType.DIAMOND) or {}
    if next(diamond) then
        local function cmpFunc(a, b)
            return tonumber(a.value) <  tonumber(b.value)
        end
        table.sort(diamond, cmpFunc)
    end
    return diamond
end

-- 通过道具ID获取商品列表
-- @param propID 道具ID
-- @return 对应商品列表
function QuickRechargeModule:getProductsByPropID(propID)
    if not propID then
        return
    end
    local ShopConfig = require("lobby.Modules.Shop.Config")
    local productType
    if propID == XH.areaData:getPropList().roomCardID then
        productType = ShopConfig.ProductType.GOLD
    elseif propID == self.PROP_GOLD then
        productType = ShopConfig.ProductType.CARD
    end
    if not productType then
        return
    end
    local productsList = XH.lobby:getModule("Shop"):getProductList(productType) or {}
    if next(productsList) then
        local function cmpFunc(a, b)
            return a.condition < b.condition
        end
        table.sort(productsList, cmpFunc)
    end
    return productsList
end

-- 获取最合适的商品
-- @param products 商品合集
-- @param target 目标商品数量
-- @return 最合适的商品信息
function QuickRechargeModule:getSuitableProduct(products, target)
    if not products or not target then
        return
    end
    local myDiamonds = XH.playerData:getDiamnd()
    for _, v in pairs(products) do
        if myDiamonds + v.value >= target then
            return v
        end
    end
    return
end

-- 判断自身钻石是否支持兑换消耗商品
-- @param propID 消耗道具ID
-- @param propNum 消耗道具数量
-- @return 是否支持与兑换商品信息
function QuickRechargeModule:checkSupExchange(propID, propNum)
    local productsList = self:getProductsByPropID(propID)
    if not productsList then
        XH.TipTool.showToast(XH.ConstString.getStr("QUICK_RECHARGE_NOT_ENOUGH"))
        return
    end
    local myPropNum
    if propID == self.PROP_GOLD then
        myPropNum = XH.playerData:getSR()
    elseif propID == XH.areaData:getPropList().roomCardID then
        myPropNum = XH.playerData:getRoomCard()
    else
        myPropNum = 0
    end
    local myDiamonds = XH.playerData:getDiamnd()
    for _, v in pairs(productsList) do
        if v.condition <= myDiamonds and (v.value + myPropNum) >= propNum then
            return true,v
        end
    end
    for _, v in pairs(productsList) do
        if (v.value + myPropNum) >= propNum then
            return false,v
        end
    end
    return
end

-- 消耗为钻石的购买行为
-- @param diamonds 钻石商品列表
-- @param data 特殊场景商品
-- @return 无
function QuickRechargeModule:reqSingleQuickRecharge(diamonds, data)
    local productTmp = self:getSuitableProduct(diamonds, data.score)
    if productTmp then
        local lackNum = data.score - XH.playerData:getDiamnd()
        local targetName = data.title
        local param = { }
        param.lackNum = lackNum
        param.targetName = targetName
        param.costNum = data.score
        param.product = productTmp
        param.giftType = TaskDefine.TASK_ID.TASK_ID_QUICK_RECHARGE_LEAD
        XH.viewManager:openView("QuickRechargeBuyView", nil, param)
    else
        XH.TipTool.showToast(XH.ConstString.getStr("QUICK_RECHARGE_NOT_ENOUGH"))
    end
end

-- 消耗为非钻石的购买行为，需要2次兑换
-- @param diamonds 钻石商品列表
-- @param data 特殊场景商品
-- @return 无
function QuickRechargeModule:reqMultiQuickRecharge(diamonds, data)
    local bFind, firstProduct = self:checkSupExchange(data.consume_prop_id, data.score)
    if bFind and firstProduct then
        self:reqExchangeGoods(firstProduct.item, firstProduct.prop_type)
    elseif firstProduct then
        self._firstProduct = firstProduct
        local productTmp = self:getSuitableProduct(diamonds, firstProduct.condition)
        if productTmp then
            XH.lobby:getModule("Shop"):reqBuyDiamond(productTmp, "deliver_award", TaskDefine.TASK_ID.TASK_ID_QUICK_RECHARGE_LEAD_MULTI, nil, true, nil, "gift")
        else
            XH.TipTool.showToast(XH.ConstString.getStr("QUICK_RECHARGE_NOT_ENOUGH"))
        end
    else
        XH.TipTool.showToast(XH.ConstString.getStr("QUICK_RECHARGE_NOT_ENOUGH"))
    end
end

-- task通知充值成功
function QuickRechargeModule:onRespNotifyTaskPercent(event)
    local respData =  XH.MatchLinkProtocol.RespNotifyTaskPercent:new()
    respData:bistream(event.msg.buff, event.msg.len)
    if respData.nTaskId == TaskDefine.TASK_ID.TASK_ID_QUICK_RECHARGE_LEAD then
        if not self._specialSceneGood then
            XH.TipTool.showToast(XH.ConstString.getStr("QUICK_RECHARGE_GOODS_LOST"))
        end
        self:reqExchangeGoods(self._specialSceneGood.itemid, self._specialSceneGood.prop_type, self._specialSceneGood.id)
    elseif respData.nTaskId == TaskDefine.TASK_ID.TASK_ID_QUICK_RECHARGE_LEAD_MULTI then
        if not self._specialSceneGood or not self._firstProduct then
            XH.TipTool.showToast(XH.ConstString.getStr("QUICK_RECHARGE_GOODS_LOST"))
        end
        self:reqExchangeGoods(self._firstProduct.item, self._firstProduct.prop_type)
    end
end

return QuickRechargeModule  *%  