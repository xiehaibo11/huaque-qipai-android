--新背包系统

local BagSysModule = class("BagSysModule", XH.ModuleBase)
local BagSysProtocol = require("app.Protocols.BagSysProtocol")
local BagSysData = require("lobby.Modules.BagSysNew.BagSysData")
local TaskDefine = require("app.Define.TaskDefine")
local areaConfig = require("app.Config.AreaConfig")
local BagConfig = require("lobby.Modules.BagSysNew.Config")

BagSysModule.EVENT_USE_PROPS = "EVENT_USE_PROPS" --使用道具
BagSysModule.EVENT_OPERATE_PROPS = "EVENT_OPERATE_PROPS" --操作道具
BagSysModule.EVENT_BAG_REFRESH_SYSTEM_TIME = "EVENT_BAG_REFRESH_SYSTEM_TIME"
BagSysModule.EVENT_SELF_BACK_PACK_DATA = "EVENT_SELF_BACK_PACK_DATA" --自己背包数据
BagSysModule.EVENT_PLAYER_BAG_DATA = "EVENT_PLAYER_BAG_DATA" --某个人背包数据
BagSysModule.EVENT_BAGMANAGER_CHANGE_HEAD_FRRAME = "EVENT_BAGMANAGER_CHANGE_HEAD_FRRAME"
BagSysModule.EVENT_COUPON_DATA_LIST = "EVENT_COUPON_DATA_LIST"
BagSysModule.KW_SHOW_BAG_BTN_TEXT = "KW_SHOW_BAG_BTN_TEXT"
BagSysModule.EVENT_UPDATE_DOUBLE_CARD = "EVENT_UPDATE_DOUBLE_CARD"

local BASIC_RROPS_TYPE = 0 
local BOX_EMOJI_RROPS_TYPE = 1
local GOLD_EMOJI_RROPS_TYPE = 2 
local KEEP_ALIVE_TIME = 100 --心跳发送间隔时间

function BagSysModule:ctor()
    BagSysModule.super.ctor(self)
    self._couponList = {}
    self._doubleCards = {} -- 奖励加倍卡数据
    self:initData()
end

function BagSysModule:initData()
    if not self._bagSysData then
        self._bagSysData = BagSysData.new()
    end
end

function BagSysModule:getReqConfig()
    return {
        ReqSystemTimes = { reqPath = "app.Req.Task.ReqTaskPercent", callBack = self.onReqSystemTimesCallBack },
        ReqCouponList = { reqPath = "lobby.Req.BagSysNew.ReqCouponList", callBack = self.onReqCouponListCallback },    
        ReqDoubleCards = { reqPath = "lobby.Req.BagSysNew.ReqPropsCount50", callBack = self.respDoubleCards },    
    }
end

function BagSysModule:getBagData()
    return self._bagSysData
end

function BagSysModule:getProxyEvents()
    return {
        { module = XH.netEngine, eventName = BagSysProtocol.NotifyServerState.event_key, callBack = "onNotifyServerState" },
        -- { module = XH.netEngine, eventName = BagSysProtocol.RespBackpackData.event_key, callBack = "onNotifyPlayerBagData" },
        { module = XH.netEngine, eventName = BagSysProtocol.NotifyPropsAddMsg.event_key, callBack = "onNotifyPropsAddMsg" },
    }
end

function BagSysModule:onNotifyServerState(event)
    local resp =  BagSysProtocol.NotifyServerState:new()
    resp:bistream(event.msg.buff, event.msg.len)
    self:reqAppID()
end

function BagSysModule:reqAppID()
    if self._keepAliveScheduleID and self._keepAliveScheduleID ~= 0 then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._keepAliveScheduleID)  
        self._keepAliveScheduleID = 0
    end
    self._connectCnt = 0
    local ReqAppidList = require("lobby.Req.BagSysNew.ReqAppidList")
    local reqAppidList = ReqAppidList:new()
    reqAppidList:addReqCallBack(self, self.onReqBagAppidCallBack)
    reqAppidList:start(XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function BagSysModule:onReqBagAppidCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        if data.appid then
            self._bagSysData:setSvrAppidList(data.appid)
        end
        self:reqPlayerConnect()
    end
end

function BagSysModule:reqPlayerConnect()
    local appid = self._bagSysData:getSvrAppidList()
    if appid and next(appid) then
        local ReqPlayerConnect = require("lobby.Req.BagSysNew.ReqPlayerConnect")
        for i = 1, #appid do
            local reqPlayerConnect = ReqPlayerConnect:new()
            reqPlayerConnect:addReqCallBack(self, self.onReqBagPlayerConnectCallBack)
            reqPlayerConnect:start(appid[i], XH.KW_CONFIG_LOGIC_TIME_OUT)
        end
    end
end

function BagSysModule:onReqBagPlayerConnectCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        self._bagSysData:setProposalAppid(data.props_appid)
        self._connectCnt = self._connectCnt + 1
        if self._connectCnt == #(self._bagSysData:getSvrAppidList()) then
            self:reqJoinBagSys()
            self:reqKeepAlive()
        end
    elseif ctype == XH.Req.TYPE.TIMEOUT then
        self._connectCnt = 0
    elseif ctype == XH.Req.TYPE.FAIL then
        self._connectCnt = 0
    end
end

function BagSysModule:reqKeepAlive()
    self._keepAliveScheduleID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        local appid = self._bagSysData:getSvrAppidList()
        if appid and type(appid) == "table" and next(appid) then
            local ReqKeepAlive = require("lobby.Req.BagSysNew.ReqKeepAlive")
            for i = 1, #appid do
                local reqKeepAlive = ReqKeepAlive:new()
                reqKeepAlive:addReqCallBack(self, self.onReqBagKeepAliveCallBack)
                reqKeepAlive:start(appid[i], XH.KW_CONFIG_LOGIC_TIME_OUT)
            end
        end
    end, KEEP_ALIVE_TIME, false)
end

function BagSysModule:onReqBagKeepAliveCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.TIMEOUT then
        self:reqPlayerConnect()
    elseif ctype == XH.Req.TYPE.FAIL then
        self:reqPlayerConnect()
    end
end

function BagSysModule:reqJoinBagSys()
    local ReqJoinBag = require("lobby.Req.BagSysNew.ReqJoinBagSys")
    local reqJoinBag = ReqJoinBag:new()
    reqJoinBag:addReqCallBack(self, self.onReqJoinBagCallBack)
    reqJoinBag:start(self._bagSysData:getProposalAppid(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function BagSysModule:onReqJoinBagCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        if data.props_appid then
            self._bagSysData:setProposalAppid(data.props_appid)
        end
        self:reqPropsConfig(true,BASIC_RROPS_TYPE)
        XH.SysTool.performWithDelayGlobal( function()
            self:reqPropsConfig(true,BOX_EMOJI_RROPS_TYPE)
        end , 1)
        XH.SysTool.performWithDelayGlobal( function()
            self:reqPropsConfig(true,GOLD_EMOJI_RROPS_TYPE)
        end , 2)
        self:reqSelfBagkData()
    end
end

-- 请求配置
function BagSysModule:reqPropsConfig(bzip,propstype)
    local md5 = self._bagSysData:getPropsConfigMd5(propstype)
    local ReqPropsConfig = require("lobby.Req.BagSysNew.ReqPropsConfig")
    local reqPropsConfig = ReqPropsConfig:new()
    reqPropsConfig:addReqCallBack(self, self.onReqPropsConfigCallBack)
    reqPropsConfig:start(self._bagSysData:getProposalAppid(), md5, bzip,propstype, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function BagSysModule:onReqPropsConfigCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        if data then
            self._bagSysData:setPropsConfig(data)
        end
    end
end

--请求自己背包道具
function BagSysModule:reqSelfBagkData(isReqCard)
    self._isReqCard = isReqCard
    local pcnt = 1
    local areaid = {XH.areaData:getAreaID()}
    local numid = {XH.playerData:getNumberID()}
    local propsid = {}

    local ReqBackpackData = require("lobby.Req.BagSysNew.ReqBackpackData")
    local reqBackpackData = ReqBackpackData:new()
    reqBackpackData:addReqCallBack(self, self.onReqSelfBagDataCallBack)
    reqBackpackData:start(self._bagSysData:getProposalAppid(), pcnt, areaid, numid, propsid, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function BagSysModule:onReqSelfBagDataCallBack(cell, ctype, data)
    if self._isReqCard then
        XH.lobby:getModule("BagSysNew"):reqDoubleCards(true)
        self._isReqCard = nil
    end
    if ctype == XH.Req.TYPE.SUCCESS then
        data = data or {}
        --dump(data.player_props_info, "hcc>>onReqSelfBagDataCallBack",5)
        data.player_props_info = data.player_props_info or {}
        for _ , bagData in pairs(data.player_props_info) do
            if bagData.numid and bagData.numid == XH.playerData:getNumberID() then
                self._bagSysData:setSelfBagData(bagData)
                self:dispatchEvent({name = self.EVENT_SELF_BACK_PACK_DATA})
                local headUrlData, propid = self._bagSysData:getSelfHeadFrameUrl()
                local headFrameData = {url = headUrlData, propid = propid}
                if not headUrlData then
                    headFrameData = nil
                end
                self:dispatchEvent({name = self.EVENT_BAGMANAGER_CHANGE_HEAD_FRRAME, data = headFrameData})
            end
        end
        self:autoUseProp()
    end
end

--若已经有使用中的，把剩余个数道具使用掉
function BagSysModule:autoUseProp()
    local selfBagData = self._bagSysData:getSelfBagData()
    local devideTimeProp = self._bagSysData:devideSingleData(selfBagData.time_prop_info)  
    local devideCountProp = self._bagSysData:devideSingleData(selfBagData.count_prop_info)   
    if devideTimeProp and devideCountProp then
        for propid,_ in pairs(devideTimeProp) do
            local countProp = devideCountProp[propid]
            if countProp and not self:checkVIPProp(propid) then
                local ReqUseProps = require("lobby.Req.BagSysNew.ReqUseProps")
                local reqUseProps = ReqUseProps:new()
                reqUseProps:addReqCallBack(self,function(node, cell, ctype, data)
                    if ctype == XH.Req.TYPE.SUCCESS then
                        self:reqSelfBagkData()
                    end
                end)
                reqUseProps:start(self._bagSysData:getProposalAppid(), propid, countProp.propscnt, XH.KW_CONFIG_LOGIC_TIME_OUT)
            end
        end
    end
end

--购买完后直接使用
function BagSysModule:directUseProp(propsidUse,isNotShowTip)
    propsidUse = tonumber(propsidUse) or 0
    local pcnt = 1
    local areaid = {XH.areaData:getAreaID()}
    local numid = {XH.playerData:getNumberID()}
    local propsid = {}

    local ReqBackpackData = require("lobby.Req.BagSysNew.ReqBackpackData")
    local reqBackpackData = ReqBackpackData:new()
    reqBackpackData:addReqCallBack(self, function(node, cell, ctype, data)
        if ctype == XH.Req.TYPE.SUCCESS then
            data = data or {}
            data.player_props_info = data.player_props_info or {}
            for _ , bagData in pairs(data.player_props_info) do
                if bagData.numid and bagData.numid == XH.playerData:getNumberID() then
                    self._bagSysData:setSelfBagData(bagData)
                    local count_prop_info = bagData.count_prop_info or {}
                    local time_prop_info = bagData.time_prop_info or {}
                    local devideCountProp = self._bagSysData:devideSingleData(count_prop_info)
                    local devideTimeProp = self._bagSysData:devideSingleData(time_prop_info)
                    if devideCountProp and devideCountProp[propsidUse] then
                        self:reqUseProps(propsidUse, devideCountProp[propsidUse].propscnt,isNotShowTip)
                    end
                    if devideTimeProp and devideTimeProp[propsidUse] then
                        self:reqOperateProps(XH.BagSysProtocol.OPERATE_PROPS.OP_EQUIP, propsidUse,isNotShowTip)
                    end
                end
            end
        end
    end)
    reqBackpackData:start(self._bagSysData:getProposalAppid(), pcnt, areaid, numid, propsid, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

--[[
请求背包数据
pcnt:玩家数量
areaid: table
numid: table
propsid:  table, 表示取特定id的道具信息，若不填，表示玩家所有的道具信息都下发
]]
function BagSysModule:reqBagPackData(pcnt, areaid, numid, propsid)
    local ReqBackpackData = require("lobby.Req.BagSysNew.ReqBackpackData")
    local reqBackpackData = ReqBackpackData:new()
    reqBackpackData:addReqCallBack(self, self.onReqBagDataCallBack)
    reqBackpackData:start(self._bagSysData:getProposalAppid(), pcnt, areaid, numid, propsid, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function BagSysModule:onReqBagDataCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        data = data or {}
        data.player_props_info = data.player_props_info or {}
        for _ , bagData in pairs(data.player_props_info) do
            if bagData.numid then
                self._bagSysData:setPlayerBagData(bagData, bagData.numid)
                self:dispatchEvent({name = self.EVENT_PLAYER_BAG_DATA, data = {numid = bagData.numid, data = bagData}})
            end
        end
    end
end

--[[
function BagSysModule:onNotifyPlayerBagData(event)
    local respBackPackData =  XH.BagSysProtocol.RespBackpackData:new()
    respBackPackData:bistream(event.msg.buff, event.msg.len)
    local data = respBackPackData
    data.player_props_info = data.player_props_info or {}
    for _ , bagData in pairs(data.player_props_info) do
        if bagData.numid then
            self._bagSysData:setPlayerBagData(bagData, bagData.numid)
            self:dispatchEvent({name = self.EVENT_PLAYER_BAG_DATA, data = {numid = bagData.numid, data = bagData}})
        end
    end
end
]]

function BagSysModule:onNotifyPropsAddMsg(event)
    local notifyPropsAddMsg =  XH.BagSysProtocol.NotifyPropsAddMsg:new()
    notifyPropsAddMsg:bistream(event.msg.buff, event.msg.len)
    self:reqSelfBagkData()
    local lobbyid = XH.areaData:getLobbyID()
    local roomCardID = nil
    local diamondID = nil
    if areaConfig[lobbyid] then
        local propList = areaConfig[lobbyid].propList
        if propList then
            roomCardID = propList.roomCardID
            diamondID = propList.propDiamndID
        end
    end

    if not roomCardID or not diamondID then return end

    local ppi = notifyPropsAddMsg.ppi
    if ppi then
        local gold = notifyPropsAddMsg.ppi.gold
        if gold then
            XH.playerData:setSR(gold)
        end
        local count_prop_info = ppi.count_prop_info
        if count_prop_info then
            for _ , propinfo in ipairs(count_prop_info) do
                if propinfo.propsid == roomCardID and propinfo.propscnt then
                    XH.playerData:setRoomCard(propinfo.propscnt)
                elseif propinfo.propsid == diamondID and propinfo.propscnt then
                    XH.playerData:setDiamnd(propinfo.propscnt)
                end
            end
        end
    end
    XH.playerData:dispatchEvent({name = XH.playerData.EVENT_PLAYER_DROP_CHANGED})
end

--使用道具
function BagSysModule:reqUseProps(propsid, propscnt, isNotShowTip)
    local ReqUseProps = require("lobby.Req.BagSysNew.ReqUseProps")
    local reqUseProps = ReqUseProps:new()
    if isNotShowTip then 
        reqUseProps:addReqCallBack(self, self.onReqUsePropsWithOutTipCallBack)
    else
        reqUseProps:addReqCallBack(self, self.onReqUsePropsCallBack)
    end
    reqUseProps:start(self._bagSysData:getProposalAppid(), propsid, propscnt, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function BagSysModule:onReqUsePropsCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({name = self.EVENT_USE_PROPS, data = data})
        self:reqOperateProps(XH.BagSysProtocol.OPERATE_PROPS.OP_EQUIP, data.propsid)
    else
        data = data or {}
        local tipstr = XH.BagSysProtocol.ERRORSTR[data.errorcode] or "失败!"
        tipstr = tipstr .. ",code:" .. (data.errorcode or "")
        XH.TipTool.showTip( {type = XH.TIP_LAYER_TYPE.OK,} ,tipstr)
    end
end

function BagSysModule:onReqUsePropsWithOutTipCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({name = self.EVENT_USE_PROPS, data = data})
        self:reqOperateProps(XH.BagSysProtocol.OPERATE_PROPS.OP_EQUIP, data.propsid, true)
    else
        data = data or {}
        local tipstr = XH.BagSysProtocol.ERRORSTR[data.errorcode] or "失败!"
        tipstr = tipstr .. ",code:" .. (data.errorcode or "")
        XH.TipTool.showTip( {type = XH.TIP_LAYER_TYPE.OK,} ,tipstr)
    end
end

--操作道具
function BagSysModule:reqOperateProps(operate_props, propsid, isNotShowTip)
    local ReqOperateProps = require("lobby.Req.BagSysNew.ReqOperateProps")
    local reqOperateProps = ReqOperateProps:new()
    if isNotShowTip then
        reqOperateProps:addReqCallBack(self, self.onReqOperatePropsWithOutTipCallBack)
    else
        reqOperateProps:addReqCallBack(self, self.onReqOperatePropsCallBack)
    end
    reqOperateProps:start(self._bagSysData:getProposalAppid(), operate_props, propsid, XH.KW_CONFIG_LOGIC_TIME_OUT )
end

function BagSysModule:onReqOperatePropsCallBack(cell, ctype, data)
     if ctype == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({name = self.EVENT_OPERATE_PROPS, data = data})
        if cell._operate_props == XH.BagSysProtocol.OPERATE_PROPS.OP_EQUIP then
            XH.TipTool.showTip( {type = XH.TIP_LAYER_TYPE.OK,} , "使用成功！")
        elseif cell._operate_props == XH.BagSysProtocol.OPERATE_PROPS.OP_UNEQUIP then
            XH.TipTool.showTip( {type = XH.TIP_LAYER_TYPE.OK,} , "卸下成功！")
        else
            XH.TipTool.showTip( {type = XH.TIP_LAYER_TYPE.OK,} , "操作成功！")
        end
    else
        XH.TipTool.showTip( {type = XH.TIP_LAYER_TYPE.OK,} , "操作成功！")
    end
    self:reqSelfBagkData()
end

function BagSysModule:onReqOperatePropsWithOutTipCallBack(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
       self:dispatchEvent({name = self.EVENT_OPERATE_PROPS, data = data})
    end
    self:reqSelfBagkData()
end

function BagSysModule:getPlayerPosition()
    local position = XH.playerData:getPlayerPosition()
    return position
end

function BagSysModule:changeTime2Str(time)
    local str = os.date("%H:%M", time)
    return str
end

function BagSysModule:judgeInTeaHouse()
    return XH.teaHouseManager:isInTeaHouse()
end

--获取当前的默认游戏id
function BagSysModule:getAreaDefaultGameID()
    return XH.areaData:getConfigGameID()
end

--获取当前区的 唯一标识
function BagSysModule:getLobbyID()
    return XH.areaData:getLobbyID()
end

--获取srs组
function  BagSysModule:getSrsGroupID()
    return XH.areaData:getSrsGroupID()
end

--请求task时间
function BagSysModule:reqSystemTimesTask()
    self:startReq("ReqSystemTimes", XH.playerData:getNumberID(), 0, 120, TaskDefine.KW_SYSTEM_TIME, XH.areaData:getSrsGroupID(), 10)
    XH.TipTool.showLoading()
end

function BagSysModule:onReqSystemTimesCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local timestamp = json.decode(data.acData).time
        if timestamp ~= nil then
            self:dispatchEvent({name = self.EVENT_BAG_REFRESH_SYSTEM_TIME, data = timestamp})
        end
    end
    XH.TipTool.hideLoading()
end

function BagSysModule:reqCouponListInfo()
    self:startReq("ReqCouponList")
end

--判断是否是时效道具，参数：道具ID
function BagSysModule:isTimePropByPropID(propID)
    return self._bagSysData:isTimePropByPropID(propID)
end

--获得道具信息
function BagSysModule:getPropInfo(propID)
    return self._bagSysData:getPropInfo(propID)
end

--判断是否是时效道具，参数: 道具基础数据table
function BagSysModule:isTimeProp(propData)
    return self._bagSysData:isTimeProp(propData)
end

--是否是记牌器道具
function BagSysModule:isJPQProp(propID)
    return self._bagSysData:isJPQProp(propID)
end

--获取当前区域 背包道具列表配置，包括背包页签，shopID等所有配置
function BagSysModule:getBagListCfg()
    local Config = require("lobby.Modules.BagSysNew.Config")
    return Config.GameMenuListByConfID[XH.areaData:getLobbyID()] or {}
end

function BagSysModule:onReqCouponListCallback(cell, type, response)
    if type == XH.Req.TYPE.SUCCESS then
        self._couponList = response.data
        if not next(self._couponList) then
            return
        end
        self:dispatchEvent( { name = self.EVENT_COUPON_DATA_LIST})
    elseif type == XH.Req.TYPE.FAIL then
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, cell:getMessage())
    end    
end

function BagSysModule:dealCouponData()
    local temptable = self._couponList
    local CouponListData ={}
    if next(temptable) then
        for  _,v in pairs(temptable)  do
            local  tempData ={}
            local timeSub = self:dealCouponTime(v.expire_date + 86400)
            if timeSub ~= false then
                tempData.propscnt = 1
                tempData.propsid = v.id
                tempData.propsname = v.coupon_name
                tempData.props_overview = v.use_rule
                tempData.props_tag_id = v.props_tag_id
                tempData.props_img = v.coupon_img
                tempData.coupon_type = v.coupon_type
                tempData.addtime = v.start_date
                tempData.endtime = v.expire_date + 86400
                tempData.coupon_rule = v.coupon_rule
                tempData.effect_goods = v.effect_goods
                tempData.goods_order_no = v.goods_order_no
                tempData.script = timeSub
                table.insert(CouponListData,tempData)
            end
        end
        return CouponListData
    end
    return {}
end

function BagSysModule:dealCouponTime(endTime)
    local numDay1 = os.time()
    local numDay2 = endTime
    local timeSub = (numDay2 - numDay1)/(3600)
    if timeSub <= 0 then
        return false
    else
        return "addhour=".. timeSub
    end
end

function BagSysModule:devideCouponProp(coupon_goods)
    local ShopConfig = require("lobby.Modules.Shop.Config")
    for _,v in pairs(coupon_goods) do
        if v == "card" then
            return ShopConfig.SpecialTag.CARD
        elseif v == "diamond" then
            return ShopConfig.SpecialTag.DIAMOND
        elseif v == "gold" then
            return ShopConfig.SpecialTag.GOLD
        end
    end
end


--是否支持背包
function BagSysModule:isSupportBag()
    return self:getBagListCfg().BHasBag and self:getBagListCfg().BHasBag > 0
end

--获取页签列表配置
function BagSysModule:getBagList()
    if not self:isSupportBag() or not self:getBagListCfg().BagList then
        return {}
    end
    return self:getBagListCfg().BagList or {}
end

--获取兑换商城ID
function BagSysModule:getShopID()
    return self:getBagListCfg().ShopID or 0
end

--判断永久道具 只判断1名字带有永久2装扮类时效道具3装扮类数量道具
function BagSysModule:judgeForeverProp(propid)
    if propid then
        local devideBaseConfig = self:getBagData():getDevideBagConfigData()
        if devideBaseConfig == nil then
            return false
        end
        for _,propInfo in pairs(devideBaseConfig) do
            if propid == propInfo.propsid then
                if propInfo.propsname and string.find(propInfo.propsname, "永久") then
                    return true
                end
                if propInfo.descp and string.find(propInfo.descp, "永久") then
                    return true
                end
                if propInfo.category_one and string.find(propInfo.category_one, "装扮") and propInfo.propstype and  propInfo.propstype ~= 20 then
                    return true
                end
                local script = loadstring("return {" .. (propInfo.script or "") .. "}")()
                if propInfo.category_one and string.find(propInfo.category_one, "装扮") and script.addhour and script.addhour > 999 then
                    return true
                end
                break
            end
        end
    end
    return false
end

function BagSysModule:judgeCanUseProp(data)
    local popCnt = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_BAG_NEW_PROP_TIPS, 0)
    if popCnt == 0 then
        for _, info in pairs(data.count_prop_info) do
            if info.props_state == 0 and info.addtime > 0 then
                self:dispatchEvent( { name = self.KW_SHOW_BAG_BTN_TEXT, data = "您有道具可以使用" })
                XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_BAG_NEW_PROP_TIPS, popCnt + 1)
                break
            end
        end
    end
end

function BagSysModule:checkVIPProp(propID)
    if not propID then
        return false
    end
    local propList = XH.areaData:getPropList()
    local seniorVipID = propList.seniorVip
    local superVipID = propList.superVip
    if (seniorVipID and propID == seniorVipID) or (superVipID and propID == superVipID) then
        return true
    end
    return false
end

function BagSysModule:getProposalAppid()
    return self._bagSysData:getProposalAppid()
end 

function BagSysModule:reqDoubleCards(showLoading)
    if showLoading then
        XH.TipTool.showLoading()
    end
    self:startReq("ReqDoubleCards", BagConfig.Prop50[BagConfig.PROP_ID_DOUBLE_CARD])
end

function BagSysModule:respDoubleCards(cell, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        local cardsData = {}
        if data and data.props and #data.props > 0 then
            for i = 1, #data.props do
                if data.props[i].count ~= 0 then
                    cardsData[#cardsData + 1] = self:transferDataToZJ(data.props[i], true)
                end
            end
        end
        self._doubleCards = cardsData
        table.sort(self._doubleCards, function(a, b)
            return a.endtime < b.endtime
        end)
    else
        self:setDoubleCards({})
    end
    self:dispatchEvent( { name = self.EVENT_UPDATE_DOUBLE_CARD})
end

-- 50数据转换为浙江包数据
function BagSysModule:transferDataToZJ(data, isTimeProp)
    local devideBaseConfig = self:getBagData():getDevideBagConfigData()
    local transferData = {}
    local bagData = {}
    if devideBaseConfig and devideBaseConfig[BagConfig.PROP_ID_DOUBLE_CARD] then
        bagData = devideBaseConfig[BagConfig.PROP_ID_DOUBLE_CARD]
    end
    transferData = {
        script = bagData.script or "",
        props_img = bagData.props_img or {},
        gameids = bagData.gameids or {},
        mutex_category_two = bagData.mutex_category_two or "",
        mutex_category_one = bagData.mutex_category_one or "",
        category_two = bagData.category_two or "",
        category_one = bagData.category_one or "",
        effective_mode = bagData.effective_mode or 0,
        gameid = bagData.gameid or 0,
        props_tag_id = bagData.props_tag_id or "",
        props_desc = bagData.props_desc or "",
        props_remark = bagData.props_remark or "",
        props_tag_name = bagData.props_tag_name or "",
        descp = bagData.descp or 0,
        propsid = bagData.propsid or 0,
        propstype = bagData.propstype or 0,
        props_overview = bagData.props_overview or "",
        propsname = bagData.propsname or "", -- 以上为后台数据不用修改
        is_time_prop = isTimeProp or false, -- 是否为时效道具
        propscnt = data.count,  -- 数量
        props_state = 0, -- 1为正在使用，一般为头像框
        addtime = 0, -- 获得时间
        endtime = data.time or 0, -- 道具结束时间
    }
    return transferData
end

function BagSysModule:setDoubleCards(data)
    self._doubleCards = data
end

-- 获取所有加倍卡（背包用）
function BagSysModule:getDoubleCards()
    return self._doubleCards or {}
end

-- 获取加倍卡总数
function BagSysModule:getDoubleCardsCount()
    local count = 0
    if self._doubleCards then
        for i = 1, #self._doubleCards do
            count = count + self._doubleCards[i].propscnt
        end
    end
    return count
end

return BagSysModule
  �m  