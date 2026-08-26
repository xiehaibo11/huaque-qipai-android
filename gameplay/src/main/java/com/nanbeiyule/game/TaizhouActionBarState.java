package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable state of the Taizhou mahjong action bar (吃碰杠胡动作条).
 *
 * <p>Button visibility is driven exclusively by the server-issued {@code nPower}
 * bitmap, exactly like the original {@code GameModule:analysePower}
 * ({@code BasicMahjong/Modules/GameLayer/Module.luac:295-339}): bits
 * {@code 0x001..0x080} are inspected and mapped through
 * {@code GameDefine.POWER_TO_ACTION} ({@code BasicMahjong/Define/GameDefine.luac:115-128}),
 * duplicate action ids collapse, and the result is sorted ascending. The PLAY
 * bit only arms tile preview (no button), and the recovered Taizhou build never
 * inspects the TWAIT/CWAIT/PWAIT/REPLACE bits here, so they produce no button
 * in this component either.
 *
 * <p>The bar has no persistent disabled visual in the original: the only
 * disable is a one-second re-enable debounce on the PASS button after it is
 * tapped ({@code UIMahLayerBase.luac:694-697}), which is a Wave 2 interaction
 * concern, so every visible action is reported as enabled.
 */
public final class TaizhouActionBarState {
    // GameDefine.POWER (GameDefine.luac:98-113).
    public static final int POWER_CANCEL = 0x001;
    public static final int POWER_PLAY = 0x002;
    public static final int POWER_CHOW = 0x004;
    public static final int POWER_PUNG = 0x008;
    public static final int POWER_HU = 0x010;
    public static final int POWER_MKONG = 0x020;
    public static final int POWER_CKONG = 0x040;
    public static final int POWER_TKONG = 0x080;
    public static final int POWER_TWAIT = 0x100;
    public static final int POWER_CWAIT = 0x200;
    public static final int POWER_PWAIT = 0x400;
    public static final int POWER_REPLACE = 0x800;

    // GameDefine.ACTION (GameDefine.luac:87-96); the ids double as button slots.
    public static final int ACTION_PASS = 1;
    public static final int ACTION_CHOW = 2;
    public static final int ACTION_PONG = 3;
    public static final int ACTION_KONG = 4;
    public static final int ACTION_HU = 5;
    public static final int ACTION_FLOWER = 6;
    public static final int ACTION_TING = 7;

    // GameDefine.COMB_FLAG (GameDefine.luac:130-146), the send-side meld kinds.
    public static final int COMB_FLAG_NONE = 0;
    public static final int COMB_FLAG_CHOW = 1;
    public static final int COMB_FLAG_PUNG = 2;
    public static final int COMB_FLAG_MKONG = 3;
    public static final int COMB_FLAG_CKONG = 4;
    public static final int COMB_FLAG_TKONG = 5;

    /** Which candidate panel is attached to the bar. */
    public enum CombKind {
        NONE,
        CHOW,
        KONG
    }

    private final int actionMask;
    private final int revision;
    private final boolean barVisible;
    private final CombKind combKind;
    private final List<int[]> combCandidates;

    private TaizhouActionBarState(
            int actionMask,
            int revision,
            boolean barVisible,
            CombKind combKind,
            List<int[]> combCandidates) {
        this.actionMask = actionMask;
        this.revision = revision;
        this.barVisible = barVisible;
        this.combKind = combKind;
        this.combCandidates = combCandidates;
    }

    /**
     * Builds the bar from the server {@code nPower} bitmap, replicating
     * {@code analysePower}: visible action ids deduplicated and sorted ascending.
     * The bar is visible exactly when at least one action exists, matching
     * {@code onMsgPower} ({@code Module.luac:342-369}).
     */
    public static TaizhouActionBarState fromPower(int nPower, int revision) {
        List<Integer> actions = new ArrayList<>();
        addAction(actions, nPower, POWER_CANCEL, ACTION_PASS);
        // POWER_PLAY arms the original tile preview; it has no action button.
        addAction(actions, nPower, POWER_CHOW, ACTION_CHOW);
        addAction(actions, nPower, POWER_PUNG, ACTION_PONG);
        addAction(actions, nPower, POWER_HU, ACTION_HU);
        addAction(actions, nPower, POWER_MKONG, ACTION_KONG);
        addAction(actions, nPower, POWER_CKONG, ACTION_KONG);
        addAction(actions, nPower, POWER_TKONG, ACTION_KONG);
        Collections.sort(actions);
        return new TaizhouActionBarState(
                nPower, revision, !actions.isEmpty(), CombKind.NONE, List.of());
    }

    /** An empty, invisible bar (the original {@code showAction({}, false)}). */
    public static TaizhouActionBarState hidden(int revision) {
        return new TaizhouActionBarState(0, revision, false, CombKind.NONE, List.of());
    }

    private static void addAction(List<Integer> actions, int power, int bit, int actionId) {
        if ((power & bit) == bit && !actions.contains(actionId)) {
            actions.add(actionId);
        }
    }

    /** The raw {@code nPower} bitmap this state was built from. */
    public int actionMask() {
        return actionMask;
    }

    /** The source round revision that produced this state. */
    public int revision() {
        return revision;
    }

    /** Whether the action button strip is on screen. */
    public boolean barVisible() {
        return barVisible;
    }

    /** Visible action ids, ascending, matching the original slot compaction. */
    public List<Integer> visibleActions() {
        List<Integer> actions = new ArrayList<>();
        addAction(actions, actionMask, POWER_CANCEL, ACTION_PASS);
        addAction(actions, actionMask, POWER_CHOW, ACTION_CHOW);
        addAction(actions, actionMask, POWER_PUNG, ACTION_PONG);
        addAction(actions, actionMask, POWER_HU, ACTION_HU);
        addAction(actions, actionMask, POWER_MKONG, ACTION_KONG);
        addAction(actions, actionMask, POWER_CKONG, ACTION_KONG);
        addAction(actions, actionMask, POWER_TKONG, ACTION_KONG);
        Collections.sort(actions);
        return actions;
    }

    /** Whether the given action id currently owns a visible bar button. */
    public boolean isActionVisible(int actionId) {
        return barVisible && visibleActions().contains(actionId);
    }

    /** The kind of candidate panel currently shown instead of the bar. */
    public CombKind combKind() {
        return combKind;
    }

    /**
     * Candidate melds in display order and display form (concealed kongs keep
     * their {BACK,BACK,BACK,value} rendering form), or an empty list.
     */
    public List<int[]> combCandidates() {
        return combCandidates;
    }

    public TaizhouActionBarState withBarHidden() {
        return new TaizhouActionBarState(actionMask, revision, false, combKind, combCandidates);
    }

    public TaizhouActionBarState withBarShown() {
        return new TaizhouActionBarState(
                actionMask, revision, !visibleActions().isEmpty(), combKind, combCandidates);
    }

    /** Attaches a candidate panel; {@code candidates} is defensively copied. */
    public TaizhouActionBarState withCombs(List<int[]> candidates, CombKind kind) {
        if (kind == null || kind == CombKind.NONE) {
            throw new IllegalArgumentException("a concrete comb kind is required");
        }
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException("at least one candidate is required");
        }
        List<int[]> copy = new ArrayList<>(candidates.size());
        for (int[] candidate : candidates) {
            if (candidate == null || candidate.length < 3) {
                throw new IllegalArgumentException("a candidate needs at least three tiles");
            }
            copy.add(candidate.clone());
        }
        return new TaizhouActionBarState(
                actionMask, revision, barVisible, kind, Collections.unmodifiableList(copy));
    }

    public TaizhouActionBarState withoutCombs() {
        return new TaizhouActionBarState(actionMask, revision, barVisible, CombKind.NONE, List.of());
    }
}
