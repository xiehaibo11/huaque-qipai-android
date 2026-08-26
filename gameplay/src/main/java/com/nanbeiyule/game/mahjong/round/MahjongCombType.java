package com.nanbeiyule.game.mahjong.round;

/**
 * Original {@code GameDefine.COMB_TYPE} meld kind codes, recovered from
 * {@code src/game/Mahjong/BasicMahjong/Define/GameDefine.luac:149-157}.
 */
public enum MahjongCombType {
    /** {@code COMB_TYPE.NONE = 0}. */
    NONE(0),
    /** {@code COMB_TYPE.CHOW = 1} — 吃（顺子）. */
    CHOW(1),
    /** {@code COMB_TYPE.PONG = 2} — 碰（刻子）. */
    PONG(2),
    /** {@code COMB_TYPE.EXPOSED_KONG = 3} — 明杠. */
    EXPOSED_KONG(3),
    /** {@code COMB_TYPE.CONCEALED_KONG = 4} — 暗杠. */
    CONCEALED_KONG(4),
    /** {@code COMB_TYPE.FILL_KONG = 5} — 补杠. */
    FILL_KONG(5),
    /** {@code COMB_TYPE.DOUBLE = 6} — 对子. */
    DOUBLE(6);

    private final int value;

    MahjongCombType(int value) {
        this.value = value;
    }

    /** Returns the original protocol value. */
    public int value() {
        return value;
    }

    /** Resolves an original protocol value, rejecting anything outside :149-157. */
    public static MahjongCombType fromValue(int value) {
        for (MahjongCombType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown mahjong comb type " + value);
    }
}
