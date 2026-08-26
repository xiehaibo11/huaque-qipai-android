package com.huaque.ui;

final class AgreementTextLayoutModel {
    private AgreementTextLayoutModel() {
    }

    static int centeredTop(int originalTop, int originalHeight, int expandedHeight) {
        return originalTop + (originalHeight - expandedHeight) / 2;
    }

    static float scaledTextRatio(
            int originalHeight,
            float originalTextRatio,
            int expandedHeight
    ) {
        return originalHeight * originalTextRatio / expandedHeight;
    }
}
