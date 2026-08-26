package com.nanbeiyule.game;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MainActivityShopFlowTest {
    @Test
    public void shopCanOpenWithoutGameHomeData() {
        assertTrue(MainActivityShopFlow.canShowShop(false, false));
    }
}
