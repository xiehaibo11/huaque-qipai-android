package com.nanbeiyule.game.mahjong;

/**
 * Original Zhejiang lobby mahjong tile value encoding.
 *
 * <p>Authoritative source is the recovered original
 * {@code src/game/Mahjong/BasicMahjong/Define/GameDefine.lua}:
 *
 * <pre>
 * MAH_DIVIDED = 16
 * MAH_FLOWER  = { NONE=0, WAN=1, TIAO=2, TONG=3, FENG=4, JIAN=5, HUA=6, BACK=7, COUNT=8 }
 * MAH_VALUE.&lt;tile&gt; = MAH_FLOWER.&lt;suit&gt; * MAH_DIVIDED + rank
 * </pre>
 *
 * <p>So a tile value is {@code (suit << 4) | rank}. The 42 faces shipped in the
 * Taizhou atlas {@code MahFace_7109} independently confirm the suited, wind,
 * dragon and first eight flower values; the remaining flower values and the
 * {@code BACK} pseudo-suit come from the table above.
 *
 * <p>Note that the value space is wider than the Taizhou artwork: ranks
 * {@code HUA_DA_BAI_BAN}(9) through {@code HUA_JIN_YUAN_BAO}(13) are defined by
 * the engine but have no face in {@code MahFace_7109}, because that atlas is
 * shared by every areaId 7109 variant and those tiles belong to other games.
 *
 * <p>This type covers the encoding only. Wall composition, dealing order,
 * melds and scoring are decided by the server and by the per-variant Lua rules;
 * nothing about them may be inferred from this class.
 */
public final class MahjongTile {
    /** {@code MAH_DIVIDED}: the suit multiplier, i.e. a four-bit rank field. */
    public static final int SUIT_SHIFT = 4;

    /** {@code MAH_FLOWER.WAN}, 万. Ranks 1-9 encode as 0x11-0x19. */
    public static final int SUIT_WAN = 0x1;

    /** {@code MAH_FLOWER.TIAO}, 条. Ranks 1-9 encode as 0x21-0x29. */
    public static final int SUIT_TIAO = 0x2;

    /** {@code MAH_FLOWER.TONG}, 筒. Ranks 1-9 encode as 0x31-0x39. */
    public static final int SUIT_TONG = 0x3;

    /** {@code MAH_FLOWER.FENG}, 风: 东南西北 as 0x41-0x44. */
    public static final int SUIT_FENG = 0x4;

    /** {@code MAH_FLOWER.JIAN}, 箭: 中發白 as 0x51-0x53. */
    public static final int SUIT_JIAN = 0x5;

    /**
     * {@code MAH_FLOWER.HUA}, 花: 梅兰竹菊 (1-4), 春夏秋冬 (5-8), then 大白板,
     * 老鼠, 猫, 财神, 金元宝 (9-13).
     */
    public static final int SUIT_HUA = 0x6;

    /** {@code MAH_FLOWER.BACK}, 背: the blank face, the tile back and the joker. */
    public static final int SUIT_BACK = 0x7;

    /** {@code MAH_VALUE.FACE}: blank tile face. */
    public static final int FACE = 0x71;

    /** {@code MAH_VALUE.BACK}: tile back. */
    public static final int BACK = 0x72;

    /** {@code MAH_VALUE.JOKER}: 财神 joker. */
    public static final int JOKER = 0x76;

    /** Highest flower rank that the Taizhou {@code MahFace_7109} atlas draws. */
    public static final int MAX_DRAWN_FLOWER_RANK = 8;

    private static final int[] MAX_RANK = {
        0, // NONE
        9, // WAN
        9, // TIAO
        9, // TONG
        4, // FENG
        3, // JIAN
        13, // HUA
    };

    private MahjongTile() {}

    /** Returns the encoded value for {@code suit} and {@code rank}. */
    public static int encode(int suit, int rank) {
        int value = (suit << SUIT_SHIFT) | (rank & 0xF);
        if (rank < 0 || rank > 0xF || !isValid(value)) {
            throw new IllegalArgumentException(
                    "invalid tile suit=" + suit + " rank=" + rank);
        }
        return value;
    }

    /** Returns the suit nibble of {@code value}. */
    public static int suitOf(int value) {
        return (value >> SUIT_SHIFT) & 0xF;
    }

    /** Returns the rank nibble of {@code value}. */
    public static int rankOf(int value) {
        return value & 0xF;
    }

    /** Returns whether {@code value} is a value the original engine defines. */
    public static boolean isValid(int value) {
        int suit = suitOf(value);
        int rank = rankOf(value);
        if (suit == SUIT_BACK) {
            return value == FACE || value == BACK || value == JOKER;
        }
        return suit >= SUIT_WAN
                && suit < MAX_RANK.length
                && rank >= 1
                && rank <= MAX_RANK[suit];
    }

    /** Returns whether {@code value} is a numbered suit tile (万/条/筒). */
    public static boolean isSuited(int value) {
        int suit = suitOf(value);
        return isValid(value)
                && (suit == SUIT_WAN || suit == SUIT_TIAO || suit == SUIT_TONG);
    }

    /** Returns whether {@code value} is a wind or dragon honour. */
    public static boolean isHonour(int value) {
        int suit = suitOf(value);
        return isValid(value) && (suit == SUIT_FENG || suit == SUIT_JIAN);
    }

    /**
     * Returns whether {@code value} is a flower, matching the original
     * {@code MahLogic.checkMahValueIsFlower} range {@code HUA_MEI..HUA_JIN_YUAN_BAO}.
     */
    public static boolean isFlower(int value) {
        return isValid(value) && suitOf(value) == SUIT_HUA;
    }

    /**
     * Returns whether {@code value} has a face in the Taizhou atlas, matching the
     * narrower original {@code MahLogic.isFlowerMahValue} range {@code HUA_MEI..HUA_DONG}
     * for flowers.
     */
    public static boolean hasTaizhouFace(int value) {
        if (!isValid(value) || suitOf(value) == SUIT_BACK) {
            return false;
        }
        return !isFlower(value) || rankOf(value) <= MAX_DRAWN_FLOWER_RANK;
    }
}
