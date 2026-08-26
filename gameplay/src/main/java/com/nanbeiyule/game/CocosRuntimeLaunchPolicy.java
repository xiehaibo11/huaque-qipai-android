package com.nanbeiyule.game;

/** Keeps the original-engine path opt-in until the full table projection is migrated. */
final class CocosRuntimeLaunchPolicy {
    private CocosRuntimeLaunchPolicy() {}

    static boolean shouldLaunch(boolean runtimeAvailable, boolean explicitlyRequested) {
        return runtimeAvailable && explicitlyRequested;
    }
}
