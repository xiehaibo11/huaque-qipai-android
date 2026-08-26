package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Portrait renderer matching the rotated 1080x1920 original scoring-assistant surface. */
final class ScoreAssistantRenderer {
    private static final int TEXT = Color.rgb(52, 50, 48);
    private static final int MUTED = Color.rgb(124, 124, 124);
    private static final int RED = Color.rgb(241, 94, 65);
    private static final int GREEN = Color.rgb(74, 145, 120);
    private static final int BLUE = Color.rgb(75, 114, 238);
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Asia/Shanghai"));
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

    ScoreAssistantRenderer(Context context) {
        text.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/zihun_jingdian_lihei.ttf"));
    }

    void draw(Canvas canvas, ScoreAssistantState state, float scroll) {
        drawBackground(canvas);
        if (state.loadState() == ScoreAssistantState.LoadState.LOADING) {
            centered(canvas, "计分数据加载中...", 950f, 40f, MUTED);
        } else if (state.loadState() == ScoreAssistantState.LoadState.ERROR) {
            centered(canvas, state.error(), 940f, 38f, RED);
            button(canvas, ScoreAssistantLayout.RETRY, "重试", BLUE);
        } else if (state.detail() != null) {
            drawDetail(canvas, state.detail());
        } else if (state.tab() == ScoreAssistantState.Tab.ACTIVE) {
            drawHome(canvas, state.active());
        } else if (state.tab() == ScoreAssistantState.Tab.HISTORY) {
            drawHistory(canvas, state.history(), scroll);
        } else {
            drawMonthly(canvas, state.monthly());
        }
        drawNavigation(canvas, state.tab());
        drawClose(canvas);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawColor(Color.rgb(217, 238, 255));
        paint.setColor(Color.rgb(197, 225, 253));
        canvas.drawCircle(180f, 420f, 180f, paint);
        canvas.drawCircle(900f, 220f, 140f, paint);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(34f, 330f, 1046f, 1605f, 28f, 28f, paint);
        paint.setColor(Color.argb(70, 88, 154, 220));
        canvas.drawRect(0f, 1733f, 1080f, 1737f, paint);
        paint.setColor(Color.WHITE);
        canvas.drawRect(0f, 1737f, 1080f, 1920f, paint);
    }

    private void drawHome(Canvas canvas, List<ScoreAssistantApiProtocol.LedgerSummary> active) {
        label(canvas, "麻将计分", 62f, 220f, 76f, BLUE, Paint.Align.LEFT, true);
        if (active.isEmpty()) {
            centered(canvas, "还没有进行中的计分", 830f, 40f, MUTED);
            centered(canvas, "支持 2 至 6 人，所有累计分由服务端计算", 905f, 29f, MUTED);
            button(canvas, ScoreAssistantLayout.CREATE, "开始计分", BLUE);
            return;
        }
        label(canvas, "进行中的计分", 68f, 420f, 44f, TEXT, Paint.Align.LEFT, true);
        float y = 510f;
        for (ScoreAssistantApiProtocol.LedgerSummary ledger : active) {
            drawCompactLedger(canvas, ledger, y);
            y += 190f;
            if (y > 1390f) break;
        }
        button(canvas, ScoreAssistantLayout.CREATE, "+ 新建计分", BLUE);
    }

    private void drawDetail(Canvas canvas, ScoreAssistantApiProtocol.LedgerDetail detail) {
        boolean active = detail.status() == ScoreAssistantApiProtocol.Status.IN_PROGRESS;
        label(canvas, "第 " + (detail.roundCount() + (active ? 1 : 0)) + " 局", 540f, 245f,
                72f, BLUE, Paint.Align.CENTER, true);
        label(canvas, "名称", 112f, 485f, 43f, MUTED, Paint.Align.CENTER, false);
        label(canvas, "胜负", 365f, 485f, 43f, MUTED, Paint.Align.CENTER, false);
        label(canvas, "得分", 760f, 485f, 43f, MUTED, Paint.Align.CENTER, false);
        float y = 600f;
        for (ScoreAssistantApiProtocol.Player player : detail.players()) {
            drawPlayerRow(canvas, player, y, active);
            y += 150f;
            if (y > 1450f) break;
        }
        if (active) {
            button(canvas, ScoreAssistantLayout.PRIMARY, "保存", Color.rgb(249, 194, 54));
            button(canvas, ScoreAssistantLayout.SECONDARY, "对局结束", Color.rgb(64, 139, 239));
        } else {
            button(canvas, ScoreAssistantLayout.PRIMARY,
                    detail.favorite() ? "取消收藏" : "收藏", Color.rgb(249, 194, 54));
            button(canvas, ScoreAssistantLayout.SECONDARY, "删除", Color.rgb(64, 139, 239));
        }
    }

    private void drawPlayerRow(Canvas canvas, ScoreAssistantApiProtocol.Player player, float y,
            boolean editable) {
        label(canvas, player.name(), 112f, y + 64f, 38f, TEXT, Paint.Align.CENTER, false);
        paint.setColor(player.totalScore() >= 0 ? RED : GREEN);
        canvas.drawCircle(360f, y + 55f, 32f, paint);
        label(canvas, player.totalScore() >= 0 ? "胜" : "负", 360f, y + 66f, 30f,
                Color.WHITE, Paint.Align.CENTER, true);
        label(canvas, signed(player.totalScore()), 610f, y + 68f, 44f,
                scoreColor(player.totalScore()), Paint.Align.CENTER, true);
        paint.setColor(Color.rgb(233, 233, 233));
        canvas.drawRoundRect(830f, y + 8f, 930f, y + 100f, 10f, 10f, paint);
        if (editable) label(canvas, "✎", 760f, y + 72f, 38f, Color.LTGRAY, Paint.Align.CENTER, false);
        paint.setColor(Color.rgb(236, 236, 236));
        canvas.drawRect(60f, y + 124f, 1020f, y + 126f, paint);
    }

    private void drawHistory(Canvas canvas, ScoreAssistantApiProtocol.HistoryPage page, float scroll) {
        label(canvas, "战绩记录", 58f, 270f, 62f, BLUE, Paint.Align.LEFT, true);
        List<ScoreAssistantApiProtocol.LedgerSummary> ledgers = page == null ? List.of() : page.ledgers();
        if (ledgers.isEmpty()) centered(canvas, "暂无战绩信息", 940f, 40f, MUTED);
        int save = canvas.save();
        canvas.clipRect(box(ScoreAssistantLayout.CARDS));
        for (int i = 0; i < ledgers.size(); i++) {
            drawHistoryCard(canvas, ledgers.get(i), ScoreAssistantLayout.cardRect(i, scroll));
        }
        canvas.restoreToCount(save);
        if (page != null) {
            label(canvas, page.page() + "/" + Math.max(1, page.totalPages()), 925f, 1655f,
                    34f, MUTED, Paint.Align.CENTER, false);
        }
    }

    private void drawHistoryCard(Canvas canvas, ScoreAssistantApiProtocol.LedgerSummary ledger,
            ScoreAssistantLayout.Box box) {
        paint.setColor(Color.rgb(248, 249, 250));
        canvas.drawRoundRect(box.left() + 30f, box.top() + 8f, box.right() - 30f,
                box.bottom() - 8f, 24f, 24f, paint);
        label(canvas, DATE.format(ledger.startedAt()), 60f, box.top() + 66f, 30f,
                MUTED, Paint.Align.LEFT, false);
        String names = ledger.players().stream().map(ScoreAssistantApiProtocol.Player::name)
                .reduce((a, b) -> a + "  " + b).orElse("");
        label(canvas, ellipsize(names, 820f), 60f, box.top() + 145f, 38f,
                TEXT, Paint.Align.LEFT, true);
        label(canvas, ledger.roundCount() + "局", 960f, box.top() + 145f, 34f,
                BLUE, Paint.Align.RIGHT, true);
        ScoreAssistantApiProtocol.Player owner = owner(ledger.players());
        label(canvas, owner == null ? "" : "我的总分 " + signed(owner.totalScore()),
                60f, box.top() + 235f, 38f,
                owner == null ? MUTED : scoreColor(owner.totalScore()), Paint.Align.LEFT, true);
        if (ledger.favorite()) label(canvas, "★", 960f, box.top() + 245f, 52f,
                Color.rgb(248, 187, 46), Paint.Align.RIGHT, true);
    }

    private void drawMonthly(Canvas canvas, ScoreAssistantApiProtocol.MonthlyStatistics monthly) {
        label(canvas, "我的", 58f, 270f, 62f, BLUE, Paint.Align.LEFT, true);
        if (monthly == null) { centered(canvas, "本月数据加载中", 950f, 38f, MUTED); return; }
        centered(canvas, monthly.month().toString(), 440f, 46f, TEXT);
        metric(canvas, "总场数", Long.toString(monthly.totalPlay()), 235f, 650f);
        metric(canvas, "胜场", Long.toString(monthly.winPlay()), 540f, 650f);
        metric(canvas, "负场", Long.toString(monthly.lossPlay()), 845f, 650f);
        metric(canvas, "总得分", Long.toString(monthly.totalScore()), 235f, 930f);
        metric(canvas, "总胜分", Long.toString(monthly.winScore()), 540f, 930f);
        metric(canvas, "总负分", Long.toString(monthly.lossScore()), 845f, 930f);
    }

    private void drawNavigation(Canvas canvas, ScoreAssistantState.Tab selected) {
        nav(canvas, 180f, 0, "首页", selected == ScoreAssistantState.Tab.ACTIVE);
        nav(canvas, 540f, 1, "记录", selected == ScoreAssistantState.Tab.HISTORY);
        nav(canvas, 900f, 2, "我的", selected == ScoreAssistantState.Tab.MONTHLY);
    }

    private void nav(Canvas canvas, float x, int icon, String title, boolean selected) {
        int color = selected ? BLUE : Color.rgb(142, 142, 142);
        drawNavIcon(canvas, x, icon, color, selected);
        label(canvas, title, x, 1880f, 30f, color, Paint.Align.CENTER, selected);
    }

    private void drawNavIcon(Canvas canvas, float x, int icon, int color, boolean selected) {
        paint.setColor(color);
        paint.setStrokeWidth(selected ? 11f : 9f);
        paint.setStyle(Paint.Style.STROKE);
        if (icon == 0) {
            Path home = new Path();
            home.moveTo(x - 42f, 1800f);
            home.lineTo(x, 1765f);
            home.lineTo(x + 42f, 1800f);
            home.lineTo(x + 30f, 1800f);
            home.lineTo(x + 30f, 1835f);
            home.lineTo(x - 30f, 1835f);
            home.lineTo(x - 30f, 1800f);
            canvas.drawPath(home, paint);
        } else if (icon == 1) {
            canvas.drawRect(x - 42f, 1774f, x + 42f, 1834f, paint);
            canvas.drawLine(x - 20f, 1774f, x - 20f, 1834f, paint);
            canvas.drawLine(x + 20f, 1774f, x + 20f, 1834f, paint);
        } else {
            canvas.drawCircle(x, 1788f, 22f, paint);
            canvas.drawArc(x - 43f, 1810f, x + 43f, 1860f, 190f, 160f, false, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawClose(Canvas canvas) {
        paint.setColor(Color.argb(180, 55, 93, 137));
        canvas.drawCircle(994f, 119f, 58f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(7f);
        paint.setColor(Color.WHITE);
        canvas.drawLine(970f, 95f, 1018f, 143f, paint);
        canvas.drawLine(1018f, 95f, 970f, 143f, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawCompactLedger(Canvas canvas, ScoreAssistantApiProtocol.LedgerSummary ledger,
            float y) {
        paint.setColor(Color.rgb(246, 249, 252));
        canvas.drawRoundRect(55f, y, 1025f, y + 155f, 24f, 24f, paint);
        String names = ledger.players().stream().map(ScoreAssistantApiProtocol.Player::name)
                .reduce((a, b) -> a + "  " + b).orElse("");
        label(canvas, ellipsize(names, 700f), 85f, y + 62f, 36f, TEXT, Paint.Align.LEFT, true);
        label(canvas, ledger.roundCount() + "局 · " + DATE.format(ledger.startedAt()), 85f,
                y + 116f, 27f, MUTED, Paint.Align.LEFT, false);
    }

    private void metric(Canvas canvas, String title, String value, float x, float y) {
        label(canvas, value, x, y, 68f, BLUE, Paint.Align.CENTER, true);
        label(canvas, title, x, y + 65f, 32f, MUTED, Paint.Align.CENTER, false);
    }

    private void button(Canvas canvas, ScoreAssistantLayout.Box b, String title, int color) {
        paint.setColor(color);
        canvas.drawRoundRect(box(b), 65f, 65f, paint);
        label(canvas, title, b.centerX(), b.centerY() + 18f, 52f,
                Color.WHITE, Paint.Align.CENTER, true);
    }

    private void centered(Canvas c, String value, float y, float size, int color) {
        label(c, value == null ? "" : value, 540f, y, size, color, Paint.Align.CENTER, false);
    }

    private void label(Canvas c, String value, float x, float baseline, float size, int color,
            Paint.Align align, boolean bold) {
        text.setTextSize(size);
        text.setColor(color);
        text.setTextAlign(align);
        text.setFakeBoldText(bold);
        c.drawText(value == null ? "" : value, x, baseline, text);
    }

    private String ellipsize(String value, float width) {
        if (text.measureText(value) <= width) return value;
        while (value.length() > 1 && text.measureText(value + "…") > width) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "…";
    }

    private static String signed(long score) { return score > 0 ? "+" + score : Long.toString(score); }
    private static int scoreColor(long score) { return score >= 0 ? RED : GREEN; }
    private static ScoreAssistantApiProtocol.Player owner(List<ScoreAssistantApiProtocol.Player> list) {
        return list.stream().filter(ScoreAssistantApiProtocol.Player::ownerPlayer).findFirst().orElse(null);
    }
    private static RectF box(ScoreAssistantLayout.Box b) {
        return new RectF(b.left(), b.top(), b.right(), b.bottom());
    }
}
