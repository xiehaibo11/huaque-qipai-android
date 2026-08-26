---@class PlayerBillListItem : View
local PlayerBillListItem = class("PlayerBillListItem", XH.ViewBase)
local PlayerBillView = require("lobby.Modules.PlayerBill.View")

PlayerBillListItem.ColorText = cc.c3b(163,111,72)
PlayerBillListItem.ColorTextSelf = cc.c3b(30,145,141)

--名字字号
PlayerBillListItem.SizeTextName = {
    [1] = 42,
    [2] = 42,
    [3] = 42,
    [4] = 42,
    [5] = 38,
    [6] = 38,
    [7] = 34,
    [8] = 34,
}

function PlayerBillListItem:getCSBPath()
    return "cocosStudio/hall/CSB/PlayerBillListItem.csb"
end

function PlayerBillListItem:getBindingInfo()
    return {
        ["_KW_IMG_ITEM"] = { varName = "_imgItem" },
        ["_KW_TEXT_ITEM_INDEX"] = { varName = "_textIndex" },
        ["_KW_BMFONT_GAMENAME"] = { varName = "_textGameName" },
        ["_KW_BMFONT_ROOMID"] = { varName = "_textRoomId" },
        ["_KW_BMFONT_PLAYCOUNT"] = { varName = "_textPlayCount" },
        ["_KW_BMFONT_DATE_TIME"] = { varName = "_textTime" },
        ["_KW_BMFONT_ROOMCARD"] = { varName = "_textRoomCard" },
        ["_KW_BTN_DETIAL"] = { varName = "_btnDetail", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventDetail" },
        ["_KW_PANEL_NAME_SCORE"] = { varName = "_panelNameScore" },
        ["_KW_PANEL_SINGLE_NAME_SCORE"] = { varName = "_panelSingleNameScore" },
        ["_KW_CHECKBOX_SELECT"] = { varName = "_checkBoxSelect" },
    }
end

function PlayerBillListItem:ctor()
    PlayerBillListItem.super.ctor(self)
    self:initView()
end

function PlayerBillListItem:initView()
    if self._imgItem then
        self._imgItem:removeFromParent()
        self:addChild(self._imgItem)
    end
    if self._btnDetail then
        self._btnDetail:setSwallowTouches(false)
    end

    if self._checkBoxSelect then
        self._checkBoxSelect:setVisible(PlayerBillView.isShowBillMerge())
        self._checkBoxSelect:addEventListener(handler(self, self.onTouchEventCheckBoxSelect))
    end
end

function PlayerBillListItem:getSize()
    if self._imgItem ~= nil then
        return self._imgItem:getContentSize()
    end
    return cc.size(0, 0)
end

function PlayerBillListItem:updateInfo(info, index)
    self:setIndex(index)
    local gameNameList = XH.areaData:getAreaGameNameList()
    self:setGameName(gameNameList[info.gameID] or "")

    if XH.ChannelTool.checkIsDoubleKouGame() then
        local GameSubByConfID = require("app.Config.GameSub").GameSubByConfID
        if GameSubByConfID[info.gameID].GameType == "DoubleKou" then
            self:setGameName("双扣")
        end
    end

    if tonumber(info.config) then
        self:setRoomId(string.format("%06d", tonumber(info.config)))
    end
    self:setPlayCount(info.finishgames, info.totalgames)
    self:setDateTime(os.date("%Y-%m-%d %H:%M", info.starttime))
    --大小卡
    local exchangeRate = 1
    local smallCardId = XH.areaData:getPropList().smallRoomCardID
    local smallCardRatio = XH.areaData:getPropSmallRoomCardRatio()
    if info.nPropid ~= nil and info.nPropid == smallCardId then
        exchangeRate = smallCardRatio or 1
    end
    if info.nPayType ~= nil 
    and (info.nPayType == 20 or info.nPayType == 21) then
        self:setRoomCard(info.fee or 0, true)
        if info.limitFree and info.limitFree == 1 then
            self:setRoomCard(0, true)
        end
    else
        local checkIsOldGPGame = function(gameid)
            local gameids = XH.areaData:getUsedOldGPGames()
            if gameids then
                for _,v in pairs(gameids) do
                    if gameid == v then
                        return true
                    end
                end
            end
            return false
        end
        if info.prevaa and info.prevaa ==  1 then
            if XH.areaData:getIsNewBoxGameProcess() and not checkIsOldGPGame(info.gameID) then
                self:setRoomCard(info.nUsePropCnt / exchangeRate)
            else
                self:setRoomCard(info.nUsePropCnt / (exchangeRate * (info.playercnt or 1)))
            end
        else
            self:setRoomCard(info.nUsePropCnt / exchangeRate)
        end
        if info.limitFree and info.limitFree == 1 then
            self:setRoomCard(0)
        end
    end

    self:setSelectState(info.bSelected)
   
    self:updateNameAndScores(info.playerInfos, info.endInfoEx, info.roomHostID or 0)
end

function PlayerBillListItem:onTouchEventCheckBoxSelect(send, eventType)
    --加载默认选项时不需要音效
    if eventType == ccui.CheckBoxEventType.selected then
        if self._selectCallBack then
            self._selectCallBack(self._index, true)
        end
    elseif eventType == ccui.CheckBoxEventType.unselected then
        if self._selectCallBack then
            self._selectCallBack(self._index, false)
        end
    end
end

function PlayerBillListItem:setSelectCallBack(func)
    self._selectCallBack = func
end

function PlayerBillListItem:setSelectState(bSelect)
    if not self._checkBoxSelect then
        return
    end

    if bSelect == nil then
        bSelect = false
    end

    self._checkBoxSelect:setSelected(bSelect)
end

function PlayerBillListItem:setIndex(index)
    if not self._textIndex then
        return 
    end
    self._index = index
    self._textIndex:setString(tostring(index))
end

function PlayerBillListItem:setGameName(nameStr)
    if not self._textGameName then
        return 
    end
    self._textGameName:setString(nameStr)
end

--房间号
function PlayerBillListItem:setRoomId(roomId)
    if not self._textRoomId then
        return 
    end
    self._textRoomId:setString(roomId)
end

--局数
function PlayerBillListItem:setPlayCount(count, countTotal)
    if not self._textPlayCount then
        return 
    end
    self._textPlayCount:setString(count.."/"..countTotal)
end

--时间
function PlayerBillListItem:setDateTime(time)
    if not self._textTime then
        return 
    end
    self._textTime:setString(time)
end

--房卡
function PlayerBillListItem:setRoomCard(roomCard, bFee)
    if not self._textRoomCard then
        return 
    end
    if bFee then
        self._textRoomCard:setString(XH.StringTool.replaceMatchStr("圈子卡x") .. roomCard)
    else
        self._textRoomCard:setString("房卡x" .. roomCard)
    end
end

function PlayerBillListItem:updateNameAndScores(playerInfos,endInfoEx,roomHostID)
    if not self._panelNameScore or not self._panelSingleNameScore then
        return 
    end

    if self._tbNameScoreLayouts then
        for _, v in pairs(self._tbNameScoreLayouts) do
            v:removeFromParent()
        end
    end
    self._tbNameScoreLayouts = {}

    local displayUserId = XH.playerData:getNumberID()
    local panelSize = self._panelNameScore:getContentSize()
    local playerCount = #playerInfos
    local singleWidth = panelSize.width/playerCount
    for i = 1, playerCount do
        local nameScoreLayout = self._panelSingleNameScore:clone()

        local textName = ccui.Helper:seekWidgetByName(nameScoreLayout, "KW_TEXT_PLAYER_NAME")
        local nicknameStr = XH.StringTool.cutStringByLength(playerInfos[i].nickname or "")
        textName:setString(nicknameStr)
        textName:setTextColor(displayUserId == playerInfos[i].numid and self.ColorTextSelf or self.ColorText)
        textName:setFontSize(self.SizeTextName[playerCount])

        local textScore = ccui.Helper:seekWidgetByName(nameScoreLayout, "KW_TEXT_PLAYER_SCORE")
        local totalScore = playerInfos[i].totalscore
        if totalScore >= 0 then
            totalScore = "+"..totalScore
        end
        if endInfoEx then
            local endInfoExItem = endInfoEx[tostring(playerInfos[i].numid)]
            if endInfoExItem then
                local kongsr = endInfoExItem["KONGSC"]
                local isSuanGangScore = endInfoExItem["ISSGF"] or 0
                if kongsr and isSuanGangScore == 0 then
                    totalScore = totalScore .. "("..kongsr..")"
                end
            end  
        end
        textScore:setString(totalScore)
        textScore:setTextColor(displayUserId == playerInfos[i].numid and self.ColorTextSelf or self.ColorText)
        textScore:setFontSize(self.SizeTextName[playerCount])

        if roomHostID and roomHostID > 0 and roomHostID == playerInfos[i].numid then
            XH.UITool.setVisible(nameScoreLayout,"KW_IMG_HOST",true)
        end

        nameScoreLayout:setPositionX(singleWidth*(i-1/2))
        self._panelNameScore:addChild(nameScoreLayout)
        self._tbNameScoreLayouts[i] = nameScoreLayout
    end
end

function PlayerBillListItem:setDetailBtnFunc(func)
    self._detailFunc = func
end

function PlayerBillListItem:onBtnEventDetail(send, eventType)
    if self._detailFunc then
        self._detailFunc(self._index)
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_29) 
    end
end

return PlayerBillListItem�#  