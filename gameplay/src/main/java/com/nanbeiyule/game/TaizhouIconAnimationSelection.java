package com.nanbeiyule.game;

import java.util.Random;
import java.util.function.IntUnaryOperator;

/**
 * 原版 {@code IconAnimationManager} 的本局动画抽选。
 *
 * <p>{@code game/GameBase/Modules/IconAnimationManager/Module.lua:19-26} 在模块构造时执行一次
 * {@code self._playType = math.random(0, 4)}，之后所有图标视图在自己的 {@code ctor} 里用
 * {@code GetPlayAnimationIndex()} 与自身 {@code PlayType} 比对，决定播主动画还是退回静态图/次要
 * 动画。抽选每进一次房间只做一次，不随图标重建或页面刷新改变。
 *
 * <p>{@code math.random(0, 4)} 两端闭区间，因此五个 {@code PlayType} 概率均等，且每局至多只有
 * 一个图标处于主动画状态；抽中 {@code XIA_GUANG} 或 {@code RECALL_NEW} 时，当前等待桌上的三个
 * 图标全部处于未命中分支，这是原版行为，不是资源缺失。
 */
final class TaizhouIconAnimationSelection {
    /** 与原版 {@code IconAnimationManagerModule.PlayType} 逐值对应。 */
    enum PlayType {
        LUCKY_MISSION(0),
        XIA_GUANG(1),
        JU_BAO_PEN(2),
        QING_CAI_SHEN(3),
        RECALL_NEW(4);

        private final int value;

        PlayType(int value) {
            this.value = value;
        }

        int value() {
            return value;
        }

        static PlayType of(int value) {
            for (PlayType type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException("unknown PlayType: " + value);
        }
    }

    static final int PLAY_TYPE_COUNT = 5;

    private final PlayType playType;

    /** 生产入口：等价于原版的 {@code math.random(0, 4)}。 */
    TaizhouIconAnimationSelection() {
        this(bound -> new Random().nextInt(bound));
    }

    /** 注入随机源，便于测试逐个分支而不依赖运气。 */
    TaizhouIconAnimationSelection(IntUnaryOperator randomIndex) {
        int drawn = randomIndex.applyAsInt(PLAY_TYPE_COUNT);
        if (drawn < 0 || drawn >= PLAY_TYPE_COUNT) {
            throw new IllegalStateException("PlayType draw out of range: " + drawn);
        }
        this.playType = PlayType.of(drawn);
    }

    PlayType playType() {
        return playType;
    }

    /** 请财神：命中播 {@code zzb_qcs_icon} 的 {@code loop}，否则画静态图标。 */
    boolean caishenAnimated() {
        return playType == PlayType.QING_CAI_SHEN;
    }

    /** 聚宝盆：命中播 {@code zzb_jbp_icon} 的 {@code animation}，否则画静态图标。 */
    boolean treasurePotAnimated() {
        return playType == PlayType.JU_BAO_PEN;
    }

    /**
     * 福利任务：两种情况都播骨骼，只是动画名不同。
     *
     * <p>{@code LuckyMission/IconView.lua:12} 为
     * {@code aniName = ... == PlayType.LuckyMission and "animation" or "animation2"}。
     */
    String luckyMissionAnimation() {
        return playType == PlayType.LUCKY_MISSION ? "animation" : "animation2";
    }
}
