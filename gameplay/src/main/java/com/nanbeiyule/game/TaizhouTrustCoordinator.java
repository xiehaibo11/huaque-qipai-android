package com.nanbeiyule.game;

import android.app.Activity;
import com.nanbeiyule.game.gameplay.GameplayEvent;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.round.MahjongTrustState;
import java.util.List;
import org.json.JSONObject;

/**
 * 托管模块的运行期归属，还原 {@code GameBase/Modules/Trust/Module.luac}。
 *
 * <p>{@code onMsgTrust}(:19-38) 收到托管消息后先写座位标志并广播
 * {@code EVENT_UPDATE_TRUST_DATA}，再判断 {@code checkShowTrust}——只有自己座位才
 * 弹/收托管层；{@code onMsgGameEnd}(:48-54) 在牌局结束时清空全部座位并收层。
 * 回放与旁观（{@code isPlayBack/getIsSeer}）只更新数据、不弹层。
 */
final class TaizhouTrustCoordinator {
    /** 后端托管事件；载荷 {@code {"seat":int,"trusted":bool,"punishSeconds":int}}。 */
    private static final String EVENT_TRUST = "TRUST";
    /** {@code msgGameEnd}/{@code msgEndGame} 对应的本地事件，收到即清空托管。 */
    private static final String EVENT_ROUND_RESULT = "ROUND_RESULT";

    private final Activity owner;
    private final Runnable cancelTrust;
    private final MahjongTrustState trustState = new MahjongTrustState();
    private TaizhouTrustDialog dialog;
    private int punishSeconds;

    TaizhouTrustCoordinator(Activity owner, Runnable cancelTrust) {
        this.owner = owner;
        this.cancelTrust = cancelTrust;
    }

    MahjongTrustState state() {
        return trustState;
    }

    void onEvents(List<GameplayEvent> events, GameplayTableState state) {
        for (GameplayEvent event : events) {
            if (EVENT_ROUND_RESULT.equals(event.type())) {
                trustState.clear();
                dismiss();
                continue;
            }
            if (!EVENT_TRUST.equals(event.type())) {
                continue;
            }
            JSONObject payload = event.payload();
            int seat = payload.optInt("seat", -1);
            if (seat < 0) {
                continue;
            }
            boolean trusted = payload.optBoolean("trusted", false);
            punishSeconds = payload.optInt("punishSeconds", punishSeconds);
            trustState.apply(seat, trusted);
            if (state == null || seat != state.mySeat()) {
                continue;
            }
            if (trusted) {
                show();
            } else {
                dismiss();
            }
        }
    }

    /** {@code Trust/Module.luac:59-62 showTrustView}：同名层已在场时不重复添加。 */
    private void show() {
        if (owner.isFinishing() || dialog != null) {
            return;
        }
        TaizhouTrustDialog next =
                new TaizhouTrustDialog(owner, punishSeconds, cancelTrust::run);
        dialog = next;
        next.setOnDismissListener(
                ignored -> {
                    if (dialog == next) {
                        dialog = null;
                    }
                });
        next.show();
    }

    void dismiss() {
        TaizhouTrustDialog current = dialog;
        dialog = null;
        if (current != null && current.isShowing()) {
            current.dismiss();
        }
    }

    /** {@code onGameStart} 与离桌都要收层，避免托管层挂在下一局。 */
    void close() {
        trustState.clear();
        dismiss();
    }
}
