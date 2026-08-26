local InvateConfig = require("lobby.Modules.Invate.Config")
local InvateView = class("InvateView", XH.ViewBase)

local inputNumCount = 6
local INVATE_TYPE_CHECK = 1

function InvateView:getCSBPath()
    return "cocosStudio/hall/CSB/InvateLayer.csb"
end

function InvateView:getBindingInfo()
    return {
        ["_KW_UI_ROOT_LAYER"] = { varName = "_rootNode", onTouchEnded = "onTouchEventCloseBtn" },
        ["_KW_BTN_CLOSE"] = { varName = "_closeBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventCloseBtn" },
        ["_KW_BTN_CANCEL"] = { varName = "_cancelBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventCancelBtn" },
        ["_KW_BTN_SUBMIT"] = { varName = "_submitBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventSubmit" },
        ["_KW_BTN_OK"] = { varName = "_okBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventOK"},
        ["_KW_YAOQING_KUANG"] = { varName = "_invateFram", onTouchEnded = "onTouchEvenShowKeyBoard"},
        ["_KW_BIND_INVATE_CODE_LAYER"] = { varName = "_bindingLayer", onTouchEnded = "onTouchEvenKeyBoardtCancel"},
        ["_KW_KEYBOARD_SURE"] = { varName = "_sureBtn",type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventOK" },
        ["_KW_KEYBOARD_DEL"] = { varName = "_delBtn",type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventDeleteBtn" },

        ["_KW_TUIGUANGYUAN_INFO"] = {varName="_tuiguangyuanLayer"},
        ["_KW_CHECK_INVATE_CODE_LAYER"] = {varName="_checkLayer"},
        ["_KW_KEYBOARD_PANEL"] = {varName="_keyboardLayer"},

        ["_KW_UI_INVATE_REWARD_TEXT"] = {varName="_rewardText"},

        ["_KW_TEXT_CHECK_INVATE_CODE_TEXT"] = {varName="_checkTextTittle"},
        ["_KW_TEXT_CHECK_INVATE_CODE"] = {varName="_checkText"},
        ["_KW_UI_TIP_TEXT"] = {varName="_checkTipText"},

        ["_KW_TUIGUANGYUAN_INFO_TEXT"] = {varName="_tuiguangInfoText"},
        ["_KW_TUIGUANGYUAN_INFO_TEXT_TITTLE"] = {varName="_tuiguangTextTittle"},
        ["_KW_CHANGE_BIND"] = {varName="_changeBind",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventChangeBind"},
        ["_KW_CHANGE_RELIEVE"] = {varName="_changeRelieve",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventChangeRelieve"},

        ["_KW_INPUT_NUM_TEXT_"] = { beginIndex = 0, endIndex = 6, varName = "_inputText_"},
        ["_KW_KEYBOARD_"] = { beginIndex = 0, endIndex = 9, varName = "_keyBoardBtn_", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventNumBtn"},
    }
end

function InvateView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("Invate"), eventKeyName = "EVENT_INVATE_INFO_SUCCESS", callBack = "onGetInvateInfoBack" },
        { module = XH.lobby:getModule("Invate"), eventKeyName = "EVENT_CHECK_WEIXININFO", callBack = "onCheckWeiXinBack" },
        { module = XH.lobby:getModule("Invate"), eventKeyName = "EVENT_CHECK_INVATE_CODE", callBack = "onCheckInvateCodeBack" },
        { module = XH.lobby:getModule("Invate"), eventKeyName = "EVENT_CHANGE_BIND_STATE", callBack = "onChangeBindStateBack" },
        { module = XH.lobby:getModule("Invate"), eventKeyName = "EVENT_INVATE_UNBIND_SUCCESS", callBack = "onGetInvateUnBindBack" },
        { module = XH.lobby:getModule("Invate"), eventKeyName = "EVENT_MY_INVATE_CODE_SUCCESS", callBack = "onGetMyInvateCodeBack" },
    }
end

function InvateView:ctor() 
    InvateView.super.ctor(self)
    
    self._currentInputNumIndex = 1
    XH.lobby:getModule("Invate"):setCodeValue(0)
    self:checkInvateState()
    local lobbyID = XH.areaData:getLobbyID()
    if lobbyID ~= XH.LOBBY_ID.WENZHOU then
        self._changeBind:setVisible(false)
    end
    if lobbyID == XH.LOBBY_ID.SHAOXING3D then
        self._tuiguangTextTittle:setText("您已绑定邀请码:")
        self._checkTextTittle:setText("请确认您要绑定的邀请码为:")
        self._changeRelieve:setVisible(true)
        local isAgent = XH.playerData:getPlayerIsAgent()
        if isAgent then
            self._bindingLayer:setVisible(false)
            self._tuiguangyuanLayer:setVisible(true)
            self:setBindVisible(false)
            XH.lobby:getModule("Invate"):reqMyInviteCode()
        end
    end
    local needReqState = XH.userDefault:getValue(XH.userDefault.KEY_ID.NEED_REQ_CHANGE_BIND_RESULT,false) 
    if lobbyID == XH.LOBBY_ID.WENZHOU and needReqState then
        XH.lobby:getModule("Invate"):reqChangeBindState()
    end
end

function InvateView:setBindVisible(visible)
    if XH.areaData:getLobbyID() == XH.LOBBY_ID.WENZHOU then 
        self._changeBind:setVisible(visible)
    end
end

function InvateView:getInvateConfig()
    local lobbyid = XH.areaData:getLobbyID()
    if InvateConfig[lobbyid] then
        return  InvateConfig[lobbyid]
    end
    return InvateConfig["default"]
end

function InvateView:checkInvateState()
    local code = XH.playerData:getInvateCode()
    if code ~= 0 then
        if self._bindingLayer then
            self._bindingLayer:setVisible(false)
            XH.lobby:getModule("Invate"):reqCheckInvateCode(code,INVATE_TYPE_CHECK)
        end
    else
        if self._tuiguangyuanLayer then
            self._tuiguangyuanLayer:setVisible(false)
        end
        if self._bindingLayer then
            self._bindingLayer:setVisible(true)
        end
    end

    local invateCfg = self:getInvateConfig()

    self._rewardText:setString(invateCfg.KW_INVATE_REWARD_TIP)
end

function InvateView:onTouchEventCloseBtn(send, eventType)
    self:close()
end

function InvateView:resetInvateLayer()
    if self._bindingLayer then
        self._bindingLayer:setVisible(true)
    end

    if self._checkLayer then
        self._checkLayer:setVisible(false)
    end
    if self._keyboardLayer then
        self._keyboardLayer:setVisible(false)
    end 

    for i = 1,inputNumCount do
        self["_inputText_"..i]:setString("")
    end

    self._currentInputNumIndex = 1
    XH.lobby:getModule("Invate"):setCodeValue(0)
end

function InvateView:onTouchEventCancelBtn(send, eventType)
    if self._bindingLayer then
        self._bindingLayer:setVisible(true)
    end 
    if self._checkLayer then
        self._checkLayer:setVisible(false)
    end
end

function InvateView:onTouchEventOK(send, eventType)
    
    if self._keyboardLayer then
        self._keyboardLayer:setVisible(false)
    end 

    local code = ""
    if self._currentInputNumIndex > inputNumCount then
        for i = 1,inputNumCount do
            local numStr = self["_inputText_"..i]:getString()
            code = code .. numStr
        end
    end
    local nCode = tonumber(code,10)
    if nCode == nil or nCode > 1000000 then
        XH.TipTool.showTip({
			type = XH.TIP_LAYER_TYPE.OK,
		},"此邀请码不存在！")
        return 
    end
    
    XH.lobby:getModule("Invate"):reqCheckInvateCode(code)
end

function InvateView:onTouchEventSubmit(send, eventType)

    local code = ""
    if self._currentInputNumIndex > inputNumCount then
        for i = 1,inputNumCount do
            local numStr = self["_inputText_"..i]:getString()
            code = code .. numStr
        end
    end
    
    XH.lobby:getModule("Invate"):setCodeValue(tonumber(code))
    XH.lobby:getModule("Invate"):reqBind(code)
end

function InvateView:onTouchEvenKeyBoardtCancel(send, eventType)

    if self._keyboardLayer then
        self._keyboardLayer:setVisible(false)
    end 
end

function InvateView:onTouchEvenShowKeyBoard(send, eventType)
    if self._keyboardLayer then
        self._keyboardLayer:setVisible(true)
    end 
end

function InvateView:onTouchEventChangeBind(send, eventType)
    XH.viewManager:openView("InvateChangeView")
    self:close()
end

function InvateView:onTouchEventChangeRelieve(send, eventType)
    local agreeFunc = function()
        XH.lobby:getModule("Invate"):reqUnBind()
    end
    XH.TipTool.showTip({
		type = XH.TIP_LAYER_TYPE.OK_CANCEL,
        funcOK = agreeFunc
	}, "解除绑定需消耗50元宝，是否确定解绑？")
end

function InvateView:onTouchEventDeleteBtn(send, eventType)

    if self._currentInputNumIndex == 1 then
        return
    end

    self._currentInputNumIndex = self._currentInputNumIndex - 1
    local inputText = self["_inputText_"..self._currentInputNumIndex]
    if inputText then
        inputText:setString("")
    end
end

function InvateView:onTouchEventNumBtn(send, eventType)
    if not(self._currentInputNumIndex > 0 and self._currentInputNumIndex <= inputNumCount) then
        return
    end
    local bottonName = send:getName()
    local splitStr = string.split(bottonName, "_")
    local num = splitStr[#splitStr]

    local inputText = self["_inputText_"..self._currentInputNumIndex]
    if inputText then
        inputText:setString(num)
    end
    self._currentInputNumIndex = self._currentInputNumIndex + 1
end

function InvateView:onCheckWeiXinBack(event)
    local data = event.data
    if self._tuiguangyuanLayer then
        self:setBindVisible(false)
        self._tuiguangyuanLayer:setVisible(true)
    end
    if self._checkLayer then
        self._checkLayer:setVisible(false)
    end
    if self._bindingLayer then
        self._bindingLayer:setVisible(false)
    end
    local weixinString = XH.StringTool.getBackStringByFlag(data.nickname, '|')
    local nameString = XH.StringTool.getFrontStringByFlag(data.nickname, '|')
    local Text = self._tuiguangInfoText
    if Text then
        if weixinString == "" then
            Text:setString(data.nickname)
            if XH.areaData:getLobbyID() == XH.LOBBY_ID.SHAOXING3D then
                Text:setString(data.code)
            end
        else
            Text:setString(nameString)
        end
    end
    ccui.Helper:doLayout(Text)
end

function InvateView:onCheckInvateCodeBack(event)
    local data = event.data
    if self._bindingLayer then
        self._bindingLayer:setVisible(false)
    end

    if self._checkLayer then
        self._checkLayer:setVisible(true)
    end

    local checkText = self._checkText
    if checkText then
        local nameString = XH.StringTool.getFrontStringByFlag(data.nickname, '|')
        if nameString == "" then
            checkText:setString(data.nickname)
            if XH.areaData:getLobbyID() == XH.LOBBY_ID.SHAOXING3D then
                checkText:setString(data.code)
            end
        else
            checkText:setString(nameString)
        end
    end
    ccui.Helper:doLayout(checkText)
end

function InvateView:onChangeBindStateBack(event)
    local data = event.data
    self._tuiguangInfoText:setText(data.nickname)
    if XH.areaData:getLobbyID() == XH.LOBBY_ID.SHAOXING3D then
        self._tuiguangInfoText:setText(data.nickname)
    end
    XH.playerData:setInvateCode(data.code)
end

function InvateView:onGetInvateInfoBack(event)
    if XH.areaData:getLobbyID() == XH.LOBBY_ID.SHAOXING3D then
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, "绑定成功！")
    else
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, "获取房卡成功！")
    end
    local getCode = XH.lobby:getModule("Invate"):getCodeValue()
    XH.playerData:setInvateCode(getCode)
    XH.playerData:flushPlayerDrop()
    XH.playerData:setBindDSB(true)
    self:close()
end

function InvateView:onGetInvateUnBindBack(event)
    local data = event.data
    if data == 0 then
        XH.playerData:setInvateCode(0)
        XH.playerData:flushPlayerDrop()
        XH.playerData:setBindDSB(false)
        if self._tuiguangyuanLayer then
            self._tuiguangyuanLayer:setVisible(false)
        end
        if self._bindingLayer then
            self._bindingLayer:setVisible(true)
        end
    elseif data == 1060 then
        local agreeFunc = function()
            XH.viewManager:openView("ShopView")
        end
        XH.TipTool.showTip({
		    type = XH.TIP_LAYER_TYPE.OK,
            funcOK = agreeFunc
	    }, "元宝不足或请求异常，解绑失败！")
    end
end

function InvateView:onGetMyInvateCodeBack(event)
    local data = event.data
    if data then
        self._tuiguangInfoText:setText(data.selfCode)
        self._tuiguangTextTittle:setText("您的邀请码为:")
        self._changeRelieve:setVisible(false)
    end
end

return InvateView1  