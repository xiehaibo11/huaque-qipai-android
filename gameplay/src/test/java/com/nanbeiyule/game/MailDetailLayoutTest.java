package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MailDetailLayoutTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void usesTheRecoveredMailDetailLayerGeometry() {
        assertBox(MailLayout.DETAIL_CLOSE, 1676.72f, 82.29f, 1776.72f, 182.29f);
        assertBox(MailLayout.DETAIL_TITLE, 427f, 204f, 1627f, 256f);
        assertBox(MailLayout.DETAIL_CONTENT, 421f, 328f, 1661f, 778f);
        assertBox(MailLayout.DETAIL_AWARD_LIST, 635f, 695f, 1435f, 895f);
        assertBox(MailLayout.DETAIL_DELETE_ONLY, 897.5f, 906.5f, 1162.5f, 993.5f);
        assertBox(MailLayout.DETAIL_DELETE, 747.5f, 906.5f, 1012.5f, 993.5f);
        assertBox(MailLayout.DETAIL_CLAIM, 1050.5f, 906.5f, 1315.5f, 993.5f);
    }

    private static void assertBox(
            MailLayout.Box actual, float left, float top, float right, float bottom) {
        assertEquals(left, actual.left(), EPSILON);
        assertEquals(top, actual.top(), EPSILON);
        assertEquals(right, actual.right(), EPSILON);
        assertEquals(bottom, actual.bottom(), EPSILON);
    }
}
