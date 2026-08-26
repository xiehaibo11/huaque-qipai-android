local ShopModule = class("ShopModule", XH.ModuleBase)
local TaskDefine = require("app.Define.TaskDefine")
local ShopConfig = require("lobby.Modules.Shop.Config")

ShopModule.EVNET_SHOPMANAGER_SHOW_SHOP = "EVNET_SHOPMANAGER_SHOW_SHOP"
ShopModule.EVNET_SHOPMANAGER_HIDE_SHOP = "EVNET_SHOPMANAGER_HIDE_SHOP"
ShopModule.EVENT_REQ_EXCHANGE_GOLD_SUCCESS = "EVENT_REQ_EXCHANGE_GOLD_SUCCESS"
ShopModule.EVENT_SHOP_REFRESH_SYSTEM = "EVENT_SHOP_REFRESH_SYSTEM"
ShopModule.EVENT_SHOW_DIAMOND_VIEW = "EVENT_SHOW_DIAMOND_VIEW"
ShopModule.EVENT_REQ_EXCHANGE_GIFT_BOX_SUCCESS = "EVENT_REQ_EXCHANGE_GIFT_BOX_SUCCESS"
ShopModule.EVENT_SHOW_CARD_VIEW = "EVENT_SHOW_CARD_VIEW"
ShopModule.EVENT_SHOW_GOLD_VIEW = "EVENT_SHOW_GOLD_VIEW"
ShopModule.EVENT_SHOP_NEW_ITEM = "EVENT_SHOP_NEW_ITEM"
ShopModule.EVENT_PAGE_PRODUCTS_CONFIG = "EVENT_PAGE_PRODUCTS_CONFIG"
ShopModule.EVENT_START_SHOW_PRODUCTS = "EVENT_START_SHOW_PRODUCTS"
ShopModule.EVNET_DIAMOND_BUY_RESULT = "EVNET_DIAMOND_BUY_RESULT"
ShopModule.EVNET_SHOP_CLOSE_COUPON_VIEW = "EVNET_SHOP_CLOSE_COUPON_VIEW"
ShopModule.EVNET_SHOP_PAGE_MARK_TOP = "EVNET_SHOP_PAGE_MARK_TOP"

-- 购买商品后自动转换对应道具
ShopModule.BUY_DIAMOND_SEPCIAL_TAG = 77770321

-- 暂定上线后为新商品节点
local KW_DEFAULT_TIME_RED_POINT = 1611763200

ShopModule.BUY_DIAMOND_RESULT = {
    SUCCESS = 1,
    CANCEL = 2
}

ShopModule.KW_REQ_LAST_BUY_PRODUCT_ID_TYPE = {
    GET_LAST_BUY_ID = 1,
    SET_LAST_BUY_ID = 2,
    GET_VIP_OFF_TIME_TIPS = 3,
    SET_VIP_OFF_TIME_TIPS = 4,
}

function ShopModule:ctor()
    ShopModule.super.ctor(self)
    self._needShowExchangeTips = true
    self._fishPayCallBack = nil

    self._reqProductType = -1
    self._reqProductInfo = {}
    self._allProps = { }

    -- 新商场复制
    self._propsWithId = { }
    self._allPropTags = { }
    -- 本地点击过的商品列表
    self._isChoseItems = { }
    -- 获取到最新的商品列表
    self._isNewItems = { }
    -- 已过期的商品id
    self._isActOutIds = {}
    -- 多商品购买规格切换
    self._multiBuyCfgId = nil
    -- 商品购买记录
    self._bugLogs = {}
    -- 商品列表
    self._hasReqItems = nil
end

function ShopModule:getReqConfig()
    return {
        ReqSystemTimes = { reqPath = "app.Req.Task.ReqTaskPercent", callBack = self.onReqSystemTimesCallBack },
        ReqExchangeProduct = { reqPath = "lobby.Req.Shop.ReqExchange", callBack = self.respExchangeProduct },
        ReqDiamondProductsInfo = { reqPath = "lobby.Req.Shop.ReqProductList", callBack = self.respDiamondProductsInfo },
        ReqExchangeProductList = { reqPath = "lobby.Req.Shop.ReqExchangeProductList", callBack = self.respProductsInfo },
        ReqTaskIsNewItem = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onReqTakIsNewItemCallBack },
        ReqShopPageMarkTop = { reqPath = "lobby.Req.ShopNew.ReqShopPageMarkTop", callBack = self.onReqShopPageMarkTopCallBack },
        ReqShopProductByConfig = { reqPath = "lobby.Req.ShopNew.ReqShopProductByConfig", callBack = self.onReqShopProductByConfigCallBack },
        ReqTaskBuyLogs = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onReqShopBuyLogsCallBack },
    }
end

function ShopModule:getProxyEvents()
    return {
        { module = XH.sdkManager, eventKeyName = "EVENT_IAP_CALLBACK", callBack = "respBuyDiamond" },
        { module = XH.login:getModule("Login"), eventKeyName = "LOGIN_EVENT_SUCCESS", callBack = "onLoginSuccessCallBack" },
        { module = XH.netEngine, eventName = XH.MatchLinkProtocol.RespNotifyTaskPercent.event_key, callBack = "onRespNotifyTaskPercent" },
        { module = XH.sdkManager, eventKeyName = "EVENT_GETORDER_CALLBACK", callBack = "onGetOrderIdSuccessCallBack"}
    }
end

-- 请求商品列表
function ShopModule:reqProductList(productType)
    if productType == ShopConfig.ProductType.DIAMOND then
        self:reqDiamondProductsInfo()
    else
        self:reqExchangeProductsInfo(productType)
    end
end

-- 请求钻石商品信息
function ShopModule:reqDiamondProductsInfo()
    XH.TipTool.showLoading()
    self:startReq("ReqDiamondProductsInfo", XH.configerManager:getShopAppId(), XH.configerManager:getShopPackageAppId())
end

-- 钻石信息结果
function ShopModule:respDiamondProductsInfo(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        local _, getDiamondSubId = self:getTagsWithName(ShopConfig.SpecialTag.DIAMOND)
        if getDiamondSubId then
            self._propsWithId[getDiamondSubId] = self:newFliterDataByArea(data)
        end
    else
        self:clearPropsThisAreaID(ShopConfig.ProductType.DIAMOND)
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK,
        } , req:getMessage())
    end
    self:dispatchEvent( { name = self.EVNET_SHOPMANAGER_SHOW_SHOP, data = { tag = ShopConfig.SpecialTag.DIAMOND } })
end

-- 请求金币和房卡商品信息
function ShopModule:reqExchangeProductsInfo(propType)
    XH.TipTool.showLoading()
    local numId = XH.playerData:getNumberID()
    local userId = un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID())
    local areaId = XH.playerData:getBrandID()
    self:startReq("ReqExchangeProductList", numId, userId, areaId, XH.areaData:getAreaWindID(), propType, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

-- 新商城筛选对应AREAID商品
function ShopModule:newFliterDataByArea(data)
    data = data or { }
    local props = { }
    for _, v in pairs(data) do
        if string.find(tostring(v.productId), tostring(XH.areaData:getAreaID())) then
            props[#props + 1] = v
        end
    end
    return props
end

function ShopModule:respProductsInfo(req, type, data)
    XH.TipTool.hideLoading()
    local productType = req:getProductType()
    self:clearPropsThisAreaID(productType)
    local resData = nil
    if type == XH.Req.TYPE.SUCCESS then
        self:setCurrentAreaProps(productType, data)
    else
        if productType == ShopConfig.ProductType.PB 
        or productType == ShopConfig.ProductType.YPQ
        or productType == ShopConfig.ProductType.FRAME then
            resData = { type = type, msg = req:getMessage(), isTip = true }
        end
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, req:getMessage())
    end
    self:dispatchEvent({name = self.EVNET_SHOPMANAGER_SHOW_SHOP, data = resData})
end

-- 购买钻石
-- propID,购买钻石礼包时使用，由于服务端无法从有猫腻后台取相应道具ID，所以需要客户端透传
-- targetPropID 购买钻石最终兑换道具id，传propid即可 必传！
function ShopModule:reqBuyDiamond(productInfo, scriptName, gifttype, autoOrder, bHideSuccessTip, propID, targetPropID,triggerGameID)
    if targetPropID == nil then
        targetPropID = 0
    elseif targetPropID == -1 then
        targetPropID = XH.areaData:getGoldPropId() -- 金币转换
    end
    XH.TipTool.showLoading()
    self:recordBuyParam(productInfo, scriptName, gifttype, autoOrder, bHideSuccessTip, propID, targetPropID)
    self._bHideBuySuccessTip = bHideSuccessTip
    local gameId = triggerGameID or XH.areaData:getConfigGameID() 
    local channelID = XH.SysTool:getChannelID()
    local numId = XH.playerData:getNumberID()
    local userId = un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID())
    local areaId = tostring(XH.playerData:getBrandID())
    local propid = propID or XH.areaData:getPropDiamndID()
    local groupid = XH.areaData:getSrsGroupID()
    -- 传了这个表示走新的发货流程
    local defaultScriptName = scriptName or "deliver_award"
    gifttype = gifttype or ""
    local ext = json.encode( {
        gameid = gameId,
        areaid = areaId,
        propid = propid,
        channelid = channelID,
        numid = numId,
        source = areaId .. XH.areaData:getLobbyID(),
        script_name = defaultScriptName,
        order_id = "",
        groupid = groupid,
        gifttype = gifttype,
        to_propsid = tostring(targetPropID),
    } )

    local proForpey = {
        order_id = "",
        product_id = productInfo.productId,
        product_name = productInfo.name,
        product_price = productInfo.price,
        product_count = "1",
        role_id = numId,
        role_name = userId,
        role_grade = "",
        role_balance = "",
        server_id = "",
        ext = ext,
        ["notify_url"] = "",
        is_auto_order = autoOrder and tostring(autoOrder) or "0"
    }

    local DEBUG = require("app.Config.GlobalConfig").IsDebug
    if DEBUG then
        proForpey.is_test = 1
    end

    local proForH5
    if propid == XH.areaData:getPropList().propDiamndID then
        proForH5 =({
            channel_id = 2,
            product_id = "prop"..propid,
            product_name = productInfo.name,
            product_price = "1.00",
            product_qty = tonumber(productInfo.value),
            trade_attach = ext,
            client_info = {
                device_id = XH.SysTool:GetDevid(),
                client_ip = un.Device.getIp(),
            },
        })
    else
        proForH5 =({
            channel_id = 2,
            product_id = "prop"..propid,
            product_name = productInfo.name,
            product_price = tonumber(productInfo.price) * 100,
            product_qty = 1,
            trade_attach = ext,
            client_info = {
                device_id = XH.SysTool:GetDevid(),
                client_ip = un.Device.getIp(),
            },
        })
    end
    local target = cc.Application:getInstance():getTargetPlatform()
    if target == cc.PLATFORM_OS_IPHONE or target == cc.PLATFORM_OS_IPAD then
        XH.sdkManager:xhCallOCFunc("XH_IAP", "xhBuy:", proForpey)
    elseif device.platform == "android" then
        if XH.ChannelTool.checkIsECarChannel() then --车载渠道，传H5道具参数
            XH.TipTool.hideLoading()
            XH.viewManager:openView("PayTypeView", nil, proForH5)
        elseif XH.SysTool.CompareToBundleVersion and XH.SysTool:CompareToBundleVersion("1.2.1") then
            XH.TipTool.hideLoading()
            XH.viewManager:openView("PayTypeView", nil, proForpey)
        else
            XH.sdkManager:callFunctionWithMap(XH.ThirdDefine.SDK_INTERFACE_NAMES_KEY.BUY, { ["0"] = proForpey })
        end
        --调用百度营销sdk  付费接口 
    else
        XH.TipTool.hideLoading()
    end
end

function ShopModule:reqBuyDiamondGiftpack(productInfo, propID, autoOrder)
    self:reqBuyDiamond(productInfo, nil, nil, autoOrder, nil, propID, "gift")
end

function ShopModule:reqBuyFish(productInfo, gameid, payCb)
    XH.TipTool.showLoading()
    local areaConf = XH.areaData:getAreaDataByLobbyID()
    local channelID = areaConf.channelID
    local userData = XH.playerData
    local numId = userData:getNumberID()
    local userId = un.StringUtils.GB_18030_2000_TO_UTF8(userData:getUserID())
    local areaId = tostring(userData:getBrandID())
    local groupid = areaConf.srsGroupID
    --传了这个表示走新的发货流程
    local ext = {
        gameid = gameid,
        areaid = areaId,
        channelid = channelID,
        numid = numId,
        source = areaId .. areaConf.lobbyID,
        order_id = "",
        groupid = groupid,
    }

    if next(productInfo.ext) then
        table.merge(ext, productInfo.ext)
    end
    local proForpey = {
        order_id = "",
        product_id = productInfo.id,
        product_name = productInfo.name,
        product_price = productInfo.price,
        product_count = productInfo.count,
        role_id = numId,
        role_name = userId,
        role_grade = "",
        role_balance = "",
        server_id = "",
        ext = json.encode(ext),
        ["notify_url"] = "",
    }
    local target = cc.Application:getInstance():getTargetPlatform()
    if target == cc.PLATFORM_OS_IPHONE or target == cc.PLATFORM_OS_IPAD then
        XH.sdkManager:xhCallOCFunc("XH_IAP", "xhBuy:", proForpey)
    elseif device.platform == "android" then
        if XH.SysTool.CompareToBundleVersion and XH.SysTool:CompareToBundleVersion("1.2.1") then
            XH.TipTool.hideLoading()
            XH.viewManager:openView("PayTypeView", nil, proForpey)
        else
            XH.sdkManager:callFunctionWithMap(XH.ThirdDefine.SDK_INTERFACE_NAMES_KEY.BUY,{["0"] = proForpey})
        end
    else
        XH.TipTool.hideLoading()
    end
    self._fishPayCallBack = payCb
end

function ShopModule:setFishPayFunction(func)
    self._fishPayCallBack = func
end

-- 新商城相关数据存储
function ShopModule:saveProductNewMsg(productType, productId, multiCfgId)
    self._reqProductType = productType
    self._multiBuyCfgId = nil
    if self._propsWithId[self._reqProductType] and self._propsWithId[self._reqProductType].goods_list then
        for i = 1, #self._propsWithId[self._reqProductType].goods_list do
            if self._propsWithId[self._reqProductType].goods_list[i].id == productId then
                self._reqProductInfo = self._propsWithId[self._reqProductType].goods_list[i]
                break
            end
        end
        if multiCfgId and tonumber(multiCfgId) then
            self._multiBuyCfgId = tonumber(multiCfgId)
        end
    end
end

-- 请求兑换道具
-- realProductType 传金币兑换下面会一堆问题，需要转回去
function ShopModule:reqExchangeProduct(productType, productId, richType, propValue, multiCfgId, needTips, gift_cfg, propType, actPrice, realProductType)
    -- XH.TipTool.showLoading()
    self:saveProductNewMsg(productType, productId, multiCfgId)
    self._needShowExchangeTips = true
    if needTips == false then
        self._needShowExchangeTips = needTips
    end
    local userId = un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID())
    local gameId = XH.areaData:getConfigGameID()
    local _, getSecondTag = self:getTagsWithName(ShopConfig.SpecialTag.GOLD)
    if productType == ShopConfig.ProductType.GOLD or(richType and richType == "gold") or productType == getSecondTag then
        gameId = XH.areaData:getCommonGoldGameID()
    end
    if realProductType then
        productType = realProductType
    end
    local targetPropName = self._reqProductInfo and self._reqProductInfo.name or ""
    local targetPropCost = self._reqProductInfo and self._reqProductInfo.condition or 0
    if richType then
        if self._reqProductInfo.multi_goods_info and multiCfgId then
            for _, v in pairs(self._reqProductInfo.multi_goods_info[multiCfgId].multi_goods) do
                if v.type == richType then
                    targetPropCost = (actPrice or v.value) .. v.name
                    targetPropName = targetPropName .. self._reqProductInfo.multi_goods_info[multiCfgId].name
                    break
                end
            end
        else
            targetPropCost = (actPrice or self._reqProductInfo.multi_score_config[1].value) .. self._reqProductInfo.multi_score_config[1].name
        end
    else
        local costPropName = ShopConfig.PropName(self._reqProductInfo.cur_shop_propid)
        targetPropCost = (actPrice or targetPropCost) .. (costPropName or XH.ConstString.getStr("PROP_NAME_SHOP_DIAMOND"))
    end
    local tipStr = XH.ConstString.getStr("TIP_SHOP_ENOUGH_MID_PROP", targetPropCost, targetPropName)
    XH.TipTool.showTip( {
        type = XH.TIP_LAYER_TYPE.OK,
        funcOK = function()
            XH.TipTool.showLoading()
            self:startReq("ReqExchangeProduct", XH.playerData:getBrandID(), XH.areaData:getAreaWindID(), XH.playerData:getNumberID(), userId, gameId, productId, productType, XH.KW_CONFIG_LOGIC_TIME_OUT, propType, nil, richType, propValue, multiCfgId, gift_cfg)
        end
    } , tipStr)
    -- self:startReq("ReqExchangeProduct", XH.playerData:getBrandID(), XH.areaData:getAreaWindID(), XH.playerData:getNumberID(), userId, gameId, productId, productType, XH.KW_CONFIG_LOGIC_TIME_OUT, propType, nil, richType, propValue, multiCfgId, gift_cfg)
end

-- 判断是否刷新活动数据信息
function ShopModule:judgeUpdateActMsg(data)
    if data.data and data.data.nextJoinAct == false and self._reqProductInfo and self._reqProductInfo.id then
        if self._multiBuyCfgId then
            if self._isActOutIds[self._reqProductInfo.id] then
                self._isActOutIds[self._reqProductInfo.id][self._multiBuyCfgId] = -1
            else
                self._isActOutIds[self._reqProductInfo.id] = { }
                self._isActOutIds[self._reqProductInfo.id][self._multiBuyCfgId] = -1
            end
        else
            self._isActOutIds[self._reqProductInfo.id] = -1
        end
    elseif data.data and data.data.nextJoinAct == true then
        if self._propsWithId[self._reqProductType] and self._propsWithId[self._reqProductType].goods_list then
            for i = 1, #self._propsWithId[self._reqProductType].goods_list do
                if self._propsWithId[self._reqProductType].goods_list[i].id == self._reqProductInfo.id then
                    self._propsWithId[self._reqProductType].goods_list[i].goods_act.act_config.left_num = self._propsWithId[self._reqProductType].goods_list[i].goods_act.act_config.left_num - 1
                    break
                end
            end
        end
    end
end

-- 请求兑换结果
function ShopModule:respExchangeProduct(req, type, data)
    XH.TipTool.hideLoading()
    local bTip = true
    if XH.roomManager.isInGame then
        bTip = not XH.roomManager:isInGame()
        if not bTip then
            XH.TipTool.showToast("兑换成功",3)
            self:dispatchEvent({name = self.EVNET_SHOPMANAGER_HIDE_SHOP})
        end
    end
    local tipStr = req:getMessage()
    if type ==  XH.Req.TYPE.SUCCESS then
        self:judgeUpdateActMsg(data)
        self:dispatchEvent({name = self.EVENT_REQ_EXCHANGE_GOLD_SUCCESS})
        tipStr = data.message
        if self:getTagWithShop(self._reqProductInfo, ShopConfig.SpecialTag.MARKER) or self._reqProductType == ShopConfig.ProductType.MARKER then
            tipStr = "RESULT_SHOP_BY_MARKER_SUCCEED"
        end
        if self:getTagWithShop(self._reqProductInfo, ShopConfig.SpecialTag.VIP) then
            bTip = false
            local propList = XH.areaData:getPropList()
            if data.data.goods == "资深会员" or data.data.goods == "金币周卡" then
                XH.lobby:getModule("NewVip"):reqVipUser(propList.seniorVip)
            elseif data.data.goods == "超级会员" or data.data.goods == "金币月卡" then
                XH.lobby:getModule("NewVip"):reqVipUser(propList.superVip)
            end
        end
        XH.playerData:flushPlayerDrop()
        XH.playerData:flushGoldCoin()
        if data.propValue then --背包道具
            bTip = false
            XH.viewManager:openView("ShopHingMsgView", nil, data.propValue)
        end
        if self._propsWithId[self._reqProductType] and (self._propsWithId[self._reqProductType].tag == ShopConfig.SpecialTag.GOLD or self._propsWithId[self._reqProductType].tag == ShopConfig.SpecialTag.CARD) then
            if self._propsWithId[self._reqProductType].tag == ShopConfig.SpecialTag.GOLD then
                XH.taskManager:reqLastBuyProductID(self.KW_REQ_LAST_BUY_PRODUCT_ID_TYPE.SET_LAST_BUY_ID, tonumber(data.data.id), -1)
                XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_LAST_BUY_PRODUCT_ID_GOLD, tonumber(data.data.id))
            else
                XH.taskManager:reqLastBuyProductID(self.KW_REQ_LAST_BUY_PRODUCT_ID_TYPE.SET_LAST_BUY_ID, tonumber(data.data.id), XH.areaData:getPropRoomCardID())
                XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_LAST_BUY_PRODUCT_ID_CARD, tonumber(data.data.id))
            end
        end
    elseif type ==  XH.Req.TYPE.FAIL then
        if string.find(req:getMessage(), "库存不足") then
            XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK,
            }, req:getMessage())
            return
        end
        if req._propValue and #req._propValue ~= "" and string.find(req:getMessage(), "不足") then
            bTip = false
            local dispatchName = self.EVENT_SHOW_DIAMOND_VIEW
            if req._richType and string.find(req._richType, XH.areaData:getPropRoomCardID()) then
                dispatchName = self.EVENT_SHOW_CARD_VIEW
            elseif req._richType and string.find(req._richType, "gold") then
                dispatchName = self.EVENT_SHOW_GOLD_VIEW
            end
            local okFunc = function ()
                self:dispatchEvent({name = dispatchName})
            end
            XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK_CANCEL,
                funcOK = okFunc
            }, "您的道具不足，是否前往购买？")
        end
    end
    if self:getTagWithShop(self._reqProductInfo, ShopConfig.SpecialTag.GIFT) then
        local msg = {}
        msg.status = 0
        if type ==  XH.Req.TYPE.SUCCESS then
            bTip = false
            msg.status = XH.Req.TYPE.SUCCESS
        end
        --礼包兑换
        self:dispatchEvent({name = self.EVENT_REQ_EXCHANGE_GIFT_BOX_SUCCESS,data = msg})
        self:onUserBuyGigt(msg)
    end
    if bTip and self._needShowExchangeTips then
        if type ==  XH.Req.TYPE.SUCCESS then
            if self._propsWithId[self._reqProductType] and (self._propsWithId[self._reqProductType].tag == ShopConfig.SpecialTag.GOLD or self._propsWithId[self._reqProductType].tag == ShopConfig.SpecialTag.CARD) then
                self:showTipsWithPhoneBind(tipStr)
            elseif self:getTagWithShop(self._reqProductInfo, ShopConfig.SpecialTag.WECHATPACKTE) then
                tipStr = "RESULT_SHOP_BY_WECHAT_PACKTE"
                local func = function()
                end
                XH.TipTool.showTip({
                    type = XH.TIP_LAYER_TYPE.OK_CANCEL,
                    funcOK = func
                }, tipStr)
            else
                XH.TipTool.showTip({
                    type = XH.TIP_LAYER_TYPE.OK,
                }, tipStr)
            end
        else
            XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK,
            }, tipStr)
        end
    end
    self:dispatchEvent({name = self.EVNET_SHOPMANAGER_SHOW_SHOP})
end

-- 购买钻石结果
function ShopModule:respBuyDiamond(event)
    -- 场景在新金币场
    if XH.SceneManager:getInstance():getTopSceneTag() == 1001 or XH.SceneManager:getInstance():getTopSceneTag() == 1002 then
        return
    end
    local code = event.data.code
    local msg = event.data.msg
    XH.TipTool.hideLoading()

    local isShowTip = false
    local tipStr = msg
    if code == 200 then
        isShowTip = true
        --200001 补单信息不提示
        for _ , value in pairs(event.data) do
            if string.find(value, 200001) then
                isShowTip = false
                break
            end
        end
        if self._bHideBuySuccessTip then
            isShowTip = false
        end
        if XH.lobby:getModule("YGiftBankruptcy"):isPayWithGift(self._buyParam) then
            isShowTip = false
        end
        tipStr = "购买成功"
        XH.playerData:flushPlayerDrop()
        self:dispatchEvent( { name = self.EVNET_DIAMOND_BUY_RESULT, msg = {resultTag = self.BUY_DIAMOND_RESULT.SUCCESS} })
        XH.taskManager:reqLastBuyProductID(self.KW_REQ_LAST_BUY_PRODUCT_ID_TYPE.SET_LAST_BUY_ID, tonumber(self._buyParam.productInfo.id), XH.areaData:getPropDiamndID())
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_LAST_BUY_PRODUCT_ID_DIAMOND, tonumber(self._buyParam.productInfo.id))
        --talkingdata插件
        if device.platform == "ios" then 
            local orderId = event.data.msg                      --IOS订单号
            self:onPayTalkingDataSDK(orderId)
            self._buyParam = { }
        end
        if device.platform == "android" then 
            self:getAndroidOrderId()
        end
    elseif code == 208 or code == 209 then
        -- 漏单恢复,重新下单
        local data

        pcall( function()
            data = cjson.decode(msg)
        end )

        if data and data.product_id then
            if string.find(data.product_id, "GIFT") then
                --个性化礼包漏单
                XH.lobby:getModule("PersonalGiftPack"):buyDiamond(data.product_id, nil, 1)
                return
            end
            local productInfo = nil
            local productInfos = self:getCurrentAreaProps(ShopConfig.ProductType.DIAMOND)
            if productInfos == nil then
                return
            elseif #productInfos == 0 then
                return
            end
            for _, v in pairs(productInfos) do
                if v.productId == data.product_id then
                    productInfo = v
                    break
                end
            end
            if productInfo then
                self:reqBuyDiamond(productInfo, nil, nil, 1)
            end
        end
    elseif code == 201 then -- 充值限额
        if string.find(msg , "订阅信息失败") then
            tipStr = "订阅失败"
        else
            local infoList = XH.StringTool.splitWithTrim(msg, '|')
            for i, infoItem in pairs(infoList) do
                if i == 2 then
                    tipStr = infoItem
                end
            end
        end
        isShowTip = true
    elseif code == 202 then
        -- 用户取消
        tipStr = "支付取消"
        isShowTip = true
        self:dispatchEvent( { name = self.EVNET_DIAMOND_BUY_RESULT, msg = {resultTag = self.BUY_DIAMOND_RESULT.CANCEL} })
    elseif code == 211 then
         -- 恢复购买成功
         isShowTip = false
    elseif code == 210 then
         -- 交易异常
         isShowTip = true
         tipStr = "交易异常"
    end

    if isShowTip then
        if code == 200 then
            self:showTipsWithPhoneBind(tipStr)
        elseif code == 202 and next(self._buyParam) then
            -- 会员模块不处理重新支付逻辑
            if self._buyParam.productInfo.module and self._buyParam.productInfo.module == "Sxvip" then
                XH.TipTool.showTip( {
                    type = XH.TIP_LAYER_TYPE.OK,
                } , tipStr)
                return
            end
            XH.viewManager:openView("ShopPayCancel", nil, self._buyParam)
            XH.throwDataManager:throwData(XH.ThrowDataDefine.ShopPayCancelView, {userid = XH.playerData:getNumberID()})
        else
            XH.TipTool.showTip( {
                type = XH.TIP_LAYER_TYPE.OK,
            } , tipStr)
        end
    end

    self:dispatchEvent( { name = self.EVNET_SHOPMANAGER_SHOW_SHOP })
    if self._fishPayCallBack then
        self._fishPayCallBack(code, msg)
    end
end


-------------对外接口-----------
-- 是否已经获取过道具列表
function ShopModule:isHanveProductList(productType)
    local props = self:getCurrentAreaProps(productType)
    return #props > 0
end

-- 获取道具列表
function ShopModule:getProductList(productType)
    return self:getCurrentAreaProps(productType)
end

-- 购买钻石
function ShopModule:reqBuyDiamondProduct(productType, selectIdx)
    local props = self:getShopItemsWithId(productType)
    if props and props[selectIdx] then
        self:reqBuyDiamond(props[selectIdx])
    end
end

-- 购买商品
function ShopModule:reqBuyProduct(productType, selectIdx, gift_cfg, actPrice)
    local props = self:getShopItemsWithId(productType)
    if props.goods_list == nil and props.goods_list[selectIdx] == nil then
        return
    end
    local productInfo = props.goods_list[selectIdx]
    -- 礼包特殊兑换
    if productInfo and self:getTagWithShop(productInfo, ShopConfig.SpecialTag.GIFT) and productInfo.multi_score_config then
        self:reqExchangeProduct(productType, productInfo.id, productInfo.multi_score_config[1].type, nil, nil, true, gift_cfg, productInfo.prop_type, actPrice)
        return
    end
    -- 通用兑换
    if productInfo.multi_score_config and #productInfo.multi_score_config > 0 then
        self:reqExchangeProduct(productType, productInfo.id, productInfo.multi_score_config[1].type, productInfo.value, gift_cfg, nil, nil, productInfo.prop_type, actPrice)
    else
        self:reqExchangeProduct(productType, productInfo.id, nil, nil, nil, nil, nil, productInfo.prop_type, actPrice)
    end
    self:sendCloseCouponView()
end

-- 是否有足够的兑换道具
function ShopModule:isHaveEnoughDiamond(productType, selectIdx, cutPrize)
    local isEnough = false
    local isHideTips = false
    local playerPropsInfo = XH.playerData:getPlayerDrop()
    local props = self:getShopItemsWithId(productType)
    if props.goods_list == nil and props.goods_list[selectIdx] == nil then
        return
    end
    local productInfo = props.goods_list[selectIdx]
    local needPropName = nil
    local needPropId = XH.areaData:getPropList().propDiamndID
    local needCostPrize
    local tipMsg = "ERROR_SHOP_DIAMOND_NOT_ENOUGH"
    if self._propsWithId[productType] and self._propsWithId[productType].tag == ShopConfig.SpecialTag.DIAMOND then
        isEnough = true
    elseif productInfo.multi_score_config and #productInfo.multi_score_config > 0 then
        if #productInfo.multi_score_config == 1 then
            needPropId = XH.StringTool.getLastCharAndToNum(productInfo.multi_score_config[1].type)
            local personPropCount = playerPropsInfo[needPropId] or 0
            if cutPrize then
                if personPropCount < tonumber(cutPrize) then
                    needCostPrize = tonumber(productInfo.multi_score_config[1].value)
                else
                    needCostPrize = tonumber(cutPrize)
                end
            else
                needCostPrize = tonumber(productInfo.multi_score_config[1].value)
            end
            if needPropId and needCostPrize <= personPropCount then
                isEnough = true
            end
            needPropName = self:getShopNameById(needPropId)
        end
    else
        local personPropCount = playerPropsInfo[productInfo.cur_shop_propid] or 0
        if cutPrize then
            if personPropCount < tonumber(cutPrize) then
                needCostPrize = tonumber(productInfo.condition)
            else
                needCostPrize = tonumber(cutPrize)
            end
        else
            needCostPrize = tonumber(productInfo.condition)
        end
        if productInfo.cur_shop_propid and needCostPrize <= personPropCount then
            isEnough = true
        end
        needPropId = productInfo.cur_shop_propid
        needPropName = self:getShopNameById(productInfo.cur_shop_propid)
    end
    -- 兑换商城
    if (not isEnough) and self:checkSelfIsLiQuanExchange(needPropId, needCostPrize) then 
        if ShopConfig.NotEnoughPropsTips and ShopConfig.NotEnoughPropsTips[needPropName] then
            tipMsg = ShopConfig.NotEnoughPropsTips[needPropName]
        end
        return isEnough, tipMsg, isHideTips
    end
    -- 支持快捷购买
    if (not isEnough) and self:checkSelfSupExchange(needPropId, needCostPrize) then
        if ShopConfig.NotEnoughPropsTips and ShopConfig.NotEnoughPropsTips[needPropName] then
            tipMsg = ShopConfig.NotEnoughPropsTips[needPropName]
        end
        return isEnough, tipMsg, isHideTips
    elseif (not isEnough) and (XH.areaData:isSupportGoldQuickRecharge() or false) and self:checkQuickReChargeType(productInfo.tags[1].key) then
        local targetPropNum = productInfo.type == "gold" and tonumber(productInfo.value) or tonumber(productInfo.prop_count)
        local targetPropId = productInfo.type == "gold" and -1 or tonumber(productInfo.value)
        local bShelvesSuppport, shelvesInfo = self:checkMaxShelvesEnough(needPropId, needCostPrize)
        if (needPropId ~= XH.areaData:getPropList().propDiamndID and bShelvesSuppport and shelvesInfo) or 
            (needPropId == XH.areaData:getPropList().propDiamndID and bShelvesSuppport) then
            self:arrangeQuickInfo(needPropId, needCostPrize, productInfo.name, targetPropNum, targetPropId, shelvesInfo, productInfo.id, productInfo.goods_act)
            isHideTips = true
        end
    end
    if ShopConfig.NotEnoughPropsTips and ShopConfig.NotEnoughPropsTips[needPropName] then
        tipMsg = ShopConfig.NotEnoughPropsTips[needPropName]
    end
    return isEnough, tipMsg, isHideTips, needCostPrize
end

-- 整理道具不足信息
-- @param needPropId 需要消耗的道具id
-- @param needCostPrize 需要消耗的道具数量
-- @param targetName 最终需要兑换的商品名称
-- @param targetPropNum 最终需要兑换的商品数量
-- @param targetPropId 最终需要兑换的道具id
-- @return 无
function ShopModule:arrangeQuickInfo(needPropId, needCostPrize, targetName, targetPropNum, targetPropId, shelvesInfo, productId, goodAct)
    if not needPropId or not needCostPrize then
        return
    end
    local param = { }
    local product
    if needPropId ~= XH.areaData:getPropList().propDiamndID then
        product = shelvesInfo
        param.needCost = needCostPrize
        param.product = shelvesInfo
        param.targetDiamond = shelvesInfo.condition
        param.targetValue = shelvesInfo.type == "gold" and tonumber(shelvesInfo.value) or tonumber(shelvesInfo.prop_count)
    else
        param.targetDiamond = needCostPrize
        param.targetValue = targetPropNum
    end
    param.name = targetName
    param.giftType = TaskDefine.TASK_ID.TASK_ID_QUICK_RECHARGE .. "_" .. needPropId .. "_" .. needCostPrize .. "_" .. targetPropId .. "_" .. targetPropNum .. "_" .. XH.areaData:getAreaID() .. "_" .. XH.playerData:getNumberID()
    if product then
        param.giftType = param.giftType .. "_" .. XH.areaData:getPropList().propDiamndID
        param.giftType = param.giftType .. "_" .. product.condition
        param.giftType = param.giftType .. "_" .. (product.type == "gold" and -1 or tonumber(product.value))
        param.giftType = param.giftType .. "_" .. (product.type == "gold" and tonumber(product.value) or product.prop_count)
    end
    param.giftType = param.giftType .. "_" .. productId
    if goodAct and goodAct.act_type then
        param.goodAct = goodAct
    end
    param.targetPropId = targetPropId
    XH.viewManager:openView("NotEnoughPropsView", nil, param)
end

function ShopModule:isHaveEnoughCurrency(targetProp, targetNum)
    if not targetProp or not targetNum then
        return false
    end
    if targetProp == -1 then
        local playerSR = XH.playerData:getSR()
        if playerSR >= targetNum then
            return true
        end
    end
    local playerPropsInfo = XH.playerData:getPlayerDrop()
    if playerPropsInfo[targetProp] and playerPropsInfo[targetProp] >= targetNum then
        return true
    end
    return false
end

function ShopModule:isAleardyRealName()
    local bIdentityStatus = XH.playerData:getIdentityStatus()
    if XH.configerManager:getIsNewRealName() then 
        return bIdentityStatus == 0 or bIdentityStatus == 1
    else
        return bIdentityStatus == 2
    end
end

function ShopModule:reqSystemTimes()
    self:startReq("ReqSystemTimes", XH.playerData:getNumberID(), 0, 120, TaskDefine.KW_SYSTEM_TIME, XH.areaData:getSrsGroupID(), 10)
end

function ShopModule:onReqSystemTimesCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local timestamp = json.decode(data.acData).time
        if timestamp ~= nil then
            self._diffTime = timestamp - os.time()
            self._timestamp = timestamp
            self:dispatchEvent({name = self.EVENT_SHOP_REFRESH_SYSTEM, data = timestamp})
        end
    end
    self:dispatchEvent({name = self.EVENT_SHOP_REFRESH_SYSTEM, data = os.time()})
end

function ShopModule:getServerTime()
    local diffTime = self._diffTime or 0
    if self._timestamp then
        return os.time() + self._diffTime
    end
    return os.time()
end

function ShopModule:getSysTimestamp()
    return self._timestamp or os.time()
end

function ShopModule:getPropsDataByType(propType)
    if not propType then
        return {}
    end
    self._allProps[propType] = self._allProps[propType] or {}
    return self._allProps[propType]
end

function ShopModule:getCurrentAreaProps(propType)
    if not propType then
        return {}
    end
    return self:getPropsByTypeAndAreaID(propType, XH.areaData:getAreaID())
end

function ShopModule:getPropsByTypeAndAreaID(propType, areaID)
    if not propType or not areaID then
        return {}
    end
    local props = self:getPropsDataByType(propType)
    props[areaID] = props[areaID] or {}
    return props[areaID]
end

function ShopModule:clearPropsThisAreaID(propType)
    if not propType then
        return
    end
    if not self._allProps[propType] then
        return
    end
    self._allProps[propType][XH.areaData:getAreaID()] = {}
end

function ShopModule:setCurrentAreaProps(propType, data)
    if not propType or not data then
        return
    end
    ---- 仅配一种支付方式
    for i = 1, #data do
        if data[i].multi_score_config and #data[i].multi_score_config == 1 then
            data[i].price = tonumber(data[i].multi_score_config[1].value)
            data[i].richType = data[i].multi_score_config[1].type
        end
    end

    -- 按照sort 排序
    table.sort(data,function(a,b)
        if a.sort and b.sort then
            return a.sort > b.sort
        end
        return false
    end)

    -- 订阅和非订阅进行分类处理，合并到一个table里面


    self:getPropsDataByType(propType)[XH.areaData:getAreaID()] = data
end

function ShopModule:showTipsWithPhoneBind(tipStr)
    local getPhoneBindState = XH.userDefault:getValue(XH.userDefault.KEY_ID.LAST_PHONE_STATE, "")
    local phoneBindShopTimes = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_PHONE_BIND_SHOP_CHOOSE, 0)
    if XH.playerData:getPlayerPhone() == "" and getPhoneBindState == "" and phoneBindShopTimes == 0 then
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PHONE_BIND_SHOP_CHOOSE, 1)
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK,
            funcOK = function()
                self:showBindTips()
            end,
            funcClose = function()
                self:showBindTips()
            end
        } , tipStr)
    else
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK,
        } , tipStr)
    end
end

function ShopModule:showBindTips()
    XH.TipTool.showTip( {
        type = XH.TIP_LAYER_TYPE.OK,
        funcOK = function()
            -- 启用一键绑定流程
            XH.login:getModule("Login"):reqGYInfo(true)
        end
    } , "是否绑定手机号")
end

-- 购买商品
function ShopModule:showGiftContent(productType, selectIdx, displaySecondType, cutPrice)
    local props = self:getShopItemsWithId(productType)
    if props.goods_list == nil and props.goods_list[selectIdx] == nil then
        return
    end
    local productInfo = props.goods_list[selectIdx]
    local leftBtnTypeData = XH.lobby:getModule("BagSysNew"):getBagData():getDevideBagConfigDataTag()
    if leftBtnTypeData.gift then
        local selectgiftidx = 0
        for i = 1, #leftBtnTypeData.gift do
            if leftBtnTypeData.gift[i].propsid == tonumber(productInfo.value) then
                selectgiftidx = i
                break
            end
        end
        if selectgiftidx > 0 then
            local gift_type = leftBtnTypeData.gift[selectgiftidx].props_gift_send_type
            if gift_type == 0 then
                XH.viewManager:openView("ShopGiftChooseView", nil, productInfo, leftBtnTypeData.gift[selectgiftidx], selectIdx, productType, cutPrice)
            elseif gift_type == 1 then
                XH.viewManager:openView("ShopGiftChanceView", nil, productInfo, leftBtnTypeData.gift[selectgiftidx], selectIdx, productType, cutPrice)
            else
                XH.TipTool.showToast("当前礼包暂未开放,请联系客服", 2)
            end
        else
            XH.TipTool.showToast("当前礼包暂时不能购买", 2)
        end
    end
end

-- 请求标签列表
function ShopModule:reqShopPageMarkTop()
    self:initShopNewDatas()
    self:startReq("ReqShopPageMarkTop", XH.playerData:getNumberID(), XH.areaData:getNotifyGameID(), XH.areaData:getAreaID(), XH.areaData:getAreaWindID(), un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID()), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function ShopModule:onReqShopPageMarkTopCallBack(req, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local getTagLists = { }
        for _, value in pairs(data) do
            if value.sub and #value.sub > 0 then
                getTagLists[#getTagLists + 1] = value
            end
        end
        self._allPropTags = getTagLists
        self:dispatchEvent({name = self.EVNET_SHOP_PAGE_MARK_TOP, data = getTagLists})
    end
end

-- 请求指定标签下的商品
function ShopModule:reqShopProductByConfig(id)
    XH.TipTool.showLoading()
    self._hasReqItems = id
    self:startReq("ReqShopProductByConfig", XH.playerData:getNumberID(), XH.areaData:getNotifyGameID(), XH.areaData:getAreaID(), XH.areaData:getAreaWindID(), un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID()), id, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function ShopModule:onReqShopProductByConfigCallBack(req, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local specialTag
        if data and data.id then
            self._propsWithId[data.id] = data
            specialTag = self._propsWithId[data.id].tag
            self:judgeIsSpecialTag(data.id)
            -- sxvip 标签下 订阅和非订阅商品，需要合并在一块变成一个table
            if specialTag == ShopConfig.SpecialTag.SXVIP then
                local goodsList = data.goods_list or {}
                local mergedGoodTable = {}
                local mergedGoodList = {}
                -- 根据价格分类
                for _, goods in ipairs(goodsList) do
                    local merge_key = goods.value .. "_" ..goods.prop_count
                    if not mergedGoodTable[merge_key] then
                        mergedGoodTable[merge_key] = {}
                    end
                    table.insert(mergedGoodTable[merge_key], goods)
                end
                -- 合并新的列表
                for merge_key, goodsList in pairs(mergedGoodTable) do
                    local tmpGoods = {
                        ["merge_key"] = merge_key,
                    }
                    for _, goods in ipairs(goodsList) do
                        goods.product_id = goods.goods_mark
                        if goods.tags[1].key then --根据标签记录商品
                            tmpGoods[goods.tags[1].key] = goods
                        end
                    end
                    table.insert(mergedGoodList , tmpGoods)
                end
                -- 排序
                table.sort(mergedGoodList , function(a , b) 
                    return tonumber(string.split(a["merge_key"] , "_")[2]) <  tonumber(string.split(b["merge_key"] , "_")[2])
                end)
                data.goods_list = mergedGoodList
            end
        elseif self._hasReqItems then
            self._propsWithId[self._hasReqItems] = {}
            self._hasReqItems = nil
        end
        self:dispatchEvent( { name = self.EVENT_PAGE_PRODUCTS_CONFIG, data = {tag = specialTag} })
        XH.TipTool.hideLoading()
    else
        XH.TipTool.hideLoading()
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK,
        } , req:getMessage())
    end
end

-- 判断特殊标签（热门推荐 去除vip相关）
function ShopModule:judgeIsSpecialTag(id)
    if self._propsWithId[id] and(self._propsWithId[id].tag == ShopConfig.SpecialTag.HOT or self._propsWithId[id].tag == ShopConfig.SpecialTag.NEW or self._propsWithId[id].tag == ShopConfig.SpecialTag.FAV) then
        local propsInfo = { }
        for i = 1, #self._propsWithId[id].goods_list do
            if not self:getTagWithShop(self._propsWithId[id].goods_list[i], ShopConfig.SpecialTag.VIP) then
                propsInfo[#propsInfo + 1] = self._propsWithId[id].goods_list[i]
            end
        end
        self._propsWithId[id].goods_list = propsInfo
    end
end

-- 登录成功获取商品信息
function ShopModule:onLoginSuccessCallBack(event)
    self:reqIsNewShopItem()
end

-- 请求是否有新商品
function ShopModule:reqIsNewShopItem()
    self._isNewItems = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_SHOP_NEW_ITEMS_LIST, "{}")
    self._isChoseItems = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_PERSON_NEW_ITEMS_CHOOSE, "{}")
    pcall( function()
        self._isNewItems = cjson.decode(self._isNewItems)
        self._isChoseItems = cjson.decode(self._isChoseItems)
    end )
    self._isChoseItems = self._isChoseItems or { }
    self._isActOutIds = { }
    self:startReq("ReqTaskIsNewItem", TaskDefine.TASK_ID.TASK_TYPE_NEW_SHOP_RED_POINT, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, "")
end

-- 请求新商品回调
function ShopModule:onReqTakIsNewItemCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data.acData and data.acData ~= "" then
            local newShopData
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_SHOP_NEW_ITEMS_LIST, data.acData)
            pcall( function()
                newShopData = cjson.decode(data.acData)
            end )
            if newShopData then
                self._isNewItems = newShopData
                self:judgeIsNewShopData()
            end
        end
    end
end

-- 判断是否有新商品
function ShopModule:judgeIsNewShopData(choseTag)
    local shopNewState = self:getNewShopData(choseTag)
    self:dispatchEvent( { name = self.EVENT_SHOP_NEW_ITEM, data = shopNewState })
end

-- 判断是否存在新商品（设置最新点击 -> 判断子节点是否全被点击 -> 隐藏父节点）
function ShopModule:getNewShopData(choseTag)
    local shopNewTags = { }
    local needShowRedTags = { }
    local shopNewState = false
    -- 先排除未开始就记录的红点
    for key, value in pairs(self._isNewItems) do
        if value >= KW_DEFAULT_TIME_RED_POINT then
            needShowRedTags[key] = value
        end
    end
    if choseTag then
        if needShowRedTags[tostring(choseTag)] then
            self._isChoseItems[tostring(choseTag)] = needShowRedTags[tostring(choseTag)]
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PERSON_NEW_ITEMS_CHOOSE, json.encode(self._isChoseItems))
        end
    end
    local getParentNode = self:getFirstListBySecondTag(choseTag)
    if getParentNode then
        local parentIsChose = true
        for _, value in pairs(getParentNode.sub) do
            if self._isChoseItems[tostring(value.id)] ~= needShowRedTags[tostring(value.id)] then
                parentIsChose = false
            end
        end
        if parentIsChose then
            self._isChoseItems[tostring(getParentNode.id)] = needShowRedTags[tostring(getParentNode.id)]
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PERSON_NEW_ITEMS_CHOOSE, json.encode(self._isChoseItems))
        end
    end
    for newTag, newTagTime in pairs(needShowRedTags) do
        if self._isChoseItems[tostring(newTag)] == nil or self._isChoseItems[tostring(newTag)] ~= newTagTime then
            shopNewTags[tostring(newTag)] = 1
            shopNewState = true
        end
    end
    return shopNewState, shopNewTags
end

--发送关闭优惠卡券界面消息
function ShopModule:sendCloseCouponView()
    self:dispatchEvent({name = self.EVNET_SHOP_CLOSE_COUPON_VIEW})
end
-- 获取商品购买记录
function ShopModule:reqShopBuyLogs()
    self._bugLogs = {}
    local taskString = ";buyLog = " .. TaskDefine.TASK_ID.TASK_TYPE_NEW_SHOP_ITEM_LOG
    self:startReq("ReqTaskBuyLogs", TaskDefine.TASK_ID.TASK_TYPE_NEW_SHOP_RED_POINT, 0, 0, XH.areaData:getSrsGroupID(), 5, taskString)
end

-- 请求购买商品记录回调
function ShopModule:onReqShopBuyLogsCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data.acData and data.acData ~= "" then
            local buyLogs
            pcall( function()
                buyLogs = cjson.decode(data.acData)
            end )
            if buyLogs then
                self._bugLogs = buyLogs.itemsLog
                self._timeAutoAddStamp = buyLogs.time
                self:startAddTime()
            end
        end
    else
        XH.TipTool.hideLoading()
    end
    self:dispatchEvent( { name = self.EVENT_START_SHOW_PRODUCTS })
end

function ShopModule:startAddTime()
    if self._scheToShow then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._scheToShow)
    end
    self._scheToShow = cc.Director:getInstance():getScheduler():scheduleScriptFunc( function()
        if self._timeAutoAddStamp then
            self._timeAutoAddStamp = self._timeAutoAddStamp + 1
        end
    end , 1, false)
end

-- 初始化本地数据
function ShopModule:initShopNewDatas()
    self._propsWithId = { }
    self._allPropTags = { }
end

-- 判断活动是否到期
function ShopModule:judgeIsActOut(goodsAct, productData, multiProductInfo)
    multiProductInfo = multiProductInfo or { }
    local time = self:getSystemTime()
    if goodsAct and goodsAct ~= "" and goodsAct.promote_start then
        if time < goodsAct.promote_start or time > goodsAct.promote_end then
            return false
        end
        if tonumber(goodsAct.act_config.left_num) <= 0 then
            return false
        end
        if self:getBuyLogByTime(productData.gid, multiProductInfo.multi_cfg_id, goodsAct.act_config.cycle) >= tonumber(goodsAct.act_config.each_num) then
            return false
        end
    else
        return false
    end
    if self:getIsActOut(productData.id) then
        if type(self:getIsActOut(productData.id)) == "table" then
            for multId, _ in pairs(XH.lobby:getModule("Shop"):getIsActOut(productData.id)) do
                if multId == multiProductInfo.multi_cfg_id then
                    return false
                end
            end
        else
            return false
        end
    end
    if goodsAct.act_type ~= ShopConfig.ProductAct.SEND then
        if goodsAct.act_config[goodsAct.act_type] and type(goodsAct.act_config[goodsAct.act_type]) == "table" then
            local isHaveCheckPrize = false
            for i = 1, #goodsAct.act_config[goodsAct.act_type] do
                if goodsAct.act_config[goodsAct.act_type][i].checked == "1" then
                    isHaveCheckPrize = true
                    break
                end
            end
            if isHaveCheckPrize == false then
                return false
            end
        end
    end
    return true
end

-- 返回某个商品某个时间的购买记录
function ShopModule:getBuyLogByTime(productid, multigid, cycle)
    multigid = multigid or 0
    if cycle == "" then
        cycle = nil
    end
    if productid and self._bugLogs[tostring(productid)] and self._bugLogs[tostring(productid)][tostring(multigid)] then
        local dayTime = self._bugLogs[tostring(productid)][tostring(multigid)].day or 0
        local weekTime = self._bugLogs[tostring(productid)][tostring(multigid)].week or 0
        local monthTime = self._bugLogs[tostring(productid)][tostring(multigid)].month or 0
        if cycle == "day" then
            return dayTime
        end
        if cycle == "week" then
            return(weekTime + dayTime)
        end
        if cycle == "month" then
            return(monthTime + weekTime + dayTime)
        end
        return self._bugLogs[tostring(productid)][tostring(multigid)]["all"] or 0
    end
    return 0
end

-- 获取某一id下商品
function ShopModule:getShopItemsWithId(id)
    return self._propsWithId[id]
end

-- 获取所有标签
function ShopModule:getShopAllTags()
    return self._allPropTags or {}
end

function ShopModule:getFirstTagsWithTag(firstTag)
    for _, firstList in pairs(self._allPropTags) do
        if firstList.id == firstTag then
            return firstList
        end
    end
    return { }
end

function ShopModule:getSecondTagsWithTag(firstTag, secondTag)
    local firstList = self:getFirstTagsWithTag(firstTag)
    if firstList.sub then
        for _, secondList in pairs(firstList.sub) do
            if secondList.id == secondTag then
                return secondList
            end
        end
    end
    return { }
end

function ShopModule:getTagsWithName(tagName)
    if tagName and tagName ~= "" then
        for _, firstList in pairs(self._allPropTags) do
            for _, secondList in pairs(firstList.sub) do
                if secondList.tag == tagName then
                    return firstList.id, secondList.id
                end
            end
        end
    end
    return nil, nil
end

function ShopModule:getFirstListBySecondTag(secondTag)
    if secondTag then
        for _, firstList in pairs(self._allPropTags) do
            for _, secondList in pairs(firstList.sub) do
                if secondList.id == secondTag then
                    return firstList
                end
            end
        end
    end
    return nil
end

-- 获取时间接口（会递增）
function ShopModule:getSystemTime()
    return self._timeAutoAddStamp or os.time()
end

-- 获取是否存在某标签
function ShopModule:getTagWithShop(productInfo, tag)
    if productInfo.tags and productInfo.tags ~= "" then
        for i = 1, #productInfo.tags do
            if productInfo.tags[i].key == tag then
                return true
            end
        end
    end
    return false
end

-- 设置活动是否过期
function ShopModule:getIsActOut(id)
    if id then
        return self._isActOutIds[id]
    end
end

-- 获取到道具命名
function ShopModule:getShopNameById(propId)
    if propId == 0 then
        return "gold"
    end
    local propList = XH.areaData:getPropList()
    for propName, propValue in pairs(propList) do
        if propValue == propId then
            return propName
        end
    end
    return nil
end

-- 单道具获取到商品消耗图片
function ShopModule:getShopPropImage(propId)
    if propId then
        local getPropName = self:getShopNameById(propId)
        if getPropName and ShopConfig.ProductDetailIcon then
            return ShopConfig.ProductDetailIcon[getPropName]
        end
    end
    return nil
end

-- 多道具商品获取图片配置
function ShopModule:getMoreItemsShopPropImage(propType)
    if propType then
        if ShopConfig.ProductDetailIcon[propType] then
            return ShopConfig.ProductDetailIcon[propType]
        end
        local propId = XH.StringTool.getLastCharAndToNum(propType)
        local getPropName = self:getShopNameById(propId)
        if getPropName and ShopConfig.ProductDetailIcon then
            return ShopConfig.ProductDetailIcon[getPropName]
        end
    end
    return nil
end

function ShopModule:onUserBuyGigt(msg)
    if msg and msg.status == XH.Req.TYPE.SUCCESS then
        local productInfo = self._reqProductInfo
        local productName = productInfo.name or ""
        if string.find(productName,"女神") then
            XH.lobby:getModule("GiftBag"):buyWomensDayGift()
        end
    end
end

function ShopModule:checkQuickReChargeType(productType)
    if not productType then
        return false
    end
    for _, v in pairs(ShopConfig.QuickReChargeType) do
        if productType == v then
            return true
        end
    end
    return false
end

function ShopModule:checkMaxShelvesEnough(propId, propNum)
    if propId == XH.areaData:getPropList().propDiamndID then
        local propTag = ShopConfig.SpecialTag.DIAMOND
        local _, currencySubId = XH.lobby:getModule("Shop"):getTagsWithName(propTag)
        local productList = XH.lobby:getModule("Shop"):getShopItemsWithId(currencySubId)
        table.sort(productList, function(a, b)
            return tonumber(a.value) < tonumber(b.value)
        end)
        if tonumber(productList[#productList].value) >= propNum then
            return true
        else
            return false
        end
    end
    local propTag = propId == XH.areaData:getPropList().roomCardID and ShopConfig.SpecialTag.CARD or ShopConfig.SpecialTag.GOLD
    local _, currencySubId = XH.lobby:getModule("Shop"):getTagsWithName(propTag)
    local productList = XH.lobby:getModule("Shop"):getShopItemsWithId(currencySubId)
    local goods = clone(productList.goods_list)
    local myProp
    if propId == XH.areaData:getPropList().roomCardID then
        table.sort(goods, function(a, b)
            return a.prop_count < b.prop_count
        end)
        myProp = XH.playerData:getRoomCard()
    else
        table.sort(goods, function(a, b)
            return tonumber(a.value) < tonumber(b.value)
        end)
        myProp = XH.playerData:getSR()
    end
    for _, v in pairs(goods) do
        local tmpValue = v.type == "gold" and tonumber(v.value) or tonumber(v.prop_count)
        if tmpValue + myProp >= propNum then
            return true, v
        end
    end
    return false
end

function ShopModule:checkSelfSupExchange(propId, propNum)
    if propId == XH.areaData:getPropList().propDiamndID then
        return
    end
    local selfDiamondCnt = XH.playerData:getDiamnd()
    local selfPropCnt = propId == XH.areaData:getPropList().roomCardID and XH.playerData:getRoomCard() or XH.playerData:getSR()
    local propTag = propId == XH.areaData:getPropList().roomCardID and ShopConfig.SpecialTag.CARD or ShopConfig.SpecialTag.GOLD
    local _, currencySubId = XH.lobby:getModule("Shop"):getTagsWithName(propTag)
    local productList = XH.lobby:getModule("Shop"):getShopItemsWithId(currencySubId)
    for _, v in pairs(productList.goods_list) do
        local tmpValue = v.type == "gold" and tonumber(v.value) or tonumber(v.prop_count)
        if tmpValue + selfPropCnt >= propNum and selfDiamondCnt >= v.condition then
            return true
        end
    end
    return false
end

function ShopModule:checkSelfIsLiQuanExchange(propId,propNum)
    if propId == XH.areaData:getPropList().couponID then
        return true 
    else
        return false
    end
end

-- 快捷兑换task通知结果
function ShopModule:onRespNotifyTaskPercent(event)
    local respData =  XH.MatchLinkProtocol.RespNotifyTaskPercent:new()
    respData:bistream(event.msg.buff, event.msg.len)
    if respData.nTaskId == TaskDefine.TASK_ID.TASK_ID_QUICK_RECHARGE then
        if respData.acData and respData.acData ~= "" then
            local data = json.decode(respData.acData)
            if data and data.status == 1 then
                XH.playerData:flushPlayerDrop()
                XH.playerData:flushGoldCoin()
                if data.propValue then
                    XH.viewManager:openView("ShopHingMsgView", nil, data.propValue)
                else
                    XH.TipTool.showToast("兑换成功")
                end
                if data.productid and data.propid then
                    if tonumber(data.propid) == XH.areaData:getPropRoomCardID() then
                        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_LAST_BUY_PRODUCT_ID_CARD, tonumber(data.productid))
                    else
                        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_LAST_BUY_PRODUCT_ID_GOLD, tonumber(data.productid))
                    end
                    self:dispatchEvent( { name = self.EVNET_SHOPMANAGER_SHOW_SHOP })
                end
            else
                XH.TipTool.showToast("自动兑换失败")
            end
        end
    elseif respData.nTaskId == TaskDefine.TASK_ID.TASK_TYPE_HOTYUN_RECHARGE then
        if respData.acData and respData.acData ~= "" then
            local data = json.decode(respData.acData)
            if data and data.transactionId and data.currencyAmount and data.paymentType then
                if XH.sdkManager:checkCanUseReYun() then
                    local jsonData = {}
                    jsonData.transactionId = tostring(data.transactionId)
                    jsonData.paymentType = tostring(data.paymentType)
                    jsonData.currencyType = tostring(data.currencyType) or "CNY"
                    jsonData.currencyAmount = tostring(tonumber(data.currencyAmount)/100)
                    local jsonStr = json.encode(jsonData)
                    XH.sdkManager:callFunctionWithMap("trackingio_set_payment",{["0"]=jsonStr})
                end
                if XH.sdkManager:checkCanUseReYunIOS() then
                    local currencyType = "CNY"
                    if data.currencyType then
                        currencyType = tostring(data.currencyType)
                    end
                    local info = {
                        ["functionName"] = "trackingio_set_payment",
                        ["args"] =
                        {
                            ["transactionId"] = tostring(data.transactionId),
                            ["paymentType"] = tostring(data.paymentType),
                            ["currencyType"] = currencyType,
                            ["currencyAmount"] = tostring(tonumber(data.currencyAmount)/100),
                        }
                    }
                    XH.sdkManager:xhCallOCFunc("UserInterface", "callFunction", info)
                end
            end
        end
    end
end

function ShopModule:recordBuyParam(productInfo, scriptName, gifttype, autoOrder, bHideSuccessTip, propID, targetPropID)
    self._buyParam = { }
    self._buyParam.productInfo = productInfo
    self._buyParam.scriptName = scriptName
    self._buyParam.gifttype = gifttype
    self._buyParam.autoOrder = autoOrder
    self._buyParam.bHideSuccessTip = bHideSuccessTip
    self._buyParam.propID = propID
    self._buyParam.targetPropID = targetPropID
end

function ShopModule:getAndroidOrderId()
    local payType = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_PAY_TYPE)
    if device.platform == "android" and XH.SysTool:CompareToBundleVersion("1.2.7") then
        local mapData = {
            ["0"] = tostring(payType),                                    --支付方式 
        }
        XH.sdkManager:callFunctionWithMap(XH.ThirdDefine.SDK_INTERFACE_NAMES_KEY.GET_ORDERID,mapData)
    end
end

function ShopModule:onGetOrderIdSuccessCallBack(event)
    local code = event.data.code
    local msg = event.data.msg
    if code == XH.ThirdDefine.GET_ORDERID_CALLBACK_CODE.ORDERID_SUCCESS then  
        if msg ~= nil then 
            self:onPayTalkingDataSDK(msg)
        end
    end
    self._buyParam = { }
end

function ShopModule:onPayTalkingDataSDK(orderId)
    if orderId == nil then 
        return 
    end
    local price
    if device.platform == "ios" then 
        price = math.modf(tonumber(self._buyParam.productInfo.price)) 
    elseif device.platform == "android" then 
        price = math.modf(tonumber(self._buyParam.productInfo.price)) * 100
    end 
    local mapData = {
        ["0"] = tostring(XH.playerData:getNumberID()),                                  --用户帐号
        ["1"] = tostring(orderId),                                                      --订单ID
        ["2"] = tostring(price),                                                        --货币单位为分
        ["3"] = "CNY" ,                                                                 --汇率默认为CNY
        ["4"] = "default",                                                              --支付类型
    }
    dump(mapData,"订单具体详情")
    if (device.platform == "android" and XH.SysTool:CompareToBundleVersion("1.2.7")) and XH.ChannelTool.checkIsSemChannel() then
        XH.sdkManager:callFunctionWithMap("talkingdata_onPay",mapData)
        if self._buyParam.productInfo.price then--android百度营销sdk上报购买金额
            -- local luaj = require("cocos.cocos2d.luaj")
            -- luaj.callStaticMethod("org/cocos2dx/lua/AppActivity", "baiduActionPurchase",tonumber(self._buyParam.productInfo.price))
            XH.lobby:getModule("BaiduOcpc"):reprotUserData("PURCHASE",{price = self._buyParam.productInfo.price})
        end
    end
    if (device.platform == "ios" and XH.SysTool:CompareToBundleVersion("1.3.13")) then
        XH.sdkManager:xhCallOCFunc("","talkingdata_onPay",mapData)
        if self._buyParam.productInfo.price then--ios百度营销sdk上报购买金额
            local tmpPrice = {
                ["BaiduSDKActionParamKeyPurchaseMoney"] = tonumber(self._buyParam.productInfo.price),
            }
            XH.sdkManager:xhCallOCFunc("", "baidu_Purchase", tmpPrice)
        end
    end
end

return ShopModule k 