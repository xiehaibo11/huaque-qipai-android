package com.nanbeiyule.game.lobbyshare;

import java.util.Objects;

/** Starts sharing without inferring a completed share or granting client-side rewards. */
public final class LobbyShareController {
    public enum Outcome {
        WECHAT_OPENED,
        WECHAT_UNAVAILABLE,
        WECHAT_REJECTED
    }

    private final LobbyShareContent content;
    private final LobbyShareGateway gateway;

    public LobbyShareController(LobbyShareContent content, LobbyShareGateway gateway) {
        this.content = Objects.requireNonNull(content, "content");
        this.gateway = Objects.requireNonNull(gateway, "gateway");
    }

    public Outcome shareToWechatFriend() {
        LobbyShareGateway.Result result =
                gateway.shareToFriend(
                        content.shareTitle(),
                        content.shareDescription(),
                        content.webpageUrl());
        return switch (result) {
            case STARTED -> Outcome.WECHAT_OPENED;
            case NOT_CONFIGURED, NOT_INSTALLED -> Outcome.WECHAT_UNAVAILABLE;
            case REJECTED -> Outcome.WECHAT_REJECTED;
        };
    }

    public String copyableLink() {
        return content.webpageUrl();
    }
}
