local LoginModule = class("LoginModule", XH.ModuleBase)
local LoginConfig = require("login.Modules.Login.Config")
local areaConfig = require("app.Config.AreaConfig")
local GoldLaunchConfig = require("lobby.Modules.GoldLaunch.Config")

--发送事件
LoginModule.LOGIN_EVENT_SUCCESS                        = "LOGIN_EVENT_SUCCESS"
LoginModule.LOGIN_EVENT_FAIL                           = "LOGIN_EVENT_FAIL"

LoginModule.EVENT_LOGIN_SUCCESS_CHANGE_GROUP           = "EVENT_LOGIN_SUCCESS_CHANGE_GROUP"

--接受事件
LoginModule.EVENT_BIND_USER                            = "EVENT_BIND_USER"

--设置按钮
LoginModule.EVENT_SET_LOGIN_STATE                      = "EVENT_SET_LOGIN_STATE"
--外部重连成功
LoginModule.EVENT_RELINK_SUCCESS                      = "EVENT_RELINK_SUCCESS"

local KW_GY_SESSION_TIME = 300


local LOGIN_STATUS = {
    LOGIN_NOTLOGIN   = -2,
    LOGIN_FAIL       = 1,
    LOGIN_SUCCESS    = 2,
}

function LoginModule:ctor() 
	LoginModule.super.ctor(self)
    
    self._loginStates = LOGIN_STATUS.LOGIN_NOTLOGIN
	
    self._wechatUrl = ""
    self._nRlinkCount = 0

    self._sdkLoginType = XH.SDK_LOGIN_TYPE.SDK_UNKNOWN
    self._isReqWXBind = false
    self._isReqGYInfo = false
    self._onlyBindPhone = false
    self._bReqLoginStatus = false
    self._bSDKInit = false

    self._isNewPlayer = false
end

function LoginModule:getProxyEvents()
    return {
        { module = XH.netEngine, eventName = XH.SRSProtocol.RespSRSLoad.event_key, callBack = "onRespSRSLoad" },
        { module = XH.netEngine, eventName = XH.SRSProtocol.RespSRSAddr.event_key, callBack = "onRespSRSAddr" },
        { module = XH.netEngine, eventName = XH.AuthProtocol.ReportNewPlayer.event_key, callBack = "onReportNewPlayer" },
        { module = XH.netEngine, eventKeyName = "EVENT_NET_ENGINE_LINKSTATUS_CHANGED", callBack = "onLinkStatusChanged" },
        { module = XH.netEngine, eventKeyName = "EVENT_RELINK_SRS", callBack = "onRelinkSrs" },
        { module = XH.sdkManager, eventKeyName = "EVENT_LOGIN_CALLBACK", callBack = "onLoginCallback" },
        { module = XH.sdkManager, eventKeyName = "EVENT_AMAP_CALLBACK", callBack = "onGetLocationCallBack" },
        { module = XH.playerData, eventKeyName = "EVENT_PLAYER_INFO_CHANGED", callBack = "onPlayerInfoChanged" },
        { module = XH.sdkManager, eventKeyName = "EVENT_GETUISDK_CALLBACK", callBack = "onReciveGeTuiDataCallBack" },
    }
end

function LoginModule:getReqConfig()
    return {
        ReqLoginIn = { reqPath = "login.Req.RunLogin", callBack = self.onTranLoginCallBack },
        ReqStopSvrMsg = { reqPath = "login.Req.ReqStopSvrMsg"},
        TransmitBindUser = { reqPath = "app.Req.PlayerMessage.TransmitBindUser", callBack = self.onTransmitBindUser },
        ReqSessionLoginIn = { reqPath = "login.Req.RunLogin", callBack = self.onTranLoginSessionCallBack },
        ReqChangeGroupSessionLoginIn = { reqPath = "login.Req.RunLogin", callBack = self.onTranChangeGroupSessionLoginCallBack },
        ReqBindUser = { reqPath = "app.Req.PlayerMessage.ReqBindUser", callBack = self.onReqPhoneBind },
        ReqSetHeadAndNickname = { reqPath = "login.Req.Login.ReqGetOrSetHead", callBack = self.onReqSetHeadAndNickname },
        ReqNeedShowFreeQuan = { reqPath = "app.Req.Task.ReqTaskPercent", callBack = self.onReqTaskPlayerTagCallBack },
        ReqReuiteUserDataByTask = { reqPath = "app.Req.Task.ReqTaskPercent", callBack = self.onReqTaskReuiteUserData },
        ReqBindClientId = { reqPath = "app.Req.PlayerMessage.ReqBindUser", callBack = self.onReqBindClientId },
    }
end

function LoginModule:onLinkStatusChanged()
    local SRS = XH.netEngine:getSRSBySRSGroupID(XH.areaData:getSrsGroupID())
    if SRS then
        local linkstate = SRS:getLinkState()
        if linkstate ~= XH.SRS_LINK_STATE.LINK_STATE_SUCCESS then
            if  self._nRlinkCount == 0 then
                self._loginStates = LOGIN_STATUS.LOGIN_NOTLOGIN
                XH.netEngine:reLinkSRS(XH.areaData:getSrsGroupID())
                self._nRlinkCount = self._nRlinkCount + 1 
                --游戏中登录失败
                if XH.gameManager and XH.gameManager.onLoginFail then
                    XH.gameManager.onLoginFail()
                end
            end
        end
    end
end

function LoginModule:onRelinkSrs()
    self:sessionLogin()
end

--设置sdk中的config参数
function LoginModule:setSDKConfig()
    --设置sdk
    local proForpey = {
        appId = XH.areaData:getAppID(),
        channelId = XH.areaData:getChannelID(),
        packageId = XH.areaData:getPackgeID(),
        wxappId = XH.areaData:getWXAppID(),
        wxkeyId = XH.areaData:getWXAppKeyID(),
        wxulink = XH.areaData:getUniversalLink(),
    }
    XH.sdkManager:xhCallOCFunc("XHSwitchApp", "xhsa_set_appid:", proForpey)
end

--session登录
function LoginModule:sessionLogin()
    self:setSDKConfig()
    local sessionInfo = XH.loginData:getSessionLoginInfo()
    if sessionInfo then
        XH.TipTool.showLoading()
        if sessionInfo.wechatUrl then
            self._wechatUrl = sessionInfo.wechatUrl
        end
        self:reqPlayerSessionLogin(XH.SRSProtocol.PlayerConnect.USERTYPE.SESSION,sessionInfo.userid,sessionInfo.password)
    end
end

function LoginModule:wxLogin()
    if not XH.sdkManager:isInstallWeChat() then
        --提示玩家未安装微信
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, "您还未安装微信")
        return
    end
    self:reqWXLogin()
end

function LoginModule:changeGroupSessionLogin(srsGroupID)
    local sessionInfo = XH.loginData:getSessionLoginInfo()
    if sessionInfo then
        if sessionInfo.wechatUrl then
            self._wechatUrl = sessionInfo.wechatUrl
        end
        self:reqPlayerChangeGroupSessionLogin(XH.SRSProtocol.PlayerConnect.USERTYPE.SESSION,sessionInfo.userid,sessionInfo.password,srsGroupID)
    end
end

function LoginModule:quickLogin()
    local userType = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_DATA_LAST_LOGIN_TYPE,-1)
      --密码登录
    if userType ==  XH.SRSProtocol.PlayerConnect.USERTYPE.IDENTIFY or userType ==  XH.SRSProtocol.PlayerConnect.USERTYPE.PHONENUM then
        local userid = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_DATA_LAST_LOGIN_USERID, "")
        local password = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_DATA_LAST_LOGIN_PASSWORD, "");
        self:reqPlayerLogin(userType,userid,password)
    elseif userType ==  XH.SRSProtocol.PlayerConnect.USERTYPE.DEVELOPER then
        --微信session失败，换取sdk的自动登录方式
        --登录出错，出现出错的界面，跳转
        local sdkLoginType = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_DATA_LAST_SDK_LOGIN_TYPE,0)
        if sdkLoginType == XH.SDK_LOGIN_TYPE.SDK_WX then
            self._sdkLoginType = XH.SDK_LOGIN_TYPE.SDK_WX
            if device.platform == "android" then
                local wxappId = XH.areaData:getWXAppID()
                XH.sdkManager:callFunctionWithMap(XH.ThirdDefine.SDK_INTERFACE_NAMES_KEY.WX_LOGIN, {wxappId}, false)
            else
                XH.sdkManager:xhCallOCFunc("XHWXLogin", "weixinLoginIn:", {})
            end
        elseif sdkLoginType == XH.SDK_LOGIN_TYPE.SDK_GY then
            self._sdkLoginType = XH.SDK_LOGIN_TYPE.SDK_GY
            if device.platform == "android" then
                if XH.SysTool:GetBundleVersion() >= "1.1.9" and XH.configerManager:getIsJgSdk() then
                    XH.sdkManager:callFunction(XH.ThirdDefine.SDK_INTERFACE_NAMES_KEY.JG_LOGIN)
                else
                    XH.sdkManager:callFunction(XH.ThirdDefine.SDK_INTERFACE_NAMES_KEY.GY_LOGIN)
                end
            else
                if XH.SysTool:GetBundleVersion() >= "1.1.9" and XH.configerManager:getIsJgSdk() then
                    local getToponInfo = {
                        ["functionName"] = "jg_onetap_login",
                    }
                    XH.sdkManager:xhCallOCFunc("UserInterface", "callFunction", getToponInfo)
                else
                    XH.sdkManager:xhCallOCFunc("XHGeYan", "gy_onetap_login")
                end
            end
        end
    end
end

--登录接口 *登录方式*用户名*密码
function LoginModule:reqPlayerLogin(userType,loginName,loginPasswd,isReqBind,sdkType,sdkData)
    if not isReqBind then
        XH.TipTool.showLoading()
    end
    local SRS = XH.netEngine:getSRSBySRSGroupID(XH.areaData:getSrsGroupID())
    if SRS then
        local linkstate = SRS:getLinkState()
        if linkstate == XH.SRS_LINK_STATE.LINK_STATE_SUCCESS then
            XH.netEngine:removeSRS(XH.areaData:getSrsGroupID())
            XH.SysTool.performDelayOnce(function ()
                self:reqLoginIn(userType,loginName,loginPasswd,isReqBind,sdkType,sdkData)
            end,0.001)
            return
        end
    end
    self:reqLoginIn(userType,loginName,loginPasswd,isReqBind,sdkType,sdkData)
end

function LoginModule:reqLoginIn(userType,loginName,loginPasswd,isReqBind,sdkType,sdkData)
    if isReqBind then
        self:reqBindUser(userType,loginName,loginPasswd,sdkType,sdkData)
        return
    end
    self:startReq("ReqLoginIn", userType,loginName,loginPasswd,XH.areaData:getAreaID(),XH.areaData:getLobbyID(),XH.areaData:getConfigGameID(),sdkType,XH.areaData:getSrsGroupID(),XH.KW_CONFIG_LOGIC_TIME_OUT) --5012
end

function LoginModule:reqBindUser(userType,loginName,loginPasswd,sdkType,sdkData)
    self:startReq("TransmitBindUser", userType,loginName,loginPasswd,XH.areaData:getAreaID(),XH.areaData:getLobbyID(),{
        areaID = XH.playerData:getBrandID(),numID = XH.playerData:getNumberID(),
        sdkType=sdkType,sdkData = sdkData
    },XH.areaData:getSrsGroupID(),XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function LoginModule:onTransmitBindUser(req,type,data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        XH.TipTool.showToast("绑定成功！")
        self:dispatchEvent({name = LoginModule.EVENT_BIND_USER})
        local jsonData
        local ok, _ = pcall(function()
            jsonData = cjson.decode(data.sdkData)
        end)
    
        if not ok then
            return
        end
        local userid = jsonData.uid
        local token = jsonData.session
        local nickname = jsonData.username
        local password = token.."|"..1010 .."|"..1432
        XH.playerData:setPid(jsonData.pid)
        XH.playerData:setWeChatURL(jsonData.headimgurl or "")
        XH.playerData:setThirdNickName(nickname)
        XH.playerData:setUnionId(data.sdkData.unionid)
        if self._bReqLoginStatus then
            return
        end
        self._bReqLoginStatus = true
        local srsGroupID = XH.areaData:getSrsGroupID()
        XH.netEngine:removeSRS(srsGroupID)
        local userType = XH.SRSProtocol.PlayerConnect.USERTYPE.DEVELOPER
        local loginName = userid
        local loginPasswd = password
        local sdkType = data.sdkType
        XH.SysTool.performDelayOnce(function ()
            self:reqPlayerLogin(userType,loginName,loginPasswd,false,sdkType,data)
        end,0.001)
    else
        XH.TipTool.showToast(req:getMessage())
        self:sessionLogin()
    end
end

--session登录
function LoginModule:reqSessionLoginIn(userType,loginName,loginPasswd)
    XH.TipTool.showLoading()
    local lobbyID = XH.areaData:getLobbyID()
    local areaID = XH.areaData:getAreaID()
    local configGameID = XH.areaData:getConfigGameID()
    local srsGroupID = XH.areaData:getSrsGroupID()
    self:startReq("ReqSessionLoginIn", userType,loginName,loginPasswd,areaID,lobbyID,configGameID,nil,srsGroupID,5) --5012
end

function LoginModule:reqChangeGroupSessionLoginIn(userType,loginName,loginPasswd,srsGroupID)
    local lobbyID = XH.areaData:getLobbyID()
    local areaID = XH.areaData:getAreaID()
    local configGameID = XH.areaData:getConfigGameID()
    self:startReq("ReqChangeGroupSessionLoginIn", userType,loginName,loginPasswd,areaID,lobbyID,configGameID,nil,srsGroupID,3) --5012
end

function LoginModule:reqPlayerSessionLogin(userType, loginName, loginPasswd)
    local srsGroupID = XH.areaData:getSrsGroupID()
    XH.netEngine:removeSRS(srsGroupID)
    XH.SysTool.performDelayOnce( function()
        self:reqSessionLoginIn(userType, loginName, loginPasswd)
    end , 0.001, false)
end

function LoginModule:reqPlayerChangeGroupSessionLogin(userType,loginName,loginPasswd,srsGroupID)
    XH.netEngine:removeSRS(srsGroupID)
    XH.SysTool.performDelayOnce(function ()
        self:reqChangeGroupSessionLoginIn(userType,loginName,loginPasswd,srsGroupID)
    end,0.001)
end

--游客登录
function LoginModule:reqTouristLogin(name)
    local srsGroupID = XH.areaData:getSrsGroupID()
    XH.netEngine:removeSRS(srsGroupID)
    local userType = XH.SRSProtocol.PlayerConnect.USERTYPE.IDENTIFY
    local loginName = name or ""
    local loginPasswd = ""

    XH.TipTool.showLoading()
    XH.SysTool.performDelayOnce(function ()
        self:reqPlayerLogin(userType,loginName,loginPasswd)
    end,0.001)
end

function LoginModule:gotoLobby()
    if XH.SceneManager:getInstance():getTopSceneTag() == XH.SCENE_TAG.LOGIN
    or XH.SceneManager:getInstance():getTopSceneTag() == XH.SCENE_TAG.LOBBY then
        XH.Controller:getInstance():enterLobby()
        local lastLoginTime = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_LAST_LOGIN_TIME, 0)
        XH.loginData:setIsTodayFirstLogin(os.date("%Y%m%d") ~= os.date("%Y%m%d", lastLoginTime))
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_LAST_LOGIN_TIME, os.time())
        if  XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_FIRST_LOGIN_TIME, 0) == 0 then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_FIRST_LOGIN_TIME, os.time()) 
        end
    end
end

--登录回调
function LoginModule:onTranLoginSessionCallBack(transmit,type,data)
    XH.TipTool.hideLoading()
    if type ==  XH.Req.TYPE.SUCCESS then
        self._loginStates = LOGIN_STATUS.LOGIN_SUCCESS
        if XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_IS_APPLE_LOGIN) then
            self._sdkLoginType = XH.SDK_LOGIN_TYPE.SDK_APPLE
        end
        self:changePlayerData(data)
        --头像设置
        if self._wechatUrl and self._wechatUrl ~=  "" then
            XH.playerData:setWeChatURL(self._wechatUrl)
        end
        self:gotoLobby()
        self:dealAfterLoginReq()

        -- 大厅重连完成后重连其他用到的srs
        local SRSs = XH.netEngine:getAllSRS()
        for k,_ in pairs(SRSs) do
            if k ~= XH.areaData:getSrsGroupID() and not table.indexof(self:getSrsGroupIDOther(),k) then
                XH.SysTool.performDelayOnce(function()
                self:changeGroupSessionLogin(k)
                end, 1)
            end 
        end
        self:dispatchEvent({name = self.EVENT_RELINK_SUCCESS})
    elseif type ==  XH.Req.TYPE.TIMEOUT then
        self._loginStates = LOGIN_STATUS.LOGIN_NOTLOGIN
        local srsGroupID = XH.areaData:getSrsGroupID()
        XH.netEngine:reLinkSRS(srsGroupID)
        self._nRlinkCount = self._nRlinkCount + 1
    else
        if data and data.getErrcode == XH.SRSProtocol.PlayerData.ERRORCODE.UNKNOWN_THIRDID then
            XH.loginData:cleanLocalSessionTime()
            --拉起微信授权
            self:reqWXLogin()
        elseif data and data.getErrcode == XH.SRSProtocol.PlayerData.ERRORCODE.ACOCUNT_CANCELING then
            XH.TipTool.hideLoading()
            XH.playerData:setPlayerData({playerData = {numid = data.numid, areaid = XH.areaData:getAreaID() }, ppd = {userid = tostring(data.numid)}} )
            XH.playerData:showWriteOffDialog()
        else
            XH.loginData:cleanLocalSessionTime()
            -- 登陆失败，根据不同登陆方式，换种方式去登陆
            self:quickLogin()
        end
    end
end

function LoginModule:onTranChangeGroupSessionLoginCallBack(transmit,type,data)
    if type ==  XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({name = LoginModule.EVENT_LOGIN_SUCCESS_CHANGE_GROUP , msg = { srsGroupID = data.srsGroupID }})
        self:reqJoin()
	elseif type ==  XH.Req.TYPE.TIMEOUT then
        self:changeGroupSessionLogin(transmit._srsGroupID)
    else
        self:changeGroupSessionLogin(transmit._srsGroupID)
    end
end

function LoginModule:onTranLoginCallBack(transmit, type, data)
    self._bReqLoginStatus = false
    if type == XH.Req.TYPE.SUCCESS then
        self._loginStates = LOGIN_STATUS.LOGIN_SUCCESS
        local sex = nil
        -- local tempNickName = XH.playerData:getThirdNickName()
        -- local tempImageUrl = XH.playerData:getWeChatURL()
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_IS_APPLE_LOGIN, self._sdkLoginType == XH.SDK_LOGIN_TYPE.SDK_APPLE) --本地缓存是否苹果登录
        self:changePlayerData(data)
        local sessionInfo = { }
        if data.sdkType == XH.SDK_LOGIN_TYPE.SDK_GY then
            sessionInfo.sessionTime = tostring(os.time())
        end
        XH.loginData:saveSessionLoginInfo(sessionInfo)

        -- local isChange = false
        -- if tempNickName ~= XH.playerData:getNickName() then
        --     isChange = true
        -- end
        -- if tempImageUrl ~= XH.playerData:getWeChatURL() then
        --     isChange = true
        -- end
        if XH.areaData:isSupportAutoPhoneBind() and data.isBindPhone ~= nil and data.isBindPhone == false and not XH.ChannelTool.checkIsDoubleKouGame() then
            if data.sdkType == XH.SDK_LOGIN_TYPE.SDK_WX --[[and isChange]] then
                if data and data.ppd and data.ppd.nickname2 and data.ppd.notify and data.ppd.nickname2 ~= "" and data.ppd.notify ~= "" then
                    XH.playerData:reqChangePlayerInfo(sex, data.ppd.nickname2, data.ppd.notify, false, true)
                else
                    self:loginFinished()
                end
            else
                self:reqGYInfo()
            end
        else
            if data.sdkType == XH.SDK_LOGIN_TYPE.SDK_WX --[[and isChange]] then
                if data and data.ppd and data.ppd.nickname2 and data.ppd.notify and data.ppd.nickname2 ~= "" and data.ppd.notify ~= "" then
                    XH.playerData:reqChangePlayerInfo(sex, data.ppd.nickname2, data.ppd.notify)
                else
                    self:loginFinished()
                end
            else
                self:loginFinished()
            end
        end
    elseif type == XH.Req.TYPE.FAIL then
        XH.TipTool.hideLoading()
        if transmit and data and data.getErrcode == 57 then
            if not XH.sdkManager:isInstallWeChat() then
                XH.TipTool.showTip( {
                    type = XH.TIP_LAYER_TYPE.OK,
                } , "登录取消或调用失败，请使用其他登录方式")
            else
                --个验登录失败,错误码57,客户端再次微信授权
                XH.SysTool.performDelayOnce(function ()
                    self:reqWXLogin()
                end,0.5)
            end
        elseif data and data.getErrcode == XH.SRSProtocol.PlayerData.ERRORCODE.ACOCUNT_CANCELING then
            XH.TipTool.hideLoading()
            XH.playerData:setPlayerData({playerData = {numid = data.numid, areaid = XH.areaData:getAreaID() }, ppd = {userid = tostring(data.numid)}} )
            XH.playerData:showWriteOffDialog()
        else
            XH.TipTool.showTip( {
                 type = XH.TIP_LAYER_TYPE.OK,
            } , transmit:getMessage() or "登录失败，请联系客服")
            XH.TipTool.hideLoading()
        end
    elseif type == XH.Req.TYPE.TIMEOUT then
        self._loginStates = LOGIN_STATUS.LOGIN_NOTLOGIN
        local srsGroupID = XH.areaData:getSrsGroupID()
        XH.netEngine:reLinkSRS(srsGroupID)
        self._nRlinkCount = self._nRlinkCount + 1
    end
end

--微信登录
function LoginModule:reqWXLogin()
    self:setSDKConfig()
    XH.TipTool.showLoading()
    local target = cc.Application:getInstance():getTargetPlatform()
    if target == cc.PLATFORM_OS_IPHONE or target == cc.PLATFORM_OS_IPAD then
        self._sdkLoginType = XH.SDK_LOGIN_TYPE.SDK_WX
        XH.sdkManager:xhCallOCFunc("XHWXLogin", "weixinLoginIn:", {})
    elseif device.platform == "android" then
        self._sdkLoginType = XH.SDK_LOGIN_TYPE.SDK_WX
        local wxappId = XH.areaData:getWXAppID()
        XH.sdkManager:callFunctionWithMap(XH.ThirdDefine.SDK_INTERFACE_NAMES_KEY.WX_LOGIN, { wxappId }, false)
    else
        XH.TipTool.hideLoading()
    end
end

--微信绑定
function LoginModule:reqWXBind()
    if device.platform == "android" then
        self._isReqWXBind = true
        self:reqWXLogin()
        return
    end
    self:setSDKConfig()
    XH.TipTool.showLoading()
    local target = cc.Application:getInstance():getTargetPlatform()
    if target == cc.PLATFORM_OS_IPHONE or target == cc.PLATFORM_OS_IPAD then
        self._isReqWXBind = true
        self._sdkLoginType = XH.SDK_LOGIN_TYPE.SDK_WX
        XH.sdkManager:xhCallOCFunc("XHWXLogin", "weixinLoginIn:", {})
    else
        XH.TipTool.hideLoading()
    end
end

--个验登录
function LoginModule:reqGYLogin()
    XH.TipTool.showLoading()
    local target = cc.Application:getInstance():getTargetPlatform()
    if target == cc.PLATFORM_OS_IPHONE or target == cc.PLATFORM_OS_IPAD then
        local sessoinTime = XH.loginData:getLocalSessionTime()
        if sessoinTime + KW_GY_SESSION_TIME > os.time() then
            self:sessionLogin()
        else
            if XH.StringTool.compareVersions(XH.SysTool:GetBundleVersion(), "1.0.18") < 0 then
                XH.TipTool.hideLoading()
                XH.TipTool.showToast("当前版本不支持个手机一键登录，请到应用商店下载最新版本")
                return
            end
            self._sdkLoginType = XH.SDK_LOGIN_TYPE.SDK_GY
            if XH.SysTool:GetBundleVersion() >= "1.1.9" and XH.configerManager:getIsJgSdk() then
                local getToponInfo = {
                    ["functionName"] = "jg_onetap_login",
                }
                XH.sdkManager:xhCallOCFunc("UserInterface", "callFunction", getToponInfo)
            else
                XH.sdkManager:xhCallOCFunc("XHGeYan", "gy_onetap_login")
            end
        end
    elseif device.platform == "android" then
        local sessoinTime = XH.loginData:getLocalSessionTime()
        if sessoinTime + KW_GY_SESSION_TIME > os.time() then
            self:sessionLogin()
        else
            if not XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_PERMISSION_PHONE_STATE, false) then
                local size = cc.Director:getInstance():getWinSize()
                XH.TipTool.showToast("设备信息权限：用于区分玩家的唯一性和账号登陆", 2, cc.p(size.width/2,size.height - 80))
                XH.SysTool.performDelayOnce(function()
                    XH.sdkManager:callFunction("user_initiative")
                    local args = {
                        ["0"] = XH.sdkManager.AndroidPermission.READ_PHONE_STATE,
                    }
                    XH.sdkManager:setCurrnetCheckPermission(XH.sdkManager.AndroidPermission.READ_PHONE_STATE)
                    XH.sdkManager:callFunctionWithMap("add_permission",args)
                end, 0.1)
            end
            
            XH.SysTool.performDelayOnce(function()
                self._sdkLoginType = XH.SDK_LOGIN_TYPE.SDK_GY
                if XH.SysTool:GetBundleVersion() >= "1.1.9" and XH.configerManager:getIsJgSdk() then
                    XH.sdkManager:callFunction(XH.ThirdDefine.SDK_INTERFACE_NAMES_KEY.JG_LOGIN)
                else
                    XH.sdkManager:callFunction(XH.ThirdDefine.SDK_INTERFACE_NAMES_KEY.GY_LOGIN)
                end
            end, 0.2)
        end
    else
        XH.TipTool.hideLoading()
    end
end

--获取个验信息
function LoginModule:reqGYInfo(isOnlyBindPhone)
    local target = cc.Application:getInstance():getTargetPlatform()
    if target == cc.PLATFORM_OS_IPHONE or target == cc.PLATFORM_OS_IPAD then
        if XH.SysTool:GetBundleVersion() == "1.1.9" then
            if isOnlyBindPhone then
                XH.viewManager:openView("PhoneBindView")
            else
                self:loginFinished()
            end
            return
        end
        if XH.StringTool.compareVersions(XH.SysTool:GetBundleVersion(), "1.0.18") < 0 then
            self:loginFinished()
            return
        end
        self._isReqGYInfo = true
        self._onlyBindPhone = isOnlyBindPhone or false
        self._sdkLoginType = XH.SDK_LOGIN_TYPE.SDK_GY
        if XH.SysTool:GetBundleVersion() >= "1.1.9" and XH.configerManager:getIsJgSdk() then
            local getToponInfo = {
                ["functionName"] = "jg_onetap_login",
            }
            XH.sdkManager:xhCallOCFunc("UserInterface", "callFunction", getToponInfo)
        else
            XH.sdkManager:xhCallOCFunc("XHGeYan", "gy_onetap_login")
        end
    elseif target == cc.PLATFORM_OS_ANDROID then
        --原先的代码没有对android进行处理,20201102追加
        self._isReqGYInfo = true
        self._onlyBindPhone = isOnlyBindPhone or false
        self._sdkLoginType = XH.SDK_LOGIN_TYPE.SDK_GY
        if XH.SysTool:GetBundleVersion() >= "1.1.9" and XH.configerManager:getIsJgSdk() then
            XH.sdkManager:callFunction(XH.ThirdDefine.SDK_INTERFACE_NAMES_KEY.JG_LOGIN)
        else
            XH.sdkManager:callFunction(XH.ThirdDefine.SDK_INTERFACE_NAMES_KEY.GY_LOGIN)
        end
    else
        self:loginFinished()
    end
end

function LoginModule:reqPhoneBind(thirdid, phoneNumber)
    self:startReq("ReqBindUser", nil, nil, thirdid, phoneNumber, nil, XH.areaData:getSrsGroupID(), 10)
end

function LoginModule:onReqPhoneBind(req, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        XH.lobby:getModule("TabsActivity"):reqPhoneBindActBindState()
        if self._onlyBindPhone then
            -- 请求玩家手机绑定状态
            XH.playerData:flushPlayerPhoneBindState()
        else
            self:loginFinished()
        end
    else
        if not self._onlyBindPhone then
            self:loginFinished()
        end
    end
    self._onlyBindPhone = false
end

function LoginModule:onLoginCallback(event)
    local code = event.data.code
    local msg = event.data.msg
    local userid = ""
    local phoneNumber = ""
    if code == XH.ThirdDefine.LOGIN_CALLBACK_CODE.ACTION_RET_LOGIN_SUCCESS then
        local data = cjson.decode(msg)
        userid = data.uid
        local token = data.session
        local nickname = data.username
        local password = token.."|"..1010 .."|"..1432
        if self._isReqGYInfo then
            phoneNumber = userid
            local keyPos = string.find(phoneNumber, "_")
            if keyPos then
                phoneNumber = string.sub(phoneNumber, keyPos + 1)
            end
            for i = 1, #phoneNumber do
                local char = string.byte(phoneNumber, i, i)
                if char < 48 or char > 57 then
                    phoneNumber = ""
                    break
                end
            end
            if #phoneNumber < 11 then
                phoneNumber = ""
            end
        else
            if not self._isReqWXBind then
                XH.playerData:setPid(data.pid)
                XH.playerData:setWeChatURL(data.headimgurl or "")
                XH.playerData:setThirdNickName(nickname)
                XH.playerData:setUnionId(msg.unionid)
        
                if self._bReqLoginStatus then
                    return
                end
                self._bReqLoginStatus = true
            end
            local srsGroupID = XH.areaData:getSrsGroupID()
            XH.netEngine:removeSRS(srsGroupID)
            local userType = XH.SRSProtocol.PlayerConnect.USERTYPE.DEVELOPER
            local loginName = userid
            local loginPasswd = password        
            local isReqBind = self._isReqWXBind
            local sdkType = self._sdkLoginType
            XH.SysTool.performDelayOnce(function ()
                self:reqPlayerLogin(userType,loginName,loginPasswd,isReqBind,sdkType,msg)
            end,0.001)
        end
    elseif code == XH.ThirdDefine.LOGIN_CALLBACK_CODE.ACTION_RET_LOGIN_FAIL or code == XH.ThirdDefine.LOGIN_CALLBACK_CODE.ACTION_RET_GYLOGIN_FAIL then
        XH.TipTool.hideLoading()
        if self._sdkLoginType == XH.SDK_LOGIN_TYPE.SDK_GY then
            if self._onlyBindPhone then
                XH.viewManager:openView("PhoneBindView")
            else
                XH.viewManager:openView("PhoneLoginView")
            end
        end
    elseif code ~= XH.ThirdDefine.LOGIN_CALLBACK_CODE.ACTION_RET_INIT_SUCCESS and code ~= XH.ThirdDefine.LOGIN_CALLBACK_CODE.ACTION_RET_INIT_FAIL then
        XH.TipTool.hideLoading()
    elseif code == XH.ThirdDefine.LOGIN_CALLBACK_CODE.ACTION_RET_LOGIN_CANCEL then
        XH.TipTool.hideLoading()
    end
    if self._isReqGYInfo then
        if phoneNumber == "" then
            if not self._onlyBindPhone then
                self:loginFinished()
            -- else
            --     XH.lobby:getModule("TabsActivity"):reqPhoneBindActBindState()
            end
            self._onlyBindPhone = false
        else
            XH.lobby:getModule("TabsActivity"):reqPhoneBindActBindState()
            self:reqPhoneBind(userid, phoneNumber)
        end
    end
    self._isReqWXBind = false
    self._isReqGYInfo = false
end

function LoginModule:loginFinished()
    XH.TipTool.hideLoading()
    self:gotoLobby()
    self:dealAfterLoginReq()
end

function LoginModule:initThrowDataSDK()
    local initParam = {}
    initParam["site"] = "S005"
    initParam["userId"] = tostring(XH.areaData:getAreaID() or 0) .. "_" .. tostring(XH.playerData:getNumberID() or 0)
    initParam["device_id"] = XH.SysTool:GetDevid()
    initParam["os"] = device.platform
    initParam["os_version"] = un.Device.getOsName()
    initParam["app_channel"] = tostring(XH.SysTool:getChannelID() or 0)
    initParam["isp"] = XH.playerData:getSP()
    initParam["app_version"] = XH.SysTool.GetBundleVersion()
    initParam["app_name"] = "浙江游戏大厅"
    initParam["network"] = un.Device.getNetEnvName()
    initParam["app_package"] = "com.xm.zjgamecenter"
    initParam["res_version"] = XH.SysTool.GetResVersion()
    XH.dhsdk:init(initParam)

    -- 数数
    XH.TDSDK:setSuperProperties(
    {
        channel_id = XH.SysTool:getChannelID(),
        area_id = XH.areaData:getAreaID(),
        tenant_id = XH.areaData:getTenantid(),
    }
    )
    XH.TDSDK:setUserId(tostring(XH.areaData:getAreaID() or 0) .. "_" .. tostring(XH.playerData:getNumberID() or 0))
end

function LoginModule:dealAfterLoginReq()
    XH.TipTool.resetLoading()
    self._nRlinkCount = 0
    --请求停服公告
    self:reqStopSvrStatus()
    -- 登录重置玩家邮件数据
    XH.lobby:getModule("Mail"):resetMailModule()
    -- 请求邮件状态
    XH.lobby:getModule("Mail"):reqCheckNewMail()

    --请求玩家位置
    --请求玩家金币
    XH.playerData:flushGoldCoin()
    --请求玩家道具
    XH.playerData:flushPlayerDrop()
    -- 请求task相关
    XH.taskManager:reqTaskAfterLogin()
    cc.UserDefault:getInstance():setBoolForKey("KW_RN_TOURIST_LOGIN_GAME_OVER",false)
    --请求玩家游客状态
    if XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_RN_TOURIST_LOGIN) then
        XH.playerData:reqVisitorHeart()
    else
        --请求玩家身份认证
        XH.playerData:flushPlayerDefendIdentity()
        XH.playerData:stopVisitorHeart()
    end
    --请求玩家手机绑定状态
    XH.playerData:flushPlayerPhoneBindState()
    XH.playerData:flushPlayerPosition()
	--请求是否是用户
	XH.playerData:flushAgentFlag()

    -- 请求玩家是否绑定用户
    XH.playerData:flushBindInvateCode()

    -- 1006服务需要设置头像昵称
    self:reqSetHeadAndNickname()

    -- 请求IM服务appid
    if XH.areaData:isSupportFriendList() then
        XH.lobby:getModule("Im"):reqAppID()
    end

    -- 请求新背包服务appid
    if XH.areaData:isSupportNewBagSystem() then
        XH.lobby:getModule("BagSysNew"):reqAppID()
    end

    -- 请求商城前置信息
    XH.lobby:getModule("Shop"):reqShopPageMarkTop()

    if LoginConfig.ReuiteUserData and LoginConfig.ReuiteUserData[XH.areaData:getLobbyID()] then 
        self:reqReuiteUserDataByTask(LoginConfig.ReuiteUserData[XH.areaData:getLobbyID()])
    end
    -- 启动比赛场监听和重置比赛场内和区相关的数据
    self:teahouseMonitorandResetData()

    self:sendUpdateLog()

    --拉取策略srs组
    self:loadRemoteSrs()

    self:dispatchEvent({name = LoginModule.LOGIN_EVENT_SUCCESS})

    XH.goldConfigManager:resetGoldConfig()

    XH.sdkManager:reqGeTuiClientId()

    XH.sdkManager:setCurrnetCheckPermission(XH.sdkManager.AndroidPermission.ACCESS_LOCATION)
    XH.sdkManager:callFunctionWithMap("check_has_permission",{["0"] = XH.sdkManager.AndroidPermission.ACCESS_LOCATION})
    -- ios15需要手动初始化Topon，避免权限弹窗未弹出
    if device.platform == "ios" and XH.SysTool:CompareToBundleVersion("1.3.06") then
        XH.sdkManager:xhCallOCFunc("UserInterface", "callFunction", {["functionName"] = "topon_init"})
    end
    self:onTalkingDataLogin()

    -- if XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_RRBY_AFTER_UPDATE, false) then
    --     XH.lobby:getModule("RRBuYu"):checkUpdateStatus()
    -- end
    
    -- 查询插件列表
    if XH.lobby:getModule("Replugin"):isSupportRePlugin() then
        local data = {}
        data.user_id = XH.playerData:getNumberID()
        data.cpsid = 1354
        if device.platform == "ios" then
            data.cpsid = 10215
        end
        XH.lobby:getModule("Replugin"):getPluginsInfo(json.encode(data))
    end

    self:pushReYunLoginSuccess()

    local ConfigurationDefine = require("lobby.Modules.Configuration.Define")
    XH.lobby:getModule("Configuration"):reqConfigDataMd5(ConfigurationDefine.Config.ALL)
    XH.lobby:getModule("Configuration"):reqConfigDataMd5(ConfigurationDefine.Config.LOBBY)
    XH.lobby:getModule("Configuration"):reqConfigDataMd5(ConfigurationDefine.Config.GOLD)
    -- XH.lobby:getModule("GoldLaunch"):reqGiftInfo(GoldLaunchConfig.Type.LOBBY)
    -- 请求反诈接口
    XH.lobby:getModule("TabsActivity"):reqFanZhaGiftInfo()
end

function LoginModule:pushReYunLoginSuccess()
    if XH.sdkManager:checkCanUseReYun() then
        local jsonData = {}
        jsonData.accountId = XH.areaData:getAreaID() .. "_" .. XH.playerData:getNumberID()
        local jsonStr = json.encode(jsonData)
        if self._isNewPlayer then
            XH.sdkManager:callFunctionWithMap("trackingio_set_registerwithaccountid",{["0"]=jsonStr})
            self._isNewPlayer = false
        end
        XH.sdkManager:callFunctionWithMap("trackingio_set_loginsuccessbusiness",{["0"]=jsonStr})
    end
    if XH.sdkManager:checkCanUseReYunIOS() then
        local funcName = "trackingio_set_loginsuccessbusiness"
        if self._isNewPlayer then
            funcName = "trackingio_set_registerwithaccountid"
            self._isNewPlayer = false
        end
        local info = {
            ["functionName"] = funcName,
            ["args"] =
            {
                ["accountId"] = XH.areaData:getAreaID() .. "_" .. XH.playerData:getNumberID(),
            }
        }
        XH.sdkManager:xhCallOCFunc("UserInterface", "callFunction", info)
    end
end

function LoginModule:initRemoteData()
    self._remoteList = {}       --远程策略组srs，key为区srsGroupID
    self._curRespRSAddrIndex = 0  --当前拉取策划srs索引
end

--拉取策略srs组
function LoginModule:loadRemoteSrs()
    self:initRemoteData()
    self:sendReqSRSLoad()
end

function LoginModule:sendReqSRSLoad()
    local reqSRSLoad = XH.SRSProtocol.ReqSRSLoad:new()
    reqSRSLoad.nAppID = 0
    local srsGroupId = XH.areaData:getSrsGroupID()
    XH.netEngine:sendProtocol(reqSRSLoad,XH.SRSProtocol.ReqSRSLoad.processid,0,srsGroupId)
end

function LoginModule:onRespSRSLoad(event)
    local respData =  XH.SRSProtocol.RespSRSLoad:new()
    respData:bistream(event.msg.buff, event.msg.len)

    local wid = XH.areaData:getSrsGroupID() or 0
    self._remoteList[wid] = {}
    if respData.nCount > 0 then
        self._remoteList[wid] = respData.SRSLoad
        for i=1,respData.nCount do
            self:requestSRSAddr(i)
        end
    end
end

function LoginModule:requestSRSAddr(i)
    local wid = XH.areaData:getSrsGroupID() or 0
    if not self._remoteList[wid][i] or not self._remoteList[wid][i].nAppID then
        return
    end
    self._remoteList[wid][i].szIP = ""
    self._remoteList[wid][i].sPort = 0
    local reqSRSAddr = XH.SRSProtocol.ReqSRSAddr:new()
    reqSRSAddr.nAppID = self._remoteList[wid][i].nAppID
    local srsGroupId = XH.areaData:getSrsGroupID()
    XH.netEngine:sendProtocol(reqSRSAddr,XH.SRSProtocol.ReqSRSAddr.processid,0,srsGroupId)
end

function LoginModule:onRespSRSAddr(event)
    local respData =  XH.SRSProtocol.RespSRSAddr:new()
    respData:bistream(event.msg.buff, event.msg.len)

    local wid = XH.areaData:getSrsGroupID() or 0
    self._curRespRSAddrIndex = self._curRespRSAddrIndex + 1

    if self._remoteList[wid] then
        for i=1, #self._remoteList[wid] do
            if self._remoteList[wid][i].nAppID == respData.nAppID then
                self._remoteList[wid][i].szIP = respData.szIP
                self._remoteList[wid][i].sPort = respData.sPort
                break
            end
        end

        --请求到全部策略srs，一次性写入文件中
        local len = #self._remoteList[wid]
        if self._curRespRSAddrIndex == len  then
            local srsText = json.encode(self._remoteList[wid])
            local txtFile = io.open(string.format("%s%s%d.json",cc.FileUtils:getInstance():getWritablePath(),"srslist", wid),"wb")
            if txtFile then
                txtFile:write(srsText)
                txtFile:close()
            end
        end
    end
end

function LoginModule:onReportNewPlayer(event)
    local respData =  XH.AuthProtocol.ReportNewPlayer:new()
    respData:bistream(event.msg.buff, event.msg.len)
    self._isNewPlayer = true
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.LOGIN_REGISTER_SUCCESS, nil, {user_ip=un.Device.getIp()})
end

function LoginModule:clearLoginStates()
    XH.netEngine:removeAllSRS()
    self._loginStates = LOGIN_STATUS.LOGIN_NOTLOGIN
    -- XH.loginData:clearSessionLoginInfo()
    XH.loginData:cleanAllAreasSessionLoginInfo()
end

function LoginModule:changeUser()
    XH.playerData:stopVisitorHeart()
    local isShowYouKe = false
    if XH.playerData:getPlayerIsAgent() and XH.areaData:getAgentShowYouKe() then
        isShowYouKe = true
    end
    self:clearLoginStates()
    XH.Controller:getInstance():enterLogin(isShowYouKe)
end

--修改玩家数据格式
function LoginModule:changePlayerData(data)
    XH.playerData:setPlayerData(data)
    if XH.bugly and data then
        local userId = data.playerData.areaid .. "-" .. data.playerData.numid
        XH.bugly.setUserId(userId)
        XH.bugly.setUserData(userId, "LoginSuccess")
    end
    self:initThrowDataSDK()
end

--检查登录是否正常
function LoginModule:checkLoginNormal()
    if self._loginStates == LOGIN_STATUS.LOGIN_SUCCESS then
        return true
    end
    return false
end

function LoginModule:reqSetHeadAndNickname()
    local type = XH.MatchLinkProtocol.ReqGetOrSetHead.TYPE.TYPE_SET
    local areaID = XH.playerData:getBrandID()
    local numID = XH.playerData:getNumberID()
    local head = XH.playerData:getWeChatURL()
    local nickname = XH.playerData:getNickName()
    local srsGroupID = XH.areaData:getSrsGroupID()
    self:startReq("ReqSetHeadAndNickname", type, areaID, numID, head, nickname, srsGroupID, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function LoginModule:onReqSetHeadAndNickname(cell, type, data)

end

function LoginModule:onPlayerInfoChanged(event)
    local data = event.msg.data
    if not data then
        self:loginFinished()
        return
    end
    if data.isReq then
        return
    end
    if data.isBindPhone then
        self:reqGYInfo()
    else
        self:loginFinished()
    end
end

-- 启动比赛场监听和重置比赛场内和区相关的数据
function LoginModule:teahouseMonitorandResetData()
    XH.teaHouseManager:reset()
end

--发送游戏运营日志
function LoginModule:sendUpdateLog()
    local clientInfo = XH.ToolProtocol.ClientInfo:new()
    clientInfo.szMachineType = XH.SysTool:GetDevid()
    clientInfo.szOsVer = XH.SysTool:GetOsVersion()
    clientInfo.szDeviceType = device.platform .. ",BundleVersion:" .. XH.SysTool:GetBundleVersion()
    clientInfo.szPackageName = "com.xm.zjgamecenter"
    clientInfo.channelid = XH.SysTool:getChannelID()
    clientInfo.lobbyid = XH.areaData:getLobbyID()
    clientInfo.lobbystate = XH.userDefault:getValue(XH.userDefault.KEY_ID.HOTUPDATE_STATES, -1, {key = "Lobby"})
    clientInfo.szLobbyPreVer = XH.userDefault:getValue(XH.userDefault.KEY_ID.OLD_VWESION,"", {key = "Lobby"})
    clientInfo.szLobbyCurrVer = XH.userDefault:getValue(XH.userDefault.KEY_ID.NEW_VWESION,"", {key = "Lobby"})
    XH.netEngine:sendProtocol(clientInfo, XH.ToolProtocol.ClientInfo.processid, 0, XH.areaData:getSrsGroupID())
end

--设置选择区的 唯一标识
function LoginModule:enterLobby(lobbyID)
    if lobbyID then
        XH.loginData:setLobbyID(lobbyID)
        XH.areaData:setLobbyID(lobbyID)
    end
end

--销毁时执行的方式
function LoginModule:destoryManager()
end

function LoginModule:reqNeedShowFreeQuan()
    self:startReq("ReqNeedShowFreeQuan", XH.playerData:getNumberID(),0,XH.TaskProtocol.ReqTaskProtocol.processid,9908,XH.areaData:getSrsGroupID(),XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function LoginModule:onReqTaskPlayerTagCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local result = json.decode(data.acData)
        local shopTbl = {
            ["vip.19"] = "1小时会员",
            ["vip.16"] = "1天会员",
            ["vip.20"] = "3天会员",
            ["vip.17"] = "7天会员",
            ["vip.18"] = "30天会员",
        }
        local hideList = {}
        for key, value in pairs(shopTbl) do
            if not string.find(data.acData, key) then
                table.insert(hideList, value)
            end
        end
        XH.playerData:setHideShopList(hideList)
        XH.playerData:setShowShopList(result.canBuy)
        XH.playerData:setFreeVip(false)
        if result.freeOneHour and tonumber(result.freeOneHour) == 0 then
            XH.playerData:setIsShowFreeQuan(true)
            XH.playerData:setFreeVip(true)
            -- 上新商城后标签依赖配置
            -- XH.viewManager:addCacheView("VipView")
        elseif result.isVip and tonumber(result.isVip) == 1 then
            XH.playerData:setIsShowFreeQuan(true)
            -- 上新商城后标签依赖配置
            -- XH.viewManager:addCacheView("VipView")
        else 
            XH.playerData:setIsShowFreeQuan(false)
        end
    end
end

-----------------------------------------------------------------------------------
--关于定位登录
-----------------------------------------------------------------------------------
function LoginModule:onGetLocationCallBack(event)
    local currScene = XH.SceneManager:getInstance():getTopScene()
    if currScene and currScene:getTag() ~= XH.SCENE_TAG.LOGIN then
        return
    end
    local code = event.data.code
	local msg = event.data.msg
	
	XH.TipTool.hideLoading()

    if XH.ChannelTool.checkIsCPSChannel() then
        self:cpsChannelUserLogin(false)
        return
    elseif XH.ChannelTool.checkIsWaishengChannel() then 
        self:wsChannelUserLogin(false)
        return 
    end
    if code == XH.ThirdDefine.AMAP_CALLBACK_CODE.AMAP_SUCCESS then
        local tData

        local ok, _ = pcall(function()
            tData = cjson.decode(msg)
        end)

        if not ok then
            print("cjson failed: " .. msg)
            return
        end
        
        local city = tData["city"]
        local district = tData["district"]
        
        local needShowChooseArea, name, lobbyid = XH.login:getModule("ChooseArea"):needShowChooseArea(city, district)
        if needShowChooseArea then
                XH.viewManager:openView("ChooseAreaView")
        else
            name = string.sub(name, 1, -4) 
            XH.viewManager:openView("ChooseAreaTipLayer", nil, {msg = name,lobbyid = lobbyid})
            self:enterLobby(lobbyid)
            XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_ENTER_AREA_NAME , XH.login:getModule("ChooseArea"):getAreaName(lobbyid))
        end
    else
        XH.viewManager:openView("ChooseAreaView")
	end
end

------------------老用户回馈 start --------------------------
function LoginModule:reqReuiteUserDataByTask(taskID)
    self:startReq("ReqReuiteUserDataByTask",XH.playerData:getNumberID(),0,XH.TaskProtocol.ReqTaskProtocol.processid,taskID,XH.areaData:getSrsGroupID(),XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function LoginModule:onReqTaskReuiteUserData(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local result = json.decode(data.acData)
        if data.acData == "" or #result == 0 then
            return
        end
        -- 奖励中有绑定卡，需要转化比例，才能正确显示
        local freeCardId = areaConfig[XH.userDefault:getValue(XH.userDefault.KEY_ID.LOBBYID, 0)].propList.freeRoomCardID
        for _, value in pairs(result) do
            if value.type == freeCardId then
                value.cnt = value.cnt / XH.areaData:getPropFreeRoomCardRatio()
            end
        end
        XH.playerData:setRequitePrize(result)
        XH.viewManager:addCacheView("RequiteViewView", XH.ZORDER.TOPLAYER - 1)
    end
end 
------------------老用户回馈 end --------------------------

-- 刷新登录界面按钮
function LoginModule:flushLoginState(state)
    self:dispatchEvent({name = LoginModule.EVENT_SET_LOGIN_STATE, data = state})
end

--大厅重连后不用重新断开重连的srs
function LoginModule:getSrsGroupIDOther()
    local srsGroup = {7001,5009,7160} --捕鱼srs，新金币场走自己的登录逻辑
    return srsGroup
end

--发送个推clientid给tool
function LoginModule:reqBindGeTuiClientId(clientid)
    self:startReq("ReqBindClientId", nil, nil, nil, nil, clientid, XH.areaData:getSrsGroupID(), 10)
end

function LoginModule:onReciveGeTuiDataCallBack(event)
    local code = event.data.code
    local msg = event.data.msg
    -- dump(event.data,"tangligen:<onReciveGeTuiDataCallBack>")
    if code == XH.ThirdDefine.GETUISDK_CALLBACK_CODE.GETUI_FLAG_SDK_GET_CLIENT_ID or 
        code == XH.ThirdDefine.GETUISDK_CALLBACK_CODE.GETUI_FLAG_SDK_GET_CLIENT_ID_NEW or code == XH.ThirdDefine.GETUISDK_CALLBACK_CODE.GETUI_FLAG_SDK_GET_IOS_CLIENT_ID or code == XH.ThirdDefine.GETUISDK_CALLBACK_CODE.GETUI_FLAG_SDK_GET_IOS_CLIENT_ID_NEW then
         self:reqBindGeTuiClientId(msg)
    end

end

function LoginModule:onReqBindClientId(req, type, data)

end

function LoginModule:reqJoin()
    if not XH.areaData:getAreaID() then
        return
    end
    local srsGroupID = XH.areaData:getGoldSrsGroupID()
    local reqJoin = XH.MatchLinkProtocol.ReqJoin:new()
    reqJoin.areaid = XH.playerData:getBrandID()
    reqJoin.numid = XH.playerData:getNumberID()
    XH.netEngine:sendProtocol(reqJoin, XH.MatchLinkProtocol.ReqInvite.processid, XH.areaData:getMatchLinkAppID() or 0, srsGroupID)
end

--默认选择地区
function LoginModule:cpsChannelUserLogin()
    local lobbyID = XH.ChannelTool.getCpsLoginLobbyID()
    XH.login:getModule("Login"):enterLobby(lobbyID)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_ENTER_AREA_NAME , XH.login:getModule("ChooseArea"):getAreaName(lobbyID))
    XH.SysTool.performDelayOnce(
        function ()
            XH.login:getModule("Login"):sessionLogin()
        end, 0.1)
end

--外省渠道包默认选择丽水区
function LoginModule:wsChannelUserLogin()
    local lobbyID = XH.LOBBY_ID.LISHUI
    XH.login:getModule("Login"):enterLobby(lobbyID)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_ENTER_AREA_NAME , XH.login:getModule("ChooseArea"):getAreaName(lobbyID))
    XH.SysTool.performDelayOnce(
        function ()
            XH.login:getModule("Login"):sessionLogin()
        end, 0.1)
end
--车载、浙江包渠道包默认选择channel.json对应的渠道
function LoginModule:zjChannelUserLogin(qudaoName)
    local lobbyID = XH.LOBBY_ID[qudaoName]
    XH.login:getModule("Login"):enterLobby(lobbyID)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_ENTER_AREA_NAME , XH.login:getModule("ChooseArea"):getAreaName(lobbyID))
    XH.SysTool.performDelayOnce(
        function ()
            XH.login:getModule("Login"):sessionLogin()
        end, 0.1)
end

function LoginModule:getSDKInitState()
    return self._bSDKInit
end

function LoginModule:setSDKInitState(state)
    self._bSDKInit = state
end

function LoginModule:getAgreementConfig(lobbyID,type,targetPlatform)
    self._AgreementConfig = require("lobby.Config.Qualification.Qualification_" .. lobbyID).AgreementConfigByConfID or {}
    if not self._AgreementConfig then 
        return nil
    end 
    for i = 1,#self._AgreementConfig do 
        if self._AgreementConfig[i].AgreementType == type and self._AgreementConfig[i].Platform == targetPlatform then 
            return self._AgreementConfig[i]
        end 
    end
    return nil 
end

function LoginModule:getCompanyName(lobbyID,targetPlatform)
    self._CompanyNameConfig = require("lobby.Config.Qualification.Qualification_" .. lobbyID).CompanyNameConfigByConfID or {}
    if not self._CompanyNameConfig then 
        return nil
    end 
    for i = 1,#self._CompanyNameConfig do 
        if self._CompanyNameConfig[i].DevicePlatform == targetPlatform then 
            return self._CompanyNameConfig[i].CompanyName
        end 
    end
    return nil 
end

--apple登录
function LoginModule:reqAppleLogin()
    XH.TipTool.showLoading()
    local target = cc.Application:getInstance():getTargetPlatform()
    if target == cc.PLATFORM_OS_IPHONE or target == cc.PLATFORM_OS_IPAD then
        self._sdkLoginType = XH.SDK_LOGIN_TYPE.SDK_APPLE
        XH.sdkManager:xhCallOCFunc("", "apple_login")
    else
        XH.TipTool.hideLoading()
    end
end

function LoginModule:onTalkingDataLogin()
    --添加talkingdata(在用户帐号登录成功)
    local mapData = {
        ["0"] = tostring(XH.playerData:getNumberID())
    }
    if (device.platform == "android" and XH.SysTool:CompareToBundleVersion("1.2.7") and XH.ChannelTool.checkIsSemChannel()) then
        XH.sdkManager:callFunctionWithMap("talkingdata_onLogin",mapData)
    end
    if (device.platform == "ios" and XH.SysTool:CompareToBundleVersion("1.3.13")) then
        XH.sdkManager:xhCallOCFunc("","talkingdata_onLogin",mapData)
    end
end

function LoginModule:reqStopSvrStatus()
    self:startReq("ReqStopSvrMsg", XH.areaData:getSrsGroupID(),XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function LoginModule:getSdkLoginType()
    return self._sdkLoginType
end

return LoginModuleL�  