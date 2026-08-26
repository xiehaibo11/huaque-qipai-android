package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

/** The two 900023 remodel rows appended by TeaHouseCreateView.lua to the old setup CSB. */
final class MatchArenaCreateRemodelRenderer {
    private static final long[] VALUES = {100, 500, 1000, 2000};
    private static final float[] OPTION_X = {740, 920, 1100, 1280};
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Typeface typeface;
    private final Bitmap checkbox;
    private final Bitmap checkboxSelected;
    private final Bitmap option;
    private final Bitmap optionSelected;
    private final Bitmap help;
    private final Bitmap bubble;
    private final Bitmap input;

    MatchArenaCreateRemodelRenderer(Context context) {
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        checkbox = bitmap(context, R.drawable.teahouse_setup_checkbox);
        checkboxSelected = bitmap(context, R.drawable.teahouse_setup_checkbox_select);
        option = bitmap(context, R.drawable.tea_house_createroom_check_box1_bg);
        optionSelected = bitmap(context, R.drawable.tea_house_createroom_check_box1);
        help = bitmap(context, R.drawable.teahouse_setup_btn_what);
        bubble = bitmap(context, R.drawable.teahouse_setup_img_bg_bubble1);
        input = bitmap(context, R.drawable.teahouse_setup_input_bg1);
    }

    void draw(
            Canvas canvas,
            MatchArenaCreateState state,
            String error,
            boolean showAutoHelp,
            boolean showReminderHelp,
            boolean showBuyCardHelp,
            boolean showChargeCardHelp,
            boolean errorIsAuto) {
        if (state.mode() == MatchArenaCreateState.Mode.LOBBY_CARD) return;
        drawBaseHelp(canvas, showBuyCardHelp, showChargeCardHelp);
        drawRow(canvas, state, true, 365);
        drawRow(canvas, state, false, 255);
        if (state.autoTransferEnabled()) {
            centered(canvas, "补卡记录前往【更多】-【操作日志】查看", 950.011f, cocosY(67), 28, 0xffb97345);
        }
        if (showAutoHelp) drawAutoHelp(canvas);
        if (showReminderHelp) drawReminderHelp(canvas);
        drawError(canvas, error, errorIsAuto);
    }

    private void drawRow(Canvas canvas, MatchArenaCreateState state, boolean auto, float cocosY) {
        boolean enabled = auto ? state.autoTransferEnabled() : state.lowCardReminderEnabled();
        draw(canvas, checkbox, cocosRect(368, cocosY, 76, 80));
        if (enabled) draw(canvas, checkboxSelected, cocosRect(368, cocosY, 76, 66));
        centered(canvas, auto ? "自动补卡" : "缺卡提醒", 508, cocosY(cocosY), 42, 0xffa36f48);
        draw(canvas, help, cocosRect(640, cocosY, 73, 74));
        if (!enabled) return;
        long selected = auto ? state.selectedAutoTransferAmount() : state.selectedLowCardReminderThreshold();
        boolean custom = auto ? state.autoTransferUsesCustomValue() : state.lowCardReminderUsesCustomValue();
        for (int index = 0; index < OPTION_X.length; index++) {
            draw(canvas, option, cocosRect(OPTION_X[index], cocosY, 80, 80));
            if (!custom && selected == VALUES[index]) {
                draw(canvas, optionSelected, cocosRect(OPTION_X[index], cocosY, 74, 74));
            }
            text(canvas, Long.toString(VALUES[index]), OPTION_X[index] + 40, cocosY(cocosY) + 15, 42, 0xffa36f48);
        }
        draw(canvas, option, cocosRect(1460, cocosY, 80, 80));
        if (custom) draw(canvas, optionSelected, cocosRect(1460, cocosY, 74, 74));
        drawNine(canvas, input, new RectF(1500, cocosY(cocosY) - 36, 1649, cocosY(cocosY) + 36));
        if (!custom) centered(canvas, "自定义", 1574.5f, cocosY(cocosY), 39, 0xfffffaeb);
    }

    private void drawAutoHelp(Canvas canvas) {
        drawCapInsets(canvas, bubble, cocosRect(744.28f, 475, 380, 160), 131, 59, 65, 58);
        centered(canvas, "勾选后,当比赛场内可用库存<50,", 744, cocosY(489), 32, 0xffa36f48);
        centered(canvas, "会自动充入勾选数值房卡", 744, cocosY(451), 32, 0xffa36f48);
    }

    private void drawReminderHelp(Canvas canvas) {
        drawCapInsets(canvas, bubble, cocosRect(744.28f, 355, 380, 140), 131, 59, 65, 58);
        centered(canvas, "当库存不足时,会提醒领队", 744, cocosY(355), 32, 0xffa36f48);
    }

    private void drawError(Canvas canvas, String error, boolean errorIsAuto) {
        if (error == null || !error.startsWith("!")) return;
        text(
                canvas,
                error,
                errorIsAuto ? 422 : 422.5f,
                cocosY(errorIsAuto ? 317 : 207) + 10,
                30,
                0xffc54f30);
    }

    private void drawBaseHelp(Canvas canvas, boolean buy, boolean charge) {
        draw(canvas, help, cocosRect(780, 829, 73, 74));
        draw(canvas, help, cocosRect(1029.5233f, 490, 73, 74));
        if (buy) {
            RectF bounds = cocosRect(883.5f, 957, 380, 190);
            drawCapInsets(canvas, bubble, bounds, 167, 59, 29, 58);
            centered(canvas, "购买房卡:只有在商城购买的", 883.5f, 74, 32, 0xffa36f48);
            centered(canvas, "房卡可以划入比赛场", 883.5f, 116, 32, 0xffa36f48);
        }
        if (charge) {
            RectF bounds = cocosRect(1133.0233f, 618, 380, 190);
            drawCapInsets(canvas, bubble, bounds, 167, 59, 29, 58);
            centered(canvas, "划入比赛场的房卡从", 1133.0233f, 416, 32, 0xffa36f48);
            centered(canvas, "游戏账户上扣除", 1133.0233f, 458, 32, 0xffa36f48);
        }
    }

    private void centered(Canvas canvas, String value, float x, float y, float size, int color) {
        configure(size, color, Paint.Align.CENTER);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        canvas.drawText(value, x, y - (metrics.ascent + metrics.descent) / 2f, paint);
    }

    private void text(Canvas canvas, String value, float x, float baseline, float size, int color) {
        configure(size, color, Paint.Align.LEFT);
        canvas.drawText(value, x, baseline, paint);
    }

    private void configure(float size, int color, Paint.Align align) {
        paint.setTypeface(typeface);
        paint.setTextSize(size);
        paint.setTextAlign(align);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
    }

    private void draw(Canvas canvas, Bitmap bitmap, RectF destination) {
        paint.setAlpha(255);
        canvas.drawBitmap(bitmap, null, destination, paint);
    }

    private void drawNine(Canvas canvas, Bitmap bitmap, RectF destination) {
        int[] sx = {0, 16, bitmap.getWidth() - 16, bitmap.getWidth()};
        int[] sy = {0, 16, bitmap.getHeight() - 16, bitmap.getHeight()};
        float[] dx = {destination.left, destination.left + 16, destination.right - 16, destination.right};
        float[] dy = {destination.top, destination.top + 16, destination.bottom - 16, destination.bottom};
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                canvas.drawBitmap(
                        bitmap,
                        new Rect(sx[column], sy[row], sx[column + 1], sy[row + 1]),
                        new RectF(dx[column], dy[row], dx[column + 1], dy[row + 1]),
                        paint);
            }
        }
    }

    private void drawCapInsets(
            Canvas canvas,
            Bitmap bitmap,
            RectF destination,
            int capX,
            int capY,
            int capWidth,
            int capHeight) {
        int left = capX;
        int right = bitmap.getWidth() - capX - capWidth;
        int bottom = capY;
        int top = bitmap.getHeight() - capY - capHeight;
        int[] sx = {0, left, bitmap.getWidth() - right, bitmap.getWidth()};
        int[] sy = {0, top, bitmap.getHeight() - bottom, bitmap.getHeight()};
        float[] dx = {destination.left, destination.left + left, destination.right - right, destination.right};
        float[] dy = {destination.top, destination.top + top, destination.bottom - bottom, destination.bottom};
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                canvas.drawBitmap(
                        bitmap,
                        new Rect(sx[column], sy[row], sx[column + 1], sy[row + 1]),
                        new RectF(dx[column], dy[row], dx[column + 1], dy[row + 1]),
                        paint);
            }
        }
    }

    private static Bitmap bitmap(Context context, int resource) {
        return BitmapFactory.decodeResource(context.getResources(), resource);
    }

    private static float cocosY(float value) { return 1080 - value; }

    private static RectF cocosRect(float centerX, float centerY, float width, float height) {
        return new RectF(
                centerX - width / 2,
                cocosY(centerY) - height / 2,
                centerX + width / 2,
                cocosY(centerY) + height / 2);
    }
}
