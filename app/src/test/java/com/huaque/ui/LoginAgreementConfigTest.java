package com.huaque.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class LoginAgreementConfigTest {
    private static final String LEGAL_BASE_URL = "https://www.nanbeiyule.com";

    @Test
    public void readsServerAndPrivacyLinksFromNanbeiConfiguration() {
        LoginAgreementConfig config =
                LoginAgreementConfig.fromJson(
                        """
                        {
                          "operatorName": "南北娱乐",
                          "version": "2026-09-01",
                          "agreements": [
                            {
                              "type": "SERVER",
                              "url": "https://www.nanbeiyule.com/terms?v=2"
                            },
                            {
                              "type": "PRIVACY",
                              "url": "https://www.nanbeiyule.com/privacy?v=2"
                            }
                          ]
                        }
                        """,
                        LEGAL_BASE_URL);

        assertEquals(
                "https://www.nanbeiyule.com/terms?v=2", config.userServiceUrl());
        assertEquals(
                "https://www.nanbeiyule.com/privacy?v=2", config.privacyPolicyUrl());
        assertEquals("2026-09-01", config.version());
    }

    @Test
    public void rejectsAnotherOperatorAndUntrustedAgreementHosts() {
        LoginAgreementConfig wrongOperator =
                LoginAgreementConfig.fromJson(
                        """
                        {
                          "operatorName": "其他运营方",
                          "agreements": [
                            {"type": "SERVER", "url": "https://www.nanbeiyule.com/new-terms"}
                          ]
                        }
                        """,
                        LEGAL_BASE_URL);
        LoginAgreementConfig wrongHost =
                LoginAgreementConfig.fromJson(
                        """
                        {
                          "operatorName": "南北娱乐",
                          "agreements": [
                            {"type": "PRIVACY", "url": "https://example.com/privacy"}
                          ]
                        }
                        """,
                        LEGAL_BASE_URL);

        assertEquals(
                "https://www.nanbeiyule.com/terms", wrongOperator.userServiceUrl());
        assertEquals(
                "https://www.nanbeiyule.com/privacy", wrongHost.privacyPolicyUrl());
    }

    @Test
    public void fallsBackWhenTheResponseIsMalformed() {
        LoginAgreementConfig config =
                LoginAgreementConfig.fromJson("not-json", LEGAL_BASE_URL);

        assertEquals("https://www.nanbeiyule.com/terms", config.userServiceUrl());
        assertEquals("https://www.nanbeiyule.com/privacy", config.privacyPolicyUrl());
        assertEquals("2026-08-23", config.version());
    }
}
