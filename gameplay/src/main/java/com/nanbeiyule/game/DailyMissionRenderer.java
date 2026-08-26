package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.time.Instant;
import java.util.List;

final class DailyMissionRenderer {
    private static final DailyMissionLayout.Box ACTIVITY_LIGHT =
            new DailyMissionLayout.Box(1106.7257f, 87.8625f, 1785.7257f, 168.8625f);
    private static final DailyMissionLayout.Box ACTIVITY_ICON =
            new DailyMissionLayout.Box(313.7f, 236.8f, 398.7f, 337.1f);

    // 以下字号与颜色全部取自 CSB 的 TextOptions（fontSize）与 WidgetOptions（color）。
    private static final int TAB_TEXT_ACTIVE_COLOR = Color.rgb(171, 69, 2);
    private static final int TAB_TEXT_INACTIVE_COLOR = Color.rgb(93, 93, 93);
    private static final float TAB_TEXT_ACTIVE_SIZE = 48f;
    private static final float TAB_TEXT_INACTIVE_SIZE = 44f;
    private static final float LEFT_TEXT_SIZE = 32f;
    private static final float NO_MISSION_SIZE = 46f;
    private static final float MILESTONE_COUNT_SIZE = 32f;
    private static final float MILESTONE_STAGE_SIZE = 30f;
    /** KW_CNT 是 CSB 里唯一开了描边的文字：outlineSize 3、outlineColor rgb(49,48,53)。 */
    static final int TEXT_OUTLINE_COLOR = Color.rgb(49, 48, 53);
    static final float TEXT_OUTLINE_WIDTH = 3f;

    private final DailyMissionDrawableSet drawables;
    private final DailyMissionEffects effects;
    private final java.util.Map<Integer, ColorMatrixColorFilter> tintFilters =
            new java.util.HashMap<>();
    private final RectF nineSliceDestination = new RectF();
    private final SxvipBitmapFont activityPointsFont;
    private final DailyMissionTaskRenderer taskRenderer;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint overlayPaint = new Paint();
    private final Rect source = new Rect();

    DailyMissionRenderer(Context context) {
        drawables = new DailyMissionDrawableSet(context.getResources());
        effects = new DailyMissionEffects(context.getAssets());
        Typeface typeface = Typeface.createFromAsset(
                context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        textPaint.setTypeface(typeface);
        activityPointsFont = SxvipBitmapFont.load(
                context.getResources(), "daily_mission/fnt_ziti-export.fnt");
        taskRenderer = new DailyMissionTaskRenderer(drawables, effects, typeface);
    }

    boolean animated() {
        return effects.available();
    }

    void draw(
            Canvas canvas,
            DailyMissionState state,
            float taskScroll,
            DailyMissionTaskRenderer.RewardScroll rewardScroll,
            int pressedTaskIndex,
            long countdownSeconds,
            float elapsedSeconds,
            Instant now,
            boolean loading,
            String errorMessage) {
        // 顺序照 _KW_ROOT_LAYER 的子节点次序：底板、顶栏、页签、页面底图……最后是顶部扫光。
        // 主题按 getCurTabConfig：用当前页签的文案去 Config.lua TAB_INFO 里查。
        DailyMissionTheme theme = state == null
                ? DailyMissionTheme.DAILY
                : DailyMissionTheme.forDisplayName(state.page().displayName());
        drawBitmap(canvas, drawables.board, DailyMissionLayout.BOARD);
        drawBitmap(canvas, drawables.activityLight, ACTIVITY_LIGHT);
        drawTabs(canvas, state);
        drawBitmap(
                canvas, drawables.pageBackground(theme), DailyMissionLayout.PAGE_BACKGROUND);

        if (state != null) {
            boolean hasStages = !state.milestones().isEmpty();
            // _KW_BG_1/_KW_BG_2：Config.lua 的 BG_IMG_TASK，50x50 圆角贴图九宫格拉伸。
            drawNineSlice(
                    canvas,
                    drawables.taskBackground(theme),
                    DailyMissionLayout.taskBackground(hasStages),
                    DailyMissionLayout.TASK_BACKGROUND_INSET);
            if (hasStages) drawActivity(canvas, state, theme, elapsedSeconds);
            canvas.save();
            DailyMissionLayout.Box viewport = DailyMissionLayout.taskViewport(hasStages);
            canvas.clipRect(viewport.left(), viewport.top(), viewport.right(), viewport.bottom());
            taskRenderer.drawTasks(
                    canvas, state, taskScroll, rewardScroll, pressedTaskIndex,
                    elapsedSeconds, now, hasStages);
            canvas.restore();
            if (state.tasks().isEmpty() && !loading) {
                drawTextAtCenter(canvas, "暂无可完成任务~", 1018f, viewport.centerY(),
                        NO_MISSION_SIZE, Color.argb(153, 255, 255, 255));
            }
        }
        drawBitmap(canvas, drawables.close, DailyMissionLayout.CLOSE);
        drawBitmap(canvas, drawables.title(theme), DailyMissionLayout.TITLE);
        drawCountdown(canvas, countdownSeconds);
        effects.draw(
                canvas,
                DailyMissionEffects.TOP_SWEEP,
                elapsedSeconds,
                DailyMissionLayout.EFFECT_TOP_SWEEP_X,
                DailyMissionLayout.EFFECT_TOP_SWEEP_Y,
                1f,
                1f);
        drawLoadingOrError(canvas, loading, errorMessage);
    }

    /** 原版 initTabs：页签数量、文案和红点全部由服务端 pageList 驱动，最多五个。 */
    private void drawTabs(Canvas canvas, DailyMissionState state) {
        if (state == null) return;
        List<DailyMissionState.Page> pages = state.tabs();
        for (int index = 0; index < pages.size(); index++) {
            DailyMissionState.Page page = pages.get(index);
            boolean active = page.pageCode().equals(state.page().pageCode());
            drawTab(canvas, index, active, page.displayName());
        }
        for (int index = 0; index < pages.size(); index++) {
            if (pages.get(index).redPoint()) {
                drawBitmap(canvas, drawables.redPoint, DailyMissionLayout.tabRedPoint(index));
            }
        }
    }

    private void drawTab(Canvas canvas, int index, boolean active, String label) {
        DailyMissionLayout.Box box = DailyMissionLayout.tabImage(index, active);
        drawBitmap(canvas, active ? drawables.tabActive : drawables.tabInactive, box);
        // pageList 的页签名可以带换行，例如原版测试数据里的「中秋\n幸运任务」。
        String[] lines = label.split("\n");
        float lineHeight = active
                ? DailyMissionLayout.TAB_TEXT_LINE_HEIGHT_ACTIVE
                : DailyMissionLayout.TAB_TEXT_LINE_HEIGHT_INACTIVE;
        float centerY = box.top() + DailyMissionLayout.TAB_TEXT_OFFSET_Y
                - (lines.length - 1) * lineHeight * 0.5f;
        for (String line : lines) {
            drawTextAtCenter(
                    canvas,
                    line,
                    box.left() + DailyMissionLayout.TAB_TEXT_OFFSET_X,
                    centerY,
                    active ? TAB_TEXT_ACTIVE_SIZE : TAB_TEXT_INACTIVE_SIZE,
                    active ? TAB_TEXT_ACTIVE_COLOR : TAB_TEXT_INACTIVE_COLOR);
            centerY += lineHeight;
        }
    }

    private void drawCountdown(Canvas canvas, long seconds) {
        String value = DailyMissionState.formatCountdown(seconds);
        configureText(LEFT_TEXT_SIZE, Color.WHITE, Paint.Align.LEFT);
        // _KW_LEFT pos(718,945) anchor(0,0.5) -> 顶部原点中心 y=135
        canvas.drawText("活动剩余:" + value, 718f, baselineFor(135f), textPaint);
    }

    private void drawActivity(
            Canvas canvas,
            DailyMissionState state,
            DailyMissionTheme theme,
            float elapsedSeconds) {
        int themeColor = theme.progressColor();
        drawBitmap(canvas, drawables.activityBadge, DailyMissionLayout.ACTIVITY_BADGE);
        drawBitmap(canvas, drawables.activityIcon, ACTIVITY_ICON);
        effects.draw(
                canvas,
                DailyMissionEffects.ACTIVITY_ICON_FLOW,
                elapsedSeconds,
                DailyMissionLayout.EFFECT_ACTIVITY_ICON_X,
                DailyMissionLayout.EFFECT_ACTIVITY_ICON_Y,
                DailyMissionLayout.EFFECT_ACTIVITY_ICON_SCALE,
                DailyMissionLayout.EFFECT_ACTIVITY_ICON_SCALE);
        // _KW_CUR_PER 是 TextBMFont，用原版位图字体 fnt_ziti-export 绘制；
        // 节点 pos(160,47.4064) anchor(0.5,0.5) 于 Image_13 内 -> 舞台中心 (458.85,274.606)
        activityPointsFont.drawCentered(
                canvas, Long.toString(state.activityPoints()), 458.85f, 274.606f);
        effects.draw(
                canvas,
                DailyMissionEffects.ACTIVITY_STARS,
                elapsedSeconds,
                DailyMissionLayout.EFFECT_ACTIVITY_STARS_X,
                DailyMissionLayout.EFFECT_ACTIVITY_STARS_Y,
                1f,
                1f);
        // View.lua:219 _KW_PROGRESS_BG:setColor(showConfig.PROGRESS_CLOLOR)
        drawTintedBitmap(
                canvas,
                drawables.activityProgressTrack,
                DailyMissionLayout.ACTIVITY_PROGRESS,
                themeColor);
        List<Long> targets = state.milestones().stream()
                .map(DailyMissionState.Milestone::target)
                .toList();
        float percent = DailyMissionState.segmentedProgress(state.activityPoints(), targets);
        DailyMissionLayout.Box progress = DailyMissionLayout.ACTIVITY_PROGRESS;
        if (percent > 0f) {
            canvas.save();
            canvas.clipRect(
                    progress.left(), progress.top(),
                    progress.left() + progress.width() * percent / 100f, progress.bottom());
            drawBitmap(canvas, drawables.activityProgressFill, progress);
            canvas.restore();
        }
        // View.lua:257 _KW_ANI_PRO:setPositionX(进度条宽度 * showPer / 100)
        effects.draw(
                canvas,
                DailyMissionEffects.PROGRESS_HEAD,
                elapsedSeconds,
                progress.left() + progress.width() * percent / 100f,
                DailyMissionLayout.EFFECT_PROGRESS_HEAD_Y,
                1f,
                1f);
        for (int index = 0; index < state.milestones().size(); index++) {
            drawMilestone(
                    canvas, state.milestones().get(index), index, themeColor, elapsedSeconds);
        }
    }

    /**
     * 严格照 _KW_PANEL_ITEM_AWARD 的节点层次：Image_4 底框常驻，达标后叠加 KW_IMG_AWARD；
     * KW_UN_STAGE 阶段标签底常驻并染成页签主题色，达标后叠加 KW_IMG_STAGE；
     * 已领取时最上层整体盖 KW_IS_GET（标签 + 框 + 已领取印章）。
     */
    private void drawMilestone(
            Canvas canvas,
            DailyMissionState.Milestone milestone,
            int index,
            int themeColor,
            float elapsedSeconds) {
        DailyMissionLayout.Box box = DailyMissionLayout.milestoneRect(index);
        float frameCenterX = box.centerX() + DailyMissionLayout.MILESTONE_FRAME_OFFSET_X;
        float labelCenterY = box.top() + DailyMissionLayout.MILESTONE_STAGE_CENTER_Y;
        // Lua 的 isFullStage 是「活跃值已达标」，可领取和已领取都算达标。
        boolean reached = milestone.state() != DailyMissionState.MilestoneState.LOCKED;
        boolean claimed = milestone.state() == DailyMissionState.MilestoneState.CLAIMED;

        drawBitmapAt(canvas, drawables.milestoneFrame, frameCenterX, box.centerY());
        if (reached) {
            drawBitmapAt(canvas, drawables.milestoneReached, frameCenterX, box.centerY());
        }

        DailyMissionState.Reward reward = milestone.rewards().isEmpty()
                ? null : milestone.rewards().get(0);
        Bitmap icon = reward == null ? null : drawables.rewardIcon(reward);
        float propHalf = DailyMissionLayout.MILESTONE_PROP_SIZE * 0.5f;
        if (icon != null) {
            drawBitmap(canvas, icon, new RectF(
                    box.centerX() - propHalf,
                    box.top() + DailyMissionLayout.MILESTONE_PROP_TOP,
                    box.centerX() + propHalf,
                    box.top() + DailyMissionLayout.MILESTONE_PROP_TOP
                            + DailyMissionLayout.MILESTONE_PROP_SIZE));
        }
        if (reward != null) {
            drawOutlinedTextAtCenter(
                    canvas,
                    "x" + reward.quantity(),
                    box.left() + DailyMissionLayout.MILESTONE_COUNT_RIGHT,
                    box.top() + DailyMissionLayout.MILESTONE_COUNT_CENTER_Y,
                    MILESTONE_COUNT_SIZE,
                    Paint.Align.RIGHT);
        }

        // View.lua:218 KW_UN_STAGE:setColor(showConfig.PROGRESS_CLOLOR)
        drawTintedBitmapAt(
                canvas, drawables.milestoneLocked, box.centerX(), labelCenterY, themeColor);
        if (reached) {
            drawBitmapAt(
                    canvas, drawables.milestoneReachedLabel, box.centerX(), labelCenterY);
        }
        // KW_STAGE 在 CSB 里没有开描边。
        drawTextAtCenter(canvas, Long.toString(milestone.target()), box.centerX(),
                labelCenterY, MILESTONE_STAGE_SIZE, Color.WHITE);

        // View.lua:233 达标且未领取时才显示闪光。
        if (milestone.state() == DailyMissionState.MilestoneState.CLAIMABLE) {
            effects.draw(
                    canvas,
                    DailyMissionEffects.MILESTONE_READY,
                    elapsedSeconds,
                    box.centerX(),
                    box.centerY() + DailyMissionLayout.EFFECT_MILESTONE_OFFSET_Y,
                    DailyMissionLayout.EFFECT_MILESTONE_SCALE,
                    DailyMissionLayout.EFFECT_MILESTONE_SCALE);
        }
        if (claimed) {
            drawBitmapAt(
                    canvas, drawables.milestoneClaimedLabel, box.centerX(), labelCenterY);
            // KW_IS_GET>Image_5 在 CSB 里被设成 120x123，略大于 hydl_zz 的 117x120。
            drawBitmap(canvas, drawables.milestoneClaimed, new RectF(
                    frameCenterX - 60f, box.centerY() - 61.5f,
                    frameCenterX + 60f, box.centerY() + 61.5f));
            float overlayCenterY = box.top() + DailyMissionLayout.MILESTONE_CLAIMED_CENTER_Y;
            drawBitmap(canvas, drawables.claimedOverlay, new RectF(
                    box.centerX() - DailyMissionLayout.MILESTONE_CLAIMED_WIDTH * 0.5f,
                    overlayCenterY - DailyMissionLayout.MILESTONE_CLAIMED_HEIGHT * 0.5f,
                    box.centerX() + DailyMissionLayout.MILESTONE_CLAIMED_WIDTH * 0.5f,
                    overlayCenterY + DailyMissionLayout.MILESTONE_CLAIMED_HEIGHT * 0.5f));
        }
    }

    private void drawLoadingOrError(Canvas canvas, boolean loading, String errorMessage) {
        if (!loading && (errorMessage == null || errorMessage.isBlank())) return;
        RectF panel = new RectF(720f, 555f, 1320f, 685f);
        overlayPaint.setColor(Color.argb(205, 28, 43, 88));
        canvas.drawRoundRect(panel, 26f, 26f, overlayPaint);
        String text = loading ? "任务加载中..." : errorMessage;
        drawCenteredText(canvas, text, panel.centerX(), panel.centerY() + 14f,
                38f, Color.WHITE);
    }

    private void drawCenteredText(
            Canvas canvas, String text, float x, float baseline, float size, int color) {
        configureText(size, color, Paint.Align.CENTER);
        canvas.drawText(text, x, baseline, textPaint);
    }

    /** CSB 的 Text 用 anchor(0.5,0.5) 定位，按字体度量把中心换算成基线。 */
    private void drawTextAtCenter(
            Canvas canvas, String text, float x, float centerY, float size, int color) {
        configureText(size, color, Paint.Align.CENTER);
        canvas.drawText(text, x, baselineFor(centerY), textPaint);
    }

    private void drawOutlinedTextAtCenter(
            Canvas canvas, String text, float x, float centerY, float size, Paint.Align align) {
        configureText(size, TEXT_OUTLINE_COLOR, align);
        float baseline = baselineFor(centerY);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(TEXT_OUTLINE_WIDTH);
        canvas.drawText(text, x, baseline, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(text, x, baseline, textPaint);
    }

    private float baselineFor(float centerY) {
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        return centerY - (metrics.ascent + metrics.descent) * 0.5f;
    }

    private void configureText(float size, int color, Paint.Align align) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setTextAlign(align);
        textPaint.setStyle(Paint.Style.FILL);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, DailyMissionLayout.Box box) {
        drawBitmap(canvas, bitmap, new RectF(box.left(), box.top(), box.right(), box.bottom()));
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF destination) {
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawBitmap(bitmap, source, destination, bitmapPaint);
    }

    /** CSB 的 ImageView 用原图尺寸居中摆放，不缩放。 */
    private void drawBitmapAt(Canvas canvas, Bitmap bitmap, float centerX, float centerY) {
        drawBitmap(canvas, bitmap, new RectF(
                centerX - bitmap.getWidth() * 0.5f,
                centerY - bitmap.getHeight() * 0.5f,
                centerX + bitmap.getWidth() * 0.5f,
                centerY + bitmap.getHeight() * 0.5f));
    }

    /** cocos 的 setColor 是顶点色相乘，这里用等价的乘法色矩阵。 */
    private void drawTintedBitmap(
            Canvas canvas, Bitmap bitmap, DailyMissionLayout.Box box, int color) {
        bitmapPaint.setColorFilter(tintFilter(color));
        drawBitmap(canvas, bitmap, box);
        bitmapPaint.setColorFilter(null);
    }

    private void drawTintedBitmapAt(
            Canvas canvas, Bitmap bitmap, float centerX, float centerY, int color) {
        bitmapPaint.setColorFilter(tintFilter(color));
        drawBitmapAt(canvas, bitmap, centerX, centerY);
        bitmapPaint.setColorFilter(null);
    }

    private ColorMatrixColorFilter tintFilter(int color) {
        ColorMatrixColorFilter cached = tintFilters.get(color);
        if (cached == null) {
            cached = new ColorMatrixColorFilter(new ColorMatrix(new float[] {
                    Color.red(color) / 255f, 0f, 0f, 0f, 0f,
                    0f, Color.green(color) / 255f, 0f, 0f, 0f,
                    0f, 0f, Color.blue(color) / 255f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f}));
            tintFilters.put(color, cached);
        }
        return cached;
    }

    /**
     * 九宫格拉伸，四角保持原始像素。cap insets 来自 CSB 的 ImageViewOptions，
     * _KW_BG_1/_KW_BG_2 在 50x50 贴图上四边各 16。
     */
    private void drawNineSlice(
            Canvas canvas, Bitmap bitmap, DailyMissionLayout.Box box, float inset) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int cut = Math.round(inset);
        if (cut * 2 >= width || cut * 2 >= height
                || box.width() < inset * 2f || box.height() < inset * 2f) {
            drawBitmap(canvas, bitmap, box);
            return;
        }
        int[] sourceX = {0, cut, width - cut, width};
        int[] sourceY = {0, cut, height - cut, height};
        float[] destinationX = {box.left(), box.left() + inset, box.right() - inset, box.right()};
        float[] destinationY = {box.top(), box.top() + inset, box.bottom() - inset, box.bottom()};
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                source.set(sourceX[column], sourceY[row], sourceX[column + 1], sourceY[row + 1]);
                nineSliceDestination.set(
                        destinationX[column], destinationY[row],
                        destinationX[column + 1], destinationY[row + 1]);
                canvas.drawBitmap(bitmap, source, nineSliceDestination, bitmapPaint);
            }
        }
    }
}
