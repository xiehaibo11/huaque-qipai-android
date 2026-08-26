local PhoneBindActNode = class("PhoneBindActNode", XH.ViewBase)

PhoneBindActNode.KW_NAME_PHONE_BIND_ACT = "KW_NAME_PHONE_BIND_ACT"

local KW_DEFAULT_HEIGHT_SIZE = 1342
local KW_DEFAULT_WIDTH_SIZE = 870
local KW_CAN_CHOSE_PRIZE_MAX = 3

-- nBindState 0为get 1为bind
-- 请求玩家信息
-- local dataString = ";nServerId = " .. serverId ..";nBindOrGet = " .. nBindState
-- 请求奖品
-- local dataString = ";nServerId = " .. serverId ..";pids = " .. pids

local KW_PIDS = {
    [1] = "pids",
    [2] = "pidss",
    [3] = "pidsss",
}

function PhoneBindActNode:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/PhoneBindAct.csb"
end

function PhoneBindActNode:ctor(param)
    param = param or { }
    PhoneBindActNode.super.ctor(self, param)
    self.csbNode = self
    self.width = param.width or KW_DEFAULT_HEIGHT_SIZE
    self.height = param.height or KW_DEFAULT_WIDTH_SIZE
    self:setContentSize(self.width, self.height)
    self:setName(self.KW_NAME_PHONE_BIND_ACT)

    self:initData()
    self:reqPeoInfo()
end

function PhoneBindActNode:getBindingInfo()
    return {
        ["_KW_FIX_PANEL"] = { varName = "_fixPanel" },
        ["_KW_BTN_GET_PRIZE"] = { varName = "_btnGetPrize", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventGetPrize" },
        ["_KW_CHECKBOX_CHOSE_"] = { beginIndex = 1, endIndex = 3, varName = "_checkBoxPanel", onTouchEnded = "onTouchEventChoosePrize" },
        ["_KW_IMG_PRIZECHOSE_"] = { beginIndex = 1, endIndex = 3, varName = "_prizeChosePanel", onTouchEnded = "onTouchEventChoosePrize" },
        ["_KW_TEXT_PRIZE_"] = { beginIndex = 1, endIndex = 3, varName = "_textPrizeName" },
    }
end

function PhoneBindActNode:getProxyEvents()
    return {
        { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_PHONE_BIND_ACT_GETINFO", callBack = "onEventGetInfo" },
        { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_PHONE_BIND_ACT_GETPRIZE", callBack = "onEventGetPrize" },
        { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_PHONE_BIND_ACT_PRIZE_POOLS", callBack = "onEventGetPools" },
    }
end

-- 初始化数据
function PhoneBindActNode:initData()
    self._prizeData = { }
    self._personMsg = { }
end

-- 刷新界面
function PhoneBindActNode:flushUI()
    if self._personMsg.isGetPrizes == 1 and self._personMsg.isBind == 1 then
        self._btnGetPrize:loadTexture("phone_bind_act_hasget.png", ccui.TextureResType.plistType)
    end
end

-- 点击选框
function PhoneBindActNode:onTouchEventChoosePrize(send, event)
    if self._personMsg.isGetPrizes == 1 and self._personMsg.isBind == 1 then
        return
    end
    local getClickImg = ccui.Helper:seekWidgetByName(send, "_KW_CLICK")
    if getClickImg then
        if (not getClickImg:isVisible()) and self:judgeChoseNums() then
            XH.TipTool.showToast("已达选择上限", 3)
            return
        end
        for i = 1, KW_CAN_CHOSE_PRIZE_MAX do
            if (self["_checkBoxPanel" .. i] and self["_checkBoxPanel" .. i]:getName() == send:getName())
                or(self["_prizeChosePanel" .. i] and self["_prizeChosePanel" .. i]:getName() == send:getName()) then
                if self["_prizeChosePanel" .. i] then
                    if getClickImg:isVisible() then
                        self["_prizeChosePanel" .. i]:loadTexture("phone_bind_act_circle2.png", ccui.TextureResType.plistType)
                    else
                        self["_prizeChosePanel" .. i]:loadTexture("phone_bind_act_circle1.png", ccui.TextureResType.plistType)
                    end
                end
                break
            end
        end
        getClickImg:setVisible(not getClickImg:isVisible())
    end
end

-- 累计选择
function PhoneBindActNode:judgeChoseNums()
    local clickNums = self:getPrizeChoseNums()
    if self._personMsg and self._personMsg.canChoosePrizes then
        if self._personMsg.canChoosePrizes <= clickNums then
            return true
        end
    end
    return false
end

-- 刷新奖池
function PhoneBindActNode:flushPools()
    for i = 1, KW_CAN_CHOSE_PRIZE_MAX do
        if self._prizeData[i] and self["_prizeChosePanel" .. i] then
            local fixPanel = ccui.Helper:seekWidgetByName(self["_prizeChosePanel" .. i], "_KW_FIX_PANEL")
            if fixPanel then
                local size = fixPanel:getContentSize()
                local imageNode = XH.RemoteImage.new()
                :setTouchEnabled(false)
                :setPosition(size.width / 2, size.height / 2)
                :setContentSize(size.width, size.height)
                :addTo(fixPanel)

                imageNode:setMaxContentSize(size)
                imageNode:setUrl(self._prizeData[i].prizes.image_url or "", true)
            end
            if self["_textPrizeName" .. i] then
                self["_textPrizeName" .. i]:setText(self._prizeData[i].prizes.prize_name)
                self["_textPrizeName" .. i]:setVisible(true)
            end
        end
    end
end

-- 奖品数量
function PhoneBindActNode:getPrizeChoseNums()
    local clickNums = 0
    for i = 1, KW_CAN_CHOSE_PRIZE_MAX do
        if self["_checkBoxPanel" .. i] then
            local getClickImg = ccui.Helper:seekWidgetByName(self["_checkBoxPanel" .. i], "_KW_CLICK")
            if getClickImg and getClickImg:isVisible() then
                clickNums = clickNums + 1
            end
        end
    end
    return clickNums
end

-- 奖池获取
function PhoneBindActNode:getPrizeLists()
    local pids = ""
    for i = 1, KW_CAN_CHOSE_PRIZE_MAX do
        if self["_checkBoxPanel" .. i] then
            local getClickImg = ccui.Helper:seekWidgetByName(self["_checkBoxPanel" .. i], "_KW_CLICK")
            if getClickImg and getClickImg:isVisible() and self._prizeData[i] and KW_PIDS[i] then
                pids = pids .. ";" .. KW_PIDS[i] .. " = " .. self._prizeData[i].pid
            end
        end
    end
    return pids
end

-- 获得奖励
function PhoneBindActNode:onTouchEventGetPrize(send, event)
    if self._personMsg.isGetPrizes == 1 and self._personMsg.isBind == 1 then
        return
    end
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.TABS_ACTIVITY_BIND_GIFT)
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.TABS_ACTIVITY_JOINACT)
    local getPhoneBindState = XH.userDefault:getValue(XH.userDefault.KEY_ID.LAST_PHONE_STATE, "")
    if XH.playerData:getPlayerPhone() == "" and getPhoneBindState == "" then
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK,
            funcOK = function()
                -- 启用一键绑定流程
                XH.login:getModule("Login"):reqGYInfo(true)
            end
        } , "是否绑定手机号")
        return
    end
    if self:getPrizeChoseNums() ~= self._personMsg.canChoosePrizes then
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK,
        } , "请确认是否已经选取所有奖励")
        return
    end
    local pids = self:getPrizeLists()
    if pids == "" then
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK,
        } , "数据获取有误，请重新打开页面")
        return
    end
    self:reqGetPrize(pids)
end

-- 获取玩家信息
function PhoneBindActNode:reqPeoInfo()
    XH.lobby:getModule("TabsActivity"):reqPhoneBindActPeoInfo()
end

-- 请求绑定
function PhoneBindActNode:reqBindState()
    XH.lobby:getModule("TabsActivity"):reqPhoneBindActBindState()
end

-- 请求奖励
function PhoneBindActNode:reqGetPrize(pids)
    local dataString = ";nServerId = " .. XH.lobby:getModule("TabsActivity").PhoneBindServerId.GetPrize .. pids
    XH.lobby:getModule("TabsActivity"):reqPhoneBindActPrize(dataString)
end

-- 请求活动奖池
function PhoneBindActNode:reqBindPools(aid)
    XH.lobby:getModule("TabsActivity"):reqPhongBindActTaskConfig(aid)
end

-- 获取信息监听
function PhoneBindActNode:onEventGetInfo(msg)
    if msg and msg.data then
        self._personMsg = msg.data or { }
        self:reqBindPools(self._personMsg.aid)
        self:flushUI()
    end
end

-- 获取奖励监听
function PhoneBindActNode:onEventGetPrize(msg)
    if msg and msg.data then
        local getPrizeMsg = msg.data
        if getPrizeMsg.errcode then
            XH.TipTool.showTip( {
                type = XH.TIP_LAYER_TYPE.OK,
            } , getPrizeMsg.msg or "数据有误，请重新打开重试")
        elseif getPrizeMsg.prizeMsg then
            local awardLists = { }
            for _, value in pairs(getPrizeMsg.prizeMsg) do
                if value.prizeMsg then
                    awardLists[#awardLists + 1] = { }
                    awardLists[#awardLists].url = value.prizeMsg.imageUrl
                    awardLists[#awardLists].desc = value.prizeMsg.prizeName
                end
            end
            self._personMsg = getPrizeMsg.info or self._personMsg
            self:flushUI()
            self:flushInfoProps()
            XH.viewManager:openView("CommonRewardView", nil, awardLists)
        end
    end
end

-- 获取奖池监听
function PhoneBindActNode:onEventGetPools(msg)
    if msg and msg.data then
        self._prizeData = msg.data or { }
        self:flushPools()
    end
end

-- 刷新身上数据
function PhoneBindActNode:flushInfoProps()
    self:performWithDelay(function()
		XH.playerData:flushPlayerDrop()
        XH.playerData:flushGoldCoin()
	end, 1)
end

return PhoneBindActNode  &  