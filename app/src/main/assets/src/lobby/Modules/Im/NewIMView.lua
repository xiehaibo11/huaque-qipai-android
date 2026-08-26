local ViewBase = require("lobby.Modules.Im.View")
local NewIMListView = class("NewIMListView", ViewBase)
local SxVipConfig = require('lobby.Modules.Sxvip.Config')

local MAX_PLAYER_COUNT = 4
local KW_ONE_DAY = 24*3600  -- 一天时长
local TABLEVIEW_CELL_INTERVAL = 10  -- tableView子节点间隔
-- local KW_WIDGET_HEIGHT = 5 -- 列表容器子控件间距

local KW_IMG_AWARD_BACK = "friend_award_back.png"
local KW_IMG_FRIENDLY_TIP = "friend_action_friendly_tips.png"
local KW_IMG_DEFULT_HEAD = "_0002_mrtx_w.png"

NewIMListView.KW_PLAYER_STATE_PIC = {
    [1] = "friend_state_off_line.png",
    [2] = "friend_state_gaming.png",
    [4] = "friend_state_on_line.png",
    [8] = "friend_state_wait.png",
}

NewIMListView.KW_ACTION_PIC = {
    [1] = "friend_action_wechat_invite.png",
    [2] = "friend_action_order.png",
    [4] = "friend_action_online_invite.png",
    [8] = "friend_action_online_invite.png",
    [99] = "imteahouse_paiyou_apply_btn.png"
}

NewIMListView.TABLEVIEWTYPE = {
    FRIENDLISTVIEW = {
        parent = "_friendListViewV2",
        item = "_playerItemV2",
    },
    TABELLISTVIEW = {
        parent = "_tableListViewV2",
        item = "_tableItem"
    },
    READYLISTVIEW = {
        parent = "_readyListView",
        item = "_readyItem"
    },
    GROUPLISTVIEW = {
        parent = "_groupListView",
        item = "_groupItem"
    },
}

NewIMListView.KW_FRIEND_TYPE = {
    FRIEND = 0, -- 我的牌友
    GROUP = 1   -- 我的分组
}

NewIMListView.GROUP_TYPE = {
    ALL = 0, -- 我的牌友
    GROUP = 1,   -- 我的分组
    TEAHOSE =2,
    BOX = 3
}

function NewIMListView:getCSBPath()
    return "cocosStudio/hall/CSB/IM/IMListLayer.csb"
end

function NewIMListView:getBindingInfo()
    return {
        ["_KW_PANEL_ADAPT"] = { varName = "_panelAdapt", onTouchEnded = "on_KWA_BG_PANEL" },
        ["_KW_UI_OPEN_PIC"] = { varName = "_openBtnPic", onTouchEnded = "on_KW_BTND_OPEN" },
        ["_KWA_FRIEND_PANEL"] = { varName = "_friendPanel", onTouchEnded = "on_KWA_FRIEND_PANEL" },
        ["_KWA_LIST_PANEL"] = { varName = "_listPanel" },
        ["_KW_LABLE_LIST"] = { varName = "_lableList", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KW_LABLE_LIST" },
        ["_KW_LABLE_FOLLOW"] = { varName = "_lableFollow", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KW_LABLE_FOLLOW" },
        ["_KW_LABLE_RECALL"] = { varName = "_lableRecall" },
        ["_KW_FRIEND_LIST_VIEW"] = { varName = "_friendListView" },
        ["_KW_FRIEND_LIST_VIEW_NEW"] = { varName = "_friendListViewV2" },
        ["_KW_GROUP_LIST_VIEW"] = { varName = "_groupListView" },
        ["_KW_TABLE_LIST_VIEW"] = { varName = "_tableListView" },
        ["_KW_TABLE_LIST_VIEW_NEW"] = { varName = "_tableListViewV2" },
        ["_KW_PLAYER_ITEM"] = { varName = "_playerItem" },
        ["_KW_PLAYER_ITEM_NEW"] = { varName = "_playerItemV2" },
        ["_KW_GROUP_ITEM_NEW"] = {varName = "_groupItem"},
        ["_KW_TABLE_ITEM_NEW"] = { varName = "_tableItem" },
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
        ["_KW_FRIENDS_UI_NEW"] = {varName = "_friendsUIV2"},
        ["_KW_MY_FRIENDS"] = { varName = "_changeMyList"},
        ["_KW_BTN_FRIENDS"] = { varName = "_btnFriends", type = XH.UI_TYPE.IMAGE_TOBUTTON, style = XH.UIButton.CLICK_STYLE.NOEFFECT, onTouchEnded = "doClickMyFriends" },
        ["_KW_BTN_GROUPS"] = { varName = "_btnGroups", type = XH.UI_TYPE.IMAGE_TOBUTTON, style = XH.UIButton.CLICK_STYLE.NOEFFECT, onTouchEnded = "doClickMyGroups" },
        ["_KW_UI_ACTION_INVITE_ALL"] = {varName = "_inviteAll" , onTouchEnded = "on_KW_BTND_INVITE_ALL"},
        ["_KW_UI_ACTION_ADD_FRIEND"] = {varName = "_addFriend", onTouchEnded = "on_KW_BTND_ADD_FRIEND"},
        ["_KW_FRIEND_LIST_BTN_NEW"] = {varName = "_friendListBtnNew"},
        ["_KW_GROUP_LIST_BTN"]= {varName = "_groupListBtn"},
        ["_KW_UI_ACTION_ADD_GROUP"] = {varName = "_addGropBtn", onTouchEnded = "on_KW_UI_ACTION_ADD_GROUP"},
        ["_KW_FREIND_GUIDE_ANIM"] = {varName = "_addFriendGuideAnim"},
        ["_KW_TABLE_FILTER_NODE"] = {varName = "_tableFilterNode"},
        ["_KW_FILTER_BTN"] = {varName = "_tableFilterBtn",type = XH.UI_TYPE.IMAGE_TOBUTTON,onTouchEnded = "on_KW_UI_TABLE_FILTER"},
        ["_KW_FILTER_LIST"] = {varName = "_groupFilterView"},
        ["_KW_FLITER_BACK"] = {varName = "_groupFilterBack",type = XH.UI_TYPE.IMAGE_TOBUTTON,onTouchEnded = "on_KW_UI_TABLE_FILTER_BACK"},
        ["_KW_GRPUP_FILTER_ITEM"] = {varName = "_groupFilterItem"},
        ["_KW_USER_CNT"] = {varName = "_groupUserCnt"},
        ["_KW_UI_CREATE_TABLE"] = {varName = "_createTable", onTouchEnded = "onTouchEventCreateTable"},
        ["_KW_TABLE_LIST_BTN_NEW"] = {varName = "_tableListBtnNew"},
        ["_KW_SEARCH_BTN"] = {varName = "_friendListSearch",type = XH.UI_TYPE.IMAGE_TOBUTTON,onTouchEnded = "on_KW_SEARCH_PLAYER"},
        ["_KW_SEARCH_CANCEL"] = {varName = "_friendSearchCancel",type = XH.UI_TYPE.IMAGE_TOBUTTON,onTouchEnded = "on_KW_SEARCH_CANCEL"},
        ["_KW_SEARCH_TEXT"] = {varName = "_searchTextInput"},
        ["_KW_GROUP_GUIDE_ANIM"] = {varName = "_groupGuideAnim"},
        ["_KW_CREATE_GUIDE_ANIM"] = {varName = "_createRoomAnim"},
        ["_KW_CREATE_POP"] = {varName = "_createPop"},
        ["_KW_FILTER_POP"] = {varName = "_filterPop"},
        ["_KW_UI_BOX_REFRESH"] = {varName = "_refreshTable1", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventRefreshTableList"},
        ["_KW_UI_GROUP_REFRESH"] = {varName = "_refreshTable2", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventRefreshTableList"},
        ["_KW_BOX_CREATE"] = {varName = "_tableViewBoxCreateBtn"},
        ["_KW_GROUP_CREATE"] = {varName = "_tableViewGroupCreateBtn"},
        ["_KW_SELECT_NAME"] = {varName = "_selectedGroupName"},
        ["_KW_SEARCH_NODE"] = {varName = "_searchNode"},

        -- 旁观玩家UI
        ["_KW_IMG_RED_POINT"] = { varName = "_imgRedPoint" },
        ["_KW_IMG_QUE1REN"] = { varName = "_toastTips" },
    }
end

function NewIMListView:getProxyEvents()
    local proxyEvents = NewIMListView.super.getProxyEvents(self)
    table.insert(proxyEvents, { module = XH.lobby:getModule("TeaHouseList"), eventKeyName = "EVENT_FREIND_GROUP_LIST_CHANGED", callBack = "onGroupListChanged" })
    table.insert(proxyEvents, { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_UPDATE_ADD_FRIEND_STATE", callBack = "updeteSearchPlayerInfo" })
    table.insert(proxyEvents, { module = XH.lobby:getModule("JoinBoxRoom"),eventKeyName = "EVENT_HIDE_JOINBOXROOM", callBack = "updateTableInfo" })
    return proxyEvents
end

function NewIMListView:ctor() 
    NewIMListView.super.ctor(self)
end

function NewIMListView:initData()
    self._curFriendsType = 0
    self._selectedGroupId = 0
    self._selectedTeaId = 0
    self._selectedGroupType = 0
    self._isSearching = false
    self._searchPlayer = {}
    NewIMListView.super.initData(self)
    XH.lobby:getModule("Im"):reqApplyNum(0)
    XH.lobby:getModule("TeaHouseList"):reqTeaHouseList()
end

function NewIMListView:initUI()
    self:adaptForLiuHai()
    self._panelAdapt:setTouchEnabled(false)
    for k, v in pairs(self.TABLEVIEWTYPE) do
        if self[v.parent] then
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
    self._searchTextInput:setInputMode(cc.EDITBOX_INPUT_MODE_PHONENUMBER)
end

function NewIMListView:updateActionBtn()
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
    self._btnAction:setVisible(false)
    if  self:getData():getLable() ~= self:getData().KW_LIST_TYPE.TABLELIST then
        self._friendListBtnNew:setVisible(self._curFriendsType == self.KW_FRIEND_TYPE.FRIEND)
        self._groupListBtn:setVisible(self._curFriendsType == self.KW_FRIEND_TYPE.GROUP)
    end
end

function NewIMListView:initTableView(parentNode, viewName)
    parentNode:removeAllChildren()
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

function NewIMListView:onTableViewCellSizeForTable(view, index)
    local item = self[self.TABLEVIEWTYPE[view:getName()].item]
    return item:getContentSize().width, item:getContentSize().height + TABLEVIEW_CELL_INTERVAL
end

function NewIMListView:onTableViewCellAtIndex(view, index)
    local cell = view:dequeueCell()
    if not cell then
        cell = cc.TableViewCell:new()
        self:setTableViewItem(cell, self[self.TABLEVIEWTYPE[view:getName()].item])
    end

    local item = cell:getChildByName(self[self.TABLEVIEWTYPE[view:getName()].item]:getName())
    item:setVisible(true)
    local dataList = self:getTableViewData(view)
    local data = dataList[index + 1]
    if data then
        if view:getName() == "TABELLISTVIEW" then
            self:setTableInfo(item, data, index)
        elseif view:getName() == "FRIENDLISTVIEW" then
            self:setFriendInfo(item, data, index)
        elseif view:getName() == "GROUPLISTVIEW" then
            self:setGroupInfo(item, data, index)
        elseif view:getName() == "READYLISTVIEW" then
            self:setReadyInfo(item, data, index)
        end
    end

    return cell
end

function NewIMListView:scrollTableViewDidScroll(view)
    local listSize = view:getContentSize()    -- 总大小
    if listSize.height == 0 then return end     -- 分母不能为0
    local offset = view:getContentOffset()    -- 取滑动偏移
    if view:getVerticalFillOrder() == cc.TABLEVIEW_FILL_TOPDOWN then
        offset.y = offset.y + listSize.height   -- 从起点开始偏移量
    end
    local viewData = self:getTableViewData(view)
    local endIdx
    local item = self:getTableViewItemModel(view)
    endIdx = offset.y / item:getContentSize().height + 1
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

function NewIMListView:numberOfCellsInTableView(view)
    return #self:getTableViewData(view)
end

function NewIMListView:onRefreshList(event)
    if not self._readyListView or not self._friendListViewV2 or not self._groupListView or not self._tableListViewV2 then
        return
    end
    cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/hall/Image/friends.plist")
    local tableView = self._readyListView:getChildByName("READYLISTVIEW")
    if not self._bReadyState or XH.gameManager then
        if self._curFriendsType == self.KW_FRIEND_TYPE.GROUP then 
            tableView = self._groupListView:getChildByName("GROUPLISTVIEW")
        else
            tableView = self._friendListViewV2:getChildByName("FRIENDLISTVIEW")
        end
    end
    if not self._bReadyState and self:getData():getLable() == self:getData().KW_LIST_TYPE.TABLELIST then
        tableView = self._tableListViewV2:getChildByName("TABELLISTVIEW")
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

function NewIMListView:updateTips(cnt)
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
        -- 牌友列表的状态下减去自己
        if self._curFriendsType == self.KW_FRIEND_TYPE.FRIEND then 
            cnt = cnt -1
            self._noFriendTip:setString("您的牌友列表当前为空\n快去添加牌友开始游戏吧")
            if cnt < 4 then
                local isFinishGuide = cc.UserDefault:getInstance():getBoolForKey("IMListLayer_First_Add_Guide", false) 
                self._addFriendGuideAnim:setVisible(not isFinishGuide)    
            end
        elseif self._curFriendsType == self.KW_FRIEND_TYPE.GROUP then
            self._noFriendTip:setString("限免开放分组功能,快去\n添加牌友组合一起游戏")
            if cnt == 0 then
                local isFinishGuide = cc.UserDefault:getInstance():getBoolForKey("IMListLayer_First_Group_Guide", false) 
                self._groupGuideAnim:setVisible(not isFinishGuide)  
            end
        end
        if cnt == 0 then
            self._noFriendTip:setVisible(true)
        end
    elseif self:getData():getLable() == self:getData().KW_LIST_TYPE.TABLELIST then
        self._qpSearch:stopAllActions()
        self._qpSearch:setOpacity(0)
        if cnt == 0 then
            self._noTableTip:setVisible(true)
        end
        local isFinishGuide = cc.UserDefault:getInstance():getBoolForKey("IMListLayer_First_Table_Guide", false) 
        self._createRoomAnim:setVisible(not isFinishGuide)  
    end
end

function NewIMListView:doClickMyFriends(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._searchNode:setVisible(true)
    self._curFriendsType = self.KW_FRIEND_TYPE.FRIEND
    self._friendListBtnNew:setVisible(true)
    self._friendListViewV2:setVisible(true)
    self._groupListBtn:setVisible(false)
    self._groupListView:setVisible(false)
    self._changeMyList:loadTexture("imteahouse_left_page_" .. (self._curFriendsType + 1) .. ".png", ccui.TextureResType.plistType) 
    self:onRefreshList()
end


function NewIMListView:doClickMyGroups(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._searchNode:setVisible(false)
    self._curFriendsType = self.KW_FRIEND_TYPE.GROUP
    self._noFriendTip:setVisible(false)
    self._friendListBtnNew:setVisible(false)
    self._friendListViewV2:setVisible(false)
    self._groupListBtn:setVisible(true)
    self._groupListView:setVisible(true)
    self._changeMyList:loadTexture("imteahouse_left_page_" .. (self._curFriendsType + 1) .. ".png", ccui.TextureResType.plistType) 
    XH.lobby:getModule("TeaHouseList"):reqTeaHouseList()
end

function NewIMListView:on_KW_LABLE_LIST(send, eventType)
    self:updateLableUI(true)
    self:getData():setLable(self:getData().KW_LIST_TYPE.PLAYERLIST)
    self:updateActionBtn()
    self._friendsUIV2:setVisible(true)
    self._friendListView:setVisible(false)
    self._tableListViewV2:setVisible(false)
    self._tableFilterNode:setVisible(false)
    self._tableListView:setVisible(false)
    self._tableListBtnNew:setVisible(false)
    self:showJoinTableTip(false) 
    if self._curFriendsType == self.KW_FRIEND_TYPE.GROUP then
        self:doClickMyGroups(self._btnGroups,ccui.TouchEventType.ended)
    else
        self:doClickMyFriends(self._btnFriends,ccui.TouchEventType.ended)
    end
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

function NewIMListView:on_KW_LABLE_FOLLOW(send, eventType)
    -- #  即将开始 第一次点击显示气泡
    if  not cc.UserDefault:getInstance():getBoolForKey("IMListLayer_First_Touch_Table2", false) then
        XH.UITool.setText(self._filterPop, "pop_txt", XH.UITool.getText(self._filterPop, "pop_txt"))
        self._filterPop:setVisible(true)
    else
        self._filterPop:setVisible(false)
    end
    XH.lobby:getModule("TeaHouseList"):reqTeaHouseList()
    self:updateLableUI(false)
    self:getData():setLable(self:getData().KW_LIST_TYPE.TABLELIST)
    self:updateActionBtn()
    self:onRefreshList()

    self.PlayerbuttonChanged = true
    self._friendListView:setVisible(false)
    self._friendListViewV2:setVisible(false)
    self._groupListView:setVisible(false)
    self._friendsUIV2:setVisible(false)
    self._friendListBtnNew:setVisible(false)
    self._tableListView:setVisible(false)
    self._tableListViewV2:setVisible(true)
    self._tableListBtnNew:setVisible(true)
    self._tableFilterNode:setVisible(true)
    self._groupFilterBack:setSwallowTouches(false)
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_23, {tabname = "即将开始" })

    -- 抛送最近曝光数据
    local sceneName = XH.lobby:getModule("Im"):judgeSceneName()
    local data = {
        block_label = sceneName
    }
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyxt24052713, data)
end

function NewIMListView:on_KW_APPLY_NUM(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.viewManager:openView("IMTeaHouseApplyFriendView")
end


function NewIMListView:on_KW_BTND_ADD_FRIEND(send, eventType)
    -- TODO 邀请好友
    if self._addFriendGuideAnim:isVisible() then
        cc.UserDefault:getInstance():setBoolForKey("IMListLayer_First_Add_Guide", true)
        self._addFriendGuideAnim:setVisible(false) 
    end 

    XH.viewManager:openView("IMAddPaiYouView")
end

function NewIMListView:on_KW_UI_ACTION_ADD_GROUP(send, eventType)
    --请求创建比赛场协议，传参level=100
    if self._groupGuideAnim:isVisible() then
        self._groupGuideAnim:setVisible(false)
        cc.UserDefault:getInstance():setBoolForKey("IMListLayer_First_Group_Guide", true) 
    end

    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_ADD_GROUP_CLICK)

    if XH.playerData:getPlayerPhone() == "" then
        XH.viewManager:openView("PhoneBindView")
        return
    end
    if  XH.playerData:getPlayerIsAgent() or XH.areaData:getVirtualAgentState() then
        XH.lobby:getModule("Im"):reqCreateGroup()
    else
        XH.lobby:getModule("Im"):ReqRegisterAgent(XH.areaData:getAreaID(),XH.playerData:getPlayerPhone(),XH.playerData:getWeChatURL(),XH.playerData:getNickName())        
    end
end


function NewIMListView:on_KW_UI_TABLE_FILTER(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    -- 发起请求
    self._filterPop:setVisible(false)
    cc.UserDefault:getInstance():setBoolForKey("IMListLayer_First_Touch_Table2", true)
    XH.lobby:getModule("TeaHouseList"):reqTeaHouseList()
    self._groupFilterView:setVisible(true)
    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_FILTER_BTN)
end

function NewIMListView:on_KW_UI_TABLE_FILTER_BACK()
    self._groupFilterView:setVisible(false)
end

function NewIMListView:on_KW_SEARCH_PLAYER(send, eventType)
    local text = self._searchTextInput:getText()
    if text ~= "" then
        XH.lobby:getModule("Im"):reqFriendState(text)
        self._isSearching = true
    end
end

function NewIMListView:on_KW_SEARCH_CANCEL()
    self._searchTextInput:setText("")
    self._isSearching = false
    self:onRefreshList()
end

function NewIMListView:on_KW_UI_CONTROLL(data, send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    data.scene = "MANAGEMENTLIST"
    XH.viewManager:openView("IMTeaHouseMemberView", nil, data)

    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_MANAGE_CLICK,{item_id = data.nTeaNumber})
end

function NewIMListView:on_KW_UI_LOOK(data, send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    data.scene = "MEMBERLIST"
    XH.viewManager:openView("IMTeaHouseMemberView",nil,data)
    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_LIST_LOOK_CLICK,{item_id = data.nTeaNumber})
end

function NewIMListView:on_KW_UI_RANK(data, send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.viewManager:openView("IMTeaHouseRankView",nil,data)
    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_RANK_CLICK,{item_id = data.nTeaNumber})
end

function NewIMListView:on_KW_BTND_SEARCH_FRIENDS(send, eventType)
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

function NewIMListView:onTouchEventReqApplyList(send, eventType)
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
    
end

function NewIMListView:onTouchEventSet(playerInfo, send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local info = {scene = "SHIELD", name = playerInfo.nickname, 
    num = playerInfo.numid, url = playerInfo.headurl, flag = playerInfo.shieldState}
    XH.viewManager:openView("IMTeaHouseTipsView", nil, {data = info })
end

function NewIMListView:onTouchEventGroupSet(playerInfo, send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if XH.playerData:getNumberID() == playerInfo.nTeaOwnerNumid then
        playerInfo.scene = "GROUPINFO"
        playerInfo.playerType = "OWNER"
    else
        playerInfo.scene = "GROUPINFO"
        playerInfo.playerType = "MEMBER"
    end
    XH.viewManager:openView("IMTeaHouseGroupTipsView", nil, {data = playerInfo })
    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_HEAD_CLICK,{item_id = playerInfo.nTeaNumber})
end

--------------------------------------

function NewIMListView:setFriendInfo(node, data, index)
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
        self:setActionEx(node, data)
        XH.UITool.setVisible(node, "_KW_UI_INVITE", true)
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
        local btnWatch = XH.UITool.seekNodeByName(node, "_KW_UI_WATCH")
        if not XH.lobby:getModule("Sxvip"):isExpire() then
            btnWatch:addTouchEventListener(handler(data, handler(self, self.on_KW_UI_WATCH)))
            XH.UITool.loadTexture(node, "_KW_UI_WATCH", "friend_action_watch.png", ccui.TextureResType.plistType)
        else
            btnWatch:addTouchEventListener(handler(data, handler(self, self.on_KW_UI_WATCH_VIP)))
            XH.UITool.loadTexture(node, "_KW_UI_WATCH", "friend_action_watch_vip.png", ccui.TextureResType.plistType)
        end
    else
        XH.UITool.setVisible(node, "_KW_UI_WATCH", false)
    end

    if data.numid == XH.playerData:getNumberID() then
        XH.UITool.setVisible(node, "_KW_APPLY_NODE", self:getData():getApplyNum() > 0)
        XH.UITool.setText(node, "_KW_APPLY_NUM", self:getData():getApplyNum() > 99 and "99+" or self:getData():getApplyNum())
        XH.UITool.addTouchEventListener(node, "_KW_UI_INVITE", handler(self, self.on_KW_APPLY_NUM))
    else
        XH.UITool.setVisible(node, "_KW_APPLY_NODE", false)
    end
end

function NewIMListView:setGroupInfo(node, data, index)
    XH.UITool.setText(node, "_KW_GROUP_NAME", XH.StringTool.cutStringByLength(clone(data.szData), 10))
    -- 设置头像
    local headNode = XH.UIRemoteImage.create(XH.UITool.seekNodeByName(node, "_KW_IMG_HEAD"))
    if headNode then
        headNode:setSwallowTouches(false)
        headNode:setDefaultTexture(KW_IMG_DEFULT_HEAD, ccui.TextureResType.plistType)
        headNode:setUrl(data.szUrl)
        headNode:addTouchEventListener(handler(data, handler(self, self.onTouchEventGroupSet)))
    end
    
    XH.UITool.addTouchEventListener(node, "_KW_UI_RANK", handler(data, handler(self, self.on_KW_UI_RANK)))
    -- 身份筛选,自身为组长
    if data.nTeaOwnerNumid == XH.playerData:getNumberID() then
        -- _KW_UI_CONTROLL
        XH.UITool.loadTexture(node,"_KW_UI_CONTROLL","imteahouse_manage_group_member.png", ccui.TextureResType.plistType)
        XH.UITool.addTouchEventListener(node, "_KW_UI_CONTROLL", handler(data, handler(self, self.on_KW_UI_CONTROLL)))
    else
        XH.UITool.loadTexture(node,"_KW_UI_CONTROLL","imteahouse_check_member_btn.png", ccui.TextureResType.plistType)
        XH.UITool.addTouchEventListener(node, "_KW_UI_CONTROLL", handler(data, handler(self, self.on_KW_UI_LOOK)))
    end
    local userCntTxt = XH.UITool.seekNodeByName(node, "_KW_USER_CNT")
    userCntTxt:setString("人数:"..data.nOnLineUserCnt.."/"..data.nUserCnt)
end

function NewIMListView:setTableInfo(node, data, index)
    self:setNormalTableInfo(node, data, 0, 0)
end

function NewIMListView:setNormalTableInfo(node, data, index, tableType)
    dump(data)
    local isContainSelf = XH.lobby:getModule("Im"):isContainSelf(data)
    local setNode = node:getChildByName("_KW_TABLE_SET")
    if setNode then
        setNode:addTouchEventListener(handler(data, handler(self, self.onTouchRoomInfo)))
    end
    local gameNames = XH.areaData:getAreaGameNameList()
    if gameNames[data.gameid] then
        XH.UITool.setText(node, "_KW_UI_GAME_NAME", gameNames[data.gameid])
    end
    if data.gameRule then
        local luaString = "return {" .. data.gameRule .. "}"
        local luaFunction = loadstring(luaString)
        if luaFunction then
            local rule = luaFunction()
            if rule["maxQuanShu"] then
                XH.UITool.setText(node, "_KW_UI_PLAY_COUNT", rule["maxQuanShu"] .. "圈")
            else
                if data.maxcount and data.maxcount > 0 then
                    XH.UITool.setText(node, "_KW_UI_PLAY_COUNT", data.maxcount .. "局")
                else
                    XH.UITool.setVisible(node, "_KW_UI_PLAY_COUNT", false)
                end
            end
        end
    end
    -- if tableType and tableType == self:getData().TABLEDATATYPE.NORMALOPEN then
        if tonumber(data.paytype) == 0 then
            XH.UITool.setText(node, "_KW_UI_PAY_TYPE", "房主支付")
        elseif tonumber(data.paytype) == 1 then
            XH.UITool.setText(node, "_KW_UI_PAY_TYPE", "平摊支付")
        elseif tonumber(data.paytype) == 22 then
            XH.UITool.setText(node, "_KW_UI_PAY_TYPE", "平摊支付")
        elseif tonumber(data.paytype) == 23 then
            XH.UITool.setText(node, "_KW_UI_PAY_TYPE", "冠军支付")
        else
            XH.UITool.setText(node, "_KW_UI_PAY_TYPE", "")
        end
    -- else
    --     XH.UITool.setText(node, "_KW_UI_PAY_TYPE", "")
    -- end
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
                    headNode:setVisible(true)
                    if isContainSelf then
                        XH.lobby:getModule("Im"):updateHeadImg(headPanel, nil, "imteahouse_box_bg.png")
                    end
                    headNode:setEnabled(true)
                    headNode:addTouchEventListener(handler(data, handler(self, self.onTouchRoomInfo)))
                else
                    headNode:setVisible(false)
                end
            end
        end
    end
end

function NewIMListView:updateLableUI(bEnabled)
    self._lableList:setEnabled(not bEnabled)
    if bEnabled then
        self._lableList:loadTexture("friend_lable_list_2.png", ccui.TextureResType.plistType)
        self._lableFollow:loadTexture("friend_lable_follow_1.png", ccui.TextureResType.plistType)
    else
        self._lableList:loadTexture("friend_lable_list_1.png", ccui.TextureResType.plistType)
        self._lableFollow:loadTexture("friend_lable_follow_2.png", ccui.TextureResType.plistType)
    end
    self._lableList:ignoreContentAdaptWithSize(true)
    self._lableFollow:ignoreContentAdaptWithSize(true)
    self._lableFollow:setEnabled(bEnabled)
    self._friendListView:setVisible(bEnabled)
    self._friendListViewV2:setVisible(bEnabled)
    self._tableListView:setVisible(not bEnabled)
    if self._lableRecall then
        self._lableRecall:setVisible(false)
    end
end

function NewIMListView:setActionEx(node, data)
    if not data.player_state or ( data.player_state ~= XH.IMProtocol.PLAYERSTATE.PS_OFFLINE and data.player_state ~= XH.IMProtocol.PLAYERSTATE.PS_ONLINE and data.player_state ~= XH.IMProtocol.PLAYERSTATE.PS_SELF) then
        return
    end
    local subTime = 0
    if data.last_login_time then
        subTime = os.time() - data.last_login_time
    end
    XH.UITool.setVisible(node,"KW_ACT_TEXT",false)
    if data.player_state == XH.IMProtocol.PLAYERSTATE.PS_ONLINE and not self:judgeDefultChannelId(data.channelid) then
        XH.UITool.loadTexture(node, "_KW_UI_INVITE", KW_IMG_FRIENDLY_TIP, ccui.TextureResType.plistType)
        XH.UITool.addTouchEventListener(node, "_KW_UI_INVITE", handler(data, handler(self, self.onTouchEventFriendlyTips)))
    elseif data.player_state == XH.IMProtocol.PLAYERSTATE.PS_OFFLINE and subTime > 7 * KW_ONE_DAY --[[and not self:getData():getRecallAwardState()]] then
        XH.UITool.loadTexture(node, "_KW_UI_INVITE", KW_IMG_AWARD_BACK, ccui.TextureResType.plistType)
        XH.UITool.addTouchEventListener(node, "_KW_UI_INVITE", handler(data, handler(self, self.onTouchEventAwardRecall)))
    elseif data.player_state == XH.IMProtocol.PLAYERSTATE.PS_SELF then
        XH.UITool.addTouchEventListener(node, "_KW_UI_INVITE", handler(self, self.onTouchEventReqApplyList))
    end
end

function NewIMListView:getTableViewData(view)
    if view:getName() == "READYLISTVIEW" then
        local friendList = self:getData():getFriendList()
        for i = #friendList,1,-1 do
            if friendList[i].numid ==  XH.playerData:getNumberID() then
                table.remove(friendList, i)
            end
        end
        return friendList
    elseif view:getName() == "FRIENDLISTVIEW" then
        local friendList = self:getData():getFriendList()
        if self._isSearching then
            friendList = self._searchPlayer
        else
            local isAddSelf = false
            for _,v in pairs(friendList) do
                if v.numid ==  XH.playerData:getNumberID() then
                    isAddSelf = true
                end
            end
            if not isAddSelf then
                local selfInfo = self:getData():generateSelfInfo()
                table.insert(friendList, 1, selfInfo)
            end
        end
        return friendList
    elseif view:getName() == "GROUPLISTVIEW" then
        return self:getData():getGroupList()
    else
        return self:sortTableList()
    end
end

function NewIMListView:getTableViewItemModel(view)
    if view:getName() == "FRIENDLISTVIEW" then
        return self._playerItemV2
    elseif view:getName() == "READYLISTVIEW" then
        return self._readyItem
    elseif view:getName() == "GROUPLISTVIEW" then
        return self._groupItem
    else
        return self._tableItem
    end
end

function NewIMListView:reqTableViewInfo(view)
    if view:getName() == "FRIENDLISTVIEW" or view:getName() == "READYLISTVIEW" then
        XH.lobby:getModule("Im"):reqFriendListInfo()
    else
        XH.lobby:getModule("Im"):reqMixTableListInfo()
    end
end


function NewIMListView:onGroupListChanged(event)
    if self:getData():getLable() == self:getData().KW_LIST_TYPE.TABLELIST then
        if event.bNotUpdateUI then
            local groupList = self:getData():getGroupList()
            local bExsit = false
            for _ ,v in pairs(groupList) do
                if self._selectedTeaId == v.nTeaNumber then
                    bExsit = true
                end
            end
            if not bExsit then
                XH.TipTool.showToast("筛选失败,请重试")
            end
        else
            self:updateGroupFilter()
        end
    else
        self:onRefreshList()
    end
end

function NewIMListView:onTouchEventGroupSelect(data, send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._selectedTeaId  = data.nTeaNumber or 0
    self._selectedGroupId = data.nPasswd
    self._selectedGroupType = data.groupType
    self._selectedGroupName:setText(XH.StringTool.replaceMatchStr(data.name))
    if data.noClose == nil then
        self._groupFilterView:setVisible(false)
    else
        data.noClose = nil
    end
    cc.UserDefault:getInstance():setStringForKey("IMListLayer_Last_Selected_Group",self._selectedGroupId)
    if self._selectedGroupType == NewIMListView.GROUP_TYPE.GROUP then
        if XH.gameManager then
            self._tableViewBoxCreateBtn:setVisible(true)
            self._tableViewGroupCreateBtn:setVisible(false)
        else
            self._tableViewBoxCreateBtn:setVisible(false)
            self._tableViewGroupCreateBtn:setVisible(true)
        end
        if  not cc.UserDefault:getInstance():getBoolForKey("IMListLayer_First_Touch_Create_Table2", false) then
            self._createPop:setVisible(true)
        else
            self._createPop:setVisible(false)
        end
        local position = XH.playerData:getPlayerPosition()
        if position.gameID == 0 then 
            self._createTable:loadTexture("imteahouse_create_room.png",ccui.TextureResType.plistType)
        else
           self._createTable:loadTexture("imteahouse_back_room.png",ccui.TextureResType.plistType)
        end
        XH.lobby:getModule("TeaHouseList"):reqTeaHouseList(true)
    else
        self._tableViewBoxCreateBtn:setVisible(true)
        self._tableViewGroupCreateBtn:setVisible(false)
    end
    self:getData():getFilterTableListData(self._selectedGroupType,self._selectedGroupId)
    self:onRefreshList()
end

function NewIMListView:updateGroupFilter()
    --所有房间＞分组＞比赛场＞大厅包厢
    -- # 刷新分组显示
    local lastGroupId = cc.UserDefault:getInstance():getStringForKey("IMListLayer_Last_Selected_Group", 0)
    self._selectedGroupId = tonumber(lastGroupId)
    local group = self:getData():getGroupList()
    if self._selectedGroupId > self.GROUP_TYPE.BOX then
        -- 选中的是组，检查分组是否存在
        local find = false
        for _,v in pairs(group) do
            if tonumber(v.nPasswd) == self._selectedGroupId then
                find = true
            end
        end
        if not find then
            self._selectedGroupId = self.GROUP_TYPE.ALL
        end
    elseif self._selectedGroupId == self.GROUP_TYPE.BOX then
        if #self:getData():getFriendList() == 0 then
            self._selectedGroupId = self.GROUP_TYPE.ALL
        end
    elseif self._selectedGroupId == self.GROUP_TYPE.TEAHOSE then
        if #self:getData():getTeahouseList() == 0 then
            self._selectedGroupId = self.GROUP_TYPE.ALL
        end
    end
    self._groupFilterView:removeAllChildren()
    local filterList = {}
    table.insert( filterList, {name = "所有房间",nPasswd = self.GROUP_TYPE.ALL, groupType = self.GROUP_TYPE.ALL} )
    for _,v in pairs(group) do
        local groupInfo = {}
        groupInfo.name = v.szData
        groupInfo.url = v.szUrl
        groupInfo.nPasswd = v.nPasswd
        groupInfo.groupType = self.GROUP_TYPE.GROUP
        groupInfo.nTeaNumber = v.nTeaNumber
        table.insert( filterList, groupInfo )
    end
    if #self:getData():getTeahouseList() > 0 then
        table.insert( filterList, {name = "亲友圈",groupType = self.GROUP_TYPE.TEAHOSE, nPasswd = self.GROUP_TYPE.TEAHOSE} )
    end
    if #self:getData():getFriendList() > 0 then
        table.insert( filterList, {name = "大厅包厢",groupType = self.GROUP_TYPE.BOX, nPasswd = self.GROUP_TYPE.BOX} )
    end
    for _, value in pairs(filterList) do
        local node  = self._groupFilterItem:clone()
        if value.url then
            XH.UITool.setVisible(node,"KW_FILTER_GROUP",true)
            XH.UITool.setVisible(node,"KW_FILTER_NORMALE",false)
            XH.UITool.setText(node,"KW_GROUP_NAME",value.name)
            local headNode = XH.UIRemoteImage.create(XH.UITool.seekNodeByName(node, "_KW_IMG_HEAD"))
            if headNode then
                headNode:setDefaultTexture(KW_IMG_DEFULT_HEAD, ccui.TextureResType.plistType)
                headNode:setUrl(value.url)
            end
        else
            XH.UITool.setVisible(node,"KW_FILTER_GROUP",false)
            XH.UITool.setVisible(node,"KW_FILTER_NORMALE",true)
            XH.UITool.setText(node,"KW_FILTER_NORMALE",value.name)
        end
        node:addTouchEventListener(handler(value, handler(self, self.onTouchEventGroupSelect)))
        if value.nPasswd == self._selectedGroupId then
            value.noClose = true
            self:onTouchEventGroupSelect(value, node, ccui.TouchEventType.ended)
            XH.UITool.setVisible(node,"KW_GROUP_SELECTED",true)
        else
            XH.UITool.setVisible(node,"KW_GROUP_SELECTED",false)
        end
        self._groupFilterView:addChild(node)
    end
    local cnt  = #filterList or 0
    local height =  self._groupFilterItem:getContentSize().height
    local width = self._groupFilterView:getContentSize().width
    local newheight =  height * (cnt > 10 and 10 or cnt )
    self._groupFilterView:setContentSize(cc.size(width,newheight))
    
end

function NewIMListView:getShowData()
    if self._bReadyState or self:getData():getLable() <= self:getData().KW_LIST_TYPE.PLAYERLIST then
        if self._curFriendsType == self.KW_FRIEND_TYPE.GROUP then
            return self:getData():getGroupList()
        else
            return self:getData():getFriendList()
        end
    elseif self:getData():getLable() == self:getData().KW_LIST_TYPE.TABLELIST then
        return self:getData():getTableList()
    end
    return {}
end

function NewIMListView:onTouchEventCreateTable(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._createPop:setVisible(false)
    cc.UserDefault:getInstance():setBoolForKey("IMListLayer_First_Touch_Create_Table2", true)
    self._createRoomAnim:setVisible(false) 
    cc.UserDefault:getInstance():setBoolForKey("IMListLayer_First_Table_Guide", true) 
    local position = XH.playerData:getPlayerPosition()
    if position.gameID == 0 then
        if cc.UserDefault:getInstance():getBoolForKey("BAN_PLAYER_CAN_GAME".. XH.playerData:getNumberID()) then
            XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK,            
            }, "您已被封禁对局，无法进入房间，如有疑问请联系客服。")
        else
            XH.viewManager:openView("IMCreateRoomView",nil,{payModeType = 1,payTypeTable = {22,23}, nGroupId = self._selectedTeaId })
        end
        XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_CREATE_BACK_ROOM_CLICK,{label = "创建房间"})
    else
        XH.TipTool.showLoading()
        local func = function ()
		    if tonumber(position.teaID) ~= 0 and not XH.lobby:getModule("Im"):checkIsGroup(position.teaID) then
			    --事件分发,请求进入比赛场
                XH.teaHouseManager:joinTeaHouse(position.teaID, position.roomID)
		    else
                local func = function( )
                    local param = {}
                    param.appID = position.gameAppID
                    param.gameID = position.gameID
                    param.roomMode = XH.ROOM_MODE.BOXROOM
                    param.roomID = position.roomID
                    param.srsGroupID = position.srsGroupID
                    param.sRoomMode = position.roomMode
                    XH.lobby:getModule("Im"):getIMData():setEnterTeaHouseID(position.teaNumber)
                    XH.roomManager:gameStart(param)
                end

                if XH.lobby:getModule("Lobby"):isNeedHotUpdata(position.gameID) then
                    lobby.gameHotUpdateSuccessCallBack = func
                else
                    func()
                end
		    end
            XH.TipTool.hideLoading()
        end
        self:performWithDelay(func, 0.2)
        XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_CREATE_BACK_ROOM_CLICK,{label = "返回房间"})
    end
end

function NewIMListView:showListLayer()
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
    if self:getData():getIsSeer() then
        self:on_KW_LABLE_LIST(self._lableList, ccui.TouchEventType.ended)
    else
        self:on_KW_LABLE_FOLLOW(self._lableFollow, ccui.TouchEventType.ended)
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
                    if self:getData():getIsSeer() then
                        self:on_KW_LABLE_LIST(self._lableList, ccui.TouchEventType.ended)
                    else
                        self:on_KW_LABLE_FOLLOW(self._lableFollow, ccui.TouchEventType.ended)
                    end
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

function NewIMListView:onTouchRoomInfo(data,send, eventType)
    local ishowCreateBnt = self._selectedGroupType == self.GROUP_TYPE.GROUP
    XH.viewManager:openView("IMTeaHouseRoomInfoView",nil,{roominfo = data,nTeaId = self._selectedTeaId, bShowCreate = ishowCreateBnt})
    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_BEGIN_JOININ_TABLE_CLICK,{item_id = data.gameid})
end

function NewIMListView:sortTableList()
    local tabList = self:getData():getFilterTableListData(self._selectedGroupType, self._selectedGroupId)
    local selfTable = nil
    for i = #tabList, 1,-1 do
        if XH.lobby:getModule("Im"):isContainSelf(tabList[i]) then
            selfTable = tabList[i]
            table.remove(tabList, i)
        end
    end
    if selfTable then
        table.insert(tabList, 1 ,selfTable)
    end
    return tabList
end

function NewIMListView:updeteSearchPlayerInfo(event)
    --刷新界面
    local searchdata = event.info
    if self._isSearching and searchdata.playerinfo.bFind  then
        if searchdata.state == 3 then
            self._searchPlayer = {}
            local player = {}
            player.bInTea = false 
            player.headurl = searchdata.playerinfo.headurl
            player.nickname = searchdata.playerinfo.nickname
            player.player_state = searchdata.playerinfo.online and 8 or 1
            player.shieldState = searchdata.joinLimit
            player.numid = searchdata.playerinfo.numid
            player.last_login_time = searchdata.lastLoginTime
            player.timeprop = {}
            table.insert( self._searchPlayer, player)
            self:onRefreshList()
        else
            self._isSearching = false
            if searchdata and searchdata.playerinfo and searchdata.playerinfo.numid == XH.playerData:getNumberID() then
                XH.TipTool.showToast("不能搜索自己哦~")
            else
                XH.TipTool.showToast("当前玩家还不是你的牌友")
            end
        end
    else
        self._isSearching = false
    end
end

function NewIMListView:updateTableInfo(event)
    XH.lobby:getModule("Im"):reqMixTableListInfo()
end


return NewIMListView
  �  