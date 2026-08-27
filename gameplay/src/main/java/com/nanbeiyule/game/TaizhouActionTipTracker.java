package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayActionTip;
import java.util.Optional;

/**
 * Transient timing of the table-centre action tip (吃碰杠补花胡提示帧). The
 * original clears action animations after 0.9s in AnimationLayer.
 *
 * <p>A tip appears only when the server projection publishes a NEWER tip
 * cursor: the first cursor seen after entering the table is just a baseline,
 * so a mid-round entry never replays an old tip, and polling the same cursor
 * never extends the window.
 */
final class TaizhouActionTipTracker {
    static final long TIP_VISIBLE_MILLIS = 900L;

    record VisibleTip(GameplayActionTip tip, long elapsedMillis) {}

    private GameplayActionTip lastSeen;
    private GameplayActionTip shownTip;
    private long shownAtElapsed = Long.MIN_VALUE;
    private boolean baselineEstablished;

    /** Feeds the projection's current tip; {@code null} when none exists. */
    void update(GameplayActionTip tip, long nowElapsed) {
        if (!baselineEstablished) {
            // Entering the table mid-round must not replay the tip that is
            // already in the projection; the first feed is only the baseline.
            baselineEstablished = true;
            lastSeen = tip;
            return;
        }
        if (tip == null || !tip.isNewerThan(lastSeen)) {
            return;
        }
        shownTip = tip;
        shownAtElapsed = nowElapsed;
        lastSeen = tip;
    }

    /** Returns the tip kind to draw at {@code nowElapsed}, or empty. */
    Optional<GameplayActionTip.Kind> visibleKind(long nowElapsed) {
        return visibleTip(nowElapsed).map(visible -> visible.tip().kind());
    }

    Optional<VisibleTip> visibleTip(long nowElapsed) {
        if (shownTip == null) {
            return Optional.empty();
        }
        long elapsed = nowElapsed - shownAtElapsed;
        if (elapsed < 0L || elapsed >= TIP_VISIBLE_MILLIS) {
            return Optional.empty();
        }
        return Optional.of(new VisibleTip(shownTip, elapsed));
    }

    /** Milliseconds until the running tip hides; 0 when nothing is showing. */
    long remainingMillis(long nowElapsed) {
        if (shownTip == null) {
            return 0L;
        }
        return Math.max(0L, TIP_VISIBLE_MILLIS - (nowElapsed - shownAtElapsed));
    }
}
