package com.nanbeiyule.game;

/**
 * Source rectangles for the recovered Xianyi Dou Dizhu QuicklyJoin number strips.
 *
 * <p>The QuicklyJoin CSB and bitmap-font descriptor are absent from the archive. The keypad
 * bounds below are therefore inferred from {@code font_num_1.png}'s transparent-pixel boundaries;
 * the slot font is a confirmed ten-cell {@code 320x49} strip. These are image-atlas crops, not
 * recovered scene coordinates.
 */
final class XianyiJoinRoomNumberAtlas {
    private static final SourceRect[] KEYPAD_DIGITS = {
        new SourceRect(76, 0, 113, 52),
        new SourceRect(120, 0, 143, 52),
        new SourceRect(154, 0, 188, 52),
        new SourceRect(191, 0, 225, 52),
        new SourceRect(228, 0, 265, 52),
        new SourceRect(267, 0, 301, 52),
        new SourceRect(305, 0, 340, 52),
        new SourceRect(343, 0, 379, 52),
        new SourceRect(381, 0, 416, 52),
        new SourceRect(419, 0, 454, 52)
    };

    private static final SourceRect[] SLOT_DIGITS = {
        new SourceRect(0, 0, 32, 49),
        new SourceRect(32, 0, 64, 49),
        new SourceRect(64, 0, 96, 49),
        new SourceRect(96, 0, 128, 49),
        new SourceRect(128, 0, 160, 49),
        new SourceRect(160, 0, 192, 49),
        new SourceRect(192, 0, 224, 49),
        new SourceRect(224, 0, 256, 49),
        new SourceRect(256, 0, 288, 49),
        new SourceRect(288, 0, 320, 49)
    };

    private XianyiJoinRoomNumberAtlas() {}

    static SourceRect keypadDigit(int digit) {
        return digit(KEYPAD_DIGITS, digit);
    }

    static SourceRect slotDigit(int digit) {
        return digit(SLOT_DIGITS, digit);
    }

    private static SourceRect digit(SourceRect[] digits, int digit) {
        if (digit < 0 || digit >= digits.length) {
            throw new IllegalArgumentException("digit must be between 0 and 9");
        }
        return digits[digit];
    }

    record SourceRect(int left, int top, int right, int bottom) {
        SourceRect {
            if (right <= left || bottom <= top) {
                throw new IllegalArgumentException("source rectangle must have positive size");
            }
        }

        int width() {
            return right - left;
        }

        int height() {
            return bottom - top;
        }
    }
}
