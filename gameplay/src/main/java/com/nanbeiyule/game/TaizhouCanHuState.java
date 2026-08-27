package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayTingInfo;
import com.nanbeiyule.game.mahjong.MahjongTile;
import java.util.List;

/**
 * Immutable projection of the CanHuMahs 听牌可胡提示层. The grid math is copied
 * verbatim from {@code CanHuMahsUI.luac:initUI}: column count per data size,
 * background size, the one/two-row padding, the whole-node scale thresholds and
 * the 胡任意牌 single-cell special case. Local tile positions are relative to
 * the background's content box (Cocos bottom-up Y).
 */
public record TaizhouCanHuState(
        boolean visible,
        List<Integer> huTargets,
        List<List<InfoSegment>> infoRows,
        int columns,
        int rows,
        float backgroundWidth,
        float backgroundHeight,
        float nodeScale,
        Source source,
        boolean anyTile) {
    public enum Source {
        SELECTED_TILE,
        TING_BUTTON
    }

    /**
     * 原版 {@code data[i]} 的 {@code huInfoNum}/{@code huInfo} 按逗号切出的一段：
     * 数字加单位（「台」「胡」「张」），一格最多三段。
     */
    public record InfoSegment(int number, String unit) {}

    private static final TaizhouCanHuState HIDDEN =
            new TaizhouCanHuState(
                    false,
                    List.of(),
                    List.of(),
                    0,
                    0,
                    0.0f,
                    0.0f,
                    1.0f,
                    Source.SELECTED_TILE,
                    false);

    public TaizhouCanHuState {
        huTargets = huTargets == null ? List.of() : List.copyOf(huTargets);
        infoRows = infoRows == null ? List.of() : List.copyOf(infoRows);
        if (!infoRows.isEmpty() && infoRows.size() != huTargets.size()) {
            throw new IllegalArgumentException("one info row per hu target");
        }
        for (List<InfoSegment> row : infoRows) {
            if (row.size() > TaizhouCanHuLayout.INFO_ROW_OFFSETS.length) {
                throw new IllegalArgumentException("at most three info segments per cell");
            }
        }
        source = source == null ? Source.SELECTED_TILE : source;
    }

    static TaizhouCanHuState hidden() {
        return HIDDEN;
    }

    static TaizhouCanHuState shown(
            List<Integer> huTargets, List<List<InfoSegment>> infoRows) {
        return shown(huTargets, infoRows, Source.SELECTED_TILE);
    }

    static TaizhouCanHuState shownFromTingButton(
            List<Integer> huTargets, List<List<InfoSegment>> infoRows) {
        return shown(huTargets, infoRows, Source.TING_BUTTON);
    }

    private static TaizhouCanHuState shown(
            List<Integer> huTargets, List<List<InfoSegment>> infoRows, Source source) {
        if (huTargets == null || huTargets.isEmpty()) {
            throw new IllegalArgumentException("huTargets are required");
        }
        for (int target : huTargets) {
            if (!MahjongTile.isValid(target) && target != GameplayTingInfo.ANY_TILE) {
                throw new IllegalArgumentException("invalid hu target " + target);
            }
        }
        int count = huTargets.size();
        // CanHuMahsUI.luac: width = #data (<4) / 4 (≤8) / 5 (≤20) / 6 (>20)。
        int columns = count < 4 ? count : (count <= 8 ? 4 : (count <= 20 ? 5 : 6));
        int rows = (count + columns - 1) / columns;
        boolean anyTile = count == 1 && huTargets.get(0) == GameplayTingInfo.ANY_TILE;
        // 胡任意牌固定 375 宽，否则 (width+1)*235。
        float backgroundWidth =
                anyTile
                        ? TaizhouCanHuLayout.ANY_TILE_BG_WIDTH
                        : (columns + 1) * TaizhouCanHuLayout.COLUMN_UNIT;
        // height*200，一/两行再 +25。
        float backgroundHeight =
                rows * TaizhouCanHuLayout.ROW_UNIT
                        + (rows <= 2 ? TaizhouCanHuLayout.SHORT_GRID_PADDING : 0.0f);
        // 21..30 → 0.85；>30 → 0.7。
        float nodeScale =
                count > 30
                        ? TaizhouCanHuLayout.HUGE_GRID_SCALE
                        : (count > 20 ? TaizhouCanHuLayout.LARGE_GRID_SCALE : 1.0f);
        return new TaizhouCanHuState(
                true,
                huTargets,
                infoRows,
                columns,
                rows,
                backgroundWidth,
                backgroundHeight,
                nodeScale,
                source,
                anyTile);
    }

    /** Lua tile {@code x = 260 + col*230} (background-local, anchor (0.5,0.5)). */
    public float tileLocalX(int zeroBasedIndex) {
        return TaizhouCanHuLayout.TILE_FIRST_LOCAL_X
                + (zeroBasedIndex % columns) * TaizhouCanHuLayout.TILE_STEP_X;
    }

    /** Lua tile {@code y = bgHeight - 120 - row*190} (background-local Cocos Y). */
    public float tileLocalY(int zeroBasedIndex) {
        return backgroundHeight
                - TaizhouCanHuLayout.TILE_FIRST_TOP_INSET
                - (zeroBasedIndex / columns) * TaizhouCanHuLayout.TILE_STEP_Y;
    }
}
