-- 大厅（主场景）
local lobbyBgPath = "cocosStudio/hall/Image/LobbyBg/"
-- local DEFAULT_DOWNLOAD_IMAGE_DIR = device.writablePath .. "tmpImageCachDir/"

local HorizontalMenuBar = require("lobby.Modules.Lobby.MenuBar.HorizontalMenuBar")
local VerticalMenuBar = require("lobby.Modules.Lobby.MenuBar.VerticalMenuBar")
local LobbyView = class("LobbyView", XH.ViewBase)

local LobbyConfig = require("lobby.Modules.Lobby.Config")
local ShopConfig = require("lobby.Modules.Shop.Config")
local SxvipConfig = require("lobby.Modules.Sxvip.Config")

local ShowPropFunction = require("lobby.Modules.BagSysNew.ShowPropFunction")
local GameNewTypeConfiger = require("app.Config.Parse.GameNewTypeConfiger")

local PromoteConfig = require("lobby.Modules.Promote.Config")
local LuckyTaskDefine = require("lobby.Modules.LuckyTask.Define")

local UrlConf = require("app.Config.UrlConf")
local ConfigurationDefine = require("lobby.Modules.Configuration.Define")

local DEFAULT_SECOND_SCENE_SIZE = cc.size(900,150)
local HEAD_FRAME_NAME = "HEAD_FRAME_NAME"

local UNIONGAME_NAME ={
    [3021] = "人人捕鱼",
    [3022] = "谁是首富",
    [3015] = "玲萌直播",
    [3002] = "捕鱼王",
    [3001] = "决战沙邑",
}

function LobbyView:getCSBPath()
    return "cocosStudio/hall/CSB/MainScene.csb"
end

function LobbyView:getBindingInfo()
    return {
        ["_KWA_BTND_ADD_ROOM_CARD"] = { varName = "KWA_BTND_ADD_ROOM_CARD", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KWA_BTND_ADD_ROOM_CARD"},
        ["_KWA_BTND_ADD_DIAMOND"] = { varName = "KWA_BTND_ADD_DIAMOND", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KWA_BTND_ADD_DIAMOND"},
        ["_KWA_BTND_CREATE_BACK_BOX_ROOM"] = { varName = "_createBackBoxRoomBtn", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickCreateRoom" },
        ["_KWA_BTND_TEA_HOUSE"] = { varName = "KWA_BTND_TEA_HOUSE", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KWA_BTND_TEA_HOUSE" },
        ["_KWA_IMAGE_HEAD"] = { varName = "KWA_IMAGE_HEAD"},
        ["_KWA_HEAD_PANEL"] = { varName = "KWA_HEAD_PANEL", type = XH.UI_TYPE.PANEL_TOBUTTON, onTouchEnded = "on_KWA_BTND_HEAD"},
        ["_KWA_BTN_VIP"] = { varName = "KWA_BTN_VIP", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KWA_BTN_VIP" },
        ["_KW_HEAD_FRAME"] = { varName = "_headFrameImg" },
        ["_KW_HEAD_FRAME_TEMP"] = { varName = "_headFrameImgTemp" },
        ["_KW_IMAGE_HEAD_FLAG"] = { varName = "_imageHeadFlag" },
        ["_KWA_LABEL_HEAD_ROOMCARD"] = { varName = "KWA_LABEL_HEAD_ROOMCARD" },
        ["_KWA_XIUXIANDATING_TITLE"] = { varName = "KWA_XIUXIANDATING_TITLE" },
        ["_KWA_NICKNAME"] = { varName = "KWA_NICKNAME" },
        ["_KWA_USER_ID"] = { varName = "KWA_USER_ID" },
        ["_KWA_BTN_COPY"] = { varName = "KWA_BTN_COPY", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KWA_BTN_COPY" },
        ["_KW_IMG_AREA"] = { varName = "areaImage"},
        ["_KWA_AREA_PANEL"] = { varName = "KWA_AREA_PANEL", type = XH.UI_TYPE.PANEL_TOBUTTON , onTouchEnded = "onTouchEventChangeArea" },
        ["_KW_TEXT_AREA"] = { varName = "areaText" },
        ["_KWA_NORMAL_LOBBY_PANEL"] = { varName = "KWA_NORMAL_LOBBY_PANEL" },
        ["_KWA_XIUXIAN_LOBBY_PANEL"] = { varName = "KWA_XIUXIAN_LOBBY_PANEL" },
        ["_KWA_LABEL_HEAD_DIAMOND"] = { varName = "KWA_LABEL_HEAD_DIAMOND" },
        ["_KW_IMAGE_DIAMOND"] = { varName = "KW_IMAGE_DIAMOND" },
        ["_KWA_HEAD_INFO_BG"] = { varName = "_headInfoPanel" },
        ["_KW_IMG_BOX_ROOM_FLAG"] = {varName="_imgBoxRoomFlag"},
        ["_KW_IMG_BOX_ROOM_FREE"] = {varName="_imgBoxRoomFree"},
        ["_KW_IMG_TEA_FLAG"] = {varName="_imgTeaFlag"},
        ["_KW_IMG_BOX_ROOM_FLAG_CPS"] = {varName="_imgBoxRoomFlagCps"},
        ["_KW_IMG_TEA_FLAG_CPS"] = {varName="_imgTeaFlagCps"},
        ["_KW_LOBBY_TIPS"] = {varName="_lobbyTips"},
        ["_KW_LOBBY_TIPS_CONTENT"] = {varName="_lobbyTipsContent"},

        -- 金币场相关
        ["_KWA_LEFT_POS"] = { varName = "KWA_LEFT_POS" },
        ["_KWA_RIGHT_POS"] = { varName = "KWA_RIGHT_POS" },
        ["_KWA_RIGHT_TOP_PANEL"] = { varName = "KWA_RIGHT_TOP_PANEL" },
        ["_KWA_ROOM_CARD_BG"] = { varName = "KWA_ROOM_CARD_BG", type = XH.UI_TYPE.IMAGE_TOBUTTON, style = XH.UIButton.CLICK_STYLE.NOEFFECT, onTouchEnded = "on_KWA_BTND_ADD_ROOM_CARD" },
        ["_KWA_DIAMOND_BG"] = { varName = "KWA_DIAMOND_BG", type = XH.UI_TYPE.IMAGE_TOBUTTON, style = XH.UIButton.CLICK_STYLE.NOEFFECT, onTouchEnded = "on_KWA_BTND_ADD_DIAMOND" },
        ["_KWA_GOLD_NUM_BG"] = { varName = "KWA_GOLD_NUM_BG" },
        ["_KWA_GOLD_NUM_BG_2"] = { varName = "KWA_GOLD_NUM_BG_2" },
        ["_KWA_LABEL_HEAD_GOLD"] = { varName = "KWA_LABEL_HEAD_GOLD" },
        ["_KWA_LABEL_HEAD_GOLD_2"] = { varName = "KWA_LABEL_HEAD_GOLD_2" },
        ["_KWA_BTND_ADD_GOLD"] = { varName = "KWA_BTND_ADD_GOLD", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KWA_BTND_ADD_GOLD" },
        ["_KWA_TOUCH_LAYER_PANEL"] = { varName = "KWA_TOUCH_LAYER_PANEL", type = XH.UI_TYPE.LAYOUT, onTouchEnded = "on_KWA_TOUCH_LAYER_PANEL" },
        ["_KWA_GOLD_BTND_JOIN_ROOM"] = { varName = "KWA_GOLD_BTND_JOIN_ROOM", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickJoinRoom" },
        ["_KW_MINI_GAME_PANEL"] = { varName = "KW_MINI_GAME_PANEL"},
        
        ---比赛场更新
        ["_KW_IMG_TEA_HOUSE_UPDATE"] = { varName = "_imgTeaHouseUpdate" },
        ["_KW_LB_TEA_HOUSE_UPDATE"] = { varName = "_lbTeaHouseUpdate" },
        ["_KW_TEXT_TEA_HOUSE_UPDATE"] = { varName = "_textTeaHouseUpdate" },

        --
        ["_KWA_MENU_BAR"] = { varName = "KWA_MENU_BAR" },
        ["_KWA_TOP_MENU_BAR"] = { varName = "KWA_TOP_MENU_BAR" },
        ["_KWA_GOLD_TOP_MENU_BAR"] = { varName = "KWA_GOLD_TOP_MENU_BAR" },
        ["_KWA_GOLD_MENU_BAR"] = { varName = "KWA_GOLD_MENU_BAR" },
        ["_KWA_RIGHT_MENU_BAR"] = { varName = "_rightMenuBarPos" },
        ["_KWA_LEFT_MENU_BAR"] = { varName = "_leftMenuBarPos" },
        ["_KWA_BOTTOM_LEFT_MENU_BAR"] = { varName = "_bottomLeftMenuBarPos" },
        --临时bq相关控制
        ["_KW_TEXT_TEMPORARY"] = { varName = "_TextBanQuan"},

        -- 好友系统相关
        ["_KWA_IM_LIST_PANEL"] = { varName = "KWA_IM_LIST_PANEL" },

        ["_KW_TEA_REMODEL_ICON"] = {varName = "_imgTeaRemodel"},
        ["_KW_IMG_CREATE_AWARD_TIP"] = {varName = "_imgShowTeaHouseAwardTip"},
        ["_KW_IMG_BACKGROUNG"] = { varName = "_backgroundImg"},

        ["_KWA_PARY_DEC"] = { varName = "KWA_PARY_DEC"},

        --cps渠道包界面布局调整
        ["_KW_BOX_PANEL"] = {varName = "_KW_BOX_PANEL"},
        ["_KW_GOLD_PANEL"] = {varName = "_KW_GOLD_PANEL"},
        ["_KW_BOX_PANEL_CPS"] = {varName = "_KW_BOX_PANEL_CPS"},
        ["_KW_GOLD_PANEL_CPS"] = {varName = "_KW_GOLD_PANEL_CPS"},
        ["_KW_BTND_CREATE_BACK_BOX_ROOM_CPS"] = { varName = "_createBackBoxRoomBtnCps", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickCreateRoom" },
        ["_KW_BTND_TEA_HOUSE_CPS"] = { varName = "KW_BTND_TEA_HOUSE_CPS", type = XH.UI_TYPE.BUTTON, onTouchEnded = "on_KWA_BTND_TEA_HOUSE" },
        ["_KW_GOLD_BTND_JOIN_ROOM_CPS"] = { varName = "KW_GOLD_BTND_JOIN_ROOM_CPS", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickJoinRoom" },
        ["_KW_MINI_GAME_PANEL_CPS"] = { varName = "KW_MINI_GAME_PANEL_CPS"},
        ["_KW_IMG_CPS_BACKGROUND"] = { varName = "_KW_IMG_CPS_BACKGROUND",type = XH.UI_TYPE.LAYOUT, onTouchEnded = "on_KW_IMG_CPS_BACKGROUND"  },
        ["_KW_PANEL_CHOOSE_GUIDE"] = { varName = "KW_PANEL_CHOOSE_GUIDE"},
        ["_KWA_TOP_HORSE"] = { varName = "_KWA_TOP_HORSE", onTouchEnded = "on_KWA_TOP_HORSE"},
        ["_KWA_TOP_HORSE_TEXT"] = { varName = "_KWA_TOP_HORSE_TEXT"},
        ["_KWA_TOP_HORSE_LEN"] = { varName = "_KWA_TOP_HORSE_LEN"},
        ["_KW_ACT_FREE_TIPS"] = { varName = "_KW_ACT_FREE_TIPS", onTouchEnded = "onClickFreeView"},
        ["_KW_ACT_FREE_TIP"] = { varName = "_KW_ACT_FREE_TIP", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onClickFreeTipView"},
        ["_KW_ACT_FREE_ALL"] = { varName = "_KW_ACT_FREE_ALL" },
        ["_KW_ACT_FREE_NEXT"] = { varName = "_KW_ACT_FREE_NEXT" },
        ["_KW_ACT_FREE_NEXT_1"] = { varName = "_KW_ACT_FREE_NEXT_1" },
        ["_KW_ACT_FREE_NEXT_2"] = { varName = "_KW_ACT_FREE_NEXT_2" },
        ["_KW_ACT_FREE_LW"] = { varName = "_KW_ACT_FREE_LW" },
        ["_KW_BG_CHANGE_IMG_1"] = { varName = "_KW_BG_CHANGE_IMG_1" },
        ["_KW_BG_CHANGE_IMG_2"] = { varName = "_KW_BG_CHANGE_IMG_2" },
        ["_KW_BG_CHANGE_IMG_3"] = { varName = "_KW_BG_CHANGE_IMG_3" },
        ["_KW_BG_CHANGE_IMG_4"] = { varName = "_KW_BG_CHANGE_IMG_4" },
        ["_KW_BG_CHANGE_IMG_5"] = { varName = "_KW_BG_CHANGE_IMG_5" },
        ["_KW_BG_CHANGE_IMG_6"] = { varName = "_KW_BG_CHANGE_IMG_6" },
        ["_KW_PANEL_TIME_LEFT"] = { varName = "_KW_PANEL_TIME_LEFT" },
    }
end

function LobbyView:getInitProxyEventsState()
    return "enterTransitionFinish"
end

function LobbyView:getProxyEvents()
    return {
        { module = XH.teaHouseManager, eventKeyName = "EVENT_TEA_HOUSE_UPDATE_START", callBack = "onTeaHouseUpdateStart" },
        { module = XH.teaHouseManager, eventKeyName = "EVENT_TEA_HOUSE_UPDATE_CHANGE", callBack = "onTeaHouseUpdateChanged" },
        { module = XH.playerData, eventKeyName = "EVENT_AGENT_FLAG_CHANGED", callBack = "onAgentFlagChanged" },
        { module = XH.playerData, eventKeyName = "EVENT_PHONE_CHANGED", callBack = "onPhoneChanged" },
        { module = XH.playerData, eventKeyName = "EVENT_BIND_DSB_CHANGED", callBack = "onBindDSBChanged" },
        { module = XH.playerData, eventKeyName = "EVENT_POSITION_CHANGED", callBack = "onPositionChanged" },
        { module = XH.playerData, eventKeyName = "EVENT_PLAYER_DROP_CHANGED", callBack = "onItemUpdate" },
        { module = XH.playerData, eventKeyName = "EVENT_BIND_USER_CHANGED", callBack = "onCheckBindUser" },
        { module = XH.playerData, eventKeyName = "EVENT_SR_CHANGED", callBack = "onSRChanged" },
        { module = XH.playerData, eventKeyName = "EVENT_GET_PLAYER_POSITION", callBack = "onEnterH5GameListen" },
        { module = XH.playerData, eventKeyName = "EVENT_PLAYER_RECHARGE_FLAG", callBack = "onPlayerRechargeFlag" },
        { module = XH.goldConfigManager, eventKeyName = "GOLD_EVENT_CONFIG", callBack = "onRefreshGoldUI" },
        { module = XH.taskManager, eventKeyName = "BREAK_GIFT_BAG_SHOW_TIME", callBack = "onBreakGiftBagEvent" },
        { module = XH.lobby:getModule("BackActivity"), eventKeyName = "KW_BACK_ACT_SHOW_ICON", callBack = "onShowBackActIcon" },
        { module = XH.lobby:getModule("Mail"), eventKeyName = "MAIL_EVENT_CHECKNEW", callBack = "refreshMailRed" },
        { module = XH.lobby:getModule("NewVip"), eventKeyName = "EVENT_SHOW_VIP", callBack = "onShowVip" },
        { module = XH.lobby:getModule("Lobby"), eventKeyName = "SHOW_XIUXIAN_GAME", callBack = "showXIUXIANLobby" },
        { module = XH.lobby:getModule("Lobby"), eventKeyName = "SHOW_BOX_GAME", callBack = "showYouXILobby" },
        { module = XH.lobby:getModule("Lobby"), eventKeyName = "EVENT_SHOW_DETAIL_LAYER", callBack = "onShowDetailGoldLayer" },
        { module = XH.lobby:getModule("BagSysNew"), eventKeyName = "EVENT_BAGMANAGER_CHANGE_HEAD_FRRAME", callBack = "onEventChangeHeadFrame" },
        { module = XH.lobby:getModule("BagSysNew"), eventKeyName = "KW_SHOW_BAG_BTN_TEXT", callBack = "onEventSetBagBtnText" },
        { module = XH.lobby:getModule("GiftBag"), eventKeyName = "EVENT_SHOW_BREAK_GIFT", callBack = "onBreakGiftBagShow"},
        { module = XH.lobby:getModule("SaleGiftbag"), eventKeyName = "TASK_TYPE_ID_DISCOUNT", callBack = "onShowDiscountLayer"},
        { module = XH.lobby:getModule("SaleGiftbag"), eventKeyName = "EVENT_SHOW_DISCOUNT_GIFT", callBack = "onEventShowDiscountGift"},
        { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_HUO_DONG_RED_POS", callBack = "onEventflushPhoneBindRedPos" },
        { module = XH.lobby:getModule("Lobby"), eventKeyName = "EVENT_OPEN_IM_VIEW", callBack = "onEventOpenImCallBack" },
        { module = XH.lobby:getModule("CusService"), eventKeyName = "EVENT_CUS_SERVICE_MSG_UNREAD", callBack = "refreshCurServiceRed" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVENT_HAVE_NEW_GIFT_BOX", callBack = "refreshShopIconRed" },
        { module = XH.lobby:getModule("Guide"), eventKeyName = "GUIDE_CREATE_BOX_ROOM", callBack = "on_GUIDE_CREATE_ROOM" },
        { module = XH.lobby:getModule("Guide"), eventKeyName = "GUIDE_JOIN_BOX_ROOM", callBack = "on_KWA_BTND_JOIN_ROOM" },
        { module = XH.lobby:getModule("Guide"), eventKeyName = "GUIDE_JOIN_TEA_HOUSE", callBack = "on_KWA_BTND_TEA_HOUSE" },
        { module = XH.lobby:getModule("Guide"), eventKeyName = "GUIDE_CHOOSEAREA", callBack = "on_GUIDE_CHOOSEAREA" },
        { module = XH.lobby:getModule("ToponAct"), eventKeyName = "KW_TOPON_ACT_SHOW_ICON", callBack = "onShowToponActIcon" },
        { module = XH.lobby:getModule("Shop"), eventKeyName = "EVENT_SHOP_NEW_ITEM", callBack = "refreshShopIconRed" },
        { module = XH.lobby:getModule("PrayAct"), eventKeyName = "EVENT_INVITE_PRAYACT_INFO", callBack = "onPrayActInfo"},
        { module = XH.lobby:getModule("LaborDayGift"), eventKeyName = "EVENT_FULSH_LABOR_DAY_ICON", callBack = "onEventShowLaborDay"},
        { module = XH.lobby:getModule("PersonalGiftPack"), eventKeyName = "EVENT_CONFIG_DATA_CHANGED", callBack = "onPersonalGiftPackInfo"},
        { module = XH.sdkManager, eventKeyName = "EVENT_H5GAME_CALLBACK", callBack = "onH5GameClose" },
        { module = XH.lobby:getModule("Lobby"), eventKeyName = "SHOW_START_H5ROOM",callBack = "startH5Room"},
        { module = XH.lobby:getModule("Lobby"), eventKeyName = "SHOW_LOBBY_ICONS",callBack = "onReqWebIconsBack"},
        { module = XH.lobby:getModule("Lobby"), eventKeyName = "SHOW_LEISURE_ENTRANCE",callBack = "onEventShowLeisureEntrance"},
        { module = XH.lobby:getModule("Lobby"), eventKeyName = "EVENT_GET_ICON_WORLD_POS",callBack = "onEventGetIconWorldPos"},
        { module = XH.lobby:getModule("CreateTeaHouseAct"), eventKeyName = "EVENT_FULSH_CREATE_TEAHOUSE_ACT_POPINFO",callBack = "onEventShowCreateTeaHouseAct"},
        { module = XH.viewManager,eventKeyName = "EVENT_CLOSE_VIEW", callBack = "onCloseView" },
        { module = XH.viewManager,eventKeyName = "EVENT_OPEN_VIEW", callBack = "onOpenView" },
        { module = XH.lobby:getModule("Lobby"), eventKeyName = "EVENT_SHOW_RED_POS", callBack = "onEventShowRedPos" },
        { module = XH.lobby:getModule("LuckyTurnTableAct"), eventKeyName = "EVENT_GET_ACT_DATA", callBack = "onGetLuckyTurnTableActData"},
        { module = XH.lobby:getModule("Promote"), eventKeyName = "EVENT_FULSH_PROMOTE_DAY_ICON", callBack = "onEventShowPromoteIcon"},
        { module = XH.lobby:getModule("LuckyTask"), eventKeyName = "EVENT_NUIAddLobbyIcon", callBack = "onEventShowLuckTaskIcon"},
        { module = XH.lobby:getModule("AoYun"), eventKeyName = "EVENT_NUIAddLobbyIcon", callBack = "onEventShowLuckTaskIcon"},
        { module = XH.lobby:getModule("LuckyTask"), eventKeyName = "EVENT_NotifyLuckyTaskJoin", callBack = "onEventLuckyTaskJoin"},
        { module = XH.lobby:getModule("LuckyTask"), eventKeyName = "EVENT_NUIUpdateLobbyIconBubbleTip", callBack = "onEventLuckyTaskIconTip"},
        { module = XH.lobby:getModule("Share"), eventKeyName = "EVENT_LOBBY_SHARE_DATA", callBack = "onEventFlushShareInfo"},
        { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_SHOW_SIGN_ICON", callBack = "onEventShowSignIcon" },
        { module = XH.lobby:getModule("TabsActivity"), eventKeyName = "EVENT_SHOW_SIGN_REDPOINT", callBack = "onEventShowSignRedPoint" },
        { module = XH.lobby:getModule("Article"), eventKeyName = "EVENT_ARTICLE_LIST_INFO", callBack = "onEventArticleList" },
        { module = XH.lobby:getModule("LimitFreeAct"), eventKeyName = "EVENT_FLUSH_FREE_ACT_INFO", callBack = "onEventFlushLimitFreeAct" },
        { module = XH.lobby:getModule("Sxvip"), eventKeyName = "SXVIP_EVENT_CONFIG_SUCC", callBack = "onUpdateSxvipStatus" },
        { module = XH.lobby:getModule("Sxvip"), eventKeyName = "SXVIP_EVENT_CONFIG_FAIL", callBack = "onUpdateSxvipStatus" },
        { module = XH.lobby:getModule("Sxvip"), eventKeyName = "SXVIP_EVENT_REMAINDER", callBack = "onUpdateSxvipStatus" },
        { module = XH.lobby:getModule("YGiftBankruptcy"), eventKeyName = "FLUSH_ACT_INFO", callBack = "onUpdateBankruptcyStatus" },
        { module = XH.lobby:getModule("YGiftDefeat"), eventKeyName = "FLUSH_ACT_INFO", callBack = "onUpdateGiftDefeatStatus" },
        { module = XH.lobby:getModule("Sxvip"), eventKeyName = "SXVIP_EVENT_MESSAGE_COUNT", callBack = "onUpdateSxvipStatus" },
        { module = XH.lobby:getModule("Configuration"), eventKeyName = "EVENT_REQ_CONFIGURATION_SUCCESS", callBack = "onGetConfiguration" },
    }
end

function LobbyView:onEnterEventInit()
    self._enterForegroundListener = cc.EventListenerCustom:create("APP_ENTER_FOREGROUND_EVENT",handler(self, self.onEnterCPSChannelForeground))
    cc.Director:getInstance():getEventDispatcher():addEventListenerWithSceneGraphPriority(self._enterForegroundListener,self)
end

function LobbyView:onCleanup()
    self:removeHorseSchedule()
    self:removeGoldSchedule()
    LobbyView.super.onCleanup(self)
end

function LobbyView:onExit()
    LobbyView.super.onExit(self)

    if self._enterForegroundListener then
        cc.Director:getInstance():getEventDispatcher():removeEventListener(self._enterForegroundListener)
    end

    if self.KWA_IM_LIST_PANEL then
        local child = self.KWA_IM_LIST_PANEL:getChildren()[1]
        if child then
            child:close()
        end
    end

    if self._mainRightMenuBar then
        local iMMessageIcon = self._mainRightMenuBar:getMenuByID(LobbyConfig.ICON_ID.HAO_YOU_XI_TONG)
        if iMMessageIcon then
            local child = iMMessageIcon:getChildren()[1]
            if child then
                child:close()
            end
        end
    end
end

-- 切换选区
function LobbyView:onTouchEventChangeArea(send, eventType)
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_CHANGE_AREA)
    XH.login:getModule("Login"):clearLoginStates()
    XH.loginData:setLobbyID(0)
    XH.Controller:getInstance():enterLogin(false,true)
end

function LobbyView:ctor() 
    LobbyView.super.ctor(self)
    self:setContentSize(display.width, display.height)
    self._curLobbyType = XH.LOBBY_TYPE.YUEJU
    
    self._mainTopMenuBar = nil
    self._mainBottomRightMenuBar = nil
    self._mainBottomLeftMenuBar = nil
    self._mainScondMenu = nil
    self._mainRightMenuBar = nil
    self._mainLeftMenuBar = nil
    self._goldTopMenuBar = nil
    self._goldMenuBar = nil
    self._goldSecondMenu = nil
    self._goldBottomLeftMenuBar = nil
    self._discountData = nil
    self:adaptForLiuHai()--左侧按钮列表刘海屏适配
    self:recordLobbyViewData()
    cc.UserDefault:getInstance():setBoolForKey("isNeedHideAdvertisement", false)
    cc.UserDefault:getInstance():setBoolForKey("isNeedHideAdvertisement1", false)

    -- self.KWA_NORMAL_LOBBY_PANEL:setVisible(false)
    -- self._KW_BOX_PANEL:setVisible(false)
    -- self._KW_GOLD_PANEL:setVisible(false)
    
    -- XH.UITool.setVisible(self, "panel_head", false)
end

function LobbyView:recordLobbyViewData()
    local data = {}
    data.time1 = os.time()
    data.userid = XH.playerData:getNumberID()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.LoginSuccess,data)
end

function LobbyView:onEnter()
    self:initMatchUI()
    XH.playerData:flushPlayerPosition()
    XH.lobby:getModule("Mail"):reqCheckNewMail()

    local bSupportFriendList = XH.areaData:isSupportFriendList()
    if bSupportFriendList then
        self.KWA_IM_LIST_PANEL:removeAllChildren()
        if XH.areaData:isSupportFriendsV2() then
            self._imListLayer = require("lobby.Modules.Im.NewIMView").new()
        else
            self._imListLayer = require("lobby.Modules.Im.View").new()
        end
        self.KWA_IM_LIST_PANEL:addChild(self._imListLayer)

        XH.SysTool.performWithDelayGlobal(function ()
            local reserveInfo = XH.lobby:getModule("Im"):getReserveInfo()
            if reserveInfo then
                XH.viewManager:openView("ImInviteView", nil, {info = reserveInfo})
                XH.lobby:getModule("Im"):clearReserveInfo()
            end
        end, 0.5)
    end

    -- 个推消息相关处理
    XH.lobby:getModule("Lobby"):dealWithGeTuiMsg()

    --创圈活动3.17
    XH.lobby:getModule("CreateTeaHouseAct"):onEventFulshView()

    XH.lobby:getModule("CrmNotice"):reqCrmNotice()

    XH.lobby:getModule("LuckyTurnTableAct"):reqActData()

    --请求封禁状态
    cc.UserDefault:getInstance():setBoolForKey("BAN_PLAYER_CAN_GAME".. XH.playerData:getNumberID(),false)
    XH.lobby:getModule("Lobby"):reqGetBanInfo()
end

--游戏第一次登陆成功的时候，该layer还没有初始化
function LobbyView:onEnterTransitionFinish()
    XH.sdkManager:reqDeviceId()
    self:initLayer()
    self.KWA_TOUCH_LAYER_PANEL:setSwallowTouches(false)
    self:updateData()

    self:setGoldSchedule()
    self:initAudio()
    self:initRightTopUI()
    self:initMainRightBottomUI()
    self:initGoldRightTopUI()
    self:initGoldRightBottomUI()
    self:initRightUI()
    self:initLeftUI()
    self:initBottomLeftUI()
    self:initGoldBottomLeftUI()
    self:initGoldSecondMenu()
    self:initMainSecondMenu()
    self:initAni()
    self:onEnterEventInit()
    self:retainPlists()
    --刷新一次cps渠道的界面
    self:updateCpsChannelUI()
    if self._curLobbyType == XH.LOBBY_TYPE.YUEJU then
        self:showYouXILobby()
    else
        self:showXIUXIANLobby()
    end
    if self._imgTeaHouseUpdate then
        self._imgTeaHouseUpdate:setVisible(false)
    end
    XH.goldConfigManager:resetGoldConfig()
    XH.lobby:getModule("Lobby"):reqWebIcons()
    self:updateLeisureEntrance()
    if XH.ChannelTool.checkIsECarChannel() then
        self.KWA_GOLD_TOP_MENU_BAR:setVisible(false)
    end
    
    self:showSexVoiceTip()
    --进入大厅主动请求一次y_m_n_link的接口
    if not self.isReqGetUri then
        self.isReqGetUri = true
        local targetPlatform = cc.Application:getInstance():getTargetPlatform()
        if cc.PLATFORM_OS_IPAD == targetPlatform or cc.PLATFORM_OS_IPHONE == targetPlatform then
            XH.sdkManager:xhCallOCFunc("XHlink", "xhlink_get_uri")
        elseif cc.PLATFORM_OS_ANDROID == targetPlatform then
            if XH.SysTool:GetBundleVersion() > "1.0.4" then
                XH.TipTool.showClipboardTip({msg = "是否允许使用剪切板，快速加入房间"})
            end
        end
    end
    -- 双扣渠道包增加金币场引导
    if XH.ChannelTool.checkIsDoubleKouGame() then
        local isShowGuide = cc.UserDefault:getInstance():getIntegerForKey("_KW_SHOW_GOLDGUIDEVIEW" .. XH.areaData:getLobbyID() .. XH.playerData:getNumberID(), 1)
        if isShowGuide == 0 then
            XH.SysTool.performWithDelayGlobal(function ()
                XH.viewManager:openView("GoldGuideView")
                cc.UserDefault:getInstance():setIntegerForKey("_KW_SHOW_GOLDGUIDEVIEW" .. XH.areaData:getLobbyID() .. XH.playerData:getNumberID(), 1)
            end,0.5)
        end
    end
    self:onEventFlushLimitFreeAct()
end

function LobbyView:showUpdateTip()
    XH.TipTool.showTip(
        {
            type = XH.TIP_LAYER_TYPE.OK,
            funcOK = function()
                XH.SysTool.downloadNewVersionApp()
            end
        },
        "新玩法上线！请更新至新版本体验！"
    )
end

function LobbyView:isNeedShowUpdateTip()
    -- 人人捕鱼提示更新条件
    if device.platform == "ios" and not XH.SysTool:CompareToBundleVersion("1.3.29") then
        return true
    end
    return false
end

function LobbyView:checkShowUpdateTip()
    if XH.loginData:isTodayFirstLogin() and self:isNeedShowUpdateTip() then
        self:showUpdateTip()
    end
    if device.platform == "ios" and self:isNeedShowUpdateTip() then
        self:setMenuIconVisible(LobbyConfig.ICON_ID.APP_UPDATE_TIP, true)
    end
end

function LobbyView:onPlayerRechargeFlag(event)
    if XH.playerData:isRechargePlayer() then
        self:setMenuIconVisible(LobbyConfig.ICON_ID.MEI_RI_CHOU_JIANG, false)
    end
end

function LobbyView:onReqWebIconsBack(event)
    self:initRightTopUI()
    self:initGoldRightTopUI()
    self:initRightUI()
    self:initGoldBottomLeftUI()
    if self._curLobbyType == XH.LOBBY_TYPE.YUEJU then
        self._mainTopMenuBar:setVisible(true)
        self._mainRightMenuBar:setVisible(true)
        self._goldTopMenuBar:setVisible(false)
        self._goldBottomLeftMenuBar:setVisible(false)
    else
        self._mainTopMenuBar:setVisible(false)
        self._mainRightMenuBar:setVisible(false)
        self._goldTopMenuBar:setVisible(true)
        self._goldBottomLeftMenuBar:setVisible(true)
    end
    if XH.lobby:getModule("ToponAct"):isOpen() then
        XH.lobby:getModule("ToponAct"):reqToponActInfo()
    end
    
    XH.lobby:getModule("Lobby"):checkOpenActivity()
        --请求折扣礼包
    XH.lobby:getModule("SaleGiftbag"):reqDiscountGiftBag()
    if XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_CUSSERVICE_TIP_SHOW, false) then
        self:removeMenuIconTipText(LobbyConfig.ICON_ID.KE_FU)
    end
    self:initTipImageButtonCallBack()

    XH.lobby:getModule("BackActivity"):reqBackActProtocol("lobby")

    XH.lobby:getModule("PrayAct"):reqPrayActInfo()
    XH.lobby:getModule("LaborDayGift"):getActInfo()
    XH.lobby:getModule("PopCommonLayer"):reqPopLayerInfo()
    -- 问卷系统
    if XH.lobby:getModule("PopComplain"):isReqComplainData() then
        XH.lobby:getModule("PopComplain"):reqComplainInfo()
    end

    --创圈活动3.17
    XH.lobby:getModule("CreateTeaHouseAct"):reqStatus()
    if XH.lobby:getModule("Promote"):getBaseInfo() == nil then
        XH.lobby:getModule("Promote"):reqSpreadInfo()
    end

    XH.lobby:getModule("LuckyTask"):reqActList()
    XH.lobby:getModule("LuckyTask"):reqTaskList()
    XH.lobby:getModule("Share"):reqLobbyShareInfo()
    XH.lobby:getModule("Article"):reqArticleListInfo()
    XH.lobby:getModule("YGiftBankruptcy"):initGoldActData()
    XH.lobby:getModule("AoYun"):isShowIcon()
    -- self:checkShowUpdateTip()
end

function LobbyView:adaptForLiuHai()
    local nodeListName = {"panel_head", "_KWA_LEFT_MENU_BAR"}

    local nodeList = {}
    for _,name in pairs(nodeListName) do
        local tmpNode = XH.UITool.seekNodeByName(self, name)
        nodeList[#nodeList + 1] = tmpNode
    end
    XH.UITool.adaptForLiuHai(nodeList)
end

function LobbyView:onAgentFlagChanged(event)
    self:refreshTransferUI()
end

-- 邀请码红点
function LobbyView:onBindDSBChanged(event)
    local lasttime = XH.userDefault:getValue(XH.userDefault.KEY_ID.LAST_TOUCH_INVATE)
    if XH.playerData:getIsBindDSB() or XH.SysTool.isToday(lasttime) then
        self:setMenuIconRedPointVisible(LobbyConfig.ICON_ID.YAO_QING,false)
    else
        self:setMenuIconRedPointVisible(LobbyConfig.ICON_ID.YAO_QING,true)
    end
end

-- 邮件红点
function LobbyView:refreshMailRed(event)
    local cnt = event.count or 0
    local awardCnt = event.awardCount or 0
    self:setMenuIconTipNewAwardVisible(LobbyConfig.ICON_ID.YOU_JIAN, awardCnt > 0)
    self:setMenuIconTipNewsVisible(LobbyConfig.ICON_ID.YOU_JIAN, awardCnt <= 0 and cnt > 0)
end

-- 客服红点
function LobbyView:refreshCurServiceRed(event)
    local cnt = event.msg or 0
    local showRed = cnt > 0 and true or false
    self:setMenuIconRedPointVisible(LobbyConfig.ICON_ID.KE_FU,showRed)
end

-- 刷新商城红点状态
function LobbyView:refreshShopIconRed(event)
    if event then
        self:setMenuIconRedPointVisible(LobbyConfig.ICON_ID.SHANG_CHENG, event.data or false)
    end
end

function LobbyView:initAni()
    -- BQ相关处理
    self:initLobbyMainBanquan()
    -- 更新远端动画
    self:updateGameCommonAni()
end

function LobbyView:initLobbyMainBanquan()
    local areaBqString = XH.areaData:getTemporaryBq() or ""
    if XH.configerManager:getIsBanQuan() and areaBqString ~= "" then
        self._TextBanQuan:setText("-" .. areaBqString)
        self._TextBanQuan:setVisible(true)
    else
        self._TextBanQuan:setVisible(false)
    end
end

function LobbyView:onShowVip(event)
    local isVip = event.isVip or false
    self:initVipAni(isVip)
end

function LobbyView:initLayer()
    self:onChangeBg()
end

function LobbyView:onItemUpdate()
    self:updateData()
    self:showShopChargeTip()
end

function LobbyView:onPositionChanged()
    self:showBackBoom()
end

function LobbyView:showBackBoom()
    
    local node 
    if XH.ChannelTool.checkIsCPSChannel() then
        node = XH.UITool.seekNodeByName(self, "_KW_IMG_BOX_ROOM_TITLE_CPS")
    else
        node = XH.UITool.seekNodeByName(self, "_KW_IMG_BOX_ROOM_TITLE")
    end
    if node then
        local position = XH.playerData:getPlayerPosition()
        cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/hall/Image/lobby.plist")
        if position.gameID ~= 0 then
            node:loadTexture("lobby_title_back_box.png", ccui.TextureResType.plistType)
        else
            node:loadTexture("lobby_title_create_box.png", ccui.TextureResType.plistType)
        end
        local info = XH.lobby:getModule("LimitFreeAct"):getActConfig()
        if info and not XH.lobby:getModule("LimitFreeAct"):isActEnd() then
            cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/Common/Image/LimitFreeAct/LimitFreeAct.plist")
            if position.gameID ~= 0 then
                node:loadTexture("LimitFreeAct_ny_fhfj.png", ccui.TextureResType.plistType)
                node:setContentSize(241, 76)
            else
                node:loadTexture("LimitFreeAct_ny_cjfj.png", ccui.TextureResType.plistType)
                node:setContentSize(237, 77)
            end
        end
    end
end

function LobbyView:onPhoneChanged()
    if XH.playerData:isBindPhone() then
        self:setMenuIconVisible(LobbyConfig.ICON_ID.BANG_DING_SHOU_JI, false)
    end
    XH.playerData:flushBindUserData()
    self:showPhoneBindQiPao()
end

function LobbyView:on_GUIDE_CREATE_ROOM(event)
    if event.msg and event.msg.isGuideState then
        XH.viewManager:openView("CreateBoxRoomView",nil,{isGuideState = true})
    end
end

function LobbyView:onClickCreateRoom(send,eventType)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.fj240112)
    self:on_KWA_BTND_CREATE_BACK_BOX_ROOM(send,eventType)
end

function LobbyView:on_KWA_BTND_CREATE_BACK_BOX_ROOM(send,eventType)
    --if XH.playerData:checkNewRealNameVisitor() ~= 0 then return end--游客登陆
    local position = XH.playerData:getPlayerPosition()
    if position.gameID == 0 then
        if cc.UserDefault:getInstance():getBoolForKey("BAN_PLAYER_CAN_GAME".. XH.playerData:getNumberID()) then
            XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK,            
            }, "您已被封禁对局，无法进入房间，如有疑问请联系客服。")
        else
            XH.viewManager:openView("CreateBoxRoomView")
        end
        XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_CREATE_BOX_ROOM)
    else
        XH.TipTool.showLoading()
        local func = function ()
		    if tonumber(position.teaID) ~= 0 and not XH.lobby:getModule("Im"):checkIsGroup(position.teaID) then
			    --事件分发,请求进入比赛场
                XH.teaHouseManager:joinTeaHouse(position.teaID, position.roomID)
		    else
                if position.teaNumber then
                    XH.lobby:getModule("Im"):getIMData():setEnterTeaHouseID(position.teaNumber)
                end
                local func = function( )
                    local param = {}
                    param.appID = position.gameAppID
                    param.gameID = position.gameID
                    param.roomMode = XH.ROOM_MODE.BOXROOM
                    param.roomID = position.roomID
                    param.srsGroupID = position.srsGroupID
                    param.sRoomMode = position.roomMode
                    XH.roomManager:gameStart(param)
                end

                if XH.lobby:getModule("Lobby"):isNeedHotUpdata(position.gameID) then
                    lobby.gameHotUpdateSuccessCallBack = func
                else
                    func()
                end
		    end
            XH.TipTool.hideLoading()
        end
        self:performWithDelay(func, 0.2)
    end
end

function LobbyView:on_KWA_BTND_HEAD(send,eventType)
    XH.playerData:flushPlayerDrop()
    XH.playerData:flushGoldCoin()
    XH.playerData:flushBindUserData()
    XH.viewManager:openView("PersonalCenterView")
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_BTN_PERSON_CENTER)
end

function LobbyView:onClickJoinRoom(send, eventType)
    self:on_KWA_BTND_JOIN_ROOM(send, eventType)
end

function LobbyView:on_KWA_BTND_JOIN_ROOM(send,eventType)
    if XH.playerData:checkNewRealName() ~= 0 then return end--实名限制
    if XH.playerData:checkNewRealNameVisitor() ~= 0 then return end--游客登陆
    cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/hall/Image/joinboxroom.plist")
    local position = XH.playerData:getPlayerPosition()
    if position.gameID ~= 0 then
        self:on_KWA_BTND_CREATE_BACK_BOX_ROOM(self._createBackBoxRoomBtn, eventType)
    else
        XH.viewManager:openView("JoinBoxRoomView")
    end
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_JOIN_BOX_ROOM)
    XH.TipTool.showClipboardTip({always = true, msg = "是否允许使用剪切板，快速加入房间"})
end

function LobbyView:on_KWA_BTND_TEA_HOUSE(send,eventType)
    if XH.playerData:checkNewRealName() ~= 0 then return end--实名限制
    if XH.playerData:checkNewRealNameVisitor() ~= 0 then return end--游客登陆
    XH.teaHouseManager:openTeaHouseList()
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_JOIN_TEA_HOUSE)
end

function LobbyView:on_KWA_BTND_ADD_GOLD(send,eventType)
    XH.viewManager:openView("ShopView", nil, {productType = ShopConfig.SpecialTag.GOLD})
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_ADD_GOLD_BTN,{Source = self._curLobbyType})
end

function LobbyView:on_KWA_BTND_ACTIVITY_QIANDAO(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
end

function LobbyView:on_KWA_TOUCH_LAYER_PANEL(send, eventType)
    self._mainScondMenu:setVisible(false)
    self._goldSecondMenu:setVisible(false)

    --好友系统页面
    XH.lobby:getModule("Im"):dispatchEvent({name = XH.lobby:getModule("Im").EVENT_HIDE_MESSAGELAYER})
    XH.lobby:getModule("Im"):dispatchEvent({name = XH.lobby:getModule("Im").EVENT_HIDE_NOTIFY_INVITE})
end

function LobbyView:showYouXILobby()
    XH.lobby:getModule("Lobby"):setIsOpenGoldView(false)
    self._curLobbyType = XH.LOBBY_TYPE.YUEJU
    self:onChangeBg()
    
    if XH.ChannelTool.judgeIsChannel() then 
        self:setAreaInfoVisible(false)
    else
        self:setAreaInfoVisible(true)
    end 

    self.KWA_XIUXIANDATING_TITLE:setVisible(false)
    
    self._mainBottomRightMenuBar:setVisible(true)
    self._mainScondMenu:setVisible(false)
    self._mainTopMenuBar:setVisible(true)
    self._mainBottomLeftMenuBar:setVisible(true)
    self._mainRightMenuBar:setVisible(true)
    self._mainLeftMenuBar:setVisible(true)
    self._goldTopMenuBar:setVisible(false)
    self._goldMenuBar:setVisible(false)
    self._goldSecondMenu:setVisible(false)
    self._goldBottomLeftMenuBar:setVisible(false)

    self:showLobbyHeadSR(false)
    self:changeLobbyUI(self.KWA_RIGHT_POS, self.KWA_NORMAL_LOBBY_PANEL, true)
    self:showBreakGift()

    -- XH.lobby:getModule("Im"):dispatchEvent({name = XH.lobby:getModule("Im").EVENT_SHOW_LIST_ENTRANCE})
    XH.lobby:getModule("PopSystem"):startShowPopViews("LOBBY")

    self:showNewPlayTypeUI(self:isShowNewPlayTypeUI())
    self:UpdatePromoteState()
end

function LobbyView:UpdatePromoteState()
    if PromoteConfig[XH.areaData:getLobbyID()] == nil then
        self._imgBoxRoomFree:setVisible(false)
        return
    end
    if PromoteConfig[XH.areaData:getLobbyID()].isFree == false then
        self._imgBoxRoomFree:setVisible(false)
        return
    end
    self._imgBoxRoomFree:setVisible(XH.lobby:getModule("Promote"):isOnlineAct())
end

function LobbyView:isShowNewPlayTypeUI()
    if XH.areaData:getLobbyID() == XH.LOBBY_ID.LISHUI then
        return false
    end
    return GameNewTypeConfiger:getIsNewPlayTypeArea(XH.areaData:getLobbyID())
end

function LobbyView:showXIUXIANLobby()
    XH.lobby:getModule("Lobby"):setIsOpenGoldView(true)
    self._curLobbyType = XH.LOBBY_TYPE.XIUXIAN
    self:onChangeBg()
    self:onUpdateBankruptcyStatus()
    self:onUpdateGiftDefeatStatus()
    self:setAreaInfoVisible(false)
    self.KWA_XIUXIANDATING_TITLE:setVisible(true)

    self._mainBottomRightMenuBar:setVisible(false)
    self._mainScondMenu:setVisible(false)
    self._mainTopMenuBar:setVisible(false)
    self._mainBottomLeftMenuBar:setVisible(false)
    self._mainRightMenuBar:setVisible(false)
    self._mainLeftMenuBar:setVisible(false)
    self._goldTopMenuBar:setVisible(true)
    self._goldMenuBar:setVisible(true)
    self._goldSecondMenu:setVisible(false)
    self._goldBottomLeftMenuBar:setVisible(true)
    self:showLobbyHeadSR(true)
    self:changeLobbyUI(self.KWA_LEFT_POS, self.KWA_NORMAL_LOBBY_PANEL,false)
    local tmpGoldTopMenus = self._goldTopMenuBar._curShowMenus
    if tmpGoldTopMenus then
        for _,v in ipairs(tmpGoldTopMenus) do
            if UNIONGAME_NAME[v.IconID] then
                XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.BUYUWANG_GOLD_SHOW,{label = UNIONGAME_NAME[v.IconID]})
            end
        end
    end
    XH.lobby:getModule("Im"):dispatchEvent({name = XH.lobby:getModule("Im").EVENT_HIDE_LIST_ENTRANCE})
    XH.lobby:getModule("PopSystem"):startShowPopViews("GOLD")
    XH.lobby:getModule("YGiftBankruptcy"):initGoldActData()
end

function LobbyView:updateData()
     local nickname = XH.playerData:getNickName()
     local sex = XH.playerData:getSex()
     local id = XH.playerData:getNumberID()
     local roomCard = XH.playerData:getRemodelRoomCard()
     if string.len(nickname) > 12 then
        local validBytes = XH.StringTool.getMinimumValidBytesUTF8(nickname, 12)
        nickname = string.sub(nickname,1,validBytes) .. "..."
     end
     self.KWA_NICKNAME:setText(nickname)
     self.KWA_USER_ID:setText("序号:" .. id)
     self.KWA_LABEL_HEAD_ROOMCARD:setText(roomCard)

     --钻石数据
     local diamondCnt = XH.playerData:getDiamnd()
     self.KWA_LABEL_HEAD_DIAMOND:setText(diamondCnt)

     --设置金币数据
     self:updateSR()

     local head = XH.playerData:getHead()
     if head == 0 then --default head
        local headImageKey
         if sex == 0 then --girl
            headImageKey = "_0002_mrtx_w.png"
         else   --boy
            headImageKey = "_0002_mrtx_m.png"
         end
         XH.UITool.setPlistTextureOnNode(self.KWA_IMAGE_HEAD, headImageKey)
     end

	local url = XH.playerData:getWeChatURL()
    self:updateImg(url)
end

function LobbyView:updateImg(url)
    if not url or url == "" then
        return 
    end

    if not self._headImg then
        local size = self.KWA_IMAGE_HEAD:getContentSize()
        self._headImg = XH.RemoteImage.new()
            :setTouchEnabled(false)
            :setPosition(size.width/2, size.height/2)
            :ignoreContentAdaptWithSize(false)
            :setContentSize(size.width, size.height)
            :addTo(self.KWA_IMAGE_HEAD)
    end

    self._headImg:setUrl(url, true)
end

function LobbyView:onShowBackActIcon(event)
    local data = event.data
    if data.actStartTime and data.actStartTime > 0 then
        self:setMenuIconVisible(LobbyConfig.ICON_ID.HUI_GUI_LI_BAO, true)
        XH.lobby:getModule("TabsActivity"):setShowBackAct(true)
    else
        self:setMenuIconVisible(LobbyConfig.ICON_ID.HUI_GUI_LI_BAO, false)
        XH.lobby:getModule("TabsActivity"):setShowBackAct(false)
    end
end

function LobbyView:onShowToponActIcon(event)
    local data = event.data
    for _, info in ipairs(data) do
        if info.icon and info.isVisibled then
            self:setMenuIconVisible(info.icon, info.isVisibled)
        end
    end
end

function LobbyView:onLoginSuccess()
    self:onChangeBg()
    self:onShowInvateBtn()
end

function LobbyView:getBackground()
    local lobbyID = XH.areaData:getLobbyID()
    local areaBgString = XH.areaData:getAreaBackground() or lobbyID
    if self._curLobbyType == XH.LOBBY_TYPE.XIUXIAN then
        areaBgString = "gold_lobby"
    end
    local isFileEXist = cc.FileUtils:getInstance():isFileExist(lobbyBgPath .. areaBgString .. ".png")
    if not isFileEXist then
        areaBgString = "default"
    end
    return areaBgString
end

function LobbyView:onChangeBg()
    if not self._backgroundImg then
        return
    end
    local areaBgString = self:getBackground()
    self._backgroundImg:loadTexture(lobbyBgPath .. areaBgString .. ".png", ccui.TextureResType.localType)
    if self.areaImage and self.areaText then
        local areaString = XH.areaData:getAreaName()
        self.areaText:setString(areaString)
        self:setAreaInfoVisible(true)
    end
    local info = XH.lobby:getModule("LimitFreeAct"):getActConfig()
    if info and not XH.lobby:getModule("LimitFreeAct"):isActEnd() then
        self._backgroundImg:loadTexture("cocosStudio/Common/Image/LimitFreeAct/LimitFreeNewYear.png", ccui.TextureResType.localType)
    end
end

function LobbyView:setAreaInfoVisible(visible)
    self.areaText:setVisible(visible)
    self.areaImage:setVisible(visible)
    self.KWA_AREA_PANEL:setVisible(visible)
end

function LobbyView:changeLobbyUI(targetPosNode, moveNode, bMoveIn)
    if targetPosNode == nil or moveNode == nil then
        return
    end
    moveNode:stopAllActions()
    local actionTime = 0.25
    local targetPosX = targetPosNode:getPositionX();
    local targetPosY = targetPosNode:getPositionY()

    local action
    if bMoveIn then
        action = cc.FadeIn:create(actionTime)
    else
        action = cc.FadeOut:create(actionTime)
    end
    
    moveNode:runAction(cc.Spawn:create(action,cc.MoveTo:create(actionTime, cc.p(targetPosX, targetPosY))))

    if not self._goldLayer then
        self._goldLayer = require("lobby.Modules.Gold.View").new();
        self._goldLayer:setVisible(false)
        self.KWA_XIUXIAN_LOBBY_PANEL:addChild(self._goldLayer);
    end

    if not bMoveIn then
        self._goldLayer:setVisible(true)
    end

    if self._goldLayer then
        self._goldLayer:showGoldLayer(not bMoveIn)
    end

    if XH.areaData:isRemodelArea() then
        self._imgTeaRemodel:setVisible(true)
    end
end

function LobbyView:showLobbyHeadSR(bShow)
    XH.playerData:flushPlayerDrop()
    self:setGoldNumberVisible2(bShow)
    self:setGoldNumberVisible(not bShow)
    self:setRoomCardNumberVisible(not bShow)
    self:setDiamondNumberVisible(not bShow)
    self:onUpdateSxvipStatus()
    self:updateSR()
end

function LobbyView:setGoldNumberVisible2(visible)
    self.KWA_GOLD_NUM_BG_2:setVisible(visible)
    self.KWA_LABEL_HEAD_GOLD_2:setVisible(visible)
end

function LobbyView:setGoldNumberVisible(visible)
    self.KWA_GOLD_NUM_BG:setVisible(visible)
    self.KWA_LABEL_HEAD_GOLD:setVisible(visible)
end

function LobbyView:setDiamondNumberVisible(visible)
    self.KWA_DIAMOND_BG:setVisible(visible)
    self.KWA_LABEL_HEAD_DIAMOND:setVisible(visible)
end

function LobbyView:setRoomCardNumberVisible(visible)
    self.KWA_ROOM_CARD_BG:setVisible(visible)
    self.KWA_LABEL_HEAD_ROOMCARD:setVisible(visible)
end

function LobbyView:onTeaHouseUpdateStart(event)
    local data = event.msg.data
    if self._imgTeaHouseUpdate then
        self._imgTeaHouseUpdate:setVisible(data.isStart)
    end
end

function LobbyView:onTeaHouseUpdateChanged(event)
    local data = event.msg.data
    if self._lbTeaHouseUpdate then
        self._lbTeaHouseUpdate:setPercent(data.percent)
    end
    if self._textTeaHouseUpdate then
        self._textTeaHouseUpdate:setString(string.format("加载中(%.0d%%)...", data.percent))
    end
end

function LobbyView:on_KWA_BTN_COPY(send,eventType)
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_COPY_NUMBER)
    local strId = self.KWA_USER_ID:getString()
    local copyStr = strId
    local stringArr = string.split(strId,":")
    if stringArr and #stringArr == 2 then
        copyStr = stringArr[2]
    end
    XH.SysTool.copyString(copyStr)
    XH.TipTool.showToast("复制成功!")

end

function LobbyView:onCheckBindUser()
    if XH.playerData:isBindPhone() then
        self:setMenuIconVisible(LobbyConfig.ICON_ID.BANG_DING_SHOU_JI, false)
    end
end

function LobbyView:onSRChanged()
    self:updateSR()
    self:showPhoneBindQiPao()
    self:showShopChargeTip()
end

function LobbyView:updateSR()
    local playerSR = XH.playerData:getSR() or 0
    self.KWA_LABEL_HEAD_GOLD:setText(playerSR)
    self.KWA_LABEL_HEAD_GOLD_2:setText(playerSR)
end

function LobbyView:onEventChangeHeadFrame(event)
    if not event or not event.data then
        self._headFrameImg:setVisible(true)
        self._headFrameImgTemp:setVisible(false)
        return
    end
    self._headFrameImgTemp:removeChildByName(HEAD_FRAME_NAME)
    local size = self._headFrameImgTemp:getContentSize()
    if not ShowPropFunction.createHeadFrameAni(self._headFrameImgTemp,event.data.propid,0.6,HEAD_FRAME_NAME) then
        local headImgFrame = XH.RemoteImage.new()
        headImgFrame:ignoreContentAdaptWithSize(false)
        headImgFrame:addTo(self._headFrameImgTemp)
        headImgFrame:setPosition(size.width / 2 , size.height / 2 + 5)
        headImgFrame:setContentSize(size.width + 30, size.height + 30)
        headImgFrame:setUrl(event.data.url)
        headImgFrame:setName(HEAD_FRAME_NAME)
    end
    self._headFrameImg:setVisible(false)
    self._headFrameImgTemp:setVisible(true)
end

function LobbyView:onEventSetBagBtnText(event)
    if event.data then
        self:setMenuIconTipText(LobbyConfig.ICON_ID.BEI_BAO, event.data)
    end
end

--by ruzibeil
--刷新按钮显示
function LobbyView:refreshTransferUI()
    local isAgentShow = XH.playerData:getPlayerIsAgent() and XH.areaData:isInTransferActivity()
    self:setMenuIconVisible(LobbyConfig.ICON_ID.GENG_XIN_FU_LI, isAgentShow)
    local function checkNeedTransferPopLayer()
        local curDate = os.date("%Y%m%d")
        local lasteDate = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_DATA_TRANSFER_DATE, "")
        if lasteDate ~= curDate then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_DATA_TRANSFER_DATE, curDate)
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_DATA_TRANSFER_CNT, 0)
        end
        local todayCnt = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_DATA_TRANSFER_CNT, 0)
        if todayCnt < 3  then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_DATA_TRANSFER_CNT, todayCnt + 1)
            return true
        end
        return false
    end
    if isAgentShow and checkNeedTransferPopLayer() then
        XH.viewManager:addCacheView("AgentTransferView")
    end
end

-------------------------------金币场快捷入口--------------------------------

function LobbyView:onRefreshGoldUI()
    self:initGoldQuickList()
end

local gameView = nil
function LobbyView:initGoldQuickList()
    if not XH.ChannelTool.checkIsDoubleKouGame() and (XH.ChannelTool.checkIsCPSChannel() or XH.ChannelTool.checkIsWaishengChannel()) then
        self.KW_MINI_GAME_PANEL_CPS:removeAllChildren()
        gameView = require("lobby.Modules.Lobby.GameListView").new()
        gameView:addItemClickListener(self, self.onMenuBarItemClicked)
        self.KW_MINI_GAME_PANEL_CPS:addChild(gameView)
    else
        self.KW_MINI_GAME_PANEL:removeAllChildren()
        gameView = require("lobby.Modules.Lobby.GameListView").new()
        gameView:addItemClickListener(self, self.onMenuBarItemClicked)
        self.KW_MINI_GAME_PANEL:addChild(gameView)
    end
end

function LobbyView:initRightTopUI()
    cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/hall/Image/lobby.plist")
    
    local param = {
        config = XH.lobby:getModule("Lobby"):getLobbyIconsConfig(LobbyConfig.ICON_POS_TYPE.LOBBY_LONG),
        size = cc.size(900, 162),
        adjustSize = cc.size(80, 0),
        bFixDis = true,
        fixDis = 150,
        tipType = LobbyConfig.ICON_TIP_TEXT_TYPE.TOP
    }

    if self._mainTopMenuBar then
        self._mainTopMenuBar:removeFromParent()
    end
    -- if not self._mainTopMenuBar then
        local topMenuBarRoot = self.KWA_TOP_MENU_BAR
        local topMenuBar = HorizontalMenuBar.new(param)
        topMenuBar:setAnchorPoint(cc.p(1, 1))
        topMenuBarRoot:addChild(topMenuBar)
        topMenuBar:addItemClickListener(self, self.onMenuBarItemClicked)
        self._mainTopMenuBar = topMenuBar
    -- end
    -- 推广活动刷新
    XH.lobby:getModule("Promote"):flushPromoteActIcon()
end

function LobbyView:initMainRightBottomUI()
    local param = {
        config = XH.configerManager:getLobbyMenuBarConfig(LobbyConfig.MENUBAR_ID.MAIN_BOTTOM_RIGHT) or {},
        size = cc.size(display.width, 100),
        adjustSize = cc.size(60, -3),
        bg = "lobby_caidanlanxia_bg.png",
        bFull = true,
        line = "img_line.png",
        bAddForward = true,
        iconSize = cc.size(150, 100),
        tipType = LobbyConfig.ICON_TIP_TEXT_TYPE.BOTTOM
    }
 
    if not self._mainBottomRightMenuBar then
        local menuBar = HorizontalMenuBar.new(param)
        menuBar:setAnchorPoint(cc.p(1, 0))
        menuBar:setVisible(false)
        self.KWA_MENU_BAR:addChild(menuBar)
        menuBar:addItemClickListener(self, self.onMenuBarItemClicked)
        self._mainBottomRightMenuBar = menuBar
    end 
end

function LobbyView:initGoldRightTopUI()
    cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/hall/Image/lobby.plist")
    
    local param = {
        config = XH.lobby:getModule("Lobby"):getLobbyIconsConfig(LobbyConfig.ICON_POS_TYPE.GOLD_LONG),
        size = cc.size(900, 162),
        adjustSize = cc.size(80, 0),
        bFixDis = true,
        fixDis = 150,
        tipType = LobbyConfig.ICON_TIP_TEXT_TYPE.TOP
    }

    if self._goldTopMenuBar then
        self._goldTopMenuBar:removeFromParent()
    end
    -- if not self._goldTopMenuBar then
        local goldTopMenuBarRoot = self.KWA_GOLD_TOP_MENU_BAR
        local goldTopMenuBar = HorizontalMenuBar.new(param)
        goldTopMenuBar:setAnchorPoint(cc.p(1, 1))
        goldTopMenuBarRoot:addChild(goldTopMenuBar)
        goldTopMenuBar:addItemClickListener(self, self.onMenuBarItemClicked)
        self._goldTopMenuBar = goldTopMenuBar
        
    -- end
end

function LobbyView:initGoldRightBottomUI()
    local param = {
        config = XH.configerManager:getLobbyMenuBarConfig(LobbyConfig.MENUBAR_ID.GOLD_BOTTOM_RIGHT) or {},
        size = cc.size(1000, 103),
        adjustSize = cc.size(60, 0),
        bg = "lobby_menu_bg.png",
        bFull = true,
        line = "img_line.png",
        capInsets = cc.rect(50, 15, 33, 33),
        iconSize = cc.size(150, 100),
        tipType = LobbyConfig.ICON_TIP_TEXT_TYPE.BOTTOM
    }
 
    if not self._goldMenuBar then
        param.maxDis = 500
        local menuBar = HorizontalMenuBar.new(param)
        menuBar:setAnchorPoint(cc.p(1, 0))
        menuBar:setVisible(false)
        self.KWA_GOLD_MENU_BAR:addChild(menuBar)
        menuBar:addItemClickListener(self, self.onMenuBarItemClicked)
        self._goldMenuBar = menuBar
    end 
end

function LobbyView:initBottomLeftUI()
    local param = {
        config = XH.configerManager:getLobbyMenuBarConfig(LobbyConfig.MENUBAR_ID.MAIN_BOTTOM_LEFT) or {},
        size = cc.size(300, 100),
        bFixDis = true,
        fixDis = 150,
        tipType = LobbyConfig.ICON_TIP_TEXT_TYPE.BOTTOM,
        bAddForward = true
    }
    if not self._mainBottomLeftMenuBar then
        local menuBar = HorizontalMenuBar.new(param)
        menuBar:setAnchorPoint(cc.p(0, 0.5))
        self._bottomLeftMenuBarPos:addChild(menuBar)
        menuBar:addItemClickListener(self, self.onMenuBarItemClicked)
        self._mainBottomLeftMenuBar = menuBar
    end
end

function LobbyView:initGoldBottomLeftUI()
    local param = {
        config = XH.lobby:getModule("Lobby"):getLobbyIconsConfig(LobbyConfig.ICON_POS_TYPE.GOLD_SHORT),
        size = cc.size(300, 100),
        bFixDis = true,
        fixDis = 150,
        tipType = LobbyConfig.ICON_TIP_TEXT_TYPE.BOTTOM,
        bAddForward = true
    }
    if self._goldBottomLeftMenuBar then
        self._goldBottomLeftMenuBar:removeFromParent()
    end
    -- if not self._goldBottomLeftMenuBar then
        local menuBar = HorizontalMenuBar.new(param)
        menuBar:setAnchorPoint(cc.p(0, 0.5))
        self._bottomLeftMenuBarPos:addChild(menuBar)
        menuBar:addItemClickListener(self, self.onMenuBarItemClicked)
        self._goldBottomLeftMenuBar = menuBar
    -- end
end

function LobbyView:initGoldSecondMenu()
    local menuBarRoot = self._goldMenuBar:getMenuByID(LobbyConfig.ICON_ID.GENG_DUO)
    if not menuBarRoot then
        return
    end
    
    local param = {
        config = XH.configerManager:getLobbyMenuBarConfig(LobbyConfig.MENUBAR_ID.MAIN_MORE) or {},
        size = DEFAULT_SECOND_SCENE_SIZE,
        adjustSize = cc.size(40, 10),
        bg = "lobby_more_qipao.png",
        bSmallIconBar = true,
        bFull = true,
        iconSize = cc.size(60, 100),
        capInsets = cc.rect(10, 75, 33, 33),
    }

    local menuBar = HorizontalMenuBar.new(param)  
    local worldPos = self._goldMenuBar:convertToWorldSpace(cc.p(menuBarRoot:getPosition()))
    menuBar:setAnchorPoint(cc.p(1, 0))
    menuBar:setPosition(cc.p(worldPos.x + 30, worldPos.y + 60))
    menuBar:addItemClickListener(self, self.onMenuBarItemClicked)
    self:addChild(menuBar)
    self._goldSecondMenu = menuBar
end

function LobbyView:changeGoldSecondMenuVisible()
    if not self._goldSecondMenu then
        return
    end
    local isShow = self._goldSecondMenu:isVisible()
    self._goldSecondMenu:setVisible(not isShow)
end

function LobbyView:initMainSecondMenu()
    local menuBarRoot = self._mainBottomRightMenuBar:getMenuByID(LobbyConfig.ICON_ID.GENG_DUO)
    if not menuBarRoot then
        return
    end
    
    local param = {
        config = XH.configerManager:getLobbyMenuBarConfig(LobbyConfig.MENUBAR_ID.MAIN_MORE) or {},
        size = DEFAULT_SECOND_SCENE_SIZE,
        adjustSize = cc.size(40, 10),
        bg = "lobby_more_qipao.png",
        bSmallIconBar = true,
        bFull = true,
        iconSize = cc.size(60, 100),
        capInsets = cc.rect(75, 10, 15, 15),
    }

    local menuBar = HorizontalMenuBar.new(param)  
    local worldPos = self._mainBottomRightMenuBar:convertToWorldSpace(cc.p(menuBarRoot:getPosition()))
    menuBar:setAnchorPoint(cc.p(0, 0))
    menuBar:setPosition(cc.p(worldPos.x - 30, worldPos.y + 60))
    menuBar:addItemClickListener(self, self.onMenuBarItemClicked)
    menuBar:setVisible(false)
    self:addChild(menuBar)
    self._mainScondMenu = menuBar
end
 
function LobbyView:changeMainSecondMenuVisible()
    if not self._mainScondMenu then
        return
    end
    local isShow = self._mainScondMenu:isVisible()
    self._mainScondMenu:setVisible(not isShow)
end

function LobbyView:initRightUI()
    local param = {
        config = XH.lobby:getModule("Lobby"):getLobbyIconsConfig(LobbyConfig.ICON_POS_TYPE.LOBBY_SHORT),
        size = cc.size(105, 700),
        bFixDis = true,
        fixDis = 150,
        tipType = LobbyConfig.ICON_TIP_TEXT_TYPE.RIGHT
    }
    if self._mainRightMenuBar then
        self._mainRightMenuBar:removeFromParent()
    end
    -- if not self._mainRightMenuBar then
        local menuBar = VerticalMenuBar.new(param)
        menuBar:setAnchorPoint(cc.p(0.5, 1))
        self._rightMenuBarPos:addChild(menuBar)
        menuBar:addItemClickListener(self, self.onMenuBarItemClicked)
        self._mainRightMenuBar = menuBar
        for k, v in pairs(self._mainRightMenuBar._allMenuIcons) do
            if v._info and v._info.iconID == LobbyConfig.ICON_ID.GOLD_NEW_DEFEAT or v._info.iconID == LobbyConfig.ICON_ID.GOLD_NEW_BANKRUPT then 
                v:setVisible(false)
            end
        end
        self._mainRightMenuBar:updateIconsPosition()

    -- end
end

function LobbyView:initLeftUI()
    local param = {
        config = XH.configerManager:getLobbyMenuBarConfig(LobbyConfig.MENUBAR_ID.MAIN_LEFT) or {},
        size = cc.size(105, 700),
        bFixDis = true,
        fixDis = 150,
    }
    if not self._mainLeftMenuBar then
        local menuBar = VerticalMenuBar.new(param)
        menuBar:setAnchorPoint(cc.p(0.5, 1))
        self._leftMenuBarPos:addChild(menuBar)
        menuBar:addItemClickListener(self, self.onMenuBarItemClicked)
        self._mainLeftMenuBar = menuBar
    end
end

function LobbyView:getAllMenuBars()
    local bars = {}
    table.insert(bars, self._mainTopMenuBar)
    table.insert(bars, self._mainBottomRightMenuBar)
    table.insert(bars, self._mainBottomLeftMenuBar)
    table.insert(bars, self._mainRightMenuBar)
    table.insert(bars, self._mainLeftMenuBar)
    table.insert(bars, self._mainScondMenu)
    table.insert(bars, self._goldTopMenuBar)
    table.insert(bars, self._goldMenuBar)
    table.insert(bars, self._goldSecondMenu)
    table.insert(bars, self._goldBottomLeftMenuBar)
    return bars
end

function LobbyView:setMenuIconVisible(iconID, bShow)
    for _, menuBar in pairs(self:getAllMenuBars()) do
        if bShow then
            menuBar:recoveryMenu(iconID)
        else
            menuBar:removeMenuByID(iconID)
        end
    end
end

function LobbyView:setMenuIconRedPointVisible(iconID, bShow)
    for _, menuBar in pairs(self:getAllMenuBars()) do
        menuBar:setRedPointVisible(iconID, bShow)
    end
end

function LobbyView:setMenuIconTipNewsVisible(iconID, bShow)
    for _, menuBar in pairs(self:getAllMenuBars()) do
        menuBar:setTipNewsVisible(iconID, bShow)
    end
end

function LobbyView:setMenuIconTipNewAwardVisible(iconID, bShow)
    for _, menuBar in pairs(self:getAllMenuBars()) do
        menuBar:setTipNewAwardVisible(iconID, bShow)
    end
end

function LobbyView:setMenuIconExtendAnimVisible(iconID, bShow)
    for _, menuBar in pairs(self:getAllMenuBars()) do
        menuBar:setExtendAnimVisible(iconID, bShow)
    end
end

function LobbyView:setMenuIconTipText(iconID, tipText)
    for _, menuBar in pairs(self:getAllMenuBars()) do
        menuBar:setIconTipText(iconID, tipText)
    end
end

function LobbyView:removeMenuIconTipText(iconID)
    for _, menuBar in pairs(self:getAllMenuBars()) do
        menuBar:removeTipText(iconID)
    end
end

function LobbyView:setTipTextImgVisible(iconID, bShow)
    for _, menuBar in pairs(self:getAllMenuBars()) do
        menuBar:setTipTextImgVisible(iconID, bShow)
    end
end

function LobbyView:onMenuBarItemClicked(event)
    local send = event.send
    local eventType = event.eventType
    local bind = event.bind
    local touchID = tonumber(send:getName())
    local dealFuncTab = {
        [LobbyConfig.ICON_ID.SHANG_CHENG] = function() bind:on_KWA_BTND_SHOP(send, eventType) end,
        [LobbyConfig.ICON_ID.HUO_DONG] = function() bind:on_KWA_BTND_EVENT(send, eventType) end,
        [LobbyConfig.ICON_ID.GUI_ZE] = function() bind:on_KWA_BTND_HELP(send, eventType) end,
        [LobbyConfig.ICON_ID.GONG_GAO] = function() bind:on_KWA_BTND_POST(send, eventType) end,
        [LobbyConfig.ICON_ID.ZHAN_JI] = function() bind:on_KWA_BTND_RESULT(send, eventType) end,
        [LobbyConfig.ICON_ID.SHE_ZHI] = function() bind:on_KWA_BTND_SETTING(send, eventType) end,
        [LobbyConfig.ICON_ID.GENG_DUO] = function() bind:on_KWA_BTND_MORE(send, eventType) end,
        [LobbyConfig.ICON_ID.YAO_QING] = function() bind:on_KWA_BTND_INVATE(send, eventType) end,
        [LobbyConfig.ICON_ID.JIAN_KANG_XU_ZHI] = function() bind:on_KWA_BTND_HEALTH_NOTE(send, eventType) end,
        [LobbyConfig.ICON_ID.FANG_DB] = function() bind:on_KWA_BTND_ANTI_JAMBEL(send, eventType) end,
        [LobbyConfig.ICON_ID.BANG_DING_SHOU_JI] = function() bind:on_KWA_BTND_BIND_PHONE(send, eventType) end,
        [LobbyConfig.ICON_ID.DUI_HUAN] = function() bind:on_KWA_BTND_LIQUAN(send, eventType) end,
        [LobbyConfig.ICON_ID.FEN_XAING] = function() bind:on_KWA_BTND_SHARE(send, eventType) end,
        [LobbyConfig.ICON_ID.YOU_JIAN] = function() bind:on_KWA_BTND_MAIL(send, eventType) end,
        [LobbyConfig.ICON_ID.KE_FU] = function() bind:on_KWA_BTND_SERVICE(send, eventType) end,
        [LobbyConfig.ICON_ID.BEI_BAO] = function() bind:on_KWA_BTND_BAG(send, eventType) end,
        [LobbyConfig.ICON_ID.ZHE_JIANG_XIN_WEN] = function() bind:onTouchEventZheJiangNews(send, eventType) end,
        [LobbyConfig.ICON_ID.TE_HUI_SHANG_CHENG] = function() bind:onTouchEventTeHuiShangCheng(send, eventType) end,
        [LobbyConfig.ICON_ID.GENG_XIN_FU_LI] = function() bind:onTouchEventTransferActivity(send, eventType) end,
        [LobbyConfig.ICON_ID.HUI_GUI_LI_BAO] = function() bind:onTouchEventBackAct(send, eventType) end,
        [LobbyConfig.ICON_ID.MEI_RI_CHOU_JIANG] = function() bind:onTouchEventDailyLuckyAct(send, eventType) end,
        [LobbyConfig.ICON_ID.XIAN_SHI_YOU_HUI] = function() bind:onTouchEventBreakGift(send, eventType) end, 
        [LobbyConfig.ICON_ID.ZHE_KOU_LI_BAO] = function() bind:onTouchEventShowDiscountLayer(send, eventType) end, 
        [LobbyConfig.ICON_ID.QIAN_PAO_BU_YU] = function() bind:onTouchEventH5BuYu(send, eventType) end,
        [LobbyConfig.ICON_ID.JUE_ZHAN_SHA_YI] = function() bind:onTouchEventH5Legend(send, eventType) end, 
        [LobbyConfig.ICON_ID.YI_DAO_CHUAN_SHI] = function() bind:onTouchEventH5Knifelegend(send, eventType) end,
        [LobbyConfig.ICON_ID.SHARE_HMQ_ACT]  = function () bind:onTouchEventShareHmqAct(send, eventType) end,
        [LobbyConfig.ICON_ID.LUCKY_TURN_TABLE]  = function () bind:onTouchEventLuckTurnTableAct(send, eventType) end,
        [LobbyConfig.ICON_ID.NEW_VIP]  = function () bind:onTouchEventVip(send, eventType) end,
        [LobbyConfig.ICON_ID.TOPON_PH_FEE] = function() bind:onTouchEventShowToponActPhFee(send, eventType) end,
        [LobbyConfig.ICON_ID.PRAY_ACT] = function() bind:onTouchEventPrayAct(send, eventType) end,
        [LobbyConfig.ICON_ID.LAYOR_DAY_GIFT] = function() bind:onTouchEventLaborDayGiftAct(send, eventType) end,
        [LobbyConfig.ICON_ID.BU_YU_DA_JIANG_SAI] = function () bind:onTouchEventH5BuYuDaJiangSai(send,eventType) end,
        [LobbyConfig.ICON_ID.DA_TIAN_SHI_ZHI_JIAN] = function () bind:onTouchEventH5DaTianShiZhiJian(send,eventType) end,
        [LobbyConfig.ICON_ID.LING_MENG_ZHI_BO] = function() bind:onTouchEventH5LingMeng(send, eventType) end,
        [LobbyConfig.ICON_ID.RE_XUE_FENG_SHEN] = function () bind:onTouchEventH5ReXue(send,eventType) end,
        [LobbyConfig.ICON_ID.DOU_LUO_DA_LU] = function() bind:onTouchEventH5DouLuo(send, eventType) end,
        [LobbyConfig.ICON_ID.JIE_JI_SAN_GUO] = function() bind:onTouchEventH5SanGuo(send, eventType) end,
        [LobbyConfig.ICON_ID.REN_REN_BU_YU] = function() bind:onTouchEventUnityRRBUYU(send, eventType) end,
        [LobbyConfig.ICON_ID.REN_REN_BU_YU_1] = function() bind:onTouchEventUnityRRBUYU(send, eventType) end,
        [LobbyConfig.ICON_ID.REN_REN_BU_YU_2] = function() bind:onTouchEventUnityRRBUYU(send, eventType) end,
        [LobbyConfig.ICON_ID.SHEI_SHI_SHOU_FU] = function() bind:onTouchEventH5SheiShiShouFu(send, eventType) end,
        [LobbyConfig.ICON_ID.CREATE_TEA_HOUSE] = function() bind:onTouchEventCreateTeaHouse(send, eventType) end,
        [LobbyConfig.ICON_ID.NEW_PALY_TYPE_C]  = function () bind:onTouchEventNewPlayTypeC(send, eventType) end,
        [LobbyConfig.ICON_ID.PROMOTE_ACT] = function() bind:onTouchEventShowPromote(send, eventType) end,
        [LobbyConfig.ICON_ID.WX_PUBLIC] = function() bind:onTouchEventWxPublic(send, eventType) end,
        [LobbyConfig.ICON_ID.LUCKY_TASK] = function() bind:onTouchEventShowLuckyTask(send, eventType) end,
        [LobbyConfig.ICON_ID.SIGN_IN] = function() bind:onTouchEventTabSignInAct(send, eventType) end,
        [LobbyConfig.ICON_ID.APP_UPDATE_TIP] = function() bind:onTouchEventAppUpdateTip(send, eventType) end,
        [LobbyConfig.ICON_ID.GOLDNEW_LZDDZ] = function() bind:onTouchEventGoldNewView(send, eventType) end,
        [LobbyConfig.ICON_ID.GOLDNEW_LZDDZ_CENTER] = function() bind:onTouchEventGoldNewView(send, eventType) end,
        [LobbyConfig.ICON_ID.JINLONGJIANGLIN] = function() bind:onTouchEventJinLong(send, eventType) end,
        [LobbyConfig.ICON_ID.WEIJINGCHUANQI] = function() bind:onTouchEventH5WeiJingChuangQi(send, eventType) end,
        [LobbyConfig.ICON_ID.ZHUOYUECHUANSHUO] = function() bind:onTouchEventH5ZYCS(send, eventType) end,
        [LobbyConfig.ICON_ID.YUANZHENGJIANGSHI] = function() bind:onTouchEventH5YZJS(send, eventType) end,
        [LobbyConfig.ICON_ID.RONGYAOZHINU] = function() bind:onTouchEventH5RYZN(send, eventType) end,
        [LobbyConfig.ICON_ID.BAIZHANSHACHENG] = function() bind:onTouchEventH5BZSC(send, eventType) end,
        [LobbyConfig.ICON_ID.GOLD_NEW_BANKRUPT] = function() bind:onTouchEventGoldBankrupt(send, eventType) end,
        [LobbyConfig.ICON_ID.GOLD_NEW_DEFEAT] = function() bind:onTouchEventDefeat(send, eventType) end,
        [LobbyConfig.ICON_ID.HUANXIANGMINGJIANGLU] = function() bind:onTouchEventH5HXMJL(send, eventType) end,
        [LobbyConfig.ICON_ID.AOYUN_ACT] = function() bind:onTouchEventAoYunAct(send, eventType) end,
}

    local func = dealFuncTab[touchID]
    if func then 
        func()
    end
end

function LobbyView:onTouchEventAppUpdateTip(send,eventType)
    self:showUpdateTip()
end

function LobbyView:onTouchEventWxPublic(send, eventType)
    local WXPublicConfig = require("lobby.Modules.WXPublic.Config")
    if not WXPublicConfig.PublicConfig[XH.areaData:getLobbyID()] then
        XH.TipTool.showToast("请联系客服")
        return 
    end
    XH.viewManager:openView("WXPublicView")
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.WX_PUBLIC_CLICK_LOBBY_ICON)
end

function LobbyView:onOpenView(event)
    if event and XH.viewManager:isFullScreenView(event.viewName) then
        self:setVisible(false)
    end
end

function LobbyView:onCloseView(event)
    if event and XH.viewManager:isFullScreenView(event.viewName) then
        self:setVisible(true)
    end
end

function LobbyView:on_KWA_BTND_SHOP(send,eventType)
    local productType 
    local paramData = {}
    -- productType = ShopConfig.SpecialTag.GOLD
    -- paramData.productType = productType
    if self._shopCardLight then
        paramData.showCardChargeTip = true 
    end
    if self._shopGoldLight then
        paramData.showGoldChargeTip = true 
    end
    XH.viewManager:openView("ShopView", nil, paramData)
    --点击商城后隐藏充值提示
    self:hideShopIconChargeTip()
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_BOTTOM_SHOP,{Source = self._curLobbyType})
    XH.lobby:getModule("PropEventTracker"):track(XH.NewThrowDataDefine.dj24080866, {Source = self._curLobbyType})
end

function LobbyView:on_KWA_BTND_EVENT(send,eventType)
    if XH.lobby:getModule("TabsActivity"):getCanGetFanZhaAward() then
        XH.viewManager:openView("TabsActivityView", nil, { tab = "FANZHA"})
    else
        XH.viewManager:openView("TabsActivityView")
    end
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_BOTTOM_ACTIVITY,{Source = self._curLobbyType})
end

function LobbyView:on_KWA_BTND_HELP(send,eventType)
    XH.viewManager:openView("RuleView")
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_MORE_RULE,{Source = self._curLobbyType})
end

function LobbyView:on_KWA_BTND_POST(send,eventType)
    XH.viewManager:openView("TabsActivityView", nil, {firstTab = 0})
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_MORE_ANNOUNCE,{Source = self._curLobbyType})
end

function LobbyView:on_KWA_BTND_RESULT(send,eventType)
    XH.viewManager:openView("PlayerBillView")
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_BOTTOM_BILL)
end

function LobbyView:on_KWA_BTND_SETTING(send,eventType)
    XH.viewManager:openView("SetView")
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_MORE_SETTING,{Source = self._curLobbyType})
end

function LobbyView:on_KWA_BTND_MORE(send, eventType)
    if self._curLobbyType ==  XH.LOBBY_TYPE.YUEJU then
        self:changeMainSecondMenuVisible()
    elseif self._curLobbyType ==  XH.LOBBY_TYPE.XIUXIAN then
        self:changeGoldSecondMenuVisible()
    end
    cc.UserDefault:getInstance():setBoolForKey("KW_FIRST_CLICK_GENGDUO", false)
    self:setTipTextImgVisible(LobbyConfig.ICON_ID.GENG_DUO, false)
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_BOX_MOER,{Source = self._curLobbyType})
end

function LobbyView:on_KWA_BTND_INVATE(send,eventType)
    -- 隐藏邀请码红点
    XH.userDefault:setValue(XH.userDefault.KEY_ID.LAST_TOUCH_INVATE, os.time())
    self:setMenuIconRedPointVisible(LobbyConfig.ICON_ID.YAO_QING,false)
    XH.viewManager:openView("InvateView")
end

function LobbyView:on_KWA_BTND_HEALTH_NOTE(send,eventType)    
    XH.viewManager:openView("HealthView")
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_MORE_HEALTH,{Source = self._curLobbyType})
end

function LobbyView:on_KWA_BTND_ANTI_JAMBEL(send,eventType)
    XH.viewManager:openView("AniGambleView")
end

function LobbyView:on_KWA_BTND_BIND_PHONE(send,eventType)
    XH.viewManager:openView("PhoneBindView")
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_PHONE_BIND,{Source = self._curLobbyType})
end

function LobbyView:on_KWA_BTND_LIQUAN(send,eventType)
end

function LobbyView:on_KWA_BTND_SHARE(send,eventType)
    local shareInfo = XH.lobby:getModule("Share"):getShareInfo()
    if shareInfo == nil or shareInfo.award == nil or #shareInfo.award == 0 then
        XH.lobby:getModule("Lobby"):onTouchShare()
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.GOLD_SHARE_NEW_LOBBY_SHARE_CLICK, nil, {share_type = 0, area_id = XH.areaData:getAreaID()})
    else
        XH.viewManager:openView("LobbyShareView")
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.GOLD_SHARE_NEW_LOBBY_SHARE_CLICK, nil, {share_type = 1, area_id = XH.areaData:getAreaID()})
    end
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_BOTTOM_SHARE)
end

function LobbyView:on_KWA_BTND_MAIL(send,eventType)
    XH.viewManager:openView("MailView")
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_EMAIL)
end

-- 客服系统点击事件(numid, nickname, avatar)
function LobbyView:on_KWA_BTND_SERVICE(send, eventType)
    XH.lobby:getModule("CusService"):oponCusServerView(nil,"GAMELOBBY")
    self:hideKeFuTips()
    XH.lobby:getModule("CrmNotice"):clearCrmNotice()
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_CUSTOMER_SERVICE)
end

function LobbyView:on_KWA_BTND_BAG(send,eventType)
    XH.viewManager:openView("BagMainViewNew")
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_BOTTOM_BAG)
    self:setTipTextImgVisible(LobbyConfig.ICON_ID.BEI_BAO, false)
end

-- 浙江新闻点击事件
function LobbyView:onTouchEventZheJiangNews(send, eventType)
    XH.viewManager:openView("ZheJiangNewsView")
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.ZJ_NEWS_BTN)
end

--by ruzibeil
--新增B端迁移活动按钮
function LobbyView:onTouchEventTransferActivity(send,eventType)
    XH.viewManager:openView("AgentTransferView")
end

function LobbyView:onTouchEventBackAct(send, eventType)
    XH.viewManager:openView("TabsActivityView", nil, { tab = "BACKGIFT"})
    self:refreshIcon(send, LobbyConfig.ICON_ID.HUI_GUI_LI_BAO)
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.BACK_ACT_LOBBY_ICON)
end

function LobbyView:onTouchEventDailyLuckyAct(send, eventType)
    XH.viewManager:openView("DailyLuckDrawMainView")
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_DAILY_LUCKY_DRAW,{Source = self._curLobbyType})
end

function LobbyView:onTouchEventBreakGift(send, eventType)
    if XH.playerData:checkNewRealName() ~= 0 then return end--实名限制
    if XH.playerData:checkNewRealNameVisitor() ~= 0 then return end--游客登陆
    XH.viewManager:openView("GiftBagView")
end

function LobbyView:onTouchEventH5Legend(send, eventType)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.ZHUBO_CLICK,{label = "决战沙邑",item_id = "决战沙邑"})
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.LEGEND)
end

function LobbyView:onTouchEventH5BuYu(send, eventType) 
    if self._curLobbyType == XH.LOBBY_TYPE.XIUXIAN then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.BUYUWANG_GOLD)
    else
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.SHOUFU_CLICK,{label = "捕鱼王"})
    end
    XH.viewManager:openView("FishingKingTransferView")
end


function LobbyView:onTouchEventH5ReXue(send, eventType)
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.REXUEFENGSHEN)
end

function LobbyView:onTouchEventH5DouLuo(send, eventType)
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.DOULUODALU)
end

function LobbyView:onTouchEventH5SanGuo(send, eventType)
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.JIEJISANGUO)
end

function LobbyView:onTouchEventH5SheiShiShouFu(send, eventType)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.SHOUFU_CLICK)
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.SHEISHISHOUFU)
end

function LobbyView:onTouchEventH5BuYuDaJiangSai(send,eventType)
    local tmpData = {}
    tmpData.userid = XH.playerData:getNumberID()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.H5Game_buyudajiangsai, tmpData)
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.BUYUDAJIANGSAI)
end

function LobbyView:onTouchEventH5DaTianShiZhiJian(send,eventType)
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.DATIANSHIZHIJIAN)
end

function LobbyView:onTouchEventH5Knifelegend(send, eventType)
    --点击合集大厅(原一刀传世) 开启H5Room 
    -- self:startH5Room()
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.KNIFELEGEND)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.GOLD_ICON_YIDAOCHUANSHI_CLICK)
end

function LobbyView:onTouchEventH5LingMeng(send, eventType)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.ZHUBO_CLICK)
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.LINGMENGZHIBO)
end

function LobbyView:onTouchEventUnityRRBUYU(send,eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local data = {
        block_label = "人人捕鱼"
    }
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.lianyun2024021801, data)
    if not XH.lobby:getModule("RRBuYu"):isSupportRRBY() then
        return
    end
    send:setTouchEnabled(false)
    self:performWithDelay(function()
        if send and not tolua.isnull(send) then
            send:setTouchEnabled(true)
        end
    end, 4)
    if XH.UITool.isVisible(send,"KW_NODE_PROGRESS") or XH.lobby:getModule("RRBuYu"):isDownloading() then 
        return
    end
    self:startUnityGame(XH.lobby:getModule("Lobby").H5GameNameList.RENRENBUYU)
end

-- 3折礼包
function LobbyView:onTouchEventShowDiscountLayer(send, eventType)
    XH.viewManager:openView("SaleGiftbagView", nil, self._discountData)
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_BOTTOM_DISCOUNTGIFT)
end

function LobbyView:onEventShowDiscountGift(event)
    self:setMenuIconVisible(LobbyConfig.ICON_ID.ZHE_KOU_LI_BAO, false)
end

function LobbyView:onShowDiscountLayer(data)
    if data and data.msg then
        self._discountData = data.msg
        if data.msg.showIcon then
            self:initDiscountAni(data.msg.showLayer)
        else
            self:setMenuIconVisible(LobbyConfig.ICON_ID.ZHE_KOU_LI_BAO, false)
        end
    end
end

function LobbyView:showLeftTime()
    if not self._discountData or not self._discountData.leftTime or self._discountData.leftTime < 0 then
        return
    end
    local leftTimeText = XH.UITool.seekNodeByName(self._discountBtnBG, "_KW_DISCOUNT_GIFT_LEFT_TIME")
    if not leftTimeText then
        return
    end
    local day = math.floor(self._discountData.leftTime / 60 / 60 / 24)
    local hour = math.floor(self._discountData.leftTime / 60 / 60) % 24
    local min = math.floor(self._discountData.leftTime / 60) % 60
    local sec = self._discountData.leftTime % 60
    local str = string.format("%d天%d时", day, hour)
    if day == 0 then
        str = string.format("%d时%d分", hour, min)
        if hour == 0 then
            str = string.format("%d分%d秒", min, sec)
        end
    end
    leftTimeText:setString(str)
    leftTimeText:stopAllActions()
    leftTimeText:runAction(cc.Sequence:create(cc.DelayTime:create(1), cc.CallFunc:create(
        function()
            self._discountData.leftTime = self._discountData.leftTime - 1
            self:showLeftTime()
        end
    )))
end

------------------------------破产礼包------------------------------

function LobbyView:onBreakGiftBagShow(event)
    self:setMenuIconVisible(LobbyConfig.ICON_ID.XIAN_SHI_YOU_HUI, false)
end

function LobbyView:showBreakGift()
    --休闲大厅返回到游戏时
    local breakGiftInfo = XH.taskManager:getBreakGiftInfo()
    if not breakGiftInfo then
        self:setMenuIconVisible(LobbyConfig.ICON_ID.XIAN_SHI_YOU_HUI, false)
        return
    end
    self:setMenuIconVisible(LobbyConfig.ICON_ID.XIAN_SHI_YOU_HUI, breakGiftInfo.isShowBtn)
end

function LobbyView:onBreakGiftBagEvent(event)
    self:showBreakGift()
end

function LobbyView:initDiscountAni(bShowLayer)
    self:setMenuIconVisible(LobbyConfig.ICON_ID.ZHE_KOU_LI_BAO, true)
    if bShowLayer then
        XH.viewManager:openView("SaleGiftbagView", nil, self._discountData)
    end
end

----------------------------H5ROOM游戏------------------------------
function LobbyView:startH5Room()
    XH.lobby:getModule("Lobby"):startH5Room()
end 
 
----------------------------UNITY游戏------------------------------
function LobbyView:startUnityGame(gameName)
    if gameName == XH.lobby:getModule("Lobby").H5GameNameList.RENRENBUYU then
        XH.TipTool.showLoading(5)
        if device.platform == "android" then
            XH.lobby:getModule("RRBuYu"):isDownloadCompleted(true)
            XH.lobby:getModule("Replugin"):postToSepperllitaPlugin(1)
        elseif device.platform == "ios" then
            XH.lobby:getModule("RRBuYu"):joinInRRBuyu()
        end
        XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.RENRENBUYU_1)
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.RENRENBUYU_1)
    end
end


------------------------------------------------------------------------


function LobbyView:h5StartGameLoginCallback(cell, type, data)
    -- 回恢复音效
    local musicPercent = XH.lobby:getModule("Set"):getMusicPercent()
    local soundPercent = XH.lobby:getModule("Set"):getSoundPercent()
    XH.audioManager:SetMusicVolume(musicPercent / 100.0)
    XH.audioManager:SetSFXVolume(soundPercent / 100.0)
end

function LobbyView:on_KWA_BTND_ADD_ROOM_CARD(send,eventType)
    XH.playerData:flushPlayerDrop()
    XH.viewManager:openView("ShopView", nil, {productType = ShopConfig.SpecialTag.CARD})
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_ADD_CARD_BTN)
end

function LobbyView:on_KWA_BTND_ADD_DIAMOND(send,eventType)
    XH.playerData:flushPlayerDrop()
    XH.viewManager:openView("ShopView", nil, {productType = ShopConfig.SpecialTag.DIAMOND})
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_ADD_DIAMOND_BTN)
end

function LobbyView:onShowDetailGoldLayer(event)
    local moduleName = "lobby.Modules.Gold.LevelView" .. (event.goldUIStype or "")
    local success, levelLayerModule = pcall(require, moduleName)
    if not success then
        -- 如果加载失败，则使用默认模块
        levelLayerModule = require("lobby.Modules.Gold.LevelView")
    end
    local levelLayer = levelLayerModule.new(event.confID, event.gameID, event.goldUIStype);
    self:addChild(levelLayer)
end

function LobbyView:onTouchEventShareHmqAct(send, eventType)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.USERDEFAULT_ACTIVITY_TYPE, XH.lobby:getModule("TabsActivity").ActivityType.ActivityShareHmq)
    XH.viewManager:openView("TabsActivityView", nil, { tab = "SHAREFORCARD"})
    self:refreshIcon(send, LobbyConfig.ICON_ID.SHARE_HMQ_ACT)
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_SHARE_AWARD_CARD)
end

function LobbyView:onTouchEventLuckTurnTableAct(send, eventType)
    XH.viewManager:openView("TabsActivityView", nil, { tab = "LUCKYTURNTABLE"})
    self:refreshIcon(send, LobbyConfig.ICON_ID.LUCKY_TURN_TABLE)
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_UESR_LUCKY_TURN_TABLE_ACT)
end

function LobbyView:onTouchEventVip(send, eventType)
    XH.viewManager:openView("TabsActivityView", nil, { tab = "VIPGIFT"})
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.VIP_WELFARE_BTN,{Source = self._curLobbyType})
end

function LobbyView:PlayAnimation(node,path,armatureName,animationPlayParam)
    if node and node:getChildrenCount() == 0 then
        if not cc.FileUtils:getInstance():isFileExist(path) then
            return
        end
        ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(path)
        local aniNode = ccs.Armature:create(armatureName)
        node:addChild(aniNode)
        aniNode:getAnimation():play(animationPlayParam, -1, 1)
    end
end

function LobbyView:initVipAni(isVip)
    if not XH.areaData:isSupportVip() then
        return
    end
    local menuItem = self.KWA_HEAD_PANEL:getChildByName("vipAniMenu")
    if not menuItem then
        menuItem = ccui.Layout:create()
        menuItem:setAnchorPoint(0.5, 0.5)
        local size = cc.size(49, 51)
        menuItem:setContentSize(size)
        menuItem:setPosition(cc.p(104, 99))
        menuItem:setTouchEnabled(true)
        menuItem:addTouchEventListener(handler(self, self.onTouchEventVip))
        menuItem:setName("vipAniMenu")
        self.KWA_HEAD_PANEL:addChild(menuItem)
        if cc.FileUtils:getInstance():isFileExist("animation/Lobby/Base/vip-icon/vip-icon.ExportJson") then
            ccs.ArmatureDataManager:getInstance():addArmatureFileInfo("animation/Lobby/Base/vip-icon/vip-icon.ExportJson")
            local shopAni = ccs.Armature:create("vip-icon")
            menuItem:addChild(shopAni)
            shopAni:setAnchorPoint(cc.p(0.5, 0.5))
            shopAni:setPosition(cc.p(size.width/2, size.height/2))
            shopAni:getAnimation():play("Animation1", -1, 1)
            shopAni:setName("vipAni")
        end

        local shopAni = menuItem:getChildByName("vipAni")
        if shopAni then
            local shaderCmd
            if isVip then 
                shaderCmd = "ShaderPositionTextureColor_noMVP"
            else 
                shaderCmd = "ShaderUIGrayScale"
            end
            local glProgram = cc.GLProgramCache:getInstance():getGLProgram(shaderCmd)
            if glProgram then
                local bones = shopAni:getBoneDic()
                for _,v in pairs(bones) do
                    local renderNode = v:getDisplayManager():getDisplayRenderNode()
                    if renderNode then
                        renderNode:setGLProgram(glProgram)
                    end
                end
            end
        end
    end
end

-- 对低版本隐藏相关抽奖ICON
function LobbyView:showDailyLuckyDrawIcon()
    if device.platform == "android" and XH.SysTool:GetBundleVersion() <= "1.0.1" then
        self:setMenuIconVisible(LobbyConfig.ICON_ID.MEI_RI_CHOU_JIANG, false)
    elseif XH.ChannelTool.checkIsECarChannel() then
        self:setMenuIconVisible(LobbyConfig.ICON_ID.MEI_RI_CHOU_JIANG, false)
    elseif XH.playerData:isRechargePlayer() then
        self:setMenuIconVisible(LobbyConfig.ICON_ID.MEI_RI_CHOU_JIANG, false)
    else
        self:setMenuIconVisible(LobbyConfig.ICON_ID.MEI_RI_CHOU_JIANG, true)
    end
end

-- 防止安卓资源被释放
function LobbyView:retainPlists()
    if cc.Application:getInstance():getTargetPlatform() ~= cc.PLATFORM_OS_ANDROID then
        return
    end
    local filePlists = {
        "res/cocosStudio/hall/Image/img_login.plist",
        "res/cocosStudio/Common/Image/HallWithTea/common_htg.plist",
		"res/cocosStudio/hall/Image/goldLayer/goldLayer.plist",
		"res/cocosStudio/hall/Image/goldLayer/gold_title.plist",
    }
    for i = 1, #filePlists do
        local getTables = cc.FileUtils:getInstance():getValueMapFromFile(filePlists[i])
        if getTables and getTables.frames then
            for pngName, _ in pairs(getTables.frames) do
                local spriteFrame = cc.SpriteFrameCache:getInstance():getSpriteFrameByName(pngName)
                if spriteFrame then
                    local getPngKeys = spriteFrame:getReferenceCount()
                    if getPngKeys and getPngKeys == 1 then
                        spriteFrame:retain()
                    end
                end
            end
        end
    end
end

function LobbyView:onTipButtonClicked(event)
    local send = event.send
    local eventType = event.eventType
    local bind = event.bind
    local touchParent = send:getParent() and send:getParent():getParent()
    local touchID = nil
    if touchParent then
        touchID = tonumber(touchParent:getName())
    end
    local dealFuncTab = {
        [LobbyConfig.ICON_ID.KE_FU] = function() bind:on_KWA_BTND_SERVICE_TIP(send, eventType) end,
    }
    local func = dealFuncTab[touchID]
    if func then
        func()
    end
end

function LobbyView:on_KWA_BTND_SERVICE_TIP(send, eventType)
    self:hideKeFuTips()
end

function LobbyView:initTipImageButtonCallBack()
    for _, menuBar in pairs(self:getAllMenuBars()) do
        if menuBar then
            menuBar:addTipButtonClickListener(self, self.onTipButtonClicked)
        end
    end
end

function LobbyView:hideKeFuTips()
    self:removeMenuIconTipText(LobbyConfig.ICON_ID.KE_FU)
    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_CUSSERVICE_TIP_SHOW, true)
end

function LobbyView:onEventflushPhoneBindRedPos(msg)
    if msg then
        self:setMenuIconRedPointVisible(LobbyConfig.ICON_ID.HUO_DONG, msg.data)
    end
end

function LobbyView:onEventOpenImCallBack()
    local bSupportFriendList = XH.areaData:isSupportFriendList()
    if bSupportFriendList and self._imListLayer ~= nil then
        self._imListLayer:showListLayer()
    end
end

function LobbyView:showPhoneBindQiPao()
    if not XH.playerData:isBindPhone() and (XH.playerData:getRoomCard() <= 1 or XH.playerData:getSR() <= 2000) then
        self:setMenuIconTipText(LobbyConfig.ICON_ID.HUO_DONG, "免费房卡金币！")
    else   
        self:setTipTextImgVisible(LobbyConfig.ICON_ID.HUO_DONG, false)
    end
end

function LobbyView:showSexVoiceTip()
    self:setTipTextImgVisible(LobbyConfig.ICON_ID.GENG_DUO, false)
end

--[[
    @desc: 房卡数量小于2或者金币数量小于2000,显示充值提示
    author:{qiuzhong}
    time:2021-02-01 13:49:25
]]
function LobbyView:showShopChargeTip()
    local minRoomCardNum = 2
    local minGoldNum = 2000
    self._shopCardLight = false
    self._shopGoldLight = false
    local isTodayCardShow = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_LOBBY_SHOP_CHARGE_CARD_TIP_SHOW, false)
    local isTodayGoldShow = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_LOBBY_SHOP_CHARGE_GOLD_TIP_SHOW, false)
    if XH.playerData:getRoomCard() <= minRoomCardNum and not isTodayCardShow then
        self._shopCardLight = true
    end
    if  XH.playerData:getSR() <= minGoldNum and not isTodayGoldShow then
        self._shopGoldLight = true
    end
    self:setMenuIconExtendAnimVisible(LobbyConfig.ICON_ID.SHANG_CHENG, self._shopCardLight or self._shopGoldLight)
    if self._shopCardLight  or  self._shopGoldLight then
        self:setMenuIconTipText(LobbyConfig.ICON_ID.SHANG_CHENG, "您有新的折\n扣请查收！")
    else
        self:setTipTextImgVisible(LobbyConfig.ICON_ID.SHANG_CHENG,false)
    end
end

--[[
    @desc: 隐藏商城充值提示
    author:{qiuzhong}
    time:2021-02-01 13:54:57
]]
function LobbyView:hideShopIconChargeTip()
    if self._shopCardLight then
        self._shopCardLight = false
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_LOBBY_SHOP_CHARGE_CARD_TIP_SHOW, true)
    end
    if self._shopGoldLight then
        self._shopGoldLight = false
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_LOBBY_SHOP_CHARGE_GOLD_TIP_SHOW, true)
    end
    self:setMenuIconExtendAnimVisible(LobbyConfig.ICON_ID.SHANG_CHENG, false)
    self:setTipTextImgVisible(LobbyConfig.ICON_ID.SHANG_CHENG,false)
end

-- 大厅领话费
function LobbyView:onTouchEventShowToponActPhFee(send, eventType)
    XH.viewManager:openView("TabsActivityView", nil, { tab = "TOPONPHONEFEE"})
    self:refreshIcon(send, LobbyConfig.ICON_ID.TOPON_PH_FEE)
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.LOBBY_TOPON_PHONE_FEE_ICON)
end

-- 推广活动
function LobbyView:onTouchEventShowPromote(send,eventType)
    XH.viewManager:openView("PromoteHallView")
end

-- 幸运任务
function LobbyView:onTouchEventShowLuckyTask(send,eventType)
    local throwDataSceneName = ""
    if self._goldTopMenuBar and self._goldTopMenuBar:isVisible() then
        throwDataSceneName = "金币场"
    elseif self._mainRightMenuBar and self._mainRightMenuBar:isVisible() then 
        throwDataSceneName = "大厅"
    end
    XH.viewManager:openView("LuckyTaskView",nil,throwDataSceneName)

    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.LUCKY_TASK_ICON_CLICK, {block_label = throwDataSceneName})
end

-- 签到页面
function LobbyView:onTouchEventTabSignInAct(send,eventType)
    XH.viewManager:openView("TabsActivityView", nil, { tab = "DAILYSIGN"})
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.GOLD_SHARE_NEW_SIGN_CLICK, nil, {area_id = XH.areaData:getAreaID()})
end

--祈福小游戏
function LobbyView:onTouchEventPrayAct(send, eventType)
    XH.viewManager:openView("PrayActView")
end

function LobbyView:onTouchEventLaborDayGiftAct(send, eventType)
    local userData = {}
    userData.userid = XH.playerData:getNumberID()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.LaborDayGiftOpen, userData)
    XH.viewManager:openView("TabsActivityView", nil, { tab = "LABORDAYGIFT"})
    self:refreshIcon(send, LobbyConfig.ICON_ID.LAYOR_DAY_GIFT)
end

function LobbyView:onPrayActInfo(event)
    if not event or not event.data then
        return
    end
    if event.data.giftbox == 3 then
        self.KWA_PARY_DEC:setVisible(true)
        self.KWA_PARY_DEC:loadTexture(string.format("prayact_logo_%d.png", event.data.selectkind),ccui.TextureResType.plistType)
        self:performWithDelay(function()
            XH.playerData:flushGoldCoin()
        end,3)
    end
end

function LobbyView:onEventShowPromoteIcon(event)
    if not event or not event.data then
        return
    end
    self:setMenuIconVisible(event.data.icon, event.data.isShow)
    self:UpdatePromoteState()
end

function LobbyView:onEventShowLuckTaskIcon(event)
  if not event or not event.data then
      return
  end
  self:setMenuIconVisible(event.data.icon, event.data.isShow)
end

function LobbyView:onEventShowLaborDay(event)
    if not event or not event.data then
        return
    end
    self:setMenuIconVisible(event.data.icon, event.data.isShow)
end

function LobbyView:updateCpsChannelUI()
    if XH.ChannelTool.checkIsDoubleKouGame() then
        return
    end
    if XH.ChannelTool.checkIsCPSChannel() then
        self._KW_BOX_PANEL:setVisible(false)
        self._KW_GOLD_PANEL:setVisible(false)
        self._KW_BOX_PANEL_CPS:setVisible(true)
        self._KW_GOLD_PANEL_CPS:setVisible(true)
        local imgName = XH.ChannelTool.getCPSHotupdateImg()
        if  imgName ~= "" then 
            self._KW_IMG_CPS_BACKGROUND:loadTexture(imgName, ccui.TextureResType.localType) 
        end
        self._KW_IMG_CPS_BACKGROUND:setVisible(true)
        if XH.ChannelTool.checkIsDoubleKouGame() then
            self._KW_IMG_CPS_BACKGROUND:setVisible(false)
        end
        self:performWithDelay(function()
            self._KW_IMG_CPS_BACKGROUND:setVisible(false)
        end,3)
    elseif XH.ChannelTool.checkIsWaishengChannel() then 
        self._KW_BOX_PANEL:setVisible(false)
        self._KW_GOLD_PANEL:setVisible(false)
        self._KW_BOX_PANEL_CPS:setVisible(true)
        self._KW_GOLD_PANEL_CPS:setVisible(true)
        --缩放调整位置
        self._KW_BOX_PANEL_CPS:setScale(0.9)
        self._KW_BOX_PANEL_CPS:setAnchorPoint(cc.p(0.5, 0.5))
        self._KW_BOX_PANEL_CPS:setPositionType(ccui.PositionType.percent)
        self._KW_BOX_PANEL_CPS:setPositionPercent(cc.p(0.25, 0.1))

    end
end

-- 切到前台
function LobbyView:onEnterCPSChannelForeground()
    self:onEnterH5Game()
end

function LobbyView:onEnterH5GameListen()
    if not self._bNotfirstEnterH5 then
        self._bNotfirstEnterH5 = true
        self:onEnterH5Game()
    end
end

function LobbyView:onH5GameClose()
    if XH.ChannelTool.checkIsCPSChannel() and not XH.ChannelTool.checkIsDoubleKouGame() then
        self:performWithDelay(function()
            self._KW_IMG_CPS_BACKGROUND:setVisible(false)
        end,1)
    end
end

function LobbyView:onEnterH5Game()
    --特殊渠道直接打开游戏
    if XH.ChannelTool.checkIsCPSChannel() and not XH.ChannelTool.checkIsDoubleKouGame() then
        local position = XH.playerData:getPlayerPosition()
        --不在游戏中，且在大厅
        if position.gameID == 0 and XH.SceneManager:getInstance():getTopSceneTag() == XH.SCENE_TAG.LOBBY then
            XH.lobby:getModule("Lobby"):startH5Game(XH.ChannelTool.getH5GameName())
        end  
   end
end

function LobbyView:onTouchEventTeHuiShangCheng(send, eventType)
    XH.viewManager:openView("TabsActivityView", nil, { tab = "PERSONALGIFT"})
    local data = {}
    data.userid = XH.playerData:getNumberID()
    XH.throwDataManager:throwData(XH.ThrowDataDefine.PersonalGiftPackIcon,data)
end

function LobbyView:onPersonalGiftPackInfo(event)
    local confData = event.msg.configData or {}
    local bShow = #confData > 0
    self:setMenuIconVisible(LobbyConfig.ICON_ID.TE_HUI_SHANG_CHENG, bShow)
end

function LobbyView:on_KW_IMG_CPS_BACKGROUND(send, eventType)
    self._KW_IMG_CPS_BACKGROUND:setVisible(false) 
end

function LobbyView:refreshIcon(icon, iconID)
    if not iconID or not iconID then
        return
    end
    local iconClicked = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_ACT_ICON_CLICKED, false, {key = iconID})
    if not iconClicked then
        cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/hall/Image/Activity/activity_icons.plist")
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_ACT_ICON_CLICKED, true, {key = iconID})
        icon:stopAllActions()
        icon:removeAllChildren()
        if icon._imgIcon then
            icon._imgIcon = nil
        end
        local iconCfg = LobbyConfig.MenuIconConfig[iconID]
        icon:initImageViewIcon(iconCfg.Plist, iconCfg.IconResName)
    end
end

function LobbyView:onEventShowLeisureEntrance(event)
    self:updateLeisureEntrance()
end

function LobbyView:updateLeisureEntrance()
    local leisureEntranceData = XH.lobby:getModule("Lobby"):getLeisureEntranceData()
    if not leisureEntranceData or not next(leisureEntranceData) then
        return
    end
    self:onRefreshGoldUI()
end

function LobbyView:onEventGetIconWorldPos(event)
    if not event or not event.data then
        return
    end
    local data = event.data
    for _, menuBar in pairs(self:getAllMenuBars()) do
        local btn = menuBar:getMenuByID(data.iconId)
        if btn then
            local posX, posY = btn:getPosition()
            local worldPos = menuBar:convertToWorldSpace(cc.p(posX,posY))
            local size = btn:getContentSize()
            data.callback(worldPos,size)
            return
        end
    end
    data.callback()
end

function LobbyView:onTouchEventCreateTeaHouse(send, eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    XH.viewManager:openView("CreateTeaHouseView")
    XH.throwDataManager:throwDataClick(XH.ThrowDataDefine.TEA_CREATE_1)
end

function LobbyView:onEventShowCreateTeaHouseAct(event)
    if not event or not event.data then
        return
    end
    self:setMenuIconVisible(event.data.icon, event.data.isShow)
    if self._imgShowTeaHouseAwardTip then 
        self._imgShowTeaHouseAwardTip:setVisible(event.data.isShow)
    end
end

function LobbyView:initAudio()
    local gameID = 1
    local key = "GameSound_" .. gameID
    local priority = 1
    local GameSoundResChecker = require("app.hotupdate.gamesound.GameSoundResChecker")
    GameSoundResChecker._startHotFix(gameID, key, priority)
end

function LobbyView:updateGameCommonAni()
    local GameAniResChecker = require("app.hotupdate.gameani.GameAniResChecker")
    GameAniResChecker._startHotFix(GameAniResChecker.KW_ANI_TYPE.GAMECOMMON, 1)
end

function LobbyView:showNewPlayTypeUI(show)
    if not self._lobbyTips or not self._lobbyTipsContent then
        return
    end
    self:initLobbyEntranceFlagImg(show)
    self._lobbyTips:setVisible(show)
    local lobbyTipsContent = GameNewTypeConfiger:getNewPlayTypeLobbyTipsByLobbyID(XH.areaData:getLobbyID())
    if lobbyTipsContent then
        self._lobbyTipsContent:setString(lobbyTipsContent)
        local contentSize = self._lobbyTipsContent:getContentSize()
        local tipsSize = self._lobbyTips:getContentSize()
        local minLength = 166
        local whiteLenth = 10
        if contentSize.width + whiteLenth * 2 > minLength then
            self._lobbyTips:setContentSize(contentSize.width + whiteLenth * 2, tipsSize.height)
        end
        self._lobbyTipsContent:setPositionX(contentSize.width / 2 + whiteLenth)
    else
        self._lobbyTips:setVisible(false)
    end
end

function LobbyView:initLobbyEntranceFlagImg(show)
    if not self._imgBoxRoomFlag or not self._imgTeaFlag or not self._imgBoxRoomFlagCps or not self._imgTeaFlagCps then
        return
    end
    pcall(function()
        local teaFlagImg = GameNewTypeConfiger:getLobbyTeaFlagImgByLobbyID(XH.areaData:getLobbyID())
        if teaFlagImg and teaFlagImg ~= "" then
            self._imgTeaFlag:loadTexture(teaFlagImg, ccui.TextureResType.plistType)
            self._imgTeaFlagCps:loadTexture(teaFlagImg, ccui.TextureResType.plistType)
            self._imgTeaFlag:setVisible(show)
            self._imgTeaFlagCps:setVisible(show)
        end
        local boxFlagImg = GameNewTypeConfiger:getLobbyBoxFlagImgByLobbyID(XH.areaData:getLobbyID())
        if boxFlagImg and boxFlagImg ~= "" then
            self._imgBoxRoomFlag:loadTexture(boxFlagImg, ccui.TextureResType.plistType)
            self._imgBoxRoomFlagCps:loadTexture(boxFlagImg, ccui.TextureResType.plistType)
            self._imgBoxRoomFlag:setVisible(show)
            self._imgBoxRoomFlagCps:setVisible(show)
        end
    end)
end

function LobbyView:onTouchEventNewPlayTypeC(send, eventType)
    XH.viewManager:openView("NewTypeDrawView")
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.LOBBY_MENUBAR_ICON_CLICK,{item_id = LobbyConfig.ICON_ID.NEW_PALY_TYPE_C})
end

function LobbyView:onEventShowRedPos(event)
    self:setMenuIconRedPointVisible(event.data.iconID, event.data.bShow)
end

function LobbyView:onGetLuckyTurnTableActData(event)
    if not event.data then
        return
    end
    if self._mainRightMenuBar and event.data.leftDraw and event.data.leftDraw > 0 then
        local newPlayTypeDrawIcon = self._mainRightMenuBar:getMenuByID(LobbyConfig.ICON_ID.NEW_PALY_TYPE_C)
        if newPlayTypeDrawIcon and newPlayTypeDrawIcon.showTipText then
            newPlayTypeDrawIcon:showTipText(LobbyConfig.ICON_TIP_TEXT_TYPE.RIGHT, "可抽奖")
        end
    end
end

function LobbyView:on_GUIDE_CHOOSEAREA()
    self.KW_PANEL_CHOOSE_GUIDE:setVisible(true)
    XH.SysTool.performWithDelayGlobal(function ()
        self.KW_PANEL_CHOOSE_GUIDE:setVisible(false)
    end,6)
end

function LobbyView:onEventLuckyTaskJoin(event)
    if event.data == nil then
        return
    end
    local type = LuckyTaskDefine.JumpType[event.data.type]
    local gameid = event.data.gameid
    if type == nil then
        return
    end
    if type == LuckyTaskDefine.JumpType.AC_JUMPTYPE_NULL then
        return
    elseif type == LuckyTaskDefine.JumpType.AC_JUMPTYPE_LOGIN then
        XH.TipTool.showToast("今天已登录，明天再来吧")
    elseif type == LuckyTaskDefine.JumpType.AC_JUMPTYPE_TEA then
        -- 比赛场
        self:on_KWA_BTND_TEA_HOUSE()
    elseif type == LuckyTaskDefine.JumpType.AC_JUMPTYPE_TANL then
        -- 包厢加比赛场
        self:on_KWA_BTND_TEA_HOUSE()
    elseif type == LuckyTaskDefine.JumpType.AC_JUMPTYPE_CHARGE then
        -- 充值
        XH.viewManager:openView("ShopView", nil, {productType = ShopConfig.SpecialTag.DIAMOND})
    elseif type == LuckyTaskDefine.JumpType.AC_JUMPTYPE_SHARE then
        -- 分享
        self:on_KWA_BTND_SHARE()
    elseif type == LuckyTaskDefine.JumpType.AC_JUMPTYPE_OTHER then
        -- 联运
    elseif type == LuckyTaskDefine.JumpType.AC_JUMPTYPE_COIN then
        -- 金币场
        self:showXIUXIANLobby()
    elseif type == LuckyTaskDefine.JumpType.AC_JUMPTYPE_GAME then 
        local confID = XH.goldConfigManager:getConfIDByGameID(gameid)
        if confID == nil then 
            return 
        end
        local roominfo = XH.goldConfigManager:getGoldRoomInfoByIndex(confID).roomInfo
        if gameView then 
            gameView.roomConf = roominfo
            gameView:onTouchEventQuickJoinGold(gameView, ccui.TouchEventType.ended)
        end 

        --gameid
    else
        -- 大厅创建面板
        local position = XH.playerData:getPlayerPosition()
        if position.gameID == 0 then
            if cc.UserDefault:getInstance():getBoolForKey("BAN_PLAYER_CAN_GAME".. XH.playerData:getNumberID()) then
                XH.TipTool.showTip({
                    type = XH.TIP_LAYER_TYPE.OK,            
                }, "您已被封禁对局，无法进入房间，如有疑问请联系客服。")
            else
                XH.viewManager:openView("CreateBoxRoomView",nil,{gameid = gameid})
            end
        else
            self:on_KWA_BTND_CREATE_BACK_BOX_ROOM()
        end
    end
end

function LobbyView:onEventLuckyTaskIconTip(event)
    if event.data == nil then
        return
    end
    for _, menuBar in pairs(self:getAllMenuBars()) do
        if menuBar._allMenuIcons ~= nil and #menuBar._allMenuIcons > 0 then
            for i = 1, #menuBar._allMenuIcons do
                if tostring(LobbyConfig.ICON_ID.LUCKY_TASK) == menuBar._allMenuIcons[i]:getName() and menuBar._allMenuIcons[i]:getChildrenCount() > 0 then
                    XH.lobby:getModule("LuckyTask"):flushAwardState(menuBar._allMenuIcons[i]:getChildren()[1], event.data.bShow)
                end
            end
        end
    end
end

function LobbyView:getMenuNodeByName(icon)
    for _, menuBar in pairs(self:getAllMenuBars()) do
        if menuBar._allMenuIcons ~= nil and #menuBar._allMenuIcons > 0 then
            for i = 1, #menuBar._allMenuIcons do
                local menu = menuBar._allMenuIcons[i]
                if tostring(icon) == menu:getName() then
                    return menu
                end
            end
        end
    end
    return nil
end

function LobbyView:onEventFlushShareInfo(event)
    local node = self:getMenuNodeByName(LobbyConfig.ICON_ID.FEN_XAING)
    if node == nil then
        return
    end
    local shareInfo = XH.lobby:getModule("Share"):getShareInfo()
    local show = not (shareInfo == nil or shareInfo.award == nil or #shareInfo.award == 0)
    self:setMenuIconRedPointVisible(LobbyConfig.ICON_ID.FEN_XAING, show)
    for i = 1,node:getChildrenCount() do 
        if node:getChildren()[i]:getName() == "KW_VIEW_LOBBY_NAME" then
            node:getChildren()[i]:setVisible(show)
            return
        end
    end
    if show then
        local tipTextImg = ccui.ImageView:create()
        tipTextImg:loadTexture("sign_in_lobby_qipao.png", ccui.TextureResType.plistType)
        tipTextImg:setAnchorPoint(cc.p(0.5,0))
        tipTextImg:setName("KW_VIEW_LOBBY_NAME")
        tipTextImg:setPosition(node:getContentSize().width*0.5+20,node:getContentSize().height*0.5+35)
        tipTextImg:setScale9Enabled(true)
        node:addChild(tipTextImg)
        local tipText = ccui.Text:create()
        tipText:setFontName("cocosStudio/Common/Font/fangzhengcuyuan.TTF")
        tipText:setFontSize(36)
        tipText:setColor(cc.c3b(233, 78, 52))
        tipText:setAnchorPoint(cc.p(0.5, 0.5))
        tipText:setString("分享有礼")
        tipTextImg:setContentSize(tipText:getContentSize().width + 40,tipTextImg:getContentSize().height+10)
        tipText:setPosition(tipTextImg:getContentSize().width*0.5,tipTextImg:getContentSize().height*0.5+15)
        tipTextImg:addChild(tipText)
    end
end

function LobbyView:onEventShowSignIcon(event)
    self:setMenuIconVisible(event.data.iconID, event.data.bShow)
end

function LobbyView:onEventShowSignRedPoint(event)
    self:setMenuIconRedPointVisible(event.data.iconID, event.data.bShow)
end

function LobbyView:onTouchEventGoldNewView(send)
    XH.lobby:getModule("Lobby"):enterNewGold()
    if send:getName() == "3026" then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_GOLD_THROW_1, nil, {area_id = XH.areaData:getAreaID()})
    elseif send:getName() == "3027" then
        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.NEW_GOLD_THROW_2, nil, {area_id = XH.areaData:getAreaID()})
    end
end

function LobbyView:onTouchEventJinLong(send, eventType)
    XH.viewManager:openView("JinLongPopNode")
end

function LobbyView:onTouchEventH5WeiJingChuangQi(send, eventType)
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.SHOUFU_CLICK)
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.WEIJINGCHUANQI)
end

function LobbyView:onTouchEventH5ZYCS(send, eventType)
    -- XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.SHOUFU_CLICK)
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.ZHUOYUECHUANSHUO)
end

function LobbyView:onTouchEventH5YZJS(send, eventType)
    -- XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.SHOUFU_CLICK)
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.YUANZHENGJIANGSHI)
end

function LobbyView:onTouchEventH5RYZN(send, eventType)
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.RONGYAOZHINU)
end

function LobbyView:onTouchEventH5BZSC(send, eventType)
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.BAIZHANSHACHENG)
end

function LobbyView:onTouchEventH5HXMJL(send, eventType)
    XH.lobby:getModule("Lobby"):startH5Game(XH.lobby:getModule("Lobby").H5GameNameList.HUANXIANGMINGJIANGLU)
end

function LobbyView:onEventArticleList()
    local list = XH.lobby:getModule("Article"):getArticleList()
    if not list or #list == 0 then
        return
    end
    self:setHorseSchedule()
end

function LobbyView:showHorseStart()
    local list = XH.lobby:getModule("Article"):getArticleList()
    if not list or #list == 0 then
        return
    end
    local idx = XH.lobby:getModule("Article"):getCurIndex()
    idx = idx + 1
    if not list[idx] then
        idx = 1
    end
    self:showHorse(list[idx])
    XH.lobby:getModule("Article"):setCurIndex(idx)
end

function LobbyView:on_KWA_TOP_HORSE()
    local list = XH.lobby:getModule("Article"):getArticleList()
    if not list or #list == 0 then
        return
    end
    local idx = XH.lobby:getModule("Article"):getCurIndex()
    if not list[idx] then
        return
    end
    XH.lobby:getModule("Article"):reqArticleThrow()
    cc.Application:getInstance():openURL(list[idx].url)
end

local horseTime = 10
function LobbyView:showHorse(str)
    self._KWA_TOP_HORSE_TEXT:stopAllActions()
    self._KWA_TOP_HORSE:setVisible(true)
    self._KWA_TOP_HORSE_TEXT:setString(str.title)
    self._KWA_TOP_HORSE_TEXT:setPositionX(self._KWA_TOP_HORSE:getContentSize().width - 60)
    local repeatAct2 = cc.MoveTo:create(horseTime, cc.p(-self._KWA_TOP_HORSE_TEXT:getContentSize().width, self._KWA_TOP_HORSE_TEXT:getPositionY()))
    self._KWA_TOP_HORSE_TEXT:runAction(repeatAct2)
end

function LobbyView:removeHorseSchedule()
    if self._listener then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._listener)
    end
    self._listener = nil
end

function LobbyView:setHorseSchedule()
    self:removeHorseSchedule()
    self:showHorseStart()
    self._listener = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        self:showHorseStart()
    end, horseTime, false)
end

function LobbyView:onEventFlushLimitFreeAct()
    self._KW_ACT_FREE_TIPS:setVisible(false)
    self._KW_ACT_FREE_LW:setVisible(false)
    self:onChangeBg()
    local info = XH.lobby:getModule("LimitFreeAct"):getActConfig()
    if not info or XH.lobby:getModule("LimitFreeAct"):isActEnd() then
        return
    end
    self:flushActBG()
    self._KW_ACT_FREE_TIPS:setVisible(true)
    self._KW_ACT_FREE_LW:setVisible(true)
    if XH.lobby:getModule("LimitFreeAct"):isInFreeTime() then
        self:flushLimitFreeUI(true)
        return
    end
    self:flushLimitFreeUI(false, XH.lobby:getModule("LimitFreeAct"):getNextFreeTimeIdx())
end

function LobbyView:flushActBG()
    cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/Common/Image/LimitFreeAct/LimitFreeAct.plist")
    self._KW_BG_CHANGE_IMG_1:loadTexture("LimitFreeAct_ny_qyyjc.png", ccui.TextureResType.plistType)
    self._KW_BG_CHANGE_IMG_2:loadTexture("LimitFreeAct_ny_qydzc.png", ccui.TextureResType.plistType)
    self._KW_BG_CHANGE_IMG_3:loadTexture("LimitFreeAct_ny_di2.png", ccui.TextureResType.plistType)
    self._KW_BG_CHANGE_IMG_4:loadTexture("LimitFreeAct_ny_qyyjc.png", ccui.TextureResType.plistType)
    self._KW_BG_CHANGE_IMG_5:loadTexture("LimitFreeAct_ny_xxjbc.png", ccui.TextureResType.plistType)
    self._KW_BG_CHANGE_IMG_6:loadTexture("LimitFreeAct_ny_di2.png", ccui.TextureResType.plistType)
    self._KW_BOX_PANEL:loadTexture("LimitFreeAct_ny_di1.png", ccui.TextureResType.plistType)
    self._KW_GOLD_PANEL:loadTexture("LimitFreeAct_ny_di1.png", ccui.TextureResType.plistType)
    self._createBackBoxRoomBtn:loadTextures("LimitFreeAct_ny_create.png", "LimitFreeAct_ny_create.png", "LimitFreeAct_ny_create.png", ccui.TextureResType.plistType)
    self.KWA_BTND_TEA_HOUSE:loadTextures("LimitFreeAct_ny_qyq.png", "LimitFreeAct_ny_qyq.png", "LimitFreeAct_ny_qyq.png", ccui.TextureResType.plistType)
    self.KWA_GOLD_BTND_JOIN_ROOM:loadTextures("LimitFreeAct_ny_jrfj.png", "LimitFreeAct_ny_jrfj.png", "LimitFreeAct_ny_jrfj.png", ccui.TextureResType.plistType)
    if XH.areaData:isSupportMatch() then
        self._KW_BG_CHANGE_IMG_2:loadTexture("lobby_title_yueju_match.png", ccui.TextureResType.plistType)
        self.KWA_BTND_TEA_HOUSE:loadTextures("lobby_btn_match.png", "lobby_btn_match.png", "lobby_btn_match.png", ccui.TextureResType.plistType)
    end
end

function LobbyView:flushLimitFreeUI(isFree, next)
    self._KW_ACT_FREE_ALL:setVisible(isFree)
    self._KW_ACT_FREE_NEXT:setVisible(not isFree)
    self._KW_ACT_FREE_NEXT_1:setVisible(next == 1)
    self._KW_ACT_FREE_NEXT_2:setVisible(next == 2)
end

function LobbyView:onClickFreeView()
    XH.viewManager:openView("LimitFreeActView")
end

function LobbyView:onClickFreeTipView()
    XH.lobby:getModule("LimitFreeAct"):openTipView()
end

function LobbyView:on_KWA_BTN_VIP(send , eventType)
    if eventType ~= ccui.TouchEventType.ended then
        return
    end
    local data = {
        block_label = '大厅顶部入口'
    }
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.zyt2024032801, data)
    XH.viewManager:openView("SxvipView")
    XH.lobby:getModule("Sxvip"):setTouchIconStatus(true)
end

function LobbyView:onUpdateSxvipStatus()
    if self._curLobbyType ==  XH.LOBBY_TYPE.YUEJU then
        local showEntry = XH.lobby:getModule("Sxvip"):getSxvipOpen()
        self.KWA_BTN_VIP:setVisible(showEntry)
        
        local messageCount = XH.lobby:getModule("Sxvip"):getMessageCount()
        if messageCount > 0 then
            -- 隐藏会员即将过期显示
            XH.UITool.setVisible(self.KWA_BTN_VIP, "_KWA_IMG_BUBBLE", false)
            local imgMessage = self.KWA_BTN_VIP:getChildByName("_KWA_IMG_MESSAGE")
            imgMessage:setVisible(true)
            local messageStr = tostring(messageCount)
            if messageCount > 99 then
                messageStr = "99+"
            end
            XH.UITool.setText(imgMessage, "_KW_TEXT_COUNT", messageStr)
            return
        end
        XH.UITool.setVisible(self.KWA_BTN_VIP, "_KWA_IMG_MESSAGE", false)

        -- 最后一天，气泡一直显示
        -- 到期，点击消失
        local sxvipRemainder = tonumber(XH.playerData:getSxvipRemainder())
        if sxvipRemainder and sxvipRemainder > 0 then
            local imgBubble = self.KWA_BTN_VIP:getChildByName("_KWA_IMG_BUBBLE")
            if imgBubble then
                imgBubble:setVisible(false)
                local textBubble = imgBubble:getChildByName("_KWA_TIPS")
                if textBubble then
                    local remainTime = sxvipRemainder - os.time()
                    if remainTime <= 0 and not XH.lobby:getModule("Sxvip"):getTouchIconStatus() then --过期未点击
                        imgBubble:setVisible(true)
                        imgBubble:setContentSize(cc.size(180, imgBubble:getContentSize().height))
                        textBubble:setString("特权已到期")
                    elseif remainTime > 0 and remainTime < 86400 then -- 最后一天
                        imgBubble:setVisible(true)
                        imgBubble:setContentSize(cc.size(260, imgBubble:getContentSize().height))
                        textBubble:setString("会员权益即将到期")
                    end
                end
            end
        end
    else
        self.KWA_BTN_VIP:setVisible(false)
    end
end

function LobbyView:onTouchEventGoldBankrupt(send, eventType)
    local block_label = "金币场大厅"
    if self._curLobbyType == XH.LOBBY_TYPE.YUEJU then
        block_label = "大厅"
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.jblb2024052902, {page_label = "破产礼包", block_label = block_label})
    XH.lobby:getModule("YGiftBankruptcy"):showBankruptcy(self._curLobbyType)
end

function LobbyView:onTouchEventDefeat(send, eventType)
    local block_label = "金币场大厅"
    if self._curLobbyType == XH.LOBBY_TYPE.YUEJU then
        block_label = "大厅"
    end
    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.jblb2024052902, {page_label = "免赔礼包", block_label = block_label})
    XH.lobby:getModule("YGiftBankruptcy"):showGiftDefeat(nil, self._curLobbyType)
end

function LobbyView:getGoldIconByID(id)
    local tmpGoldTopMenus = self._goldTopMenuBar._allMenuIcons
    if tmpGoldTopMenus then
        for _,v in ipairs(tmpGoldTopMenus) do
            if v:getName() == tostring(id) then
                return v
            end
        end
    end
    return nil
end

-- 刷新免赔按钮
function LobbyView:onUpdateGiftDefeatStatus()
    local icons = {}
    table.insert(icons,self._mainRightMenuBar:getMenuByID(LobbyConfig.ICON_ID.GOLD_NEW_DEFEAT))
    table.insert(icons,self:getGoldIconByID(LobbyConfig.ICON_ID.GOLD_NEW_DEFEAT))
    for i, v in ipairs(icons) do
        local icon = v
        if icon then
            local info = XH.lobby:getModule("YGiftDefeat"):getPlayerActInfo()
            if info == nil then
                if cc.PLATFORM_OS_WINDOWS == cc.Application:getInstance():getTargetPlatform() then 
                    icon:setVisible(false)
                else 
                    icon:setVisible(false)
                end 
            else 
                local left = 0
                for i = 1, #info do
                    left = tonumber(info[i].period) - XH.lobby:getModule("Shop"):getServerTime()
                end
                if left <= 0 then
                    if icon:isVisible() then
                        XH.viewManager:closeView("YGiftDefeatView")
                    end
                    icon:setVisible(false)
                else 
                    if not icon:isVisible() then
                        local block_label = "金币场大厅"
                        if self._curLobbyType == XH.LOBBY_TYPE.YUEJU then
                            block_label = "大厅"
                        end
                        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.jblb2024052901, {page_label = "免赔礼包", block_label = block_label})
                    end
                    icon:setVisible(true)
                    local item = icon:getChildByName("_KW_PANEL_TIME_LEFT")
                    if item then
                        item:getChildByName("_KW_TEXT"):setString(self:getLeftTimeStr(left))
                    else
                        local tmp = self._KW_PANEL_TIME_LEFT:clone()
                        tmp:getChildByName("_KW_TEXT"):setString(self:getLeftTimeStr(left))
                        tmp:setPosition(icon:getContentSize().width / 2, icon:getContentSize().height)
                        tmp:setVisible(true)
                        icon:addChild(tmp)
                    end
                end
            end
        end
    end
    self._mainRightMenuBar:updateIconsPosition()
    self._goldTopMenuBar:updateIconsPosition()

end

-- 刷新破产按钮
function LobbyView:onUpdateBankruptcyStatus()
    local icons = {}
    table.insert(icons,self._mainRightMenuBar:getMenuByID(LobbyConfig.ICON_ID.GOLD_NEW_BANKRUPT))
    table.insert(icons,self:getGoldIconByID(LobbyConfig.ICON_ID.GOLD_NEW_BANKRUPT))
    for i, v in ipairs(icons) do
        local icon = v
        if icon then
            local info = XH.lobby:getModule("YGiftBankruptcy"):getPlayerActInfo()
            if info == nil then
                if cc.PLATFORM_OS_WINDOWS == cc.Application:getInstance():getTargetPlatform() then 
                    icon:setVisible(false)
                else 
                    icon:setVisible(false)
                end 
            else 
                local left = 0
                for i = 1, #info do
                    left = tonumber(info[i].period) - XH.lobby:getModule("Shop"):getServerTime()
                end
                if left <= 0 then
                    if icon:isVisible() then
                        XH.viewManager:closeView("GoldBaseLiveView")
                        XH.viewManager:closeView("GoldBankruptcyView")
                    end
                    icon:setVisible(false)
                else 
                    if not icon:isVisible() then
                        local block_label = "金币场大厅"
                        if self._curLobbyType == XH.LOBBY_TYPE.YUEJU then
                            block_label = "大厅"
                        end
                        XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.jblb2024052901, {page_label = "破产礼包", block_label = block_label})
                    end
                    icon:setVisible(true)
                    local item = icon:getChildByName("_KW_PANEL_TIME_LEFT")
                    if item then
                        item:getChildByName("_KW_TEXT"):setString(self:getLeftTimeStr(left))
                    else
                        local tmp = self._KW_PANEL_TIME_LEFT:clone()
                        tmp:getChildByName("_KW_TEXT"):setString(self:getLeftTimeStr(left))
                        tmp:setVisible(true)
                        tmp:setPosition(icon:getContentSize().width / 2, icon:getContentSize().height)
                        icon:addChild(tmp)
                    end
                end
            end
        end
    end
    self._mainRightMenuBar:updateIconsPosition()
    self._goldTopMenuBar:updateIconsPosition()

end

function LobbyView:getLeftTimeStr(time)
    return string.format("%02d", math.floor(time/60)) .. ":" .. string.format("%02d", math.floor(time%60))
end

function LobbyView:removeGoldSchedule()
    if self._goldListener then
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(self._goldListener)
    end
    self._goldListener = nil
end

function LobbyView:setGoldSchedule()
    self:removeGoldSchedule()
    self._goldListener = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        if not self or tolua.isnull(self) then
            return
        end
        self:onUpdateBankruptcyStatus()
        self:onUpdateGiftDefeatStatus()
    end, 1, false)
end

function LobbyView:onTouchEventAoYunAct()
    XH.viewManager:openView("AoYunView", nil, {})
end

function LobbyView:onGetConfiguration(event)
    local key = event.data.key
    if key and key == string.format(ConfigurationDefine.Config.LOBBY, XH.areaData:getLobbyID()) then
        local lobbyJsonData = XH.lobby:getModule("Configuration"):getConfigJsonData(ConfigurationDefine.Config.LOBBY, "lobby")
        if lobbyJsonData and lobbyJsonData.openMatch then
            XH.areaData:setIsSupportMatch(true)
        else
            XH.areaData:setIsSupportMatch(false)
        end
        self:initMatchUI()
    end
end

function LobbyView:initMatchUI()
    if not cc.SpriteFrameCache:getInstance():isSpriteFramesWithFileLoaded("cocosStudio/hall/Image/lobby_match.plist") then
        cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/hall/Image/lobby_match.plist")
        cc.SpriteFrameCache:getInstance():addSpriteFrames("cocosStudio/hall/Image/teahouse_match.plist")
    end
    if XH.areaData:isSupportMatch() then
        self._KW_BG_CHANGE_IMG_2:loadTexture("lobby_title_yueju_match.png", ccui.TextureResType.plistType)
        self.KWA_BTND_TEA_HOUSE:loadTextures("lobby_btn_match.png", "lobby_btn_match.png", "lobby_btn_match.png", ccui.TextureResType.plistType)
    end
end

return LobbyView�� 