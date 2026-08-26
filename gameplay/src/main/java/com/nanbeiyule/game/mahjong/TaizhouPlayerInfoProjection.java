package com.nanbeiyule.game.mahjong;

import com.nanbeiyule.game.gameplay.GameplaySeat;
import com.nanbeiyule.game.gameplay.GameplayTableState;

/**
 * 头像命中测试：把设计坐标映射回原始座位号。
 *
 * <p>原版 {@code BasicMahjong/Modules/Player} 的 {@code HeadNode} 整块可点，点开
 * {@code PlayerInfoLayer.csb}；这里用 {@code PlayerLayer.csb} 的
 * {@link TaizhouMahjongPlayerLayout#HEAD_WIDTH}×{@link TaizhouMahjongPlayerLayout#HEAD_HEIGHT}
 * 头像框做同样的命中范围。
 */
public final class TaizhouPlayerInfoProjection {
    private TaizhouPlayerInfoProjection() {}

    /** 命中的座位号；没命中返回 {@code -1}。 */
    public static int seatAt(GameplayTableState state, float designX, float designY) {
        if (state == null) {
            return -1;
        }
        for (GameplaySeat seat : state.seats()) {
            int localSeat =
                    TaizhouMahjongSeatMapper.toLocalSeat(
                            seat.seatNumber(), state.mySeat(), state.chairCount());
            TaizhouMahjongPlayerLayout.PlayerSlot slot;
            try {
                slot = TaizhouMahjongPlayerLayout.forLocalSeat(localSeat);
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            float halfWidth = TaizhouMahjongPlayerLayout.HEAD_WIDTH / 2.0f;
            float halfHeight = TaizhouMahjongPlayerLayout.HEAD_HEIGHT / 2.0f;
            if (designX >= slot.centerX() - halfWidth
                    && designX <= slot.centerX() + halfWidth
                    && designY >= slot.centerY() - halfHeight
                    && designY <= slot.centerY() + halfHeight) {
                return seat.seatNumber();
            }
        }
        return -1;
    }
}
