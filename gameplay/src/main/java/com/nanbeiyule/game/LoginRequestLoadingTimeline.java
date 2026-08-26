package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Pure-Java sampler for the recovered Cocos Studio login-loading movement. */
final class LoginRequestLoadingTimeline {
    static final int DURATION_FRAMES = 246;
    static final float SPEED = 1.33333337f;
    static final float BASE_FPS = 60.0f;

    enum Tile {
        EAST,
        SOUTH,
        WEST,
        NORTH
    }

    record DrawCommand(
            Tile tile,
            float x,
            float y,
            float scaleX,
            float scaleY,
            float alpha,
            int z) {}

    record Keyframe(
            int frame,
            float x,
            float y,
            float scaleX,
            float scaleY,
            float alpha,
            boolean tweenFrame,
            int tweenEasing) {}

    record Track(
            String name,
            Tile tile,
            float baseX,
            float baseY,
            float baseScaleX,
            float baseScaleY,
            int z,
            Keyframe[] keyframes) {}

    private static final int LINEAR = 0;
    private static final int QUAD_EASE_OUT = 5;
    private static final float INVISIBLE_ALPHA = 0.0f;

    private final Track[] tracks;

    LoginRequestLoadingTimeline() {
        tracks = LoginRequestLoadingTimelineData.tracks();
    }

    int trackCount() {
        return tracks.length;
    }

    float frameAtElapsedNanos(long elapsedNanos) {
        if (elapsedNanos < 0L) {
            throw new IllegalArgumentException(
                    "Elapsed time must be non-negative");
        }
        double unwrappedFrame =
                elapsedNanos
                        * (double) BASE_FPS
                        * SPEED
                        / 1_000_000_000.0;
        return wrapFrame((float) (unwrappedFrame % DURATION_FRAMES));
    }

    List<DrawCommand> sampleFrame(float requestedFrame) {
        if (!Float.isFinite(requestedFrame)) {
            throw new IllegalArgumentException("Frame must be finite");
        }
        float frame = wrapFrame(requestedFrame);
        List<DrawCommand> commands = new ArrayList<>(tracks.length);
        for (Track track : tracks) {
            DrawCommand command = sampleTrack(track, frame);
            if (command != null
                    && command.alpha() > INVISIBLE_ALPHA
                    && command.scaleX() != 0.0f
                    && command.scaleY() != 0.0f) {
                commands.add(command);
            }
        }
        commands.sort(Comparator.comparingInt(DrawCommand::z));
        return List.copyOf(commands);
    }

    private static DrawCommand sampleTrack(Track track, float frame) {
        Keyframe[] keyframes = track.keyframes();
        if (keyframes.length == 0 || frame < keyframes[0].frame()) {
            return null;
        }

        Keyframe from = keyframes[keyframes.length - 1];
        Keyframe to = null;
        for (int index = 0; index < keyframes.length; index++) {
            Keyframe candidate = keyframes[index];
            if (candidate.frame() > frame) {
                to = candidate;
                from = keyframes[index - 1];
                break;
            }
            from = candidate;
        }

        float progress = 0.0f;
        if (to != null
                && from.tweenFrame()
                && to.frame() > from.frame()) {
            progress =
                    (frame - from.frame())
                            / (to.frame() - from.frame());
            progress = applyEasing(progress, from.tweenEasing());
        }

        float offsetX = interpolate(from.x(), to, Keyframe::x, progress);
        float offsetY = interpolate(from.y(), to, Keyframe::y, progress);
        float frameScaleX =
                interpolate(
                        from.scaleX(),
                        to,
                        Keyframe::scaleX,
                        progress);
        float frameScaleY =
                interpolate(
                        from.scaleY(),
                        to,
                        Keyframe::scaleY,
                        progress);
        float alpha =
                interpolate(
                        from.alpha(),
                        to,
                        Keyframe::alpha,
                        progress);
        return new DrawCommand(
                track.tile(),
                track.baseX() + offsetX,
                track.baseY() + offsetY,
                track.baseScaleX() * frameScaleX,
                track.baseScaleY() * frameScaleY,
                clamp(alpha, 0.0f, 1.0f),
                track.z());
    }

    private interface KeyframeValue {
        float get(Keyframe keyframe);
    }

    private static float interpolate(
            float start,
            Keyframe end,
            KeyframeValue endValue,
            float progress) {
        if (end == null || progress == 0.0f) {
            return start;
        }
        return start + (endValue.get(end) - start) * progress;
    }

    private static float applyEasing(float progress, int easing) {
        float clamped = clamp(progress, 0.0f, 1.0f);
        return switch (easing) {
            case LINEAR -> clamped;
            case QUAD_EASE_OUT -> -clamped * (clamped - 2.0f);
            default ->
                    throw new IllegalArgumentException(
                            "Unsupported recovered tween easing: " + easing);
        };
    }

    private static float wrapFrame(float frame) {
        float wrapped = frame % DURATION_FRAMES;
        return wrapped < 0.0f ? wrapped + DURATION_FRAMES : wrapped;
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
