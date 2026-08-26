package com.nanbeiyule.game.mahjong;

import java.util.List;

/** 服务端真实累计的台州大结算数据；字段顺序对齐 msgTotalResult(1038) 的 30109 扩展。 */
public record TaizhouTotalResultState(int playCount, boolean show, List<SeatTotal> seats) {
    public TaizhouTotalResultState {
        if (playCount < 0) {
            throw new IllegalArgumentException("playCount must be non-negative");
        }
        seats = List.copyOf(seats);
    }

    public record SeatTotal(
            int seatNumber,
            List<Long> roundWinLost,
            int maxHuCount,
            int maxFanNum,
            int maxFanCount,
            List<String> maxFanNames,
            int winByOwn,
            int winScoreNum,
            int jiePaoNum,
            int discardNum,
            long maxScore,
            int laZiNum,
            int chengBaoNum) {
        public SeatTotal {
            if (seatNumber <= 0
                    || maxHuCount < 0
                    || maxFanNum < 0
                    || maxFanCount < 0
                    || winByOwn < 0
                    || winScoreNum < 0
                    || jiePaoNum < 0
                    || discardNum < 0
                    || laZiNum < 0
                    || chengBaoNum < 0) {
                throw new IllegalArgumentException("invalid total-result seat values");
            }
            roundWinLost = List.copyOf(roundWinLost);
            maxFanNames = List.copyOf(maxFanNames);
        }
    }
}
