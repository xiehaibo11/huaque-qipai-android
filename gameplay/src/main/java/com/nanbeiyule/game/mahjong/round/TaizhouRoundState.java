package com.nanbeiyule.game.mahjong.round;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Whole-table round state of the original Taizhou {@code GameData} state bus,
 * recovered from
 * {@code src/game/Mahjong/BasicMahjong/Modules/GameLayer/GameData.luac} and
 * {@code src/game/Mahjong/TaiZhou/BasicTaiZhouMahjong/Modules/GameLayer/GameData.luac}.
 *
 * <p>Component mapping:
 *
 * <ul>
 *   <li>{@code seats} — 每座位状态，见 {@link TaizhouRoundSeatState}
 *   <li>{@code jokerTiles} — 财神 {@code _jokerMahData} (Basic GameData.luac:98,:400-408)
 *   <li>{@code insteadTiles} — 替牌 {@code _insteadMahData} (:100,:419-427)
 *   <li>{@code openTiles} — 开牌区 {@code _openMahData} (:96,:360-376)
 *   <li>{@code sharedOutTiles} — 非四方共享出牌区 {@code _outCardDataNoSquare} (:101,:802-808)
 *   <li>{@code shengPaiJieDuan} — 生牌阶段 {@code _isShengPaiJieDuan}
 *       (Taizhou GameData.luac:8,:30-36)。原版只有布尔阶段标记，没有"生牌数"计数值。
 *   <li>{@code leftBankerCount} — 剩余庄数 {@code _nLeftBanker} (:10,:79-85)
 *   <li>{@code remainingWallCount} — 剩余牌堆 {@code _mahSurplusCounts}
 *       (Basic GameData.luac:86,:336-347)，原版拒绝负数 (:337-338)
 *   <li>{@code gameStep} — {@code _curGameStep} (:57,:207-217)，枚举见 :196-216
 * </ul>
 *
 * 推断: {@code activeSeat} 表示当前操作座位；Basic GameData 只有
 * {@code _lastPlaySeat} (:55,:199-205) 与 {@code _curPower} (:53,:1166-1172)，
 * 活动座位由回合消息驱动，本字段按现有 gameplay 包同名语义建模。
 *
 * 推断: 座位号 1..chairCount 沿用 {@code TaizhouMahjongVisibleRound} 与
 * {@code GameplaySeat} 的既有约定（Lua 侧为 0..maxPlayer-1）；
 * chairCount 只允许 2 或 4，与 {@code TaizhouMahjongVisibleRound:100} 对齐。
 */
public record TaizhouRoundState(
        int chairCount,
        int mySeat,
        List<TaizhouRoundSeatState> seats,
        List<Integer> jokerTiles,
        List<Integer> insteadTiles,
        List<Integer> openTiles,
        List<Integer> sharedOutTiles,
        boolean shengPaiJieDuan,
        int leftBankerCount,
        Integer activeSeat,
        int remainingWallCount,
        MahjongGameStep gameStep) {
    public TaizhouRoundState {
        if (chairCount != 2 && chairCount != 4) {
            throw new IllegalArgumentException("chairCount must be 2 or 4");
        }
        if (mySeat <= 0 || mySeat > chairCount) {
            throw new IllegalArgumentException("mySeat is outside chairCount");
        }
        seats = normalizeSeats(chairCount, seats);
        jokerTiles = TaizhouRoundSeatState.copyTiles(jokerTiles, "jokerTiles");
        insteadTiles = TaizhouRoundSeatState.copyTiles(insteadTiles, "insteadTiles");
        openTiles = TaizhouRoundSeatState.copyTiles(openTiles, "openTiles");
        sharedOutTiles = TaizhouRoundSeatState.copyTiles(sharedOutTiles, "sharedOutTiles");
        if (leftBankerCount < 0) {
            throw new IllegalArgumentException("leftBankerCount must be non-negative");
        }
        if (activeSeat != null && (activeSeat <= 0 || activeSeat > chairCount)) {
            throw new IllegalArgumentException("activeSeat is outside chairCount");
        }
        if (remainingWallCount < 0) {
            throw new IllegalArgumentException("remainingWallCount must be non-negative");
        }
        Objects.requireNonNull(gameStep, "gameStep");
    }

    /** Returns the seat state for a 1-based seat number. */
    public TaizhouRoundSeatState seatAt(int seatNumber) {
        if (seatNumber <= 0 || seatNumber > chairCount) {
            throw new IllegalArgumentException("seat is outside chairCount");
        }
        return seats.get(seatNumber - 1);
    }

    public TaizhouRoundState withSeats(List<TaizhouRoundSeatState> nextSeats) {
        return new TaizhouRoundState(
                chairCount, mySeat, nextSeats, jokerTiles, insteadTiles, openTiles,
                sharedOutTiles, shengPaiJieDuan, leftBankerCount, activeSeat,
                remainingWallCount, gameStep);
    }

    public TaizhouRoundState withJokerTiles(List<Integer> nextJokerTiles) {
        return new TaizhouRoundState(
                chairCount, mySeat, seats, nextJokerTiles, insteadTiles, openTiles,
                sharedOutTiles, shengPaiJieDuan, leftBankerCount, activeSeat,
                remainingWallCount, gameStep);
    }

    public TaizhouRoundState withInsteadTiles(List<Integer> nextInsteadTiles) {
        return new TaizhouRoundState(
                chairCount, mySeat, seats, jokerTiles, nextInsteadTiles, openTiles,
                sharedOutTiles, shengPaiJieDuan, leftBankerCount, activeSeat,
                remainingWallCount, gameStep);
    }

    public TaizhouRoundState withOpenTiles(List<Integer> nextOpenTiles) {
        return new TaizhouRoundState(
                chairCount, mySeat, seats, jokerTiles, insteadTiles, nextOpenTiles,
                sharedOutTiles, shengPaiJieDuan, leftBankerCount, activeSeat,
                remainingWallCount, gameStep);
    }

    public TaizhouRoundState withSharedOutTiles(List<Integer> nextSharedOutTiles) {
        return new TaizhouRoundState(
                chairCount, mySeat, seats, jokerTiles, insteadTiles, openTiles,
                nextSharedOutTiles, shengPaiJieDuan, leftBankerCount, activeSeat,
                remainingWallCount, gameStep);
    }

    public TaizhouRoundState withShengPaiJieDuan(boolean nextShengPaiJieDuan) {
        return new TaizhouRoundState(
                chairCount, mySeat, seats, jokerTiles, insteadTiles, openTiles,
                sharedOutTiles, nextShengPaiJieDuan, leftBankerCount, activeSeat,
                remainingWallCount, gameStep);
    }

    public TaizhouRoundState withLeftBankerCount(int nextLeftBankerCount) {
        return new TaizhouRoundState(
                chairCount, mySeat, seats, jokerTiles, insteadTiles, openTiles,
                sharedOutTiles, shengPaiJieDuan, nextLeftBankerCount, activeSeat,
                remainingWallCount, gameStep);
    }

    public TaizhouRoundState withActiveSeat(Integer nextActiveSeat) {
        return new TaizhouRoundState(
                chairCount, mySeat, seats, jokerTiles, insteadTiles, openTiles,
                sharedOutTiles, shengPaiJieDuan, leftBankerCount, nextActiveSeat,
                remainingWallCount, gameStep);
    }

    public TaizhouRoundState withRemainingWallCount(int nextRemainingWallCount) {
        return new TaizhouRoundState(
                chairCount, mySeat, seats, jokerTiles, insteadTiles, openTiles,
                sharedOutTiles, shengPaiJieDuan, leftBankerCount, activeSeat,
                nextRemainingWallCount, gameStep);
    }

    public TaizhouRoundState withGameStep(MahjongGameStep nextGameStep) {
        return new TaizhouRoundState(
                chairCount, mySeat, seats, jokerTiles, insteadTiles, openTiles,
                sharedOutTiles, shengPaiJieDuan, leftBankerCount, activeSeat,
                remainingWallCount, nextGameStep);
    }

    private static List<TaizhouRoundSeatState> normalizeSeats(
            int chairCount, List<TaizhouRoundSeatState> source) {
        Objects.requireNonNull(source, "seats");
        if (source.size() != chairCount) {
            throw new IllegalArgumentException("one round seat is required per chair");
        }
        TaizhouRoundSeatState[] ordered = new TaizhouRoundSeatState[chairCount];
        Set<Integer> seen = new HashSet<>();
        for (TaizhouRoundSeatState seat : source) {
            Objects.requireNonNull(seat, "seat");
            int seatNumber = seat.seatNumber();
            if (seatNumber > chairCount || !seen.add(seatNumber)) {
                throw new IllegalArgumentException("duplicate or invalid round seat");
            }
            ordered[seatNumber - 1] = seat;
        }
        List<TaizhouRoundSeatState> result = new ArrayList<>(List.of(ordered));
        return List.copyOf(result);
    }
}
