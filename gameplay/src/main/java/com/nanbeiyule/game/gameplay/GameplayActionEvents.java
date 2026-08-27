package com.nanbeiyule.game.gameplay;

import com.nanbeiyule.game.mahjong.round.MahjongCombType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Applies the Wave 2-B action events (ACTION_OFFERED/ACTION_EXPIRED/
 * MELD_APPLIED/FLOWER_REPLACED/WIN_DECLARED) to the table projection. Payload
 * shapes are the published contract verbatim; every malformed payload forces a
 * snapshot resync instead of a partial local guess.
 */
final class GameplayActionEvents {
    private GameplayActionEvents() {}

    static GameplayTableState applyActionOffered(
            GameplayTableState state, GameplayEvent event) {
        try {
            Optional<GameplayActionOffer> offer =
                    GameplayRoundProtocol.parseOptionalActionOffer(event.payload());
            Integer nextActiveSeat = GameplayRoundProtocol.optionalActiveSeat(event.payload());
            if (nextActiveSeat == null && offer.isPresent()) {
                nextActiveSeat = offer.get().seat();
            }
            Integer nextClockRemainingSeconds =
                    GameplayRoundProtocol.optionalNonNegativeCount(
                            event.payload(), "clockRemainingSeconds");
            GameplayTableState next =
                    state.withActionLayer(
                            event.revision(),
                            event.eventOrder(),
                            offer,
                            state.melds(),
                            state.flowers(),
                            state.actionTip());
            return next.withCursorMetadata(
                    event.revision(),
                    event.eventOrder(),
                    nextActiveSeat,
                    nextClockRemainingSeconds,
                    next.remainingWallCount());
        } catch (RuntimeException | JSONException exception) {
            throw new GameplayResyncRequiredException("Action offer payload is invalid");
        }
    }

    static GameplayTableState applyActionExpired(
            GameplayTableState state, GameplayEvent event) {
        try {
            JSONObject payload = event.payload();
            int offerId = payload.getInt("offerId");
            Optional<GameplayActionOffer> current = state.actionOffer();
            Optional<GameplayActionOffer> next =
                    current.isPresent() && current.get().offerId() == offerId
                            ? Optional.empty()
                            : current;
            return state.withActionLayer(
                    event.revision(),
                    event.eventOrder(),
                    next,
                    state.melds(),
                    state.flowers(),
                    state.actionTip());
        } catch (RuntimeException | JSONException exception) {
            throw new GameplayResyncRequiredException("Action expired payload is invalid");
        }
    }

    static GameplayTableState applyMeldApplied(GameplayTableState state, GameplayEvent event) {
        try {
            GameplayMeld meld = GameplayRoundProtocol.parseMeld(event.payload());
            List<GameplayMeld> melds = new ArrayList<>(state.melds());
            melds.add(meld);
            Optional<GameplayActionOffer> offer = state.actionOffer();
            if (offer.isPresent() && offer.get().seat() == meld.seat()) {
                offer = Optional.empty();
            }
            return state.withActionLayer(
                    event.revision(),
                    event.eventOrder(),
                    offer,
                    melds,
                    state.flowers(),
                    Optional.of(
                            new GameplayActionTip(
                                    tipKind(meld.combType()),
                                    meld.seat(),
                                    event.revision(),
                                    event.eventOrder())));
        } catch (RuntimeException | JSONException exception) {
            throw new GameplayResyncRequiredException("Meld applied payload is invalid");
        }
    }

    static GameplayTableState applyFlowerReplaced(
            GameplayTableState state, GameplayEvent event) {
        try {
            JSONObject payload = event.payload();
            int seat = payload.getInt("seat");
            int flower = payload.getInt("flower");
            payload.getInt("replacement");
            List<GameplaySeatFlowers> flowers = new ArrayList<>(state.flowers().size() + 1);
            boolean found = false;
            for (GameplaySeatFlowers seatFlowers : state.flowers()) {
                if (seatFlowers.seatNumber() == seat) {
                    flowers.add(seatFlowers.withFlower(flower));
                    found = true;
                } else {
                    flowers.add(seatFlowers);
                }
            }
            if (!found) {
                flowers.add(new GameplaySeatFlowers(seat, List.of(flower)));
            }
            return state.withActionLayer(
                    event.revision(),
                    event.eventOrder(),
                    state.actionOffer(),
                    state.melds(),
                    flowers,
                    Optional.of(
                            new GameplayActionTip(
                                    GameplayActionTip.Kind.FLOWER,
                                    seat,
                                    event.revision(),
                                    event.eventOrder())));
        } catch (RuntimeException | JSONException exception) {
            throw new GameplayResyncRequiredException("Flower replaced payload is invalid");
        }
    }

    /**
     * WIN_DECLARED keeps the existing play-permission clearing and additionally
     * closes any pending offer and raises the hu tip; an optional
     * {@code endPlayerState} payload member is ignored by the projection.
     */
    static GameplayTableState applyWinDeclared(GameplayTableState state, GameplayEvent event) {
        try {
            int winnerSeat = event.payload().getInt("winnerSeat");
            return state.withActionLayer(
                    event.revision(),
                    event.eventOrder(),
                    Optional.empty(),
                    state.melds(),
                    state.flowers(),
                    Optional.of(
                            new GameplayActionTip(
                                    GameplayActionTip.Kind.HU,
                                    winnerSeat,
                                    event.revision(),
                                    event.eventOrder())))
                    .withoutPlayPermission(event.revision(), event.eventOrder());
        } catch (RuntimeException | JSONException exception) {
            throw new GameplayResyncRequiredException("Win declared payload is invalid");
        }
    }

    private static GameplayActionTip.Kind tipKind(MahjongCombType combType) {
        return switch (combType) {
            case CHOW -> GameplayActionTip.Kind.CHOW;
            case PONG -> GameplayActionTip.Kind.PONG;
            case CONCEALED_KONG -> GameplayActionTip.Kind.CONCEALED_KONG;
            default -> GameplayActionTip.Kind.KONG;
        };
    }
}
