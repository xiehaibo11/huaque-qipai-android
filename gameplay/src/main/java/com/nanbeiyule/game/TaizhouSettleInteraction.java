package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouSettleLayout;

/**
 * 结算页（Settle.csb）三按钮的命中与「查看牌桌」回看状态。
 *
 * <p>行为证据（{@code BasicMahjong/Modules/WinLost/View.luac}）：
 * <ul>
 *   <li>查看牌桌 = {@code onCheckTableClicked}（:396-400）收起结算窗。原版的恢复入口是
 *       CenterBtns 的 {@code _KW_BTN_SHOW_SETTLE} 按钮（CenterBtns/View.luac:104-108）；
 *       CenterBtns 的 {@code _KW_BTN_SHOW_SETTLE} 按钮（CenterBtns/View.luac:104-108）；
 *       非末局回看态沿用当前已验证的「再点牌桌任意处恢复」入口。
 *   <li>下一局 = {@code onNextGameClicked}（:411-437）；局尽时同一入口打开 BigWinLost。
 *   <li>洗牌 = 房卡 DIRECT 分支（initShuffle :200-254），接既有房间工具洗牌预约。
 * </ul>
 */
final class TaizhouSettleInteraction {
    enum Action {
        CHECK_TABLE,
        SHUFFLE,
        NEXT_ROUND,
        TOTAL_RESULT
    }

    /** 查看牌桌回看态：结算层暂时收起，下一次点击恢复。 */
    private boolean reviewingTable;

    /** 结算层是否处于显示阶段；末局会话已完成时仍先显示单局结算。 */
    static boolean showing(GameplayTableState state) {
        return state != null
                && (state.phase() == GameplayPhase.ROUND_RESULT
                        || state.phase() == GameplayPhase.COMPLETED)
                && state.settlement().isPresent();
    }

    /** 结算层显示且不在回看态时拦截全部触摸（原版是模态整页）。 */
    boolean isBlocking(GameplayTableState state) {
        return showing(state) && !reviewingTable;
    }

    boolean reviewingTable() {
        return reviewingTable;
    }

    void enterReview() {
        reviewingTable = true;
    }

    void exitReview() {
        reviewingTable = false;
    }

    /** 新快照到达时复位回看态。 */
    void reset() {
        reviewingTable = false;
    }

    /** 命中三按钮之一；未命中返回 null（调用方仍然吞掉，保持模态）。 */
    static Action actionAt(GameplayTableState state, float designX, float designY) {
        if (!showing(state)) {
            return null;
        }
        // BasicMahjong/Modules/WinLost/View.luac:updateBtnInEnd：末局隐藏查看桌面、洗牌与
        // 下一局，只在中间显示 _KW_BTN_CHECK_BILL，并复用 onNextGameClicked 打开大结算。
        if (hasVisibleTotalResult(state)) {
            return TaizhouSettleLayout.BUTTON_CHECK_BILL.contains(designX, designY)
                    ? Action.TOTAL_RESULT
                    : null;
        }
        if (TaizhouSettleLayout.BUTTON_CHECK_TABLE.contains(designX, designY)) {
            return Action.CHECK_TABLE;
        }
        if (TaizhouSettleLayout.BUTTON_SHUFFLE.contains(designX, designY)) {
            return Action.SHUFFLE;
        }
        if (TaizhouSettleLayout.BUTTON_NEXT.contains(designX, designY)) {
            return Action.NEXT_ROUND;
        }
        return null;
    }

    /** 局尽时不再向服务端请求下一局，改由本地查看账单入口打开权威大结算。 */
    static boolean canRequestNextRound(GameplayTableState state) {
        return state != null
                && state.totalResult().isEmpty()
                && state.roundNumber() < state.maxPlayCount();
    }

    static boolean hasVisibleTotalResult(GameplayTableState state) {
        return state != null
                && state.totalResult().filter(result -> result.show()).isPresent();
    }
}
