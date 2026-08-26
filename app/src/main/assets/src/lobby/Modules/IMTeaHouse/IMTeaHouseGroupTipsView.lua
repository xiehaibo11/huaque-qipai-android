local IMTeaHouseGroupTipsView = class("IMTeaHouseGroupTipsView", XH.ViewBase)
function IMTeaHouseGroupTipsView:getCSBPath()
    return "cocosStudio/hall/CSB/IMTeaHouse/IMTeaHouseGroupTipsLayer.csb"
end
--imteahouse_exit_group   退出分组图片名称
function IMTeaHouseGroupTipsView:getBindingInfo()
    return {     
        --分组信息界面 
        ["_KW_GROUP_INFO_PANEL"] = {varName = "_groupInfoPanel"},
        ["_KW_DETAIL_FRAME_PANEL"] = {varName = "_detailInfoPanel"},
        ["_KW_IMG_HEAD"] = {varName = "_headImg",type = XH.UI_TYPE.REMOTEIMAGE},
        ["_KW_MEMBER_NUM"] = {varName = "_memberNumtext"},
        ["_KW_TEXT_COST"] = {varName = "_costText"},
        ["_KW_MEMBER_TYPE"] = {varName = "_memberTypeText"},
        ["_KW_BTN_DISMISS"] = {varName = "_disMissBtn",onTouchEnded = "onTouchDismissBtn"},
        ["_KW_EXIT_GROUP"] = {varName = "_exitBtn", onTouchEnded = "onTouchExitBtn"},
        ["_KW_GROUP_EDIT"] = {varName = "_groupNameEdit"},
        ["_KWA_BTND_CLOSE"] = {varName = "_closeBtn",onTouchEnded = "onTouchClose"},
        ["_KW_EDIT_BTN"] = {varName = "_editBtn"},
        ["_KW_GROUP_NAME"] = {varName = "_groupName"},
        --退出分组提示界面
        ["_KW_PANEL_EXIT"] = {varName = "_exitPanel"},
        ["_KW_EXIT_CONFIRM"] = {varName = "_exitSureBtn",onTouchEnded = "onTouchExitConFrim"},
        ["_KW_CANCEL_BTN"] = {varName = "_exitCancelBtn",onTouchEnded = "onTouchCancel"},
        --解散分组提示界面
        ["_KW_PANEL_DISMISS"] = {varName = "_DismissPanel"},
        ["_KW_DISMISS_CONFIRM"] = {varName = "_dismissSureBtn",onTouchEnded = "onTouchDisMissConFrim"},
        ["_KW_DISMISS_CANCEL_BTN"] = {varName = "_dismissCancelBtn", onTouchEnded = "onTouchCancel"},
    }
end

local VIEWTYPE = {
    GROUPINFO = "GROUPINFO",--分组信息界面
    EXITGROUP = "EXITGROUP",--退出分组提示界面
    DISMISSGROUP = "DISMISSGROUP",--解散分组提示界面
}

local PLAYERTYPE = {
    OWNER = "组长",--领队
    MEMBER = "成员",--成员
    OWNER1 = "OWNER",
    MEMBER1 = "MEMBER"
}

function IMTeaHouseGroupTipsView:ctor(info)
    IMTeaHouseGroupTipsView.super.ctor(self)
    self._tmpInputName = nil
    if info.data then
        self._teaNumber = info.data.nTeaNumber
        XH.lobby:getModule("Im"):reqTeaHouseCost(self._teaNumber)
        XH.lobby:getModule("Im"):reqUserInfoListCnt(self._teaNumber)
        self:initView(info.data)
    end
end

function IMTeaHouseGroupTipsView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_COST_UPDATE", callBack = "onUpdateCost" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_PLAYERCNT_UPDATE", callBack = "onUpdatePlayerCnt" },
    }
end

function IMTeaHouseGroupTipsView:initView(info)
    self:selectShowViewType(info)
end



function IMTeaHouseGroupTipsView:selectShowViewType(data)
    if data.scene == VIEWTYPE.GROUPINFO then
        self:updatePanelView(true,false,false)
        if data.playerType then
            if PLAYERTYPE[data.playerType] then
                self._memberTypeText:setString(PLAYERTYPE[data.playerType])
                self._disMissBtn:setVisible(data.playerType == PLAYERTYPE.OWNER1 )
                if data.playerType == PLAYERTYPE.OWNER1 then
                    local positionX = self._detailInfoPanel:getPositionX()
                    local positionY = self._detailInfoPanel:getPositionY()
                    self._detailInfoPanel:setPosition(cc.p(positionX ,positionY - 40))
                end
                self._exitBtn:setVisible(data.playerType == PLAYERTYPE.MEMBER1 )        
                self._editBtn:setVisible(data.playerType == PLAYERTYPE.OWNER1 )
                self._groupNameEdit:setEnabled(data.playerType == PLAYERTYPE.OWNER1)
                self._groupNameEdit:setFontColor(cc.c3b(205, 133, 81))
            end
            self._groupName:setString(data.szData)
            self._groupNameEdit:setText(data.szData)
            self._memberNumtext:setString(data.nOnLineUserCnt)
            self._costText:setString(data.nTodayUseProps)
            self._headImg:setUrl(data.szUrl)
            
            self._groupNameEdit:setInputMode(cc.EDITBOX_INPUT_MODE_NUMERIC)
            self._groupNameEdit:registerScriptEditBoxHandler(function(eventName)
                if eventName == "began" then
                    self._groupName:setVisible(false)
                elseif eventName == "changed" then
                    self._groupName:setString(self._groupNameEdit:getText())
                elseif eventName == "ended" then                    
                    local inputNum = self._groupNameEdit:getText()
                    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_EDIT_GROUPNAME_CLICK,{item_id = self._teaNumber})                    
                    if tonumber(inputNum) and #inputNum <= 4 then
                        self._groupName:setString(self._groupNameEdit:getText())
                        self._groupName:setVisible(true)
                        self._tmpInputName = self._groupNameEdit:getText()
                        XH.lobby:getModule("Im"):reqSetTeaInfo(self._teaNumber,self._groupNameEdit:getText())
                    elseif #inputNum > 4 then
                        if inputNum ~= data.szData then                        
                            XH.TipTool.showToast("长度超长，只能输入最多4个数字哦~")                                                           
                        end
                        self._groupNameEdit:setText(self._tmpInputName or data.szData)
                        self._groupName:setString(self._tmpInputName or data.szData)
                        self._groupName:setVisible(true)                         
                    else
                        XH.TipTool.showToast("只能输入数字哦")
                        self._groupNameEdit:setText(self._tmpInputName or data.szData)
                        self._groupName:setString(self._tmpInputName or data.szData)
                        self._groupName:setVisible(true)                        
                    end
                end
            end)
        end
    elseif data.scene == VIEWTYPE.EXITGROUP then
        self:updatePanelView(false,true,false)
    elseif data.scene == VIEWTYPE.DISMISSGROUP then
        self:updatePanelView(false,false,true)
    end
    self._scene = data.scene
end

function IMTeaHouseGroupTipsView:updatePanelView(state1,state2,state3)
    self._groupInfoPanel:setVisible(state1)
    self._exitPanel:setVisible(state2)
    self._DismissPanel:setVisible(state3)
end

function IMTeaHouseGroupTipsView:onTouchDisMissConFrim(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.lobby:getModule("Im"):reqCloseTeaHouse(self._teaNumber)
    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_DISMISS_TWICE_CONFIRM,{item_id = self._teaNumber})
    self:close()
end

function IMTeaHouseGroupTipsView:onTouchDismissBtn(send,eventType)
    local data = {scene = "DISMISSGROUP" }
    self:selectShowViewType(data)
    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_DISMISS_GROUP_CLICK,{item_id = self._teaNumber})
end

function IMTeaHouseGroupTipsView:onTouchExitBtn(send,eventType)
    local data = {scene = "EXITGROUP" }
    self:selectShowViewType(data)
end

function IMTeaHouseGroupTipsView:onTouchExitConFrim(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.lobby:getModule("Im"):reqQuitTeaHouse(self._teaNumber)
    self:close()
end

function IMTeaHouseGroupTipsView:onUpdateCost(event)
    if event.info and event.info.nTodaycost and event.info.nHistorycost then
        local todayCost = (event.info.nTodaycost)/(XH.areaData:getPropSmallRoomCardRatio() or 1)
        local historyCost = (event.info.nHistorycost)/(XH.areaData:getPropSmallRoomCardRatio() or 1)
        self._costText:setString(todayCost.."/"..historyCost)
    end
end

function IMTeaHouseGroupTipsView:onUpdatePlayerCnt(event)
    if event.info and event.info.nCnt and event.info.nTotal then
        self._memberNumtext:setString(event.info.nCnt.."/"..event.info.nTotal)
    end
end

function IMTeaHouseGroupTipsView:onTouchClose(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if self._scene == VIEWTYPE.DISMISSGROUP then
        XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_DISMISS_TWICE_CLOSE)
    else
        XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_TIP_CLOSE_CLICK)
    end
    self:close()
end

function IMTeaHouseGroupTipsView:onTouchCancel(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if self._scene == VIEWTYPE.DISMISSGROUP then
        XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_DISMISS_TWICE_CANCEL,{item_id = self._teaNumber})
    end
    self:close()
end

return IMTeaHouseGroupTipsView   $  