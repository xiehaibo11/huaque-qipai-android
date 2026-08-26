package com.nanbeiyule.game.cocosarmature;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

/** 关键帧取样：区间定位、保持帧与缓动插值。 */
public final class ArmatureKeyframesTest {
    private static ArmatureData.Keyframe frame(
            int index, float y, boolean tween, int easing) {
        return new ArmatureData.Keyframe(
                index, 0.0f, y, 1.0f, 1.0f, 0.0f, 0.0f, 0, tween, easing);
    }

    /** 原版 dian 轨道：0 帧 y=0 → 7 帧 y=46（twE=5 Quad_EaseOut）→ 12 帧 y=0 保持。 */
    private static final List<ArmatureData.Keyframe> DIAN =
            List.of(frame(0, 0.0f, true, 5), frame(7, 46.0f, true, 7), frame(12, 0.0f, false, 0));

    @Test
    public void clampsBeforeTheFirstAndAfterTheLastKeyframe() {
        assertEquals(0.0f, ArmatureKeyframes.sample(DIAN, -3.0f).y(), 0.0f);
        assertEquals(0.0f, ArmatureKeyframes.sample(DIAN, 99.0f).y(), 0.0f);
    }

    @Test
    public void appliesTheOriginalQuadEaseOutBetweenTheFirstPair() {
        // t=0.5 时 Quad_EaseOut = -0.5*(0.5-2) = 0.75。
        assertEquals(46.0f * 0.75f, ArmatureKeyframes.sample(DIAN, 3.5f).y(), 0.001f);
    }

    @Test
    public void holdsAFrameThatIsNotMarkedAsTween() {
        List<ArmatureData.Keyframe> held =
                List.of(frame(0, 10.0f, false, 0), frame(10, 90.0f, true, 0));

        assertEquals(10.0f, ArmatureKeyframes.sample(held, 5.0f).y(), 0.0f);
    }

    @Test
    public void identityLeavesTheStaticPoseUntouched() {
        ArmatureData.Keyframe identity = ArmatureKeyframes.identity();

        assertEquals(0.0f, identity.x(), 0.0f);
        assertEquals(1.0f, identity.scaleX(), 0.0f);
        assertEquals(0, identity.displayIndex());
    }
}
