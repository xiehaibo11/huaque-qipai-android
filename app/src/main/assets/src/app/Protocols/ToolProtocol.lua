local ToolProtocol = {}

local MAXNICKNAME = 20

local CMDT_FIRST_CLIENT = 10000
local CMDT_FIRST_CLIENT_TOOL = CMDT_FIRST_CLIENT + 2000
local CMDT_REQTEMPSESSIONID = CMDT_FIRST_CLIENT_TOOL + 7
local CMDT_RESPTEMPSESSIONID = CMDT_FIRST_CLIENT_TOOL + 8

local CMDT_REQGETPROPATTRS = CMDT_FIRST_CLIENT_TOOL + 9;
local CMDT_RESPGETPROPATTRS = CMDT_FIRST_CLIENT_TOOL + 10;
local CMDT_REQSR = CMDT_FIRST_CLIENT_TOOL + 11
local CMDT_RESPSR = CMDT_FIRST_CLIENT_TOOL + 12

local CMDT_PACKAGE_TYPE = CMDT_FIRST_CLIENT_TOOL + 57; -- 机型，系统及包体类型

local CMDT_REQPLAYERSIMPLEINFO = CMDT_FIRST_CLIENT_TOOL + 61 -- 请求玩家信息
local CMDT_RESPPLAYERSIMPLEINFO = CMDT_FIRST_CLIENT_TOOL + 62 -- 回复玩家信息

local CMDT_REQCHANGEPLAYERINFO = CMDT_FIRST_CLIENT_TOOL + 65 -- 请求修改玩家信息
local CMDT_RESPCHANGEPLAYERINFO = CMDT_FIRST_CLIENT_TOOL + 66 -- 回复修改玩家信息

local CMDT_REQCHECKBINDUSER = CMDT_FIRST_CLIENT_TOOL + 67 -- 检查是否捆绑
local CMDT_RESPCHECKBINDUSER = CMDT_FIRST_CLIENT_TOOL + 68

local CMDT_REQBINDUSER = CMDT_FIRST_CLIENT_TOOL + 69 -- 请求捆绑玩家
local CMDT_RESPBINDUSER = CMDT_FIRST_CLIENT_TOOL + 70

ToolProtocol.ReqTempSessionID = {
    XY_ID = CMDT_REQTEMPSESSIONID,
    askid = 0,
    areaid = 0,
    numid = 0,
    userid = "",
    source = 0,
    sessionid = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.areaid)
        bos:writeInt32(self.numid)
        bos:writeInt32(self.source)
        bos:write(self.sessionid, 16)
        bos:writeString(self.userid)
        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.areaid = bis:readInt32()
        self.numid = bis:readInt32()
        self.source = bis:readInt32()
        self.sessionid = bis:read(16)
        self.userid = bis:readCString(51)

        return self
    end
}

ToolProtocol.RespTempSessionID = {
    XY_ID = CMDT_RESPTEMPSESSIONID,
    FLAG = {
        SUCCESS = 0,
        SHOW_MESSAGE = 1,
        PTUSER_NEED_REGISTER = 2,
        UNKNOW_USERID = 3,
        INVALID_PASSWORD = 4,
        UNKNOW_USERTYPE = 5,
        LOCALAUTH_USERTYPE_ERR = 6,
        PLAYERCENTER_SEND_ERR = 7,
        PTSVR_SEND_ERR = 8,
        NO_PTINFO = 9,
        NO_AUTHFLAG = 10,
        DBERR = 11,
        DBCONNERR = 12,
        FORBID_USERID = 13,
        LOCK_BY_USER_SELF = 14,
        INVALID_HARDWARE = 15,
        PT_TIME_OUT = 16,
        PC_SEND_ERR = 17,
        PC_TIME_OUT = 18,
        AD_SEND_ERR = 19,
        AD_TIME_OUT = 20,

        POST_SENDERR = 23,
        POST_TIMEOUT = 24,
        AUTH_TOO_FREQUENTLY = 25,
        IP_TOO_FREQUENTLY = 26,
        PASS_ERR_OVER_TIMES = 27,
        SMS_ERR_OVER_TIMES = 28,
        IPPASS_ERR_OVER_TIMES = 29,

        INVALID_SESSIONID = 72,
        NO_SESSIONID = 74
    },

    askid = 0,
    flag = '0',
    tempsessionid = "",
    source = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeUInt8(self.flag)
        bos:write(self.tempsessionid, 16)
        bos:writeInt32(self.source)
        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.flag = bis:readUInt8()
        self.tempsessionid = bis:read(16)
        self.source = bis:readInt32()

        return self
    end

}

ToolProtocol.ReqSR = {
    XY_ID = CMDT_REQSR,

    askid = 0,
    gameid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.gameid)

        return bos
    end
}

ToolProtocol.ResSR = {
    XY_ID = CMDT_RESPSR,

    FLAG = {
        SUCCESS = 0,
        POST_SENDERR = 1,
        POST_TIMEOUT = 2,

        DBERR = 21,
        GAMETABLENOTFIND = 22,
        DBCONNNOTFIND = 23,
        SVRSCORETYPEERR = 24
    },
    askid = 0,
    flag = 0,
    purse = 0,
    bank = 0,
    havepwd = 0, -- 0 = no pwd//1 = have pwd

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.flag = bis:readUInt8()
        self.purse = bis:readInt64()
        self.bank = bis:readInt64()
        self.havepwd = bis:readUInt8()

        return self
    end
}

ToolProtocol.ReqPlayerPropsData = {
    XY_ID = CMDT_REQGETPROPATTRS,

    askid = 1,
    gameid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.gameid)
        return bos
    end
}

ToolProtocol.RespPlayerPropsData = {
    XY_ID = CMDT_RESPGETPROPATTRS,

    FLAG = {
        SUCCESS = 0,
        POST_SENDERR = 1,
        POST_TIMEOUT = 2,

        DBCONNNOTFIND = 21,
        NETBUSY = 28,
        DBERR = 29
    },

    flag = 0,
    askid = 0,
    yb = 0,
    charm = 0,
    contribution = 0,
    attsize = 0,
    att = {},
    propssize = 0,
    props = {},
    timesize = 0,
    timeprops = {},
    timeend = {},
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.flag = bis:readUInt8()
        self.askid = bis:readInt32()
        self.yb = bis:readInt32()
        self.charm = bis:readInt32()
        self.contribution = bis:readInt32()
        self.attsize = bis:readUInt8()
        self.att = {}
        for i = 1, self.attsize do
            self.att[i] = bis:readInt32()
        end

        self.propssize = bis:readUInt8()
        self.props = {}
        for i = 1, self.propssize do
            self.props[i] = {
                id = bis:readInt32(),
                count = bis:readInt32()
            }
        end

        self.timesize = bis:readUInt8()
        self.timeprops = {}
        for i = 1, self.timesize do
            self.timeprops[i] = {
                id = bis:readInt32(),
                time = bis:readInt32()
            }
        end

        self.timeend = {}
        for i = 1, self.propssize do
            self.timeend[i] = bis:readInt32()
        end

        return self
    end
}

ToolProtocol.ClientInfo = {
    XY_ID = CMDT_PACKAGE_TYPE,

    nAskID = 0,
    szMachineType = "", -- 机型
    szOsVer = "", -- 系统版本
    szPackageName = "", -- 包名
    szDeviceType = "", -- 设备类型
    channelid = 0,
    lobbyid = 0, -- 从哪个游戏客户端登录
    lobbystate = -10, -- 更新结果对比UpdateState表
    szLobbyPreVer = "", -- 更新前版本
    szLobbyCurrVer = "", -- 当前版本

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskID)
        bos:writeString(self.szMachineType)
        bos:writeString(self.szOsVer)
        bos:writeString(self.szPackageName)
        bos:writeString(self.szDeviceType)
        bos:writeInt32(self.channelid)
        bos:writeInt32(self.lobbyid)
        bos:writeInt32(self.lobbystate)
        bos:writeString(self.szLobbyPreVer)
        bos:writeString(self.szLobbyCurrVer)
        return bos
    end
}

ToolProtocol.ReqPlayerSimpleInfo = {
    XY_ID = CMDT_REQPLAYERSIMPLEINFO,

    askid = 0,
    areaid = 0,
    numid = 0,
    gameid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.areaid)
        bos:writeInt32(self.numid)
        bos:writeInt32(self.gameid)
        return bos
    end
}

ToolProtocol.RespPlayerSimpleInfo = {
    XY_ID = CMDT_RESPPLAYERSIMPLEINFO,

    askid = 0,
    nickname = "",
    headurl = "",
    sex = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.nickname = bis:readCString(5 * MAXNICKNAME + 1)
        self.headurl = bis:readCString(1024)
        self.sex = bis:readUInt8()
        return self
    end
}

ToolProtocol.ReqChangePlayerInfo = {
    XY_ID = CMDT_REQCHANGEPLAYERINFO,

    CHANGETYPE = {
        CGT_NULL = 0x00,
        CGT_SEX = 0x01,
        CGT_NICKNAME = 0x02,
        CGT_HEADURL = 0x04,
        CGT_UNBIND = 0x08
    },
    askid = 0,
    flag = 0,
    sex = 0,
    nickname = "",
    headurl = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeUInt8(self.flag)
        bos:writeUInt8(self.sex)
        bos:writeString(self.nickname)
        bos:writeString(self.headurl)
        return bos
    end
}

ToolProtocol.RespChangePlayerInfo = {
    XY_ID = CMDT_RESPCHANGEPLAYERINFO,

    FLAG = {SUCCESS = 0, DBERR = 1, FAILED = 2},

    askid = 0,
    flag = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.flag = bis:readUInt8()
        return self
    end
}

ToolProtocol.ReqCheckBindUser = {
    XY_ID = CMDT_REQCHECKBINDUSER,

    askid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self, buff, bufflen)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        return bos
    end
}

ToolProtocol.RespCheckBindUser = {
    XY_ID = CMDT_RESPCHECKBINDUSER,

    FLAG = {NOBIND = 0, BINDUSER = 1},

    LOGINTYPE = {
        WX_LOGIN = 0,
        GY_LOGIN = 1,
        DX_LOGIN = 2,
        GY2WX_LOGIN = 3,
        DX2WX_LOGIN = 4,
    },

    askid = 0,
    flag = 0,
    logintype = 0,
    bind_areaid = 0,
    bind_numid = 0,
    bind_phone = "",
    bind_third = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.flag = bis:readUInt8()
        self.logintype = bis:readUInt8()
        self.bind_areaid = bis:readInt32()
        self.bind_numid = bis:readInt32()
        self.bind_phone = bis:readCString(20)
        if bis:getAvailableSize() > 0 then
            self.bind_third = bis:readCString(256)
        end
        return self
    end
}

ToolProtocol.ReqBindUser = {
    XY_ID = CMDT_REQBINDUSER,

    askid = 0,
    src_bind_areaid = 0,
    src_bind_numid = 0,
    bound_thirdid = "",
    src_bind_phone = "",
    auth_code = 0,
    clientid = "",
    ostype = 0, -- 0表示安卓 1表示ios
    channeltype = 0,--//0:个推, 1:极光, 2: 其他
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self, buff, bufflen)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.src_bind_areaid)
        bos:writeInt32(self.src_bind_numid)
        bos:writeString(self.bound_thirdid)
        bos:writeString(self.src_bind_phone)
        bos:writeInt32(self.auth_code)
        bos:writeString(self.clientid)
        bos:writeInt32(self.ostype)
        bos:writeInt32(self.channeltype)
        return bos
    end
}

ToolProtocol.RespBindUser = {
    XY_ID = CMDT_RESPBINDUSER,

    FLAG = {
        SUCCESS = 0,
        NOUSER = 1,
        BINDED = 2,
        SRC_BINDED = 3,
        DBERR = 4, -- DBP脚本sql执行出错
        NOTWEIXINUSER = 6, -- 不是微信账号，无法绑定
        OVERLIMIT = 7, -- 超过限制次数
        SAMEBOUNDTHIRDID = 8 -- 当前绑定的和要绑定的一样，无需再绑定
    },

    askid = 0,
    flag = 0,
    src_bind_areaid = 0,
    src_bind_numid = 0,
    src_bind_phone = "",
    src_bind_nickname = "",
    src_bind_nickname2 = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.flag = bis:readUInt8()
        self.src_bind_areaid = bis:readInt32()
        self.src_bind_numid = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.src_bind_phone = bis:readCString(20)
        end
        if bis:getAvailableSize() > 0 then
            self.src_bind_nickname = bis:readCString(200)
        end
        if bis:getAvailableSize() > 0 then
            self.src_bind_nickname2 = bis:readCString(200)
        end
        return self
    end
}

for _, v in pairs(ToolProtocol) do
    v.processid = 62
end
ToolProtocol.processid = 62

return ToolProtocol
  �8  