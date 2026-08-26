local LaborDayGiftView = class("LaborDayGiftView",XH.ViewBase)
local ShopConfig = require("lobby.Modules.Shop.Config")
local ShowPropFunction = require("lobby.Modules.BagSysNew.ShowPropFunction")

local KW_PANEL_PROP = "_KW_PANEL_PROP_"
local KW_IMG_INFO = "_KW_IMG_INFO"
local KW_TEXT_PRICE = "_KW_TEXT_PRICE"
local KW_PROP_BG = "_KW_PROP_BG"
local KW_TEXT_NAME = "_KW_TEXT_NAME"
local KW_TEXT_NUM = "_KW_TEXT_NUM"
local KW_IMG_BUY = "_KW_IMG_BUY"
local KW_TEXT_SHOUJIA = "_KW_TEXT_SHOUJIA"
local KW_BTN_PREVIEW = "_KW_BTN_PREVIEW"
local KW_CHECK_BOX = "_KW_CHECK_BOX"

local KW_ONE_DAY = 24 * 60 * 60
local KW_ONE_HOUR = 60 * 60
local KW_ONR_MINUTE = 60
local KW_ACT_RULE_ID = 2020050101
local KW_SHOW_SIZE = cc.size(300,300)

-- 道具标签（道具服配置的）
local KW_PROP_HEAD_FRAME_TAG_ID     = "head_frame"      --头像框
local KW_PROP_CARD_HOLDER_TAG_ID    = "card_holder"     --压牌器
local KW_PROP_TABLE_TAG_ID          = "table_bg"        --桌布

function LaborDayGiftView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/LaborDayGift/LaborDayGift.csb"
end

function LaborDayGiftView:getBindingInfo()
    return {
        ["_KW_BTN_BUY"] = {varName = "_btnBuy", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventBuy" },
        ["_KW_BTN_CLOSE"] = {varName = "_btnClose", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KW_BTN_RULE"] = {varName = "_btnRule", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventRule" },
        ["_KW_BTN_BAG"] = {varName = "_btnBag", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventBag" },
        ["_KW_QIPAO_BAG"] = {varName = "_qipaiBag" },
        ["_KW_LISTVIEW_TIME_TEXT"] = {varName = "_listviewTimeText" },
        ["_KW_TEXT_TIME"] = {varName = "_textTime" },
        ["_KW_TEXT_DAY"] = {varName = "_textDay" },
        ["_KW_TEXT_TIAN"] = {varName = "_textTian" },
        ["_KW_TEXT_HOUR"] = {varName = "_textHour" },
        ["_KW_TEXT_XIAOSHI"] = {varName = "_textXiaoShi" },
        ["_KW_TEXT_MINUTE"] = {varName = "_textMinute" },
        ["_KW_TEXT_FEN"] = {varName = "_textFen" },
        ["_KW_PANEL_LIBAO_"] = { varName = "_panelLiBao_", beginIndex = 1, endIndex = 3 },
        ["_KW_PANEL_RULE"] = { varName = "_panelRule", type = XH.UI_TYPE.PANEL, onTouchEnded = "onTouchEventCloseRule" },
        ["_KW_BTN_RULECLOSE"] = { varName = "_btnCloseRule", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventCloseRule" },
        ["_KW_RULE_WEB_VIEW"] = { varName = "_ruleWebView" },

        ["_KW_PANEL_PREVIEW"] = { varName = "_panelPreview", type = XH.UI_TYPE.PANEL, onTouchEnded = "onTouchEventClosePreView" },
        ["_KW_SHOW_NODE"] = { varName = "_nodeShow"},
        ["_KW_BG_PREVIEW"] = { varName = "_bgProview"},
        ["_KW_UI_ROOT_LAYER"] = { varName = "_rootLayer"},
    }
end

function LaborDayGiftView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("LaborDayGift"), eventKeyName = "EVENT_FULSH_LABOR_DAY_INFO", callBack = "onFulshInfo" },
        { module = XH.lobby:getModule("LaborDayGift"), eventKeyName = "EVENT_FULSH_LABOR_DAY_PAY", callBack = "onFulshBuyBtn" },
        { module = XH.lobby:getModule("LaborDayGift"), eventKeyName = "EVENT_FULSH_LABOR_DAY_ICON", callBack = "onFulshActInfo" },
        { module = XH.sdkManager, eventKeyName = "EVENT_IAP_CALLBACK", callBack = "respBuyDiamond" },
    }
end

function LaborDayGiftView:ctor(actInfo)
	LaborDayGiftView.super.ctor(self)
    self._price = {}
    self._chooseLibao = 0
    self._openTime = os.time()
    self._actInfo = actInfo
    self._isBuy = {false,false,false}
    XH.lobby:getModule("Shop"):reqProductList(ShopConfig.ProductType.DIAMOND)
    if actInfo then
        self:init(actInfo)
    else
        XH.lobby:getModule("LaborDayGift"):getActInfo()
    end
end

function LaborDayGiftView:init(actInfo)
    self:initTime(actInfo.time)
    self._btnBuy:setEnabled(true)
    self._btnBuy:setBright(true)
    for i, info in ipairs(actInfo.libaoinfo) do
        self._isBuy[i] = info.isBuy
        local panel = self["_panelLiBao_" .. i]
        local priceText = XH.UITool.seekNodeByName(panel, KW_TEXT_PRICE)
        if priceText then
            priceText:setString(info.price .. "元")
            self:setPriceTextColor(panel, false)
            self._price[i] = info.price
        end
        local buyImg = XH.UITool.seekNodeByName(panel, KW_IMG_BUY)
        if buyImg then
            buyImg:setVisible(info.isBuy)
        end
        local libaoBg = XH.UITool.seekNodeByName(panel, KW_IMG_INFO)
        if libaoBg then
            libaoBg:loadTexture("Labour_day_nobg.png", ccui.TextureResType.plistType)
            libaoBg:addTouchEventListener(handler(self, self.onTouchEventChooseLiBao))
            libaoBg:setTag(i)
            if info.isBuy then
                libaoBg:setEnabled(false)
            end
        end
        self:loadAward(panel, info.award)
    end
    for i = 3, 1, -1 do
        if actInfo.libaoinfo[i].isBuy == false then
            local libaoBg = XH.UITool.seekNodeByName(self["_panelLiBao_" .. i], KW_IMG_INFO)
            if libaoBg then
                libaoBg:loadTexture("Labour_day_yesbg.png", ccui.TextureResType.plistType)
                self._chooseLibao = i
                self:setPriceTextColor(libaoBg, true)
            end
            break
        end
    end
    self:initWebView()
end

function LaborDayGiftView:initTime(time)
    local day = math.floor( time/KW_ONE_DAY )
    local hour = math.floor( (time - day * KW_ONE_DAY) / KW_ONE_HOUR )
    local minute = math.floor( (time - day * KW_ONE_DAY - hour * KW_ONE_HOUR) / KW_ONR_MINUTE )
    if day > 0 then
        self._textDay:setString(day)
        self._textHour:setString(hour)
        self._textMinute:setString(minute)
    elseif hour > 0 then
        self._textDay:setString("")
        self._textTian:setString("")
        self._textHour:setString(hour)
        self._textMinute:setString(minute)
    elseif minute > 5 then
        self._textDay:setString("")
        self._textTian:setString("")
        self._textHour:setString("")
        self._textXiaoShi:setString("")
        self._textMinute:setString(minute)
    else
        self._textDay:setString("")
        self._textTian:setString("")
        self._textHour:setString("")
        self._textXiaoShi:setString("")
        self._textMinute:setString(minute)
        self._scheToShow = cc.Director:getInstance():getScheduler():scheduleScriptFunc( function()
            time = time - 1
            if time == 0 then
                self:close()
                XH.TipTool.showTip({
                    type = XH.TIP_LAYER_TYPE.OK,
                }, "活动已经结束~")
            else
                minute = math.floor( (time - day * KW_ONE_DAY - hour * KW_ONE_HOUR) / KW_ONR_MINUTE )
                self._textMinute:setString(minute)
            end
        end , 1, false)
    end
    local listViewSize = self._textTime:getContentSize().width
                       + self._textDay:getContentSize().width
                       + self._textTian:getContentSize().width
                       + self._textHour:getContentSize().width
                       + self._textXiaoShi:getContentSize().width
                       + self._textMinute:getContentSize().width
                       + self._textFen:getContentSize().width
    self._listviewTimeText:setContentSize(listViewSize,100)
end

function LaborDayGiftView:onFulshInfo(event)
    if event.data then
        local isbuy = event.data
        self._isBuy = isbuy
        for i, isShowBuy in ipairs(isbuy) do
            local buyImg = XH.UITool.seekNodeByName(self["_panelLiBao_" .. i], KW_IMG_BUY)
            if buyImg then
                buyImg:setVisible(isShowBuy)
            end
            if isShowBuy then
                XH.UITool.seekNodeByName(self["_panelLiBao_" .. i], KW_IMG_INFO):setEnabled(false)
            end
        end
        self._chooseLibao = 0
        for i = 3, 1, -1 do
            if isbuy[i] == false then
                local libaoBg = XH.UITool.seekNodeByName(self["_panelLiBao_" .. i], KW_IMG_INFO)
                if libaoBg then
                    libaoBg:loadTexture("Labour_day_yesbg.png", ccui.TextureResType.plistType)
                    self._chooseLibao = i
                    self:setPriceTextColor(libaoBg, true)
                end
                break
            end
        end
        self._btnBuy:setEnabled(true)
        self._btnBuy:setBright(true)
    end
end

function LaborDayGiftView:onFulshBuyBtn(event)
    self._btnBuy:setEnabled(true)
    self._btnBuy:setBright(true)
end

function LaborDayGiftView:onFulshActInfo(event)
    if event.data then
        local actInfo = event.data.actInfo
        if actInfo and self._actInfo == nil then
            self._actInfo = actInfo
            self:init(actInfo)
        end
    end
end

function LaborDayGiftView:loadAward(panel, awardInfo)
    for i,award in ipairs(awardInfo) do
        local awardPanel = XH.UITool.seekNodeByName(panel, KW_PANEL_PROP .. i)
        if awardPanel then
            local awardBG = XH.UITool.seekNodeByName(awardPanel, KW_PROP_BG)
            local yulan = XH.UITool.seekNodeByName(awardPanel, KW_BTN_PREVIEW)
            if awardBG then
                awardBG:removeAllChildren()
                XH.UITool.reloadNodeRemoteImage(awardBG, award.url, "award_img")
                local awardImg = XH.UITool.seekNodeByName(awardBG, "award_img")
                awardImg:setContentSize(awardBG:getContentSize())
                if self:judgePreviewProp(award.propid) then
                    awardBG:addTouchEventListener(handler(self,self.onTouchEventOpenPreView))
                    yulan:addTouchEventListener(handler(self,self.onTouchEventOpenPreView))
                    awardBG:setTag(award.propid)
                    yulan:setTag(award.propid)
                else
                    yulan:setVisible(false)
                end
            end
            local awardName = XH.UITool.seekNodeByName(awardPanel, KW_TEXT_NAME)
            if awardName then
                awardName:setString(award.name)
            end
            local awardCnt = XH.UITool.seekNodeByName(awardPanel, KW_TEXT_NUM)
            if awardCnt then
                if string.find(award.name, "钻石") then
                    awardCnt:setString(award.cnt .. "钻")
                elseif XH.lobby:getModule("BagSysNew"):judgeForeverProp(award.propid) then
                    awardCnt:setString("永久")
                else
                    awardCnt:setString(award.cnt .. "天")
                end
            end
        end
    end
end

function LaborDayGiftView:initWebView()
    if device.platform == "windows" then
        return
    end
    local sz = self._ruleWebView
    if self._webView == nil then
        local webView = ccexp.WebView:create()
        webView:setContentSize(sz:getContentSize())
        webView:setScalesPageToFit(false)
        webView:setPosition(cc.p(sz:getContentSize().width/2, sz:getContentSize().height/2))
        webView:setAnchorPoint(cc.p(0.5, 0.5))
        webView:setBounces(false)
        webView:setVisible(false)
        sz:addChild(webView)
        self._webView = webView

        webView:setOnShouldStartLoading(function(sender,url)
            self._webView:setVisible(false)
            return true
        end)

        local UrlConf = require("app.Config.UrlConf")
        local url = string.format(UrlConf.GAME_RULE_HTML_ADDR .. "/%d/%d/%d.html", XH.SysTool:getChannelID(), XH.areaData:getAreaID(), KW_ACT_RULE_ID)
        if url then
            self._webView:loadURL(url)
        end
        self._webView:setVisible(false)
    end
end

function LaborDayGiftView:onTouchEventClose(send, eventType)
    self:close()
end

function LaborDayGiftView:onTouchEventBuy(send, eventType)
    if (not XH.lobby:getModule("Shop"):isAleardyRealName()) and(not XH.lobby:getModule("Lobby"):getIsForeignIP()) then
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK,
            funcOK = function()
                XH.viewManager:openView("IdentityCheckView")
            end
        } , "PLEASE_REAL_NAME")
        return
    end
    if self._chooseLibao == 0 then
        XH.TipTool.showToast("没有礼包可以购买啦~", 3)
        return
    end
    if self._chooseLibao == -1 then
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, "您已充值，请勿重复点击。若您以取消支付，可重新点击购买。")
        self._actInfo = nil
        XH.lobby:getModule("LaborDayGift"):getActInfo(false)
        return
    end
    send:setEnabled(false)
    send:setBright(false)
    if device.platform == "windows" then
        XH.lobby:getModule("LaborDayGift"):getActAward(self._chooseLibao)
    else
        self._isBuy[self._chooseLibao] = true
        XH.lobby:getModule("LaborDayGift"):buyLiBao(self._price[self._chooseLibao], self._chooseLibao)
        self._chooseLibao = -1
        local userData = {}
        userData.userid = XH.playerData:getNumberID()
        XH.throwDataManager:throwData(XH.ThrowDataDefine.LaborDayGiftBuy, userData)
    end
end

function LaborDayGiftView:onTouchEventRule(send, eventType)
    --self._panelRule:setVisible(true)
    --if self._webView then
    --    self._webView:setVisible(true)
    --end
    XH.viewManager:openView("CommonRuleView", nil, KW_ACT_RULE_ID)
end

function LaborDayGiftView:onTouchEventBag(send, eventType)
    --self:close()
    XH.viewManager:openView("BagMainViewNew")
end

function LaborDayGiftView:onTouchEventCloseRule(send, eventType)
    self._panelRule:setVisible(false)
    if self._webView then
        self._webView:setVisible(false)
    end
end

function LaborDayGiftView:onTouchEventChooseLiBao(send, eventType)
    local chooseTag = send:getTag()
    for i = 1, 3 do
        local libaoBg = XH.UITool.seekNodeByName(self["_panelLiBao_" .. i], KW_IMG_INFO)
        if libaoBg then
            if i == chooseTag then
                libaoBg:loadTexture("Labour_day_yesbg.png", ccui.TextureResType.plistType)
                self._chooseLibao = i
                self:setPriceTextColor(libaoBg, true)
            else
                libaoBg:loadTexture("Labour_day_nobg.png", ccui.TextureResType.plistType)
                self:setPriceTextColor(libaoBg, false)
            end
        end
    end
    -- local userData = { }
    -- userData.userid = XH.playerData:getNumberID()
    -- if self._chooseLibao == 1 then
    --    XH.throwDataManager:throwData(XH.ThrowDataDefine.LaborDayGiftLiBao1, userData)
    -- elseif self._chooseLibao == 2 then
    --    XH.throwDataManager:throwData(XH.ThrowDataDefine.LaborDayGiftLiBao2, userData)
    -- elseif self._chooseLibao == 3 then
    --    XH.throwDataManager:throwData(XH.ThrowDataDefine.LaborDayGiftLiBao3, userData)
    -- end
end

function LaborDayGiftView:close()
    if self._scheToShow then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._scheToShow)
    end
    if os.time() - self._openTime >= 3 then
        local userData = {}
        userData.userid = XH.playerData:getNumberID()
        XH.throwDataManager:throwData(XH.ThrowDataDefine.LaborDayGiftStop, userData)
    end
    LaborDayGiftView.super.close(self)
end

function LaborDayGiftView:respBuyDiamond(event)
    local code = event.data.code
    --local msg = event.data.msg
    XH.TipTool.hideLoading()
    if code == 200 then
        self._actInfo = nil
        self:onFulshInfo({data = self._isBuy})
        XH.SysTool.performWithDelayGlobal( function()
            XH.lobby:getModule("LaborDayGift"):getActAward(0)
        end, 0.5)
    elseif code == 202 then
        self:onFulshBuyBtn()
    end
end

function LaborDayGiftView:setPriceTextColor(panel, isChoose)
    local priceText = XH.UITool.seekNodeByName(panel, KW_TEXT_PRICE)
    local shoujiaText = XH.UITool.seekNodeByName(panel, KW_TEXT_SHOUJIA)
    if priceText and shoujiaText then
        if not isChoose then
            priceText:setTextColor(cc.c3b(16, 66, 179))
            shoujiaText:setTextColor(cc.c3b(16, 66, 179))
        else
            priceText:setTextColor(cc.c3b(183, 81, 15))
            shoujiaText:setTextColor(cc.c3b(183, 81, 15))
        end
    end
    self:setCheckBoxSelected(panel, isChoose)
end

function LaborDayGiftView:onTouchEventOpenPreView(send, eventType)
    local propId = send:getTag()
    if propId then
        local liBaoNode = send:getParent():getParent():getParent()
        local posX = liBaoNode:getPositionX()
        self._bgProview:setPositionX(posX)
        self:openPreviwPanel(true, propId)
    end
end

function LaborDayGiftView:onTouchEventClosePreView(send, eventType)
    self:openPreviwPanel(false)
end

function LaborDayGiftView:openPreviwPanel(isOpen, propId)
    if isOpen and propId then
        self:showProp(propId)
    end
    self._panelPreview:setVisible(isOpen)
end

function LaborDayGiftView:showProp(propId)
    if propId then
        local propConfig = XH.lobby:getModule("BagSysNew"):getBagData():getDevideBagConfigData()
        if propConfig[propId] then
            self._nodeShow:removeAllChildren()
            if propConfig[propId].props_tag_id == KW_PROP_HEAD_FRAME_TAG_ID then
                local node = self:createHeadFrameNode(propId)
                self._nodeShow:addChild(node)
            elseif propConfig[propId].props_tag_id == KW_PROP_CARD_HOLDER_TAG_ID then
                local node = self:createCardHolderNode(propId)
                self._nodeShow:addChild(node)
            elseif propConfig[propId].props_tag_id == KW_PROP_TABLE_TAG_ID then
                local node = self:createTableBGNode(propId)
                self._nodeShow:addChild(node)
            else
                local node = self:createPropImgNode(propId)
                self._nodeShow:addChild(node)
            end
        end
    end
end

function LaborDayGiftView:judgePreviewProp(propId)
    if propId then
        local propConfig = XH.lobby:getModule("BagSysNew"):getBagData():getDevideBagConfigData()
        if propConfig[propId] then
            if propConfig[propId].category_one == "装扮类" then
                return true
            end
        end
    end
    return false
end

function LaborDayGiftView:createHeadFrameNode(propId)
    local headImg = XH.RemoteImage.new()
    :setTouchEnabled(false)
    :setAnchorPoint(cc.p(0.5, 0.5))
    
    headImg:setUrl(XH.playerData:getWeChatURL())
    if XH.playerData:getWeChatURL() == "" then
        local sex = XH.playerData:getSex()
        headImg:loadTexture("_0002_mrtx_m.png", ccui.TextureResType.plistType)
        if sex == 0 then
            -- girl
            headImg:loadTexture("_0002_mrtx_w.png", ccui.TextureResType.plistType)
        end
        headImg:setScale(1.5)
    end

    if not ShowPropFunction.createHeadFrameAni(headImg,propId,0.8) then
        local headImgSize = headImg:getContentSize()
        local propConfig = XH.lobby:getModule("BagSysNew"):getBagData():getDevideBagConfigData()
        local propImg = XH.RemoteImage.new()
        :setTouchEnabled(false)
        :setAnchorPoint(cc.p(0.5, 0.5))
        :setPosition(headImgSize.width / 2, headImgSize.height / 2)
        :addTo(headImg)
        :setScale(1.4)
        propImg:setMaxContentSize(headImgSize)
        local props_img = propConfig[propId].props_img[#propConfig[propId].props_img]
        local url = props_img.img[#props_img.img].img_url
        propImg:setUrl(url,false)
    end

    return headImg
end

function LaborDayGiftView:createCardHolderNode(propId)
    local node = self:createPropImgNode(propId,cc.size(280,280))
    self:showPropsDargonAni(node,ShopConfig.YaPaiQiAni,1.3)
    return node
end

function LaborDayGiftView:createTableBGNode(propId)
    local node = self:createPropImgNode(propId,cc.size(200,200))
    self:showPropsAni(node,ShopConfig.TableBgAni,1.35)
    return node
end

function LaborDayGiftView:createPropImgNode(propId, size)
    size = size or KW_SHOW_SIZE
    local propConfig = XH.lobby:getModule("BagSysNew"):getBagData():getDevideBagConfigData()
    local propImg = XH.RemoteImage.new()
    :setTouchEnabled(false)
    :setAnchorPoint(cc.p(0.5,0.5))
    
    propImg:setMaxContentSize(size)
    local props_img = propConfig[propId].props_img[#propConfig[propId].props_img]
    local url = props_img.img[#props_img.img].img_url
    propImg:setUrl(url,false)
    
    return propImg
end

-- 道具节点动画
function LaborDayGiftView:showPropsAni(itemNode, aniConfig, scale)
    scale = scale or 1
    local size = cc.size(200,200)
    local aniNode = cc.Node:create()
    if aniNode and aniConfig then
        pcall( function()
            if not cc.FileUtils:getInstance():isFileExist(aniConfig.path) then
                return
            end
            ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(aniConfig.path)
            local showArmature = ccs.Armature:create(aniConfig.amatureName)
            if showArmature then
                aniNode:addChild(showArmature)
                showArmature:setAnchorPoint(cc.p(0.5, 0.5))
                showArmature:getAnimation():play(aniConfig.animationName, -1, 1)
                showArmature:setScale(scale)
                showArmature:setPosition(size.width/2,size.height/2)
            end
            itemNode:addChild(aniNode)
        end )
    end
end

-- 道具节点龙骨动画
function LaborDayGiftView:showPropsDargonAni(itemNode, aniConfig, scale)
    local size = cc.size(230,230)
    scale = scale or 1
    local aniNode = cc.Node:create()
    aniNode:setScale(scale)
    itemNode:addChild(aniNode)
    if aniNode and aniConfig then
        local propAni = display.playDargonBonesAnimByTimes(aniConfig, 0)
        if propAni then
            propAni:setAnchorPoint(cc.p(0.5, 0.5))
            aniNode:addChild(propAni)
            aniNode:setPosition(size.width/2,60)
        end
    end
end

function LaborDayGiftView:setCheckBoxSelected(node, isSelect)
    if node then
        isSelect = isSelect or false
        local checkBox = XH.UITool.seekNodeByName(node, KW_CHECK_BOX)
        if checkBox then
            checkBox:setSelected(isSelect)
        end
    end
end

return LaborDayGiftView GY  