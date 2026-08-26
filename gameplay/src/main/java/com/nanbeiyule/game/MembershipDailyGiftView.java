package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

/** Canvas recreation of SxvipDailyGiftView.csb / SxvipDailyGiftItem.csb. */
final class MembershipDailyGiftView extends View {
    private static final float DESIGN_WIDTH = 1920.0f;
    private static final float DESIGN_HEIGHT = 1080.0f;
    private static final int TAB_DAILY_GIFT = 0;
    private static final int TAB_ROUND_STATISTICS = 1;
    private static final int TAB_GOLD_STATISTICS = 2;
    private static final int TAB_FRIEND_DATA = 3;
    private static final RectF TIP_BOUNDS = MembershipDailyGiftLayout.offsetY(new RectF(1665.0f, 253.0f, 1713.0f, 303.0f));
    private static final RectF OPEN_BUTTON_BOUNDS = MembershipDailyGiftLayout.offsetY(new RectF(941.5f, 890.0f, 1364.5f, 1011.0f));
    private static final RectF ROUND_STATISTICS_OPEN_MEMBERSHIP_BOUNDS = MembershipRoundStatisticsRenderer.openMembershipButtonBounds();
    private static final RectF GOLD_STATISTICS_OPEN_MEMBERSHIP_BOUNDS = MembershipGoldStatisticsRenderer.openMembershipButtonBounds();
    private static final RectF CLAIM_ONE_BOUNDS = MembershipDailyGiftLayout.offsetY(new RectF(645.0f, 878.0f, 1072.0f, 999.0f));
    private static final RectF CLAIM_TWO_BOUNDS = MembershipDailyGiftLayout.offsetY(new RectF(1237.0f, 878.0f, 1660.0f, 999.0f));
    private static final RectF FIRST_PANEL_BOUNDS = MembershipDailyGiftLayout.offsetY(new RectF(598.22f, 326.53f, 1118.22f, 906.53f));
    private static final RectF SECOND_PANEL_BOUNDS = MembershipDailyGiftLayout.offsetY(new RectF(1179.13f, 326.53f, 1699.13f, 906.53f));
    private static final RectF[] NAVIGATION_TAB_BOUNDS = {new RectF(0.0f, 124.0f, 430.0f, 246.0f),
            new RectF(0.0f, 246.0f, 430.0f, 395.0f), new RectF(0.0f, 395.0f, 430.0f, 544.0f),
            new RectF(0.0f, 544.0f, 430.0f, 693.0f), new RectF(0.0f, 693.0f, 430.0f, 842.0f)};

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private final Runnable closeAction;
    private final MembershipDailyGiftDialog.Actions actions;
    private final Typeface originalTypeface;
    private final Bitmap shopBackgroundBitmap;
    private final Bitmap panelBackgroundBitmap;
    private final Bitmap redPanelBitmap;
    private final Bitmap greenPanelBitmap;
    private final Bitmap rewardCellBitmap;
    private final Bitmap tipButtonBitmap;
    private final Bitmap topOpenButtonBitmap;
    private final Bitmap topBackgroundBitmap;
    private final Bitmap backPanelBitmap;
    private final Bitmap backArrowBitmap;
    private final Bitmap memberTitleBitmap;
    private final Bitmap memberTitleBackgroundBitmap;
    private final Bitmap leftNavigationBaseBitmap;
    private final Bitmap leftNavigationBackgroundBitmap;
    private final Bitmap leftNavigationSelectedBitmap;
    private final Bitmap leftNavigationDividerBitmap;
    private final Bitmap[] navigationTextBitmaps;
    private final Bitmap statisticsBackgroundBitmap;
    private final Bitmap statisticsBottomBackgroundBitmap;
    private final Bitmap statisticsChartBitmap;
    private final Bitmap statisticsBlurBitmap;
    private final Bitmap statisticsOpenBitmap;
    private final Bitmap statisticsInfoBackgroundBitmap;
    private final Bitmap statisticsSelectorBackgroundBitmap;
    private final Bitmap statisticsSelectorArrowBitmap;
    private final Bitmap statisticsQuickRangeBackgroundBitmap;
    private final Bitmap statisticsQuickRangeSelectedBitmap;
    private final Bitmap statisticsRateIconBitmap;
    private final Bitmap statisticsCountIconBitmap;
    private final Bitmap statisticsChampionIconBitmap;
    private final Bitmap statisticsScoreIconBitmap;
    private final Bitmap statisticsStrongestFriendIconBitmap;
    private final Bitmap statisticsFavouriteFriendIconBitmap;
    private final Bitmap goldStatisticsOpenButtonBitmap;
    private final Bitmap goldStatisticsPromptButtonBitmap;
    private final Bitmap[] goldStatisticsBlurBitmaps;
    private final Bitmap openButtonBitmap;
    private final Bitmap redButtonBitmap;
    private final Bitmap greenButtonBitmap;
    private final Bitmap claimedButtonBitmap;
    private final MembershipDailyGiftRewardIconSet rewardIconSet;
    private final MembershipDailyGiftChromeRenderer chromeRenderer;
    private final MembershipRoundStatisticsRenderer roundStatisticsRenderer;
    private final MembershipGoldStatisticsRenderer goldStatisticsRenderer;
    private final MembershipFriendDataRenderer friendDataRenderer;
    private final MembershipDailyGiftOptionRenderer optionRenderer;
    private Runnable buttonClickSound = () -> {};
    private MembershipDailyGiftState state;
    private MembershipGoldStatisticsState goldStatisticsState;
    private boolean loading = true;
    private boolean goldStatisticsLoading;
    private String errorMessage = "";
    private String goldStatisticsErrorMessage = "";
    private int selectedNavigationIndex = TAB_DAILY_GIFT;
    private int roundStatisticsOpenDropdownIndex = MembershipRoundStatisticsRenderer.ROUND_SELECTOR_NONE;
    private int goldStatisticsOpenDropdownIndex = MembershipGoldStatisticsRenderer.GOLD_SELECTOR_NONE;

    MembershipDailyGiftView(Context context, Runnable closeAction, MembershipDailyGiftDialog.Actions actions) {
        super(context);
        this.closeAction = closeAction;
        this.actions = actions;
        originalTypeface = loadOriginalTypeface(context);
        textPaint.setTypeface(originalTypeface);
        chromeRenderer = new MembershipDailyGiftChromeRenderer(originalTypeface);
        roundStatisticsRenderer = new MembershipRoundStatisticsRenderer(originalTypeface);
        goldStatisticsRenderer = new MembershipGoldStatisticsRenderer(originalTypeface);
        friendDataRenderer = new MembershipFriendDataRenderer(originalTypeface);
        optionRenderer = new MembershipDailyGiftOptionRenderer(originalTypeface);
        shopBackgroundBitmap = loadBitmap(R.drawable.shop_new_itemsbg);
        panelBackgroundBitmap = loadBitmap(R.drawable.sxvip_daily_gift_panel_background);
        redPanelBitmap = loadBitmap(R.drawable.sxvip_daily_gift_panel_red);
        greenPanelBitmap = loadBitmap(R.drawable.sxvip_daily_gift_panel_green);
        rewardCellBitmap = loadBitmap(R.drawable.sxvip_daily_gift_reward_cell);
        tipButtonBitmap = loadBitmap(R.drawable.sxvip_daily_gift_tip_button);
        topOpenButtonBitmap = loadBitmap(R.drawable.sxvip_open);
        topBackgroundBitmap = loadBitmap(R.drawable.shop_new_bgdi);
        backPanelBitmap = loadBitmap(R.drawable.shop_new_backdi);
        backArrowBitmap = loadBitmap(R.drawable.shop_new_back);
        memberTitleBitmap = loadBitmap(R.drawable.sxvip_img_title);
        memberTitleBackgroundBitmap = loadBitmap(R.drawable.shop_new_titlebg);
        leftNavigationBaseBitmap = loadBitmap(R.drawable.shop_new_leftdi);
        leftNavigationBackgroundBitmap = loadBitmap(R.drawable.shop_new_leftbgdi);
        leftNavigationSelectedBitmap = loadBitmap(R.drawable.shop_new_chose);
        leftNavigationDividerBitmap = loadBitmap(R.drawable.shop_new_cut);
        navigationTextBitmaps = new Bitmap[] {
                loadBitmap(R.drawable.sxvip_nav_text_daily_gift),
                loadBitmap(R.drawable.sxvip_nav_text_round_stats),
                loadBitmap(R.drawable.sxvip_nav_text_gold_stats),
                loadBitmap(R.drawable.sxvip_nav_text_friend_data),
                loadBitmap(R.drawable.sxvip_nav_text_feedback)
        };
        statisticsBackgroundBitmap = loadBitmap(R.drawable.sxvips_img_dabj);
        statisticsBottomBackgroundBitmap = loadBitmap(R.drawable.sxvips_img_xiafangdi);
        statisticsChartBitmap = loadBitmap(R.drawable.sxvips_shenglv);
        statisticsBlurBitmap = loadBitmap(R.drawable.sxvips_img_mohu);
        statisticsOpenBitmap = loadBitmap(R.drawable.sxvips_open);
        statisticsInfoBackgroundBitmap = loadBitmap(R.drawable.sxvips_img_shenlvdi);
        statisticsSelectorBackgroundBitmap = loadBitmap(R.drawable.sxvips_img_shijiandi);
        statisticsSelectorArrowBitmap = loadBitmap(R.drawable.sxvips_btn_sanjiao);
        statisticsQuickRangeBackgroundBitmap = loadBitmap(R.drawable.sxvips_btn_shijiandi);
        statisticsQuickRangeSelectedBitmap = loadBitmap(R.drawable.sxvips_btn_shijianxz);
        statisticsRateIconBitmap = loadBitmap(R.drawable.sxvips_icon_shenlv);
        statisticsCountIconBitmap = loadBitmap(R.drawable.sxvips_icon_changshu);
        statisticsChampionIconBitmap = loadBitmap(R.drawable.sxvips_icon_guanjun);
        statisticsScoreIconBitmap = loadBitmap(R.drawable.sxvips_icon_yousheng);
        statisticsStrongestFriendIconBitmap = loadBitmap(R.drawable.sxvips_icon_zuiqiang);
        statisticsFavouriteFriendIconBitmap = loadBitmap(R.drawable.sxvips_icon_zuixiang);
        goldStatisticsOpenButtonBitmap = loadBitmap(R.drawable.gold_statistics_btn_kthy_gs);
        goldStatisticsPromptButtonBitmap = loadBitmap(R.drawable.gold_statistics_img_qp);
        goldStatisticsBlurBitmaps = new Bitmap[] {loadBitmap(R.drawable.gold_statistics_img_blur_1),
                loadBitmap(R.drawable.gold_statistics_img_blur_2), loadBitmap(R.drawable.gold_statistics_img_blur_3),
                loadBitmap(R.drawable.gold_statistics_img_blur_4)};
        openButtonBitmap = loadBitmap(R.drawable.sxvip_daily_gift_button_open);
        redButtonBitmap = loadBitmap(R.drawable.sxvip_daily_gift_button_red);
        greenButtonBitmap = loadBitmap(R.drawable.sxvip_daily_gift_button_green);
        claimedButtonBitmap = loadBitmap(R.drawable.sxvip_daily_gift_button_claimed);
        rewardIconSet = MembershipDailyGiftRewardIconSet.load(getResources());
    }

    void setState(MembershipDailyGiftState state) {
        this.state = state;
        loading = false;
        errorMessage = "";
        invalidate();
    }

    void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
            errorMessage = "";
        }
        invalidate();
    }

    void setError(String message) {
        loading = false;
        errorMessage = message == null ? "" : message;
        invalidate();
    }

    void setGoldStatisticsState(MembershipGoldStatisticsState state) {
        goldStatisticsState = state;
        goldStatisticsLoading = false;
        goldStatisticsErrorMessage = "";
        invalidate();
    }

    void setGoldStatisticsLoading(boolean loading) {
        goldStatisticsLoading = loading;
        if (loading) {
            goldStatisticsErrorMessage = "";
        }
        invalidate();
    }

    void setGoldStatisticsError(String message) {
        goldStatisticsLoading = false;
        goldStatisticsErrorMessage = message == null ? "" : message;
        invalidate();
    }

    void setButtonClickSound(Runnable buttonClickSound) {
        this.buttonClickSound = buttonClickSound == null ? () -> {} : buttonClickSound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float scaleX = getWidth() / DESIGN_WIDTH;
        float scaleY = getHeight() / DESIGN_HEIGHT;
        canvas.save();
        canvas.scale(scaleX, scaleY);
        drawPage(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return true;
        }
        float scaleX = getWidth() / DESIGN_WIDTH;
        float scaleY = getHeight() / DESIGN_HEIGHT;
        float x = event.getX() / scaleX;
        float y = event.getY() / scaleY;
        performClick();
        if (x < 112.0f && y < 112.0f) {
            buttonClickSound.run();
            closeAction.run();
        } else {
            int tabIndex = navigationTabIndexAt(x, y);
            if (tabIndex >= 0) {
                buttonClickSound.run();
                selectedNavigationIndex = tabIndex;
                roundStatisticsOpenDropdownIndex = MembershipRoundStatisticsRenderer.ROUND_SELECTOR_NONE;
                goldStatisticsOpenDropdownIndex = MembershipGoldStatisticsRenderer.GOLD_SELECTOR_NONE;
                if (selectedNavigationIndex == TAB_GOLD_STATISTICS) {
                    actions.onGoldStatisticsSelected();
                }
                invalidate();
                return true;
            }
            handlePageTouch(x, y);
        }
        return true;
    }

    private void handlePageTouch(float x, float y) {
        if (selectedNavigationIndex == TAB_GOLD_STATISTICS) {
            handleGoldStatisticsTouch(x, y);
        } else if (selectedNavigationIndex == TAB_ROUND_STATISTICS) {
            handleRoundStatisticsTouch(x, y);
        } else if (selectedNavigationIndex == TAB_FRIEND_DATA) {
            handleFriendDataTouch(x, y);
        } else if (TIP_BOUNDS.contains(x, y)) {
            buttonClickSound.run();
            actions.onTip();
        } else if (OPEN_BUTTON_BOUNDS.contains(x, y) && shouldShowOpenButton()) {
            buttonClickSound.run();
            actions.onOpenMembership();
        } else if (CLAIM_ONE_BOUNDS.contains(x, y) && canClaim()) {
            buttonClickSound.run();
            actions.onClaimGift(1);
        } else if (CLAIM_TWO_BOUNDS.contains(x, y) && canClaim()) {
            buttonClickSound.run();
            actions.onClaimGift(2);
        }
    }

    private void handleRoundStatisticsTouch(float x, float y) {
        int tappedSelector = MembershipRoundStatisticsRenderer.selectorIndexAt(x, y);
        if (tappedSelector != MembershipRoundStatisticsRenderer.ROUND_SELECTOR_NONE) {
            buttonClickSound.run();
            if (shouldShowOpenButton()) {
                actions.onOpenMembership();
                return;
            }
            roundStatisticsOpenDropdownIndex = tappedSelector == roundStatisticsOpenDropdownIndex
                    ? MembershipRoundStatisticsRenderer.ROUND_SELECTOR_NONE : tappedSelector;
            invalidate();
            return;
        }
        roundStatisticsOpenDropdownIndex = MembershipRoundStatisticsRenderer.ROUND_SELECTOR_NONE;
        if (ROUND_STATISTICS_OPEN_MEMBERSHIP_BOUNDS.contains(x, y)) {
            buttonClickSound.run();
            actions.onOpenMembership();
        }
    }

    private void handleFriendDataTouch(float x, float y) {
        if (MembershipFriendDataRenderer.openMembershipTouchBounds().contains(x, y)) {
            buttonClickSound.run();
            actions.onOpenMembership();
        }
    }

    private void handleGoldStatisticsTouch(float x, float y) {
        if (MembershipGoldStatisticsRenderer.gameplaySelectorContains(x, y)) {
            buttonClickSound.run();
            goldStatisticsOpenDropdownIndex = goldStatisticsOpenDropdownIndex == MembershipGoldStatisticsRenderer.GOLD_SELECTOR_GAME_PLAY ? MembershipGoldStatisticsRenderer.GOLD_SELECTOR_NONE : MembershipGoldStatisticsRenderer.GOLD_SELECTOR_GAME_PLAY;
            invalidate();
            return;
        }
        if (goldStatisticsOpenDropdownIndex == MembershipGoldStatisticsRenderer.GOLD_SELECTOR_GAME_PLAY && MembershipGoldStatisticsRenderer.gameplayDropdownContains(x, y)) {
            buttonClickSound.run();
            goldStatisticsOpenDropdownIndex = MembershipGoldStatisticsRenderer.GOLD_SELECTOR_NONE;
            invalidate();
            return;
        }
        goldStatisticsOpenDropdownIndex = MembershipGoldStatisticsRenderer.GOLD_SELECTOR_NONE;
        if (GOLD_STATISTICS_OPEN_MEMBERSHIP_BOUNDS.contains(x, y)) {
            buttonClickSound.run();
            actions.onOpenMembership();
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void drawPage(Canvas canvas) {
        drawDailyGiftOpaqueBase(canvas);
        if (selectedNavigationIndex == TAB_GOLD_STATISTICS) {
            drawGoldStatisticsPage(canvas);
        } else if (selectedNavigationIndex == TAB_ROUND_STATISTICS) {
            drawRoundStatisticsPage(canvas);
        } else if (selectedNavigationIndex == TAB_FRIEND_DATA) {
            drawFriendDataPage(canvas);
        } else {
            drawDailyGiftPanelForeground(canvas);
            drawGiftOptions(canvas);
            drawStatus(canvas);
        }
    }

    private void drawDailyGiftOpaqueBase(Canvas canvas) {
        chromeRenderer.drawBaseLayer(canvas, topBackgroundBitmap);
    }

    private void drawDailyGiftPanelForeground(Canvas canvas) {
        chromeRenderer.drawPanelBackgroundLayer(canvas, shopBackgroundBitmap, panelBackgroundBitmap);
        drawChromeForeground(canvas);
    }

    private void drawRoundStatisticsPage(Canvas canvas) {
        roundStatisticsRenderer.draw(canvas, statisticsBackgroundBitmap, statisticsBottomBackgroundBitmap,
                statisticsChartBitmap, statisticsBlurBitmap, statisticsOpenBitmap,
                statisticsInfoBackgroundBitmap, statisticsSelectorBackgroundBitmap,
                statisticsSelectorArrowBitmap, statisticsQuickRangeBackgroundBitmap,
                statisticsQuickRangeSelectedBitmap, statisticsRateIconBitmap, statisticsCountIconBitmap,
                statisticsChampionIconBitmap, statisticsScoreIconBitmap,
                statisticsStrongestFriendIconBitmap, statisticsFavouriteFriendIconBitmap,
                roundStatisticsOpenDropdownIndex);
        drawChromeForeground(canvas);
    }

    private void drawFriendDataPage(Canvas canvas) {
        friendDataRenderer.draw(canvas);
        drawChromeForeground(canvas);
    }

    private void drawGoldStatisticsPage(Canvas canvas) {
        goldStatisticsRenderer.draw(canvas, goldStatisticsState, goldStatisticsLoading,
                goldStatisticsErrorMessage, statisticsBackgroundBitmap,
                statisticsSelectorBackgroundBitmap, statisticsSelectorArrowBitmap,
                goldStatisticsOpenButtonBitmap, goldStatisticsPromptButtonBitmap,
                goldStatisticsBlurBitmaps, goldStatisticsOpenDropdownIndex);
        drawChromeForeground(canvas);
    }

    private void drawChromeForeground(Canvas canvas) {
        chromeRenderer.drawForegroundLayer(canvas, backPanelBitmap, backArrowBitmap, memberTitleBitmap, memberTitleBackgroundBitmap,
                tipButtonBitmap, topOpenButtonBitmap, leftNavigationBaseBitmap, leftNavigationBackgroundBitmap,
                leftNavigationSelectedBitmap, leftNavigationDividerBitmap, navigationTextBitmaps, selectedNavigationIndex);
    }

    private void drawGiftOptions(Canvas canvas) {
        MembershipDailyGiftState.Option first = optionWithOriginalFallback(1);
        MembershipDailyGiftState.Option second = optionWithOriginalFallback(2);
        optionRenderer.drawGiftPanel(canvas, first, FIRST_PANEL_BOUNDS, redPanelBitmap,
                rewardCellBitmap, rewardIconSet);
        optionRenderer.drawGiftPanel(canvas, second, SECOND_PANEL_BOUNDS, greenPanelBitmap,
                rewardCellBitmap, rewardIconSet);
        if (loading && state == null || shouldShowOpenButton()) {
            drawOpenButton(canvas);
        } else if (loading) {
            drawBitmap(canvas, redButtonBitmap, CLAIM_ONE_BOUNDS);
            drawBitmap(canvas, greenButtonBitmap, CLAIM_TWO_BOUNDS);
            drawButtonLabel(canvas, "领取中", CLAIM_ONE_BOUNDS);
            drawButtonLabel(canvas, "领取中", CLAIM_TWO_BOUNDS);
        } else if (canClaim()) {
            drawBitmap(canvas, redButtonBitmap, CLAIM_ONE_BOUNDS);
            drawBitmap(canvas, greenButtonBitmap, CLAIM_TWO_BOUNDS);
            drawButtonLabel(canvas, "领取礼包1", CLAIM_ONE_BOUNDS);
            drawButtonLabel(canvas, "领取礼包2", CLAIM_TWO_BOUNDS);
        } else {
            RectF claimed = state != null && state.claimedGiftId() == 2 ? CLAIM_TWO_BOUNDS : CLAIM_ONE_BOUNDS;
            drawBitmap(canvas, claimedButtonBitmap, claimed);
            drawButtonLabel(canvas, "已领取", claimed);
        }
    }

    private int navigationTabIndexAt(float x, float y) {
        for (int index = 0; index < NAVIGATION_TAB_BOUNDS.length; index++) {
            if (NAVIGATION_TAB_BOUNDS[index].contains(x, y)) {
                return index;
            }
        }
        return -1;
    }

    private void drawStatus(Canvas canvas) {
        if (loading) {
            drawText(canvas, "礼包数据加载中...", 1156.0f, MembershipDailyGiftLayout.offsetY(845.0f), 34.0f,
                    Color.rgb(126, 74, 35), Paint.Align.CENTER);
        } else if (!errorMessage.isBlank()) {
            drawText(canvas, errorMessage, 1156.0f, MembershipDailyGiftLayout.offsetY(845.0f), 34.0f,
                    Color.rgb(190, 70, 45), Paint.Align.CENTER);
        }
    }

    private MembershipDailyGiftState.Option optionWithOriginalFallback(int giftId) {
        return MembershipDailyGiftOptionFallback.withOriginalRewards(
                state == null ? null : state.option(giftId), giftId);
    }

    private void drawOpenButton(Canvas canvas) {
        drawBitmap(canvas, openButtonBitmap, OPEN_BUTTON_BOUNDS);
    }

    private boolean shouldShowOpenButton() {
        return state == null || !state.membershipActive();
    }

    private boolean canClaim() {
        return state != null && state.membershipActive() && !state.claimedToday() && !loading;
    }

    private void drawButtonLabel(Canvas canvas, String text, RectF bounds) {
        drawStrokeText(canvas, text, bounds.centerX(), bounds.centerY() + 14.0f, 38.0f,
                Color.WHITE, Color.rgb(107, 72, 39), 2.0f);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF bounds) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destination.set(bounds);
        paint.setShader(null);
        paint.setAlpha(255);
        canvas.drawBitmap(bitmap, source, destination, paint);
    }

    private void drawText(
            Canvas canvas, String text, float x, float baseline, float size, int color, Paint.Align align) {
        textPaint.setTypeface(originalTypeface);
        textPaint.setTextSize(size);
        textPaint.setTextAlign(align);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(color);
        textPaint.clearShadowLayer();
        canvas.drawText(text, x, baseline, textPaint);
    }

    private void drawStrokeText(
            Canvas canvas, String text, float x, float baseline, float size,
            int fillColor, int strokeColor, float strokeWidth) {
        textPaint.setTypeface(originalTypeface);
        textPaint.setTextSize(size);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(strokeWidth);
        textPaint.setColor(strokeColor);
        textPaint.setShadowLayer(4.0f, 1.0f, 2.0f, Color.argb(150, 109, 68, 31));
        canvas.drawText(text, x, baseline, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(0.0f);
        textPaint.setColor(fillColor);
        canvas.drawText(text, x, baseline, textPaint);
        textPaint.clearShadowLayer();
    }

    private Bitmap loadBitmap(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }

    private static Typeface loadOriginalTypeface(Context context) {
        try {
            return Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        } catch (RuntimeException exception) {
            return Typeface.DEFAULT_BOLD;
        }
    }
}
