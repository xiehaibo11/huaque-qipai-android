local CusServiceModule = class("CusServiceModule", XH.ModuleBase)
local UrlConf = require("app.Config.UrlConf")
local TaskDefine = require("app.Define.TaskDefine")
CusServiceModule.EVENT_CUS_SERVICE_MSG_UNREAD = "EVENT_CUS_SERVICE_MSG_UNREAD"

function CusServiceModule:ctor()
    CusServiceModule.super.ctor(self)
end

function CusServiceModule:getProxyEvents()
    return {
        { module = XH.netEngine, eventName = XH.MatchLinkProtocol.RespNotifyTaskPercent.event_key, callBack = "onRespNotifyTaskPercent" },
    }
end

function CusServiceModule:getReqConfig()
    return {
        ReqCusServiceUnreadMsg = { reqPath = "app.Req.Task.ReqTaskPercent", callBack = self.onRespCusServiceUnreadMsg },
        ReqCusServiceClearUnread = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onRespCusServiceClearUnread },
    }
end

function CusServiceModule:reqCusServiceUnreadMsgCount()
    self:startReq("ReqCusServiceUnreadMsg", XH.playerData:getNumberID(), TaskDefine.APPID, TaskDefine.PROCESSID, TaskDefine.TASK_ID.TASK_TYPE_CALL_CENTER_MSG_COUNT, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function CusServiceModule:onRespCusServiceUnreadMsg(cell,ntype,ndata)
    if ndata and ndata.acData and ndata.acData ~= "" then
        local data = json.decode(ndata.acData)
        self:dispatchEvent( { name = self.EVENT_CUS_SERVICE_MSG_UNREAD, msg = data.msgCount or 0 }) 
    end
end

function CusServiceModule:reqCusServiceClearUnreadMsg()
    local specialStr = ";nTaskSubId=" .. TaskDefine.TASK_ID.TASK_TYPE_CALL_CENTER_MSG_COUNT 
    self:startReq("ReqCusServiceClearUnread", TaskDefine.TASK_ID.TASK_TYPE_CALL_CENTER_MSG_COUNT, TaskDefine.PROTOID, TaskDefine.APPID, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, specialStr)
end

function CusServiceModule:onRespCusServiceClearUnread(cell,ntype,ndata)
    if ndata and ndata.acData and ndata.acData ~= "" then
        local data = json.decode(ndata.acData)
        self:dispatchEvent( { name = self.EVENT_CUS_SERVICE_MSG_UNREAD, msg = data.msgCount or 0 }) 
    end
end

function CusServiceModule:onRespNotifyTaskPercent(event)
    local respData = XH.MatchLinkProtocol.RespNotifyTaskPercent:new()
    respData:bistream(event.msg.buff, event.msg.len)
    if respData.nTaskId == TaskDefine.TASK_ID.TASK_TYPE_CALL_CENTER_MSG_COUNT then
        if respData.acData and respData.acData ~= "" then
            local data = json.decode(respData.acData)
            self:dispatchEvent( { name = self.EVENT_CUS_SERVICE_MSG_UNREAD, msg = data.msgCount or 0 })
        end
    end
end

function CusServiceModule:getServiceUrl(nSource,msgData)
    local url = UrlConf.HTTP_ID_CUSTOMER_SERVICE
    local ServiceUrl
    if url then
        local nScode = XH.areaData:getCusTomerScode() or 0
        local nNumid = XH.playerData:getNumberID()
        local nNickName = string.urlencode(XH.playerData:getNickName())
        local nAvatar = string.urlencode(XH.playerData:getWeChatURL())
        local nChannelId = XH.SysTool:getChannelID()
        local deviceName = un.Device.getDeviceName()
        local appVersion = XH.SysTool:GetBundleVersion()
        if nSource then
            nSource =  XH.SysTool.url_encode(nSource)
            ServiceUrl = url .. string.format("scode=%s&numid=%d&nickname=%s&avatar=%s&channelid=%d&device=%s&app_version=%s&source_path=%s",
        nScode,nNumid,nNickName,nAvatar,nChannelId,deviceName, appVersion,nSource)
        else
            ServiceUrl = url .. string.format("scode=%s&numid=%d&nickname=%s&avatar=%s&channelid=%d&device=%s&app_version=%s",
        nScode,nNumid,nNickName,nAvatar,nChannelId,deviceName, appVersion)
        end
        -- local ServiceUrl = url .. string.format("scode=%s&numid=%d&nickname=%s&avatar=%s&channelid=%d&device=%s&app_version=%s",
        --     nScode,nNumid,nNickName,nAvatar,nChannelId,deviceName, appVersion)
        return ServiceUrl
    end
end

function CusServiceModule:getTempServiceUrl()
    local url = UrlConf.HTTP_ID_CUSTOMER_SERVICE
    if url then
        local nScode = XH.areaData:getCusTomerScode() or 0
        local localNumid = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_CUSSERVICE_TEMP_NUMID_DATA, "")
        local randNum
        if localNumid == "" then
            randNum = tostring(math.random(0, 9))
            for _ = 1, 7 do
                randNum = randNum .. tostring(math.random(0, 9))
            end
        else
            randNum = localNumid
        end
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_CUSSERVICE_TEMP_NUMID_DATA, randNum)
        local nNumid = 88888 .. randNum
        local nNickName = string.urlencode(tostring("临时访客" .. randNum))
        local nAvatar = ""
        local nChannelId = XH.SysTool:getChannelID()
        local deviceName = un.Device.getDeviceName()
        local appVersion = XH.SysTool:GetBundleVersion()
        local ServiceUrl = url .. string.format("scode=%s&numid=%s&nickname=%s&avatar=%s&channelid=%d&device=%s&app_version=%s",
            nScode,nNumid,nNickName,nAvatar,nChannelId,deviceName, appVersion)
        return ServiceUrl
    end
end

function CusServiceModule:oponCusServerView(isTempNumid,nSourceID)
    if self._ConfigJson or self:readConfig() then
        if self._ConfigJson.externalLinkAredIds and not self._ConfigJson.AllExternalLink 
        and XH.TableTool.isValueInTable(XH.areaData:getLobbyID(), self._ConfigJson.externalLinkAredIds) then
            self:openCusServerUrl(isTempNumid, nSourceID)
        else
            XH.viewManager:openView("CusServiceView", nil, isTempNumid, nSourceID)
        end
        self._needShow = nil
    else
        self._needShow = {isTempNumid = isTempNumid, nSourceID = nSourceID}
    end
    self:reqDownFile()
end

function CusServiceModule:readConfig()
    local fileName = "CusServerConfig.json"
    local filePath = cc.FileUtils:getInstance():getWritablePath() .. fileName
    local isFileEXist = cc.FileUtils:getInstance():isFileExist(filePath)
    if not isFileEXist then
        return false
    end
    local jsonData = { }
    local ConfigJson = cc.FileUtils:getInstance():getStringFromFile(filePath)
    if ConfigJson ~= nil and ConfigJson ~= "" then
        local ok, msg = pcall( function()
            jsonData = cjson.decode(ConfigJson)
        end )
        if not ok then
            print("CusServiceModule:setConfig cjson failed: " .. msg)
            return false
        end
    end
    self._ConfigJson = jsonData
    return true
end

function CusServiceModule:reqDownFile()
    self._maxDownloadCount = 3
    self._failDownloadCount = 0
    local fileName = "CusServerConfig.json"
    math.randomseed(os.time())
    local randNum = math.random(9999)
    local fileUrl = UrlConf.CUSSERVER_CONFIG_DOWNLOAD .. "v=" .. randNum
    local filePath = cc.FileUtils:getInstance():getWritablePath() .. fileName

    self:startDownFile(fileUrl,fileName,filePath)
end

function CusServiceModule:startDownFile(fileUrl,fileName,filePath)
    if self._failDownloadCount >= self._maxDownloadCount then
        local func = function()
            self._failDownloadCount = 0
            self:startDownFile(fileUrl,fileName,filePath)
        end
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
            funcOK = func
        }, "获取信息失败，请重试！")
        return 
    end
    local downloader = un.Downloader.new()

    downloader:setProgressListener(function (tag, progress)
        print("downFile fileUrl progress:" .. progress .. " tag:" .. tag)
    end)

    downloader:setListener(function (tag, costTime, errCode, errMsg)
        if errCode == 0 then
            self._failDownloadCount = 0
            self:readConfig()
            if self._needShow then
                self:oponCusServerView(self._needShow.isTempNumid, self._needShow.nSourceID)
            end
        else
            self._failDownloadCount = self._failDownloadCount + 1
            self:startDownFile(fileUrl,fileName,filePath)
        end
        print("downFile fileUrl errCode:" .. errCode .. " tag:" .. tag)
    end)
    downloader:add(fileUrl, filePath, "", fileUrl)
    downloader:removeWhenAllTaskComplete()
end

function CusServiceModule:openCusServerUrl(isTempNumid, nSource)
    --清除未读消息
    local serviceUrl
    self:reqCusServiceClearUnreadMsg()
    if nSource then
        serviceUrl = self:getServiceUrl(nSource)
    else
        serviceUrl = self:getServiceUrl()
    end
    if isTempNumid then
        serviceUrl = self:getTempServiceUrl()
    end
    if serviceUrl then
        cc.Application:getInstance():openURL(serviceUrl)
    end
end

return CusServiceModule   �"  