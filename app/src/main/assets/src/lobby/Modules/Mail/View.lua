-- 邮件UI
local MailView = class("MailView", XH.ViewBase)
local KW_TBLEVIEW_NEED_OFFSET = 160
--local REALNAME_ACTON_MAILID = 519

function MailView:getCSBPath()
    return "cocosStudio/hall/CSB/Mail/MailMainLayer.csb"
end

function MailView:getBindingInfo()
    return {
        -- 关闭按钮
        ["_KW_BTN_CLOSE"] = {varName = "_closeBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchClose"},
        -- 邮件详情-标题
        ["_KW_BMFONT_MAIL_DETAIL_TITLE"] = {varName = "_bmfontDetailTitle"},
        -- 邮件详情-发件人
        ["_KW_BMFONT_MAIL_DETAIL_FROM"] = {varName = "_bmfontDetailFrom"},
        -- 邮件详情-发件时间
        ["_KW_BMFONT_MAIL_DETAIL_TIME"] = {varName = "_bmfontDetailTime"},
        -- 邮件详情-文本滚动视图
        ["_KW_MAIL_SCROLLVIEW"] = {varName = "_detailScrollView"},
        -- 邮件详情-邮件正文
        ["_KW_MAIL_DETAIL_CONTENT"] = {varName = "_detailContent"},
        -- 邮件详情-奖励背景图
        ["_KW_MAIL_AWARD_BG"] = {varName = "_detailAwardBg"},
        -- 邮件详情-奖励列表
        ["_KW_MAIL_AWARD_LIST"] = {varName = "_detailAwardList"},
        ["_KW_AWARD_ITEM"] = {varName = "_awardItem"},
        -- 邮件详情-删除
        ["_KW_DETAIL_BTN_DEL"] = {varName = "_delBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchDel"},
        ["_KW_DETAIL_BTN_DEL_ONLY"] = {varName = "_delOnlyBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchDel"},
        -- 邮件详情-领取
        ["_KW_DETAIL_BTN_AWARD"] = {varName = "_awardBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchAward"},
        -- 邮件列表
        ["_KW_MAIL_LIST"] = {varName = "_mailListView"},
        -- 一键已读
        ["_KW_BTN_READALL"] = {varName = "_readAllBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchReadAll"},
        -- 一键领取
        ["_KW_BTN_AWARDALL"] = {varName = "_awardAllBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchAwardAll"},
        -- 全选复选框
        ["_KW_CHECKBOX_ALL"] = {varName = "_selectedAllCheckBox"},
        -- 删除
        ["_KW_BTN_DEL"] = {varName = "_delSelectedBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchDelSelected"},
        -- 无详情
        ["_KW_NO_DETAIL"] = {varName = "_noDetail"},
        -- 邮件详情
        ["_KW_MAIL_DETAIL_INFO"] = {varName = "_detailInfoPanel"},
        -- 邮件列表
        ["_KW_MAIL_LIST_PANEL"] = {varName = "_mailPanel"},
        -- 无邮件
        ["_KW_NO_MAIL"] = {varName = "_noMail"},
        ["_KW_MAIL_DETAIL_PANEL"] = {varName = "_detailPanel"},
        -- 分割图
        ["_KW_IMG_DIVISION"] = {varName = "_mailDivison"},
        -- title
        ["_KW_IMG_TITLE"] = {varName = "_mailTitle"},
        -- title + 邮件数
        ["_KW_MAIL_COUNT_PANEL"] = {varName = "_mailCountPanel"},
        -- 邮件数
        ["_KW_MAIL_COUNT"] = {varName = "_mailCount"}
    }
end

function MailView:getProxyEvents()
    return {
        {module = self._mailData, eventKeyName = "EVENT_MAIL_DATA_CHANGED", callBack = "onMailDataEvent"},
        {module = self._mailData, eventKeyName = "EVENT_MAIL_DETAIL_INFO", callBack = "onMailDetailEvent"}
    }
end

function MailView:ctor()
    self:initData()
    MailView.super.ctor(self)
    self._mailArray = {}
    self._isTableViewBottom = false
    self._isTableViewTop = false
    self:initUI()
    -- -- 测试
    self:reqGetPlayerMailList()
    self._selectedAllCheckBox:addEventListener(handler(self, self.onTouchSelectedAll))
end

function MailView:reqGetPlayerMailList()
    if self._mailData:getMailCount() == 0 or self._mailData:getNewMailCount() > 0 then
        XH.lobby:getModule("Mail"):reqGetPlayerMailList()
        self._mailData._newMailCount = 0
        self._mailData:setMailFirst(false)
    else
        self._mailData:sortMailList()
        self:refreshData()
    end
end

function MailView:initData()
    self._mailData = XH.lobby:getModule("Mail"):getMailData()
end

function MailView:initUI()
    self._detailScrollView:setScrollBarEnabled(false)
    self._detailAwardList:setScrollBarEnabled(false)
    self._mailListView:setScrollBarEnabled(false)
    self._mailTableView = self:createTableView(self._mailListView)
    self._mailTableView:reloadData()
    if self._detailAwardList and self._awardItem then
        self._detailAwardList:removeAllChildren()
    end
    self._awardAllBtn:setEnabled(true)
    self:changeMailState()
end

function MailView:onMailDataEvent(event)
    self:refreshData()
end

function MailView:onMailDetailEvent(event)
    self:refreshData()
    self:refreshDetail()
end

-- 停留在当前位置
function MailView:refreshData()
    self._mailArray = self._mailData:getMailList()
    local tableOffset = self._mailTableView:getContentOffset()
    local tableSize1 = self._mailTableView:getContentSize()
    local tableOffsetY = tableSize1.height + tableOffset.y
    self._mailTableView:reloadData()
    local tableSize2 = self._mailTableView:getContentSize()
    tableOffset.y = tableOffsetY - tableSize2.height
    local listSize = self._mailListView:getContentSize()
    local offsetHeight = 0
    if tableSize2.height < listSize.height then
        offsetHeight = listSize.height - tableSize2.height
    end
    if tableOffset.y > offsetHeight then
        tableOffset.y = offsetHeight
    end
    self._mailTableView:setContentOffset(tableOffset)

    self:changeMailState()
end

function MailView:refreshDetail()
    if not self._mailDetail then
        return
    end
    self:changeMailState()
    self._detailAwardList:removeAllChildren()
    self._curMailInfo = self._mailData:getMailInfoByMailId(self._mailDetail.mail_id)

    if self._curMailInfo then
        self._bmfontDetailTitle:setText(string.restorehtmlspecialchars(self._curMailInfo.title or ""))
        self._bmfontDetailFrom:setText(string.restorehtmlspecialchars(self._mailDetail.sender or ""))
        self._bmfontDetailTime:setText(self._mailDetail.send_time)
        local str = string.restorehtmlspecialchars(self._mailDetail.mail_content) or ""
        self._detailContent:setString(str)
        self._delBtn:setVisible(self._curMailInfo.isAnnex)
        self._awardBtn:setVisible(self._curMailInfo.isAnnex)
        self._delOnlyBtn:setVisible(not self._curMailInfo.isAnnex)

        self._awardBtn:setBright(true)
        self._awardBtn:setEnabled(true)

        local scrollSize = self._detailScrollView:getContentSize()
        self._detailContent:setTextAreaSize(cc.size(scrollSize.width, 0))

        if self._detailContent:getContentSize().height > scrollSize.height then
            scrollSize.height = self._detailContent:getContentSize().height
        end
        self._detailContent:setContentSize(scrollSize.width, scrollSize.height)
        self._detailScrollView:setInnerContainerSize(cc.size(scrollSize.width, scrollSize.height))
        self._detailContent:setPositionY(scrollSize.height)

        local showAward = #self._mailDetail.award_list > 0 and true or false

        self._detailAwardBg:setVisible(showAward)

        local award_list = self._mailDetail.award_list
        for i = 1, #award_list do
            local item = self._awardItem:clone()
            local award_icon = XH.UITool.seekNodeByName(item, "KW_AWARD_BG")
            if award_icon then
                self:updateImg(award_icon, award_list[i].url)
            end
            local award_desc = item:getChildByName("KW_AWARD_INFO_DESC")
            if award_desc then
                award_desc:setString(award_list[i].desc)
            end
            item:setVisible(true)
            self._detailAwardList:addChild(item)
        end

        if #award_list > 0 and self._curMailInfo.isAnnex == false then
            self._delBtn:setVisible(true)
            self._awardBtn:setVisible(true)
            self._delOnlyBtn:setVisible(false)
            self._awardBtn:setBright(false)
            self._awardBtn:setEnabled(false)
        end
    end
end

function MailView:updateImg(node, url)
    if not url then
        return
    end

    local size = node:getContentSize()
    local imageNode = XH.RemoteImage.new():setTouchEnabled(true):setPosition(size.width / 2, size.height / 2):setContentSize(size.width, size.height):setMaxContentSize(cc.size(size.width * 0.85,size.height * 0.85)):addTo(node)
    imageNode:setUrl(url, true)
end

function MailView:setSelectedType(type)
    for index, mailInfo in ipairs(self._mailArray) do
        mailInfo.isSelected = type
        local cell = self._mailTableView:cellAtIndex(index - 1)
        if cell ~= nil then
            local mailInfoItem = cell:getChildByName("MailInfoItem")
            if mailInfoItem ~= nil then
                mailInfoItem:updateMailInfo(mailInfo)
            end
        end
    end
end

--点击复选按钮
function MailView:onTouchSelectedAll(send, eventType)
    if eventType == ccui.CheckBoxEventType.selected then
        self:setSelectedType(true)
    elseif eventType == ccui.CheckBoxEventType.unselected then
        self:setSelectedType(false)
    end
end

function MailView:onTouchDel(send, eventType)
    if self._curMailInfo then
        if self._curMailInfo.isAnnex then
            XH.TipTool.showToast("您有未领取的附件，无法删除", 3)
            return
        end
        XH.lobby:getModule("Mail"):reqDelMail(1, {self._curMailInfo.mail_id})
    end
end

function MailView:onTouchAward(send, eventType)
    if self._curMailInfo.title == '实名认证' then
        --弹实名认证
        if XH.playerData:getIdentityStatus() == 2 then
            XH.viewManager:openView("IdentityCheckView",nil,{isAutoBindPhone = true})
            return
        end
    end
    if self._curMailInfo and self._curMailInfo.isAnnex then
        send:setEnabled(false)
        XH.lobby:getModule("Mail"):reqGetAward(1, {self._curMailInfo.mail_id})
    end
end

function MailView:onTouchReadAll(send, eventType)
    local mails = self._mailData:getUnReadMails()
    if #mails == 0 then
        XH.TipTool.showToast("当前没有未读邮件！", 3)
        return
    end
    XH.lobby:getModule("Mail"):reqReadAllMail()
    -- 测试
    self._mailData:testReadAll()
end

function MailView:onTouchAwardAll(send, eventType)
    send:setEnabled(false)
    local mails = self._mailData:getAwardMails()
    if #mails == 0 then
        XH.TipTool.showToast("当前没有奖励可领取！", 3)
        return
    end   
    for _, value in ipairs(self._mailArray) do
        if value.title == '实名认证' then
            --弹实名认证
            if XH.playerData:getIdentityStatus() == 2 then
                XH.viewManager:openView("IdentityCheckView",nil,{isAutoBindPhone = true})
                send:setEnabled(true)
                return
            end
        end
    end
    XH.lobby:getModule("Mail"):reqGetAward(#mails, mails)
end
function MailView:onTouchDelSelected(send, eventType)
    local selectCnt = self:getSelectedCnt()
    if selectCnt == 0 then
        XH.TipTool.showToast("你未选中任何邮件！", 3)
    end
    self._selectedAllCheckBox:setSelected(false)

    self:reqDel()
end

-- UI显示
function MailView:changeMailState()
    self._mailDetail = self._mailData:getMailDetail()
    local hasMail = #self._mailArray > 0 and true or false
    local hasDetail = self._mailDetail and true or false
    self._mailTitle:setVisible(not hasMail)
    self._mailCountPanel:setVisible(hasMail)
    self._mailDivison:setVisible(hasMail)
    self._noMail:setVisible(not hasMail)
    self._mailPanel:setVisible(hasMail)
    self._detailPanel:setVisible(hasMail)
    self._noDetail:setVisible(not hasDetail)
    self._detailInfoPanel:setVisible(hasDetail)
    local str = self._mailData:getMailCountStr()
    self._mailCount:setString(str)
end

-- 关闭
function MailView:onTouchClose(send, eventType)
    self:setSelectedType(false)

    -- 重置详情数据
    self._mailData:resetDetail()

    XH.lobby:getModule("Mail"):reqCheckNewMail()

    self:close()
end

function MailView:createTableView(rootPanel)
    if rootPanel then
        local tableView = cc.TableView:create(rootPanel:getContentSize())
        tableView:setDirection(cc.SCROLLVIEW_DIRECTION_VERTICAL)
        tableView:setVerticalFillOrder(cc.TABLEVIEW_FILL_TOPDOWN)
        tableView:setIgnoreAnchorPointForPosition(false)
        tableView:setAnchorPoint(cc.p(0, 0))
        tableView:setPosition(cc.p(0, 0))
        tableView:setDelegate()
        tableView:setBounceable(true)
        tableView:registerScriptHandler(handler(self, self.onTableCellTouched), cc.TABLECELL_TOUCHED)
        tableView:registerScriptHandler(handler(self, self.onCellSizeForTable), cc.TABLECELL_SIZE_FOR_INDEX)
        tableView:registerScriptHandler(handler(self, self.onTableCellAtIndex), cc.TABLECELL_SIZE_AT_INDEX)
        tableView:registerScriptHandler(handler(self, self.onNumberOfCellsInTableView), cc.NUMBER_OF_CELLS_IN_TABLEVIEW)
        tableView:registerScriptHandler(handler(self, self.onScrollViewScriptScroll), cc.SCROLLVIEW_SCRIPT_SCROLL)
        rootPanel:addChild(tableView)
        return tableView
    end
    return nil
end

function MailView:onScrollViewScriptScroll(tableView)
    local listPanel = self._mailListView
    if not listPanel or not self._mailTableView then
        return
    end

    local listSize = listPanel:getContentSize()
    local tableSize = self._mailTableView:getContentSize()
    local offsetHeight = 0
    if tableSize.height < listSize.height then
        offsetHeight = listSize.height - tableSize.height
    end
    local tableOffset = self._mailTableView:getContentOffset()
    tableOffset.y = tableOffset.y - offsetHeight
    if tableOffset.y > KW_TBLEVIEW_NEED_OFFSET and not self._isTableViewBottom then
        self._isTableViewBottom = true
        self:onScrollToBottomOrTop()
    end
    local offset = listSize.height - tableSize.height

    if (offset - tableOffset.y) > KW_TBLEVIEW_NEED_OFFSET and not self._isTableViewTop then
        self._isTableViewTop = true
        self:onScrollToBottomOrTop()
    end
    if tableOffset.y <= 0 then
        self._isTableViewBottom = false
    end
    if offset - tableOffset.y < 10 then
        self._isTableViewTop = false
    end
end

-- 拉取最新
function MailView:onScrollToBottomOrTop()
    XH.lobby:getModule("Mail"):reqGetPlayerMailList()
end

function MailView:onTableCellTouched(tableView, cell)
    local index = cell:getIdx()
    if cell:getIdx() == -1 then
        index = 0
    end

    if index < #self._mailArray then
        local mailInfo = self._mailArray[index + 1]
        if self._mailDetail and self._mailDetail.mail_id == mailInfo.mail_id then
            return
        end
        XH.lobby:getModule("Mail"):reqGetMailDetail(mailInfo.mail_id)
    end
end

function MailView:onCellSizeForTable(tableView, index)
    if self._mailListView then
        local size = self._mailListView:getContentSize()
        return size.width, 160
    end
    return 625, 160
end

function MailView:getSelectedCnt()
    local selectCnt = 0
    for _, mailInfo in ipairs(self._mailArray) do
        if mailInfo.isSelected == true then
            selectCnt = selectCnt + 1
        end
    end
    return selectCnt
end

function MailView:updateCheckBox()
    local selectCnt = self:getSelectedCnt()
    if selectCnt == 0 then
        self._selectedAllCheckBox:setSelected(false)
    end
    if selectCnt == #self._mailArray and selectCnt > 0 then
        self._selectedAllCheckBox:setSelected(true)
    end
end

function MailView:onTableCellAtIndex(tableView, index)
    local callFunc = function()
        self:updateCheckBox()
    end
    local cell = tableView:dequeueCell()
    if not cell then
        cell = cc.TableViewCell:new()
        local mailInfoItem = require("lobby.Modules.Mail.InfoItemView").new(self._mailArray[index + 1])
        if mailInfoItem ~= nil then
            mailInfoItem:setAnchorPoint(cc.p(0, 0))
            mailInfoItem:setPosition(0, 0)
            mailInfoItem:setName("MailInfoItem")
            mailInfoItem:setCallFunc(callFunc)
            cell:addChild(mailInfoItem)
        end
    end
    local mailInfoItem = cell:getChildByName("MailInfoItem")
    if mailInfoItem ~= nil then
        mailInfoItem:updateMailInfo(self._mailArray[index + 1])
    end
    return cell
end

function MailView:onNumberOfCellsInTableView(tableView)
    return #self._mailArray or 0
end

function MailView:reqDel()
    local delMail = {}
    local delMailId = {}
    for _, mailInfo in ipairs(self._mailArray) do
        if mailInfo.isSelected == true then
            delMail[#delMail + 1] = mailInfo
        end
    end

    if #delMail == 1 and delMail[1].isAnnex then
        XH.TipTool.showToast("您有未领取的附件，无法删除", 3)
        return
    end

    for i = 1, #delMail do
        if delMail[i].isAnnex == false and delMail[i].isRead == true then
            delMailId[#delMailId + 1] = delMail[i].mail_id
        end
    end

    if #delMailId > 0 then
        XH.lobby:getModule("Mail"):reqDelMail(#delMailId, delMailId)
    elseif #delMail > 0 then
        XH.TipTool.showToast("您有未领取的附件或未读邮件，无法删除", 3)
    end
end

return MailView
  *E  