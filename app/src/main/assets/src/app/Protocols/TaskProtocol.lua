local TaskProtocol = {}
local CMDT_FIRST_CLIENT_TASK = 0
-- local CMDT_FIRST_CLIENT_TASK_RECEIVE = 25200

local CMDT_REQTASKCONFIG = CMDT_FIRST_CLIENT_TASK + 1
local CMDT_RESPTASKCONFIG = CMDT_FIRST_CLIENT_TASK + 2
local CMDT_REQTASKPERCENT = CMDT_FIRST_CLIENT_TASK + 3 -- 请求收集麻将进度
local CMDT_RESPTASKPERCENT = CMDT_FIRST_CLIENT_TASK + 4 -- 返回收集麻将进度
local CMDT_REQGETTASKAWARD = CMDT_FIRST_CLIENT_TASK + 5 -- 请求奖励
local CMDT_RESPGETTASKAWARD = CMDT_FIRST_CLIENT_TASK + 6 -- 返回奖励
-- local CMDT_REQADDTASKDATA          = CMDT_FIRST_CLIENT_TASK + 7           -- 请求增加某个任务数据
-- local CMDT_RESPADDTASKDATA         = CMDT_FIRST_CLIENT_TASK + 8           -- 返回增加某个任务数据(
-- local CMDT_REQCLEARTASKDATA        = CMDT_FIRST_CLIENT_TASK + 9           -- 请求重置麻将子
-- local CMDT_RESPCLEARTASKDATA       = CMDT_FIRST_CLIENT_TASK + 10          -- 返回重置麻将子
local CMDT_REQTASKPROTOCOL = CMDT_FIRST_CLIENT_TASK + 11 -- 请求服务自定义协议
local CMDT_RESPTASKPROTOCOL = CMDT_FIRST_CLIENT_TASK + 12 -- 接受服务自定义数据

local CMDT_REQWEBTASKCONFIG = CMDT_FIRST_CLIENT_TASK + 13 -- web活动请求
local CMDT_RESPWEBTASKCONFIG = CMDT_FIRST_CLIENT_TASK + 14 -- web活动请求返回

-- local CMDT_RESPNOTIFYTASKPERCENT   = CMDT_FIRST_CLIENT_TASK_RECEIVE + 1   -- 接收任务奖励通知

local CMDT_REQWEBTASKLIST = 19
local CMDT_RESPWEBTASKLIST = 20

TaskProtocol.ReqTaskConfig = {
    XY_ID = CMDT_REQTASKCONFIG,
    askid = 0,
    getAll = 0, -- 1全部 ，0指定
    filename = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.askid)
        bos:writeInt32(self.getAll)
        bos:writeString(self.filename)
        return bos
    end
}

TaskProtocol.RespTaskConfig = {
    XY_ID = CMDT_RESPTASKCONFIG,
    FLAG = {
        SUCCESS = 0,
        ERROR = 1,
        FILE_NOT_EXIST = 2, -- 文件不存在
        NO_FILE = 3 -- 没有文件名
    },

    ucFlag = 0,
    nAskId = 0,
    nCurFile = 0,
    nTotalFile = 0,
    acFileName = "",
    nCurPkg = 0,
    nTotalPkg = 0,
    acData = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.ucFlag = bis:readUInt8()
        self.nAskId = bis:readInt32()
        self.nCurFile = bis:readInt32()
        self.nTotalFile = bis:readInt32()
        self.acFileName = bis:readString()
        self.nCurPkg = bis:readInt32()
        self.nTotalPkg = bis:readInt32()
        self.acData = bis:readString()

        return self
    end
}

-- 请求奖励
TaskProtocol.ReqGetTaskAward = {
    XY_ID = CMDT_REQGETTASKAWARD,

    nAskId = 0,
    nTaskId = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.nAskId)
        bos:writeInt32(self.nTaskId)
        return bos
    end
}

-- 返回奖励
TaskProtocol.RespGetTaskAward = {
    XY_ID = CMDT_RESPGETTASKAWARD,
    FLAG = {SUCCESS = 0, ERROR = 1},
    ucFlag = 0,
    nAskId = 0,
    nTaskId = 0,
    acData = "", -- 奖励

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.ucFlag = bis:readUInt8()
        self.nAskId = bis:readInt32()
        self.nTaskId = bis:readInt32()
        self.acData = bis:readString()

        return self
    end
}

-- 请求收集进度麻将
TaskProtocol.ReqTaskPercent = {
    XY_ID = CMDT_REQTASKPERCENT,

    nAskId = 0,
    nTaskId = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.nAskId)
        bos:writeInt32(self.nTaskId)
        return bos
    end
}
-- 返回收集进度
TaskProtocol.RespTaskPercent = {
    XY_ID = CMDT_RESPTASKPERCENT,
    FLAG = {SUCCESS = 0, ERROR = 1},
    STATE = {AWARD_NOT_SEND = 0, AWARD_SEND = 1},
    ucFlag = 0,
    ucState = 0,
    nAskId = 0,
    nTaskId = 0,
    nComplete = 0, -- 完成的
    nTotal = 0, -- 总数
    acData = "", -- 收集到的
    nErrorCode = 0,
    nExtraParam1 = 0,
    nExtraParam2 = 0,
    nExtraParam3 = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.ucFlag = bis:readUInt8()
        self.ucState = bis:readUInt8()
        self.nAskId = bis:readInt32()
        self.nTaskId = bis:readInt32()
        self.nComplete = bis:readInt32()
        self.nTotal = bis:readInt32()
        self.acData = bis:readString()
        self.nErrorCode = bis:readInt32()
        if bis:getAvailableSize() > 0 then
            self.nExtraParam1 = bis:readInt32()
            self.nExtraParam2 = bis:readInt32()
            self.nExtraParam3 = bis:readInt32()
        end

        return self
    end
}

-- 请求自定义协议
TaskProtocol.ReqTaskProtocol = {
    XY_ID = CMDT_REQTASKPROTOCOL,

    nAskId = 0,
    nAreaTypeId = 0,
    nProtoId = 0,
    acData = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.nAskId)
        bos:writeInt32(self.nAreaTypeId)
        bos:writeInt32(self.nProtoId)
        bos:writeString(self.acData)
        return bos
    end
}

TaskProtocol.RespTaskProtocol = {
    XY_ID = CMDT_RESPTASKPROTOCOL,
    nAskId = 0,
    nAreaTypeId = 0,
    nProtoId = 0,
    acData = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nAskId = bis:readInt32()
        self.nAreaTypeId = bis:readInt32()
        self.nProtoId = bis:readInt32()
        self.acData = bis:readString()

        return self
    end
}

TaskProtocol.ReqWebTaskList = {
    XY_ID = CMDT_REQWEBTASKLIST,

    nAskId = 0,
    nWid = 0,
    nChannel = 0,

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()
        bos:writeInt32(self.nAskId)
        bos:writeInt32(self.nWid)
        bos:writeInt32(self.nChannel)
        return bos
    end
}

TaskProtocol.RespWebTaskList = {
    XY_ID = CMDT_RESPWEBTASKLIST,

    FLAG = {ERROR = -1, SUCCESS = 0},

    nFlag = 0,
    nErrorCode = 0,
    nAskId = 0,
    nWid = 0,
    nChannel = 0,
    nCurPkg = 0,
    nTotalPkg = 0,
    acData = "",
    acErrorInfo = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nFlag = bis:readInt32()
        self.nErrorCode = bis:readInt32()
        self.nAskId = bis:readInt32()
        self.nWid = bis:readInt32()
        self.nChannel = bis:readInt32()
        self.nCurPkg = bis:readInt32()
        self.nTotalPkg = bis:readInt32()
        self.acData = bis:readString()
        self.acErrorInfo = bis:readString()
        return self
    end
}

-- 请求web活动配置
TaskProtocol.ReqWebTaskConfig = {
    XY_ID = CMDT_REQWEBTASKCONFIG,
    nAskId = 0,
    nWebTaskId = 0, -- web活动id
    nStageId = 0, -- 期数id
    nPoolId = 0, -- 奖池id

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bostream = function(self)
        local bos = un.network.IStream.new()

        bos:writeInt32(self.nAskId)
        bos:writeInt32(self.nWebTaskId)
        bos:writeInt32(self.nStageId)
        bos:writeInt32(self.nPoolId)
        return bos
    end
}

-- 请求web活动配置返回
TaskProtocol.RespWebTaskConfig = {
    XY_ID = CMDT_RESPWEBTASKCONFIG,
    FLAG = {ERROR = -1, SUCCESS = 0},
    nFlag = 0,
    nErrorCode = 0,
    nAskId = 0,
    nWebTaskId = 0,
    nStageId = 0,
    nPoolId = 0,
    nCurPkg = 0, -- 当前时第几个包（从1开始）
    nTotalPkg = 0, -- 总共几个包
    acData = "",
    acErrorInfo = "",

    new = function(self, o)
        o = o or {}
        setmetatable(o, {__index = self})
        return o
    end,

    bistream = function(self, buff, bufflen)
        local bis = un.network.OStream.new(buff)

        self.nFlag = bis:readInt32()
        self.nErrorCode = bis:readInt32()
        self.nAskId = bis:readInt32()
        self.nWebTaskId = bis:readInt32()
        self.nStageId = bis:readInt32()
        self.nPoolId = bis:readInt32()
        self.nCurPkg = bis:readInt32()
        self.nTotalPkg = bis:readInt32()
        self.acData = bis:readString()
        self.acErrorInfo = bis:readString()

        return self
    end
}

for _, v in pairs(TaskProtocol) do
    v.processid = 120
end
TaskProtocol.processid = 120

return TaskProtocol
�&  