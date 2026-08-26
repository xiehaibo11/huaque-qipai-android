package com.huaque.ui;

final class LobbyIconMotionModel {
    private static final long CYCLE_DURATION_MILLIS = 2667L;
    private static final long SEQUENCE_DURATION_MILLIS = CYCLE_DURATION_MILLIS * 5L;
    private static final long IDLE_PEAK_MILLIS = 1333L;

    private static final int MOTION_TAIZHOU = 0;
    private static final int MOTION_WAHUA = 1;
    private static final int MOTION_SHISANSHUI = 2;

    private static final long[] TAIZHOU_Y_TIMES = {0L, 333L, 500L, 600L, 767L, 2667L};
    private static final float[] TAIZHOU_Y_VALUES = {0f, 0f, -9.21f, -15.25f, 0f, 0f};
    private static final long[] TAIZHOU_SCALE_Y_TIMES =
            {0L, 333L, 500L, 600L, 767L, 900L, 1333L, 2667L};
    private static final float[] TAIZHOU_SCALE_Y_VALUES =
            {1f, 0.897f, 1.018f, 1f, 0.897f, 1f, 0.962f, 1f};

    private static final long[] WAHUA_PULSE_TIMES =
            {0L, 333L, 567L, 800L, 1033L, 1333L, 2667L};
    private static final float[] WAHUA_PULSE_Y_VALUES =
            {0f, 0f, -1.6f, 0f, -1.6f, 0f, 0f};
    private static final float[] WAHUA_PULSE_SCALE_Y_VALUES =
            {1f, 1f, 1.021f, 1f, 1.021f, 1f, 1f};

    private static final long[] SHISANSHUI_TIMES = {0L, 500L, 1333L, 2667L};
    private static final float[] SHISANSHUI_X_VALUES = {0f, -11.2f, 0f, 0f};
    private static final float[] SHISANSHUI_Y_VALUES = {0f, 4.55f, -4.29f, 0f};
    private static final float[] SHISANSHUI_SCALE_X_VALUES = {1f, 1.045f, 1f, 1f};
    private static final float[] SHISANSHUI_SCALE_Y_VALUES = {1f, 1.05f, 1.014f, 1f};

    private LobbyIconMotionModel() {
    }

    static Spec[] specs() {
        return new Spec[]{
                new Spec(R.drawable.lobby_icon_taizhou, 1188, 302, 295, 379,
                        MOTION_TAIZHOU, 2),
                new Spec(R.drawable.lobby_icon_wahua, 1597, 239, 312, 214,
                        MOTION_WAHUA, 3),
                new Spec(R.drawable.lobby_icon_shisanshui, 1555, 532, 356, 223,
                        MOTION_SHISANSHUI, 0)
        };
    }

    static long cycleDurationMillis() {
        return CYCLE_DURATION_MILLIS;
    }

    static long sequenceDurationMillis() {
        return SEQUENCE_DURATION_MILLIS;
    }

    static Frame frameAt(Spec spec, long elapsedMillis) {
        long sequenceTime = elapsedMillis % SEQUENCE_DURATION_MILLIS;
        int cycle = (int) (sequenceTime / CYCLE_DURATION_MILLIS);
        long cycleTime = sequenceTime % CYCLE_DURATION_MILLIS;
        if (cycle == spec.accentCycle) {
            return emphasisFrame(spec.motionKind, cycleTime);
        }
        return idleFrame(spec.motionKind, cycleTime);
    }

    private static Frame idleFrame(int motionKind, long cycleTime) {
        float progress = idleProgress(cycleTime);
        if (motionKind == MOTION_TAIZHOU) {
            return new Frame(0f, 0f, 1f, 1f - 0.03f * progress);
        }
        if (motionKind == MOTION_WAHUA) {
            return new Frame(0f, -4.16f * progress, 1f, 1f);
        }
        return new Frame(0f, -4.29f * progress, 1f, 1f + 0.014f * progress);
    }

    private static Frame emphasisFrame(int motionKind, long cycleTime) {
        if (motionKind == MOTION_TAIZHOU) {
            return new Frame(
                    0f,
                    interpolate(cycleTime, TAIZHOU_Y_TIMES, TAIZHOU_Y_VALUES),
                    1f,
                    interpolate(cycleTime, TAIZHOU_SCALE_Y_TIMES, TAIZHOU_SCALE_Y_VALUES));
        }
        if (motionKind == MOTION_WAHUA) {
            return new Frame(
                    0f,
                    -4.16f * idleProgress(cycleTime)
                            + interpolate(cycleTime, WAHUA_PULSE_TIMES, WAHUA_PULSE_Y_VALUES),
                    1f,
                    interpolate(cycleTime, WAHUA_PULSE_TIMES, WAHUA_PULSE_SCALE_Y_VALUES));
        }
        return new Frame(
                interpolate(cycleTime, SHISANSHUI_TIMES, SHISANSHUI_X_VALUES),
                interpolate(cycleTime, SHISANSHUI_TIMES, SHISANSHUI_Y_VALUES),
                interpolate(cycleTime, SHISANSHUI_TIMES, SHISANSHUI_SCALE_X_VALUES),
                interpolate(cycleTime, SHISANSHUI_TIMES, SHISANSHUI_SCALE_Y_VALUES));
    }

    private static float idleProgress(long cycleTime) {
        float linearProgress;
        if (cycleTime <= IDLE_PEAK_MILLIS) {
            linearProgress = cycleTime / (float) IDLE_PEAK_MILLIS;
        } else {
            linearProgress = (CYCLE_DURATION_MILLIS - cycleTime)
                    / (float) (CYCLE_DURATION_MILLIS - IDLE_PEAK_MILLIS);
        }
        return linearProgress * linearProgress * (3f - 2f * linearProgress);
    }

    private static float interpolate(long time, long[] times, float[] values) {
        for (int i = 1; i < times.length; i++) {
            if (time <= times[i]) {
                float progress = (time - times[i - 1]) / (float) (times[i] - times[i - 1]);
                return values[i - 1] + (values[i] - values[i - 1]) * progress;
            }
        }
        return values[values.length - 1];
    }

    static final class Spec {
        private final int drawableResId;
        private final int psdX;
        private final int psdY;
        private final int psdWidth;
        private final int psdHeight;
        private final int motionKind;
        private final int accentCycle;

        Spec(
                int drawableResId,
                int psdX,
                int psdY,
                int psdWidth,
                int psdHeight,
                int motionKind,
                int accentCycle) {
            this.drawableResId = drawableResId;
            this.psdX = psdX;
            this.psdY = psdY;
            this.psdWidth = psdWidth;
            this.psdHeight = psdHeight;
            this.motionKind = motionKind;
            this.accentCycle = accentCycle;
        }

        int drawableResId() {
            return drawableResId;
        }

        int psdX() {
            return psdX;
        }

        int psdY() {
            return psdY;
        }

        int psdWidth() {
            return psdWidth;
        }

        int psdHeight() {
            return psdHeight;
        }

        int accentCycle() {
            return accentCycle;
        }
    }

    static final class Frame {
        private final float translationXPsdPixels;
        private final float translationYPsdPixels;
        private final float scaleX;
        private final float scaleY;

        Frame(
                float translationXPsdPixels,
                float translationYPsdPixels,
                float scaleX,
                float scaleY) {
            this.translationXPsdPixels = translationXPsdPixels;
            this.translationYPsdPixels = translationYPsdPixels;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
        }

        float translationXPsdPixels() {
            return translationXPsdPixels;
        }

        float translationYPsdPixels() {
            return translationYPsdPixels;
        }

        float scaleX() {
            return scaleX;
        }

        float scaleY() {
            return scaleY;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Frame)) {
                return false;
            }
            Frame frame = (Frame) other;
            return Float.compare(translationXPsdPixels, frame.translationXPsdPixels) == 0
                    && Float.compare(translationYPsdPixels, frame.translationYPsdPixels) == 0
                    && Float.compare(scaleX, frame.scaleX) == 0
                    && Float.compare(scaleY, frame.scaleY) == 0;
        }

        @Override
        public int hashCode() {
            int result = Float.floatToIntBits(translationXPsdPixels);
            result = 31 * result + Float.floatToIntBits(translationYPsdPixels);
            result = 31 * result + Float.floatToIntBits(scaleX);
            return 31 * result + Float.floatToIntBits(scaleY);
        }
    }
}
