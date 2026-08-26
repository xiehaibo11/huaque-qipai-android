-- SRSXY协议
local SRSProtocol = {}
local SvrDef = require("app.Define.SvrDef")

local CMDT_PLAYERCONNECT = 5 -- 请求大厅登录  
local CMDT_PLAYERDATA = 6 -- 认证结果,由认证服务向游戏服务端发送,如果通过,则包含用户的数据
-- local CMDT_REPORTSRSERR   = 9  --SRS通知其他服务一些错误(如转发失败等)(s->s)

local CMDT_REQSRSLOAD = 10 -- 请求SRS的负载情况(c->s) 
local CMDT_RESPSRSLOAD = 11 -- srs负载应答(s->c)

local CMDT_REQSRSADDR = 14
local CMDT_RESPSRSADDR = 15

-- local CMDT_TESTSPEED  = 18
local CMDT_REQPLAYERPLUSDATA = 23
local CMDT_RESPPLAYERPLUSDATA = 24

SRSProtocol.PlayerConnect = {
    XY_ID = CMDT_PLAYERCONNECT,

    USERTYPE = {
        USERID = 0, -- 平台帐号
        PTID = 1, -- PT帐号
        NMY = 2,
        GLOBAL_ANONYMITY = 3, -- 全局匿名帐号
        IDENTIFY = 5, -- 硬件码登录(移动设备)
        DEVELOPER = 6,
        SESSION = 7,
        REGISTER = 8,
        PHONENUM = 9, -- 手机加密码登录
        ANONYMITY = 255 -- 匿名
    },

    CLIENTTYPE = {
        PC = 0, -- 计算机
        TV = 1, -- 电视
        MOBILE = 2, -- 手机
        WEB = 3 -- 浏览器
    },

    OS_VERSION = {
        -- pc 0-199
        OS_WND_UNKNOWN = 0,
        OS_WND_95 = 10,
        OS_WND_98 = 20,
        OS_WND_ME = 30,
        OS_WND_NT_351 = 40,
        OS_WND_NT_40 = 50,
        OS_WND_2000 = 60,
        OS_WND_XP = 70,
        OS_WND_2003 = 80,
        OS_WND_VISTA = 90,
        OS_WND_SERVER_2008 = 92,
        OS_WND_7 = 94,
        OS_WND_SERVER_2008_R2 = 96,
        OS_WND_VISTA_PLUS = 100,
        OS_WND_8 = 101
    },

    clienttype = 2, -- 用于区分是哪种类型的客户端登录的，如TV大厅，PC大厅
    usertype = 0, -- 用户类型，定义在USERTYPE枚举中
    areaid = 0, -- 地区号
    userid = "",
    pwd = "", -- 密码
    ver = 0,
    identify = "", -- 硬件识别码(RC4加密,老的用的是协议的加密key, 其他都是用默认的key加密)
    osver = 0, -- 操作系统版本                                  -- SRS获取的ip

    data = "", -- 平台附加数据                                   -- 客户端连接端口(用于移动)
    channelid = 0, -- 渠道号
    nGameID = 0,
    isNew = false,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeUInt8(self.clienttype)
        bos:writeUInt8(self.usertype)
        bos:writeUInt32(self.areaid)
        bos:writeString(self.userid)
        if self.usertype ~= self.USERTYPE.SESSION then
            if self.usertype == self.USERTYPE.PHONENUM or self.isNew == false then
                bos:writeString(self.pwd)
            else
                bos:writeString("")
            end
        else
            bos:write(self.pwd, 16)
        end
        bos:writeString(self.identify)
        bos:writeInt32(self.ver)
        bos:writeInt32(self.channelid)
        bos:writeInt32(self.osver)
        bos:writeString(self.identify)
        bos:writeInt32(self.nGameID)

        --bos:writeString(self.data)
        if self.usertype ~= self.USERTYPE.SESSION and self.usertype ~= self.USERTYPE.PHONENUM and self.isNew == true then
			bos:writeInt16(#self.pwd)
            bos:write(self.pwd, #self.pwd)
        end
        
        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.clienttype = bis:readUInt8()
        self.usertype = bis:readUInt8()
        self.areaid = bis:readUInt32()
        self.userid = bis:readString()
        if self.usertype ~= self.USERTYPE.SESSION then
            self.pwd = bis:readString()
        else
            self.pwd = bis:read(16)
        end
        self.identify = bis:readString()
        self.osver = bis:readInt32()

        if (bis:getAvailableSize() > 0) then
            self.channelid = bis:readInt32()
        end

        return self
    end
}

SRSProtocol.PlayerData = {
    XY_ID = CMDT_PLAYERDATA,

    ERRORCODE = {
        -- authsvr返回的flag
        SUCCESS = 0, -- 认证成功
        SHOW_MESSAGE = 1, -- 错误信息在errmsg中
        PTUSER_NEED_REGISTER = 2, -- PT帐号正确，需要注册平台帐号
        UNKNOW_USERID = 3, -- 数据库users表中没有该平台帐号信息
        INVALID_PASSWORD = 4, -- 密码错误（本地认证）
        UNKNOW_USERTYPE = 5, -- 未知的帐号类型
        LOCALAUTH_USERTYPE_ERR = 6, -- 本地认证帐号类型错误（本地认证只能使用USERID类型）
        PLAYERCENTER_SEND_ERR = 7, -- 向玩家中心发送协议失败
        PTSVR_SEND_ERR = 8, -- 向PTSVR发送协议失败
        NO_PTINFO = 9, -- 数据库users_pt表中没有该帐号数据
        NO_AUTHFLAG = 10, -- 没有认证标志（游戏认证）
        DBERR = 11, -- 数据库错误
        DBCONNERR = 12, -- 表所在数据库连接未找到
        FORBID_USERID = 13,
        LOCK_BY_USER_SELF = 14, -- 帐号被自己锁定
        INVALID_HARDWARE = 15, -- 硬件码错误
        PT_TIME_OUT = 16, -- ptsvr返回超时
        PC_SEND_ERR = 17, -- 向playercenter发送协议失败
        PC_TIME_OUT = 18, -- playercenter返回超时
        AD_SEND_ERR = 19, -- AccountDataSvr服务发送失败
        AD_TIME_OUT = 20, -- AccountDataSvr返回超时

        -- lobbysvr返回的flag
        POST_SENDERR = 23, -- 向post服务发送协议失败
        POST_TIMEOUT = 24, -- post服务返回超时

        RISK_REFUSE = 25, -- 控制器被挡
        LOBBYVER_OLDR = 26, -- 需要升级,但当前版本依然可用
        LOBBYVER_TOOOLD = 27, -- 版本太旧,必须升级
        LOBBYVER_TOONEW = 28, -- 版本比最近版本还要新,认为异常,必须升级

        SMS_SEND_ERR = 30, -- 短信服务发送协议失败
        INVALID_SMSCODE = 31, -- 短信验证码错误
        NO_SMSCODE = 32, -- 回复短信验证码时发现memcache已经没有验证码了
        UNKNOW_AREAID = 33, -- areaid非法(==0)

        POST_SEND_ERR = 34, -- post发送协议失败
        OTP_TIME_OUT = 35, -- otpsvr返回超时
        INVALID_SCREEN = 36, -- 分辨率非法
        NO_GAMEPOST = 37,

        ACCOUNT_ERR = 41, -- 数据库中 没有该平台得账号信息

        NOT_KNOW_ACCOUNT_TYPE = 42, -- 未知得账号类型
        ACCOUNT_NOT_EXIST = 45, -- 账号不存在，请重新登录
        ACCOUNT_PWD_ERR = 48, -- 手机登录失败，密码错误，请重新验证并登录
        BIND_COMPUTER = 49, -- 您的帐号绑定了电脑，请先解绑，再进行登录
        BAN_LOGIN = 50, -- 您的帐号被禁止登录，请联系客服核实
        PLAYERCENTER_BANTO_LOGIN = 51, -- 玩家中心拒绝该帐号登录
        CNENTER_BANTO_IP = 52, -- 玩家中心拒绝该IP登录
        PLAEYER_CNENTER_DB_ERR = 53, -- 玩家中心数据库错误
        ARED_ID_ERR = 54, -- 区号错误
        UNKNOWN_THIRDID = 57, -- 没有该third_id
        NUMBER_ACCOUNT_ERR = 61, -- 数字帐号错误
        SYSTEM_BANTO_LOGIN = 62, -- 系统拒绝您的登录
        SERVER_NO_ERR = 63, -- 游戏或服务器编号错误
        HARDWARE_CODE_ERR = 64, -- 服务器编号错误
        ERROR = 65, -- 错误
        SESSIONID_ERROR = 66, -- SESSIONID错误

        INVALID_SESSIONID = 72, -- 令牌错误
        NO_SESSIONID = 74, -- 没有这个玩家的令牌信息
        INVALID_SESSIONID_PC = 75, -- 令牌错误, 且机器码错误
        INVALID_SESSIONID_IP = 76, -- 令牌错误, 且ip错误(机器码也错误)

        ACOCUNT_CANCELING = 80, -- 账号申请注销中

        LOGIN_TIME_OUT = 97, -- 登录超时
        SYSTEM_MAINTAIN = 98, -- 系统正在维护当中，请稍候再登录
        FREQUENT_LOGIN = 99, -- 频繁登录，请稍后尝试
        ACCOUNTORPWD_ERROR_1 = 101, -- 您输入的帐号密码有误，请重新输入！
        ACCOUNTORPWD_ERROR_2 = 102, -- 您输入的帐号密码有误，请重新输入
        SYSTEM_REFUSE_LOGIN = 103, -- 系统拒绝您的登录
        NOT_BING_PWD_PRO = 104, -- 您还没绑定密宝
        PWD_PRO_ERROR = 105, -- 您的密宝有误，不能正常登录
        NEED_TWO_PWD = 106, -- 需二级密码才能登录
        ACCOUNT_ALREADY_EXIST = 107, -- 您输入的用户已经存在
        ACCOUNT_LOGIN_OTHER = 110, -- 该帐号已经在其它地方登录
        ID_EXIST = 111, -- 游戏ID已经存在
        PT_LOGIN_FAIL = 112, -- PT登录失败
        ACCOUNT_SIMPLE_BIND = 113, -- 该帐号处于单机绑定状态，请使用绑定的机器登录
        ACCOUNT_NEED_PWD_BIND = 114, -- 该帐号必须使用密宝登录
        ACCOUNT_NEED_SAGE_CARD_LOGIN = 115, -- 该帐号必须使用安全卡登录
        ACCOUNT_NEED_SAGE_CARD_LOGIN_1 = 116 -- 该帐号必须使用安全卡登录

    },

    askid = 0,
    flag = 0, -- 错误号
    areaid = 0, -- 区号(即areaid)
    areatypeid = 0,
    numid = 0, -- 数字帐号
    msgbox = SvrDef.MsgBox:new(), -- 弹出消息

    -- 秘宝相关;
    elimited = 0, -- 是否成年,0未成年1成年
    eprotected = 0, -- 是否补填防沉迷,0没有1有
    eUsedTime = 0, -- 盛大秘宝使用次数
    eUsedDays = 0,
    eLevel = 0,
    protecturl = "", -- 实名认证登记地址
    notify = "", -- PT返回,对玩家的一些通知

    iparea = 0, -- 本次登录的ip所属地区(格式: 省+两位市+两位区县组成的整型, 如省=3,市=5,县=23,则iparea=30523)
    sp = 0, -- 本次登录的ip所属运营商
    lastip = 0, -- 上次登录的ip
    lastiparea = 0, -- 上次登录的ip所属地区(格式同iparea)
    lastsp = 0, -- 上次登录的ip所属运营商

    lobbyver = 0, -- 服务端配的当前大厅版本号(curversion)
    sessionid = "",

    datasize = 0,
    data = "", -- 内容为定义在agsvrdef.h的PlayerPlusData结构

    -- 防沉迷相关
    lastleavetime = 0, -- 上一次离开房间的时间
    onlinetime = 0, -- 当前累计游戏在线时间(在房间内的时间, 所有游戏的, 双进了会不准, 不管)
    breaktime = 0, -- 上次进房间前累计的休息时间

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeUInt8(self.flag)
        bos:writeInt32(self.areaid)
        bos:writeInt32(self.numid)
        bos:writeString(self.nickname)
        bos:writeString(self.protecturl)
        if self.flag == self.ERRORCODE.SHOW_MESSAGE then
            bos:writeString(self.msg)
        end
        bos:write(self.sessionid, 16)

        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        -- bis:attach(buff, bufflen)

        self.flag = bis:readUInt8()
        self.areaid = bis:readInt32()
        self.numid = bis:readInt32()

        self.nickname = bis:readString()
        self.protecturl = bis:readString()
        if self.flag == self.ERRORCODE.SHOW_MESSAGE then
            self.msg = bis:readString()
        end
        if bis:getAvailableSize() > 0 then
            self.sessionid = bis:read(16)
        end

        return self
    end
}

SRSProtocol.ReqPlayerPlusData = {
    XY_ID = CMDT_REQPLAYERPLUSDATA,

    -- new
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        return bos
    end,

    -- bistream
    bistream = function(self, buff, bufflen)
        return self
    end
}

SRSProtocol.RespPlayerPlusData = {
    XY_ID = CMDT_RESPPLAYERPLUSDATA,

    -- 帐号权限标志
    URIGHT = {
        ADMIN = 0x0001, -- 副领队
        LOCKUSER = 0x0002,
        LOCKCHAT = 0x0004, -- 封聊天
        VIP = 0x0008, -- 会员
        LOCKMAC = 0x0010, -- 锁机
        LOCKMOBILE = 0x0040, -- 是否绑定手机
        IPSID = 0x0080, -- sessionid是否与ip绑定(验sessionid的时候是否要验ip)
        AUTHSMS = 0x0100, -- 是否有发短信验证码的会员权限
        ROBOT = 0x0200 -- 是否是机器人
    },

    CLIENTTYPE = {
        PC = 0, -- 计算机
        TV = 1, -- 电视
        MOBILE = 2, -- 手机
        WEB = 3 -- 浏览器
    },

    userid = "", -- 平台帐号
    ptid = "", -- 盛大通行证
    ptnumid = "", -- 盛大数字账号
    nickname = "", -- 昵称
    nickname2 = "", -- 昵称
    identify = "", -- 明文

    sex = '0', -- 性别
    head = 0, -- 头像
    right = 0, -- 权限
    regtime = 0, -- 注册时间
    vipid = 0, -- 会员类型
    vipendtime = 0, -- 会员到期时间

    ip = 0, -- 客户端ip
    osver = 0, -- 客户端操作系统版本号
    clienttype = 0, -- 客户端类型

    elimited = 0,
    eprotected = 0,
    protecturl = "",

    keylen = 0, -- m_key 的长度, 允许的取值: 16, 24, 32
    key = "", -- 协议加密密钥

    ptname = "",
    notify = "",

    isnewplayer = false,

    -- new
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    -- bistream
    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        -- bis:attach(buff, bufflen)

        self.userid = bis:readString() -- readCString(51)
        self.ptid = bis:readString()
        self.ptnumid = bis:readString()
        self.nickname = bis:readString()
        self.identify = bis:readString()

        self.sex = bis:readUInt8()
        self.head = bis:readInt32()
        self.right = bis:readInt32()
        self.regtime = bis:readInt32()
        self.vipid = bis:readInt32()
        self.vipendtime = bis:readInt32()
        self.ip = bis:readInt32()
        self.osver = bis:readInt32()
        self.clienttype = bis:readInt32()

        if (bis:getAvailableSize() > 0) then
            self.keylen = bis:readUInt8()
            self.key = bis:read(self.keylen)
        end
        if (bis:getAvailableSize() > 0) then
            self.elimited = bis:readInt32()
            self.eprotected = bis:readInt32()
            self.protecturl = bis:readString()
        end
        if (bis:getAvailableSize() > 0) then
            self.ptname = bis:readString()
        end
        if (bis:getAvailableSize() > 0) then
            self.notify = bis:readString()
        end
        if (bis:getAvailableSize() > 0) then
            self.nickname2 = bis:readString()
        end
        if (bis:getAvailableSize() > 0) then
            self.isnewplayer = bis:readBool()
        end

        return self
    end
}

SRSProtocol.ReqSRSLoad = {
    XY_ID = CMDT_REQSRSLOAD,
    nAppID = 0, --  应用编号，0为查询全部

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeUInt16(self.nAppID)
        return bos
    end
}

SRSProtocol.tagSRSLoadRec = {
    nAppID = 0,
    nLoad = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end
}

SRSProtocol.RespSRSLoad = {
    XY_ID = CMDT_RESPSRSLOAD,
    nCount = 0,
    SRSLoad = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.nCount = bis:readUInt16()

        for i = 1, self.nCount do
            local srsLoadItem = SRSProtocol.tagSRSLoadRec:new()
            srsLoadItem.nAppID = bis:readUInt16()
            srsLoadItem.nLoad = bis:readUInt16()
            self.SRSLoad[i] = srsLoadItem
        end
        return self
    end

}

SRSProtocol.ReqSRSAddr = {
    XY_ID = CMDT_REQSRSADDR,
    nAppID = 0, --  应用编号，0为查询全部

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeUInt16(self.nAppID)
        return bos
    end
}

SRSProtocol.RespSRSAddr = {
    XY_ID = CMDT_RESPSRSADDR,
    nAppID = 0,
    szIP = "",
    sPort = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.nAppID = bis:readUInt16()
        self.szIP = bis:readString()
        self.sPort = bis:readUInt16()
        return self
    end

}

for _, v in pairs(SRSProtocol) do
    v.processid = 0
    if v.XY_ID then
        v.event_key = v.processid .. "_" .. v.XY_ID
    end
end
SRSProtocol.processid = 0

return SRSProtocol
   !D  