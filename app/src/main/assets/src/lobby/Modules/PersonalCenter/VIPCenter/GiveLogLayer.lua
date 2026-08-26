local GiveLogLayer = class("GiveLogLayer", XH.ViewBase)

local KW_TEXT_LOG_TIME = "_KW_TEXT_LOG_TIME"
local KW_TEXT_LOG_TYPE = "_KW_TEXT_LOG_TYPE"
local KW_TEXT_LOG_NAME = "_KW_TEXT_LOG_NAME"
local KW_TEXT_LOG_INFO = "_KW_TEXT_LOG_INFO"

local KW_MAX_LOG_NUM = 6
local KW_CHOOSE_TYPE = {
    ALL = 0,
    SEND = 1,
    GET = 2,
}

--转让状态：0：默认状态,1:已撤回,2:已失效
local KW_OPERATE_TYPE = {
    NORMAL = 0,
    ROLLBACK = 1,
    EXPIRE = 2,
}

local KW_CHOOSE_TYPE_TEXT = {
    [KW_CHOOSE_TYPE.SEND] = "送出",
    [KW_CHOOSE_TYPE.GET] = "收到",
    [KW_CHOOSE_TYPE.ALL] = "全部",
}
function GiveLogLayer:getCSBPath()
    return "cocosStudio/hall/CSB/PersonalCenter/PerCenterGiveLogLayer.csb"
end

function GiveLogLayer:getBindingInfo()
    return{
        ["_KW_BTN_CHOOSE_TYPE"] = { varName = "_btnChooseType", onTouchEnded = "onTouchEventChooseType", type = XH.UI_TYPE.IMAGE_TOBUTTON },
        ["_KW_LOG_TYPE_CHOOSE_BG"] = { varName = "_bgLogTypeChoose"},
        ["_KW_TEXT_CHOOSE_TYPE"] = { varName = "_textChooseType"},
        ["_KW_LOG_TYPE_ALL"] = { varName = "_btnChooseAll", onTouchEnded = "onTouchEventChooseAll", type = XH.UI_TYPE.TEXT },
        ["_KW_LOG_TYPE_GET"] = { varName = "_btnChooseGet", onTouchEnded = "onTouchEventChooseGet", type = XH.UI_TYPE.TEXT },
        ["_KW_LOG_TYPE_SEND"] = { varName = "_btnChooseSend", onTouchEnded = "onTouchEventChooseSend", type = XH.UI_TYPE.TEXT },
        ["_KW_BTN_NEXT_PAGE"] = { varName = "_btnNextPage", onTouchEnded = "onTouchEventNextPage", type = XH.UI_TYPE.IMAGE_TOBUTTON },
        ["_KW_BTN_PRE_PAGE"] = { varName = "_btnPrePage", onTouchEnded = "onTouchEventPrePage", type = XH.UI_TYPE.IMAGE_TOBUTTON },
        ["_KW_TEXT_PAGE"] = { varName = "_textPage"},
        ["_KW_LISTVIEW_LOG"] = { varName = "_listViewLog" },
        ["_KW_PANEL_LOG_ITEM"] = { varName = "_panelLogItem" },
        ["_KW_TEXT_LOG_GIVE_PROP"] = { varName = "_btnGiveProp", onTouchEnded = "onTouchEventGiveProp", type = XH.UI_TYPE.TEXT },
        ["_KW_TEXT_LOG_CANCEL"] = { varName = "_btnCancelGiveProp", onTouchEnded = "onTouchEventCancelGiveProp", type = XH.UI_TYPE.TEXT },
        ["_KW_TEXT_LOG_SAVE_TIME"] = { varName = "_textLogSaveTime" },
    }
end

function GiveLogLayer:getProxyEvents()
    return {
        { module = XH.lobby:getModule("PersonalCenter"),eventKeyName = "EVENT_VIP_LAYER_VIP_LOG_INFO", callBack = "eventVipLogInfo" },
        { module = XH.lobby:getModule("PersonalCenter"),eventKeyName = "EVENT_VIP_LAYER_ROLL_BACK_CARD", callBack = "eventRollBackCard" }
    }
end

function GiveLogLayer:ctor()
    GiveLogLayer.super.ctor(self)
    self._page = 1
    self._chooseType = KW_CHOOSE_TYPE.ALL
    local logInfo = XH.lobby:getModule("PersonalCenter"):getVipLogInfo(self._page,KW_MAX_LOG_NUM)
    if logInfo then
        self._logInfoTable = logInfo
        self:initUI()
    end
end

function GiveLogLayer:flush(data)
    XH.lobby:getModule("PersonalCenter"):getVipLogInfo(1,KW_MAX_LOG_NUM, true)
end

function GiveLogLayer:initUI()
    local saveTime = math.ceil( self._logInfoTable.log_expire_time / (60*60*24))
    local showStr = "注：记录仅保留最近" .. saveTime .. "天"
    if self._logInfoTable.rollback_expire_time > 0 then
        showStr =  showStr .. "，撤回有效时间为"  .. self._logInfoTable.rollback_expire_time / 60 .. "分钟。"
    end
    self._textLogSaveTime:setString(showStr)
    local logInfoTable = self:getLogInfoByType(self._chooseType)
    self._textPage:setString(self._page .. "/" .. self:getMaxPage(logInfoTable))
    self._textChooseType:setString(KW_CHOOSE_TYPE_TEXT[self._chooseType])
    self:updateLogList()
end

function GiveLogLayer:updateLogList()
    local logInfoTable = self:getLogInfoByType(self._chooseType)
    self._listViewLog:removeAllChildren()
    self._listViewLog:setScrollBarOpacity(0)
    for i, logInfo in ipairs(logInfoTable) do
        if (self._page - 1) * KW_MAX_LOG_NUM < i and i <= self._page * KW_MAX_LOG_NUM then
            local logItem = self._panelLogItem:clone()
            XH.UITool.setText(logItem, KW_TEXT_LOG_TIME, os.date("%Y.%m.%d %H:%M",logInfo.oper_time) )
            XH.UITool.setText(logItem, KW_TEXT_LOG_TYPE, logInfo.oper_type == KW_CHOOSE_TYPE.SEND and "您送出" or "您收到")
            XH.UITool.setTextColor(logItem, KW_TEXT_LOG_TYPE, logInfo.oper_type == KW_CHOOSE_TYPE.SEND and cc.c3b(222, 75, 59) or cc.c3b(30, 145, 141))
            XH.UITool.setText(logItem, KW_TEXT_LOG_NAME, self:hideName(logInfo.to_user_name))
            XH.UITool.setText(logItem, KW_TEXT_LOG_INFO, logInfo.prop_cnt .. "张房卡")
            XH.UITool.seekNodeByName(logItem, "_KW_TEXT_LOG_GIVE_PROP").log_index = tostring(logInfo.log_index)
            XH.UITool.seekNodeByName(logItem, "_KW_TEXT_LOG_CANCEL").log_index = tostring(logInfo.log_index)
            self:updateOperateButton(logItem,logInfo)
            logItem:setVisible(true)
            self._listViewLog:pushBackCustomItem(logItem)
        end
        if i > self._page * KW_MAX_LOG_NUM then
            break
        end
    end
end

function GiveLogLayer:updateOperateButton(item,logInfo)
    local isVisible = logInfo.oper_type == KW_CHOOSE_TYPE.SEND
    XH.UITool.setVisible(item, "_KW_OPER_LOG", isVisible)
    if isVisible then
        local panelNode = XH.UITool.seekNodeByName(item, "_KW_OPER_LOG")
        local giveButton = XH.UITool.seekNodeByName(item, "_KW_TEXT_LOG_GIVE_PROP")
        local cancelButton = XH.UITool.seekNodeByName(item, "_KW_TEXT_LOG_CANCEL")
        local rollbackExpireTime = self._logInfoTable.rollback_expire_time or 0
        local curTime = os.time()
        if logInfo.oper_state ==  KW_OPERATE_TYPE.NORMAL and rollbackExpireTime > 0  and curTime - logInfo.oper_time < rollbackExpireTime then
            giveButton:setVisible(true)
            cancelButton:setText("[撤回]")
            cancelButton:setVisible(true)
        elseif logInfo.oper_state ==  KW_OPERATE_TYPE.ROLLBACK then
            giveButton:setVisible(true)
            cancelButton:setText("[已撤回]")
            cancelButton:setVisible(true)
            cancelButton:setEnabled(false)
            cancelButton:setTextColor(cc.c3b(127, 127, 127))
        else
            cancelButton:setVisible(false)
            giveButton:setVisible(true)
            giveButton:setPosition(panelNode:getContentSize().width/2,panelNode:getContentSize().height/2)
        end
    end
end

function GiveLogLayer:getLogInfoByType(chooseType)
    if not self._logInfoTable then
        return {}
    end
    local logTable = {}
    for _, logInfo in ipairs(self._logInfoTable.logs) do
        if logInfo.oper_type == chooseType or chooseType == KW_CHOOSE_TYPE.ALL then
            table.insert(logTable, logInfo)
        end
    end
    return logTable
end

function GiveLogLayer:eventVipLogInfo(event)
    if event and event.data then
        self._logInfoTable = event.data
        self:initUI()
    end
end

function GiveLogLayer:eventRollBackCard(event)
    if event and event.data then
        local rollBcakData = event.data
        local index = rollBcakData.log_index
        local state = rollBcakData.oper_state
        if state ==  KW_OPERATE_TYPE.ROLLBACK then
            local logInfoTable = self:getLogInfoByType(self._chooseType)
            for _, value in pairs(logInfoTable) do
                if value.log_index == index then
                    value.oper_state = KW_OPERATE_TYPE.ROLLBACK
                    XH.TipTool.showToast("成功撤回" .. value.prop_cnt .. "张卡", 2)
                end
            end
            XH.playerData:flushPlayerDrop()
        elseif state ==  KW_OPERATE_TYPE.EXPIRE then
            XH.TipTool.showToast("已经过期，无法撤回", 2)
            local logInfoTable = self:getLogInfoByType(self._chooseType)
            for _, value in pairs(logInfoTable) do
                if value.log_index == index then
                    value.oper_state = KW_OPERATE_TYPE.EXPIRE
                end
            end
            
        end
        self:updateLogList()
    end
end

function GiveLogLayer:hideName(nickname)
    if string.len(nickname) > 10 then
        local validBytes = XH.StringTool.getMinimumValidBytesUTF8(nickname, 10)
        nickname = string.sub(nickname,1,validBytes) .. "..."
    end
    return nickname
end

function GiveLogLayer:onTouchEventChooseType(send, eventType)
    self._bgLogTypeChoose:setVisible(not self._bgLogTypeChoose:isVisible())
end

function GiveLogLayer:onTouchEventChooseAll(send, eventType)
    self._chooseType = KW_CHOOSE_TYPE.ALL
    self._page = 1
    local logInfoTable = self:getLogInfoByType(self._chooseType)
    self._textPage:setString(self._page .. "/" .. self:getMaxPage(logInfoTable))
    self:updateLogList()
    self._bgLogTypeChoose:setVisible(false)
    self._textChooseType:setString(KW_CHOOSE_TYPE_TEXT[self._chooseType])
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.PERSONAL_CENTET_LOGTYPE_BTN_CLICK, {nType = "all"})
end

function GiveLogLayer:onTouchEventChooseGet(send, eventType)
    self._chooseType = KW_CHOOSE_TYPE.GET
    self._page = 1
    local logInfoTable = self:getLogInfoByType(self._chooseType)
    self._textPage:setString(self._page .. "/" .. self:getMaxPage(logInfoTable))
    self:updateLogList()
    self._bgLogTypeChoose:setVisible(false)
    self._textChooseType:setString(KW_CHOOSE_TYPE_TEXT[self._chooseType])
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.PERSONAL_CENTET_LOGTYPE_BTN_CLICK, {nType = "get"})
end

function GiveLogLayer:onTouchEventChooseSend(send, eventType)
    self._chooseType = KW_CHOOSE_TYPE.SEND
    self._page = 1
    local logInfoTable = self:getLogInfoByType(self._chooseType)
    self._textPage:setString(self._page .. "/" .. self:getMaxPage(logInfoTable))
    self:updateLogList()
    self._bgLogTypeChoose:setVisible(false)
    self._textChooseType:setString(KW_CHOOSE_TYPE_TEXT[self._chooseType])
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.PERSONAL_CENTET_LOGTYPE_BTN_CLICK, {nType = "send"})
end

function GiveLogLayer:onTouchEventNextPage(send, eventType)
    local logInfoTable = self:getLogInfoByType(self._chooseType)
    if (self._page + 1) > self:getMaxPage(logInfoTable) then
        return
    else
        self._page = self._page + 1
    end
    self._textPage:setString(self._page .. "/" .. self:getMaxPage(logInfoTable))
    self:updateLogList()
end

function GiveLogLayer:onTouchEventPrePage(send, eventType)
    local logInfoTable = self:getLogInfoByType(self._chooseType)
    if (self._page - 1) < 1 then
        return
    else
        self._page = self._page - 1
    end
    self._textPage:setString(self._page .. "/" .. self:getMaxPage(logInfoTable))
    self:updateLogList()
end

function GiveLogLayer:onTouchEventGiveProp(send, eventType)
    local logInfoTable = self:getLogInfoByType(self._chooseType)
    for _, logInfo in ipairs(logInfoTable) do
        if send.log_index == tostring(logInfo.log_index) then
            XH.lobby:getModule("PersonalCenter"):showGiveLayerTab("GivePropLayer",logInfo)
            break
        end
    end
end

function GiveLogLayer:onTouchEventCancelGiveProp(send, eventType)
    local logInfoTable = self:getLogInfoByType(self._chooseType)
    for _, logInfo in ipairs(logInfoTable) do
        if send.log_index == tostring(logInfo.log_index) then
            XH.lobby:getModule("PersonalCenter"):reqVIPRollbackTransCard(send.log_index)
            break
        end
    end
end

function GiveLogLayer:getMaxPage(logInfoTable)
    local maxPage = math.ceil(#logInfoTable / KW_MAX_LOG_NUM)
    if maxPage == 0 or maxPage == nil then
        return 1
    end
    return maxPage
end

return GiveLogLayer/  