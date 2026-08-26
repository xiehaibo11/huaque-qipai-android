package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Transparent overlay for original SxvipDailyGiftView zzb_hy_lblq fireworks. */
final class MembershipDailyGiftEffectView extends TextureView
        implements TextureView.SurfaceTextureListener {
    enum Layer {
        BACK,
        FRONT
    }

    private static final String TAG = "MembershipDailyGiftFx";
    private static final String ASSET_DIRECTORY = "membership_daily_gift_effects/zzb_hy_lblq";
    private static final String BASE_NAME = "zzb_hy_lblq";
    private static final OriginalLobbyEffectSpec BACK_SPEC =
            new OriginalLobbyEffectSpec(
                    ASSET_DIRECTORY,
                    BASE_NAME,
                    "hou",
                    940.0f,
                    610.0f,
                    1.0f,
                    Set.of());
    private static final OriginalLobbyEffectSpec FRONT_SPEC =
            new OriginalLobbyEffectSpec(
                    ASSET_DIRECTORY,
                    BASE_NAME,
                    "qian",
                    960.0f,
                    600.0f,
                    1.0f,
                    Set.of());

    private final Layer layer;
    private final ExecutorService assetExecutor = Executors.newSingleThreadExecutor();
    private final Object stateLock = new Object();
    private List<OriginalLobbyEffectInstance> loadedInstances;
    private SurfaceTexture availableSurface;
    private MembershipPrivilegeEffectRenderThread renderThread;
    private int surfaceWidth;
    private int surfaceHeight;
    private boolean detached;
    private boolean assetsLoading = true;

    MembershipDailyGiftEffectView(Context context, Layer layer) {
        super(context);
        this.layer = layer;
        setOpaque(false);
        setClickable(false);
        setFocusable(false);
        setSurfaceTextureListener(this);
        assetExecutor.execute(this::loadAssets);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        synchronized (stateLock) {
            availableSurface = surface;
            surfaceWidth = width;
            surfaceHeight = height;
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
        MembershipPrivilegeEffectRenderThread thread;
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
        // EGL render thread drives presentation.
    }

    @Override
    protected void onDetachedFromWindow() {
        MembershipPrivilegeEffectRenderThread thread;
        List<OriginalLobbyEffectInstance> abandoned;
        synchronized (stateLock) {
            detached = true;
            thread = renderThread;
            renderThread = null;
            abandoned = loadedInstances;
            loadedInstances = null;
        }
        stopRenderer(thread);
        OriginalLobbyEffectView.recycleInstances(abandoned);
        assetExecutor.shutdownNow();
        super.onDetachedFromWindow();
    }

    private void loadAssets() {
        List<OriginalLobbyEffectInstance> instances = new ArrayList<>();
        try {
            for (OriginalLobbyEffectSpec spec : specsFor(layer)) {
                instances.add(
                        new OriginalLobbyEffectInstance(
                                OriginalLobbyEffectAssets.load(getContext().getAssets(), spec),
                                spec));
            }
            synchronized (stateLock) {
                assetsLoading = false;
                if (detached) {
                    OriginalLobbyEffectView.recycleInstances(instances);
                    return;
                }
                loadedInstances = instances;
                startRendererIfReadyLocked();
            }
        } catch (Exception exception) {
            OriginalLobbyEffectView.recycleInstances(instances);
            synchronized (stateLock) {
                assetsLoading = false;
            }
            Log.e(TAG, "Unable to load recovered original daily gift fireworks", exception);
        }
    }

    private static List<OriginalLobbyEffectSpec> specsFor(Layer layer) {
        return switch (layer) {
            case BACK -> List.of(BACK_SPEC);
            case FRONT -> List.of(FRONT_SPEC);
        };
    }

    private void startRendererIfReadyLocked() {
        if (detached
                || assetsLoading
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
                new MembershipPrivilegeEffectRenderThread(
                        availableSurface,
                        surfaceWidth,
                        surfaceHeight,
                        instances);
        renderThread.start();
    }

    private void stopRenderer(MembershipPrivilegeEffectRenderThread thread) {
        if (thread == null) {
            return;
        }
        thread.requestStop();
        try {
            thread.join(1500L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            Log.w(TAG, "Interrupted while stopping daily gift effect renderer", exception);
        }
    }
}
