local ConsumeActView = class("ConsumeActView", XH.ViewBase)
local FourToTwoConfig = require("lobby.Config.FourToTwoActConfig")
local MIN_PRO_NODE_NUM = 4

local KW_AIM_STATE = "KW_AIM_STATE"
local KW_AWARD_NUM = "KW_AWARD_NUM"
local KW_AWARD_ALREADY = "KW_AWARD_ALREADY"
local KW_IMG_LIGHT = "KW_IMG_LIGHT"
local KW_AWARD_IMG = "KW_AWARD_IMG"
local KW_AWARD_NODE = "KW_AWARD_NODE"
local KW_AWARD_CARD_NUM = "KW_AWARD_CARD_NUM"

function ConsumeActView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/BusinessAct/ConsumeAct/ConsumeActMain.csb"
end

function ConsumeActView:getBindingInfo()
    return {
        ["_KW_BTN_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KW_BTN_RECORDS"] = { varName = "_btnGoRecords", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventRecords" },
        ["_KW_BTN_ACT_HELP"] = { varName = "_btnActHelp", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventActHelp" },
        ["_KW_BTN_ALL_PRIZES"] = { varName = "_btnGetAllPrize", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventGetAllPrizes" },
        ["_KW_BTN_ACT_GO"]= { varName = "_btnGoAct", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventGoAct" },
        ["_KW_ACT_PROCESS"] = {varName = "_process"},
        ["_KW_TEXT_DEADLINE"] = { varName = "_deadLineTime" },
        ["_KW_TEXT_CUR_CONSUME"] = {varName = "_curConsume"},
        ["_KW_ACT_PRO_NODE"] = {varName = "_processNode"},
        ["_KW_ACT_AWARD_NODE"] = {varName = "_awardNode"},
        ["_KW_POS_START"] = {varName = "_nodeStartPos"},
        ["_KW_POS_END"] = {varName = "_nodeEndPos"},
        ["_KW_ACT_CONTENT"] = {varName = "_actContent"},
		["_KW_TEXT_REFUSH"] = {varName = "_refushText"},
    }
end

function ConsumeActView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("TeaHouseBusinessAct"), eventKeyName = "EVENT_TEACONSUME_UPDATE", callBack = "onEventUpdateActUI" },
        { module = XH.lobby:getModule("TeaHouseBusinessAct"), eventKeyName = "EVENT_TEACONSUME_COST", callBack = "onEventUpdateCost" },
        { module = XH.lobby:getModule("TeaHouseBusinessAct"), eventKeyName = "EVENT_AWARD_CALLBACK", callBack = "onEventUpdateAward" },
        
    }
end

function ConsumeActView:ctor(param)
    ConsumeActView.super.ctor(self,param)
    self._processNodeList = {}
    self._awardNodeList = {}
    self._maxMissionCount =0
    self._missionData = {}
    self._cardCost = 0
    --请求活动数据
    XH.lobby:getModule("TeaHouseBusinessAct"):reqConsumeActData()
end

function ConsumeActView:onTouchEventClose(send, eventType)
    self:close()
end

function ConsumeActView:onTouchEventRecords(send, eventType)
    XH.viewManager:openView("AwardCenterView")
end

function ConsumeActView:onTouchEventActHelp(send, eventType)
    XH.viewManager:openView("TeaHouseActRule")
end

function ConsumeActView:onTouchEventGoAct(send, eventType)
    XH.lobby:getModule("TeaHouseBusinessAct"):onTouchShare()
    --北极星数据统计
    local data = {}
    data.userid = XH.playerData:getNumberID()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.BusinessConsumeActGo,data)
end

function ConsumeActView:onEventUpdateActUI(event)
    if event.data then
        self:updateConsumeActUI(event.data)
    end
end



function ConsumeActView:onEventUpdateCost(event)
    if event.data then
        self._cardCost = tonumber(event.data)
        if FourToTwoConfig.AreaConfig[XH.areaData:getLobbyID()].TeaHouseOpen then
            self._curConsume:setString(string.format( "当前2人消耗：%.1f",self._cardCost))
        else
            self._curConsume:setString(string.format( "当前消耗：%.1f",self._cardCost))
        end
        for i =1,#self._processNodeList do
            local num = XH.UITool.getText(self._processNodeList[i],KW_AWARD_NUM)
            if tonumber(num) <= self._cardCost then
                XH.UITool.setButtonState(self._processNodeList[i],KW_AIM_STATE,true)
            end
        end
        for i =1,#self._awardNodeList do
            local num = XH.UITool.getText(self._awardNodeList[i],KW_AWARD_NUM)
            if tonumber(num) <= self._cardCost and not XH.UITool.isVisible(self._awardNodeList[i],KW_AWARD_ALREADY) then
                XH.UITool.setVisible(self._awardNodeList[i],KW_IMG_LIGHT,true)
                self._awardNodeList[i]:setTouchEnabled(true)
            end
        end
        self._process:setPercent(self._cardCost / self._maxMissionCount *100)
    end
end

function ConsumeActView:onEventUpdateAward(event)
    if event.data then
        if event.data.missionData then
            self._missionData = event.data.missionData
            for i = 1,#self._missionData do
                if self._missionData[i].isAwardGot then
                    for j =1,#self._awardNodeList do
                        if self._awardNodeList[j]:getTag() == self._missionData[i].id then
                            XH.UITool.setVisible(self._awardNodeList[j],KW_IMG_LIGHT,false)
                            XH.UITool.setVisible(self._awardNodeList[j],KW_AWARD_ALREADY,true)
                        end
                    end
                end
            end
        end
        --显示奖励动画
        if event.data.awards then
            local awards = {}
            for i=1,#event.data.awards do
                local aw = {}
                aw.url = event.data.awards[i].image_url
                aw.desc = event.data.awards[i].prize_name
                table.insert(awards,aw)
            end
            if next(awards) then
                XH.teaHouseManager:refreshTeaHouseProp()
                XH.viewManager:openView("CommonRewardView", nil,awards,false)
            end
        end
    end
end

function ConsumeActView:updateConsumeActUI(data)
    if not data then
        return 
    end
    --往界面上添加节点,不足4个补足到4个节点
    if data.missionData then
        --请求消耗
        self._missionData = data.missionData
        local maxEndTime = 0
        local minStartTime = 1927036800 -- 20310125
        local displayMissions = {}
        for i =1,#data.missionData do
            if data.missionData[i].desc_tag == XH.lobby:getModule("TeaHouseBusinessAct").TeaHouseMissionList["CONSUME"] then
                if data.missionData[i].count > self._maxMissionCount then
                    self._maxMissionCount = data.missionData[i].count 
                end
                if self._missionData[i].endTime and tonumber(self._missionData[i].endTime) > tonumber(maxEndTime) then
                    maxEndTime = self._missionData[i].endTime
                end
                if self._missionData[i].startTime and tonumber(self._missionData[i].startTime) < tonumber(minStartTime) then
                    minStartTime = self._missionData[i].startTime
                end
                displayMissions[#displayMissions + 1] = data.missionData[i]
            end
        end
        local formatDate = function(time)
            local year = os.date("%Y",time)
            local month = os.date("%m",time)
            local day = os.date("%d",time)
            return year .. month .. day
        end
		local showDay = function(time)
            local day = math.floor(time/(24*60*60))
			time = time - day*(24*60*60)
			local hour = math.floor(time/(60*60))
			time = time - hour*(60*60)
			local minute = math.floor(time/(60))
			if day > 0 then
				return day.."天"..hour.."小时"
			elseif hour > 0 then
				return hour.."小时"..minute.."分"
			elseif minute > 0 then
				return minute.."分"
			else
				return "小于1分钟"
			end
        end
        local webStart = self:chageTimeFormatWeb(formatDate(data.missionData[1].wStartTime))
        local webEnd = self:chageTimeFormatWeb(formatDate(data.missionData[1].wEndTime))
        --XH.TipTool.showToast("任务时间：" .. os.date("%Y-%m-%d %H:%M:%S",tonumber(data.missionData[1].wStartTime)) .. "  " .. os.date("%Y-%m-%d %H:%M:%S",tonumber(data.missionData[1].wEndTime)), 3)
		local stattime = os.date("%Y年%m月%d日",minStartTime)--self:chageTimeFormat(formatDate(minStartTime)) 
        local endtime = os.date("%Y年%m月%d日",maxEndTime)---self:chageTimeFormat(formatDate(maxEndTime + 7*24*3600))		
		if data.missionData[1].isNew == 1 then        
			self._refushText:setString("刷新时间：" .. showDay(data.missionData[1].exTime))
			if data.missionData[1].exTime < 24*60*60 then
				XH.TipTool.showToast("活动刷新刷新时间不足一天，请注意奖励领取！")
			end
			stattime = self:chageTimeFormat(formatDate(data.missionData[1].wStartTime)) 
			endtime = self:chageTimeFormat(formatDate(data.missionData[1].wEndTime))
		else
			self._refushText:setVisible(false)
		end
        --XH.lobby:getModule("TeaHouseBusinessAct"):reqTeaConsumeCost(webStart,webEnd)
        XH.lobby:getModule("TeaHouseBusinessAct"):reqTeaMoreCost(webStart,webEnd)
        self._deadLineTime:setText(string.format("活动时间：%s - %s",stattime,endtime))
        local nodeNum = #displayMissions
        local startPos,node_y = self._nodeStartPos:getPosition()
        local endPos = self._nodeEndPos:getPosition()
        if nodeNum == 1 then
            --只有一个任务
            local distace = (endPos - startPos) / MIN_PRO_NODE_NUM
            --添加3个假节点到进度条上，界面更具有美感
            for i = 1,MIN_PRO_NODE_NUM-1 do
                local node = self:setProcessNodeUI(math.floor(i*self._maxMissionCount/MIN_PRO_NODE_NUM))
                node:setPosition(startPos + distace * i,node_y)
                self._actContent:addChild(node)
                table.insert( self._processNodeList, node)
            end
            --添加任务节点到进度条上
            local award = self:setAwardNodeUI(displayMissions[1])
            award:setPosition(endPos,node_y)
            self._actContent:addChild(award)
            table.insert( self._awardNodeList, award)
        else
            --多节点从小到大排序
            table.sort(displayMissions, function(v1,v2)
                return v1.count < v2.count
            end )
            for i =1 ,nodeNum do
                local distace = (endPos - startPos) / displayMissions[nodeNum].count * displayMissions[i].count
                --添加任务节点到进度条上
                local award = self:setAwardNodeUI(displayMissions[i])
                award:setPosition(startPos+distace,node_y)
                self._actContent:addChild(award)
                table.insert( self._awardNodeList, award)
            end
            --当运营配置2个任务且这两个任务是倍数关系
            if nodeNum == 2 and displayMissions[1].count * 2 == displayMissions[2].count then 
                --添加2个假节点到进度条上
                local distaceNode = (endPos - startPos) / MIN_PRO_NODE_NUM
                for i = 1,MIN_PRO_NODE_NUM - 1 do
                    if i ~= 2 then
                        local node = self:setProcessNodeUI(math.floor(i*self._maxMissionCount/MIN_PRO_NODE_NUM))
                        node:setPosition(startPos + distaceNode * i,node_y)
                        self._actContent:addChild(node)
                        table.insert( self._processNodeList, node)
                    end
                end
            end
        end
    end
end

function ConsumeActView:setAwardNodeUI(displayMission)
    local award = self._awardNode:clone()
    award:setTag(displayMission.id)
    award:addTouchEventListener(handler(self,self.onTouchGetSingleAward))
    XH.UITool.setText(award,KW_AWARD_NUM,displayMission.count)
    XH.UITool.setVisible(award,KW_AWARD_ALREADY,displayMission.isAwardGot)
    XH.UITool.setText(award,KW_AWARD_CARD_NUM,"x" ..displayMission.award.prop_value)
    if displayMission.award.prize_type == "teahouse_card" then
        XH.UITool.setVisible(award,KW_AWARD_IMG,true)
    else
        local node =  XH.UITool.seekNodeByName(award,KW_AWARD_NODE)
        if node then
            local posx,posy = XH.UITool.seekNodeByName(award,KW_AWARD_IMG):getPosition()
            local img = XH.RemoteImage.new()
            :setPosition(posx,posy)
            :addTo(node)
            :setMaxContentSize(node:getContentSize())
            img:setUrl(displayMission.award.image_url)
        end
    end
    return award
end

function ConsumeActView:setProcessNodeUI(aim)
    local node = self._processNode:clone()
    XH.UITool.setButtonState(node,KW_AIM_STATE,false)
    XH.UITool.setText(node,KW_AWARD_NUM,aim)
    return node
end

function ConsumeActView:btnState(send, eventType)
    if eventType == ccui.TouchEventType.began then
        XH.audioManager:play("BUTTON_CLICK")
        send:setColor(cc.c3b(160,160,160))
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setColor(cc.c3b(255, 255, 255))
    end
end

function ConsumeActView:onTouchGetSingleAward(send,eventType)
    self:btnState(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    --北极星数据统计
    local data = {}
    data.userid = XH.playerData:getNumberID()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.BusinessConsumeActAward,data)
    local id = send:getTag() 
    local missison =self:findMissionById(self._missionData,id)
    if  missison.isAwardGot then
        if self:isAllAwardGot(self._missionData) then
            XH.TipTool.showToast("恭喜您已完成所有任务~", 3)
        else
            XH.TipTool.showToast("已领取奖励，继续加油哦~", 3)
        end
    elseif missison.isAwardGot == false then
        if self._cardCost >= missison.count then
            XH.lobby:getModule("TeaHouseBusinessAct"):reqGetConsumeActAward(self._cardCost,id)
        else
            XH.TipTool.showToast("先完成任务才能领取奖励哦~", 3)
        end
    end
end

function ConsumeActView:findMissionById(missions,id)
    if missions then
        for _,v in pairs (missions) do
            if tonumber(v.id) == id then
                return v
            end
        end
    end
    return nil
end

function ConsumeActView:isAllAwardGot(missions)
    if missions then
        for _,v in pairs (missions) do
            if v.isAwardGot == false then
                return false
            end
        end
    end
    return true
end

function ConsumeActView:onTouchEventGetAllPrizes(send, eventType)
    --北极星数据统计
    local data = {}
    data.userid = XH.playerData:getNumberID()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.BusinessConsumeActAward,data)
    local canGetPrize = 0
    local haveGot = 0
    for i =1,#self._missionData do
        if self._cardCost >= self._missionData[i].count  then
            canGetPrize = canGetPrize + 1
        end
        if  self._missionData[i].isAwardGot then
            haveGot = haveGot + 1
        end
    end
    if canGetPrize == 0 then
        XH.TipTool.showToast("先完成任务才可以领取奖励哦~", 3)
        return
    end
    if haveGot == canGetPrize then
        if self._cardCost >= self._maxMissionCount then
            XH.TipTool.showToast("恭喜您已完成所有任务~", 3)
        else
            XH.TipTool.showToast("先完成任务才可以领取奖励哦~", 3)
        end
        return
    end
    XH.lobby:getModule("TeaHouseBusinessAct"):reqGetConsumeActAward(self._cardCost)
end

function ConsumeActView:chageTimeFormat(time)
    local year = string.sub(time,1,4)
    local month = string.sub(time,5,6)
    local day = string.sub(time,7,8)
    return year .. "年" .. month .. "月" .. day .. "日"
end

function ConsumeActView:chageTimeFormatWeb(time)
    local year = string.sub(time,1,4)
    local month = string.sub(time,5,6)
    local day = string.sub(time,7,8)
    return year .. "-" .. month .. "-" .. day
end

return ConsumeActView
 �?  