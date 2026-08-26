local cjson = require("cjson")
local HotFixError = un.const.HotFixError

-- DownloadInfo
local DownloadInfo = un.instance()

function DownloadInfo:ctor(filePath)
    self._filePath = filePath
    self._info = {
        url = "",
        size = 0,
        blockCount = 0,
        downloadInfo = {}
    }
    self._isLoaded = false
end

function DownloadInfo:load()
    if self._isLoaded then
        return true
    end

    local fs = un.FileSystem
    local filePath = self._filePath
    if filePath and #filePath > 0 and fs.isFileExist(filePath) then
        local content = fs.readStringFromFile(filePath)
        if #content > 0 then
            local json, msg = un.utils.str2json(content)
            if json then
                self._info = json
                self._isLoaded = true
                return true
            end
        end
    end

    return false
end

function DownloadInfo:save()
    local filePath = self._filePath
    if filePath and #filePath > 0 then
        local content = un.utils.json2str(self._info)
        if content and #content > 0 then
            un.FileSystem.writeStringToFile(content, filePath)
        end
    end
end

function DownloadInfo:setDownloadInfo(downloadInfo)
    self._info["downloadInfo"] = downloadInfo
end

function DownloadInfo:setUrl(url)
    self._info["url"] = url
end

function DownloadInfo:setSize(size)
    self._info["size"] = size
end

function DownloadInfo:setBlockCount(count)
    self._info["blockCount"] = count
end

function DownloadInfo:getDownloadInfo()
    return self._info["downloadInfo"]
end

function DownloadInfo:getUrl()
    return self._info["url"]
end

function DownloadInfo:getSize()
    return self._info["size"]
end

function DownloadInfo:getBlockCount()
    return self._info["blockCount"]
end

-- ZipDownloader
local ZipDownloader = un.instance()

local d2 = un.downloader2
local MINI_BLOCK_SIZE = 5 * 1024 * 1024

function ZipDownloader:ctor(zipUrls, zipSize, workPath, priority, listener)
    self._zipUrls = zipUrls
    self._zipSize = zipSize
    self._workPath = workPath
    self._priority = priority
    self._listener = listener
    self._infoPath = workPath .. "info.manifest"
    self._info = DownloadInfo.new(self._infoPath)
    self._downloadStartTime = 0
    self._downloadTotalTime = 0
end

function ZipDownloader:start()
    local zipUrls = self._zipUrls
    local zipSize = self._zipSize
    local workPath = self._workPath
    local listener = self._listener
    local info = self._info
    local fs = un.FileSystem

    -- 检查下载信息
    local blockCount
    local downloadInfo
    if info:load() and info:getSize() == zipSize and table.has(zipUrls, info:getUrl()) then
        blockCount = info:getBlockCount()
        downloadInfo = info:getDownloadInfo()

        local isNeedSave = false
        for k,v in pairs(downloadInfo) do
            if v["success"] and not fs.isFileExist(v["filePath"]) then
                v["success"] = false
                isNeedSave = true
            end
        end

        if isNeedSave then
            info:save()
        end
    else
        -- 分快下载
        fs.removeDirectory(workPath)
        fs.createDirectory(workPath)

        print("ZipDownloader.start: use one to download 3")
        local threadCount = 1 --zipSize < MINI_BLOCK_SIZE and 1 or (d2.getThreadCount() * 2)
        local blockSize = math.floor(zipSize / threadCount)
        downloadInfo = {}
        if threadCount > 1 then -- 需要分块
            for i=1, threadCount - 1 do
                downloadInfo[tostring(i)] = {
                    beginPos = (i - 1) * blockSize,
                    endPos = i * blockSize - 1,
                    filePath = string.format("%szip_%d.data", workPath, i),
                    success = false
                }
            end
        end

        downloadInfo[tostring(threadCount)] = {
            beginPos = (threadCount - 1) * blockSize,
            endPos = zipSize - 1,
            filePath = string.format("%szip_%d.data", workPath, threadCount),
            success = false
        }

        blockCount = threadCount

        info:setUrl(zipUrls[1])
        info:setSize(zipSize)
        info:setBlockCount(blockCount)
        info:setDownloadInfo(downloadInfo)
        info:save()
    end

    -- 添加监听器
    local priority = self._priority
    local urlCount = #zipUrls
    local curUrlIndex = 1
    local retryCount = blockCount * 5
    local curUrl = zipUrls[curUrlIndex]
    local saveProgress = 0
    local curProgress = 0

    local totalCount = blockCount
    local successCount = 0
    local curCount = 0
    local errTable = {}
    local progressTable = {}
    local cbId
    cbId = d2.addListener(function (tag, costTime, errCode, errMsg)
        curCount = curCount + 1

        if errCode > 0 then
            errTable[tag] = errMsg
            print("ZipDownloader error")
            table.print(errTable)
        else
            downloadInfo[tag]["success"] = true
            info:save()
            successCount = successCount + 1
        end

        if curCount == totalCount then
            print(string.format("ZipDownloader finish. successCount: %s, totalCouont: %s", tostring(successCount), tostring(totalCount)))
            table.print(downloadInfo)
            if successCount < totalCount then
                if retryCount > 0 then
                    print("ZipDownloader start retry")
                    table.print(errTable)
                    retryCount = retryCount - 1
                    totalCount = 0
                    curCount = 0
                    successCount = 0
                    curUrlIndex = curUrlIndex % urlCount + 1
                    curUrl = zipUrls[curUrlIndex]
                    for k,v in pairs(errTable) do
                        local dinfo = downloadInfo[k]
                        d2.add(curUrl, dinfo.filePath, "", 0, dinfo.beginPos, dinfo.endPos, true, false, true, priority, k, cbId)
                        totalCount = totalCount + 1
                    end
                    errTable = {}
                    d2.commit()

                    saveProgress = curProgress
                else
                    d2.removeListener(cbId)
                    listener.onError(HotFixError.DOWNLOAD_FAILED, errMsg, errTable)
                end
            else
                self._downloadTotalTime = un.Device.getTime() - self._downloadStartTime
                d2.removeListener(cbId)
                self:_mergeFile(blockCount, downloadInfo)
                -- listener:onSuccess()
            end
        end
    end)

    d2.addProgressListener(cbId, function (tag, progress)
        print(string.format("zip download progress tag=%s, progress=%s", tag, progress))
        progressTable[tag] = progress
        local tprogress = 0.0
        for k,v in pairs(progressTable) do
            tprogress = tprogress + v
        end
        curProgress = (1.0 - saveProgress) * (tprogress / blockCount) + saveProgress
        listener.onProgress(curProgress)
    end)

    --[[
        void add(const std::string& url, const std::string& filePath, const std::string& md5,
		unsigned long fileSize, unsigned long beginPos, unsigned long endPos, 
        bool supportResume, bool needDetailTime, bool needProgress
        int priority, const std::string& tag, int cbId);
    ]]
    for k,v in pairs(downloadInfo) do
        if v["success"] then
            successCount = successCount + 1
        else
            d2.add(curUrl, v.filePath, "", 0, v.beginPos, v.endPos, true, false, true, priority, k, cbId)
        end
    end

    if successCount < blockCount then
        d2.commit()
        self._downloadStartTime = un.Device.getTime()
    else
        d2.removeListener(cbId)
        self:_mergeFile(blockCount, downloadInfo)
    end
end

function ZipDownloader:_mergeFile(blockCount, downloadInfo)
    local targetFile = self._workPath .. "final.zip"
    if blockCount == 1 then
        un.FileSystem.renameFile(downloadInfo["1"]["filePath"], targetFile)
        self._listener.onSuccess(targetFile, self._downloadTotalTime)

    else
        local files = {}
        for i = 1, blockCount do
            local dinfo = downloadInfo[tostring(i)]
            table.insert(files, dinfo.filePath)
        end

        local mergeFile = un.MergeFile.new()
        mergeFile:setListener(function (errorCode, errorMsg)
            if (errorCode > 0) then
                print(string.format("ZipDownloader._mergeFile: merge zip error. %s", errorMsg))
                self._listener.onError(HotFixError.MERGE_ZIP_FILE_FAILED, errorMsg)
            else
                local fs = un.FileSystem
                fs.removeFile(self._infoPath)
                for _,v in ipairs(files) do
                    fs.removeFile(v)
                end
                self._listener.onSuccess(targetFile, self._downloadTotalTime)
            end
        end)
        mergeFile:add(files, targetFile)
        mergeFile:removeWhenAllTaskComplete()
    end
end

un.hotfix.ZipDownloader = ZipDownloader
�$  