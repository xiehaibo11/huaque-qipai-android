package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** Original 18-game selector around the official Zhejiang rule WebView. */
@SuppressLint("ViewConstructor")
final class GameRuleView extends ViewGroup {
    interface Actions {
        void onDismissRequested();
        void onGameSelected(GameRuleCatalog.Entry entry);
        void onImageTutorialRequested(long gameId);
        void onTutorialStartGameRequested(long gameId);
    }

    private static final String OFFICIAL_ROOT =
            "https://wechat.hzxuanming.com/game_center/game_rule/";
    private final GameRuleState state = new GameRuleState(GameRuleCatalog.taizhou());
    private final GameRuleRenderer renderer;
    private final WebView webView;
    private final GameRuleTutorialView tutorial;
    private final Actions actions;
    private final int touchSlop;
    private Runnable clickSound = () -> {};
    private AdaptiveViewport.Insets safeInsets = AdaptiveViewport.Insets.NONE;
    private GameRuleViewport viewport;
    private float listScroll;
    private float startX;
    private float startY;
    private float lastY;
    private boolean dragging;
    private String failedUrl;

    @SuppressLint("SetJavaScriptEnabled")
    GameRuleView(Context context, Actions actions) {
        super(context);
        if (actions == null) throw new IllegalArgumentException("actions");
        this.actions = actions;
        renderer = new GameRuleRenderer(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setWillNotDraw(false);
        setContentDescription("规则，18款游戏目录");
        webView = new WebView(context);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(false);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setWebViewClient(new RuleClient());
        addView(webView);
        tutorial = new GameRuleTutorialView(context, new GameRuleTutorialView.Actions() {
            @Override public void onStartGame(long gameId) {
                actions.onTutorialStartGameRequested(gameId);
            }
        });
        addView(tutorial);
        loadSelected();
    }

    void setClickSound(Runnable sound) {
        clickSound = sound == null ? () -> {} : sound;
        tutorial.setClickSound(sound);
    }

    void release() {
        webView.stopLoading();
        webView.setWebViewClient(new WebViewClient());
        webView.destroy();
        tutorial.release();
        renderer.release();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        updateProjection(width, height);
        webView.measure(
                MeasureSpec.makeMeasureSpec(Math.round(
                                GameRuleLayout.CONTENT_WIDTH * viewport.scale()),
                        MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(Math.round(
                                GameRuleLayout.CONTENT_HEIGHT * viewport.scale()),
                        MeasureSpec.EXACTLY));
        tutorial.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        updateProjection(right - left, bottom - top);
        int childLeft = Math.round(viewport.mapX(GameRuleLayout.CONTENT_LEFT));
        int childTop = Math.round(viewport.mapY(GameRuleLayout.CONTENT_TOP));
        webView.layout(childLeft, childTop,
                childLeft + webView.getMeasuredWidth(), childTop + webView.getMeasuredHeight());
        tutorial.layout(0, 0, right - left, bottom - top);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int save = canvas.save();
        canvas.translate(viewport.left(), viewport.top());
        canvas.scale(viewport.scale(), viewport.scale());
        renderer.drawChrome(canvas, state, listScroll);
        canvas.restoreToCount(save);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (state.content() == GameRuleState.Content.DOCUMENT
                || tutorial.getVisibility() == VISIBLE) return;
        int save = canvas.save();
        canvas.translate(viewport.left(), viewport.top());
        canvas.scale(viewport.scale(), viewport.scale());
        renderer.drawStatus(canvas, state);
        canvas.restoreToCount(save);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (tutorial.getVisibility() == VISIBLE) return false;
        float x = designX(event);
        float y = designY(event);
        return GameRuleLayout.closeContains(x, y)
                || x <= GameRuleLayout.CONTENT_LEFT
                || y <= GameRuleLayout.CONTENT_TOP;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = designX(event);
        float y = designY(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                startX = x;
                startY = y;
                lastY = y;
                dragging = false;
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (!dragging && Math.abs(y - startY) * viewport.scale() > touchSlop) dragging = true;
                if (dragging && startX <= GameRuleLayout.LIST_LEFT + GameRuleLayout.LIST_WIDTH) {
                    listScroll = GameRuleLayout.clampListScroll(
                            listScroll + lastY - y, state.entries().size());
                    invalidate();
                }
                lastY = y;
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                if (!dragging) handleTap(x, y);
                dragging = false;
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                dragging = false;
                return true;
            }
            default -> { return true; }
        }
    }

    @Override public boolean performClick() { super.performClick(); return true; }

    private void handleTap(float x, float y) {
        if (GameRuleLayout.closeContains(x, y)) {
            clicked();
            actions.onDismissRequested();
            return;
        }
        if (state.selected().gameId() == GameRuleLayout.IMAGE_TUTORIAL_GAME_ID
                && GameRuleLayout.imageTutorialContains(x, y)) {
            clicked();
            actions.onImageTutorialRequested(state.selected().gameId());
            tutorial.open();
            return;
        }
        int selected = GameRuleLayout.itemAt(x, y, listScroll, state.entries().size());
        if (selected < 0 || selected == state.selectedIndex()) return;
        clicked();
        state.select(selected);
        actions.onGameSelected(state.selected());
        loadSelected();
    }

    private void loadSelected() {
        state.beginLoad();
        failedUrl = null;
        webView.setVisibility(INVISIBLE);
        invalidate();
        webView.loadUrl(state.selected().ruleUrl());
    }

    private void showDocument(String url) {
        if (!state.selected().ruleUrl().equals(url) || url.equals(failedUrl)) return;
        state.show();
        webView.setVisibility(VISIBLE);
        invalidate();
    }

    private void showFailure(String url, boolean missing, String message) {
        if (!state.selected().ruleUrl().equals(url)) return;
        failedUrl = url;
        if (missing) state.missing(); else state.error(message);
        webView.setVisibility(INVISIBLE);
        invalidate();
    }

    private void clicked() {
        performClick();
        clickSound.run();
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        AdaptiveViewport.Insets updated = AdaptiveCanvasView.safeInsetsFrom(insets);
        if (!updated.equals(safeInsets)) {
            safeInsets = updated;
            requestLayout();
        }
        return super.onApplyWindowInsets(insets);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow(); requestApplyInsets();
    }

    private float designX(MotionEvent event) { return viewport.unmapX(event.getX()); }
    private float designY(MotionEvent event) { return viewport.unmapY(event.getY()); }

    private void updateProjection(float width, float height) {
        if (width <= 0f || height <= 0f) return;
        viewport = GameRuleViewport.fit(width, height, safeInsets);
        tutorial.setViewport(viewport);
    }

    private final class RuleClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            return !isOfficialHttps(url);
        }

        @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return !isOfficialHttps(url);
        }

        @Override public void onPageFinished(WebView view, String url) { showDocument(url); }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request,
                WebResourceResponse response) {
            if (Build.VERSION.SDK_INT >= 23 && request.isForMainFrame()) {
                showFailure(request.getUrl().toString(), response.getStatusCode() == 404,
                        "规则服务暂时不可用");
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request,
                WebResourceError error) {
            if (Build.VERSION.SDK_INT >= 23 && request.isForMainFrame()) {
                showFailure(request.getUrl().toString(), false, "规则加载失败，请检查网络后重试");
            }
        }
    }

    private static boolean isOfficialHttps(String url) {
        Uri uri = Uri.parse(url);
        return "https".equals(uri.getScheme())
                && "wechat.hzxuanming.com".equals(uri.getHost())
                && url.startsWith(OFFICIAL_ROOT);
    }
}
