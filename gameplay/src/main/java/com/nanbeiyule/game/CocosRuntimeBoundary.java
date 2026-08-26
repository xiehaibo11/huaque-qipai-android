package com.nanbeiyule.game;

import android.content.Context;
import java.io.File;

/** Selects the experimental original-engine path without making startup depend on native code. */
final class CocosRuntimeBoundary {
    enum Mode {
        COCOS_LUA,
        NATIVE_CANVAS
    }

    static final class Decision {
        private final Mode mode;
        private final String reason;

        private Decision(Mode mode, String reason) {
            this.mode = mode;
            this.reason = reason;
        }

        Mode mode() {
            return mode;
        }

        String reason() {
            return reason;
        }
    }

    private CocosRuntimeBoundary() {}

    static Decision detect(boolean nativeLibraryAvailable, boolean luaBootstrapAvailable) {
        if (nativeLibraryAvailable && luaBootstrapAvailable) {
            return new Decision(Mode.COCOS_LUA, "Cocos native library and Lua bootstrap are available");
        }
        return new Decision(
                Mode.NATIVE_CANVAS,
                "Cocos native library and Lua bootstrap are unavailable");
    }

    static Decision detect(Context context) {
        if (context == null) {
            return detect(false, false);
        }
        File nativeLibrary =
                new File(context.getApplicationInfo().nativeLibraryDir, "libcocos2dlua.so");
        boolean bootstrapAvailable;
        try {
            context.getAssets().open("cocos-lua/main.lua").close();
            bootstrapAvailable = true;
        } catch (Exception ignored) {
            bootstrapAvailable = false;
        }
        return detect(nativeLibrary.isFile(), bootstrapAvailable);
    }
}
