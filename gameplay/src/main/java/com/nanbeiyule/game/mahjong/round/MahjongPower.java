package com.nanbeiyule.game.mahjong.round;

/**
 * Original {@code GameDefine.POWER} permission bitmask, recovered from
 * {@code src/game/Mahjong/BasicMahjong/Define/GameDefine.luac:98-113}.
 *
 * <p>The server sends one bitmask per turn; the Lua client only ever tests
 * bits with bitwise AND, so every predicate here is a pure mask check.
 */
public final class MahjongPower {
    /** {@code POWER.NONE = 0x000} — 无. */
    public static final int NONE = 0x000;
    /** {@code POWER.CANCEL = 0x001} — 过. */
    public static final int CANCEL = 0x001;
    /** {@code POWER.PLAY = 0x002} — 出. */
    public static final int PLAY = 0x002;
    /** {@code POWER.CHOW = 0x004} — 吃. */
    public static final int CHOW = 0x004;
    /** {@code POWER.PUNG = 0x008} — 碰. */
    public static final int PUNG = 0x008;
    /** {@code POWER.HU = 0x010} — 和. */
    public static final int HU = 0x010;
    /** {@code POWER.MKONG = 0x020} — 直杠. */
    public static final int MKONG = 0x020;
    /** {@code POWER.CKONG = 0x040} — 暗杠. */
    public static final int CKONG = 0x040;
    /** {@code POWER.TKONG = 0x080} — 补杠. */
    public static final int TKONG = 0x080;
    /** {@code POWER.TWAIT = 0x100} — 抓听. */
    public static final int TWAIT = 0x100;
    /** {@code POWER.CWAIT = 0x200} — 吃听. */
    public static final int CWAIT = 0x200;
    /** {@code POWER.PWAIT = 0x400} — 碰听. */
    public static final int PWAIT = 0x400;
    /** {@code POWER.REPLACE = 0x800} — 补. */
    public static final int REPLACE = 0x800;

    private MahjongPower() {}

    /** Returns whether the 过 bit ({@code CANCEL}) is set. */
    public static boolean hasCancel(int power) {
        return (power & CANCEL) != 0;
    }

    /** Returns whether the 出 bit ({@code PLAY}) is set. */
    public static boolean canPlay(int power) {
        return (power & PLAY) != 0;
    }

    /** Returns whether the 吃 bit ({@code CHOW}) is set. */
    public static boolean hasChow(int power) {
        return (power & CHOW) != 0;
    }

    /** Returns whether the 碰 bit ({@code PUNG}) is set. */
    public static boolean hasPung(int power) {
        return (power & PUNG) != 0;
    }

    /** Returns whether the 和 bit ({@code HU}) is set. */
    public static boolean hasHu(int power) {
        return (power & HU) != 0;
    }

    // 推断: Lua 只有三个独立杠位，没有组合判定函数；hasKong 是 MKONG|CKONG|TKONG 的并集判定。
    /** Returns whether any kong bit ({@code MKONG|CKONG|TKONG}) is set. */
    public static boolean hasKong(int power) {
        return (power & (MKONG | CKONG | TKONG)) != 0;
    }

    /** Returns whether the 直杠 bit ({@code MKONG}) is set. */
    public static boolean hasExposedKong(int power) {
        return (power & MKONG) != 0;
    }

    /** Returns whether the 暗杠 bit ({@code CKONG}) is set. */
    public static boolean hasConcealedKong(int power) {
        return (power & CKONG) != 0;
    }

    /** Returns whether the 补杠 bit ({@code TKONG}) is set. */
    public static boolean hasFillKong(int power) {
        return (power & TKONG) != 0;
    }

    /** Returns whether the 抓听 bit ({@code TWAIT}) is set. */
    public static boolean hasDrawWait(int power) {
        return (power & TWAIT) != 0;
    }

    /** Returns whether the 吃听 bit ({@code CWAIT}) is set. */
    public static boolean hasChowWait(int power) {
        return (power & CWAIT) != 0;
    }

    /** Returns whether the 碰听 bit ({@code PWAIT}) is set. */
    public static boolean hasPungWait(int power) {
        return (power & PWAIT) != 0;
    }

    /** Returns whether the 补 bit ({@code REPLACE}) is set. */
    public static boolean hasReplace(int power) {
        return (power & REPLACE) != 0;
    }
}
