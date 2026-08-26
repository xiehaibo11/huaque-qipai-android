-- 资源检查模块
local ResChecker = {}

local RELINK_LIMIT_TIMES = 5   --重连最高上限

-- 外部接口
-- @param isForce   是否强制清理资源
-- @param pathKey   清理资源的目录
function ResChecker.start(isForce, isSelfCheck)
	require("app.Launcher"):getInstance():setSelfLauncher(isSelfCheck)
    -- 这里可以做一些启动界面的初始化等工作
	ResChecker:_initData()
    ResChecker._ensureRes(isForce)
end

-- 初始化数据
function ResChecker:_initData()
    self._scene = nil
	self._loader = nil
	self._relinkTimes = 0
	self._needUpdate = false 
    self._oldVersion = {}
    self._newVersion = {}
	self._lobbyHotUpdata = {}
	self._gamesHotUpdata = {}
end

---------------------------------------------------------------------------
--    资源检查功能
---------------------------------------------------------------------------

-- 资源检查监听器
local ResEnsureListener = {}

-- 结果通知函数
-- @param isFirst   表示进行了一次资源清理工作
function ResEnsureListener.onFinish(isFirst, Key)
	if Key ~= "Lobby" then --如果不是大厅热更，即正在检测子游戏是否需要热更（不做热更界面显示）
		return
	end 
    print("ResEnsureListener.onSuccess: isFirst = ", isFirst)
    -- 热更的异常捕捉
    local ok, msg = pcall(function ()
        -- 在此添加初始化代码
		--显示热更新界面
		ResChecker._lobbyHotUpdata = require("app.hotupdate.lobby.LobbyHotUpdateData")
		ResChecker._scene = require(ResChecker._lobbyHotUpdata.HotUpdateScenePath)
		if not require("app.Launcher"):getInstance():getSelfLauncher() then
			ResChecker._scene:start()
		end
		ResChecker._loader = require(ResChecker._lobbyHotUpdata.HotUpdateLoaderPath)
        -- 开始热更
        ResChecker._startHotFix(isFirst)
    end)

    if not ok then
        print("ResEnsureListener_error " .. tostring(msg))
        -- 热更中出现异常，且现在不是清理资源后进行的第一次热更，则重新开始清理
        if not isFirst then
            ResChecker.start(true) -- 使用强制清理
        end
    end
end

-- 检查资源函数
-- @param isForce   是否强制清理资源，如果为真，则会删除本地的所有文件
function ResChecker._ensureRes(isForce)
    print("ResChecker.ensureRes isForce: ", isForce)
    -- Lobby 是热更模块的 key
    local ResEnsure = require("app.hotupdate.lobby.ResEnsure")
    ResEnsure.start("Lobby", isForce, ResEnsureListener)
end

---------------------------------------------------------------------------
--    热更功能
---------------------------------------------------------------------------

-- 热更监听器
local hotfixListener = {}

-- 选择热更类型通知函数
-- @param key           热更失败的模块名
-- @param hotfixType    更新类型
-- @param oldVersion    老版本号
-- @param newVersion    更新版本号
-- @param msg           更新消息
function hotfixListener:onChooseHotFixType(key, hotfixType, oldVersion, newVersion, msg)
	print("更新宝返回信息："..msg.."  hotfixType:"..hotfixType)
--	hotfixType = un.const.HotFixType.SILENT
	if key ~= "Lobby" then --如果不是大厅热更，即正在检测子游戏是否需要热更（不做热更界面显示）
		self._manager:destroy()    --销毁检测子游戏是否需要热更的热更对象
		return
	end 
	-- 自我更新
	if require("app.Launcher"):getInstance():getSelfLauncher() then
		if hotfixType == un.const.HotFixType.FORCE then
			local func = function()
				cc.Director:getInstance():endToLua()
			end
			XH.TipTool.showTip({
				type = XH.TIP_LAYER_TYPE.OK,
				funcOK = func,
				funcClose = func,
				funcCancel = func
			}, "有新版本可用，请先退出应用后再安装更新")
		else
			local func = function()
				cc.Director:getInstance():popScene()
			end
			XH.TipTool.showTip({
				type = XH.TIP_LAYER_TYPE.OK,
				funcOK = func,
				funcClose = func,
				funcCancel = func
			}, "恭喜，您的游戏已是最新版本！")
		end
		return
	end
	--------------------
    if hotfixType == un.const.HotFixType.FORCE then -- 强制更新
        print("force")
		cc.UserDefault:getInstance():setBoolForKey("KW_DATA_NEED_FORCE_UPDATE"..key, true)
		ResChecker._needUpdate = true
		ResChecker._scene:needHotUpdate(key,  oldVersion, newVersion)
        self._manager:doUpdate(hotfixType, true)
    elseif hotfixType == un.const.HotFixType.NORMAL then -- 普通更新
        print("choose")
		cc.UserDefault:getInstance():setBoolForKey("KW_DATA_NEED_FORCE_UPDATE"..key, false)
		ResChecker._needUpdate = true
		ResChecker._scene:needHotUpdate(key,  oldVersion, newVersion)
        self._manager:doUpdate(hotfixType, true)
    elseif hotfixType == un.const.HotFixType.SILENT then -- 静默更新
        print("slient")
		ResChecker._needUpdate = false
        self._manager:doUpdate(hotfixType, false)
		cc.UserDefault:getInstance():setBoolForKey("KW_DATA_NEED_FORCE_UPDATE"..key, false)
		print("slientslient")
		ResChecker._scene:showProgress(100)
		ResChecker._loader.load()
		ResChecker._scene.isHotUpdate = false
		ResChecker._scene:hotUpdateSuccess() 
		ResChecker._isGameNeedHotUpdate()
    else -- 无需更新
		ResChecker._needUpdate = false
        print("noupdate")
        -- 在此做启动游戏的相关处理
		ResChecker._scene:showProgress(100)
		ResChecker._loader.load()
		ResChecker._scene.isHotUpdate = false
		ResChecker._scene:hotUpdateSuccess() 
		ResChecker._isGameNeedHotUpdate()
		--加载大厅当前版本
		-- local writePath = un.FileSystem.getWritePath()
		-- local rootPath = writePath .. un.const.HotFixPath	
		-- local workPath = rootPath .. un.const.HotfixSubPath .. "/" .. key .. "/"
		-- local assetsManagerEx
		-- -- add by louis for android update 2020/1/11
		-- local targetPlatform = cc.Application:getInstance():getTargetPlatform()
		-- if cc.PLATFORM_OS_ANDROID == targetPlatform then
		-- 	assetsManagerEx = cc.AssetsManagerEx:create("GameHotUpdate3/"..ResChecker._lobbyHotUpdata.HotUpdateList[key], rootPath)
		-- else
		-- 	assetsManagerEx = cc.AssetsManagerEx:create("GameHotUpdate3/"..ResChecker._lobbyHotUpdata.HotUpdateList[key], rootPath, workPath)
		-- end
		-- local localManifest = assetsManagerEx:getLocalManifest()
		-- if localManifest then
		-- 	lobby = lobby or {}
		-- 	lobby.Version =  localManifest:getVersion()
		-- 	print("Lobby.Version:"..lobby.Version)
        --     cc.UserDefault:getInstance():setStringForKey("Lobby_oldVersion_", "")
        --     cc.UserDefault:getInstance():setStringForKey("Lobby_newVersion_", lobby.Version or "error")
        --     cc.UserDefault:getInstance():setIntegerForKey("Lobby_hotUpdateState_", 2)
		-- end
    end
end

-- 热更成功通知函数
function hotfixListener:onSuccess(key)
    -- 在此做热更成功的相关处理，如 Reload，启动游戏等
    if ResChecker._needUpdate == true then
        ResChecker._loader.reload()
    else
        ResChecker._loader.load()
    end
	lobby = lobby or {}
	lobby._needUpdate = lobby._needUpdate  or {} 
	lobby._needUpdate[key] = false
	ResChecker._scene:hotUpdateSuccess() 
	ResChecker._isGameNeedHotUpdate()
	--加载大厅当前版本
	-- local writePath = un.FileSystem.getWritePath()
	-- local rootPath = writePath .. un.const.HotFixPath	
	-- local workPath = rootPath .. un.const.HotfixSubPath .. "/" .. key .. "/"
	-- local targetPlatform = cc.Application:getInstance():getTargetPlatform()
	-- -- add by louis for android update 2020/1/11
	-- local assetsManagerEx
	-- if cc.PLATFORM_OS_ANDROID == targetPlatform then
	-- 	assetsManagerEx = cc.AssetsManagerEx:create("GameHotUpdate3/"..ResChecker._lobbyHotUpdata.HotUpdateList[key], rootPath)
	-- else
	-- 	assetsManagerEx = cc.AssetsManagerEx:create("GameHotUpdate3/"..ResChecker._lobbyHotUpdata.HotUpdateList[key], rootPath, workPath)
	-- end
	-- local localManifest = assetsManagerEx:getLocalManifest()
	-- if localManifest then
	-- 	lobby = lobby or {}
	-- 	lobby.Version =  localManifest:getVersion()
	-- 	print("lobby.Version:"..lobby.Version)
	-- end
end

-- 热更失败通知函数
-- @param key       热更失败的模块名
-- @param error     错误
-- @param msg       错误消息
-- @param data      错误数据
function hotfixListener:onFailed(key, error, msg, data)
	print("onFailed_error_" .. error.code)
    -- NEED_RESTART 错误需要做特殊处理，遇到此错误应该尝试一下强制清理资源
    if error == un.const.HotFixError.NEED_RESTART then
        -- 从头开始，再次开始必须使用强制清理资源(热更大厅时出错强制清理所有资源)
		if key == "Lobby" then
			ResChecker.start(true)
		else
			ResChecker._isGameNeedHotUpdate(true)
		end
	elseif error == un.const.HotFixError.DOWNLOAD_VERSION_FILE_FAILED then
		ResChecker._scene:hotUpdateFaile("加载游戏失败，请检查网络后重启游戏", true, key)
    else
		if key ~= "Lobby" then --如果是子游戏下载版本失败，则跳过
			return
		end 
        -- 热更失败，做相关处理
		ResChecker._relinkTimes = ResChecker._relinkTimes + 1
		if ResChecker._relinkTimes < RELINK_LIMIT_TIMES and cc.UserDefault:getInstance():getBoolForKey("KW_DATA_NEED_FORCE_UPDATE"..key) then
			ResChecker._scene:hotUpdateFaile("热更新失败正在重试中", true, key)
			print("热更新失败正在重试中次数："..ResChecker._relinkTimes)
			ResChecker._startHotFix()
		elseif cc.UserDefault:getInstance():getBoolForKey("KW_DATA_NEED_FORCE_UPDATE"..key) then
			ResChecker._scene:hotUpdateFaile("加载游戏失败，请检查网络后重启游戏", true, key)
		else
			ResChecker._scene:showProgress(100)
			ResChecker._loader.load()
			ResChecker._scene.isHotUpdate = false
			ResChecker._scene:hotUpdateSuccess() 
			ResChecker._isGameNeedHotUpdate()
		end
	end
end

-- 热更进度通知函数
-- @param stage     当前进行的是哪一步
-- @param progress  当前的进度
function hotfixListener:onProgress(stage, progress)
    -- 进度条显示的是下载进度
    if stage == un.const.HotFixStage.DOWNLOAD then
        -- 这里可以更新进度条
		ResChecker._scene:showProgress(progress * 100)
    end
end

-- 启动热更函数
-- @param isFirst   是否是清理资源后第一次热更
function ResChecker._startHotFix(isFirst)
    -- isFirst 默认值为 false
    isFirst = isFirst or false
	ResChecker._gamesHotUpdata = require("app.hotupdate.games.GameHotUpdateData")	
	--先合并以前静默热更下来的文件
	un.hotfix.deferMerge.start("Lobby")
	--un.hotfix.deferMerge.start("GameCommon")
	for key, _ in pairs(ResChecker._gamesHotUpdata.HotUpdateList) do
		un.hotfix.deferMerge.start(key)
	end

    -- 热更信息，可以放在这里，也可以像老热更一样放在一个单独的文件里面
    local hotfixData = {
        HotUpdateList = ResChecker._lobbyHotUpdata.HotUpdateList,
    }

    -- HotFixManager 是热更对外提供的接口， "HotUpdate" 表示manifest的路径
    -- 将这个值拼在 project.manifest 前面，如 HotUpdate/Lobby/project.manifest
    local manager = un.hotfix.HotFixManager.new(hotfixListener, "GameHotUpdate3", isFirst, true, 0)
    hotfixListener._manager = manager
    -- 调用 start 函数启动热更
    manager:start(hotfixData)
end

-- 大厅热更完成，检测子游戏是否需要热更函数
-- @param isFirst   是否是清理资源后第一次热更
function ResChecker._isGameNeedHotUpdate(isFirst)
	-- isFirst 默认值为 false
    isFirst = isFirst or false
	ResChecker._gamesHotUpdata = require("app.hotupdate.games.GameHotUpdateData")	
    -- 热更信息，可以放在这里
	local hotfixData = {
        HotUpdateList = ResChecker._gamesHotUpdata.HotUpdateList,
    }
	-- 暂时忽略GameCommon
	-- hotfixData.HotUpdateList["GameCommon"] = ResChecker._gamesHotUpdata.HotUpdateCommon["GameCommon"]
	-- 最后一个参数为热更新优先级
    local managerGame = un.hotfix.HotFixManager.new(hotfixListener, "GameHotUpdate3", isFirst, true, 1)
    hotfixListener._manager = managerGame
    -- 调用 start 函数启动热更
	managerGame:start(hotfixData)
end

return ResChecker �0  