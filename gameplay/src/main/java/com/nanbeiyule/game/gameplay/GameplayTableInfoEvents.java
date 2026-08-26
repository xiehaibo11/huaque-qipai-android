package com.nanbeiyule.game.gameplay;

import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Applies the Wave 3 table-info events: TING_INFO (SEAT event, 听牌可胡数据),
 * SHENG_PAI_COUNT (生牌数) and LEFT_BANKER (剩余庄/局数). Payload shapes are the
 * published contract verbatim; every malformed payload forces a snapshot resync
 * instead of a partial local guess.
 */
final class GameplayTableInfoEvents {
    private GameplayTableInfoEvents() {}

    /**
     * TING_INFO replaces the ting map of the addressed seat. Events addressed to
     * another seat only advance the cursor, mirroring the original client which
     * only ever reads the bottom (own) seat's {@code getCanHuMahsData}.
     */
    static GameplayTableState applyTingInfo(GameplayTableState state, GameplayEvent event) {
        try {
            Optional<GameplayTingInfo> tingInfo =
                    GameplayRoundProtocol.parseOptionalTingInfo(event.payload());
            if (tingInfo.isPresent() && tingInfo.get().seat() != state.mySeat()) {
                return state.withTableInfo(
                        event.revision(),
                        event.eventOrder(),
                        state.tingInfo(),
                        state.shengPaiCount(),
                        state.leftBankerCount());
            }
            return state.withTableInfo(
                    event.revision(),
                    event.eventOrder(),
                    tingInfo,
                    state.shengPaiCount(),
                    state.leftBankerCount());
        } catch (RuntimeException | JSONException exception) {
            throw new GameplayResyncRequiredException("Ting info payload is invalid");
        }
    }

    static GameplayTableState applyShengPaiCount(GameplayTableState state, GameplayEvent event) {
        try {
            Integer count =
                    GameplayRoundProtocol.optionalNonNegativeCount(
                            event.payload(), "shengPaiCount");
            if (count == null) {
                throw new JSONException("shengPaiCount is required");
            }
            return state.withTableInfo(
                    event.revision(),
                    event.eventOrder(),
                    state.tingInfo(),
                    count,
                    state.leftBankerCount());
        } catch (RuntimeException | JSONException exception) {
            throw new GameplayResyncRequiredException("Sheng pai count payload is invalid");
        }
    }

    static GameplayTableState applyLeftBanker(GameplayTableState state, GameplayEvent event) {
        try {
            Integer count =
                    GameplayRoundProtocol.optionalNonNegativeCount(
                            event.payload(), "leftBankerCount");
            if (count == null) {
                throw new JSONException("leftBankerCount is required");
            }
            return state.withTableInfo(
                    event.revision(),
                    event.eventOrder(),
                    state.tingInfo(),
                    state.shengPaiCount(),
                    count);
        } catch (RuntimeException | JSONException exception) {
            throw new GameplayResyncRequiredException("Left banker payload is invalid");
        }
    }

    /** A new round restarts ting and sheng-pai data; the banker quota spans rounds. */
    static GameplayTableState clearForNewRound(GameplayTableState state) {
        return state.withTableInfo(
                state.revision(), state.eventOrder(), Optional.empty(), null, state.leftBankerCount());
    }
}
