local UIFunction = {}

function UIFunction.seekWidgetByName(root, strKeyword)    
    if root == nil then
        return nil
    end
    return ccui.Helper:seekWidgetByName(root, strKeyword)
end

function UIFunction.findNodeByName(root, strKeyword)    
    if root == nil then
        return nil
    end

    local getName = function()
        return root:getName()
    end

    local success, name = pcall(getName)

    if success == false then
        return nil
    end

    if name == strKeyword then
        return root
    end

    local node = nil
    local childrennode = root:getChildren()
    for i = 1, root:getChildrenCount() do
        node = UIFunction.findNodeByName(childrennode[i], strKeyword)
        if node ~= nil then
            break
        end
    end
    return node
end

--封装监听函数
function UIFunction.addTouchEventListener(node, name, func)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:addTouchEventListener(func)
        return true
    end
    return false
end

function UIFunction.addClickEventListener(node, name, func)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:addClickEventListener(func)
        return true
    end
    return false
end

--封装监听函数
function UIFunction.addEventListener(node, name, func)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:addEventListener(func)
        return true
    end
    return false
end

function UIFunction.loadTexture(node, name, pngKeyword, type)
    if node == nil then
        return
    end
    if pngKeyword == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then

        findNode:loadTexture(string.format("%s", pngKeyword), type)
        return true
    end
    return false
end


function UIFunction.setVisible(node, name, visible)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:setVisible(visible)
        return true
    end
    return false
end

function UIFunction.isVisible(node, name)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        if findNode:isVisible() then
            return true
        else
            return false
        end
    end
    return false
end

function UIFunction.setColor(node, name, color3b)
    if node == nil then
        return
    end
    local findNode = ccui.Helper:seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:setColor(color3b)
        return true
    end
    return false
end

function UIFunction.setTextColor(node, name, color3b)
    if node == nil then
        return
    end
    local findNode = ccui.Helper:seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:setTextColor(color3b)
        return true
    end
    return false
end

function UIFunction.setText(node, name, string)
    if node == nil then
        return
    end
    local text = UIFunction.seekWidgetByName(node, name)
    if text ~= nil then
        text:setString(string)
        return true
    end
    return false
end

function UIFunction.setEditBoxText(node, name, string)
    if node == nil then
        return
    end
    local text = UIFunction.seekWidgetByName(node, name)
    if text ~= nil then
        text:setText(string)
        return true
    end
    return false
end

function UIFunction.setPercent(node, name, percent)
    if node == nil then
        return
    end
    local slider = UIFunction.seekWidgetByName(node, name)
    if slider ~= nil then
        slider:setPercent(percent)
        return true
    end
    return false
end

function UIFunction.setFontSize(node, name, size)
    if node == nil then
        return
    end
    local text = UIFunction.seekWidgetByName(node, name)
    if text ~= nil then
        text:setFontSize(size)
        return true
    end
    return false
end

function UIFunction.setFntFile(node, name, string)
    if node == nil then
        return
    end
    local text = UIFunction.seekWidgetByName(node, name)
    if text ~= nil then
        text:setFntFile(string)
        return true
    end
    return false
end

function UIFunction.getInputText(node, name)
    if node == nil then
        return ""
    end
    local text = UIFunction.seekWidgetByName(node, name)
    if text ~= nil then
        return text:getText()
    end
    return ""
end

function UIFunction.setInputText(node, name, txt)
    if node == nil or name == nil or txt == nil then
        return
    end
    local text = UIFunction.seekWidgetByName(node, name)
    if text ~= nil then
        text:setText(txt)
    end
end

function UIFunction.getText(node, name)
    if node == nil then
        return ""
    end
    local text = UIFunction.seekWidgetByName(node, name)
    if text ~= nil then
        return text:getString()
    end
    return ""
end

function UIFunction.setOpacity(node, name, opacity)
    if node == nil then
        return
    end
    local text = UIFunction.seekWidgetByName(node, name)
    if text ~= nil then
        return text:setOpacity(opacity)
    end
    return ""
end

function UIFunction.setPositionX(node, name, x)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:setPositionX(x)
    end
    return
end

function UIFunction.getPositionX(node, name)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        return findNode:getPositionX()
    end
    return 0
end

function UIFunction.setPositionY(node, name, y)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:setPositionY(y)
    end
    return
end

function UIFunction.setPosition(node, name, pos)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:setPosition(pos)
    end
    return
end

function UIFunction.getPositionY(node, name)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        return findNode:getPositionY()
    end
    return 0
end

function UIFunction.getPosition(node, name)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        return findNode:getPosition()
    end
    return 0, 0
end

function UIFunction.getContentSize(node, name)
    if node == nil then
        return {["height"] = 0, ["width"] = 0 }
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        return findNode:getContentSize()
    else
        return {["height"] = 0, ["width"] = 0 }
    end
end

function UIFunction.getNodeName(node, name)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        return findNode:getName()
    end
    return 0
end

function UIFunction.removeAllChildren(node, name)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:removeAllChildren()
    end
    return
end


function UIFunction.playCsbFrameAni(node, name, csb, beginIndex, endIndex, loop, calBackFun)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        local frameAni = cc.CSLoader:createTimeline(csb)
        findNode:stopAllActions()
        findNode:runAction(frameAni)

        if endIndex ~= nil then
            frameAni:gotoFrameAndPlay(beginIndex, endIndex, loop)
        else
            frameAni:gotoFrameAndPlay(beginIndex, loop)
        end

        if calBackFun ~= nil then
            frameAni:setFrameEventCallFunc(calBackFun)
        end
    end
end

--LWJ
function UIFunction.isBright(node, name)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then    
        return findNode:isBright()
    end
    return false
end

function UIFunction.setBrightEx(node, name, enabled)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then    
        findNode:setBright(enabled)
        findNode:setEnabled(enabled)
        return true
    end
    return false
end

function UIFunction.playNodeCsbFrameAniEx(node, csb, beginIndex, endIndex, loop, calBackFun)
    if node ~= nil then
        local frameAni = cc.CSLoader:createTimeline(csb)
        node:runAction(frameAni)
        if endIndex ~= nil then
            frameAni:gotoFrameAndPlay(beginIndex, endIndex, loop)
        else
            frameAni:gotoFrameAndPlay(beginIndex, loop)
        end

        if calBackFun ~= nil then
            frameAni:setFrameEventCallFunc(calBackFun)
        end
    end
end

function UIFunction.gotoCsbFrameAndPause(node, name, csb, nIndex)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        local frameAni = cc.CSLoader:createTimeline(csb)
        findNode:stopAllActions()
        findNode:runAction(frameAni)
        frameAni:gotoFrameAndPause(nIndex)
    end
end

function UIFunction.setBright(node, name, enabled)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:setBright(enabled)
        return true
    end
    return false
end
function UIFunction.stopPlayCsbFrameAni(node, name, csb)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        node:stopAllActions()
    end
end


function UIFunction.setTouchEnabled(node, name, enabled)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:setTouchEnabled(enabled)

        return true
    end
    return false
end

function UIFunction.setBtnSelect(node, name, bSel)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then    
        findNode:setSelected(bSel)
        return true
    end
    return false
end

function UIFunction.setAnchorPointNode(node, posx, posy)
    if node then
        node:setAnchorPoint(cc.p(posx, posy))
    end
end

function UIFunction.playAniWithTargetPos(lastArmature, targetPosLayer, targetPosAniKey, aniPath, armatureKeyWord, aniKeyWord, loop, callBackFunc)
    if aniPath == nil then
        return
    end
    if armatureKeyWord == nil then
        return
    end
    if aniKeyWord == nil or aniKeyWord == "" then
        return
    end

    if lastArmature == nil then
        local findNode = UIFunction.seekWidgetByName(targetPosLayer, targetPosAniKey)
        if findNode == nil then
            return
        end

        ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(aniPath)
        lastArmature = ccs.Armature:create(armatureKeyWord)
        if lastArmature then
            findNode:addChild(lastArmature)
            lastArmature:setAnchorPoint(cc.p(0.5, 0.5));
        end
    end

    if lastArmature ~= nil then
        lastArmature:getAnimation():play(aniKeyWord, -1, loop)
        if callBackFunc ~= nil then
            lastArmature:getAnimation():setMovementEventCallFunc(callBackFunc)
        end
        return lastArmature
    end

end

function UIFunction.playNodeAniWithTargetPos(targetPosLayer, pos, aniPath, armatureKeyWord, aniKeyWord, loop, callBackFunc)
    if aniPath == nil then
        return
    end

    if armatureKeyWord == nil then
        return
    end
    if aniKeyWord == nil or aniKeyWord == "" then
        return
    end

    ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(aniPath)
    local lastArmature = ccs.Armature:create(armatureKeyWord)
    if lastArmature then
        targetPosLayer:addChild(lastArmature)
        lastArmature:setAnchorPoint(cc.p(0.5, 0.5))
        lastArmature:setPosition(pos)
    end

    if lastArmature ~= nil then
        lastArmature:getAnimation():play(aniKeyWord, -1, loop)
        if callBackFunc ~= nil then
            lastArmature:getAnimation():setMovementEventCallFunc(callBackFunc)
        end
        return lastArmature
    end
end

function UIFunction.playAniWithTargetPosNew(lastArmature, targetPosLayer, targetPosAniKey, aniPath, armatureKeyWord, aniKeyWord, loop, callBackFunc)
    if aniPath == nil then
        return
    end
    if not cc.FileUtils:getInstance():isFileExist(aniPath .. armatureKeyWord .. ".ExportJson") then
        return
    end
    if armatureKeyWord == nil then
        return
    end
    if aniKeyWord == nil or aniKeyWord == "" then
        return
    end
    if targetPosAniKey == nil or targetPosAniKey == "" then
        return
    end

    if lastArmature == nil then
        local findNode = UIFunction.seekWidgetByName(targetPosLayer, targetPosAniKey)
        if findNode == nil then
            return
        end

        local pngPath = aniPath .. armatureKeyWord .. ".png"
        local plistPath = aniPath .. armatureKeyWord .. ".plist"
        local jsonPath = aniPath .. armatureKeyWord .. ".ExportJson"
        ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(pngPath, plistPath, jsonPath)
        local lastArmature1 = ccs.Armature:create(armatureKeyWord)
        if lastArmature1 then
            findNode:addChild(lastArmature1)
            lastArmature1:setAnchorPoint(cc.p(0.5, 0.5))
            lastArmature1:getAnimation():play(aniKeyWord, -1, 1)
        end    
    end
end

function UIFunction.playAniWithTargetPosChanged(lastArmature, targetPosLayer, targetPosAniKey, aniPath, armatureKeyWord, aniKeyWord, loop, callBackFunc)
    if aniPath == nil then
        return
    end
    if not cc.FileUtils:getInstance():isFileExist(aniPath .. armatureKeyWord .. ".ExportJson") then
        return
    end
    if armatureKeyWord == nil then
        return
    end
    if aniKeyWord == nil or aniKeyWord == "" then
        return
    end
    if targetPosAniKey == nil or targetPosAniKey == "" then
        return
    end

    if lastArmature == nil then
        local findNode = UIFunction.seekWidgetByName(targetPosLayer, targetPosAniKey)
        if findNode == nil then
            return
        end
        local index = 0
        while true do
            if cc.FileUtils:getInstance():isFileExist(aniPath .. armatureKeyWord .. index .. ".plist") then
                ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(
                aniPath .. armatureKeyWord .. index .. ".png",
                aniPath .. armatureKeyWord .. index .. ".plist",
                aniPath .. armatureKeyWord .. ".ExportJson")
            else
                break
            end
            index = index + 1
        end
        local lastArmature1 = ccs.Armature:create(armatureKeyWord)
        if lastArmature1 then
            findNode:addChild(lastArmature1)
            lastArmature1:setAnchorPoint(cc.p(0.5, 0.5))
            lastArmature1:getAnimation():play(aniKeyWord, -1, 1)
        end    
    end
end

function UIFunction.autoMoveTo(node, name, posX, posY, time, callBackFunc)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:stopAllActions()
        local moveAction = cc.MoveTo:create(time, cc.p(posX, posY))
        if callBackFunc ~= nil then
            findNode:runAction(cc.Sequence:create(moveAction, cc.CallFunc:create(callBackFunc)))
        else
            findNode:runAction(cc.Sequence:create(moveAction))
        end
    end
end

function UIFunction.getWorldPosition(node, name)
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        return findNode:getWorldPosition()
    end
    return 0
end

function UIFunction.moveNode(moveNode, targetNode)
    if moveNode == nil then
        return
    end

    if targetNode == nil then
        return
    end

    moveNode:retain()
    moveNode:removeFromParent(false)
    targetNode:addChild(moveNode)
    moveNode:release()
end

function UIFunction.isEnabled(node, name)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        return findNode:isEnabled()
    end
    return false
end

function UIFunction.setButtonState(node, name, enable)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:setEnabled(enable)
        findNode:setTouchEnabled(enable)
        findNode:setBright(enable)
    end
    return
end

function UIFunction.adapt9Box(node)
    if node == nil then
        return
    end
    local glView = cc.Director:getInstance():getOpenGLView()
    local screenSize = cc.Director:getInstance():getWinSize()
    if glView:getResolutionPolicy() == cc.ResolutionPolicy.FIXED_WIDTH then
        local designSize = cc.size(960, 640)
        local scaleY = screenSize.height / designSize.height
        local kuangaobi = (screenSize.width / screenSize.height) / (designSize.width / designSize.height)
        node:setScaleY(scaleY)
        local childrenNode = node:getChildren()
        for i = 1, #childrenNode do
            if kuangaobi < 1 then
                childrenNode[i]:setScaleX(scaleY * kuangaobi)
                childrenNode[i]:setScaleY(kuangaobi)
            else
                childrenNode[i]:setScaleX(scaleY)
            end
        end
    elseif glView:getResolutionPolicy() == cc.ResolutionPolicy.FIXED_HEIGHT then
        local designSize = cc.size(640, 960)
        local scaleX = screenSize.width / designSize.width
        local kuangaobi = (designSize.width / designSize.height) / (screenSize.width / screenSize.height)
        node:setScaleX(scaleX)
        local childrenNode = node:getChildren()
        for i = 1, #childrenNode do
            if kuangaobi < 1 then
                childrenNode[i]:setScaleY(scaleX * kuangaobi)
                childrenNode[i]:setScaleX(kuangaobi)
            else
                childrenNode[i]:setScaleY(scaleX)
            end
        end
    end
end

function UIFunction.adapt9BoxOnlyChild(node)
    local glView = cc.Director:getInstance():getOpenGLView()
    local screenSize = cc.Director:getInstance():getWinSize()
    local designSize = UIFunction.getDesignSize()
    local scaleY = screenSize.height / designSize.height
    local kuangaobi = (screenSize.width / screenSize.height) / (designSize.width / designSize.height)
    if glView:getResolutionPolicy() == cc.ResolutionPolicy.FIXED_WIDTH then
        local childrenNode = node:getChildren()
        for i = 1, #childrenNode do
            if kuangaobi < 1 then
                childrenNode[i]:setScaleX(scaleY * kuangaobi)
                childrenNode[i]:setScaleY(kuangaobi)
            else
                childrenNode[i]:setScaleX(scaleY)
            end
        end
    end
end

function UIFunction.getDesignSize()
    local screenSize = cc.Director:getInstance():getWinSize()
    if screenSize.width > screenSize.height then
        return cc.size(960, 640)
    else
        return cc.size(640, 960)
    end
end

function UIFunction.getTotalScaleY(node)
    local scaleY = 1
    while true do
        if node == nil then
            break
        end
        if node:getParent() == nil then
            break
        end
        node = node:getParent()
        scaleY = scaleY * node:getScaleY()
    end
    return scaleY
end

function UIFunction.setScaleX(node, name, scale)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:setScaleX(scale)
    end
end

function UIFunction.setScale(node, name, scale)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        findNode:setScaleX(scale)
        findNode:setScaleY(scale)
    end
end

function UIFunction.scheduleScriptOnceFunc(func, delay)
    local scheduleScriptID = 0
    local tempScheduleScriptID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(scheduleScriptID)
        func()
    end, delay, false)
    scheduleScriptID = tempScheduleScriptID
    return scheduleScriptID
end

function UIFunction.addTouchEventListenerByObj(node, name, obj, fun)
    if node == nil then
        return
    end
    if obj == nil then
        return
    end
    if fun == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode == nil then
        return
    end

    findNode:addTouchEventListener(function(send, eventType)
        fun(obj, send, eventType)
    end)
end

function UIFunction.onTouchEventByObj(node, name, func, eventType, obj)
    if node == nil then
        return
    end
    local findNode = UIFunction.seekWidgetByName(node, name)
    if findNode ~= nil then
        return func(obj, findNode, eventType)
    end
    return nil
end

function UIFunction.setImageByURL(imageView, Url, realSize, isDownLoad)--目前只能用于头像
    if imageView == nil or Url == nil or Url == "" then return end
    local imageName, _ = string.gsub(Url, "http://", "h")
    imageName, _ = string.gsub(imageName, "/", "_")
    imageName = "headCache/" .. imageName
    local imagePath = cc.FileUtils:getInstance():getWritablePath() .. imageName
    print("AvatarDataManagerimagePath:" .. imagePath)
    if cc.FileUtils:getInstance():isFileExist(imagePath .. ".png") == true then
        print("AvatarDataManagerimagePath:" .. imagePath .. ".png")
        local function foo()
            imageView:loadTexture(imagePath .. ".png", ccui.TextureResType.localType)
        end
        if not pcall(foo) then
            cc.FileUtils:getInstance():removeFile(imagePath .. ".png")
            print("AvatarDataManager：" .. imagePath .. ".png" .. "加载出错：" .. Url)
        else
            local size = imageView:getContentSize()
            imageView:setScale(realSize.width / size.width, realSize.height / size.height)
            return
        end
    elseif cc.FileUtils:getInstance():isFileExist(imagePath .. ".jpg") then
        print("AvatarDataManagerimagePath:" .. imagePath .. ".jpg")
        local function foo()
            imageView:loadTexture(imagePath .. ".jpg", ccui.TextureResType.localType)
        end
        if not pcall(foo) then
            cc.FileUtils:getInstance():removeFile(imagePath .. ".jpg")
            print("AvatarDataManager：" .. imagePath .. ".jpg" .. "加载出错：" .. Url)
        else
            local size = imageView:getContentSize()
            imageView:setScale(realSize.width / size.width, realSize.height / size.height)
            return
        end
    end
    if isDownLoad ~= false then
        print("AvatarDataManagerUrl:" .. Url .. "  imagePath:" .. imageName .. ".png")
        imageView:startDownload(Url, imageName .. ".png")
    end
end

function UIFunction.adaptBackground(picNode)
    if picNode == nil then
        return
    end
    local screenSize = cc.Director:getInstance():getWinSize()
    local PICSize = picNode:getContentSize()
    local designSize = cc.size(960, 640)
    local screenkuangaobi = (screenSize.width / screenSize.height) / (designSize.width / designSize.height)
    local compareHeight = PICSize.height * (screenSize.width / PICSize.width)
    if screenSize.height < compareHeight then
        designSize = cc.size(PICSize.width, 640)
        local kuangaobi = (screenSize.width / screenSize.height) / (designSize.width / designSize.height)
        if kuangaobi > 1 then
            picNode:setScaleY(kuangaobi * screenkuangaobi)
            picNode:setScaleX(kuangaobi * picNode:getScaleX())
        end
    elseif screenSize.height > PICSize.height then
        designSize = cc.size(960, PICSize.height)
        local kuangaobi = (screenSize.width / screenSize.height) / (designSize.width / designSize.height)
        picNode:setScaleY((1 / kuangaobi) * picNode:getScaleY())
        picNode:setScaleX(1 / kuangaobi)
    end
end

--适配iphonex 方法 继承方法

function UIFunction.adaptForIphoneX(node, startPosX)
    if node == nil then
        return
    end
    local designSize = UIFunction.getDesignSize()
    local safeSize = cc.size(designSize.width - startPosX * 2, designSize.height)

    local childrenNode = node:getChildren()
    for i = 1, #childrenNode do
        local nodePos = cc.p(childrenNode[i]:getPosition())
        local newPos = cc.p(startPosX + nodePos.x / designSize.width * safeSize.width, nodePos.y)
        childrenNode[i]:setPosition(newPos)
    end
end

-- 刘海屏设备适配函数，实现同adaptForIphoneX
function UIFunction.adaptNotchDevice(node, startPosX, hdSize)
    if node == nil then
        return
    end
    local designSize = UIFunction.getDesignSize()
    if hdSize then
        designSize = UIFunction.getDesignSizeHD()
    end
    local safeSize = cc.size(designSize.width - startPosX * 2, designSize.height)

    local childrenNode = node:getChildren()
    for i = 1, #childrenNode do
        local nodePos = cc.p(childrenNode[i]:getPosition())
        local newPos = cc.p(startPosX + nodePos.x / designSize.width * safeSize.width, nodePos.y)
        childrenNode[i]:setPosition(newPos)
    end
end

function UIFunction.getImageAddHeight(imgNode, screenHeightExceptImgNode)
    if not imgNode then
        return 0
    end

    if screenHeightExceptImgNode <= 0 then
        return 0
    end

    local screenSize = cc.Director:getInstance():getWinSize()
    local designSize = UIFunction.getDesignSizeHD()
    local scaleY = screenSize.height / designSize.height
    local kuangaobi = (screenSize.width / screenSize.height) / (designSize.width / designSize.height)
    if kuangaobi < 1 then
        local imgNodeSize = imgNode:getContentSize()
        local imgNodeHeight = (screenSize.height - screenHeightExceptImgNode * (scaleY * kuangaobi)) / (scaleY * kuangaobi)
        if imgNodeHeight - imgNodeSize.height < 0 then
            return 0
        end
        return imgNodeHeight - imgNodeSize.height --返回这个imgNode九格增加的长度
    else
        return 0
    end
end

function UIFunction.addContentSizeWithHeight(node, height)
    if not node then
        return
    end

    local contentSize = node:getContentSize()
    node:setContentSize(contentSize.width, contentSize.height + height)
end

function UIFunction.adjustChildrenPosition(parentNode, adjustTypeTabel)
    if not parentNode then
        return
    end

    if not adjustTypeTabel then
        return
    end
    local childrenNode = parentNode:getChildren()
    if childrenNode then
        for i = 1, #childrenNode do
            local childName = childrenNode[i]:getName()
            local splitTable = XH.GT.split(childName, "_")
            local adjustType = splitTable[#splitTable]
            if adjustType == adjustTypeTabel["top"] then
                UIFunction.adjustToTop(parentNode, childrenNode[i])
            elseif adjustType == adjustTypeTabel["center"] then
                UIFunction.adjustToCenter(parentNode, childrenNode[i])
            elseif adjustType == adjustTypeTabel["bottom"] then
                UIFunction.adjustToBottom(parentNode, childrenNode[i])
            end
        end
    end
end

function UIFunction.adjustToTop(parentNode, childNode)
    if not parentNode then
        return
    end
    if not childNode then
        return
    end
    local parentSize = parentNode:getContentSize()
    childNode:setPositionY(parentSize.height)
end

function UIFunction.adjustToCenter(parentNode, childNode)
    if not parentNode then
        return
    end
    if not childNode then
        return
    end
    local parentSize = parentNode:getContentSize()
    childNode:setPositionY(parentSize.height / 2)
end

function UIFunction.adjustToBottom(parentNode, childNode)
    if not parentNode then
        return
    end
    if not childNode then
        return
    end
    childNode:setPositionY(0)
end


--基于九宫适配基础上的反适配,通过放缩使该节点在X轴上铺满全屏
function UIFunction.adaptFullScreenX(node)
    if not node then
        return
    end
    local nodeSize = node:getContentSize()
    local screenSize = cc.Director:getInstance():getWinSize()
    local designSize = UIFunction.getDesignSizeHD()
    local realWidth = designSize.height / screenSize.height * designSize.width
    local realHeight = designSize.width / screenSize.width * designSize.height
    local kuangaobi = (screenSize.width / screenSize.height) / (designSize.width / designSize.height)
    local scaleRate = designSize.width / nodeSize.width
    if kuangaobi >= 1 then
        node:setScaleX(scaleRate / ((realHeight / designSize.height) * (designSize.width / realWidth)))
    else
        node:setScaleX(scaleRate / (realHeight / designSize.height / 2))
    end
end

--基于九宫适配基础上的反适配,通过放缩使该结点铺满全屏幕
function UIFunction.adaptFullScreen(node)
    if not node then
        return
    end
    local nodeSize = node:getContentSize()
    local screenSize = cc.Director:getInstance():getWinSize()
    local designSize = UIFunction.getDesignSizeHD()
    local realWidth = designSize.height / screenSize.height * designSize.width
    local realHeight = designSize.width / screenSize.width * designSize.height
    local kuangaobi = (screenSize.width / screenSize.height) / (designSize.width / designSize.height)
    local scaleRateX = designSize.width / nodeSize.width
    local scaleRateY = designSize.height / nodeSize.height
    if kuangaobi < 1 then
        node:setScaleX(scaleRateX / (realHeight / designSize.height / 2))
        node:setScaleY(scaleRateY / (realWidth / designSize.width / 2))
    else
        node:setScaleX(scaleRateX / ((realHeight / designSize.height) * (designSize.width / realWidth)))
        node:setScaleY(scaleRateY)
    end
end

function UIFunction.adaptBackgroundHD(picNode)
    if picNode == nil then
        return
    end
    local screenSize = cc.Director:getInstance():getWinSize()
    local designSize = UIFunction.getDesignSizeHD()
    local realWidth = designSize.height / screenSize.height * designSize.width
    local scaleRateX = (designSize.width / designSize.height) * (screenSize.height / screenSize.width)
    picNode:setScaleY(1 / (realWidth / designSize.width / 2))
    picNode:setScaleX(scaleRateX)
end

function UIFunction.adapt9BoxHD(node)
    if node == nil then
        return
    end
    local screenSize = cc.Director:getInstance():getWinSize()
    local designSize = UIFunction.getDesignSizeHD()
    local realWidth = designSize.height / screenSize.height * designSize.width
    local realHeight = designSize.width / screenSize.width * designSize.height
    local kuangaobi = (screenSize.width / screenSize.height) / (designSize.width / designSize.height)
    node:setScaleX(designSize.height / realHeight)
    node:setScaleY(designSize.width / realWidth)
    local childrenNode = node:getChildren()

    for i = 1, #childrenNode do
        if kuangaobi < 1 then
            childrenNode[i]:setScaleX(realHeight / designSize.height / 2)
            childrenNode[i]:setScaleY(realWidth / designSize.width / 2)
        else
            childrenNode[i]:setScaleX((realHeight / designSize.height) * (designSize.width / realWidth))
        end
    end
end

function UIFunction.getDesignSizeHD()
    local screenSize = cc.Director:getInstance():getWinSize()
    if screenSize.width > screenSize.height then
        return cc.size(1920, 1080)
    else
        return cc.size(1080, 1920)
    end
end

XH.GT.scheduleScriptOnceFunc = UIFunction.scheduleScriptOnceFunc
XH.GT.getDesignSize = UIFunction.getDesignSize
XH.GT.adapt9Box = UIFunction.adapt9Box

return UIFunction   �  