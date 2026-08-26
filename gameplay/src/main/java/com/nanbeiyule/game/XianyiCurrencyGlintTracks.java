package com.nanbeiyule.game;

/** Exact keyframes decoded from the original Cocos Studio currency-effect CSBs. */
final class XianyiCurrencyGlintTracks {
    record Track(float[] frames, float[] values) {}

    record Star(Track x, Track y, Track scale, Track rotation, Track alpha) {}

    static final Star[] DOU = {
        star(
                track(
                        frames(86, 91, 96, 101, 106, 111, 116, 121, 126, 131, 136, 141, 146, 151, 156, 161, 166),
                        0.2015f, -5.5737f, -11.0867f, -18.1129f, -20.4136f, -21.2765f,
                        -20.5643f, -16.6896f, -10.7148f, -5.638f, -0.5612f, 4.5813f,
                        10.249f, 12.9025f, 14.234f, 15.5471f, 17.9098f),
                track(
                        frames(86, 91, 96, 101, 106, 111, 116, 121, 126, 131, 136, 141, 146, 151, 156, 161, 166),
                        -22.8094f, -20.9717f, -18.8717f, -12.5244f, -5.6522f, 0.2106f,
                        8.173f, 14.8732f, 19.9981f, 22.0573f, 23.5912f, 22.5658f,
                        20.4902f, 16.0567f, 12.6635f, 5.8383f, 1.3756f),
                track(
                        frames(86, 91, 96, 101, 106, 111, 116, 126, 136, 146, 156, 161, 166),
                        0.0754f, 0.1081f, 0.1774f, 0.1865f, 0.1956f, 0.2046f,
                        0.2137f, 0.2318f, 0.2137f, 0.1956f, 0.1774f, 0.1348f, 0.0761f),
                track(
                        frames(86, 91, 96, 101, 106, 111, 116, 121, 126, 131, 136, 141, 146, 151, 156, 161, 166),
                        -102.2814f, -52.5829f, -22.1406f, 11.5495f, 45.2395f, 78.9296f,
                        112.6198f, 146.3098f, 180f, 210.0001f, 240.0002f, 270f,
                        299.9999f, 335.9999f, 360f, 380f, 400.0007f),
                track(frames(86, 91, 96, 156, 161, 166), 0, 127, 255, 255, 147, 0)),
        star(
                track(frames(20, 35, 45, 60), -14.9947f, -16.365f, -16.365f, -14.9947f),
                track(frames(20, 35, 45, 60), 15.2481f, 16.2759f, 16.2759f, 15.2481f),
                track(frames(20, 35, 45, 60), 0.0587f, 0.1419f, 0.1419f, 0.0738f),
                track(frames(35, 45, 60), 35, 50, 90),
                track(frames(20, 35, 45, 60), 0, 255, 255, 0)),
        star(
                fixed(-3.6895f),
                fixed(-20.723f),
                track(frames(93, 108, 118, 133), 0.0685f, 0.1419f, 0.1419f, 0.0685f),
                track(frames(93, 108, 118, 133), -65.6575f, 0, 35, 90),
                track(frames(93, 108, 118, 133), 0, 255, 255, 0)),
        star(
                fixed(12.7546f),
                fixed(4.6284f),
                track(frames(181, 197, 208, 228), 0.0889f, 0.1419f, 0.1419f, 0.0813f),
                track(frames(181, 197, 208, 228), -68.5753f, 0, 32.034f, 136.0357f),
                track(frames(181, 197, 208, 228), 0, 255, 255, 0))
    };

    static final Star[] GOLD = {
        star(
                track(
                        frames(40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 105, 110, 115, 120, 265),
                        0.2015f, -5.5737f, -11.0867f, -17.1129f, -21.4136f, -22.2765f,
                        -20.5643f, -16.6896f, -10.7148f, -5.638f, -0.5612f, 4.5813f,
                        11.2491f, 15.9026f, 19.234f, 21.547f, 21.9099f, 17.9098f),
                track(
                        frames(40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 105, 110, 115, 120, 265),
                        -20.8094f, -19.9717f, -17.8717f, -12.5244f, -5.6522f, 0.2106f,
                        8.173f, 14.8732f, 19.9981f, 23.0573f, 23.5912f, 23.5658f,
                        20.4902f, 17.0567f, 12.6635f, 5.8384f, 1.3756f, 1.3756f),
                track(
                        frames(40, 45, 50, 55, 60, 65, 70, 80, 90, 100, 110, 115, 120),
                        0.0754f, 0.1081f, 0.1774f, 0.1865f, 0.1956f, 0.2046f,
                        0.2137f, 0.2318f, 0.2137f, 0.1956f, 0.1774f, 0.1348f, 0.0761f),
                track(
                        frames(40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 105, 110, 115, 120),
                        -102.2814f, -52.5829f, -22.1406f, 11.5495f, 45.2395f, 78.9296f,
                        112.6198f, 146.3098f, 180f, 210.0001f, 240.0002f, 270f,
                        299.9999f, 335.9999f, 360f, 380f, 400.0007f),
                track(frames(40, 45, 50, 110, 115, 120), 0, 127, 255, 255, 147, 0)),
        star(
                track(frames(135, 150, 160, 175), -16.9948f, -16.365f, -16.365f, -16.9947f),
                track(frames(135, 150, 160, 175), 16.2482f, 16.2759f, 16.2759f, 17.2481f),
                track(frames(135, 150, 160, 175), 0.0587f, 0.2f, 0.2f, 0.0738f),
                track(frames(150, 160, 175), 35, 50, 90),
                track(frames(135, 150, 160, 175), 0, 255, 255, 0)),
        star(
                track(frames(67, 82, 92, 107), -10.6694f, -11.5422f, -12.1237f, -12.1236f),
                track(frames(67, 82, 92, 107), -18.1054f, -17.2329f, -17.5238f, -18.1052f),
                track(frames(67, 82, 92, 107), 0.0685f, 0.1419f, 0.1419f, 0.0685f),
                track(frames(67, 82, 92, 107), -65.658f, 0, 35, 90),
                track(frames(67, 82, 92, 107), 0, 255, 255, 0)),
        star(
                track(frames(196, 212, 223, 243), 15.3724f, 16.2448f, 15.3723f, 15.0816f),
                track(frames(196, 212, 223, 243), 9.8637f, 10.4453f, 10.7362f, 10.1545f),
                track(frames(196, 212, 223, 243), 0.0889f, 0.1419f, 0.1419f, 0.0813f),
                track(frames(196, 212, 223, 243), -68.5757f, 0, 32.034f, 136.0356f),
                track(frames(196, 212, 223, 243), 0, 255, 255, 0))
    };

    static final Track[] CARD_SWEEP_ALPHA = {
        track(frames(0, 1, 4, 5), 0, 153, 153, 0),
        track(frames(4, 5, 10, 11), 0, 155, 158, 0),
        track(frames(10, 11, 18, 19), 0, 160, 163, 0),
        track(frames(18, 19, 23, 24), 0, 158, 155, 0),
        track(frames(23, 24, 28, 29), 0, 153, 153, 0)
    };

    static final Star CARD_STAR =
            star(
                    fixed(24.5507f),
                    fixed(18.2328f),
                    track(frames(30, 45, 60), 0.1f, 0.7f, 0.4f),
                    track(frames(30, 45, 60), 0, 90, 180),
                    track(frames(30, 45, 60), 0, 255, 0));

    private XianyiCurrencyGlintTracks() {}

    private static Star star(Track x, Track y, Track scale, Track rotation, Track alpha) {
        return new Star(x, y, scale, rotation, alpha);
    }

    private static Track fixed(float value) {
        return track(frames(0), value);
    }

    private static Track track(float[] frames, float... values) {
        return new Track(frames, values);
    }

    private static float[] frames(float... frames) {
        return frames;
    }
}
