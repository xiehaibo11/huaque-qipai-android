package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplaySeat;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouTotalResultState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 将服务端大结算投影为 BasicTaiZhouMahjong/BigWinLost 原版展示字段。 */
final class TaizhouTotalResultProjection {
    record Result(String roomLabel, String playCountLabel, String timeLabel, List<Player> players) {
        Result {
            players = List.copyOf(players);
        }
    }

    record Player(
            int seatNumber,
            String displayName,
            long publicPlayerId,
            String avatarKey,
            boolean host,
            long totalScore,
            boolean bigWinner,
            List<String> scoreDescriptions) {
        Player {
            scoreDescriptions = List.copyOf(scoreDescriptions);
        }

        String totalScoreText() {
            return totalScore >= 0 ? "+" + totalScore : Long.toString(totalScore);
        }
    }

    static Result project(GameplayTableState state) {
        if (state == null || state.totalResult().isEmpty()) {
            throw new IllegalArgumentException("visible total result is required");
        }
        TaizhouTotalResultState total = state.totalResult().orElseThrow();
        Map<Integer, TaizhouTotalResultState.SeatTotal> totalsBySeat = new LinkedHashMap<>();
        for (TaizhouTotalResultState.SeatTotal seat : total.seats()) {
            totalsBySeat.put(seat.seatNumber(), seat);
        }
        List<GameplaySeat> seats = state.seats().stream()
                .sorted(Comparator.comparingInt(GameplaySeat::seatNumber))
                .toList();
        long highest = Long.MIN_VALUE;
        int firstHighestSeat = -1;
        for (GameplaySeat seat : seats) {
            long score = totalScore(totalsBySeat.get(seat.seatNumber()));
            if (score > highest) {
                highest = score;
                firstHighestSeat = seat.seatNumber();
            }
        }
        if (highest <= 0) {
            firstHighestSeat = -1;
        }
        List<Player> players = new ArrayList<>(seats.size());
        for (GameplaySeat seat : seats) {
            TaizhouTotalResultState.SeatTotal values = totalsBySeat.get(seat.seatNumber());
            if (values == null) {
                continue;
            }
            players.add(
                    new Player(
                            seat.seatNumber(),
                            seat.displayName(),
                            seat.publicPlayerId(),
                            seat.avatarKey(),
                            seat.host(),
                            totalScore(values),
                            seat.seatNumber() == firstHighestSeat,
                            scoreDescriptions(values)));
        }
        return new Result(
                "房间号: " + state.roomNumber(),
                "局数:  " + total.playCount(),
                totalResultTime(state),
                players);
    }

    private static long totalScore(TaizhouTotalResultState.SeatTotal seat) {
        if (seat == null) {
            return 0L;
        }
        long total = 0L;
        for (long score : seat.roundWinLost()) {
            total += score;
        }
        return total;
    }

    /** BasicTaiZhouMahjong/BigWinLost/Module.luac:getResultInfo 的固定 1..5 顺序。 */
    private static List<String> scoreDescriptions(TaizhouTotalResultState.SeatTotal seat) {
        return List.of(
                "自摸次数 x" + seat.winByOwn(),
                "接炮次数 x" + seat.jiePaoNum(),
                "点炮次数 x" + seat.discardNum(),
                "包牌次数 x" + seat.chengBaoNum(),
                "腊子次数 x" + seat.laZiNum());
    }

    private static String totalResultTime(GameplayTableState state) {
        if (state.settlement().isPresent() && !state.settlement().get().time().isBlank()) {
            return state.settlement().get().time();
        }
        return state.updatedAt().replace('T', ' ').replace("Z", "");
    }

    private TaizhouTotalResultProjection() {}
}
