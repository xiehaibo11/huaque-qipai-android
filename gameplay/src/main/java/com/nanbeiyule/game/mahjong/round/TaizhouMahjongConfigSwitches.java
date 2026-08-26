package com.nanbeiyule.game.mahjong.round;

/**
 * Final gameplay switches of 台州麻将 30109 after the
 * BasicMahjong → BasicTaiZhouMahjong → TaiZhouMahjong ConfigData/RoomData
 * override chain:
 *
 * <ul>
 *   <li>{@code haveTing}: Basic false
 *       (BasicMahjong/Data/ConfigData.luac:3-5) → BasicTaiZhou true
 *       (TaiZhou/BasicTaiZhouMahjong/Data/ConfigData.luac:3-5) → 30109 未覆写 = true
 *   <li>{@code localShuffle}: Basic true (:62-64) → BasicTaiZhou true (:8-10) = true
 *   <li>{@code settleGoldWindow}: Basic false (:57-59) → BasicTaiZhou true (:13-15)
 *       → 30109 false (TaiZhou/TaiZhouMahjong/Data/ConfigData.luac:14-16) = false
 *   <li>{@code laZiHu}: BasicTaiZhou false (:22-24) → 30109 true (:4-6) = true
 *   <li>{@code maiMa}: BasicTaiZhou false (:27-29)，30109 未覆写 = false
 *   <li>{@code maiDi}: BasicTaiZhou false (:32-34)，30109 未覆写 = false
 *   <li>{@code shengPaiJieDuan}: BasicTaiZhou false (:37-39) → 30109 true (:9-11) = true
 *   <li>{@code openCutCards}: BasicTaiZhou RoomData 固定 false
 *       (TaiZhou/BasicTaiZhouMahjong/Data/RoomData.luac:26-28)
 *   <li>{@code dynamicSeatCount}: 提前开局动态座位数 {@code _dynamicChairs}
 *       (RoomData.luac:5)，构造默认 0，运行期由服务端下发 (:9-15)
 *   <li>{@code maxHuCount}: 胡数封顶 {@code _maxHuCount} (RoomData.luac:6)，
 *       构造默认 0，运行期由服务端下发 (:17-23)
 * </ul>
 *
 * <p>These are client display/flow switches only; {@code localShuffle} 只是
 * 客户端本地洗牌表现开关，不代表本模型实现了任何洗牌或牌墙算法。
 */
public record TaizhouMahjongConfigSwitches(
        boolean haveTing,
        boolean localShuffle,
        boolean settleGoldWindow,
        boolean laZiHu,
        boolean maiMa,
        boolean maiDi,
        boolean shengPaiJieDuan,
        boolean openCutCards,
        int dynamicSeatCount,
        int maxHuCount) {
    public TaizhouMahjongConfigSwitches {
        if (dynamicSeatCount < 0) {
            throw new IllegalArgumentException("dynamicSeatCount must be non-negative");
        }
        if (maxHuCount < 0) {
            throw new IllegalArgumentException("maxHuCount must be non-negative");
        }
    }

    /** Returns the evidenced final 30109 values listed above. */
    public static TaizhouMahjongConfigSwitches taizhouMahjong() {
        return new TaizhouMahjongConfigSwitches(
                true, true, false, true, false, false, true, false, 0, 0);
    }

    public TaizhouMahjongConfigSwitches withDynamicSeatCount(int nextDynamicSeatCount) {
        return new TaizhouMahjongConfigSwitches(
                haveTing, localShuffle, settleGoldWindow, laZiHu, maiMa, maiDi,
                shengPaiJieDuan, openCutCards, nextDynamicSeatCount, maxHuCount);
    }

    public TaizhouMahjongConfigSwitches withMaxHuCount(int nextMaxHuCount) {
        return new TaizhouMahjongConfigSwitches(
                haveTing, localShuffle, settleGoldWindow, laZiHu, maiMa, maiDi,
                shengPaiJieDuan, openCutCards, dynamicSeatCount, nextMaxHuCount);
    }
}
