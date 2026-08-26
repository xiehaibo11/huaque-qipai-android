package com.nanbeiyule.game;

import android.app.Activity;
import android.content.Intent;

/** Starts the Cocos/Lua table host only for an explicit migration verification run. */
final class CocosRuntimeLauncher {
    static final String EXTRA_ENABLE = "zjyx.enable_cocos_lua";
    static final String EXTRA_ROOM_NUMBER = "zjyx.room_number";

    private CocosRuntimeLauncher() {}

    static boolean launchIfRequested(Activity activity, String roomNumber) {
        if (activity == null) {
            return false;
        }
        boolean explicitlyRequested =
                activity.getIntent().getBooleanExtra(EXTRA_ENABLE, false);
        CocosRuntimeBoundary.Decision decision = CocosRuntimeBoundary.detect(activity);
        if (!CocosRuntimeLaunchPolicy.shouldLaunch(
                decision.mode() == CocosRuntimeBoundary.Mode.COCOS_LUA,
                explicitlyRequested)) {
            return false;
        }
        Intent intent = new Intent();
        intent.setClassName(activity, "org.cocos2dx.lua.AppActivity");
        intent.putExtra(EXTRA_ROOM_NUMBER, roomNumber == null ? "" : roomNumber);
        activity.startActivity(intent);
        return true;
    }
}
