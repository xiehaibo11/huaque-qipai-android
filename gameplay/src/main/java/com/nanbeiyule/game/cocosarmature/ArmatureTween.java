package com.nanbeiyule.game.cocosarmature;

/**
 * 原版 {@code cocos2d::tweenfunc} 的缓动表。
 *
 * <p>取值来自 CocosStudio 导出的 {@code twE}，与 {@code CCTween::tweenTo} 使用的
 * {@code TweenType} 枚举一一对应：{@code -1 = Linear(MIN)}、{@code 0 = Linear}、
 * {@code 1..3 Sine}、{@code 4..6 Quad}、{@code 7..9 Cubic}、{@code 10..12 Quart}、
 * {@code 13..15 Quint}。仓库内局内动画只用到 0/5/7，其余按同族公式补齐。
 */
final class ArmatureTween {
    private ArmatureTween() {}

    static float apply(int easing, float t) {
        if (t <= 0.0f) return 0.0f;
        if (t >= 1.0f) return 1.0f;
        return switch (easing) {
            case 1 -> (float) (1.0 - Math.cos(t * Math.PI / 2.0));
            case 2 -> (float) Math.sin(t * Math.PI / 2.0);
            case 3 -> (float) (-0.5 * (Math.cos(Math.PI * t) - 1.0));
            case 4 -> t * t;
            case 5 -> -t * (t - 2.0f);
            case 6 -> inOut(t, 2);
            case 7 -> t * t * t;
            case 8 -> shifted(t, 3);
            case 9 -> inOut(t, 3);
            case 10 -> t * t * t * t;
            case 11 -> -shifted(t, 4);
            case 12 -> inOut(t, 4);
            case 13 -> power(t, 5);
            case 14 -> shifted(t, 5);
            case 15 -> inOut(t, 5);
            default -> t;
        };
    }

    private static float power(float t, int exponent) {
        float result = 1.0f;
        for (int i = 0; i < exponent; i++) {
            result *= t;
        }
        return result;
    }

    /** {@code (t-1)^n + 1}，n 为奇数时取正、偶数时取负，对应 EaseOut 家族。 */
    private static float shifted(float t, int exponent) {
        float shifted = t - 1.0f;
        float result = power(shifted, exponent) + 1.0f;
        return exponent % 2 == 0 ? -result + 2.0f : result;
    }

    private static float inOut(float t, int exponent) {
        float scaled = t * 2.0f;
        if (scaled < 1.0f) {
            return 0.5f * power(scaled, exponent);
        }
        float shifted = scaled - 2.0f;
        float tail = power(shifted, exponent);
        return exponent % 2 == 0 ? -0.5f * (tail - 2.0f) : 0.5f * (tail + 2.0f);
    }
}
