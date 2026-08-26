package com.nanbeiyule.game.spine37;

import java.util.List;

public final class Spine37Animator {
    private Spine37Animator() {}

    public static float wrapTime(float time, float duration) {
        if (duration <= 0.0f) {
            return 0.0f;
        }
        float wrapped = time % duration;
        return wrapped < 0.0f ? wrapped + duration : wrapped;
    }

    public static float sampleX(
            List<Spine37Data.NumericFrame> frames,
            float time,
            float setupValue) {
        if (frames == null || frames.isEmpty() || time < frames.get(0).time()) {
            return setupValue;
        }
        int previousIndex = previousFrame(frames, time);
        Spine37Data.NumericFrame previous = frames.get(previousIndex);
        if (previousIndex == frames.size() - 1 || previous.stepped()) {
            return previous.x();
        }
        Spine37Data.NumericFrame next = frames.get(previousIndex + 1);
        float alpha =
                previous.curve()
                        .apply(
                                (time - previous.time())
                                        / (next.time() - previous.time()));
        return previous.x() + (next.x() - previous.x()) * alpha;
    }

    public static float sampleY(
            List<Spine37Data.NumericFrame> frames,
            float time,
            float setupValue) {
        if (frames == null || frames.isEmpty() || time < frames.get(0).time()) {
            return setupValue;
        }
        int previousIndex = previousFrame(frames, time);
        Spine37Data.NumericFrame previous = frames.get(previousIndex);
        if (previousIndex == frames.size() - 1 || previous.stepped()) {
            return previous.y();
        }
        Spine37Data.NumericFrame next = frames.get(previousIndex + 1);
        float alpha =
                previous.curve()
                        .apply(
                                (time - previous.time())
                                        / (next.time() - previous.time()));
        return previous.y() + (next.y() - previous.y()) * alpha;
    }

    private static int previousFrame(
            List<Spine37Data.NumericFrame> frames,
            float time) {
        int low = 0;
        int high = frames.size() - 1;
        while (low <= high) {
            int middle = (low + high) >>> 1;
            if (frames.get(middle).time() <= time) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }
        return Math.max(0, high);
    }
}
