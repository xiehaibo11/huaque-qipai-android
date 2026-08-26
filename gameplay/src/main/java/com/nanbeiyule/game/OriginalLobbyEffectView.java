package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class OriginalLobbyEffectView extends TextureView
        implements TextureView.SurfaceTextureListener {
    private static final String TAG = "OriginalLobbyEffect";

    private final ExecutorService assetExecutor = Executors.newSingleThreadExecutor();
    private final Object stateLock = new Object();
    private final List<OriginalLobbyEffectSpec> specs;
    private final Runnable onFirstFrameRendered;

    private List<OriginalLobbyEffectInstance> loadedInstances;
    private SurfaceTexture availableSurface;
    private OriginalLobbyEffectRenderThread renderThread;
    private int surfaceWidth;
    private int surfaceHeight;
    private boolean detached;
    private boolean assetsLoading = true;

    public OriginalLobbyEffectView(
            Context context, List<OriginalLobbyEffectSpec> specs) {
        this(context, specs, null);
    }

    private OriginalLobbyEffectView(
            Context context,
            List<OriginalLobbyEffectSpec> specs,
            Runnable onFirstFrameRendered) {
        super(context);
        this.specs = specs;
        this.onFirstFrameRendered = onFirstFrameRendered;
        setOpaque(false);
        setSurfaceTextureListener(this);
        setClickable(false);
        setFocusable(false);
        assetExecutor.execute(this::loadAssets);
    }

    public static OriginalLobbyEffectView createZhejiangBottomControls(
            Context context, Runnable onFirstFrameRendered) {
        return new OriginalLobbyEffectView(
                context,
                ZhejiangLobbyBottomEffectLayout.specs(),
                onFirstFrameRendered);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        synchronized (stateLock) {
            availableSurface = surface;
            surfaceWidth = width;
            surfaceHeight = height;
            requestAssetsIfNeededLocked();
            startRendererIfReadyLocked();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
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
        OriginalLobbyEffectRenderThread thread;
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
        OriginalLobbyEffectRenderThread thread;
        List<OriginalLobbyEffectInstance> abandoned;
        synchronized (stateLock) {
            detached = true;
            thread = renderThread;
            renderThread = null;
            abandoned = loadedInstances;
            loadedInstances = null;
        }
        stopRenderer(thread);
        recycleInstances(abandoned);
        assetExecutor.shutdownNow();
        super.onDetachedFromWindow();
    }

    private void loadAssets() {
        try {
            List<OriginalLobbyEffectInstance> instances = new ArrayList<>();
            try {
                for (OriginalLobbyEffectSpec spec : specs) {
                    instances.add(
                            new OriginalLobbyEffectInstance(
                                    OriginalLobbyEffectAssets.load(
                                            getContext().getAssets(), spec),
                                    spec));
                }
            } catch (Exception exception) {
                recycleInstances(instances);
                throw exception;
            }
            synchronized (stateLock) {
                assetsLoading = false;
                if (detached) {
                    recycleInstances(instances);
                    return;
                }
                loadedInstances = instances;
                startRendererIfReadyLocked();
            }
        } catch (Exception exception) {
            synchronized (stateLock) {
                assetsLoading = false;
            }
            Log.e(TAG, "Unable to load recovered original lobby effects", exception);
        }
    }

    private void requestAssetsIfNeededLocked() {
        if (!detached
                && availableSurface != null
                && renderThread == null
                && loadedInstances == null
                && !assetsLoading) {
            assetsLoading = true;
            assetExecutor.execute(this::loadAssets);
        }
    }

    private void startRendererIfReadyLocked() {
        if (detached
                || renderThread != null
                || availableSurface == null
                || loadedInstances == null
                || surfaceWidth <= 0
                || surfaceHeight <= 0) {
            return;
        }
        List<OriginalLobbyEffectInstance> instances = loadedInstances;
        loadedInstances = null;
        renderThread =
                new OriginalLobbyEffectRenderThread(
                        availableSurface,
                        surfaceWidth,
                        surfaceHeight,
                        instances,
                        this::postFirstFrameRendered);
        renderThread.start();
    }

    private void postFirstFrameRendered() {
        if (onFirstFrameRendered == null) {
            return;
        }
        post(
                () -> {
                    synchronized (stateLock) {
                        if (detached) {
                            return;
                        }
                    }
                    onFirstFrameRendered.run();
                });
    }

    private void stopRenderer(OriginalLobbyEffectRenderThread thread) {
        if (thread == null) {
            return;
        }
        thread.requestStop();
        try {
            thread.join(1500L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            Log.w(TAG, "Interrupted while stopping lobby effect renderer", exception);
        }
    }

    static void recycleInstances(List<OriginalLobbyEffectInstance> instances) {
        if (instances == null) {
            return;
        }
        for (OriginalLobbyEffectInstance instance : instances) {
            instance.recyclePendingBitmaps();
        }
    }
}
