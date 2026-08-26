local PromoteView = class("PromoteView", XH.ViewBase)
local PromoteDefine = require("lobby.Modules.Promote.Define")
local PromoteConfig = require("lobby.Modules.Promote.Config")

function PromoteView:ctor(isAdmin, isOwner, teaNumber, owner_numid, worldPos)
    PromoteView.super.ctor(self)
    self._isAdmin = isAdmin
    self._isOwner = isOwner
    self._teaNumber = teaNumber
    self._ownerNumid = owner_numid
    self._worldPos = worldPos
    XH.lobby:getModule("Promote"):reqSpreadTask(self._teaNumber, self._ownerNumid, self._isOwner, self._isAdmin)
    XH.lobby:getModule("Promote"):reqSpreadAwardList(self._teaNumber, PromoteDefine.DRAW_TYPE.PERIOD)
end

function PromoteView:getProxyEvents()
    return {{module = XH.lobby:getModule("Promote"), eventKeyName = "EVENT_FULSH_PROMOTE_TASK", callBack = "initUI"}}
end

function PromoteView:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/Promote/PromoteTeaLayer.csb"
end

function PromoteView:getBindingInfo()
    return {
        ["_KW_TEXT_TIME"] = {varName = "KW_TEXT_TIME"},
        ["_KW_BTN_INTRO"] = {varName = "KW_BTN_INTRO", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickIntro"},
        ["_KW_BTN_RULE"] = {varName = "KW_BTN_RULE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickRule"},
        ["_KW_BTN_CLOSE"] = {varName = "KW_BTN_CLOSE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickClose"},
        ["_KW_PANEL_USER"] = {varName = "KW_PANEL_USER"},
        ["_KW_PANEL_OWNER"] = {varName = "KW_PANEL_OWNER"},
        -- rule
        ["_KW_RULE_INTRO"] = {varName = "KW_RULE_INTRO"},
        ["_KW_RULE_WFXZ"] = {varName = "KW_RULE_WFXZ"},
        ["_KW_BTN_CLOSE_RULE"] = {varName = "KW_BTN_CLOSE_RULE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickCloseRule"},
        ["_KW_TEXT_TIP"] = {varName = "KW_TEXT_TIP"},
        ["_KW_PANEL_RULE"] = {varName = "KW_PANEL_RULE"},
        -- user
        ["_KW_BTN_USER_DO_1"] = {varName = "KW_BTN_USER_DO_1", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickUserDo1"},
        ["_KW_BTN_USER_DO_2"] = {varName = "KW_BTN_USER_DO_2", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickUserDo2"},
        ["_KW_BTN_USER_REWARD_1"] = {varName = "KW_BTN_USER_REWARD_1", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickUserRewardFirst"},
        ["_KW_BTN_USER_REWARD_2"] = {varName = "KW_BTN_USER_REWARD_2", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickUserRewardSecond"},
        ["_KW_BTN_USER_REWARD_3"] = {varName = "KW_BTN_USER_REWARD_3", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickUserRewardPeriod"},
        ["_LABLE_USER_CUR_2"] = {varName = "LABLE_USER_CUR_2"},
        ["_LABLE_USER_CUR_1"] = {varName = "LABLE_USER_CUR_1"},
        ["_LABLE_USER_MAX_2"] = {varName = "LABLE_USER_MAX_2"},
        ["_LABLE_USER_MAX_1"] = {varName = "LABLE_USER_MAX_1"},
        ["_LABLE_MM"] = {varName = "LABLE_MM"},
        ["_LABLE_USER_TITLE_2"] = {varName = "LABLE_USER_TITLE_2"},
        ["_LABLE_USER_TITLE_1"] = {varName = "LABLE_USER_TITLE_1"},
        ["_IMG_USER_REWARD_3"] = {varName = "IMG_USER_REWARD_3"},
        ["_IMG_USER_REWARD_2"] = {varName = "IMG_USER_REWARD_2"},
        ["_IMG_USER_REWARD_1"] = {varName = "IMG_USER_REWARD_1"},
        ["_IMG_USER_END_2"] = {varName = "IMG_USER_END_2"},
        ["_IMG_USER_END_1"] = {varName = "IMG_USER_END_1"},
        ["_AWARD_DAY_1"] = {varName = "AWARD_DAY_1"},
        ["_KW_AWARD_SELECT_1"] = {varName = "KW_AWARD_SELECT_1"},
        ["_KW_AWARD_TEXT_1_1"] = {varName = "KW_AWARD_TEXT_1_1"},
        ["_KW_AWARD_TEXT_1_2"] = {varName = "KW_AWARD_TEXT_1_2"},
        ["_AWARD_DAY_2"] = {varName = "AWARD_DAY_2"},
        ["_KW_AWARD_SELECT_2"] = {varName = "KW_AWARD_SELECT_2"},
        ["_KW_AWARD_TEXT_2_1"] = {varName = "KW_AWARD_TEXT_2_1"},
        ["_KW_AWARD_TEXT_2_2"] = {varName = "KW_AWARD_TEXT_2_2"},
        ["_AWARD_DAY_3"] = {varName = "AWARD_DAY_3"},
        ["_KW_AWARD_SELECT_3"] = {varName = "KW_AWARD_SELECT_3"},
        ["_KW_AWARD_TEXT_3_1"] = {varName = "KW_AWARD_TEXT_3_1"},
        ["_KW_AWARD_TEXT_3_2"] = {varName = "KW_AWARD_TEXT_3_2"},
        -- owner
        ["_KW_BTN_OWNER_LEFT_INVITE"] = {varName = "KW_BTN_OWNER_LEFT_INVITE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickOwnerInviteLeft"},
        ["_KW_BTN_OWNER_LEFT_REWARD"] = {varName = "KW_BTN_OWNER_LEFT_REWARD", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickOwnerRewardLeft"},
        ["_KW_BTN_OWNER_LEFT_CREATE"] = {varName = "KW_BTN_OWNER_LEFT_CREATE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickOwnerCreate"},
        ["_KW_BTN_OWNER_RIGHT_SHARE"] = {varName = "KW_BTN_OWNER_RIGHT_SHARE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickOwnerShare"},
        ["_KW_BTN_OWNER_RIGHT_INVITE"] = {varName = "KW_BTN_OWNER_RIGHT_INVITE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickOwnerInviteRight"},
        ["_KW_BTN_OWNER_RIGHT_REWARD"] = {varName = "KW_BTN_OWNER_RIGHT_REWARD", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickOwnerRewardRight"},
        ["_KW_BTN_OWNER_RIGHT_CREATE"] = {varName = "KW_BTN_OWNER_RIGHT_CREATE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickOwnerCreate"},
        ["_KW_BTN_REWARD_LOG"] = {varName = "KW_BTN_REWARD_LOG", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickRewardLog"},
        ["_IMG_OWNER_RIGHT_REWARD"] = {varName = "IMG_OWNER_RIGHT_REWARD"},
        ["_IMG_OWNER_RIGHT_END"] = {varName = "IMG_OWNER_RIGHT_END"},
        ["_LABLE_OWNER_LEFT_NUM"] = {varName = "LABLE_OWNER_LEFT_NUM"},
        ["_LABLE_OWNER_LEFT_PRO"] = {varName = "LABLE_OWNER_LEFT_PRO"},
        ["_LABLE_OWNER_LEFT_TITLE"] = {varName = "LABLE_OWNER_LEFT_TITLE"},
        ["_IMG_OWNER_LEFT_END"] = {varName = "IMG_OWNER_LEFT_END"},
        ["_IMG_OWNER_LEFT_REWARD"] = {varName = "IMG_OWNER_LEFT_REWARD"},
        ["_LABLE_OWNER_RIGHT_REWARD"] = {varName = "LABLE_OWNER_RIGHT_REWARD"},
        ["_LABEL_OWNER_RIGHT_PROGRESS"] = {varName = "LABEL_OWNER_RIGHT_PROGRESS"},
        ["_LABEL_OWNER_RIGHT_TITLE"] = {varName = "LABEL_OWNER_RIGHT_TITLE"},
        ["_KW_BTN_OWNER_LEFT_REWARD_RED"] = {varName = "KW_BTN_OWNER_LEFT_REWARD_RED"},
        ["_KW_BTN_OWNER_RIGHT_REWARD_RED"] = {varName = "KW_BTN_OWNER_RIGHT_REWARD_RED"},
        ["_KW_BTN_USER_REWARD_1_RED"] = {varName = "KW_BTN_USER_REWARD_1_RED"},
        ["_KW_BTN_USER_REWARD_2_RED"] = {varName = "KW_BTN_USER_REWARD_2_RED"},
        ["_KW_BTN_USER_REWARD_3_RED"] = {varName = "KW_BTN_USER_REWARD_3_RED"},
        ["_KW_TEXT_TIP_MANAGER"] = {varName = "KW_TEXT_TIP_MANAGER"},
        ["_KW_ROOT_LAYER"] = {varName = "_rootLayer"},
        ["_KW_BTN_USER_LEFT_INVITE"] = {varName = "KW_BTN_USER_LEFT_INVITE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickManagerInvite"},
        ["_KW_BTN_USER_RIGHT_INVITE"] = {varName = "KW_BTN_USER_RIGHT_INVITE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickManagerInvite"}
    }
end

function PromoteView:initUI()
    local isManager = false
    if self._isAdmin or self._isOwner then
        isManager = true
    end
    local isFree = false
    if PromoteConfig[XH.areaData:getLobbyID()] ~= nil then
        isFree = PromoteConfig[XH.areaData:getLobbyID()].isFree
    end
    self.KW_BTN_REWARD_LOG:setVisible(self._isOwner)
    self.KW_TEXT_TIP_MANAGER:setVisible(isManager and isFree)
    self.KW_PANEL_USER:setVisible(not isManager)
    self.KW_PANEL_OWNER:setVisible(isManager)
    self.KW_BTN_USER_LEFT_INVITE:setVisible(self._isAdmin)
    self.KW_BTN_USER_RIGHT_INVITE:setVisible(self._isAdmin)

    local baseInfo = XH.lobby:getModule("Promote"):getBaseInfo()
    local taskInfo = XH.lobby:getModule("Promote"):getTaskInfo()
    if baseInfo == nil or taskInfo == nil then
        return
    end
    self.KW_TEXT_TIME:setString("活动时间  " .. os.date("%m", baseInfo.start) .. "." .. os.date("%d", baseInfo.start) .. "-" .. os.date("%m", baseInfo["end"]) .. "." .. os.date("%d", baseInfo["end"]))

    local type = taskInfo.task_type
    local type_left = taskInfo.first_task_type

    if type == PromoteDefine.TASK_TYPE.USER then
        self:updateUserView(baseInfo, taskInfo)
    else
        if type_left == PromoteDefine.TASK_TYPE.A then
            self:updateLeftA(baseInfo, taskInfo)
        elseif type_left == PromoteDefine.TASK_TYPE.C then
            self:updateLeftC(baseInfo, taskInfo)
        end
        if type == PromoteDefine.TASK_TYPE.C then
            self:updateRightC(baseInfo, taskInfo)
        end
    end
end

function PromoteView:initTextByConfig(config, node)
    if node ~= nil and PromoteConfig[XH.areaData:getLobbyID()] ~= nil and PromoteConfig[XH.areaData:getLobbyID()][config] then
        node:setString(PromoteConfig[XH.areaData:getLobbyID()][config])
    end
end

function PromoteView:updateLeftA(baseInfo, taskInfo)
    local hasMode = XH.lobby:getModule("Promote"):hasActPlayModes()
    local isOpenAct = XH.lobby:getModule("Promote"):isOpenAct()
    self:initTextByConfig("teaView_ownerLeftTitle_A", self.LABLE_OWNER_LEFT_TITLE)
    self:initTextByConfig("teaView_ownerLeftNum_A", self.LABLE_OWNER_LEFT_NUM)
    self.LABLE_OWNER_LEFT_PRO:setString(taskInfo.task.total_round .. "/" .. baseInfo.owner_first_task_times)
    if self._isOwner then
        local isComplete = taskInfo.task.total_round >= baseInfo.owner_first_task_times
        local isReward = taskInfo.award.first == 1
        if isOpenAct then
            self.LABLE_OWNER_LEFT_PRO:setVisible(hasMode and (not isReward) and (not isComplete))
            self.KW_BTN_OWNER_LEFT_INVITE:setVisible(hasMode and (not isReward) and (not isComplete))
            self.IMG_OWNER_LEFT_REWARD:setVisible(isReward)
            self.IMG_OWNER_LEFT_END:setVisible(false)
            self.KW_BTN_OWNER_LEFT_CREATE:setVisible((not isReward) and (not hasMode))
            self.KW_BTN_OWNER_LEFT_REWARD:setVisible((not isReward) and isComplete and hasMode)
            self.KW_BTN_OWNER_LEFT_REWARD_RED:setVisible(not isReward and isComplete)
            self.KW_BTN_OWNER_LEFT_REWARD:setEnabled(isComplete and (not isReward))
        else
            self.LABLE_OWNER_LEFT_PRO:setVisible(false)
            self.KW_BTN_OWNER_LEFT_INVITE:setVisible(false)
            self.IMG_OWNER_LEFT_REWARD:setVisible(isReward)
            self.IMG_OWNER_LEFT_END:setVisible((not isReward) and (not isComplete))
            self.KW_BTN_OWNER_LEFT_CREATE:setVisible(false)
            self.KW_BTN_OWNER_LEFT_REWARD:setVisible((not isReward) and isComplete)
            self.KW_BTN_OWNER_LEFT_REWARD_RED:setVisible((not isReward) and isComplete)
            self.KW_BTN_OWNER_LEFT_REWARD:setEnabled(true)
        end
    else
        self.LABLE_OWNER_LEFT_PRO:setVisible(false)
        self.KW_BTN_OWNER_LEFT_INVITE:setVisible(false)
        self.IMG_OWNER_LEFT_REWARD:setVisible(false)
        self.IMG_OWNER_LEFT_END:setVisible(not isOpenAct)
        self.KW_BTN_OWNER_LEFT_CREATE:setVisible(false) -- isOpenAct)
        self.KW_BTN_OWNER_LEFT_REWARD:setVisible(false)
        self.KW_BTN_OWNER_LEFT_REWARD_RED:setVisible(false)
    end
end

function PromoteView:updateLeftC(baseInfo, taskInfo)
    local hasMode = XH.lobby:getModule("Promote"):hasActPlayModes()
    local isOpenAct = XH.lobby:getModule("Promote"):isOpenAct()
    self:initTextByConfig("teaView_ownerLeftTitle_C", self.LABLE_OWNER_LEFT_TITLE)
    self:initTextByConfig("teaView_ownerLeftNum_C", self.LABLE_OWNER_LEFT_NUM)
    self.LABLE_OWNER_LEFT_PRO:setString("0/10人")
    if self._isOwner then
        local isComplete = false
        local isReward = taskInfo.award.first == 1
        if isOpenAct then
            self.LABLE_OWNER_LEFT_PRO:setVisible((not isReward) and (not isComplete) and hasMode)
            self.KW_BTN_OWNER_LEFT_INVITE:setVisible((not isReward) and (not isComplete) and hasMode)
            self.IMG_OWNER_LEFT_REWARD:setVisible(isReward)
            self.IMG_OWNER_LEFT_END:setVisible(false)
            self.KW_BTN_OWNER_LEFT_CREATE:setVisible((not isReward) and (not hasMode))
            self.KW_BTN_OWNER_LEFT_REWARD:setVisible((not isReward) and isComplete and hasMode)
            self.KW_BTN_OWNER_LEFT_REWARD_RED:setVisible((not isReward) and isComplete)
            self.KW_BTN_OWNER_LEFT_REWARD:setEnabled((not isReward) and isComplete)
        else
            self.LABLE_OWNER_LEFT_PRO:setVisible(false)
            self.KW_BTN_OWNER_LEFT_INVITE:setVisible(false)
            self.IMG_OWNER_LEFT_REWARD:setVisible(isReward)
            self.IMG_OWNER_LEFT_END:setVisible((not isReward) and (not isComplete))
            self.KW_BTN_OWNER_LEFT_CREATE:setVisible(false)
            self.KW_BTN_OWNER_LEFT_REWARD:setVisible((not isReward) and isComplete)
            self.KW_BTN_OWNER_LEFT_REWARD_RED:setVisible((not isReward) and isComplete)
            self.KW_BTN_OWNER_LEFT_REWARD:setEnabled(true)
        end
    else
        self.LABLE_OWNER_LEFT_PRO:setVisible(false)
        self.KW_BTN_OWNER_LEFT_INVITE:setVisible(false)
        self.IMG_OWNER_LEFT_REWARD:setVisible(false)
        self.IMG_OWNER_LEFT_END:setVisible(not isOpenAct)
        self.KW_BTN_OWNER_LEFT_CREATE:setVisible(false) -- isOpenAct)
        self.KW_BTN_OWNER_LEFT_REWARD:setVisible(false)
        self.KW_BTN_OWNER_LEFT_REWARD_RED:setVisible(false)
    end
end

function PromoteView:updateRightC(baseInfo, taskInfo)
    local hasMode = XH.lobby:getModule("Promote"):hasActPlayModes()
    local isOpenAct = XH.lobby:getModule("Promote"):isOpenAct()
    local gameName = "广式麻将" -- 以后可配
    local ruleName = "红中癞子" -- 以后可配

    if self.LABEL_OWNER_RIGHT_TITLE:getChildrenCount() == 0 then
        local textStr = "<font face='cocosStudio/Common/Font/fangzhengcuyuan.TTF' size='32' color='#EAB8B5'>" .. gameName .. "2/3/4人带" .. ruleName .. "玩法，亲友圈每日对局达 <font color='#FFDE00'>" .. taskInfo.task_config.task_target .. "场</font> ，即可获得<font color='#FFDE00'>" .. taskInfo.task_config.task_award .. "房卡</font>奖励</font>"
        local richText = ccui.RichText:createWithXML(XH.StringTool.replaceMatchStr(textStr), {})
        richText:setAnchorPoint(cc.p(0.5, 0.5))
        richText:ignoreContentAdaptWithSize(false)
        richText:setContentSize(self.LABEL_OWNER_RIGHT_TITLE:getContentSize())
        richText:setHorizontalAlignment(cc.VERTICAL_TEXT_ALIGNMENT_CENTER)
        richText:setPosition(cc.p(self.LABEL_OWNER_RIGHT_TITLE:getContentSize().width / 2, self.LABEL_OWNER_RIGHT_TITLE:getContentSize().height / 2))
        self.LABEL_OWNER_RIGHT_TITLE:addChild(richText)
    end

    self.LABEL_OWNER_RIGHT_TITLE:setString("")
    self.LABEL_OWNER_RIGHT_PROGRESS:setString("当前进度：" .. taskInfo.task.round .. "/" .. taskInfo.task_config.task_target .. " 场")
    self.LABLE_OWNER_RIGHT_REWARD:setString("房卡x" .. taskInfo.award.left_award)

    if self._isOwner then
        local canReward = taskInfo.award.left_award > 0
        if isOpenAct then
            self.IMG_OWNER_RIGHT_REWARD:setVisible(false)
            self.IMG_OWNER_RIGHT_END:setVisible(false)
            self.KW_BTN_OWNER_RIGHT_CREATE:setVisible(not hasMode)
            self.KW_BTN_OWNER_RIGHT_REWARD:setVisible(hasMode)
            self.KW_BTN_OWNER_RIGHT_REWARD_RED:setVisible(canReward)
            self.KW_BTN_OWNER_RIGHT_REWARD:setEnabled(canReward)
        else
            self.IMG_OWNER_RIGHT_REWARD:setVisible(false)
            self.IMG_OWNER_RIGHT_END:setVisible(not canReward)
            self.KW_BTN_OWNER_RIGHT_CREATE:setVisible(false)
            self.KW_BTN_OWNER_RIGHT_REWARD:setVisible(canReward)
            self.KW_BTN_OWNER_RIGHT_REWARD_RED:setVisible(canReward)
            self.KW_BTN_OWNER_RIGHT_REWARD:setEnabled(true)
        end
    else
        self.IMG_OWNER_RIGHT_REWARD:setVisible(false)
        self.IMG_OWNER_RIGHT_END:setVisible(not isOpenAct)
        self.KW_BTN_OWNER_RIGHT_CREATE:setVisible(false) -- isOpenAct)
        self.KW_BTN_OWNER_RIGHT_REWARD:setVisible(false)
        self.KW_BTN_OWNER_RIGHT_REWARD_RED:setVisible(false)
    end
end

function PromoteView:updateUserView(baseInfo, taskInfo)
    local isOpenAct = XH.lobby:getModule("Promote"):isOpenAct()
    local userConfig = baseInfo.player_task_config
    local userRound = taskInfo.task.round or 0
    local isUserFirstComplete = userRound >= userConfig.first_target
    local isUserSecondComplete = userRound >= userConfig.second_target
    local isUserFirstReward = taskInfo.award.first == 1
    local isUserSecondReward = taskInfo.award.second == 1
    local isUserPeriodComplete = taskInfo.task.complete_task >= userConfig.period_target
    local isUserPeriodAward = taskInfo.award.period == 1
    self.LABLE_USER_CUR_1:setString(math.min(userRound, userConfig.first_target))
    self.LABLE_USER_CUR_2:setString(math.min(userRound, userConfig.second_target))
    self.LABLE_USER_MAX_1:setString('/' .. userConfig.first_target)
    self.LABLE_USER_MAX_2:setString('/' .. userConfig.second_target)
    self.KW_BTN_USER_DO_1:setVisible(isOpenAct and (not isUserFirstComplete))
    self.KW_BTN_USER_DO_2:setVisible(isOpenAct and (not isUserSecondComplete))
    self.KW_BTN_USER_REWARD_1:setVisible(isOpenAct and isUserFirstComplete and (not isUserFirstReward))
    self.KW_BTN_USER_REWARD_1_RED:setVisible(isOpenAct and (not isUserFirstReward) and isUserFirstComplete)
    self.KW_BTN_USER_REWARD_2:setVisible(isOpenAct and isUserSecondComplete and (not isUserSecondReward))
    self.KW_BTN_USER_REWARD_2_RED:setVisible(isOpenAct and (not isUserSecondReward) and isUserSecondComplete)
    self.IMG_USER_REWARD_1:setVisible(isOpenAct and isUserFirstReward)
    self.IMG_USER_REWARD_2:setVisible(isOpenAct and isUserSecondReward)
    self.IMG_USER_END_1:setVisible(not isOpenAct)
    self.IMG_USER_END_2:setVisible(not isOpenAct)
    self.AWARD_DAY_1:setVisible(not isUserPeriodComplete)
    self.AWARD_DAY_2:setVisible(not isUserPeriodComplete)
    self.AWARD_DAY_3:setVisible(not isUserPeriodComplete)
    self.KW_BTN_USER_REWARD_3:setVisible(isUserPeriodComplete and (not isUserPeriodAward))
    self.KW_BTN_USER_REWARD_3_RED:setVisible((not isUserPeriodAward) and isUserPeriodComplete)
    self.IMG_USER_REWARD_3:setVisible(isUserPeriodAward)
    self.KW_AWARD_SELECT_1:setVisible(taskInfo.task.complete_task >= 1)
    self.KW_AWARD_TEXT_1_1:setVisible(not (taskInfo.task.complete_task >= 1))
    self.KW_AWARD_TEXT_1_2:setVisible(taskInfo.task.complete_task >= 1)
    self.KW_AWARD_SELECT_2:setVisible(taskInfo.task.complete_task >= 2)
    self.KW_AWARD_TEXT_2_1:setVisible(not (taskInfo.task.complete_task >= 2))
    self.KW_AWARD_TEXT_2_2:setVisible(taskInfo.task.complete_task >= 2)
    self.KW_AWARD_SELECT_3:setVisible(taskInfo.task.complete_task >= 3)
    self.KW_AWARD_TEXT_3_1:setVisible(not (taskInfo.task.complete_task >= 3))
    self.KW_AWARD_TEXT_3_2:setVisible(taskInfo.task.complete_task >= 3)
end

function PromoteView:onClickCloseRule()
    self.KW_PANEL_RULE:setVisible(false)
end

function PromoteView:onClickIntro()
    local time = "" -- "活动时间：timeReplace\n"
    local intro = time .. "1、完成相应的活动要求即可获得奖励礼包，奖励礼包内有钻石，任务完成后可领取\n2、领取界面点击翻倍领取并分享，即可成功翻倍领取奖励\n3、活动统计对局数需为广式麻将红中癞子所有小局都打完的对局，中途解散对局不计入在内\n4、所有奖励在任务完成1小时后可以领取，需在活动时间内领取，过期视为自动放弃"
    if self._isAdmin or self._isOwner then
        intro = time .. "1、活动统计对局数需为所有小局都打完的对局，中途解散对局不计入在内\n2、活动统计对局数为名下所有圈广式麻将红中癞子完整场次数\n3、奖励完成可领，具体数额见领取记录\n4、活动上限为3000局，超过3000局不额外奖励\n5、严禁恶意刷奖，一经发现平台有权取消恶意刷奖产生的奖励"
    end
    self.KW_TEXT_TIP:setText(intro) -- 领队 和 玩家规则不同
    self:showRuleType(false)
end

function PromoteView:onClickRule()
    self.KW_TEXT_TIP:setText("1、红中癞子为房间选项，勾选后红中为癞子。共4张\n2、癞子可替换任意牌进行牌型组合\n3、红中癞子牌不可点炮\n4、癞子牌可打出\n5、癞子牌本身不可吃碰杠，也不可和其他牌组合进行吃碰杠\n6、胡牌时，如果有癞子，按最大的番型组合进行计算胡分。")
    self:showRuleType(true)
end

function PromoteView:showRuleType(isRule)
    self.KW_RULE_INTRO:setVisible(not isRule)
    self.KW_RULE_WFXZ:setVisible(isRule)
    self.KW_PANEL_RULE:setVisible(true)
end

function PromoteView:onClickManagerInvite()
    if not XH.lobby:getModule("Promote"):isOpenAct() then
        XH.TipTool.showToast("活动已结束")
        return
    end
    local hasMode = XH.lobby:getModule("Promote"):hasActPlayModes()
    if hasMode then
        XH.TipTool.showToast("已有相关规则的桌子")
        return
    end
    self:noticeCreateTable()
    self:close()
end

function PromoteView:quickCreateView()
    if not XH.lobby:getModule("Promote"):isOpenAct() then
        XH.TipTool.showToast("活动已结束")
        return
    end
    local hasMode = XH.lobby:getModule("Promote"):hasActPlayModes()
    if hasMode then
        XH.TipTool.showToast("已有相关规则的桌子")
        return
    end
    XH.lobby:getModule("Promote"):quickCreatePlayMode(self._teaNumber)
end

function PromoteView:onClickRewardLog()
    XH.viewManager:openView("PromoteLogView", nil, self._teaNumber)
end

function PromoteView:onClickClose()
    if self._isAdmin or self._isOwner then
        self:close()
        return
    end
    local todatTimes = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_PROMOTE_USER_CLOSE, 0)
    if todatTimes ~= 0 then
        self:close()
        return
    end
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PROMOTE_USER_CLOSE, 1)
    -- 玩家播放缩小动画
    if self._worldPos ~= nil then
        local worldSize = cc.Director:getInstance():getWinSize()
        local perX = self._worldPos.x / worldSize.width
        local perY = self._worldPos.y / worldSize.height
        self._rootLayer:setAnchorPoint(cc.p(perX, perY))
        self._rootLayer:setPosition(cc.p(self._rootLayer:getContentSize().width * perX, self._rootLayer:getContentSize().height * perY))
        self._rootLayer:runAction(cc.Sequence:create(cc.ScaleTo:create(1, 0), cc.CallFunc:create(function()
            self:close()
        end)))
    else
        self:close()
    end
end

function PromoteView:onClickUserDo()
    if not XH.lobby:getModule("Promote"):isOpenAct() then
        XH.TipTool.showToast("活动已结束")
        return
    end

    if XH.lobby:getModule("Promote"):joinModeRoom(self._teaNumber) then
        self:close()
        return
    end

    self:noticeCreateTable()
    self:close()
end

function PromoteView:onClickUserDo1()
    self:onClickUserDo()
end

function PromoteView:onClickUserDo2()
    self:onClickUserDo()
end

function PromoteView:onClickUserRewardFirst()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.PROMOTE_ACT_CLICK3, {userid = XH.playerData:getNumberID()})
    XH.lobby:getModule("Promote"):reqSpreadDraw(self._teaNumber, PromoteDefine.DRAW_TYPE.FIRST)
end

function PromoteView:onClickUserRewardSecond()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.PROMOTE_ACT_CLICK4, {userid = XH.playerData:getNumberID()})
    XH.lobby:getModule("Promote"):reqSpreadDraw(self._teaNumber, PromoteDefine.DRAW_TYPE.SECOND)
end

function PromoteView:onClickUserRewardPeriod()
    XH.lobby:getModule("Promote"):reqSpreadDraw(self._teaNumber, PromoteDefine.DRAW_TYPE.PERIOD)
end

function PromoteView:onClickOwnerRewardLeft()
    local type = 1
    local taskInfo = XH.lobby:getModule("Promote"):getTaskInfo()
    local baseInfo = XH.lobby:getModule("Promote"):getBaseInfo()
    if taskInfo == nil or baseInfo == nil then
        return
    end
    local canReward = false
    local nowDate = XH.lobby:getModule("Promote"):getTodayZeroTime()
    local nowDataList = {}
    if taskInfo.first_task_type == PromoteDefine.TASK_TYPE.A and taskInfo.task.total_round >= baseInfo.owner_first_task_times then
        canReward = true
        nowDataList[#nowDataList + 1] = nowDate
    end

    if canReward then
        XH.lobby:getModule("Promote"):reqSpreadAward(self._teaNumber, nowDataList, PromoteDefine.AWARD_TYPE.FIRST)
    else
        XH.TipTool.showToast("任务未完成！")
    end
end

function PromoteView:onClickOwnerRewardRight()
    local type = 2
    local canReward = false
    local taskInfo = XH.lobby:getModule("Promote"):getTaskInfo()
    local rewardList = XH.lobby:getModule("Promote"):getRewardLog()
    if taskInfo == nil or rewardList == nil then
        return
    end
    local nowDate = XH.lobby:getModule("Promote"):getTodayZeroTime()
    local nowDataList = {}
    if taskInfo.task_type == PromoteDefine.TASK_TYPE.A and taskInfo.award.left_award > 0 then
        local nowDate = XH.lobby:getModule("Promote"):getTodayZeroTime()
        for _, reward in pairs(rewardList) do
            if reward.date < nowDate and reward.award > 0 and reward.deliver == 0 then
                nowDataList[#nowDataList + 1] = reward.date
            end
        end
        canReward = true
    elseif taskInfo.task_type == PromoteDefine.TASK_TYPE.B and taskInfo.task.round >= taskInfo.task_config.task_target then
        nowDataList[#nowDataList + 1] = nowDate
        canReward = true
    elseif taskInfo.task_type == PromoteDefine.TASK_TYPE.C and taskInfo.award.left_award > 0 then
        local nowDate = XH.lobby:getModule("Promote"):getTodayZeroTime()
        for _, reward in pairs(rewardList) do
            if reward.date <= nowDate and reward.award > 0 and reward.deliver == 0 then
                nowDataList[#nowDataList + 1] = reward.date
            end
        end
        canReward = true
    end
    XH.throwDataManager:throwData(XH.ThrowDataDefine.PROMOTE_ACT_CLICK5, {userid = XH.playerData:getNumberID()})
    if canReward then
        XH.lobby:getModule("Promote"):reqSpreadAward(self._teaNumber, nowDataList, PromoteDefine.AWARD_TYPE.PERIOD)
    else
        if type == 2 and taskInfo.task_type == PromoteDefine.TASK_TYPE.A then
            XH.TipTool.showToast("无奖励可领取！")
        else
            XH.TipTool.showToast("任务未完成！")
        end
    end
end

function PromoteView:onClickOwnerInviteLeft()
    XH.viewManager:openView("PromoteInviteView", nil, self._teaNumber)
end

function PromoteView:onClickOwnerInviteRight()
    XH.viewManager:openView("PromoteInviteView", nil, self._teaNumber)
end

function PromoteView:onClickOwnerShare()
    XH.lobby:getModule("Promote"):shreToWx()
end

-- 提示去通知领队/副领队创建
function PromoteView:noticeCreateTable()
    XH.viewManager:openView("PromoteTipView", nil, PromoteDefine.TIP_TYPE.PLAYER_NOTICE, self._teaNumber, "当前圈子无癞子牌桌，可通知圈主创建牌桌")
end

function PromoteView:onClickOwnerCreate()
    if self._isAdmin then
        XH.throwDataManager:throwData(XH.ThrowDataDefine.PROMOTE_ACT_CLICK8, {userid = XH.playerData:getNumberID()})
    elseif self._isOwner then
        XH.throwDataManager:throwData(XH.ThrowDataDefine.PROMOTE_ACT_CLICK9, {userid = XH.playerData:getNumberID()})
    end
    self:quickCreateView()
end

return PromoteView
   �l  