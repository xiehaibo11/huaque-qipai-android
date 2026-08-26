package com.nanbeiyule.game.lobbyshare;

import com.nanbeiyule.game.wechat.WechatLoginManager;
import java.util.Objects;

/** Production adapter backed by the app's registered WeChat Open SDK manager. */
public final class WechatLobbyShareGateway implements LobbyShareGateway {
    private final WechatLoginManager manager;

    public WechatLobbyShareGateway(WechatLoginManager manager) {
        this.manager = Objects.requireNonNull(manager, "manager");
    }

    @Override
    public Result shareToFriend(String title, String description, String webpageUrl) {
        return switch (manager.shareWebpage(title, description, webpageUrl)) {
            case STARTED -> Result.STARTED;
            case NOT_CONFIGURED -> Result.NOT_CONFIGURED;
            case NOT_INSTALLED -> Result.NOT_INSTALLED;
            case REJECTED -> Result.REJECTED;
        };
    }
}
