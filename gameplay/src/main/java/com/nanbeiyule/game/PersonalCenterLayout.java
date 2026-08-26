package com.nanbeiyule.game;

import java.util.List;

/** Original-design geometry shared by drawing, hit testing, and viewport tests. */
final class PersonalCenterLayout extends PersonalCenterLayoutGeometry {
    static final float DESIGN_WIDTH = 1920.0f;
    static final float DESIGN_HEIGHT = 1080.0f;
    private static final float ORIGINAL_DIALOG_WIDTH_SCALE = 0.814f;
    private static final float ORIGINAL_DIALOG_HEIGHT_SCALE = 0.852f;
    private final Box panel =
            new Box(16.0f, 15.0f, 1904.0f, 1065.0f);
    private final Box titleBar =
            new Box(16.0f, 15.0f, 1904.0f, 115.0f);
    private final Box sidebar =
            new Box(16.0f, 15.0f, 394.0f, 1065.0f);
    private final Box close =
            new Box(1816.0f, 26.0f, 1890.0f, 100.0f);
    private final List<Box> tabs =
            List.of(
                    new Box(28.0f, 132.0f, 382.0f, 230.0f),
                    new Box(28.0f, 230.0f, 382.0f, 328.0f),
                    new Box(28.0f, 328.0f, 382.0f, 426.0f),
                    new Box(28.0f, 426.0f, 382.0f, 524.0f),
                    new Box(28.0f, 524.0f, 382.0f, 622.0f));
    private final Box avatar =
            new Box(650.0f, 145.0f, 850.0f, 345.0f);
    private final Box copy =
            new Box(842.0f, 425.0f, 930.0f, 472.0f);
    private final Box purchasedRoomCards =
            new Box(1110.0f, 155.0f, 1810.0f, 265.0f);
    private final Box boundRoomCards =
            new Box(1110.0f, 285.0f, 1810.0f, 395.0f);
    private final Box diamonds =
            new Box(1110.0f, 415.0f, 1810.0f, 525.0f);
    private final Box purchaseRoomCards =
            new Box(1714.0f, 168.0f, 1796.0f, 252.0f);
    private final Box boundRoomCardsHelp =
            new Box(1714.0f, 298.0f, 1796.0f, 382.0f);
    private final Box purchaseDiamonds =
            new Box(1714.0f, 428.0f, 1796.0f, 512.0f);
    private final Box switchAccount =
            new Box(690.0f, 760.0f, 990.0f, 838.0f);
    private final Box switchRegion =
            new Box(1110.0f, 760.0f, 1410.0f, 838.0f);
    private final Box deleteAccount =
            new Box(718.0f, 862.0f, 928.0f, 930.0f);
    private final Box refreshAvatar =
            new Box(1168.0f, 862.0f, 1378.0f, 930.0f);
    private final List<Box> quickActions =
            equalColumns(450.0f, 550.0f, 1830.0f, 710.0f, 4);
    private final List<Box> securityRows =
            verticalRows(450.0f, 145.0f, 1830.0f, 88.0f, 105.0f, 5);
    private final List<Box> privacyRows =
            verticalRows(450.0f, 145.0f, 1830.0f, 100.0f, 122.0f, 5);
    private final Box privacyPolicy =
            new Box(1010.0f, 830.0f, 1214.0f, 920.0f);
    private final Box musicSlider =
            new Box(720.0f, 175.0f, 1470.0f, 250.0f);
    private final Box musicToggle =
            new Box(1680.0f, 175.0f, 1790.0f, 250.0f);
    private final Box soundSlider =
            new Box(720.0f, 252.0f, 1470.0f, 327.0f);
    private final Box soundToggle =
            new Box(1680.0f, 252.0f, 1790.0f, 327.0f);
    private final Box voiceToggle =
            new Box(1680.0f, 330.0f, 1790.0f, 405.0f);
    private final Box vibrationToggle =
            new Box(1680.0f, 407.0f, 1790.0f, 482.0f);
    private final List<Box> graphicsQuality =
            equalColumns(820.0f, 490.0f, 1396.0f, 560.0f, 4);
    private final List<Box> effectsQuality =
            equalColumns(960.0f, 567.0f, 1410.0f, 637.0f, 3);
    private final Box batterySaverToggle =
            new Box(1680.0f, 640.0f, 1790.0f, 715.0f);
    private final List<Box> systemTools =
            List.of(
                    new Box(735.0f, 770.0f, 925.0f, 930.0f),
                    new Box(1015.0f, 770.0f, 1205.0f, 930.0f),
                    new Box(1295.0f, 770.0f, 1485.0f, 930.0f));
    private final List<Box> helpRows =
            verticalRows(450.0f, 145.0f, 1830.0f, 100.0f, 122.0f, 5);


    static Viewport fit(int width, int height) {
        return fit(width, height, AdaptiveViewport.Insets.NONE);
    }

    static Viewport fit(
            int width,
            int height,
            AdaptiveViewport.Insets insets) {
        AdaptiveViewport adaptiveViewport =
                AdaptiveViewport.create(
                        width,
                        height,
                        DESIGN_WIDTH,
                        DESIGN_HEIGHT,
                        insets);
        AdaptiveViewport.Transform transform =
                adaptiveViewport.dialogTransform(
                        DESIGN_WIDTH,
                        DESIGN_HEIGHT,
                        1.0f,
                        1.0f);
        float baseScale = transform.scaleX();
        float scaleX =
                baseScale * ORIGINAL_DIALOG_WIDTH_SCALE;
        float scaleY =
                baseScale * ORIGINAL_DIALOG_HEIGHT_SCALE;
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;
        float offsetX =
                centerX - DESIGN_WIDTH * scaleX / 2.0f;
        float offsetY =
                centerY - DESIGN_HEIGHT * scaleY / 2.0f;
        PersonalCenterLayout layout = new PersonalCenterLayout();
        Box panel = layout.panel;
        float panelLeft = offsetX + panel.left() * scaleX;
        float panelRight = offsetX + panel.right() * scaleX;
        float panelTop = offsetY + panel.top() * scaleY;
        float panelBottom = offsetY + panel.bottom() * scaleY;
        float safeLeft = insets.left();
        float safeRight = width - insets.right();
        float safeTop = insets.top();
        float safeBottom = height - insets.bottom();
        if (panelLeft < safeLeft) {
            offsetX += safeLeft - panelLeft;
        } else if (panelRight > safeRight) {
            offsetX -= panelRight - safeRight;
        }
        if (panelTop < safeTop) {
            offsetY += safeTop - panelTop;
        } else if (panelBottom > safeBottom) {
            offsetY -= panelBottom - safeBottom;
        }
        Box visible =
                new Box(
                        offsetX + panel.left() * scaleX,
                        offsetY + panel.top() * scaleY,
                        offsetX + panel.right() * scaleX,
                        offsetY + panel.bottom() * scaleY);
        return new Viewport(
                scaleX,
                scaleY,
                offsetX,
                offsetY,
                visible);
    }

    Box panel() {
        return panel;
    }

    Box titleBar() {
        return titleBar;
    }

    Box sidebar() {
        return sidebar;
    }

    Box closeHit() {
        return close;
    }

    List<Box> tabHits() {
        return tabs;
    }

    Box avatar() {
        return avatar;
    }

    Box copyHit() {
        return copy;
    }

    Box purchasedRoomCards() {
        return purchasedRoomCards;
    }

    Box boundRoomCards() {
        return boundRoomCards;
    }

    Box diamonds() {
        return diamonds;
    }

    Box purchaseRoomCardsHit() {
        return purchaseRoomCards;
    }

    Box boundRoomCardsHelpHit() {
        return boundRoomCardsHelp;
    }

    Box purchaseDiamondsHit() {
        return purchaseDiamonds;
    }

    Box switchAccountHit() {
        return switchAccount;
    }

    Box switchRegionHit() {
        return switchRegion;
    }

    Box deleteAccountHit() {
        return deleteAccount;
    }

    Box refreshAvatarHit() {
        return refreshAvatar;
    }

    List<Box> quickActionHits() {
        return quickActions;
    }

    List<Box> securityHits() {
        return securityRows;
    }

    List<Box> privacyHits() {
        return privacyRows;
    }

    Box privacyPolicyHit() {
        return privacyPolicy;
    }

    Box musicSliderHit() {
        return musicSlider;
    }

    Box musicToggleHit() {
        return musicToggle;
    }

    Box soundSliderHit() {
        return soundSlider;
    }

    Box soundToggleHit() {
        return soundToggle;
    }

    Box voiceToggleHit() {
        return voiceToggle;
    }

    Box vibrationToggleHit() {
        return vibrationToggle;
    }

    List<Box> graphicsQualityHits() {
        return graphicsQuality;
    }

    List<Box> effectsQualityHits() {
        return effectsQuality;
    }

    Box batterySaverToggleHit() {
        return batterySaverToggle;
    }

    List<Box> systemToolHits() {
        return systemTools;
    }

    List<Box> helpHits() {
        return helpRows;
    }
}
