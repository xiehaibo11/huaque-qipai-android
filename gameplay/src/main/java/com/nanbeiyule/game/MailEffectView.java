package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class MailEffectView extends TextureView implements TextureView.SurfaceTextureListener {
    private static final String TAG = "MailEffect";
    private final ExecutorService assetExecutor = Executors.newSingleThreadExecutor();
    private final Object stateLock = new Object();
    private final Runnable firstFrame;
    private final MailEffectSpec spec;
    private MailEffectInstance loadedInstance;
    private SurfaceTexture surface;
    private MailEffectRenderThread renderThread;
    private int surfaceWidth;
    private int surfaceHeight;
    private boolean detached;
    private boolean loading = true;

    MailEffectView(Context context, Runnable firstFrame) {
        this(context, MailEffectSpec.mainMailbox(), firstFrame);
    }

    MailEffectView(Context context, MailEffectSpec spec, Runnable firstFrame) {
        super(context);
        this.spec = spec;
        this.firstFrame = firstFrame;
        setOpaque(false);
        setSurfaceTextureListener(this);
        setClickable(false);
        setFocusable(false);
        assetExecutor.execute(this::loadAssets);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture value, int width, int height) {
        synchronized (stateLock) {
            surface = value;
            surfaceWidth = width;
            surfaceHeight = height;
            startIfReady();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture value, int width, int height) {
        synchronized (stateLock) {
            surfaceWidth = width;
            surfaceHeight = height;
            if (renderThread != null) renderThread.setViewport(width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture value) {
        MailEffectRenderThread thread;
        synchronized (stateLock) {
            surface = null;
            thread = renderThread;
            renderThread = null;
        }
        stop(thread);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture value) {}

    @Override
    protected void onDetachedFromWindow() {
        MailEffectRenderThread thread;
        MailEffectInstance abandoned;
        synchronized (stateLock) {
            detached = true;
            thread = renderThread;
            renderThread = null;
            abandoned = loadedInstance;
            loadedInstance = null;
        }
        stop(thread);
        if (abandoned != null) abandoned.recyclePendingBitmaps();
        assetExecutor.shutdownNow();
        super.onDetachedFromWindow();
    }

    private void loadAssets() {
        try {
            MailEffectInstance instance = new MailEffectInstance(
                    OriginalLobbyEffectAssets.load(
                            getContext().getAssets(), spec.assetDirectory(), spec.baseName()),
                    spec);
            synchronized (stateLock) {
                loading = false;
                if (detached) {
                    instance.recyclePendingBitmaps();
                    return;
                }
                loadedInstance = instance;
                startIfReady();
            }
        } catch (Exception exception) {
            synchronized (stateLock) {
                loading = false;
            }
            Log.e(TAG, "Unable to load recovered mail effect", exception);
        }
    }

    private void startIfReady() {
        if (detached || loading || renderThread != null || surface == null
                || loadedInstance == null || surfaceWidth <= 0 || surfaceHeight <= 0) {
            return;
        }
        MailEffectInstance instance = loadedInstance;
        loadedInstance = null;
        renderThread = new MailEffectRenderThread(
                surface,
                surfaceWidth,
                surfaceHeight,
                instance,
                () -> post(firstFrame));
        renderThread.start();
    }

    private static void stop(MailEffectRenderThread thread) {
        if (thread == null) return;
        thread.requestStop();
        try {
            thread.join(1500L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
