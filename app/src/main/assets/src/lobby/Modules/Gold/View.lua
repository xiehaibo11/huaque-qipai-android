-- 金币场
local GoldView = class("GoldView", XH.ViewBase)
local LobbyConfig = require("lobby.Modules.Lobby.Config")
local ConfigurationDefine = require("lobby.Modules.Configuration.Define")

local KW_IMG_ACTIVITY = "_KW_IMG_ACTIVITY"
local KW_TEXT_LFETTIME = "_KW_TEXT_LFETTIME"
local KW_IMG_LEFT_TIME = "_KW_IMG_LEFT_TIME"
local KW_GOLD_PLAY_ACT_TISHI = "_KW_GOLD_PLAY_ACT_TISHI"
local KW_GOLD_PLAY_TEXT = "_KW_GOLD_PLAY_TEXT"

local KW_ITEM_BG_TO_TITLE_COLOR = {
    [0] = {194,255,173},
    [1] = {194,255,173},
    [2] = {194,255,173},
    [3] = {255,194,140},
    [4] = {134,239,255},
    [5] = {204,248,149},
    [6] = {252,243,178},
}

function GoldView:getCSBPath()
    return "cocosStudio/hall/CSB/goldroom/GoldLayer.csb"
end

function GoldView:getBindingInfo()
    return {
        ["_KWA_BTND_CREATE_JINBICHANG_ITEM"] = {varName = "KWA_BTND_CREATE_JINBICHANG_ITEM"},
        ["_KWA_XIUXIAN_LOBBY_PANEL"] = {varName = "_goldRoot"},
        ["_KW_UI_GAME_LIST_VIEW"] = {varName = "_gameListView"},
        ["_KW_UI_BACK"] = {varName = "KW_UI_BACK", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KWA_BTND_BACk"},
        ["_KW_SCROLL_ANI"] = {varName = "KW_SCROLL_ANI"}
    }
end

function GoldView:getProxyEvents()
    return {
        {module = XH.goldConfigManager, eventKeyName = "GOLD_EVENT_CONFIG", callBack = "onRefreshUI"},
        {module = XH.goldConfigManager, eventKeyName = "GOLD_EVENT_ROOMINFO", callBack = "onRespGoldRoomInfo"},
        {module = XH.goldConfigManager, eventKeyName = "GOLD_EVENT_SHOWPLAYER_COUNT", callBack = "onRespGoldPlayerCount"},
        {module = XH.goldConfigManager, eventKeyName = "GOLD_EVENT_REQ_PLAYER_CNT", callBack = "onReqGoldPlayerCount"},
        {module = XH.lobby:getModule("Gold"), eventKeyName = "EVENT_REFRESH_GOLD_SYSTEMTIME", callBack = "onSystemTime"},
        {module = XH.lobby:getModule("Guide"), eventKeyName = "EVENT_UPDATE_NEW_USER_BACK_BTN", callBack = "updateNewUserGuideUI"},
        { module = XH.lobby:getModule("Lobby"), eventKeyName = "SHOW_LEISURE_ENTRANCE",callBack = "onEventShowLeisureEntrance"},
        { module = XH.taskManager, eventKeyName = "SHARE_GIVE_GOLD_SUCCESS", callBack = "onEventShareGiveGold" },
    }
end

function GoldView:ctor()
    GoldView.super.ctor(self)
    self._timeStamp = os.time()
    self.hasInit = false
    self._moveItemPos = -1 * (self.KWA_BTND_CREATE_JINBICHANG_ITEM:getContentSize().width + 44) 
end

function GoldView:showGoldLayer(bMoveIn)
    if bMoveIn then
        self:moveInAnimation()
    else
        self:moveOutAnimation()
    end
    self:adaptForLiuHai()
    self:updateNewUserGuideUI()
end

function GoldView:updateNewUserGuideUI()
    self.KW_UI_BACK:setVisible(not XH.playerData:getIsFirstGoldGuide())
end

function GoldView:on_KWA_BTND_BACk(send, eventType)
    XH.lobby:getModule("Im"):dispatchEvent({name = XH.lobby:getModule("Im").EVENT_SHOW_LIST_ENTRANCE})
    XH.lobby:getModule("Lobby"):dispatchEvent({name = XH.lobby:getModule("Lobby").SHOW_BOX_GAME})
end

function GoldView:hide()
    self:moveOutAnimation()
end

function GoldView:initGameListView()
    self._gameListView:removeAllChildren()
    local goldGames = XH.goldConfigManager:getGoldRoomlist()

    XH.playerData:screenGameList(goldGames,1)

    if goldGames and #goldGames > 0 then
        self._gameListView:setInnerContainerSize(cc.size(570 * #goldGames, self._gameListView:getContentSize().height))
        self._gameListView:setItemsMargin(44)
        self._gameListView:setScrollBarEnabled(false)
        self._gameListView:setBounceEnabled(true)
        self._gameListView:addScrollViewEventListener(function(sender, eventType)
            if eventType == ccui.ScrollviewEventType.containerMoved + 2 then
                self:changeScrollAniDirection()
            end
        end)
        if #goldGames >= 4 then
            local params = {
                path = "animation/Lobby/Base/jbc-tip-jt/",
                ske = "jbc-tip-jt_ske.json",
                tex = "jbc-tip-jt_tex.json",
                armatureName = "Armature",
                dragonBonesName = "jbc-tip-jt",
                animationName = "newAnimation"
            }
            local arrowAni = display.playDargonBonesAnimByTimes(params, 0)
            if arrowAni and self.KW_SCROLL_ANI then
                self.KW_SCROLL_ANI:removeAllChildren()
                self.KW_SCROLL_ANI:setVisible(true)
                self.KW_SCROLL_ANI:addChild(arrowAni)
                self.KW_SCROLL_ANI:setTouchEnabled(true)
                self.KW_SCROLL_ANI:addTouchEventListener(handler(self, self.onTouchMoveBtn))
                arrowAni:setPosition(cc.p(self.KW_SCROLL_ANI:getContentSize().width/2,self.KW_SCROLL_ANI:getContentSize().height/2))
            end
        end
        local reqData = {}
        for i = 1, #goldGames do
            local tmpCreateBtn = self.KWA_BTND_CREATE_JINBICHANG_ITEM:clone()
            self._gameListView:addChild(tmpCreateBtn)
            tmpCreateBtn:setName("KWA_BTND_CREATE_" .. goldGames[i].Index)
            tmpCreateBtn:setTag(goldGames[i].GameID)
            tmpCreateBtn:addTouchEventListener(handler(self, self.on_KWA_BTND_CREATE_JINBICHANG))
            tmpCreateBtn:setVisible(true)
            self:initCreateBtnInfo(tmpCreateBtn, goldGames[i])
            reqData[i] = {}
            reqData[i].gameID = goldGames[i].GameID
            
            local goldPlayActTiShi = ccui.Helper:seekWidgetByName(tmpCreateBtn, KW_GOLD_PLAY_ACT_TISHI)
            if goldGames[i].GoldActText then
                goldPlayActTiShi:setVisible(true)
                goldPlayActTiShi:getChildByName(KW_GOLD_PLAY_TEXT):setString(goldGames[i].GoldActText)
            end
        end
        XH.goldConfigManager:reqRoomPlayerCount(reqData)
        self._gameListView:refreshView()
    end
end

function GoldView:onTouchMoveBtn(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:startMove()
end

function GoldView:startMove()
    local innerSizeWidth = self._gameListView:getInnerContainerSize().width - self._gameListView:getContentSize().width
    local listInnerContaniner = self._gameListView:getInnerContainer()
    local gameListPos = {}
    gameListPos.x, gameListPos.y = listInnerContaniner:getPosition()
    if gameListPos.x  < -(innerSizeWidth - self._moveItemPos + 44) then
        self.KW_SCROLL_ANI:setScaleX(-1)
        self._moveItemPos = self._moveItemPos * -1
    elseif gameListPos.x > -44 then
        self.KW_SCROLL_ANI:setScaleX(1)
        self._moveItemPos = math.abs( self._moveItemPos )  
    end
    local itemCount = #(self._gameListView:getItems())
    local tmp1,_ = math.modf((math.abs(gameListPos.x) + 1) / math.abs( self._moveItemPos ))
    if self._moveItemPos > 0 then
        tmp1 = tmp1 + 1
    else
        tmp1 = tmp1 - 1
    end
    if tmp1 < 0 then
        tmp1 = 0
    end
    if tmp1 > (itemCount - 5) then
        self.KW_SCROLL_ANI:setScaleX(-1)
    end
    if tmp1 == 0 then
        self.KW_SCROLL_ANI:setScaleX(1)
    end
    self._gameListView:scrollToItem(tmp1,cc.p(0,0),cc.p(0,0))
end 

--改变箭头
function GoldView:changeScrollAniDirection()
    local innerPos = self._gameListView:getInnerContainerPosition()
    local innerSizeWidth = self._gameListView:getInnerContainerSize().width - self._gameListView:getContentSize().width
    if innerPos.x >= 0 then 
        self.KW_SCROLL_ANI:setScaleX(1)
        self._moveItemPos = math.abs( self._moveItemPos ) 
    elseif innerPos.x <= -(innerSizeWidth - self._moveItemPos + 44) then 
        self.KW_SCROLL_ANI:setScaleX(-1)
        self._moveItemPos = self._moveItemPos * -1
    end
end

function GoldView:onRespGoldRoomInfo(event)
    if self._lastConfID and self._lastRoomFlag then
        XH.TipTool.hideLoading()
        self:reqJoinGoldRoom()
        self._lastConfID = nil
        self._lastRoomFlag = nil
    end
end

function GoldView:on_KWA_BTND_CREATE_JINBICHANG(send, eventType)
    if eventType == ccui.TouchEventType.began then
        self._canSlide = true
        XH.audioManager:play("BUTTON_CLICK")
        send:setScale(0.9)
        send:setColor(cc.c3b(160, 160, 160))
    elseif eventType ==ccui.TouchEventType.moved then
        if self._canSlide == true then
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.Gold_GAME_7)
            self._canSlide = false
        end 
    elseif eventType == ccui.TouchEventType.ended or eventType == ccui.TouchEventType.canceled then
        send:setScale(1.0)
        send:setColor(cc.c3b(255, 255, 255))
    end
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local confID = XH.StringTool.getNumberSuffixByString(send:getName())
    self._lastConfID = confID
    local roomConf = XH.goldConfigManager:getGoldRoomInfoByIndex(self._lastConfID)
    if XH.playerData:checkNewRealName(roomConf.roomInfo.GameID) ~= 0 then return end--实名限
    local isNewUser=XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEW_USER_GUIDE_FINISHED)
    if isNewUser ==false then
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.NEWPLAYER_GUIDE_12,{GameID =roomConf.roomInfo.GameID,GameName = roomConf.roomInfo.GameTitle})
    end
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.GOLD_GAME_6,{GameName = roomConf.roomInfo.GameTitle})
    self._lastRoomFlag = send.roomFlag
    if roomConf.roomInfo.GameID == LobbyConfig.GoldNewGameID then
        XH.lobby:getModule("Lobby"):enterNewGold()
        return
    end
    if roomConf.roomInfo.GameID == 999999 then
        XH.viewManager:openView("QuestionGuideView", nil, {from = "Gold", type = "GuanDan"})
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.GUANDAN_GOLD_CLICK, nil, {area_id = XH.areaData:getAreaID(),channel_id = XH.SysTool:getChannelID()})
        return
    end
    self:onTouchJoinGoldBtn()
end

function GoldView:onTouchJoinGoldBtn()
    if XH.lobby:getModule("Gold"):reqTaskGetAward() then
        return
    end

    if not XH.goldConfigManager:getGoldRoomDetailInfos() then
        XH.TipTool.showLoading()
        XH.goldConfigManager:changeGroupLogin()
        return
    end

    self:reqJoinGoldRoom()
end

function GoldView:reqJoinGoldRoom()
    local confID = self._lastConfID
    local roomFlag = self._lastRoomFlag
    local roomConf = XH.goldConfigManager:getGoldRoomInfoByIndex(confID)
    if roomConf.roomLevelInfos and #roomConf.roomLevelInfos == 0 then
        XH.TipTool.showToast("获取房间信息出错 - " .. roomConf.roomInfo.GameID, 2)
        return
    end

    local func = function()
        XH.lobby:getModule("ToponAct"):setHotUpdata(false)
        --新手引导情况下直接去初级金币场
        if roomFlag and #roomFlag > 1 and not XH.playerData:getIsFirstGoldGuide() then
            local moduleName = "lobby.Modules.Gold.LevelView" .. (roomConf.roomInfo.goldUIStype or "")
            local success, levelLayerModule = pcall(require, moduleName)
            if not success then
                -- 如果加载失败，则使用默认模块
                levelLayerModule = require("lobby.Modules.Gold.LevelView")
            end
            local levelLayer = levelLayerModule.new(confID, roomConf.roomInfo.GameID, roomConf.roomInfo.goldUIStype)
            self:addChild(levelLayer)
            return
        end
        if roomFlag and #roomFlag > 0 then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_JOIN_GOLD_GAME_ID, confID)
            XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_JOIN_GOLD_LEVEL, roomFlag[1])
            XH.lobby:getModule("Gold"):reqJoinGoldRoom(confID, roomFlag[1])
        end
        --新手引导关闭引导界面
        XH.lobby:getModule("Gold"):closeNewUserGameGuideView()
        XH.playerData:setIsFirstGoldGuide(false)
        self:updateNewUserGuideUI()
    end

    self:isNeedHotUpdata(roomConf.roomInfo.GameID, func)
end

function GoldView:initCreateBtnInfo(buttonNode, roomInfo)
    if roomInfo == nil or roomInfo == {} or not buttonNode then
        return
    end
    XH.UITool.setText(buttonNode, "_KW_LABEL_ITEM_TITLE", roomInfo.GameTitle)
    local titleColor = KW_ITEM_BG_TO_TITLE_COLOR[0]
    if roomInfo.GameItemBG and KW_ITEM_BG_TO_TITLE_COLOR[roomInfo.GameItemBG] then
        titleColor = KW_ITEM_BG_TO_TITLE_COLOR[roomInfo.GameItemBG]
    end
    XH.UITool.setTextColor(buttonNode, "_KW_LABEL_ITEM_TITLE", cc.c3b(titleColor[1], titleColor[2], titleColor[3]))
    self:loadTexture(buttonNode, "gold_item_" .. roomInfo.GameItemBG .. ".png")
    local node = XH.UITool.seekNodeByName(buttonNode, "_KW_LABEL_ITEM_LOGO")
    if node then
        self:loadTexture(node, "gold_item_logo_" .. roomInfo.GameItemLogo .. ".png")
    end

    if roomInfo.GameID == 999999 then
        local iconNew = XH.UITool.seekNodeByName(buttonNode, "_KW_ICON_NEW")
        if iconNew then
            XH.SysTool.createShakeAni(iconNew)
            iconNew:setVisible(true)
        end
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.GUANDAN_ICON_SHOW, nil, {area_id = XH.areaData:getAreaID(),channel_id = XH.SysTool:getChannelID()})
    end
    XH.UITool.setVisible(buttonNode, "_KW_IMG_FLAG_NEW_LEVELUP", self:isShowNewLevelUpFlag(roomInfo.GameID, roomInfo.PlayerCount))

    buttonNode.roomFlag = roomInfo.roomFlag or nil
    if self:isShowBottomTitle(roomInfo) then 
        XH.UITool.setVisible(buttonNode, "_KW_LABEL_ITEM_TITLE_BOTTOM",true)
        XH.UITool.setText(buttonNode, "_KW_LABEL_ITEM_TITLE_BOTTOM", self:isShowBottomTitle(roomInfo))
        if roomInfo.GameTitle == "瞎子麻将" or roomInfo.GameTitle == "亮子麻将" then 
            XH.UITool.setText(buttonNode, "_KW_LABEL_ITEM_TITLE", "冲击麻将")
        end 
    end
end

function GoldView:isShowBottomTitle(roomInfo)
    local txtBottom = {
        [30116] = "千变",
        [42038] = "千变",
        [30386] = "跑得快",
        [30159] = "杭州",
        [301170002] = "二人",
        [30332] = "灵溪",
    }
    local finalTxt = ""
    if roomInfo.GameID and txtBottom[roomInfo.GameID] then 
        finalTxt = txtBottom[roomInfo.GameID]
    elseif roomInfo.PlayerCount and roomInfo.GameID and txtBottom[tonumber(roomInfo.GameID .. "000" .. roomInfo.PlayerCount)] then 
        finalTxt = txtBottom[tonumber(roomInfo.GameID .. "000" .. roomInfo.PlayerCount)]
    elseif roomInfo.GameTitle == "瞎子麻将" then 
        finalTxt = "瞎子"
    elseif roomInfo.GameTitle == "亮子麻将" then 
        finalTxt = "亮子"
    end 
    return finalTxt
end

function GoldView:loadTexture(node, fileName)
    if cc.SpriteFrameCache:getInstance():getSpriteFrameByName(fileName) then
        node:loadTexture(fileName, ccui.TextureResType.plistType)
    end
end

--进入动画
function GoldView:moveInAnimation()
    if self._goldRoot then
        local actionTime = 0.25
        self._goldRoot:stopAllActions()
        local targetPosX = display.width / 2
        local targetPosY = self._goldRoot:getPositionY()
        local actSpawn = cc.Spawn:create(cc.FadeIn:create(actionTime), cc.MoveTo:create(actionTime, cc.p(targetPosX, targetPosY)))
        local sequenceAction =
            cc.Sequence:create(
            actSpawn,
            cc.CallFunc:create(
                function()
                    XH.lobby:getModule("Gold"):reqTaskGetAward("goldRoom")
                end
            )
        )
        self._goldRoot:runAction(sequenceAction)
    end
end

--关闭动画
function GoldView:moveOutAnimation()
    if self._goldRoot then
        local actionTime = 0.25
        self._goldRoot:stopAllActions()
        local targetPosX = display.width * 3 / 2
        local targetPosY = self._goldRoot:getPositionY()
        self._goldRoot:runAction(cc.Spawn:create(cc.FadeOut:create(actionTime), cc.MoveTo:create(actionTime, cc.p(targetPosX, targetPosY))))
    end
end

function GoldView:isNeedHotUpdata(GameID, func)
    if XH.lobby:getModule("Lobby"):isNeedHotUpdata(GameID) then
        XH.lobby:getModule("ToponAct"):setHotUpdata(true)
        XH.TipTool.hideLoading()
        lobby.gameHotUpdateSuccessCallBack = func
    else
        func()
    end
end

--倒计时
function GoldView:runCountDownAcitvity(node, leftTime)
    local iconNode = node:getChildByName(KW_IMG_ACTIVITY)
    local bgNode = node:getChildByName(KW_IMG_LEFT_TIME)
    local timeNode = bgNode:getChildByName(KW_TEXT_LFETTIME)
    timeNode:stopAllActions()
    timeNode:runAction(
        cc.RepeatForever:create(
            cc.Sequence:create(
                cc.CallFunc:create(
                    function()
                        leftTime = leftTime - 1
                        if leftTime >= 0 then
                            local d = math.floor(leftTime / 86400)
                            local h = math.floor((leftTime - d * 86400) / 3600)
                            local m = math.floor((leftTime - d * 86400 - h * 3600) / 60)
                            local strTime = string.format("%d天%02d时", d, h)
                            if d == 0 then
                                strTime = string.format("%02d时%02d分", h, m)
                            end
                            timeNode:setString(strTime)
                        else
                            bgNode:setVisible(false)
                            iconNode:setVisible(false)
                            timeNode:stopAllActions()
                        end
                    end
                ),
                cc.DelayTime:create(1)
            )
        )
    )
end

--是否是限时活动游戏
function GoldView:isActivityGame(gameid)
    local boxActivityGame = XH.areaData:getGoldActivityGame()
    local startTime, endTime = XH.areaData:getActivityTime()
    --活动是否开始
    if startTime > self._timeStamp or endTime < self._timeStamp then
        return false
    end
    for _, value in pairs(boxActivityGame) do
        if gameid == value then
            return true
        end
    end
    return false
end

--请求系统时间
function GoldView:onSystemTime(event)
    local timestamp = event.data
    if timestamp ~= nil then
        self._timeStamp = timestamp
        local _, endTime = XH.areaData:getActivityTime()
        local leftTime = endTime - self._timeStamp
        self:refreshActivityIcon(leftTime)
    end
end

--刷新活动角标
function GoldView:refreshActivityIcon(leftTime)
    for _, node in pairs(self._gameListView:getChildren()) do
        --判断是否是活动游戏
        local gameid = node:getTag()
        if self:isActivityGame(gameid) then
            XH.UITool.setVisible(node, KW_IMG_ACTIVITY, true)
            XH.UITool.loadTexture(node, KW_IMG_ACTIVITY, "gold_status_limit_logo.png", ccui.TextureResType.plistType)
            XH.UITool.setVisible(node, KW_IMG_LEFT_TIME, true)
            self:runCountDownAcitvity(node, leftTime)
        else
            XH.UITool.setVisible(node, KW_IMG_LEFT_TIME, false)
            XH.UITool.setVisible(node, KW_IMG_ACTIVITY, false)
        end
    end
end

function GoldView:adaptForLiuHai()
    if not self.hasInit then
        self.hasInit = true
        XH.UITool.adaptForLiuHai({self.KW_UI_BACK, self.KW_SCROLL_ANI:getParent()})
    end
end

function GoldView:onReqGoldPlayerCount(event)
    if not event.data or #event.data == 0 then
        return
    end
    self:performWithDelay(
    function()
        XH.goldConfigManager:reqRoomPlayerCount(event.data)
    end ,
    0.06
    )
end

function GoldView:isShowNewLevelUpFlag(gameID, playerCount)
    if gameID == 30117 and playerCount == 4 then
        return true
    end
    return false
end

function GoldView:onRespGoldPlayerCount(event)
    if event.level ~= 0 then
        return
    end
    for _, node in pairs(self._gameListView:getChildren()) do
        --判断是否是当前游戏的活动
        local gameid = node:getTag()
        if event.gameID == gameid and event.data[gameid] and event.data[gameid].level and event.data[gameid].level[0] then
            local data = event.data[gameid]
            XH.UITool.setVisible(node, "_KW_GAME_PLAYER_COUNT_BG", true)
            XH.UITool.setText(node, "_KW_LABEL_PLAYER_COUNT", data.level[0])
            return
        end
    end
end

function GoldView:onRefreshUI(event)
    self:performWithDelay(
    function()
        self:initGameListView()
        XH.lobby:getModule("Gold"):RequestSysTime()
    end ,
    0.06
    )
end

function GoldView:onEventShowLeisureEntrance(event)
    self:onRefreshUI()
end

-- 分享领取钻石成功提示
function GoldView:onEventShareGiveGold(event)
    local clientAward = {}
    local goldCount = 3000
    local allCfg = XH.lobby:getModule("Configuration"):getConfigJsonData(ConfigurationDefine.Config.ALL, "AllConfig")
    if allCfg and allCfg.BreakShareGold then
        goldCount = allCfg.BreakShareGold
    end
    clientAward[1] = {}
    clientAward[1].url = "https://img1-cdn.bianfeng.com/uploads/materials/images/202011/6B267621509008RH.png"
    clientAward[1].cnt = goldCount
    clientAward[1].desc = goldCount .. "金币"
    clientAward[1].propid = 0
    XH.viewManager:openView("CommonRewardView", nil, clientAward, false)
end

return GoldView
�T  