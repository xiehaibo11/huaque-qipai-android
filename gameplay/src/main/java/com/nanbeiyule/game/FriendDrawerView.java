package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import java.util.List;

/**
 * Friend drawer overlay: a collapsed arrow tab on the left edge of the
 * lobby that slides the friend panel in over 200 ms. Rendering lives in
 * {@link FriendDrawerRenderer}, touch routing in
 * {@link FriendDrawerTouchController}, data in {@link FriendDrawerState}.
 */
public final class FriendDrawerView extends AdaptiveCanvasView {
    interface Listener {
        void onExpandedChanged(boolean expanded);

        void onLoadMoreRequested();

        void onInviteRequested(FriendEntry friend);

        void onRecallRequested(FriendEntry friend);

        void onInviteAllRequested();

        void onFriendAvatarRequested(FriendEntry friend);
    }

    private static final long ANIMATION_MS = 200L;

    private final FriendDrawerLayout layout = new FriendDrawerLayout();
    private final FriendDrawerState state = new FriendDrawerState();
    private final FriendDrawerRenderer renderer;
    private final FriendDrawerTouchController touchController;

    private boolean expanded;
    private float progress;
    private boolean animating;
    private long animationStartMs;
    private float animationFrom;

    public FriendDrawerView(Context context) {
        super(context);
        renderer = new FriendDrawerRenderer(context);
        touchController =
                new FriendDrawerTouchController(
                        layout,
                        state,
                        ViewConfiguration.get(context)
                                .getScaledTouchSlop());
        setClickable(true);
        setFocusable(true);
        setWillNotDraw(false);
        setContentDescription(
                context.getString(R.string.game_home_friend_list));
        setVisibility(GONE);
    }

    void setListener(Listener listener) {
        touchController.setListener(listener);
    }

    boolean isExpanded() {
        return expanded;
    }

    boolean isAnimating() {
        return animating;
    }

    float expandProgress() {
        return progress;
    }

    void showDrawer() {
        setVisibility(VISIBLE);
        invalidate();
    }

    void hideDrawer() {
        animating = false;
        expanded = false;
        progress = 0.0f;
        state.resetPages();
        setVisibility(GONE);
    }

    void expand() {
        setExpanded(true);
    }

    void collapse() {
        setExpanded(false);
    }

    void setTab(FriendDrawerState.Tab tab) {
        state.setTab(tab);
        invalidate();
    }

    private void setExpanded(boolean target) {
        if (target == expanded && !animating) {
            return;
        }
        expanded = target;
        animationFrom = progress;
        animationStartMs = SystemClock.uptimeMillis();
        animating = true;
        postInvalidateOnAnimation();
        Listener listener = touchControllerListener();
        if (listener != null) {
            listener.onExpandedChanged(target);
        }
    }

    private Listener touchControllerListener() {
        return touchController.listener();
    }

    void beginInitialLoad() {
        state.beginInitialLoad();
        invalidate();
    }

    void applyPage(FriendListPage page) {
        state.applyPage(page);
        invalidate();
    }

    void loadFailed() {
        state.loadFailed();
        invalidate();
    }

    int nextPage() {
        return state.nextPage();
    }

    int loadedCount() {
        return state.loadedCount();
    }

    void setUnreadApplications(int count) {
        state.setUnreadApplications(count);
        invalidate();
    }

    void setAvatarBitmap(String avatarKey, Bitmap bitmap) {
        state.putAvatar(avatarKey, bitmap);
        invalidate();
    }

    /** Wires the per-friend invite cooldown check used to dim the
     * invite button; evaluated at draw time. */
    void setInviteCooldownChecker(
            java.util.function.LongPredicate checker) {
        state.setInviteCooldownChecker(checker);
        invalidate();
    }

    List<String> missingAvatarKeys() {
        return state.missingAvatarKeys();
    }

    List<FriendEntry> onlineFriends() {
        return state.onlineFriends();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getVisibility() != VISIBLE
                || getWidth() <= 0
                || getHeight() <= 0) {
            return;
        }
        updateAnimation();
        GameHomeViewportLayout viewportLayout =
                GameHomeViewportLayout.calculate(
                        getWidth(),
                        getHeight(),
                        adaptiveSafeInsets());
        int save = canvas.save();
        AdaptiveCanvasDrawing.apply(
                canvas, viewportLayout.pageTransform());
        renderer.draw(
                canvas,
                state,
                layout,
                progress,
                System.currentTimeMillis());
        canvas.restoreToCount(save);
        if (animating) {
            postInvalidateOnAnimation();
        }
    }

    private void updateAnimation() {
        if (!animating) {
            return;
        }
        float target = expanded ? 1.0f : 0.0f;
        float elapsed =
                (SystemClock.uptimeMillis() - animationStartMs)
                        / (float) ANIMATION_MS;
        if (elapsed >= 1.0f) {
            progress = target;
            animating = false;
            return;
        }
        progress = animationFrom + (target - animationFrom) * elapsed;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        return touchController.onTouch(this, event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
