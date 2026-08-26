package com.nanbeiyule.game.wechat;

public final class WechatCallbackContract {
    public static final String ACTION_AUTH_RESPONSE =
            "com.nanbeiyule.game.action.WECHAT_AUTH_RESPONSE";
    public static final String ACTION_SUBSCRIPTION_RESPONSE =
            "com.nanbeiyule.game.action.WECHAT_SUBSCRIPTION_RESPONSE";
    public static final String EXTRA_ERROR_CODE = "wechat_error_code";
    public static final String EXTRA_CODE = "wechat_code";
    public static final String EXTRA_STATE = "wechat_state";
    public static final String EXTRA_ACTION = "wechat_subscription_action";
    public static final String EXTRA_TEMPLATE_ID = "wechat_subscription_template_id";
    public static final String EXTRA_SCENE = "wechat_subscription_scene";
    public static final String EXTRA_RESERVED = "wechat_subscription_reserved";
    public static final String EXTRA_TRANSACTION = "wechat_subscription_transaction";
    public static final String EXTRA_OPEN_ID = "wechat_subscription_open_id";

    private WechatCallbackContract() {
    }
}
