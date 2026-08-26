local List = import(".List")
local Util = import(".Util")

local function convertKey(event)
    local fieldKey = {}
    fieldKey["action"] = "act"
    fieldKey["page"] = "pg"
    fieldKey["page_item_id"] = "pi"
    fieldKey["page_label"] = "pl"
    fieldKey["block"] = "blk"
    fieldKey["block_item_id"] = "bi"
    fieldKey["block_label"] = "bl"
    fieldKey["item_id"] = "itemid"
    fieldKey["label"] = "label"
    fieldKey["sort"] = "sort"
    fieldKey["event_id"] = "eid"
    fieldKey["duration"] = "dur"
    fieldKey["extended"] = "ext"
    fieldKey["customize"] = "cust"
    fieldKey["pre_page_id"] = "ppg"
    fieldKey["pre_page_item_id"] = "ppi"
    fieldKey["pre_page_label"] = "ppl"

    local temp = {}
    for i, v in pairs(event) do
        if fieldKey[i] ~= nil then
            temp[fieldKey[i]] = Util.trim(v)
        else
            temp[i] = Util.trim(v)
        end
    end
    return temp
end

local function pushEvents(_self, events)
    local tryCnt = 3
    _self.pushCallback = function(res, httpStatus)
        tryCnt = tryCnt - 1
        if httpStatus == nil or type(httpStatus) ~= "number" or tonumber(httpStatus) ~= 200 then
            if tryCnt >= 0 then
                Util.post(_self.serverList["action"][tryCnt % 2 + 1] .. "/api/action", _self.site, events, _self.pushCallback)
            end
            return
        end

        if nil == res then
            print("Error:pushEvents failed")
            return
        end
        local resultJson = Util.fromJson(res)
        local resultCode = tonumber(resultJson["code"])
        if resultCode ~= 0 then
            print("Error:pushEvents failed:" .. res)
        end
    end

    Util.post(_self.serverList["action"][tryCnt % 2 + 1] .. "/api/action", _self.site, events, _self.pushCallback)
end

local function batchPush(_self, size)
    local events = {}
    for i = 1, size do
        events[i] = List.popFront(_self.eventList)
    end
    pushEvents(_self, events)
end

local function initSuccess(_self, resultJson)
    local resData = resultJson["data"]
    _self.serverTime = resData["time"]
    _self.serverTimeDiff = resData["time"] - Util.now()
    _self.env = resData["env"]
    _self.packSize = resData["pack_size"]
    _self.interval = resData["interval"]
    _self.validDay = resData["valid_day"]
    _self.serverList = resData["server_list"]
    _self.abtest = resData["abtest"]

    _self.ready = true

    local launch = {}
    launch["action"] = "push"
    launch["page"] = "P0010"
    launch["event_id"] = "100"
    _self:track(launch)

    if cc ~= nil then
        if cc.polarisDhSdkScheduleScriptId ~= nil then
            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(cc.polarisDhSdkScheduleScriptId)
            cc.polarisDhSdkScheduleScriptId = nil
        end
        local batchPushTimer = function()
            local size = List.Size(_self.eventList)
            if size > 0 then
                batchPush(_self, size)
            end
        end
        cc.polarisDhSdkScheduleScriptId = cc.Director:getInstance():getScheduler():scheduleScriptFunc(batchPushTimer, _self.interval, false)
    end
end

local function initFailed(_self, data)
    if cc ~= nil then
        if _self.initRetryTimerScheduleScriptId ~= nil then
            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(_self.initRetryTimerScheduleScriptId)
            _self.initRetryTimerScheduleScriptId = nil
        end

        local initRetryTimer = function()
            local getConfigCallback = function(res)
                if nil == res then
                    return
                end
                local resultJson = Util.fromJson(res)
                if resultJson["code"] ~= 0 then
                    return
                end

                cc.Director:getInstance():getScheduler():unscheduleScriptEntry(_self.initRetryTimerScheduleScriptId)
                initSuccess(_self, resultJson)
            end
            Util.get(_self.host .. "/api/init", data, getConfigCallback, 3);

            _self.initRetryTimerCount = _self.initRetryTimerCount + 1
            if _self.initRetryTimerCount > 10 then
                cc.Director:getInstance():getScheduler():unscheduleScriptEntry(_self.initRetryTimerScheduleScriptId)
                error("初始化失败.")
            end
        end
        _self.initRetryTimerScheduleScriptId = cc.Director:getInstance():getScheduler():scheduleScriptFunc(initRetryTimer, 60, false)
    end
end

local function class(base, _ctor)
    local c = {}
    if not _ctor and type(base) == 'function' then
        _ctor = base
        --base = nil
    elseif type(base) == 'table' then
        for i, v in pairs(base) do
            c[i] = v
        end
        c._base = base
    end
    c.__index = c
    local mt = {}
    mt.__call = function(_, ...)
        local obj = {}
        setmetatable(obj, c)
        if _ctor then
            _ctor(obj, ...)
        end
        return obj
    end
    c._ctor = _ctor
    c.is_a = function(self, klass)
        local m = getmetatable(self)
        while m do
            if m == klass then
                return true
            end
            m = m._base
        end
        return false
    end
    setmetatable(c, mt)
    return c
end

local _DhSDK = class(function(self)
    self.ready = false
    self.host = "https://dh-api.hzxuanming.com"
    self.eventList = List.new()
    self.sessionId = Util.randomLetter(32)
    self.serverTimeDiff = 0
    self.initRetryTimerCount = 0
end)

function _DhSDK:init(params)
if true then return end
    self.site = params["site"]
    if self.site == nil or type(self.site) ~= "string" or string.len(self.site) == 0 then
    end

    self.userId = params["userId"]
    if self.userId == nil or type(self.userId) ~= "string" or string.len(self.userId) == 0 then
    end

    local data = {}
    data["st"] = self.site
    data["uid"] = self.userId
    data["did"] = params["device_id"]
    data["os"] = params["os"]
    data["osv"] = params["os_version"]
    data["isp"] = params["isp"]
    data["ac"] = params["app_channel"]

    self.baseField = {}
    self.baseField["st"] = self.site
    self.baseField["an"] = params["app_name"]
    self.baseField["pkg"] = params["app_package"]
    self.baseField["abd"] = params["app_build"]
    self.baseField["av"] = params["app_version"]
    self.baseField["ac"] = params["app_channel"]
    self.baseField["rv"] = params["res_version"]
    self.baseField["uid"] = self.userId
    self.baseField["ind"] = params["is_new_device"]
    self.baseField["os"] = params["os"]
    self.baseField["osv"] = params["os_version"]
    self.baseField["dname"] = params["device_name"]
    self.baseField["isp"] = params["isp"]
    self.baseField["idfv"] = params["idfv"]
    self.baseField["andid"] = params["android_id"]
    self.baseField["did"] = params["device_id"]
    self.baseField["ouid"] = params["open_udid"]
    self.baseField["mfrs"] = params["manufacturer"]
    self.baseField["model"] = params["model"]
    self.baseField["oaid"] = params["oaid"]
    self.baseField["vaid"] = params["vaid"]
    self.baseField["aaid"] = params["aaid"]
    self.baseField["net"] = params["network"]

    local getConfigCallback = function(res)
        if nil == res then
            initFailed(self, data)
            return
        end
        local resultJson = Util.fromJson(res)
        if resultJson["code"] ~= 0 then
            initFailed(self, data)
            return
        end
        initSuccess(self, resultJson)
    end
    Util.get(self.host .. "/api/init", data, getConfigCallback, 3);
end

function _DhSDK:debug(event)
if true then return end
    if self.ready == false then
        return
    end

    event = convertKey(event)
    if event["et"] == nil then
        event["et"] = Util.now() + self.serverTimeDiff
    end
    event["ssid"] = self.sessionId
    local data = { Util.mergeTables(event, self.baseField) }
    pushEvents(self, data)
end

function _DhSDK:track(event)
if true then return end
    event = convertKey(event)
    if event["et"] == nil then
        event["et"] = Util.now() + self.serverTimeDiff
    end
    event["ssid"] = self.sessionId
    List.pushBack(self.eventList, Util.mergeTables(event, self.baseField))

    if self.ready == false then
        return
    end

    if List.Size(self.eventList) >= self.batchNumber then
        batchPush(self, self.batchNumber)
    end
end

function _DhSDK:flush()
    if self.ready == false then
        return
    end

    local size = List.Size(self.eventList)
    if size > 0 then
        batchPush(self, size)
    end
end

_DhSDK.platForm = "Lua"
_DhSDK.version = "1.0.0"
_DhSDK.batchNumber = 100

return _DhSDK#  