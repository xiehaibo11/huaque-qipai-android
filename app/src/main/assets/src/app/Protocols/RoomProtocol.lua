-- LobbyProcess 相关协议，请求位置信息，加入房间，创建房间等
local RoomProtocol = {}
local SvrDef = require("app.Define.SvrDef")

local CMDT_REQUEST_CREATE_TABLE = 11; -- 请求创建房间
local CMDT_RESPONSE_CREATE_TABLE = 12; -- 创建房间响应
local CMDT_REQUEST_JOIN_TABLE = 13; -- 请求加入房间
local CMDT_RESPONSE_JOIN_TABLE = 14; -- 加入房间响应
local CMDT_REQUEST_POSITION = 15; -- 请求位置信息
local CMDT_RESPONSE_POSITION = 16; -- 位置信息响应

local CMDT_REQUEST_JOIN_TABLE_WITH_GOLD = 17; -- 请求加入金币场
local CMDT_RESPONSE_JOIN_TABLE_WITH_GOLD = 18; -- 返回加入金币场

local CMDT_REQGETGOLDROOMINFO = 24; -- 获取金币场配置
local CMDT_RESPGETGOLDROOMINFO = 25; -- 返回

-- 统一错误码
RoomProtocol.ERRORCODE = {
    SUCCESS = 0,
    SHOW_MESSAGE = 1, -- 消息提示
    ERROR_PARAM = 2, -- 参数错误
    ERROR_GET_GAME_APPID = 3, -- 获取Game服务appid出错
    ERROR_CREATE_ROOM_ID = 4, -- 创建房间号出错
    ERROR_SAVE_ROOM_INFO = 5, -- 保存房间信息出错
    ERROR_ROOM_ID = 6, -- 错误的房间号
    NO_MATCH_GAME_APPID = 7, -- 没有匹配的Game服务
    NO_FOUND_ROOM_CONFIG = 8, -- 没有找到对应的房间配置
    NOT_ENOUGH_PROP = 9, -- 道具不足
    ALREADY_IN_TABLE = 10, -- 已经在桌子上
    ROOM_DISMISSED = 11, -- 房间已解散
    NOT_FOUND_GAME_ID = 12, -- 使用appid无法找到对应的gameid
    NOT_TEA_HOUSE_RIGHT = 13, -- 没有比赛场权限
    NOT_EMPTY_TABLE = 14, -- 没有空桌
    TEA_HOUSE_CLOSED = 15, -- 比赛场已关闭
    ERROR_GET_ROOM_MODE = 16, -- 从树列表获取roommode失败
    REDIS_NOT_FOUND_ROOM_ID = 17, -- 从redis获取不到房间号

    ERROR_TABLE_START = 24, -- 桌子已经开局，提示是否旁观

    ERROE_DISTANCE_LIMIT = 48, -- 距离过近，防作弊无法加入房间

    ERROR_CAN_NOT_FIND_ROOM = 100, -- 不能找到匹配的房间
    ERROR_MAINTENANCE = 101, -- 该游戏维护中，请进入其他游戏
    ERROR_INAPPID = 102, -- 已在游戏房间中
    ERROR_NOT_ENOUGH_SR = 103, -- 金币不足
    ERROR_TOO_MUCH_SR = 104, -- 金币太多了，请前往更高级的场次游戏
    ERROR_IP_LIMITED = 105, -- 同ip限制
    ERROR_NOT_OPEN_GPS = 106, -- 没开启定位
    ERROR_UNBINDWECHAT = 108, -- 未绑定微信的账号，禁止进入
    ERROR_BAN = 109,--禁止加入游戏
    ERROR_IN_MATCH_LIST = 217 -- 在匹配队列中
}

RoomProtocol.CLIENTTYPE = {
    PC = 0, -- 计算机
    TV = 1, -- 电视
    MOBILE = 2, -- 手机
    WEB = 3 -- 浏览器
}

RoomProtocol.HARDWAREFLAG = -- 带硬件标识
{
    HF_NONE = 0,
    HF_CAMERA = 1, -- 摄像头
    HF_MICROPHONE = 2, -- 麦克风
    HF_JOYPAD = 4 -- 游戏手柄，不区分有线无线
}

RoomProtocol.ACTION = {
    SITDOWN = 1,    --坐下
    STANDUP = 2,    --站起
    READY = 3,  --房间模块按了开始
    SEEGAME = 4,    --普通旁观(桌上有人才可旁观)
    CHANGETOSTART = 5,  --桌子里面按了开始
    CHANGETOSEEGAME = 6,    --坐着的变成旁观
    CHANGESEAT = 7, --换座位
    LEAVEROOM = 8,  --离开房间
    SEEGAME2 = 9,   --新增旁观(空桌也可旁观)
}

RoomProtocol.ReqCreateTable = {
    XY_ID = CMDT_REQUEST_CREATE_TABLE,

    askid = 0,
    gameid = 0, -- 游戏编号
    areatypeid = 0, -- 子区编号
    tabletype = 0, -- 房间消耗与消耗，对应天枢配置的t1-t12
    clienttype = 0, -- 客户端类型，见宏LobbyProcess32::ClientType
    hardwareflag = 0, -- 硬件标识，见宏HARDWAREFLAG
    ver = 0,
    channelid = 0, -- 渠道号
    ntype = 0, -- 标示客户端游戏方式，见宏TYPE
    osver = 0, -- client OS版本号
    identify = "",
    reconnect = false, -- 是否重连标志, 1为重连,0为正常
    clienttypecustom = 0, -- 客户端自定义类型
    nickname = "", -- 昵称
    nickname2 = "", -- 昵称2(UTF-8)
    headurl = "", -- 玩家头像url
    roomrule = "", -- 房间规则
    gamerule = "", -- 玩法规则
    lastroomid = 0,
    vipPropId = 0, -- 免费建房道具id

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.gameid)
        bos:writeInt32(self.areatypeid)
        bos:writeInt32(self.tabletype)
        bos:writeInt32(self.clienttype)
        bos:writeInt32(self.hardwareflag)
        bos:writeInt32(self.ver)
        bos:writeInt32(self.channelid)
        bos:writeUInt8(self.ntype)
        bos:writeInt32(self.osver)
        bos:writeString(self.identify)
        bos:writeBool(self.reconnect)
        bos:writeInt32(self.clienttypecustom)
        bos:writeString(self.nickname)
        bos:writeString(self.nickname2)
        bos:writeString(self.headurl)
        bos:writeString(self.roomrule)
        bos:writeString(self.gamerule)
        bos:writeInt32(self.lastroomid)
        bos:writeInt32(self.vipPropId)
        return bos
    end
}

RoomProtocol.RespCreateTable = {
    XY_ID = CMDT_RESPONSE_CREATE_TABLE,

    FLAG = {},

    state = 0, -- lobby流程状态
    errorcode = 0,
    askid = 0,
    roommode = 0, -- 对应IGameInterface宏RoomMode
    gameappid = 0, -- GameProcess的appid
    roomid = 0, -- 房间号
    gameid = 0, -- 玩法编号
    tableid = 0, -- 桌号
    chairid = 0, -- 位置
    msgbox = SvrDef.MsgBox:new(), -- 提示消息
    srsgroupid = 0, -- srsgroupid

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.state = bis:readUInt8()
        self.errorcode = bis:readInt32()
        self.askid = bis:readInt32()
        self.roommode = bis:readInt32()
        self.gameappid = bis:readInt32()
        self.roomid = bis:readInt32()
        self.gameid = bis:readInt32()
        self.tableid = bis:readInt32()
        self.chairid = bis:readUInt8()
        if self.errorcode == RoomProtocol.ERRORCODE.SHOW_MESSAGE then
            self.msgbox.m_type = bis:readUInt8()
            self.msgbox.m_szCaption = bis:readString()
            self.msgbox.m_szText = bis:readString()
            self.msgbox.m_dwIconBtn = bis:readUInt16()
            self.msgbox.m_delay = bis:readUInt8()
            self.msgbox.m_colour = bis:readUInt16()
            self.msgbox.m_szWeb = bis:readString()
            self.msgbox.m_dwAction = bis:readUInt8()
        end
        self.srsgroupid = bis:readInt32()
        return self
    end
}

RoomProtocol.ReqJoinTable = {
    XY_ID = CMDT_REQUEST_JOIN_TABLE,

    askid = 0,
    areatypeid = 0,
    roomid = 0,
    clienttype = 0, -- 客户端类型, 见宏CLIENTTYPE
    hardwareflag = 0, -- 硬件标识
    ver = 0,
    channelid = 0, -- 渠道号
    ntype = 0, -- 标示客户端游戏方式
    osver = 0, -- client OS版本号
    identify = "",
    reconnect = false, -- 是否重连标志, 1为重连,0为正常
    clienttypecustom = 0, -- 客户端自定义类型
    nickname = "", -- 昵称
    nickname2 = "", -- 昵称2(UTF-8)
    headurl = "", -- 玩家头像url
    lobbyid = 0, -- 大厅id，大厅进入比赛场用
    logicData = "", -- 暂时无用
    acOtherInfo = "", -- 逻辑扩展字段
    action = 0,   -- 行为

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.areatypeid)
        bos:writeInt32(self.roomid)
        bos:writeInt32(self.clienttype)
        bos:writeInt32(self.hardwareflag)
        bos:writeInt32(self.ver)
        bos:writeInt32(self.channelid)
        bos:writeUInt8(self.ntype)
        bos:writeInt32(self.osver)
        bos:writeString(self.identify)
        bos:writeBool(self.reconnect)
        bos:writeInt32(self.clienttypecustom)
        bos:writeString(self.nickname)
        bos:writeString(self.nickname2)
        bos:writeString(self.headurl)
        bos:writeInt32(self.lobbyid)
        bos:writeString(self.logicData)
        bos:writeString(self.acOtherInfo)
        bos:writeUInt8(self.action)
        return bos
    end
}

RoomProtocol.RespJoinTable = {
    XY_ID = CMDT_RESPONSE_JOIN_TABLE,

    FLAG = {},

    state = 0, -- lobby流程状态，对应宏STATE
    errorcode = 0,
    askid = 0,
    roommode = 0, -- 对应IGameInterface宏RoomMode
    gameappid = 0, -- GameProcess的appid
    roomid = 0, -- 房间号
    gameid = 0, -- 玩法编号
    tableid = 0, -- 桌号
    chairid = 0, -- 位置
    msgbox = SvrDef.MsgBox:new(), -- 提示消息
    srsgroupid = 0, -- srsgroupid
    teaid = 0, -- 比赛场口令
    proxyid = 0,
    teaappid = 0,
    tealevel = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.state = bis:readUInt8()
        self.errorcode = bis:readInt32()
        self.askid = bis:readInt32()
        self.roommode = bis:readInt32()
        self.gameappid = bis:readInt32()
        self.roomid = bis:readInt32()
        self.gameid = bis:readInt32()
        self.tableid = bis:readInt32()
        self.chairid = bis:readUInt8()
        if self.errorcode == RoomProtocol.ERRORCODE.SHOW_MESSAGE then
            self.msgbox.m_type = bis:readUInt8()
            self.msgbox.m_szCaption = bis:readString()
            self.msgbox.m_szText = bis:readString()
            self.msgbox.m_dwIconBtn = bis:readUInt16()
            self.msgbox.m_delay = bis:readUInt8()
            self.msgbox.m_colour = bis:readUInt16()
            self.msgbox.m_szWeb = bis:readString()
            self.msgbox.m_dwAction = bis:readUInt8()
        end
        self.srsgroupid = bis:readInt32()
        if bis:getAvailableSize() >= 4 then
            self.teaid = bis:readInt32()
        end
        if bis:getAvailableSize() >= 4 then
            self.proxyid = bis:readInt32()
        end
        if bis:getAvailableSize() >= 4 then
            self.teaappid = bis:readInt32()
        end
        if bis:getAvailableSize() >= 4 then
            self.tealevel = bis:readInt32()
        end
        return self
    end
}

RoomProtocol.ReqPlayerPosition = {
    XY_ID = CMDT_REQUEST_POSITION,

    askid = 0,
    gameid = 0, -- 大厅gameid
    areatypeid = 0,
    lobbyid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.gameid)
        bos:writeInt32(self.areatypeid)
        bos:writeInt32(self.lobbyid)
        return bos
    end
}

RoomProtocol.RespPlayerPosition = {
    XY_ID = CMDT_RESPONSE_POSITION,

    errorcode = 0,
    askid = 0,
    placesize = 0,
    places = {},
    msgbox = SvrDef.MsgBox:new(), -- 提示消息
    fmterrcode = 0,
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.errorcode = bis:readInt32()
        self.askid = bis:readInt32()
        self.placesize = bis:readInt32()

        for i = 1, self.placesize do
            self.places[i] = {}
            self.places[i].state = bis:readUInt8()
            self.places[i].roommode = bis:readInt32()
            self.places[i].gameappid = bis:readInt32()
            self.places[i].roomid = bis:readInt32()
            self.places[i].gameid = bis:readInt32()
            self.places[i].tableid = bis:readInt32()
            self.places[i].chairid = bis:readUInt8()
        end

        if self.errorcode == RoomProtocol.ERRORCODE.SHOW_MESSAGE then
            self.msgbox.m_type = bis:readUInt8()
            self.msgbox.m_szCaption = bis:readString()
            self.msgbox.m_szText = bis:readString()
            self.msgbox.m_dwIconBtn = bis:readUInt16()
            self.msgbox.m_delay = bis:readUInt8()
            self.msgbox.m_colour = bis:readUInt16()
            self.msgbox.m_szWeb = bis:readString()
            self.msgbox.m_dwAction = bis:readUInt8()
        end
        for i = 1, self.placesize do
            self.places[i].srsgroupid = bis:readInt32()
        end
        for i = 1, self.placesize do
            self.places[i].teaid = bis:readInt32()
        end
        for i = 1, self.placesize do
            self.places[i].teaappid = bis:readInt32()
        end
        if  bis:getAvailableSize() >= 4 then
            self.fmterrcode = bis:readInt32()
        end
        for i = 1, self.placesize do
            if  bis:getAvailableSize() >= 4 then
                self.places[i].teaNumber= bis:readInt32()
            end
        end
        for i = 1, self.placesize do
            if  bis:getAvailableSize() >= 4 then
                self.places[i].proxyid = bis:readInt32()
            end
        end

        return self
    end
}

RoomProtocol.ReqJoinTableWithGold = {
    XY_ID = CMDT_REQUEST_JOIN_TABLE_WITH_GOLD,

    askid = 0,
    areatypeid = 0,
    roomid = 0,
    clienttype = 0, -- 客户端类型，见宏CLIENTTYPE
    hardwareflag = 0, -- 硬件标识
    ver = 0,
    channelid = 0, -- 渠道号
    type = 0, -- 标识客户端游戏方式
    osver = 0, -- 客户端os版本
    identify = "",
    reconnect = false, -- 是否重连标志，true为重连，false为正常
    clienttypecustom = 0, -- 客户端自定义类型
    nickname = "", -- 昵称
    nickname2 = "", -- 昵称2（utf-8）
    headurl = "", -- 玩家头像
    lobbyid = 0, -- 虚拟大厅id

    baseplaytypescore = 0, -- 底分（-1表示不显示）
    minsr = 0, -- 最小财富值（-1表示不限制）
    maxsr = 0, -- 最大财富（-1表示不限制）
    gameid = 0, -- 游戏id
    chaircnt = 0, -- 椅子数
    roomnameflag = 0, -- 场次标识（初级场，中级场等，从1开始，-1表示不限制）
    nRoomModeType = 0,
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.areatypeid)
        bos:writeInt32(self.roomid)
        bos:writeInt32(self.clienttype)
        bos:writeInt32(self.hardwareflag)
        bos:writeInt32(self.ver)
        bos:writeInt32(self.channelid)
        bos:writeUInt8(self.type)
        bos:writeInt32(self.osver)
        bos:writeString(self.identify)
        bos:writeBool(self.reconnect)
        bos:writeInt32(self.clienttypecustom)
        bos:writeString(self.nickname)
        bos:writeString(self.nickname2)
        bos:writeString(self.headurl)
        bos:writeInt32(self.lobbyid)
        bos:writeInt32(self.baseplaytypescore)
        bos:writeInt32(self.minsr)
        bos:writeInt32(self.maxsr)
        bos:writeInt32(self.gameid)
        bos:writeInt32(self.chaircnt)
        bos:writeInt32(self.roomnameflag)
        bos:writeInt32(self.nRoomModeType)
        return bos
    end
}

RoomProtocol.RespJoinTableWithGold = {
    XY_ID = CMDT_RESPONSE_JOIN_TABLE_WITH_GOLD,

    askid = 0,
    state = -1,
    errorcode = -1,
    roommode = 0,
    gameappid = 0,
    roomid = 0,
    gameid = 0,
    chaircnt = 0,
    tableid = 0,
    chairid = -1,
    inappid = 0,
    srsgroupid = 0,
    msgbox = SvrDef.MsgBox:new(),
    fmterrcode = 0,
    nMatchAppid = 0,
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.state = bis:readUInt8()
        self.errorcode = bis:readInt32()
        self.roommode = bis:readInt32()
        self.gameappid = bis:readInt32()
        self.roomid = bis:readInt32()
        self.gameid = bis:readInt32()
        self.chaircnt = bis:readInt32()
        self.tableid = bis:readInt32()
        self.chairid = bis:readUInt8()
        self.inappid = bis:readInt32()
        self.srsgroupid = bis:readInt32()
        if self.errorcode == RoomProtocol.ERRORCODE.SHOW_MESSAGE then
            self.msgbox.m_type = bis:readUInt8()
            self.msgbox.m_szCaption = bis:readString()
            self.msgbox.m_szText = bis:readString()
            self.msgbox.m_dwIconBtn = bis:readUInt16()
            self.msgbox.m_delay = bis:readUInt8()
            self.msgbox.m_colour = bis:readUInt16()
            self.msgbox.m_szWeb = bis:readString()
            self.msgbox.m_dwAction = bis:readUInt8()
        end
        self.fmterrcode = bis:readInt32()
        self.nMatchAppid = bis:readInt32()
        return self
    end
}

RoomProtocol.StartGameByLobby = {
    XY_ID = 23,

    m_CanStart = 0, -- 能否开始游戏
    szPasswd = "", -- 进入包房密码

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeUInt8(self.m_CanStart)
        bos:writeString(self.szPasswd)
        return bos
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.m_CanStart = bis:readUInt8()
        self.szPasswd = bis:readString()

        return self
    end
}

RoomProtocol.ReqGetGoldRoomInfo = {
    XY_ID = CMDT_REQGETGOLDROOMINFO,

    askid = 0,
    size = 0,
    gameids = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.size)
        for i = 1, self.size do
            bos:writeInt32(self.gameids[i])
        end
        return bos
    end
}

RoomProtocol.RespGetGoldRoomInfo = {
    XY_ID = CMDT_RESPGETGOLDROOMINFO,

    askid = 0,
    size = 0,
    roomInfos = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.size = bis:readInt32()
        self.roomInfos = {}
        for i = 1, self.size do
            self.roomInfos[i] = {}
            self.roomInfos[i].gameid = bis:readInt32()
            self.roomInfos[i].appid = bis:readInt32()
            self.roomInfos[i].roommode = bis:readInt32()
            self.roomInfos[i].roomnameflag = bis:readInt32() --  1-初级场，2-中级场，3-高级场
            self.roomInfos[i].basescore = bis:readInt32() -- 底分
            self.roomInfos[i].servicepay = bis:readInt32() -- 茶水费
            self.roomInfos[i].minrich = bis:readInt32() -- 金币下限
            self.roomInfos[i].maxrich = bis:readInt32() -- 金币上限
            self.roomInfos[i].chaircnt = bis:readInt32() -- 人数
            self.roomInfos[i].rsv1 = bis:readInt32() -- 保留位
            self.roomInfos[i].data = bis:readString() -- 其它数据，保留位，暂时无数据
        end
        return self
    end
}

for _, v in pairs(RoomProtocol) do
    v.processid = 84
    if v.XY_ID then
        v.event_key = v.processid .. "_" .. v.XY_ID
    end
end

RoomProtocol.processid = 84

return RoomProtocol
  jO  