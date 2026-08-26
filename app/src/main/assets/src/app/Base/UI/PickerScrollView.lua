local PickerScrollView = { }
---------------------------------
-- 将一个普通panel转化为滚动选择控件
-- @param panel ccui.Layout
-- @param info table
--{
--      childNode 子控件（必须）
--      childText 子控件内的文字控件（必须）
--      center 中心位置（可选）
--      allowRotation 是否开启旋转（可选）
--      rotationRate 3D旋转系数（可选）
--      opacityOffset 不透明衰减度（可选）
--      inertiaTime 惯性衰减时间（可选）
--      minimumAniPos 惯性停止最小值（可选）
--      shownItem 显示数（可选）
--}
-- @return PickerScrollViewFuncs
---------------------------------
function PickerScrollView.create(panel, info)
    local lastPos = nil
    local inertialAnimateHandler = nil

    if panel == nil or tolua.type(panel) ~= "ccui.Layout" then
        return
    end

    -- 暂不支持无自己的子节点
    local childNode = info.childNode
    if childNode == nil then
        return
    end

    local childHeight = childNode:getContentSize().height
    local childWidth = childNode:getContentSize().width
    local panelHeight = panel:getContentSize().height
    local childText = info.childText
    local panelCenter = info.center or panelHeight / 2 + (childNode:getAnchorPoint().y - 0.5) * childHeight
    local allowRotation = info.allowRotation or false
    local rotationRate = info.rotationRate or -15
    local opacityOffset = info.opacityOffset or 0
    local inertiaTime = info.inertiaTime or 50
    local minimumAniPos = info.minimumAniPos or 0.001
    local shownItem = info.shownItem or ((panelHeight / childHeight) + 2)
    local allItemCount = 0

    -- extend panel object
    local PickerScrollViewFuncs = panel

    ---------------------------------
    -- 将一个普通panel转化为滚动选择控件
    -- @param listData table 要显示的字符串的列表
    ---------------------------------
    function PickerScrollViewFuncs:init(listData, itemSuffix)
        -- 刷新数据时停止动画
        if inertialAnimateHandler ~= nil then
            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(inertialAnimateHandler)
            inertialAnimateHandler = nil
        end
        panel:removeAllChildren()
        -- 预留了Tag = 0 的情况是静止的
        local childTag = 1

        -- 列表只有一个项时可能需要不同的表现形式
        if #listData == 1 then
            local item = childNode:clone()
            item:getChildByName(childText):setString(tostring(itemSuffix or ""))
            panel:addChild(item)
            item:setPositionX(panel:getContentSize().width / 2)
            self:setChildStyle(item, panel:getContentSize().height + (childTag - 1) * childHeight)
            item:setTag(0)
            item:setVisible(true)
            item:setSwallowTouches(false)
            return
        end

        -- 如果数量不足则复制
        allItemCount = math.ceil(shownItem / (#listData)) * (#listData)
        while #(panel:getChildren()) < shownItem do
            for _, v in ipairs(listData) do
                local item = childNode:clone()
                item:getChildByName(childText):setString(v .. tostring(itemSuffix or ""))
                panel:addChild(item)
                item:setPositionX(panel:getContentSize().width / 2)
                self:setChildStyle(item, panel:getContentSize().height + (childTag - 1) * childHeight)
                item:setTag(childTag)
                item:setVisible(true)
                item:setSwallowTouches(false)
                childTag = childTag + 1
            end
        end

        local deltaY = 0

        local function onTouchBegan(send, eventType)
            local touchLocaion = send:getTouchBeganPosition()
            --print(string.format("onTouchBegan:X = %.2f  Y = %.2f", touchLocaion.x, touchLocaion.y))

            -- 新的触摸事件发生时，关闭上一次的动画
            if inertialAnimateHandler ~= nil then
                cc.Director:getInstance():getScheduler():unscheduleScriptEntry(inertialAnimateHandler)
                inertialAnimateHandler = nil
            end

            lastPos = touchLocaion
        end

        local function onTouchMoved(send, eventType)
            local touchLocation = send:getTouchMovePosition()
            deltaY = touchLocation.y - lastPos.y
            --print(string.format("onTouch:X = %.2f  Y = %.2f", touchLocation.x, touchLocation.y))
            local children = panel:getChildren()
            for _, v in pairs(children) do
                if v:getTag() ~= 0 then
                    local ChildY = v:getPositionY()
                    ChildY = ChildY + touchLocation.y - lastPos.y
                    self:setChildStyle(v, ChildY)
                end
            end
            lastPos = touchLocation
        end

        local function onTouchEnded(send, eventType)
            --print(string.format("onEnd:X = %.2f  Y = %.2f", lastPos.x, lastPos.y))
            local dy = deltaY / inertiaTime
            inertialAnimateHandler = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
                deltaY = deltaY - dy
                local children = panel:getChildren()
                if deltaY > -1 * minimumAniPos and deltaY < minimumAniPos then
                    self:forcePosition(children)
                else
                    for _, v in pairs(children) do
                        if v:getTag() ~= 0 then
                            local ChildY = v:getPositionY()
                            ChildY = ChildY + deltaY
                            self:setChildStyle(v, ChildY)
                        end
                    end

                end
            end, 0, false)
        end

        panel:addTouchEventListener(function(send, eventType)
            if eventType == ccui.TouchEventType.began then
                onTouchBegan(send, eventType)
            elseif eventType == ccui.TouchEventType.ended or
                    eventType == ccui.TouchEventType.canceled then
                onTouchEnded(send, eventType)
            elseif eventType == ccui.TouchEventType.moved then
                onTouchMoved(send, eventType)
            end
        end)

    end

    ---------------------------------
    -- 设置每个子节点转动后的样式
    -- @param child node 要设置的子节点
    -- @param posY number 子节点的位置
    ---------------------------------
    function PickerScrollViewFuncs:setChildStyle(child, posY)
        -- set position
        if posY < -1 * childHeight then
            posY = posY + childHeight * allItemCount
        end
        if posY > childHeight * (allItemCount - 1) then
            posY = posY - childHeight * allItemCount
        end
        child:setPositionY(posY)

        -- set scale
        local d = (posY - panelCenter)
        if d < 0 then
            d = d * -1
        end
        local scale = 2 - d / (childHeight * 1.5)
        scale = scale * 0.6
        if scale < 0.6 then
            scale = 0.6
        end
        child:getChildByName(childText):setScale(scale)
        local opacity = 255 - d / childHeight * opacityOffset
        if opacity < 0 then
            opacity = 0
        end
        child:getChildByName(childText):setOpacity(opacity)

        -- set Rotation3D
        if allowRotation then
            local e = (posY - panelCenter)
            if e < 0 then
                e = e * -1
            end
            print(e)
            local skew = (e / (childHeight * 1.5))
            child:getChildByName(childText):setRotation3D(cc.vec3(skew * childHeight / shownItem * 2 / rotationRate / math.pi * 180, 0, 0))
            local skewPos = shownItem * shownItem / 4 - skew * skew
            if skewPos < 0 then
                skewPos = 0
            end
            child:getChildByName(childText):setPositionX(rotationRate * math.sqrt(skewPos) + childWidth / 2 - rotationRate * shownItem / 2)
            child:getChildByName(childText):setOpacity(255 - math.ceil(skew * opacityOffset))
        end
    end

    -- 强制所有节点归位
    function PickerScrollViewFuncs:forcePosition(children)
        for _, v in pairs(children) do
            if v:getTag() ~= 0 then
                local ChildY = v:getPositionY()
                local gapToProperPos = ChildY % childHeight
                if gapToProperPos < 0 then
                    gapToProperPos = gapToProperPos + childHeight
                end
                if gapToProperPos >= childHeight / 2 then
                    ChildY = ChildY + childHeight - gapToProperPos
                else
                    ChildY = ChildY - gapToProperPos
                end
                self:setChildStyle(v, ChildY)
            end
        end
        if inertialAnimateHandler ~= nil then
            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(inertialAnimateHandler)
            inertialAnimateHandler = nil
            self:onValueChanged()
        end
    end

    -- 获取中间节点的值
    function PickerScrollViewFuncs:getValue()
        local children = panel:getChildren()
        self:forcePosition(children)
        for _, v in pairs(children) do
            if v:getTag() ~= 0 then
                local ChildY = v:getPositionY()
                if ChildY == panelCenter then
                    return v:getChildByName(childText):getString()
                end
            end
        end
    end

    -- 设置中间节点的值
    function PickerScrollViewFuncs:setValue(value)
        local children = panel:getChildren()
        if not children then
            return
        end
        local center = nil
        for _, v in pairs(children) do
            if v:getTag() ~= 0 then
                if tonumber(value) == tonumber(string.match(v:getChildByName(childText):getString(), "%d+")) then
                    center = v:getTag()
                    break
                end
            end
        end

        -- 没有对应项就返回
        if center == nil then
            return
        end
        for _, v in pairs(children) do
            if v:getTag() ~= 0 then
                self:setChildStyle(v, (center - v:getTag()) * childHeight + panelCenter)
            end
        end
        self:forcePosition(children)
        self:onValueChanged()
    end

    -- 控件值变化响应事件
    function PickerScrollViewFuncs:onValueChanged()
        local eventDispatcher = cc.Director:getInstance():getEventDispatcher()
        local event = cc.EventCustom:new("PickerScrollView.valueChanged")
        event.pickerPanel = panel
        eventDispatcher:dispatchEvent(event)
    end

    -- 退出处理，必须加在控件节点的退出事件中
    function PickerScrollViewFuncs:onExit()
        if inertialAnimateHandler ~= nil then
            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(inertialAnimateHandler)
            inertialAnimateHandler = nil
        end
    end

    return PickerScrollViewFuncs
end

return PickerScrollView   u,  