package com.huaque.ui;

import static org.junit.Assert.assertFalse;

import android.app.UiAutomation;
import android.view.accessibility.AccessibilityEvent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.huaque.ui.auth.AuthViewState;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.cocos2dx.lua.AppActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.luaj.vm2.LuaValue;

@RunWith(AndroidJUnit4.class)
public final class LoginSuccessFeedbackTest {
    @Test
    public void authenticatedLoginEntersLobbyWithoutSuccessToast() throws Exception {
        CountDownLatch successToast = new CountDownLatch(1);
        UiAutomation automation =
                InstrumentationRegistry.getInstrumentation().getUiAutomation();
        automation.setOnAccessibilityEventListener(event -> {
            if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                    && event.getText().contains("登录成功")) {
                successToast.countDown();
            }
        });
        try (ActivityScenario<AppActivity> scenario = ActivityScenario.launch(AppActivity.class)) {
            scenario.onActivity(activity -> {
                invokePrivate(activity, "showLoginPage");
                invokePrivate(activity, "ensureAuthRuntime");
                invokeRenderAuthState(activity, authenticatedState());
            });
            assertFalse(
                    "认证成功后不得显示“登录成功”Toast",
                    successToast.await(1, TimeUnit.SECONDS));
        } finally {
            automation.setOnAccessibilityEventListener(null);
        }
    }

    private static AuthViewState authenticatedState() {
        LuaValue state = LuaValue.tableOf();
        state.set("phase", "AUTHENTICATED");
        state.set("message", "登录成功");
        state.set("authenticated", LuaValue.TRUE);
        return AuthViewState.fromLua(state);
    }

    private static void invokePrivate(MainActivity activity, String methodName) {
        try {
            Method method = MainActivity.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(activity);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("无法调用 " + methodName, exception);
        }
    }

    private static void invokeRenderAuthState(MainActivity activity, AuthViewState state) {
        try {
            Method method = MainActivity.class.getDeclaredMethod(
                    "renderAuthState", AuthViewState.class);
            method.setAccessible(true);
            method.invoke(activity, state);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("无法触发认证成功状态", exception);
        }
    }
}
