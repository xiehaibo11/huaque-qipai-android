package com.nanbeiyule.game.wulong;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.WuLongRound;
import java.io.IOException;
import java.util.List;

/** Native, evidence-sized 30588 table that renders only the server-projected round state. */
public final class WuLongTableView extends View {
    public interface Listener {
        void onReady();
        void onStart();
        void onPlay(List<Integer> cards);
        void onPass();
        void onNextRound();
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final WuLongInteractionController interaction = new WuLongInteractionController();
    private final WuLongCardRenderer cards;
    private final WuLongControlRenderer controls;
    private final WuLongControlRenderer results;
    private WuLongTableState state;
    private Listener listener;

    public WuLongTableView(Context context) {
        super(context);
        cards = new WuLongCardRenderer(loadOriginalAtlas(context), loadCardFrames(context));
        controls = new WuLongControlRenderer(loadControlAtlas(context), loadControlFrames(context));
        results = new WuLongControlRenderer(loadResultAtlas(context), loadResultFrames(context));
    }

    public void setListener(Listener listener) { this.listener = listener; }

    public void setState(WuLongTableState state) {
        this.state = state;
        interaction.clear();
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (state == null) return;
        float sx = getWidth() / (float) WuLongTableLayout.DESIGN_WIDTH;
        float sy = getHeight() / (float) WuLongTableLayout.DESIGN_HEIGHT;
        canvas.save();
        canvas.scale(sx, sy);
        drawSeats(canvas, state.round().orElse(null));
        drawRound(canvas, state.round().orElse(null));
        drawActions(canvas);
        canvas.restore();
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP || state == null) return true;
        float x = event.getX() * WuLongTableLayout.DESIGN_WIDTH / getWidth();
        float y = event.getY() * WuLongTableLayout.DESIGN_HEIGHT / getHeight();
        Listener active = listener;
        if (active == null) return true;
        WuLongRound round = state.round().orElse(null);
        List<WuLongHandProjection.CardPlacement> hand = round == null ? List.of()
                : orderedHandProjection(round.hand(), interaction);
        int handIndex = handCardIndex(x, y, hand);
        WuLongTableInputRouter.Target target = targetFor(x, y, handIndex);
        WuLongTableInputRouter.Decision decision = WuLongTableInputRouter.route(
                state.snapshot().phase(), state.snapshot().mySeat(), round == null ? null : round.activeSeat(),
                isHost(), allSeatsReady(), round != null && !interaction.selectedCards(round.hand()).isEmpty(),
                round != null && !round.lastPlay().isEmpty(), target);
        if (decision.intent() == WuLongTableInputRouter.Intent.READY) active.onReady();
        else if (decision.intent() == WuLongTableInputRouter.Intent.START) active.onStart();
        else if (decision.intent() == WuLongTableInputRouter.Intent.TOGGLE_HAND) {
            applyHandTap(interaction, handIndex, round.hand());
            invalidate();
        } else if (decision.intent() == WuLongTableInputRouter.Intent.PLAY) {
            active.onPlay(interaction.selectedCards(round.hand()));
        } else if (decision.intent() == WuLongTableInputRouter.Intent.PASS) active.onPass();
        else if (decision.intent() == WuLongTableInputRouter.Intent.NEXT_ROUND) active.onNextRound();
        return true;
    }

    private WuLongTableInputRouter.Target targetFor(float x, float y, int handIndex) {
        GameplayPhase phase = state.snapshot().phase();
        if (phase == GameplayPhase.WAITING) {
            if (WuLongTableLayout.readyButton().contains(x, y)) return WuLongTableInputRouter.Target.READY;
            if (WuLongTableLayout.startButton().contains(x, y)) return WuLongTableInputRouter.Target.START;
            return WuLongTableInputRouter.Target.NONE;
        }
        if (phase == GameplayPhase.ROUND_RESULT) {
            return WuLongTableLayout.startButton().contains(x, y)
                    ? WuLongTableInputRouter.Target.NEXT : WuLongTableInputRouter.Target.NONE;
        }
        if (handIndex >= 0) return WuLongTableInputRouter.Target.HAND_CARD;
        if (WuLongTableLayout.playButton().contains(x, y)) return WuLongTableInputRouter.Target.PLAY;
        if (WuLongTableLayout.passButton().contains(x, y)) return WuLongTableInputRouter.Target.PASS;
        return WuLongTableInputRouter.Target.NONE;
    }

    private void drawSeats(Canvas canvas, WuLongRound round) {
        paint.setTextSize(28);
        for (int seat = 1; seat <= 4; seat++) {
            int serverSeat = seat;
            float[] anchor = WuLongTableLayout.playerAnchor(seat, state.snapshot().mySeat());
            controls.draw(canvas, WuLongControlFrames.head(), new RectF(anchor[0] - 42, anchor[1] - 42,
                    anchor[0] + 42, anchor[1] + 42));
            paint.setColor(seat == state.snapshot().mySeat() ? Color.YELLOW : Color.WHITE);
            int count = round == null ? 0 : round.cardCounts().getOrDefault(seat, 0);
            String name = state.snapshot().seats().stream().filter(item -> item.seatNumber() == serverSeat)
                    .findFirst().map(item -> item.displayName()).orElse("");
            canvas.drawText(name, anchor[0] - 45, anchor[1] + 60, paint);
            controls.draw(canvas, WuLongControlFrames.cardCount(), new RectF(anchor[0] + 46, anchor[1] - 26,
                    anchor[0] + 82, anchor[1] + 20));
            canvas.drawText(Integer.toString(count), anchor[0] + 55, anchor[1] + 5, paint);
        }
    }

    private void drawRound(Canvas canvas, WuLongRound round) {
        if (round == null) return;
        int mySeat = state.snapshot().mySeat();
        for (WuLongHandProjection.CardPlacement placement : orderedHandProjection(round.hand(), interaction)) {
            boolean selected = interaction.isSelected(placement.sourceIndex());
            cards.drawOwnedCard(canvas, placement.cardId(), placement.bounds().asRectF(), selected);
        }
        for (int seat = 1; seat <= 4; seat++) {
            if (seat == mySeat) continue;
            for (int index = 0; index < Math.min(round.cardCounts().getOrDefault(seat, 0), 9); index++) {
                cards.drawBack(canvas, WuLongTableLayout.handCardBounds(seat, mySeat, index,
                        round.cardCounts().getOrDefault(seat, 0), false).asRectF());
            }
        }
        drawLastPlay(canvas, round);
        drawClock(canvas);
        canvas.drawText("桌面分：" + round.deskScore(), 1510, 80, paint);
        if (round.result() != null) drawResult(canvas, round);
    }

    private void drawActions(Canvas canvas) {
        if (state.snapshot().phase() == GameplayPhase.WAITING) {
            controls.draw(canvas, WuLongControlFrames.ready(), WuLongTableLayout.readyButton().asRectF());
            controls.draw(canvas, WuLongControlFrames.start(), WuLongTableLayout.startButton().asRectF());
        } else if (state.snapshot().phase() == GameplayPhase.ROUND_RESULT) {
            results.draw(canvas, WuLongControlFrames.resultNext(), WuLongTableLayout.startButton().asRectF());
        } else {
            controls.draw(canvas, WuLongControlFrames.play(), WuLongTableLayout.playButton().asRectF());
            controls.draw(canvas, WuLongControlFrames.pass(), WuLongTableLayout.passButton().asRectF());
        }
    }

    /** Draws the one server-active recovered clock panel; timeout policy is explicitly self-built. */
    private void drawClock(Canvas canvas) {
        WuLongClockProjection.Panel panel = WuLongClockProjection.project(state.snapshot());
        if (panel == null) return;
        WuLongClockProjection.Bounds bounds = panel.bounds();
        controls.draw(canvas, WuLongControlFrames.clock(), new RectF(
                bounds.left(), bounds.top(), bounds.left() + bounds.width(), bounds.top() + bounds.height()));
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(28);
        canvas.drawText(panel.remainingText(), bounds.left() + bounds.width() / 2f - 4f,
                bounds.top() + bounds.height() / 2f + 10f, paint);
        if (!panel.statusLabel().isEmpty()) {
            paint.setTextSize(18);
            canvas.drawText(panel.statusLabel(), bounds.left() + bounds.width() / 2f,
                    bounds.top() + bounds.height() + 25f, paint);
        }
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private int handCardIndex(float x, float y, List<WuLongHandProjection.CardPlacement> hand) {
        if (!canSelectHand()) return -1;
        return WuLongHandProjection.hitTest(hand, x, y);
    }

    /** The exact projection used by this view's Canvas draw and touch hit path. */
    static List<WuLongHandProjection.CardPlacement> orderedHandProjection(
            List<Integer> serverHand, WuLongInteractionController interaction) {
        return WuLongHandProjection.project(serverHand, interaction::isSelected);
    }

    /** Runtime hand-tap seam: onTouchEvent and behavior tests deliberately invoke this same path. */
    static List<Integer> applyHandTap(WuLongInteractionController interaction, int sourceIndex,
            List<Integer> serverHand) {
        return interaction.tap(sourceIndex, serverHand);
    }

    private boolean canStartRound() {
        return state.snapshot().phase() == GameplayPhase.WAITING && isHost() && allSeatsReady();
    }

    private boolean canPlay() {
        return canSubmitPlay();
    }

    private boolean canSelectHand() {
        return state.round().isPresent() && WuLongTableInteractionDecisions.canSelectHand(
                state.snapshot().phase(), state.snapshot().mySeat(), state.round().get().activeSeat());
    }

    private boolean canSubmitPlay() {
        return state.round().isPresent() && WuLongTableInteractionDecisions.canSubmitPlay(
                state.snapshot().phase(), state.snapshot().mySeat(), state.round().get().activeSeat(),
                !interaction.selectedCards(state.round().get().hand()).isEmpty());
    }

    private boolean canPass() {
        return state.round().isPresent() && WuLongTableInteractionDecisions.canPass(
                state.snapshot().phase(), state.snapshot().mySeat(), state.round().get().activeSeat(),
                !state.round().get().lastPlay().isEmpty());
    }

    private boolean canNextRound() {
        return state.snapshot().phase() == GameplayPhase.ROUND_RESULT && isHost();
    }

    private boolean isHost() {
        return state.snapshot().seats().stream().anyMatch(
                seat -> seat.seatNumber() == state.snapshot().mySeat() && seat.host());
    }

    private boolean allSeatsReady() {
        return state.snapshot().seats().size() == 4 && state.snapshot().seats().stream().allMatch(seat -> seat.ready());
    }

    private static Bitmap loadOriginalAtlas(Context context) {
        try {
            return BitmapFactory.decodeStream(context.getAssets().open("wulong-30588/original/tex/plist/doublekou_card.png"));
        } catch (IOException ignored) {
            return null;
        }
    }

    private static WuLongCardFrames loadCardFrames(Context context) {
        try { return WuLongCardFrames.load(context.getAssets().open("wulong-30588/original/tex/plist/doublekou_card.plist")); }
        catch (IOException ignored) { return null; }
    }

    private static Bitmap loadControlAtlas(Context context) {
        try {
            return BitmapFactory.decodeStream(context.getAssets().open("wulong-30588/original/Common/Image/doublekou_gamelayer.png"));
        } catch (IOException ignored) { return null; }
    }

    private static WuLongPlistFrameResolver loadControlFrames(Context context) {
        try {
            return WuLongPlistFrameResolver.load(
                    context.getAssets().open("wulong-30588/original/Common/Image/doublekou_gamelayer.plist"));
        } catch (IOException ignored) {
            return null;
        }
    }

    private static Bitmap loadResultAtlas(Context context) {
        try {
            return BitmapFactory.decodeStream(context.getAssets().open("wulong-30588/original/Common/Image/doublekou_result.png"));
        } catch (IOException ignored) {
            return null;
        }
    }

    private static WuLongPlistFrameResolver loadResultFrames(Context context) {
        try {
            return WuLongPlistFrameResolver.load(
                    context.getAssets().open("wulong-30588/original/Common/Image/doublekou_result.plist"));
        } catch (IOException ignored) {
            return null;
        }
    }

    private void drawResult(Canvas canvas, WuLongRound round) {
        results.draw(canvas, WuLongControlFrames.resultTitle(), new RectF(710, 210, 1210, 305));
        results.draw(canvas, WuLongControlFrames.resultShuangKou(), new RectF(790, 315, 1130, 430));
        paint.setColor(Color.WHITE);
        paint.setTextSize(26);
        // NANBEI_SELF_BUILT composition: original result chrome, server-persisted finish/score values.
        for (WuLongResultProjection.SeatResult value : WuLongResultProjection.from(
                round.result(), round.finishOrder(), state.snapshot().mySeat())) {
            float[] anchor = WuLongTableLayout.playerAnchor(value.serverSeat(), state.snapshot().mySeat());
            canvas.drawText(String.format("#%d  %+d", value.finishIndex() + 1, value.finalScore()),
                    anchor[0] - 42, anchor[1] + 90, paint);
        }
    }

    private void drawLastPlay(Canvas canvas, WuLongRound round) {
        if (round.lastPlay().isEmpty() || round.lastPlaySeat() == null) return;
        WuLongOutCardProjection.render(round.lastPlaySeat(), state.snapshot().mySeat(), round.lastPlay(),
                (cardId, bounds) -> cards.drawOwnedCard(canvas, cardId, bounds.asRectF(), false));
    }
}
