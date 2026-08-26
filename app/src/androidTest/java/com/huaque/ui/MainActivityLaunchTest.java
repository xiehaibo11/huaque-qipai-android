package com.huaque.ui;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicBoolean;
import org.cocos2dx.lua.AppActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MainActivityLaunchTest {
    @Test
    public void launchReachesRunningActivity() {
        AtomicBoolean running = new AtomicBoolean();
        try (ActivityScenario<AppActivity> scenario = ActivityScenario.launch(AppActivity.class)) {
            scenario.onActivity(activity -> running.set(true));
        }
        assertTrue("AppActivity must survive startup", running.get());
    }
}
