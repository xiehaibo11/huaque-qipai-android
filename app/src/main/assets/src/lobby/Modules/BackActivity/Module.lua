local BackActivityModule = class("BackActivityModule", XH.ModuleBase)
local BackActivityConfig = require("lobby.Modules.BackActivity.Config")
local TaskDefine = require("app.Define.TaskDefine")
local DEFAULT_DOWNLOAD_IMAGE_DIR = device.writablePath .. "tmpImageCachDir/"

BackActivityModule.KW_BACK_ACT_SHOW_ICON = "BackActivityShowIcon"
BackActivityModule.KW_BACK_ACT_FLUSH_INFO = "BackActivityFlushInfo"
BackActivityModule.KW_BACK_ACT_DOWNLOAD_IMAGE = "BackActivityDownloadImage"

BackActivityModule.EVENT_SHOW_BIG_WIN_LOST_UI = "EVENT_SHOW_BIG_WIN_LOST_UI"
BackActivityModule.EVENT_GET_ACT_INFO_BACK = "EVENT_GET_ACT_INFO_BACK"

function BackActivityModule:ctor()
    BackActivityModule.super.ctor(self)
    self._actInfo = nil
    self._actAwardInfo = nil
    self._reqInfoType = 1
    self._changeInfo = false
    self._isNeedFlushAllAward = true
    self._playAward = nil
    self._isGoldShow = false
    self._isNeedShowSgin = false
    self._nowTime = 0
end

function BackActivityModule:getReqConfig()
    return {
        ReqBackActProtocol = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.reqBackActProtocolCallBack },
        ReqBackAwardProtocol = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.reqBackAwardProtocolCallBack },
        ReqPlayerGoldCoin = { reqPath = "app.Req.PlayerMessage.ReqPlayerGoldCoin", callBack = self.onTranPlayerGoldCallBack },
    }
end

function BackActivityModule:getProxyEvents()
    return {
        { module = XH.netEngine, eventName = XH.MatchLinkProtocol.RespNotifyTaskPercent.event_key, callBack = "onRespNotifyTaskPercent" },
    }
end
   
function BackActivityModule:reqDownloadImage(image_url)
    local downloadImage = require("app.Req.DownloadImage")
    local reqDownloadImage = downloadImage:new()
    reqDownloadImage:addReqCallBack(self, self.reqDownloadImageCallBack)
    reqDownloadImage:setUrl(image_url, false)
end

function BackActivityModule:reqDownloadImageCallBack(cell, ntype, backData)
    if ntype == cell.TYPE.SUCCESS then
        local imageName = XH.StringTool.getImageNameByUrl(backData)
        local imagePath = DEFAULT_DOWNLOAD_IMAGE_DIR .. imageName
--        node:loadTexture(imagePath, ccui.TextureResType.localType)
--        node:setVisible(true)
        local acData = {}
        acData.imagePath = imagePath
        acData.url = backData
        self:dispatchEvent( { name = BackActivityModule.KW_BACK_ACT_DOWNLOAD_IMAGE, data = acData })
    end
end

function BackActivityModule:reqBackActProtocol(inLobby)
    if inLobby then
        local todayLoginCount = XH.userDefault:getValue(XH.userDefault.KEY_ID.TODAY_BACK_ACT, 0)
        if todayLoginCount < 1 then
            --self._changeInfo = true
            XH.userDefault:setValue(XH.userDefault.KEY_ID.TODAY_BACK_ACT, todayLoginCount + 1)
        end
    else
        self._changeInfo = true
    end
    if self._nowTime ~= 0 then
        local nowDay = os.date("%d",self._nowTime)
        local newDay = os.date("%d",os.time())
        if newDay ~= nowDay and self._actAwardInfo ~= nil then 
            self._actAwardInfo = nil 
            self._actInfo = nil
        end
    end
    if self:isShowLayer() then
        if self._actAwardInfo == nil then
            self._nowTime = os.time()
            self._reqInfoType = 4
            self._actAwardInfo = { }
            local dataString = ";infoId = " .. self._reqInfoType .. ";"
            self:startReq("ReqBackActProtocol", TaskDefine.TASK_ID.TASK_TYPE_BACK_ACT_INFO, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
        else
            local dataString = ";infoId = 1;"
            self:startReq("ReqBackActProtocol", TaskDefine.TASK_ID.TASK_TYPE_BACK_ACT_INFO, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
        end
    end
end

function BackActivityModule:reqBackActProtocolCallBack(ncell, ntype, data)
    if data == nil or data == "" or data.acData == nil or data.acData == "" then
        XH.TipTool.hideLoading()
        self:dispatchEvent( { name = BackActivityModule.KW_BACK_ACT_SHOW_ICON, data = { } })
        return
    end
    local actData = cjson.decode(data.acData)
    if ntype == XH.Req.TYPE.SUCCESS then
        if actData.actTime then
            self._actInfo = actData
        else
            for key, val in pairs(actData) do
                self._actAwardInfo[key] = val
            end
            self._reqInfoType = self._reqInfoType - 1
            local dataString = ";infoId=" .. self._reqInfoType .. ";"
            self:startReq("ReqBackActProtocol", TaskDefine.TASK_ID.TASK_TYPE_BACK_ACT_INFO, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
        end
        if self._actInfo and self._actAwardInfo then
            if self._changeInfo then
                self:dispatchEvent( { name = self.EVENT_GET_ACT_INFO_BACK })
                -- XH.viewManager:openView("BackActivityView")
                self._changeInfo = false
            end
            if self._isNeedShowSgin then
                self:showSignLayer()
            end
            self:dispatchEvent( { name = BackActivityModule.KW_BACK_ACT_SHOW_ICON, data = actData })
        end
    else
        XH.TipTool.hideLoading()
        if self._isNeedShowSgin then
            self:showSignLayer()
        end
    end
end

function BackActivityModule:reqBackAwardProtocol(awardType, awardId)
    local dataString = ";awardType=\"" .. awardType .. "\";awardId=" .. awardId .. ";"
    self:startReq("ReqBackAwardProtocol", TaskDefine.TASK_ID.TASK_TYPE_BACK_ACT_GET_AWARD, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
end

function BackActivityModule:reqBackAwardProtocolCallBack(ncell, ntype, data)
    if data == nil or data == "" or data.acData == nil or data.acData == "" then
        XH.TipTool.hideLoading()
        return
    end
    local actData = cjson.decode(data.acData)
    if ntype == XH.Req.TYPE.SUCCESS then
        if actData.awardData then
            XH.viewManager:openView("BackActivityAwardView", nil, nil, actData.awardData)
            self._actInfo = actData.playerData
            self._isNeedFlushAllAward = true
            self:dispatchEvent( { name = BackActivityModule.KW_BACK_ACT_FLUSH_INFO })
        end
    else
        XH.TipTool.hideLoading()
    end
end

function BackActivityModule:getActInfo()
    return self._actInfo
end

function BackActivityModule:getActAwardInfo()
    return self:changeBindCardAward(self._actAwardInfo)
end

function BackActivityModule:getLoginDay()
    if self._actInfo then
        return self._actInfo.loginDay
    end
    return nil
end

function BackActivityModule:getRechargeDay()
    if self._actInfo then
        return self._actInfo.rechargeDay
    end
    return nil
end

function BackActivityModule:getThreeTime()
    if self._actInfo then
        return self._actInfo.threeDayTime
    end
    return nil
end

function BackActivityModule:onRespNotifyTaskPercent(event)
    local respData = XH.MatchLinkProtocol.RespNotifyTaskPercent:new()
    respData:bistream(event.msg.buff, event.msg.len)

    if respData.nTaskId == TaskDefine.TASK_ID.TASK_TYPE_BACK_ACT_COUNT then
        local actData = cjson.decode(respData.acData)
        if actData.awardData then
            self._playAward = actData.awardData
            if actData.isGold then
                XH.viewManager:openView("BackActivityAwardView", nil, nil, actData.awardData)
                self._playAward = nil
            end
            self._changeInfo = true
            self._isNeedFlushAllAward = true
        elseif actData.isChange then
            self._changeInfo = true
        end
    end
end

function BackActivityModule:isNeedFlushAllAward()
    local isNeed = self._isNeedFlushAllAward
    self._isNeedFlushAllAward = false
    return isNeed
end

function BackActivityModule:onEventShowBigWinLost(event)
    if self._playAward ~= nil then
        XH.viewManager:openView("BackActivityAwardView", nil, nil, self._playAward)
        self._playAward = nil
    end
end

function BackActivityModule:onEventMsgResult(event)

end

function BackActivityModule:isShowLayer()
    return BackActivityConfig.isShowArea[XH.areaData:getLobbyID()]
end

function BackActivityModule:needShowSign()
    if not XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_SHOW_TODAY, false) then
        self._isNeedShowSgin = true
    end
end

function BackActivityModule:showSignLayer()
    if not XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_SHOW_TODAY, false) then
        XH.viewManager:openView("TabsActivityView")
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_NEWUSER_ACT_SHOW_TODAY, true)
        self._isNeedShowSgin = false
    end
end

function BackActivityModule:flushGoldCoin()
    local gameID = XH.areaData:getCommonGoldGameID()
    local srsGroupID = XH.areaData:getSrsGroupID()
    self:startReq("ReqPlayerGoldCoin", gameID, srsGroupID, 10)
end

function BackActivityModule:onTranPlayerGoldCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data.purse then
            XH.playerData:setSR(data.purse)
        else
            XH.playerData:setSR(0)
        end
    end
end

function BackActivityModule:fulshPlayerProp()
    self._listener = cc.Director:getInstance():getScheduler():scheduleScriptFunc( function()
        XH.playerData:flushPlayerDrop()
        self:flushGoldCoin()
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._listener)
    end , 2, false)
end

function BackActivityModule:changeBindCardAward(awards)
    local awardList = clone(awards)
    for key,value in pairs(awardList) do
        if key == "rechargeAward" or key == "playGoldAllAward" or key == "playBoxAllAward" then
            if type(value) == "table" then
                for _ ,v in pairs(value) do
                    if type(v) == "table" then
                        for k,sv in pairs(v) do
                            sv.cnt = XH.playerData:changeBindCardRate(k,sv.cnt)
                        end
                    end
                end
            end
        elseif key == "boxAward" or key == "goldAward" then
            if type(value) == "table" then
                for k ,val in pairs(value) do
                   if k == "play" then
                        if type(val) == "table" then
                            for _, v in pairs(val) do
                                v.cnt = XH.playerData:changeBindCardRate(v.propid,v.cnt) 
                            end
                        end
                   else
                        for sk ,v in pairs(val) do
                            v.cnt = XH.playerData:changeBindCardRate(sk,v.cnt)
                        end
                   end
                end
            end
        end 
    end
    return awardList
end

return BackActivityModule  ž+  