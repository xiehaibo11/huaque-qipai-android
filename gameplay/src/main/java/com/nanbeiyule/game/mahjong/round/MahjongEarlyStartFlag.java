package com.nanbeiyule.game.mahjong.round;

/**
 * Taizhou {@code GameDefine.EarlyStartFlag} early-start request state codes,
 * recovered from
 * {@code src/game/Mahjong/TaiZhou/BasicTaiZhouMahjong/Define/GameDefine.luac:70-78}.
 *
 * <p>The original table deliberately assigns {@code 0} to both
 * {@code EARLY_START_NONE} and {@code EARLY_START_CANCEL} (:71-72), so
 * {@link #fromValue(int)} maps {@code 0} back to {@link #EARLY_START_NONE}.
 */
public enum MahjongEarlyStartFlag {
    /** {@code EARLY_START_NONE = 0} — 没有玩家请求提前开局. */
    EARLY_START_NONE(0),
    /** {@code EARLY_START_CANCEL = 0} — 取消提前开局请求. */
    EARLY_START_CANCEL(0),
    /** {@code EARLY_START_WAIT = 1} — 有玩家请求，正在等待其他玩家选择. */
    EARLY_START_WAIT(1),
    /** {@code EARLY_START_DISAGREE = 2} — 有玩家不同意. */
    EARLY_START_DISAGREE(2),
    /** {@code EARLY_START_AGREE = 3} — 所有玩家同意. */
    EARLY_START_AGREE(3),
    /** {@code EARLY_START_SUCCESS = 4} — 提前开局成功. */
    EARLY_START_SUCCESS(4),
    /** {@code EARLY_START_PLAYERCNT = 5} — 满足椅子数，取消提前开局. */
    EARLY_START_PLAYERCNT(5);

    private final int value;

    MahjongEarlyStartFlag(int value) {
        this.value = value;
    }

    /** Returns the original protocol value. */
    public int value() {
        return value;
    }

    /**
     * Resolves an original protocol value. {@code 0} is shared by
     * {@link #EARLY_START_NONE} and {@link #EARLY_START_CANCEL} (:71-72) and
     * resolves to {@link #EARLY_START_NONE}.
     */
    public static MahjongEarlyStartFlag fromValue(int value) {
        for (MahjongEarlyStartFlag flag : values()) {
            if (flag.value == value) {
                return flag;
            }
        }
        throw new IllegalArgumentException("unknown taizhou early start flag " + value);
    }

    /**
     * Taizhou {@code GameDefine.EarlyStartStatus} per-player answer codes,
     * recovered from
     * {@code src/game/Mahjong/TaiZhou/BasicTaiZhouMahjong/Define/GameDefine.luac:80-85}.
     */
    enum Status {
        /** {@code DEFAULT = 1} — 待确认. */
        DEFAULT(1),
        /** {@code AGREE = 2} — 同意. */
        AGREE(2),
        /** {@code REFUSE = 3} — 拒绝. */
        REFUSE(3),
        /** {@code REQUEST = 4} — 请求开局. */
        REQUEST(4);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        /** Returns the original protocol value. */
        public int value() {
            return value;
        }

        /** Resolves an original protocol value, rejecting anything outside :80-85. */
        public static Status fromValue(int value) {
            for (Status status : values()) {
                if (status.value == value) {
                    return status;
                }
            }
            throw new IllegalArgumentException("unknown taizhou early start status " + value);
        }
    }
}
