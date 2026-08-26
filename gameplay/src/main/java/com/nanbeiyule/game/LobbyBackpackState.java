package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class LobbyBackpackState {
    enum Phase {
        LOADING,
        READY,
        ERROR
    }

    private final Phase phase;
    private final List<LobbyBackpackEntry> entries;
    private final LobbyBackpackCategory category;
    private final int selectedIndex;
    private final String error;

    private LobbyBackpackState(
            Phase phase,
            List<LobbyBackpackEntry> entries,
            LobbyBackpackCategory category,
            int selectedIndex,
            String error) {
        this.phase = phase;
        this.entries = entries;
        this.category = category;
        this.selectedIndex = selectedIndex;
        this.error = error;
    }

    static LobbyBackpackState loading() {
        return new LobbyBackpackState(
                Phase.LOADING, Collections.emptyList(), LobbyBackpackCategory.ALL, 0, "");
    }

    static LobbyBackpackState ready(List<LobbyBackpackEntry> source) {
        List<LobbyBackpackEntry> entries = source == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(source));
        return new LobbyBackpackState(
                Phase.READY, entries, LobbyBackpackCategory.ALL, 0, "");
    }

    static LobbyBackpackState error(String message) {
        String safe = message == null || message.isBlank() ? "背包加载失败" : message.trim();
        return new LobbyBackpackState(
                Phase.ERROR, Collections.emptyList(), LobbyBackpackCategory.ALL, 0, safe);
    }

    LobbyBackpackState selectCategory(LobbyBackpackCategory category) {
        return new LobbyBackpackState(
                phase, entries, Objects.requireNonNull(category), 0, error);
    }

    LobbyBackpackState selectEntry(int index) {
        int max = Math.max(0, visibleEntries().size() - 1);
        return new LobbyBackpackState(
                phase, entries, category, Math.max(0, Math.min(index, max)), error);
    }

    List<LobbyBackpackEntry> visibleEntries() {
        if (category == LobbyBackpackCategory.ALL) {
            return entries;
        }
        return entries.stream().filter(entry -> entry.category() == category).toList();
    }

    LobbyBackpackEntry selectedEntry() {
        List<LobbyBackpackEntry> visible = visibleEntries();
        return visible.isEmpty() ? null : visible.get(Math.min(selectedIndex, visible.size() - 1));
    }

    Phase phase() {
        return phase;
    }

    LobbyBackpackCategory category() {
        return category;
    }

    int selectedIndex() {
        return selectedIndex;
    }

    String error() {
        return error;
    }
}
