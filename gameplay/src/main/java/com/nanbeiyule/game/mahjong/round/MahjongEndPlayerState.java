package com.nanbeiyule.game.mahjong.round;

/**
 * Taizhou {@code GameDefine.endPlayerState} round-end player state codes,
 * recovered from
 * {@code src/game/Mahjong/TaiZhou/BasicTaiZhouMahjong/Define/GameDefine.luac:5-14}.
 */
public enum MahjongEndPlayerState {
    /** {@code EPS_NULL = 0}. */
    EPS_NULL(0),
    /** {@code EPS_HU = 1} — 胡牌. */
    EPS_HU(1),
    /** {@code EPS_DISCARD = 2} — 点炮. */
    EPS_DISCARD(2),
    /** {@code EPS_ROBKONG = 3} — 抢杠. */
    EPS_ROBKONG(3),
    /** {@code EPS_GANGSHANGKAIHUA = 4} — 杠上开花. */
    EPS_GANGSHANGKAIHUA(4),
    /** {@code EPS_CHENGBAO = 5} — 承包. */
    EPS_CHENGBAO(5),
    /** {@code EPS_DRAWN = 9} — 流局. */
    EPS_DRAWN(9);

    private final int value;

    MahjongEndPlayerState(int value) {
        this.value = value;
    }

    /** Returns the original protocol value. */
    public int value() {
        return value;
    }

    /** Resolves an original protocol value, rejecting anything outside :5-14. */
    public static MahjongEndPlayerState fromValue(int value) {
        for (MahjongEndPlayerState state : values()) {
            if (state.value == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("unknown taizhou end player state " + value);
    }
}
