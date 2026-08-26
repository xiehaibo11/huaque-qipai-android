local ToponDailySignView = class("ToponDailySignView", XH.ViewBase)
local ToponActConfig = require("lobby.Modules.ToponAct.Config")

ToponDailySignView.KW_NAME_EVERY_DAY_NODE = "KW_NAME_EVERY_DAY_NODE"

local KW_PANEL_SIGNIN = "KW_PANEL_SIGNIN"
local KW_IMG_SIGNIN_BG = "KW_IMG_SIGNIN_BG"
local KW_IMG_DAY = "KW_IMG_DAY"
local KW_IMG_DROP_ID = "KW_IMG_DROP_ID"
local KW_TEXT_DROP = "KW_TEXT_DROP"
local KW_SIGNIN_DAY = "KW_SIGNIN_DAY_"
local KW_IMG_YQD = "KW_IMG_YQD"
local KW_IMG_FB = "KW_IMG_FB"

function ToponDailySignView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/EveryDaySignInLayerNew.csb"
end

function ToponDailySignView:getBindingInfo()
    return {
        ["_KW_TEXT_REFUSH_TIME"] = { varName = "_signTimeText" },
        ["_KW_BTN_SIGNIN"] = { varName = "_signBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventSignIn" },
        ["_KW_BTN_SIGNIN_CHAOLIANG"] = { varName = "_signReceivetn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventSignIn" },
        ["_KW_CHECK_BOX_CHANGE"] = { varName = "_changeCheckBox", type = XH.UI_TYPE.CHECKBOX, onSelect = "onTouchEventChange" },
        ["_KW_CHECK_BOX_DOUBLE"] = { varName = "_changeDoubleCheckBox", type = XH.UI_TYPE.CHECKBOX, onSelect = "onTouchEventChangeDouble" },
        ["_KW_UI_ZHUANPAN_BG"] = { varName = "_toponAwardList" },
        ["_KW_UI_ZHUANPAN_BG_1"] = { varName = "_sginAwardList" },
        ["_KW_BTN_GO_GOLDSCENE"] = { varName = "_KW_BTN_GO_GOLDSCENE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventGoldScene" },
        ["_KW_IMG_GO_GOLDSCENE"] = { varName = "_KW_IMG_GO_GOLDSCENE" },
        ["_KW_BTN_TIPS"] = { varName = "_KW_BTN_TIPS", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventShowTips" },
        ["_KW_PANEL_TIPS"] = { varName = "_KW_PANEL_TIPS", onTouchEnded = "onTouchEventHideTips" },
        ["_KW_BTN_GOGET"] = { varName = "_KW_BTN_GOGET" },
        ["_KW_BTN_TIPS_2"] = { varName = "_KW_BTN_TIPS_2", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventShowTips" },
        ["_KW_TEXT_COUNT_DB"] = { varName = "_KW_TEXT_COUNT_DB" },
        ["_KW_TEXT_COUNT_DB2"] = { varName = "_KW_TEXT_COUNT_DB2" },
        ["_KW_IS_REWARD"] = { varName = "_KW_IS_REWARD" },
        ["_KW_BTN_GOGET_EX"] = { varName = "_KW_BTN_GOGET_EX", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventGoGet" },
    }
end

function ToponDailySignView:ctor(param)
    param = param or {}
    ToponDailySignView.super.ctor(self, param)
    self.csbNode = self
    self.width = param.width or 1342
    self.height = param.height or 870
    self:setContentSize(self.width, self.height)
    self:setName(self.KW_NAME_EVERY_DAY_NODE)

    self._today = 0
    self._actInfo = nil
    self._nodeList = {}
    self._urlList = {}
    self._isDownload = false
    self._initTopon = false
    self._initSgin = false
    self:reqAwardList()
    XH.lobby:getModule("BagSysNew"):reqDoubleCards(true)

    self._KW_BTN_GOGET_EX:setBright(XH.lobby:getModule("LuckyTask"):isExistActId())
    self._KW_BTN_GOGET_EX:setEnabled(XH.lobby:getModule("LuckyTask"):isExistActId())

    local conventionData = {}
    conventionData.block_label = "活动"
    conventionData.label = "登录有礼"
    local extraData = {}
    extraData.area_id = XH.areaData:getAreaID()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.TABS_ACTIVITY_TAB_PUSH, conventionData, extraData)
end

function ToponDailySignView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("ToponAct"), eventKeyName = "KW_TOPON_ACT_SHIN_INFO", callBack = "flushEverySigninInfo" },
        { module = XH.lobby:getModule("BagSysNew"), eventKeyName = "EVENT_UPDATE_DOUBLE_CARD", callBack = "flushEventDoubleInfo" },
    }
end

function ToponDailySignView:reqShareAward()
    if self._actInfo == nil then
        return
    end

    -- performWithDelay(self, function()
    -- end, 5)
    local srsList = XH.netEngine:getAllSRS()
    -- for k, _ in pairs(srsList) do
    --         srsList[k]:close()
    --         XH.netEngine:removeSRS(k)
    -- end
    local closeCount = 0
    for k, _ in pairs(srsList) do
        if srsList[k]:getLinkState() == XH.SRS_LINK_STATE.LINK_STATE_SUCCESS then
            closeCount = closeCount + 1
        end
    end
    if closeCount == #srsList then
        XH.lobby:getModule("ToponAct"):setDailySignWebType(self._actInfo.webType)
    else
        local awardType = ToponActConfig.SIGN_CONFIG.SIGN
        if self._changeDoubleCheckBox:isSelected() then
            awardType = ToponActConfig.SIGN_CONFIG.DOUBLE
        end
        XH.lobby:getModule("ToponAct"):reqActAward(self._actInfo.webType, nil, nil, awardType)
    end
    self:flushSignInState()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.GOLD_SHARE_NEW_SIGN_SHARE_SUCCESS, nil, { area_id = XH.areaData:getAreaID() })

end

function ToponDailySignView:reqShareAwardFailed()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.GOLD_SHARE_NEW_SIGN_SHARE_FAILED, nil, { area_id = XH.areaData:getAreaID() })
end

function ToponDailySignView:flushSignInState()
    local getAllChild
    self:initSignInBtn(1)
    if self._toponAwardList then
        getAllChild = self._toponAwardList:getChildren()
        if getAllChild[self._today] then
            local imgSignIn = XH.UITool.seekNodeByName(getAllChild[self._today], KW_IMG_YQD)
            local imgSignFanBei = XH.UITool.seekNodeByName(getAllChild[self._today], KW_IMG_FB)
            if imgSignIn then
                imgSignIn:setVisible(true)
            end
            if imgSignFanBei then
                imgSignFanBei:setVisible(true)
            end
            local dropNode = XH.UITool.seekNodeByName(getAllChild[self._today], KW_IMG_DROP_ID)
            if dropNode then
                dropNode:stopAllActions()
            end
        end
    end
    if self._sginAwardList then
        getAllChild = self._sginAwardList:getChildren()
        if getAllChild[self._today] then
            local imgSignIn = XH.UITool.seekNodeByName(getAllChild[self._today], KW_IMG_YQD)
            if imgSignIn then
                imgSignIn:setVisible(true)
            end
            local dropNode = XH.UITool.seekNodeByName(getAllChild[self._today], KW_IMG_DROP_ID)
            if dropNode then
                dropNode:stopAllActions()
            end
        end
    end
    XH.lobby:getModule("TabsActivity"):onShowSginRedPoint(false)
    if self._actInfo.actInfo.currTime == 0 then
        self._actInfo.actInfo.currTime = os.time()
        self._actInfo.actInfo.times = self._actInfo.actInfo.times + 1
    end
end

function ToponDailySignView:onTouchEventSignIn(send, eventType)
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.TABS_ACTIVITY_DAY_SIGN)
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.TABS_ACTIVITY_JOINACT)
    if self._changeCheckBox:isSelected() then
        self:flushSignInState()
        XH.lobby:getModule("ToponAct"):getToponActAward(self._actInfo.webType, self._actInfo.actInfo.poolid)
    else
        XH.lobby:getModule("Share"):startShareFzb(self.reqShareAward, self.reqShareAwardFailed, self)
    end
    local conventionData = {}
    conventionData.block_label = "登录有礼"
    if self._changeCheckBox:isSelected() then
        conventionData.label = "超量领取"
    else
        conventionData.label = "分享领取奖励"
    end
    local extraData = {}
    extraData.area_id = XH.areaData:getAreaID()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.TABS_ACTIVITY_SIGN_IN_MORE, conventionData, extraData)
end

function ToponDailySignView:onTouchEventChange(send, eventType)
    if eventType == ccui.CheckBoxEventType.selected then
        self._changeDoubleCheckBox:setSelected(false)
        self:showAwardList()
    elseif eventType == ccui.CheckBoxEventType.unselected then
        self:showAwardList()
    end

    local awardInfo = self._actInfo.actInfo
    if awardInfo.currTime == 0 then
        self:initSignInBtn(0)
    end
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.TABS_ACTIVITY_DAY_SIGN_CHECKBOX)
end

function ToponDailySignView:onTouchEventChangeDouble(send, eventType)
    if eventType == ccui.CheckBoxEventType.selected then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.qd2024061104, { page_item_id = "选中" })
        self._changeCheckBox:setSelected(false)
        self:showAwardList()
    elseif eventType == ccui.CheckBoxEventType.unselected then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.qd2024061104, { page_item_id = "取消" })
        self:showAwardList()
    end

    local awardInfo = self._actInfo.actInfo
    if awardInfo.currTime == 0 then
        self:initSignInBtn(0)
    end
end

function ToponDailySignView:reqAwardList()
    if XH.lobby:getModule("ToponAct"):isSign50() then
        XH.lobby:getModule("ToponAct"):reqSignDetail()
        return
    end
    XH.lobby:getModule("ToponAct"):showDailySignLayer()
end

function ToponDailySignView:judgeTime(time)
    local day = math.floor(time / (24 * 60 * 60))
    local hour = math.floor((time - (24 * 60 * 60 * day)) / (60 * 60))
    local minute = math.ceil((time - (24 * 60 * 60 * day) - (60 * 60 * hour)) / 60)
    if day > 0 then
        return day .. "天" .. hour .. "时"
    elseif hour > 0 then
        return hour .. "时" .. minute .. "分"
    else
        return minute .. "分"
    end
end

function ToponDailySignView:flushEverySigninInfo(event)
    local data = event.data
    if self._signTimeText then
        if data.actInfo.signTime then
            self._signTimeText:setString("签到刷新倒计时：" .. self:judgeTime(data.actInfo.signTime) .. " (每周一0点刷新)")
        else
            self._signTimeText:setString("")
        end
        self._signTimeText:setVisible(true)
    end
    local count = XH.lobby:getModule("BagSysNew"):getDoubleCardsCount()
    self._changeDoubleCheckBox:setVisible(count > 0)
    if data.actInfo.newSginInfo and data.actInfo.newSginInfo.newAwardInfo then
        self._changeCheckBox:setVisible(true)
    else
        self._changeCheckBox:setVisible(false)
        self._changeCheckBox:setSelected(false)
    end
    self._actInfo = data
    self._toponAwardList:setVisible(false)
    self._sginAwardList:setVisible(false)

    self:showAwardList()
end

function ToponDailySignView:showAwardList()
    local awardInfo = self._actInfo.actInfo
    self._nodeList = {}
    self._urlList = {}
    if XH.ChannelTool.checkIsECarChannel() then
        self._changeCheckBox:setVisible(false)
        self._changeCheckBox:setSelected(false)
    end
    if XH.playerData:isRechargePlayer() then
        self._changeCheckBox:setVisible(false)
        self._changeCheckBox:setSelected(false)
    end
    self:initSignInBtn(awardInfo.currTime)
    if self._changeCheckBox:isSelected() or self._changeDoubleCheckBox:isSelected() then
        self._toponAwardList:setVisible(true)
        self._sginAwardList:setVisible(false)
        if not self._initTopon then
            local showInfo = awardInfo.newSginInfo.newAwardInfo
            if self._changeCheckBox:isSelected() then
                showInfo = awardInfo.newSginInfo.adAwardInfo
            end
            self:initSignInData(self._toponAwardList, showInfo, awardInfo.currTime, awardInfo.times, true)
            -- self._initTopon = true
        end
    else
        self._toponAwardList:setVisible(false)
        self._sginAwardList:setVisible(true)
        if not self._initSgin then
            self:initSignInData(self._sginAwardList, awardInfo.newSginInfo.oldAwardInfo, awardInfo.currTime, awardInfo.times)
            -- self._initSgin = true
        end
    end
end

function ToponDailySignView:initSignInData(node, data, currTime, times, isDouble)
    self:initSignInBtn(currTime)
    local awardList = data
    if times and awardList then
        self._today = times + 1
        for i = 1, #awardList do
            local dayNode = XH.UITool.seekNodeByName(node, KW_SIGNIN_DAY .. i)
            local signNode = XH.UITool.seekNodeByName(dayNode, KW_PANEL_SIGNIN)
            if signNode then
                signNode:setPosition(cc.p(0, 0))
                self:initDayNode(signNode, awardList[i], i, times, currTime, isDouble)
            end
        end
    end
    if currTime then
        local dayNode = XH.UITool.seekNodeByName(node, KW_SIGNIN_DAY .. self._today)
        if dayNode then
            local dropNode = XH.UITool.seekNodeByName(dayNode, KW_IMG_DROP_ID)
            if dropNode then
                self:runTimeLineAni(dropNode)
            end
        end
    end
end


function ToponDailySignView:initSignInBtn(currTime)
    if currTime then
        if currTime == 0 then
            self._signBtn:setBright(true)
            self._signBtn:setEnabled(true)
            if self._changeCheckBox:isSelected() then
                self._signReceivetn:setVisible(true)
                self._signBtn:setVisible(false)
            else
                self._signReceivetn:setVisible(false)
                self._signBtn:setVisible(true)
            end
            local count = XH.lobby:getModule("BagSysNew"):getDoubleCardsCount()
            self._changeDoubleCheckBox:setVisible(count > 0)
            self:flushGoGetState(count <= 0)
            self._KW_IS_REWARD:setVisible(false)
        else
            self._changeDoubleCheckBox:setSelected(false)
            self._KW_IS_REWARD:setVisible(true)
            self._changeCheckBox:setVisible(false)
            self._changeDoubleCheckBox:setVisible(false)
            self._signBtn:setVisible(true)
            self._signBtn:setBright(false)
            self._signBtn:setEnabled(false)
            self._signReceivetn:setVisible(false)
            self:flushGoGetState(true)
        end
        -- self._signBtn:setVisible(true)
    end
    self:initGoldSceneBtn(currTime)
end

function ToponDailySignView:initDayNode(node, data, index, times, currTime, isDouble)
    if node and data then
        local dropNode = XH.UITool.seekNodeByName(node, KW_IMG_DROP_ID)
        local dropCntText = XH.UITool.seekNodeByName(node, KW_TEXT_DROP)
        local imgSignIn = XH.UITool.seekNodeByName(node, KW_IMG_YQD)
        local imgSignFanBei = XH.UITool.seekNodeByName(node, KW_IMG_FB)
        dropNode:ignoreContentAdaptWithSize(true)
        self:reqDownloadImage(dropNode, data.image_url)
        dropCntText:setString(data.prize_name)
        if index <= times then
            imgSignIn:setVisible(true)
            if imgSignFanBei then
                if data.signType == ToponActConfig.SIGN_CONFIG.SIGN then
                    imgSignFanBei:setVisible(false)
                else
                    imgSignFanBei:setVisible(true)
                end
            end

        end

        local dayImg = XH.UITool.seekNodeByName(node, KW_IMG_DAY)
        if dayImg then
            dayImg:loadTexture("img_every_day_text_" .. index .. ".png", ccui.TextureResType.plistType)
        end
        local dayNode = XH.UITool.seekNodeByName(node, KW_IMG_SIGNIN_BG)
        local dayNodeFB = XH.UITool.seekNodeByName(node, KW_IMG_FB)
        if currTime == 0 then
            if index == times + 1 then
                dayNode:loadTexture("img_every_day_bg_2.png", ccui.TextureResType.plistType)
                if dayNodeFB then
                    if isDouble then
                        dayNodeFB:setVisible(true)
                    else
                        dayNodeFB:setVisible(false)
                    end
                end
            end
        else
            if index == times then
                dayNode:loadTexture("img_every_day_bg_2.png", ccui.TextureResType.plistType)
            end
        end
    end
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_TOPON_DAILY_SIGN_AWARD)
end

function ToponDailySignView:runTimeLineAni(node)
    node:stopAllActions()
    local action = cc.CSLoader:createTimeline(self:getCSBPath())
    node:runAction(action)
    action:gotoFrameAndPlay(0, 105, true)
    --local ani1 = cc.ScaleTo:create(0.85,0.5)
    --local delayTime = cc.DelayTime:create(0.8)
    --local ani3 = cc.ScaleTo:create(0.5,0.85)
    --allAction = cc.Sequence:create(ani1,delayTime,ani3,delayTime)
    --node:runAction(cc.RepeatForever:create(allAction))
end

function ToponDailySignView:reqDownloadImage(node, image_url)
    node:removeAllChildren()
    --XH.UITool.reloadNodeRemoteImage(node, image_url, "award_img", false)
    --local awardImg = XH.UITool.seekNodeByName(node, "award_img")
    --awardImg:setMaxContentSize(node:getContentSize())
    local size = node:getContentSize()
    local imageNode = XH.RemoteImage.new()
    :setTouchEnabled(false)
    :setPosition(size.width / 2, size.height / 2)
    :addTo(node)
    :setMaxContentSize(size)
    imageNode:setUrl(image_url)
end

local DEFAULT_DOWNLOAD_IMAGE_DIR = device.writablePath .. "tmpImageCachDir/"
function ToponDailySignView:reqDownloadImageCallBack(node, cell, ntype, backData)
    if ntype == cell.TYPE.SUCCESS then
        local imageName = XH.StringTool.getImageNameByUrl(backData)
        local imagePath = DEFAULT_DOWNLOAD_IMAGE_DIR .. imageName
        if node then
            node:loadTexture(imagePath, ccui.TextureResType.localType)
        end
    end
end

function ToponDailySignView:onTouchEventGoldScene()
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_SIGN_IN_GO_GOLD_STATE, 1)
    XH.lobby:getModule("Lobby"):enterNewGold()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_GOLD_THROW_ERQI_7, nil, { area_id = XH.areaData:getAreaID() })
    XH.viewManager:closeView("TabsActivityView")
end

function ToponDailySignView:initGoldSceneBtn(currTime)
    local clickTime = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_SIGN_IN_GO_GOLD_STATE, 0)
    if currTime == 0 or clickTime == 1 then
        self:flushGoldSceneState(false)
        return
    end
    self:flushGoldSceneState(true)
end

function ToponDailySignView:flushGoldSceneState(isShow)
    if not XH.lobby:getModule("NewGoldScene"):getIconVisible() then
        isShow = false
    end
    self._KW_BTN_GO_GOLDSCENE:setVisible(isShow)
    self._KW_IMG_GO_GOLDSCENE:setVisible(isShow)
end

function ToponDailySignView:onExit()
    if XH.lobby:getModule("ToponAct"):getDailySignWebType() ~= nil then
        XH.TipTool.showToast("网络异常，重连后会自动领取奖励")
    end
end

function ToponDailySignView:onTouchEventShowTips()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.qd2024061103)
    self._KW_PANEL_TIPS:setVisible(not self._KW_PANEL_TIPS:isVisible())
end

function ToponDailySignView:onTouchEventHideTips()
    self._KW_PANEL_TIPS:setVisible(false)
end

-- 获取加倍卡跳转
function ToponDailySignView:onTouchEventGoGet()
    if self._actInfo and self._actInfo.actInfo then
        local day = self._actInfo.actInfo.times
        if self._actInfo.actInfo.currTime == 0 then
            day = day + 1
        end
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.qd2024061102, { page_item_id = "第" .. day .. "天" })
    else
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.qd2024061102, { page_item_id = "第0天" })
    end
    XH.viewManager:openView("LuckyTaskView")
    XH.viewManager:closeView("TabsActivityView")
end

-- 刷新加倍卡数量
function ToponDailySignView:flushEventDoubleInfo()
    -- 显示数量等
    local count = XH.lobby:getModule("BagSysNew"):getDoubleCardsCount()
    self._KW_TEXT_COUNT_DB2:setString(count .. "张奖励加倍卡")
    self._KW_TEXT_COUNT_DB:setString("拥有" .. count .. "张")
    if self._isInitClick then
        return
    end
    self._isInitClick = true
    -- 设置加倍选中状态
    if count > 0 then
        self._changeCheckBox:setSelected(false)
        self._changeDoubleCheckBox:setSelected(true)
        if self._actInfo then
            local awardInfo = self._actInfo.actInfo
            if awardInfo.currTime == 0 then
                self:flushGoGetState(false)
                self:initSignInBtn(0)
            end
            self:showAwardList()
        end
    else
        self:flushGoGetState(true)
        self._changeDoubleCheckBox:setVisible(false)
    end
end

function ToponDailySignView:flushGoGetState(isShow)
    if isShow and not self._KW_BTN_GOGET:isVisible() then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.qd2024061101)
    end
    self._KW_BTN_GOGET:setVisible(isShow)
end

return ToponDailySignView ;R  