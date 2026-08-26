package com.nanbeiyule.game.wulong;

import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;

/** Canvas fallback composition around the copied original 30588 atlas; no opponent card values render. */
final class WuLongCardRenderer {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Bitmap originalCardAtlas;
    private final WuLongCardFrames frames;

    WuLongCardRenderer(Bitmap originalCardAtlas, WuLongCardFrames frames) { this.originalCardAtlas = originalCardAtlas; this.frames = frames; }

    void drawBack(Canvas canvas, RectF target) {
        drawFrame(canvas, "doublekou_back.png", target);
    }

    void drawOwnedCard(Canvas canvas, int cardValue, RectF target, boolean selected) {
        drawComponents(cardValue, cardRect(target), (frameName, frameTarget) -> drawFrame(canvas,
                frameName, asRectF(frameTarget)));
        if (selected) {
            paint.setColor(Color.rgb(255, 234, 132));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6);
            canvas.drawRect(target, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawFrame(Canvas canvas, String frameName, RectF target) {
        if (frames != null) frames.resolver().draw(canvas, originalCardAtlas, frameName, target, paint);
    }

    interface ComponentReceiver {
        void draw(String frameName, WuLongCardComponentLayout.CardRect target);
    }

    /** Production component path, also observable by deterministic renderer contract tests. */
    static void drawComponents(int cardValue, WuLongCardComponentLayout.CardRect target,
            ComponentReceiver receiver) {
        for (WuLongCardComponentLayout.Component component :
                WuLongCardComponentLayout.componentsFor(cardValue, target)) {
            receiver.draw(component.frameName(), component.target());
        }
    }

    /**
     * BaseCardGame/Card.lua: ids 1..52 are 13-card suit groups; odd groups are red. 53/54 are
     * small/big jokers. This is rendering evidence only, not a server-side card judgement rule.
     */
    static List<String> sourceNamesForCard(int cardValue) {
        List<String> names = new ArrayList<>();
        for (WuLongCardComponentLayout.Component component : WuLongCardComponentLayout.componentsFor(
                cardValue, new WuLongCardComponentLayout.CardRect(0, 0, 232, 294))) {
            names.add(component.frameName());
        }
        return List.copyOf(names);
    }

    private static WuLongCardComponentLayout.CardRect cardRect(RectF target) {
        return new WuLongCardComponentLayout.CardRect(target.left, target.top, target.right, target.bottom);
    }

    private static RectF asRectF(WuLongCardComponentLayout.CardRect target) {
        return new RectF(target.left(), target.top(), target.right(), target.bottom());
    }
}
