package com.huaque.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LoginLoadingModelTest {
    @Test
    public void loadingIsVisibleWhileVerifyingAndUntilAuthenticatedPageChanges() {
        assertTrue(LoginLoadingModel.isVisible("VERIFYING", false));
        assertTrue(LoginLoadingModel.isVisible("AUTHENTICATED", true));
        assertFalse(LoginLoadingModel.isVisible("ERROR", false));
        assertFalse(LoginLoadingModel.isVisible("IDLE", false));
    }

    @Test
    public void animationProgressLoopsAndUsesLoginLabel() {
        assertEquals(99, LoginLoadingModel.nextProgress(98));
        assertEquals(0, LoginLoadingModel.nextProgress(99));
        assertEquals("正在登录", LoginLoadingModel.LOGIN_LABEL);
    }
}
