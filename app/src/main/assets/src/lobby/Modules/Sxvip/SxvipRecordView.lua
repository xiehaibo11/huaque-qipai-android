local SxvipRecordView = class("SxvipRecordView", XH.ViewBase)
local SxvipConfig = require("lobby.Modules.Sxvip.Config")
local LIST_PAGE_SIZE = 5
local INT32 = 2147483647

local KW_TEXT_NUMBER = "_KW_TEXT_NUMBER"
local KW_IMG_HEAD = "_KW_IMG_HEAD"
local KW_PLAYER_NAME = "_KW_PLAYER_NAME"
local KW_PLAYER_ID = "_KW_PLAYER_ID"
local KW_RECORD_DAY = "_KW_RECORD_DAY"
local KW_RECORD_TIME = "_KW_RECORD_TIME"
local KW_BTN_REFUSE = "_KW_BTN_REFUSE"
local KW_BTN_GIVE = "_KW_BTN_GIVE"
local KW_IMG_STATE_BG_RED = "_KW_IMG_STATE_BG_RED"
local KW_IMG_STATE_BG_GRAY = "_KW_IMG_STATE_BG_GRAY"
local KW_TEXT_STATE = "_KW_TEXT_STATE"
local KW_TEXT_WAIT = "_KW_TEXT_WAIT"

local selectDateStr = {"最近7天", "最近3天", "今天"}
local selectStateStrReq = {"全部状态", "赠送/拒绝", "等待同意"}
local selectStateStrSend = {"全部状态", "已赠送", "已领取"}

function SxvipRecordView:ctor()
    SxvipRecordView.super.ctor(self)
    self:initData()
    self:initUI()
end

function SxvipRecordView:getCSBPath()
    return "cocosStudio/hall/CSB/Sxvip/SxvipRecordLayer.csb"
end

function SxvipRecordView:getBindingInfo()
    return {
        ["_KW_BTN_CLOSE"] = { varName = "KW_BTN_CLOSE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnClose"},
        ["_KW_LISTVIEW_LOG"] = { varName = "_listLog" },
        ["_KW_PLAYER_ITEM"] = { varName = "_logItem" },
        ["_KW_TEXT_NOITEM"] = { varName = "_imgNoItem" },
        ["_KW_CLEAR_BTN"] = { varName = "_btnClearUserInfo", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnClearInfo"},
        ["_KW_BTN_REQUEST_LOG"] = { varName = "_btnRequestLog", onTouchEnded = "onChangeTab"},
        ["_KW_BTN_SEND_LOG"] = { varName = "_btnSendLog", onTouchEnded = "onChangeTab"},
        ["_KW_INPUT_NUMID"] = {varName = "_inputText"},
        ["_KW_BTN_PREPAGE"] = { varName = "_btnPrePage", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnPrePage"},
        ["_KW_BTN_NEXTPAGE"] = { varName = "_btnNextPage", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnNextPage"},
        ["_KW_TEXT_CURPAGE"] = { varName = "_textCurPage" },
        
        ["_KW_TEXT_SELECT_DATE"] = { varName = "_textSelectDate" },
        ["_KW_IMG_DATE_BG"] = { varName = "_imgDateBg", onTouchEnded = "onBtnEventDateBg" },
        ["_KW_BTN_SHOW_DATE"] = { varName = "_btnShowDate", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventShowDate" },
        ["_KW_BTN_HIDE_DATE"] = { varName = "_btnHideDate", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventHideDate" },
        ["_KW_IMG_DATE_SELECT"] = { varName = "_imgDateSelect" },
        ["_KW_BTN_DATE_SELECT_1"] = { varName = "_btnDateSelect_1", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventDateSelect" },
        ["_KW_BTN_DATE_SELECT_2"] = { varName = "_btnDateSelect_2", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventDateSelect" },
        ["_KW_BTN_DATE_SELECT_3"] = { varName = "_btnDateSelect_3", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventDateSelect" },

        ["_KW_TEXT_SELECT_STATE"] = { varName = "_textSelectState" },
        ["_KW_IMG_STATE_BG"] = { varName = "_imgStateBg", onTouchEnded = "onBtnEventStateBg" },
        ["_KW_BTN_SHOW_STATE"] = { varName = "_btnShowState", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventShowState" },
        ["_KW_BTN_HIDE_STATE"] = { varName = "_btnHideState", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventHideState" },
        ["_KW_IMG_STATE_SELECT"] = { varName = "_imgStateSelect" },
        ["_KW_BTN_STATE_SELECT_1"] = { varName = "_btnStateSelect_1", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventStateSelect" },
        ["_KW_BTN_STATE_SELECT_2"] = { varName = "_btnStateSelect_2", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventStateSelect" },
        ["_KW_BTN_STATE_SELECT_3"] = { varName = "_btnStateSelect_3", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventStateSelect" },
    }
end

function SxvipRecordView:getProxyEvents()
    return {
       { module = XH.lobby:getModule("Sxvip"), eventKeyName = "SXVIP_EVENT_SEND_RECORD", callBack = "onNotifyFellowSend" },
    }
end

function SxvipRecordView:initData()
    self._isRequestingUserInfo = false
    self._isSearchingShow = false
    self._logType = SxvipConfig.OPT.NONE
    self._curPage = 1
    self._maxPage = 1
    self._selectDateIndex = 1
    self._selectStateIndex = 1
end

function SxvipRecordView:initUI()
    self._textCurPage:setString("1/1")
    self._inputText:registerScriptEditBoxHandler(function(eventName)
        if eventName == "ended" or eventName == "return" then
            if not self or tolua.isnull(self) or self._isRequestingUserInfo then
                return
            end
            self:reqSearchUserInfo()
        end
    end)
    self:initSelect()
    self:onChangeTab(self._btnRequestLog, ccui.TouchEventType.ended)
end

-- 切换页签时，需要重置数据
function SxvipRecordView:resetDataAndUI()
    self._curPage = 1
    self._maxPage = 1
    self._selectDateIndex = 1
    -- self._selectStateIndex = 1
    self._textCurPage:setString("1/1")
    self._inputText:setText("")
    self._textSelectDate:setString("最近7天")
    -- self._textSelectState:setString("全部状态")
    self:updateSelectState()
    self:closeDateSelect()
    self:closeStateSelect()
end

function SxvipRecordView:initSelect()
    self:closeDateSelect()
    for i = 1, #selectDateStr do
        if self["_btnDateSelect_"..i] then
            self["_btnDateSelect_"..i]:setTag(i)
            self["_btnDateSelect_"..i]:setTitleText(selectDateStr[i])
        end
    end
    self._textSelectDate:setString(selectDateStr[1])
    self._selectDateIndex = 1
    self:updateSelectState()
end

function SxvipRecordView:updateSelectState()
    self:closeStateSelect()
    local selectStateStr = selectStateStrReq
    if self._logType == SxvipConfig.OPT.log_send then
        selectStateStr = selectStateStrSend
    end
    for i = 1, #selectStateStr do
        if self["_btnStateSelect_"..i] then
            self["_btnStateSelect_"..i]:setTag(i)
            self["_btnStateSelect_"..i]:setTitleText(selectStateStr[i])
        end
    end
    self._selectStateIndex = 1
    self._textSelectState:setString(selectStateStr[1])
end

function SxvipRecordView:reqSearchUserInfo()
    local strInput = self._inputText:getText()
    if strInput == nil or strInput == "" then
        if self._isSearchingShow then
            self._isSearchingShow = false
            -- 根据玩家ID请求数据
            local param = {}
            self:reqFellowSend(param)
        end
        return
    end
    local tmpSearchNum = tonumber(strInput)
    if tmpSearchNum then
        if tmpSearchNum == 0 or tmpSearchNum >= INT32 then
            XH.TipTool.showToast("输入内容错误，请重试")
            return
        end
        self._isRequestingUserInfo = true
        self._isSearchingShow = true
        -- 根据玩家ID请求数据
        local param = {}
        self:reqFellowSend(param)
    else
        XH.TipTool.showToast("输入内容有误，请重试")
    end
end

function SxvipRecordView:onBtnClearInfo(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if self._isSearchingShow or self._inputText:getText() ~= "" then
        self._inputText:setText("")
        -- 请求日志信息
        local param = {}
        self:reqFellowSend(param)
    end
end

function SxvipRecordView:onChangeTab(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local name = send:getName()
    local bChange = false
    if name == "_KW_BTN_SEND_LOG" and self._logType ~= SxvipConfig.OPT.log_send then
        bChange = true
        self._logType = SxvipConfig.OPT.log_send
        self:setButtonState(self._btnSendLog, false)
        self:setButtonState(self._btnRequestLog, true)
    elseif name == "_KW_BTN_REQUEST_LOG" and self._logType ~= SxvipConfig.OPT.log_wait then
        bChange = true
        self._logType = SxvipConfig.OPT.log_wait
        self:setButtonState(self._btnRequestLog, false)
        self:setButtonState(self._btnSendLog, true)
    end
    self:resetDataAndUI()
    if bChange then
        -- 请求日志信息
        local param = {}
        param.log_select = SxvipConfig.LOGSELECT.None
        self:reqFellowSend(param)
    end
end

function SxvipRecordView:onBtnPrePage(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if self._curPage <= 1 then
        XH.TipTool.showToast("已经是第一页了")
        return
    end
    local param = {}
    param.page = self._curPage - 1
    self:reqFellowSend(param)
end

function SxvipRecordView:onBtnNextPage(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if self._curPage >= self._maxPage then
        XH.TipTool.showToast("已经是最后一页了")
        return
    end
    local param = {}
    param.page = self._curPage + 1
    self:reqFellowSend(param)
end

function SxvipRecordView:onBtnEventDateBg(send, eventType)
    if self._imgDateSelect:isVisible() then
        self:closeDateSelect()
    else
        self:showDateSelect()
    end
end

function SxvipRecordView:onBtnEventShowDate(send, eventType)
    self:showDateSelect()
end

function SxvipRecordView:onBtnEventHideDate(send, eventType)
    self:closeDateSelect()
end

function SxvipRecordView:onBtnEventDateSelect(send, eventType)
    self:closeDateSelect()
    local index = send:getTag()
    self:selectDate(index)
end

function SxvipRecordView:closeDateSelect()
    self._imgDateSelect:setVisible(false)
    self._btnShowDate:setVisible(true)
    self._btnHideDate:setVisible(false)
end

function SxvipRecordView:showDateSelect()
    self._imgDateSelect:setVisible(true)
    self._btnShowDate:setVisible(false)
    self._btnHideDate:setVisible(true)
end

function SxvipRecordView:selectDate(index)
    if not index then
        return
    end
    self._selectDateIndex = index
    local dateStr = self["_btnDateSelect_" .. index]:getTitleText()
    self._textSelectDate:setString(dateStr)
    -- 请求对应日期，需要获取日期开始结束时间
    local param = {}
    self:reqFellowSend(param)
end

function SxvipRecordView:onBtnEventStateBg(send, eventType)
    if self._imgStateSelect:isVisible() then
        self:closeStateSelect()
    else
        self:showStateSelect()
    end
end

function SxvipRecordView:onBtnEventShowState(send, eventType)
    self:showStateSelect()
end

function SxvipRecordView:onBtnEventHideState(send, eventType)
    self:closeStateSelect()
end

function SxvipRecordView:onBtnEventStateSelect(send, eventType)
    self:closeStateSelect()
    local index = send:getTag()
    self:selectState(index)
end

function SxvipRecordView:selectState(index)
    if not index then
        return
    end
    self._selectStateIndex = index
    local stateStr = self["_btnStateSelect_" .. index]:getTitleText()
    self._textSelectState:setString(stateStr)
    -- 请求对应日期，需要获取日期开始结束时间
    local param = {}
    self:reqFellowSend(param)
end

function SxvipRecordView:closeStateSelect()
    self._imgStateSelect:setVisible(false)
    self._btnShowState:setVisible(true)
    self._btnHideState:setVisible(false)
end

function SxvipRecordView:showStateSelect()
    self._imgStateSelect:setVisible(true)
    self._btnShowState:setVisible(false)
    self._btnHideState:setVisible(true)
end

function SxvipRecordView:reqFellowSend(data)
    -- 请求日志信息
    local param = {}
    param.opt = self._logType
    param.log_select = self:getLogSelect() or SxvipConfig.LOGSELECT.None
    param.page = data.page or 1
    param.page_size = LIST_PAGE_SIZE
    local timestamp = self:getTimeStamp()
    param.start_time = timestamp.start
    param.end_time = timestamp.endtime
    param.opt_user_id = tonumber(self._inputText:getText()) or 0
    XH.lobby:getModule("Sxvip"):reqFellowSend(param)
end

-- 请求返回通知
function SxvipRecordView:onNotifyFellowSend(event)
    self._isRequestingUserInfo = false
    if not event or not event.data then
        return
    end
    if event.data.opt == SxvipConfig.OPT.Agree or event.data.opt == SxvipConfig.OPT.Refuse then
        local param = {}
        self:reqFellowSend(param)
        return
    end
    self._listLog:removeAllChildren()
    if event.data.total == 0 then
        self._imgNoItem:setVisible(true)
        self._textCurPage:setString("1/1")
        return
    end
    self._imgNoItem:setVisible(false)
    -- 计算页数显示
    local totalCount = event.data.total
    self._maxPage = math.floor(totalCount/LIST_PAGE_SIZE)
    if totalCount % LIST_PAGE_SIZE ~= 0 then
        self._maxPage = self._maxPage + 1
    end
    self._curPage = event.data.page
    self._textCurPage:setString(self._curPage .. "/" .. self._maxPage)
    local transData = self:transUserInfos(event.data.logs)
    self:onUpdateRecordList(transData)
end

-- 获取最近n天开始结束时间戳
function SxvipRecordView:getTimeStamp()
    local timestamps = {}
    local nMap = {7, 3, 1}
    local n = nMap[self._selectDateIndex] or 1
    local currentTime = XH.lobby:getModule("Sxvip"):getSysTime() -- 获取当前时间戳
    local dayStart = os.date("*t", currentTime) -- 获取当天开始时间
    dayStart.hour = 0
    dayStart.min = 0
    dayStart.sec = 0
    local startTimestamp = os.time(dayStart) - 86400 * (n-1) -- 计算开始时间戳
    local endTimestamp = os.time(dayStart) + 86399 -- 结束时间戳为当天23:59:59
    timestamps.start = startTimestamp
    timestamps.endtime = endTimestamp
    return timestamps
end

-- 获取日志选择类型
function SxvipRecordView:getLogSelect()
    local nMap = {0, 1, 4}
    if self._logType == SxvipConfig.OPT.log_send then
        nMap = {0, 2, 3}
    end
    return nMap[self._selectStateIndex] or SxvipConfig.LOGSELECT.None
end

function SxvipRecordView:transUserInfos(data)
    local retData = {}
    local selfNumId = XH.playerData:getNumberID()
    for k, v in pairs(data) do
        local tmpData = {}
        local state = SxvipConfig.RecordState.send
        local userHead = ''
        local userNick = ''
        local userId = 0
        local isSelfOpt = selfNumId == v.userId
        if v.status == SxvipConfig.ServerReplyRecordState.WaitingOrsendOrRefuse then
            state = isSelfOpt and SxvipConfig.RecordState.Waiting or SxvipConfig.RecordState.sendOrRefuse
        elseif v.status == SxvipConfig.ServerReplyRecordState.Agree then
            state = isSelfOpt and SxvipConfig.RecordState.Received or SxvipConfig.RecordState.send;
        elseif v.status == SxvipConfig.ServerReplyRecordState.Refuse then
            state = isSelfOpt and SxvipConfig.RecordState.refuse or SxvipConfig.RecordState.beRefused;
        elseif v.status == SxvipConfig.ServerReplyRecordState.send then
            state = isSelfOpt and SxvipConfig.RecordState.send or SxvipConfig.RecordState.Received;
        end
        userHead = isSelfOpt and v.optUserHead or v.userHead;
        userNick = isSelfOpt and v.optUserNick or v.userNick;
        userId = isSelfOpt and v.optUserId or v.userId;
        tmpData.id = v.id
        tmpData.userId = userId
        tmpData.userNick = userNick
        tmpData.userHead = userHead
        tmpData.state = state
        tmpData.logTime = v.logTime
        tmpData.day = v.day
        table.insert(retData, tmpData)
    end
    return retData
end

function SxvipRecordView:onUpdateRecordList(data)
    if data then
        local index = (self._curPage - 1) * LIST_PAGE_SIZE + 1
        for key, var in pairs(data) do
            local item = self._logItem:clone()
            item:setVisible(true)
            XH.UITool.setText(item, KW_TEXT_NUMBER, index)
            XH.UITool.setText(item, KW_PLAYER_NAME, "昵称：" .. XH.StringTool.cutStringByLength(clone(var.userNick), 12))
            XH.UITool.setText(item, KW_PLAYER_ID, "序号：" .. var.userId)
            XH.UITool.setText(item, KW_RECORD_TIME, os.date("%Y-%m-%d %H:%M", tonumber(var.logTime)))
            XH.UITool.setText(item, KW_RECORD_DAY, var.day .. "天")
            -- 设置头像
            local headNode = XH.UIRemoteImage.create(XH.UITool.seekNodeByName(item, KW_IMG_HEAD))
            if headNode then
                headNode:setSwallowTouches(false)
                headNode:setDefaultTexture(KW_IMG_DEFULT_HEAD, ccui.TextureResType.plistType)
                headNode:setUrl(var.userHead)
            end
            XH.UITool.setVisible(item, KW_BTN_REFUSE, false)
            XH.UITool.setVisible(item, KW_BTN_GIVE, false)
            XH.UITool.setVisible(item, KW_IMG_STATE_BG_RED, false)
            XH.UITool.setVisible(item, KW_IMG_STATE_BG_GRAY, false)
            XH.UITool.setVisible(item, KW_TEXT_WAIT, false)
            XH.UITool.setVisible(item, KW_TEXT_STATE, false)
            if var.state == SxvipConfig.RecordState.send then
                XH.UITool.setVisible(item, KW_IMG_STATE_BG_GRAY, true)
                XH.UITool.setVisible(item, KW_TEXT_STATE, true)
                XH.UITool.setText(item, KW_TEXT_STATE, "已赠送")
            elseif var.state == SxvipConfig.RecordState.Received then
                XH.UITool.setVisible(item, KW_IMG_STATE_BG_RED, true)
                XH.UITool.setVisible(item, KW_TEXT_STATE, true)
                XH.UITool.setText(item, KW_TEXT_STATE, "已领取")
            elseif var.state == SxvipConfig.RecordState.Waiting then
                XH.UITool.setVisible(item, KW_TEXT_WAIT, true)
            elseif var.state == SxvipConfig.RecordState.sendOrRefuse then
                XH.UITool.setVisible(item, KW_BTN_REFUSE, true)
                XH.UITool.setVisible(item, KW_BTN_GIVE, true)
                XH.UITool.addTouchEventListener(item, KW_BTN_GIVE, handler(var, handler(self, self.onBtnSend)))
                XH.UITool.addTouchEventListener(item, KW_BTN_REFUSE, handler(var, handler(self, self.onBtnRefuse)))
            elseif var.state == SxvipConfig.RecordState.refuse then
                XH.UITool.setVisible(item, KW_IMG_STATE_BG_GRAY, true)
                XH.UITool.setVisible(item, KW_TEXT_STATE, true)
                XH.UITool.setText(item, KW_TEXT_STATE, "已拒绝")
            elseif var.state == SxvipConfig.RecordState.beRefused then
                XH.UITool.setVisible(item, KW_IMG_STATE_BG_GRAY, true)
                XH.UITool.setVisible(item, KW_TEXT_STATE, true)
                XH.UITool.setText(item, KW_TEXT_STATE, "被拒绝")
            end
            index = index + 1
            self._listLog:setScrollBarEnabled(false)
            self._listLog:pushBackCustomItem(item)
        end
    end
end

function SxvipRecordView:onBtnSend(data, send, eventType)
    if eventType == ccui.TouchEventType.began then
        send:setScale(0.95)
        send:setColor(cc.c3b(160,160,160))
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setScale(1.0)
        send:setColor(cc.c3b(255, 255, 255))
    end
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyhz24051405, { item_id = data.day })
    -- 赠送之前客户端先判断会员天数是否足够，不足提示
    if not XH.lobby:getModule("Sxvip"):isVipEnoughToSend(data.day) then
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK_CANCEL,
            funcOK = function()
                XH.viewManager:closeView('SxvipGiveView')
                XH.viewManager:closeView('SxvipRecordView')
                XH.viewManager:closeView('SxvipView')
                XH.viewManager:openView("SxvipView", nil, {defalutMenu = SxvipConfig.MENU_DEFINE.BUY_VIP})
            end,
        } , "会员天数不足，是否前往商城补充？")
        return
    end

    -- 会员充足提示
    XH.TipTool.showTip( {
        type = XH.TIP_LAYER_TYPE.OK_CANCEL,
        funcOK = function()
            local param = {}
            param.opt = SxvipConfig.OPT.Agree
            param.log_id = data.id
            XH.lobby:getModule("Sxvip"):reqFellowSend(param)
        end,
    } , string.format("是否确定赠送【%d天会员】给\n【%s】?", data.day, XH.StringTool.cutStringByLength(clone(data.userNick), 12)) )
end

function SxvipRecordView:onBtnRefuse(data, send, eventType)
    if eventType == ccui.TouchEventType.began then
        send:setScale(0.95)
        send:setColor(cc.c3b(160,160,160))
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setScale(1.0)
        send:setColor(cc.c3b(255, 255, 255))
    end
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyhz24051406, { item_id = data.day })
    -- 拒绝
    local param = {}
    param.opt = SxvipConfig.OPT.Refuse
    param.log_id = data.id
    XH.lobby:getModule("Sxvip"):reqFellowSend(param)
end

function SxvipRecordView:onBtnClose(sender, eventType)
    XH.lobby:getModule("Sxvip"):reqVipRedPointCount()
    self:close()
end

function SxvipRecordView:setButtonState(node, enable)
    if not node then
        return
    end
    node:setEnabled(enable)
    node:setTouchEnabled(enable)
    node:setBright(enable)
end

return SxvipRecordView�U  