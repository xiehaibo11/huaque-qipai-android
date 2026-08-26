local IMTeaHouseMemberView = class("IMTeaHouseMemberView", XH.ViewBase)
function IMTeaHouseMemberView:getCSBPath()
    return "cocosStudio/hall/CSB/IMTeaHouse/IMTeaHouseMemberView.csb"
end

function IMTeaHouseMemberView:getBindingInfo()
    return {       
        ["_KW_HEAD_ONE_ROW"] = {varName = "_listRow",type = XH.UI_TYPE.LAYOUT},
        ["_KW_PANEL_ITEM"] = {varName = "_headItem", type = XH.UI_TYPE.LAYOUT},
        ["_KW_ADD_GROUP_LIST"] = {varName = "_AddGroupList" , type = XH.UI_TYPE.LISTVIEW},
        ["_KW_MANAGE_GROUP_LIST"] = {varName = "_panelManageList"},
        ["_KW_ADD_MEMBER_BTN"] = {varName = "_addMemberBtn",type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchAddMemberBtn"},
        ["_KW_DELETE_MEMBER_BTN"] = {varName = "_delMemberBtn",type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchDelMemberBtn"},
        ["_KW_PLAYER_NAME"] = {varName = "_pyName", type = XH.UI_TYPE.TEXT},
        ["_KW_PLAYER_ID"] = {varName = "_pyNumID", type = XH.UI_TYPE.TEXT},
        ["_KW_HEAD_SELECT"] = {varName = "_headSelCheckBox",type = XH.UI_TYPE.CHECKBOX},
        ["_KW_HEAD_DELETE"] = {varName = "_headDelete", type = XH.UI_TYPE.BUTTON},
        ["_KW_ADD_FRIENDS"] = {varName = "_AddFriendbtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchAddFriend"},
        ["_KW_BTND_CLOSE"] = {varName = "_closeBtn",type = XH.UI_TYPE.IMAGE},
        ["_KW_BTN_FINISH"] = {varName = "_btnFisish", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchFinished"},
        ["_KW_ADD_GROUP_PANEL"] = {varName = "_panelAddGroup" , type = XH.UI_TYPE.LAYOUT},
        -- 分页按钮
        ["_KW_LEFT_ARROW"] = {varName = "_leftArrowBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchLeftArrow"},
        ["_KW_RIGHT_ARROW"] = {varName = "_rightArrowBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchRightArrow"},
        ["_KW_START_PAGE"] = {varName = "_starPageText"},
        ["_KW_TOTAL_PAGE"] = {varName = "_totalPageText"},
        --记录按钮
        ["_KW_RECORD_BTN"] = {varName = "_recordBtn",type = XH.UI_TYPE.BUTTON, onTouchEnded = "onOpenRecordView"},
        ["_KW_IMG_TITLE"] = {varName = "_titleImg",type = XH.UI_TYPE.IMAGE},
    }
end
--一行展示几个头像
local ONEROWSHOWCNT = 5
local ROWCNT = 3
local ONEPAGESHOWCNT = 15
--记录当前所有玩家列表
local MEMBERLISTITEM = {}
local VIEWTYPE = {
    MEMBERLIST = "MEMBERLIST",
    DELETEMEMBER = "DELETEMEMBER",
    MANAGEMENTLIST = "MANAGEMENTLIST"
}
function IMTeaHouseMemberView:ctor(info)
    IMTeaHouseMemberView.super.ctor(self)
    -- self.scene = info.scene

    self._memberList ={}--self:createTestData()--info.data or ""
    self._teaNum = info.nTeaNumber
    self._ownerId = info.nTeaOwnerNumid
    self._totalPage = 1
    self._curPage = 1
    
    --TODO：请求分组列表信息
    XH.lobby:getModule("Im"):reqGroupMemberList(self._teaNum)
    self._scene = info.scene
    self:dealClostBtnEvents()
end

function IMTeaHouseMemberView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_TEA_HOUSE_LIST_CHANGED", callBack = "onRefreshList" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_MEMBERLIST_KICK", callBack = "onRefreshListData" },
        { module = XH.lobby:getModule("Im"), eventKeyName = "EVENT_GORUP_MEMBER_LIST_UPDATE", callBack = "onRefreshMemberList" },
    }
end

function IMTeaHouseMemberView:createTestData()
    local testPlayerData = {}
    for i =1 ,55 do
        local tem  = {}
        tem.numid = 10000000 + i
        tem.nickname = "00001" .. i 
        tem.url = ""
        table.insert(testPlayerData,tem)
    end
    return testPlayerData
end

function IMTeaHouseMemberView:initPanel()
    if self._scene == VIEWTYPE.MANAGEMENTLIST then
        self._addMemberBtn:setVisible(true)
        self._delMemberBtn:setVisible(true)
        self._panelManageList:setVisible(true)
        self._AddGroupList:setVisible(false)

        return self._panelManageList
    else
        self._addMemberBtn:setVisible(false)
        self._delMemberBtn:setVisible(false)
        self._panelManageList:setVisible(false)
        self._AddGroupList:setVisible(true)
        return self._AddGroupList
    end
end

--初始化颜色ITEM的整体布局
function IMTeaHouseMemberView:onRefreshList(event)
    self._curPanel = self:initPanel()
    self._curPanel:removeAllItems()
    MEMBERLISTITEM = {}
    if event then
        self._memberList = event.info
    end
    local celCnt = 0
    local modCnt = 0
    if self._memberList and next(self._memberList) ~= nil then
        celCnt,modCnt = math.modf(#self._memberList / ONEPAGESHOWCNT)
    end
    if modCnt ~= 0 then
        celCnt = celCnt + 1
    end
    self._totalPage = celCnt
    local curPage = self._curPage
    local totalPage = self._totalPage
    if next(self._memberList) ~= nil then
        self._curPanel:setScrollBarEnabled(false)
        local ROW
        for i = 1, ROWCNT do
            ROW = self._listRow:clone()
            for j = (i-1) * ONEROWSHOWCNT + 1 , i * ONEROWSHOWCNT  do
                local index = j + (curPage -1) * ONEPAGESHOWCNT
                if self._memberList[index] then
                    local memberInfo = self._memberList[index].stUserInfo
                    local item = self._headItem:clone()
                    item:setName("_KW_HEAD_ITEM_"..index)
                    item:getChildByName("_KW_PLAYER_NAME"):setString(XH.StringTool.cutStringByLength(memberInfo.acNickName or "",12))
                    item:getChildByName("_KW_PLAYER_ID"):setString(memberInfo.nNumId)
                    self:updateImg(item:getChildByName("_KW_IMG_HEAD"), memberInfo.acHeadUrl)

                    item.name = memberInfo.acNickName
                    item.numberId = memberInfo.nNumId
                    item.url = memberInfo.acHeadUrl
                    
                    if self._scene == VIEWTYPE.DELETEMEMBER and self._ownerId ~= memberInfo.nNumId then--如果是删除的场景,展示删除按钮
                        local delNode = item:getChildByName("_KW_HEAD_DELETE")
                        delNode:setName("_KW_HEAD_ITEM_"..index)
                        delNode:setVisible(true)
                        delNode:addTouchEventListener(handler(self, self.onTouchHeadDeleteEvents))
                    -- else--其他场景点击头像事件  一期查看组员暂不做，先预留
                    --     item:addTouchEventListener(handler(self, self.onTouchHeadEvents))
                    end
                    
                    ROW:addChild(item)

                    table.insert(MEMBERLISTITEM,item)

                    local whiteWidth = 0
                    local rowSizeWidth = ROW:getContentSize().width / ONEROWSHOWCNT - 20
                    local headPos = j % ONEROWSHOWCNT
                    if headPos ~= 0 then
                        item:setPosition(cc.p(rowSizeWidth * (headPos - 1) + whiteWidth,0))
                    else
                        item:setPosition(cc.p(rowSizeWidth * (ONEROWSHOWCNT - 1) + whiteWidth,0))
                    end
                end
            end
            self._curPanel:addChild(ROW)
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
    end
end

function IMTeaHouseMemberView:getData()
    if not self._imData then
        self._imData = XH.lobby:getModule("Im"):getIMData()
    end
    return self._imData
end

function IMTeaHouseMemberView:updateImg(node,url)
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

function IMTeaHouseMemberView:onTouchLeftArrow(send,eventType)
    self._curPage = self._curPage - 1
    self:onRefreshList()
    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_PAGEBTN_CLICK)
end

function IMTeaHouseMemberView:onTouchRightArrow(send,eventType)
    self._curPage = self._curPage + 1
    self:onRefreshList()
    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_PAGEBTN_CLICK,{extended = "{\"id\":2223}"})
end

function IMTeaHouseMemberView:onClose(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.lobby:getModule("TeaHouseList"):reqTeaHouseList()
    self:close()
    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_MEMBERLIST_CLOSE_CLICK)
end

function IMTeaHouseMemberView:onTouchHeadDeleteEvents(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local tmpInfo = {data = {} }
    for i in ipairs(MEMBERLISTITEM) do
        if MEMBERLISTITEM[i]:getName() == send:getName() then
            tmpInfo.data.num = MEMBERLISTITEM[i].numberId
            tmpInfo.data.name = MEMBERLISTITEM[i].name
            tmpInfo.data.url = MEMBERLISTITEM[i].url   
            tmpInfo.data.teaNum = self._teaNum
            tmpInfo.data.scene = "DELETEFROMTEA"
            break
        end
    end

    --二次确认弹窗
    --踢出去后请求刷新列表
    XH.viewManager:openView("IMTeaHouseTipsView",nil,tmpInfo) 
end

function IMTeaHouseMemberView:onRefreshListData(event)
    if event then
        for i,v in ipairs(self._memberList) do
            if v.stUserInfo.nNumId == event.numberId then
                table.remove(self._memberList,i)
            end
        end
    end
    self:onRefreshList()
end

function IMTeaHouseMemberView:onOpenRecordView(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.viewManager:openView("IMTeahouseRecordView",nil,self._teaNum)
    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_RECORDBTN_CLICK)
end

function IMTeaHouseMemberView:onTouchAddMemberBtn(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local tmpData = {}
    tmpData.scene = "ADDMEMBER"
    tmpData.teaNumber = self._teaNum
    XH.viewManager:openView("IMAddGroupView", nil, {info = tmpData})
    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_ADDMEMBERBTN_CLICK,{item_id = self._teaNum})
end

function IMTeaHouseMemberView:onRefreshMemberList()
    XH.lobby:getModule("Im"):reqGroupMemberList(self._teaNum)
end

function IMTeaHouseMemberView:onTouchDelMemberBtn(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if self._memberList and #self._memberList == 1 then
        XH.TipTool.showToast("无组员可删除")
        return
    end
    self._scene = VIEWTYPE.DELETEMEMBER
    self._titleImg:loadTexture("imteahouse_delete_member.png", ccui.TextureResType.plistType)
    self._closeBtn:addTouchEventListener(handler(self, self.onTouchToAddMemberView))
    self:onRefreshList()
    XH.lobby:getModule("Im"):newThrowData(XH.NewThrowDataDefine.GROUP_DELMEMBERBTN_CLICK,{item_id = self._teaNum})
end

function IMTeaHouseMemberView:onTouchToAddMemberView(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._scene = VIEWTYPE.MANAGEMENTLIST
    self._titleImg:loadTexture("imteahouse_check_member.png", ccui.TextureResType.plistType)
    self:dealClostBtnEvents()
    self:onRefreshList()
    
end


function IMTeaHouseMemberView:dealClostBtnEvents()
    if self._scene == VIEWTYPE.DELETEMEMBER then
        self._closeBtn:addTouchEventListener(handler(self, self.onTouchToAddMemberView))
    else
        self._closeBtn:addTouchEventListener(handler(self, self.onClose))
    end
end
--点击头像事件 一期查看组员暂不用该功能，预留供以后使用
-- function IMTeaHouseMemberView:onTouchHeadEvents(send,eventType)
--     if eventType ~= ccui.TouchEventType.ended then
--         return
--     end
--     self:updateHeadState(send)
-- end

--  --更新头像选择状态 一期暂无需用到，预留供以后使用
-- function IMTeaHouseMemberView:updateHeadState(send)
--     for i in ipairs(MEMBERLISTITEM) do
--         if MEMBERLISTITEM[i]:getName() == send:getName() then
--             local tmpNumID = tonumber(MEMBERLISTITEM[i]:getChildByName("_KW_PLAYER_ID"):getString())
--             local selectItem = MEMBERLISTITEM[i]:getChildByName("_KW_HEAD_SELECT")
--             if not selectItem:isVisible() then 
--                 selectItem:setVisible(true)
--             else
--                 selectItem:setVisible(false)
--             end
--             return
--         end
--     end
-- end

return IMTeaHouseMemberView�3  