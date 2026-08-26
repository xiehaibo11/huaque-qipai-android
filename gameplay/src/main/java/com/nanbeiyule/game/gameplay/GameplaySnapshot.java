package com.nanbeiyule.game.gameplay;

import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayPermission;
import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import com.nanbeiyule.game.mahjong.TaizhouSettleState;
import com.nanbeiyule.game.mahjong.TaizhouDiceState;
import com.nanbeiyule.game.mahjong.TaizhouMultipleState;
import com.nanbeiyule.game.mahjong.TaizhouTotalResultState;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record GameplaySnapshot(
        String sessionId,
        String roomNumber,
        long gameId,
        int roomMode,
        String roomVenue,
        GameplayPhase phase,
        int roundNumber,
        long revision,
        int chairCount,
        int maxPlayCount,
        String gameRuleDisplay,
        boolean autoReady,
        int mySeat,
        List<GameplaySeat> seats,
        Optional<TaizhouMahjongVisibleRound> visibleRound,
        Optional<TaizhouMahjongPlayPermission> playPermission,
        Optional<TaizhouSettleState> settlement,
        Optional<TaizhouMultipleState> multipleChoice,
        Integer activeSeat,
        Integer clockRemainingSeconds,
        int remainingWallCount,
        String updatedAt,
        Optional<GameplayActionOffer> actionOffer,
        List<GameplayMeld> melds,
        List<GameplaySeatFlowers> flowers,
        Optional<GameplayTingInfo> tingInfo,
        Integer shengPaiCount,
        Integer leftBankerCount,
        Optional<TaizhouDiceState> diceRoll,
        Optional<TaizhouTotalResultState> totalResult,
        Optional<WuLongRound> wuLongRound,
        Map<Integer, Boolean> chengBaoFlagsBySeat) {
    public GameplaySnapshot(
            String sessionId,
            String roomNumber,
            long gameId,
            GameplayPhase phase,
            int roundNumber,
            long revision,
            int chairCount,
            int maxPlayCount,
            String gameRuleDisplay,
            boolean autoReady,
            int mySeat,
            List<GameplaySeat> seats,
            Optional<TaizhouMahjongVisibleRound> visibleRound,
            Optional<TaizhouMahjongPlayPermission> playPermission,
            Optional<TaizhouSettleState> settlement,
            Optional<TaizhouMultipleState> multipleChoice,
            Integer activeSeat,
            Integer clockRemainingSeconds,
            int remainingWallCount,
            String updatedAt,
            Optional<GameplayActionOffer> actionOffer,
            List<GameplayMeld> melds,
            List<GameplaySeatFlowers> flowers,
            Optional<GameplayTingInfo> tingInfo,
            Integer shengPaiCount,
            Integer leftBankerCount,
            Optional<TaizhouDiceState> diceRoll,
            Optional<TaizhouTotalResultState> totalResult,
            Optional<WuLongRound> wuLongRound,
            Map<Integer, Boolean> chengBaoFlagsBySeat) {
        this(
                sessionId,
                roomNumber,
                gameId,
                0,
                "",
                phase,
                roundNumber,
                revision,
                chairCount,
                maxPlayCount,
                gameRuleDisplay,
                autoReady,
                mySeat,
                seats,
                visibleRound,
                playPermission,
                settlement,
                multipleChoice,
                activeSeat,
                clockRemainingSeconds,
                remainingWallCount,
                updatedAt,
                actionOffer,
                melds,
                flowers,
                tingInfo,
                shengPaiCount,
                leftBankerCount,
                diceRoll,
                totalResult,
                wuLongRound,
                chengBaoFlagsBySeat);
    }

    public GameplaySnapshot(
            String sessionId,
            String roomNumber,
            long gameId,
            GameplayPhase phase,
            int roundNumber,
            long revision,
            int chairCount,
            int maxPlayCount,
            String gameRuleDisplay,
            boolean autoReady,
            int mySeat,
            List<GameplaySeat> seats,
            Optional<TaizhouMahjongVisibleRound> visibleRound,
            Optional<TaizhouMahjongPlayPermission> playPermission,
            Optional<TaizhouSettleState> settlement,
            Optional<TaizhouMultipleState> multipleChoice,
            Integer activeSeat,
            Integer clockRemainingSeconds,
            int remainingWallCount,
            String updatedAt,
            Optional<GameplayActionOffer> actionOffer,
            List<GameplayMeld> melds,
            List<GameplaySeatFlowers> flowers,
            Optional<GameplayTingInfo> tingInfo,
            Integer shengPaiCount,
            Integer leftBankerCount,
            Optional<TaizhouDiceState> diceRoll,
            Optional<TaizhouTotalResultState> totalResult,
            Optional<WuLongRound> wuLongRound) {
        this(
                sessionId,
                roomNumber,
                gameId,
                phase,
                roundNumber,
                revision,
                chairCount,
                maxPlayCount,
                gameRuleDisplay,
                autoReady,
                mySeat,
                seats,
                visibleRound,
                playPermission,
                settlement,
                multipleChoice,
                activeSeat,
                clockRemainingSeconds,
                remainingWallCount,
                updatedAt,
                actionOffer,
                melds,
                flowers,
                tingInfo,
                shengPaiCount,
                leftBankerCount,
                diceRoll,
                totalResult,
                wuLongRound,
                Map.of());
    }

    public GameplaySnapshot(
            String sessionId,
            String roomNumber,
            long gameId,
            GameplayPhase phase,
            int roundNumber,
            long revision,
            int chairCount,
            int maxPlayCount,
            String gameRuleDisplay,
            boolean autoReady,
            int mySeat,
            List<GameplaySeat> seats,
            Optional<TaizhouMahjongVisibleRound> visibleRound,
            Optional<TaizhouMahjongPlayPermission> playPermission,
            Optional<TaizhouSettleState> settlement,
            Optional<TaizhouMultipleState> multipleChoice,
            Integer activeSeat,
            Integer clockRemainingSeconds,
            int remainingWallCount,
            String updatedAt,
            Optional<GameplayActionOffer> actionOffer,
            List<GameplayMeld> melds,
            List<GameplaySeatFlowers> flowers,
            Optional<GameplayTingInfo> tingInfo,
            Integer shengPaiCount,
            Integer leftBankerCount,
            Optional<TaizhouDiceState> diceRoll) {
        this(
                sessionId,
                roomNumber,
                gameId,
                phase,
                roundNumber,
                revision,
                chairCount,
                maxPlayCount,
                gameRuleDisplay,
                autoReady,
                mySeat,
                seats,
                visibleRound,
                playPermission,
                settlement,
                multipleChoice,
                activeSeat,
                clockRemainingSeconds,
                remainingWallCount,
                updatedAt,
                actionOffer,
                melds,
                flowers,
                tingInfo,
                shengPaiCount,
                leftBankerCount,
                diceRoll,
                Optional.empty(),
                Optional.empty());
    }

    public GameplaySnapshot(
            String sessionId,
            String roomNumber,
            long gameId,
            GameplayPhase phase,
            int roundNumber,
            long revision,
            int chairCount,
            int maxPlayCount,
            String gameRuleDisplay,
            boolean autoReady,
            int mySeat,
            List<GameplaySeat> seats,
            Optional<TaizhouMahjongVisibleRound> visibleRound,
            Optional<TaizhouMahjongPlayPermission> playPermission,
            Optional<TaizhouSettleState> settlement,
            Optional<TaizhouMultipleState> multipleChoice,
            Integer activeSeat,
            int remainingWallCount,
            String updatedAt,
            Optional<GameplayActionOffer> actionOffer,
            List<GameplayMeld> melds,
            List<GameplaySeatFlowers> flowers,
            Optional<GameplayTingInfo> tingInfo,
            Integer shengPaiCount,
            Integer leftBankerCount) {
        this(
                sessionId,
                roomNumber,
                gameId,
                phase,
                roundNumber,
                revision,
                chairCount,
                maxPlayCount,
                gameRuleDisplay,
                autoReady,
                mySeat,
                seats,
                visibleRound,
                playPermission,
                settlement,
                multipleChoice,
                activeSeat,
                null,
                remainingWallCount,
                updatedAt,
                actionOffer,
                melds,
                flowers,
                tingInfo,
                shengPaiCount,
                leftBankerCount,
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    /** Wave 2 形状的全参构造：听牌/生牌/剩庄字段一律按缺席处理。 */
    public GameplaySnapshot(
            String sessionId,
            String roomNumber,
            long gameId,
            GameplayPhase phase,
            int roundNumber,
            long revision,
            int chairCount,
            int maxPlayCount,
            String gameRuleDisplay,
            boolean autoReady,
            int mySeat,
            List<GameplaySeat> seats,
            Optional<TaizhouMahjongVisibleRound> visibleRound,
            Optional<TaizhouMahjongPlayPermission> playPermission,
            Optional<TaizhouSettleState> settlement,
            Optional<TaizhouMultipleState> multipleChoice,
            Integer activeSeat,
            int remainingWallCount,
            String updatedAt,
            Optional<GameplayActionOffer> actionOffer,
            List<GameplayMeld> melds,
            List<GameplaySeatFlowers> flowers) {
        this(
                sessionId,
                roomNumber,
                gameId,
                phase,
                roundNumber,
                revision,
                chairCount,
                maxPlayCount,
                gameRuleDisplay,
                autoReady,
                mySeat,
                seats,
                visibleRound,
                playPermission,
                settlement,
                multipleChoice,
                activeSeat,
                null,
                remainingWallCount,
                updatedAt,
                actionOffer,
                melds,
                flowers,
                Optional.empty(),
                null,
                null,
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    public GameplaySnapshot(
            String sessionId,
            String roomNumber,
            long gameId,
            GameplayPhase phase,
            int roundNumber,
            long revision,
            int chairCount,
            int maxPlayCount,
            String gameRuleDisplay,
            boolean autoReady,
            int mySeat,
            List<GameplaySeat> seats,
            Optional<TaizhouMahjongVisibleRound> visibleRound,
            Optional<TaizhouMahjongPlayPermission> playPermission,
            Optional<TaizhouSettleState> settlement,
            String updatedAt) {
        this(
                sessionId,
                roomNumber,
                gameId,
                phase,
                roundNumber,
                revision,
                chairCount,
                maxPlayCount,
                gameRuleDisplay,
                autoReady,
                mySeat,
                seats,
                visibleRound,
                playPermission,
                settlement,
                Optional.empty(),
                null,
                null,
                -1,
                updatedAt,
                Optional.empty(),
                List.of(),
                List.of(),
                Optional.empty(),
                null,
                null,
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    public GameplaySnapshot(
            String sessionId,
            String roomNumber,
            long gameId,
            GameplayPhase phase,
            int roundNumber,
            long revision,
            int chairCount,
            int maxPlayCount,
            String gameRuleDisplay,
            boolean autoReady,
            int mySeat,
            List<GameplaySeat> seats,
            String updatedAt) {
        this(
                sessionId,
                roomNumber,
                gameId,
                phase,
                roundNumber,
                revision,
                chairCount,
                maxPlayCount,
                gameRuleDisplay,
                autoReady,
                mySeat,
                seats,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                null,
                null,
                -1,
                updatedAt,
                Optional.empty(),
                List.of(),
                List.of(),
                Optional.empty(),
                null,
                null,
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    public GameplaySnapshot {
        sessionId = requireText(sessionId, "sessionId");
        roomNumber = requireText(roomNumber, "roomNumber");
        roomVenue = roomVenue == null ? "" : roomVenue;
        phase = Objects.requireNonNull(phase, "phase");
        updatedAt = requireText(updatedAt, "updatedAt");
        Objects.requireNonNull(gameRuleDisplay, "gameRuleDisplay");
        if (gameId <= 0
                || roundNumber < 0
                || revision < 0
                || (chairCount != 2 && chairCount != 4)
                || maxPlayCount <= 0
                || mySeat <= 0
                || mySeat > chairCount) {
            throw new IllegalArgumentException("invalid gameplay snapshot numbers");
        }
        seats = List.copyOf(Objects.requireNonNull(seats, "seats"));
        visibleRound = visibleRound == null ? Optional.empty() : visibleRound;
        playPermission = playPermission == null ? Optional.empty() : playPermission;
        settlement = settlement == null ? Optional.empty() : settlement;
        multipleChoice = multipleChoice == null ? Optional.empty() : multipleChoice;
        actionOffer = actionOffer == null ? Optional.empty() : actionOffer;
        melds = melds == null ? List.of() : List.copyOf(melds);
        flowers = flowers == null ? List.of() : List.copyOf(flowers);
        tingInfo = tingInfo == null ? Optional.empty() : tingInfo;
        diceRoll = diceRoll == null ? Optional.empty() : diceRoll;
        totalResult = totalResult == null ? Optional.empty() : totalResult;
        wuLongRound = wuLongRound == null ? Optional.empty() : wuLongRound;
        chengBaoFlagsBySeat =
                chengBaoFlagsBySeat == null ? Map.of() : Map.copyOf(chengBaoFlagsBySeat);
        if (shengPaiCount != null && shengPaiCount < 0) {
            throw new IllegalArgumentException("shengPaiCount must be non-negative");
        }
        if (leftBankerCount != null && leftBankerCount < 0) {
            throw new IllegalArgumentException("leftBankerCount must be non-negative");
        }
        if (clockRemainingSeconds != null && clockRemainingSeconds < 0) {
            throw new IllegalArgumentException("clockRemainingSeconds must be non-negative");
        }
        if (activeSeat != null && (activeSeat <= 0 || activeSeat > chairCount)) {
            throw new IllegalArgumentException("activeSeat is outside chairCount");
        }
        if (remainingWallCount < -1) {
            throw new IllegalArgumentException("remainingWallCount must be -1 or non-negative");
        }
        if (diceRoll.isPresent() && diceRoll.get().seatNumber() > chairCount) {
            throw new IllegalArgumentException("dice seat is outside chairCount");
        }
        for (int seat : chengBaoFlagsBySeat.keySet()) {
            if (seat <= 0 || seat > chairCount) {
                throw new IllegalArgumentException("chengBao flag seat is outside chairCount");
            }
        }
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
