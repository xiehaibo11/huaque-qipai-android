package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import com.nanbeiyule.game.goldroom.GoldChooseRoomLayout;
import com.nanbeiyule.game.goldroom.GoldHallActEntry;
import com.nanbeiyule.game.goldroom.GoldHallActEntryGroup;
import com.nanbeiyule.game.goldroom.GoldHallChromeLayout;
import com.nanbeiyule.game.goldroom.GoldQuickStartSelector;
import com.nanbeiyule.game.goldroom.GoldRoomConf;
import com.nanbeiyule.game.goldroom.GoldRoomLevel;
import java.util.List;

/**
 * The native gold-room choose-room page (原版 GoldNew 选场态).
 *
 * <p>Draws in 1920x1080 design space and scales uniformly to the view, matching the original
 * fixed design resolution. Card geometry and hit boxes come from {@link GoldChooseRoomLayout}.
 */
final class GoldChooseRoomView extends View {
    interface OnLevelSelectedListener {
        void onLevelSelected(GoldRoomLevel level);
    }

    interface OnBackRequestedListener {
        void onBackRequested();
    }

    /** 右上活动入口组 {@code _menuBarTopAct} 的点击。 */
    interface OnActEntrySelectedListener {
        void onActEntrySelected(GoldHallActEntry entry);
    }

    /** 标题右侧「?」{@code _btnCurGameRule} 的点击，原版 {@code onClickGameRule}。 */
    interface OnRuleRequestedListener {
        void onRuleRequested();
    }

    private final GoldChooseRoomBitmaps bitmaps;
    private final GoldChooseRoomRenderer renderer;
    private final GoldHallChromeRenderer chromeRenderer;
    private final GoldChooseRoomEffects effects;
    private final long startUptimeMillis = android.os.SystemClock.uptimeMillis();
    private final Paint statusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private GameHomeState.Wallet wallet;
    private GoldRoomConf conf;
    private String statusText = "正在加载房间信息...";
    private int selectedIndex = 0;
    private float scale = 1.0f;
    private float offsetX;
    private float offsetY;
    private OnLevelSelectedListener levelSelectedListener;
    private OnBackRequestedListener backRequestedListener;
    private OnActEntrySelectedListener actEntrySelectedListener;
    private OnRuleRequestedListener ruleRequestedListener;

    /** 活动组构成与红点由外部按服务端状态设置，原版同样是先拉状态再决定显隐。 */
    void setActEntries(GoldHallActEntryGroup group, java.util.Set<GoldHallActEntry> redPoints) {
        chromeRenderer.setActEntries(group, redPoints);
        invalidate();
    }

    GoldChooseRoomView(Context context) {
        super(context);
        bitmaps = new GoldChooseRoomBitmaps(context);
        renderer = new GoldChooseRoomRenderer(bitmaps, context.getResources());
        effects = new GoldChooseRoomEffects(context.getAssets());
        chromeRenderer =
                new GoldHallChromeRenderer(
                        bitmaps,
                        effects,
                        android.graphics.BitmapFactory.decodeResource(
                                context.getResources(),
                                R.drawable.time_login_act_entry_icon),
                        android.graphics.BitmapFactory.decodeResource(
                                context.getResources(),
                                R.drawable.time_login_act_entry_red_point));
        statusPaint.setColor(0xFFFFFFFF);
        statusPaint.setTextAlign(Paint.Align.CENTER);
        statusPaint.setTextSize(44.0f);
    }

    /** Authoritative wallet from the game-home state; nothing is rendered until it arrives. */
    void setWallet(GameHomeState.Wallet wallet) {
        this.wallet = wallet;
        this.selectedIndex = selectedIndexForCurrentWallet();
        invalidate();
    }

    void setOnLevelSelectedListener(OnLevelSelectedListener listener) {
        levelSelectedListener = listener;
    }

    void setOnBackRequestedListener(OnBackRequestedListener listener) {
        backRequestedListener = listener;
    }

    void setOnActEntrySelectedListener(OnActEntrySelectedListener listener) {
        actEntrySelectedListener = listener;
    }

    void setOnRuleRequestedListener(OnRuleRequestedListener listener) {
        ruleRequestedListener = listener;
    }

    void setConf(GoldRoomConf conf) {
        this.conf = conf;
        this.statusText = null;
        this.selectedIndex = selectedIndexForCurrentWallet();
        invalidate();
    }

    void setStatusText(String statusText) {
        this.statusText = statusText;
        invalidate();
    }

    /** Releases every original bitmap; the owner calls this when the page is dismissed. */
    void release() {
        bitmaps.recycle();
        effects.release();
    }

    /** Spine clock; the card skeletons loop, so a monotonic elapsed time is enough. */
    private float elapsedSeconds() {
        return (android.os.SystemClock.uptimeMillis() - startUptimeMillis) / 1000.0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        GoldChooseRoomViewport.Transform backdrop =
                GoldChooseRoomViewport.backdrop(getWidth(), getHeight());
        canvas.save();
        canvas.translate(backdrop.offsetX(), backdrop.offsetY());
        canvas.scale(backdrop.scale(), backdrop.scale());
        renderer.drawBackdrop(canvas);
        canvas.restore();
        updateProjection();
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scale, scale);
        if (conf != null) {
            renderer.drawCards(
                    canvas,
                    conf.levels(),
                    selectedIndex,
                    conf.showsPlayerCount(),
                    effects,
                    elapsedSeconds());
        }
        drawChrome(canvas);
        if ((effects.available() && conf != null) || hasAnimatedActEntry()) {
            postInvalidateOnAnimation();
        }
        if (statusText != null) {
            canvas.drawText(
                    statusText,
                    GoldChooseRoomLayout.DESIGN_WIDTH / 2.0f,
                    GoldChooseRoomLayout.DESIGN_HEIGHT / 2.0f,
                    statusPaint);
        }
        canvas.restore();
    }

    /** 骨骼驱动的活动入口需要持续重绘，与卡面动效是否可用无关。 */
    private boolean hasAnimatedActEntry() {
        for (GoldHallActEntry entry : chromeRenderer.actEntryGroup().visible()) {
            if (effects.has(entry.spineSkeleton())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Top bar and quick-start button from {@code GoldLayer.csb}. The activity strip
     * ({@code _menuBarTopAct}) is server-configured in the original; the four entries recovered
     * from the 1.5.4 device screenshot are drawn from their own button CSBs.
     */
    private void drawChrome(Canvas canvas) {
        chromeRenderer.drawTopBar(
                canvas, conf == null ? null : conf.displayName(), wallet, elapsedSeconds());
        if (conf != null && !conf.levels().isEmpty()) {
            GoldRoomLevel level = conf.levels().get(selectedIndex);
            chromeRenderer.drawQuickStart(
                    canvas, conf.displayName() + " " + levelName(selectedIndex, level));
        }
    }

    /**
     * {@code GlobalDefine.GOLD_LEVEL_DEFAULT_NAME}: the default 档位 names shown when the server
     * sends no override.
     */
    private static String levelName(int index, GoldRoomLevel level) {
        String[] names = {"新手场", "进阶场", "高级场", "大师场", "土豪场"};
        int slot = level.uiType() - 1;
        if (slot < 0 || slot >= names.length) {
            slot = Math.min(Math.max(index, 0), names.length - 1);
        }
        return names[slot];
    }

    private int selectedIndexForCurrentWallet() {
        return conf == null
                ? 0
                : GoldQuickStartSelector.selectIndex(conf, wallet == null ? 0L : wallet.coins());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return event.getActionMasked() == MotionEvent.ACTION_DOWN;
        }
        updateProjection();
        float designX = (event.getX() - offsetX) / scale;
        float designY = (event.getY() - offsetY) / scale;
        if (GoldHallChromeLayout.backContains(designX, designY)) {
            if (backRequestedListener != null) {
                backRequestedListener.onBackRequested();
            }
            return true;
        }
        // 原版 _btnCurGameRule 与返回键同属 _panelLT，房间目录未到位时也可点开规则。
        if (GoldHallChromeLayout.ruleContains(designX, designY)) {
            if (ruleRequestedListener != null) {
                ruleRequestedListener.onRuleRequested();
            }
            return true;
        }
        // 活动入口的 _panel 满铺 160x160，原版不管房间目录是否到位都可点。
        GoldHallActEntry actEntry = chromeRenderer.actEntryGroup().at(designX, designY);
        if (actEntry != null) {
            if (actEntrySelectedListener != null) {
                actEntrySelectedListener.onActEntrySelected(actEntry);
            }
            return true;
        }
        if (conf == null) {
            return true;
        }
        List<GoldRoomLevel> levels = conf.levels();
        if (GoldHallChromeLayout.quickStartContains(designX, designY) && !levels.isEmpty()) {
            selectedIndex = selectedIndexForCurrentWallet();
            if (levelSelectedListener != null) {
                levelSelectedListener.onLevelSelected(levels.get(selectedIndex));
            }
            return true;
        }
        for (int index = 0; index < levels.size(); index++) {
            if (GoldChooseRoomLayout.itemContains(index, levels.size(), designX, designY)) {
                selectedIndex = index;
                invalidate();
                if (levelSelectedListener != null) {
                    levelSelectedListener.onLevelSelected(levels.get(index));
                }
                return true;
            }
        }
        return true;
    }

    private void updateProjection() {
        GoldChooseRoomViewport.Transform content =
                GoldChooseRoomViewport.content(getWidth(), getHeight());
        scale = content.scale();
        offsetX = content.offsetX();
        offsetY = content.offsetY();
    }

}
