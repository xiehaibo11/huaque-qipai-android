package com.huaque.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AgreementTextLayoutModelTest {
    @Test
    public void expandedLineBoxesKeepTheirTextCenters() {
        assertCenteredAtSamePosition(233, 47, 75);
        assertCenteredAtSamePosition(371, 56, 76);
        assertCenteredAtSamePosition(463, 56, 76);
        assertCenteredAtSamePosition(608, 54, 80);
    }

    @Test
    public void expandedLineBoxesKeepTheirTextSize() {
        assertEquals(47f * 0.95f,
                75f * AgreementTextLayoutModel.scaledTextRatio(47, 0.95f, 75),
                0.001f);
        assertEquals(56f * 0.82f,
                76f * AgreementTextLayoutModel.scaledTextRatio(56, 0.82f, 76),
                0.001f);
        assertEquals(54f * 0.88f,
                80f * AgreementTextLayoutModel.scaledTextRatio(54, 0.88f, 80),
                0.001f);
    }

    private static void assertCenteredAtSamePosition(
            int originalTop,
            int originalHeight,
            int expandedHeight
    ) {
        int expandedTop = AgreementTextLayoutModel.centeredTop(
                originalTop,
                originalHeight,
                expandedHeight);
        assertEquals(
                originalTop * 2 + originalHeight,
                expandedTop * 2 + expandedHeight);
    }
}
