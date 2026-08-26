package com.nanbeiyule.game;

/** Typed personal-center commands handled by the Activity owner. */
enum PersonalCenterAction {
    MY_RECORDS("我的战绩"),
    FAVORITES("我的收藏"),
    GIFTS("我的礼包"),
    MESSAGES("我的消息"),
    SHOP_ROOM_CARDS("购买房卡"),
    BOUND_ROOM_CARD_HELP("绑定房卡说明"),
    SHOP_DIAMONDS("购买钻石"),
    ACCOUNT_DELETION("账号注销"),
    OPEN_APP_PERMISSION_SETTINGS("权限设置"),
    TOGGLE_CLIPBOARD_PERMISSION("剪贴板权限"),
    PHONE_BINDING("绑定手机"),
    PHONE_SEND_CODE("发送验证码"),
    PHONE_SUBMIT("完成手机换绑"),
    MEMBERSHIP_PREVIOUS("上一个会员等级"),
    MEMBERSHIP_NEXT("下一个会员等级"),
    MEMBERSHIP_CENTER("会员中心"),
    MEMBERSHIP_GIFT("会员礼包"),
    LOGIN_PASSWORD("登录密码"),
    PAYMENT_PASSWORD("支付密码"),
    REAL_NAME("实名认证"),
    DEVICE_MANAGEMENT("设备管理"),
    PRIVACY_POLICY("隐私政策"),
    CLEAR_CACHE("清理缓存"),
    NETWORK_CHECK("网络检测"),
    RESOURCE_REPAIR("问题修复"),
    FAQ("常见问题"),
    CUSTOMER_SERVICE("联系客服"),
    FEEDBACK("意见反馈"),
    REPORT("举报反馈"),
    FEEDBACK_HISTORY("反馈记录");

    private final String displayName;

    PersonalCenterAction(String displayName) {
        this.displayName = displayName;
    }

    String displayName() {
        return displayName;
    }
}
