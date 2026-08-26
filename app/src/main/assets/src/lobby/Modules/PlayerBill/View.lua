---@class PlayerBillView : View
local PlayerBillView = class("PlayerBillView", XH.ViewBase)

PlayerBillView.ColorDetailText = cc.c3b(163,111,72)
PlayerBillView.ColorDetailTextSelf = cc.c3b(30,145,141)
PlayerBillView.MaxDateSelectCount = 7

-- 每新增日期对应的账单底部
local KW_CONTENT_HEIGHT_SIZE = 60

-- 请求多天日期时的状态
local KW_CONTENT_STATE_WITH_DAYS = {
    STATE_REFRESH = -1,
    HAS_FINISHED = 0,
}

function PlayerBillView:getCSBPath()
    return "cocosStudio/hall/CSB/PlayerBillView.csb"
end

function PlayerBillView:getBindingInfo()
    return {
        ["_KW_IMG_BG"] = { varName = "_imgBg", onTouch = "onTouchEventBg" },
        ["_KW_PANEL_ADAPT"] = { varName = "_panelAdapt" },
        ["_KW_PANEL_BILL_LIST"] = { varName = "_panelBillList" },
        ["_KW_TEXT_DATE"] = { varName = "_textDate" },
        ["_KW_BMFONT_BIG_WIN_COUNT"] = { varName = "_textBigWinCount" },
        ["_KW_BMFONT_TODAY_SCORE"] = { varName = "_textTodayScore" },
        ["_KW_BMFONT_TODAY_PLAYCOUNT"] = { varName = "_textTodayPlayCount" },
        ["_KW_PANEL_LIST"] = { varName = "_panelListInfo" },
        ["_KW_BTN_SEE_PLAYBACK"] = {  varName = "_btnSeePlayback", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventSeePlayback" },
        ["_KW_BTN_LIST_FRESH"] = { varName = "_btnFresh", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventFresh" },
        ["_KW_BTN_BACK"] = {  varName = "_btnBack", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventBack" },
        ["_KW_BTN_SHOW_DATE"] = { varName = "_btnShowDate", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventShowDate" },
        ["_KW_BTN_HIDE_DATE"] = { varName = "_btnHideDate", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventHideDate" },
        ["_KW_IMG_DATE_SELECT"] = { varName = "_imgDateSelect" },
        ["_KW_TEXT_NOTICE_NO_BILL"] = { varName = "_textNoticeNoBill" },
        
        ["_KW_PANEL_BILL_DETAIL"] = { varName = "_panelDetail" },
        ["_KW_TEXT_GAMENAME"] = { varName = "_textDetailGameName" },
        ["_KW_TEXT_DETAIL_ROOMID"] = { varName = "_textDetailRoomId" },
        ["_KW_TEXT_DETAIL_DATE_TIME"] = { varName = "_textDetailDateTime" },
        ["_KW_TEXT_DETAIL_PLAYCOUNT"] = { varName = "_textDetailPlayCount" },
        ["_KW_TEXT_DETAIL_INDEX"] = { varName = "_textDetailIndex" },
        
        ["_KW_PANEL_DETAIL_PLAYER_NAME"] = { varName = "_panelDetailPlayerName" },
        ["_KW_PANEL_SINGLE_NAME_ID"] = { varName = "_panelDetailNameId" },
        ["_KW_PANEL_PLAYER_TOTAL_SCORE"] = { varName = "_panelDetailTotalScores" },
        ["_KW_TEXT_PLAYER_TOTAL_SCORE"] = { varName = "_textTotalScore" },
        ["_KW_BTN_DETAIL_RETURN_LIST"] = { varName = "_btnReturnToList", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventReturnToList" },
        ["_KW_BTN_DETAIL_SHARE"] = { varName = "_btnSwitchShare", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventSwitchShare" },
        ["_KW_BTN_DETAIL_PLAYBACK"] = { varName = "_btnSwitchPlayback", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventSwitchPlayback" },
        ["_KW_PANEL_DETAIL_LIST"] = { varName = "_panelDetailListInfo" },
        ["_KW_PANEL_DETAIL_ITEM"] = { varName = "_panelDetailItem" },
        
        ["_KW_BTN_DATE_SELECT_1"] = { varName = "_btnDateSelect_1", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventDateSelect" },
        ["_KW_BTN_DATE_SELECT_2"] = { varName = "_btnDateSelect_2", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventDateSelect" },
        ["_KW_BTN_DATE_SELECT_3"] = { varName = "_btnDateSelect_3", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventDateSelect" },
        ["_KW_BTN_DATE_SELECT_4"] = { varName = "_btnDateSelect_4", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventDateSelect" },
        ["_KW_BTN_DATE_SELECT_5"] = { varName = "_btnDateSelect_5", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventDateSelect" },
        ["_KW_BTN_DATE_SELECT_6"] = { varName = "_btnDateSelect_6", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventDateSelect" },
        ["_KW_BTN_DATE_SELECT_7"] = { varName = "_btnDateSelect_7", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventDateSelect" },
        
        ["_KW_BTN_GAMELIST_ITEM"] = { varName = "_btnGameListItem", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventSelectGame" },
        ["_KW_BTN_SHOW_GAMELIST"] = { varName = "_btnShowGameList", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventShowGameList" },
        ["_KW_BTN_HIDE_GAMELIST"] = { varName = "_btnHideGameList", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventHideGameList" },
        ["_KW_IMG_GAMELIST"] = { varName = "_imgGameList" },
        ["_KW_TEXT_SELECT_GAME"] = { varName = "_textSelectGame" },
        ["_KW_LISTVIEW_GAMELIST"] = { varName = "_listviewGameList" },

        ["_KW_PANEL_BILL_MERGE"] = { varName = "_panelBillMerge" },
        ["_KW_PANEL_BILL_MERGE_LIST"] = { varName = "_panelBillMergeList" },
        ["_KW_BTN_MERGE_BILL"] = { varName = "_btnMergeBill", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventMergeBill" },

        ["_KW_BTN_DATE_SELECT_WITH_2"] = { varName = "_btnDatesSelect", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventDateSelectDays" },
    }
end

function PlayerBillView.isShowBillMerge()
    local lobbyID = XH.areaData:getLobbyID()
    if lobbyID == XH.LOBBY_ID.NINGBO then
        return true
    end
    return false
end

function PlayerBillView:ctor(param)
    param = param or {}
    PlayerBillView.super.ctor(self, param)
    self:initData()
    self:initView()
    self:setNodeEventEnabled(true)
    -- 判断是否需要最近两天界面展示
    self:initAloneDays()
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_24) 
end

function PlayerBillView:setNodeEventEnabled(enable)
    self:registerScriptHandler(function(state)
        print("state == ",state)
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
end

function PlayerBillView:initData()
    self._bFirstEnter = true
    self._billInfoList = {}
    self._detailInfoList = {}
    self._bShowShare = true
    self._selectGameID = 0

    self._nReqWithDays = KW_CONTENT_STATE_WITH_DAYS.STATE_REFRESH  -- 是否同时最近数据
    self._nHaveReqDays = 0  -- 请求的总天数

    XH.lobby:getModule("PlayerBill"):initData()
end

function PlayerBillView:initView()
    self:initDateSelect()
    self:initGameListSelect()
    self._panelBillList:setVisible(true)
    self._panelDetail:setVisible(false)
    self._panelBillMerge:setVisible(PlayerBillView.isShowBillMerge())
    self._panelListInfo:setVisible(not PlayerBillView.isShowBillMerge())
    self:updateMergeBtnEnable()
    self:adaptForLiuHai()
end

--适配
function PlayerBillView:adaptForLiuHai()
    if display.notchWidth <= 0 or not self._panelAdapt then
        return 
    end
    local notchWidth = display.notchWidth
    local nowSize = self._panelAdapt:getContentSize()
    self._panelAdapt:setContentSize(cc.size(nowSize.width - notchWidth*2, nowSize.height))
    ccui.Helper:doLayout(self._panelAdapt)
end

function PlayerBillView:onEnter()
    --第一次进入才主动更新
    if self._bFirstEnter then
        XH.lobby:getModule("PlayerBill"):reqCurTime()
    end
    self._bFirstEnter = false
end

function PlayerBillView:onExit()
    if self._playerBillEventProxy then
        self._playerBillEventProxy:removeAllEventListeners()
		self._playerBillEventProxy = nil
    end
end

function PlayerBillView:getProxyEvents()
    local playerBillModule = XH.lobby:getModule("PlayerBill")
    return {
        { module = playerBillModule, eventKeyName = playerBillModule.EVENT_HALL_BILL_UPDATE_TOTALLIST,  callBack = "onUpdateTotalList" },
        { module = playerBillModule, eventKeyName = playerBillModule.EVENT_HALL_BILL_UPDATE_BILLDETAIL,  callBack = "onUpdateBillDetail" },
        { module = playerBillModule, eventKeyName = playerBillModule.EVENT_HALL_BILL_UPDATE_CURTIME,  callBack = "onUpdateCurTime" },
        { module = playerBillModule, eventKeyName = playerBillModule.EVENT_HALL_BILL_CLOSE,  callBack = "onBillClose" },
    }
end

function PlayerBillView:initDateSelect()
    self:closeDateSelect()

    local showCount = XH.lobby:getModule("PlayerBill").SHOW_BILL_DAY
    local imgSize = self._imgDateSelect:getContentSize()
    local totalHeight = 60 * showCount + 20
    self._imgDateSelect:setContentSize(cc.size(imgSize.width, totalHeight))
    
    for i = 1, self.MaxDateSelectCount do
        if self["_btnDateSelect_"..i] then
            self["_btnDateSelect_"..i]:setTag(i)
            self["_btnDateSelect_"..i]:setPositionY(20*(showCount-i+1)+40*(showCount-i+1/2))
            if i > showCount then
                self["_btnDateSelect_"..i]:setVisible(false)
            end
        end
    end
end

function PlayerBillView:initGameListSelect()
    self:closeGameListSelect()

    local gameIdList = XH.lobby:getModule("PlayerBill"):getBoxRoomGameListFinal()
    local gameNameList = XH.lobby:getModule("PlayerBill"):getAreaGameNameListFinal()

    self._listviewGameList:removeAllChildren()
    self:addGameListBtn("全选", 0)
    if gameIdList and gameNameList then
        for _, gameid in pairs(gameIdList) do
            self:addGameListBtn(gameNameList[gameid], gameid)
        end
    end

    self:setSelectGame(0)
end

function PlayerBillView:addGameListBtn(textStr, gameId)
    local newItem = self._btnGameListItem:clone()
    newItem:setTitleText(textStr or "")
    newItem:setTag(gameId or 0)
    self._listviewGameList:addChild(newItem)
end

function PlayerBillView:onBillClose(event)
    self:close()
end

--更新当前日期
function PlayerBillView:onUpdateCurTime(event)
    local curBillDateStr = XH.lobby:getModule("PlayerBill"):getCurBillDateStr()
    self._textDate:setString(curBillDateStr)

    self._curBillTimeIndex = 0
    
    local curTime = XH.lobby:getModule("PlayerBill"):getCurTime()
    local showCount = XH.lobby:getModule("PlayerBill").SHOW_BILL_DAY
    for i = 1, showCount do
        if self["_btnDateSelect_"..i] then
            self["_btnDateSelect_"..i]:setTitleText(os.date("%Y-%m-%d", curTime - 86400 * (showCount - i)))
        end
    end
end

--刷新战绩总列表
function PlayerBillView:onUpdateTotalList(event)
    local getBillInfo
    if self._selectGameID > 0 then
        getBillInfo = clone(XH.lobby:getModule("PlayerBill"):getCurBillInfoByGameId(self._selectGameID))
    else
        getBillInfo = clone(XH.lobby:getModule("PlayerBill"):getCurBillInfo())
    end

    -- 如果获取多日战绩
    if self._nReqWithDays > KW_CONTENT_STATE_WITH_DAYS.HAS_FINISHED then
        self._nReqWithDays = self._nReqWithDays - 1
        self:performWithDelay( function()
            self:selectDate(self.MaxDateSelectCount - self._nReqWithDays, "withDays_" .. self._nHaveReqDays)
        end , 0.001)
        return
    elseif self._nReqWithDays == KW_CONTENT_STATE_WITH_DAYS.HAS_FINISHED and self._nHaveReqDays ~= 0 then
        self._curBillTimeIndex = KW_CONTENT_STATE_WITH_DAYS.HAS_FINISHED
        if self._selectGameID > 0 then
            getBillInfo = clone(XH.lobby:getModule("PlayerBill"):getCurBillInfoWithDaysByGameId(self._nHaveReqDays, self._selectGameID))
        else
            getBillInfo = clone(XH.lobby:getModule("PlayerBill"):getCurBillInfoWithDays(self._nHaveReqDays))
        end
    end

    self._billInfoList = getBillInfo
    
    -- 屏蔽浙江包没有的游戏
    local finalResults = { }
    local gameNameList = XH.lobby:getModule("PlayerBill"):getAreaGameNameListFinal()

    local isCard13 = function(gameid)
        if XH.ReadGameListAndRuleConfig.judgeIsCard13ByGameId(gameid) and XH.lobby:getModule("PlayerBill")._curTime > 1590166800 then
            return true
        else
            return false
        end
    end
    for _, result in ipairs(self._billInfoList) do
        if result.gameID and gameNameList[result.gameID] and not isCard13(result.gameID) then
            table.insert(finalResults, result)
        end
    end

    self._billInfoList = finalResults

    self:refreshTableView()

    local displayUserId = XH.playerData:getNumberID()
    local nTodayScore = 0
    local nTodayPlayCount = #self._billInfoList
    local nBigWinCount = 0
    for _, billInfo in ipairs(self._billInfoList) do
        local selfScore = 0
        local bigScore = 0
        for _, tPlayerInfo in ipairs(billInfo.playerInfos) do
            if tPlayerInfo.totalscore > bigScore then
                bigScore = tPlayerInfo.totalscore
            end
            -- 统计玩家今日优胜值
            if tPlayerInfo.numid == displayUserId then
                selfScore = tPlayerInfo.totalscore
            end
        end
        nTodayScore = nTodayScore + selfScore
        if bigScore == selfScore then
            nBigWinCount = nBigWinCount + 1
        end
    end
    self:setBigWinCount(nBigWinCount)
    self:setTodayPlayCount(nTodayPlayCount)
    self:setTodayScore(nTodayScore)
    self._textNoticeNoBill:setVisible(#self._billInfoList == 0)

    self:updateMergeBtnEnable()
end

function PlayerBillView:onBtnEventDateSelect(send, eventType)
    self._nHaveReqDays = 0
    self:closeDateSelect()
    local index = send:getTag()
    self:selectDate(index)
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_25) 
end

function PlayerBillView:closeDateSelect()
    self._imgDateSelect:setVisible(false)
    self._btnShowDate:setVisible(true)
    self._btnHideDate:setVisible(false)
end

function PlayerBillView:showDateSelect()
    self._imgDateSelect:setVisible(true)
    self._btnShowDate:setVisible(false)
    self._btnHideDate:setVisible(true)
end

--日期
function PlayerBillView:selectDate(index, state)
    if index == self._curBillTimeIndex and state == nil then
        return
    end
    local dateStr = self["_btnDateSelect_" .. index]:getTitleText()
    if state == nil then
        self._textDate:setString(dateStr)
    end
    XH.lobby:getModule("PlayerBill"):setCurBillDateStr(dateStr)
    self._curBillTimeIndex = index
    XH.lobby:getModule("PlayerBill"):readPlayerBillInfo(self._curBillTimeIndex)
end

--冠军次数
function PlayerBillView:setBigWinCount(count)
    if not self._textBigWinCount then
        return 
    end
    self._textBigWinCount:setString(count)
end

--今日优胜值
function PlayerBillView:setTodayScore(score)
    if not self._textTodayScore then
        return 
    end
    self._textTodayScore:setString(score)
end

--今日场数
function PlayerBillView:setTodayPlayCount(count)
    if not self._textTodayPlayCount then
        return 
    end
    self._textTodayPlayCount:setString(count)
end

function PlayerBillView:getListPanel()
    if PlayerBillView.isShowBillMerge() then
        return self._panelBillMergeList
    end
    return self._panelListInfo
end

function PlayerBillView:refreshTableView(isCleanup)
    if not self:getListPanel() then
        return
    end
    if self._tvInfoList ~= nil and isCleanup then
        self._tvInfoList:removeFromParent()
        self._tvInfoList = nil
    end
    if self._tvInfoList == nil then
        self._tvInfoList = cc.TableView:create(self:getListPanel():getContentSize())
        self._tvInfoList:setDirection(cc.SCROLLVIEW_DIRECTION_VERTICAL)
        self._tvInfoList:setVerticalFillOrder(cc.TABLEVIEW_FILL_TOPDOWN)
        self._tvInfoList:setIgnoreAnchorPointForPosition(false)
        self._tvInfoList:setAnchorPoint(cc.p(0, 0))
        self._tvInfoList:setPosition(cc.p(0, 0))
        self:getListPanel():addChild(self._tvInfoList)
        self._tvInfoList:registerScriptHandler(handler(self, self.onTableCellTouched), cc.TABLECELL_TOUCHED)
        self._tvInfoList:registerScriptHandler(handler(self, self.onCellSizeForTable), cc.TABLECELL_SIZE_FOR_INDEX)
        self._tvInfoList:registerScriptHandler(handler(self, self.onTableCellAtIndex), cc.TABLECELL_SIZE_AT_INDEX)
        self._tvInfoList:registerScriptHandler(handler(self, self.onNumberOfCellsInTableView), cc.NUMBER_OF_CELLS_IN_TABLEVIEW)
    end
    if self._tvInfoList ~= nil then
        self._tvInfoList:reloadData()
    end
end

function PlayerBillView:onTableCellTouched(tableView, cell)

end

function PlayerBillView:onCellSizeForTable(tableView, index)
    local size = self:getListPanel():getContentSize()
    return size.width, 220
end

function PlayerBillView:onTableCellAtIndex(tableView, index)
    local cell = tableView:dequeueCell()
    if not cell then
        cell = cc.TableViewCell:new()
        local infoItem = require("lobby.Modules.PlayerBill.ListItem"):new()
        if infoItem ~= nil then
            infoItem:setAnchorPoint(cc.p(0, 0))
            infoItem:setPosition(0, 0)
            infoItem:setName("InfoItem")
            if self:getListPanel() then
                local itemSize = infoItem:getContentSize()
                local listSize = self:getListPanel():getContentSize()
                infoItem:setContentSize(cc.size(listSize.width, itemSize.height))
                ccui.Helper:doLayout(infoItem)
            end
            infoItem:setDetailBtnFunc(handler(self, self.onDetailBtnClick))
            infoItem:setSelectCallBack(handler(self, self.onSelectBillItem))
            cell:addChild(infoItem)
        end
    end
    local infoItem = cell:getChildByName("InfoItem")
    if infoItem ~= nil then
        infoItem:updateInfo(self._billInfoList[index + 1], index + 1)
    end
    return cell
end

function PlayerBillView:onSelectBillItem(index, bSelected)
    if self._billInfoList[index] then
        self._billInfoList[index].bSelected = bSelected
    end
    self:updateMergeBtnEnable()
end

function PlayerBillView:onNumberOfCellsInTableView(tableView)
    return #self._billInfoList
end

--详情按钮点击
function PlayerBillView:onDetailBtnClick(index)
    self._curShowRoomIndex = index

	local tCurRoomInfo, detailData 
    if self._selectGameID > 0 then
        tCurRoomInfo, detailData = XH.lobby:getModule("PlayerBill"):fetchDetailDataByIdx(index, self._nHaveReqDays, self._selectGameID)
    else
        tCurRoomInfo, detailData = XH.lobby:getModule("PlayerBill"):fetchDetailDataByIdx(index, self._nHaveReqDays)
    end
	if detailData then
		self:showDetailView(tCurRoomInfo, detailData)
	end
end

function PlayerBillView:onBtnEventBack(send, eventType)
    self:close()
end

--刷新按钮回调
function PlayerBillView:onBtnEventFresh(send, eventType)
    local lastFreshTime = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_LAST_REFRESH_LOBBY_BILL_TIME, 0)
    local betweenTime = os.time() - lastFreshTime
    if betweenTime < 10 then
        local tipsStr = "刷新过于频繁，".. (10- betweenTime) .."秒后再尝试刷新"
        XH.TipTool.showToast(tipsStr,2)
        return
    else
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_LAST_REFRESH_LOBBY_BILL_TIME, os.time())
    end

    self._nHaveReqDays = 0
    XH.lobby:getModule("PlayerBill"):refreshAllData()
end

--查看回放按钮回调
function PlayerBillView:onBtnEventSeePlayback(send, eventType)
    XH.viewManager:openView("PlayerBillInputView")
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_27) 
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.zyt24041701, {block_label='大厅战绩一级页'})
end

function PlayerBillView:onBtnEventShowDate(send, eventType)
    self:showDateSelect()
end

function PlayerBillView:onBtnEventHideDate(send, eventType)
    self:closeDateSelect()
end

function PlayerBillView:closeGameListSelect()
    self._imgGameList:setVisible(false)
    self._btnShowGameList:setVisible(true)
    self._btnHideGameList:setVisible(false)
end

function PlayerBillView:showGameListSelect()
    self._imgGameList:setVisible(true)
    self._btnShowGameList:setVisible(false)
    self._btnHideGameList:setVisible(true)
end

function PlayerBillView:setSelectGame(gameID)
    self._selectGameID = gameID

    local gameName = "全选"
    if self._selectGameID >0 then
        local gameNameList = XH.lobby:getModule("PlayerBill"):getAreaGameNameListFinal()
        gameName = gameNameList[gameID] or ""
    end

    if self._textSelectGame then
        self._textSelectGame:setString(gameName)
    end
end

function PlayerBillView:onBtnEventSelectGame(send, eventType)
    self:closeGameListSelect()
    self:setSelectGame(send:getTag())
    self:onUpdateTotalList()
    local tmpData = {}
    tmpData.userid = XH.playerData:getNumberID()
    tmpData.gamename = send:getTitleText()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.MY_26, tmpData)
end

function PlayerBillView:onBtnEventShowGameList(send, eventType)
    self:showGameListSelect()
end

function PlayerBillView:onBtnEventHideGameList(send, eventType)
    self:closeGameListSelect()
end

function PlayerBillView:onBtnEventMergeBill(send, eventType)
    local mergeListInfo = {}
    for _, billInfo in pairs(self._billInfoList) do
        if billInfo.bSelected then
            table.insert(mergeListInfo, billInfo)
        end
    end

    if #mergeListInfo == 0 then
        return
    end

    XH.viewManager:openView("PlayerBillMergeView", nil, mergeListInfo)
end

function PlayerBillView:updateMergeBtnEnable()
    if not PlayerBillView.isShowBillMerge() then
        return
    end
    local selectedCount = 0
    for _, bill in pairs(self._billInfoList) do
        if bill.bSelected then
            selectedCount = selectedCount + 1
            if selectedCount > 1 then
                break
            end
        end
    end
    if self._btnMergeBill then
        self._btnMergeBill:setEnabled(selectedCount > 1)
    end
end

---------------------------------详情界面--------------------------------------

function PlayerBillView:onUpdateBillDetail(event)
    self:showDetailView(event.msg.tCurRoomInfo, event.msg.tBillDetail)
end

function PlayerBillView:showDetailView(tCurRoomInfo, detailData)
    self._tCurRoomInfo = tCurRoomInfo
    self._panelBillList:setVisible(false)

    local topData = {}
    topData.gameID = tCurRoomInfo.gameID
    topData.roomID = tCurRoomInfo.config
    topData.totalgames = tCurRoomInfo.totalgames
    topData.finishgames = tCurRoomInfo.finishgames
    topData.time = tCurRoomInfo.starttime
    self:updateDetailTopView(topData)

    self:updateDetailSwitchBtns()
    self._detailInfoList = detailData.detailInfo.gameDetail
    self:refreshDetailTableView()
    self:updateDetailPlayerNames(tCurRoomInfo.playerInfos,tCurRoomInfo.roomHostID or 0)
    self:updateDetailTotalScores(tCurRoomInfo.playerInfos, tCurRoomInfo.endInfoEx)
    self._panelDetail:setVisible(true)
end

function PlayerBillView:updateDetailTopView(data)
    self:setDetailIndex(self._curShowRoomIndex)
    local gameNameList = XH.lobby:getModule("PlayerBill"):getAreaGameNameListFinal()
    self:setDetailGameName(gameNameList[data.gameID] or "")
    if XH.ChannelTool.checkIsDoubleKouGame() then
        self:setDetailGameName("双扣")
    end
    self:setDetailRoomId(string.format("%06d", data.roomID))
    self:setDetailPlayCount(data.finishgames, data.totalgames)
    self:setDetailDateTime(os.date("%Y-%m-%d %H:%M", data.time))
end

function PlayerBillView:setDetailIndex(index)
    if not self._textDetailIndex then
        return 
    end
    self._textDetailIndex:setString(index)
end

function PlayerBillView:setDetailGameName(nameStr)
    if not self._textDetailGameName then
        return 
    end
    self._textDetailGameName:setString(nameStr)
end

--房间号
function PlayerBillView:setDetailRoomId(roomId)
    if not self._textDetailRoomId then
        return 
    end
    self._textDetailRoomId:setString(roomId)
end

--局数
function PlayerBillView:setDetailPlayCount(count, countTotal)
    if not self._textDetailPlayCount then
        return 
    end
    self._textDetailPlayCount:setString(count.."/"..countTotal)
end

--时间
function PlayerBillView:setDetailDateTime(time)
    if not self._textDetailDateTime then
        return 
    end
    self._textDetailDateTime:setString(time)
end

function PlayerBillView:updateDetailPlayerNames(playerInfos,roomHostID)
    if not self._panelDetailPlayerName or not self._panelDetailNameId then
        return 
    end

    if self._tbDetailPlayerNameTexts then
        for _, v in pairs(self._tbDetailPlayerNameTexts) do
            v:removeFromParent()
        end
    end
    self._tbDetailPlayerNameTexts = {}

    local displayUserId = XH.playerData:getNumberID()
    local panelSize = self._panelDetailPlayerName:getContentSize()
    local playerCount = #playerInfos
    local singleWidth = panelSize.width/playerCount
    for i = 1, playerCount do
        local nameScoreLayout = self._panelDetailNameId:clone()

        local textName = ccui.Helper:seekWidgetByName(nameScoreLayout, "KW_TEXT_PLAYER_NAME")
        local nicknameStr = XH.StringTool.cutStringByLength(playerInfos[i].nickname or "")
        textName:setString(nicknameStr)
        textName:setTextColor(displayUserId == playerInfos[i].numid and self.ColorDetailTextSelf or self.ColorDetailText)

        local textId = ccui.Helper:seekWidgetByName(nameScoreLayout, "KW_TEXT_PLAYER_ID")
        textId:setString(playerInfos[i].numid or "")
        
        textId:setTextColor(displayUserId == playerInfos[i].numid and self.ColorDetailTextSelf or self.ColorDetailText)
        nameScoreLayout:setPositionX(singleWidth*(i-1/2))

        if roomHostID and roomHostID > 0 and roomHostID == playerInfos[i].numid then
            XH.UITool.setVisible(nameScoreLayout,"KW_IMG_HOST",true)
        end

        self._panelDetailPlayerName:addChild(nameScoreLayout)
        self._tbDetailPlayerNameTexts[i] = nameScoreLayout
    end
end

function PlayerBillView:updateDetailTotalScores(playerInfos, endInfoEx)
    if not self._panelDetailTotalScores or not self._textTotalScore then
        return 
    end

    if self._tbDetailTotalScoreTexts then
        for _, v in pairs(self._tbDetailTotalScoreTexts) do
            v:removeFromParent()
        end
    end
    self._tbDetailTotalScoreTexts = {}

    local displayUserId = XH.playerData:getNumberID()
    local panelSize = self._panelDetailTotalScores:getContentSize()
    local playerCount = #playerInfos
    local singleWidth = panelSize.width/playerCount
    for i = 1, playerCount do
        local textScore = self._textTotalScore:clone()
        local totalScore = playerInfos[i].totalscore
        if totalScore >= 0 then
            totalScore = "+"..totalScore
        end
        if endInfoEx then
            local endInfoExItem = endInfoEx[tostring(playerInfos[i].numid)]
            if endInfoExItem then
                local kongsr = endInfoExItem["KONGSC"]
                local isSuanGangScore = endInfoExItem["ISSGF"] or 0
                if kongsr and isSuanGangScore == 0 then
                    totalScore = totalScore .. "("..kongsr..")"
                end
            end  
        end
        textScore:setString(totalScore)
        textScore:setTextColor(displayUserId == playerInfos[i].numid and self.ColorDetailTextSelf or self.ColorDetailText)
        textScore:setPositionX(singleWidth*(i-1/2))
        self._panelDetailTotalScores:addChild(textScore)
        self._tbDetailTotalScoreTexts[i] = textScore
    end
end

function PlayerBillView:updateDetailSwitchBtns()
    self._btnSwitchShare:setVisible(self._bShowShare)
    self._btnSwitchPlayback:setVisible(not self._bShowShare)
end

function PlayerBillView:refreshDetailTableView(isCleanup)
    if not self._panelDetailListInfo then
        return
    end
    if self._tvDetailList ~= nil and isCleanup then
        self._tvDetailList:removeFromParent()
        self._tvDetailList = nil
    end
    if self._tvDetailList == nil then
        self._tvDetailList = cc.TableView:create(self._panelDetailListInfo:getContentSize())
        self._tvDetailList:setDirection(cc.SCROLLVIEW_DIRECTION_VERTICAL)
        self._tvDetailList:setVerticalFillOrder(cc.TABLEVIEW_FILL_TOPDOWN)
        self._tvDetailList:setIgnoreAnchorPointForPosition(false)
        self._tvDetailList:setAnchorPoint(cc.p(0, 0))
        self._tvDetailList:setPosition(cc.p(0, 0))
        self._panelDetailListInfo:addChild(self._tvDetailList)
        self._tvDetailList:registerScriptHandler(handler(self, self.onDetailTableCellTouched), cc.TABLECELL_TOUCHED)
        self._tvDetailList:registerScriptHandler(handler(self, self.onDetailCellSizeForTable), cc.TABLECELL_SIZE_FOR_INDEX)
        self._tvDetailList:registerScriptHandler(handler(self, self.onDetailTableCellAtIndex), cc.TABLECELL_SIZE_AT_INDEX)
        self._tvDetailList:registerScriptHandler(handler(self, self.onDetailNumberOfCellsInTableView), cc.NUMBER_OF_CELLS_IN_TABLEVIEW)
    end
    if self._tvDetailList ~= nil then
        self._tvDetailList:reloadData()
    end
end

function PlayerBillView:onDetailTableCellTouched(tableView, cell)

end

function PlayerBillView:onDetailCellSizeForTable(tableView, index)
    local size = self._panelDetailItem:getContentSize()
    return size.width, size.height
end

function PlayerBillView:onDetailTableCellAtIndex(tableView, index)
    local cell = tableView:dequeueCell()
    if not cell then
        cell = cc.TableViewCell:new()
        local detailItem = require("lobby.Modules.PlayerBill.DetailItem"):new()
        if detailItem ~= nil then
            detailItem:setAnchorPoint(cc.p(0, 0))
            detailItem:setPosition(0, 0)
            detailItem:setName("DetailItem")
            if self._panelDetailItem then
                detailItem:setContentSize(self._panelDetailItem:getContentSize())
                ccui.Helper:doLayout(detailItem)
            end
            cell:addChild(detailItem)
        end
    end
    local detailItem = cell:getChildByName("DetailItem")
    if detailItem ~= nil then
        detailItem:updateInfo(self._detailInfoList[index + 1], index + 1, self._tCurRoomInfo.gameID, self._tCurRoomInfo.playerInfos)
        detailItem:setShowShareBtnBool(self._bShowShare)
    end
    return cell
end

function PlayerBillView:onDetailNumberOfCellsInTableView(tableView)
    return #self._detailInfoList
end

--返回列表
function PlayerBillView:onBtnEventReturnToList(send, eventType)
    self._panelBillList:setVisible(true)
    self._panelDetail:setVisible(false)
end

function PlayerBillView:onBtnEventSwitchShare(send, eventType)
    self._bShowShare = false
    self:updateDetailSwitchBtns()
    self:refreshDetailTableView()
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_30, {tabName = "分享"})
end

function PlayerBillView:onBtnEventSwitchPlayback(send, eventType)
    self._bShowShare = true
    self:updateDetailSwitchBtns()
    self:refreshDetailTableView()  
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_30, {tabName = "回放"})
end

-- 是否显示最近的战绩信息
function PlayerBillView:initAloneDays()
    if XH.areaData:isSupportBillDays() then
        self._btnDatesSelect:setVisible(true)
        local contentSize = self._imgDateSelect:getContentSize()
        self._imgDateSelect:setContentSize(contentSize.width, contentSize.height + KW_CONTENT_HEIGHT_SIZE)
    end
end

-- 新增查看最近两天信息
function PlayerBillView:onBtnEventDateSelectDays(send, eventType)
    self:closeDateSelect()
    self._textDate:setText(send:getTitleText())
    local getName = string.split(send:getName(), "_")
    local index = tonumber(getName[#getName])
    if index and index < self.MaxDateSelectCount then
        self._nHaveReqDays = index
        self._nReqWithDays = index - 1
        self:selectDate(self.MaxDateSelectCount - self._nReqWithDays, "withDays_" .. self._nHaveReqDays)
    else
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK,
        } , "日期选择异常，请关闭重试")
    end
end

return PlayerBillView   �  