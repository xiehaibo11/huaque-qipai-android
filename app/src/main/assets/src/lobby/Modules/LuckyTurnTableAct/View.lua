--转盘活动界面
local LuckyTurnTableActView = class("LuckyTurnTableActView", XH.ViewBase)
local ToponActConfig = require("lobby.Modules.ToponAct.Config")
local LuckyTurnTableActConfig = require("lobby.Modules.LuckyTurnTableAct.Config")
LuckyTurnTableActView.LIGHT_ANI_PATH = "animation/Lobby/Base/lucky_turn_table/Ios_zhuanpan/Ios_zhuanpan.ExportJson"
LuckyTurnTableActView.WIN_ANI_PATH = "animation/Lobby/Base/lucky_turn_table/Ios_zhuanpan_guang/Ios_zhuanpan_guang.ExportJson"

local KW_ITEM_ANGLE = 36    -- 圆盘共10格，每格36度

function LuckyTurnTableActView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/LuckyTurnTableAct/LuckyTurnTableLayer.csb"
end

function LuckyTurnTableActView:getBindingInfo()
    return {
        ["_KW_BTN_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KW_BTN_RULE"] = { varName = "_btnActRule", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventActRule" },
        ["_KW_BTN_AWARD_CENTER"] = { varName = "_btnAwardCenter", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventAwardCenter" },
        ["_KW_BTN_SHARE"] = { varName = "_btnShare", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventShare" },
        ["_KW_BTN_MORE"] = { varName = "_btnMore", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventMore" },
        ["_KW_BTN_START"] = { varName = "_btnStart", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventStart" },
        ["_KW_UI_ARROW"] = { varName = "_uiArrow" },
        ["_KW_IMG_IN"] = { varName = "_turnTable" },
        ["_KW_TIP_SHARE"] = { varName = "_shareTips" },
        ["_KW_UI_LIGHT_ANI"] = { varName = "_lightAni" },
        ["_KW_UI_WIN_ANI"] = { varName = "_winAni" },
        ["_KW_ITEM_"] = { beginIndex = 1, endIndex = 10, varName = "_award"},
        ["_KW_TXT_FINISH"] = { varName = "_txtFinish" },
        ["_KW_TXT_TOTAL"] = { varName = "_txtTotalCount" },
        ["_KW_TXT_DRAW"] = { varName = "_txtChance" },
        ["_KW_TXT_MAX"] = { varName = "_txtMaxChance" },
        ["_KW_TXT_CONDITION_"] = { beginIndex = 1, endIndex = 4, varName = "_condition"},
        ["_KW_PANEL_HORSE_"] = { beginIndex = 1, endIndex = 2, varName = "_horsePanel"},
        ["_KW_PANEL_HORSE_LAMP"] = { varName = "_horseLamp" },
        ["_KW_BTN_AWARD_KGG"] = { varName = "_btnAwardKgg", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventAwardKgg" },
        ["_KW_BOTTOM_PANEL"] = { varName = "_bottomPanel" },
    }
end

function LuckyTurnTableActView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("LuckyTurnTableAct"), eventKeyName = "EVENT_UPDATE_AWARD", callBack = "updateAwards"},
        { module = XH.lobby:getModule("LuckyTurnTableAct"), eventKeyName = "EVENT_SHOW_GOT_AWARD", callBack = "showGotAward"},
        { module = XH.lobby:getModule("LuckyTurnTableAct"), eventKeyName = "EVENT_GET_ACT_DATA", callBack = "onGetActData"},
        { module = XH.lobby:getModule("LuckyTurnTableAct"), eventKeyName = "EVENT_UPDATE_CONDITION", callBack = "onGetActCondition"},
        { module = XH.lobby:getModule("LuckyTurnTableAct"), eventKeyName = "EVENT_WATCH_ADVERTISEMENT", callBack = "onWatchAdvertisement"},
        { module = XH.sdkManager, eventKeyName = "EVENT_TOPONSDK_CALLBACK", callBack = "onToponAdsShowCallBack" },
    }
end

function LuckyTurnTableActView:ctor(param)
    cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/hall/Image/Activity/LuckyTurnTableAct/activity_luckyTurnTable.plist")
    param = param or {}
	LuckyTurnTableActView.super.ctor(self, param)
    self:initData()
    self:initView()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.LUCKY_TURN_TABLE_PUSH)
end

function LuckyTurnTableActView:initData()
    self._chances = 0
    self._totalChances = 0
    self._finish = 0
    self._totalFinish = 0
    self._awardsData = nil
    self._award = nil
    self._webType = nil
    XH.lobby:getModule("LuckyTurnTableAct"):reqActData()
    XH.lobby:getModule("LuckyTurnTableAct"):reqActPrizePoolData()
end

function LuckyTurnTableActView:initView()
    XH.lobby:getModule("LuckyTurnTableAct"):reqActCondition()
    -- self:showShareTips()
    self:showStandbyAni()
    self:setHorseAwardText()
    self:showHorseAni()
end

function LuckyTurnTableActView:onTouchEventClose(send, eventType)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.LUCKY_TURN_TABLE_CLOSE_CLICK)
    self:close()
end

function LuckyTurnTableActView:onTouchEventActRule(send, eventType)
    -- TODO 活动规则
    XH.viewManager:openView("LuckyTurnTableActHelpView")
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.LUCKY_TURN_TABLE_RULE_CLICK)
end

function LuckyTurnTableActView:onTouchEventAwardCenter(send, eventType)
    XH.viewManager:openView("AwardCenterView")
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.LUCKY_TURN_TABLE_AWARD_CENTER_CLICK)
    self:close()
end

function LuckyTurnTableActView:onTouchEventShare(send, eventType)
    -- TODO 分享
    XH.lobby:getModule("Lobby"):onTouchShare()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.LUCKY_TURN_TABLE_SHARE_CLICK)
    self:close()
end

function LuckyTurnTableActView:onTouchEventMore(send, eventType)
    -- TODO 更多福利
    XH.viewManager:openView("TabsActivityView")
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.LUCKY_TURN_TABLE_MORE_CLICK)
    self:close()
end

function LuckyTurnTableActView:onTouchEventStart(send, eventType)
    if self._chances <= 0 then
        XH.TipTool.showToast("抽奖次数不足！")
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.LUCKY_TURN_TABLE_START_CLICK)
        return
    end

    XH.lobby:getModule("LuckyTurnTableAct"):reqDrawPrize()
    self:showWinAni(false)
    self._btnStart:setEnabled(false)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.LUCKY_TURN_TABLE_START_CLICK)
end

function LuckyTurnTableActView:showShareTips()
    self._shareTips:stopAllActions()
    local opacity = self._shareTips:getOpacity()  
    if opacity == 255 then
        self._shareTips:setOpacity(0)
    else
        self._shareTips:runAction(cc.Sequence:create(cc.FadeIn:create(0.2), cc.DelayTime:create(5), cc.FadeOut:create(0.2)))
    end
end

function LuckyTurnTableActView:showStandbyAni()
    self:showLightAni("daiji")

    self._turnTable:stopAllActions()
    local actionTime = 1 + 1 / 3
    local angle = 5
    local actRotate = cc.Sequence:create(cc.RotateTo:create(actionTime, angle),cc.RotateTo:create(actionTime, 0))
    local repeatAct = cc.RepeatForever:create(actRotate)
    self._turnTable:runAction(repeatAct)
end

function LuckyTurnTableActView:showLightAni(AniName)
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

function LuckyTurnTableActView:showWinAni(bShow)
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
        XH.viewManager:openView("LuckyTurnTableActAwardView", nil, self._award)
        self._btnStart:setEnabled(true)
        self:showWinAni(false)
    end)
end

function LuckyTurnTableActView:showArrowAni()
    self._uiArrow:stopAllActions()
    local actionTime = 10 / 60
    local angle1 = 2
    local angle2 = -2
    local actRotate = cc.Sequence:create(cc.RotateTo:create(actionTime, angle1),cc.RotateTo:create(actionTime, angle2))
    local repeatAct = cc.Repeat:create(actRotate, 8)     -- 循环动画8次
    self._uiArrow:runAction(repeatAct)
end

function LuckyTurnTableActView:showTurnTableAni(offSetAngle)
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
                    end)
    local action = cc.Sequence:create(actRotate, actWinAward)
    self._turnTable:runAction(action)
end

function LuckyTurnTableActView:setHorseAwardText(data)
    if LuckyTurnTableActConfig.areaAward[XH.areaData:getLobbyID()] or data then
        local awardList = data or LuckyTurnTableActConfig.areaAward[XH.areaData:getLobbyID()]
        for key, val in pairs(awardList) do
            local awardText= XH.UITool.seekNodeByName(self._horseLamp, "_KW_TEXT_PROP_NAME_" .. key)
            if awardText then
                awardText:setString(val)
            end
        end
    end
end

function LuckyTurnTableActView:showHorseAni()
    local actionTime = 10
    local actionTimeFirst = 5
    local posX = self["_horsePanel1"]:getPositionX()
    local posY = self._horseLamp:getContentSize().height
    local actionFirst = cc.MoveTo:create(actionTimeFirst, cc.p(posX, posY))

--    local repeatAct1 = cc.RepeatForever:create(cc.Sequence:create(cc.MoveTo:create(actionTime, cc.p(posX, posY)), cc.CallFunc:create(function()
--        self["_horsePanel1"]:setPositionY(-posY)
--    end)))
    local sequenceAct1 = cc.Sequence:create(actionFirst, cc.CallFunc:create(function()
        self["_horsePanel1"]:setPositionY(-posY)
    end), cc.CallFunc:create(function()
        self["_horsePanel1"]:stopAllActions()
        self["_horsePanel1"]:runAction(cc.RepeatForever:create(cc.Sequence:create(cc.MoveTo:create(actionTime, cc.p(posX, posY)), cc.CallFunc:create(function()
        self["_horsePanel1"]:setPositionY(-posY)
    end))))
    end))
    self["_horsePanel1"]:runAction(sequenceAct1)

    local repeatAct2 = cc.RepeatForever:create(cc.Sequence:create(cc.MoveTo:create(actionTime, cc.p(posX, posY)), cc.CallFunc:create(function()
        self["_horsePanel2"]:setPositionY(-posY)
    end)))
    self["_horsePanel2"]:runAction(repeatAct2)
end

function LuckyTurnTableActView:updateAwards(event)
    if not event.awards then
        XH.TipTool.showToast("获取奖品信息失败，请尝试重新打开页面！")
        return
    end
    local awards = event.awards
    self._awardsData = awards
    for i = 1, #awards do
        if self["_award" .. i] then
            local awardPanel = XH.UITool.seekNodeByName(self["_award" .. i], "_KW_IMG_AWARD")
            if awardPanel then
                XH.UITool.reloadNodeRemoteImage(awardPanel, awards[i].url)
            end
            local value = XH.playerData:changeBindCardRate(awards[i].pid,awards[i].val)
            XH.UITool.setText(self["_award" .. i], "_KW_TXT_VALUE", value)
        end
    end
end

function LuckyTurnTableActView:showGotAward(event)
    if not event.info then
        return
    end
    local award = event.info
    self._award = award
    local tmpIdx = 0
    for i = 1, #self._awardsData do
        if award.ppid == self._awardsData[i].ppid then
            tmpIdx = i
            break
        end
    end
    if tmpIdx == 0 then
        return
    end
    -- 偏移角度
    local offSetAngle = 360 - ((tmpIdx - 1) * KW_ITEM_ANGLE)
    if offSetAngle == 0 then
        offSetAngle = 360
    end

    self:showLightAni("star")
    self:showArrowAni()
    self:showTurnTableAni(offSetAngle)

    XH.lobby:getModule("LuckyTurnTableAct"):reqActData()
end

function LuckyTurnTableActView:onGetActData(event)
    if not event.data then
        return
    end
    local data = event.data
    self._chances = data.leftDraw
    self._totalChances = data.totalDrawCount
    self._finish = data.finish
    self._totalFinish = data.totalTarget
    
    self._txtFinish:setText(self._finish)
    self._txtTotalCount:setText(self._totalFinish)
    self._txtChance:setText(self._chances)
    self._txtMaxChance:setText(self._totalChances)

    if data.advertisementCount then
        self._btnAwardKgg:setVisible(true)
        self._btnAwardKgg:setPositionX(341)
        self._btnAwardCenter:setPositionX(640)
        self._btnShare:setPositionX(940)
        --看广告抽奖次数为0，按钮置灰
        if data.advertisementCount == 0 then
            self._btnAwardKgg:setEnabled(true)
        else
            self._btnAwardKgg:setEnabled(false)
        end
    end
    self:initBottomBtnsPos()

    if data.awardList then
        self:setHorseAwardText(data.awardList)
    end
end

function LuckyTurnTableActView:onGetActCondition(event)
    if not event.data then
        return
    end
    local data = event.data
    for i = 1, #data do
        if self["_condition" .. i] then
            self["_condition" .. i]:setText(XH.StringTool.replaceMatchStr(data[i]))
        end
    end
end

function LuckyTurnTableActView:onTouchEventAwardKgg(send, eventType)
    if device.platform ~= "windows" then
        self._webType = ToponActConfig.SeeAdvertisement
        XH.sdkManager:addToponAds(XH.ThirdDefine.TOPONSDK_AD_TYPE.REWARDEVIDEO ,"show")
    else
        XH.lobby:getModule("LuckyTurnTableAct"):reqWatchAdvertisement()
        XH.lobby:getModule("LuckyTurnTableAct"):reqDrawPrize()
        self:showWinAni(false)
    end
end

-- 视频播放完成
function LuckyTurnTableActView:onToponAdsShowCallBack(event)
    local code = event.data.code
    if (code == XH.ThirdDefine.TOPONSDK_CALLBACK_CODE.TOPON_FLAG_SDK_CLOSE) then
        if self._webType == ToponActConfig.SeeAdvertisement then
            XH.lobby:getModule("LuckyTurnTableAct"):reqWatchAdvertisement()
        end
    end
end

function LuckyTurnTableActView:onWatchAdvertisement(event)
    XH.lobby:getModule("LuckyTurnTableAct"):reqDrawPrize()
    self:showWinAni(false)
    self._webType = nil
    self._btnAwardKgg:setEnabled(false)
    self._totalChances = self._totalChances + 1
    self._txtMaxChance:setText(self._totalChances)
end

function LuckyTurnTableActView:initBottomBtnsPos()
    if not self._bottomPanel then
        return
    end
    local children = self._bottomPanel:getChildren()
    if children then
        local visibleChildren = {}
        for _, v in pairs(children) do
            if v:isVisible() then
                table.insert(visibleChildren, v)
            end
        end
        if #visibleChildren > 0 then
            local bottomSize = self._bottomPanel:getContentSize()
            local everySize = bottomSize.width / #visibleChildren
            for i = 1, #visibleChildren do
                visibleChildren[i]:setPositionX(i * everySize - everySize / 2)
            end
        end
    end
end

return LuckyTurnTableActView >  