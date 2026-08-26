package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PersonalCenterPhoneLayoutTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void matchesOriginalPhoneBindingGeometryWithoutOverlap() {
        ShopLayout.Rect phone = PersonalCenterPhoneLayout.PHONE_INPUT;
        ShopLayout.Rect code = PersonalCenterPhoneLayout.CODE_INPUT;
        ShopLayout.Rect send = PersonalCenterPhoneLayout.SEND_CODE;

        assertEquals(480f, phone.width(), EPSILON);
        assertEquals(79f, phone.height(), EPSILON);
        assertEquals(261f, code.width(), EPSILON);
        assertEquals(79f, code.height(), EPSILON);
        assertEquals(303f, send.width(), EPSILON);
        assertEquals(110f, send.height(), EPSILON);
        assertTrue(code.right() < send.left());
        assertEquals(7.512f, send.left() - code.right(), EPSILON);
        assertEquals(3f, code.centerY() - send.centerY(), EPSILON);
    }
}
