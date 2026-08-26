package com.nanbeiyule.game;

/** Keeps tap classification sticky across the whole gesture and binds it to one target. */
final class TapGestureGuard {
    static final int NO_TARGET = -1;

    private final float slop;
    private float downX;
    private float downY;
    private int downTarget = NO_TARGET;
    private boolean active;
    private boolean movedBeyondSlop;

    TapGestureGuard(float slop) {
        if (slop < 0f) {
            throw new IllegalArgumentException("slop must not be negative");
        }
        this.slop = slop;
    }

    void begin(float x, float y, int target) {
        downX = x;
        downY = y;
        downTarget = target;
        active = true;
        movedBeyondSlop = false;
    }

    void move(float x, float y) {
        if (active
                && (Math.abs(x - downX) > slop || Math.abs(y - downY) > slop)) {
            movedBeyondSlop = true;
        }
    }

    boolean finish(float x, float y, int target) {
        move(x, y);
        boolean tap =
                active
                        && !movedBeyondSlop
                        && downTarget != NO_TARGET
                        && downTarget == target;
        reset();
        return tap;
    }

    void reset() {
        active = false;
        movedBeyondSlop = false;
        downTarget = NO_TARGET;
    }
}
