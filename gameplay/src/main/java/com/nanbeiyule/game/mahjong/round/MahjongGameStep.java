package com.nanbeiyule.game.mahjong.round;

/**
 * Original {@code GameDefine.ENUM_GAME_STEP} round phase codes, recovered from
 * {@code src/game/Mahjong/BasicMahjong/Define/GameDefine.luac:196-216}
 * ({@code GAME_STEP} aliases the same table at :218).
 */
public enum MahjongGameStep {
    /** {@code GAME_STEP_NONE = 0}. */
    GAME_STEP_NONE(0),
    /** {@code GAME_STEP_START_GAME = 1} — 开始游戏. */
    GAME_STEP_START_GAME(1),
    /** {@code GAME_STEP_ANTE = 2}. */
    GAME_STEP_ANTE(2),
    /** {@code GAME_STEP_SPECF_MAH = 3} — 做牌. */
    GAME_STEP_SPECF_MAH(3),
    /** {@code GAME_STEP_THROW_CHIP_1 = 4} — 掷骰子(定位骰子). */
    GAME_STEP_THROW_CHIP_1(4),
    /** {@code GAME_STEP_THROW_CHIP_2 = 5} — 掷骰子(开牌骰子). */
    GAME_STEP_THROW_CHIP_2(5),
    /** {@code GAME_STEP_TAKE_FIRST = 6} — 抓牌. */
    GAME_STEP_TAKE_FIRST(6),
    /** {@code GAME_STEP_THROW_CHIP_3 = 7} — 掷骰子(财神骰子). */
    GAME_STEP_THROW_CHIP_3(7),
    /** {@code GAME_STEP_OPEN_MAH = 8} — 翻开. */
    GAME_STEP_OPEN_MAH(8),
    /** {@code GAME_STEP_FIRST_REPLACE = 9} — 刚开始的补花. */
    GAME_STEP_FIRST_REPLACE(9),
    /** {@code GAME_STEP_PLAY_MAH = 10} — 开始打麻将. */
    GAME_STEP_PLAY_MAH(10),
    /** {@code GAME_STEP_WIN_LOST = 11} — 计算. */
    GAME_STEP_WIN_LOST(11),
    /** {@code GAME_STEP_END_GAME = 12} — 结束游戏. */
    GAME_STEP_END_GAME(12),
    /** {@code GAME_STEP_SHUFFLE = 13}. */
    GAME_STEP_SHUFFLE(13),
    /** {@code GAME_STEP_ADD_MULTIPLE = 14} — 加倍. */
    GAME_STEP_ADD_MULTIPLE(14),
    /** {@code GAME_STEP_ADD_MULTIPLE_END = 15} — 加倍结束. */
    GAME_STEP_ADD_MULTIPLE_END(15),
    /** {@code GAME_STEP_COUNT = 16}. */
    GAME_STEP_COUNT(16),
    /** {@code GAME_STEP_USER = 100}. */
    GAME_STEP_USER(100);

    private final int value;

    MahjongGameStep(int value) {
        this.value = value;
    }

    /** Returns the original protocol value. */
    public int value() {
        return value;
    }

    /** Resolves an original protocol value, rejecting anything outside :196-216. */
    public static MahjongGameStep fromValue(int value) {
        for (MahjongGameStep step : values()) {
            if (step.value == value) {
                return step;
            }
        }
        throw new IllegalArgumentException("unknown mahjong game step " + value);
    }
}
