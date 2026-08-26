package com.huaque.ui.auth;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

public final class LuaAccessTokenReaderTest {
    @Test
    public void readsOnlyAStringFromTheAuthenticatedLuaSession() {
        Globals globals = new Globals();
        globals.set("auth_access_token", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf("access-token");
            }
        });

        assertEquals("access-token", LuaAuthRuntime.readAccessToken(globals));
    }

    @Test
    public void returnsEmptyWhenTheLuaSessionCannotProvideAToken() {
        Globals missing = new Globals();
        Globals nil = new Globals();
        nil.set("auth_access_token", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.NIL;
            }
        });
        Globals wrongType = new Globals();
        wrongType.set("auth_access_token", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(7);
            }
        });

        assertEquals("", LuaAuthRuntime.readAccessToken(missing));
        assertEquals("", LuaAuthRuntime.readAccessToken(nil));
        assertEquals("", LuaAuthRuntime.readAccessToken(wrongType));
    }
}
