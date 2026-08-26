package com.nanbeiyule.game.gameplay;

/**
 * The latest transient action-tip trigger in the table projection, raised by
 * MELD_APPLIED (CHOW/PONG/KONG), FLOWER_REPLACED (FLOWER) and WIN_DECLARED
 * (HU). The tip is event-sourced metadata: the table view tracks the cursor to
 * show the matching {@code taizhou_mahjong_action_tip} frame briefly.
 */
public record GameplayActionTip(GameplayActionTip.Kind kind, long revision, int eventOrder) {
    /** Tip kinds mapped to the original {@code onBtnAction.plist} frames. */
    public enum Kind {
        /** {@code act_chi.png}. */
        CHOW,
        /** {@code act_peng.png}. */
        PONG,
        /** {@code act_gang.png}. */
        KONG,
        /** {@code act_buhua.png}. */
        FLOWER,
        /** {@code eff_hupai.png}. */
        HU
    }

    public GameplayActionTip {
        if (kind == null || revision <= 0 || eventOrder <= 0) {
            throw new IllegalArgumentException("invalid action tip cursor");
        }
    }

    /** Returns whether this tip was triggered after {@code other}. */
    public boolean isNewerThan(GameplayActionTip other) {
        if (other == null) {
            return true;
        }
        return revision > other.revision
                || (revision == other.revision && eventOrder > other.eventOrder);
    }
}
