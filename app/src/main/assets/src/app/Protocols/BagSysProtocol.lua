local BagSysProtocol = {}

--[[
//基础协议流程如下:
//1、appid传0，REQAPPIDLIST，获取到appid列表L
//2、根据列表L,指定appid，依次发送REQPLAYERCONNECT，根据回包里的appid，后面的业务逻辑(400开始的协议)全部指定appid，该appid只是建议，服务端不做强校验
//3、每隔100秒，向每个appid发送REQKEEPALIVE
//4、若某个服务挂掉，会推送NOTIFYSERVERSTATE，若刚好和指定的appid相同，则重新走1流程
//5、若某个服务启动，会推送NOTIFYSERVERSTATE，重新走流程1
//6、若收到srs协议9，表示转发失败，重新走流程1
]]

--------------------- 基础协议
local CMDT_REQAPPIDLIST = 501;
local CMDT_RESPAPPIDLIST = 502;

local CMDT_NOTIFYSERVERSTATE = 503;

local CMDT_REQPLAYERCONNECT = 504;
local CMDT_RESPPLAYERCONNECT = 505;

local CMDT_REQKEEPALIVE = 506; -- 请求发送心跳，该协议100秒发送一次即可
local CMDT_RESPKEEPALIVE = 507;

-- local CMDT_REQCLIENTIP                      = 508;  -- 请求玩家ip，该协议是模拟协议(服务端使用)
-- local CMDT_REQCLIENTCLOSE                   = 509;  -- 客户端关闭，该协议是模式协议(服务端使用)

local CMDT_REQJOINBOXPROP = 601; -- 请求加入prop，appid必须指定
local CMDT_RESPJOINBOXPROP = 602;

--------------------- 逻辑协议

local CMDT_REQPROPSCONFIG = 603; -- 请求获取道具配置，包含基础配置（props配置表）以及web配置
local CMDT_RESPPROPSCONFIG = 604;

local CMDT_REQBACKPACKDATA = 605; -- 请求背包数据,可以是自己的或者是其他玩家的
local CMDT_RESPBACKPACKDATA = 606;

local CMDT_REQUSEPROPS = 607; -- 请求使用道具，若是数量道具，则数量减去N。若是时效道具，数量减N，且添加时效（users_timeprops表）
local CMDT_RESPUSEPROPS = 608;

local CMDT_REQOPERATEPROPS = 609; -- 操作道具，给时效道具附加属性以及状态 OK
local CMDT_RESPOPERATEPROPS = 610;

local CMDT_NOTIFYPROPSDATA = 611; -- 通知道具增加消息，充值成功加道具或者Task加道具通知

local CMDT_REQGIFTPACKINFO = 612; -- 请求礼拜限购次数相关
local CMDT_RESPGIFTPACKINFO = 613; 

-- 统一错误码
BagSysProtocol.ERRORCODE = {
    SUCCESS = 0,
    EC_PLEASE_PROCESS_BASE_PROTOCOL_FIRSTLY = 20000, -- 未加入玩家列表，请走基础协议
    EC_PLAYER_DONT_HAVE_THE_PROPS = 20001, -- 玩家没有该道具，可能已经过期或者用完
    EC_THE_PROPS_ALREAD_IN_BODY = 20002, -- 该道具已经在身上，无需再装备
    EC_THE_PROPS_ALREAD_IN_BACKPACK = 20003 -- 该道具已经在背包，无需再卸下
}

BagSysProtocol.ERRORSTR = {
    [20000] = "未加入玩家列表!",
    [20001] = "玩家没有该道具,可能已经过期或用完！",
    [20002] = "该道具已装备,无需再装备!",
    [20003] = "该道具已经在背包,无需再卸下!"
}

-- 服务状态
BagSysProtocol.SERVER_STATE = {
    SS_START = 0, -- 启动
    SS_STOP = 1 -- 关闭
}

-- 道具状态
BagSysProtocol.PROPS_STATE = {
    PS_IN_BACKPACK = 0, -- 在背包中(未装备)
    PS_IN_BODY = 1 -- 在身上（装备上了）
}

-- 操作道具
BagSysProtocol.OPERATE_PROPS = {
    OP_EQUIP = 0, -- 装备
    OP_UNEQUIP = 1 -- 不装备
}

------------------------

BagSysProtocol.CountPropsInfo = {
    propsid = 0,
    propscnt = 0,
    addtime = 0,
    props_state = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end
}

BagSysProtocol.TimePropsInfo = {
    propsid = 0,
    propscnt = 0,
    endtime = 0,
    addtime = 0,
    props_state = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end
}

BagSysProtocol.PlayerPropsInfo = {
    areaid = 0,
    numid = 0,
    cpcnt = 0,
    count_prop_info = {},
    tpcnt = 0,
    time_prop_info = {},
    gold = 0,

    new = function(self, o)
        self.count_prop_info = {}
        self.time_prop_info = {}
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end
}

------------------------

BagSysProtocol.ReqAppidList = {
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

BagSysProtocol.RespAppidList = {
    XY_ID = CMDT_RESPAPPIDLIST,
    askid = 0,
    ec = 0,
    count = 0,
    appid = {},

    new = function(self, o)
        self.appid = {}
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

BagSysProtocol.NotifyServerState = {
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

BagSysProtocol.ReqPlayerConnect = {
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

BagSysProtocol.RespPlayerConnect = {
    XY_ID = CMDT_RESPPLAYERCONNECT,
    askid = 0,
    props_appid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.props_appid = bis:readInt32()
        return self
    end
}

BagSysProtocol.ReqKeepAlive = {
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

BagSysProtocol.RespKeepAlive = {
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

BagSysProtocol.ReqJoinBoxProp = {
    XY_ID = CMDT_REQJOINBOXPROP,

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

BagSysProtocol.RespJoinBoxProp = {
    XY_ID = CMDT_RESPJOINBOXPROP,

    askid = 0,
    ec = 0,
    props_appid = 0,
    props_seqid = 0,
    server_time = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.props_appid = bis:readInt32()
        self.props_seqid = bis:readInt32()
        self.server_time = bis:readUInt64()
        return self
    end
}

---------------------------------

BagSysProtocol.ReqPropsConfig = {
    XY_ID = CMDT_REQPROPSCONFIG,

    askid = 0,
    md5 = "",
    bzip = false,
    props_type = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeString(self.md5)
        bos:writeBool(self.bzip)
	    bos:writeInt32(self.props_type)
        return bos
    end
}

BagSysProtocol.ResPropsConfig = {
    XY_ID = CMDT_RESPPROPSCONFIG,

    askid = 0,
    ec = 0,
    total_package = 0,
    cur_package = 0,
    pack_msg_len = 0,
    pack_msg = "",
    bzip = false,
    props_type = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        self.pack_msg = ""
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.ec = bis:readInt32()
        self.total_package = bis:readInt32()
        self.cur_package = bis:readInt32()
        self.pack_msg_len = bis:readUInt8()
        if bis:getAvailableSize() > 0 then
            self.pack_msg = bis:read(self.pack_msg_len)
        end
        if bis:getAvailableSize() > 0 then
            self.bzip = bis:readBool()
        end
	    self.props_type = bis:readInt32()
        return self
    end
}

BagSysProtocol.ReqBackpackData = {
    XY_ID = CMDT_REQBACKPACKDATA,
    askid = 0,
    pcnt = 0,
    areaid = {},
    numid = {},
    propsid_cnt = 0,
    propsid = {}, -- 这里表示取特定id的道具信息，若不填，表示玩家所有的道具信息都下发

    new = function(self, o)
        self.areaid = {}
        self.numid = {}
        self.propsid = {}
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeUInt8(self.pcnt)
        for i = 1, self.pcnt do
            bos:writeInt32(self.areaid[i])
            bos:writeInt32(self.numid[i])
        end
        bos:writeUInt8(self.propsid_cnt)
        for i = 1, self.propsid_cnt do
            bos:writeInt32(self.propsid[i])
        end
        return bos
    end
}

BagSysProtocol.RespBackpackData = {
    XY_ID = CMDT_RESPBACKPACKDATA,
    askid = 0,
    ppicnt = 0,
    player_props_info = {},

    new = function(self, o)
        self.player_props_info = {}
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.ppicnt = bis:readUInt8()

        for i = 1, self.ppicnt do
            self.player_props_info[i] = BagSysProtocol.PlayerPropsInfo:new()
            self.player_props_info[i].areaid = bis:readInt32()
            self.player_props_info[i].numid = bis:readInt32()

            self.player_props_info[i].cpcnt = bis:readUInt8()
            self.player_props_info[i].count_prop_info = {}
            for j = 1, self.player_props_info[i].cpcnt do
                self.player_props_info[i].count_prop_info[j] = BagSysProtocol.CountPropsInfo:new()
                self.player_props_info[i].count_prop_info[j].propsid = bis:readInt32()
                self.player_props_info[i].count_prop_info[j].propscnt = bis:readInt32()
                self.player_props_info[i].count_prop_info[j].addtime = bis:readUInt64()
                self.player_props_info[i].count_prop_info[j].props_state = bis:readUInt8()
            end

            self.player_props_info[i].tpcnt = bis:readUInt8()
            self.player_props_info[i].time_prop_info = {}
            for j = 1, self.player_props_info[i].tpcnt do
                self.player_props_info[i].time_prop_info[j] = BagSysProtocol.TimePropsInfo:new()
                self.player_props_info[i].time_prop_info[j].propsid = bis:readInt32()
                self.player_props_info[i].time_prop_info[j].propscnt = bis:readInt32()
                self.player_props_info[i].time_prop_info[j].endtime = bis:readUInt64()
                self.player_props_info[i].time_prop_info[j].addtime = bis:readUInt64()
                self.player_props_info[i].time_prop_info[j].props_state = bis:readUInt8()
            end
            self.player_props_info[i].gold = bis:readInt64()
        end
        return self
    end
}

BagSysProtocol.ReqUseProps = {
    XY_ID = CMDT_REQUSEPROPS,

    askid = 0,
    propsid = 0,
    propscnt = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.propsid)
        bos:writeInt32(self.propscnt)
        return bos
    end

}

BagSysProtocol.RespUseProps = {
    XY_ID = CMDT_RESPUSEPROPS,

    askid = 0,
    ec = 0,
    ppi = BagSysProtocol.PlayerPropsInfo:new(),

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.ec = bis:readInt32()

        self.ppi = BagSysProtocol.PlayerPropsInfo:new()
        self.ppi.areaid = bis:readInt32()
        self.ppi.numid = bis:readInt32()

        self.ppi.cpcnt = bis:readUInt8()
        self.ppi.count_prop_info = {}
        for i = 1, self.ppi.cpcnt do
            self.ppi.count_prop_info[i] = BagSysProtocol.CountPropsInfo:new()
            self.ppi.count_prop_info[i].propsid = bis:readInt32()
            self.ppi.count_prop_info[i].propscnt = bis:readInt32()
            self.ppi.count_prop_info[i].addtime = bis:readUInt64()
            self.ppi.count_prop_info[i].props_state = bis:readUInt8()
        end

        self.ppi.tpcnt = bis:readUInt8()
        self.ppi.time_prop_info = {}
        for i = 1, self.ppi.tpcnt do
            self.ppi.time_prop_info[i] = BagSysProtocol.CountPropsInfo:new()
            self.ppi.time_prop_info[i].propsid = bis:readInt32()
            self.ppi.time_prop_info[i].propscnt = bis:readInt32()
            self.ppi.time_prop_info[i].endtime = bis:readUInt64()
            self.ppi.time_prop_info[i].addtime = bis:readUInt64()
            self.ppi.time_prop_info[i].props_state = bis:readUInt8()
        end
        self.ppi.gold = bis:readInt64()
        return self
    end

}

BagSysProtocol.ReqOperateProps = {
    XY_ID = CMDT_REQOPERATEPROPS,

    askid = 0,
    operate_props = 0,
    propsid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeUInt8(self.operate_props)
        bos:writeInt32(self.propsid)
        return bos
    end
}

BagSysProtocol.RespOperateProps = {
    XY_ID = CMDT_RESPOPERATEPROPS,

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

BagSysProtocol.NotifyPropsAddMsg = {
    XY_ID = CMDT_NOTIFYPROPSDATA,

    ppi = BagSysProtocol.PlayerPropsInfo:new(),

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.ppi = BagSysProtocol.PlayerPropsInfo:new()
        self.ppi.areaid = bis:readInt32()
        self.ppi.numid = bis:readInt32()
        self.ppi.cpcnt = bis:readUInt8()
        self.ppi.count_prop_info = {}
        for i = 1, self.ppi.cpcnt do
            self.ppi.count_prop_info[i] = BagSysProtocol.CountPropsInfo:new()
            self.ppi.count_prop_info[i].propsid = bis:readInt32()
            self.ppi.count_prop_info[i].propscnt = bis:readInt32()
            self.ppi.count_prop_info[i].addtime = bis:readUInt64()
            self.ppi.count_prop_info[i].props_state = bis:readUInt8()
        end

        self.ppi.tpcnt = bis:readUInt8()
        self.ppi.time_prop_info = {}
        for i = 1, self.ppi.tpcnt do
            self.ppi.time_prop_info[i] = BagSysProtocol.CountPropsInfo:new()
            self.ppi.time_prop_info[i].propsid = bis:readInt32()
            self.ppi.time_prop_info[i].propscnt = bis:readInt32()
            self.ppi.time_prop_info[i].endtime = bis:readUInt64()
            self.ppi.time_prop_info[i].addtime = bis:readUInt64()
            self.ppi.time_prop_info[i].props_state = bis:readUInt8()
        end
        self.ppi.gold = bis:readInt64()
        return self
    end
}

BagSysProtocol.ReqGiftPackInfo = {
    XY_ID = CMDT_REQGIFTPACKINFO,

    askid = 0,
    count = 0,
    giftids = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.count)
        for i = 1, self.count do
            bos:writeInt32(self.giftids[i])
        end
        return bos
    end
}

BagSysProtocol.GiftInfos = {
    giftpackid = 0, -- 礼包id
    lc = 0, --限制使用次数 
    sc = 0, --剩余使用次数 

    new = function(self, o)
        self.giftpackid = 0
        self.lc = 0
        self.sc = 0
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end
}

BagSysProtocol.RespGiftPackInfo = {
    XY_ID = CMDT_RESPGIFTPACKINFO,

    askid = 0,
    count = 0,
    giftInfos = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.count = bis:readInt32()
        self.giftInfos = {}
        for i = 1, self.count do
            local giftInfo = BagSysProtocol.GiftInfos:new()
            giftInfo.giftpackid = bis:readInt32()
            giftInfo.lc = bis:readInt32()
            giftInfo.sc = bis:readInt32()
            self.giftInfos[i] = giftInfo
        end
        return self
    end
}

for _, v in pairs(BagSysProtocol) do
    v.processid = 92
    if v.XY_ID then
        v.event_key = v.processid .. "_" .. v.XY_ID
    end
end

BagSysProtocol.processid = 92

return BagSysProtocol
   �K  