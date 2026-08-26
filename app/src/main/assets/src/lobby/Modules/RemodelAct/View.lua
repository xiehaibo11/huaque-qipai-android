local RemoteImage = XH.RemoteImage
local TeaHouseRemodelActView = class("TeaHouseRemodelActView", XH.ViewBase)

TeaHouseRemodelActView.LIGHT_ANI_PATH = "animation/Lobby/Base/lucky_turn_table/Ios_zhuanpan/Ios_zhuanpan.ExportJson"
TeaHouseRemodelActView.WIN_ANI_PATH = "animation/Lobby/Base/lucky_turn_table/Ios_zhuanpan_guang/Ios_zhuanpan_guang.ExportJson"

function TeaHouseRemodelActView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/RemodelAct/TeaHouseRemodelAct.csb"
end

function TeaHouseRemodelActView:getBindingInfo()
    return {
        ["_KW_REMODEL_ACT_BTN_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KW_REMODEL_ACT_BTN_RULE"] = { varName = "_btnRule", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventActRule" },
        ["_KW_REMODEL_PLAY_COUNT"] = { varName = "_playcount" },
        ["_KW_REMODEL_PLAYER_COUNT"] = { varName = "_leftcount" },
        ["_KW_PROP_"] = { beginIndex = 1, endIndex = 10, varName = "_propItem_"},
        ["_KW_BTN_GET_AWARD"] = { varName = "_reqGetAward", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventReqGetAward" },
        ["_KW_BTN_ACT_FINAL"] = { varName = "_reqGetFinalAward", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventReqGetFinalAward" },
        ["_KW_ANIM_LIGHT_SMALL"] = { varName = "_lightAni" },
        ["_KW_ANIM_LIGHT_BIG"] = { varName = "_winAni" },
        ["_KW_AWARD_PANEL"] = { varName = "_turnTable" },
        ["_KW_UI_ARROW"] = { varName = "_uiArrow" },
        ["_KW_REMODEL_ACT_PRO"] = { varName = "_activePro" },
        ["_KW_REMODEL_ACT_PRO_TEXT"] = { varName = "_activeProText" },
        ["_KW_BTN_AWARD_CENTER"] = { varName = "_btnAwardCenter", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventAwardCenter" },
        ["_KW_BTN_ACT_FINAL_ALREADY"] = { varName = "_btnAwardCenterAlready"},
    }
end

function TeaHouseRemodelActView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("RemodelAct"), eventKeyName = "EVENT_REMODEL_ACT_GETDATA", callBack = "onGetRemodelActDataSuccess" },
        { module = XH.lobby:getModule("RemodelAct"), eventKeyName = "EVENT_REMODEL_AWARD_DATA", callBack = "onGetRemodelAwardSuccess" },
        { module = XH.lobby:getModule("RemodelAct"), eventKeyName = "EVENT_REMODEL_FINALAWARD_DATA", callBack = "onGetRemodelFinalAward" },
    }
end

function TeaHouseRemodelActView:ctor(param)
    param = param or {}
	TeaHouseRemodelActView.super.ctor(self, param)
    self:initData()
    if param and param.teaId then
        self._nTeaId = param.teaId
        local taskSubId = XH.lobby:getModule("RemodelAct").RemodelTaskSubID["activityInfo"]
        self:getRemodelActData(param.teaId,taskSubId)
    end
    self:showStandbyAni()
end

function TeaHouseRemodelActView:initData()
    self._data = {}
    self._canUsedCount = 0
    self._propType = ""
    self._nTeaId = 0
    self._activeDay = 0
    self._actTotalDay = 0
    self._isFinishAct = false
end

function TeaHouseRemodelActView:updateRemodelActUI(data)
    if data["gameProcess"] then
        if data["gamePrizeCount"] and data["gamePrizeTotal"] and data["gamePrizeCount"] >= data["gamePrizeTotal"] then 
            self._playcount:setText("今日对局奖励已经达到上限")
        else
            self._playcount:setText(XH.StringTool.replaceMatchStr("亲友圈对局进度") .. data["gameProcess"] )
        end      
    end
    if data["playerCanUseCount"] then
        self._leftcount:setText(data["playerCanUseCount"] or 0)
    end

    if data["dayProcess"] and data["actTotalDay"] then
        self._activeDay = data["dayProcess"]
        local percent = data["dayProcess"]/data["actTotalDay"]
        self._activePro:setPercent((percent <=1 and percent or 1) * 100)
        if tonumber(data["dayProcess"]) >= tonumber(data["actTotalDay"]) then
            data["dayProcess"] = data["actTotalDay"]
        end
        self._activeProText:setText(data["dayProcess"] .. "/" .. data["actTotalDay"])
    end
    self._btnAwardCenterAlready:setVisible(self._isFinishAct)   
    self._reqGetFinalAward:setVisible(not self._isFinishAct) 
    if self._activeDay < self._actTotalDay  then
        self._reqGetFinalAward:setEnabled(false)
    else
        if not self._isFinishAct then
            self._reqGetFinalAward:setEnabled(true)
        end
    end
end

function TeaHouseRemodelActView:updateRemodelAwardUI(data)
    for i=1,10 do
        self:updatePropImg(self["_propItem_" .. i],data[i])
    end
end

function TeaHouseRemodelActView:updatePropImg(addNode,data)
    local imgNode = addNode:getChildByName("_KW_PROP_IMG")
    if imgNode then
        local size = imgNode:getContentSize()
        local propImg = RemoteImage.new()
                :setTouchEnabled(false)
                :setPosition(size.width/2 - 0.5, size.height/2 + 0.3)
                :ignoreContentAdaptWithSize(false)
                :setContentSize(size.width - 7, size.height - 7)
                :addTo(imgNode)
        propImg:setUrl(data.url)
    end   
    local textNode = addNode:getChildByName("_KW_PROP_NUM")
    if textNode then 
        local areaConfig = require("app.Config.AreaConfig")
        local lobbyid = XH.userDefault:getValue(XH.userDefault.KEY_ID.LOBBYID, 0)
        local freeCardId = areaConfig[lobbyid].propList.freeRoomCardID
        local spendFeeID = areaConfig[lobbyid].propList.spendFeeID
        if data.prop_id == freeCardId or data.prop_id == spendFeeID then
            textNode:setText(data.prop_value / XH.areaData:getPropFreeRoomCardRatio())
        else
            textNode:setText(data.prop_value)
        end
    end
end

function TeaHouseRemodelActView:onTouchEventClose(send, eventType)
    self:close()
end

function TeaHouseRemodelActView:onTouchEventActRule(send, eventType)
    XH.viewManager:openView("RemodelActRuleView")
end

function TeaHouseRemodelActView:onTouchEventAwardCenter(send, eventType)
    XH.viewManager:openView("AwardCenterView",XH.ZORDER.WINDOW )
end

function TeaHouseRemodelActView:onTouchEventReqGetAward(send, eventType)
    if self._canUsedCount > 0 then
        local taskSubId = XH.lobby:getModule("RemodelAct").RemodelTaskSubID["reqAward"]
        self:getRemodelActData(self._nTeaId,taskSubId)
        self:showWinAni(false)
        self._reqGetAward:setEnabled(false)
    else
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, XH.ConstString.getStr("SUMMER_ACT_INFO_NO_COUNT"))
    end
end

function TeaHouseRemodelActView:onTouchEventReqGetFinalAward(send, eventType)
    if self._isFinishAct then
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        },"您已经领取道具大礼包")
        return
    end
    if self._activeDay >= self._actTotalDay and self._actTotalDay > 0 then
        local taskSubId = XH.lobby:getModule("RemodelAct").RemodelTaskSubID["finalAward"]
        self:getRemodelActData(self._nTeaId,taskSubId)
    end
end

function TeaHouseRemodelActView:getRemodelActData(teaId,taskSubId)
    XH.lobby:getModule("RemodelAct"):reqRemodelActData(teaId,taskSubId)
end

function TeaHouseRemodelActView:onGetRemodelActDataSuccess(event)
    local data = event.data
    if next(data) then
        self._canUsedCount = data["playerCanUseCount"] or 0
        self._actTotalDay =  data["actTotalDay"]
        self._isFinishAct = data["isFinishAct"]
        self._activeDay = data["dayProcess"]
        self:updateRemodelActUI(data)
        if data.awardlist then
            self._awardlist = data.awardlist
            self:updateRemodelAwardUI(data.awardlist)
        end
    end
end

function TeaHouseRemodelActView:onGetRemodelAwardSuccess(event)
    local data = event.data
    if next(data) and data.status == 0 then
        local award = data.data
        self._award = award
        local tmpIdx = 0
        for i = 1, #self._awardlist do
            if (award.prop_id == self._awardlist[i].prop_id and award.prop_value == self._awardlist[i].prop_value) 
                or award.prize_name == self._awardlist[i].prize_name then
                tmpIdx = i
                break
            end
        end
        if tmpIdx == 0 then
            return
        end
        -- 偏移角度
        local offSetAngle = 360 - ((tmpIdx - 1) * 36)
        if offSetAngle == 0 then
            offSetAngle = 360
        end
    
        self:showLightAni("star")
        self:showArrowAni()
        self:showTurnTableAni(offSetAngle,data)
    end
end

local TASKRESPFLAG = {
    SUCCESS = 0,
    FAIL = 1,
}

function TeaHouseRemodelActView:onGetRemodelFinalAward(event)
    if event.data and  event.data.status and event.data.status == TASKRESPFLAG.SUCCESS then
        XH.viewManager:openView("RemodelActAwardView", nil, event.data.awardSet or {})
        self._btnAwardCenterAlready:setVisible(true)   
        self._reqGetFinalAward:setVisible(false) 
    elseif event.status == TASKRESPFLAG.FAIL then 
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        },"您已经领取过道具大礼包了")
    end
end

function TeaHouseRemodelActView:showStandbyAni()
    self:showLightAni("daiji")
    self._turnTable:stopAllActions()
    local actionTime = 1 + 1 / 3
    local angle = 5
    local actRotate = cc.Sequence:create(cc.RotateTo:create(actionTime, angle),cc.RotateTo:create(actionTime, 0))
    local repeatAct = cc.RepeatForever:create(actRotate)
    self._turnTable:runAction(repeatAct)
end

function TeaHouseRemodelActView:showLightAni(AniName)
    self._lightAni:stopAllActions()
    self._lightAni:removeAllChildren()
    if not cc.FileUtils:getInstance():isFileExist(self.LIGHT_ANI_PATH) then
        return
    end
    ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(self.LIGHT_ANI_PATH)
    local armature = ccs.Armature:create("Ios_zhuanpan")
    self._lightAni:addChild(armature)
    armature:setAnchorPoint(cc.p(0.5, 0.5))
    armature:setPosition(cc.p(0, 0))
    armature:getAnimation():play(AniName, -1, 1)
end

function TeaHouseRemodelActView:showWinAni(bShow)
    self._winAni:stopAllActions()
    self._winAni:removeAllChildren()
    if not bShow then
        return
    end
    if not cc.FileUtils:getInstance():isFileExist(self.WIN_ANI_PATH) then
        return
    end
    ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(self.WIN_ANI_PATH)
    local armature = ccs.Armature:create("Ios_zhuanpan_guang")
    self._winAni:addChild(armature)
    armature:setAnchorPoint(cc.p(0.5, 0))
    armature:setPosition(cc.p(0, 0))
    armature:getAnimation():play("zhongjiang_guang", -1, 0)
    armature:getAnimation():setMovementEventCallFunc(function()
        XH.viewManager:openView("RemodelActAwardView", nil, {self._award})
        self._reqGetAward:setEnabled(true)
        self:showStandbyAni()
        self:showWinAni(false)
    end)
end

function TeaHouseRemodelActView:showArrowAni()
    self._uiArrow:stopAllActions()
    local actionTime = 10 / 60
    local angle1 = 2
    local angle2 = -2
    local actRotate = cc.Sequence:create(cc.RotateTo:create(actionTime, angle1),cc.RotateTo:create(actionTime, angle2))
    local repeatAct = cc.Repeat:create(actRotate, 8)     -- 循环动画8次
    self._uiArrow:runAction(repeatAct)

    -- 刷新玩家道具信息
    XH.playerData:flushPlayerDrop()
    XH.playerData:flushGoldCoin()
end

function TeaHouseRemodelActView:showTurnTableAni(offSetAngle,data)
    self._turnTable:stopAllActions()
    self._turnTable:setRotation(0)
    local actionTime1 = 35 / 60     -- 根据动画帧率计算时间，60帧为1秒
    local actionTime2 = 75 / 60
    local actionTime3 = 70 / 60
    local angle1 = 180
    local angle2 = 900
    local angle3 = offSetAngle
    local actRotate = cc.Sequence:create(cc.RotateBy:create(actionTime1, angle1),cc.RotateBy:create(actionTime2, angle2),cc.RotateBy:create(actionTime3, angle3))
    local actWinAward = cc.CallFunc:create(function()
                        self:showLightAni("zhongjiang")
                        self:showWinAni(true)
                        self:updateRemodelActUI(data)
                    end)
    local action = cc.Sequence:create(actRotate, actWinAward)
    self._turnTable:runAction(action)
end

return TeaHouseRemodelActView  �0  