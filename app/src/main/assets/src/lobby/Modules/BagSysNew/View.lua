--新背包系统界面

local BagMainViewNew = class("BagMainViewNew", XH.ViewBase)
local ShowPropFunction = require("lobby.Modules.BagSysNew.ShowPropFunction")
local BagConfig = require("lobby.Modules.BagSysNew.Config")

local DEFAULT_DOWNLOAD_IMAGE_DIR = device.writablePath .. "tmpImageCachDir/"

local KW_MIG_TEXT_BG_TIME = "KW_MIG_TEXT_BG_TIME"
local KW_MIG_TEXT_BG_TIME_EX = "KW_MIG_TEXT_BG_TIME_EX"
local KW_IMG_PRODUCT_IMG = "KW_IMG_PRODUCT_IMG"
local KW_IMG_PRODUCT_IMG_BG = "_KW_IMG_PRODUCT_IMG_BG"
local KW_IMG_PRODUCT_ANI_BG = "_KW_IMG_PRODUCT_ANI_BG"
local KW_BTN_UNLOAD = "_KW_BTN_UNLOAD"
local KW_BTN_LOAD = "_KW_BTN_LOAD"
local KW_TXT_NAME = "KW_TXT_NAME"
local KW_TXT_PRODUCT_NAME = "KW_TXT_PRODUCT_NAME"
local KW_LAYOUT_ANI_POS = "_KW_LAYOUT_ANI_POS"
local KW_LAYOUT_ANI_BG = "_KW_LAYOUT_ANI_BG"
local KW_TXT_DESC = "KW_TXT_DESC"
local KW_TXT_PROP_DETAIL = "KW_TXT_PROP_DETAIL"
local KW_TIME_PROP_BG = "_KW_TIME_PROP_BG"
local KW_COUNT_PROP_BG = "_KW_COUNT_PROP_BG"
local KW_TXT_DESC_COUNT = "_KW_TXT_DESC_COUNT"
local KW_IMG_NOT_USE = "_KW_IMG_NOT_USE"
local KW_TEXT_DESC_TIME = "_KW_TEXT_DESC_TIME"
local KW_TXT_DESC_FOREVER = "_KW_TXT_DESC_FOREVER"
local KW_TXT_DESC_TIMEEVER = "_KW_TXT_DESC_TIMEEVER"

local KW_HORIZONTAL_DIS = 0 -- 左右边距
local KW_VECTICAL_DIS = 10 -- 顶边距
local KW_HORIZONTAL_X_DIS = 0 -- 每个item水平最小间距
local KW_VECTICAL_Y_DIS = 10 -- 每个item垂直间距
local KW_HORIZONTAL_WITH = 810 --固定宽度显示，宽度
local KW_SCROLLVIEW_HEIGHT_OFFSET = 20 --scrollview内框高度偏移量

local KW_DEFAULT_ITEM_SIZE = cc.size(270,290)
local KW_ZJB_PKG_ID = 2

local KW_DAY_SECOND_TIME = 86400
local KW_MIN_SECOND_TIME = 3600
local KW_SECOND_TIME = 60

local KW_ADD_HOUR_STR = "addhour"

-- 道具标签（道具服配置的）
local KW_PROP_ALL_TAG_ID            = "all_prop"        --所有
local KW_PROP_COUPON_ID             = "prop_coupon"     --优惠卡券
local KW_PROP_HEAD_FRAME_TAG_ID     = "head_frame"      --头像框
local KW_PROP_MAH_BACK_TAG_ID       = "mahback"         --牌背
local KW_PROP_CARD_HOLDER_TAG_ID    = "card_holder"     --压牌器
local KW_PROP_JPQ_TAG_ID            = "prop_jpq"        --记牌器
local KW_PROP_XPQ_TAG_ID            = "prop_xpq"        --洗牌券
local KW_PROP_TABLE_TAG_ID          = "table_bg"        --桌布
local KW_PROP_ENTER_ANI_TAG_ID      = "prop_rqdh"       --入场动画
local KW_PROP_DOUBLE_CARD_ID        = "cilent_double_card"       --加倍卡


--页签排序:如果配置了这个表，会按照这个表排序，否则不会
local KW_CATEGORY_NAME_SORT = {
    KW_PROP_ALL_TAG_ID,
    KW_PROP_COUPON_ID,
    KW_PROP_HEAD_FRAME_TAG_ID, 
    KW_PROP_MAH_BACK_TAG_ID, 
    KW_PROP_CARD_HOLDER_TAG_ID, 
    KW_PROP_JPQ_TAG_ID,
    KW_PROP_XPQ_TAG_ID,
    KW_PROP_TABLE_TAG_ID,
    KW_PROP_ENTER_ANI_TAG_ID,
    KW_PROP_DOUBLE_CARD_ID,
}

--背包配置ID对应的标签名字，id配置在：lobby.modules.BagSysNew.Config 的 BagList
local KW_BAGID_CATEGORYNAME = {
    [4] = KW_PROP_JPQ_TAG_ID,
    [6] = KW_PROP_XPQ_TAG_ID,
    [10] = KW_PROP_HEAD_FRAME_TAG_ID,
    [11] = KW_PROP_CARD_HOLDER_TAG_ID,
    [12] = KW_PROP_MAH_BACK_TAG_ID,
    [16] = KW_PROP_TABLE_TAG_ID,
    [17] = KW_PROP_ENTER_ANI_TAG_ID,
    [18] = KW_PROP_COUPON_ID,
    [99999] = KW_PROP_DOUBLE_CARD_ID,
}

--动画节点名字
local KW_ANI_NAME_DOWN       = "downArrowAni"

--头像框动画配置
local HEAD_FRAME_ANI_CONFIG = {
    [150326] =
    {
        addAni = true,
        path = "animation/Lobby/Base/head-tx-zcjb/head-tx-zcjb.ExportJson",
        amatureName = "head-tx-zcjb",
        animationName = "Animation1",
    },
}


function BagMainViewNew:getCSBPath()
    return "cocosStudio/hall/CSB/BagLayerNew.csb"
end

function BagMainViewNew:ctor(param)
    BagMainViewNew.super.ctor(self,param)

    -- 默认显示的行数
    self._curLineCount = 2

    -- 当前选中标签页
    self._displayCateory = KW_PROP_ALL_TAG_ID

    -- 当前选中标签页内容的下标
    self._displayCateoryIndex = 1

    -- 分类后的道具 propstag->props
    self._categoryDevideData = {}
    
    --当前时间
    self._curTimeStamp = os.time()
    self:initRequest()
    self:initUI()

    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_16) 
end

function BagMainViewNew:getBindingInfo()
    return {
        ["_KW_SCROLLVIEW_DISPLAY"] = { varName = "_displayList" },
        ["_KW_PANEL_PRODUCT_MOD"] = { varName = "_productMod" },
        ["_KW_BTN_TYPE_MOD"] = { varName = "_btnType" },
        ["_KW_LIST_PRODUCT_TYPE"] = { varName = "_leftBtnParent" },
        ["_KW_BTN_CLOSE"] = { varName = "_btnClose", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventClose" },

        ["_KW_BMFONT_NUM_DIAMOND"] = { varName = "_bmfontNumDiamond" },
        ["_KW_BMFONT_NUM_ROOM_CARD"] = { varName = "_bmfontNumRoomCard" },
        ["_KW_BMFONT_NUM_GOLD"] = { varName = "_bmfontNumGold" },
        ["_KW_BTN_SHOP"] = { varName = "_btnShop", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onTouchEventOpenShopLayer" },
        ["_KW_IMG_NO_ITEM"] = { varName = "_imgNoItem" },
        ["_KW_BMFONT_NOTIEM"] = {varName = "_bmfontNoItem"},
        ["_KW_PROP_DETAIL_BG"] = { varName = "_imgPropDetail" },
        ["_KW_PRODUCT_TYPE_ANI_POS"] = { varName = "_typeAniNode" },
        ["_KW_IMG_AREA"] = { varName = "_areaImage" },
        ["_KW_TEXT_AREA"] = { varName = "_areaText" },
    }
end

function BagMainViewNew:getProxyEvents()

    return {
        { module = XH.playerData, eventKeyName = "EVENT_PLAYER_DROP_CHANGED", callBack = "showTopInfo" },
        { module = XH.lobby:getModule("BagSysNew"), eventKeyName = "EVENT_SELF_BACK_PACK_DATA", callBack = "onRefresh" },
        { module = XH.lobby:getModule("BagSysNew"), eventKeyName = "EVENT_USE_PROPS", callBack = "onUseProps" },
        { module = XH.lobby:getModule("BagSysNew"), eventKeyName = "EVENT_OPERATE_PROPS", callBack = "onOperateProps" },
        { module = XH.lobby:getModule("BagSysNew"), eventKeyName = "EVENT_BAG_REFRESH_SYSTEM_TIME", callBack = "onSystemTime" },
        { module = XH.lobby:getModule("BagSysNew"), eventKeyName = "EVENT_COUPON_DATA_LIST", callBack = "onRefresh" },
        { module = XH.lobby:getModule("BagSysNew"), eventKeyName = "EVENT_UPDATE_DOUBLE_CARD", callBack = "onRefreshDouble" }
    }
end

function BagMainViewNew:initRequest()
    XH.lobby:getModule("BagSysNew"):reqSelfBagkData(true)
    XH.lobby:getModule("BagSysNew"):autoUseProp()
    XH.lobby:getModule("BagSysNew"):reqCouponListInfo()
end

function BagMainViewNew:initUI()
    self:showTopInfo()
    if self._imgPropDetail then
        self._imgPropDetail:setVisible(false)
    end

    --如果是外省渠道包，对地区进行隐藏
    if XH.ChannelTool.judgeIsChannel() then
        if self._areaImage and self._areaText then
            self._areaText:setString("")
            self._areaImage:setVisible(false)
        end
    else
        --显示当前区域
        if self._areaImage and self._areaText then
            local areaString = XH.areaData:getAreaName()
            self._areaText:setString(areaString)
            self._areaImage:setVisible(true)
         end
    end

    if self._displayList then
        self._displayList:setScrollBarEnabled(false)
    end

    if self._leftBtnParent then
        self._leftBtnParent:setScrollBarEnabled(false)
    end
end
-------------------------- custom event

-- 刷新界面消息
function BagMainViewNew:onRefresh(event)
    self:showTopInfo()
    XH.lobby:getModule("BagSysNew"):reqSystemTimesTask()
    self:showAllBagProp()
end

--使用了道具，没使用的道具，使用掉
function BagMainViewNew:onUseProps(event)
end

--操作了道具，装备，卸下
function BagMainViewNew:onOperateProps(event)
end

function BagMainViewNew:onSystemTime(event)
    self._curTimeStamp = event.data
    self:showAllBagProp()
end

-------------------------- touch event

-- 关闭按钮
function BagMainViewNew:onTouchEventClose(send, event)
    self:close()
end

-- 打开商城按钮
function BagMainViewNew:onTouchEventOpenShopLayer(send, event)
    if event == ccui.TouchEventType.began then
        send:setColor(cc.c3b(160, 160, 160))
    elseif event == ccui.TouchEventType.ended or ccui.TouchEventType.canceled then
        send:setColor(cc.c3b(255, 255, 255))
    end

    if event ~= ccui.TouchEventType.ended then
        return
    end
    self:close()
    XH.viewManager:openView("ShopView")
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_19)  
end

function BagMainViewNew:onBtnTouchEventPropItem(productData, send, event)
    if event ~= ccui.TouchEventType.ended then
        return
    end
    self._displayCateoryIndex = tonumber(send:getName())
    self:showPropItemSelected(send, productData)
end

function BagMainViewNew:onTabChanged(cateGoryName, send, event)
    if event == ccui.TouchEventType.began then
        send:setColor(cc.c3b(160, 160, 160))
    elseif event == ccui.TouchEventType.ended or ccui.TouchEventType.canceled then
        send:setColor(cc.c3b(255, 255, 255))
    end

    if event ~= ccui.TouchEventType.ended then
        return
    end
    if self._leftBtnParent then
        local leftTabBtns = self._leftBtnParent:getChildren()
        for _, btn in pairs(leftTabBtns) do
            local isSelect = btn == send
            btn:setEnabled(not isSelect)
            local color = isSelect and cc.c3b(255, 255, 255) or cc.c3b(191, 108, 29)
            local tabName = btn:getChildByName(KW_TXT_PRODUCT_NAME)
            if tabName then
                tabName:setTextColor(color)
            end
        end
        self:showPropByCateGory(cateGoryName)
        self._displayCateory = cateGoryName
        self._displayCateoryIndex = 1
    end
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_17, {tabName = cateGoryName}) 
end

function BagMainViewNew:onBtnTouchEventLoadProp(productData, send, event)
    if event == ccui.TouchEventType.began then
        send:setColor(cc.c3b(160, 160, 160))
    elseif event == ccui.TouchEventType.ended or ccui.TouchEventType.canceled then
        send:setColor(cc.c3b(255, 255, 255))
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.MY_18, {propsname = productData.propsname ,propstype = productData.mutex_category_two}) 
    end

    if event ~= ccui.TouchEventType.ended then
        return
    end

    local bagsysModule = XH.lobby:getModule("BagSysNew")

    if productData.coupon_type then
        if productData.effect_goods  then
            local coupon_goods = productData.effect_goods.special_goods
            local showProp = bagsysModule:devideCouponProp(coupon_goods)
            XH.viewManager:openView("ShopView", nil, { productType = showProp })
        end
    else 
        local props_state = productData.props_state
        if props_state == XH.BagSysProtocol.PROPS_STATE.PS_IN_BACKPACK then -- 未装备 如果服务没有UseProp，需要先UseProp
            if bagsysModule:isTimeProp(productData) and productData.propscnt > 0 then
                bagsysModule:reqUseProps(productData.propsid, productData.propscnt)
            else
                bagsysModule:reqOperateProps(XH.BagSysProtocol.OPERATE_PROPS.OP_EQUIP, productData.propsid)
            end
        else
            if bagsysModule:isTimeProp(productData) and productData.propscnt > 0 then
                bagsysModule:reqUseProps(productData.propsid, productData.propscnt)
            else
                XH.TipTool.showTip( {type = XH.TIP_LAYER_TYPE.OK,} ,"该道具已装备过!")    
            end
        end
    end

end

function BagMainViewNew:onBtnTouchEventUnloadProp(productData, send, event)
    if event == ccui.TouchEventType.began then
        send:setColor(cc.c3b(160, 160, 160))
    elseif event == ccui.TouchEventType.ended or ccui.TouchEventType.canceled then
        send:setColor(cc.c3b(255, 255, 255))
    end

    if event ~= ccui.TouchEventType.ended then
        return
    end
    
    local props_state = productData.props_state
    local bagsysModule = XH.lobby:getModule("BagSysNew")
    if props_state == XH.BagSysProtocol.PROPS_STATE.PS_IN_BACKPACK then -- 未装备
        XH.TipTool.showTip( {type = XH.TIP_LAYER_TYPE.OK,} ,"道具未装备!")
        return
    end
    bagsysModule:reqOperateProps(XH.BagSysProtocol.OPERATE_PROPS.OP_UNEQUIP, productData.propsid)
end

-------------------------- show UI

function BagMainViewNew:showPropItemSelected(itemNode, productData)
    if not itemNode then return end
    if not self._displayList then return end

    local allProductItem = self._displayList:getChildren()
    for _ , item in pairs(allProductItem) do
        local visible = item == itemNode
        XH.UITool.setVisible(item,"_KW_IMG_SEL_BG", visible)
    end

    self:showPropItemDetail(productData)
end

function BagMainViewNew:showPropItemDetail(productData)
    local detailItem = self._imgPropDetail
    if not detailItem then return end
    detailItem:setVisible(true)
    
    if productData.propsname then
        productData.propsname = string.gsub(productData.propsname,"（永久）","")
    end
    XH.UITool.setText(detailItem, KW_TXT_NAME, productData.propsname)
    -- XH.UITool.setText(detailItem, KW_TXT_DESC, productData.props_overview or "")
    local textStr = productData.props_overview or ""   --支持优惠券详情传html文本，统一改成富文本显示
    textStr = "<font face='cocosStudio/Common/Font/fangzhengcuyuan.TTF' size='24' color='#B97345'>" .. textStr .. "</font>"
    local textNode = XH.UITool.seekNodeByName(detailItem, KW_TXT_DESC)
    textNode:removeAllChildren()
    local richText = ccui.RichText:createWithXML(textStr, { })
    richText:setAnchorPoint(cc.p(0.5, 0.5))
    richText:ignoreContentAdaptWithSize(false)
    richText:setContentSize(textNode:getContentSize())
    richText:setHorizontalAlignment(cc.TEXT_ALIGNMENT_LEFT)
    richText:setPosition(cc.p(textNode:getContentSize().width/2, textNode:getContentSize().height/2+10))
    XH.UITool.setText(detailItem, KW_TXT_DESC,"")
    textNode:addChild(richText)
    
    local bagModule = XH.lobby:getModule("BagSysNew")
    XH.UITool.setVisible(detailItem, KW_MIG_TEXT_BG_TIME, true)
    XH.UITool.setVisible(detailItem, KW_MIG_TEXT_BG_TIME_EX, false)
    if productData.is_time_prop then
        local nowTimestamp = self._curTimeStamp --取服务当前时间
        local endTimestamp = productData.endtime or 0
        local leftTime = endTimestamp - nowTimestamp
        if leftTime < 0 then leftTime = 0 end
        local dayTime = math.floor(leftTime / KW_DAY_SECOND_TIME) or 0--秒为单位
        local hourTime = math.floor((leftTime - dayTime * KW_DAY_SECOND_TIME) / KW_MIN_SECOND_TIME) or 0
        local minuteTime = math.floor((leftTime - dayTime*KW_DAY_SECOND_TIME - hourTime * KW_MIN_SECOND_TIME) / KW_SECOND_TIME) or 0
        if hourTime < 0 then hourTime = 0 end
        if minuteTime < 0 then minuteTime = 0 end
        local showStr
        if dayTime == 0 then
            showStr = "剩余时间:" .. hourTime .. "小时".. minuteTime .. "分钟"
        else
            showStr = "剩余时间:" .. dayTime .. "天" .. hourTime .. "小时".. minuteTime .. "分钟"
        end
        if productData.propsid == BagConfig.PROP_ID_DOUBLE_CARD then
            showStr = showStr .. "\n剩余数量:" ..  productData.propscnt
            XH.UITool.setVisible(detailItem, KW_MIG_TEXT_BG_TIME, false)
            XH.UITool.setVisible(detailItem, KW_MIG_TEXT_BG_TIME_EX, true)
        end
        XH.UITool.setText(detailItem, KW_TXT_PROP_DETAIL, showStr)
    else
        XH.UITool.setText(detailItem, KW_TXT_PROP_DETAIL, "剩余数量:" .. tostring(productData.propscnt))
        -- 如果是没使用的时效道具 则显示时间
        if bagModule:isTimeProp(productData) then
            local script = productData.script or ""
            local addHourCount = bagModule:getBagData():getLuaStrValue(script,KW_ADD_HOUR_STR)
            local propscnt = productData.propscnt or 0
            addHourCount = tonumber(addHourCount)
            if addHourCount then
                local day = math.floor(addHourCount*propscnt / 24)
                local hour = math.floor(addHourCount*propscnt - day*24)
                local showStr = "剩余时间:" .. tostring(day) .. "天" .. tostring(hour) .. "小时"
                XH.UITool.setText(detailItem, KW_TXT_PROP_DETAIL,  showStr)
            end
        end
    end
    if bagModule:judgeForeverProp(productData.propsid) then
        XH.UITool.setText(detailItem, KW_TXT_PROP_DETAIL,  "剩余时间: 永久")
    end
    local props_state = productData.props_state
    local isShowLoadBtn = props_state == XH.BagSysProtocol.PROPS_STATE.PS_IN_BACKPACK
    XH.UITool.setVisible(detailItem, KW_BTN_UNLOAD, not isShowLoadBtn)
    XH.UITool.setVisible(detailItem, KW_BTN_LOAD, isShowLoadBtn)
    --选中一张图片
    local imageUrl = ""
    productData.props_img = productData.props_img or {}
    for _, pinfo in pairs(productData.props_img) do
        if pinfo.package_id and tonumber(pinfo.package_id) == KW_ZJB_PKG_ID then
            local imgArray = pinfo.img
            if imgArray then
                for idx = 1 , #imgArray do
                    imageUrl = imgArray[idx].img_url or ""
                end
            end
            break
        end
    end
    
    local propAniBG = XH.UITool.seekNodeByName(detailItem, KW_IMG_PRODUCT_ANI_BG)
    if not ShowPropFunction.createHeadFrameAni(propAniBG,productData.propsid,0.8) then
        self:showImgUrl(detailItem, KW_IMG_PRODUCT_IMG, imageUrl, productData)
        XH.UITool.setVisible(detailItem, KW_IMG_PRODUCT_IMG_BG,true)
        XH.UITool.setVisible(detailItem, KW_IMG_PRODUCT_ANI_BG,false)
    else
        XH.UITool.setVisible(detailItem, KW_IMG_PRODUCT_ANI_BG,true)
        XH.UITool.setVisible(detailItem, KW_IMG_PRODUCT_IMG_BG,false)
    end

    XH.UITool.addTouchEventListener(detailItem, KW_BTN_UNLOAD, handler(productData, handler(self, self.onBtnTouchEventUnloadProp)))
    XH.UITool.addTouchEventListener(detailItem, KW_BTN_LOAD, handler(productData, handler(self, self.onBtnTouchEventLoadProp)))

    if bagModule:isTimeProp(productData) then
        --已经使用了的记牌器，不能卸下
        if productData.props_tag_id == KW_PROP_JPQ_TAG_ID then
            if productData.props_state == XH.BagSysProtocol.PROPS_STATE.PS_IN_BODY then
                XH.UITool.setVisible(detailItem, KW_BTN_UNLOAD, false)
            else
                XH.UITool.setVisible(detailItem, KW_BTN_LOAD, false)
            end
        end

        if productData.propscnt > 0 then
            XH.UITool.setVisible(detailItem, KW_BTN_UNLOAD, false)
            XH.UITool.setVisible(detailItem, KW_BTN_LOAD, true)
        end
    elseif bagModule:judgeForeverProp(productData.propsid) then
        XH.UITool.setVisible(detailItem, KW_BTN_UNLOAD, not isShowLoadBtn)
        XH.UITool.setVisible(detailItem, KW_BTN_LOAD, isShowLoadBtn)
    else
        --非时效道具，不能操作
        XH.UITool.setVisible(detailItem, KW_BTN_UNLOAD, false)
        XH.UITool.setVisible(detailItem, KW_BTN_LOAD, false)  
    end

    -----显示动画
    local aniNode = detailItem:getChildByName(KW_LAYOUT_ANI_POS)
    self:clearAni(aniNode)
    if productData.props_tag_id == KW_PROP_CARD_HOLDER_TAG_ID then
        self:showCardHolderAni(aniNode, 1.5)
    elseif productData.props_tag_id == KW_PROP_TABLE_TAG_ID then
        self:showTableBgAni(aniNode, 1.5)
    elseif productData.props_tag_id == KW_PROP_HEAD_FRAME_TAG_ID then
        self:showHeadFrameAni(aniNode, 1, productData.propsid)
    end
end

function BagMainViewNew:clearAni(aniNode)
    if not tolua.isnull(aniNode) then
        aniNode:removeAllChildren()
    end
    XH.UITool.setVisible(aniNode:getParent(), KW_LAYOUT_ANI_BG, false)
end

function BagMainViewNew:showTopInfo()
    local function countNumScale(len)
        local fontSize = 1
        if len <= 6 then
            fontSize = 1.34
        end
        return fontSize
    end
    local numGoldFontSize = countNumScale(#tostring(XH.playerData:getSR()))
    if self._bmfontNumGold then
	    self._bmfontNumGold:setScale(numGoldFontSize)
	    self._bmfontNumGold:setText(XH.playerData:getSR())
    end
    local numRoomCardFontSize = countNumScale(#tostring(XH.playerData:getRoomCard()))
    if self._bmfontNumRoomCard then
        self._bmfontNumRoomCard:setScale(numRoomCardFontSize)
        self._bmfontNumRoomCard:setText(XH.playerData:getRoomCard())
    end
    local numDiamondFontSize = countNumScale(#tostring(XH.playerData:getDiamnd()))
    if self._bmfontNumDiamond then
        self._bmfontNumDiamond:setScale(numDiamondFontSize)
        self._bmfontNumDiamond:setText(XH.playerData:getDiamnd())
    end
end

function BagMainViewNew:showAllBagProp()
    -- 背包数据
    self._categoryDevideData = XH.lobby:getModule("BagSysNew"):getBagData():getSelfDevideCategoryData()

    --显示左边所有页签
    self:showLeftBtnType()

    --默认选中一个页签
    self:showCateGory(self._displayCateory)
    
    --选中页签展示的背包物品
    self:showPropByCateGory(self._displayCateory)
end

--显示左边页签，按照客户端的 KW_CATEGORY_NAME_SORT table内容排序
function BagMainViewNew:showLeftBtnType()
    local leftBtnTypeData = XH.lobby:getModule("BagSysNew"):getBagData():getDevideBagConfigDataTag()
    local couponCardList = XH.lobby:getModule("BagSysNew"):dealCouponData()
    if not leftBtnTypeData then return end

    local tmpCateGoryNameName = {}
    for cateGoryName, _ in pairs(leftBtnTypeData) do
        tmpCateGoryNameName[#tmpCateGoryNameName + 1] = cateGoryName
    end

    local resultTable = {KW_PROP_ALL_TAG_ID} --默认有个全部的页签
    --过滤掉商城没有的页签
    local propIdList = XH.lobby:getModule("BagSysNew"):getBagList()
    if propIdList and next(propIdList) then
        for _, propTag in ipairs(propIdList) do
            if KW_BAGID_CATEGORYNAME[propTag] and table.indexof(tmpCateGoryNameName,KW_BAGID_CATEGORYNAME[propTag]) then
                    if KW_BAGID_CATEGORYNAME[propTag] == KW_PROP_COUPON_ID then--如果没有优惠券就隐藏优惠卡券页签
                        if XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_WEBCOUPON_EXIST) or next(couponCardList) then
                            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_WEBCOUPON_EXIST, true)
                            table.insert(resultTable,KW_BAGID_CATEGORYNAME[propTag])
                        end
                    else
                        table.insert(resultTable,KW_BAGID_CATEGORYNAME[propTag])
                    end
                
            end
        end
    end
    table.insert(resultTable,KW_PROP_DOUBLE_CARD_ID) -- 默认塞入加倍卡页签

    local tmpNameSort = {}
    for _, cateGoryName in ipairs(KW_CATEGORY_NAME_SORT) do
        local existIndex = table.indexof(resultTable,cateGoryName)
        if existIndex then
            tmpNameSort[#tmpNameSort+1] = cateGoryName
            table.remove(resultTable,existIndex)
        end
    end

    table.insertto(tmpNameSort,resultTable)

    --用来克隆页签按钮
    if self._btnType and self._leftBtnParent then
        self._leftBtnParent:removeAllChildren()
        for _, cateGoryName in ipairs(tmpNameSort) do
            local categoryProp = leftBtnTypeData[cateGoryName]
            if categoryProp then
                local btnTab = self._btnType:clone()
                local cateGoryStr = cateGoryName
                if next(categoryProp) then
                    local tempCategoryTwo = categoryProp[1].category_two
                    cateGoryStr = (tempCategoryTwo and tempCategoryTwo ~= "") and tempCategoryTwo or categoryProp[1].props_tag_name
                end

                if cateGoryName == KW_PROP_ALL_TAG_ID then
                    cateGoryStr = "全部"
                end
                if cateGoryName == KW_PROP_COUPON_ID then
                    cateGoryStr = "优惠卡券"
                end
                if cateGoryName == KW_PROP_DOUBLE_CARD_ID then
                    cateGoryStr = "奖励加倍卡"
                end
                XH.UITool.setText(btnTab, KW_TXT_PRODUCT_NAME, tostring(cateGoryStr))
                self._leftBtnParent:pushBackCustomItem(btnTab)
                btnTab:addTouchEventListener(handler(cateGoryName, handler(self, self.onTabChanged)))
                btnTab:setName(cateGoryName)
            elseif cateGoryName == KW_PROP_DOUBLE_CARD_ID then  -- 奖励加倍卡单独塞入
                local btnTab = self._btnType:clone()
                local cateGoryStr = "奖励加倍卡"
                XH.UITool.setText(btnTab, KW_TXT_PRODUCT_NAME, tostring(cateGoryStr))
                self._leftBtnParent:pushBackCustomItem(btnTab)
                btnTab:addTouchEventListener(handler(cateGoryName, handler(self, self.onTabChanged)))
                btnTab:setName(cateGoryName)
            end
        end
    end
end

--选中某个页签
function BagMainViewNew:showCateGory(cateGoryName)
    self._displayCateory = cateGoryName
    if self._leftBtnParent then
        local leftTabBtns = self._leftBtnParent:getChildren()
        for _, btn in ipairs(leftTabBtns) do
            local isSelect = btn:getName() == cateGoryName
            btn:setEnabled(not isSelect)
            local color = isSelect and cc.c3b(255, 255, 255) or cc.c3b(191, 108, 29)
            local tabName = btn:getChildByName(KW_TXT_PRODUCT_NAME)
            if tabName then
                tabName:setTextColor(color)
            end
        end
        if leftTabBtns and #leftTabBtns >= 6 then
            self:showDownAni()
        end
    end
end

--标签页内容显示
function BagMainViewNew:showPropByCateGory(cateGoryName)
    local productList = self:getBagDataByType(cateGoryName)
    local totalNeedCount = #productList
    -- 动态初始化滑动区域大小
    self:resetDisPlayContentSize(totalNeedCount)
    if self._imgNoItem then
        self._imgNoItem:setVisible(totalNeedCount == 0)
        self._bmfontNoItem:setVisible(totalNeedCount == 0)
    end

    -- 创建道具UI
    for i = 1, totalNeedCount do
        local productData = productList[i]
        local item = self:getProductByIndex(i)
        if not item then
            item = self:createNewProduct(productData)
        end
        item:setZOrder(totalNeedCount - i)
        self:updateItemUI(item, productData, i)
        item:addTouchEventListener(handler(productData, handler(self,self.onBtnTouchEventPropItem)))
    end

    local productCount = self:getProductCount()
    -- 隐藏多余的item
    for i = totalNeedCount + 1, productCount do
        local item = self:getProductByIndex(i)
        if item then
            item:setVisible(false)
        end
    end

    --默认隐藏详情
    if self._imgPropDetail then
        self._imgPropDetail:setVisible(false) 
    end

    --默认选中一个item
    if totalNeedCount > 0 then
        local defaultIndex = self._displayCateoryIndex <= totalNeedCount and self._displayCateoryIndex or 1
        local defaultItem = self:getProductByIndex(defaultIndex)
        self:showPropItemSelected(defaultItem,productList[defaultIndex] or {})
    end

    --跳转到顶层
    if self._displayList and self._displayList.jumpToTop then
        self._displayList:jumpToTop()
    end
end

function BagMainViewNew:updateItemUI(item, productData, index)
    if tolua.isnull(item) then
        return
    end
    item:setName(tostring(index))
    item:setTag(productData.propsid or 0)
    item:setPosition(self:getDisplayPos(index))
    item:setVisible(true)

    if productData.propsname then
        productData.propsname = string.gsub(productData.propsname,"（永久）","")
    end
    XH.UITool.setText(item, KW_TXT_NAME, productData.propsname)
    if productData.propsname and #productData.propsname >= 24 then
        XH.UITool.setFontSize(item, KW_TXT_NAME,24)
    end
    XH.UITool.setVisible(item,KW_IMG_NOT_USE,false)
    local bagModule = XH.lobby:getModule("BagSysNew")

    if productData.is_time_prop then
        XH.UITool.setVisible(item,KW_TIME_PROP_BG,true)
        XH.UITool.setVisible(item,KW_COUNT_PROP_BG,false)

        local nowTimestamp = self._curTimeStamp -- 取服务当前时间
        local endTimestamp = productData.endtime or 0
        local leftTime = endTimestamp - nowTimestamp
        if leftTime < 0 then leftTime = 0 end
        local dayTime = math.floor(leftTime / KW_DAY_SECOND_TIME) or 0
        local hourTime = math.floor((leftTime - dayTime * KW_DAY_SECOND_TIME) / KW_MIN_SECOND_TIME) or 0
        local minuteTime = math.floor((leftTime - dayTime * KW_DAY_SECOND_TIME - hourTime * KW_MIN_SECOND_TIME) / KW_SECOND_TIME) or 0
        if dayTime == 0 then
            XH.UITool.setText(item, KW_TEXT_DESC_TIME, hourTime .. 's' .. minuteTime .. 'f')
        else
            XH.UITool.setText(item, KW_TEXT_DESC_TIME, dayTime .. 't' .. hourTime .. 's')
        end
    else
        XH.UITool.setVisible(item, KW_TIME_PROP_BG,false)
        XH.UITool.setVisible(item, KW_COUNT_PROP_BG,true)
        XH.UITool.setText(item, KW_TXT_DESC_COUNT, '*' .. " " .. (productData.propscnt or ""))

        -- 如果是没使用的时效道具 则显示时间
        if bagModule:isTimeProp(productData) then
            XH.UITool.setVisible(item,KW_TIME_PROP_BG,true)
            XH.UITool.setVisible(item,KW_COUNT_PROP_BG,false)
            XH.UITool.setVisible(item,KW_IMG_NOT_USE,true)
            local script = productData.script or "" 
            local addHourCount = bagModule:getBagData():getLuaStrValue(script,KW_ADD_HOUR_STR)
            local propscnt = productData.propscnt or 0
            addHourCount = tonumber(addHourCount)
            if addHourCount then
                local dayTime = math.floor(addHourCount*propscnt / 24)
                local hourTime = math.floor(addHourCount*propscnt - dayTime*24)
                XH.UITool.setText(item, KW_TEXT_DESC_TIME, dayTime .. 't' .. hourTime .. 's')
            end
        end
    end
    if bagModule:judgeForeverProp(productData.propsid) then
        XH.UITool.setVisible(item, KW_TXT_DESC_COUNT, false)
        XH.UITool.setVisible(item, KW_TXT_DESC_FOREVER, true)
        XH.UITool.setVisible(item, KW_TEXT_DESC_TIME, false)
        XH.UITool.setVisible(item, KW_TXT_DESC_TIMEEVER, true)
        XH.UITool.setVisible(item,KW_IMG_NOT_USE,false)
    else
        XH.UITool.setVisible(item, KW_TXT_DESC_COUNT, true)
        XH.UITool.setVisible(item, KW_TXT_DESC_FOREVER, false)
        XH.UITool.setVisible(item, KW_TEXT_DESC_TIME, true)
        XH.UITool.setVisible(item, KW_TXT_DESC_TIMEEVER, false)
    end
    --选中一张图片
    local imageUrl = ""
    productData.props_img = productData.props_img or {}
    for _, pinfo in pairs(productData.props_img) do
        if pinfo.package_id and tonumber(pinfo.package_id) == KW_ZJB_PKG_ID then
            local imgArray = pinfo.img
            if imgArray then
                for idx = 1 , #imgArray do
                    imageUrl = imgArray[idx].img_url or ""
                end
            end
            break
        end
    end

    
    local propAniBG = XH.UITool.seekNodeByName(item, KW_IMG_PRODUCT_ANI_BG)
    if not ShowPropFunction.createHeadFrameAni(propAniBG,productData.propsid,0.8) then
        self:showImgUrl(item, KW_IMG_PRODUCT_IMG, imageUrl, productData)
        XH.UITool.setVisible(item, KW_IMG_PRODUCT_IMG_BG,true)
        XH.UITool.setVisible(item, KW_IMG_PRODUCT_ANI_BG,false)
    else
        XH.UITool.setVisible(item, KW_IMG_PRODUCT_ANI_BG,true)
        XH.UITool.setVisible(item, KW_IMG_PRODUCT_IMG_BG,false)
    end
end

-- 计算item的位置
function BagMainViewNew:getDisplayPos(index)
    local nextIndex = index or self:getProductCount()
    nextIndex = nextIndex - 1
    -- 计算位置从0开始

    -- 每个item的大小
    local productModSize = KW_DEFAULT_ITEM_SIZE
    if self._productMod then
        productModSize = self._productMod:getContentSize()
    end

    local modHeight = productModSize.height
    local modWidth = productModSize.width
    local displayContentSize = self._displayList:getInnerContainerSize()

    -- 计算最小间距下每行的个数
    local miniTotalXDis = modWidth + KW_HORIZONTAL_X_DIS
    -- local withoutBoderWidt = displayContentSize.width - KW_HORIZONTAL_DIS * 2
    local withoutBoderWidt = KW_HORIZONTAL_WITH - KW_HORIZONTAL_DIS * 2
    local everyLineCount = math.floor(withoutBoderWidt / miniTotalXDis)

    -- 中间的水平分布
    local xDif =(withoutBoderWidt - everyLineCount * modWidth) /(everyLineCount - 1)
    local lineIndex = math.floor(nextIndex / everyLineCount)

    local totalXDis = modWidth + xDif
    local totalYDis = modHeight + KW_VECTICAL_Y_DIS

    -- 起始的位置
    local startPosX = KW_HORIZONTAL_DIS + modWidth / 2
    local startPosY = KW_VECTICAL_DIS + modHeight / 2

    local posX = startPosX + totalXDis *(nextIndex % everyLineCount)
    local posY = displayContentSize.height -(startPosY + totalYDis * lineIndex)

    return posX, posY
end

-- 获得UI界面上item的总数
function BagMainViewNew:getProductCount()
    return self._displayList:getChildrenCount() or 0
end

-- 通过索引获得item
function BagMainViewNew:getProductByIndex(index)
    return self._displayList:getChildByName(tostring(index))
end

-- 添加一个item到界面上.挂载到ScrollView中去
function BagMainViewNew:createNewProduct(productData)
    local item = self._productMod:clone()
    if item then
        item:retain()
        item:removeFromParent()
        self._displayList:addChild(item)
        item:release()
    end
    return item
end

-- 计算显示区域的大小的位置
function BagMainViewNew:resetDisPlayContentSize(productCount)
    if not productCount then
        return
    end
    -- 每个item的大小
    local productModSize = KW_DEFAULT_ITEM_SIZE
    if self._productMod then
        productModSize = self._productMod:getContentSize()
    end

    local modWidth = productModSize.width
    local displayContentSize = self._displayList:getInnerContainerSize()

    -- 计算最小间距下每行的个数
    local miniTotalXDis = modWidth + KW_HORIZONTAL_X_DIS
    local withoutBoderWidt = displayContentSize.width - KW_HORIZONTAL_DIS * 2
    local everyLineCount = math.floor(withoutBoderWidt / miniTotalXDis)
    local lineCount = math.ceil(productCount / everyLineCount)

    if lineCount ~= self._curLineCount then
        -- 现有的滑动区域需要变化
        displayContentSize = self._displayList:getContentSize()
        self._displayList:setInnerContainerSize(cc.size(displayContentSize.width, lineCount * (productModSize.height + KW_SCROLLVIEW_HEIGHT_OFFSET)))
        self._curLineCount = lineCount
    end
end

function BagMainViewNew:showImgUrl(item, imgName, imageUrl, productData)
    XH.UITool.ignoreContentAdaptWithSize(item, KW_IMG_PRODUCT_IMG, true)
    XH.UITool.setVisible(item, KW_IMG_PRODUCT_IMG,false)
    local downloadImage = require("app.Req.DownloadImage")
    local reqDownloadImage = downloadImage:new()
    reqDownloadImage:addReqCallBack(self, function(node, cell, type1)
        if type1 == XH.Req.TYPE.SUCCESS then
            local tmpName = XH.StringTool.getImageNameByUrl(imageUrl)
            local imagePath = DEFAULT_DOWNLOAD_IMAGE_DIR .. tmpName  
            XH.UITool.loadTexture(item, imgName, imagePath, ccui.TextureResType.localType)

            local propImg = XH.UITool.seekNodeByName(item, KW_IMG_PRODUCT_IMG)
            local propImgBG = XH.UITool.seekNodeByName(item, KW_IMG_PRODUCT_IMG_BG)
            if propImg and propImgBG then
                self:AdaptWithSize(propImg, propImgBG:getContentSize(),productData)
            end
        end
    end)
    reqDownloadImage:setUrl(imageUrl, false)
end

function BagMainViewNew:AdaptWithSize(sender, bgSize, productData)
    if sender then
        sender:setVisible(true)
        local width = sender:getContentSize().width
        local height = sender:getContentSize().height
        local scaleNum = width
        if width < height then
            scaleNum = height
        end
        local scale = bgSize.width / scaleNum
        sender:setScale(scale)
        if productData and productData.props_tag_id == KW_PROP_MAH_BACK_TAG_ID then
            sender:setScale(scale - 0.15)
        end
    end
end

--压牌器动画
function BagMainViewNew:showCardHolderAni(parentNode, scale)
    scale = scale or 1
    if not parentNode then return end
    XH.UITool.setVisible(parentNode:getParent(), KW_LAYOUT_ANI_BG, true)
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
        parentNode:addChild(propAni)
        propAni:setScale(scale)
    end
end

--箭头动画
function BagMainViewNew:showDownAni()
    local aniNode = self._typeAniNode
    if not aniNode then return end

    if aniNode:getChildByName(KW_ANI_NAME_DOWN) then
        return
    end

    local params1 = {
            path = "animation/Lobby/Base/dj-jt/",
            ske = "dj-jt_ske.json",
            tex = "dj-jt_tex.json",
            armatureName = "Armature",
            dragonBonesName = "dj-jt",
            animationName = "newAnimation"
        }
    local propAni = display.playDargonBonesAnimByTimes(params1, 0)

    if propAni then
        aniNode:addChild(propAni)
        propAni:setName(KW_ANI_NAME_DOWN)
    end
end

--桌布动画
function BagMainViewNew:showTableBgAni(parentNode, scale)
    scale = scale or 1
    if not parentNode then return end

    local aniConfig = {
        path = "animation/Lobby/Base/sy_light/sy_light.ExportJson",
        amatureName = "sy_light",
        animationName = "Animation1",
    }

    pcall(function()
        local OFFSET_Y = 36
        if not cc.FileUtils:getInstance():isFileExist(aniConfig.path) then
            return
        end
        ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(aniConfig.path)
        local showArmature = ccs.Armature:create(aniConfig.amatureName)
        if showArmature then
            parentNode:addChild(showArmature)
            showArmature:setAnchorPoint(cc.p(0.5, 0.5))
            showArmature:getAnimation():play(aniConfig.animationName, -1, 1)
            showArmature:setScale(scale)
            showArmature:setPositionY(showArmature:getPositionY() + OFFSET_Y)
        end
    end)
end

--头像框动画
function BagMainViewNew:showHeadFrameAni(parentNode, scale, propsid)
    scale = scale or 1
    local aniConfig = HEAD_FRAME_ANI_CONFIG[tonumber(propsid)]
    if not aniConfig then return end
    local OFFSET_X = 0
    local OFFSET_Y = 35
    pcall(function()
        if not cc.FileUtils:getInstance():isFileExist(aniConfig.path) then
            return
        end
        ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(aniConfig.path)
        local showArmature = ccs.Armature:create(aniConfig.amatureName)
        if showArmature then
            parentNode:addChild(showArmature)
            showArmature:setAnchorPoint(cc.p(0.5, 0.5))
            showArmature:getAnimation():play(aniConfig.animationName, -1, 1)
            showArmature:setScale(scale)
            showArmature:setPosition(cc.p(showArmature:getPositionX() + OFFSET_X, showArmature:getPositionY() + OFFSET_Y))
        end
    end)
end

-- 刷新加倍卡显示
function BagMainViewNew:onRefreshDouble()
    self:showPropByCateGory(self._displayCateory)
end

function BagMainViewNew:getBagDataByType(cateGoryName)
    local data = self._categoryDevideData[cateGoryName] or {}
    data = clone(data)
    if cateGoryName == KW_PROP_ALL_TAG_ID then  -- 塞入加倍卡数据
        local doubleCards = XH.lobby:getModule("BagSysNew"):getDoubleCards()
        if doubleCards and #doubleCards > 0 then
            for i = 1, #doubleCards do
                data[#data + 1] = doubleCards[i]
            end
        end
    elseif cateGoryName == KW_PROP_DOUBLE_CARD_ID then
        data = XH.lobby:getModule("BagSysNew"):getDoubleCards()
    end
    return data
end

return BagMainViewNew�  