--[[
    @下载器流程：
    0. 访问指定网址获取WebService地址
    1. 根据WebService地址拼接版本等信息获取version列表和cdn地址
    2. 下载version列表文件
    3. 下载version文件对应的Manifest文件（校验是否存在同名文件且大小一致，相同则不必下载）
    4. 下载Manifest文件对应的Bundle文件（校验是否存在同名文件且大小一致，相同则不必下载）
    5. 下载成功/失败都会输出日志，过滤关键词"Fish download"

    @使用方式参考：
    local FishDownload = require "client.fish.FishDownload"

    FishDownload:start(resultCallback, progressCallback)
    其中 resultCallback, progressCallback 按需传参
    resultCallback参数true/false代表下载成功/失败

    FishDownload:isDownloadCompleted(function (result)
        print(result) --是否完整true/false
    end)

    @date: 2021.11.08
    @brief: 人人捕鱼资源下载
    @owner: chuyuming
]]

local d2 = un.downloader2
local fs = un.FileSystem
local deferred = require('app.hotupdate.rrfish.deferred')
local writePath = un.FileSystem.getWritePath()
local FishPath = writePath .. "hotfix/fish"

local FishBundleTag = "Bundles"
local BundlesChainLen = 64
local BlockCount = 8 -- 同时下载文件数

local FishDownload = {}
FishDownload._cdnUrl = nil
FishDownload._progressInfo = nil
FishDownload._resultCallback = nil
FishDownload._progressCallback = nil
FishDownload._isCheckCompleteness = nil

function FishDownload:start( resultCallback, progressCallback )
    self:_start(resultCallback, progressCallback)
end

function FishDownload:isDownloadCompleted( callback )
    self:_start(callback, nil, true)
end

function FishDownload:_start( resultCallback, progressCallback, isCheckCompleteness )
    self._resultCallback = resultCallback
    self._progressCallback = progressCallback
    self._isCheckCompleteness = isCheckCompleteness

    local v = self:getVersionInfo()
    local suffix = string.format("ver=%d&vers=%d&cps=%d&pf=%s", v.ver, v.vers, v.cpsId, v.pf)
    local url = string.format("%s/gate/config?%s", v.gate_url, suffix)
    local filePath = string.format("%s/%s", FishPath, "Start")
    self:downloadFilePromise(url, filePath):next(function ( path )
        return self:readConetntPromise(path)
    end):next(function ( content )
        if content.Res > 0 then
            url = string.format("%s%s?%s", content.Data.WebService, v.bundle_uri, suffix)
            filePath = string.format("%s/%s", FishPath, "WebServiceInfo")
            return self:downloadFilePromise(url, filePath)
        end
    end):next(function ( path )
        return self:readConetntPromise(path)
    end):next(function ( content )
        if content.Res > 0 then
            self._cdnUrl = content.Data.url
            self:downloadVersionFiles(content.Data.files)
        end
    end, function ( err )
        self:onDownloadFail()
    end)
end

function FishDownload:getFishBundlesPath( ... )
    return FishPath .. "/" .. FishBundleTag
end

function FishDownload:getVersionInfo( ... )
    local versionInfo
    if device.platform == "android" then
        local pathKey = "configure/rrby_version.json"
        if not cc.FileUtils:getInstance():isFileExist(pathKey) then
           return
        end
        local strVersionInfo = cc.FileUtils:getInstance():getStringFromFile(pathKey)
        if strVersionInfo ~= nil then
            versionInfo = json.decode(strVersionInfo)
        end
    else
        versionInfo = {ver = 3000, vers = 16, cpsId = 1354, pf = "android",
                gate_url = "https://rrby2conf.renrenbuyu.com:8092",
                bundle_uri = "/gate/bundle/v2"}
    end
    return versionInfo
end

function FishDownload:downloadVersionFiles( versionFiles )
    self:willDownloadFilesPromise(versionFiles, "versions"):next(function ( fullFilePaths )
        return self:readConetntsPromise(fullFilePaths)
    end):next(function ( fileConetnts )
        local manifestFiles = {}
        for _,v in ipairs(versionFiles) do
            local file = string.gsub(v, ".version", "")
            table.insert(manifestFiles, file)
        end
        local fileSizes = {}
        for i,v in ipairs(fileConetnts) do
            fileSizes[manifestFiles[i]] = v.size
        end
        return self:willDownloadFilesPromise(manifestFiles, "manifests", fileSizes)
    end):next(function ( fullFilePaths )
        return self:readConetntsPromise(fullFilePaths)
    end):next(function ( fileConetnts )
        local bundleFiles = {}
        local fileSizes = {}
        self._progressInfo = {}
        local totalSize = 0
        for _,content in ipairs(fileConetnts) do
            for _,v in ipairs(content.bundles) do
                table.insert(bundleFiles, v.nameWithAppendHash)
                fileSizes[v.nameWithAppendHash] = v.size
                totalSize = totalSize + v.size
            end
        end
        table.sort( bundleFiles, function ( v1, v2 )
            if fileSizes[v1] == fileSizes[v2] then
                return string.len(v1) < string.len(v2)
            else
                return fileSizes[v1] < fileSizes[v2]
            end
        end )

        for fileName,fileSize in pairs(fileSizes) do
            local filePath = string.format("%s/%s/%s", FishPath, FishBundleTag, fileName)
            self._progressInfo[filePath] = {ratio = fileSize / totalSize, progress = 0}
        end
        return self:willDownloadFilesPromise(bundleFiles, FishBundleTag, fileSizes)
    end):next(function ( ... )
        self:onDownloadSuccess()
    end, function ( ... )
        self:onDownloadFail()
    end)
end

function FishDownload:willDownloadFilesPromise( fileNames, catalog, fileSizes)
    local function doConcurrency( vlist )
        local list = {}
        for _,v in ipairs(vlist) do
            local url = self._cdnUrl .. v
            local fileSize = fileSizes and fileSizes[v]
            table.insert( list, self:prepareFilePromise(url, catalog, v, fileSize) )
        end
        return deferred.all(list)
    end

    local function getGroup( vlist, countInGroup )
        local group = {}
        for i,v in ipairs(vlist) do
            local idx = math.floor((i - 1) / countInGroup) + 1
            group[idx] = group[idx] or {}
            table.insert(group[idx], v)
        end
        return group
    end

    if #fileNames <= BlockCount then
        return doConcurrency(fileNames)
    else
        local countInGroup = #fileNames < (BundlesChainLen * BundlesChainLen) and BundlesChainLen or BundlesChainLen * 2
        return deferred.map(getGroup(fileNames, countInGroup), function ( v )
            return deferred.map(getGroup(v, BlockCount), function ( vlist )
                return doConcurrency(vlist)
            end)
        end)
    end
end

function FishDownload:prepareFilePromise( url, catalog, fileName, fileSize )
    local filePath = string.format("%s/%s/%s", FishPath, catalog, fileName)
    if self:isFileWithSizeExist(filePath, fileSize) then
        if self._progressInfo and self._progressInfo[filePath] then
            self._progressInfo[filePath].progress = 1
        end
        local promise = deferred.new()
        promise:resolve(filePath)
        return promise
    else
        if self._isCheckCompleteness and FishBundleTag == catalog then
            local promise = deferred.new()
            promise:reject(filePath)
            return promise
        else
            return self:downloadFilePromise( url, filePath, fileSize, catalog)
        end
    end
end

function FishDownload:isFileWithSizeExist( filePath, fileSize )
    return filePath and #filePath > 0 and fs.isFileExist(filePath) and fs.getFileSize(filePath) == fileSize
end

function FishDownload:readConetntPromise( fullFilePath )
    local promise = deferred.new()
    if fullFilePath and #fullFilePath > 0 and fs.isFileExist(fullFilePath) then
        local content = fs.readStringFromFile(fullFilePath)
        if #content > 0 then
            promise:resolve(un.utils.str2json(content))
        else
            promise:reject()
        end
    else
        promise:reject()
    end
    return promise
end

function FishDownload:readConetntsPromise( fullFilePaths )
    local promises = {}
    for _,v in ipairs(fullFilePaths) do
        table.insert( promises, self:readConetntPromise(v) )
    end
    return deferred.all(promises)
end

function FishDownload:downloadFilePromise( url, filePath, fileSize, fileTag )
    local promise = deferred.new()
    local retryCount = 2
    local curCount = 0
    local cbId

    local saveProgress, curProgress, progressTable
    if fileSize then
        saveProgress = 0
        curProgress = 0
        progressTable = {}
    end

    cbId = d2.addListener(function (tag, costTime, errCode, errMsg)
        curCount = curCount + 1
        if errCode > 0 then
            if curCount == retryCount then
                d2.removeListener(cbId)
                promise:reject(errMsg)
            else
                if fileSize then
                    saveProgress = curProgress
                    d2.add(url, filePath, "", 0, 0, fileSize, true, false, true, 0, "1", cbId)
                else
                    d2.add(url, filePath, "", 0, 0, 0, false, true, false, 0x7FFFFFFF, "", cbId)
                end
                d2.commit()
            end
        else
            d2.removeListener(cbId)
            promise:resolve(filePath)
        end
    end)

    if fileSize then
        d2.addProgressListener(cbId, function (tag, progress)
            if fileTag == tag then
                print(string.format("Fish download filePath=%s, progress=%s, tag=%s", filePath, progress, tag))
                progressTable[tag] = progress
                local tprogress = 0.0
                for _,v in pairs(progressTable) do
                    tprogress = tprogress + v
                end
                curProgress = (1.0 - saveProgress) * (tprogress) + saveProgress
                self:onDownloadProgress(tag, filePath, curProgress)
            end
        end)
        d2.add(url, filePath, "", 0, 0, fileSize, true, false, true, 0, fileTag, cbId)
    else
        d2.add(url, filePath, "", 0, 0, 0, false, true, false, 0x7FFFFFFF, "", cbId)
    end
    d2.commit()
    return promise
end

function FishDownload:onDownloadSuccess( ... )
    print("Fish download success.")
    if self._resultCallback then
        self._resultCallback(true)
    end
end

function FishDownload:onDownloadFail( ... )
    print("Fish download fail.")
    if self._resultCallback then
        self._resultCallback(false)
    end
end

function FishDownload:onDownloadProgress( tag, filePath, progress )
    if tag == FishBundleTag and self._progressInfo then
        self._progressInfo[filePath].progress = math.max(self._progressInfo[filePath].progress, progress)
        local p = 0
        for _,v in pairs(self._progressInfo) do
            p = p + v.ratio * v.progress
        end
        print("Fish download progress = " .. p)
        if self._progressCallback then
            self._progressCallback(p)
        end
    end
end

return FishDownload

  �,  