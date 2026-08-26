local SxvipModule = class("SxvipModule", XH.ModuleBase)

local SxvipConfig = require("lobby.Modules.Sxvip.Config")
local ShopConfig = require("lobby.Modules.Shop.Config")
local TaskDefine = require("app.Define.TaskDefine")
local ConfigurationDefine = require("lobby.Modules.Configuration.Define")

SxvipModule.SXVIP_EVENT_REMAINDER = "SXVIP_EVENT_REMAINDER"
SxvipModule.SXVIP_EVENT_CONFIG_SUCC = "SXVIP_EVENT_CONFIG_SUCC"
SxvipModule.SXVIP_EVENT_CONFIG_FAIL = "SXVIP_EVENT_CONFIG_FAIL"
SxvipModule.SXVIP_EVENT_STATIC_CONFIG_SUCC = "SXVIP_EVENT_STATIC_CONFIG_SUCC"
SxvipModule.SXVIP_EVENT_FRIEND_INFO_UPDATE = "SXVIP_EVENT_FRIEND_INFO_UPDATE"
SxvipModule.SXVIP_EVENT_STATISTICS_UPDATE_ALL = "SXVIP_EVENT_STATISTICS_UPDATE_ALL"
SxvipModule.SXVIP_EVENT_STATISTICS_UPDATE_SUB = "SXVIP_EVENT_STATISTICS_UPDATE_SUB"
SxvipModule.EVENT_UPDATE_VIPSHOP_LIST = "EVENT_UPDATE_VIPSHOP_LIST"
SxvipModule.SXVIP_EVENT_REWARD_STATUS = "SXVIP_EVENT_REWARD_STATUS"
-- 以下是会员赠送
SxvipModule.SXVIP_EVENT_FRIENDLIST_CHANGED = "SXVIP_EVENT_FRIENDLIST_CHANGED"
SxvipModule.SXVIP_EVENT_GET_OTHER_USERINFO = "SXVIP_EVENT_GET_OTHER_USERINFO"
SxvipModule.SXVIP_EVENT_SEND_RECORD = "SXVIP_EVENT_SEND_RECORD"
SxvipModule.SXVIP_EVENT_MESSAGE_COUNT = "SXVIP_EVENT_MESSAGE_COUNT"     -- 待处理消息条数

function SxvipModule:ctor()
    SxvipModule.super.ctor(self)

    self._sendTime = 0      --可赠送时间
    self._remainder = 0     --会员结束时间戳
    self._config = {}       --会员配置(服务数据)
    self._resConfig = {}    --会员静态资源
    self._product = nil     --当前购买的商品信息
    self._systemTime = os.time() -- 服务器时间，默认为本地时间
    self.showToastTips = false --购买或者续费成功提示
    self._isOpen = false      --是否开启会员系统
    self._sxVipShopList = {}
    self._isRequestShopList = false
    self._hadAward = true    --是否有奖励，默认是true
    self._touchIconStatus = false  --是否已经点击过icon

    -- 打开会员页面需要保留数据，关闭之后清除
    self._statisticsData = {}
    self._friendInfoData = {}

    -- 会员赠送增加
    self._friendListInfo = {}
    self._messageCount = 0
    self._maxFriendPage = 1
    self:onGetConfiguration({data = {key = ConfigurationDefine.Config.ALL}}, true)
end

function SxvipModule:getReqConfig()
    return {
        ReqRemainder = { reqPath = "lobby.Req.Sxvip.ReqSxvipRemainder", callBack = self.respRemainder },
        ReqSxvipConfig = { reqPath = "lobby.Req.Sxvip.ReqSxvipConfig", callBack = self.respConfig },
        ReqSxvipPrivilege = { reqPath = "lobby.Req.Sxvip.ReqSxvipPrivilege", callBack = self.respPrivilege },
        ReqExchangeProduct = { reqPath = "lobby.Req.Shop.ReqExchange", callBack = self.respExchangeProduct },
        RequestSysTime = { reqPath = "app.Req.Task.ReqTaskPercent", callBack = self.onReqTaskPercentCallBack },
        ReqFriendInfo = { reqPath = "lobby.Req.Sxvip.ReqSxvipFriendInfo", callBack = self.respFriendInfo },
        ReqGameplayStatistics = { reqPath = "lobby.Req.Sxvip.ReqSxvipGameplayStatistics" , callBack = self.respSxvipGameplayStatistics } ,
        ReqGameplayStatisticsSub = { reqPath = "lobby.Req.Sxvip.ReqSxvipGameplayStatistics" , callBack = self.respSxvipGameplayStatisticsSub } ,
        ReqDailyReward = { reqPath = "lobby.Req.Sxvip.ReqSxvipDailyReward" , callBack = self.respDailyReward },
        ReqDailyRewardStatus =  { reqPath = "lobby.Req.Sxvip.ReqSxvipDailyReward" , callBack = self.respDailyRewardStatus },
        ReqExchangeProductList = { reqPath = "lobby.Req.Shop.ReqExchangeProductList", callBack = self.respProductsInfo },
        ReqDiamondProductsInfo = { reqPath = "lobby.Req.Shop.ReqProductList", callBack = self.respDiamondProductsInfo },
        -- 以下是会员赠送相关接口
        ReqFriendList = { reqPath = "lobby.Req.Im.ReqFriendList", callBack = self.respFriendListCallBack },
        ReqOtherUserInfo = { reqPath = "teahouse.Req.NetReqOtherUserInfo", callBack = self.respOtherUserInfo },
        ReqSxvipFellowSend = { reqPath = "lobby.Req.Sxvip.ReqSxvipFellowSend", callBack = self.respSxvipFellowSend },
        -- 会员订阅接口（先订阅后付费）
        ReqSxvipSubscribe = { reqPath = "lobby.Req.Sxvip.ReqSxvipSubscribe", callBack = self.respSxvipSubscribe },
    }
end

function SxvipModule:getProxyEvents()
    return {
        { module = XH.sdkManager, eventKeyName = "EVENT_IAP_CALLBACK", callBack = "respBuyProp" },
        { module = XH.lobby:getModule("Configuration"), eventKeyName = "EVENT_REQ_CONFIGURATION_SUCCESS", callBack = "onGetConfiguration" },
        -- { module = XH.login:getModule("Login"), eventKeyName = "LOGIN_EVENT_SUCCESS", callBack = "onLoginSuccessCallBack" },
        -- { module = XH.netEngine, eventName = XH.MatchLinkProtocol.RespNotifyTaskPercent.event_key, callBack = "onRespNotifyTaskPercent" },
        -- { module = XH.sdkManager, eventKeyName = "EVENT_GETORDER_CALLBACK", callBack = "onGetOrderIdSuccessCallBack"}
    }
end

-- 检测权限是否存在
function SxvipModule:getPowerExit(power, gameId)
    if power == SxvipConfig.SXVIP_POWER.WATCH and gameId ~= nil and gameId ~= 0 then
        local allCfg = XH.lobby:getModule("Configuration"):getConfigJsonData(ConfigurationDefine.Config.ALL, "AllConfig")
        if allCfg and allCfg.notSupportWatchGameId and gameId then
            for k, v in pairs(allCfg.notSupportWatchGameId) do
                if v == gameId then
                    return false
                end
            end
        end
    end
    return power and table.indexof(self:getPrivilegeList() , power)
end

function SxvipModule:reqExchangeProductsInfo(propType)
    if self._isRequestShopList then
        return
    end
    self._isRequestShopList = true
    XH.TipTool.showLoading()
    local numId = XH.playerData:getNumberID()
    local userId = un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID())
    local areaId = XH.playerData:getBrandID()
    self:startReq("ReqExchangeProductList", numId, userId, areaId, XH.areaData:getAreaWindID(), 0, XH.KW_CONFIG_LOGIC_TIME_OUT, "", propType)
end

function SxvipModule:respProductsInfo(req, type, data)
    self._isRequestShopList = false
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        local repData = {
            goods_list = data
        }
        local goodsList = repData.goods_list or {}
        -- cps渠道去除订阅商品
        if  XH.ChannelTool:checkIsCPSChannel() then
            for i = #goodsList, 1, -1 do
                if goodsList[i].tags and goodsList[i].tags[1] and goodsList[i].tags[1].key and goodsList[i].tags[1].key == "Subscription" then
                    table.remove(goodsList , i)
                end
            end
        end
        for _, goods in ipairs(goodsList) do
            goods.product_id = goods.mark
            goods.prop_type = goods.prop_type or SxvipConfig:getSxvipShopId()
        end
        -- 排序
        -- table.sort(goodsList , function(a , b)
        -- end)
        repData.goods_list = goodsList
        self._sxVipShopList = repData
        self:reqDiamondProductsInfo()
    end
end

function SxvipModule:getVipShopList()
    return self._sxVipShopList or {}
end

function SxvipModule:reqConfig()
    self:startReq("ReqSxvipConfig") 
end

function SxvipModule:respConfig(req, type, data)
    if type ==  XH.Req.TYPE.SUCCESS then
       self._isOpen = true
       self._config = data and data.fellowConf or {}
       self:dispatchEvent({name = self.SXVIP_EVENT_CONFIG_SUCC, data = data and data.fellowConf or {}})
    else
        self._isOpen = false
        self._config = {}
        self:dispatchEvent({name = self.SXVIP_EVENT_CONFIG_FAIL})
    end
end

function SxvipModule:getSxvipOpen()
    return self._isOpen
end

function SxvipModule:reqRemainder()
    self:startReq("ReqRemainder") 
end

function SxvipModule:respRemainder(req, type, data)
    self._sendTime = 0
    if type ==  XH.Req.TYPE.SUCCESS then
        for _, prop in pairs(data.props or {}) do
            if tonumber(prop.propId) == tonumber(SxvipConfig.PROP_ID) then
                self._remainder = prop.time
                if not self:isExpire() and self.showToastTips then
                    XH.TipTool.showToast("您的会员权益已生效。")
                    self.showToastTips = false
                end
                self:RequestSysTime()
                XH.playerData:setSxvipRemainder(prop.time or 0)
                self:setNVipSendTime(prop.time or 0, true)
            elseif tonumber(prop.propId) == tonumber(SxvipConfig.PROP_ID_SJ) then
                self:setNVipSendTime(prop.time or 0, false)
            end
        end
        self:dispatchEvent({name = self.SXVIP_EVENT_REMAINDER,  data = data})
    end
end

function SxvipModule:setNVipSendTime(time, isAdd)
    local serverTime = self:getSysTime()
    time = time - serverTime
    if time < 0 then
        return
    end
    if not isAdd then
        time = time * -1
    end
    self._sendTime = self._sendTime + time
end

-- 获取可赠送时间（150725-150735）
function SxvipModule:getNVipSendTime()
    if self._sendTime < 0 then
        return 0
    end
    return self._sendTime
end

-- 是否过期
function SxvipModule:isExpire()
    return self._systemTime > self._remainder
end

-- 获取特权数量
function SxvipModule:getPrivilegeNum()
    if self._config and self._config.list then
        local num = 0
        for _, v in ipairs(self._config.list) do
            if #v.benefit > num then
                num = #v.benefit
            end
        end
        return num
    end
    return 0
end

-- 获取特权列表
function SxvipModule:getPrivilegeList()
    if self._config and self._config.list then
        local privilegeList = {}
        for _, v in ipairs(self._config.list) do
            if #v.benefit > #privilegeList then
                privilegeList = v.benefit
            end
        end
        return privilegeList
    end
    return {}
end

-- 获取每日奖励列表
function SxvipModule:getPeriodsList()
    if self._config and self._config.list then
        local privilegeList = {}
        for _, v in ipairs(self._config.list) do
            if #v.benefit > #privilegeList then
                privilegeList = v.periods
            end
        end
        return privilegeList
    end
    return {}
end

-- 远端请求配置
function SxvipModule:reqPrivilege()
    self:startReq("ReqSxvipPrivilege")
end

-- 远端获取静态配置成功
function SxvipModule:respPrivilege(req, type, data)
    if type ==  XH.Req.TYPE.SUCCESS then
        self._resConfig = data
        self:dispatchEvent({name = self.SXVIP_EVENT_STATIC_CONFIG_SUCC, data = data})
    end
end

-- 获取特权所有配置
function SxvipModule:getStaticConfig()
    return self._resConfig
end

-- 通过特权id获取特权
function SxvipModule:getPrivilegeStaticById(id)
    for i, v in ipairs(self._resConfig) do
        if v.id == id then
            return v
        end
    end
end

-- 开始兑换会员会员
function SxvipModule:respBuyProp(event)
    -- 场景在新金币场
    if XH.SceneManager:getInstance():getTopSceneTag() == 1001 or XH.SceneManager:getInstance():getTopSceneTag() == 1002 then
        return
    end
    local code = event.data.code
    local msg = event.data.msg
    XH.TipTool.hideLoading()
    if code == 200 then
        -- XH.SysTool.performWithDelayGlobal( function()
        --     local productId = self._product.id
        --     local userId = un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID())
        --     local gameId = XH.areaData:getConfigGameID()
        --     local productType = nil
        --     local propType = self._product.prop_type
        --     local propValue = nil 
        --     local multiCfgId = nil
        --     local gift_cfg = nil
        --     self:startReq("ReqExchangeProduct",XH.playerData:getBrandID(), XH.areaData:getAreaWindID(), XH.playerData:getNumberID(), userId, gameId, productId, productType, XH.KW_CONFIG_LOGIC_TIME_OUT, propType, nil, nil, propValue, multiCfgId, gift_cfg )
        --     self._product = nil
        -- end, 0.1)
        -- 2秒后请求下更新
        if self._product then
            XH.SysTool.performWithDelayGlobal( function()
                self:reqConfig()
                self:reqRemainder()
                self.showToastTips = true
            end, 2)
        end
    elseif code == 208 or code == 209 then
        self:reqBuy(self._product , 1)
    else
        self._product = nil
    end
end

function SxvipModule:respExchangeProduct(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self.showToastTips = true
        self:reqConfig()
        self:reqRemainder()
    else
        XH.TipTool.showToast(data.message or "兑换失败")
    end
end

-- 开始支付
function SxvipModule:reqBuy(productParam , autoOrder, notShowTip)
    if not productParam then
        return
    end
    local isSubscription = true
    if productParam.tags and productParam.tags[1] and productParam.tags[1].key == "Non-subscription" then
        isSubscription = false
    end
     -- 发起支付购买
    local product = {
        productId = productParam.product_id ,
        name = productParam.name  ,
        price = productParam.real_price ,
        isSubscription = isSubscription ,
        module = "Sxvip"
    }
    local isSubscribe = isSubscription
    local subscribePeriodType = isSubscription and "DAY" or nil 
    local subscribePeriod = isSubscription and productParam.prop_count or nil 
    local subscribePrice = isSubscription and productParam.real_price * 100 or nil  -- 后续自动代扣价格，价格：分
    if notShowTip and device.platform ~= "android" then
        self._product = productParam
        self:doPay(product, autoOrder, productParam.id, productParam.prop_type, isSubscribe, subscribePeriodType, subscribePeriod,  subscribePrice)
        return
    end
    local tips = string.format("是否花费%s元购买%s？购买后自动兑换" , product.price , isSubscription and "续订" .. product.name  or product.name)
    XH.TipTool.showTip({
        type = XH.TIP_LAYER_TYPE.OK,
        funcOK = function()
            if not isSubscription then --非续订商品才需要记录购买，自己进行兑换
                self._product = productParam
            end
            self:doPay(product, autoOrder, productParam.id, productParam.prop_type, isSubscribe, subscribePeriodType, subscribePeriod,  subscribePrice)
        end
    }, tips)
end

function SxvipModule:doPay(productInfo, autoOrder, itemId , propType  , isSubscribe , subscribePeriodType , subscribePeriod , subscribePrice)
    XH.TipTool.showLoading()
    XH.lobby:getModule("Shop"):recordBuyParam(productInfo, "deliver_award", "", autoOrder, false , XH.areaData:getPropDiamndID())
    local gameId = XH.areaData:getConfigGameID()
    local channelID = XH.SysTool:getChannelID()
    local numId = XH.playerData:getNumberID()
    local userId = un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID())
    local areaId = tostring(XH.playerData:getBrandID())
    local propid = XH.areaData:getPropDiamndID()
    local groupid = XH.areaData:getSrsGroupID()
    -- 传了这个表示走新的发货流程
    local defaultScriptName =  "deliver_award"
    local extTable = {}
    if isSubscribe then
        extTable = {
            gameid = gameId,
            areaid = areaId,
            propid = propid,
            channelid = channelID,
            numid = numId,
            source = areaId .. XH.areaData:getLobbyID(),
            script_name = defaultScriptName,
            order_id = "",
            groupid = groupid,
            gifttype = "",
            item_id = itemId , 
            prop_type = propType ,
            vip_sub = "1" ,
            sid = XH.areaData:getAreaWindID(),
            is_subscribe = isSubscribe  ,
            subscribe_period_type = subscribePeriodType  ,
            subscribe_period = subscribePeriod  ,
            subscribe_price = subscribePrice ,
        }
    else
        extTable = {
            gameid = gameId,
            areaid = areaId,
            propid = propid,
            channelid = channelID,
            numid = numId,
            source = areaId .. XH.areaData:getLobbyID(),
            script_name = defaultScriptName,
            order_id = "",
            groupid = groupid,
            gifttype = "",
            item_id = itemId , 
            prop_type = propType ,
            vip_sub = "1" ,
            sid = XH.areaData:getAreaWindID(),
        }
    end
    local ext = json.encode(extTable)

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

    local target = cc.Application:getInstance():getTargetPlatform()
    if target == cc.PLATFORM_OS_IPHONE or target == cc.PLATFORM_OS_IPAD then
        XH.sdkManager:xhCallOCFunc("XH_IAP", "xhBuy:", proForpey)
    elseif device.platform == "android" then
        if XH.ChannelTool.checkIsECarChannel() then --车载渠道，传H5道具参数 暂未处理
            XH.TipTool.hideLoading()
            -- XH.viewManager:openView("PayTypeView", nil, proForH5)
        elseif XH.SysTool.CompareToBundleVersion and XH.SysTool:CompareToBundleVersion("1.2.1") then 
            if isSubscribe then
                -- 调用服务接口处理先订阅后付费功能
                -- 客户端SDK版本较低，升级SDK的话，Android更包率较低，达不到业务目标，使用H5支付方式，对SDK版本无要求
                -- 请求50中转服务，50服务对接Hummer服务下单，返回给客户端一个url，客户端打开url唤起ZFB进行签约并支付
                self:startReq("ReqSxvipSubscribe", proForpey)
                -- XH.sdkManager:callFunctionWithMap("alp_open_iap" , { ["0"] = proForpey })
            else
                XH.TipTool.hideLoading()
                XH.viewManager:openView("PayTypeView", nil, proForpey)
            end
        else
            XH.sdkManager:callFunctionWithMap(XH.ThirdDefine.SDK_INTERFACE_NAMES_KEY.BUY, { ["0"] = proForpey })
        end
    else
        XH.TipTool.hideLoading()
    end
end

function SxvipModule:respSxvipSubscribe(req, type, data)
    XH.TipTool.hideLoading()
    if device.platform == "ios" then
        return
    end
    local errMsg = "请求购买失败"
    if data and data.hummerErr and data.hummerErr ~= "" then
        errMsg = data.hummerErr
    end
    if type == XH.Req.TYPE.SUCCESS then
        if data.schemaUrl and data.schemaUrl ~= "" then
            local ret = cc.Application:getInstance():openURL(data.schemaUrl)
            if not ret then
                XH.TipTool.showToast("该设备没有安装支付应用，请安装后重试")
            end
        else
            XH.TipTool.showToast(errMsg)
        end
    else
        XH.TipTool.showToast(errMsg)
    end
end

function SxvipModule:reqFriendInfo()
    XH.TipTool.showLoading()
    self:startReq("ReqFriendInfo")
end

function SxvipModule:respFriendInfo(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
       local info = data.drawReoprt or {}
       self:setFriendInfoData(info)
       self:dispatchEvent({name = self.SXVIP_EVENT_FRIEND_INFO_UPDATE, data = info})
    else
        XH.TipTool.showToast(data and data.message or "请求牌友信息失败")
    end
end

function SxvipModule:reqGameplayStatisticsAll()
    XH.TipTool.showLoading()
    self:startReq("ReqGameplayStatistics", 0)
end

function SxvipModule:respSxvipGameplayStatistics(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        local info = self:arrangeStatisticsData(data)
        self:setStatisticsDataSub(req._gameid, info)
        self:dispatchEvent({name = self.SXVIP_EVENT_STATISTICS_UPDATE_ALL, data = info})
     else
         XH.TipTool.showToast(data and data.message or "请求牌局数据失败")
     end
end

function SxvipModule:reqGameplayStatisticsSub(gameid)
    XH.TipTool.showLoading()
    self:startReq("ReqGameplayStatisticsSub", gameid)
end

function SxvipModule:respSxvipGameplayStatisticsSub(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        local info = self:arrangeStatisticsData(data)
        self:setStatisticsDataSub(req._gameid, info)
        self:dispatchEvent({name = self.SXVIP_EVENT_STATISTICS_UPDATE_SUB, data = info})
     else
         XH.TipTool.showToast(data and data.message or "请求牌局数据失败")
     end
end

-- 整理数据
function SxvipModule:arrangeStatisticsData(data)
    local gameplayStatistics = {
        lostUser = data.lostUser or "" ,
        totalWin = data.totalWin or 0 ,
        totalPlay = data.totalPlay or 0 ,
        winUser  = data.winUser or "",
        gameIds = data.gameIds or {} ,
        mainTime = "" ,
        dayReport = {},
        weekReport = {},
        winRate = ""
    }
    gameplayStatistics.winRate = string.format("%.2f%%",  math.floor(data.totalPlay == 0 and 0 or (data.totalWin / data.totalPlay * 100)))
    -- 天数据整理
    for i, v in ipairs(data.dayReport) do
        local date = v.startTime or "19970101"
        local month = string.sub(date, 5, 6)
        local day = string.sub(date, 7)
        -- 非会员隐藏
        if self:isExpire() then
            v.totalCount = 0
            v.winCount = 0
        end
        local winRate = string.format("%.0f%%", v.totalCount == 0 and 0 or (v.winCount / v.totalCount * 100))
        local dayData = {
            totalCount = v.totalCount or 0 ,
            winCount = v.winCount or 0,
            winRate = winRate ,
            date = month .. "-" .. day
        }
        table.insert(gameplayStatistics.dayReport , i ,dayData)
    end
    -- 期间
    local originalDate = gameplayStatistics.dayReport[#gameplayStatistics.dayReport].date .. "-" .. gameplayStatistics.dayReport[1].date
    local parts = string.split(originalDate, "-")
    local transformedDate = parts[1].."月"..parts[2].."日-"..parts[3].."月"..parts[4].."日"
    gameplayStatistics.mainTime = transformedDate
    -- 周数据整理
    for i, v in ipairs(data.weekReport) do
        -- 非会员隐藏
        if self:isExpire() then
            v.totalCount = 0
            v.winCount = 0
        end
        local winRate = string.format("%.0f%%", v.totalCount == 0 and 0 or (v.winCount / v.totalCount * 100))
        local weekData = {
            totalCount = v.totalCount or 0 ,
            winCount = v.winCount or 0 ,
            weekIndex = i , 
            winRate = winRate ,
        }
        table.insert(gameplayStatistics.weekReport , i ,weekData)
    end
    return gameplayStatistics
end

-- 请求每日奖励
function SxvipModule:reqDailyReward()
    XH.TipTool.showLoading()
    self:startReq("ReqDailyReward")
end

-- 每日奖励回调
function SxvipModule:respDailyReward(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        if data.awardInfo and data.awardInfo[1] then
            local propId = tonumber(data.awardInfo[1].propId )
            local propCnt = data.awardInfo[1].propCnt
            local propInfo = XH.lobby:getModule("BagSysNew"):getPropInfo(propId) or {}
            local msg = string.format("已成功发放%s个【%s】，可在游戏中使用，祝您游戏愉快。" , propCnt , propInfo.propsname or "")
            XH.TipTool.showToast(msg , 3)
            self._hadAward = false
            self:dispatchEvent({name = self.SXVIP_EVENT_REWARD_STATUS})
        end
    else
        XH.TipTool.showToast(req:getMessage())
    end
end

-- 请求每日奖励的状态
function SxvipModule:reqDailyRewardStatus()
    XH.TipTool.showLoading()
    self:startReq("ReqDailyRewardStatus", 10 , 1)
end

-- 每日奖励状态回调
function SxvipModule:respDailyRewardStatus(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        if data.HadAward ~= nil then
            self._hadAward = data.HadAward
        end
    end
end

-- 获取是否有奖励可领取
function SxvipModule:getHadAwardStatus()
    return self._hadAward
end

-- 服务器时间同步
function SxvipModule:RequestSysTime()
    self:startReq("RequestSysTime", XH.playerData:getNumberID(), 0, 120, TaskDefine.KW_SYSTEM_TIME, XH.areaData:getSrsGroupID(), 10)
end

function SxvipModule:onReqTaskPercentCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        self._systemTime = json.decode(data.acData).time or os.time()
        self:startSchedule()
    end
end

function SxvipModule:refreshTime()
    self._systemTime = self._systemTime + 1
end

function SxvipModule:getSysTime()
    return self._systemTime or os.time()
end

function SxvipModule:startSchedule()
    if self._refreshTime ~= nil then
        return
    end
    if self._refreshTime then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._refreshTime)
    end
    self._refreshTime = nil
    self:refreshTime()
    self._refreshTime = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        self:refreshTime()
    end, 1, false)
end

function SxvipModule:onDestroy()
    SxvipModule.super.onDestroy(self)
    self:removeTimeScheduler()
end

function SxvipModule:removeTimeScheduler()
    if self._refreshTime then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._refreshTime)
    end
end

-- 请求钻石商品信息
function SxvipModule:reqDiamondProductsInfo()
    XH.TipTool.showLoading()
    self:startReq("ReqDiamondProductsInfo", XH.configerManager:getShopAppId(), XH.configerManager:getShopPackageAppId())
end

-- 钻石信息结果
function SxvipModule:respDiamondProductsInfo(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        if data and table.nums(data) > 0 and self._sxVipShopList and  self._sxVipShopList.goods_list and table.nums(self._sxVipShopList.goods_list) > 0 then
            local products = {}
            for _, v in ipairs(data) do
                products[v.productId] = v
            end
            for _, v in ipairs(self._sxVipShopList.goods_list) do
                v.real_price = products[v.product_id] and tonumber(products[v.product_id].price or 0) or 0
            end
            self:dispatchEvent({name = self.EVENT_UPDATE_VIPSHOP_LIST, data = data})
        end
    end
end

function SxvipModule:getTouchIconStatus()
    return self._touchIconStatus
end

function SxvipModule:setTouchIconStatus(status)
    self._touchIconStatus = status
end

function SxvipModule:getStatisticsData()
    return self._statisticsData
end

function SxvipModule:setStatisticsDataSub(gameid , statisticsData)
    self._statisticsData[gameid] = statisticsData
end

function SxvipModule:getStatisticsDataByGameid(gameid)
    return self._statisticsData[gameid or 0]
end

function SxvipModule:clearStatisticsData()
    self._statisticsData = {}
end

function SxvipModule:getFriendInfoData()
    return self._friendInfoData
end

function SxvipModule:setFriendInfoData(friendInfoData)
    self._friendInfoData = friendInfoData
end

function SxvipModule:clearFriendInfoData()
    self._friendInfoData = {}
end

function SxvipModule:getFriendMaxPage()
    return self._maxFriendPage
end

-- 配置中心请求返回后再请求VIP相关的信息
function SxvipModule:onGetConfiguration(event)
    if self._isInit and self._isInit == true then
        return
    end
    local key = event.data.key
    if key and key == ConfigurationDefine.Config.ALL and SxvipConfig:getSxvipShow() then
        self._isInit = true
        -- 请求会员剩余时间
        self:reqRemainder()
        -- 请求会员配置
        self:reqConfig()
        self:reqVipRedPointCount()
    end
end

-- 请求好有列表
function SxvipModule:reqFriendList(curPage)
    curPage = curPage or 0
    if curPage == 0 then
        self._friendListInfo = {}
    end
    if curPage >= self._maxFriendPage and curPage ~= 0 then
        return
    end
    self:startReq("ReqFriendList", XH.lobby:getModule("Im"):getIMData():getProposalAppid(), curPage, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function SxvipModule:respFriendListCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        if not data.friendInfo or type(data.friendInfo) ~= "table" then
            return
        end
        self._maxFriendPage = data.total_package
        for _, v in pairs(data.friendInfo) do
            local bFind = false
            for i = 1, #self._friendListInfo do
                if v.numid == self._friendListInfo[i].numid then
                    self._friendListInfo[i] = clone(v)
                    bFind = true
                    break
                end
            end
            if not bFind then
                table.insert(self._friendListInfo, v)
            end
        end
        self:dispatchEvent({name = self.SXVIP_EVENT_FRIENDLIST_CHANGED})
    end
end

function SxvipModule:getFriendListInfo()
    return self._friendListInfo or {}
end

-- 请求要赠送玩家信息
function SxvipModule:reqOtherUserInfo(uid)
    XH.TipTool.showLoading()
    self:startReq("ReqOtherUserInfo", tonumber(uid))
end

function SxvipModule:respOtherUserInfo(cell, ctype, data)
    XH.TipTool.hideLoading()
    if ctype == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({ name = self.SXVIP_EVENT_GET_OTHER_USERINFO, data = data })
    end
end

function SxvipModule:reqVipRedPointCount()
    -- 请求日志信息
    local param = {}
    local currentTime = self:getSysTime() -- 获取当前时间戳
    local dayStart = os.date("*t", currentTime) -- 获取当天开始时间
    dayStart.hour = 0
    dayStart.min = 0
    dayStart.sec = 0
    local startTimestamp = os.time(dayStart) - 86400*6 -- 计算开始时间戳
    local endTimestamp = os.time(dayStart) + 86399 -- 结束时间戳为当天23:59:59

    param.opt = SxvipConfig.OPT.log_wait
    param.log_select = SxvipConfig.LOGSELECT.Handler
    param.page = 1
    param.page_size = 5
    param.start_time = startTimestamp
    param.end_time = endTimestamp
    param.isReqRedPoint = true
    self:reqFellowSend(param)
end

function SxvipModule:reqFellowSend(param)
    if not param.isReqRedPoint then
        XH.TipTool.showLoading()
    end
    self:startReq("ReqSxvipFellowSend", param) 
end

function SxvipModule:respSxvipFellowSend(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        if data.isReqRedPoint then
            self._messageCount = data.total
            self:dispatchEvent({ name = self.SXVIP_EVENT_MESSAGE_COUNT })
        end
        if data.opt == SxvipConfig.OPT.Get then
            XH.TipTool.showToast("您已向【" .. XH.StringTool.cutStringByLength(clone(data.selectFriendInfo.nickname), 10) .. "】发起申请，等待对方操作")
        elseif data.opt == SxvipConfig.OPT.Send or data.opt == SxvipConfig.OPT.Agree then
            XH.TipTool.showToast("赠送成功!", 3)
            self:reqRemainder()
        end
        self:dispatchEvent({ name = self.SXVIP_EVENT_SEND_RECORD, data = data })
    elseif type == XH.Req.TYPE.FAIL then
        if not data or not data.resp or not data.resp.errCode then
            XH.TipTool.showTip( { type = XH.TIP_LAYER_TYPE.OK} , req:getMessage())
            return
        end
        local tipStr = ""
        if data.resp.errCode == SxvipConfig.SEND_ERRORCODE.ERROR_NOT_ENOUGH then
            XH.TipTool.showTip( {
                type = XH.TIP_LAYER_TYPE.OK_CANCEL,
                funcOK = function()
                    XH.viewManager:closeView('SxvipGiveView')
                    XH.viewManager:closeView('SxvipRecordView')
                    XH.viewManager:closeView('SxvipView')
                    XH.viewManager:openView("SxvipView", nil, {defalutMenu = SxvipConfig.MENU_DEFINE.BUY_VIP})
                end,
            } , "会员天数不足，是否前往商城补充？")
            return
        end
        if data.resp.errCode == SxvipConfig.SEND_ERRORCODE.ERROR_GET_ONE then
            tipStr = "今日索要已达上限，请换个牌友！"
        elseif data.resp.errCode == SxvipConfig.SEND_ERRORCODE.ERROR_GET_ALL then
            tipStr = "今日索要已达上限，请明天再来！"
        elseif data.resp.errCode == SxvipConfig.SEND_ERRORCODE.ERROR_SEND_ONE then
            tipStr = "今日无法给该玩家赠送，请换个牌友！"
        elseif data.resp.errCode == SxvipConfig.SEND_ERRORCODE.ERROR_SEND_ALL then
            tipStr = "今日赠送已达上限，请明天再来！"
        else
            XH.TipTool.showToast("请求VIP互赠出错(错误码" .. data.resp.errCode .. ")")
        end
        if tipStr ~= '' then
            XH.TipTool.showTip( { type = XH.TIP_LAYER_TYPE.OK} , tipStr)
        end
    end
end

function SxvipModule:getMessageCount()
    return self._messageCount or 0
end

function SxvipModule:isVipEnoughToSend(sendDay)
    if self:isExpire() then
        return false
    end
    local days = math.floor((self._remainder - self._systemTime)/86400)
    if days < sendDay then
        return false
    end
    return true
end

return SxvipModule��  