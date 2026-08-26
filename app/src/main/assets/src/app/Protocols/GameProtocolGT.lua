local GameProtocolGT = {}
local MAXUSERID	    =	50			--最大边锋用户名字符个数
local MAXNICKNAME	=	20			--最大绰号字符个数
local MAXUSERNAME   =  20          --最大用户名字符个数

local CMDT_FIRST_CLIENT_GAME = 11000 -- 游戏协议起始编号

local CMDT_REQJOINROOM = CMDT_FIRST_CLIENT_GAME + 7
local CMDT_RESPJOINROOM = CMDT_FIRST_CLIENT_GAME + 8
local CMDT_PLAYERINFO = CMDT_FIRST_CLIENT_GAME + 9
local CMDT_STARTGAME = CMDT_FIRST_CLIENT_GAME + 13
local CMDT_REQPLAYERACT = CMDT_FIRST_CLIENT_GAME + 16
local CMDT_RESPPLAYERACT = CMDT_FIRST_CLIENT_GAME + 17
local CMDT_REQLEAVEROOM = CMDT_FIRST_CLIENT_GAME + 73
local CMDT_RESPLEAVEROOM = CMDT_FIRST_CLIENT_GAME + 74
local CMDT_ENTERROOMFINISH	=	CMDT_FIRST_CLIENT_GAME+41
local CMDT_REQSR            = CMDT_FIRST_CLIENT_GAME + 62    --(向游戏服务请求刷新银子)请求银子数量(c->s)
local CMDT_RESPSR            = CMDT_FIRST_CLIENT_GAME + 63    --（游戏服务）返回银子数量(s->c)

local SvrDef = require("app.Define.SvrDef")
GameProtocolGT.ReqJoinRoom = {
    XY_ID = CMDT_REQJOINROOM,
    CLIENTTYPE =  {
        PC = 0,
        TV = 1,
        MOBILE = 2,
        WEB = 3,
    },

    HARDWAREFLAG =  {
        HF_NONE         =0,
        HF_CAMERA       =0x01,  -- 摄像头
        HF_MICROPHONE   =0x02,  -- 麦克风
        HF_JOYPAD       =0x04,  -- 游戏手柄，不区分有线无线
        HF_CLIENT_EXTRA =0x08,  -- 用于千变双扣中区分结算类型（积分 or 财富）
    },
    roomID = 0,
    clientType = 0,
    hardWareFlag = 0,
    ver = 0,
    dataSize = 0,
    data = "",

    channelid  =  0,
    m_ucType = 0,       --标示客户端游戏方式
    m_osver = 0,--bf.LuaSysFunc:GetOsVersion(),--标示客户端游戏方式
    m_Identify = "",
    m_breconnect = false,
    
    new = function (self, o)
        local o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function (self)
        local bos = un.network.IStream.new()
        print("CMDT_REQJOINROOM:"..self.roomID)
        bos:writeUInt32(self.roomID)
        bos:writeUInt32(self.clientType)
        bos:writeUInt32(self.hardWareFlag)
        bos:writeUInt32(self.ver)
        bos:writeUInt16(self.dataSize)
        if self.dataSize > 0 then
            bos:write(self.data, self.dataSize)
        end
    
        bos:writeUInt32(self.channelid)
        bos:writeInt8(self.m_ucType)
        bos:writeUInt32(self.m_osver)
        bos:writeString(self.m_Identify)
        bos:writeBool(self.m_breconnect)
        return bos
    end,
}

GameProtocolGT.RespJoinRoom = {
    XY_ID = CMDT_RESPJOINROOM,

    JOINROOMFLAG = {
        SUCCESS = 0,
        SHOW_MESSAGE = 1,   --错误由MsgBox定义
        --PLAYERFULL = 1,
        RECONNECTNOINROOM = 2,-- 重连时, 玩家不在房间
        RECONNECTNOINTABLE = 3,-- 重连时, 玩家不在桌子
        OTHERERR = 4,
        --        NOTLEAVEOLDROOM = 5,
        --        NOROOM = 6,
        --        NOTOPEN = 7,
        --        CONDITIONERR = 8,
        --        MUCHCONNECT = 9,  --重连次数过多
        --        MUCHBREAK = 10,     --断线次数过多

        -- 大于20,来自记分
        REPORTSTATUSFAIL = 21,
        REPORTSTATUSTIMEOUT = 22,
        REPORTSTATUSSENDFAIL = 23,
        DENYLOGIN = 24,
        GAMETABLENOTFIND = 25,
        DBCONNNOTFIND = 26,
        NOMATCHDATA = 27,
        NETBUSY = 28,
        DBERR = 29,
        MAXPLAYSR = 30,
    },

    JOINROOMTYPE = {
        JOIN = 0,   --进房间
        CHANGE = 1,     --换房间
        REJOIN = 2,     --重新进入该房间
    },
    flag = 0,
    type = 0,
    roomID = 0,
    msgbox = SvrDef.MsgBox:new(),
    inappID = 0,

    new = function (self, o)
        local o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function (self,buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.flag = bis:readUInt8()
        self.type = bis:readUInt8()
        self.roomID = bis:readUInt32()
        if self.flag == GameProtocolGT.RespJoinRoom.JOINROOMFLAG.SHOW_MESSAGE then
            self.msgbox.m_type = bis:readUInt8()
            self.msgbox.m_szCaption = bis:readString()
            self.msgbox.m_szText = bis:readString()
            self.msgbox.m_dwIconBtn = bis:readUInt32()
            self.msgbox.m_delay = bis:readUInt8()
            self.msgbox.m_colour = bis:readUInt32()
            self.msgbox.m_szWeb = bis:readString()
            self.msgbox.m_dwAction = bis:readUInt8()
        end

        if bis:getAvailableSize() > 0 then
            self.inappID = bis:readInt32()
        end
                return self
            end
}

GameProtocolGT.ReqPlayerAct = 
{
    XY_ID = CMDT_REQPLAYERACT,

    ACTION = 
    {
        NONE = 0,
        SITDOWN = 1,                --坐下
        STANDUP = 2,                --站起
        READY = 3,                  --房间模块按了开始
        SEEGAME = 4,                --普通旁观(桌上有人才可旁观)
        CHANGETOSTART = 5,          --桌子里面按了开始
        CHANGETOSEEGAME = 6,        --坐着的变成旁观
        CHANGESEAT = 7,             --换座位
        LEAVEROOM = 8,              --离开房间
        SEEGAME2 = 9,               --新增旁观(空桌也可旁观)
    },

    askid = 0,
    action = 0,
    tableID = 0,
    sitorder = 0,
    password = "",
    
    new = function (self, o)
        local o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function (self)
        local bos = un.network.IStream.new()

        bos:writeUInt32(self.action)       -- 修改bos:WriteUChar(self.action)为bos:writeUInt32(self.action)
        bos:writeUInt16(self.tableID)      -- 修改bos:WriteUShort(self.tableID)为bos:writeUInt16(self.tableID)
        bos:writeInt8(self.sitorder)       -- 修改bos:WriteUChar(self.sitorder)为bos:writeInt8(self.sitorder)
        bos:writeString(self.password)     -- 修改bos:WriteCString(self.password)为bos:writeString(self.password)
        bos:writeInt32(self.askid)         -- 修改bos:WriteInt(self.askid)为bos:writeInt32(self.askid)

        return bos
    end,
}

GameProtocolGT.RespPlayerAct = 
{
    XY_ID = CMDT_RESPPLAYERACT,
    
    FLAG = 
    {
        SUCCESS = 0,
        SHOW_MESSAGE = 1,              --错误由MsgBox定义
    },
    
    askid = 0,
    flag = 0,
    msgbox = nil,
    
    new = function (self, o)
        local o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function (self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.flag = bis:readUInt8()  -- 修改bis:ReadUChar()为bis:readUInt8()

        if self.flag == GameProtocolGT.RespPlayerAct.FLAG.SHOW_MESSAGE then
            self.msgbox = SvrDef.MsgBox:new()
            self.msgbox.m_type = bis:readUInt8()  -- 修改bis:ReadUChar()为bis:readUInt8()
            self.msgbox.m_szCaption = bis:readString()  -- 修改bis:ReadCharPtr()为bis:readString()
            self.msgbox.m_szText = bis:readString()  -- 修改bis:ReadCharPtr()为bis:readString()
            self.msgbox.m_dwIconBtn = bis:readUInt32()  -- 修改bis:ReadUInt()为bis:readUInt32()
            self.msgbox.m_delay = bis:readUInt8()  -- 修改bis:ReadUChar()为bis:readUInt8()
            self.msgbox.m_colour = bis:readUInt32()  -- 修改bis:ReadUInt()为bis:readUInt32()
            self.msgbox.m_szWeb = bis:readString()  -- 修改bis:ReadCharPtr()为bis:readString()
            self.msgbox.m_dwAction = bis:readUInt8()  -- 修改bis:ReadUChar()为bis:readUInt8()
        end

        if bis:getAvailableSize() > 0 then
            self.askid = bis:readInt32()  -- 修改bis:ReadInt()为bis:readInt32()
        end

        return self
    end
}

GameProtocolGT.ReqLeaveRoom = 
{
    XY_ID = CMDT_REQLEAVEROOM,


    new = function (self, o)
        local o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function (self)
        local bos = un.network.IStream.new()
        return bos
    end,
}

GameProtocolGT.RespLeaveRoom = 
{
    XY_ID = CMDT_RESPLEAVEROOM,
    LEAVEROOMTYPE = {
        NORMAL = 0,
        KICK = 1,
    },

    type = 0,

    new = function (self, o)
        local o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function (self,buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.type = bis:readUInt8()
        return self
    end
}

GameProtocolGT.PlayerInfo =
{
    XY_ID = CMDT_PLAYERINFO,
    SEX =
    {
        FEMALE = 0,
        MALE = 1
    },
    CLIENTTYPE =
    {
        PC = 0,
        TV = 1,
        MOBILE = 2,
        WEB = 3
    },
    HARDWAREFLAG =
    {
        HF_NONE = 0,
        HF_CAMERA = 1,
        HF_MICROPHONE = 2,
        HF_JOYPAD = 4
    },

    m_brandid = 0,
    m_numid = 0,

    m_customerid = "",
    m_nickname = "",
    m_right = 0,
    m_sex = 0,
    m_sr = 0,
    m_jf = 0,
    m_jy = 0,
    m_Win = 0,
    m_Lost = 0,
    m_Peace = 0,
    m_Escape = 0,
    m_bhid = 0,
    m_bhname = 0,
    m_state = 0,
    m_tableorder = 0,
    m_sitorder = 0,
    m_netspeed = 0,
    m_headid = 0,
    m_vipid = 0,
    m_clienttype = 0,
    m_hardwareflag = 0,
    m_pictureid = 0,
    m_typescore = 0,

    -- add ptnumid
    m_ptnumid = "",

    m_osver = 0,

    new = function (self, o)
        local o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function (self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.m_brandid = bis:readUInt32()
        self.m_numid = bis:readUInt32()

        local nlen = bis:readUInt8()
        if nlen > MAXUSERID then
            return
        end
        self.m_customerid = bis:read(nlen)
        local nlen = bis:readUInt8()
        if nlen > MAXNICKNAME then
            return
        end
        self.m_nickname = bis:read(nlen)
        self.m_right = bis:readInt32()
        self.m_sex = bis:readUInt8()
        self.m_sr = bis:readInt64()
        self.m_jf = bis:readInt64()
        self.m_jy = bis:readInt32()
        self.m_Win = bis:readInt32()
        self.m_Lost = bis:readInt32()
        self.m_Peace = bis:readInt32()
        self.m_Escape = bis:readInt32()
        self.m_bhid = bis:readInt32()
        local nlen = bis:readUInt8()
        if nlen >= string.len(self.m_bhname) then
            return
        end
        self.m_bhname = bis:read(nlen)
        self.m_state = bis:readInt16()
        self.m_tableorder = bis:readUInt16()
        self.m_sitorder = bis:readUInt8()
        self.m_netspeed = bis:readInt16()
        self.m_headid = bis:readInt32()
        self.m_vipid = bis:readInt32()
        self.m_clienttype = bis:readInt32()
        self.m_hardwareflag = bis:readInt32()
        self.m_pictureid = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.m_typescore = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.m_ptnumid = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.m_osver = bis:readInt32()
        end
        return self
    end
}

GameProtocolGT.EnterRoomFinish =
{
    XY_ID = CMDT_ENTERROOMFINISH,

    m_roomid = 0,        --房间编号

    new = function (self, o)
        local o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function (self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.m_roomid = bis:readUInt32()

        return self
    end
}

--请求银行和钱包的银子
GameProtocolGT.ReqSR = {
    XY_ID = CMDT_REQSR,

    mode = '0',
    areaid = 0,
    numid = 0,

    new = function(self, o)
        local o = o or {}
        setmetatable(o, { __index = self })
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeUInt8(self.mode)
        bos:writeInt32(self.areaid)
        bos:writeInt32(self.numid)
        return bos
    end,
}

GameProtocolGT.RespSR = {
    XY_ID = CMDT_RESPSR,
    FLAG =    {
        SUCCESS = 0,
        DBERR = 1, -- 数据库异常
        GAMETABLENOTFIND = 2, -- 数据库异常
        DBCONNNOTFIND = 3, -- 数据库异常
        NOTSUPPORT = 20, -- 游戏服务不支持划账
        SERVING = 21, -- 服务中, 即上次请求还在继续中
        SHOULDSTAND = 22, -- 只有站着才可以转账
        OVERMAXSR = 23, -- 超过最大限额
        NOROOM    = 24, -- 没有进入房间
        ERRAREAID = 25, -- 错误区号，一般为2区
    },

    flag = '0',
    purse = 0, -- 当前钱包财富
    bank = 0, -- 当前银行财富
    modifyblankpwd = '0', -- 需要改掉空密码  1=need modify

    new = function(self, o)
        local o = o or {}
        setmetatable(o, { __index = self })
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.flag = bis:readUInt8()
        self.purse = bis:readInt64()
        self.bank = bis:readInt64()
        self.modifyblankpwd = bis:readUInt8()
        return self
    end
}

GameProtocolGT.StartGame = {
    XY_ID = CMDT_STARTGAME,

    canStart = 0,

    new = function(self, o)
        local o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.canStart = bis:readUInt8()

        return self
    end
}

for _, v in pairs(GameProtocolGT) do
    v.processid = 140
end
GameProtocolGT.processid = 140

return GameProtocolGT�7  