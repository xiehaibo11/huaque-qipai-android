local ActiveProtocol = {}

local CMDT_REQTEABILLINFO = 31 -- 比赛场账单(领队用)
local CMDT_RESPTEABILLINFO = 32

-- local CMDT_REQSELECTTEABILLINFO = 35            -- 获取根据bigWinScore筛选冠军数据
-- local CMDT_RESPSELECTTEABILLINFO = 36

-- local CMDT_REQTEAPLAYERDATABYDAY  = 39          -- 获取比赛场玩家某日的统计数据
-- local CMDT_RESPTEAPLAYERDATABYDAY = 40

local CMDT_REQBOXLEDGER = 42 -- 获取玩家账单(目前包含包厢和比赛场)
local CMDT_RESPBOXLEDGER = 43

ActiveProtocol.ReqGetGame = {
    XY_ID = 5,

    nAskid = 0,
    nGameid = 0,
    lUuId = "", --  string

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self, buff, bufflen)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nGameid)
        bos:writeString(self.lUuId)
        return bos
    end
}

ActiveProtocol.RespGetGame = {
    XY_ID = 6,

    --[[
	BYTE ucFlag;
	int nAskid;
	int nGameid;
	ulong8 lUuId;
	BYTE ucType;
	short nCnt;
	short nIdx;
	short nSize;
	char szData[4000];
	]]

    ucFlag = 0,
    nAskid = 0,
    nGameid = 0,
    lUuId = "", --  string
    ucType = 0,
    nCnt = 0,
    nIdx = 0,
    nSize = 0,
    szData = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.ucFlag = bis:readUInt8()
        self.nAskid = bis:readInt32()
        self.nGameid = bis:readInt32()
        self.lUuId = bis:readString()
        self.ucType = bis:readUInt8()
        self.nCnt = bis:readInt16()
        self.nIdx = bis:readInt16()
        self.nSize = bis:readInt16()
        if self.nSize > 0 then
            self.szData = bis:read(self.nSize)
        end

        return self
    end
}

ActiveProtocol.GameDetail = {
    uuid = 0,
    scores = {},
    numid = {},
    config = "",
    cuuid = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end
}

ActiveProtocol.DetailInfo = {
    finishgames = 0, -- 完成局数
    playercnt = 0, -- 玩家数量
    gameDetail = {}, -- GameDetail

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end
}

ActiveProtocol.ReqLedger = {
    XY_ID = 1,

    askid = 0,
    gameid = 0,
    startidx = 0, --  -1从最近的开始
    cnt = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self, buff, bufflen)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.gameid)
        bos:writeInt32(self.startidx)
        bos:writeInt32(self.cnt)
        return bos
    end
}

ActiveProtocol.RespLedger = {
    XY_ID = 2,

    askid = 0,
    ledgerInfo = {},
    endtime = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.ledgerInfo = {}
        self.ledgerInfo.appid = bis:readInt32()
        self.ledgerInfo.tableid = bis:readInt32()
        self.ledgerInfo.starttime = bis:readUInt32()
        self.ledgerInfo.roomserialid = bis:readInt32()
        self.ledgerInfo.totalgames = bis:readInt16()
        self.ledgerInfo.finishgames = bis:readInt16()
        self.ledgerInfo.playercnt = bis:readInt16()

        self.ledgerInfo.playerInfos = {}
        for i = 1, self.ledgerInfo.playercnt do
            self.ledgerInfo.playerInfos[i] = {}
            self.ledgerInfo.playerInfos[i].numid = bis:readInt32()
            self.ledgerInfo.playerInfos[i].nickname = bis:readString()
            self.ledgerInfo.playerInfos[i].totalscore = bis:readInt32()
        end
        self.ledgerInfo.config = bis:readString()

        if bis:getAvailableSize() > 0 then
            for i = 1, self.ledgerInfo.playercnt do
                self.ledgerInfo.playerInfos[i].ucType = bis:readUInt8()
            end
        end
        if bis:getAvailableSize() >= 4 then
            self.endtime = bis:readUInt32()
        end
        self.ledgerInfo.roommode = 0
        self.ledgerInfo.gameID = 0
        self.ledgerInfo.nUsePropCnt = 0
        self.ledgerInfo.roomHostID = 0
        self.ledgerInfo.areaID = 0
        self.ledgerInfo.tid = 0
        -- 消耗类型  4：领队消耗   5：AA消耗    2022.1.11后 领队消耗统一改名为冠军消耗
        self.ledgerInfo.nMaiType = 0

        if #self.ledgerInfo.config > 0 and string.byte(self.ledgerInfo.config, 1) == 123 -- '{'
        and string.byte(self.ledgerInfo.config, #self.ledgerInfo.config) == 125 then -- '}'
            local luaBlock = loadstring("return " .. self.ledgerInfo.config)
            local configdict = nil
            if luaBlock then
                configdict = luaBlock()
            end

            if configdict then
                if configdict["url"] and configdict["numid"] then
                    for i = 1, self.ledgerInfo.playercnt do
                        for k = 1, self.ledgerInfo.playercnt do
                            if self.ledgerInfo.playerInfos[i].numid == configdict["numid"][k] then
                                self.ledgerInfo.playerInfos[i].szUrl = string.urldecode(configdict["url"][k])
                                break
                            end
                        end
                    end
                end
                if configdict["roomNum"] then
                    local roomNum = tonumber(configdict["roomNum"])
                    if roomNum then
                        self.ledgerInfo.config = string.format("%06d", roomNum)
                    else
                        self.ledgerInfo.config = tostring(configdict["roomNum"])
                    end
                end
                if configdict["roommode"] then
                    self.ledgerInfo.roommode = tonumber(configdict["roommode"])
                end
                if configdict["gid"] then
                    self.ledgerInfo.gameID = tonumber(configdict["gid"])
                end
                if configdict["propcnt"] then
                    self.ledgerInfo.nUsePropCnt = tonumber(configdict["propcnt"])
                end
                if configdict["onid"] then
                    self.ledgerInfo.roomHostID = tonumber(configdict["onid"])

                end
                if configdict["oaid"] then
                    self.ledgerInfo.areaID = tonumber(configdict["oaid"])
                end
                if configdict["tid"] then
                    self.ledgerInfo.tid = tonumber(configdict["tid"])
                end
                if configdict["nMaiType"] then
                    self.ledgerInfo.nMaiType = tonumber(configdict["nMaiType"])
                    if self.ledgerInfo.nMaiType == 4 then
                        self.ledgerInfo.nMaiType = 0
                    elseif self.ledgerInfo.nMaiType == 5 then
                        self.ledgerInfo.nMaiType = 1
                    end
                end
                -- 拓展信息（麻将的杠分、点杠次数等，斗地主地主次数等，全垒打次数）
                if configdict["endInfoEx"] then
                    self.ledgerInfo.endInfoEx = {}
                    local endInfoExDict = string.split(configdict["endInfoEx"], "|")
                    for _, item in pairs(endInfoExDict) do
                        local luaBlockExt = loadstring("return { " .. item .. "}")
                        local endInfoExDictItem
                        if luaBlockExt then
                            endInfoExDictItem = luaBlockExt()
                            if endInfoExDictItem then
                                self.ledgerInfo.endInfoEx[tostring(endInfoExDictItem["NID"])] = endInfoExDictItem
                            end
                        end
                    end
                end
            end
        end
        return self
    end
}

ActiveProtocol.ReqDetail = {
    XY_ID = 3,

    askid = 0,
    gameid = 0,
    appid = 0,
    tableid = 0,
    starttime = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self, buff, bufflen)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.gameid)
        bos:writeInt32(self.appid)
        bos:writeInt32(self.tableid)
        bos:writeUInt32(self.starttime)
        return bos
    end
}

ActiveProtocol.RespDetail = {
    XY_ID = 4,

    askid = 0,
    detailInfo = ActiveProtocol.DetailInfo:new(),
    flag = -1,
    nErrorCode = 0,
    bComplete = true,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()

        self.detailInfo = ActiveProtocol.DetailInfo:new()
        self.detailInfo.finishgames = bis:readInt16()
        self.detailInfo.playercnt = bis:readInt16()

        self.detailInfo.gameDetail = {}
        for i = 1, self.detailInfo.finishgames do
            self.detailInfo.gameDetail[i] = ActiveProtocol.GameDetail:new() ------lcy ()
            self.detailInfo.gameDetail[i].uuid = bis:readInt64()
            self.detailInfo.gameDetail[i].scores = {}
            self.detailInfo.gameDetail[i].numid = {}
            for j = 1, self.detailInfo.playercnt do
                self.detailInfo.gameDetail[i].scores[j] = bis:readInt32()
                self.detailInfo.gameDetail[i].numid[j] = bis:readInt32()
            end
            self.detailInfo.gameDetail[i].config = bis:readString()
        end
        for i = 1, self.detailInfo.finishgames do
            if (bis:getAvailableSize() > 0) then
                self.detailInfo.gameDetail[i].cuuid = bis:readString()
            end
        end

        if bis:getAvailableSize() > 0 then
            self.flag = bis:readUInt8()
        end
        for i = 1, self.detailInfo.finishgames do
            if (bis:getAvailableSize() >= 4) then
                self.detailInfo.gameDetail[i].shareID = bis:readInt32()
            end
        end
        for i = 1, self.detailInfo.finishgames do
            if (bis:getAvailableSize() >= 4) then
                self.detailInfo.gameDetail[i].endtime = bis:readInt32()
            end
        end
        if bis:getAvailableSize() > 0 then
            self.nErrorCode = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.bComplete = bis:readBool()
        end
        return self
    end
}

ActiveProtocol.BatchProtocol = {
    XY_ID = 106,

    FLAG = {
        START = 0, -- 批量发送开始
        END = 1 -- 批量发送结束
    },
    m_flag = 0,
    m_xyid = 0, -- 正在批量发的协议号
    m_count = 0, -- 数量

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.m_flag = bis:readUInt8()
        self.m_xyid = bis:readInt16()
        self.m_count = bis:readInt16()

        return self
    end
}

ActiveProtocol.ReqCurTime = {
    XY_ID = 24,

    nAskid = 0, -- 请求id
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        return bos
    end
}

ActiveProtocol.RespCurTime = {
    XY_ID = 25,

    nAskid = 0, -- 请求id
    nTime = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nTime = bis:readInt32()

        return self
    end
}

ActiveProtocol.ReqTeaPlayerLedger = {
    XY_ID = 26,

    nAskid = 0, -- 请求id
    nTeaid = 0,
    nDay = 0,
    nCnt = 0,
    nStartIndex = 0, -- 开始索引值
    nAreaid = 0, -- 区号id
    nNumid = 0, -- 指定请求的玩家id
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nTeaid)
        bos:writeInt32(self.nDay)
        bos:writeInt32(self.nCnt)
        bos:writeInt32(self.nStartIndex)
        bos:writeInt32(self.nAreaid)
        bos:writeInt32(self.nNumid)
        return bos
    end
}

ActiveProtocol.RespTeaPlayerLedger = {
    XY_ID = 27,

    askid = 0,
    ledgerInfo = {},
    nCnt = 0,
    nEndTime = 0,
    szConfig = "",
    acLedgerId = "",  -- 总单id
    nActiveState = 0, -- 战绩状态 0:已经结束 1:正在游戏中
    chtotalscore = {},
    nAllTotalCnt = 0, -- 战绩总数

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ledgerInfo = {}
        self.ledgerInfo.appid = bis:readInt32()
        self.ledgerInfo.tableid = bis:readInt32()
        self.ledgerInfo.starttime = bis:readUInt32()
        self.ledgerInfo.roomserialid = bis:readInt32()
        self.ledgerInfo.totalgames = bis:readInt16()
        self.ledgerInfo.finishgames = bis:readInt16()
        self.ledgerInfo.playercnt = bis:readInt16()

        self.ledgerInfo.playerInfos = {}
        for i = 1, self.ledgerInfo.playercnt do
            self.ledgerInfo.playerInfos[i] = {}
            self.ledgerInfo.playerInfos[i].numid = bis:readInt32()
            self.ledgerInfo.playerInfos[i].nickname = bis:readString()
            self.ledgerInfo.playerInfos[i].totalscore = bis:readInt32()
        end
        self.ledgerInfo.config = bis:readString()

        if bis:getAvailableSize() > 0 then
            for i = 1, self.ledgerInfo.playercnt do
                self.ledgerInfo.playerInfos[i].ucType = bis:readUInt8()
            end
        end
        self.nCnt = bis:readInt32()
        if bis:getAvailableSize() >= 4 then
            self.nEndTime = bis:readInt32()
        end

        if bis:getAvailableSize() > 0 then
            self.szConfig = bis:readString()
            if #self.szConfig > 0 and string.byte(self.szConfig, 1) == 123 -- '{'
            and string.byte(self.szConfig, #self.szConfig) == 125 then -- '}'
                local luaBlock = loadstring("return " .. self.szConfig)
                local configdict = nil
                if luaBlock then
                    configdict = luaBlock()
                end
                if configdict and configdict["fee"] then
                    self.ledgerInfo.fee = tonumber(configdict["fee"])
                end
            end
        end

        if bis:getAvailableSize() > 0 then
            self.acLedgerId = bis:readString()
        end
        if bis:getAvailableSize() > 0 then
            self.nActiveState = bis:readInt32()
        end
        self.chtotalscore = {}
        if bis:getAvailableSize() > 0 then
            for i = 1 , self.ledgerInfo.playercnt do
                self.chtotalscore[i] = bis:readString()
                self.ledgerInfo.playerInfos[i].chtotalscore = self.chtotalscore[i]
            end
        end    
        if bis:getAvailableSize() > 0 then
            self.nAllTotalCnt = bis:readInt32()
        end

        self.ledgerInfo.roommode = 0
        self.ledgerInfo.gameID = 0
        self.ledgerInfo.nUsePropCnt = 0
        self.ledgerInfo.roomHostID = 0
        self.ledgerInfo.areaID = 0
        self.ledgerInfo.tid = 0
        -- 消耗类型  4：领队消耗   5：AA消耗
        self.ledgerInfo.nMaiType = 0
        -- {roomNum=9286901;gid=30001;propcnt=3;tabid=19;chid=20001;cltype=0;
        -- ident="41f14saf90003069900000003FD3333379asfafF29";msgsrc=1;roommode=13;
        -- url={'','',};numid={40102019,40102242,};oaid=1000079;onid=72147;tid=4009;
        -- tableid=9286901;FZnumid=40102019;nMaiType=4;}

        if #self.ledgerInfo.config > 0 and string.byte(self.ledgerInfo.config, 1) == 123 -- '{'
        and string.byte(self.ledgerInfo.config, #self.ledgerInfo.config) == 125 then -- '}'
            local luaBlock = loadstring("return " .. self.ledgerInfo.config)
            local configdict = nil
            if luaBlock then
                configdict = luaBlock()
            end

            if configdict then
                if configdict["url"] and configdict["numid"] then
                    for i = 1, self.ledgerInfo.playercnt do
                        for k = 1, self.ledgerInfo.playercnt do
                            if self.ledgerInfo.playerInfos[i].numid == configdict["numid"][k] then
                                self.ledgerInfo.playerInfos[i].szUrl = string.urldecode(configdict["url"][k])
                                break
                            end
                        end
                    end
                end
                if configdict["roomNum"] then
                    local roomNum = tonumber(configdict["roomNum"])
                    if roomNum then
                        self.ledgerInfo.config = string.format("%06d", roomNum)
                    else
                        self.ledgerInfo.config = tostring(configdict["roomNum"])
                    end
                end
                if configdict["roommode"] then
                    self.ledgerInfo.roommode = tonumber(configdict["roommode"])
                end
                if configdict["gid"] then
                    self.ledgerInfo.gameID = tonumber(configdict["gid"])
                end
                if configdict["propcnt"] then
                    self.ledgerInfo.nUsePropCnt = tonumber(configdict["propcnt"])
                end
                if configdict["propsid"] then
                    self.ledgerInfo.nPropid = tonumber(configdict["propsid"])
                end
                if configdict["onid"] then
                    self.ledgerInfo.roomHostID = tonumber(configdict["onid"])

                end
                if configdict["oaid"] then
                    self.ledgerInfo.areaID = tonumber(configdict["oaid"])
                end
                if configdict["tid"] then
                    self.ledgerInfo.tid = tonumber(configdict["tid"])
                end
                if configdict["nMaiType"] then
                    self.ledgerInfo.nMaiType = tonumber(configdict["nMaiType"])
                    if self.ledgerInfo.nMaiType == 4 then
                        self.ledgerInfo.nMaiType = 0
                    elseif self.ledgerInfo.nMaiType == 5 then
                        self.ledgerInfo.nMaiType = 1
                    end
                end
                if configdict["nPayType"] then
                    self.ledgerInfo.nPayType = tonumber(configdict["nPayType"])
                end
                if configdict["prevaa"] then
                    self.ledgerInfo.prevaa = tonumber(configdict["prevaa"])
                end
                -- 拓展信息（麻将的杠分、点杠次数等，斗地主地主次数等，全垒打次数等）
                if configdict["endInfoEx"] then
                    self.ledgerInfo.endInfoEx = {}
                    local endInfoExDict = string.split(configdict["endInfoEx"], "|")
                    for _, item in pairs(endInfoExDict) do
                        local luaBlockExt = loadstring("return { " .. item .. "}")
                        local endInfoExDictItem
                        if luaBlockExt then
                            endInfoExDictItem = luaBlockExt()
                            if endInfoExDictItem then
                                self.ledgerInfo.endInfoEx[tostring(endInfoExDictItem["NID"])] = endInfoExDictItem
                            end
                        end
                    end
                end
                if configdict["originpaytype"] then
                    self.ledgerInfo.originpaytype = tonumber(configdict["originpaytype"])
                end

            end
        end
        return self
    end
}

ActiveProtocol.ReqCleanTeaBills = { -- 一键处理
    XY_ID = 28,
    nAskid = 0, -- 请求id
    nTeaid = 0, -- 比赛场id
    nDay = 0, -- 日期
    nGameLobbyID = 0,
    nRefreshTime = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nTeaid)
        bos:writeInt32(self.nDay)
        bos:writeInt32(self.nGameLobbyID)
        bos:writeInt32(self.nRefreshTime)
        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nTeaid = bis:readInt32()
        self.nDay = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.nGameLobbyID = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nRefreshTime = bis:readInt32()
        end
        return self
    end
}

ActiveProtocol.RespDealBigWinnerBill = {
    XY_ID = 21,
    FLAG = {
        SUCCESS = 0,
        NO_RIGHT = 1,
        BILL_INCONSISTENT = 2,
        NOT_FENGXIN_RIGHT = 3
    },
    nAskid = 0, -- 请求id
    nTableid = 0,
    nTime = 0,
    nFullCnt = 0,
    nPlayerCnt = 0,
    nAreaid = {},
    nNumid = {},
    szNickName = {},
    nDetailCnt = 0,
    nScore = {},
    nCost = 0,
    nCnt = 0,
    nSeat = 0, -- 主体座位号
    flag = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nTableid = bis:readInt32()
        self.nTime = bis:readInt32()
        self.nFullCnt = bis:readInt32()
        self.nPlayerCnt = bis:readInt32()
        self.nAreaid = {}
        self.nNumid = {}
        self.szNickName = {}
        for i = 1, self.nPlayerCnt do
            self.nAreaid[i] = bis:readInt32()
            self.nNumid[i] = bis:readInt32()
            self.szNickName[i] = bis:readString()
        end
        self.nScore = {}
        self.nDetailCnt = bis:readInt32()
        for i = 1, self.nDetailCnt do
            local tempScore = {}
            for j = 1, self.nPlayerCnt do
                tempScore[j] = bis:readInt32()
            end
            self.nScore[i] = tempScore
        end
        self.nCost = bis:readInt32()
        self.nCnt = bis:readInt32()

        if bis:getAvailableSize() > 0 then
            self.nSeat = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.flag = bis:readInt8()
        end
        return self
    end

}

-- 请求处理冠军账单
ActiveProtocol.ReqDealBigWinnerBill = {
    XY_ID = 20,

    DEAL_TYPE = {
        DEAL_NORMAL = 0, -- 正常计算
        DEAL_ONLYFLAG = 1 -- 仅标记计算
    },

    nAskid = 0, -- 请求id
    nTeaid = 0, -- 比赛场id
    nNumid = 0, -- 请求详情的玩家
    nDay = 0, -- 请求时间
    nCnt = 0,
    nGameLobbyID = 0,
    nRefreshTime = 0,
    nDealType = 0, --  计算类型

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nTeaid)
        bos:writeInt32(self.nNumid)
        bos:writeInt32(self.nDay)
        bos:writeInt32(self.nCnt)
        bos:writeInt32(self.nGameLobbyID)
        bos:writeInt32(self.nRefreshTime)
        bos:writeInt32(self.nDealType)
        return bos
    end
}

ActiveProtocol.ReqGetShareID = {
    XY_ID = 7,

    nAskid = 0,
    nCount = 0,
    cuuid = {}, --  char[32][25]
    nShareID = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self, buff, bufflen)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        local nCountTemp = self.nCount
        bos:writeInt32(nCountTemp)
        for i = 1, nCountTemp do
            bos:writeString(self.cuuid[i])
        end
        bos:writeInt32(self.nShareID)
        return bos
    end
}

ActiveProtocol.RespGetShareID = {
    XY_ID = 8,

    nAskid = 0,
    nCount = 0,
    shareID = {}, --  int[32]
    cuuid = {}, --  char[32][25]
    shareList = {}, -- char[512]

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nCount = bis:readInt32()
        for i = 1, self.nCount do
            self.shareID[i] = bis:readInt32()
        end
        for i = 1, self.nCount do
            self.cuuid[i] = bis:readString()
        end
        if bis:getAvailableSize() > 0 then
            self.shareList = bis:readString()
        end

        return self
    end
}

-- 请求冠军详单
ActiveProtocol.ReqTeaBigWinnerBill = {
    XY_ID = 18,
    FLAG = {UNBILL = 0, BILL = 1},
    nAskid = 0, -- 请求id
    nTeaid = 0, -- 比赛场id
    nNumid = 0, -- 请求详情的玩家
    nDay = 0, -- 请求时间
    nCnt = 0, -- 请求账单数量
    ucFlag = 0, -- 已计算/未计算

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nTeaid = bis:readInt32()
        self.nNumid = bis:readInt32()
        self.nDay = bis:readInt32()
        self.nCnt = bis:readInt32()
        self.ucFlag = bis:readUInt8()
        return self
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nTeaid)
        bos:writeInt32(self.nNumid)
        bos:writeInt32(self.nDay)
        bos:writeInt32(self.nCnt)
        bos:writeUInt8(self.ucFlag)
        return bos
    end
}

-- 返回冠军详单
ActiveProtocol.RespTeaBigWinnerBill = {
    XY_ID = 19,

    nAskid = 0, -- 请求id
    nTableid = 0, -- 桌号  
    nTime = 0, -- 开始时间
    nFullCnt = 0, -- 包厢完整局数
    nPlayerCnt = 0, -- 当前总单玩家数量
    nAreaid = {}, -- 玩家区号
    nNumid = {}, -- 玩家数字帐号
    szNickName = {}, -- 玩家昵称
    nDetailCnt = 0, -- 当前总单详单局数
    nScore = {}, -- 对应玩家胜负值
    nCnt = 0, -- 下发的总单数量
    nBillOperator = 0, -- 操作者,0 或者 numid
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nTableid = bis:readInt32()
        self.nTime = bis:readInt32()
        self.nFullCnt = bis:readInt32()
        self.nPlayerCnt = bis:readInt32()
        self.nAreaid = {}
        self.nNumid = {}
        self.szNickName = {}
        self.nScore = {}
        for i = 1, self.nPlayerCnt do
            self.nAreaid[i] = bis:readInt32()
            self.nNumid[i] = bis:readInt32()
            self.szNickName[i] = bis:readString()
            self.nScore[i] = bis:readInt32()
        end
        self.nDetailCnt = bis:readInt32()
        self.nCnt = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.nBillOperator = bis:readInt32()
        end
        return self
    end

}

-- 请求冠军总单
ActiveProtocol.ReqTeaBigWinnerInfo = {
    XY_ID = 16,
    FLAG = {
        UNBILL = 0,
        BILL = 1,
        UNBILLEX = 2, -- 冠军次数为0也下发数据
        BILLEX = 3
    },
    SORT_TYPE = {
        BY_TIME = 0, -- 根据时间排序
        BY_WINTIMES = 1 -- 根据冠军次数
    },
    nAskid = 0, -- 请求id
    nTeaid = 0, -- 比赛场id
    nDay = 0, -- 请求时间
    nPlayerIndex = 0, -- 请求的玩家序列(从旧至新)
    nPlayerCnt = 0, -- 请求的总共玩家数量
    ucFlag = 0, -- 已计算/未计算
    nSingleNumid = 0, -- 非零时,只请求单一冠军信息
    ucSortType = 0, -- 排序类型
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nTeaid = bis:readInt32()
        self.nDay = bis:readInt32()
        self.nPlayerIndex = bis:readInt32()
        self.nPlayerCnt = bis:readInt32()
        self.ucFlag = bis:readUInt8()
        if bis:getAvailableSize() > 0 then
            self.nSingleNumid = bis:readInt32()
        end
        return self
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nTeaid)
        bos:writeInt32(self.nDay)
        bos:writeInt32(self.nPlayerIndex)
        bos:writeInt32(self.nPlayerCnt)
        bos:writeUInt8(self.ucFlag)
        bos:writeInt32(self.nSingleNumid)
        bos:writeUInt8(self.ucSortType)
        return bos
    end
}

-- 返回冠军总单
ActiveProtocol.RespTeaBigWinnerInfo = {
    XY_ID = 17,

    nAskid = 0, -- 请求id
    nAreaid = 0,
    nNumid = 0, -- 玩家numid
    nCostCard = 0, -- 房卡消耗
    nWinnerTimes = 0, -- 冠军次数
    nScore = 0, -- 每日优胜值
    nPlayerCnt = 0, -- 下发的冠军数
    szUrl = "",
    szNickName = "",
    nTimer = 0,
    nTotalBillOperator = 0, -- 操作者，0 领队， -1 多人操作， numid 操作者
    nRefreshTime = 0,
    nTotalTimes = 0, -- 总场次
    nTotalScore = 0,
    szConfig = "", -- 扩展信息 fee=15.6;
    nBillCnt = 0, --当前请求类型的战绩数量
    nScoreWin = 0,--当前请求类型的应分总和(暂未实现)
    nScoreLost = 0,--当前请求类型的输分总和(暂未实现)
    nAATimes = 0,
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nAreaid = bis:readInt32()
        self.nNumid = bis:readInt32()
        self.nCostCard = bis:readInt32()
        self.nWinnerTimes = bis:readInt32()
        self.nScore = bis:readInt32()
        self.nPlayerCnt = bis:readInt32()
        self.szUrl = bis:readString()
        self.szNickName = bis:readString()
        self.nTimer = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.nTotalBillOperator = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nRefreshTime = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nTotalTimes = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nTotalScore = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.szConfig = bis:readString()
            self.propsid = 0
            if string.len(self.szConfig) > 0 and string.byte(self.szConfig, 1) == 123 -- '{'
            and string.byte(self.szConfig, #self.szConfig) == 125 then -- '}'
                local luaBlock = loadstring("return " .. self.szConfig)
                local configdict = nil
                if luaBlock then
                    configdict = luaBlock()
                end
                if configdict and configdict["fee"] then
                    self.fee = tonumber(configdict["fee"])
                end
                if configdict and configdict["propsid"] then
                    self.propsid = tonumber(configdict["propsid"])
                end
                if configdict and configdict["billdealed"] then
                    self.billdealed = tonumber(configdict["billdealed"])
                end
            end
        end
        if bis:getAvailableSize() > 0 then
            self.nBillCnt = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nScoreWin = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nScoreLost = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nAATimes = bis:readInt32()
        end
        
        return self
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nAreaid)
        bos:writeInt32(self.nNumid)
        bos:writeInt32(self.nCostCard)
        bos:writeInt32(self.nWinnerTimes)
        bos:writeInt32(self.nScore)
        bos:writeInt32(self.nPlayerCnt)
        bos:writeString(self.szUrl)
        bos:writeString(self.szNickName)
        bos:writeInt32(self.nTimer)
        bos:writeInt32(self.nTotalBillOperator)
        bos:writeInt32(self.nRefreshTime)
        bos:writeInt32(self.nBillCnt)
        bos:writeInt32(self.nScoreWin)
        bos:writeInt32(self.nScoreLost)
        bos:writeInt32(self.nAATimes)
        return bos
    end
}

-- 领队请求比赛场账单，包括正在游戏中
ActiveProtocol.ReqTeaBillInfo = {
    XY_ID = CMDT_REQTEABILLINFO,

    nAskid = 0, -- 请求id
    nTeaid = 0,
    nDay = 0, -- 请求第几天的数据
    nStartIndex = 0, -- 从第几条开始取，0表示从第一条开始取
    nBillCnt = 0, -- 需要获取的条数
    nAskType = 0, -- 请求类型 0：正常 1：包括游戏中
    nOrderType = 0, -- startindex顺序类型  顺序类型 0：从新至旧 1：从旧至新
    nTimeType = 0, -- nDay时间类型  0:按天取整 1:时间戳小的方向 2:时间戳增大的方向
    nPayType = 0, -- 0:全部 1：aa支付 2:领队支付
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nTeaid)
        bos:writeInt32(self.nDay)
        bos:writeInt32(self.nStartIndex)
        bos:writeInt32(self.nBillCnt)
        bos:writeInt32(self.nAskType)
        bos:writeInt32(self.nOrderType)
        bos:writeInt32(self.nTimeType)
        bos:writeInt32(self.nPayType)
        return bos
    end
}

-- 领队请求比赛场账单返回，包括正在游戏中
ActiveProtocol.RespTeaBillInfo = {
    XY_ID = CMDT_RESPTEABILLINFO,

    nAskid = 0,
    ledgerInfo = {}, -- 总单信息
    nCardCost = 0, -- 房卡消耗
    nEndTime = 0, -- 结束时间
    nOrdinalIndex = 0, -- 从旧至新对应序号(从1开始)
    nTotalCnt = 0, -- 下发数据总条数
    nActiveState = 0, -- 账单状态 0:已经结束 1:正在游戏中
    nDealState = 0, -- 账单计算状态，1已计算，0未计算
    acLedgerId = "", -- 总单id
    nDealerNumId = 0,
    acDealerNickname = "",
    acDealerHeadUrl = "",
    anArrearCount = {},
    chtotalscore = {},
    nAllTotalCnt = 0,
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.nAskid = bis:readInt32()
        self.ledgerInfo = {}
        self.ledgerInfo.appid = bis:readInt32()
        self.ledgerInfo.tableid = bis:readInt32()
        self.ledgerInfo.starttime = bis:readInt32()
        self.ledgerInfo.roomserialid = bis:readInt32()
        self.ledgerInfo.totalgames = bis:readInt16()
        self.ledgerInfo.finishgames = bis:readInt16()
        self.ledgerInfo.playercnt = bis:readInt16()

        self.ledgerInfo.playerInfos = {}
        for i = 1, self.ledgerInfo.playercnt do
            self.ledgerInfo.playerInfos[i] = {}
            self.ledgerInfo.playerInfos[i].numid = bis:readInt32()
            self.ledgerInfo.playerInfos[i].nickname = bis:readString()
            self.ledgerInfo.playerInfos[i].totalscore = bis:readInt32()
        end
        self.ledgerInfo.config = bis:readString()

        if bis:getAvailableSize() > 0 then
            for i = 1, self.ledgerInfo.playercnt do
                self.ledgerInfo.playerInfos[i].ucType = bis:readUInt8()
            end
        end

        self.nCardCost = bis:readInt32()
        self.nEndTime = bis:readInt32()
        self.nOrdinalIndex = bis:readInt32()
        self.nTotalCnt = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.nActiveState = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nDealState = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.acLedgerId = bis:readString()
        end
        if bis:getAvailableSize() > 0 then
            self.nDealerNumId = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.acDealerNickname = bis:readString()
        end
        if bis:getAvailableSize() > 0 then
            self.acDealerHeadUrl = bis:readString()
        end
        self.anArrearCount = {}
        for i = 1, self.ledgerInfo.playercnt do
            if bis:getAvailableSize() > 0 then
                self.anArrearCount[i] = bis:readInt32()
                self.ledgerInfo.playerInfos[i].arrearcount = self.anArrearCount[i]
            end
        end
        self.chtotalscore = {}
        for i = 1 , self.ledgerInfo.playercnt do
            if bis:getAvailableSize() > 0 then
                self.chtotalscore[i] = bis:readString()
                self.ledgerInfo.playerInfos[i].chtotalscore = self.chtotalscore[i]
            end
        end          
        if bis:getAvailableSize() > 0 then
            self.nAllTotalCnt = bis:readInt32()	 --  所有账单的总条数
        end
        self.ledgerInfo.roommode = 0
        self.ledgerInfo.gameID = 0
        self.ledgerInfo.nUsePropCnt = 0
        self.ledgerInfo.roomHostID = 0
        self.ledgerInfo.areaID = 0
        self.ledgerInfo.tid = 0
        self.ledgerInfo.scoreMultiple = 1
        self.ledgerInfo.roomNum = ""
        self.ledgerInfo.propsid = 0
        if #self.ledgerInfo.config > 0 and string.byte(self.ledgerInfo.config, 1) == 123 -- '{'
        and string.byte(self.ledgerInfo.config, #self.ledgerInfo.config) == 125 then -- '}'
            local luaBlock = loadstring("return " .. self.ledgerInfo.config)
            local configdict = nil
            if luaBlock then
                configdict = luaBlock()
            end

            if configdict then
                if configdict["url"] and configdict["numid"] then
                    for i = 1, self.ledgerInfo.playercnt do
                        for k = 1, self.ledgerInfo.playercnt do
                            if self.ledgerInfo.playerInfos[i].numid == configdict["numid"][k] then
                                self.ledgerInfo.playerInfos[i].szUrl = string.urldecode(configdict["url"][k])
                                break
                            end
                        end
                    end
                end
                if configdict["roomNum"] then
                    local roomNum = tonumber(configdict["roomNum"])
                    if roomNum then
                        self.ledgerInfo.roomNum = string.format("%06d", roomNum)
                        -- 兼容老比赛场
                        self.ledgerInfo.config = string.format("%06d", roomNum)
                    else
                        self.ledgerInfo.roomNum = tostring(configdict["roomNum"])
                        -- 兼容老比赛场
                        self.ledgerInfo.config = tostring(configdict["roomNum"])
                    end
                end
                if configdict["roommode"] then
                    self.ledgerInfo.roommode = tonumber(configdict["roommode"])
                end
                if configdict["gid"] then
                    self.ledgerInfo.gameID = tonumber(configdict["gid"])
                end
                if configdict["propcnt"] then
                    self.ledgerInfo.nUsePropCnt = tonumber(configdict["propcnt"])
                end
                if configdict["onid"] then
                    self.ledgerInfo.roomHostID = tonumber(configdict["onid"])

                end
                if configdict["oaid"] then
                    self.ledgerInfo.areaID = tonumber(configdict["oaid"])
                end
                if configdict["tid"] then
                    self.ledgerInfo.tid = tonumber(configdict["tid"])
                end
                if configdict["endtype"] then
                    self.ledgerInfo.endtype = tonumber(configdict["endtype"])
                end
                if configdict["displayer_nid"] then
                    self.ledgerInfo.displayer_nid = tonumber(configdict["displayer_nid"])
                end
                if configdict["displayer_nick"] then
                    self.ledgerInfo.displayer_nick = configdict["displayer_nick"]
                end
                if configdict["nPayType"] then
                    self.ledgerInfo.nPayType = tonumber(configdict["nPayType"])
                end
                if configdict["scoreMultiple"] then
                    self.ledgerInfo.scoreMultiple = tonumber(configdict["scoreMultiple"])
                end
                if configdict["propsid"] then
                    self.ledgerInfo.propsid = tonumber(configdict["propsid"])
                end
                if configdict["prevaa"] then
                    self.ledgerInfo.prevaa = tonumber(configdict["prevaa"])
                end
                -- 拓展信息（麻将的杠分、点杠次数等，斗地主地主次数等）
                if configdict["endInfoEx"] then
                    self.ledgerInfo.endInfoEx = {}
                    local endInfoExDict = string.split(configdict["endInfoEx"], "|")
                    for _, item in pairs(endInfoExDict) do
                        local luaBlockExt = loadstring("return { " .. item .. "}")
                        local endInfoExDictItem
                        if luaBlockExt then
                            endInfoExDictItem = luaBlockExt()
                            if endInfoExDictItem then
                                self.ledgerInfo.endInfoEx[tostring(endInfoExDictItem["NID"])] = endInfoExDictItem
                            end
                        end
                    end
                end
            end
        end
        return self
    end
}

-- 请求消耗协议
-- 用于查询比赛场的房卡消耗
ActiveProtocol.ReqTeaHouseCost = {
    XY_ID = 12,
    nAskid = 0, -- 请求id
    nAreaid = 0, -- 区号id
    nTeaid = 0, -- 比赛场id
    nDay = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nAreaid = bis:readInt32()
        self.nTeaid = bis:readInt32()

        if bis:getAvailableSize() > 0 then
            self.nDay = bis:readInt32()
        end
        return self
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nAreaid)
        bos:writeInt32(self.nTeaid)
        bos:writeInt32(self.nDay)
        return bos
    end

}

-- 消耗回复协议
ActiveProtocol.RespTeaHouseCost = {
    XY_ID = 13,
    nAskid = 0, -- 请求id
    nAreaid = 0, -- 区号id
    nTeaid = 0, -- 比赛场id
    nTodaycost = 0, -- 今日消耗
    nHistorycost = 0, -- 历史消耗
    nPropid = 0, -- 道具id

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nAreaid = bis:readInt32()
        self.nTeaid = bis:readInt32()
        self.nTodaycost = bis:readInt32()
        self.nHistorycost = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.nPropid = bis:readInt32()
        end
        return self
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nAreaid)
        bos:writeInt32(self.nTeaid)
        bos:writeInt32(self.nTodaycost)
        bos:writeInt32(self.nHistorycost)
        bos:writeInt32(self.nPropid)
        return bos
    end
}

-- 请求消耗协议
-- 用于查询玩家比赛场中的房卡消耗
ActiveProtocol.ReqTeaHousePlayerCost = {
    XY_ID = 14,
    nAskid = 0, -- 请求id
    nAreaid = 0, -- 区号id
    nTeaid = 0, -- 比赛场id
    nNumid = 0, -- 玩家id

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nAreaid = bis:readInt32()
        self.nTeaid = bis:readInt32()
        self.nNumid = bis:readInt32()
        return self
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nAreaid)
        bos:writeInt32(self.nTeaid)
        bos:writeInt32(self.nNumid)
        return bos
    end

}

-- 消耗回复协议
ActiveProtocol.RespTeaHousePlayerCost = {
    XY_ID = 15,
    nAskid = 0, -- 请求id
    nAreaid = 0, -- 区号id
    nTeaid = 0, -- 比赛场id
    nNumid = 0, -- 玩家id
    nTodaycost = 0, -- 今日消耗
    nHistorycost = 0, -- 历史消耗
    nPropid = 0, -- 道具id

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nAreaid = bis:readInt32()
        self.nTeaid = bis:readInt32()
        self.nNumid = bis:readInt32()
        self.nTodaycost = bis:readInt32()
        self.nHistorycost = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.nPropid = bis:readInt32()
        end
        return self
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nAreaid)
        bos:writeInt32(self.nTeaid)
        bos:writeInt32(self.nNumid)
        bos:writeInt32(self.nTodaycost)
        bos:writeInt32(self.nHistorycost)
        bos:writeInt32(self.nPropid)
        return bos
    end
}

ActiveProtocol.ReqBoxLedger = {
    XY_ID = CMDT_REQBOXLEDGER,
    nAskid = 0,
    nDay = 0, -- 时间戳|偏移值(>=0)均可
    nCnt = 0, -- 请求数量
    nStartIndex = 0, -- 从第几条开始取，0表示从最近的第一条开始取
    nAreaid = 0, -- 区号id(填0则请求本人账单)
    nNumid = 0, -- 指定请求的玩家id
    nGameid = 0, -- 指定游戏id
    nFlag = 0, -- 获取类型，账单数据 or 总账单条数

    REQFLAG = {
        RPFG_ALL = 0, -- 请求玩家所有战绩详细数据,回包数据包含所有战绩数据
        RPFG_DAYCNT = 1 -- 仅仅请求玩家某日战绩总数量,回包数据仅仅包含总战绩数量nTotalCnt
    },

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nDay = bis:readInt32()
        self.nCnt = bis:readInt32()
        self.nStartIndex = bis:readInt32()
        self.nAreaid = bis:readInt32()
        self.nNumid = bis:readInt32()
        return self
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nDay)
        bos:writeInt32(self.nCnt)
        bos:writeInt32(self.nStartIndex)
        bos:writeInt32(self.nAreaid)
        bos:writeInt32(self.nNumid)
        bos:writeInt32(self.nGameid)
        bos:writeInt32(self.nFlag)
        return bos
    end
}

ActiveProtocol.RespBoxLedger = {
    XY_ID = CMDT_RESPBOXLEDGER,

    askid = 0,
    ledgerInfo = {},
    nEndTime = 0,
    nTotalCnt = 0,
    nCurrentCnt = 0,
    ordinalIndex = 0, -- 当前条数的索引值

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ledgerInfo = {}
        self.ledgerInfo.appid = bis:readInt32()
        self.ledgerInfo.tableid = bis:readInt32()
        self.ledgerInfo.starttime = bis:readUInt32()
        self.ledgerInfo.roomserialid = bis:readInt32()
        self.ledgerInfo.totalgames = bis:readInt16()
        self.ledgerInfo.finishgames = bis:readInt16()
        self.ledgerInfo.playercnt = bis:readInt16()

        self.ledgerInfo.playerInfos = {}
        for i = 1, self.ledgerInfo.playercnt do
            self.ledgerInfo.playerInfos[i] = {}
            self.ledgerInfo.playerInfos[i].numid = bis:readInt32()
            self.ledgerInfo.playerInfos[i].nickname = bis:readString()
            self.ledgerInfo.playerInfos[i].totalscore = bis:readInt32()
        end
        self.ledgerInfo.config = bis:readString()

        if bis:getAvailableSize() > 0 then
            for i = 1, self.ledgerInfo.playercnt do
                self.ledgerInfo.playerInfos[i].ucType = bis:readUInt8()
            end
        end

        if bis:getAvailableSize() >= 4 then
            self.nEndTime = bis:readInt32()
        end
        self.nTotalCnt = bis:readInt32()
        self.nCurrentCnt = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.ordinalIndex = bis:readInt32()
        end

        -- if bis:getAvailableSize() > 0 then
        --     self.szConfig = bis:readString()
        --     if #self.szConfig > 0  and string.byte(self.szConfig, 1) == 123 --'{'
        --         and string.byte(self.szConfig, #self.szConfig) == 125 then  --'}'
        --         local luaBlock = loadstring("return "..self.szConfig)
        --         local configdict = nil
        --         if luaBlock then
        --             configdict = luaBlock()
        --         end
        --         if configdict and configdict["fee"] then
        --             self.ledgerInfo.fee = tonumber(configdict["fee"])
        --         end
        --     end
        -- end

        self.ledgerInfo.roommode = 0
        self.ledgerInfo.gameID = 0
        self.ledgerInfo.nUsePropCnt = 0
        self.ledgerInfo.roomHostID = 0
        self.ledgerInfo.areaID = 0
        self.ledgerInfo.tid = 0
        -- 消耗类型  4：领队消耗   5：AA消耗
        self.ledgerInfo.nMaiType = 0
        -- {roomNum=9286901;gid=30001;propcnt=3;tabid=19;chid=20001;cltype=0;
        -- ident="41f14saf90003069900000003FD3333379asfafF29";msgsrc=1;roommode=13;
        -- url={'','',};numid={40102019,40102242,};oaid=1000079;onid=72147;tid=4009;
        -- tableid=9286901;FZnumid=40102019;nMaiType=4;}

        if #self.ledgerInfo.config > 0 and string.byte(self.ledgerInfo.config, 1) == 123 -- '{'
        and string.byte(self.ledgerInfo.config, #self.ledgerInfo.config) == 125 then -- '}'
            local luaBlock = loadstring("return " .. self.ledgerInfo.config)
            local configdict = nil
            if luaBlock then
                configdict = luaBlock()
            end

            if configdict then
                if configdict["url"] and configdict["numid"] then
                    for i = 1, self.ledgerInfo.playercnt do
                        for k = 1, self.ledgerInfo.playercnt do
                            if self.ledgerInfo.playerInfos[i].numid == configdict["numid"][k] then
                                self.ledgerInfo.playerInfos[i].szUrl = string.urldecode(configdict["url"][k])
                                break
                            end
                        end
                    end
                end
                if configdict["roomNum"] then
                    local roomNum = tonumber(configdict["roomNum"])
                    if roomNum then
                        self.ledgerInfo.config = string.format("%06d", roomNum)
                    else
                        self.ledgerInfo.config = tostring(configdict["roomNum"])
                    end
                end
                if configdict["roommode"] then
                    self.ledgerInfo.roommode = tonumber(configdict["roommode"])
                end
                if configdict["gid"] then
                    self.ledgerInfo.gameID = tonumber(configdict["gid"])
                end
                if configdict["propcnt"] then
                    self.ledgerInfo.nUsePropCnt = tonumber(configdict["propcnt"])
                end
                if configdict["propsid"] then
                    self.ledgerInfo.nPropid = tonumber(configdict["propsid"])
                end
                if configdict["onid"] then
                    self.ledgerInfo.roomHostID = tonumber(configdict["onid"])

                end
                if configdict["oaid"] then
                    self.ledgerInfo.areaID = tonumber(configdict["oaid"])
                end
                if configdict["tid"] then
                    self.ledgerInfo.tid = tonumber(configdict["tid"])
                end
                if configdict["nMaiType"] then
                    self.ledgerInfo.nMaiType = tonumber(configdict["nMaiType"])
                    if self.ledgerInfo.nMaiType == 4 then
                        self.ledgerInfo.nMaiType = 0
                    elseif self.ledgerInfo.nMaiType == 5 then
                        self.ledgerInfo.nMaiType = 1
                    end
                end
                if configdict["nPayType"] then
                    self.ledgerInfo.nPayType = tonumber(configdict["nPayType"])
                end
                -- 限时免费字段
                if configdict["LimitFree"] then
                    self.ledgerInfo.limitFree = tonumber(configdict["LimitFree"])
                end
                -- 拓展信息（麻将的杠分、点杠次数等，斗地主地主次数等，全垒打次数等）
                if configdict["endInfoEx"] then
                    self.ledgerInfo.endInfoEx = {}
                    local endInfoExDict = string.split(configdict["endInfoEx"], "|")
                    for _, item in pairs(endInfoExDict) do
                        local luaBlockExt = loadstring("return { " .. item .. "}")
                        local endInfoExDictItem
                        if luaBlockExt then
                            endInfoExDictItem = luaBlockExt()
                            if endInfoExDictItem then
                                self.ledgerInfo.endInfoEx[tostring(endInfoExDictItem["NID"])] = endInfoExDictItem
                            end
                        end
                    end
                end
                if configdict["prevaa"] then
                    self.ledgerInfo.prevaa = tonumber(configdict["prevaa"])
                end
            end
        end
        return self
    end
}

ActiveProtocol.ReqSelectTeaBillInfo = {
    XY_ID = 35,

    PLAYER_TYPE = {
        PLAYER_ALL = 0, --  全部玩家
        PLAYER_BIGWINNER = 1 --  仅冠军    
    },

    nAskid = 0, --  请求id
    nTeaid = 0,
    nDay = 0, --  (0：当天  1：前1天  2:前2天）
    nMinScore = 0, --  冠军分数（查询大于等于该分数）
    nGameLobbyId = 0, --  虚拟大厅id

    nMaxScore = -1, --  冠军分数上限（查询小于等于该分数，-1为无上限），与MinScore共同限制
    nStartTime = 0, --  查询时间范围(起始)    使用该范围时无视nDay值
    nEndTime = 0, --  查询时间范围(结束)
    nSingleNumid = 0, --  查询单个玩家 0为无效
    nPlayerType = 0, --  玩家类型

    nGameId = 0, --  游戏id

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nTeaid)
        bos:writeInt32(self.nDay)
        bos:writeInt32(self.nMinScore)
        bos:writeInt32(self.nGameLobbyId)
        bos:writeInt32(self.nMaxScore)
        bos:writeInt32(self.nStartTime)
        bos:writeInt32(self.nEndTime)
        bos:writeInt32(self.nSingleNumid)
        bos:writeInt32(self.nPlayerType)
        bos:writeInt32(self.nGameId)
        return bos
    end
}

ActiveProtocol.RespSelectTeaBillInfo = {
    XY_ID = 36,

    ucFlag = 0,
    FLAG = {
        SUCCESS = 0,
        ERROR = 1,
        FILE_NOT_EXIST = 2, -- 文件不存在
        NO_FILE = 3 -- 没有文件名
    },

    nAskid = 0,
    nTotalCnt = 0, -- 下发数据总条数
    nCurrentCnt = 0, -- 当前第几条
    data = {},
    nJuShu = -1,
    nBigWin = -1,
    szConfig = "",
    nBigWinScore = 0,
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.ucFlag = bis:readUInt8()
        self.nAskid = bis:readInt32()
        self.nTotalCnt = bis:readInt32()
        self.nCurrentCnt = bis:readInt32()
        self.data = {
            nickname = bis:readString(),
            numid = bis:readInt32(),
            url = bis:readString(),
            totalRoomCnt = bis:readInt32(), -- 查询时间内的总场次
            bigWinCnt = bis:readInt32(), -- 查询时间内且冠军分数大于等于设定的冠军次数
            totalScore = bis:readInt32(),
            totalWinScore = bis:readInt32(), -- 选中日期内的所有场次胜分
            totalLoseScore = bis:readInt32() -- 选中日期内的所有场次负分
        }
        if bis:getAvailableSize() > 0 then
            self.nJuShu = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nBigWin = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.szConfig = bis:readString()
        end
        if bis:getAvailableSize() > 0 then
            self.data.nBigWinScore = bis:readInt32()
        end
    end
}

for _, v in pairs(ActiveProtocol) do
    v.processid = 30
end
ActiveProtocol.processid = 30

return ActiveProtocol
 ��  