package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.gameplay.GameplayTingInfo;
import com.nanbeiyule.game.mahjong.TaizhouCanHuSurplus;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayInteraction;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Visibility state machine of the CanHuMahs 听牌可胡提示层.
 *
 * <p>Original gating ({@code UIMahLayerBase:_checkCanShowTing} +
 * {@code _onSelectedMah}): the HAVE_TING setting must be on and a ting hand
 * tile must be selected; deselecting removes the window. The Wave 3 contract
 * adds: a fresh TING_INFO (content change) hides the tip until the next
 * selection change, and a cleared/absent ting map hides it outright. Only the
 * own seat's ting data is consulted, mirroring the original's bottom-seat
 * {@code getCanHuMahsData}.
 *
 * <p>After the bottom seat discards a ting tile, original
 * {@code GameLayer/Module.luac} stores {@code getCanHuMahsData(seat)[mah]} as
 * last ting data, hides CanHuMahsUI, and shows {@code RightBtnsLayer._KW_BTN_TING}.
 * Tapping that right-side button reopens CanHuMahsUI without a tingMahID, which
 * uses the {@code _model} branch instead of the bottom selected-tile branch.
 */
final class TaizhouCanHuTracker {
    private GameplayTableState tableState;
    private TaizhouMahjongPreferences preferences = TaizhouMahjongPreferences.defaults();
    private TaizhouMahjongPlayInteraction interaction;
    private GameplayTingInfo seenTingInfo;
    private Integer seenDiscard;
    private boolean suppressed;
    private List<GameplayTingInfo.HuTarget> lastTingTargets = List.of();
    private boolean showFanNum;
    private boolean showHuNum;
    private boolean showingTingButtonPanel;
    private TaizhouCanHuState current = TaizhouCanHuState.hidden();

    /** Refreshes the full context; called on every new table projection. */
    void update(
            GameplayTableState state,
            TaizhouMahjongPreferences prefs,
            TaizhouMahjongPlayInteraction playInteraction) {
        tableState = state;
        preferences =
                prefs == null ? TaizhouMahjongPreferences.defaults() : prefs;
        interaction = playInteraction;
        recompute();
    }

    /** 触摸路由透传：手牌手势（按下/移动/抬起/取消）后按缓存上下文重算。 */
    void onHandGesture(TaizhouMahjongPlayInteraction playInteraction) {
        interaction = playInteraction;
        showingTingButtonPanel = false;
        recompute();
    }

    TaizhouCanHuState current() {
        return current;
    }

    void onSelfDiscardRequested(int discard) {
        GameplayTingInfo tingInfo = ownTingInfo();
        List<GameplayTingInfo.HuTarget> targets =
                tingInfo == null ? List.of() : tingInfo.huTargetsFor(discard);
        rememberShowFlags(tingInfo);
        lastTingTargets = List.copyOf(targets);
        showingTingButtonPanel = false;
        suppressed = true;
        current = TaizhouCanHuState.hidden();
    }

    boolean tingButtonVisible() {
        return preferences.tingHintEnabled()
                && tableState != null
                && tableState.phase() == GameplayPhase.PLAYING
                && !lastTingTargets.isEmpty();
    }

    void onTingButtonClicked() {
        if (!tingButtonVisible()) {
            return;
        }
        showingTingButtonPanel = true;
        current =
                TaizhouCanHuState.shownFromTingButton(
                        tiles(lastTingTargets), infoRows(lastTingTargets));
    }

    /** Original CanHuMahsUI:onBgClick closes the window without clearing last ting data. */
    void onBackgroundClicked() {
        if (!current.visible()) {
            return;
        }
        showingTingButtonPanel = false;
        suppressed = true;
        current = TaizhouCanHuState.hidden();
    }

    private void recompute() {
        if (tableState == null || tableState.phase() != GameplayPhase.PLAYING) {
            lastTingTargets = List.of();
            showingTingButtonPanel = false;
            current = TaizhouCanHuState.hidden();
            return;
        }
        if (!preferences.tingHintEnabled()) {
            showingTingButtonPanel = false;
            current = TaizhouCanHuState.hidden();
            return;
        }
        if (showingTingButtonPanel && tingButtonVisible()) {
            current =
                    TaizhouCanHuState.shownFromTingButton(
                            tiles(lastTingTargets), infoRows(lastTingTargets));
            return;
        }

        GameplayTingInfo tingInfo = ownTingInfo();
        Integer discard = selectedDiscard();
        // 新 TING_INFO（内容变化）→ 隐藏到下次换选；值相等的快照回放缓存不算新数据。
        if (!Objects.equals(tingInfo, seenTingInfo)) {
            suppressed = true;
        }
        if (!Objects.equals(discard, seenDiscard)) {
            suppressed = false;
        }
        seenTingInfo = tingInfo;
        seenDiscard = discard;
        rememberShowFlags(tingInfo);

        // 原版 UIMahLayer:_checkCanShowTing 还要求 getPlayPower()：没轮到自己时选中手牌
        // 只抬牌，不弹可胡提示。
        if (suppressed
                || tingInfo == null
                || discard == null
                || interaction == null
                || !interaction.hasPlayPermission()) {
            current = TaizhouCanHuState.hidden();
            return;
        }
        List<GameplayTingInfo.HuTarget> targets = tingInfo.huTargetsFor(discard);
        current =
                targets.isEmpty()
                        ? TaizhouCanHuState.hidden()
                        : TaizhouCanHuState.shown(tiles(targets), infoRows(targets));
    }

    private void rememberShowFlags(GameplayTingInfo tingInfo) {
        if (tingInfo != null) {
            showFanNum = tingInfo.showFanNum();
            showHuNum = tingInfo.showHuNum();
        }
    }

    private static List<Integer> tiles(List<GameplayTingInfo.HuTarget> targets) {
        List<Integer> tiles = new ArrayList<>(targets.size());
        for (GameplayTingInfo.HuTarget target : targets) {
            tiles.add(target.tile());
        }
        return tiles;
    }

    /**
     * 每格的 {@code huInfoNum}/{@code huInfo}：原版 {@code GameModule:canHuInfoNum} 先按
     * {@code bShowFanNum}/{@code bShowHuNum} 拼「N台」「N胡」，{@code CanHuMahsUI:initUI} 的
     * {@code needGetSurplusMahCount} 分支再补一段「N张」；胡任意牌没有剩余张数。
     */
    private List<List<TaizhouCanHuState.InfoSegment>> infoRows(
            List<GameplayTingInfo.HuTarget> targets) {
        List<List<TaizhouCanHuState.InfoSegment>> rows = new ArrayList<>(targets.size());
        for (GameplayTingInfo.HuTarget target : targets) {
            List<TaizhouCanHuState.InfoSegment> segments = new ArrayList<>(3);
            if (showFanNum) {
                segments.add(
                        new TaizhouCanHuState.InfoSegment(
                                target.fanPoint(), TaizhouCanHuLayout.FAN_UNIT));
            }
            if (showHuNum) {
                segments.add(
                        new TaizhouCanHuState.InfoSegment(
                                target.huPoint(), TaizhouCanHuLayout.HU_UNIT));
            }
            if (target.tile() != GameplayTingInfo.ANY_TILE) {
                segments.add(
                        new TaizhouCanHuState.InfoSegment(
                                TaizhouCanHuSurplus.remaining(tableState, target.tile()),
                                TaizhouCanHuLayout.SURPLUS_UNIT));
            }
            rows.add(List.copyOf(segments));
        }
        return rows;
    }

    private GameplayTingInfo ownTingInfo() {
        if (tableState == null) {
            return null;
        }
        GameplayTingInfo tingInfo = tableState.tingInfo().orElse(null);
        // 原版只读本家（BOTTOM）座位的 getCanHuMahsData。
        return tingInfo != null && tingInfo.seat() == tableState.mySeat() ? tingInfo : null;
    }

    private Integer selectedDiscard() {
        if (interaction == null) {
            return null;
        }
        Integer selectedIndex = interaction.visualState().selectedIndex();
        if (selectedIndex == null) {
            return null;
        }
        for (TaizhouMahjongPlayGesture.Tile tile : interaction.tiles()) {
            if (tile.index() == selectedIndex) {
                return tile.value();
            }
        }
        return null;
    }
}
