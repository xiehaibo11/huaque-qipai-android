package com.nanbeiyule.game.wechat;

public interface WechatSubscriptionLauncher {
    WechatSubscriptionStartResult startSubscription(WechatSubscriptionIntent intent);
}
