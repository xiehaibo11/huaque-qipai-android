package com.nanbeiyule.game;

import android.os.SystemClock;
import android.view.View;

/**
 * 等待桌底部三图标骨骼动画的驱动器：墙钟换算、循环重绘调度与可见性生命周期。
 * 从 {@link TaizhouMahjongTableView} 拆出以控制视图行数；动画时间轴从视图创建
 * 起算，与原版图标视图在 ctor 里起播一致。暂停/恢复/释放的冻结语义由
 * {@link TaizhouWaitingIconSpinePlayer} 锁定（暂停时墙钟增量不积分）。
 */
final class TaizhouIconEffectsDriver {
    private final TaizhouWaitingIconEffects effects;
    private final long startedAt = SystemClock.elapsedRealtime();

    TaizhouIconEffectsDriver(TaizhouWaitingIconEffects effects) {
        this.effects = effects;
    }

    /** 动画时间轴（秒）；暂停期间墙钟继续走但播放器不积分，恢复后不跳帧。 */
    float elapsedSeconds() {
        return (SystemClock.elapsedRealtime() - startedAt) / 1000.0f;
    }

    /** 骨骼资源可用时驱动循环重绘；缺资源保持按需重绘，不空转。 */
    void scheduleFrame(View view) {
        if (effects.available()) {
            view.postInvalidateOnAnimation();
        }
    }

    /** 窗口不可见时冻结动画时钟，恢复可见时继续。 */
    void onWindowVisibilityChanged(int visibility) {
        if (visibility == View.VISIBLE) {
            effects.resume();
        } else {
            effects.pause();
        }
    }

    /** 视图离窗时冻结时钟并释放着色器与页面位图。 */
    void release() {
        effects.pause();
        effects.release();
    }
}
