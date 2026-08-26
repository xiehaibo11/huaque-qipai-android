package com.huaque.ui.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class AuthViewStateTest {
    @Test
    public void mapsOnlyTheLuaPresentationState() {
        LuaTable value = new LuaTable();
        value.set("visible", LuaValue.TRUE);
        value.set("phase", "CODE_SENT");
        value.set("phone", "13800138000");
        value.set("message", "验证码已发送");
        value.set("remainingSeconds", 42);
        value.set("sendEnabled", LuaValue.FALSE);
        value.set("loginEnabled", LuaValue.TRUE);
        value.set("authenticated", LuaValue.FALSE);

        AuthViewState state = AuthViewState.fromLua(value);

        assertTrue(state.visible());
        assertEquals("CODE_SENT", state.phase());
        assertEquals("13800138000", state.phone());
        assertEquals("验证码已发送", state.message());
        assertEquals(42, state.remainingSeconds());
        assertFalse(state.sendEnabled());
        assertTrue(state.loginEnabled());
        assertFalse(state.authenticated());
    }

    @Test
    public void rejectsMalformedLuaStateInsteadOfInventingSuccess() {
        LuaTable value = new LuaTable();
        value.set("visible", LuaValue.TRUE);

        assertThrows(IllegalArgumentException.class, () -> AuthViewState.fromLua(value));
    }
}
