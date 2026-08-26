package com.nanbeiyule.game.wechat;

public record WechatSubscriptionCallback(
        int errCode,
        String action,
        String templateId,
        int scene,
        String reserved,
        String openId,
        String transaction) {
    public WechatSubscriptionCallback {
        action = text(action);
        templateId = text(templateId);
        reserved = text(reserved);
        openId = text(openId);
        transaction = text(transaction);
    }

    public boolean isConfirmed() {
        return errCode == 0 && "confirm".equals(action) && !openId.isBlank();
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
