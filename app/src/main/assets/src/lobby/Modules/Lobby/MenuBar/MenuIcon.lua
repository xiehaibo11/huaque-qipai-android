local LobbyMenuIcon = class("LobbyMenuIcon", function ()
    return ccui.Layout:create()
end)

local LobbyConfig = require("lobby.Modules.Lobby.Config")

local DefaultSize = cc.size(105,105)

function LobbyMenuIcon:getIconInfo(iconID)
    if self._info.iconID and self._info.iconID == iconID then
        return self._info
    end
end

function LobbyMenuIcon.createIcon(iconID, bSmallIconBar)
    local iconConf = LobbyConfig.MenuIconConfig[tonumber(iconID)]
    if not iconConf then
        return nil
    end
    if iconConf.Plist then
        cc.SpriteFrameCache:getInstance():addSpriteFrames(iconConf.Plist)
    end
    local param = {
        iconResName = iconConf.IconResName, 
        bNeedRedPoint = iconConf.IsNeedRedPoint,
        bShowWithAni = iconConf.AniInfo ~= nil,
        bShowWithSpine = iconConf.SpineInfo ~= nil,
        spineInfo = iconConf.SpineInfo,
        aniInfo = iconConf.AniInfo,
        aniInfoEx = iconConf.AniInfoEx,
        bShowWithDargonBones = iconConf.DargonBonesInfo ~= nil,
        dargonBonesInfo = iconConf.DargonBonesInfo,
        iconResPath = iconConf.IconResPath,
        iconAniScale = iconConf.IconAniScale,
        redPointPos = iconConf.RedPointPos,
        OffSetX = iconConf.OffSetX,
        OffSetY = iconConf.OffSetY,
        progressInfo = iconConf.ProgeressInfo,
        iconSize = iconConf.iconSize,
        iconID = tonumber(iconID),
        iconScale = iconConf.IconScale,
    }
    if bSmallIconBar and iconConf.MiniIconResName then
        param.iconResName = iconConf.MiniIconResName
    end
    local menuItem = LobbyMenuIcon.new(param)
    menuItem = XH.UIButton.create(menuItem)
    menuItem:setName(tostring(iconID))
    return menuItem
end

function LobbyMenuIcon:ctor(info)
    self._info = info or {}
    self._tipTextType = nil
    self:setContentSize(DefaultSize)
    if XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_ACT_ICON_CLICKED, false, {key = info.iconID}) then
        self:initImageViewIcon(info.iconResPath, info.iconResName, info.iconScale)
    else
        self:initImageViewIcon(info.iconResPath, info.iconResName, info.iconScale)
        if info.bShowWithSpine then
            self:initSpineIcon(info.spineInfo, info.iconSize)
        end
        if info.bShowWithAni then
            self:initAniIcon(info.aniInfo, info.iconSize)
        end
        if info.bShowWithDargonBones then
            self:initDargonBonesIcon(info.dargonBonesInfo, info.iconSize)
        end
        if info.progressInfo then
            self:initProgressView(info.progressInfo)
        end
    end
    self:setAnchorPoint(display.CENTER)
end

function LobbyMenuIcon:initImageViewIcon(iconResPath, iconResName, iconScale)
    local isPlist = iconResName and iconResName ~= ""
    local isLocal = iconResPath and iconResPath ~= ""
    if not isPlist and not isLocal then
        return
    end
    if not self._imgIcon then
        self._imgIcon = ccui.ImageView:create()
        self._imgIcon:setTouchEnabled(false)
        self:addChild(self._imgIcon)
    end
    if isPlist then
        self._imgIcon:loadTexture(iconResName, ccui.TextureResType.plistType)
    elseif isLocal then
        self._imgIcon:loadTexture(iconResPath, ccui.TextureResType.localType)
    end
    if iconScale then
        self._imgIcon:setScale(iconScale)
    end
    local iconImgSize = self._imgIcon:getContentSize()
    self._imgIcon:setPosition(cc.p(iconImgSize.width/2,iconImgSize.height/2 + (self._info.OffSetY or 0)))
    self:setContentSize(iconImgSize)
end

function LobbyMenuIcon:removeImageViewIcon()
    if self._imgIcon then
        if self._imgIcon:getParent() then
            self._imgIcon:removeSelf()
        end
        self._imgIcon = nil
    end
end

function LobbyMenuIcon:initSpineIcon(aniInfo, iconSize)
    if iconSize then
        self:setContentSize(iconSize)
    end
    self:removeImageViewIcon()
    self:showSpineAni(aniInfo)
end

function LobbyMenuIcon:showSpineAni(aniInfo)
    if not aniInfo or not aniInfo.JsonPath or not aniInfo.AtlasPath or not aniInfo.ArmatureName then
        return
    end
    if not cc.FileUtils:getInstance():isFileExist(aniInfo.JsonPath) or not cc.FileUtils:getInstance():isFileExist(aniInfo.AtlasPath) then
        return
    end
    local params = {path = "", tex = aniInfo.JsonPath, ske = aniInfo.AtlasPath, armatureName = aniInfo.ArmatureName}
    local size = self:getContentSize()
    local armature = display.playDargonBonesSpine(params)
    if armature then
        self:addChild(armature)
        armature:setAnchorPoint(cc.p(0.5, 0.5))
        armature:setPosition(cc.p(size.width/2 + (self._info.OffSetX or 0), size.height/2 + (self._info.OffSetY or 0)))
    end
    if self._info.iconAniScale then
        armature:setScale(self._info.iconAniScale)
    end
end

function LobbyMenuIcon:initAniIcon(aniInfo, iconSize)
    if iconSize then
        self:setContentSize(iconSize)
    end
    self:removeImageViewIcon()
    self:showAni(aniInfo)
end

function LobbyMenuIcon:showAni(aniInfo)
    if not aniInfo or not aniInfo.File or not aniInfo.ArmatureName or not aniInfo.AniName then
        return
    end
    if not cc.FileUtils:getInstance():isFileExist(aniInfo.File) then
        return
    end
    ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(aniInfo.File)
    local armature = ccs.Armature:create(aniInfo.ArmatureName)
    self:addChild(armature)
    armature:setAnchorPoint(cc.p(0.5, 0.5))
    local size = self:getContentSize()
    armature:setPosition(cc.p(size.width/2 + (self._info.OffSetX or 0), size.height/2 + (self._info.OffSetY or 0)))
    armature:getAnimation():play(aniInfo.AniName, -1, 1)
    if self._info.iconAniScale then
        armature:setScale(self._info.iconAniScale)
    end
end

function LobbyMenuIcon:initDargonBonesIcon(dargonBonesInfo, iconSize)
    if iconSize then
        self:setContentSize(iconSize)
    end
    self:removeImageViewIcon()
    self:showDargonBonesIcon(dargonBonesInfo)
end

function LobbyMenuIcon:showDargonBonesIcon(dargonBonesInfo)
    if not dargonBonesInfo then
        return
    end
    local dargonBones = display.playDargonBonesAnimByTimes(dargonBonesInfo, 0)
    if not dargonBones then
        return
    end
    self:addChild(dargonBones)
    dargonBones:setAnchorPoint(cc.p(0.5, 0.5))
    local size = self:getContentSize()
    dargonBones:setPosition(cc.p(size.width/2.0 + (self._info.OffSetX or 0), size.height/2.0 + (self._info.OffSetY or 0)))
    if self._info.iconAniScale then
        dargonBones:setScale(self._info.iconAniScale)
    end
end

function LobbyMenuIcon:initRedPoint()
    if self._redPointSprite then
        return
    end
    local size = self:getContentSize()
    local iconSprite = cc.Sprite:create()
    local addPos = self._info.redPointPos or cc.p(60, 50)
    iconSprite:setPosition(cc.p(size.width/2 + addPos.x , size.height/2 + addPos.y))
    iconSprite:setSpriteFrame(LobbyConfig.RedPointResName)
    iconSprite:setVisible(false)
    self:addChild(iconSprite)
    self._redPointSprite = iconSprite
end

function LobbyMenuIcon:initProgressView(progressInfo)
    if progressInfo.Plist then
        cc.SpriteFrameCache:getInstance():addSpriteFrames(progressInfo.Plist)
    end
    self._progressNode = cc.Node:create()
    self:addChild(self._progressNode)
    local size = self:getContentSize()
    self._progressNode:setPosition(cc.p(size.width / 2 , size.height / 2))
    self._progressNode:setVisible(false)
    self._progressNode:setName("KW_NODE_PROGRESS")
    --创建一个图片精灵作为背景
	local spriteBg = cc.Sprite:createWithSpriteFrameName(progressInfo.markResName)
    self._progressNode:addChild(spriteBg)
    spriteBg:setPosition(progressInfo.markOffset)
	--创建一个进度条
    local sprite = cc.Sprite:createWithSpriteFrameName(progressInfo.barResName)
	local circleProgressBar = cc.ProgressTimer:create(sprite)
    circleProgressBar:setName("KW_PROGRESS")
    self._progressNode:addChild(circleProgressBar)
	circleProgressBar:setType(cc.PROGRESS_TIMER_TYPE_RADIAL)
    circleProgressBar:setReverseDirection(progressInfo.bReverseDirection)
    circleProgressBar:setPosition(progressInfo.barOffset)
	circleProgressBar:setPercentage(0)
    --显示进度文本
    local txtPercent = ccui.Text:create()
    txtPercent:setName("KW_TEXT_PERCENT")
    self._progressNode:addChild(txtPercent)
    txtPercent:setFontName(progressInfo.TextFont)
    txtPercent:setFontSize(progressInfo.TextFontSize)
    txtPercent:setColor(progressInfo.TextColor)
    txtPercent:setString("")
    txtPercent:setAnchorPoint(progressInfo.TextAnchorPoint)
    txtPercent:setPosition(progressInfo.TextOffset)
end

function LobbyMenuIcon:showProgressPercent(percent)
    if not percent or  not self._info.progressInfo then return end
    percent = math.floor(percent)
    if percent < 0 or percent >= 100 then
        self._progressNode:setVisible(false)
        return
    end
    self._progressNode:setVisible(true)
    local temPercent = percent
    if self._info.progressInfo.bReverseDirection then
        temPercent = 100 - temPercent
    end
    local circleProgressBar = self._progressNode:getChildByName("KW_PROGRESS")
    if circleProgressBar then
        circleProgressBar:setPercentage(temPercent)
    end
    local txtPercent = self._progressNode:getChildByName("KW_TEXT_PERCENT")
    if txtPercent then
        txtPercent:setString(string.format("%d%%", percent))
    end
end

function LobbyMenuIcon:showRedPoint()
    if not self._redPointSprite then
        self:initRedPoint()
    end
    if self._redPointSprite then
        self._redPointSprite:setVisible(true)
    end
end

function LobbyMenuIcon:hideRedPoint()
    if self._redPointSprite then
        self._redPointSprite:setVisible(false)
    end
end

function LobbyMenuIcon:removeTipText()
    if self._tipText then
        self._tipText:removeSelf()
        self._tipText = nil
    end
    if self._tipTextImg then
        self._tipTextImg:removeSelf()
        self._tipTextImg = nil
    end
end

function LobbyMenuIcon:startHideDelay(hideDelayTime)
    if not hideDelayTime or hideDelayTime <= 0 then
        return
    end
    if not self._tipTextImg then
        return
    end
    local animation = cc.Sequence:create(cc.FadeIn:create(0.2), cc.DelayTime:create(hideDelayTime) , cc.FadeOut:create(0.2), cc.CallFunc:create(function()
        self:removeTipText()
    end))
    self._tipTextImg:runAction(animation)
end

function LobbyMenuIcon:checkTipTextTypeChanged(tipTextType)
    if not tipTextType then
        return false
    end
    if self._tipTextType == nil or self._tipTextType ~= tipTextType then
        self:removeTipText()
    end
    self._tipTextType = tipTextType
end

local FlushFunctionTextImg = {
    [LobbyConfig.ICON_TIP_TEXT_TYPE.BOTTOM] = "flushBottomTextImg",
    [LobbyConfig.ICON_TIP_TEXT_TYPE.TOP] = "flushTopTextImg",
    [LobbyConfig.ICON_TIP_TEXT_TYPE.RIGHT] = "flushRightTextImg",
}

function LobbyMenuIcon:updateTipTextImg()
    if FlushFunctionTextImg[self._tipTextType] then
        self[FlushFunctionTextImg[self._tipTextType]](self)
    end
end

function LobbyMenuIcon:flushTopTextImg()
    if self._tipText == nil or self._tipTextImg == nil then return end
    local textSize = self._tipText:getContentSize()
    local imgHeight = textSize.height + 60
    local imgWidth = textSize.width + 60
    self._tipText:setPosition(cc.p(imgWidth / 2, imgHeight / 2))
    self._tipTextImg:setContentSize(cc.size(imgWidth, imgHeight))
end

function LobbyMenuIcon:flushBottomTextImg()
    if self._tipText == nil or self._tipTextImg == nil then return end
    local textSize = self._tipText:getContentSize()
    local orgImgSize = self._tipTextImg:getContentSize()
    local imgWidth = textSize.width + 60
    self._tipTextImg:setContentSize(cc.size(imgWidth, orgImgSize.height))
    self._tipText:setPositionX(imgWidth/2)
end

function LobbyMenuIcon:flushRightTextImg()
    if self._tipText == nil or self._tipTextImg == nil then return end
    local textSize = self._tipText:getContentSize()
    local orgImgSize = self._tipTextImg:getContentSize()
    local imgWidth = textSize.width + 60
    self._tipTextImg:setContentSize(cc.size(imgWidth, orgImgSize.height))
    self._tipText:setPositionX(imgWidth/2)
end

function LobbyMenuIcon:showTipText(tipType, text, hideDelayTime)
    if not tipType or not text or text == "" then
        return
    end
    self:checkTipTextTypeChanged(tipType)
    self:initTipText()
    self:setTipText(text, hideDelayTime)
end

function LobbyMenuIcon:setTipText(text, hideDelayTime)
    if not text or text == "" then
        return
    end
    self._tipText:setString(text)
    self:updateTipTextImg()
    self._tipTextImg:setVisible(true)
    self:startHideDelay(hideDelayTime)

    if #self._tipText:getString() < 30 and self._tipTextType == LobbyConfig.ICON_TIP_TEXT_TYPE.RIGHT then 
        local textSize = self._tipText:getContentSize()
        self._tipTextImg:setContentSize(textSize.width + 30, textSize.height + 30)
        self._tipText:setPosition(self._tipTextImg:getContentSize().width*0.51, self._tipTextImg:getContentSize().height*0.55)
    end
end

function LobbyMenuIcon:setTipTextImgVisible(bShow)
    if self._tipTextImg == nil or self._tipText == nil or not self._tipTextType then
        return
    end
    local textConf = LobbyConfig.TipTextConfig[self._tipTextType]
    if not textConf then
        return
    end
    local text = self._tipText:getString()
    if not text or text  == "" then
        return
    end
    self._tipTextImg:setVisible(bShow)
end

local FlushFunctionNames = {
    [LobbyConfig.ICON_TIP_TEXT_TYPE.BOTTOM] = "flushBottomTipText",
    [LobbyConfig.ICON_TIP_TEXT_TYPE.TOP] = "flushTopTipText",
    [LobbyConfig.ICON_TIP_TEXT_TYPE.RIGHT] = "flushRightTipText",
}

function LobbyMenuIcon:initTipText()
    if self._tipTextImg or not self._tipTextType then
        return
    end
    local textConf = LobbyConfig.TipTextConfig[self._tipTextType]
    if not textConf then
        return
    end
    local tipTextImg = ccui.ImageView:create()
    tipTextImg:loadTexture(textConf.BgResName, ccui.TextureResType.plistType)
    tipTextImg:setAnchorPoint(textConf.AnchorPoint)
    tipTextImg:setScale9Enabled(textConf.Scale9Enabled)
    if textConf.CapInsets then
        tipTextImg:setCapInsets(textConf.CapInsets)
    end
    self:addChild(tipTextImg)
    self._tipTextImg = tipTextImg

    local tipText = ccui.Text:create()
    tipText:setFontName(textConf.Font)
    tipText:setFontSize(textConf.FontSize)
    tipText:setColor(textConf.TextColor)
    tipText:setAnchorPoint(textConf.TextAnchorPoint)
    tipText:setPositionY(textConf.TextPositionY)
    tipTextImg:addChild(tipText)
    self._tipText = tipText

    if FlushFunctionNames[self._tipTextType] then
        self[FlushFunctionNames[self._tipTextType]](self)
    end
end

function LobbyMenuIcon:flushBottomTipText()
    if self._tipTextImg then
        local size = self:getContentSize()
        self._tipTextImg:setPosition(cc.p(size.width/2 -40 , size.height/2 + 40))
        self._tipTextImg:setContentSize(cc.size(0,80))
    end
end

function LobbyMenuIcon:flushTopTipText()
    if self._tipTextImg then
        local size = self:getContentSize()
        self._tipTextImg:setPosition(cc.p(size.width/2 -20 , -5))
    end
end

function LobbyMenuIcon:flushRightTipText()
    if self._tipTextImg then
        local size = self:getContentSize()
        self._tipTextImg:setPosition(cc.p(20 , size.height - 5))
    end
end

function LobbyMenuIcon:initTipButtonStyle(buttonStyle)
    if not self._tipTextImg then
        return
    end
    local btnConf = LobbyConfig.TipButtonConfig[buttonStyle]
    if not btnConf then
        return
    end
    if self._tipButton then
        self._tipButton:removeSelf()
        self._tipButton = nil
    end
    local tipBtn = ccui.Button:create(btnConf.NormalResName, btnConf.PressedResName, 
        btnConf.DisabledResName, ccui.TextureResType.plistType)
    tipBtn:setAnchorPoint(btnConf.AnchorPoint)
    tipBtn:setScale(btnConf.BtnScale)
    tipBtn:setVisible(true)
    tipBtn:setTitleFontSize(btnConf.FontSize)
    tipBtn:setTitleColor(btnConf.TextColor)
    tipBtn:setTitleFontName(btnConf.Font)
    self._tipTextImg:setSwallowTouches(false)
    self._tipButton = tipBtn
    self._tipTextImg:addChild(tipBtn)
end

function LobbyMenuIcon:setTipButtonText(text)
    if self._tipButton == nil then return end
    self._tipButton:setTitleText(text)
end

function LobbyMenuIcon:updateTipButton(buttonStyle)
    if self._tipText == nil or self._tipTextImg == nil or self._tipButton == nil then return end
    local btnConf = LobbyConfig.TipButtonConfig[buttonStyle]
    if not btnConf then
        return
    end
    local offsetX = btnConf.OffSetX
    local orgSize = self._tipTextImg:getContentSize()
    local height = self._tipButton:getContentSize().height + orgSize.height
    self._tipTextImg:setContentSize(cc.size(orgSize.width + offsetX, height + 40))
    local posY = height - self._tipText:getContentSize().height - self._tipButton:getContentSize().height / 2
    local posX = orgSize.width/2 + offsetX
    self._tipButton:setPosition(cc.p(posX, posY + btnConf.BtnPosY))
    posY = self._tipTextImg:getContentSize().height - self._tipButton:getContentSize().height
    local textConf = LobbyConfig.TipTextConfig[self._tipTextType]
    self._tipText:setPosition(cc.p(posX, posY + textConf.TextPositionY))
end

function LobbyMenuIcon:showButtonByStyle(buttonStyle, text)
    if not buttonStyle then
        return
    end
    self:initTipButtonStyle(buttonStyle)
    self:updateTipButton(buttonStyle)
    if text and text ~= "" then
        self:setTipButtonText(text)
    end
end

function LobbyMenuIcon:setButtonClickCallBack(handle)
    if self._tipButton == nil then return end
    local function callBack(send, eventType)
        handle(send, eventType)
    end
    self._tipButton:addTouchEventListener(callBack)
end

function LobbyMenuIcon:initTipImageStyle(style)
    if not self._tipTextImg then return end
    style = style or LobbyConfig.MenuIconTipImageStyle.STYPE_ONE
    local styleConf = LobbyConfig.MenuIconTipImage[style]
    if not styleConf then return end
    local img = cc.Sprite:create()
    img:setPosition(cc.p(styleConf.PositionX, self._tipTextImg:getContentSize().height / 2 - 10))
    img:setSpriteFrame(styleConf.resName)
    img:setAnchorPoint(styleConf.AnchorPoint)
    img:setVisible(true)
    self._tipTextImg:addChild(img)
    self._birdImg = img
    self._birdImgStyle = style
end

function LobbyMenuIcon:showTipImageStyle(imgStyle)
    if not imgStyle then
        return
    end
    self:initTipImageStyle(imgStyle)
    self:updateTipImage(imgStyle)
end

function LobbyMenuIcon:updateTipImage(imgStyle)
    if self._tipText == nil or self._tipTextImg == nil or self._tipButton == nil or self._birdImg == nil then return end
    local orgSize = self._tipTextImg:getContentSize()
    local imgConf = LobbyConfig.MenuIconTipImage[imgStyle]
    if not imgConf then
        return
    end
    local offsetX = imgConf.OffSetX
    self._tipTextImg:setContentSize(cc.size(orgSize.width + offsetX, orgSize.height))
    orgSize = self._tipTextImg:getContentSize()
    self._tipText:setPositionX(orgSize.width/2 + offsetX)
    self._tipButton:setPositionX(orgSize.width/2 + offsetX)
end

local FlushTipImageFunctionNames = {
    [LobbyConfig.ICON_TIP_TEXT_TYPE.BOTTOM] = "flushBottomTipImgRotation",
    [LobbyConfig.ICON_TIP_TEXT_TYPE.TOP] = "flushTopTipImageRotation",
    [LobbyConfig.ICON_TIP_TEXT_TYPE.RIGHT] = "flushRightTipImgRotation",
}

function LobbyMenuIcon:flushTipImageRotation()
   if FlushTipImageFunctionNames[self._tipTextType] then
        self[FlushTipImageFunctionNames[self._tipTextType]](self)
   end
end

function LobbyMenuIcon:flushTopTipImageRotation()
    local rotationY = 180
    local offsetX = 25
    local imgOffsetX = 40
    local orgPosX = -90
    if LobbyConfig.MenuIconTipImage[self._birdImgStyle] then
        orgPosX = LobbyConfig.MenuIconTipImage[self._birdImgStyle].PositionX or -90
    end
    if self._tipTextImg then
        self._tipTextImg:setRotationSkewY(rotationY)
        local children = self._tipTextImg:getChildren()
        for _,node in ipairs(children) do
            if node then
                node:setRotationSkewY(rotationY)
            end
        end
        local size = self:getContentSize()
        self._tipTextImg:setPositionX(size.width/2 + offsetX)
        local imgSize = self._tipTextImg:getContentSize()
        local posX = imgSize.width/2 - imgOffsetX
        self._tipText:setPositionX(posX)
        if self._tipButton then
            self._tipButton:setPositionX(posX)
        end
        if self._birdImg then
            self._birdImg:setPositionX(orgPosX + imgSize.width + self._birdImg:getContentSize().width )
        end
    end
end

function LobbyMenuIcon:flushBottomTipImgRotation()
    
end

function LobbyMenuIcon:flushRightTipImgRotation()
    
end

function LobbyMenuIcon:judgeRotateTipImg()
    if self._tipTextImg == nil then return false end
    if self._tipTextType == LobbyConfig.ICON_TIP_TEXT_TYPE.TOP then
        if self:getPositionX() > self._tipTextImg:getContentSize().width then
            return true
        end
    end
    return false
end

function LobbyMenuIcon:showExtendAnim()
    if self._extendAnim then
        self._extendAnim:setVisible(true)
    else
        self:initAniIconEx(self._info.aniInfoEx)
    end
end

function LobbyMenuIcon:hideExtendAnim()
    if self._extendAnim then
        self._extendAnim:setVisible(false)
    end
end

function LobbyMenuIcon:initAniIconEx(aniInfoEx)
    if self._extendAnim then
        return
    end
    if not aniInfoEx or not aniInfoEx.File or not aniInfoEx.ArmatureName or not aniInfoEx.AniName then
        return
    end
    if not cc.FileUtils:getInstance():isFileExist(aniInfoEx.File) then
        return
    end
    ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(aniInfoEx.File)
    local armature = ccs.Armature:create(aniInfoEx.ArmatureName)
    self:addChild(armature)
    armature:setAnchorPoint(cc.p(0.5, 0.5))
    local size = self:getContentSize()
    armature:setPosition(cc.p(size.width/2 + (aniInfoEx.OffSetX or 0), size.height/2 + (aniInfoEx.OffSetY or 0)))
    armature:getAnimation():play(aniInfoEx.AniName, -1, 1)
    if aniInfoEx.AniExScale then
        armature:setScale(aniInfoEx.AniExScale)
    end
    self._extendAnim = armature
end

function LobbyMenuIcon:getNewsAndNewAwardPos()
    local size = self:getContentSize()
    return cc.p(size.width/2 ,size.height)
end

function LobbyMenuIcon:playUpDownAction(target, isToDo)
    if not target then
        return
    end
    target:stopAllActions()
    if not isToDo then
        return
    end
    local actDelay = cc.DelayTime:create(3)
    local actMoveUp1 = cc.MoveBy:create(0.2,cc.p(0, 20))
    local actMoveUp2 = cc.MoveBy:create(0.1,cc.p(0, 10))
    local actMoveDown1 = cc.MoveBy:create(0.2,cc.p(0, -20))
    local actMoveDown2 = cc.MoveBy:create(0.1,cc.p(0, -10))
    local sequenceAction = cc.Sequence:create(actDelay,actMoveUp1,actMoveDown1,actMoveUp2,actMoveDown2)
    target:runAction(cc.RepeatForever:create(sequenceAction))
end

function LobbyMenuIcon:initTipNews()
    if self._tipNewsImg then
        return
    end
    local tipNewsImg = ccui.ImageView:create()
    tipNewsImg:loadTexture(LobbyConfig.TipNewsConfig.ResName, ccui.TextureResType.plistType)
    tipNewsImg:setAnchorPoint(LobbyConfig.TipNewsConfig.AnchorPoint)
    tipNewsImg:setPosition(self:getNewsAndNewAwardPos())
    self:addChild(tipNewsImg)
    self._tipNewsImg = tipNewsImg
end

function LobbyMenuIcon:showTipNews(bShow)
    if bShow == nil then
        bShow = true
    end
    self:initTipNews()
    self._tipNewsImg:setVisible(bShow)
end

function LobbyMenuIcon:initTipNewAward()
    if self._tipNewAwardImg then
        return
    end
    local tipNewAwardImg = ccui.ImageView:create()
    tipNewAwardImg:loadTexture(LobbyConfig.TipNewAwardConfig.ResName, ccui.TextureResType.plistType)
    tipNewAwardImg:setAnchorPoint(LobbyConfig.TipNewAwardConfig.AnchorPoint)
    tipNewAwardImg:setPosition(self:getNewsAndNewAwardPos())
    self:addChild(tipNewAwardImg)
    self._tipNewAwardImg = tipNewAwardImg
end

function LobbyMenuIcon:showTipNewAward(bShow)
    if bShow == nil then
        bShow = true
    end
    self:initTipNewAward()
    self._tipNewAwardImg:setVisible(bShow)
    self:playUpDownAction(self._tipNewAwardImg, bShow)
end

return LobbyMenuIcon�b  