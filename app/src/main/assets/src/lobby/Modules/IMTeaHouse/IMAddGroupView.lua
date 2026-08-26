local IMAddGroupView = class("IMAddGroupView", XH.ViewBase)
function IMAddGroupView:getCSBPath()
    return "cocosStudio/hall/CSB/IMTeaHouse/IMTeaHouseAddGroup.csb"
end

function IMAddGroupView:getBindingInfo()
    return {       
        ["_KW_HEAD_ONE_ROW"] = {varName = "_listRow",type = XH.UI_TYPE.LAYOUT},
        ["_KW_PANEL_ITEM"] = {varName = "_headItem", type = XH.UI_TYPE.LAYOUT},
        ["_KW_ADD_GROUP_LIST"] = {varName = "_AddGroupList" , type = XH.UI_TYPE.LISTVIEW},
        ["_KW_PLAYER_NAME"] = {varName = "_pyName", type = XH.UI_TYPE.TEXT},
        ["_KW_PLAYER_ID"] = {varName = "_pyNumID", type = XH.UI_TYPE.TEXT},
        ["_KW_HEAD_SELECT"] = {varName = "_headSelCheckBox",type = XH.UI_TYPE.CHECKBOX},
        ["_KW_HEAD_DELETE"] = {varName = "_headDelete", type = XH.UI_TYPE.BUTTON},
        ["_KW_ADD_FRIENDS"] = {varName = "_AddFriendbtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchAddFriend"},
        ["_KW_BTND_CLOSE"] = {varName = "_close",type = XH.UI_TYPE.IMAGE, onTouchEnded = "onClose"},
        ["_KW_BTN_FINISH"] = {varName = "_btnFisish", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchFinished"},
        ["_KW_ADD_GROUP_PANEL"] = {varName = "_panelAddGroup" , type = XH.UI_TYPE.LAYOUT},
        ["_KW_PANEL_NO_GROUP"] = {varName = "_panelNoGroup", type = XH.UI_TYPE.LAYOUT },
        ["_KW_IMG_TITLE"] = {varName = "_titleImg",type = XH.UI_TYPE.IMAGE},
        -- 分页按钮
        ["_KW_LEFT_ARROW"] = {varName = "_leftArrowBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchLeftArrow"},
        ["_KW_RIGHT_ARROW"] = {varName = "_rightArrowBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchRightArrow"},
        ["_KW_START_PAGE"] = {varName = "_starPageText"},
        ["_KW_TOTAL_PAGE"] = {varName = "_totalPageText"},
    }
end
--一行展示几个头像
local ONEROWSHOWCNT = 5
--记录当前所有玩家列表
local MEMBERLISTITEM = {}
local tmpFriendList = {}
local SCENETYPE = {
    ADDMEMBER = "ADDMEMBER",
    ADDGROUP = "ADDGROUP"
}
local KW_TITLE_PNG = "imteahouse_add_zuyuan.png"
function IMAddGroupView:ctor(data)
    IMAddGroupView.super.ctor(self)
    self._finalAddList = {}
    self._teaMemberList = {}
    self._selectNum = 0
    self._scene = SCENETYPE.ADDGROUP
    if data.info then
        self._teaNum = data.info.teaNumber
        if data.info.scene then
            self._scene = SCENETYPE[data.info.scene]
            if self._scene == SCENETYPE.ADDMEMBER then
                self._titleImg:loadTexture(KW_TITLE_PNG, ccui.TextureResType.plistType)
            end
        end
    end
    self:initView()
end

function IMAddGroupView:getProxyEvents()
    return {
        { module = self:getData(), eventKeyName = "EVENT_LIST_DATA_CHANGED", callBack = "onRefreshPaiYouList" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_TEA_HOUSE_LIST_CHANGED", callBack = "onRefreshTeaMemberList" },
    }
end

function IMAddGroupView:createTestData()
    local testPlayerData = {}
    for i =1 ,27 do
        local tem  = {}
        tem.numid = 10000000 + i
        tem.nickName = "00001" .. i 
        tem.url = ""
        table.insert(testPlayerData,tem)
    end
    return testPlayerData
end

function IMAddGroupView:initView()
    --1、请求牌友列表服务，获取总页数
    --2、监听回调，刷新界面
    self:clearListCache()
    self:updateFinishBtnCnt(0)
    XH.lobby:getModule("Im"):reqFriendListInfo()
end

function IMAddGroupView:clearListCache()
    XH.lobby:getModule("Im"):setCurFriendListPage(0)
    XH.lobby:getModule("Im"):setTotalFriendListPage(0)
    self:getData():clearFriendList()
end

--初始化颜色ITEM的整体布局
function IMAddGroupView:onRefreshList(event)
    self._AddGroupList:removeAllItems()
    -- self._AddGroupList:removeAllChildren()
    MEMBERLISTITEM = {}
    tmpFriendList = self:getData():getFriendList()
    if next(tmpFriendList) ~= nil then
        local curPage = XH.lobby:getModule("Im"):getCurFriendListPage()
        local totalPage = XH.lobby:getModule("Im"):getTotalFriendListPage()
        self._starPageText:setString(curPage)
        self._totalPageText:setString(totalPage)
        self._AddGroupList:setScrollBarEnabled(false)
        local ROW
        for i = 1, 2 do
            ROW = self._listRow:clone()
            for j = (i-1) * ONEROWSHOWCNT + 1 , i * ONEROWSHOWCNT  do
                local index = j + (curPage -1) * 10 
                if tmpFriendList[index] then
                    local item = self._headItem:clone()  
                    item:setName("_KW_HEAD_ITEM_"..index)
                    item:getChildByName("_KW_PLAYER_NAME"):setString(XH.StringTool.cutStringByLength(tmpFriendList[index].nickname or "",12))
                    item:getChildByName("_KW_PLAYER_ID"):setString(tmpFriendList[index].numid)
                    -- item:getChildByName("_KW_IMG_HEAD"):setUrl(tmpFriendList[j].url)
                    self:updateImg(item:getChildByName("_KW_IMG_HEAD"), tmpFriendList[index].headurl)
                    --如果当前玩家在比赛场列表里，也在牌友列表里，需要置灰不可选中
                    if self._scene == SCENETYPE.ADDMEMBER then
                        if tmpFriendList[index].noSelect then
                            item:getChildByName("_KW_HEAD_SELECT"):setVisible(true)
                            item:getChildByName("_KW_HEAD_SELECT"):getVirtualRenderer():setState(1)
                        else
                            for _,v in ipairs(self._finalAddList) do
                                if tmpFriendList[index].numid == v then
                                    item:getChildByName("_KW_HEAD_SELECT"):setVisible(true)
                                end
                            end
                            item:addTouchEventListener(handler(self, self.onTouchHeadEvents))
                        end
                    else
                        item:addTouchEventListener(handler(self, self.onTouchHeadEvents))
                    end
                    item.name = tmpFriendList[index].nickname
                    item.numberId = tmpFriendList[index].numid
                    item.url = tmpFriendList[index].headurl

                    ROW:addChild(item)

                    table.insert(MEMBERLISTITEM,item)

                    local whiteWidth = 40
                    local rowSizeWidth = ROW:getContentSize().width / ONEROWSHOWCNT
                    local headPos = j % ONEROWSHOWCNT
                    if headPos ~= 0 then
                        item:setPosition(cc.p(rowSizeWidth * (headPos - 1) + whiteWidth,0))
                    else
                        item:setPosition(cc.p(rowSizeWidth * (ONEROWSHOWCNT - 1) + whiteWidth,0))
                    end
                end
            end
            self._AddGroupList:addChild(ROW)
        end
        if curPage ~= totalPage then
            self._leftArrowBtn:setEnabled(not (curPage == 1))
            self._rightArrowBtn:setEnabled(not (totalPage == 1))
        elseif curPage == totalPage then
                self._leftArrowBtn:setEnabled(not (curPage == 1))
                self._rightArrowBtn:setEnabled(false)
        else
            self._leftArrowBtn:setEnabled(true)
            self._rightArrowBtn:setEnabled(true)
        end
    else
        self._panelAddGroup:setVisible(false)
        self._panelNoGroup:setVisible(true)
    end
end

function IMAddGroupView:getData()
    if not self._imData then
        self._imData = XH.lobby:getModule("Im"):getIMData()
    end
    return self._imData
end

function IMAddGroupView:updateImg(node,url)
    if (not url) or (url == "")  then
        return
    end
    local size = node:getContentSize()
    local imageNode = XH.RemoteImage.new()
        :setPosition(size.width/2, size.height/2)
        :ignoreContentAdaptWithSize(true)
        :addTo(node)
    imageNode:setUrl(url)
end

function IMAddGroupView:onTouchLeftArrow(send,eventType)
    local tmpCurPage = XH.lobby:getModule("Im"):getCurFriendListPage()-1
    if tmpCurPage - 1 >= 0 then
        XH.lobby:getModule("Im"):setCurFriendListPage(tmpCurPage)
        self:onRefreshList()
    end
end

function IMAddGroupView:onTouchRightArrow(send,eventType)
    local tmpCurPage = XH.lobby:getModule("Im"):getCurFriendListPage()
    local tmpTotalPage = XH.lobby:getModule("Im"):getTotalFriendListPage() 
    if tmpCurPage <= tmpTotalPage - 1 then
        XH.lobby:getModule("Im"):reqFriendListInfoForGroup(tmpCurPage, tmpCurPage)
        XH.lobby:getModule("Im"):setCurFriendListPage(tmpCurPage+1)
    end
end

--点击头像事件 
--todo：记录当前玩家id，加入一个table里 
function IMAddGroupView:onTouchHeadEvents(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:updateHeadState(send)
end

function IMAddGroupView:onTouchHeadDeleteEvents(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local tmpInfo = {}
    for i in ipairs(MEMBERLISTITEM) do
        if MEMBERLISTITEM[i]:getName() == send:getName() then
            tmpInfo.numberId = MEMBERLISTITEM[i].numberId
            tmpInfo.name = MEMBERLISTITEM[i].name
            tmpInfo.url = MEMBERLISTITEM[i].url         
            break
        end
    end

    --二次确认弹窗
    --踢出去后请求刷新列表
    XH.viewManager:openView("IMTeaHouseTipsView",nil,tmpInfo)
    
end

 --更新头像选择状态
function IMAddGroupView:updateHeadState(send)
    for i in ipairs(MEMBERLISTITEM) do
        if MEMBERLISTITEM[i]:getName() == send:getName() then
            local tmpNumID = tonumber(MEMBERLISTITEM[i]:getChildByName("_KW_PLAYER_ID"):getString())
            local selectItem = MEMBERLISTITEM[i]:getChildByName("_KW_HEAD_SELECT")
            if not selectItem:isVisible() then 
                selectItem:setVisible(true)
                self._selectNum = self._selectNum + 1
                table.insert(self._finalAddList,tmpNumID)
            else
                selectItem:setVisible(false)
                self._selectNum = self._selectNum - 1
                for k,v in ipairs(self._finalAddList) do
                    if v == tmpNumID then
                        table.remove(self._finalAddList,k)
                        break
                    end
                end
            end
            --处理该条数据到table里
            --处理完成后退出循环
            self:updateFinishBtnCnt(self._selectNum)
            return
        end
    end
end

function IMAddGroupView:onTouchFinished(send,eventType)
    if next(self._finalAddList) ~= nil then
        for i,v in ipairs(self._finalAddList) do
            if i == 1 then
                XH.lobby:getModule("Im"):reqInvitePlayer(self._teaNum,v,#self._finalAddList,true)
            else
                XH.lobby:getModule("Im"):reqInvitePlayer(self._teaNum,v,#self._finalAddList,false)                
            end
        end
    end
    XH.lobby:getModule("TeaHouseList"):reqTeaHouseList()
    self:close()
end

function IMAddGroupView:updateFinishBtnCnt(number)
    if number ~= 0 then
        self._btnFisish:setEnabled(true)
        self._btnFisish:setTitleText("完成(".. number ..")")
    else
        self._btnFisish:setEnabled(false)
        self._btnFisish:setTitleText("完成")
    end
end

function IMAddGroupView:onRefreshPaiYouList(event)
    self:onRefreshList()
    if self._scene == SCENETYPE.ADDMEMBER then
        XH.lobby:getModule("Im"):reqGroupMemberList(self._teaNum) -- 查看组员协议
    end
end

function IMAddGroupView:onRefreshTeaMemberList(event)
    if event.info then
        self._teaMemberList = event.info
        local teaMemberList = #tmpFriendList
        local count = 0
        for i,_ in ipairs(tmpFriendList) do
            for _,k in ipairs(self._teaMemberList) do
                local tmpData = k.stUserInfo
                if tmpData.nNumId == tmpFriendList[i].numid then
                    tmpFriendList[i].noSelect = true
                    count = count + 1
                end
            end
        end
        if teaMemberList == count then
            XH.TipTool.showToast("无多余牌友可添加")
        end
    end
    self:onRefreshList()
end

function IMAddGroupView:onTouchAddFriend(send,eventType)
    XH.viewManager:openView("IMAddPaiYouView")
    self:close()
end

function IMAddGroupView:onClose(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:close()
end

return IMAddGroupView82  