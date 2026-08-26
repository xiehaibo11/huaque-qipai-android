package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.SharedPreferences;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public final class TaizhouPlayerBlockStoreTest {
    @Test
    public void constructionDoesNotOpenPreferencesBeforeActivityIsAttached() {
        AtomicBoolean opened = new AtomicBoolean();

        new TaizhouPlayerBlockStore(
                () -> {
                    opened.set(true);
                    return null;
                });

        assertFalse(opened.get());
    }

    @Test
    public void firstReadOpensPreferencesOnce() {
        AtomicInteger opened = new AtomicInteger();
        SharedPreferences preferences =
                (SharedPreferences)
                        Proxy.newProxyInstance(
                                SharedPreferences.class.getClassLoader(),
                                new Class<?>[] {SharedPreferences.class},
                                (proxy, method, arguments) -> {
                                    if (method.getName().equals("getBoolean")) {
                                        return true;
                                    }
                                    throw new AssertionError(method.getName());
                                });
        TaizhouPlayerBlockStore store =
                new TaizhouPlayerBlockStore(
                        () -> {
                            opened.incrementAndGet();
                            return preferences;
                        });

        assertTrue(store.stored(TaizhouPlayerBlockStore.Type.VOICE, 1L, 2L));
        assertTrue(store.stored(TaizhouPlayerBlockStore.Type.VOICE, 1L, 2L));
        assertEquals(1, opened.get());
    }
}
