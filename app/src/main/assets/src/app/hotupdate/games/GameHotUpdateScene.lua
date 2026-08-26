local KW_PROGRESS           = "Progress" 
local KW_PROGRESS_TEXT      = "ProgressText"
local KW_LABEL_TIPS         = "KW_LABEL_TIPS"
local KW_LABEL_TIPS_GUIDE  = "KW_LABEL_TIPS_GUIDE"

local HotUpdateScene = class("HotUpdateScene")

lobby = lobby or {}
HotUpdateScene._hotUpdateCSBNode = nil
HotUpdateScene._UpdateManager = nil

local tmpProgress = 0
local curProgress = 0
local fastProgress = 1
HotUpdateScene._progressScheduleID = nil
HotUpdateScene.isHotUpdate = false

function HotUpdateScene:ctor()
    self._gameID = 0
    self._gameName = ""
    self._subGameName = ""
end

function HotUpdateScene:start(gameID,gameName)
    if gameID == nil or gameID == 0 then
        return
    end
    if gameName == nil or gameName == "" then
        return
    end
    self._gameID = gameID
    self._gameName = gameName
    
    local GameSubCfg = require("app.Config.GameSub").GameSubByConfID
    if GameSubCfg[self._gameID] then
        self._subGameName = GameSubCfg[self._gameID].GameName 
    end
    --外省渠道包的热更新加载条下面更改名字
    if XH.ChannelTool.checkIsWaishengChannel() then
        if self._gameID == 30297 or self._gameID == 30067 or self._gameID == 30313 then 
            self._subGameName = "双扣"
        elseif self._gameID == 30308 then 
            self._subGameName = "跑得快"        
        end
    end
    
	HotUpdateScene._progressScheduleID = nil
	tmpProgress = 0
	curProgress = 0
	fastProgress = 1
    XH.TipTool.resetLoading()
    self._updateLayer = require("app.hotupdate.games.GameHotUpdateLayer").new({gameID = gameID}):showSelf()
    self._viewStartTime = os.time()
end

function HotUpdateScene:needHotUpdate(tag, oldVersion, newVersion)
    print("游戏TAG = ".. tag .." oldVersion = " .. oldVersion .. " newVersion = " .. newVersion)
        
    local runningScene = display.getRunningScene()
    local tmpLayer = runningScene:getChildByName("GAME_HOTUPDATE_LAYER")
    if not tmpLayer then
        return
    end
    
    cc.UserDefault:getInstance():setStringForKey(tag .. "_oldVersion_", oldVersion or "error")
    cc.UserDefault:getInstance():setStringForKey(tag .. "_newVersion_",newVersion or "error")
    XH.UITool.setText(tmpLayer,KW_LABEL_TIPS, self._subGameName.."正在加载中...")
    if XH.playerData:getIsNewUserGuide() then
        XH.UITool.setVisible(tmpLayer,KW_LABEL_TIPS_GUIDE, true)
        self._newVersion = newVersion
    end
end

function HotUpdateScene:closeUpdateLayer()
    if self._updateLayer then
        self._updateLayer:close()
    end
end

function HotUpdateScene:hotUpdateSuccess(tag)
    if HotUpdateScene.isHotUpdate then return end
    cc.UserDefault:getInstance():setIntegerForKey(tostring(self._gameName) .. "_hotUpdateState_", 0)
    self._viewCloseTime = os.time()
    self:closeUpdateLayer()
    self:recordViewData()
    if lobby.gameHotUpdateSuccessCallBack~=nil then
        --避免gameHotUpdateSuccessCallBack不为空,造成递归回调的情况
        lobby.gameHotUpdateSuccessCallBackTmp = lobby.gameHotUpdateSuccessCallBack
        lobby.gameHotUpdateSuccessCallBack = nil
        lobby.gameHotUpdateSuccessCallBackTmp()
        return
    end
end

function HotUpdateScene:hotUpdateFaile(message,gameKey)
	if cc.UserDefault:getInstance():getBoolForKey("KW_DATA_NEED_FORCE_UPDATE"..gameKey) then
        self:closeUpdateLayer()
		if lobby and lobby.gameHotUpdateSuccessCallBack then
			lobby.gameHotUpdateSuccessCallBack = nil
        end
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, "热更新失败,请稍后再试!")
        cc.UserDefault:getInstance():setIntegerForKey(gameKey .. "_hotUpdateState_", 1)
        self:recordViewData(message)
	else
		HotUpdateScene.isHotUpdate = false
		self:hotUpdateSuccess(gameKey)
        cc.UserDefault:getInstance():setIntegerForKey(gameKey .. "_hotUpdateState_", 2)
    end
end

function HotUpdateScene.UpDateFailCallBack(notify, index)

end

function HotUpdateScene.ExitGameCallBack(notify, index)

end

function HotUpdateScene:hotUpdateProgress(progress)
    self:showProgress(progress)
    --显示进度条
end

function HotUpdateScene:parseVersion(version)
    local versionTable = {}
    for v in string.gmatch(version, "[^.]+") do
        table.insert(versionTable, tonumber(v))
    end

    return versionTable
end


function HotUpdateScene:showProgress(percent)
    if not percent then 
        return nil 
    end

    if(percent >= 0) then 
        curProgress = percent
        print("showProgress = " .. percent)
        self._percent = percent
        local runningScene = display.getRunningScene()
		local tmpLayer = runningScene:getChildByName("GAME_HOTUPDATE_LAYER")
		if not tmpLayer then
			return
        end
        
        XH.UITool.setText(tmpLayer,KW_LABEL_TIPS, self._subGameName.."正在加载中...")

		local ProgressNode = XH.UITool.seekNodeByName(tmpLayer, KW_PROGRESS)
        if HotUpdateScene._progressScheduleID == nil then
            HotUpdateScene._progressScheduleID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
                if ProgressNode and tolua.isnull(ProgressNode) == false and ((tmpProgress + fastProgress) <= curProgress or curProgress == 100) then
                    if tmpProgress == 0 and curProgress == 100 then 

                        ProgressNode:setPercent(curProgress)
                        XH.UITool.setText(tmpLayer,KW_PROGRESS_TEXT, math.ceil(curProgress) .. "%")
                        --只有一个文件需要更新的时候，收到直接就是100了，也关闭定时器
                        if HotUpdateScene._progressScheduleID then
                            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(HotUpdateScene._progressScheduleID)
                            HotUpdateScene._progressScheduleID = nil
                        end
                        return 
                    end
                    HotUpdateScene.isHotUpdate = true
                    if curProgress == 100 and fastProgress == 1 then
                        fastProgress = math.max((curProgress - tmpProgress) / 10, 1)
                    end
                    tmpProgress = tmpProgress + fastProgress
                    if math.ceil(tmpProgress) >= 100 then
                        tmpProgress = 100
                    end
					print("tmpProgress = " .. tmpProgress .. "curProgress = " .. curProgress)

                    ProgressNode:setPercent(tmpProgress)
                    XH.UITool.setText(tmpLayer,KW_PROGRESS_TEXT, math.ceil(tmpProgress) .. "%")
                    if tmpProgress == 100 and curProgress == 100 then
                        HotUpdateScene.isHotUpdate = false
                        if HotUpdateScene._progressScheduleID then
                            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(HotUpdateScene._progressScheduleID)
                            tmpProgress = 0
                            fastProgress = 1
                            HotUpdateScene._progressScheduleID = nil
                        end
                        self:hotUpdateSuccess()
                    end
                end
            end, 0.033, false)
        end
    end
end

function HotUpdateScene:showProgressTitle(message)
    print("ProgressTitle:"..message)
end

function HotUpdateScene:recordViewData(errorCode)
    if XH.playerData:getIsNewUserGuide() then
        --热更新结束埋点
        local data = {}
        data.time1 = self._viewStartTime or 0
        data.time2 = os.time() or 0
        data.staytime = data.time2 -  data.time1
        data.loading = self._percent
        data.userid = XH.playerData:getNumberID()
        data.error = errorCode or 0
        data.version2 = self._newVersion
        XH.throwDataManager:throwData(XH.ThrowDataDefine.GoldGamesLoaded, data)
    end
end

return HotUpdateScene  �  