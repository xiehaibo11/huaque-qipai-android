local IMMessageView = class("IMMessageView",XH.ViewBase)
local IMMessageUI = require("lobby.Modules.Im.MessageUIView")

local DEFAULT_TIME = 3600 --默认持续时间
local MAX_NOTIFY_COUNT = 3 --同时存在的提示条数
local DIS_EVERY_TOAST = 5 --每个notify的间距
local MOVE_UP_TIME = 0.2 --每个notify向上移动的距离

local notifyCount = 0
local MAX_LIST_COUNT = 100

function IMMessageView:getCSBPath()
    return "cocosStudio/hall/CSB/IM/IMMessage.csb"
end

function IMMessageView:getBindingInfo()
    return {
        ["_KW_UI_MESSAGE"] = {varName="_uiMessage",type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KW_BTND_MESSAGE"},
        ["_KW_POS_IN"] = { varName = "_posIn" },
        ["_KW_POS_OUT"] = { varName = "_posOut" },
        ["_KW_INVITE_PANEL"] = { varName = "_invitePanel" },
        ["_KW_INVITE_ITEM"] = { varName = "_inviteItem" },
        ["_KW_WAIT_PANEL"] = { varName = "_waitPanel" },
        ["_KW_WAIT_BG"] = { varName = "_waitBg", onTouchEnded = "on_KW_BTND_JOIN" },
        ["_KW_BTN_JOIN_NOW"] = { varName = "_btnJoin", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KW_BTND_JOIN" },
    }
end

function IMMessageView:ctor(str, id, time)
    IMMessageView.super.ctor(self)
    self._curInviteCount = 0

    self:initUI()
    self._notifyList = {}
    self._joinInfo = {}

    XH.lobby:getModule("Im"):reqMessageList(false, true, true)
end

function IMMessageView:initUI()
    self:initTableView()
end

function IMMessageView:getData()
    if not self._imData then
        self._imData = XH.lobby:getModule("Im"):getIMData()
    end
    return self._imData
end

function IMMessageView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_HIDE_MESSAGELAYER", callBack = "moveOutAnimation" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_NOTIFY_MESSAGE", callBack = "showInviteNotify" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_HIDE_MSG_ENTRANCE", callBack = "hideMessageEntrance" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_SHOW_MSG_ENTRANCE", callBack = "showMessageEntrance" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_SHOW_MSG_LIST", callBack = "showMessageList" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_SHOW_JOIN_NOW", callBack = "showJoinNowPanel" },
        { module = self:getData(), eventKeyName = "EVENT_INVITE_DATA_CHANGED", callBack = "onRefreshList" },
        { module = self:getData(), eventKeyName = "EVENT_SHOW_POINT", callBack = "showPoint" },
    }
end

function IMMessageView:initTableView()
    local tableView = cc.TableView:create(self._invitePanel:getContentSize())
    tableView:setAnchorPoint(cc.p(0, 0))
    tableView:setPosition(cc.p(0, 0))
    tableView:registerScriptHandler(handler(self, self.onTableViewCellSizeForTable), cc.TABLECELL_SIZE_FOR_INDEX)
    tableView:registerScriptHandler(handler(self, self.onTableViewCellAtIndex), cc.TABLECELL_SIZE_AT_INDEX)
    tableView:registerScriptHandler(handler(self, self.numberOfCellsInTableView), cc.NUMBER_OF_CELLS_IN_TABLEVIEW)
    tableView:registerScriptHandler(handler(self, self.scrollViewDidScroll), cc.SCROLLVIEW_SCRIPT_SCROLL)
    tableView:setVerticalFillOrder(cc.TABLEVIEW_FILL_TOPDOWN)
    tableView:setDirection(cc.SCROLLVIEW_DIRECTION_VERTICAL)
    tableView:setName("_inviteView")
    tableView:setDelegate()
    self._invitePanel:addChild(tableView)

    self._tableViewItemModelSize = self._inviteItem:getContentSize()
    self._tableViewItemModel = self._inviteItem
end

function IMMessageView:onTableViewCellSizeForTable(view)
    return self._tableViewItemModelSize.width, self._tableViewItemModelSize.height + 10
end

function IMMessageView:onTableViewCellAtIndex(view, index)
    local cell = view:dequeueCell()
    local item
    if not cell then
        cell = cc.TableViewCell:new()
        item = self._tableViewItemModel:clone()
        if not item then return cell end

        item:setTouchEnabled(false)
        item:setVisible(true)
        item:setAnchorPoint(cc.p(0, 0))
        item:setPosition(0, 0)
        item:setName("__item")
        cell:addChild(item)
    else
        item = cell:getChildByName("__item")
    end
   
    local dataList = self._imData:getInviteList()
    local data = dataList[index + 1]
    if data then
        -- 设置昵称
        XH.UITool.setText(item, "_KW_INVITE_PLAYER", XH.StringTool.cutStringByLength(clone(data.fromNickName), 5))
--        XH.UITool.setText(item, "_KW_INVITE_TIME", index + 1)
--        XH.UITool.setText(item, "_KW_INVITE_TIME", XH.lobby:getModule("Im"):changeTime2Str(data.askTime))
--        if data.msgType and XH.lobby:getModule("Im").MESSAGE_CONTENT[data.msgType] then
--            XH.UITool.setText(item, "_KW_TEXT_CONTENT", XH.lobby:getModule("Im").MESSAGE_CONTENT[data.msgType])
--        end
        XH.lobby:getModule("Im"):updateHeadImg(XH.UITool.seekNodeByName(item, "_KW_IMG_HEAD"), data.headUrl)
        XH.UITool.setText(item, "_KW_ROOM_ID", data.roomid)
        XH.UITool.setText(item, "_KW_TEXT_ROOM_INFO", XH.lobby:getModule("Im"):getRoomInfoStr(data.gameName, data.charis, data.payType))
        XH.UITool.addTouchEventListener(item, "_KW_UI_AGREE", handler(data, handler(self, self.onTouchEventAgreeInvite)))
        XH.UITool.addTouchEventListener(item, "_KW_UI_REFUSE", handler(data, handler(self, self.onTouchEventRefuseInvite)))
    end

    return cell
end

function IMMessageView:numberOfCellsInTableView(view)
    self._curInviteCount = #self._imData:getInviteList()
    return self._curInviteCount
end

function IMMessageView:scrollViewDidScroll(view)
    if self._curInviteCount < MAX_LIST_COUNT then

        local tableView = self._invitePanel:getChildByName("_inviteView")
        local listSize = tableView:getContentSize()    -- 总大小
        if listSize.height == 0 then return end     -- 分母不能为0
        local offset = tableView:getContentOffset()    -- 取滑动偏移
        if tableView:getVerticalFillOrder() == cc.TABLEVIEW_FILL_TOPDOWN then
            offset.y = offset.y + listSize.height   -- 从起点开始偏移量
        end

        local endIdx = offset.y / self._tableViewItemModelSize.height + 1
        if endIdx > self._curInviteCount then
            endIdx = self._curInviteCount
        end

        -- 滑动到显示倒数第二个的时候回调
        if endIdx > self._curInviteCount - 1 and not self._isTableViewBottom then
            self._isTableViewBottom = true
            XH.lobby:getModule("Im"):reqMessageList()
        end
        if endIdx < self._curInviteCount - 1 then
            self._isTableViewBottom = false
        end
    end
end

function IMMessageView:hideMessageEntrance()
    self._uiMessage:setVisible(false)
end

function IMMessageView:showMessageEntrance()
    self._uiMessage:setVisible(true)
end

function IMMessageView:showMessageList(event)
    XH.TipTool.hideLoading()
    if not event.msg or next(event.msg) == nil then
        self._uiMessage:setVisible(false)
        XH.TipTool.showToast("暂时没有消息，快去邀请牌友吧")
    else
        self:moveInAnimation()
    end
end

function IMMessageView:showPoint(event)
    local msg = event.msg
    if not msg or not self._uiMessage then
        return
    end
    if not msg.bShow then
        self._uiMessage:setVisible(false)
        return
    end
    self._uiMessage:setVisible(true)
end

function IMMessageView:onRefreshList(event)
    if next(self._imData:getInviteList()) == nil and not event.data then
        self._uiMessage:setVisible(false)
        XH.TipTool.showToast("暂时没有消息，快去邀请牌友吧")
        self:moveOutAnimation()
        return
    end

    local lastIndex = self._curInviteCount
    local tableView = self._invitePanel:getChildByName("_inviteView")
    local totalNum = #self._imData:getInviteList()

    local point = tableView:getContentOffset()  -- 当前偏移量
    local size1 = tableView:getContentSize()    -- 重新加载数据前列表大小
    tableView:reloadData()

    -- 滑动到当前位置
    if lastIndex > 0 and lastIndex <= totalNum then
        local size2 = tableView:getContentSize()    -- 重新加载数据后列表大小

        point.y = (size1.height + point.y) - size2.height   -- 重新计算偏移量与之前展示位置保持一致

        tableView:setContentOffset(point, false)
    end
end

function IMMessageView:showJoinNowPanel(event)
    if not event.msg or not event.msg.show then
        self._waitPanel:setVisible(false)
        return
    end
    self._joinInfo = event.msg.info
    self._waitPanel:setVisible(true)
end

function IMMessageView:on_KW_BTND_MESSAGE(send, eventType)
    if not self:judgeMoveIn() then
        self._waitPanel:setVisible(false)
        XH.lobby:getModule("Im"):dispatchEvent({name = XH.lobby:getModule("Im").EVENT_HIDE_NOTIFY_INVITE})

        XH.TipTool.showLoading()
        XH.lobby:getModule("Im"):reqMessageList(true, true)
    else
        self:moveOutAnimation()
    end
end

function IMMessageView:on_KW_BTND_JOIN(send, eventType)
    if not self._joinInfo then
        XH.TipTool.showToast("房间已不存在！")
        self._waitPanel:setVisible(false)
        return
    end
    XH.lobby:getModule("Im"):reqReplyInvite(self._joinInfo.inviteId, XH.IMProtocol.REPLYTYPE.AGREE)
    self._imData:removeMessageFromData(self._joinInfo.invitePlayer, XH.IMProtocol.REPLYTYPE.AGREE)
end

function IMMessageView:onTouchEventAgreeInvite(data, send, eventType)
    if eventType == ccui.TouchEventType.began then
        XH.audioManager:play("BUTTON_CLICK")
        send:setScale(0.95)
        send:setColor(cc.c3b(160,160,160))
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setScale(1.0)
        send:setColor(cc.c3b(255, 255, 255))
    end
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:moveOutAnimation()

    if data.msgType == XH.IMProtocol.MESSAGE_TYPE.INVITE then
        XH.lobby:getModule("Im"):reqReplyInvite(data.id, XH.IMProtocol.REPLYTYPE.AGREE)
    elseif data.msgType == XH.IMProtocol.MESSAGE_TYPE.FOLLOW then
        XH.lobby:getModule("Im"):reqReplyWillJoinTable(data.fromAreaid, data.fromNumid, XH.IMProtocol.REPLYTYPE.AGREE)
    elseif data.msgType == XH.IMProtocol.MESSAGE_TYPE.ORDER then
        XH.lobby:getModule("Im"):reqReplyReserveGame(data.fromAreaid, data.fromNumid, XH.IMProtocol.REPLYTYPE.AGREE)
    end
    self._imData:removeMessageFromData(data.fromNumid, data.msgType)
    
    -- 抛送同意按钮数据
    local data = {
        block_item_id = "同意",
        block_label = "大厅-牌友"
    }
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyxt24052711, data)
end

function IMMessageView:onTouchEventRefuseInvite(data, send, eventType)
    if eventType == ccui.TouchEventType.began then
        XH.audioManager:play("BUTTON_CLICK")
        send:setScale(0.95)
        send:setColor(cc.c3b(160,160,160))
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setScale(1.0)
        send:setColor(cc.c3b(255, 255, 255))
    end
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    if data.msgType == XH.IMProtocol.MESSAGE_TYPE.INVITE then
        XH.lobby:getModule("Im"):reqReplyInvite(data.id, XH.IMProtocol.REPLYTYPE.REFUSE)
    elseif data.msgType == XH.IMProtocol.MESSAGE_TYPE.FOLLOW then
        XH.lobby:getModule("Im"):reqReplyWillJoinTable(data.fromAreaid, data.fromNumid, XH.IMProtocol.REPLYTYPE.REFUSE)
    elseif data.msgType == XH.IMProtocol.MESSAGE_TYPE.ORDER then
        XH.lobby:getModule("Im"):reqReplyReserveGame(data.fromAreaid, data.fromNumid, XH.IMProtocol.REPLYTYPE.REFUSE)
    end
    self._imData:removeMessageFromData(data.fromNumid, data.msgType)

    -- 抛送拒绝按钮数据
    local data = {
        block_item_id = "拒绝",
        block_label = "大厅-牌友"
    }
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyxt24052711, data)
end

--进入动画
function IMMessageView:moveInAnimation()
    if self._invitePanel then
        self._invitePanel:stopAllActions()
        local actionTime = 0.2
        local targetPosX = self._posIn:getPositionX()
        local targetPosY = self._invitePanel:getPositionY()
        local moveAni = cc.Spawn:create(cc.FadeIn:create(actionTime),cc.MoveTo:create(actionTime, cc.p(targetPosX, targetPosY)), cc.ScaleTo:create(actionTime, 1))
        local actSequence = cc.Sequence:create(cc.CallFunc:create(function()
                                self._invitePanel:setVisible(true)
                            end), moveAni )
        self._invitePanel:runAction(actSequence)
    end
end

--关闭动画
function IMMessageView:moveOutAnimation()
    if self._invitePanel then
        self._invitePanel:stopAllActions()
        local actionTime = 0.2
        local targetPosX = self._posOut:getPositionX()
        local targetPosY = self._invitePanel:getPositionY()
        local moveAni = cc.Spawn:create(cc.FadeOut:create(actionTime),cc.MoveTo:create(actionTime, cc.p(targetPosX, targetPosY)), cc.ScaleTo:create(actionTime, 0.01))
        local actSequence = cc.Sequence:create(moveAni, cc.CallFunc:create(function()
                                self._invitePanel:setVisible(false)
                            end))
        self._invitePanel:runAction(actSequence)
    end
end

function IMMessageView:showInviteNotify(event)
    if self:judgeMoveIn() then
        XH.lobby:getModule("Im"):reqMessageList()
        return
    end
    if event.msg then
        local len = #self._notifyList
        if len >= MAX_NOTIFY_COUNT then
            --超过最大值,需要先移除一个
            local tmpNotify = self:removeNotify()
            if not tolua.isnull(tmpNotify) then
                tmpNotify:hideSelf()
            end
        end
        self:moveUpAni()
        self:showNotify(event.msg)
    end
end

function IMMessageView:removeNotify(notify)
    notify = notify or 1
    local index = 0
    if type(notify) == "userdata" then
        local notifyIndex = 1
        for _, tmpNotify in ipairs(self._notifyList) do
            if notify:getID() == tmpNotify:getID() then
                index = notifyIndex
                break
            end

            notifyIndex = notifyIndex + 1
        end
    elseif type(notify) == "number" then
        index = notify
    end

    return table.remove(self._notifyList, index)
end

function IMMessageView:moveUpAni()
    local deleteTab = {}
    for k, notify in ipairs(self._notifyList) do
        if not tolua.isnull(notify) then
            local curPos = cc.p(notify:getPosition())
            local contentSize = notify:getSize()

            local desY = curPos.y - contentSize.height - DIS_EVERY_TOAST
            local moveAni = cc.MoveTo:create(MOVE_UP_TIME, cc.p(curPos.x, desY))
            notify:runAction(moveAni)
        else
            deleteTab[#deleteTab + 1] = k
        end
    end

    for _, index in ipairs(deleteTab) do
        self:removeNotify(index)
    end
end

function IMMessageView:showNotify(msg)
    notifyCount = notifyCount + 1

    local notify = IMMessageUI.new(msg, notifyCount, self._uiMessage:getWorldPosition())
    local function startRemove()
        notify:hideSelf()
        self:removeNotify(notify)
    end

    local fadeOutTime = 1 --最后一秒淡出
    local delayAct = cc.DelayTime:create(DEFAULT_TIME - fadeOutTime)
    local updateCallAct = cc.CallFunc:create(startRemove)

    local len = #self._notifyList
    local delayTime = len > 0 and MOVE_UP_TIME or 0
    local delayDisply = cc.DelayTime:create(delayTime)
    local showToastFunc = cc.CallFunc:create(function()
        notify:setVisible(true)
    end)
    
    notify:addToScene()
    self:pushToast(notify)
    local sequenceAct = cc.Sequence:create(delayDisply, showToastFunc, delayAct, cc.FadeOut:create(fadeOutTime), updateCallAct)
    notify:runAction(sequenceAct)
end

function IMMessageView:pushToast(notify)
    local len = #self._notifyList
    self._notifyList[len + 1] = notify
end

function IMMessageView:judgeMoveIn()
    if self._invitePanel:getPositionX() == self._posIn:getPositionX() then
        return true
    end
    return false
end

return IMMessageView   �A  