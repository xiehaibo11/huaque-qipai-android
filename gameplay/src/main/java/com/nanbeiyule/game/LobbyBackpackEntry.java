package com.nanbeiyule.game;

import java.util.Objects;

public record LobbyBackpackEntry(
        String itemCode,
        LobbyBackpackCategory category,
        String displayName,
        String description,
        String iconKey,
        long quantity,
        String remainingText) {
    public LobbyBackpackEntry {
        itemCode = required(itemCode, "itemCode");
        category = Objects.requireNonNull(category, "category");
        if (category == LobbyBackpackCategory.ALL) {
            throw new IllegalArgumentException("inventory entry needs a concrete category");
        }
        displayName = required(displayName, "displayName");
        description = description == null ? "" : description.trim();
        iconKey = iconKey == null ? "" : iconKey.trim();
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        remainingText = required(remainingText, "remainingText");
    }

    public static LobbyBackpackEntry counted(
            String itemCode,
            LobbyBackpackCategory category,
            String displayName,
            String iconKey,
            long quantity) {
        return new LobbyBackpackEntry(
                itemCode,
                category,
                displayName,
                "",
                iconKey,
                quantity,
                "剩余数量：" + quantity);
    }

    static LobbyBackpackEntry timedDays(
            String itemCode,
            LobbyBackpackCategory category,
            String displayName,
            String iconKey,
            long days) {
        return new LobbyBackpackEntry(
                itemCode,
                category,
                displayName,
                "",
                iconKey,
                days,
                "剩余时间：" + days + "天");
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
