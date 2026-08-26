local PersonalCenterView = class("PersonalCenterView", XH.ViewBase)
local ShopConfig = require("lobby.Modules.Shop.Config")

local DEFAULT_STRING = "------"

function PersonalCenterView:ctor(param)
    param = param or {}
    PersonalCenterView.super.ctor(self, param)

    local headFrameTexture = nil
    local bagModule = XH.lobby:getModule("BagSysNew")
    if bagModule then
        headFrameTexture = bagModule:getBagData():getSelfHeadFrameTexture()
    end
    if headFrameTexture then
        self._imgHeadFrameNew:loadTexture(headFrameTexture, ccui.TextureResType.localType)
        self._imgHeadFrameNew:setContentSize(cc.size(210, 210))
        self._imgHeadFrame:setVisible(false)
        self._imgHeadFrameNew:setVisible(true)
    end
    self:updateData()
    self._btnBuy1:setName(ShopConfig.SpecialTag.CARD)
    self._btnBuy2:setName(ShopConfig.SpecialTag.GOLD)
    self._btnBuy3:setName(ShopConfig.SpecialTag.DIAMOND)
    self:showRemodelUI()
    if XH.ChannelTool.judgeIsChannel() then 
        -- 隐藏切换区域
        self._btnSelectArea:setVisible(false)
    else
        -- 显示切换区域
        self._btnSelectArea:setVisible(true)
    end
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_01)
end

function PersonalCenterView:getCSBPath()
    return "cocosStudio/hall/CSB/PersonalCenter.csb"
end

function PersonalCenterView:getBindingInfo()
    return{
        ["_KW_BTN_COPY"] = { varName = "_btnCopy", onTouchEnded = "onTouchEventCopyID", type = XH.UI_TYPE.BUTTON },
        ["_KW_BTN_REAL_NAME"] = { varName = "_btnRealName", onTouchEnded = "onTouchEventRealName", type = XH.UI_TYPE.BUTTON },
        ["_KW_BTN_RE_REAL_NAME"] = { varName = "_btnReRealName", onTouchEnded = "onTouchEventRealName", type = XH.UI_TYPE.BUTTON },
        ["_KW_BTN_GPS"] = { varName = "_btnGPS", onTouchEnded = "onTouchEventGPS", type = XH.UI_TYPE.BUTTON },
        ["_KW_BTN_BUY_1"] = { varName = "_btnBuy1", onTouchEnded = "onTouchEventAdd", type = XH.UI_TYPE.BUTTON },
        ["_KW_BTN_BUY_2"] = { varName = "_btnBuy2", onTouchEnded = "onTouchEventAdd", type = XH.UI_TYPE.BUTTON },
        ["_KW_BTN_BUY_3"] = { varName = "_btnBuy3", onTouchEnded = "onTouchEventAdd", type = XH.UI_TYPE.BUTTON },
        ["_KW_BTN_CLOSE"] = { varName = "_btnClose", onTouchEnded = "onTouchEventClose", type = XH.UI_TYPE.BUTTON },
        ["_KW_BTN_SELECT_AREA"] = { varName = "_btnSelectArea", onTouchEnded = "onTouchEventChangeArea", type = XH.UI_TYPE.BUTTON },
        ["_KW_BTN_SELECT_LOGIN_TYPE"] = { varName = "_btnSelectLoginType", onTouchEnded = "onTouchEventChangeLoginType", type = XH.UI_TYPE.BUTTON },
        ["_KW_BTN_PHONE_BIND"] = { varName = "_btnPhoneBind", onTouchEnded = "onTouchEventPhoneBind", type = XH.UI_TYPE.BUTTON },
        ["_KW_BTN_THIRD_BIND"] = { varName = "_btnThirdBind", onTouchEnded = "onTouchEventThirdBind", type = XH.UI_TYPE.BUTTON },
        ["_KW_BTN_WECHAT_BIND"] = { varName = "_btnWeChatBind", onTouchEnded = "onTouchEventWeChatBind", type = XH.UI_TYPE.BUTTON },
        ["_KW_BTN_THIRD_UNBIND"] = { varName = "_btnThirdUnbind", onTouchEnded = "onTouchEventThirdUnbind", type = XH.UI_TYPE.BUTTON },
        ["_KW_PANEL_BG"] = { varName = "_btnBG", onTouchEnded = "onTouchEventBGClose" },
        ["_KW_TXT_NICKNAME"] = { varName = "_textNickname"},
        ["_KW_TXT_ROOM_CARD"] = { varName = "_textRoomCard"},
        ["_KW_TXT_GOLD"] = { varName = "_textGold"},
        ["_KW_TXT_DIAMOND"] = { varName = "_textDiamond"},
        ["_KW_TXT_ID"] = { varName = "_textID"},
        ["_KW_IMG_HEAD"] = { varName = "_imgHead", type = XH.UI_TYPE.REMOTEIMAGE },
        ["_KW_IMG_PHONE_BIND_TAG"] = { varName = "_imgPhoneBindTag" },
        ["_KW_TXT_PHONE_TIP"] = { varName = "_textPhoneTip"},
        ["_KW_TXT_PHONE"] = { varName = "_textPhone"},
        ["_KW_BMFONT_VIP_LEVEL"] = { varName = "_bmfontVIPLevel",type = XH.UI_TYPE.TEXT},
        ["_KW_TXT_DIAMOND_TIP"] = { varName = "_textDiamondTip"},
        ["_KW_IMG_TITLE"] = { varName = "_imageTitle", onTouchEnded = "onTouchEventTitle"},
        ["_KW_IMG_FRAME"] = { varName = "_imgHeadFrame" },
        ["_KW_IMG_FRAME_NEW"] = { varName = "_imgHeadFrameNew" },
        ["KW_HEAD_NODE"] = { varName = "_imgHeadNode" },

        ["_KW_TXT_ZHU_XIAO"] = { varName = "_btnZhuXiao", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventZhuXiao" },
        ["_KW_BTN_QUAN_XIAN"] = { varName = "_btnQuanXian", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventQuanXian" },

        ["_KW_TXT_CARD_TIP"] = {varName = "_buyFangKaText"},
        ["_KW_IMG_ICON_CARD_FREE"] = {varName = "_freeFangKaIcon"},
        ["_KW_TXT_CARD_TIP_FREE"] = {varName = "_freeFangKaText"},
        ["_KW_TXT_ROOM_CARD_FREE"] = {varName = "_freeFangKaNum"},
        ["_KW_BTN_FREE_TIP"] = {varName = "_btnfreeFangKa", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventFreeRoomCard"},
        ["_KW_TEXT_CHANNEL"] = {varName = "_textChannel"},
        ["_KW_IMG_ICON_CARD"] = {varName = "_buyFangKaIcon"},
        ["_KW_BTN_REAL_NAME_ING"] = {varName = "_btnRealNameIng"},
    }
end

function PersonalCenterView:getProxyEvents()
    return {
        { module = XH.playerData, eventKeyName = "EVENT_PLAYER_DROP_CHANGED", callBack = "onDataChanged" },
        { module = XH.playerData, eventKeyName = "EVENT_PLAYER_TIME_DROPS_CHANGED", callBack = "onDataChanged" },
        { module = XH.playerData, eventKeyName = "EVENT_SR_CHANGED", callBack = "onDataChanged" },
        { module = XH.playerData, eventKeyName = "EVENT_PHONE_CHANGED", callBack = "onDataChanged" },
        { module = XH.playerData, eventKeyName = "EVENT_IDENTITY_CHANGED", callBack = "onDataChanged" },
        { module = XH.playerData, eventKeyName = "EVENT_BIND_USER_CHANGED", callBack = "onDataChanged" },
        { module = XH.login:getModule("Login"),eventKeyName = "EVENT_BIND_USER", callBack = "onBindUser" },
        { module = XH.lobby:getModule("PersonalCenter"),eventKeyName = "EVENT_PPCMGR_LOCATION_CHANGED", callBack = "onDataChanged" },
    }
end

-- 实名认证
function PersonalCenterView:onTouchEventRealName(send, eventType)
    XH.viewManager:openView("IdentityCheckView",nil,{isAutoBindPhone = true})
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_03) 
end

-- 复制按钮
function PersonalCenterView:onTouchEventCopyID(send, eventType)
    local strId = self._textID:getString()
    local copyStr = strId
    local stringArr = string.split(strId, ":")
    if stringArr and #stringArr == 2 then
        copyStr = stringArr[2]
    end
    XH.SysTool.copyString(copyStr)
    XH.TipTool.showTip({
        type = XH.TIP_LAYER_TYPE.OK,
    }, "COPY_SUCCESS")
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_02)   
end

-- 定位
function PersonalCenterView:onTouchEventGPS(send, eventType)

end

-- "+"号按钮事件处理
function PersonalCenterView:onTouchEventAdd(send, eventType)
    XH.viewManager:openView("ShopView", nil, {productType = send:getName()})
    local addname = send:getName()
    if addname == "fk" then
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_14) 
    end 
    if addname == "zs" then 
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_15) 
    end 
end

-- 关闭按钮
function PersonalCenterView:onTouchEventClose(send, eventType)
    self:close()
end

-- 点击背景
function PersonalCenterView:onTouchEventBGClose(send, eventType)
    self:close()
end

-- 切换选区
function PersonalCenterView:onTouchEventChangeArea(send, eventType)
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_13) 
    XH.playerData:stopVisitorHeart()
    XH.login:getModule("Login"):clearLoginStates()
    XH.loginData:setLobbyID(0)
    XH.Controller:getInstance():enterLogin()
end

--切换账号
function PersonalCenterView:onTouchEventChangeLoginType(send, eventType)
    XH.playerData:stopVisitorHeart()
    local isShowYouKe = false
    if XH.playerData:getPlayerIsAgent() and XH.areaData:getAgentShowYouKe() then
        isShowYouKe = true
    end
    XH.login:getModule("Login"):clearLoginStates()
    XH.Controller:getInstance():enterLogin(isShowYouKe)
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_12) 
end

function PersonalCenterView:onTouchEventPhoneBind(send, eventType)
    local isBindPhone = XH.playerData:isBindPhone()
    if not isBindPhone or XH.areaData:isSupportPhoneLogin() then
        XH.viewManager:openView("PhoneBindView")
    else
        XH.TipTool.showToast("手机已经绑定")
    end
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_09)  
end

function PersonalCenterView:onTouchEventThirdBind(send, eventType)
    XH.login:getModule("Login"):reqWXBind()
end

function PersonalCenterView:onTouchEventWeChatBind(send, eventType)
    XH.login:getModule("Login"):reqWXBind()
end

function PersonalCenterView:onTouchEventThirdUnbind(send, eventType)
    XH.lobby:getModule("PersonalCenter"):reqUnbindUser()
end

-- 设置昵称
function PersonalCenterView:setNickName(name)
    local res = XH.StringTool.cutStringByLength(name or DEFAULT_STRING)
    self._textNickname:setString(res)
end

-- 设置ID
function PersonalCenterView:setUserID(id)
    self._textID:setText("序号:" .. tostring(id) or DEFAULT_STRING)
end

-- 设置位置
function PersonalCenterView:setPlace(str)

end

-- 设置金币
function PersonalCenterView:setGoldNum(num)
    self._textGold:setString(tostring(num) or DEFAULT_STRING)
end

-- 设置房卡
function PersonalCenterView:setRoomCardNum(num)
    self._textRoomCard:setString(tostring(num) or DEFAULT_STRING)
end

-- 设置钻石
function PersonalCenterView:setDiamondNum(num)
    self._textDiamond:setString(tostring(num) or DEFAULT_STRING)
end

-- 设置头像
function PersonalCenterView:setUserHeadPic(picUrl, sex)
    local url = XH.playerData:getWeChatURL()
    self:updateImg(url)
end

function PersonalCenterView:updateImg(url)
    if not url then
        return
    end
    self._imgHead:setUrl(url)
end

-- 设置实名认证
function PersonalCenterView:setRealName(isConfirm)
    isConfirm = isConfirm or false

    if XH.configerManager:getIsKongZhi() then
        if isConfirm then
            self._btnRealName:setVisible(true)
            self._btnRealName:setEnabled(not isConfirm)
            self._btnReRealName:setVisible(false)
        else
            --获取伪认证状态
            local fakeIdentify = XH.lobby:getModule("PersonalCenter"):getFakeIdentify()
            if fakeIdentify then
                self._btnReRealName:setVisible(true)
                self._btnRealName:setVisible(false)
            else
                self._btnRealName:setVisible(true)
                self._btnRealName:setEnabled(true)
                self._btnReRealName:setVisible(false)
            end
        end
    else
        self._btnReRealName:setVisible(false)
        self._btnRealName:setEnabled(not isConfirm)
    end
    if XH.configerManager:getIsNewRealName() then
        if XH.playerData:getIdentityStatus() == 0 then
            self._btnRealName:setVisible(true)
            self._btnRealName:setEnabled(false)
            self._btnRealNameIng:setVisible(false)
        elseif XH.playerData:getIdentityStatus() == 1 then
            self._btnRealName:setVisible(false)
            self._btnRealNameIng:setVisible(true)
        elseif XH.playerData:getIdentityStatus() == 2 then
            self._btnRealName:setVisible(true)
            self._btnRealName:setEnabled(true)
            self._btnRealNameIng:setVisible(false)
        end
    end
end

function PersonalCenterView:updateData()
  --  self:setRealName(XH.playerData:isProtected())
    self:setNickName(XH.playerData:getNickName())
    self:setUserID(XH.playerData:getNumberID())
    self:setGoldNum(XH.playerData:getSR())
    self:setRoomCardNum(XH.playerData:getRoomCard())
    self:setDiamondNum(XH.playerData:getDiamnd())
    self:setUserHeadPic(XH.playerData:getHead(), XH.playerData:getSex())
    self:setPlace(XH.lobby:getModule("PersonalCenter"):getLocationInfo() or "")
    self:setFreeRoomCardNum(XH.playerData:getFreeRoomCard())
    local isBindPhone = XH.playerData:isBindPhone()
    if self._imgPhoneBindTag then
        self._imgPhoneBindTag:setVisible(isBindPhone)
    end
    --self._btnPhoneBind:setVisible(true)
    self._btnWeChatBind:setVisible(false)
   
    if XH.playerData:getIsRealNameTourist() then
        self._btnRealName:setVisible(false)
        self._btnZhuXiao:setVisible(false)
        self._btnPhoneBind:setVisible(false)
    end
end

-- 显示绑定的手机号
function PersonalCenterView:showPhoneTip()
    if XH.areaData:isSupportPhoneLogin() or XH.areaData:isSupportAutoPhoneBind() then
        local isBindPhone = XH.playerData:isBindPhone()
        self._textPhoneTip:setVisible(isBindPhone)
        self._textPhone:setVisible(isBindPhone)
        if isBindPhone then
            self._textPhone:setString(XH.playerData:getEncryptBindPhone())
        end
    else
        self._textPhoneTip:setVisible(false)
        self._textPhone:setVisible(false)
    end
end

-- 账户注销
function PersonalCenterView:onTouchEventZhuXiao(send, eventType)
    XH.viewManager:openView("AutoLogoutView") 
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_05)   
end

-- 用户权限
function PersonalCenterView:onTouchEventQuanXian(send, eventType)
    XH.viewManager:openView("CeLueCenterView")
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_07) 
end

-- 数据更新消息监听
function PersonalCenterView:onDataChanged()
    self:updateData()
end

function PersonalCenterView:onBindUser(event)
    self:close()
end

function PersonalCenterView:onTouchEventFreeRoomCard(send, eventType)
    local tipsNode = XH.UITool.seekNodeByName(send, "KW_PIC_TIPS_PAOPAO")
    if tipsNode then
        tipsNode:stopAllActions()
        local opacity = tipsNode:getOpacity()  
        if opacity == 255 then
            tipsNode:setOpacity(0)
        else
            tipsNode:runAction(cc.Sequence:create(cc.FadeIn:create(0.2), cc.DelayTime:create(3), cc.FadeOut:create(0.2)))
        end
    end
end

-- 设置免费房卡
function PersonalCenterView:setFreeRoomCardNum(num)
    if XH.areaData:isRemodelArea() then
        self._freeFangKaNum:setString(tostring(num) or DEFAULT_STRING)
    end
end

function PersonalCenterView:showRemodelUI()
    local isRemodelArea = XH.areaData:isRemodelArea()
    if  isRemodelArea then
        self._buyFangKaText:setText("购买房卡:")
        --self._buyFangKaText:setFontSize(32)
    else
        --位置调整
        self._buyFangKaIcon:setPositionY(self._freeFangKaIcon:getPositionY())
        self._buyFangKaText:setPositionY(self._freeFangKaText:getPositionY())
        self._textRoomCard:setPositionY(self._freeFangKaNum:getPositionY())
        self._btnBuy1:setPositionY(self._btnfreeFangKa:getPositionY())
    end
    self._freeFangKaIcon:setVisible(isRemodelArea)
    self._freeFangKaText:setVisible(isRemodelArea)
    self._freeFangKaNum:setVisible(isRemodelArea)
    self._btnfreeFangKa:setVisible(isRemodelArea)
end

-- 设置名称变为渠道号
function PersonalCenterView:setChannelWithId()
    local manifestPath = un.FileSystem.getWritePath() .. un.const.HotFixPath .. "harbor/Lobby/project.manifest"
    local localManifest = un.hotfix.Manifest.new(manifestPath)
    local curChannel = ""
    local curVersion = ""
    if localManifest:isLoaded() then
        curChannel = localManifest:getChannel()
        curVersion = localManifest:getVersion()
    end
    local secondCmdConfigs = nil
    pcall( function()
        secondCmdConfigs = require("local.Debug.Cmd.ReChannel.Config")

    end )
    if secondCmdConfigs then
        for index = 1, #secondCmdConfigs do
            local name = secondCmdConfigs[index].name
            if secondCmdConfigs[index].channel == curChannel then
                self._textChannel:setVisible(true)
                self._textChannel:setText("当前渠道：" .. name .. " 版本：" .. curVersion)
                break
            end
        end
    end
end

function PersonalCenterView:onTouchEventTitle()
    if self._handleClearCache == nil then
        self._handleClearCache = cc.Director:getInstance():getScheduler():scheduleScriptFunc(
        handler(self, self.removeHandleClearCache), 10, false)
        self._diamondClickCnt = 1
        pcall( function()
            XH.viewManager:openView("DebugUI")
        end )
        self:setChannelWithId()
    else
        self._diamondClickCnt = self._diamondClickCnt + 1
        if self._diamondClickCnt == 5 then
            -- XH.TipTool.showToast("已打开", 3)
            pcall( function()
                local checker = require("app.hotupdate.entry.EntryResChecker")
                checker._startHotFix()
            end )
        end
    end
end

function PersonalCenterView:removeHandleClearCache()
    if self._handleClearCache then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._handleClearCache)
        self._handleClearCache = nil
    end
end

function PersonalCenterView:onExit()
    PersonalCenterView.super.onExit()
    self:removeHandleClearCache()
end


return PersonalCenterView�D  