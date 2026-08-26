package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import java.util.List;

/** Membership-card state, drawing and hit testing embedded in the activity content panel. */
final class GoldMembershipCardsPanel {
    enum Action {
        NONE,
        CLAIM,
        OPEN_SHOP
    }

    record Target(Action action, int cardIndex) {
        static final Target NONE = new Target(Action.NONE, -1);
    }

    private final GoldMembershipCardRenderer renderer;
    private final Paint statusPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private GoldMembershipCardsState state;
    private boolean loading = true;
    private String errorMessage = "";

    GoldMembershipCardsPanel(Context context) {
        Typeface typeface =
                Typeface.createFromAsset(
                        context.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        renderer = new GoldMembershipCardRenderer(context.getResources(), typeface);
        statusPaint.setTypeface(typeface);
    }

    void setState(GoldMembershipCardsState state) {
        this.state = state;
        loading = false;
        errorMessage = "";
    }

    void updateCard(GoldMembershipCardsState.Card card) {
        if (state != null) setState(state.withUpdatedCard(card));
    }

    void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) errorMessage = "";
    }

    void setError(String message) {
        loading = false;
        errorMessage = message == null ? "" : message;
    }

    void draw(Canvas canvas) {
        if (state != null) {
            List<GoldMembershipCardsState.Card> cards = state.cards();
            for (int index = 0; index < Math.min(2, cards.size()); index++) {
                renderer.draw(
                        canvas,
                        cards.get(index),
                        GoldMembershipCardsLayout.welfareCard(index),
                        loading);
            }
        }
        drawStatus(canvas);
    }

    Target targetAt(float x, float y) {
        return targetAt(state, loading, x, y);
    }

    static Target targetAt(
            GoldMembershipCardsState state, boolean loading, float x, float y) {
        if (loading || state == null) return Target.NONE;
        List<GoldMembershipCardsState.Card> cards = state.cards();
        for (int index = 0; index < Math.min(2, cards.size()); index++) {
            GoldMembershipCardsState.Card card = cards.get(index);
            if (card.isActive() && GoldMembershipCardsLayout.renewButton(index).contains(x, y)) {
                return new Target(Action.OPEN_SHOP, index);
            }
            if (!GoldMembershipCardsLayout.primaryButton(index).contains(x, y)) continue;
            if (!card.isActive()) return new Target(Action.OPEN_SHOP, index);
            if (card.canClaim()) return new Target(Action.CLAIM, index);
            return Target.NONE;
        }
        return Target.NONE;
    }

    GoldMembershipCardsState.Card cardAt(int index) {
        if (state == null || index < 0 || index >= state.cards().size()) return null;
        return state.cards().get(index);
    }

    private void drawStatus(Canvas canvas) {
        String status = state == null && loading ? "会员卡数据加载中..." : errorMessage;
        if (status.isBlank()) return;
        GoldMembershipCardsLayout.Bounds list = GoldMembershipCardsLayout.welfareList();
        statusPaint.setTextAlign(Paint.Align.CENTER);
        statusPaint.setTextSize(34.0f);
        statusPaint.setColor(errorMessage.isBlank() ? Color.WHITE : Color.rgb(190, 70, 45));
        canvas.drawText(status, list.left() + 950.0f, list.top() + 390.0f, statusPaint);
    }
}
