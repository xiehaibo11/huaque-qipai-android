package com.nanbeiyule.game.mahjong;

/**
 * 当前生效的牌面外观，对应原版全局的 {@code CF.settingData}。
 *
 * <p>原版牌桌上每张 {@code UIMah} 都监听 {@code EVENT_MAHJONG_CONFIG}，设置页一改就整桌刷新；
 * 这里同样用一处全局值，牌桌的绘制与命中测试都读它，避免两边算出不同的牌尺寸。
 */
public final class MahjongSettingData {
    private static volatile MahjongTileAppearance appearance =
            MahjongTileAppearance.area7109Defaults();

    private MahjongSettingData() {}

    public static MahjongTileAppearance appearance() {
        return appearance;
    }

    /** {@code CF.settingData:dispatchMahjongCfg}。 */
    public static void setAppearance(MahjongTileAppearance next) {
        appearance = next == null ? MahjongTileAppearance.area7109Defaults() : next;
    }
}
