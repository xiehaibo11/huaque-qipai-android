package com.nanbeiyule.game;

/** Recovered local configuration for the Taizhou (900023) official WeChat account. */
record WechatPublicModel(long lobbyId, String publicName) {
    enum OpenAction {
        OPEN_WECHAT,
        SHOW_NOT_INSTALLED
    }

    WechatPublicModel {
        if (lobbyId <= 0 || publicName == null || publicName.isBlank()) {
            throw new IllegalArgumentException("Wechat public configuration is incomplete");
        }
    }

    static WechatPublicModel taizhou() {
        return new WechatPublicModel(900023L, "台州休闲");
    }

    String clipboardText() {
        return publicName;
    }

    String displayName() {
        return "【" + publicName + "】";
    }

    String notice() {
        return "关注官方微信公众号"
                + displayName()
                + "，了解最新官方活动，游戏资讯，还有神秘礼品等您发现哦！";
    }

    static OpenAction openAction(boolean wechatInstalled) {
        return wechatInstalled ? OpenAction.OPEN_WECHAT : OpenAction.SHOW_NOT_INSTALLED;
    }
}
