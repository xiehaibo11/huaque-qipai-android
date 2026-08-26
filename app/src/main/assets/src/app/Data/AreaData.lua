local AreaConfig = require("app.Config.AreaConfig")

local AreaData = class("AreaData")

AreaData.EVENT_LOBBY_CHANGED = "EVENT_LOBBY_CHANGED"

function AreaData:ctor()
    cc(self):addComponent("components.behavior.EventProtocol"):exportMethods()
    self._lobbyID = 0
    self:initData()
    self:initActivityData()
    self:initSrsGroupIDData()
end

function AreaData:resetData()
    self._lobbyID = 0
    self:initData()
    self:initActivityData()
    self:initSrsGroupIDData()
end

function AreaData:initData(data)
    data = data or {}
    self._areaID = data.areaID or 0
    self._srsGroupID = data.srsGroupID or 0
    self._areaTypeID = data.areaTypeID or 0
    self._areaName = data.areaName or ""
    self._areaBackground = data.areaBackground
    self._configGameID = data.configGameID or 0 --主游戏id
    self._windMsgID = data.windMsgID or 0   --风信id
    self._notifyGameID = data.notifyGameID or 0 --公告后台配置游戏id
    self._identityID = data.identityID or 0 --身份认证id
    self._apiHubRid = data.apiHubRid or 0
    self._nTeaHouseAppID = data.nTeaHouseAppID or 0 --比赛场appId
    self._commonGoldGameID = data.commonGoldGameID or 0 --金币场主游戏id
    self._channelID = data.channelID or 0   --渠道ID
    self._packgeID = data.packgeID or 0 --有猫腻后台渠道id
    self._appID = data.appID or 0    --有猫腻后台appid
    self._appKey = data.appKey or "" --有猫腻后台APPKEY
    self._wxappID = data.wxappID or ""
    self._wxappKeyID = data.wxappKeyID or ""
    self._wxappIDNew = data.wxappIDNew or ""
    self._wxappKeyIDNew = data.wxappKeyIDNew or ""
    self._shareIconUrl = data.shareIconUrl or ""
    self._XHLinkURL = data.XHLinkURL or ""
    self._UniversalLink = data.UniversalLink or ""
    self._isAgentReqKey = data.isAgentReqKey or ""
    self._isAgentReqSecret = data.isAgentReqSecret or ""
    self._phoneloginAppID = data.phonelogin_app_id or ""
    self._phoneloginAppKey = data.phonelogin_app_key or ""
    self._couponShopID = data.couponShopID or 0  --礼券商城ID
    self._multiShopIDs = data.multiShopIDs
    self._notifAppid = data.notifAppid or 0
    self._notifSecret = data.notifSecret or ""
    self._temporaryBq = data.temporaryBq or ""
    self._h5CpID = data.h5CpID or ""
    self._h5ConfigID = data.h5ConfigID or ""
    self._cusTomerScode = data.cusTomerScode or ""
    self._bindAgentSecret = data.bindAgentSecret or ""
    self._transferBeginTime = data.transferBeginTime or 0
    self._transferEndTime = data.transferEndTime or 0

    data.propList = data.propList or {}
    self._propRoomCardID = data.propList.roomCardID or 0    --房卡
    self._propDiamndID = data.propList.propDiamndID or 0    --钻石 
    self._propCouponID = data.propList.couponID or 0    --礼券
    self._propSpendFeeID = data.propList.spendFeeID or 0    --话费
    self._propSmallRoomCardID = data.propList.smallRoomCardID      --加倍/洗牌道具（小房卡）
    self._propSeniorVip = data.propList.seniorVip --资深会员
    self._propSuperVip = data.propList.superVip --超级会员
    self._propFreeCouponID = data.propList.freeCouponID --免费建房道具
    self._propBindRoomCardID = data.propList.freeRoomCardID or 0    --绑定房卡
    self._propSmallRoomCardRatio = data.smallRoomCardRatio --小房卡转换比例
    self._propList = data.propList

    self._windAid = data.windAid or 0

    self._sortPower = data.sortPower or 0
    self._agentShowYouKe = data.agentShowYouKe or false --用户切换账号登录界面显示游客登录

    self._phoneVerifyCodeAppid = data.phoneVerifyCodeAppid or 0 --获取手机验证码的appid
    self._phoneVerifyCodeSecret = data.phoneVerifyCodeSecret or "" --获取手机验证码的Secret

    self._matchLinkAppID = data.nMatchLinkAppID
    self._supportFriendList = data.supportFriendList or false   --是否支持好友列表
    self._isSupportGoldQuickRecharge = data.isSupportGoldQuickRecharge or false --是否支持快速购买破产
    self._isSupportNewBagSystem = data.isSupportNewBagSystem or false --是否支持新背包系统
    self._isRemodelArea = data.isRemodelArea or false --是否是转型的地区
    self._isFixPayTypeArea = data.isFixPayTypeArea or false --修复了领队模式平摊消耗扣比赛场房卡
    self._isSupportBillDays = data.isSupportBillDays or false --是否支持账单查看最近几天战绩
    self._propFreeRoomCardRatio = data.freeRoomCardRatio  or 1 --赠送房卡转换比例
    self._isNotTeaAgentMode = data.isNotTeaAgentMode or false
    self._supportVip = data.supportVip or false
    self._isVirtualAgent = data.isVirtualAgent or false
    self._supportGift =  data.supportGift or false
    self._isInteractiveExpression = data.isInteractiveExpression or false

    self._isNewLoginProtocol = data.isNewLoginProtocol or false
    self._isNewBoxGameProcess = data.isNewBoxGameProcess or false
    self._isTeaSvrSupportBacthInvite = data.isTeaSvrSupportBacthInvite or false
    self._usedOldGPGames = data.usedOldGPGames
    self._isOpenSimpleSettingGuide = data.isOpenSimpleSettingGuide or false
    self._bSupportFriendsV2 = data.bSupportFriendsV2 or false
    self._bSupportSRS50 = data.isSupportSRS50 or false
    self._bSupportMatch = false
end

function AreaData:setLobbyID(lobbyID)
    self._lobbyID = tonumber(lobbyID)
    self:initData(AreaConfig[self._lobbyID])
    self:dispatchEvent({name = AreaData.EVENT_LOBBY_CHANGED})
end

function AreaData:setXHLinkURL(XHLinkUrl)
    self._XHLinkURL = XHLinkUrl
end

function AreaData:getLobbyID()
    return self._lobbyID
end

function AreaData:getAreaID()
    return self._areaID
end

function AreaData:getAreaDataByLobbyID(getLobbyid)
    local lobbyid = self:getLobbyID()
    if getLobbyid then 
        lobbyid = getLobbyid
    end
    if lobbyid and AreaConfig[lobbyid]  then
        return AreaConfig[lobbyid]
    end
    return {}
end

function AreaData:getSrsGroupID()
    return self._srsGroupID
end

function AreaData:getAreaTypeID()
    return self._areaTypeID
end

function AreaData:getAreaName()
    return self._areaName
end

function AreaData:getAreaBackground()
    return self._areaBackground
end

function AreaData:getConfigGameID()
    return self._configGameID
end

function AreaData:getAreaWindID()
    return self._windMsgID
end

function AreaData:getNotifyGameID()
    return self._notifyGameID
end

--获取当前身份认证id
function AreaData:getIdentityID()
    return self._identityID
end

function AreaData:getApiHubRid()
    return self._apiHubRid
end

function AreaData:getTeaHouseAppID()
    return self._nTeaHouseAppID
end

function AreaData:getCommonGoldGameID()
    return self._commonGoldGameID
end

function AreaData:getChannelID()
    return self._channelID
end

function AreaData:getPackgeID()
    return self._packgeID
end

function AreaData:getAppID()
    return self._appID
end

function AreaData:getAppKey()
    return self._appKey
end

function AreaData:getWXAppID()
    if (device.platform == "ios" and XH.SysTool:CompareToBundleVersion("1.3.10")) or 
    (device.platform == "android" and XH.SysTool:CompareToBundleVersion("1.2.4")) then
        if self._wxappIDNew and self._wxappIDNew ~= "" then
            return self._wxappIDNew
        end
    end
    return self._wxappID
end

function AreaData:getWXAppKeyID()
    if (device.platform == "ios" and XH.SysTool:CompareToBundleVersion("1.3.10")) or 
    (device.platform == "android" and XH.SysTool:CompareToBundleVersion("1.2.4")) then
        if self._wxappKeyIDNew and self._wxappKeyIDNew ~= "" then
            return self._wxappKeyIDNew
        end
    end
    return self._wxappKeyID
end

function AreaData:getShareIconUrl()
    return self._shareIconUrl
end

function AreaData:getXHLinkURL()
    return self._XHLinkURL
end

function AreaData:getUniversalLink()
    return self._UniversalLink
end

function AreaData:getIsAgentReqKey()
    return self._isAgentReqKey
end

function AreaData:getIsAgentReqSecret()
    return self._isAgentReqSecret
end

function AreaData:getPhoneloginAppID()
    return self._phoneloginAppID
end

function AreaData:getPhoneloginAppKey()
    return self._phoneloginAppKey
end

function AreaData:getCouponShopID()
    return self._couponShopID
end

function AreaData:getNotifAppid()
    return self._notifAppid
end

function AreaData:getNotifSecret()
    return self._notifSecret
end

function AreaData:getTemporaryBq()
    return self._temporaryBq
end

function AreaData:getH5CpID()
    if XH.ChannelTool.checkIsCPSChannel() then 
        return "bf_zhejdtcps"
    else
        return self._h5CpID
    end 
end

function AreaData:getH5ConfigID()
    return self._h5ConfigID
end

function AreaData:getCusTomerScode()
    return self._cusTomerScode
end

function AreaData:getBindAgentSecret()
    return self._bindAgentSecret
end

function AreaData:getTransferEndTime()
    return self._transferEndTime
end

function AreaData:getTransferBeginTime()
    return self._transferBeginTime
end

function AreaData:getPropRoomCardID()
    return self._propRoomCardID
end

function AreaData:getPropDiamndID()
    return self._propDiamndID
end

function AreaData:getPropCouponID()
    return self._propCouponID
end

function AreaData:getPropSpendFeeID()
    return self._propSpendFeeID
end

function AreaData:getPropSmallRoomCardID()
    return self._propSmallRoomCardID
end

function AreaData:getPropSeniorVip()
    return self._propSeniorVip
end

function AreaData:getPropSuperVip()
    return self._propSuperVip
end

function AreaData:getPropFreeCouponID()
    return self._propFreeCouponID
end

function AreaData:getPropBindRoomCardID()
    return self._propBindRoomCardID
end

function AreaData:isInTransferActivity()
    return os.time() < self:getTransferEndTime()
end

function AreaData:getPropSmallRoomCardRatio()
    return self._propSmallRoomCardRatio
end

function AreaData:getPropFreeRoomCardRatio()
    return self._propFreeRoomCardRatio
end

function AreaData:getAgentShowYouKe()
    return self._agentShowYouKe
end

function AreaData:getPropList()
    return self._propList
end

function AreaData:getWindAid()
    return self._windAid
end

function AreaData:getPhoneVerifyCodeAppid()
    return self._phoneVerifyCodeAppid
end

function AreaData:getPhoneVerifyCodeSecret()
    return self._phoneVerifyCodeSecret
end

function AreaData:getMatchLinkAppID()
    return self._matchLinkAppID
end

function AreaData:initActivityData()
    self._boxControlGame = {}
    self._goldControlGame = {}
    self._boxActivityGame = {30066,30183}
    self._goldActivityGame = {30386, 30421}
    self._activityEndTime = 0
    self._activityStartTime = 0
end

function AreaData:setBoxControlGame(games)
    self._boxControlGame = games or {}
end

function AreaData:getBoxControlGame()
    return self._boxControlGame or {}
end

function AreaData:setGoldControlGame(games)
    self._goldControlGame = games or {}
end

function AreaData:getGoldControlGame()
    return self._goldControlGame or {}
end

--获取活动信息
function AreaData:setBoxActivityGame(games)
    self._boxActivityGame = games or {}
end

function AreaData:getBoxActivityGame()
    return self._boxActivityGame or {}
end

function AreaData:setGoldActivityGame(games)
    self._goldActivityGame = games or {}
end

function AreaData:getGoldActivityGame()
    return self._goldActivityGame or {}
end

function AreaData:setActivityTime(startTime, endTime)
    self._activityStartTime = startTime
    self._activityEndTime = endTime
end

function AreaData:getActivityTime()
    local startTime = self._activityStartTime
    local endTime = self._activityEndTime
    return startTime, endTime
end

function AreaData:initSrsGroupIDData()
    self._srsGroupIDData = {}
end

function AreaData:setSrsGroupIDbyAppID(appID, srsGroupID)
    self._srsGroupIDData[appID] = srsGroupID
end

function AreaData:getSrsGroupIDbyAppID(appID)
    return self._srsGroupIDData[appID] or 0
end

function AreaData:getLocationGameRuleKeyword()
	local rtn = nil
	local lobbyID = self:getLobbyID()
	if lobbyID == XH.LOBBY_ID.TAIZHOU then --台州
		rtn = "forceGPS"
	elseif lobbyID == XH.LOBBY_ID.ZHOUSHAN then --舟山
		rtn = "PrevCheat"
	elseif lobbyID == XH.LOBBY_ID.LISHUI then --丽水
		rtn = "bCheckPos"
	elseif lobbyID == XH.LOBBY_ID.JIAXING then --嘉兴
		rtn = "PrevCheat"
    elseif lobbyID ==  XH.LOBBY_ID.JINHUA then --金华
        rtn = "PrevCheat"
    elseif lobbyID == XH.LOBBY_ID.SHAOXING3D then --绍兴
		rtn = "bCheckPos"
	end
	return rtn
end

--获取当前区游戏名字列表
function AreaData:getAreaGameNameList()
    return XH.ReadGameListAndRuleConfig.getGameNameList(self:getLobbyID()) or {}
end

function AreaData:getBoxRoomGameList()
    return XH.ReadGameListAndRuleConfig.getBoxRoomGameList(self:getLobbyID()) or {}
end

function AreaData:getTeaHouseRoomGameList()
    return XH.ReadGameListAndRuleConfig.getTeaHouseRoomGameList(self:getLobbyID()) or {}
end

function AreaData:getGroupRoomGameList()
    return XH.ReadGameListAndRuleConfig.getGroupRoomGameList(self:getLobbyID()) or {}
end

function AreaData:isSupportAutoPhoneBind()
    if self:getLobbyID() == XH.LOBBY_ID.LISHUI or
    self:getLobbyID() == XH.LOBBY_ID.WENZHOU or
    self:getLobbyID() == XH.LOBBY_ID.SHUKE or
    self:getLobbyID() == XH.LOBBY_ID.QUZHOU or
    self:getLobbyID() == XH.LOBBY_ID.SHAOXING or
    self:getLobbyID() == XH.LOBBY_ID.YUEQING or
    self:getLobbyID() == XH.LOBBY_ID.QINGTIAN or
    self:getLobbyID() == XH.LOBBY_ID.RUIAN or 
    self:getLobbyID() == XH.LOBBY_ID.JIAXING then
        return true
    end
    return false
end

function AreaData:isSupportPhoneLogin()
    --所有区都开启个验登录
    if true then
        return true
    end
end

function AreaData:isSupportFriendList()
    return self._supportFriendList
end

function AreaData:isSupportGoldQuickRecharge()
    return self._isSupportGoldQuickRecharge
end

function AreaData:isSupportNewBagSystem()
   return self._isSupportNewBagSystem
end

function AreaData:isRemodelArea()
    return self._isRemodelArea
end

function AreaData:isFixPayTypeArea()
    return self._isFixPayTypeArea
end

function AreaData:isSupportBillDays()
    return self._isSupportBillDays
end

function AreaData:isNotTeaAgentMode()
    return self._isNotTeaAgentMode 
end

function AreaData:isTeaSvrSupportBacthInvite()
    return self._isTeaSvrSupportBacthInvite
end

function AreaData:isOpenSimpleSettingGuide()
    return self._isOpenSimpleSettingGuide
end

function AreaData:getGoldSrsGroupID()
    local GlobalCfg = require("app.Config.GlobalConfig")
    local DEBUG = GlobalCfg.IsDebug
    if DEBUG then 
        return 5167
    end
    return 5067
end

function AreaData:isSupportVip()
    return self._supportVip
end

function AreaData:isSupportGift()
    return self._supportGift
end

function AreaData:setVirtualAgentState(state)
    self._isVirtualAgent = state or false
end

function AreaData:getVirtualAgentState()
    return self._isVirtualAgent
end

function AreaData:getEnterLobbyNeedOpenIm()
    return self._isEnterLobbyNeedOpenIm
end

function AreaData:setEnterLobbyNeedOpenIm(bOpen)
    self._isEnterLobbyNeedOpenIm = bOpen
end

function AreaData:getGeTuiMsgLive()
    return self._geTuiMsgLive
end

function AreaData:setGeTuiMsgLive(msg)
    self._geTuiMsgLive = msg
end

function AreaData:isInteractiveExpression()
    return self._isInteractiveExpression
end

function AreaData:getMultiShopIDs()
    return self._multiShopIDs
end

function AreaData:getIsNewLoginProtocol()
   return self._isNewLoginProtocol 
end

function AreaData:getIsNewBoxGameProcess()
    return self._isNewBoxGameProcess 
end

function AreaData:getUsedOldGPGames()
    return self._usedOldGPGames 
end

function AreaData:getTenantid()
    return 4
end

function AreaData:isSupportFriendsV2()
    return self._bSupportFriendsV2
end

function AreaData:getGoldPropId()
    return 101473
end

function AreaData:isSupportSRS50()
    return self._bSupportSRS50
end

function AreaData:setIsSupportMatch(isSupport)
    self._bSupportMatch = isSupport
end

-- 是否打开比赛场
function AreaData:isSupportMatch()
    return self._bSupportMatch
end

return AreaData  �A  