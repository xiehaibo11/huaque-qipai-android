package com.nanbeiyule.game;

import com.nanbeiyule.game.spine37.Spine37Runtime;
import java.util.List;

/**
 * 等待桌三图标的逐帧播放器：持有一条共享的可暂停动画时钟，按 {@link TaizhouWaitingIconSpineCatalog}
 * 里已装载的运行时输出逐帧 pose。
 *
 * <p>循环规则证据（全部为无限循环，不设间隔）：
 *
 * <ul>
 *   <li>请财神 {@code Guide/GamePropView.lua}：
 *       {@code SpineManager:playAni(..., "zzb_qcs_icon", "loop", true)}，
 *       {@code SpineManager.lua:77} 落到 {@code setAnimation(trackid, aniName, loop)}；
 *   <li>聚宝盆 {@code JuBaoPen/JuBaoPenIconView.lua:10}：
 *       {@code playAni(..., "zzb_jbp_icon", "animation", true)}；
 *   <li>福利任务 {@code LuckyMission/IconView.lua:11-15}：
 *       {@code display.playDargonBonesSpine} 未传 {@code loop}，
 *       {@code cocos/framework/display.lua:717} 默认 {@code true}。
 * </ul>
 *
 * <p>无限循环由 {@code Spine37Runtime.sample} 的 {@code wrapTime} 实现，本类只累计时间。时钟按
 * 调用方墙钟的增量积分：视图不可见时合成层不再发起绘制，时钟自然停走，动画即暂停；暂停/恢复
 * 钩子留给视图可见性接线（{@link TaizhouWaitingIconEffects#pause()}）。
 */
final class TaizhouWaitingIconSpinePlayer {
    private final TaizhouWaitingIconSpineCatalog catalog;
    private float sampleTimeSeconds;
    private float lastWallClockSeconds = Float.NaN;
    private boolean playing = true;

    TaizhouWaitingIconSpinePlayer(TaizhouWaitingIconSpineCatalog catalog) {
        this.catalog = catalog;
    }

    /** 以调用方墙钟推进动画时钟；同一墙钟时间重复推进不重复计数。 */
    void advanceTo(float wallClockSeconds) {
        if (Float.isNaN(wallClockSeconds)) {
            return;
        }
        if (Float.isNaN(lastWallClockSeconds)) {
            lastWallClockSeconds = wallClockSeconds;
            return;
        }
        float delta = wallClockSeconds - lastWallClockSeconds;
        lastWallClockSeconds = wallClockSeconds;
        // 时钟回拨只重定基线，不倒流动画。
        if (playing && delta > 0.0f) {
            sampleTimeSeconds += delta;
        }
    }

    void pause() {
        playing = false;
    }

    void resume() {
        playing = true;
    }

    boolean playing() {
        return playing;
    }

    /** 当前动画时间轴读数，随 {@link #advanceTo} 累计、暂停时冻结。 */
    float sampleTimeSeconds() {
        return sampleTimeSeconds;
    }

    /** 按播放器时钟采样一帧；骨架已降级时返回空序列，合成层静默跳过、交还静态位图分支。 */
    List<Spine37Runtime.DrawCommand> sample(String skeleton, String animation) {
        OriginalLobbyEffectAssets.Loaded loaded = catalog.loaded(skeleton);
        if (loaded == null) {
            return List.of();
        }
        return loaded.runtime().sample(animation, sampleTimeSeconds);
    }
}
