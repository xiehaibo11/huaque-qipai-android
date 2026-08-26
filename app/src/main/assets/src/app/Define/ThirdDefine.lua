local ThirdDefine = {}

--微信登录回调
ThirdDefine.LOGIN_CALLBACK_CODE = {
    ACTION_RET_INIT_SUCCESS = 100,
    ACTION_RET_INIT_FAIL = 101,

    ACTION_RET_LOGIN_SUCCESS = 102,
    ACTION_RET_LOGIN_TIMEOUT = 103,
    ACTION_RET_LOGIN_NO_NEED = 104,
    ACTION_RET_LOGIN_FAIL = 105,
    ACTION_RET_LOGIN_CANCEL = 106,

    ACTION_RET_LOGOUT_SUCCESS = 107,
    ACTION_RET_LOGOUT_FAIL = 108,

    ACTION_RET_PLATFORM_ENTER = 109,
    ACTION_RET_PLATFORM_BACK = 110,

    ACTION_RET_PAUSE_PAGE = 111,
    ACTION_RET_EXIT_PAGE = 112,

    ACTION_RET_ANTIADDICTIONQUERY = 113,
    ACTION_RET_REALNAMEREGISTER = 114,

    ACTION_RET_ACCOUNTSWITCH_SUCCESS = 115,
    ACTION_RET_ACCOUNTSWITCH_FAIL = 116,

    ACTION_GET_WECHAT_INSTALLED_SUCCESS = 117,
    ACTION_GET_WECHAT_INSTALLED_FAIL = 118,
    ACTION_GET_WECHAT_INSTALLED_SUCCESS_NEW = 910,
    ACTION_GET_WECHAT_INSTALLED_FAIL_NEW = 911,

    ACTION_RET_GYLOGIN_FAIL = 7060604,
}

ThirdDefine.SYSFUNC_CALLBACK = {
    SYSFUNC_GET_CLIPBOARDCONTENT_SUCCESS = 1107
}

--支付回调
ThirdDefine.IAP_PAYRESULT = {
    PAYRESULT_SUCCESS = 200,
    PAYRESULT_FAIL = 201,
    PAYRESULT_CANCEL = 202,
    PAYRESULT_NETWORK_ERROR = 203,
    PAYRESULT_PRODUCTIONINFOR_INCOMPLETE = 204,
    PAYRESULT_INIT_SUCCESS = 205,
    PAYRESULT_INIT_FAIL = 206,
    PAYRESULT_NOW_PAYING = 207,
    PAYRESULT_AUTOORDER = 208, --收到此回调，cp要生成一个订阅类型的订单
    PAYRESULT_ABNORMAL = 210,  --交易异常
    PAYRESULT_RESTORE = 211,  --恢复购买
}

ThirdDefine.XHLINK_CALLBACK_CODE = {
    XHLINK_SUCCESS = 1601,
    XHLINK_FAIL = 1602,
}

ThirdDefine.XH_SYSFUNC_SDK_CODE = {
    SYSFUC_ADDRESS_SUCCESS = 1101, --获取经纬度成功
    SYSFUC_ADDRESS_FAIL = 1102, --获取经纬度失败
    SYSFUC_ADDRESS_FAIL_2 = 1103, --获取经纬度失败
    SYSFUC_ADDRESS_FAIL_3 = 1104, --获取经纬度失败
}

ThirdDefine.XH_SYSFUNC_PACKAGEID_CODE = {
    SYSFUC_PACKAGEID_SUCCESS = 1200, --获取packageId成功
}

ThirdDefine.XH_DEVICE_ID_CALLBACK_CODE = {
    DEVICE_ID_FAIL1 = 2208,
    DEVICE_ID_FAIL2 = 2209,
    DEVICE_ID_FAIL3 = 2210,
    DEVICE_ID_SUCCESS = 1001002,
}

ThirdDefine.OPENURL_CALLBACK_CODE = {
    BARGAIN = 10000,
}

ThirdDefine.FASTVO_CALLBACK_CODE = {
    FASTVO_RECORD_SUCCESS = 1401, --录音成功，msg 返回语音文件名 fileName
    FASTVO_RECORD_FAIL = 1402, --录音失败
    FASTVO_RECORD_UPLOAD_SUCCESS = 1403, --录音上传成功，msg 返回语音文件 url
    FASTVO_RECORD_ING = 1407, --正在录音
    FASTVO_RECORD_PLAY_SUCCESS = 1404, --录音播放成功
    FASTVO_RECORD_PLAY_FAIL = 1405, --录音播放失败
    FASTVO_RECORD_DOWNLOAD_SUCCESS = 1406, --录音下载完成，msg 返回输出地址 outFile
    FASTVO_RECORD_CANCEL = 1408 --取消录音成功
}

--分享回调
ThirdDefine.SHAREDSDK_CALLBACK_CODE = {
    WX_FLAG_SHARERESULT_SUCCESS = 801, --微信分享成功
    WX_FLAG_SHARERESULT_FAIL = 802, --微信分享失败
    WX_FLAG_SHARERESULT_DENY = 803, --微信分享拒绝权限
    WX_FLAG_SHARERESULT_UNKNOWN = 804, --微信分享未知状态
}

-- ToponSDK回调
ThirdDefine.TOPONSDK_CALLBACK_CODE = {
    TOPON_FLAG_SDK_CLICKED = 121000,-- 广告点击
    TOPON_FLAG_SDK_CLOSE = 121001,-- 广告关闭
    TOPON_FLAG_SDK_LOADFAIL = 121002,-- 加载失败
    TOPON_FLAG_SDK_SHOWSUCCESS = 121003,-- 展示成功
    TOPON_FLAG_SDK_LOADSUCCESS = 121004,-- 加载成功
    TOPON_FLAG_SDK_VIDEOSUCCESS = 121005,-- 视频播放完成
    TOPON_FLAG_SDK_JLSUCCESS = 121006,-- 激励校验成功
    TOPON_FLAG_SDK_HAVELOAD = 121007,-- 已加载过
    TOPON_FLAG_SDK_ISLOADING = 121008,-- 还在加载中
    TOPON_FLAG_SDK_LOADINGAPI = 121009,-- 调起加载api 用于埋点
    TOPON_FLAG_SDK_NOTREADY = 121010,-- not ready
    TOPON_FLAG_SDK_MDAPI = 121011,-- 调起显示api 用于埋点
    TOPON_FLAG_SDK_SHOWFAILED = 121012,-- 广告展示失败
    TOPON_FLAG_SDK_JLFAIL = 121013,-- 激励校验失败
}

-- ToponSDK广告类型
ThirdDefine.TOPONSDK_AD_TYPE = {
    REWARDEVIDEO = "rewardvideo",
    BANNER = "banner",
    SPLASHAD = "splash_ad",
}

-- 个推 回调
ThirdDefine.GETUISDK_CALLBACK_CODE = {
    --安卓用
    GETUI_FLAG_SDK_GET_CLIENT_ID = 1501,--获取clientid
    GETUI_FLAG_SDK_BIND_ALIAS = 1502,--绑定别名成功
    GETUI_FLAG_SDK_BIND_FAIL = 1503,--绑定别名失败
    GETUI_FLAG_SDK_UN_BIND_SUCCESS = 1504,--解绑别名成功
    GETUI_FLAG_SDK_UN_BIND_FAIL = 1505,--解绑别名失败
    GETUI_FLAG_SDK_GET_NOTIFY_INFO_SUCC = 1506,--获取透传参数成功
    GETUI_FLAG_SDK_GET_NOTIFY_INFO_FAIL = 1507,--获取透传参数失败
    GETUI_FLAG_SDK_GET_NOTIFY_SOURCE_SUCC = 1508, -- 
    GETUI_FLAG_SDK_GET_NOTIFY_STATE_ON = 1509, -- 开启状态
    GETUI_FLAG_SDK_GET_NOTIFY_STATE_OFF = 1510, -- 关闭状态
    GETUI_FLAG_SDK_GET_NOTIFY_EVENT_ARRIVE = 1512,  --个推消息到达时
    GETUI_FLAG_SDK_GET_NOTIFY_EVENT_CLICK = 1513,   --点击个推消息时
    --安卓极光SDK
    GETUI_FLAG_SDK_GET_CLIENT_ID_NEW = 125023,--极光获取clientid 
    GETUI_FLAG_SDK_GET_NOTIFY_EVENT_ARRIVE_NEW = 125030, --极光当消息到达时，会自动通知客户端，code为成功的code
    GETUI_FLAG_SDK_GET_NOTIFY_EVENT_CLICK_NEW = 125032,--极光点击消息时，会自动通知客户端
    --ios用
    GETUI_FLAG_SDK_GET_IOS_CLIENT_ID = 331000,  --获取clientid ios
	GETUI_FLAG_SDK_GET_IOS_MSG = 331001, --收到苹果推送的msg 离线或在线
    --极光ios用
    GETUI_FLAG_SDK_GET_IOS_CLIENT_ID_NEW = 1250023,  --获取clientid 极光
    GETUI_FLAG_SDK_GET_IOS_MSG_NEW = 1250030, --收到苹果推送的msg 离线或在线
}

ThirdDefine.GETUI_PAREM_DEFINE = {
    GETUI_SECOND_POP_IM = 1, --表示打开大厅的好友系统 
}

--高德地图
ThirdDefine.AMAP_CALLBACK_CODE = {
    AMAP_SUCCESS = 1701,
    AMAP_FAIL = 1702,
    AMAP_FAIL_NO = 1703,
}

--H5游戏
ThirdDefine.H5GAME_CALLBACK_CODE = {
    H5GAME_CLOSE = 910005,
}

ThirdDefine.ADD_PERMISSION_CALLBACK_CODE = {
    ADD_PERMISSION_SUCCESS = 85000,
    ADD_PERMISSION_FAIL = 85001,
    HAVE_PERMISSION_ALREADY = 85006,
    NOT_HAVE_PERMISSION = 85007,
    SELECTED_NOT_SHOW_PERMISSION =  85010, --勾选了禁止不再提醒
    NOT_SELECTED_NOT_SHOW_PERMISSION = 85011,
}

--H5游戏
ThirdDefine.ZFB_REALNAME_CALLBACK_CODE = {
    REALNAME_SUCCESS = 30034001,
    REALNAME_FAIL = 30034002,
}

--获取订单号
ThirdDefine.GET_ORDERID_CALLBACK_CODE = {
    ORDERID_SUCCESS = 1001003,
    ORDERID_FAIL = 1001004,
}

--人人捕鱼-乐变
ThirdDefine.LEIBIAN_CALLBACK_CODE = {
    UNKNOWERR = 137001,
    NOUPDATE = 137002,
    NOTFORCEVER = 137003,
    FORCEVER = 137004,
    REQERR = 137005,
    SDKNOTREADY = 137006,
    USELEIBIANCLOSED = 137007,
    AFTERUPDATE = 137018,
    DOWNPERCENT = 137020,
    DOWNERRREASON = 137030,
    DOWNPROGRESS = 137031,
    DOWNSIZE = 137032,
    DOWNEXTRACT = 137033,
    NEEDRESTART = 137037
}

-- replugin 2.0
ThirdDefine.REPLUGIN_CALLBACK_CODE = {
    PLUGIN_LIST_SUCCESS = 51000,
    PLUGIN_LIST_FAIL = 51001,
    PLUGIN_STATUS_SUCCESS = 52000,
    PLUGIN_STATUS_FAIL = 52001,
    PLUGIN_INSTALL_SUCCESS = 53000,
    PLUGIN_INSTALL_FAIL = 53001,
    PLUGIN_DOWNLOAD_PROCESS = 53002,
    PLUGIN_DOWNLOAD_FAIL = 53003,
    PLUGIN_INSTALL_ALREADY = 54000,
    PLUGIN_INSTALL_NOT = 54001,
    PLUGIN_START_SUCCESS = 55000,
    PLUGIN_START_FAIL = 55001,
    PLUGIN_UNINSTALL_SUCCESS = 56000,
    PLUGIN_UNINSTALL_FAIL = 56001,
    PLUGIN_PRELOAD_SUCCESS = 57000,
    PLUGIN_PRELOAD_FAIL = 57001,
    PLUGIN_QUIT_HOST = 58001,
}

ThirdDefine.APPLE_REVOKE_SESSION_CALLBACK_CODE = {
    RET_APPLE_REVOKE_SESSION_SUCCESS = 7061206,
    RET_APPLE_REVOKE_SESSION_FAIL = 7061207,
}

ThirdDefine.BAIDU_OCPC_SDK_CALLBACK_CODE = {
    SET_GET_USER_CLIPBOARD_DATA_SUCCESS = 95001,
    SET_GET_USER_CLIPBOARD_DATA_FAIL = 95002,
    REPORT_USER_DATA_SUCCESS = 95003,
    REPORT_USER_DATA_FAIL = 95004,
}

local targetPlatform = cc.Application:getInstance():getTargetPlatform()
if cc.PLATFORM_OS_ANDROID == targetPlatform or device.platform == "windows" then
    ThirdDefine.SDK_INTERFACE_NAMES = {
        -- 高德相关
        ["amap_get_location_by_sport"] = { cls = "XHAmap", func = "XH_amap_get_location:" },

        -- 语音相关
        ["fastvo_start_record"] = { cls = "XHfastvo", func = "XH_fastvo_start_record:" },
        ["fastvo_stop_record"] = { cls = "XHfastvo", func = "XH_fastvo_stop_record" },
        ["fastvo_cancel_record"] = { cls = "XHfastvo", func = "XH_fastvo_cancel_record" },
        ["fastvo_start_play"] = { cls = "XHfastvo", func = "XH_fastvo_start_play:" },
        ["fastvo_stop_play"] = { cls = "XHfastvo", func = "XH_fastvo_stop_play" },

        --分享
        ["sharesdk_show_share_wx"] = { cls = "XHsharesdk", func = "XH_sharesdk_show_share_wx:" },

        --商城
        ["iosBuy"] = { cls = "XH_IAP", func = "xhBuy:" },

        --XHLink
        ["xhlink"] = { cls = "XHlink", func = "xhlink_get_uri" },

        --剪贴板
        ["sysfunc_get_clipboardcontent"] = { cls = "XHsysfunc", func = "XH_sysfuncGetClipboardContent" },

        --微信
        ["WXLogin"] = { cls = "XHWXLogin", func = "weixinLoginIn" },

        --个验
        ["GYLogin"] = { cls = "XHGeYan", func = "gy_onetap_login" },

        --极光
        ["jglogin_login"] = { cls = "XHGeYan", func = "jg_onetap_login" },

    }

    ThirdDefine.SDK_INTERFACE_NAMES_KEY = {
        -- 高德相关
        AMP_GET = "amap_get_location_by_sport",

        -- 语音相关
        FASTVO_START = "fastvo_start_record",
        FASTVO_STOP = "fastvo_stop_record",
        FASTVO_CABCEL = "fastvo_cancel_record",
        FASTVO_START_PLAY = "fastvo_start_play",
        FASTVO_STOP_PLAY = "fastvo_stop_play",

        --分享
        WX_SHARE = "sharesdk_show_share_wx",

        --商城
        BUY = "iosBuy",

        --XHLink
        LINK = "xhlink",

        --剪贴板
        SYS_FUNC_GET = "sysfunc_get_clipboardcontent",

        --微信
        WX_LOGIN = "WXLogin",

        --个验
        GY_LOGIN = "GYLogin",

        --极光
        JG_LOGIN = "jglogin_login",
    }
else
    ThirdDefine.SDK_INTERFACE_NAMES = {
        -- 高德相关
        ["amap_get_location"] = { cls = "XHAmap", func = "XH_amap_get_location:" },

        -- 语音相关
        ["fastvo_start_record"] = { cls = "XHfastvo", func = "XH_fastvo_start_record:" },
        ["fastvo_stop_record"] = { cls = "XHfastvo", func = "XH_fastvo_stop_record" },
        ["fastvo_cancel_record"] = { cls = "XHfastvo", func = "XH_fastvo_cancel_record" },
        ["fastvo_start_play"] = { cls = "XHfastvo", func = "XH_fastvo_start_play:" },
        ["fastvo_stop_play"] = { cls = "XHfastvo", func = "XH_fastvo_stop_play" },

        --分享
        ["sharesdk_show_share_wx"] = { cls = "XHsharesdk", func = "XH_sharesdk_show_share_wx:" },
    }
end

return ThirdDefine  �+  