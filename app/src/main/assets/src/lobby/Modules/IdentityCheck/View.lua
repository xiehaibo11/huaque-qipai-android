local IdentityCheckView = class("IdentityCheckView",XH.ViewBase)

IdentityCheckView.IS_SHOWING = false
local KW_SPECIAL_CODE = 123698741

function IdentityCheckView:getCSBPath()
    if device.platform == "windows" or (device.platform == "android" and XH.SysTool:CompareToBundleVersion("1.2.5") )then
        return "cocosStudio/hall/CSB/window/RealNameZFuBLayer.csb"
    end
    return "cocosStudio/hall/CSB/window/RealNameLayer.csb"
end

function IdentityCheckView:getBindingInfo()
    return {
        ["_KW_REALNAME_CLOSE"] = {varName="_btnClose",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventClose"},
        ["_KW_BTN_NEXT"] = {varName="_btnNext",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventNext"},
        ["_KW_BTN_FINISH_"] = {beginIndex = 1, endIndex = 2,varName="_btnFinish",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventFinsh"},
        ["_KW_INPUT_NAME"] = {varName="_inputName",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventInputName"},
        ["_KW_INPUT_NUMBER"] = {varName="_inputNumber",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventInputNumber"},
        -- ["_KW_IMG_CHANGEAREA"] = {varName="_btnChangeArea",type = XH.UI_TYPE.IMAGE_TOBUTTON,onTouchEnded = "onTouchEventChangeArea"},
        ["_KW_IMG_CHANGEUSER"] = {varName="_btnChangeArea",type = XH.UI_TYPE.IMAGE_TOBUTTON,onTouchEnded = "onTouchEventChangeUser"},
        -- ["_KW_TEXT_AREA"] = {varName="_textAreaName"},
        ["_KW_TEXT_USER_ID"] = {varName="_textUserID"},
        ["_KW_IMG_UNCLOSE_BG"] = {varName="_unCloseBindBg"},
        ["_KW_ORIGINAL_PANEL"] = {varName = "_originalPanel"},
        ["_KW_NEW_BIND_PANEL"] = {varName = "_newBindPanel"},
        ["_KW_CHECKBOX_AUTOBIND"] = {varName = "_checkBoxAutoBind",type = XH.UI_TYPE.CHECKBOX,onSelect = "onTouchEventCheckAutoBind"},
        ["_KW_REALNAME_NAME_REAL"] = {varName="_tittlenamereal"},
        ["_KW_REALNAME_NUMBER_REAL"] = {varName="_tittlenumberreal"},
        ["_KW_REALNAME_NUMBER_REAL_NO"] = {varName="_tittleNonumberreal"},
        ["_KW_INPUT_NAME_BG"] = {varName="_inputnamebg"},
        ["_KW_INPUT_NUMBER_BG"] = {varName="_inputnumberbg"},
        ["_KW_NEW_REALNAME_ING"] = {varName="_newRealNameing"},
        ["_KW_REALNAME_IPHONE_REAL"] = {varName="_newRealiphone"},
        ["_KW_BTN_RN_TOURIST_LOGIN"] = {varName = "_btnRealNameV",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventVis"},
        ["_KW_REALNAME_KNOW"] = {varName = "_btnRealKnow",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventClose"},
        ["_KW_BTN_RETURN_"] = {beginIndex = 1, endIndex = 2, varName="_returnBtn",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventReturn"},
        ["_KW_BTN_SD"] = {varName="_dsBtn",type = XH.UI_TYPE.IMAGE,onTouchEnded = "onTouchEventChooseSD"},
        ["_KW_IMG_SD"] = {varName="_sdChooseTip"},
        ["_KW_BTN_ZF"] = {varName="_zfBtn",type = XH.UI_TYPE.IMAGE,onTouchEnded = "onTouchEventChooseZF"},
        ["_KW_IMG_ZF"] = {varName="_zfChooseTip"},
        ["_KW_CHOOSE_PANEL"] = {varName="_choosePanel"},
        ["_KW_REALNAME_PANEL"] = {varName="_realNamePanel"},
        ["_KW_ZFUB_PANEL"] = {varName="_zFuBPanel"},
        ["_KW_ZFUB_REAL_BTN"] = {varName="_zFuBRealBtn",type = XH.UI_TYPE.IMAGE,onTouchEnded = "onTouchEventChooseZF"},
        ["_KW_ROOT_PANEL"] = {varName="_rootPanel"},-- 不要删，给子类HealthSystemView用的
    }
end

function IdentityCheckView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("IdentityCheck"),eventKeyName = "EVNET_REAL_NAME_BACK", callBack = "onRealNameRefresh" },
        { module = XH.lobby:getModule("IdentityCheck"),eventKeyName = "EVNET_REAL_NAME_BACK_ING", callBack = "onRealNameRefreshing" },
        -- { module = XH.lobby:getModule("IdentityCheck"),eventKeyName = "EVNET_REQ_SYS_TIME_CALLBACK", callBack = "onSysTimeCallback" },
        { module = XH.lobby:getModule("PersonalCenter"),eventKeyName = "EVENT_FAKE_BIND_CHANGED", callBack = "onFakeBindChanged" },
        { module = XH.lobby:getModule("Lobby"),eventKeyName = "EVENT_GET_IS_FOREIGN_IP", callBack = "onGetIsForeignIpChanged" },
        { module = XH.lobby:getModule("IdentityCheck"),eventKeyName = "EVENT_REALNAME_TIMEOVER", callBack = "onRealNameTimeOver" },
        { module = XH.lobby:getModule("IdentityCheck"),eventKeyName = "EVENT_REALNAME_TIMEOVER_LIMIT", callBack = "onRealNameTimeOverLimit" },
        { module = XH.sdkManager,eventKeyName = "EVENT_ZFB_REALNAME_CALLBACK", callBack = "onZFBRealName" },  
    }
end

function IdentityCheckView:ctor(param)
    param = param or {}
    IdentityCheckView.super.ctor(self,param)
    local isAutoBindUI = param.isAutoBindPhone or false
    self.isRealnameIng = param.isRealnameIng or false
    self.compulsoryRealName = param.compulsoryRealName or false
    self:initLayerNode(isAutoBindUI,self.isRealnameIng)
    if self.compulsoryRealName then
        self:updateUI()
    end
    self:setNodeEventEnabled(true)
    XH.lobby:getModule("Notify"):reqNotifyInfoForActivity()

    --if device.platform == "windows" or (device.platform == "android" and XH.SysTool:CompareToBundleVersion("1.2.5") )then
    --    self._choosePanel:setVisible(true)
    --    self._realNamePanel:setVisible(false)
    --    self._btnFinish1:setPositionX(-1 * self._returnBtn1:getPositionX())
    --    self._returnBtn1:setVisible(true)
    --    self._btnFinish2:setPositionX(-1 * self._returnBtn2:getPositionX())
    --    self._returnBtn2:setVisible(true)
    --else
    --    self._choosePanel:setVisible(false)
    --    self._realNamePanel:setVisible(true)
    --end
end

function IdentityCheckView:onTouchEventChooseSD(send, eventType)
    self._choosePanel:setVisible(false)
    self._realNamePanel:setVisible(true)
end

function IdentityCheckView:onTouchEventChooseZF(send, eventType)
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.TABS_REAL_NAME_ZFUB_SDK)
    XH.sdkManager:callFunction("al_authinfo")
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.REALNAME_ZFUB, nil, {user_ip=un.Device.getIp()})
end

function IdentityCheckView:onTouchEventReturn(send, eventType)
    self._choosePanel:setVisible(true)
    self._realNamePanel:setVisible(false)
end

function IdentityCheckView:onZFBRealName(event)
    if event and event.data then
        local code = event.data.code
        local msg = event.data.msg
        if code == XH.ThirdDefine.ZFB_REALNAME_CALLBACK_CODE.REALNAME_SUCCESS then
            local data = cjson.decode(msg)
            if data.ext then
                local ext = cjson.decode(data.ext)
                local realname = ext.realName
                local certNo = ext.certNo
                --XH.TipTool.showToast(" realname :" .. tostring(realname)  .. " certNo :" .. tostring(certNo))
                self:eventFinsh(realname, certNo)
            end
        elseif code == XH.ThirdDefine.ZFB_REALNAME_CALLBACK_CODE.REALNAME_FAIL then
            self._choosePanel:setVisible(false)
            self._realNamePanel:setVisible(true)
        end
    end
end

function IdentityCheckView:updateUI()
    if self._btnNext and self._btnFinish then 
        self._btnNext:setVisible(true)
        self._btnFinish:setPositionY(self._btnFinish:getPositionY())
        self._btnFinish:setPositionX(self._btnFinish:getPositionX() + (self._btnFinish:getPositionX() - self._btnNext:getPositionX()))
    end
end

function IdentityCheckView:setNodeEventEnabled(enable)
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

function IdentityCheckView:onFakeBindChanged(event)
    if XH.configerManager:getIsKongZhi() then
        --获取伪认证状态
        local fakeIdentify = XH.lobby:getModule("PersonalCenter"):getFakeIdentify()
        if fakeIdentify then
            self:close()
        end
    end
end

function IdentityCheckView:onGetIsForeignIpChanged(event)
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

function IdentityCheckView:initUserID()
    self._textUserID:setText("序号：" .. XH.playerData:getNumberID())
    self._textUserID:setVisible(true)
end

function IdentityCheckView:initLayerNode(isAutoBindUI,isRealnameIng)
    self:initUserID()
    self._inputName:setInputMode(cc.EDITBOX_INPUT_MODE_ANY)
    self._inputName:setFontSize(42)
    self._inputName:setPlaceholderFontColor(cc.c3b(174,137,93))
    self._inputName:setFontColor(cc.c3b(253,249,245))
    self._inputName:registerScriptEditBoxHandler(function(eventName)
        if eventName == "began" or eventName == "changed" then
            return 
        end 
        if eventName == "ended" or eventName == "return" then
            local strName = self._inputName:getText()
            if strName then
                strName = string.gsub(strName,"[.]","·")    --争对少数民族名字的处理
                self._inputName:setText(strName)
            end
        end
    end)
    self._inputNumber:setInputMode(cc.EDITBOX_INPUT_MODE_ANY)
    self._inputNumber:setFontSize(42)
    self._inputNumber:setPlaceholderFontColor(cc.c3b(174,137,93))
    self._inputNumber:setFontColor(cc.c3b(253,249,245))
    self._inputNumber:registerScriptEditBoxHandler(function(eventName)
        if eventName == "began" or eventName == "changed" then
            return
        end
        if eventName == "ended" or eventName == "return" then
            local strIDNum = self._inputNumber:getText()
            if strIDNum then
                strIDNum = string.gsub(strIDNum,"x","X")
                self._inputNumber:setText(strIDNum)
            end
        end
    end)

    if  XH.playerData:getPlayerPhone() == "" then
        self._originalPanel:setVisible(not isAutoBindUI)
        self._newBindPanel:setVisible(isAutoBindUI)
    else
        self._originalPanel:setVisible(true)
    end

    --新增切换地区按钮
    if XH.playerData:getPassiveValue() == 2 and not XH.lobby:getModule("PersonalCenter"):getFakeIdentify() then
        self._unCloseBindBg:setVisible(true)
        -- local areaString = XH.areaData:getAreaName()
        -- self._textAreaName:setText(areaString)
        self._btnClose:setVisible(false)
        self._btnNext:setVisible(false)
        
        if XH.ChannelTool.checkIsWaishengChannel() then
            self._btnChangeArea:setVisible(false)
        else
            self._btnChangeArea:setVisible(true)
        end 
        --self._btnFinish:setPositionX(self._btnFinish:getPositionX() - (self._btnFinish:getPositionX() - self._btnNext:getPositionX())/2)
    elseif  XH.playerData:isProtectedLevel() then
        self._unCloseBindBg:setVisible(true)
        -- local areaString = XH.areaData:getAreaName()
        -- self._textAreaName:setText(areaString)
        self._btnClose:setVisible(false)
        self._btnNext:setVisible(false)

        if XH.ChannelTool.checkIsWaishengChannel() then
            self._btnChangeArea:setVisible(false)
        else
            self._btnChangeArea:setVisible(true)
        end 
        --self._btnFinish:setPositionX(self._btnFinish:getPositionX() - (self._btnFinish:getPositionX() - self._btnNext:getPositionX())/2)
    else
        self._unCloseBindBg:setVisible(false)
    end

    if isRealnameIng then
        self._inputnamebg:setVisible(false)
        self._inputnumberbg:setVisible(false)
        self._originalPanel:setVisible(false)
        self._newRealNameing:setVisible(true)
    end
    -- 双扣CPS包默认不勾选绑定手机号
    if XH.ChannelTool.checkIsDoubleKouGame() and self._checkBoxAutoBind then
        self._checkBoxAutoBind:setSelected(false)
    end
end

function IdentityCheckView:onTouchEventInputName(send,eventType)
    self._tittlenamereal:setVisible(false)
end

function IdentityCheckView:onTouchEventInputNumber(send,eventType)
    self._tittlenumberreal:setVisible(false)
    self._tittleNonumberreal:setVisible(false)
end

function IdentityCheckView:onTouchEventFinsh(send,eventType)
    local strName = self._inputName:getText()
    local strIDNum = self._inputNumber:getText()
    self:eventFinsh(strName, strIDNum)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.REALNAME_FINISHED, nil, {user_ip=un.Device.getIp()})
end

function IdentityCheckView:eventFinsh(strName, strIDNum)
    local isNewUser=XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEW_USER_GUIDE_FINISHED)
    if isNewUser ==false then
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.NEWPLAYER_GUIDE_2) 
    end
    
    --提审模式下的特殊编码
    if self:checkSpecialCode(strName) then
        local isAutoBindPhone = false
        if self._newBindPanel and self._newBindPanel:isVisible() then
            --新的面板可见,并且处于勾选状态
            if self._checkBoxAutoBind and self._checkBoxAutoBind:isSelected() then
                isAutoBindPhone = true
            end
        end
        XH.lobby:getModule("IdentityCheck"):SpecialCodeBind(isAutoBindPhone)
        return
    end

    if strName and strName~= "" then
        strName = XH.StringTool.trim(strName)
        strName = string.gsub(strName,"[.]","·")    --争对少数民族名字的处理
    end 
    if strIDNum and strIDNum~= "" then
        strIDNum = XH.StringTool.trim(strIDNum)
        strIDNum = string.gsub(strIDNum,"x","X")
    end 
    if strIDNum == "" then
        self._tittlenumberreal:setVisible(true)
    end
    if strName == "" then
        self._tittlenamereal:setVisible(true)
    end
    if XH.areaData:getLobbyID() == XH.LOBBY_ID.JINHUA then --金华的特判
        if self._checkBoxAutoBind and not self._checkBoxAutoBind:isSelected() then
            self._newRealiphone:setVisible(true)
            return
        end
    end
    if strName == "" or strIDNum == "" then
       return
    end
    --控制器
    if XH.configerManager:getIsKongZhi() then
        XH.lobby:getModule("PersonalCenter"):reqBindID(strName)
        return
    end

    if not self:checkNameLegality(strName) then
        XH.TipTool.showTip({
			type = XH.TIP_LAYER_TYPE.OK,
            bTop = true
		},XH.ConstString.getStr("ERROR_NAME"))
        return
    end

    local strIdNumberLen = string.len(strIDNum)
    if strIdNumberLen ~= 18 then
        self._tittleNonumberreal:setVisible(true)
        return
    end

    local result,tips = self:checkIdNumberLegality(strIDNum)
    if not result then
        if tips then
            XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK,
                bTop = true
            },tips)
        else
            XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK,
                bTop = true
            },XH.ConstString.getStr("ERROR_IDENTITY"))
        end
        return
    end
    if strName and strIDNum then
        self:onDefendRequest(strName,strIDNum)
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_04)         
    end

end

function IdentityCheckView:onTouchEventNext(send,eventType)
    local isNewUser=XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEW_USER_GUIDE_FINISHED)
    if isNewUser ==false then
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.NEWPLAYER_GUIDE_3) 
    end

    if self.compulsoryRealName then
        XH.TipTool.showTip( {type = XH.TIP_LAYER_TYPE.OK,bTop = true,} , "完成实名认证才能游戏和充值哦，\n\n请尽快完成。")
    end
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_IDENTITY_CHECKVIEW_CLOSE)
    self:close()
end

function IdentityCheckView:onTouchEventClose(send,eventType)
    --判断是否是新注册用户
    local isNewUser=XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEW_USER_GUIDE_FINISHED)
    if isNewUser ==false then
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.NEWPLAYER_GUIDE_1) 
    end
    if self.isRealnameIng then
        cc.UserDefault:getInstance():setBoolForKey(string.format("KW_REALNAME_CHECK_ING_%s",XH.playerData:getNumberID()),true)
    end
    if self.compulsoryRealName then
        XH.TipTool.showTip( {type = XH.TIP_LAYER_TYPE.OK,bTop = true,} , "完成实名认证才能游戏和充值哦，\n\n请尽快完成。")
    end
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_IDENTITY_CHECKVIEW_CLOSE)
    self:close()
end

function IdentityCheckView:onTouchEventCheckAutoBind(send,eventType)
    if eventType == ccui.CheckBoxEventType.selected then
        self._newRealiphone:setVisible(false)
    end
    local autoBind = self._checkBoxAutoBind:isSelected()
    local conventionData = {}
    conventionData.label = autoBind==true and "勾选一键绑定" or "不勾选一键绑定"
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.REALNAME_BINDPHONE, conventionData, {user_ip=un.Device.getIp()})
end

function IdentityCheckView:onDefendRequest(strName, strIDNum )
     if strName == nil then
        return
    end
    if strIDNum == nil then
        return
    end
    local isAutoBindPhone = false
    if self._newBindPanel and self._newBindPanel:isVisible() then
        --新的面板可见,并且处于勾选状态
        if self._checkBoxAutoBind and self._checkBoxAutoBind:isSelected() then
            isAutoBindPhone = true
        end
    end
    XH.lobby:getModule("IdentityCheck"):reqDefendIdentity(strName, strIDNum, isAutoBindPhone)
end


function IdentityCheckView:checkSpecialCode(strName)
    if tonumber(strName) == KW_SPECIAL_CODE then
        return true
    else
        return false
    end
end

function IdentityCheckView:checkNameLegality(name)
    local len = string.len(name)
    local index = string.find(name,"·")
	if name == nil then
        return false
    --争对少数名族·在最前面和最后面的处理
    elseif index and index == len -1 or index == 1 then 
        return false
    elseif XH.StringTool.isSpecialCharsExist(name) == true then 
        return false
    end
	return true
end

function IdentityCheckView:checkIdNumberLegality(strIdNumber)
	if strIdNumber == nil then
		return false
	end
    
    local firstNumber = tonumber(string.sub(strIdNumber,1,1))
    if firstNumber ~= nil then
        if firstNumber == 0 then
            return false
        end
    end

    if XH.playerData:getActiveValue() >= 2 or XH.configerManager:getIsNewRealName() then   -- 实名身份证号年月日判断
        local yearNumber = tonumber(string.sub(strIdNumber,7,10))
        local mouthNumber = tonumber(string.sub(strIdNumber,11,12))
        local dayNumber = tonumber(string.sub(strIdNumber,13,14))

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
                local bigMouth = {[1]=1,[3]=1,[5]=1,[7]=1,[8]=1,[10]=1,[12]=1,} --大月月份
                if bigMouth[mouthNumber] == 1 then 
                    if dayNumber > 31 then
                        return false
                    end
                elseif mouthNumber == 2 then -- 2月日期判断
                    if (math.fmod(yearNumber,4) == 0 and math.fmod(yearNumber,100) ~= 0) or (math.fmod(yearNumber,400) == 0) then
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
            if age < 18 then--判断是否成年
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

--在实名时切换地区
function IdentityCheckView:onTouchEventChangeArea(send,eventType)

    self:close()

    XH.login:getModule("Login"):clearLoginStates()
    XH.loginData:setLobbyID(0)
    XH.Controller:getInstance():enterLogin()
end

function IdentityCheckView:onTouchEventChangeUser(send,eventType)
    self:close()
    XH.lobby:getModule("IdentityCheck"):changeUser()
end

function IdentityCheckView:onRealNameRefresh(event)
    self:close()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.REALNAME_SUCCESS, nil, {user_ip=un.Device.getIp()})
    XH.TipTool.showTip({
        type = XH.TIP_LAYER_TYPE.OK,
        bTop = true,
        funcOK = function()
            if event.msg and event.msg.isAutoBindPhone then
                XH.login:getModule("Login"):reqGYInfo(true)
            end
        end
    },XH.ConstString.getStr("IDENTITY_SUCCESS"))
end

function IdentityCheckView:onRealNameRefreshing(event)
    self:close()
    XH.viewManager:openView("IdentityCheckView",nil,{isRealnameIng = true})
end


function IdentityCheckView:onTouchEventVis(send,eventType)
    local data = {}
    data.time1 = os.time()
    data.userid = XH.playerData:getNumberID()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.RealNameV, data)
    XH.playerData:setProtectedInwhitelist(2)
    XH.lobby:getModule("IdentityCheck"):reqRealNameV()
end

function IdentityCheckView:onRealNameTimeOver(event)
--    XH.TipTool.showTip({
--        bTop = true,
--        type = XH.TIP_LAYER_TYPE.OK_NO_CLOSE,
--        funcOK = function(  )
--        end,
--    }, "游客您好，您已经完成40分钟体验，之后您可以选择【微信登录】继续享受精彩的游戏服务哦！下次体验时间:2021年5月21日8时50分")

    local msg = "游客您好，您已经完成40分钟体验，之后您可以选择【微信登录】继续享受精彩的游戏服务哦！"
    if event.data > 0 then
        msg = msg .. "下次体验时间:".. os.date("%Y",event.data).."年"..os.date("%m",event.data).."月"..os.date("%d",event.data).."日"..os.date("%H",event.data).."时"..os.date("%M",event.data).."分"
    end

    XH.TipTool.showTip({
        bTop = true,
        type = XH.TIP_LAYER_TYPE.OK_NO_CLOSE,
        funcOK = function(  )
        end,
    }, msg)
end

function IdentityCheckView:onRealNameTimeOverLimit(event)
    self:close()
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_RN_TOURIST_LOGIN, true)
    XH.login:getModule("Login"):reqTouristLogin()
end



-- function IdentityCheckView:onSysTimeCallback(event)
--     if event and event.data then
--         if self._userName and self._userId then
--             if XH.lobby:getModule("IdentityCheck"):isAudlt(self._userId,event.data) then
--                 self:onDefendRequest(self._userName,self._userId)
--             else
--                 XH.TipTool.showTip({
--                     type = XH.TIP_LAYER_TYPE.OK,
--                 },"实名认证失败，您还未成年")
--             end
--        end
--     end
-- end

return IdentityCheckView �b  