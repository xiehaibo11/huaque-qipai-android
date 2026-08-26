local PlayerData = class("PlayerData", XH.ModuleBase)
local areaConfig = require("app.Config.AreaConfig")

local DEBUG = require("app.Config.GlobalConfig").IsDebug --灰度
local KW_CLOSE_REAL_NAME = true -- 此变量用于控制灰度实名弹窗弹出

PlayerData.EVENT_AGENT_FLAG_CHANGED = "EVENT_AGENT_FLAG_CHANGED"
PlayerData.EVENT_PHONE_CHANGED = "EVENT_PHONE_CHANGED"
PlayerData.EVENT_BIND_DSB_CHANGED = "EVENT_BIND_DSB_CHANGED"
PlayerData.EVENT_POSITION_CHANGED = "EVENT_POSITION_CHANGED"
PlayerData.EVENT_IDENTITY_CHANGED = "EVENT_IDENTITY_CHANGED"
PlayerData.EVENT_SR_CHANGED = "EVENT_SR_CHANGED"
PlayerData.EVENT_PLAYER_DROP_CHANGED = "EVENT_PLAYER_DROP_CHANGED"
PlayerData.EVENT_PLAYER_TIME_DROPS_CHANGED = "EVENT_PLAYER_TIME_DROPS_CHANGED"
PlayerData.EVENT_BIND_USER_CHANGED = "EVENT_BIND_USER_CHANGED"
PlayerData.EVENT_PLAYER_INFO_CHANGED = "EVENT_PLAYER_INFO_CHANGED"

PlayerData.EVENT_FORCE_SHOW_IDENTITYCHECKVIEW = "EVENT_FORCE_SHOW_IDENTITYCHECKVIEW"
PlayerData.EVENT_FORCE_SHOW_IDENTITYCHECKVIEW_NEW = "EVENT_FORCE_SHOW_IDENTITYCHECKVIEW_NEW"
PlayerData.EVENT_FORCE_SHOW_IDENTITYCHECKVIEW_NEW_ING = "EVENT_FORCE_SHOW_IDENTITYCHECKVIEW_NEW_ING"

PlayerData.EVENT_GET_PLAYER_POSITION = "EVENT_GET_PLAYER_POSITION"
PlayerData.EVENT_REALNAME_TIMEOVER = "EVENT_REALNAME_TIMEOVER"
PlayerData.EVENT_REALNAME_TIMEOVER_LIMIT = "EVENT_REALNAME_TIMEOVER_LIMIT"
PlayerData.EVENT_IDENTITY_GETBACK = "EVENT_IDENTITY_GETBACK"

PlayerData.EVENT_NEW_APPLY_WRITE_OFF = "EVENT_NEW_APPLY_WRITE_OFF"
PlayerData.EVENT_PLAYER_RECHARGE_FLAG = "EVENT_PLAYER_RECHARGE_FLAG"

function PlayerData:ctor()
    PlayerData.super.ctor(self)
    self:initDatas()
    self:initUserTagData()
    self._whiteListGames = {}
    self._clubWhiteListGames = {}
end

function PlayerData:getReqConfig()
    return {
        ReqPlayerGoldCoin = {reqPath = "app.Req.PlayerMessage.ReqPlayerGoldCoin", callBack = self.onTranPlayerGoldCallBack},
        ReqPlayerPosition = {reqPath = "lobby.Req.Room.ReqPlayerPosition", callBack = self.onPlayerPositionCallBack},
        ReqPlayerDrop = {reqPath = "app.Req.PlayerMessage.ReqPlayerDrop", callBack = self.onPlayerDropCallBack},
        ReqIdentityStatus = {reqPath = "lobby.Req.IdentityDefend.ReqIdentityStatus", callBack = self.onIdentityStatusCallback},
        ReqGameIdentityStatus = {reqPath = "lobby.Req.IdentityDefend.ReqIdentityStatus", callBack = self.onGameIdentityStatusCallback},
        ReqDefendIdentity = { reqPath = "lobby.Req.IdentityDefend.ReqDefendIdentity", callBack = self.onDefendCallback },
        ReqRealNameAuth = {reqPath = "lobby.Req.IdentityDefend.ReqRealNameAuth", callBack = self.onRealNameAuthCallback},
        ReqIsAgent = {reqPath = "app.Req.PlayerMessage.ReqIsAgent", callBack = self.onIsAgentCallBack},
        ReqCheckBind = {reqPath = "app.Req.ThreeLevelDistribution.ReqCheckBind", callBack = self.onCheckInvateCodeCallback},
        ReqPhoneBindingState = {reqPath = "lobby.Req.PhoneBind.ReqPhoneBindingState", callBack = self.onPhoneStateCallback},
        ReqCheckBindUser = { reqPath = "app.Req.PlayerMessage.ReqCheckBindUser", callBack = self.onCheckBindUserCallback },
        ReqChangePlayerInfo = { reqPath = "app.Req.PlayerMessage.ReqChangePlayerInfo", callBack = self.onChangeInfoCallBack },
        ReqVisitorHeart = {reqPath = "lobby.Req.IdentityDefend.ReqVisitorHeart", callBack = self.onReqVisitorHeartCallback},
        ReqPlayerWriteOffStatus = {reqPath = "app.Req.PlayerMessage.ReqPlayerWriteOffStatus", callBack = self.onPlayerWriteOffStatusCallBack},
        ReqCancelWriteOff = {reqPath = "app.Req.PlayerMessage.ReqCancelWriteOff", callBack = self.onCancelWriteOffCallBack},
    }
end

function PlayerData:resetData()
    self:initDatas()
    self:initUserTagData()
end

function PlayerData:initDatas()
    self._nAreaTypeID = 0
    self._nBrandID = 0
    self._nIPArea = 0
    self._nLastIP = 0
    self._nLastIPArea = 0
    self._nLastSP = 0
    self._nLevel = 0
    self._nlimited = 0
    self._nLobbyVer = 0
    self._nNumID = 0
    self._nProtected = 0
    self._nSP = 0
    self._nUsedDays = 0
    self._nUsedTime = 0
    self._strProtectURL = ""
    self._strNotify = ""
    self._nLastLeaveTime = 0
    self._nOnLineTime = 0
    self._nBreakTime = 0
    self._strSessionid = 0
    self._bIdentityStatus = 0
    self._bIdentityInwhitelist = 0
    self._bIdentityLevel = 0
    self._nActiveValue = 0
    self._nPassiveValue = 0
    self._nConfigVale = 0

    self._playerPosition = {}

    self._isAgent = false
    self._tPlusData = {}
    self._tPlusData = {
        bySex = '0',
        nClientType = 0,
        nHead = 0,
        nIP = 0,
        nOSVer = 0,
        nRegTime = 0,
        nRight = 0,
        nVipEndTime = 0,
        nVipID = 0,
        strIdentify = 0,

        strNickName = "",
        strNickName2 = "",
        strPTNumID = "",
        strUserID = "",
        strPTID = "",
        nSR = 0,
        _nProtected = 0,
        elimited = 0
    }
    self._ppd = {}
    self._strWeChatURL = ""
    self._strThirdNickName = ""
    self._nPid = ""
    self._strUnionId = ""
    self._phoneBindStr = ""
    self._bindDSB = false

    self._props = {}
    for _, v in pairs(areaConfig) do
        local lobbyList = v.propList
        if lobbyList then
            for _, t in pairs(lobbyList) do
                self._props[t] = 0
            end
        end
    end
    self._timeProps = {}
    self._playerPosition = {}
    self._playerPosition = {
        gameID = 0,
        gameAppID = 0,
        roomID = 0,
        teaID = 0,
        srsGroupID = 0
    }

    self._bindUserData = nil
    self._isFillInfo = false

    self._showFreeQuan = false

    self._showShopList = {}
    self._hideShopList = {}
    self._isFreeVip = false
    ------老用户回馈道具---------
    self._requitePrize = {}
    ------请求H5 游戏 微信注册的UserID------
    -- 用来进入传奇来了的用户号
    self._legendUserID = ""
    -- 用来进入捕鱼来的用户号
    self._buYuUserID = ""
    -- 是否需要带config_id 1是 0否
    self._isNeedConfigID = 1
    -- 新实名游客心跳发送
    self._heartBeatScheduleIDs = nil
    --保存玩家当前比赛场
    self._teaId = 0
    --合规策略游戏是否需要隐藏
    self._isNeedHideComGames = false
    self._isNeedHideClubComGames = false
    --合规策略金币游戏是否需要隐藏
    self._isNeedHideComGoldGames = false
    --是否使用新合规策略
    self._isUseNewHideGameReq = false
    --问卷系统
    self._complainData = {}
    self._isreqComplainData = true
    self._isRechargePlayer= false
    -- sxvip 
    self._sxvip = {
        remainder = 0,  --过期时间
    }
end

function PlayerData:setPlayerData(data)
    if data then
        if data.playerData then
            self._nAreaTypeID = data.playerData.areatypeid
            self._nBrandID = data.playerData.areaid
            self._nIPArea = data.playerData.iparea
            self._nLastIP = data.playerData.lastip
            self._nLastIPArea = data.playerData.lastiparea
            self._nLastSP = data.playerData.lastsp
            self._nLevel = data.playerData.eLevel
            self._nlimited = data.playerData.elimited
            self._nLobbyVer = data.playerData.lobbyver
            self._nNumID = data.playerData.numid
            self._nProtected = data.playerData.eprotected
            self._nSP = data.playerData.sp
            self._nUsedDays = data.playerData.eUsedDays
            self._nUsedTime = data.playerData.eUsedTime
            self._strProtectURL = data.playerData.protecturl
            self._strNotify = data.playerData.notify
            self._nLastLeaveTime = data.playerData.lastleavetime
            self._nOnLineTime = data.playerData.onlinetime
            self._nBreakTime = data.playerData.breaktime
            self._strSessionid = data.playerData.sessionid
        end
        if data.ppd then
            self._tPlusData.bySex = data.ppd.sex
            self._tPlusData.nClientType = data.ppd.clienttype
            self._tPlusData.nHead = data.ppd.head
            self._tPlusData.nIP = data.ppd.ip
            self._tPlusData.nOSVer = data.ppd.osver
            self._tPlusData.nRegTime = data.ppd.regtime
            self._tPlusData.nRight = data.ppd.right
            self._tPlusData.nVipEndTime = data.ppd.vipendtime
            self._tPlusData.nVipID = data.ppd.vipid
            self._tPlusData.strIdentify = data.ppd.identify
            self._tPlusData.strNickName = un.StringUtils.GB_18030_2000_TO_UTF8(data.ppd.nickname)
            self._tPlusData.strNickName2 = data.ppd.nickname2
            self._tPlusData.strPTNumID = un.StringUtils.GB_18030_2000_TO_UTF8(data.ppd.ptnumid)
            self._tPlusData.strUserID = un.StringUtils.GB_18030_2000_TO_UTF8(data.ppd.userid)
            self._tPlusData.strPTID = un.StringUtils.GB_18030_2000_TO_UTF8(data.ppd.ptid)
            self._tPlusData._nlimited = data.ppd.elimited
            self._tPlusData._nProtected = data.ppd.eprotected
            self._tPlusData.notify = data.ppd.notify
            self._ppd = data.ppd
        end
        if data.simpleInfo then
            if data.simpleInfo.headurl ~= "" then
                self._strWeChatURL = data.simpleInfo.headurl
            end
        end
        if data.ppd then
            if data.ppd.notify ~= "" then
                self:setWeChatURL(data.ppd.notify)
            end
        end
    end
end

function PlayerData:getPlayerPhone()
    if XH.lobby:getModule("PhoneBind"):isSpecialCode() then
        self._phoneBindStr = "15356558599"
    end
    return self._phoneBindStr
end

function PlayerData:setPlayerPhone(phone)
    self._phoneBindStr = phone
    self:dispatchEvent({name = PlayerData.EVENT_PHONE_CHANGED})
end

function PlayerData:getPlayerDrop()
    return self._props
end

function PlayerData:setPlayerDrop(propsList)
    for _, v in pairs(areaConfig) do
        local lobbyList = v.propList
        if lobbyList then
            for _, t in pairs(lobbyList) do
                self._props[t] = 0
            end
        end
    end
    for _, v in pairs(propsList) do
        -- 洗牌客户端冻结房卡数量
        local freezeCount = self:getFreezePropCountByID(v.id) or 0
        self._props[v.id] = v.count - freezeCount
    end
    self:dispatchEvent({name = PlayerData.EVENT_PLAYER_DROP_CHANGED})
end

function PlayerData:setPlayerTimeDrop(timeProps)
    self._timeProps = timeProps
    self:dispatchEvent({name = PlayerData.EVENT_PLAYER_TIME_DROPS_CHANGED})
end

function PlayerData:getPlayerTimeDrop()
    return self._timeProps
end

function PlayerData:setRoomCard(nRoomCard)
    local lobbyid = XH.userDefault:getValue(XH.userDefault.KEY_ID.LOBBYID,0)
    if areaConfig[lobbyid] then
        local propList = areaConfig[lobbyid].propList
        if propList then
            self._props[propList.roomCardID] = nRoomCard
        end
    end
end

function PlayerData:getRoomCard()
    local lobbyid = XH.userDefault:getValue(XH.userDefault.KEY_ID.LOBBYID,0)
    if areaConfig[lobbyid] then
        local propList = areaConfig[lobbyid].propList
        if propList then
            local tmpRoomCardCount = tonumber(self._props[propList.roomCardID])
            if propList.smallRoomCardID and tonumber(self._props[propList.smallRoomCardID]) ~= 0 and XH.areaData:getPropSmallRoomCardRatio() then
                tmpRoomCardCount = tmpRoomCardCount + tonumber(self._props[propList.smallRoomCardID]) / XH.areaData:getPropSmallRoomCardRatio()
            end
            return tmpRoomCardCount
        end
    end
    return 0
end

function PlayerData:setDiamnd(nDiamd)
    local lobbyid = XH.userDefault:getValue(XH.userDefault.KEY_ID.LOBBYID, 0)
    if areaConfig[lobbyid] then
        local propList = areaConfig[lobbyid].propList
        if propList then self._props[propList.propDiamndID] = nDiamd end
    end
end

function PlayerData:getDiamnd()
    local lobbyid = XH.userDefault:getValue(XH.userDefault.KEY_ID.LOBBYID, 0)
    if areaConfig[lobbyid] then
        local propList = areaConfig[lobbyid].propList
        if propList then return self._props[propList.propDiamndID] end
    end
    return 0
end

function PlayerData:getLiQun()
    local lobbyid = XH.userDefault:getValue(XH.userDefault.KEY_ID.LOBBYID,0)
    if areaConfig[lobbyid] then
        local propList = areaConfig[lobbyid].propList
        if propList then
            return self._props[propList.couponID]
        end
    end
    return 0
end

function PlayerData:getHuaFei()
    local lobbyid = XH.userDefault:getValue(XH.userDefault.KEY_ID.LOBBYID,0)
    if areaConfig[lobbyid] then
        local propList = areaConfig[lobbyid].propList
        if propList then
            return self._props[propList.spendFeeID]
        end
    end
    return 0
end

function PlayerData:getIPass()
    local lobbyid = XH.userDefault:getValue(XH.userDefault.KEY_ID.LOBBYID,0)
    if areaConfig[lobbyid] then
        local propList = areaConfig[lobbyid].propList
        if propList then
            return (self._props[propList.ipassFree] + self._props[propList.ipassCharge]) / 100
        end
    end
    return 0
end

function PlayerData:setSR(nSR)
    self._tPlusData.nSR = nSR
    self:dispatchEvent({name = PlayerData.EVENT_SR_CHANGED})
end

function PlayerData:getSR()
    return self._tPlusData.nSR
end

function PlayerData:getIdentityStatus()
    --提审输入特殊码默认状态为已认证
    if XH.lobby:getModule("IdentityCheck"):isSpecialCode() then
        self._bIdentityStatus = 0
    end
    return self._bIdentityStatus
end

function PlayerData:setProtected(isDefend)
    if isDefend then
        self._bIdentityStatus = isDefend
    end    
    self:dispatchEvent({name = PlayerData.EVENT_IDENTITY_CHANGED})
end

function PlayerData:setProtectedInwhitelist(isDefend)
    if isDefend then
        self._bIdentityInwhitelist = isDefend
    end 
end

function PlayerData:setProtectedLevel(isDefend)
    if isDefend then
        self._bIdentityLevel = isDefend
    end 
    self:dispatchEvent({name = PlayerData.EVENT_IDENTITY_GETBACK})
end

function PlayerData:isProtectedLevel(isDefend)
    return self._bIdentityLevel > 0
end

function PlayerData:getProtectedInwhitelist()
     return self._bIdentityInwhitelist
end

function PlayerData:isProtected()
    return self._bIdentityStatus > 0
end

function PlayerData:isAdult()
    return self._bIdentityStatus ~= 1
end

--1 检测身份证号码位数
--2 检测身份证号码是否符合编码规则
--多级认证信息验证规则(实名认证)
function PlayerData:setActiveValue(nActiveValue)
    self._nActiveValue = nActiveValue
end

function PlayerData:getActiveValue()
    return self._nActiveValue
end

--伪绑定+海外网络是否强弹
function PlayerData:setConfigVale(nConfigVale)
    self._nConfigVale = nConfigVale
end

function PlayerData:getConfigVale()
   return  self._nConfigVale
end

--1 弹窗可关闭
--2 弹窗不可关闭
--实名认证界面被动弹出级别(实名认证)
function PlayerData:setPassiveValue(nPassiveValue)
    self._nPassiveValue = nPassiveValue
end

function PlayerData:getPassiveValue()
   return self._nPassiveValue
end

function PlayerData:getAreaTypeID()
    return self._nAreaTypeID
end

function PlayerData:getBrandID()
    return self._nBrandID
end

function PlayerData:getIPArea()
    return self._nIPArea
end

function PlayerData:getLastIP()
    return self._nLastIP
end

function PlayerData:getLastIPArea()
    return self._nLastIPArea
end

function PlayerData:getLastSP()
    return self._nLastSP
end

function PlayerData:getLevel()
    return self._nLevel
end

function PlayerData:getLimited()
    return self._nlimited
end

function PlayerData:getLobbyVersion()
    return self._nLobbyVer
end

function PlayerData:getNumberID()
    return self._nNumID
end

function PlayerData:getProtected()
    return self._nProtected
end

function PlayerData:getSP()
    return self._nSP
end

function PlayerData:getUsedDays()
    return self._nUsedDays
end

function PlayerData:getUsedTime()
    return self._nUsedTime
end

function PlayerData:setProtectURL(_strProtectURL)
    self._strProtectURL = _strProtectURL
end

function PlayerData:getProtectURL()
    return self._strProtectURL
end

function PlayerData:getNotify()
    return self._strNotify
end

function PlayerData:getLastLeaveTime()
    return self._nLastLeaveTime
end

function PlayerData:getOnLineTime()
    return self._nOnLineTime
end

function PlayerData:getBreakTime()
    return self._nBreakTime
end

function PlayerData:setSessionID(sessionID)
    self._strSessionid = sessionID
end

function PlayerData:getSessionID()
    return self._strSessionid
end

function PlayerData:setSex(sex)
    self._tPlusData.bySex = sex
end

function PlayerData:getSex()
    if self._tPlusData.bySex == 49 or self._tPlusData.bySex == 1 then
        return 1
    elseif self._tPlusData.bySex == 48 or self._tPlusData.bySex == 0 then
        return 0
    else
        return 0
    end
end

function PlayerData:getClientType()
    return self._tPlusData.nClientType
end

function PlayerData:getHead()
    return self._tPlusData.nHead
end

function PlayerData:getIP()
    return self._tPlusData.nIP
end

function PlayerData:getOSVersion()
    return self._tPlusData.nOSVer
end

function PlayerData:getRegTime()
    return self._tPlusData.nRegTime
end

function PlayerData:getRight()
    return self._tPlusData.nRight
end

function PlayerData:getVipEndTime()
    return self._tPlusData.nVipEndTime
end

function PlayerData:getVipID()
    return self._tPlusData.nVipID
end

function PlayerData:getIdentify()
    return self._tPlusData.strIdentify
end

function PlayerData:setIdentify(strIdentify)
    self._tPlusData.strIdentify = strIdentify
end

function PlayerData:setNickName(strNickName)
    self._tPlusData.strNickName = strNickName
end

function PlayerData:setNickName2(strNickName)
    self._tPlusData.strNickName2 = strNickName
end

function PlayerData:getNickName()
    if self._tPlusData.strNickName2 ~= "" then
        return self._tPlusData.strNickName2
    end
    return self._tPlusData.strNickName
end

function PlayerData:getPTNumberID()
    return self._tPlusData.strPTNumID
end

function PlayerData:getUserID()
    return self._tPlusData.strUserID
end

function PlayerData:getPTID()
    return self._tPlusData.strPTID
end

function PlayerData:getppd()
    return self._ppd
end

function PlayerData:setWeChatURL(_strWeChatURL)
    self._strWeChatURL = _strWeChatURL
end

function PlayerData:getWeChatURL()
    local url = XH.StringTool.urlToHttps(self._strWeChatURL)
    return url
end

function PlayerData:setThirdNickName(strNickName)
    self._strThirdNickName = strNickName
end

function PlayerData:setPid(pid)
    self._nPid = pid
end

function PlayerData:getPid()
    return self._nPid
end

function PlayerData:getThirdNickName()
    return self._strThirdNickName
end

function PlayerData:setUnionId(_strUnionId)
    self._strUnionId = _strUnionId ~= nil and _strUnionId or ""
end

function PlayerData:getUnionId()
    return self._strUnionId
end

function PlayerData:setPlayerPosition(data)
    self._playerPosition = {}
    self._playerPosition.gameID = data.gameid
    self._playerPosition.gameAppID = data.gameappid
    self._playerPosition.roomID = data.roomid
    self._playerPosition.teaID = data.teaid --比赛场口令
    self._playerPosition.srsGroupID = data.srsgroupid
    self._playerPosition.roomMode = data.roommode
    self._playerPosition.teaNumber = data.teaNumber --比赛场id
    self:dispatchEvent({name = PlayerData.EVENT_POSITION_CHANGED})
end

function PlayerData:getPlayerPosition()
    return self._playerPosition
end

function PlayerData:setNeedHideComGames(isHide)
    self._isNeedHideComGames = isHide
end

function PlayerData:getNeedHideComGames()
    return self._isNeedHideComGames
end

function PlayerData:setNeedHideComGoldGames(isHide)
    self._isNeedHideComGoldGames = isHide
end

function PlayerData:getNeedHideComGoldGames()
    return self._isNeedHideComGoldGames
end

function PlayerData:setNeedHideClubComGames(isHide)
    self._isNeedHideClubComGames = isHide
end

function PlayerData:getNeedHideClubComGames()
    return self._isNeedHideClubComGames
end

function PlayerData:setUseNewHideGameReq(bNewReq)
    self._isUseNewHideGameReq = bNewReq
end

function PlayerData:isUseNewHideGameReq(bNewReq)
    return self._isUseNewHideGameReq
end

function PlayerData:flushBindUserData()
    self:startReq("ReqCheckBindUser", XH.areaData:getSrsGroupID(), 10)
end

function PlayerData:onCheckBindUserCallback(req, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        self:setBindUserData(data)
    end
end

function PlayerData:setBindUserData(bindUserData)
    if bindUserData.bind_phone ~= nil then
        local keyPos = string.find(bindUserData.bind_phone, "_")
        if keyPos then
            bindUserData.bind_phone = string.sub(bindUserData.bind_phone, keyPos + 1)
        end
        for i = 1, #bindUserData.bind_phone do
            local char = string.byte(bindUserData.bind_phone, i, i)
            if char < 48 or char > 57 then
                bindUserData.bind_phone = ""
                break
            end
        end
    end
    self._bindUserData = bindUserData
    self:dispatchEvent({name = PlayerData.EVENT_BIND_USER_CHANGED})
end

function PlayerData:getBindUserData()
    return self._bindUserData
end

function PlayerData:isBindPhone()
    local isBindPhone = false
    local phone = self:getPlayerPhone()
    if phone and phone ~= "" then
        isBindPhone = true
    end
    if self._bindUserData and #self._bindUserData.bind_phone >= 11 then
        isBindPhone = true
    end
    return isBindPhone
end

function PlayerData:getBindPhone()
    local phone = self:getPlayerPhone()
    if phone and phone ~= "" then
        return phone
    end
    if self._bindUserData and #self._bindUserData.bind_phone >= 11 then
        return self._bindUserData.bind_phone
    end
    return ""
end

function PlayerData:getEncryptBindPhone()
    local bindPhone = self:getBindPhone()
    if bindPhone == "" then
        return bindPhone
    end
    bindPhone = string.sub(bindPhone, 1, 3) .. "****" .. string.sub(bindPhone, #bindPhone - 3)
    return bindPhone
end

function PlayerData:getIsBindDSB()
    return self._bindDSB
end

function PlayerData:setBindDSB(isBindDSB)
    self._bindDSB = isBindDSB
    self:dispatchEvent({name = PlayerData.EVENT_BIND_DSB_CHANGED})
end

function PlayerData:isNeedFillInfo()
    local url = self:getWeChatURL()
    if self._isFillInfo or #url > 0 then
        return false
    end
    self._isFillInfo = true
    return true
end

function PlayerData:getIsShowFreeQuan()
    return self._showFreeQuan
end

function PlayerData:setIsShowFreeQuan(bShow)
    self._showFreeQuan = bShow or false
end

function PlayerData:getHideShopList()
    return self._hideShopList
end

function PlayerData:setHideShopList(hideShopList)
    self._hideShopList = hideShopList or {}
end
function PlayerData:getShowShopList()
    return self._showShopList
end

function PlayerData:setShowShopList(showShopList)
    self._showShopList = showShopList or {}
end

function PlayerData:setFreeVip(isFreeVip)
    self._isFreeVip = isFreeVip or false
end

function PlayerData:isFreeVip()
    return self._isFreeVip
end

function PlayerData:getRequitePrize()
    return self._requitePrize
end

function PlayerData:setRequitePrize(requitePrize)
    self._requitePrize = requitePrize or {}
end

-- H5相关
function PlayerData:getLegendUserID()
    return self._legendUserID
end

function PlayerData:setLegendUserID(legendUserID)
    self._legendUserID = legendUserID or ""
end

function PlayerData:getBuYuUserID()
    return self._buYuUserID
end

function PlayerData:setBuYuUserID(buYuUserID)
    self._buYuUserID = buYuUserID or ""
end

function PlayerData:getWeChatBuYuUserID()
    return self._weChatBuYuUserID
end

function PlayerData:setWeChatBuYuUserID(weChatBuYuUserID)
    self._weChatBuYuUserID = weChatBuYuUserID or ""
end

function PlayerData:isNeedConfigID()
    return self._isNeedConfigID
end

function PlayerData:setIsNeedConfigID(isNeedConfigID)
    self._isNeedConfigID = isNeedConfigID or 1
end

function PlayerData:initUserTagData(data)
    self._userTagInfo = {}
    if data then
        local UserTagInfo = data.UserTagInfo or {}
        for _, v in pairs(UserTagInfo) do
            self._userTagInfo[v.tag_id] = v
        end
    end
end

function PlayerData:getUserTagByID(id)
    return clone(self._userTagInfo[id])
end

--获取全部tag
function PlayerData:getAllTags()
    return clone(self._userTagInfo)
end

--玩家是否为金币场玩家
function PlayerData:checkGoldRoomUser()
    local UserDefine = require("app.Define.UserDefine")
    if self._userTagInfo[UserDefine.USER_TAG.GOLDROOM_USER] then
        return true
    end
    return false
end

--请求玩家金币
function PlayerData:flushGoldCoin()
    local gameID = XH.areaData:getCommonGoldGameID()
    local srsGroupID =XH.areaData:getSrsGroupID()
    self:startReq("ReqPlayerGoldCoin", gameID, srsGroupID, 10)
end

function PlayerData:onTranPlayerGoldCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
         if data.purse then
            self:setSR(data.purse)
         else
            self:setSR(0)
         end
         local eventDispatcher = cc.Director:getInstance():getEventDispatcher()
         local event = cc.EventCustom:new("GoldBaseLive.GameReJoinTableWithGold")
         eventDispatcher:dispatchEvent(event)
    end
end

function PlayerData:flushPlayerDrop()
    local gameID = XH.areaData:getConfigGameID()
    local srsGroupID = XH.areaData:getSrsGroupID()
    self:startReq("ReqPlayerDrop",gameID,srsGroupID,500)
end

function PlayerData:onPlayerDropCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        self:setPlayerTimeDrop(data.timeprops)
        self:setPlayerDrop(data.props)
    end
end

function PlayerData:flushPlayerPosition()
    local lobbyID = XH.areaData:getLobbyID()
    local configGameID = XH.areaData:getConfigGameID()
    local srsGroupID= XH.areaData:getSrsGroupID()
    local areaTypeID = XH.areaData:getAreaTypeID()
    self:startReq("ReqPlayerPosition", configGameID, areaTypeID, lobbyID, srsGroupID, 15)
end

function PlayerData:onPlayerPositionCallBack(reqPosition, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        self:setPlayerPosition(data[1])
        self:dispatchEvent({name = PlayerData.EVENT_GET_PLAYER_POSITION})
    end
end

-- 请求玩家认证状态
function PlayerData:flushPlayerDefendIdentity()
    self:startReq("ReqIdentityStatus", XH.KW_CONFIG_LOGIC_TIME_OUT)
end

-- 请求玩家游戏认证状态
function PlayerData:flushPlayerGameDefendIdentity(gameid)
    self:startReq("ReqGameIdentityStatus", XH.KW_CONFIG_LOGIC_TIME_OUT,gameid)
end

function PlayerData:onIdentityStatusCallback(cell, type, data)
    if DEBUG and KW_CLOSE_REAL_NAME then
        return 0
    end
    if type == XH.Req.TYPE.SUCCESS then
         if data.status > 0 then
            self:setProtected(data.status)
         else
            self:setProtected(0)
            if not XH.configerManager:getIsNewRealName() then
                self:flushRealNameAuth()
            end
         end
         if data.inwhitelist then
            self:setProtectedInwhitelist(data.inwhitelist)
         end
         if data.level then
            if data.level == 1 and data.status == 2 and self:getProtectedInwhitelist() == 0 then
                self:setProtectedLevel(data.level)
                if XH.lobby:getModule("Lobby"):getIsForeignIP() or XH.lobby:getModule("IdentityCheck"):isSpecialCode() then
                return
                end
                self:dispatchEvent({name = PlayerData.EVENT_FORCE_SHOW_IDENTITYCHECKVIEW})
            else
                self:setProtectedLevel(0)
            end
         end
    else
        self:setProtected(0)
    end
end

function PlayerData:onGameIdentityStatusCallback(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
         if data.status == 2 then
            self:startReq("ReqDefendIdentity","","", XH.KW_CONFIG_LOGIC_TIME_OUT,data.gameid)
         end
    end
end

function PlayerData:onDefendCallback(cell, type, data)
    -- if type == XH.Req.TYPE.SUCCESS then
    --      if data.status == 2 then
    --         print("")
    --      end
    -- end
end

--实名限制请求
function PlayerData:flushRealNameAuth()
    if not self:isProtected() or not self:isAdult() then
        self:startReq("ReqRealNameAuth", XH.KW_CONFIG_LOGIC_TIME_OUT)
    end
end

function PlayerData:onRealNameAuthCallback(cell, type, data)
    if DEBUG and KW_CLOSE_REAL_NAME then
        return 0
    end
    if type == XH.Req.TYPE.SUCCESS then
        local foreignNum = 0
        if data.nConfigVale then
            local nRealNameAuthConfig = bit:d2b(data.nConfigVale)
            local bindNum = nRealNameAuthConfig[data.BITCONFIG.HAS_BIND]
            foreignNum = nRealNameAuthConfig[data.BITCONFIG.OVERSEA_SHOWPOP]
            self:setConfigVale(data.nConfigVale)
            if bindNum == 1 then   --伪绑定过不用强弹
                XH.lobby:getModule("PersonalCenter"):setFakeIdentify(true)
                return
            end
        end
        if data.nActiveValue ~= 0 then
            self:setActiveValue(data.nActiveValue)
        end
        if data.nPassiveValue and data.nPassiveValue ~= 0 then
            local powerByte = bit:d2b(data.nPassiveValue)
            local PassiveByte = {}
            local ActiveByte = {}
            --64 63 55 56
            for i = 57,64 do
                PassiveByte[i] = powerByte[ i - 8 ]
                ActiveByte[i] = powerByte[ i ]
            end
            --local pValue = bit:b2d(PassiveByte)
            --local aValue = bit:b2d(ActiveByte)
            self:setPassiveValue(bit:b2d(PassiveByte))
            self:setActiveValue(bit:b2d(ActiveByte))
            if XH.lobby:getModule("Lobby"):getIsForeignIP() and foreignNum == 0 or XH.lobby:getModule("IdentityCheck"):isSpecialCode() then
                return
            end
            self:dispatchEvent({name = PlayerData.EVENT_FORCE_SHOW_IDENTITYCHECKVIEW})
        end
    end
end

--请求是否是用户
function PlayerData:flushAgentFlag()
    self:startReq("ReqIsAgent")
end

--请求是否是用户回调
function PlayerData:onIsAgentCallBack(cell, type, data)
    local bIsAgent = false
    if type == XH.Req.TYPE.SUCCESS then
        --if data['open_agent_subauth'] == 1 then
        --    bIsAgent = true
        --else
        --    bIsAgent = false
        --end

        if data['open_teahouse'] == 1 then
            bIsAgent = true
        else
            bIsAgent = false
        end

        if XH.areaData:getLobbyID() == XH.LOBBY_ID.SHAOXING3D then
            if data['is_virtual_agent'] and data['is_virtual_agent'] == 0 then
                bIsAgent = true
            else
                bIsAgent = false
            end
            if data['is_virtual_agent'] == 1 then
                XH.areaData:setVirtualAgentState(true)
            else
                XH.areaData:setVirtualAgentState(false)
            end
        end
    elseif type == XH.Req.TYPE.FAIL then
        bIsAgent = false
    end
    self:setPlayerIsAgent(bIsAgent)
end

function PlayerData:flushBindInvateCode()    
    local tHttpInfo = {}
    tHttpInfo["rid"] = XH.areaData:getApiHubRid()
    tHttpInfo["numid"] = XH.playerData:getNumberID()
    self:startReq("ReqCheckBind", tHttpInfo, 15)
end

function PlayerData:onCheckInvateCodeCallback(cell,type,data)
    if type == XH.Req.TYPE.SUCCESS then
        if data == 0 then   --已绑定
            local jsonData = json.decode(cell._message)
            local tmpData = cjson.decode(jsonData.data)
            self:setInvateCode(tonumber(tmpData["code"]))
            self:setBindDSB(true)
        elseif data == 1060 then
            self:setInvateCode(0)
        end
    end
end

function PlayerData:setInvateCode(code)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_DATA_INVATE_CODE, code)
end

function PlayerData:getInvateCode()
    return XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_DATA_INVATE_CODE, 0)
end

function PlayerData:flushPlayerPhoneBindState()
    self:startReq("ReqPhoneBindingState", XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function PlayerData:onPhoneStateCallback(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
         if data.info then
            self:setPlayerPhone(tostring(data.info.phone))
         else
            self:setPlayerPhone("")
         end
    else
        self:setPlayerPhone("")
    end

    XH.lobby:getModule("TabsActivity"):reqPhoneBindActPeoInfo()
end

function PlayerData:reqChangePlayerInfo(sex,name,headurl,isReq,isBindPhone)
    self:startReq("ReqChangePlayerInfo",sex,name,headurl,isReq,isBindPhone,XH.areaData:getSrsGroupID(),XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function PlayerData:onChangeInfoCallBack(transmit,type,data)
    local isSuccess = (type == XH.Req.TYPE.SUCCESS)
    if isSuccess then
        if data.sex then
            self:setSex(data.sex)
        end
        if data.nickname then
            self:setNickName(data.nickname)
            self:setNickName2(data.nickname)
        end
        if data.headurl then
            self:setWeChatURL(data.headurl)
        end
    end
    self:dispatchEvent({name = PlayerData.EVENT_PLAYER_INFO_CHANGED, msg = { isSuccess = isSuccess, data = data}})
end

function PlayerData:getFreeRoomCard()
    if XH.areaData:isRemodelArea() then
        local lobbyid = XH.userDefault:getValue(XH.userDefault.KEY_ID.LOBBYID, 0)
        if areaConfig[lobbyid] then
            local propList = areaConfig[lobbyid].propList
            if propList then
                if self._props[propList.freeRoomCardID] and XH.areaData:getPropFreeRoomCardRatio() then
                    local tmpRoomCardCount = tonumber(self._props[propList.freeRoomCardID]) / XH.areaData:getPropFreeRoomCardRatio()
                    return tmpRoomCardCount
                end
            end
        end
    end
    return 0
end

--转型后的大厅总房卡显示
function PlayerData:getRemodelRoomCard()
    local tmpRoomCardCount = self:getRoomCard()
    if XH.areaData:isRemodelArea() then
        tmpRoomCardCount = tmpRoomCardCount + self:getFreeRoomCard()
    end
    return tmpRoomCardCount
end

--记录下当前玩家是否可见新手签到活动
function PlayerData:setIsDisplayNewUser(isDisplay)
    self._isDisplayNewUser = isDisplay or false
end

function PlayerData:getIsDisplayNewUser()
    return self._isDisplayNewUser or false
end

--是否处于新用户引导的状态
function PlayerData:setIsNewUserGuide(isNewUserGuide)
    self._isNewUserGuide = isNewUserGuide or false
end

function PlayerData:getIsNewUserGuide()
    return self._isNewUserGuide or false
end

--新手引导第一次进入金币场
function PlayerData:setIsFirstGoldGuide(isGoldFirstGuide)
    self._isFirstGoldGuide = isGoldFirstGuide or false
end

function PlayerData:getIsFirstGoldGuide()
    return self._isFirstGoldGuide or false
end

--是否已是第一局新手引导
function PlayerData:setIsFirstGoldGame(isFirstGoldGame)
    self._isFirstGoldGame = isFirstGoldGame or false
end

function PlayerData:getIsFirstGoldGame()
    return self._isFirstGoldGame or false
end

--是否屏蔽破产弹窗,新手引导期间会用到
function PlayerData:getIsShieldBaseLiveView()
    return self._isShieldBaseLive or false
end

function PlayerData:setIsShieldBaseLiveView(isShieldBaseLive)
    self._isShieldBaseLive = isShieldBaseLive
end

function PlayerData:getIsBoxRoomGuide()
    return self._isBoxRoomGuide or false
end

function PlayerData:setIsBoxRoomGuide(isGuide)
    self._isBoxRoomGuide = isGuide
end

--是否自动加入过H5游戏
function PlayerData:getIsAutoJoinedH5Game()
    return self._isAutoJoinH5Game or false
end

function PlayerData:setIsAutoJoinedH5Game(isAuto)
    self._isAutoJoinH5Game = isAuto
end

function PlayerData:getIsRealNameTourist()
    return XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_RN_TOURIST_LOGIN)
end

function PlayerData:setPlayerIsAgent(bIsAgent)
    self._isAgent = bIsAgent
    self:dispatchEvent({name = PlayerData.EVENT_AGENT_FLAG_CHANGED})
end

function PlayerData:getPlayerIsAgent()
    return self._isAgent
end

function PlayerData:checkNewRealName(gameid)
    if DEBUG and KW_CLOSE_REAL_NAME then
        return 0
    end
    if XH.lobby:getModule("IdentityCheck"):isSpecialCode() then 
        return 0
    end
    if not XH.lobby:getModule("Lobby"):getIsForeignIP() then
        if XH.configerManager:getIsNewRealName() then --新实名认证
            if self._bIdentityInwhitelist == 0 then --未在白名单中
                if self._bIdentityStatus == 2 then --未认证
                    self:dispatchEvent({name =    PlayerData.EVENT_FORCE_SHOW_IDENTITYCHECKVIEW_NEW})
                    cc.UserDefault:getInstance():setBoolForKey(string.format("KW_REALNAME_CHECK_ING_%s",self:getNumberID()),false)
                    return 2
                elseif self._bIdentityStatus == 1 then --认证中
                    if not cc.UserDefault:getInstance():getBoolForKey(string.format("KW_REALNAME_CHECK_ING_%s",self:getNumberID()),false) then
                        self:dispatchEvent({name = PlayerData.EVENT_FORCE_SHOW_IDENTITYCHECKVIEW_NEW_ING})
                        return 1
                    end
                    return 0
                elseif self._bIdentityStatus == 0 and gameid then -- 静默提交认证
                    self:flushPlayerGameDefendIdentity(gameid)
                    return 0
                end
            end
        end
    end
 
    return 0
end

-- 请求玩家游客状态
function PlayerData:reqVisitorHeart()
    self:startReq("ReqVisitorHeart", XH.KW_CONFIG_LOGIC_TIME_OUT,true)
    if self._heartBeatScheduleIDs == nil then
        self._heartBeatScheduleIDs = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
            self:startReq("ReqVisitorHeart", XH.KW_CONFIG_LOGIC_TIME_OUT,false)
        end, 60, false)
    end
end

-- 请求玩家游客状态
function PlayerData:stopVisitorHeart()
    if self._heartBeatScheduleIDs ~= nil then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._heartBeatScheduleIDs)
        self._heartBeatScheduleIDs = nil
    end
end

function PlayerData:onReqVisitorHeartCallback(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data.nonlinetime/60 > 40 then
            cc.UserDefault:getInstance():setBoolForKey("KW_RN_TOURIST_LOGIN_GAME_OVER",true)
            cc.UserDefault:getInstance():setIntegerForKey("KW_RN_TOURIST_LOGIN_GAME_OVER_TIMNER",data.nnextplaytime)
        end
        local currScene = XH.SceneManager:getInstance():getTopScene()
        if currScene and (currScene:getTag() == XH.SCENE_TAG.GAME or currScene:getTag() == XH.SCENE_TAG.TEAHOUSE) then
            print("onReqVisitorHeartCallback")
        else
            if data.nonlinetime/60 > 40 then
                self:dispatchEvent({name = PlayerData.EVENT_REALNAME_TIMEOVER,data = data.nnextplaytime})
                self:stopVisitorHeart()
            end
        end
        
    end
end

function PlayerData:checkNewRealNameVisitor()
    if DEBUG and KW_CLOSE_REAL_NAME then
        return 0
    end
    if not XH.lobby:getModule("Lobby"):getIsForeignIP() then
        if self.getIsRealNameTourist() then --新实名认证
            self:dispatchEvent({name = PlayerData.EVENT_REALNAME_TIMEOVER_LIMIT})
            return 1
        end
    end
    return 0
end

function PlayerData:setTeaId(teaid)
    self._teaId = teaid or 0
end

function PlayerData:getTeaId()
    return self._teaId or 0
end

function PlayerData:screenGameList(gameListData,type)
    if (not self:getNeedHideComGames()) then
        return
    end
    if type == 0 or type == 2 then
        if self:getNeedHideComGames() then
            if #XH.areaData:getBoxActivityGame() > 0 then
                for _,v in ipairs(XH.areaData:getBoxActivityGame()) do
                    for i=#gameListData,1,-1 do
                        if type == 0 then
                            if v == gameListData[i].gameID and not table.has(self._whiteListGames, tostring(v)) then
                                table.remove(gameListData, i)
                            end
                        elseif type == 2 then
                            if v == gameListData[i] and not table.has(self._whiteListGames, tostring(v)) then
                                table.remove(gameListData, i)
                            end
                        end
                    end
                end
            end
        end
    elseif type == 1 then
        local bHideGoldGames = self._isUseNewHideGameReq and self:getNeedHideComGoldGames() or self:getNeedHideComGames()
        if bHideGoldGames then
            if #XH.areaData:getGoldActivityGame() > 0 then
                for _,v in ipairs(XH.areaData:getGoldActivityGame()) do
                    for i=#gameListData,1,-1 do
                        if gameListData[i].GameID == v and not table.has(self._whiteListGames, tostring(v)) then
                            table.remove(gameListData, i)
                        end
                    end
                end
            end
        end
    end
end

function PlayerData:screenLobbyGoldGameList(gameListData)
    local bHideGoldGames = self._isUseNewHideGameReq and self:getNeedHideComGoldGames() or self:getNeedHideComGames()
    if bHideGoldGames then
        if #XH.areaData:getGoldActivityGame() > 0 then
            for _,v in ipairs(XH.areaData:getGoldActivityGame()) do
                for m,n in ipairs(gameListData) do
                    local roomConf = XH.goldConfigManager:getGoldRoomInfoByIndex(n).roomInfo
                    if v == roomConf.GameID then
                        table.remove(gameListData,m)
                    end
                end
            end
        end
    end
    return gameListData
end

function PlayerData:setWhiteListGames(gamesStr)
    gamesStr = gamesStr or ""
    local games = string.split(gamesStr, ",")
    games = games or {}
    self._whiteListGames = games
end

function PlayerData:setClubWhiteListGames(gamesStr)
    gamesStr = gamesStr or ""
    local games = string.split(gamesStr, ",")
    games = games or {}
    self._clubWhiteListGames = games
end

function PlayerData:screenTeaHouseGame(gameListData)
    if (not self:getNeedHideClubComGames()) then
        return
    end
    if self:getNeedHideClubComGames() then
        if #XH.areaData:getBoxActivityGame() > 0 then
            for _,v in ipairs(XH.areaData:getBoxActivityGame()) do
                for i=#gameListData,1,-1 do
                    if gameListData[i] == v and not table.has(self._clubWhiteListGames, tostring(v)) then
                        table.remove(gameListData, i)
                    end
                end
            end
        end
    end
end

-- 仅本次允许权限录音
function PlayerData:getVoicePermissionOnlyOnce()
    return self._voicePermission
end

function PlayerData:setVoicePermissionOnlyOnce(perm)
    self._voicePermission = perm
end

-- 仅本次允许权限位置
function PlayerData:getPositionPermissionOnlyOnce()
    return self._positionPermission
end

function PlayerData:setPositionPermissionOnlyOnce(perm)
    self._positionPermission = perm
end

--设置比赛场问卷系统(我要吐槽)数据
function PlayerData:setComplainData(info)
    self._complainData = info
end

function PlayerData:getComplainData()
    return self._complainData 
end

function PlayerData:setReqComplainData(IsReq)
    self._isreqComplainData = IsReq
end

function PlayerData:getReqComplainData()
    return self._isreqComplainData
end

--请求是否是注销中的用户
function PlayerData:reqWriteOffStatus()
    local lastReqTime = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_WRITE_OFF_REQ_TIME, 0)
    local writeOffStatus = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_WRITE_OFF_STATUS, false)
    if lastReqTime == 0 or (os.time() - lastReqTime > 6 * 24 * 60 * 60) or 
        (lastReqTime > 0 and not writeOffStatus) then
        self:startReq("ReqPlayerWriteOffStatus", XH.KW_CONFIG_LOGIC_TIME_OUT)
    else
        XH.SysTool.performDelayOnce(function()
            self:showWriteOffDialog()
        end, 0.5)
    end
end

function PlayerData:showWriteOffDialog()
    XH.TipTool.showLogOffTip( {
        type = XH.TIP_LAYER_TYPE.OK_CANCEL_NO_CLOSE,
        bTop = true,
        funcOK = function()
            self:startReq("ReqCancelWriteOff", XH.KW_CONFIG_LOGIC_TIME_OUT)
        end
    } , XH.ConstString.getStr("WRITE_OFF_ING"))
end

local KW_WRITE_OFF_STATUS = {
    NOT_APPLY = 0,
    APPLY_ING = 1,
    CANCLE_APPLY = 2,
    SUCCESS_APPLY = 3
}
function PlayerData:onPlayerWriteOffStatusCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_WRITE_OFF_REQ_TIME, os.time())
        if data.status and data.status == KW_WRITE_OFF_STATUS.APPLY_ING then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_WRITE_OFF_STATUS, true)
        else
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_WRITE_OFF_STATUS, false)
        end
        if data.new_apply then
            self:dispatchEvent({name = PlayerData.EVENT_NEW_APPLY_WRITE_OFF, bNewApply = data.new_apply})
        else
            if data.status and data.status == KW_WRITE_OFF_STATUS.APPLY_ING then
                XH.TipTool.showTip( {
                    type = XH.TIP_LAYER_TYPE.OK_CANCEL_NO_CLOSE,
                    bTop = true,
                    funcOK = function()
                        self:startReq("ReqCancelWriteOff", XH.KW_CONFIG_LOGIC_TIME_OUT)
                    end
                } , XH.ConstString.getStr("WRITE_OFF_ING"))
            end
        end
    else
        self:dispatchEvent({name = PlayerData.EVENT_NEW_APPLY_WRITE_OFF})
    end
end

function PlayerData:onCancelWriteOffCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_WRITE_OFF_STATUS, false)
        XH.TipTool.showToast("撤销成功")
    end
end

function PlayerData:setTeaCreateGroup(bIsGroup)
    self._bIsCreateTeaGroup = bIsGroup
end

function PlayerData:isTeaCreateGroup()
    return self._bIsCreateTeaGroup or false
end

function PlayerData:changeBindCardRate(propid,cnt)
    local lobbyid = XH.userDefault:getValue(XH.userDefault.KEY_ID.LOBBYID, 0)
    local proplist = areaConfig[lobbyid].propList
    if proplist then
        local freeCardId = proplist.freeRoomCardID
        if freeCardId and tonumber(propid) == freeCardId then
            cnt = math.floor(cnt / XH.areaData:getPropFreeRoomCardRatio())
        end
    end
    return cnt
end

local SpecialBYID = {523337,1081193339,1079879549,1080573405,222068882,
59733500,1080817735,1081897927,221955922,1080574190,
60718923,40799457,1081092338,1081046850,162802,
1059582,1081483239,162010,4672422,58159435,221209121,
33057516,1080122724,1081507210,1080988114,68590811,
1080199306,1080894314,1080929955,1081975015,221929154,
1081039287,221026326,1080988585,1079821682,62671787,
1079868711,40725146,1082026411,351682,68595427,
1082027354,1081531952
}
function PlayerData:isSpecialBYID()
    for _, nid in pairs(SpecialBYID) do
        if nid == self:getNumberID() then
            return true
        end
    end
    return false
end

function PlayerData:setIsRechargePlayer(isRechargePlayer)
    self._isRechargePlayer = isRechargePlayer
    self:dispatchEvent({name = PlayerData.EVENT_PLAYER_RECHARGE_FLAG})
end

function PlayerData:isRechargePlayer()
    return self._isRechargePlayer
end

function PlayerData:getFreezePropCountByID(propID)
    local lobbyId = XH.userDefault:getValue(XH.userDefault.KEY_ID.LOBBYID,0)
    local strKeyDiamond = string.format("KW_MIN_DIAMOND_%d_%d", lobbyId, self._nNumID)
    local strKeyRoomCard = string.format("KW_MIN_ROOMCARD_%d_%d", lobbyId, self._nNumID)
    local strKeyFreeRoomCard = string.format("KW_MIN_FREE_ROOMCARD_%d_%d", lobbyId, self._nNumID)
    if propID == XH.areaData:getPropDiamndID() then
        local freezeDiamond = cc.UserDefault:getInstance():getStringForKey(strKeyDiamond, "0") or 0
        return tonumber(freezeDiamond)
    elseif propID == XH.areaData:getPropRoomCardID() then
        local freezeRoomCard = cc.UserDefault:getInstance():getStringForKey(strKeyRoomCard, "0") or 0
        return tonumber(freezeRoomCard)
    elseif propID == XH.areaData:getPropBindRoomCardID() then
        local freezeFreeRoomCard = cc.UserDefault:getInstance():getStringForKey(strKeyFreeRoomCard, "0") or 0
        return tonumber(freezeFreeRoomCard)
    end
    return 0
end

function PlayerData:setFreeRoomCard(nRoomCard)
    local lobbyid = XH.userDefault:getValue(XH.userDefault.KEY_ID.LOBBYID,0)
    if areaConfig[lobbyid] then
        local propList = areaConfig[lobbyid].propList
        if propList then
            self._props[propList.freeRoomCardID] = nRoomCard
        end
    end
end

-- 设置过期时间
function PlayerData:setSxvipRemainder(time)
    self._sxvip.remainder = time or 0
end

function PlayerData:getSxvipRemainder()
    return self._sxvip.remainder
end

-- 获取服务位置信息
function PlayerData:setServerLocation(longitude, latitude, city)
    self._longitude = longitude
    self._latitude = latitude
    self._city = city
    if latitude and longitude then
        print("yuhang set gps success" .. latitude .. "|" .. longitude)
    end
end

function PlayerData:getServerLocation()
    return self._longitude, self._latitude, self._city or ""
end

return PlayerData
   ��  