package com.huaque.ui.wechat;

public final class WechatSubscriptionOfferPolicy {
    private boolean explicitWechatLogin;

    public void onWechatAuthenticated() {
        explicitWechatLogin = true;
    }

    public void onWechatAuthenticationFailed() {
        explicitWechatLogin = false;
    }

    public boolean onHomeLoaded() {
        boolean offer = explicitWechatLogin;
        explicitWechatLogin = false;
        return offer;
    }
}
