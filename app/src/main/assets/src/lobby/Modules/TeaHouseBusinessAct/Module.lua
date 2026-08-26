local BusinessActModule = class("BusinessActModule", XH.ModuleBase)
local TaskDefine = require("app.Define.TaskDefine")
local TeaHouseBusinessActConfig = require("lobby.Modules.TeaHouseBusinessAct.Config")
local FourToTwoConfig = require("lobby.Config.FourToTwoActConfig")

BusinessActModule.RECALL_ACT_TEAID_KEY_WITH_NUMBER_ID = "RECALL_ACT_TEAID_KEY_WITH_NUMBER_ID"
BusinessActModule.RECALL_ACT_IGNORE_KEY_WITH_NUMBER_ID = "RECALL_ACT_IGNORE_KEY_WITH_NUMBER_ID"

BusinessActModule.EVENT_TEAOWNER_MSG = "EVENT_TEAOWNER_MSG"
BusinessActModule.EVENT_REFRESH_CHOSE_STATE = "EVENT_REFRESH_CHOSE_STATE"
BusinessActModule.EVENT_TEAOWNER_AWARD_REFRESH = "EVENT_TEAOWNER_AWARD_REFRESH"

BusinessActModule.EVENT_TEACONSUME_UPDATE = "EVENT_TEACONSUME_UPDATE"
BusinessActModule.EVENT_TEACONSUME_COST = "EVENT_TEACONSUME_COST"
BusinessActModule.EVENT_AWARD_CALLBACK = "EVENT_AWARD_CALLBACK"
BusinessActModule.EVENT_TEAHOUSE_ACT_SHOW_TYPE = "EVENT_TEAHOUSE_ACT_SHOW_TYPE"

BusinessActModule.DEFAULT_PEOPLE_HEAD = "http://palmastatic.gaoshiwang.cn//materials/images/20201124/11104959697.png"

BusinessActModule.ActivityType = {
    ActivityRecall = 1, --召回活动
	ActivityConsume = 2, -- 消耗活动
}

-- 请求奖励
BusinessActModule.KW_GET_PRIZE_STATE = {
	GET_PERCENT_PRIZE = 1,
	GET_ALL_PRIZE = 2
}

-- 请求TASK数据信息
local KW_REQ_MODE = {
	PERSON_MSG = 1,
	GET_PRIZE = 2,
    GET_IS_ACTIVITY_TIME = 3
}

-- 设置记忆领取模式
BusinessActModule.KW_SET_CHOSE_STATE = {
	GET_OWN_CARDS = 1,
	GET_TEAHOUSE_CARDS = 2
}

local KW_CONSUME_REQ_MODE = {
    --获取活动状态
    REQ_ACT_OPEN = 1,
    --获取活动数据
    REQ_ACT_INFO = 2,
    --获取单个奖励
    REQ_ACT_SINGLE_AWARD = 3,
    --获取全部奖励
    REQ_ACT_ALL_AWARD = 4,
}

BusinessActModule.TeaHouseMissionList = {
    --单个比赛场消耗
    ["CONSUME"] = 1000,
    -- 单个比赛场活跃玩家数
    ["ACTIVE"] = 1001,
}

function BusinessActModule:ctor()
    BusinessActModule.super.ctor(self)
    self._nTeaid = 0
    self._nReqIndex = 0
    self._teaOwnerMsg = {}
    self._needRecalLists = {}
    self._allTeaPersonMsg = {}
    self._actShowType = {
        TeaHouseRecallAct = false,
        TeaHouseConsumeAct = false,
    }
end

function BusinessActModule:getReqConfig()
    return {
        ReqTeaOwnerMsg = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onReqTeaOwnerMsgCallBack },
        ReqGetPrize = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onReqGetPrizeCallBack },
        ReqGetActTime = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onReqGetActTimeCallBack },
        ReqGetConsumeAct = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onReqGetConsumeActCallBack },
        ReqGetConsumeActAward = { reqPath = "app.Req.Task.ReqTaskProtocol", callBack = self.onReqGetConsumeAwardCallBack },
        ReqTeaRecallsList = { reqPath = "lobby.Req.TeaHouseBusinessAct.ReqTeaHouseRecallLists", callBack = self.onReqTeaRecallsListCallBack },
        ReqTeaConsumeCost = { reqPath = "lobby.Req.TeaHouseBusinessAct.ReqTeaHouseConsumeCost", callBack = self.onReqTeaConsumeCostCallBack },
        ReqTeaMoreCost = { reqPath = "lobby.Req.TeaHouseBusinessAct.ReqTeaHouseMoreCost", callBack = self.onReqTeaMoreCostCallBack },
    }
end

function BusinessActModule:openBusinessAct(actName, var1, var2)
    if self._actShowType[actName] or actName == "allView" then
        XH.viewManager:openView("TeaHouseBusinessActView",var1,var2)
    else
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        },"活动暂未开启，敬请期待")
    end
end

-- 请求奖励
function BusinessActModule:reqGetPrize(state, level)
    XH.TipTool.showLoading()
    local getPrizeOrder = self:getNumberWithTeaidKey()
    local recallCount = #self._teaOwnerMsg.recallLists or 0
    local dataString = ";nReqPoolId = " .. KW_REQ_MODE.GET_PRIZE .. ";nGetPrizeState=" .. state .. ";nLevel=" .. level .. ";nChose=" .. getPrizeOrder .. ";nTeaid=" .. self._nTeaid .. ";nCount=" .. recallCount
    self:startReq("ReqGetPrize", TaskDefine.TASK_ID.TASK_TYPE_TEAHOUSE_RECALL_LISTS, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
end

function BusinessActModule:onReqGetPrizeCallBack(cell, cellType, data)
    XH.TipTool.hideLoading()
    if cellType == XH.Req.TYPE.SUCCESS then
        data = data.acData
        if data and data ~= "" then
            local jsonData
            local ok = pcall( function()
                jsonData = cjson.decode(data)
            end )
            if not ok then
                return
            end
            if jsonData.errCode == 1 then
                XH.TipTool.showToast("领取奖励失败，确认是否已领取过~", 3)
            else
                local prizeList = {}
                if jsonData and #jsonData.prizeLists > 0 then
                    for i = 1, #jsonData.prizeLists do
                        prizeList[#prizeList+1] = {}
                        prizeList[#prizeList].cnt = jsonData.prizeLists[i]
                        prizeList[#prizeList].type = XH.areaData:getPropRoomCardID()
                        prizeList[#prizeList].desc = jsonData.nameLists[i]
                        prizeList[#prizeList].url = jsonData.urlLists[i]
                    end
                    XH.viewManager:openView("CommonRewardView", nil, prizeList)
                else
                    XH.TipTool.showToast("先完成任务才可以领取奖励哦~", 3)
                end
            end
            self._teaOwnerMsg.teaOwnerPer = jsonData.allLists
            self:dispatchEvent( { name = BusinessActModule.EVENT_TEAOWNER_AWARD_REFRESH })
        end
    end
end

-- 请求领队信息
function BusinessActModule:reqPersonMsg()
    --XH.TipTool.showLoading()
    self._teaOwnerMsg = {}
    local reqLists = "{" .. table.concat(self._needRecalLists,",") .. "}"
    local dataString = ";nReqPoolId = " .. KW_REQ_MODE.PERSON_MSG .. ";nTeaid=" .. self._nTeaid .. ";nNumids = " .. reqLists
    self:startReq("ReqTeaOwnerMsg", TaskDefine.TASK_ID.TASK_TYPE_TEAHOUSE_RECALL_LISTS, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
end

function BusinessActModule:onReqTeaOwnerMsgCallBack(cell, cellType, data)
    --XH.TipTool.hideLoading()
    if cellType == XH.Req.TYPE.SUCCESS then
        data = data.acData
        if data and data ~= "" then
            local jsonData
            local ok = pcall( function()
                jsonData = cjson.decode(data)
            end )
            if not ok then
                return
            end
            self._teaOwnerMsg = jsonData
            self:dispatchEvent( { name = BusinessActModule.EVENT_TEAOWNER_MSG, data = jsonData })
        end
    end
end

-- 请求领取的key值
function BusinessActModule:setNumberWithTeaidKey(Value)
    local key = BusinessActModule.RECALL_ACT_TEAID_KEY_WITH_NUMBER_ID .. "-" .. self._nTeaid .. "-" .. XH.playerData:getNumberID()
    cc.UserDefault:getInstance():setIntegerForKey(key, Value or 0)
    self:dispatchEvent( { name = BusinessActModule.EVENT_REFRESH_CHOSE_STATE, data = Value })
end

-- 返回领取的key值
function BusinessActModule:getNumberWithTeaidKey()
    local key = BusinessActModule.RECALL_ACT_TEAID_KEY_WITH_NUMBER_ID .. "-" .. self._nTeaid .. "-" .. XH.playerData:getNumberID()
    return cc.UserDefault:getInstance():getIntegerForKey(key, 0)
end

-- 请求下次不再提示的key值
function BusinessActModule:setIgnoreTeaidKey(Value)
    local key = BusinessActModule.RECALL_ACT_IGNORE_KEY_WITH_NUMBER_ID .. "-" .. self._nTeaid .. "-" .. XH.playerData:getNumberID()
    cc.UserDefault:getInstance():setIntegerForKey(key, Value or 0)
    self:dispatchEvent( { name = BusinessActModule.EVENT_REFRESH_CHOSE_STATE, data = Value })
end

-- 返回下次不再提示的key值
function BusinessActModule:getIgnoreWithTeaidKey()
    local key = BusinessActModule.RECALL_ACT_IGNORE_KEY_WITH_NUMBER_ID .. "-" .. self._nTeaid .. "-" .. XH.playerData:getNumberID()
    return cc.UserDefault:getInstance():getIntegerForKey(key, 0)
end

-- 设置该比赛场待召回玩家
function BusinessActModule:setNeedRecallLists(value)
    self._needRecalLists = value or {}
end

function BusinessActModule:getNeedRecallLists()
    return self._needRecalLists
end

function BusinessActModule:getTeaOwnerMsg()
    return self._teaOwnerMsg
end

function BusinessActModule:setReqTeaNumber(value)
    self._nTeaid = value
end

-- WEB请求玩家列表
function BusinessActModule:reqUnRecallsLists()
    XH.TipTool.showLoading()
    local reqAid = self._recallActWebAid or TeaHouseBusinessActConfig.aidConfig[XH.areaData:getLobbyID()]
    if reqAid then
        self._allTeaPersonMsg = {}
        self:startReq("ReqTeaRecallsList", reqAid, self._nTeaid, 15)
    end
end

function BusinessActModule:onReqTeaRecallsListCallBack(cell, cellType, data)
    XH.TipTool.hideLoading()
    if cellType == XH.Req.TYPE.SUCCESS then
        if data and type(data) == "table" then
            self._needRecalLists = { }
            for i = 1, #data do
                self._needRecalLists[#self._needRecalLists + 1] = data[i].numid
                self._allTeaPersonMsg[data[i].numid] = { }
                self._allTeaPersonMsg[data[i].numid].nickName = data[i].nickname
                self._allTeaPersonMsg[data[i].numid].avarUrl = data[i].avatar
            end
            self:reqPersonMsg()
        end
    end
end

function BusinessActModule:getTeaNickWithUrl()
    return self._allTeaPersonMsg
end

function BusinessActModule:reqConsumeActData()
    XH.TipTool.showLoading()
    local dataString  = string.format(";nReqMode=%s;nWid=%s;nChannel=%s;nTeaID=%s;nMissionId=%s",KW_CONSUME_REQ_MODE.REQ_ACT_INFO,XH.areaData:getAreaWindID(),XH.areaData:getChannelID(),self._nTeaid,self.TeaHouseMissionList.CONSUME)
    self:startReq("ReqGetConsumeAct", TaskDefine.TASK_ID.TASK_TYPE_TEAHOUSE_CONSUEM_ACT, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
end

function BusinessActModule:onReqGetConsumeActCallBack(cell, cellType, data)
    --XH.TipTool.hideLoading()
    if cellType == XH.Req.TYPE.SUCCESS then
        data = data.acData
        if data and data ~= "" then
            local jsonData
            local ok = pcall( function()
                jsonData = cjson.decode(data)
            end )
            if not ok then
                return
            end
            self:dispatchEvent( { name = BusinessActModule.EVENT_TEACONSUME_UPDATE, data = jsonData })
        end
    end
end

-- WEB请求比赛场消耗
function BusinessActModule:reqTeaConsumeCost(startTime,endTime)
    --XH.TipTool.showLoading()
    self:startReq("ReqTeaConsumeCost",self._nTeaid,startTime,endTime, 15)
end

function BusinessActModule:onReqTeaConsumeCostCallBack(cell, cellType, data)
    XH.TipTool.hideLoading()
    if cellType == XH.Req.TYPE.SUCCESS then
        if data and type(data) == "table" then
            if XH.areaData:getLobbyID() == XH.LOBBY_ID.WENZHOU then
                self:dispatchEvent( { name = BusinessActModule.EVENT_TEACONSUME_COST, data = data.cost - data.player_cost })
            else
                self:dispatchEvent( { name = BusinessActModule.EVENT_TEACONSUME_COST, data = data.cost })
            end
        end
    end
end

-- WEB请求比赛场消耗
function BusinessActModule:reqTeaMoreCost(startTime,endTime)
    --XH.TipTool.showLoading()
    self:startReq("ReqTeaMoreCost",self._nTeaid, startTime, endTime, 15)
end

function BusinessActModule:onReqTeaMoreCostCallBack(cell, cellType, data)
    XH.TipTool.hideLoading()
    if cellType == XH.Req.TYPE.SUCCESS then
        if data and type(data) == "table" then
            if FourToTwoConfig.AreaConfig[XH.areaData:getLobbyID()].TeaHouseOpen then
                local two_cost = 0
                --local two_player_cost = 0
                for _, info in pairs(data.detail) do
                    if info.player_num == 2 then
                        two_cost = info.cost
                        --two_player_cost = info.player_cost
                    end
                end
                self:dispatchEvent( { name = BusinessActModule.EVENT_TEACONSUME_COST, data = two_cost })
            elseif XH.areaData:getLobbyID() == XH.LOBBY_ID.WENZHOU then
                self:dispatchEvent( { name = BusinessActModule.EVENT_TEACONSUME_COST, data = data.cost - data.player_cost })
            else
                self:dispatchEvent( { name = BusinessActModule.EVENT_TEACONSUME_COST, data = data.cost })
            end
        end
    end
end

function BusinessActModule:onTouchShare()
    if device.platform == "windows" then
        return
    end
    local url = XH.areaData:getXHLinkURL()
    local shareText = "马上开局就缺你了，您的好友【" .. XH.playerData:getNickName() .. "】邀请您上线游戏！"
    local shareInfo = {
        textTitle = "好友邀请通知", --分享标题
        textTitleUrl = "", --微信不用
        site = "", --微信不用
        siteUrl = "", --微信不用
        imagePath = "", --微信不用
        url = url, --微信网页模式分享的链接
        type = "url", --word是文字分享，url网页模式分享，pic分享图片
        scene = "0", --0是分享到好友会话，1是分享到朋友圈
        imageUrl = "com.xm.zjgamecenter", --分享图片地址（分享网页模式，需要传安卓包名）
        text = shareText, --分享内容 
        comment = "download", --微信不用, 用以区分直接分享下载地址和游戏内
        copyTextWhenNoWX = shareText, --未安装微信时复制到剪贴板的内容
    }
    local callBackFunc = function ()
        print("邀请好友返回游戏")
    end
    XH.sdkManager:startShare(shareInfo,callBackFunc,self)
end

-- 请求消耗活动的奖励
function BusinessActModule:reqGetConsumeActAward(costCount,missionid)
    XH.TipTool.showLoading()
    local awardtype
    local dataString  = string.format(";nReqMode=%s;nWid=%s;nChannel=%s;nTeaID=%s;nMissionId=%s",KW_CONSUME_REQ_MODE.REQ_ACT_INFO,XH.areaData:getAreaWindID(),XH.areaData:getChannelID(),self._nTeaid,self.TeaHouseMissionList.CONSUME)
    dataString = dataString .. ";nCount=" .. costCount 
    if missionid then
        awardtype = KW_CONSUME_REQ_MODE.REQ_ACT_SINGLE_AWARD
        dataString = dataString .. ";nMissionid=" .. missionid
    else
        awardtype = KW_CONSUME_REQ_MODE.REQ_ACT_ALL_AWARD
    end
    dataString = dataString .. ";nReqMode=" ..  awardtype
    self:startReq("ReqGetConsumeActAward", TaskDefine.TASK_ID.TASK_TYPE_TEAHOUSE_CONSUEM_ACT, 0, 0, XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT, dataString)
end

function BusinessActModule:onReqGetConsumeAwardCallBack(cell, cellType, data)
    XH.TipTool.hideLoading()
    if cellType == XH.Req.TYPE.SUCCESS then
        data = data.acData
        if data and data ~= "" then
            local jsonData
            local ok = pcall( function()
                jsonData = cjson.decode(data)
            end )
            if not ok then
                return
            end
            self:dispatchEvent( { name = BusinessActModule.EVENT_AWARD_CALLBACK, data = jsonData })
        end
    end
end

function BusinessActModule:setTeaHouseBusinessActType(actName, bShow, webAid)
    self._actShowType[actName] = bShow or false
    self:dispatchEvent( { name = BusinessActModule.EVENT_TEAHOUSE_ACT_SHOW_TYPE})
    if actName == "TeaHouseRecallAct" and webAid ~= nil and type(webAid) == "number" then
        self._recallActWebAid = webAid
    end
end

function BusinessActModule:getTeaHouseBusinessActType()
    return self._actShowType
end

return BusinessActModule   �>  