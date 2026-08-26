local NewPlayTypeModule = class("PhoneBindModule", XH.ModuleBase)
local GameNewTypeConfiger = require("app.Config.Parse.GameNewTypeConfiger")

NewPlayTypeModule.EVENT_GET_ACT_INFO = "EVENT_GET_ACT_INFO"
NewPlayTypeModule.EVENT_GET_AWARD_SUCCESS = "EVENT_GET_AWARD_SUCCESS"
NewPlayTypeModule.EVENT_GET_ACT_INFO_NEW = "EVENT_GET_ACT_INFO_NEW"
NewPlayTypeModule.EVENT_GET_GREET_INFO = "EVENT_GET_GREET_INFO"
NewPlayTypeModule.EVENT_AWARD_HISTORY = "EVENT_AWARD_HISTORY"
NewPlayTypeModule.EVENT_GET_GREET_PRIZE = "EVENT_GET_GREET_PRIZE"
NewPlayTypeModule.EVENT_UPDATE_ACT_INFO = "EVENT_UPDATE_ACT_INFO"

NewPlayTypeModule.TEA_HOUSE_TYPE = {
    FREE = 1,
    WITHNEWTYPEMODE = 2,
    WITHOUTNEWTYPEMODE = 3,
}

NewPlayTypeModule.KW_STAGE_STATE = {
    INCONFORMITY = 0,
    ACHIEVE = 1,
    GETED = 2
}

NewPlayTypeModule.AUTO_CREATE_TIPS_TYPE = {
    NOTGETNOBOOK = 1,
    NOTGETANDBOOK = 2,
    GETEDAWARDWITHOUTMODE = 3,
    MODECOUNTMAX = 4
}

NewPlayTypeModule.DEL_AUTO_MODE_TIPS_TYPE = {
    NOTGET = 1,
    GETEDAWARDNOTTIMEOUT = 2,
    GETEDAWARDTIMEOUT = 3
}

local KW_TRANSIT_TIME = 7*24*60*60

function NewPlayTypeModule:ctor()
    NewPlayTypeModule.super.ctor(self)
end

function NewPlayTypeModule:getReqConfig()
    return {
        ReqNewPlayTypeOrder = { reqPath = "lobby.Req.NewPlayType.ReqNewPlayTypeOrder" },
        ReqNewPlayTypeActInfo = { reqPath = "lobby.Req.NewPlayType.ReqNewPlayTypeActInfo", callBack = self.onReqNewPlayTypeActInfoCallback },
        ReqNewPlayTypeGetAward = { reqPath = "lobby.Req.NewPlayType.ReqNewPlayTypeGetAward", callBack = self.onReqNewPlayTypeGetAwardCallback },
        ReqNewPlayTypeActInfoNew = { reqPath = "lobby.Req.NewPlayType.ReqNewPlayTypeActInfoNew", callBack = self.onReqNewPlayTypeActInfoNewCallback },
        ReqNewPlayTypeGreetInfo = { reqPath = "lobby.Req.NewPlayType.ReqNewPlayTypeGreetInfo", callBack = self.onReqNewPlayTypeGreetInfoCallback },
        ReqNewPlayTypeGreetPrize = { reqPath = "lobby.Req.NewPlayType.ReqNewPlayTypeGetGreetPrize", callBack = self.onReqNewPlayTypeGreetPrizeCallback },
        ReqNewPlayTypeCheckAct = { reqPath = "lobby.Req.NewPlayType.ReqNewPlayTypeCheckAct", callBack = self.onReqNewPlayTypeCheckActCallback },
        ReqNewPlayTypeGetFirstPrize = { reqPath = "lobby.Req.NewPlayType.ReqNewPlayTypeGetFirstPrize", callBack = self.onReqNewPlayTypeGetFirstPrizeCallback },
        ReqNewPlayTypeGetPrize = { reqPath = "lobby.Req.NewPlayType.ReqNewPlayTypeGetPrize", callBack = self.onReqNewPlayTypeGetPrizeCallback },
        ReqNewPlayTypeAwardHistory = { reqPath = "lobby.Req.NewPlayType.ReqNewPlayTypeAwardHistory", callBack = self.onReqNewPlayTypeAwardHistoryCallback },
    }
end

function NewPlayTypeModule:reqNewPlayTypeOrder(teaOwnerID, teaNumber)
    self:startReq("ReqNewPlayTypeOrder", teaOwnerID, teaNumber, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function NewPlayTypeModule:reqNewPlayTypeActInfo(bJustCheck)
    if not self:checkTeaHouseInfo() then
        return
    end
    self._getInfoNotOpenView = bJustCheck
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    self:startReq("ReqNewPlayTypeActInfo", teahouseData:getTeaNumber(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function NewPlayTypeModule:onReqNewPlayTypeActInfoCallback(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        self._newPlayTypeActInfoEndTime = data.actEnd
        if self._getInfoNotOpenView then
            self:dispatchEvent( { name = self.EVENT_GET_ACT_INFO, info = data })
        else
            if data.actEnd and os.date("%Y-%m-%d", os.time() - KW_TRANSIT_TIME) > data.actEnd then
                return
            end
            XH.viewManager:openView("NewPlayTypeToBView", nil, {info = data})
        end
    end
end

function NewPlayTypeModule:reqTeaHousePayType()
    if not self:checkTeaHouseInfo() then
        return
    end
    XH.TipTool.showLoading()
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    local teaHouseManager = XH.teaHouseManager:getTeaHouseManager()
    local netReqTeaHousePayType = teaHouseManager.reqManager:createReq("NetReqTeaHousePayType")
    netReqTeaHousePayType:addReqCallBack(self, self.onTeaHousePayType)
    netReqTeaHousePayType:start(teahouseData:getTeaNumber(), 10)
end

function NewPlayTypeModule:onTeaHousePayType(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        local payTypeTable = data.anPaySet
        local teaHouseManager = XH.teaHouseManager:getTeaHouseManager()
        --兼容服务端逻辑，当modetype为0时，需要根据不同区特判
        if data.nPayModeType == 0 then
            local payModeTypeConf = teaHouseManager.configManager:loadConfig("PayModeTypeConfig")
            local lobbyID = XH.areaData:getLobbyID()
            if payModeTypeConf[tonumber(lobbyID)] then
                data.nPayModeType = payModeTypeConf[tonumber(lobbyID)]
            else
                local bLiuTongMode = false
                for _, tempPayType in pairs(payTypeTable) do
                    if tempPayType == XH.TeaHouseProtocol.ReqSetPayType.PayType.TYPE_TEA_PLAYER_OWNER_CARD_AA then
                        bLiuTongMode = true
                    end
                end
                --如果是流通模式，则不处理
                --如果还是0，默认为后项付费
                if not bLiuTongMode then
                    data.nPayModeType = XH.TeaHouseProtocol.ReqSetPayType.PayModePype.TYPE_PAY_MODEL_BEHID
                end
            end
        end
        local teaPayModeType = data.nPayModeType
        teaHouseManager.teahouseSetup:setTeaNumber(teaHouseManager.teahouseData:getTeaNumber())
        teaHouseManager.viewManager:openView("TeaHouseCreatePlayModeView", 0, { playModeId = 0, payModeType = teaPayModeType, payTypeTable = payTypeTable, openLayer = "", nGameId = GameNewTypeConfiger:getDefultGameIDByLobbyID(XH.areaData:getLobbyID())})
    end
end

function NewPlayTypeModule:reqGetStageAward(stageID)
    if not self:checkTeaHouseInfo() then
        return
    end
    XH.TipTool.showLoading()
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    self:startReq("ReqNewPlayTypeGetAward", stageID, teahouseData:getTeaNumber(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function NewPlayTypeModule:onReqNewPlayTypeGetAwardCallback(cell, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent( { name = self.EVENT_GET_AWARD_SUCCESS })
    elseif type == XH.Req.TYPE.FAIL then
        if cell:getMessage() and cell:getMessage() ~= "" then
            XH.TipTool.showToast(cell:getMessage())
        else
            XH.TipTool.showToast("领取失败！请稍后重试")
        end
    end
end

function NewPlayTypeModule:checkTeahouseType()
    if not self:checkTeaHouseInfo() then
        return
    end    
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    if teahouseData:getPlayModeInfoFlag() and teahouseData:getPlayModeInfosCount() > 0 then
        return self:checkNewPlaytypeMode()
    elseif not teahouseData:needShowCreateTable() then
        return self:checkNewPlaytypeTables()
    else
        return self.TEA_HOUSE_TYPE.FREE
    end
end

function NewPlayTypeModule:checkTeaHouseInfo()
    if not XH.teaHouseManager:isInTeaHouse() then
        return
    end
    if not XH.teaHouseManager:getTeaHouseManager() then
        return
    end
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    if not teahouseData then
        return
    end
    return true
end

function NewPlayTypeModule:orderNewPlayTypeAndShare(withoutShare)
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    if teahouseData then
        local teahouseInfo = teahouseData:getTeaHouseInfoByID(teahouseData:getTeaNumber())
        if not teahouseInfo or not teahouseInfo.nTeaOwnerNumid then
            return
        end
        self:reqNewPlayTypeOrder(teahouseInfo.nTeaOwnerNumid, teahouseData:getTeaNumber())
    end
    -- if not withoutShare then
    --     local shareInfo = {
    --         textTitle = "浙江游戏大厅",
    --         imagePath = "https://palmastatic.wenzhoushuke.com//materials/images/20220826/15521465784.png",
    --         type = "pic",
    --         scene = 1,
    --         imageUrl = "",
    --         text = "",
    --         sharePath = "",
    --     }
    --     XH.sdkManager:startShare(shareInfo)
    -- end
end

function NewPlayTypeModule:checkNewPlaytypeMode()
    local GameNewTypeCfg = require("app.Config.GameNewTypeConfig").GameNewTypeConfByGameID
    if not GameNewTypeCfg then
        return
    end
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    local playModeInfos = teahouseData:getPlayModeInfos()
    for _, playModeInfo in pairs(playModeInfos) do
        for _, v in pairs(GameNewTypeCfg) do
            if v.LobbyID == XH.areaData:getLobbyID() then
                if not next(v.NewPlayTypeNode) and v.GameID == playModeInfo.nGameId then
                    return self.TEA_HOUSE_TYPE.WITHNEWTYPEMODE
                else
                    for _, newType in pairs(v.NewPlayTypeNode) do
                        local nodeInfo = loadstring("return " .. newType)()
                        if nodeInfo then
                            local nodeName = nodeInfo[4] or nodeInfo[1]
                            if string.find(nodeName, "playCount_") then
                                local playCount = XH.StringTool.getNumberSuffixByString(nodeName)
                                if XH.TableTool.isValueInTable(playCount, playModeInfo.anCount) then
                                    return self.TEA_HOUSE_TYPE.WITHNEWTYPEMODE
                                end
                            elseif string.find(nodeName, "playerCount_") then
                                local playerCount = XH.StringTool.getNumberSuffixByString(nodeName)
                                if XH.TableTool.isValueInTable(playerCount, playModeInfo.anPlayerCount) then
                                    return self.TEA_HOUSE_TYPE.WITHNEWTYPEMODE
                                end
                            elseif string.find(playModeInfo.acData, nodeName) then
                                return self.TEA_HOUSE_TYPE.WITHNEWTYPEMODE
                            end
                        end
                    end
                end
            end
        end
    end
    return self.TEA_HOUSE_TYPE.WITHOUTNEWTYPEMODE
end

function NewPlayTypeModule:checkNewPlaytypeTables()
    local GameNewTypeCfg = require("app.Config.GameNewTypeConfig").GameNewTypeConfByGameID
    if not GameNewTypeCfg then
        return
    end
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    local tablesInfos = teahouseData:getTableInfos()
    for _, tableInfo in pairs(tablesInfos) do
        for _, v in pairs(GameNewTypeCfg) do
            if v.LobbyID == XH.areaData:getLobbyID() then
                if not next(v.NewPlayTypeNode) and v.GameID == tableInfo.gameid then
                    return self.TEA_HOUSE_TYPE.WITHNEWTYPEMODE
                else
                    for _, newType in pairs(v.NewPlayTypeNode) do
                        local nodeInfo = loadstring("return " .. newType)()
                        if nodeInfo then
                            local nodeName = nodeInfo[4] or nodeInfo[1]
                            if string.find(nodeName, "playCount_") then
                                local playCount = XH.StringTool.getNumberSuffixByString(nodeName)
                                if tableInfo.count and playCount == tableInfo.count then
                                    return self.TEA_HOUSE_TYPE.WITHNEWTYPEMODE
                                end
                            elseif string.find(nodeName, "playerCount_") then
                                local playerCount = XH.StringTool.getNumberSuffixByString(nodeName)
                                if tableInfo.size and playerCount == tableInfo.size then
                                    return self.TEA_HOUSE_TYPE.WITHNEWTYPEMODE
                                end
                            elseif tableInfo.data and string.find(tableInfo.data, nodeName) then
                                return self.TEA_HOUSE_TYPE.WITHNEWTYPEMODE
                            end
                        end
                    end
                end
            end
        end
    end
    return self.TEA_HOUSE_TYPE.WITHOUTNEWTYPEMODE
end

function NewPlayTypeModule:reqNewPlayTypeActInfoNew(bJustCheck)
    if not self:checkTeaHouseInfo() then
        return
    end
    self._getInfoNotOpenViewNew = bJustCheck
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    self:startReq("ReqNewPlayTypeActInfoNew", teahouseData:getTeaNumber(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function NewPlayTypeModule:onReqNewPlayTypeActInfoNewCallback(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        self._newPlayTypeActInfoEndTime = data.act_end_date
        if  self._getInfoNotOpenViewNew then
            self:dispatchEvent( { name = self.EVENT_GET_ACT_INFO_NEW, info = data })
        else
            if data.act_end_date and os.date("%Y-%m-%d", os.time() - KW_TRANSIT_TIME) > data.act_end_date then
                return
            end
            XH.viewManager:openView("NewPlayTypeToBViewNew", nil, {info = data})
        end
    end
end

function NewPlayTypeModule:reqNewPlayTypeGreetInfo(teaNumber, eventName)
    if not teaNumber or teaNumber == 0 then
        return
    end
    self._greetInfoEventName = eventName
    self:startReq("ReqNewPlayTypeGreetInfo", teaNumber, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function NewPlayTypeModule:onReqNewPlayTypeGreetInfoCallback(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        self._greetInfo = data
        if self._greetInfoEventName then
            self:dispatchEvent( { name = self._greetInfoEventName, info = data })
        end
    end
end

function NewPlayTypeModule:getGreetInfo()
    return self._greetInfo
end

function NewPlayTypeModule:getGreetPrize()
    if not self:checkTeaHouseInfo() then
        return
    end
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    self:startReq("ReqNewPlayTypeGreetPrize", teahouseData:getTeaNumber(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function NewPlayTypeModule:onReqNewPlayTypeGreetPrizeCallback(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local awardData = {[1] = {}}
        awardData[1].url = "ico_card_4.png"
        awardData[1].desc = (data.prop_count or "") .. (data.prop_name or "")
        XH.viewManager:openView("CommonRewardView", nil, awardData, true)
        XH.teaHouseManager:autoCreateNewPlayTypeMode(1, 1)
        self:dispatchEvent( { name = self.EVENT_GET_GREET_PRIZE })
    else
        XH.TipTool.showToast("领取失败")
    end
end

function NewPlayTypeModule:reqNewPlayTypeCheckAct(bJustCheck)
    if not self:checkTeaHouseInfo() then
        return
    end
    self._justCheck = bJustCheck
    if XH.areaData:getLobbyID() == XH.LOBBY_ID.ZHOUSHAN then
        self:reqNewPlayTypeActInfo(self._justCheck)
        return
    end
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    self:startReq("ReqNewPlayTypeCheckAct", teahouseData:getTeaNumber(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function NewPlayTypeModule:onReqNewPlayTypeCheckActCallback(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data.new_act and data.new_act == 1 then
            self:reqNewPlayTypeActInfoNew(self._justCheck)
        else
            self:reqNewPlayTypeActInfo(self._justCheck)
        end
    else
        XH.TipTool.showToast("活动类型请求失败，请稍后重试！")
    end
end

function NewPlayTypeModule:reqGetFirstPrize()
    if not self:checkTeaHouseInfo() then
        return
    end
    XH.TipTool.showLoading()
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    self:startReq("ReqNewPlayTypeGetFirstPrize", teahouseData:getTeaNumber(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function NewPlayTypeModule:onReqNewPlayTypeGetFirstPrizeCallback(cell, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        local awardData = {[1] = {}}
        awardData[1].url = "ico_card_4.png"
        awardData[1].desc = (data.prop_count or "") .. (data.prop_name or "")
        XH.viewManager:openView("CommonRewardView", nil, awardData, true)
        self:dispatchEvent( { name = self.EVENT_UPDATE_ACT_INFO })
    else
        XH.TipTool.showToast("领取失败，请稍后重试！")
    end
end

function NewPlayTypeModule:reqGetPrize()
    if not self:checkTeaHouseInfo() then
        return
    end
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    self:startReq("ReqNewPlayTypeGetPrize", teahouseData:getTeaNumber(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function NewPlayTypeModule:onReqNewPlayTypeGetPrizeCallback(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local awardData = {[1] = {}}
        awardData[1].url = "ico_card_4.png"
        awardData[1].desc = (data.prop_count or "") .. (data.prop_name or "")
        XH.viewManager:openView("CommonRewardView", nil, awardData, true)
        self:dispatchEvent( { name = self.EVENT_UPDATE_ACT_INFO })
    else
        XH.TipTool.showToast("领取失败，请稍后重试！")
    end
end

function NewPlayTypeModule:reqNewPlayTypeAwardHistory()
    if not self:checkTeaHouseInfo() then
        return
    end
    local teahouseData = XH.teaHouseManager:getTeaHouseData()
    self:startReq("ReqNewPlayTypeAwardHistory", teahouseData:getTeaNumber(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function NewPlayTypeModule:onReqNewPlayTypeAwardHistoryCallback(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent( { name = self.EVENT_AWARD_HISTORY, info = data })
    else
        XH.TipTool.showToast("获取失败，请稍后重试！")
    end
end

function NewPlayTypeModule:checkHaveAutoCreateMode()
    local autoPlayModeInfos = GameNewTypeConfiger:getAutoCreatePlayModeInfoByLobbyID(XH.areaData:getLobbyID())
    if autoPlayModeInfos then
        local teahouseData = XH.teaHouseManager:getTeaHouseData()
        local playModeInfos = teahouseData:getPlayModeInfos()
        for _, playModeInfo in pairs(playModeInfos) do
            if playModeInfo.bIsEnable and playModeInfo.nGameId == autoPlayModeInfos.gameID then
                return true
            end
        end
        return false
    end
    return true
end

function NewPlayTypeModule:checkInNewPlayTypeActTime()
    if self._newPlayTypeActInfoEndTime then
        if os.date("%Y-%m-%d", os.time() - KW_TRANSIT_TIME) < self._newPlayTypeActInfoEndTime then
            return true
        end
    end
    return
end

return NewPlayTypeModule 3K  