package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.nanbeiyule.game.goldroom.GoldHallActEntry;
import com.nanbeiyule.game.goldroom.GoldHallActEntryGroup;
import com.nanbeiyule.game.goldroom.GoldHallChromeLayout;

/**
 * Draws the gold-hall chrome recovered from {@code GoldLayer.csb}: the top bar (back, game name,
 * rule button, wallet pills, activity entries, settings) and the bottom-right quick-start button.
 *
 * <p>Wallet numbers come from the authenticated game-home state; nothing here invents a balance.
 */
final class GoldHallChromeRenderer {
    private final GoldChooseRoomBitmaps bitmaps;
    private final GoldChooseRoomEffects effects;
    private final Paint bitmapPaint =
            new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();

    private GoldHallActEntryGroup actEntryGroup = GoldHallActEntryGroup.residentOnly();
    private java.util.Set<GoldHallActEntry> redPointEntries = java.util.Set.of();
    private final Bitmap timeLoginIcon;
    /** 原版红点是 NewGoldHall/Pop/_Plist 的 Img_res.png，不在选场页图集里。 */
    private final Bitmap redPointIcon;

    GoldHallChromeRenderer(
            GoldChooseRoomBitmaps bitmaps,
            GoldChooseRoomEffects effects,
            Bitmap timeLoginIcon,
            Bitmap redPointIcon) {
        this.bitmaps = bitmaps;
        this.effects = effects;
        this.timeLoginIcon = timeLoginIcon;
        this.redPointIcon = redPointIcon;
    }

    /** Top bar. {@code wallet} may be null before the home state has loaded. */
    void drawTopBar(
            Canvas canvas, String gameName, GameHomeState.Wallet wallet, float elapsedSeconds) {
        drawFrame(canvas, bitmaps.main("Img_tx_di.png"), -236.0f, 3.0f, 1488.0f, 156.0f);
        drawCentred(
                canvas,
                bitmaps.main("Btn_fanhui.png"),
                GoldHallChromeLayout.BACK_CENTER_X,
                GoldHallChromeLayout.BACK_CENTER_Y,
                72.0f,
                88.0f);
        if (gameName != null && !gameName.isEmpty()) {
            textPaint.setColor(0xFFFFFFFF);
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(GoldHallChromeLayout.TITLE_TEXT_SIZE);
            textPaint.setFakeBoldText(true);
            canvas.drawText(
                    gameName,
                    GoldHallChromeLayout.TITLE_LEFT,
                    baseline(GoldHallChromeLayout.TITLE_CENTER_Y),
                    textPaint);
            textPaint.setFakeBoldText(false);
        }
        drawCentred(
                canvas,
                bitmaps.main("Btn_wenhao.png"),
                GoldHallChromeLayout.RULE_CENTER_X,
                GoldHallChromeLayout.RULE_CENTER_Y,
                GoldHallChromeLayout.RULE_SIZE,
                GoldHallChromeLayout.RULE_SIZE);
        drawWalletPills(canvas, wallet);
        drawTopActivities(canvas, elapsedSeconds);
        drawCentred(
                canvas,
                bitmaps.main("Btn_sz.png"),
                GoldHallChromeLayout.GEAR_CENTER_X,
                GoldHallChromeLayout.GEAR_CENTER_Y,
                GoldHallChromeLayout.GEAR_ICON_WIDTH,
                GoldHallChromeLayout.GEAR_ICON_HEIGHT);
    }

    private void drawWalletPills(Canvas canvas, GameHomeState.Wallet wallet) {
        Bitmap pill = bitmaps.main("Img_di.png");
        drawFrame(
                canvas,
                pill,
                GoldHallChromeLayout.GOLD_PILL_LEFT,
                GoldHallChromeLayout.PILL_TOP,
                GoldHallChromeLayout.PILL_WIDTH,
                GoldHallChromeLayout.PILL_HEIGHT);
        drawFrame(
                canvas,
                pill,
                GoldHallChromeLayout.DIAMOND_PILL_LEFT,
                GoldHallChromeLayout.PILL_TOP,
                GoldHallChromeLayout.PILL_WIDTH,
                GoldHallChromeLayout.PILL_HEIGHT);
        drawCentred(
                canvas,
                bitmaps.main("Img_JB.png"),
                GoldHallChromeLayout.GOLD_ICON_CENTER_X,
                GoldHallChromeLayout.GOLD_ICON_CENTER_Y,
                GoldHallChromeLayout.GOLD_ICON_WIDTH,
                GoldHallChromeLayout.GOLD_ICON_HEIGHT);
        drawCentred(
                canvas,
                bitmaps.main("Img_ZS.png"),
                GoldHallChromeLayout.DIAMOND_ICON_CENTER_X,
                GoldHallChromeLayout.DIAMOND_ICON_CENTER_Y,
                GoldHallChromeLayout.DIAMOND_ICON_WIDTH,
                GoldHallChromeLayout.DIAMOND_ICON_HEIGHT);
        Bitmap plus = bitmaps.main("Img_JH2.png");
        drawCentred(
                canvas,
                plus,
                GoldHallChromeLayout.GOLD_PLUS_CENTER_X,
                GoldHallChromeLayout.GOLD_PLUS_CENTER_Y,
                GoldHallChromeLayout.PLUS_ICON_WIDTH,
                GoldHallChromeLayout.PLUS_ICON_HEIGHT);
        drawCentred(
                canvas,
                plus,
                GoldHallChromeLayout.DIAMOND_PLUS_CENTER_X,
                GoldHallChromeLayout.DIAMOND_PLUS_CENTER_Y,
                GoldHallChromeLayout.PLUS_ICON_WIDTH,
                GoldHallChromeLayout.PLUS_ICON_HEIGHT);
        if (wallet == null) {
            return;
        }
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(GoldHallChromeLayout.WALLET_TEXT_SIZE);
        float textBaseline = baseline(GoldHallChromeLayout.WALLET_TEXT_CENTER_Y);
        canvas.drawText(
                Long.toString(wallet.coins()),
                GoldHallChromeLayout.GOLD_TEXT_CENTER_X,
                textBaseline,
                textPaint);
        canvas.drawText(
                Long.toString(wallet.diamonds()),
                GoldHallChromeLayout.DIAMOND_TEXT_CENTER_X,
                textBaseline,
                textPaint);
    }

    /**
     * {@code _menuBarTopAct} 的活动入口。组是变长的，只画当前可见的那几颗。
     *
     * <p>每个入口按自己的 CSB 摆放：静态图标是 {@code _imgBtn}（157x159 的未裁切框，图集帧再按
     * plist 的裁切位移补正），「福利任务」的 {@code _imgBtn} 在 CSB 里是隐藏的，真正的图标是挂在
     * {@code _aniNode} 上的骨骼。红点由服务端活动状态驱动，接入前一律不画。
     */
    private void drawTopActivities(Canvas canvas, float elapsedSeconds) {
        for (GoldHallActEntry entry : actEntryGroup.visible()) {
            float centerX = actEntryGroup.centerX(entry);
            float centerY = GoldHallChromeLayout.ACT_BUTTON_CENTER_Y;
            String skeleton = entry.spineSkeleton();
            if (skeleton != null) {
                if (effects.has(skeleton)) {
                    effects.draw(
                            canvas,
                            skeleton,
                            elapsedSeconds,
                            centerX,
                            centerY + GoldHallActEntry.ANI_NODE_OFFSET_Y,
                            1.0f,
                            1.0f);
                }
                continue;
            }
            drawCentred(
                    canvas,
                    entry.usesStandaloneIcon()
                            ? standaloneIcon(entry)
                            : bitmaps.actButton(entry.atlasFrame()),
                    centerX + entry.iconOffsetX(),
                    centerY + entry.iconOffsetY(),
                    entry.iconWidth(),
                    entry.iconHeight());
            if (redPointEntries.contains(entry)) {
                // 原版红点挂在按钮面板右上角，Y 在 CSB 里向上，这里取反。
                drawCentred(
                        canvas,
                        redPointIcon,
                        centerX + entry.redPointOffsetX(),
                        centerY - entry.redPointOffsetY(),
                        entry.redPointSize(),
                        entry.redPointSize());
            }
        }
    }

    /** 服务端开关的入口用随其页面一起抽取的独立位图，不在 ActBtns 图集里。 */
    private Bitmap standaloneIcon(GoldHallActEntry entry) {
        if (entry == GoldHallActEntry.ACT_TIME_LOGIN) {
            return timeLoginIcon;
        }
        return null;
    }

    /** 当前活动组构成与红点由外部按服务端状态设置。 */
    void setActEntries(GoldHallActEntryGroup group, java.util.Set<GoldHallActEntry> redPoints) {
        actEntryGroup = group;
        redPointEntries = redPoints;
    }

    GoldHallActEntryGroup actEntryGroup() {
        return actEntryGroup;
    }

    /** Bottom-right quick-start button; {@code subtitle} is "<玩法> <档位>". */
    void drawQuickStart(Canvas canvas, String subtitle) {
        drawCentred(
                canvas,
                bitmaps.main("Btn_anniu.png"),
                GoldHallChromeLayout.QUICK_START_BG_CENTER_X,
                GoldHallChromeLayout.QUICK_START_BG_CENTER_Y,
                GoldHallChromeLayout.QUICK_START_BG_WIDTH,
                GoldHallChromeLayout.QUICK_START_BG_HEIGHT);
        drawCentred(
                canvas,
                bitmaps.main("Img_KS.png"),
                GoldHallChromeLayout.QUICK_START_LABEL_CENTER_X,
                GoldHallChromeLayout.QUICK_START_LABEL_CENTER_Y,
                GoldHallChromeLayout.QUICK_START_LABEL_WIDTH,
                GoldHallChromeLayout.QUICK_START_LABEL_HEIGHT);
        if (subtitle == null || subtitle.isEmpty()) {
            return;
        }
        textPaint.setColor(0xFF7A3B12);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(GoldHallChromeLayout.QUICK_START_NAME_TEXT_SIZE);
        canvas.drawText(
                subtitle,
                GoldHallChromeLayout.QUICK_START_NAME_CENTER_X,
                baseline(GoldHallChromeLayout.QUICK_START_NAME_CENTER_Y),
                textPaint);
    }

    private float baseline(float centerY) {
        return centerY - (textPaint.descent() + textPaint.ascent()) / 2.0f;
    }

    private void drawCentred(
            Canvas canvas, Bitmap bitmap, float centerX, float centerY, float width, float height) {
        drawFrame(canvas, bitmap, centerX - width / 2.0f, centerY - height / 2.0f, width, height);
    }

    private void drawFrame(
            Canvas canvas, Bitmap bitmap, float left, float top, float width, float height) {
        if (bitmap == null || bitmap.isRecycled() || width <= 0 || height <= 0) {
            return;
        }
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destination.set(left, top, left + width, top + height);
        canvas.drawBitmap(bitmap, source, destination, bitmapPaint);
    }
}
