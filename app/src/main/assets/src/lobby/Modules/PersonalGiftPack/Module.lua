local PersonalGiftPackModule = class("PersonalGiftPackModule", XH.ModuleBase)

PersonalGiftPackModule.EVENT_CONFIG_DATA_CHANGED = "EVENT_CONFIG_DATA_CHANGED"
PersonalGiftPackModule.EVNET_GIFTPACK_BUY_RESULT = "EVNET_GIFTPACK_BUY_RESULT"

PersonalGiftPackModule.KW_PERSONAL_GIFTPACK_FLAG = "PersonalGiftPack"
PersonalGiftPackModule.KW_PRODUCT_ID_FLAG = "productid"
PersonalGiftPackModule.KW_PROP_ID_FLAG = "propid"

function PersonalGiftPackModule:ctor()
    PersonalGiftPackModule.super.ctor(self)
    self._selectedProductData = nil
    self._tempConfigData = nil
end

function PersonalGiftPackModule:getProxyEvents()
    return {
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVNET_DIAMOND_BUY_RESULT",  callBack = "onDiamondBuyResult" },
    }
end

function PersonalGiftPackModule:getReqConfig()
    return {
        ReqCacheWindow = { reqPath = "app.Req.ReqCacheWindow", callBack = self.onRespWebActivity },
        ReqDiamondProductsInfo = { reqPath = "lobby.Req.Shop.ReqProductList", callBack = self.respDiamondConfigInfo },
        ReqGiftPackInfo = { reqPath = "lobby.Req.BagSysNew.ReqGiftPackInfo", callBack = self.onRespGiftPackInfo },
    }
end

function PersonalGiftPackModule:reqWebActivity()
    self:startReq("ReqCacheWindow")
end

function PersonalGiftPackModule:analyBtnLinkStr(linkStr)
    local productID = ""
    local propID = 0
    local linkTable = string.split(linkStr, ";")
    for _, v in pairs(linkTable) do
        if string.find(v, self.KW_PRODUCT_ID_FLAG.."=") then
            local giftIdTable = string.split(v, "=")
            if #giftIdTable == 2 then
                productID = tostring(giftIdTable[2])
            end
        end
        if string.find(v, self.KW_PROP_ID_FLAG.."=") then
            local propIdTable = string.split(v, "=")
            if #propIdTable == 2 then
                propID = tonumber(propIdTable[2])
            end
        end
    end
    return productID, propID
end

function PersonalGiftPackModule:analyButtonImgConf(webConf)
    webConf.button_config = webConf.button_config or {}
    local allBtnsConf = {}
    for name, btnConf in pairs(webConf.button_config) do
        local conf = {}
        conf.imageUrl = btnConf.img.url
        conf.name = btnConf.link
        conf.width = btnConf.img.w
        conf.height = btnConf.img.h
        conf.posX = btnConf.img.x
        conf.posY = btnConf.img.y
        local productID, propID = self:analyBtnLinkStr(btnConf.link)
        if productID and productID ~= "" then
            conf.productID = productID
        end
        if propID and propID ~= 0 then
            conf.propID = propID
        end
        allBtnsConf[name] = conf
    end
    return allBtnsConf
end

function PersonalGiftPackModule:analyBgImgConf(webConf)
    local conf = {}
    conf.imageUrl = webConf.popup_img
    conf.name = webConf.name
    return conf
end

-- 解析web返回配置数据，转换成底图数据和按钮数据
function PersonalGiftPackModule:analyWebConf(confData)
    confData = confData or {}
    local resultConf = {}
    local allPorpIDs = {}
    for _, singleConf in pairs(confData) do
        if string.find(singleConf.config_params, self.KW_PERSONAL_GIFTPACK_FLAG) then
            local listConf = {}
            listConf.bgConf = self:analyBgImgConf(singleConf)
            listConf.btnsConf = self:analyButtonImgConf(singleConf)
            listConf.pop_sort = singleConf.pop_sort
            listConf.pop_scene = string.split(singleConf.pop_scene, ",")
            for _, btnConf in pairs(listConf.btnsConf) do
                local _, propID = self:analyBtnLinkStr(btnConf.name)
                table.insert(allPorpIDs, propID)
            end
            table.insert(resultConf, listConf)
        end
    end
    table.sort(resultConf, function (a, b)
        return tonumber(a.pop_sort) < tonumber(b.pop_sort)
    end)
    return resultConf, allPorpIDs
end

function PersonalGiftPackModule:onRespWebActivity(cell, reqType, data)
    if reqType == XH.Req.TYPE.SUCCESS then
        local configData, allPorpIDs = self:analyWebConf(data)
        self._tempConfigData = configData
        self:reqLeftBuyCount(allPorpIDs)
    end
end

-- 调用时可以不传propID
-- autoOrder传1表示漏单补发
function PersonalGiftPackModule:buyDiamond(productId, propID, autoOrder)
    -- 未实名不能购买 海外玩家不用实名认证

    if (not XH.lobby:getModule("Shop"):isAleardyRealName()) and (not XH.lobby:getModule("Lobby"):getIsForeignIP()) then
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK,
            funcOK = function()
                XH.viewManager:openView("IdentityCheckView")
            end
        } , "PLEASE_REAL_NAME")
        return
    end
    self._selectedProductData = {
        productId = productId,
        propID = propID,
        autoOrder = autoOrder
    }
    self:reqDiamondConfigInfo()
end

-- 请求商品信息
function PersonalGiftPackModule:reqDiamondConfigInfo()
    XH.TipTool.showLoading()
    self:startReq("ReqDiamondProductsInfo", XH.configerManager:getShopAppId(), XH.configerManager:getShopPackageAppId())
end

function PersonalGiftPackModule:respDiamondConfigInfo(req, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        data = data or {}
        for _, v in pairs(data) do
            if self._selectedProductData and tostring(v.productId) == tostring(self._selectedProductData.productId) then
                local tempPropID = self._selectedProductData.propID or tonumber(v.propid)
                XH.lobby:getModule("Shop"):reqBuyDiamondGiftpack(v, tempPropID, self._selectedProductData.autoOrder)
                return
            end
        end
    else
        XH.TipTool.showTip( {
            type = XH.TIP_LAYER_TYPE.OK,
        } , req:getMessage())
    end
end

function PersonalGiftPackModule:reqLeftBuyCount(propIDs)
    if not propIDs or #propIDs <= 0 then
        self:dispatchEvent({ name = self.EVENT_CONFIG_DATA_CHANGED, msg = { configData = {} } })
        return
    end
    self:startReq("ReqGiftPackInfo", propIDs, XH.areaData:getSrsGroupID(), 10)
end

function PersonalGiftPackModule:analyLeftBuyCount(giftInfos)
    giftInfos = giftInfos or {}
    local leftData = {}
    for _, giftinfo in pairs(giftInfos) do
        leftData[tostring(giftinfo.giftpackid)] = giftinfo.sc
    end
    return leftData
end

function PersonalGiftPackModule:onRespGiftPackInfo(req, type, data)
    if not self._tempConfigData then
        return
    end
    if type == XH.Req.TYPE.SUCCESS then
        local leftData = self:analyLeftBuyCount(data.giftInfos)
        local finalShowData = {}
        for _, conf in ipairs(self._tempConfigData) do
            local finalBtns = {}
            for _, btnConf in pairs(conf.btnsConf) do
                if btnConf.propID and leftData[tostring(btnConf.propID)] and leftData[tostring(btnConf.propID)] > 0 then
                    table.insert(finalBtns, btnConf)
                end
            end
            if #finalBtns > 0 then
                conf.btnsConf = finalBtns
                table.insert(finalShowData, conf)
            end
        end
        self:dispatchEvent({ name = self.EVENT_CONFIG_DATA_CHANGED, msg = { configData = finalShowData } })
    end
end

function PersonalGiftPackModule:addUserDefaultOpenTime()
    local time, index = self:analyOpenTimeInfo()
    if os.date("%Y%m%d") ~= os.date("%Y%m%d", time) then
        index = 0
    end
    index = index + 1
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_PERSONAL_GIFTPACK_OPEN_INFO, os.time().."_"..index)
end

function PersonalGiftPackModule:analyOpenTimeInfo()
    local openInfoStr = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_PERSONAL_GIFTPACK_OPEN_INFO, "")
    local infoTable = string.split(openInfoStr, "_")
    if infoTable and #infoTable == 2 then
        return tonumber(infoTable[1]), tonumber(infoTable[2])
    end
    return 0, 0
end

function PersonalGiftPackModule:checkNeedAutoOpenView()
    local time, index = self:analyOpenTimeInfo()
    if time == 0 or os.date("%Y%m%d") ~= os.date("%Y%m%d", time) or index < 3 then
        return true
    end
    return false
end

function PersonalGiftPackModule:onDiamondBuyResult(event)
    if not event.msg or not event.msg.resultTag then
        return
    end
    local bSuccess = event.msg.resultTag == XH.lobby:getModule("Shop").BUY_DIAMOND_RESULT.SUCCESS
    if bSuccess then
        self:reqWebActivity()
    end
    self:dispatchEvent({ name = self.EVNET_GIFTPACK_BUY_RESULT, msg = {bSuccess = bSuccess} })
end

return PersonalGiftPackModule   �"  