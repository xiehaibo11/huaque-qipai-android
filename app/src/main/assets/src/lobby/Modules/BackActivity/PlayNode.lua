local PlayNode = class("PlayNode", XH.ViewBase)

local KW_BTN_AWARD = "_KW_BTN_AWARD"
local KW_BTN_AWARD_PIC_NAME = "act_btn_play"
local KW_BTN_AWARD_PIC_THREE_NAME = "act_btn_three"
local KW_PANEL_AWARD = "_KW_PANEL_AWARD_"
local KW_TEXT_NUM = "_KW_TEXT_NUM"
local KW_PIC_AWARD = "_KW_PIC_AWARD"
local KW_TEXT_PLAYLIMIT = "_KW_TEXT_PLAYLIMIT"
local KW_TEXT_PLAY_JU = "_KW_TEXT_PLAY_JU"
local KW_ITEM_ALLAWARD = "_KW_ITEM_ALLAWARD_"

local KW_DAY = { "一", "二", "三", "四", "五", "六", "七" }
local KW_MAX_LOGIN_DAY = 7

function PlayNode:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/BackActivity/BackActivityPlayLayer.csb"
end

function PlayNode:getBindingInfo()
    return {
        ["_KW_PANEL_LOGIN"] = { varName = "_panelLogin" },
        ["_KW_THREE_DAY_SHOW_PANEL"] = { varName = "_panelThree" },
        ["_KW_TEXT_THREE_TIME"] = { varName = "_textThreeTime" },
        ["_KW_TEXT_PLAY_TYPE"] = { varName = "_textPlayType" },
        ["_KW_BTN_ACTIVITY_RULE"] = { varName = "RULE", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventShowRule" },
        ["_KW_BTN_MORE"] = { varName = "MORE", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventShowMore" },
        ["_KW_LISTVIEW_PLAY"] = { varName = "_listViewPlay" },
        ["_KW_BTN_SHOW_MORE"] = { varName = "_btnShowMoerAward", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventShowMoreAllAward" },
        ["_KW_BTN_CLOSE_MORE"] = { varName = "_btnCloseMoerAward", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventCloseMoreAllAward" },
        ["_KW_LISTVIEW_MORE_INFO"] = { varName = "_listviewAllAward" },
        ["_KW_ALL_AWARD_SHOW_PANEL"] = { varName = "_panelAllAward" },
        ["_KW_TEXT_ALLAWARD_DAY"] = { varName = "_textAllAwardDay" },
        ["_KW_PANEL_ITEM_PLAY_"] = { varName = "_playItemPanel_", beginIndex = 1, endIndex = 3 },
    }
end

function PlayNode:getProxyEvents()
    return {
        { module = XH.lobby:getModule("BackActivity"), eventName = "BackActivityDownloadImage", callBack = "onDownloadImageCallBack" },
    }
end

function PlayNode:ctor(param, father, awardType)
    param = param or { }
    PlayNode.super.ctor(self, param)
    self._awardType = awardType
    self._father = father
    self._downloadImageUrlList = { }
    self._downloadImageNodeList = { }

    self:initNode()
end

------------------------------------------------- init ---------------------------------------------
function PlayNode:initNode(awardType)
    self._downloadImageUrlList = { }
    self._downloadImageNodeList = { }
    local awardInfo = XH.lobby:getModule("BackActivity"):getActAwardInfo()
    self:initAllPlayAward(awardInfo)
    self:initLoginAward(awardInfo)
    self:initPlayAward(awardInfo)
    self:initThreeAward(awardInfo)

    local threeTime = XH.lobby:getModule("BackActivity"):getThreeTime()
    self:initThreeTime(threeTime)

    self._textPlayType:setString(self._awardType == "Box" and XH.StringTool.replaceMatchStr("亲友圈/包厢今日福利") or "金币场今日福利")
    self:downloadImage()

    self:runBtnAni()
end

function PlayNode:initLoginAward(data)
    local actInfo = XH.lobby:getModule("BackActivity"):getActInfo()
    local allAward = self._awardType == "Box" and(data.playBoxAllAward) or(data.playGoldAllAward)
    local loginAward = self._awardType == "Box" and(data.boxAward.login) or(data.goldAward.login)
    local loginDay = XH.lobby:getModule("BackActivity"):getLoginDay()
    local loginCnt = self._awardType == "Box" and(actInfo.loginBoxCnt) or(actInfo.loginGoldCnt)

    local getAwardBtn = self._panelLogin:getChildByName(KW_BTN_AWARD)
    local pngName = KW_BTN_AWARD_PIC_NAME .. loginCnt[loginDay] .. ".png"
    getAwardBtn:loadTexture(pngName, ccui.TextureResType.plistType)
    getAwardBtn:ignoreContentAdaptWithSize(true)
    getAwardBtn.awardId = loginDay
    if loginCnt[loginDay] == 1 then
        getAwardBtn.awardType = self._awardType == "Box" and "loginBox" or "loginGold"
        getAwardBtn:addTouchEventListener(handler(self, self.onTouchEventGetAward))
    end

    local item = 1
    for propid, awardinfo in pairs(loginAward) do
        local awardPanel = self._panelLogin:getChildByName(KW_PANEL_AWARD .. item)
        if awardPanel then
            item = item + 1
            awardPanel:setVisible(true)
            local awardNum = awardPanel:getChildByName(KW_TEXT_NUM)
            if awardNum then
                awardNum:setString("x" .. awardinfo.cnt)
            end
            local awardPic = awardPanel:getChildByName(KW_PIC_AWARD)
            local url = allAward[loginDay][propid].url
            if awardPic and url then
                self:addDownloadImageList(url, awardPic)
            end
        end
    end
end

function PlayNode:initPlayAward(data)
    local actInfo = XH.lobby:getModule("BackActivity"):getActInfo()
    local allAward = self._awardType == "Box" and(data.playBoxAllAward) or(data.playGoldAllAward)
    local playAward = self._awardType == "Box" and(data.boxAward.play) or(data.goldAward.play)
    local playLimit = self._awardType == "Box" and(actInfo.playBoxLimit) or(actInfo.playGoldLimit)
    local playCnt = self._awardType == "Box" and(actInfo.boxCnt) or(actInfo.goldCnt)
    local playType = self._awardType == "Box" and(actInfo.playBoxAaward) or(actInfo.playGoldAaward)
    local loginDay = XH.lobby:getModule("BackActivity"):getLoginDay()

    for i = 1, 3 do
        local key = tostring(i)
        local playAwardType = playType[key]
        local clonePanel = self["_playItemPanel_" .. key]
        local getAwardBtn = clonePanel:getChildByName(KW_BTN_AWARD)
        getAwardBtn:loadTexture(KW_BTN_AWARD_PIC_NAME .. playAwardType .. ".png", ccui.TextureResType.plistType)
        getAwardBtn:ignoreContentAdaptWithSize(true)
        if playAwardType == 1 then
            getAwardBtn.awardType = self._awardType == "Box" and "playBox" or "playGold"
            getAwardBtn.awardId = i
            getAwardBtn:addTouchEventListener(handler(self, self.onTouchEventGetAward))
        elseif playAwardType == 0 then
            getAwardBtn.awardId = i
            getAwardBtn:addTouchEventListener(handler(self, self.onTouchEventGoToPlay))
        end


        local playLimt = clonePanel:getChildByName(KW_TEXT_PLAYLIMIT)
        if playLimt then
            local pCnt = playCnt > playLimit[key] and playLimit[key] or playCnt
            playLimt:setString(pCnt .. "/" .. playLimit[key])
        end
        local playJu = clonePanel:getChildByName(KW_TEXT_PLAY_JU)
        if playJu then
            playJu:setString(self._awardType == "Box" and "场" or "局")
        end

        local awardPanel = clonePanel:getChildByName(KW_PANEL_AWARD .. 1)
        if awardPanel then
            awardPanel:setVisible(true)
            local awardNum = awardPanel:getChildByName(KW_TEXT_NUM)
            if awardNum then
                awardNum:setString("x" .. playAward[key].cnt)
            end
            local awardPic = awardPanel:getChildByName(KW_PIC_AWARD)
            local url = allAward[loginDay][tostring(playAward[key].propid)].url
            if awardPic and url then
                self:addDownloadImageList(url, awardPic)
            end
        end
    end
end

function PlayNode:flushAllAward(data, allAward)
    local actInfo = XH.lobby:getModule("BackActivity"):getActInfo()
    local loginDay = XH.lobby:getModule("BackActivity"):getLoginDay()
    local playAward = self._awardType == "Box" and(data.boxAward.play) or(data.goldAward.play)
    local playType = self._awardType == "Box" and(actInfo.playBoxAaward) or(actInfo.playGoldAaward)
    local loginAward = self._awardType == "Box" and(data.boxAward.login) or(data.goldAward.login)
    local loginType = self._awardType == "Box" and(actInfo.loginBoxCnt) or(actInfo.loginGoldCnt)
    if loginType[loginDay] == 2 then
        for propid, award in pairs(loginAward) do
            allAward[loginDay][propid].cnt = allAward[loginDay][propid].cnt - award.cnt
        end
    end

    for i = 1, 3 do
        if playType[tostring(i)] == 2 then
            allAward[loginDay][tostring(playAward[tostring(i)].propid)].cnt = allAward[loginDay][tostring(playAward[tostring(i)].propid)].cnt - playAward[tostring(i)].cnt
        end
        local awardPanel = self._panelAllAward:getChildByName(KW_ITEM_ALLAWARD ..i)
        if awardPanel then
            awardPanel:setVisible(false)
        end
    end

    local newAllAward = { }
    local propidList = { }
    for day = 1, 7 do
        newAllAward[day] = {}
        for propid, info in pairs(allAward[day]) do
            if #newAllAward[day] == 0 then
                local pos = #newAllAward[day] + 1
                table.insert(newAllAward[day], pos, info)
                table.insert(propidList, pos, propid)
            else
                local addAward = false 
                for j = 1, #newAllAward[day] do
                    if tonumber(propidList[j]) > tonumber(propid) then
                        table.insert(newAllAward[day], j, info)
                        table.insert(propidList, j, propid)
                        addAward = true
                        break
                    end
                end
                if not addAward then
                    local pos = #newAllAward[day] + 1
                    table.insert(newAllAward[day], pos, info)
                    table.insert(propidList, pos, propid)
                end
            end
        end
    end

    local addDay = 1
    for _, info in pairs(allAward[loginDay]) do
        if info.cnt > 0 then
            addDay = 0
        end
    end
    if addDay == 1 then
        self._textAllAwardDay:setString("明日可领取：")
    else
        self._textAllAwardDay:setString("今日可领取：")
    end
    if loginDay + addDay > KW_MAX_LOGIN_DAY then
        self._textAllAwardDay:setString("奖励已领完")
    end
    return newAllAward,(loginDay + addDay)
end

function PlayNode:initAllPlayAward(data)
    local allAward = clone(self._awardType == "Box" and(data.playBoxAllAward) or(data.playGoldAllAward))
    if XH.lobby:getModule("BackActivity"):getLoginDay() > KW_MAX_LOGIN_DAY then
        return
    end
    local newAllAward, loginDay = self:flushAllAward(data, allAward)
    if loginDay > KW_MAX_LOGIN_DAY then
        return 
    end
    local item = 1
    for _, info in ipairs(newAllAward[loginDay]) do
        if info.cnt ~= 0 then
            local awardPanel = self._panelAllAward:getChildByName(KW_ITEM_ALLAWARD .. item)
            if awardPanel then
                awardPanel:setVisible(true)
                local awardNum = awardPanel:getChildByName(KW_TEXT_NUM)
                if awardNum then
                    awardNum:setString("x" .. info.cnt)
                end
                local awardPic = awardPanel:getChildByName(KW_PIC_AWARD)
                local url = info.url
                if awardPic and url then
                    self:addDownloadImageList(url, awardPic)
                end
            end
            item = item + 1
        end
    end

    local itemPanel = ccui.Helper:seekWidgetByName(self._listviewAllAward, "_KW_ITEM_MORE_INFO")
    self._listviewAllAward:setItemModel(clone(itemPanel))
    self._listviewAllAward:removeAllChildren()
    self._listviewAllAward:removeAllItems()
    for i = loginDay + 1, #newAllAward do
        self._listviewAllAward:pushBackDefaultItem()
        local Items = self._listviewAllAward:getItems()
        local clonePanel = Items[#Items]

        local dayText = clonePanel:getChildByName("_KW_TEXT_DAY")
        dayText:setString("第" .. KW_DAY[i] .. "日可领取：")
        local item2 = 1
        for _, info in ipairs(newAllAward[i]) do
            local awardPanel = clonePanel:getChildByName(KW_ITEM_ALLAWARD .. item2)
            if awardPanel then
                awardPanel:setVisible(true)
                local awardNum = awardPanel:getChildByName(KW_TEXT_NUM)
                if awardNum then
                    awardNum:setString("x" .. info.cnt)
                end
                local awardPic = awardPanel:getChildByName(KW_PIC_AWARD)
                local url = info.url
                if awardPic and url then
                    self:addDownloadImageList(url, awardPic)
                end
            end
            item2 = item2 + 1
        end

    end
end

function PlayNode:initThreeAward(data)
    local actInfo = XH.lobby:getModule("BackActivity"):getActInfo()
    local threeAward = self._awardType == "Box" and(data.boxAward.play["0"]) or(data.goldAward.play["0"])
    local threeLimit = self._awardType == "Box" and(actInfo.playBoxLimit["0"]) or(actInfo.playGoldLimit["0"])
    local threeCnt = self._awardType == "Box" and(actInfo.threeBoxCnt) or(actInfo.threeGoldCnt)
    local threeType = self._awardType == "Box" and(actInfo.playBoxAaward["0"]) or(actInfo.playGoldAaward["0"])

    for i, j in pairs(actInfo.playBoxAaward) do
        print(i .. "  " .. j)
    end


    local awardPanel = self._panelThree:getChildByName("_KW_PANEL_THREE")
    local getAwardBtn = awardPanel:getChildByName(KW_BTN_AWARD)
    getAwardBtn:loadTexture(KW_BTN_AWARD_PIC_THREE_NAME .. threeType .. ".png", ccui.TextureResType.plistType)
    getAwardBtn:ignoreContentAdaptWithSize(true)
    if threeType == 1 then
        getAwardBtn.awardType = self._awardType == "Box" and "playBox" or "playGold"
        getAwardBtn.awardId = 0
        getAwardBtn:addTouchEventListener(handler(self, self.onTouchEventGetAward))
    elseif threeType == 0 then
        getAwardBtn.awardId = 0
        getAwardBtn:addTouchEventListener(handler(self, self.onTouchEventGoToPlay))
    end
    if awardPanel then
        local awardNum = awardPanel:getChildByName(KW_TEXT_NUM)
        if awardNum then
            awardNum:setString("x" .. threeAward.cnt)
        end
        local playLimt = awardPanel:getChildByName(KW_TEXT_PLAYLIMIT)
        if playLimt then
            local playcnt = threeCnt > threeLimit and threeLimit or threeCnt
            playLimt:setString(playcnt .. "/" .. threeLimit)
        end
        local playJu = awardPanel:getChildByName(KW_TEXT_PLAY_JU)
        if playJu then
            playJu:setString(self._awardType == "Box" and "场" or "局")
        end
        local awardAni = awardPanel:getChildByName("_KW_ANI_AWARD")
        if awardAni then
            local key = ""
            if threeType == 1 then
                key = key .. "loop-"
            else
                key = key .. "stop-"
            end
            if self._awardType == "Box" then
                key = key .. "fk"
            else
                key = key .. "jb"
            end
            awardAni:getAnimation():play(key, -1, 1)
        end
    end
end

function PlayNode:initThreeTime(time)
    self._actTime = time
    local timeNode = self._textThreeTime
    if timeNode then
        local sequence = cc.Sequence:create(cc.CallFunc:create( function()
            if self._actTime == 0 then
                timeNode:setString("00时00分00秒")
                local actInfo = XH.lobby:getModule("BackActivity"):getActInfo()
                local threeType = self._awardType == "Box" and(actInfo.playBoxAaward["0"]) or(actInfo.playGoldAaward["0"])
                local getAwardBtn = self._panelThree:getChildByName("_KW_PANEL_THREE"):getChildByName(KW_BTN_AWARD)
                if threeType == 0 then
                    getAwardBtn:loadTexture(KW_BTN_AWARD_PIC_THREE_NAME .. 2 .. ".png", ccui.TextureResType.plistType)
                    getAwardBtn:setEnabled(false)
                end
                self._actTime = -1
            elseif self._actTime > 0 then
                local m_TimeD = math.floor(self._actTime /(3600 * 24))
                local m_TimeH = math.floor((self._actTime - m_TimeD * 3600 * 24) / 3600)
                local m_TimeM = math.floor((self._actTime - m_TimeD * 3600 * 24 - m_TimeH * 3600) / 60)
                local m_TimeS = math.floor(self._actTime -(m_TimeD * 3600 * 24 + m_TimeM * 60 + m_TimeH * 3600))
                m_TimeH = string.format("%02d", m_TimeH)
                m_TimeM = string.format("%02d", m_TimeM)
                m_TimeS = string.format("%02d", m_TimeS)
                if m_TimeD > 0 then
                    local timeString = m_TimeD .. "天" .. m_TimeH .. "时" .. m_TimeM .. "分"
                    timeNode:setString(timeString)
                else
                    local timeString = m_TimeH .. "时" .. m_TimeM .. "分" .. m_TimeS .. "秒"
                    timeNode:setString(timeString)
                end
                self._actTime = self._actTime - 1
            end
        end ), cc.DelayTime:create(1))
        local action = cc.RepeatForever:create(sequence)
        timeNode:runAction(action)
    end
end

function PlayNode:onTouchEventShowRule(send, eventType)
    XH.viewManager:openView("BackActivityRuleView", nil)
end

function PlayNode:onTouchEventShowMore(send, eventType)
    XH.lobby:getModule("TabsActivity"):changeTabTo("TASKSYS")
end

function PlayNode:onTouchEventShowMoreAllAward(send, eventType)
    self._btnShowMoerAward:setVisible(false)
    self._btnCloseMoerAward:setVisible(true)
    self._listviewAllAward:setVisible(true)
end

function PlayNode:onTouchEventCloseMoreAllAward(send, eventType)
    self._btnShowMoerAward:setVisible(true)
    self._btnCloseMoerAward:setVisible(false)
    self._listviewAllAward:setVisible(false)
end

function PlayNode:onTouchEventGetAward(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local awardType = send.awardType
    local awardId = send.awardId
    if awardType and awardId then
        XH.lobby:getModule("BackActivity"):reqBackAwardProtocol(awardType, awardId)
    end
end

function PlayNode:onTouchEventGoToPlay(send, eventType)
    if self._awardType == "Box" then
        XH.viewManager:openView("BackActivityChooseView")
    else
        XH.lobby:getModule("Lobby"):showXiuXianGame()
        XH.lobby:getModule("TabsActivity"):dispatchEvent({name = XH.lobby:getModule("TabsActivity").EVENT_CLOSE_TAB_VIEW})
    end
end

function PlayNode:addDownloadImageList(url, node)
    if url and node then
        local find = false
        for key, urlVal in ipairs(self._downloadImageUrlList) do
            if urlVal == url then
                table.insert(self._downloadImageNodeList[key], node)
                find = true
                break
            end
        end
        if not find then
            table.insert(self._downloadImageUrlList, url)
            self._downloadImageNodeList[#self._downloadImageUrlList] = { }
            table.insert(self._downloadImageNodeList[#self._downloadImageUrlList], node)
        end
    end
end

function PlayNode:onDownloadImageCallBack(event)
    if event then
        local imagePath = event.data.imagePath
        local url = event.data.url

        for key, urlVal in ipairs(self._downloadImageUrlList) do
            if urlVal == url then
                for _, node in pairs(self._downloadImageNodeList[key]) do
                    node:loadTexture(imagePath, ccui.TextureResType.localType)
                    node:setVisible(true)
                end
                break
            end
        end
    end
end

function PlayNode:downloadImage()
    for _, url in pairs(self._downloadImageUrlList) do
        XH.lobby:getModule("BackActivity"):reqDownloadImage(url)
    end
end

function PlayNode:runBtnAni()
    local actInfo = XH.lobby:getModule("BackActivity"):getActInfo()
    local playType = self._awardType == "Box" and(actInfo.playBoxAaward) or(actInfo.playGoldAaward)
    for i = 1, 3 do
        local playAwardType = playType[tostring(i)]
        if playAwardType == 0 then
            local getAwardBtn = self["_playItemPanel_" .. i]:getChildByName(KW_BTN_AWARD)
            self:runTimeLineAni(getAwardBtn)
            return
        end
    end
    local threeType = self._awardType == "Box" and(actInfo.playBoxAaward["0"]) or(actInfo.playGoldAaward["0"])
    if threeType == 0 then
        local awardPanel = self._panelThree:getChildByName("_KW_PANEL_THREE")
        local getAwardBtn = awardPanel:getChildByName(KW_BTN_AWARD)
        self:runTimeLineAni(getAwardBtn)
    end
end

function PlayNode:runTimeLineAni(node)
    node:stopAllActions()
    local action = cc.CSLoader:createTimeline(self:getCSBPath())
    node:runAction(action)
    action:gotoFrameAndPlay(0, 105, true)
end

return PlayNode �Q  