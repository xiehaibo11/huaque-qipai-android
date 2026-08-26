local H5GameWidgetModule = class("H5GameWidgetModule", XH.ModuleBase)
local DEBUG = false
H5GameWidgetModule.H5GAME_EVENT_CONFIG = "H5GAME_EVENT_CONFIG"
H5GameWidgetModule.H5GAME_EVENT_CLOSE = "H5GAME_EVENT_CLOSE"

local H5GameNameList = {
    LEGEND = "LEGEND",
    BUYU = "BUYU",
    KNIFELEGEND = "KNIFELEGEND",
    REXUEFENGSHEN = "REXUEFENGSHEN",
    DOULUODALU = "DOULUODALU",
    JIEJISANGUO = "JIEJISANGUO",
    RENRENBUYU = "RENRENBUYU",
}


function H5GameWidgetModule:getProxyEvents()
    return {
        { module = XH.login:getModule("Login"), eventKeyName = "LOGIN_EVENT_SUCCESS", callBack = "onLoginSuccessCallBack" },
    }
end

function H5GameWidgetModule:getReqConfig()
    return {
        ReqH5GameShowList = { reqPath = "lobby.Req.H5GameWidget.ReqH5GameShowList", callBack = self.respH5GameShowList },
    }
end



function H5GameWidgetModule:ctor()
    H5GameWidgetModule.super.ctor(self)
    self._lateFun = nil
    self._lateText = ""
    self._roomId = ""
    self._gameName = ""
    self._block = ""
    self._blockLabel = ""
end

function H5GameWidgetModule:onLoginSuccessCallBack(event)
    self:resetH5GameConfig()
end

function H5GameWidgetModule:getH5GameConfig()
    if self._H5GameConfig == nil then
        self:resetH5GameConfig()
        return nil
    else
        return self._H5GameConfig
    end
end

function H5GameWidgetModule:respH5GameShowList(req, type, data)
    if type ==  XH.Req.TYPE.SUCCESS then
        self._H5GameConfig = data
        local filePath = cc.FileUtils:getInstance():getWritablePath() .. self._filename
        local isFileEXist = cc.FileUtils:getInstance():isFileExist(filePath)
        if isFileEXist then
            self:saveConfig(self._filename,cjson.encode(data))
        else
            self:createConfig(self._filename,cjson.encode(data))
        end
    end
end

function H5GameWidgetModule:addH5GameWidget(iconType, gameid)
    if not XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_RN_TOURIST_LOGIN) then
        if device.platform == "android" or device.platform == "windows" then
            if iconType == "game_end" and self._lateFun then
                self:runLateGoFun()
            end
            self._gameid = gameid
            local view = XH.viewManager:getView(XH.SceneManager:getInstance():getTopSceneTag(), "H5GameWidgetView")
            if view then
                view:init(iconType, gameid)
            elseif self._H5GameConfig then
                self:showH5GameWidget(iconType, gameid)
            elseif self._H5GameConfig == nil then
                self:resetH5GameConfig(iconType, gameid)
            end
        end
    end
end

function H5GameWidgetModule:showH5GameWidget(iconType, gameid)
    if XH.ChannelTool.checkIsECarChannel() then
        return
    end
    if self._H5GameConfig and self._H5GameConfig.data then
        for scene, iconInfo in pairs(self._H5GameConfig.data) do
            if scene == iconType and next(iconInfo.game_sort) ~= nil and iconInfo.status == 1 then
                --if iconInfo.gameid and gameid and XH.TableTool.isValueInTable(gameid, iconInfo.gameid) then
                --    XH.viewManager:openView("H5GameWidgetView", nil, iconType, gameid)
                --    break
                --end
                XH.viewManager:openView("H5GameWidgetView", nil, iconType, gameid)
                break
            end
        end
    end
end

function H5GameWidgetModule:close5GameWidget(iconType)
    self:dispatchEvent({ name = self.H5GAME_EVENT_CLOSE, data = iconType })
end

function H5GameWidgetModule:resetH5GameConfig(iconType, gameid)
    self:clearH5GameConfig()
    self._filename = "H5GameShowIcon_new_" .. XH.areaData:getLobbyID() .. ".json"
    self:reqConfigDataNew()
    self:loadConfig(self._filename,iconType, gameid)
    --self:reqConfigData()
end

function H5GameWidgetModule:reqConfigDataNew()
    if self._H5GameConfig == nil then
        self:startReq("ReqH5GameShowList", XH.KW_CONFIG_LOGIC_TIME_OUT)
    end
end

function H5GameWidgetModule:reqConfigData()
    local filename = "H5GameShowIcon_" .. XH.areaData:getLobbyID()
    local ReqConfigData = require("app.Req.ReqConfigData")
    local reqConfigData = ReqConfigData:new()
    reqConfigData:addReqCallBack(self, self.onConfigDataCallback)
    reqConfigData:start(filename,XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function H5GameWidgetModule:onConfigDataCallback(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data and data ~= "" then
            local jsonData
            local ok, msg = pcall(function()
                jsonData = cjson.decode(data)
            end)
        
            if not ok then
                print("H5GameWidgetModule:onConfigDataCallback cjson failed: " .. msg)
                return
            end
            local fileName = jsonData.filename
            local fileUrl = jsonData.url
            local fileMD5 = jsonData.md5
            self:reqDownFile(fileUrl,fileName,fileMD5)
        end
    else
        local view = XH.viewManager:getView(XH.SceneManager:getInstance():getTopSceneTag(), "H5GameWidgetView")
        if view then
            view:close()
        end
    end
end

function H5GameWidgetModule:reqDownFile(fileUrl,fileName,fileMD5)
    if DEBUG then
        return 
    end
    local filePath = cc.FileUtils:getInstance():getWritablePath() .. fileName
    local isFileEXist = cc.FileUtils:getInstance():isFileExist(filePath)
    if isFileEXist and fileMD5 == un.getMd5OfFile(filePath) then
        return
    end

    self:startDownFile(fileUrl,fileName,filePath)
end

function H5GameWidgetModule:startDownFile(fileUrl,fileName,filePath)
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
            self:loadConfig(fileName)
        else
            self._failDownloadCount = self._failDownloadCount + 1
            self:startDownFile(fileUrl,fileName,filePath)
        end
        print("downFile fileUrl errCode:" .. errCode .. " tag:" .. tag)
    end)
    downloader:add(fileUrl, filePath, "", fileUrl)
    downloader:removeWhenAllTaskComplete()
end

function H5GameWidgetModule:loadConfig(fileName, iconType, gameid)
    local filePath = cc.FileUtils:getInstance():getWritablePath() .. fileName
    local isFileEXist = cc.FileUtils:getInstance():isFileExist(filePath)
    if not isFileEXist then
        return
    end
    local jsonData = { }
    local H5GameJson = cc.FileUtils:getInstance():getStringFromFile(filePath)
    if H5GameJson ~= nil and H5GameJson ~= "" then
        local ok, msg = pcall( function()
            jsonData = cjson.decode(H5GameJson)
        end )
        if not ok then
            print("H5GameWidgetModule:onConfigDataCallback cjson failed: " .. msg)
            return
        end
    end
    self._H5GameConfig = jsonData
    if iconType then
        self:showH5GameWidget(iconType, gameid)
    else
        self:dispatchEvent( { name = H5GameWidgetModule.H5GAME_EVENT_CONFIG })
    end
end

function H5GameWidgetModule:createConfig(fileName, data)
    local filePath = cc.FileUtils:getInstance():getWritablePath() .. fileName
    cc.FileUtils:getInstance():createDirectory(filePath)
    local isFileEXist = cc.FileUtils:getInstance():isFileExist(filePath)
    if isFileEXist then
        self:saveConfig(fileName,data)
    end
end

function H5GameWidgetModule:saveConfig(fileName, data)
    local filePath = cc.FileUtils:getInstance():getWritablePath() .. fileName
    cc.FileUtils:getInstance():writeStringToFile(tostring(data),filePath)
end

function H5GameWidgetModule:clearH5GameConfig()
    self._H5GameConfig = nil
    self._maxDownloadCount = 3
    self._failDownloadCount = 0
end

------------------------------H5游戏------------------------------
function H5GameWidgetModule:startH5Game(gameName,isWeChatId)
    if device.platform ~= "android" then
        return
    end
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_JOIN_LIANYUN_GAME,{gameName = gameName})

    if XH.playerData:isSpecialBYID() and not XH.SysTool:CompareToBundleVersion("1.3.1") then
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK_CANCEL_NO_CLOSE,
            funcOK = function()
                XH.SysTool.downloadNewVersionApp()
            end
        } , "当前版本不支持，是否前往下载最新版本")
        return
    end
    
    if XH.sdkManager:isSupportFunctionAndroid("start_game_login") == "false" then
        XH.TipTool.showToast("当前版本不支持，请下载最新版本")
        return
    end

    if self._loginLock then
        XH.TipTool.showToast("您操作的太频繁了，请稍后再试")
        return
    end

    self._loginLock = true
    if XH.sdkManager:isSupportFunctionAndroid("start_game_login") then
        if self._gameid then
            XH.sdkManager:closeGoldGame()
        end
        XH.audioManager:SetMusicVolume(0)
        XH.audioManager:SetSFXVolume(0)
        local reqH5LoginCer = require("app.Req.H5Game.ReqH5StartGameLogin").new()
        reqH5LoginCer:addReqCallBack(self, self.h5StartGameLoginCallback)
        local nickname = XH.playerData:getNickName()
        local sex = XH.playerData:getSex()
        local id = XH.playerData:getNumberID()
        local url = XH.playerData:getWeChatURL()
        if gameName == H5GameNameList.LEGEND and XH.playerData:getLegendUserID() ~= "" then
            id = XH.playerData:getLegendUserID()
        elseif gameName == H5GameNameList.BUYU and XH.playerData:getBuYuUserID() ~= "" then
            id = XH.playerData:getBuYuUserID()
        elseif gameName == H5GameNameList.BUYU and XH.playerData:getWeChatBuYuUserID() ~= "" and isWeChatId then 
            id = XH.playerData:getWeChatBuYuUserID()
        end
        local bNeedConfigID = XH.playerData:isNeedConfigID() == 1
        reqH5LoginCer:start(id, nickname, sex, url, reqH5LoginCer.GameList[gameName], bNeedConfigID, isWeChatId)
    else
        XH.TipTool.showToast("not support function!")
    end
    XH.SysTool.performDelayOnce(function()
        self._loginLock = false
    end, 5)
end

function H5GameWidgetModule:setLateGoFun(fun, text)
    if fun then
        self._lateFun = fun
        self._lateText = text
    end
end

function H5GameWidgetModule:runLateGoFun()
    local cnt = 5
    self._listener = cc.Director:getInstance():getScheduler():scheduleScriptFunc( function()
        XH.TipTool.showToast(cnt .. "s后即将前往".. self._lateText, 3)
        cnt = cnt - 1
        if cnt == -1 and self._lateFun then
            self._lateFun()
            self._lateFun = nil
            self._lateText = ""
            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._listener)
        end
    end , 1, false)
end

function H5GameWidgetModule:setGameData(roomID,gameName,scene,block)
    if roomID then
        self._roomId = roomID
    end
    if gameName then
        self._gameName = gameName
    end
    if scene then
        self._blockLabel = scene
    end
    if block then
        self._block = block
    end
end

function H5GameWidgetModule:getGameData()
    local tmpData = {}
    tmpData.roomId = self._roomId
    tmpData.gameName = self._gameName
    tmpData.blockLabel = self._blockLabel
    tmpData.block = self._block
    return tmpData
end

return H5GameWidgetModule   �/  