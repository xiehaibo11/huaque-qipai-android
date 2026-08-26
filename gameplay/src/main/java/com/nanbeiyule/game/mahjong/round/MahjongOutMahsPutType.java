package com.nanbeiyule.game.mahjong.round;

/**
 * Original {@code GameDefine.OUTMAHS_PUT_TYPE} discard-area layout codes,
 * recovered from
 * {@code src/game/Mahjong/BasicMahjong/Define/GameDefine.luac:190-193}.
 *
 * <p>The mode comes from the client setting
 * {@code settingData:getOutTableCardStyle()}
 * ({@code BasicMahjong/Modules/GameLayer/GameData.luac:1004-1006,:1137}) and
 * decides which discard list {@code getSurplusMahs} counts.
 */
public enum MahjongOutMahsPutType {
    /** {@code FOUR_DIRECTION = 1} — 四方出牌显示. */
    FOUR_DIRECTION(1),
    /** {@code TOGETHER = 2} — 所有牌一起摆放. */
    TOGETHER(2);

    private final int value;

    MahjongOutMahsPutType(int value) {
        this.value = value;
    }

    /** Returns the original protocol value. */
    public int value() {
        return value;
    }

    /** Resolves an original protocol value, rejecting anything outside :190-193. */
    public static MahjongOutMahsPutType fromValue(int value) {
        for (MahjongOutMahsPutType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown mahjong out put type " + value);
    }
}
