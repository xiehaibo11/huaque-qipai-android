package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import com.nanbeiyule.game.goldroom.GoldChooseRoomLayout;
import com.nanbeiyule.game.goldroom.GoldRoomLevel;
import com.nanbeiyule.game.goldroom.GoldRoomRibbon;
import java.util.List;

/**
 * Draws the original choose-room cards in 1920x1080 design space.
 *
 * <p>Every rectangle comes from {@link GoldChooseRoomLayout}; every colour and string rule comes
 * from the original ChooseRoom.lua. See
 * android/docs/ORIGINAL-GOLD-CHOOSE-ROOM-EVIDENCE.md.
 */
final class GoldChooseRoomRenderer {
    /** {@code IMG_CFG.color}: the 准入 text colour per level palette. */
    private static final int[] GOLD_LIMIT_COLORS = {
        0xFF6D8F6F, 0xFF4D9DBE, 0xFF896F69, 0xFFA17C41, 0xFFAC6B5C
    };

    private final GoldChooseRoomBitmaps bitmaps;
    private final Resources resources;
    /** One BMFont per level palette, loaded on first use. */
    private final SxvipBitmapFont[] baseScoreFonts = new SxvipBitmapFont[5];

    private final Paint bitmapPaint =
            new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();

    GoldChooseRoomRenderer(GoldChooseRoomBitmaps bitmaps, Resources resources) {
        this.bitmaps = bitmaps;
        this.resources = resources;
        textPaint.setTypeface(
                Typeface.createFromAsset(resources.getAssets(), "fonts/fangzhengcuyuan.ttf"));
        outlinePaint.setStyle(Paint.Style.STROKE);
    }

    /** Draws the backdrop, letting the 2340-wide art overhang the design box symmetrically. */
    void drawBackdrop(Canvas canvas) {
        Bitmap background = drawableBitmap("gold_hall_background");
        if (background != null) {
            float overhang =
                    (background.getWidth() - GoldChooseRoomLayout.DESIGN_WIDTH) / 2.0f;
            drawStretched(
                    canvas,
                    background,
                    -overhang,
                    0,
                    background.getWidth(),
                    GoldChooseRoomLayout.DESIGN_HEIGHT);
        }
        Bitmap foreground = drawableBitmap("gold_hall_foreground");
        if (foreground != null) {
            float overhang =
                    (foreground.getWidth() - GoldChooseRoomLayout.DESIGN_WIDTH) / 2.0f;
            drawStretched(
                    canvas,
                    foreground,
                    -overhang,
                    GoldChooseRoomLayout.DESIGN_HEIGHT - foreground.getHeight(),
                    foreground.getWidth(),
                    foreground.getHeight());
        }
    }

    /** Draws every level card of the row, marking {@code selectedIndex} with the original spine. */
    void drawCards(
            Canvas canvas,
            List<GoldRoomLevel> levels,
            int selectedIndex,
            boolean showsPlayerCount,
            GoldChooseRoomEffects effects,
            float elapsedSeconds) {
        int count = levels.size();
        boolean fallbackHighlight = needsFallbackHighlight(effects);
        for (int index = 0; index < count; index++) {
            float left = GoldChooseRoomLayout.itemLeft(index, count);
            drawCard(
                    canvas,
                    levels.get(index),
                    left,
                    index == selectedIndex,
                    fallbackHighlight,
                    showsPlayerCount);
            drawCardEffects(canvas, effects, elapsedSeconds, left, index == selectedIndex);
        }
    }

    /**
     * {@code ChooseRoom.lua:452-456}: every card carries the ambient skeleton and the selected one
     * adds the highlight skeleton, which the CSB squashes to 0.82 vertically.
     */
    private void drawCardEffects(
            Canvas canvas,
            GoldChooseRoomEffects effects,
            float elapsedSeconds,
            float itemLeft,
            boolean selected) {
        if (effects == null || !effects.available()) {
            return;
        }
        float top = GoldChooseRoomLayout.ITEM_TOP;
        effects.draw(
                canvas,
                GoldChooseRoomEffects.AMBIENT,
                elapsedSeconds,
                itemLeft + GoldChooseRoomLayout.AMBIENT_ANI_X,
                top + GoldChooseRoomLayout.AMBIENT_ANI_Y,
                1.0f,
                1.0f);
        if (!selected) {
            return;
        }
        effects.draw(
                canvas,
                GoldChooseRoomEffects.SELECTED,
                elapsedSeconds,
                itemLeft + GoldChooseRoomLayout.SELECT_ANI_X,
                top + GoldChooseRoomLayout.SELECT_ANI_Y,
                GoldChooseRoomLayout.SELECT_ANI_SCALE_X,
                GoldChooseRoomLayout.SELECT_ANI_SCALE_Y);
    }

    /** True when the fallback stroke should stand in for a missing highlight skeleton. */
    boolean needsFallbackHighlight(GoldChooseRoomEffects effects) {
        return effects == null || !effects.available();
    }

    private void drawCard(
            Canvas canvas,
            GoldRoomLevel level,
            float itemLeft,
            boolean selected,
            boolean fallbackHighlight,
            boolean showsPlayerCount) {
        float top = GoldChooseRoomLayout.ITEM_TOP;
        Bitmap card = bitmaps.chooseRoom("Img_xc_" + level.uiType() + ".png");
        if (card != null) {
            drawStretched(
                    canvas,
                    card,
                    itemLeft + GoldChooseRoomLayout.CARD_CENTER_X
                            - GoldChooseRoomLayout.CARD_WIDTH / 2.0f,
                    top + GoldChooseRoomLayout.CARD_TOP,
                    GoldChooseRoomLayout.CARD_WIDTH,
                    GoldChooseRoomLayout.CARD_HEIGHT);
        }
        if (selected && fallbackHighlight) {
            drawSelectionFrame(canvas, itemLeft, top);
        }
        drawBaseScore(canvas, level, itemLeft, top);
        drawGoldLimit(canvas, level, itemLeft, top);
        if (showsPlayerCount) {
            drawPlayerCount(canvas, level, itemLeft, top);
        }
        drawRibbons(canvas, level, itemLeft, top);
    }

    private void drawPlayerCount(
            Canvas canvas, GoldRoomLevel level, float itemLeft, float top) {
        Bitmap icon = bitmaps.chooseRoom("Img_xc_rs" + level.uiType() + ".png");
        if (icon != null) {
            drawStretched(
                    canvas,
                    icon,
                    itemLeft + GoldChooseRoomLayout.PLAYER_ICON_CENTER_X
                            - GoldChooseRoomLayout.PLAYER_ICON_WIDTH / 2.0f,
                    top + GoldChooseRoomLayout.PLAYER_ICON_CENTER_Y
                            - GoldChooseRoomLayout.PLAYER_ICON_HEIGHT / 2.0f,
                    GoldChooseRoomLayout.PLAYER_ICON_WIDTH,
                    GoldChooseRoomLayout.PLAYER_ICON_HEIGHT);
        }
        textPaint.setColor(goldLimitColor(level.uiType()));
        textPaint.setTextSize(36.0f);
        textPaint.setTextAlign(Paint.Align.LEFT);
        float baseline =
                top
                        + GoldChooseRoomLayout.PLAYER_COUNT_CENTER_Y
                        - (textPaint.descent() + textPaint.ascent()) / 2.0f;
        canvas.drawText(
                Long.toString(level.onlineCount()),
                itemLeft + GoldChooseRoomLayout.PLAYER_COUNT_LEFT + 49.0f,
                baseline,
                textPaint);
    }

    /** {@code _fontBaseScore}: the per-palette BMFont, recentred on the item like the original. */
    private void drawBaseScore(
            Canvas canvas, GoldRoomLevel level, float itemLeft, float top) {
        SxvipBitmapFont font = baseScoreFont(level.uiType());
        if (font == null) {
            return;
        }
        font.drawCentered(
                canvas,
                level.baseScoreText(),
                itemLeft + GoldChooseRoomLayout.ITEM_WIDTH / 2.0f,
                top + GoldChooseRoomLayout.BASE_SCORE_CENTER_Y);
    }

    private SxvipBitmapFont baseScoreFont(int uiType) {
        int index = uiType - 1;
        if (index < 0 || index >= baseScoreFonts.length) {
            index = 0;
        }
        if (baseScoreFonts[index] == null) {
            baseScoreFonts[index] =
                    SxvipBitmapFont.load(
                            resources,
                            "gold_choose_room_fonts/Img_xc_zi" + (index + 1) + "-export.fnt");
        }
        return baseScoreFonts[index];
    }

    /**
     * Placeholder for the {@code zzb_jbdt_tjxf} spine that highlights the chosen card. Until the
     * skeleton is wired the frame is stroked with the ribbon gold so the selection stays visible;
     * this is marked as an approximation rather than原版事实.
     */
    private void drawSelectionFrame(Canvas canvas, float itemLeft, float top) {
        outlinePaint.setColor(0xFFF6D24B);
        outlinePaint.setStrokeWidth(6.0f);
        float left =
                itemLeft + GoldChooseRoomLayout.CARD_CENTER_X
                        - GoldChooseRoomLayout.CARD_WIDTH / 2.0f;
        destination.set(
                left + 26.5f,
                top + GoldChooseRoomLayout.CARD_TOP + 13.0f,
                left + GoldChooseRoomLayout.CARD_WIDTH - 26.5f,
                top + GoldChooseRoomLayout.CARD_TOP + GoldChooseRoomLayout.CARD_HEIGHT - 29.0f);
        canvas.drawRoundRect(destination, 14.0f, 14.0f, outlinePaint);
    }

    private void drawGoldLimit(
            Canvas canvas, GoldRoomLevel level, float itemLeft, float top) {
        textPaint.setColor(goldLimitColor(level.uiType()));
        textPaint.setTextSize(GoldChooseRoomLayout.GOLD_LIMIT_HEIGHT);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        float baseline =
                top
                        + GoldChooseRoomLayout.GOLD_LIMIT_CENTER_Y
                        - (textPaint.descent() + textPaint.ascent()) / 2.0f;
        canvas.drawText(
                level.goldLimitText(),
                itemLeft + GoldChooseRoomLayout.GOLD_LIMIT_RIGHT,
                baseline,
                textPaint);
    }

    private void drawRibbons(Canvas canvas, GoldRoomLevel level, float itemLeft, float top) {
        drawRibbon(
                canvas,
                level.ribbon1(),
                itemLeft,
                top + GoldChooseRoomLayout.RIBBON_1_CENTER_Y);
        drawRibbon(
                canvas,
                level.ribbon2(),
                itemLeft,
                top + GoldChooseRoomLayout.RIBBON_2_CENTER_Y);
    }

    private void drawRibbon(
            Canvas canvas, GoldRoomRibbon ribbon, float itemLeft, float centerY) {
        if (ribbon == null) {
            return;
        }
        Bitmap band = bitmaps.chooseRoom("Img_cd_" + ribbon.type() + ".png");
        float left =
                itemLeft
                        + GoldChooseRoomLayout.RIBBON_CENTER_X
                        - GoldChooseRoomLayout.RIBBON_WIDTH / 2.0f;
        if (band != null) {
            drawStretched(
                    canvas,
                    band,
                    left,
                    centerY - GoldChooseRoomLayout.RIBBON_HEIGHT / 2.0f,
                    GoldChooseRoomLayout.RIBBON_WIDTH,
                    GoldChooseRoomLayout.RIBBON_HEIGHT);
        }
        drawRibbonSegments(canvas, ribbon, left, centerY);
    }

    /** {@code doLayoutHoriz}: inline nodes are spread evenly across the band. */
    private void drawRibbonSegments(
            Canvas canvas, GoldRoomRibbon ribbon, float bandLeft, float centerY) {
        List<GoldRoomRibbon.Segment> segments = ribbon.segments();
        textPaint.setTextSize(44.0f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        outlinePaint.setColor(ribbon.outlineColor());
        outlinePaint.setStrokeWidth(2.0f);
        outlinePaint.setTextSize(44.0f);
        outlinePaint.setTextAlign(Paint.Align.CENTER);
        float step = GoldChooseRoomLayout.RIBBON_WIDTH / (segments.size() + 1.0f);
        float baseline = centerY - (textPaint.descent() + textPaint.ascent()) / 2.0f;
        for (int index = 0; index < segments.size(); index++) {
            GoldRoomRibbon.Segment segment = segments.get(index);
            float centerX = bandLeft + step * (index + 1);
            if (segment.isIcon()) {
                Bitmap icon =
                        bitmaps.main(segment.diamondIcon() ? "Img_ZS.png" : "Img_JB.png");
                if (icon != null) {
                    drawStretched(
                            canvas,
                            icon,
                            centerX - icon.getWidth() / 2.0f,
                            centerY - icon.getHeight() / 2.0f,
                            icon.getWidth(),
                            icon.getHeight());
                }
                continue;
            }
            canvas.drawText(segment.text(), centerX, baseline, outlinePaint);
            textPaint.setColor(segment.color());
            canvas.drawText(segment.text(), centerX, baseline, textPaint);
        }
    }

    private static int goldLimitColor(int uiType) {
        int index = uiType - 1;
        if (index < 0 || index >= GOLD_LIMIT_COLORS.length) {
            index = 0;
        }
        return GOLD_LIMIT_COLORS[index];
    }

    private Bitmap drawableBitmap(String resourceName) {
        return bitmaps.standalone(resourceName);
    }

    private void drawStretched(
            Canvas canvas, Bitmap bitmap, float left, float top, float width, float height) {
        if (bitmap == null || bitmap.isRecycled() || width <= 0 || height <= 0) {
            return;
        }
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destination.set(left, top, left + width, top + height);
        canvas.drawBitmap(bitmap, source, destination, bitmapPaint);
    }
}
