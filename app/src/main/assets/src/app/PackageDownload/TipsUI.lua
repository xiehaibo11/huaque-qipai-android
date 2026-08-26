--- 强更提示界面
--@module TipsUI

local View = class("View")

local tool = require("app.PackageDownload.Tool")
local config = require("app.PackageDownload.Config")

local __originScale = 0.8
local __clickScale = 0.9
local __clickColor = cc.c3b(160, 160, 160)
local __normalColor = cc.c3b(255, 255, 255)

function View:getCSBPath()
    return "cocosStudio/hall/CSB/PackageUpdate/Tips.csb"
end

local BTN_POSITION = {
    [1] = {0},
    [2] = {-150, 150}
}

-- local UPDATE_TYPE = {
--     No = 0,
--     Force = 1,
--     Gerenal = 2
-- }

local TIPS_CONCENT = {
    ["Install"] = {
        [1] = {
            {style = "STYLE_1", content = "游戏版本已准备就绪(免流量)"},
            {style = "MAIN_TEXT", content = "稳定性更强！游戏更流畅！"},
            {style = "STYLE_1", content = "即将进行安装"},
            {style = "STYLE_2", content = "(安装后方可正常进行游戏)"}
        },
        [2] = {
            {style = "STYLE_1", content = "游戏版本已准备就绪(免流量)"},
            {style = "MAIN_TEXT", content = "稳定性更强！游戏更流畅！"},
            {style = "STYLE_1", content = "是否现在安装？"}
        }
    },
    ["Update"] = {
        [1] = {
            {style = "STYLE_1", content = "检测到游戏存在更高版本，"},
            {style = "MAIN_TEXT", content = "稳定性更强！游戏更流畅！"},
            {style = "STYLE_1", content = "即将进行下载更新"},
            {style = "STYLE_2", content = "(包体大小:%dMB,更新后可正常进行游戏)", param = {"fileSize"}}
        },
        [2] = {
            {style = "STYLE_1", content = "检测到游戏存在更高版本"},
            {style = "MAIN_TEXT", content = "稳定性更强！游戏更流畅！"},
            {style = "STYLE_2", content = "(包体大小:%dMB,后台下载不影响游戏)", param = {"fileSize"}},
            {style = "STYLE_1", content = "是否进行升级更新？"}
        }
    },
    ["NetTip"] = {
        [1] = {},
        [2] = {}
    }
}

local BTN_TYPES = {
    ["Install"] = {
        [1] = {"_INSTALL"},
        [2] = {"_WAIT", "_INSTALL"}
    },
    ["Update"] = {
        [1] = {"_UPDATE"},
        [2] = {"_WAIT", "_UPDATE"}
    },
    ["NetTip"] = {
        [1] = {"_UPDATE"},
        [2] = {"_WAIT", "_UPDATE"}
    }
}

function View:ctor(title, versionInfo, func)
    self._versionInfo = versionInfo
    self._apkPath = config.apkPath .. self._versionInfo.last_version .. "/update.apk"
    self._func = func
    local scene = cc.Director:getInstance():getRunningScene()
    self._csbRoot = cc.CSLoader:createNodeWithVisibleSize(self:getCSBPath())
    scene:addChild(self._csbRoot)

    self:initUI()
    self:initBtnUI(BTN_TYPES[title][self._versionInfo.updateType])

    local tips = TIPS_CONCENT[title][self._versionInfo.updateType]
    self:initTips(tips)
end

function View:initUI()
    self._panel = tool.findNodeByName(self._csbRoot, "PANEL")
    self._tipsPanel = tool.findNodeByName(self._csbRoot, "TIPS")
    self._contentList = tool.findNodeByName(self._csbRoot, "CONTENT_LIST")
    self._contentList:setScrollBarEnabled(false)
    self._contentList:setEnabled(false)

    self._contentModel = {}
    self._contentModel["STYLE_1"] = tool.findNodeByName(self._csbRoot, "STYLE_1")
    self._contentModel["MAIN_TEXT"] = tool.findNodeByName(self._csbRoot, "MAIN_TEXT")
    self._contentModel["STYLE_2"] = tool.findNodeByName(self._csbRoot, "STYLE_2")

    -- 安装按钮
    self._installBtn = tool.findNodeByName(self._csbRoot, "_INSTALL")
    self._installBtn:addTouchEventListener(handler(self, self.onClickInstall))
    self._installBtn:setVisible(false)

    -- 稍后再说
    self._waitBtn = tool.findNodeByName(self._csbRoot, "_WAIT")
    self._waitBtn:addTouchEventListener(handler(self, self.onClickWait))
    self._waitBtn:setVisible(false)

    -- 立即更新
    self._updateBtn = tool.findNodeByName(self._csbRoot, "_UPDATE")
    self._updateBtn:addTouchEventListener(handler(self, self.onClickGoUpdate))
    self._updateBtn:setVisible(false)
end

function View:initBtnUI(btns)
    local btnPs = BTN_POSITION[#btns]
    for i = 1, #btns do
        local btn = tool.findNodeByName(self._csbRoot, btns[i])
        btn:setVisible(true)
        btn:setPositionX(btnPs[i])
    end
end

function View:initTips(tips)
    local height = 0
    for _, tip in ipairs(tips) do
        if tip.style == "MAIN_TEXT" then
            tip.content = self._versionInfo.content ~= "" and self._versionInfo.content or tip.content
        end
        if tip.param then
            local param = tip.param
            local data = {}
            for i = 1, #param do
                if param[i] == "fileSize" then
                    data[i] = self._versionInfo[param[i]] / 1024 / 1024
                else
                    data[i] = self._versionInfo[param[i]]
                end
            end
            if unpack(data) ~= 0 then
                tip.content = string.format(tip.content, unpack(data))
            else
                tip.content = "更新后可正常进行游戏"
            end
        end
        local node = self._contentModel[tip.style]:clone()
        local stringTool = require("app.Tool.StringTool")
        node:setString(stringTool.newLineStringByMaxLen(tip.content, 54))
        node:setName("_")
        self._contentList:pushBackCustomItem(node)
        height = node:getSize().height + height
    end
    local listWidth = self._contentList:getContentSize().width
    self._contentList:setContentSize(cc.size(listWidth, height))
    local width = self._tipsPanel:getSize().width
    self._tipsPanel:setSize(cc.size(width, height + 250))
    ccui.Helper:doLayout(self._tipsPanel)
end

function View:onClickGoUpdate(send, eventType)
    if eventType == ccui.TouchEventType.began then
        send:setScale(__originScale * __clickScale)
        send:setColor(__clickColor)
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setScale(__originScale)
        send:setColor(__normalColor)
    end
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    if cc.Application:getInstance():getTargetPlatform() == cc.PLATFORM_OS_ANDROID then
        self._func()
    else
        cc.Application:getInstance():openURL("https://www.baidu.com")
    end
    self._csbRoot:removeSelf(true)
end

function View:onClickWait(send, eventType)
    if eventType == ccui.TouchEventType.began then
        send:setScale(__originScale * __clickScale)
        send:setColor(__clickColor)
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setScale(__originScale)
        send:setColor(__normalColor)
    end
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local eventDispatcher = cc.Director:getInstance():getEventDispatcher()
    local event = cc.EventCustom:new("TOTALUPDATE_CLOSE")
    eventDispatcher:dispatchEvent(event)
    self._csbRoot:removeSelf(true)
end

function View:onClickInstall(send, eventType)
    if eventType == ccui.TouchEventType.began then
        send:setScale(__originScale * __clickScale)
        send:setColor(__clickColor)
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setScale(__originScale)
        send:setColor(__normalColor)
    end
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    tool.install(self._apkPath)
    un.FileSystem.removeDirectory(un.FileSystem.getWritePath() .. "hotfix/")
end

function View:onClickClose(send, eventType)
    if eventType == ccui.TouchEventType.began then
        send:setScale(__originScale * __clickScale)
        send:setColor(__clickColor)
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setScale(__originScale)
        send:setColor(__normalColor)
    end
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._csbRoot:removeSelf(true)
end

return View
  �   