package com.nanbeiyule.game.gameplay;

import com.nanbeiyule.game.mahjong.MahjongTile;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Server-projected ting (listen) information for one seat: which tiles each
 * discard could hu. Mirrors the original {@code getCanHuMahsData(seat)} map
 * that {@code CanHuMahsUI.luac} renders when a ting hand tile is selected.
 */
public record GameplayTingInfo(int seat, Map<Integer, List<Integer>> huTargetsByDiscard) {
    /** {@code MahLogic.isRenYiMahValue}: the 255 pseudo value meaning "hu any tile". */
    public static final int ANY_TILE = 255;

    public GameplayTingInfo {
        if (seat <= 0) {
            throw new IllegalArgumentException("ting seat must be positive");
        }
        Objects.requireNonNull(huTargetsByDiscard, "huTargetsByDiscard");
        Map<Integer, List<Integer>> copy = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : huTargetsByDiscard.entrySet()) {
            int discard = Objects.requireNonNull(entry.getKey(), "discard");
            if (!MahjongTile.isValid(discard)) {
                throw new IllegalArgumentException("invalid ting discard " + discard);
            }
            List<Integer> targets = Objects.requireNonNull(entry.getValue(), "huTargets");
            for (int target : targets) {
                if (!MahjongTile.isValid(target) && target != ANY_TILE) {
                    throw new IllegalArgumentException("invalid ting hu target " + target);
                }
            }
            copy.put(discard, List.copyOf(targets));
        }
        // 保留服务端下发顺序，Map.copyOf 不保证迭代序。
        huTargetsByDiscard = Collections.unmodifiableMap(copy);
    }

    /** Returns the hu targets for {@code discard}, or an empty list when none. */
    public List<Integer> huTargetsFor(int discard) {
        return huTargetsByDiscard.getOrDefault(discard, List.of());
    }
}
