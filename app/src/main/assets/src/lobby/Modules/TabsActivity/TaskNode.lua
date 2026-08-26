local TaskNode = class("TaskNode",XH.ViewBase)

TaskNode.KW_NAME_EVERY_TASK = "KW_NAME_EVERY_TASK"

local KW_PANEL_TASK_MODEL = "KW_PANEL_TASK_MODEL"
local KW_TEXT_TASK_CONTENT = "KW_TEXT_TASK_CONTENT"
local KW_TEXT_AWARD_NAME = "KW_TEXT_AWARD_NAME"
local KW_IMG_TASK_PROGRESS = "KW_IMG_TASK_PROGRESS"
local KW_IMG_TASK_PROGRESS_BG = "KW_IMG_TASK_PROGRESS_BG"
local KW_TEXT_PROCESS = "KW_TEXT_PROCESS"
local KW_IMG_STATE = "KW_IMG_STATE"
local KW_IMG_LINE = "KW_IMG_LINE"
local KW_IMG_AWARD_BG = "KW_IMG_AWARD_BG"

local TaskState = 
{
    finishNoGetAward = 0,
    noFinish = 1,
    finish = 2
}

local TOAST_SHOW_TIME = 3

function TaskNode:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/TaskSystem.csb"
end

function TaskNode:getBindingInfo()
    return {
        ["_KW_LISTVIEW_TASK_LIST"] = {varName="taskList"},
        ["_KW_TEXT_TIPS"] = {varName="tips"},
    }
end

function TaskNode:ctor(param)
    param = param or {}
    TaskNode.super.ctor(self,param) 
    self.csbNode = self
    self.width = param.width or 1342
	self.height = param.height or 870
	self:setContentSize(self.width,self.height)
	self:setName(self.KW_NAME_EVERY_TASK)
    self._awardImg = {}
    self._closeCallFunc = function()
        XH.lobby:getModule("TabsActivity"):dispatchEvent({name = XH.lobby:getModule("TabsActivity").EVENT_CLOSE_TAB_VIEW})
    end
    XH.lobby:getModule("TabsActivity"):reqWebTaskListData()
end

function TaskNode:getProxyEvents()
    return {
        { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_FLUSH_TASK_LIST", callBack = "onFlushTaskList" },
        { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_WEB_TAST_LIST", callBack = "flushUI" },
        { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_SHARE_TO_SUCCESS", callBack = "onShareSuccessCallBack" },
    }
end

function TaskNode:onFlushTaskList()
    XH.lobby:getModule("TabsActivity"):reqWebTaskListData()
end

function TaskNode:initTouchEvent()
    XH.UITool.addTouchEventListener(self.csbNode, KW_IMG_STATE, handler(self, self.onTouchEventImageState))
end

function TaskNode:onTouchEventImageState(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local name = string.split(send:getParent():getName(), "_")
    local taskIndex = tonumber(name[#name])
    if self._data[taskIndex] then
        local state = self._data[taskIndex].status
        if state == TaskState.noFinish then
            local tmpData = {}
            tmpData.userid = XH.playerData:getNumberID()
            tmpData.taskid = self._data[taskIndex].id
            XH.throwDataManager:throwData(XH.ThrowDataDefine.TASK_NODE_BTN_CLICK_GO, tmpData) 
            XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.TABS_ACTIVITY_JOINACT)
            if self._data[taskIndex].desc_tag == 21 or self._data[taskIndex].desc_tag == 22 then  -- 购买次数 21,购买金额 22
                local ShopConfig = require("lobby.Modules.Shop.Config")
                XH.viewManager:openView("ShopView", nil, {productType = ShopConfig.SpecialTag.DIAMOND})
                if self._closeCallFunc then
                    self._closeCallFunc()
                end
                return
            end
            --去金币场
            if self._data[taskIndex].desc_tag == 20 or self._data[taskIndex].desc_tag == 2 or self._data[taskIndex].desc_tag == 12 then
                XH.lobby:getModule("Lobby"):showXiuXianGame()
                if self._closeCallFunc then
                    self._closeCallFunc()
                end
                return
            end
            --牌友列表任务
            if self:isFriendSysTask(self._data[taskIndex].desc) then
                XH.lobby:getModule("Im"):dispatchEvent({name = XH.lobby:getModule("Im").EVENT_SHOW_LISTLAYER})
                if self._closeCallFunc then
                    self._closeCallFunc()
                end
                return
            end
            if self._data[taskIndex].desc_tag == 23 then
                self:shareToMoment()
                return
            end
            --还要判断一下roommode 如果有明确的 需要直接跳转，没有明确的走下面
            local parme  = {}
            parme.type = "doTask"
            XH.viewManager:openView("TabsActivityMesBox", nil, parme)
        elseif state == TaskState.finishNoGetAward then
            if self:realName() then
                return
            end
            XH.TipTool.showLoading()
            --请求task发奖
            local ReqTaskProtocol = require("app.Req.Task.ReqTaskProtocol")
            local reqTaskProtocol = ReqTaskProtocol:new()
            reqTaskProtocol:addReqCallBack(self, self.onRespTaskAwardCallBack)
            local TaskDefine =  require("app.Define.TaskDefine")
            local wid = XH.areaData:getAreaWindID()
            local str = ";id = " .. self._data[taskIndex].id .. ";wid = " .. wid ..";channel = " .. XH.SysTool:getChannelID()
            reqTaskProtocol:start(TaskDefine.TASK_ID.TASK_TYPE_MISSION_SYS_AWARD_NEW, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, str)
            local tmpData = {}
            tmpData.userid = XH.playerData:getNumberID()
            tmpData.taskid = self._data[taskIndex].id
            XH.throwDataManager:throwData(XH.ThrowDataDefine.TASK_NODE_BTN_CLICK_GET, tmpData) 
            XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.TABS_ACTIVITY_JOINACT)

        end
    end
end
--实名判断
function TaskNode:realName() 
    local year = tonumber(os.date('%Y')) 
    local moth = tonumber(os.date('%m'))
    local day = tonumber(os.date('%d'))
    if year == 2021 and moth == 6 and day >= 17 and day <= 30 then
        --弹实名认证
        if XH.playerData:getIdentityStatus() == 2 then
            XH.viewManager:openView("IdentityCheckView",nil,{isAutoBindPhone = true})
            return true
        end
    end
    return false
end
function TaskNode:onRespTaskAwardCallBack(cell, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        if data.acData and data.acData ~= "" then 
            local respData = json.decode(data.acData)
            if respData.ret and respData.ret == 0 then
                XH.TipTool.showToast("领奖失败，请确认您是否已经领取或者未完成任务", TOAST_SHOW_TIME)
            else
                local parme  = {}
                parme.type = "getAward"
                parme.data = json.decode(data.acData)
                XH.viewManager:openView("TabsActivityMesBox", nil, parme)
            end
        end
    end
end

function TaskNode:flushUI(event)
    local data = event.data
    if #data == 0 then
        self.tips:setVisible(true)
    end
    local taskList = self.taskList
    taskList:removeAllChildren()
    if not taskList then
        return
    end

    --比赛场内不显示金币场任务
    if XH.SceneManager:getInstance():getTopSceneTag() == XH.SCENE_TAG.TEAHOUSE then
        local tmpData = {}
        for i = 1, #data do
            local desc_tag = data[i].desc_tag
            if desc_tag ~= 20 and desc_tag ~= 12 and desc_tag ~= 2 then
                table.insert(tmpData, data[i])
            end
        end
        data = tmpData
    end

    self._data = data
    local listHeight = 20
    for i = 1, #data do
        local taskModel = XH.UITool.seekNodeByName(self.csbNode, KW_PANEL_TASK_MODEL)
        local taskModelClone = nil
        if taskModel then
            taskModelClone = taskModel:clone()
        end
        if taskModelClone then
            taskModelClone:setName("task_" .. i)
            local awardName = XH.UITool.seekNodeByName(taskModelClone, KW_TEXT_AWARD_NAME)
            if awardName then
                awardName:setText(data[i].award_desc)
            end
            --设置奖励图片
            local awardBg = XH.UITool.seekNodeByName(taskModelClone, KW_IMG_AWARD_BG)
            if awardBg then
                self:updateImg(i, awardBg, data[i].img)
            end
            --设置任务名字
            local taskContent = XH.UITool.seekNodeByName(taskModelClone, KW_TEXT_TASK_CONTENT)
            if taskContent then
                taskContent:setText(data[i].desc)
            end
            --设置进度条
            local progressBg = XH.UITool.seekNodeByName(taskModelClone, KW_IMG_TASK_PROGRESS_BG)
            local progress = XH.UITool.seekNodeByName(taskModelClone, KW_IMG_TASK_PROGRESS)
            if progressBg and progress then
                local width = math.ceil(progressBg:getContentSize().width / data[i].count * data[i].finishcnt)
                if width > progressBg:getContentSize().width then
                    width = progressBg:getContentSize().width
                end
                progress:setContentSize(width, progress:getContentSize().height)
            end
            --设置进度文字
            local progressText = XH.UITool.seekNodeByName(taskModelClone, KW_TEXT_PROCESS)
            if progressText then
                progressText:setText(data[i].finishcnt .. "/" .. data[i].count)
            end
            --设置状态
            local stateImg = XH.UITool.seekNodeByName(taskModelClone, KW_IMG_STATE)
            if stateImg then
                local state = data[i].status
                if state == TaskState.noFinish then
                    stateImg:loadTexture("task_system_go_task.png", ccui.TextureResType.plistType)
                elseif state == TaskState.finishNoGetAward then
                    stateImg:loadTexture("task_system_get_award.png", ccui.TextureResType.plistType)
                elseif state == TaskState.finish then
                    stateImg:loadTexture("task_system_task_have_award.png", ccui.TextureResType.plistType)
                end
            end
            XH.UITool.addTouchEventListener(taskModelClone, KW_IMG_STATE, handler(self, self.onTouchEventImageState))
            --设置分割线
            local line = XH.UITool.seekNodeByName(taskModelClone, KW_IMG_LINE)
            if line then
                line:setVisible(i ~= data)
            end
            taskList:addChild(taskModelClone)
            listHeight = listHeight + taskModelClone:getContentSize().height
        end
    end
    if listHeight > taskList:getContentSize().height then
        taskList:setInnerContainerSize(cc.size(taskList:getContentSize().width, listHeight))
    end
end

function TaskNode:updateImg(i, node, url)
    if not url then
        return
    end
    local size = node:getContentSize()
    local imageNode = XH.RemoteImage.new()
        :setTouchEnabled(true)
        :setPosition(size.width / 2, size.height / 2)
        :setContentSize(size.width, size.height)
        :addTo(node)

    imageNode:setMaxContentSize(size)
    imageNode:setUrl(url, true)
end

function TaskNode:setCloseNodeCallFunc(closeCallFunc, weakSelf)
    self._closeCallFunc = function () closeCallFunc(weakSelf) end
end

function TaskNode:shareToMoment()
    XH.lobby:getModule("TabsActivity"):onShareToMomentByTask()
end

function TaskNode:onShareSuccessCallBack()
    XH.taskManager:reqShareGiveRoomCard() 
    if self._closeCallFunc then
        self._closeCallFunc()
    end
end

function TaskNode:isFriendSysTask(taskDesc)
    if not taskDesc or taskDesc == "" then
        return false
    end
    local keyWord = {"牌友", "好友"}
    for _, v in pairs(keyWord) do
        if string.find(taskDesc, v) then
            return true
        end
    end
    return false
end

return TaskNode   �.  