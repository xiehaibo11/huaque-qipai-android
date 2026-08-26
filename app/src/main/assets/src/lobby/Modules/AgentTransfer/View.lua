--------------------------------------------
-- File: TransferActivityView.lua
-- Desc: B端迁移活动主界面
-- by: Ruzibeil
--------------------------------------------
local AgentTransferView = class("AgentTransferView",XH.ViewBase)
local HttpDefine = require("app.Define.HttpDefine")
local UrlConf = require("app.Config.UrlConf")
local Config = require("lobby.Modules.AgentTransfer.Config")

local KW_POS = "KW_POS_"
local KW_TEXT_PLAYER_NAME = "KW_TEXT_PLAYER_NAME"
local KW_IMG_PLAYER_FRAME = "KW_IMG_PLAYER_FRAME"
local KW_IMG_PLAYER_HEADUI = "KW_IMG_PLAYER_HEADUI"
local DEFAULT_HEAD_FRAME = "transfer_defaultfimg.png"
local KW_ROW_CNT = 4
local KW_PAGE_CNT = 16

function AgentTransferView:getCSBPath()
    return "cocosStudio/hall/CSB/window/AgentTransferLayer.csb"
end

function AgentTransferView:getBindingInfo()
    return {
        ["_KW_IMG_MAIN"] = {varName="_imgMain"},
        ["_KW_BTN_CLOSE"] = {varName="_btnClose",type = XH.UI_TYPE.IMAGE_TOBUTTON,onTouchEnded = "onTouchEventClose"},
        ["_KW_BTN_INVITE_BINDPLAYER"] = {varName="_btnInvite",type = XH.UI_TYPE.IMAGE_TOBUTTON,onTouchEnded = "onTouchEventInvitePlayer"},
        ["_KW_BTN_ACTIVITY_RULE"] = {varName="_btnRule",type = XH.UI_TYPE.IMAGE_TOBUTTON,onTouchEnded = "onTouchEventActivityRule"},		
		["_KW_TEXT_WECHAT_INFO"] = {varName="_txtWechatInfo"},

        ["_KW_TEXT_TARGET_TRASNFERCNT"] = {varName="_txtTargetCnt"},
        ["_KW_TEXT_TARGET_TRASNFERCOST"] = {varName="_txtTargetConsume"},
        ["_KW_TEXT_TARGET_AWARDINFO"] = {varName="_txtTargetAwardCnt"},
        ["_KW_TEXT_PROGRESS_TRANSFERCNT"] = {varName="_txtProgressTransferCnt"},
        ["_KW_TEXT_PROGRESS_TRANSFERCOST"] = {varName="_txtProgressTransferConsume"},
		["_KW_TEXT_ALREADY_TRANSFERCNT"] = {varName="_txtAlreadyTransferCnt"},
        ["_KW_TEXT_ALREADY_TRANSFERCOST"] = {varName="_txtAlreadyTransferCost"},
        ["_KW_TEXT_ALREADYAWARD"] = {varName="_txtAlreadyAward"},
        ["_KW_LISTVIEW_ALREADYAWARDS"] = {varName="_alreadyAwardList"},	

		["_KW_LISTVIEW_TRANSFER_LIST"] = {varName="_transferList"},
        ["_KW_PANEL_TRANSFERPLAYER_LINEITEM"] = {varName="_transferLineItem"},	
        ["_KW_PANEL_TRANSFERPLAYER_ITEM"] = {varName="_transferItem"},	

        ["_KW_PANEL_RULE"] = {varName="_panelRule"},
        ["_KW_BTN_RULE_CLOSE"] = {varName="_btnCloseRule",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventCloseRule"},
        ["_KW_TEXT_ACTIVITY_TIME"] = {varName="_txtActTime"},
    }
end

function AgentTransferView:ctor()
	AgentTransferView.super.ctor(self)
    self:initData()
	self:setWechatInfo()
end

---------------------------------------------------------
--@brief  初始化数据并请求
--@param  
--@return 
--by Ruzibeil
---------------------------------------------------------
function AgentTransferView:initData()
    self._agentProgressData = {}
	self._transferBindList = {}  -- 结构为最多每行四个 { [1] = {},[2] = {}}
	self._inviteUrl = ""
	self._isNeedReq = true
	self._reqListCnt = 0
	self:reqAgentTransferProgress()
end

---------------------------------------------------------
--@brief  设置微信号
--@param  
--@return 
--by Ruzibeil
---------------------------------------------------------
function AgentTransferView:setWechatInfo()
	local lobbyId = XH.areaData:getLobbyID()
	local strWechat = Config.WeChatInfo[lobbyId]
	self._txtWechatInfo:setText(strWechat or "")
end

--测试用代码
function AgentTransferView:testUI()
	local responseData = 
	{
		target_num = 100,target_consume=2000,
		current_prizes = {name = "房卡",value = 100},
		progress = {migrate_num = 20,consume = 600},
		finish_prizes = {
			{name = "房卡",value = 100},
			{name = "钻石",value = 100},
			{name = "礼券",value = 100},
		},
		user_list = {
			
		}
	}
	if not responseData then return end
	local userList = responseData.user_list
	for i = 1,16 do
		local useritem = {}
		useritem.avatar = ""
		useritem.nickname = "B端" .. i
		table.insert(userList,useritem)
	end
	local targetNum = responseData.target_num
	local targetCost = responseData.target_consume
	self._agentProgressData._targetInfo = {targetNum = targetNum,targetCost = targetCost}
	self._agentProgressData._targetaward = responseData.current_prizes
	self._agentProgressData._targetProgress = responseData.progress
	self._agentProgressData._alreadyAward = responseData.finish_prizes
	self:showAgentProgressUI()
	self:dealUntransferListData(responseData.user_list)
	self:showUnTransferList()
end

---------------------------------------------------------
--@brief 用户迁移活动数据请求
--@param  
--@return 
--by Ruzibeil
---------------------------------------------------------
function AgentTransferView:reqAgentTransferProgress()
	local urlParam = XH.lobby:getModule("AgentTransfer"):getUrlParam()
	if urlParam ~= "" then
		local strUrl = UrlConf.REQ_ZJ_AGENT_TRANSINFO .. urlParam
		XH.httpManager:RequestGet(HttpDefine.REQ_ZJ_AGENT_TRANSINFO, strUrl, 4, handler(self, self.respAgentTransferProgress))
		XH.TipTool.showLoading()
	end
end

function AgentTransferView:respAgentTransferProgress(eType, status, response)
    XH.TipTool.hideLoading()
	if eType ~= HttpDefine.REQ_ZJ_AGENT_TRANSINFO then
        return
    end
    if status == 200 then
        if response.status == 0 then
			local responseData = response.data
			if not responseData then return end
			local targetNum = responseData.target_num
			local targetCost = responseData.target_consume
			self._agentProgressData._targetInfo = {targetNum = targetNum,targetCost = targetCost}
			self._agentProgressData._targetaward = responseData.current_prizes
			self._agentProgressData._targetProgress = responseData.progress
			self._agentProgressData._alreadyAward = responseData.finish_prizes
            self:showAgentProgressUI()			
			self:dealUntransferListData(responseData.user_list)
			self:showUnTransferList()
			self._inviteUrl = responseData.invite_url
        else
            XH.TipTool.showTip({
				type = XH.TIP_LAYER_TYPE.OK_NO_CLOSE,
				bTop = true
			}, "请求活动信息失败：" .. response.info)
        end
    else
        XH.TipTool.showTip({
			type = XH.TIP_LAYER_TYPE.OK_NO_CLOSE,
			bTop = true
		}, "请求活动信息失败：" .. status)
    end
end

---------------------------------------------------------
--@brief 将web返回的未迁移数据转换成每行四个数据的table格式
--@param  
--@return 
--by Ruzibeil
---------------------------------------------------------
function AgentTransferView:dealUntransferListData(responseList)
	local bindList = responseList
	if not bindList and not next(bindList) then return end
	if #bindList < KW_PAGE_CNT then
		self._isNeedReq = false
	else
		self._reqListCnt = self._reqListCnt + 1
	end
	--拼接先前的table为每行四个
	local lastLineList = self._transferBindList[#self._transferBindList]
	if lastLineList and #lastLineList < KW_ROW_CNT then
		local addCnt = 0
		for i = 1, KW_ROW_CNT - #lastLineList do
			if bindList[i] then
				addCnt = addCnt + 1
				local alignItem = clone(bindList[i])
				table.insert(lastLineList,alignItem)
			end
		end
		for i = #bindList,1,-1 do
			if i <= addCnt then
				table.remove(bindList,i)
			end
		end
	end
	--处理剩余的
	local bindLine = math.ceil(#bindList/KW_ROW_CNT)
	for i = 0, bindLine - 1 do
		local lineList = {}
		for j = 1,KW_ROW_CNT do
			local addIndex = i * KW_ROW_CNT + j
			if addIndex > #bindList then
				break
			end
			table.insert(lineList,bindList[addIndex])
		end
		table.insert(self._transferBindList,lineList)
	end
end

---------------------------------------------------------
--@brief 用户迁移活动请求未迁移玩家
--@param  
--@return 
--by Ruzibeil
---------------------------------------------------------
function AgentTransferView:reqUnTransferList()
	local urlParam = XH.lobby:getModule("AgentTransfer"):getUrlParam()
	if urlParam ~= "" then
		local strUrl = UrlConf.REQ_ZJ_AGENT_TRANSUSER .. urlParam
		local pageStr = string.format("&page=%d",self._reqListCnt + 1)
		strUrl = strUrl .. pageStr
		XH.httpManager:RequestGet(HttpDefine.REQ_ZJ_AGENT_TRANSUSER, strUrl, 4, handler(self, self.respUnTransferList))
		XH.TipTool.showLoading()
	end
end

function AgentTransferView:respUnTransferList(eType, status, response)
    XH.TipTool.hideLoading()
	if eType ~= HttpDefine.REQ_ZJ_AGENT_TRANSUSER then
        return
    end
    if status == 200 then
        if response.status == 0 then
			local responseData = response.data
			if not responseData then return end
			self:dealUntransferListData(responseData.user_list)
			self:showUnTransferList()
        else
            XH.TipTool.showTip({
				type = XH.TIP_LAYER_TYPE.OK_NO_CLOSE,
				bTop = true
			}, "请求玩家信息失败：" .. response.info)
        end
    else
        XH.TipTool.showTip({
			type = XH.TIP_LAYER_TYPE.OK_NO_CLOSE,
			bTop = true
		}, "请求玩家信息失败：" .. status)
    end
end

---------------------------------------------------------
--@brief 用户迁移活动进度显示
--@param  
--@return 
--by Ruzibeil
---------------------------------------------------------	
function AgentTransferView:showAgentProgressUI()
	local targetNum = self._agentProgressData._targetInfo.targetNum
	local targetConsume = self._agentProgressData._targetInfo.targetCost
	if targetNum >=0 then  -- 约定为-1时隐藏配置
		self._txtTargetCnt:setText("目标人数: " ..  targetNum)
	end
	if targetConsume >=0 then
		self._txtTargetConsume:setText("目标消耗: " .. targetConsume)
	end
	local targetAwardInfo = self._agentProgressData._targetaward
	self._txtTargetAwardCnt:setText(targetAwardInfo.name .. ":" .. targetAwardInfo.value)

	local progressInfo = self._agentProgressData._targetProgress
	if progressInfo.migrate_num >= 0 then
		self._txtProgressTransferCnt:setText("当前人数: " ..  progressInfo.migrate_num)
	end
	if progressInfo.consume >= 0 then
		self._txtProgressTransferConsume:setText("当前消耗: " ..  progressInfo.consume)
	end
	local alreadyAward = self._agentProgressData._alreadyAward
	local awardMap = {}	
	for _,subAward in pairs(alreadyAward) do
		if subAward.name and subAward.value then
			local awardKey = subAward.name
			awardMap[awardKey] = awardMap[awardKey] or 0
			awardMap[awardKey] = awardMap[awardKey] + subAward.value
		end
	end
	for key,val in pairs(awardMap) do
		local itemAward = self._txtAlreadyAward:clone()
		self._alreadyAwardList:pushBackCustomItem(itemAward)
		itemAward:setText(key .. ": " .. val)
		itemAward:setVisible(true)
	end
end

---------------------------------------------------------
--@brief  移除窗口
--@param  
--@return 
--by Ruzibeil
---------------------------------------------------------
function AgentTransferView:onTouchEventClose(send,eventType)
    self:close()
end

---------------------------------------------------------
--@brief 用户迁移活动规则显示
--@param  
--@return 
--by Ruzibeil
---------------------------------------------------------	
function AgentTransferView:onTouchEventActivityRule(send,eventType)
	local lobbyId = XH.areaData:getLobbyID()
	self._txtActTime:setText(Config.ActivityTime[lobbyId] or "") 
    self._panelRule:setVisible(true)
end

function AgentTransferView:onTouchEventCloseRule(send,eventType)
    self._panelRule:setVisible(false)
end

---------------------------------------------------------
--@brief 用户迁移活动邀请玩家
--@param  
--@return 
--by Ruzibeil
---------------------------------------------------------	
function AgentTransferView:onTouchEventInvitePlayer(send,eventType)
    if self._inviteUrl ~= "" then
	    XH.lobby:getModule("AgentTransfer"):shareToInvitePlayer(self._inviteUrl)
    end
end

function AgentTransferView:close()
    local tempCallBack = self.popLayerCallback
	AgentTransferView.super.close(self)
	if tempCallBack then
		tempCallBack()
	end
end

------------------------------ 未迁移玩家列表tableview------------------------------

function AgentTransferView:showUnTransferList()
	self:initUnTransferListTableView()
	if self._unTransferTableView then
		self._unTransferTableView:reloadData()
		local offset = self._unTransferTableView:getContentOffset()
		if offset.y < 0 and self._offsetDefault then
			if offset.y < self._offsetDefault then
				offset.y = offset.y - self._offsetDefault
				self._offsetDefault = self._unTransferTableView:getContentOffset().y
				self._unTransferTableView:setContentOffset(offset)
			end
		end
	end
end	

function AgentTransferView:initUnTransferListTableView()
    if not self._unTransferTableView then
        local uListView = self._transferList
        if uListView then
			-- 初始化并设置大小
            local viewSize = uListView:getContentSize()
            local tableview = cc.TableView:create(viewSize)
            tableview:setDirection(cc.SCROLLVIEW_DIRECTION_VERTICAL)
            tableview:setVerticalFillOrder(cc.TABLEVIEW_FILL_TOPDOWN)
            tableview:setPosition(uListView:getPosition())
            tableview:setDelegate()
            uListView:getParent():addChild(tableview)
            tableview:registerScriptHandler(handler(self, AgentTransferView.UnTransferListCellTouched), cc.TABLECELL_TOUCHED)
            tableview:registerScriptHandler(handler(self, AgentTransferView.cellSizeForUnTransferList), cc.TABLECELL_SIZE_FOR_INDEX)
            tableview:registerScriptHandler(handler(self, AgentTransferView.UnTransferListCellAtIndex), cc.TABLECELL_SIZE_AT_INDEX)
            tableview:registerScriptHandler(handler(self, AgentTransferView.numberOfCellsInTableView), cc.NUMBER_OF_CELLS_IN_TABLEVIEW)
			tableview:registerScriptHandler(handler(self, AgentTransferView.scrollViewDidScroll), cc.SCROLLVIEW_SCRIPT_SCROLL)
            uListView:removeFromParent()
			self._unTransferTableView = tableview
        end
    end
end

--设置滚动事件
function AgentTransferView:scrollViewDidScroll(view)
    local offset = view:getContentOffset()
    if not self._offsetDefault then
        offset = view:getContentOffset()
        self._offsetDefault = offset.y
    end
    if offset.y == 0 and self._isNeedReq then
        self:reqUnTransferList()
    end
end

--测试用代码
function AgentTransferView:testAdd()
	XH.TipTool.showLoading()
	self._unTransferTableView:runAction(cc.Sequence:create(
		cc.DelayTime:create(0.2),
		cc.CallFunc:create(function()
			XH.TipTool.hideLoading()	
			local userList = {}
			for i = (self._reqListCnt) * 16 + 1 , (self._reqListCnt + 1) * 16 do
				local useritem = {}
				useritem.avatar = ""
				useritem.nickname = "B端" .. i
				table.insert(userList,useritem)
			end	
			self:dealUntransferListData(userList)
			self:showUnTransferList()
		end)
		))
end

-- 设置单个单元的大小，如果需要控制间隔，return的height需要加上间距值
function AgentTransferView:cellSizeForUnTransferList(view, idx)
    local defaultItem = self._transferItem
    local itemSize = defaultItem:getContentSize()
    return itemSize.width, itemSize.height + 12
end

-- 设置数据总量
function AgentTransferView:numberOfCellsInTableView(view)
    return #self._transferBindList
end

-- 设置单元点击事件
function AgentTransferView:UnTransferListCellTouched(view, cell)
end

--设置每个单元
function AgentTransferView:UnTransferListCellAtIndex(view, idx)
    local index = idx + 1
    local cell = view:dequeueCell()
	if not cell then
        cell = cc.TableViewCell:new()
		cell:removeAllChildren()
        local item = self._transferLineItem:clone()
        item:setAnchorPoint(cc.p(0, 0))
        item:setPosition(cc.p(0,0))
        item:setName(self._transferLineItem:getName())
        item:setVisible(true)
        cell:addChild(item)
    end
	--清理先前数据
	local lineItem = cell:getChildByName(self._transferLineItem:getName())
	for i = 1, KW_ROW_CNT do			
		local posNode = lineItem:getChildByName(KW_POS .. i)
		if posNode then
			local	childItem = posNode:getChildren()
			if not next(childItem) then
				local transferItem = self._transferItem:clone()
				posNode:removeAllChildren()
				posNode:addChild(transferItem)
				transferItem:setPosition(cc.p(0,0))
			end
			posNode:setVisible(false)
		end
	end	
	--刷新cell
	local itemLineData = self._transferBindList[index]
	if itemLineData then
		for i,itemData in pairs(itemLineData) do
			local posNode =  lineItem:getChildByName(KW_POS .. i)
			local nickName = itemData.nickname
			if string.len(nickName) > 18 then
				local validBytes = XH.StringTool.getMinimumValidBytesUTF8(nickName, 18)
				nickName = string.sub(nickName,1,validBytes) .. "..."
			end
			local headUrl = itemData.avatar
			if posNode then
				posNode:setVisible(true)
				XH.UITool.setText(posNode,KW_TEXT_PLAYER_NAME,nickName)
				self:showHeadAdapt(posNode,headUrl)	
			end
		end		
	end
	return cell
end

function AgentTransferView:showHeadAdapt(item,headUrl)
	local faceNode = ccui.Helper:seekWidgetByName(item,KW_IMG_PLAYER_HEADUI)
	local frameNode =  ccui.Helper:seekWidgetByName(item,KW_IMG_PLAYER_FRAME)
    if headUrl ~= "" then
		faceNode:removeAllChildren()
		local size = faceNode:getContentSize()
		local adaptSize = frameNode:getContentSize()
		local headImg = XH.RemoteImage.new()
			:setPosition(size.width / 2, size.height / 2)
			:ignoreContentAdaptWithSize(false)
			:setContentSize(adaptSize.width * 0.88, adaptSize.height * 0.88)
			:addTo(faceNode)
			faceNode:setVisible(true)
		headImg:setUrl(headUrl,true)
	else
		faceNode:loadTexture(DEFAULT_HEAD_FRAME,ccui.TextureResType.plistType)
		faceNode:setVisible(true)
	end
end

return AgentTransferView F  