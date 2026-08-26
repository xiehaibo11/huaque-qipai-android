local RequiteViewView = class("RequiteViewView",XH.ViewBase)
local TaskDefine =  require("app.Define.TaskDefine")

local CHOOSE_INDEX = {
    FANG_KA_LOOBY = 1,
    GOLD = 2,
    FANG_KA_TEAHOUSE = 3
}

function RequiteViewView:getCSBPath()
    if XH.areaData:getLobbyID() == XH.LOBBY_ID.SHAOXING3D then
        return "cocosStudio/hall/CSB/window/RequiteLayerShaoXing3D.csb"
    end

    return "cocosStudio/hall/CSB/window/RequiteLayer.csb"
end

function RequiteViewView:getBindingInfo()
    return {
        ["_KW_BTN_SURE_SELECT"] = {varName="_btnSureSelect",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventSureSelect"},
        ["_KW_PANEL_ITEM_ID"] = {varName="_panelItemTeahouseId",type = XH.UI_TYPE.IMAGE_TOBUTTON,onTouchEnded = "onTouchEventTeaHouseId"},
        ["_KW_PANEL_SELECT_TEAHOUSE_ID"] = {varName="_panelShowTeahouseList",type = XH.UI_TYPE.IMAGE_TOBUTTON,onTouchEnded = "onTouchShowTeahouseList"},
        ["_KW_TEXT_SELECT_TEAHOUSE_ID"] = {varName="_textTeahouseId"},
        ["_KW_PANEL_LIST_TEAHOUSE"] = {varName="_panelListTeahouse"},
        ["_KW_LISTVIEW_TEAHOUSE"] = {varName="_listviewTeahouse"},
        ["_KW_PANEL_VIEW_2"] = {varName="_panelView2"},
        ["_KW_PANEL_VIEW_3"] = {varName="_panelView3"},
        ["_KW_PRIZE_CHECKBOX_1"] = {varName="_prizeCheckbox1"},
        ["_KW_PRIZE_CHECKBOX_2"] = {varName="_prizeCheckbox2"},
        ["_KW_PRIZE_CHECKBOX_3"] = {varName="_prizeCheckbox3"},
        ["_KW_PRIZE_DEATIL_TEXT_1"] = {varName="_prizeDeatilText1"},        
        ["_KW_PRIZE_DEATIL_TEXT_2"] = {varName="_prizeDeatilText2"},
        ["_KW_PRIZE_DEATIL_TEXT_3"] = {varName="_prizeDeatilText3"},
        ["_KW_PANEL_AWARD_1"] = {varName="_panelAward1"},
        ["_KW_PANEL_AWARD_2"] = {varName="_panelAward2"},
        ["_KW_PANEL_AWARD_3"] = {varName="_panelAward3"},
    }
end

function RequiteViewView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("RequiteView"), eventKeyName = "EVENT_REQUITE_SUCCESS", callBack = "onGetRequiteBack" },
        { module = XH.lobby:getModule("TeaHouseList"), eventKeyName = "EVENT_TEA_HOUSE_LIST_CHANGED", callBack = "onTeaHouseListChanged" },
    }
end

function RequiteViewView:ctor(param)
    param = param or {}
	RequiteViewView.super.ctor(self,param)
    self._chooseRequiteIndex = CHOOSE_INDEX.FANG_KA_LOOBY
    self._chooseRequiteIDs = {}
    self._chooseTeahouseInfo = nil
    self:initView()
    self:checkReqTeaHouseList()
    self:showAwardView()
end

function RequiteViewView:initView()
    self._listviewTeahouse:setItemModel(self._panelItemTeahouseId)
    self._listviewTeahouse:setScrollBarEnabled(false)
    self._prizeCheckbox1:addEventListener(handler(self, self.onTouchChoosePrize))
    self._prizeCheckbox2:addEventListener(handler(self, self.onTouchChoosePrize))
    self._prizeCheckbox3:addEventListener(handler(self, self.onTouchChoosePrize))
end

function RequiteViewView:checkReqTeaHouseList()
    --目前只有台州需要
    if XH.areaData:getLobbyID() ~= XH.LOBBY_ID.TAIZHOU then
        return
    end
    local requitePrize = XH.playerData:getRequitePrize()
    if not requitePrize then
        return
    end
    for i = 1, #requitePrize do
        if requitePrize[i].info and self:isTeahouseAward(requitePrize[i].info) then
            --请求比赛场列表
            self:reqTeaHouseList()
            break
        end
    end
end

function RequiteViewView:reqTeaHouseList()
    self:performWithDelay(function()
        XH.lobby:getModule("TeaHouseList"):reqTeaHouseList()
    end, 0.1)
end

function  RequiteViewView:onTeaHouseListChanged(event)
    local teaHousePendingList = XH.lobby:getModule("TeaHouseList"):getTeaHousePendingList()
    for key, var in ipairs(teaHousePendingList) do
        if var.nTeaNumber ~= 0 then
            local password = string.format("%06d", var.nPasswd)
            self._listviewTeahouse:pushBackDefaultItem()
            local items = self._listviewTeahouse:getItems()
            local item = items[#items]
            XH.UITool.setText(item, "KW_TEXT_TEAHOUSE_ID", password)
            item:setTag(key)
            item:setVisible(true)
        end
    end
end

function RequiteViewView:showAwardView()
    local requitePrize = XH.playerData:getRequitePrize()
    if not requitePrize then
        return
    end
    self._panelView2:setVisible(#requitePrize == 2)
    self._panelView3:setVisible(#requitePrize == 3)
    for i = 1, #requitePrize do
        if requitePrize[i].type == -1 then
            self._chooseRequiteIDs[CHOOSE_INDEX.GOLD] = requitePrize[i]
            self._prizeDeatilText2:setString(requitePrize[i].cnt .. "金币")
        elseif requitePrize[i].info and self:isTeahouseAward(requitePrize[i].info) then
            self._chooseRequiteIDs[CHOOSE_INDEX.FANG_KA_TEAHOUSE] = requitePrize[i]
            self._prizeDeatilText3:setString(requitePrize[i].cnt .. XH.StringTool.replaceMatchStr("亲友圈房卡"))
        else
            self._chooseRequiteIDs[CHOOSE_INDEX.FANG_KA_LOOBY] = requitePrize[i]
            self._prizeDeatilText1:setString(requitePrize[i].cnt .. "房卡")
        end
    end
    local panelView = #requitePrize == 2 and self._panelView2 or self._panelView3
    local awardItems = {self._panelAward1, self._panelAward2, self._panelAward3}
    for i = 1, #requitePrize do
        if awardItems[i] then
            local pos = XH.UITool.seekNodeByName(panelView,  "KW_POS_" .. i)
            if pos then
                local awardItem = awardItems[i]:removeFromParentAndCleanup(false)
                pos:addChild(awardItem)
                awardItem:setPosition(cc.p(0, 0))
                awardItem:setVisible(true)
            end
        end
    end
end

function RequiteViewView:onTouchEventTeaHouseId(send, eventType)
    local index = send:getTag()
    local password = XH.UITool.getText(send, "KW_TEXT_TEAHOUSE_ID")
    local teaHousePendingList = XH.lobby:getModule("TeaHouseList"):getTeaHousePendingList()
    local teahouseInfo = teaHousePendingList[index]
    self._chooseTeahouseInfo = clone(teahouseInfo)
    if self._textTeahouseId then
        self._textTeahouseId:setString(password)
    end
    self:resetListViewStatus()
end

function RequiteViewView:onTouchShowTeahouseList(send, eventType)
    if self._panelListTeahouse then
        self._panelListTeahouse:setVisible(not self._panelListTeahouse:isVisible())
    end
    send:setRotation(send:getRotation() + 180)
    --切换到比赛场标签上
    self._prizeCheckbox1:setSelected(false)
    self._prizeCheckbox2:setSelected(false)
    self._prizeCheckbox3:setSelected(true)
    self._chooseRequiteIndex = CHOOSE_INDEX.FANG_KA_TEAHOUSE
end

function RequiteViewView:onTouchEventSureSelect(send, eventType)
    local reqPropId = -1
    if self._chooseRequiteIDs[self._chooseRequiteIndex] then
        reqPropId = self._chooseRequiteIDs[self._chooseRequiteIndex].type
    end
    local reqRequiteAwardFunc = function ()
        local paramStr = ";reqPropId = " .. reqPropId 
        if self._chooseRequiteIndex == CHOOSE_INDEX.FANG_KA_TEAHOUSE then
            local pPropInfo = string.format("\"%s\"", self._chooseRequiteIDs[self._chooseRequiteIndex].info or "")
            paramStr = paramStr .. ";reqPropInfo = " .. pPropInfo  .. ";teaid = " .. self._chooseTeahouseInfo.nTeaNumber
        end
        XH.lobby:getModule("RequiteView"):reqTaskProtocol(TaskDefine.TASK_ID.TASK_REQUITE_TASK_ID, paramStr)
    end
    if self._chooseRequiteIndex == CHOOSE_INDEX.FANG_KA_TEAHOUSE and self:isTeahouseAward(self._chooseRequiteIDs[self._chooseRequiteIndex].info) then
        if not self._chooseTeahouseInfo then
            XH.TipTool.showTip({
                bTop = true,
                type = XH.TIP_LAYER_TYPE.OK,
            }, "需要选择具体亲友圈的房卡才可以领\n取奖励哦~")
            return
        end
        local password = string.format("%06d", self._chooseTeahouseInfo.nPasswd)
        local tipStr = string.format("请确认所选的为%s亲友圈，%s的%d张亲友圈房卡哦~", self._chooseTeahouseInfo.acOwnerNick, password, self._chooseRequiteIDs[CHOOSE_INDEX.FANG_KA_TEAHOUSE].cnt)
        XH.TipTool.showTip({
            bTop = true,
            type = XH.TIP_LAYER_TYPE.OK_CANCEL,
            funcOK = reqRequiteAwardFunc
        }, tipStr)
    else
        reqRequiteAwardFunc()
    end
end

function RequiteViewView:onTouchChoosePrize(send, eventType)
    send:setSelected(true)
    if send:getName() == "_KW_PRIZE_CHECKBOX_2" then
        self._prizeCheckbox1:setSelected(false)
        self._prizeCheckbox3:setSelected(false)
        self._chooseRequiteIndex = CHOOSE_INDEX.GOLD
    elseif send:getName() == "_KW_PRIZE_CHECKBOX_3" then
        self._prizeCheckbox1:setSelected(false)
        self._prizeCheckbox2:setSelected(false)
        self._chooseRequiteIndex = CHOOSE_INDEX.FANG_KA_TEAHOUSE
    else
        self._prizeCheckbox2:setSelected(false)
        self._prizeCheckbox3:setSelected(false)
        self._chooseRequiteIndex = CHOOSE_INDEX.FANG_KA_LOOBY
    end
    self:resetListViewStatus()
end

function RequiteViewView:onGetRequiteBack(event)
    local data = event.data
    if data.nType ==  XH.Req.TYPE.SUCCESS then
            --显示掉落窗口
        local doDelay = function(prizeData)
            XH.viewManager:openView("GoldRewardView", XH.ZORDER.TOPLAYER, prizeData)
            XH.playerData:flushPlayerDrop()
            XH.playerData:flushGoldCoin()
            self:close()
        end
        self:performWithDelay(handler({self._chooseRequiteIDs[self._chooseRequiteIndex]}, doDelay), 0.2)
    end 
end

function RequiteViewView:isTeahouseAward(info)
    if string.find(info or "", "TeaHouseFangKa") then
        return true
    end
    return false
end

function RequiteViewView:close()
    if self._teahouseListProxy then
        self._teahouseListProxy:removeAllEventListeners()
    end
    RequiteViewView.super.close(self)
end

function RequiteViewView:resetListViewStatus()
    if self._panelListTeahouse then
        self._panelListTeahouse:setVisible(false)
    end
    if self._panelShowTeahouseList then
        self._panelShowTeahouseList:setRotation(0)
    end
end

return RequiteViewView  �(  