-- local ZIP_LIST = {"res.dat", "src.dat"}
-- local cjson = require("cjson")

local HotFixManager = un.instance()
local HotFixStage = un.const.HotFixStage
local HotFixError = un.const.HotFixError
local HotFixType = un.const.HotFixType

function HotFixManager:ctor(listener, prefix, isFirst, isMerge, downloadPriority)
    self._listener = listener
    self._prefix = prefix
    self._isFirst = isFirst
    self._isMerge = isMerge ~= false and true or false
    self._downloadPriority = tonumber(downloadPriority) or 0
    self._isTerminate = false
    self._isPreferZip = false
    self._preProgress = 0
    self._processStatus = {}
    self._processStatusCount = 0
    self._zipProgressTable = {}
    self._unzipProgressTable = {}
    self._downloadProgressTable = {}
    self._decompressProgressTable = {}
    self._hotfixType = HotFixType.NOUPDATE
    self._hotfixInfo = {
        oldVersion = "",
        newVersion = "",
        fileCount = 0,
        fileSize = 0,
    }
    self._downloadInfo = {}
end

function HotFixManager:preferZip()
    print("HotFixManager: prefer zip")
    self._isPreferZip = true
end

function HotFixManager:start(config)
    local fs = un.FileSystem
    local writePath = fs.getWritePath()
    local rootPath = writePath .. un.const.HotFixPath
    local hotfixPath = rootPath .. un.const.HotfixSubPath
    if not fs.createDirectory(hotfixPath) then
        self:onError(nil, HotFixError.CREATE_HOTFIX_DIRECTORY_FAILED)
        return
    end

    self._config = config
    self._rootPath = rootPath
    self._hotfixPath = hotfixPath

    self:_checkLocalRes()
end

function HotFixManager:_checkLocalRes()
    print("HotFixManager. check local res ...")
    local config = self._config
    local hotfixPath = self._hotfixPath
    local rootPath = self._rootPath

    local records = {
        hotfixTypesKeys = {},
        downloadInfoKeys = {},
        downloadKeys = {},
        decompressKeys = {},
    }

    local processor = {}
    local processorCount = 0
    self._processor = processor
    self._records = records
    self._hotfixTypes = {}

    local fs = un.FileSystem
    for k,v in pairs(config.HotUpdateList) do
        local processPath = hotfixPath .. k .. '/'
        if not fs.createDirectory(processPath) then
            self:onError(nil, HotFixError.CREATE_PROCESS_DIRECTORY_FAILED, v)
            return
        end
        -- processor[k] = un.hotfix.HotFixProcessor.new(k, rootPath, processPath, v, self, self._prefix, self._isFirst)        
        processor[k] = un.hotfix.processorHolder.obtain(self, k, rootPath, processPath, v, self._prefix, self._downloadPriority, self._isFirst, self._isPreferZip)
        processorCount = processorCount + 1
    end

    self._processorCount = processorCount

    -- 检查本地的资源版本是否比包体新，否则就要重新从包体解压
    local success = true
    for k,v in pairs(processor) do
        if not v:loadManifest() then
            success = false
        end
    end

    if success then
        self:_startHotFix()
    else
        self:_terminate()
    end
end

function HotFixManager:_startHotFix()
    print("HotFixManager. start hotfix ...")
    for k,v in pairs(self._processor) do
        v:start()
    end
end

function HotFixManager:onVersionDownload(key, hotfixType, oldVersion, newVersion, msg)
    local records = self._records
    if table.has(records.hotfixTypesKeys, key) then
        return
    end

    local hotfixTypes = self._hotfixTypes
    if key and hotfixType then
        hotfixTypes[key] = {hotfixType, oldVersion, newVersion, msg}
        table.insert(records.hotfixTypesKeys, key)
    end

    if #records.hotfixTypesKeys + self._processStatusCount == self._processorCount then 
        local priority = { 
            [HotFixType.NOUPDATE] = 1,
            [HotFixType.SILENT] = 2,
            [HotFixType.NORMAL] = 3,
            [HotFixType.FORCE] = 4,
        }
        local hotfixList = {}
        for k,v in pairs(hotfixTypes) do
            hotfixList[#hotfixList + 1] = v[1]
        end
        table.sort(hotfixList, function(a, b)
            return priority[a] > priority[b]
        end)

        hotfixType = hotfixList[1]

        local fixKey
        for k,v in pairs(hotfixTypes) do
            if v[1] == hotfixType then
                fixKey = k
            end
        end

        self._hotfixInfo.oldVersion = hotfixTypes[fixKey][2]
        self._hotfixInfo.newVersion = hotfixTypes[fixKey][3]
        if hotfixType == HotFixType.NOUPDATE then -- 无需更新也要抛送埋点
            -- self:_report(key, true)
            self:_releaseProcessors()
        end
        self._listener:onChooseHotFixType(key, hotfixType, hotfixTypes[fixKey][2], hotfixTypes[fixKey][3], hotfixTypes[fixKey][4])
    end
end

function HotFixManager:doUpdate(chooseHotfixType, isMerge)
    self._hotfixType = chooseHotfixType
    -- self._isMerge = isMerge ~= false and true or false
    for k,v in pairs(self._processor) do
        local hotfixType = self._hotfixTypes[k]
        local isError = self._processStatus[k]
        if not isError and hotfixType ~= un.const.HotFixType.NOUPDATE then
            v:update()
        end
    end
end

function HotFixManager:onDownloadInfo(key, totalCount, totalSize)
    local records = self._records
    if table.has(records.downloadInfoKeys, key) then
        return
    end
    table.insert(records.downloadInfoKeys, key)

    local hotfixInfo = self._hotfixInfo
    hotfixInfo.fileCount = hotfixInfo.fileCount + totalCount
    hotfixInfo.fileSize = hotfixInfo.fileSize + totalSize

    -- local processor = self._processor[key]
    -- if processor and not self._processStatus[key] then
    --     processor:startDownload()
    -- end

    if  self._hotfixType ~= HotFixType.SILENT and #records.downloadInfoKeys + self._processStatusCount == self._processorCount then
        if self._listener.onDownloadInfo then
            self._listener:onDownloadInfo(hotfixInfo.fileCount, hotfixInfo.fileSize)
        end
    end
end

function HotFixManager:onZipDownloadProgress(key, progress)
    self._zipProgressTable[key] = progress
    self:_onProgress()
end

function HotFixManager:onUnzipProgress(key, progress)
    self._unzipProgressTable[key] = progress
    self:_onProgress()
end

function HotFixManager:onDownloadProgress(key, progress)
    self._downloadProgressTable[key] = progress
    self:_onProgress()
end

function HotFixManager:_onProgress()
    if self._hotfixType ~= HotFixType.SILENT then
        local zip = 0.0
        for _,v in pairs(self._zipProgressTable) do
            zip = zip + v
        end

        local unzip = 0.0
        for _,v in pairs(self._unzipProgressTable) do
            unzip = unzip + v
        end

        local download = 0.0
        for _,v in pairs(self._downloadProgressTable) do
            download = download + v
        end

        local progress = (zip * 0.85 + unzip * 0.1 + download * 0.05) / self._processorCount
        if progress > self._preProgress then
            self._preProgress = progress
            self._listener:onProgress(HotFixStage.DOWNLOAD, progress)
        end
     end
end

function HotFixManager:onDownloadSuccess(key, totalCount, totalSize)
    local records = self._records
    if table.has(records.downloadKeys, key) then
        return
    end

    -- local hotfixInfo = self._hotfixInfo
    -- hotfixInfo.fileCount = hotfixInfo.fileCount + totalCount
    -- hotfixInfo.fileSize = hotfixInfo.fileSize + totalSize

    self._downloadInfo[key] = {count = totalCount, size = totalSize}

    table.insert(records.downloadKeys, key)
    if #records.downloadKeys + self._processStatusCount == self._processorCount then
        -- set progress 1000
        if self._hotfixType ~= HotFixType.SILENT then
            self._listener:onProgress(HotFixStage.DOWNLOAD, 1, key)
        end
        for k,v in pairs(self._processor) do
            local isError = self._processStatus[k]
            if not isError then
                if self._isMerge then
                    v:startDecompress()
                else
                    v:saveDownloadFinishTag()
                    self:onDecompressSuccess(k)
                end
            end
        end
    end
end

function HotFixManager:onDecompressProgress(key, progress)
    local progressTable = self._decompressProgressTable
    progressTable[key] = progress
    local p = 0
    for _,v in pairs(progressTable) do
        p = p + v
    end

    p = p / self._processorCount
    if self._hotfixType ~= HotFixType.SILENT then
        self._listener:onProgress(HotFixStage.DECOMPRESS, p, key)
    end
end

function HotFixManager:onDecompressSuccess(key)
    local records = self._records
    if table.has(records.decompressKeys, key) then
        return
    end

    table.insert(records.decompressKeys, key)
    if #records.decompressKeys + self._processStatusCount == self._processorCount then
        -- self:_report(nil, true)
        self:_releaseProcessors()
        if self._hotfixType ~= HotFixType.SILENT then
            self._listener:onProgress(HotFixStage.DECOMPRESS, 1, key)
            un.FileInterface.clearChache()
            self._listener:onSuccess(key, table.shallow_copy(self._downloadInfo), self._isMerge)
        end
    end
end

function HotFixManager:_terminate(key)
    if self._isTerminate then
        return
    end
    self._isTerminate = true

    key = key or self._processStatus['__FirstKey']
    -- self:_report(key, false)
    self:_releaseProcessors()
    if self._hotfixType ~= HotFixType.SILENT then
        local info = self._processStatus[key]
        self._listener:onFailed(key, info[1], info[2], info[3])
    end
end

function HotFixManager:onError(key, error, msg, data)
    print("HotFixManager.onError. errCode: " .. error.code .. ", errMsg: " .. error.msg)
    if key then
        print("HotFixManager.onError. key = " .. key)
    end
    if msg then
        print("HotFixManager.onError. msg = " .. msg)
    end
    if data then
        print("HotFixManager.onError. data = " .. un.url.toJson(data))
    end

    local processorStatus = self._processStatus
    if key then -- processor error
        local records = self._records
        if not processorStatus[key] then
            processorStatus[key] = {error, msg, data}
            self._processStatusCount = self._processStatusCount + 1
            if self._processStatusCount == 1 then
                processorStatus["__FirstKey"] = key
            end
            if self._processStatusCount == self._processorCount
                or #records.decompressKeys + self._processStatusCount == self._processorCount
                or #records.downloadKeys +self._processStatusCount == self._processorCount
                or #records.hotfixTypesKeys + self._processStatusCount == self._processorCount then
                self:_terminate()
            -- elseif counts.decompressCount > 0 then
            --     self:onDecompressSuccess()
            -- elseif counts.downloadCount > 0 then
            --     self:onDownloadSuccess()
            -- elseif counts.hotfixTypesCount > 0 then
            --     self:onVersionDownload()
            end
        end
    else -- HotFixManager error
        processorStatus['__un.hotfix.HotFixManager'] = {error, msg, data}
        self:_terminate('__un.hotfix.HotFixManager')
    end
end

--[[
function HotFixManager:_report(key, isSuccess)
    print('HotFixManager._report: isSuccess = ' .. tostring(isSuccess))
    key = key or self._processStatus['__FirstKey']
    local errInfo = self._processStatus[key]

    local processor = self._processor[key]
    if not processor or not processor:isLoadLocalManifestSuccess() then
        for k,v in pairs(self._processor) do
            if v:isLoadLocalManifestSuccess() then
                key = k
                processor = v
                break
            end
        end
    end

    if not processor or not processor:isLoadLocalManifestSuccess() then
        return
    end

    if not errInfo then
        errInfo = self._processStatus[key] or { un.const.HotFixError.SUCCESS }
    end

    -- NEED_RESTART do not report
    if errInfo[1] == un.const.HotFixError.NEED_RESTART then
        return
    end

    local info = processor:getReportInfo()
    info['type'] = "1"
    info['ip'] = un.Device.getIp()
    info['device_code'] = un.Device.getUUID()
    info['os'] = un.Device.getOsName()
    info['hardware'] = un.Device.getDeviceName()
    info['net_env'] = un.Device.getNetEnvName()
    info['pack_name'] = string.format("%s#%s", un.Device.getPackageName(), un.Device.getAppVersion())
    info["sdk_ver"] = un.getVersion()
    info['time'] = tostring(os.time())
    info['status'] = isSuccess and "1" or "0"
    info['err_code'] = tostring(errInfo[1].code)

    -- 热更信息
    local hotfixInfo = self._hotfixInfo
    info["before_version"] = hotfixInfo.oldVersion
    info["after_version"] = hotfixInfo.newVersion
    info["file_count"] = hotfixInfo.fileCount
    info["file_size"] = hotfixInfo.fileSize

    -- 错误信息
    local errMessage = errInfo[1].msg
    if errInfo[2] then
        errMessage = errMessage .. "|" .. errInfo[2]
    end
    if errInfo[3] then
        errMessage = errMessage .. "|" .. un.url.toJson(errInfo[3])
    end
    if #errMessage > 512 then
        errMessage = string.sub(errMessage, 1, 512)
    end
    info['err_msg'] = errMessage
    info['cost_time'] = processor:getCostTime()
    info["ext"] = processor:getExtInfo()

    -- 签名
    local sign = un.url.sign(info)
    info['sign'] = sign

    local content = un.url.toJson(info)

    local retryCount = 3
    local http = un.Http.new()
    http:setListener(function (tag, tContent, errCode, errMsg)
        if errCode > 0 then
            if retryCount > 0 then
                retryCount = retryCount - 1
                http:add(processor:getMonitorUrl(), content, "")
            else
                print("HotFixManager._report: error. " .. errMsg)
            end
        end
    end)
    http:add(processor:getMonitorUrl(), content, "")
    http:removeWhenAllTaskComplete()

    print("HotFixManager._report: content = " .. content)
    print("HotFixManager._report: cost time " .. info["cost_time"])

    local psrKeys = {}
    for k,v in pairs(self._processor) do
        table.insert(psrKeys, k)
    end
    print("HotfixManager._report: modules = " .. table.tostring(psrKeys))
end
]]
function HotFixManager:_releaseProcessors()
    for k,v in pairs(self._processor) do
        un.hotfix.processorHolder.release(self, k)
    end
end

function HotFixManager:destroy()
    self:_releaseProcessors()
end

un.hotfix.HotFixManager = HotFixManager

   -;  