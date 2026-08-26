local PrayActView = class("PrayActView",XH.ViewBase)
local PrayActConfig = require("lobby.Modules.PrayAct.Config")
local KW_ACT_RULE_ID = 20210415
local _KW_TEXT_PMD_AWARD = "_KW_TEXT_PMD_AWARD"
local PrayActGoldConfig = {
    [1] = "及188金币",
    [2] = "及88金币",
    [3] = "及8金币",
    [4] = "及1金币",
}

local PrayActNameConfig = {
    [1] = "上上签",
    [2] = "中上签",
    [3] = "中中签",
    [4] = "中下签",
}

function PrayActView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/PrayActionLayer.csb"
end

function PrayActView:getBindingInfo()
    return {
         ["_KW_BTN_CLOSE"] = {varName="_btnClose",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventClose"},
         ["_KW_BTN_PRAY"] = {varName="_btnPray",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventPray"},
         ["_KW_BTN_PRAY_ANGIN"] = { varName = "KW_BTN_PRAY_ANGIN",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventPrayAngin"},
         ["_KW_BTN_PRAY_SURE"] = { varName = "KW_BTN_PRAY_SURE",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventPraySure"},
         ["_KW_BTN_PRAY_SURE_BUY"] = { varName = "KW_BTN_PRAY_SURE_BUY",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventPraySureBuy"},
         ["_KW_BTN_AWARD"] = { varName = "KW_BTN_AWARD",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventAwardC"},
         ["_KW_BTN_SM"] = { varName = "KW_BTN_SM",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventShuoM"},
         ["_KW_BTN_SM_CLOSE"] = { varName = "KW_BTN_SM_CLOSE",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventShuoMClose"},
         ["_KW_BTN_AWARD_PRAY"] = { varName = "KW_BTN_AWARD_PRAY",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventAwardC"},
         ["_KW_BTN_SM_PRAY"] = { varName = "KW_BTN_SM_PRAY",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventShuoM"},
         ["_KW_BTN_CLOSE_PRAY"] = { varName = "KW_BTN_CLOSE_PRAY",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventClose"},

         ["_KW_BTN_PRAY_SHARE"] = { varName = "KW_BTN_PRAY_SHARE",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventShare"},
         ["_KW_LAYOUT_ANI_POS"] = { varName = "_aniPos" },
         ["_KW_IMG_BG"] = { varName = "KW_IMG_BG" },
         ["_KW_IMG_PRAY_BG"] = { varName = "KW_IMG_PRAY_BG" },     
         ["_KW_TEXT_PRAY_NUM"] = { varName = "KW_TEXT_PRAY_NUM" },
         ["_KW_TEXT_PRAY_NAME"] = { varName = "KW_TEXT_PRAY_NAME" },
         ["_KW_TEXT_PRAY_NAME_S"] = { varName = "KW_TEXT_PRAY_NAME_S" },
         ["_KW_IMG_PRAY_DEC"] = { varName = "KW_IMG_PRAY_DEC" },
         ["_KW_TEXT_PRAY_DEC_"] = { beginIndex = 1, endIndex = 3, varName = "KW_TEXT_PRAY_DEC_"},
         ["_KW_IMG_LINE_"] = { beginIndex = 1, endIndex = 6, varName = "KW_IMG_LINE_"},
         ["_KW_IMG_PRAY_SM"] = { varName = "KW_IMG_PRAY_SM"},
         ["_KW_PANEL_GAME_RULE"] = { varName = "_panelGameRule"},
         ["_KW_TEXT_DEFAULT_TEXT"] = {varName="_pmdDefaultText"},
         ["_KW_NOTICE_PANEL_PMD"] = {varName="_noticePmd"},
         ["_KW_TEXT_PMD_NUM"] = {varName="_pmdText"},
         
         ["_KW_IMG_BG_ANGIN"] = {varName="KW_IMG_BG_ANGIN"},
         ["_KW_MSG_LABEL_FK"] = {varName="KW_MSG_LABEL_FK"},
         ["_KW_IMG_BG_ANGIN_CLOSE"] = { varName = "KW_IMG_BG_ANGIN_CLOSE",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventAnginClose"},
         ["_KW_IMG_BG_ANGIN_SURE"] = { varName = "KW_IMG_BG_ANGIN_SURE",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventAnginSure"},
         ["_KW_IMG_BG_ANGIN_Q"] = { varName = "KW_IMG_BG_ANGIN_Q",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventAnginClose"},
         ["_KW_PANEL_IGNORE_NEXT_TIME"] = { varName = "KW_PANEL_IGNORE_NEXT_TIME",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventAnginSelect"},
         ["_KW_IMG_IGNORE_STATE"] = {varName="_imgIgnoreState"},
         ["_KW_LAYOUT_ANI_TOUCH"] = {varName="KW_LAYOUT_ANI_TOUCH",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventPray"},
    }
end

function PrayActView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("PrayAct"), eventKeyName = "EVENT_INVITE_PRAYACT_DRAW", callBack = "onPrayActDraw"},
        { module = XH.lobby:getModule("PrayAct"), eventKeyName = "EVENT_INVITE_PRAYACT_DRAW_GOLD", callBack = "onPrayActDrawGold"},
    }
end

function PrayActView:ctor()
    PrayActView.super.ctor(self)
    self._isFirstEnter = false
    self:initLayerNode()
    self:showActivityPmd()
    self._viewStatTime = os.time()
    --数据统计
    local data = {}
    data.time1 = os.time()
    data.userid = XH.playerData:getNumberID()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.PrayActTouch, data)
end

function PrayActView:showActivityPmd()
    local needPlayerInfo = {}
    for _ = 1, 20 do
        local randIndex = math.random(1, #PrayActGoldConfig)
            local numid = "恭喜玩家".. math.random(1,9)..math.random(0,9).."xxxx"..math.random(0,9)..math.random(0,9).."在祈福游戏中获得"
            local prize_name = PrayActNameConfig[randIndex]..PrayActGoldConfig[randIndex]
            local tmp = {}
            tmp.numid = numid
            tmp.prize_name = prize_name
            table.insert(needPlayerInfo, tmp)
    end
    self:runPmdAction(needPlayerInfo)
end

function PrayActView:runPmdAction(data)
    if self._isFirstEnter then return end
    self._isFirstEnter = true
    local posStart = 970
    local posEnd = -1500
    local index = 1
    local textNode = self._pmdText
    self._noticePmd:stopAllActions()
    local cb = function()
        if index >= #data then
            index = 1
        end
        if textNode == nil or tolua.isnull(textNode) then
            return
        end
        local cloneText = textNode:clone()
        cloneText:setString(data[index].numid)
        XH.UITool.setText(cloneText, _KW_TEXT_PMD_AWARD, data[index].prize_name)
        cloneText:setVisible(true)
        cloneText:setPositionX(posStart)
        self._noticePmd:addChild(cloneText)
        index = index + 1
        local endPos = cc.p(posEnd,cloneText:getPositionY())
        cloneText:runAction(cc.Sequence:create(cc.MoveTo:create(20, endPos), cc.CallFunc:create(function ()
            cloneText:removeFromParentAndCleanup()
        end)))
    end
    local seq = cc.Sequence:create(cc.CallFunc:create(cb), cc.DelayTime:create(13))
    self._noticePmd:runAction(cc.RepeatForever:create(seq))
end

function PrayActView:initWebView()
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
        self._webView:setVisible(true)
    end
end

function PrayActView:loadWebViewUrl()
    local UrlConf = require("app.Config.UrlConf")
    local url = string.format(UrlConf.GAME_RULE_HTML_ADDR .. "/%d/%d/%d.html", XH.SysTool:getChannelID(), XH.areaData:getAreaID(), KW_ACT_RULE_ID)
    if url then
        if self._webView then
            self._webView:loadURL(url)
        end
    end
end

function PrayActView:showAni(show)
    local aniNode = self._aniPos
    if aniNode and tolua.isnull(aniNode) == false then
        aniNode:removeAllChildren()
        local params1
        if show then
            params1 = {
                path = "animation/Lobby/Base/pray_act/",
                ske = "ios_chouqian_1_ske.json",
                tex = "ios_chouqian_1_tex.json",
                armatureName = "Armature",
                dragonBonesName = "ios_chouqian_1",
                animationName = "chouqian" 
            }
        else
            params1 = {
                path = "animation/Lobby/Base/pray_act/",
                ske = "ios_chouqian_1_ske.json",
                tex = "ios_chouqian_1_tex.json",
                armatureName = "Armature",
                dragonBonesName = "ios_chouqian_1",
                animationName = "chouqian_stop" 
            }
        end
        local propAni = display.playDargonBonesAnimByTimes(params1, 0)
        if propAni then
            aniNode:addChild(propAni)
        end
    end
end

function PrayActView:initLayerNode()
    local info = XH.lobby:getModule("PrayAct")._PrayActInfo
    if info then
        if info.giftbox == 0 then
           self:showAni(false)
        else
           self:showPrayActstate(info)
        end
    end
end

function PrayActView:onPrayActDraw(event)
    if not event and event.data then
        return
    end
    self:performWithDelay(function()
        self:showPrayActstate(event.data)
        XH.audioManager:stopAllSFX()
    end, 2)
end

function PrayActView:onPrayActDrawGold(event)
    if not event and event.data then
        return
    end
    self.KW_BTN_PRAY_SURE:setVisible(false)
    self.KW_BTN_PRAY_ANGIN:setVisible(false)
    self.KW_BTN_PRAY_SURE_BUY:setVisible(false)
    self.KW_BTN_PRAY_SHARE:setVisible(true)
    XH.viewManager:openView("LuckyTurnTableActAwardView", nil, event.data)
end

function PrayActView:showPrayActstate(info)
    if not info or not info.selectkind or not info.descriptions then 
        return 
    end
    if not PrayActConfig[info.selectkind] or not PrayActConfig[info.selectkind][info.descriptions] then
        return
    end
    self.KW_IMG_BG:setVisible(false)
    self.KW_IMG_PRAY_BG:setVisible(true)
    --info.selectkind = 4
    --info.descriptions = 8
    local dec = PrayActConfig[info.selectkind][info.descriptions]
    self.KW_TEXT_PRAY_NUM:setString(dec.name)
    self.KW_TEXT_PRAY_NAME:setString(dec.name_dec)
    if info.selectkind == 1 and info.descriptions == 8 then
        self.KW_TEXT_PRAY_NAME_S:setString("艮卦")
        self.KW_TEXT_PRAY_NAME:setString("")
    elseif info.selectkind == 4 and info.descriptions == 8 then
        self.KW_TEXT_PRAY_NAME_S:setString("夬卦")
        self.KW_TEXT_PRAY_NAME:setString("")
    else
        self.KW_TEXT_PRAY_NAME_S:setString("")
    end
    self.KW_IMG_PRAY_DEC:loadTexture(string.format("prayact_%d.png", info.selectkind),ccui.TextureResType.plistType)
    if dec.dec1 then
        self.KW_TEXT_PRAY_DEC_1:setVisible(true)
        self.KW_TEXT_PRAY_DEC_1:setString(dec.dec1)
    end
    if dec.dec2 then
        self.KW_TEXT_PRAY_DEC_2:setVisible(true)
        self.KW_TEXT_PRAY_DEC_2:setString(dec.dec2)
    end
    if dec.dec3 then
        self.KW_TEXT_PRAY_DEC_3:setVisible(true)
        self.KW_TEXT_PRAY_DEC_3:setString(dec.dec3)
    end
    for i = 1,#dec.num do
        self["KW_IMG_LINE_"..i]:loadTexture(string.format("prayact_point_%d.png", dec.num[i]+1),ccui.TextureResType.plistType)
    end 

    if info.giftbox == 2 then 
        self.KW_BTN_PRAY_SURE:setVisible(false)
        self.KW_BTN_PRAY_ANGIN:setVisible(false)
        self.KW_BTN_PRAY_SURE_BUY:setVisible(true)
    end
    if info.giftbox == 3 then
        self.KW_BTN_PRAY_SURE:setVisible(false)
        self.KW_BTN_PRAY_ANGIN:setVisible(false)
        self.KW_BTN_PRAY_SURE_BUY:setVisible(false)
        self.KW_BTN_PRAY_SHARE:setVisible(true)
    end
end

function PrayActView:onTouchEventClose(send,eventType)
    --数据统计
    local data = {}
    data.time1 = self._viewStartTime or 0
    data.time2 = os.time() or 0
    data.staytime = data.time2 - data.time1
    data.userid = XH.playerData:getNumberID()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.PrayActJoin, data)
    self:close()
end

function PrayActView:onTouchEventPray(send,eventType)
    self:showAni(true)
    XH.audioManager:play("PRAY_ACT_OPEN")
    self._btnPray:setEnabled(false)
    XH.lobby:getModule("PrayAct"):reqPrayActDraw()
    --数据统计
    local data = {}
    data.time1 = os.time()
    data.userid = XH.playerData:getNumberID()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.PrayActYao, data)
end

function PrayActView:onTouchEventPrayAngin(send,eventType)
    if XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_PRAY_ANGIN_GET_TIPS) then
        if XH.playerData:getRemodelRoomCard() >= XH.lobby:getModule("PrayAct")._PrayActInfo.fangka then
            self.KW_IMG_BG_ANGIN:setVisible(false)
            self.KW_IMG_BG:setVisible(true)
            self.KW_IMG_PRAY_BG:setVisible(false)
            self:showAni(false)
            self._btnPray:setVisible(false)
            self:showAni(true)
            XH.lobby:getModule("PrayAct"):reqPrayActDraw()
            XH.audioManager:play("PRAY_ACT_OPEN")
            --数据统计
            local data = {}
            data.time1 = os.time()
            data.userid = XH.playerData:getNumberID()
            XH.throwDataManager:throwData(XH.ThrowDataDefine.PrayActYaoAngin, data)
        else
             XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK,
            },"您的房卡不足！")
        end
    else
        self.KW_IMG_BG_ANGIN:setVisible(true)
        local info = XH.lobby:getModule("PrayAct")._PrayActInfo
        self.KW_MSG_LABEL_FK:setString(info.fangka.."张房卡再次抽签?")

        --数据统计
        local data = {}
        data.time1 = os.time()
        data.userid = XH.playerData:getNumberID()
        XH.throwDataManager:throwData(XH.ThrowDataDefine.PrayActNotips, data)
    end
end

function PrayActView:onTouchEventAnginClose(send,eventType)
    self.KW_IMG_BG_ANGIN:setVisible(false)
end

function PrayActView:onTouchEventAnginSelect(send,eventType)
    self._imgIgnoreState:setVisible(not self._imgIgnoreState:isVisible())
end

function PrayActView:onTouchEventAnginSure(send,eventType)
    if XH.playerData:getRemodelRoomCard() >= XH.lobby:getModule("PrayAct")._PrayActInfo.fangka then
        self.KW_IMG_BG_ANGIN:setVisible(false)
        self.KW_IMG_BG:setVisible(true)
        self.KW_IMG_PRAY_BG:setVisible(false)
        self:showAni(false)
        self._btnPray:setVisible(false)
        self:showAni(true)
        XH.lobby:getModule("PrayAct"):reqPrayActDraw()
        XH.audioManager:play("PRAY_ACT_OPEN")
        --数据统计
        local data = {}
        data.time1 = os.time()
        data.userid = XH.playerData:getNumberID()
        XH.throwDataManager:throwData(XH.ThrowDataDefine.PrayActYaoAngin, data)
        if self._imgIgnoreState:isVisible() then
           XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PRAY_ANGIN_GET_TIPS, true)
        end
    else
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        },"您的房卡不足！")
    end
end

function PrayActView:onTouchEventPraySure(send,eventType)
    self.KW_BTN_PRAY_SURE:setEnabled(false)
    XH.lobby:getModule("PrayAct"):reqPrayActDrawGold()
end

function PrayActView:onTouchEventPraySureBuy(send,eventType)
    self.KW_BTN_PRAY_SURE_BUY:setEnabled(false)
    XH.lobby:getModule("PrayAct"):reqPrayActDrawGold()
end

function PrayActView:onTouchEventAwardC(send,eventType)
    XH.viewManager:openView("AwardCenterView")
    --self:close()
end

function PrayActView:onTouchEventShuoM(send,eventType)
    self.KW_IMG_PRAY_SM:setVisible(true)
    self:initWebView()
    self:loadWebViewUrl()
    if self._webView then
        self._webView:setVisible(true)
    end
end

function PrayActView:onTouchEventShuoMClose(send,eventType)
    self.KW_IMG_PRAY_SM:setVisible(false)
    if self._webView then
        self._webView:setVisible(false)
    end
end

function PrayActView:onTouchEventShare(send,eventType)
    if not XH.sdkManager:isInstallWeChat() then
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, "您还未安装微信")
        return
    end
    self:captureScreenAndShareToWeChat()
end

function PrayActView:captureScreenAndShareToWeChat(savePicName)
    local year = os.date("%Y")
    local mouth = os.date("%m")
    local day = os.date("%d")
    local hour = os.date("%H")
    local min = os.date("%M")
    local second = os.date("%S")
    local strTime = year .. mouth .. day .. hour .. min .. second

    savePicName = savePicName or strTime ..".jpg"
    local shareToWeChat = function(succeed, outputFile)
        if not succeed or not cc.FileUtils:getInstance():isFileExist(outputFile) then
            return
        end
        local shareInfo = {
            textTitle = "", --分享标题
            imagePath = outputFile, --微信不用
            type = "pic", --word是文字分享，url网页模式分享，pic分享图片
            scene = 0, --0是分享到好友会话，1是分享到朋友圈
            imageUrl = "", --分享图片地址（分享网页模式，需要传安卓包名）
            text = "", --分享内容 
            sharePath = "",
        }
        XH.sdkManager:startShare(shareInfo)
        self:close()
    end
    self:captureScreenshot(shareToWeChat, savePicName)
end

function PrayActView:captureScreenshot(callback, fileName)
    local fileFullPath = cc.FileUtils:getInstance():getWritablePath() .. "tmp/" .. fileName
    local directoryFullPath = cc.FileUtils:getInstance():getWritablePath() .. "tmp"
    if cc.FileUtils:getInstance():isDirectoryExist(directoryFullPath) then
        cc.FileUtils:getInstance():removeDirectory(directoryFullPath)
        cc.FileUtils:getInstance():createDirectory(directoryFullPath)
    else
        cc.FileUtils:getInstance():createDirectory(directoryFullPath)
    end

    local scale = 0.5
    local screenSize = cc.Director:getInstance():getWinSize()
    local render = cc.RenderTexture:create(screenSize.width * scale, screenSize.height * scale, _G.kCCTexture2DPixelFormat_RGBA8888, gl.DEPTH24_STENCIL8_OES)
    render:setKeepMatrix(true)
    render:begin()
    cc.Director:getInstance():getRunningScene():visit()
    render:endToLua()
    render:saveToFile("tmp/" .. fileName, cc.IMAGE_FORMAT_JPEG, false)
    local retry = 6
    local callbackHanlder
    callbackHanlder = function()
        local exist = cc.FileUtils:getInstance():isFileExist(fileFullPath)
        if exist then
            callback(true, fileFullPath)
        elseif retry >= 0 then
            self:performWithDelay(callbackHanlder, 0.5)
            retry = retry - 1
        else
            callback(false, nil)
        end
    end
    self:performWithDelay(callbackHanlder,0.5)
end


return PrayActView   �I  