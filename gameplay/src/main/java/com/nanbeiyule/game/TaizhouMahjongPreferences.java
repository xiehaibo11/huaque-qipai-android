package com.nanbeiyule.game;

import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;
import java.util.Objects;

/** Device-local Taizhou options recovered from the original Mahjong settings. */
record TaizhouMahjongPreferences(
        boolean dialectEnabled,
        TaizhouMahjongPlayGesture.Mode playMode,
        boolean tingHintEnabled,
        boolean traceEnabled,
        boolean pureModeEnabled) {
    TaizhouMahjongPreferences {
        playMode = Objects.requireNonNull(playMode, "playMode");
    }

    TaizhouMahjongPreferences(boolean dialectEnabled, TaizhouMahjongPlayGesture.Mode playMode) {
        this(
                dialectEnabled,
                playMode,
                defaults().tingHintEnabled(),
                defaults().traceEnabled(),
                defaults().pureModeEnabled());
    }

    TaizhouMahjongPreferences(
            boolean dialectEnabled,
            TaizhouMahjongPlayGesture.Mode playMode,
            boolean tingHintEnabled) {
        this(
                dialectEnabled,
                playMode,
                tingHintEnabled,
                defaults().traceEnabled(),
                defaults().pureModeEnabled());
    }

    static TaizhouMahjongPreferences defaults() {
        // 原版 UIMahConfig2D.MahSettingDefault[PlayType] 默认 SINGLE_CLICK。
        // 原版 HAVE_TING 默认开启、出牌轨迹（configTab.MahIsHaveTrace=4）与纯净模式
        // （configTab.ClearModel=10）默认关闭：麻将 1.0.0.687 基类 SettingData 的
        // _curDefault 赋值未随文件归档（反编译缺口），默认值以金币包同脉
        // MahXueLiu SettingData.lua:87 defaultCfg={100,1,100,0,2,1,1,1,1,0,1,1,2}
        // 的第 4/9/10 位（0/1/0）为准——标注为同级证据，非 30109 直接证据。
        return new TaizhouMahjongPreferences(
                true, TaizhouMahjongPlayGesture.Mode.SINGLE_CLICK, true, false, false);
    }

    TaizhouMahjongPreferences withDialectEnabled(boolean enabled) {
        return new TaizhouMahjongPreferences(
                enabled, playMode, tingHintEnabled, traceEnabled, pureModeEnabled);
    }

    TaizhouMahjongPreferences withPlayMode(TaizhouMahjongPlayGesture.Mode mode) {
        return new TaizhouMahjongPreferences(
                dialectEnabled, mode, tingHintEnabled, traceEnabled, pureModeEnabled);
    }

    /** 原版 SettingData:setHaveTing/getHaveTing（存档键 HAVE_TING）听牌提示开关。 */
    TaizhouMahjongPreferences withTingHintEnabled(boolean enabled) {
        return new TaizhouMahjongPreferences(
                dialectEnabled, playMode, enabled, traceEnabled, pureModeEnabled);
    }

    /**
     * 原版 SettingData:setMahIsHaveTrace（存档键 MAH_IS_HAVE_TRACE）出牌轨迹开关。
     * 当前仅持久化：轨迹动画属 3D 链路，2D 基类 onEventShowOutMahAction 空实现，
     * 30109 2D 效果证据不足（见缺陷评估第 9 项）。
     */
    TaizhouMahjongPreferences withTraceEnabled(boolean enabled) {
        return new TaizhouMahjongPreferences(
                dialectEnabled, playMode, tingHintEnabled, enabled, pureModeEnabled);
    }

    /**
     * 原版 SettingData:setIsClearModel（EVENT_CLEAR_MODEL）纯净模式开关。当前仅持久化：
     * 其效果目标是道具背包系统的装饰牌背图案（UIMah.luac:83/99
     * _setBackPatternImgVisible(not getIsClearModel())），Android 牌桌尚未渲染该类
     * 装饰图案；将来接入牌背装饰时必须读取本开关。
     */
    TaizhouMahjongPreferences withPureModeEnabled(boolean enabled) {
        return new TaizhouMahjongPreferences(
                dialectEnabled, playMode, tingHintEnabled, traceEnabled, enabled);
    }
}
