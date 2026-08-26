package com.huaque.ui;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class AgreementDialogLayoutModelTest {
    @Test
    public void matchesOriginalDialogBoundsOnCurrentCanvas() {
        assertArrayEquals(
                new int[]{279, 84, 1362, 913},
                AgreementDialogLayoutModel.dialogBounds(1920, 1080));
    }

    @Test
    public void mapsLegacyChildrenInsideOriginalDialogBounds() {
        int[] panel = AgreementDialogLayoutModel.dialogBounds(1920, 1080);

        assertArrayEquals(
                new int[]{663, 864, 222, 89},
                AgreementDialogLayoutModel.mapLegacyBox(panel, 437, 874, 252, 100));
        assertArrayEquals(
                new int[]{1042, 864, 222, 90},
                AgreementDialogLayoutModel.mapLegacyBox(panel, 868, 874, 252, 101));
    }
}
