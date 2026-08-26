local CreateBoxRoomView = class("CreateBoxRoomView",XH.ViewBase)
local FourToTwoConfig = require("lobby.Config.FourToTwoActConfig")
local MutiVersionGameConfig =  require("lobby.Config.MutiVersionGameConfig")
local CreateBoxRoomConfig = require("lobby.Modules.CreateBoxRoom.Config")
local Mark = require("lobby.Modules.Mark.View")
local GameNewTypeConfiger = require("app.Config.Parse.GameNewTypeConfiger")
local PromoteConfig = require("lobby.Modules.Promote.Config")

local KW_TEXT_MODEl = "KW_TEXT_MODEl"
local KW_IMG_ACTIVITY = "KW_IMG_ACTIVITY"
local KW_IMG_NEW_PLAY_TYPE = "KW_IMG_NEW_PLAY_TYPE"
local KW_IMG_NEW_PROMOTE = "KW_IMG_NEW_PROMOTE"
local KW_IMG_LEFT_TIME = "KW_IMG_LEFT_TIME"
local KW_TEXT_LFETTIME = "KW_TEXT_LFETTIME"
local KW_OPTION_ITEM_TEXT = "KW_OPTION_ITEM_TEXT"
local KW_ITEM_TEXT = "KW_ITEM_TEXT"
local KW_PANEL_TIPS = "KW_PANEL_TIPS"
local KW_PIC_TIPS_PAOPAO = "KW_PIC_TIPS_PAOPAO"
local KW_TEXT_TIPS = "KW_TEXT_TIPS" 

local KW_PLAYMODE_STATUS = "KW_PLAYMODE_STATUS"
local KW_PLAYMODE_NAME = "KW_PLAYMODE_NAME"

local TabTextDefaultColor = cc.c3b(255,251,205)
local TabTextSelectedColor = cc.c3b(163, 111, 72)

--一行不同数量选项时的按钮间隔
local buttonPosition = {
    {230},
    {230, 675},
    {230, 675, 1120},
    {230, 527, 824, 1120},
    {230, 452.5, 675, 897.5, 1120}
}

--单选节点保存
local radioTable = {}
--复选节点保存
local checkBoxTable = {}
--大类节点保存
local categorieTable = {}
--有联动控制属性的节点保存
local linkageLevelTable = {}
--被联动需要隐藏的节点保存
local hideTable = {}
--被联动需要隐藏且需要自动选择其他节点的节点保存
local hideSelectTable = {}
--被联动需要禁用的节点保存
local prohibitTable = {}
--被联动需要禁用的节点保存(且至少需要两个联动值同时存在时才生效)
local prohibitMeanwhileTable = {}
--被联动需要禁用且需要自动选择其他节点的节点保存
local prohibitSelectTable = {}
--被联动需要禁用且同时选择的节点保存
local prohibitAndSelectTable = {}
--被联动需要显示的节点保存
local showTable = {}
--被联动需要选中的节点保存
local linkSelectedTable = {}
--被联动需要取消选中的节点保存
local linkUnSelectedTable = {}
--未选中时才有的联动值
local unSelectlinkageLevelTable = {}
--被联动需要禁用的节点保存(联动级别大于普通的禁用)
local linkProhibitTable = {}
--局数选项节点保存（也可保存用于计算消耗卡数的其他节点）
local playCountTable = {}
--消耗类型选项节点保存（用于计算消耗卡数，房主和aa）
local costTypeTable = {}
--gamerule中必须存在的节点规则（例如衢州麻将的局麻）
local mustExistRuleTable = {}
--有特殊条件的节点保存（比如不是按照局数的）
local conditionTable = {}
--需要在加载完选项后更新联动的联动值(保存当前所选所有按钮联动值方便联动控制)
local linkageLevelValue = {}

function CreateBoxRoomView:getCSBPath()
    return "cocosStudio/hall/CSB/createboxroom/CreateBoxRoomDynamic.csb"
end

function CreateBoxRoomView:getBindingInfo()
    return {
        ["_KW_BTN_BACK"] = {varName="_btnClose",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventClose"},
        ["_KW_LISTVIEW_CHOOSE_GAME"] = { varName = "_listViewGames" },
        ["_KW_PANEL_GAME_RULE_DYNAMIC"] = { varName = "_gameRule" },
        ["_KW_BTN_MODEl"] = { varName = "_btnModel", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventChooseGameDynamic" },
        ["_KW_RADIO_ITEM"] = { varName = "_radioItem" },
        ["_KW_CHECK_BOX_ITEM"] = { varName = "_checkBoxItem" },
        ["_KW_TEXT_COST_INFO"] = { varName = "_costTextInfo" },
        ["_KW_PANEL_OPTIONS_ITEM"] = { varName = "_optionItem" },
        ["_KW_TEXT_FREE_CREATE_CNT"] = { varName = "_freeCreateCnt" },
        ["_KW_IMG_FREE_CREATE_CLOCK"] = { varName = "_imgFreeCreateClock" },
        ["_KW_TEXT_FREE_CREATE_TIME"] = { varName = "_freeCreateTime" },
        ["_KW_PANEL_CREATE_FREE"] = { varName = "_panelCreateFree" },
        ["_KW_TEXT_COST_INFO_NORMAL"] = { varName = "_costTextInfoNormal" },
        ["_KW_PANEL_LINE"] = { varName = "_panelLine" },
        ["_KW_BTN_CREATE_BOX_ROOM"] = {varName="_btnCreateBoxRoom",onTouchEnded = "onTouchEventCreateBoxRoom"},
        ["_KW_BTN_CREATE_BOX_ROOM_FREE"] = {varName="_btnCreateBoxRoomFree",onTouchEnded = "onTouchEventCreateBoxRoomFree"},
        ["_KW_BTN_CREATE_BOX_ROOM_NORMAL"] = { varName = "_btnCreateBoxRoomNormal", onTouchEnded = "onTouchEventCreateBoxRoom" },
        --desc:扩展新手引导内容 dev:qiuzhong date:20210315
        ["_KW_NEW_USER_GUIDE"] = { varName = "_newUserGuidePanel"},
        ["_KW_GUIDE_STEP_1"] = {varName = "_guideLayerStep1"},
        ["_KW_GUIDE_STEP_2"] = {varName ="_guideLayerStep2"},
        ["_KW_UI_JUMP_OVER_BTN"] = { varName = "KW_UI_JUMP_OVER_BTN",type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KW_UI_JUMP_OVER_BTN" },
        ["_KW_UPGRADE_TEXT"] = { varName = "_upgradeLabel"},

        ["_KW_FOUR_TO_TWO_TIP"] = { varName = "_tipFourToTwo"},
        ["_KW_FOUR_TO_TWO_TIP_2"] = { varName = "_tipFourToTwo2"},
        --合规部分游戏选择玩法
        ["_KW_BTN_PLAYMODE_AREA"] = {varName = "_btnPlayModeArea",type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventPlayModeAreaChange" },
        ["_KW_PANEL_SELECT_PLAYMODE_AREA"] = {varName = "_panelPlayModeArea"},
        ["_KW_PANEL_DEFAULT_ITEM"] = {varName = "_panelDefaultItem"},
        ["_KW_NODE_MODULE_1"] = {  varName = "_nodeItem1" },
        ["_KW_NODE_MODULE_2"] = {  varName = "_nodeItem2" },
        ["_KW_NODE_MODULE_3"] = {  varName = "_nodeItem3" },
        ["_KW_NODE_MODULE_4"] = {  varName = "_nodeItem4" },
        ["_KW_NODE_MODULE_5"] = {  varName = "_nodeItem5" },
        ["_KW_NODE_MODULE_6"] = {  varName = "_nodeItem6" },
        ["_KW_NODE_MODULE_7"] = {  varName = "_nodeItem7" },
        ["_KW_NODE_MODULE_8"] = {  varName = "_nodeItem8" },
        ["_KW_NODE_MODULE_9"] = {  varName = "_nodeItem9" },
        ["_KW_PLAYMODE_AREA_NOW"] = { varName = "_textPlayModeName"},
        ["_KW_BTN_PLAYTYPE_CLOSE"] = { varName = "_btnPlayTypeClose",type = XH.UI_TYPE.BUTTON , onTouchEnded = "onTouchEventPlayTypeClose" },
        ["_KW_BTN_PLAYTYPE_SURE"] = { varName = "_btnPlayTypeSave",type = XH.UI_TYPE.BUTTON , onTouchEnded = "onTouchEventPlayTypeSave" },
        ["_KW_LIMIT_TIME_ACT"] = { varName = "_imgLimitAct"},
        ["_KW_IMG_AREA_COST_HUA"] = { varName = "_imgAreaCostHua"},
        ["_KW_AREA_COST_TEXT"] = { varName = "_imgAreaCostText"},

        ["_KW_NEW_PALY_TYPE_INTERDUCE"] = { varName = "_btnNewPlayTypeInterduce", type = XH.UI_TYPE.BUTTON , onTouchEnded = "onTouchEventNewPlayTypeInterduce" },
        ["_KW_CREATE_TIPS"] = { varName = "_createTips"},
        ["_KW_TXT_CREATE_TIPS"] = { varName = "_createTipsStr"},
        ["_KW_TIPS_MODEL"] = { varName = "_tipsModel" },
        ["_KW_PROMOTE_TIPS"] = { varName = "_promoteTips" },
        ["_KW_ACT_FREE_TIPS"] = { varName = "_KW_ACT_FREE_TIPS" },
        ["_KW_PANEL_TIP_1"] = { varName = "_KW_PANEL_TIP_1" },
        ["_KW_PANEL_TIP_2"] = { varName = "_KW_PANEL_TIP_2" },
        ["_KW_ACT_FREE_ALL"] = { varName = "_KW_ACT_FREE_ALL" },
        ["_KW_ACT_FREE_NEXT"] = { varName = "_KW_ACT_FREE_NEXT" },
        ["_KW_ACT_FREE_NEXT_1"] = { varName = "_KW_ACT_FREE_NEXT_1" },
        ["_KW_ACT_FREE_NEXT_2"] = { varName = "_KW_ACT_FREE_NEXT_2" },
    }
end

function CreateBoxRoomView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("CreateBoxRoom"), eventKeyName = "EVENT_HIDE_CREATEBOXROOM", callBack = "close" },
        { module = XH.lobby:getModule("CreateBoxRoom"), eventKeyName = "EVENT_SYSTEM_TIME_SUCCESS", callBack = "onGetSystemTimeSuccess" },
        { module = XH.lobby:getModule("LimitFreeAct"), eventKeyName = "EVENT_FLUSH_FREE_ACT_INFO", callBack = "onEventFlushLimitFreeAct" },
    }
end

function CreateBoxRoomView:ctor(param)
    param = param or {}
    CreateBoxRoomView.super.ctor(self,param)
    
    -- 推广活动默认勾选
    self._isPromote = param.isPromote
    self._gameID = 0
    self._firstSelectedGameId = param.gameid or 0
    self._isGuideState = param.isGuideState or false
    self._chooseTwoPlayer = param.chooseTwoPlayer or false

    self._modeItems = {}
    self._tmpGameID = nil
    self._mutiGameTable = nil
    self._tmpGameName = nil
    self._tmpLobbyID = nil
    self._tmpMutiVersionGame = nil
    self._isMutiVersionGame = false

    local info = XH.lobby:getModule("LimitFreeAct"):getActConfig()
    if info and not XH.lobby:getModule("LimitFreeAct"):isActEnd() then
        local size = self._gameRule:getContentSize()
        self._gameRule:setContentSize(size.width, size.height - 50)
        local posX, posY = self._gameRule:getPosition()
        self._gameRule:setPosition(posX, posY - 50)
    end
    
    XH.playerData:setIsBoxRoomGuide(self._isGuideState)
    self._notSetDefaultSelectGame = self._isGuideState
    XH.lobby:getModule("CreateBoxRoom"):onCreateView()
    self:initUI()

    local tmpData = {}
    tmpData.userid = XH.playerData:getNumberID()
    tmpData.ispopviewopen = self._chooseTwoPlayer
    XH.throwDataManager:throwData(XH.ThrowDataDefine.LOBBY_CREATE_BOX_LAYER_OPEN, tmpData) 
end

function CreateBoxRoomView:initUI()
    self._radioItem:addEventListener(handler(self, self.onTouchEventRadio))
    self._checkBoxItem:addEventListener(handler(self, self.onTouchEventCheckBox))

    XH.UITool.addTouchEventListener(self._radioItem, KW_OPTION_ITEM_TEXT, handler(self, self.onTouchRadioOptionText))
    XH.UITool.addTouchEventListener(self._checkBoxItem, KW_OPTION_ITEM_TEXT, handler(self, self.onTouchCheckBoxOptionText))
    
    self._listViewGames:setScrollBarEnabled(false)
    self._gameRule:setScrollBarEnabled(false)

    local newTypeDefultGameID = GameNewTypeConfiger:getDefultGameIDByLobbyID(XH.areaData:getLobbyID())
    if newTypeDefultGameID ~= 0 and self._firstSelectedGameId == 0 then
        if not XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEW_TYPE_FISRT_OPEN_CREATE_LAYER, false, {key = newTypeDefultGameID .. XH.playerData:getNumberID()}) then
            self._firstSelectedGameId = newTypeDefultGameID
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_NEW_TYPE_FISRT_OPEN_CREATE_LAYER, true, {key = newTypeDefultGameID .. XH.playerData:getNumberID()})
        end
    end
    
    self:initGameList()
    -- 刘海屏适配
    self:adaptForLiuHai()
    self:initConsumeTips()
    self:onEventFlushLimitFreeAct()
    --新手引导状态
    if self._isGuideState then
        self._newUserGuidePanel:setVisible(true)
    else
        if newTypeDefultGameID ~= 0 and not XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEW_TYPE_SHOW_EXTRA_RULE, false, {key = newTypeDefultGameID .. XH.playerData:getNumberID()}) then
            if next(GameNewTypeConfiger:getExtraRuleByGameID(newTypeDefultGameID)) then
                XH.viewManager:openView("NewPlayTypeExtraRuleView", nil, newTypeDefultGameID)
                XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_NEW_TYPE_SHOW_EXTRA_RULE, true, {key = newTypeDefultGameID .. XH.playerData:getNumberID()})
            end
        end
    end
end

-- function CreateBoxRoomView:getTabBtnGameNameImg(gameID)
--     if CreateBoxRoomConfig.TabListNames[gameID] then
--         return CreateBoxRoomConfig.TabListNames[gameID].NameImg
--     end
-- end

-- function CreateBoxRoomView:setTabBtnGameName(tabBtn, gameName, gameID)
--     XH.UITool.setText(tabBtn, KW_TEXT_MODEl, gameName)
--     local nameImg = self:getTabBtnGameNameImg(gameID)
--     if nameImg and nameImg ~= "" then
--         XH.UITool.ignoreContentAdaptWithSize(tabBtn,"KW_IMG_MODEl",true)
--         XH.UITool.loadTexture(tabBtn, "KW_IMG_MODEl", nameImg, ccui.TextureResType.plistType)
--         XH.UITool.setColor(tabBtn, "KW_IMG_MODEl", TabTextDefaultColor)
--         XH.UITool.setVisible(tabBtn, "KW_IMG_MODEl", true)
--         XH.UITool.setVisible(tabBtn, KW_TEXT_MODEl, false)
--     else
--         XH.UITool.setVisible(tabBtn, "KW_IMG_MODEl", false)
--         XH.UITool.setVisible(tabBtn, KW_TEXT_MODEl, true)
--     end
-- end

function CreateBoxRoomView:createGameListBtn(gameID, gameName)
    local newBtn = self._btnModel:clone()
    XH.UITool.setText(newBtn, KW_TEXT_MODEl, gameName)
    -- self:setTabBtnGameName(newBtn, gameName, gameID)
    newBtn:setTag(gameID)
    newBtn:setName(gameID)
    --判断是否是活动游戏
    if XH.lobby:getModule("CreateBoxRoom"):isActivityGame(gameID) then
        XH.UITool.setVisible(newBtn, KW_IMG_ACTIVITY, true)
        XH.UITool.setVisible(newBtn, KW_IMG_LEFT_TIME, true)
        XH.UITool.loadTexture(newBtn, KW_IMG_ACTIVITY, "lt_icon.png", ccui.TextureResType.plistType)
        self:runCountDownAcitvity(newBtn, XH.lobby:getModule("CreateBoxRoom"):getActivityLeftTime())
    end
    --判断是否为标记游戏
    if XH.lobby:getModule("Mark"):isShowMarkGame(gameID) then
        local markText = XH.lobby:getModule("Mark"):getMarkTextByGameID(gameID)
        XH.UITool.setText(newBtn, KW_TEXT_MODEl, markText.gameName) 
        local newMark = Mark.new(gameID)
        newBtn:addChild(newMark)
        newMark:setPosition(cc.p(0 , 0))
    end
    if self:isShowNewPlayType(gameID) then
        newBtn:loadTextures("com_btn_label_normal2.png","com_btn_label_normal2.png","com_btn_label_select.png",ccui.TextureResType.plistType)
        -- 推广活动默认勾选
        if PromoteConfig[XH.areaData:getLobbyID()] ~= nil and PromoteConfig[XH.areaData:getLobbyID()].GAMEID == gameID then
            XH.UITool.setVisible(newBtn, KW_IMG_NEW_PROMOTE, true)
        else
            XH.UITool.setVisible(newBtn, KW_IMG_NEW_PLAY_TYPE, true)
        end
    end
    return newBtn
end

function CreateBoxRoomView:isShowNewPlayType(gameID)
    -- if XH.areaData:getLobbyID() == XH.LOBBY_ID.LISHUI then
    --     return false
    -- end
    return GameNewTypeConfiger:getIsNewPlayTypGame(gameID)
end

-- 初始化左边游戏列表
function CreateBoxRoomView:initGameList()
    if not self._btnModel then
        return
    end
    local gameListData = XH.lobby:getModule("CreateBoxRoom"):getGameListData()

    XH.playerData:screenGameList(gameListData,0)

    if #gameListData == 0 then
        self._upgradeLabel:setVisible(true)
        self._btnCreateBoxRoom:setVisible(false)
    end

    local firstGameId = 0
    for _, singleGameData in ipairs(gameListData) do
        local newItem = self:createGameListBtn(singleGameData.gameID, singleGameData.gameName)
        self._listViewGames:addChild(newItem)
        if firstGameId == 0 then
            firstGameId = singleGameData.gameID
        end
    end
    self:setDefaultSelectGameDynamic(firstGameId)
    self:checkSelectGameDynamicLegal()
end

local ADAPT_NODE_LIST = {"_KW_LISTVIEW_CHOOSE_GAME", "KW_IMG_SPLIT_LINE"}
function CreateBoxRoomView:adaptForLiuHai()
    local nodeList = {}
    for _,name in pairs(ADAPT_NODE_LIST) do
        local tmpNode = XH.UITool.seekNodeByName(self, name)
        nodeList[#nodeList + 1] = tmpNode
    end
    XH.UITool.adaptForLiuHai(nodeList)
end

function CreateBoxRoomView:checkFreeCreateProp()
    local KW_TASK_ID = {
        [XH.LOBBY_ID.QUZHOU] = 9906
    }
    if XH.playerData:getIsShowFreeQuan() then
        local taskID = KW_TASK_ID[XH.areaData:getLobbyID()]
        local cellTaskPercent = require("app.Req.Task.ReqTaskPercent")
        local TaskPercent = cellTaskPercent:new()
        TaskPercent:addReqCallBack(self, self.onReqTaskFreePropCallBack)
        local srsGroupID = XH.areaData:getSrsGroupID()
        TaskPercent:start(XH.playerData:getNumberID(),0,XH.TaskProtocol.ReqTaskProtocol.processid,taskID,srsGroupID,XH.KW_CONFIG_LOGIC_TIME_OUT)
    end
end

--请求免费券道具回调
function CreateBoxRoomView:onReqTaskFreePropCallBack(cell, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        local respData = json.decode(data.acData)
        if not respData.ret or respData.ret ~= 0 then
            return
        end
        local freeLeftTime = respData.time or 0
        local freeCnt = respData.cnt or 0
        -- 下架衢州VIP，保留已有权益
        if freeLeftTime == 0 and freeCnt == 0 then
            return 
        end

        self._freeCnt = freeCnt
        self._freeLeftTime = freeLeftTime
        self:runCountDown()
        self._btnCreateBoxRoom:setVisible(false)
        self._panelCreateFree:setVisible(true)
        --免费建房动画效果
        local params = {
		    path = "animation/Lobby/Base/create_box_room/",
		    ske = "zjb_mfjf_ani_ske.json",
		    tex = "zjb_mfjf_ani_tex.json",
		    armatureName = "Armature",
		    dragonBonesName = "zjb_mfjf_ani",
		    animationName = "mfjf_ani"
	    }
	    local ani = display.playDargonBonesAnimByTimes(params, 0)
        if ani then
            self._btnCreateBoxRoomFree:addChild(ani)
            local size = self._btnCreateBoxRoomFree:getContentSize()
            ani:setPosition(size.width / 2, size.height / 2)
            if self._imgFreeCreateClock then
                self._imgFreeCreateClock:setZOrder(10)
                self._imgFreeCreateClock:setPositionX(30)
            end
        end
    end
end

--倒计时
function CreateBoxRoomView:runCountDown()
    if self._freeCnt == nil or self._freeLeftTime == nil then
        return
    end
    self._imgFreeCreateClock:setVisible(true)
    self._freeCreateTime:stopAllActions()
    self._freeCreateTime:runAction(cc.RepeatForever:create(cc.Sequence:create(cc.DelayTime:create(1),
    cc.CallFunc:create(function()
        self._freeLeftTime = self._freeLeftTime - 1
        if self._freeLeftTime >= 0 then
            local d = math.floor(self._freeLeftTime / 86400)
            local h = math.floor((self._freeLeftTime - d * 86400) / 3600)
            local m = math.floor((self._freeLeftTime - d * 86400 - h * 3600) / 60)
            local strTime = string.format("剩%d天%02d：%02d", d, h, m)
            self._freeCreateTime:setString(strTime)
        else
            self._freeCreateTime:stopAllActions()
        end
    end))))
end

--根据key值大小顺序遍历table表
local  pairsByKeys = function(t)
    if t == nil then t = {} end
    local a = {}
    for n in pairs(t) do
        a[#a+1] = n
    end
    table.sort(a, function(x, y)
            return tonumber(string.match(x, "%d+")) < tonumber(string.match(y, "%d+"))
        end
    )
    -- table.sort(a)
    local i = 0
    return function()
        i = i + 1
        return a[i], t[a[i]]
    end
end

--选定一个游戏，动态加载游戏规则
function CreateBoxRoomView:onTouchEventChooseGameDynamic(send, eventType,isAutoTouch)
    local gameID = send:getTag()
    if self._isGuideState and not isAutoTouch then 
        if self._guideLayerStep2:isVisible() then
            self._guideLayerStep2:setVisible(false)
        else
            if self._guideLayerStep1:isVisible() then
                self._guideLayerStep2:setVisible(true)
            end
        end
        self._guideLayerStep1:setVisible(false)
    end
    if self._notSetDefaultSelectGame then
        if not isAutoTouch then
            self._notSetDefaultSelectGame = false
        else 
            local btns = send:getParent():getChildren()
            for _, btn in pairs(btns) do
                XH.UITool.setTextColor(btn, KW_TEXT_MODEl, TabTextSelectedColor)
                XH.UITool.setFontSize(btn, KW_TEXT_MODEl, 54)
                -- XH.UITool.setColor(btn, "KW_IMG_MODEl", TabTextSelectedColor)
            end
            return
        end
    end
    self._btnPlayModeArea:setVisible(false)
    if gameID == self._gameID then
        return
    end
    self._mutiGameTable = MutiVersionGameConfig.GameConfig[gameID]
    self._tmpLobbyID = nil
    self._isMutiVersionGame = false
    if self._mutiGameTable then
        local loadGameId = self._tmpGameID or gameID
        local mutiGame = self._mutiGameTable[loadGameId]
        if mutiGame then
            self._btnPlayModeArea:setTitleText("玩法："..mutiGame.areaName)
            self._tmpLobbyID = mutiGame.lobbyid
            gameID = mutiGame.gameid
            self._isMutiVersionGame = mutiGame.isMutiVersionGame
        end
        self._btnPlayModeArea:setVisible(true)
    end
    local btns = send:getParent():getChildren()
    for _, btn in pairs(btns) do
        local isSelect = btn == send
        local fontSize = isSelect and 60 or 54
        local color = isSelect and TabTextDefaultColor or TabTextSelectedColor
        XH.UITool.setTextColor(btn, KW_TEXT_MODEl, color)
        XH.UITool.setFontSize(btn, KW_TEXT_MODEl, fontSize)
        -- XH.UITool.setColor(btn, "KW_IMG_MODEl", color)
        btn:setEnabled(not isSelect)
    end

    self._gameID = gameID
    self:initPromoteTips(self._gameID)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_CHOOSE_GAME_ID, tonumber(gameID))

    self._categoriesIndex = nil
    --根据游戏id动态加载规则选项
    self:loadGameRuleDynamic(gameID,self._tmpLobbyID)
    self:showFreeLine(gameID)    

    self:initFourToTwo(gameID)
    self:initNewPlayTypeUI(gameID)

    local dataEx = {}
    dataEx.numid = XH.playerData:getNumberID()
    dataEx.gameid = gameID
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.Box_CreateRoom_Btn_Choose, dataEx)
end

--初始化保存的节点表和联动值等
function CreateBoxRoomView:clearNodetable()
    radioTable = {}
    checkBoxTable = {}
    categorieTable = {}
    linkageLevelTable = {}
    hideTable = {}
    hideSelectTable = {}
    prohibitTable = {}
    prohibitMeanwhileTable = {}
    prohibitSelectTable = {}
    prohibitAndSelectTable = {}
    showTable = {}
    linkSelectedTable = {}
    linkUnSelectedTable = {}
    unSelectlinkageLevelTable = {}
    linkProhibitTable = {}
    playCountTable = {}
    costTypeTable = {}
    mustExistRuleTable = {}
    conditionTable = {}
    linkageLevelValue = {}
end

function CreateBoxRoomView:getXmlKey(xmlString)
    local tableKey = {}
    local head = "</".."(.-)"..">"
    local insertNum = 1
    while(true) do
        local num0, num1, tmpItem = string.find(xmlString, head, insertNum)
        insertNum = num1
        if num0 == nil and num1 == nil or tmpItem == "userDefaultRoot" then
            break
        end
        table.insert(tableKey, tmpItem)
    end
    return tableKey
end

--根据游戏id动态加载规则选项
function CreateBoxRoomView:loadGameRuleDynamic(gameid,lobbyid)
    if self._gameRule and self._optionItem then
        self._gameRule:removeAllChildren()
        self:clearNodetable()
        local gameRuleData = XH.ReadGameListAndRuleConfig.loadBoxGameRule(lobbyid or XH.areaData:getLobbyID(), gameid)
		if gameRuleData == nil then
			return
        end
        --当前游戏是否弃用天枢的T1
        self._isT1Disuse = false
        if gameRuleData["isT1Disuse"] then
            self._isT1Disuse = true
        end
        --当前游戏天枢的T几是否是四个为一组的使用
        self._isFourAset = false
        if gameRuleData["isFourAset"] then
            self._isFourAset = true
        end
        --当前游戏消耗和人数相关时
        self._isCostRelativePlayers = false
        if gameRuleData["isCostRelativePlayers"] then
            self._isCostRelativePlayers = true
        end
        --当前后台t几是乱七八糟不按常理的则需配置一下字段，后续直接读取局数下的t值
        self._isTChaos = false
        if gameRuleData["isTChaos"] then
            self._isTChaos = true
        end
        --当前游戏规则配置版本号（默认0）
        self._gameRuleVersion = 0
        if gameRuleData["gameRuleVersion"] then
            self._gameRuleVersion = gameRuleData["gameRuleVersion"]
        end
        local gameRuleVersionCur = XH.userDefault:getValue(XH.userDefault.KEY_ID.GAMERULE_VERSION, 0, {gameID = self._gameID}) 
        
        if tonumber(self._gameRuleVersion) ~= gameRuleVersionCur then
            local userDefaultXMLString  = cc.FileUtils:getInstance():getStringFromFile(cc.UserDefault:getXMLFilePath())
            local tableKey = self:getXmlKey(userDefaultXMLString)
            for Index = 1, #tableKey do
                local pos1
                local pos2
                pos1, pos2 = string.find(tableKey[Index], "gameRule"..self._gameID)
                if pos1 and pos2 then
                    cc.UserDefault:getInstance():deleteValueForKey(tableKey[Index])
                end
            end
        end
        --缓存当前游戏规则配置版本号（默认0）
        XH.userDefault:setValue(XH.userDefault.KEY_ID.GAMERULE_VERSION, tonumber(self._gameRuleVersion), {gameID = self._gameID}) 

        --该显示的所有选项表
        local showOptiondsTable
        --选择大类
        if gameRuleData["categories"] then
            if self._categoriesIndex then
                showOptiondsTable = gameRuleData["categories"][self._categoriesIndex]
            else
                showOptiondsTable = gameRuleData["categories"][gameRuleData["defaultchoose"]]
                local savedCategoriesIndex = XH.userDefault:getValue(XH.userDefault.KEY_ID.GAMERULE_CATEGORIE_SELECT,0,{gameID = self._gameID})
                if savedCategoriesIndex ~= 0 then
                    self._categoriesIndex = savedCategoriesIndex
                    showOptiondsTable = gameRuleData["categories"][savedCategoriesIndex]
                end
            end
        else
            showOptiondsTable = gameRuleData
        end
        
        for keyName, playTypeData in pairsByKeys(showOptiondsTable) do
            if playTypeData["type"] == "radio" then
                --同个类型的单选框存在同一个table中，即人数的节点存在同一个
                radioTable[#radioTable + 1] = {}
            end
            for line, _ in pairsByKeys(playTypeData["ctrls"]) do
                local newOptionItem
                if playTypeData["type"] == "radio" then
                    newOptionItem = self._optionItem:clone()
                    if playTypeData["nodeName"] == "playCount" or playTypeData["nodeName"] == "playerCount" then
                        newOptionItem:setName(playTypeData["nodeName"]..line)
                    else
                        newOptionItem:setName(playTypeData["nodeName"])
                    end
                    self._gameRule:addChild(newOptionItem)
                    self:setOptions(playTypeData, newOptionItem, line, keyName)
                elseif playTypeData["type"] == "checkbox" then
                    newOptionItem = self._optionItem:clone()
                    if playTypeData["nodeName"] == "playCount" or playTypeData["nodeName"] == "playerCount" then
                        newOptionItem:setName(playTypeData["nodeName"]..line)
                    else
                        newOptionItem:setName(playTypeData["nodeName"])
                    end
                    self._gameRule:addChild(newOptionItem)
                    self:setOptions(playTypeData, newOptionItem, line, keyName)
                end
                if playTypeData["text"] == "" then
                    XH.UITool.setVisible(newOptionItem, KW_ITEM_TEXT, false)
                else
                    XH.UITool.setText(newOptionItem, KW_ITEM_TEXT, playTypeData["text"])
                    XH.UITool.setVisible(newOptionItem, KW_ITEM_TEXT, tonumber(line) == 1)
                end
            end
        end
    end
    
    --判断本地是否缓存了按钮选择
    local isCacheGameRule = XH.userDefault:getValue(XH.userDefault.KEY_ID.GAMERULE_CACHE_EXIST, false, { gameID = self._gameID, categoriesIndex = self._categoriesIndex })
    if isCacheGameRule then
        --加载完选项之后更新和联动值相关的节点
        self:updateLinkageNodeBylinkageLevelValue()
    else
        --加载默认选项
        self:setDefaultOptions()
    end
    --记录本地是否缓存了按钮选择
    XH.userDefault:setValue(XH.userDefault.KEY_ID.GAMERULE_CACHE_EXIST, true, { gameID = self._gameID, categoriesIndex = self._categoriesIndex })
    --更新房卡消耗的显示
    self:updateShowCostInfo()
    self:checkRadioLegal()
    --刷新列表容器视图
    self._gameRule:refreshView()
end

--检查单选是否有没有勾选的情况
function CreateBoxRoomView:checkRadioLegal()
    for _, lineTable in pairs(radioTable) do
        local isLineLegal = false
        for _, nodeInfo in pairs(lineTable) do
            if nodeInfo["node"]:isSelected() then
                isLineLegal = true
                break
            end
        end
        if not isLineLegal then
            for i, _ in pairs(lineTable) do
                if lineTable[i] and lineTable[i]["node"] and lineTable[i]["node"]:isBright() then
                    self:onTouchEventRadio(lineTable[i]["node"], ccui.CheckBoxEventType.selected, true)
                    break
                end
            end
        end
    end
end

--动态加载单个选项（单选复选）
function CreateBoxRoomView:setOptions(playTypeData, newOptionItem, line, keyName)
    local optionNode
    if self._radioItem and self._checkBoxItem then
        local lineData = playTypeData["ctrls"][line]
        local tmpCount = #lineData
        for optionIndex = 1, tmpCount do
            if lineData[optionIndex]["text"] == "强制定位" then
                lineData[optionIndex]["text"] = "防作弊"
            end
            if playTypeData["type"] == "radio" then
                optionNode = self._radioItem:clone()
                XH.UITool.setText(optionNode, KW_OPTION_ITEM_TEXT, lineData[optionIndex]["text"])
                --保存单选框节点
                local tmp = {}
                tmp["node"] = optionNode
                tmp["nodeConfig"] = lineData[optionIndex]
                table.insert(radioTable[#radioTable],tmp)

                if lineData[optionIndex]["categorieIndex"] then
                    if categorieTable[#categorieTable + 1] == nil then
                        categorieTable[#categorieTable + 1] = {}
                    end
                    categorieTable[#categorieTable]["node"] = optionNode
                    categorieTable[#categorieTable]["nodeConfig"] = lineData[optionIndex]
                end
            elseif playTypeData["type"] == "checkbox" then
                optionNode = self._checkBoxItem:clone()
                XH.UITool.setText(optionNode, KW_OPTION_ITEM_TEXT, lineData[optionIndex]["text"])
                --保存复选框节点
                if checkBoxTable[#checkBoxTable + 1] == nil then
                    checkBoxTable[#checkBoxTable + 1] = {}
                end
                checkBoxTable[#checkBoxTable]["node"] = optionNode
                checkBoxTable[#checkBoxTable]["nodeConfig"] = lineData[optionIndex]
            end
            --判断是否按钮有提示信息
            if lineData[optionIndex]["haveTips"] then
                XH.UITool.setText(optionNode, KW_TEXT_TIPS, lineData[optionIndex]["haveTips"])
                local optionTextSize = XH.UITool.getContentSize(optionNode, KW_OPTION_ITEM_TEXT)
                local tipTextSize = XH.UITool.getContentSize(optionNode, KW_TEXT_TIPS)
                XH.UITool.setContentSize(optionNode, KW_PIC_TIPS_PAOPAO, tipTextSize.width + 40, tipTextSize.height + 40)
                XH.UITool.setPositionY(optionNode, KW_TEXT_TIPS, (tipTextSize.height + 40) / 2)
                XH.UITool.setVisible(optionNode, KW_PANEL_TIPS, true)
                XH.UITool.setPositionX(optionNode, KW_PANEL_TIPS, optionTextSize.width + 10)
                XH.UITool.addTouchEventListener(optionNode, KW_PANEL_TIPS, handler(self, self.onTouchEventTipsBtn))

                if optionIndex ~= tmpCount then
                    optionNode:setLocalZOrder(tmpCount - optionIndex + 1)
                else
                    if optionIndex >= 3 then
                        local xPosition =  XH.UITool.getPositionX(optionNode, KW_PIC_TIPS_PAOPAO)
                        XH.UITool.setPositionX(optionNode, KW_PIC_TIPS_PAOPAO, xPosition - (tipTextSize.width + 100))
                    end
                end
            end
            if lineData[optionIndex]["tag"] and optionNode then
                cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/Common/Image/common_discount/common_tag.plist")
                self:createFlagAfterNode(optionNode, lineData[optionIndex]["tag"] .. ".png")
            end
            --保存条件，联动和联动相关等需要保存的节点
            self:saveCanChangeNode(lineData, optionIndex, optionNode)

            newOptionItem:addChild(optionNode)
            optionNode:setPosition(cc.p(buttonPosition[tmpCount][optionIndex], 50))
            optionNode:setName(lineData[optionIndex]["nodeName"])
            local isSelected = self:getCacheButtonState(optionNode:getName())
            self:setSelectState(optionNode,isSelected)

            -- if tonumber(string.match(optionNode:getName(), "%d")) == 2 and string.find(optionNode:getName(), "playerCount") then
            --     self:initFourToTwoPlayerBtn(optionNode, tonumber(string.match(keyName, "%d")), lineData[optionIndex]["haveTips"] ~= nil)
            -- end

            -- 推广活动默认勾选
            if self._isPromote and PromoteConfig[XH.areaData:getLobbyID()] ~= nil and PromoteConfig[XH.areaData:getLobbyID()].NODENAME ~= nil then
                local promoteRule = PromoteConfig[XH.areaData:getLobbyID()].NODENAME
                if optionNode:getName() == promoteRule then
                    self:setSelectState(optionNode,true)
                end
            end

            local newPlayTypeNodes = self:getNewPlayTypeNodes()
            if newPlayTypeNodes and next(newPlayTypeNodes) then
                self:initNewPlayTypeNode(optionNode, newPlayTypeNodes)
            end
        end
    end
end

function CreateBoxRoomView:getNewPlayTypeNodes()
    -- if XH.areaData:getLobbyID() == XH.LOBBY_ID.LISHUI then
    --     return nil
    -- end
    return GameNewTypeConfiger:getNewPlayTypeNodeByGameID(self._gameID)
end

--保存条件，联动和联动相关等需要保存的节点
function CreateBoxRoomView:saveCanChangeNode(lineData, optionIndex, optionNode)
    if lineData[optionIndex]["condition"] then
		if conditionTable[#conditionTable + 1] == nil then
            conditionTable[#conditionTable + 1] = {}
        end
        conditionTable[#conditionTable]["node"] = optionNode
		conditionTable[#conditionTable]["nodeConfig"] = lineData[optionIndex]
    end
    if lineData[optionIndex]["mustExistRule"] then
		if mustExistRuleTable[#mustExistRuleTable + 1] == nil then
            mustExistRuleTable[#mustExistRuleTable + 1] = {}
        end
        mustExistRuleTable[#mustExistRuleTable]["node"] = optionNode
		mustExistRuleTable[#mustExistRuleTable]["nodeConfig"] = lineData[optionIndex]
    end
    if lineData[optionIndex]["costType"] then
		if costTypeTable[#costTypeTable + 1] == nil then
            costTypeTable[#costTypeTable + 1] = {}
        end
        costTypeTable[#costTypeTable]["node"] = optionNode
		costTypeTable[#costTypeTable]["nodeConfig"] = lineData[optionIndex]
    end
    if lineData[optionIndex]["allCost"] then
		if playCountTable[#playCountTable + 1] == nil then
            playCountTable[#playCountTable + 1] = {}
        end
        playCountTable[#playCountTable]["node"] = optionNode
		playCountTable[#playCountTable]["nodeConfig"] = lineData[optionIndex]
    end
    if lineData[optionIndex]["linkageLevel"] then
        if linkageLevelTable[lineData[optionIndex]["linkageLevel"]] == nil then
            linkageLevelTable[lineData[optionIndex]["linkageLevel"]] = {}
        end
        table.insert(linkageLevelTable[lineData[optionIndex]["linkageLevel"]],optionNode)
    end
    if lineData[optionIndex]["prohibit"] then
        for _, value in ipairs(lineData[optionIndex]["prohibit"]) do
            if prohibitTable[value] == nil then
                prohibitTable[value] = {}
            end
            table.insert(prohibitTable[value], optionNode)
        end
    end
    if lineData[optionIndex]["prohibitMeanwhile"] then
		if prohibitMeanwhileTable[#prohibitMeanwhileTable + 1] == nil then
            prohibitMeanwhileTable[#prohibitMeanwhileTable + 1] = {}
        end
        prohibitMeanwhileTable[#prohibitMeanwhileTable]["node"] = optionNode
		prohibitMeanwhileTable[#prohibitMeanwhileTable]["nodeConfig"] = lineData[optionIndex]
    end
    if lineData[optionIndex]["prohibitSelect"] then
		if prohibitSelectTable[#prohibitSelectTable + 1] == nil then
            prohibitSelectTable[#prohibitSelectTable + 1] = {}
        end
        prohibitSelectTable[#prohibitSelectTable]["node"] = optionNode
		prohibitSelectTable[#prohibitSelectTable]["nodeConfig"] = lineData[optionIndex]
    end
    if lineData[optionIndex]["prohibitAndSelect"] then
        for _, value in ipairs(lineData[optionIndex]["prohibitAndSelect"]) do
            if prohibitAndSelectTable[value] == nil then
                prohibitAndSelectTable[value] = {}
            end
            table.insert(prohibitAndSelectTable[value], optionNode)
        end
    end
    if lineData[optionIndex]["hideSelect"] then
		if hideSelectTable[#hideSelectTable + 1] == nil then
            hideSelectTable[#hideSelectTable + 1] = {}
        end
        hideSelectTable[#hideSelectTable]["node"] = optionNode
		hideSelectTable[#hideSelectTable]["nodeConfig"] = lineData[optionIndex]
    end
    if lineData[optionIndex]["hide"] then
        for _, value in ipairs(lineData[optionIndex]["hide"]) do
            if hideTable[value] == nil then
                hideTable[value] = {}
            end
            table.insert(hideTable[value], optionNode)
        end
    end
    if lineData[optionIndex]["show"] then
        for _, value in ipairs(lineData[optionIndex]["show"]) do
            if showTable[value] == nil then
                showTable[value] = {}
            end
            table.insert(showTable[value], optionNode)
        end
    end
    if lineData[optionIndex]["linkSelect"] then
        for _, value in ipairs(lineData[optionIndex]["linkSelect"]) do
            if linkSelectedTable[value] == nil then
                linkSelectedTable[value] = {}
            end
            table.insert(linkSelectedTable[value], optionNode)
        end
    end
    if lineData[optionIndex]["linkUnSelect"] then
        for _, value in ipairs(lineData[optionIndex]["linkUnSelect"]) do
            if linkUnSelectedTable[value] == nil then
                linkUnSelectedTable[value] = {}
            end
            table.insert(linkUnSelectedTable[value], optionNode)
        end
    end
    if lineData[optionIndex]["unSelectlinkageLevel"] then
        if unSelectlinkageLevelTable[lineData[optionIndex]["unSelectlinkageLevel"]] == nil then
            unSelectlinkageLevelTable[lineData[optionIndex]["unSelectlinkageLevel"]] = {}
        end
        table.insert(unSelectlinkageLevelTable[lineData[optionIndex]["unSelectlinkageLevel"]],optionNode)
    end
    if lineData[optionIndex]["linkProhibit"] then
        for _, value in ipairs(lineData[optionIndex]["linkProhibit"]) do
            if linkProhibitTable[value] == nil then
                linkProhibitTable[value] = {}
            end
            table.insert(linkProhibitTable[value], optionNode)
        end
    end
end

--加载默认选项
function CreateBoxRoomView:setDefaultOptions()
    local gameRuleData = XH.ReadGameListAndRuleConfig.loadBoxGameRule(XH.areaData:getLobbyID(), self._gameID)
	if gameRuleData == nil then return end
    --该显示的所有选项表
    local showOptiondsTable
    --选择大类
    if gameRuleData["categories"] then
        if self._categoriesIndex then
            showOptiondsTable = gameRuleData["categories"][self._categoriesIndex]
        else
            showOptiondsTable = gameRuleData["categories"][gameRuleData["defaultchoose"]]
        end
    else
        showOptiondsTable = gameRuleData
    end
    for _, playTypeData in pairsByKeys(showOptiondsTable) do
        if playTypeData["defaultchoose"] then
            for _, nodeName in ipairs(playTypeData["defaultchoose"]) do
                local optionNode = XH.UITool.seekNodeByName(self._gameRule, nodeName)
                if optionNode then
                    if playTypeData["type"] == "radio" then
                        self:onTouchEventRadio(optionNode, ccui.CheckBoxEventType.selected, true)
                    elseif playTypeData["type"] == "checkbox" then
                        self:onTouchEventCheckBox(optionNode, ccui.CheckBoxEventType.selected, true)
                    end
                    
                end
            end
        end
    end
end

--设置默认选择的游戏
function CreateBoxRoomView:setDefaultSelectGameDynamic(defaultGameId)
    local chcheGameID = XH.userDefault:getValue(XH.userDefault.KEY_ID.LAST_CHOOSE_GAME_ID, defaultGameId)
    local gameID = self._firstSelectedGameId ~= 0 and self._firstSelectedGameId or chcheGameID
    if gameID == 0 then
        gameID = GameNewTypeConfiger:getDefultGameIDByLobbyID(XH.areaData:getLobbyID())
    end
    local gameBtnNode = XH.UITool.seekNodeByName(self, gameID) or self._listViewGames:getItem(0)
    if gameBtnNode then
        self:onTouchEventChooseGameDynamic(gameBtnNode, ccui.TouchEventType.ended,true)
        local index = self._listViewGames:getIndex(gameBtnNode)
        if index then
            self._listViewGames:jumpToItem(index, cc.p(0, 0), cc.p(0, 0))
        end
    end
end

function CreateBoxRoomView:checkSelectGameDynamicLegal()
    local isLegal = false
    for _, value in pairs(self._listViewGames:getChildren()) do
        if value then
            if not value:isEnabled() then
                isLegal = true
            end
        end
    end
    if  not isLegal then
        local gameListData = XH.lobby:getModule("CreateBoxRoom"):getGameListData()
        if gameListData[1] then
            local firstGameId  = gameListData[1].gameID or 0
            if firstGameId ~= 0 then
                local gameBtnNode = XH.UITool.seekNodeByName(self, firstGameId)
                if gameBtnNode then
                    self:onTouchEventChooseGameDynamic(gameBtnNode, ccui.TouchEventType.ended,true)
                end
            end
        end
    end
end

--点击单选按钮
function CreateBoxRoomView:onTouchEventRadio(send, eventType, isDefault)
    --加载默认选项时不需要音效
    if isDefault == nil then
        XH.audioManager:play("BUTTON_CLICK")
    end
    if eventType == ccui.CheckBoxEventType.selected then
        --判断点击的是否是大类的按钮
        local isCategoriesBtn = false
        for _, v in pairs(categorieTable) do
            if v["node"] == send and isDefault == nil then
              --记录所选游戏的大类
                XH.userDefault:setValue(XH.userDefault.KEY_ID.GAMERULE_CATEGORIE_SELECT, tonumber(v["nodeConfig"]["categorieIndex"]) , {gameID = self._gameID})
                self._categoriesIndex = v["nodeConfig"]["categorieIndex"]
                isCategoriesBtn = true
            end
        end
        local tmpNodeTable = {}  --点击的节点的节点组
        for _, nodeTable in pairs(radioTable) do
            for _, nodeInfo in pairs(nodeTable) do
                if nodeInfo["node"] == send then
                    tmpNodeTable = nodeTable
                    break
                end
            end
        end
        for _,nodeInfo in pairs(tmpNodeTable) do
            if nodeInfo["node"] == send then
                self:setSelectState(nodeInfo["node"],true)
            else
                if nodeInfo["node"]:isBright() and nodeInfo["node"]:isEnabled()then
                    self:setSelectState(nodeInfo["node"],false)
                end
            end
        end
        for _, nodeTable in pairs(linkageLevelTable) do
            for _, node in pairs(nodeTable) do
                if node == send then
                    --更新联动相关联节点
                    self:updateLinkageNode(node)
                end
            end
        end
        for _, nodeTable in pairs(unSelectlinkageLevelTable) do
            for _, node in pairs(nodeTable) do
                if node == send then
                    --更新联动相关联节点
                    self:updateLinkageNode(node)
                end
            end
        end
        --是大类按钮就重新刷新按钮选项
        if isCategoriesBtn then
            self:loadGameRuleDynamic(self._gameID)
            return
        end
    elseif eventType == ccui.CheckBoxEventType.unselected then
        self:setSelectState(send, true)
    end
    --更新房卡消耗的显示
    self:updateShowCostInfo()
end

--点击复选按钮
function CreateBoxRoomView:onTouchEventCheckBox(send, eventType, isDefault)
    --加载默认选项时不需要音效
    if isDefault == nil then
        XH.audioManager:play("BUTTON_CLICK")
    end
    if eventType == ccui.CheckBoxEventType.selected then
        self:setSelectState(send,true)
    elseif eventType == ccui.CheckBoxEventType.unselected then
        self:setSelectState(send, false, true)
    end
    for _, nodeTable in pairs(linkageLevelTable) do
        for _, node in pairs(nodeTable) do
            if node == send then
                --更新联动相关联节点
                self:updateLinkageNode(node)
            end
        end
    end
    for _, nodeTable in pairs(unSelectlinkageLevelTable) do
        for _, node in pairs(nodeTable) do
            if node == send then
                --更新联动相关联节点
                self:updateLinkageNode(node)
            end
        end
    end
    --更新房卡消耗的显示
    self:updateShowCostInfo()
end

--点击Radio选项的文字
function CreateBoxRoomView:onTouchRadioOptionText(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    send = send:getParent()
    if send:isBright() and send:isSelected() == true then
        self:onTouchEventRadio(send, ccui.CheckBoxEventType.unselected)
    elseif send:isBright() and send:isSelected() == false then
        self:onTouchEventRadio(send, ccui.CheckBoxEventType.selected)
    end
end

--点击CheckBox选项的文字
function CreateBoxRoomView:onTouchCheckBoxOptionText(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    send = send:getParent()
    if send:isBright() and send:isSelected() == true then
        self:onTouchEventCheckBox(send, ccui.CheckBoxEventType.unselected)
    elseif send:isBright() and send:isSelected() == false then
        self:onTouchEventCheckBox(send, ccui.CheckBoxEventType.selected)
    end
end

--点击提示问号按钮
function CreateBoxRoomView:onTouchEventTipsBtn(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    local tipsNode = XH.UITool.seekNodeByName(send, KW_PIC_TIPS_PAOPAO)
    if tipsNode then
        tipsNode:stopAllActions()
        local opacity = tipsNode:getOpacity()  
        if opacity == 255 then
            tipsNode:setOpacity(0)
        else
            tipsNode:runAction(cc.Sequence:create(cc.FadeIn:create(0.2), cc.DelayTime:create(2), cc.FadeOut:create(0.2)))
        end
    end
end

--加载完选项之后更新和联动值相关的节点
function CreateBoxRoomView:updateLinkageNodeBylinkageLevelValue()
    self:updateLinkageNode()
end

--更新联动相关联节点
function CreateBoxRoomView:updateLinkageNode(clicknode)
    local linknode
    --联动显示按钮和联动不选中按钮级别最低
    for _, v in pairs(linkageLevelValue) do
        local linkageLevel
        for level, levelnode in pairs(v) do
            linkageLevel = level
            linknode = levelnode
        end
        if showTable[linkageLevel] then
            for _, node in pairs(showTable[linkageLevel]) do
                local isSelected = self:getCacheButtonState(node:getName())
                self:setSelectState(node,isSelected)
                node:setBright(true)
                node:setTouchEnabled(true)
                node:setVisible(true)
            end
        end
        if linkUnSelectedTable[linkageLevel] then
            for _, node in pairs(linkUnSelectedTable[linkageLevel]) do
                if node ~= clicknode then
                    self:setSelectState(node, false)
                end
            end
        end
    end
    --隐藏和隐藏之后选中，禁用和禁用之后选中级别中等
    for _, v in pairs(linkageLevelValue) do
        local linkageLevel
        for level, levelnode in pairs(v) do
            linkageLevel = level
            linknode = levelnode
        end
        if hideTable[linkageLevel] then
            for _, node in pairs(hideTable[linkageLevel]) do
                local isSelect = node:isSelected() 
                node:setVisible(false)
                --处理隐藏需要自动选中其他节点
                for _, hideSelectInfo in pairs(hideSelectTable) do
                    if hideSelectInfo["node"] == node then
                        --将隐藏的按钮缓存为未选中
                        self:setSelectState(node,false)
                        --隐藏按钮之后要选择的按钮设置为选中
                        if isSelect then
                            local needSelectNode = XH.UITool.seekNodeByName(self._gameRule, hideSelectInfo["nodeConfig"]["hideSelect"])
                            if needSelectNode then
                                self:setSelectState(needSelectNode,true)
                            end
                        end
                    end 
                end
            end
        end
        if prohibitTable[linkageLevel] then
            for _, node in pairs(prohibitTable[linkageLevel]) do
                local isSelect = node:isSelected() 
                node:setSelected(false)
                node:setBright(false)
                node:setTouchEnabled(false)
                --处理禁用需要自动选中其他节点
                for _, prohibitSelectInfo in pairs(prohibitSelectTable) do
                    if prohibitSelectInfo["node"] == node then
                        --将禁用的按钮缓存为未选中
                        self:setSelectState(node,false)
                        --禁用按钮之后要选择的按钮设置为选中
                        if isSelect then
                            local needSelectNode = XH.UITool.seekNodeByName(self._gameRule, prohibitSelectInfo["nodeConfig"]["prohibitSelect"])
                            if needSelectNode then
                                self:setSelectState(needSelectNode,true)
                            end
                        end
                    end 
                end
                local childrenNode = node:getChildren()
                for i = 1,node:getChildrenCount() do
                    childrenNode[i]:setColor(cc.c3b(255,255,255))
                    childrenNode[i]:setTextColor(cc.c3b(160, 160, 160))
                end
            end
        end
        if prohibitAndSelectTable[linkageLevel] then
            for _, node in pairs(prohibitAndSelectTable[linkageLevel]) do
                local isSelect = node:isSelected() 
                node:setSelected(true)
                node:setBright(false)
                node:setTouchEnabled(false)
                self:setSelectState(node,true)
                --处理禁用且选中自己的同时需要选中其他节点
                for _, prohibitSelectInfo in pairs(prohibitSelectTable) do
                    if prohibitSelectInfo["node"] == node then
                        --禁用按钮之后要选择的按钮设置为选中
                        if isSelect then
                            local needSelectNode = XH.UITool.seekNodeByName(self._gameRule, prohibitSelectInfo["nodeConfig"]["prohibitSelect"])
                            if needSelectNode then
                                self:setSelectState(needSelectNode,true)
                            end
                        end
                    end 
                end
                local childrenNode = node:getChildren()
                for i = 1,node:getChildrenCount() do
                    childrenNode[i]:setColor(cc.c3b(255,255,255))
                    childrenNode[i]:setTextColor(cc.c3b(160, 160, 160))
                end
            end
        end
    end
    --联动必须选中和联动必须禁用级别高
    for _, v in pairs(linkageLevelValue) do
        local linkageLevel
        for level, levelnode in pairs(v) do
            linkageLevel = level
            linknode = levelnode
        end
        if linkSelectedTable[linkageLevel] then
            for _, node in pairs(linkSelectedTable[linkageLevel]) do
                if linknode and linknode:isSelected() then
                    node:setBright(true)
                    node:setTouchEnabled(true)
                    self:setSelectState(node, true)
                end
            end
        end
        if linkProhibitTable[linkageLevel] then
            for _, node in pairs(linkProhibitTable[linkageLevel]) do
                if linknode and linknode:isSelected() then
                    local isSelect = node:isSelected() 
                    node:setSelected(false)
                    node:setBright(false)
                    node:setTouchEnabled(false)
                    --处理禁用需要自动选中其他节点
                    for _, prohibitSelectInfo in pairs(prohibitSelectTable) do
                        if prohibitSelectInfo["node"] == node then
                            --将禁用的按钮缓存为未选中
                            self:setSelectState(node,false)
                            --禁用按钮之后要选择的按钮设置为选中
                            if isSelect then
                                local needSelectNode = XH.UITool.seekNodeByName(self._gameRule, prohibitSelectInfo["nodeConfig"]["prohibitSelect"])
                                if needSelectNode then
                                    self:setSelectState(needSelectNode,true)
                                end
                            end
                        end 
                    end
                    local childrenNode = node:getChildren()
                    for i = 1,node:getChildrenCount() do
                        childrenNode[i]:setColor(cc.c3b(255,255,255))
                        childrenNode[i]:setTextColor(cc.c3b(160, 160, 160))
                    end 
                end
            end
        end
    end
    --多个联动值同时存在时才禁用某个按钮的级别最高
    for _, prohibitMeanwhileInfo in pairs(prohibitMeanwhileTable) do
        local isNeedProhibitCount = 0
        local prohibitMeanwhileCount = 0
        for _, value in pairs(prohibitMeanwhileInfo["nodeConfig"]["prohibitMeanwhile"]) do
            prohibitMeanwhileCount  = prohibitMeanwhileCount + 1
            for _, v in pairs(linkageLevelValue) do
                if v[value] then
                    isNeedProhibitCount = isNeedProhibitCount + 1
                    break
                end
            end
        end
        local node = prohibitMeanwhileInfo["node"]
        if isNeedProhibitCount == prohibitMeanwhileCount then
            local isSelect = node:isSelected() 
            node:setSelected(false)
            node:setBright(false)
            node:setTouchEnabled(false)
            --处理禁用需要自动选中其他节点
            for _, prohibitSelectInfo in pairs(prohibitSelectTable) do
                if prohibitSelectInfo["node"] == node then
                    --将禁用的按钮缓存为未选中
                    self:setSelectState(node,false)
                    --禁用按钮之后要选择的按钮设置为选中
                    if isSelect then
                        local needSelectNode = XH.UITool.seekNodeByName(self._gameRule, prohibitSelectInfo["nodeConfig"]["prohibitSelect"])
                        if needSelectNode then
                            self:setSelectState(needSelectNode,true)
                        end
                    end
                end 
            end
            local childrenNode = node:getChildren()
            for i = 1,node:getChildrenCount() do
                childrenNode[i]:setColor(cc.c3b(255,255,255))
                childrenNode[i]:setTextColor(cc.c3b(160, 160, 160))
            end
        end 
    end
end

--设置按钮选择状态
function CreateBoxRoomView:setSelectState(node, isSelected, isCheckBox)
    node:setSelected(isSelected)
    local childrenNode = node:getChildren()
    for i = 1,node:getChildrenCount() do
        if node:isSelected() then
            childrenNode[i]:setColor(cc.c3b(255, 255, 255))
            childrenNode[i]:setTextColor(cc.c3b(163, 111, 72))
        else
            childrenNode[i]:setColor(cc.c3b(255, 255, 255))
            childrenNode[i]:setTextColor(cc.c3b(163, 111, 72))
        end
    end
    self:setCacheButtonState(node:getName(), isSelected)
    for k, nodeTable in pairs(unSelectlinkageLevelTable) do
        for _, tmpNode in pairs(nodeTable) do
            if node == tmpNode then
                if node:isSelected() then
                    --存储需要更新联动相关联节点的联动值
                    for index, singlelinkageLevelValue in pairs(linkageLevelValue) do
                        for level, levelnode in pairs(singlelinkageLevelValue) do
                            if levelnode == node and k == level then
                                table.remove(linkageLevelValue, index)
                            end
                        end
                    end
                else
                    --存储需要更新联动相关联节点的联动值
                    local tmpSinglelinkageLevelValue = {}
                    tmpSinglelinkageLevelValue[k] = node
                    local isExist = false
                    --屏蔽已经存在的节点联动
                    for _, nodeInfo in pairs(linkageLevelValue) do
                        if nodeInfo[k] == node then
                            isExist = true
                        end
                    end
                    if isExist == false then
                        table.insert(linkageLevelValue, tmpSinglelinkageLevelValue)
                    end
                end
            end
        end
    end
    for k, nodeTable in pairs(linkageLevelTable) do
        for _, tmpNode in pairs(nodeTable) do
            if node == tmpNode then
                if node:isSelected() then
                    --存储需要更新联动相关联节点的联动值
                    local tmpSinglelinkageLevelValue = {}
                    tmpSinglelinkageLevelValue[k] = node
                    local isExist = false
                    --屏蔽已经存在的节点联动
                    for _, nodeInfo in pairs(linkageLevelValue) do
                        if nodeInfo[k] == node then
                            isExist = true
                        end
                    end
                    if isExist == false then
                        table.insert(linkageLevelValue, tmpSinglelinkageLevelValue)
                    end
                else
                    --存储需要更新联动相关联节点的联动值
                    for index, singlelinkageLevelValue in pairs(linkageLevelValue) do
                        for level, levelnode in pairs(singlelinkageLevelValue) do
                            if levelnode == node and k == level then
                                table.remove(linkageLevelValue, index)
                            end
                        end
                    end
                end
            end
        end
    end
end

function CreateBoxRoomView:getCacheButtonStateExtraParam(nodeName)
    local extraParam = {}
    extraParam.gameID = self._gameID
    extraParam.categoriesIndex = self._categoriesIndex
    extraParam.nodeName = nodeName
    return extraParam
end

--缓存按钮选中状态
function CreateBoxRoomView:setCacheButtonState(nodeName, isSelected)
    --去除xml不能出现的字符
    local extraParam = self:getCacheButtonStateExtraParam(nodeName)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.GAMERULE_CACHE_BUTTON_STATE, isSelected, extraParam)
end

--读取按钮选中状态
function CreateBoxRoomView:getCacheButtonState(nodeName)
    --去除xml不能出现的字符
    local extraParam = self:getCacheButtonStateExtraParam(nodeName)
    return XH.userDefault:getValue(XH.userDefault.KEY_ID.GAMERULE_CACHE_BUTTON_STATE, false, extraParam)
end

--点击创建游戏
function CreateBoxRoomView:onTouchEventCreateBoxRoom(send, eventType)
    if XH.playerData:checkNewRealName(self._gameID) ~= 0 then return end--实名限制
    if XH.playerData:checkNewRealNameVisitor() ~= 0 then return end--游客登陆
    local gameRule = self:getGameRule()
    gameRule = gameRule .. string.format("RoomFee='%d';", tonumber(self._roomFee or 0))
    --多版本合规台炮麻将、嘉兴麻将
    if self._gameID == MutiVersionGameConfig.MutiVersionGameID.JiaXingMahjong 
    or self._gameID == MutiVersionGameConfig.MutiVersionGameID.TaiPaoMahjong then
        for _, v in pairs(categorieTable) do
            if v["nodeConfig"]["categorieIndex"] == (self._categoriesIndex or 1) then
                local tmpRuleStr = loadstring("return {" .. v["nodeConfig"]["nodeName"] .. "}")()  
                if tmpRuleStr["gamedID"] then
                    self._gameID = tonumber(tmpRuleStr["gamedID"])
                    if self._gameID == 30020 or self._gameID == 30022 then--包厢厂硬爆头读房间配置特殊，需特判
                        self._isTChaos = true
                    end
                    break
                end
            end
        end
    end

    if self:checkNewPlayTypeSelect() then
        return
    end

    XH.lobby:getModule("CreateBoxRoom"):reqCreateBoxRoom(self._gameID, self:getGameConditions(), gameRule, self:getRoomMode(), self:getPlayerCount(), XH.areaData:getSrsGroupID())
    self:RecordData(self._gameID,os.time())
    local tmpData = {}
    tmpData.userid = XH.playerData:getNumberID()
    tmpData.playercnt = self:getPlayerCount()
    tmpData.playcnt = self:getPlayCount()
    tmpData.gameid = self._gameID
    XH.throwDataManager:throwData(XH.ThrowDataDefine.LOBBY_CREATE_BOX_ROOM_BTN, tmpData) 
end

--点击免费创建游戏
function CreateBoxRoomView:onTouchEventCreateBoxRoomFree(send, eventType)
    if self._freeCnt and self._freeCnt > 0 and self._freeLeftTime and self._freeLeftTime > 0 then
        local gameRule = self:getGameRule()
        gameRule = gameRule .. "RoomFee='0';"
        XH.lobby:getModule("CreateBoxRoom"):reqCreateBoxRoom(self._gameID, self:getGameConditions(), gameRule, self:getRoomMode(), self:getPlayerCount(), XH.areaData:getSrsGroupID(), XH.areaData:getPropFreeCouponID())
        self:RecordData(self._gameID,os.time())
    else
        XH.TipTool.showTip({
			type = XH.TIP_LAYER_TYPE.OK_CANCEL,
		}, "暂时不支持购买")
    end
end

--点击关闭按钮
function CreateBoxRoomView:onTouchEventClose(send, eventType)
    if XH.playerData:getIsBoxRoomGuide() then
        XH.playerData:setIsBoxRoomGuide(false)
    end
    self:close()
end

--获取游戏规则
function CreateBoxRoomView:getGameRule()
    local strGameRule = ""
    for _,nodeTable in pairs(radioTable) do
        for _, nodeInfo in pairs(nodeTable) do
            if nodeInfo["node"]:isSelected() and nodeInfo["node"]:isVisible() then
                local ruleString = nodeInfo["node"]:getName()
                if string.sub(ruleString,#ruleString,#ruleString) == ";" then
                    strGameRule = strGameRule .. nodeInfo["node"]:getName()
                end
            end
        end
    end

    for _, checkBoxInfo in pairs(checkBoxTable) do
		if checkBoxInfo["node"]:isSelected() and checkBoxInfo["node"]:isVisible() then
			strGameRule = strGameRule .. checkBoxInfo["nodeConfig"]["select"]
        elseif checkBoxInfo["node"]:isVisible() then
            strGameRule = strGameRule .. checkBoxInfo["nodeConfig"]["unselect"]
		end 
    end

    for _, mustExistRuleInfo in pairs(mustExistRuleTable) do
		if mustExistRuleInfo["node"]:isSelected() and mustExistRuleInfo["node"]:isVisible() then
			strGameRule = strGameRule .. mustExistRuleInfo["nodeConfig"]["mustExistRuleYes"]
        elseif mustExistRuleInfo["node"]:isVisible() then
            if mustExistRuleInfo["nodeConfig"]["mustExistRuleNo"] then
                strGameRule = strGameRule .. mustExistRuleInfo["nodeConfig"]["mustExistRuleNo"]
            end
		end
    end

    local defaultRuleToServer = self:getDefaultRuleToServer()
    if defaultRuleToServer and (not defaultRuleToServer.NotCategoriesIndex or defaultRuleToServer.NotCategoriesIndex ~= self._categoriesIndex) then
        if defaultRuleToServer.Rule then
            strGameRule = strGameRule .. defaultRuleToServer.Rule
        end
    end

    return strGameRule
end

function CreateBoxRoomView:getDefaultRuleToServer()
    local gameID = self._gameID
    for _, v in pairs(categorieTable) do
        if v["nodeConfig"] and v["nodeConfig"]["categorieIndex"] and v["nodeConfig"]["categorieIndex"] == (self._categoriesIndex or 1) then
            local tmpRuleStr = loadstring("return {" .. v["nodeConfig"]["nodeName"] .. "}")()  
            if tmpRuleStr["gamedID"] then
                gameID = tmpRuleStr["gamedID"]
            end
        end
    end
    local defaultRuleToServer = nil
    if gameID and CreateBoxRoomConfig.DefaultRuleToServer[gameID] then
        defaultRuleToServer = CreateBoxRoomConfig.DefaultRuleToServer[gameID]
    end
    return defaultRuleToServer
end

--获取房间除了局数玩法之外的特殊玩法，比如圈数这种不定局的玩法
function CreateBoxRoomView:getGameConditions()
	local gameConditions = {}
    for _, conditionInfo in pairs(conditionTable) do
		if conditionInfo["node"]:isSelected() then
			gameConditions[conditionInfo["nodeConfig"]["condition"]] = conditionInfo["nodeConfig"]["conditionYes"]
		else
            if conditionInfo["nodeConfig"]["conditionNo"] ~= nil then
				gameConditions[conditionInfo["nodeConfig"]["condition"]] = conditionInfo["nodeConfig"]["conditionNo"]
			end
		end 
    end
    return gameConditions
end

--获取房间模式，即天枢房间模式规则中的t几，其中的配置项为房间开始扣道具局数，数量，最多局数等
function CreateBoxRoomView:getRoomMode()
    if self._isTChaos then
        --根据玩家选择的是几局和消耗方式确定模式几
        local mode = 1
        local isAACost = false
        local isBigWinnerCost = false
        for _, costTypeInfo in pairs(costTypeTable) do
            if costTypeInfo["node"]:isSelected() then
                if tostring(costTypeInfo["nodeConfig"]["costType"]) == "aaCost" then
                    isAACost = true
                elseif tostring(costTypeInfo["nodeConfig"]["costType"]) == "bigWinnerCost" then
                    isBigWinnerCost = true
                end               
            end
        end
            for _, costRelevantInfo in pairs(playCountTable) do
                if  costRelevantInfo["node"]:isSelected() then
                    if isAACost then
                        local tmpMode = costRelevantInfo["nodeConfig"][tostring("aaCostT"..self:getPlayerCount())]
                        if tmpMode then
                            mode = tmpMode
                        else
                            mode = costRelevantInfo["nodeConfig"]["aaCostT"]
                        end
                    elseif isBigWinnerCost then
                        local tmpMode = costRelevantInfo["nodeConfig"][tostring("bigWinnerCostT"..self:getPlayerCount())]
                        if tmpMode then
                            mode = tmpMode
                        else
                            mode = costRelevantInfo["nodeConfig"]["bigWinnerCostT"]
                        end
                    else
                        local tmpMode = costRelevantInfo["nodeConfig"][tostring("allCostT"..self:getPlayerCount())]
                        if tmpMode then
                            mode = tmpMode
                        else
                            mode = costRelevantInfo["nodeConfig"]["allCostT"]
                        end
                    end
                end
            end
        return mode
    else
        --根据玩家选择的是几局和消耗方式确定模式几
        local mode = 1
        local hasGetMode = false
        local playCountItem = XH.UITool.seekNodeByName(self._gameRule, "playCount1")
        if playCountItem then
            for i = 2, #playCountItem:getChildren() do
                if playCountItem:getChildren()[i]:isSelected() then
                    if self._isT1Disuse then
                        mode = i
                        hasGetMode = true
                        break
                    else
                        mode = i - 1
                        hasGetMode = true
                        break
                    end
                end
            end
        end
        if hasGetMode == false then
            playCountItem = XH.UITool.seekNodeByName(self._gameRule, "playCount2")
            if playCountItem then
                for i = 2, #playCountItem:getChildren() do
                    if playCountItem:getChildren()[i]:isSelected() then
                        if self._isT1Disuse then
                            mode = i
                            break
                        else
                            mode = i - 1
                            break
                        end
                    end
                end
            end
        end
        local payTypeItem = XH.UITool.seekNodeByName(self._gameRule, "costType")
        if payTypeItem then
            for i = 2, #payTypeItem:getChildren() do
                if payTypeItem:getChildren()[i]:isSelected() then
                    local payIndex = tonumber(string.match(payTypeItem:getChildren()[i]:getName(), "%d"))
                    if self._isFourAset then
                        --天枢中通常4为一组，即t1至t4为房主消耗，5至8为aa消耗
                        mode = payIndex * 4 + mode
                    else
                        if playCountItem then
                            mode = payIndex * (#playCountItem:getChildren() - 1) + mode
                        else
                            mode = mode * 2
                        end
                    end
                end
            end
        end
        return mode
    end
end

--获取玩家数量
function CreateBoxRoomView:getPlayerCount()
    local index = 1
    while true do
        local playerCountItem = XH.UITool.seekNodeByName(self._gameRule, "playerCount"..index)
        index = index + 1
        if playerCountItem then
            for i = 2, #playerCountItem:getChildren() do
                if string.find(playerCountItem:getChildren()[i]:getName(), "FOUR_TO_TWO") == nil and playerCountItem:getChildren()[i]:isSelected() then
                    return tonumber(string.match(playerCountItem:getChildren()[i]:getName(), "%d"))
                end
            end
        else
            break
        end
    end
    return 4
end

--获取局数
function CreateBoxRoomView:getPlayCount()
    local index = 1
    while true do
        local playCountItem = XH.UITool.seekNodeByName(self._gameRule, "playCount"..index)
        index = index + 1
        if playCountItem then
            for i = 2, #playCountItem:getChildren() do
                if playCountItem:getChildren()[i].isSelected and playCountItem:getChildren()[i]:isSelected() then
                    return tonumber(string.match(playCountItem:getChildren()[i]:getName(), "%d+"))
                end
            end
        else
            break
        end
    end
    return 8
end

--更新房卡消耗的显示
function CreateBoxRoomView:updateShowCostInfo()
    local costString = ""
    local discountCost = -1
    for _, costRelevantInfo in pairs(playCountTable) do
        if costRelevantInfo["node"]:isSelected() and self._costTextInfo then
            if self._isCostRelativePlayers then
                if self._isMutiVersionGame then
                    local cost = tonumber(costRelevantInfo["nodeConfig"][tostring("allMutiVersionCost" .. self:getPlayerCount())])
                    -- discountCost = tonumber(costRelevantInfo["nodeConfig"][tostring("allDiscountCost" .. self:getPlayerCount())])
                    costString = "x " .. cost
                    for _, costTypeInfo in pairs(costTypeTable) do
                        if costTypeInfo["node"]:isSelected() then
                            if tostring(costTypeInfo["nodeConfig"]["costType"]) == "aaCost" then
                                cost = tonumber(costRelevantInfo["nodeConfig"][tostring("aaMutiVersionCost" .. self:getPlayerCount())])
                                -- discountCost = tonumber(costRelevantInfo["nodeConfig"][tostring("aaDiscountCost" .. self:getPlayerCount())])
                                -- if tonumber(TeaHouse.BridgeData.getLobbyID()) == XH.LOBBY_ID.TAIZHOU and tostring(costTypeInfo["nodeConfig"]["costType"]) == "allCost" then
                                --     cost = math.ceil(cost)
                                -- end
                                costString = "x " .. cost
                                costString = costString .. " (每人)"
                                -- if discountCost and discountCost ~= -1 then 
                                --     discountCost = discountCost .." (每人)"
                                -- end
                            end
                            break
                        end
                    end
                else
                    discountCost = tonumber(costRelevantInfo["nodeConfig"][tostring("allDiscountCost" .. self:getPlayerCount())])
                    costString = "x ".. costRelevantInfo["nodeConfig"][tostring("allCost"..self:getPlayerCount())]
                    for _, costTypeInfo in pairs(costTypeTable) do
                        if costTypeInfo["node"]:isSelected() then
                            costString = "x ".. costRelevantInfo["nodeConfig"][tostring(costTypeInfo["nodeConfig"]["costType"]..self:getPlayerCount())]
                            if tostring(costTypeInfo["nodeConfig"]["costType"]) == "aaCost" then
                                discountCost = tonumber(costRelevantInfo["nodeConfig"][tostring("aaDiscountCost" .. self:getPlayerCount())])
                            else
                                discountCost = tonumber(costRelevantInfo["nodeConfig"][tostring("allDiscountCost" .. self:getPlayerCount())])
                            end
                            self._roomFee = costRelevantInfo["nodeConfig"][tostring(costTypeInfo["nodeConfig"]["costType"]..self:getPlayerCount())]
                            if tostring(costTypeInfo["nodeConfig"]["costType"]) == "aaCost" then
                                costString = costString .. " (每人)"
                                if discountCost and discountCost ~= -1 then 
                                    discountCost = discountCost .." (每人)"
                                end
                            end
                            break
                        end
                    end
                end
            else
                costString = "x ".. costRelevantInfo["nodeConfig"]["allCost"]
                for _, costTypeInfo in pairs(costTypeTable) do
                    if costTypeInfo["node"]:isSelected() then
                        costString = "x ".. costRelevantInfo["nodeConfig"][costTypeInfo["nodeConfig"]["costType"]]
                        self._roomFee = costRelevantInfo["nodeConfig"][costTypeInfo["nodeConfig"]["costType"]]
                        if tostring(costTypeInfo["nodeConfig"]["costType"]) == "aaCost" then
                            costString = costString .. " (每人)"
                        end
                        break
                    end
                end
            end
            break
        end
    end
    if costString ~= "" then
        self._costTextInfo:setString(costString)
        self._costTextInfoNormal:setString(costString)
    end
    self._imgAreaCostHua:setVisible(false)
    self._imgAreaCostText:setVisible(false)
    self._imgLimitAct:setVisible(false)
    if discountCost and discountCost ~= -1 then
        self._imgAreaCostHua:setVisible(true)
        self._imgAreaCostText:setVisible(true)
        self._imgLimitAct:setVisible(true)
        self._imgAreaCostText:setString("房卡消耗 X "..discountCost)
    end
    self:flushLimitFreeText()
end

--倒计时
function CreateBoxRoomView:runCountDownAcitvity(node, leftTime)
    local iconNode = node:getChildByName(KW_IMG_ACTIVITY)
    local bgNode = node:getChildByName(KW_IMG_LEFT_TIME)
    local timeNode = bgNode:getChildByName(KW_TEXT_LFETTIME)

    timeNode:stopAllActions()
    timeNode:runAction(cc.RepeatForever:create(cc.Sequence:create(
    cc.CallFunc:create(function()
        leftTime = leftTime - 1
        if leftTime >= 0 then
            local d = math.floor(leftTime / 86400)
            local h = math.floor((leftTime - d * 86400) / 3600)
            local m = math.floor((leftTime - d * 86400 - h * 3600) / 60)
            local strTime = string.format("活动时间：%d天%02d时", d, h)
            if d == 0 then
                strTime = string.format("活动时间：%02d时%02d分", h, m)
            end
            timeNode:setString(strTime)
        else
            iconNode:setVisible(false)
            bgNode:setVisible(false)
            timeNode:stopAllActions()
        end
    end),cc.DelayTime:create(1))))
end
    
function CreateBoxRoomView:onGetSystemTimeSuccess(event)
    self:refreshActivityIcon(XH.lobby:getModule("CreateBoxRoom"):getActivityLeftTime())
    self:showLimitTimeFreeLogo()
end

--刷新活动角标
function CreateBoxRoomView:refreshActivityIcon(leftTime)
    for _, value in pairs(self._listViewGames:getChildren()) do
        --判断是否是活动游戏
        local gameid = value:getTag()
        if XH.lobby:getModule("CreateBoxRoom"):isActivityGame(gameid) then
            XH.UITool.setVisible(value, KW_IMG_ACTIVITY, true)
            XH.UITool.setVisible(value, KW_IMG_LEFT_TIME, true)
            XH.UITool.loadTexture(value, KW_IMG_ACTIVITY, "lt_icon.png", ccui.TextureResType.plistType)
            self:runCountDownAcitvity(value,leftTime)
        elseif XH.lobby:getModule("CreateBoxRoom"):isOnlyShowActGame(gameid) then
            XH.UITool.setVisible(value, KW_IMG_LEFT_TIME, false)
            XH.UITool.setVisible(value, KW_IMG_ACTIVITY, true)
            XH.UITool.loadTexture(value, KW_IMG_ACTIVITY, "lt_icon.png", ccui.TextureResType.plistType)
        else
            XH.UITool.setVisible(value, KW_IMG_LEFT_TIME, false)
            XH.UITool.setVisible(value, KW_IMG_ACTIVITY, false)
        end
        --判断是否为标记游戏(和活动角标同在) 
        if XH.lobby:getModule("Mark"):isShowMarkGame(gameid) then 
            XH.UITool.setVisible(value, KW_IMG_LEFT_TIME, false)
            XH.UITool.setVisible(value, KW_IMG_ACTIVITY, false)
        end
        self:showLimitTimeFreeLogo(gameid, value)
    end
end

--显示限免
function CreateBoxRoomView:showLimitTimeFreeLogo()
    for _, value in pairs(self._listViewGames:getChildren()) do
        if XH.lobby:getModule("CreateBoxRoom"):isLimitTimeFree() then
            XH.UITool.loadTexture(value, KW_IMG_ACTIVITY, "create_box_room_limit_time_free.png", ccui.TextureResType.plistType)
            XH.UITool.setVisible(value, KW_IMG_ACTIVITY, true)
        end
    end
end

function CreateBoxRoomView:showFreeLine(gameid)
    self._panelLine:setVisible(XH.lobby:getModule("CreateBoxRoom"):isLimitTimeFree())
end

function CreateBoxRoomView:initConsumeTips()
    local normalTypeMaxLen = 46
    local freeTypeMaxLen = 27
    local tipsStr = XH.ConstString.getStr("CREATE_TABLE_CONSUME_AFTER_FIRST_GAME")
    
    if XH.areaData:getLobbyID() == XH.LOBBY_ID.NINGBO or XH.areaData:getLobbyID() == XH.LOBBY_ID.YUYAO then
        local cfg = XH.lobby:getModule("Configuration"):getAllConfigData("CardCostInfo")
        if not cfg or  not cfg.createPanelText[tostring(XH.areaData:getLobbyID())] then
            tipsStr = XH.ConstString.getStr("CREATE_TABLE_CONSUME_BEFORE_FIRST_GAME")
        end
    end
    if self._btnCreateBoxRoom then
        local tipsNode = XH.UITool.seekNodeByName(self._btnCreateBoxRoom, "_KW_TEXT_ZHU")
        if tipsNode then
            tipsNode:setString(XH.StringTool.newLineStringByMaxLen(tipsStr, normalTypeMaxLen))
        end
    end
    if self._panelCreateFree then
        local tipsNode = XH.UITool.seekNodeByName(self._panelCreateFree, "_KW_TEXT_ZHU")
        if tipsNode then
            tipsNode:setString(XH.StringTool.newLineStringByMaxLen(tipsStr, freeTypeMaxLen))
        end
    end
end

function CreateBoxRoomView:on_KW_UI_JUMP_OVER_BTN(send,eventType)
    local isStep1 = self._guideLayerStep1:isVisible()
    self._jumpTime = os.time()
    self._newUserGuidePanel:setVisible(false)
    self._isGuideState = false
    self._notSetDefaultSelectGame = false
    XH.playerData:setIsBoxRoomGuide(false)
    if isStep1 then
        self:setDefaultSelectGameDynamic()
    end
end

--记录数据到北极星
function CreateBoxRoomView:RecordData(app_id,time)
    --数据统计
    local data = {}
    data.time2 = time
    data.time3 = self._jumpTime
    data.app_id = app_id
    data.userid = XH.playerData:getNumberID()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.CreateBoxGameSelected,data)
end

function CreateBoxRoomView:initFourToTwoPlayerBtn(twoPlayerBnt, optionIndex, haveTip)
    if FourToTwoConfig.getIsExceptGame(self._gameID) then
        return
    end
    if twoPlayerBnt and FourToTwoConfig.AreaConfig[XH.areaData:getLobbyID()].LobbyOpen then
        self:addFourToTwoHitImg(twoPlayerBnt, haveTip)
        self:addFourToTwoTip(twoPlayerBnt, optionIndex)
    end
end

function CreateBoxRoomView:initFourToTwo(gameId)
    if FourToTwoConfig.AreaConfig[XH.areaData:getLobbyID()].LobbyOpen and not FourToTwoConfig.getIsExceptGame(self._gameID) then
        local twoPlayerBnt
        local index = 1
        while true do
            local playerCountItem = XH.UITool.seekNodeByName(self._gameRule, "playerCount" .. index)
            index = index + 1
            if playerCountItem then
                for i = 2, #playerCountItem:getChildren() do
                    if tonumber(string.match(playerCountItem:getChildren()[i]:getName(), "%d")) == 2 then
                        twoPlayerBnt = playerCountItem:getChildren()[i]
                    end
                end
            else
                break
            end
        end
        local userGameIdStr = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_FOUR_TO_TWO_CTREATEROOM, "{}")
        local userGameIdTab = cjson.decode(userGameIdStr)

        if twoPlayerBnt then
            if not XH.TableTool.isValueInTable(gameId, userGameIdTab) or self._chooseTwoPlayer then
                self:choseTwoPlayersBtn(twoPlayerBnt)
                table.insert(userGameIdTab, gameId)
                XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_FOUR_TO_TWO_CTREATEROOM, cjson.encode(userGameIdTab))
            end
        end
    end
end

function CreateBoxRoomView:choseTwoPlayersBtn(send)
    if send then
        local tmpNodeTable = { }
        local isCategoriesBtn = false
        for _, v in pairs(categorieTable) do
            if v["node"] == send then
              --记录所选游戏的大类
                XH.userDefault:setValue(XH.userDefault.KEY_ID.GAMERULE_CATEGORIE_SELECT, tonumber(v["nodeConfig"]["categorieIndex"]) , {gameID = self._gameID})
                self._categoriesIndex = v["nodeConfig"]["categorieIndex"]
                isCategoriesBtn = true
            end
        end
        -- 点击的节点的节点组
        for _, nodeTable in pairs(radioTable) do
            for _, nodeInfo in pairs(nodeTable) do
                if nodeInfo["node"] == send then
                    tmpNodeTable = nodeTable
                    break
                end
            end
        end
        for _, nodeInfo in pairs(tmpNodeTable) do
            if nodeInfo["node"] == send then
                self:setSelectState(nodeInfo["node"], true)
            else
                if nodeInfo["node"]:isBright() and nodeInfo["node"]:isEnabled() then
                    self:setSelectState(nodeInfo["node"], false)
                end
            end
        end
        for _, nodeTable in pairs(linkageLevelTable) do
            for _, node in pairs(nodeTable) do
                if node == send then
                    --更新联动相关联节点
                    self:updateLinkageNode(node)
                end
            end
        end
        for _, nodeTable in pairs(unSelectlinkageLevelTable) do
            for _, node in pairs(nodeTable) do
                if node == send then
                    --更新联动相关联节点
                    self:updateLinkageNode(node)
                end
            end
        end
        if isCategoriesBtn then
            self:loadGameRuleDynamic(self._gameID)
        end
        -- 更新房卡消耗的显示
        self:updateShowCostInfo()
    end
end

function CreateBoxRoomView:addFourToTwoHitImg(node, haveTip)
    if not self:isValueInHideTable(node) then
        node:getParent():removeChildByName("_KW_FOUR_TO_TWO_HIT_IMG")
        local imgNode = ccui.ImageView:create("create_box_room_Hot_No_Cheat.png", ccui.TextureResType.plistType)
        if imgNode then
            node:getParent():addChild(imgNode)
            local pos = cc.p(node:getPosition())
            if haveTip then
                imgNode:setPosition(pos.x + node:getContentSize().width + 120,pos.y)
                local tipText = XH.UITool.seekNodeByName(node, KW_PANEL_TIPS)
                if tipText then
                    local tempPosX = tipText:getPositionX()
                    tipText:setPositionX(tempPosX + imgNode:getContentSize().width + 10)
                end
            else
                imgNode:setPosition(pos.x + node:getContentSize().width + 120,pos.y)
            end
            imgNode:ignoreContentAdaptWithSize(false)
            :setAnchorPoint(cc.p(0.5,0.5))
            :setName("_KW_FOUR_TO_TWO_HIT_IMG")
        end
    end
end

function CreateBoxRoomView:addFourToTwoTip(node, optionIndex)
    if not self:isValueInHideTable(node) then
        node:getParent():removeChildByName("_KW_FOUR_TO_TWO_TIP_PANEL")
        local tipNode
        if optionIndex < 3 then
            tipNode = self._tipFourToTwo2:clone()
        else
            tipNode = self._tipFourToTwo:clone()
        end
        
        if tipNode then
            tipNode:setName("_KW_FOUR_TO_TWO_TIP_PANEL")
            local pos = cc.p(node:getPosition())
            tipNode:setPosition(pos.x,pos.y+20)
            if optionIndex < 3 then
                tipNode:setPosition(pos.x,pos.y-20)
                node:getParent():setGlobalZOrder(99999)
            end
            node:getParent():addChild(tipNode)
            
            tipNode:addTouchEventListener(function(send, eventType)
                if eventType ~= ccui.TouchEventType.ended then
                    return
                end
                send:setVisible(false)
            end)

            local actSequence = cc.Sequence:create(cc.DelayTime:create(3), cc.CallFunc:create(function()
                                    tipNode:setVisible(false)
                                end))
            tipNode:runAction(actSequence)
        end
    end
end

function CreateBoxRoomView:isValueInHideTable(value)
    if value ~= nil then
        for _, hideValue in pairs(hideTable) do
            local isValue = XH.TableTool.isValueInTable(value, hideValue)
            if isValue then
                return isValue
            end
        end
    end
    return false
end

function CreateBoxRoomView:onTouchEventPlayModeAreaChange(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._panelPlayModeArea:setVisible(true)
    self:initItem()
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MutiVersionGame_AreaBtn) 
end

function CreateBoxRoomView:initItem()
    --读一个配置表，从表里初始化节点数据
    self._itemIndex = 1
    local loadGameId = self._tmpGameID or self._gameID
    for i,v in pairs(self._mutiGameTable) do
        if loadGameId == i then
            self:addItem(true,v.areaName,v.gameid,v.lobbyid,v.isMutiVersionGame)
            self._textPlayModeName:setString(v.areaName)
        else
            self:addItem(false,v.areaName,v.gameid,v.lobbyid,v.isMutiVersionGame)
        end
    end
end

function CreateBoxRoomView:addItem(itemState, itemName, gameId,lobbyid,isMutiVersionGame)
    if not self._panelDefaultItem then
        return
    end
    local itemNode = self["_nodeItem" .. self._itemIndex]
    if not itemNode then
        return
    end
    self._itemIndex = self._itemIndex + 1
    local item = self._panelDefaultItem:clone()
    item:setVisible(true)
    item:setPosition(cc.p(0, 0))
    itemNode:addChild(item)
    local btnState = ccui.Helper:seekWidgetByName(item, KW_PLAYMODE_STATUS)
    if btnState then
        btnState:setSelected(itemState)
        item:addTouchEventListener(function(send, eventType)
            if eventType ~= ccui.TouchEventType.ended then
                return
            end
            if not btnState:isSelected() then
                btnState:setSelected(true)
                self:updateItemState(itemName)
            end
        end)
        local textTip = ccui.Helper:seekWidgetByName(item, KW_PLAYMODE_NAME)
        if textTip then
            textTip:setString(itemName)
        end
        self._modeItems[#self._modeItems + 1] = {
            BtnName = itemName,
            SelectBtn = btnState,
            GameID = gameId,
            LobbyID = lobbyid,
            IsMutiVersionGame = isMutiVersionGame
        }
    end
    
end

function CreateBoxRoomView:updateItemState(itemName)
    for _,v in ipairs(self._modeItems) do
        if v.BtnName ~= itemName then
            v.SelectBtn:setSelected(false)
        else
            v.SelectBtn:setSelected(true)
            self._tmpGameID = v.GameID
            self._tmpGameName = v.BtnName
            self._tmpLobbyID = v.LobbyID
            self._tmpMutiVersionGame = v.IsMutiVersionGame
        end
    end
end

function CreateBoxRoomView:onTouchEventPlayTypeSave(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    --读取对应地区的配置，刷新桌子面板玩法
    if self._tmpGameID then
        self._gameID = self._tmpGameID 
    end
    self._isMutiVersionGame = self._tmpMutiVersionGame
    self:loadGameRuleDynamic(self._tmpGameID or self._gameID,self._tmpLobbyID)
    if self._tmpGameName then
        self._btnPlayModeArea:setTitleText("玩法："..self._tmpGameName) 
    end
    self._panelPlayModeArea:setVisible(false)
end

function CreateBoxRoomView:onTouchEventPlayTypeClose(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._panelPlayModeArea:setVisible(false)
end

function CreateBoxRoomView:initNewPlayTypeNode(optionNode, newPlayTypeNodes)
    if not newPlayTypeNodes or not optionNode then
        return
    end
    for _, v in pairs(newPlayTypeNodes) do
        local nodeInfo = loadstring("return " .. v)()
        if nodeInfo then
            local nodeName = nodeInfo[1]
            local nodeTips = nodeInfo[2]
            local nodeFlag = nodeInfo[3]
            if optionNode:getName() == nodeName then
                if nodeTips and nodeTips ~= "" then
                    self:createBubbleOnNode(optionNode, nodeTips, 5)
                end
                if nodeFlag and nodeFlag ~= "" then
                    self:createFlagAfterNode(optionNode, nodeFlag)
                end
            end
        end
    end
end

function CreateBoxRoomView:checkNewPlayTypeSelect()
    if XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_NEW_TYPE_NOT_SELECTED_NOTICE, false) then
        return
    end
    local newPlayTypeNodes = GameNewTypeConfiger:getNewPlayTypeNodeByGameID(self._gameID)
    if newPlayTypeNodes and next(newPlayTypeNodes) then
        local bNewPlayTypeSelected = false
        for _, v in pairs(newPlayTypeNodes) do
            local nodeInfo = loadstring("return " .. v)()
            if nodeInfo then
                local nodeName = nodeInfo[1]
                for _,nodeTable in pairs(radioTable) do
                    for _, radioInfo in pairs(nodeTable) do
                        if radioInfo["node"]:isSelected() and radioInfo["node"]:isVisible() and radioInfo["node"]:getName() == nodeName then
                            bNewPlayTypeSelected = true
                            break
                        end
                    end
                    if bNewPlayTypeSelected then
                        break
                    end
                end
            
                for _, checkBoxInfo in pairs(checkBoxTable) do
                    if checkBoxInfo["node"]:isSelected() and checkBoxInfo["node"]:isVisible() and checkBoxInfo["node"]:getName() == nodeName then
                        bNewPlayTypeSelected = true
                        break
                    end 
                end
            
                for _, mustExistRuleInfo in pairs(mustExistRuleTable) do
                    if mustExistRuleInfo["node"]:isSelected() and mustExistRuleInfo["node"]:isVisible() and mustExistRuleInfo["node"]:getName() == nodeName then
                        bNewPlayTypeSelected = true
                        break
                    end
                end
            end
            if bNewPlayTypeSelected then
                break
            end
        end
        if not bNewPlayTypeSelected and GameNewTypeConfiger:getCreateWithoutNewPlayTypeContentByGameID(self._gameID) ~= "" then
            XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK,
            }, GameNewTypeConfiger:getCreateWithoutNewPlayTypeContentByGameID(self._gameID))
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_NEW_TYPE_NOT_SELECTED_NOTICE, true)
            return true
        end
    end
    return
end

function CreateBoxRoomView:initNewPlayTypeUI(gameID)
    if not gameID then return end
    if GameNewTypeConfiger:getShowExtraRuleInCreate(gameID) then
        self._btnNewPlayTypeInterduce:setVisible(true)
    else
        self._btnNewPlayTypeInterduce:setVisible(false)
    end
    if self:isShowNewPlayType(gameID) then
        self:showCreateTips(true)
    else
        self:showCreateTips(false)
    end
end

function CreateBoxRoomView:onTouchEventNewPlayTypeInterduce(send,eventType)
    XH.viewManager:openView("NewPlayTypeExtraRuleView", nil, self._gameID)
end

function CreateBoxRoomView:showCreateTips(show)
    if not show then
        self._createTips:setVisible(false)
        return
    end
    local createTips = GameNewTypeConfiger:getCreateTipsByGameIDAndScene(self._gameID, "Box")
    if createTips and createTips ~= "" then
        local KW_MAX_TIPS_LENGTH = 24
        local whiteHeight = 59
        local whiteWidth = 49
        local whiteStartPosY = 17
        createTips = XH.StringTool.newLineStringByMaxLen(createTips, KW_MAX_TIPS_LENGTH)
        self._createTipsStr:setString(createTips)
        local tipsSize = self._createTipsStr:getContentSize()
        self._createTips:setContentSize(tipsSize.width + whiteWidth, tipsSize.height + whiteHeight )
        self._createTips:setVisible(true)
        self._createTipsStr:setPositionY(tipsSize.height + whiteHeight - whiteStartPosY)
    end
end

function CreateBoxRoomView:createFlagAfterNode(node, flag)
    if not node or not flag or flag == "" then
        return
    end
    local flagNode
    local ok = pcall(function()
        flagNode = ccui.ImageView:create(flag, ccui.TextureResType.plistType)
    end)
    if not ok then
        return
    end
    local textNode = XH.UITool.seekWidgetByName(node, KW_OPTION_ITEM_TEXT)
    local queryNode = XH.UITool.seekNodeByName(node, KW_PANEL_TIPS)
    if textNode then
        local whiteWidth = 5
        local textSize = textNode:getContentSize()
        local flagSize = flagNode:getContentSize()
        local posX = textSize.width + flagSize.width / 2 + whiteWidth * 2
        if queryNode and queryNode:isVisible() then
            posX = posX + queryNode:getContentSize().width
        end
        flagNode:setPosition(posX, textSize.height / 2)
        flagNode:ignoreContentAdaptWithSize(true)
        flagNode:setName("HotFlag")
        flagNode:addTo(textNode)
    end
end

function CreateBoxRoomView:createBubbleOnNode(node, bubbleStr, showTime)
    if not node or not bubbleStr or bubbleStr == "" then
        return
    end
    if not self._tipsModel then
        return
    end
    local tipsNode = self._tipsModel:clone()
    local KW_MAX_TIPS_LENGTH = 18
    local whiteHeight = 59
    local whiteWidth = 49
    local whiteStartY = 17
    bubbleStr = XH.StringTool.newLineStringByMaxLen(bubbleStr, KW_MAX_TIPS_LENGTH)
    XH.UITool.setText(tipsNode, "KW_TXT_TIPS", bubbleStr)
    local tipsPosX = XH.UITool.getPosition(tipsNode, "KW_TXT_TIPS")
    local tipsSize = XH.UITool.getContentSize(tipsNode, "KW_TXT_TIPS")
    tipsNode:setContentSize(tipsSize.width + whiteWidth, tipsSize.height + whiteHeight )
    XH.UITool.setPosition(tipsNode, "KW_TXT_TIPS", cc.p(tipsPosX, tipsSize.height + whiteHeight - whiteStartY))
    local textNode = XH.UITool.seekWidgetByName(node, KW_OPTION_ITEM_TEXT)
    if textNode then
        local nodeSize = textNode:getContentSize()
        tipsNode:setName("KW_BUBBLE")
        tipsNode:addTo(textNode)
        tipsNode:setPosition(nodeSize.width / 2, nodeSize.height)
        if showTime then
            tipsNode:stopAllActions()
            tipsNode:runAction(
                cc.Sequence:create(
                    cc.DelayTime:create(showTime), 
                    cc.CallFunc:create(function()
                        tipsNode:setVisible(false)
                    end)
                )
            )
        end
    end
end

function CreateBoxRoomView:initPromoteTips(gameid)
    if PromoteConfig[XH.areaData:getLobbyID()] == nil then
        self._promoteTips:setVisible(false)
        return
    end
    if PromoteConfig[XH.areaData:getLobbyID()].isFree == false then
        self._promoteTips:setVisible(false)
        return
    end
    self._promoteTips:setVisible(XH.lobby:getModule("Promote"):isOnlineAct() and gameid == PromoteConfig[XH.areaData:getLobbyID()].GAMEID)
end

function CreateBoxRoomView:flushLimitFreeUI(isFree, next)
    self._KW_ACT_FREE_ALL:setVisible(isFree)
    self._KW_ACT_FREE_NEXT:setVisible(not isFree)
    self._KW_ACT_FREE_NEXT_1:setVisible(next == 1)
    self._KW_ACT_FREE_NEXT_2:setVisible(next == 2)
end

function CreateBoxRoomView:onEventFlushLimitFreeAct()
    self._KW_ACT_FREE_TIPS:setVisible(false)
    local info = XH.lobby:getModule("LimitFreeAct"):getActConfig()
    if not info or XH.lobby:getModule("LimitFreeAct"):isActEnd() then
        return
    end
    self._KW_PANEL_TIP_1:setVisible(false)
    self._KW_PANEL_TIP_2:setVisible(false)
    self._KW_ACT_FREE_TIPS:setVisible(true)
    if XH.lobby:getModule("LimitFreeAct"):isInFreeTime() then
        self:flushLimitFreeUI(true)
        return
    end
    self:flushLimitFreeUI(false, XH.lobby:getModule("LimitFreeAct"):getNextFreeTimeIdx())
end

function CreateBoxRoomView:flushLimitFreeText()
    local info = XH.lobby:getModule("LimitFreeAct"):getActConfig()
    if not info or XH.lobby:getModule("LimitFreeAct"):isActEnd() then
        return
    end
    if XH.lobby:getModule("LimitFreeAct"):isInFreeTime(self._gameID) then
        self._imgAreaCostHua:setVisible(true)
        self._imgAreaCostText:setVisible(true)
        self._imgAreaCostText:setString("限时免费")
    end
end

return CreateBoxRoomView� 