package com.nanbeiyule.game.goldroom;

/**
 * One selectable gold-room level, mirroring an entry of the original {@code roomLevelInfos}.
 *
 * <p>Only raw values live here; the 底分 and 准入 strings come from {@link GoldRoomText} so the
 * original ChooseRoom.lua formatting stays in one place.
 */
public record GoldRoomLevel(
        int roomNameFlag,
        int uiType,
        int chairCount,
        long baseScore,
        boolean dynamicCost,
        long minRich,
        long maxRich,
        long onlineCount,
        String tagLeftTop,
        String tagRightTop,
        String tagRibbon1,
        String tagRibbon2) {

    /** {@code _fontBaseScore} content. */
    public String baseScoreText() {
        return GoldRoomText.baseScoreText(baseScore, dynamicCost);
    }

    /** {@code _txtGoldLimit} content. */
    public String goldLimitText() {
        return GoldRoomText.goldLimitText(minRich, maxRich);
    }

    /** Parsed first ribbon, or null when the slot is unconfigured. */
    public GoldRoomRibbon ribbon1() {
        return GoldRoomRibbon.parse(tagRibbon1);
    }

    /** Parsed second ribbon, or null when the slot is unconfigured. */
    public GoldRoomRibbon ribbon2() {
        return GoldRoomRibbon.parse(tagRibbon2);
    }
}
