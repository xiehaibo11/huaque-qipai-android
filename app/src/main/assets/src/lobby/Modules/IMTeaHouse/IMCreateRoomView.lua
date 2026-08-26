local IMCreateRoomView = class("IMCreateRoomView", XH.ViewBase)
local MutiVersionGameConfig =  require("lobby.Config.MutiVersionGameConfig")

function IMCreateRoomView:getCSBPath()
    return "cocosStudio/TeaHouse/CSB/TeaHouseCreateRoomView.csb"
end

function IMCreateRoomView:getBindingInfo()
    return {
        ["_KW_IMG_TITLE_CREATE_ROOM"] = { varName = "_textTitleCreateRoom" },
        ["_KW_IMG_TITLE_CREATE_PLAYMODE"] = {  varName = "_textTitleCreatePlayMode" },
        ["_KW_BTN_CREATE_BOX_ROOM"] = { varName = "_btnCreate", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventTeaHouseCreateBoxRoom" },
        ["_KW_BTN_CREATE_PLAYMODE"] = {  varName = "_btnCreatePlayMode", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventTeaHouseCreatePlayMode" },
        ["_KW_BTN_BACK"] = {  varName = "_btnBack", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KW_BTN_MODEl"] = {  varName = "_btnModel", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventChooseGameDynamic" },
        ["_KW_RADIO_ITEM"] = {  varName = "_radioItem" },
        ["_KW_CHECK_BOX_ITEM"] = {  varName = "_checkBoxItem"},
        ["_KW_TEXT_FIELD_ITEM"] = {  varName = "_textFieldItem"},
        ["_KW_TEXT_COST_INFO"] = {  varName = "_costTextInfo" },
        ["_KW_PANEL_GAME_RULE_DYNAMIC"] = {  varName = "_gameRule"},
        ["_KW_PANEL_OPTIONS_ITEM"] = {  varName = "_optionItem" },
        ["_KW_LISTVIEW_CHOOSE_GAME"] = {  varName = "_listViewGames" },
        ["_KW_PANEL_ROOMCARD_NOTICE"] = {  varName = "_panelRoomCardNotice"},
        ["_KW_IMG_HUA"] = { varName = "_imgDiscountHua" },
        ["_KW_TEXT_COST_DISCOUNT"] = {  varName = "_costTextDiscount"},
        ["_KW_UPGRADE_TEXT"] = {  varName = "_upgradeLabel" },
        ["_KW_PANEL_NOTICE"] = {  varName = "_panelNotice" },
        ["_KW_FOUR_TO_TWO_TIP"] = { varName = "_tipFourToTwo"},
        ["_KW_FOUR_TO_TWO_TIP_2"] = { varName = "_tipFourToTwo2"},
        ["_KW_COSTCARD_NOTICE"] = {varName = "_tipCreatNotice"},
        ["_KW_BTN_PLAYMODE_AREA"] = {varName = "_btnPlayModeArea", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventPlayModeAreaChange" },
        ["_KW_PANEL_SELECT_PLAYMODE_AREA"] = {varName = "_panelPlayModeArea" , },
        ["_KW_PANEL_DEFAULT_ITEM"] = {varName = "_panelDefaultItem"},
        ["_KW_NODE_MODULE_1"] = {  varName = "_nodeItem1",  },
        ["_KW_NODE_MODULE_2"] = {  varName = "_nodeItem2",  },
        ["_KW_NODE_MODULE_3"] = {  varName = "_nodeItem3",  },
        ["_KW_NODE_MODULE_4"] = {  varName = "_nodeItem4",  },
        ["_KW_NODE_MODULE_5"] = {  varName = "_nodeItem5",  },
        ["_KW_NODE_MODULE_6"] = {  varName = "_nodeItem6",  },
        ["_KW_NODE_MODULE_7"] = {  varName = "_nodeItem7",  },
        ["_KW_NODE_MODULE_8"] = {  varName = "_nodeItem8",  },
        ["_KW_NODE_MODULE_9"] = {  varName = "_nodeItem9",  },
        ["_KW_PLAYMODE_AREA_NOW"] = {varName = "_textPlayModeName"},
        ["_KW_BTN_PLAYTYPE_CLOSE"] = {varName = "_btnPlayTypeClose",type = XH.UI_TYPE.BUTTON,onTouchEnded = "onTouchEventPlayTypeClose" },
        ["_KW_BTN_PLAYTYPE_SURE"] = {varName = "_btnPlayTypeSave",type = XH.UI_TYPE.BUTTON ,onTouchEnded = "onTouchEventPlayTypeSave" },
        ["_KW_LIMIT_TIME_ACT"] = {varName = "_imgLimitAct"},
        ["_KW_IMG_AREA_COST_HUA"] = {varName = "_imgAreaCostHua"},
        ["_KW_AREA_COST_TEXT"] = {varName = "_imgAreaCostText"},
        ["_KW_PANEL_GROUP"] = {varName = "_groupPanel"},
        ["_KW_GROUP_BTN_CREATE_ROOM"] = {varName = "_groupNormalCreate",type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventTeaHouseCreateBoxRoom"},
        ["_KW_GROUP_BTN_QUICK_CREATE"] = {varName = "_groupQuickCreate",type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventTeaHouseQuickCreate"},
        ["_KW_TEXT_COST_INFO_GROUP"] = {varName = "_costTextInfo2"}
    }
end

local KW_BTN_MODEl_TEXT = "KW_TEXT_MODEl"

local KW_OPTION_ITEM_TEXT = "KW_OPTION_ITEM_TEXT"
local KW_ITEM_TEXT = "KW_ITEM_TEXT"
local KW_PANEL_TIPS = "KW_PANEL_TIPS"
local KW_PIC_TIPS_PAOPAO = "KW_PIC_TIPS_PAOPAO"
local KW_TEXT_TIPS = "KW_TEXT_TIPS"

local KW_IMG_ACTIVITY = "KW_IMG_ACTIVITY"
local KW_IMG_LEFT_TIME = "KW_IMG_LEFT_TIME"
local KW_TEXT_LFETTIME = "KW_TEXT_LFETTIME"

local KW_PLAYMODE_STATUS = "KW_PLAYMODE_STATUS"
local KW_PLAYMODE_NAME = "KW_PLAYMODE_NAME"

--一行不同数量选项时的按钮间隔
local buttonPosition = {
    { 230 },
    { 230, 675 },
    { 230, 675, 1120 },
    { 230, 527, 824, 1120 },
    { 230, 452.5, 675, 897.5, 1120 }
}

local TextFieldPosition = 190   --输入框x坐标

local OptionfntSize = {
    normal = 40,
    small = 34,
    extraSmall = 28
}

--单选节点保存
local radioTable = {}
--复选节点保存
local checkBoxTable = {}
--输入框节点保存
local inputBoxTable = {}
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

--根据key值大小顺序遍历table表
local pairsByKeys = function(t)
    if t == nil then t = {} end
    local a = {}
    for n in pairs(t) do
        a[#a + 1] = n
    end
    table.sort(a, function(x, y)
        return tonumber(string.match(x, "%d+")) < tonumber(string.match(y, "%d+"))
    end
    )
    local i = 0
    return function()
        i = i + 1
        return a[i], t[a[i]]
    end
end

function IMCreateRoomView:ctor(data)
    IMCreateRoomView.super.ctor(self)
    self._gameID = 0
    self._payModeType = data.payModeType
    self._payTypeTable = data.payTypeTable
    self._nGroupId = data.nGroupId
    self._timeStamp = os.time()

    self._modeItems = {}
    self._tmpGameID = nil
    self._mutiGameTable = nil
    self._tmpGameName = nil
    self._tmpLobbyID = nil
    self._tmpMutiVersionGame = nil
    self._isMutiVersionGame = false
    self:init()
end

function IMCreateRoomView:init()
    self:initCacheInfoKey()
    self:initTitleAndCreateBtn()
    self:initUIAndEvents()
    self:initGroupUI()
end

function IMCreateRoomView:initGroupUI()
    local gameinfo  = cc.UserDefault:getInstance():getStringForKey("IMListLayer_Last_Game_Group_".. self._nGroupId,"")
    local isExist = gameinfo ~= ""
    self._panelNotice:setVisible(not isExist)
    self._panelRoomCardNotice:setVisible(not isExist)
    self._btnCreate:setVisible(not isExist)
    self._groupPanel:setVisible(isExist)
end

function IMCreateRoomView:initCacheInfoKey()
    self._keyCacheInfo = "_teaHouse"
end

function IMCreateRoomView:initUIAndEvents()
    self._radioItem:addEventListener(handler(self, self.onTouchEventRadio))
    self._checkBoxItem:addEventListener(handler(self, self.onTouchEventCheckBox))

    local radioText = ccui.Helper:seekNodeByName(self._radioItem, KW_OPTION_ITEM_TEXT)
    radioText:addTouchEventListener(handler(self, self.onTouchRadioOptionText))
    local checkBoxText = ccui.Helper:seekNodeByName(self._checkBoxItem, KW_OPTION_ITEM_TEXT)
    checkBoxText:addTouchEventListener(handler(self, self.onTouchCheckBoxOptionText))

    self._listViewGames:setScrollBarEnabled(false)
    self._panelRoomCardNotice:setVisible(true)
    self:initListViewGames()

    self:adaptForLiuHai()
end

function IMCreateRoomView:adaptForLiuHai()
    local nodeListName = { "_KW_LISTVIEW_CHOOSE_GAME", "KW_IMG_SPLIT_LINE" }
    local nodeList = {}
    for _, name in pairs(nodeListName) do
        local tmpNode = ccui.Helper:seekNodeByName(self, name)
        nodeList[#nodeList + 1] = tmpNode
    end
    XH.UITool.adaptForLiuHai(nodeList)
end

function IMCreateRoomView:initListViewGames()
    local gameIdList = clone(XH.areaData:getGroupRoomGameList())
    local gameNameList = clone(XH.areaData:getAreaGameNameList())
    XH.playerData:screenTeaHouseGame(gameIdList)

    if #gameIdList == 0 then
        self._upgradeLabel:setVisible(true)
        self._panelRoomCardNotice:setVisible(false)
        self._btnCreate:setVisible(false)
        self._panelNotice:setVisible(false)
    end
    
    if gameIdList and gameNameList and self._btnModel then
        local firstGameId = 0
        for _, gameid in pairs(gameIdList) do
            self:addGameBtnOnce(gameNameList[gameid], gameid)
            if firstGameId == 0 then
                firstGameId = gameid
            end
        end
        self:setDefaultSelectGameDynamic(firstGameId)
        self:checkSelectGameDynamicLegal()
    end
end

function IMCreateRoomView:addGameBtnOnce(textStr, gameId)
    local newItem = self._btnModel:clone()
    local tempText = newItem:getChildByName(KW_BTN_MODEl_TEXT)
    tempText:setText(textStr)
    newItem:setTag(gameId)
    newItem:setName(gameId)
    self._listViewGames:addChild(newItem)
end

function IMCreateRoomView:initTitleAndCreateBtn()
    self._textTitleCreateRoom:setVisible(true)
    self._btnCreate:setVisible(true)
    self._textTitleCreatePlayMode:setVisible(false)
    self._btnCreatePlayMode:setVisible(false)
    self._tipCreatNotice:setString("注：房卡在完成第一局游戏后扣除，\n提前解散不扣房卡")
end

--选定一个游戏，动态加载游戏规则
function IMCreateRoomView:onTouchEventChooseGameDynamic(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._btnPlayModeArea:setVisible(false)
    local gameID = send:getTag()
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
        local color = isSelect and cc.c3b(255, 251, 205) or cc.c3b(163, 111, 72)
        local tempText = btn:getChildByName(KW_BTN_MODEl_TEXT)
        tempText:setTextColor(color)
        tempText:setFontSize(fontSize)
        btn:setEnabled(not isSelect)
    end

    self._gameID = gameID
    XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_CHOOSE_GAME_ID, gameID, { keyCacheInfo = self._keyCacheInfo })

    self._categoriesIndex = nil
    --根据游戏id动态加载规则选项
    self:loadGameRuleDynamic(gameID,self._tmpLobbyID)
    print("TouchChooseGame")
end

--初始化保存的节点表和联动值等
function IMCreateRoomView:clearNodetable()
    radioTable = {}
    checkBoxTable = {}
    inputBoxTable = {}
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

function IMCreateRoomView:getXmlKey(xmlString)
    local tableKey = {}
    local head = "</".."(.-)"..">"
    local num0
    local num1 = 1
    local tmpItem
    while(true) do
        num0, num1, tmpItem = string.find(xmlString, head, num1)
        if num0 == nil and num1 == nil or tmpItem == "userDefaultRoot" then
            break
        end
        table.insert(tableKey, tmpItem)
    end
    return tableKey
end

function IMCreateRoomView:getGameidAndkeyCacheInfo()
    local extraParam = {gameID = self._gameID, keyCacheInfo = self._keyCacheInfo}
    return extraParam
end

--根据游戏id动态加载规则选项
function IMCreateRoomView:loadGameRuleDynamic(gameid,lobbyid)
    if self._gameRule and self._optionItem then
        self._gameRule:removeAllChildren()
        self:clearNodetable()
        local gameRuleData = self:getTeaHouseGameRuleList(gameid,lobbyid)
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
        local gameruleExtraParam = self:getGameidAndkeyCacheInfo()
        local gameRuleVersionCur = XH.userDefault:getValue(XH.userDefault.KEY_ID.GAMERULE_VERSION, 0, gameruleExtraParam) 
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
        XH.userDefault:setValue(XH.userDefault.KEY_ID.GAMERULE_VERSION, tonumber(self._gameRuleVersion), gameruleExtraParam) 

        --该显示的所有选项表
        local showOptiondsTable
        --选择大类
        if gameRuleData["categories"] then
            if self._categoriesIndex then
                showOptiondsTable = gameRuleData["categories"][self._categoriesIndex]
            else
                showOptiondsTable = gameRuleData["categories"][gameRuleData["defaultchoose"]]
                local savedCategoriesIndex = XH.userDefault:getValue(XH.userDefault.KEY_ID.GAMERULE_CATEGORIE_SELECT, 0 , self:getGameidAndkeyCacheInfo() )
                if savedCategoriesIndex ~= 0 then
                    self._categoriesIndex = savedCategoriesIndex
                    showOptiondsTable = gameRuleData["categories"][savedCategoriesIndex]
                end
            end
        else
            showOptiondsTable = gameRuleData
        end

        for playType, playTypeData in pairsByKeys(showOptiondsTable) do
            if playTypeData["type"] == "radio" then
                --同个类型的单选框存在同一个table中，即人数的节点存在同一个
                radioTable[#radioTable + 1] = {}
            end
            for line, _ in pairsByKeys(playTypeData["ctrls"]) do
                local newOptionItem
                if playTypeData["type"] == "radio" then
                    newOptionItem = self._optionItem:clone()
                    if playTypeData["nodeName"] == "playCount" or playTypeData["nodeName"] == "playerCount" then
                        newOptionItem:setName(playTypeData["nodeName"] .. line)
                    else
                        newOptionItem:setName(playTypeData["nodeName"])
                    end
                    self._gameRule:addChild(newOptionItem)
                    self:setOptions(playTypeData, newOptionItem, line, playType)
                elseif playTypeData["type"] == "checkbox" then
                    newOptionItem = self._optionItem:clone()
                    if playTypeData["nodeName"] == "playCount" or playTypeData["nodeName"] == "playerCount" then
                        newOptionItem:setName(playTypeData["nodeName"] .. line)
                    else
                        newOptionItem:setName(playTypeData["nodeName"])
                    end
                    self._gameRule:addChild(newOptionItem)
                    self:setOptions(playTypeData, newOptionItem, line, playType)
                elseif playTypeData["type"] == "textField" then
                    newOptionItem = self._optionItem:clone()
                    newOptionItem:setName(playTypeData["nodeName"])
                    self._gameRule:addChild(newOptionItem)
                    self:setTextField(playTypeData, newOptionItem, line)
                end
                local tempText = ccui.Helper:seekNodeByName(newOptionItem, KW_ITEM_TEXT)
                if not playTypeData["text"] or playTypeData["text"] == "" then
                    tempText:setVisible(false)
                else
                    tempText:setText(playTypeData["text"])
                    tempText:setVisible(tonumber(line) == 1)
                end
            end
        end
    end
    --判断本地是否缓存了按钮选择
    local gameruleCacheExistExtraParam = { gameID = self._gameID, categoriesIndex = self._categoriesIndex, keyCacheInfo = self._keyCacheInfo }
    local isCacheGameRule = XH.userDefault:getValue(XH.userDefault.KEY_ID.GAMERULE_CACHE_EXIST, false, gameruleCacheExistExtraParam)
    if isCacheGameRule then
        --加载完选项之后更新和联动值相关的节点
        self:updateLinkageNodeBylinkageLevelValue()
    else
        --加载默认选项
        self:setDefaultOptions() 
    end
    self:updateCostTypeLine()
    --记录本地是否缓存了按钮选择
    XH.userDefault:setValue(XH.userDefault.KEY_ID.GAMERULE_CACHE_EXIST,  true, gameruleCacheExistExtraParam)
    --更新房卡消耗的显示
    self:updateShowCostInfo()
    --刷新列表容器视图
    self._gameRule:refreshView()
end

function IMCreateRoomView:getTeaHouseGameRuleList(gameid,lobbyid)
    local gameRuleList = XH.ReadGameListAndRuleConfig.loadTeaHouseGameRule(lobbyid or XH.areaData:getLobbyID(), gameid)
    if not gameRuleList then
        return
    end
    if not gameRuleList.categories or not gameRuleList.categories[1] then
        return gameRuleList
    end
    --可以选择消耗方式的区才根据消耗方式来显示，否则根据json配置表显示
    if self._payModeType == XH.TeaHouseProtocol.ReqSetPayType.PayModePype.TYPE_PAY_MODEL_FRON then
        for categoriesIndex, categories in ipairs(gameRuleList["categories"]) do
            for nodeStr, nodeData in pairs(categories) do
                if (nodeData.nodeName and nodeData.nodeName == "costType") or string.find(nodeStr, "costType") or string.find(nodeStr, "CostType") or string.find(nodeStr, "costtype") then
                    gameRuleList.categories[categoriesIndex][nodeStr] = self:createPayTypeConfInFixedPayType(gameRuleList.categories[categoriesIndex][nodeStr])
                    break
                end
            end
        end
    end
    return gameRuleList
    -- for categoriesIndex, categories in ipairs(gameRuleList["categories"]) do
    --     for nodeStr, nodeData in pairs(categories) do
    --         if (nodeData.nodeName and nodeData.nodeName == "costType") or string.find(nodeStr, "costType") or string.find(nodeStr, "CostType") or string.find(nodeStr, "costtype") then
    --             gameRuleList.categories[categoriesIndex][nodeStr] = nil
    --             break
    --         end
    --     end
    --     local bHavePayType, payTypeData = self:createPayTypeConf()
    --     if bHavePayType then
    --         gameRuleList.categories[categoriesIndex]["100payTypeData"] = payTypeData
    --     end

    --     local bHaveRoomRate, roomRateData = self:createRoomRateConf()
    --     if bHaveRoomRate then
    --         gameRuleList.categories[categoriesIndex]["101roomRate"] = roomRateData
    --     end
    -- end
    -- return gameRuleList
end

function IMCreateRoomView:createRoomRateConf()
    if not self._payTypeTable then
        return false, nil
    end
    if XH.LOBBY_ID.SHAOXING3D == XH.areaData:getLobbyID() then
        return false, nil
    end
    local bFangFeiMode = false
    for _, v in ipairs(self._payTypeTable) do
        if v == XH.TeaHouseProtocol.ReqSetPayType.PayType.TYPE_AGENT_PLAYER_AA
        or v == XH.TeaHouseProtocol.ReqSetPayType.PayType.TYPE_AGENT_PLAYER_WINNER then
            bFangFeiMode = true
            break
        end
    end
    if not bFangFeiMode then
        return false, nil
    end
    local roomRateData = {
        text = XH.StringTool.replaceMatchStr("圈子卡"),
        nodeName = "roomRate",
        type = "textField",
        ctrls = {
            [1] = {
                {
                    nodeName = "roomCostInput",
                    ruleName = "chessfee",
                    ruleText = XH.StringTool.replaceMatchStr("圈子卡"),
                    decimalPlaces = 2,
                    defaultInput = 0,
                    text = "",
                    haveTips = XH.StringTool.replaceMatchStr("在输入框内输入您想设置的圈子卡")
                }
            }
        }
    }
    return true, roomRateData
end

function IMCreateRoomView:createPayTypeConfInFixedPayType(payTypeData)
    local payData = {}
    local pay1 = {}
    pay1.costType = "aaCost" 
    pay1.nodeName = "PayType='22';"
    pay1.text = "平摊消耗"
    local pay2 = {}
    pay2.costType = "allCost" 
    pay2.nodeName = "PayType='23';"
    pay2.text = "冠军消耗"
    table.insert(payData,pay2)
    table.insert(payData,pay1)
    if not payTypeData.defaultchoose[1] then
        payTypeData.defaultchoose[1] = "PayType='23';"
    end
    payTypeData.ctrls["1"] = payData
    return payTypeData
end

function IMCreateRoomView:createPayTypeConf()
    local payTypeData = {
        text = "消耗",
        nodeName = "costType",
        type = "radio",
        defaultchoose = {
        },
        ctrls = {
            [1] = {
            }
        }
    }
    if self._payTypeTable then
        for _, v in ipairs(self._payTypeTable) do
            if v == XH.TeaHouseProtocol.ReqSetPayType.PayType.TYPE_TEA_OWNER then
                local tempPayData = {
                    nodeName = "PayType='0';",
                    text = "冠军消耗",
                    costType = "allCost"
                }
                if self._payModeType and self._payModeType == XH.TeaHouseProtocol.ReqSetPayType.PayModePype.TYPE_PAY_MODEL_FRON then
                    tempPayData.text = "冠军消耗"
                end
                table.insert(payTypeData.ctrls[1], tempPayData)
                if not payTypeData.defaultchoose[1] then
                    payTypeData.defaultchoose[1] = "PayType='0';"
                end
            elseif v == XH.TeaHouseProtocol.ReqSetPayType.PayType.TYPE_TEA_PLAYER_AA then
                local tempPayData = {
                    nodeName = "PayType='1';",
                    text = "平摊消耗",
                    costType = "aaCost"
                }
                table.insert(payTypeData.ctrls[1], tempPayData)
                if not payTypeData.defaultchoose[1] then
                    payTypeData.defaultchoose[1] = "PayType='1';"
                end
            elseif v == XH.TeaHouseProtocol.ReqSetPayType.PayType.TYPE_TEA_ROOM_OWNER then
                local tempPayData = {
                    nodeName = "PayType='6';",
                    text = "房主消耗",
                    costType = "allCost"
                }
                table.insert(payTypeData.ctrls[1], tempPayData)
                if not payTypeData.defaultchoose[1] then
                    payTypeData.defaultchoose[1] = "PayType='6';"
                end
            elseif v == XH.TeaHouseProtocol.ReqSetPayType.PayType.TYPE_TEA_PLAYER_OWNER_CARD_AA then
                local tempPayData = {
                    nodeName = "PayType='7';",
                    text = "平摊消耗",
                    costType = "aaCost"
                }
                table.insert(payTypeData.ctrls[1], tempPayData)
                if not payTypeData.defaultchoose[1] then
                    payTypeData.defaultchoose[1] = "PayType='7';"
                end
            elseif v == XH.TeaHouseProtocol.ReqSetPayType.PayType.TYPE_AGENT_PLAYER_AA then
                local tempPayData = {
                    nodeName = "PayType='20';",
                    text = "平摊消耗",
                    costType = "allCost", --均由领队付房卡
                    --下面是宁波需要加的字段
                    mustExistRule = "msutRule", 
                    mustExistRuleYes = "PlaymentType='1';"
                }
                --绍兴3d的名字和其他地区不统一
                if XH.LOBBY_ID.SHAOXING3D == XH.areaData:getLobbyID() then
                    tempPayData.text = "冠军消耗"
                    --绍兴领队支付放到最前
                    table.insert(payTypeData.ctrls[1], 1, tempPayData)
                else
                    table.insert(payTypeData.ctrls[1], tempPayData)
                end
                if not payTypeData.defaultchoose[1] then
                    payTypeData.defaultchoose[1] = "PayType='20';"
                end
            elseif v == XH.TeaHouseProtocol.ReqSetPayType.PayType.TYPE_AGENT_PLAYER_WINNER then
                local tempPayData = {
                    nodeName = "PayType='21';",
                    text = "冠军消耗",
                    costType = "allCost", --均由领队付房卡
                    --下面是宁波需要加的字段
                    mustExistRule = "msutRule", 
                    mustExistRuleYes = "PlaymentType='3';"
                }
                table.insert(payTypeData.ctrls[1], tempPayData)
                if not payTypeData.defaultchoose[1] then
                    payTypeData.defaultchoose[1] = "PayType='21';"
                end
            elseif v == XH.TeaHouseProtocol.ReqSetPayType.PayType.TEA_PLAYER_OWNER_TEACARD_AA then
                local tempPayData = {
                    nodeName = "PayType='22';",
                    text = "平摊消耗",
                    costType = "aaCost", 
                }
                table.insert(payTypeData.ctrls[1], tempPayData)
                if not payTypeData.defaultchoose[1] then
                    payTypeData.defaultchoose[1] = "PayType='22';"
                end
            elseif v == XH.TeaHouseProtocol.ReqSetPayType.PayType.TEA_PLAYER_OWNER_TEACARD_WIN then
                local tempPayData = {
                    nodeName = "PayType='23';",
                    text = "冠军消耗",
                    costType = "allCost", 
                }
                table.insert(payTypeData.ctrls[1], tempPayData)
                if not payTypeData.defaultchoose[1] then
                    payTypeData.defaultchoose[1] = "PayType='23';"
                end
            elseif v == XH.TeaHouseProtocol.ReqSetPayType.PayType.TYPE_TEA_OWNER_YUFU_MODE_AA then
                local tempPayData = {
                    nodeName = "PayType='999';PrevAA=1;",
                    text = "平摊消耗",
                    costType = "aaCost", 
                }
                table.insert(payTypeData.ctrls[1], tempPayData)
                if not payTypeData.defaultchoose[1] then
                    payTypeData.defaultchoose[1] = "PayType='0';"
                end
            elseif v == XH.TeaHouseProtocol.ReqSetPayType.PayType.TEA_AA then
                local tempPayData = {
                    nodeName = "PayType='24';",
                    text = "平摊消耗",
                    costType = "aaCost"
                }
                table.insert(payTypeData.ctrls[1], tempPayData)
                if not payTypeData.defaultchoose[1] then
                    payTypeData.defaultchoose[1] = "PayType='24';"
                end
            end
        end
        return true, payTypeData
    end
    return false, nil
end

function IMCreateRoomView:setTextField(playTypeData, newOptionItem, line)
    if not self._textFieldItem then
        return 
    end
    local lineData = playTypeData["ctrls"][line]
    local tmpCount = #lineData
    local optionNode = self._textFieldItem:clone()
    for optionIndex = 1, tmpCount do
        local tempText = ccui.Helper:seekNodeByName(optionNode, "KW_OPTION_ITEM_TEXT")
        if tempText then
            tempText:setText(lineData[optionIndex]["text"] or "")
        end

        --判断是否按钮有提示信息
        if lineData[optionIndex]["haveTips"] then
            local tempOptionText = ccui.Helper:seekNodeByName(optionNode, KW_OPTION_ITEM_TEXT)
            local tempTipText = ccui.Helper:seekNodeByName(optionNode, KW_TEXT_TIPS)
            local tempTipPaoPao = ccui.Helper:seekNodeByName(optionNode, KW_PIC_TIPS_PAOPAO)
            tempTipText:setText(lineData[optionIndex]["haveTips"])

            local optionTextSize = tempOptionText:getContentSize()
            local tipTextSize = tempTipText:getContentSize()

            tempTipPaoPao:setContentSize(tipTextSize.width + 40, tipTextSize.height + 40)
            tempTipText:setPositionY((tipTextSize.height + 40) / 2)

            local tempTipsPanel = ccui.Helper:seekNodeByName(optionNode, KW_PANEL_TIPS)
            tempTipsPanel:setVisible(true)
            tempTipsPanel:setPositionX(optionTextSize.width + 10)
            tempTipsPanel:addTouchEventListener(handler(self, self.onTouchEventTipsBtn))
        end

        local editBox = ccui.EditBox:create(cc.size(210,55), "tea_house_createroom_textfield.png")
        if editBox then
            editBox:setAnchorPoint(cc.p(0, 0.5))
            editBox:setPosition(cc.p(20, 40))
            editBox:setFontSize(40)
            editBox:setFontColor(cc.c3b(255,250,235))
            editBox:setText(tostring(lineData[optionIndex]["defaultInput"] or 0))
            editBox:setInputMode(cc.EDITBOX_INPUT_MODE_NUMERIC)
            editBox:registerScriptEditBoxHandler(function(eventName)
                if eventName == "changed" then
                    local editBoxText = editBox:getText()
                    if editBoxText ~= nil then
                        local tempTextArr = string.split(editBoxText, ".")
                        local decimalPlaces = lineData[optionIndex]["decimalPlaces"] or 2
                        if #tempTextArr >= 2 and string.len(tempTextArr[#tempTextArr]) > decimalPlaces then
                            XH.TipTool.showToast("仅允许"..decimalPlaces.."位小数")
                            editBox:setText(string.sub(editBoxText, 1, -2))
                            return 
                        end
                    end
                end
            end)
            inputBoxTable[#inputBoxTable + 1] = {
                node = editBox,
                nodeConfig = lineData[optionIndex]
            }
            optionNode:addChild(editBox)
        end

        newOptionItem:addChild(optionNode, 1)
        optionNode:setPosition(cc.p(TextFieldPosition, 50))
        optionNode:setName(lineData[optionIndex]["nodeName"])
    end
end

--动态加载单个选项（单选复选）
function IMCreateRoomView:setOptions(playTypeData, newOptionItem, line, keyName)
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
                local tempText = ccui.Helper:seekNodeByName(optionNode, KW_OPTION_ITEM_TEXT)
                if #lineData[optionIndex]["text"] > 24 then
                    tempText:setFontSize(OptionfntSize.small)
                end
                tempText:setText(lineData[optionIndex]["text"])
                --保存单选框节点
                local tmp = {}
                tmp["node"] = optionNode
                tmp["nodeConfig"] = lineData[optionIndex]
                table.insert(radioTable[#radioTable], tmp)

                if lineData[optionIndex]["categorieIndex"] then
                    if categorieTable[#categorieTable + 1] == nil then
                        categorieTable[#categorieTable + 1] = {}
                    end
                    categorieTable[#categorieTable]["node"] = optionNode
                    categorieTable[#categorieTable]["nodeConfig"] = lineData[optionIndex]
                end
            elseif playTypeData["type"] == "checkbox" then
                optionNode = self._checkBoxItem:clone()
                local tempText = ccui.Helper:seekNodeByName(optionNode, KW_OPTION_ITEM_TEXT)
                if #lineData[optionIndex]["text"] > 24 then
                    tempText:setFontSize(OptionfntSize.small)
                end
                tempText:setText(lineData[optionIndex]["text"])
                --保存复选框节点
                if checkBoxTable[#checkBoxTable + 1] == nil then
                    checkBoxTable[#checkBoxTable + 1] = {}
                end
                checkBoxTable[#checkBoxTable]["node"] = optionNode
                checkBoxTable[#checkBoxTable]["nodeConfig"] = lineData[optionIndex]
            end
            --判断是否按钮有提示信息
            if lineData[optionIndex]["haveTips"] then
                local tempOptionText = ccui.Helper:seekNodeByName(optionNode, KW_OPTION_ITEM_TEXT)
                local tempTipText = ccui.Helper:seekNodeByName(optionNode, KW_TEXT_TIPS)
                local tempTipPaoPao = ccui.Helper:seekNodeByName(optionNode, KW_PIC_TIPS_PAOPAO)
                tempTipText:setText(lineData[optionIndex]["haveTips"])

                local optionTextSize = tempOptionText:getContentSize()
                local tipTextSize = tempTipText:getContentSize()

                tempTipPaoPao:setContentSize(tipTextSize.width + 40, tipTextSize.height + 40)
                tempTipText:setPositionY((tipTextSize.height + 40) / 2)

                local tempTipsPanel = ccui.Helper:seekNodeByName(optionNode, KW_PANEL_TIPS)
                tempTipsPanel:setVisible(true)
                tempTipsPanel:setPositionX(optionTextSize.width + 10)
                tempTipsPanel:addTouchEventListener(handler(self, self.onTouchEventTipsBtn))

                if optionIndex ~= tmpCount then
                    optionNode:setLocalZOrder(tmpCount - optionIndex + 1)
                else
                    if optionIndex >= 3 then
                        local xPosition = tempTipPaoPao:getPositionX()
                        tempTipPaoPao:setPositionX(xPosition - (tipTextSize.width + 100))
                    end
                end
            end
            --保存条件，联动和联动相关等需要保存的节点
            self:saveCanChangeNode(lineData, optionIndex, optionNode)

            newOptionItem:addChild(optionNode)
            optionNode:setPosition(cc.p(buttonPosition[tmpCount][optionIndex], 50))
            optionNode:setName(lineData[optionIndex]["nodeName"])
            local isSelected = self:getCacheButtonState(optionNode:getName())
            self:setSelectState(optionNode, isSelected)
        end
    end
end

--保存条件，联动和联动相关等需要保存的节点
function IMCreateRoomView:saveCanChangeNode(lineData, optionIndex, optionNode)
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
        table.insert(linkageLevelTable[lineData[optionIndex]["linkageLevel"]], optionNode)
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
        table.insert(unSelectlinkageLevelTable[lineData[optionIndex]["unSelectlinkageLevel"]], optionNode)
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
function IMCreateRoomView:setDefaultOptions()
    local gameRuleData = self:getTeaHouseGameRuleList(self._gameID,self._tmpLobbyID)
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
                local optionNode = ccui.Helper:seekNodeByName(self._gameRule, nodeName)
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
function IMCreateRoomView:setDefaultSelectGameDynamic(defaultGameId)
    local gameID = XH.userDefault:getValue(XH.userDefault.KEY_ID.LAST_CHOOSE_GAME_ID, defaultGameId, { keyCacheInfo = self._keyCacheInfo })
    self:setSelectGameDynamic(gameID)
end

function IMCreateRoomView:setSelectGameDynamic(gameId)
    local gameBtnNode = ccui.Helper:seekNodeByName(self._listViewGames, gameId)
    if gameBtnNode then
        self:onTouchEventChooseGameDynamic(gameBtnNode, ccui.TouchEventType.ended)
    end
end

function IMCreateRoomView:checkSelectGameDynamicLegal()
    local isLegal = false
    for _, value in pairs(self._listViewGames:getChildren()) do
        if value then
            if not value:isEnabled() then
                isLegal = true
            end
        end
    end
    if  not isLegal then
        local gameIdList = XH.areaData:getTeaHouseRoomGameList()
        local firstGameId = gameIdList[1] or 0
        self:setSelectGameDynamic(firstGameId)
    end
end

--点击单选按钮
function IMCreateRoomView:onTouchEventRadio(send, eventType, isDefault)
    --加载默认选项时不需要音效
    if eventType == ccui.CheckBoxEventType.selected then
        --判断点击的是否是大类的按钮
        local isCategoriesBtn = false
        for _, v in pairs(categorieTable) do
            if v["node"] == send and isDefault == nil then
                --记录所选游戏的大类
                XH.userDefault:setValue(XH.userDefault.KEY_ID.GAMERULE_CATEGORIE_SELECT, tonumber(v["nodeConfig"]["categorieIndex"]) , self:getGameidAndkeyCacheInfo())
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
function IMCreateRoomView:onTouchEventCheckBox(send, eventType, isDefault)
    --加载默认选项时不需要音效
    if eventType == ccui.CheckBoxEventType.selected then
        self:setSelectState(send, true)
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
function IMCreateRoomView:onTouchRadioOptionText(send, eventType)
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
function IMCreateRoomView:onTouchCheckBoxOptionText(send, eventType)
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
function IMCreateRoomView:onTouchEventTipsBtn(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end

    local tipsNode = ccui.Helper:seekNodeByName(send, KW_PIC_TIPS_PAOPAO)
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
function IMCreateRoomView:updateLinkageNodeBylinkageLevelValue()
    self:updateLinkageNode()
end

--因为比赛场内的消耗方式由领队设定，可能之前缓存选择的方式已经没了，导致消耗方式一个都没有选择
function IMCreateRoomView:updateCostTypeLine()
    local costTypeNode = ccui.Helper:seekNodeByName(self._gameRule, "costType")
    if not costTypeNode then
        return
    end
    local allChildren = costTypeNode:getChildren()
    local bSelect = false
    local firstVisibleNode --第一个可见的节点
    for i = 2, #allChildren do
        if allChildren[i]:isVisible() then
            if allChildren[i]:isSelected() then
                bSelect = true
                break
            else
                --记录第一个可见的节点
                if not firstVisibleNode  then
                    firstVisibleNode = allChildren[i]
                end
            end
        end
    end
    if not bSelect and firstVisibleNode then
        self:setSelectState(firstVisibleNode, true)
    end
end

--更新联动相关联节点
function IMCreateRoomView:updateLinkageNode(clicknode)
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
                self:setSelectState(node, isSelected)
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
                        self:setSelectState(node, false)
                        --隐藏按钮之后要选择的按钮设置为选中
                        if isSelect then
                            local needSelectNode = ccui.Helper:seekNodeByName(self._gameRule, hideSelectInfo["nodeConfig"]["hideSelect"])
                            if needSelectNode then
                                self:setSelectState(needSelectNode, true)
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
                        self:setSelectState(node, false)
                        --禁用按钮之后要选择的按钮设置为选中
                        if isSelect then
                            local needSelectNode = ccui.Helper:seekNodeByName(self._gameRule, prohibitSelectInfo["nodeConfig"]["prohibitSelect"])
                            if needSelectNode then
                                self:setSelectState(needSelectNode, true)
                            end
                        end
                    end
                end
                local childrenNode = node:getChildren()
                for i = 1, node:getChildrenCount() do
                    childrenNode[i]:setColor(cc.c3b(255, 255, 255))
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
                self:setSelectState(node, true)
                --处理禁用且选中自己的同时需要选中其他节点
                for _, prohibitSelectInfo in pairs(prohibitSelectTable) do
                    if prohibitSelectInfo["node"] == node then
                        --禁用按钮之后要选择的按钮设置为选中
                        if isSelect then
                            local needSelectNode = ccui.Helper:seekNodeByName(self._gameRule, prohibitSelectInfo["nodeConfig"]["prohibitSelect"])
                            if needSelectNode then
                                self:setSelectState(needSelectNode, true)
                            end
                        end
                    end
                end
                local childrenNode = node:getChildren()
                for i = 1, node:getChildrenCount() do
                    childrenNode[i]:setColor(cc.c3b(255, 255, 255))
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
                            self:setSelectState(node, false)
                            --禁用按钮之后要选择的按钮设置为选中
                            if isSelect then
                                local needSelectNode = ccui.Helper:seekNodeByName(self._gameRule, prohibitSelectInfo["nodeConfig"]["prohibitSelect"])
                                if needSelectNode then
                                    self:setSelectState(needSelectNode, true)
                                end
                            end
                        end
                    end
                    local childrenNode = node:getChildren()
                    for i = 1, node:getChildrenCount() do
                        childrenNode[i]:setColor(cc.c3b(255, 255, 255))
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
            prohibitMeanwhileCount = prohibitMeanwhileCount + 1
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
                    self:setSelectState(node, false)
                    --禁用按钮之后要选择的按钮设置为选中
                    if isSelect then
                        local needSelectNode = ccui.Helper:seekNodeByName(self._gameRule, prohibitSelectInfo["nodeConfig"]["prohibitSelect"])
                        if needSelectNode then
                            self:setSelectState(needSelectNode, true)
                        end
                    end
                end
            end
            local childrenNode = node:getChildren()
            for i = 1, node:getChildrenCount() do
                childrenNode[i]:setColor(cc.c3b(255, 255, 255))
                childrenNode[i]:setTextColor(cc.c3b(160, 160, 160))
            end
        end
    end
end

--设置按钮选择状态
function IMCreateRoomView:setSelectState(node, isSelected, isCheckBox)
    node:setSelected(isSelected)
    local childrenNode = node:getChildren()
    for i = 1, node:getChildrenCount() do
        childrenNode[i]:setColor(cc.c3b(255, 255, 255))
        childrenNode[i]:setTextColor(cc.c3b(163, 111, 72))
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

function IMCreateRoomView:getCacheButtonStateExtraParam(nodeName)
    local extraParam = {}
    extraParam.gameID = self._gameID
    extraParam.categoriesIndex = self._categoriesIndex
    extraParam.nodeName = nodeName
    extraParam.keyCacheInfo = self._keyCacheInfo
    return extraParam
end

--缓存按钮选中状态
function IMCreateRoomView:setCacheButtonState(nodeName, isSelected)
    --去除xml不能出现的字符
    local extraParam = self:getCacheButtonStateExtraParam(nodeName)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.GAMERULE_CACHE_BUTTON_STATE, isSelected, extraParam)
end

--读取按钮选中状态
function IMCreateRoomView:getCacheButtonState(nodeName)
    --去除xml不能出现的字符
    local extraParam = self:getCacheButtonStateExtraParam(nodeName)
    return XH.userDefault:getValue(XH.userDefault.KEY_ID.GAMERULE_CACHE_BUTTON_STATE, false, extraParam)
end

local KW_BASE_SCORE_NAME_TABLE = { "basescore", "baseScore", "BaseScore", "Basescore", "base", "Base" } --所有区底分的字段名列表
local KW_PAYTYPE_NAME_TABLE = { "PayType", "moneyType", "TeaHousePayType" }

function IMCreateRoomView:onTouchEventTeaHouseCreateBoxRoom(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if XH.playerData:checkNewRealName(self._gameID) ~= 0 then return end--实名限制
    local data = {}
    local gameRule, bRightRule = self:getGameRule()
    if not bRightRule then
        return
    end
    local zhRule = self:getBoxRoomRuleString()
    zhRule = string.gsub(zhRule, "/", ",")
    local gameNameList =  XH.areaData:getAreaGameNameList()
    local boxName = gameNameList[self._gameID]
    data.gameid = self._gameID

    --设置底分
    local luaString = "return {" .. gameRule .. "}"
    local ruleTable = loadstring(luaString)()
    

    data.base = 1
    for _, baseScoreName in pairs(KW_BASE_SCORE_NAME_TABLE) do
        if ruleTable[baseScoreName] then
            data.base = tonumber(ruleTable[baseScoreName])
        end
    end

    data.cont = self:getPlayCount()
    data.type = 0
    local lobbyID = XH.areaData:getLobbyID()
    if tonumber(lobbyID) == XH.LOBBY_ID.YUYAO then
        local payType = tonumber(ruleTable["PlaymentType"]) or 1
        if payType == 1 then --余姚平摊模式增加字段
            gameRule = gameRule .. "AveragePay='1';"
        end
    end
    if tonumber(lobbyID) == XH.LOBBY_ID.YUYAO or tonumber(lobbyID) == XH.LOBBY_ID.NINGBO then
        local defauleInfo = "MaxChessWaitTime='10';MinFeeTip='0';DismissWaitTime='3';DismissWay='2';"  --创建时附带默认的游戏规则    
        gameRule = gameRule .. defauleInfo
    end
    
    if tonumber(lobbyID) == XH.LOBBY_ID.QUZHOU then
        data.type = (data.cont == 10000 or data.cont == 999) and 1 or 0
    end
    if tonumber(lobbyID) == XH.LOBBY_ID.ZHOUSHAN and (data.gameid == 30356 or data.gameid == 30357) then
        data.type = (data.cont == 100 or data.cont == 200) and 3 or 0
    end
    --宁波慈溪麻将特判
    if tonumber(lobbyID) == XH.LOBBY_ID.NINGBO then
        data.type = (data.cont == 1100) and 1 or 0
    end
    --嵊州麻将特判
    if tonumber(lobbyID) == XH.LOBBY_ID.SHAOXING then
        if data.cont > 10000 then
            data.cont = (data.cont % 10000) * 100
        end
    end
    data.size = self:getPlayerCount()
    data.name = boxName
    data.data = string.format("%sZhRule='%s';", gameRule, zhRule)
    data.tableCnt = 1
    for _, pt in pairs(XH.TeaHouseProtocol.ReqSetPayType.PayType) do
        for _, payTypeName in ipairs(KW_PAYTYPE_NAME_TABLE) do
            if string.find(gameRule, payTypeName .. "='" .. pt .. "'") then
                data.payType = pt
                break
            end
        end
    end
    if self._gameID == MutiVersionGameConfig.MutiVersionGameID.JiaXingMahjong 
    or self._gameID == MutiVersionGameConfig.MutiVersionGameID.TaiPaoMahjong then
        for _, v in pairs(categorieTable) do
            if v["nodeConfig"]["categorieIndex"] == ( self._categoriesIndex or 1) then
                local tmpRuleStr = loadstring("return {" .. v["nodeConfig"]["nodeName"] .. "}")()  
                if tmpRuleStr["gamedID"] then
                    data.gameid = tonumber(tmpRuleStr["gamedID"])
                end
            end
        end
    end
    data.nTeaid = self._nGroupId
    XH.lobby:getModule("Im"):getIMData():setEnterTeaHouseID(self._nGroupId)
    XH.lobby:getModule("Im"):createBoxRoom(data)
    self:close()
end

function IMCreateRoomView:onTouchEventTeaHouseCreatePlayMode(send, eventType)
    --不需要操作
end


function IMCreateRoomView:onTouchEventTeaHouseQuickCreate(send, eventType)
    -- XH.TipTool.showToast("对不起，快速创建暂时无法使用~")
    local gameinfo  = cc.UserDefault:getInstance():getStringForKey("IMListLayer_Last_Game_Group_".. self._nGroupId,"")
    local createInfo = json.decode(gameinfo)
    XH.lobby:getModule("Im"):getIMData():setEnterTeaHouseID(self._nGroupId)
    XH.lobby:getModule("Im"):createBoxRoom(createInfo)
    self:close()
end

--点击关闭按钮
function IMCreateRoomView:onTouchEventClose(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self:close()
end

--获取游戏规则
function IMCreateRoomView:getGameRule()
    local bRight = true
    local strGameRule = ""
    for _, nodeTable in pairs(radioTable) do
        for _, nodeInfo in pairs(nodeTable) do
            if nodeInfo["node"]:isSelected() and nodeInfo["node"]:isVisible() then
                local ruleString = nodeInfo["node"]:getName()
                if string.sub(ruleString, #ruleString, #ruleString) == ";" then
                    strGameRule = strGameRule .. nodeInfo["node"]:getName()
                end
            end
        end
    end

    for _, checkBoxInfo in pairs(checkBoxTable) do
        if checkBoxInfo["node"]:isSelected() and checkBoxInfo["node"]:isVisible() then
            if checkBoxInfo["nodeConfig"]["select"] then
                strGameRule = strGameRule .. checkBoxInfo["nodeConfig"]["select"]
            end
        elseif checkBoxInfo["node"]:isVisible() then
            if checkBoxInfo["nodeConfig"]["unselect"] then
                strGameRule = strGameRule .. checkBoxInfo["nodeConfig"]["unselect"]
            end
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

    for _, inputBoxInfo in pairs(inputBoxTable) do
        if inputBoxInfo["node"] and inputBoxInfo["nodeConfig"] and inputBoxInfo["nodeConfig"]["ruleName"] then
            local inputText = inputBoxInfo["node"]:getText()
            local inputValue = 0
            if inputText ~= nil and inputText ~= "" and tonumber(inputText) then
                inputValue = tonumber(inputText)
            end

            local inputNodeName = inputBoxInfo["nodeConfig"]["nodeName"] or ""
            if self:isChessFeeMustBiggerZero() and inputNodeName == "roomCostInput" and inputValue <= 0 then
                bRight = false
                local ruleText = inputBoxInfo["nodeConfig"]["ruleText"] or ""
                XH.TipTool.showToast(ruleText .. "输入值必须大于0")
            end
            inputValue = string.format("%.02f", inputValue)
            local ruleStr = inputBoxInfo["nodeConfig"]["ruleName"] .. "='" .. inputValue .. "';"
            strGameRule = strGameRule .. ruleStr
        end
    end

    return strGameRule, bRight
end

function IMCreateRoomView:isChessFeeMustBiggerZero()
    local lobbyID = XH.areaData:getLobbyID()
    if tonumber(lobbyID) == XH.LOBBY_ID.YUYAO then
        return true
    end
    return false
end

--获取房间除了局数玩法之外的特殊玩法，比如圈数这种不定局的玩法
function IMCreateRoomView:getGameConditions()
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

--获取玩家数量
function IMCreateRoomView:getPlayerCount()
    local index = 1
    while true do
        local playerCountItem = ccui.Helper:seekNodeByName(self._gameRule, "playerCount" .. index)
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
function IMCreateRoomView:getPlayCount()
    local index = 1
    while true do
        local playCountItem = ccui.Helper:seekNodeByName(self._gameRule, "playCount" .. index)
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
    return 10000
end

--更新房卡消耗的显示
function IMCreateRoomView:updateShowCostInfo()
    local costString = ""
    local discountCost = -1
    for _, costRelevantInfo in pairs(playCountTable) do
        if costRelevantInfo["node"]:isSelected() and self._costTextInfo then
            if self._isCostRelativePlayers then
                if self._isMutiVersionGame then
                    local cost = tonumber(costRelevantInfo["nodeConfig"][tostring("allMutiVersionCost" .. self:getPlayerCount())])
                    costString = "x " .. cost
                    for _, costTypeInfo in pairs(costTypeTable) do
                        if costTypeInfo["node"]:isSelected() then
                            if tostring(costTypeInfo["nodeConfig"]["costType"]) == "aaCost" then
                                local cost2 = tonumber(costRelevantInfo["nodeConfig"][tostring("aaMutiVersionCost" .. self:getPlayerCount())])
                                costString = "x " .. cost2
                                costString = costString .. " /人"
                            end
                            break
                        end
                    end
                else
                    local cost = tonumber(costRelevantInfo["nodeConfig"][tostring("allCost" .. self:getPlayerCount())])
                    discountCost = tonumber(costRelevantInfo["nodeConfig"][tostring("allDiscountCost" .. self:getPlayerCount())])
                    costString = "x " .. cost
                    for _, costTypeInfo in pairs(costTypeTable) do
                        if costTypeInfo["node"]:isSelected() then
                            local cost2 = tonumber(costRelevantInfo["nodeConfig"][tostring(costTypeInfo["nodeConfig"]["costType"] .. self:getPlayerCount())])
                            if tostring(costTypeInfo["nodeConfig"]["costType"]) == "aaCost" then
                                discountCost = tonumber(costRelevantInfo["nodeConfig"][tostring("aaDiscountCost" .. self:getPlayerCount())])
                            else
                                discountCost = tonumber(costRelevantInfo["nodeConfig"][tostring("allDiscountCost" .. self:getPlayerCount())])
                            end
                            costString = "x " .. cost2
                            if tostring(costTypeInfo["nodeConfig"]["costType"]) == "aaCost" then
                                costString = costString .. " /人"
                                if discountCost and discountCost ~= -1 then 
                                    discountCost = discountCost .." /人"
                                end
                            end
                            break
                        end
                    end
                end
            else
                local cost = tonumber(costRelevantInfo["nodeConfig"]["allCost"])
                discountCost = tonumber(costRelevantInfo["nodeConfig"]["discountCost"])
                costString = "x " .. cost
                for _, costTypeInfo in pairs(costTypeTable) do
                    if costTypeInfo["node"]:isSelected() then
                        local cost2 = tonumber(costRelevantInfo["nodeConfig"][tostring(costTypeInfo["nodeConfig"]["costType"])] or "0")
                        costString = "x " .. cost2
                        if tostring(costTypeInfo["nodeConfig"]["costType"]) == "aaCost" then
                            costString = costString .. " /人"
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
        self._costTextInfo2:setString(costString)
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
end

--根据本地选择的按钮获取规则串
function IMCreateRoomView:getBoxRoomRuleString(unShowRuleData)
    local ruleText = ""
    for _, nodeTable in pairs(radioTable) do
        for _, nodeInfo in pairs(nodeTable) do
            if nodeInfo["node"]:isSelected() and string.match(nodeInfo["nodeConfig"]["nodeName"], "playCount") == nil
            and string.match(nodeInfo["nodeConfig"]["nodeName"], "Cost") == nil
            and string.match(nodeInfo["nodeConfig"]["nodeName"], "playerCount") == nil then
                --nodeInfo["node"]:isVisible()应该全区开放的，是一个固定玩法规则的BUG
                --目前别的区没反馈且测试没时间，所以加个了特判对出问题的区进行修复
                if XH.areaData:getLobbyID() ~= XH.LOBBY_ID.TAIZHOU or nodeInfo["node"]:isVisible() then 
                    if ruleText == "" then
                        ruleText = nodeInfo["nodeConfig"]["text"]
                    else
                        ruleText = ruleText .. "/" .. nodeInfo["nodeConfig"]["text"]
                    end
                end
            end
        end
    end
    for _, checkBoxInfo in pairs(checkBoxTable) do
        if checkBoxInfo["node"]:isSelected() and checkBoxInfo["node"]:isVisible() then
            local nodeName = checkBoxInfo["nodeConfig"]["nodeName"]
            local isNeedAdd = true

            if unShowRuleData then
                for _, unShowRule in pairs(unShowRuleData) do
                    if string.find(nodeName, unShowRule) then
                        isNeedAdd = false
                        break
                    end
                end
            end
            if isNeedAdd then
                if ruleText == "" then
                    ruleText = checkBoxInfo["nodeConfig"]["text"]
                else
                    ruleText = ruleText .. "/" .. checkBoxInfo["nodeConfig"]["text"]
                end
            end
        end
    end
    for _, inputBoxInfo in pairs(inputBoxTable) do
        if inputBoxInfo["node"] and inputBoxInfo["nodeConfig"] and inputBoxInfo["nodeConfig"]["ruleText"] then
            local inputText = inputBoxInfo["node"]:getText()
            local inputValue = 0
            if inputText ~= nil and inputText ~= "" and tonumber(inputText) then
                inputValue = tonumber(inputText)
            end
            if ruleText == "" then
                ruleText = inputBoxInfo["nodeConfig"]["ruleText"]
            else
                ruleText = ruleText .. "/" .. inputBoxInfo["nodeConfig"]["ruleText"] .. inputValue
            end
        end
    end
    return ruleText
end

--刷新活动角标
function IMCreateRoomView:refreshActivityIcon(event)
    if not event or not event.msg then 
        return 
    end
end

--倒计时
function IMCreateRoomView:runCountDownAcitvity(node, leftTime)
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

--是否是限时活动游戏
function IMCreateRoomView:isActivityGame(gameid)
    return false
 end

function IMCreateRoomView:choseTwoPlayersBtn(send)
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

function IMCreateRoomView:isValueInHideTable(value)
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

function IMCreateRoomView:onTouchEventPlayModeAreaChange(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._panelPlayModeArea:setVisible(true)
    self:initItem()
end

function IMCreateRoomView:initItem()
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

function IMCreateRoomView:addItem(itemState, itemName, gameId,lobbyid,isMutiVersionGame)
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

function IMCreateRoomView:updateItemState(itemName)
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

function IMCreateRoomView:onTouchEventPlayTypeSave(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    --读取对应地区的配置，刷新桌子面板玩法
    self._isMutiVersionGame = self._tmpMutiVersionGame
    if self._tmpGameID then
        self._gameID = self._tmpGameID 
    end
    self:loadGameRuleDynamic(self._tmpGameID or self._gameID,self._tmpLobbyID)
    if self._tmpGameName then
        self._btnPlayModeArea:setTitleText("玩法："..self._tmpGameName) 
    end
    self._panelPlayModeArea:setVisible(false)
end

function IMCreateRoomView:onTouchEventPlayTypeClose(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    self._panelPlayModeArea:setVisible(false)
end

return IMCreateRoomView�[ 