package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.http.SslError;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.nanbeiyule.game.news.ZhejiangNewsUrlPolicy;

/** Native safe-area host for the original Zhejiang news WebView surface. */
@SuppressLint("SetJavaScriptEnabled")
final class ZhejiangNewsPage extends FrameLayout {
    private final ZhejiangNewsUrlPolicy urlPolicy = new ZhejiangNewsUrlPolicy();
    private final WebView webView;
    private final View loadingView;
    private final TextView errorText;
    private final View errorView;
    private boolean released;
    private boolean mainFrameFailed;
    ZhejiangNewsPage(Context context, Runnable closeRequested) {
        super(context);
        setBackgroundColor(Color.WHITE);
        setFitsSystemWindows(false);

        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        addView(column, match());
        column.addView(toolbar(context, closeRequested),
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(60)));

        FrameLayout content = new FrameLayout(context);
        column.addView(content,
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));

        webView = new WebView(context);
        configureWebView();
        content.addView(webView, match());

        loadingView = loading(context);
        content.addView(loadingView, match());

        LinearLayout errorColumn = new LinearLayout(context);
        errorColumn.setOrientation(LinearLayout.VERTICAL);
        errorColumn.setGravity(Gravity.CENTER);
        errorColumn.setPadding(dp(24), dp(24), dp(24), dp(24));
        errorText = label(context, "新闻页面加载失败，请检查网络后重试", 18, Color.BLACK);
        errorText.setGravity(Gravity.CENTER);
        errorColumn.addView(errorText, wrap());
        Button retry = new Button(context);
        retry.setText("重新加载");
        retry.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        retry.setOnClickListener(ignored -> loadCurrentNews());
        LinearLayout.LayoutParams retryParams = new LinearLayout.LayoutParams(dp(160), dp(52));
        retryParams.topMargin = dp(18);
        errorColumn.addView(retry, retryParams);
        errorView = errorColumn;
        content.addView(errorView, match());

        setOnApplyWindowInsetsListener(
                (view, insets) -> {
                    AdaptiveViewport.Insets safe = AdaptiveCanvasView.safeInsetsFrom(insets);
                    setPadding(
                            Math.round(safe.left()),
                            Math.round(safe.top()),
                            Math.round(safe.right()),
                            Math.round(safe.bottom()));
                    return insets;
                });
        showLoading();
        loadCurrentNews();
    }
    private View toolbar(Context context, Runnable closeRequested) {
        FrameLayout bar = new FrameLayout(context);
        bar.setBackgroundColor(Color.rgb(225, 222, 222));
        TextView title = label(context, "新闻资讯", 26, Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(licensedTypeface(context));
        bar.addView(title,
                new FrameLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT,
                        Gravity.CENTER));
        TextView back = label(context, "‹  返回", 18, Color.rgb(33, 33, 33));
        back.setGravity(Gravity.CENTER);
        back.setClickable(true);
        back.setFocusable(true);
        back.setContentDescription("关闭新闻资讯");
        back.setOnClickListener(ignored -> closeRequested.run());
        FrameLayout.LayoutParams backParams =
                new FrameLayout.LayoutParams(dp(132), LayoutParams.MATCH_PARENT,
                        Gravity.END | Gravity.CENTER_VERTICAL);
        bar.addView(back, backParams);
        return bar;
    }
    private View loading(Context context) {
        LinearLayout loading = new LinearLayout(context);
        loading.setOrientation(LinearLayout.VERTICAL);
        loading.setGravity(Gravity.CENTER);
        ProgressBar spinner = new ProgressBar(context);
        loading.addView(spinner, new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView text = label(context, "正在加载中……", 18, Color.BLACK);
        LinearLayout.LayoutParams textParams = wrap();
        textParams.topMargin = dp(12);
        loading.addView(text, textParams);
        return loading;
    }
    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setGeolocationEnabled(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
        }
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);
        webView.setBackgroundColor(Color.WHITE);
        webView.setWebChromeClient(
                new WebChromeClient() {
                    @Override
                    public void onProgressChanged(WebView view, int progress) {
                        if (progress < 100 && errorView.getVisibility() != VISIBLE) {
                            loadingView.setVisibility(VISIBLE);
                        }
                    }
                });
        webView.setWebViewClient(new SafeNewsClient());
        webView.setDownloadListener(
                (url, userAgent, contentDisposition, mimeType, contentLength) ->
                        Toast.makeText(getContext(), "新闻页不允许下载文件", Toast.LENGTH_SHORT)
                                .show());
    }
    boolean navigateBack() {
        if (!released && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }
    void release() {
        if (released) {
            return;
        }
        released = true;
        webView.stopLoading();
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        webView.removeAllViews();
        webView.destroy();
    }
    private void loadCurrentNews() {
        if (released) {
            return;
        }
        showLoading();
        webView.loadUrl(ZhejiangNewsUrlPolicy.DEFAULT_URL);
    }
    private void showLoading() {
        mainFrameFailed = false;
        webView.setVisibility(INVISIBLE);
        loadingView.setVisibility(VISIBLE);
        errorView.setVisibility(GONE);
    }
    private void showContent() {
        webView.setVisibility(VISIBLE);
        loadingView.setVisibility(GONE);
        errorView.setVisibility(GONE);
    }
    private void showError(String message) {
        mainFrameFailed = true;
        webView.setVisibility(INVISIBLE);
        loadingView.setVisibility(GONE);
        errorText.setText(message);
        errorView.setVisibility(VISIBLE);
    }
    private final class SafeNewsClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (!request.isForMainFrame()) {
                return false;
            }
            return blockIfUntrusted(request.getUrl().toString());
        }
        @Override
        @SuppressWarnings("deprecation")
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return blockIfUntrusted(url);
        }
        @Override
        public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
            if (urlPolicy.permits(url)) {
                showLoading();
            }
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            if (!mainFrameFailed && urlPolicy.permits(url)) {
                showContent();
            }
        }
        @Override
        public void onReceivedError(
                WebView view, WebResourceRequest request, WebResourceError error) {
            if (request.isForMainFrame()) {
                showError("新闻页面加载失败，请检查网络后重试");
            }
        }
        @Override
        public void onReceivedHttpError(
                WebView view,
                WebResourceRequest request,
                WebResourceResponse errorResponse) {
            if (request.isForMainFrame() && errorResponse.getStatusCode() >= 400) {
                showError("新闻服务暂时不可用，请稍后重试");
            }
        }

        @Override
        public void onReceivedSslError(
                WebView view, SslErrorHandler handler, SslError error) {
            handler.cancel();
            showError("新闻页面安全连接失败");
        }

        private boolean blockIfUntrusted(String url) {
            if (urlPolicy.permits(url)) {
                return false;
            }
            Toast.makeText(getContext(), "仅支持浙江在线站内链接", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    private static TextView label(
            Context context, String value, int sizeSp, int color) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        view.setTextColor(color);
        return view;
    }

    private static Typeface licensedTypeface(Context context) {
        try {
            return Typeface.createFromAsset(
                    context.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        } catch (RuntimeException ignored) {
            return Typeface.DEFAULT_BOLD;
        }
    }

    private int dp(int value) {
        return Math.round(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        value,
                        getResources().getDisplayMetrics()));
    }

    private static LayoutParams match() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    private static LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
