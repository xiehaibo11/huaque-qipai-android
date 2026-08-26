package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Transparent layer for the original SxvipPrivilegeLayer _KW_ANI_ROOT spine. */
final class MembershipPrivilegeEffectView extends TextureView
        implements TextureView.SurfaceTextureListener {
    private static final String TAG = "MemberPrivilegeFx";
    private static final OriginalLobbyEffectSpec SPEC =
            new OriginalLobbyEffectSpec(
                    "membership_effects/zzb_hy_czhy",
                    "zzb_hy_czhy",
                    "loop",
                    960.0f,
                    540.0f,
                    1.0f,
                    Set.of());

    private final ExecutorService assetExecutor = Executors.newSingleThreadExecutor();
    private final Object stateLock = new Object();
    private OriginalLobbyEffectInstance loadedInstance;
    private SurfaceTexture availableSurface;
    private MembershipPrivilegeEffectRenderThread renderThread;
    private int surfaceWidth;
    private int surfaceHeight;
    private boolean detached;
    private boolean assetsLoading = true;

    MembershipPrivilegeEffectView(Context context) {
        super(context);
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
        OriginalLobbyEffectInstance abandoned;
        synchronized (stateLock) {
            detached = true;
            thread = renderThread;
            renderThread = null;
            abandoned = loadedInstance;
            loadedInstance = null;
        }
        stopRenderer(thread);
        recycleInstance(abandoned);
        assetExecutor.shutdownNow();
        super.onDetachedFromWindow();
    }

    private void loadAssets() {
        try {
            OriginalLobbyEffectInstance instance =
                    new OriginalLobbyEffectInstance(
                            OriginalLobbyEffectAssets.load(getContext().getAssets(), SPEC),
                            SPEC);
            synchronized (stateLock) {
                assetsLoading = false;
                if (detached) {
                    recycleInstance(instance);
                    return;
                }
                loadedInstance = instance;
                startRendererIfReadyLocked();
            }
        } catch (Exception exception) {
            synchronized (stateLock) {
                assetsLoading = false;
            }
            Log.e(TAG, "Unable to load recovered original membership effect", exception);
        }
    }

    private void startRendererIfReadyLocked() {
        if (detached
                || assetsLoading
                || renderThread != null
                || availableSurface == null
                || loadedInstance == null
                || surfaceWidth <= 0
                || surfaceHeight <= 0) {
            return;
        }
        OriginalLobbyEffectInstance instance = loadedInstance;
        loadedInstance = null;
        renderThread =
                new MembershipPrivilegeEffectRenderThread(
                        availableSurface,
                        surfaceWidth,
                        surfaceHeight,
                        instance);
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
            Log.w(TAG, "Interrupted while stopping membership effect renderer", exception);
        }
    }

    static void recycleInstance(OriginalLobbyEffectInstance instance) {
        if (instance != null) {
            instance.recyclePendingBitmaps();
        }
    }
}
