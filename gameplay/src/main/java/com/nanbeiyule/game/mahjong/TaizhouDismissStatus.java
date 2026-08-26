package com.nanbeiyule.game.mahjong;

/**
 * {@code GameBase/Modules/Dismiss/View.luac:8-13} 的 {@code DismissView.Status}。
 *
 * <p>原版是 1..4 的数字下标，文案与颜色表按同一下标取值；这里保留原始序号，
 * 便于与 {@code onMsgDismissCountDown}(:240-268) 的 {@code iAgrees} 判定对齐。
 */
public enum TaizhouDismissStatus {
    /** {@code DEFAULT = 1} 选择中。 */
    DEFAULT(1),
    /** {@code AGREE = 2} 同意。 */
    AGREE(2),
    /** {@code REFUSE = 3} 拒绝。 */
    REFUSE(3),
    /** {@code REQUEST = 4} 发起人，文案与颜色同「同意」。 */
    REQUEST(4);

    private final int value;

    TaizhouDismissStatus(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
