local TeaHouseProtocol = {}

local XY_ID_PLUS = 200
local CMDT_REQINVITEPLAYER = XY_ID_PLUS + 98 --邀请玩家
local CMDT_RESPINVITEPLAYER = XY_ID_PLUS + 99

local CMDT_REQOPERATIONHISTORY = XY_ID_PLUS + 177 -- 请求申请或操作记录
local CMDT_RESPOPERATIONHISTORY = XY_ID_PLUS + 178 -- 返回申请或操作记录

local CMDT_REQREGISTERAGENT                       = XY_ID_PLUS + 349;  -- 请求注册用户
local CMDT_RESPREGISTERAGENT                      = XY_ID_PLUS + 350;  -- 返回

local CMDT_REQRECOMENDINVITATION                  = XY_ID_PLUS + 396    -- 获取邀请函
local CMDT_RESPRECOMENDINVITATION                 = XY_ID_PLUS + 397    -- 邀请函（单条）
local CMDT_REQDEALRECOMENDINVITATION              = XY_ID_PLUS + 398    -- 处理邀请函 ，返回值为 上一条

local CMDT_REQCREATETABLEAUTOSIT = XY_ID_PLUS + 81 --创建桌子并自动入座
local CMDT_RESPCREATETABLEAUTOSIT = XY_ID_PLUS + 82 --返回创建桌子并自动入座

local CMDT_RESPJIONTAETABLE = XY_ID_PLUS + 84 --返回入座

TeaHouseProtocol.ReqTeaHouseRight = {
    XY_ID = 51 + XY_ID_PLUS,
    nAskid = 0, --序号
    nAreaid = 0,
    nNumid = 0,
    new = function(self, o)
        o = o or {}
        setmetatable(o, { __index = self })
        return o
    end,
    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nAreaid)
        bos:writeInt32(self.nNumid)
        return bos
    end,
}

TeaHouseProtocol.RespTeaHouseRight = {
    XY_ID = 52 + XY_ID_PLUS,
    RETVAL = {
        OK = 0,
        ERROR = 1,
    },
    nAskid = 0,
    nSize = 0,
    nTeaHouseId = {},
    nResult = 0,
    nErrorCode = 0,
    nLastLeaveTeaID = 0,
    new = function(self, o)
        o = o or {}
        setmetatable(o, { __index = self })
        return o
    end,
    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nSize = bis:readInt32()
        self.nTeaHouseId = {}
        for i = 1, self.nSize do
            self.nTeaHouseId[i] = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nResult = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nErrorCode = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nLastLeaveTeaID = bis:readInt32()
        end
        return self
    end
}

TeaHouseProtocol.ReqSetPayType = {
    XY_ID = 77 + XY_ID_PLUS,

    PayType = {
        TYPE_TEA_OWNER = 0, --领队支付
        TYPE_TEA_PLAYER_AA = 1, --比赛场内玩家支付，扣玩家房卡
        TYPE_TEA_ROOM_OWNER = 6, --比赛场内创建桌子的玩家支付，扣玩家房卡
        TYPE_TEA_PLAYER_OWNER_CARD_AA = 7, --比赛场内玩家AA支付
        TYPE_TEA_PLAYER_OWNER_CARD_ROOMOWNER  = 8, --比赛场内房主支付
        TYPE_AGENT_PLAYER_AA = 20, --游戏结束后，扣用户大厅的房卡(user_props)，aa扣除
        TYPE_AGENT_PLAYER_WINNER = 21, --游戏结束后，扣用户大厅的房卡(user_props)，冠军出
        TEA_PLAYER_OWNER_TEACARD_AA      = 22, -- 比赛场内玩家AA支付，扣user_props表道具，和TEA_PLAYER_AA道具id不同， -- 商城自己购买比赛场卡
        TEA_PLAYER_OWNER_TEACARD_WIN     = 23, --比赛场内玩家冠军支付，扣user_props表道具 --  商城自己购买比赛场卡
        TEA_AA  = 24, --新增支付模式，比赛场领队模式、平摊消耗，扣比赛场房卡
        TYPE_TEA_OWNER_YUFU_MODE_AA = 999, -- 预付模式下模拟的AA支付,服务端不处理这个类型,实际的支付类型是领队付预付模式下，规则中带PrevAA=1的表示aa支付,不带的默认是冠军支付。
    },
    ----------------新增start----------------
    PayModePype =    {
        TYPE_TEA_NULL = 0, -- 兼容线上
        TYPE_PAY_MODEL_FRON = 1, -- 前项收费
        TYPE_PAY_MODEL_BEHID = 2, -- 后项收费
    },
    ----------------新增end----------------
    nAskid = 0,
    nTeaid = 0,
    nPayType = 0, --最好传anPaySet数组中的第一个
    nSize = 0, --支付方式个数
    anPaySet = {}, --支付方式集合
    ----------------新增start----------------
    nPayModeType = 0,
    bCurrencyFirst = false,
    ----------------新增end----------------
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self, buff, bufflen)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nTeaid)
        bos:writeInt32(self.nPayType)
        bos:writeInt32(self.nSize)
        for i = 1, self.nSize do
            bos:writeInt32(self.anPaySet[i])
        end
        ----------------新增start----------------
        bos:writeInt32(self.nPayModeType)
        bos:writeBool(self.bCurrencyFirst)
        ----------------新增end----------------
        return bos
    end
}

TeaHouseProtocol.ReqServerAppid = {
    XY_ID = 106 + XY_ID_PLUS,

    nAskid = 0,
    szUrl = "",
    szName = "",
    nGameLobbyId = 0,
    nAgentAreaId = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeString(self.szUrl)
        bos:writeString(self.szName)
        bos:writeInt32(self.nGameLobbyId)
        bos:writeInt32(self.nAgentAreaId)
        return bos
    end
}

TeaHouseProtocol.SetupTeaHouse = {
    XY_ID = 41 + XY_ID_PLUS,
    nAskid = 0,
    nTeaID = 0,
    szData = "",
    nDefaultProps = 0, --默认新会员可消耗道具数
    nOverPropCnt = 0, --默认新会员可消耗道具数
    nTeaDailyMaxPropCnt = 0,
    bOthersHiden = false,
    ----------------新增start----------------
    nEnableAutoTransferCard = 0, --启用自动转卡功能
    nLowCardNum = 0, --低于多少时触发
    nTransferCardNum = 0, --自己转入多少
    nErrorCode = 0,
    nRemindLowCardNum = -1, --缺卡提醒数量
    ----------------新增end----------------
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nTeaID)
        bos:writeString(self.szData)
        bos:writeInt32(self.nDefaultProps)
        bos:writeInt32(self.nOverPropCnt)
        bos:writeInt32(self.nTeaDailyMaxPropCnt)
        bos:writeBool(self.bOthersHiden)
        ----------------新增start----------------
        bos:writeInt32(self.nEnableAutoTransferCard)
        bos:writeInt32(self.nLowCardNum)
        bos:writeInt32(self.nTransferCardNum)
        bos:writeInt32(self.nErrorCode)
        bos:writeInt32(self.nRemindLowCardNum)
        ----------------新增end----------------
        return bos
    end,
    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nTeaID = bis:readInt32()
        self.szData = bis:readString()
        self.nDefaultProps = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.nOverPropCnt = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nTeaDailyMaxPropCnt = bis:readInt32()
        end

        if bis:getAvailableSize() > 0 then
            self.bOthersHiden = bis:readBool()
        end
        ----------------新增start----------------
        if bis:getAvailableSize() > 0 then
            self.nEnableAutoTransferCard = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nLowCardNum = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nTransferCardNum = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nErrorCode = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nRemindLowCardNum = bis:readInt32()
        end
        ----------------新增end----------------
        return self
    end
}

TeaHouseProtocol.RespServerAppid = {
    XY_ID = 107 + XY_ID_PLUS,

    APPID_RESP_TYPE = {
        SUCCESS = 0,
        APPID_ERROR = -1
    },
    nAskid = 0,
    nAppid = 0,
    ucFlag = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nAppid = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.ucFlag = bis:readUInt8()
        end
        return self
    end
}

TeaHouseProtocol.ReqTeaHouseInfoByPlayerType = {
    XY_ID = 100 + XY_ID_PLUS,
    PLAYER_TYPE = {
        PT_PLAYER = 0,
        PT_TEAHOUSEOWN = 1
    },

    REQ_BIT = {
        TEAHOUSE_TABLECOUNT = 1, -- 是否请求比赛场桌子数
        TEAHOUSE_CAN_QUIT = 2 -- 请求是否可以退出比赛场
    },

    nAskid = 0,
    nPlayerType = 0,
    szUrl = "",
    szName = "",
    nAgentAreaid = 0,
    nGameLobbyid = 0,
    bReqUserRight = false, -- 是否请求比赛场桌子数
    nReqBit = 0, -- 请求是否可以退出比赛场

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nPlayerType)
        bos:writeString(self.szUrl)
        bos:writeString(self.szName)
        bos:writeInt32(self.nAgentAreaid)
        bos:writeInt32(self.nGameLobbyid)
        bos:writeBool(self.bReqUserRight)
        bos:writeInt32(self.nReqBit)
        return bos
    end
}

TeaHouseProtocol.RespTeaHouseInfoByPlayerType = {
    XY_ID = 101 + XY_ID_PLUS,

    nAskid = 0,
    nPlayerType = 0,
    nTotal = 0,
    nCurrent = 0,
    teahouseSummery = {},
    nUserRight = 0,
    nManagerRight = 0,
    nErrorCode = 0,
    bUseBuyedProp = true,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nPlayerType = bis:readInt32()
        self.nTotal = bis:readInt32()
        self.nCurrent = bis:readInt32()
        self.teahouseSummery = {}
        self.teahouseSummery.nTeaHouseAppid = bis:readInt32()
        self.teahouseSummery.nTeaNumber = bis:readInt32()
        self.teahouseSummery.szTeaName = bis:readString()
        self.teahouseSummery.nPasswd = bis:readInt32()

        self.teahouseSummery.nUserCnt = bis:readInt32()
        self.teahouseSummery.szUrl = bis:readString()
        self.teahouseSummery.szData = bis:readString()
        self.teahouseSummery.nOnLineUserCnt = bis:readInt32()
        self.teahouseSummery.ucState = bis:readUInt8()
        self.teahouseSummery.nTodayUseProps = bis:readInt32()
        self.teahouseSummery.nAreaid = bis:readInt32()
        self.teahouseSummery.nTeaOwnerNumid = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.teahouseSummery.acOwnerNick = bis:readString() -- 领队昵称
            self.teahouseSummery.acNotice = bis:readString() -- 比赛场公告
            self.teahouseSummery.nSubAreaId = bis:readInt32() -- 子区
            self.teahouseSummery.nDismissTime = bis:readInt32() -- 申请解散比赛场时间
        end

        self.teahouseSummery.splitTeaName = function(node, name)
            local lp, rp = 0, 0
            if string.sub(name, #name, #name) == ')' then
                rp = #name
                for i = #name - 1, 1, -1 do
                    local c = string.sub(name, i, i)
                    if c == '(' then
                        lp = i
                        break
                    end
                end
            end
            if lp > 0 and rp > 0 and rp > lp then
                local ext = string.sub(name, lp + 1, rp - 1)
                local num = tonumber(ext)
                if num ~= nil then
                    node.szTeaName = string.sub(name, 1, lp - 1)
                    return
                end
            end

            node.szTeaName = name
        end

        self.teahouseSummery.showTeaName = function(node)
            local txt = node.szTeaName
            if node.szData and #node.szData > 0 then
                txt = string.format("%s(%s)", txt, node.szData)
            end
            return txt
        end

        -- 扩展信息
        if bis:getAvailableSize() > 0 then
            self.teahouseSummery.acExtraData = bis:readString()
        end

        if bis:getAvailableSize() > 0 then
            self.nUserRight = bis:readInt32()
        end

        if bis:getAvailableSize() > 0 then
            self.nManagerRight = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nErrorCode = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.bUseBuyedProp = bis:readBool()
        end
        return self
    end
}

TeaHouseProtocol.NotifyCardCount = {
    XY_ID = 391 + XY_ID_PLUS,

    ECARDTYPE = {
        OWNER = 0,
        TEAHOUSE = 1,
    },
    cardtype = 0,
    nowcount = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.cardtype = bis:readInt32()
        self.nowcount = bis:readInt32()
        return self
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.cardtype)
        bos:writeInt32(self.nowcount)
        return bos
    end
}

--玩家在大厅
TeaHouseProtocol.ReqRecomendInvitation = {
    XY_ID = CMDT_REQRECOMENDINVITATION,
    nAskid = 0,
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

TeaHouseProtocol.RespRecomendInvitation =
{
    XY_ID = CMDT_RESPRECOMENDINVITATION,
    UCFLAG = {
        EMPTY = 0,        --无邀请
        JOINFAIL = 1,     --同意但是加入失败了
        ERRDATA = 2,      --处理的邀请信息错误
        LASTONE = 3,      --成功，并且这是最后一条邀请（同意的话其他内容是空的）
        JOINSUCCESS = 4,
        HAVEMOREDATA = 5,
    },
    nAskid = 0,
    ucflag = 0,
    numid = 0,
    teaappid = 0,
    teaid = 0,
    teapwd = 0,
    szUrl = "", --头像的url
    szNickName = "",--昵称
    playmodecount = 0,
    playmodes = {},
    friendcount = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.nAskid = bis:readInt32()
        self.ucFlag = bis:readInt8()
        self.numid = bis:readInt32()
        self.teaappid = bis:readInt32()
        self.teaid = bis:readInt32()
        self.teapwd = bis:readInt32()
        self.szUrl = bis:readString()
        self.szNickName = bis:readString()
        self.playmodecount = bis:readInt32()
        local tempPlayModes = {}
        for i = 1 , self.playmodecount do
            tempPlayModes[i] =  bis:readInt32()
        end
        self.playmodes = tempPlayModes
        self.friendcount = bis:readInt32()
        return self
    end
}

TeaHouseProtocol.ReqDealRecomendInvitation = {
    XY_ID = CMDT_REQDEALRECOMENDINVITATION,
    acceptInvite = false,
    nAskid = 0,
    numid = 0, --领队numid
    teaid = 0,
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeBool(self.acceptInvite)
        bos:writeInt32(self.numid)
        bos:writeInt32(self.teaid)
        return bos
    end
}

TeaHouseProtocol.ReqInvitePlayer = {
    XY_ID = CMDT_REQINVITEPLAYER,
    nAskId = 0,
    nTeaId = 0,
    nInviteAreaId = 0,
    nInviteNumId = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        bos:writeInt32(self.nTeaId)
        bos:writeInt32(self.nInviteAreaId)
        bos:writeInt32(self.nInviteNumId)
        return bos
    end,
}

TeaHouseProtocol.RespInvitePlayer = {
    XY_ID = CMDT_RESPINVITEPLAYER,

    FLAG = {
        SUCCESS = 0,
        NO_RIGHT = 1,
        MAX_TEAHOUSE = 2,
        MAX_MEMBERS = 3,
        IS_MEMBER = 4,
    },

    ucFlag = 0,
    nAskId = 0,
    nTeaId = 0,
    nErrorCode = 0,
    nNumid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.ucFlag = bis:readUInt8()
        self.nAskId = bis:readInt32()
        self.nTeaId = bis:readInt32()
        self.nErrorCode = bis:readInt32()
        self.nNumid = bis:readInt32()
        return self
    end,
}


--23 比赛场主申请新比赛场(c->s)
TeaHouseProtocol.ReqOpenTeaHouse = {
    XY_ID = 23 + XY_ID_PLUS,
    askid = 0, --序号
    name = "", --名称
    nDefaultProps = 0, --默认新会员可消耗道具数    
    propid = 0,
    nOverPropCnt = 0, --默认可透支道具数
    notice = "", --公告
    areaID = 0, --区域号
    nLevel = 0,--比赛场等级
    bNoCardCheck = 0, --传1跳过房卡检查

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeString(self.name)
        bos:writeInt32(self.nDefaultProps or 0)
        bos:writeInt32(self.propid)
        bos:writeInt32(self.nOverPropCnt or 0)
        bos:writeString(self.notice or "")
        bos:writeInt32(self.areaID or 0)
        bos:writeInt32(self.nLevel)
        bos:writeInt32(self.bNoCardCheck or 0)
        return bos
    end,
}

--返回新比赛场(s->c)
TeaHouseProtocol.RespOpenTeaHouse = {
    XY_ID = 24 + XY_ID_PLUS,
    FLAG = {
        SUCCESS = 0, --成功
        NOTRIGHT = 1, --不权限
        MAXHOUSE = 2, --已经达到创建上限
        OTHER = 3, --其他错误
        NOTPROP = 4, --没有足够的卡
        SENSITIVE = 5, --被MGC过滤
        BAN = 6, --被ban比赛场功能
    },
    askid = 0, --序号
    flag = 0, --返回标示 
    teaNumber = 0, --比赛场编号
    szTeaName = "",
    nErrorCode = 0,
    nPasswd = 0,
    nUnLockActiveValue = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.flag = bis:readUInt8()
        self.teaNumber = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.szTeaName = bis:readString()
        end
        if bis:getAvailableSize() > 0 then
            self.nErrorCode = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nPasswd = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nUnLockActiveValue = bis:readInt32()
        end
        return self
    end
}

TeaHouseProtocol.ReqRegisterAgent = 
{
    XY_ID = CMDT_REQREGISTERAGENT ,
    nAskId = 0,
    nAgentAreaId = 0,
    acPhoneNum = "",
    acHeadUrl = "",
    acNickName = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function (self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        bos:writeInt32(self.nAgentAreaId)
        bos:writeString(self.acPhoneNum)
        bos:writeString(self.acHeadUrl)
        bos:writeString(self.acNickName)
        return bos
    end
}

TeaHouseProtocol.RespRegisterAgent = 
{
    XY_ID = CMDT_RESPREGISTERAGENT ,

    RETVAL = {
            OK = 0,
            PARAM_ERROR = 1,
            NOT_BIND = 2,   -- 未绑定手机号
            IS_AGENT = 3,   -- 已经是用户了
            REGISTER_ERROR = 4, -- 注册失败
            PHONE_ERROR = 5,    -- 手机号不是绑定的手机号
        },
    ucFlag = 0,
    nErrorCode = 0,
    nFengxinErrorCode = 0,
    nAskId = 0,
    nAgentAreaId = 0,
    nAgentNumId = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.ucFlag = bis:readUInt8()
        self.nErrorCode = bis:readInt32()
        self.nFengxinErrorCode = bis:readInt32()
        self.nAskId = bis:readInt32()
        self.nAgentAreaId = bis:readInt32()
        self.nAgentNumId = bis:readInt32()
        return self
    end
}

--25 比赛场主关闭比赛场(c->s)
TeaHouseProtocol.ReqCloseTeaHouse = {
    XY_ID = 25 + XY_ID_PLUS,
    askid = 0, --序号
    teaNumber = 0, --比赛场编号

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.teaNumber)
        return bos
    end,
}

-- 26 返回关闭比赛场(s->c)
TeaHouseProtocol.RespCloseTeaHouse = {
    XY_ID = 26 + XY_ID_PLUS,
    FLAG = {
        SUCCESS = 0, --成功
        NOTRIGHT = 1, --不权限
        EXISTTABLE = 2, --还有包房
        OTHER = 3, --其他错误
    },

    askid = 0, --序号
    flag = 0, --返回标示 

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

--87 设置比赛场信息(名字、公告内容) 
TeaHouseProtocol.ReqSetTeaInfo = {
    XY_ID = 87 + XY_ID_PLUS,
    askid = 0, --序号
    nTeaId = 0,
    acTeaName = "",
    acNotice = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.nTeaId)
        bos:writeString(self.acTeaName)
        bos:writeString(self.acNotice)
        return bos
    end,
}

--88 返回比赛场信息
TeaHouseProtocol.RespSetTeaInfo = {
    XY_ID = 88 + XY_ID_PLUS,
    REQ_STATE = {
        SUCCESS = 0,
        NOT_OWNER = 1,
        TEA_NOT_EXIST = 2,
        TEA_NAME_EXIIST = 999, --弃用
        OTHER = 3,
    },
    flag = 0,
    askid = 0, --序号


    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.flag = bis:readUInt8()
        self.askid = bis:readInt32()
        return self
    end
}


--玩家申请退出比赛场(c->s) 
TeaHouseProtocol.ReqQuitTeaHouse = {
    XY_ID = 139 + XY_ID_PLUS,
    askid = 0, --序号
    teaNumber = 0, -- 比赛场num

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.teaNumber)
        return bos
    end,

}

--返回玩家申请退出比赛场(s->c)
TeaHouseProtocol.RespQuitTeaHouse = {
    XY_ID = 140 + XY_ID_PLUS,
    REQ_STATE = {
        AGREE = 0, --成功
        REFUSE = 1, --拒绝
        WAIT = 2, --请求成功/等待领队处理
        NOT_IN = 3, --不在比赛场中
        OTHER_ERROR = 4, --其他错误
    },

    askid = 0, --序号
    state = 0, --请求状态


    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.state = bis:readInt32()
        return self
    end
}

-- 27 比赛场主踢人(c->s)
TeaHouseProtocol.ReqKickUser = {
    FLAG = {
        KICK_ROOM = 1, --踢出包房
        KICK_TEAHOUSE = 2, --踢出比赛场，同时删除比赛场权限
    },
    APPLY_TYPE = {
        OWNER = 1, --领队申请踢人
        DEALPOWER = 2, --处理玩家申请
    },
    XY_ID = 27 + XY_ID_PLUS,
    askid = 0, --序号
    tableid = 0, --桌号
    areaid = 0, --玩家区号
    numid = 0, --玩家数字帐号
    ucType = 0, --uchar 踢人类型 1 踢出包房 2 踢出比赛场，同时删除比赛场权限
    nApplyType = 0,
    teaid = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.tableid)
        bos:writeInt32(self.areaid)
        bos:writeInt32(self.numid)
        bos:writeUInt8(self.ucType)
        bos:writeInt32(self.nApplyType)
        bos:writeInt32(self.teaid)
        return bos
    end,
}

--返回踢人(s->c)
TeaHouseProtocol.RespKickUser = {
    XY_ID = 28 + XY_ID_PLUS,
    FLAG = {
        SUCCESS = 0, --成功
        NOTRIGHT = 1, --不权限
        NOTTABLE = 2, --包房不存在
        NOTUSER = 3, --玩家不存在
        GAMEING = 4, --游戏中           
        OTHER = 5, --其他错误
        MANAGER_K_MANAGER = 6, --副领队踢副领队
        NOT_FENGXIN_RIGHT = 7,
    },

    askid = 0, --序号
    flag = 0, --返回标示

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

TeaHouseProtocol.UserInfo = {
    XY_ID = 31 + XY_ID_PLUS,
    US_STATE = {
        US_OFFLINE = 0,
        US_ONLINE = 1,
        US_GAME = 2,
    },

    TP_EVENT = {
        TP_EVENT_NONE = 0, --无
        TP_EVENT_QUIT = 1, --请求退出比赛场会员
    },

    areaid = 0, --玩家区号
    numid = 0, --玩家数字帐号
    tableid = 0, --所在桌号
    seat = 0, --座位号
    url = "", --微信头像的url  
    nickName = "", --玩家昵称
    sex = 0, --性别(取值见SEX定义)
    nPropCnt = 0, --玩家所在比赛场可消耗道具数量
    bHaveRight = 0, --是否有该比赛场权限
    nDailyCost = 0, --每日消耗（小房卡）
    nHistoryCost = 0, --历史消耗（小房卡）
    nJoinTime = 0, --加入时间
    nAskid = 0,
    teaNumber = 0,
    nUserShowProps = 0, --玩家显示的卡
    nAgentShowProps = 0, --用户显示的卡
    bOffLine = false, --是否离线
    ucState = "", --玩家状态 US_STATE
    nChangeTime = 0, --游戏时间
    nLastFightTime = 0, --最终游戏时间
    nIdx = 0, --索引（从0开始）
    nTotal = 0, --总共索引数
    nUserEvent = 0, --用户申请事件
    nUserRight = 0,
    acAksPowerData = "", --申请加入信息
    nAskTime = 0, --申请加入时间
    nLastLeaveTime = 0, -- 最后离开比赛场时间
    bCanPlay = true,
    nPlayerOwnerCards = 0, --台州转型人人，比赛场流通模式使用该字段，划卡处统计流通卡数量
    acUserExtraInfo = "", --用户一些额外数据，格式见ReqUserInfo
    
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.areaid = bis:readInt32()
        self.numid = bis:readInt32()
        self.tableid = bis:readInt32()
        self.seat = bis:readInt16()
        self.url = bis:readString()
        self.nickName = bis:readString()
        self.sex = bis:readUInt8()
        if bis:getAvailableSize() > 0 then
            self.nPropCnt = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.bHaveRight = bis:readBool()
            self.nDailyCost = bis:readInt32()
            self.nHistoryCost = bis:readInt32()
            self.nJoinTime = bis:readInt32()
            self.nAskid = bis:readInt32()
            self.teaNumber = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nUserShowProps = bis:readInt32()
            self.nAgentShowProps = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.bOffLine = bis:readBool()
        end
        if bis:getAvailableSize() > 0 then
            self.ucState = bis:readUInt8()
        end
        if bis:getAvailableSize() > 0 then
            self.nChangeTime = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nLastFightTime = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nIdx = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nTotal = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nUserEvent = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nUserRight = bis:readInt32() -- = = 1
        end
        if bis:getAvailableSize() > 0 then
            self.acAksPowerData = bis:readString()
        end
        if bis:getAvailableSize() > 0 then
            self.nAskTime = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.nLastLeaveTime = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.bCanPlay = bis:readBool()
        end
        if bis:getAvailableSize() > 0 then
            self.nPlayerOwnerCards = bis:readInt32()
        end
        if bis:getAvailableSize() > 0 then
            self.acUserExtraInfo = bis:readString()
        end
        return self
    end
}

TeaHouseProtocol.RespServerAppid = {
    XY_ID = 107 + XY_ID_PLUS,

    APPID_RESP_TYPE = {
        SUCCESS = 0,
        APPID_ERROR = -1
    },
    nAskid = 0,
    nAppid = 0,
    ucFlag = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nAppid = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.ucFlag = bis:readUInt8()
        end
        return self
    end
}

TeaHouseProtocol.ReqUserInfoListCnt = {
    XY_ID = 137 + XY_ID_PLUS,

    nAskid = 0,
    nTeaNumber = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskid)
        bos:writeInt32(self.nTeaNumber)
        return bos
    end
}

TeaHouseProtocol.RespUserInfoListCnt = {
    XY_ID = 138 + XY_ID_PLUS,

    nAskid = 0,
    nCnt = 0,
    nTotal = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskid = bis:readInt32()
        self.nCnt = bis:readInt32()
        self.nTotal = bis:readInt32()

        return self
    end
}

TeaHouseProtocol.TeaOperRecord = {
    PLAYER_OP = {
        NONE = -1,
        APPLY = 0, --玩家申请权限
        AGREE = 1, --领队同意玩家申请
        REFUSE = 2, --领队拒绝玩家申请
        KICK = 3, --领队踢人
        QUIT = 4, --玩家申请退出
        INVITE = 5, --领队邀请玩家
        AGREEQUIT = 6, --领队同意玩家退出
        REFUSEQUIT = 7, --领队拒绝玩家退出
        FORCEQUIT = 8, --玩家强制退出
    },

    nTeaId = 0,
    nPlayerAreaId = 0,
    nPlayerNumId = 0,
    acNickName = "",
    acUrl = "",
    cOperType = 0, --操作类型
    nTime = 0,
    nTeaPwd = 10,
    nOperAreaId = 0, -- 操作者areaid
    nOperNumId = 0, -- 操作者numid
    acOperNickName = "", -- 操作者昵称
    acOperUrl = "", -- 操作者头像
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,
}

TeaHouseProtocol.ReqOperationHistory = {
    XY_ID = CMDT_REQOPERATIONHISTORY,

    TYPE = {
        PLAYER = 1,
        TEA = 2,
        TEA_CARD = 3,
    },

    nAskId = 0,
    nTeaId = 0,
    nAskType = 0, --请求类型，1玩家记录，2比赛场记录
    nStartIndex = 0, --从第几条开始请求。0表示第一条
    nCount = 0, --请求多少条，最多100
    nReqHisBit = 0, --请求哪些操作的历史记录，默认0表示所有，不支持分页请求

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        bos:writeInt32(self.nTeaId)
        bos:writeInt32(self.nAskType)
        bos:writeInt32(self.nStartIndex)
        bos:writeInt32(self.nCount)
        bos:writeInt32(self.nReqHisBit)
        return bos
    end,
}

TeaHouseProtocol.RespOperationHistory = {
    XY_ID = CMDT_RESPOPERATIONHISTORY,

    RETVAL = {
        OK = 0, -- 找到
        PARAM_ERR = 1, -- 参数错误
        NO_DATA = 2, -- 没有数据
        NO_RIGHT = 3, -- 没有权限
    },

    ucFlag = 0,
    nAskId = 0,
    nTeaId = 0,
    nTotal = 0,
    nCurIndex = 0,
    stHis = {},
    nErrorCode = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.ucFlag = bis:readUInt8()
        self.nAskId = bis:readInt32()
        self.nTeaId = bis:readInt32()
        self.nTotal = bis:readInt32()
        self.nCurIndex = bis:readInt32()
        local tempTeaOperRecord = TeaHouseProtocol.TeaOperRecord:new()
        tempTeaOperRecord.nTeaId = bis:readInt32()
        tempTeaOperRecord.nPlayerAreaId = bis:readInt32()
        tempTeaOperRecord.nPlayerNumId = bis:readInt32()
        tempTeaOperRecord.acNickName = bis:readString()
        tempTeaOperRecord.acUrl = bis:readString()
        tempTeaOperRecord.cOperType = bis:readUInt8()
        tempTeaOperRecord.nTime = bis:readInt32()
        tempTeaOperRecord.nTeaPwd = bis:readInt32()
        tempTeaOperRecord.nOperAreaId = bis:readInt32()
        tempTeaOperRecord.nOperNumId = bis:readInt32()
        tempTeaOperRecord.acOperNickName = bis:readString()
        tempTeaOperRecord.acOperUrl = bis:readString()
        self.stHis = tempTeaOperRecord
        if bis:getAvailableSize() > 0 then
            self.nErrorCode = bis:readInt32() 
        end
        return self
    end
}

TeaHouseProtocol.ReqCreateTableAutoSit = {
    XY_ID = CMDT_REQCREATETABLEAUTOSIT,
    CREATETYPE = {
        TY_PROXY = 0, --用户
        TY_TEA_PlAYER = 1, --比赛场中玩家
    },
    HARDWAREFLAG = {
        HF_NONE = 0,
        HF_CAMERA = 1, --摄像头
        HF_MICROPHONE = 2, --麦克风
        HF_JOYPAD = 4 --游戏手柄，不区分有线无线
    },
    CLIENTTYPE = {
        PC = 0, --计算机
        TV = 1, --电视
        MOBILE = 2, --手机
        WEB = 3, --浏览器
    },
    GAMETYPE = {
        CT_NORMAL = 0, --普通
        CT_SPORTS = 1, --竞技
    },
    SITTYPE = {
        ST_RAND = 0, --0为随机
        ST_SET = 1, --1为指定
    },
    nAskId = 0, --序号
    nGameId = 0, --游戏编号
    nBase = 0, --底数
    nCount = 0, --局数
    nType = 0, --类型(包房模式 0 定局包房 1 定分包房 2 定时包房)
    sSize = 0, --椅子数
    acName = "", --包房名称
    acRuleData = "", --包房游戏规则
    nPropID = 0, --房卡ID
    ucCreateType = 0, --创建者类型

    --JoinRoom相关数据
    nRoomId = 0,
    nClientType = 0, --客户端类型, 见宏CLIENTTYPE
    nHardWareFlag = 0, --硬件标识
    nRoomVer = 0, --房间版本
    nChannelId = 0, --渠道号
    ucGameType = 0, --标示客户端游戏方式
    nOsVer = 0, --client OS版本号
    acIdentify = "",
    llIp = 0, --客户端ip
    bReConnect = false, --是否重连标志, 1为重连,0为正常
    nClientTypeCustom = 0, --客户端自定义类型
    acHeadUrl = "", --玩家头像url
    nTableId = 0, --桌号
    nChairId = 0, --座位号
    ucSitType = 0, --是否按指定座位标志,1为指定,0为随机
    acLogicData = "",
    nPayType = 0, -- 默认0，领队支付
    nTableIndex = 0, -- 对应比赛场的序列号
    nModelIndex = 0, --代表自定义  123 对应的固定玩法的序号
    bNotAutoSit = false, --true不入座，false入座
    nLastTableid = 0, --续桌上次桌子的id号
    nMatchPlayModeId = 0,
    nVirtualGameRuleID = 0, -- 虚拟游戏规则ID
    bSeer = false,            -- true表示旁观， 默认false表示非旁观
    nTeaid = 0,
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        bos:writeInt32(self.nGameId)
        bos:writeInt32(self.nBase)
        bos:writeInt32(self.nCount)
        bos:writeInt32(self.nType)
        bos:writeInt32(self.sSize)
        bos:writeString(self.acName)
        bos:writeString(self.acRuleData)
        bos:writeInt32(self.nPropID)
        bos:writeUInt8(self.ucCreateType)
        bos:writeInt32(self.nRoomId)
        bos:writeInt32(self.nClientType)
        bos:writeInt32(self.nHardWareFlag)
        bos:writeInt32(self.nRoomVer)
        bos:writeInt32(self.nChannelId)
        bos:writeUInt8(self.ucGameType)
        bos:writeInt32(self.nOsVer)
        bos:writeString(self.acIdentify)
        bos:writeInt64(self.llIp)
        bos:writeBool(self.bReConnect)
        bos:writeInt32(self.nClientTypeCustom)
        bos:writeString(self.acHeadUrl)
        bos:writeInt32(self.nTableId)
        bos:writeInt32(self.nChairId)
        bos:writeUInt8(self.ucSitType)
        bos:writeString(self.acLogicData)
        bos:writeInt32(self.nPayType)
        bos:writeInt32(self.nTableIndex)
        bos:writeInt32(self.nModelIndex)
        bos:writeBool(self.bNotAutoSit)
        bos:writeInt32(self.nLastTableid)
        bos:writeInt32(self.nMatchPlayModeId)
        bos:writeInt32(self.nVirtualGameRuleID)
        bos:writeBool(self.bSeer)
        bos:writeInt32(self.nTeaid)
        return bos
    end,
}

TeaHouseProtocol.RespCreateTableAutoSit = {
    XY_ID = CMDT_RESPCREATETABLEAUTOSIT,

    FLAG = {
        --比赛场返回
        SUCCESS = 0, --成功
        NOT_RIGHT = 1, --没有权限
        MAXTABLE = 2, --已经达到创建上限
        NOT_APPID = 3, --没有app
        NOT_SETUP = 4, --没有对应局数配置
        OTHER = 5, --其他错误
        NOT_PROP = 6,
        NOT_TABLE = 7,
        NOT_ENOUGHPROP = 8, --沒有足夠分配的足夠道具
        MAX_CREAT_TABLE = 9, --玩家已达创建最大数量的桌子
        BAN = 10, --玩家被禁游戏
        TEAHOUSE_CLOESD = 11, --比赛场已暂时关闭
        TEAHOUSE_WITHOUT_PLAYMODE = 12, --比赛场不使用固定玩法
        TEAHOUSE_MAX_DAILY_COST = 13, --比赛场达每日最大消耗
        TEAHOUSE_FORBIDDEN = 16, --被拉黑
        TEAHOUSE_HAVE_THE_SAME_DESK = 20, --被拉黑
        --GP返回
        NOT_EMPTY_TABLE = 31, --没有空卓
        NOT_PROP_BY_GP = 32, --没有足够道具
        EXP_PROP = 33, --时效道具已失效
        NOENOUGH_PROP = 34, --没有足够的普通道具
        ERR_DATA = 35, --数据不正确
        NO_TYPEID = 36, --没有扣除道具类型id
        ERR_INTABLE = 37, --玩家已经在桌子上
        ERR_USEPROP = 38, --使用道具失败
        ERR_TABLEID = 39, --错误的房间号
        ERR_EMPTY_SEAT = 40, --无法获取空位置
        ERR_NOT_CREATE_TABLE = 41, --未创建桌子
        ERR_ALREADY_GAME = 42, --已经开始游戏了
        ERR_ALREADY_CREATE = 43, --已经在创建桌子了
        ERR_ALREADY_JOIN = 44, --已经在加入桌子了
        ERR_TABLE_FULL = 45, --桌子已经满了
        FAILED = 46, --其他错误
        ERR_GPS_OPEN = 47, --未开启GPS
        ERR_DISTANCE = 48, --与桌子上其余玩家距离太近，不符合入座条件
        ERR_HAS_ROOM = 239, --与桌子上其余玩家距离太近，不符合入座条件
    },

    nAskId = 0, --序号
    ucFlag = 0, --返回标示
    nTableId = 0, --桌号
    acPasswd = "", --进入包房密码
    nUsePropCnt = 0, --消耗房卡数
    nGameAppId = 0,
    nRoomId = 0,
    nGameId = 0,
    ucSeat = 0,
    bIsNewProcedure = true, --判断新老流程
    nTeaPwd = 0,
    nTeaAppId = 0,
    nErrorCode = 0,
    lastsize = 0,
    lastnumids = {},
    lasturls = {},
    lastnicks = {},
    szData = "",
    nUnLockActiveValue = 0,
    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskId = bis:readInt32()
        self.ucFlag = bis:readUInt8()
        self.nTableId = bis:readInt32()
        self.acPasswd = bis:readString()
        self.nUsePropCnt = bis:readInt32()
        self.nGameAppId = bis:readInt32()
        self.nRoomId = bis:readInt32()
        self.nGameId = bis:readInt32()
        self.ucSeat = bis:readUInt8()
        if bis:getAvailableSize() > 0 then
            self.bIsNewProcedure = bis:readBool()
        end
        if bis:getAvailableSize() > 0 then
            self.nTeaPwd = bis:readInt32()
            self.nTeaAppId = bis:readInt32()
            self.nErrorCode = bis:readInt32()
            self.lastsize = bis:readInt32()
            for i = 1, self.lastsize do
                self.lastnumids[i] = bis:readInt32()
                self.lasturls[i] = bis:readString()
                self.lastnicks[i] = bis:readString()
            end
            self.szData = bis:readString()
        end
        if bis:getAvailableSize() > 0 then
            self.nUnLockActiveValue = bis:readInt32()
        end
        return self
    end
}

TeaHouseProtocol.RespJoinTeaTable = {
    XY_ID = CMDT_RESPJIONTAETABLE,

    FLAG = {
        SUCCESS = 0, --成功
        NOT_RIGHT = 1, --没有权限
        NOT_EMPTY = 2, --没有空位
        OTHER = 3, --其他的错误
        NOT_ENOUGHPROP = 4, --沒有足夠分配的足夠道具
        TEAHOUSE_CLOESD = 5, --比赛场已暂时关闭
        TEAHOUSE_IP_LIMIT = 8, --同ip限制
        TEAHOUSE_FORBIDDEN = 9, --黑名单
        NOT_PROP = 10, -- 道具不足
        GPS_NOT_OPEN = 12, -- 没有开启GPS定位 
        DISTANCE_TOO_CLOSE = 13, --与桌子上其余玩家距离太近，不符合入座条件
        FORBID_PLAY_IN_SAME_TABLE = 14,  -- 限制玩家同桌功能，桌上有玩家被限制同桌
        BAN = 17,--玩家被禁游戏
        NOT_EMPTY_TABLE = 31, --没有空卓
        NOT_PROP_BY_GP = 32, --没有时效道具
        EXP_PROP = 33, --时效道具已失效
        NOENOUGH_PROP = 34, --没有足够的普通道具
        ERR_DATA = 35, --数据不正确
        NO_TYPEID = 36, --没有扣除道具类型id
        ERR_INTABLE = 37, --玩家已经在桌子上
        ERR_USEPROP = 38, --使用道具失败
        ERR_TABLEID = 39, --错误的房间号
        ERR_EMPTY_SEAT = 40, --无法获取空位置
        ERR_NOT_CREATE_TABLE = 41, --未创建桌子
        ERR_ALREADY_GAME = 42, --已经开始游戏了
        ERR_ALREADY_CREATE = 43, --已经在创建桌子了
        ERR_ALREADY_JOIN = 44, --已经在加入桌子了
        ERR_TABLE_FULL = 45, --桌子已经满了
        FAILED = 46, --其他错误
        ERR_GPS_OPEN = 47, --未开启GPS
        ERR_DISTANCE = 48, --与桌子上其余玩家距离太近，不符合入座条件
    },

    nAskId = 0, --序号
    ucFlag = 0, --返回标示
    nTableId = 0, --桌号
    nChairId = 0,
    nGameId = 0,
    nAppId = 0,
    nRoomId = 0, --客户端展示的房间号
    bIsNewProcedure = true, --判断新老流程

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskId = bis:readInt32()
        self.ucFlag = bis:readUInt8()
        self.nTableId = bis:readInt32()
        self.nChairId = bis:readInt32()
        self.nGameId = bis:readInt32()
        self.nAppId = bis:readInt32()
        self.nRoomId = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.bIsNewProcedure = bis:readBool()
        end

        return self
    end
}


TeaHouseProtocol.processid = 116
for _, v in pairs(TeaHouseProtocol) do
    if type(v) == "table" then
        v.processid = TeaHouseProtocol.processid
        if v.XY_ID then
            v.event_key = v.processid .. "_" .. v.XY_ID
        end
    end
end

return TeaHouseProtocol
��  