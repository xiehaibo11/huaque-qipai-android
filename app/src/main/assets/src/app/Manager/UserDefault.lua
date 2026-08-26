local UserDefault = {}

local  KEY_TYPE = {
    BOOL = 1,
    INT = 2,
    STRING = 3,
    JSON = 4,
}

UserDefault.KEY_ID = {
    EXIST_ADDRESS = 1,
    PLAYER_REAL_NAME = 2,
    PLAYER_MOBILE = 3,
    PLAYER_CITY = 4,
    PLAYER_PROVINCE = 5,
    PLAYER_AREA = 6,
    PLAYER_ADDRESS = 7,
    LAST_ENTER_AREA_NAME = 8,
    BARGAIN_IS_END = 9,
    LAST_PHONE_STATE = 10,
    YOU_SUGGEST_WE_CHANGE_MANAGER_PHONE = 11,
    USER_DEFAULT_AREA_TIP = 12,
    GAMERULE_CACHE_BUTTON_STATE = 13,
    GAMERULE_CATEGORIE_SELECT = 14,
    GAMERULE_CACHE_EXIST = 15,
    GAMERULE_VERSION = 16,
    TRANSFER_BINDCHECK_STATE = 17,
    EXCHANGE_FEE_PHONE = 18,
    USERDEFAULT_ACTIVITY_TYPE = 19,
    NEED_REQ_CHANGE_BIND_RESULT = 20,
    LAST_TOUCH_ACTIVITY = 21,
    LAST_JOIN_GOLD_LEVEL_WITH_LOBBY = 22,
    LAST_JOIN_GOLD_LEVEL = 23,
    LAST_JOIN_GOLD_GAME_ID = 24,
    LAST_JOIN_GOLD_INDEX = 25,
    CHANGE_INVATE_CODE = 26,
    GET_REAL_IP_KEY = 27,
    TODAY_LOGIN_COUNT = 28,
    IS_FOREIGN_IP_KEY = 29,
    LAST_CHOOSE_GAME_ID = 31,
    KW_DATA_TRANSFER_CNT = 32,
    KW_DATA_TRANSFER_DATE = 33,
    USER_DEFAULT_AUTO_ACTIVITY = 34,
    LAST_TOUCH_INVATE = 36,
    TODAY_BACK_ACT = 37,
    TEMP_SHARE_DOWNLOAD_ADDRESS = 38,
    KW_DATA_REQ_PLAYER_BILL_TIME_HALL = 39,
    KW_DATA_REQ_PLAYER_BILL_HALL = 40,
    LOBBYID = 41,
    KW_DATA_LAST_SDK_LOGIN_TYPE = 42,
    KW_DATA_LAST_LOGIN_TYPE = 43,
    KW_DATA_LAST_LOGIN_USERID = 44,
    KW_DATA_LAST_LOGIN_PASSWORD = 45,
    KW_DATA_LAST_LOGIN_NUMBERID = 46,
    KW_DATA_LAST_LOGIN_TYPE_TMP = 47,
    KW_DATA_IS_ABROAD_SRS = 48,
    NEW_VWESION = 49,
    OLD_VWESION = 50,
    HOTUPDATE_STATES = 60,
    KW_RANK_LIST_TAB_TYPE_RECORD = 61,
    KW_RANK_LIST_DAY_TYPE_RECORD = 62,
    KW_OWNER_RECORD_NAME = 63,
    KW_ENTER_RECORD_NAME = 64,
    KW_FILTER_RECORD_NAME = 65,
    TABLE_BILL_DOWNLOAD_FINISH = 66,
    KW_DATA_REQ_PLAYER_BILL_TIME = 67,
    KW_DATA_REQ_PLAYER_BILL = 68,
    KW_DATA_INVATE_CODE = 69,
    KW_DATA_NEED_FORCE_UPDATE = 70,
    START_UPDATE_TIME = 71,
    KW_YSCL_LOGIN_XYZC = 72,
    KW_YSCL_LOGIN_YSQX = 73,
    KW_DATA_NATIONAL_DAY_ACT_CNT = 74,
    KW_LAST_REFRESH_LOBBY_BILL_TIME = 75,
    KW_CUSSERVICE_TEMP_NUMID_DATA = 76,
    KW_CUSSERVICE_TIP_SHOW = 77,
    KW_NEWUSER_ACT_HAVE_PERMISSION = 78,
    KW_NEWUSER_ACT_SHOW_TODAY = 79,
    KW_GETUI_ANDROID_MSG = 80,
    KW_NEWUSER_ACT_TIME_PROP_DEAL = 81,
	KW_PHONE_BIND_SHOP_CHOOSE = 82,
    KW_GOLD_LEVEL_MID_TIPS = 83,
    KW_GOLD_LEVEL_HIGH_TIPS = 84,
    KW_NEW_USER_GUIDE_FINISHED = 86,
    KW_SHOP_NEW_ITEMS_LIST = 87,
    KW_PERSON_NEW_ITEMS_CHOOSE = 88,
    KW_TOPON_LOBBY_WAIT_VIEW_CTN = 89,
    KW_LAST_GOLD_GAMEID = 90,
    KW_LOBBY_SHOP_CHARGE_CARD_TIP_SHOW = 91,
    KW_LOBBY_SHOP_CHARGE_GOLD_TIP_SHOW = 92,
    KW_TOPON_GOLD_AREA_DRAW_POP = 93,
    KW_PRAY_ANGIN_GET_TIPS = 94,
    KW_H5GAME_TIP_SHOW = 95,
    KW_NEW_PLAYER = 96,
    KW_NEW_PLAYER_INTINE_SHOW_TIME = 97,
    KW_LAST_LOGIN_TIME = 98, -- 最近一次登录时间
    KW_INVITE_NEW_GOLD_TIPS = 99,
    KW_BAG_NEW_PROP_TIPS = 100,
    KW_LAST_BUY_PRODUCT_ID_DIAMOND = 101,
    KW_LAST_BUY_PRODUCT_ID_CARD = 102,
    KW_LAST_BUY_PRODUCT_ID_GOLD = 103,
    KW_PERSONAL_GIFTPACK_OPEN_INFO = 104,
    KW_RN_TOURIST_LOGIN = 105,
    KW_NEW_TOPON_FEE_FIRST_OPEN = 106,
    KW_ACT_ICON_CLICKED = 107,
    KW_ACT_ICON_DEFULt = 108,
    KW_FOUR_TO_TWO_CTREATEROOM = 109,
    KW_COMPLAIN_SUBMIT = 110,
    KW_COMPLAIN_TIPS = 111,
    KW_PUSH_STATE = 112, --个推开关
    KW_FAST_VOICE_STATE = 113, --语音授权状态
    KW_FAST_VOICE_STATE_DENY = 114,--禁止语音弹窗弹出
    KW_MAP_POSITION_STATE = 115, -- 位置授权状态
    KW_MAP_POSITION_STATE_DENY = 116,--禁止位置弹窗弹出
    KW_MAP_POSITION_LAST_TIME = 117,
    KW_ANNOUNCE_VERSION = 118,
    KW_ANNOUNCE = 119,
    KW_FOUR_TO_TWO_TEA_RED = 120,
    KW_DATA_AUTO_CARD_NOTIFY_TYPE = 121, -- 自动补卡功能通知类型
    KW_ISFIRST_LOGIN = 122,-- 是否新设备第一次登录
    KW_FIRST_CREATE_TEA_HOUSE = 123,-- 第一次创建比赛场
    KW_FIRST_SET_PLAYER_JOIN = 124,-- 第一次操作玩家管理权限
    KW_LOCAL_TOKEN = 125,-- 本地缓存的token
    KW_LOCAL_TOKEN_TIME = 126,-- 本地缓存的token时效
    KW_WRITE_EXTERNAL_STORAGE_PERMISSION_STATE = 131,--存储权限允许状态
    KW_PAY_TYPE = 132,  --支付状态
    KW_WEBCOUPON_EXIST = 133,--是否有web下发的优惠券
    KW_WRITE_OFF_STATUS = 135,--注销状态
    KW_WRITE_OFF_REQ_TIME = 136,--最近请求注销状态时间
    KW_SETMODULE_GUIDE_AWARD = 137,--记录新设置界面发放奖励
    KW_RECORD_GUIDE_ENTRY = 138,--记录进入比赛场大厅入口是否通过新手引导
    KW_RRBY_AFTER_UPDATE = 139,
    KW_FIRST_LOGIN_TIME = 140,--第一次登录APP的时间
    KW_SETTING_GUIDE_STATUS = 141,--设置界面的简单引导状态
    KW_IS_APPLE_LOGIN = 142,--判断是否苹果登录
    KW_TODAY_CLICK_NEW_PLAY_TYPE_BTN = 143,
    KW_CRM_NOTICE_TIME = 144,--客服消息通知时间
    KW_CLICK_NEW_PLAY_TYPE_EXTRA_RULE = 145,
    KW_NEW_TYPE_FISRT_OPEN_CREATE_LAYER = 146,
    KW_NEW_TYPE_SHOW_EXTRA_RULE = 147,
    KW_NEW_TYPE_NOT_SELECTED_NOTICE =  148,
    KW_TODAY_CLICK_NEW_PLAY_TYPE_DRAW_BTN = 149,
    KW_NEW_TYPE_FISRT_OPEN_TEA_HOUSE_MAIN = 150,
    KW_NEW_TYPE_EXTRA_RULE_LEAD_CHANGE_PAGE = 151,
    KW_NEW_TYPE_ACT_C_POP_EVERYDAY = 152,
    KW_NEW_TYPE_ACT_B_POP_EVERYDAY = 153,
    KW_NEW_TYPE_TABLE_GUIDE = 154,
    KW_NEW_TYPE_AUTO_CREATE_MODE = 155,
    KW_NEW_TYPE_AUTO_CREATE_MODE_DEL = 156,
    KW_NEW_TYPE_AUTO_CREATE_MODE_ERR = 157,
    KW_NEW_TYPE_AUTO_CREATE_MODE_TIPS_NOT_GET_AWARD = 158,
    KW_PROMOTE_LAST_TEAID = 159,  -- 推广活动，最后进入的teaid
    KW_PROMOTE_LAST_PLAYMODE = 160, -- 推广活动，最后创建的玩法
    KW_PROMOTE_HALL_DIALOG = 161,  -- 推广活动，大厅弹窗次数
    KW_PROMOTE_TEA_DIALOG = 162,   -- 推广活动，比赛场弹窗次数
    KW_PROMOTE_TEA_OWNER_CREATE = 163,   -- 推广活动，比赛场弹窗引导创建次数
    KW_PROMOTE_USER_CREATE = 164,   --推广活动，玩家引导创建频率
    KW_PROMOTE_HALL_CLOSE = 165,   --推广活动，大厅弹窗关闭
    KW_PROMOTE_USER_CLOSE = 166,   --推广活动，玩家弹窗关闭
    KW_PLAYER_RECHARGE_FLAG = 167, --充值用户标记
    KW_LOCAL_TOKEN_50 = 168,-- 50本地缓存的token
    KW_LOCAL_TOKEN_TIME_50 = 169,
    KW_LUCKY_TASK_INFO_ID = 170,
    KW_SIGN_IN_GO_GOLD_STATE = 171,
    KW_TEAHOUSE_MAX_PLAYER_TIP = 172,
    KW_TEAHOUSE_SYSTEM_KICK_TIP = 173,
    KW_CONFIGURATION_KEY = 174,
    KW_CONFIGURATION_DATA = 175,
    KW_PRAY_GUIDE = 180,    -- 求财运引导的缓存
    KW_SHUFFLE4_TOP_TIP_KEY = 181,              -- 顶部提示显示
    KW_SHUFFLE4_TOP_TIP_DAY_KEY = 182,          -- 顶部提示当日显示次数
    KW_SHUFFLE4_DISCOUNT_DAY_CD_KEY = 183,      -- 折扣弹窗显示，天冷却
    KW_SHUFFLE4_DISCOUNT_TIP_DAY_KEY = 184,     -- 折扣弹窗当日显示次数
    KW_PRAY_GUIDE = 185,                        -- 求财运引导的缓存
    KW_PRAY_GUIDE_USE_TIP = 186,                -- 求财运引导首次使用需要弹窗
    KW_SHUFFLE4_DISCOUNT_TODAY_BUY = 187,       -- 折扣洗牌今日是否已购买
    KW_SHUFFLE4_DISCOUNT_NO_USE_CD_ROUND = 188,          -- 折扣洗牌冷却局数
    KW_LIMIT_FREE_ACT = 189,          -- 限时免费活动弹窗

    KW_PERMISSION_TIP_SHOW = 200,   -- 权限顶部提示弹窗是否显示过
    KW_PERMISSION_PHONE_STATE = 201,    -- 电话权限

    KW_TEAHOUSE_WATCH_STARTTIME = 202,  -- 旁观开始时间
    KW_SXVIP_DEFALUT_VIEW = 203,  --记忆打开页
    KW_AOYUN_ACT_VIEW = 204,  --奥运弹窗
}

function UserDefault:getKeyInfoTable()
    return {
        [UserDefault.KEY_ID.EXIST_ADDRESS] = { keyType = KEY_TYPE.BOOL , mainKey = "KW_DATA_IS_EXIST_ADDRESS_%d_%d" ,getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid" ,defaultValue = false },
        [UserDefault.KEY_ID.PLAYER_REAL_NAME] = { keyType = KEY_TYPE.STRING , mainKey = "KW_DATA_PLAYER_REAL_NAME_%d_%d" ,getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid" },
        [UserDefault.KEY_ID.PLAYER_MOBILE] = { keyType = KEY_TYPE.STRING , mainKey = "KW_DATA_PLAYER_MOBILE_%d_%d" ,getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid" },
        [UserDefault.KEY_ID.PLAYER_CITY] = { keyType = KEY_TYPE.STRING , mainKey = "KW_DATA_PLAYER_CITY_%d_%d" ,getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid" },
        [UserDefault.KEY_ID.PLAYER_PROVINCE] = { keyType = KEY_TYPE.STRING , mainKey = "KW_DATA_PLAYER_PROVINCE_%d_%d" ,getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid" },
        [UserDefault.KEY_ID.PLAYER_AREA] = { keyType = KEY_TYPE.STRING , mainKey = "KW_DATA_PLAYER_AREA_%d_%d" ,getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid" },
        [UserDefault.KEY_ID.PLAYER_ADDRESS] = { keyType = KEY_TYPE.STRING , mainKey = "KW_DATA_PLAYER_ADDRESS_%d_%d" ,getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid" },
        [UserDefault.KEY_ID.LAST_ENTER_AREA_NAME] = { keyType = KEY_TYPE.STRING , mainKey = "lastEnterAreaName:" },
        [UserDefault.KEY_ID.BARGAIN_IS_END] = { keyType = KEY_TYPE.BOOL , mainKey = "KW_DATA_BARGAIN_IS_END_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid"},
        [UserDefault.KEY_ID.LAST_PHONE_STATE] = { keyType = KEY_TYPE.STRING , mainKey = "%d-%d-bindPhoneSuccess",getFullKeyFunc = "getFullKeyByMainKeyAndNumidLobbyid"},
        [UserDefault.KEY_ID.YOU_SUGGEST_WE_CHANGE_MANAGER_PHONE] = { keyType = KEY_TYPE.STRING , mainKey = "%d%d_YouSuggestWeChangeManager",getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid"},
        [UserDefault.KEY_ID.USER_DEFAULT_AREA_TIP] = { keyType = KEY_TYPE.BOOL , mainKey = "USER_DEFAULT_AREA_TIP%d",getFullKeyFunc = "getFullKeyByMainKeyAndLobbyid"},
        [UserDefault.KEY_ID.GAMERULE_CACHE_BUTTON_STATE] = { keyType = KEY_TYPE.BOOL ,getFullKeyFunc = "getFullKey_GAMERULE_CACHE_BUTTON_STATE"},
        [UserDefault.KEY_ID.GAMERULE_CATEGORIE_SELECT] = { keyType = KEY_TYPE.INT ,getFullKeyFunc = "getFullKey_GAMERULE_CATEGORIE_SELECT"},
        [UserDefault.KEY_ID.GAMERULE_CACHE_EXIST] = { keyType = KEY_TYPE.BOOL ,getFullKeyFunc = "getFullKey_GAMERULE_CACHE_EXIST"},
        [UserDefault.KEY_ID.GAMERULE_VERSION] = { keyType = KEY_TYPE.INT ,getFullKeyFunc = "getFullKey_GAMERULE_VERSION"},
        [UserDefault.KEY_ID.TRANSFER_BINDCHECK_STATE] = { keyType = KEY_TYPE.BOOL, mainKey = "KW_TRANSFER_BINDCHECK_%d_%d" ,getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid"},
        [UserDefault.KEY_ID.EXCHANGE_FEE_PHONE] = { keyType = KEY_TYPE.STRING , mainKey = "%d-%d-exchangeFeePhone",getFullKeyFunc = "getFullKeyByMainKeyAndNumidLobbyid"},
        [UserDefault.KEY_ID.USERDEFAULT_ACTIVITY_TYPE] = {keyType = KEY_TYPE.INT, mainKey = "USERDEFAULT_ACTIVITY_TYPE" },
        [UserDefault.KEY_ID.NEED_REQ_CHANGE_BIND_RESULT] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_DATA_NEED_REQ_CHANGE_BIND_RESULT_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumid"  },
        [UserDefault.KEY_ID.LAST_TOUCH_ACTIVITY] = {keyType = KEY_TYPE.INT, mainKey = "KW_LAST_TOUCH_ACTIVITY_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumid"  },
        [UserDefault.KEY_ID.LAST_JOIN_GOLD_LEVEL_WITH_LOBBY] = {keyType = KEY_TYPE.INT, mainKey = "_KW_DATA_LAST_JOIN_GOLD_LEVEL_%d" ,getFullKeyFunc = "getFullKeyByMainKeyAndLobbyid"},
        [UserDefault.KEY_ID.LAST_JOIN_GOLD_LEVEL] = {keyType = KEY_TYPE.INT, mainKey = "KW_DATA_LAST_JOIN_GOLD_LEVEL" },
        [UserDefault.KEY_ID.LAST_JOIN_GOLD_GAME_ID] = {keyType = KEY_TYPE.INT, mainKey = "KW_DATA_LAST_JOIN_GOLD_GAME_ID" },
        [UserDefault.KEY_ID.LAST_JOIN_GOLD_INDEX] = { keyType = KEY_TYPE.INT , mainKey = "_KW_DATA_LAST_JOIN_GOLD_INDEX_%d",getFullKeyFunc = "getFullKeyByMainKeyAndLobbyid"},
        [UserDefault.KEY_ID.CHANGE_INVATE_CODE] = {keyType = KEY_TYPE.INT, mainKey = "KW_CHANGE_INVATE_CODE_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumid"  },
        [UserDefault.KEY_ID.GET_REAL_IP_KEY] = {keyType = KEY_TYPE.STRING, mainKey = "GET_REAL_IP_KEY" },
        [UserDefault.KEY_ID.TODAY_LOGIN_COUNT] = {keyType = KEY_TYPE.INT, mainKey = "KW_DATA_TODAY_LOGIN_COUNT_%s_%d",getFullKeyFunc = "getFullKeyTodayLobbyid"  },
        [UserDefault.KEY_ID.IS_FOREIGN_IP_KEY] = {keyType = KEY_TYPE.BOOL, mainKey = "IS_FOREIGN_IP_KEY" },
        [UserDefault.KEY_ID.LAST_CHOOSE_GAME_ID] = {keyType = KEY_TYPE.INT ,getFullKeyFunc = "getFullKey_LAST_CHOOSE_GAME_ID" },
        [UserDefault.KEY_ID.KW_DATA_TRANSFER_CNT] = {keyType = KEY_TYPE.INT, mainKey = "KW_DATA_TRANSFER_CNT%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumid"  },
        [UserDefault.KEY_ID.KW_DATA_TRANSFER_DATE] = {keyType = KEY_TYPE.STRING, mainKey = "KW_DATA_TRANSFER_DATE%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumid"  },
        [UserDefault.KEY_ID.USER_DEFAULT_AUTO_ACTIVITY] = {keyType = KEY_TYPE.STRING, mainKey = "USER_DEFAULT_AUTO_ACTIVITY%d",getFullKeyFunc = "getFullKeyByMainKeyAndLobbyid"  },
        [UserDefault.KEY_ID.LAST_TOUCH_INVATE] = {keyType = KEY_TYPE.INT, mainKey = "KW_LAST_TOUCH_INVATE_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumid"  },
        [UserDefault.KEY_ID.TODAY_BACK_ACT] = {keyType = KEY_TYPE.INT, mainKey = "KW_DATA_TODAY_BACK_ACT_%s_%d",getFullKeyFunc = "getFullKeyTodayLobbyid"  },
        [UserDefault.KEY_ID.TEMP_SHARE_DOWNLOAD_ADDRESS] = {keyType = KEY_TYPE.STRING, mainKey = "TEMP_SHARE_DOWNLOAD_ADDRESS" },
        [UserDefault.KEY_ID.KW_DATA_REQ_PLAYER_BILL_TIME_HALL] = {keyType = KEY_TYPE.STRING, mainKey = "KW_DATA_REQ_PLAYER_BILL_TIME_HALL%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumid"  },
        [UserDefault.KEY_ID.KW_DATA_REQ_PLAYER_BILL_HALL] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_DATA_REQ_PLAYER_BILL_HALL%s%d",getFullKeyFunc = "getFullKey_KW_DATA_REQ_PLAYER_BILL_HALL"  },
        [UserDefault.KEY_ID.LOBBYID] = {keyType = KEY_TYPE.INT, mainKey = "LOBBYID" },
        [UserDefault.KEY_ID.KW_DATA_LAST_SDK_LOGIN_TYPE] = {keyType = KEY_TYPE.INT, mainKey = "KW_DATA_LAST_SDK_LOGIN_TYPE" },
        [UserDefault.KEY_ID.KW_DATA_LAST_LOGIN_TYPE] = {keyType = KEY_TYPE.INT, mainKey = "KW_DATA_LAST_LOGIN_TYPE" },
        [UserDefault.KEY_ID.KW_DATA_LAST_LOGIN_USERID] = {keyType = KEY_TYPE.STRING, mainKey = "KW_DATA_LAST_LOGIN_USERID" },
        [UserDefault.KEY_ID.KW_DATA_LAST_LOGIN_PASSWORD] = {keyType = KEY_TYPE.STRING, mainKey = "KW_DATA_LAST_LOGIN_PASSWORD" },
        [UserDefault.KEY_ID.KW_DATA_LAST_LOGIN_NUMBERID] = {keyType = KEY_TYPE.STRING, mainKey = "KW_DATA_LAST_LOGIN_NUMBERID" },
        [UserDefault.KEY_ID.KW_DATA_LAST_LOGIN_TYPE_TMP] = {keyType = KEY_TYPE.INT, mainKey = "KW_DATA_LAST_LOGIN_TYPE_TMP" },
        [UserDefault.KEY_ID.KW_DATA_IS_ABROAD_SRS] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_DATA_IS_ABROAD_SRS_" ,getFullKeyFunc = "getFullKeyByMainKeyAndLobbyid"},
        [UserDefault.KEY_ID.NEW_VWESION] = {keyType = KEY_TYPE.STRING, mainKey = "_newVersion_" ,getFullKeyFunc = "getFullKeyByMainKeyAndPreKey"},
        [UserDefault.KEY_ID.OLD_VWESION] = {keyType = KEY_TYPE.STRING, mainKey = "_oldVersion_" ,getFullKeyFunc = "getFullKeyByMainKeyAndPreKey"},
        [UserDefault.KEY_ID.HOTUPDATE_STATES] = {keyType = KEY_TYPE.INT, mainKey = "_hotUpdateState_" ,getFullKeyFunc = "getFullKeyByMainKeyAndPreKey"},
        [UserDefault.KEY_ID.KW_RANK_LIST_TAB_TYPE_RECORD] = {keyType = KEY_TYPE.INT, mainKey = "KW_RANK_LIST_TAB_TYPE_RECORD" },
        [UserDefault.KEY_ID.KW_RANK_LIST_DAY_TYPE_RECORD] = {keyType = KEY_TYPE.INT, mainKey = "KW_RANK_LIST_DAY_TYPE_RECORD" },
        [UserDefault.KEY_ID.KW_OWNER_RECORD_NAME] = {keyType = KEY_TYPE.STRING, mainKey = "KW_OWNER_RECORD_NAME_%d_%d",getFullKeyFunc = "getFullKey_TeaHouseStorageTool" },
        [UserDefault.KEY_ID.KW_ENTER_RECORD_NAME] = {keyType = KEY_TYPE.STRING, mainKey = "KW_ENTER_RECORD_NAME_%d_%d",getFullKeyFunc = "getFullKey_TeaHouseStorageTool" },
        [UserDefault.KEY_ID.KW_FILTER_RECORD_NAME] = {keyType = KEY_TYPE.STRING, mainKey = "KW_FILTER_RECORD_NAME_%d_%d",getFullKeyFunc = "getFullKey_TeaHouseStorageTool" },
        [UserDefault.KEY_ID.TABLE_BILL_DOWNLOAD_FINISH] = {keyType = KEY_TYPE.BOOL ,getFullKeyFunc = "getFullKey_TABLE_BILL_DOWNLOAD_FINISH"},
        [UserDefault.KEY_ID.KW_DATA_REQ_PLAYER_BILL_TIME] = {keyType = KEY_TYPE.STRING, mainKey = "KW_DATA_REQ_PLAYER_BILL_TIME%d" , getFullKeyFunc = "getFullKeyByMainKeyAndNumid"},
        [UserDefault.KEY_ID.KW_DATA_REQ_PLAYER_BILL] = {keyType = KEY_TYPE.BOOL ,getFullKeyFunc = "getFullKey_KW_DATA_REQ_PLAYER_BILL"},
        [UserDefault.KEY_ID.KW_DATA_INVATE_CODE] = {keyType = KEY_TYPE.INT, mainKey = "KW_DATA_INVATE_CODE_%d" , getFullKeyFunc = "getFullKeyByMainKeyAndNumid"},
        [UserDefault.KEY_ID.KW_DATA_NEED_FORCE_UPDATE] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_DATA_NEED_FORCE_UPDATE", getFullKeyFunc = "getFullKeyByMainKeyAndSufKey"},
        [UserDefault.KEY_ID.START_UPDATE_TIME] = {keyType = KEY_TYPE.STRING, mainKey = "_startUpdateTime_", getFullKeyFunc = "getFullKeyByMainKeyAndPreKey"},
        [UserDefault.KEY_ID.KW_YSCL_LOGIN_XYZC] = {keyType = KEY_TYPE.INT, mainKey = "KW_YSCL_LOGIN_XYZC" },
        [UserDefault.KEY_ID.KW_YSCL_LOGIN_YSQX] = {keyType = KEY_TYPE.INT, mainKey = "KW_YSCL_LOGIN_YSQX" },
        [UserDefault.KEY_ID.KW_DATA_NATIONAL_DAY_ACT_CNT] = {keyType = KEY_TYPE.INT, mainKey = "KW_DATA_NATIONAL_DAY_ACT_CNT_%s_%d",getFullKeyFunc = "getFullKeyTodayLobbyid"  },
        [UserDefault.KEY_ID.KW_LAST_REFRESH_LOBBY_BILL_TIME] = {keyType = KEY_TYPE.INT, mainKey = "KW_REFRESH_LOBBY_BILL_TIME_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidLobbyid"  },
        [UserDefault.KEY_ID.KW_CUSSERVICE_TEMP_NUMID_DATA] = {keyType = KEY_TYPE.STRING, mainKey = "KW_CUSSERVICE_TEMP_NUMID_DATA"},
        [UserDefault.KEY_ID.KW_CUSSERVICE_TIP_SHOW] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_CUSSERVICE_TIP_SHOW"},
        [UserDefault.KEY_ID.KW_PHONE_BIND_SHOP_CHOOSE] = { keyType = KEY_TYPE.INT , mainKey = "%s-%d-%dbindShopChooseSuccess",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid"},
        [UserDefault.KEY_ID.KW_NEWUSER_ACT_HAVE_PERMISSION] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEWUSER_ACT_HAVE_PERMISSION_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidLobbyid"},
        [UserDefault.KEY_ID.KW_NEWUSER_ACT_SHOW_TODAY] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEWUSER_ACT_SHOW_TODAY_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid"},
        [UserDefault.KEY_ID.KW_GETUI_ANDROID_MSG] = {keyType = KEY_TYPE.STRING, mainKey = "KW_GETUI_ANDROID_MSG"},
        [UserDefault.KEY_ID.KW_NEWUSER_ACT_TIME_PROP_DEAL] = {keyType = KEY_TYPE.STRING, mainKey = "KW_NEWUSER_ACT_TIME_PROP_DEAL_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidLobbyid"},
        [UserDefault.KEY_ID.KW_GOLD_LEVEL_MID_TIPS] = {keyType = KEY_TYPE.INT, mainKey = "KW_GOLD_LEVEL_MID_TIPS_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid"},
        [UserDefault.KEY_ID.KW_GOLD_LEVEL_HIGH_TIPS] = {keyType = KEY_TYPE.INT, mainKey = "KW_GOLD_LEVEL_HIGH_TIPS_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid"},
        [UserDefault.KEY_ID.KW_NEW_USER_GUIDE_FINISHED] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEW_USER_GUIDE_FINISHED_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid"},
        [UserDefault.KEY_ID.KW_SHOP_NEW_ITEMS_LIST] = { keyType = KEY_TYPE.STRING , mainKey = "_KW_SHOP_NEW_ITEMS_LIST_%d",getFullKeyFunc = "getFullKeyByMainKeyAndLobbyid"},
        [UserDefault.KEY_ID.KW_PERSON_NEW_ITEMS_CHOOSE] = { keyType = KEY_TYPE.STRING , mainKey = "_KW_PERSON_NEW_ITEMS_CHOOSE_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidLobbyid"},
        [UserDefault.KEY_ID.KW_TOPON_LOBBY_WAIT_VIEW_CTN] = {keyType = KEY_TYPE.INT, mainKey = "KW_TOPON_LOBBY_WAIT_VIEW_CTN_NEW_%s_%d",getFullKeyFunc = "getFullKeyTodayLobbyid"},
        [UserDefault.KEY_ID.KW_LAST_GOLD_GAMEID] = {keyType = KEY_TYPE.INT, mainKey = "KW_LAST_GOLD_GAMEID_%d" ,getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid"},
        [UserDefault.KEY_ID.KW_LOBBY_SHOP_CHARGE_CARD_TIP_SHOW] = { keyType = KEY_TYPE.BOOL , mainKey = "_KW_LOBBY_SHOP_CHARGE_CARD_TIP_SHOW_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidLobbyid"},
        [UserDefault.KEY_ID.KW_LOBBY_SHOP_CHARGE_GOLD_TIP_SHOW] = { keyType = KEY_TYPE.BOOL , mainKey = "_KW_LOBBY_SHOP_CHARGE_GOLD_TIP_SHOW_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidLobbyid"},
        [UserDefault.KEY_ID.KW_TOPON_GOLD_AREA_DRAW_POP] = {keyType = KEY_TYPE.INT, mainKey = "KW_TOPON_GOLD_AREA_DRAW_POP_NEW_%s_%d",getFullKeyFunc = "getFullKeyTodayLobbyid"},
        [UserDefault.KEY_ID.KW_H5GAME_TIP_SHOW] = {keyType = KEY_TYPE.INT, mainKey = "_KW_H5GAME_TIP_SHOW_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidLobbyid"},
        [UserDefault.KEY_ID.KW_PRAY_ANGIN_GET_TIPS] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_PRAY_ANGIN_GET_TIPS_%d" , getFullKeyFunc = "getFullKeyByMainKeyAndNumid"},
        [UserDefault.KEY_ID.KW_NEW_PLAYER] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEW_PLAYER_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid"},
        [UserDefault.KEY_ID.KW_NEW_PLAYER_INTINE_SHOW_TIME] = {keyType = KEY_TYPE.INT, mainKey = "KW_NEW_PLAYER_INTINE_SHOW_TIME_%s_%d",getFullKeyFunc = "getFullKeyTodayLobbyid"},
        [UserDefault.KEY_ID.KW_LAST_LOGIN_TIME] = {keyType = KEY_TYPE.INT, mainKey = "KW_LAST_LOGIN_TIME_%d" , getFullKeyFunc = "getFullKeyByMainKeyAndNumid"},
        [UserDefault.KEY_ID.KW_INVITE_NEW_GOLD_TIPS] = {keyType = KEY_TYPE.INT, mainKey = "KW_INVITE_NEW_GOLD_TIPS_%s_%d",getFullKeyFunc = "getFullKeyTodayLobbyid"},
        [UserDefault.KEY_ID.KW_RN_TOURIST_LOGIN] = { keyType = KEY_TYPE.BOOL ,mainKey = "KW_RN_TOURIST_LOGIN"},
        [UserDefault.KEY_ID.KW_BAG_NEW_PROP_TIPS] = {keyType = KEY_TYPE.INT, mainKey = "KW_BAG_NEW_PROP_TIPS_%s_%d",getFullKeyFunc = "getFullKeyTodayLobbyid"},
        [UserDefault.KEY_ID.KW_LAST_BUY_PRODUCT_ID_DIAMOND] = {keyType = KEY_TYPE.INT, mainKey = "KW_LAST_BUY_PRODUCT_ID_DIAMOND_%s_%d",getFullKeyFunc = "getFullKeyTodayLobbyid"},
        [UserDefault.KEY_ID.KW_LAST_BUY_PRODUCT_ID_CARD] = {keyType = KEY_TYPE.INT, mainKey = "KW_LAST_BUY_PRODUCT_ID_CARD_%s_%d",getFullKeyFunc = "getFullKeyTodayLobbyid"},
        [UserDefault.KEY_ID.KW_LAST_BUY_PRODUCT_ID_GOLD] = {keyType = KEY_TYPE.INT, mainKey = "KW_LAST_BUY_PRODUCT_ID_GOLD_%s_%d",getFullKeyFunc = "getFullKeyTodayLobbyid"},
        [UserDefault.KEY_ID.KW_PERSONAL_GIFTPACK_OPEN_INFO] = {keyType = KEY_TYPE.STRING, mainKey = "KW_PERSONAL_GIFTPACK_LAST_OPEN_TIME_%d" , getFullKeyFunc = "getFullKeyByMainKeyAndNumid"},
        [UserDefault.KEY_ID.KW_NEW_TOPON_FEE_FIRST_OPEN] = {keyType = KEY_TYPE.INT, mainKey = "KW_NEW_TOPON_FEE_FIRST_OPEN_%d" , getFullKeyFunc = "getFullKeyByMainKeyAndNumid"},
        [UserDefault.KEY_ID.KW_ACT_ICON_CLICKED] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_ACT_ICON_CLICKED", getFullKeyFunc = "getFullKeyByMainKeyAndSufKey"},
        [UserDefault.KEY_ID.KW_ACT_ICON_DEFULt] = {keyType = KEY_TYPE.STRING, mainKey = "KW_ACT_ICON_DEFULt_%d" , getFullKeyFunc = "getFullKeyByMainKeyAndNumid"},
        [UserDefault.KEY_ID.KW_FOUR_TO_TWO_CTREATEROOM] = {keyType = KEY_TYPE.STRING, mainKey = "KW_FOUR_TO_TWO_CTREATEROOM_%d_%d" , getFullKeyFunc = "getFullKeyByMainKeyAndNumidLobbyid"},
        [UserDefault.KEY_ID.KW_ANNOUNCE_VERSION] = {keyType = KEY_TYPE.STRING, mainKey = "KW_ANNOUNCE_VERSION", getFullKeyFunc = "getFullKeyByMainKeyAndSufKey"},
        [UserDefault.KEY_ID.KW_ANNOUNCE] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_ANNOUNCE_", getFullKeyFunc = "getFullKeyByMainKeyAndSufKey"},
        [UserDefault.KEY_ID.KW_PUSH_STATE] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_PUSH_STATE"},
        [UserDefault.KEY_ID.KW_FAST_VOICE_STATE] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_FAST_VOICE_STATE"},
        [UserDefault.KEY_ID.KW_FAST_VOICE_STATE_DENY] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_FAST_VOICE_STATE_DENY"},
        [UserDefault.KEY_ID.KW_MAP_POSITION_STATE] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_MAP_POSITION_STATE"},
        [UserDefault.KEY_ID.KW_MAP_POSITION_STATE_DENY] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_MAP_POSITION_STATE_DENY"},
        [UserDefault.KEY_ID.KW_MAP_POSITION_LAST_TIME] = {keyType = KEY_TYPE.INT, mainKey = "KW_MAP_POSITION_LAST_TIME"},
        [UserDefault.KEY_ID.KW_COMPLAIN_SUBMIT] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_COMPLAIN_SUBMIT"},
        [UserDefault.KEY_ID.KW_COMPLAIN_TIPS] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_COMPLAIN_TIPS_%s_%d",getFullKeyFunc = "getFullKeyTodayLobbyid"},
        [UserDefault.KEY_ID.KW_DATA_AUTO_CARD_NOTIFY_TYPE] = {keyType = KEY_TYPE.INT, mainKey = "KW_DATA_AUTO_CARD_NOTIFY_TYPE_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid"},
        [UserDefault.KEY_ID.KW_FOUR_TO_TWO_TEA_RED] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_FOUR_TO_TWO_TEA_RED_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid"},
        [UserDefault.KEY_ID.KW_ISFIRST_LOGIN] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_ISFIRST_LOGIN"},
        [UserDefault.KEY_ID.KW_FIRST_CREATE_TEA_HOUSE] = {keyType = KEY_TYPE.INT, mainKey = "KW_FIRST_CREATE_TEA_HOUSE_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidLobbyid"},
        [UserDefault.KEY_ID.KW_FIRST_SET_PLAYER_JOIN] = {keyType = KEY_TYPE.INT, mainKey = "KW_FIRST_SET_PLAYER_JOIN_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidLobbyid"},
        [UserDefault.KEY_ID.KW_LOCAL_TOKEN] = {keyType = KEY_TYPE.STRING, mainKey = "KW_LOCAL_TOKEN_%d", getFullKeyFunc = "getFullKeyByMainKeyAndNumid" },
        [UserDefault.KEY_ID.KW_LOCAL_TOKEN_TIME] = {keyType = KEY_TYPE.INT, mainKey = "KW_LOCAL_TOKEN_TIME_%d", getFullKeyFunc = "getFullKeyByMainKeyAndNumid" },
        [UserDefault.KEY_ID.KW_WRITE_EXTERNAL_STORAGE_PERMISSION_STATE] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_WRITE_EXTERNAL_STORAGE_PERMISSION_STATE"},
        [UserDefault.KEY_ID.KW_PAY_TYPE] = {keyType = KEY_TYPE.STRING, mainKey = "KW_PAY_TYPE"},
        [UserDefault.KEY_ID.KW_WEBCOUPON_EXIST] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_WEBCOUPON_EXIST"},
        [UserDefault.KEY_ID.KW_WRITE_OFF_STATUS] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_WRITE_OFF_STATUS_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumid"},
        [UserDefault.KEY_ID.KW_WRITE_OFF_REQ_TIME] = {keyType = KEY_TYPE.INT, mainKey = "KW_WRITE_OFF_REQ_TIME_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid"},
        [UserDefault.KEY_ID.KW_SETMODULE_GUIDE_AWARD] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_SETMODULE_GUIDE_AWARD_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidLobbyid"},
        [UserDefault.KEY_ID.KW_RECORD_GUIDE_ENTRY] = { keyType = KEY_TYPE.BOOL ,mainKey = "KW_RECORD_GUIDE_ENTRY"},
        [UserDefault.KEY_ID.KW_RRBY_AFTER_UPDATE] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_RRBY_AFTER_UPDATE"},
        [UserDefault.KEY_ID.KW_FIRST_LOGIN_TIME] = {keyType = KEY_TYPE.INT, mainKey = "KW_FIRST_LOGIN_TIME"},
        [UserDefault.KEY_ID.KW_SETTING_GUIDE_STATUS] = {keyType = KEY_TYPE.INT, mainKey = "KW_SETTING_GUIDE_STATUS"},
        [UserDefault.KEY_ID.KW_IS_APPLE_LOGIN] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_IS_APPLE_LOGIN"},
        [UserDefault.KEY_ID.KW_TODAY_CLICK_NEW_PLAY_TYPE_BTN] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_TODAY_CLICK_NEW_PLAY_TYPE_BTN_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid"},
        [UserDefault.KEY_ID.KW_CRM_NOTICE_TIME] = { keyType = KEY_TYPE.INT , mainKey = "KW_DATA_CRM_NOTICE_TIME_%d_%d" ,getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid" },
        [UserDefault.KEY_ID.KW_CLICK_NEW_PLAY_TYPE_EXTRA_RULE] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_CLICK_NEW_PLAY_TYPE_EXTRA_RULE", getFullKeyFunc = "getFullKeyByMainKeyAndSufKey"},
        [UserDefault.KEY_ID.KW_NEW_TYPE_FISRT_OPEN_CREATE_LAYER] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEW_TYPE_FISRT_OPEN_CREATE_LAYER", getFullKeyFunc = "getFullKeyByMainKeyAndSufKey"},
        [UserDefault.KEY_ID.KW_NEW_TYPE_SHOW_EXTRA_RULE] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEW_TYPE_SHOW_EXTRA_RULE", getFullKeyFunc = "getFullKeyByMainKeyAndSufKey"},
        [UserDefault.KEY_ID.KW_NEW_TYPE_NOT_SELECTED_NOTICE] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEW_TYPE_NOT_SELECTED_NOTICE_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumid"},
        [UserDefault.KEY_ID.KW_TODAY_CLICK_NEW_PLAY_TYPE_DRAW_BTN] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_TODAY_CLICK_NEW_PLAY_TYPE_DRAW_BTN_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid"},
        [UserDefault.KEY_ID.KW_NEW_TYPE_FISRT_OPEN_TEA_HOUSE_MAIN] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEW_TYPE_FISRT_OPEN_TEA_HOUSE_MAIN_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumid"},
        [UserDefault.KEY_ID.KW_NEW_TYPE_EXTRA_RULE_LEAD_CHANGE_PAGE] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEW_TYPE_EXTRA_RULE_LEAD_CHANGE_PAGE_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumid"},
        [UserDefault.KEY_ID.KW_NEW_TYPE_ACT_C_POP_EVERYDAY] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEW_TYPE_ACT_C_POP_EVERYDAY_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid" },
        [UserDefault.KEY_ID.KW_NEW_TYPE_ACT_B_POP_EVERYDAY] = {keyType = KEY_TYPE.INT, mainKey = "KW_NEW_TYPE_ACT_B_POP_EVERYDAY_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid" },
        [UserDefault.KEY_ID.KW_NEW_TYPE_TABLE_GUIDE] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEW_TYPE_TABLE_GUIDE", getFullKeyFunc = "getFullKeyByMainKeyAndSufKey"},
        [UserDefault.KEY_ID.KW_NEW_TYPE_AUTO_CREATE_MODE] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEW_TYPE_AUTO_CREATE_MODE", getFullKeyFunc = "getFullKeyByMainKeyAndSufKey"},
        [UserDefault.KEY_ID.KW_NEW_TYPE_AUTO_CREATE_MODE_DEL] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEW_TYPE_AUTO_CREATE_MODE_DEL", getFullKeyFunc = "getFullKeyByMainKeyAndSufKey"},
        [UserDefault.KEY_ID.KW_NEW_TYPE_AUTO_CREATE_MODE_ERR] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEW_TYPE_AUTO_CREATE_MODE_ERR", getFullKeyFunc = "getFullKeyByMainKeyAndSufKey"},
        [UserDefault.KEY_ID.KW_NEW_TYPE_AUTO_CREATE_MODE_TIPS_NOT_GET_AWARD] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_NEW_TYPE_AUTO_CREATE_MODE_TIPS_NOT_GET_AWARD", getFullKeyFunc = "getFullKeyByMainKeyAndSufKey"},
        [UserDefault.KEY_ID.KW_PROMOTE_LAST_TEAID] = {keyType = KEY_TYPE.INT, mainKey = "KW_PROMOTE_LAST_TEAID_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid" },
        [UserDefault.KEY_ID.KW_PROMOTE_LAST_PLAYMODE] = {keyType = KEY_TYPE.STRING, mainKey = "KW_PROMOTE_LAST_PLAYMODE_%d_%d",getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid" },
        [UserDefault.KEY_ID.KW_PROMOTE_HALL_DIALOG] = {keyType = KEY_TYPE.INT, mainKey = "KW_PROMOTE_HALL_DIALOG_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid" },
        [UserDefault.KEY_ID.KW_PROMOTE_TEA_DIALOG] = {keyType = KEY_TYPE.INT, mainKey = "KW_PROMOTE_TEA_DIALOG_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid" },
        [UserDefault.KEY_ID.KW_PROMOTE_TEA_OWNER_CREATE] = {keyType = KEY_TYPE.STRING, mainKey = "KW_PROMOTE_TEA_OWNER_CREATE_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid" },
        [UserDefault.KEY_ID.KW_PROMOTE_USER_CREATE] = {keyType = KEY_TYPE.STRING, mainKey = "KW_PROMOTE_USER_CREATE_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid" },
        [UserDefault.KEY_ID.KW_PROMOTE_HALL_CLOSE] = {keyType = KEY_TYPE.INT, mainKey = "KW_PROMOTE_HALL_CLOSE_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid" },
        [UserDefault.KEY_ID.KW_PROMOTE_USER_CLOSE] = {keyType = KEY_TYPE.INT, mainKey = "KW_PROMOTE_USER_CLOSE_%s_%d_%d",getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid" },
        [UserDefault.KEY_ID.KW_PLAYER_RECHARGE_FLAG] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_PLAYER_RECHARGE_FLAG_%d_%d", getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid"},
        [UserDefault.KEY_ID.KW_LOCAL_TOKEN_50] = {keyType = KEY_TYPE.STRING, mainKey = "KW_LOCAL_TOKEN_50_%d_%d", getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid" },
        [UserDefault.KEY_ID.KW_LOCAL_TOKEN_TIME_50] = {keyType = KEY_TYPE.INT, mainKey = "KW_LOCAL_TOKEN_TIME_50_%d_%d", getFullKeyFunc = "getFullKeyByMainKeyAndNumidAreaid" },
        [UserDefault.KEY_ID.KW_LUCKY_TASK_INFO_ID] = {keyType = KEY_TYPE.INT, mainKey = "KW_LUCKY_TASK_INFO_ID_%s_%d_%d", getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid" },
        [UserDefault.KEY_ID.KW_SIGN_IN_GO_GOLD_STATE] = {keyType = KEY_TYPE.INT, mainKey = "KW_SIGN_IN_GO_GOLD_STATE_%s_%d_%d", getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid" },
        [UserDefault.KEY_ID.KW_CONFIGURATION_KEY] = {keyType = KEY_TYPE.JSON, mainKey = "KW_CONFIGURATION_KEY_",getFullKeyFunc = "getFullKeyByMainKeyAndSufKey"},
        [UserDefault.KEY_ID.KW_CONFIGURATION_DATA] = {keyType = KEY_TYPE.JSON, mainKey = "KW_CONFIGURATION_DATA_",getFullKeyFunc = "getFullKeyByMainKeyAndSufKey"},
        [UserDefault.KEY_ID.KW_TEAHOUSE_MAX_PLAYER_TIP] = {keyType = KEY_TYPE.INT, mainKey = "KW_TEAHOUSE_MAX_PLAYER_TIP_%s_%d_%d", getFullKeyFunc = "getFullKeyTodayLobbyidAndTeaid" },
        [UserDefault.KEY_ID.KW_TEAHOUSE_SYSTEM_KICK_TIP] = {keyType = KEY_TYPE.INT, mainKey = "KW_TEAHOUSE_SYSTEM_KICK_TIP_%d_%s", getFullKeyFunc = "getFullKeyLobbyidAndExtraParam" },
        [UserDefault.KEY_ID.KW_PRAY_GUIDE] = {keyType = KEY_TYPE.STRING, mainKey = "KW_PRAY_GUIDE_%d", getFullKeyFunc = "getFullKeyByMainKeyAndLobbyid" },
        [UserDefault.KEY_ID.KW_SHUFFLE4_TOP_TIP_KEY] = {keyType = KEY_TYPE.STRING, mainKey = "KW_SHUFFLE4_TOP_TIP_KEY_%d", getFullKeyFunc = "getFullKeyByMainKeyAndLobbyid" },
        [UserDefault.KEY_ID.KW_SHUFFLE4_TOP_TIP_DAY_KEY] = {keyType = KEY_TYPE.STRING, mainKey = "KW_SHUFFLE4_TOP_TIP_DAY_KEY_%d", getFullKeyFunc = "getFullKeyByMainKeyAndLobbyid" },
        [UserDefault.KEY_ID.KW_SHUFFLE4_DISCOUNT_DAY_CD_KEY] = {keyType = KEY_TYPE.STRING, mainKey = "KW_SHUFFLE4_DISCOUNT_DAY_CD_KEY_%d", getFullKeyFunc = "getFullKeyByMainKeyAndLobbyid" },
        [UserDefault.KEY_ID.KW_SHUFFLE4_DISCOUNT_TIP_DAY_KEY] = {keyType = KEY_TYPE.STRING, mainKey = "KW_SHUFFLE4_DISCOUNT_TIP_DAY_KEY_%d", getFullKeyFunc = "getFullKeyByMainKeyAndLobbyid" },
        [UserDefault.KEY_ID.KW_PRAY_GUIDE_USE_TIP] = {keyType = KEY_TYPE.BOOL, mainKey = "KW_PRAY_GUIDE_USE_TIP_%s_%d", getFullKeyFunc = "getFullKeyTodayLobbyid" },
        [UserDefault.KEY_ID.KW_SHUFFLE4_DISCOUNT_TODAY_BUY] = {keyType = KEY_TYPE.INT, mainKey = "KW_SHUFFLE4_DISCOUNT_TODAY_BUY_%d", getFullKeyFunc = "getFullKeyByMainKeyAndLobbyid" },
        [UserDefault.KEY_ID.KW_SHUFFLE4_DISCOUNT_NO_USE_CD_ROUND] = {keyType = KEY_TYPE.INT, mainKey = "KW_SHUFFLE4_DISCOUNT_NO_USE_CD_ROUND_%d", getFullKeyFunc = "getFullKeyByMainKeyAndLobbyid" },
        [UserDefault.KEY_ID.KW_PERMISSION_TIP_SHOW] = {keyType = KEY_TYPE.INT, mainKey = "KW_PERMISSION_TIP_SHOW", getFullKeyFunc = "getFullKeyByMainKeyAndSufKey" },
        [UserDefault.KEY_ID.KW_LIMIT_FREE_ACT] = {keyType = KEY_TYPE.INT, mainKey = "KW_LIMIT_FREE_ACT_%s_%d_%d", getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid" },
        [UserDefault.KEY_ID.KW_TEAHOUSE_WATCH_STARTTIME] = {keyType = KEY_TYPE.INT, mainKey = "KW_TEAHOUSE_WATCH_STARTTIME", getFullKeyFunc = "getFullKeyByMainKeyAndSufKey" },
        [UserDefault.KEY_ID.KW_SXVIP_DEFALUT_VIEW] = {keyType = KEY_TYPE.INT, mainKey = "KW_SXVIP_DEFALUT_VIEW", getFullKeyFunc = "getFullKeyLobbyidAndExtraParam" },
        [UserDefault.KEY_ID.KW_AOYUN_ACT_VIEW] = {keyType = KEY_TYPE.INT, mainKey = "KW_AOYUN_ACT_VIEW_%s_%d_%d", getFullKeyFunc = "getFullKeyTodayLobbyidAndNumid" },
    }
end


function UserDefault:getWholeInfoByKey(key, Value, extraParam)
    local ret = {}
    local keyInfo = self:getKeyInfoTable()[key] or {}
    if keyInfo.getFullKeyFunc then
        ret.fullKey = self[keyInfo.getFullKeyFunc](self, keyInfo.mainKey, extraParam)
    else
        ret.fullKey = keyInfo.mainKey
    end
    ret.Value = Value or keyInfo.defaultValue
    ret.keyType = keyInfo.keyType
    return ret
end

---------------------------------------------------------
--@brief 设置本地缓存
--@param key 对应UserDefault.KEY_ID的成员， Value 存入值， extraParam额外参数，用于拼 fullKey的参数拓展
--@return nil
---------------------------------------------------------
function UserDefault:setValue(key, Value, extraParam)
    local wholeInfo = self:getWholeInfoByKey(key, Value, extraParam)
    local keyType = wholeInfo.keyType or 0
    local fullKey = wholeInfo.fullKey
    Value = wholeInfo.Value
    if keyType == KEY_TYPE.BOOL then
        cc.UserDefault:getInstance():setBoolForKey(fullKey,Value or false)
    elseif keyType == KEY_TYPE.INT then
        cc.UserDefault:getInstance():setIntegerForKey(fullKey,Value or 0)
    elseif keyType == KEY_TYPE.STRING then  
        cc.UserDefault:getInstance():setStringForKey(fullKey,Value or "")
    elseif keyType == KEY_TYPE.JSON then
        cc.UserDefault:getInstance():setStringForKey(fullKey,json.encode(Value) or "")
    end
end

function UserDefault:getValue(key, Value, extraParam)
    local wholeInfo = self:getWholeInfoByKey(key, Value, extraParam)
    local keyType = wholeInfo.keyType or 0
    local fullKey = wholeInfo.fullKey
    Value = wholeInfo.Value
    if keyType == KEY_TYPE.BOOL then
        return cc.UserDefault:getInstance():getBoolForKey(fullKey,Value or false)
    elseif keyType == KEY_TYPE.INT then
        return cc.UserDefault:getInstance():getIntegerForKey(fullKey,Value or 0)
    elseif keyType == KEY_TYPE.STRING then  
        return cc.UserDefault:getInstance():getStringForKey(fullKey,Value or "")
    elseif keyType == KEY_TYPE.JSON then
        local jsonInfo = cc.UserDefault:getInstance():getStringForKey(fullKey,json.encode(Value) or "")
        return json.decode(jsonInfo)
    end
end

--全key 由主key 和 当前 areaid、自身numid 组成
function UserDefault:getFullKeyByMainKeyAndNumidAreaid(mainKey)
    return string.format(mainKey, XH.areaData:getAreaID() ,XH.playerData:getNumberID() )
end

--全key 由主key 和 当前Lobbyid、自身numid 组成
function UserDefault:getFullKeyByMainKeyAndNumidLobbyid(mainKey)
    return string.format(mainKey,XH.areaData:getLobbyID(),XH.playerData:getNumberID())
end

--全key 由主key 和 当前Lobbyid 组成
function UserDefault:getFullKeyByMainKeyAndLobbyid(mainKey)
    return string.format(mainKey,XH.areaData:getLobbyID())
end

--全key 由主key 和 自身numid 组成
function UserDefault:getFullKeyByMainKeyAndNumid(mainKey)
    return string.format(mainKey, XH.playerData:getNumberID())
end

--全key 由主key和后缀拼接而成
function UserDefault:getFullKeyByMainKeyAndSufKey(mainKey, extraParam)
    local key = extraParam.key or ""
    return mainKey .. key
end

--全key 由主key和前缀拼接而成
function UserDefault:getFullKeyByMainKeyAndPreKey(mainKey, extraParam)
    local key = extraParam.key or ""
    return key .. mainKey
end

--全key 由主key和 当天时间、lobbyid组成
function UserDefault:getFullKeyTodayLobbyid(mainKey)
    return string.format(mainKey,os.date("%Y-%m-%d"),XH.areaData:getLobbyID())
end

--全key 由主key和 当天时间、lobbyid 和自身numid组成
function UserDefault:getFullKeyTodayLobbyidAndNumid(mainKey)
    return string.format(mainKey,os.date("%Y-%m-%d"),XH.areaData:getLobbyID(),XH.playerData:getNumberID())
end

--全key 由主key和 当天时间、lobbyid 和teaid组成
function UserDefault:getFullKeyTodayLobbyidAndTeaid(mainKey, extraParam)
    return string.format(mainKey,os.date("%Y-%m-%d"),XH.areaData:getLobbyID(),extraParam)
end

--全key 由主key和 lobbyid 和额外参数组成
function UserDefault:getFullKeyLobbyidAndExtraParam(mainKey, extraParam)
    return string.format(mainKey,XH.areaData:getLobbyID(),extraParam)
end


---------------------------------------------------------
--@brief 设置玩家的邮寄奖励地址
--@param table，包含玩家真实姓名手机号地址
--@return nil
---------------------------------------------------------
function UserDefault:setUserPostInfo(param)
    param = param or {}
    param.realname = param.realname or ""
    param.mobile = param.mobile or ""
    param.addr = param.addr or ""
    param.province = param.province or ""
    param.city = param.city or ""
    param.area = param.area or ""

    self:setValue(UserDefault.KEY_ID.PLAYER_REAL_NAME,param.realname)
    self:setValue(UserDefault.KEY_ID.PLAYER_MOBILE,param.mobile)
    self:setValue(UserDefault.KEY_ID.PLAYER_ADDRESS,param.addr)
end

---------------------------------------------------------
--@brief 获取玩家的邮寄奖励地址
--@param table，包含玩家真实姓名手机号地址等默认值
--@return table,包含玩家真实姓名手机号地址等信息
---------------------------------------------------------
function UserDefault:getUserPostInfo(param)
    param = param or {}
    param.realname = param.realname or ""
    param.mobile = param.mobile or ""
    param.addr = param.addr or ""
    param.province = param.province or ""
    param.city = param.city or ""
    param.area = param.area or ""

    param.realname =  self:getValue(UserDefault.KEY_ID.PLAYER_REAL_NAME,param.realname)
    param.mobile   =  self:getValue(UserDefault.KEY_ID.PLAYER_MOBILE,param.mobile)
    param.addr     =  self:getValue(UserDefault.KEY_ID.PLAYER_ADDRESS,param.addr)
    return param
end

function UserDefault:getFullKey_GAMERULE_CACHE_BUTTON_STATE(mainKey, extraParam)
    local gameID = extraParam.gameID or ""
    local categoriesIndex = extraParam.categoriesIndex
    local nodeName = extraParam.nodeName or ""
    local keyCacheInfo = extraParam.keyCacheInfo or ""

    local keyString = "gameRule".. gameID .. "_" .. nodeName .. "_1"
    if categoriesIndex then
        keyString = "gameRule".. gameID .. "_" .. nodeName .. "_" .. categoriesIndex
    end
	keyString = string.gsub(keyString, "=", "")
	keyString = string.gsub(keyString, "'", "")
    keyString = string.gsub(keyString, ";", "")
    return keyString .. keyCacheInfo
end

function UserDefault:getFullKey_GAMERULE_CATEGORIE_SELECT(mainKey, extraParam)
    local gameID = extraParam.gameID or 0
    local keyCacheInfo = extraParam.keyCacheInfo or ""
    return "gameRule".. gameID .. "_categorieSelect" .. keyCacheInfo
end

function UserDefault:getFullKey_GAMERULE_CACHE_EXIST(mainKey, extraParam)
    local gameID = extraParam.gameID
    local categoriesIndex = extraParam.categoriesIndex
    local keyCacheInfo = extraParam.keyCacheInfo or ""
    local key = "gameRule"..gameID.."cache_1"
    if categoriesIndex then
        key = "gameRule"..gameID.."cache_" .. categoriesIndex
    end
    return key .. keyCacheInfo
end

function UserDefault:getFullKey_GAMERULE_VERSION(mainKey, extraParam)
    local gameID = extraParam.gameID or 0
    local keyCacheInfo = extraParam.keyCacheInfo or ""
    return "gameRule".. gameID .. "_gameRuleVersion" .. keyCacheInfo
end

function UserDefault:getFullKey_KW_DATA_REQ_PLAYER_BILL_HALL(mainKey, extraParam)
    local date = extraParam.date or ""
    return string.format(mainKey,date,XH.playerData:getNumberID())
end

function UserDefault:getFullKey_TeaHouseStorageTool(mainKey, extraParam)
    local numid = extraParam.numid or ""
    return string.format(mainKey,XH.areaData:getAreaID(), numid)
end

function UserDefault:getFullKey_LAST_CHOOSE_GAME_ID(mainKey, extraParam)
    extraParam = extraParam or {}
    local keyCacheInfo = extraParam.keyCacheInfo or ""
    return "LAST_CHOOSE_GAME_ID".. XH.areaData:getLobbyID() .. keyCacheInfo
end

function UserDefault:getFullKey_TABLE_BILL_DOWNLOAD_FINISH(mainKey, extraParam)
    local date = extraParam.date or ""
    local teaid = extraParam.teaid or ""
    local key = "TABLE_BILL_DOWNLOAD_FINISH_" .. teaid .. "_" .. XH.areaData:getAreaID() .. "_" .. XH.playerData:getNumberID() .. "_" .. date
    return key
end

function UserDefault:getFullKey_KW_DATA_REQ_PLAYER_BILL(mainKey, extraParam)
    local date = extraParam.date or ""
    local teaNumber = extraParam.teaNumber or ""
    return "KW_DATA_REQ_PLAYER_BILL" .. date .. XH.playerData:getNumberID() .. teaNumber
end

return UserDefault ��  