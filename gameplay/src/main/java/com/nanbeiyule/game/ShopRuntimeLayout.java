package com.nanbeiyule.game;

/**
 * Geometry measured from the running Zhejiang lobby shop at 1600x900 and converted to the
 * original 1920x1080 coordinate space. {@link ShopLayout} intentionally keeps the raw CSB
 * geometry; this class captures the later runtime adaptation visible in device evidence.
 */
public final class ShopRuntimeLayout {
    public static final ShopLayout.Rect HEADER =
            new ShopLayout.Rect(0f, 0f, ShopLayout.PAGE_WIDTH, 122f);
    public static final ShopLayout.Rect PRIMARY_CATEGORY_LIST =
            new ShopLayout.Rect(0f, 122f, 268f, ShopLayout.PAGE_HEIGHT);
    public static final ShopLayout.Rect SECONDARY_CATEGORY_STRIP =
            new ShopLayout.Rect(268f, 122f, 370f, ShopLayout.PAGE_HEIGHT);
    public static final ShopLayout.Rect CONTENT_VIEWPORT =
            new ShopLayout.Rect(370f, 122f, ShopLayout.PAGE_WIDTH, ShopLayout.PAGE_HEIGHT);
    public static final ShopLayout.Rect MEMBERSHIP_VIEWPORT =
            new ShopLayout.Rect(268f, 122f, ShopLayout.PAGE_WIDTH, ShopLayout.PAGE_HEIGHT);
    public static final ShopLayout.Rect BACK_BUTTON =
            new ShopLayout.Rect(10f, 16f, 220f, 112f);
    public static final ShopLayout.Rect BAG_BUTTON =
            new ShopLayout.Rect(1688f, 8f, 1802f, 116f);

    public static final float CATEGORY_ROW_HEIGHT = 146f;
    public static final float PROP_SECTION_ROW_HEIGHT = 216f;
    public static final float HOT_SECTION_ROW_HEIGHT = 220f;
    public static final float INTERACTION_SECTION_ROW_HEIGHT = 220f;
    public static final float DECORATION_SECTION_ROW_HEIGHT =
            SECONDARY_CATEGORY_STRIP.height() / ShopDecorationSection.ordered().size();
    public static final float MEMBERSHIP_CARD_SCALE = 0.9f;
    public static final float MEMBERSHIP_LAYER_ORIGIN_X = 288f;
    public static final float MEMBERSHIP_LAYER_ORIGIN_Y = 108f;
    // Exact Shop.csb parent-chain origin for the static footer; card/effect geometry keeps
    // the separately screenshot-calibrated layer origin above.
    public static final float MEMBERSHIP_FOOTER_ORIGIN_X = 285.936f;

    private static final float MEMBERSHIP_CARD_LEFT = 360f;
    private static final float MEMBERSHIP_CARD_TOP = 225.45f;
    private static final float MEMBERSHIP_CARD_WIDTH = 468f * MEMBERSHIP_CARD_SCALE;
    private static final float MEMBERSHIP_CARD_HEIGHT = 799f * MEMBERSHIP_CARD_SCALE;
    private static final float MEMBERSHIP_CARD_STEP = 478f * MEMBERSHIP_CARD_SCALE;

    private static final Grid REGULAR = new Grid(4, 370f, 144f, 359f, 439f, 20f, 20f);
    private static final float HOT_CARD_WIDTH = 460f;
    private static final float HOT_HORIZONTAL_GAP = 40f;
    private static final float HOT_GRID_LEFT = 369.936005f;
    private static final Grid HOT = new Grid(
        3,
        HOT_GRID_LEFT,
        124f,
        HOT_CARD_WIDTH,
        616f,
        HOT_HORIZONTAL_GAP,
        40f
    );
    private static final Grid MEMBERSHIP = new Grid(4, 400f, 98f, 270f, 930f, 30f, 30f);
    private static final Grid GOLD_MEMBERSHIP =
            new Grid(3, 420f, 166f, 430f, 760f, 60f, 30f);

    private ShopRuntimeLayout() {}

    public static ShopLayout.Rect categoryRow(int index, float scrollOffset) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be non-negative");
        }
        float top = PRIMARY_CATEGORY_LIST.top() + index * CATEGORY_ROW_HEIGHT - scrollOffset;
        return new ShopLayout.Rect(
                PRIMARY_CATEGORY_LIST.left(),
                top,
                PRIMARY_CATEGORY_LIST.right(),
                top + CATEGORY_ROW_HEIGHT);
    }

    public static int categoryIndexAt(
            float designX, float designY, float scrollOffset, int categoryCount) {
        if (!PRIMARY_CATEGORY_LIST.contains(designX, designY) || categoryCount <= 0) {
            return -1;
        }
        int index =
                (int)
                        Math.floor(
                                (designY - PRIMARY_CATEGORY_LIST.top() + scrollOffset)
                                        / CATEGORY_ROW_HEIGHT);
        return index >= 0 && index < categoryCount ? index : -1;
    }

    public static ShopLayout.Rect propSectionRow(int index) {
        if (index < 0 || index >= ShopPropSection.ordered().size()) {
            throw new IllegalArgumentException("invalid prop section index");
        }
        float top =
                SECONDARY_CATEGORY_STRIP.top()
                        + index * PROP_SECTION_ROW_HEIGHT;
        return new ShopLayout.Rect(
                SECONDARY_CATEGORY_STRIP.left(),
                top,
                SECONDARY_CATEGORY_STRIP.right(),
                top + PROP_SECTION_ROW_HEIGHT);
    }

    public static int propSectionIndexAt(float designX, float designY) {
        if (!SECONDARY_CATEGORY_STRIP.contains(designX, designY)) {
            return -1;
        }
        int index =
                (int)
                        Math.floor(
                                (designY - SECONDARY_CATEGORY_STRIP.top())
                                        / PROP_SECTION_ROW_HEIGHT);
        return index >= 0 && index < ShopPropSection.ordered().size() ? index : -1;
    }

    public static ShopLayout.Rect hotSectionRow(int index) {
        if (index < 0 || index >= ShopHotSection.ordered().size()) {
            throw new IllegalArgumentException("invalid hot section index");
        }
        float top = SECONDARY_CATEGORY_STRIP.top() + index * HOT_SECTION_ROW_HEIGHT;
        return new ShopLayout.Rect(
                SECONDARY_CATEGORY_STRIP.left(),
                top,
                SECONDARY_CATEGORY_STRIP.right(),
                top + HOT_SECTION_ROW_HEIGHT);
    }

    public static int hotSectionIndexAt(float designX, float designY) {
        if (!SECONDARY_CATEGORY_STRIP.contains(designX, designY)) {
            return -1;
        }
        int index =
                (int)
                        Math.floor(
                                (designY - SECONDARY_CATEGORY_STRIP.top())
                                        / HOT_SECTION_ROW_HEIGHT);
        return index >= 0 && index < ShopHotSection.ordered().size() ? index : -1;
    }

    public static ShopLayout.Rect interactionSectionRow(int index) {
        if (index < 0 || index >= ShopInteractionSection.ordered().size()) {
            throw new IllegalArgumentException("invalid interaction section index");
        }
        float top =
                SECONDARY_CATEGORY_STRIP.top()
                        + index * INTERACTION_SECTION_ROW_HEIGHT;
        return new ShopLayout.Rect(
                SECONDARY_CATEGORY_STRIP.left(),
                top,
                SECONDARY_CATEGORY_STRIP.right(),
                top + INTERACTION_SECTION_ROW_HEIGHT);
    }

    public static int interactionSectionIndexAt(float designX, float designY) {
        if (!SECONDARY_CATEGORY_STRIP.contains(designX, designY)) {
            return -1;
        }
        int index =
                (int)
                        Math.floor(
                                (designY - SECONDARY_CATEGORY_STRIP.top())
                                        / INTERACTION_SECTION_ROW_HEIGHT);
        return index >= 0 && index < ShopInteractionSection.ordered().size() ? index : -1;
    }

    public static ShopLayout.Rect decorationSectionRow(int index) {
        if (index < 0 || index >= ShopDecorationSection.ordered().size()) {
            throw new IllegalArgumentException("invalid decoration section index");
        }
        float top =
                SECONDARY_CATEGORY_STRIP.top()
                        + index * DECORATION_SECTION_ROW_HEIGHT;
        return new ShopLayout.Rect(
                SECONDARY_CATEGORY_STRIP.left(),
                top,
                SECONDARY_CATEGORY_STRIP.right(),
                top + DECORATION_SECTION_ROW_HEIGHT);
    }

    public static int decorationSectionIndexAt(float designX, float designY) {
        if (!SECONDARY_CATEGORY_STRIP.contains(designX, designY)) {
            return -1;
        }
        int index =
                (int)
                        Math.floor(
                                (designY - SECONDARY_CATEGORY_STRIP.top())
                                        / DECORATION_SECTION_ROW_HEIGHT);
        return index >= 0 && index < ShopDecorationSection.ordered().size() ? index : -1;
    }

    public static float maxCategoryScroll(int categoryCount) {
        return Math.max(
                0f,
                categoryCount * CATEGORY_ROW_HEIGHT - PRIMARY_CATEGORY_LIST.height());
    }

    public static ShopLayout.Rect productCard(
            ShopCategory category, int index, float scrollOffset) {
        if (category == null) {
            throw new IllegalArgumentException("category is required");
        }
        if (index < 0) {
            throw new IllegalArgumentException("index must be non-negative");
        }
        Grid grid = grid(category);
        int column = index % grid.columns;
        int row = index / grid.columns;
        float left = grid.left + column * (grid.width + grid.horizontalGap);
        float top = grid.top + row * (grid.height + grid.verticalGap) - scrollOffset;
        return new ShopLayout.Rect(left, top, left + grid.width, top + grid.height);
    }

    public static ShopLayout.Rect membershipCard(int index, float scrollOffset) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be non-negative");
        }
        float left = MEMBERSHIP_CARD_LEFT + index * MEMBERSHIP_CARD_STEP - scrollOffset;
        return new ShopLayout.Rect(
                left,
                MEMBERSHIP_CARD_TOP,
                left + MEMBERSHIP_CARD_WIDTH,
                MEMBERSHIP_CARD_TOP + MEMBERSHIP_CARD_HEIGHT);
    }

    public static ShopLayout.Rect membershipBuyButton(int index, float scrollOffset) {
        ShopLayout.Rect card = membershipCard(index, scrollOffset);
        return new ShopLayout.Rect(
                card.left() + 3f * MEMBERSHIP_CARD_SCALE,
                card.top() + 699f * MEMBERSHIP_CARD_SCALE,
                card.left() + 465f * MEMBERSHIP_CARD_SCALE,
                card.top() + 799f * MEMBERSHIP_CARD_SCALE);
    }

    public static int membershipProductIndexAt(
            float designX,
            float designY,
            float scrollOffset,
            int productCount) {
        if (!MEMBERSHIP_VIEWPORT.contains(designX, designY) || productCount <= 0) {
            return -1;
        }
        for (int index = 0; index < productCount; index++) {
            if (membershipBuyButton(index, scrollOffset).contains(designX, designY)) {
                return index;
            }
        }
        return -1;
    }

    public static float maxMembershipScroll(int productCount) {
        if (productCount <= 0) {
            return 0f;
        }
        return Math.max(
                0f,
                membershipCard(productCount - 1, 0f).right() - MEMBERSHIP_VIEWPORT.right());
    }

    public static int productIndexAt(
            ShopCategory category,
            float designX,
            float designY,
            float scrollOffset,
            int productCount) {
        if (!CONTENT_VIEWPORT.contains(designX, designY) || productCount <= 0) {
            return -1;
        }
        for (int index = 0; index < productCount; index++) {
            if (productCard(category, index, scrollOffset).contains(designX, designY)) {
                return index;
            }
        }
        return -1;
    }

    public static float maxProductScroll(ShopCategory category, int productCount) {
        if (productCount <= 0) {
            return 0f;
        }
        Grid grid = grid(category);
        int rows = (productCount + grid.columns - 1) / grid.columns;
        float bottom =
                grid.top
                        + rows * grid.height
                        + Math.max(0, rows - 1) * grid.verticalGap
                        + 22f;
        return Math.max(0f, bottom - CONTENT_VIEWPORT.bottom());
    }

    public static int columns(ShopCategory category) {
        return grid(category).columns;
    }

    private static Grid grid(ShopCategory category) {
        return switch (category) {
            case HOT_RECOMMENDATION -> HOT;
            case TIME_MEMBERSHIP -> MEMBERSHIP;
            case GOLD_MEMBERSHIP -> GOLD_MEMBERSHIP;
            default -> REGULAR;
        };
    }

    private static final class Grid {
        final int columns;
        final float left;
        final float top;
        final float width;
        final float height;
        final float horizontalGap;
        final float verticalGap;

        Grid(
                int columns,
                float left,
                float top,
                float width,
                float height,
                float horizontalGap,
                float verticalGap) {
            this.columns = columns;
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
            this.horizontalGap = horizontalGap;
            this.verticalGap = verticalGap;
        }
    }
}
