package com.nanbeiyule.game.mahjong.round;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 各座位的托管标志，还原 {@code GameBase/Modules/Trust/Module.luac} 的 {@code _trustSeat}。
 *
 * <p>{@code onMsgTrust}(:19-38) 用 {@code sTrustSeat ?? nSeat} 取座位，用
 * {@code bTrust ?? (nFlag==1) ?? bState ?? false} 取标志；{@code onMsgGameEnd}(:48-54)
 * 在 {@code msgGameEnd}/{@code msgEndGame} 到达时把全部座位清零并收起托管层。
 */
public final class MahjongTrustState {
    private final Map<Integer, Boolean> trustedSeats = new HashMap<>();

    /** {@code onMsgTrust}：写入一个座位的托管标志。 */
    public void apply(int seat, boolean trusted) {
        if (trusted) {
            trustedSeats.put(seat, Boolean.TRUE);
        } else {
            trustedSeats.remove(seat);
        }
    }

    /** {@code getTrustState(seat)}。 */
    public boolean isTrusted(int seat) {
        return trustedSeats.containsKey(seat);
    }

    /** {@code onMsgGameEnd}：一局结束清空全部托管。 */
    public void clear() {
        trustedSeats.clear();
    }

    public boolean isEmpty() {
        return trustedSeats.isEmpty();
    }

    /** 只读快照，供头像层渲染托管角标。 */
    public Map<Integer, Boolean> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(trustedSeats));
    }
}
