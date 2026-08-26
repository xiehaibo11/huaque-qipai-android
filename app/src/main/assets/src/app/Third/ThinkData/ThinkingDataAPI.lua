local ____utils = require("app.Third.ThinkData.Utils")
local _ = ____utils._
local logger = ____utils.logger
local slice = ____utils.slice

local function getTimezoneOffset()
    local now = os.time()
    local utcNow = os.time(os.date("!*t", now))
    local localNow = os.time(os.date("*t", now))
    return (localNow - utcNow) / 60
end

local function __TS__CountVarargs(...)
    return select("#", ...)
end

local function __TS__SparseArrayNew(...)
    local sparseArray = { ... }
    sparseArray.sparseLength = __TS__CountVarargs(...)
    return sparseArray
end

local function __TS__SparseArrayPush(sparseArray, ...)
    local args = { ... }
    local argsLen = __TS__CountVarargs(...)
    local listLen = sparseArray.sparseLength
    for i = 1, argsLen do
        sparseArray[listLen + i] = args[i]
    end
    sparseArray.sparseLength = listLen + argsLen
end

local function __TS__SparseArraySpread(sparseArray)
    local _unpack = unpack or table.unpack
    return _unpack(sparseArray, 1, sparseArray.sparseLength)
end

local ____exports = {}
local ____PropertyChecker = require("app.Third.ThinkData.PropertyChecker")
local PropertyChecker = ____PropertyChecker.PropertyChecker
local ____Config = require("app.Third.ThinkData.Config")
local Config = ____Config.Config
local ____SenderQueue = require("app.Third.ThinkData.SenderQueue")
local senderQueue = ____SenderQueue.default
local ____PlatformAPI = require("app.Third.ThinkData.PlatformAPI")
local PlatformAPI = ____PlatformAPI.default
local DEFAULT_CONFIG = {
    name = "thinkingdata",
    is_plugin = false,
    maxRetries = 3,
    sendTimeout = 3,
    enablePersistence = true,
    asyncPersistence = false,
    enableLog = true,
    strict = false,
    debugMode = "none",
    enableCalibrationTime = false
}
--- Get system information asynchronously and initialize preset properties
-- 
-- #lib: SDK type,
-- #lib_version: SDK version
-- #network_type: current network type
-- #manufacture: device manufactory
-- #device_model: device mode, e.g iPhone 8
-- #screen_width: device screen width
-- #screen_height: device screen height
-- #os: device os name
-- #os_version: device os version
-- #mp_platform: current platform name
local systemInformation = {
    properties = {["#lib"] = Config.LIB_NAME, ["#lib_version"] = Config.LIB_VERSION },
    initDeviceId = function(self, deviceId)
        if _:isString(deviceId) then
            self.properties["#device_id"] = deviceId
        end
    end,
    getSystemInfo = function(self, callback)
        local that = self
        PlatformAPI:onNetworkStatusChange(function(self, res)
            that.properties["#network_type"] = res.networkType
        end)
        PlatformAPI:getNetworkType({
            success = function(self, res)
                that.properties["#network_type"] = res.networkType
            end,
            complete = function(self)
                PlatformAPI:getSystemInfo({
                    success = function(self, res)
                        local ____res_system_0
                        if res.system then
                            local system = res['system']:gsub("%s+", " ")
                            local result = {}
                            for token in system:gmatch("%S+") do
                                table.insert(result, token)
                            end
                            ____res_system_0 = result
                        else
                            ____res_system_0 = {}
                        end
                        local osInfo = ____res_system_0
                        local data = {
                            ["#manufacturer"] = res.brand,
                            ["#device_model"] = res.model,
                            ["#screen_width"] = tonumber(res.screenWidth),
                            ["#screen_height"] = tonumber(res.screenHeight),
                            ["#os"] = osInfo[1],
                            ["#os_version"] = osInfo[2],
                            ["#mp_platform"] = res.mp_platform,
                            ["#system_language"] = res.systemLanguage
                        }
                        _.extend(that.properties, data)
                        _:setMpPlatform(res.mp_platform)
                    end,
                    complete = function(self)
                        callback(nil)
                    end
                })
            end
        })
    end
}
--- Data cache management class
-- 
-- Keys :
-- 1. device_id: #device_id
-- 2. distinct_id: #distinct_id
-- 3. account_id: #account_id
-- 4. props: super properties
-- 5. event_timers: #duration
local ThinkingDataPersistence = _.__TS__Class()
ThinkingDataPersistence.name = "ThinkingDataPersistence"
function ThinkingDataPersistence.prototype.____constructor(self, config, callback)
    self.enabled = config.enablePersistence
    if self.enabled then
        if config.isChildInstance then
            self.name = (tostring(config.persistenceName) .. "_") .. tostring(config.name)
            self.nameOld = (tostring(config.persistenceNameOld) .. "_") .. tostring(config.name)
        else
            self.name = config.persistenceName
            self.nameOld = config.persistenceNameOld
        end
        if config.asyncPersistence then
            self._state = {}
            PlatformAPI:getStorage(
            self.name,
            true,
            function(____, data)
                if _:isEmptyObject(data) then
                    PlatformAPI:getStorage(
                    self.nameOld,
                    true,
                    function(____, dataOld)
                        self._state = _.extend2Layers({}, dataOld, self._state)
                        self:_init(config, callback)
                        self:_save()
                    end
                    )
                else
                    self._state = _.extend2Layers({}, data, self._state)
                    self:_init(config, callback)
                    self:_save()
                end
            end
            )
        else
            self._state = PlatformAPI:getStorage(self.name) or ({})
            if _:isEmptyObject(self._state) then
                self._state = PlatformAPI:getStorage(self.nameOld) or ({})
            end
            self:_init(config, callback)
        end
    else
        self._state = {}
        self:_init(config, callback)
    end
end
function ThinkingDataPersistence.prototype._init(self, config, callback)
    if not self:getDistinctId() then
        self:setDistinctId(_:UUID())
    end
    if not config.isChildInstance then
        if not self:getDeviceId() then
            self:_setDeviceId(_:UUID())
        end
        systemInformation:initDeviceId(self:getDeviceId())
    end
    self.initComplete = true
    if type(callback) == "function" then
        callback(nil)
    end
    self:_save()
end
function ThinkingDataPersistence.prototype._save(self)
    if self.enabled and self.initComplete then
        PlatformAPI:setStorage(
        self.name,
        json.encode(self._state)
        )
    end
end
function ThinkingDataPersistence.prototype._set(self, name, value)
    local obj
    if type(name) == "string" then
        obj = {}
        obj[name] = value
    elseif type(name) == "table" then
        obj = name
    end
    _:each(
    obj,
    function(____, value, key)
        self._state[key] = value
    end
    )
    self:_save()
end
function ThinkingDataPersistence.prototype._get(self, name)
    return self._state[name]
end
function ThinkingDataPersistence.prototype.setEventTimer(self, eventName, timestamp)
    local timers = self._state.event_timers or ({})
    timers[eventName] = timestamp
    self:_set("event_timers", timers)
end
function ThinkingDataPersistence.prototype.removeEventTimer(self, eventName)
    local timers = self._state.event_timers or ({})
    local timestamp = timers[eventName]
    if not _:isUndefined(timestamp) then
        self._state.event_timers[eventName] = nil
        self:_save()
    end
    return timestamp
end
function ThinkingDataPersistence.prototype.getDeviceId(self)
    return self._state.device_id
end
function ThinkingDataPersistence.prototype._setDeviceId(self, deviceId)
    if self:getDeviceId() then
        logger:warn("cannot modify the device id.")
        return
    end
    self:_set("device_id", deviceId)
end
function ThinkingDataPersistence.prototype.getDistinctId(self)
    return self._state.distinct_id
end
function ThinkingDataPersistence.prototype.setDistinctId(self, distinctId)
    self:_set("distinct_id", distinctId)
end
function ThinkingDataPersistence.prototype.getAccountId(self)
    return self._state.account_id
end
function ThinkingDataPersistence.prototype.setAccountId(self, accoundId)
    self:_set("account_id", accoundId)
end
function ThinkingDataPersistence.prototype.getSuperProperties(self)
    return self._state.props or ({})
end
function ThinkingDataPersistence.prototype.setSuperProperties(self, superProperties, replace)
    local ____replace_1
    if replace then
        ____replace_1 = superProperties
    else
        ____replace_1 = _.extend(
        self:getSuperProperties(),
        superProperties
        )
    end
    local props = ____replace_1
    self:_set("props", props)
end
local dataStoragePrefix = "tampsdk_"
local tabStoragePrefix = "tab_tampsdk_"
local BatchConsumer = _.__TS__Class()
BatchConsumer.name = "BatchConsumer"
function BatchConsumer.prototype.____constructor(self, config, ta)
    self.config = config
    self.ta = ta
    self.timer = nil
    self.batchConfig = _.extend({ size = 5, interval = 5, storageLimit = 200 }, self.config.batchConfig)
    if self.batchConfig.size < 1 then
        self.batchConfig.size = 1
    end
    if self.batchConfig.size > 30 then
        self.batchConfig.size = 30
    end
    self.tabKey = tabStoragePrefix .. tostring(self.config.appId)
    self.storageLimit = self.batchConfig.storageLimit
    self.isRequest = false
    self.trackList = {}
    self.needFlush = false
end
function BatchConsumer.prototype.batchInterval(self)
    self.timer = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        self:recycle()
        self:send()
        if self.timer ~= nil then
            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self.timer)
        end
        self.timer = nil
        self:batchInterval()
    end, self.batchConfig.interval, false)
end
function BatchConsumer.prototype.add(self, data)
    if self.isRequest then
        table.insert(self.trackList, data)
        return
    end
    local d = data
    local dataKey = ((dataStoragePrefix .. tostring(self.config.appId)) .. "_") .. tostring(_:UUID())
    local tabStorage = PlatformAPI:getStorage(self.tabKey)
    if not _:isArray(tabStorage) then
        tabStorage = {}
    end
    if #tabStorage <= self.storageLimit then
        table.insert(tabStorage, dataKey)
        PlatformAPI:setStorage(
        self.tabKey,
        json.encode(tabStorage)
        )
        PlatformAPI:setStorage(
        dataKey,
        json.encode(d)
        )
    else
        local deleteDatas = {}
        for i = 1, 20 do
            table.insert(deleteDatas, table.remove(tabStorage, 1))
        end
        print("deleted events data:" .. tostring(deleteDatas))
        table.insert(tabStorage, dataKey)
        PlatformAPI:setStorage(
        self.tabKey,
        json.encode(tabStorage)
        )
        PlatformAPI:setStorage(
        dataKey,
        json.encode(d)
        )
        local postData = {}
        local dList = {}
        do
            local i = 1
            while i <= #deleteDatas do
                local item = PlatformAPI:getStorage(deleteDatas[i])
                dList[#dList + 1] = item
                i = i + 1
            end
        end
        postData.data = dList
        postData["#app_id"] = self.config.appId
        self:request(postData, deleteDatas)
    end
end
function BatchConsumer.prototype.flush(self)
    if self.timer ~= nil then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self.timer)
    end
    self.timer = nil
    self:send()
    self:batchInterval()
end
function BatchConsumer.prototype.send(self)
    if self.isRequest then
        self.needFlush = true
        return
    end
    local tabStorage = PlatformAPI:getStorage(self.tabKey)
    if tabStorage then
        if #tabStorage > 0 then
            local postData = {}
            local data = {}
            local tabList = {}
            local ____temp_2
            if #tabStorage < self.batchConfig.size then
                ____temp_2 = #tabStorage
            else
                ____temp_2 = self.batchConfig.size
            end
            local len = ____temp_2
            do
                local i = 1
                while i <= len do
                    local d = PlatformAPI:getStorage(tabStorage[i])
                    data[#data + 1] = d
                    tabList[#tabList + 1] = tabStorage[i]
                    i = i + 1
                end
            end
            postData.data = data
            postData["#app_id"] = self.config.appId
            self:request(postData, tabList)
        end
    end
end
function BatchConsumer.prototype.request(self, data, dataKeys)
    local ____self = self
    logger:info("Flush data: " .. json.encode(data))
    ____self.isRequest = true
    senderQueue:enqueue(
    data,
    self.ta.serverUrl,
    {
        maxRetries = self.config.maxRetries,
        sendTimeout = self.config.sendTimeout,
        callback = function(self, res)
            if res.code == 0 then
                ____self:remove(dataKeys)
            end
            ____self.isRequest = false
            do
                local i = 1
                while i <= #____self.trackList do
                    ____self:add(____self.trackList[i])
                    i = i + 1
                end
            end
            ____self.trackList = {}
            if ____self.needFlush then
                ____self.needFlush = false
                ____self:flush()
            end
        end,
        debugMode = self.config.debugMode,
        deviceId = self.ta:getDeviceId()
    }
    )
end
function BatchConsumer.prototype.remove(self, dataKeys)
    local tabStorage = PlatformAPI:getStorage(self.tabKey)
    if tabStorage then
        do
            local i = 1
            while i <= #dataKeys do
                local idx = _:indexOf(tabStorage, dataKeys[i])
                if idx > -1 then
                    table.remove(tabStorage, idx)
                end
                PlatformAPI:removeStorage(dataKeys[i])
                i = i + 1
            end
        end
        PlatformAPI:setStorage(
        self.tabKey,
        json.encode(tabStorage)
        )
    end
end
function BatchConsumer.prototype.recycle(self)
end
____exports.default = _.__TS__Class()
local ThinkingDataAPI = ____exports.default
ThinkingDataAPI.name = "ThinkingDataAPI"
function ThinkingDataAPI.prototype.____constructor(self, config)
    local ____config_4 = config
    local ____config_appId_3
    if config.appId then
        ____config_appId_3 = _:checkAppId(config.appId)
    else
        ____config_appId_3 = _:checkAppId(config.appid)
    end
    ____config_4.appId = ____config_appId_3
    local ____config_6 = config
    local ____config_serverUrl_5
    if config.serverUrl then
        ____config_serverUrl_5 = _:checkUrl(config.serverUrl)
    else
        ____config_serverUrl_5 = _:checkUrl(config.server_url)
    end
    ____config_6.serverUrl = ____config_serverUrl_5
    local defaultConfig = _.extend(
    {},
    DEFAULT_CONFIG,
    PlatformAPI:getConfig()
    )
    if _:isObject(config) then
        self.config = _.extend(defaultConfig, config)
    else
        self.config = defaultConfig
    end
    self:_init(self.config)
end
function ThinkingDataAPI.prototype._init(self, config)
    self.name = config.name
    self.appId = config.appId or config.appid
    local serverUrl = config.serverUrl or config.server_url
    self.serverUrl = tostring(serverUrl) .. "/sync_xcx"
    self.serverDebugUrl = tostring(serverUrl) .. "/data_debug"
    self.configUrl = tostring(serverUrl) .. "/config"
    self.autoTrackProperties = {}
    self._queue = {}
    self:updateConfig(self.configUrl, self.appId)
    if config.isChildInstance then
        self._state = {}
    else
        logger.enabled = config.enableLog
        self.instances = {}
        self._state = { getSystemInfo = false, initComplete = false }
        PlatformAPI:setGlobal(self, self.name)
    end
    self.store = _.__TS__New(
    ThinkingDataPersistence,
    config,
    function()
        if self.config.asyncPersistence and _:isFunction(self.config.persistenceComplete) then
            self.config:persistenceComplete(self)
        end
        self:_updateState()
    end
    )
    local ____isBoolean_result_7
    if _:isBoolean(self.store:_get("ta_enabled")) then
        ____isBoolean_result_7 = self.store:_get("ta_enabled")
    else
        ____isBoolean_result_7 = true
    end
    self.enabled = ____isBoolean_result_7
    local ____isBoolean_result_8
    if _:isBoolean(self.store:_get("ta_isOptOut")) then
        ____isBoolean_result_8 = self.store:_get("ta_isOptOut")
    else
        ____isBoolean_result_8 = false
    end
    self.isOptOut = ____isBoolean_result_8
    if not config.isChildInstance and config.autoTrack then
        self.autoTrack = PlatformAPI:initAutoTrackInstance(self, config)
    end
    if self.config.enableBatch ~= nil and self.config.enableBatch ~= false then
        self.batchConsumer = _.__TS__New(BatchConsumer, self.config, self)
        self.batchConsumer:batchInterval()
    end
end
function ThinkingDataAPI.prototype.initSystemInfo(self)
    if not self.config.isChildInstance then
        systemInformation:getSystemInfo(function()
            self:_updateState({ getSystemInfo = true })
        end)
    end
end
function ThinkingDataAPI.prototype.updateConfig(self, configUrl, appId)
    local headers = _:createExtraHeaders()
    headers["content-type"] = "application/json"
    local request = PlatformAPI:request({
        url = (tostring(configUrl) .. "?appid=") .. tostring(appId),
        method = "GET",
        header = headers,
        success = function(____, res)
            if not _:isUndefined(res) and not _:isUndefined(res.data) then
                logger:info(((("Get remote config success" .. "(") .. tostring(appId)) .. ") :") .. json.encode(res.data))
                if not _:isUndefined(res.data.data) then
                    self.config.syncBatchSize = res.data.data.sync_batch_size
                    self.config.syncInterval = res.data.data.sync_interval
                    self.config.disableEventList = res.data.data.disable_event_list
                    if not _:isUndefined(res.data.data.secret_key) then
                        local secretKey = res.data.data.secret_key
                        self.config.secretKey = { publicKey = secretKey.key, version = secretKey.version }
                    end
                end
            end
        end,
        fail = function(____, res)
            logger:info(((("Get remote config fail" .. "(") .. tostring(appId)) .. ") :") .. tostring(res.errMsg))
        end
    })
    local timer
    timer = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        if (_:isObject(request) or _:isPromise(request)) and _:isFunction(request.abort) then
            request:abort()
        end
        if timer ~= nil then
            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(timer)
            timer = nil
        end
    end, 3, false)
end
function ThinkingDataAPI.prototype.initInstance(self, name, config)
    if self.config.isChildInstance then
        logger:warn("initInstance() cannot be called on child instance")
        return nil
    end
    if _:isString(name) and name ~= self.name and _:isUndefined(self[name]) then
        local instance = _.__TS__New(
        ____exports.default,
        _.extend({}, self.config, { enablePersistence = false, isChildInstance = true, name = name }, config)
        )
        self[name] = instance
        table.insert(self.instances, name)
        self[name]._state = self._state
        return instance
    else
        logger:warn("initInstance() failed due to the name is invalid: " .. tostring(name))
        return nil
    end
end
function ThinkingDataAPI.prototype.lightInstance(self, name)
    return self[name]
end
function ThinkingDataAPI.prototype._setAutoTrackProperties(self, props)
    _.extend(self.autoTrackProperties, props)
end
function ThinkingDataAPI.prototype.init(self)
    self:initSystemInfo()
    if self._state.initComplete then
        return false
    end
    self:_updateState({ initComplete = true })
    logger:info((((((((((("TDAnalytics SDK initialize success, AppId = " .. tostring(self.config.appId)) .. ", ServerUrl = ") .. tostring(self.config.serverUrl)) .. ", Mode = ") .. tostring(self.config.model)) .. ", DeviceId = ") .. tostring(self:getDeviceId())) .. ", Lib = ") .. tostring(Config.LIB_NAME)) .. ", LibVersion = ") .. tostring(Config.LIB_VERSION))
end
function ThinkingDataAPI.prototype._isReady(self)
    return self._state.getSystemInfo and self._state.initComplete and self.store.initComplete and self:getDeviceId()
end
function ThinkingDataAPI.prototype._updateState(self, state)
    if _:isObject(state) then
        _.extend(self._state, state)
    end
    self:_onStateChange()
    _:each(
    self.instances,
    function(____, name)
        self[name]:_onStateChange()
    end
    )
end
function ThinkingDataAPI.prototype._onStateChange(self)
    if self:_isReady() and self._queue and #self._queue > 0 then
        _:each(
        self._queue,
        function(____, item)
            self[item[1]]:apply(
            self,
            slice:call(item[2])
            )
        end
        )
        self._queue = {}
    end
end
function ThinkingDataAPI.prototype._hasDisabled(self)
    local hasDisabled = not self.enabled or self.isOptOut
    if hasDisabled then
        logger:info("SDK is Pause or Stop!")
    end
    return hasDisabled
end
function ThinkingDataAPI.prototype._sendRequest(self, eventData, time)
    if self:_hasDisabled() then
        return
    end
    -- if not _:isUndefined(self.config.disableEventList) then
    --     if self.config.disableEventList:includes(eventData.eventName) then
    --         logger:info("Disabled Event : " .. tostring(eventData.eventName))
    --         return
    --     end
    -- end
    local ____isDate_result_9
    if _:isDate(time) then
        ____isDate_result_9 = time
    else
        ____isDate_result_9 = os.date("%Y-%m-%d %H:%M:%S")
    end
    time = ____isDate_result_9
    local data = { data = { {
        ["#type"] = eventData.type,
        ["#time"] = time,
        ["#distinct_id"] = self.store:getDistinctId()
    } } }
    if self.store:getAccountId() then
        data.data[1]["#account_id"] = self.store:getAccountId()
    end
    if eventData.type == "track" or eventData.type == "track_update" or eventData.type == "track_overwrite" then
        data.data[1]["#event_name"] = eventData.eventName
        if eventData.type == "track_update" or eventData.type == "track_overwrite" then
            data.data[1]["#event_id"] = eventData.extraId
        elseif eventData.firstCheckId then
            data.data[1]["#first_check_id"] = eventData.firstCheckId
        end
        local ____data_data__1_14 = data.data[1]
        local _____12 = _
        local ____extend_13 = _.extend
        local ____array_11 = __TS__SparseArrayNew(
        {["#zone_offset"] = 0 - getTimezoneOffset() / 60 },
        systemInformation.properties,
        self.autoTrackProperties,
        self.store:getSuperProperties()
        )
        local ____table_dynamicProperties_10
        if self.dynamicProperties then
            ____table_dynamicProperties_10 = self:dynamicProperties()
        else
            ____table_dynamicProperties_10 = {}
        end
        __TS__SparseArrayPush(____array_11, ____table_dynamicProperties_10)
        ____data_data__1_14.properties = ____extend_13(
        _____12,
        __TS__SparseArraySpread(____array_11)
        )
        local startTimestamp = self.store:removeEventTimer(eventData.eventName)
        if not _:isUndefined(startTimestamp) then
            local duration = os.time() - startTimestamp
            if duration > 86400 then
                duration = 86400
            elseif duration < 0 then
                duration = 0
            end
            data.data[1].properties["#duration"] = duration
        end
    else
        data.data[1].properties = {}
    end
    if _:isObject(eventData.properties) and not _:isEmptyObject(eventData.properties) then
        _.extend(data.data[1].properties, eventData.properties)
    end
    if self.config.maxRetries > 1 then
        data.data[1]["#uuid"] = _:UUIDv4()
    end
    data["#app_id"] = self.appId
    logger:info("Tracking data, " .. json.encode(data, nil, 4))
    local ____temp_15
    if self.config.debugMode == "debug" or self.config.debugMode == "debugOnly" then
        ____temp_15 = self.serverDebugUrl
    else
        ____temp_15 = self.serverUrl
    end
    local serverUrl = ____temp_15
    if self.batchConsumer and self.config.debugMode == "none" then
        self.batchConsumer:add(data.data[1])
        if _:isFunction(eventData.onComplete) then
            eventData:onComplete({ code = 0, msg = "success" })
        end
        return
    end
    senderQueue:enqueue(
    data,
    serverUrl,
    {
        maxRetries = self.config.maxRetries,
        sendTimeout = self.config.sendTimeout,
        callback = eventData.onComplete,
        debugMode = self.config.debugMode,
        deviceId = self:getDeviceId()
    }
    )
end
function ThinkingDataAPI.prototype._isObjectParams(self, obj)
    return _:isObject(obj) and _:isFunction(obj.onComplete)
end
function ThinkingDataAPI.prototype.track(self, eventName, properties, time, onComplete)
    if self:_hasDisabled() then
        return
    end
    if self:_isObjectParams(eventName) then
        local options = eventName
        eventName = options.eventName
        properties = options.properties
        time = options.time
        onComplete = options.onComplete
    end
    if PropertyChecker:event(eventName) and PropertyChecker:properties(properties) or not self.config.strict then
        self:_internalTrack(eventName, properties, time, onComplete)
    elseif _:isFunction(onComplete) then
        onComplete(nil, { code = -1, msg = "invalid parameters" })
    end
end
function ThinkingDataAPI.prototype.trackUpdate(self, options)
    if self:_hasDisabled() then
        return
    end
    if options and options.eventId and (PropertyChecker:event(options.eventName) and PropertyChecker:properties(options.properties) or not self.config.strict) then
        if self:_isReady() then
            local property = _:checkCalibration(options.properties, options.time, self.config.enableCalibrationTime)
            local ____isDate_result_16
            if _:isDate(options.time) then
                ____isDate_result_16 = options.time
            else
                ____isDate_result_16 = os.date("%Y-%m-%d %H:%M:%S")
            end
            local time = ____isDate_result_16
            self:_sendRequest({
                type = "track_update",
                eventName = options.eventName,
                properties = property,
                onComplete = options.onComplete,
                extraId = options.eventId
            }, time)
        else
            table.insert(self._queue, { "trackUpdate", { options } })
        end
    else
        logger:warn("Invalide parameter for trackUpdate: you should pass an object contains eventId to trackUpdate()")
        if _:isFunction(options.onComplete) then
            options:onComplete({ code = -1, msg = "invalid parameters" })
        end
    end
end
function ThinkingDataAPI.prototype.trackOverwrite(self, options)
    if self:_hasDisabled() then
        return
    end
    if options and options.eventId and (PropertyChecker:event(options.eventName) and PropertyChecker:properties(options.properties) or not self.config.strict) then
        if self:_isReady() then
            local property = _:checkCalibration(options.properties, options.time, self.config.enableCalibrationTime)
            local ____isDate_result_17
            if _:isDate(options.time) then
                ____isDate_result_17 = options.time
            else
                ____isDate_result_17 = os.date("%Y-%m-%d %H:%M:%S")
            end
            local time = ____isDate_result_17
            self:_sendRequest({
                type = "track_overwrite",
                eventName = options.eventName,
                properties = property,
                onComplete = options.onComplete,
                extraId = options.eventId
            }, time)
        else
            table.insert(self._queue, { "trackOverwrite", { options } })
        end
    else
        logger:warn("Invalide parameter for trackOverwrite: you should pass an object contains eventId to trackOverwrite()")
        if _:isFunction(options.onComplete) then
            options:onComplete({ code = -1, msg = "invalid parameters" })
        end
    end
end
function ThinkingDataAPI.prototype.trackFirstEvent(self, options)
    if self:_hasDisabled() then
        return
    end
    if options and options.eventName and (PropertyChecker:event(options.eventName) and PropertyChecker:properties(options.properties) or not self.config.strict) then
        if self:_isReady() then
            local property = _:checkCalibration(options.properties, options.time, self.config.enableCalibrationTime)
            local ____isDate_result_18
            if _:isDate(options.time) then
                ____isDate_result_18 = options.time
            else
                ____isDate_result_18 = os.date("%Y-%m-%d %H:%M:%S")
            end
            local time = ____isDate_result_18
            local ____self__sendRequest_23 = self._sendRequest
            local ____options_eventName_20 = options.eventName
            local ____property_21 = property
            local ____options_onComplete_22 = options.onComplete
            local ____options_firstCheckId_19
            if options.firstCheckId then
                ____options_firstCheckId_19 = options.firstCheckId
            else
                ____options_firstCheckId_19 = self:getDeviceId()
            end
            ____self__sendRequest_23(self, {
                type = "track",
                eventName = ____options_eventName_20,
                properties = ____property_21,
                onComplete = ____options_onComplete_22,
                firstCheckId = ____options_firstCheckId_19
            }, time)
        else
            table.insert(self._queue, { "trackFirstEvent", { options } })
        end
    else
        logger:warn("Invalide parameter for trackFirstEvent: you should pass an object contains eventName to trackFirstEvent()")
        if _:isFunction(options.onComplete) then
            options:onComplete({ code = -1, msg = "invalid parameters" })
        end
    end
end
function ThinkingDataAPI.prototype._internalTrack(self, eventName, properties, time, onComplete)
    if self:_hasDisabled() then
        return
    end
    local property = _:checkCalibration(properties, time, self.config.enableCalibrationTime)
    local ____isDate_result_24
    if _:isDate(time) then
        ____isDate_result_24 = time
    else
        ____isDate_result_24 = os.date("%Y-%m-%d %H:%M:%S")
    end
    time = ____isDate_result_24
    if self:_isReady() then
        self:_sendRequest({ type = "track", eventName = eventName, properties = property, onComplete = onComplete }, time)
    else
        table.insert(self._queue, { "_internalTrack", { eventName, properties, time, onComplete } })
    end
end
function ThinkingDataAPI.prototype.userSet(self, properties, time, onComplete)
    if self:_hasDisabled() then
        return
    end
    if self:_isObjectParams(properties) then
        local options = properties
        properties = options.properties
        time = options.time
        onComplete = options.onComplete
    end
    if PropertyChecker:propertiesMust(properties) or not self.config.strict then
        local ____isDate_result_25
        if _:isDate(time) then
            ____isDate_result_25 = time
        else
            ____isDate_result_25 = os.date("%Y-%m-%d %H:%M:%S")
        end
        time = ____isDate_result_25
        if self:_isReady() then
            self:_sendRequest({ type = "user_set", properties = properties, onComplete = onComplete }, time)
        else
            table.insert(self._queue, { "userSet", { properties, time, onComplete } })
        end
    else
        logger:warn("calling userSet failed due to invalid arguments")
        if _:isFunction(onComplete) then
            onComplete(nil, { code = -1, msg = "invalid parameters" })
        end
    end
end
function ThinkingDataAPI.prototype.userSetOnce(self, properties, time, onComplete)
    if self:_hasDisabled() then
        return
    end
    if self:_isObjectParams(properties) then
        local options = properties
        properties = options.properties
        time = options.time
        onComplete = options.onComplete
    end
    if PropertyChecker:propertiesMust(properties) or not self.config.strict then
        local ____isDate_result_26
        if _:isDate(time) then
            ____isDate_result_26 = time
        else
            ____isDate_result_26 = os.date("%Y-%m-%d %H:%M:%S")
        end
        time = ____isDate_result_26
        if self:_isReady() then
            self:_sendRequest({ type = "user_setOnce", properties = properties, onComplete = onComplete }, time)
        else
            table.insert(self._queue, { "userSetOnce", { properties, time, onComplete } })
        end
    else
        logger:warn("calling userSetOnce failed due to invalid arguments")
        if _:isFunction(onComplete) then
            onComplete(nil, { code = -1, msg = "invalid parameters" })
        end
    end
end
function ThinkingDataAPI.prototype.userUnset(self, property, time, onComplete)
    if self:_hasDisabled() then
        return
    end
    if self:_isObjectParams(property) then
        local options = property
        property = options.property
        time = options.time
        onComplete = options.onComplete
    end
    if PropertyChecker:propertyName(property) or not self.config.strict then
        local ____isDate_result_27
        if _:isDate(time) then
            ____isDate_result_27 = time
        else
            ____isDate_result_27 = os.date("%Y-%m-%d %H:%M:%S")
        end
        time = ____isDate_result_27
        if self:_isReady() then
            local properties
            properties = {}
            properties[property] = 0
            self:_sendRequest({ type = "user_unset", properties = properties, onComplete = onComplete }, time)
        else
            table.insert(self._queue, { "userUnset", { property, onComplete, time } })
        end
    else
        logger:warn("calling userUnset failed due to invalid arguments")
        if _:isFunction(onComplete) then
            onComplete(nil, { code = -1, msg = "invalid parameters" })
        end
    end
end
function ThinkingDataAPI.prototype.userDel(self, time, onComplete)
    if self:_hasDisabled() then
        return
    end
    if self:_isObjectParams(time) then
        local options = time
        time = options.time
        onComplete = options.onComplete
    end
    local ____isDate_result_28
    if _:isDate(time) then
        ____isDate_result_28 = time
    else
        ____isDate_result_28 = os.date("%Y-%m-%d %H:%M:%S")
    end
    time = ____isDate_result_28
    if self:_isReady() then
        self:_sendRequest({ type = "user_del", onComplete = onComplete }, time)
    else
        table.insert(self._queue, { "userDel", { time, onComplete } })
    end
end
function ThinkingDataAPI.prototype.userAdd(self, properties, time, onComplete)
    if self:_hasDisabled() then
        return
    end
    if self:_isObjectParams(properties) then
        local options = properties
        properties = options.properties
        time = options.time
        onComplete = options.onComplete
    end
    if PropertyChecker:userAddProperties(properties) or not self.config.strict then
        local ____isDate_result_29
        if _:isDate(time) then
            ____isDate_result_29 = time
        else
            ____isDate_result_29 = os.date("%Y-%m-%d %H:%M:%S")
        end
        time = ____isDate_result_29
        if self:_isReady() then
            self:_sendRequest({ type = "user_add", properties = properties, onComplete = onComplete }, time)
        else
            table.insert(self._queue, { "userAdd", { properties, time, onComplete } })
        end
    else
        logger:warn("calling userAdd failed due to invalid arguments")
        if _:isFunction(onComplete) then
            onComplete(nil, { code = -1, msg = "invalid parameters" })
        end
    end
end
function ThinkingDataAPI.prototype.userAppend(self, properties, time, onComplete)
    if self:_hasDisabled() then
        return
    end
    if self:_isObjectParams(properties) then
        local options = properties
        properties = options.properties
        time = options.time
        onComplete = options.onComplete
    end
    if PropertyChecker:userAppendProperties(properties) or not self.config.strict then
        local ____isDate_result_30
        if _:isDate(time) then
            ____isDate_result_30 = time
        else
            ____isDate_result_30 = os.date("%Y-%m-%d %H:%M:%S")
        end
        time = ____isDate_result_30
        if self:_isReady() then
            self:_sendRequest({ type = "user_append", properties = properties, onComplete = onComplete }, time)
        else
            table.insert(self._queue, { "userAppend", { properties, time, onComplete } })
        end
    else
        logger:warn("calling userAppend failed due to invalid arguments")
        if _:isFunction(onComplete) then
            onComplete(nil, { code = -1, msg = "invalid parameters" })
        end
    end
end
function ThinkingDataAPI.prototype.userUniqAppend(self, properties, time, onComplete)
    if self:_hasDisabled() then
        return
    end
    if self:_isObjectParams(properties) then
        local options = properties
        properties = options.properties
        time = options.time
        onComplete = options.onComplete
    end
    if PropertyChecker:userAppendProperties(properties) or not self.config.strict then
        local ____isDate_result_31
        if _:isDate(time) then
            ____isDate_result_31 = time
        else
            ____isDate_result_31 = os.date("%Y-%m-%d %H:%M:%S")
        end
        time = ____isDate_result_31
        if self:_isReady() then
            self:_sendRequest({ type = "user_uniq_append", properties = properties, onComplete = onComplete }, time)
        else
            table.insert(self._queue, { "userUniqAppend", { properties, time, onComplete } })
        end
    else
        logger:warn("calling userAppend failed due to invalid arguments")
        if _:isFunction(onComplete) then
            onComplete(nil, { code = -1, msg = "invalid parameters" })
        end
    end
end
function ThinkingDataAPI.prototype.flush(self)
    if self.batchConsumer and self.config.debugMode == "none" then
        self.batchConsumer:flush()
    end
end
function ThinkingDataAPI.prototype.authorizeOpenID(self, id)
    self:identify(id)
end
function ThinkingDataAPI.prototype.identify(self, distinctId)
    if self:_hasDisabled() then
        return
    end
    if type(distinctId) == "number" then
        distinctId = tostring(distinctId)
    elseif type(distinctId) ~= "string" then
        return false
    end
    self.store:setDistinctId(distinctId)
    logger:info("Setting distinct ID, DistinctId = " .. tostring(distinctId))
end
function ThinkingDataAPI.prototype.getDistinctId(self)
    return self.store:getDistinctId()
end
function ThinkingDataAPI.prototype.login(self, accoundId)
    if self:_hasDisabled() then
        return
    end
    if type(accoundId) == "number" then
        accoundId = tostring(accoundId)
    elseif type(accoundId) ~= "string" then
        return false
    end
    self.store:setAccountId(accoundId)
    logger:info("Login SDK, AccountId = " .. tostring(accoundId))
end
function ThinkingDataAPI.prototype.getAccountId(self)
    return self.store:getAccountId()
end
function ThinkingDataAPI.prototype.logout(self)
    if self:_hasDisabled() then
        return
    end
    self.store:setAccountId(nil)
    logger:info("Logout SDK")
end
function ThinkingDataAPI.prototype.setSuperProperties(self, obj)
    if self:_hasDisabled() then
        return
    end
    if PropertyChecker:propertiesMust(obj) or not self.config.strict then
        self.store:setSuperProperties(obj)
    else
        logger:warn("setSuperProperties parameter must be a valid property value")
    end
end
function ThinkingDataAPI.prototype.clearSuperProperties(self)
    if self:_hasDisabled() then
        return
    end
    self.store:setSuperProperties({}, true)
end
function ThinkingDataAPI.prototype.unsetSuperProperty(self, propertyName)
    if self:_hasDisabled() then
        return
    end
    if _:isString(propertyName) then
        local superProperties = self:getSuperProperties()
        superProperties[propertyName] = nil
        self.store:setSuperProperties(superProperties, true)
    end
end
function ThinkingDataAPI.prototype.getSuperProperties(self)
    return self.store:getSuperProperties()
end
function ThinkingDataAPI.prototype.getPresetProperties(self)
    local properties = systemInformation.properties
    local presetProperties = {}
    local os = properties["#os"]
    presetProperties.os = _:isUndefined(os) and "" or os
    local screenWidth = properties["#screen_width"]
    presetProperties.screenWidth = _:isUndefined(screenWidth) and 0 or screenWidth
    local screenHeight = properties["#screen_height"]
    presetProperties.screenHeight = _:isUndefined(screenHeight) and 0 or screenHeight
    local networkType = properties["#network_type"]
    presetProperties.networkType = _:isUndefined(networkType) and "" or networkType
    local deviceModel = properties["#device_model"]
    presetProperties.deviceModel = _:isUndefined(deviceModel) and "" or deviceModel
    local osVersion = properties["#os_version"]
    presetProperties.osVersion = _:isUndefined(osVersion) and "" or osVersion
    presetProperties.deviceId = self:getDeviceId()
    local zoneOffset = 0 - getTimezoneOffset() / 60
    presetProperties.zoneOffset = zoneOffset
    local manufacturer = properties["#manufacturer"]
    presetProperties.manufacturer = _:isUndefined(manufacturer) and "" or manufacturer
    presetProperties.toEventPresetProperties = function(self)
        return {
            ["#device_model"] = presetProperties.deviceModel,
            ["#device_id"] = presetProperties.deviceId,
            ["#screen_width"] = presetProperties.screenWidth,
            ["#screen_height"] = presetProperties.screenHeight,
            ["#os"] = presetProperties.os,
            ["#os_version"] = presetProperties.osVersion,
            ["#network_type"] = presetProperties.networkType,
            ["#zone_offset"] = zoneOffset,
            ["#manufacturer"] = presetProperties.manufacturer
        }
    end
    return presetProperties
end
function ThinkingDataAPI.prototype.setDynamicSuperProperties(self, dynamicProperties)
    if self:_hasDisabled() then
        return
    end
    if type(dynamicProperties) == "function" then
        if PropertyChecker:properties(dynamicProperties(nil)) or not self.config.strict then
            self.dynamicProperties = dynamicProperties
        else
            logger:warn("A dynamic public property must return a valid property value")
        end
    else
        logger:warn("setDynamicSuperProperties parameter must be a function type")
    end
end
function ThinkingDataAPI.prototype.timeEvent(self, eventName, time)
    if self:_hasDisabled() then
        return
    end
    local ____isDate_result_32
    if _:isDate(time) then
        ____isDate_result_32 = time
    else
        ____isDate_result_32 = os.date("%Y-%m-%d %H:%M:%S")
    end
    time = ____isDate_result_32
    if self:_isReady() then
        if PropertyChecker:event(eventName) or not self.config.strict then
            self.store:setEventTimer(
            eventName,
            time:getTime()
            )
        else
            logger:warn("calling timeEvent failed due to invalid eventName: " .. tostring(eventName))
        end
    else
        table.insert(self._queue, { "timeEvent", { eventName, time } })
    end
end
function ThinkingDataAPI.prototype.getDeviceId(self)
    return systemInformation.properties["#device_id"]
end
function ThinkingDataAPI.prototype.enableTracking(self, enabled)
    self.enabled = enabled
    self.store:_set("ta_enabled", enabled)
end
function ThinkingDataAPI.prototype.optOutTracking(self)
    self.store:setSuperProperties({}, true)
    self.store:setDistinctId(_:UUID())
    self.store:setAccountId(nil)
    self._queue = {}
    self.isOptOut = true
    self.store:_set("ta_isOptOut", true)
end
function ThinkingDataAPI.prototype.optOutTrackingAndDeleteUser(self)
    local time = os.date("%Y-%m-%d %H:%M:%S")
    self:_sendRequest({ type = "user_del" }, time)
    self:optOutTracking()
end
function ThinkingDataAPI.prototype.optInTracking(self)
    self.isOptOut = false
    self.store:_set("ta_isOptOut", false)
end
function ThinkingDataAPI.prototype.setTrackStatus(self, status)
    repeat
        local ____switch262 = status
        local ____cond262 = ____switch262 == "PAUSE"
        if ____cond262 then
            self.eventSaveOnly = false
            self:optInTracking()
            self:enableTracking(false)
            break
        end
        ____cond262 = ____cond262 or ____switch262 == "STOP"
        if ____cond262 then
            self.eventSaveOnly = false
            self:optOutTracking(true)
            break
        end
        ____cond262 = ____cond262 or ____switch262 == "SAVE_ONLY"
        if ____cond262 then
            break
        end
        ____cond262 = ____cond262 or ____switch262 == "NORMAL"
        do
            self.eventSaveOnly = false
            self:optInTracking()
            self:enableTracking(true)
            break
        end
    until true
    logger:info("Change Status to " .. tostring(status))
end
return ____exportsà»  