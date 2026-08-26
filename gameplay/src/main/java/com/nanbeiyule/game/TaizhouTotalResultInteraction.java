package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayTableState;

/** 原版 BigWinLost 模态页的本地打开状态及返回大厅/分享按钮命中。 */
final class TaizhouTotalResultInteraction {
    enum Action {
        BACK_LOBBY,
        SHARE
    }

    private boolean entered;

    boolean enter(GameplayTableState state) {
        entered = available(state);
        return entered;
    }

    boolean showing(GameplayTableState state) {
        return entered && available(state);
    }

    void onSnapshot(GameplayTableState state) {
        if (!available(state)) {
            entered = false;
        }
    }

    void close() {
        entered = false;
    }

    Action actionAt(GameplayTableState state, float designX, float designY) {
        if (!showing(state)) {
            return null;
        }
        if (TaizhouTotalResultLayout.BUTTON_BACK_LOBBY.contains(designX, designY)
                || TaizhouTotalResultLayout.TOP_BACK.contains(designX, designY)) {
            return Action.BACK_LOBBY;
        }
        if (TaizhouTotalResultLayout.BUTTON_SHARE.contains(designX, designY)) {
            return Action.SHARE;
        }
        return null;
    }

    private static boolean available(GameplayTableState state) {
        return TaizhouSettleInteraction.hasVisibleTotalResult(state);
    }
}
