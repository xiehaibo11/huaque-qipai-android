package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Server-authoritative gold-membership cards, separate from the Shaoxing daily gift. */
record GoldMembershipCardsState(List<Card> cards) {
    enum CardState {
        NOT_ACTIVE,
        NOT_AWARD,
        HAS_AWARD
    }

    record Card(
            String productCode,
            String title,
            int durationDays,
            long dailyCoins,
            CardState state,
            long remainingSeconds) {
        boolean isActive() {
            return state != CardState.NOT_ACTIVE;
        }

        boolean canClaim() {
            return state == CardState.NOT_AWARD;
        }
    }

    GoldMembershipCardsState {
        cards = List.copyOf(cards);
    }

    static GoldMembershipCardsState fromJson(JSONObject body) throws JSONException {
        JSONArray cardBodies = body.getJSONArray("cards");
        List<Card> cards = new ArrayList<>(cardBodies.length());
        for (int index = 0; index < cardBodies.length(); index++) {
            cards.add(cardFromJson(cardBodies.getJSONObject(index)));
        }
        return new GoldMembershipCardsState(cards);
    }

    static Card cardFromJson(JSONObject body) throws JSONException {
        String stateValue = requiredString(body, "state");
        CardState state;
        try {
            state = CardState.valueOf(stateValue);
        } catch (IllegalArgumentException exception) {
            throw new JSONException("Unknown gold-membership card state: " + stateValue);
        }
        return new Card(
                requiredString(body, "productCode"),
                requiredString(body, "title"),
                body.getInt("durationDays"),
                body.getLong("dailyCoins"),
                state,
                body.getLong("remainingSeconds"));
    }

    GoldMembershipCardsState withUpdatedCard(Card updatedCard) {
        List<Card> updated = new ArrayList<>(cards.size());
        boolean replaced = false;
        for (Card card : cards) {
            if (card.productCode().equals(updatedCard.productCode())) {
                updated.add(updatedCard);
                replaced = true;
            } else {
                updated.add(card);
            }
        }
        if (!replaced) {
            updated.add(updatedCard);
        }
        return new GoldMembershipCardsState(updated);
    }

    private static String requiredString(JSONObject body, String field) throws JSONException {
        String value = body.getString(field).trim();
        if (value.isEmpty()) {
            throw new JSONException(field + " must not be blank");
        }
        return value;
    }
}
