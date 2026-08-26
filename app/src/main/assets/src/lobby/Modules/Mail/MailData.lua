local MailData = class("MailData")

MailData.EVENT_MAIL_DATA_CHANGED = "EVENT_MAIL_DATA_CHANGED"
MailData.EVENT_MAIL_DETAIL_INFO = "EVENT_MAIL_DETAIL_INFO"

local MAIL_DEBUG = false

function MailData:ctor()
    cc(self):addComponent("components.behavior.EventProtocol"):exportMethods()
    -- 邮件列表
    self._mailArray = {}
    -- 最大邮件id
    self._maxMailID = 0
    -- 新邮件数量
    self._newMailCount = 0
    -- 邮件详情
    self._detailInfoArray = {}
    -- 当前邮件详情
    self._curDetailInfo = nil
    -- 是否登录后第一次拉取
    self._isFirst = true
end

function MailData:setMailFirst(isFirst)
    self._isFirst = isFirst
end

function MailData:getMailFirst()
    return self._isFirst
end

-- 设置邮件列表数据
function MailData:setMailList(mails)
    print("MailData:setMailList.." .. #mails)
    if mails and #mails > 0 then
        self:addMails(mails)
        self:updateMaxMailId()
    end
end

-- 获取邮件列表
function MailData:getMailList()
    return self._mailArray
end

-- 是否是有效的邮件，本地存在或者id=0的为无效
function MailData:isValidMail(mailid)
    if mailid == 0 then
        return false
    end
    for _, value in ipairs(self._mailArray) do
        if mailid == value.mail_id then
            return false
        end
    end
    return true
end

-- 新增邮件数据
function MailData:addMails(mails)
    for i = 1, #mails do
        if self:isValidMail(mails[i].mail_id) == true then
            self._mailArray[#self._mailArray + 1] = mails[i]
        end
    end

    if #mails > 0 then
        self:updateMaxMailId()
    end
end

-- 删除邮件
function MailData:delMails(mails)
    for index = 1, #mails do
        for key, value in ipairs(self._mailArray) do
            if mails[index] == value.mail_id then
                table.remove(self._mailArray, key)
            end
        end

        for key, value in ipairs(self._detailInfoArray) do
            if mails[index] == value.mail_id then
                table.remove(self._detailInfoArray, key)
            end
        end

        if self._curDetailInfo and self._curDetailInfo.mail_id == mails[index] then
            self._curDetailInfo = nil
        end
    end

    self:updateMaxMailId()
end

-- 设置邮件状态
function MailData:setMailState(unread, count)
    self._newMailCount = count or 0
end

-- 获取最大邮件id
function MailData:getMaxMailId()
    return self._maxMailID or 0
end

-- 获取新到达邮件数
function MailData:getNewMailCount()
    return self._newMailCount
end

-- 更新最大邮件
function MailData:updateMaxMailId()
    for _, value in ipairs(self._mailArray) do
        if value.mail_id > self._maxMailID then
            self._maxMailID = value.mail_id
        end
    end
    if #self._mailArray == 0 then
        self._maxMailID = 0
    end

    self:sortMailList()

    self:dispatchMailData()
end

function MailData:getMailCount()
    return #self._mailArray or 0
end

function MailData:refreshReadState(mailids)
    for i = 1, #mailids do
        for _, value in ipairs(self._mailArray) do
            if value.mail_id == mailids[i] then
                value.isRead = true
            end
        end
    end
end

-- 设置邮件详情
function MailData:setMailDetail(detail)
    self._detailInfoArray[detail.mail_id] = detail
    self._curDetailInfo = detail
    self:refreshReadState({detail.mail_id})
    --self:dispatchMailData()
    self:dispatchEvent({name = self.EVENT_MAIL_DETAIL_INFO})
end

-- 获取邮件详情
function MailData:getMailDetail()
    return self._curDetailInfo
end

function MailData:resetDetail()
    self._curDetailInfo = nil
end

-- 根据邮件id获取邮件详情
function MailData:getMailDetailById(mailid)
    return self._detailInfoArray[mailid]
end

function MailData:dispatchMailData()
    self:dispatchEvent({name = self.EVENT_MAIL_DATA_CHANGED})
end

-- 一键已读
function MailData:readAllMail()
    for _, value in ipairs(self._mailArray) do
        value.isRead = true
    end

    self:dispatchMailData()
end

-- 带有奖励的邮件
function MailData:getAwardMails()
    local mailids = {}
    for _, value in ipairs(self._mailArray) do
        if value.isAnnex == true then
            mailids[#mailids + 1] = value.mail_id
        end
    end

    return mailids
end

-- 未读的邮件
function MailData:getUnReadMails()
    local mailids = {}
    for _, value in ipairs(self._mailArray) do
        if value.isRead == false then
            mailids[#mailids + 1] = value.mail_id
        end
    end

    return mailids
end

function MailData:getMailIds()
    local mailids = {}
    for _, value in ipairs(self._mailArray) do
        mailids[#mailids + 1] = value.mail_id
    end

    return mailids
end

-- 领取奖励
function MailData:awardMails(mailids)
    for i = 1, #mailids do
        for _, value in ipairs(self._mailArray) do
            if value.mail_id == mailids[i] then
                value.isAnnex = false
            end
        end
    end

    --self:dispatchMailData()
    self:dispatchEvent({name = self.EVENT_MAIL_DETAIL_INFO})
end

function MailData:getMailInfoByMailId(mailId)
    for _, value in ipairs(self._mailArray) do
        if value.mail_id == mailId then
            return value
        end
    end
end

function MailData:getMailCountStr()
    local readCnt = 0
    local allMail = #self._mailArray
    for _, value in ipairs(self._mailArray) do
        if value.isRead == false then
            readCnt = readCnt + 1
        end
    end

    return "（" .. readCnt .. "/" .. allMail .. "）"
end

-- 邮件列表排序
function MailData:sortMailList()
    table.sort(
        self._mailArray,
        function(a, b)
            -- 是否已读
            local readA = a.isRead and 1 or 0
            local readB = b.isRead and 1 or 0
            -- 是否含附件
            local annexA = a.isAnnex and 1 or 0
            local annexB = b.isAnnex and 1 or 0

            if readA == readB then
                if readA == 1 then
                    if annexA == annexB then
                        return a.mail_id > b.mail_id
                    end
                    return annexB < annexA
                else
                    return a.mail_id > b.mail_id
                end
            else
                return readA < readB
            end
        end
    )
end

function MailData:testMail()
    if not MAIL_DEBUG then
        return
    end
    local MailProtocol = require("app.Protocols.MailProtocol")
    local maxCnt = #self._mailArray
    if maxCnt > 0 then
        self:updateMaxMailId()
        return
    end
    local maillist = {}
    for i = maxCnt + 1, maxCnt + 20 do
        local mail_info = MailProtocol.MailInfo:new()
        mail_info.mail_id = i
        mail_info.expire_time = 1593067726 - i * 60 * 60 * 24
        mail_info.title = "测试邮件title." .. i
        mail_info.intro = "我是测试测试测试测试测试邮件intro." .. i
        local showAward = false
        if i % 2 == 0 then
            showAward = true
        end
        mail_info.isRead = showAward
        mail_info.isAnnex = showAward
        maillist[#maillist + 1] = mail_info
    end

    self:setMailList(maillist)
end

function MailData:testMailDetail(index)
    if not MAIL_DEBUG then
        return
    end
    local MailProtocol = require("app.Protocols.MailProtocol")
    local mail_info = MailProtocol.RespGetMailDetail:new()
    mail_info.askid = os.time()
    mail_info.mail_id = self._mailArray[index + 1].mail_id
    mail_info.sender = "大王叫我来巡山" .. mail_info.mail_id
    mail_info.send_time = "2020-06-05 16:57:04"
    mail_info.mail_content = "测试邮件内容"
    mail_info.award_count = 4
    for i = 1, mail_info.award_count do
        local award_info = MailProtocol.AwardInfo:new()
        award_info.url = "https://palmstatic.hzxuanming.com/materials/images/20200915/18102152897.png"
        award_info.desc = "砖石100" .. i
        mail_info.award_list[i] = award_info
    end

    if self._detailInfoArray.mail_id ~= mail_info.mail_id then
        self:setMailDetail(mail_info)
    end
end

function MailData:testReadAll()
    if not MAIL_DEBUG then
        return
    end
    self:readAllMail()
end

function MailData:testAward(mailid, isRewardAll)
    if not MAIL_DEBUG then
        return
    end

    local mailids = {}

    if isRewardAll then
        for _, value in ipairs(self._mailArray) do
            if value.isAnnex == true then
                mailids[#mailids + 1] = value.mail_id
            end
        end
    else
        mailids = {mailid}
    end

    self:awardMails(mailids)
end

return MailData
   Q#  