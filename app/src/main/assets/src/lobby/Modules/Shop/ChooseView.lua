local ShopChooseView = class("ShopChooseView", XH.ViewBase)
local ShopConfig = require("lobby.Modules.Shop.Config")
-- UI名字定义
local KW_IMG_GOODS_FIRST_LOGO = "_KW_IMG_GOODS_FIRST_LOGO"
local KW_IMG_GOODS_SELECT = "_KW_IMG_GOODS_SELECT"
local KW_TEXT_DAY_CNT = "_KW_TEXT_DAY_CNT"
local KW_TEXT_PRICE = "_KW_TEXT_PRICE"
local KW_TEXT_OLD_PRICE = "_KW_TEXT_OLD_PRICE"
local KW_TEXT_TIMEDATE = "_KW_TEXT_TIMEDATE"
local KW_IMG_CUT_LINE = "_KW_IMG_CUT_LINE"
local DEFAULT_DOWNLOAD_IMAGE_DIR = device.writablePath .. "tmpImageCachDir/"
--- item 的宽
local KW_PROP_LIST_ITEM_WIDTH = 280
--- item item位置适配数n
local KW_PROP_LIST_ITEM_ADAPT_COUNT = 2
--- item 之前最小间距(当item数大于适配数n时使用)
local KW_PROP_LIST_ITEM_MIN_MARGIN = 20
--- item 之前最大间距(当item数不大于适配数n时使用)
local KW_PROP_LIST_ITEM_MAX_MARGIN = 50

function ShopChooseView:getCSBPath()
    return "cocosStudio/hall/CSB/ShopChooseLayer.csb"
end

function ShopChooseView:getBindingInfo()
    return {
        ["_KW_BTN_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose"},
        ["_KW_IMG_GOODS"] = { varName = "_goodsImg" },
        ["_KW_LAYOUT_ANI_BG"] = { varName = "_aniBg" },
        ["_KW_LAYOUT_ANI_POS"] = { varName = "_aniPos" },

        ["_KW_TEXT_GOODS_NAME"] = { varName = "_goodsName" },
        ["_KW_TEXT_GOODS_DETAIL"] = { varName = "_goodsDetail" },

        ["_KW_TEXT_EXCHANGE_1"] = { varName = "_buyTypeText1" },
        ["_KW_TEXT_EXCHANGE_2"] = { varName = "_buyTypeText2" },
        ["_KW_BTN_TYPE_1"] = { varName = "_checkBoxItem1" },
        ["_KW_BTN_TYPE_2"] = { varName = "_checkBoxItem2" },
        ["_KW_BTN_EXCHANGE"] = { varName = "_btnBuy", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBuyBtnTouchEvent" },
        ["_KW_PANEL_GOODS_BG"] = { varName = "_goodsView" },
        ["_KW_SCROLLVIEW_DISPLAY"] = { varName = "_displayList" },
    }
end

function ShopChooseView:ctor(param, productType)
    ShopChooseView.super.ctor(self, param)
    self._payTpye = 1
    self._propTpye = 0
    self._propID = 0
    self._propValue = ""
    self._data = { }
    self._dataKeys = { }
    self._richType = { }
    self._shopPropType = nil
    self._multiCfId = 1
    self._productData = param
    self:initUI(param)
    self:initEvents()
    self:show(param, productType)
end

function ShopChooseView:initUI(productData)
    self._checkBoxItem1:setTag(1)
    self._checkBoxItem2:setTag(2)
end

function ShopChooseView:initEvents()
    self._checkBoxItem1:addEventListener(handler(self, self.onTouchEventChoosePayType))
    self._checkBoxItem2:addEventListener(handler(self, self.onTouchEventChoosePayType))
end

function ShopChooseView:show(productData, productType)
    self._propTpye = productType
    local imageName = XH.StringTool.getImageNameByUrl(productData.image)
    local imagePath = DEFAULT_DOWNLOAD_IMAGE_DIR .. imageName
    self._propValue = productData.value
    self._shopPropType = productData.prop_type
    self._goodsImg:ignoreContentAdaptWithSize(true)
    self._goodsImg:loadTexture(imagePath, ccui.TextureResType.localType)
    self._goodsName:setText(productData.name)
    self._goodsDetail:setText(productData.des)
    for i = 1, #productData.multi_score_config do
        self._richType[i] = productData.multi_score_config[i].type
        local text = productData.multi_score_config[i].name
        if string.find(text, "房卡") then
            text = "房卡"
        end
        self["_buyTypeText" .. i]:setText(text .. "兑换")
    end
    if not productData.multi_goods_info or not next(productData.multi_goods_info) then
        productData.multi_goods_info = self:getGoodDetailInfoByGoods(clone(productData))
    end
    self:initPropList(productData.multi_goods_info, productData.id)
    self:showAni(productType)
end

function ShopChooseView:showAni(productType)
    local aniNode = self._aniPos
    local aniBG = self._aniBg
    if aniNode and tolua.isnull(aniNode) == false and aniBG and tolua.isnull(aniBG) == false then
        aniNode:removeAllChildren()
        aniBG:setVisible(false)
        if productType ~= ShopConfig.ProductType.YPQ then
            return
        end
        local params1 = {
            path = "animation/Lobby/Base/zjb_ypq_sczs_ani/",
            ske = "zjb_ypq_sczs_ani_ske.json",
            tex = "zjb_ypq_sczs_ani_tex.json",
            armatureName = "Armature",
            dragonBonesName = "zjb_ypq_sczs_ani",
            animationName = "zjb_ypq_sc"
        }
        local propAni = display.playDargonBonesAnimByTimes(params1, 0)
        if propAni then
            aniBG:setVisible(true)
            aniNode:addChild(propAni)
        end
    end
end

-- 关闭按钮
function ShopChooseView:onTouchEventClose(send, event)
    self:close()
end

-- 选时长按钮
function ShopChooseView:onTouchEventChooseTime(send, event)
    if event ~= ccui.TouchEventType.ended then
        return
    end
    self:doChooseTime(send)
end

-- 选时长
function ShopChooseView:doChooseTime(send)
    self._propID = send:getTag()
    self._multiCfId = send.multiCfgId
    local allItem = self._displayList:getChildren()
    for i = 1, #allItem do
        XH.UITool.setVisible(allItem[i], KW_IMG_GOODS_SELECT, send == allItem[i])
    end
end
-- 选支付按钮
function ShopChooseView:onTouchEventChoosePayType(send, event)
    if event == ccui.CheckBoxEventType.selected then
        self._payTpye = send:getTag()
        if send == self._checkBoxItem1 then
            self._checkBoxItem2:setSelected(false)
        else
            self._checkBoxItem1:setSelected(false)
        end
        self:updateSelectionData()
    elseif event == ccui.CheckBoxEventType.unselected then
        send:setSelected(true)
    end
end

-- 设置选时长界面
function ShopChooseView:updateSelectionData()
    local allItem = self._displayList:getChildren()
    for i = 1, #allItem do
        local goodInfo = self:getGoodsInfoByRichType(allItem[i].goods, self._richType[self._payTpye])
        if goodInfo then
            self:setItemPrice(allItem[i], goodInfo)
            self:checkIsActTime(allItem[i], goodInfo, allItem[i].goods_act)
        end
    end
end
-- 设置选时长界面
function ShopChooseView:getGoodsInfoByRichType(goodsInfos, richType)
    for i = 1, #goodsInfos do
        if richType == goodsInfos[i].type then
            return goodsInfos[i]
        end
    end
    return nil
end
-- 购买按钮处理
function ShopChooseView:onBuyBtnTouchEvent(send, event)
    local allItem = self._displayList:getChildren()
    local itemInfo
    for _, v in pairs(allItem) do
        if v.multiCfgId == self._multiCfId then
            itemInfo = v
        end
    end
    if itemInfo then
        local currency = itemInfo.goods[self._payTpye]
        local propId = currency.type == "gold" and -1 or tonumber(XH.StringTool.getNumberSuffixByString(currency.type))
        if XH.lobby:getModule("Shop"):isHaveEnoughCurrency(propId, tonumber(currency.value)) then
            XH.lobby:getModule("Shop"):reqExchangeProduct(self._propTpye, self._propID, self._richType[self._payTpye], self._propValue, self._multiCfId, nil, nil, self._shopPropType)
        elseif XH.lobby:getModule("Shop"):checkSelfSupExchange(propId, tonumber(currency.value)) then
            local needPropName = propId == -1 and "gold" or XH.lobby:getModule("Shop"):getShopNameById(propId)
            if ShopConfig.NotEnoughPropsTips and ShopConfig.NotEnoughPropsTips[needPropName] then
                local tipMsg = ShopConfig.NotEnoughPropsTips[needPropName]
                XH.TipTool.showTip( {
                    type = XH.TIP_LAYER_TYPE.OK_CANCEL_NO_CLOSE,
                    funcOK = function()
                        if tipMsg == "ERROR_SHOP_ROOM_CARD_NOT_ENOUGH" then
                            XH.lobby:getModule("Shop"):dispatchEvent({name = XH.lobby:getModule("Shop").EVENT_SHOW_CARD_VIEW})
                        else
                            XH.lobby:getModule("Shop"):dispatchEvent({name = XH.lobby:getModule("Shop").EVENT_SHOW_GOLD_VIEW})
                        end
                    end
                } , tipMsg)
            end
        else
            local bShelvesSuppport, shelvesInfo = XH.lobby:getModule("Shop"):checkMaxShelvesEnough(propId, tonumber(currency.value))
            if (propId ~= XH.areaData:getPropList().propDiamndID and bShelvesSuppport and shelvesInfo) or 
                (propId == XH.areaData:getPropList().propDiamndID and bShelvesSuppport) then
                XH.lobby:getModule("Shop"):arrangeQuickInfo(propId, tonumber(currency.value), self._productData.name, tonumber(self._productData.multi_goods_info[self._multiCfId].prop_count), tonumber(self._productData.value), shelvesInfo, self._productData.id, self._productData.goods_act)
            else
                XH.lobby:getModule("Shop"):reqExchangeProduct(self._propTpye, self._propID, self._richType[self._payTpye], self._propValue, self._multiCfId, nil, nil, self._shopPropType)
            end
        end
    else
        XH.lobby:getModule("Shop"):reqExchangeProduct(self._propTpye, self._propID, self._richType[self._payTpye], self._propValue, self._multiCfId, nil, nil, self._shopPropType)
    end
    self:close()
end

-- 添加一个item到界面上
function ShopChooseView:createNewProduct(productData, gid, multiCfgId)
    local item = self._goodsView:clone()
    if item then
        -- 挂载到ScrollView中去
        item:retain()
        item:setTag(gid)
        item:removeFromParent()
        self._displayList:addChild(item)
        self:setItemDate(item, productData.prop_count)
        self:setTimeDate(item, productData.name)
        XH.UITool.setVisible(item, KW_IMG_GOODS_FIRST_LOGO, false)
        item:addTouchEventListener(handler(self, self.onTouchEventChooseTime))
        item.goods = clone(productData.multi_goods)
        item.goods_act = clone(productData.goods_act)
        item.multiCfgId = multiCfgId
        item:release()
    end
    return item
end

function ShopChooseView:setTimeDate(item, name)
    XH.UITool.setText(item, KW_TEXT_TIMEDATE, "天")
    if string.find(name, "小时") then
        XH.UITool.setText(item, KW_TEXT_TIMEDATE, "小时")
    elseif string.find(name, "洗牌券") or string.find(name, "张") then
        XH.UITool.setText(item, KW_TEXT_TIMEDATE, "张")
    end
end

function ShopChooseView:initPropList(data, gid)
    self._displayList:removeAllChildren()
    self._displayList:setInnerContainerSize(cc.size(KW_PROP_LIST_ITEM_WIDTH * #data, self._displayList:getContentSize().height))
    self._displayList:setTouchEnabled(KW_PROP_LIST_ITEM_WIDTH * #data > self._displayList:getContentSize().width)
    local itemsMargin = KW_PROP_LIST_ITEM_MAX_MARGIN
    if #data > KW_PROP_LIST_ITEM_ADAPT_COUNT then
        itemsMargin = KW_PROP_LIST_ITEM_MIN_MARGIN
    end
    self._displayList:setItemsMargin(itemsMargin)
    self._displayList:setScrollBarEnabled(false)
    table.sort(data, function(v1, v2)
        return v1.prop_count < v2.prop_count
    end )
    for i = 1, #data do
        local item = self:createNewProduct(data[i], gid, data[i].multi_cfg_id)
        if i == #data then
            self:doChooseTime(item)
        end
    end
    self:updateSelectionData()
end

function ShopChooseView:setItemPrice(item, price)
    local num = tonumber(price.value)
    if num >= 10000 then
        num = num / 10000 .. "万"
    end
    XH.UITool.setText(item, KW_TEXT_PRICE, num .. price.name)
end

function ShopChooseView:setItemDate(item, date)
    XH.UITool.setText(item, KW_TEXT_DAY_CNT, date)
end

function ShopChooseView:getGoodDetailInfoByGoods(item)
    local goods = { }
    goods.prop_count = item.prop_count
    goods.multi_goods = item.multi_score_config
    goods.name = item.name
    return { goods }
end

function ShopChooseView:setOldItemPrice(item, price)
    XH.UITool.setText(item, KW_TEXT_OLD_PRICE, self:moreThanPrize(price.value) .. price.name)
end

function ShopChooseView:setCutPrize(item, price, goodInfo)
    XH.UITool.setText(item, KW_TEXT_PRICE, self:moreThanPrize(price) .. goodInfo.name)
end

function ShopChooseView:moreThanPrize(price)
    local num = tonumber(price)
    if num >= 10000 then
        num = num / 10000 .. "万"
    end
    return num
end

-- 检测是否在活动时间
function ShopChooseView:checkIsActTime(item, goodInfo, goodsAct)
    XH.UITool.setVisible(item, KW_TEXT_OLD_PRICE, false)
    local mutliconfig = { }
    mutliconfig.multi_cfg_id = item.multiCfgId
    if goodsAct and goodsAct ~= "" and XH.lobby:getModule("Shop"):judgeIsActOut(goodsAct, self._productData, mutliconfig) then
        if goodsAct.act_type == ShopConfig.ProductAct.FIRST then
            XH.UITool.setText(item, KW_TEXT_OLD_PRICE, goodsAct.act_config.description)
            XH.UITool.setVisible(item, KW_TEXT_OLD_PRICE, true)
            XH.UITool.setVisible(item, KW_IMG_CUT_LINE, false)
            return
        end
        if goodsAct.act_type == ShopConfig.ProductAct.SEND then
            local goodsActInfo = goodsAct.act_config[goodsAct.act_type]
            XH.UITool.setText(item, KW_TEXT_OLD_PRICE, "额外赠送" .. self:moreThanPrize(goodsActInfo.value) .. goodsActInfo.name)
            XH.UITool.setVisible(item, KW_TEXT_OLD_PRICE, true)
            XH.UITool.setVisible(item, KW_IMG_CUT_LINE, false)
            return
        end
        for j = 1, #goodsAct.act_config[goodsAct.act_type] do
            if goodsAct.act_config[goodsAct.act_type][j].type == goodInfo.type and goodsAct.act_config[goodsAct.act_type][j].checked == "1" then
                self:setOldItemPrice(item, goodInfo)
                self:setCutPrize(item, goodsAct.act_config[goodsAct.act_type][j].value, goodInfo)
                if goodsAct.act_type == ShopConfig.ProductAct.DISCOUNT then
                    self:setCutPrize(item, tonumber(goodsAct.act_config[goodsAct.act_type][j].value) * tonumber(goodInfo.value) / 100, goodInfo)
                end
                XH.UITool.setVisible(item, KW_TEXT_OLD_PRICE, true)
                XH.UITool.setVisible(item, KW_IMG_CUT_LINE, true)
                return
            end
        end
    end
end

return ShopChooseView   �8  