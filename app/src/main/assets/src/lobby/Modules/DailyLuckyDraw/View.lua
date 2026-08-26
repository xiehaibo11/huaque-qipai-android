local DailyLuckyDrawView = class("DailyLuckyDrawView",XH.ViewBase)
local UrlConf = require("app.Config.UrlConf")
local TaskDefine = require("app.Define.TaskDefine")
local DailyLuckyDrawConfig = require("lobby.Modules.DailyLuckyDraw.Config")
local ToponActConfig = require("lobby.Modules.ToponAct.Config")

local DEFAULT_DOWNLOAD_IMAGE_DIR = device.writablePath .. "tmpImageCachDir/"

local _KW_PANEL_CONTENT_SIZE = "_KW_PANEL_CONTENT_SIZE"
local _KW_TEXT_PRIZE_VALUE = "_KW_TEXT_PRIZE_VALUE"
local _KW_TEXT_PRIZE_NAME = "_KW_TEXT_PRIZE_NAME"
local _KW_IMG_PRIZE = "_KW_IMG_PRIZE"

local PROPS_NUMS = 4
local PRIZE_PANELS_NUMS = 2

-- 延时时间
local ANI_DELAY_TIME = 1

local KW_ACT_RULE_ID = 20201207

function DailyLuckyDrawView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/DailyLuckyDraw/DailyLuckyDrawView.csb"
end

function DailyLuckyDrawView:getBindingInfo()
    return {
        ["_KW_BTN_NOCHANCE"] = { varName = "_btnNoTimes" },
        ["_KW_PRIZE_LISTVIEW"] = { varName = "_prizeListView" },
        ["_KW_BTN_TIMELIMITS"] = { varName = "_btnTimelimits" },
        ["_KW_TEXT_TIMELIMITS"] = { varName = "_kwTime" },
        ["_KW_PANEL_PRIZE_MODEL"] = { varName = "_prizeModel" },
        ["_KW_PANEL_LISTVIEW_1"] = { varName = "_panelPrizeView1" },
        ["_KW_PANEL_LISTVIEW_2"] = { varName = "_panelPrizeView2" },
        ["_KW_PANEL_LISTVIEW_TOHIDE"] = { varName = "_playAni" },
        ["_KW_PANEL_TOASTGX"] = { varName = "_panelToUpdate" },
        ["_KW_BTN_LJGX"] = { varName = "_btnLjgx", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnGoUpdate" },
        ["_KW_BTN_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnCloseBg" },
        ["_KW_BTN_YHCG"] = { varName = "_btnYhcg", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnCloseTips" },
        ["_KW_BTN_GETPRIZE"] = { varName = "_btnGetPrize", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnGetPrize" },
        ["_KW_BTN_CLOSE_GX"] = { varName = "_btnCloseGx", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnCloseTips" },
        ["_KW_BTN_HELP"] = { varName = "_btnHelp", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnHelp" },
        ["_KW_PANEL_HELP"] = { varName = "_panelHelp", onTouchEnded = "onBtnCloseHelp" },
        ["_KW_BTN_CLOSE_HELP"] = { varName = "_btnCloseHelp", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnCloseHelp" },
        ["_KW_PANEL_GAME_RULE"] = { varName = "_panelGameRule" },
        ["_KW_PANEL_LOADING"] = {varName="_panelLoading"},
        ["_KW_TEXT_GETPRIZE_TIMES"] = {varName="_textGetprizeTimes"},
        ["_KW_BTN_GETPRIZE_DIAMOND"] = {varName="_btnGetprizeDiamond", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnGetPrizeDiamond" },
        ["_KW_IMG_PRICE_LOGO"] = {varName="_imgPriseLogo"},
        ["_KW_TEXT_PRICE"] = {varName="_textPrice"},
    }
end

function DailyLuckyDrawView:ctor(webToponData)
	DailyLuckyDrawView.super.ctor(self)

    -- 初始化数据
    self:initData()
end

function DailyLuckyDrawView:initData()
    -- 依赖于panel开始滚动
    self._prizeListsData = {}
    self._moveTimes = 0
    self._needRefreshPosX = 0
    XH.lobby:getModule("DailyLuckyDraw"):reqActInfo(ToponActConfig.LuckyDraw)
end

-- 初始化监听事件
function DailyLuckyDrawView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("DailyLuckyDraw"), eventKeyName = "EVENT_GET_POOLINFO", callBack = "flushUI" },
        { module = XH.lobby:getModule("DailyLuckyDraw"), eventKeyName = "EVENT_CLOSE_MAINBG", callBack = "close" },
        { module = XH.lobby:getModule("DailyLuckyDraw"), eventKeyName = "EVENT_GET_PEO_INFO", callBack = "onGetPeoInfoSuccess" },
        { module = XH.lobby:getModule("DailyLuckyDraw"), eventKeyName = "EVENT_GET_PRIZE_INFO", callBack = "onGetPrizeInfoSuccess" },
        { module = XH.lobby:getModule("ToponAct"), eventKeyName = "EVENT_NEED_TIME", callBack = "onTimeRefreshSuccess" },
        { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_CLOSE_NEW_DAILY_LUCKY_DRAW", callBack = "close" },
        { module = XH.lobby:getModule("ToponAct"), eventKeyName = XH.lobby:getModule("ToponAct").KW_TOPON_ACT_FULSH_CNT, callBack = "flushCnt" },
        { module = XH.lobby:getModule("DailyLuckyDraw"), eventKeyName = "EVENT_GET_ACT_INFO", callBack = "onGetActInfo" },
    }
end

function DailyLuckyDrawView:onGetActInfo(event)
    if not event or not event.data then
        return
    end
    self._actInfo = event.data
    XH.lobby:getModule("DailyLuckyDraw"):onSetPlayerInfo(event.data.limitCnt, event.data.maxLimtCnt)
    self:reqPrize(event.data)
    self:onGetPeoInfoSuccess()
    self._textPrice:setString("-" .. event.data.actInfo.cost)
    self:initWebView()
end

-- 请求商品
function DailyLuckyDrawView:reqPrize(webToponData)
    XH.lobby:getModule("DailyLuckyDraw"):reqTaskConfig(webToponData)
end

-- 获取玩家个人信息回调
function DailyLuckyDrawView:onGetPeoInfoSuccess(isFulsh)
    local playerData = XH.lobby:getModule("DailyLuckyDraw"):onGetPlayerInfo()
    local boolCanGetPrize =(playerData.sendMax == playerData.sendTimes)
    if XH.lobby:getModule("ToponAct"):onGetTimeLimits() > 1 and isFulsh ~= true then
        self._btnTimelimits:setVisible(true)
        self._kwTime:setText("（" .. XH.lobby:getModule("DailyLuckyDraw"):onGetTimeLimits() .. "秒）")
    end
    self._textGetprizeTimes:setText("（".. playerData.sendMax - playerData.sendTimes .. "次）")
    self._btnNoTimes:setVisible(boolCanGetPrize)
    self._btnGetPrize:setVisible(not boolCanGetPrize)
end

-- 请求抽奖回调
function DailyLuckyDrawView:onGetPrizeInfoSuccess(msg)
    if msg.data then
        XH.viewManager:openView("DailyLuckDrawRewardView", nil, msg.data)
        self:onGetPeoInfoSuccess()
        self:flushInfoProps()
    end
end

-- 获取玩家身上数据
function DailyLuckyDrawView:flushInfoProps()
    XH.playerData:flushPlayerDrop()
    XH.playerData:flushGoldCoin()
end

-- 获取玩家身上数据
function DailyLuckyDrawView:reqGetTodayInfo()
    XH.lobby:getModule("DailyLuckyDraw"):reqTaskProtol(TaskDefine.TASK_ID.TASK_TYPE_ZHEJAING_DAILY_LUCKY_DROP, DailyLuckyDrawConfig.GetTaskConfig.getInfo, "getPeoInfoCallBack")
end

-- 设置界面(依赖于panel)
function DailyLuckyDrawView:flushUI(msg)
    if msg.data then
        local prizeLists = msg.data.prizes or { }
        self._prizeListsData = prizeLists
        for i = 1, #prizeLists do
            local prizeModel = self._prizeModel:clone()
            prizeModel:setPosition(cc.p(self._prizeModel:getContentSize().width * (i - 0.5), self._panelPrizeView1:getContentSize().height/2))
            XH.UITool.ignoreContentAdaptWithSize(prizeModel, _KW_IMG_PRIZE, true)
            XH.UITool.setText(prizeModel, _KW_TEXT_PRIZE_VALUE, prizeLists[i].prop_value)
            XH.UITool.setText(prizeModel, _KW_TEXT_PRIZE_NAME, prizeLists[i].prize_name)
            self:downloadImage(prizeModel, prizeLists[i].image_url)
            prizeModel:setVisible(true)
            local prizeModel2 = prizeModel:clone()
            self:downloadImage(prizeModel2, prizeLists[i].image_url)
            self._panelPrizeView1:addChild(prizeModel)
            self._panelPrizeView2:addChild(prizeModel2)
        end
        -- 开始滚动动画
        if #self._panelPrizeView1:getChildren() > PROPS_NUMS then
            self:startInitAni()
        end
    end
end

-- 动画初始化相关数据
function DailyLuckyDrawView:startInitAni()
    self:removeAniAction()
    self._needRefreshPosX = self._panelPrizeView1:getPositionX() + (#self._panelPrizeView1:getChildren() * self._prizeModel:getContentSize().width)
    self._panelPrizeView2:setPositionX(self._needRefreshPosX)
    self:startAni()
end

-- 开始进行动画
function DailyLuckyDrawView:startAni()
    local moveFunc = function()
        -- 进行相应移动
        for i = 1, PRIZE_PANELS_NUMS do
            if self._moveTimes == #self._panelPrizeView1:getChildren() * i then
                self["_panelPrizeView" .. i]:setPositionX(self._needRefreshPosX)
                if i == PRIZE_PANELS_NUMS then
                    self._moveTimes = 0
                end
            end
            local getPositionX = self["_panelPrizeView" .. i]:getPositionX() - self._prizeModel:getContentSize().width
            self["_panelPrizeView" .. i]:runAction(cc.MoveTo:create(1, cc.p(getPositionX, self["_panelPrizeView" .. i]:getPositionY())))
        end
        -- 移动次数增加
        self._moveTimes = self._moveTimes + 1
    end
    local delayAct = cc.DelayTime:create(ANI_DELAY_TIME)

    local updateCallAct = cc.CallFunc:create(moveFunc)
    local sequenceAct = cc.Sequence:create(updateCallAct, delayAct)
    local repeatAct = cc.RepeatForever:create(sequenceAct)
    self._playAni:runAction(repeatAct)
end

-- 停止所有有关动画
function DailyLuckyDrawView:removeAniAction()
    if self._playAni then
        self._playAni:stopAllActions()
    end
    if self._panelPrizeView1 then
        self._panelPrizeView1:stopAllActions()
    end
    if self._panelPrizeView2 then
        self._panelPrizeView2:stopAllActions()
    end
end

-- 设置MODEL界面
function DailyLuckyDrawView:downloadImage(node, url)
    local downloadImage = require("app.Req.DownloadImage")
    local reqDownloadImage = downloadImage:new()
    reqDownloadImage:addReqCallBack(self, function(imgNode, cell, type)
        if type == XH.Req.TYPE.SUCCESS then
            local imageName = XH.StringTool.getImageNameByUrl(url)
            local imagePath = DEFAULT_DOWNLOAD_IMAGE_DIR .. imageName
            XH.UITool.loadTexture(node, _KW_IMG_PRIZE, imagePath, ccui.TextureResType.localType)
            if self and self.setImageSize then
                self:setImageSize(node)
            end
        end
    end )
    reqDownloadImage:setUrl(url, false)
end

-- 设置IMAGE大小
function DailyLuckyDrawView:setImageSize(node)
    local getParSize = ccui.Helper:seekWidgetByName(node, _KW_PANEL_CONTENT_SIZE)
    local getChildSize = ccui.Helper:seekWidgetByName(node, _KW_IMG_PRIZE)
    local sizeX = getParSize:getContentSize().width / getChildSize:getContentSize().width
    local sizeY = getParSize:getContentSize().height / getChildSize:getContentSize().height
    if sizeX < sizeY then
        getChildSize:setScale(sizeX)
    else
        getChildSize:setScale(sizeY)
    end
    getParSize:setVisible(true)
end

-- 删除监听事件
function DailyLuckyDrawView:removeEvents()
    if self._timeToScroll then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._timeToScroll)
        self._timeToScroll = nil
    end
    self:removeAniAction()
end

-- 前往更新
function DailyLuckyDrawView:onBtnGoUpdate(send, eventType)
    if device.platform == "ios" then
        cc.Application:getInstance():openURL(UrlConf.DOWNLOAD_ADDRESS_IOS)
    else
        cc.Application:getInstance():openURL(UrlConf.DOWNLOAD_ADDRESS_ANDROID)
    end
end

-- 点击免费抽奖
function DailyLuckyDrawView:onBtnGetPrize(send, eventType)
    if not self._actInfo then
        return
    end
    local poolid = self._actInfo.actInfo.poolid[2] or self._actInfo.actInfo.poolid[1]
    XH.lobby:getModule("ToponAct"):getToponActAward(self._actInfo.webType, poolid)
end

-- 点击钻石抽奖
function DailyLuckyDrawView:onBtnGetPrizeDiamond(send, eventType)
    if not self._actInfo then
        return
    end
    XH.lobby:getModule("ToponAct"):getToponActAward(self._actInfo.webType, self._actInfo.actInfo.poolid[1], true)
end

-- 视频播放完成
function DailyLuckyDrawView:onToponAdsShowCallBack(event)
    local code = event.data.code
    if (code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_JLSUCCESS) then
        if device.platform == "ios" then
            XH.lobby:getModule("DailyLuckyDraw"):setClickState()
        end
        XH.lobby:getModule("DailyLuckyDraw"):reqTaskProtol(TaskDefine.TASK_ID.TASK_TYPE_ZHEJAING_DAILY_LUCKY_DROP, DailyLuckyDrawConfig.GetTaskConfig.getPrize, "getPrizeCallBack")
    end
end

-- 版本号相关判断(需要修改)
function DailyLuckyDrawView:judgeVersion()
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

-- 关闭主界面
function DailyLuckyDrawView:onBtnCloseBg(send, eventType)
    self:close()
end

-- 关闭更新tips弹窗
function DailyLuckyDrawView:onBtnCloseTips(send, eventType)
    self._panelToUpdate:setVisible(false)
end

-- 移除窗口
function DailyLuckyDrawView:close()
    self:removeEvents()
    DailyLuckyDrawView.super.close(self)
end

-- 刷新倒计时
function DailyLuckyDrawView:onTimeRefreshSuccess(event)
    if event then
        local time = event.data
        if time == 0 then
            self._btnTimelimits:setVisible(false)
            self._kwTime:setText("（"  .. time .. "秒）")
        else
            self._btnTimelimits:setVisible(true)
            self._kwTime:setText("（" .. time .. "秒）")
        end
    end
end

-- 打开帮助界面
function DailyLuckyDrawView:onBtnHelp(send, eventType)
    self._panelHelp:setVisible(true)
    if self._webView then 
        self._webView:setVisible(true)
    end
end

-- 关闭帮助界面
function DailyLuckyDrawView:onBtnCloseHelp(send, eventType)
    self._panelHelp:setVisible(false)
    if self._webView then 
        self._webView:setVisible(false)
    end
end

function DailyLuckyDrawView:initWebView()
    if device.platform == "windows" then
        return
    end
    local sz = self._panelGameRule
    if self._webView == nil then
        local webView = ccexp.WebView:create()
        webView:setContentSize(sz:getContentSize())
        webView:setScalesPageToFit(false)
        webView:setPosition(cc.p(sz:getContentSize().width/2, sz:getContentSize().height))
        webView:setAnchorPoint(sz:getAnchorPoint())
        webView:setBounces(false)
        webView:setVisible(false)
        sz:addChild(webView)
        self._webView = webView

        webView:setOnShouldStartLoading(function(sender,url)
            self._panelLoading:setVisible(true)
            self._webView:setVisible(false)
            return true
        end)

        webView:setOnDidFinishLoading(function(sender,url)
            self._panelLoading:setVisible(false)
        end)
        local url = string.format(UrlConf.GAME_RULE_HTML_ADDR .. "/%d/%d/%d.html", XH.SysTool:getChannelID(), XH.areaData:getAreaID(), KW_ACT_RULE_ID)
        if url then
            self._webView:loadURL(url)
        end
        self._webView:setVisible(false)
    end
end

function DailyLuckyDrawView:flushCnt(event)
    local data = event.data
    if data and data.webType == ToponActConfig.LuckyDraw then
        XH.lobby:getModule("DailyLuckyDraw"):onSetPlayerInfo(data.limitCnt,data.maxLimtCnt)
        self:onGetPeoInfoSuccess(true)
    end
end

return DailyLuckyDrawView  �<  