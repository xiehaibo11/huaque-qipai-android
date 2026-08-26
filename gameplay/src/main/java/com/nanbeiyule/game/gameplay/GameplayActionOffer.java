package com.nanbeiyule.game.gameplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * One server-issued action offer (the {@code ACTION_OFFERED} SEAT event and the
 * matching optional {@code actionOffer} snapshot field). The offer is private
 * to the offered seat: it never enters the public round projection.
 *
 * <p>{@code powerMask} is the original {@code GameDefine.POWER} bitmap that
 * drives the action bar; {@code contextTile} is the incoming tile the offer
 * reacts to ({@code 0} when the offer does not reference one, e.g. a self-drawn
 * concealed kong window). {@code chowCandidates} holds the server-computed
 * three-tile combs (including {@code contextTile} in its run position), the
 * same shape the original {@code MahAlgorithm.findChow} returns; the {@code
 * candidateIndex} of the CHOW command indexes this list verbatim.
 */
public record GameplayActionOffer(
        int seat,
        int powerMask,
        String actionToken,
        int contextTile,
        List<int[]> chowCandidates,
        List<KongOption> kongOptions,
        int offerId) {
    /** One server-offered kong: the tile and which kong kind it forms. */
    public record KongOption(GameplayKongType kongType, int tileValue) {
        public KongOption {
            Objects.requireNonNull(kongType, "kongType");
        }
    }

    public GameplayActionOffer {
        if (seat <= 0 || offerId <= 0 || powerMask < 0 || contextTile < 0) {
            throw new IllegalArgumentException("invalid action offer numbers");
        }
        Objects.requireNonNull(actionToken, "actionToken");
        if (actionToken.isBlank()) {
            throw new IllegalArgumentException("actionToken must not be blank");
        }
        List<int[]> pairs = new ArrayList<>(chowCandidates.size());
        for (int[] pair : Objects.requireNonNull(chowCandidates, "chowCandidates")) {
            if (pair == null || pair.length != 3) {
                throw new IllegalArgumentException("a chow candidate is a three-tile comb");
            }
            pairs.add(pair.clone());
        }
        chowCandidates = List.copyOf(pairs);
        kongOptions = List.copyOf(Objects.requireNonNull(kongOptions, "kongOptions"));
    }
}
