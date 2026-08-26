local ShopGiftChanceView = class("ShopGiftChanceView", XH.ViewBase)
-- UI名字定义
local KW_TEXT_TITTLE_POSSIBLE = "_KW_TEXT_TITTLE_POSSIBLE"
local KW_TEXT_TITTLE = "_KW_BMFONT_TITTLE"
local KW_TEXT_GOODS_NAME = "_KW_BMFONT_GOODS_NAME"
local KW_TEXT_GOODS_MUN = "_KW_TEXT_GOODS_MUN"
local KW_TEXT_GOODS_MUN_CHANCE = "_KW_TEXT_GOODS_MUN_CHANCE"

local KW_MAX_TEXT_WIDTH = 240

local KW_MAX_BMFONT_FONT_SIZE = 1.10
local KW_MIN_BMFONT_FONT_SIZE = 0.75



function ShopGiftChanceView:getCSBPath()
    return "cocosStudio/hall/CSB/ShopGiftChanceLayer.csb"
end

function ShopGiftChanceView:getBindingInfo()
    return {
        ["_KW_BTN_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KW_IMG_GOODS"] = { varName = "_goodsImg" },
        ["_KW_LAYOUT_ANI_BG"] = { varName = "_aniBg" },
        ["_KW_LAYOUT_ANI_POS"] = { varName = "_aniPos" },
        ["_KW_TEXT_GOODS_DETAIL"] = { varName = "_goodsDetail" },
        ["_KW_BTN_TYPE_1"] = { varName = "_checkBoxItem1" },
        ["_KW_BTN_TYPE_2"] = { varName = "_checkBoxItem2" },
        ["_KW_BTN_EXCHANGE"] = { varName = "_btnBuy", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBuyBtnTouchEvent" },
        ["_KW_PANEL_GOODS_BG"] = { varName = "_goodsView" },
        ["_KW_SCROLLVIEW_DISPLAY"] = { varName = "_displayList" },
        ["_KW_PANEL_PRODUCT_MOD"] = { varName = "_giftView" },
        ["_KW_IMG_PRODUCT_IMG_BG"] = { varName = "_productbg" },
        ["_KW_TEXT_GIFT_NAME"] = { varName = "_giftName" },
    }
end

function ShopGiftChanceView:ctor(param, productInfo, selectIdx, propType, cutPrice)
    ShopGiftChanceView.super.ctor(self, param)
    self._payTpye = 1
    self._propID = 0
    self._propValue = ""
    self._data = { }
    self._richType = { }
    self._multiCfId = 1
    self._productData = { }
    self._exchangeProducts = { }
    self._selectIdx = selectIdx or 1
    self._propType = propType
    self._cutPrice = cutPrice
    self:initUI(param, productInfo)
end

function ShopGiftChanceView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("GiftBag"), eventKeyName = "EVENT_UPDATE_GIFT_EXCHANGE_AUOTSEND", callBack = "onEventExchangeGift" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVENT_REQ_EXCHANGE_GIFT_BOX_SUCCESS", callBack = "onEventExchangeSuccess" },
    }
end

function ShopGiftChanceView:initUI(productData, productInfo, selectIdx)
    self._productData = productData
    self._productInfo = productInfo or { }
    self._giftName:setText(productData.name)
    XH.UITool.reloadNodeRemoteImage(self._productbg, productData.image)
    if not productInfo then return end
    local item = self._giftView:clone()
    if item then
        item:retain()
        item:removeFromParent()
        self._displayList:addChild(item)
        XH.UITool.setVisible(item, KW_TEXT_TITTLE, true)
        XH.UITool.setVisible(item, KW_TEXT_GOODS_NAME, false)
        XH.UITool.setVisible(item, KW_TEXT_GOODS_MUN, false)
        XH.UITool.setVisible(item, KW_TEXT_GOODS_MUN_CHANCE, false)
        item:release()
    end
    for i = 1, #productInfo.prop_boxes_normal do
        table.insert(self._exchangeProducts, productInfo.prop_boxes_normal[i])
        local item_normal = self._giftView:clone()
        if item_normal then
            item_normal:retain()
            item_normal:removeFromParent()
            self._displayList:addChild(item_normal)
            XH.UITool.setText(item_normal, KW_TEXT_GOODS_NAME, productInfo.prop_boxes_normal[i].box_display_name)
            XH.UITool.adaptBmfontToWidth(item_normal:getChildByName(KW_TEXT_GOODS_NAME), KW_MAX_TEXT_WIDTH, KW_MAX_BMFONT_FONT_SIZE, KW_MIN_BMFONT_FONT_SIZE)
            if productInfo.prop_boxes_normal[i].box_props_type == "gold" then
                XH.UITool.setText(item_normal, KW_TEXT_GOODS_MUN, "x" .. productInfo.prop_boxes_normal[i].box_gold)
            else
                XH.UITool.setText(item_normal, KW_TEXT_GOODS_MUN, "x" .. productInfo.prop_boxes_normal[i].box_props_count)
            end
            -- TODO 其他类型的道具展示
            XH.UITool.setVisible(item_normal, KW_TEXT_GOODS_MUN_CHANCE, false)
            item_normal:release()
        end
    end
    local item_tittle = self._giftView:clone()
    if item_tittle then
        item_tittle:retain()
        item_tittle:removeFromParent()
        self._displayList:addChild(item_tittle)
        if #productInfo.prop_boxes_chance == 0 then
            XH.UITool.setVisible(item_tittle, KW_TEXT_TITTLE_POSSIBLE, false)
        else
            XH.UITool.setVisible(item_tittle, KW_TEXT_TITTLE_POSSIBLE, true)
        end
        XH.UITool.setVisible(item_tittle, KW_TEXT_GOODS_NAME, false)
        XH.UITool.setVisible(item_tittle, KW_TEXT_GOODS_MUN, false)
        XH.UITool.setVisible(item_tittle, KW_TEXT_GOODS_MUN_CHANCE, false)
        item_tittle:release()
    end
    for i = 1, #productInfo.prop_boxes_chance do
        table.insert(self._exchangeProducts, productInfo.prop_boxes_chance[i])
        local item_chance = self._giftView:clone()
        if item_chance then
            item_chance:retain()
            item_chance:removeFromParent()
            self._displayList:addChild(item_chance)
            XH.UITool.setText(item_chance, KW_TEXT_GOODS_NAME, productInfo.prop_boxes_chance[i].box_display_name)
            if productInfo.prop_boxes_chance[i].box_props_type == "gold" then
                XH.UITool.setText(item_chance, KW_TEXT_GOODS_MUN, "x" .. productInfo.prop_boxes_chance[i].box_gold)
            else
                XH.UITool.setText(item_chance, KW_TEXT_GOODS_MUN, "x" .. productInfo.prop_boxes_chance[i].box_props_count)
            end
            XH.UITool.setText(item_chance, KW_TEXT_GOODS_MUN_CHANCE, productInfo.prop_boxes_chance[i].box_chance .. "%")
            item_chance:release()
        end
    end
end

-- 关闭按钮
function ShopGiftChanceView:onTouchEventClose(send, event)
    self:close()
end

-- 检测是否在活动时间
function ShopGiftChanceView:checkIsActTime(goodsAct, productData)
    if goodsAct and goodsAct ~= "" and XH.lobby:getModule("Shop"):judgeIsActOut(goodsAct, productData) then
        return true
    end
    return false
end

-- 活动兑换
function ShopGiftChanceView:exchangeActItem(goodsAct, productData)
    local useCurDiamond = XH.playerData:getDiamnd()
    if self._productData.goods_act.act_config and self._productData.goods_act.act_config[self._productData.goods_act.act_type] then
        local prize = self._productData.goods_act.act_config[self._productData.goods_act.act_type][1].value
        if tonumber(prize) and useCurDiamond < tonumber(prize) then
            local param = { }
            param.targetDiamond = tonumber(prize) or 0
            param.propStr = self._productData.name or ""
            -- 礼包名字
            param.giftType = XH.lobby:getModule("GiftBag").GIFTBOX_TYPE.KW_GIFT_BOX_AUTO
            -- 礼包类型
            XH.viewManager:openView("PropChargeLayer", nil, param)
        else
            -- 兑换流程
            self:doExchange()
        end
        return true
    end
    return false
end

-- 购买按钮处理
function ShopGiftChanceView:onBuyBtnTouchEvent(send, event)

    -- 未实名不能购买 海外玩家不用实名认证
    if XH.playerData:checkNewRealName() ~= 0 then return end--实名限制
    if XH.playerData:checkNewRealNameVisitor() ~= 0 then return end--游客登陆
    -- 获取玩家的钻石
    local useCurDiamond = XH.playerData:getDiamnd()
    if self:checkIsActTime(self._productData.goods_act, self._productData) then
        self:exchangeActItem(self._productData.goods_act, self._productData)
        return
    end
    local needCost = tonumber(self._productData.multi_score_config[1].value)
    if self._cutPrice then
        needCost = tonumber(self._cutPrice)
    end
    if self._productData.multi_score_config and needCost and useCurDiamond < needCost then
        local param = { }
        param.targetDiamond = needCost or 0
        param.propStr = self._productData.name or ""
        -- 礼包名字
        param.giftType = XH.lobby:getModule("GiftBag").GIFTBOX_TYPE.KW_GIFT_BOX_AUTO
        -- 礼包类型
        XH.viewManager:openView("PropChargeLayer", nil, param)
    else
        -- 兑换流程
        self:doExchange()
    end
end

function ShopGiftChanceView:doExchange()
    local exchangeProduct = { }
    exchangeProduct.props_gift_send_type = XH.lobby:getModule("GiftBag").GIFTSENDAWARDTYPE.SERVER_AUTO_SEND
    -- 客户端和web端的约定
    XH.lobby:getModule("Shop"):reqBuyProduct(self._propType, self._selectIdx, json.encode(exchangeProduct))
end

function ShopGiftChanceView:onEventExchangeGift(event)
    XH.SysTool.performWithDelayGlobal( function()
        self:doExchange()
    end , 0.5)
end

function ShopGiftChanceView:onEventExchangeSuccess(event)
    if event.data and event.data.status == XH.Req.TYPE.SUCCESS then
        local awards = { }
        local awardProps = XH.lobby:getModule("GiftBag"):getAwardProps(self._exchangeProducts)
        for i = 1, #awardProps do
            local award = { }
            award.url = XH.lobby:getModule("GiftBag"):getAwardImageByProp(awardProps[i])
            award.desc = awardProps[i].name
            -- .. "x" .. awardProps[i].count
            award.propType = awardProps[i].type
            table.insert(awards, award)
        end
        XH.viewManager:openView("GiftRewardView", nil, awards)
    end
    self:close()
end

return ShopGiftChanceView   -&  