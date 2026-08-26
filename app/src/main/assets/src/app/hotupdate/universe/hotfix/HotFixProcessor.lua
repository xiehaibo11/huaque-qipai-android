local HotFixProcessor = un.instance()

local HotFixError = un.const.HotFixError

local Status = {
    None = 0,
    ManifestLoaded = 1,
    VersionFileDownloading = 2,
    ZipDownloading = 3,
    Unziping = 4,
    Downloading = 5,
    Merging = 6,
    End = 7,
    Error = 8

}
HotFixProcessor.Status = Status

function HotFixProcessor:ctor(key, rootPath, workPath, localManifestPath, prefix, downloadPriority, isFirst, isPreferZip)
    self._key = key
    self._rootPath = rootPath
    self._workPath = workPath
    self._localManifestPath = localManifestPath
    self._listener = un.ListenerHub.new()
    self._prefix = prefix
    self._downloadPriority = downloadPriority
    self._isFirst = isFirst
    self._isFullZipMode = false
    self._isHaveMissingFile = false
    self._useZip = false
    self._isPreferZip = isPreferZip
    self._zipRetryCount = 3

    self._versionManifest = nil
    self._projectManifest = nil
    self._downloadManifest = nil

    self._startTime = 0
    self._downloadVersionTime = 0
    self._downloadManifestTime = 0
    self._downloadFileTime = 0
    self._unzipTime = 0
    self._mergeTime = 0
    self._diffTime = 0
    self._status = Status.None
    self._errInfo = nil
    self._fixTypeInfo = {}
    self._totalFileCount = 0
    self._totalFileSize = 0

    self._extInfo = {
        api_url = "",
        manifest_url = ""
    }

    self._zipDiffList = nil
    self._downloadDiffList = nil
    self._downloadPath = workPath .. "download/"
    self._downloadFinishTagFile = workPath .. "download_finish.tag"
    self._zipWorkPath = workPath .. "zip/"
    self._unzipPath = self._zipWorkPath .. "decompress/"
end

function HotFixProcessor:getListener()
    return self._listener
end

function HotFixProcessor:isLoadLocalManifestSuccess()
    return self._localManifest and self._localManifest:isLoaded()
end

--[[
- 加载本地的 manifest
]]
function HotFixProcessor:loadManifest()
    if self._status ~= Status.None then
        if self._status == Status.Error then
            self:_fireError()
            return false
        else
            return true
        end
    end
    self._status = Status.ManifestLoaded

    -- 加载包体中的 project.manifest
    local localManifest = un.hotfix.Manifest.new(self._prefix .. "/" .. self._localManifestPath)
    if not localManifest:isLoaded() then
        self:_onError(HotFixError.LOAD_LOCAL_MANIFEST_ERROR)
        return false
    end

    self._localManifest = localManifest

    local workPath = self._workPath

    -- 加载热更文件夹中的 project.manifest
    local hotfixManifestPath = workPath .. "project.manifest"
    if not self._isFirst then
        local hotfixMainfest = un.hotfix.Manifest.new(hotfixManifestPath)
        if not hotfixMainfest:isLoaded() then
            un.FileSystem.removeDirectory(workPath)
            self:_onError(HotFixError.NEED_RESTART, workPath)
            return false
        end

        self._localManifest = hotfixMainfest
        return true
    else
        localManifest:setFileAndRootPath(hotfixManifestPath)
        localManifest:saveToFile(true)
        self._localManifest = localManifest
        return true
    end
end

function HotFixProcessor:start()
    if self._status ~= Status.ManifestLoaded then
        if self._fixTypeInfo then
            local fixTypeInfo = self._fixTypeInfo
            self._listener:onVersionDownload(self._key, fixTypeInfo[1], fixTypeInfo[2], fixTypeInfo[3], fixTypeInfo[4])
        end
        return
    end
    self._status = Status.VersionFileDownloading

    -- 下载 version.minifest
    local url = self._localManifest:getUpdateUrl()
    local count = #url
    if not url or count == 0 then
        self:_onError(HotFixError.NOT_FIND_VERSION_URL)
        return
    end

    self._isHaveMissingFile = self._localManifest:isHaveMissingFile()

    -- 拼接参数
    local params = {
        ip = un.Device.getIp(),
        device_code = "",--un.Device.getUUID(),
        os = un.Device.getOsName(),
        hardware = un.Device.getDeviceName(),
        res_status = self._isHaveMissingFile and "0" or "1"
    }
    local fullUrl = {}
    for i = 1, count do
        fullUrl[#fullUrl + 1] = un.url.generate(url[i], params)
    end
    un.utils.shuffle(fullUrl)

    self._startTime = un.Device.getTime()

    -- 下载 version.manifest
    local retryCount = count * 2
    local curCount = 0
    local versionPath = self._workPath .. "version.manifest"
    local d2 = un.downloader2
    local cbId
    cbId = d2.addListener(function (tag, costTime, errCode, errMsg)
        curCount = curCount + 1
        if errCode > 0 then
            if curCount == retryCount then
                self._downloadVersionTime = costTime
                self._extInfo.api_url = fullUrl[((curCount - 1) % count) + 1]
                d2.removeListener(cbId)
                self:_onError(HotFixError.DOWNLOAD_VERSION_FILE_FAILED, errMsg)
            else
                d2.add(fullUrl[(curCount % count) + 1], versionPath, "", 0, 0, 0, false, true, false, 0x7FFFFFFF, "", cbId)
                d2.commit()
            end
        else
            self._downloadVersionTime = costTime
            self._extInfo.api_url = fullUrl[((curCount - 1) % count) + 1]
            d2.removeListener(cbId)
            self:_onVersionDownload(versionPath)
        end
    end)
    d2.add(fullUrl[1], versionPath, "", 0, 0, 0, false, true, false, 0x7FFFFFFF, "", cbId)
    d2.commit()
end

function HotFixProcessor:_onVersionDownload(versionPath)
    local versionManifest = un.hotfix.Manifest.new(versionPath)
    if not versionManifest:isLoaded() then
        self:_onError(HotFixError.LOAD_VERSION_FILE_FAILED)
        return
    end


    self._versionManifest = versionManifest
    local msg = versionManifest:getTipMsg()
    local oldVersion = self._localManifest:getVersion()
    local newVersion = versionManifest:getVersion()

    local oldVersionTable = self:parseVersion(oldVersion)
    local newVersionTable = self:parseVersion(newVersion)

    if self:checkVersionUpdate(oldVersionTable, newVersionTable) then
        lobby = lobby or {}
		lobby._needUpdate = lobby._needUpdate  or {} 
		lobby._needUpdate[self._key] = true
    else
        lobby = lobby or {}
		lobby._needUpdate = lobby._needUpdate  or {} 
		lobby._needUpdate[self._key] = false
    end

    if lobby._needUpdate[self._key] and self._isHaveMissingFile or self._localManifest < versionManifest then
        print("HotFixProcessor. new version found.")
        local fixType = self._isHaveMissingFile and un.const.HotFixType.FORCE or versionManifest:getUpdateType()
        self._fixTypeInfo = {fixType, oldVersion, newVersion, msg}
        self._listener:onVersionDownload(self._key, fixType, oldVersion, newVersion, msg)
    else
        print("HotFixProcessor. aleady up to date")
        self._fixTypeInfo = {un.const.HotFixType.NOUPDATE, oldVersion, newVersion, msg}
        self:_report()
        self._listener:onVersionDownload(self._key, un.const.HotFixType.NOUPDATE, oldVersion, newVersion, msg)
    end
end

function HotFixProcessor:parseVersion(version)
    local versionTable = {}
    for v in string.gmatch(version, "[^.]+") do
        table.insert(versionTable, tonumber(v))
    end

    return versionTable
end

function HotFixProcessor:checkVersionUpdate(oldVersion , newVersion)
    if oldVersion[1] and newVersion[1] and oldVersion[1] < newVersion[1] then
        return true
    elseif oldVersion[2] and newVersion[2] and oldVersion[2] < newVersion[2] then
        return true
    elseif oldVersion[3] and newVersion[3] and oldVersion[3] < newVersion[3] then
        return true
    elseif oldVersion[4] and newVersion[4] and oldVersion[4] < newVersion[4] then
        return true
    end
    return false
end

function HotFixProcessor:update()
    if self._status ~= Status.VersionFileDownloading then
        if self._status < Status.Error then
            if self._status > Status.ZipDownloading then
                self._listener:onDownloadInfo(self._key, self._totalFileCount, self._totalFileSize)
                self._listener:onZipDownloadProgress(self._key, 1)
            end

            if self._status > Status.Unziping then
                self._listener:onUnzipProgress(self._key, 1)
            end

            if self._status > Status.Downloading then
                self._listener:onDownloadProgress(self._key, 1)
                self._listener:onDownloadSuccess(self._key, 0, 0)
            end
        end
        return
    end
    self._status = Status.ZipDownloading

    -- 删除下载完成标记，因为又开始了一次更新
    un.FileSystem.removeFile(self._downloadFinishTagFile)

    local canUseZip = not self._localManifest:isForbidZip()
    if not canUseZip then
        print("HotFixProcessor: %s forbid zip", self._key)
    end

    -- 差异包检查
    local zipList = self._versionManifest:getZipList()
    if canUseZip and zipList and un.MergeFile then -- compat old version
        local zipUrls  = zipList["url"]
        if zipUrls and #zipUrls > 0 then
            self._useZip = true
            -- 通知
            local fileCount, fileSize = 1, zipList["size"]
            self._totalFileCount = fileCount
            self._totalFileSize = fileSize
            print(string.format("HotFixProcessor: %s use zip", self._key))
            print(string.format("%s Total Download FileCount: %s, FileSize: %s", self._key, fileCount, fileSize))
            self._listener:onDownloadInfo(self._key, fileCount, fileSize)
            self:_startZipDownload()
            return
        end
    end

    -- 整包zip
    if canUseZip and self._isHaveMissingFile and self._localManifest:getFoundFileCount() < 8 then
        local fullZipList = self._versionManifest:getFullZipList()
        if fullZipList and un.MergeFile then -- compat old version
            local zipUrls = fullZipList["url"]
            if zipUrls and #zipUrls > 0 then
                self._useZip = true
                self._isFullZipMode = true
                -- 通知
                local fileCount, fileSize = 1, fullZipList["size"]
                self._totalFileCount = fileCount
                self._totalFileSize = fileSize
                print(string.format("HotFixProcessor: %s use full zip", self._key))
                print(string.format("%s Total Download FileCount: %s, FileSize: %s", self._key, fileCount, fileSize))
                self._listener:onDownloadInfo(self._key, fileCount, fileSize)
                self:_startZipDownload()
                return
            end
        end
    end

    self:_startGeneralDownload()
end

function HotFixProcessor:_startGeneralDownload(zipProjectManifest, diffList)
    if self._status ~= Status.Unziping then
        if self._status < Status.Downloading then
            self._listener:onZipDownloadProgress(self._key, 1)
            self._listener:onUnzipProgress(self._key, 1)
        end
        if self._status > Status.Downloading and self._status < Status.Error then
            self._listener:onDownloadProgress(self._key, 1)
            return
        end
        if self._status == Status.Downloading then return end
    end
    self._status = Status.Downloading

    if zipProjectManifest then
        self._projectManifest = zipProjectManifest
        self._downloadDiffList = diffList
        self:_figureOutDownloadFile()
        self:_generalDownload()
    else
        self:_downloadProjectManifest()
    end
end

function HotFixProcessor:_downloadProjectManifest()
    local url = self._versionManifest:getManifestUrl()
    local md5 = self._versionManifest:getProjectMd5()

    local count = #url
    if not url or count == 0 then
        self:_onError(HotFixError.NOT_FIND_PROJECT_MANIFEST_URL)
        return
    end

    local retryCount = count * 2
    local curCount = 0
    local projectTempPath = self._workPath .. "project.manifest.temp"
    local d2 = un.downloader2
    local cbId
    cbId = d2.addListener(function (tag, costTime, errCode, errMsg)
        curCount = curCount + 1
        if errCode > 0 then
            if curCount == retryCount then
                self._downloadManifestTime = costTime
                self._extInfo.manifest_url = url[((curCount - 1) % count) + 1]
                d2.removeListener(cbId)
                self:_onError(HotFixError.DOWNLOAD_PROJJECT_MANIFEST_FAILED, errMsg)
            else
                d2.add(url[(curCount % count) + 1], projectTempPath, md5, 0, 0, 0, false, false, false, 0x7FFFFFFF, "", cbId)
                d2.commit()
            end
        else
            d2.removeListener(cbId)
            self._downloadManifestTime = costTime
            self._extInfo.manifest_url = url[((curCount - 1) % count) + 1]
            self:_onProjectDownload(projectTempPath)
        end
    end)
    d2.add(url[1], projectTempPath, md5, 0, 0, 0, false, false, false, 0x7FFFFFFF, "", cbId)
    d2.commit()
end

function HotFixProcessor:_onProjectDownload(projectTempPath)
    local projectManifest = un.hotfix.Manifest.new(projectTempPath)
    if not projectManifest:isLoaded() then
        self:_onError(HotFixError.LOAD_PROJECT_MANIFEST_FAILED)
        return
    end

    self._projectManifest = projectManifest

    self:_figureOutDownloadFile()

    -- 通知
    local fileCount, fileSize = self._downloadDiffList:getTotalDownloadFileCountAndSize()
    self._totalFileCount = self._totalFileCount + fileCount
    self._totalFileSize = self._totalFileSize + fileSize
    print(string.format("%s Total Download FileCount: %s, FileSize: %s", self._key, fileCount, fileSize))
    if not self._useZip then
        self._listener:onDownloadInfo(self._key, fileCount, fileSize)
    end

    self:_generalDownload()
end

function HotFixProcessor:_figureOutDownloadFile()
    local startTime = un.Device.getTime()
    local diffList
    if not self._downloadDiffList then
        diffList = self._localManifest:genDiffList(self._projectManifest)
        self._downloadDiffList = diffList
    else
        diffList = self._downloadDiffList
    end

    local downloadManifest = un.hotfix.Manifest.new(self._workPath .. 'download.manifest', self._workPath .. 'download/')
    if downloadManifest:isLoaded() then
        downloadManifest:applyDiffList(diffList)
    end

    self._diffTime = self._diffTime + un.Device.getTime() - startTime
    self._downloadManifest = downloadManifest
end

function HotFixProcessor:_generalDownload()
    local listener = self._listener

    local diffList = self._downloadDiffList
    if diffList:getCount() == 0 then
        listener:onDownloadProgress(self._key, 1)
        listener:onDownloadSuccess(self._key, 0, 0)
        return
    end

    local fs = un.FileSystem
    local downloadPath = self._downloadPath
    fs.createDirectory(downloadPath)

    local url = self._projectManifest:getFileUrl()
    local urlCount = #url
    local curUrlIndex = 1
    local retryCount = urlCount * 5
    local curUrl = url[curUrlIndex]

    local totalSize = 0

    local totalNeedCount = 0
    local successCount = 0
    local totalCount = 0
    local curCount = 0
    local errTable = {}
    local startTime = un.Device.getTime()

    local d2 = un.downloader2
    local cbId
    cbId = d2.addListener(function (tag, costTime, errCode, errMsg)
        curCount = curCount + 1

        if errCode > 0 then
            errTable[tag] = errMsg
        else
            local file = self._projectManifest:getFile(tag)
            self._downloadManifest:addFile(tag, file)
            self._downloadManifest:saveToFile()

            diffList:remove(tag)
            successCount = successCount + 1
            listener:onDownloadProgress(self._key, successCount / totalNeedCount)
        end

        if curCount == totalCount then
            if successCount == totalNeedCount then
                print("HotFixProcessor._startDownload. download success " .. self._key)
                self._downloadFileTime = un.Device.getTime() - startTime
                self._downloadManifest:saveToFile(true)
                d2.removeListener(cbId)
                listener:onDownloadProgress(self._key, 1)
                listener:onDownloadSuccess(self._key, totalNeedCount, totalSize)
            else
                if retryCount > 0 then
                    print("HotFixProcessor._startDownload. retry download " .. tostring(retryCount))
                    retryCount = retryCount - 1
                    totalCount = 0
                    curCount = 0
                    curUrlIndex = curUrlIndex % urlCount + 1
                    curUrl = url[curUrlIndex]
                    local suffix = "?rand=" .. os.clock()
                    for k,v in pairs(errTable) do
                        local info = diffList:get(k)
                        d2.add(curUrl .. info.name .. suffix, downloadPath .. k, info.zipMd5, 0, 0, 0, false, false, false, self._downloadPriority, k, cbId)
                        totalCount = totalCount + 1
                    end
                    errTable = {}
                    d2.commit()
                else
                    d2.removeListener(cbId)
                    self._downloadManifest:saveToFile(true)
                    self._downloadFileTime = un.Device.getTime() - startTime
                    self:_onError(HotFixError.DOWNLOAD_FAILED, errMsg, errTable)
                end
            end
        end
    end)

    local suffix = "?rand=" .. os.clock()
    diffList:foreach(function (file, info)
        if info.diffType ~= un.const.FileDiffType.DELETE then
            d2.add(curUrl .. info.name .. suffix, downloadPath .. file, info.zipMd5, 0, 0, 0, false, false, false, self._downloadPriority, file, cbId)
            totalNeedCount = totalNeedCount + 1
            totalSize = totalSize + info.size
        end
    end)
    totalCount = totalNeedCount

    if totalNeedCount == 0 then
        d2.removeListener(cbId)
        listener:onDownloadProgress(self._key, 1)
        listener:onDownloadSuccess(self._key, 0, 0)
    else
        d2.commit()
    end
end

function HotFixProcessor:startDecompress()
    if self._status ~= Status.Downloading then
        if self._status > Status.Merging and self._status < Status.Error then
            self._listener:onDecompressSuccess(self._key)
        end
        return
    end
    self._status = Status.Merging

    self:_startMerge()
    -- local listener = self._listener
    -- local fileInter = un.FileInterface
    -- local writePath = fileInter.getWritablePath()
    -- local downloadPath = self._downloadPath

    -- local totalCount = 0
    -- local successCount = 0
    -- local curCount = 0
    -- local isSuccess = true
    -- local errTable = {}

    -- local decompressor = un.Decompressor.new()
    -- decompressor:setListener(function (tag, errCode, errMsg) 
    --     curCount = curCount + 1
    --     if errCode > 0 then
    --         errTable[tag] = errMsg
    --         isSuccess = false
    --         print(errMsg)
    --     else
    --         local file = self._downloadManifest:getFile(tag)
    --         self._localManifest:addFile(tag, file)
    --         self._localManifest:saveToFile()

    --         successCount = successCount + 1
    --         listener:onDecompressProgress(self._key, successCount / totalCount)
    --     end

    --     self._downloadManifest:removeFile(tag)
    --     self._downloadManifest:saveToFile()

    --     fileInter.removeFile(downloadPath .. tag)

    --     if curCount == totalCount then
    --         self._localManifest:saveToFile(true)
    --         self._downloadManifest:saveToFile(true)
    --         if isSuccess then
    --             print("decompress success")
    --             fileInter.removeDirectory(downloadPath)
    --             self:_startDelete()
    --             listener:onDecompressProgress(self._key, 1)
    --             listener:onDecompressSuccess(self._key)
    --         else
    --             listener:onError(self._key, HotFixError.DECOMPRESS_ERROR, errMsg, errTable)
    --         end
    --     end
    -- end)

    -- self._downloadManifest:foreachFileList(function (file, info) 
    --     decompressor:add(downloadPath .. file, writePath .. file, file)
    --     totalCount = totalCount + 1
    -- end)
    -- decompressor:removeWhenAllTaskComplete()

    -- if totalCount == 0 then
    --     fileInter.removeDirectory(downloadPath)
    --     self:_startDelete()
    --     listener:onDecompressProgress(self._key, 1)
    --     listener:onDecompressSuccess(self._key)
    -- end
end

function HotFixProcessor:_startZipDownload()
    local managerListener = self._listener
    local zipList = self._isFullZipMode and self._versionManifest:getFullZipList() or self._versionManifest:getZipList()

    un.FileSystem.removeDirectory(self._unzipPath)

    -- (zipUrls, zipSize, workPath, listener)
    local listener = {}

    function listener.onProgress(progress)
        managerListener:onZipDownloadProgress(self._key, progress)
    end

    function listener.onSuccess(zipFile, downloadTime)
        self._downloadFileTime = downloadTime
        self:_startUnZip(zipFile)
    end

    function listener.onError(error, msg, data)
        print(string.format("HotFixProcessor._startZipDownload: %s zip download error %s", self._key, msg))
        self:_startGeneralDownload()
    end

    local zipDownloader = un.hotfix.ZipDownloader.new(zipList["url"], zipList["size"], self._zipWorkPath, self._downloadPriority, listener)
    zipDownloader:start()
    -- listener.onError("", "zip download test error")
end

function HotFixProcessor:_startUnZip(zipFile)
    self._status = Status.Unziping
    local listener = self._listener
    local startTime = un.Device.getTime()
    local unzip = un.Unzipper.new()
    unzip:setListener(function (tag, errCode, errMsg, progress)
        if progress >= 0.0 then
            listener:onUnzipProgress(self._key, progress)
        else
            self._unzipTime = un.Device.getTime() - startTime;
            if errCode > 0 then
                print(string.format("HotFixProcessor._startUnZip: %s unzip error %s", self._key, errMsg))
                if self._isPreferZip and self._zipRetryCount > 0 then
                    print(string.format("HotFixProcessor._startUnZip: %s start zip retry %d", self._key, self._zipRetryCount))
                    self._zipRetryCount = self._zipRetryCount - 1
                    un.FileSystem.removeDirectory(self._zipWorkPath)
                    self:_startZipDownload()
                else
                    listener:onUnzipProgress(self._key, 1)
                    self:_startGeneralDownload()
                end
            else
                un.FileSystem.removeFile(zipFile)
                listener:onUnzipProgress(self._key, 1)
                self:_checkZipFile()
            end
        end
    end)
    unzip:add(zipFile, self._unzipPath, "")
    unzip:removeWhenAllTaskComplete()
end

function HotFixProcessor:_checkZipFile()
    local manifestPath = self._unzipPath .. "project.manifest"
    local projectManifest = un.hotfix.Manifest.new(manifestPath)
    if not projectManifest:isLoaded() then
        self:_startGeneralDownload()
        return
    end

    local fs = un.FileSystem
    local unzipPath = self._unzipPath
    local startTime = un.Device.getTime()

    local downloadDiffList = un.hotfix.DiffList.new()
    local zipDiffList = self._localManifest:genDiffList(projectManifest)
    zipDiffList:foreach(function (file, info)
        if info.diffType ~= un.const.FileDiffType.DELETE then
            if not fs.isFileExist(unzipPath .. file) then
                downloadDiffList:add(file, info["name"], info["zipMd5"], info["size"], info["diffType"])
            end
        end
    end)
    downloadDiffList:foreach(function (file, info)
        zipDiffList:remove(file)
    end)
    self._diffTime = self._diffTime + un.Device.getTime() - startTime

    self._zipDiffList = zipDiffList
    self:_startGeneralDownload(projectManifest, downloadDiffList)
end

function HotFixProcessor:_startMerge()
    local listener = self._listener
    local fs = un.FileSystem
    local rootPath = self._rootPath
    local unzipPath = self._unzipPath
    local downloadPath = self._downloadPath
    local localManifest = self._localManifest
    local projectManifest = self._projectManifest

    local success = true
    local errFile = ""
    local startTime = un.Device.getTime()

    if self._useZip and self._zipDiffList then
        self._zipDiffList:foreach(function (file, info)
            if success and info.diffType ~= un.const.FileDiffType.DELETE then
                if fs.renameFile(unzipPath .. file, rootPath .. file) then
                    local fileInfo = projectManifest:getFile(file)
                    localManifest:addFile(file, fileInfo)
                    localManifest:saveToFile()
                else
                    errFile = file
                    success = false
                end
            end
        end)
    end

    if self._downloadManifest then
        self._downloadManifest:foreachFileList(function (file, info)
            if success then
                if fs.renameFile(downloadPath .. file, rootPath .. file) then
                    local fileInfo = self._downloadManifest:getFile(file)
                    self._localManifest:addFile(file, fileInfo)
                    self._localManifest:saveToFile()
                else
                    errFile = file
                    success = false
                end

                self._downloadManifest:removeFile(file)
                self._downloadManifest:saveToFile()
            end
        end)

        self._downloadManifest:saveToFile(true)
    end

    self:_startDelete()
    self._mergeTime = un.Device.getTime() - startTime

    if success then
        fs.removeDirectory(downloadPath)
        fs.removeDirectory(self._zipWorkPath)
        self._status = Status.End
        self:_report()
        listener:onDecompressSuccess(self._key)
    else
        self:_onError(HotFixError.MOVE_FILE_ERROR, errFile)
    end
end

function HotFixProcessor:_startDelete()
    local fs = un.FileSystem
    local rootPath = self._rootPath

    local errList = {}

    if self._useZip and self._zipDiffList then
        self._zipDiffList:foreach(function (file, info)
            if info.diffType == un.const.FileDiffType.DELETE then
                local fullpath = rootPath .. file
                if not fs.isFileExist(fullpath) or fs.removeFile(fullpath) then
                    self._localManifest:removeFile(file)
                    self._localManifest:saveToFile()
                else
                    errList[#errList + 1] = file
                end
            end
        end)
    end

    if self._downloadDiffList then
        self._downloadDiffList:foreach(function (file, info)
            if info.diffType == un.const.FileDiffType.DELETE then
                local fullpath = rootPath .. file
                if not fs.isFileExist(fullpath) or fs.removeFile(fullpath) then
                    self._localManifest:removeFile(file)
                    self._localManifest:saveToFile()
                else
                    errList[#errList + 1] = file
                end
            end
        end)
    end

    if #errList > 0 then
        print("delete error " .. self._key)
        self._localManifest:saveToFile(true)
    else
        print("delete success" .. self._key)
        self:_updateLocalManifest()
    end
end

function HotFixProcessor:_updateLocalManifest()
    -- local version = self._projectManifest:getVersion()
    -- local updateUrl = self._projectManifest:getUpdateUrl()
    -- local monitorUrl = self._projectManifest:getMonitorUrl()
    -- self._localManifest:setVersion(version)
    -- self._localManifest:setUpdateUrl(updateUrl)
    -- self._localManifest:setMonitorUrl(monitorUrl)
    local json = self._projectManifest:getJson()
    self._localManifest:setJson(json)
    self._localManifest:saveToFile(true)
end

function HotFixProcessor:getReportInfo()
    local localManifest = self._localManifest
    return {
        env = localManifest:getEnv(),
        appid = localManifest:getAppId(),
        channel = localManifest:getChannel(),
        engine_ver = localManifest:getEngineVer()
    }
end

function HotFixProcessor:getMonitorUrl()
    return self._localManifest:getMonitorUrl()
end

function HotFixProcessor:getCostTime()
    local totalTime = (self._startTime == 0) and 0 or (un.Device.getTime() - self._startTime)
    local info = {
        total = totalTime,
        api = self._downloadVersionTime,
        cdn = self._downloadFileTime,
        manifest = tostring(self._downloadManifestTime)
    }

    return un.url.toJson(info)
end

function HotFixProcessor:getExtInfo()
    local info = self._extInfo
    info.api_url = un.url.rmtail(info.api_url)
    info.manifest_url = un.url.rmtail(info.manifest_url)
    info.unzip_time = self._unzipTime
    info.merge_time = self._mergeTime
    info.diff_time = self._diffTime
    return un.url.toJson(info)
end

function HotFixProcessor:_onError(error, msg, data)
    self._status = Status.Error
    self._errInfo = {error, msg, data}
    self:_report()
    self._listener:onError(self._key, error, msg, data)
end

function HotFixProcessor:_fireError()
    if self._errInfo then
        local errInfo = self._errInfo
        self._listener:onError(self._key, errInfo[1], errInfo[2], errInfo[3])
    end
end

function HotFixProcessor:saveDownloadFinishTag()
    un.FileSystem.writeStringToFile("2333333", self._downloadFinishTagFile)
end

function HotFixProcessor:_report()
    if not self._localManifest or not self._localManifest:isLoaded() then
        print("HotFixProcessor._report: not load local manifest")
        return
    end

    local isSuccess = false
    local errInfo = self._errInfo
    if not errInfo then
        errInfo = { un.const.HotFixError.SUCCESS }
        isSuccess = true
    end

    -- NEED_RESTART do not report
    if errInfo[1] == un.const.HotFixError.NEED_RESTART then
        return
    end

    local info = self:getReportInfo()
    info['type'] = "1"
    info['ip'] = un.Device.getIp()
    info['device_code'] = ""--un.Device.getUUID()
    info['os'] = un.Device.getOsName()
    info['hardware'] = un.Device.getDeviceName()
    info['net_env'] = un.Device.getNetEnvName()
    if un.Device.getPackageName then
        info['pack_name'] = string.format("%s#%s", un.Device.getPackageName(), un.Device.getAppVersion())
    else
        info['pack_name'] = string.format("%s#%s", "", un.Device.getAppVersion())
    end
    info["sdk_ver"] = un.getVersion()
    info['time'] = tostring(os.time())
    info['status'] = isSuccess and "1" or "0"
    info['err_code'] = tostring(errInfo[1].code)

    if self._fixTypeInfo and isSuccess and self._fixTypeInfo[1] ~= un.const.HotFixType.NOUPDATE then
        info['before_version'] = self._fixTypeInfo[2]
        info['after_version'] = self._fixTypeInfo[3]
    else
        info['before_version'] = self._localManifest:getVersion()
        info['after_version'] = info['before_version']
    end

    info["file_count"] = self._totalFileCount
    info["file_size"] = self._totalFileSize

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
    info['cost_time'] = self:getCostTime()
    info['ext'] = self:getExtInfo()

    -- 签名
    local sign = un.url.sign(info)
    info['sign'] = sign

    local content = un.url.toJson(info)
    local monitorUrl = self._localManifest:getMonitorUrl()

    local retryCount = 3
    local http = un.Http.new()
    http:setListener(function (tag, tContent, errCode, errMsg)
        if errCode > 0 then
            if retryCount > 0 then
                retryCount = retryCount - 1
                http:add(monitorUrl, content, "")
            else
                print(string.format("HotFixProcessor._report: %s error. %s", self._key, errMsg))
            end
        end
    end)
    http:add(monitorUrl, content, "")
    http:removeWhenAllTaskComplete()
    print(string.format("HotFixProcessor._report: %s content = %s", self._key, content))
end

un.hotfix.HotFixProcessor = HotFixProcessor

��  