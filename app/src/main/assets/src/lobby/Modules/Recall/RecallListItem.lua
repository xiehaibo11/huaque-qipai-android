local RecallListItem = class("RecallListItem", XH.ViewBase)

function RecallListItem:getCSBPath()
    return "cocosStudio/hall/CSB/Recall/RecallListItem.csb"
end

function RecallListItem:getBindingInfo()
    return {
        ["_KW_PLAYER_ITEM"] = { varName = "_imgItem" },
        ["_KW_IMG_ICON_ALREADY_RECALL"] = { varName = "_iconAlreadyRecall" },
        ["_KW_TEXT_SUCC_RATE"] = { varName = "_textSuccRate" },
        ["_KW_BTN_INVITE"] = { varName = "_btnInvate", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventInvate" },
        ["_KW_BTN_GET_AWARD"] = { varName = "_btnGetAward", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventGetAward" },
        ["_KW_IMG_HEAD"] = { varName = "_imgHead" },
        ["_KW_IMG_FRAME"] = { varName = "_imgFrame" },
        ["_KW_TEXT_TIME"] = { varName = "_textTime" },
        ["_KW_ICON_AWARD"] = { varName = "_iconAward" },
        ["_KW_TEXT_AWARD_COUNT"] = { varName = "_textAwardCount" },
        ["_KW_TEXT_NAME"] = { varName = "_textName" },
        ["_KW_TEXT_NUMBER_ID"] = { varName = "_textNumberID" },
        ["_KW_IMG_HOT"] = { varName = "_imgHot" },
    }
end

function RecallListItem:ctor()
    RecallListItem.super.ctor(self)
    self:initView()
    self._userID = nil
    self._propID = nil
    self:initLeftTimeDelay()
end

function RecallListItem:onCleanup()
    self:initLeftTimeDelay()
    RecallListItem.super.onCleanup(self)
end

function RecallListItem:initView()
    if self._imgItem then
        self._imgItem:removeFromParent()
        self:addChild(self._imgItem)
    end
end

function RecallListItem:getSize()
    return cc.size(560, 150)
end

function RecallListItem:updateInfo(info, index)
    self._userID = info.user_id
    if info.recall_status == "RECALL_GOT_REWARD" then
        self:showStateAlreadyGetAward()
    elseif info.recall_status == "RECALL_RECALLED_OTHERS" then
        self:showStateRecalByOther()
    elseif info.recall_status == "RECALL_LOGIN_PLAY" then
        self:showStateWaitGetAward()
        self:setLeftTime(tonumber(info.expire_at))
    elseif info.recall_status == "RECALL_RECALLED" or info.recall_status == "RECALL_PLAY_REWARD" then
        self:showStatePlayOnce()
    end
    local playerRewards = nil
    if XH.lobby:getModule("Recall"):isTeahouseOwnerOrAdmin() then
        playerRewards = info.ownerRewards
    elseif info.rewards and #info.rewards >= 1 then
        playerRewards = info.rewards[1]
    end
    if playerRewards then
        self._propID = playerRewards.id
        self:setAward(playerRewards.count)
    end
    self:setIsHot(info.hot)
    self:setSuccessRate(info.successRate)
    self:setPlayerInfo(info.nick_name, info.user_id, info.head_icon)
end

function RecallListItem:setPlayerInfo(name, id, headUrl)
    self._textName:setText(XH.StringTool.cutStringByLength(name, 10))
    self._textNumberID:setText(tostring(id))
    -- 设置头像
    local headNode = XH.UIRemoteImage.create(self._imgHead)
    if headNode then
        headNode:setDefaultTexture("_0002_mrtx_m.png", ccui.TextureResType.plistType)
        headNode:setUrl(headUrl)
    end
end

function RecallListItem:setIsHot(isHot)
    self._imgHot:setVisible(isHot or false)
end

function RecallListItem:setSuccessRate(rate)
    self._textSuccRate:setText("成功率" .. rate .. "%")
end

function RecallListItem:initLeftTimeDelay()
    if self._leftTimeDelayID then
        self:_removeAllScheduleID(self._leftTimeDelayID)
    end
    self._leftTimeDelayID = nil
end

function RecallListItem:setLeftTime(leftTime)
    self:initLeftTimeDelay()
    if leftTime <= 0 then
        leftTime = 0
    end
    local hour = math.floor( leftTime/3600 )
    local min =  math.floor( (leftTime- hour*3600)/60 )
    local sec = leftTime - hour*3600 - min*60
    hour = hour > 9 and tostring(hour) or "0"..hour
    min = min > 9 and tostring(min) or "0"..min
    sec = sec > 9 and tostring(sec) or "0"..sec
    self._textTime:setText(hour..":"..min..":"..sec)

    if leftTime > 0 then
        self._leftTimeDelayID = self:performWithDelay(function ()
            self:setLeftTime(leftTime-1)
        end, 1)
    end
end

function RecallListItem:setAward(count)
    if self._propID == XH.areaData:getPropDiamndID() then
        self._iconAward:loadTexture("recall_res_icon_zs.png", ccui.TextureResType.plistType)
    elseif self._propID == XH.areaData:getPropRoomCardID() then
        self._iconAward:loadTexture("recall_res_icon_fk.png", ccui.TextureResType.plistType)
    else
        self._textAwardCount:setVisible(false)
        return
    end
    self._iconAward:ignoreContentAdaptWithSize(true)
    self._textAwardCount:setText("+"..count)
end

--待领奖
function RecallListItem:showStateWaitGetAward()
    self._textTime:setVisible(true)
    self._textAwardCount:setVisible(true)
    self._iconAlreadyRecall:setVisible(false)
    self._btnInvate:setVisible(false)
    self:setGetAwardBtnVisible(true, true)
end

--已领奖
function RecallListItem:showStateAlreadyGetAward()
    self._textTime:setVisible(false)
    self._textAwardCount:setVisible(true)
    self._iconAlreadyRecall:setVisible(false)
    self._btnInvate:setVisible(false)
    self:setGetAwardBtnVisible(true, false)
end

--已被他人召回
function RecallListItem:showStateRecalByOther()
    self._textTime:setVisible(false)
    self._textAwardCount:setVisible(false)
    self._iconAlreadyRecall:setVisible(true)
    self._btnInvate:setVisible(false)
    self:setGetAwardBtnVisible(false)
end

--邀请打一局
function RecallListItem:showStatePlayOnce()
    self._textTime:setVisible(false)
    self._textAwardCount:setVisible(true)
    self._iconAlreadyRecall:setVisible(false)
    self._btnInvate:setVisible(true)
    self:setGetAwardBtnVisible(false)
end

function RecallListItem:setGetAwardBtnVisible(visible, enable)
    self._btnGetAward:setVisible(visible or false)
    if visible then
        local btnRes = enable and "recall_res_btn_getaward.png" or "recall_res_bt_already_getaward.png"
        self._btnGetAward:loadTexture(btnRes, ccui.TextureResType.plistType)
        self._btnGetAward:setTouchEnabled(enable or false)
    end
end

function RecallListItem:onBtnEventInvate()
    XH.lobby:getModule("Recall"):shareWX()
    local conventionData = {}
    conventionData.item_id = self._userID
    conventionData.page = "P1104"
    if XH.SceneManager:getInstance():getTopSceneTag() == XH.SCENE_TAG.LOBBY then
        conventionData.page = "P1063"
    end
    
    local exposureMode = 2;
    if self:isUserInTopFive(self._userID, XH.lobby:getModule("Recall"):getPlayerList()) then
        exposureMode = 1;
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RECALL_LISTITEM_PLAYONCE,conventionData, {area_id = XH.areaData:getAreaID(), exposure_mode = exposureMode})

    -- 抛送打一局按钮数据
    local sceneName = XH.lobby:getModule("Im"):judgeSceneName()
    local data = {
        block_label = sceneName,
        item_id = string.format("%d_%d", XH.areaData:getAreaID(), self._userID)
    }
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.zh24052702, data)
end

function RecallListItem:isUserInTopFive(user_id, infoList)
    for i = 1, math.min(5, #infoList) do
        if infoList[i].user_id == user_id then
            return true
        end
    end
    return false
end

function RecallListItem:onBtnEventGetAward()
    XH.lobby:getModule("Recall"):getAward(self._userID, self._propID)
    local conventionData = {}
    conventionData.item_id = self._userID
    conventionData.page = "P1104"
    if XH.SceneManager:getInstance():getTopSceneTag() == XH.SCENE_TAG.LOBBY then
        conventionData.page = "P1063"
    end
    local exposureMode = 2;
    if self:isUserInTopFive(self._userID, XH.lobby:getModule("Recall"):getPlayerList()) then
        exposureMode = 1;
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RECALL_LISTITEM_GETAWARD,conventionData, {area_id = XH.areaData:getAreaID(), exposure_mode = exposureMode})
end

return RecallListItem   