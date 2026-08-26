package com.huaque.ui.auth;

import android.content.Context;
import android.os.Looper;

import com.huaque.ui.BuildConfig;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.CoroutineLib;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JseMathLib;

import java.io.IOException;
import java.io.InputStream;

public final class LuaAuthRuntime {
    public interface Listener {
        void onState(AuthViewState state);

        void onError(String message);
    }

    private final Listener listener;
    private final Globals globals;
    private final LuaPlatformGateway gateway;
    private boolean closed;

    public LuaAuthRuntime(Context context, Listener listener) {
        assertMainThread();
        this.listener = listener;
        this.globals = createGlobals(context);
        this.gateway = new LuaPlatformGateway(
                context.getApplicationContext(),
                BuildConfig.AUTH_BASE_URL,
                this::handleHttpResult);
        installPlatformFunctions();
        try {
            globals.loadfile("src/auth/Bootstrap.lua").call();
        } catch (LuaError error) {
            listener.onError("登录模块初始化失败：" + error.getMessage());
        }
    }

    public void dispatch(String action, String phone, String code, long nowSeconds) {
        assertMainThread();
        if (closed) {
            return;
        }
        try {
            LuaValue function = globals.get("auth_dispatch");
            if (!function.isfunction()) {
                throw new LuaError("auth_dispatch is unavailable");
            }
            function.invoke(LuaValue.varargsOf(new LuaValue[]{
                    LuaValue.valueOf(action),
                    LuaValue.valueOf(phone == null ? "" : phone),
                    LuaValue.valueOf(code == null ? "" : code),
                    LuaValue.valueOf(nowSeconds)
            }));
        } catch (LuaError error) {
            listener.onError("登录操作失败：" + error.getMessage());
        }
    }

    public void close(long nowSeconds) {
        assertMainThread();
        if (closed) {
            return;
        }
        try {
            LuaValue function = globals.get("auth_close");
            if (function.isfunction()) {
                function.call(LuaValue.valueOf(nowSeconds));
            }
        } catch (LuaError ignored) {
            // The Activity is already closing; no UI error can be acted on here.
        }
        closed = true;
        gateway.close();
    }

    public String sessionValue(String key) {
        assertMainThread();
        if (closed || key == null || key.isEmpty()) {
            return "";
        }
        LuaValue function = globals.get("auth_session_value");
        if (!function.isfunction()) {
            return "";
        }
        LuaValue value = function.call(LuaValue.valueOf(key));
        return value.isnil() ? "" : value.tojstring();
    }

    public String accessToken() {
        assertMainThread();
        return closed ? "" : readAccessToken(globals);
    }

    static String readAccessToken(Globals globals) {
        try {
            LuaValue function = globals.get("auth_access_token");
            if (!function.isfunction()) {
                return "";
            }
            LuaValue value = function.call();
            return value.type() == LuaValue.TSTRING ? value.tojstring() : "";
        } catch (LuaError ignored) {
            return "";
        }
    }

    private void installPlatformFunctions() {
        globals.set("platform_emit", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue value) {
                try {
                    listener.onState(AuthViewState.fromLua(value));
                } catch (IllegalArgumentException error) {
                    listener.onError("登录状态格式错误：" + error.getMessage());
                }
                return LuaValue.NIL;
            }
        });
        globals.set("platform_http_post", new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue requestId, LuaValue path, LuaValue body) {
                gateway.post(requestId.checkjstring(), path.checkjstring(), body.checkjstring());
                return LuaValue.NIL;
            }
        });
        globals.set("platform_store_get", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue key) {
                String value = gateway.get(key.checkjstring());
                return value == null ? LuaValue.NIL : LuaValue.valueOf(value);
            }
        });
        globals.set("platform_store_set", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue key, LuaValue value) {
                gateway.set(key.checkjstring(), value.checkjstring());
                return LuaValue.NIL;
            }
        });
        globals.set("platform_now_seconds", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(System.currentTimeMillis() / 1000L);
            }
        });
    }

    private void handleHttpResult(String requestId, int status, String body) {
        assertMainThread();
        if (closed) {
            return;
        }
        try {
            LuaValue function = globals.get("auth_on_http_result");
            if (!function.isfunction()) {
                throw new LuaError("auth_on_http_result is unavailable");
            }
            function.invoke(LuaValue.varargsOf(new LuaValue[]{
                    LuaValue.valueOf(requestId),
                    LuaValue.valueOf(status),
                    LuaValue.valueOf(body == null ? "" : body)
            }));
        } catch (LuaError error) {
            listener.onError("登录响应处理失败：" + error.getMessage());
        }
    }

    private static Globals createGlobals(Context context) {
        Globals globals = new Globals();
        globals.load(new BaseLib());
        globals.load(new PackageLib());
        globals.load(new Bit32Lib());
        globals.load(new TableLib());
        globals.load(new StringLib());
        globals.load(new CoroutineLib());
        globals.load(new JseMathLib());
        LoadState.install(globals);
        LuaC.install(globals);
        globals.finder = filename -> openAsset(context, filename);
        globals.get("package").set("path", "src/?.lua;src/?/init.lua");
        return globals;
    }

    private static InputStream openAsset(Context context, String filename) {
        try {
            return context.getAssets().open(filename);
        } catch (IOException error) {
            return null;
        }
    }

    private static void assertMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("Lua auth runtime must run on the main thread");
        }
    }
}
