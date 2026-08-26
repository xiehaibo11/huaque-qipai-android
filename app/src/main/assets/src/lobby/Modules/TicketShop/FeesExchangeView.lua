local TicketShopFeesExchangeView = class("TicketShopFeesExchangeView", XH.ViewBase)

function TicketShopFeesExchangeView:getCSBPath()
    return "cocosStudio/hall/CSB/window/TicketFeesExchangeLayer.csb"
end

function TicketShopFeesExchangeView:getBindingInfo()
    return {
        ["_KW_PANEL_BG"] = {varName = "_btnBackGround", onTouchEnded = "onTouchEventClose"},
        ["_KWA_BTND_CLOSE"] = {varName = "_btnBackGround", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose"},
        ["_KW_BTN_SURE"] = {varName = "_btnSure", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventSure"},
        ["_KW_BTN_SEND_CODE"] = {varName = "_btnSendCode", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventSendCode"},
        ["_KW_BTN_CHANGE_PHONE"] = {varName = "_btnChangePhone", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventChangePhone"},
        ["_KW_IMG_TITLE"] = {varName = "_imgTitle"},
        ["_KW_TEXT_TIME"] = {varName = "_codeTimeLimt"},
        ["_KW_TEXT_LAST_PHONE_NUM"] = {varName = "_lastPhoneNum"},
        
        ["_KW_INPUT_PHONE_BG"] = {varName = "_inputPhoneBg"},
        ["_KW_INPUT_CODE_BG"] = {varName = "_inputCodeBg"},
        ["_KW_INPUT_CODE_CHECK"] = {varName = "_inputCodeCheck"},
        ["_KW_INPUT_PHONE"] = {varName = "_inputPhone"},
        --20210118 新增下拉选择兑换商品 
        ["_KW_BTN_SHOW_PRODUCTLIST"] = {varName = "_btnShowProductList", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventShowProductList"},
        ["_KW_BTN_HIDE_PRODUCTLIST"] = {varName = "_btnHideProductList", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onBtnEventHideProductList"},
        ["_KW_IMG_PRODUCTLIST"] = {varName = "_imgProductList"},
        ["_KW_LISTVIEW_PRODUCTLIST"] = {varName = "_productItemList"},
        ["_KW_PRODUCT_ITEM"] = {varName = "_tableViewItemModel"},
        ["_KW_TEXT_SELECT_PRODUCT"] = {varName = "_selectedProduct"},

        -- 2021/12/22修改系统字体为BMFONT
        ["_KW_BMFONT_MYCOUNT"] = {varName = "_bmfontMyCount"}, 
        ["_KW_BMFONT_HUAFEI_COUNT"] = {varName = "_bmfontHuaFeiCount"},
        ["_KW_BMFONT_MYCOUNT_FUKA"] = {varName = "_bmfontFuKaCount"},
        ["_KW_BMFONT_PHONE_NUM"] = {varName = "_bmfontPhoneName"},
        ["_KW_BMFONT_YANZHENG_CODE"] = {varName = "_codeCheckBMFont"},
        ["_KW_BMFONT_EXCHANGE"] = {varName = "_bmfontExchangeCount"},
        ["_KW_BMFONT_NEED_CARD_COUNT_TITLE"] = {varName = "_bmfontNeedCardCountTitle"},
        ["_KW_BMFONT_NEED_CARD_COUNT"] = {varName = "_bmfontNeedCardCount"},
        ["_KW_BMFONT_TIP_WUGUAN"] = {varName = "_iosTip"},
    }
end

function TicketShopFeesExchangeView:getProxyEvents()
    return {
        {module = XH.lobby:getModule("TicketShop"), eventKeyName = "EVNET_PROPS_JINGDONGKA", callBack = "onGetJingDongKaList"}
    }
end

local newyear = 1609430400

--兑换物品列表
local EXCHANGE_PRODUCT_LIST = {
    JINGDONGKA = "JINGDONGKA",
}

local EXCHANGE_PRODUCT_TYPE = {
    ["JINGDONGKA"] = 100,
}

local EXCHANGE_PRODUCT_NAME = {
    ["JINGDONGKA"] = "奖励",
}

local SUB_ITEM_CTRL_TYPE = {
    ADD = 1,
    DELETE = 2
}

function TicketShopFeesExchangeView:ctor(param)
    self._canTouchBackGround = true
    param = param or {}
    TicketShopFeesExchangeView.super.ctor(self, param)
    self._feevisible = param.sysTime > newyear
    self._selectNum = 0
    self._getLastPhone = tonumber(param.getLastPhone) or nil
    -- 需要先获取最后一次的手机号码
    self._feesProductId = param.productid or 0
    self._boxItems = {}
    self._exchangeProduct = nil
    XH.lobby:getModule("TicketShop"):reqHuaFeiExchangeProductsInfo()
end

-- 初始化界面UI
function TicketShopFeesExchangeView:initUI()
    self:updateNowCardNum()
    self:updateSelectedInfo()
    self._imgTitle:ignoreContentAdaptWithSize(true)
    self:initTableView()
    self:initIosTip()
end

function TicketShopFeesExchangeView:initIosTip()
    local targetPlatform = cc.Application:getInstance():getTargetPlatform()
    self._iosTip:setVisible(cc.PLATFORM_OS_IPAD == targetPlatform or cc.PLATFORM_OS_IPHONE == targetPlatform)
end

function TicketShopFeesExchangeView:updateNowCardNum()
    self._bmfontFuKaCount:setText((XH.playerData:getHuaFei() or 0) .. "张")
end

function TicketShopFeesExchangeView:checkSelectedItemIsNeedPhone()
    if not self._exchangeProduct then
        return false
    end
    if XH.lobby:getModule("TicketShop"):checkIsJingDongKa(self._exchangeProduct.subItemType) then
        return true
    end
    return false
end

function TicketShopFeesExchangeView:updateSelectedInfo()
    local isNeedPhoneItem = self:checkSelectedItemIsNeedPhone()
    local isShowLastPhone = isNeedPhoneItem and self._getLastPhone ~= nil
    self._bmfontPhoneName:setVisible(isNeedPhoneItem and not isShowLastPhone) 
    self._inputPhoneBg:setVisible(isNeedPhoneItem and not isShowLastPhone)
    self._codeCheckBMFont:setVisible(isNeedPhoneItem and not isShowLastPhone) 
    self._inputCodeBg:setVisible(isNeedPhoneItem and not isShowLastPhone)
    self._btnSendCode:setVisible(isNeedPhoneItem and not isShowLastPhone)
    self._btnChangePhone:setVisible(isNeedPhoneItem and isShowLastPhone)
    self._lastPhoneNum:setVisible(isNeedPhoneItem and isShowLastPhone)

    if self._getLastPhone ~= nil then
        self._lastPhoneNum:setText(self._getLastPhone)
    end
    self._selectedProduct:setString(self._exchangeProduct and self._exchangeProduct.name or "请选择你要兑换的奖励")
    self._bmfontNeedCardCount:setString(self._exchangeProduct and (self._exchangeProduct.price .. "张"))
    self._bmfontNeedCardCount:setVisible(self._exchangeProduct ~= nil)
    self._bmfontNeedCardCountTitle:setVisible(self._exchangeProduct ~= nil)
end

-- 点击关闭按钮关闭事件
function TicketShopFeesExchangeView:onTouchEventClose(send, eventType)
    self:close()
end

-- 点击获取验证码
function TicketShopFeesExchangeView:onTouchEventSendCode(send, eventType)
    local strPhone = self._inputPhone:getText()

    if strPhone and strPhone ~= "" then
        strPhone = XH.StringTool.trim(strPhone)
    end
    if not self:checkPhoneLegality(strPhone) then
        XH.TipTool.showToast(XH.ConstString.getStr("ERROR_PHONE"), 3)
        return
    end
    self:onCodeRequest(strPhone)
    self:showCodeText()
end

-- 显示验证码获取倒计时
function TicketShopFeesExchangeView:showCodeText()
    local sendbtn = self._btnSendCode
    local timeTxtNode = self._codeTimeLimt
    if sendbtn and timeTxtNode then
        sendbtn:setBright(false)
        sendbtn:setEnabled(false)
        timeTxtNode:setString(tonumber(string.format("%02d", 60)))
        timeTxtNode:setVisible(true)
        local nTime = 60
        timeTxtNode:stopAllActions()
        timeTxtNode:runAction(
            cc.RepeatForever:create(
                cc.Sequence:create(
                    cc.DelayTime:create(1),
                    cc.CallFunc:create(
                        function()
                            nTime = nTime - 1
                            if nTime > 0 then
                                local strTime = tonumber(string.format("%02d", nTime))
                                timeTxtNode:setString(strTime)
                            else
                                timeTxtNode:stopAllActions()
                                timeTxtNode:setVisible(false)
                                sendbtn:setBright(true)
                                sendbtn:setEnabled(true)
                            end
                        end
                    )
                )
            )
        )
    end
end

-- 校验手机号
function TicketShopFeesExchangeView:checkPhoneLegality(strPhone)
    if strPhone == nil then
        return
    end
    if not string.match(strPhone, "^%d+$") or string.len(strPhone) < 11 then
        return false
    end
    return true
end

-- 校验验证码
function TicketShopFeesExchangeView:checkCodeLegality(strCode)
    if strCode == nil then
        return
    end
    if not string.match(strCode, "^%d+$") or string.len(strCode) ~= 6 then
        return false
    end
    return true
end

-- 待修改
function TicketShopFeesExchangeView:onTouchEventSure(send, eventType)
    if not self._exchangeProduct then
        XH.TipTool.showToast("请选择需要兑换的商品", 3)
        return
    end

    local nowCount = XH.playerData:getHuaFei() or 0
    if self._exchangeProduct.price > nowCount then
        XH.TipTool.showToast("您的可兑换余额不足！", 3)
        return
    end

    local isNeedPhone = self:checkSelectedItemIsNeedPhone()
    local phoneNumber = self._inputPhone:getText()
    local codeCheckNum = self._inputCodeCheck:getText()
    if isNeedPhone then
        if self._getLastPhone ~= nil then
            phoneNumber = self._getLastPhone
        else
            if not self:checkPhoneLegality(phoneNumber) then
                XH.TipTool.showToast(XH.ConstString.getStr("ERROR_PHONE"), 3)
                return
            end
            if not self:checkCodeLegality(codeCheckNum) then
                XH.TipTool.showToast(XH.ConstString.getStr("ERROR_PHONE_CODE"), 3)
                return
            end
        end
    end
    if self._exchangeProduct.type == EXCHANGE_PRODUCT_TYPE["JINGDONGKA"] and self._exchangeProduct.id then
        local product = self:getHuaFeiProductById(self._jingDongKaList, self._exchangeProduct.id)
        local paramExTable = nil
        if isNeedPhone then
            paramExTable = {}
            paramExTable.tel_fee_to_gift = 1
            paramExTable.check_phone = phoneNumber
            paramExTable.check_code = codeCheckNum
        end
        local richType = product.price_config[1].type
        XH.lobby:getModule("TicketShop"):reqHuaFeiExchangeProduct(product.id, phoneNumber, richType, paramExTable, self._exchangeProduct.subItemType)
    end
    self._exchangeProduct = nil
    self:close()
end

function TicketShopFeesExchangeView:getHuaFeiProductById(productList, id)
    for i = 1, #productList do
        if productList[i].id == id then
            return productList[i]
        end
    end
    return nil
end

-- 获取到验证码
function TicketShopFeesExchangeView:onCodeRequest(strPhone)
    if strPhone == nil then
        return
    end

    -- 请求兑换话费
    XH.lobby:getModule("TicketShop"):reqFeesPhoneCheck(strPhone)
end

-- 修改需要充值的手机号码
function TicketShopFeesExchangeView:onTouchEventChangePhone(send, eventType)
    self._getLastPhone = nil
    self:updateSelectedInfo()
end

function TicketShopFeesExchangeView:onBtnEventShowProductList(send, eventType)
    self._imgProductList:setVisible(true)
    self._btnShowProductList:setVisible(false)
    self._btnHideProductList:setVisible(true)
end

function TicketShopFeesExchangeView:onBtnEventHideProductList(send, eventType)
    self._imgProductList:setVisible(false)
    self._btnShowProductList:setVisible(true)
    self._btnHideProductList:setVisible(false)
end

function TicketShopFeesExchangeView:addBoxItem(item)
    table.insert(self._boxItems, item)
end

function TicketShopFeesExchangeView:addNotSubBoxItem(name, type, price, id, subItemType)
    local item = self:createBoxItem(name, type, price, id, subItemType)
    self:addBoxItem(item)
    return item
end

function TicketShopFeesExchangeView:createBoxItem(name, type, price, id, subItemType)
    local item = {}
    item.name = name
    item.type = type
    item.price = price
    item.id = id
    item.isSubItem = false
    item.subItemType = subItemType or ""
    item.sort = 0
    return item
end

function TicketShopFeesExchangeView:createSubBoxItem(name, type, price, id, subItemType, sort)
    local item = self:createBoxItem(name, type, price, id, subItemType)
    item.isSubItem = true
    if sort then
        item.sort = sort
    end
    return item
end

function TicketShopFeesExchangeView:addBoxSubItem(name, type, price, id, subItemType)
    local item = self:createSubBoxItem(name, type, price, id, subItemType)
    self:addBoxItem(item)
    return item
end

function TicketShopFeesExchangeView:removeItemsBySubtype(subtype)
    for i = #self._boxItems, 1, -1 do
        if self._boxItems[i].isSubItem and self._boxItems[i].type == subtype then
            table.remove(self._boxItems, i)
        end
    end
end

function TicketShopFeesExchangeView:initTableView()
    for _, v in pairs(EXCHANGE_PRODUCT_LIST) do
        local isHaveTag = true
        if "JINGDONGKA" == v then
            if self._jingDongKaList then
                isHaveTag = true
                local kaList = self:changeWebJingDongKaToClient(self._jingDongKaList)
                for _, value in pairs(kaList) do
                    self:addBoxItem(value)
                end
            else
                isHaveTag = false
            end
        end
        if isHaveTag then
            local tempItem = self:addNotSubBoxItem(EXCHANGE_PRODUCT_NAME[v], EXCHANGE_PRODUCT_TYPE[v], -1, -1)
            tempItem.isOpenSub = true
        end
    end
    self:sortTableViewData()
    self._tableViewItemModelSize = self._tableViewItemModel:getContentSize()
    local newSize = self._productItemList:getContentSize()
    local offset_y = 15
    if not self._viewTableView then
        self._viewTableView = cc.TableView:create(newSize)
        self._viewTableView:setAnchorPoint(cc.p(0, 0))
        self._viewTableView:setPosition(cc.p(0, offset_y))
        self._viewTableView:setVerticalFillOrder(cc.TABLEVIEW_FILL_TOPDOWN)
        self._viewTableView:setDirection(cc.SCROLLVIEW_DIRECTION_VERTICAL)
        self._viewTableView:setDelegate()
        self._viewTableView:registerScriptHandler(handler(self, self.onTableCellTouched), cc.TABLECELL_TOUCHED)
        self._viewTableView:registerScriptHandler(handler(self, self.onTableViewCellSizeForTable), cc.TABLECELL_SIZE_FOR_INDEX)
        self._viewTableView:registerScriptHandler(handler(self, self.onTableViewCellAtIndex), cc.TABLECELL_SIZE_AT_INDEX)
        self._viewTableView:registerScriptHandler(handler(self, self.numberOfCellsInTableView), cc.NUMBER_OF_CELLS_IN_TABLEVIEW)
        self._viewTableView:registerScriptHandler(handler(self, self.scrollViewDidScroll), cc.SCROLLVIEW_SCRIPT_SCROLL)
        self._imgProductList:setContentSize(cc.size(newSize.width, newSize.height + offset_y))
        self._imgProductList:addChild(self._viewTableView)
        self._productItemList:removeFromParent()
    end
    if self._viewTableView then
        self._viewTableView:reloadData()
    end
end

function TicketShopFeesExchangeView:onTableCellTouched(tableView, cell)
    local idx = cell:getIdx() + 1
    local boxItem = self._boxItems[idx]
    local ctrlType
    if boxItem.isOpenSub then
        boxItem.isOpenSub = false
        ctrlType = SUB_ITEM_CTRL_TYPE.DELETE
    else
        boxItem.isOpenSub = true
        ctrlType = SUB_ITEM_CTRL_TYPE.ADD
    end

    local item = cell:getChildByName("__item")
    local tag = item:getTag()
    if boxItem.isSubItem then
        self:selectItem(idx)
    elseif tag == EXCHANGE_PRODUCT_TYPE["JINGDONGKA"] then
        if ctrlType then
            if self._jingDongKaList then
                local kaList = self:changeWebJingDongKaToClient(self._jingDongKaList)
                self:updateSubItem(kaList, ctrlType, EXCHANGE_PRODUCT_TYPE["JINGDONGKA"])
            end
        end
    end
end

function TicketShopFeesExchangeView:selectItem(index)
    -- local nowCount = XH.playerData:getHuaFei() or 0
    -- if self._boxItems[index].price <= nowCount then
        self._exchangeProduct = self._boxItems[index]
        self:updateSelectedInfo()
    -- else
    --     XH.TipTool.showToast("您的可兑换余额不足！", 3)
    -- end
    --关闭下拉框
    self._imgProductList:setVisible(false)
    self._btnShowProductList:setVisible(true)
    self._btnHideProductList:setVisible(false)
end

function TicketShopFeesExchangeView:changeWebJingDongKaToClient(jingDongKaList)
    local kaList = {}
    for i = 1, #jingDongKaList do
        local price = tonumber(jingDongKaList[i].price_config[1].value)
        if price > 0 then
            local tempKaItemData = jingDongKaList[i]
            local kaItem = self:createSubBoxItem(tempKaItemData.name, EXCHANGE_PRODUCT_TYPE["JINGDONGKA"], price, tonumber(tempKaItemData.id), tempKaItemData.type, tempKaItemData.sort)
            table.insert(kaList, kaItem)
        end
    end
    return kaList
end

function TicketShopFeesExchangeView:sortTableViewData()
    table.sort(self._boxItems,function(v1, v2)
            if v1.type < v2.type then
                return true
            elseif v1.type == v2.type then
                if not v1.isSubItem and v2.isSubItem then
                    return true
                end
                if v1.isSubItem and not v2.isSubItem then
                    return false
                end
                if v1.sort > v2.sort then
                    return true
                end
            end
            return false
        end
    )
end

function TicketShopFeesExchangeView:updateSubItem(subtable, ctrltype, subtype)
    if ctrltype == SUB_ITEM_CTRL_TYPE.ADD then
        for _, v in pairs(subtable) do
            self:addBoxItem(v)
        end
    elseif ctrltype == SUB_ITEM_CTRL_TYPE.DELETE then
        self:removeItemsBySubtype(subtype)
    end
    self:sortTableViewData()
    if self._viewTableView then
        self._viewTableView:reloadData()
    end
end

function TicketShopFeesExchangeView:onTableViewCellSizeForTable(view)
    return self._tableViewItemModelSize.width, self._tableViewItemModelSize.height
end

function TicketShopFeesExchangeView:onTableViewCellAtIndex(view, index)
    local idx = index + 1
    local cell = view:dequeueCell()
    local item
    if not cell then
        cell = cc.TableViewCell:new()
        item = self._tableViewItemModel:clone()
        if not item then
            return cell
        end
        item:setTouchEnabled(false)
        item:setVisible(true)
        item:setAnchorPoint(cc.p(0, 0))
        item:setPosition(0, 0)
        item:setName("__item")
        cell:addChild(item)
    else
        item = cell:getChildByName("__item")
    end
    item:setTag(self._boxItems[idx].type)
    XH.UITool.setVisible(item, "KW_PRODUCT_NAME", not self._boxItems[idx].isSubItem)
    XH.UITool.setVisible(item, "KW_PRODUCT_ARROW", not self._boxItems[idx].isSubItem)
    XH.UITool.setVisible(item, "KW_PRODUCT_NAME_SUB", self._boxItems[idx].isSubItem)
    XH.UITool.setText(item, "KW_PRODUCT_NAME_SUB", self._boxItems[idx].name)
    XH.UITool.setText(item, "KW_PRODUCT_NAME", self._boxItems[idx].name)
    if self._boxItems[idx].isOpenSub then
        XH.UITool.loadTexture(item, "KW_PRODUCT_ARROW", "shop_fuka_arrow_2.png", ccui.TextureResType.plistType)
    elseif self._boxItems[idx].isOpenSub == false then
        XH.UITool.loadTexture(item, "KW_PRODUCT_ARROW", "shop_fuka_arrow_1.png", ccui.TextureResType.plistType)
    end
    return cell
end

function TicketShopFeesExchangeView:numberOfCellsInTableView(view)
    if #self._boxItems > 0 then
        return #self._boxItems
    end
    return self:getTableSize(EXCHANGE_PRODUCT_LIST)
end

function TicketShopFeesExchangeView:scrollViewDidScroll(view)
    print("")
end

function TicketShopFeesExchangeView:getTableSize(table)
    local itemCount = 0
    for _, _ in pairs(table) do
        itemCount = itemCount + 1
    end
    return itemCount
end

function TicketShopFeesExchangeView:onGetJingDongKaList(event)
    --加载京东卡到道具列表
    if event.data then
        if next(event.data) then
            self._jingDongKaList = event.data
        end
        self:initUI()
    else
        XH.TipTool.showToast("福卡兑换开小差了,请联系客服~", 2)
    end
end

return TicketShopFeesExchangeView
   uO  