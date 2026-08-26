package com.nanbeiyule.game;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.nanbeiyule.game.gameplay.GameplayEvent;
import com.nanbeiyule.game.gameplay.GameplaySeat;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.round.TaizhouDismissState;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 解散模块的运行期归属，还原 {@code GameBase/Modules/Dismiss/Module.luac}。
 *
 * <p>{@code onMsgDismissCountDown}(:240-268) 把全量 {@code iAgrees} 刷进投票层，并按
 * {@code :252-258} 判定是否收层；{@code onMsgDismissFlag}(:106-141) 用 flag 1/2/3 决定
 * 「房主已解散包厢!」「多数玩家同意解散包厢!」「等待时间过长解散包厢!」三条提示，
 * 其中 flag=1 且未开局时房主直接回大厅、其他人点确认后回大厅（{@code checkIsNeedLeaveFunc}）。
 */
final class TaizhouDismissCoordinator {
    private static final String EVENT_DISMISS_COUNTDOWN = "DISMISS_COUNTDOWN";
    private static final String EVENT_DISMISS_RESPONDED = "DISMISS_RESPONDED";
    private static final String EVENT_DISMISS_FLAG = "DISMISS_FLAG";

    private final Activity owner;
    private final Runnable leaveTable;
    private final TaizhouDismissState state = new TaizhouDismissState();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = this::onTick;
    private RespondListener respondListener = agree -> {};
    private TaizhouDismissDialog dialog;
    private boolean responded;

    interface RespondListener {
        void onRespond(boolean agree);
    }

    TaizhouDismissCoordinator(Activity owner, Runnable leaveTable) {
        this.owner = owner;
        this.leaveTable = leaveTable;
    }

    void setRespondListener(RespondListener listener) {
        respondListener = listener == null ? agree -> {} : listener;
    }

    TaizhouDismissState state() {
        return state;
    }

    void onEvents(List<GameplayEvent> events, GameplayTableState tableState) {
        for (GameplayEvent event : events) {
            switch (event.type()) {
                case EVENT_DISMISS_COUNTDOWN -> onCountdown(event.payload(), tableState);
                case EVENT_DISMISS_RESPONDED -> onResponded(event.payload(), tableState);
                case EVENT_DISMISS_FLAG -> onFlag(event.payload(), tableState);
                default -> {
                    // 其余事件与解散无关。
                }
            }
        }
    }

    private void onCountdown(JSONObject payload, GameplayTableState tableState) {
        int requestSeat = payload.optInt("requestSeat", -1);
        int seconds = payload.optInt("seconds", 0);
        List<Integer> agreements = new ArrayList<>();
        JSONArray array = payload.optJSONArray("agreements");
        for (int index = 0; array != null && index < array.length(); index++) {
            agreements.add(array.isNull(index) ? null : array.optInt(index));
        }
        state.applyCountdown(requestSeat, seconds, agreements);
        state.applyRequest(requestSeat, nicknameOf(tableState, requestSeat), seconds);
        int playerCount = tableState == null ? agreements.size() : tableState.seats().size();
        if (state.shouldHide(playerCount)) {
            dismiss();
            return;
        }
        show(tableState);
    }

    private void onResponded(JSONObject payload, GameplayTableState tableState) {
        int seat = payload.optInt("seat", -1);
        boolean agree = payload.optBoolean("agree", false);
        if (seat < 0) {
            return;
        }
        state.applyResponse(seat, agree);
        if (!agree) {
            if (tableState != null && seat != tableState.mySeat()) {
                Toast.makeText(
                                owner,
                                nicknameOf(tableState, seat) + "不同意解散包厢!",
                                Toast.LENGTH_SHORT)
                        .show();
            }
            dismiss();
            return;
        }
        if (dialog != null) {
            dialog.refresh();
        }
    }

    /** {@code getTipStr}(:151-161) 的三条结果文案。 */
    private void onFlag(JSONObject payload, GameplayTableState tableState) {
        int flag = payload.optInt("flag", 0);
        String message =
                switch (flag) {
                    case 1 -> "房主已解散包厢!";
                    case 2 -> "多数玩家同意解散包厢!";
                    case 3 -> "等待时间过长解散包厢!";
                    default -> null;
                };
        dismiss();
        if (message != null) {
            Toast.makeText(owner, message, Toast.LENGTH_LONG).show();
        }
        // 原版 flag=1 且未开局时房主直接回大厅，其余情况提示后回大厅。
        leaveTable.run();
    }

    private void show(GameplayTableState tableState) {
        if (owner.isFinishing() || tableState == null) {
            return;
        }
        if (dialog != null) {
            dialog.refresh();
            scheduleTick();
            return;
        }
        List<TaizhouDismissView.Seat> seats = new ArrayList<>();
        for (GameplaySeat seat : tableState.seats()) {
            seats.add(
                    new TaizhouDismissView.Seat(
                            seat.seatNumber(), seat.displayName(), null));
        }
        boolean selfResponded = responded || state.requestSeat() == tableState.mySeat();
        TaizhouDismissDialog next =
                new TaizhouDismissDialog(
                        owner,
                        state,
                        seats,
                        false,
                        selfResponded,
                        agree -> {
                            responded = true;
                            respondListener.onRespond(agree);
                        });
        dialog = next;
        next.setOnDismissListener(
                ignored -> {
                    if (dialog == next) {
                        dialog = null;
                        handler.removeCallbacks(tick);
                    }
                });
        next.show();
        scheduleTick();
    }

    /** {@code startclock}(:127-152)：每秒刷新一次剩余秒数。 */
    private void scheduleTick() {
        handler.removeCallbacks(tick);
        if (state.remainingSeconds() > 0) {
            handler.postDelayed(tick, 1_000L);
        }
    }

    private void onTick() {
        state.tick();
        if (dialog != null) {
            dialog.refresh();
            scheduleTick();
        }
    }

    private static String nicknameOf(GameplayTableState tableState, int seat) {
        if (tableState == null) {
            return "";
        }
        for (GameplaySeat candidate : tableState.seats()) {
            if (candidate.seatNumber() == seat) {
                return candidate.displayName();
            }
        }
        return "";
    }

    void dismiss() {
        TaizhouDismissDialog current = dialog;
        dialog = null;
        handler.removeCallbacks(tick);
        if (current != null && current.isShowing()) {
            current.dismiss();
        }
    }

    void close() {
        state.clear();
        responded = false;
        dismiss();
    }
}
