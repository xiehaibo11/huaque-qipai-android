package com.nanbeiyule.game;

/**
 * Pure original-distance gesture state for the hold-to-talk table button.
 *
 * <p>RightBtns/View.lua:161-164 measures {@code cc.pGetDistance} from the touch-down point and
 * feeds it to Voice/Module.lua:99-103, whose {@code touchMoved} only ever sets {@code _isCancel =
 * true} and never clears it. Cancellation is therefore sticky for the rest of the gesture: sliding
 * back inside the threshold does not re-arm sending. Only a new touch-down resets it
 * (Voice/Module.lua:110 {@code startRecordIng}).
 */
final class TaizhouMahjongVoiceGesture {
    static final float CANCEL_DISTANCE = 80.0f;

    enum Action {
        NONE,
        START,
        UPDATE,
        SEND,
        CANCEL
    }

    record Result(Action action, boolean recording, boolean cancelPending) {}

    private boolean recording;
    private float downX;
    private float downY;
    private boolean cancelPending;

    Result onDown(float designX, float designY, boolean insideVoiceButton) {
        reset();
        if (!insideVoiceButton) {
            return result(Action.NONE);
        }
        recording = true;
        downX = designX;
        downY = designY;
        return result(Action.START);
    }

    Result onMove(float designX, float designY) {
        if (!recording) {
            return result(Action.NONE);
        }
        float dx = designX - downX;
        float dy = designY - downY;
        // Sticky: the original never clears _isCancel once the threshold is crossed.
        cancelPending |= dx * dx + dy * dy > CANCEL_DISTANCE * CANCEL_DISTANCE;
        return result(Action.UPDATE);
    }

    Result onUp(float designX, float designY) {
        if (!recording) {
            return result(Action.NONE);
        }
        onMove(designX, designY);
        Action action = cancelPending ? Action.CANCEL : Action.SEND;
        reset();
        return new Result(action, false, false);
    }

    Result onCancel() {
        if (!recording) {
            return result(Action.NONE);
        }
        reset();
        return new Result(Action.CANCEL, false, false);
    }

    private Result result(Action action) {
        return new Result(action, recording, cancelPending);
    }

    private void reset() {
        recording = false;
        cancelPending = false;
    }
}
