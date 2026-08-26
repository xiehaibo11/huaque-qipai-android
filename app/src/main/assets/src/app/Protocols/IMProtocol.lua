local IMProtocol = {}

local CMDT_REQAPPIDLIST = 301;
local CMDT_RESPAPPIDLIST = 302;
local CMDT_NOTIFYSERVERSTATE = 303;
local CMDT_REQPLAYERCONNECT = 304;
local CMDT_RESPPLAYERCONNECT = 305;
local CMDT_REQKEEPALIVE = 306; -- 请求发送心跳，该协议100秒发送一次即可
local CMDT_RESPKEEPALIVE = 307;
-- local CMDT_REQCLIENTIP                      = 308;  --请求玩家ip，该协议是模拟协议(服务端使用)
-- local CMDT_REQCLIENTCLOSE                   = 309;  --客户端关闭，该协议是模式协议(服务端使用)
local CMDT_REQJOINIM = 401;
local CMDT_RESPJOINIM = 402;
local CMDT_REQOPENFRIENDLIST = 408; -- 请求打开好友列表
local CMDT_RESPOPENFIENDLIST = 409;
local CMDT_REQCLOSEFRIENDLIST = 410; -- 请求关闭好友列表
local CMDT_RESPCLOSEFRIENDLIST = 411;
local CMDT_NOTIFYPLAYERINFO = 412; -- 服务通知好友状态变更
local CMDT_REQFRIENDLIST = 413; -- 请求好友列表
local CMDT_RESPFRIENDLIST = 414;
local CMDT_REQINVITEGAME = 415; -- 邀请好友游戏
local CMDT_RESPINVITEGAME = 416;
local CMDT_NOTIFYINVITEINFO = 417; -- 通知邀请消息
local CMDT_REQGETINVITEINFO = 418; -- 获取最近被邀请信息列表
local CMDT_RESPGETINVITEINFO = 419;
local CMDT_REQREPLYINVITEGAME = 420; -- 回复被邀请
local CMDT_RESPREPLYINVITEGAME = 421;
local CMDT_NOTIFYREPLYINVITEGAME = 422; -- 通知邀请方
-- local CMDT_REQINVITELIST                    = 423;  --获取玩家邀请其他人的列表
-- local CMDT_RESPINVITELIST                   = 424;
local CMDT_REQINVITEDDLIST = 425; -- 获取玩家被邀请的列表
local CMDT_RESPINVITEDDLIST = 426;
local CMDT_REQFRIENDTABLEINFO = 431; -- 获取好友所在桌子信息
local CMDT_RESPFRIENDTABLEINFO = 432;
local CMDT_REQWILLJOINTABLE = 461; -- 玩家请求加入好友的房间(跟随)
local CMDT_RESPWILLJOINTABLE = 437;
local CMDT_NOTIFYWILLJONTABLE = 438; -- 通知好友跟随请求
local CMDT_REQREPLYWILLJONTABLE = 439; -- 请求回复跟随
local CMDT_RESPREPLYWILLJONTABLE = 440;
-- local CMDT_NOTIFYWILLJONTABLERESULT         = 441;  --回复玩家跟随结果
local CMDT_REQFOLLOWEDDLIST = 442; -- 请求跟随的列表(进入游戏区的时候请求一次，重连请求一次，其他情况依赖推送更新)
local CMDT_RESPFOLLOWEDDLIST = 443;
local CMDT_REQRESERVEGAME = 450; -- 请求预约
local CMDT_RESPRESERVEGAME = 451;
local CMDT_NOTIFYRESERVEGAMEINFO = 452; -- 通知预约消息
local CMDT_REQREPLYRESERVEGAME = 453; -- 请求回复预约消息
local CMDT_RESPREPLYRESERVEGAME = 454;
local CMDT_REQONEKEYINVITE = 455; -- 一键邀请
local CMDT_RESPONEKEYINVITE = 456;
local CMDT_REQINVITEMERGELIST = 457; -- 请求所有消息通知列表(邀请+跟随+预约)
local CMDT_RESPINVITEMERGELIST = 458;
local CMDT_REQTABLEINFO = 459; -- 获取桌子信息
local CMDT_RESPTABLEINFO = 460;
local CMDT_REQSEARCHFRIEND = 462; -- 请求搜索历史好友
local CMDT_RESPSEARCHFRIEND = 463;
local CMDT_REQLIMITFRIEND  = 464; -- 屏蔽好友
local CMDT_RESPLIMITFRIEND = 465;
local CMDT_REQFRIENDINFO = 466; -- 请求好友信息
local  CMDT_RESPFRIENDINFO = 467;

local CMDT_REQGETAPPLYS = 468; -- 获取申请加好友列表
local CMDT_RESPGETAPPLYS = 469;
local CMDT_REQADDFRIEND = 470; -- 添加好友
local CMDT_RESPADDFRIEND = 471;
local CMDT_REQDELFRIEND = 472; -- 删除好友
local CMDT_RESPDELFRIEND = 473;
local CMDT_REQADDFRIENDSTATE = 474; -- 添加好友的状态 接受、拒绝、未处理
local CMDT_RESPADDFRIENDSTATE = 475;
local CMDT_REQDEALFRIENDAPPLY = 476; -- 处理加好友请求
local CMDT_RESPDEALFRIENDAPPLY = 477;
local CMDT_NOTIFYFRIENDLISTCHANGE = 478; -- 增加、删除 通知

local CMDT_REQ_REALTIME_GAME_RECORD = 3000 -- 请求回放
local CMDT_RESP_REALTIME_GAME_RECORD = 3001 -- 返回回放
local CMDT_REQ_UNWATCH_REALTIME_GAME_RECORD = 3002 -- 请求不观战
local CMDT_RESP_UNWATCH_REALTIME_GAME_RECORD = 3003 -- 返回不观战

-- 统一错误码
IMProtocol.ERRORCODE = {
    SUCCESS = 0,
    EC_PLEASE_PROCESS_BASE_PROTOCOL_FIRSTLY = 10000, -- 未加入玩家列表，请走基础协议
    EC_CUR_PACKET_IS_INVALID = 10001, -- 当前请求页是无效的
    EC_ALREADY_INVITED_THIS_FRIEND = 10100, -- 已经邀请过该好友
    EC_CANT_FIND_INVITE_INFO = 10101, -- 找不到邀请信息
    EC_INVITEDD_AND_INVITE_NOT_FRIEND = 10102, -- 邀请者和被邀请者不是好友
    EC_FRIEND_CANT_BE_INVITEDD = 10103, -- 好友不能被邀请，状态不符(好友状态必须是在线或者等待)
    EC_CANT_INVITE_SELF = 10104, -- 不能邀请自己
    EC_CANT_GENERATE_INVITE_ID = 10105, -- 不能生成邀请编号
    EC_CANT_GENERATE_INVITE_INFO = 10106, -- 不能生成邀请信息
    EC_INVITE_PLAYER_NOT_IN_TABLE = 10107, -- 发出邀请的玩家不在桌子上
    EC_HAVE_SAME_ROOM_INVITE = 10108, -- 被邀请的玩家已经有相同的房间邀请了
    EC_CANT_IN_SAME_ROOM = 10109, -- 邀请的玩家和被邀请的玩家不能在同一个房间中
    EC_TABLE_FULL = 10110, -- 桌子上已经坐满了
    EC_TABLE_IS_NOT_EXIST = 10111, -- 桌子不存在
    EC_REPEAT_FOLLOWED = 10112, -- 重复跟随
    EC_HAVE_INTABLE = 10113, -- 玩家已经在桌子上
    EC_HAVE_GAMEING = 10114, -- 玩家已经在游戏中了
    EC_HAVE_JOIN_OTHER_ROOM = 10115, -- 玩家已经跟随了其他人
    EC_ROOM_OWNER = 10116, -- 房主不能被拉走
    EC_ROOM_OWNER_CANT_AGREE_INVITE = 10117, -- 房主不能被邀请
    EC_CANT_RESERVE_SELF = 10118, -- 不能预约自己
    EC_FRIEND_CANT_BE_RESERVED = 10119, -- 好友不能被预约，状态不对
    EC_CANT_GENERATE_RESERVE_INFO = 10120, -- 不能生成预约信息
    EC_ALREADY_RESERVED_THIS_FRIEND = 10121, -- 已经预约过该好友
    EC_FRIEND_IS_NOT_WAITING = 10122, -- 该好友不在等待状态
    EC_RESERVE_IS_INVALID = 10123, -- 预约已经失效
    EC_RESERVE_TABLEID_ERROR = 10124, -- 预约者的房间信息异常
    EC_ONEKEYINVITE_OVER = 10125, -- 当天一键邀请已经用过了
    EC_HAVEUSED_SEARCH = 10126, -- 已经用过查找历史牌友功能
    EC_WAITINT_PERIOD_OF_TIME = 10127, -- 等一会儿
    EC_INVITE_TOO_FREQUENTLY = 10128, -- 邀请太频繁
    EC_PLAYER_LIMIT_IN_TABLE = 10129, -- 桌子上有玩家 限制（请求者）入桌
    EC_PLAYER_BAN = 10130 -- 禁止游戏
}

IMProtocol.ERRORSTR = {
    [10100] = "请勿重复邀请",
    [10101] = "找不到邀请信息",
    [10102] = "好友无法邀请",
    [10103] = "好友已经离线或者游戏中，邀请失败",
    [10104] = "无法邀请自己",
    [10105] = "不能生成邀请编号",
    [10106] = "不能生成邀请信息",
    [10107] = "对方已离开房间",
    [10108] = "已经有相同的房间邀请",
    [10109] = "邀请的玩家和被邀请的玩家不能在同一个房间中",
    [10110] = "房间人数已达上限，邀请失败",
    [10111] = "桌子不存在",
    [10112] = "请勿重复跟随",
    [10113] = "玩家已经在桌子上",
    [10114] = "玩家已经在游戏中",
    [10115] = "玩家已经跟随其他人",
    [10116] = "跟随失败！好友已开始游戏",
    [10117] = "加入新房间需退出当前房间",
    [10118] = "不能预约自己",
    [10119] = "好友状态错误，预约失败",
    [10120] = "生成预约信息失败",
    [10121] = "请勿重复预约",
    [10122] = "好友状态变更，预约失败",
    [10123] = "预约已经失效",
    [10124] = "您当前房间信息异常，预约失败",
    [10125] = "今日一键邀请使用次数已达上限",
    [10126] = "该功能仅限使用一次",
    [10127] = "【%s】：等我一会，马上就来",
    [10128] = "该玩家正忙，请稍后再试",
    [10129] = "你已被本桌牌友屏蔽入座，无法加入该对局"
}

-- 玩家状态
IMProtocol.PLAYERSTATE = {
    PS_OFFLINE = 1, -- 离线状态
    PS_GAME = 2, -- 游戏状态
    PS_ONLINE = 4, -- 在线状态
    PS_WAIT = 8,-- 等待状态
    PS_SELF = 99 -- 自己
}

-- 回复邀请类型
IMProtocol.REPLYTYPE = {
    AGREE = 0, -- 同意邀请
    REFUSE = 1, -- 拒绝邀请
    WAIT = 2 -- 等我一会
}

-- 通知消息类型
IMProtocol.MESSAGE_TYPE = {
    INVITE = 1, -- 邀请消息
    FOLLOW = 2, -- 跟随消息
    ORDER = 3 -- 预约消息
}

-- 支付方式
IMProtocol.PAYTYPE = {HOST = 0, AA = 1}

-- 屏蔽状态
IMProtocol.SHIELDTYPE = {
    NOTSHIELD = 0, -- 取消屏蔽
    SHIELD = 1, -- 屏蔽
}

IMProtocol.ReqAppidList = {
    XY_ID = CMDT_REQAPPIDLIST,
    askid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        return bos
    end
}

IMProtocol.RespAppidList = {
    XY_ID = CMDT_RESPAPPIDLIST,
    askid = 0,
    ec = 0,
    count = 0,
    appid = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.count = bis:readInt32()
        for i = 1, self.count do
            self.appid[i] = bis:readInt32()
        end
        return self
    end
}

IMProtocol.NotifyServerState = {
    XY_ID = CMDT_NOTIFYSERVERSTATE,
    appid = 0,
    server_state = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.appid = bis:readInt32()
        self.server_state = bis:readInt32()
        return self
    end
}

IMProtocol.ReqPlayerConnect = {
    XY_ID = CMDT_REQPLAYERCONNECT,
    askid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        return bos
    end
}

IMProtocol.RespPlayerConnect = {
    XY_ID = CMDT_RESPPLAYERCONNECT,
    askid = 0,
    im_appid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.im_appid = bis:readInt32()
        return self
    end
}

IMProtocol.ReqKeepAlive = {
    XY_ID = CMDT_REQKEEPALIVE,
    askid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        return bos
    end
}

IMProtocol.RespKeepAlive = {
    XY_ID = CMDT_RESPKEEPALIVE,
    askid = 0,
    ec = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        return self
    end
}

IMProtocol.ReqJoinIM = {
    XY_ID = CMDT_REQJOINIM,

    askid = 0,
    channelid = 0, -- 区分浙江包、ios企业包等
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
        bos:writeInt32(self.channelid)
        bos:writeString(self.nickname)
        bos:writeString(self.headurl)
        return bos
    end
}

IMProtocol.RespJoinIM = {
    XY_ID = CMDT_RESPJOINIM,

    askid = 0,
    ec = 0,
    im_appid = 0,
    im_seqid = 0,
    server_time = 0,
    threshold = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.im_appid = bis:readInt32()
        self.im_seqid = bis:readInt32()
        self.server_time = bis:readUInt64()
        if bis:getAvailableSize() > 0 then
            self.threshold = bis:readInt32()
        end
        return self
    end
}

IMProtocol.ReqOpenFriendList = {
    XY_ID = CMDT_REQOPENFRIENDLIST,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        return bos
    end
}

IMProtocol.RespOpenFriendList = {
    XY_ID = CMDT_RESPOPENFIENDLIST,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        return self
    end
}

IMProtocol.ReqCloseFriendList = {
    XY_ID = CMDT_REQCLOSEFRIENDLIST,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        return bos
    end
}

IMProtocol.RespCloseFriendList = {
    XY_ID = CMDT_RESPCLOSEFRIENDLIST,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        return self
    end
}

IMProtocol.NotifyPlayerInfo = {
    XY_ID = CMDT_NOTIFYPLAYERINFO,
    friendInfo = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.friendInfo = {}

        self.friendInfo.areaid = bis:readInt32()
        self.friendInfo.numid = bis:readInt32()
        self.friendInfo.channelid = bis:readInt32()
        self.friendInfo.last_login_time = bis:readUInt64()
        self.friendInfo.last_fight_time = bis:readUInt64()
        self.friendInfo.nickname = bis:readString()
        self.friendInfo.headurl = bis:readString()
        self.friendInfo.player_state = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.friendInfo.shieldState = bis:readInt32()
            local timepropcnt = bis:readInt32()
            self.friendInfo.timeprop = {}
            for propindex = 1, timepropcnt do
                self.friendInfo.timeprop[propindex] = bis:readInt32()
            end
        end
        if bis:getAvailableSize() > 0 then
            self.friendInfo.tea_online = bis:readInt8()
            self.friendInfo.chair_count = bis:readInt32()
            self.friendInfo.user_count = bis:readInt32()
            self.friendInfo.roomid = bis:readInt32()
            self.friendInfo.gameid = bis:readInt32()
        end
        return self
    end
}

IMProtocol.ReqFriendList = {
    XY_ID = CMDT_REQFRIENDLIST,
    askid = 0,
    cur_package = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        bos:writeUInt16(self.cur_package)
        return bos
    end
}

IMProtocol.RespFriendList = {
    XY_ID = CMDT_RESPFRIENDLIST,
    askid = 0,
    ec = 0,
    total_package = 0,
    cur_package = 0,
    count = 0,
    friendInfo = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.total_package = bis:readUInt16()
        self.cur_package = bis:readUInt16()
        self.count = bis:readUInt8()
        self.friendInfo = {}
        for _ = 1, self.count do
            local tmp = {}
            tmp.areaid = bis:readInt32()
            tmp.numid = bis:readInt32()
            tmp.channelid = bis:readInt32()
            tmp.last_login_time = bis:readUInt64()
            tmp.last_fight_time = bis:readUInt64()
            tmp.nickname = bis:readString()
            tmp.headurl = bis:readString()
            tmp.player_state = bis:readInt32()
            table.insert(self.friendInfo, tmp)
        end
        if bis:getAvailableSize() > 0 then
            for index = 1, self.count do
                self.friendInfo[index].shieldState = bis:readInt32()
            end
        end
        if bis:getAvailableSize() > 0 then
            for index = 1, self.count do
                local timepropcnt = bis:readInt32()
                self.friendInfo[index].timeprop = {}
                for propindex = 1, timepropcnt do
                    self.friendInfo[index].timeprop[propindex] = bis:readInt32()
                end
            end
        end
        if bis:getAvailableSize() > 0 then
            for index = 1, self.count do
                self.friendInfo[index].bInTea = bis:readBool()
            end
        end
        if bis:getAvailableSize() > 0 then
            for index = 1, self.count do
                self.friendInfo[index].chair_count = bis:readInt32()
                self.friendInfo[index].user_count = bis:readInt32()
                self.friendInfo[index].roomid = bis:readInt32()
                self.friendInfo[index].gameid = bis:readInt32()
            end
        end
        return self
    end
}

IMProtocol.ReqInviteGame = {
    XY_ID = CMDT_REQINVITEGAME,
    askid = 0,
    inviteId = 0,
    inviteAreaid = 0,
    invitePlayer = 0,
    beInviteAreaid = 0,
    beInvitePlayer = 0,
    askTime = 0,
    roomid = 0,
    gameid = 0,
    charis = 0,
    gameAppid = 0,
    tableid = 0,
    gamerule = "",
    extend = "",
    askName = "",
    invitedd_nickname = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        bos:writeInt32(self.inviteId)
        bos:writeInt32(self.inviteAreaid)
        bos:writeInt32(self.invitePlayer)
        bos:writeInt32(self.beInviteAreaid)
        bos:writeInt32(self.beInvitePlayer)
        bos:writeUInt64(self.askTime)
        bos:writeInt32(self.roomid)
        bos:writeInt32(self.gameid)
        bos:writeInt32(self.charis)
        bos:writeInt32(self.gameAppid)
        bos:writeInt32(self.tableid)
        bos:writeString(self.gamerule)
        bos:writeString(self.extend)
        bos:writeString(self.askName)
        bos:writeString(self.invitedd_nickname)
        return bos
    end
}

IMProtocol.RespInviteGame = {
    XY_ID = CMDT_RESPINVITEGAME,
    askid = 0,
    ec = 0,
    inviteAreaid = 0,
    inviteNumid = 0,
    inviteId = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.inviteAreaid = bis:readInt32()
        self.inviteNumid = bis:readInt32()
        self.inviteId = bis:readInt32()
        return self
    end
}

IMProtocol.NotifyInviteInfo = {
    XY_ID = CMDT_NOTIFYINVITEINFO,

    inviteId = 0,
    inviteAreaid = 0,
    invitePlayer = 0,
    beInviteAreaid = 0,
    beInvitePlayer = 0,
    askTime = 0,
    roomid = 0,
    gameid = 0,
    charis = 0,
    gameAppid = 0,
    tableid = 0,
    gamerule = "",
    extend = "",
    askName = "",
    invitedd_nickname = "",
    gameName = "",
    payType = 0,
    headUrl = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.inviteId = bis:readInt32()
        self.inviteAreaid = bis:readInt32()
        self.invitePlayer = bis:readInt32()
        self.beInviteAreaid = bis:readInt32()
        self.beInvitePlayer = bis:readInt32()
        self.askTime = bis:readUInt64()
        self.roomid = bis:readInt32()
        self.gameid = bis:readInt32()
        self.charis = bis:readInt32()
        self.gameAppid = bis:readInt32()
        self.tableid = bis:readInt32()
        self.gamerule = bis:readString()
        self.extend = bis:readString()
        self.askName = bis:readString()
        self.invitedd_nickname = bis:readString()
        self.gameName = bis:readString()
        self.payType = bis:readInt32()
        self.headUrl = bis:readString()
        return self
    end
}

IMProtocol.ReqGetInviteInfo = {
    XY_ID = CMDT_REQGETINVITEINFO,
    askid = 0,
    cur_package = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        bos:writeUInt16(self.cur_package)
        return bos
    end
}

IMProtocol.RespGetInviteInfo = {
    XY_ID = CMDT_RESPGETINVITEINFO,
    askid = 0,
    ec = 0,
    total_package = 0,
    cur_package = 0,
    count = 0,
    inviteInfo = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.total_package = bis:readUInt16()
        self.cur_package = bis:readUInt16()
        self.count = bis:readUInt8()
        self.inviteInfo = {}
        for _ = 1, self.count do
            local tmp = {}
            tmp.invite_id = bis:readInt32()
            tmp.invite_areaid = bis:readInt32()
            tmp.invite_numid = bis:readInt32()
            tmp.invitedd_areaid = bis:readInt32()
            tmp.invitedd_numid = bis:readInt32()
            tmp.askTime = bis:readUInt64()
            tmp.game_roomid = bis:readInt32()
            tmp.gameid = bis:readInt32()
            tmp.chairs = bis:readInt32()
            tmp.game_appid = bis:readInt32()
            tmp.tableid = bis:readInt32()
            tmp.gamerule = bis:readString()
            tmp.extend = bis:readString()
            table.insert(self.friendInfo, tmp)
        end
        return self
    end
}

IMProtocol.ReqReplyInviteGame = {
    XY_ID = CMDT_REQREPLYINVITEGAME,
    askid = 0,
    invite_id = 0,
    reply_type = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        bos:writeInt32(self.invite_id)
        bos:writeUInt8(self.reply_type)
        return bos
    end
}

IMProtocol.RespReplyInviteGame = {
    XY_ID = CMDT_RESPREPLYINVITEGAME,
    askid = 0,
    ec = 0,
    invite_id = 0,
    game_roomid = 0,
    host = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.invite_id = bis:readInt32()
        self.game_roomid = bis:readInt32()
        self.host = bis:readUInt8()
        return self
    end
}

IMProtocol.NotifyReplyInviteGame = {
    XY_ID = CMDT_NOTIFYREPLYINVITEGAME,

    invite_id = 0,
    reply_areaid = 0, -- 被邀请方
    reply_numid = 0,
    reply_invite_type = 0,
    reply_nickname = "",
    ec = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.invite_id = bis:readInt32()
        self.reply_areaid = bis:readInt32()
        self.reply_numid = bis:readInt32()
        self.reply_invite_type = bis:readUInt8()
        self.reply_nickname = bis:readString()
        if bis:getAvailableSize() > 0 then
            self.ec = bis:readInt32()
        end
        return self
    end
}

IMProtocol.ReqInviteddList = {
    XY_ID = CMDT_REQINVITEDDLIST,
    askid = 0,
    cur_package = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        bos:writeUInt16(self.cur_package)
        return bos
    end
}

IMProtocol.RespInviteddList = {
    XY_ID = CMDT_RESPINVITEDDLIST,
    askid = 0,
    ec = 0,
    total_package = 0,
    cur_package = 0,
    count = 0,
    inviteInfo = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.total_package = bis:readUInt16()
        self.cur_package = bis:readUInt16()
        self.count = bis:readUInt8()
        self.inviteInfo = {}
        for _ = 1, self.count do
            local tmp = {}
            tmp.invite_id = bis:readInt32()
            tmp.invite_areaid = bis:readInt32()
            tmp.invite_numid = bis:readInt32()
            tmp.invitedd_areaid = bis:readInt32()
            tmp.invitedd_numid = bis:readInt32()
            tmp.askTime = bis:readUInt64()
            tmp.game_roomid = bis:readInt32()
            tmp.gameid = bis:readInt32()
            tmp.chairs = bis:readInt32()
            tmp.game_appid = bis:readInt32()
            tmp.tableid = bis:readInt32()
            tmp.gamerule = bis:readString()
            tmp.extend = bis:readString()
            tmp.askName = bis:readString()
            tmp.invitedd_nickname = bis:readString()
            table.insert(self.inviteInfo, tmp)
        end
        return self
    end
}

IMProtocol.ReqFriendTableList = {
    XY_ID = CMDT_REQFRIENDTABLEINFO,
    askid = 0,
    cur_package = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        bos:writeUInt16(self.cur_package)
        return bos
    end
}

IMProtocol.RespFriendTableList = {
    XY_ID = CMDT_RESPFRIENDTABLEINFO,
    askid = 0,
    ec = 0,
    total_package = 0,
    cur_package = 0,
    count = 0,
    friendTableInfo = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.total_package = bis:readUInt16()
        self.cur_package = bis:readUInt16()
        self.count = bis:readUInt8()
        self.friendTableInfo = {}
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
            self.friendTableInfo[i] = tmp
        end
        return self
    end
}

IMProtocol.ReqWillJoinTable = {
    XY_ID = CMDT_REQWILLJOINTABLE,

    askid = 0,
    gameid = 0,
    game_appid = 0,
    tableid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        bos:writeInt32(self.gameid)
        bos:writeInt32(self.game_appid)
        bos:writeInt32(self.tableid)

        return bos
    end
}

IMProtocol.RespWillJoinTable = {
    XY_ID = CMDT_RESPWILLJOINTABLE,

    askid = 0,
    ec = 0,
    state = 0,
    tableid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.state = bis:readInt32()
        self.tableid = bis:readInt32()

        return self
    end
}

IMProtocol.NotifyWillJoinTable = {
    XY_ID = CMDT_NOTIFYWILLJONTABLE,

    tableid = 0, -- 跟随id
    askTime = 0, -- 跟随申请发出时间
    follow_areaid = 0, -- 跟随的玩家
    follow_numid = 0,
    askName = "",
    follow_headurl = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.tableid = bis:readInt32()
        self.askTime = bis:readUInt64()
        self.follow_areaid = bis:readInt32()
        self.follow_numid = bis:readInt32()
        self.askName = bis:readString()
        self.follow_headurl = bis:readString()
        return self
    end
}

IMProtocol.ReqReplyFollow = {
    XY_ID = CMDT_REQREPLYWILLJONTABLE,
    askid = 0,
    areaid = 0,
    numid = 0,
    reply_follow_type = 0,

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
        bos:writeUInt8(self.reply_follow_type)
        return bos
    end
}

IMProtocol.RespReplyFollow = {
    XY_ID = CMDT_RESPREPLYWILLJONTABLE,
    askid = 0,
    ec = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        return self
    end
}

IMProtocol.ReqFolloweddList = {
    XY_ID = CMDT_REQFOLLOWEDDLIST,
    askid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        return bos
    end
}

IMProtocol.RespFolloweddList = {
    XY_ID = CMDT_RESPFOLLOWEDDLIST,
    askid = 0,
    ec = 0,
    total_package = 0,
    cur_package = 0,
    count = 0,
    players = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.total_package = bis:readUInt16()
        self.cur_package = bis:readUInt16()
        self.count = bis:readUInt8()
        self.players = {}
        for _ = 1, self.count do
            local tmp = {}
            tmp.askTime = bis:readUInt64()
            tmp.follow_areaid = bis:readInt32()
            tmp.follow_numid = bis:readInt32()
            tmp.askName = bis:readString()
            tmp.follow_headurl = bis:readString()
            table.insert(self.players, tmp)
        end
        return self
    end
}

IMProtocol.ReqReserveGame = {
    XY_ID = CMDT_REQRESERVEGAME,
    askid = 0,
    areaid = 0,
    numid = 0,

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
        return bos
    end
}

IMProtocol.RespReserveGame = {
    XY_ID = CMDT_RESPRESERVEGAME,
    askid = 0,
    ec = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        return self
    end
}

IMProtocol.NotifyReserveGame = {
    XY_ID = CMDT_NOTIFYRESERVEGAMEINFO,

    areaid = 0,
    numid = 0,
    askTime = 0,
    askName = "",
    roomid = 0,
    charis = 0,
    payType = 0,
    gameName = "",
    headUrl = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.areaid = bis:readInt32()
        self.numid = bis:readInt32()
        self.askTime = bis:readInt32()
        self.askName = bis:readString()
        self.roomid = bis:readInt32()
        self.charis = bis:readInt32()
        self.payType = bis:readInt32()
        self.gameName = bis:readString()
        self.headUrl = bis:readString()
        return self
    end
}

IMProtocol.ReqReplyReserveGame = {
    XY_ID = CMDT_REQREPLYRESERVEGAME,

    askid = 0,
    areaid = 0,
    numid = 0,
    flag = 0,

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
        bos:writeUInt8(self.flag)
        return bos
    end
}

IMProtocol.RespReplyReserveGame = {
    XY_ID = CMDT_RESPREPLYRESERVEGAME,
    askid = 0,
    ec = 0,
    roomid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.roomid = bis:readInt32()
        return self
    end
}

IMProtocol.ReqQuickInvite = {
    XY_ID = CMDT_REQONEKEYINVITE,
    askid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        return bos
    end
}

IMProtocol.RespQuickInvite = {
    XY_ID = CMDT_RESPONEKEYINVITE,
    askid = 0,
    ec = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        return self
    end
}

IMProtocol.ReqAllMessageList = {
    XY_ID = CMDT_REQINVITEMERGELIST,
    askid = 0,
    cur_package = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        bos:writeUInt16(self.cur_package)
        return bos
    end
}

IMProtocol.RespAllMessageList = {
    XY_ID = CMDT_RESPINVITEMERGELIST,
    askid = 0,
    ec = 0,
    total_package = 0,
    cur_package = 0,
    count = 0,
    messageInfo = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.total_package = bis:readUInt16()
        self.cur_package = bis:readUInt16()
        self.count = bis:readUInt8()
        self.messageInfo = {}
        for _ = 1, self.count do
            local tmp = {}
            tmp.id = bis:readInt32()
            tmp.fromAreaid = bis:readInt32()
            tmp.fromNumid = bis:readInt32()
            tmp.toAreaid = bis:readInt32()
            tmp.toNumid = bis:readInt32()
            tmp.time = bis:readUInt64()
            tmp.fromNickName = bis:readString()
            tmp.toNickName = bis:readString()
            tmp.msgType = bis:readInt32()
            tmp.roomid = bis:readInt32()
            tmp.charis = bis:readInt32()
            tmp.payType = bis:readInt32()
            tmp.gameName = bis:readString()
            tmp.headUrl = bis:readString()
            table.insert(self.messageInfo, tmp)
        end
        return self
    end
}

IMProtocol.ReqTableInfo = {
    XY_ID = CMDT_REQTABLEINFO,
    askid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        return bos
    end
}

IMProtocol.RespTableInfo = {
    XY_ID = CMDT_RESPTABLEINFO,
    askid = 0,
    ec = 0,
    roomid = 0,
    gameid = 0,
    maxcount = 0,
    chairs = 0,
    gamename = "",
    gamerule = "",
    paytype = 0,
    teapwd = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.roomid = bis:readInt32()
        self.gameid = bis:readInt32()
        self.maxcount = bis:readInt32()
        self.chairs = bis:readInt32()
        self.gamename = bis:readString()
        self.gamerule = bis:readString()
        self.paytype = bis:readInt32()
        self.teapwd = bis:readInt32()
        return self
    end
}

IMProtocol.ReqHistoryFriends = {
    XY_ID = CMDT_REQSEARCHFRIEND,
    askid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        return bos
    end
}

IMProtocol.RespHistoryFriends = {
    XY_ID = CMDT_RESPSEARCHFRIEND,
    askid = 0,
    ec = 0,
    total = 0,
    begin = 0,
    count = 0,
    oldfriends = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.total = bis:readInt32()
        self.begin = bis:readInt32()
        self.count = bis:readInt32()
        self.oldfriends = {}
        for _ = 1, self.count do
            local tmp = {}
            tmp.url = bis:readString()
            tmp.nickname = bis:readString()
            table.insert(self.oldfriends, tmp)
        end
        return self
    end
}

IMProtocol.ReqLimitFriend = {
    XY_ID = CMDT_REQLIMITFRIEND,
    askid = 0,
    numid = 0,
    flag = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        bos:writeInt32(self.numid)
        bos:writeInt32(self.flag)
        return bos
    end
}

IMProtocol.RespLimitFriend = {
    XY_ID = CMDT_RESPLIMITFRIEND,
    askid = 0,
    numid = 0,
    flag = 0,
    ec = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.numid = bis:readInt32()
        self.flag = bis:readInt32()
        self.ec = bis:readInt32()
        return self
    end
}

IMProtocol.ReqFriendInfo = {
    XY_ID = CMDT_REQFRIENDINFO,
    askid = 0,
    numid = 0,
    
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    
    bostream = function(self)
        local bos = un.network.IStream.new()
        
        bos:writeInt32(self.askid)
        bos:writeInt32(self.numid)
        return bos
    end
}

IMProtocol.RespFriendInfo = {
    XY_ID = CMDT_RESPFRIENDINFO,
    askid = 0,
    bFriend = 0,
    info = {},
    
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    
    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        
        self.askid = bis:readInt32()
        self.bFriend = bis:readInt32()
        local tmp = {}
        tmp.areaid = bis:readInt32()
        tmp.numid = bis:readInt32()
        tmp.channelid = bis:readInt32()
        tmp.last_login_time = bis:readUInt64()
        tmp.last_fight_time = bis:readUInt64()
        tmp.nickname = bis:readString()
        tmp.headurl = bis:readString()
        tmp.player_state = bis:readInt32()
        tmp.shieldState = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            local timepropcnt = bis:readInt32()
            tmp.timeprop = {}
            for propindex = 1, timepropcnt do
                tmp.timeprop[propindex] = bis:readInt32()
            end
        end
        self.info = tmp
        return self
    end
}

IMProtocol.ReqGetApplys = {
    XY_ID = CMDT_REQGETAPPLYS,
    askid = 0,
    maxtime = 0,
    
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    
    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.maxtime)
        return bos
    end
}

IMProtocol.RespGetApplys = {
    XY_ID = CMDT_RESPGETAPPLYS,
    askid = 0,
    infocount = 0,
    infos = {},
    mintime = 0,
    totalcount = 0,
    
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    
    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.infocount = bis:readInt32()
        self.infos = {}
        for _ = 1, self.infocount do
            local tmp = {}
            tmp.numid = bis:readInt32()
            tmp.nickname = bis:readString()
            tmp.headurl = bis:readString()
            tmp.online = bis:readBool()
            table.insert( self.infos,tmp)
        end
        self.mintime = bis:readInt32()
        self.totalcount = bis:readInt32()
        return self
    end
}

IMProtocol.ReqAddFriend = {
    XY_ID = CMDT_REQADDFRIEND,
    askid = 0,
    numid = 0,
    
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    
    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.numid)
        return bos
    end
}

IMProtocol.RespAddFriend = {
    XY_ID = CMDT_RESPADDFRIEND,
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
        self.flag = bis:readInt32()
        return self
    end
}

IMProtocol.ReqDelFriend = {
    XY_ID = CMDT_REQDELFRIEND,
    askid = 0,
    numid = 0,
    
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    
    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.numid)
        return bos
    end
}

IMProtocol.RespDelFriend = {
    XY_ID = CMDT_RESPDELFRIEND,
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
        self.flag = bis:readInt32()
        return self
    end
}

IMProtocol.ReqAddFriendState = {
    XY_ID = CMDT_REQADDFRIENDSTATE,
    askid = 0,
    numid = 0,
    
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    
    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.numid)
        return bos
    end
}

IMProtocol.RespAddFriendState = {
    XY_ID = CMDT_RESPADDFRIENDSTATE,
    askid = 0,
    info = {},
    state = 0,
    joinLimit = 0,
    lastLoginTime = 0,
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    
    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        local tmp = {}
        tmp.numid = bis:readInt32()
        tmp.nickname = bis:readString()
        tmp.headurl = bis:readString()
        tmp.online = bis:readBool()
        self.playerinfo = tmp
        self.state = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.joinLimit = bis:readInt32()
            self.lastLoginTime = bis:readInt32()
        end
        return self
    end
}

IMProtocol.ReqDealFriendApply = {
    XY_ID = CMDT_REQDEALFRIENDAPPLY,
    askid = 0,
    numid = 0,
    accept = false,
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    
    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.numid)
        bos:writeBool(self.accept)
        return bos
    end
}

IMProtocol.RespDealFriendApply = {
    XY_ID = CMDT_RESPDEALFRIENDAPPLY,
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
        self.flag = bis:readInt32()
        return self
    end
}

IMProtocol.NotifyFriendListChange = {
    XY_ID = CMDT_NOTIFYFRIENDLISTCHANGE,
    askid = 0,
    flag = 0,
    info = {},
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    
    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.flag = bis:readInt32()
        local tmp = {}
        tmp.numid = bis:readInt32()
        tmp.nickname = bis:readString()
        tmp.headurl = bis:readString()
        tmp.online = bis:readBool()
        self.info = tmp
        return self
    end
}

-- 回放型旁观
IMProtocol.ReqRealtimeGameRecord = {
    XY_ID = CMDT_REQ_REALTIME_GAME_RECORD,

    askid = 0,
    room_id = 0,
    offset = 0,
    before_round = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.room_id)
        bos:writeInt32(self.offset)
        bos:writeInt32(self.before_round)
        return bos
    end
}

IMProtocol.RespRealtimeGameRecord = {
    XY_ID = CMDT_RESP_REALTIME_GAME_RECORD,

    FLAG = {
        NOT_GOOD = 1, -- 数据不完整
    },

    askid = 0,
    flag = 0,
    room_id = 0,
    max_offset = 0,
    current = 0,
    total = 0,
    zip = 0,
    payload_size = 0,
    payload = '',

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.flag = bis:readInt32()
        self.room_id = bis:readInt32()
        self.max_offset = bis:readInt32()
        self.current = bis:readInt32()
        self.total = bis:readInt32()
        self.zip = bis:readInt32()
        self.payload_size = bis:readInt32()
        if self.payload_size > 0 then
            self.payload = bis:read(self.payload_size)
        end
        return self
    end
}

IMProtocol.ReqUnwatchRealtimeGameRecord = {
    XY_ID = CMDT_REQ_UNWATCH_REALTIME_GAME_RECORD,

    askid = 0,
    room_id = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.room_id)
        return bos
    end
}

IMProtocol.RespUnwatchRealtimeGameRecord = {
    XY_ID = CMDT_RESP_UNWATCH_REALTIME_GAME_RECORD,

    askid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        return self
    end
}

for _, v in pairs(IMProtocol) do
    v.processid = 100
    if v.XY_ID then
        v.event_key = v.processid .. "_" .. v.XY_ID
    end
end
IMProtocol.processid = 100

return IMProtocol
 ��  