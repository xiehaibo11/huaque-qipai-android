local ChooseAreaView = class("ChooseAreaView",XH.ViewBase)
local areaConfig = require("app.Config.AreaConfig")

local KW_PANEL_GPS_ANI = "KW_PANEL_GPS_ANI"
local KW_IMG_CHECK = "KW_IMG_CHECK"
local KW_IMG_CHECK_STATE = "KW_IMG_CHECK_STATE"
local KW_SECOND_BACK = "KW_SECOND_BACK"
local KW_AREA_NAME_BG = "KW_AREA_NAME_BG"
local stringTips = {
    [1] = "点击地图上相应的区域玩法，选择您所要进入的玩法",
    [2] = "您也可以直接点击右边的区域玩法，进入对应玩法",
}

function ChooseAreaView:getCSBPath()
    return "cocosStudio/hall/CSB/ChooseArea.csb"
end

function ChooseAreaView:getBindingInfo()
    return {
        ["_kw_huzhou"] = {varName="_huzhouBg"},
        ["_kw_jiaxing"] = {varName="_jiaxingBg"},
        ["_kw_hangzhou"] = {varName="_hangzhouBg"},
        ["_kw_shaoxing"] = {varName="_shaoxingBg"},
        ["_kw_ningbo"] = {varName="_ningboBg"},
        ["_kw_zhoushan"] = {varName="_zhoushanBg"},
        ["_kw_quzhou"] = {varName="_quzhouBg"},
        ["_kw_jinhua"] = {varName="_jinhuaBg"},
        ["_kw_taizhou"] = {varName="_taizhouBg"},
        ["_kw_lishui"] = {varName="_lishuiBg"},
        ["_kw_wenzhou"] = {varName="_wenzhouBg"},
        ["_kw_second_hangzhou"] = {varName="_secondhangzhou"},
        ["_kw_second_wenzhou"] = {varName="_secondwenzhou"},
        ["_kw_second_lishui"] = {varName="_secondlishui"},
        ["_kw_second_ningbo"] = {varName="_secondningbo"},
        ["_kw_second_shaoxing"] = {varName="_secondshaoxing"},

        ["_kw_panel_huzhou"] = {varName="_huzhouClickNode",onTouchEnded = "onAreaClicked"},
        ["_kw_panel_jiaxing"] = {varName="_jiaxingClickNode",onTouchEnded = "onAreaClicked"},
        ["_kw_panel_hangzhou"] = {varName="_hangzhouClickNode",onTouchEnded = "onAreaClicked"},
        ["_kw_panel_shaoxing"] = {varName="_shaoxingClickNode",onTouchEnded = "onAreaClicked"},
        ["_kw_panel_ningbo"] = {varName="_ningboClickNode",onTouchEnded = "onAreaClicked"},
        ["_kw_panel_zhoushan"] = {varName="_zhoushanClickNode",onTouchEnded = "onAreaClicked"},
        ["_kw_panel_quzhou"] = {varName="_quzhouClickNode",onTouchEnded = "onAreaClicked"},
        ["_kw_panel_jinhua"] = {varName="_jinhuaClickNode",onTouchEnded = "onAreaClicked"},
        ["_kw_panel_taizhou"] = {varName="_taizhouClickNode",onTouchEnded = "onAreaClicked"},
        ["_kw_panel_lishui"] = {varName="_lishuiClickNode",onTouchEnded = "onAreaClicked"},
        ["_kw_panel_wenzhou"] = {varName="_wenzhouClickNode",onTouchEnded = "onAreaClicked"},

        ["_KW_FNT_CHOOSEAREA_TIP"] = {varName="_textChooseAreaTip"},

        ["_KW_BTN_BACK"] = {varName="_back",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onBackClick"},
        ["_KW_BTN_MODEL"] = {varName="_btnModel"},
        ["_KW_LISTVIEW_CHOOSE_AREA"] = {varName="_chooseAreaList"},
        ["_BG"] = {varName="_bg"},
    }
end

function ChooseAreaView:ctor(param) 
    local tipParam = param or {}
	ChooseAreaView.super.ctor(self,tipParam)
    self:initUI()
    self._cityName = ""
    self._viewStartTime = os.time()
end

function ChooseAreaView:initUI()
    --适配
    self:adaptBg()
    --初始化所有的城市地区的背景，未上线的灰置，已上线的点亮
    local cityName = XH.login:getModule("ChooseArea"):getAllAreaName()
    --获取上一个进入的地区，设置选中的状态,其他加混色
    local lastEnterAreaName =  XH.userDefault:getValue(XH.userDefault.KEY_ID.LAST_ENTER_AREA_NAME )
    for i = 1, #cityName do
        local count = XH.login:getModule("ChooseArea"):countAreaIncludeApp(cityName[i])
        local checkStateBg = XH.UITool.seekNodeByName(self["_" .. cityName[i] .. "Bg"], KW_IMG_CHECK_STATE)
        if count == 0 then
            local checkBg = XH.UITool.seekNodeByName(self["_" .. cityName[i] .. "Bg"], KW_IMG_CHECK)
            checkBg:loadTexture("choose_area_check_no.png", ccui.TextureResType.plistType)
            checkStateBg:loadTexture("choose_area_not_open.png", ccui.TextureResType.plistType)
        elseif count > 1 then
            checkStateBg:loadTexture("choose_area_more_open.png", ccui.TextureResType.plistType)
        else
            checkStateBg:setVisible(false)
        end
        if cityName[i] ~= lastEnterAreaName and lastEnterAreaName ~= "" then
            self["_" .. cityName[i] .. "Bg"]:setColor(cc.c3b(191, 191, 191))
        end
    end 
    if lastEnterAreaName ~= "" and self["_" .. lastEnterAreaName .. "Bg"] then
        local gpsAniNode = XH.UITool.seekNodeByName(self["_" .. lastEnterAreaName .. "Bg"], KW_PANEL_GPS_ANI)
        if gpsAniNode then
            gpsAniNode:setVisible(true)
		    local params = {
			    path = "animation/Lobby/Base/Zjb_dingwei/",
			    ske = "zjb_map_fb_ske.json",
			    tex = "zjb_map_fb_tex.json",
			    armatureName = "Armature",
			    dragonBonesName = "zjb_map_fb",
			    animationName = "newAnimation"
		    }

		    if not self._gpsAni then
			    self._gpsAni = display.playDargonBonesAnimByTimes(params, 0)
			    if self._gpsAni then
				    gpsAniNode:addChild(self._gpsAni)
			    end
		    end
	    end
        local checkBg = XH.UITool.seekNodeByName(self["_" .. lastEnterAreaName .. "Bg"], KW_IMG_CHECK)
        checkBg:loadTexture("choose_area_check.png", ccui.TextureResType.plistType)
    end

    self:initChooseAreaList()

    --循环播放提示语
    self:loopPlayTip()
end

function ChooseAreaView:initChooseAreaList()
    --对区域栏内所有的app添加点击事件
    local temp = {}
    for k,v in pairs(areaConfig) do
        local node = self._btnModel:clone()
        node:setName("KW_BTN_" .. k)
        node:setTitleText(v.areaName)
        node:addTouchEventListener(handler(self, self.onTouchEventChooseArea))
        temp[v.sortPower] = node
    end

    self._chooseAreaList:removeAllChildren()
    self._listViewHeight = self._chooseAreaList:getContentSize().height
    self._chooseAreaList:setContentSize(self._chooseAreaList:getContentSize().width, self._listViewHeight)
    for i = 1, #temp do
        if temp[i] and tolua.isnull(temp[i]) == false then
            self._chooseAreaList:addChild(temp[i])
        end
    end
end

function ChooseAreaView:adaptBg()
    local screenSize = cc.Director:getInstance():getWinSize()
    self._bg:setScale(screenSize.height / 1080)
    if screenSize.width / screenSize.height < 1.7 then
        self._bg:setScaleX(1)
    end
end

function ChooseAreaView:initChooseTipText()
    if not self._textChooseAreaTip or #stringTips < 1 then
        return
    end
    self._textChooseAreaTip:setString(stringTips[1])
end

function ChooseAreaView:loopPlayTip()
    self:initChooseTipText()
    if #stringTips <= 1 then
        return
    end
    local tipsCount = #stringTips
    local tmpTipIndex = 1
    self._textChooseAreaTip:runAction(cc.RepeatForever:create(cc.Sequence:create(cc.DelayTime:create(5),
        cc.CallFunc:create(function()
            tmpTipIndex = tmpTipIndex + 1
            tmpTipIndex = tmpTipIndex <= tipsCount and tmpTipIndex or 1
            self._textChooseAreaTip:setString(stringTips[tmpTipIndex])
        end)))
    )
end

function ChooseAreaView:onBackClick()
    self:close()
end

--地图上的区域点击事件
function ChooseAreaView:onAreaClicked(send, eventType)
    --获取地区名字
    self._areaid_choice = "Map"
    local bottonName = send:getName()
    local splitStr = string.split(bottonName, "_")
    local areaName = splitStr[#splitStr]

    self._cityName = areaName

    local count = XH.login:getModule("ChooseArea"):countAreaIncludeApp(areaName)
    if count == 0 then
        --此地区未上线
        XH.TipTool.showToast("暂未开放，敬请期待", 2)
        return
    elseif count == 1 then
        --此地区只有一个app ，直接进入登录流程
        local lobbyID = XH.login:getModule("ChooseArea"):getAreaLobbyID(areaName)
        if #lobbyID == 1 then
            self:onChooseOneArea(lobbyID[1])
        end
    else
        self:initSecondEvent(self["_second" .. areaName])
        self["_" .. areaName .. "Bg"]:runAction(cc.ScaleTo:create(0.2, 1.3))
        self["_second" .. areaName]:setVisible(true)
        local actionScale = cc.ScaleTo:create(0.2, 1)
        self["_second" .. areaName]:runAction(cc.Spawn:create(actionScale, cc.FadeIn:create(0.2)))
        self._chooseAreaList:removeAllChildren()
        local secondApp = XH.login:getModule("ChooseArea"):getAreaIncludeApp(areaName)
        self._chooseAreaList:setContentSize(self._chooseAreaList:getContentSize().width, cc.Director:getInstance():getWinSize().height)
        for i = 1, #secondApp do
            local node = self._btnModel:clone()
            node:setName("KW_BTN_SECOND_LIST_" .. secondApp[i])
            node:setTitleText(areaConfig[secondApp[i]].areaName)
            node:addTouchEventListener(handler(self, self.onTouchEventChooseArea))
            self._chooseAreaList:addChild(node)
        end
    end
end

function ChooseAreaView:initSecondEvent(node)
    if node then
        local backNode = XH.UITool.seekNodeByName(node, KW_SECOND_BACK)
        if backNode then
            backNode:addTouchEventListener(handler(self, self.onTouchEventSecondBack))
        end
        local areaList = XH.UITool.seekNodeByName(node, KW_AREA_NAME_BG)
        if areaList then
            for i = 1, #areaList:getChildren() do
                areaList:getChildren()[i]:addTouchEventListener(handler(self, self.onTouchEventChooseArea))
            end
        end
    end
end

function ChooseAreaView:onTouchEventSecondBack(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self["_" .. self._cityName .. "Bg"]:runAction(cc.ScaleTo:create(0.2, 1))
    local actionScale = cc.ScaleTo:create(0.2, 0.01)
    self["_second" .. self._cityName]:runAction(cc.Spawn:create(actionScale, cc.FadeOut:create(0.2)))
    self._chooseAreaList:removeAllChildren()
    self._chooseAreaList:setContentSize(self._chooseAreaList:getContentSize().width, self._listViewHeight)
    local temp = {}
    for k,v in pairs(areaConfig) do
        local node = self._btnModel:clone()
        node:setName("KW_BTN_" .. k)
        node:setTitleText(v.areaName)
        node:addTouchEventListener(handler(self, self.onTouchEventChooseArea))
        temp[v.sortPower] = node
    end
    for i = 1, #temp do
        self._chooseAreaList:addChild(temp[i])
    end
end

function ChooseAreaView:onTouchEventChooseArea(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._areaid_choice = "List"
    local bottonName = send:getName()
    local splitStr = string.split(bottonName, "_")
    local lobbyID = splitStr[#splitStr]
    self:onChooseOneArea(lobbyID)
end

function ChooseAreaView:onChooseOneArea(lobbyID)
    self._chooseLobbyid = lobbyID or -1
    XH.login:getModule("Login"):enterLobby(lobbyID)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_ENTER_AREA_NAME , XH.login:getModule("ChooseArea"):getAreaName(lobbyID))

    -- --选中一个区后重置之前的srs
    XH.netEngine:removeAllSRS()
    self:performWithDelay(
        function ()
            XH.login:getModule("Login"):sessionLogin()
            self:close()
        end, 0.1)
end

function ChooseAreaView:close()
    --数据统计
    local data = {}
    data.time1 = self._viewStartTime or 0
    data.time2 = os.time() or 0
    data.staytime = data.time2 - data.time1
    data.areaid = self._chooseLobbyid
    data.areaid_choice = self._areaid_choice or ""
    --XH.throwDataManager:throwData(XH.ThrowDataDefine.AreaSelected, data)
    ChooseAreaView.super.close(self)
end

return ChooseAreaView  �.  