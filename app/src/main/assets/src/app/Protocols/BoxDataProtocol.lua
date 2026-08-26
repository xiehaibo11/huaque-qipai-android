-- 游戏协议模块
local BoxDataProtocol = {}

local CMDT_REQREALNAMEAUTH = 1 -- 防沉迷请求
local CMDT_RESPREALNAMEAUTH = 2 -- 防沉迷回调

local CMDT_REQUSERTAG = 5
local CMDT_RESPUSERTAG = 6

local CMDT_REQBINDID = 13 -- 控制器绑定固定id
local CMDT_RESPBINDID = 14

-- 33转发50protobuf协议
local CMDT_REQTRANST050 = 5050
-- 10000开始为客户端请求比赛场相关列表数据
local CMDT_REQRANKUSERINFO = 10000 -- 请求按某个维度排序的玩家排行榜信息
local CMDT_RESPRANKUSERINFO = 10001 -- 返回按某个维度排序的玩家排行榜信息

local CMDT_REQTEAMEMBERLIST = 10002 -- 请求比赛场成员列表
local CMDT_RESPTEAMEMBERLIST = 10003 -- 返回比赛场成员列表
local CMDT_REQTEAHOUSEINFO = 10006 -- 请求比赛场列表
local CMDT_RESPTEAHOUSEINFO = 10007 -- 返回比赛场列表
local CMDT_REQTEAHOUSETABLEINFO = 10012 -- 请求玩家比赛场桌子信息
local CMDT_RESPTEAHOUSETABLEINFO = 10013 -- 返回玩家比赛场桌子信息
local CMDT_REQSELFFIGHTINFO = 10014 -- 请求个人比赛场对战时间信息
local CMDT_RESPSELFFIGHTINFO = 10015 -- 返回个人比赛场对战时间信息

local CMDT_REQGETCONFIGDATAEX = 15; -- 获取风信写入到redis中的配置数据 支持chanelid维度key去获取文件地址和md5
local CMDT_RESPGETCONFIGDATAEX = 16;
local CMDT_REQROOMPLAYERCOUNT = 17; -- 获取房间人数
local CMDT_RESPROOMPLAYERCOUNT = 18;
local CMDT_REQGETUSEEMOJIPROPINFO    = 19; --是否使用过互动道具
local CMDT_RESPGETUSEEMOJIPROPINFO   = 20;

local CMDT_REQHEARTBEAT = 23  -- 新实名认证,游客登陆心跳
local CMDT_RESPHEARTBEAT = 24 -- 心跳回调

local CMDT_REQOTHERUSERINFO = 25 --获取其他玩家信息
local CMDT_RESPOTHERUSERINFO = 26

local CMDT_REQGETBANINFO = 27 --获取禁止状态
local CMDT_RESPGETBANINFO = 28
-- 统一错误码
BoxDataProtocol.ERRORCODE = {SUCCESS = 0}

BoxDataProtocol.TransTo50Data = {
     XY_ID = CMDT_REQTRANST050,

    nAskId = 0,
    nToProcessid = 0,
    nToXY_ID = 0,
    nXYData = nil,
    nErrCode = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        bos:writeInt32(self.nToProcessid)
        bos:writeInt32(self.nToXY_ID)
        bos:writeString(self.nXYData)
        bos:writeInt32(self.nErrCode)

        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskId = bis:readInt32()
        self.nToProcessid = bis:readInt32()
        self.nToXY_ID = bis:readInt32()
        self.nXYData = bis:readString()
        self.nErrCode = bis:readInt32()
        return self
    end
}

BoxDataProtocol.ReqRealNameAuth = {
    XY_ID = CMDT_REQREALNAMEAUTH,

    nAskId = 0,
    nChannel = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        bos:writeInt32(self.nChannel)

        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskId = bis:readInt32()
        self.nChannel = bis:readInt32()
        return self
    end

}

BoxDataProtocol.RespRealNameAuth = {
    XY_ID = CMDT_RESPREALNAMEAUTH,

    nAskId = 0,
    nPassiveValue = 0, -- 被动认证结果 
    nActiveValue = 0, -- 主动认证结果, 玩家点击认证按钮时使用的身份验证规则
    nConfigVale = 0, -- 右起第一位表示 是否绑定，第二位表示是否强弹(针对海外玩家)

    BITCONFIG = {
        HAS_BIND = 64,
        OVERSEA_SHOWPOP = 63
    },

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        bos:writeInt16(self.nPassiveValue)
        bos:writeInt16(self.nActiveValue)
        bos:writeUInt8(self.nConfigVale)
        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskId = bis:readInt32()
        self.nPassiveValue = bis:readInt16()
        self.nActiveValue = bis:readInt16()
        if bis:getAvailableSize() > 0 then
            self.nConfigVale = bis:readUInt8()
        end
        return self
    end
}

-- 控制器绑定
BoxDataProtocol.ReqBindID = {
    XY_ID = CMDT_REQBINDID,

    askid = 0,

    id = "", -- char id[50]

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeString(self.id)
        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.id = bis:readString()
        return self
    end

}

BoxDataProtocol.RespBindID = {
    XY_ID = CMDT_RESPBINDID,

    askid = 0,
    flag = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.flag)
        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.flag = bis:readInt32()
        return self
    end
}

BoxDataProtocol.ReqUserTag = {
    XY_ID = CMDT_REQUSERTAG,

    TAGTYPE = {
        TTY_ALL = 0,
        TTY_LOGINLOBBY = 0x01,
        TTY_PRODUCT = 0x02,
        TTY_GAMES = 0x04,
        TTY_PAY = 0x08,
        TTY_CARDCHANNEL = 0x10,
        TTY_CONSUMER = 0x20
    },

    nAskId = 0,

    tag_type = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        bos:writeInt8(self.tag_type)
        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskId = bis:readInt32()
        self.tag_type = bis:readInt8()
        return self
    end

}

BoxDataProtocol.UserTagInfo = {
    tag_id = 0,
    update_time = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end
}

BoxDataProtocol.RespUserTag = {
    XY_ID = CMDT_RESPUSERTAG,

    nAskId = 0,

    tag_type = 0,

    total_packages = 0,
    cur_index = 0,
    tags = 0,
    UserTagInfo = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        bos:writeInt8(self.tag_type)
        bos:writeUInt16(self.total_packages)
        bos:writeUInt16(self.cur_index)
        bos:writeUInt16(self.tags)
        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskId = bis:readInt32()
        self.tag_type = bis:readInt8()
        self.total_packages = bis:readUInt16()
        self.cur_index = bis:readUInt16()
        self.tags = bis:readUInt16()

        for i = 1, self.tags do
            local info = BoxDataProtocol.UserTagInfo:new()
            info.tag_id = bis:readInt32()
            info.update_time = bis:readUInt32()
            self.UserTagInfo[i] = info
        end
        return self
    end
}

BoxDataProtocol.ReqRankUserInfo = {
    XY_ID = CMDT_REQRANKUSERINFO,

    nAskId = 0,
    nTeaId = 0,
    nStartIndex = 0,
    nCount = 0,
    nBigSmall = 0,
    acRankMode = "",
    nStartDay = 0,
    nEndDay = 0,
    bFilter = false,
    nReqExtraInfo = 0,
    nGameId = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        bos:writeInt32(self.nTeaId)
        bos:writeInt32(self.nStartIndex)
        bos:writeInt32(self.nCount)
        bos:writeInt32(self.nBigSmall)
        bos:writeString(self.acRankMode)
        bos:writeInt32(self.nStartDay)
        bos:writeInt32(self.nEndDay)
        bos:writeBool(self.bFilter)
        bos:writeInt32(self.nReqExtraInfo)
        bos:writeInt32(self.nGameId)
        return bos
    end
}

BoxDataProtocol.CustomUserInfo = {
    EXTRAINFO = {
        LAST_GAMETIME = 1, -- 第1位：获取玩家最后游戏时间
        GAMECOUNT = 2, -- 第2位：获取请求玩家游戏局数
        WINNERCOUNT = 4 -- 第3位：获取请求玩家冠军次数
    },

    STATE = {
        ST_OFFLINE = 0, -- 离线玩家
        ST_ONLINE = 1, -- 在线
        ST_GAME = 2 -- 在游戏中
    },

    nAreaId = 0,
    nNumId = 0,
    acHeadUrl = "",
    acNickName = "",
    acExtraData = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end
}

BoxDataProtocol.RespRankUserInfo = {
    XY_ID = CMDT_RESPRANKUSERINFO,

    RETVAL = {
        OK = 0, -- 找到
        NOT_FOUND = 1, -- 未找到
        TEA_NOTEXIST = 2, -- 比赛场不存在
        PARAM_ERROR = 3, -- 参数错误
        NO_RIGHT = 4 -- 没有权限
    },

    ucFlag = 0,
    nErrorCode = 0,
    nAskId = 0,
    nTeaId = 0,
    nTotal = 0,
    nCurIndex = 0,
    acRankMode = "",
    nScore = 0,
    nRank = 0,
    nGameId = 0,
    nAllScore = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.ucFlag = bis:readUInt8()
        self.nErrorCode = bis:readInt32()
        self.nAskId = bis:readInt32()
        self.nTeaId = bis:readInt32()
        self.nTotal = bis:readInt32()
        self.nCurIndex = bis:readInt32()
        self.acRankMode = bis:readString()
        self.nScore = bis:readInt32()
        self.nRank = bis:readInt32()
        self.nGameId = bis:readInt32()
        self.stUser = BoxDataProtocol.CustomUserInfo:new()
        self.stUser.nAreaId = bis:readInt32()
        self.stUser.nNumId = bis:readInt32()
        self.stUser.acHeadUrl = bis:readString()
        self.stUser.acNickName = bis:readString()
        self.stUser.acExtraData = bis:readString()
        self.nAllScore = bis:readInt32()
        return self
    end
}

BoxDataProtocol.ReqGetConfigDataEx = {
    XY_ID = CMDT_REQGETCONFIGDATAEX,

    askid = 0,
    areaid = 0,
    channelid = 0,
    gameid = 0,
    filename = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.areaid)
        bos:writeInt32(self.channelid)
        bos:writeInt32(self.gameid)
        bos:writeString(self.filename)
        return bos
    end
}

BoxDataProtocol.RespGetConfigDataEx = {
    XY_ID = CMDT_RESPGETCONFIGDATAEX,

    askid = 0,
    data = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.data = bis:readString()
        return self
    end
}

BoxDataProtocol.ReqRoomPlayerCount = {
    XY_ID = CMDT_REQROOMPLAYERCOUNT,
    PLAYTYPE = {
        PT_ALLMODE = 0x00,
        PT_SIMPLEMODE = 0x01,
        PT_CHEATMODE = 0x02,
        PT_PRIVATEMODE = 0x04,
        PT_TEAHOUSEMODE = 0x08
    },
    ROOMLEVEL = {
        RL_ALLLEVEL = 0x00,
        RL_LEVEL0 = 0x01,
        RL_LEVEL1 = 0x02,
        RL_LEVEL2 = 0x04
    },

    askid = 0,
    gameid = 0, -- 必填
    playtype = 0, -- 选填, 默认为0,所有的游戏玩法, 可组合 PT_SIMPLEMODE|PT_CHEATMODE
    level = 0, -- 选填, 默认为0,所有的场次组合, 可组合 RL_LEVEL0 | RL_LEVEL1
    appid = 0, -- 选填, 默认为0,获取符合前面条件的房间人数，若指定appid，仅获取该房间人数

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.gameid)
        bos:writeInt32(self.playtype)
        bos:writeInt32(self.level)
        bos:writeInt32(self.appid)

        return bos
    end
}

BoxDataProtocol.RespRoomPlayerCount = {
    XY_ID = CMDT_RESPROOMPLAYERCOUNT,

    askid = 0,
    gameid = 0,
    playtype = 0,
    level = 0,
    appid = 0,
    playercount = 0,
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.gameid = bis:readInt32()
        self.playtype = bis:readInt32()
        self.level = bis:readInt32()
        self.appid = bis:readInt32()
        self.playercount = bis:readInt32()
        return self
    end
}

BoxDataProtocol.PropUseLastTime = 
{
    propid = 0,
    lastime = 0,

    new = function (self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
}

BoxDataProtocol.ReqGetUseEmojiPropInfo = {
    XY_ID = CMDT_REQGETUSEEMOJIPROPINFO,
    askid = 0,          
    count = 0,   
    propids = {},      

    new = function ( self, o )
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function( self )
        local bos = un.network.IStream.new()
        bos:writeInt32( self.askid  )
        bos:writeInt32( self.count )
        for i=1,self.count do
           bos:writeInt32(self.propids[i])
        end
        
        return bos
    end,
}

BoxDataProtocol.RespGetUseEmojiPropInfo = {
    XY_ID = CMDT_RESPGETUSEEMOJIPROPINFO,
    askid       = 0,
    flag        = 0,
    count       = 0,
    PropUseLastTime = {},

    new = function ( self, o )
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function( self, buff, bufflen )
        local bis = un.network.OStream.new(buff)
        self.PropUseLastTime = {}
        self.askid      = bis:readInt32()
        self.flag       = bis:readInt32()
        self.count      = bis:readInt32()
        for i = 1 ,self.count do
            local info = {}
            info.propid  = bis:readInt32()
            info.lastime = bis:readInt32()
            self.PropUseLastTime[i] = info
        end
        return self
    end
}

BoxDataProtocol.ReqTeaMemberList = {
    XY_ID = CMDT_REQTEAMEMBERLIST,

    nAskId = 0,          
    nTeaId = 0,   
    bReqOnLine = false,
    bGetOwner = false,
    nReqExtraInfo = 0,

    new = function ( self, o )
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function( self )
        local bos = un.network.IStream.new()
        bos:writeInt32( self.nAskId )
        bos:writeInt32( self.nTeaId )
        bos:writeBool( self.bReqOnLine )
        bos:writeBool( self.bGetOwner )
        bos:writeInt32( self.nReqExtraInfo )
        
        return bos
    end,
}

BoxDataProtocol.RespTeaMemberList = {
    XY_ID = CMDT_RESPTEAMEMBERLIST,

    ucFlag = 0,
    nErrorCode = 0,
    nAskId = 0,
    nTeaId = 0,
    nReqExtraInfo = 0,
    nOnLineCnt = 0,
    nTotal = 0,
    nCurIndex = 0,
    stUserInfo = {},

    RETVAL = {
        OK = 0,         -- 找到
        PARAM_ERR = 1,  -- 参数错误
        NO_DATAS = 2,   -- 没有数据
        NO_RIGHT = 3,   -- 没有权限
        GET_ERR = 4,    -- 获取数据失败
    },

    new = function ( self, o )
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function( self, buff, bufflen )
        local bis = un.network.OStream.new(buff)
        self.ucFlag = bis:readUInt8()
        self.nErrorCode = bis:readInt32()
        self.nAskId = bis:readInt32()
        self.nTeaId = bis:readInt32()
        self.nReqExtraInfo = bis:readInt32()
        self.nOnLineCnt = bis:readInt32()
        self.nTotal = bis:readInt32()
        self.nCurIndex = bis:readInt32()
        self.stUserInfo = {}
        self.stUserInfo.nAreaId = bis:readInt32()
        self.stUserInfo.nNumId = bis:readInt32()
        self.stUserInfo.acHeadUrl = bis:readString()
        self.stUserInfo.acNickName = bis:readString()
        self.stUserInfo.acExtraData = bis:readString()
        return self
    end
}

BoxDataProtocol.ReqLatelyFightInfo = {
    XY_ID = CMDT_REQSELFFIGHTINFO,

    nAskId = 0,
    nTeaId = 0,
    nReqExtraInfo = 0,

    new = function ( self, o )
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function( self )
        local bos = un.network.IStream.new()
        bos:writeInt32( self.nAskId  )
        bos:writeInt32( self.nTeaId )
        bos:writeInt32( self.nReqExtraInfo )
        
        return bos
    end,
}

BoxDataProtocol.RespLatelyFightInfo = {
    XY_ID = CMDT_RESPSELFFIGHTINFO,

    ucFlag = 0,
    nAskId = 0,
    nTeaId = 0,
    nLastFightTime = 0,
    nTotal = 0,
    nCurIndex = 0,
    stUserInfo = {},
    nErrorCode = 0,

    RETVAL = {
        OK = 0,         -- 找到
        PARAM_ERR = 1,  -- 参数错误
        NO_DATAS = 2,   -- 没有数据
        NO_RIGHT = 3,   -- 没有权限
    },
    MAX_COUNT = 100,

    new = function ( self, o )
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function( self, buff, bufflen )
        local bis = un.network.OStream.new(buff)
        self.ucFlag = bis:readUInt8()
        self.nAskId = bis:readInt32()
        self.nTeaId = bis:readInt32()
        self.nLastFightTime = bis:readInt32()
        self.nTotal = bis:readInt32()
        self.nCurIndex = bis:readInt32()
        self.stUserInfo = {}
        self.stUserInfo.nAreaId = bis:readInt32()
        self.stUserInfo.nNumId = bis:readInt32()
        self.stUserInfo.acHeadUrl = bis:readString()
        self.stUserInfo.acNickName = bis:readString()
        self.stUserInfo.acExtraData = bis:readString()
        self.nErrorCode = bis:readInt32()
        return self
    end
}

BoxDataProtocol.ReqTeaHouseInfo = {
    XY_ID = CMDT_REQTEAHOUSEINFO,

    askid = 0,
    reqbit = 0,

    EXTRAINFO = {
        USER_CNT            = 1,   -- 第1位：获取比赛场玩家数
        HAVE_CAN_JOIN_TABLE = 2,   -- 第2位：获取 有可以加入的有人的正在等待中的桌子的比赛场
    },

    new = function ( self, o )
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function( self )
        local bos = un.network.IStream.new()
        bos:writeInt32( self.askid  )
        bos:writeInt32( self.reqbit )
        return bos
    end,
}

BoxDataProtocol.RespTeaHouseInfo = {
    XY_ID = CMDT_RESPTEAHOUSEINFO,

    askid = 0,
    current = 0, -- 当前是第几个，从1开始
    total = 0, -- 总共有几个
    teaInfo = {}, -- 比赛场信息

    new = function ( self, o )
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function( self, buff, bufflen )
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.current = bis:readInt32()
        self.total = bis:readInt32()
        self.teaInfo = {}
        self.teaInfo.tea_appid = bis:readInt32()
        self.teaInfo.teaid = bis:readInt32()
        self.teaInfo.teaname = bis:readString()
        self.teaInfo.passwd = bis:readInt32()
        self.teaInfo.url = bis:readString()
        self.teaInfo.data = bis:readString()
        self.teaInfo.state = bis:readUInt8()
        self.teaInfo.owner_areaid = bis:readInt32()
        self.teaInfo.owner_numid = bis:readInt32()
        self.teaInfo.owner_nick = bis:readString()
        self.teaInfo.extrainfo = bis:readString()
        return self
    end,
}

BoxDataProtocol.ReqTeaHouseTableInfo = {
    XY_ID = CMDT_REQTEAHOUSETABLEINFO,

    askid = 0,
    teaid = 0,
    cur_package = 0,

    new = function ( self, o )
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function( self )
        local bos = un.network.IStream.new()
        bos:writeInt32( self.askid  )
        bos:writeInt32( self.teaid )
        bos:writeUInt16( self.cur_package )
        return bos
    end,
}

BoxDataProtocol.RespTeaHouseTableInfo = {
    XY_ID = CMDT_RESPTEAHOUSETABLEINFO,

    askid = 0,
    ec = 0,
    teaid = 0,
    total_package = 0,
    cur_package = 0,
    count = 0,
    tableInfos = {},

    new = function ( self, o )
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function( self, buff, bufflen )
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.teaid = bis:readInt32()
        self.total_package = bis:readUInt16()
        self.cur_package = bis:readUInt16()
        self.count = bis:readUInt8()
        self.tableInfos = {}
        for i = 1, self.count do
            local tmp = {}
            tmp.game_roomid = bis:readInt32()
            tmp.game_appid = bis:readInt32()
            tmp.gameid = bis:readInt32()
            tmp.game_name = bis:readString()
            tmp.paytype = bis:readInt8()
            tmp.maxcount = bis:readInt8()
            tmp.count = bis:readUInt8()
            tmp.players = {}
            for j = 1, tmp.count do
                local tmpPlayer = {}
                tmpPlayer.areaid = bis:readInt32()
                tmpPlayer.numid = bis:readInt32()
                tmpPlayer.seat = bis:readInt32()
                tmpPlayer.nickname = bis:readString()
                tmpPlayer.url = bis:readString()
                tmpPlayer.player_state = bis:readUInt8()
                tmp.players[j] = tmpPlayer
            end
            tmp.chairs = bis:readUInt8()
            self.tableInfos[i] = tmp
        end
        for j = 1, self.count do
            if bis:getAvailableSize() > 0 then
                local tableInfo = self.tableInfos[j]
                tableInfo.gameRule = bis:readString()
            end
        end
        for k = 1, self.count do
            if bis:getAvailableSize() > 0 then
                local tableInfo = self.tableInfos[k]
                tableInfo.gameCount = bis:readInt32()
            end
        end
        return self
    end,
}

BoxDataProtocol.ReqVisitorHeart = {
    XY_ID = CMDT_REQHEARTBEAT,

    nAskId = 0,
    nident = {},
    flag = false,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        bos:writeString(self.nident)
        bos:writeBool(self.flag)
        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskId = bis:readInt32()
        self.nident = bis:readString()
        self.flag = bis:readBool()
        return self
    end

}

BoxDataProtocol.RespVisitorHeart = {
    XY_ID = CMDT_RESPHEARTBEAT,

    nAskId = 0,
    nonlinetime  = 0, -- 在线时长 
    nnextplaytime = 0, -- nonlinetime》=40 得时候需要关注这个值 = 0 表示不能再体验了  ，否则表示下次体验时间戳

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        bos:writeInt32(self.nonlinetime)
        bos:writeInt32(self.nnextplaytime)
        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskId = bis:readInt32()
        self.nonlinetime = bis:readInt32()
        self.nnextplaytime = bis:readInt32()
        return self
    end
}

BoxDataProtocol.ReqOtherUserInfo = {
    XY_ID = CMDT_REQOTHERUSERINFO,

    nAskId = 0,
    nAreaId = 0,
    nNumId = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        bos:writeInt32(self.nAreaId)
        bos:writeInt32(self.nNumId)
        return bos
    end
}

BoxDataProtocol.RespOtherUserInfo = {
    XY_ID = CMDT_RESPOTHERUSERINFO,

    nAskId = 0,
    nNumid = 0,
    nNickname  = "",
    nHeadurl = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskId = bis:readInt32()
        self.nNumid = bis:readInt32()
        self.nNickname = bis:readString()
        self.nHeadurl = bis:readString()
        return self
    end
}

BoxDataProtocol.ReqGetBanInfo = {
    XY_ID = CMDT_REQGETBANINFO,

    nAskId = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        return bos
    end
}

BoxDataProtocol.RespGetBanInfo = {
    XY_ID = CMDT_RESPGETBANINFO,
    nAskId = 0,
    bansize = 0,
    bantype = {
        BAN_GAME = 1,
        BAN_TEA = 2,
        BAN_LOGIN = 3,
        BAN_WARN = 4,
        BAN_RECHARGE = 5,
        BAN_GIVECARD = 6
    },
    banendtime = {},
    banreason = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.nAskId = bis:readInt32()
        self.bansize = bis:readInt32()
        self.bantype = {}
        self.banendtime = {}
        self.banreason = ""
        for i = 1,self.bansize do            
            self.bantype[i] = bis:readInt32()
            self.banendtime[i] = bis:readInt32()            
        end
        self.banreason = bis:readString()
        return self
    end
}

for _, v in pairs(BoxDataProtocol) do
    v.processid = 113
end
BoxDataProtocol.processid = 113

return BoxDataProtocol
   n  