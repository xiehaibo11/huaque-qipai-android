package com.nanbeiyule.game;

public final class ShopLayout {
    public static final float PAGE_WIDTH = 1920f;
    public static final float PAGE_HEIGHT = 1080f;

    public static final Rect MENU_PANEL = new Rect(0f, 124f, 430f, 1080f);
    public static final Rect CATEGORY_LIST = new Rect(0f, 145.497f, 436f, 1064.5f);
    public static final Rect CONTENT_VIEWPORT = new Rect(425.64f, 169f, 1895.64f, 1019f);
    public static final Rect BACK_BUTTON = new Rect(15.24f, 33.516f, 97.24f, 96.516f);
    public static final Rect BAG_BUTTON = new Rect(1779.04f, 5.5f, 1867.04f, 102.5f);

    public static final int PRODUCT_COLUMNS = 4;
    public static final float CATEGORY_ROW_HEIGHT = 122f;
    public static final float PRODUCT_WIDTH = 320f;
    public static final float PRODUCT_HEIGHT = 377f;
    public static final float PRODUCT_HORIZONTAL_GAP = 28f;
    public static final float PRODUCT_VERTICAL_GAP = 20f;
    public static final float PRODUCT_MARGIN_HORIZONTAL = 33f;
    public static final float PRODUCT_MARGIN_TOP = 30f;

    private ShopLayout() {}

    public static Transform contain(float screenWidth, float screenHeight) {
        if (screenWidth <= 0f || screenHeight <= 0f) {
            throw new IllegalArgumentException("screen dimensions must be positive");
        }
        float scale = Math.min(screenWidth / PAGE_WIDTH, screenHeight / PAGE_HEIGHT);
        float offsetX = (screenWidth - PAGE_WIDTH * scale) * 0.5f;
        float offsetY = (screenHeight - PAGE_HEIGHT * scale) * 0.5f;
        return new Transform(scale, offsetX, offsetY);
    }

    public static Rect categoryRow(int index, float scrollOffset) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be non-negative");
        }
        float top = CATEGORY_LIST.top + index * CATEGORY_ROW_HEIGHT - scrollOffset;
        return new Rect(0f, top, 430f, top + CATEGORY_ROW_HEIGHT);
    }

    public static int categoryIndexAt(
            float designX, float designY, float scrollOffset, int categoryCount) {
        if (!CATEGORY_LIST.contains(designX, designY) || categoryCount <= 0) {
            return -1;
        }
        int index = (int) Math.floor(
                (designY - CATEGORY_LIST.top + scrollOffset) / CATEGORY_ROW_HEIGHT);
        return index >= 0 && index < categoryCount ? index : -1;
    }

    public static Rect productCard(int index, float scrollOffset) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be non-negative");
        }
        int column = index % PRODUCT_COLUMNS;
        int row = index / PRODUCT_COLUMNS;
        float left =
                CONTENT_VIEWPORT.left
                        + PRODUCT_MARGIN_HORIZONTAL
                        + column * (PRODUCT_WIDTH + PRODUCT_HORIZONTAL_GAP);
        float top =
                CONTENT_VIEWPORT.top
                        + PRODUCT_MARGIN_TOP
                        + row * (PRODUCT_HEIGHT + PRODUCT_VERTICAL_GAP)
                        - scrollOffset;
        return new Rect(left, top, left + PRODUCT_WIDTH, top + PRODUCT_HEIGHT);
    }

    public static int productIndexAt(
            float designX, float designY, float scrollOffset, int productCount) {
        if (!CONTENT_VIEWPORT.contains(designX, designY) || productCount <= 0) {
            return -1;
        }
        for (int index = 0; index < productCount; index++) {
            if (productCard(index, scrollOffset).contains(designX, designY)) {
                return index;
            }
        }
        return -1;
    }

    public static float maxProductScroll(int productCount) {
        int rows = Math.max(1, (productCount + PRODUCT_COLUMNS - 1) / PRODUCT_COLUMNS);
        float contentHeight =
                PRODUCT_MARGIN_TOP
                        + rows * PRODUCT_HEIGHT
                        + Math.max(0, rows - 1) * PRODUCT_VERTICAL_GAP
                        + PRODUCT_MARGIN_TOP;
        return Math.max(0f, contentHeight - CONTENT_VIEWPORT.height());
    }

    public static final class Rect {
        private final float left;
        private final float top;
        private final float right;
        private final float bottom;

        public Rect(float left, float top, float right, float bottom) {
            if (right < left || bottom < top) {
                throw new IllegalArgumentException("invalid rectangle");
            }
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public float left() {
            return left;
        }

        public float top() {
            return top;
        }

        public float right() {
            return right;
        }

        public float bottom() {
            return bottom;
        }

        public float width() {
            return right - left;
        }

        public float height() {
            return bottom - top;
        }

        public float centerX() {
            return (left + right) * 0.5f;
        }

        public float centerY() {
            return (top + bottom) * 0.5f;
        }

        public boolean contains(float x, float y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }
    }

    public static final class Transform {
        private final float scale;
        private final float offsetX;
        private final float offsetY;

        private Transform(float scale, float offsetX, float offsetY) {
            this.scale = scale;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public float scale() {
            return scale;
        }

        public float offsetX() {
            return offsetX;
        }

        public float offsetY() {
            return offsetY;
        }

        public float toDesignX(float screenX) {
            return (screenX - offsetX) / scale;
        }

        public float toDesignY(float screenY) {
            return (screenY - offsetY) / scale;
        }

        public float toScreenX(float designX) {
            return offsetX + designX * scale;
        }

        public float toScreenY(float designY) {
            return offsetY + designY * scale;
        }

        public boolean containsScreenPoint(float screenX, float screenY) {
            return screenX >= offsetX
                    && screenX <= offsetX + PAGE_WIDTH * scale
                    && screenY >= offsetY
                    && screenY <= offsetY + PAGE_HEIGHT * scale;
        }
    }
}
