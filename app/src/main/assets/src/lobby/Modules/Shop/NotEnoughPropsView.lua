local NotEnoughPropsView = class("NotEnoughPropsView", XH.ViewBase)
local ShopConfig = require("lobby.Modules.Shop.Config")

function NotEnoughPropsView:getCSBPath()
    return "cocosStudio/hall/CSB/MessageBoxEx/ChargeMsgBox.csb"
end

function NotEnoughPropsView:getBindingInfo()
    return {
        ["_KWA_BTND_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose" },
        ["_KWA_BTND_OK"] = { varName = "_btnConfirm", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventConfirm" },
        ["_KW_DIAMOND_TEXT"] = { varName = "_diamondNum"},
        ["_KW_PROP_TEXT"] = { varName = "_propText"},
        ["_KW_LEFT_DIAMOND_TEXT"] = { varName = "_tips"},
        ["_KW_CUR_PROP_TEXT"] = { varName = "_curDiamondNum"},
        ["_KW_LEFT_DESC_TEXT"] = { varName = "_leftTipText"},
        ["_KW_TIP_Text_1"] = { varName = "_chargeTip1"},
        ["_KW_TIP_Text_2"] = { varName = "_chargeTip2"},
        ["_KW_TIP_Text_3"] = { varName = "_chargeTip3"},
        ["_KW_TIP_Text_4"] = { varName = "_chargeTip4"},
        ["_KW_TIP_Text_6"] = { varName = "_chargeTip6"},
        ["_KW_TIP_Text_7"] = { varName = "_chargeTip7"},
        ["_KW_YUAN_TEXT"] = { varName = "_yuanText"},
        ["_KW_TIP_Text_Prop_Name"] = { varName = "_propName"},
        ["_KW_TIP_ORIGINAL"] = { varName = "_tipOriginal"},
    }
end

function NotEnoughPropsView:getProxyEvents()
    return {
        -- { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_EXCHANGE_DIAMOND_GOLD", callBack = "onEventExchangeGold" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVNET_SHOPMANAGER_SHOW_SHOP", callBack = "onEventProductList" },
    }
end

function NotEnoughPropsView:ctor(param)
    self._canTouchBackGround = false
    param = param or { }
    NotEnoughPropsView.super.ctor(self, param)
    self:initData(param)
end

function NotEnoughPropsView:initData(param)
    self._diamondProducts = nil
    self._targetDiamond = param.targetDiamond
    self._targetValue = param.targetValue
    self._name = param.name
    self._buyProduct = nil
    self._giftType = param.giftType
    self._product = param.product
    self._needCost = param.needCost
    self._goodAct = param.goodAct
    self._targetPropId = param.targetPropId
    local _, subId = XH.lobby:getModule("Shop"):getTagsWithName(ShopConfig.SpecialTag.DIAMOND)
    if not XH.lobby:getModule("Shop"):getShopItemsWithId(subId) then
        XH.lobby:getModule("Shop"):reqShopProductByConfig(subId)
    else
        self:onEventProductList()
    end
end

-- 初始化界面UI
function NotEnoughPropsView:updateLayerUI()
    local subStr = self:getRechargeLeftStr()
    if self._buyProduct then
        if self._product then
            if self._product.type == "gold" then
                self._propName:setText("金币:")
                self._curDiamondNum:setText(XH.playerData:getSR())
            else
                self._propName:setText("房卡:")
                self._curDiamondNum:setText(XH.playerData:getRoomCard())
            end
        else
            self._curDiamondNum:setText(XH.playerData:getDiamnd())
        end
        self._diamondNum:setText(self._buyProduct.value)
        self._yuanText:setText(tonumber(self._buyProduct.price))
        self._propText:setText(self._name)
        if subStr then
            local tipsStr
            if type(subStr) == "string" then
                tipsStr = subStr
            elseif subStr > 0 then
                tipsStr = XH.ConstString.getStr("QUICK_RECHARGE_NUM", subStr)
            end
            if tipsStr then
                self._tips:setText(tipsStr)
                self._tips:setVisible(true)
                self._leftTipText:setVisible(true)
                self._chargeTip6:setVisible(true)
            end
        else
            self._tips:setVisible(false)
        end
        if self._goodAct then
            self._tipOriginal:setVisible(true)
        end
    else
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK_CANCEL,
            funcOK = function()
                XH.viewManager:openView("ShopView", nil, {productType = ShopConfig.SpecialTag.GOLD})
                self:close()
            end,
            funcCancel = function()
                self:close()
            end
        }, "ERROR_SHOP_DIAMOND_NOT_ENOUGH")
    end
    self:dolayout()
end

function NotEnoughPropsView:dolayout()
    local KW_HORIZONTAL_DIS = 5
    local size1 = self._propName:getContentSize()
    local pos1 = self._chargeTip1:getContentSize().width + self._chargeTip1:getPositionX() + size1.width + KW_HORIZONTAL_DIS
    self._curDiamondNum:setPositionX(pos1)
    local pos2 = self._curDiamondNum:getPositionX()  + self._curDiamondNum:getContentSize().width + KW_HORIZONTAL_DIS
    self._chargeTip7:setPositionX(pos2)
    local pos3 = self._chargeTip7:getPositionX()  + self._chargeTip7:getContentSize().width + KW_HORIZONTAL_DIS
    self._yuanText:setPositionX(pos3)
    local pos9 = self._yuanText:getPositionX()  + self._yuanText:getContentSize().width + KW_HORIZONTAL_DIS
    self._chargeTip2:setPositionX(pos9)
    local pos10 = self._chargeTip2:getPositionX()  + self._chargeTip2:getContentSize().width + KW_HORIZONTAL_DIS
    self._diamondNum:setPositionX(pos10)
    local pos5 = self._chargeTip4:getPositionX()  + self._chargeTip4:getContentSize().width + KW_HORIZONTAL_DIS
    self._propText:setPositionX(pos5)
    local pos7 = self._leftTipText:getPositionX()  + self._leftTipText:getContentSize().width + KW_HORIZONTAL_DIS
    self._tips:setPositionX(pos7)
    local pos8 = self._tips:getPositionX()  + self._tips:getContentSize().width + KW_HORIZONTAL_DIS
    self._chargeTip6:setPositionX(pos8)
    self._diamondNum:setVisible(true)
    self._propText:setVisible(true)
    self._curDiamondNum:setVisible(true)
    self._yuanText:setVisible(true)
end

function NotEnoughPropsView:getRechargeLeftStr()
    local subStr
    table.sort(self._diamondProducts, function (a, b)
        return tonumber(a.value) < tonumber(b.value)
    end)
    
    local myDiamond = XH.playerData:getDiamnd()
    for _, v in pairs(self._diamondProducts) do
        if tonumber(v.value) + myDiamond >= self._targetDiamond then
            self._buyProduct = v
            if self._product then
                local myProp = self._product.type == "gold" and XH.playerData:getSR() or XH.playerData:getRoomCard()
                if tonumber(v.value) + myDiamond - self._targetDiamond > 0 then
                    subStr = (tonumber(v.value) + myDiamond - self._targetDiamond) .. "钻石"
                end
                if tonumber(self._targetValue) + myProp - self._needCost > 0 then
                    local leftNum = tonumber(self._targetValue) + myProp - self._needCost
                    if self._product.type == "gold" then
                        leftNum = leftNum / 10000
                    else
                        leftNum = leftNum
                    end
                    local propNumStr = XH.StringTool.getNumberPrefixByString(self._product.name)
                    local propTypeStr = string.sub(self._product.name, string.len(propNumStr) + 1)
                    if subStr then
                        subStr = subStr .. "," .. leftNum .. propTypeStr
                    else
                        subStr = leftNum .. propTypeStr
                    end
                end
            else
                subStr = tonumber(v.value) - self._targetDiamond
            end
            break
        end
    end
    return subStr
end

function NotEnoughPropsView:onEventProductList(data)
    if not self._diamondProducts then
        local _, subId = XH.lobby:getModule("Shop"):getTagsWithName(ShopConfig.SpecialTag.DIAMOND)
        self._diamondProducts = XH.lobby:getModule("Shop"):getShopItemsWithId(subId) or {}
        if next(self._diamondProducts) then
            local function cmpFunc(a, b)
                return a.value < b.value
            end
            table.sort(self._diamondProducts, cmpFunc)
            self:updateLayerUI()
        end
    end
end

function NotEnoughPropsView:onTouchEventClose(send, eventType)
    self:close()
end

function NotEnoughPropsView:onTouchEventConfirm(send, eventType)
    if self._buyProduct then
        XH.lobby:getModule("Shop"):reqBuyDiamond(self._buyProduct, "deliver_award", self._giftType, nil, true, nil, self._targetPropId)
        self:close()
    end
end

return NotEnoughPropsView�!  