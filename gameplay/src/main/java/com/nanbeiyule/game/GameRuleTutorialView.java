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

/** Native host for the four original 30579 ImageTextTutorial pages. */
final class GameRuleTutorialView extends View {
    interface Actions {
        void onStartGame(long gameId);
    }

    private static final int[] PAGE_IDS = {
            R.drawable.game_rule_tutorial_30579_1,
            R.drawable.game_rule_tutorial_30579_2,
            R.drawable.game_rule_tutorial_30579_3,
            R.drawable.game_rule_tutorial_30579_4
    };
    private static final Rect NEXT_SOURCE = new Rect(2, 2, 334, 110);
    private static final Rect CLOSE_SOURCE = new Rect(336, 2, 385, 51);
    private final GameRuleTutorialModel model = new GameRuleTutorialModel();
    private final Bitmap[] pages = new Bitmap[GameRuleTutorialModel.PAGE_COUNT];
    private final Bitmap controls;
    private final Actions actions;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF destination = new RectF();
    private GameRuleViewport viewport;
    private Runnable clickSound = () -> {};
    private float downX;
    private float downY;

    GameRuleTutorialView(Context context, Actions actions) {
        super(context);
        if (actions == null) throw new IllegalArgumentException("actions");
        this.actions = actions;
        for (int i = 0; i < pages.length; i++) {
            pages[i] = BitmapFactory.decodeResource(getResources(), PAGE_IDS[i]);
        }
        controls = BitmapFactory.decodeResource(getResources(),
                R.drawable.game_rule_tutorial_30579_controls);
        textPaint.setTypeface(Typeface.createFromAsset(
                context.getAssets(), "fonts/zihun_jingdian_lihei.ttf"));
        setVisibility(GONE);
        setContentDescription("暗斗双扣图文教程，共4页");
    }

    void setViewport(GameRuleViewport viewport) {
        this.viewport = viewport;
        invalidate();
    }

    void setClickSound(Runnable sound) {
        clickSound = sound == null ? () -> {} : sound;
    }

    void open() {
        model.reset();
        setVisibility(VISIBLE);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewport == null) return;
        canvas.drawColor(Color.argb(76, 0, 0, 0));
        int save = canvas.save();
        canvas.translate(viewport.left(), viewport.top());
        canvas.scale(viewport.scale(), viewport.scale());
        drawPage(canvas);
        drawControls(canvas);
        canvas.restoreToCount(save);
    }

    private void drawPage(Canvas canvas) {
        destination.set(GameRuleTutorialLayout.PANEL_LEFT, GameRuleTutorialLayout.PANEL_TOP,
                GameRuleTutorialLayout.PANEL_LEFT + GameRuleTutorialLayout.PANEL_WIDTH,
                GameRuleTutorialLayout.PANEL_TOP + GameRuleTutorialLayout.PANEL_HEIGHT);
        canvas.drawBitmap(pages[model.pageIndex()], null, destination, bitmapPaint);
    }

    private void drawControls(Canvas canvas) {
        destination.set(GameRuleTutorialLayout.NEXT_LEFT, GameRuleTutorialLayout.NEXT_TOP,
                GameRuleTutorialLayout.NEXT_LEFT + GameRuleTutorialLayout.NEXT_WIDTH,
                GameRuleTutorialLayout.NEXT_TOP + GameRuleTutorialLayout.NEXT_HEIGHT);
        canvas.drawBitmap(controls, NEXT_SOURCE, destination, bitmapPaint);
        destination.set(GameRuleTutorialLayout.CLOSE_LEFT, GameRuleTutorialLayout.CLOSE_TOP,
                GameRuleTutorialLayout.CLOSE_LEFT + GameRuleTutorialLayout.CLOSE_SIZE,
                GameRuleTutorialLayout.CLOSE_TOP + GameRuleTutorialLayout.CLOSE_SIZE);
        canvas.drawBitmap(controls, CLOSE_SOURCE, destination, bitmapPaint);
        drawCentered(canvas, model.isLastPage() ? "开始游戏" : "下一页");
        float centerX = GameRuleLayout.DESIGN_WIDTH * 0.5f;
        float y = GameRuleTutorialLayout.PANEL_TOP + GameRuleTutorialLayout.PANEL_HEIGHT
                - GameRuleTutorialLayout.INDICATOR_BOTTOM_MARGIN;
        float first = centerX - GameRuleTutorialLayout.INDICATOR_SPACING * 1.5f;
        for (int i = 0; i < GameRuleTutorialModel.PAGE_COUNT; i++) {
            indicatorPaint.setColor(i == model.pageIndex() ? Color.WHITE : 0x77FFFFFF);
            canvas.drawCircle(first + i * GameRuleTutorialLayout.INDICATOR_SPACING, y, 7f,
                    indicatorPaint);
        }
    }

    private void drawCentered(Canvas canvas, String value) {
        textPaint.setTextSize(56f);
        textPaint.setColor(Color.rgb(206, 92, 4));
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float y = GameRuleTutorialLayout.NEXT_TOP
                + GameRuleTutorialLayout.NEXT_HEIGHT * 0.5f
                - (metrics.ascent + metrics.descent) * 0.5f;
        canvas.drawText(value, GameRuleTutorialLayout.NEXT_LEFT
                + GameRuleTutorialLayout.NEXT_WIDTH * 0.5f, y, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (viewport == null) return true;
        float x = viewport.unmapX(event.getX());
        float y = viewport.unmapY(event.getY());
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            downX = x;
            downY = y;
            return true;
        }
        if (event.getActionMasked() != MotionEvent.ACTION_UP) return true;
        if (GameRuleTutorialLayout.closeContains(x, y)) {
            clicked();
            setVisibility(GONE);
        } else if (GameRuleTutorialLayout.nextContains(x, y)) {
            clicked();
            if (model.next() == GameRuleTutorialModel.Next.START_GAME) {
                setVisibility(GONE);
                actions.onStartGame(GameRuleTutorialModel.GAME_ID);
            }
            invalidate();
        } else if (Math.abs(x - downX) > 80f && Math.abs(y - downY) < 200f) {
            if (x < downX) model.select(model.pageIndex() + 1); else model.previous();
            invalidate();
        }
        return true;
    }

    private void clicked() {
        performClick();
        clickSound.run();
    }

    @Override public boolean performClick() { super.performClick(); return true; }

    void release() {
        for (Bitmap page : pages) recycle(page);
        recycle(controls);
    }

    private static void recycle(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
    }
}
