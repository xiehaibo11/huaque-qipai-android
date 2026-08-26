package com.nanbeiyule.game.mahjong.round;

/**
 * Original {@code GameDefine.ACTION} player action codes, recovered from
 * {@code src/game/Mahjong/BasicMahjong/Define/GameDefine.luac:87-96}.
 *
 * <p>Constant names follow the Lua table verbatim: the original uses
 * {@code PONG} (not PUNG) for 碰 and {@code FLOWER} (not REPLACE_FLOWER) for 补花.
 */
public enum MahjongAction {
    /** {@code ACTION.NONE = 0}. */
    NONE(0),
    /** {@code ACTION.PASS = 1} — 过. */
    PASS(1),
    /** {@code ACTION.CHOW = 2} — 吃. */
    CHOW(2),
    /** {@code ACTION.PONG = 3} — 碰. */
    PONG(3),
    /** {@code ACTION.KONG = 4} — 杠. */
    KONG(4),
    /** {@code ACTION.HU = 5} — 胡. */
    HU(5),
    /** {@code ACTION.FLOWER = 6} — 补花. */
    FLOWER(6),
    /** {@code ACTION.TING = 7} — 听. */
    TING(7);

    private final int value;

    MahjongAction(int value) {
        this.value = value;
    }

    /** Returns the original protocol value. */
    public int value() {
        return value;
    }

    /** Resolves an original protocol value, rejecting anything outside :87-96. */
    public static MahjongAction fromValue(int value) {
        for (MahjongAction action : values()) {
            if (action.value == value) {
                return action;
            }
        }
        throw new IllegalArgumentException("unknown mahjong action " + value);
    }
}
