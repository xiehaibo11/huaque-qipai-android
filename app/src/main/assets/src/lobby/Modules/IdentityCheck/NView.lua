local IdentityCheckNView = class("IdentityCheckNView", XH.ViewBase)

IdentityCheckNView.IS_SHOWING = false
local KW_SPECIAL_CODE = 123698741

function IdentityCheckNView:getCSBPath()
    if device.platform == "windows" or (device.platform == "android" and XH.SysTool:CompareToBundleVersion("1.2.5")) then
        return "cocosStudio/hall/CSB/window/RealNameLayerAND.csb"
    end
    return "cocosStudio/hall/CSB/window/RealNameLayerIOS.csb"
end

function IdentityCheckNView:getBindingInfo()
    return {
        ["_KW_ROOT_PANEL"] = {varName = "_rootPanel"}, -- 不要删，给子类HealthSystemView用的
        ["_KW_PRO_1"] = {varName = "_pro1"},
        ["_KW_PRO_2"] = {varName = "_pro2"},
        ["_KW_PRO_3"] = {varName = "_pro3"},
        ["_KW_PRO_4"] = {varName = "_pro4"},
        ["_KW_TXT_1"] = {varName = "_txt1"},
        ["_KW_TXT_2"] = {varName = "_txt2"},
        ["_KW_TXT_3"] = {varName = "_txt3"},
        ["_KW_TXT_4"] = {varName = "_txt4"},
        ["_KW_ITEM_1"] = {varName = "_item1"},
        ["_KW_ITEM_2"] = {varName = "_item2"},
        ["_KW_ITEM_3"] = {varName = "_item3"},
        ["_KW_ITEM_4"] = {varName = "_item4"},
        ["_KW_IMG_ADD"] = {varName = "_imgAdd"},
        ["_KW_TXT_AWARD"] = {varName = "_txtAward"},
        ["_KW_BTN_KEFU"] = {varName = "_btnKefu", type = XH.UI_TYPE.IMAGE, onTouchEnded = "onTouchEventKefu"},
        ["_KW_REALNAME_CLOSE"] = {varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose"},
        ["_KW_TEXT_USER_ID"] = {varName = "_textUserID"},
        ["_KW_REALNAME_PANEL"] = {varName = "_realNamePanel"},
        ["_KW_REALNAME_NAME_REAL"] = {varName = "_tittlenamereal"},
        ["_KW_REALNAME_NUMBER_REAL"] = {varName = "_tittlenumberreal"},
        ["_KW_REALNAME_NUMBER_REAL_NO"] = {varName = "_tittleNonumberreal"},
        ["_KW_INPUT_NUMBER_BG"] = {varName = "_inputnumberbg"},
        ["_KW_INPUT_NAME"] = {varName = "_inputName", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventInputName"},
        ["_KW_INPUT_NUMBER"] = {varName = "_inputNumber", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventInputNumber"},
        ["_KW_BTN_FINISH_2"] = {varName = "_btnFinish", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventFinsh"},
        ["_KW_CHECKBOX_AUTOBIND"] = {varName = "_checkBoxAutoBind", type = XH.UI_TYPE.CHECKBOX, onSelect = "onTouchEventCheckAutoBind"},
        ["_KW_ZFUB_REAL_BTN"] = {varName = "_zFuBRealBtn", type = XH.UI_TYPE.IMAGE, onTouchEnded = "onTouchEventChooseZF"},
        ["_KW_IMG_CHANGEUSER"] = {varName = "_btnChangeArea", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventChangeUser"}
    }
end

function IdentityCheckNView:getProxyEvents()
    return { --
        {module = XH.lobby:getModule("IdentityCheck"), eventKeyName = "EVNET_REAL_NAME_BACK", callBack = "onRealNameRefresh"},
        {module = XH.lobby:getModule("IdentityCheck"), eventKeyName = "EVNET_REAL_NAME_BACK_ING", callBack = "onRealNameRefreshing"},
        {module = XH.lobby:getModule("PersonalCenter"), eventKeyName = "EVENT_FAKE_BIND_CHANGED", callBack = "onFakeBindChanged"},
        {module = XH.lobby:getModule("Lobby"), eventKeyName = "EVENT_GET_IS_FOREIGN_IP", callBack = "onGetIsForeignIpChanged"},
        {module = XH.sdkManager, eventKeyName = "EVENT_ZFB_REALNAME_CALLBACK", callBack = "onZFBRealName"},
        {module = XH.lobby:getModule("IdentityCheck"), eventKeyName = "EVENT_REALNAME_AWARD_INFO", callBack = "onGetPrizeInfo"}
    }
end

function IdentityCheckNView:ctor(param)
    param = param or {}
    IdentityCheckNView.super.ctor(self, param)
    XH.lobby:getModule("IdentityCheck"):ReqAwardInfo()
    local isAutoBindUI = param.isAutoBindPhone or false
    self.isRealnameIng = param.isRealnameIng or false
    self.compulsoryRealName = param.compulsoryRealName or false
    self:initLayerNode(isAutoBindUI, self.isRealnameIng)
    if self.compulsoryRealName then
        self._btnClose:setVisible(true)
    end
    self:setNodeEventEnabled(true)
    XH.lobby:getModule("Notify"):reqNotifyInfoForActivity()
end

function IdentityCheckNView:onGetPrizeInfo(event)
    local awards = event.data
    self._imgAdd:setVisible(#awards > 1)
    self._item1:setVisible(#awards >= 2)
    self._item2:setVisible(#awards >= 3)
    self._item3:setVisible(#awards >= 4)
    self._item4:setVisible(#awards >= 5)
    for i = 1, #awards do
        if i == 1 then
            self._txtAward:setText(awards[i].propCount .. awards[i].propName)
        else
            local index = i - 1
            if self["_item" .. index] and self["_pro" .. index] then
                if awards[i].propImgUrl ~= "" then
                    local size = self._pro1:getContentSize()
                    local headImage = XH.RemoteImage.new():setPosition(self["_pro" .. index]:getContentSize().width / 2, self["_pro" .. index]:getContentSize().height / 2):ignoreContentAdaptWithSize(false):setContentSize(size.width, size.height):addTo(self["_pro" .. index])
                    headImage:setUrl(awards[i].propImgUrl or "")
                end
            end
            if self["_txt" .. index] then
                self["_txt" .. index]:setText("x" .. awards[i].propCount)
            end
        end
    end
end

function IdentityCheckNView:setNodeEventEnabled(enable)
    if enable then
        self:registerScriptHandler(function(state)
            if state == "enter" and self.onEnter then
                self:onEnter()
            elseif state == "exit" and self.onExit then
                self:onExit()
            elseif state == "enterTransitionFinish" and self.onEnterTransitionFinish then
                self:onEnterTransitionFinish()
            elseif state == "exitTransitionStart" and self.onExitTransitionStart then
                self:onExitTransitionStart()
            elseif state == "cleanup" and self.onCleanup then
                self:onCleanup()
            end
        end)
    end
end

function IdentityCheckNView:checkSpecialCode(strName)
    if tonumber(strName) == KW_SPECIAL_CODE then
        return true
    else
        return false
    end
end

function IdentityCheckNView:eventFinsh(strName, strIDNum)
    local isNewUser = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEW_USER_GUIDE_FINISHED)
    if isNewUser == false then
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.NEWPLAYER_GUIDE_2)
    end

    -- 提审模式下的特殊编码
    if self:checkSpecialCode(strName) then
        local isAutoBindPhone = false
        if self._checkBoxAutoBind and self._checkBoxAutoBind:isSelected() then
            isAutoBindPhone = true
        end
        XH.lobby:getModule("IdentityCheck"):SpecialCodeBind(isAutoBindPhone)
        return
    end

    if strName and strName ~= "" then
        strName = XH.StringTool.trim(strName)
        strName = string.gsub(strName, "[.]", "·") -- 争对少数民族名字的处理
    end
    if strIDNum and strIDNum ~= "" then
        strIDNum = XH.StringTool.trim(strIDNum)
        strIDNum = string.gsub(strIDNum, "x", "X")
    end
    if strIDNum == "" then
        self._tittlenumberreal:setVisible(true)
    end
    if strName == "" then
        self._tittlenamereal:setVisible(true)
    end
    if XH.areaData:getLobbyID() == XH.LOBBY_ID.JINHUA then -- 金华的特判
        if self._checkBoxAutoBind and not self._checkBoxAutoBind:isSelected() then
            self._newRealiphone:setVisible(true)
            return
        end
    end
    if strName == "" or strIDNum == "" then
        return
    end
    -- 控制器
    if XH.configerManager:getIsKongZhi() then
        XH.lobby:getModule("PersonalCenter"):reqBindID(strName)
        return
    end

    if not self:checkNameLegality(strName) then
        XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK, bTop = true}, XH.ConstString.getStr("ERROR_NAME"))
        return
    end

    local strIdNumberLen = string.len(strIDNum)
    if strIdNumberLen ~= 18 then
        self._tittleNonumberreal:setVisible(true)
        return
    end

    local result, tips = self:checkIdNumberLegality(strIDNum)
    if not result then
        if tips then
            XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK, bTop = true}, tips)
        else
            XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK, bTop = true}, XH.ConstString.getStr("ERROR_IDENTITY"))
        end
        return
    end
    if strName and strIDNum then
        self:onDefendRequest(strName, strIDNum)
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_04)
    end
end

function IdentityCheckNView:checkNameLegality(name)
    local len = string.len(name)
    local index = string.find(name, "·")
    if name == nil then
        return false
        -- 争对少数名族·在最前面和最后面的处理
    elseif index and index == len - 1 or index == 1 then
        return false
    elseif XH.StringTool.isSpecialCharsExist(name) == true then
        return false
    end
    return true
end

function IdentityCheckNView:checkIdNumberLegality(strIdNumber)
    if strIdNumber == nil then
        return false
    end

    local firstNumber = tonumber(string.sub(strIdNumber, 1, 1))
    if firstNumber ~= nil then
        if firstNumber == 0 then
            return false
        end
    end

    if XH.playerData:getActiveValue() >= 2 or XH.configerManager:getIsNewRealName() then -- 实名身份证号年月日判断
        local yearNumber = tonumber(string.sub(strIdNumber, 7, 10))
        local mouthNumber = tonumber(string.sub(strIdNumber, 11, 12))
        local dayNumber = tonumber(string.sub(strIdNumber, 13, 14))

        if yearNumber ~= nil and mouthNumber ~= nil and dayNumber ~= nil then
            -- 年月日范围判断
            if yearNumber <= 1850 then
                return false
            end
            if mouthNumber <= 0 or mouthNumber >= 13 then
                return false
            end
            if dayNumber <= 0 then
                return false
            else
                -- 大小月2月日期判断
                local bigMouth = {[1] = 1, [3] = 1, [5] = 1, [7] = 1, [8] = 1, [10] = 1, [12] = 1} -- 大月月份
                if bigMouth[mouthNumber] == 1 then
                    if dayNumber > 31 then
                        return false
                    end
                elseif mouthNumber == 2 then -- 2月日期判断
                    if (math.fmod(yearNumber, 4) == 0 and math.fmod(yearNumber, 100) ~= 0) or (math.fmod(yearNumber, 400) == 0) then
                        if dayNumber > 29 then
                            return false
                        end
                    else
                        if dayNumber > 28 then
                            return false
                        end
                    end
                elseif dayNumber > 30 then
                    return false
                end
            end
        end
        if yearNumber then
            local curYear = os.date("%Y")
            local age = curYear - yearNumber
            if age < 18 then -- 判断是否成年
                self._isAdult = false
            else
                self._isAdult = true
            end
        end

        if XH.playerData:getActiveValue() and XH.playerData:getActiveValue() >= 3 then
            if not self._isAdult then
                local tips = "您填写的信息显示未满18周岁,继续认证可能导致您无法进入游戏,建议核对您的身份信息是否正确！"
                return false, tips
            end
        end
        if XH.configerManager:getIsNewRealName() then
            if not self._isAdult then
                local tips = "您填写的信息显示未满18周岁,继续认证可能导致您无法进入游戏,建议核对您的身份信息是否正确！"
                return false, tips
            end
        end
    end
    return true
end

function IdentityCheckNView:onDefendRequest(strName, strIDNum)
    if strName == nil then
        return
    end
    if strIDNum == nil then
        return
    end
    local isAutoBindPhone = false
    if self._checkBoxAutoBind and self._checkBoxAutoBind:isSelected() then
        isAutoBindPhone = true
    end
    XH.lobby:getModule("IdentityCheck"):reqDefendIdentity(strName, strIDNum, isAutoBindPhone)
end

function IdentityCheckNView:onZFBRealName(event)
    if event and event.data then
        local code = event.data.code
        local msg = event.data.msg
        if code == XH.ThirdDefine.ZFB_REALNAME_CALLBACK_CODE.REALNAME_SUCCESS then
            local data = cjson.decode(msg)
            if data.ext then
                local ext = cjson.decode(data.ext)
                local realname = ext.realName
                local certNo = ext.certNo
                self:eventFinsh(realname, certNo)
            end
        elseif code == XH.ThirdDefine.ZFB_REALNAME_CALLBACK_CODE.REALNAME_FAIL then
            self._realNamePanel:setVisible(true)
        end
    end
end

function IdentityCheckNView:onGetIsForeignIpChanged(event)
    if XH.configerManager:getIsKongZhi() then
        local nConfigVale = XH.playerData:getConfigVale()
        if nConfigVale then
            local nRealNameAuthConfig = bit:d2b(nConfigVale)
            local foreignNum = nRealNameAuthConfig[63]
            local isForeignIp = XH.lobby:getModule("Lobby"):getIsForeignIP()
            if foreignNum == 0 and isForeignIp then
                self:close()
            end
        end
    end
end

function IdentityCheckNView:onFakeBindChanged(event)
    if XH.configerManager:getIsKongZhi() then
        -- 获取伪认证状态
        local fakeIdentify = XH.lobby:getModule("PersonalCenter"):getFakeIdentify()
        if fakeIdentify then
            self:close()
        end
    end
end

function IdentityCheckNView:onRealNameRefresh(event)
    self:close()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.REALNAME_SUCCESS, nil, {user_ip = un.Device.getIp()})
    -- XH.TipTool.showTip({
    --     type = XH.TIP_LAYER_TYPE.OK,
    --     bTop = true,
    --     funcOK = function()
    if event.msg and event.msg.isAutoBindPhone then
        XH.login:getModule("Login"):reqGYInfo(true)
    end
    --     end
    -- }, XH.ConstString.getStr("IDENTITY_SUCCESS"))
end

function IdentityCheckNView:onRealNameRefreshing(event)
    self:close()
    -- XH.viewManager:openView("IdentityCheckView", nil, {isRealnameIng = true})
end

function IdentityCheckNView:initUserID()
    self._textUserID:setText("亲爱的玩家:" .. XH.playerData:getNumberID())
    self._textUserID:setVisible(true)
end

function IdentityCheckNView:initLayerNode(isAutoBindUI, isRealnameIng)
    self:initUserID()
    self._inputName:setInputMode(cc.EDITBOX_INPUT_MODE_ANY)
    self._inputName:setFontSize(32)
    self._inputName:setPlaceholderFontColor(cc.c3b(174, 137, 93))
    self._inputName:setFontColor(cc.c3b(32, 67, 156))
    self._inputName:registerScriptEditBoxHandler(function(eventName)
        if eventName == "began" or eventName == "changed" then
            return
        end
        if eventName == "ended" or eventName == "return" then
            local strName = self._inputName:getText()
            if strName then
                strName = string.gsub(strName, "[.]", "·") -- 争对少数民族名字的处理
                self._inputName:setText(strName)
            end
        end
    end)
    self._inputNumber:setInputMode(cc.EDITBOX_INPUT_MODE_ANY)
    self._inputNumber:setFontSize(32)
    self._inputNumber:setPlaceholderFontColor(cc.c3b(174, 137, 93))
    self._inputNumber:setFontColor(cc.c3b(32, 67, 156))
    self._inputNumber:registerScriptEditBoxHandler(function(eventName)
        if eventName == "began" or eventName == "changed" then
            return
        end
        if eventName == "ended" or eventName == "return" then
            local strIDNum = self._inputNumber:getText()
            if strIDNum then
                strIDNum = string.gsub(strIDNum, "x", "X")
                self._inputNumber:setText(strIDNum)
            end
        end
    end)

    -- 新增切换地区按钮
    if XH.playerData:getPassiveValue() == 2 and not XH.lobby:getModule("PersonalCenter"):getFakeIdentify() then
        self._btnClose:setVisible(false)
    elseif XH.playerData:isProtectedLevel() then
        self._btnClose:setVisible(false)
    end

    if isRealnameIng then
        self:close()
    end
    -- 双扣CPS包默认不勾选绑定手机号
    if XH.ChannelTool.checkIsDoubleKouGame() and self._checkBoxAutoBind then
        self._checkBoxAutoBind:setSelected(false)
    end
end

function IdentityCheckNView:onTouchEventClose(send, eventType)
    -- 判断是否是新注册用户
    local isNewUser = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEW_USER_GUIDE_FINISHED)
    if isNewUser == false then
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.NEWPLAYER_GUIDE_1)
    end
    if self.isRealnameIng then
        cc.UserDefault:getInstance():setBoolForKey(string.format("KW_REALNAME_CHECK_ING_%s", XH.playerData:getNumberID()), true)
    end
    if self.compulsoryRealName then
        XH.TipTool.showTip({type = XH.TIP_LAYER_TYPE.OK, bTop = true}, "完成实名认证才能游戏和充值哦，\n\n请尽快完成。")
    end
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_IDENTITY_CHECKVIEW_CLOSE)
    self:close()
end

function IdentityCheckNView:onTouchEventInputName(send, eventType)
    self._tittlenamereal:setVisible(false)
end

function IdentityCheckNView:onTouchEventInputNumber(send, eventType)
    self._tittlenumberreal:setVisible(false)
    self._tittleNonumberreal:setVisible(false)
end

function IdentityCheckNView:onTouchEventFinsh(send, eventType)
    local strName = self._inputName:getText()
    local strIDNum = self._inputNumber:getText()
    self:eventFinsh(strName, strIDNum)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.REALNAME_FINISHED, nil, {user_ip = un.Device.getIp()})
end

function IdentityCheckNView:onTouchEventCheckAutoBind(send, eventType)
    local autoBind = self._checkBoxAutoBind:isSelected()
    local conventionData = {}
    conventionData.label = autoBind == true and "勾选一键绑定" or "不勾选一键绑定"
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.REALNAME_BINDPHONE, conventionData, {user_ip = un.Device.getIp()})
end

function IdentityCheckNView:onTouchEventChooseZF(send, eventType)
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.TABS_REAL_NAME_ZFUB_SDK)
    XH.sdkManager:callFunction("al_authinfo")
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.REALNAME_ZFUB, nil, {user_ip = un.Device.getIp()})
end

function IdentityCheckNView:onTouchEventKefu()
    XH.lobby:getModule("CusService"):oponCusServerView(nil, "GAMELOBBY")
end

function IdentityCheckNView:onTouchEventChangeUser(send, eventType)
    self:close()
    XH.lobby:getModule("IdentityCheck"):changeUser()
end

return IdentityCheckNView
   L  