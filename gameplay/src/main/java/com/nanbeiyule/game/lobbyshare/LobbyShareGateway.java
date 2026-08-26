package com.nanbeiyule.game.lobbyshare;

/** Boundary for opening a real WeChat friend webpage-share request. */
public interface LobbyShareGateway {
    enum Result {
        STARTED,
        NOT_CONFIGURED,
        NOT_INSTALLED,
        REJECTED
    }

    Result shareToFriend(String title, String description, String webpageUrl);
}
