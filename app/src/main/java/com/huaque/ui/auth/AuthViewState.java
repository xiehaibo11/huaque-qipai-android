package com.huaque.ui.auth;

import org.luaj.vm2.LuaValue;

public final class AuthViewState {
    private final boolean visible;
    private final String phase;
    private final String phone;
    private final String message;
    private final int remainingSeconds;
    private final boolean sendEnabled;
    private final boolean loginEnabled;
    private final boolean authenticated;

    private AuthViewState(
            boolean visible,
            String phase,
            String phone,
            String message,
            int remainingSeconds,
            boolean sendEnabled,
            boolean loginEnabled,
            boolean authenticated) {
        this.visible = visible;
        this.phase = phase;
        this.phone = phone;
        this.message = message;
        this.remainingSeconds = remainingSeconds;
        this.sendEnabled = sendEnabled;
        this.loginEnabled = loginEnabled;
        this.authenticated = authenticated;
    }

    public static AuthViewState fromLua(LuaValue value) {
        if (value == null || !value.istable()) {
            throw new IllegalArgumentException("Lua auth state must be a table");
        }
        LuaValue phaseValue = value.get("phase");
        if (!phaseValue.isstring() || phaseValue.tojstring().isEmpty()) {
            throw new IllegalArgumentException("Lua auth state requires a phase");
        }
        return new AuthViewState(
                value.get("visible").optboolean(false),
                phaseValue.tojstring(),
                value.get("phone").optjstring(""),
                value.get("message").optjstring(""),
                Math.max(0, value.get("remainingSeconds").optint(0)),
                value.get("sendEnabled").optboolean(false),
                value.get("loginEnabled").optboolean(false),
                value.get("authenticated").optboolean(false));
    }

    public boolean visible() {
        return visible;
    }

    public String phase() {
        return phase;
    }

    public String phone() {
        return phone;
    }

    public String message() {
        return message;
    }

    public int remainingSeconds() {
        return remainingSeconds;
    }

    public boolean sendEnabled() {
        return sendEnabled;
    }

    public boolean loginEnabled() {
        return loginEnabled;
    }

    public boolean authenticated() {
        return authenticated;
    }
}
