local UrlConf = require("app.Config.UrlConf")
local TaskDefine = require("app.Define.TaskDefine")
local LobbyModule = class("LobbyModule", XH.ModuleBase)
local LobbyConfig = require("lobby.Modules.Lobby.Config")
local NewGoldSceneDefine = require("lobby.Modules.NewGoldScene.Define")
--玩家数据更新消息
LobbyModule.EVENT_GET_MY_SUGGEST = "EVENT_GET_MY_SUGGEST"
LobbyModule.EVENT_GET_IS_FOREIGN_IP = "EVENT_GET_IS_FOREIGN_IP"
LobbyModule.SHOW_XIUXIAN_GAME = "SHOW_XIUXIAN_GAME"
LobbyModule.SHOW_BOX_GAME = "SHOW_BOX_GAME"
LobbyModule.SHOW_GOLD_GAME = "SHOW_GOLD_GAME"
LobbyModule.EVENT_SHOW_DETAIL_LAYER = "EVENT_SHOW_DETAIL_LAYER"
LobbyModule.EVENT_SHOW_LEGEND_SETVISIBLE = "EVENT_SHOW_LEGEND_SETVISIBLE"
LobbyModule.EVENT_OPEN_IM_VIEW = "EVENT_OPEN_IM_VIEW"
LobbyModule.SHOW_START_H5ROOM = "SHOW_START_H5ROOM"
LobbyModule.SHOW_LOBBY_ICONS = "SHOW_LOBBY_ICONS"
LobbyModule.SHOW_LEISURE_ENTRANCE = "SHOW_LEISURE_ENTRANCE"
LobbyModule.EVENT_GET_ICON_WORLD_POS = "EVENT_GET_ICON_WORLD_POS"
LobbyModule.EVENT_SHOW_RED_POS = "EVENT_SHOW_RED_POS"

LobbyModule.KW_QUICK_ENTRANCE_TYPE = {
    RELAXED = "relaxed",
    JOINT = "joint"
}

-- 安卓H5
LobbyModule.H5GameNameList = {
    LEGEND = "LEGEND",
    BUYU = "BUYU",
    KNIFELEGEND = "KNIFELEGEND",
    BUYUDAJIANGSAI = "BUYUDAJIANGSAI",
    DATIANSHIZHIJIAN = "DATIANSHIZHIJIAN",
    LINGMENGZHIBO = "LINGMENGZHIBO",
    REXUEFENGSHEN = "REXUEFENGSHEN",
    DOULUODALU = "DOULUODALU",
    JIEJISANGUO = "JIEJISANGUO",
    RENRENBUYU = "RENRENBUYU",
    SHEISHISHOUFU = "SHEISHISHOUFU",
    WEIJINGCHUANQI = "WEIJINGCHUANQI", -- 维京传奇
    ZHUOYUECHUANSHUO = "ZHUOYUECHUANSHUO", -- 卓越传说
    YUANZHENGJIANGSHI = "YUANZHENGJIANGSHI", -- 远征将士
    RONGYAOZHINU = "RONGYAOZHINU", -- 荣耀之怒
    BAIZHANSHACHENG = "BAIZHANSHACHENG", -- 百战沙城
    HUANXIANGMINGJIANGLU = "HUANXIANGMINGJIANGLU"
}

function LobbyModule:ctor()
    LobbyModule.super.ctor(self)
    
	self.hotUpdatePriority = 1
    self._dealOpenUrl = false
    self._isOpenGoldView = false
    self._lobbyIcons = { }
    self._canJoin = true
end

function LobbyModule:getProxyEvents()
    return {
        { module = XH.sdkManager, eventKeyName = "EVENT_XHLINK_CALLBACK", callBack = "onXhLinkCallBack" },
        { module = XH.sdkManager, eventKeyName = "EVENT_DEFAULT_CALLBACK", callBack = "onDefaultCallBack" },
        { module = XH.sdkManager, eventKeyName = "EVENT_TOPONSDK_CALLBACK", callBack = "onLoadToponAdsCallBack" },
        { module = XH.taskManager, eventKeyName = "LOGIN_GUIDE_SHOW", callBack = "onGuideEvent" },
        { module = XH.playerData, eventKeyName = "EVENT_FORCE_SHOW_IDENTITYCHECKVIEW", callBack = "onShowIdentityCheckView" },
        { module = XH.playerData, eventKeyName = "EVENT_FORCE_SHOW_IDENTITYCHECKVIEW_NEW", callBack = "onShowIdentityCheckViewNew" },
        { module = XH.playerData, eventKeyName = "EVENT_FORCE_SHOW_IDENTITYCHECKVIEW_NEW_ING", callBack = "onShowIdentityCheckViewNewIng" },
        { module = XH.login:getModule("Login"), eventKeyName = "LOGIN_EVENT_SUCCESS", callBack = "onLoginSuccessCallBack" },
        { module = XH.playerData, eventKeyName = "EVENT_REALNAME_TIMEOVER", callBack = "onRealNameTimeOverCallBack" },
        { module = XH.playerData, eventKeyName = "EVENT_REALNAME_TIMEOVER_LIMIT", callBack = "onRealNameTimeOverLimitCallBack" },
        { module = XH.playerData, eventKeyName = "EVENT_IDENTITY_GETBACK", callBack = "onIdentityBack" },
        { module = XH.netEngine, eventName = XH.MatchLinkProtocol.RespStopSvrMsg.event_key, callBack = "onRespStopSvrMsg"},
    }
end

function LobbyModule:getReqConfig()
    return {
        ReqGetRealIP = { reqPath = "lobby.Req.ThreeLevelDistribution.reqGetRealIp", callBack = self.onGetRealIP },
        ReqIsForeignIP = { reqPath = "lobby.Req.ThreeLevelDistribution.reqGetIpInfo", callBack = self.onIsForeignIPCallback },
        ReqGetDynamicUrl = { reqPath = "lobby.Req.DynamicUrl.ReqDynamicUrl", callBack = self.onGetDynamicUrlCallBack },
        ReqIsLegendSet = { reqPath = "app.Req.Task.ReqTaskPercent", callBack = self.onReqIsLegendSetCallBack },
        ReqWebIcons = {reqPath = "app.Req.ReqWebIcons", callBack = self.onReqWebIconsCallBack},
        ReqLeisureEntrance = {reqPath = "app.Req.ReqLeisureEntrance", callBack = self.onReqLeisureEntranceCallBack},
        ReqTeaAutoAwardInfo = {reqPath = "lobby.Req.TeaHouse.ReqRechargeAutoAwardInfo", callBack = self.onReqTeaAutoAwardInfoCallBack},
        ReqGetBanInfo = {reqPath = "lobby.Req.LockTips.ReqGetBanInfo", callBack = self.onReqGetBanInfoCallBack},
        ReqRechargeFenqun = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onReqRechargeFenqun },
        ReqLocation = { reqPath = "lobby.Req.Room.ReqLocation50", callBack = self.onReqLocation },
    }
end

function LobbyModule:onLoginSuccessCallBack(event)
    --请求合规http  4.30之后注册的玩家才判断
    XH.lobby:getModule('Legitimacy'):reqIsUserHideGame()
    XH.sdkManager:reqSysfunGpsSdk()

	self._getSchemaPragmascheduleID = self._getSchemaPragmascheduleID or 0
    if self._getSchemaPragmascheduleID ~= 0 then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._getSchemaPragmascheduleID)  
        self._getSchemaPragmascheduleID = 0
    end
    self._getSchemaPragmascheduleID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(handler(self,self.schemaPragmascheduleCallBack),0.5,false)

    -- 请求休闲场入口
    self:reqLeisureEntrance()

    --每天首次登陆请求一次
    if self:checkCanGetRealIp() then
        self:sendGetRealIP()
        self:setCanGetRealIp()
    end

    --获取风信后台动态配置分享链接
    self:reqGetDynamicUrl()
    self:downloadMahFace()
    self:reqUserTag()
    --请求H5 游戏 微信注册的UserID接口
    if device.platform == "android" then
        self:reqH5WeCahtUserID()
    end
    local target = cc.Application:getInstance():getTargetPlatform()
    if target == cc.PLATFORM_OS_IPHONE or target == cc.PLATFORM_OS_IPAD then
        local numid = XH.playerData:getNumberID() or 0
        if numid > 0 then
            XH.sdkManager:xhCallOCFunc("XH_IAP", "xhIapinit:", {numid})

            -- 个推版本大于等于1.0.32才支持别名设置
            local version = XH.SysTool:GetBundleVersion()
            if version >= "1.0.32" then
                XH.sdkManager:xhCallOCFunc("XHGeTui", "xhGeTuiBindAlias:", numid)
            end
        end
    end
    self:reqTeaAutoAwardInfo()
    --临时的客服系统账号清空
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_CUSSERVICE_TEMP_NUMID_DATA, "")
    self:LobbyKeypadEnabled()--安卓物理返回键提示框
    self:reqCusServiceUnreadMsg()
    self:reqRecomendInvitation()
    self:reqRechargeFenqun()
    self:initModuleData()
    XH.bugly.buglyLog("loginSuccess", "loginSuccess")
end

function LobbyModule:onReqRechargeFenqun(cell, ctype, data)
    if data == nil or data == "" or data.acData == "" then
        return
    end
    if ctype == XH.Req.TYPE.SUCCESS then
        local actData = cjson.decode(data.acData)
        if actData and tonumber(actData) == 1 then
            XH.playerData:setIsRechargePlayer(true)
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PLAYER_RECHARGE_FLAG, true)
        end
    end
end

function LobbyModule:reqRechargeFenqun()
    if XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_PLAYER_RECHARGE_FLAG, false) then
        XH.playerData:setIsRechargePlayer(true)
        return
    end
    self:startReq("ReqRechargeFenqun", TaskDefine.TASK_ID.TASK_ID_RECHARGE_FLAG_FENQUN, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, "")
end

-- 初始化求财运引导数据
function LobbyModule:initModuleData()
    XH.isNeedInitPrayGuideData = true
    XH.isNeedInitShuffleData = true
end

function LobbyModule:LobbyKeypadEnabled()
    if device.platform == "android" or device.platform == "windows" then
        local runningScene = XH.SceneManager:getInstance():getTopScene()
        runningScene:setKeypadEnabled(true)
        runningScene:addNodeEventListener(cc.KEYPAD_EVENT, function (event)
            if event.key == "back" then
                runningScene:setKeypadEnabled(false)
                local agreeFunc = function()
				    cc.Director:getInstance():endToLua()
			    end
                local funcCancelFunc = function()
				    runningScene:setKeypadEnabled(true)
                end      

                local Text = "是否确认退出【浙江游戏大厅】，不留下再玩一会么？"
                if XH.ChannelTool.checkIsWaishengChannel() then 
                    Text = "是否确认退出【游戏大厅】，不留下再玩一会么？"
                end

			    XH.TipTool.showTip({
				    type = XH.TIP_LAYER_TYPE.OK_CANCEL,
				    funcOK = agreeFunc,
                    funcCancel = funcCancelFunc,
                    funcClose = funcCancelFunc
			    }, Text)
            end
        end)
    end
end

--请求H5 游戏 微信注册的UserID接口
function LobbyModule:reqH5WeCahtUserID()
    local areaID = XH.areaData:getAreaID()
    local firstChargeConf = require("lobby.Modules.Lobby.H5WeChatUserIDConfig")
    local conf = firstChargeConf.H5WeChatUserIDConfigByConfID[areaID]
    local h5IdConf = firstChargeConf.H5WeChatAllUserIDConfigByConfID[areaID]
    -- 先初始化三个数据 防止数据残留导致bug
    XH.playerData:setLegendUserID()
    XH.playerData:setBuYuUserID()
    XH.playerData:setIsNeedConfigID()
    XH.playerData:setWeChatBuYuUserID()
    if conf then
        local reqH5WeCahtUserID = require("app.Req.H5Game.ReqH5WeCahtUserID")
        local tReqH5WeCahtUserID = reqH5WeCahtUserID:new()
        tReqH5WeCahtUserID:addReqCallBack(self, self.respH5WeCahtUserID)
        tReqH5WeCahtUserID:start(conf.AID, conf.Secret, conf.AppID, conf.WindID, XH.KW_CONFIG_LOGIC_TIME_OUT)
    end
    if h5IdConf then
        local ReqH5WeChatAllUserID = require("app.Req.H5Game.ReqH5WeChatAllUserID")
        local tReqH5WeCahtAllUserID = ReqH5WeChatAllUserID:new()
        tReqH5WeCahtAllUserID:addReqCallBack(self, self.respH5WeChatAllUserID)
        tReqH5WeCahtAllUserID:start(h5IdConf.AID, h5IdConf.Secret, h5IdConf.AppID, h5IdConf.WindID, XH.KW_CONFIG_LOGIC_TIME_OUT)
    end
end

function LobbyModule:respH5WeCahtUserID(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        XH.playerData:setLegendUserID(data.user_id)
        XH.playerData:setBuYuUserID(data.fishing_user_id)
        XH.playerData:setIsNeedConfigID(data.is_need_config_id)
    end
end

function LobbyModule:respH5WeChatAllUserID(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        XH.playerData:setWeChatBuYuUserID(data.fishing_user_id)
    end
end

function LobbyModule:showXiuXianGame()
    self:dispatchEvent({name = LobbyModule.SHOW_XIUXIAN_GAME})
end

function LobbyModule:showH5room()
    self:dispatchEvent({name = LobbyModule.SHOW_START_H5ROOM})
end 

function LobbyModule:showBoxGame()
    self:dispatchEvent({name = LobbyModule.SHOW_BOX_GAME})
end

--下载牌花
function LobbyModule:downloadMahFace()
    if not XH.login:getModule("Login") then return end
    local priority = 1
    local MahFaceResChecker = require("app.hotupdate.mahface.MahFaceResChecker") 
    MahFaceResChecker._startHotFix(nil, nil, priority)
end

--是否请求过真实ip以及获取是否国外网络
function LobbyModule:checkCanGetRealIp()
    local result = true
    local lastValue = XH.userDefault:getValue(XH.userDefault.KEY_ID.GET_REAL_IP_KEY, "")
    if lastValue == self:getTodayTimeStr() then
        result = false
    end
    return result
end

function LobbyModule:setCanGetRealIp()
    XH.userDefault:setValue(XH.userDefault.KEY_ID.GET_REAL_IP_KEY,self:getTodayTimeStr())
end

--获得我的真实IP
function LobbyModule:sendGetRealIP()    
    local tHttpInfo = {}
    tHttpInfo["version"] = 1
    self:startReq("ReqGetRealIP", tHttpInfo,15)
end

function LobbyModule:onGetRealIP(cell,type,data)
    if type == XH.Req.TYPE.SUCCESS then
       self._realIp = data.ip
       if self._realIp then
            self:sendIsForeignIP(self._realIp)
       end
    end
end

--请求是否是国外网络
function LobbyModule:sendIsForeignIP(ip)    
    local tHttpInfo = {}
    tHttpInfo["ip"] = ip
    self:startReq("ReqIsForeignIP", tHttpInfo, 15)
end

function LobbyModule:onIsForeignIPCallback(cell,type,data)
    if type == XH.Req.TYPE.SUCCESS then
        XH.userDefault:setValue(XH.userDefault.KEY_ID.IS_FOREIGN_IP_KEY, data)
        self:dispatchEvent( { name = LobbyModule.EVENT_GET_IS_FOREIGN_IP })
    end
end

--是否是国外网络
function LobbyModule:getIsForeignIP()   
    return XH.userDefault:getValue(XH.userDefault.KEY_ID.IS_FOREIGN_IP_KEY, false)
end

function LobbyModule:schemaPragmascheduleCallBack()
    local runningScene = display.getRunningScene()
    if XH.SceneManager:getInstance():getTopScene() ~= runningScene then
        return
    end

	self:dealSchemaPragma()
end

--临时分享埋点逻辑判断
function LobbyModule:checkSendShareAddressMonitorPoint()
    local result = true
    local lastValue = XH.userDefault:getValue(XH.userDefault.KEY_ID.TEMP_SHARE_DOWNLOAD_ADDRESS, "")
    if lastValue == self:getTodayTimeStr() then
        result = false
    end
    return result
end

--设置临时分享埋点key
function LobbyModule:setSendShareAddressMonitorPoint()
    XH.userDefault:setValue(XH.userDefault.KEY_ID.TEMP_SHARE_DOWNLOAD_ADDRESS,self:getTodayTimeStr())
end


--活动强弹逻辑判断
function LobbyModule:checkAutoOpenActivity()
    local result = true
    local lastValue = XH.userDefault:getValue(XH.userDefault.KEY_ID.USER_DEFAULT_AUTO_ACTIVITY, "")
    if lastValue == self:getTodayTimeStr() then
        result = false
    end
    return result
end

function LobbyModule:checkOpenActivity()
    if self:checkAutoOpenActivity() then
        self:setkAutoOpenActivity()
        XH.SysTool.performDelayOnce(
            function()
            if XH.ChannelTool.checkIsWaishengChannel() then 
                XH.TipTool.showToast("欢迎进入 全国 区",3)
            else
                --增加每天首次进入大厅时候的区域提示
                local areaString = XH.areaData:getAreaName()
                XH.TipTool.showToast("欢迎进入 "..areaString .." 大区",3)
            end 
        end
        ,0.3)
    end
end

--设置活动强弹key
function LobbyModule:setkAutoOpenActivity()
    XH.userDefault:setValue(XH.userDefault.KEY_ID.USER_DEFAULT_AUTO_ACTIVITY,self:getTodayTimeStr())
end

function LobbyModule:getTodayTimeStr()
    local expirydate = os.time()
    return os.date("%Y",expirydate).."-"..os.date("%m",expirydate).."-"..os.date("%d",expirydate)
end

---------------------------------------------------------
--@brief dealSchemaPragma 处理链接调起的参数
--封装的目的是，为了后面读取链接带回的参数，做扩展性
--只考虑ios平台，安卓使用剪切板回调
---------------------------------------------------------
function LobbyModule:dealSchemaPragma()
	if device.platform == "android" then
        if XH.SysTool:GetBundleVersion() <= "1.0.4" then
            self:reqClipboard()
        end
    end
end

function LobbyModule:dealSchemaPragmaBoxRoom(strBoxRoomID)
    local teaHouseID, teaHouseTable = self:spiltTeaHouseInfo(strBoxRoomID)
    if teaHouseID then
        XH.teaHouseManager:joinTeaHouse(teaHouseID, teaHouseTable)
    elseif "" ~= strBoxRoomID and strBoxRoomID ~= nil then
        local boxRoomIdStr = string.match(strBoxRoomID, "%d+") or ""
        local _start, _end = string.find(boxRoomIdStr, "%d+", 0)
        if _start and _end then
            local boxid = string.sub(boxRoomIdStr, _start, _end)
            if boxid and #boxid == 6 then
                local numid = XH.playerData:getNumberID()
                if boxid == numid then
                    return
                end
                if not self._canJoin then
                    return
                end
                self:joinLimit()
                XH.lobby:getModule("JoinBoxRoom"):reqJoinBoxRoom(boxid, 2)
            end
        end
    else
        self:reqClipboard()
    end
end

function LobbyModule:joinLimit()
    self._canJoin = false
    XH.SysTool.performDelayOnce(function()
        self._canJoin = true
    end, 0.5)
end

function LobbyModule:spiltTeaHouseInfo(keyword)
    if keyword then
        local strTable = XH.StringTool.splitWithTrim(keyword, '_')
        if strTable and #strTable == 4 then
            if strTable[1] == "house" and strTable[3] == "table" then
                local house = tonumber(strTable[2])
                local table = tonumber(strTable[4])
                return house,table
            end
        end
        if strTable  and #strTable == 2 then
            if strTable[1] == "house" then
                local house = tonumber(strTable[2])
                return house,nil
            end
        end
    end
end

function LobbyModule:onXhLinkCallBack(event)
    local code = event.data.code
    local msg = event.data.msg
    if code == XH.ThirdDefine.XHLINK_CALLBACK_CODE.XHLINK_SUCCESS then
        local dataTale = string.split(msg,"=")
        if dataTale and dataTale[2] then
            if dataTale[1] == "bargain_param" then
                if un.Encryption then
                    local result = XH.EncryptTool.aesEncrypt(dataTale[2], false)
                    local _, _,numid = string.find(result, "numid:(%d+)-areaid:(%d+)")
                    if numid ~= nil then
                        self._dealOpenUrl = true
                    end
                else
                    XH.TipTool.showTip({
                        type = XH.TIP_LAYER_TYPE.OK,
                    }, "当前版本无法助力，请下载新的包体")
                end
            else
                self:dealSchemaPragmaBoxRoom(dataTale[2])
            end
        end
    elseif code == XH.ThirdDefine.XHLINK_CALLBACK_CODE.XHLINK_FAIL then
        self:reqClipboard()
    end
end

function LobbyModule:onDefaultCallBack(event)
    local code = event.data.code
    local msg = event.data.msg
    if code == XH.ThirdDefine.SYSFUNC_CALLBACK.SYSFUNC_GET_CLIPBOARDCONTENT_SUCCESS then
		self:onReqClipboardCallback(msg)
    end
end

function LobbyModule:reqClipboard()
    XH.TipTool.showClipboardTip({msg = "是否允许使用剪切板，快速加入房间"})
end

function LobbyModule:onReqClipboardCallback(msg)
    local runningScene = display.getRunningScene()
    local topScene = XH.SceneManager:getInstance():getTopScene()
    if topScene ~= runningScene then
        return
    end
   
    if topScene and topScene.getTag then
        if topScene:getTag() ~= XH.SCENE_TAG.LOBBY then
            return
        end
    end

    if msg and #msg > 0 then
        if not XH.teaHouseManager:checkClipboard(msg) then
			local boxRoomIdStr = string.match(msg, "%d+") or ""
			local _start, _end = string.find(boxRoomIdStr, "%d+", 0)
			if _start and _end then
				local boxid = string.sub(boxRoomIdStr, _start, _end)
                if boxid and #boxid == 6 then
                    local numid = XH.playerData:getNumberID() 
                    if boxid == numid then
                        return
                    end
                    XH.SysTool.copyString("")
                  if not self._canJoin then
                      return
                  end
                  self:joinLimit()
                  if string.find(msg, "key=") then
                      XH.lobby:getModule("JoinBoxRoom"):reqJoinBoxRoom(boxid, 2)
                  else
                      XH.lobby:getModule("JoinBoxRoom"):reqJoinBoxRoom(boxid, 3)
                  end
				end
			end
		end
	end
end

--子游戏热更判断接口
function LobbyModule:isNeedHotUpdata(gameId)
    if gameId == LobbyConfig.LBGameID then
        return false
    end
    if XH.configerManager:getIsIosTishen() then
        return false
    end
    local targetPlatform = cc.Application:getInstance():getTargetPlatform()
    local HOTUPDATE_OPEN = false
    if device.platform == "windows" and WINDOWS_HOTUPDATE_OPEN then
        HOTUPDATE_OPEN = true
    end
    if cc.PLATFORM_OS_IPAD == targetPlatform or cc.PLATFORM_OS_IPHONE == targetPlatform  or cc.PLATFORM_OS_ANDROID == targetPlatform then 
        HOTUPDATE_OPEN = true
    end

    if HOTUPDATE_OPEN then
        local configer = require("app.Config.Parse.GameSubConfiger").new()
        local gameid = tonumber(gameId)
        local key = configer:getGameTypePathByGameId(gameid)
        if key == "" then
            return false
        end

        lobby = lobby or {}
        lobby._needUpdate = lobby._needUpdate or {}
        if lobby._needUpdate[key] == nil or lobby._needUpdate[key] == true then
            self.hotUpdatePriority = self.hotUpdatePriority + 1
            local HotUpdateThree = require('app.hotupdate.games.GameResChecker')
            HotUpdateThree._startHotFix(gameid, key, self.hotUpdatePriority)
            return true
        end
    end
    return false
end

--请求标签信息
function LobbyModule:reqUserTag()
    XH.playerData:initUserTagData()       --请求之前先重置数据
    local reqUserTag = XH.BoxDataProtocol.ReqUserTag:new()
    reqUserTag.nAskId = 0
    reqUserTag.tag_type = 0
    local SRSGroupID = XH.areaData:getSrsGroupID()
    XH.netEngine:addProtocolScriptFuncByObj(self, self.respUserTag, XH.BoxDataProtocol.ReqUserTag.processid, SRSGroupID)
    XH.netEngine:sendProtocol(reqUserTag, XH.BoxDataProtocol.ReqUserTag.processid,0,SRSGroupID)
end

function LobbyModule:respUserTag(XYID, buff, len, processid)
    if processid ~= XH.BoxDataProtocol.RespUserTag.processid then
        return
    end
    if XYID ~=  XH.BoxDataProtocol.RespUserTag.XY_ID then
        return
    end
    local respUserTag =  XH.BoxDataProtocol.RespUserTag:new()
    respUserTag:bistream(buff, len)
    
    XH.playerData:initUserTagData(respUserTag)

    local currScene = XH.SceneManager:getInstance():getTopScene()
    if currScene and currScene:getTag() == XH.SCENE_TAG.LOBBY then
        XH.lobby:getModule("PopSystem"):reqData()
        XH.lobby:getModule("PersonalGiftPack"):reqWebActivity()
    end
	self:reqUserTransferActivityState()
end

--B端迁移活动验证用户 
--by ruzibeil
function LobbyModule:reqUserTransferActivityState()
    if XH.areaData:isInTransferActivity() then
        local isTransferBind = XH.userDefault:getValue(XH.userDefault.KEY_ID.TRANSFER_BINDCHECK_STATE, false)
		if not isTransferBind then
			XH.lobby:getModule("AgentTransfer"):reqIsTransferBind()			 
		end
	end
end

-- 请求风信后台配置的动态链接
function LobbyModule:reqGetDynamicUrl()
    local secret = XH.areaData:getNotifSecret()
    local appid = XH.areaData:getNotifAppid()
    local wid = XH.areaData:getAreaWindID()
    if secret and appid and wid then
        self:startReq("ReqGetDynamicUrl", secret, appid, wid, 15)
    end
end

-- 请求风信后台配置的动态链接回调
function LobbyModule:onGetDynamicUrlCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data and data.data and next(data.data) then
            local getDynamicUrls = data.data
            if getDynamicUrls.zjIOS then
                UrlConf.DOWNLOAD_ADDRESS_IOS = getDynamicUrls.zjIOS.url or UrlConf.DOWNLOAD_ADDRESS_IOS
            end
            if getDynamicUrls.zjAndroid then
                UrlConf.DOWNLOAD_ADDRESS_ANDROID = getDynamicUrls.zjAndroid.url or UrlConf.DOWNLOAD_ADDRESS_ANDROID
            end
            if getDynamicUrls.zjGamesShare and XH.areaData:getXHLinkURL() then
                XH.areaData:setXHLinkURL(getDynamicUrls.zjGamesShare.url)
            end
            if getDynamicUrls.zjFissionUrl then
                UrlConf.FISSION_ACT_DOWNLOAD = getDynamicUrls.zjFissionUrl.url
            end
            UrlConf.DOWNLOAD_ADDRESS_ANDROID_TEXT = {}
            for i = 1,5 do
                if getDynamicUrls["zjAndriodTextUrl" .. i] then
                    UrlConf.DOWNLOAD_ADDRESS_ANDROID_TEXT[i] = getDynamicUrls["zjAndriodTextUrl" .. i].url
                end
            end
        end
    end
end

function LobbyModule:onTouchShare()
    if device.platform == "windows" then
        self:onShareSuccessCallBack()
        return
    end
    XH.lobby:getModule("Share"):share(self.shareCallback,self)
    if XH.lobby:getModule("Lobby"):checkSendShareAddressMonitorPoint() then
        XH.lobby:getModule("Lobby"):setSendShareAddressMonitorPoint()
    end
end

--无论取消还是成功，都当成时分享成功处理
function LobbyModule:shareCallback(data)
    self:onShareSuccessCallBack()
end

function LobbyModule:onShareSuccessCallBack()
    -- XH.taskManager:reqShareGiveRoomCard() 
    XH.lobby:getModule("LuckyTask"):shareSuccess()
end

function LobbyModule:isDealOpenUrl()
    return self._dealOpenUrl
end

function LobbyModule:onShowIdentityCheckView(event)
    XH.viewManager:openView("IdentityCheckView",nil,{isAutoBindPhone = true})
end

function LobbyModule:onShowIdentityCheckViewNew(event)
    XH.viewManager:openView("IdentityCheckView") 
end

function LobbyModule:onShowIdentityCheckViewNewIng(event)
    XH.TipTool.showToast("实名认证中，请稍后再试") 
    -- XH.viewManager:openView("IdentityCheckView",nil,{isRealnameIng = true})
end

function LobbyModule:onIdentityBack()
    if XH.playerData:getIdentityStatus() == 2 and (not XH.playerData:isProtectedLevel()) and (XH.playerData:getProtectedInwhitelist() == 0)  then
        XH.viewManager:openView("IdentityCheckView",nil,{isAutoBindPhone = true,compulsoryRealName = true})
    end
end

function LobbyModule:onGuideEvent(event)
    -- local param = {}
    -- param.goldcnt = event.goldcnt or 0
    -- param.cardcnt = event.cardcnt or 0
    -- 双扣CPS包不显示新手引导功能
    local param = event.data or {}
    XH.viewManager:openView("GuideView", nil, param)
end

-- 广告预加载相关优化
function LobbyModule:onLoadToponAdsCallBack(event)
--    local code = event.data.code
--    if (code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_VIDEOSUCCESS) then
--        XH.sdkManager:startToponAds()
--        XH.sdkManager:startToponBanner()
--    elseif (code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_CLOSE) then
--        XH.sdkManager:startToponAds()
--        XH.sdkManager:startToponBanner()
--    end
end

function LobbyModule:judgeVersion()
    local getVersion = XH.SysTool:GetBundleVersion()
    if device.platform == "android" and getVersion <= "1.0.1" then
        return false
    end
    if device.platform == "ios" and getVersion <= "1.0.43" then
        return false
    end
    return true
end

function LobbyModule:reqJoinGoldRoom(confID, roomFlag)
    local roomConf = XH.goldConfigManager:getGoldRoomInfoByIndex(confID)
    if roomConf.roomLevelInfos and #roomConf.roomLevelInfos == 0 then
        XH.TipTool.showToast("获取房间信息出错 - " .. roomConf.roomInfo.GameID, 2)
        return 
    end

    local func = function ()
        local roomInfos = roomConf.roomLevelInfos
        local join = roomConf.roomInfo.DirectJoin
        local index = 1
        if roomFlag and #roomFlag > 1 then
            if join and tonumber(join) == 1 then
                local playerSR = XH.playerData:getSR() or 0
                for i = #roomInfos, 1, -1 do
                    if roomInfos[i].minrich <= playerSR then
                        index = i
                        break
                    end
                end
            else
                self:onShowDetailGoldLayer(confID, roomConf.roomInfo.GameID,roomConf.roomInfo.goldUIStype)
                return
            end
        end
        if roomFlag and #roomFlag > 0 and roomFlag[index] then
            XH.lobby:getModule("Gold"):reqJoinGoldRoom(confID,roomFlag[index])
        end       
        XH.lobby:getModule("ToponAct"):setHotUpdata(false)
    end

    if self:isNeedHotUpdata(roomConf.roomInfo.GameID) then
        lobby.gameHotUpdateSuccessCallBack = func
        XH.lobby:getModule("ToponAct"):setHotUpdata(true)
    else
        func()
    end
end

function LobbyModule:onShowDetailGoldLayer(confID, gameID,goldUIStype)
    self:dispatchEvent( { name = self.EVENT_SHOW_DETAIL_LAYER, confID = confID, gameID = gameID, goldUIStype = goldUIStype})
end

-- get userid , session
function LobbyModule:getUserLoginInfo()
    local userID
    local session
    local lobbyID = XH.areaData:getLobbyID()
    local pathFile = cc.FileUtils:getInstance():getWritablePath() .. "SessionLoginData_" .. lobbyID .. ".dat"
    if cc.FileUtils:getInstance():isFileExist(pathFile) then
        local file = io.open(pathFile,"rb")
        local len = tonumber(file:read(2),10)
        userID = file:read(len)
        len = tonumber(file:read(2),10)
        session = file:read(len)
        file:close()
    else
        local playerData = XH.playerData
        userID = playerData:getUserID()
        session = playerData:getSessionID()
    end
    return userID, session
end

-- 传奇显隐性请求
function LobbyModule:reqIsLegendToSet()
    self:startReq("ReqIsLegendSet", XH.playerData:getNumberID(), 0, 120, TaskDefine.TASK_ID.TASK_TYPE_LEGENG_SET_VISIBLE, XH.areaData:getSrsGroupID(), 10)
end

-- 传奇显隐性判断
function LobbyModule:onReqIsLegendSetCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data and data.nComplete then
            self:dispatchEvent( { name = self.EVENT_SHOW_LEGEND_SETVISIBLE, state = data.nComplete })
        end
    end
end

function LobbyModule:openImView()
    self:dispatchEvent( { name = self.EVENT_OPEN_IM_VIEW })
end

-- 客服是否有新的消息
function LobbyModule:reqCusServiceUnreadMsg()
    XH.lobby:getModule("CusService"):reqCusServiceUnreadMsgCount()
end

function LobbyModule:reqRecomendInvitation()
    XH.lobby:getModule("TeaHouseBatchInvite"):reqRecomendInvitation()
end

-- 个推消息相关处理
function LobbyModule:dealWithGeTuiMsg()
    XH.SysTool.performWithDelayGlobal( function()
        local getuiMsg = XH.areaData:getGeTuiMsgLive()
        local currScene = XH.SceneManager:getInstance():getTopScene()
        local okMsg = false
        if getuiMsg then
            okMsg = pcall( function()
                getuiMsg = XH.SysTool.getTableByStrings(getuiMsg, ";", "=", true)
            end )
        end
        if currScene and currScene:getTag() == XH.SCENE_TAG.LOGIN then
            return
        end
        XH.areaData:setGeTuiMsgLive(nil)
        if currScene and(currScene:getTag() == XH.SCENE_TAG.GAME or currScene:getTag() == XH.SCENE_TAG.TEAHOUSE) then
            return
        end
        if getuiMsg and okMsg then
            if getuiMsg.areaid and tonumber(getuiMsg.areaid) ~= XH.areaData:getAreaID() then
                return
            end
            local getVersion = XH.SysTool:GetBundleVersion()
            if getuiMsg.secondpop and tonumber(getuiMsg.secondpop) == XH.ThirdDefine.GETUI_PAREM_DEFINE.GETUI_SECOND_POP_IM then
                XH.areaData:setEnterLobbyNeedOpenIm(true)
                XH.lobby:getModule("Lobby"):openImView()
            elseif getuiMsg.view and getVersion >= "1.0.55" then
                local ViewsConfig = require("app.Config.ViewsConfig")
                if ViewsConfig[getuiMsg.view] then
                    XH.viewManager:openView(getuiMsg.view)
                elseif getuiMsg.view == "ImView" then
                    XH.areaData:setEnterLobbyNeedOpenIm(true)
                    XH.lobby:getModule("Lobby"):openImView()
                end
            elseif getuiMsg.url and getVersion >= "1.0.55" then
                if XH.SysTool:GetBundleVersion() >= "1.1.9" and XH.configerManager:getIsJgSdk() then
                    cc.Application:getInstance():openURL("https://"..getuiMsg.url)
                else
                     cc.Application:getInstance():openURL(getuiMsg.url)
                end     
            end
        end
    end , 0.5)
end

function LobbyModule:onRealNameTimeOverCallBack(event)
    local msg = "游客您好，您已经完成40分钟体验，之后您可以选择【微信登录】继续享受精彩的游戏服务哦！"
    if event.data > 0 then
        msg = msg .. "下次体验时间:".. os.date("%Y",event.data).."年"..os.date("%m",event.data).."月"..os.date("%d",event.data).."日"..os.date("%H",event.data).."时"..os.date("%M",event.data).."分"
    end

    XH.TipTool.showTip({
        bTop = true,
        type = XH.TIP_LAYER_TYPE.OK_NO_CLOSE,
        funcOK = function(  )
            local isShowYouKe = false
            if XH.playerData:getPlayerIsAgent() and XH.areaData:getAgentShowYouKe() then
                isShowYouKe = true
            end
            XH.login:getModule("Login"):clearLoginStates()
            XH.Controller:getInstance():enterLogin(isShowYouKe)
        end,
    }, msg)
end

--转换时间格式   从时间戳 到2016-5-6 23:59:59
function LobbyModule:timeStamptoBdate(expirydate)
	return os.date("%Y",expirydate).."-"..os.date("%m",expirydate).."-"..os.date("%d",expirydate).." "..
		os.date("%H",expirydate)..":"..os.date("%M",expirydate)..":"..os.date("%S",expirydate)
end

function LobbyModule:onRealNameTimeOverLimitCallBack(event)
    XH.TipTool.showTip({
        bTop = true,
        type = XH.TIP_LAYER_TYPE.OK_NO_CLOSE,
        funcOK = function(  )
            
        end,
    }, "游客模式下暂时无法使用该功能，您可以选择【微信登录】获得完整的游戏服务")
end

function LobbyModule:setIsOpenGoldView(isOpenGoldView)
    self._isOpenGoldView = isOpenGoldView
end

function LobbyModule:isOpenGoldView()
    return self._isOpenGoldView
end

function LobbyModule:reqWebIcons()
    self:startReq("ReqWebIcons")
end

function LobbyModule:onReqWebIconsCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data then
            self._lobbyIcons = clone(data)
            XH.lobby:getModule("PopSystem"):setReqWebTabsBack(true)
        end
    end
    self:dispatchEvent({name = self.SHOW_LOBBY_ICONS})
end

function LobbyModule:getWebTabsData()
    return self._lobbyIcons or {}
end

function LobbyModule:getLobbyIconsConfig(posType)
    if not posType then
        return {}
    end
    local allMenuConfig = XH.configerManager:getLobbyMenuBarConfig()
    local lobbyIcons = self._lobbyIcons[LobbyConfig.ICON_DATA[posType]] or {}
    if not allMenuConfig then
        return {}
    end

    table.sort(lobbyIcons, function(a, b)
        return a.sort < b.sort
    end)

    local result = { }
    for i = 1, #lobbyIcons do
        for _, v in pairs(allMenuConfig) do
            if v.IconID == tonumber(lobbyIcons[i].icon_code) then
                table.insert(result, v)
                break
            end
        end
    end

    -- 直接塞入免赔礼包、破产礼包配置(可以移动到iop)
    if posType == 3 then
        for _, v in pairs(allMenuConfig) do
            if v.IconID == 3034 or v.IconID == 3035 then
                table.insert(result, v)
            end
        end
    end
    -- 推广活动先
    if posType == 1 then
        for _, v in pairs(allMenuConfig) do
            if v.IconID == 3023 then
                table.insert(result, v)
                break
            end
        end
    end
    -- 幸运任务
    if posType == 2 then
        -- 判断连炸斗地主显示
        XH.lobby:getModule("NewGoldScene"):setIconVisible(NewGoldSceneDefine.SHOW_SCENE.RIGHT, false)
        for i = 1, #lobbyIcons do
            if tonumber(lobbyIcons[i].icon_code) == LobbyConfig.ICON_ID.GOLDNEW_LZDDZ then
                XH.lobby:getModule("NewGoldScene"):setIconVisible(NewGoldSceneDefine.SHOW_SCENE.RIGHT, true)
                break
            end
        end
        for _, v in pairs(allMenuConfig) do
            if v.IconID == 3024 or v.IconID == 3025 or v.IconID == 3034 or v.IconID == 3035 or v.IconID == 3037 then
                table.insert(result, v)
            end
        end
    end
    return result
end

function LobbyModule:reqLeisureEntrance()
    self:startReq("ReqLeisureEntrance")
end

function LobbyModule:onReqLeisureEntranceCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data then
            self._leisureEntrance = data
            self:onLeisureEntranceInfoChanged(self._leisureEntrance)
            self:dispatchEvent({name = self.SHOW_LEISURE_ENTRANCE})
        end
    end
end

local GameInfo = {
    -- [30116] = "双扣",
    -- [30386] = "关牌"
}
--合规
function LobbyModule:onLeisureEntranceInfoChanged(data)
    for _,v in pairs(data) do 
        if GameInfo[v.icon_params.GameID] ~= nil then 
            v.icon_params.GameTitle = GameInfo[v.icon_params.GameID]
        end
    end
end

function LobbyModule:getLeisureEntranceData()

    --除了前面拿的配置，还要去配置中心拿一下，覆盖或者补进来
    local ConfigurationDefine = require("lobby.Modules.Configuration.Define")
    local levelCfg = XH.lobby:getModule("Configuration"):getConfigJsonData(ConfigurationDefine.Config.GOLD, "Level")
    if levelCfg and levelCfg.Game then 
        for k,v in pairs(levelCfg.Game) do
            local gameid = k
            local isFind = false
            for i = 1,#self._leisureEntrance do
                if self._leisureEntrance[i].icon_params ~= nil and self._leisureEntrance[i].icon_params.GameID ~= nil and self._leisureEntrance[i].icon_params.GameID == tonumber(gameid) then
                    self._leisureEntrance[i] = v
                    isFind = true
                    break
                end
            end
            if isFind == false then 
                self._leisureEntrance[#self._leisureEntrance + 1] = v 
            end
        end
    end
    return self._leisureEntrance or {}
end

function LobbyModule:getEntranceByType(type)
    local tmp = { }
    local result = { }
    local leisureEntrance = self:getLeisureEntranceData()
    if leisureEntrance then
        if type then
            for _, v in pairs(leisureEntrance) do
                if type == v.icon_type then
                    table.insert(tmp, v)
                end
            end
        else
            tmp = clone(leisureEntrance)
        end
    end
    table.sort(tmp, function(a, b)
        if a.sort and b.sort then
            return a.sort < b.sort
        end
    end)
    for k, v in pairs(tmp) do
        v.icon_params.icon_type = v.icon_type
        table.insert(result, v.icon_params)
        result[k].Index = v.sort
    end
    return result
end

function LobbyModule:clearLeisureEntranceData()
    self._leisureEntrance = { }
end

function LobbyModule:getLobbyIconWorldPos(iconId, callback)
    local event = {}
    event.iconId = iconId
    event.callback = callback
    self:dispatchEvent( { name = self.EVENT_GET_ICON_WORLD_POS, data = event })
end

function LobbyModule:onRespStopSvrMsg(event)
    local respData = XH.MatchLinkProtocol.RespStopSvrMsg:new()
    respData:bistream(event.msg.buff, event.msg.len)
    if respData.ucflag == XH.MatchLinkProtocol.RespStopSvrMsg.UCFLAG.STOPED then
        XH.playerData:stopVisitorHeart()
        XH.login:getModule("Login"):clearLoginStates()
        XH.Controller:getInstance():enterLogin()
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK_NO_CLOSE,
            bTop = true,
            bCrossScene = true,
        }, "游戏正在维护中,请过一会再来~")
    end
end

function LobbyModule:onGetConfiguration(event)
    
    local a = 0
    
end

function LobbyModule:showIconRedPos(iconID, bShow)
    self:dispatchEvent({ name = LobbyModule.EVENT_SHOW_RED_POS, data = {iconID = iconID, bShow = bShow} })
end

function LobbyModule:reqTeaAutoAwardInfo()
    self:startReq("ReqTeaAutoAwardInfo", 15)
end

function LobbyModule:onReqTeaAutoAwardInfoCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data and data.need_show == 1 then
            XH.viewManager:openView("TeaHouseAutoAwardView", nil, data)
        end
    end
end

function LobbyModule:startFishingH5Game()
    if XH.playerData:getWeChatBuYuUserID() ~= "" then
        XH.TipTool.showChoseTip({
            msg = "请选择登陆方式",
            btnOneTxt = "公众号登陆",
            funcChoseOne = function ()
                self:startH5Game(self.H5GameNameList.BUYU,true)
            end,
            btnTwoTxt = "APP账号登陆",
            funcChoseTwo = function ()
                self:startH5Game(self.H5GameNameList.BUYU)
            end,
        })
        return
    end
    self:startH5Game(self.H5GameNameList.BUYU)
end

function LobbyModule:startH5Game(gameName, isWeChatId)
    if device.platform ~= "android" then
        return
    end
    if XH.playerData:checkNewRealName() ~= 0 then return end--实名限制
    if XH.playerData:checkNewRealNameVisitor() ~= 0 then return end--游客登陆

    if XH.playerData:isSpecialBYID() and not XH.SysTool:CompareToBundleVersion("1.3.1") then
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK_CANCEL_NO_CLOSE,
            funcOK = function()
                XH.SysTool.downloadNewVersionApp()
            end
        } , "当前版本不支持，是否前往下载最新版本")
        return
    end

    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_JOIN_LIANYUN_GAME,{gameName = gameName})
    if XH.sdkManager:isSupportFunctionAndroid("start_game_login") == "false" then
        XH.TipTool.showToast("当前版本不支持，请下载最新版本")
        return
    end

    if self._loginLock then
        if not XH.ChannelTool.checkIsCPSChannel() then
            XH.TipTool.showToast("您操作的太频繁了，请稍后再试")
        end
        return
    end

    self._loginLock = true
    if XH.sdkManager:isSupportFunctionAndroid("start_game_login") then
        XH.audioManager:SetMusicVolume(0)
        XH.audioManager:SetSFXVolume(0)
        local reqH5LoginCer = require("app.Req.H5Game.ReqH5StartGameLogin").new()
        reqH5LoginCer:addReqCallBack(self, self.h5StartGameLoginCallback)
        local nickname = XH.playerData:getNickName()
        local sex = XH.playerData:getSex()
        local id = XH.playerData:getNumberID()
        local url = XH.playerData:getWeChatURL()
        if gameName == self.H5GameNameList.LEGEND and XH.playerData:getLegendUserID() ~= "" then
            id = XH.playerData:getLegendUserID()
        elseif gameName == self.H5GameNameList.BUYU and XH.playerData:getBuYuUserID() ~= "" then
            id = XH.playerData:getBuYuUserID()
        elseif gameName == self.H5GameNameList.BUYU and XH.playerData:getWeChatBuYuUserID() ~= "" and isWeChatId then 
            id = XH.playerData:getWeChatBuYuUserID()
        end
        local bNeedConfigID = XH.playerData:isNeedConfigID() == 1
        reqH5LoginCer:start(id, nickname, sex, url, reqH5LoginCer.GameList[gameName], bNeedConfigID, isWeChatId)
    else
        XH.TipTool.showToast("not support function!")
    end
    self:performWithDelay(function()
        self._loginLock = false
    end, 5)
end

function LobbyModule:h5StartGameLoginCallback(cell, type, data)
    -- 回恢复音效
    local musicPercent = XH.lobby:getModule("Set"):getMusicPercent()
    local soundPercent = XH.lobby:getModule("Set"):getSoundPercent()
    XH.audioManager:SetMusicVolume(musicPercent / 100.0)
    XH.audioManager:SetSFXVolume(soundPercent / 100.0)
end

function LobbyModule:startH5Room()
    if device.platform ~= "android" then
        return
    end

    if XH.playerData:checkNewRealName() ~= 0 then return end--实名限制
    if XH.playerData:checkNewRealNameVisitor() ~= 0 then return end--游客登陆

    if XH.playerData:isSpecialBYID() and not XH.SysTool:CompareToBundleVersion("1.3.1") then
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK_CANCEL_NO_CLOSE,
            funcOK = function()
                XH.SysTool.downloadNewVersionApp()
            end
        } , "当前版本不支持，是否前往下载最新版本")
        return
    end

    if XH.sdkManager:isSupportFunctionAndroid("start_game_room") == "false" then
        XH.TipTool.showToast("当前版本不支持，请下载最新版本")
        return
    end

    if self._loginLock then
        if not XH.ChannelTool.checkIsCPSChannel() then
            XH.TipTool.showToast("您操作的太频繁了，请稍后再试")
        end
        return
    end

    self._loginLock = true
    if XH.sdkManager:isSupportFunctionAndroid("start_game_room") then
        XH.audioManager:SetMusicVolume(0)
        XH.audioManager:SetSFXVolume(0)
        --获取H5 ROOM 的许可 
        local reqH5RoomLoginCer = require("app.Req.H5Game.ReqH5StartGameRoom").new()
        ---恢复音效
        reqH5RoomLoginCer:addReqCallBack(self, self.h5StartGameLoginCallback)

        local cp =  XH.areaData:getH5CpID() or ""                   --得到CP_ID
        local nickname = XH.playerData:getNickName()                --授权用户id
        local sex = XH.playerData:getSex()                          --授权用户性别
        local id = XH.playerData:getNumberID()                      --授权用户id
        local url = XH.playerData:getWeChatURL()                    --得到微信链接  
        local bNeedConfigID = XH.playerData:isNeedConfigID() == 1   
        reqH5RoomLoginCer:start(cp ,id, nickname, sex, url , bNeedConfigID)
    else
        XH.TipTool.showToast("not support function!")
    end
    self:performWithDelay(function()
        self._loginLock = false
    end, 5)
end 
function LobbyModule:reqGetBanInfo()
    self:startReq("ReqGetBanInfo", 15)
end

function LobbyModule:onReqGetBanInfoCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local tempTable = {}
        tempTable.bansize = data.bansize
        tempTable.bantype = data.bantype
        tempTable.banreason = data.banreason
        tempTable.banendtime = data.banendtime
        local lastLoginTime = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_LAST_LOGIN_TIME, 0)
        local oldStrJson = cc.UserDefault:getInstance():getStringForKey("BAN_PLAYER_INFO".. XH.playerData:getNumberID())
        local tempJson = json.encode(tempTable)
        for i=1,data.bansize do
            if data.bantype[i] == XH.BoxDataProtocol.RespGetBanInfo.bantype.BAN_GAME then  --禁止游戏      
                cc.UserDefault:getInstance():setBoolForKey("BAN_PLAYER_CAN_GAME".. XH.playerData:getNumberID(),true)                
            end
        end
        if (oldStrJson ~= tempJson and data.bansize > 0) or (data.bansize > 0 and (not XH.SysTool.isToday(lastLoginTime))) then                        
            XH.viewManager:openView("LockTips", nil, data) 
            cc.UserDefault:getInstance():setStringForKey("BAN_PLAYER_INFO".. XH.playerData:getNumberID(),tempJson)
            return
        end                
        for i=1,data.bansize do
            if data.bantype[i] == XH.BoxDataProtocol.RespGetBanInfo.bantype.BAN_LOGIN then--禁止登录
                XH.viewManager:openView("LockTips", nil, data)                
                return    
            end
        end
    end
end

function LobbyModule:enterNewGold(param)
    local newGoldController = require("newgold.NewGoldController")
    newGoldController:getInstance():enter(param)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_SIGN_IN_GO_GOLD_STATE, 1)
end

function LobbyModule:reqLocation()
    self:startReq("ReqLocation", 5)
end

function LobbyModule:onReqLocation(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local ret2
        local ok2, msgEx2 = pcall(function()
            ret2 = cjson.decode(data.info)
        end)
        if ok2 then
            XH.playerData:setServerLocation(ret2.longitude, ret2.latitude, ret2.location)
            local ret
            local info = {
                longitude = ret2.longitude,
                latitude = ret2.latitude,
                country = ret2.location,
            }
            local ok, msgEx = pcall(function()
                ret = cjson.encode(info)
            end)
            if ok and ret then
                XH.sdkManager:dispatchAmap(XH.ThirdDefine.AMAP_CALLBACK_CODE.AMAP_SUCCESS, ret)
                return
            end
        end
    end
    XH.sdkManager:dispatchAmap(XH.ThirdDefine.AMAP_CALLBACK_CODE.AMAP_FAIL, "")
end

return LobbyModule��  