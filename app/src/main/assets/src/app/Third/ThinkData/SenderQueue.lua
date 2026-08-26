local ____exports = {}
local ____utils = require("app.Third.ThinkData.Utils")
local _ = ____utils._
local logger = ____utils.logger
local ____PlatformAPI = require("app.Third.ThinkData.PlatformAPI")
local PlatformAPI = ____PlatformAPI.default
local HttpTask = _.__TS__Class()
HttpTask.name = "HttpTask"
function HttpTask.prototype.____constructor(self, data, serverUrl, tryCount, timeout, callback)
    self.data = data
    self.serverUrl = serverUrl
    self.callback = callback
    local ____isNumber_result_0
    if _:isNumber(tryCount) then
        ____isNumber_result_0 = tryCount
    else
        ____isNumber_result_0 = 1
    end
    self.tryCount = ____isNumber_result_0
    local ____isNumber_result_1
    if _:isNumber(timeout) then
        ____isNumber_result_1 = timeout
    else
        ____isNumber_result_1 = 3
    end
    self.timeout = ____isNumber_result_1
    self.taClassName = "HttpTask"
end
function HttpTask.prototype.run(self)
    local timer
    local headers = _:createExtraHeaders()
    headers["content-type"] = "application/json"
    local request = PlatformAPI:request({
        url = self.serverUrl,
        method = "POST",
        data = self.data,
        header = headers,
        success = function(____, res)
            self:onSuccess(res)
            if timer ~= nil then
                cc.Director:getInstance():getScheduler():unscheduleScriptEntry(timer)
                timer = nil
            end
        end,
        fail = function(____, res)
            self:onFailed(res)
            if timer ~= nil then
                cc.Director:getInstance():getScheduler():unscheduleScriptEntry(timer)
                timer = nil
            end
        end
    })
    timer = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        if (_:isObject(request) or _:isPromise(request)) and _:isFunction(request.abort) then
            request:abort()
        end
        if timer ~= nil then
            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(timer)
            timer = nil
        end
    end, self.timeout, false)
end
function HttpTask.prototype.onSuccess(self, res)
    if res.statusCode == 200 then
        local msg
        repeat
            local ____switch10 = res.data.code
            local ____cond10 = ____switch10 == 0
            if ____cond10 then
                msg = "success"
                break
            end
            ____cond10 = ____cond10 or ____switch10 == -1
            if ____cond10 then
                msg = "invalid data"
                break
            end
            ____cond10 = ____cond10 or ____switch10 == -2
            if ____cond10 then
                msg = "invalid APP ID"
                break
            end
            do
                msg = "Unknown return code"
            end
        until true
        self:callback({ code = res.data.code, msg = msg })
    else
        self:callback({ code = -3, msg = res.statusCode })
    end
end
function HttpTask.prototype.onFailed(self, res)
    local ____self_2, ____tryCount_3 = self, "tryCount"
    local ____self_tryCount_4 = ____self_2[____tryCount_3] - 1
    ____self_2[____tryCount_3] = ____self_tryCount_4
    if ____self_tryCount_4 > 0 then
        self:run()
    else
        self:callback({ code = -3, msg = res.errMsg })
    end
end
local HttpTaskDebug = _.__TS__Class()
HttpTaskDebug.name = "HttpTaskDebug"
function HttpTaskDebug.prototype.____constructor(self, data, serverDebugUrl, tryCount, timeout, dryrun, deviceId, callback)
    self.data = data
    self.serverDebugUrl = serverDebugUrl
    self.callback = callback
    local ____isNumber_result_5
    if _:isNumber(tryCount) then
        ____isNumber_result_5 = tryCount
    else
        ____isNumber_result_5 = 1
    end
    self.tryCount = ____isNumber_result_5
    local ____isNumber_result_6
    if _:isNumber(timeout) then
        ____isNumber_result_6 = timeout
    else
        ____isNumber_result_6 = 3
    end
    self.timeout = ____isNumber_result_6
    self.dryrun = dryrun
    self.deviceId = deviceId
    self.taClassName = "HttpTaskDebug"
end
function HttpTaskDebug.prototype.run(self)
    local timer
    local debugData = (((((("appid=" .. tostring(self.data["#app_id"])) .. "&source=client&dryRun=") .. tostring(self.dryrun)) .. "&deviceId=") .. tostring(self.deviceId)) .. "&data=") .. encodeURIComponent(
    nil,
    json.encode(self.data.data[1])
    )
    local headers = _:createExtraHeaders()
    headers["content-type"] = "application/x-www-form-urlencoded"
    local request = PlatformAPI:request({
        url = self.serverDebugUrl,
        method = "POST",
        data = debugData,
        header = headers,
        success = function(____, res)
            self:onSuccess(res)
            if timer ~= nil then
                cc.Director:getInstance():getScheduler():unscheduleScriptEntry(timer)
                timer = nil
            end
        end,
        fail = function(____, res)
            self:onFailed(res)
            if timer ~= nil then
                cc.Director:getInstance():getScheduler():unscheduleScriptEntry(timer)
                timer = nil
            end
        end
    })
    timer = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        if (_:isObject(request) or _:isPromise(request)) and _:isFunction(request.abort) then
            request:abort()
        end
        if timer ~= nil then
            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(timer)
            timer = nil
        end
    end, self.timeout, false)
end
function HttpTaskDebug.prototype.onSuccess(self, res)
    if res.statusCode == 200 then
        local msg
        if res.data.errorLevel == 0 then
            msg = "Verify data success."
        elseif res.data.errorLevel == 1 then
            local errorProperties = res.data.errorProperties
            local errorStr = ""
            do
                local i = 1
                while i <= #errorProperties do
                    local errorReasons = errorProperties[i].errorReason
                    local propertyName = errorProperties[i].propertyName
                    errorStr = ((((errorStr .. " propertyName:") .. tostring(propertyName)) .. " errorReasons:") .. tostring(errorReasons)) .. "\n"
                    i = i + 1
                end
            end
            msg = (("Debug data error. errorLevel:" .. tostring(res.data.errorLevel)) .. " reason:") .. errorStr
        elseif res.data.errorLevel == 2 or res.data.errorLevel == -1 then
            msg = (("Debug data error. errorLevel:" .. tostring(res.data.errorLevel)) .. " reason:") .. tostring(res.data.errorReasons)
        end
        logger:info(msg)
        self:callback({ code = res.data.errorLevel, msg = msg })
    else
        self:callback({ code = -3, msg = res.statusCode })
    end
end
function HttpTaskDebug.prototype.onFailed(self, res)
    local ____self_7, ____tryCount_8 = self, "tryCount"
    local ____self_tryCount_9 = ____self_7[____tryCount_8] - 1
    ____self_7[____tryCount_8] = ____self_tryCount_9
    if ____self_tryCount_9 > 0 then
        self:run()
    else
        self:callback({ code = -3, msg = res.errMsg })
    end
end
local SenderQueue = _.__TS__Class()
SenderQueue.name = "SenderQueue"
function SenderQueue.prototype.____constructor(self)
    self.items = {}
    self.isRunning = false
    self.showDebug = false
end
function SenderQueue.prototype.enqueue(self, data, serverUrl, config, enqueue)
    if enqueue == nil then
        enqueue = true
    end
    local element
    local that = self
    if config.debugMode == "debug" then
        element = _.__TS__New(
        HttpTaskDebug,
        data,
        serverUrl,
        config.maxRetries,
        config.sendTimeout,
        0,
        config.deviceId,
        function(____, res)
            that.isRunning = false
            if _:isFunction(config.callback) then
                config:callback(res)
            end
            that:_runNext()
            if that.showDebug == false then
                if res.code == 0 or res.code == 1 or res.code == 2 then
                    that.showDebug = true
                    if _:isFunction(PlatformAPI.showDebugToast) then
                        PlatformAPI:showDebugToast("The current mode is Debug")
                    end
                end
            end
        end
        )
    elseif config.debugMode == "debugOnly" then
        element = _.__TS__New(
        HttpTaskDebug,
        data,
        serverUrl,
        config.maxRetries,
        config.sendTimeout,
        1,
        config.deviceId,
        function(____, res)
            that.isRunning = false
            if _:isFunction(config.callback) then
                config:callback(res)
            end
            that:_runNext()
            if that.showDebug == false then
                if res.code == 0 or res.code == 1 or res.code == 2 then
                    that.showDebug = true
                    if _:isFunction(PlatformAPI.showDebugToast) then
                        PlatformAPI:showDebugToast("The current mode is debugOnly")
                    end
                end
            end
        end
        )
    else
        element = _.__TS__New(
        HttpTask,
        json.encode(data),
        serverUrl,
        config.maxRetries,
        config.sendTimeout,
        function(____, res)
            that.isRunning = false
            if _:isFunction(config.callback) then
                config:callback(res)
            end
            that:_runNext()
        end
        )
    end
    if enqueue == true then
        table.insert(self.items, element)
        self:_runNext()
    else
        element:run()
    end
end
function SenderQueue.prototype._dequeue(self)
    self.items = table.remove(self.items, 1)
    return self.items
end
function SenderQueue.prototype._runNext(self)
    if #self.items > 0 and not self.isRunning then
        self.isRunning = true
        if self.items[1].taClassName ~= "HttpTask" then
            self:_dequeue():run()
        else
            local items = {}
            for i = 1, #self.items do
                table.insert(items, table.remove(self.items, 1))
            end
            local httpTask0 = items[1]
            local data = json.decode(httpTask0.data)
            local appId = data["#app_id"]
            local callbackList = {}
            callbackList[#callbackList + 1] = httpTask0.callback
            do
                local i = 1
                while i <= #items do
                    local task = items[i]
                    local taskData = json.decode(task.data)
                    if taskData["#app_id"] == appId and httpTask0.serverUrl == task.serverUrl then
                        for _, value in ipairs(taskData.data) do
                            table.insert(data.data, value)
                        end
                        callbackList[#callbackList + 1] = task.callback
                    else
                        table.insert(self.items, task)
                    end
                    i = i + 1
                end
            end
            local flushTime = os.time()
            data["#flush_time"] = flushTime
            local element
            element = _.__TS__New(
            HttpTask,
            json.encode(data),
            httpTask0.serverUrl,
            httpTask0.tryCount,
            httpTask0.timeout,
            function(self, res)
                for cb, element in pairs(callbackList) do
                    element(res)
                end
            end
            )
            element:run()
        end
    end
end
local senderQueue = _.__TS__New(SenderQueue)
____exports.default = senderQueue
return ____exports   9/  