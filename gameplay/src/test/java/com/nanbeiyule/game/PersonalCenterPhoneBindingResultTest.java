package com.nanbeiyule.game;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public class PersonalCenterPhoneBindingResultTest {
    @Test
    public void readsWhetherAccountLinkingRequiresLoginAgain() throws Exception {
        PersonalCenterApiClient.PhoneBindingResult linked =
                PersonalCenterApiClient.PhoneBindingResult.fromJson(
                        new JSONObject(
                                "{\"maskedPhone\":\"138****8000\","
                                        + "\"reloginRequired\":true}"));
        PersonalCenterApiClient.PhoneBindingResult regular =
                PersonalCenterApiClient.PhoneBindingResult.fromJson(
                        new JSONObject(
                                "{\"maskedPhone\":\"138****8000\"}"));

        assertTrue(linked.reloginRequired());
        assertFalse(regular.reloginRequired());
    }
}
