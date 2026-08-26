local BoxRoomCacheMgr = class("BoxRoomCacheMgr")

local KW_DATA_BOX_ROOM_HISTORY = "BoxRoomHistory.lua"
local KW_DATA_BOX_ROOM_INFO = "KW_DATA_BOX_ROOM_INFO.lua"
local MAX_CACHE_ROUND = 300 --限定的最大局数

BoxRoomCacheMgr._curUserKey = ""

function BoxRoomCacheMgr:getInstance()
    if not self._boxRoomCacheMgr then
        self._boxRoomCacheMgr = BoxRoomCacheMgr.new()
    end
    return self._boxRoomCacheMgr
end

function BoxRoomCacheMgr:init(areaid, numid)
    self:destroyCache()
    local userKey = self:createUserKey(areaid, numid)
    self._curUserKey = userKey
end

function BoxRoomCacheMgr:destroyCache()
    self._boxRoomHistroyInfos = nil
    self._boxRoomInfo = nil
end

function BoxRoomCacheMgr:getBoxRoomHistoryInfos()
    if not self._boxRoomHistroyInfos then
        self._boxRoomHistroyInfos = XH.bf.LuaFileCache:getInstance():getLuaFromFile(self._curUserKey.."_"..KW_DATA_BOX_ROOM_HISTORY)
    end
    return self._boxRoomHistroyInfos or {}
end

function BoxRoomCacheMgr:setBoxRoomHistoryInfos(boxRoomInfos)
    if not boxRoomInfos or "table" ~= type(boxRoomInfos) then return end
    self:saveHistoryToLocalCache(boxRoomInfos)
end

-- history = { players * {nickName = "", score = 0, numid = 0, areaid = 0} }
-- new 包厢不解散 点击开始游戏
-- insert 当前包厢内最近的一次记录与新纪录de 玩家不同
-- append 4个玩家都没改变过，@· 自动准备 @2 继续游戏
function BoxRoomCacheMgr:addBoxRoomHistoryInfo(gameID,roomnumber,history,recordPath)
    if not history or "table" ~= type(history) or
       not gameID or "number" ~= type(gameID) then return end
    
    local infoObj = self:getBoxRoomHistoryInfos()
    local isCreateBoxRoom = true
    for _, v in pairs(infoObj) do
        if v.roomNumber == roomnumber  and self._createTime == v.time then
            if v.gameID == gameID then
                isCreateBoxRoom = false
            end
        end
    end
    
    recordPath = recordPath or ""
    if isCreateBoxRoom then
        self:insertNewBoxRoomData(gameID, roomnumber,history,recordPath)
    else
        self:insertRoundData(gameID, roomnumber,history,recordPath)
    end
end

function BoxRoomCacheMgr:isNewBoxRoom(gameID)
    local infoObj = self:getBoxRoomHistoryInfos()
    local isNewBoxRoom = false
    for _, v in pairs(infoObj) do
        if self:getBoxRoomCode() == v.roomNumber and self._createTime == v.time then
            if v.gameID == gameID then
                isNewBoxRoom = true
            end
        end
    end
    return isNewBoxRoom
end

function BoxRoomCacheMgr:setCreateRoomTime(time)
    self._createTime = time
end

function BoxRoomCacheMgr:getCreateRoomTime()
    local boxRoomInfo = XH.bf.LuaFileCache:getInstance():getLuaFromFile(self._curUserKey.."_"..KW_DATA_BOX_ROOM_INFO) or {}
    return boxRoomInfo.time or os.time()
end

function BoxRoomCacheMgr:setBoxRoomInfo(info)
    self._boxRoomInfo = info
    XH.bf.LuaFileCache:getInstance():writeFileWithLua(self._curUserKey.."_"..KW_DATA_BOX_ROOM_INFO, info)
end

function BoxRoomCacheMgr:getBoxRoomInfo()
    if not self._boxRoomInfo then
        self._boxRoomInfo = XH.bf.LuaFileCache:getInstance():getLuaFromFile(self._curUserKey.."_"..KW_DATA_BOX_ROOM_INFO) or {}
    end
    return self._boxRoomInfo
end

-- 判断自己是否是创建者
function BoxRoomCacheMgr:isSelfCreate()
    self._boxRoomInfo = self:getBoxRoomInfo()
    if "" == self._boxRoomInfo.createName or not self._boxRoomInfo.createName then
        return false
    end
    return self._boxRoomInfo.inviteName == self._boxRoomInfo.createName
end

-- 获取创建者的名字
function BoxRoomCacheMgr:getCreateName()
    return self:getBoxRoomInfo().createName
end

function BoxRoomCacheMgr:insertNewBoxRoomData(gameID, roomNumber, history,recordPath)
    local name = XH.GT.GAME_NAME_LIST[gameID] or ""
    local t =
    {
        gameName = name,
        gameID = gameID,
        time = self._createTime or os.time(),
        roomNumber = roomNumber,
        totalSceore = {},
        histories = {},
    }
    local tmpRoundsInfo = {}
    for _, v in pairs(history) do
        local userKey = self:createUserKey(v.areaid, v.numid)
        t.totalSceore[userKey] = {nickName = v.nickName, scores = v.score}
        tmpRoundsInfo[userKey] = {nickName = v.nickName, scores = v.score, scoresDescribe = v.scoresDescribe }
    end
    tmpRoundsInfo.recordPath = recordPath
    t.histories[#t.histories + 1] = tmpRoundsInfo
    local infoObj = self:getBoxRoomHistoryInfos()
    infoObj[#infoObj + 1] = t
    self:setBoxRoomHistoryInfos(infoObj)
end

function BoxRoomCacheMgr:insertRoundData(gameID, roomnumber, history,recordPath)
    local infoObj = self:getBoxRoomHistoryInfos()
    local latestCreateRoomData = infoObj[#infoObj]
    local tmpRoundsInfo = {}
    for _, v in pairs(history) do
        local userKey = self:createUserKey(v.areaid, v.numid)
        latestCreateRoomData.totalSceore[userKey].scores =  latestCreateRoomData.totalSceore[userKey].scores + v.score
        tmpRoundsInfo[userKey] = {nickName = v.nickName, scores = v.score, scoresDescribe = v.scoresDescribe}
    end
    tmpRoundsInfo.recordPath = recordPath
    latestCreateRoomData.histories[#latestCreateRoomData.histories + 1] = tmpRoundsInfo
    self:saveHistoryToLocalCache(infoObj)
end

function BoxRoomCacheMgr:saveHistoryToLocalCache(boxRoomInfos)
    self._boxRoomHistroyInfos = boxRoomInfos
    local totalRoundNum = 0            --最多保存300局，超过则移除
    local boxRoomNum = 0
    for i = #self._boxRoomHistroyInfos,1,-1 do       
        totalRoundNum = totalRoundNum + #self._boxRoomHistroyInfos[i].histories 
        if totalRoundNum > MAX_CACHE_ROUND then
            boxRoomNum = i
        end
    end
    table.remove(self._boxRoomHistroyInfos, 1, boxRoomNum)
    XH.bf.LuaFileCache:getInstance():writeFileWithLua(self._curUserKey.."_"..KW_DATA_BOX_ROOM_HISTORY, self._boxRoomHistroyInfos)
end

function BoxRoomCacheMgr:serachCurHistroy()
    local infoObj = self:getBoxRoomHistoryInfos()
    local result = nil
    local t = self:getBoxRoomInfo()
    for _, v in pairs(infoObj) do
        if v.gameID == t.gameID and
           v.time == t.time then
            result = v
        end
    end
    return result
end

function BoxRoomCacheMgr:addCurrentJushu()
    cc.UserDefault:getInstance():setIntegerForKey(self._curUserKey.."_".."KW_DATA_BOX_ROOM_CURRENT_JUSHU", self:getCurrentJushu() + 1)
end

function BoxRoomCacheMgr:getCurrentJushu()
    return cc.UserDefault:getInstance():getIntegerForKey(self._curUserKey.."_".."KW_DATA_BOX_ROOM_CURRENT_JUSHU", 0)
end

function BoxRoomCacheMgr:clearCurrentJuShu()
    cc.UserDefault:getInstance():setIntegerForKey(self._curUserKey.."_".."KW_DATA_BOX_ROOM_CURRENT_JUSHU", 0)
end

function BoxRoomCacheMgr:createUserKey(areaid, numid)
    if areaid and numid then
        return areaid.."_"..numid
    end
    return 0
end

function BoxRoomCacheMgr:parseUserKey()
    if self._curUserKey then
        local szValue = {}
        local index = 0
        for w in string.gmatch(self._curUserKey,"[^_]+") do
            index = index + 1
            szValue[index] = w
        end
        if next(szValue) then
            return szValue[1], szValue[2]
        end
    end
    return nil
end

function BoxRoomCacheMgr:setBoxRoomCode(roomCode)
    self.roomCode = roomCode or ""
    cc.UserDefault:getInstance():setStringForKey(self._curUserKey.."_".."roomCode", self.roomCode)
end

function BoxRoomCacheMgr:getBoxRoomCode()
    if not self.roomCode or "" == self.roomCode then
        self.roomCode = cc.UserDefault:getInstance():getStringForKey(self._curUserKey.."_".."roomCode", "")
    end
    return self.roomCode
end

-- 生成包厢房间号
function BoxRoomCacheMgr:genBoxCode(roomID, tableID, sitOrder, pwd)
    print("genBoxCode:roomID, tableID, sitOrder, pwd", roomID, tableID, sitOrder, pwd)
    if self._agentRoomId ~= nil and self._agentRoomId ~= "" then
        if self:getBoxRoomCode() ~= self._agentRoomId then
            XH.bf.BoxRoomCacheMgr:getInstance():setBoxRoomCode(self._agentRoomId)
        end
        return self._agentRoomId
    end
    
    
    local boxRoomList = XH.GT.getBoxRoomList()
    
    local boxCode = ""
    local roomIndex = ""
    for i = 1,#boxRoomList do
        if roomID == boxRoomList[i] then
            roomIndex = string.format("%03d",i)
            break
        end
    end    
    print("1:roomID, boxCode", roomID, boxCode)
    -- 包厢房间号 roomID
    boxCode = boxCode .. string.sub(roomIndex,3,3)
    -- 包厢房间号 tableID,不足3位补0
    local strTableID = string.format("%03d", tableID)
    boxCode = boxCode .. strTableID
    
    -- 包厢房间号 roomID
    boxCode = boxCode .. string.sub(roomIndex,2,2)
    --print("3:sitOrder, boxCode", sitOrder, boxCode)
    
    -- 包厢房间号 房间密码
    boxCode = boxCode .. string.sub(roomIndex,1,1)
    print("4:pwd, boxCode", pwd, boxCode)
    
    -- 房间号
    XH.bf.BoxRoomCacheMgr:getInstance():setBoxRoomCode(boxCode)
    return tonumber(boxCode)
end

function BoxRoomCacheMgr:setLastFirstEnterRoomTime(time)
    cc.UserDefault:getInstance():setIntegerForKey(self._curUserKey.."_".."lastFirstEnterRoomTime", time or 0)
end

function BoxRoomCacheMgr:getLastFirstEnterRoomTime()
    return cc.UserDefault:getInstance():getIntegerForKey(self._curUserKey.."_".."lastFirstEnterRoomTime", 0)
end

function BoxRoomCacheMgr:addAllDirectionFilePath(fillFilePath)
    local infoObj = self:getBoxRoomHistoryInfos()
    local latestCreateRoomData = infoObj[#infoObj]
    fillFilePath = fillFilePath or ""
    if not latestCreateRoomData then return end
    latestCreateRoomData.histories[#latestCreateRoomData.histories].fillFilePath = fillFilePath
    infoObj[#infoObj] = latestCreateRoomData
    self:saveHistoryToLocalCache(infoObj)
end

function BoxRoomCacheMgr:setAgentRoomId(roomid)
    self._agentRoomId = roomid
end


XH.bf.BoxRoomCacheMgr = BoxRoomCacheMgr(  