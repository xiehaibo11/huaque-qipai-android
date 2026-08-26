local IMListView = class("IMListView", XH.ViewBase)
local SxVipConfig = require('lobby.Modules.Sxvip.Config')

local MAX_PLAYER_COUNT = 4
local KW_ONE_DAY = 24*3600  -- 一天时长
local TABLEVIEW_CELL_INTERVAL = 10  -- tableView子节点间隔
local KW_OPEN_BTN_WHITE_WIDTH = 15  -- 打开按钮留白长度
local KW_FRAME_DEFULT_SIZE = cc.size(190, 190)  -- 头像框默认大小
local KW_WIDGET_HEIGHT = 5 -- 列表容器子控件间距

local KW_IMG_AWARD_BACK = "friend_award_back.png"
local KW_IMG_FRIENDLY_TIP = "friend_action_friendly_tips.png"
local KW_IMG_DEFULT_HEAD = "_0002_mrtx_w.png"
--local KW_IMG_DEFULT_FRAME = "img_head_frame.png"

IMListView.KW_PLAYER_STATE_PIC = {
    [1] = "friend_state_off_line.png",
    [2] = "friend_state_gaming.png",
    [4] = "friend_state_on_line.png",
    [8] = "friend_state_wait.png",
}

IMListView.KW_ACTION_PIC = {
    [1] = "friend_action_wechat_invite.png",
    [2] = "friend_action_order.png",
    [4] = "friend_action_online_invite.png",
    [8] = "friend_action_online_invite.png",
}

IMListView.KW_OPEN_BTN_PIC = {
    CLOSE = "friend_btn_open_1.png",
    OPEN = "friend_btn_open_2.png",
    INGAME = "friend_btn_open_in_game.png",
}

IMListView.TABLEVIEWTYPE = {
    FRIENDLISTVIEW = {
        parent = "_friendListView",
        item = "_playerItem"
    },
    TABELLISTVIEW = {
        [1] = {
            parent = "_tableListView",
            item = "_boxItem"
        },
        [2] = {
            parent = "_tableListView",
            item = "_boxMoreItem"
        },
        [3] = {
            parent = "_tableListView",
            item = "_teaItem"
        },
        [4] = {
            parent = "_tableListView",
            item = "_teaMoreItem"
        },
    },
    READYLISTVIEW = {
        parent = "_readyListView",
        item = "_readyItem"
    },
}

IMListView.KW_DEFULT_CHANNE_ID = {0, 30001, 30002}

function IMListView:getCSBPath()
    return "cocosStudio/hall/CSB/IM/IMListLayer.csb"
end

function IMListView:getBindingInfo()
    return {
        ["_KW_PANEL_ADAPT"] = { varName = "_panelAdapt", onTouchEnded = "on_KWA_BG_PANEL" },
        ["_KW_UI_OPEN_PIC"] = { varName = "_openBtnPic", onTouchEnded = "on_KW_BTND_OPEN"},
        ["_KWA_FRIEND_PANEL"] = { varName = "_friendPanel", onTouchEnded = "on_KWA_FRIEND_PANEL" },
        ["_KWA_LIST_PANEL"] = { varName = "_listPanel" },
        ["_KW_LABLE_LIST"] = { varName = "_lableList", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KW_LABLE_LIST" },
        ["_KW_LABLE_FOLLOW"] = { varName = "_lableFollow", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KW_LABLE_FOLLOW" },
        ["_KW_LABLE_RECALL"] = { varName = "_lableRecall", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KW_LABLE_RECALL" },
        ["_KW_IMG_HAVE_AWARD"] = { varName = "_iconHaveAward" },
        ["_KW_FRIEND_LIST_VIEW"] = { varName = "_friendListView" },
        ["_KW_TABLE_LIST_VIEW"] = { varName = "_tableListView" },
        ["_KW_PLAYER_ITEM"] = { varName = "_playerItem" },
        ["_KW_TABLE_ITEM"] = { varName = "_tableItem" },
        ["_KW_TEA_ITEM"] = { varName = "_teaItem" },
        ["_KW_TEA_MORE_ITEM"] = { varName = "_teaMoreItem" },
        ["_KW_LOBBY_ITEM"] = { varName = "_boxItem" },
        ["_KW_LOBBY_MORE_ITEM"] = { varName = "_boxMoreItem" },
        ["_KW_UI_QIPAO"] = { varName = "_tableTipQiPao" },
        ["_KW_HEAD_1"] = { varName = "_tableHead1" },
        ["_KW_HEAD_2"] = { varName = "_tableHead2" },
        ["_KW_HEAD_3"] = { varName = "_tableHead3" },
        ["_KW_HEAD_4"] = { varName = "_tableHead4" },
        ["_KW_UI_ACTION"] = { varName = "_btnAction" },
        ["_KW_INVITE_TIPS"] = { varName = "_inviteTip" },
        ["_KW_REFRESH_TIPS"] = { varName = "_refreshTip" },
        ["_KW_UI_TIPS_NO_FRIEND"] = { varName = "_noFriendTip"},
        ["_KW_UI_TIPS_NO_TABLE"] = { varName = "_noTableTip"},
        ["_KW_UI_TIPS_SEARCH_WITH_NO_FRIEND"] = { varName = "_searchTipWithNoFriend"},
        ["_KW_UI_QIPAO_SEARCH"] = { varName = "_qpSearch"},
        ["_KW_READY_PANEL"] = { varName = "_readyPanel", onTouchEnded = "on_KW_BTND_OPEN" },
        ["_KW_READY_LIST_VIEW"] = { varName = "_readyListView" },
        ["_KW_READY_ITEM"] = { varName = "_readyItem" },
        ["_KW_UI_TIPS_READY_NO_FRIEND"] = { varName = "_noFriendReadyTip" },
        ["_KW_PANNEL_MESSAGE"] = { varName = "_msgPanel" },
        ["_KW_UI_OPEN_PIC_IN_GAME"] = { varName = "_openBtnPicInGame" },
        ["_KW_UI_OPEN_IN_GAME"] = { varName = "_openBtnInGame", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KW_BTND_OPEN" },
        ["_KW_MSG_PANEL_IN_GAME"] = { varName = "_msgPanelInGame" },
        ["_KW_NODE_FRIEND_LIST"] = { varName = "_nodeFriendList" },
        ["_KW_PANEL_RECALL"] = { varName = "_panelRecall" },
        -- 旁观玩家UI
        ["_KW_IMG_RED_POINT"] = { varName = "_imgRedPoint" },
        ["_KW_IMG_QUE1REN"] = { varName = "_toastTips" },
    }
end

function IMListView:ctor() 
    IMListView.super.ctor(self)
    
    self._recallView = nil
    self:initData()
    self:initUI()
    self:updateSeerUI()
end

function IMListView:initData()
    self._bMoveIn = false
    self._bReadyState = true
    self._curFriendListDataCount = 0
    self._curTableListDataCount = 0
    self._notchWidth = 0
    self._firstIn = true

    --请求好友列表
    XH.taskManager:reqFriendAwardGetState()
    XH.lobby:getModule("Im"):setCurFriendListPage(0)
    XH.lobby:getModule("Im"):setTotalFriendListPage(0)
    XH.lobby:getModule("Im"):getIMData():clearFriendList()
    XH.lobby:getModule("Im"):reqFriendListInfo()

    self:reqHasRecallAward()
end

function IMListView:onEnter()
    IMListView.super.onEnter(self)
    self:initListenerEventReGetAwardData()
    self._listenerEventReGetAwardData = cc.EventListenerCustom:create("RecallModule.ReGetAwardData", handler(self, self.onReGetAwardData))
    cc.Director:getInstance():getEventDispatcher():addEventListenerWithFixedPriority(self._listenerEventReGetAwardData, 1)
end

function IMListView:onExit()
    IMListView.super.onExit(self)
    self:initListenerEventReGetAwardData()
end

function IMListView:onReGetAwardData()
    self:reqHasRecallAward()
end

function IMListView:initListenerEventReGetAwardData()
    if self._listenerEventReGetAwardData then
        cc.Director:getInstance():getEventDispatcher():removeEventListener(self._listenerEventReGetAwardData)
    end
    if self._updateFriendStatusScheduler then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._updateFriendStatusScheduler)
        self._updateFriendStatusScheduler = nil
    end
    self._listenerEventReGetAwardData = nil
end

function IMListView:onDestroy()
    IMListView.super.onDestroy(self)
    self:initListenerEventReGetAwardData()
end

function IMListView:initUI()
    self:adaptForLiuHai()
    self._panelAdapt:setTouchEnabled(false)
    for k, v in pairs(self.TABLEVIEWTYPE) do
        if k == "TABELLISTVIEW" then
            self:initTableView(self[v[1].parent], k)
        elseif self[v.parent] then
            self:initTableView(self[v.parent], k)
        end
    end

    self:initMsgUI(self._msgPanel)

    local date = os.date("%Y-%m-%d")
    if not cc.UserDefault:getInstance():getBoolForKey("IMListLayer_First_Open_" .. date, false) then
        self:on_KW_BTND_OPEN()
        cc.UserDefault:getInstance():setBoolForKey("IMListLayer_First_Open_" .. date, true)
    end

    if XH.gameManager and not XH.gameManager:isPlayBack() then
        self:initUIInGame()
        if not self:getData():getIsSeer() then
            self:showListLayer()
        end
    end
    if XH.areaData:getEnterLobbyNeedOpenIm() then
        self:on_KW_BTND_OPEN()
        XH.areaData:setEnterLobbyNeedOpenIm(false)
    end

    self:setIconHaveAwardVisible(false)
end

function IMListView:getData()
    if not self._imData then
        self._imData = XH.lobby:getModule("Im"):getIMData()
    end
    return self._imData
end

function IMListView:adaptForLiuHai()
    self._notchWidth = XH.UITool.getLiuHaiWidth()
    local nodeListName = {"_KWA_FRIEND_PANEL", "_KW_READY_PANEL", "_KW_UI_OPEN_PIC", "_KW_UI_OPEN_PIC_IN_GAME"}
    local nodeList = {}
    for _,name in pairs(nodeListName) do
        local tmpNode = XH.UITool.seekNodeByName(self, name)
        nodeList[#nodeList + 1] = tmpNode
    end
    XH.UITool.adaptForLiuHai(nodeList)
end

function IMListView:initMsgUI(parentNode)
    --初始化好友消息入口
    if not parentNode then
        return
    end
    self:closeLobbyImMsgView()
    local tmpNode = require("lobby.Modules.Im.MessageView").new()
    parentNode:removeAllChildren()
    parentNode:addChild(tmpNode)
    tmpNode:setPosition(0, 0)
end

function IMListView:initUIInGame()
    self._openBtnPic:setVisible(false)
    self._readyPanel:setVisible(false)
    self._friendPanel:setPositionX(0)
    self._openBtnPicInGame:setVisible(true)
    self:initMsgUI(self._msgPanelInGame)
end

function IMListView:closeLobbyImMsgView()
    local children = self._msgPanel:getChildren()
    if #children > 0 then
        if children[1].close then
            children[1]:close()
        end
    end
end

function IMListView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_HIDE_LISTLAYER", callBack = "moveAnimation" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_HIDE_LIST_ENTRANCE", callBack = "hideListEntrance" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_SHOW_LIST_ENTRANCE", callBack = "showListEntrance" },
        { module = self:getData(), eventKeyName = "EVENT_LIST_DATA_CHANGED", callBack = "onRefreshList" },
        { module = XH.taskManager,eventKeyName = "FRIEND_RECALL_AWARD_STATE", callBack = "updateRecallAwardState"},
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_SHOW_LISTLAYER", callBack = "openListLayer" },
        { module = self:getData(), eventKeyName = "EVENT_IM_UPDATE_SEER", callBack = "updateSeerUI" },
    }
end

function IMListView:updateActionBtn()
    if self._bReadyState then
        return
    end

    self._btnAction:ignoreContentAdaptWithSize(true)
    if self:getData():getLable() == self:getData().KW_LIST_TYPE.NONE or self:getData():getLable() == self:getData().KW_LIST_TYPE.PLAYERLIST then
        self._btnAction:setColor(cc.c3b(255,255,255))
        self._inviteTip:setVisible(false)
        self._refreshTip:setVisible(false)

        self._btnAction:loadTexture("friend_invite_all.png", ccui.TextureResType.plistType)
        self._btnAction:addTouchEventListener(handler(self, self.on_KW_BTND_INVITE_ALL))
    else
        self._btnAction:loadTexture("friend_btn_refresh_list.png", ccui.TextureResType.plistType)
        self._inviteTip:setVisible(false)
        self._refreshTip:setVisible(false)
        self._btnAction:addTouchEventListener(handler(self, self.onTouchEventRefreshTableList))
        self._btnAction:setEnabled(true)
        self._btnAction:setColor(cc.c3b(255,255,255))
    end
end

function IMListView:initTableView(parentNode, viewName)
    local tableView = cc.TableView:create(parentNode:getContentSize())
    tableView:setAnchorPoint(cc.p(0, 0))
    tableView:setPosition(cc.p(0, 0))
    tableView:registerScriptHandler(handler(self, self.onTableViewCellSizeForTable), cc.TABLECELL_SIZE_FOR_INDEX)
    tableView:registerScriptHandler(handler(self, self.onTableViewCellAtIndex), cc.TABLECELL_SIZE_AT_INDEX)
    tableView:registerScriptHandler(handler(self, self.numberOfCellsInTableView), cc.NUMBER_OF_CELLS_IN_TABLEVIEW)
    tableView:registerScriptHandler(handler(self, self.scrollTableViewDidScroll), cc.SCROLLVIEW_SCRIPT_SCROLL)
    tableView:setVerticalFillOrder(cc.TABLEVIEW_FILL_TOPDOWN)
    tableView:setDirection(cc.SCROLLVIEW_DIRECTION_VERTICAL)
    tableView:setName(viewName)
    tableView:setDelegate()
    if viewName == "READYLISTVIEW" then
        tableView:setTouchEnabled(false)
    end
    parentNode:addChild(tableView)
end

function IMListView:onTableViewCellSizeForTable(view, index)
    local item = self._tableItem
    if view:getName() == "TABELLISTVIEW" then
        local dataType = self:getTableDataTypeByIndex(index + 1)
        if self.TABLEVIEWTYPE[view:getName()][dataType] then
            item = self[self.TABLEVIEWTYPE[view:getName()][dataType].item]
        end
        if dataType == self:getData().TABLEDATATYPE.TEAOPEN or dataType == self:getData().TABLEDATATYPE.NORMALOPEN then
            local itemHeight = item:getContentSize().height + TABLEVIEW_CELL_INTERVAL
            local tableListData = self:getData():getTableList()
            local tables = tableListData[index + 1].tables
            itemHeight = itemHeight + #tables * (self._tableItem:getContentSize().height + KW_WIDGET_HEIGHT)
            return item:getContentSize().width, itemHeight
        end
    else
        item = self[self.TABLEVIEWTYPE[view:getName()].item]
    end
    return item:getContentSize().width, item:getContentSize().height + TABLEVIEW_CELL_INTERVAL
end

function IMListView:onTableViewCellAtIndex(view, index)
    local cell = view:dequeueCell()
    if not cell then
        cell = cc.TableViewCell:new()
        if view:getName() == "TABELLISTVIEW" then
            for _, v in pairs(self.TABLEVIEWTYPE[view:getName()]) do
                self:setTableViewItem(cell, self[v.item])
            end
        else
            self:setTableViewItem(cell, self[self.TABLEVIEWTYPE[view:getName()].item])
        end
    end

    local item
    if view:getName() == "TABELLISTVIEW" then
        local dataType = self:getTableDataTypeByIndex(index + 1)
        item = cell:getChildByName(self[self.TABLEVIEWTYPE[view:getName()][dataType].item]:getName())
        for _, v in pairs(self.TABLEVIEWTYPE[view:getName()]) do
            cell:getChildByName(self[v.item]:getName()):setVisible(false)
        end
        local listView = XH.UITool.seekNodeByName(cell, "_KW_LIST_VIEW_MORE")
        listView:setContentSize(listView:getContentSize().width, 0)
        listView:removeAllItems()
    else
        item = cell:getChildByName(self[self.TABLEVIEWTYPE[view:getName()].item]:getName())
    end

    item:setVisible(true)
    local dataList = self:getTableViewData(view)
    local data = dataList[index + 1]
    if data then
        if view:getName() == "TABELLISTVIEW" then
            self:setTableInfo(item, data, index)
        elseif view:getName() == "FRIENDLISTVIEW" then
            self:setFriendInfo(item, data, index)
        elseif view:getName() == "READYLISTVIEW" then
            self:setReadyInfo(item, data, index)
        end
    end

    return cell
end

function IMListView:numberOfCellsInTableView(view)
    return #self:getTableViewData(view)
end

function IMListView:scrollTableViewDidScroll(view)
    local listSize = view:getContentSize()    -- 总大小
    if listSize.height == 0 then return end     -- 分母不能为0
    local offset = view:getContentOffset()    -- 取滑动偏移
    if view:getVerticalFillOrder() == cc.TABLEVIEW_FILL_TOPDOWN then
        offset.y = offset.y + listSize.height   -- 从起点开始偏移量
    end
    local viewData = self:getTableViewData(view)
    local endIdx
    if view:getName() == "TABELLISTVIEW" then
        endIdx = 1
        local totalItemHeight = 0
        for i = 1, #viewData do
            if viewData[i].tableType == self:getData().TABLEDATATYPE.NORMAL or viewData[i].tableType == self:getData().TABLEDATATYPE.TEANOTOPEN then
                totalItemHeight = totalItemHeight + self._teaItem:getContentSize().height
            elseif viewData[i].tableType == self:getData().TABLEDATATYPE.NORMALOPEN or viewData[i].tableType == self:getData().TABLEDATATYPE.TEAOPEN then
                totalItemHeight = totalItemHeight + self._teaItem:getContentSize().height + #viewData[i].tables * (self._tableItem:getContentSize().height + KW_WIDGET_HEIGHT)
            end
            endIdx = i
            if offset.y < totalItemHeight then
                break
            end
        end
    else
        local item = self:getTableViewItemModel(view)
        endIdx = offset.y / item:getContentSize().height + 1
    end
    if endIdx > #viewData then
        endIdx = #viewData
    end

    -- 滑动到显示倒数第二个的时候回调
    if endIdx > #viewData - 2 and not self._isTableViewBottom then
        self._isTableViewBottom = true
        self:reqTableViewInfo(view)
    end
    if endIdx < #viewData - 2 then
        self._isTableViewBottom = false
    end
end

function IMListView:getTimeLen(lastTime)
    if not lastTime then
        return ""
    end
    local subTime = os.time() - lastTime
    if subTime <= KW_ONE_DAY then
        return math.ceil(subTime / 3600) .. "小时前在线"
    elseif subTime <= 7 * KW_ONE_DAY then
        return math.ceil(subTime / KW_ONE_DAY) .. "天前在线"
    else
        return "7天前在线"
    end
end

function IMListView:showListLayer()
    self._bMoveIn = not self._bMoveIn
    self._bReadyState = not self._bMoveIn
    if next(self:getData():getFriendList()) ~= nil then
        local tmpPage = XH.lobby:getModule("Im"):getCurFriendListPage() - 1 >= 0 and  XH.lobby:getModule("Im"):getCurFriendListPage() - 1 or 0
        for i = 0, tmpPage do
            XH.lobby:getModule("Im"):reqFriendListInfoEx(i, i)
        end
    else
        XH.lobby:getModule("Im"):reqFriendListInfo()
    end
    if self._bMoveIn then
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_20)  
        XH.lobby:getModule("Im"):reqMixTableListInfo()
        display.performWithDelay(self,function()
            -- 进入房间自动打开好友列表
            if XH.gameManager then
                self:on_KW_LABLE_LIST(self._lableList, ccui.TouchEventType.ended)
            else
                -- 若有等待中的桌子自动打开桌子列表
                local tableListData = self:getData():getTableList()
                if next(tableListData) ~= nil and tableListData[1].tables and next(tableListData[1].tables) ~= nil then
                    self:on_KW_LABLE_FOLLOW(self._lableFollow, ccui.TouchEventType.ended)
                else
                    -- 以上条件均不满足时默认打开上次的页签
                    if self:getData():getLable() <= self:getData().KW_LIST_TYPE.PLAYERLIST then
                        self:on_KW_LABLE_LIST(self._lableList, ccui.TouchEventType.ended)
                    elseif self:getData():getLable() == self:getData().KW_LIST_TYPE.TABLELIST then
                        self:on_KW_LABLE_FOLLOW(self._lableFollow, ccui.TouchEventType.ended)
                    end
                    self:updateActionBtn()
                end
            end
        end,0.2)
    end
    self:moveAnimation(self._bMoveIn)
end

function IMListView:moveAnimation(bMoveIn)
    if bMoveIn then
        self._panelAdapt:setTouchEnabled(bMoveIn)
    end
    if not bMoveIn and XH.gameManager then
        self:moveOutAnimationInGame()
        return
    end
    if self._friendPanel then
        local actionTime = 0.2
        self._friendPanel:stopAllActions()
        local targetPosX = self._listPanel:getContentSize().width + self._notchWidth
        local targetPosY = self._friendPanel:getPositionY()
        local actSpawn = cc.Spawn:create(cc.FadeIn:create(actionTime),cc.MoveTo:create(actionTime, cc.p(targetPosX, targetPosY)))
        local actSequence = cc.Sequence:create(cc.CallFunc:create(function()
                                self._friendPanel:setVisible(true)
                                if not XH.gameManager then
                                    self._readyPanel:setVisible(false)
                                    self._openBtnPic:loadTexture(self.KW_OPEN_BTN_PIC.CLOSE, ccui.TextureResType.plistType)
                                end
                            end), actSpawn)
        if not bMoveIn then
            targetPosX = self._readyPanel:getContentSize().width *  self._readyPanel:getScale() + self._notchWidth
            actSpawn = cc.Spawn:create(cc.FadeOut:create(actionTime),cc.MoveTo:create(actionTime, cc.p(targetPosX, targetPosY)))
            actSequence = cc.Sequence:create(actSpawn, cc.CallFunc:create(function()
                                self._friendPanel:setVisible(false)
                                self._readyPanel:setVisible(true)
                                local date = os.date("%Y-%m-%d")
                                if not cc.UserDefault:getInstance():getBoolForKey("IMListLayer_First_Close_" .. date, false) then
                                    XH.lobby:getModule("Im"):reqFriendListInfo()
                                    cc.UserDefault:getInstance():setBoolForKey("IMListLayer_First_Close_" .. date, true)
                                end
                                local totalNum = #self:getShowData()
                                self:updateTips(totalNum)
                                self._openBtnPic:loadTexture(self.KW_OPEN_BTN_PIC.OPEN, ccui.TextureResType.plistType)
                                self._panelAdapt:setTouchEnabled(false)
                            end))
        end
        self._friendPanel:runAction(actSequence)
    end
    if XH.gameManager then
        self:moveEntranceBtnInGame(true)
    else
        self:moveEntranceBtn(bMoveIn)
    end
end

function IMListView:moveOutAnimationInGame()
    if self._friendPanel then
        local actionTime = 0.2
        self._friendPanel:stopAllActions()
        local targetPosY = self._friendPanel:getPositionY()
        local targetPosX = self._notchWidth
        local actSpawn = cc.Spawn:create(cc.FadeOut:create(actionTime),cc.MoveTo:create(actionTime, cc.p(targetPosX, targetPosY)))
        local actSequence = cc.Sequence:create(actSpawn, cc.CallFunc:create(function()
                            self._friendPanel:setVisible(false)
                            self._panelAdapt:setTouchEnabled(false)
                        end))
        self._friendPanel:runAction(actSequence)
    end
    self:moveEntranceBtnInGame(false)
end

function IMListView:moveEntranceBtn(bMoveIn)
    if self._openBtnPic and self._listPanel then
        local actionTime = 0.2
        self._openBtnPic:stopAllActions()
        local targetPosX = self._listPanel:getContentSize().width - KW_OPEN_BTN_WHITE_WIDTH + self._notchWidth
        if not bMoveIn then
            targetPosX = self._readyPanel:getContentSize().width *  self._readyPanel:getScale() - KW_OPEN_BTN_WHITE_WIDTH + self._notchWidth
            if self._notchWidth > 0 then
                targetPosX = targetPosX - KW_OPEN_BTN_WHITE_WIDTH
            end
        end
        local targetPosY = self._openBtnPic:getPositionY()
        local actSpawn = cc.Spawn:create(cc.MoveTo:create(actionTime, cc.p(targetPosX, targetPosY)))
        self._openBtnPic:runAction(actSpawn)
    end
end

function IMListView:moveEntranceBtnInGame(bMoveIn)
    if self._openBtnPicInGame then
        local actionTime = 0.2
        self._openBtnPicInGame:stopAllActions()
        local targetPosX = self._listPanel:getContentSize().width + self._notchWidth
        if not bMoveIn then
            targetPosX = self._notchWidth
        end
        local targetPosY = self._openBtnPicInGame:getPositionY()
        local actSpawn = cc.Spawn:create(cc.MoveTo:create(actionTime, cc.p(targetPosX, targetPosY)))
        self._openBtnPicInGame:runAction(actSpawn)
    end
end

function IMListView:hideListEntrance()
    self._openBtnPic:setVisible(false)
    self._friendPanel:setVisible(false)
    self._readyPanel:setVisible(false)
    self._openBtnPicInGame:setVisible(false)
    self._panelAdapt:setTouchEnabled(false)
end

function IMListView:showListEntrance()
    self._openBtnPic:setVisible(true)
    self._readyPanel:setVisible(true)
    if XH.gameManager then
        self._openBtnPicInGame:setVisible(true)
    end
end

function IMListView:onRefreshList(event)
    if not self._friendListView or not self._tableListView or not self._readyListView then
        return
    end
    cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/hall/Image/friends.plist")
    local tableView = self._readyListView:getChildByName("READYLISTVIEW")
    if not self._bReadyState or XH.gameManager then
        tableView = self._friendListView:getChildByName("FRIENDLISTVIEW")
    end
    if not self._bReadyState and self:getData():getLable() == self:getData().KW_LIST_TYPE.TABLELIST then
        tableView = self._tableListView:getChildByName("TABELLISTVIEW")
    end
    local lastIndex = #self:getTableViewData(tableView)
    local totalNum = #self:getShowData()

    self:updateTips(totalNum)
    self:updateActionBtn()
    self:updateSeerRedPoint()
    -- 旁观玩家处理好友有缺1人开局的
    if self._firstIn then
        self:onUpdateStatus()
        self._firstIn = false
    end

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

function IMListView:updateTips(cnt)
    self._noFriendTip:setVisible(false)
    self._noTableTip:setVisible(false)
    self._searchTipWithNoFriend:setVisible(false)
    self._noFriendReadyTip:setVisible(false)
    if cnt == nil then
        return
    end
    if self._bReadyState then
        if cnt == 0 then
            self._noFriendReadyTip:setVisible(true)
        end
    elseif self:getData():getLable() == self:getData().KW_LIST_TYPE.PLAYERLIST then
        if cnt == 0 then
            self._noFriendTip:setVisible(true)
        end
    elseif self:getData():getLable() == self:getData().KW_LIST_TYPE.TABLELIST then
        self._qpSearch:stopAllActions()
        self._qpSearch:setOpacity(0)
        if cnt == 0 then
            self._noTableTip:setVisible(true)
        end
    end
end

---------------按钮事件---------------
function IMListView:on_KWA_BG_PANEL(send, eventType)
    if not self._bMoveIn then
        return
    end
    self:showListLayer()
end

function IMListView:on_KWA_FRIEND_PANEL(send, eventType)
    self:showListLayer()
end

function IMListView:on_KW_BTND_OPEN(send, eventType)
    self:showListLayer()
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_OPEN_PAIYOU)

    local block_label = ''
    if XH.gameManager then
        if self:getData():getIsSeer() then
            if XH.lobby:getModule("Im"):judgeInTeaHouse() then
                block_label = '比赛场旁观房间'
            else
                block_label = '包厢旁观房间'
            end
        end
    else
        if XH.lobby:getModule("Im"):judgeInTeaHouse() then
            block_label = '比赛场大厅'
        else
            block_label = '大厅'
        end
    end
    if block_label ~= '' then
        local postion = XH.playerData:getPlayerPosition()
        local data = {
            block_label = block_label,
            item_id = postion.gameID,
        }
        local customize = {
            role = XH.lobby:getModule("Sxvip"):isExpire() and '非会员' or '是会员'
        }
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.zyt2024032809, data, customize)
    end
end

function IMListView:on_KW_LABLE_LIST(send, eventType)
    self._nodeFriendList:setVisible(true)
    self._panelRecall:setVisible(false)
    self:updateLableUI(true)
    self:getData():setLable(self:getData().KW_LIST_TYPE.PLAYERLIST)
    self:updateActionBtn()
    self:onRefreshList()

    self._friendListView:setVisible(true)
    self._tableListView:setVisible(false)

    self:showJoinTableTip(false)  
    if self.PlayerbuttonChanged == true then 
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_23, {tabname = "牌友列表" })
        self.PlayerbuttonChanged = false
    end 

    -- 抛送列表曝光数据
    local sceneName = XH.lobby:getModule("Im"):judgeSceneName()
    local data = {
        block_label = sceneName
    }
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyxt24052701, data)
end

function IMListView:on_KW_LABLE_FOLLOW(send, eventType)
    self._nodeFriendList:setVisible(true)
    self._panelRecall:setVisible(false)
    self:updateLableUI(false)
    self:getData():setLable(self:getData().KW_LIST_TYPE.TABLELIST)
    self:updateActionBtn()
    self:onRefreshList()

    self._friendListView:setVisible(false)
    self._tableListView:setVisible(true)
    self.PlayerbuttonChanged = true
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_23, {tabname = "即将开始" })

    -- 抛送最近曝光数据
    local sceneName = XH.lobby:getModule("Im"):judgeSceneName()
    local data = {
        block_label = sceneName
    }
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyxt24052713, data)
end

function IMListView:initRecallPanel()
    if not self._recallView then
        self._recallView = require("lobby.Modules.Recall.View"):new()
        self._panelRecall:addChild(self._recallView)
    end 
end

function IMListView:on_KW_LABLE_RECALL(send, eventType)
    self._nodeFriendList:setVisible(false)
    self._panelRecall:setVisible(true)
    self:initRecallPanel()
    if self._recallView then
        self._recallView:reloadListData()
    end
    self:updateLableUI(nil, true)
    local conventionData = {}
    conventionData.page = "P1063"
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RECALL_TITLEBTN_CLICK,conventionData, {area_id = XH.areaData:getAreaID()})

    -- 抛送召回曝光数据
    local sceneName = XH.lobby:getModule("Im"):judgeSceneName()
    local data = {
        block_label = sceneName
    }
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.zh24052701, data)
end

function IMListView:setIconHaveAwardVisible(visible)
    if not self._iconHaveAward then
        return
    end
    self._iconHaveAward:setVisible(visible or false)
    if visible then
        XH.SysTool.createShakeAni(self._iconHaveAward)
    else
        self._iconHaveAward:stopAllActions()
    end
end

function IMListView:reqHasRecallAward()
    local req = require("lobby.Req.Recall.ReqHasRecallRewards").new()
    req:addReqCallBack(
        self,
        function(tager, req, nType, vData)
            if nType == XH.Req.TYPE.SUCCESS and target then
                if vData and vData.nXYData and vData.nXYData.recall_count then
                    tager:setIconHaveAwardVisible(tonumber(vData.nXYData.recall_count) > 0)
                else
                    tager:setIconHaveAwardVisible(false)
                end
            end
        end
    )
    req:start(10)
end

function IMListView:on_KW_UI_INVITE(playerInfo, send, eventType)
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
    if playerInfo.player_state >= XH.IMProtocol.PLAYERSTATE.PS_ONLINE then
        -- if playerInfo.bInTea then
        --     XH.TipTool.showToast("该用户正在比赛场内，可能暂时无法收到您的邀请")
        --     return
        -- end
        if XH.lobby:getModule("Im"):judgePlayerInRoom() then
            local playerPosition = XH.lobby:getModule("Im"):getPlayerPosition()
            XH.lobby:getModule("Im"):reqInviteGame(playerInfo, playerPosition.roomID)
        end
    elseif playerInfo.player_state == XH.IMProtocol.PLAYERSTATE.PS_GAME then
        if XH.lobby:getModule("Im"):judgePlayerInRoom() then
            XH.lobby:getModule("Im"):reqReserveGame(playerInfo.areaid, playerInfo.numid)
        end
    elseif playerInfo.player_state == XH.IMProtocol.PLAYERSTATE.PS_OFFLINE then
        local playerPos = XH.lobby:getModule("Im"):getPlayerPosition()
        if playerPos.gameAppID and playerPos.gameAppID == 0 then
            XH.lobby:getModule("Im"):onTouchShare()
        else
            XH.SysTool.copyString(playerInfo.nickname)
            XH.lobby:getModule("Im"):reqTableInfo()
        end
    end
    if XH.gameManager then
        XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.BOX_ROOM_INVITE)
    else
        XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_FRIEND_LIST_INVITE)
    end

    -- 抛送点击邀请、预约、在线按钮数据
    local buttonContent = nil
    if playerInfo.player_state == XH.IMProtocol.PLAYERSTATE.PS_OFFLINE then 
        buttonContent = "微信邀请"
    elseif playerInfo.player_state == XH.IMProtocol.PLAYERSTATE.PS_GAME then 
        buttonContent = "预约游戏"
    elseif playerInfo.player_state >= XH.IMProtocol.PLAYERSTATE.PS_ONLINE then 
        buttonContent = "在线邀请"
    end
    local sceneName = XH.lobby:getModule("Im"):judgeSceneName()
    local data = {
        block_item_id = buttonContent,
        block_label = sceneName
    }
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyxt24052702, data)
end

function IMListView:on_KW_UI_WATCH(data, send, eventType)
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
    local throwData = {
        block_label = '牌友列表',
        item_id = tostring(data.gameid),
    }
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.g030601, throwData)
    XH.TipTool.showLoading()
    self._bMoveIn = false
    self:moveAnimation()
    local ConfigurationDefine = require("lobby.Modules.Configuration.Define")
    local lobbyJsonData = XH.lobby:getModule("Configuration"):getConfigJsonData(ConfigurationDefine.Config.LOBBY, "lobby")
    if lobbyJsonData and lobbyJsonData.openGPWatch then
        if XH.gameManager then
            XH.gameManager:leaveGame()
        end
        XH.lobby:getModule("JoinBoxRoom"):reqJoinBoxRoom(data.roomid, nil, true)
    else
        -- 请求boxdata服务新协议进入回放观战模式
        XH.lobby:getModule("Watch"):reqRealtimeGameRecord(data.roomid, 0, data.gameid, data.chair_count)
        if XH.gameManager then
            XH.gameManager:leaveGame(false, true)
        end
    end
end

function IMListView:on_KW_UI_WATCH_VIP(data, send, eventType)
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

    local throwData = {
        block_label = '牌友列表',
        item_id = tostring(data.gameid),
    }
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.g030601, throwData)
    XH.viewManager:openView("SxvipView", nil, {defalutMenu = SxVipConfig.MENU_DEFINE.BUY_VIP})
end

function IMListView:on_BTND_WILL_JOIN(tableInfo, send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._bMoveIn = false
    self:moveAnimation()
    XH.lobby:getModule("Im"):reqWillJoinTable(tableInfo)

    -- 抛送最近点桌子按钮数据
    local sceneName = XH.lobby:getModule("Im"):judgeSceneName()
    local data = {
        block_label = sceneName
    }
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyxt24052714, data)
end

function IMListView:on_BTND_NORMAL_JOIN(tableInfo, send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local playPosition = XH.lobby:getModule("Im"):getPlayerPosition()
    if playPosition and playPosition.gameAppID and playPosition.gameAppID ~= 0 then
        XH.TipTool.showToast("请先退出当前房间后再加入")
        return
    end
    XH.TipTool.showLoading()
    XH.lobby:getModule("JoinBoxRoom"):reqJoinBoxRoom(tableInfo.game_roomid)
end

function IMListView:onTouchEventRefreshTableList(send, eventType)
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
    self:clearListCache()
    XH.lobby:getModule("Im"):reqMixTableListInfo()
    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_REFRESH_LIST_CLICK)
end

function IMListView:on_KW_BTND_INVITE_ALL(send, eventType)
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
    if XH.lobby:getModule("Im"):judgePlayerInRoom() then
        XH.lobby:getModule("Im"):reqQuickInvite()
    end

    -- 抛送一件邀请的按钮点击数据
    local sceneName = XH.lobby:getModule("Im"):judgeSceneName()
    local data = {
        block_label = sceneName
    }
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyxt24052703, data)
end

function IMListView:on_KW_BTND_SEARCH_FRIENDS(send, eventType)
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
    XH.viewManager:openView("IMSearchLoadingView")
end

function IMListView:onTouchEventFriendlyTips(playerInfo, send, eventType)
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
    XH.viewManager:openView("IMTipsLayerView", nil, {ctype = "InviteTips", name = playerInfo.nickname})
    local tmpData = {}
    tmpData.userid = XH.playerData:getNumberID()
    tmpData.buttontype = "友情提示"
    if XH.gameManager then 
        XH.throwDataManager:throwData(XH.ThrowDataDefine.YUEJU_05, tmpData)
    else
        XH.throwDataManager:throwData(XH.ThrowDataDefine.MY_22, tmpData)
    end
end

function IMListView:onTouchEventAwardRecall(playerInfo, send, eventType)
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
    XH.lobby:getModule("Im"):onTouchShare(XH.lobby:getModule("Im").onAwardRecallCallBack)
    if XH.gameManager then
        XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.BOX_ROOM_FRIEND_LIST_AWARD_RECALL)
    else
        XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_FRIEND_LIST_AWARD_RECALL)
    end
    local tmpData = {}
    tmpData.userid = XH.playerData:getNumberID()
    tmpData.buttontype = "有奖召回"
    if XH.gameManager then 
        XH.throwDataManager:throwData(XH.ThrowDataDefine.YUEJU_05, tmpData)
    else
        XH.throwDataManager:throwData(XH.ThrowDataDefine.MY_22, tmpData)
    end
end

function IMListView:onTouchEventReadyHead(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:showListLayer()
end

function IMListView:onTouchEventSet(playerInfo, send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.viewManager:openView("IMTipsLayerView", nil, {ctype = "ShieldTips", name = playerInfo.nickname, 
    num = playerInfo.numid, url = playerInfo.headurl, flag = playerInfo.shieldState})
end

--------------------------------------

function IMListView:setReadyInfo(node, data, index)
    XH.UITool.setText(node, "_KW_PLAYER_NAME", XH.StringTool.cutStringByLength(clone(data.nickname), 10))
    -- 设置头像
    local headNode = XH.UIRemoteImage.create(XH.UITool.seekNodeByName(node, "_KW_IMG_HEAD"))
    if headNode then
        headNode:setDefaultTexture(KW_IMG_DEFULT_HEAD, ccui.TextureResType.plistType)
        headNode:setUrl(data.headurl)
    end
    -- 设置头像框
    local frameParent = headNode
    if headNode:getChildrenCount() > 0 then
        frameParent = headNode:getChildren()[1]
    end
    frameParent:removeAllChildren()
    if frameParent and data.timeprop and next(data.timeprop) then
        for _, v in pairs(data.timeprop) do
            if XH.propsData:getExtendPropTagIdByPropid(v) == "head_frame" then
                local url = XH.propsData:getExtendPropImgUrlByPropid(v) or ""
                self:setFrameUrl(frameParent, node, url)
                break
            end
        end
    end
    -- 设置状态
    if self.KW_PLAYER_STATE_PIC[data.player_state] then
        XH.UITool.loadTexture(node, "_KW_UI_STATE", self.KW_PLAYER_STATE_PIC[data.player_state], ccui.TextureResType.plistType)
        XH.UITool.setVisible(node, "_KW_UI_STATE", true)
    else
        XH.UITool.setVisible(node, "_KW_UI_STATE", false)
    end
end

function IMListView:setFriendInfo(node, data, index)
     -- test
--    XH.UITool.setText(node, "_KW_TEST_NID", data.numid)
--    XH.UITool.setText(node, "_KW_TEST_IDX", index + 1)
    -- 设置昵称
    XH.UITool.setText(node, "_KW_PLAYER_NAME", XH.StringTool.cutStringByLength(clone(data.nickname), 10))
    -- 设置头像
    local headNode = XH.UIRemoteImage.create(XH.UITool.seekNodeByName(node, "_KW_IMG_HEAD"))
    if headNode then
        headNode:setSwallowTouches(false)
        headNode:setDefaultTexture(KW_IMG_DEFULT_HEAD, ccui.TextureResType.plistType)
        headNode:setUrl(data.headurl)
        headNode:addTouchEventListener(handler(data, handler(self, self.onTouchEventSet)))
    end
    -- 设置头像框
    local frameParent = headNode
    if headNode:getChildrenCount() > 0 then
        frameParent = headNode:getChildren()[1]
    end
    frameParent:removeAllChildren()
    if frameParent and data.timeprop and next(data.timeprop) then
        for _, v in pairs(data.timeprop) do
            if XH.propsData:getExtendPropTagIdByPropid(v) == "head_frame" then
                local url = XH.propsData:getExtendPropImgUrlByPropid(v) or ""
                self:setFrameUrl(frameParent, node, url)
                break
            end
        end
    end
    -- 设置状态
    if self.KW_PLAYER_STATE_PIC[data.player_state] then
        XH.UITool.loadTexture(node, "_KW_UI_STATE", self.KW_PLAYER_STATE_PIC[data.player_state], ccui.TextureResType.plistType)
        XH.UITool.setVisible(node, "_KW_UI_STATE", true)
    else
        XH.UITool.setVisible(node, "_KW_UI_STATE", false)
    end
    if data.player_state == XH.IMProtocol.PLAYERSTATE.PS_OFFLINE then
        XH.UITool.setText(node, "_KW_ONLINE_TIME", self:getTimeLen(data.last_login_time))
        XH.UITool.setVisible(node, "_KW_ONLINE_TIME", true)
    else
        XH.UITool.setVisible(node, "_KW_ONLINE_TIME", false)
    end
    -- 设置比赛场在线标记
    if data.bInTea then
        if XH.areaData:isSupportMatch() then
            XH.UITool.loadTexture(node, "_KW_TAG_IN_TEA", "friend_tag_tea.png", ccui.TextureResType.plistType)
        end
        XH.UITool.setVisible(node, "_KW_TAG_IN_TEA", true)
    else
        XH.UITool.setVisible(node, "_KW_TAG_IN_TEA", false)
    end
    -- 设置按钮状态
    if self.KW_ACTION_PIC[data.player_state] and not self:getData():getIsSeer() then
        XH.UITool.loadTexture(node, "_KW_UI_INVITE", self.KW_ACTION_PIC[data.player_state], ccui.TextureResType.plistType)
        XH.UITool.addTouchEventListener(node, "_KW_UI_INVITE", handler(data, handler(self, self.on_KW_UI_INVITE)))
        XH.UITool.setVisible(node, "_KW_UI_INVITE", true)
        self:setActionEx(node, data)
    else
        XH.UITool.setVisible(node, "_KW_UI_INVITE", false)
    end
    if data.player_state == XH.IMProtocol.PLAYERSTATE.PS_GAME then
        XH.UITool.setVisible(node, "_KW_UI_WATCH", true)
    else
        XH.UITool.setVisible(node, "_KW_UI_WATCH", false)
    end
    if data.shieldState then
        if data.shieldState == XH.IMProtocol.SHIELDTYPE.SHIELD then
            XH.UITool.setVisible(node, "_KW_IMG_SHIELD", true)
        else
            XH.UITool.setVisible(node, "_KW_IMG_SHIELD", false)
        end
    end
    -- 会员权益里有观战功能，会员显示观战，非会员显示会员观战
    -- 会员权益里没有观战，不显示观战
    if XH.lobby:getModule("Sxvip"):getPowerExit(SxVipConfig.SXVIP_POWER.WATCH, data.gameid) and XH.lobby:getModule("Im"):JudgeCanWatch(data.roomid) then
        if not XH.lobby:getModule("Sxvip"):isExpire() then
            XH.UITool.loadTexture(node, "_KW_UI_WATCH", "friend_action_watch.png", ccui.TextureResType.plistType)
            XH.UITool.addTouchEventListener(node, "_KW_UI_WATCH", handler(data, handler(self, self.on_KW_UI_WATCH)))
        else
            XH.UITool.loadTexture(node, "_KW_UI_WATCH", "friend_action_watch_vip.png", ccui.TextureResType.plistType)
            XH.UITool.addTouchEventListener(node, "_KW_UI_WATCH", handler(data, handler(self, self.on_KW_UI_WATCH_VIP)))
        end
    else
        XH.UITool.setVisible(node, "_KW_UI_WATCH", false)
    end
end

function IMListView:setTableInfo(node, data, index)
    if data.tableType then
        if data.tableType == self:getData().TABLEDATATYPE.TEAOPEN or data.tableType == self:getData().TABLEDATATYPE.NORMALOPEN then
            self:setOpenTableInfo(node, data, index)
        elseif data.tableType == self:getData().TABLEDATATYPE.TEANOTOPEN or data.tableType == self:getData().TABLEDATATYPE.NORMAL then
            self:setNotOpenTableInfo(node, data, index)
        end
    end
end

function IMListView:setBaseInfo(node, data, index)
    XH.UITool.addTouchEventListener(node, "_KW_BTN_SHOW_MORE", handler(index, handler(self, self.onTouchShowMoreTable)))
    if data.passwd then
        local qyqNum = string.format("%06d", data.passwd or 0)
        XH.UITool.setText(node, "_KW_TXT_QYQ", XH.UITool.getText(node, "_KW_TXT_QYQ"))
        XH.UITool.setText(node, "_KW_TXT_QYQ_NUM", qyqNum)
        local headNode = XH.UIRemoteImage.create(XH.UITool.seekNodeByName(node, "_KW_PANEL_TEA_HEAD"))
        if headNode then
            headNode:setDefaultTexture(KW_IMG_DEFULT_HEAD, ccui.TextureResType.plistType)
            headNode:setUrl(data.url)
        end
    end
end

function IMListView:setOpenTableInfo(node, data, index)
    self:setBaseInfo(node, data, index)
    if data.tables then
        local listView = XH.UITool.seekNodeByName(node, "_KW_LIST_VIEW_MORE")
        if listView and next(data.tables) ~= nil then
            listView:setSwallowTouches(false)
            listView:removeAllItems()
            for k, v in pairs(data.tables) do
                local tmpItem = self._tableItem:clone()
                self:setNormalTableInfo(tmpItem, v, k, data.tableType)
                tmpItem:setSwallowTouches(false)
                listView:pushBackCustomItem(tmpItem)
            end
            listView:setContentSize(listView:getContentSize().width, #data.tables * (self._tableItem:getContentSize().height + KW_WIDGET_HEIGHT))
            local size = listView:getContentSize()
            node:setPositionY(size.height)
        end
    end
end

function IMListView:setNotOpenTableInfo(node, data, index)
    self:setBaseInfo(node, data, index)
end

function IMListView:setNormalTableInfo(node, data, index, tableType)
    dump(data)
    local gameNames = XH.areaData:getAreaGameNameList()
    if gameNames[data.gameid] then
        XH.UITool.setText(node, "_KW_UI_GAME_NAME", gameNames[data.gameid])
    end
    if data.maxcount and data.maxcount > 0 then
        XH.UITool.setText(node, "_KW_UI_PLAY_COUNT", data.maxcount .. "局")
    else
        XH.UITool.setVisible(node, "_KW_UI_PLAY_COUNT", false)
    end
    if tableType and tableType == self:getData().TABLEDATATYPE.NORMALOPEN then
        if tonumber(data.paytype) == 0 then
            XH.UITool.setText(node, "_KW_UI_PAY_TYPE", "房主支付")
        elseif tonumber(data.paytype) == 1 then
            XH.UITool.setText(node, "_KW_UI_PAY_TYPE", "平摊支付")
        end
    else
        XH.UITool.setText(node, "_KW_UI_PAY_TYPE", "")
    end
    for i = 1, MAX_PLAYER_COUNT do
        local headNode = node:getChildByName("_KW_HEAD_" .. i)
        if headNode then
            headNode:setSwallowTouches(false)
            local headPanel = headNode:getChildByName("_KW_HEAD_PANEL")
            if headPanel then
                if i <= data.count then
                    XH.lobby:getModule("Im"):updateHeadImg(headPanel, data.players[i].url)
                    headNode:setEnabled(false)
                elseif i <= data.chairs then
                    headPanel:removeAllChildren()
                    -- if tableType and tableType == self:getData().TABLEDATATYPE.TEAOPEN then
                    --     headNode:addTouchEventListener(handler(data, handler(self, self.on_BTND_NORMAL_JOIN)))
                    -- else
                        headNode:addTouchEventListener(handler(data, handler(self, self.on_BTND_WILL_JOIN)))
                    -- end
                    headNode:setVisible(true)
                    headNode:setEnabled(true)

                    -- if index == 0 and i == data.count + 1 then
                    --     self:showJoinTableTip(true, headNode:getWorldPosition().x)
                    -- end
                else
                    headNode:setVisible(false)
                end
            end
        end
    end
end

function IMListView:limitTouch(node, time)
    node:setEnabled(false)
    node:setBright(false)
    node:setColor(cc.c3b(160,160,160))
    self:performWithDelay(function()
        node:setEnabled(true)
        node:setBright(true)
        node:setColor(cc.c3b(255,255,255))
    end, time)
end

function IMListView:updateLableUI(bEnabled, isRecall)
    if isRecall then
        self._lableList:loadTexture("friend_lable_list_1.png", ccui.TextureResType.plistType)
        self._lableFollow:loadTexture("friend_lable_follow_1.png", ccui.TextureResType.plistType)
        self._lableRecall:loadTexture("friend_lable_recall_2.png", ccui.TextureResType.plistType)
    else
        self._lableList:setEnabled(not bEnabled)
        if bEnabled then
            self._lableList:loadTexture("friend_lable_list_2.png", ccui.TextureResType.plistType)
            self._lableFollow:loadTexture("friend_lable_follow_1.png", ccui.TextureResType.plistType)
        else
            self._lableList:loadTexture("friend_lable_list_1.png", ccui.TextureResType.plistType)
            self._lableFollow:loadTexture("friend_lable_follow_2.png", ccui.TextureResType.plistType)
        end
        self._lableRecall:loadTexture("friend_lable_recall_1.png", ccui.TextureResType.plistType)
        self._tableListView:setVisible(not bEnabled)
        self._friendListView:setVisible(bEnabled)
    end
    self._lableList:ignoreContentAdaptWithSize(true)
    self._lableFollow:ignoreContentAdaptWithSize(true)
    self._lableRecall:ignoreContentAdaptWithSize(true)
    self._lableFollow:setEnabled(isRecall or bEnabled)
    self._lableList:setEnabled(isRecall or not bEnabled)
    self._lableRecall:setEnabled(not isRecall)

    self:updateRecallVisible()
end

function IMListView:isOpenRecall()
    -- local lobbyID = XH.areaData:getLobbyID()
    -- if lobbyID == XH.LOBBY_ID.HANGMAQUAN then
    --     return true
    -- end
    -- return false
    return true
end

function IMListView:updateRecallVisible()
    if self._lableRecall then
        self._lableRecall:setVisible(self:isOpenRecall())
    end
end

function IMListView:clearListCache()
    if self:getData():getLable() == self:getData().KW_LIST_TYPE.PLAYERLIST then
        XH.lobby:getModule("Im"):setCurFriendListPage(0)
        XH.lobby:getModule("Im"):setTotalFriendListPage(0)
        self:getData():clearFriendList()
    elseif self:getData():getLable() == self:getData().KW_LIST_TYPE.TABLELIST then
        XH.lobby:getModule("Im"):setCurTableListPage(0)
        XH.lobby:getModule("Im"):setTotalTableListPage(0)
        self:getData():clearTableList()
    end
end

function IMListView:getShowData()
    if self._bReadyState or self:getData():getLable() <= self:getData().KW_LIST_TYPE.PLAYERLIST then
        return self:getData():getFriendList()
    elseif self:getData():getLable() == self:getData().KW_LIST_TYPE.TABLELIST then
        return self:getData():getTableList()
    end
    return {}
end

function IMListView:getCurDataCount()
    if self:getData():getLable() == self:getData().KW_LIST_TYPE.PLAYERLIST then
        return self._curFriendListDataCount
    elseif self:getData():getLable() == self:getData().KW_LIST_TYPE.TABLELIST then
        return self._curTableListDataCount
    end
    return 0
end

function IMListView:showJoinTableTip(bShow, posX)
    self._tableTipQiPao:stopAllActions()
    if not bShow then
        self._tableTipQiPao:setVisible(false)
        return
    end
    self._tableTipQiPao:setPositionX(posX)
    self._tableTipQiPao:setVisible(true)
    local opacity = self._tableTipQiPao:getOpacity()  
    if opacity == 255 then
        self._tableTipQiPao:setOpacity(0)
    else
        self._tableTipQiPao:runAction(cc.Sequence:create(cc.FadeIn:create(0.2), cc.DelayTime:create(5), cc.FadeOut:create(0.2)))
    end
end

-- 原用于浙江包玩家拉单包玩家，现在渠道包很多，不需要这个判断了
function IMListView:judgeDefultChannelId(channelId)
    return true
    -- if not channelId then
    --     return false
    -- end
    -- for _, v in pairs(self.KW_DEFULT_CHANNE_ID) do
    --     if channelId == v then
    --         return true
    --     end
    -- end
    -- return false
end

function IMListView:setActionEx(node, data)
    if not data.player_state or ( data.player_state ~= XH.IMProtocol.PLAYERSTATE.PS_OFFLINE and data.player_state ~= XH.IMProtocol.PLAYERSTATE.PS_ONLINE) then
        return
    end
    local subTime = 0
    if data.last_login_time then
        subTime = os.time() - data.last_login_time
    end
    if data.player_state == XH.IMProtocol.PLAYERSTATE.PS_ONLINE and not self:judgeDefultChannelId(data.channelid) then
        XH.UITool.loadTexture(node, "_KW_UI_INVITE", KW_IMG_FRIENDLY_TIP, ccui.TextureResType.plistType)
        XH.UITool.addTouchEventListener(node, "_KW_UI_INVITE", handler(data, handler(self, self.onTouchEventFriendlyTips)))
    elseif data.player_state == XH.IMProtocol.PLAYERSTATE.PS_OFFLINE and subTime > 7 * KW_ONE_DAY --[[and not self:getData():getRecallAwardState()]] then
        XH.UITool.loadTexture(node, "_KW_UI_INVITE", KW_IMG_AWARD_BACK, ccui.TextureResType.plistType)
        XH.UITool.addTouchEventListener(node, "_KW_UI_INVITE", handler(data, handler(self, self.onTouchEventAwardRecall)))
        if self:isOpenRecall() then
            XH.UITool.setVisible(node, "_KW_UI_INVITE", false)
        end
    end
end

function IMListView:getTableViewData(view)
    if view:getName() == "FRIENDLISTVIEW" or view:getName() == "READYLISTVIEW" then
        return self:getData():getFriendList()
    else
        return self:getData():getTableList()
    end
end

function IMListView:getTableViewItemModel(view)
    if view:getName() == "FRIENDLISTVIEW" then
        return self._playerItem
    elseif view:getName() == "READYLISTVIEW" then
        return self._readyItem
    else
        return self._tableItem
    end
end

function IMListView:reqTableViewInfo(view)
    if view:getName() == "FRIENDLISTVIEW" or view:getName() == "READYLISTVIEW" then
        XH.lobby:getModule("Im"):reqFriendListInfo()
    else
        XH.lobby:getModule("Im"):reqMixTableListInfo()
    end
end

function IMListView:updateRecallAwardState(event)
    if not event or not event.msg then
        return
    end
    local jsonData = {}
    local ok, msg = pcall(function()
        jsonData = cjson.decode(event.msg)
    end)

    if not ok then
        print("cjson failed: " .. msg)
        return
    end
    if jsonData.code then
        local tmpState = jsonData.code == 1 and true or false
        if self.getData then
            self:getData():setRecallAwardState(tmpState)
        end
    end
end

function IMListView:openListLayer(event)
    if self._bMoveIn then
        return
    end
    self:showListLayer()
end

function IMListView:setFrameUrl(node, item, url)
    local propFrame = nil
    if node then
        local size = node:getContentSize()
        propFrame = XH.RemoteImage.new()
            :setPosition(size.width/2, size.height/2)
            :ignoreContentAdaptWithSize(false)
            :setContentSize(KW_FRAME_DEFULT_SIZE)
            :addTo(node)
        if item then
            XH.UITool.setVisible(item, "_KW_IMG_FRAME", false)
        end
    end
    if propFrame then
        propFrame:setUrl(url)
    end
end

function IMListView:getTableDataTypeByIndex(idx)
    local tmpType = 0
    local tableListData = self:getData():getTableList()
    if idx and next(tableListData) ~= nil then
        if tableListData[idx] and tableListData[idx].tableType then
            return tableListData[idx].tableType
        end
    end
    return tmpType
end

function IMListView:setTableViewItem(parent, item)
    local childItem = item:clone()
    if not childItem then return parent end
    childItem:setTouchEnabled(false)
    childItem:setAnchorPoint(cc.p(0, 0))
    childItem:setPosition(0, 0)
    childItem:setName(childItem:getName())
    childItem:setVisible(false)
    parent:addChild(childItem)
end

function IMListView:onTouchShowMoreTable(index, send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local tableType = self:getData():getTableType(index)
    if tableType and tableType == self:getData().TABLEDATATYPE.TEANOTOPEN then
        local teaId = self:getData():getTableTeaId(index)
        if teaId then
            XH.lobby:getModule("Im"):reqTeaDetailTable(teaId)
        end
    end
    self:getData():updateTableType(index)
end

function IMListView:updateSeerRedPoint()
    if not self:getData():getIsSeer() then
        return
    end
    local friendListWaiting = self:getData():getFriendListWaiting()
    self._imgRedPoint:setVisible(#friendListWaiting > 0)
end

function IMListView:onUpdateStatus()
    if not self:getData():getIsSeer() then
        return
    end
    local friendListWaiting = self:getData():getFriendListWaiting()
    local isShow = false
    for k,v in pairs(friendListWaiting) do 
        if v.chair_count and v.user_count and v.chair_count > 0 and v.user_count > 0 and v.chair_count - v.user_count == 1 then
            isShow = true
            break
        end
    end
    if isShow then
        if not self or tolua.isnull(self) or tolua.isnull(self._toastTips) then
            return
        end
        local function hideTips()
            if self and not tolua.isnull(self) and not tolua.isnull(self._toastTips) then
                self._toastTips:setVisible(false)
            end
        end
        self._toastTips:setVisible(true)
        local delayAction = cc.Sequence:create(cc.DelayTime:create(5), cc.CallFunc:create(hideTips))
        self._toastTips:runAction(delayAction)
    end
end

function IMListView:updateSeerUI()
    if not self:getData():getIsSeer() then
        return
    end
    self._btnAction:setVisible(false)
    if self._inviteAll then
        self._inviteAll:setVisible(false)
    end
    -- 旁观玩家定时处理好友状态
    if not self._updateFriendStatusScheduler then
        self._updateFriendStatusScheduler = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
            if self and not tolua.isnull(self) then
                self:onUpdateStatus()
            end
        end, 20, false)
    end
end

return IMListView
   9�  