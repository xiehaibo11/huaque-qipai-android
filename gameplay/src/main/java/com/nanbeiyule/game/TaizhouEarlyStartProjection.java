package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplaySeat;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouEarlyStartLayout;

/**
 * 提前开局按钮（{@code TableInfo.csb} 的 {@code _KW_BTN_EARLY_START}）的显示与命中投影。
 *
 * <p>原版证据：{@code EarlyStart/Module.luac:29} 只在
 * {@code curPlayerCount >= getMinPlayer()} 时下发提前开局；{@code TableInfoLayer.luac:115-125}
 * 按当前人数写「N人也能开！」气泡；{@code TableInfoLayer.luac:132-145} 在自己已准备后把按钮
 * 从 Y=528 挪到 Y=388。原版客户端不判断房主（由服务端下发驱动）；南北娱乐 QA 后端只在
 * WAITING 且房主时受理 {@code EARLY_START}，因此这里只对房主显示——属于 QA 链路的已标注
 * 收紧，不是原版多人同意流。
 */
public final class TaizhouEarlyStartProjection {
    /** 台州四人桌的最少开局人数（Module.luac:29 的 getMinPlayer 语义）。 */
    private static final int MIN_PLAYERS = 2;

    private TaizhouEarlyStartProjection() {}

    /** WAITING + 房主 + 人数在 [2, chairCount) 时显示。 */
    public static boolean showButton(GameplayTableState state) {
        if (state == null
                || state.phase() != GameplayPhase.WAITING
                || state.seats().size() < MIN_PLAYERS
                || state.seats().size() >= state.chairCount()) {
            return false;
        }
        for (GameplaySeat seat : state.seats()) {
            if (seat.seatNumber() == state.mySeat()) {
                return seat.host();
            }
        }
        return false;
    }

    /**
     * 活路径是 {@code CenterBtnsLayer.csb} 的 {@code _KW_BTN_QUICK}，单一位置 (960,692)。
     *
     * <p>原来这里返回的 {@code TableInfo.csb} 双态按钮（Cocos Y 528/388）属死节点：
     * 置显它的 {@code TableInfoLayer.luac:115-129 earlyBeginStart} 在 1.0.0.687 无任何
     * 调用点，详见 {@link TaizhouEarlyStartLayout} 的类注释。
     */
    public static TaizhouEarlyStartLayout.Node buttonNode(GameplayTableState state) {
        return TaizhouEarlyStartLayout.BUTTON;
    }

    /**
     * 气泡文案。原版 {@code CenterBtns/View.luac:31} 是
     * {@code playerCount.."人也能开"}，**无感叹号**；带感叹号的
     * {@code "%d人也能开!"} 属死路径 {@code TableInfoLayer.luac:121}。
     */
    public static String tipText(GameplayTableState state) {
        return TaizhouEarlyStartLayout.quickTipText(state.seats().size());
    }

    public static boolean hitButton(GameplayTableState state, float designX, float designY) {
        return showButton(state) && buttonNode(state).contains(designX, designY);
    }

    private static boolean localReady(GameplayTableState state) {
        for (GameplaySeat seat : state.seats()) {
            if (seat.seatNumber() == state.mySeat()) {
                return seat.ready();
            }
        }
        return false;
    }
}
