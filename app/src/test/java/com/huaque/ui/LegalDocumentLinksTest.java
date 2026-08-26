package com.huaque.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class LegalDocumentLinksTest {
    @Test
    public void buildsUserServiceUrlFromFrontendBaseUrl() {
        assertEquals(
                "https://www.nanbeiyule.com/terms",
                LegalDocumentLinks.userServiceUrl("https://www.nanbeiyule.com/"));
    }

    @Test
    public void buildsPrivacyPolicyUrlFromFrontendBaseUrl() {
        assertEquals(
                "https://www.nanbeiyule.com/privacy",
                LegalDocumentLinks.privacyPolicyUrl("https://www.nanbeiyule.com"));
    }
}
