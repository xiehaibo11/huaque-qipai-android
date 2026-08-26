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

final class LoginAgreementHintRenderThread extends Thread {
    private static final String TAG = "LoginAgreementHint";
    private final SurfaceTexture surfaceTexture;
    private final LoginAgreementHintAssets.Loaded assets;
    private final Object renderSignal = new Object();
    private volatile boolean running = true;
    private volatile boolean animationVisible;
    private volatile int width;
    private volatile int height;
    private volatile long animationStartedNanos;

    LoginAgreementHintRenderThread(
            SurfaceTexture surfaceTexture,
            int width,
            int height,
            LoginAgreementHintAssets.Loaded assets,
            boolean animationVisible) {
        super("login-agreement-hint");
        this.surfaceTexture = surfaceTexture;
        this.width = width;
        this.height = height;
        this.assets = assets;
        this.animationVisible = animationVisible;
        animationStartedNanos = System.nanoTime();
    }

    void setViewport(int width, int height) {
        this.width = width;
        this.height = height;
    }

    void setAnimationVisible(boolean visible) {
        synchronized (renderSignal) {
            if (visible && !animationVisible) {
                animationStartedNanos = System.nanoTime();
            }
            animationVisible = visible;
            renderSignal.notifyAll();
        }
    }

    void requestStop() {
        synchronized (renderSignal) {
            running = false;
            renderSignal.notifyAll();
        }
        interrupt();
    }

    @Override
    public void run() {
        EGLDisplay display = EGL14.EGL_NO_DISPLAY;
        EGLContext context = EGL14.EGL_NO_CONTEXT;
        EGLSurface surface = EGL14.EGL_NO_SURFACE;
        LoginAgreementHintRenderer renderer = null;
        try {
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            int[] versions = new int[2];
            require(
                    display != EGL14.EGL_NO_DISPLAY
                            && EGL14.eglInitialize(
                                    display,
                                    versions,
                                    0,
                                    versions,
                                    1),
                    "Unable to initialize EGL");
            EGLConfig config = chooseConfig(display);
            int[] contextAttributes = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE
            };
            context =
                    EGL14.eglCreateContext(
                            display,
                            config,
                            EGL14.EGL_NO_CONTEXT,
                            contextAttributes,
                            0);
            require(
                    context != EGL14.EGL_NO_CONTEXT,
                    "Unable to create EGL context");
            int[] surfaceAttributes = {EGL14.EGL_NONE};
            surface =
                    EGL14.eglCreateWindowSurface(
                            display,
                            config,
                            surfaceTexture,
                            surfaceAttributes,
                            0);
            require(
                    surface != EGL14.EGL_NO_SURFACE,
                    "Unable to create EGL surface");
            require(
                    EGL14.eglMakeCurrent(
                            display,
                            surface,
                            surface,
                            context),
                    "Unable to make EGL current");

            renderer = new LoginAgreementHintRenderer(assets);
            renderer.initialize();
            boolean transparentFramePresented = false;
            long nextFrameNanos = System.nanoTime();
            while (running) {
                if (!animationVisible) {
                    if (!transparentFramePresented) {
                        renderer.clear(width, height);
                        present(display, surface);
                        transparentFramePresented = true;
                    }
                    waitForRenderSignal();
                    nextFrameNanos = System.nanoTime();
                    continue;
                }

                transparentFramePresented = false;
                float elapsedSeconds =
                        (System.nanoTime() - animationStartedNanos)
                                / 1_000_000_000.0f;
                renderer.draw(width, height, elapsedSeconds);
                present(display, surface);
                nextFrameNanos += LoginAgreementHintView.FRAME_INTERVAL_NANOS;
                waitUntil(nextFrameNanos);
                if (nextFrameNanos < System.nanoTime() - LoginAgreementHintView.FRAME_INTERVAL_NANOS) {
                    nextFrameNanos = System.nanoTime();
                }
            }
        } catch (Exception exception) {
            if (running) {
                Log.e(
                        TAG,
                        "Agreement hand renderer stopped unexpectedly",
                        exception);
            }
        } finally {
            if (renderer != null && display != EGL14.EGL_NO_DISPLAY) {
                renderer.release();
            } else {
                LoginAgreementHintAssets.recycle(assets);
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

    private void waitForRenderSignal() throws InterruptedException {
        synchronized (renderSignal) {
            while (running && !animationVisible) {
                renderSignal.wait();
            }
        }
    }

    private void waitUntil(long deadlineNanos) throws InterruptedException {
        synchronized (renderSignal) {
            while (running && animationVisible) {
                long remaining = deadlineNanos - System.nanoTime();
                if (remaining <= 0L) {
                    return;
                }
                renderSignal.wait(
                        remaining / 1_000_000L,
                        (int) (remaining % 1_000_000L));
            }
        }
    }

    private static void present(
            EGLDisplay display,
            EGLSurface surface) {
        if (!EGL14.eglSwapBuffers(display, surface)) {
            throw new IllegalStateException(
                    "Unable to present agreement hint frame");
        }
    }

    private static EGLConfig chooseConfig(EGLDisplay display) {
        int[] attributes = {
            EGL14.EGL_RENDERABLE_TYPE,
            4,
            EGL14.EGL_RED_SIZE,
            8,
            EGL14.EGL_GREEN_SIZE,
            8,
            EGL14.EGL_BLUE_SIZE,
            8,
            EGL14.EGL_ALPHA_SIZE,
            8,
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
                    message
                            + " (EGL 0x"
                            + Integer.toHexString(EGL14.eglGetError())
                            + ")");
        }
    }
}
