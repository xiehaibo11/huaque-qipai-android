local SxvipStatisticsItem = class("SxvipStatisticsItem" , XH.ViewBase)

local GameSub = require("app.Config.GameSub")

local Week_Static = {
    [1] = "本周" ,
    [2] = "前1周" ,
    [3] = "前2周" ,
    [4] = "前3周" ,
}

function SxvipStatisticsItem:getCSBPath()
    return "cocosStudio/hall/CSB/Sxvip/SxvipStatisticsItem.csb"
end

function SxvipStatisticsItem:getBindingInfo()
    return {
        ["_KW_VIP_ROOT"] = {varName = "_root"},
        ["_KWA_MID_PANEL"] = {varName = "_midPanel"},
        ["_KWA_LEFT_PANEL"] = {varName = "_leftPanel"},
        ["_KWA_LEFT_TOP_PANEL"] = {varName = "_leftTopPanel"},
        ["_KWA_TXT_MAIN_TIME"] = {varName = "_txtMainTime"},
        ["_KWA_TXT_MAIN_WIN_RATE"] = {varName = "_txtMainWinRate"},
        ["_KWA_TXT_FRIEND_XIANG"] = {varName = "_txtFriendXiang"},
        ["_KWA_TXT_FRIEND_QIANG"] = {varName = "_txtFriendQiang"},
        ["_KWA_LISTVIEW_GAMEPLAY"] = {varName = "_listViewGameplay"},
        ["_KWA_LISTVIEW_GAMEPLAY_BG"] = {varName = "_listViewGameplayBg"},
        ["_KWA_BTN_GAMEPLAY"] = {varName = "_btnGameplay" ,  type = XH.UI_TYPE.BUTTON ,  onTouchEnded = "ON_KWA_BTN_GAMEPLAY" },
        ["_KWA_ITEM_GAMEPLAY"] = {varName = "_itemGameplay"},
        ["_KWA_PANEL_DAY_RATE"] = {varName = "_panelDayRate"},
        ["_KWA_PANEL_WEEK_RATE"] = {varName = "_panelWeekRate"},
        ["_KWA_TXT_DAY_7"] = {varName = "_txtDay7"},
        ["_KWA_TXT_DAY_6"] = {varName = "_txtDay6"},
        ["_KWA_TXT_DAY_5"] = {varName = "_txtDay5"},
        ["_KWA_TXT_DAY_4"] = {varName = "_txtDay4"},
        ["_KWA_TXT_DAY_3"] = {varName = "_txtDay3"},
        ["_KWA_TXT_DAY_2"] = {varName = "_txtDay2"},
        ["_KWA_TXT_DAY_1"] = {varName = "_txtDay1"},
        ["_KWA_ITEM_DAY_RATE"] = {varName = "_itemDayRate"},
        ["_KWA_ITEM_WEEK_RATE"] = {varName = "_itemWeekRate"},
        ["_KWA_TXT_WEEK_4"] = {varName = "_txtWeek4"},
        ["_KWA_TXT_WEEK_3"] = {varName = "_txtWeek3"},
        ["_KWA_TXT_WEEK_2"] = {varName = "_txtWeek2"},
        ["_KWA_TXT_WEEK_1"] = {varName = "_txtWeek1"},
        ["_KW_TOUCH_LAYER"] =  {varName = "_touchLayer" , type = XH.UI_TYPE.LAYER ,  onTouchEnded = "ON_KW_TOUCH_LAYER"},
    }
end

function SxvipStatisticsItem:getProxyEvents()
    return {
       { module = XH.lobby:getModule("Sxvip"), eventKeyName = "SXVIP_EVENT_STATISTICS_UPDATE_ALL", callBack = "onStatisticsUpdateAll" },
       { module = XH.lobby:getModule("Sxvip"), eventKeyName = "SXVIP_EVENT_STATISTICS_UPDATE_SUB", callBack = "onStatisticsUpdateSub" },
    }
end

function SxvipStatisticsItem:ctor()
    SxvipStatisticsItem.super.ctor(self)
    
    self._gameIds = {}

    self._statisticsData = {}

    self._selectGameId = 0

    self._gamePlayOpen = false

    self:initView()

    -- 无数据则请求
    if not XH.lobby:getModule("Sxvip"):getStatisticsDataByGameid(self._selectGameId) then
        XH.lobby:getModule("Sxvip"):reqGameplayStatisticsAll()
    else
        self:onStatisticsUpdateAll()
    end
end

function SxvipStatisticsItem:initView()
    local size = self._root:getContentSize()
    self:setContentSize(size)

    self._listViewGameplay:setScrollBarEnabled(false)
end

function SxvipStatisticsItem:updateAllView()
    self:updateNoramlStatic()
    self:updateView()
end

function SxvipStatisticsItem:updateNoramlStatic()
    -- 玩法
    self._listViewGameplay:removeAllItems()
    for _, v in ipairs(self._gameIds) do
        local item = self._itemGameplay:clone()
        local nameTxt = self:getNameByGameId(v)
        item:getChildByName("_KWA_ITEM_GAMEPLAY_NAME"):setString(nameTxt)
        item.customData = v
        self._listViewGameplay:pushBackCustomItem(item)
        item:addTouchEventListener(handler(self , self.gamePlayItemClick))
    end
    -- 主时间
    self._txtMainTime:setString(self._statisticsData.mainTime or "")
end

function SxvipStatisticsItem:gamePlayItemClick(sender , eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if sender.customData then
        self._selectGameId = sender.customData
        self._gamePlayOpen = false
        self:updateGameplayList()
        if not XH.lobby:getModule("Sxvip"):getStatisticsDataByGameid(self._selectGameId) then
            XH.lobby:getModule("Sxvip"):reqGameplayStatisticsSub(sender.customData)
        else
            self:onStatisticsUpdateSub()
        end
    end
end

function SxvipStatisticsItem:getNameByGameId(id)
    if id == 0 then
        return "所有玩法"
    end
    local gameInfo = GameSub.GameSubByConfID[id]
    return gameInfo and gameInfo.GameName or "未知玩法"
end

function SxvipStatisticsItem:updateView()
    self:updateTopRightPanel()
    self:updateGameInfo()
    self:updateDayInfo()
    self:updateWeekInfo()
end

function SxvipStatisticsItem:updateTopRightPanel()
    self._gamePlayOpen = false
    -- 更新按钮文字
    self._btnGameplay:getChildByName("_KWA_TXT_GAMEPLAY"):setString(self:getNameByGameId(self._selectGameId))
    -- 更新状态按钮上得三角形状态
    self._btnGameplay:getChildByName("_KWA_IMG_MARK"):setRotation(0)
    -- 隐藏列表
    self._listViewGameplay:setVisible(self._gamePlayOpen)
    self._listViewGameplayBg:setVisible(self._gamePlayOpen)
end

function SxvipStatisticsItem:updateGameInfo()
    if XH.lobby:getModule("Sxvip"):isExpire() then
        -- 胜率
        self._txtMainWinRate:setString("*%")
        -- 最香牌友
        self._txtFriendXiang:setString("**")
        -- 最强牌友
        self._txtFriendQiang:setString("**")
    else
        -- 胜率
        self._txtMainWinRate:setString(self._statisticsData.winRate)
        -- 最香牌友
        local lostUser = self._statisticsData.lostUser
        if string.len(lostUser) > 12 then
            local validBytes = XH.StringTool.getMinimumValidBytesUTF8(lostUser, 12)
            lostUser = string.sub(lostUser,1,validBytes) .. "..."
        end
        self._txtFriendXiang:setString(lostUser)
        -- 最强牌友
        local winUser = self._statisticsData.winUser
        if string.len(winUser) > 12 then
            local validBytes = XH.StringTool.getMinimumValidBytesUTF8(winUser, 12)
            winUser = string.sub(winUser,1,validBytes) .. "..."
        end
        self._txtFriendQiang:setString(winUser)
    end
end

function SxvipStatisticsItem:updateDayInfo()
    local dayInfo = self._statisticsData.dayReport
    if not dayInfo then return end
    self._panelDayRate:removeAllChildren()
    local size = self._panelDayRate:getContentSize()
    -- 创建item
    for i = 1, 7 do
        local dateTxt = self["_txtDay" .. i]
        local data = dayInfo[i]
        if dateTxt and data then
            dateTxt:setString(data.date)
            local dayItem = self._itemDayRate:clone()
            local rateTxt = XH.UITool.seekNodeByName(dayItem , "_KWA_TEXT_DAY_RATE")
            local rateKuang = XH.UITool.seekNodeByName(dayItem , "_KWA_IMG_BUBBLE")
            if rateTxt and rateKuang then
                rateTxt:setString(data.winRate)
                local kuangSize = rateKuang:getContentSize()
                rateKuang:setContentSize(rateTxt:getContentSize().width + 10 , kuangSize.height)
            end
            self._panelDayRate:addChild(dayItem)
            dayItem:setName("dayItem" .. i)
            dayItem:setPosition(dateTxt:getPositionX() , data.totalCount == 0 and 0 or size.height * data.winCount / data.totalCount)
            local lastItem = self._panelDayRate:getChildByName("dayItem" .. (i - 1))
            if lastItem then
                 -- 创建 DrawNode
                local drawNode = cc.DrawNode:create()
                self._panelDayRate:addChild(drawNode , -1)
                -- 获取容器的位置
                local container1Pos = cc.p(lastItem:getPositionX(), lastItem:getPositionY())
                local container2Pos = cc.p(dayItem:getPositionX(), dayItem:getPositionY())
                -- 设置直线颜色为红色
                local lineColor = cc.c4f(231/255, 56/255, 61/255, 1)
                -- 画直线连接两个容器，并设置颜色
                drawNode:drawSegment(container1Pos, container2Pos, 2, lineColor)
            end
        end
    end
end


function SxvipStatisticsItem:updateWeekInfo()
    local weekInfo = self._statisticsData.weekReport
    if not weekInfo then return end
    self._panelWeekRate:removeAllChildren()
    local size = self._panelWeekRate:getContentSize()
    for i = 1, 4 do
        local dateTxt = self["_txtWeek" .. i]
        local data = weekInfo[i]
        if dateTxt and data then
            dateTxt:setString(Week_Static[data.weekIndex])
            local weekItem = self._itemWeekRate:clone()
            local rateTxt = XH.UITool.seekNodeByName(weekItem , "_KWA_TEXT_WEEK_RATE")
            local rateKuang = XH.UITool.seekNodeByName(weekItem , "_KWA_IMG_BUBBLE")
            if rateTxt and rateKuang then
                rateTxt:setString(data.winRate)
                local kuangSize = rateKuang:getContentSize()
                rateKuang:setContentSize(rateTxt:getContentSize().width + 10 , kuangSize.height)
            end
            self._panelWeekRate:addChild(weekItem)
            weekItem:setName("weekItem" .. i)
            weekItem:setPosition(dateTxt:getPositionX() , data.totalCount == 0 and 0 or size.height * data.winCount / data.totalCount)
            -- 划线
            local lastItem = self._panelWeekRate:getChildByName("weekItem" .. (i - 1))
            if lastItem then
                 -- 创建 DrawNode
                 local drawNode = cc.DrawNode:create()
                 self._panelWeekRate:addChild(drawNode , -1)
                 -- 获取容器的位置
                 local container1Pos = cc.p(lastItem:getPositionX(), lastItem:getPositionY())
                 local container2Pos = cc.p(weekItem:getPositionX(), weekItem:getPositionY())
                 -- 设置直线颜色为蓝色
                 local lineColor = cc.c4f(72/255, 111/255, 188/255, 1)
                 -- 画直线连接两个容器，并设置颜色
                 drawNode:drawSegment(container1Pos, container2Pos, 2, lineColor)
            end
        end
    end
end

-- do nothing
function SxvipStatisticsItem:refreshView()

end

function SxvipStatisticsItem:onStatisticsUpdateAll(event)
    local data = XH.lobby:getModule("Sxvip"):getStatisticsDataByGameid(self._selectGameId)
    if data then
        self._gameIds = data.gameIds or {}
        if not table.indexof(self._gameIds,0) then
            table.insert(self._gameIds, 1, 0) --全部
        end
        self._statisticsData = data
        self:updateAllView()
    end
end

function SxvipStatisticsItem:onStatisticsUpdateSub(event)
    local data = XH.lobby:getModule("Sxvip"):getStatisticsDataByGameid(self._selectGameId)
    if data then
        self._statisticsData = data
        self:updateView()
    end
end

function SxvipStatisticsItem:ON_KWA_BTN_GAMEPLAY(sender , eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._gamePlayOpen = not self._gamePlayOpen
    self:updateGameplayList()
end

function SxvipStatisticsItem:ON_KW_TOUCH_LAYER(sender , eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._gamePlayOpen = false
    self:updateGameplayList()
end

function SxvipStatisticsItem:updateGameplayList()
    self._listViewGameplay:setVisible(self._gamePlayOpen)
    self._listViewGameplayBg:setVisible(self._gamePlayOpen)
end

return SxvipStatisticsItem   I.  