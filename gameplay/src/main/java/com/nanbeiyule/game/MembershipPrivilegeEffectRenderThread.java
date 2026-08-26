package com.nanbeiyule.game;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.util.Log;
import java.util.List;

final class MembershipPrivilegeEffectRenderThread extends Thread {
    private static final String TAG = "MemberPrivilegeFx";
    private final SurfaceTexture surfaceTexture;
    private final List<OriginalLobbyEffectInstance> instances;
    private final long animationStartedNanos;
    private volatile boolean running = true;
    private volatile int width;
    private volatile int height;
    private volatile float contentOffsetX;
    private volatile float contentOffsetY;

    MembershipPrivilegeEffectRenderThread(
            SurfaceTexture surfaceTexture,
            int width,
            int height,
            OriginalLobbyEffectInstance instance) {
        this(surfaceTexture, width, height, List.of(instance), System.nanoTime());
    }

    MembershipPrivilegeEffectRenderThread(
            SurfaceTexture surfaceTexture,
            int width,
            int height,
            List<OriginalLobbyEffectInstance> instances) {
        this(surfaceTexture, width, height, instances, System.nanoTime());
    }

    MembershipPrivilegeEffectRenderThread(
            SurfaceTexture surfaceTexture,
            int width,
            int height,
            List<OriginalLobbyEffectInstance> instances,
            long animationStartedNanos) {
        super("membership-privilege-effect");
        this.surfaceTexture = surfaceTexture;
        this.width = width;
        this.height = height;
        this.instances = List.copyOf(instances);
        this.animationStartedNanos = animationStartedNanos;
    }

    void setViewport(int width, int height) {
        this.width = width;
        this.height = height;
    }

    void setContentOffsetX(float contentOffsetX) {
        this.contentOffsetX = Math.max(0.0f, contentOffsetX);
    }

    void setContentOffsetY(float contentOffsetY) {
        this.contentOffsetY = Math.max(0.0f, contentOffsetY);
    }

    void requestStop() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        EGLDisplay display = EGL14.EGL_NO_DISPLAY;
        EGLContext context = EGL14.EGL_NO_CONTEXT;
        EGLSurface surface = EGL14.EGL_NO_SURFACE;
        MembershipPrivilegeEffectRenderer renderer = null;
        try {
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            int[] versions = new int[2];
            require(
                    display != EGL14.EGL_NO_DISPLAY
                            && EGL14.eglInitialize(display, versions, 0, versions, 1),
                    "Unable to initialize EGL");
            EGLConfig config = chooseConfig(display);
            context =
                    EGL14.eglCreateContext(
                            display,
                            config,
                            EGL14.EGL_NO_CONTEXT,
                            new int[] {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE},
                            0);
            require(context != EGL14.EGL_NO_CONTEXT, "Unable to create EGL context");
            surface =
                    EGL14.eglCreateWindowSurface(
                            display,
                            config,
                            surfaceTexture,
                            new int[] {EGL14.EGL_NONE},
                            0);
            require(surface != EGL14.EGL_NO_SURFACE, "Unable to create EGL surface");
            require(
                    EGL14.eglMakeCurrent(display, surface, surface, context),
                    "Unable to make EGL current");

            renderer =
                    new MembershipPrivilegeEffectRenderer(instances, animationStartedNanos);
            renderer.initialize();
            long nextFrame = System.nanoTime();
            while (running) {
                renderer.draw(width, height, contentOffsetX, contentOffsetY);
                if (!EGL14.eglSwapBuffers(display, surface)) {
                    throw new IllegalStateException("Unable to present membership effect frame");
                }
                nextFrame += 33_333_333L;
                long sleepNanos = nextFrame - System.nanoTime();
                if (sleepNanos > 0L) {
                    Thread.sleep(sleepNanos / 1_000_000L, (int) (sleepNanos % 1_000_000L));
                } else {
                    nextFrame = System.nanoTime();
                }
            }
        } catch (InterruptedException exception) {
            if (running) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "Membership effect renderer interrupted", exception);
            }
        } catch (Exception exception) {
            Log.e(TAG, "Membership effect renderer stopped unexpectedly", exception);
        } finally {
            if (renderer != null && display != EGL14.EGL_NO_DISPLAY) {
                renderer.release();
            } else {
                OriginalLobbyEffectView.recycleInstances(instances);
            }
            if (display != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(
                        display,
                        EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_CONTEXT);
                if (surface != EGL14.EGL_NO_SURFACE) {
                    EGL14.eglDestroySurface(display, surface);
                }
                if (context != EGL14.EGL_NO_CONTEXT) {
                    EGL14.eglDestroyContext(display, context);
                }
                EGL14.eglTerminate(display);
            }
        }
    }

    private static EGLConfig chooseConfig(EGLDisplay display) {
        int[] attributes = {
            EGL14.EGL_RENDERABLE_TYPE, 4,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] count = new int[1];
        require(
                EGL14.eglChooseConfig(
                                display,
                                attributes,
                                0,
                                configs,
                                0,
                                configs.length,
                                count,
                                0)
                        && count[0] > 0,
                "Unable to choose EGL configuration");
        return configs[0];
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(
                    message + " (EGL 0x" + Integer.toHexString(EGL14.eglGetError()) + ")");
        }
    }
}
