package com.nanbeiyule.game;

public enum LobbyBackpackCategory {
    ALL("全部"),
    PROP("道具"),
    INTERACTION("互动"),
    DECORATION("装扮");

    private final String title;

    LobbyBackpackCategory(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
