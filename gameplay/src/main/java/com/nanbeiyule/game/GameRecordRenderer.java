package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

final class GameRecordRenderer {
    private static final int BROWN = Color.rgb(163, 111, 72);
    private static final int RED_BROWN = Color.rgb(194, 108, 70);
    private static final int SELF_TEAL = Color.rgb(30, 145, 141);
    private static final DateTimeFormatter ROW_TIME = DateTimeFormatter
            .ofPattern("yyyy-M-d HH:mm")
            .withZone(ZoneId.of("Asia/Shanghai"));

    private final GameRecordDrawableSet images;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Rect source = new Rect();

    GameRecordRenderer(Context context) {
        images = new GameRecordDrawableSet(context.getResources());
        Typeface font;
        try {
            font = Typeface.createFromAsset(
                    context.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        } catch (RuntimeException exception) {
            font = Typeface.DEFAULT_BOLD;
        }
        textPaint.setTypeface(font);
    }

    void draw(
            Canvas canvas,
            GameRecordPage page,
            boolean gold,
            String date,
            String latestDate,
            long selectedGameId,
            List<Long> gameIds,
            boolean dateMenu,
            boolean gameMenu,
            boolean loading,
            String error,
            float scroll,
            float safeLeft,
            float safeRight,
            float safeBottom) {
        drawBackground(canvas);
        drawBitmap(canvas, images.top, 0, 0, 1920, 121);
        drawBitmap(canvas, images.titleBackground, 668, 0, 584, 120);
        drawBitmap(canvas, images.title, 817, 9, 286, 82);
        drawBitmap(canvas, images.back, 30 + safeLeft, 0, 94, 102);
        drawTabs(canvas, gold, safeRight);
        drawFilters(canvas, page, gold, date, selectedGameId, dateMenu, gameMenu,
                safeLeft, safeRight);
        drawContent(canvas, page, gold, loading, error, scroll, safeRight, safeBottom);
        if (dateMenu) drawDateMenu(canvas, latestDate, safeLeft);
        if (gameMenu) drawGameMenu(canvas, selectedGameId, gameIds, safeLeft);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawColor(Color.rgb(248, 229, 187));
        source.set(0, 0, images.background.getWidth(), 81);
        canvas.drawBitmap(images.background, source, new RectF(0, 0, 1920, 81), bitmapPaint);
        source.set(0, 81, images.background.getWidth(), 146);
        canvas.drawBitmap(images.background, source, new RectF(0, 81, 1920, 842), bitmapPaint);
        source.set(0, 146, images.background.getWidth(), images.background.getHeight());
        canvas.drawBitmap(images.background, source, new RectF(0, 842, 1920, 1080), bitmapPaint);
    }

    private void drawTabs(Canvas canvas, boolean gold, float right) {
        drawBitmap(canvas, gold ? images.goldOn : images.goldOff,
                1346 + right, 39, 277, 69);
        drawBitmap(canvas, gold ? images.battleOff : images.battleOn,
                1606 + right, 39, 277, 69);
    }

    private void drawFilters(
            Canvas canvas,
            GameRecordPage page,
            boolean gold,
            String date,
            long gameId,
            boolean dateMenu,
            boolean gameMenu,
            float left,
            float right) {
        text(canvas, "日期：", 21 + left, 173, 45, RED_BROWN, Paint.Align.LEFT);
        drawBitmap(canvas, images.inputBackground, 130 + left, 138, 310, 74);
        text(canvas, date, 285 + left, 175, 40, Color.rgb(255, 250, 235), Paint.Align.CENTER);
        drawBitmap(canvas, dateMenu ? images.dropUp : images.dropDown,
                373 + left, 141, 74, 72);
        if (!gold) {
            drawBitmap(canvas, images.inputBackground, 465 + left, 138, 350, 74);
            text(canvas, gameName(gameId), 620 + left, 175, 40,
                    Color.rgb(255, 250, 235), Paint.Align.CENTER);
            drawBitmap(canvas, gameMenu ? images.dropUp : images.dropDown,
                    738 + left, 141, 74, 72);
            GameRecordPage.Summary summary = page == null
                    ? new GameRecordPage.Summary(0, 0, 0) : page.summary();
            text(canvas, "冠军次数：" + summary.championCount(),
                    822 + left, 151, 33, RED_BROWN, Paint.Align.LEFT);
            text(canvas, "今日优胜值：" + summary.score(),
                    1085 + left, 151, 33, RED_BROWN, Paint.Align.LEFT);
            text(canvas, "今日场数：" + summary.roundCount(),
                    822 + left, 196, 33, RED_BROWN, Paint.Align.LEFT);
        }
        drawBitmap(canvas, gold ? images.totalGold : images.totalBattle,
                1571 + right, 133, 201, 78);
        if (!gold) drawBitmap(canvas, images.refresh, 1782 + right, 133, 121, 78);
    }

    private void drawContent(
            Canvas canvas,
            GameRecordPage page,
            boolean gold,
            boolean loading,
            String error,
            float scroll,
            float right,
            float bottom) {
        if (gold && page != null && !page.membershipActive()) {
            text(canvas, "开通会员可查看金币战绩哦~",
                    960, 532, 48, BROWN, Paint.Align.CENTER);
            drawBitmap(canvas, images.openMember, 746, 604, 428, 165);
        } else if (page != null && !page.records().isEmpty()) {
            drawRecords(canvas, page.records(), scroll);
        } else if (!loading && (error == null || error.isBlank())) {
            text(canvas, "暂无战绩", 960, 600, 48, BROWN, Paint.Align.CENTER);
        }
        if (loading) {
            text(canvas, "正在加载战绩...", 960, 680, 36, BROWN, Paint.Align.CENTER);
        } else if (error != null && !error.isBlank()) {
            text(canvas, error, 960, 650, 34, BROWN, Paint.Align.CENTER);
            text(canvas, "点击刷新重试", 960, 704, 30, SELF_TEAL, Paint.Align.CENTER);
        }
        if (gold) {
            text(canvas, "仅记录开通会员期间的近7天金币场战绩",
                    1815 + right, 1032 + bottom, 31, Color.rgb(174, 137, 93), Paint.Align.RIGHT);
        } else {
            text(canvas, "输入好友的详情页回放码，即可查看当前对局！",
                    1420 + right, 1033 + bottom, 31, Color.rgb(174, 137, 93), Paint.Align.RIGHT);
            drawBitmap(canvas, images.replay, 1644 + right, 986 + bottom, 226, 86);
        }
    }

    private void drawRecords(Canvas canvas, List<GameRecordPage.Record> records, float scroll) {
        int save = canvas.save();
        canvas.clipRect(40, 225, 1880, 975);
        for (int index = 0; index < records.size(); index++) {
            float top = 225 + index * 220f - scroll;
            if (top > 975 || top + 220 < 225) continue;
            drawRecord(canvas, records.get(index), index + 1, top);
        }
        canvas.restoreToCount(save);
    }

    private void drawRecord(Canvas canvas, GameRecordPage.Record record, int index, float top) {
        drawBitmap(canvas, images.itemBackground, 45, top + 8, 1830, 205);
        text(canvas, Integer.toString(index), 105, top + 119, 68,
                Color.rgb(197, 79, 48), Paint.Align.CENTER);
        drawBitmap(canvas, images.gameBackground, 157, top + 19, 315, 64);
        text(canvas, record.gameName(), 315, top + 51, 34,
                Color.rgb(251, 244, 221), Paint.Align.CENTER);
        text(canvas, "房间号: " + record.roomNumber(), 175, top + 112, 29, BROWN, Paint.Align.LEFT);
        text(canvas, "局数: " + record.finishedRounds() + "/" + record.totalRounds(),
                175, top + 151, 29, BROWN, Paint.Align.LEFT);
        text(canvas, formatTime(record.finishedAt()), 175, top + 190, 28, BROWN, Paint.Align.LEFT);
        List<GameRecordPage.Player> players = record.players();
        float slot = 1100f / Math.max(1, players.size());
        for (int playerIndex = 0; playerIndex < players.size(); playerIndex++) {
            GameRecordPage.Player player = players.get(playerIndex);
            float center = 500 + slot * (playerIndex + 0.5f);
            if (player.host()) drawBitmap(canvas, images.host, center - 86, top + 37, 64, 34);
            text(canvas, player.displayName(), center, top + 66, 34,
                    player.self() ? SELF_TEAL : BROWN, Paint.Align.CENTER);
            text(canvas, signed(player.score()), center, top + 145, 42,
                    player.score() >= 0 ? Color.rgb(222, 91, 45) : SELF_TEAL,
                    Paint.Align.CENTER);
        }
        drawBitmap(canvas, images.detail, 1639, top + 50, 215, 96);
    }

    private void drawDateMenu(Canvas canvas, String selected, float left) {
        drawBitmap(canvas, images.popup, 115 + left, 213, 330, 440);
        java.time.LocalDate today = java.time.LocalDate.parse(selected);
        for (int index = 0; index < 7; index++) {
            String value = today.minusDays(index).toString();
            text(canvas, value, 280 + left, 253 + index * 60, 32,
                    BROWN, Paint.Align.CENTER);
        }
    }

    private void drawGameMenu(Canvas canvas, long selected, List<Long> gameIds, float left) {
        int count = Math.max(1, gameIds.size() + 1);
        float height = Math.min(440, count * 64f + 20f);
        drawBitmap(canvas, images.popup, 465 + left, 213, 350, height);
        text(canvas, "全选", 640 + left, 253, 34,
                selected == 0 ? SELF_TEAL : BROWN, Paint.Align.CENTER);
        for (int index = 0; index < gameIds.size(); index++) {
            long gameId = gameIds.get(index);
            text(canvas, gameName(gameId), 640 + left, 317 + index * 64, 32,
                    selected == gameId ? SELF_TEAL : BROWN, Paint.Align.CENTER);
        }
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, float x, float y, float width, float height) {
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawBitmap(bitmap, source, new RectF(x, y, x + width, y + height), bitmapPaint);
    }

    private void text(Canvas canvas, String value, float x, float centerY,
            float size, int color, Paint.Align align) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setTextAlign(align);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setFakeBoldText(true);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(value, x, centerY - (metrics.ascent + metrics.descent) / 2f, textPaint);
    }

    private static String gameName(long gameId) {
        if (gameId == 0) return "全选";
        return switch ((int) gameId) {
            case 30109, 30400 -> "台州麻将";
            case 30284 -> "挖花玩法";
            case 30588 -> "茶苑双扣";
            default -> "游戏" + gameId;
        };
    }

    private static String formatTime(String value) {
        try {
            return ROW_TIME.format(Instant.parse(value));
        } catch (RuntimeException exception) {
            return value;
        }
    }

    private static String signed(long score) {
        return score > 0 ? "+" + score : Long.toString(score);
    }
}
