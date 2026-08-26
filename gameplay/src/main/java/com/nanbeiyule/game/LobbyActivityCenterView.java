package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.MotionEvent;
import java.util.List;

/** Original Zhejiang activity center with the recovered free-draw panel embedded in place. */
@SuppressLint("ViewConstructor")
final class LobbyActivityCenterView extends AdaptiveCanvasView {
    interface Actions {
        void onActivityRequested(LobbyActivityCenterModel.Destination destination);

        void onDismissRequested();

        default void onAnnouncementRequested() {}

        default void onAwardCenterRequested() {}

        default void onGoldMembershipClaim(String productCode) {}
    }

    private enum TargetType { NONE, ROW, CONTENT, ANNOUNCEMENT, AWARD_CENTER, CLOSE }

    private record Target(TargetType type, int index) {
        static final Target NONE = new Target(TargetType.NONE, -1);
    }

    private final Actions actions;
    private final List<LobbyActivityCenterModel.Item> items = LobbyActivityCenterModel.items();
    private final OriginalActivityCenterChrome chrome;
    private final FreeDrawPanelOverlay freeDrawOverlay;
    private final GoldMembershipCardsPanel goldMembershipPanel;
    private final LoginGiftPanelRenderer loginGiftPanel;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Bitmap freeDrawPreview;
    private final Bitmap loginGiftBadge;
    private int selectedIndex;
    private Target pressed = Target.NONE;
    private Runnable buttonClickSound = () -> {};
    private FreeDrawState freeDrawState;
    private long freeDrawAnimationStartMillis;

    LobbyActivityCenterView(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        chrome = new OriginalActivityCenterChrome(context);
        freeDrawOverlay = new FreeDrawPanelOverlay(context);
        goldMembershipPanel = new GoldMembershipCardsPanel(context);
        loginGiftPanel = new LoginGiftPanelRenderer(context);
        freeDrawPreview = bitmap(R.drawable.original_activity_free_draw);
        loginGiftBadge = bitmap(R.drawable.daily_mission_original_red_point);
        textPaint.setTypeface(
                Typeface.createFromAsset(context.getAssets(), "fonts/zihun_jingdian_lihei.ttf"));
        setClickable(true);
        setFocusable(true);
        setContentDescription("活动：免费抽奖；可切换公告");
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    void setFreeDrawState(FreeDrawState state) {
        if (state == null) {
            freeDrawAnimationStartMillis = 0L;
        } else if (freeDrawState == null || !freeDrawState.prizes().equals(state.prizes())) {
            freeDrawAnimationStartMillis = SystemClock.uptimeMillis();
        }
        freeDrawState = state;
        invalidate();
    }

    void setGoldMembershipCardsState(GoldMembershipCardsState state) { goldMembershipPanel.setState(state); invalidate(); }

    void setGoldMembershipCardsLoading(boolean loading) { goldMembershipPanel.setLoading(loading); invalidate(); }

    void setGoldMembershipCardsError(String message) { goldMembershipPanel.setError(message); invalidate(); }

    void updateGoldMembershipCard(GoldMembershipCardsState.Card card) { goldMembershipPanel.updateCard(card); invalidate(); }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.argb(178, 0, 0, 0));
        if (getWidth() <= 0 || getHeight() <= 0) return;
        AdaptiveViewport.Transform transform =
                adaptiveViewport(
                                LobbyActivityCenterLayout.DESIGN_WIDTH,
                                LobbyActivityCenterLayout.DESIGN_HEIGHT)
                        .designTransform();
        int save = AdaptiveCanvasDrawing.apply(canvas, transform);
        chrome.draw(canvas, OriginalActivityCenterChrome.SelectedTab.ACTIVITY, true);
        for (int index = 0; index < items.size(); index++) {
            chrome.drawRow(
                    canvas,
                    LobbyActivityCenterLayout.row(index, 0f),
                    items.get(index).title(),
                    index == selectedIndex,
                    textPaint);
        }
        canvas.drawBitmap(
                loginGiftBadge,
                null,
                new RectF(
                        LobbyActivityCenterLayout.LOGIN_GIFT_BADGE.left(),
                        LobbyActivityCenterLayout.LOGIN_GIFT_BADGE.top(),
                        LobbyActivityCenterLayout.LOGIN_GIFT_BADGE.right(),
                        LobbyActivityCenterLayout.LOGIN_GIFT_BADGE.bottom()),
                bitmapPaint);
        drawSelectedPreview(canvas);
        canvas.restoreToCount(save);
    }

    private void drawSelectedPreview(Canvas canvas) {
        LobbyActivityCenterModel.Destination destination =
                items.get(selectedIndex).destination();
        if (destination == LobbyActivityCenterModel.Destination.FREE_DRAW) {
            drawPreview(canvas, freeDrawPreview, LobbyActivityCenterLayout.FREE_DRAW_CONTENT, true);
            long elapsedMillis =
                    freeDrawAnimationStartMillis == 0L
                            ? 0L
                            : SystemClock.uptimeMillis() - freeDrawAnimationStartMillis;
            freeDrawOverlay.draw(
                    canvas,
                    LobbyActivityCenterLayout.FREE_DRAW_CONTENT,
                    freeDrawState,
                    elapsedMillis);
            if (freeDrawState != null
                    && FreeDrawPanelOverlay.shouldAnimate(freeDrawState.prizes().size())) {
                postInvalidateOnAnimation();
            }
            return;
        }
        if (destination == LobbyActivityCenterModel.Destination.MEMBERSHIP_GIFT) {
            goldMembershipPanel.draw(canvas); return;
        }
        if (destination == LobbyActivityCenterModel.Destination.LOGIN_GIFT) {
            if (loginGiftPanel.draw(canvas, LobbyActivityCenterLayout.ACTIVITY_CONTENT,
                    SystemClock.uptimeMillis() / 1000f)) postInvalidateOnAnimation();
        }
    }

    private void drawPreview(
            Canvas canvas, Bitmap preview, AdaptiveViewport.Rect bounds, boolean fillBounds) {
        float scale = Math.min(bounds.width() / preview.getWidth(), bounds.height() / preview.getHeight());
        float width = fillBounds ? bounds.width() : preview.getWidth() * scale;
        float height = fillBounds ? bounds.height() : preview.getHeight() * scale;
        RectF destination =
                new RectF(
                        bounds.centerX() - width * 0.5f,
                        bounds.centerY() - height * 0.5f,
                        bounds.centerX() + width * 0.5f,
                        bounds.centerY() + height * 0.5f);
        int save = canvas.save();
        canvas.clipRect(bounds.left(), bounds.top(), bounds.right(), bounds.bottom());
        bitmapPaint.setAlpha(pressed.type() == TargetType.CONTENT ? 220 : 255);
        canvas.drawBitmap(preview, null, destination, bitmapPaint);
        bitmapPaint.setAlpha(255);
        canvas.restoreToCount(save);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) return false;
        AdaptiveViewport.Transform transform =
                adaptiveViewport(
                                LobbyActivityCenterLayout.DESIGN_WIDTH,
                                LobbyActivityCenterLayout.DESIGN_HEIGHT)
                        .designTransform();
        float x = transform.unmapX(event.getX());
        float y = transform.unmapY(event.getY());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                pressed = targetAt(x, y);
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                clearPressed();
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                Target released = targetAt(x, y);
                Target started = pressed;
                clearPressed();
                if (started.equals(released)) activate(released);
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    private Target targetAt(float x, float y) {
        if (LobbyActivityCenterLayout.closeContains(x, y)) {
            return new Target(TargetType.CLOSE, -1);
        }
        if (LobbyActivityCenterLayout.sectionAt(x, y)
                == LobbyActivityCenterLayout.Section.ANNOUNCEMENT) {
            return new Target(TargetType.ANNOUNCEMENT, -1);
        }
        if (LobbyActivityCenterLayout.awardCenterContains(x, y)) {
            return new Target(TargetType.AWARD_CENTER, -1);
        }
        int row = LobbyActivityCenterLayout.rowAt(x, y, 0f, items.size());
        if (row >= 0) return new Target(TargetType.ROW, row);
        if (items.get(selectedIndex).destination()
                == LobbyActivityCenterModel.Destination.MEMBERSHIP_GIFT) {
            GoldMembershipCardsPanel.Target goldTarget = goldMembershipPanel.targetAt(x, y);
            return switch (goldTarget.action()) {
                case CLAIM -> new Target(TargetType.CONTENT, goldTarget.cardIndex());
                case OPEN_SHOP -> new Target(TargetType.CONTENT, -1);
                case NONE -> Target.NONE;
            };
        }
        if (items.get(selectedIndex).destination()
                        == LobbyActivityCenterModel.Destination.FREE_DRAW
                && LobbyActivityCenterLayout.freeDrawButtonContains(x, y)) {
            return new Target(TargetType.CONTENT, selectedIndex);
        }
        if (LobbyActivityCenterLayout.contentContains(x, y)) {
            LobbyActivityCenterModel.Destination destination = items.get(selectedIndex).destination();
            if (destination == LobbyActivityCenterModel.Destination.FREE_DRAW
                    || destination == LobbyActivityCenterModel.Destination.LOGIN_GIFT) return Target.NONE;
            return new Target(TargetType.CONTENT, selectedIndex);
        }
        return Target.NONE;
    }

    private void activate(Target target) {
        switch (target.type()) {
            case ROW -> {
                selectedIndex = target.index();
                setContentDescription("活动：" + items.get(selectedIndex).title() + "；可切换公告");
                clicked();
                invalidate();
            }
            case CONTENT -> {
                clicked();
                if (items.get(selectedIndex).destination()
                        == LobbyActivityCenterModel.Destination.MEMBERSHIP_GIFT) {
                    GoldMembershipCardsState.Card card = goldMembershipPanel.cardAt(target.index());
                    if (card == null) {
                        actions.onActivityRequested(LobbyActivityCenterModel.Destination.MEMBERSHIP_GIFT);
                    } else {
                        actions.onGoldMembershipClaim(card.productCode());
                    }
                } else {
                    actions.onActivityRequested(items.get(target.index()).destination());
                }
            }
            case ANNOUNCEMENT -> {
                clicked();
                actions.onAnnouncementRequested();
            }
            case AWARD_CENTER -> {
                clicked();
                actions.onAwardCenterRequested();
            }
            case CLOSE -> {
                clicked();
                actions.onDismissRequested();
            }
            case NONE -> {}
        }
    }

    private void clearPressed() {
        pressed = Target.NONE;
        invalidate();
    }

    private void clicked() {
        performClick();
        buttonClickSound.run();
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private Bitmap bitmap(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }
}
