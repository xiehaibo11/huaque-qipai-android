package com.nanbeiyule.game;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.MotionEvent;
import com.nanbeiyule.game.TaizhouSettingNewLayout.Page;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;

/** Taizhou Mahjong 30109 / 30400 的 SettingNew.csb 流程。 */
@SuppressLint("ViewConstructor")
final class TaizhouSettingView extends TaizhouToolView {
    interface Actions {
        void onSettingsChanged(PersonalCenterSystemSettings settings);

        void onPreferencesChanged(TaizhouMahjongPreferences preferences);

        void onStyleChanged(TaizhouSettingStyle style);

        void onExitRequested();

        void onTrustRequested();
    }

    private static final float DRAG_SLOP = 12.0f;

    private final Actions actions;
    private final boolean goldRoom;
    private final TaizhouSettingNewRenderer renderer;
    private final TaizhouSettingArea7109Catalog catalog =
            TaizhouSettingArea7109Catalog.original();
    private PersonalCenterSystemSettings settings;
    private TaizhouMahjongPreferences preferences;
    private TaizhouSettingStyle style;
    private Runnable dismissAction = () -> {};
    private Page currentPage;
    private TaizhouSettingNewViewport viewport = TaizhouSettingNewViewport.DESIGN;
    private float rootX = TaizhouSettingNewLayout.DESIGN_WIDTH;
    private float scrollY;
    private float downX;
    private float downY;
    private float downScrollY;
    private long animationStartedAt;
    private boolean dragging;
    private TaizhouSettingNewInteraction.SliderHit activeSlider;
    private boolean closing;

    TaizhouSettingView(
            Context context,
            PersonalCenterSystemSettings settings,
            TaizhouMahjongPreferences preferences,
            TaizhouSettingStyle style,
            boolean goldRoom,
            Actions actions) {
        super(context);
        this.settings = settings == null ? PersonalCenterSystemSettings.defaults() : settings;
        this.preferences = preferences == null
                ? TaizhouMahjongPreferences.defaults() : preferences;
        this.style = style == null ? TaizhouSettingStyle.defaults() : style;
        this.goldRoom = goldRoom;
        this.actions = actions;
        renderer = new TaizhouSettingNewRenderer(context);
        setContentDescription("牌局设置");
        post(this::animateIn);
    }

    void setDismissAction(Runnable dismissAction) {
        this.dismissAction = dismissAction == null ? () -> {} : dismissAction;
    }

    @Override
    protected void drawDesign(Canvas canvas) {
        TaizhouSettingNewViewport next = TaizhouSettingNewViewport.of(designViewport());
        if (!next.equals(viewport)) {
            // _KW_PANAEL_BG 是 BothEdge，可视区变化时面板要跟着重排。
            rootX += next.menuOpenX() - viewport.menuOpenX();
            viewport = next;
        }
        // _KW_PANAEL_BG：BackColorAlpha=102 的黑色遮罩，铺满可视区。
        fillPaint.setColor(Color.argb(102, 0, 0, 0));
        canvas.drawRect(0.0f, viewport.top(), viewport.right(), viewport.bottom(), fillPaint);
        renderer.draw(canvas, this, rootX, currentPage, settings, preferences, style,
                scrollY, animationSeconds(), goldRoom, viewport);
        if (currentPage == Page.ANIMATION) {
            // DragonBones 预览循环播放（playDargonBonesAnimByTimes(params, 0)）。
            postInvalidateOnAnimation();
        }
    }

    private float animationSeconds() {
        if (animationStartedAt == 0L) {
            return 0.0f;
        }
        return (SystemClock.uptimeMillis() - animationStartedAt) / 1000.0f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (closing) {
            return true;
        }
        float x = designX(event);
        float y = designY(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> onDown(x, y);
            case MotionEvent.ACTION_MOVE -> onMove(x, y);
            case MotionEvent.ACTION_UP -> onUp(x, y);
            default -> { }
        }
        return true;
    }

    private void onDown(float x, float y) {
        downX = x;
        downY = y;
        downScrollY = scrollY;
        dragging = false;
        activeSlider = null;
        if (currentPage == Page.MAH) {
            activeSlider = TaizhouSettingNewInteraction.sliderAt(
                    detailX(x), y - viewport.topOffset());
            if (activeSlider != null) {
                applySlider(activeSlider);
            }
        }
    }

    private void onMove(float x, float y) {
        if (activeSlider != null) {
            TaizhouSettingNewInteraction.SliderHit hit =
                    TaizhouSettingNewInteraction.sliderAt(
                            detailX(x), y - viewport.topOffset());
            applySlider(hit != null
                    ? hit
                    : new TaizhouSettingNewInteraction.SliderHit(
                            activeSlider.slider(), x < downX ? 0.0f : 1.0f));
            dragging = true;
            return;
        }
        float range = currentPage == null ? 0.0f
                : TaizhouSettingNewOptions.scrollRange(currentPage);
        if (range <= 0.0f) {
            return;
        }
        if (!dragging && Math.abs(y - downY) < DRAG_SLOP) {
            return;
        }
        dragging = true;
        scrollY = Math.max(0.0f, Math.min(range, downScrollY - (y - downY)));
        invalidate();
    }

    private void onUp(float x, float y) {
        if (dragging || activeSlider != null) {
            activeSlider = null;
            dragging = false;
            return;
        }
        if (currentPage == null) {
            handleMenuTouch(x - rootX, y);
        } else {
            handleDetailTouch(x, y);
        }
    }

    private void handleMenuTouch(float localX, float rawY) {
        float y = rawY - viewport.topOffset();
        if (localX < 0.0f || TaizhouSettingNewInteraction.closeContains(localX, y)) {
            performClick();
            animateOut(dismissAction);
            return;
        }
        if (handleBasicFunctionTouch(localX, y)) {
            return;
        }
        Page selected = TaizhouSettingNewInteraction.menuPageAt(localX, y);
        if (selected != null) {
            performClick();
            openPage(selected);
        } else if (TaizhouSettingNewInteraction.roomButtonContains(
                localX, rawY - viewport.bottomOffset(), goldRoom)) {
            performClick();
            actions.onExitRequested();
            animateOut(dismissAction);
        }
    }

    private void handleDetailTouch(float x, float rawY) {
        if (TaizhouSettingNewLayout.detailCloseButton(viewport).contains(x, rawY)) {
            performClick();
            animateOut(dismissAction);
            return;
        }
        if (x < TaizhouSettingNewLayout.DETAIL_LOCAL_X) {
            handleMenuColumnTouch(x, rawY);
            return;
        }
        float localX = detailX(x);
        float y = rawY - TaizhouSettingNewOptions.anchorOffset(currentPage, viewport);
        if (currentPage == Page.ADVANCED) {
            handleAdvancedTouch(localX, rawY);
            return;
        }
        int plan = TaizhouSettingNewInteraction.planAt(
                localX, rawY - viewport.bottomOffset(), catalog.customPlanIndex());
        if (plan >= 0) {
            performClick();
            applyStyle(plan == catalog.customPlanIndex()
                    ? style
                    : TaizhouSettingStyle.ofPlan(plan + 1));
            return;
        }
        TaizhouSettingNewInteraction.Selection selection =
                TaizhouSettingNewInteraction.optionAt(currentPage, localX, y, scrollY);
        if (selection != null) {
            performClick();
            applyStyle(style.with(selection.choice(),
                    catalog.realValue(selection.choice(), selection.index())));
        }
    }

    private void handleMenuColumnTouch(float localX, float rawY) {
        float y = rawY - viewport.topOffset();
        if (handleBasicFunctionTouch(localX, y)) {
            return;
        }
        Page selected = TaizhouSettingNewInteraction.menuPageAt(localX, y);
        if (selected != null) {
            performClick();
            openPage(selected);
            return;
        }
        if (currentPage != Page.ADVANCED
                && TaizhouSettingNewInteraction.saveButtonContains(
                        localX, rawY - viewport.bottomOffset())) {
            performClick();
            actions.onStyleChanged(style);
            animateOut(dismissAction);
        }
    }

    private void handleAdvancedTouch(float localX, float rawY) {
        TaizhouSettingNewInteraction.ToggleHit hit =
                TaizhouSettingNewInteraction.toggleAt(
                        localX, rawY - viewport.topOffset(), rawY - viewport.bottomOffset());
        if (hit != null) {
            performClick();
            applyToggle(hit);
            return;
        }
        int voice = TaizhouSettingNewInteraction.voiceIndexAt(
                this, localX, rawY - viewport.topOffset());
        if (voice >= 0) {
            performClick();
            updatePreferences(preferences.withDialectEnabled(voice == 1));
        }
    }

    private boolean handleBasicFunctionTouch(float localX, float y) {
        if (TaizhouSettingNewInteraction.voiceSwitchContains(localX, y)) {
            performClick();
            update(settings.withMaleVoice(!settings.maleVoice()));
            return true;
        }
        if (TaizhouSettingNewInteraction.trustButtonContains(localX, y)) {
            performClick();
            actions.onTrustRequested();
            animateOut(dismissAction);
            return true;
        }
        return false;
    }

    private void applyToggle(TaizhouSettingNewInteraction.ToggleHit hit) {
        boolean on = hit.on();
        switch (hit.toggle()) {
            case TING_HINT -> updatePreferences(preferences.withTingHintEnabled(on));
            case PLAY_MODE -> updatePreferences(preferences.withPlayMode(
                    on ? TaizhouMahjongPlayGesture.Mode.SINGLE_CLICK
                            : TaizhouMahjongPlayGesture.Mode.DOUBLE_CLICK));
            case SOUND -> update(settings.withSoundEnabled(on));
            case PURE_MODE -> updatePreferences(preferences.withPureModeEnabled(on));
            case MUSIC -> update(settings.withMusicEnabled(on));
        }
    }

    private void applySlider(TaizhouSettingNewInteraction.SliderHit hit) {
        applyStyle(style.with(hit.slider(), hit.percent()));
    }

    private void openPage(Page page) {
        currentPage = page;
        scrollY = 0.0f;
        if (page == Page.ANIMATION && animationStartedAt == 0L) {
            animationStartedAt = SystemClock.uptimeMillis();
        }
        animateRootTo(0.0f, null);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private float detailX(float x) {
        return x - rootX - TaizhouSettingNewLayout.DETAIL_LOCAL_X;
    }

    private void update(PersonalCenterSystemSettings next) {
        settings = next;
        actions.onSettingsChanged(next);
        invalidate();
    }

    private void updatePreferences(TaizhouMahjongPreferences next) {
        preferences = next;
        actions.onPreferencesChanged(next);
        invalidate();
    }

    private void applyStyle(TaizhouSettingStyle next) {
        style = next;
        invalidate();
    }

    private void animateIn() {
        animateRootTo(viewport.menuOpenX(), null);
    }

    private void animateOut(Runnable completion) {
        closing = true;
        animateRootTo(viewport.menuClosedX(), completion);
    }

    private void animateRootTo(float targetX, Runnable completion) {
        ValueAnimator animator = ValueAnimator.ofFloat(rootX, targetX);
        animator.setDuration(200L);
        animator.addUpdateListener(
                animation -> {
                    rootX = (float) animation.getAnimatedValue();
                    invalidate();
                });
        if (completion != null) {
            animator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    completion.run();
                }
            });
        }
        animator.start();
    }
}
