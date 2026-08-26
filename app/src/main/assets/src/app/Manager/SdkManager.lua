--
-- sdk管理
--

local SdkManager = class("SdkManager")

SdkManager.EVENT_IAP_CALLBACK = "EVENT_IAP_CALLBACK"
SdkManager.EVENT_XHLINK_CALLBACK = "EVENT_XHLINK_CALLBACK"
SdkManager.EVENT_FASTVO_CALLBACK = "EVENT_FASTVO_CALLBACK"
SdkManager.EVENT_AMAP_CALLBACK = "EVENT_AMAP_CALLBACK"
SdkManager.EVENT_SHAREDSDK_CALLBACK = "EVENT_SHAREDSDK_CALLBACK"
SdkManager.EVENT_LOGIN_CALLBACK = "EVENT_LOGIN_CALLBACK"
SdkManager.EVENT_OPENURL_CALLBACK = "EVENT_OPENURL_CALLBACK"
SdkManager.EVENT_DEFAULT_CALLBACK = "EVENT_DEFAULT_CALLBACK"
SdkManager.EVENT_TOPONSDK_CALLBACK = "EVENT_TOPONSDK_CALLBACK"
SdkManager.EVENT_TOPONSDK_CLOSE_GOLD = "EVENT_TOPONSDK_CLOSE_GOLD"
SdkManager.EVENT_TOPONSDK_BANNER_CALLBACK = "EVENT_TOPONSDK_BANNER_CALLBACK"
SdkManager.EVENT_GETUISDK_CALLBACK = "EVENT_GETUISDK_CALLBACK"
SdkManager.EVENT_H5GAME_CALLBACK = "EVENT_H5GAME_CALLBACK"
SdkManager.EVENT_ADDPERMISSION_CALLBACK = "EVENT_ADDPERMISSION_CALLBACK"
SdkManager.EVENT_ZFB_REALNAME_CALLBACK = "EVENT_ZFB_REALNAME_CALLBACK"
SdkManager.EVENT_GETORDER_CALLBACK = "EVENT_GETORDER_CALLBACK"
SdkManager.EVENT_LEBIAN_CALLBACK = "EVENT_LEBIAN_CALLBACK"
SdkManager.EVENT_APPLE_REVOKE_SESSION_CALLBACK = 'EVENT_APPLE_REVOKE_SESSION_CALLBACK'
SdkManager.EVENT_BAIDU_OCPC_CALLBACK = 'EVENT_BAIDU_OCPC_CALLBACK'

SdkManager.AndroidPermission = {
    ACCESS_LOCATION = "6;7",            --允许获位置
    RECORD_AUDIO = "8",                 --允许程序录制声音通过手机或耳机的麦克
    READ_PHONE_STATE = "9",             --允许访问电话状态
    WRITE_EXTERNAL_STORAGE = "23",      --允许写入外部存储
}

function SdkManager:ctor()
    cc(self):addComponent("components.behavior.EventProtocol"):exportMethods()

    self._deviceId = ""
    self._sdkCallBackListOld = { }
    self._isInstalledWeChat = false
    if device.platform == "android" then
        self._isInstalledWeChat = true
    end

    self._toponAdList = {}
    self._isLoadToponAd = false
    self._loadToponList = {
        [XH.ThirdDefine.TOPONSDK_AD_TYPE.REWARDEVIDEO] = false,
        [XH.ThirdDefine.TOPONSDK_AD_TYPE.BANNER] = false,
    }

    self:initEvents()
end

-- 初始化监听事件
function SdkManager:initEvents()
    local target = cc.Application:getInstance():getTargetPlatform()
    if target == cc.PLATFORM_OS_IPHONE or target == cc.PLATFORM_OS_IPAD or target == cc.PLATFORM_OS_ANDROID then
        xh.XHBridge:setListener(handler(self, self.onSdkCallBack))
    end
end

function SdkManager:xhCallOCFunc(cls, funcName, data)
    local target = cc.Application:getInstance():getTargetPlatform()
    if target == cc.PLATFORM_OS_IPHONE or target == cc.PLATFORM_OS_IPAD or target == cc.PLATFORM_OS_ANDROID then
        if device.platform == "ios" and XH.SysTool:CompareToBundleVersion("1.3.06") and funcName and funcName == "weixinLoginIn:" then
            data = {
                ["WXSUP_WX_APPID"] = XH.areaData:getWXAppID(),
                ["WXSUP_WX_ULINK"] = XH.areaData:getUniversalLink()
            }
        end
        if funcName and funcName == "gy_onetap_login" then
            if device.platform == "ios" and XH.SysTool:CompareToBundleVersion("1.3.08") then
                funcName = "yidonglogin_login"
            end
            if device.platform == "android" and XH.SysTool:CompareToBundleVersion("1.2.4") then
                funcName = "yidonglogin_login"
            end
        end
        xh.XHBridge:xhCallOCFunc(cls, funcName, json.encode(data))
    end
end

function SdkManager:xhCallAppleSessionOCFunc(cls, funcName, data)
    local target = cc.Application:getInstance():getTargetPlatform()
    if target == cc.PLATFORM_OS_IPHONE or target == cc.PLATFORM_OS_IPAD then
        if XH.SysTool:CompareToBundleVersion("1.3.20") and funcName and funcName == "apple_revoke_session" then
            xh.XHBridge:xhCallOCFunc(cls, funcName, json.encode(data))
        end
    else
        self:onAppleRevokeSessionCallback(XH.ThirdDefine.APPLE_REVOKE_SESSION_CALLBACK_CODE.RET_APPLE_REVOKE_SESSION_SUCCESS, '')
    end
end

-- 设置区域后台appId
function SdkManager:setGameAppId(gameAppId)
    self._gameAppId = gameAppId
    -- 调用XH后台插件更新appId
end

function SdkManager:getGameAppId()
    return self._gameAppId
end

function SdkManager:setNotSupportPayResultCallBack(isNotSupportPayResultCallBack)
    self._isNotSupportPayResultCallBack = isNotSupportPayResultCallBack 
end

function SdkManager:isNotSupportPayResultCallBack()
    return self._isNotSupportPayResultCallBack or false 
end

-- 统一回调
function SdkManager:onSdkCallBack(code, msg)
    if self:hasIndexOf(XH.ThirdDefine.IAP_PAYRESULT, code) then
        -- iap 200~208
        self:onIapPayResult(code, msg)
    elseif self:hasIndexOf(XH.ThirdDefine.LOGIN_CALLBACK_CODE, code) then
        self:onLoginCallback(code, msg)
    elseif self:hasIndexOf(XH.ThirdDefine.XHLINK_CALLBACK_CODE, code) then
        self:onXhlinkCallback(code, msg)
    elseif self:hasIndexOf(XH.ThirdDefine.AMAP_CALLBACK_CODE, code) then
        self:onAMapCallback(code, msg)
    elseif self:hasIndexOf(XH.ThirdDefine.FASTVO_CALLBACK_CODE, code) then
        self:onFastvoCallback(code, msg)
    elseif self:hasIndexOf(XH.ThirdDefine.SHAREDSDK_CALLBACK_CODE, code) then
        self:onSharedSdkCallback(code, msg)
--    elseif self:hasIndexOf(XH.ThirdDefine.OPENURL_CALLBACK_CODE, code) then
--        self:onOpenUrlCallBack(code, msg)
    elseif self:hasIndexOf(XH.ThirdDefine.TOPONSDK_CALLBACK_CODE, code) then
        self:onToponSdkCallback(code, msg)
    elseif self:hasIndexOf(XH.ThirdDefine.GETUISDK_CALLBACK_CODE, code) then
        self:onGeTuiSdkCallback(code, msg)
    elseif  self:hasIndexOf(XH.ThirdDefine.H5GAME_CALLBACK_CODE, code) then
        self:onH5GameCloseCallback(code, msg)
    elseif  self:hasIndexOf(XH.ThirdDefine.ADD_PERMISSION_CALLBACK_CODE, code) then
        self:onAddPermissionCallback(code, msg)
    elseif self:hasIndexOf(XH.ThirdDefine.ZFB_REALNAME_CALLBACK_CODE, code) then
        self:onZhiFuRealNameCallback(code, msg)
    elseif self:hasIndexOf(XH.ThirdDefine.GET_ORDERID_CALLBACK_CODE, code) then 
        self:onGetOrderIdCallback(code, msg)
    elseif self:hasIndexOf(XH.ThirdDefine.LEIBIAN_CALLBACK_CODE, code) then 
        self:onLebianCallback(code, msg)
    elseif self:hasIndexOf(XH.ThirdDefine.APPLE_REVOKE_SESSION_CALLBACK_CODE, code) then
        self:onAppleRevokeSessionCallback(code, msg)
    elseif self:hasIndexOf(XH.ThirdDefine.BAIDU_OCPC_SDK_CALLBACK_CODE,code) then
        self:onBaiduOcpcCallback(code, msg)
    elseif self:hasIndexOf(XH.ThirdDefine.XH_DEVICE_ID_CALLBACK_CODE,code) then
        self:onDeviceIdCallback(code, msg)
    elseif self:hasIndexOf(XH.ThirdDefine.XH_SYSFUNC_SDK_CODE,code) then
        self:onSysfuncSdkCallback(code, msg)
    else
        self:onDefaultCallback(code, msg)
    end

    self:onDealOldCallback(code, msg)
end

function SdkManager:onIapPayResult(code, msg)
    if self:isNotSupportPayResultCallBack() then
        return
    end
    self:dispatchEvent( { name = self.EVENT_IAP_CALLBACK, data = { code = code, msg = msg } })
end

function SdkManager:onLoginCallback(code, msg)
    if code == XH.ThirdDefine.LOGIN_CALLBACK_CODE.ACTION_GET_WECHAT_INSTALLED_SUCCESS or 
    code == XH.ThirdDefine.LOGIN_CALLBACK_CODE.ACTION_GET_WECHAT_INSTALLED_SUCCESS_NEW then
        self._isInstalledWeChat = true
    elseif code == XH.ThirdDefine.LOGIN_CALLBACK_CODE.ACTION_RET_INIT_SUCCESS then
        require("app.Launcher"):getInstance():gotoStartLaunch()
    end
    self:dispatchEvent( { name = self.EVENT_LOGIN_CALLBACK, data = { code = code, msg = msg } })
end

function SdkManager:onH5GameCloseCallback(code, msg)
    self:dispatchEvent( { name = self.EVENT_H5GAME_CALLBACK, data = { code = code, msg = msg } })
end

function SdkManager:onXhlinkCallback(code, msg)
    self:dispatchEvent( { name = self.EVENT_XHLINK_CALLBACK, data = { code = code, msg = msg } })
end

function SdkManager:onAMapCallback(code, msg)
    self:stopAmapSchedule()
    -- XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_MAP_POSITION_STATE, true)
    if code then
        if msg and type(msg) == "string" then
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.KW_COMMON, {label = "定位返回" .. code}, { info = msg })
        else
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.KW_COMMON, {label = "定位异常" .. code}, { info = "异常" })
        end
    else
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.KW_COMMON, {label = "无定位"}, { info = "无" })
    end
    -- 如果code是失败的话，判断本地是否已经有了
    if code ~= XH.ThirdDefine.AMAP_CALLBACK_CODE.AMAP_SUCCESS then
        local localLongitude, localLatitude, city = XH.playerData:getServerLocation()
        if localLongitude and localLatitude then
            local ret
            local info = {
                longitude = localLongitude,
                latitude = localLatitude,
                country = city or "",
            }
            local ok, msgEx = pcall(function()
                ret = cjson.encode(info)
            end)
            if ok and ret then
                code = XH.ThirdDefine.AMAP_CALLBACK_CODE.AMAP_SUCCESS
                msg = ret
            end
        else
            if device.platform == "ios" then
                self._bNotGpsServer = false
            end
            if device.platform == "ios" and msg and type(msg) == "string" and string.find(msg, "locationServicesDisabled") then
                self._bNotGpsServer = true
            end
        end
    end

    self:dispatchEvent({ name = self.EVENT_AMAP_CALLBACK, data = { code = code, msg = msg } })
end

function SdkManager:dispatchAmap(code, msg)
    self:dispatchEvent({ name = self.EVENT_AMAP_CALLBACK, data = { code = code, msg = msg } })
end

function SdkManager:onFastvoCallback(code, msg)
    self:dispatchEvent( { name = self.EVENT_FASTVO_CALLBACK, data = { code = code, msg = msg } })
end

function SdkManager:onSharedSdkCallback(code, msg)
    self:dispatchEvent( { name = self.EVENT_SHAREDSDK_CALLBACK, data = { code = code, msg = msg } })
end

function SdkManager:onToponSdkCallback(code, msg)
    self:dispatchEvent( { name = self.EVENT_TOPONSDK_CALLBACK, data = { code = code, msg = msg, adName = (#self._toponAdList > 0 and self._toponAdList[1].adName or "") } })
    --XH.TipTool.showTip( {type = XH.TIP_LAYER_TYPE.OK,} , "topl:" .. tostring(self._toponAdList[1].adName) .. " code:" .. code .. " type:" .. tostring(self._toponAdList[1].showType) .. " no:" .. #self._toponAdList)
    if (code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_LOADSUCCESS) then
        if #self._toponAdList > 0 then
            XH.sdkManager:setToponCanShow(self._toponAdList[1].adName, true)
            if self._toponAdList[1].showType == "show" then
                self._isLoadToponAd = false
                self:startToponAds()
            else
                table.remove(self._toponAdList, 1)
                self._isLoadToponAd = false
                self:startToponAds()
            end
        end
    elseif (code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_HAVELOAD) then
        if #self._toponAdList > 0 then
            XH.sdkManager:setToponCanShow(self._toponAdList[1].adName, true)
            if self._toponAdList[1].showType == "show" then
                self._isLoadToponAd = false
                self:startToponAds()
            else
                table.remove(self._toponAdList, 1)
                self._isLoadToponAd = false
                self:startToponAds()
            end
        end
    --elseif (code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_JLSUCCESS) then
    elseif (code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_CLOSE) then
        if #self._toponAdList > 0 then
            if self._toponAdList[1].adName == XH.ThirdDefine.TOPONSDK_AD_TYPE.REWARDEVIDEO then
                local adName = self._toponAdList[1].adName
                table.remove(self._toponAdList, 1)
                self._isLoadToponAd = false
                self:addToponAds(adName)
            elseif self._toponAdList[1].adName == XH.ThirdDefine.TOPONSDK_AD_TYPE.BANNER and self._toponAdList[1].cnt > 0 then
                table.remove(self._toponAdList, 1)
                self._isLoadToponAd = false
                self:startToponAds()
            else
                table.remove(self._toponAdList, 1)
                self._isLoadToponAd = false
                self:startToponAds()
            end
        end
    elseif code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_SHOWSUCCESS then
        if #self._toponAdList > 0 then
            if self._toponAdList[1].adName == XH.ThirdDefine.TOPONSDK_AD_TYPE.BANNER then
                table.remove(self._toponAdList, 1)
                self._isLoadToponAd = false
                self:startToponAds()
            end
        end
    elseif code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_LOADFAIL then
        if #self._toponAdList > 0 then
            self._isLoadToponAd = false
            self._toponAdList[1].cnt = self._toponAdList[1].cnt + 1
            self:startToponAds()
        end
    elseif code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_SHOWFAILED or code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_NOTREADY then
        if #self._toponAdList > 0 then
            table.remove(self._toponAdList, 1)
            self._isLoadToponAd = false
            self:startToponAds()
        end
    end
end

function SdkManager:onGeTuiSdkCallback(code, msg)
    local data = {}
    data.code = code
    data.msg = msg
    dump(data,"tangligen<onGeTuiSdkCallback>")
    if code == XH.ThirdDefine.GETUISDK_CALLBACK_CODE.GETUI_FLAG_SDK_GET_IOS_MSG or code == XH.ThirdDefine.GETUISDK_CALLBACK_CODE.GETUI_FLAG_SDK_GET_IOS_MSG_NEW then
        --ios个推后台推送消息后会调用到此处
        if XH.SysTool:GetBundleVersion() >= "1.1.9" and XH.configerManager:getIsJgSdk() then
            self:onReciveMsgIosGeTuiNEW(msg)
        else
            self:onReciveMsgIosGeTui(msg)
        end
    elseif code == XH.ThirdDefine.GETUISDK_CALLBACK_CODE.GETUI_FLAG_SDK_GET_NOTIFY_EVENT_ARRIVE then
        --获取安卓个推透传参数
        self:getNotifyInfo()
    elseif code == XH.ThirdDefine.GETUISDK_CALLBACK_CODE.GETUI_FLAG_SDK_GET_NOTIFY_EVENT_CLICK or code ==  XH.ThirdDefine.GETUISDK_CALLBACK_CODE.GETUI_FLAG_SDK_GET_NOTIFY_EVENT_CLICK_NEW then
        --个推 android
        if XH.SysTool:GetBundleVersion() >= "1.1.9" and XH.configerManager:getIsJgSdk() then
            self:onReciveMsgAndroidGeTuiNEW(msg)
        else
            self:onReciveMsgAndroidGeTui(msg)
        end
    elseif code == XH.ThirdDefine.GETUISDK_CALLBACK_CODE.GETUI_FLAG_SDK_GET_NOTIFY_INFO_SUCC or code == XH.ThirdDefine.GETUISDK_CALLBACK_CODE.GETUI_FLAG_SDK_GET_NOTIFY_EVENT_ARRIVE_NEW then
        self:onReciveAndroidNotify(msg)
    elseif code == XH.ThirdDefine.GETUISDK_CALLBACK_CODE.GETUI_FLAG_SDK_GET_NOTIFY_STATE_ON or code == XH.ThirdDefine.GETUISDK_CALLBACK_CODE.GETUI_FLAG_SDK_GET_NOTIFY_STATE_OFF then
        self:onReciveNotifyState(code)
    else
        self:dispatchEvent( { name = self.EVENT_GETUISDK_CALLBACK, data = { code = code, msg = msg } })
    end
end

function SdkManager:onDefaultCallback(code, msg)
    self:dispatchEvent( { name = self.EVENT_DEFAULT_CALLBACK, data = { code = code, msg = msg } })
end

function SdkManager:reqSysfunGpsSdk()
    if device.platform == "ios" then
        XH.sdkManager:xhCallOCFunc("XHsysfunc", "sysfunc_get_coordinate", {})
    else
        local localLongitude, localLatitude = XH.playerData:getServerLocation()
        if localLongitude and localLatitude then
            return
        end
        self:callFunctionWithMap("sysfunc_get_gps", { })
    end
end

function SdkManager:saveImageToGallery(picName)
    if device.platform == "ios" then
        XH.sdkManager:xhCallOCFunc("XHsysfunc", "sysfunc_save_image_to_gallery", {["0"] = picName })
    else
        self:callFunctionWithMap("sysfunc_save_image_to_gallery", {["0"] = picName })
    end
end

function SdkManager:onSysfuncSdkCallback(code, msg)
    if device.platform == "android" then
        self._bNotGpsServer = false
    end
    XH.playerData:setServerLocation()
    if code then
        if msg and type(msg) == "string" then
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.KW_COMMON, {label = "获取经纬度返回" .. code}, { info = msg })
        else
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.KW_COMMON, {label = "获取经纬度异常" .. code}, { info = "异常" })
        end
    else
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.KW_COMMON, {label = "无获取经纬度"}, { info = "无" })
    end
    if code == XH.ThirdDefine.XH_SYSFUNC_SDK_CODE.SYSFUC_ADDRESS_SUCCESS then
        local json
        local ok = pcall(function()
            json = cjson.decode(msg)
        end)
        if ok and json then
            XH.playerData:setServerLocation(json.longitude, json.latitude)
        end
    elseif code == XH.ThirdDefine.XH_SYSFUNC_SDK_CODE.SYSFUC_ADDRESS_FAIL_2 and device.platform == "android" then
        self._bNotGpsServer = true
    end
end

-- 获取AndroidMainfest.xml中的metaData数据(TD_SDK_APP_ID, TD_CONFIG_ID)
function SdkManager:reqGetMetaDataValue(strParam)
    if strParam == "TD_CONFIG_ID" then
        if device.platform == "windows" then
            return "8170"
        end
        if device.platform == "android" and self:isSupportFunctionAndroid("alp_open_iap") == "false" then
            return "8170"
        end
    end
    strParam = strParam or ""
    local luaj = require("cocos.cocos2d.luaj")
    local sigs = "(Ljava/lang/String;)Ljava/lang/String;"
    local strClass = "com.bianfeng.platform.executor.AppConfig"
    local _, ret = luaj.callStaticMethod(strClass, "getMetaDataValue", { strParam }, sigs)
    return ret
end

function SdkManager:hasIndexOf(list, code)
    if not list then
        return false
    end
    local result = false
    for _, v in pairs(list) do
        if v and v == code then
            result = true
            break
        end
    end
    return result
end

function SdkManager:isInstallWeChat()
    return self._isInstalledWeChat
end

function SdkManager:isCanShareToThird()
    return self:isInstallWeChat() or false
end

-- 高德定位相关的接口
SdkManager.AmapAccuracy = {
    LOW = "1",
    MID = "3",
    HIGH = "5"
}

-- accuracy 精度
function SdkManager:startLocationByAmap(accuracy)
    if device.platform == "windows" then
        local msg = "{\"latitude\":20.290883,\"longitude\":200.117307,\"country\":\"中国\",\"province\":\"浙江省\",\"city\":\"杭州市\",\"district\":\"上城区\",\"street\":\"司马渡巷\"}"
        local code = 1701
        self:onAMapCallback(code, msg)
        return
    end

    if device.platform == "android" then
        -- 如果本地已经有经纬度说明获取成功
        local localLongitude, localLatitude = XH.playerData:getServerLocation()
        if localLongitude and localLatitude then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_MAP_POSITION_STATE, true)
        end
        self._locationAccuracy = accuracy
        local getLocaltiom = function()
            self:getUserLocation()
        end
        self:checkLocationPermission(getLocaltiom)
        return
    end
    local amapInfo = XH.ThirdDefine.SDK_INTERFACE_NAMES["amap_get_location"]
    self.startGps = os.time()
    XH.sdkManager:xhCallOCFunc(amapInfo.cls, amapInfo.func, {["0"] = accuracy})
    self:stopAmapSchedule()
    self._startLocationByAmapScheduleID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        local msg = "{}"
        local code = -1
        self:onAMapCallback(code, msg)
    end, 2, false)
end

function SdkManager:setCurrnetCheckPermission(perm)
    self._currnetCheckPermission = perm
end

function SdkManager:getCurrnetCheckPermission()
    return self._currnetCheckPermission
end
--info 分享数据
--func 回调方法
--obj 回调方法所在的self对象
local DEFAULT_SHARE_TEXT = "我在《浙江游戏大厅》等你来游戏，快来一起玩吧"
function SdkManager:startShare(info, func, obj)
    -- if device.platform == "windows" then
    --     print("windows not support share to wechat")
    --     return
    -- end

    if not self:isCanShareToThird() then
        local shareText = DEFAULT_SHARE_TEXT
        if info.copyTextWhenNoWX and info.copyTextWhenNoWX ~= "" then
            shareText = info.copyTextWhenNoWX
        end
        XH.SysTool.copyString(shareText)
        XH.TipTool.showToast("邀请信息已复制成功，粘贴分享给您的好友吧！", 2)
        return
    end

    -- android包推广
    if info.url then
        if info.comment ~= "download" then
            local channelid = XH.SysTool:getChannelID()
            info.url = info.url .. "&channel=" .. channelid
        end
    end

    local ReqShare = require("app.Req.SDK.ReqShare")
    local reqShare = ReqShare.new()
    if func and obj then
        reqShare:addReqCallBack(obj, func)
    end

    reqShare:start(info)
end

function SdkManager:closeGoldGame()
    self:dispatchEvent( { name = self.EVENT_TOPONSDK_CLOSE_GOLD, data = "" })
end
--info 分享数据
--func 回调方法
--obj 回调方法所在的self对象
function SdkManager:addToponAds(name ,showType)
    if device.platform == "windows" or name == nil or name == "" then
        print("windows not support topon")
        return
    end
    if name == XH.ThirdDefine.TOPONSDK_AD_TYPE.REWARDEVIDEO and showType == "show" then
        self:closeGoldGame()
    end
    local adInfo = {}
    adInfo.adName = name
    adInfo.showType = showType
    adInfo.cnt = 0
    if showType == "close" then
        for key, info in ipairs(self._toponAdList) do
            if name == info.adName then
                if key == 1 then
                    self._toponAdList[key].showType = nil
                else
                    table.remove(self._toponAdList,key)
                end
            end
        end
        if not self._isLoadToponAd then
            table.insert(self._toponAdList,1,adInfo)
        else
            table.insert(self._toponAdList,2,adInfo)
        end
        self:startToponAds()
    else
        local canFind = false
        for _, info in ipairs (self._toponAdList) do
            if name == info.adName and showType == info.showType then
                canFind = true
                break
            end
        end
        if not canFind then
            table.insert(self._toponAdList,adInfo)
        end
        --XH.TipTool.showTip( {type = XH.TIP_LAYER_TYPE.OK,} , "topl:" .. tostring(self._isLoadToponAd) .." no:" .. #self._toponAdList .. " name:" .. (#self._toponAdList > 0 and self._toponAdList[1].adName or ""))
        self:startToponAds()
    end
end

function SdkManager:startToponAds()
    if not self._isLoadToponAd and #self._toponAdList > 0 then
        local adInfo = self._toponAdList[1]
        if adInfo.adName == XH.ThirdDefine.TOPONSDK_AD_TYPE.REWARDEVIDEO and self._toponAdList[1].cnt < 2 then
            self._isLoadToponAd = true
            self._toponAdList[1].cnt = self._toponAdList[1].cnt + 1
            local ReqToponAds = require("app.Req.SDK.ReqToponAds")
            local reqToponAds = ReqToponAds.new()
            reqToponAds:start(adInfo.showType)
        -- elseif adInfo.adName == XH.ThirdDefine.TOPONSDK_AD_TYPE.BANNER and self._toponAdList[1].cnt < 2 then
        --     self._isLoadToponAd = true
        --     if device.platform == "android" and adInfo.showType == "close" then
        --         table.remove(self._toponAdList,1)
        --         self._isLoadToponAd = false
        --     else
        --         self._toponAdList[1].cnt = self._toponAdList[1].cnt + 1
        --     end
        --     local ReqToponBanner = require("app.Req.SDK.ReqToponBanner")
        --     local reqToponBanner = ReqToponBanner.new()
        --     reqToponBanner:start(adInfo.showType)
        elseif adInfo.adName == XH.ThirdDefine.TOPONSDK_AD_TYPE.SPLASHAD and self._toponAdList[1].cnt < 2 then
            self._isLoadToponAd = true
            self._toponAdList[1].cnt = self._toponAdList[1].cnt + 1
            local ReqToponSplashAd = require("app.Req.SDK.ReqToponSplashAd")
            local reqToponSplashAd = ReqToponSplashAd.new()
            reqToponSplashAd:start(adInfo.showType)
        else
            table.remove(self._toponAdList,1)
            self:startToponAds()
        end
    end
end

function SdkManager:getToponCanShow(name)
    return self._loadToponList[name]
end

function SdkManager:setToponCanShow(name, canShow)
    self._loadToponList[name] = canShow
end

function SdkManager:clearToponList()
    if #self._toponAdList > 0 and self._toponAdList[1].adName == XH.ThirdDefine.TOPONSDK_AD_TYPE.SPLASHAD then
        print("")
    else
        self._toponAdList = { }
        self._isLoadToponAd = false
        self._loadToponList = {
            [XH.ThirdDefine.TOPONSDK_AD_TYPE.REWARDEVIDEO] = false,
            [XH.ThirdDefine.TOPONSDK_AD_TYPE.BANNER] = false,
            [XH.ThirdDefine.TOPONSDK_AD_TYPE.SPLASHAD] = false,
        }
    end
end

--------------兼容老游戏区接口----------------
function SdkManager:isSupportFunction(funcName)
    local result = XH.ThirdDefine.SDK_INTERFACE_NAMES[funcName] ~= nil
    return result or false
end

-- 添加监听
-- func 回调的方法，注意需要自己绑定好self
-- funcName 方法名字
-- return 监听成功类名，用于移除监听
function SdkManager:addSDKCallBackOld(func, funcName)
    local callbackKey = nil
    if funcName and type(func) == "function" and XH.ThirdDefine.SDK_INTERFACE_NAMES[funcName] then
        callbackKey = XH.ThirdDefine.SDK_INTERFACE_NAMES[funcName].cls or ""
        self._sdkCallBackListOld[callbackKey] = func
    else
        print("[SdkManager:addSDKCallBackOld] funcName is nil or func is not a function...")
    end
    return callbackKey
end

-- 移除监听
-- className 要移除某个类的名字，比如定位的话是 "XHAmap"
function SdkManager:removeSDKCallBackOld(className)
    if className then
        if device.platform == "android" then
            local classs = XH.ThirdDefine.SDK_INTERFACE_NAMES[className].cls or nil
            if classs then
                self._sdkCallBackListOld[classs] = nil
            end
        else
            self._sdkCallBackListOld[className] = nil
        end
    end
end

-- 调用原生OC方法，带参数
-- funcName 方法名
-- data 参数
function SdkManager:callFunctionWithMap(funcName, data, isTransform)
    if device.platform == "ios" then
        if funcName and XH.ThirdDefine.SDK_INTERFACE_NAMES[funcName] then
            XH.sdkManager:xhCallOCFunc(XH.ThirdDefine.SDK_INTERFACE_NAMES[funcName].cls, XH.ThirdDefine.SDK_INTERFACE_NAMES[funcName].func, data)
        else
            print("[SdkManager:callFunctionWithMap] funcName not exist")
        end
    elseif device.platform == "android" then
        if isTransform == nil then
            isTransform = true
        end
        self:callFunctionAndroid(funcName, data, isTransform)
    end
end

-- 调用原生OC方法，不带参数
-- funcName 方法名
function SdkManager:callFunction(funcName)
    if device.platform == "android" and XH.SysTool:CompareToBundleVersion("1.2.4") and funcName and funcName == "GYLogin" then
        funcName = "yidonglogin_login"
    end
    self:callFunctionWithMap(funcName, { })
end

function SdkManager:onDealOldCallback(code, msg)
    local className = ""
    
    if self:hasIndexOf(XH.ThirdDefine.AMAP_CALLBACK_CODE, code) then
        className = "XHAmap"
    elseif self:hasIndexOf(XH.ThirdDefine.FASTVO_CALLBACK_CODE, code) then
        className = "XHfastvo"
    elseif self:hasIndexOf(XH.ThirdDefine.SHAREDSDK_CALLBACK_CODE, code) then
        className = "XHshare"
    end

    local func = self._sdkCallBackListOld[className]
    if func then
        local data = {msg = msg}
        func(code, data)
    end
end

------------------------------- android --------------------------------------------
---
---
--------------------------------
-- 执行函数
-- @function [parent=#] callFunction
-- @param #string functionName 函数名
-- @param #string info 透传信息
function SdkManager:callFunctionAndroid(functionName, info, isTransform)
    local res =  {}
    if isTransform then
        local i = 0
        while (true)
        do
            local param = info[tostring(i)]
            if param == nil then
                break
            end
            res[#res + 1] = param
            i = i + 1
        end
    else
        res = info
    end

    xh.XHBridge:xhCallOCFunc("UserInterface", "callFunction", json.encode({
        functionName = functionName,
        args = res
    }))
end


--------------------------------
-- 执行函数(有返回值)
-- @function [parent=#] callFunctionWithResult
-- @param #string functionName 函数名
-- @param #string info 透传信息
-- @return #string 返回信息
function SdkManager:callFunctionWithResultAndroid(functionName, info)
    return xh.XHBridge:xhCallOCFunc("UserInterface", "callFunctionWithResult", json.encode({
        functionName = functionName,
        args = info
    }))
end

--------------------------------
-- 是否有函数支持
-- @function [parent=#] isSupportFunction
-- @param #string functionName 函数名
-- @return 注意：返回类型string！！！！！
function SdkManager:isSupportFunctionAndroid(functionName)
    return xh.XHBridge:xhCallOCFunc("UserInterface", "isSupportFunction", json.encode({
        functionName = functionName,
    }))
end

function SdkManager:stopAmapSchedule()
    if self._startLocationByAmapScheduleID and self._startLocationByAmapScheduleID ~= 0 then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._startLocationByAmapScheduleID)
        self._startLocationByAmapScheduleID = 0
    end
end

function SdkManager:reqGeTuiClientId()
    local ReqGeTui = require("app.Req.SDK.ReqGeTui")
    local reqGetui = ReqGeTui.new()
    reqGetui:start()
    reqGetui:getClientId()
end

function SdkManager:isSupportGeTui()
    local version = XH.SysTool:GetBundleVersion()
    if device.platform == "ios" and version >= "1.0.50" then
        return true
    elseif device.platform == "android" and version >= "1.0.7" then
        return true
    end
    return false
end

function SdkManager:openGeTuiPremission()
    if device.platform == "ios" then
        XH.sdkManager:xhCallOCFunc("XH_Extern", "SetPushModeForOff", "{}")
    end
end

function SdkManager:onReciveMsgIosGeTui(msg)
    local currScene = XH.SceneManager:getInstance():getTopScene()
    if currScene and(currScene:getTag() == XH.SCENE_TAG.GAME or currScene:getTag() == XH.SCENE_TAG.TEAHOUSE) then
        return
    end
    local json
    local ok = pcall( function()
        json = cjson.decode(msg)
    end )
    if ok then
        local payloadData = cjson.decode(json.payloadData)
        if payloadData and next(payloadData) then
            if payloadData.key and payloadData.key ~= "" then
                XH.areaData:setGeTuiMsgLive(payloadData.key)
            end
            local SRS = XH.netEngine:getSRSBySRSGroupID(XH.areaData:getSrsGroupID())
            if SRS then
                local linkstate = SRS:getLinkState()
                if linkstate == XH.SRS_LINK_STATE.LINK_STATE_SUCCESS then
                    XH.lobby:getModule("Lobby"):dealWithGeTuiMsg()
                end
            end
        end
    end
end

function SdkManager:onReciveMsgIosGeTuiNEW(msg)
    local currScene = XH.SceneManager:getInstance():getTopScene()
    if currScene and(currScene:getTag() == XH.SCENE_TAG.GAME or currScene:getTag() == XH.SCENE_TAG.TEAHOUSE) then
        return
    end
    local json
    local ok = pcall( function()
        json = cjson.decode(msg)
    end )
    if ok then
        if json.key then
            XH.areaData:setGeTuiMsgLive(json.key)
            local SRS = XH.netEngine:getSRSBySRSGroupID(XH.areaData:getSrsGroupID())
            if SRS then
                local linkstate = SRS:getLinkState()
                if linkstate == XH.SRS_LINK_STATE.LINK_STATE_SUCCESS then
                    XH.lobby:getModule("Lobby"):dealWithGeTuiMsg()
                end
            end
        end
    end
end


function SdkManager:onReciveMsgAndroidGeTui(msg)
    local currScene = XH.SceneManager:getInstance():getTopScene()
    if currScene and (currScene:getTag() == XH.SCENE_TAG.GAME or currScene:getTag() == XH.SCENE_TAG.TEAHOUSE)  then
        return
    end
    local payloadKey = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_GETUI_ANDROID_MSG, "")
    if payloadKey == "" then return end
    local data = loadstring("return { ".. payloadKey .. "}")()
    if data == nil or next(data) == nil then return end
    if data.secondpop and tonumber(data.secondpop) == XH.ThirdDefine.GETUI_PAREM_DEFINE.GETUI_SECOND_POP_IM then
        XH.areaData:setEnterLobbyNeedOpenIm(true)
        if XH.lobby:getModule("Lobby") then
            XH.lobby:getModule("Lobby"):openImView()
        end
    end
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_GETUI_ANDROID_MSG, "")
end

function SdkManager:onReciveMsgAndroidGeTuiNEW(msg)
    local currScene = XH.SceneManager:getInstance():getTopScene()
    if currScene and (currScene:getTag() == XH.SCENE_TAG.GAME or currScene:getTag() == XH.SCENE_TAG.TEAHOUSE)  then
        return
    end
    
    local payloadKey = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_GETUI_ANDROID_MSG, "")
    if payloadKey == "" then return end
    local data = json.decode(payloadKey)
    if data == nil then return end
    local notificationExtras = json.decode(data.notificationExtras)
    if notificationExtras == nil then return end
    local getuiMsg = XH.SysTool.getTableByStrings(notificationExtras.key, ";", "=", true)
    if getuiMsg then
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
            cc.Application:getInstance():openURL("https://"..getuiMsg.url)
        end
    end


    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_GETUI_ANDROID_MSG, "")
end

function SdkManager:onReciveAndroidNotify(msg)
    if msg and msg ~= "" then
        local str = string.gsub(msg,"\\u003d","=")
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_GETUI_ANDROID_MSG, str)
    end
end

function SdkManager:getNotifyInfo()
    local ReqGeTui = require("app.Req.SDK.ReqGeTui")
    local reqGetui = ReqGeTui.new()
    reqGetui:start()
    reqGetui:getNotifyInfo()
end

function SdkManager:onReciveNotifyState(code)
    if code == XH.ThirdDefine.GETUISDK_CALLBACK_CODE.GETUI_FLAG_SDK_GET_NOTIFY_STATE_OFF then
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PUSH_STATE, false)
    else
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PUSH_STATE, true)
    end
end

function SdkManager:getUserLocation()
    self.startGps = os.time()
    self:callFunctionWithMap(XH.ThirdDefine.SDK_INTERFACE_NAMES_KEY.AMP_GET, { ["0"] = self._locationAccuracy})
    --超时处理
    self:stopAmapSchedule()
    self._startLocationByAmapScheduleID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        local msg = "{}"
        local code = -1
        self:onAMapCallback(code, msg)
    end, 2, false)
end

function SdkManager:onAddPermissionCallback(code, msg)
    if XH.sdkManager:getCurrnetCheckPermission() then
        if code then
            if msg and type(msg) == "string" and type(XH.sdkManager:getCurrnetCheckPermission()) == "string" then
                XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.KW_COMMON, {label = XH.sdkManager:getCurrnetCheckPermission() .. "获取权限返回" .. code}, { info = msg })
            elseif type(XH.sdkManager:getCurrnetCheckPermission()) == "string" then
                XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.KW_COMMON, {label = XH.sdkManager:getCurrnetCheckPermission() .. "获取权限异常" .. code}, { info = "异常" })
            end
        else
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.KW_COMMON, {label = "无获取权限"}, { info = "无" })
        end
    end

    local setPermissionState = function(perm,value)
        if perm  == self.AndroidPermission.ACCESS_LOCATION then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_MAP_POSITION_STATE, value)
        elseif perm  == self.AndroidPermission.RECORD_AUDIO then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_FAST_VOICE_STATE, value)
        end
    end
    if  code == XH.ThirdDefine.ADD_PERMISSION_CALLBACK_CODE.ADD_PERMISSION_SUCCESS then
        setPermissionState(XH.sdkManager:getCurrnetCheckPermission(),true)
        if XH.sdkManager:getCurrnetCheckPermission() ==  self.AndroidPermission.ACCESS_LOCATION then
            --允许位置权限后获取位置
            self:getUserLocation()
        end
        XH.sdkManager:setCurrnetCheckPermission(nil)
    elseif code == XH.ThirdDefine.ADD_PERMISSION_CALLBACK_CODE.ADD_PERMISSION_FAIL then
        if XH.sdkManager:getCurrnetCheckPermission() ==  self.AndroidPermission.ACCESS_LOCATION then
            --拒绝位置权限，返回一个错误的数据
            self:onAMapCallback(-1, "{}")
        end
        setPermissionState(XH.sdkManager:getCurrnetCheckPermission(),false)
        if XH.sdkManager:getCurrnetCheckPermission() then
            local args = {
                ["0"] = XH.sdkManager:getCurrnetCheckPermission(),
            }
            XH.sdkManager:setCurrnetCheckPermission(XH.sdkManager:getCurrnetCheckPermission())
            XH.sdkManager:callFunctionWithMap("check_select_no_request",args)
        end
    elseif code == XH.ThirdDefine.ADD_PERMISSION_CALLBACK_CODE.SELECTED_NOT_SHOW_PERMISSION then 
        --玩家勾选了不再提醒 
        if XH.sdkManager:getCurrnetCheckPermission()  == self.AndroidPermission.ACCESS_LOCATION  then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_MAP_POSITION_STATE_DENY, true)
        elseif XH.sdkManager:getCurrnetCheckPermission()  == self.AndroidPermission.RECORD_AUDIO then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_FAST_VOICE_STATE_DENY, true)
        end
        XH.sdkManager:setCurrnetCheckPermission(nil)
    elseif code == XH.ThirdDefine.ADD_PERMISSION_CALLBACK_CODE.HAVE_PERMISSION_ALREADY then 
        setPermissionState(XH.sdkManager:getCurrnetCheckPermission(),true)
        if XH.sdkManager:getCurrnetCheckPermission()  == self.AndroidPermission.ACCESS_LOCATION  then
            XH.userDefault:setValue(XH.userDefault.KW_MAP_POSITION_STATE_DENY, false)
        elseif XH.sdkManager:getCurrnetCheckPermission()  == self.AndroidPermission.RECORD_AUDIO then
            XH.userDefault:setValue(XH.userDefault.KW_FAST_VOICE_STATE_DENY, false)
        elseif XH.sdkManager:getCurrnetCheckPermission() == self.AndroidPermission.WRITE_EXTERNAL_STORAGE then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_WRITE_EXTERNAL_STORAGE_PERMISSION_STATE, true)
        elseif XH.sdkManager:getCurrnetCheckPermission() == self.AndroidPermission.READ_PHONE_STATE then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PERMISSION_PHONE_STATE, true)
        end
        XH.sdkManager:setCurrnetCheckPermission(nil)
    elseif code == XH.ThirdDefine.ADD_PERMISSION_CALLBACK_CODE.NOT_HAVE_PERMISSION then
        print("spideman_checkPermissionCallBack")
        local size = cc.Director:getInstance():getWinSize()
        if XH.sdkManager:getCurrnetCheckPermission() == self.AndroidPermission.READ_PHONE_STATE then
            XH.TipTool.showToast("设备信息权限：用于区分玩家的唯一性和账号登陆", 2, cc.p(size.width/2,size.height - 80))
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PERMISSION_PHONE_STATE, false)
        elseif XH.sdkManager:getCurrnetCheckPermission() == self.AndroidPermission.WRITE_EXTERNAL_STORAGE then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_WRITE_EXTERNAL_STORAGE_PERMISSION_STATE, false)
        end
        setPermissionState(XH.sdkManager:getCurrnetCheckPermission(),false)
        XH.sdkManager:setCurrnetCheckPermission(nil)
    end
end

function SdkManager:checkLocationPermission(func)
    --检查本地存储中是否已经授权

    if XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_MAP_POSITION_STATE, false)  then
        func()
    else 
        --未授权的玩家,检查是否已经勾选禁止提示
        if XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_MAP_POSITION_STATE_DENY, false) then
            --已经勾选的弹窗提示,玩家可以去系统中手工开启
            self:onAMapCallback(-1, "{}")
            -- os.time() - XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_MAP_POSITION_LAST_TIME, 0) > 48*3600
            if  XH.SceneManager:getInstance():getTopSceneTag() == XH.SCENE_TAG.LOBBY then
                if not XH.sdkManager:guideOpenGpsServer() then
                    XH.TipTool.showTip({
                        type = XH.TIP_LAYER_TYPE.OK,
                        funcOK = function()
                            XH.sdkManager:callFunction("jump_permission_system")
                        end
                    },"您已经关闭了位置权限,需要您在系统设置中重新开启")
                end
                XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_MAP_POSITION_STATE_DENY, false)
                -- XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_MAP_POSITION_LAST_TIME, os.time())
            end
        else
            local size = cc.Director:getInstance():getWinSize()
            XH.TipTool.showToast("权限说明：用于启用游戏防作弊功能,需要获取您的位置权限", 2, cc.p(size.width/2,size.height - 80))
            XH.SysTool.performDelayOnce(function()
                local args = {
                    ["0"] = XH.sdkManager.AndroidPermission.ACCESS_LOCATION,
                }
                XH.sdkManager:setCurrnetCheckPermission(XH.sdkManager.AndroidPermission.ACCESS_LOCATION)
                XH.sdkManager:callFunctionWithMap("add_permission",args)
            end, 0.1)
        end
    end
end

--请求登录接口
--设置默认的参数
function SdkManager:setInitSDKConfig()
    local areaConfig = require("app.Config.AreaConfig")
    local config = areaConfig[900003]
    local proForpey = {
        appId = config.appID,
        channelId = config.channelID,
        packageId = config.packgeID,
        wxappId = XH.areaData:getWXAppID(),
        wxkeyId = XH.areaData:getWXAppKeyID(),
        wxulink = config.UniversalLink,
    }
    self:xhCallOCFunc("XHSwitchApp", "xhsa_set_init_appid:", proForpey)
end

function SdkManager:checkInstalledWechat()
    --检查微信是否安装
    if device.platform ~= "android" then
        self:xhCallOCFunc("XHWXLogin", "getInstalledWechat", {})
    end
end

function SdkManager:onZhiFuRealNameCallback(code, msg)
    --XH.TipTool.showToast(code .. " mag :" .. tostring(msg))
    self:dispatchEvent( { name = self.EVENT_ZFB_REALNAME_CALLBACK, data = { code = code, msg = msg } })
end

function SdkManager:onGetOrderIdCallback(code, msg)
    self:dispatchEvent( { name = self.EVENT_GETORDER_CALLBACK, data = { code = code, msg = msg } })
end

function SdkManager:onLebianCallback(code, msg)
    self:dispatchEvent( { name = self.EVENT_LEBIAN_CALLBACK, data = { code = code, msg = msg } })
end

function SdkManager:checkHavePermission(permissionCode)
    if device.platform ~= "android" then
        return
    end
    if not permissionCode or not self:hasIndexOf(self.AndroidPermission, permissionCode) then
        return
    end
    self:setCurrnetCheckPermission(permissionCode)
    self:callFunctionWithMap("check_has_permission",{["0"] = permissionCode})
end

function SdkManager:onAppleRevokeSessionCallback(code, msg)
    self:dispatchEvent( { name = self.EVENT_APPLE_REVOKE_SESSION_CALLBACK, data = { code = code, msg = msg } })
end

function SdkManager:onBaiduOcpcCallback(code, msg)
    self:dispatchEvent( { name = self.EVENT_BAIDU_OCPC_CALLBACK, data = { code = code, msg = msg } })
end

function SdkManager:checkCanUseReYun()
    if device.platform ~= "android" or not XH.SysTool:CompareToBundleVersion("1.4.1") then
        return false
    end
    local channelID = tonumber(XH.SysTool:getChannelID())
    if channelID < 73000 or channelID > 73999 then
        return false
    end
    return true
end

function SdkManager:checkCanUseReYunIOS()
    if device.platform == "ios" and cc.Application:getInstance():getVersion() >= "1.3.34" then
        return true
    end
    return false
end

function SdkManager:getDeviceId()
    return self._deviceId
end

function SdkManager:reqDeviceId()
    if device.platform ~= "android" then
        return
    end
    if XH.sdkManager:isSupportFunctionAndroid("ymn_datafun_get_deviceid") == "false" then
        return
    end
    self:callFunction("ymn_datafun_get_deviceid")
end

function SdkManager:onDeviceIdCallback(code, msg)
    if code == XH.ThirdDefine.XH_DEVICE_ID_CALLBACK_CODE.DEVICE_ID_SUCCESS then
        self._deviceId = msg
    end
end

-- 引导开启gps定位服务
function SdkManager:guideOpenGpsServer()
    if not self._bNotGpsServer then
        return false
    end
    if device.platform == "android" then
        if XH.sdkManager:isSupportFunctionAndroid("sysfunc_open_location_settings") == "false" then
            -- 老版本引导
            XH.TipTool.showTip(
            {
                type = XH.TIP_LAYER_TYPE.OK,
                funcOK = function()

                end
            },
            "定位服务未开启，请手动前往系统设置开启"
            )
        else
            -- 新版本引导
            XH.TipTool.showTip(
            {
                type = XH.TIP_LAYER_TYPE.OK,
                funcOK = function()
                    XH.sdkManager:callFunction("sysfunc_open_location_settings")
                end
            },
            "定位服务未开启，请点击确认跳转系统设置开启"
            )
        end
    end
    if device.platform == "ios" then
        -- 直接引导
        XH.TipTool.showTip(
        {
            type = XH.TIP_LAYER_TYPE.OK,
        },
        "定位服务未开启，请点击设置-隐私-定位服务，前往开启"
        )
    end
    return true
end

return SdkManager
��  