local RRBuYuModule = class("RRBuYuModule", XH.ModuleBase)
RRBuYuModule.EVENT_BUYU_DOWNLOAD_PROGRESS = "EVENT_BUYU_DOWNLOAD_PROGRESS"
local TaskDefine = require("app.Define.TaskDefine")
local cjson = require("cjson")
local RENRENAWARDSUCCESS = "RENRENAWARDSUCCESS"
local APP_HOST_NAME = "com.xm.zjgamecenter"
local PLUGIN_ID = "61c550c2-3d61-4375-86bd-4dc5a9a4d558"
local PLUGIN_ACTIVITY_NAME = "org.cocos2dx.lua.AppActivity"

local KW_AFTER_UPDATE_CODE = {
    BEFOREUPDATE = 0,
    AFTERUPDATE = 1,
    NEEDRECALL = 2
}

function RRBuYuModule:ctor()
    RRBuYuModule.super.ctor(self)
    self.downLoading = false
    self._joinRenrenbuyuDelayID = nil
    self._downloadWithoutRestart = false
    self._curProgress = 0
    self._canJoinRRBY = false
    self._pluginInfo = {}
    self._isAlreadyCallInstall = false
end

function RRBuYuModule:getReqConfig()
    return {
        RequestBuYuAward = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onReqRenRenBuYuCallBack },
    }
end

function RRBuYuModule:getProxyEvents()
    local repluginCallback = "onRePluginCallBack"
    local isSupportRePlugin,isNewRePlugin = XH.lobby:getModule("Replugin"):isSupportRePlugin()
    if isNewRePlugin then
        repluginCallback = "onRePluginCallBack_2_2"
    end
    return {
        { module = XH.sdkManager, eventKeyName = "EVENT_LEBIAN_CALLBACK", callBack = "onLebianCallBack"},
        { module = XH.lobby:getModule("Replugin"), eventKeyName = "EVENT_REPLUGIN_CALLBACK", callBack = repluginCallback}
    }
end

function RRBuYuModule:isSupportRRBY()
    if device.platform == "ios" and XH.SysTool:CompareToBundleVersion("1.3.29") then
        return true
    end
    if device.platform == "android" and XH.SysTool:CompareToBundleVersion("1.2.9") then
        return true
    end
    XH.TipTool.showTip({
        type = XH.TIP_LAYER_TYPE.OK,
        funcOK = function ()
            local UrlConf = require("app.Config.UrlConf")
            if device.platform == "ios" then
                cc.Application:getInstance():openURL(UrlConf.DOWNLOAD_ADDRESS_IOS)
            else
                cc.Application:getInstance():openURL(UrlConf.DOWNLOAD_ADDRESS_ANDROID_2)
            end
        end
    }, "当前版本不支持人人捕鱼，请下载最新版本")
    return false
end


function RRBuYuModule:RequestBuYuAward()
    self:startReq("RequestBuYuAward", TaskDefine.TASK_ID.TASK_TYPE_RENREN_BUYU_AWARD, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function RRBuYuModule:onReqRenRenBuYuCallBack(cell, cellType, data)
    if cellType == XH.Req.TYPE.SUCCESS then
        local acData = data.acData
        if acData and acData ~= "" then
            local jsonData
            local ok = pcall( function()
                jsonData = cjson.decode(acData)
            end )
            if not ok then
                return
            end
            if jsonData.code == 0 or jsonData.code == 3 then
                cc.UserDefault:getInstance():setBoolForKey(RENRENAWARDSUCCESS, true)
            end
            self:removejoinRenrenbuyuDelayID()
            self:doJoinRenrenbuyu()
        end
    end
end

function RRBuYuModule:removejoinRenrenbuyuDelayID()
    if self._joinRenrenbuyuDelayID then
        self:_removeAllScheduleID(self._joinRenrenbuyuDelayID)
        self._joinRenrenbuyuDelayID = nil
    end
end


function RRBuYuModule:writeVersion(versionPath)
    local version = cc.Application:getInstance():getVersion()
    local file = io.open(versionPath, "w")
    file:write(version)
    file:close()
end

function RRBuYuModule:CheckPreSoInfo()
    local writablePath = cc.FileUtils:getInstance():getWritablePath()
    local configPath = writablePath .. "../app_p_a/p.l"
    
    if not cc.FileUtils:getInstance():isFileExist(configPath) then
        return
    end

    local config = cc.FileUtils:getInstance():getStringFromFile(configPath)
    local jarname = config:match("/([^/]+)%.jar")

    if jarname == nil then
        return
    end

    local dir = writablePath .. '../app_p_n/' .. jarname

    if not cc.FileUtils:getInstance():isDirectoryExist(dir) then
        return
    end

    local oldcpath = package.cpath
    package.cpath = package.cpath .. ";" .. dir .. "/?.so;"
    function dealMsg(msg)
        print(msg)
        print(debug.traceback())
        return msg
    end
    local status,msg = pcall(require, "libmain")

    if string.find(msg,"luaopen_libmain") == nil then
        self:uninstallRRBuyu()
    end
        
    package.cpath = oldcpath

end

function RRBuYuModule:PreCheckBY()
    print("PreCheckBY")
    -- Find oldversion
    local writablePath = cc.FileUtils:getInstance():getWritablePath()
    local version = cc.Application:getInstance():getVersion()

    local versionPath = writablePath .. "BuYuVersion.txt"
    if not cc.FileUtils:getInstance():isFileExist(versionPath) then
        self:CheckPreSoInfo()
        self:writeVersion(versionPath)
        return
    end

    local file = io.open(versionPath, "r")
    local oldversion = file:read("*a")
    file:close()
    if oldversion ~= version then
        self:uninstallRRBuyu()
    end
    self:writeVersion(versionPath)
end

function RRBuYuModule:doJoinRenrenbuyu()
    if device.platform == "ios" then
        display.removeUnusedSpriteFrames()

        local luaoc = require("cocos.cocos2d.luaoc")
        local args = {}
        local info = self:getBaseLoginInfo()
        args["1"] = json.encode(info)
        args["2"] = function()
            
        end
        luaoc.callStaticMethod("AppController", "showUnity", args)
        return
    end
    local isSupportRePlugin,isNewRePlugin = XH.lobby:getModule("Replugin"):isSupportRePlugin()
    if isSupportRePlugin then
        if isNewRePlugin then
            self:PreCheckBY()
            XH.lobby:getModule("Replugin"):openPlugin(PLUGIN_ID,self:getHostPackageInfo())
        else
            XH.lobby:getModule("Replugin"):queryPluginStatus(PLUGIN_ID)
        end
    else
        local info = self:getBaseLoginInfo()
        local luaj = require("cocos.cocos2d.luaj")
        local args = {}
        table.insert(args,json.encode(info))
        table.insert(args,handler(self,self.unity2Cocos))
        luaj.callStaticMethod("org/cocos2dx/lua/AppActivity", "showUnity", args)
    end
end

-----------------------------ShowUnity---人人捕鱼---------------------------
function RRBuYuModule:joinInRRBuyu()
    if device.platform == "android" then
        if  XH.lobby:getModule("Replugin"):isSupportRePlugin() then
            self:setInUnity(false)
        else
            self:setInUnity(true)
        end
    end
    if not cc.UserDefault:getInstance():getBoolForKey(RENRENAWARDSUCCESS, false) then
        --请求人人捕鱼奖励
        self:RequestBuYuAward()
        self._joinRenrenbuyuDelayID = self:performWithDelay(function()
            self:doJoinRenrenbuyu()
            self:removejoinRenrenbuyuDelayID()
        end, 4)
        return
    end
    self:doJoinRenrenbuyu()
end

function RRBuYuModule:unity2Cocos()
    --帧处理
    XH.SysTool.performDelayOnce(function()
        local Callback = "unity2Cocos返回成功"
        print("unity2Cocos返回".. Callback)
        self:setInUnity(false)
	end, 0.01)
end

function RRBuYuModule:isDownloadCompleted(IsLobbyIcon)
    self._isLobbyIcon = IsLobbyIcon
    if XH.lobby:getModule("Replugin"):isSupportRePlugin() then
        self:joinInRRBuyu()
    else
        -- if XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_RRBY_AFTER_UPDATE, false) then
        --     XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_RRBY_AFTER_UPDATE, false)
        --     self:joinInRRBuyu()
        -- else
            self:reqAfterUpdate()
        -- end
    end
end

--进入人人捕鱼引擎
function RRBuYuModule:setInUnity(isInUnity)
    if isInUnity ~= nil then 
        XH.sdkManager:setNotSupportPayResultCallBack(isInUnity)
    end
end

--人人捕鱼下载中(防止多次点击下载)
function RRBuYuModule:isDownloading()
    return self.downLoading or false 
end

function RRBuYuModule:setDownloading(isDownloading)
    self.downLoading = isDownloading 
end

function RRBuYuModule:reStartAPP()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RRBY_CALL_SDKFUC_RESETAPP)
    XH.sdkManager:callFunction("lebian_resetApp")
end

function RRBuYuModule:rrbyHotUpdateProgressCallback(percent)
    local event = {}
    event.percent = percent or self._curProgress
    self:dispatchEvent( { name = self.EVENT_BUYU_DOWNLOAD_PROGRESS, data = event })
end

function RRBuYuModule:onLebianCallBack(event)
    if not event or not event.data then
        return
    end
    local code = event.data.code
    local msg = event.data.msg
    XH.TipTool.hideLoading()
    -- if XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_RRBY_AFTER_UPDATE, false) then
    --     return
    -- end
    if code == XH.ThirdDefine.LEIBIAN_CALLBACK_CODE.NOUPDATE then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RRBY_STATUS_NO_UPDATE, nil, {request_code = code})
        self:joinInRRBuyu()
    elseif code == XH.ThirdDefine.LEIBIAN_CALLBACK_CODE.NOTFORCEVER or code == XH.ThirdDefine.LEIBIAN_CALLBACK_CODE.FORCEVER then
        self:setDownloading(true)
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RRBY_STATUS_NEED_UPDATE, nil, {request_code = code})
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RRBY_CALL_SDKFUC_RESOURCEDOWNLOAD)
        XH.sdkManager:callFunction("lebian_rengengDownload")
        if self._canJoinRRBY then
            self:setDownloading(false)
            self:joinInRRBuyu()
        else
            XH.TipTool.showToast("人人捕鱼检测到新版本,开始更新", 3)
        end
    elseif code <= XH.ThirdDefine.LEIBIAN_CALLBACK_CODE.USELEIBIANCLOSED then
        XH.TipTool.showToast("人人捕鱼检测失败,请稍后重试(" .. code .. ")", 3)
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RRBY_SDK_ERR_CODE, nil, {request_code = code})
    elseif code == XH.ThirdDefine.LEIBIAN_CALLBACK_CODE.DOWNERRREASON then
        XH.TipTool.showToast("人人捕鱼下载失败,请稍后重试(" .. code .. ")", 3)
        self._curProgress = 0
        self:setDownloading(false)
        self:rrbyHotUpdateProgressCallback(-1)
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RRBY_SDK_DOWNLOAD_ERR_CODE, nil, {request_code = code})
    elseif code == XH.ThirdDefine.LEIBIAN_CALLBACK_CODE.DOWNPROGRESS then
        if self._canJoinRRBY then
            return
        end
        local ok, msgTable = pcall(cjson.decode, msg)
        if ok and msgTable.progress then
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RRBY_SDK_DOWNLOAD_PERCENT_CODE, nil, {lebian_dl_progess = msgTable.progress, request_code = code})
            -- XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_RRBY_AFTER_UPDATE, false)
            self._curProgress = msgTable.progress
            self:rrbyHotUpdateProgressCallback()
            if tonumber(msgTable.progress) == 100 then
                -- XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_RRBY_AFTER_UPDATE, true)
                self._curProgress = 0
                self:setDownloading(false)
                self._downloadWithoutRestart = true
                self:showRestartTips()
            end
        end
    elseif code == XH.ThirdDefine.LEIBIAN_CALLBACK_CODE.AFTERUPDATE then
        if tonumber(msg) == KW_AFTER_UPDATE_CODE.NEEDRECALL then
            self:reqAfterUpdate()
        elseif tonumber(msg) == KW_AFTER_UPDATE_CODE.BEFOREUPDATE or tonumber(msg) == KW_AFTER_UPDATE_CODE.AFTERUPDATE then
            self._canJoinRRBY = tonumber(msg) == KW_AFTER_UPDATE_CODE.AFTERUPDATE
            self:checkUpdateStatus()
        end
    elseif code == XH.ThirdDefine.LEIBIAN_CALLBACK_CODE.NEEDRESTART then
        self:showRestartTips()
    else
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RRBY_SDK_ERR_CODE, nil, {request_code = code})
    end
end

function RRBuYuModule:onRePluginCallBack_2_2(event)
    if not event or not event.data then
        return
    end
    XH.TipTool.hideLoading()
    local code = event.data.code
    local msg = event.data.msg
    print("RRBuYuModule => onRePluginCallBack_2_2 code = " .. code .. " msg = " .. msg)
    if code == XH.ThirdDefine.REPLUGIN_CALLBACK_CODE.PLUGIN_DOWNLOAD_PROCESS then
        self:setDownloading(true)
        self._curProgress = tonumber(msg)
        self:rrbyHotUpdateProgressCallback()
        if tonumber(msg) == 100 then
            self._curProgress = 0
            self:setDownloading(false)
        end
    elseif code == XH.ThirdDefine.REPLUGIN_CALLBACK_CODE.PLUGIN_INSTALL_SUCCESS then
        print("RRBuYuModule -> PLUGIN_INSTALL_SUCCESS")
        local currScene = XH.SceneManager:getInstance():getTopScene()
        local isAllow = currScene and (currScene:getTag() == XH.SCENE_TAG.LOBBY or currScene:getTag() == XH.SCENE_TAG.TEAHOUSE)
        XH.lobby:getModule("Replugin"):allowStartPlugin(PLUGIN_ID,self:getHostPackageInfo(),isAllow)
    elseif code == XH.ThirdDefine.REPLUGIN_CALLBACK_CODE.PLUGIN_START_SUCCESS then
        print("RRBuYuModule -> PLUGIN_START_SUCCESS")
    elseif code == XH.ThirdDefine.REPLUGIN_CALLBACK_CODE.PLUGIN_INSTALL_FAIL then
        local currScene = XH.SceneManager:getInstance():getTopScene()
        local isAllow = currScene and (currScene:getTag() == XH.SCENE_TAG.LOBBY or currScene:getTag() == XH.SCENE_TAG.TEAHOUSE)
        XH.lobby:getModule("Replugin"):allowStartPlugin(PLUGIN_ID,self:getHostPackageInfo(),isAllow)
    elseif code == XH.ThirdDefine.REPLUGIN_CALLBACK_CODE.PLUGIN_QUIT_HOST then
        XH.sdkManager:callFunction("wechat_reset_pay_params")
    end
end

function RRBuYuModule:onRePluginCallBack(event)
    if not event or not event.data then
        return
    end
    XH.TipTool.hideLoading()
    local code = event.data.code
    local msg = event.data.msg
    print("RRBuYuModule => onRePluginCallBack code = " .. code .. " msg = " .. msg)
    if code == XH.ThirdDefine.REPLUGIN_CALLBACK_CODE.PLUGIN_INSTALL_NOT then
        XH.TipTool.showToast("人人捕鱼未加载,开始加载", 3)
        self:setDownloading(true)
        self._curProgress = 0
        self:rrbyHotUpdateProgressCallback()
        XH.lobby:getModule("Replugin"):installPluginWithTimeOut(PLUGIN_ID,"10","30")
        if self._isLobbyIcon then
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.RENRENBUYU_5)
        else
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.RENRENBUYU_4)
        end
    elseif code == XH.ThirdDefine.REPLUGIN_CALLBACK_CODE.PLUGIN_DOWNLOAD_PROCESS then
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_RRBY_AFTER_UPDATE, false)
        self._curProgress = tonumber(msg)
        self:rrbyHotUpdateProgressCallback()
        if tonumber(msg) == 100 then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_RRBY_AFTER_UPDATE, true)
            self._curProgress = 0
            self:setDownloading(false)
        end
    elseif code == XH.ThirdDefine.REPLUGIN_CALLBACK_CODE.PLUGIN_STATUS_SUCCESS then
        print("RRBuYuModule -> isInstallPlugin")
        if msg ~= "" then
            local info = json.decode(msg)
            if info then
                self._pluginInfo = info
            end
        end
        XH.lobby:getModule("Replugin"):isInstallPlugin(PLUGIN_ID)
    elseif code == XH.ThirdDefine.REPLUGIN_CALLBACK_CODE.PLUGIN_INSTALL_ALREADY then
        print("RRBuYuModule -> PLUGIN_INSTALL_ALREADY")
        if not self._isAlreadyCallInstall and XH.lobby:getModule("Replugin"):checkPluginUpdateStatus(self._pluginInfo) then
            XH.TipTool.showToast("人人捕鱼检测到新版本,开始更新", 3)
            self:setDownloading(true)
            self._curProgress = 0
            self:rrbyHotUpdateProgressCallback()
            XH.lobby:getModule("Replugin"):installPluginWithTimeOut(PLUGIN_ID,"10","30")
            self._isAlreadyCallInstall = true
        else
            self._isAlreadyCallInstall = false
            XH.lobby:getModule("Replugin"):startApk(PLUGIN_ID,self:getHostPackageInfo())
        end
    elseif code == XH.ThirdDefine.REPLUGIN_CALLBACK_CODE.PLUGIN_INSTALL_SUCCESS then
        print("RRBuYuModule -> PLUGIN_INSTALL_SUCCESS")
        self._isAlreadyCallInstall = false
        XH.lobby:getModule("Replugin"):startApk(PLUGIN_ID,self:getHostPackageInfo())
    elseif code == XH.ThirdDefine.REPLUGIN_CALLBACK_CODE.PLUGIN_START_SUCCESS then
        print("RRBuYuModule -> PLUGIN_START_SUCCESS")
    elseif code == XH.ThirdDefine.REPLUGIN_CALLBACK_CODE.PLUGIN_DOWNLOAD_FAIL then
        local subCode = json.encode(msg)
        if subCode.code then
            if subCode.code == XH.lobby:getModule("Replugin").DownloadCode.FILE_ERROR or subCode.code == XH.lobby:getModule("Replugin").DownloadCode.MD5_FAIL then
                local text = XH.lobby:getModule("Replugin").DownloadMsg[subCode.code]
                XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK, function()
                    XH.lobby:getModule("Replugin"):clearDownloadCache(PLUGIN_ID)
                end}, text)
            else
                local text = XH.lobby:getModule("Replugin").DownloadMsg[subCode.code]
                XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK}, text)
            end
        end
    elseif code == XH.ThirdDefine.REPLUGIN_CALLBACK_CODE.PLUGIN_QUIT_HOST then
        XH.sdkManager:callFunction("wechat_reset_pay_params")
        self:unity2Cocos()
    elseif code == XH.ThirdDefine.REPLUGIN_CALLBACK_CODE.PLUGIN_INSTALL_FAIL then
        if self._isAlreadyCallInstall then
            XH.lobby:getModule("Replugin"):isInstallPlugin(PLUGIN_ID)
        end
    end
end

function RRBuYuModule:showRestartTips()
    XH.TipTool.hideLoading()
    local okFunc = function ()
        self._downloadWithoutRestart = false
        self:reStartAPP()
    end
    XH.TipTool.showTip({
        type = XH.TIP_LAYER_TYPE.OK_CANCEL,
        funcOK = okFunc
    }, "人人捕鱼更新成功,赶快重启应用体验游戏吧~")
end

function RRBuYuModule:checkUpdateStatus()
    XH.sdkManager:callFunction("lebian_queryUpdateStatus")
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RRBY_CALL_SDKFUC_QUERYUPDATESTATUS)
end

function RRBuYuModule:reqAfterUpdate()
    self._canJoinRRBY = false
    XH.sdkManager:callFunction("lebian_afterUpdate")
end

function RRBuYuModule:getBaseLoginInfo()
    local unionID = XH.playerData:getNumberID()
    if XH.playerData:getBuYuUserID() ~= "" then
        unionID = XH.playerData:getBuYuUserID()
    end
    local startTime = os.time({day = 1, month = 4, year = 2021, hour = 0, minute = 0, second = 0}) 
    if XH.playerData:getRegTime() >  startTime then
        --为没有AA1_的用户加AA1_
        local matchid = string.match(tostring(unionID),"(%d+)")
        if tostring(unionID) == tostring(matchid)  then
            unionID="AA1_"..tostring(unionID)
        end
    end
    local closeWeChatPay = false
    local info =
    { 
        ["channelID"] = tostring(XH.SysTool:getChannelID()),                                  
        ["areaID"] = XH.areaData:getAreaID(),
        ["gameID"] = tostring(XH.areaData:getConfigGameID()),                              
        ["numID"] = XH.playerData:getNumberID(),
        ["userID"] = un.StringUtils.GB_18030_2000_TO_UTF8(XH.playerData:getUserID()), 
        ["sessionID"] = XH.StringTool.bin2hex(XH.playerData:getSessionID()),
        ["deviceID"] = tostring(XH.SysTool:GetDevid()),
        ["nickname"] = tostring(XH.playerData:getNickName()),
        ["uaalType"] = "zhejiangbao",
        ["grade"] = XH.playerData:getSex(),
        ["scriptName"] = "deliver_award",
        ["groupID"] = XH.areaData:getSrsGroupID(),
        ["closeWeChatPay"] = closeWeChatPay,
        ["aliPay"] = "alipayopen_pay",
        ["wechatPay"] = "weixin_pay",
        ["cpsID"] = 1354,   -- 捕鱼需要，固定传值
        ["unionID"] = unionID,
        ["cpStr"] = XH.areaData:getH5CpID() or "",
        ["headUrl"] = XH.playerData:getWeChatURL() or "",
        ["ThirdNumId"] = XH.playerData:getNumberID(),   -- 第三方账号id
        ["ThirdAreaId"] = XH.areaData:getAreaID(),      -- 第三方区域id
    }
    if device.platform == "ios" then
        info.cpsID = 10215
    end
    return info
end

function RRBuYuModule:getHostPackageInfo()
    local info = self:getBaseLoginInfo()
    info.hostPackageName = APP_HOST_NAME
    info.hostActivity = PLUGIN_ACTIVITY_NAME
    return json.encode(info) 
end

function RRBuYuModule:uninstallRRBuyu()
    if XH.lobby:getModule("Replugin"):isSupportRePlugin() then
        XH.lobby:getModule("Replugin"):unInstallPlugin(PLUGIN_ID)
    end
end

return RRBuYuModule

,S  