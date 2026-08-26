local ToponActModule = class("ToponActModule", XH.ModuleBase)
local ToponActConfig = require("lobby.Modules.ToponAct.Config")
local TaskDefine = require("app.Define.TaskDefine")
local LobbyConfig = require("lobby.Modules.Lobby.Config")

ToponActModule.KW_TOPON_ACT_SHOW_ICON = "KW_TOPON_ACT_SHOW_ICON"
ToponActModule.KW_TOPONGOLDAREADRAW_VIEW_CLOSE = "KW_TOPONGOLDAREADRAW_VIEW_CLOSE"
ToponActModule.KW_TOPON_ACT_FULSH_CNT = "KW_TOPON_ACT_FULSH_CNT"
ToponActModule.KW_TOPON_GOLD_BREAK_SHOW = "KW_TOPON_GOLD_BREAK_SHOW"
ToponActModule.KW_TOPON_ACT_FULSH_LOBBY_WAIT = "KW_TOPON_ACT_FULSH_LOBBY_WAIT"
ToponActModule.KW_TOPON_ACT_SHIN_INFO = "KW_TOPON_ACT_SHIN_INFO"
ToponActModule.EVENT_NEED_TIME = "EVENT_NEED_TIME"
ToponActModule.KW_TOPON_ACT_SHOW_SIGN = "KW_TOPON_ACT_SHOW_SIGN"
ToponActModule.KW_TOPON_ACT_PHONE_FEE_INFO = "KW_TOPON_ACT_PHONE_FEE_INFO"
local KW_TIME_EXPIRE = 8

ToponActModule.KW_TOPON_ACT_NEW_FEE_GET_AWARD = "KW_TOPON_ACT_NEW_FEE_GET_AWARD"

function ToponActModule:ctor()
    ToponActModule.super.ctor(self)
    self._isWin = nil
    self._toponActInfo = nil
    self._isShowBreakGold = false
    self._webType = nil
    self._dailySignWebType = nil
    self._poolid = nil
    self._isCast = nil
    self._roomInfo = nil
    self._isHoting = false
    self._showIconList = {}
    self._refrehTimes = 0

    self._signActId = nil
end

function ToponActModule:getProxyEvents()
    return {
        --        { module = XH.netEngine, eventName = XH.MatchLinkProtocol.RespNotifyTaskPercent.event_key, callBack = "onRespNotifyTaskPercent" },
        { module = XH.login:getModule("Login"), eventKeyName = "LOGIN_EVENT_SUCCESS", callBack = "onLoginSuccessCallBack" },
        { module = XH.login:getModule("Login"), eventKeyName = "EVENT_RELINK_SUCCESS", callBack = "lobbyRelinkSuccess" },
        { module = XH.sdkManager, eventKeyName = "EVENT_TOPONSDK_CALLBACK", callBack = "onToponAdsShowCallBack" },
    }
end

function ToponActModule:getReqConfig()
    return {
        ReqToponActInfo = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.resqToponActInfo },
        ReqActInfo = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.resqActInfo },
        ReqActAward = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.resqActAward },
        ReqGoldActInfo = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.resqGoldActInfo },
        ReqPlayerGoldCoin = { reqPath = "app.Req.PlayerMessage.ReqPlayerGoldCoin", callBack = self.onTranPlayerGoldCallBack },
        ReqSetWaitCntAndLookAd = { reqPath = "app.Req.Task.ReqTaskProtocol"},
        ReqNewFeeActAward = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.resqNewFeeActAward },
        ReqSign = { reqPath = "lobby.Req.LuckTask.ReqSign", callBack = self.respSign },
        ReqSignDetail = { reqPath = "lobby.Req.LuckTask.ReqSignDetail", callBack = self.respSignDetail },
    }
end

----------------------------------------Method-----------------------------------------
function ToponActModule:isSign50()
    return true
end

function ToponActModule:setSignActId(actId)
    self._signActId = actId
end

function ToponActModule:getSignActId()
    return self._signActId
end

--//1: 签到 2：双倍签到 3:补签 4：翻倍 5:累计签到
--//补签/翻倍 第几天
function ToponActModule:reqSign(action, day)
    if self._signActId == nil or self.sign50Info == nil then
        return
    end
    day = self:getCurDay()
    action = action or ToponActConfig.SIGN_CONFIG.SIGN
    self:startReq("ReqSign", 10, self._signActId, action, day)
end

function ToponActModule:respSign(cell, ctype, data)
    XH.lobby:getModule("BagSysNew"):reqDoubleCards()
    if ctype == XH.Req.TYPE.SUCCESS then
        if data and data.res and data.res.reward then
            local actData = {}
            actData[#actData + 1] = {
                url = data.res.reward.icon,
                desc = data.res.reward.count .. data.res.reward.name,
            }
            XH.viewManager:openView("CommonRewardView", nil, actData, false)
            self:fulshPlayerProp()
            self:reqSignDetail()
        end
    else
        XH.viewManager:closeView("TabsActivityView")
        XH.TipTool.showToast("签到失败，请打开重试")
    end
end

function ToponActModule:reqSignDetail()
    if self._signActId == nil then
        return
    end
    self:startReq("ReqSignDetail", 10, self._signActId)
end

function ToponActModule:getCurDay()
    if self.sign50Info == nil then
        return
    end
    return self.sign50Info.totalSign
end

function ToponActModule:getLeftTime()
    local time = XH.lobby:getModule("Shop"):getServerTime()
    local currentDate = os.date("*t", time)
    local weekDay = currentDate.wday
    if weekDay == 1 then
        weekDay = 8
    end
    weekDay = weekDay - 1
    local endOfDay = os.time({ year = currentDate.year, month = currentDate.month, day = currentDate.day, hour = 23, min = 59, sec = 59 })
    local leftTIme = endOfDay - time
    return (7 - weekDay) * 86400 + leftTIme
end

function ToponActModule:respSignDetail(cell, ctype, data)
    if ctype == XH.Req.TYPE.SUCCESS then
        if data and data.res then
            local actData = self:transferData50(data.res)
            self:dispatchEvent( { name = ToponActModule.KW_TOPON_ACT_SHIN_INFO, data = actData })
        end
    end
end

function ToponActModule:transferData50(data)
    self.sign50Info = data
    local actData = {
        webType = ToponActConfig.DailySign,
        actInfo = {
            newSginInfo = {
                oldAwardInfo = {},
                newAwardInfo = {}, -- 勾选奖励加倍卡
                adAwardInfo = {}, -- 勾选看广告
            }
        }
    }
    table.sort(data.bases, function(a, b)
        return a.day < b.day
    end)
    local currTime = 0
    for i = 1, #data.bases do
        if data.bases[i].sub then
            currTime = 1
        end
        local tmpData = {
            image_url = data.bases[i].reward.icon,
            prize_name = data.bases[i].reward.count .. data.bases[i].reward.name,
            isSign = data.bases[i].multiple ~= 0,
            signType = data.bases[i].multiple
        }
        actData.actInfo.newSginInfo.oldAwardInfo[#actData.actInfo.newSginInfo.oldAwardInfo + 1] = clone(tmpData)
        actData.actInfo.newSginInfo.newAwardInfo[#actData.actInfo.newSginInfo.newAwardInfo + 1] = clone(tmpData)
        actData.actInfo.newSginInfo.adAwardInfo[#actData.actInfo.newSginInfo.adAwardInfo + 1] = clone(tmpData)
        if data.bases[i].multiple == 0 then
            actData.actInfo.newSginInfo.newAwardInfo[#actData.actInfo.newSginInfo.newAwardInfo].prize_name = data.bases[i].reward.count * 2 .. data.bases[i].reward.name
            if data.bases[i].ext then
                actData.actInfo.newSginInfo.adAwardInfo[#actData.actInfo.newSginInfo.adAwardInfo].prize_name = data.bases[i].reward.count * 2 .. data.bases[i].reward.name
                local ok, res_tab = pcall(json.decode, data.bases[i].ext)
                if ok and res_tab and res_tab.read_ad then
                    actData.actInfo.newSginInfo.adAwardInfo[#actData.actInfo.newSginInfo.adAwardInfo].prize_name = res_tab.read_ad .. data.bases[i].reward.name
                end
            end
        end
    end
    actData.actInfo.poolid = 1
    actData.actInfo.signTime = tonumber(data.expire or 0) --self:getLeftTime() -- 距离下周一的时间 秒
    actData.actInfo.currTime = currTime -- 上次签到时间
    actData.actInfo.times = data.totalSign -- 总签到次数
    XH.lobby:getModule("TabsActivity"):onShowSginRedPoint(currTime == 0)
    return actData
end

function ToponActModule:isOpen()
    return ToponActConfig.IS_OPEN[XH.areaData:getLobbyID()]
end

function ToponActModule:getStopTime()
    return ToponActConfig.STOP_TIME or 900
end

function ToponActModule:isHotUpdata()
    return self._isHoting
end

function ToponActModule:setHotUpdata(isHoting)
    self._isHoting = isHoting
end

function ToponActModule:showPhoneFeeLayer()
    self:reqActInfo(ToponActConfig.PhoneFee)
end

function ToponActModule:showLobbyWaitLayer(data)
    XH.viewManager:openView("ToponLobbyWaitView", nil, data)
end

function ToponActModule:showDailySignLayer()
    self:reqActInfo(ToponActConfig.DailySign)
end

function ToponActModule:showDailyLuckyDrawView()
    self:reqActInfo(ToponActConfig.LuckyDraw)
end

function ToponActModule:showGoldWinLostLayer(isWin)
    self._isWin = isWin
    self:reqActInfo(ToponActConfig.GoldWinLost)
end

function ToponActModule:setWin(isWin)
    self._isWin = isWin
end

function ToponActModule:showGoldPhoneFeeLayer()
    self:reqActInfo(ToponActConfig.GoldPhoneFee)
end

function ToponActModule:getToponActAward(webType, poolid, isCast)
    if device.platform ~= "windows" and isCast == nil then
        XH.TipTool.showLoading()
        if webType == ToponActConfig.GoldPhoneFee or webType == ToponActConfig.GoldWinLost then
            self._roomInfo = {}
            self._roomInfo.gameid = XH.userDefault:getValue(XH.userDefault.KEY_ID.LAST_JOIN_GOLD_GAME_ID, 0)
            self._roomInfo.level = XH.userDefault:getValue(XH.userDefault.KEY_ID.LAST_JOIN_GOLD_LEVEL, 0)
        else
            self._roomInfo = nil
        end
        self._webType = webType
        self._poolid = poolid
        self._isCast = isCast
        XH.sdkManager:addToponAds(XH.ThirdDefine.TOPONSDK_AD_TYPE.REWARDEVIDEO ,"show")
        self:setClickState()
        self:reqSetWaitCntAndLookAd(webType)
    else
        self:reqActAward(webType, poolid, isCast)
        if isCast == nil then
            self:setClickState()
        end
    end
end

-- 版本号相关判断(需要修改)
function ToponActModule:judgeVersion()
    if device.platform == "windows" then
        return false
    end
    local getVersion = XH.SysTool:GetBundleVersion()
    if device.platform == "android" and getVersion <= "1.0.1" then
        return true
    end
    -- 1.0.7版本插件打错，导致广告不能播放，需要引导
    if device.platform == "android" and getVersion == "1.0.7" then
        return true
    end
    if device.platform == "ios" and getVersion <= "1.0.43" then
        return true
    end
    return false
end

function ToponActModule:closeToponGoldDrawView()
    XH.sdkManager:addToponAds(XH.ThirdDefine.TOPONSDK_AD_TYPE.BANNER ,"close")
    self:dispatchEvent( { name = "KW_TOPONGOLDAREADRAW_VIEW_CLOSE", data = "" })
end

function ToponActModule:isShowToponBreak()
    return self._isShowBreakGold
end

function ToponActModule:getToponBreakCnt()
    return self._isShowBreakGold
end

----------------------------------------Req-----------------------------------------
-- 请求task 获取当前用户活动信息
function ToponActModule:reqToponActInfo()
    self._showIconList = {}
    self:startReq("ReqToponActInfo", TaskDefine.TASK_ID.TASK_TYPE_TOPONACT_GET_PLAYER_INFO, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, "")
end

function ToponActModule:reqActInfo(webType)
    if webType and self._toponActInfo then
        local webAid = 0
        for _, actInfo in ipairs(self._toponActInfo.toponAct) do
            if webType == actInfo.webType then
                webAid = actInfo.aid
            end
        end
        if webAid ~= 0 then
            local dataString = ";webAid = " .. webAid .. ";webType = " .. webType .. ";"
            self:startReq("ReqActInfo", TaskDefine.TASK_ID.TASK_TYPE_TOPONACT_GET_ACT_INFO, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
        elseif webType == ToponActConfig.DailySign then
            local dataString = ";webAid = " .. "nil" .. ";webType = " .. webType .. ";"
            self:startReq("ReqActInfo", TaskDefine.TASK_ID.TASK_TYPE_TOPONACT_GET_ACT_INFO, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
        end
    elseif webType == ToponActConfig.DailySign then
        local dataString = ";webAid = " .. "nil" .. ";webType = " .. webType .. ";"
        self:startReq("ReqActInfo", TaskDefine.TASK_ID.TASK_TYPE_TOPONACT_GET_ACT_INFO, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
    end
end

function ToponActModule:reqActAward(webType, poolid, isCost, awardType)
    if self:isSign50() then
        if webType == ToponActConfig.DailySign then
            if poolid then
                self:reqSign(ToponActConfig.SIGN_CONFIG.READ_AD)
                return
            end
            self:reqSign(awardType)
            return
        end
    end
    if webType and self._toponActInfo and poolid then
        local webAid = 0
        for _, actInfo in ipairs(self._toponActInfo.toponAct) do
            if webType == actInfo.webType then
                webAid = actInfo.aid
            end
        end
        if webAid ~= 0 then
            local dataString = ";webAid = " .. webAid .. ";webType = " .. webType .. ";poolid = " .. poolid .. ";isNew = true;"
            if isCost then
                dataString = dataString .. "isCost = " .. tostring(isCost) .. ";"
            end
            self:startReq("ReqActAward", TaskDefine.TASK_ID.TASK_TYPE_TOPONACT_GET_ACT_AWARD, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
        end
    elseif webType == ToponActConfig.DailySign then
        local dataString = ";webAid = " .. "nil" .. ";webType = " .. webType .. ";poolid = " .. "nil" .. ";"
        self:startReq("ReqActAward", TaskDefine.TASK_ID.TASK_TYPE_TOPONACT_GET_ACT_AWARD, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
    end
end

function ToponActModule:reqGoldActInfo(isWin)
    if self:isOpen() then
        self:setWin(isWin)
        self:startReq("ReqGoldActInfo", TaskDefine.TASK_ID.TASK_TYPE_TOPONACT_GET_GOLD_ACT, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, "")
    end
end

-- 是否符合条件请求
function ToponActModule:canReqToponBreakActInfo()
    if self:isOpen() then
        if self._toponActInfo then
            local webAid = 0
            for _, actInfo in ipairs(self._toponActInfo.toponAct) do
                if ToponActConfig.BreakGold == actInfo.webType then
                    webAid = actInfo.aid
                end
            end
            return webAid ~= 0
        end
    end
    return false
end

function ToponActModule:reqToponBreakActInfo()
    if self:isOpen() then
        self:reqActInfo(ToponActConfig.BreakGold)
    end
end

function ToponActModule:reqSetWaitCntAndLookAd(webType)
    if webType and self._toponActInfo then
        local webAid = 0
        for _, actInfo in ipairs(self._toponActInfo.toponAct) do
            if webType == actInfo.webType then
                webAid = actInfo.aid
            end
        end
        if webAid ~= 0 then
            local dataString = ";webAid = " .. webAid .. ";webType = " .. webType .. ";"
            self:startReq("ReqSetWaitCntAndLookAd", TaskDefine.TASK_ID.TASK_TYPE_TOPONACT_LOBBY_WITE_CNT, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
        end
    end
end

----------------------------------------Resp-----------------------------------------
function ToponActModule:resqToponActInfo(cell, ctype, data)
    if data == nil or data == "" or data.acData == "" then
        return
    end
    if ctype == XH.Req.TYPE.SUCCESS then
        local actData = cjson.decode(data.acData)
        if actData.error == 0 then
            self._toponActInfo = actData
            for _, actInfo in ipairs(self._toponActInfo.toponAct) do
                if ToponActConfig.ACT_TYPE[actInfo.webType] and actInfo.isShow then
                    self[ToponActConfig.ACT_TYPE[actInfo.webType]](self,actInfo)
                end
            end
            
            self:dispatchEvent( { name = ToponActModule.KW_TOPON_ACT_SHOW_ICON, data = self._showIconList })
        end
    end
end

function ToponActModule:resqActInfo(cell, ctype, data)
    if data == nil or data == "" or data.acData == "" then
        return
    end
    if ctype == XH.Req.TYPE.SUCCESS then
        local actData = cjson.decode(data.acData)
        if actData.error == 0 then
            if actData.webType == ToponActConfig.BreakGold then
                self:dispatchEvent( { name = ToponActModule.KW_TOPON_GOLD_BREAK_SHOW, data = actData })
            elseif actData.webType == ToponActConfig.DailySign then
                self:dispatchEvent( { name = ToponActModule.KW_TOPON_ACT_SHIN_INFO, data = actData })
            elseif actData.webType == ToponActConfig.PhoneFee then
                self:dispatchEvent( { name = ToponActModule.KW_TOPON_ACT_PHONE_FEE_INFO, data = actData })
            else
                for _, info in ipairs(self._toponActInfo.toponAct) do
                    if actData.webType == info.webType and ToponActConfig.SHOW_ACT_NAME[actData.webType] then
                        XH.viewManager:openView(ToponActConfig.SHOW_ACT_NAME[actData.webType], nil, actData, self._isWin)
                        self._isWin = nil
                        break
                    end
                end
            end
        elseif actData.error == 3 then
            if actData.webType == ToponActConfig.DailySign then
                self:dispatchEvent( { name = ToponActModule.KW_TOPON_ACT_SHIN_INFO, data = actData })
            end
        end
    end
end

function ToponActModule:resqActAward(cell, ctype, data)
    if data == nil or data == "" or data.acData == "" then
        return
    end
    if ctype == XH.Req.TYPE.SUCCESS then
        local actData = cjson.decode(data.acData)
        if actData.error == 0 then
            local callFun = function()
                if self._roomInfo then
                    local gameid = XH.userDefault:getValue(XH.userDefault.KEY_ID.LAST_JOIN_GOLD_GAME_ID, 0)
                    local level = XH.userDefault:getValue(XH.userDefault.KEY_ID.LAST_JOIN_GOLD_LEVEL, 0)
                    XH.lobby:getModule("Gold"):reqJoinGoldRoom(gameid, level)
                end
            end
            local CommonRewardView = require("lobby.Modules.CommonReward.RewardView")
            local bntType = CommonRewardView.KW_BTN_TYPE.CONTINUE
            if self._roomInfo ~= nil then
                XH.viewManager:openView("CommonRewardView", nil, actData.awardData, false, callFun, "", bntType)
            else
                if actData.webType == ToponActConfig.PhoneFee then
                    XH.viewManager:openView("CommonRewardView", nil, actData.awardData, false, nil, "福卡可以兑换钻石哦~")
                else
                    XH.viewManager:openView("CommonRewardView", nil, actData.awardData, false)
                end
            end
            self:dispatchEvent( { name = ToponActModule.KW_TOPON_ACT_FULSH_CNT, data = actData })
            self:fulshPlayerProp()
        elseif actData.error == 7 then
            XH.TipTool.showTip( {
                type = XH.TIP_LAYER_TYPE.OK,
            } , actData.msg)
        end
    end
end

function ToponActModule:resqGoldActInfo(cell, ctype, data)
    if data == nil or data == "" or data.acData == "" then
        return
    end
    if ctype == XH.Req.TYPE.SUCCESS then
        local actData = cjson.decode(data.acData)
        if actData.error == 0 then
            local popCnt = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_TOPON_GOLD_AREA_DRAW_POP, 0)
            if actData.showGoldBanner and (popCnt >= 0 or not (actData.showGoldWinLost or actData.showGoldPhoneFee) ) then
                if not cc.UserDefault:getInstance():getBoolForKey("isNeedHideAdvertisement1") then
                    XH.sdkManager:addToponAds(XH.ThirdDefine.TOPONSDK_AD_TYPE.BANNER ,"show")
                end
                self:reqSetWaitCntAndLookAd(ToponActConfig.GoldBanner)
            end

            if actData.showGoldWinLost then
                local goldActData = cjson.decode(actData.goldWinLostInfo)
                XH.viewManager:openView(ToponActConfig.SHOW_ACT_NAME[goldActData.webType], nil, goldActData, self._isWin)
                self._isWin = nil
            end

            if actData.showGoldPhoneFee then
                local goldActData = cjson.decode(actData.goldPhoneFeeInfo)
                XH.viewManager:openView(ToponActConfig.SHOW_ACT_NAME[goldActData.webType], nil, goldActData, self._isWin)
                self._isWin = nil
            end
        end
    end
end

function ToponActModule:onLoginSuccessCallBack(event)
    if not self:judgeVersion() then
        XH.sdkManager:clearToponList()
        XH.sdkManager:addToponAds(XH.ThirdDefine.TOPONSDK_AD_TYPE.REWARDEVIDEO)
        if self:isOpen() then
            self:reqToponActInfo()
        end
    end
end

function ToponActModule:onToponAdsShowCallBack(event)
    local code = event.data.code
    --local adType = event.data.adName
    if (code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_JLSUCCESS) then
        if self._webType and self._poolid then
            self:reqActAward(self._webType, self._poolid, self._isCast)
            self._webType = nil
            self._poolid = nil
            self._isCast = nil
            self:dispatchEvent( { name = ToponActModule.KW_TOPON_ACT_FULSH_LOBBY_WAIT, data = "" })
        elseif self._webType == ToponActConfig.PhoneFee then
            self:reqNewFeeActAward(1)
            self._webType = nil
            self._poolid = nil
            self._isCast = nil
            self:dispatchEvent( { name = ToponActModule.KW_TOPON_ACT_FULSH_LOBBY_WAIT, data = "" })
        end
        if device.platform == "ios" then
            self:setClickState()
        end
    elseif (code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_SHOWFAILED) then
            self._webType = nil
            self._poolid = nil
            self._isCast = nil
            self:dispatchEvent( { name = ToponActModule.KW_TOPON_ACT_FULSH_LOBBY_WAIT, data = "" })
    elseif (code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_CLOSE) then
        self:dispatchEvent( { name = ToponActModule.KW_TOPON_ACT_FULSH_LOBBY_WAIT, data = "" })
    end
end
---------------------------initActInfo-----------------------------
function ToponActModule:initDailySignInfo()
    -- if self._toponActInfo.sginCurrTime == 0 then
    --     XH.lobby:getModule("TabsActivity"):onShowSginRedPoint(true)
    -- else
    --     XH.lobby:getModule("TabsActivity"):onShowSginRedPoint(false)
    -- end
    XH.lobby:getModule("Notify"):reqNotifyInfoForActivity()
end

function ToponActModule:initLobbyWaitInfo(data)
    self:showLobbyWaitLayer(data)
end

function ToponActModule:initPhoneFeeInfo()
    local actData = { }
    actData.icon = LobbyConfig.ICON_ID.TOPON_PH_FEE
    if XH.ChannelTool.checkIsECarChannel() then
        actData.isVisibled = false
        XH.lobby:getModule("TabsActivity"):setShowPhoneFeeAct(false)
    elseif XH.playerData:isRechargePlayer() then
        actData.isVisibled = false
        XH.lobby:getModule("TabsActivity"):setShowPhoneFeeAct(false)
    else
        actData.isVisibled = true
        table.insert(self._showIconList, actData)
        XH.lobby:getModule("TabsActivity"):setShowPhoneFeeAct(true)
    end

end

function ToponActModule:initLuckyDrawInfo()
    local actData = { }
    actData.icon = LobbyConfig.ICON_ID.MEI_RI_CHOU_JIANG
    if device.platform == "android" and XH.SysTool:GetBundleVersion() <= "1.0.1" then
        actData.isVisibled = false
        XH.lobby:getModule("TabsActivity"):setShowFreeDrawAct(false)
    elseif XH.ChannelTool.checkIsECarChannel() then
        actData.isVisibled = false
        XH.lobby:getModule("TabsActivity"):setShowFreeDrawAct(false)
    elseif XH.playerData:isRechargePlayer() then
        actData.isVisibled = false
        XH.lobby:getModule("TabsActivity"):setShowFreeDrawAct(false)
    else
        actData.isVisibled = true
        table.insert(self._showIconList, actData)
        XH.lobby:getModule("TabsActivity"):setShowFreeDrawAct(true)
    end
end

function ToponActModule:initBreakGoldInfo()
    self._isShowBreakGold = true
end

function ToponActModule:initGoldBannerInfo()
    self._isShowGoldBanner = true
end

function ToponActModule:initGoldWinLostInfo()
    self._isShowGoldWinLost = true
end

function ToponActModule:initGoldPhoneFeeInfo()
    self._isShowGoldPhoneFee = true
end

function ToponActModule:showDailySignAward(awardData)
    local newData = { }
    for key, info in ipairs(awardData) do
        newData[key] = { }
        if info.url then
            newData[key].url, newData[key].desc = self:getLocalPic(info.propid, info.cnt)
        else
            newData[key].url, newData[key].desc = self:getLocalPic(info.type, info.cnt)
        end
    end
    if #newData > 0 then
        XH.viewManager:openView("CommonRewardView", nil, newData, true)
    end
end

-- 签到使用的是本地图片
function ToponActModule:getLocalPic(propid, cnt)
    local propList = XH.areaData:getPropList()
    local url = ""
    local desc = ""
    if propid == -1 or propid == 0 then
        if cnt >= 0 and cnt < 100 then
            url = "ico_gold_1.png"
        elseif cnt >= 100 and cnt < 1000 then
            url = "ico_gold_2.png"
        elseif cnt >= 1000 and cnt < 10000 then
            url = "ico_gold_3.png"
        else
            url = "ico_gold_4.png"
        end

        desc = XH.ConstString.getStr("ACTIVITY_GOAL_COUNT", tostring(cnt))
    elseif propid == propList.roomCardID then
        if cnt == 1 then
            url = "ico_card_1.png"
        elseif cnt == 2 then
            url = "ico_card_2.png"
        elseif cnt == 3 then
            url = "ico_card_3.png"
        else
            url = "ico_card_4.png"
        end
        desc = XH.ConstString.getStr("ACTIVITY_ROOM_CARD", tostring(cnt))
    elseif propid == propList.propDiamndID then
        if cnt <= 8 then
            url = "ico_diamond_1.png"
        elseif cnt > 8 and cnt <= 28 then
            url = "ico_diamond_2.png"
        elseif cnt > 28 and cnt <= 88 then
            url = "ico_diamond_3.png"
        else
            url = "ico_diamond_4.png"
        end
        desc = XH.ConstString.getStr("ACTIVITY_DIAMOND", tostring(cnt))
    end
    return url, desc
end

-------------------------------------------------------------------
-- 请求玩家金币
function ToponActModule:flushGoldCoin()
    local gameID = XH.areaData:getCommonGoldGameID()
    local srsGroupID = XH.areaData:getSrsGroupID()
    self:startReq("ReqPlayerGoldCoin", gameID, srsGroupID, 10)
end

function ToponActModule:onTranPlayerGoldCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data.purse then
            XH.playerData:setSR(data.purse)
        else
            XH.playerData:setSR(0)
        end
    end
end

function ToponActModule:fulshPlayerProp()
    if self._listener == nil then
        self._listener = cc.Director:getInstance():getScheduler():scheduleScriptFunc( function()
            XH.playerData:flushPlayerDrop()
            self:flushGoldCoin()
            self:removefulshPropSchedule()
        end , 2.5, false)
    end
end

function ToponActModule:removefulshPropSchedule()
    cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._listener)
    self._listener = nil
end

--------------------------------------------------------------------
--计算冷却时间
function ToponActModule:setClickState()
    self._refrehTimes = KW_TIME_EXPIRE
    self:dispatchEvent( { name = ToponActModule.EVENT_NEED_TIME, data = self._refrehTimes })
    if self._scheToShow == nil then
        self._scheToShow = cc.Director:getInstance():getScheduler():scheduleScriptFunc( function()
            self._refrehTimes = self._refrehTimes - 1
            if self._refrehTimes <= 0 then
                self._refrehTimes = 0
                self:removeScheToShowSchedule()
            end
            self:dispatchEvent( { name = ToponActModule.EVENT_NEED_TIME, data = self._refrehTimes })
        end , 1, false)
    end
end

function ToponActModule:onGetTimeLimits()
    return self._refrehTimes
end

function ToponActModule:removeScheToShowSchedule()
    cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._scheToShow)
    self._scheToShow = nil
end

function ToponActModule:onDestroy()
    self:removeScheToShowSchedule()
    self:removefulshPropSchedule()
    ToponActModule.super.onDestroy(self)
end

function ToponActModule:reqNewFeeActAward(awardType)
    local webid = ToponActConfig.NEW_FEE_WEB_AID[XH.areaData:getLobbyID()]
    self:startReq("ReqNewFeeActAward", TaskDefine.TASK_ID.TASK_TYPE_NEW_TOPON_FUKA_AWARD, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, ";webAid = " .. webid .. ";awardType = " .. awardType)
end

function ToponActModule:resqNewFeeActAward(cell, ctype, data)
    if data == nil or data == "" or data.acData == "" then
        return
    end
    if ctype == XH.Req.TYPE.SUCCESS then
        local actData = cjson.decode(data.acData)
        if actData then
            if actData.awardType == 1 or actData.awardType == 2 then
                self:dispatchEvent( { name = "KW_TOPON_ACT_NEW_FEE_GET_AWARD", data = actData })
            elseif actData.award then
                XH.viewManager:openView("CommonRewardView", nil, {actData.award})
                self:dispatchEvent( { name = "KW_TOPON_ACT_NEW_FEE_GET_AWARD", data = actData })
                self:fulshPlayerProp()
            end
        end
    end  
end

function ToponActModule:getTopOnActInfo()
    return self._toponActInfo
end

function ToponActModule:setDailySignWebType(v)
    self._dailySignWebType = v
end

function ToponActModule:getDailySignWebType()
    return self._dailySignWebType
end

function ToponActModule:lobbyRelinkSuccess(v)
    if self._dailySignWebType ~= nil then
        self:reqActAward(self._dailySignWebType)
        self._dailySignWebType = nil
    end
end


return ToponActModule �w  