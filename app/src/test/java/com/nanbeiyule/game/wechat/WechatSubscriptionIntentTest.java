package com.nanbeiyule.game.wechat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import org.junit.Test;

public final class WechatSubscriptionIntentTest {
    @Test
    public void openSdkSubscriptionContractIsType18() {
        assertEquals(18, ConstantsAPI.COMMAND_SUBSCRIBE_MESSAGE);
        assertEquals(620756998,
                com.tencent.mm.opensdk.constants.Build.SUBSCRIBE_MESSAGE_SUPPORTED_SDK_INT);
    }

    @Test
    public void validatesEveryServerSuppliedFieldStrictly() {
        long future = 2_000L;
        new WechatSubscriptionIntent("intent-1", "template", 0, "Abc123", future);
        new WechatSubscriptionIntent("intent-2", "template", 10_000, "z9", future);

        assertThrows(IllegalArgumentException.class,
                () -> new WechatSubscriptionIntent("", "template", 1, "Abc", future));
        assertThrows(IllegalArgumentException.class,
                () -> new WechatSubscriptionIntent("intent", "", 1, "Abc", future));
        assertThrows(IllegalArgumentException.class,
                () -> new WechatSubscriptionIntent("intent", "template", -1, "Abc", future));
        assertThrows(IllegalArgumentException.class,
                () -> new WechatSubscriptionIntent("intent", "template", 10_001, "Abc", future));
        assertThrows(IllegalArgumentException.class,
                () -> new WechatSubscriptionIntent("intent", "template", 1, "a-b", future));
        assertThrows(IllegalArgumentException.class,
                () -> new WechatSubscriptionIntent("intent", "template", 1,
                        "a".repeat(129), future));
    }
}
