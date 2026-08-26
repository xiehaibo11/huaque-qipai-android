package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Original JuBaoPenLotteryView row tags and fortune gain derived from authoritative data. */
record TaizhouTreasureResultPresentation(
        List<Item> items,
        int totalFortuneDelta,
        int drawCount,
        FortuneState.Wallet wallet) {
    enum Tag { NEW, UPGRADE, MAX }

    record Item(
            FortuneTreasureDrawResult.Draw draw,
            TaizhouTreasureCatalog.Item catalog,
            Tag tag,
            int fortuneDelta) {
        String code() {
            return draw.treasureCode();
        }
    }

    TaizhouTreasureResultPresentation {
        items = List.copyOf(items == null ? List.of() : items);
    }

    static TaizhouTreasureResultPresentation from(
            FortuneState before,
            FortuneTreasureDrawResult result) {
        Map<String, Integer> beforeLevels = new LinkedHashMap<>();
        for (FortuneState.Treasure treasure : before.treasures()) {
            if (treasure.remainingSeconds() > 0) {
                beforeLevels.put(treasure.treasureCode(), treasure.level());
            }
        }
        Map<String, Integer> finalLevels = new LinkedHashMap<>();
        for (FortuneTreasureDrawResult.Draw draw : result.draws()) {
            finalLevels.merge(draw.treasureCode(), draw.level(), Math::max);
        }
        List<Item> items = new ArrayList<>();
        int total = 0;
        for (FortuneTreasureDrawResult.Draw draw : result.draws()) {
            TaizhouTreasureCatalog.Item catalog =
                    TaizhouTreasureCatalog.itemForCode(draw.treasureCode());
            if (catalog == null) continue;
            int finalLevel = finalLevels.getOrDefault(draw.treasureCode(), draw.level());
            Tag tag = finalLevel == 1 ? Tag.NEW : finalLevel == 10 ? Tag.MAX : Tag.UPGRADE;
            int delta = beforeLevels.getOrDefault(draw.treasureCode(), 0) >= 10
                    ? 0 : catalog.fortunePerLevel();
            items.add(new Item(draw, catalog, tag, delta));
            total += delta;
        }
        return new TaizhouTreasureResultPresentation(
                items, total, result.count(), result.wallet());
    }

    Item itemFor(String treasureCode) {
        for (Item item : items) {
            if (item.code().equals(treasureCode)) return item;
        }
        return null;
    }
}
