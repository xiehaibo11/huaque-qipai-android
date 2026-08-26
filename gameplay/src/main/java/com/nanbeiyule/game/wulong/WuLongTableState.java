package com.nanbeiyule.game.wulong;

import com.nanbeiyule.game.gameplay.GameplaySnapshot;
import com.nanbeiyule.game.gameplay.WuLongRound;
import java.util.Optional;

/** Immutable view of only the authenticated viewer's authoritative 30588 snapshot. */
public record WuLongTableState(GameplaySnapshot snapshot, Optional<WuLongRound> round) {
    public static WuLongTableState from(GameplaySnapshot snapshot) {
        if (snapshot.gameId() != 30588L) throw new IllegalArgumentException("not a 30588 session");
        return new WuLongTableState(snapshot, snapshot.wuLongRound());
    }
}
