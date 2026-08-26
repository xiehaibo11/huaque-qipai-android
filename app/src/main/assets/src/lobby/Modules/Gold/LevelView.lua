local LevelView = class("LevelView", XH.ViewBase)

local KW_DATA_SET_LEVEL = {
    [2] = "KW_GOLD_LEVEL_MID_TIPS",
    [3] = "KW_GOLD_LEVEL_HIGH_TIPS",
}

function LevelView:getCSBPath()
    return "cocosStudio/hall/CSB/goldroom/GoldLevelLayer.csb" 
end

function LevelView:getBindingInfo()
    return {
        -- 金币场相关
        ["_KW_UI_GOLD_LEVEL_ROOT"] = { varName = "_goldLevelRoot" },
        ["_KW_ITEM_LIST"] = { varName = "_itemList" },
        ["_KW_UI_GOLD_LEVEL_ITEM"] = { varName = "_goldLevelItem" },
        ["_KW_UI_CLOSE_BTN"] = { varName = "_closeBtn",type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose"  },
        ["_KW_UI_GOLD_LZDDZ_ITEM"] = { varName = "_KW_UI_GOLD_LZDDZ_ITEM",type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventGoldScene"  },
    }
end

function LevelView:getProxyEvents()
    return {
        { module = XH.goldConfigManager, eventKeyName = "GOLD_EVENT_SHOWPLAYER_COUNT", callBack = "onRespGoldPlayerCount" },
    }
end

function LevelView:ctor(confID, gameID)
    self._confID = confID
    LevelView.super.ctor(self)
	self._roomConf = XH.goldConfigManager:getGoldRoomInfoByIndex(confID)
    self._roomInfo = self._roomConf.roomInfo
    self._gameID = gameID
    self._roomLevelInfos = self._roomConf.roomLevelInfos
    self:createLevelItem()
end

function LevelView:getRichString(rich)
    if rich < 0 then
        return 1000
    end
    if rich < 10000 then
        return rich
    end

    return rich / 10000 .. "万"
end

-- 判断是否显示连炸斗地主
function LevelView:isShowGoldScene()
    if not XH.lobby:getModule("NewGoldScene"):getIconVisible() then
        return false
    end
    if not string.find(self._roomInfo.GameTitle, "斗地主") then
        return false
    end
    return true
end

-- 显示连炸斗地主
function LevelView:judgeShowGoldScene()
    if not self:isShowGoldScene() then
        return
    end
    local tempItem = self._KW_UI_GOLD_LZDDZ_ITEM:clone()
    tempItem:setVisible(true)
    self._itemList:addChild(tempItem)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_GOLD_THROW_ERQI_3, nil, { area_id = XH.areaData:getAreaID() })
end

function LevelView:onTouchEventGoldScene()
    XH.lobby:getModule("Lobby"):enterNewGold()
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_GOLD_THROW_ERQI_4, nil, { area_id = XH.areaData:getAreaID() })
    self:close()
end

function LevelView:createLevelItem()
    local roomInfo = self._roomInfo
    local reqData = {}
    self:judgeShowGoldScene()
    for i = 1, #self._roomLevelInfos do
        local tempItem = self._goldLevelItem:clone()
        local roomLevelInfo = self._roomLevelInfos[i]
        local node = XH.UITool.seekNodeByName(tempItem, "_KW_UI_GOLD_LEVEL_ITEM")
        if node and roomInfo.roomFlagImage and #roomInfo.roomFlagImage > 0 then
            self:loadTexture(node,roomInfo.roomFlagImage[i])
        end

        if node and roomInfo.BaseScoreImage and roomInfo.BaseScoreImage ~= "" then
            XH.UITool.loadTexture(tempItem, "KW_IMAGE_DIFEN", roomInfo.BaseScoreImage, ccui.TextureResType.plistType)
            XH.UITool.setTextColor(tempItem, "KW_UI_GOLD_LIMIT",cc.c3b(29, 78, 101))
        end

        local roomDesc
        if roomLevelInfo.maxrich >= 40000000 or roomLevelInfo.maxrich == -1 then
            roomDesc = self:getRichString(roomLevelInfo.minrich) .. "以上"
        else
            roomDesc = self:getRichString(roomLevelInfo.minrich) .. "-" .. self:getRichString(roomLevelInfo.maxrich)
        end

        XH.UITool.setText(tempItem, "KW_UI_GOLD_LIMIT","准入:" .. roomDesc)
        XH.UITool.setText(tempItem, "KW_UI_GOLD_BASE_SCORE",roomLevelInfo.basescore)

        if roomLevelInfo.gameid == 30117 and roomLevelInfo.roomnameflag == 4 then
            XH.UITool.setVisible(tempItem, "KW_IMG_FLAG_7CS", true)
        else
            XH.UITool.setVisible(tempItem, "KW_IMG_FLAG_7CS", false)
        end

        tempItem:addTouchEventListener(handler(self, self.onJoinGoldRoomTouchEvent))

        local tempInfo = {}
        tempInfo.ConfID = self._confID
        tempInfo.Level = roomInfo.roomFlag[i] or -1
        if type(roomInfo.SrsGroupId) == "table" then
            tempInfo.SrsGroupId = roomInfo.SrsGroupId[i]
        end
        local level = roomInfo.roomFlag[i] or 0
        if level == 3 then
            level = 4
        end

        tempItem.roomInfo = tempInfo or nil
        -- local pos = self:getPositionByIndex(i)
        -- tempItem:setPosition(pos)
        self._itemList:addChild(tempItem)
        tempItem:setVisible(true)
        -- self._goldLevelRoot:addChild(tempItem,#self._roomLevelInfos - i)
        reqData[i] = {}
        reqData[i].gameID = roomInfo.GameID
        reqData[i].level = level

        if self._roomInfo.GoldActRoomType and self._roomInfo.GoldActRoomType[tempInfo.Level] ~= nil and self._roomInfo.GoldActRoomType[tempInfo.Level] ~= "" then
            local goldActQiPao = XH.UITool.seekNodeByName(tempItem, "_KW_GOLD_PLAY_QIPAO")
            if goldActQiPao then
                goldActQiPao:setVisible(true)
                local text = XH.UITool.seekNodeByName(goldActQiPao, "_KW_GOLD_PLAY_QIPAO_TEXT")
                local btn = XH.UITool.seekNodeByName(goldActQiPao, "_KW_GOLD_PALY_JOIN_BTN")
                if text then
                    text:setString(self._roomInfo.GoldActRoomType[tempInfo.Level])
                end
                if btn then
                    btn:addTouchEventListener(handler(self, self.onJoinGoldRoomTouchEvent))
                    btn.roomInfo = tempInfo or nil
                end
            end
            
        else
            local goldActQiPao = XH.UITool.seekNodeByName(tempItem, "_KW_GOLD_PLAY_QIPAO")
            goldActQiPao:setVisible(false)
        end
    end
    XH.goldConfigManager:reqRoomPlayerCount(reqData)

    local itemCount = #self._itemList:getChildren() 
    if itemCount == 1 then
        self._itemList:setContentSize(cc.size(355,620))
    elseif itemCount == 2 then
        self._itemList:setContentSize(cc.size(870,620))
    elseif itemCount == 3 then
        self._itemList:setContentSize(cc.size(1385,620))
    elseif itemCount > 3 then
        self._itemList:setContentSize(cc.size(1560,620))
    end
end

function LevelView:onRespGoldPlayerCount(event)
    if event.level == 0 then
        return
    end
    for _, node in pairs(self._itemList:getChildren()) do
        if node.roomInfo then
            --判断是否是当前游戏的活动
            local gameLevel = node.roomInfo.Level
            if gameLevel == 3 then
                gameLevel = 4
            end
            if gameLevel == event.level and event.gameID == self._gameID then
                local data = event.data[self._gameID]
                XH.UITool.setVisible(node, "_KW_GAME_PLAYER_COUNT_BG", true)
                XH.UITool.setText(node, "_KW_LABEL_PLAYER_COUNT", data.level[gameLevel])
                return
            end
        end
    end
end

function LevelView:onJoinGoldRoomTouchEvent(send, eventType)   
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local roomInfo = send.roomInfo
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.GOLD_GAME_1,{GameName =self._roomInfo.GameTitle,Score = self._roomConf.roomLevelInfos[roomInfo.Level].basescore})
    --if self:judgeGoldWithLevels(send) then --万飞说不要了
        XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_JOIN_GOLD_GAME_ID, roomInfo.ConfID)
        XH.lobby:getModule("Gold"):reqJoinGoldRoom(roomInfo.ConfID, roomInfo.Level, roomInfo.SrsGroupId)
        XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_JOIN_GOLD_LEVEL, roomInfo.Level)
        self:close()
    --end
end

function LevelView:judgeGoldWithLevels(send)
    local roomInfo = send.roomInfo
    local playerGold = XH.playerData:getSR() or 0
    if self._roomLevelInfos and #self._roomLevelInfos > 1 and self._roomInfo.SpecialPlayer == 0 then
        for roomflag = #self._roomLevelInfos, roomInfo.Level + 1, -1 do
            if KW_DATA_SET_LEVEL[roomflag] and playerGold >= self._roomLevelInfos[roomflag].minrich then
                local getTodayPopTimes = XH.userDefault:getValue(XH.userDefault.KEY_ID[KW_DATA_SET_LEVEL[roomflag]], 0)
                if getTodayPopTimes == 0 then
                    XH.userDefault:setValue(XH.userDefault.KEY_ID[KW_DATA_SET_LEVEL[roomflag]], 1)
                    self:changeRoomTips(send, self._roomLevelInfos[roomflag])
                    return false
                end
            end
        end
    end
    return true
end

function LevelView:changeRoomTips(send, roomLevelInfos)
    local roomInfo = send.roomInfo
    XH.TipTool.showTip( {
        type = XH.TIP_LAYER_TYPE.OK_CANCEL,
        funcOK = function()
            XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_JOIN_GOLD_GAME_ID, roomInfo.ConfID)
            XH.lobby:getModule("Gold"):reqJoinGoldRoom(roomInfo.ConfID, roomLevelInfos.roomnameflag)
            XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_JOIN_GOLD_LEVEL, roomLevelInfos.roomnameflag)
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.GOLD_GAME_3,{GameName =self._roomInfo.GameTitle,Source = self._curLobbyType})
            self:close()
        end,
        funcCancel = function ()
            XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.GOLD_GAME_4,{GameName =self._roomInfo.GameTitle,Source = self._curLobbyType})
        end,
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.GOLD_GAME_2,{GameName =self._roomInfo.GameTitle,Source = self._curLobbyType})
    } , "您已经可以前往" .. XH.GOLD_LEVEL_DEFAULT_NAME[roomLevelInfos.roomnameflag] .. "一展拳脚了，是否前往？")
end

function LevelView:onTouchEventClose(send,eventType)
    self:close()
end

function LevelView:loadTexture(node,fileName)
    node:loadTexture(fileName, ccui.TextureResType.plistType)
end

--计算位置
function LevelView:getPositionByIndex(index)
    index = index - 1
    local roomCnt = #self._roomConf.roomLevelInfos
    local itemSize = self._goldLevelItem:getContentSize()
    itemSize.width = itemSize.width + 100
    local displaySize = self._goldLevelRoot:getContentSize()
    local posX = displaySize.width / 2
    if roomCnt % 2 == 0 then
        posX = posX + ((index + 0.5 - roomCnt/2) * itemSize.width)
    else
        posX = posX + ((index - math.floor(roomCnt/2)) * itemSize.width)
    end

    return cc.p(posX, displaySize.height / 2)
end

return LevelView
   !*  