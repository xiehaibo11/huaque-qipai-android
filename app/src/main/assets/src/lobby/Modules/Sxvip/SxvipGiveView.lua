local SxvipGiveView = class("SxvipGiveView", XH.ViewBase)

local SxvipConfig = require("lobby.Modules.Sxvip.Config")

local KW_IMG_DEFULT_HEAD = "_0002_mrtx_w.png"
local INT32 = 2147483647

local KW_HORIZONTAL_DIS = 10 -- 左右边距
local KW_VECTICAL_DIS = 10 -- 顶边距
local KW_HORIZONTAL_X_DIS = 20 -- 每个item水平最小间距
local KW_VECTICAL_Y_DIS = 20 -- 每个item垂直间距
local KW_HORIZONTAL_WITH = 900 --固定宽度显示，宽度
local KW_SCROLLVIEW_HEIGHT_OFFSET = 20 --scrollview内框高度偏移量

local KW_DEFAULT_ITEM_SIZE = cc.size(400, 150)

function SxvipGiveView:ctor()
    SxvipGiveView.super.ctor(self)
    -- 当前选中标签页内容的下标
    self._displayCateoryIndex = 1
    self._isSearching = false
    self._isRequestingUserInfo = false
    self._selectDay = 0
    self._selectFriendInfo = {}
    self._page = 0
    self._reqNextPageLock = false
    XH.lobby:getModule("Sxvip"):reqFriendList()
    self:initUI()
end

function SxvipGiveView:initUI()
    self._inputText:registerScriptEditBoxHandler(function(eventName)
        if eventName == "ended" or eventName == "return" then
            if not self or tolua.isnull(self) or self._isRequestingUserInfo then
                return
            end
            self:reqSearchUserInfo()
        end
    end)
    XH.lobby:getModule("Sxvip"):reqRemainder()
    self:onTouchGiveItem(self._btnGiveItem_1)
    self:onUpdateMessage()
end

function SxvipGiveView:getCSBPath()
    return "cocosStudio/hall/CSB/Sxvip/SxvipGiveLayer.csb"
end

function SxvipGiveView:getBindingInfo()
    return {
        ["_KW_BTN_CLOSE"] = { varName = "KW_BTN_CLOSE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnClose"},
        ["_KW_SCROLLVIEW_FRIEND"] = { varName = "_friendList", type = XH.UI_TYPE.SCROLLVIEW, onTouch = "onTouchEventList" },
        ["_KW_PLAYER_ITEM"] = { varName = "_friendMod" },
        ["_KW_TEXT_NOFRIEND"] = { varName = "_imgNoItem" },
        ["_KW_TEXT_VIP_DAYS"] = { varName = "_textDays" },
        ["_KW_CLEAR_BTN"] = { varName = "_btnClearUserInfo", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnClearInfo"},
        ["_KW_INPUT_NUMID"] = {varName = "_inputText"},
        ["_KW_BTN_REQUEST"] = { varName = "_btnRequest", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnRequest"},
        ["_KW_BTN_SEND"] = { varName = "_btnSend", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnSend"},
        ["_KW_BTN_RECORD"] = { varName = "_btnRecord", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnRecord"},
        ["_KW_GIVE_ITEM_1"] = { varName = "_btnGiveItem_1", onTouchEnded = "onTouchGiveItem"},
        ["_KW_GIVE_ITEM_2"] = { varName = "_btnGiveItem_2", onTouchEnded = "onTouchGiveItem"},
        ["_KW_BTN_DETAIL"] = { varName = "_KW_BTN_DETAIL", onTouchEnded = "onTouchShowDetail"},
        ["_KW_BTN_SHOWDETAIL"] = { varName = "_KW_BTN_SHOWDETAIL"},
    }
end

function SxvipGiveView:getProxyEvents()
    return {
       { module = XH.lobby:getModule("Sxvip"), eventKeyName = "SXVIP_EVENT_FRIENDLIST_CHANGED", callBack = "showFriendList" },
       { module = XH.lobby:getModule("Sxvip"), eventKeyName = "SXVIP_EVENT_REMAINDER", callBack = "onRemainder" },
       { module = XH.lobby:getModule("Sxvip"), eventKeyName = "SXVIP_EVENT_GET_OTHER_USERINFO", callBack = "onSearchUserInfo" },
       { module = XH.lobby:getModule("Sxvip"), eventKeyName = "SXVIP_EVENT_MESSAGE_COUNT", callBack = "onUpdateMessage" },
    }
end

function SxvipGiveView:onTouchShowDetail()
    self:clearDelay()
    if self._KW_BTN_SHOWDETAIL:isVisible() then
        self._KW_BTN_SHOWDETAIL:setVisible(false)
        return
    end
    self._KW_BTN_SHOWDETAIL:setVisible(true)
    self._hideScheduleID = XH.SysTool.performDelayOnce(function()
        if not self or tolua.isnull(self) then
            return
        end
        self._KW_BTN_SHOWDETAIL:setVisible(false)
    end, 3)
end

function SxvipGiveView:clearDelay()
    if not self._hideScheduleID then
        return
    end
    cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._hideScheduleID)
    self._hideScheduleID = nil
end

--标签页内容显示
function SxvipGiveView:showFriendList(friendInfo)
    local friendList = {}
    if self._isSearching then
        friendList = friendInfo
    else
        friendList = XH.lobby:getModule("Sxvip"):getFriendListInfo() or {}
    end
    local totalNeedCount = #friendList
    -- 动态初始化滑动区域大小
    self:resetDisPlayContentSize(totalNeedCount)
    if self._imgNoItem then
        self._imgNoItem:setVisible(totalNeedCount == 0)
    end

    -- 创建牌友UI
    for i = 1, totalNeedCount do
        local friendInfo = friendList[i]
        local item = self:getFriendItemByIndex(i)
        if not item then
            item = self:createNewFriendItem()
        end
        item:setZOrder(totalNeedCount - i)
        self:updateItemUI(item, friendInfo, i)
        item:addTouchEventListener(handler(friendInfo, handler(self,self.onBtnTouchEventFriendItem)))
    end

    local friendCount = self:getFriendCount()
    -- 隐藏多余的item
    for i = totalNeedCount + 1, friendCount do
        local item = self:getFriendItemByIndex(i)
        if item then
            item:setVisible(false)
        end
    end

    --默认选中一个item
    if totalNeedCount > 0 then
        local defaultIndex = self._displayCateoryIndex <= totalNeedCount and self._displayCateoryIndex or 1
        local defaultItem = self:getFriendItemByIndex(defaultIndex)
        self:showFriendItemSelected(defaultItem, friendList[defaultIndex] or {})
    end
    if #friendList > 0 then
        self._reqNextPageLock = false
    end
end

function SxvipGiveView:updateItemUI(item, friendInfo, index)
    if tolua.isnull(item) then
        return
    end
    item:setName(tostring(index))
    -- item:setTag(productData.propsid or 0)
    item:setPosition(self:getDisplayPos(index))
    item:setVisible(true)
    self:setFriendInfo(item, friendInfo)
end

function SxvipGiveView:setFriendInfo(item, data)
    -- 设置昵称
    XH.UITool.setText(item, "_KW_PLAYER_NAME", "昵称:" .. XH.StringTool.cutStringByLength(clone(data.nickname), 10))
    XH.UITool.setText(item, "_KW_PLAYER_ID", "序号:" .. data.numid)
    -- 设置头像
    local headNode = XH.UIRemoteImage.create(XH.UITool.seekNodeByName(item, "_KW_IMG_HEAD"))
    if headNode then
        headNode:setSwallowTouches(false)
        headNode:setDefaultTexture(KW_IMG_DEFULT_HEAD, ccui.TextureResType.plistType)
        headNode:setUrl(data.headurl)
    end
end

-- 计算item的位置
function SxvipGiveView:getDisplayPos(index)
    local nextIndex = index or self:getFriendCount()
    nextIndex = nextIndex - 1
    -- 计算位置从0开始

    -- 每个item的大小
    local friendModSize = KW_DEFAULT_ITEM_SIZE
    if self._friendMod then
        friendModSize = self._friendMod:getContentSize()
    end

    local modHeight = friendModSize.height
    local modWidth = friendModSize.width
    local displayContentSize = self._friendList:getInnerContainerSize()

    -- 计算最小间距下每行的个数
    local miniTotalXDis = modWidth + KW_HORIZONTAL_X_DIS
    -- local withoutBoderWidt = displayContentSize.width - KW_HORIZONTAL_DIS * 2
    local withoutBoderWidt = KW_HORIZONTAL_WITH - KW_HORIZONTAL_DIS * 2
    local everyLineCount = math.floor(withoutBoderWidt / miniTotalXDis)

    -- 中间的水平分布
    local xDif =(withoutBoderWidt - everyLineCount * modWidth) /(everyLineCount - 1)
    local lineIndex = math.floor(nextIndex / everyLineCount)

    local totalXDis = modWidth + xDif
    local totalYDis = modHeight + KW_VECTICAL_Y_DIS

    -- 起始的位置
    local startPosX = KW_HORIZONTAL_DIS + modWidth / 2
    local startPosY = KW_VECTICAL_DIS + modHeight / 2

    local posX = startPosX + totalXDis *(nextIndex % everyLineCount)
    local posY = displayContentSize.height -(startPosY + totalYDis * lineIndex)

    return posX, posY
end

-- 获得UI界面上item的总数
function SxvipGiveView:getFriendCount()
    return self._friendList:getChildrenCount() or 0
end

-- 通过索引获得item
function SxvipGiveView:getFriendItemByIndex(index)
    return self._friendList:getChildByName(tostring(index))
end

-- 添加一个item到界面上.挂载到ScrollView中去
function SxvipGiveView:createNewFriendItem()
    local item = self._friendMod:clone()
    if item then
        item:retain()
        item:removeFromParent()
        self._friendList:addChild(item)
        item:release()
    end
    return item
end

-- 计算显示区域的大小的位置
function SxvipGiveView:resetDisPlayContentSize(friendCount)
    if not friendCount then
        return
    end
    -- 每个item的大小
    local friendModSize = KW_DEFAULT_ITEM_SIZE
    if self._friendMod then
        friendModSize = self._friendMod:getContentSize()
    end

    local modWidth = friendModSize.width
    local displayContentSize = self._friendList:getInnerContainerSize()

    -- 计算最小间距下每行的个数
    local miniTotalXDis = modWidth + KW_HORIZONTAL_X_DIS
    local withoutBoderWidt = displayContentSize.width - KW_HORIZONTAL_DIS * 2
    local everyLineCount = math.floor(withoutBoderWidt / miniTotalXDis)
    local lineCount = math.ceil(friendCount / everyLineCount)

    if lineCount ~= self._curLineCount then
        -- 现有的滑动区域需要变化
        displayContentSize = self._friendList:getContentSize()
        self._friendList:setInnerContainerSize(cc.size(displayContentSize.width, lineCount * (friendModSize.height + KW_SCROLLVIEW_HEIGHT_OFFSET)))
        self._curLineCount = lineCount
    end
end

function SxvipGiveView:onBtnTouchEventFriendItem(friendInfo, send, event)
    -- item会覆盖scrollView触摸事件，再这里item取消的时候，调用list滑动结束事件，判断是否需要加载新一页的牌友列表
    if event == ccui.TouchEventType.canceled then
        self:onTouchEventList(self._friendList, ccui.TouchEventType.ended)
    end
    if event ~= ccui.TouchEventType.ended then
        return
    end
    self._displayCateoryIndex = tonumber(send:getName())
    self:showFriendItemSelected(send, friendInfo)
end

function SxvipGiveView:showFriendItemSelected(itemNode, friendInfo)
    if not itemNode then return end
    if not self._friendList then return end

    local allFriendItem = self._friendList:getChildren()
    for _ , item in pairs(allFriendItem) do
        local visible = item == itemNode
        XH.UITool.setVisible(item,"_KW_IMG_SEL_BG", visible)
    end
    self._selectFriendInfo = friendInfo
end

function SxvipGiveView:onRemainder()
    local endTimeStamp = XH.lobby:getModule("Sxvip"):getNVipSendTime()
    if endTimeStamp <= 0 then
        self._textDays:setText('0')
        return
    end
    local days = math.floor((endTimeStamp)/86400)
    self._textDays:setText(days)
end

function SxvipGiveView:onBtnClearInfo(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if self._isSearching or self._inputText:getText() ~= "" then
        self._isSearching = false
        self._inputText:setText("")
        self:showFriendList()
    end
end

function SxvipGiveView:reqSearchUserInfo()
    local strInput = self._inputText:getText()
    if strInput == nil or strInput == "" then
        if self._isSearching then
            self._isSearching = false
            self:showFriendList()
        end
        return
    end
    local tmpSearchNum = tonumber(strInput)
    if tmpSearchNum then
        if XH.playerData:getNumberID() == tmpSearchNum then
            XH.TipTool.showToast("不能给自己赠送哦~")
            return
        elseif tmpSearchNum == 0 or tmpSearchNum >= INT32 then
            XH.TipTool.showToast("输入内容错误，请重试")
            return
        end
        self._isRequestingUserInfo = true
        XH.lobby:getModule("Sxvip"):reqOtherUserInfo(tmpSearchNum)
    else
        XH.TipTool.showToast("输入内容有误，请重试")
    end
end

function SxvipGiveView:onSearchUserInfo(event)
    self._isRequestingUserInfo = false
    local data = event.data
    if not data then
        return
    end
    if data.nNumid == 0 then
        XH.TipTool.showToast("未搜索到该玩家，请确认序号！", 3)
        return
    end
    self._isSearching = true
    local searchFriendInfo = {}
    searchFriendInfo[1] = {}
    searchFriendInfo[1].numid = data.nNumid
    searchFriendInfo[1].nickname = data.nNickname
    searchFriendInfo[1].headurl = data.nHeadurl
    self:showFriendList(searchFriendInfo)
end

-- 索要
function SxvipGiveView:onBtnRequest(sender, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if not self._selectFriendInfo.numid then
        XH.TipTool.showToast("请选择牌友索要")
        return
    end
    if self._selectDay == 0 then
        XH.TipTool.showToast("请选择索要的会员天数")
        return
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyhz24051402, { item_id = self._selectDay })
    local param = {}
    param.selectFriendInfo = self._selectFriendInfo
    param.opt = SxvipConfig.OPT.Get
    param.day = self._selectDay
    XH.lobby:getModule("Sxvip"):reqFellowSend(param)
end

-- 赠送
function SxvipGiveView:onBtnSend(sender, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if not self._selectFriendInfo.numid then
        XH.TipTool.showToast("请选择牌友赠送")
        return
    end
    if self._selectDay == 0 then
        XH.TipTool.showToast("请选择赠送的会员天数")
        return
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyhz24051403, { item_id = self._selectDay })
    if not XH.lobby:getModule("Sxvip"):isVipEnoughToSend(self._selectDay) then
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK_CANCEL,
            funcOK = function()
                XH.viewManager:closeView('SxvipGiveView')
                XH.viewManager:closeView('SxvipView')
                XH.viewManager:openView("SxvipView", nil, {defalutMenu = SxvipConfig.MENU_DEFINE.BUY_VIP})
            end,
        } , "会员天数不足，是否前往商城补充？")
        return
    end
    XH.TipTool.showTip( {
        type = XH.TIP_LAYER_TYPE.OK_CANCEL,
        funcOK = function()
            local param = {}
            param.selectFriendInfo = self._selectFriendInfo
            param.opt = SxvipConfig.OPT.Send
            param.day = self._selectDay
            XH.lobby:getModule("Sxvip"):reqFellowSend(param)
        end,
    } , string.format("是否确定赠送【%d天会员】给\n【%s】?", self._selectDay, XH.StringTool.cutStringByLength(clone(self._selectFriendInfo.nickname), 10)) )
end

function SxvipGiveView:onBtnRecord(sender, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.hyhz24051404)
    XH.viewManager:openView("SxvipRecordView")
end

function SxvipGiveView:onTouchGiveItem(sender, eventType)
    local name = sender:getName()
    if name == '_KW_GIVE_ITEM_1' and self._selectDay ~= 3 then
        self._selectDay = 3
        XH.UITool.setVisible(self._btnGiveItem_1, "_KW_IMG_SEL_BG", true)
        XH.UITool.setVisible(self._btnGiveItem_2, "_KW_IMG_SEL_BG", false)
    elseif name == '_KW_GIVE_ITEM_2' and self._selectDay ~= 7 then
        self._selectDay = 7
        XH.UITool.setVisible(self._btnGiveItem_1, "_KW_IMG_SEL_BG", false)
        XH.UITool.setVisible(self._btnGiveItem_2, "_KW_IMG_SEL_BG", true)
    end
end

function SxvipGiveView:onUpdateMessage()
    local messageCount = XH.lobby:getModule("Sxvip"):getMessageCount()
    if messageCount > 0 then
        local imgMessage = self._btnRecord:getChildByName("_KW_IMG_MESSAGE")
        imgMessage:setVisible(true)
        local messageStr = tostring(messageCount)
        if messageCount > 99 then
            messageStr = "99+"
        end
        XH.UITool.setText(imgMessage, "_KW_TEXT_MESSAGE_COUNT", messageStr)
    else
        XH.UITool.setVisible(self._btnRecord, "_KW_IMG_MESSAGE", false)
    end
end

function SxvipGiveView:onTouchEventList(send, event)
    if event ~= ccui.TouchEventType.ended and event ~= ccui.TouchEventType.canceled then
        return
    end
    local listInnerContaniner = self._friendList:getInnerContainer()
    local posY = listInnerContaniner:getPositionY()
    if posY > 0 then
        if self._reqNextPageLock == false then
            if (self._page + 1) >= XH.lobby:getModule("Sxvip"):getFriendMaxPage() then
                XH.TipTool.showToast("已经到底啦~")
                return
            end
            self._page = self._page + 1
            self._reqNextPageLock = true
            XH.lobby:getModule("Sxvip"):reqFriendList(self._page)
        end
    end
end

function SxvipGiveView:onBtnClose(sender, eventType)
    self:close()
end

return SxvipGiveView D  