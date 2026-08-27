package com.nanbeiyule.game.gameplay;

import com.nanbeiyule.game.mahjong.MahjongTile;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Server-projected ting (listen) information for one seat: which tiles each
 * discard could hu, and what each of them is worth. Mirrors the original
 * {@code msgAllWaitInfo}: {@code bShowFanNum}/{@code bShowHuNum} plus the
 * {@code nWaitMahs}/{@code nFanPoint}/{@code nHuPoint} arrays that
 * {@code GameLayer/Module.lua:onMsgAllWaitInfo} folds into the
 * {@code getCanHuMahsData(seat)} map rendered by {@code CanHuMahsUI}.
 */
public record GameplayTingInfo(
        int seat,
        boolean showFanNum,
        boolean showHuNum,
        Map<Integer, List<HuTarget>> huTargetsByDiscard) {
    /** {@code MahLogic.isRenYiMahValue}: the 255 pseudo value meaning "hu any tile". */
    public static final int ANY_TILE = 255;

    /** 原版 {@code nWaitMahs[j]}/{@code nFanPoint[j]}/{@code nHuPoint[j]} 的一项。 */
    public record HuTarget(int tile, int fanPoint, int huPoint) {}

    public GameplayTingInfo {
        if (seat <= 0) {
            throw new IllegalArgumentException("ting seat must be positive");
        }
        Objects.requireNonNull(huTargetsByDiscard, "huTargetsByDiscard");
        Map<Integer, List<HuTarget>> copy = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<HuTarget>> entry : huTargetsByDiscard.entrySet()) {
            int discard = Objects.requireNonNull(entry.getKey(), "discard");
            if (!MahjongTile.isValid(discard)) {
                throw new IllegalArgumentException("invalid ting discard " + discard);
            }
            List<HuTarget> targets = Objects.requireNonNull(entry.getValue(), "huTargets");
            for (HuTarget target : targets) {
                if (!MahjongTile.isValid(target.tile()) && target.tile() != ANY_TILE) {
                    throw new IllegalArgumentException("invalid ting hu target " + target.tile());
                }
            }
            copy.put(discard, List.copyOf(targets));
        }
        // 保留服务端下发顺序，Map.copyOf 不保证迭代序。
        huTargetsByDiscard = Collections.unmodifiableMap(copy);
    }

    /** Returns the hu targets for {@code discard}, or an empty list when none. */
    public List<HuTarget> huTargetsFor(int discard) {
        return huTargetsByDiscard.getOrDefault(discard, List.of());
    }
}
