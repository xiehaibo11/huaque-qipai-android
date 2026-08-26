package com.nanbeiyule.game;

import java.util.List;

/** Selection and real-document cache for the 18-game rule browser. */
final class GameRuleState {
    enum Content { LOADING, DOCUMENT, MISSING, ERROR }

    private final List<GameRuleCatalog.Entry> entries;
    private int selectedIndex;
    private Content content = Content.LOADING;
    private String error;

    GameRuleState(List<GameRuleCatalog.Entry> entries) {
        if (entries == null || entries.isEmpty()) throw new IllegalArgumentException("entries");
        this.entries = List.copyOf(entries);
    }

    List<GameRuleCatalog.Entry> entries() { return entries; }
    int selectedIndex() { return selectedIndex; }
    GameRuleCatalog.Entry selected() { return entries.get(selectedIndex); }
    Content content() { return content; }
    String error() { return error; }

    void select(int index) {
        if (index < 0 || index >= entries.size() || index == selectedIndex) return;
        selectedIndex = index;
        content = Content.LOADING;
        error = null;
    }

    void beginLoad() {
        content = Content.LOADING;
        error = null;
    }

    void show() {
        content = Content.DOCUMENT;
        error = null;
    }

    void missing() {
        content = Content.MISSING;
        error = null;
    }

    void error(String message) {
        content = Content.ERROR;
        error = message == null ? "获取规则失败，请稍后重试" : message;
    }
}
