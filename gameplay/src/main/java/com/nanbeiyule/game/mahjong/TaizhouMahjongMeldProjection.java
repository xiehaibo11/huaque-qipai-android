package com.nanbeiyule.game.mahjong;

import com.nanbeiyule.game.gameplay.GameplayMeld;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个座位当前可绘制的副露，以及它把手牌推开的起点。
 *
 * <p>原版 {@code UIMahHandArea} 把 comb 和手牌放在同一个手牌区节点里：
 * {@code _updateCombsPosition} 先按 {@code HandAreaLayout} 排完所有 comb，
 * {@code _getHandMahsStartPos} 再把手牌起点挪到最后一组之后
 * （BOTTOM 走 {@code CombTotalLength}，其余三家走最后一组的 BoundingBox）。
 * 少了这一步，手牌和副露就会叠在同一处。
 */
public final class TaizhouMahjongMeldProjection {
    private TaizhouMahjongMeldProjection() {}

    /** 该服务端座位的副露，保持服务端下发顺序。 */
    public static List<GameplayMeld> seatMelds(List<GameplayMeld> melds, int serverSeat) {
        List<GameplayMeld> result = new ArrayList<>();
        if (melds == null) {
            return result;
        }
        for (GameplayMeld meld : melds) {
            if (meld.seat() == serverSeat) {
                result.add(meld);
            }
        }
        return result;
    }

    /**
     * 公开副露先于私有手牌到达时，两层要原子对齐：取公开条数与手牌 {@code meldCount}
     * 的较小值，新副露就不会画在旧手牌上。
     */
    public static int renderableMeldCount(int publicMeldCount, int visibleMeldCount) {
        if (publicMeldCount < 0 || visibleMeldCount < 0) {
            throw new IllegalArgumentException("meld counts must be non-negative");
        }
        return Math.min(publicMeldCount, visibleMeldCount);
    }

    /** 已渲染副露之后的手牌起点，与 {@link TaizhouMahjongHandLayout} 同在手牌区局部坐标系。 */
    public static float handStartOffset(
            List<GameplayMeld> melds,
            int serverSeat,
            int mySeat,
            int chairCount,
            int visibleMeldCount) {
        int localSeat = TaizhouMahjongSeatMapper.toLocalSeat(serverSeat, mySeat, chairCount);
        List<GameplayMeld> seatMelds = seatMelds(melds, serverSeat);
        int rendered = renderableMeldCount(seatMelds.size(), visibleMeldCount);
        if (localSeat == TaizhouMahjongTableLayout.SEAT_BOTTOM) {
            return TaizhouMahjongHandLayout.bottomMeldStartOffset(rendered);
        }
        return TaizhouMahjongMeldLayout.handStartOffset(
                localSeat, seatMelds.subList(0, rendered), mySeat, chairCount);
    }
}
