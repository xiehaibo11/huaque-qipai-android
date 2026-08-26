package com.huaque.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LoginAgreementModelTest {
    @Test
    public void startsUncheckedUntilTheCurrentAgreementIsAccepted() {
        assertFalse(LoginAgreementModel.DEFAULT_ACCEPTED);
    }

    @Test
    public void promptsOnFirstLaunch() {
        assertTrue(LoginAgreementModel.requiresPrompt("", "2026-08-23"));
    }

    @Test
    public void skipsPromptAfterAcceptingTheCurrentVersion() {
        assertFalse(LoginAgreementModel.requiresPrompt("2026-08-23", "2026-08-23"));
    }

    @Test
    public void promptsAgainWhenTheAgreementVersionChanges() {
        assertTrue(LoginAgreementModel.requiresPrompt("2026-08-23", "2026-09-01"));
    }

    @Test
    public void togglesAgreementStateOnEveryTap() {
        assertFalse(LoginAgreementModel.toggle(true));
        assertTrue(LoginAgreementModel.toggle(false));
    }

    @Test
    public void onlyAllowsLoginWhenAgreementIsAccepted() {
        assertTrue(LoginAgreementModel.canContinue(true));
        assertFalse(LoginAgreementModel.canContinue(false));
    }
}
