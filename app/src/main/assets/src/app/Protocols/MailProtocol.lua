-- 邮件协议模块
local MailProtocol = {}

local CMDT_REQCHECKNEWMAIL = 1; -- 玩家进入大厅请求检查未读邮件
local CMDT_RESPCHECKNEWMAIL = 2;
local CMDT_REQGETPLAYERMAILLIST = 3; -- 拉取玩家邮件列表
local CMDT_RESPGETPLAYERMAILLIST = 4;
local CMDT_REQGETMAILDETAIL = 5; -- 邮件详情
local CMDT_RESPGETMAILDETAIL = 6;
local CMDT_REQREADALLMAIL = 7; -- 一键已读
local CMDT_RESPREADALLMAIL = 8;
local CMDT_REQDELMAIL = 9; -- 删除邮件
local CMDT_RESPDELMAIL = 10;
local CMDT_REQGETAWARD = 11; -- 附件领取
local CMDT_RESPGETAWARD = 12;

MailProtocol.ReqCheckNewMail = {
    XY_ID = CMDT_REQCHECKNEWMAIL,

    askid = 0,
    channelId = 0, -- 渠道号
    mailid = 0, -- 本地最大邮件id，默认0

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.channelId)
        bos:writeInt32(self.mailid)
        return bos
    end
}

MailProtocol.RespCheckNewMail = {
    XY_ID = CMDT_RESPCHECKNEWMAIL,

    askid = 0,
    unread = 0, -- 未读邮件数 
    count = 0, -- 新达到邮件数量
    award = 0, -- 待领取奖励邮件数

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.unread = bis:readInt32()
        self.count = bis:readInt32()
        self.award = bis:readInt32()
        return self
    end
}

MailProtocol.ReqGetPlayerMailList = {
    XY_ID = CMDT_REQGETPLAYERMAILLIST,
    askid = 0,
    -- begin_index = 0;            -- 启始索引
    max_count = 0, -- 本次最多拉取数量
    new_count = 0, -- 新邮件数量
    isFirst = false, -- 是否登录第一次拉取

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        -- bos:writeInt32( self.begin_index )
        bos:writeInt32(self.max_count)
        bos:writeInt32(self.new_count)
        bos:writeBool(self.isFirst)
        return bos
    end
}

MailProtocol.MailInfo = {
    mail_id = 0, -- 邮件id
    expire_time = 0, -- 过期时间时间戳
    title = "", -- 标题
    intro = "", -- 邮件简介
    isRead = false, -- true: 以读；false: 未读
    isAnnex = false, -- 是否有附件奖励
    send_time = 0,
    bannex = false,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end
}

MailProtocol.RespGetPlayerMailList = {
    XY_ID = CMDT_RESPGETPLAYERMAILLIST,
    askid = 0,
    count = 0, -- 邮件封数
    mail_list = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.askid = bis:readInt32()
        self.count = bis:readInt32()
        self.mail_list = {}
        for i = 1, self.count do
            local mail_info = MailProtocol.MailInfo:new()
            mail_info.mail_id = bis:readInt32()
            mail_info.expire_time = bis:readInt32()
            mail_info.title = bis:readString()
            mail_info.intro = bis:readString()
            mail_info.isRead = bis:readBool()
            mail_info.isAnnex = bis:readBool()
            mail_info.send_time = bis:readInt32()
            mail_info.bannex = bis:readBool()
            self.mail_list[i] = mail_info
        end
        return self
    end
}

MailProtocol.ReqGetMailDetail = {
    XY_ID = CMDT_REQGETMAILDETAIL,
    askid = 0,
    mailid = 0, -- 邮件id

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.mailid)
        return bos
    end
}

MailProtocol.AwardInfo = {
    url = "", -- 奖品图片
    desc = "", -- 奖品数量

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end
}

MailProtocol.RespGetMailDetail = {
    XY_ID = CMDT_RESPGETMAILDETAIL,
    askid = 0,
    mail_id = 0, -- 邮件id
    sender = "", -- 发送者
    send_time = "", -- 发送时间
    mail_content = "", -- 邮件内容
    award_count = 0, -- 附件数量
    award_list = {}, -- 附件详情

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.mail_id = bis:readInt32()
        self.sender = bis:readString()
        self.send_time = bis:readString()
        self.mail_content = bis:readString()
        self.award_count = bis:readInt32()
        self.award_list = {}
        for i = 1, self.award_count do
            local award_info = MailProtocol.AwardInfo:new()
            award_info.url = bis:readString()
            award_info.desc = bis:readString()
            self.award_list[i] = award_info
        end
        return self
    end
}

MailProtocol.ReqReadAllMail = {
    XY_ID = CMDT_REQREADALLMAIL,
    askid = 0,
    count = 0,
    mail_list = {},

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
            bos:writeInt32(self.mail_list[i])
        end
        return bos
    end
}

MailProtocol.RespReadAllMail = {
    XY_ID = CMDT_RESPREADALLMAIL,
    askid = 0,
    count = 0,
    mail_list = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.count = bis:readInt32()
        self.mail_list = {}
        for i = 1, self.count do
            self.mail_list[i] = bis:readInt32()
        end
        return self
    end
}

MailProtocol.ReqDelMail = {
    XY_ID = CMDT_REQDELMAIL,
    askid = 0,
    count = 0,
    mail_list = {},

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
            bos:writeInt32(self.mail_list[i])
        end
        return bos
    end
}

MailProtocol.RespDelMail = {
    XY_ID = CMDT_RESPDELMAIL,
    askid = 0,
    flag = 0,
    count = 0,
    mail_list = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.count = bis:readInt32()
        self.mail_list = {}
        for i = 1, self.count do
            self.mail_list[i] = bis:readInt32()
        end
        self.flag = bis:readInt32()
        return self
    end
}

MailProtocol.ReqGetAward = {
    XY_ID = CMDT_REQGETAWARD,
    askid = 0,
    mail_count = 0, -- 待领取附件的邮件数量
    mail_list = {}, -- 需要领取附件的邮件id列表

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.askid)
        bos:writeInt32(self.mail_count)
        for i = 1, self.mail_count do
            bos:writeInt32(self.mail_list[i])
        end
        return bos
    end
}

MailProtocol.RespGetAward = {
    XY_ID = CMDT_RESPGETAWARD,
    askid = 0,
    mail_count = 0,
    mail_list = {},
    award_count = 0,
    award_list = {},

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)
        self.askid = bis:readInt32()
        self.mail_count = bis:readInt32()

        self.mail_list = {}
        for i = 1, self.mail_count do
            self.mail_list[i] = bis:readInt32()
        end
        self.award_count = bis:readInt32()
        self.award_list = {}
        for i = 1, self.award_count do
            local award_info = MailProtocol.AwardInfo:new()
            award_info.url = bis:readString()
            award_info.desc = bis:readString()
            self.award_list[i] = award_info
        end
        return self
    end
}

for _, v in pairs(MailProtocol) do
    v.processid = 141
    if v.XY_ID then
        v.event_key = v.processid .. "_" .. v.XY_ID
    end
end

MailProtocol.processid = 141

return MailProtocol
  �%  