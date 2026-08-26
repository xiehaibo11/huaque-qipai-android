package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

final class JoinRoomView extends View {
    interface Actions {
        void onJoinRequested(String roomNumber);
    }

    private static final int ACTION_CLOSE = -4;

    private final Runnable closeAction;
    private final Actions actions;
    private final JoinRoomRenderer renderer;
    private final String areaName;
    private final String guideText;
    private JoinRoomInput input = JoinRoomInput.empty();
    private int pressedAction = JoinRoomRenderer.ACTION_NONE;
    private boolean submitting;
    private Runnable buttonClickSound = () -> {};

    JoinRoomView(Context context, String areaName, Runnable closeAction, Actions actions) {
        super(context);
        this.areaName = areaName == null ? "" : areaName.trim();
        this.closeAction = closeAction == null ? () -> {} : closeAction;
        this.actions = actions == null ? roomNumber -> {} : actions;
        renderer = new JoinRoomRenderer(new XianyiJoinRoomDrawableSet(context.getResources()));
        guideText = JoinRoomGuideText.randomTaizhouTip();
        setClickable(true);
        setFocusable(true);
    }

    void setInitialRoomNumber(String roomNumber) {
        input = JoinRoomInput.from(roomNumber);
        invalidate();
    }

    void submitInitialRoomNumber() {
        if (input.isComplete() && !submitting) {
            submitting = true;
            invalidate();
            actions.onJoinRequested(input.roomNumber());
        }
    }

    void setSubmitting(boolean submitting) {
        this.submitting = submitting;
        invalidate();
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        JoinRoomLayout.Viewport viewport = JoinRoomLayout.contain(getWidth(), getHeight());
        canvas.save();
        canvas.translate(viewport.offsetX(), viewport.offsetY());
        canvas.scale(viewport.scale(), viewport.scale());
        renderer.draw(canvas, input, pressedAction);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        JoinRoomLayout.Viewport viewport = JoinRoomLayout.contain(getWidth(), getHeight());
        float x = viewport.designX(event.getX());
        float y = viewport.designY(event.getY());
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            pressedAction = actionAt(x, y);
            invalidate();
            return true;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            pressedAction = JoinRoomRenderer.ACTION_NONE;
            invalidate();
            return true;
        }
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return true;
        }
        int releasedAction = actionAt(x, y);
        int action = releasedAction == pressedAction ? pressedAction : JoinRoomRenderer.ACTION_NONE;
        pressedAction = JoinRoomRenderer.ACTION_NONE;
        invalidate();
        if (action != JoinRoomRenderer.ACTION_NONE) {
            performClick();
            handleAction(action);
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private int actionAt(float x, float y) {
        if (JoinRoomLayout.CLOSE.contains(x, y) || !JoinRoomLayout.FRAME.contains(x, y)) {
            return ACTION_CLOSE;
        }
        if (submitting) {
            return JoinRoomRenderer.ACTION_NONE;
        }
        int digit = JoinRoomLayout.digitAt(x, y);
        if (digit >= 0) {
            return digit;
        }
        if (JoinRoomLayout.CLEAR.contains(x, y)) {
            return JoinRoomRenderer.ACTION_CLEAR;
        }
        if (JoinRoomLayout.DELETE.contains(x, y)) {
            return JoinRoomRenderer.ACTION_DELETE;
        }
        return JoinRoomRenderer.ACTION_NONE;
    }

    private void handleAction(int action) {
        buttonClickSound.run();
        if (action == ACTION_CLOSE) {
            closeAction.run();
            return;
        }
        if (action == JoinRoomRenderer.ACTION_CLEAR) {
            input = input.clear();
        } else if (action == JoinRoomRenderer.ACTION_DELETE) {
            input = input.deleteLast();
        } else if (action >= 0 && action <= 9) {
            input = input.append(action);
        }
        invalidate();
        if (input.isComplete()) {
            submitting = true;
            actions.onJoinRequested(input.roomNumber());
        }
    }
}
