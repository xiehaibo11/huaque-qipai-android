package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.List;

/** Per-user private persistence for the original local-only table treasure placement. */
final class TaizhouTreasurePlacementStore {
    interface Storage {
        String get(String key);

        void put(String key, String value);
    }

    private static final String PREFERENCES = "taizhou_treasure_placement";
    private static final String KEY_PREFIX = "placed_";
    private final Storage storage;

    TaizhouTreasurePlacementStore(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        storage = new Storage() {
            @Override
            public String get(String key) {
                return preferences.getString(key, "");
            }

            @Override
            public void put(String key, String value) {
                preferences.edit().putString(key, value).apply();
            }
        };
    }

    TaizhouTreasurePlacementStore(Storage storage) {
        this.storage = storage;
    }

    String selectedFor(String userId, List<FortuneState.Treasure> treasures) {
        String key = key(userId);
        String selected = storage.get(key);
        if (isActive(selected, treasures)) return selected;
        if (!selected.isEmpty()) storage.put(key, "");
        return "";
    }

    boolean place(
            String userId,
            String treasureCode,
            List<FortuneState.Treasure> treasures) {
        if (!isActive(treasureCode, treasures)) return false;
        storage.put(key(userId), treasureCode);
        return true;
    }

    void cancel(String userId) {
        storage.put(key(userId), "");
    }

    private static boolean isActive(
            String treasureCode, List<FortuneState.Treasure> treasures) {
        if (treasureCode == null || treasureCode.isBlank() || treasures == null) return false;
        for (FortuneState.Treasure treasure : treasures) {
            if (treasureCode.equals(treasure.treasureCode())
                    && treasure.remainingSeconds() > 0) return true;
        }
        return false;
    }

    private static String key(String userId) {
        return KEY_PREFIX + (userId == null ? "" : userId);
    }
}
