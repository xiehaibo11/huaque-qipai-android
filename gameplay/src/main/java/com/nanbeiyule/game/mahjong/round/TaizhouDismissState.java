package com.nanbeiyule.game.mahjong.round;

import com.nanbeiyule.game.mahjong.TaizhouDismissStatus;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 解散投票的座位状态，还原 {@code GameBase/Modules/Dismiss} 的
 * {@code EVENT_UPDATE_DISMISS_UI} 数据面。
 *
 * <p>{@code onMsgDismissCountDown}(:240-268) 每次都用 {@code iAgrees} 全量重刷：
 * {@code 0 → REFUSE}、{@code 1 或发起座位 → AGREE}、其余 {@code DEFAULT}；
 * {@code onMsgRespondDismiss}(:216-238) 单点刷新一个座位。
 */
public final class TaizhouDismissState {
    /** 一个座位的投票展示。 */
    public record SeatVote(int seat, String nickname, String avatarUrl,
            TaizhouDismissStatus status) {}

    private final Map<Integer, TaizhouDismissStatus> statuses = new LinkedHashMap<>();
    private int requestSeat = -1;
    private String requestNickname = "";
    private int remainingSeconds;

    /** {@code onMsgRequestDismiss}/{@code Status.REQUEST}：记录发起人。 */
    public void applyRequest(int seat, String nickname, int seconds) {
        requestSeat = seat;
        requestNickname = nickname == null ? "" : nickname;
        remainingSeconds = Math.max(0, seconds);
        statuses.put(seat, TaizhouDismissStatus.REQUEST);
    }

    /** {@code onMsgRespondDismiss}：单个座位的同意/拒绝。 */
    public void applyResponse(int seat, boolean agree) {
        statuses.put(seat,
                agree ? TaizhouDismissStatus.AGREE : TaizhouDismissStatus.REFUSE);
    }

    /** {@code onMsgDismissCountDown}：按 {@code iAgrees} 全量重刷。 */
    public void applyCountdown(int requestingSeat, int seconds, List<Integer> agreements) {
        requestSeat = requestingSeat;
        remainingSeconds = Math.max(0, seconds);
        statuses.clear();
        for (int seat = 0; seat < agreements.size(); seat++) {
            Integer agreement = agreements.get(seat);
            if (agreement != null && agreement == 0) {
                statuses.put(seat, TaizhouDismissStatus.REFUSE);
            } else if ((agreement != null && agreement == 1) || seat == requestingSeat) {
                statuses.put(seat, TaizhouDismissStatus.AGREE);
            } else {
                statuses.put(seat, TaizhouDismissStatus.DEFAULT);
            }
        }
    }

    /**
     * {@code onMsgDismissCountDown}(:252-258) 的收层判定：两人房两票同意、
     * 多人房全票同意、或出现任一拒绝，都不再显示投票层。
     */
    public boolean shouldHide(int playerCount) {
        int agree = 0;
        int refuse = 0;
        for (TaizhouDismissStatus status : statuses.values()) {
            if (status == TaizhouDismissStatus.REFUSE) {
                refuse++;
            } else if (status == TaizhouDismissStatus.AGREE
                    || status == TaizhouDismissStatus.REQUEST) {
                agree++;
            }
        }
        return refuse > 0
                || (playerCount == 2 && agree == 2)
                || (playerCount > 2 && agree >= playerCount);
    }

    public TaizhouDismissStatus statusOf(int seat) {
        return statuses.getOrDefault(seat, TaizhouDismissStatus.DEFAULT);
    }

    public int requestSeat() {
        return requestSeat;
    }

    public String requestNickname() {
        return requestNickname;
    }

    public int remainingSeconds() {
        return remainingSeconds;
    }

    /** {@code startclock}(:127-152) 每秒递减，到 0 停止。 */
    public void tick() {
        if (remainingSeconds > 0) {
            remainingSeconds--;
        }
    }

    public List<Integer> seats() {
        return new ArrayList<>(statuses.keySet());
    }

    public void clear() {
        statuses.clear();
        requestSeat = -1;
        requestNickname = "";
        remainingSeconds = 0;
    }
}
