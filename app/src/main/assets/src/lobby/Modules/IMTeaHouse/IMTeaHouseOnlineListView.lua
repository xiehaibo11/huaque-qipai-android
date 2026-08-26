---@class IMTeaHouseOnlineListView : View
local IMTeaHouseOnlineListView = class("IMTeaHouseOnlineListView", XH.ViewBase)

IMTeaHouseOnlineListView.OnlineListType = {
    Member = 1,
    Lately = 2
}

IMTeaHouseOnlineListView.NoticeType = {
    NoMember = 1,
    Loading = 2
}

IMTeaHouseOnlineListView.KW_REAL_WIDTH = 580
IMTeaHouseOnlineListView.KW_LIST_BG_WHITE_WIDTH = 115

local KW_ONLINE_STATE = 1
local KW_ONLINE_LIST_LATELY_SHOW_NUM = 10
local KW_ONLINE_LIST_STATE_WEIGHT = {
    [0] = 0,
    [1] = 2,
    [2] = 1,
    [3] = 1,
}

function IMTeaHouseOnlineListView:getCSBPath()
    return "cocosStudio/TeaHouse/CSB/TeaHouseOnlineListView.csb"
end

function IMTeaHouseOnlineListView:getBindingInfo()
    return {
        ["_KW_PANEL_MASK"] = { varName = "_panelMask", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onEventMask" },
        ["_KW_IMG_BG"] = { varName = "_imgBg" },
        ["_PANEL_MEMBER_LIST"] = { varName = "_panelList" },
        ["_KW_PANEL_LIST_ITEM"] = { varName = "_panelListItem" },
        ["_KW_BTN_LATELY"] = { varName = "_btnLately", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventLately" },
        ["_KW_BTN_MEMBER"] = { varName = "_btnMember", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventMember" },
        ["_KW_BTN_RECALL"] = { varName = "_btnRecall" },
        ["_TEXT_NOTICE"] = { varName = "_textNotice" },
        ["_KW_BTN_INVITE_ALL"] = { varName = "_btnInviteAll", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventInviteAll" },
        ["_KW_BTN_ONLINE"] = { varName = "_btnOnline" },
        ["_KW_OPEN_TOUCH_AREA"] = { varName = "_openTouchArea", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventOnline" },
        ["_KW_TEXT_ONLINE_NUM"] = { varName = "_textOnline" }
    }
end

function IMTeaHouseOnlineListView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_TEA_HOUSE_LIST_CHANGED", callBack = "onUpdateMemberList" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_LATELY_LIST_CHANGED", callBack = "onUpdateLatelyList" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_HIDE_ONLINE_LIST", callBack = "onHideEntrance" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_HIDE_LIST_ENTRANCE", callBack = "onHideEntrance" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_TEA_HOUSE_INFO_UPDATE", callBack = "updateOnlineNum" },
    }
end

function IMTeaHouseOnlineListView:showSelf(xzorder)
    local zorder = xzorder or XH.ZORDER.WINDOW
    local runningScene = display.getRunningScene()
    if runningScene then
        runningScene:addChild(self, zorder)
    end
    return self
end

function IMTeaHouseOnlineListView:ctor(param)
    IMTeaHouseOnlineListView.super.ctor(self)
    if param and param.inviteOfflineFunc then
        self._inviteOfflineFunc = param.inviteOfflineFunc
    end
    self:initData()
    self:initView()
end

function IMTeaHouseOnlineListView:initData()
    self._selectListType = IMTeaHouseOnlineListView.OnlineListType.Member
    self._infoList = {}
    self._bMoveIn = false
    XH.playerData:flushPlayerPosition()
    XH.lobby:getModule("Im"):reqMixTableListInfo()
end

function IMTeaHouseOnlineListView:initView()
    self:hideNoticeText()
    self:updateListTypeBtns()
    self:clearList()
    self:adaptForLiuHai()
    self:updateOnlineNum()
    -- 游戏区内自动打开成员列表
    if XH.gameManager and not XH.gameManager:isPlayBack() then
        self:performWithDelay(function()
            --游戏中和房间人数满则不弹出成员列表
            if XH.gameManager and not XH.gameManager:isGameStart() and XH.gameManager:isNumberFull() then
                self:onBtnEventOnline(self._openTouchArea, ccui.TouchEventType.ended)
            end
        end,1)
    end
end

function IMTeaHouseOnlineListView:adaptForLiuHai()
    self._notchWidth = XH.UITool.getLiuHaiWidth()
    local nodeListName = {"_imgBg", "_btnOnline"}
    local nodeList = {}
    for _,name in pairs(nodeListName) do
        nodeList[#nodeList + 1] = self[name]
    end
    XH.UITool.adaptForLiuHai(nodeList)
end

function IMTeaHouseOnlineListView:hideNoticeText()
    self._textNotice:setVisible(false)
end

function IMTeaHouseOnlineListView:showNoticeText(type)
    if type == IMTeaHouseOnlineListView.NoticeType.NoMember then
        self._textNotice:setString("暂无列表数据")
    else
        self._textNotice:setString("获取列表数据中...")
    end
    self._textNotice:setVisible(true)
end

function IMTeaHouseOnlineListView:selectListType(listType)
    self._selectListType = listType
    self:updateListTypeBtns()
    self:clearList()
    self:reqListInfo()
end

function IMTeaHouseOnlineListView:reqListInfo()
    local teaid = XH.lobby:getModule("Im"):getIMData():getEnterTeaHouseID()
    if self._selectListType == IMTeaHouseOnlineListView.OnlineListType.Member then
        XH.lobby:getModule("Im"):reqGroupMemberList(teaid)
    else
        XH.lobby:getModule("Im"):reqSelfFightInfo(teaid)
    end
end

function IMTeaHouseOnlineListView:clearList()
    self._infoList = {}
    self:refreshTableView()
end

function IMTeaHouseOnlineListView:updateListTypeBtns()
    self._btnMember:setEnabled(self._selectListType == IMTeaHouseOnlineListView.OnlineListType.Lately)
    self._btnLately:setEnabled(self._selectListType == IMTeaHouseOnlineListView.OnlineListType.Member)
    if self._selectListType == IMTeaHouseOnlineListView.OnlineListType.Lately then
        self._btnMember:loadTexture("tea_house_online_member_list_1.png", ccui.TextureResType.plistType)
        self._btnLately:loadTexture("tea_house_online_last_list_2.png", ccui.TextureResType.plistType)
    else
        self._btnMember:loadTexture("tea_house_online_member_list_2.png", ccui.TextureResType.plistType)
        self._btnLately:loadTexture("tea_house_online_last_list_1.png", ccui.TextureResType.plistType)
    end
    self._btnMember:ignoreContentAdaptWithSize(true)
    self._btnLately:ignoreContentAdaptWithSize(true)
    if self._btnRecall then
        self._btnRecall:setVisible(false)
    end
end

function IMTeaHouseOnlineListView:onBtnEventMember(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    self:selectListType(IMTeaHouseOnlineListView.OnlineListType.Member)
end

function IMTeaHouseOnlineListView:onBtnEventLately(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:selectListType(IMTeaHouseOnlineListView.OnlineListType.Lately)
end

function IMTeaHouseOnlineListView:onEventMask(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    self:moveAnimation()
end

function IMTeaHouseOnlineListView:sortListData(listData)
    if self._selectListType == IMTeaHouseOnlineListView.OnlineListType.Lately then
        table.sort(listData, function(a, b)
            if a.nLastFightTime and b.nLastFightTime then
                return a.nLastFightTime > b.nLastFightTime
            end
        end)
    end

    local tmpListData = {}
    for _, value in pairs(listData) do
        local acExtraData = {}
        local ok = pcall(function()
            json.decode(value.stUserInfo.acExtraData)
        end)
        if ok then
            acExtraData = json.decode(value.stUserInfo.acExtraData)
        end
        value.stUserInfo.ucGameStatus = acExtraData.state or 0
        value.stUserInfo.nGameTime = acExtraData.nGameTime or 0
        if value.stUserInfo.ucGameStatus >= 2 then
            value.stUserInfo.ucGameStatus = 2
        end

        if self._selectListType == IMTeaHouseOnlineListView.OnlineListType.Lately then
            if #tmpListData >= KW_ONLINE_LIST_LATELY_SHOW_NUM then
                break
            end
        end
        if XH.playerData:getNumberID() ~= value.stUserInfo.nNumId then
            table.insert(tmpListData, value)
        end
    end

    table.sort(tmpListData, function(a, b)
        local valueA = KW_ONLINE_LIST_STATE_WEIGHT[a.stUserInfo.ucGameStatus]
        local valueB = KW_ONLINE_LIST_STATE_WEIGHT[b.stUserInfo.ucGameStatus]

        valueA = valueA or 0
        valueB = valueB or 0

        if XH.playerData:getNumberID() == a.stUserInfo.nNumId then
            valueA = valueA + 1
        end

        if XH.playerData:getNumberID() == b.stUserInfo.nNumId then
            valueB = valueB + 1
        end

        return valueA > valueB
    end)

    return tmpListData
end

function IMTeaHouseOnlineListView:onUpdateMemberList(event)
    self._infoList = self:sortListData(event.info)
    if #self._infoList == 0 then
        self:showNoticeText(IMTeaHouseOnlineListView.NoticeType.NoMember)
    else
        self:hideNoticeText()
    end
    self:refreshTableView()
end

function IMTeaHouseOnlineListView:onUpdateLatelyList(event)
    self._infoList = self:sortListData(event.msg.info)
    if #self._infoList == 0 then
        self:showNoticeText(IMTeaHouseOnlineListView.NoticeType.NoMember)
    else
        self:hideNoticeText()
    end
    self:refreshTableView()
end

function IMTeaHouseOnlineListView:refreshTableView(isCleanup)
    if not self._panelList then
        return
    end
    if self._tvOnlineList ~= nil and isCleanup then
        self._tvOnlineList:removeFromParent()
        self._tvOnlineList = nil
    end
    if self._tvOnlineList == nil then
        self._tvOnlineList = cc.TableView:create(self._panelList:getContentSize())
        self._tvOnlineList:setDirection(cc.SCROLLVIEW_DIRECTION_VERTICAL)
        self._tvOnlineList:setVerticalFillOrder(cc.TABLEVIEW_FILL_TOPDOWN)
        self._tvOnlineList:setIgnoreAnchorPointForPosition(false)
        self._tvOnlineList:setAnchorPoint(cc.p(0, 0))
        self._tvOnlineList:setPosition(cc.p(0, 0))
        self._panelList:addChild(self._tvOnlineList)
        self._tvOnlineList:registerScriptHandler(handler(self, self.onTableCellTouched), cc.TABLECELL_TOUCHED)
        self._tvOnlineList:registerScriptHandler(handler(self, self.onCellSizeForTable), cc.TABLECELL_SIZE_FOR_INDEX)
        self._tvOnlineList:registerScriptHandler(handler(self, self.onTableCellAtIndex), cc.TABLECELL_SIZE_AT_INDEX)
        self._tvOnlineList:registerScriptHandler(handler(self, self.onNumberOfCellsInTableView), cc.NUMBER_OF_CELLS_IN_TABLEVIEW)
    end
    if self._tvOnlineList ~= nil then
        self._tvOnlineList:reloadData()
    end
end

function IMTeaHouseOnlineListView:onTableCellTouched(tableView, cell)

end

function IMTeaHouseOnlineListView:onCellSizeForTable(tableView, index)
    if self._panelListItem then
        local size = self._panelListItem:getContentSize()
        return size.width, size.height
    end
    return 0, 0
end

function IMTeaHouseOnlineListView:onTableCellAtIndex(tableView, index)
    local cell = tableView:dequeueCell()
    if not cell then
        cell = cc.TableViewCell:new()
        local onlineItem = XH.viewManager:createView("IMTeaHouseOnlineListItem","src.lobby.Modules.IMTeaHouse.IMTeaHouseOnlineListItem")
        if onlineItem ~= nil then
            onlineItem:setAnchorPoint(cc.p(0, 0))
            onlineItem:setPosition(0, 0)
            onlineItem:setName("OnlineItem")
            if self._panelListItem then
                onlineItem:setContentSize(self._panelListItem:getContentSize())
                ccui.Helper:doLayout(onlineItem)
            end
            cell:addChild(onlineItem)
        end
    end
    local onlineItem = cell:getChildByName("OnlineItem")
    if onlineItem ~= nil then
        onlineItem:updateInfo(self._infoList[index + 1], self._inviteOfflineFunc,self._selectListType)
    end
    return cell
end

function IMTeaHouseOnlineListView:onNumberOfCellsInTableView(tableView)
    return #self._infoList
end

function IMTeaHouseOnlineListView:onBtnEventInviteAll(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    
    local onlinePlayer = {}
    for _, v in pairs(self._infoList) do
        if v.stUserInfo.ucGameStatus and v.stUserInfo.ucGameStatus == KW_ONLINE_STATE then
            table.insert(onlinePlayer, v.stUserInfo.nNumId)
        end
    end
    if next(onlinePlayer) then
        XH.lobby:getModule("Im"):reqInvite(onlinePlayer)
    else
        XH.TipTool.showToast("暂无好友在线")
    end
end

function IMTeaHouseOnlineListView:onBtnEventOnline(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    cc.SpriteFrameCache:getInstance():addSpriteFrames("res/cocosStudio/TeaHouse/Image/tea_house_online_list.plist")
    if not self._bMoveIn then
        self:selectListType(self._selectListType)
    end

    self:moveAnimation()
end

function IMTeaHouseOnlineListView:moveAnimation()
    self._bMoveIn = not self._bMoveIn
    local bMoveIn = self._bMoveIn
    self._panelMask:setTouchEnabled(bMoveIn)
    if self._imgBg then
        local actionTime = 0.2
        self._imgBg:stopAllActions()
        local targetPosX = self._imgBg:getContentSize().width + self._notchWidth
        local targetPosY = self._imgBg:getPositionY()
        local actSpawn = cc.Spawn:create(cc.FadeIn:create(actionTime),cc.MoveTo:create(actionTime, cc.p(targetPosX, targetPosY)))
        local actSequence = cc.Sequence:create(cc.CallFunc:create(function()
                                self._imgBg:setVisible(true)
                            end), actSpawn)
        if not bMoveIn then
            targetPosX = self._notchWidth
            actSpawn = cc.Spawn:create(cc.FadeOut:create(actionTime),cc.MoveTo:create(actionTime, cc.p(targetPosX, targetPosY)))
            actSequence = cc.Sequence:create(actSpawn, cc.CallFunc:create(function()
                                self._imgBg:setVisible(false)
                            end))
        end
        self._imgBg:runAction(actSequence)
    end
    self:moveEntranceBtn(bMoveIn)
end

function IMTeaHouseOnlineListView:moveEntranceBtn(bMoveIn)
    if self._btnOnline and self._imgBg then
        local actionTime = 0.2
        self._btnOnline:stopAllActions()
        local targetPosX = self._imgBg:getContentSize().width + self._notchWidth - self.KW_LIST_BG_WHITE_WIDTH
        if not bMoveIn then
            targetPosX = self._notchWidth
        end
        local targetPosY = self._btnOnline:getPositionY()
        local actSpawn = cc.Spawn:create(cc.MoveTo:create(actionTime, cc.p(targetPosX, targetPosY)))
        self._btnOnline:runAction(actSpawn)
    end
end

function IMTeaHouseOnlineListView:updateOnlineNum(event)
    local data = XH.lobby:getModule("Im"):getIMData():getGroupList()
    local teaNumber = XH.lobby:getModule("Im"):getIMData():getEnterTeaHouseID()
    for _, v in pairs(data) do
        if v.nTeaNumber == teaNumber then
            self._textOnline:setString(string.format("%d/%d", v.nOnLineUserCnt, v.nUserCnt))
        end
    end
end

function IMTeaHouseOnlineListView:onHideEntrance(event)
    self._bMoveIn = true
    self:moveAnimation()
    if self._btnOnline then
        self._btnOnline:setVisible(false)
    end
end

return IMTeaHouseOnlineListView <  