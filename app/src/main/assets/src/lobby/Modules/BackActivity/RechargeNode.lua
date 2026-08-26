local RechargeNode = class("RechargeNode", XH.ViewBase)
local ShopConfig = require("lobby.Modules.Shop.Config")

local KW_BTN_RECHARGE = "_KW_BTN_RECHARGE"
local KW_LISTVIEW_RECHARGE = "_KW_LISTVIEW_RECHARGE"
local KW_TEXT_DIAMOND = "_KW_TEXT_DIAMOND"
local KW_TEXT_ZHEKOU = "_KW_TEXT_ZHEKOU"
local KW_ITEM_RECHARGE = "_KW_ITEM_RECHARGE"
local KW_PIC_AWARD = "_KW_PIC_AWARD"
local KW_TEXT_NUM = "_KW_TEXT_NUM"
local KW_TEXT_DAY = "_KW_TEXT_DAY"
local KW_TEXT_RECHARGE_LIMIT = "_KW_TEXT_RECHARGE_LIMIT"

local KW_DAY = { "一", "二", "三", "四", "五", "六", "七" }

function RechargeNode:getCSBPath()
    return "cocosStudio/hall/CSB/Activity/BackActivity/BackActivityRechargeLayer.csb"
end

function RechargeNode:getBindingInfo()
    return {
        ["_KW_BTN_GO_STROE"] = { varName = "_btnStore", type = XH.UI_TYPE.IMAGE_TOBUTTON, onTouchEnded = "onTouchEventGoStore" },
        ["_KW_PANEL_RECHARGE_INFO_1"] = { varName = "_panelRecharge1" },
        ["_KW_PANEL_RECHARGE_INFO_2"] = { varName = "_panelRecharge2" },
        ["_KW_PANEL_RECHARGE_INFO_3"] = { varName = "_panelRecharge3" },
    }
end

function RechargeNode:getProxyEvents()
    return {
        { module = XH.lobby:getModule("BackActivity"), eventName = "BackActivityDownloadImage", callBack = "onDownloadImageCallBack" },
    }
end

function RechargeNode:ctor(param, father)
    param = param or { }
    RechargeNode.super.ctor(self, param)
    self._downloadImageUrlList = { }
    self._downloadImageNodeList = { }

    self._father = father
    self._panelRechargeList = { self._panelRecharge1, self._panelRecharge2, self._panelRecharge3 }
    self:initNode()
end

------------------------------------------------- init ---------------------------------------------
function RechargeNode:initNode()
    self._downloadImageUrlList = {}
    self._downloadImageNodeList = {}
    local awardInfo = XH.lobby:getModule("BackActivity"):getActAwardInfo()
    local actInfo = XH.lobby:getModule("BackActivity"):getActInfo()

    for key, _ in ipairs(awardInfo.rechargeAward) do
        local rachargePanel = self._panelRechargeList[key]

        local dayText = rachargePanel:getChildByName(KW_TEXT_DAY)
        if dayText then
            dayText:setString("第" .. KW_DAY[key] .. "天")
        end

        local zhekouText = ccui.Helper:seekWidgetByName(rachargePanel, KW_TEXT_ZHEKOU)
        if zhekouText then
            zhekouText:setString(awardInfo.zhekou[key] .. "\n折")
        end

        local limitText = rachargePanel:getChildByName(KW_TEXT_RECHARGE_LIMIT)
        if limitText then
            limitText:setString(actInfo.rechargeLimit[key] .. "元")
        end

        local diamondText = rachargePanel:getChildByName(KW_TEXT_DIAMOND)
        if diamondText then
            diamondText:setString("x" .. actInfo.rechargeLimit[key] * 100)
        end

        local awardBtn = rachargePanel:getChildByName(KW_BTN_RECHARGE)
        if awardBtn then
            local today = ""
            if key == actInfo.rechargeDay and actInfo.rechargeAaward[key] == 0 then
                today = "1"
            elseif key ~= actInfo.rechargeDay and actInfo.rechargeAaward[key] == 0 then
                today = "2"
            end
            awardBtn:loadTexture("act_btn_buy" .. actInfo.rechargeAaward[key] .. today .. ".png", ccui.TextureResType.plistType)
            awardBtn:ignoreContentAdaptWithSize(true)
            if actInfo.rechargeAaward[key] == 0 and today == tostring(1) then
                awardBtn.awardId = tonumber(key)
                awardBtn:addTouchEventListener(handler(self, self.onTouchEventGoStore))
            elseif actInfo.rechargeAaward[key] == 1 then
                awardBtn.awardId = tonumber(key)
                awardBtn:addTouchEventListener(handler(self, self.onTouchEventGetAward))
            end
        end

        local listView = ccui.Helper:seekWidgetByName(rachargePanel, KW_LISTVIEW_RECHARGE)
        if listView then
            local itemPanel = ccui.Helper:seekWidgetByName(listView, KW_ITEM_RECHARGE)
            listView:setItemModel(clone(itemPanel))
            listView:removeAllChildren()
            listView:removeAllItems()
            for _, rechargeAwardInfo in pairs(awardInfo.rechargeAward[key]) do
                listView:pushBackDefaultItem()
                local Items = listView:getItems()
                local clonePanel = Items[#Items]

                local numText = clonePanel:getChildByName(KW_TEXT_NUM)
                if numText then
                    numText:setString("x" .. rechargeAwardInfo.cnt)
                end

                local awardPic = clonePanel:getChildByName(KW_PIC_AWARD)
                local url = rechargeAwardInfo.url
                if awardPic and url then
                    self:addDownloadImageList(url, awardPic)
                end
            end
            listView:setContentSize(itemPanel:getContentSize().width *(#listView:getItems()) + 10, itemPanel:getContentSize().height + 10)
            local listViewBG = ccui.Helper:seekWidgetByName(rachargePanel, "_KW_BG_LISTVIEW")
            listViewBG:setContentSize(itemPanel:getContentSize().width *(#listView:getItems()) + 20, listViewBG:getContentSize().height)
        end
    end

     self:downloadImage()
     self:runBtnAni()
end

function RechargeNode:onTouchEventGoStore(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.playerData:flushPlayerDrop()
    XH.viewManager:openView("ShopView", nil, { productType = ShopConfig.SpecialTag.DIAMOND })
    local awardId = send.awardId or 0
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_BACK_ACT_RECHARGE_BTN,{goStore = awardId})
end

function RechargeNode:onTouchEventGetAward(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local awardType = "recharge"
    local awardId = send.awardId
    if awardType and awardId then
        XH.lobby:getModule("BackActivity"):reqBackAwardProtocol(awardType, awardId)
    end
end

function RechargeNode:addDownloadImageList(url, node)
    if url and node then
        local find = false
        for key, urlVal in ipairs(self._downloadImageUrlList) do
            if urlVal == url then
                table.insert(self._downloadImageNodeList[key], node)
                find = true
                break
            end
        end
        if not find then
            table.insert(self._downloadImageUrlList, url)
            self._downloadImageNodeList[#self._downloadImageUrlList] = { }
            table.insert(self._downloadImageNodeList[#self._downloadImageUrlList], node)
        end
    end
end

function RechargeNode:onDownloadImageCallBack(event)
    if event then
        local imagePath = event.data.imagePath
        local url = event.data.url

        for key, urlVal in ipairs(self._downloadImageUrlList) do
            if urlVal == url then
                for _, node in pairs(self._downloadImageNodeList[key]) do
                    node:loadTexture(imagePath, ccui.TextureResType.localType)
                    node:setVisible(true)
                end
                break
            end
        end
    end
end

function RechargeNode:downloadImage()
    for _, url in pairs(self._downloadImageUrlList) do
        XH.lobby:getModule("BackActivity"):reqDownloadImage(url)
    end
end

function RechargeNode:runBtnAni()
    local actInfo = XH.lobby:getModule("BackActivity"):getActInfo()
    for key, panelInfo in ipairs(self._panelRechargeList) do
        if actInfo.rechargeAaward[key] == 0 and key == actInfo.rechargeDay then
            local getAwardBtn = panelInfo:getChildByName(KW_BTN_RECHARGE)
            self:runTimeLineAni(getAwardBtn)
            return
        end
    end
end

function RechargeNode:runTimeLineAni(node)
    node:stopAllActions()
    local action = cc.CSLoader:createTimeline(self:getCSBPath())
    node:runAction(action)
    action:gotoFrameAndPlay(0, 105, true)
end

return RechargeNode      