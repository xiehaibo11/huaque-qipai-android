package com.nanbeiyule.game;

import com.nanbeiyule.game.spine37.Spine37Projection;

/** Recovered checkbox-local placement mapped through the shared login viewport. */
final class LoginAgreementHintPlacement {
    static final float CSB_WIDTH = 1920.0f;
    static final float CSB_HEIGHT = 1080.0f;
    static final float CSB_SCALE_X = LoginViewportLayout.PAGE_WIDTH / CSB_WIDTH;
    static final float CSB_SCALE_Y = LoginViewportLayout.PAGE_HEIGHT / CSB_HEIGHT;
    static final float CHECKBOX_CENTER_CSB_X = 960.0f - 487.0f;
    static final float CHECKBOX_CENTER_CSB_Y = 544.968f - 347.0f + 20.0f;

    private static final float CHECKBOX_FRAME_WIDTH = 104.0f;
    private static final float CHECKBOX_FRAME_HEIGHT = 106.0f;
    private static final float SPINE_LOCAL_X = 50.0f;
    private static final float SPINE_LOCAL_Y = 40.0f;

    private LoginAgreementHintPlacement() {}

    static float rootPageX() {
        float rootCsbX =
                CHECKBOX_CENTER_CSB_X
                        - CHECKBOX_FRAME_WIDTH / 2.0f
                        + SPINE_LOCAL_X;
        return rootCsbX * CSB_SCALE_X;
    }

    static float rootPageY() {
        float rootCsbY =
                CHECKBOX_CENTER_CSB_Y
                        - CHECKBOX_FRAME_HEIGHT / 2.0f
                        + SPINE_LOCAL_Y;
        return (CSB_HEIGHT - rootCsbY) * CSB_SCALE_Y;
    }

    static Spine37Projection projection(int viewportWidth, int viewportHeight) {
        LoginViewportLayout layout =
                LoginViewportLayout.calculate(viewportWidth, viewportHeight);
        AdaptiveViewport viewport = layout.adaptiveViewport();
        AdaptiveViewport.Transform transform = viewport.designTransform();
        return Spine37Projection.fromCenteredPixelTransform(
                viewportWidth,
                viewportHeight,
                transform.scaleX() * CSB_SCALE_X,
                transform.scaleY() * CSB_SCALE_Y,
                transform.mapX(rootPageX()),
                transform.mapY(rootPageY()));
    }
}
