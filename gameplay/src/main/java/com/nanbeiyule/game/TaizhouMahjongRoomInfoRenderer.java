package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.BatteryManager;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMahjongRoomInfoLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Draws RoomInfoLayerMajiang.csb and the evidenced part of RoomCenterInfoLayer.csb. */
final class TaizhouMahjongRoomInfoRenderer {
    private static final String OK_GESTURE = "\uD83D\uDC4C";
    private final Bitmap systemBackground;
    private final Bitmap wifi;
    private final Bitmap powerBackground;
    private final Bitmap powerBar;
    private final Bitmap roomBackground;
    private final Bitmap centerRoomLabel;
    private final Bitmap healthGame;
    private final SxvipBitmapFont roomNumberFont;
    private final BatteryManager batteryManager;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint batteryPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint rulePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint gesturePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

    TaizhouMahjongRoomInfoRenderer(Context context, Bitmap gameLayerAtlas) {
        Bitmap commonAtlas =
                BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.taizhou_mahjong_common_game_layer);
        systemBackground = extract(commonAtlas, TaizhouMahjongRoomInfoLayout.SYSTEM_BACKGROUND);
        wifi = extract(commonAtlas, TaizhouMahjongRoomInfoLayout.WIFI);
        powerBackground = extract(commonAtlas, TaizhouMahjongRoomInfoLayout.POWER_BACKGROUND);
        powerBar = extract(commonAtlas, TaizhouMahjongRoomInfoLayout.POWER_BAR);
        roomBackground = extract(commonAtlas, TaizhouMahjongRoomInfoLayout.ROOM_BACKGROUND);
        centerRoomLabel =
                TaizhouMahjongGameLayerBitmap.extract(
                        gameLayerAtlas, TaizhouMahjongRoomInfoLayout.CENTER_ROOM_LABEL.frameName);
        healthGame =
                TaizhouMahjongGameLayerBitmap.extract(
                        gameLayerAtlas, TaizhouMahjongRoomInfoLayout.HEALTH_GAME.frameName);
        roomNumberFont =
                SxvipBitmapFont.load(
                        context.getResources(), "taizhou-mahjong/room_number-export.fnt");
        batteryManager =
                (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        Typeface typeface =
                Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        textPaint.setTypeface(typeface);
        textPaint.setTextSize(TaizhouMahjongRoomInfoLayout.LEFT_TEXT_SIZE);
        textPaint.setColor(TaizhouMahjongRoomInfoLayout.LEFT_TEXT_COLOR);
        rulePaint.setTypeface(typeface);
        rulePaint.setTextSize(TaizhouMahjongRoomInfoLayout.CENTER_RULE_TEXT_SIZE);
        rulePaint.setColor(TaizhouMahjongRoomInfoLayout.CENTER_RULE_COLOR);
        rulePaint.setTextAlign(Paint.Align.CENTER);
        gesturePaint.setTextSize(TaizhouMahjongRoomInfoLayout.OK_GESTURE_TEXT_SIZE);
        gesturePaint.setTextAlign(Paint.Align.CENTER);
    }

    void draw(Canvas canvas, GameplayTableState state) {
        drawSprite(canvas, systemBackground, TaizhouMahjongRoomInfoLayout.SYSTEM_BACKGROUND);
        drawCenteredText(
                canvas,
                timeFormat.format(new Date()),
                TaizhouMahjongRoomInfoLayout.TIME_CENTER_X,
                TaizhouMahjongRoomInfoLayout.TIME_CENTER_Y,
                Paint.Align.CENTER);
        drawSprite(canvas, wifi, TaizhouMahjongRoomInfoLayout.WIFI);
        drawSprite(canvas, powerBackground, TaizhouMahjongRoomInfoLayout.POWER_BACKGROUND);
        drawBattery(canvas, currentBatteryPercent());

        drawSprite(canvas, roomBackground, TaizhouMahjongRoomInfoLayout.ROOM_BACKGROUND);
        drawRoomRow(canvas, "房间号", state.roomNumber(), TaizhouMahjongRoomInfoLayout.ROOM_ROW_CENTER_Y);
        // TaiZhouMahjong/View.luac suppresses generic play-count updates. Only
        // the original leftBanker message may replace this waiting value: four
        // chairs render "N庄" (showLeftBankerCount), otherwise "N局" (showLeftJuCount).
        drawRoomRow(
                canvas,
                "剩    余",
                TaizhouTableInfoState.remainingText(state.leftBankerCount(), state.chairCount()),
                TaizhouMahjongRoomInfoLayout.REMAINING_ROW_CENTER_Y);

        if (state.roundNumber() == 0) {
            drawSprite(canvas, centerRoomLabel, TaizhouMahjongRoomInfoLayout.CENTER_ROOM_LABEL);
            roomNumberFont.drawCentered(
                    canvas,
                    state.roomNumber(),
                    TaizhouMahjongRoomInfoLayout.CENTER_ROOM_NUMBER_X,
                    TaizhouMahjongRoomInfoLayout.CENTER_ROOM_NUMBER_Y);
        }
        drawGameRule(canvas, state.gameRuleDisplay());
        drawSprite(canvas, healthGame, TaizhouMahjongRoomInfoLayout.HEALTH_GAME);
        if (state.roundNumber() == 0) {
            drawOkGesture(canvas);
        }
    }

    private void drawOkGesture(Canvas canvas) {
        Paint.FontMetrics metrics = gesturePaint.getFontMetrics();
        float baseline =
                TaizhouMahjongRoomInfoLayout.OK_GESTURE_CENTER_Y
                        - (metrics.ascent + metrics.descent) / 2.0f;
        canvas.drawText(
                OK_GESTURE,
                TaizhouMahjongRoomInfoLayout.OK_GESTURE_CENTER_X,
                baseline,
                gesturePaint);
    }

    static String waitingRemainingText() {
        return "-/-";
    }

    static int batteryColor(int percentage) {
        return percentage >= 20 ? 0xffd4981c : 0xffd41c22;
    }

    static float fittedRuleTextSize(float measuredWidth) {
        float originalSize = TaizhouMahjongRoomInfoLayout.CENTER_RULE_TEXT_SIZE;
        if (measuredWidth <= TaizhouMahjongRoomInfoLayout.CENTER_RULE_WIDTH) {
            return originalSize;
        }
        return originalSize
                * TaizhouMahjongRoomInfoLayout.CENTER_RULE_WIDTH
                / measuredWidth;
    }

    private void drawGameRule(Canvas canvas, String gameRuleDisplay) {
        if (gameRuleDisplay.isBlank()) {
            return;
        }
        String ruleText = "台州麻将:" + gameRuleDisplay;
        rulePaint.setTextSize(TaizhouMahjongRoomInfoLayout.CENTER_RULE_TEXT_SIZE);
        rulePaint.setTextSize(fittedRuleTextSize(rulePaint.measureText(ruleText)));
        float centerY = TaizhouMahjongRoomInfoLayout.CENTER_RULE_Y;
        Paint.FontMetrics metrics = rulePaint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) / 2.0f;
        int save = canvas.save();
        canvas.clipRect(
                TaizhouMahjongRoomInfoLayout.CENTER_RULE_X
                        - TaizhouMahjongRoomInfoLayout.CENTER_RULE_WIDTH / 2.0f,
                centerY - TaizhouMahjongRoomInfoLayout.CENTER_RULE_HEIGHT / 2.0f,
                TaizhouMahjongRoomInfoLayout.CENTER_RULE_X
                        + TaizhouMahjongRoomInfoLayout.CENTER_RULE_WIDTH / 2.0f,
                centerY + TaizhouMahjongRoomInfoLayout.CENTER_RULE_HEIGHT / 2.0f);
        canvas.drawText(
                ruleText,
                TaizhouMahjongRoomInfoLayout.CENTER_RULE_X,
                baseline,
                rulePaint);
        canvas.restoreToCount(save);
    }

    private void drawBattery(Canvas canvas, int percentage) {
        if (percentage < 0) {
            return;
        }
        TaizhouMahjongRoomInfoLayout.Sprite sprite = TaizhouMahjongRoomInfoLayout.POWER_BAR;
        RectF destination = bounds(sprite);
        float visibleRight = destination.left + destination.width() * percentage / 100.0f;
        int save = canvas.save();
        canvas.clipRect(destination.left, destination.top, visibleRight, destination.bottom);
        batteryPaint.setColorFilter(
                new PorterDuffColorFilter(batteryColor(percentage), PorterDuff.Mode.MULTIPLY));
        canvas.drawBitmap(powerBar, null, destination, batteryPaint);
        canvas.restoreToCount(save);
    }

    private int currentBatteryPercent() {
        if (batteryManager == null) {
            return -1;
        }
        int value = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return value < 0 ? -1 : Math.min(100, value);
    }

    private void drawRoomRow(Canvas canvas, String key, String value, float centerY) {
        drawCenteredText(canvas, key, TaizhouMahjongRoomInfoLayout.KEY_RIGHT_X, centerY, Paint.Align.RIGHT);
        drawCenteredText(canvas, ":", TaizhouMahjongRoomInfoLayout.COLON_CENTER_X, centerY, Paint.Align.CENTER);
        drawCenteredText(canvas, value, TaizhouMahjongRoomInfoLayout.VALUE_LEFT_X, centerY, Paint.Align.LEFT);
    }

    private void drawCenteredText(
            Canvas canvas, String value, float x, float centerY, Paint.Align align) {
        textPaint.setTextAlign(align);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(value, x, centerY - (metrics.ascent + metrics.descent) / 2.0f, textPaint);
    }

    private void drawSprite(
            Canvas canvas, Bitmap bitmap, TaizhouMahjongRoomInfoLayout.Sprite sprite) {
        canvas.drawBitmap(bitmap, null, bounds(sprite), bitmapPaint);
    }

    private static Bitmap extract(
            Bitmap atlas, TaizhouMahjongRoomInfoLayout.Sprite sprite) {
        return TaizhouMahjongCommonBitmap.extract(atlas, sprite.frameName);
    }

    private static RectF bounds(TaizhouMahjongRoomInfoLayout.Sprite sprite) {
        return new RectF(
                sprite.centerX - sprite.width / 2.0f,
                sprite.centerY - sprite.height / 2.0f,
                sprite.centerX + sprite.width / 2.0f,
                sprite.centerY + sprite.height / 2.0f);
    }
}
