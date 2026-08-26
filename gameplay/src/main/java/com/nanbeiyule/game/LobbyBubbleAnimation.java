package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;

/**
 * 原版 BubbleItem 的播放时间线，逐段对应 {@code BubbleItem.lua:playBubbleAction}。
 *
 * <p>缓动按 cocos2d-x 定义实现：{@code EaseIn(rate)} 是 {@code t^rate}，
 * {@code EaseOut(rate)} 是 {@code t^(1/rate)}，{@code EaseInOut(rate)} 分半段对称。
 *
 * <p>原版 type 4 是 {@code Repeat(animAction, num)} 且要求 {@code num > 0}，
 * 当前 Game Home API 不下发次数，因此和原版 else 分支一样不播放。
 */
final class LobbyBubbleAnimation {

    /** 某一时刻的气泡状态；{@code offsetY} 为原版设计单位。 */
    record Frame(boolean visible, float scaleX, float scaleY, float alpha, float offsetY) {}

    /** 一段动作：持续时间加上把段内进度映射成状态的函数。 */
    private record Segment(float seconds, Interpolator interpolator) {}

    private interface Interpolator {
        Frame at(float progress);
    }

    private static final Frame HIDDEN = new Frame(false, 0.0f, 0.0f, 0.0f, 0.0f);
    private static final Frame RESTING = new Frame(true, 1.0f, 1.0f, 1.0f, 0.0f);

    private final List<Segment> segments;
    private final float cycleSeconds;

    private LobbyBubbleAnimation(List<Segment> segments) {
        this.segments = List.copyOf(segments);
        float total = 0.0f;
        for (Segment segment : segments) {
            total += segment.seconds();
        }
        this.cycleSeconds = total;
    }

    /**
     * @param type 原版 {@code getBubbleType()}
     * @param intervalSeconds 原版 {@code delayTime}
     * @return 不可播放的类型返回 null
     */
    static LobbyBubbleAnimation of(Integer type, float intervalSeconds) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case 2 -> new LobbyBubbleAnimation(typeTwo(intervalSeconds));
            case 3 -> new LobbyBubbleAnimation(typeThree(intervalSeconds));
            default -> null;
        };
    }

    float cycleSeconds() {
        return cycleSeconds;
    }

    Frame frameAt(float elapsedSeconds) {
        float time = elapsedSeconds % cycleSeconds;
        if (time < 0.0f) {
            time += cycleSeconds;
        }
        for (Segment segment : segments) {
            if (time < segment.seconds() || segment.seconds() <= 0.0f) {
                float progress = segment.seconds() <= 0.0f ? 1.0f : time / segment.seconds();
                return segment.interpolator().at(progress);
            }
            time -= segment.seconds();
        }
        return RESTING;
    }

    // EaseOut(FadeTo(0.1,0),0.2) → ScaleTo(0,0) → Delay(interval)
    // → Spawn(EaseIn(ScaleTo(0.4,1),0.5), EaseInOut(FadeTo(0.3,255),0.3)) → Delay(8)
    private static List<Segment> typeTwo(float intervalSeconds) {
        List<Segment> segments = new ArrayList<>();
        segments.add(
                new Segment(
                        0.1f,
                        progress ->
                                new Frame(true, 1.0f, 1.0f, 1.0f - easeOut(progress, 0.2f), 0.0f)));
        segments.add(new Segment(intervalSeconds, progress -> HIDDEN));
        segments.add(
                new Segment(
                        0.4f,
                        progress -> {
                            float scale = easeIn(progress, 0.5f);
                            // 透明度只用 0.3 秒完成，之后保持满值。
                            float fade = Math.min(1.0f, progress * 0.4f / 0.3f);
                            return new Frame(
                                    true, scale, scale, easeInOut(fade, 0.3f), 0.0f);
                        }));
        segments.add(new Segment(8.0f, progress -> RESTING));
        return segments;
    }

    // Spawn(ScaleTo(0.2,1),FadeTo(0.2,255)) → EaseIn(ScaleTo(0.1,1,0.618),0.1)
    // → Spawn(EaseIn(MoveTo(0.2,(0,15)),0.2), EaseIn(ScaleTo(0.2,0.618,1),0.2)) → Delay(0.04)
    // → Spawn(EaseOut(MoveTo(0.2,(0,0)),0.2), EaseOut(ScaleTo(0.2,1,1),0.2))
    // → EaseIn(ScaleTo(0.2,1,0.618),0.2) → EaseOut(ScaleTo(0.2,1,1),0.2) → Delay(8)
    // → EaseOut(FadeTo(0.05,0),0.05) → ScaleTo(0,0) → Delay(interval)
    private static List<Segment> typeThree(float intervalSeconds) {
        List<Segment> segments = new ArrayList<>();
        segments.add(
                new Segment(
                        0.2f,
                        progress -> new Frame(true, progress, progress, progress, 0.0f)));
        segments.add(
                new Segment(
                        0.1f,
                        progress ->
                                new Frame(
                                        true,
                                        1.0f,
                                        lerp(1.0f, 0.618f, easeIn(progress, 0.1f)),
                                        1.0f,
                                        0.0f)));
        segments.add(
                new Segment(
                        0.2f,
                        progress -> {
                            float eased = easeIn(progress, 0.2f);
                            return new Frame(
                                    true,
                                    lerp(1.0f, 0.618f, eased),
                                    lerp(0.618f, 1.0f, eased),
                                    1.0f,
                                    lerp(0.0f, 15.0f, eased));
                        }));
        segments.add(
                new Segment(
                        0.04f,
                        progress -> new Frame(true, 0.618f, 1.0f, 1.0f, 15.0f)));
        segments.add(
                new Segment(
                        0.2f,
                        progress -> {
                            float eased = easeOut(progress, 0.2f);
                            return new Frame(
                                    true,
                                    lerp(0.618f, 1.0f, eased),
                                    1.0f,
                                    1.0f,
                                    lerp(15.0f, 0.0f, eased));
                        }));
        segments.add(
                new Segment(
                        0.2f,
                        progress ->
                                new Frame(
                                        true,
                                        1.0f,
                                        lerp(1.0f, 0.618f, easeIn(progress, 0.2f)),
                                        1.0f,
                                        0.0f)));
        segments.add(
                new Segment(
                        0.2f,
                        progress ->
                                new Frame(
                                        true,
                                        1.0f,
                                        lerp(0.618f, 1.0f, easeOut(progress, 0.2f)),
                                        1.0f,
                                        0.0f)));
        segments.add(new Segment(8.0f, progress -> RESTING));
        segments.add(
                new Segment(
                        0.05f,
                        progress ->
                                new Frame(
                                        true, 1.0f, 1.0f, 1.0f - easeOut(progress, 0.05f), 0.0f)));
        segments.add(new Segment(intervalSeconds, progress -> HIDDEN));
        return segments;
    }

    private static float lerp(float from, float to, float progress) {
        return from + (to - from) * progress;
    }

    private static float easeIn(float progress, float rate) {
        return (float) Math.pow(clamp(progress), rate);
    }

    private static float easeOut(float progress, float rate) {
        return (float) Math.pow(clamp(progress), 1.0f / rate);
    }

    private static float easeInOut(float progress, float rate) {
        float time = clamp(progress) * 2.0f;
        if (time < 1.0f) {
            return 0.5f * (float) Math.pow(time, rate);
        }
        return 1.0f - 0.5f * (float) Math.pow(2.0f - time, rate);
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
