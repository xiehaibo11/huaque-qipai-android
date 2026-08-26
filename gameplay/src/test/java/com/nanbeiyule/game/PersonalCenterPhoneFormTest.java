package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public final class PersonalCenterPhoneFormTest {
    @Test
    public void normalizesMainlandPhoneAndVerificationCode() {
        PersonalCenterPhoneForm form =
                PersonalCenterPhoneForm.validated(
                        "+86 138-0013-8000", " 123456 ");

        assertEquals("13800138000", form.phoneNumber());
        assertEquals("123456", form.code());
    }

    @Test
    public void rejectsInvalidPhoneAndCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PersonalCenterPhoneForm.phoneOnly("12345"));
        assertThrows(
                IllegalArgumentException.class,
                () -> PersonalCenterPhoneForm.validated("13800138000", "12"));
    }
}
