package com.huaque.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ProductionAuthEndpointTest {
    @Test
    public void debugBuildUsesProductionAuthEndpoint() {
        assertEquals("https://api.nanbeiyule.com", BuildConfig.AUTH_BASE_URL);
    }
}
