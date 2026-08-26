local ShopGiftChooseView = class("ShopGiftChooseView", XH.ViewBase)
-- UI名字定义
local KW_BTN_SINGLE = "_KW_BTN_SINGLE"
local KW_BTN_CHECKBOX = "_KW_BTN_CHECKBOX"
local KW_TEXT_GOODS_NUM = "_KW_TEXT_GOODS_NUM"
local KW_TEXT_GOODS_NAME = "_KW_TEXT_GOODS_NAME"
local KW_IMG_PRODUCT_IMG = "KW_IMG_PRODUCT_IMG"
local KW_IMG_PRODUCT = "KW_IMG_PRODUCT"
--- item 的宽

function ShopGiftChooseView:getCSBPath()
    return "cocosStudio/hall/CSB/ShopGiftChooseLayer.csb"
end

function ShopGiftChooseView:getBindingInfo()
    return {
        ["_KWA_BTND_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KW_IMG_GOODS"] = { varName = "_goodsImg" },
        ["_KW_LAYOUT_ANI_BG"] = { varName = "_aniBg" },
        ["_KW_LAYOUT_ANI_POS"] = { varName = "_aniPos" },
        ["_KW_TEXT_GOODS_DETAIL"] = { varName = "_goodsDetail" },
        ["_KW_TEXT_EXCHANGE_1"] = { varName = "_buyTypeText1" },
        ["_KW_TEXT_EXCHANGE_2"] = { varName = "_buyTypeText2" },
        ["_KW_BTN_TYPE_1"] = { varName = "_checkBoxItem1" },
        ["_KW_BTN_TYPE_2"] = { varName = "_checkBoxItem2" },
        ["_KW_BTN_EXCHANGE"] = { varName = "_btnBuy", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBuyBtnTouchEvent" },
        ["_KW_PANEL_GOODS_BG"] = { varName = "_goodsView" },
        ["_KW_SCROLLVIEW_DISPLAY"] = { varName = "_displayList" },
        ["_KW_TEXT_GITF_NUM"] = { varName = "_giftNum" },
        ["_KW_PANEL_PRODUCT_MOD"] = { varName = "_giftView" },
        ["_KW_BTN_CHECKBOX"] = { varName = "_checkbox" },
        ["_KW_BTN_SINGLE"] = { varName = "_singlebox" },
        ["_KW_TEXT_GOODS_NUM"] = { varName = "_goodsNum" },
    }
end

function ShopGiftChooseView:ctor(param, productInfo, selectIdx, productType, cutPrice)
    ShopGiftChooseView.super.ctor(self, param)
    self._payTpye = 1
    self._propTpye = 0
    self._propID = 0
    self._propValue = ""
    self._data = { }
    self._dataKeys = { }
    self._richType = { }
    self._multiCfId = 1
    self._select_gift = { }
    self._productInfo = productInfo
    self._giftInfo = param
    self._selectIdx = selectIdx or 1
    self._exchangeProducts = { }
    self._radioTable = { }
    self._productType = productType
    self._cutPrice = cutPrice
    self:initUI(param, productInfo)
end

function ShopGiftChooseView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("GiftBag"), eventKeyName = "EVENT_UPDATE_GIFT_EXCHANGE_SELECTED", callBack = "onEventExchangeGift" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVENT_REQ_EXCHANGE_GIFT_BOX_SUCCESS", callBack = "onEventExchangeSuccess" },
    }
end

local PROP_CHOOSE_TYPE = {
    RADIO = 1,
    CHECKBOX = 2,
}

function ShopGiftChooseView:initUI(param, productInfo)
    if productInfo.props_gift_choose_num == 1 then
        self._giftNum:setVisible(false)
        self._chooseType = PROP_CHOOSE_TYPE.RADIO
    else
        self._giftNum:setVisible(true)
        self._giftNum:setText("0/" .. productInfo.props_gift_choose_num)
        self._chooseType = PROP_CHOOSE_TYPE.CHECKBOX
    end
    if not productInfo then return end
    for i = 1, #productInfo.prop_boxes_normal do
        local item = self._giftView:clone()
        if item then
            -- 挂载到ScrollView中去
            item:addTouchEventListener(handler(self, self.onTouchEventPropItem))
            self._displayList:addChild(item)
            XH.UITool.setVisible(item, KW_BTN_SINGLE, self._chooseType == PROP_CHOOSE_TYPE.RADIO)
            XH.UITool.setVisible(item, KW_BTN_CHECKBOX, self._chooseType == PROP_CHOOSE_TYPE.CHECKBOX)
            XH.UITool.addEventListener(item, KW_BTN_SINGLE, handler(self, self.onTouchEventSingleChoose))
            XH.UITool.addEventListener(item, KW_BTN_CHECKBOX, handler(self, self.onTouchEventCheckChoose))
            XH.UITool.setTag(item, KW_BTN_SINGLE, i)
            XH.UITool.setTag(item, KW_BTN_CHECKBOX, i)
            XH.UITool.setText(item, KW_TEXT_GOODS_NAME, productInfo.prop_boxes_normal[i].box_display_name)
            -- 金币的数量和道具的数量字段不同
            if productInfo.prop_boxes_normal[i].box_props_type == "gold" then
                XH.UITool.setText(item, KW_TEXT_GOODS_NUM, "x" .. productInfo.prop_boxes_normal[i].box_gold)
            else
                XH.UITool.setText(item, KW_TEXT_GOODS_NUM, "x" .. productInfo.prop_boxes_normal[i].box_props_count)
            end
            local imgNode = XH.UITool.seekWidgetByName(item, KW_IMG_PRODUCT_IMG)
            if imgNode then
                if productInfo.prop_boxes_normal[i].box_props_type == "gold" then
                    local goldNum = productInfo.prop_boxes_normal[i].box_gold
                    local img = XH.UITool.seekWidgetByName(imgNode, KW_IMG_PRODUCT)
                    img:setVisible(true)
                    img:loadTexture(XH.lobby:getModule("GiftBag"):getGoldPropImg(goldNum), ccui.TextureResType.plistType)
                    img:ignoreContentAdaptWithSize(true)
                else
                    local awardProps = XH.lobby:getModule("GiftBag"):getAwardProps(productInfo.prop_boxes_normal)
                    if next(awardProps) then
                        XH.UITool.reloadNodeRemoteImage(imgNode, XH.lobby:getModule("GiftBag"):getAwardImageByProp(awardProps[i]))
                    end
                end
            end
            if self._chooseType == PROP_CHOOSE_TYPE.RADIO then
                table.insert(self._radioTable, item:getChildByName(KW_BTN_SINGLE))
                if i <= productInfo.props_gift_choose_num then
                    self:onTouchEventSingleChoose(item:getChildByName(KW_BTN_SINGLE), ccui.CheckBoxEventType.selected)
                end
                -- elseif self._chooseType == PROP_CHOOSE_TYPE.CHECKBOX  then
                --     if  i <= productInfo.props_gift_choose_num then
                --         self:onTouchEventCheckChoose(item:getChildByName(KW_BTN_CHECKBOX),ccui.CheckBoxEventType.selected)
                --     end
            end
        end
    end

end

-- 关闭按钮
function ShopGiftChooseView:onTouchEventClose(send, event)
    self:close()
end

function ShopGiftChooseView:onTouchEventPropItem(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    if self._chooseType == PROP_CHOOSE_TYPE.RADIO then
        self:onTouchEventSingleChoose(send:getChildByName(KW_BTN_SINGLE), ccui.CheckBoxEventType.selected)
    elseif self._chooseType == PROP_CHOOSE_TYPE.CHECKBOX then
        if send:getChildByName(KW_BTN_CHECKBOX):isSelected() then
            self:onTouchEventCheckChoose(send:getChildByName(KW_BTN_CHECKBOX), ccui.CheckBoxEventType.unselected)
        else
            self:onTouchEventCheckChoose(send:getChildByName(KW_BTN_CHECKBOX), ccui.CheckBoxEventType.selected)
        end
    end
end

-- 玩家选择单选复选框
function ShopGiftChooseView:onTouchEventSingleChoose(send, eventType)
    if send then
        if eventType == ccui.CheckBoxEventType.selected then
            for i = 1, #self._radioTable do
                XH.UITool.setBtnSelect(self._radioTable[i], KW_BTN_SINGLE, send == self._radioTable[i])
            end
            self._select_gift = { }
            table.insert(self._select_gift, send:getTag())
        end
    end
end

-- 玩家选择多选复选框
function ShopGiftChooseView:onTouchEventCheckChoose(send, eventType)
    if send then
        if eventType == ccui.CheckBoxEventType.selected then
            if #self._select_gift < self._productInfo.props_gift_choose_num then
                table.insert(self._select_gift, send:getTag())
                send:setSelected(true)
            else
                XH.TipTool.showToast("已达最大可选内容", 4)
                send:setSelected(false)
            end
        elseif eventType == ccui.CheckBoxEventType.unselected then
            for i = #self._select_gift, 1, -1 do
                if self._select_gift[i] == send:getTag() then
                    table.remove(self._select_gift, i)
                end
            end
            send:setSelected(false)
        end
        self._giftNum:setText(#self._select_gift .. "/" .. self._productInfo.props_gift_choose_num)
    end
end


-- 购买按钮处理
function ShopGiftChooseView:onBuyBtnTouchEvent(send, event)
    -- 未实名不能购买 海外玩家不用实名认证
    if XH.playerData:checkNewRealName() ~= 0 then return end--实名限制
    if XH.playerData:checkNewRealNameVisitor() ~= 0 then return end--游客登陆
    local cha = self._productInfo.props_gift_choose_num - #self._select_gift
    if cha > 0 then
        XH.TipTool.showToast("您还可以选择" .. cha .. "个道具，请选完再购买", 4)
        return
    end
    local useCurDiamond = XH.playerData:getDiamnd()
    local needCost = tonumber(self._giftInfo.multi_score_config[1].value)
    if self._cutPrice then
        needCost = tonumber(self._cutPrice)
    end
    if self._giftInfo.multi_score_config and needCost and useCurDiamond < needCost then
        local param = { }
        param.targetDiamond = needCost or 0
        param.propStr = self._giftInfo.name or ""
        param.giftType = XH.lobby:getModule("GiftBag").GIFTBOX_TYPE.KW_GIFT_BOX_SELECTED
        XH.viewManager:openView("PropChargeLayer", nil, param)
    else
        self:doExchange()
    end
end

function ShopGiftChooseView:doExchange()
    self._exchangeProducts = { }
    local exchangeProduct = { }
    exchangeProduct.props_gift_send_type = XH.lobby:getModule("GiftBag").GIFTSENDAWARDTYPE.PLAYER_SELECTED
    exchangeProduct.prop_boxes_choose = { }
    for i = 1, #self._select_gift do
        if self._productInfo.prop_boxes_normal[self._select_gift[i]] then
            table.insert(exchangeProduct.prop_boxes_choose, self._productInfo.prop_boxes_normal[self._select_gift[i]].box_props_id)
            table.insert(self._exchangeProducts, self._productInfo.prop_boxes_normal[self._select_gift[i]])
        end
    end
    XH.lobby:getModule("Shop"):reqBuyProduct(self._productType, self._selectIdx, json.encode(exchangeProduct))
end

function ShopGiftChooseView:onEventExchangeGift(event)
    XH.SysTool.performWithDelayGlobal( function()
        self:doExchange()
    end , 0.5)
end

function ShopGiftChooseView:onEventExchangeSuccess(event)
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

return ShopGiftChooseView   �+  