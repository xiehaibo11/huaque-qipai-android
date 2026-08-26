package com.nanbeiyule.game.mahjong.round;

/**
 * Original {@code GameDefine.COMB_FLAG} meld wire flags, recovered from
 * {@code src/game/Mahjong/BasicMahjong/Define/GameDefine.luac:130-147}.
 *
 * <p>These are the flag values that travel inside {@code MahjongComb.nFlag}
 * (msgPlayerMah/msgPlayerBack/msgAction). They are <em>not</em> the same table
 * as {@link MahjongCombType}: COMB_FLAG carries the wire flavour (including
 * concealed-only and joker-only kinds), while COMB_TYPE is the reduced render
 * kind resolved through {@code GameDefine.COMB_FLAG_TO_TYPE} (:159-169).
 *
 * <p>Original call sites kept verbatim:
 * <ul>
 *   <li>{@code GameLayer/Module.luac:796} — msgAction converts its incoming
 *       {@code tComb.nFlag} with {@code COMB_FLAG_TO_TYPE} before storing.</li>
 *   <li>{@code GameLayer/Module.luac:441} — a comb renders as a meld only when
 *       {@code flag > NONE and flag <= LKONG}.</li>
 *   <li>{@code GameLayer/Module.luac:461-462} — msgPlayerBack drops combs with
 *       {@code nFlag <= NONE or nFlag > TKONG}.</li>
 *   <li>{@code GameLayer/Module.luac:1370/1412/1443/1458/1472} — doActionChow/
 *       Pong/Kong stamp CHOW/PUNG/MKONG/CKONG/TKONG on the outgoing comb.</li>
 * </ul>
 */
public enum MahjongCombFlag {
    /** {@code COMB_FLAG.NONE = 0}. */
    NONE(0),
    /** {@code COMB_FLAG.CHOW = 1} — 吃顺子. */
    CHOW(1),
    /** {@code COMB_FLAG.PUNG = 2} — 碰刻子. */
    PUNG(2),
    /** {@code COMB_FLAG.MKONG = 3} — 明杠（直杠）. */
    MKONG(3),
    /** {@code COMB_FLAG.CKONG = 4} — 暗杠. */
    CKONG(4),
    /** {@code COMB_FLAG.TKONG = 5} — 补杠. */
    TKONG(5),
    /** {@code COMB_FLAG.CCHOW = 6} — 暗吃. */
    CCHOW(6),
    /** {@code COMB_FLAG.CPUNG = 7} — 暗碰. */
    CPUNG(7),
    /** {@code COMB_FLAG.LKONG = 8} — 乱杠. */
    LKONG(8),
    /** {@code COMB_FLAG.DOUBLE = 9} — 对（将）对. */
    DOUBLE(9),
    /** {@code COMB_FLAG.JOKCMB = 10} — 财神组合. */
    JOKCMB(10),
    /** {@code COMB_FLAG.JOKDBL = 11} — 财神对. */
    JOKDBL(11),
    /** {@code COMB_FLAG.HAND = 12} — 手牌（胡牌展示）. */
    HAND(12),
    /** {@code COMB_FLAG.SINGLE = 13} — 单张. */
    SINGLE(13);

    /** Wire value of the phantom {@code COMB_FLAG.COUNT = 14} sentinel (:146). */
    public static final int COUNT = 14;

    private final int value;

    MahjongCombFlag(int value) {
        this.value = value;
    }

    /** Returns the original protocol value. */
    public int value() {
        return value;
    }

    /** Resolves an original protocol value, rejecting anything outside :130-147. */
    public static MahjongCombFlag fromValue(int value) {
        for (MahjongCombFlag flag : values()) {
            if (flag.value == value) {
                return flag;
            }
        }
        throw new IllegalArgumentException("unknown mahjong comb flag " + value);
    }

    /**
     * Original {@code GameDefine.COMB_FLAG_TO_TYPE} lookup (:159-169).
     *
     * <p>The Lua table maps exactly eight flags; every other flag (NONE, LKONG,
     * JOKCMB, JOKDBL, HAND, SINGLE) has no entry and reads back {@code nil}.
     * This port keeps that contract and returns {@code null} instead of
     * throwing, so callers can branch exactly like
     * {@code Module.luac:796} does on a missing conversion.
     */
    public MahjongCombType combType() {
        return switch (this) {
            case CHOW, CCHOW -> MahjongCombType.CHOW;
            case PUNG, CPUNG -> MahjongCombType.PONG;
            case MKONG -> MahjongCombType.EXPOSED_KONG;
            case CKONG -> MahjongCombType.CONCEALED_KONG;
            case TKONG -> MahjongCombType.FILL_KONG;
            case DOUBLE -> MahjongCombType.DOUBLE;
            case NONE, LKONG, JOKCMB, JOKDBL, HAND, SINGLE -> null;
        };
    }

    /**
     * Original meld-visibility guard from {@code GameLayer/Module.luac:441}:
     * a comb is rendered as an exposed meld only while
     * {@code flag > NONE and flag <= LKONG}.
     */
    public boolean isMeldFlag() {
        return value > NONE.value && value <= LKONG.value;
    }

    /**
     * Original msgPlayerBack comb filter from {@code GameLayer/Module.luac:461-462}:
     * combs outside {@code NONE < flag <= TKONG} are dropped while rebuilding
     * the visible table after a relink.
     */
    public boolean isRelinkVisibleMeld() {
        return value > NONE.value && value <= TKONG.value;
    }
}
