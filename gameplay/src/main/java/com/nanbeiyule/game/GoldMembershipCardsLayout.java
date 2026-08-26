package com.nanbeiyule.game;

/** Original VipWelfareLayer/VipWelfareItem geometry embedded in the activity content panel. */
final class GoldMembershipCardsLayout {
    private static final float CONTENT_LEFT = 488.0f;
    private static final float CONTENT_TOP = 183.956f;
    private static final float CONTENT_WIDTH = 1282.0f;
    private static final float CONTENT_HEIGHT = 800.0f;
    private static final float LIST_WIDTH = 1206.0f;
    private static final float LIST_HEIGHT = 750.0f;
    private static final float CARD_WIDTH = 400.0f;
    private static final float CARD_HEIGHT = 740.0f;
    private static final float CARD_GAP = 50.0f;

    record Bounds(float left, float top, float right, float bottom) {
        float width() {
            return right - left;
        }

        float height() {
            return bottom - top;
        }

        boolean contains(float x, float y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }
    }

    private GoldMembershipCardsLayout() {}

    static Bounds welfareList() {
        float left = CONTENT_LEFT + (CONTENT_WIDTH - LIST_WIDTH) * 0.5f;
        float top = CONTENT_TOP + (CONTENT_HEIGHT - LIST_HEIGHT) * 0.5f;
        return bounds(left, top, LIST_WIDTH, LIST_HEIGHT);
    }

    static Bounds welfareCard(int index) {
        if (index < 0 || index > 1) {
            throw new IllegalArgumentException("Only two original welfare cards are supported");
        }
        Bounds list = welfareList();
        float left = list.left() + index * (CARD_WIDTH + CARD_GAP);
        return bounds(left, list.top(), CARD_WIDTH, CARD_HEIGHT);
    }

    static Bounds primaryButton(int index) {
        Bounds card = welfareCard(index);
        return bounds(card.left() + 61.5f, card.top() + 575.21f, 277.0f, 103.0f);
    }

    static Bounds renewButton(int index) {
        Bounds card = welfareCard(index);
        return bounds(card.left() + 63.0f, card.top() + 459.0f, 274.0f, 102.0f);
    }

    private static Bounds bounds(float left, float top, float width, float height) {
        return new Bounds(left, top, left + width, top + height);
    }
}
