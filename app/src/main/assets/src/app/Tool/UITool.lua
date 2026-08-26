local UITool = { }

--添加事件回调
function UITool.enableNodeEvents(self, node)
    if node.isNodeEventEnabled_ then
        return node
    end

    node._eventHandleScript = self
    node:registerScriptHandler(function(state)
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
    node.isNodeEventEnabled_ = true

    return node
end
--消除事件回调
function UITool.disableNodeEvents(node)
    node:unregisterScriptHandler()
    node.isNodeEventEnabled_ = false
    node._eventHandleScript = nil
    return node
end

function UITool.getSignedNumStr(num)
    if num > 0 then
        return '+' .. tostring(num)
    elseif num < 0 then
        return tostring(num)
    else
        return tostring(num)
    end
end

function UITool.safeCall(go, funcName, ...)
    if not go then
        print("ERROR:safeCall obj is nil:", funcName, debug.traceback())
        return
    end
    local func = go[funcName]
    if not go then
        print("ERROR:safeCall can't find func:", funcName, debug.traceback())
        return
    end

    return func(go, ...)
end

function UITool.generatePrefab(self, nameList, root, rootName, prefabSelf)
    local ret = prefabSelf or {}
    if not root then
        local rootNameEx = table.remove(nameList, 1)
        root = UITool.seekNodeByName(self, rootNameEx)
        if not root then
            print("ERROR:generatePrefab can't find prefab root:", rootNameEx)
            return
        end
        --register to self
        if not self._prefabDic then
            self._prefabDic = {}
            self.instantiatePrefab = function(node, name, nPrefabSelf)
                local prefab = node._prefabDic[name]
                if not prefab then
                    print("ERROR:instantiatePrefab can't find prefab by name:", name)
                    return
                end
                return prefab.instantiate(nPrefabSelf)
            end
        end
        self._prefabDic[rootNameEx] = ret
    end
    for _, name in pairs(nameList) do
        local node = UITool.seekNodeByName(root, name)
        if not node then
            print("WARN:generatePrefab can't find:", name)
        else
            ret[name] = node
            --bind func
            if string.match(name, "KWP_BTND_") then
                local func = ret["on_"..name] 
                if func then
                    --print("PREFAB add listener:", name, func, node:getName())
                    --dump(node)
                    node:addTouchEventListener(UITool.getDefaultTouchHanlde(ret, func))
                else
                    print("WARN:Need add Prefab Button callBack:", "on_"..name)
                end
            end
        end
    end
    ret._csbRoot = root
    ret.instantiate = function(prefabSelfEx)
        local new_root = root:clone()
        return UITool.generatePrefab(nil, nameList, new_root, rootName, prefabSelfEx)
    end
    return ret
end


function UITool.getDefaultTouchHanlde(self, func)
    return function(send, eventType)
        if eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
            send:setScale(1)
            send:setColor(cc.c3b(255,255,255))
            func(self, send, eventType)
        elseif eventType == ccui.TouchEventType.began then
            XH.audioManager:play("BUTTON_CLICK")
            send:setScale(0.9)
            send:setColor(cc.c3b(160,160,160))
            return
        end

    end
end

function UITool.seekWidgetByName(root, strKeyword)
    if root == nil then
        return nil
    end
    return ccui.Helper:seekWidgetByName(root, strKeyword)
end

function UITool.seekNodeByName(root, strKeyword)
    if root == nil then
        return nil
    end
    return ccui.Helper:seekNodeByName(root, strKeyword)
end

-- 封装监听函数
function UITool.addTouchEventListener(node, name, func)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:addTouchEventListener(func)
        return true
    end
    return false
end

function UITool.addClickEventListener(node, name, func)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:addClickEventListener(func)
        return true
    end
    return false
end

-- 封装监听函数
function UITool.addEventListener(node, name, func)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:addEventListener(func)
        return true
    end
    return false
end

function UITool.loadTexture(node, name, pngKeyword, type)
    if node == nil then
        return
    end
    if pngKeyword == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        --sx cocos, lua 不允许传空
        findNode:loadTexture(string.format("%s", pngKeyword), type or ccui.TextureResType.localType)
        return true
    end
    return false
end

function UITool.setPlistTextureOnNode(node, pngKeyword)
    if node == nil then
        print("UITool.setTextureOnNode node is nil:")
        return
    end
    if pngKeyword == nil then
        print("UITool.setTextureOnNode pngKeyword is nil:", pngKeyword)
        return
    end
    node:loadTexture(string.format("%s", pngKeyword), ccui.TextureResType.plistType)
    return true
end



function UITool.setVisible(node, name, visible)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:setVisible(visible)
        return true
    end
    return false
end

function UITool.isVisible(node, name)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        if findNode:isVisible() then
            return true
        else
            return false
        end
    end
    return false
end

function UITool.setColor(node, name, color3b)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:setColor(color3b)
        return true
    end
    return false
end

function UITool.setTextColor(node, name, color3b)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:setTextColor(color3b)
        return true
    end
    return false
end

function UITool.enableOutline(node, name, color3b, outlineSize)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:enableOutline(color3b,outlineSize or -1)
        return true
    end
    return false
end

function UITool.setText(node, name, string)
    if node == nil then
        return
    end
    local text = UITool.seekNodeByName(node, name)
    if text ~= nil then
        string = XH.StringTool.replaceMatchStr(string)
        text:setString(string)
        return true
    end
    return false
end

function UITool.setTag(node, name, tag)
    if node == nil then
        return
    end
    local nd = UITool.seekNodeByName(node, name)
    if nd ~= nil then
        nd:setTag(tag)
        return true
    end
    return false
end

function UITool.setEditBoxText(node, name, string)
    if node == nil then
        return
    end
    local text = UITool.seekNodeByName(node, name)
    if text ~= nil then
        text:setText(string)
        return true
    end
    return false
end

function UITool.setPercent(node, name, percent)
    if node == nil then
        return
    end
    local slider = UITool.seekNodeByName(node, name)
    if slider ~= nil then
        slider:setPercent(percent)
        return true
    end
    return false
end

function UITool.setFontSize(node, name, size)
    if node == nil then
        return
    end
    local text = UITool.seekNodeByName(node, name)
    if text ~= nil then
        text:setFontSize(size)
        return true
    end
    return false
end

function UITool.setFntFile(node, name, string)
    if node == nil then
        return
    end
    local text = UITool.seekNodeByName(node, name)
    if text ~= nil then
        text:setFntFile(string)
        return true
    end
    return false
end

function UITool.getInputText(node, name)
    if node == nil then
        return ""
    end
    local text = UITool.seekNodeByName(node, name)
    if text ~= nil then
        return text:getText()
    end
    return ""
end

function UITool.setInputText(node, name, txt)
    if node == nil or name == nil or txt == nil then
        return
    end
    local text = UITool.seekNodeByName(node, name)
    if text ~= nil then
        text:setText(txt)
    end
end

function UITool.getText(node, name)
    if node == nil then
        return ""
    end
    local text = UITool.seekNodeByName(node, name)
    if text ~= nil then
        return text:getString()
    end
    return ""
end

function UITool.setOpacity(node, name, opacity)
    if node == nil then
        return
    end
    local text = UITool.seekNodeByName(node, name)
    if text ~= nil then
        return text:setOpacity(opacity)
    end
    return ""
end

function UITool.setPositionX(node, name, x)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:setPositionX(x)
    end
    return
end

function UITool.getPositionX(node, name)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return findNode:getPositionX()
    end
    return 0
end

function UITool.setPositionY(node, name, y)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:setPositionY(y)
    end
    return
end

function UITool.setPosition(node, name, pos)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:setPosition(pos)
    end
    return
end

function UITool.getPositionY(node, name)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return findNode:getPositionY()
    end
    return 0
end

function UITool.getPosition(node, name)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return findNode:getPosition()
    end
    return 0, 0
end

function UITool.getNodeName(node, name)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return findNode:getName()
    end
    return 0
end

function UITool.removeAllChildren(node, name)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:removeAllChildren()
    end
    return
end

function UITool.isBright(node, name)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return findNode:isBright()
    end
    return false
end

function UITool.setBrightEx(node, name, enabled)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:setBright(enabled)
        findNode:setEnabled(enabled)
        return true
    end
    return false
end

function UITool.setBright(node, name, enabled)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:setBright(enabled)
        return true
    end
    return false
end

function UITool.stopPlayCsbFrameAni(node, name, csb)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        --local frameAni = cc.CSLoader:createTimeline(csb)
        node:stopAllActions()
    end
end

function UITool.setTouchEnabled(node, name, enabled)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:setTouchEnabled(enabled)

        return true
    end
    return false
end

function UITool.setBtnSelect(node, name, bSel)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:setSelected(bSel)
        return true
    end
    return false
end

function UITool.setAnchorPointNode(node, posx, posy)
    if node then
        node:setAnchorPoint(cc.p(posx, posy))
    end
end

function UITool.autoMoveTo(node, name, posX, posY, time, callBackFunc)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
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

function UITool.getWorldPosition(node, name)
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return findNode:getWorldPosition()
    end
    return 0
end

function UITool.moveNode(moveNode, targetNode)
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

function UITool.isEnabled(node, name)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return findNode:isEnabled()
    end
    return false
end

function UITool.setButtonState(node, name, enable)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:setEnabled(enable)
        findNode:setTouchEnabled(enable)
        findNode:setBright(enable)
    end
    return
end

function UITool.getTotalScaleY(node)
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

function UITool.setScaleX(node, name, scale)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:setScaleX(scale)
    end
end

function UITool.setScale(node, name, scale)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        findNode:setScaleX(scale)
        findNode:setScaleY(scale)
    end
end

function UITool.addTouchEventListenerByObj(node, name, obj, fun)
    if node == nil then
        return
    end
    if obj == nil then
        return
    end
    if fun == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode == nil then
        return
    end

    findNode:addTouchEventListener( function(send, eventType)
        fun(obj, send, eventType)
    end )
end

function UITool.onTouchEventByObj(node, name, func, eventType, obj)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return func(obj, findNode, eventType)
    end
    return nil
end

function UITool.addContentSizeWithHeight(node, height)
    if not node then
        return
    end

    local contentSize = node:getContentSize()
    node:setContentSize(contentSize.width, contentSize.height + height)
end

function UITool.adjustToTop(parentNode, childNode)
    if not parentNode then
        return
    end
    if not childNode then
        return
    end
    local parentSize = parentNode:getContentSize()
    childNode:setPositionY(parentSize.height)
end

function UITool.adjustToCenter(parentNode, childNode)
    if not parentNode then
        return
    end
    if not childNode then
        return
    end
    local parentSize = parentNode:getContentSize()
    childNode:setPositionY(parentSize.height / 2)
end

function UITool.adjustToBottom(parentNode, childNode)
    if not parentNode then
        return
    end
    if not childNode then
        return
    end
    childNode:setPositionY(0)
end

function UITool.getImageAddHeight(imgNode,screenHeightExceptImgNode)
    if not imgNode then
        return 0
    end

    if screenHeightExceptImgNode <=0 then
        return 0
    end

    local screenSize = cc.Director:getInstance():getWinSize()
    local designSize = UITool.getDesignSizeHD()
    local scaleY = screenSize.height / designSize.height
    local kuangaobi = ( screenSize.width / screenSize.height) / (designSize.width / designSize.height)
    if kuangaobi < 1 then
        local imgNodeSize = imgNode:getContentSize()
        local imgNodeHeight = (screenSize.height - screenHeightExceptImgNode * (scaleY * kuangaobi))/(scaleY * kuangaobi)
        if imgNodeHeight - imgNodeSize.height < 0 then
            return 0
        end
        return imgNodeHeight - imgNodeSize.height --返回这个imgNode九格增加的长度
    else
        return 0
    end
end

function UITool.getDesignSizeHD()
    local screenSize = cc.Director:getInstance():getWinSize()
    if screenSize.width > screenSize.height then
        return cc.size(1920, 1080) 
    else
        return cc.size(1080, 1920) 
    end  
end


function UITool.adjustChildrenPosition(parentNode,adjustTypeTabel)
    if not parentNode then
        return
    end

    if not adjustTypeTabel then
        return
    end
    local childrenNode = parentNode:getChildren()
    if childrenNode then
        for i = 1 , #childrenNode do
            local childName = childrenNode[i]:getName()
            local splitTable = string.split(childName,"_")
            local adjustType = splitTable[#splitTable]
            if adjustType == adjustTypeTabel["top"] then
                UITool.adjustToTop(parentNode, childrenNode[i])
            elseif adjustType == adjustTypeTabel["center"] then
                UITool.adjustToCenter(parentNode, childrenNode[i])
            elseif adjustType == adjustTypeTabel["bottom"] then
                UITool.adjustToBottom(parentNode, childrenNode[i])
            end
        end
    end
end

function UITool.setEnabled(node, name, isEnabled)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return findNode:setEnabled(isEnabled)
    end
    return false
end

function UITool.ignoreContentAdaptWithSize(node, name, isEnabled)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return findNode:ignoreContentAdaptWithSize(isEnabled)
    end
    return false
end

function UITool.jumpToTop(node, name)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return findNode:jumpToTop()
    end
    return false
end

function UITool.setTitleColor(node, name, color)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return findNode:setTitleColor(color)
    end
    return false
end


function UITool.requestRefreshView(node, name)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return findNode:requestRefreshView()
    end
    return false
end

function UITool.getContentSize(node, name)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return findNode:getContentSize()
    end
    return
end

function UITool.setContentSize(node, name, width, height)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        return findNode:setContentSize(width, height)
    end
    return
end

---------------------------------------------------------
--@brief 刘海屏幕适配
--@param nodeList 需要适配的节点
--@param notchWidth 刘海的宽度,默认不传使用底层获取的
--@return nil
---------------------------------------------------------
function UITool.adaptForLiuHai(nodeList, notchWidth)
    notchWidth = notchWidth or UITool.getLiuHaiWidth()
    nodeList = nodeList or {}
    if notchWidth > 0 then
        --开始适配
        local designSize = UITool.getDesignSizeHD()
        local safeSize = cc.size(designSize.width - notchWidth * 2 ,designSize.height)

        for _, v in pairs(nodeList) do
            local curPosX = v:getPosition()
            local newPosX = notchWidth + curPosX / designSize.width * safeSize.width
            v:setPositionX(newPosX)
        end
    end
end

---------------------------------------------------------
--@brief 刘海屏幕适配
--@param 无
--@return 刘海宽度
---------------------------------------------------------
function UITool.getLiuHaiWidth()
    local KW_DEFULT_NOTOUCH_WIDTH = 80
    local notchWidth = display.notchWidth
    local frameSize = cc.Director:getInstance():getOpenGLView():getFrameSize()
    if notchWidth > 0 or (frameSize.width / frameSize.height > 1.8) then
        return KW_DEFULT_NOTOUCH_WIDTH
    else
        return 0
    end
end

-- plist对应的所有SpriteFrame的引用计数加1
-- 主要针对生命周期和app生命周期一致的SpriteFrame使用
function UITool.retainPlistAllSpriteFrames(plistPath)
    local getTables = cc.FileUtils:getInstance():getValueMapFromFile(plistPath)
    if getTables and getTables.frames then
        for pngName, _ in pairs(getTables.frames) do
            local spriteFrame = cc.SpriteFrameCache:getInstance():getSpriteFrameByName(pngName)
            if spriteFrame then
                spriteFrame:retain()
            end
        end
    end
end

---------------------------------------------------------
--@brief 更新节点图片
--@param node 目标节点
--@param url 目标图片地址
--@param name 图片节点名称
--@return nil
---------------------------------------------------------
function UITool.reloadNodeRemoteImage(node, url, name, bForce)
    if not node then
        return 
    end
    if not url or url == "" then
        return
    end

    node:removeAllChildren()
    local size = node:getContentSize()
    local imageNode = XH.RemoteImage.new()
        :setPosition(size.width/2, size.height/2)
        :ignoreContentAdaptWithSize(false)
        :addTo(node)
    if name then
        imageNode:setName(name)
    end

    imageNode:setUrl(url, bForce)
end

function UITool.adaptTextToWidth(widget, width, maxFontSize, minFontSize)
    if widget == nil then
        return 0
    end

    local curFontSize = maxFontSize
    while curFontSize > minFontSize do
        if widget:getContentSize().width > width then
            curFontSize = curFontSize - 1
            widget:setFontSize(curFontSize)
        else
            break
        end
    end
    return curFontSize
end


function UITool.adaptBmfontToWidth(widget, width, maxFontSize, minFontSize)
    if widget == nil then
        return 0
    end

    local curFontSize = maxFontSize
    
    while curFontSize > minFontSize do
        if widget:getBoundingBox().width > width then
            curFontSize = curFontSize - 0.05
            widget:setScale(curFontSize)
        else
            break
        end
    end
    return curFontSize
end

function UITool.setRotation(node, name, rotation)
    if node == nil then
        return
    end
    local findNode = UITool.seekNodeByName(node, name)
    if findNode ~= nil then
        rotation = rotation or 0
        findNode:setRotation(rotation)
        return true
    end
    return false
end

function UITool.printNodeInfo(node, str)
    str = str or '-'
    --while node ~= nil do
        print(str .. (node.__cname or tolua.type(node) or "emptyname"))
        for i=1, #node:getChildren() do
            UITool.printNodeInfo(node:getChildren()[i], str.."-")
        end
    --end
    return false
end

function UITool.gray(node)
    local shaderCmd = "ShaderUIGrayScale"
    local glProgram = cc.GLProgramCache:getInstance():getGLProgram(shaderCmd)
    local render = node:getVirtualRenderer()
    if glProgram and render then
        render:setGLProgram(glProgram)
    end
    for _,v in pairs(node:getChildren()) do
        v:setGLProgram(glProgram)
        if v.getVirtualRenderer then
            local rChild = v:getVirtualRenderer()
            if rChild then
                rChild:setGLProgram(glProgram)
            end
        end
    end
end

function UITool.resetGray(node)
    local shaderCmd = "ShaderPositionTextureColor_noMVP"
    local glProgram = cc.GLProgramCache:getInstance():getGLProgram(shaderCmd)
    local render = node:getVirtualRenderer()
    if render then
        render:setGLProgram(glProgram)
    end

    for _,v in pairs(node:getChildren()) do
        v:setGLProgram(glProgram)
        if v.getVirtualRenderer then
            local rChild = v:getVirtualRenderer()
            if rChild then
                rChild:setGLProgram(glProgram)
            end
        end
    end
end
return UITool /k  