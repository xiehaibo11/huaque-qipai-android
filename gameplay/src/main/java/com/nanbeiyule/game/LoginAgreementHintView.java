package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Non-interactive transparent surface that renders the recovered agreement hand animation.
 *
 * <p>Failures are logged and leave the Canvas checkbox fully usable.
 */
public final class LoginAgreementHintView extends TextureView
        implements TextureView.SurfaceTextureListener {
    private static final String TAG = "LoginAgreementHint";
    static final long FRAME_INTERVAL_NANOS = 33_333_333L;

    private final ExecutorService assetExecutor =
            Executors.newSingleThreadExecutor();
    private final Object stateLock = new Object();

    private LoginAgreementHintAssets.Loaded loadedAssets;
    private SurfaceTexture availableSurface;
    private LoginAgreementHintRenderThread renderThread;
    private int surfaceWidth;
    private int surfaceHeight;
    private boolean detached;
    private boolean assetsLoading = true;
    private boolean hintVisible = true;

    public LoginAgreementHintView(Context context) {
        this(context, null);
    }

    public LoginAgreementHintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOpaque(false);
        setSurfaceTextureListener(this);
        setClickable(false);
        setFocusable(false);
        assetExecutor.execute(this::loadAssets);
    }

    void setHintVisible(boolean visible) {
        LoginAgreementHintRenderThread thread;
        synchronized (stateLock) {
            if (hintVisible == visible) {
                return;
            }
            hintVisible = visible;
            thread = renderThread;
        }
        setAlpha(visible ? 1.0f : 0.0f);
        if (thread != null) {
            thread.setAnimationVisible(visible);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(
            SurfaceTexture surface,
            int width,
            int height) {
        synchronized (stateLock) {
            availableSurface = surface;
            surfaceWidth = width;
            surfaceHeight = height;
            requestAssetsIfNeededLocked();
            startRendererIfReadyLocked();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(
            SurfaceTexture surface,
            int width,
            int height) {
        synchronized (stateLock) {
            surfaceWidth = width;
            surfaceHeight = height;
            if (renderThread != null) {
                renderThread.setViewport(width, height);
            }
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        LoginAgreementHintRenderThread thread;
        synchronized (stateLock) {
            availableSurface = null;
            thread = renderThread;
            renderThread = null;
        }
        stopRenderer(thread);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Frame presentation is driven by the private EGL render thread.
    }

    @Override
    protected void onDetachedFromWindow() {
        LoginAgreementHintRenderThread thread;
        LoginAgreementHintAssets.Loaded abandoned;
        synchronized (stateLock) {
            detached = true;
            thread = renderThread;
            renderThread = null;
            abandoned = loadedAssets;
            loadedAssets = null;
        }
        stopRenderer(thread);
        LoginAgreementHintAssets.recycle(abandoned);
        assetExecutor.shutdownNow();
        super.onDetachedFromWindow();
    }

    private void loadAssets() {
        try {
            LoginAgreementHintAssets.Loaded assets =
                    LoginAgreementHintAssets.load(getContext().getAssets());
            synchronized (stateLock) {
                assetsLoading = false;
                if (detached) {
                    LoginAgreementHintAssets.recycle(assets);
                    return;
                }
                loadedAssets = assets;
                startRendererIfReadyLocked();
            }
        } catch (Exception exception) {
            synchronized (stateLock) {
                assetsLoading = false;
            }
            Log.e(
                    TAG,
                    "Unable to load recovered agreement hand animation",
                    exception);
        }
    }

    private void requestAssetsIfNeededLocked() {
        if (!detached
                && availableSurface != null
                && renderThread == null
                && loadedAssets == null
                && !assetsLoading) {
            assetsLoading = true;
            assetExecutor.execute(this::loadAssets);
        }
    }

    private void startRendererIfReadyLocked() {
        if (detached
                || renderThread != null
                || availableSurface == null
                || loadedAssets == null
                || surfaceWidth <= 0
                || surfaceHeight <= 0) {
            return;
        }
        LoginAgreementHintAssets.Loaded assets = loadedAssets;
        loadedAssets = null;
        renderThread =
                new LoginAgreementHintRenderThread(
                        availableSurface,
                        surfaceWidth,
                        surfaceHeight,
                        assets,
                        hintVisible);
        renderThread.start();
    }

    private static void stopRenderer(LoginAgreementHintRenderThread thread) {
        if (thread == null) {
            return;
        }
        thread.requestStop();
        try {
            thread.join(1500L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            Log.w(
                    TAG,
                    "Interrupted while stopping agreement hint renderer",
                    exception);
        }
    }

}
