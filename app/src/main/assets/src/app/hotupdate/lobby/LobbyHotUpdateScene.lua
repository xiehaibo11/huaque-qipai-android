
local KW_CSB_PROGRESS = "res/cocosStudio/hall/CSB/Progress/Progress.csb"
local KW_PROGRESS = "Progress"
local KW_COLOR_LAYER = "ColorLayer"
local KW_CSB_MESSAGEBOX = "res/cocosStudio/hall/CSB/MessageBox.csb"
local KW_PROGRESS_TEXT      = "ProgressText"
local KWA_PANEL_BUTTON_LIST_2 = "KWA_PANEL_BUTTON_LIST_2"
local KWA_MSG_LABEL = "KWA_MSG_LABEL"
local KWA_BTND_CLOSE = "KWA_BTND_CLOSE"
local KWA_BTND_OK_1 = "KWA_BTND_OK_1"
local HOTUPDATE_BACK_IMG = "HOTUPDATE_BACK_IMG"
local KW_ZHEJIANG_TITLE = "KW_ZHEJIANG_TITLE"

local LobbyHotUpdateScene = class("LobbyHotUpdateScene")
local ChannelTool  = require("app.Tool.ChannelTool")

local director = cc.Director:getInstance()

LobbyHotUpdateScene._firstShowLogo = true
LobbyHotUpdateScene._hotUpdateCSBNode = nil
LobbyHotUpdateScene._needUpdate = nil
LobbyHotUpdateScene._UpdateManager = nil
LobbyHotUpdateScene._requireLobbySceneScheduler = nil
LobbyHotUpdateScene._showLogoScheduler = nil

local tmpProgress = 0
local curProgress = 0
local fastProgress = 1
LobbyHotUpdateScene._progressScheduleID = nil
LobbyHotUpdateScene.isHotUpdate = false

function LobbyHotUpdateScene:start()
    local scene = cc.Scene:create()
    local layer = cc.Layer:create()

    if device.platform == "android" then
        --监听退出按钮(点击退出按钮 询问玩家是否退出游戏)
        layer:setKeypadEnabled(true)  
        
        layer:addNodeEventListener(cc.KEYPAD_EVENT, function (event)  
            if event.key == "back" then
				self:shwoMessageBox("确定退出游戏?")
            end        
        end)
    end

    scene:addChild(layer)

    self.csbNode = cc.CSLoader:createNodeWithVisibleSize(KW_CSB_PROGRESS)

	
   
    if self.csbNode then
		self:setLabelString("VersionText","正在校验本地资源中...")
        
        if device.platform == "ios" then
            local xhLogo = LobbyHotUpdateScene.findNodeByName(self.csbNode,"KW_PIC_BF_LOGO")  
            if xhLogo then 
                xhLogo:setVisible(false)
            end
        end
        -- 显示启动图
        if cc.FileUtils:getInstance():isFileExist("ShowBFLogo.luac") or 
            cc.FileUtils:getInstance():isFileExist("ShowBFLogo.lua") then 
            local showLogo = require("ShowBFLogo")
            if showLogo and self._firstShowLogo then 
                self._firstShowLogo = false 
                local xhLogo = LobbyHotUpdateScene.findNodeByName(self.csbNode,"KW_PIC_BF_LOGO")  
                if xhLogo then 
                    if display.isUseNewResolution and display.isUseNewResolution() then
                        xhLogo:setVisible(true)
                    else
                        local designSize = cc.size(960, 640)
                        local screenSize = cc.Director:getInstance():getWinSize()
                        local kuangaobi = ( screenSize.width / screenSize.height) / (designSize.width / designSize.height)
                        xhLogo:setScaleY(1/kuangaobi)
                        xhLogo:setVisible(true)
                    end
                end
            end
        end
        self._showLogoScheduler = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._showLogoScheduler)
            self:hideLogo()
        end,1.5,false)

        layer:addChild(self.csbNode)
		
		self.tipsCsbNode = cc.CSLoader:createNodeWithVisibleSize(KW_CSB_MESSAGEBOX)
		if self.tipsCsbNode then
			layer:addChild(self.tipsCsbNode)
		end

		self:initMessageBox()
		
		if director:getRunningScene() then
			director:replaceScene(scene)
		else
			director:runWithScene(scene)
		end
					
	end
    self._hotUpdateStart = os.time()
    if  ChannelTool.checkIsCPSChannel() then
        local hotupdateImg = LobbyHotUpdateScene.findNodeByName(self.csbNode,HOTUPDATE_BACK_IMG)  
        local imgName = ChannelTool.getCPSHotupdateImg()
        if hotupdateImg and imgName ~= "" then 
            hotupdateImg:loadTexture(imgName, ccui.TextureResType.localType) 
        end
        local hotupdatetitle = LobbyHotUpdateScene.findNodeByName(self.csbNode,KW_ZHEJIANG_TITLE)
        if hotupdatetitle then 
            hotupdatetitle:setVisible(false)
        end
    end
    --外省渠道包热更浙江游戏大厅LOGO消失
    if ChannelTool.checkIsWaishengChannel() or ChannelTool.checkIsDoubleKouGame() then 
        local hotupdatetitle = LobbyHotUpdateScene.findNodeByName(self.csbNode,KW_ZHEJIANG_TITLE)
        if hotupdatetitle then
            hotupdatetitle:setVisible(false)
        end
    end 
    if ChannelTool.checkIsZJQuDaoChannel() then
        local hotupdatetitle = LobbyHotUpdateScene.findNodeByName(self.csbNode,KW_ZHEJIANG_TITLE)
        if hotupdatetitle then
            hotupdatetitle:setVisible(false)
        end
    end
    if ChannelTool.checkIsECarChannel() then
        local hotupdatetitle = LobbyHotUpdateScene.findNodeByName(self.csbNode,KW_ZHEJIANG_TITLE)
        if hotupdatetitle then
            hotupdatetitle:setVisible(false)
        end
    end
end

function LobbyHotUpdateScene.findNodeByName(root,strKeyword)        
    if root == nil then
        return nil
    end
      
    local getName = function()
        return root:getName()
    end 
    
    local success,name = pcall(getName)
    
    if success == false then
        return nil
    end
    
    if name == strKeyword then
        return root
    end
    
    local node = nil
    local childrennode = root:getChildren()
    for i = 1,root:getChildrenCount() do
        node = LobbyHotUpdateScene.findNodeByName(childrennode[i],strKeyword)
        if node ~= nil then
            break
        end
    end 
    return node
end

function LobbyHotUpdateScene.hideLogo()
    if LobbyHotUpdateScene.csbNode then 
        local xhLogo = LobbyHotUpdateScene.findNodeByName(LobbyHotUpdateScene.csbNode,"KW_PIC_BF_LOGO")  
        if xhLogo then 
            xhLogo:setVisible(false) 
        end
    end
end

function LobbyHotUpdateScene:initMessageBox()
    if self.tipsCsbNode then
		self.tipsCsbNode:setVisible(false)
		local btnGroup = LobbyHotUpdateScene.findNodeByName(self.tipsCsbNode,KWA_PANEL_BUTTON_LIST_2)  
        if btnGroup then 
            btnGroup:setVisible(false) 
        end

		local closeBtn = LobbyHotUpdateScene.findNodeByName(self.tipsCsbNode,KWA_BTND_CLOSE)  
        if closeBtn then 
            closeBtn:setVisible(false) 
        end

		local label = LobbyHotUpdateScene.findNodeByName(self.tipsCsbNode,KWA_MSG_LABEL)  
        if label then 
            label:setString("") 
        end

		local confirmBtn = LobbyHotUpdateScene.findNodeByName(self.tipsCsbNode,KWA_BTND_OK_1)  
        if confirmBtn then 
            confirmBtn:addTouchEventListener(self.ExitGameCallBack) 
        end
	end
end

function LobbyHotUpdateScene:shwoMessageBox(string)
    if self.tipsCsbNode then
		local colorLayer = LobbyHotUpdateScene.findNodeByName(self.tipsCsbNode,KW_COLOR_LAYER)  
        if colorLayer then 
            colorLayer:setVisible(true) 
        end
		local label = LobbyHotUpdateScene.findNodeByName(self.tipsCsbNode,KWA_MSG_LABEL)  
        if label then 
            label:setString(string) 
        end
		self.tipsCsbNode:setVisible(true)
	end
end

----------------------------unversie 接口------------------------------------

function LobbyHotUpdateScene:showProgress(percent)
    if not percent then return nil end
    if (percent >= 0) then 
        curProgress = percent
        print("showProgress = " .. percent)
        local ProgressNode = LobbyHotUpdateScene.findNodeByName(self.csbNode,KW_PROGRESS)
		
        if LobbyHotUpdateScene._progressScheduleID == nil then
            LobbyHotUpdateScene._progressScheduleID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
                if ProgressNode and tolua.isnull(ProgressNode) == false and ((tmpProgress + fastProgress) <= curProgress or curProgress == 100) then
                    if tmpProgress == 0 and curProgress == 100 then 

                        ProgressNode:setPercent(curProgress)

						local processLabel = LobbyHotUpdateScene.findNodeByName(self.csbNode,KW_PROGRESS_TEXT)  
						if processLabel then 
							processLabel:setString(math.ceil(curProgress) .. "%")
						end

                        --只有一个文件需要更新的时候，收到直接就是100了，也关闭定时器
                        if LobbyHotUpdateScene._progressScheduleID then
                            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(LobbyHotUpdateScene._progressScheduleID)
                            LobbyHotUpdateScene._progressScheduleID = nil
                        end

                        return 
                    end
                    LobbyHotUpdateScene.isHotUpdate = true
                    if curProgress == 100 and fastProgress == 1 then
                        fastProgress = math.max((curProgress - tmpProgress) / 10, 1)
                    end
                    tmpProgress = tmpProgress + fastProgress
                    if math.ceil(tmpProgress) >= 100 then
                        tmpProgress = 100
                    end

                    ProgressNode:setPercent(tmpProgress)

					local label = LobbyHotUpdateScene.findNodeByName(self.csbNode,KW_PROGRESS_TEXT)  
					if label then 
						label:setString( math.ceil(tmpProgress) .. "%")
					end
                    if tmpProgress == 100 and curProgress == 100 then
                        LobbyHotUpdateScene.isHotUpdate = false
                        if LobbyHotUpdateScene._progressScheduleID then
                            cc.Director:getInstance():getScheduler():unscheduleScriptEntry(LobbyHotUpdateScene._progressScheduleID)
                            tmpProgress = 0
                            fastProgress = 1
                            LobbyHotUpdateScene._progressScheduleID = nil
                        end
                        self:hotUpdateSuccess()
                    end
                end
            end, 0.033, false)
        end
    end
end

function LobbyHotUpdateScene:hotUpdateSuccess()
    cc.UserDefault:getInstance():setIntegerForKey("Lobby_hotUpdateState_", 0)
    if LobbyHotUpdateScene.isHotUpdate then return end
    cc.FileUtils:getInstance():purgeCachedEntries()
    self:startGame(true)
end

function LobbyHotUpdateScene:startGame(updateResult)  
    self._requireLobbySceneScheduler = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._requireLobbySceneScheduler) -- 取消自己
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._showLogoScheduler) -- 取消logo图
        self:hideLogo()
		
        require("app.Launcher"):getInstance():onLaunchSuccess()
    end,0.1,false)
end

function LobbyHotUpdateScene.UpDateFailCallBack(notify, index)
    cc.Director:getInstance():endToLua()
end

function LobbyHotUpdateScene:hotUpdateProgress(progress)
    self:showProgress(progress)
end

function LobbyHotUpdateScene.ExitGameCallBack()
	cc.Director:getInstance():endToLua()
end

function LobbyHotUpdateScene:showProgressTitle(message)
   
end

function LobbyHotUpdateScene:needHotUpdatescene(flag,tag)
    self._needUpdate = self._needUpdate  or {} 
end

function LobbyHotUpdateScene:needHotUpdate(tag,oldVersion, newVersion)
    self._needUpdate = self._needUpdate  or {} 
    self._needUpdate[tag] = true
    if tag=="Lobby" then
        cc.UserDefault:getInstance():setStringForKey("Lobby_oldVersion_", oldVersion or "error")
        cc.UserDefault:getInstance():setStringForKey("Lobby_newVersion_", newVersion or "error")
		self:setLabelString("VersionText","当前版本:"..oldVersion.." >>>>> 新版本:"..newVersion)
    end
end

function LobbyHotUpdateScene:hotUpdateFaile(message,relink,gameKey)
    print("hotUpdateFaile:"..gameKey)
    cc.UserDefault:getInstance():setIntegerForKey("Lobby_hotUpdateState_", 1)
    if relink == nil or relink == false then
        print("游戏加载失败,请检查网络是否正常,并重启游戏")
        self:shwoMessageBox("游戏加载失败,请检查网络是否正常,并重启游戏")
    else
        if message then
            self:setLabelString("VersionText",message)
        else
            self:setLabelString("VersionText","网络异常,尝试重连中")
        end
    end

    if cjson == nil then
        cjson = require "cjson"
    end
    local tdsdk = require("app.Third.ThinkData.Sdk")
    tdsdk:init()
    tdsdk:setSuperProperties({
        channel_id = 0,
        area_id = 0,
        tenant_id = 4,
    })
    tdsdk:track("Monitoring_program", {
        Monitoring_source = "APP",
        Monitoring_type = "客户端监控热更新失败",
        Monitoring_content = {
            info_des = message or "网络异常",
            code = -1,
            info = message or "网络异常"
        }
    })
end

function LobbyHotUpdateScene:setLabelString(key,string)
	local label = LobbyHotUpdateScene.findNodeByName(self.csbNode,key)
	if label then
		label:setString(string)
	end
end

return LobbyHotUpdateScene  �4  