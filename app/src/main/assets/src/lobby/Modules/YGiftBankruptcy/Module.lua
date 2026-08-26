local YGiftBankruptcyModule = class("YGiftBankruptcyModule", XH.ModuleBase)
local YGiftBankruptcyDefine = require("lobby.Modules.YGiftBankruptcy.Define")

YGiftBankruptcyModule.FLUSH_ACT_INFO = "FLUSH_ACT_INFO"
YGiftBankruptcyModule.FLUSH_BANKRUPTCY_ACT_INFO = "FLUSH_BANKRUPTCY_ACT_INFO"

-- 破产礼包
function YGiftBankruptcyModule:ctor()
    YGiftBankruptcyModule.super.ctor(self)
    self._rmbPropsAdd = {} -- 所有付费商品
    self._actInfo = {} -- 基础活动信息
    self._playerInfo = {} -- 具体玩家活动信息
    self._aid = {} -- 活动id
    self._roomLevel = -1    -- 房间等级
    self._throwDataGameID = 0   -- 
end

function YGiftBankruptcyModule:getReqConfig()
    return {
        ReqDiamondProductsInfo = { reqPath = "lobby.Req.Shop.ReqProductList", callBack = self.respDiamondConfigInfo },
        ReqActDetail = { reqPath = "lobby.Req.LuckTask.ReqActDetail", callBack = self.respActDetail },
        ReqBankruptcyGift = { reqPath = "lobby.Req.LuckTask.ReqBankruptcyGift", callBack = self.respPlayerInfo },
    }
end

-- 是否是新金币场游戏（目前仅双扣）
function YGiftBankruptcyModule:isNewModeGold(gameId)
    return gameId == 30116 or gameId == 42038 or gameId == 30159
end

function YGiftBankruptcyModule:setActivityId(aid)
    self._aid[XH.areaData:getSrsGroupID()] = aid
    self:reqActDetail()
    self:reqPlayerInfo()
end

function YGiftBankruptcyModule:getActivityId()
    return self._aid[XH.areaData:getSrsGroupID()]
end

function YGiftBankruptcyModule:setPlayerActInfo(info)
    self._playerInfo[XH.areaData:getSrsGroupID()] = info
end

function YGiftBankruptcyModule:getPlayerActInfo()
    return self._playerInfo[XH.areaData:getSrsGroupID()]
end

function YGiftBankruptcyModule:setActInfo(info)
    self._actInfo[XH.areaData:getSrsGroupID()] = info
end

function YGiftBankruptcyModule:getActInfo()
    return self._actInfo[XH.areaData:getSrsGroupID()]
end

function YGiftBankruptcyModule:setRoomLevel(roomLevel)
    self._roomLevel = roomLevel
end

function YGiftBankruptcyModule:getRoomLevel()
    return self._roomLevel
end

-- 为了不破坏原有结构，新增gameid专门用来抛送数数
function YGiftBankruptcyModule:setThrowDataGameID(gameID)
    if gameID == 30116 then
        gameID = 42038
    end
    self._throwDataGameID = gameID
end

function YGiftBankruptcyModule:getThrowDataGameID()
    return self._throwDataGameID
end

-- 显示免败礼包
function YGiftBankruptcyModule:showGiftDefeat(gameID, lobbyType)
    XH.viewManager:openView("YGiftDefeatView", nil, { gameID = gameID or 0, scene = lobbyType or 0 })
end

-- 显示破产礼包
function YGiftBankruptcyModule:showBankruptcy(lobbyType)
    --XH.viewManager:openView("GoldBaseLiveView", nil, { showBankupt = true })
    XH.viewManager:openView("GoldBankruptcyView", nil, { showBankupt = true, scene = lobbyType })

end

-- 房间金额不足以进入的情况（目前仅双扣）
function YGiftBankruptcyModule:isShowGiftEnter(gameID, roomLevel)
    if not self:isNewModeGold(gameID) then
        return false
    end
    if roomLevel == 1 and XH.taskManager:canGetBreakAward() then
        --XH.viewManager:openView("GoldBaseLiveView")
        XH.viewManager:openView("GoldBankruptcyView", nil, { showBankupt = true, scene = XH.LOBBY_TYPE.XIUXIAN })
        return true
    end
    if XH.lobby:getModule("YGiftEnter"):getActivityId() ~= nil then
        XH.lobby:getModule("YGiftEnter"):reqPlayerInfo(roomLevel, gameID)
        return true
    end
    return false
end

-- 登陆初始化接口
function YGiftBankruptcyModule:initGoldActData()
    self:reqDiamondConfigInfo()

    -- 破产礼包+免赔礼包
    self:reqPlayerInfo()
    XH.lobby:getModule("YGiftDefeat"):reqPlayerInfo()
end

-- 获取详细任务
function YGiftBankruptcyModule:reqActDetail()
    if self:getActivityId() == nil then
        return
    end
    if self:getActInfo() ~= nil then
        return
    end
    self:startReq("ReqActDetail", self:getActivityId(), 10)
end

function YGiftBankruptcyModule:respActDetail(req, type, data)
    self:setActInfo(nil)
    if type == XH.Req.TYPE.SUCCESS then
        if data and data.res and data.res.special then
            local ok, res_tab = pcall(cjson.decode, data.res.special)
            if ok and res_tab and res_tab.sessions then
                self:setActInfo(res_tab.sessions)
            end
        end
    end
end

-- 是否为破产/免败/进房的购买礼包
function YGiftBankruptcyModule:isPayWithGift(msg)
    if not msg then
        return false
    end
    local data = msg.productInfo
    if data and data.productId then
        local splitTable = string.split(data.productId, "-")
        local first = splitTable[1]
        local second = splitTable[3]
        if not first or not second then
            return false
        end
        local cnt = -1
        if first == YGiftBankruptcyDefine.ACTLIST.Bankruptcy then
            cnt = 0
            local info = self:getPlayerActInfo()
            if info and #info > 0 then
                for z = 1, #info do
                    if info[z].goodsId == tonumber(second) then
                        cnt = info[z].gold
                        break
                    end
                end
            end
            self:reqPlayerInfo()
        elseif first == YGiftBankruptcyDefine.ACTLIST.Enter then
            cnt = 0
            local info = XH.lobby:getModule("YGiftEnter"):getActInfo()
            if info then
                for z = 1, #info do
                    if info[z].normal_goods and #info[z].normal_goods > 0 then
                        for y = 1, #info[z].normal_goods do
                            if info[z].normal_goods[y].goods_id == tonumber(second) then
                                cnt = info[z].normal_goods[y].gold
                                break
                            end
                        end
                    end
                    if info[z].prefer_goods and #info[z].prefer_goods > 0 then
                        for y = 1, #info[z].prefer_goods do
                            if info[z].prefer_goods[y].goods_id == tonumber(second) then
                                cnt = info[z].prefer_goods[y].gold
                                break
                            end
                        end
                    end
                end
            end
        elseif first == YGiftBankruptcyDefine.ACTLIST.Defeat then
            cnt = 0
            local info = XH.lobby:getModule("YGiftDefeat"):getPlayerActInfo()
            if info then
                for z = 1, #info do
                    if info[z].goodsId == tonumber(second) then
                        if info[z].lose < 0 then
                            info[z].lose = 0 - info[z].lose
                        end
                        cnt = info[z].lose + info[z].gold
                        break
                    end
                end
            end
            XH.lobby:getModule("YGiftDefeat"):reqPlayerInfo()
        end
        if cnt == 0 then
            XH.TipTool.showToast("充值成功，您已经获得奖励金币，祝您游戏愉快~")
            XH.SysTool.performDelayOnce(function()
                XH.playerData:flushGoldCoin()
            end, 1)
            return true
        end
        if cnt ~= -1 then
            XH.TipTool.showToast("充值成功，您已经获得" .. self:getWGoldScore(cnt) .. "金币，祝您游戏愉快~")
            XH.SysTool.performDelayOnce(function()
                XH.playerData:flushGoldCoin()
            end, 1)
            return true
        end
    end
    return false
end

-- 请求商品信息
function YGiftBankruptcyModule:reqDiamondConfigInfo()
    self:startReq("ReqDiamondProductsInfo", XH.configerManager:getShopAppId(), XH.configerManager:getShopPackageAppId())
end

function YGiftBankruptcyModule:respDiamondConfigInfo(req, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        self._rmbPropsAdd = data
    end
end

function YGiftBankruptcyModule:gameLastGameID()
    return self._lastGameID or 0
end

-- 是否触发破产请求 外部调用
function YGiftBankruptcyModule:showBuyBankuptcyGift(gameID, score)
    if not self:isNewModeGold(gameID) then
        return
    end
    self._lastGameID = gameID
    if score > 0 then
        return
    end
    if score < 0 then
        score = 0 - score
    end
    if self:getActivityId() == nil then
        return
    end
    self:reqPlayerInfo(true, score)
end

-- 是否触发免败请求 外部调用
function YGiftBankruptcyModule:reqBuyDefeatGift(score, gameID)
    if not self:isNewModeGold(gameID) then
        return
    end
    self._lastGameID = gameID
    if score > 0 then
        return
    end
    if score < 0 then
        score = 0 - score
    end
    XH.lobby:getModule("YGiftDefeat"):reqPlayerInfo(true, score)
end

-- 请求下单
function YGiftBankruptcyModule:reqBuyBankuptcyGift(isMore)
    if self:getActivityId() == nil then
        return
    end
    local dataInfo = self:getPrizeInfo(isMore)
    if not dataInfo then
        return
    end
    local productId = YGiftBankruptcyDefine.ACTLIST.Bankruptcy .. "-" .. self:getActivityId() .. "-" .. dataInfo.goodsId
    self:reqBuyPropByProductId(productId)
end

function YGiftBankruptcyModule:getPrizeInfo(isMore)
    local info = self:getPlayerActInfo()
    if info then
        if #info == 1 then
            return info[1]
        end
        if #info == 2 then
            if info[1].gold > info[2].gold then
                if isMore then
                    return info[1]
                else
                    return info[2]
                end
            else
                if isMore then
                    return info[2]
                else
                    return info[1]
                end
            end
        end
    end
    return nil
end

-- 寻找商品id对应的商品礼包并下单
function YGiftBankruptcyModule:reqBuyPropByProductId(productId,triggerGameID)
    if #self._rmbPropsAdd == 0 then
        XH.TipTool.showToast("未找到匹配档位的商品" .. productId .. "，请联系客服~")
        return
    end
    for i = 1, #self._rmbPropsAdd do
        if self._rmbPropsAdd[i].productId == productId then
            XH.lobby:getModule("Shop"):reqBuyDiamond(self._rmbPropsAdd[i], nil, productId, nil, nil, nil, "gift",triggerGameID)
            return
        end
    end
    XH.TipTool.showToast("未找到匹配档位的商品" .. productId .. "，请联系客服!")
end

-- 获取玩家活动信息
function YGiftBankruptcyModule:reqPlayerInfo(isGame, score)
    self._isInGame = isGame
    self._loseScore = score or 0
    if isGame then
        XH.TipTool.showLoading()
    end
    if self:getActivityId() == nil then
        return
    end
    self:startReq("ReqBankruptcyGift", self:getActivityId(), 5)
end

function YGiftBankruptcyModule:setPlayerCnt(dayCnt, total)
    self._dayCnt = dayCnt
    self._totalCnt = total
end

function YGiftBankruptcyModule:getPlayerCnt()
    return self._dayCnt or 0, self._totalCnt or 0
end

function YGiftBankruptcyModule:respPlayerInfo(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self:setPlayerCnt(data.todayCnt, data.totalCnt)
        self:setRoomLevel(data.sessionId)
        self:setThrowDataGameID(data.gameId or 0)
        if data.goods and #data.goods > 0 then
            self:setPlayerActInfo(data.goods)
        else
            self:setPlayerActInfo(nil)
        end
        if self._isInGame and self._loseScore > 0 then
            -- 判断有条件弹窗
            local flag = cc.UserDefault:getInstance():getIntegerForKey("_KW_DATA_LAST_JOIN_GOLD_LEVEL_" .. XH.areaData:getLobbyID(), -1)
            if data.goods and #data.goods > 0 then
                XH.viewManager:closeView("YGiftDefeatView")
                -- XH.viewManager:openView("GoldBaseLiveView", nil, { gameID = self:gameLastGameID(), goldNotEnough = true, roomFlag = flag })
            else
                self:reqBuyDefeatGift(-self._loseScore, self:gameLastGameID())
            end
        end
    else
        self:setPlayerActInfo(nil)
    end
    self:dispatchEvent({ name = self.FLUSH_ACT_INFO })
    self:dispatchEvent({ name = self.FLUSH_BANKRUPTCY_ACT_INFO })
end


function YGiftBankruptcyModule:getWGoldScore(score)
    score = tonumber(score)
    return XH.StringTool.numberToString(score, 1)
end

return YGiftBankruptcyModule /2  