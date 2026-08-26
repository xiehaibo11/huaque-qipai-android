local GivePropLayer = class("GivePropLayer", XH.ViewBase)
local PersonalCenterCinfig = require("lobby.Modules.PersonalCenter.Config")
local KW_QUICK_SET_NUMBER = PersonalCenterCinfig.KW_QUICK_SET_NUMBER[XH.areaData:getLobbyID()] or {}

function GivePropLayer:getCSBPath()
    return "cocosStudio/hall/CSB/PersonalCenter/PerCenterGivePropLayer.csb"
end

function GivePropLayer:getBindingInfo()
    return{
        ["_KW_TEXTFIRLD_INPUT_NUM"] = { varName = "_textFirldInputNum", type = XH.UI_TYPE.TEXTFIELD},
        ["_KW_TEXTFIRLD_INPUT_UID"] = { varName = "_textFirldInputUid", type = XH.UI_TYPE.TEXTFIELD },
        ["_KW_TEXTFIRLD_INPUT_PASSWARD"] = { varName = "_textFirldInputPassword", type = XH.UI_TYPE.TEXTFIELD, onTouchEnded = "onTouchEventInputPassword" },
        ["_KW_CHECKBOX_PASSWARD"] = { varName = "_checkBoxPassword", type = XH.UI_TYPE.CHECKBOX, onSelect = "onSelectNeedPasswd"},
        ["_KW_CHECKBOX_SURE_AGAIN"] = { varName = "_checkBoxSureAgain", type = XH.UI_TYPE.CHECKBOX, onSelect = "onSelectNeedSure" },
        ["_KW_TEXT_PROP_NUM"] = { varName = "_textPropNum" },
        ["_KW_BTN_CHECK_UID"] = { varName = "_btnCheckUid", onTouchEnded = "onTouchEventCheckUid", type = XH.UI_TYPE.IMAGE_TOBUTTON},
        ["_KW_TEXT_PLAYER_NAME"] = { varName = "_textPlayerName" },
        ["_KW_TEXT_PLAYER_NUMID"] = { varName = "_textPlayerNumid" },
        ["_KW_IMG_PLAYER_HEAD"] = { varName = "_imgPlayerHead", type = XH.UI_TYPE.REMOTEIMAGE},
        ["_KW_BTN_OK"] = { varName = "_btnGiveProp", onTouchEnded = "onTouchEventGiveProp", type = XH.UI_TYPE.IMAGE_TOBUTTON },
        ["_KW_PANEL_CHECKBOX_ITEM"] = { varName = "_checkBoxItem" },
        ["_KW_LISTVIEW_CHECKBOX"] = { varName = "_checkBoxList" },
        ["_KW_PLAYER_INFO_PANEL"] = { varName = "_panelPalyerInfo" },
        ["_KW_BG_NO_PASSWORD"] = { varName = "_bgNoPassword" },
    }
end

function GivePropLayer:getProxyEvents()
    return {
        { module = XH.playerData, eventKeyName = "EVENT_PLAYER_DROP_CHANGED", callBack = "flushTopInfo" },
        { module = XH.lobby:getModule("PersonalCenter"), eventKeyName = "EVENT_VIP_LAYER_CHECK_USERID", callBack = "flushPlayerInfo" },
        { module = XH.lobby:getModule("PersonalCenter"), eventKeyName = "EVENT_VIP_LAYER_SET_NEED_PASSWD", callBack = "eventNeedPasswd" },
        { module = XH.lobby:getModule("PersonalCenter"), eventKeyName = "EVENT_VIP_LAYER_SET_NEED_SURE", callBack = "eventNeedSure" },
        { module = XH.lobby:getModule("PersonalCenter"), eventKeyName = "EVENT_VIP_LAYER_PASSWD_STATE", callBack = "eventPasswdState" },
        { module = XH.lobby:getModule("PersonalCenter"), eventKeyName = "EVENT_VIP_LAYER_TRANS_CARD", callBack = "eventCleanInfo" },
    }
end


function GivePropLayer:ctor(data)
    GivePropLayer.super.ctor(self)    
    self:initUI(data)
end

function GivePropLayer:initUI(data)
    self:initQuickSetList()
    self:flushTopInfo()
    self._panelPalyerInfo:setVisible(false)
    self._textFirldInputUid:registerScriptEditBoxHandler(handler(self,self.inputUidEvent))
    self._textFirldInputNum:registerScriptEditBoxHandler(handler(self,self.inputPropNumEvent))
    XH.lobby:getModule("PersonalCenter"):reqVIPGetPasswdState()
    self._needPass = true
    self._needSure = true
    self._textFirldInputNum:setFontColor(cc.c3b(255, 250, 237))
    self._textFirldInputNum:setInputMode(cc.EDITBOX_INPUT_MODE_NUMERIC)
    self._textFirldInputUid:setFontColor(cc.c3b(255, 250, 237))
    self._textFirldInputUid:setInputMode(cc.EDITBOX_INPUT_MODE_NUMERIC)
    self:eventCleanInfo()
    if data and data.to_user_id then
        self._textFirldInputUid:setText(data.to_user_id)
        self:checkUserId()
    end
end

function GivePropLayer:flush(data)
    self:initUI(data)
end

function GivePropLayer:initQuickSetList()
    self._checkBoxList:removeAllChildren()
    self._checkBoxList:setScrollBarOpacity(0)
    for _, number in ipairs(KW_QUICK_SET_NUMBER) do
        local node = self._checkBoxItem:clone()
        node:setVisible(true)
        XH.UITool.setText(node, "_KW_TEXT_CHOOSE_NUM", number)
        XH.UITool.setTag(node, "_KW_CHECKBOX_NUM", number)
        XH.UITool.seekNodeByName(node, "_KW_CHECKBOX_NUM"):setSelected(false)
        XH.UITool.addEventListener(node, "_KW_CHECKBOX_NUM", handler(self, self.onTouchEventChooseNumCheckBox))
        if number > 9999 then
            local size = node:getContentSize()
            node:setContentSize(size.width + 50, size.height)
        end
        self._checkBoxList:addChild(node)
    end
    self._checkBoxItem:setVisible(false)
end

function GivePropLayer:flushTopInfo()
    self._textPropNum:setText( "购买房卡" .. XH.playerData:getRoomCard())
    self._propNum = tonumber(XH.playerData:getRoomCard())
end

function GivePropLayer:onTouchEventInputPassword(send, eventType)
    XH.viewManager:openView("InputPassWord", nil, handler(self,self.setPassword), "请输入密码")
end

function GivePropLayer:setPassword(password)
    self._textFirldInputPassword:setString(password)
end

function GivePropLayer:onTouchEventChooseNumCheckBox(send, eventType)
    if eventType == ccui.CheckBoxEventType.selected then
        local childern = self._checkBoxList:getChildren()
        for _, child in ipairs(childern) do
            local checkBox = XH.UITool.seekNodeByName(child, "_KW_CHECKBOX_NUM")
            if checkBox:getTag() == send:getTag() then
                self._textFirldInputNum:setText(tostring(send:getTag()))
            else
                checkBox:setSelected(false)
            end
        end
    end
end

function GivePropLayer:onTouchEventCheckUid(send, eventTouch)
    self:checkUserId()
end

function GivePropLayer:checkUserId()
    local uidText = self._textFirldInputUid:getText()
    if uidText == "" then
        XH.TipTool.showToast("请输入玩家序号", 2)
        return
    elseif not tonumber(uidText) then
        XH.TipTool.showToast("请输入正确的玩家序号", 2)
        return
    end
    XH.lobby:getModule("PersonalCenter"):reqCheckUserId(uidText)
end

function GivePropLayer:flushPlayerInfo(event)
    if event then
        self._textPlayerName:setString("昵称：" .. event.data.nNickname)
        self._textPlayerNumid:setString("序号：" .. event.data.nNumid)
        self._imgPlayerHead:setUrl(event.data.nHeadurl)
        self._panelPalyerInfo:setVisible(true)
        self._playerInfo = event.data
        if self._isGivingProp then
            self:onTouchEventGiveProp()
            self._isGivingProp = false
        end
    end
end

function GivePropLayer:inputUidEvent(eventType)
    --if eventType == "began" then
    --    -- triggered when an edit box gains focus after keyboard is shown
    --elseif eventType == "ended" then
    --    -- triggered when an edit box loses focus after keyboard is hidden.
    --else
    if eventType == "changed" then
        -- triggered when the edit box text was changed.
        self._panelPalyerInfo:setVisible(false)
        self._playerInfo = nil
    --elseif eventType == "return" then
        -- triggered when the return button was pressed or the outside area of keyboard was touched.
    end
end

function GivePropLayer:inputPropNumEvent(eventType)
    --if eventType == "began" then
    --    -- triggered when an edit box gains focus after keyboard is shown
    --elseif eventType == "ended" then
    --    -- triggered when an edit box loses focus after keyboard is hidden.
    --else
    if eventType == "changed" then
        -- triggered when the edit box text was changed.
        if self._propNum < (tonumber(self._textFirldInputNum:getText()) or 0) then
            XH.TipTool.showToast("请输入正确数值！", 2)
        end
    --elseif eventType == "return" then
        -- triggered when the return button was pressed or the outside area of keyboard was touched.
    end
end

function GivePropLayer:eventNeedPasswd(event)
    if event and event.data then
        self._needPass = not self._needPass
        if not self._needPass then
            XH.TipTool.showToast("取消成功", 2)
        end
    end
    self._bgNoPassword:setVisible(not self._needPass)
    self._checkBoxPassword:setSelected(self._needPass)
end

function GivePropLayer:eventNeedSure(event)
    if event and event.data then
        self._needSure = not self._needSure
    end
    self._checkBoxSureAgain:setSelected(not self._needSure)
end

function GivePropLayer:eventPasswdState(event)
    if event then
        self._checkBoxPassword:setSelected(event.data.need_passwd)
        self._checkBoxSureAgain:setSelected(not event.data.need_sure_again)
        self._needPass = event.data.need_passwd
        self._needSure = event.data.need_sure_again
        self._bgNoPassword:setVisible(not self._needPass)
    end
end

function GivePropLayer:onSelectNeedPasswd(send, eventType)
    if eventType == ccui.CheckBoxEventType.selected then
        XH.lobby:getModule("PersonalCenter"):reqVIPSetNeedPasswd(self._textFirldInputPassword:getString(), true)
        send:setSelected(false)
        XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.PERSONAL_CENTET_NEEDPWD_BTN_CLICK,{isSelect = true})
    elseif eventType == ccui.CheckBoxEventType.unselected then
        if self._textFirldInputPassword:getString() == "" then
            local fun = function(password)
                self:setPassword(password)
                if password ~= "" then
                    XH.lobby:getModule("PersonalCenter"):reqVIPSetNeedPasswd(self._textFirldInputPassword:getString(), false)
                end
            end
            XH.viewManager:openView("InputPassWord", nil, fun, "取消勾选也需要输入密码确认哦")
            send:setSelected(true)
        else
            XH.lobby:getModule("PersonalCenter"):reqVIPSetNeedPasswd(self._textFirldInputPassword:getString(), false)
            send:setSelected(true)
        end
        XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.PERSONAL_CENTET_NEEDPWD_BTN_CLICK,{isSelect = false})
    end
end

function GivePropLayer:onSelectNeedSure(send, eventType)
    if eventType == ccui.CheckBoxEventType.selected then
        XH.lobby:getModule("PersonalCenter"):reqVIPSetNeedSure(not self._needSure)
        send:setSelected(false)
        XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.PERSONAL_CENTET_NEEDSURE_BTN_CLICK,{isSelect = true})
    elseif eventType == ccui.CheckBoxEventType.unselected then
        XH.lobby:getModule("PersonalCenter"):reqVIPSetNeedSure(not self._needSure)
        send:setSelected(true)
        XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.PERSONAL_CENTET_NEEDSURE_BTN_CLICK,{isSelect = false})
    end
end

function GivePropLayer:onTouchEventGiveProp(send, eventType)
    if self._textFirldInputNum:getText() == "" then
        XH.TipTool.showToast("请输入赠送数量", 2)
        return
    end
    if tonumber(self._textFirldInputNum:getText()) == 0 or self._propNum < (tonumber(self._textFirldInputNum:getText()) or 0) then
        XH.TipTool.showToast("请输入正确赠送数量", 2)
        return
    end
    if self._textFirldInputUid:getText() == "" then
        XH.TipTool.showToast("请输入正确序号", 2)
        return
    end
    if tonumber(self._textFirldInputUid:getText()) == XH.playerData:getNumberID() then
        XH.TipTool.showToast("不能给自己赠送房卡哦~", 2)
        return
    end
    if not self._playerInfo then
        --XH.TipTool.showToast("请验证一名玩家信息", 2)
        self._isGivingProp = true
        self:checkUserId()
        return
    end
    if self._needPass and self._textFirldInputPassword:getString() == "" then
        XH.TipTool.showToast("请输入密码确认", 2)
        return
    end
    if self._needSure then
        self:showAgainSureTipLayer()
    else
        if self._isGeting then
            XH.TipTool.showToast( "操作频繁", 2)
            return 
        end
        self._isGeting = true
        XH.lobby:getModule("PersonalCenter"):reqVIPTransCard(self._playerInfo.nNumid, self._textFirldInputNum:getText(), self._textFirldInputPassword:getString())
        XH.SysTool.performWithDelayGlobal(function ()
            self._isGeting = false
        end, 0.5)
    end
end

function GivePropLayer:showAgainSureTipLayer()
    local showData = {}
    showData.richText = "<font face='cocosStudio/Common/Font/fangzhengcuyuan.TTF' size='44' color='#CD8551'>"
    .. "是否向<font color='#17A199'>" ..self._playerInfo.nNickname .. "</font><font color='#CD8551'>赠送</font><font color='#17A199'>"
    .. self._textFirldInputNum:getText()  .."张房卡</font><font color='#CD8551'>？</font></font>"
    showData.node = self._panelPalyerInfo
    showData.showBtn = "all"
    local callBack = function()
        XH.lobby:getModule("PersonalCenter"):reqVIPTransCard(self._playerInfo.nNumid, self._textFirldInputNum:getText(), self._textFirldInputPassword:getString())
    end
    showData.okFun = callBack
    XH.viewManager:openView("PersonalCenterTipLayer", nil, showData)
end

function GivePropLayer:eventCleanInfo(event)
    self._isGeting = false
    self._textFirldInputNum:setText("")
    self._textFirldInputUid:setText("")
    self._textFirldInputPassword:setString("")
    self._panelPalyerInfo:setVisible(false)
    self._playerInfo = nil
end

return GivePropLayer {5  