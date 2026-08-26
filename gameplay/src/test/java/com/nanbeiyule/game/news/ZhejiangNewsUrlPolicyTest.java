package com.nanbeiyule.game.news;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ZhejiangNewsUrlPolicyTest {
    private final ZhejiangNewsUrlPolicy policy = new ZhejiangNewsUrlPolicy();

    @Test
    public void permitsCurrentZhejiangOnlineNewsAndItsHttpsSubdomains() {
        assertTrue(policy.permits(ZhejiangNewsUrlPolicy.DEFAULT_URL));
        assertTrue(policy.permits("https://zjol.com.cn/"));
        assertTrue(policy.permits("https://news.zjol.com.cn/article/1?from=app"));
    }

    @Test
    public void rejectsNonHttpsAndLookalikeHosts() {
        assertFalse(policy.permits("http://zjnews.zjol.com.cn/"));
        assertFalse(policy.permits("https://zjol.com.cn.attacker.example/"));
        assertFalse(policy.permits("https://attacker.example/?next=zjol.com.cn"));
    }

    @Test
    public void rejectsCredentialsPortsAndNonNetworkSchemes() {
        assertFalse(policy.permits("https://user@zjnews.zjol.com.cn/"));
        assertFalse(policy.permits("https://zjnews.zjol.com.cn:8443/"));
        assertFalse(policy.permits("javascript:alert(1)"));
        assertFalse(policy.permits("file:///sdcard/news.html"));
        assertFalse(policy.permits(null));
        assertFalse(policy.permits(""));
    }
}
