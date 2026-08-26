package com.nanbeiyule.game.mahjong;

import java.util.Objects;
import java.util.Set;

/** Private, server-authorized input state for one Taizhou Mahjong turn. */
public record TaizhouMahjongPlayPermission(
        String actionToken,
        TaizhouMahjongPlayGesture.Mode mode,
        Set<Integer> playableOriginalIndexes,
        Set<Integer> tingOriginalIndexes,
        Set<Integer> actionMaskOriginalIndexes,
        Set<Integer> preBaoOriginalIndexes) {
    public TaizhouMahjongPlayPermission {
        if (actionToken == null || actionToken.isBlank()) {
            throw new IllegalArgumentException("authoritative action token is required");
        }
        mode = Objects.requireNonNull(mode, "mode");
        playableOriginalIndexes = copyIndexes(playableOriginalIndexes, "playableOriginalIndexes");
        tingOriginalIndexes = copyIndexes(tingOriginalIndexes, "tingOriginalIndexes");
        actionMaskOriginalIndexes =
                copyIndexes(actionMaskOriginalIndexes, "actionMaskOriginalIndexes");
        preBaoOriginalIndexes = copyIndexes(preBaoOriginalIndexes, "preBaoOriginalIndexes");
        if (playableOriginalIndexes.isEmpty()) {
            throw new IllegalArgumentException("at least one playable index is required");
        }
    }

    public TaizhouMahjongPlayPermission withMode(TaizhouMahjongPlayGesture.Mode nextMode) {
        return new TaizhouMahjongPlayPermission(
                actionToken,
                nextMode,
                playableOriginalIndexes,
                tingOriginalIndexes,
                actionMaskOriginalIndexes,
                preBaoOriginalIndexes);
    }

    private static Set<Integer> copyIndexes(Set<Integer> source, String name) {
        Set<Integer> copy = Set.copyOf(Objects.requireNonNull(source, name));
        for (Integer index : copy) {
            if (index == null || index < 0) {
                throw new IllegalArgumentException(name + " contains an invalid index");
            }
        }
        return copy;
    }
}
