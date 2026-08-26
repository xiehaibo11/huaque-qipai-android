package com.nanbeiyule.game.gameplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class GameplayReducer {
    private GameplayReducer() {}

    public static GameplayTableState fromSnapshot(GameplaySnapshot snapshot) {
        return new GameplayTableState(
                snapshot.sessionId(),
                snapshot.roomNumber(),
                snapshot.gameId(),
                snapshot.roomMode(),
                snapshot.roomVenue(),
                snapshot.phase(),
                snapshot.roundNumber(),
                snapshot.revision(),
                Integer.MAX_VALUE,
                snapshot.chairCount(),
                snapshot.maxPlayCount(),
                snapshot.gameRuleDisplay(),
                snapshot.autoReady(),
                snapshot.mySeat(),
                snapshot.seats(),
                snapshot.visibleRound(),
                snapshot.playPermission(),
                snapshot.settlement(),
                snapshot.multipleChoice(),
                snapshot.activeSeat(),
                snapshot.clockRemainingSeconds(),
                snapshot.remainingWallCount(),
                snapshot.updatedAt(),
                snapshot.actionOffer(),
                snapshot.melds(),
                snapshot.flowers(),
                Optional.empty(),
                snapshot.tingInfo(),
                snapshot.shengPaiCount(),
                snapshot.leftBankerCount(),
                snapshot.diceRoll(),
                snapshot.totalResult(),
                snapshot.chengBaoFlagsBySeat());
    }

    public static GameplayTableState reduce(
            GameplayTableState state,
            GameplayEvent event) {
        requireSameSession(state, event);
        if (isDuplicate(state, event)) {
            return state;
        }
        requireNextCursor(state, event);
        return switch (event.type()) {
            case "SEAT_READY_CHANGED" -> applyReadyChanged(state, event);
            case "BOT_SEATS_FILLED", "WALL_SHUFFLED" -> advanceCursor(state, event);
            case "DICE_ROLLED" -> applyDiceRolled(state, event);
            case "WALL_OPENED" -> applyWallOpened(state, event);
            case "MULTIPLE_CHOICE_STARTED", "MULTIPLE_CHOICE_CHANGED" ->
                    applyMultipleChoiceChanged(state, event);
            case "WIN_DECLARED" -> GameplayActionEvents.applyWinDeclared(state, event);
            case "ACTION_OFFERED" -> GameplayActionEvents.applyActionOffered(state, event);
            case "ACTION_EXPIRED" -> GameplayActionEvents.applyActionExpired(state, event);
            case "MELD_APPLIED" -> GameplayActionEvents.applyMeldApplied(state, event);
            case "FLOWER_REPLACED" -> GameplayActionEvents.applyFlowerReplaced(state, event);
            case "TING_INFO" -> GameplayTableInfoEvents.applyTingInfo(state, event);
            case "SHENG_PAI_COUNT" -> GameplayTableInfoEvents.applyShengPaiCount(state, event);
            case "LEFT_BANKER" -> GameplayTableInfoEvents.applyLeftBanker(state, event);
            case "TURN_ADVANCED" -> advanceCursor(state, event);
            /**
             * 原版转发族（msgClientForward XY_ID=1043，CF_ID 1..10；扩展层
             * msgBaseClientForwardEx=22 另有 15+OPERATE_PASS）：表情、GPS、语音等互动
             * 不落桌态，只占事件游标序，payload（seatNumber/cfId/data）由 UI 层在
             * onEvents 里直接消费。
             */
            case "CLIENT_FORWARD" -> advanceCursor(state, event);
            case "ROUND_STATE_CHANGED", "DEALT", "DRAWN", "DISCARDED" ->
                    applyRoundStateChanged(state, event);
            case "SCORES_SETTLED" -> applyScoresSettled(state, event);
            case "ROUND_RESULT_READY" -> applyRoundResultReady(state, event);
            case "TOTAL_RESULT_READY" -> applyTotalResultReady(state, event);
            default ->
                    throw new GameplayResyncRequiredException(
                            "Unsupported gameplay event: " + event.type());
        };
    }

    private static GameplayTableState applyReadyChanged(
            GameplayTableState state,
            GameplayEvent event) {
        try {
            JSONObject payload = event.payload();
            int changedSeat = payload.getInt("seatNumber");
            boolean ready = payload.getBoolean("ready");
            List<GameplaySeat> seats = new ArrayList<>(state.seats().size());
            boolean found = false;
            for (GameplaySeat seat : state.seats()) {
                if (seat.seatNumber() == changedSeat) {
                    seats.add(seat.withReady(ready));
                    found = true;
                } else {
                    seats.add(seat);
                }
            }
            if (!found) {
                throw new GameplayResyncRequiredException(
                        "Ready event targets a missing seat");
            }
            return state.withEvent(event.revision(), event.eventOrder(), seats);
        } catch (JSONException exception) {
            throw new GameplayResyncRequiredException(
                    "Ready event payload is invalid");
        }
    }


    private static GameplayTableState advanceCursor(
            GameplayTableState state, GameplayEvent event) {
        JSONObject payload = event.payload();
        Integer activeSeat = nextActiveSeat(state, payload);
        Integer clockRemainingSeconds = nextClockRemainingSeconds(state, payload);
        int remainingWallCount = nextRemainingWallCount(state, payload);
        return state.withCursorMetadata(
                event.revision(),
                event.eventOrder(),
                activeSeat,
                clockRemainingSeconds,
                remainingWallCount);
    }

    private static GameplayTableState applyMultipleChoiceChanged(
            GameplayTableState state, GameplayEvent event) {
        try {
            JSONObject payload = event.payload();
            return state.withPhaseAndRoundState(
                    event.revision(),
                    event.eventOrder(),
                    GameplayPhase.valueOf(payload.optString("phase", state.phase().name())),
                    payload.optInt("roundNumber", state.roundNumber()),
                    state.visibleRound(),
                    state.playPermission(),
                    state.settlement(),
                    multipleChoiceUpdate(state, payload),
                    nextActiveSeat(state, payload),
                    nextClockRemainingSeconds(state, payload),
                    nextRemainingWallCount(state, payload));
        } catch (RuntimeException | JSONException exception) {
            throw new GameplayResyncRequiredException("Multiple choice payload is invalid");
        }
    }

    private static GameplayTableState applyDiceRolled(
            GameplayTableState state, GameplayEvent event) {
        try {
            JSONObject payload = event.payload();
            GameplayTableState next =
                    state.withPhaseAndRoundState(
                            event.revision(),
                            event.eventOrder(),
                            GameplayPhase.valueOf(payload.optString("phase", state.phase().name())),
                            payload.optInt("roundNumber", state.roundNumber()),
                            state.visibleRound(),
                            state.playPermission(),
                            state.settlement(),
                            state.multipleChoice(),
                            nextActiveSeat(state, payload),
                            nextClockRemainingSeconds(state, payload),
                            nextRemainingWallCount(state, payload));
            return next.withDiceRoll(
                    event.revision(),
                    event.eventOrder(),
                    GameplayRoundProtocol.parseOptionalDiceRoll(payload.getJSONObject("diceRoll")));
        } catch (RuntimeException | JSONException exception) {
            throw new GameplayResyncRequiredException("Dice payload is invalid");
        }
    }

    private static GameplayTableState applyWallOpened(
            GameplayTableState state, GameplayEvent event) {
        try {
            JSONObject payload = event.payload();
            com.nanbeiyule.game.mahjong.TaizhouWallState wall =
                    GameplayRoundProtocol.parseOptionalWallState(payload).orElseThrow();
            int remainingWallCount = nextRemainingWallCount(state, payload);
            if (remainingWallCount != wall.remainingCount()) {
                throw new GameplayResyncRequiredException(
                        "Wall event remaining count does not match msgWallMah");
            }
            return state.withCursorMetadata(
                    event.revision(),
                    event.eventOrder(),
                    nextActiveSeat(state, payload),
                    nextClockRemainingSeconds(state, payload),
                    remainingWallCount);
        } catch (RuntimeException | JSONException exception) {
            if (exception instanceof GameplayResyncRequiredException resync) {
                throw resync;
            }
            throw new GameplayResyncRequiredException("Wall-open payload is invalid");
        }
    }

    private static GameplayTableState applyScoresSettled(
            GameplayTableState state, GameplayEvent event) {
        try {
            JSONArray scores = event.payload().optJSONArray("scores");
            if (scores == null) {
                return advanceCursor(state, event);
            }
            List<GameplaySeat> seats = new ArrayList<>(state.seats());
            for (int index = 0; index < scores.length(); index++) {
                JSONObject score = scores.getJSONObject(index);
                int seatNumber = score.getInt("seatNumber");
                long nextScore = score.getLong("score");
                boolean found = false;
                for (int seatIndex = 0; seatIndex < seats.size(); seatIndex++) {
                    GameplaySeat seat = seats.get(seatIndex);
                    if (seat.seatNumber() == seatNumber) {
                        seats.set(seatIndex, seat.withScore(nextScore));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new GameplayResyncRequiredException("Score event targets a missing seat");
                }
            }
            return state.withEvent(event.revision(), event.eventOrder(), seats);
        } catch (RuntimeException | JSONException exception) {
            throw new GameplayResyncRequiredException("Score event payload is invalid");
        }
    }

    private static GameplayTableState applyRoundStateChanged(
            GameplayTableState state, GameplayEvent event) {
        try {
            JSONObject payload = event.payload();
            Optional<com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound> visibleRound =
                    GameplayRoundProtocol.parseOptionalVisibleRound(
                            payload.optJSONObject("visibleRound"));
            if (visibleRound.isEmpty()) {
                visibleRound =
                        GameplayRoundProtocol.parseOptionalPublicRound(
                                payload.optJSONObject("publicRound"),
                                state.chairCount(),
                                state.mySeat(),
                                state.visibleRound());
            }
            Optional<com.nanbeiyule.game.mahjong.TaizhouMahjongPlayPermission> playPermission =
                    permissionUpdate(state, payload, event.type());
            int nextRoundNumber = payload.optInt("roundNumber", state.roundNumber());
            GameplayTableState next =
                    state.withPhaseAndRoundState(
                            event.revision(),
                            event.eventOrder(),
                            GameplayPhase.valueOf(payload.getString("phase")),
                            nextRoundNumber,
                            visibleRound.isPresent() ? visibleRound : state.visibleRound(),
                            playPermission,
                            Optional.empty(),
                            multipleChoiceUpdate(state, payload),
                            nextActiveSeat(state, payload),
                            nextClockRemainingSeconds(state, payload),
                            nextRemainingWallCount(state, payload));
            if ("DEALT".equals(event.type()) && nextRoundNumber != state.roundNumber()) {
                // A new round starts with empty meld/flower areas and no pending offer.
                next =
                        next.withActionLayer(
                                next.revision(),
                                next.eventOrder(),
                                Optional.empty(),
                                List.of(),
                                List.of(),
                                Optional.empty());
                // Ting and sheng-pai data are round-scoped (TableInfoLayer:clearTable).
                next = GameplayTableInfoEvents.clearForNewRound(next);
            }
            if (payload.has("diceRoll")) {
                Optional<com.nanbeiyule.game.mahjong.TaizhouDiceState> diceRoll =
                        payload.isNull("diceRoll")
                                ? Optional.empty()
                                : GameplayRoundProtocol.parseOptionalDiceRoll(
                                        payload.getJSONObject("diceRoll"));
                next = next.withDiceRoll(event.revision(), event.eventOrder(), diceRoll);
            }
            return next;
        } catch (RuntimeException | JSONException exception) {
            throw new GameplayResyncRequiredException("Round state payload is invalid");
        }
    }

    private static GameplayTableState applyRoundResultReady(
            GameplayTableState state, GameplayEvent event) {
        try {
            JSONObject payload = event.payload();
            JSONObject settlement = payload.optJSONObject("settlement");
            if (settlement == null) {
                settlement = payload.optJSONObject("roundResult");
                if (settlement != null && payload.has("seats")) {
                    settlement.put("seats", payload.getJSONArray("seats"));
                }
            }
            return state.withPhaseAndRoundState(
                    event.revision(),
                    event.eventOrder(),
                    GameplayPhase.valueOf(payload.optString("phase", "ROUND_RESULT")),
                    payload.optInt("roundNumber", state.roundNumber()),
                    state.visibleRound(),
                    Optional.empty(),
                    GameplayRoundProtocol.parseOptionalSettlement(settlement),
                    Optional.empty(),
                    null,
                    null,
                    state.remainingWallCount());
        } catch (RuntimeException | JSONException exception) {
            throw new GameplayResyncRequiredException("Round result payload is invalid");
        }
    }

    private static GameplayTableState applyTotalResultReady(
            GameplayTableState state, GameplayEvent event) {
        try {
            JSONObject totalResult = event.payload().optJSONObject("totalResult");
            if (totalResult == null) {
                throw new GameplayResyncRequiredException("Total result payload is missing");
            }
            return new GameplayTableState(
                    state.sessionId(),
                    state.roomNumber(),
                    state.gameId(),
                    state.roomMode(),
                    state.roomVenue(),
                    state.phase(),
                    state.roundNumber(),
                    event.revision(),
                    event.eventOrder(),
                    state.chairCount(),
                    state.maxPlayCount(),
                    state.gameRuleDisplay(),
                    state.autoReady(),
                    state.mySeat(),
                    state.seats(),
                    state.visibleRound(),
                    state.playPermission(),
                    state.settlement(),
                    state.multipleChoice(),
                    state.activeSeat(),
                    state.clockRemainingSeconds(),
                    state.remainingWallCount(),
                    state.updatedAt(),
                    state.actionOffer(),
                    state.melds(),
                    state.flowers(),
                    state.actionTip(),
                    state.tingInfo(),
                    state.shengPaiCount(),
                    state.leftBankerCount(),
                    state.diceRoll(),
                    GameplayRoundProtocol.parseOptionalTotalResult(totalResult),
                    state.chengBaoFlagsBySeat());
        } catch (RuntimeException | JSONException exception) {
            throw new GameplayResyncRequiredException("Total result payload is invalid");
        }
    }

    private static boolean isDuplicate(GameplayTableState state, GameplayEvent event) {
        return event.revision() < state.revision()
                || (event.revision() == state.revision()
                        && event.eventOrder() <= state.eventOrder());
    }

    private static void requireNextCursor(GameplayTableState state, GameplayEvent event) {
        boolean nextRevision =
                event.revision() == state.revision() + 1L && event.eventOrder() == 1;
        boolean nextOrder =
                event.revision() == state.revision()
                        && state.eventOrder() != Integer.MAX_VALUE
                        && event.eventOrder() == state.eventOrder() + 1;
        if (!nextRevision && !nextOrder) {
            throw new GameplayResyncRequiredException("Gameplay event cursor has a gap");
        }
    }

    private static void requireSameSession(GameplayTableState state, GameplayEvent event) {
        if (!state.sessionId().equals(event.sessionId())) {
            throw new GameplayResyncRequiredException("Gameplay event belongs to another session");
        }
    }

    private static Optional<com.nanbeiyule.game.mahjong.TaizhouMahjongPlayPermission>
            permissionUpdate(GameplayTableState state, JSONObject payload, String eventType)
                    throws JSONException {
        if (!payload.has("playPermission")) {
            if ("DISCARDED".equals(eventType)) {
                return Optional.empty();
            }
            return state.playPermission();
        }
        if (payload.isNull("playPermission")) {
            return Optional.empty();
        }
        return GameplayRoundProtocol.parseOptionalPlayPermission(
                payload.getJSONObject("playPermission"));
    }


    private static Optional<com.nanbeiyule.game.mahjong.TaizhouMultipleState>
            multipleChoiceUpdate(GameplayTableState state, JSONObject payload) throws JSONException {
        if (!payload.has("multipleChoice")) {
            return state.multipleChoice();
        }
        if (payload.isNull("multipleChoice")) {
            return Optional.empty();
        }
        return GameplayRoundProtocol.parseOptionalMultipleChoice(
                payload.getJSONObject("multipleChoice"));
    }

    private static Integer nextActiveSeat(GameplayTableState state, JSONObject payload) {
        if (!payload.has("activeSeat")) {
            return state.activeSeat();
        }
        if (payload.isNull("activeSeat")) {
            return null;
        }
        return payload.optInt("activeSeat");
    }

    private static int nextRemainingWallCount(
            GameplayTableState state, JSONObject payload) {
        if (!payload.has("remainingWallCount") || payload.isNull("remainingWallCount")) {
            return payload.has("remainingWallCount") ? -1 : state.remainingWallCount();
        }
        return payload.optInt("remainingWallCount");
    }

    private static Integer nextClockRemainingSeconds(
            GameplayTableState state, JSONObject payload) {
        if (!payload.has("clockRemainingSeconds")) {
            return state.clockRemainingSeconds();
        }
        if (payload.isNull("clockRemainingSeconds")) {
            return null;
        }
        int seconds = payload.optInt("clockRemainingSeconds");
        if (seconds < 0) {
            throw new GameplayResyncRequiredException("Clock payload is invalid");
        }
        return seconds;
    }
}
