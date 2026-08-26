-- 大部分逻辑沿用原先PopWindowsView，保证风信后台配置不用大范围修改
local PopSystemView = class("PopSystemView", XH.ViewBase)
local ThinkingDataMap = require("app.Third.ThinkData.Define")
local SxVipConfig = require('lobby.Modules.Sxvip.Config')

local DEFAULT_DOWNLOAD_IMAGE_DIR = device.writablePath .. "tmpImageCachDir/"
local KW_BG_SIZE = {
    width = 1920,
    height = 1080,
}

function PopSystemView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/ActivityPopLayer.csb"
end

function PopSystemView:getBindingInfo()
    return {
        ["_KW_PANEL_BG"] = {varName="_panelBG",onTouch = "onTouchEventBGClose"},
    }
end

local H5GameNameList = {
    LEGEND = "LEGEND",
    BUYU = "BUYU",
    KNIFELEGEND = "KNIFELEGEND",
}

local POPVIEWKEYWORD = {
    RENRENBUYU = "捕鱼"
}

function PopSystemView:ctor(param)
    if not param then
        return
    end
	PopSystemView.super.ctor(self, param)
    self._webViewConf = param
    local tmpData = {}
    tmpData.userid = XH.playerData:getNumberID()
    tmpData.popname = self._webViewConf.name
    XH.throwDataManager:throwData(XH.ThrowDataDefine.POPSYSTEM_OPEN_VIEW, tmpData) 
    self:recordThinkingData()
    self:initUI()
    self:throwDataShow()
end

function PopSystemView:recordThinkingData(event)
    local name = "曝光"
    if event ~= nil then
        name = "点击"
    end
    local situations = self._webViewConf.fromSceneTypeStr
    if XH.teaHouseManager ~= nil and XH.teaHouseManager:isInTeaHouse() then
        situations = "亲友圈大厅"
    end
    local buttonStr = ""
    if event ~= nil then
        buttonStr = event.linkStr or "close"
    end
    XH.TDSDK:track(ThinkingDataMap.MODAL_WINDOW,
    {
        action = name,
        button_parameters = buttonStr,
        picture_url = self._webViewConf.imageUrl,
        popup_creat_time = "",
        popup_id = self._webViewConf.id,
        popup_name = self._webViewConf.name,
        popup_situations = situations,
    }
    )
end

function PopSystemView:throwDataShow()
    if not self._webViewConf then
        return
    end
    local isRRBY = false
    if self._webViewConf.name and string.find(self._webViewConf.name, "人人捕鱼") then
        isRRBY = true
    end
    local allBtnsConf = self._webViewConf.btns or {}
    for _, btnConf in pairs(allBtnsConf) do
        if btnConf.link and string.find(btnConf.link, "RENRENBUYU") then
            isRRBY = true
        end
    end
    if isRRBY then
        XH.lobby:getModule("Replugin"):postToSepperllitaExposure(2)
    end
end

function PopSystemView:createButtonImg(conf)
    local newBtnImg = XH.RemoteImage.new()
    newBtnImg:setTouchEnabled(true)
    newBtnImg:ignoreContentAdaptWithSize(false)
    newBtnImg:setUrl(conf.imageUrl)
    newBtnImg:setName(conf.link) --兼容原有逻辑
    return XH.UIButton.create(newBtnImg)
end

function PopSystemView:initBg()
    self._bgImg = XH.RemoteImage.new()
    self._bgImg:setTouchEnabled(true) --防止点击图片后直接关闭
    self._bgImg:setCallBack(function ()
        self:initBtns()
        if self._webViewConf.isFull then
            local bgSize = self._panelBG:getContentSize()
            local imgSize = self._bgImg:getContentSize()
            self._panelBG:setScaleX(bgSize.width/imgSize.width)
            self._panelBG:setScaleY(bgSize.height/imgSize.height)
        end
    end)
    self._bgImg:setUrl(self._webViewConf.imageUrl)
    local size = self._panelBG:getContentSize()
    self._bgImg:setPosition(cc.p(size.width/2, size.height/2))
    self._bgImg:setAnchorPoint(cc.p(0.5, 0.5))
    self._panelBG:addChild(self._bgImg)
end

function PopSystemView:initBtns()
    local allBtnsConf = self._webViewConf.btns or {}
    for _, btnConf in pairs(allBtnsConf) do
        local newBtnImg = self:createButtonImg(btnConf)
        self:goActBtnTotalExt(newBtnImg, btnConf)
        local size = self._bgImg:getContentSize()
        local positionX = size.width / 2
        local positionY = size.height / 2
        if btnConf.posX then
            positionX = btnConf.posX * size.width / 100
        end
        if btnConf.posY then
            positionY =(100 - btnConf.posY) * size.height / 100
        end
        newBtnImg:setPosition(positionX, positionY)
        self._bgImg:addChild(newBtnImg)
    end
end

function PopSystemView:initUI()
    self:initBg()
    self:updateBgScale()
end

-- 修改背景尺寸适配
function PopSystemView:updateBgScale()
    if not self._bgImg then
        return
    end
    local bgSize = self._bgImg:getContentSize()
    local finalScale = 1
    if bgSize.height > KW_BG_SIZE.height or bgSize.width > KW_BG_SIZE.width then
        local setScaleWidth = KW_BG_SIZE.width / bgSize.width
        local setScaleHeight = KW_BG_SIZE.height / bgSize.height
        finalScale = setScaleWidth > setScaleHeight and setScaleHeight or setScaleWidth
    end
    self._bgImg:setScale(finalScale)
end

-- 获取文件大小
function PopSystemView:getFileSize(fileName)
    local fileSize = -1
    local gameListJson = cc.FileUtils:getInstance():getStringFromFile(fileName)
    if gameListJson then 
        fileSize = string.len(gameListJson)
    end
    return fileSize
end


function PopSystemView:onTouchEventBGClose(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:recordThinkingData(send)
    if self._isLZDDZ then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_GOLD_THROW_21, nil, {area_id = XH.areaData:getAreaID()})
    end
    if self._isSHUFFLE then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.SHUFFLE_LAYER_CLOSE)
    end
    self:close()
end

function PopSystemView:onTouchEventNewGold(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:recordThinkingData(send)
    XH.lobby:getModule("Lobby"):enterNewGold()
    if self._isLZDDZ then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_GOLD_THROW_19, nil, {area_id = XH.areaData:getAreaID()})
    end
    self:close()
end

function PopSystemView:throwClickData(goActBtn)
    local webConf = self._webViewConf
    if goActBtn and webConf and webConf.fromSceneTypeStr then
        local conventionData = {}
        conventionData.page_label = webConf.fromSceneTypeStr
        conventionData.block_item_id = webConf.id or 0
        conventionData.block_label = webConf.name or ""
        conventionData.label = goActBtn.linkStr or ""
        local extraData = {}
        extraData.pic_url = webConf.imageUrl or ""
        extraData.area_id = XH.areaData:getAreaID()
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.POP_VIEW_CLICK,conventionData,extraData)
    end
end

-- 点击事件的拓展汇总
function PopSystemView:goActBtnTotalExt(goActBtn, value)
    goActBtn.linkStr = value.link
    if value.link == "BUYU" or value.link == "LEGEND" or value.link == "KNIFELEGEND" 
    or value.link == "DATIANSHIZHIJIAN" or value.link == "LINGMENGZHIBO" or value.link == "REXUEFENGSHEN" 
    or value.link == "DOULUODALU" or value.link == "JIEJISANGUO" or value.link == "SHEISHISHOUFU"
    or value.link == "WEIJINGCHUANQI" or value.link == "ZHUOYUECHUANSHUO" or value.link == "YUANZHENGJIANGSHI"
    or value.link == "RONGYAOZHINU" or value.link == "BAIZHANSHACHENG" or value.link == "HUANXIANGMINGJIANGLU" then
        goActBtn:setClickCallBack(handler(self, self.onTouchEventH5Games))
    elseif string.find(value.link, "RENRENBUYU") then
        goActBtn:setClickCallBack(handler(self, self.onTouchEventUnityGames))
    elseif value.link == "close" then
        goActBtn:setClickCallBack(handler(self, self.onTouchEventBGClose))
    elseif value.link == "LZDDZ" then
        self._isLZDDZ = true
        goActBtn:setClickCallBack(handler(self, self.onTouchEventNewGold))
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_GOLD_THROW_20, nil, {area_id = XH.areaData:getAreaID()})
    elseif value.link == "gameover" then
        goActBtn:setClickCallBack(handler(self, self.onTouchEventGameOver))
    elseif string.find(value.link, "share") then
        goActBtn:setClickCallBack(handler(self, self.onTouchEventShare))
    elseif string.find(value.link, "golinkbyyh") then
        goActBtn:setClickCallBack(handler(self, self.onTouchEventGoLink))
    elseif string.find(value.link, "copy") then
        goActBtn:setClickCallBack(handler(self, self.onTouchEventCopy))
    elseif string.find(value.link, "showLayer") then
        goActBtn:setClickCallBack(handler(self, self.onTouchEventGoShowPop))
    elseif string.find(value.link, "timeclose") then
        self:addTimeCloseNew(goActBtn,value)
    elseif string.find(value.link, "goToTeaHouseList") then
        goActBtn:setClickCallBack(handler(self, self.onTouchEventGoToTeaHouseList))
    elseif string.find(value.link, "enterTeaHouse") then
        goActBtn:setClickCallBack(handler(self, self.onTouchEventEnterTeaHouse))
    elseif string.find(value.link, "CreateBoxRoomView") then
        local verList = string.split(value.link,",")
        if #verList > 1 then
            goActBtn._gameid = tonumber(verList[2])
        end
        if #verList > 2 then
            goActBtn._chooseTwoPlayer = verList[3] == "true"
        end
        goActBtn:setClickCallBack(handler(self, self.onTouchEventGoCreateBoxRoomView))
    elseif value.link == "SHUFFLE" then
        self._isSHUFFLE = true
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.SHUFFLE_SHOW_LAYER)
        goActBtn:setClickCallBack(handler(self, self.onTouchEventShopShuffle))
    elseif value.link == "openSxvip" then
        goActBtn:setClickCallBack(handler(self, self.onTouchEventOpenSxvipView))
    end
end

-- 跳转链接点击事件
function PopSystemView:onTouchEventGoLink(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:recordThinkingData(send)
    local openUrlContext = string.split(send:getName(),"-")
    if openUrlContext and openUrlContext[2] then
        cc.Application:getInstance():openURL(openUrlContext[2])
    end
    self:addThrowBtnData(send)
end

-- 复制文本点击事件
function PopSystemView:onTouchEventCopy(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:recordThinkingData(send)
    local getCopyText = string.split(send:getName(),"-")
    if getCopyText and getCopyText[2] then
        XH.SysTool.copyString(getCopyText[2])
        XH.TipTool.showToast("复制成功!", 1)
    end
    self:addThrowBtnData(send)
end

-- 分享点击事件
function PopSystemView:onTouchEventShare(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:recordThinkingData(send)
    local getShareMsg = string.split(send:getName(), "-")
    local UrlConf = require("app.Config.UrlConf")
    local url = UrlConf.DOWNLOAD_ADDRESS_IOS

    if device.platform == "android" then
        url = UrlConf.DOWNLOAD_ADDRESS_ANDROID
    end

    local shareType = getShareMsg[2] or "url"
    local getWebText = getShareMsg[3] or "年度精品推荐，百万浙江玩家下载！"
    local getWebLinkUrl = getShareMsg[4] or url

    if shareType == "url" then
        self:shareByUrl(getWebText, getWebLinkUrl)
    elseif shareType == "word" then
        self:shareByWord(getWebText, getWebLinkUrl)
    elseif shareType == "pic" then
        self:shareByPic(getWebText, getWebLinkUrl)
    elseif shareType == "lobby" then
        XH.lobby:getModule("Lobby"):onTouchShare()
    end
    self:addThrowBtnData(send)
    self:throwClickData(send)
end

-- 三种分享类型url
function PopSystemView:shareByUrl(getWebText, getWebLinkUrl)
    XH.lobby:getModule("PopSystem"):shareByUrlMod(getWebText, getWebLinkUrl)
end

-- 三种分享类型word
function PopSystemView:shareByWord(getWebText, getWebLinkUrl)
    XH.lobby:getModule("PopSystem"):shareByWordMod(getWebText, getWebLinkUrl)
end

-- 三种分享类型pic
function PopSystemView:shareByPic(getWebText, getWebLinkUrl)
    local imageName = XH.StringTool.getImageNameByUrl(self._webViewConf.shareImg)
    local imagePath = DEFAULT_DOWNLOAD_IMAGE_DIR .. imageName
    if not cc.FileUtils:getInstance():isFileExist(imagePath) then
        return
    end
    XH.lobby:getModule("PopSystem"):shareByImageMod(getWebText, getWebLinkUrl, imagePath)
end

-- 额外按钮拓展点击事件
function PopSystemView:onTouchEventGoShowPop(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:recordThinkingData(send)
    local getShowContents = string.split(send:getName(), "-")
    if getShowContents[3] == "" then
        getShowContents[3] = nil
    end
    if getShowContents[4] == "" then
        getShowContents[4] = nil
    end
    if getShowContents and getShowContents[2] then
        if getShowContents[2] == "TabsActivityView" and getShowContents[3] then
            self:judgeTabsActClick(getShowContents[3])
            self:close()
            return
        end
        if getShowContents[3] and type(getShowContents[3] == "string") then
            local getTableData = loadstring("return {" .. getShowContents[3] .."}")()
            XH.viewManager:openView(getShowContents[2], nil, getTableData)
        else
            XH.viewManager:openView(getShowContents[2])
        end
    end
    self:addThrowBtnData(send)
    self:close()
end

function PopSystemView:judgeTabsActClick(goActState)
    if goActState then
        if goActState == "goSign" then
            self:goSign()
            return
        end
        if goActState == "goPhoneBind" then
            self:goPhoneBind()
            return
        end
        if goActState == "goSystemTask" then
            self:goSystemTask()
            return
        end
    end
    XH.viewManager:openView("TabsActivityView")
end

function PopSystemView:goSign()
    XH.userDefault:setValue(XH.userDefault.KEY_ID.USERDEFAULT_ACTIVITY_TYPE, XH.lobby:getModule("TabsActivity").ActivityType.ActivitySignIn)
    XH.viewManager:openView("TabsActivityView", nil, { tab = "DAILYSIGN"})
end

function PopSystemView:goPhoneBind()
    XH.userDefault:setValue(XH.userDefault.KEY_ID.USERDEFAULT_ACTIVITY_TYPE, XH.lobby:getModule("TabsActivity").ActivityType.ActivityPhoneBind)
    XH.viewManager:openView("TabsActivityView", nil, { tab = "PHONEBIND"})
end

function PopSystemView:goSystemTask()
    XH.userDefault:setValue(XH.userDefault.KEY_ID.USERDEFAULT_ACTIVITY_TYPE, XH.lobby:getModule("TabsActivity").ActivityType.ActivityTask)
    XH.viewManager:openView("TabsActivityView", nil, { tab = "TASKSYS"})
end

function PopSystemView:onTouchEventGoCreateBoxRoomView(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:recordThinkingData(send)
    self:addThrowBtnData(send)
    self:close()

    XH.viewManager:openView("CreateBoxRoomView", nil, {gameid = send._gameid, chooseTwoPlayer = send._chooseTwoPlayer })
end

-- 关闭游戏事件
function PopSystemView:onTouchEventGameOver(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:recordThinkingData(send)
    self:addThrowBtnData(send)
    cc.Director:getInstance():endToLua()
end

-- H5游戏统一跳转事件
function PopSystemView:onTouchEventH5Games(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:recordThinkingData(send)
    self:throwClickData(send)
    if XH.playerData:checkNewRealName() ~= 0 then return end--实名限制

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
       XH.TipTool.showToast("当前版本不支持，请下载最新版本", 1)
        return
    end
    if self._loginLock then 
        XH.TipTool.showToast("您操作的太频繁了，请稍后再试", 1)
        return
    end
    self._loginLock = true
    if XH.sdkManager and XH.sdkManager:isSupportFunctionAndroid("start_game_login") then
        XH.audioManager:SetMusicVolume(0)
        XH.audioManager:SetSFXVolume(0)
        local reqH5LoginCer = require("app.Req.H5Game.ReqH5StartGameLogin").new()
        reqH5LoginCer:addReqCallBack(self,self.h5StartGameLoginCallback)
        local nickname = XH.playerData:getNickName()
        local sex = XH.playerData:getSex()
        local id = XH.playerData:getNumberID()
        local url = XH.playerData:getWeChatURL()
        local temp = string.split(send:getName(), "_")
        local gameName = temp[#temp]
        if gameName == H5GameNameList.LEGEND and XH.playerData:getLegendUserID()~="" then
            id = XH.playerData:getLegendUserID()
        elseif gameName == H5GameNameList.BUYU and XH.playerData:getBuYuUserID()~="" then
            id = XH.playerData:getBuYuUserID()
        end
        local bNeedConfigID = XH.playerData:isNeedConfigID() == 1
        reqH5LoginCer:start(id, nickname, sex, url, reqH5LoginCer.GameList[gameName], bNeedConfigID)
    else
        XH.TipTool.showToast("not support function!", 1)
    end
    self:performWithDelay(function()
        self._loginLock = false
    end, 5)
    self:addThrowBtnData(send)
end

function PopSystemView:onTouchEventUnityGames(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:recordThinkingData(send)
    self:throwClickData(send)
    if not XH.lobby:getModule("RRBuYu"):isSupportRRBY() then
        self:addThrowBtnData(send)
        return
    end
    if self._loginLock then 
        XH.TipTool.showToast("您操作的太频繁了，请稍后再试", 1)
        return
    end
    self._loginLock = true
    self:performWithDelay(function()
        self._loginLock = false
    end, 5)
    if device.platform == "ios" then
        XH.lobby:getModule("RRBuYu"):joinInRRBuyu()
    elseif device.platform == "android" then
        if XH.lobby:getModule("RRBuYu"):isDownloading() then
            return
        end
        XH.lobby:getModule("RRBuYu"):isDownloadCompleted(false)
        XH.lobby:getModule("Replugin"):postToSepperllitaPlugin(2)
    end
    self:addThrowBtnData(send)
    self:close()
end

function PopSystemView:h5StartGameLoginCallback(cell, type, data)
    -- 回恢复音效
    local musicPercent = XH.lobby:getModule("Set"):getMusicPercent()
    local soundPercent = XH.lobby:getModule("Set"):getSoundPercent()
    XH.audioManager:SetMusicVolume(musicPercent / 100.0)
    XH.audioManager:SetSFXVolume(soundPercent / 100.0)
end

function PopSystemView:addTimeClose(goActBtn,value)
    if goActBtn then
        local canclosetime = 5
		local totaltime = 0
        local pos1 = string.find(value.link,"-")
        local pos2 = string.find(value.link,"-",pos1+1)
        if pos1 and pos2 then
            canclosetime = tonumber(string.sub(value.link,pos1+1,pos2-1))
            totaltime = tonumber(string.sub(value.link,pos2+1,#value.link))
        elseif pos1 and pos2 == nil then
            canclosetime = tonumber(string.sub(value.link,pos1+1,#value.link))
        end
		local text = ccui.Text:create()
		if text then
			goActBtn:addChild(text)
			text:setFontSize(44)
			text:setAnchorPoint(cc.p(0.5, 0.5))
			text:setPosition(goActBtn:getContentSize().width/2,goActBtn:getContentSize().height/2)
            text:setString((totaltime) .. "s 后即可跳过")
            local sequence = cc.Sequence:create(cc.CallFunc:create( function()
                if totaltime > 0 then
                    text:setString((totaltime) .. "s 后即可跳过")
                    totaltime = totaltime - 1
                elseif totaltime == 0 then
                    totaltime = totaltime - 1
					text:setString("跳过（" .. canclosetime .. "s）")
                    goActBtn:setClickCallBack(handler(self, self.onTouchEventBGClose))
                else
					if canclosetime > 0 then
						canclosetime = canclosetime - 1
						text:setString("跳过（" .. canclosetime .. "s）")
					else
						self:close()
					end
                end                  
            end ), cc.DelayTime:create(1))
            local action = cc.RepeatForever:create(sequence)
            text:runAction(action)
		end
	end
end

function PopSystemView:addTimeCloseNew(goActBtn,value)
    if goActBtn then
        local time_close
        local pos1 = string.find(value.link,"-")
        if pos1  then
            local time_string = string.sub(value.link,pos1+1,#value.link)
            time_close = tonumber(string.match(time_string,("%d+")))
        end
        local data = {}
        data.callback = function()
            self:close()
        end
        data.timeClose = time_close or 5
        local timeItem = require("src.lobby.Modules.PopSystem.RadialTimeItem").new(data)
        timeItem:setAnchorPoint(cc.p(0.5, 0.5))
        goActBtn:addChild(timeItem)
        timeItem:setPosition(goActBtn:getContentSize().width/2,goActBtn:getContentSize().height/2)
    end
end

function PopSystemView:onTouchEventGoToTeaHouseList(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:recordThinkingData(send)
    self:addThrowBtnData(send)
    if XH.playerData:checkNewRealName() ~= 0 then return end--实名限制
    if XH.playerData:checkNewRealNameVisitor() ~= 0 then return end--游客登陆
    XH.teaHouseManager:openTeaHouseList()
    self:close()
end

function PopSystemView:addThrowBtnData(send)
    local tmpData = {}
    tmpData.userid = XH.playerData:getNumberID()
    tmpData.popname = self._webViewConf.name
    tmpData.btnname = send:getName()
    if  string.find(tmpData.popname, POPVIEWKEYWORD.RENRENBUYU) then
        if tmpData.btnname == "close" then
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RENRENBUYU_POPVIEW_CLOSE)
        end
        if tmpData.btnname == "RENRENBUYU" then
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RENRENBUYU_GET_AWARD)
        end
    end
    XH.throwDataManager:throwData(XH.ThrowDataDefine.POPSYSTEM_TOUCH_BTN, tmpData) 
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.POPSYSTEM_VIEW_BTN_CLICK,{block_item_id = self._webViewConf.id or 0, label = send:getName()})
end

function PopSystemView:onTouchEventEnterTeaHouse(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:recordThinkingData(send)
    if XH.teaHouseManager:isInTeaHouse() then
        self:close()
    else
        XH.lobby:getModule("PopSystem"):reqLastQuitTeaID()
    end
end

function PopSystemView:onTouchEventShopShuffle(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:recordThinkingData(send)
    local ShopConfig = require("lobby.Modules.Shop.Config")
    local paramData = {}
    paramData.productType = ShopConfig.SpecialTag.SHUFFLE
    XH.viewManager:openView("ShopView", nil, paramData)
    if self._isSHUFFLE then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.SHUFFLE_LAYER_GO)
    end
    self:close()
end

function PopSystemView:onTouchEventOpenSxvipView(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:recordThinkingData(send)
    XH.viewManager:openView("SxvipView", nil, {defalutMenu = SxVipConfig.MENU_DEFINE.VIP_PRIVILEGE})
    self:close()
end

-- 每个新增的弹窗 增加一下 self:recordThinkingData(send) 上抛埋点

return PopSystemView<_  