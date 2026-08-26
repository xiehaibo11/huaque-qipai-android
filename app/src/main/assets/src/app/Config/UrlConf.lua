local UrlConf = {}
local DEBUG = require("app.Config.GlobalConfig").IsDebug -- 灰度

UrlConf.First_Url = "https://transit.hzxuanming.com"
if DEBUG then
    UrlConf.First_Url = "http://112.126.81.124:80"
end

-- 配置中心接口
UrlConf.URL_CONFIG_DATA_MD5 = 'https://confapi.mychuguan.com/config_data/md5_list'
UrlConf.URL_CONFIG_DATA_FILE = 'https://confapi.mychuguan.com/config_data/file_info'

UrlConf.URL_PALMAPI = UrlConf.First_Url .. "/auth/palmapi"
UrlConf.URL_GOPALMAPI = UrlConf.First_Url .. "/auth/gopalmapi"
UrlConf.URL_ACT = UrlConf.First_Url .. "/auth/act"
UrlConf.URL_EXCHANGESHOP = UrlConf.First_Url .. "/auth/exchangeshop"
UrlConf.URL_APIHUB = UrlConf.First_Url .. "/auth/apihub"
-- 获取token用于后续请求凭证
UrlConf.REQ_TOKEN = UrlConf.First_Url .. "/auth/get-token?"

UrlConf.URL_PARTENTS_SERVICE = "http://www.hzxuanming.com/jiazhang/index.html"
UrlConf.URL_USER_SERVICE = "http://www.bryouxi.com/protocol.html"
UrlConf.URL_PRIVACY_PROTOCOL = "http://bryouxi.com/game_center/game_rule/1/5040/7.html"
UrlConf.URL_PRIVACY_PROTOCOL_CHILDREN = "http://bryouxi.com/game_center/game_rule/1/5040/58.html"
UrlConf.URL_PERSONAL_INFORMATION_COLLECTION_LIST = "http://bryouxi.com/game_center/game_rule/1/5040/50.html"
UrlConf.URL_THIRD_SHARE_PERSONAL_INFORMATION_COLLECTION_LIST = "http://bryouxi.com/game_center/game_rule/1/5040/51.html"
-- 充值约定
UrlConf.HTTP_ID_CHONG_ZHI = "http://www.bryouxi.com/recharge.html"

UrlConf.URL_PARTENTS_SERVICE_IOS = "http://www.hzxuanming.com/jiazhang/index.html"
UrlConf.URL_USER_SERVICE_IOS = "http://www.hzxuanming.com/protocol.html"
UrlConf.URL_PRIVACY_PROTOCOL_IOS = "http://hzxuanming.com/game_center/game_rule/0/5040/8.html"
UrlConf.URL_PRIVACY_PROTOCOL_IOS_CHILDREN = "http://hzxuanming.com/game_center/game_rule/1/5040/58.html"
UrlConf.URL_PERSONAL_INFORMATION_COLLECTION_LIST_IOS = "http://hzxuanming.com/game_center/game_rule/2/5040/52.html"
UrlConf.URL_THIRD_SHARE_PERSONAL_INFORMATION_COLLECTION_LIST_IOS = "http://hzxuanming.com/game_center/game_rule/2/5040/53.html"
-- 充值约定
UrlConf.HTTP_ID_CHONG_ZHI_IOS = "http://hzxuanming.com/recharge_protocol.html"
-- VIP续费相关
UrlConf.URL_USER_VIPPROTOCOL_IOS = "http://www.hzxuanming.com/free/xm-hyfwxy.html"
UrlConf.URL_USER_RENEWPROTOCOL_IOS = "http://www.hzxuanming.com/free/xm-zdxfxy.html"

-- 商城相关
UrlConf.URL_STORE_PRODUCT_LIST = "https://shiming.hzxuanming.com/v1/shop/products"
UrlConf.URL_TICKETSTORE_EXCHANGE_HISTORY = "https://wechat.hzxuanming.com/exchange/combine/record/list"
UrlConf.URL_TICKETSTORE_EXCHANGE_KAMI = "https://wechat.hzxuanming.com/exchange/combine/get/cty_gift_secret"
UrlConf.URL_STORE_SPECIAL_SCENE_GOODS = "https://wechat.hzxuanming.com/exchange/combine/special_scene/goods_list"

-- 身份认证相关
UrlConf.URL_REAL_NAME_GET_STATUS = "https://shiming.hzxuanming.com/v1/player/getRealName"
UrlConf.URL_REAL_NAME_DEFINE = "https://shiming.hzxuanming.com/v1/player/setRealName"
UrlConf.URL_REAL_NAME_GET_STATUS_NEW = "https://fcm.bflyhd.cn/realname/query"
UrlConf.URL_REAL_NAME_DEFINE_NEW = "https://fcm.bflyhd.cn/realname/check"

-- 规则文档
UrlConf.REQ_GAME_RULE_IMAGE = "https://palmstatic.hzxuanming.com/Download/Rule"
UrlConf.GAME_RULE_HTML_ADDR = "https://wechat.hzxuanming.com/game_center/game_rule"

-- 判断是否海外网络
UrlConf.REQ_IS_FOREIGN_IP = "https://api.hzxuanming.com/iplooker/dat/find?"

-- 下载地址
UrlConf.DOWNLOAD_ADDRESS_IOS = "https://w.url.cn/s/ApB88pJ" -- 通过短链接，后台配置，可以根据手机类型自动跳转下载地址
UrlConf.DOWNLOAD_ADDRESS_ANDROID = "https://w.url.cn/s/ApB88pJ"
UrlConf.DOWNLOAD_ADDRESS_ANDROID_2 = "https://dwz.cn/kToxpBIt" -- 版本为1.2.8

-- 砍价活动
UrlConf.REQ_GET_AWARD_RECORD = "https://act.hzxuanming.com/base/log?"
UrlConf.REQ_USER_INFO = "https://wechat.hzxuanming.com/exchange/combine/get/user/info?"
UrlConf.REQ_SET_USER_INFO = "https://wechat.hzxuanming.com/exchange/combine/edit/user/info?"

-- 浙江包迁移
UrlConf.REQ_ZJ_AGENT_TRANSINFO = "https://act.hzxuanming.com/zj_migrate/info?"
UrlConf.REQ_ZJ_AGENT_TRANSUSER = "https://act.hzxuanming.com/zj_migrate/user_list?"
UrlConf.REQ_ZJ_TRANSFER_CHECKBIND = "https://act.hzxuanming.com/zj_migrate/check_user?"

-- 礼券商城兑换话费接口
UrlConf.REQ_SHOP_FEE_PHONE_CODE_CHECK = "https://wechat.hzxuanming.com/award_center/check_phone/send_msg?"
UrlConf.REQ_SHOP_FEE_EXCHANGE = "https://wechat.hzxuanming.com/award_center/check_phone/exchange/cty_tel_fee?"

-- 客服系统
UrlConf.HTTP_ID_CUSTOMER_SERVICE = "https://kf.kkwan.cc/?"

-- 绑定功能
UrlConf.HTTP_ID_DOBIND_AGENT = "https://wechatapi.hzxuanming.com/api/agent/bind_from_aid?"
UrlConf.HTTP_ID_RELEASEBIND_AGENT = "https://wechatapi.hzxuanming.com/api/agent/unbind?"
UrlConf.HTTP_ID_GETBIND_INFO = "https://wechatapi.hzxuanming.com/api/agent/search-bind?"
UrlConf.HTTP_ID_GETAGENT_INFO = "https://wechatapi.hzxuanming.com/api/agent/info_from_aid?"

-- 浙江日报官网地址
UrlConf.HTTP_ID_ZHEJIANG_NEWS = "https://zj.zjol.com.cn/"

-- 奖励中心相关请求
UrlConf.HTTP_ID_AWARD_CENTER_RECORD = "https://wechat.hzxuanming.com/award_center/myorder?"
UrlConf.HTTP_ID_USER_ADDRESS_GET = "https://wechat.hzxuanming.com/exchange/combine/get/user/info?"
UrlConf.HTTP_ID_USER_ADDRESS_CHANGE = "https://wechat.hzxuanming.com/exchange/combine/edit/user/info?"
-- 杭麻圈分享活动
UrlConf.HTTP_ID_HANGMA_SHARE_AWARD = "https://act.hzxuanming.com/hmq_share_send/getAward?"

-- 宝宝转型Web活动地址
UrlConf.HTTP_ID_REMODEL_WEB_ACT = "https://sparkact.hzxuanming.com/baobao_transform/index"

-- 商城礼包的购买记录
UrlConf.HTTP_ID_SHOP_GIFT_EXCHANGE_RECORD = "https://wechat.hzxuanming.com/exchange/combine/get/user/gift_count"

-- 数据抛送接口地址
UrlConf.HTTP_ID_THROW_DATA = "https://bfas.hzxuanming.com/v2/client"

-- H5入口配置
UrlConf.HTTP_ID_H5_GAME_SHOE_LIST = "https://palmapi.hzxuanming.com/joint_operation/list?"

UrlConf.HTTP_ID_COMPLIANCE = "https://hegui.bflyhd.cn/"

-- 裂变活动分享链接
UrlConf.FISSION_ACT_DOWNLOAD = "http://act.bfjiaxinmj.com/fission_invite_stage/download"

UrlConf.CUSSERVER_CONFIG_DOWNLOAD = "https://weixinhlmj.37gjw.com/zhanlong/crm/clientConfig.json?"

-- 车载h5支付链接
UrlConf.H5pay_LINK = "https://trade.hzxuanming.com/v2/trade/order"

UrlConf.URL_VERIFICATION_CODE_URL_1 = "https://palmapi.hzxuanming.com/spark/phone/login/send/code/msg"
UrlConf.URL_VERIFICATION_CODE_URL_2 = "https://palmapi.hzxuanming.com/spark/phone/login/send-code"
UrlConf.REQ_VERIFICATION_PHONE_PWD_1 = "https://palmapi.hzxuanming.com/spark/phone/login/pwd/get"
UrlConf.REQ_VERIFICATION_PHONE_PWD_2 = "https://palmapi.hzxuanming.com/spark/phone/login/check-code"

-- 撤销注销（不用验证token）
UrlConf.URL_LOGOFF_DOCANCLE = "https://wechat.hzxuanming.com/api/user/logoff/docancel"

UrlConf.First_Url_50 = "https://hz-sibyl.mengyoutech.com/"
if DEBUG then
    UrlConf.First_Url_50 = "https://test-sibyl.mengyoutech.com/"
end
UrlConf.URL_AOYUN_INFO = UrlConf.First_Url_50 .. "nyx/OlympicAct"
UrlConf.URL_SIGN_DETAIL = UrlConf.First_Url_50.."nyx/SignDetail"
UrlConf.URL_SIGN = UrlConf.First_Url_50.."nyx/Sign"
UrlConf.URL_LUCK_TASK_ACTLIST = UrlConf.First_Url_50.."nyx/actList"
UrlConf.URL_LUCK_TASK_ACTDETAIL = UrlConf.First_Url_50 .. "nyx/ActDetail"
UrlConf.URL_LUCK_TASK_GETPAGE = UrlConf.First_Url_50.."nyx/getPage"
UrlConf.URL_LUCK_TASK_GETCONTENT = UrlConf.First_Url_50.."nyx/getContent"
UrlConf.URL_LUCK_TASK_GETDRAWLOGS = UrlConf.First_Url_50.."nyx/getDrawLogs"
UrlConf.URL_LUCK_TASK_GETREWARDS = UrlConf.First_Url_50.."nyx/getRewards"
UrlConf.URL_LUCK_TASK_Participate = UrlConf.First_Url_50.."nyx/participate"
UrlConf.URL_ACT_50_BANKRUPTCY_GIFT = UrlConf.First_Url_50.."nyx/GetBankruptcyGift"
UrlConf.URL_ACT_50_DEDUCTIBLE_GIFT = UrlConf.First_Url_50.."nyx/GetDeductibleGift"
UrlConf.URL_ACT_50_JOINROOM_GIFT = UrlConf.First_Url_50.."nyx/GetJoinRoomGift"
UrlConf.URL_LUCK_ARTICLE_LIST = UrlConf.First_Url_50.."themis/ArticleList"
UrlConf.URL_LUCK_ARTICLE_REPORT = UrlConf.First_Url_50.."themis/ArticleReport"

-- 普通道具 转化为时效道具
UrlConf.URL_USE_EQUIP_CARD = UrlConf.First_Url_50.."rhea/UseEquipCard"

-- sxvip 剩余时间
UrlConf.URL_SXVIP_REMAINDER = UrlConf.First_Url_50.."rhea/getPropsCount"
-- sxvip 配置
UrlConf.URL_SXVIP_CONFIG = UrlConf.First_Url_50.."hacaba/VIPConfig"
-- sxvip 牌友信息
UrlConf.URL_SXVIP_FRIEND_INFO = UrlConf.First_Url_50.."nyx/GetUserTagDrawInfo"
-- sxvip 牌局统计
UrlConf.URL_SXVIP_GAMEPLAY_STATISTICS = UrlConf.First_Url_50.."nyx/GetUserDrawInfo"
-- sxvip 领取每日奖励
UrlConf.URL_SXVIP_DAILY_REWARD = UrlConf.First_Url_50.."eos/GetVipAward"
-- sxvip 赠送索要
UrlConf.URL_SXVIP_FELLOW_SEND = UrlConf.First_Url_50.."hacaba/FellowSend"
-- sxvip 订阅商品(先订阅再扣款)，走服务接口
UrlConf.URL_SXVIP_PAY_AFTER_SIGN = UrlConf.First_Url_50.."themis/SubscriptionGoods"

UrlConf.URL_USER_LOCATION = UrlConf.First_Url_50 .. "themis/GetUserLocation"

-- 预洗牌接口
UrlConf.URL_PRE_SHUFFLE = UrlConf.First_Url_50 .. "nyx/shufflePre"
-- 洗牌5.0
UrlConf.URL_SHUFFLE5 = UrlConf.First_Url_50 .. "nyx/disCountAct"

UrlConf.URL_PROP_DISCOUNT = UrlConf.First_Url_50 .. "nyx/getPropsDiscount"
UrlConf.URL_ACCEPT_PRESENTER = UrlConf.First_Url_50 .. "nyx/acceptPresenter"

-- 会员免费使用道具
UrlConf.URL_VIP_PROP_USE_FREE = UrlConf.First_Url_50 .. "rhea/transferProps"

-- 反诈骗问答URL
UrlConf.URL_FANZHA_QUES = "https://ks.wjx.top/vm/QwBI7a2.aspx"

if DEBUG then
    UrlConf.REQ_SHOP_FEE_PHONE_CODE_CHECK = "http://appstore.racyqp.com:8090/award_center/check_phone/send_msg?"
    UrlConf.REQ_SHOP_FEE_EXCHANGE = "http://appstore.racyqp.com:8090/award_center/check_phone/exchange/cty_tel_fee?"
    UrlConf.HTTP_ID_SHOP_GIFT_EXCHANGE_RECORD = "http://appstore.racyqp.com:8090/exchange/combine/get/user/gift_count"
    UrlConf.URL_TICKETSTORE_EXCHANGE_HISTORY = "http://appstore.racyqp.com:8090/exchange/combine/record/list"
    UrlConf.URL_VERIFICATION_CODE_URL_2 = "http://47.111.89.252:8088/spark/phone/login/send-code"
    UrlConf.URL_LOGOFF_DOCANCLE = "http://appstore.racyqp.com:8090/api/user/logoff/docancel"
    UrlConf.REQ_VERIFICATION_PHONE_PWD_2 = "https://47.111.89.252:8088/spark/phone/login/check-code"
end

UrlConf.URL_SECOND_DOMAIN = {
    REQ_CACHE_WINDOW = "common/popup/list", -- 旧的弹窗列表
    REQ_WEB_POP_LIST = "common/popup_combo/list", -- 新的弹窗列表
    REQ_NOTIFY_INFO = "bulletin/common/list", -- 公告
    REQ_DYNAMIC_URL = "app_url/all", -- 所有区走风信后台进行分享链接的配置
    REQ_H5_WECAHT_USERID = "h5game/login/get_user_id", -- 请求H5 游戏 微信注册的UserID接口
    REQ_H5_WECAHT_ALL_USERID = "h5game/login/get_all_user_id", -- 获取所有web维护的联运游戏id
    REQ_LEISURE_ENTRANCE = "resource_state/entrance/list", -- 请求大厅休闲场入口包括联运入口
    REQ_WEB_ICONS = "resource_state/all_list", -- 请求大厅Icon
    REQ_INVITE_NEW_GET_ALL_AWARD = "fission_invite_stage/get_all_award", -- 裂变活动
    REQ_INVITE_NEW_GET_AWARD = "fission_invite_stage/get_award",
    REQ_INVITE_NEW_HISTORY = "fission_invite_stage/get_invite_log",
    REQ_INVITE_NEW_INFO = "fission_invite_stage/get_all_status",
    REQ_INVITE_NEW_TIPS = "fission_invite_stage/get_login_player_num",
    REQ_HANGMA_SHARE_ACT = "hmq_share_send/info", -- 杭麻圈分享活动
    REQ_VERIFICATION_PHONE_BIND = "spark/phone/login/session/bind", -- 手机号绑定
    REQ_PHONE_VERIFCATION_STATE = "spark/phone/login/user/info",
    REQ_VERIFICATION_PHONE_REBIND = "spark/phone/login/session/rebind",
    REQ_PHONE_VERIFY_CODE = "common/send_verify_msg",
    REQ_POOLS_AID_PID = "base/pool", -- 请求奖池某一期数所有奖池
    REQ_BUSINESS_CONSUME_COST = "base/tea_house_cost", -- B端消耗活动 卡消耗数据
    REQ_BUSINESS_MORE_COST = "base/tea_house_game_cost", -- B端消耗活动 卡消耗数据 区分人数
    REQ_TEAHOUSE_RECALL_LISTS = "tea_house_recall/play_info", -- 请求未召回玩家名单
    REQ_VERIFICATION_CODE_1 = "spark/phone/login/send/code/msg", -- 手机号绑定
    REQ_VERIFICATION_CODE_2 = "spark/phone/login/send-code",
    REQ_VERIFICATION_PHONE_PWD_1 = "spark/phone/login/pwd/get",
    REQ_VERIFICATION_PHONE_PWD_2 = "spark/phone/login/check-code",
    REQ_SHOP_PRODUCTS_CONFIG = "exchange/combine/page_mark/config", -- 商城相关
    REQ_STORE_EXCHANGE_PRODUCT = "exchange/combine/exchange",
    REQ_STORE_EXCHANGE_PRODUCT_LIST = "exchange/combine/goods/list",
    REQ_STORE_PRODUCT_TYPE_LIST = "exchange/combine/tags/list",
    REQ_STORE_MODIFY_USER_INFO = "exchange/combine/edit/user/info",
    REQ_SHOP_PAGE_MARK = "exchange/combine/page_mark/top/list", -- 商城标签列表
    REQ_COUPON_LIST = "coupon_sys/app_send_list",
    REQ_WRITE_OFF_STATUS = "api/user/logoff/check",
    REQ_WRITE_OFF_CANCEL = "api/user/logoff/cancel",
    REQ_LEGITIMACY = "no_version/config", -- 合规白名单
    REQ_TEA_RECHARGE_AUTO_AWARD_INFO = "tea_house_recall_card/info", -- B端充值奖励发放工具
    REQ_TEA_NEW_PLAY_TYPE_ACT_INFO = "gs64reward/info",
    REQ_TEA_NEW_PLAY_TYPE_ACT_AWARD = "gs64reward/getPrize",
    REQ_TEA_NEW_PLAY_TYPE_ACT_ORDER = "gs64reward/new_book",
    REQ_TEA_NEW_PLAY_TYPE_ACT_APPLY = "gs64reward/apply",
    REQ_TEA_NEW_PLAY_TYPE_STAGE_AWARD = "gs64reward/getPrizeNew",
    REQ_TEA_NEW_PLAY_TYPE_ACT_INFO_NEW = "new_gs64reward/info",
    REQ_TEA_NEW_PLAY_TYPE_CHECK_ACT = "new_gs64reward/choose_act",
    REQ_TEA_NEW_PLAY_TYPE_ACT_GET_FIRST_PRIZE = "new_gs64reward/get_first_prize",
    REQ_TEA_NEW_PLAY_TYPE_ACT_GET_PRIZE = "new_gs64reward/get_prize",
    REQ_TEA_NEW_PLAY_TYPE_GREET_INFO = "new_gs64reward/greet_info",
    REQ_TEA_NEW_PLAY_TYPE_GET_GREET_PRIZE = "new_gs64reward/get_greet_prize",
    REQ_TEA_NEW_PLAY_TYPE_AWARD_HISTORY = "new_gs64reward/get_all_prize_log",
    -- 推广活动
    REQ_TEAHOUSE_SPREAD_INFO = "teahouse_spread_new/info",
    REQ_TEAHOUSE_SPREAD_TASK = "teahouse_spread_new/task",
    REQ_TEAHOUSE_SPREAD_AWARD = "teahouse_spread_new/award",
    REQ_TEAHOUSE_SPREAD_DRAW = "teahouse_spread_new/draw",
    REQ_TEAHOUSE_SPREAD_AWARD_LIST = "teahouse_spread_new/award/list",
    REQ_TEAHOUSE_SPREAD_INVITE_LIST = "teahouse_spread_new/invite/list",
    REQ_TEAHOUSE_SPREAD_INVITE_CLICK = "teahouse_spread_new/invite/click"
}

return UrlConf
(9  