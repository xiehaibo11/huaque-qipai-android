package com.nanbeiyule.game;

import java.util.List;

/** Immutable geometry shared by game-home header drawing, clipping, hit testing, and tests. */
final class GameHomeHeaderLayout {
    static final float PAGE_WIDTH = 1672.0f;
    static final float PAGE_HEIGHT = 941.0f;
    static final float HEADER_HEIGHT = 132.0f;
    private static final float ORIGINAL_DESIGN_WIDTH = 1920.0f;
    private static final float ORIGINAL_DESIGN_HEIGHT = 1080.0f;
    private static final float ORIGINAL_TO_PAGE_X =
            PAGE_WIDTH / ORIGINAL_DESIGN_WIDTH;
    private static final float ORIGINAL_TO_PAGE_Y =
            PAGE_HEIGHT / ORIGINAL_DESIGN_HEIGHT;

    record Box(float left, float top, float right, float bottom) {
        Box {
            if (right <= left || bottom <= top) {
                throw new IllegalArgumentException("Box must have positive size");
            }
        }

        float width() {
            return right - left;
        }

        float height() {
            return bottom - top;
        }

        float centerX() {
            return (left + right) / 2.0f;
        }

        float centerY() {
            return (top + bottom) / 2.0f;
        }

        boolean contains(float x, float y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }

        boolean contains(Box other) {
            return other.left >= left
                    && other.top >= top
                    && other.right <= right
                    && other.bottom <= bottom;
        }

        boolean intersects(Box other) {
            return left < other.right
                    && right > other.left
                    && top < other.bottom
                    && bottom > other.top;
        }
    }

    private final Box headerBounds =
            new Box(0.0f, 0.0f, PAGE_WIDTH, HEADER_HEIGHT);
    /*
     * Recovered from MainScene.csb:
     * panel_head origin (0, 880), _KWA_HEAD_INFO_BG origin (-10, 72.5),
     * Image_11/head_fram_bg.png at (8, 127), anchor (0, 1), size 1391 x 139.
     * Converted from Cocos bottom-left coordinates into this top-left page.
     */
    private final Box playerPanel =
            new Box(
                    -2.0f * ORIGINAL_TO_PAGE_X,
                    0.5f * ORIGINAL_TO_PAGE_Y,
                    1389.0f * ORIGINAL_TO_PAGE_X,
                    139.5f * ORIGINAL_TO_PAGE_Y);
    private final Box avatar = new Box(24.0f, 20.0f, 116.0f, 116.0f);
    private final Box playerInfo = new Box(128.0f, 16.0f, 348.0f, 122.0f);
    private final Box region = new Box(360.0f, 18.0f, 555.0f, 64.0f);
    private final Box roomCards = new Box(570.0f, 18.0f, 775.0f, 64.0f);
    private final Box coins = new Box(360.0f, 70.0f, 555.0f, 116.0f);
    private final Box diamonds = new Box(570.0f, 70.0f, 775.0f, 116.0f);
    private final Box membership = new Box(790.0f, 0.0f, 1025.0f, 126.0f);
    private final Box coinRewards = new Box(1270.0f, 15.0f, 1390.0f, 120.0f);
    private final Box customerService = new Box(1400.0f, 15.0f, 1520.0f, 120.0f);
    private final Box logout = new Box(1530.0f, 15.0f, 1650.0f, 120.0f);
    private final List<Box> cells =
            List.of(
                    avatar,
                    playerInfo,
                    region,
                    roomCards,
                    coins,
                    diamonds,
                    membership,
                    coinRewards,
                    customerService,
                    logout);

    Box headerBounds() {
        return headerBounds;
    }

    Box playerPanel() {
        return playerPanel;
    }

    Box avatar() {
        return avatar;
    }

    Box playerInfo() {
        return playerInfo;
    }

    Box region() {
        return region;
    }

    Box roomCards() {
        return roomCards;
    }

    Box coins() {
        return coins;
    }

    Box diamonds() {
        return diamonds;
    }

    Box membership() {
        return membership;
    }

    Box coinRewards() {
        return coinRewards;
    }

    Box customerService() {
        return customerService;
    }

    Box logout() {
        return logout;
    }

    Box regionHit() {
        return region;
    }

    Box avatarHit() {
        return avatar;
    }

    Box coinRewardsHit() {
        return coinRewards;
    }

    Box customerServiceHit() {
        return customerService;
    }

    Box logoutHit() {
        return logout;
    }

    List<Box> cells() {
        return cells;
    }
}
