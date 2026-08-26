package com.nanbeiyule.game.wulong;

import java.util.List;

/**
 * Direct BaseCardGame/Card.lua and BaseWuLong/Card.lua component geometry for a normal card.
 *
 * <p>The original defaults {@code DoubleKouFaceStyle} to 1.  Private left/right/top hands are
 * backs, so their only visible card component is {@code doublekou_back}; public out cards reuse
 * this NORMAL layout through their recovered .65 parent scale.
 */
final class WuLongCardComponentLayout {
    private static final float SOURCE_WIDTH = 232f;
    private static final float SOURCE_HEIGHT = 294f;
    private static final float VALUE_X = 14f;
    private static final float VALUE_Y = 275f;
    private static final float COLOR_X = 12f;
    private static final float COLOR_Y = 210f;
    private static final float ICON_X = 224f;
    private static final float ICON_Y = 15f;
    private static final float VALUE_SCALE = 1f;
    private static final float COLOR_SCALE = .44f;
    private static final float JOKER_VALUE_SCALE = 1f;
    private static final float VALUE_SOURCE_WIDTH = 48f;
    private static final float VALUE_SOURCE_HEIGHT = 60f;
    private static final float SUIT_SOURCE_WIDTH = 122f;
    private static final float SUIT_SOURCE_HEIGHT = 122f;
    private static final float JOKER_ICON_SOURCE_WIDTH = 154f;
    private static final float JOKER_ICON_SOURCE_HEIGHT = 196f;

    record CardRect(float left, float top, float right, float bottom) {}
    record Component(String frameName, CardRect target) {}

    private WuLongCardComponentLayout() {}

    /** Returns the style-1 (original persisted-default) component list in painter order. */
    static List<Component> componentsFor(int cardId, CardRect target) {
        CardRect face = target;
        if (cardId == 53) return List.of(face("doublekou_face.png", face),
                topLeft("doublekou_joker_small.png", target, VALUE_X, VALUE_Y,
                        VALUE_SOURCE_WIDTH, 228f, JOKER_VALUE_SCALE),
                bottomRight("doublekou_joker_icon_small.png", target, ICON_X, ICON_Y,
                        JOKER_ICON_SOURCE_WIDTH, JOKER_ICON_SOURCE_HEIGHT, 1f));
        if (cardId == 54) return List.of(face("doublekou_face.png", face),
                topLeft("doublekou_joker_big.png", target, VALUE_X, VALUE_Y,
                        VALUE_SOURCE_WIDTH, 228f, JOKER_VALUE_SCALE),
                bottomRight("doublekou_joker_icon_big.png", target, ICON_X, ICON_Y,
                        JOKER_ICON_SOURCE_WIDTH, JOKER_ICON_SOURCE_HEIGHT, 1f));

        int normalized = Math.max(1, Math.min(52, cardId));
        int group = (normalized - 1) / 13;
        int rank = (normalized - 1) % 13 + 1;
        String color = group % 2 == 0 ? "red" : "black";
        String[] suits = {"diamond", "club", "heart", "spade"};
        String suit = suits[group];
        return List.of(face("doublekou_face.png", face),
                topLeft("doublekou_" + color + "_" + rank + ".png", target, VALUE_X, VALUE_Y,
                        VALUE_SOURCE_WIDTH, VALUE_SOURCE_HEIGHT, VALUE_SCALE),
                topLeft("doublekou_" + suit + ".png", target, COLOR_X, COLOR_Y,
                        SUIT_SOURCE_WIDTH, SUIT_SOURCE_HEIGHT, COLOR_SCALE),
                bottomRight("doublekou_" + suit + ".png", target, ICON_X, ICON_Y,
                        SUIT_SOURCE_WIDTH, SUIT_SOURCE_HEIGHT, 1f));
    }

    private static Component face(String frameName, CardRect target) {
        return new Component(frameName, target);
    }

    /** Cocos anchor (0,1): x/y is the upper-left of the unscaled sprite. */
    private static Component topLeft(String frameName, CardRect card, float cocosX, float cocosY,
            float sourceWidth, float sourceHeight, float scale) {
        float sx = (card.right() - card.left()) / SOURCE_WIDTH;
        float sy = (card.bottom() - card.top()) / SOURCE_HEIGHT;
        float left = card.left() + cocosX * sx;
        float top = card.top() + (SOURCE_HEIGHT - cocosY) * sy;
        return new Component(frameName, new CardRect(left, top,
                left + sourceWidth * scale * sx, top + sourceHeight * scale * sy));
    }

    /** Cocos anchor (1,0): x/y is the lower-right of the unscaled sprite. */
    private static Component bottomRight(String frameName, CardRect card, float cocosX, float cocosY,
            float sourceWidth, float sourceHeight, float scale) {
        float sx = (card.right() - card.left()) / SOURCE_WIDTH;
        float sy = (card.bottom() - card.top()) / SOURCE_HEIGHT;
        float right = card.left() + cocosX * sx;
        float bottom = card.top() + (SOURCE_HEIGHT - cocosY) * sy;
        return new Component(frameName, new CardRect(right - sourceWidth * scale * sx,
                bottom - sourceHeight * scale * sy, right, bottom));
    }
}
