local BaseLiveView = class("BaseLiveView", XH.ViewBase)
local TaskDefine = require("app.Define.TaskDefine")
local ShopConfig = require("lobby.Modules.Shop.Config")
local ConfigurationDefine = require("lobby.Modules.Configuration.Define")

function BaseLiveView:getCSBPath()
    return "cocosStudio/hall/CSB/goldroom/GoldBaseLiveNew.csb"
end

local GOODS_ITEMS_INDEX = {
    BREAK = 1, -- 资产补助
    SALE = 2, -- 特价优惠（破产礼包）
    ADDS = 3, -- 广告奖励
    QUICK_BUY = 4, -- 正常商品
    GOLD_SCENE = 5, -- 连炸引导
    BREAKSHARE = 6, -- 破产分享
    SALE_EX = 7, -- 特价优惠（破产礼包）
}
local GOODS_ITEM_INFOS = {
    [GOODS_ITEMS_INDEX.BREAK] = { sender = "_itemBreak", onTouchEnded = "onTouchEventReq", leftTimes = "getBankruptUseTimes" },
    [GOODS_ITEMS_INDEX.SALE] = { sender = "_itemSale", onTouchEnded = "onTouchEventBreakGift" },
    [GOODS_ITEMS_INDEX.ADDS] = { sender = "_itemAdds", onTouchEnded = "onTouchEventGetToponAaward", leftTimes = "getWatchAddsTimes" },
    [GOODS_ITEMS_INDEX.QUICK_BUY] = { sender = "_itemBuy", onTouchEnded = "onTouchEventCharge" },
    [GOODS_ITEMS_INDEX.GOLD_SCENE] = { sender = "_itemGoldScene", onTouchEnded = "onTouchEventGoldScene" },
    [GOODS_ITEMS_INDEX.BREAKSHARE] = { sender = "_itemBreakShare", onTouchEnded = "onTouchEventReqBreakShare" },
    [GOODS_ITEMS_INDEX.SALE_EX] = { sender = "_itemSaleEx", onTouchEnded = "onTouchEventBreakGiftEx" },
}

function BaseLiveView:getBindingInfo()
    return {
        ["_KW_UI_BTND_MORE"] = { varName = "_moreBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventMore" },
        ["_KW_UI_BTND_GO_SHOPPING"] = { varName = "_goShoppingBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventMore" },
        ["_KW_UI_BTND_CLOSE"] = { varName = "_closeBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventCloseBtn" },
        ["_KW_UI_BTND_CLOSE2"] = { varName = "_closeBtn2", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventCloseBtn" },
        ["_KW_LIST_GOODS"] = { varName = "_goodsList" },
        ["_KW_ITEM_BREAK_BG"] = { varName = "_itemBreak" },
        ["_KW_ITEM_SALE_BG"] = { varName = "_itemSale" },
        ["_KW_ITEM_ADDS_BG"] = { varName = "_itemAdds" },
        ["_KW_ITEM_BUG_BG"] = { varName = "_itemBuy" },
        ["_KW_DIAMOND_GOLD"] = { varName = "_goldImg" },
        ["_KW_CHARGE_NUMBER"] = { varName = "_chargeText" },
        ["_KW_CHARGE_GOLD_NUM"] = { varName = "_chargeGoldText" },
        ["_KW_ITEM_GOLD_SCENE_BG"] = { varName = "_itemGoldScene" },
        ["_KW_ITEM_BREAK_SHARE_BG"] = { varName = "_itemBreakShare" },
        ["_KW_ITEM_SALE_BG_EX"] = { varName = "_itemSaleEx" },
    }
end

--各区金币场快速充值配置
local QuickChargeConfig = {
    [900038] = { priceMin = 6, priceMax = 30 }, -- 丽水
    [900037] = { priceMin = 6, priceMax = 30 }, -- 温茶
    [900017] = { priceMin = 6, priceMax = 30 }, -- 熟客
    [900021] = { priceMin = 6, priceMax = 30 }, -- 杭麻圈
    [900023] = { priceMin = 6, priceMax = 30 }, -- 台州
    [900043] = { priceMin = 6, priceMax = 30 }, -- 舟山
    [900008] = { priceMin = 6, priceMax = 30 }, -- 湖州
    [900003] = { priceMin = 6, priceMax = 30 }, -- 衢州
    [900036] = { priceMin = 6, priceMax = 30 }, -- 瑞安
    [900020] = { priceMin = 6, priceMax = 30 }, -- 金华
    [900025] = { priceMin = 6, priceMax = 30 }, -- 宝宝
    [900031] = { priceMin = 6, priceMax = 30 }, -- 乐清
    [900007] = { priceMin = 6, priceMax = 30 }, -- 绍兴
    [900039] = { priceMin = 6, priceMax = 30 }, -- 青田
    [40165] = { priceMin = 6, priceMax = 30 }, -- 嘉兴
    [900006] = { priceMin = 6, priceMax = 30 }, -- 宁波
    [900029] = { priceMin = 6, priceMax = 30 }, -- 余姚
}

function BaseLiveView:getProxyEvents()
    return {
        { module = XH.taskManager, eventKeyName = "BASE_LIVE_SUCCESS", callBack = "onEventBaseLive" },
        { module = XH.taskManager, eventKeyName = "BREAK_GIFT_BAG_SHOW_TIME", callBack = "onEventBreakGiftShow" },
        { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_EXCHANGE_DIAMOND_GOLD", callBack = "onEventExchange" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVNET_SHOPMANAGER_SHOW_SHOP", callBack = "onEventProductRefresh" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVENT_REQ_EXCHANGE_GOLD_SUCCESS", callBack = "onEventExchangeGold" },
        { module = XH.lobby:getModule("ToponAct"), eventKeyName = "KW_TOPON_GOLD_BREAK_SHOW", callBack = "onEventShowToponAct" },
        { module = XH.lobby:getModule("ToponAct"), eventKeyName = XH.lobby:getModule("ToponAct").KW_TOPON_ACT_FULSH_CNT, callBack = "onEventClose" },
        { module = XH.lobby:getModule("YGiftBankruptcy"), eventKeyName = "FLUSH_ACT_INFO", callBack = "onEventShowGiftBankrupt" },
    }
end

function BaseLiveView:ctor(param)
    param = param or {}
    BaseLiveView.super.ctor(self, param)
    self:initData(param)
    self:show()
    XH.SysTool.performDelayOnce(function()
        if self and self.judgeGoldThrow then
            self:judgeGoldThrow()
        end
    end, 1)
end

-- 判断是否要抛送埋点
function BaseLiveView:judgeGoldThrow()
    if self._isGoldThrow then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_GOLD_THROW_ERQI_1, nil, { area_id = XH.areaData:getAreaID() })
    end
end

function BaseLiveView:initData(param)
    self._toponActInfo = nil
    self._minDiamondProduct = nil
    self._maxDiamondProduct = nil
    self._diamondProducts = {}
    self._goldExchangeProducts = {}
    self._goldNotEnough = param.goldNotEnough or false
    self._roomFlag = param.roomFlag or -1
    self._showBankupt = param.showBankupt or false -- 是否只显示新破产礼包（从大厅入口进）
    self._gameID = param.gameID or 0 -- 判断是否用新的界面
    self._scene = param.scene or 0  -- 进入场景(大厅，金币场大厅，游戏区)
    self.allCfg = XH.lobby:getModule("Configuration"):getConfigJsonData(ConfigurationDefine.Config.ALL, "AllConfig")
    self:reqBaseLiveTask()
    local _, subId = XH.lobby:getModule("Shop"):getTagsWithName(ShopConfig.SpecialTag.DIAMOND)
    if not XH.lobby:getModule("Shop"):getShopItemsWithId(subId) then
        XH.lobby:getModule("Shop"):reqProductList(ShopConfig.ProductType.DIAMOND)
    end
    local _, goldSubId = XH.lobby:getModule("Shop"):getTagsWithName(ShopConfig.SpecialTag.GOLD)
    if goldSubId and not XH.lobby:getModule("Shop"):getShopItemsWithId(goldSubId) then
        XH.lobby:getModule("Shop"):reqShopProductByConfig(goldSubId)
    end
    XH.lobby:getModule("Shop"):reqProductList(ShopConfig.ProductType.GOLD)
    if self:isNewBankupt() then
        XH.lobby:getModule("YGiftBankruptcy"):reqPlayerInfo()
    end
end

function BaseLiveView:updateLayerUI(data)
    if self._showBankupt then
        self:updateLayerUINew()
        return
    end
    if self:isNewBankupt() and self._roomFlag and self._roomFlag ~= -1 then
        self:updateLayerUINew(data)
        return
    end
    self._goodsList:removeAllItems()
    self._isGoldThrow = false
    local lobbyid = XH.areaData:getLobbyID()
    if self._goldNotEnough then
        if QuickChargeConfig[lobbyid] then
            self:addGoldSceneIcon(true)
            self:addGoodsItem(GOODS_ITEMS_INDEX.QUICK_BUY)
        end
        return
    end
    if data.isBuyGift == 1 then
        -- 玩家已购买破产礼包
        if self:isBankruptUseUp() then
            self:addGoodsItem(GOODS_ITEMS_INDEX.BREAKSHARE)
            -- self:addGoldSceneIcon(true)
            self:addGoodsItem(GOODS_ITEMS_INDEX.QUICK_BUY)
        else
            self:addGoodsItem(GOODS_ITEMS_INDEX.BREAK)
            self:addGoldSceneIcon(true)
        end
    else
        -- 玩家未购买破产礼包
        if self:isBankruptUseUp() then
            -- 次数已经用完
            if data.isShowBtn and device.platform ~= "windows" then
                self:addGoodsItem(GOODS_ITEMS_INDEX.BREAKSHARE)
                self:addGoodsItem(GOODS_ITEMS_INDEX.SALE)
                -- self:addGoldSceneIcon(true)
            else
                -- self:addGoldSceneIcon(true)
                self:addGoodsItem(GOODS_ITEMS_INDEX.BREAKSHARE)
                self:addGoodsItem(GOODS_ITEMS_INDEX.QUICK_BUY)
            end
        else
            if data.isShowBtn then
                self:addGoodsItem(GOODS_ITEMS_INDEX.BREAK)
                self:addGoodsItem(GOODS_ITEMS_INDEX.SALE)
            else
                self:addGoodsItem(GOODS_ITEMS_INDEX.BREAK)
                self:addGoldSceneIcon(true)
            end
        end
    end
    XH.lobby:getModule("ToponAct"):reqToponBreakActInfo()
end

-- 增加连炸斗地主入口
function BaseLiveView:addGoldSceneIcon(isJudge)
    local nedReq = XH.lobby:getModule("ToponAct"):canReqToponBreakActInfo()
    if isJudge and nedReq then
        return
    end
    if not XH.lobby:getModule("NewGoldScene"):getIconVisible() then
        return
    end
    self._isGoldThrow = true
    self:addGoodsItem(GOODS_ITEMS_INDEX.GOLD_SCENE)
end

function BaseLiveView:onEventBreakGiftShow(event)
    if event.msg == "" then
        return
    end
    local data = json.decode(event.msg)
    if data == nil or next(data) == nil then
        return
    end
    --    if self._breakGiftBtn == nil or self._breakTimeText == nil or self._moreBtn == nil then return end
    --判断玩家是否可领取低保
    --判断玩家是否可领特惠礼包
    self:updateLayerUI(data)
end

function BaseLiveView:onEventProductRefresh(data)
    if data then
        local _, subId = XH.lobby:getModule("Shop"):getTagsWithName(ShopConfig.SpecialTag.DIAMOND)
        self._diamondProducts = XH.lobby:getModule("Shop"):getShopItemsWithId(subId) or {}
        -- 设置充值按钮的值
        self._goldExchangeProducts = XH.lobby:getModule("Shop"):getProductList(ShopConfig.ProductType.GOLD) or {}
        if next(self._diamondProducts) and next(self._goldExchangeProducts) then
            self:setExchageProductInfo()
        end
    end
end

function BaseLiveView:setExchageProductInfo()
    self._minDiamondProduct, self._maxDiamondProduct = self:getSuggestDiamondProduct(self._diamondProducts)
    if self._minDiamondProduct and self._maxDiamondProduct then
        self._targetProduct = self._minDiamondProduct
        if self._roomFlag and self._roomFlag > 2 then
            self._targetProduct = self._maxDiamondProduct
        end
        if self._goodsList then
            local items = self._goodsList:getItems()
            if items then
                for i = 1, #items do
                    if tonumber(items[i]:getName()) == GOODS_ITEMS_INDEX.QUICK_BUY then
                        local exchageProduct = self:getExchangeProduct()
                        if exchageProduct and exchageProduct.name then
                            XH.UITool.setText(items[i], "_KW_TEXT_ITEM_CNT", exchageProduct.name)
                            XH.UITool.setText(items[i], "_KW_TEXT_PRICE", exchageProduct.price)
                        end
                    end
                end
            end
        end
    end
end

function BaseLiveView:getExchangeProduct()
    if self:isNewBankupt() then
        return self:getExchangeProductNew()
    end
    if self._goldExchangeProducts and self._targetProduct then
        for _, v in pairs(self._goldExchangeProducts) do
            if tonumber(self._targetProduct.price) * 100 == tonumber(v.price) then
                return v
            end
        end
    end
    return {}
end

function BaseLiveView:getSuggestDiamondProduct(diamondProducts)
    local lobbyid = XH.areaData:getLobbyID()
    local min, max
    if QuickChargeConfig[lobbyid] then
        for _, v in pairs(diamondProducts) do
            if tonumber(v.price) == QuickChargeConfig[lobbyid].priceMin then
                min = v
            end
            if tonumber(v.price) == QuickChargeConfig[lobbyid].priceMax then
                max = v
            end
        end
    end
    return min, max
end


function BaseLiveView:reqBaseLiveTask()
    XH.taskManager:reqBaseLiveTask()
end

function BaseLiveView:reqBreakGiftBagTime()
    XH.taskManager:reqBreakGiftBagTime()
end

function BaseLiveView:updateBaseLiveInfo(baseLiveInfo)
    self:updateLayerUI(baseLiveInfo)
end

function BaseLiveView:runBreakGiftDownTime(time)
    if self._breakTimeText == nil then
        return
    end
    time = tonumber(time) or 0
    self._breakTimeText:stopAllActions()
    local cb = cc.CallFunc:create(function()
        time = time - 1
        if time >= 0 then
            local h = math.floor(time / 3600)
            local s = math.floor((time - h * 3600) / 60)
            local m = time - (h * 3600 + s * 60)
            local strTime = string.format("%02d:%02d:%02d", h, s, m)
            self._breakTimeText:setString(strTime)
        else
            self._breakTimeText:stopAllActions()
        end
    end)
    self._breakTimeText:runAction(cc.RepeatForever:create(cc.Sequence:create(cb, cc.DelayTime:create(1))))
end

function BaseLiveView:show()
    local baseLiveInfo = XH.taskManager._baseLiveInfo
    if not baseLiveInfo then return end
    self:updateBaseLiveInfo(baseLiveInfo)
end

function BaseLiveView:onEventBaseLive(event)
    local baseLiveInfo = event.baseLiveInfo
    if not baseLiveInfo then return end
    self:updateBaseLiveInfo(baseLiveInfo)
    self:reqBreakGiftBagTime()
end

function BaseLiveView:onTouchEventCloseBtn(send, eventType)
    -- 裂变活动提示
    local bNoBreakGift = true
    if self._goodsList then
        local items = self._goodsList:getItems()
        for _, v in pairs(items) do
            if tonumber(v:getName()) == GOODS_ITEMS_INDEX.BREAK then
                bNoBreakGift = false
                break
            end
        end
        if not bNoBreakGift then
            XH.taskManager:reqTaskGetAward(TaskDefine.TASK_ID.BASE_LIVE_THREE)
        end
    end
    if bNoBreakGift or self:isNewBankupt() then
        local eventDispatcher = cc.Director:getInstance():getEventDispatcher()
        local event = cc.EventCustom:new("GoldBaseLive.Close")
        eventDispatcher:dispatchEvent(event)
    end
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.GOLD_TABLE_02)
    if not self or tolua.isnull(self) then
        return
    end
    self:close()
end

function BaseLiveView:onTouchEventMore(send, eventType)
    XH.viewManager:openView("ShopView", nil, { productType = ShopConfig.SpecialTag.GOLD })
    self:close()
end

-- 判断是否破产且有低保领取
function BaseLiveView:JudgeBankrupt()
    local baseLiveInfo = XH.taskManager._baseLiveInfo
    if not baseLiveInfo then
        return false
    end
    local playerSR = XH.playerData:getSR() or 0
    -- 低于多少破产
    local condition = tonumber(string.match(baseLiveInfo.sTip, "%d+"))
    local ntotal = baseLiveInfo.total or 0
    local nComplete = baseLiveInfo.nComplete or 0

    if playerSR > condition then
        XH.TipTool.showToast("只有低于" .. condition .. "金币才可以领取哦!", 3)
        return false
    end

    if ntotal - nComplete <= 0 then
        XH.TipTool.showToast("您今日的领取次数已用完!", 3)
        return false
    end

    return true
end

--返回金币破产次数是否已经用完
function BaseLiveView:isBankruptUseUp()
    local baseLiveInfo = XH.taskManager._baseLiveInfo
    if not baseLiveInfo then
        return false
    end
    local ntotal = baseLiveInfo.total or 0
    local nComplete = baseLiveInfo.nComplete or 0
    if ntotal - nComplete <= 0 then
        return true
    end
    return false
end

function BaseLiveView:onTouchEventReq(send, eventType)
    if not self:JudgeBankrupt() then
        return
    end
    if self:isNewBankupt() then
        -- XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.jblb2024052904, { page_label = "破产礼包", block_item_id = self._gameID, item_id = "救济金", block_label = "领取" }, { choice = "", info = "" })
    end
    XH.taskManager:reqTaskGetAward(TaskDefine.TASK_ID.BASE_LIVE_THREE)
    self:close()
end

function BaseLiveView:onTouchEventBreakGift(send, eventType)
    if XH.playerData:checkNewRealName() ~= 0 then return end--实名限制
    if XH.playerData:checkNewRealNameVisitor() ~= 0 then return end--游客登陆
    if self:isNewBankupt() and self:isBankruptBuy() then
        XH.lobby:getModule("YGiftBankruptcy"):reqBuyBankuptcyGift()
        self:close()
        return
    end
    XH.viewManager:openView("GiftBagView")
    self:close()
end

-- 新版兑换走最低限制逻辑
function BaseLiveView:onTouchEventChargeNew()
    local playerDiamond = XH.playerData:getDiamnd() or 0
    local exchageProduct = self:getExchangeProduct()
    --玩家钻石足够直接兑换金币
    if exchageProduct and playerDiamond >= tonumber(exchageProduct.price) then
        local gId, sId = XH.lobby:getModule("Shop"):getTagsWithName(ShopConfig.SpecialTag.GOLD)
        XH.lobby:getModule("Shop"):reqExchangeProduct(sId, exchageProduct.id, nil, nil, nil, nil, nil, nil, nil, ShopConfig.ProductType.GOLD)
    else
        --玩家钻石不足,走钻石充值流程
        if self._targetProduct then
            --给task做区分的
            XH.lobby:getModule("Shop"):reqBuyDiamond(self._targetProduct, "deliver_award", XH.lobby:getModule("Shop").BUY_DIAMOND_SEPCIAL_TAG, nil, nil, nil, -1)
        else
            XH.TipTool.showToast("没有对应的充值商品!")
        end
    end
    -- XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.jblb2024052904, { page_label = "破产礼包", block_item_id = self._gameID, item_id = "普通商品", block_label = "领取" }, { choice = "", info = "" })
end

function BaseLiveView:onTouchEventCharge()
    if self:isNewBankupt() then
        self:onTouchEventChargeNew()
        return
    end
    --判断玩家身上的钻石是否足够
    if self._targetProduct then
        local playerDiamond = XH.playerData:getDiamnd() or 0
        if playerDiamond >= tonumber(self._targetProduct.value) then
            --玩家钻石足够直接兑换金币
            local exchageProduct = self:getExchangeProduct()
            if exchageProduct then
                XH.lobby:getModule("Shop"):reqExchangeProduct(ShopConfig.ProductType.GOLD, exchageProduct.id)
            end
        else
            --玩家钻石不足,走钻石充值流程
            if self._targetProduct then
                --给task做区分的
                XH.lobby:getModule("Shop"):reqBuyDiamond(self._targetProduct, "deliver_award", XH.lobby:getModule("Shop").BUY_DIAMOND_SEPCIAL_TAG, nil, nil, nil, -1)
            end
        end
    end
end

function BaseLiveView:onEventExchangeGold()
    XH.TipTool.hideLoading()
    XH.TipTool.showToast("兑换成功", 3)
    self:close()
end

function BaseLiveView:onEventExchangeDiamondToGold(event)
    --兑换钻石到金币
    if event.msg ~= "" then
        local data = json.decode(event.msg)
        if data and next(data) then
            local exchageProduct = self:getExchangeProduct()
            if exchageProduct then
                XH.lobby:getModule("Shop"):reqExchangeProduct(ShopConfig.ProductType.GOLD, exchageProduct.id)
            end
            self:close()
        end
    end
    XH.taskManager:reqPlayerDropData()
end

function BaseLiveView:onEventExchange(event)
    if event.msg ~= "" then
        local data = json.decode(event.msg)
        if data and next(data) then
            local exchageProduct = self:getExchangeProduct()
            if exchageProduct then
                XH.lobby:getModule("Shop"):reqExchangeProduct(ShopConfig.ProductType.GOLD, exchageProduct.id)
            end
            self:close()
        end
    end
    XH.taskManager:reqPlayerDropData()
end

function BaseLiveView:addGoodsItem(index)
    if self._goodsList and #self._goodsList:getItems() >=2 then
        return
    end
    local goodsItem = self[GOODS_ITEM_INFOS[index].sender]
    if goodsItem then
        local addItem = goodsItem:clone()
        addItem:setName(index)
        XH.UITool.addTouchEventListener(addItem, "_KW_BTN_ITEM_GET", XH.UITool.getDefaultTouchHanlde(self, self[GOODS_ITEM_INFOS[index].onTouchEnded]))
        if GOODS_ITEM_INFOS[index].leftTimes then
            local ntotal, nComplete = self[GOODS_ITEM_INFOS[index].leftTimes](self)
            XH.UITool.setText(addItem, "_KW_TEXT_ITEM_LEFT_TIMES", "已领取（" .. nComplete .. "/" .. ntotal .. "）")
        end
        self._goodsList:pushBackCustomItem(addItem)
        local itemCnt = #self._goodsList:getItems()
        local listWidth = addItem:getContentSize().width * itemCnt
        self._goodsList:setSize(cc.size(listWidth, self._goodsList:getContentSize().height))

        if index == GOODS_ITEMS_INDEX.BREAK then
            if XH.taskManager._baseLiveInfo and XH.taskManager._baseLiveInfo.addcnt then
                XH.UITool.setText(addItem, "_KW_TEXT_ITEM_CNT", XH.taskManager._baseLiveInfo.addcnt .. "金币")
            end
        end

        -- 新金币场逻辑相关处理
        if self:isNewBankupt() and index == GOODS_ITEMS_INDEX.SALE then
            self:flushBankurptItem(addItem)
        end
        if self:isNewBankupt() and index == GOODS_ITEMS_INDEX.SALE_EX then
            self:flushBankurptItem(addItem, true)
        end

        if index == GOODS_ITEMS_INDEX.QUICK_BUY then
            local exchageProduct = self:getExchangeProduct()
            if exchageProduct and exchageProduct.name then
                XH.UITool.setText(addItem, "_KW_TEXT_ITEM_CNT", exchageProduct.name)
                XH.UITool.setText(addItem, "_KW_TEXT_PRICE", exchageProduct.price)
            end
        end

        if index == GOODS_ITEMS_INDEX.BREAKSHARE and self.allCfg and self.allCfg.BreakShareGold then
            XH.UITool.setText(addItem, "_KW_TEXT_ITEM_CNT", self.allCfg.BreakShareGold .. "金币")
        end
    end
end

--返回金币破产次数领取次数与总次数
function BaseLiveView:getBankruptUseTimes()
    local baseLiveInfo = XH.taskManager._baseLiveInfo
    if not baseLiveInfo then
        return 0, 0
    end
    local ntotal = baseLiveInfo.total or 0
    local nComplete = baseLiveInfo.nComplete or 0
    return ntotal, nComplete
end

--返回金币破产次数领取次数与总次数
function BaseLiveView:onEventShowToponAct(event)
    if self:isNewBankupt() and self._roomFlag >= 4 then
        return
    end
    local data = event.data
    if data then
        if self._toponActInfo then
            self._toponActInfo.maxLimtCnt = data.maxLimtCnt
            self._toponActInfo.limitCnt = data.limitCnt
        else
            self._toponActInfo = data
        end
        local find = false
        local Items = self._goodsList:getItems()
        if Items then
            local ntotal = -1
            local nComplete = -2
            for _, Item in ipairs(Items) do
                if tonumber(Item:getName()) == GOODS_ITEMS_INDEX.ADDS then
                    find = true
                    ntotal, nComplete = self[GOODS_ITEM_INFOS[GOODS_ITEMS_INDEX.ADDS].leftTimes](self)
                    XH.UITool.setText(Item, "_KW_TEXT_ITEM_LEFT_TIMES", "已领取（" .. nComplete .. "/" .. ntotal .. "）")
                    break
                end
            end
            if not find then
                if not XH.ChannelTool.checkIsECarChannel() then          --车载渠道不显示看广告类活动
                    if self:isNewBankupt() then
                        if ntotal ~= nComplete then
                            self:updateLayerUINew(nil, true)
                        end
                        return
                    end
                    if ntotal == nComplete then
                        self:addGoldSceneIcon(false)
                    else
                        self:addGoodsItem(GOODS_ITEMS_INDEX.ADDS)
                    end
                end
            end
        end
    end
end

function BaseLiveView:getWatchAddsTimes()
    return self._toponActInfo.maxLimtCnt, self._toponActInfo.limitCnt
end

function BaseLiveView:onTouchEventGetToponAaward(send, eventType)
    if self._toponActInfo.limitCnt >= self._toponActInfo.maxLimtCnt then
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, "今日抽奖次数已达上限")
    else
        local webType = self._toponActInfo.webType
        local poolid = self._toponActInfo.actInfo.poolid
        self:close()
        XH.lobby:getModule("ToponAct"):getToponActAward(webType, poolid)
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.GOLD_TABLE_01)
        if self:isNewBankupt() then
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.jblb2024052904, { page_label = "破产礼包", block_item_id = self._gameID, item_id = "广告奖励", block_label = "领取" }, { choice = "", info = "" })
        end
    end
end

function BaseLiveView:onEventClose(event)
    local data = event.data
    if data then
        local ToponActConfig = require("lobby.Modules.ToponAct.Config")
        if data.webType == ToponActConfig.BreakGold then
            self:close()
        end
    end
end

function BaseLiveView:onTouchEventGoldScene()
    if self:isNewBankupt() then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.jblb2024052904, { page_label = "破产礼包", block_item_id = self._gameID, item_id = "高消产品引导", block_label = "领取" }, { choice = "", info = "" })
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_GOLD_THROW_ERQI_2, nil, { area_id = XH.areaData:getAreaID() })
    XH.lobby:getModule("Lobby"):enterNewGold()
    self:close()
end

function BaseLiveView:reqBreakShareGoldAward()
    XH.taskManager:reqTaskPercent(TaskDefine.TASK_ID.SHARE_GIVE_GOLD)
    if self and self.close then
        self:close()
    end
end

function BaseLiveView:onTouchEventReqBreakShare()
    -- self:reqBreakShareGoldAward()
    XH.lobby:getModule("Share"):startShareFzb(self.reqBreakShareGoldAward, nil, self)
end

---------------------- 新版礼包写法
function BaseLiveView:isNewBankupt()
    return XH.lobby:getModule("YGiftBankruptcy"):isNewModeGold(self._gameID) or self._showBankupt
end

function BaseLiveView:onEventShowGiftBankrupt(event)
    if not self:isNewBankupt() then
        return
    end
    self:updateLayerUINew()
end

-- 新破产礼包是否显示
function BaseLiveView:flushShowGiftBankrupt()
    local info = XH.lobby:getModule("YGiftBankruptcy"):getPlayerActInfo()
    if info then
        local tmpInfo = ""
        for i = 1, 2 do
            if info[i] then
                tmpInfo = tmpInfo .. info[i].goodsId .. ","
            end
            if i == 1 and info[i] then
                self:addGoodsItem(GOODS_ITEMS_INDEX.SALE)
            elseif i == 2 and info[i] then
                self:addGoodsItem(GOODS_ITEMS_INDEX.SALE_EX)
            end
        end
        if tmpInfo ~= "" then
            local infoX = {
                item_id = "直充礼包",
                choice = "",
                info = tmpInfo,
            }
            self:pushThrowShowData(infoX)
        end
        return true
    end
    return false
end

-- 新双扣获取可以进入的最低档位
function BaseLiveView:getExchangeProductNew()
    local flag = cc.UserDefault:getInstance():getIntegerForKey("_KW_DATA_LAST_JOIN_GOLD_LEVEL_" .. XH.areaData:getLobbyID(), -1)
    local confID = XH.userDefault:getValue(XH.userDefault.KEY_ID.LAST_JOIN_GOLD_INDEX, -1)
    if self._goldExchangeProducts and next(self._goldExchangeProducts) and self._diamondProducts and next(self._diamondProducts) and flag ~= -1 and confID ~= -1 then
        local richMin = 0
        local goldRoomInfo = XH.goldConfigManager:getGoldRoomInfoByIndex(confID)
        if goldRoomInfo then
            local _levelRoom = goldRoomInfo.roomLevelInfos
            for _, roominfo in pairs(_levelRoom) do
                if roominfo.roomnameflag == flag then
                    richMin = roominfo.minrich
                    break
                end
            end
        end
        local tmp = nil
        for _, v in pairs(self._goldExchangeProducts) do
            if tonumber(v.value) >= richMin then
                if tmp and tonumber(tmp.value) > tonumber(v.value) then
                    tmp = v
                end
                if tmp == nil then
                    tmp = v
                end
            end
        end
        if tmp then
            local tmpDiamond = nil
            if self._diamondProducts then
                for _, v in pairs(self._diamondProducts) do
                    if tonumber(v.price) >= tonumber(tmp.price) / 100 then
                        if tmpDiamond and tonumber(tmpDiamond.price) > tonumber(v.price) then
                            tmpDiamond = v
                        end
                        if tmpDiamond == nil then
                            tmpDiamond = v
                        end
                    end
                end
            end
            self._targetProduct = tmpDiamond
            return tmp
        end
    end
    return {}
end


-- 新版显示入口
function BaseLiveView:updateLayerUINew(data, isShowAd)
    self._pushData = {}
    self._goodsList:removeAllItems()
    self._isGoldThrow = false

    if self._showBankupt then
        self:flushShowGiftBankrupt()
        return
    end
    if self._roomFlag >= 4 then
        -- 破产礼包+正常商品
        local isAdd = self:flushShowGiftBankrupt()
        if not isAdd then 
            self:addGoodsItem(GOODS_ITEMS_INDEX.QUICK_BUY)
            local info = {
                item_id = "普通商品",
                choice = "",
                info = "",
            }
            self:pushThrowShowData(info)
        end 
    else
        -- 破产礼包+资产补助+广告奖励+连炸引导+正常商品
        local index = 0
        if not self:isBankruptUseUp() then
            index = index + 1
            self:addGoodsItem(GOODS_ITEMS_INDEX.BREAK)
            local info = {
                item_id = "救济金",
                choice = "",
                info = "",
            }
            self:pushThrowShowData(info)
        end
        local isAdd = self:flushShowGiftBankrupt()
        if isAdd then
            index = index + 1
        end
        if index == 0 then
            self:addGoodsItem(GOODS_ITEMS_INDEX.QUICK_BUY)
            local info = {
                item_id = "普通商品",
                choice = "",
                info = "",
            }
            self:pushThrowShowData(info)
        end
    end
    XH.lobby:getModule("ToponAct"):reqToponBreakActInfo()
end

-- 是否触发破产购买
function BaseLiveView:isBankruptBuy()
    local info = XH.lobby:getModule("YGiftBankruptcy"):getPlayerActInfo()
    if info then
        return true
    end
    return false
end

function BaseLiveView:onTouchEventBreakGiftEx(send, eventType)
    if XH.playerData:checkNewRealName() ~= 0 then return end--实名限制
    if XH.playerData:checkNewRealNameVisitor() ~= 0 then return end--游客登陆
    if self:isNewBankupt() and self:isBankruptBuy() then
        XH.lobby:getModule("YGiftBankruptcy"):reqBuyBankuptcyGift(true)
        self:close()
        return
    end
    XH.viewManager:openView("GiftBagView")
    self:close()
end

function BaseLiveView:flushBankurptItem(item, isMore)
    local info = XH.lobby:getModule("YGiftBankruptcy"):getPrizeInfo(isMore)
    if not info then
        return
    end
    local addItem = item:getChildByName("KW_IIMG_FRAME_BG")
    if not addItem then
        return
    end
    local today, total = XH.lobby:getModule("YGiftBankruptcy"):getPlayerCnt()
    XH.UITool.setText(addItem, "_KW_TEXT_ITEM_LEFT_TIMES", "已领取（" .. today .. "/" .. total .. "）")
    addItem:getChildByName("_KW_IMG_ITEM_SALE_ICON"):setVisible(false)
    addItem:getChildByName("_KW_IMG_BG_DIS"):setVisible(true)
    addItem:getChildByName("_KW_IMG_BG_DIS"):getChildByName("_KW_IMG_TEXT_DIS"):setString(string.format("%.1f", 10 * (info.price * 10000) / info.gold))
    XH.UITool.setText(addItem, "_KW_TEXT_ITEM_CNT", info.price .. "元=" .. XH.lobby:getModule("YGiftBankruptcy"):getWGoldScore(info.gold) .. "金币")
end

function BaseLiveView:pushThrowShowData(data)
    if self._pushData == nil then
        self._pushData = {}
    end
    self._pushData[#self._pushData + 1] = data
end

function BaseLiveView:startThrowData()
    if self._pushData and #self._pushData > 0 then
        for i = 1, #self._pushData do
            local info = self._pushData[i]
            local conventionData = { 
                page_label = "破产礼包", 
                block_item_id = self._gameID, 
                item_id = info.item_id 
            }
            local extraData = {
                choice = info.choice, 
                info = info.info
            }
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.jblb2024052903, conventionData, extraData)
        end
    end
    self._pushData = {}
end

function BaseLiveView:onCleanup()
    -- self:startThrowData()
    BaseLiveView.super.onCleanup(self)
end

return BaseLiveView ;�  