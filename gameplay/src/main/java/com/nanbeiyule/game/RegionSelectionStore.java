package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;

final class RegionSelectionStore {
    static final long NO_SELECTION = -1L;

    private static final String PREFERENCES = "nanbei_region_selection";
    private static final String SELECTED_LOBBY_ID = "selected_lobby_id";

    private final SharedPreferences preferences;

    RegionSelectionStore(Context context) {
        preferences =
                context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    long getSelectedLobbyId() {
        return preferences.getLong(SELECTED_LOBBY_ID, NO_SELECTION);
    }

    void setSelectedLobbyId(long lobbyId) {
        if (lobbyId <= 0L) {
            throw new IllegalArgumentException("lobbyId must be positive");
        }
        preferences.edit().putLong(SELECTED_LOBBY_ID, lobbyId).apply();
    }
}
