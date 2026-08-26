local NewUserSignInNode = class("NewUserSignInNode",XH.ViewBase)
local RemoteImage = XH.RemoteImage

NewUserSignInNode.KW_NAME_NEW_USER_SIGNIN_NODE = "KW_NAME_NEW_USER_SIGNIN_NODE"

local MAX_PROP_NODE_COUNT = 7
local KW_MAX_TEXT_WIDTH = 210
local KW_MAX_TEXT_FONT_SIZE = 36
local KW_MIN_TEXT_FONT_SIZE = 24

function NewUserSignInNode:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/NewUserSignInLayer.csb"
end

function NewUserSignInNode:getBindingInfo()
    return {
        ["_KW_BTN_SIGNIN"] = { varName="_signButton", type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventSignIn" },
        ["_KW_ACT_LEFT_TIME"] = { varName="_actLeftTime", type = XH.UI_TYPE.TEXT},
        ["_KW_SIGNIN_DAY_"] = { beginIndex = 1, endIndex = MAX_PROP_NODE_COUNT, varName = "_propItem_"},
        ["_KW_PANEL_SIGNIN_ITEM"] = { varName="_propItem"},
        ["_KW_PANEL_SIGNIN_ITEM_FINAL"] = { varName="_propFinalItem"},
    }
end

function NewUserSignInNode:ctor(param)
    param = param or {}
    NewUserSignInNode.super.ctor(self,param) 
    self.csbNode = self
	self.width = param.width or 1282
	self.height = param.height or 792
	self:setContentSize(self.width,self.height)
    self:setName(self.KW_NAME_NEW_USER_SIGNIN_NODE)
    self:reqAwardList()
end

function NewUserSignInNode:getProxyEvents()
    return {
        { module = XH.lobby:getModule("TabsActivity"),eventKeyName = "EVENT_NEW_USER_ACT_SIGNIN_LIST", callBack = "flushNewUserSignInList" },
        { module = XH.lobby:getModule("TabsActivity"),eventKeyName = "EVENT_NEW_USER_ACT_SIGNIN_AWARD", callBack = "flushInitNewUserSign" },
    }
end


function NewUserSignInNode:reqAwardList()
    XH.lobby:getModule("TabsActivity"):reqNewUserSignInInfo()
end

function NewUserSignInNode:flushInitNewUserSign(event)
    -- 播放签到动画和奖励
    if event.data then
        local func = function()
            local award = {}
            award.url = event.data.image_url
            award.desc = event.data.prize_name
            XH.viewManager:openView("CommonRewardView", nil, {award})
        end
        self:performWithDelay(func, 0.6)
        XH.playerData:flushPlayerDrop()
        XH.playerData:flushGoldCoin()
    end
    if self._todayIndex then
        self:playSignInAni(self._todayIndex)
    end
    self:updateSignInBtn(true)
end

function NewUserSignInNode:flushNewUserSignInList(event)
    self:updateSignInActUI(event.data)
end

function NewUserSignInNode:updateSignInActUI(actInfo)
    if actInfo then
        if actInfo.isSigned then
            self._todayIndex = actInfo.awardTimes
        else
            self._todayIndex = actInfo.awardTimes + 1
        end
        self:updateSignInBtn(actInfo.isSigned)
        --刷新时间
        self._actLeftTime:setString(self:getActLeftTimeStr(actInfo.endActTime))
        --刷新签到奖品列表
        for i = 1, MAX_PROP_NODE_COUNT do
            if next(actInfo.awardList) then
                if i == self._todayIndex then
                    self._toadyPoprWebPoolID  = actInfo.awardList[i].pid or 0
                end
                self:updatePropNode(self[string.format("_propItem_%d",i)],actInfo.awardList[i],i,actInfo.isSigned)
            end
        end
    end
end

function NewUserSignInNode:updatePropNode(node,data,index,isSigned)
    --克隆节点
    if not node or not data then
        return
    end
    local cloneNode
    if index == MAX_PROP_NODE_COUNT then
        cloneNode = self._propFinalItem:clone()
    else
        cloneNode = self._propItem:clone()
    end
    if cloneNode then
        --天数
        cloneNode:removeFromParent(false)
        cloneNode:setPosition(cc.p(0,0))
        local dayNode = cloneNode:getChildByName("_KW_TEXT_DAY")
        if dayNode then
            dayNode:setString("第" .. index .. "天")
        end
        --图片 
        local imgNode = cloneNode:getChildByName("_KW_IMG_DROP")
        if imgNode then
            local size = imgNode:getContentSize()
            local propImg = RemoteImage.new()
                :setTouchEnabled(false)
                :setPosition(size.width/2 - 0.5, size.height/2 + 0.3)
                :ignoreContentAdaptWithSize(false)
                :setContentSize(size.width - 7, size.height - 7)
                :addTo(imgNode)
            propImg:setUrl(data.url)
        end
        --名称    
        local nameNode = cloneNode:getChildByName("_KW_TEXT_DROP")
        if nameNode then
            nameNode:setString(data.prize_name)
            self:adaptSuitableFontSize(nameNode,KW_MAX_TEXT_WIDTH,KW_MAX_TEXT_FONT_SIZE,KW_MIN_TEXT_FONT_SIZE)
        end
        if self._todayIndex == index then         
            if index ~= MAX_PROP_NODE_COUNT then               
                local bgNode = cloneNode:getChildByName("_KW_IMG_SIGNIN_BG")
                if bgNode then
                    bgNode:loadTexture("act_new_user_item_2.png",ccui.TextureResType.plistType)
                end
            end
        end
        if index < self._todayIndex or (self._todayIndex == index and isSigned) then
            local signedNode = cloneNode:getChildByName("_KW_IMG_YQD")
            if signedNode then
                signedNode:setVisible(true)
            end
        end
        node:removeAllChildren()
        node:addChild(cloneNode)
    end
end

function NewUserSignInNode:adaptSuitableFontSize(widget, width, maxFontSize, minFontSize)
    if widget == nil then
        return 0
    end

    local curFontSize = maxFontSize
    while curFontSize > minFontSize do
        if widget:getContentSize().width > width then
            curFontSize = curFontSize - 1
            widget:setFontSize(curFontSize)
        else
            break
        end
    end
    return curFontSize
end

function NewUserSignInNode:getActLeftTimeStr(endtime)
    local timeMsg
    local leftTime = endtime - os.time()
    local day = math.floor(leftTime/86400)
    local hour = math.floor((leftTime-day*86400)/3600)
    local min = math.floor((leftTime-day*86400-hour*3600)/60)
    if day > 0 then
        timeMsg = day .. "天" .. hour.. "小时"
    else
        timeMsg = hour .. "小时" .. min.. "分"
    end
    return timeMsg
end

function NewUserSignInNode:updateSignInBtn(isSigned)
    if isSigned then
        self._signButton:setBright(false)
        self._signButton:setEnabled(false)
    else
        self._signButton:setBright(true)
        self._signButton:setEnabled(true)
    end
    self._signButton:setVisible(true)
end

function NewUserSignInNode:onTouchEventSignIn(send, eventType)
    XH.lobby:getModule("TabsActivity"):reqNewUserSignInAward(self._toadyPoprWebPoolID)
end

function NewUserSignInNode:playSignInAni(index)
    local dayNode = self[string.format("_propItem_%d",index)] 
    if not dayNode then
       return
    end
    local aniNode = XH.UITool.seekNodeByName(dayNode,"_KW_ANI_NODE")  
    if not aniNode then
       return
    end
    if not cc.FileUtils:getInstance():isFileExist("animation/Lobby/Base/activity_sign_in/writ_over.ExportJson") then
        return
    end
    ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(
       "animation/Lobby/Base/activity_sign_in/writ_over0.png", 
       "animation/Lobby/Base/activity_sign_in/writ_over0.plist", 
       "animation/Lobby/Base/activity_sign_in/writ_over.ExportJson")

   local lastCreatArmature = ccs.Armature:create("writ_over")
   local writeOver = "writ_over"
   if lastCreatArmature then
       aniNode:addChild(lastCreatArmature)
       lastCreatArmature:setAnchorPoint(cc.p(0.5, 0.5))
       lastCreatArmature:setName(writeOver)
       lastCreatArmature:setPosition(cc.p(0,0))
       lastCreatArmature:getAnimation():play("Animation1", -1, 1)
       lastCreatArmature:getAnimation():setMovementEventCallFunc( function(arm, moventType, listern)
           if moventType ~= 0 then
                local signedNode = XH.UITool.seekNodeByName(dayNode,"_KW_IMG_YQD") 
                if signedNode then
                    signedNode:setVisible(true)
                end
                aniNode:removeChildByName(writeOver)
           end
       end )
   end
end

return NewUserSignInNode�   