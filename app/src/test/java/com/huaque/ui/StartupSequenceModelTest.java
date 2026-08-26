package com.huaque.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StartupSequenceModelTest {
    @Test
    public void keepsLogoVisibleLongEnoughToRead() {
        assertEquals(3000L, StartupSequenceModel.LOGO_SPLASH_MILLIS);
        assertTrue(StartupSequenceModel.LOGO_SPLASH_MILLIS >= 3000L);
    }

    @Test
    public void keepsExistingBrandPageBeforeLoading() {
        assertEquals(3000L, StartupSequenceModel.BRAND_PAGE_MILLIS);
    }
}
