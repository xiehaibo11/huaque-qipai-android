---@class IMTeaHouseRankView : View
local IMTeaHouseRankView = class("IMTeaHouseRankView", XH.ViewBase)
local TeahouseManager = require("src/teahouse/Manager/TeaHouseManager")
local KW_MAX_NAME_FONT_SIZE = 36
local KW_MIN_NAME_FONT_SIZE = 26
local KW_MAX_NAME_WIDTH = 140
local KW_SHOW_BILL_DAY = 7

IMTeaHouseRankView.TAB_TYPE = {
    COST = 1, ---消耗
    PLAY_COUNT = 2, ---局数
    BIG_WINNER = 3, ---冠军
    SCORE = 4, ---优胜值
    WIN_LOSE = 5, ---胜负
}

IMTeaHouseRankView.DAY_TYPE = {
    DAY1 = 1, ---今日
    DAY2 = 2, ---昨日
    DAY7 = 3, ---七日
}

IMTeaHouseRankView.RANK_TYPE = {
    CLOSE = 0,
    OPEN = 1,
    FILTER = 2,
}

function IMTeaHouseRankView:getCSBPath()
    return "cocosStudio/hall/CSB/IMTeaHouse/IMTeaHouseRankView.csb"
end

function IMTeaHouseRankView:getBindingInfo()
    return {       
        ["_KW_PANEL_ROOT"] = {  varName = "_panelRoot"},
        ["_KW_PANEL_LIST_INFO"] = { varName = "_panelListInfo"},
        ["_KW_PANEL_LIST_ITEM"] = { varName = "_panelListItem"},
        ["_KW_BTN_CLOSE"] = { varName = "_btnClose", onTouchEnded = "onCloseClicked" },
        ["_KW_BTN_COST_1"] = { varName = "_btnCost1" , onTouchEnded = "onCost1Clicked" },
        ["_KW_BTN_COST_2"] = {varName = "_btnCost2", onTouchEnded = "onCost2Clicked" },
        ["_KW_BTN_PLAY_COUNT_1"] = { varName = "_btnPlayCount1" , onTouchEnded = "onPlayCount1Clicked" },
        ["_KW_BTN_PLAY_COUNT_2"] = { varName = "_btnPlayCount2", onTouchEnded = "onPlayCount2Clicked" },
        ["_KW_BTN_BIG_WINNER_1"] = { varName = "_btnBigWinner1" , onTouchEnded = "onBigWinner1Clicked" },
        ["_KW_BTN_BIG_WINNER_2"] = { varName = "_btnBigWinner2" , onTouchEnded = "onBigWinner2Clicked" },
        ["_KW_BTN_SCORE_1"] = { varName = "_btnScore1", onTouchEnded = "onScore1Clicked" },
        ["_KW_BTN_SCORE_2"] = { varName = "_btnScore2", onTouchEnded = "onScore2Clicked" },
        ["_KW_BTN_WIN_LOSE_1"] = { varName = "_btnWinLose1", onTouchEnded = "onWinLose1Clicked" },
        ["_KW_BTN_WIN_LOSE_2"] = { varName = "_btnWinLose2", onTouchEnded = "onWinLose2Clicked" },
        ["_KW_BTN_TYPE_DAY1_1"] = { varName = "_btnDay11", onTouchEnded = "onDay11Clicked" },
        ["_KW_BTN_TYPE_DAY1_2"] = { varName = "_btnDay12", onTouchEnded = "onDay12Clicked" },
        ["_KW_BTN_TYPE_DAY2_1"] = { varName = "_btnDay21", onTouchEnded = "onDay21Clicked" },
        ["_KW_BTN_TYPE_DAY2_2"] = { varName = "_btnDay22", onTouchEnded = "onDay22Clicked" },
        ["_KW_BTN_TYPE_DAY7_1"] = { varName = "_btnDay71", onTouchEnded = "onDay71Clicked" },
        ["_KW_BTN_TYPE_DAY7_2"] = { varName = "_btnDay72", onTouchEnded = "onDay72Clicked" },
        ["_KW_PANEL_LIST_TITLE"] = { varName = "_panelListTitle"},
        ["_KW_PANEL_LIST_TITLE_2"] = { varName = "_panelListTitle2"},
        ["_KW_TEXT_RANK_DESC"] = { varName = "_textRankDesc"},
        ["_KW_TEXT_RANK_DESC_2_1"] = { varName = "_textRankDesc21"},
        ["_KW_TEXT_RANK_DESC_2_2"] = { varName = "_textRankDesc22"},
        ["_KW_TEXT_RANK_DESC_2_3"] = { varName = "_textRankDesc23"},
        ["_KW_TEXT_SELF_RANK"] = { varName = "_textSelfRank" },
        ["_KW_TEXT_SELF_DESC"] = {  varName = "_textSelfDesc" },
        ["_KW_TEXT_SELF_DESC_TIP"] = { varName = "_textSelfDescTip" },
        ["_KW_NODE_RANK_1"] = { varName = "_nodeRank1"},
        ["_KW_NODE_RANK_2"] = { varName = "_nodeRank2"},
        ["_KW_NODE_RANK_3"] = { varName = "_nodeRank3"},
        ["_KW_NODE_RANK_4"] = { varName = "_nodeRank4"},
        ["_KW_PANEL_COST"] = { varName = "_panelCost"},
        ["_KW_PANEL_PLAY_COUNT"] = { varName = "_panelPlayCount"},
        ["_KW_PANEL_BIG_WINNER"] = { varName = "_panelBigWinner"},
        ["_KW_PANEL_SCORE"] = {  varName = "_panelScore"},
        ["_KW_PANEL_WIN_LOSE"] = { varName = "_panelWinLose"},
        ["_KW_PANEL_GAME_LIST"] = { varName = "_panelGameList"},
        ["_KW_PANEL_GAME_LIST_MASK"] = { varName = "_panelGameListMask"},
        ["_KW_IMG_GAME_LIST_BG"] = { varName = "_imgGameListBg"},
        ["_KW_BTN_HIDE_GAME_LIST"] = {  varName = "_btnHideGameList", onTouchEnded = "onHideGameListClicked" },
        ["_KW_BTN_SHOW_GAME_LIST"] = {  varName = "_btnShowGameList", onTouchEnded = "onShowGameListClicked" },
        ["_KW_BTN_GAME"] = { varName = "_btnGame" },
        ["_KW_SV_GAME_LIST"] = { varName = "_svGameList"},
        ["_KW_TEXT_GAME_SELECT"] = { varName = "_textGameSelect"},
        ["_KW_BTN_RANK_SETTING"] = { varName = "_btnRankSetting", onTouchEnded = "onRankSettingClicked" },

        ["_KW_PANEL_TYPE_FIXED"] = { varName = "_panelFixedType"},
        ["_KW_PANEL_TYPE_OPTIONAL"] = { varName = "_panelOptionalType"},
        ["_KW_PANEL_START_DATE"] = { varName = "_panelStartDate" },
        ["_KW_PANEL_END_DATE"] = { varName = "_panelEndDate"},
        ["_KW_BTN_BILL_SEARCH"] = { varName = "_btnSearch", onTouchEnded = "onSearchClicked" },
        ["_KW_CHECK_CHANGE_STARTDATE"] = { varName = "_changeStartDate"},
        ["_KW_CHECK_CHANGE_ENDDATE"] = { varName = "_changeEndDate" },
        ["_KW_IMG_START_DATE_FILTER_BG"] = { varName = "_startFilterBg"},
        ["_KW_IMG_END_DATE_FILTER_BG"] = { varName = "_endFilterBg"},
        ["_KW_TEXT_START_SEARCH_DATE"] = { varName = "_textStartSearchDay"},
        ["_KW_TEXT_END_SEARCH_DATE"] = {varName = "_textEndSearchDay"},
        ["_KW_TEXT_GROUP_TOTAL"] = {varName = "_groupTotalCost"},
        ["_KW_TEXT_TOTAL_COST"] = {varName = "_totalCosText"}
    }
end

function IMTeaHouseRankView:ctor(param)
    IMTeaHouseRankView.super.ctor(self)

    self._infoList = {}
    self._tvInfoList = nil

    self._tabType = nil
    self._tabNodes = {}

    self._dayType = nil
    self._dayNodes = {}

    self._bFirstEnter = true

    self._gameID = 0

    self._rankList = {}

    self._startDay = 0
    self._endDay = 0

    self._teaNum = param.nTeaNumber

    self._groupTotalCostNum = 0
    self:initUI()
    -- self:adaptUI()

    -- if not TeahouseManager.configManager.SetupConfig.IsOpenRankDateOptional then
        -- local dayType = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_RANK_LIST_DAY_TYPE_RECORD, IMTeaHouseRankView.DAY_TYPE.DAY1)
        -- self:setDayType(dayType, true)
    -- end
    self:reqRankInfo()
end

function IMTeaHouseRankView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_SCORE_RANK_UPDATE", callBack = "onScoreRankUpdate" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_BIG_WINNER_RANK_UPDATE", callBack = "onBigWinnerRankUpdate" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_COST_RANK_UPDATE", callBack = "onCostRankUpdate" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_RANK_CUR_DATE_UPDATE", callBack = "onCurDateUpdate" },
    }
end

function IMTeaHouseRankView:onEnter()
    if self._bFirstEnter then
         XH.lobby:getModule("Im"):reqCurTime()
    end
    self._bFirstEnter = false
end

function IMTeaHouseRankView:initUI()
    self._tabNodes[IMTeaHouseRankView.TAB_TYPE.COST] = { self._btnCost1, self._btnCost2 }
    self._tabNodes[IMTeaHouseRankView.TAB_TYPE.PLAY_COUNT] = { self._btnPlayCount1, self._btnPlayCount2 }
    self._tabNodes[IMTeaHouseRankView.TAB_TYPE.BIG_WINNER] = { self._btnBigWinner1, self._btnBigWinner2 }
    self._tabNodes[IMTeaHouseRankView.TAB_TYPE.SCORE] = { self._btnScore1, self._btnScore2 }
    self._tabNodes[IMTeaHouseRankView.TAB_TYPE.WIN_LOSE] = { self._btnWinLose1, self._btnWinLose2 }

    self._dayNodes[IMTeaHouseRankView.DAY_TYPE.DAY1] = { self._btnDay11, self._btnDay12 }
    self._dayNodes[IMTeaHouseRankView.DAY_TYPE.DAY2] = { self._btnDay21, self._btnDay22 }
    self._dayNodes[IMTeaHouseRankView.DAY_TYPE.DAY7] = { self._btnDay71, self._btnDay72 }

    self:updateTab(true)
    if self._panelGameList then
        self._panelGameList:addTouchEventListener(function(send, eventType)
            self:setGameListState(true)
        end)
    end
    if self._panelGameListMask then
        self._panelGameListMask:addTouchEventListener(function(send, eventType)
            self:setGameListState(false)
        end)
    end
    self:initGameList()

    -- if TeahouseManager.configManager.SetupConfig.IsOpenRankDateOptional then
        self._panelFixedType:setVisible(false)
        self._panelOptionalType:setVisible(true)
    -- else
        -- self._panelFixedType:setVisible(true)
        -- self._panelOptionalType:setVisible(false)
    -- end
end

function IMTeaHouseRankView:updateTab(isForbidReq)
    -- local teahouseData = TeahouseManager.teahouseData
    local rankIndex = 1
    -- local SwitchConfig = TeahouseManager.configManager.SwitchConfig
    -- if SwitchConfig and SwitchConfig.CostRank ~= 0 and teahouseData:checkRankBit(teahouseData.RANK_BIT.DAY_COST) then
        local nodeRank = self["_nodeRank" .. rankIndex]
        -- rankIndex = rankIndex + 1
        if self._panelCost and nodeRank then
            self._panelCost:setVisible(true)
            self._panelCost:setPosition(cc.p(nodeRank:getPosition()))
        end
        self._rankList[IMTeaHouseRankView.TAB_TYPE.COST] = 1--SwitchConfig.CostRank
    -- else
    --     if self._panelCost then
    --         self._panelCost:setVisible(false)
    --     end
    --     self._rankList[IMTeaHouseRankView.TAB_TYPE.COST] = IMTeaHouseRankView.RANK_TYPE.CLOSE
    -- end
    -- if SwitchConfig and SwitchConfig.PlayCountRank ~= 0 and teahouseData:checkRankBit(teahouseData.RANK_BIT.DAY_PLAY_COUNT) then
        -- local nodeRank = self["_nodeRank" .. rankIndex]
        -- rankIndex = rankIndex + 1
        -- if self._panelPlayCount and nodeRank then
        --     self._panelPlayCount:setVisible(true)
        --     self._panelPlayCount:setPosition(cc.p(nodeRank:getPosition()))
        -- end
        -- self._rankList[IMTeaHouseRankView.TAB_TYPE.PLAY_COUNT] = 2--SwitchConfig.PlayCountRank
    -- else
    --     if self._panelPlayCount then
    --         self._panelPlayCount:setVisible(false)
    --     end
    --     self._rankList[IMTeaHouseRankView.TAB_TYPE.PLAY_COUNT] = IMTeaHouseRankView.RANK_TYPE.CLOSE
    -- end
    -- if SwitchConfig and SwitchConfig.BigWinnerRank ~= 0 and teahouseData:checkRankBit(teahouseData.RANK_BIT.DAY_BIG_WINNER) then
        --冠军排行榜开启这段
        -- nodeRank = self["_nodeRank" .. rankIndex]
        -- rankIndex = rankIndex + 1
        -- if self._panelBigWinner and nodeRank then
        --     self._panelBigWinner:setVisible(true)
        --     self._panelBigWinner:setPosition(cc.p(nodeRank:getPosition()))
        -- end
        -- self._rankList[IMTeaHouseRankView.TAB_TYPE.BIG_WINNER] = 1--SwitchConfig.BigWinnerRank
        --冠军排行榜开启这段
    -- else
    --     if self._panelBigWinner then
    --         self._panelBigWinner:setVisible(false)
    --     end
    --     self._rankList[IMTeaHouseRankView.TAB_TYPE.BIG_WINNER] = IMTeaHouseRankView.RANK_TYPE.CLOSE
    -- end
    -- if SwitchConfig and SwitchConfig.ScoreRank ~= 0 and teahouseData:checkRankBit(teahouseData.RANK_BIT.DAY_SCORE) then
        -- 优胜值排行榜开启这段
        -- nodeRank = self["_nodeRank" .. rankIndex]
        -- rankIndex = rankIndex + 1
        -- if self._panelScore and nodeRank then
        --     self._panelScore:setVisible(true)
        --     self._panelScore:setPosition(cc.p(nodeRank:getPosition()))
        -- end
        -- self._rankList[IMTeaHouseRankView.TAB_TYPE.SCORE] = 2--SwitchConfig.ScoreRank
        -- 优胜值排行榜开启这段
    -- else
    --     if self._panelScore then
    --         self._panelScore:setVisible(false)
    --     end
    --     self._rankList[IMTeaHouseRankView.TAB_TYPE.SCORE] = IMTeaHouseRankView.RANK_TYPE.CLOSE
    -- end
    -- if SwitchConfig and SwitchConfig.WinLoseRank ~= 0 and teahouseData:checkSelfPower("ModifyBill") then
    --     local nodeRank = self["_nodeRank" .. rankIndex]
    --     rankIndex = rankIndex + 1
    --     if self._panelWinLose and nodeRank then
    --         self._panelWinLose:setVisible(true)
    --         self._panelWinLose:setPosition(cc.p(nodeRank:getPosition()))
    --     end
    --     self._rankList[IMTeaHouseRankView.TAB_TYPE.WIN_LOSE] = SwitchConfig.WinLoseRank
    -- else
    --     if self._panelWinLose then
    --         self._panelWinLose:setVisible(false)
    --     end
    --     self._rankList[IMTeaHouseRankView.TAB_TYPE.WIN_LOSE] = IMTeaHouseRankView.RANK_TYPE.CLOSE
    -- end
    -- if self._btnRankSetting then
    --     self._btnRankSetting:setVisible(SwitchConfig.RankSetting ~= 0 and teahouseData:checkSelfPower("ModifyRankSetting"))
    --     if teahouseData:isAdmin() then
    --         self._btnRankSetting:setVisible(SwitchConfig.RankSetting ~= 0 and teahouseData:checkSelfPower("ModifyRankSetting") and teahouseData:checkSelfAdminRight(teahouseData.ADMIN_RIGHT.CHECKRANK))
    --     end
    -- end
    local tabType = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_RANK_LIST_TAB_TYPE_RECORD, IMTeaHouseRankView.TAB_TYPE.COST)
    self:setTabType(tabType, isForbidReq)
end

function IMTeaHouseRankView:adaptUI()
    if not self._panelRoot then
        return
    end
    local notchWidth = XH.UITool.getNotchWidth()
    local rootSize = self._panelRoot:getContentSize()
    self._panelRoot:setContentSize(cc.size(rootSize.width - notchWidth * 2, rootSize.height))
    ccui.Helper:doLayout(self._panelRoot)
end

function IMTeaHouseRankView:refreshTableView(isCleanup)
    if not self._panelListInfo then
        return
    end
    if self._tvInfoList ~= nil and isCleanup then
        self._tvInfoList:removeFromParent()
        self._tvInfoList = nil
    end
    if self._tvInfoList == nil then
        self._tvInfoList = cc.TableView:create(self._panelListInfo:getContentSize())
        self._tvInfoList:setDirection(cc.SCROLLVIEW_DIRECTION_VERTICAL)
        self._tvInfoList:setVerticalFillOrder(cc.TABLEVIEW_FILL_TOPDOWN)
        self._tvInfoList:setIgnoreAnchorPointForPosition(false)
        self._tvInfoList:setAnchorPoint(cc.p(0, 0))
        self._tvInfoList:setPosition(cc.p(0, 0))
        self._panelListInfo:addChild(self._tvInfoList)
        self._tvInfoList:registerScriptHandler(handler(self, self.onTableCellTouched), cc.TABLECELL_TOUCHED)
        self._tvInfoList:registerScriptHandler(handler(self, self.onCellSizeForTable), cc.TABLECELL_SIZE_FOR_INDEX)
        self._tvInfoList:registerScriptHandler(handler(self, self.onTableCellAtIndex), cc.TABLECELL_SIZE_AT_INDEX)
        self._tvInfoList:registerScriptHandler(handler(self, self.onNumberOfCellsInTableView), cc.NUMBER_OF_CELLS_IN_TABLEVIEW)
    end
    if self._tvInfoList ~= nil then
        self._tvInfoList:reloadData()
    end
end

function IMTeaHouseRankView:onTableCellTouched(tableView, cell)

end

function IMTeaHouseRankView:onCellSizeForTable(tableView, index)
    if self._panelListItem == nil then
        return 0, 0
    end
    local size = self._panelListItem:getContentSize()
    return size.width, size.height
end

function IMTeaHouseRankView:onTableCellAtIndex(tableView, index)
    local cell = tableView:dequeueCell()
    if not cell then
        cell = cc.TableViewCell:new()
        local infoItem
        if self._tabType == IMTeaHouseRankView.TAB_TYPE.WIN_LOSE then
            infoItem = XH.viewManager:createView("TeaHouseRankListItem2","src.lobby.Modules.IMTeaHouse.TeaHouseRankListItem2")
        else
            infoItem = XH.viewManager:createView("TeaHouseRankListItem","src.lobby.Modules.IMTeaHouse.IMTeaHouseRankListItem")
        end
        if infoItem ~= nil then
            infoItem:setAnchorPoint(cc.p(0, 0))
            infoItem:setPosition(0, 0)
            infoItem:setName("InfoItem")
            if self._panelListItem then
                infoItem:setContentSize(self._panelListItem:getContentSize())
                ccui.Helper:doLayout(infoItem)
            end
            cell:addChild(infoItem)
        end
    end
    local infoItem = cell:getChildByName("InfoItem")
    if infoItem ~= nil then
        if self._tabType == IMTeaHouseRankView.TAB_TYPE.WIN_LOSE then
            infoItem:updateInfo(self._tabType, self._infoList[index + 1], index + 1)
        else
            self._infoList[index + 1].nScore = self:changeCostCard(self._tabType,self._infoList[index + 1].nScore)
            infoItem:updateInfoEx(self._tabType, self._infoList[index + 1])
        end
    end
    return cell
end

function IMTeaHouseRankView:onNumberOfCellsInTableView(tableView)
    return #self._infoList
end

function IMTeaHouseRankView:onCloseClicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    self:close()
end

function IMTeaHouseRankView:onCost1Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
     
    self:setTabType(IMTeaHouseRankView.TAB_TYPE.COST)
end

function IMTeaHouseRankView:onCost2Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    
end

function IMTeaHouseRankView:onPlayCount1Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    self:setTabType(IMTeaHouseRankView.TAB_TYPE.PLAY_COUNT)
    
end

function IMTeaHouseRankView:onPlayCount2Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

end

function IMTeaHouseRankView:onBigWinner1Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    self:setTabType(IMTeaHouseRankView.TAB_TYPE.BIG_WINNER)
    
end

function IMTeaHouseRankView:onBigWinner2Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    
end

function IMTeaHouseRankView:onScore1Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    self:setTabType(IMTeaHouseRankView.TAB_TYPE.SCORE)

end

function IMTeaHouseRankView:onScore2Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
end

function IMTeaHouseRankView:onWinLose1Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    self:setTabType(IMTeaHouseRankView.TAB_TYPE.WIN_LOSE)
end

function IMTeaHouseRankView:onWinLose2Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
end

function IMTeaHouseRankView:onDay11Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    self:setDayType(IMTeaHouseRankView.DAY_TYPE.DAY1)
end

function IMTeaHouseRankView:onDay12Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
end

function IMTeaHouseRankView:onDay21Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    self:setDayType(IMTeaHouseRankView.DAY_TYPE.DAY2)
end

function IMTeaHouseRankView:onDay22Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
end

function IMTeaHouseRankView:onDay71Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    self:setDayType(IMTeaHouseRankView.DAY_TYPE.DAY7)
end

function IMTeaHouseRankView:onDay72Clicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
end

function IMTeaHouseRankView:setTabType(tabType, isForbidReq)
    local rankCount = #self._rankList
    if rankCount < 1 then
        return
    end
    for _ = 1, rankCount do
        if self._rankList[tabType] ~= IMTeaHouseRankView.RANK_TYPE.CLOSE then
            break
        else
            tabType = tabType + 1
            if tabType > rankCount then
                tabType = tabType % rankCount
            end
        end
    end
    if self._rankList[tabType] == IMTeaHouseRankView.RANK_TYPE.CLOSE then
        return
    end
    if self._tabType == tabType then
        return
    end

    for i = 1, #self._tabNodes do
        if i == self._tabType then
            self._tabNodes[i][1]:setVisible(true)
            self._tabNodes[i][2]:setVisible(false)
            break
        end
    end
    self._tabType = tabType
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_RANK_LIST_TAB_TYPE_RECORD, self._tabType)
    for i = 1, #self._tabNodes do
        if i == self._tabType then
            self._tabNodes[i][1]:setVisible(false)
            self._tabNodes[i][2]:setVisible(true)
            break
        end
    end

    if self._panelGameList then
        self._panelGameList:setVisible(false)--(self._rankList[tabType] == IMTeaHouseRankView.RANK_TYPE.FILTER)
    end

    self:setGameListState(false)
    local titleIndex = 1
    if self._tabType == IMTeaHouseRankView.TAB_TYPE.COST then
        if self._textRankDesc then
            self._textRankDesc:setString("消 耗")
        end
        if self._textSelfDescTip then
            self._textSelfDescTip:setString("我的消耗:")
            ccui.Helper:doLayout(self._textSelfDescTip)
        end
        if self._totalCosText then
            self._totalCosText:setString("分组总消耗:")
            ccui.Helper:doLayout(self._totalCosText)
        end

    elseif self._tabType == IMTeaHouseRankView.TAB_TYPE.PLAY_COUNT then
        if self._textRankDesc then
            self._textRankDesc:setString("局 数")
        end
        if self._textSelfDescTip then
            self._textSelfDescTip:setString("我的局数:")
            ccui.Helper:doLayout(self._textSelfDescTip)
        end
        if self._totalCosText then
            self._totalCosText:setString("分组总局数:")
            ccui.Helper:doLayout(self._totalCosText)
        end
    elseif self._tabType == IMTeaHouseRankView.TAB_TYPE.BIG_WINNER then
        if self._textRankDesc then
            self._textRankDesc:setString("冠军数")
        end
        if self._textSelfDescTip then
            self._textSelfDescTip:setString("我的冠军数:")
            ccui.Helper:doLayout(self._textSelfDescTip)
        end
        if self._totalCosText then
            self._totalCosText:setString("冠军总数:")
            ccui.Helper:doLayout(self._totalCosText)
        end
    elseif self._tabType == IMTeaHouseRankView.TAB_TYPE.SCORE then
        if self._textRankDesc then
            self._textRankDesc:setString("优胜值")
        end
        if self._textSelfDescTip then
            self._textSelfDescTip:setString("我的优胜值:")
            ccui.Helper:doLayout(self._textSelfDescTip)
        end
        if self._totalCosText then
            self._totalCosText:setString("分组总优胜值:")
            ccui.Helper:doLayout(self._totalCosText)
        end
    elseif self._tabType == IMTeaHouseRankView.TAB_TYPE.WIN_LOSE then
        titleIndex = 2
        local SwitchConfig = TeahouseManager.configManager.SwitchConfig
        if SwitchConfig and SwitchConfig.ScoreRankWithAll == 1 then
            self:setWinLoseTypeName({"优胜值", "局 数", "冠军"})
        else
            self:setWinLoseTypeName({"优胜值", "总胜分", "总负分"})
        end

        if self._textSelfDescTip then
            self._textSelfDescTip:setString("我的优胜值:")
            ccui.Helper:doLayout(self._textSelfDescTip)
        end
    end
    if self._panelListTitle then
        self._panelListTitle:setVisible(titleIndex == 1)
    end
    if self._panelListTitle2 then
        self._panelListTitle2:setVisible(titleIndex == 2)
    end
    if not isForbidReq then
        self:reqRankInfo()
    end
end

function IMTeaHouseRankView:setDayType(dayType,isForbidReq)
    if self._dayType == dayType then
        return
    end

    for i = 1, #self._dayNodes do
        if i == self._dayType then
            self._dayNodes[i][1]:setVisible(true)
            self._dayNodes[i][2]:setVisible(false)
            break
        end
    end
    self._dayType = dayType
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_RANK_LIST_DAY_TYPE_RECORD, self._dayType)
    for i = 1, #self._dayNodes do
        if i == self._dayType then
            self._dayNodes[i][1]:setVisible(false)
            self._dayNodes[i][2]:setVisible(true)
            break
        end
    end
    if not isForbidReq then
        self:reqRankInfo()
    end
end

function IMTeaHouseRankView:reqRankInfo()
    if self._tabType == nil then
        return
    end

    local startDay = self._startDay
    local endDay = self._endDay
    -- if not TeahouseManager.configManager.SetupConfig.IsOpenRankDateOptional then
        -- if self._dayType == IMTeaHouseRankView.DAY_TYPE.DAY2 then
        --     startDay = 1
        --     endDay = 1
        -- elseif self._dayType == IMTeaHouseRankView.DAY_TYPE.DAY7 then
        --     startDay = 0
        --     endDay = 6
        -- end
    -- else
        -- startDay = self._startDay
        -- endDay = self._endDay
    -- end
    
    if self._tabType == IMTeaHouseRankView.TAB_TYPE.COST then
        XH.lobby:getModule("Im"):reqCostRank(self._teaNum, startDay, endDay, self._gameID)
    -- elseif self._tabType == IMTeaHouseRankView.TAB_TYPE.PLAY_COUNT then
    --         teahouseRank:reqPlayCountRankEx(startDay, endDay, self._gameID)
    elseif self._tabType == IMTeaHouseRankView.TAB_TYPE.BIG_WINNER then
        XH.lobby:getModule("Im"):reqBigWinnerRank(self._teaNum,startDay, endDay, self._gameID)
    elseif self._tabType == IMTeaHouseRankView.TAB_TYPE.SCORE then
        XH.lobby:getModule("Im"):reqScoreRank(self._teaNum,startDay, endDay, self._gameID)
    -- elseif self._tabType == IMTeaHouseRankView.TAB_TYPE.WIN_LOSE then
    --     teahouseRank:reqWinLoseRank(startDay, endDay, self._gameID)
    end
end

function IMTeaHouseRankView:onCostRankUpdate(event)
    if self._tabType ~= IMTeaHouseRankView.TAB_TYPE.COST then
        return
    end

    self._infoList = event.msg.info
    self:updateSelfRank()
    self:refreshTableView(true)
end

function IMTeaHouseRankView:onPlayCountRankUpdate(event)
    if self._tabType ~= IMTeaHouseRankView.TAB_TYPE.PLAY_COUNT then
        return
    end

    self._infoList = event.msg.info
    self:updateSelfRank()
    self:refreshTableView(true)
end

function IMTeaHouseRankView:onBigWinnerRankUpdate(event)
    if self._tabType ~= IMTeaHouseRankView.TAB_TYPE.BIG_WINNER then
        return
    end

    self._infoList = event.msg.info
    self:updateSelfRank()
    self:refreshTableView(true)
end

function IMTeaHouseRankView:onScoreRankUpdate(event)
    if self._tabType ~= IMTeaHouseRankView.TAB_TYPE.SCORE then
        return
    end

    self._infoList = event.msg.info
    self:updateSelfRank()
    self:refreshTableView(true)
end

function IMTeaHouseRankView:onWinLoseRankUpdate(event)
    if self._tabType ~= IMTeaHouseRankView.TAB_TYPE.WIN_LOSE then
        return
    end

    self._infoList = event.msg.info
    table.sort(self._infoList, function(info1, info2)
        return info1.totalScore > info2.totalScore
    end)
    self:updateSelfRank()
    self:refreshTableView(true)
end

function IMTeaHouseRankView:onSelfCostRankUpdate(event)
    if self._tabType ~= IMTeaHouseRankView.TAB_TYPE.COST then
        return
    end
    if self._textSelfDesc then
        self._textSelfDesc:setString(tostring(self:changeCostCard(self._tabType,event.msg.info.nScore)))
    end
end

function IMTeaHouseRankView:onSelfPlayCountRankUpdate(event)
    if self._tabType ~= IMTeaHouseRankView.TAB_TYPE.PLAY_COUNT then
        return
    end
    if self._textSelfDesc then
        self._textSelfDesc:setString(tostring(event.msg.info.nScore))
    end
end

function IMTeaHouseRankView:onSelfBigWinnerRankUpdate(event)
    if self._tabType ~= IMTeaHouseRankView.TAB_TYPE.BIG_WINNER then
        return
    end
    if self._textSelfDesc then
        self._textSelfDesc:setString(tostring(event.msg.info.nScore))
    end
end

function IMTeaHouseRankView:onSelfScoreRankUpdate(event)
    if self._tabType ~= IMTeaHouseRankView.TAB_TYPE.SCORE then
        return
    end
    if self._textSelfDesc then
        self._textSelfDesc:setString(tostring(event.msg.info.nScore))
    end
end

function IMTeaHouseRankView:onRankVisibleUpdate(event)
    self:updateTab()
end

function IMTeaHouseRankView:updateSelfRank()
    if not self._textSelfRank then
        return
    end
    
    if self._tabType == IMTeaHouseRankView.TAB_TYPE.WIN_LOSE then
        for index, info in ipairs(self._infoList) do
            self._groupTotalCost:setString(tostring(self:changeCostCard(self._tabType,info.nAllScore)))
            if info.numid == XH.playerData:getNumberID() then
                self._textSelfRank:setString(tostring(index))
                self._textSelfDesc:setString(tostring(info.totalScore))                
                return
            end
        end
        self._textSelfRank:setString("未上榜")
        self._textSelfDesc:setString("0")
        self._groupTotalCost:setString(self._groupTotalCostNum)
    else
        -- if rankType == IMTeaHouseRankView.RANK_TYPE.FILTER then
            for _, info in ipairs(self._infoList) do
                self._groupTotalCost:setString(tostring(self:changeCostCard(self._tabType,info.nAllScore)))
                if info.stUser.nNumId == XH.playerData:getNumberID() then
                    self._textSelfRank:setString(tostring(info.nRank))
                    self._textSelfDesc:setString(tostring(self:changeCostCard(self._tabType,info.nScore)))                                                                                         
                    return
                end
            end
            self._textSelfRank:setString("未上榜")
            self._textSelfDesc:setString("0")
            self._groupTotalCost:setString(self._groupTotalCostNum)
        -- else
        --     for _, info in pairs(self._infoList) do
        --         if info.stUserInfo and info.stUserInfo.numid == XH.playerData:getNumberID() then
        --             self._textSelfRank:setString(tostring(info.nRank))
        --             self._groupTotalCost:setString(info.nAllScore)
        --             return
        --         end
        --     end
        --     self._textSelfRank:setString("未上榜")
        -- end
    end
end

function IMTeaHouseRankView:setGameListState(isShow)
    if self._imgGameListBg then
        self._imgGameListBg:setVisible(isShow)
    end
    if self._btnHideGameList then
        self._btnHideGameList:setVisible(isShow)
    end
    if self._btnShowGameList then
        self._btnShowGameList:setVisible(not isShow)
    end
    if self._panelGameListMask then
        if isShow and self._panelRoot and self._panelGameList then
            local rootPosition = self._panelRoot:convertToWorldSpace(cc.p(0, 0))
            rootPosition = self._panelGameList:convertToNodeSpace(rootPosition)
            self._panelGameListMask:setPosition(rootPosition)
            self._panelGameListMask:setContentSize(self._panelRoot:getContentSize())
        end
        self._panelGameListMask:setVisible(isShow)
    end
end

function IMTeaHouseRankView:initGameList()
    if not self._svGameList or not self._btnGame then
        return
    end
    local gameList = XH.areaData:getTeaHouseRoomGameList()
    local gameNameList = XH.areaData:getAreaGameNameList()
    if gameList and gameNameList then
        self._gameListSize = self._svGameList:getContentSize()
        self._gameItemSize = self._btnGame:getContentSize()
        local listHeight = self._gameItemSize.height * (#gameList + 1) + 30
        if listHeight < self._gameListSize.height then
            listHeight = self._gameListSize.height
            self._svGameList:setInnerContainerSize(self._gameListSize)
        elseif listHeight > self._gameListSize.height then
            self._svGameList:setInnerContainerSize(cc.size(self._gameListSize.width, listHeight))
        end
        self._gameItemOffset = cc.p(self._gameItemSize.width / 2, listHeight - self._gameItemSize.height / 2)
        self:addGameItem(0, "所有游戏")
        self:setGame(0, "所有游戏")
        for _, gameID in ipairs(gameList) do
            self:addGameItem(gameID, gameNameList[gameID])
        end
    end
end

function IMTeaHouseRankView:addGameItem(gameID, gameName)
    if not gameID or not gameName then
        return
    end
    if not self._svGameList or not self._btnGame then
        return
    end
    local btnGame = self._btnGame:clone()
    btnGame:setVisible(true)
    btnGame:setPosition(self._gameItemOffset)
    self._gameItemOffset.y = self._gameItemOffset.y - self._gameItemSize.height
    btnGame:addTouchEventListener(function(send, eventType)
        
        if eventType ~= ccui.TouchEventType.ended then
            return
        end
        self:setGame(gameID, gameName)
        self:setGameListState(false)
    end)
    local textGame = btnGame:getChildByName("_KW_TEXT_GAME")
    if textGame then
        textGame:setString(gameName)
    end
    self._svGameList:addChild(btnGame)
end

function IMTeaHouseRankView:setGame(gameID, gameName)
    if self._textGameSelect then
        self._textGameSelect:setString(gameName)
        self._textGameSelect:setFontSize(KW_MAX_NAME_FONT_SIZE)
        XH.UITool.adaptTextToWidth(self._textGameSelect, KW_MAX_NAME_WIDTH, KW_MAX_NAME_FONT_SIZE, KW_MIN_NAME_FONT_SIZE)
    end
    if self._gameID ~= gameID then
        self._gameID = gameID
        self:reqRankInfo()
    end
end

function IMTeaHouseRankView:onHideGameListClicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    self:setGameListState(false)
end

function IMTeaHouseRankView:onShowGameListClicked(send, eventType)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    self:setGameListState(true)
end

-- function IMTeaHouseRankView:onRankSettingClicked(send, eventType)
--     
--     if eventType ~= ccui.TouchEventType.ended then
--         return
--     end
--     TeahouseManager.viewManager:openView("TeaHouseRankItem.TeaHouseRankSetting")
-- end

function IMTeaHouseRankView:onCurDateUpdate(event)
    local date = event.msg.nTime
    self:initDate(date)
    self._textStartSearchDay:setString(os.date("%Y-%m-%d", date))
    self._textEndSearchDay:setString(os.date("%Y-%m-%d", date))
end

function IMTeaHouseRankView:initDate(date)
    local nCurTime = date
    for i = 1, KW_SHOW_BILL_DAY do
        local startBtnFilter = ccui.Helper:seekWidgetByName(self._panelStartDate, "_KW_DATE_INFO_" .. i)
        if startBtnFilter then
            startBtnFilter:addTouchEventListener(function(send, eventType)
                self:onTouchEventChangeDate(send, eventType, i)
            end)
            startBtnFilter:setTitleText(os.date("%Y-%m-%d", nCurTime - 86400 * (KW_SHOW_BILL_DAY - i)))
            startBtnFilter:setTag(KW_SHOW_BILL_DAY - i)
        end

        local endBtnFilter = ccui.Helper:seekWidgetByName(self._panelEndDate, "_KW_DATE_INFO_" .. i)
        if endBtnFilter then
            endBtnFilter:addTouchEventListener(function(send, eventType)
                self:onTouchEventChangeDate(send, eventType, i)
            end)
            endBtnFilter:setTitleText(os.date("%Y-%m-%d", nCurTime - 86400 * (KW_SHOW_BILL_DAY - i)))
            endBtnFilter:setTag(KW_SHOW_BILL_DAY - i)
        end
    end
    self._changeStartDate:addEventListener(handler(self,self.onTouchEventChangeBWCOSTDate))
    self._changeEndDate:addEventListener(handler(self,self.onTouchEventChangeBWCOSTDate))
end

function IMTeaHouseRankView:onTouchEventChangeBWCOSTDate(send, eventType)
    local bShow = eventType == ccui.CheckBoxEventType.selected

    -- 根据复选框选中状态判定是开始日期还是结束日期
    if send == self._changeStartDate then
        self._startFilterBg:setVisible(bShow)
        self._endFilterBg:setVisible(false)
        self._changeStartDate:setSelected(bShow)
        self._changeEndDate:setSelected(false)
    elseif send == self._changeEndDate then
        self._endFilterBg:setVisible(bShow)
        self._startFilterBg:setVisible(false)
        self._changeStartDate:setSelected(false)
        self._changeEndDate:setSelected(bShow)
    end
end

function IMTeaHouseRankView:onTouchEventChangeDate(send, eventType, index)
    
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    local curBillDate = send:getTitleText()
    if send:getParent() == self._startFilterBg then
        self:onTouchEventChangeBWCOSTDate(self._changeStartDate, ccui.CheckBoxEventType.unselected)
        self._textStartSearchDay:setString(curBillDate)
        self._endDay = send:getTag()
    else
        self:onTouchEventChangeBWCOSTDate(self._changeEndDate, ccui.CheckBoxEventType.unselected)
        self._textEndSearchDay:setString(curBillDate)
        self._startDay = send:getTag()
    end
end

function IMTeaHouseRankView:onSearchClicked(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if self._startDay > self._endDay then
        XH.TipTool.showToast("日期错误")
        return
    end
    self:reqRankInfo()
end

-- 设置胜负分排行榜显示内容
function IMTeaHouseRankView:setWinLoseTypeName(tValueType)
    if self._textRankDesc21 and tValueType[1] then
        self._textRankDesc21:setString(tValueType[1])
    end
    if self._textRankDesc22 and tValueType[2] then
        self._textRankDesc22:setString(tValueType[2])
    end
    if self._textRankDesc23 and tValueType[3] then
        self._textRankDesc23:setString(tValueType[3])
    end
end

--人人比赛场地区有小卡的,房间配置都是小卡,显示需要转成大卡
function IMTeaHouseRankView:changeCostCard(tabType,num)
    if tabType == IMTeaHouseRankView.TAB_TYPE.COST and num then
        num = tonumber(num) / (XH.areaData:getPropSmallRoomCardRatio() or 1)        
    end
    self._groupTotalCostNum = num
    return num
end

return IMTeaHouseRankView ��  