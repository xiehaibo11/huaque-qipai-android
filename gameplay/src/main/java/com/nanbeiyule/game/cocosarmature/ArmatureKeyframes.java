package com.nanbeiyule.game.cocosarmature;

import java.util.List;

/** 骨骼轨道的关键帧取样：区间定位、缓动与线性插值。 */
final class ArmatureKeyframes {
    private static final ArmatureData.Keyframe IDENTITY =
            new ArmatureData.Keyframe(0, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0, false, 0);

    private ArmatureKeyframes() {}

    static ArmatureData.Keyframe identity() {
        return IDENTITY;
    }

    static ArmatureData.Keyframe sample(List<ArmatureData.Keyframe> keyframes, float frame) {
        ArmatureData.Keyframe first = keyframes.get(0);
        if (frame <= first.frameIndex() || keyframes.size() == 1) {
            return first;
        }
        ArmatureData.Keyframe last = keyframes.get(keyframes.size() - 1);
        if (frame >= last.frameIndex()) {
            return last;
        }
        int index = 0;
        while (index + 1 < keyframes.size()
                && keyframes.get(index + 1).frameIndex() <= frame) {
            index++;
        }
        ArmatureData.Keyframe from = keyframes.get(index);
        ArmatureData.Keyframe to = keyframes.get(index + 1);
        // tweenFrame=false 表示该帧保持到下一帧（原版 CCTween 的 kf->isTween 判定）。
        if (!from.tween()) {
            return from;
        }
        int span = to.frameIndex() - from.frameIndex();
        if (span <= 0) {
            return from;
        }
        float progress =
                ArmatureTween.apply(from.easing(), (frame - from.frameIndex()) / span);
        return new ArmatureData.Keyframe(
                from.frameIndex(),
                lerp(from.x(), to.x(), progress),
                lerp(from.y(), to.y(), progress),
                lerp(from.scaleX(), to.scaleX(), progress),
                lerp(from.scaleY(), to.scaleY(), progress),
                lerp(from.skewX(), to.skewX(), progress),
                lerp(from.skewY(), to.skewY(), progress),
                from.displayIndex(),
                true,
                from.easing());
    }

    private static float lerp(float from, float to, float progress) {
        return from + (to - from) * progress;
    }
}
