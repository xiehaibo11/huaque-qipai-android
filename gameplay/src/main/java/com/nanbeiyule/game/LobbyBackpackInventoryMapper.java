package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class LobbyBackpackInventoryMapper {
    private LobbyBackpackInventoryMapper() {}

    static List<LobbyBackpackEntry> map(
            List<ShopInventoryItem> inventory, ShopCatalogState catalog) {
        if (inventory == null || inventory.isEmpty()) {
            return Collections.emptyList();
        }
        ShopCatalogState safeCatalog = catalog == null ? ShopCatalogState.empty() : catalog;
        ArrayList<LobbyBackpackEntry> result = new ArrayList<>();
        for (ShopInventoryItem item : inventory) {
            if (isPeakRace(item.itemCode())) {
                continue;
            }
            result.add(map(item, safeCatalog));
        }
        return Collections.unmodifiableList(result);
    }

    private static LobbyBackpackEntry map(
            ShopInventoryItem inventory, ShopCatalogState catalog) {
        String code = inventory.itemCode();
        ShopProduct product = catalog.findProduct(catalogCode(code));
        LobbyBackpackCategory category = category(code, product);
        String name = product == null ? knownName(code) : product.displayName();
        String icon = product == null ? knownIcon(code) : product.iconKey();
        if (isTimedDays(code)) {
            return LobbyBackpackEntry.timedDays(
                    code, category, name, icon, inventory.quantity());
        }
        return LobbyBackpackEntry.counted(
                code, category, name, icon, inventory.quantity());
    }

    private static LobbyBackpackCategory category(String code, ShopProduct product) {
        if (code.startsWith("INTERACTION_") || code.startsWith("PROP_CHAT_VOICE_")) {
            return LobbyBackpackCategory.INTERACTION;
        }
        if (code.startsWith("DECORATION_") || code.startsWith("PROP_RQDH_")) {
            return LobbyBackpackCategory.DECORATION;
        }
        if (product != null) {
            return switch (product.category()) {
                case INTERACTION -> LobbyBackpackCategory.INTERACTION;
                case DECORATION -> LobbyBackpackCategory.DECORATION;
                default -> LobbyBackpackCategory.PROP;
            };
        }
        return LobbyBackpackCategory.PROP;
    }

    private static String catalogCode(String code) {
        return switch (code) {
            case "SHOP_RECORDER_DAY" -> "PROP_RECORDER_1_DAY";
            case "PROP_WASH_CARD" -> "PROP_WASH_CARD_1";
            case "PROP_LUCK_BEAD" -> "PROP_LUCK_BEAD_1";
            case "PROP_CHAT_VOICE_120404" -> "CHAT_VOICE_XIAOGU_1_DAY";
            default -> code.startsWith("PROP_RQDH_")
                    ? "DECORATION_VEHICLE_" + code.substring("PROP_RQDH_".length())
                    : code;
        };
    }

    private static String knownName(String code) {
        return switch (code) {
            case "SHOP_RECORDER_DAY", "SHOP_RECORDER_MINUTE" -> "记牌器";
            case "PROP_WASH_CARD" -> "洗牌券";
            case "PROP_LUCK_BEAD" -> "转运珠";
            case "PROP_CHAT_VOICE_120404" -> "小谷专属语音包";
            default -> code;
        };
    }

    private static String knownIcon(String code) {
        return switch (code) {
            case "SHOP_RECORDER_DAY", "SHOP_RECORDER_MINUTE" -> "recorder";
            case "PROP_WASH_CARD" -> "wash_card";
            case "PROP_LUCK_BEAD" -> "luck_bead";
            case "PROP_CHAT_VOICE_120404" -> "voice";
            default -> "";
        };
    }

    private static boolean isTimedDays(String code) {
        return code.equals("SHOP_RECORDER_DAY")
                || code.startsWith("DECORATION_")
                || code.startsWith("PROP_RQDH_")
                || code.startsWith("PROP_CHAT_VOICE_");
    }

    private static boolean isPeakRace(String code) {
        String normalized = code.toUpperCase();
        return normalized.contains("PEAK_RACE") || normalized.equals("101859");
    }
}
