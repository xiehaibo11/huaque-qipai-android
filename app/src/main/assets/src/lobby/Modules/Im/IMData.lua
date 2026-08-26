local IMData = class("IMData")
local IMProtocol = require("app.Protocols.IMProtocol")

IMData.EVENT_LIST_DATA_CHANGED = "EVENT_LIST_DATA_CHANGED"
IMData.EVENT_INVITE_DATA_CHANGED = "EVENT_INVITE_DATA_CHANGED"
IMData.EVENT_SHOW_POINT = "EVENT_SHOW_POINT"
IMData.EVENT_IM_UPDATE_SEER = "EVENT_IM_UPDATE_SEER"

IMData.KW_LIST_TYPE = {
    NONE = -1,
    PLAYERLIST = 0,
    TABLELIST = 1
}

IMData.TABLEDATATYPE = {    -- 即将开始桌子类型1.比赛场未展开 2.比赛场展开 3.普通桌子
    NORMAL = 1,
    NORMALOPEN = 2,
    TEANOTOPEN = 3,
    TEAOPEN = 4
}

IMData.GROUP_TYPE = {
    ALL = 0, -- 我的牌友
    GROUP = 1,   -- 我的分组
    TEAHOSE =2,
    BOX = 3
}

function IMData:ctor()
    cc(self):addComponent("components.behavior.EventProtocol"):exportMethods()
    -- IM服务的appid列表
    self._svrAppidList = {}
    -- IM期望指定的appid
    self._proposalAppid = 0
    -- 玩家列表信息
    self._friendListInfo = {}
    -- 桌子列表信息
    self._tableListInfo = {}
    -- 被邀请信息
    self._inviteList = {}
    -- 当前页签
    self._lable = -1
    -- 历史牌友信息
    self._historyFriends = {}
    -- 显示搜索好友的最小玩家数
    self._threshold = 0
    -- 屏蔽好友信息
    self._shieldListInfo = {}
    -- 有奖召回领取状态
    self._bRecallAwardState = false
    -- 玩家的好友申请
    self._applyNum = 0
    -- 玩家的分组列表
    self._groupList = {}
    self._teahouseList = {}
    self._enterTeahouseID = 0

    -- 好友列表等待中的玩家
    self._friendListWaiting = {}
    self._isSeeing = false
    self._watchRoomId = 0
end

function IMData:setSvrAppidList(list)
    self._svrAppidList = list
end

function IMData:getSvrAppidList()
    return self._svrAppidList or {}
end

function IMData:setProposalAppid(appid)
    self._proposalAppid = appid
end

function IMData:getProposalAppid()
    return self._proposalAppid or 0
end

function IMData:addToFriendList(infoTable)
    if not infoTable or type(infoTable) ~= "table" then
        return
    end
    
    for _, v in pairs(infoTable) do
        local bFind = false
        for i = 1, #self._friendListInfo do
            if v.numid == self._friendListInfo[i].numid then
                self._friendListInfo[i] = clone(v)
                bFind = true
                break
            end
        end
        if not bFind then
            table.insert(self._friendListInfo, v)
        end

        -- 等待中玩家列表
        local bFindWaiting = false
        for i = 1, #self._friendListWaiting do
            if self._friendListWaiting[i].numid == v.numid then
                if v.player_state == IMProtocol.PLAYERSTATE.PS_WAIT and 
                    v.user_count and v.chair_count and v.user_count > 0 and v.chair_count > 0 and v.chair_count > v.user_count then
                    self._friendListWaiting[i] = clone(v)
                else
                    table.remove(self._friendListWaiting, i)
                end
                bFindWaiting = true
                break
            end
        end
        if not bFindWaiting and v.player_state == IMProtocol.PLAYERSTATE.PS_WAIT and 
            v.user_count and v.chair_count and v.user_count > 0 and v.chair_count > 0 and v.chair_count > v.user_count then
            table.insert(self._friendListWaiting, v)
        end
    end
    self:sortFriendList()
    self:dispatch(self.EVENT_LIST_DATA_CHANGED)
end

function IMData:addToFriendListForGroup(infoTable)
    if not infoTable or type(infoTable) ~= "table" then
        return
    end
    for _, v in pairs(infoTable) do
        local bFind = false
        for i = 1, #self._friendListInfo do
            if v.numid == self._friendListInfo[i].numid then
                self._friendListInfo[i] = clone(v)
                bFind = true
                break
            end
        end
        if not bFind then
            table.insert(self._friendListInfo, v)
        end
    end
    self:dispatch(self.EVENT_LIST_DATA_CHANGED)
end

function IMData:refreshDealFriendList(data,dealType)
    if not data or type(data) ~= "table" then
        return
    end
    if dealType then
        table.insert(self._friendListInfo, data)
    else
        for i, v in pairs(self._friendListInfo) do
            if v.numid == data.numid then
                table.remove(self._friendListInfo, i)
                break
            end
        end
    end
    self:dispatch(self.EVENT_LIST_DATA_CHANGED)
end

function IMData:updatePlayerInfo(info)
    if self._friendListInfo and info then
        local bFind = false
        for i = 1, #self._friendListInfo do
            if self._friendListInfo[i].numid == info.numid then
                self._friendListInfo[i] = clone(info)
                bFind = true
                break
            end
        end
        if not bFind then
            table.insert(self._friendListInfo, info)
        end

        -- 等待玩家列表
        local bFindWaiting = false
        for i = 1, #self._friendListWaiting do
            if self._friendListWaiting[i].numid == info.numid then
                if info.player_state == IMProtocol.PLAYERSTATE.PS_WAIT and 
                    info.user_count and info.chair_count and info.user_count > 0 and info.chair_count > 0 and info.chair_count > info.user_count then
                    self._friendListWaiting[i] = clone(info)
                else
                    table.remove(self._friendListWaiting, i)
                end
                bFindWaiting = true
                break
            end
        end
        if not bFindWaiting and info.player_state == IMProtocol.PLAYERSTATE.PS_WAIT and 
            info.user_count and info.chair_count and info.user_count > 0 and info.chair_count > 0 and info.chair_count > info.user_count then
            table.insert(self._friendListWaiting, info)
        end
        
        self:sortFriendList()
        self:dispatch(self.EVENT_LIST_DATA_CHANGED)
    end
end

function IMData:getFriendList()
    return self._friendListInfo or {}
end

-- 获取等待中状态好友
function IMData:getFriendListWaiting()
    return self._friendListWaiting or {}
end

function IMData:clearFriendList()
    self._friendListInfo = {}
    self._shieldListInfo = {}
    self._friendListWaiting = {}
end

function IMData:sortFriendList()
    table.sort(self._friendListInfo, function(a, b)
        if a.player_state and b.player_state and a.last_login_time and b.last_login_time then
            if a.player_state ~= b.player_state then
                return a.player_state > b.player_state
            else
                return a.last_login_time > b.last_login_time
            end
        end
    end)
end

----------------------------------------------------
function IMData:addToTableList(infoTable)
    if not infoTable or type(infoTable) ~= "table" then
        return
    end
    if next(infoTable) == nil then
        self:dispatch(self.EVENT_LIST_DATA_CHANGED)
        return
    end
    local index = 0
    for k, v in pairs(self._tableListInfo) do
        if v.tableType and (v.tableType == self.TABLEDATATYPE.NORMAL or v.tableType == self.TABLEDATATYPE.NORMALOPEN) then
            index = k
            break
        end
    end
    if index == 0 then
        index = #self._tableListInfo + 1
        self._tableListInfo[index] = {}
        self._tableListInfo[index].tableType = self.TABLEDATATYPE.NORMALOPEN
        self._tableListInfo[index].tables = {}
    end
    for _, v in pairs(infoTable) do
        local bFind = false
        for i = 1, #self._tableListInfo[index].tables do
            if v.game_roomid == self._tableListInfo[index].tables[i].game_roomid then
                self._tableListInfo[index].tables[i] = clone(v)
                bFind = true
                break
            end
        end
        if not bFind then
            table.insert(self._tableListInfo[index].tables, v)
        end
    end
    self:dispatch(self.EVENT_LIST_DATA_CHANGED)
end

function IMData:getTableList()
    return self._tableListInfo or {}
end

function IMData:clearTableList()
    self._tableListInfo = {}
end

function IMData:clearNormalTable()
    for i = #self._tableListInfo, 1, -1 do
        if self._tableListInfo[i].tableType and (self._tableListInfo[i].tableType == self.TABLEDATATYPE.NORMAL or self._tableListInfo[i].tableType == self.TABLEDATATYPE.NORMALOPEN) then
            table.remove(self._tableListInfo, i)
        end
    end
end

function IMData:clearTeaList()
    for i = #self._tableListInfo, 1, -1 do
        if self._tableListInfo[i].tableType and (self._tableListInfo[i].tableType == self.TABLEDATATYPE.TEAOPEN or self._tableListInfo[i].tableType == self.TABLEDATATYPE.TEANOTOPEN) then
            table.remove(self._tableListInfo, i)
        end
    end
end

function IMData:sortTableList()
end

function IMData:getTableType(idx)
    if not idx then
        return
    end
    return self._tableListInfo[idx + 1].tableType
end

function IMData:getTableTeaId(idx)
    if not idx then
        return
    end
    return self._tableListInfo[idx + 1].teaid
end

function IMData:updateTableType(idx)
    if not idx then
        return
    end
    if self._tableListInfo[idx + 1] then
        if self._tableListInfo[idx + 1].tableType == self.TABLEDATATYPE.TEANOTOPEN then
            self._tableListInfo[idx + 1].tableType = self.TABLEDATATYPE.TEAOPEN
        elseif self._tableListInfo[idx + 1].tableType == self.TABLEDATATYPE.TEAOPEN then
            self._tableListInfo[idx + 1].tableType = self.TABLEDATATYPE.TEANOTOPEN
        elseif self._tableListInfo[idx + 1].tableType == self.TABLEDATATYPE.NORMAL then
            self._tableListInfo[idx + 1].tableType = self.TABLEDATATYPE.NORMALOPEN
        elseif self._tableListInfo[idx + 1].tableType == self.TABLEDATATYPE.NORMALOPEN then
            self._tableListInfo[idx + 1].tableType = self.TABLEDATATYPE.NORMAL
        end
        self:dispatch(self.EVENT_LIST_DATA_CHANGED)
    end
end

function IMData:addTeaInfoToTableList(teaInfo)
    -- 先剔除已经没有桌子的比赛场
    for i = #self._tableListInfo, 1, -1 do
        if self._tableListInfo[i].tableType and (self._tableListInfo[i].tableType == self.TABLEDATATYPE.TEAOPEN or self._tableListInfo[i].tableType == self.TABLEDATATYPE.TEANOTOPEN) then
            local bExist = false
            for j = 1, #teaInfo do
                if teaInfo[j].teaid and self._tableListInfo[i].teaid and teaInfo[j].teaid == self._tableListInfo[i].teaid then
                    bExist = true
                    break
                end
            end
            if not bExist then
                table.remove(self._tableListInfo, i)
            end
        end
    end
    -- 将请求到的数据加入桌子列表
    for i = 1, #teaInfo do
        local bExist = false
        for _, v in pairs(self._tableListInfo) do
            if v.teaid and v.teaid == teaInfo[i].teaid then
                local tmpTableType = clone(v.tableType)
                local tmpTables = clone(v.tables)
                local tmpCurPage = clone(v.curPage)
                local tmpTotalPage = clone(v.totalPage)
                v = clone(teaInfo[i])
                v.tableType = tmpTableType
                v.tables = tmpTables
                v.curPage = tmpCurPage
                v.totalPage = tmpTotalPage
                bExist = true
                break
            end
        end
        if not bExist then
            local tmp = clone(teaInfo[i])
            tmp.tableType = self.TABLEDATATYPE.TEANOTOPEN
            tmp.tables = {}
            table.insert(self._tableListInfo, tmp)
        end
    end
    self:dispatch(self.EVENT_LIST_DATA_CHANGED)
end

function IMData:clearTeaTableByTeaId(teaId)
    if not teaId then
        return
    end
    for _, v in pairs(self._tableListInfo) do
        if v.teaid and v.teaid == teaId then
            v.tables = {}
            break
        end
    end
end

function IMData:addTableToTea(teaid, tableInfo)
    if not teaid or not tableInfo or type(tableInfo) ~= "table" then
        return
    end
    
    for _, v in pairs(self._tableListInfo) do
        if v.teaid and v.teaid == teaid then
            for k = 1, #tableInfo do
                local bFind = false
                for i = 1, #v.tables do
                    if tableInfo[k].game_roomid == v.tables[i].game_roomid then
                        v.tables[i] = clone(tableInfo[k])
                        bFind = true
                        break
                    end
                end
                if not bFind then
                    table.insert(v.tables, tableInfo[k])
                end
            end
            break
        end
    end
    self:dispatch(self.EVENT_LIST_DATA_CHANGED)
end

function IMData:setTeaTableCurPageByTeaId(teaId, page)
    if not teaId then
        return
    end
    for _, v in pairs(self._tableListInfo) do
        if v.teaid and v.teaid == teaId then
            v.curPage = page
            break
        end
    end
end

function IMData:getTeaTableCurPageByTeaId(teaId)
    if not teaId then
        return
    end
    for _, v in pairs(self._tableListInfo) do
        if v.teaid and v.teaid == teaId then
            return v.curPage
        end
    end
end

function IMData:setTeaTableTotalPageByTeaId(teaId, page)
    if not teaId then
        return
    end
    for _, v in pairs(self._tableListInfo) do
        if v.teaid and v.teaid == teaId then
            v.totalPage = page
            break
        end
    end
end

function IMData:getTeaTableTotalPageByTeaId(teaId)
    if not teaId then
        return
    end
    for _, v in pairs(self._tableListInfo) do
        if v.teaid and v.teaid == teaId then
            return v.totalPage
        end
    end
end
----------------------------------------------------

function IMData:addToInviteList(infoTable, bJustNotify)
    if not infoTable or type(infoTable) ~= "table" then
        return
    end
    
    self:dispatchEvent( { name = self.EVENT_SHOW_POINT, msg = {bShow = next(infoTable) ~= nil} })
    if bJustNotify then
        return
    end
    
    for i = 1, #infoTable do
        local bFind = false
        for j = 1, #self._inviteList do
            if infoTable[i].fromNumid == self._inviteList[j].fromNumid and infoTable[i].msgType == self._inviteList[j].msgType then
                self._inviteList[j] = clone(infoTable[i])
                bFind = true
                break
            end
        end
        if not bFind then
            table.insert(self._inviteList, infoTable[i])
        end
    end
    self:sortInviteList()
    self:dispatch(self.EVENT_INVITE_DATA_CHANGED)
end

function IMData:getInviteList()
    return self._inviteList or {}
end

function IMData:clearInviteList()
    self._inviteList = {}
end

function IMData:removeMessageFromData(numid, msgType)
    local bChanged = false
    for i = #self._inviteList, 1, -1 do
        if self._inviteList[i].fromNumid == numid and self._inviteList[i].msgType == msgType then
            table.remove(self._inviteList, i)
            bChanged = true
            break
        end
    end
    if bChanged then
        self:dispatch(self.EVENT_INVITE_DATA_CHANGED, true)
    end
    if next(self._inviteList) ~= nil then
        self:dispatchEvent( { name = self.EVENT_SHOW_POINT, msg = {bShow = true} })
    else
        self:dispatchEvent( { name = self.EVENT_SHOW_POINT, msg = {bShow = false} })
        XH.lobby:getModule("Im"):dispatchEvent({name = XH.lobby:getModule("Im").EVENT_HIDE_MESSAGELAYER})
    end
end

function IMData:sortInviteList()
    table.sort(self._inviteList, function(a, b)
        return a.time > b.time
    end)
end

function IMData:dispatch(eventName, bShowTip)
    if bShowTip ~= nil then
        self:dispatchEvent( { name = eventName, data = bShowTip })
    else
        self:dispatchEvent( { name = eventName })
    end
end

function IMData:setLable(lable)
    self._lable = lable
end

function IMData:getLable()
    return self._lable or -1
end

function IMData:addHistoryFriendsInfo(info)
    if not info or type(info) ~= "table" then
        return
    end
    for i = 1, #info do
        table.insert(self._historyFriends, info[i])
    end
end

function IMData:getHistoryFriendsInfo()
    return self._historyFriends or {}
end

function IMData:clearHistoryFriendsInfo()
    self._historyFriends = {}
end

function IMData:setThesHold(data)
    self._threshold = data
end

function IMData:getThesHold()
    return self._threshold or 0
end

function IMData:setShieldState(numid, flag)
    if not numid or not flag then
        return
    end
    for i = 1, #self._friendListInfo do
        if numid == self._friendListInfo[i].numid then
            self._friendListInfo[i].shieldState = flag
            break
        end
    end
end

function IMData:setRecallAwardState(bGet)
    self._bRecallAwardState = bGet
end

function IMData:getRecallAwardState(bGet)
    return self._bRecallAwardState or false
end

function IMData:getApplyNum()
    return self._applyNum
end

function IMData:setApplyNum(num)
    self._applyNum = num
end

function IMData:getGroupList()
    return self._groupList
end

function IMData:setGroupList(group)
    self._groupList = group
end

function IMData:getTeahouseList()
    return self._teahouseList
end

function IMData:setTeahouseList(list)
    self._teahouseList = list
end

function IMData:setEnterTeaHouseID(teaid)
    self._enterTeahouseID = teaid
end

function IMData:getEnterTeaHouseID()
    return self._enterTeahouseID
end

function IMData:setIsSeer(bSeer)
    self._isSeeing = bSeer or false
    XH.SysTool.performWithDelayGlobal( function()
        self:dispatchEvent({name = self.EVENT_IM_UPDATE_SEER})
    end, 2)
end

function IMData:getIsSeer()
    return self._isSeeing or false
end

function IMData:setWatchRoomId(roomid)
    self._watchRoomId = roomid or 0
end

function IMData:getWatchRoomId()
    return self._watchRoomId
end

function IMData:getFilterTableListData(groupType,groupid)
    if groupType == self.GROUP_TYPE.ALL then
        local tablelist = self:getTableList()
        local tables = {}
        for i = 1 ,#tablelist do
            for _,v in pairs(tablelist[i].tables) do
                table.insert(tables,v)
            end
        end
        return tables
    elseif groupType == self.GROUP_TYPE.GROUP then
        local tablelist = self:getTableList()
        local tables = {}
        for i =1 ,#tablelist do
            if groupid == tablelist[i].passwd then
                for _,v in pairs(tablelist[i].tables) do
                    table.insert(tables,v)
                end
            end
        end
        return tables
    elseif groupType == self.GROUP_TYPE.BOX then
        local tablelist  =  self:getTableList()
        local tables = {}
        for _,value in pairs(tablelist) do
            if value.tableType == self.TABLEDATATYPE.NORMAL 
                or value.tableType == self.TABLEDATATYPE.NORMALOPEN then
                for _,v in pairs(value.tables) do
                    table.insert(tables,v)
                end
            end
        end
        return tables
    elseif groupType == self.GROUP_TYPE.TEAHOSE then
        local tablelist = self:getTableList()
        local tables = {}
        local group = self:getGroupList()
        for i =1 ,#tablelist do
            if tablelist[i].tableType == self.TABLEDATATYPE.TEANOTOPEN or tablelist[i].tableType == self.TABLEDATATYPE.TEAOPEN then
                local bGroup = false 
                for _,gr in pairs(group) do 
                    if gr.nPasswd == tablelist[i].passwd then
                        bGroup = true
                    end
                end
                if not bGroup then
                    for _,v in pairs(tablelist[i].tables) do
                        table.insert(tables,v)
                    end
                end
            end
        end
        return tables
    end
    return {}
end

-- 自己的数据
function IMData:generateSelfInfo()
    local fake_data = {}
    fake_data.bInTea = false 
    fake_data.headurl = XH.playerData:getWeChatURL() or ""
    fake_data.nickname = XH.playerData:getNickName()
    fake_data.player_state = 99
    fake_data.shieldState = 0
    fake_data.last_login_time = os.time()
    fake_data.channelid = XH.areaData:getChannelID()
    fake_data.areaid = XH.areaData:getAreaTypeID()
    fake_data.last_fight_time = os.time()
    fake_data.numid = XH.playerData:getNumberID()
    fake_data.timeprop = {}
    return fake_data
end

return IMData   S  