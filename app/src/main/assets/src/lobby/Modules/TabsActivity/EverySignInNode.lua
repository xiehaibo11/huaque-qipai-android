local EverySignInNode = class("EverySignInNode",XH.ViewBase)

EverySignInNode.KW_NAME_EVERY_DAY_NODE = "KW_NAME_EVERY_DAY_NODE"

local KW_PANEL_SIGNIN = "KW_PANEL_SIGNIN"
local KW_IMG_SIGNIN_BG = "KW_IMG_SIGNIN_BG"
local KW_IMG_DAY =  "KW_IMG_DAY"
local KW_IMG_DROP_ID = "KW_IMG_DROP_ID"
local KW_TEXT_DROP  = "KW_TEXT_DROP"
local KW_SIGNIN_DAY = "KW_SIGNIN_DAY_"
local KW_ANI_NODE = "KW_ANI_NODE"
local KW_IMG_YQD = "KW_IMG_YQD"
local KW_BTN_SIGNIN = "KW_BTN_SIGNIN"

function EverySignInNode:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/EveryDaySignInLayer.csb"
end

function EverySignInNode:getBindingInfo()
    return {
    }
end

function EverySignInNode:ctor(param)
    param = param or {}
    EverySignInNode.super.ctor(self,param) 
    self.csbNode = self
	self.width = param.width or 1342
	self.height = param.height or 870
	self:setContentSize(self.width,self.height)
	self:setName(self.KW_NAME_EVERY_DAY_NODE)
    self:reqAwardList()
    self:initTouchEvent()
    self._today = 0
end

function EverySignInNode:getProxyEvents()
    return {
        { module = XH.lobby:getModule("TabsActivity"),eventKeyName = "EVENT_EVERY_SIGNIN_LIST", callBack = "flushEverySigninList" },
        { module = XH.lobby:getModule("TabsActivity"),eventKeyName = "EVENT_INIT_EVERY_SIGNIN", callBack = "flushInitEverySign" },
    }
end

function EverySignInNode:initTouchEvent()
    if not self.csbNode then
        return
    end

    XH.UITool.addTouchEventListener(self.csbNode, KW_BTN_SIGNIN, handler(self,self.onTouchEventSignIn))
end

function EverySignInNode:flushActEvent()
    self:reqAwardList()    
end

function EverySignInNode:onTouchEventSignIn(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.lobby:getModule("TabsActivity"):reqEverySignInList()
    
end

function EverySignInNode:reqAwardList()
    XH.lobby:getModule("TabsActivity"):reqEverySignAwardList()
end

function EverySignInNode:flushInitEverySign(event)
    if self.initSignInData then
        self:initSignInData(event.data)
    end
end

function EverySignInNode:reqEverySignInList()
    XH.lobby:getModule("TabsActivity"):reqEverySignAwardList()
end

function EverySignInNode:flushEverySigninList(event)
    self:playSignInAni(self._today)
    self:reqAwardList()
    XH.playerData:flushPlayerDrop()
    XH.playerData:flushGoldCoin()
end

function EverySignInNode:initSignInData(data)
    if data then 
        self:initSignInBtn(data.currTime)
        local signInPanel = XH.UITool.seekNodeByName(self.csbNode, KW_PANEL_SIGNIN)
        local times = data.times
        local awardList =  data.awardList
        if times and awardList then
            for i = 1,#awardList do
                local signNode = signInPanel:clone()
                signNode:removeFromParent(false)
                signNode:setPosition(cc.p(0,0))
                self._today = times + 1
                self:initDayNode(signNode,awardList[i][1],i,times,data.currTime)

                local dayNode = XH.UITool.seekNodeByName(self.csbNode, KW_SIGNIN_DAY ..i)
                if dayNode then
                    dayNode:removeAllChildren()
                    dayNode:addChild(signNode)
                end
            end
        end
    end
end

function EverySignInNode:initDayNode(node,data,index,times,currTime)
    if node and data then
        local propList = XH.areaData:getPropList()
        local dropNode = XH.UITool.seekNodeByName(node, KW_IMG_DROP_ID)
        local dropCntText = XH.UITool.seekNodeByName(node, KW_TEXT_DROP)
        local imgSignIn = XH.UITool.seekNodeByName(node, KW_IMG_YQD)
        dropNode:ignoreContentAdaptWithSize(true)
        if index <= times then
            imgSignIn:setVisible(true)
        end
        if data.type == -1 then
             if data.cnt >=0 and data.cnt < 100 then
                dropNode:loadTexture("ico_gold_1.png", ccui.TextureResType.plistType)
             elseif data.cnt >=100 and data.cnt < 1000 then
                dropNode:loadTexture("ico_gold_2.png", ccui.TextureResType.plistType)
             elseif data.cnt >=1000 and data.cnt < 10000 then
                dropNode:loadTexture("ico_gold_3.png", ccui.TextureResType.plistType)
             elseif data.cnt >=10000  then
                dropNode:loadTexture("ico_gold_4.png", ccui.TextureResType.plistType)
             end

             local str =  XH.ConstString.getStr("ACTIVITY_GOAL_COUNT", tostring(data.cnt))
             dropCntText:setString(str)
        elseif data.type == propList.roomCardID then
            if data.cnt == 1 then
                dropNode:loadTexture("ico_card_1.png", ccui.TextureResType.plistType)
             elseif data.cnt == 2 then
                dropNode:loadTexture("ico_card_2.png", ccui.TextureResType.plistType)
             elseif data.cnt == 3 then
                dropNode:loadTexture("ico_card_3.png", ccui.TextureResType.plistType)
             elseif data.cnt >= 4  then
                dropNode:loadTexture("ico_card_4.png", ccui.TextureResType.plistType)
             end
             local str = XH.ConstString.getStr("ACTIVITY_ROOM_CARD", tostring(data.cnt))
             dropCntText:setString(str)
        elseif data.type == propList.propDiamndID then
            if data.cnt <= 8 then
                dropNode:loadTexture("ico_diamond_1.png", ccui.TextureResType.plistType)
             elseif data.cnt > 8 and data.cnt <= 28 then
                dropNode:loadTexture("ico_diamond_2.png", ccui.TextureResType.plistType)
             elseif data.cnt > 28 and data.cnt <= 88 then
                dropNode:loadTexture("ico_diamond_3.png", ccui.TextureResType.plistType)
             elseif data.cnt > 88  then
                dropNode:loadTexture("ico_diamond_4.png", ccui.TextureResType.plistType)
             end
             local str = XH.ConstString.getStr("ACTIVITY_DIAMOND", tostring(data.cnt))
             dropCntText:setString(str)
        end
       
        local dayImg = XH.UITool.seekNodeByName(node, KW_IMG_DAY)
        if dayImg then
            dayImg:loadTexture("img_every_day_text_"..index..".png", ccui.TextureResType.plistType)
        end
        local dayNode = XH.UITool.seekNodeByName(node, KW_IMG_SIGNIN_BG)
        if currTime == 0 then
            if index == times + 1 then
                dayNode:loadTexture("img_every_day_bg_2.png", ccui.TextureResType.plistType)
            end
        else 
            if index == times  then
                dayNode:loadTexture("img_every_day_bg_2.png", ccui.TextureResType.plistType)
            end
        end
        
    end
end

function EverySignInNode:initSignInBtn(currTime)
    if currTime then
        local signInBtn = XH.UITool.seekNodeByName(self.csbNode, KW_BTN_SIGNIN)
        if currTime == 0 then
             signInBtn:setBright(true)
             signInBtn:setEnabled(true)
        else
            signInBtn:setBright(false)
            signInBtn:setEnabled(false)
        end
        signInBtn:setVisible(true)
    end
end

function EverySignInNode:playSignInAni(day)
     local dayNode = XH.UITool.seekNodeByName(self.csbNode, KW_SIGNIN_DAY ..day)  
     if not dayNode then
        return
     end
     local aniNode = XH.UITool.seekNodeByName(dayNode, KW_ANI_NODE)  
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
                aniNode:removeChildByName(writeOver)
            end
        end )
    end
end

return EverySignInNode|!  