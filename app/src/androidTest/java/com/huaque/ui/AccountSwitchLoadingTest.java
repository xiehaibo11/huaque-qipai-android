package com.huaque.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.cocos2dx.lua.AppActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class AccountSwitchLoadingTest {
    @Test
    public void accountSwitchBrieflyShowsOnlyTheRingBeforeLoginPage() throws Exception {
        try (ActivityScenario<AppActivity> scenario = ActivityScenario.launch(AppActivity.class)) {
            scenario.onActivity(activity -> {
                invokePrivate(activity, "showLoginPage");
                invokePrivate(activity, "ensureAuthRuntime");
                installEmptyLobby(activity);
                invokeActivityResult(activity);

                View decor = activity.getWindow().getDecorView();
                assertNotNull(
                        "切换账号必须短暂显示“南”字圆环",
                        findByContentDescription(decor, "切换账号加载圆环"));
                assertNull("圆环不得显示“正在登录”", findTextView(decor, "正在登录"));
                assertNull("圆环不得显示“加载中”", findTextView(decor, "加载中"));
            });

            Thread.sleep(900L);

            scenario.onActivity(activity -> {
                View decor = activity.getWindow().getDecorView();
                assertNull(
                        "短暂过渡结束后必须移除圆环",
                        findByContentDescription(decor, "切换账号加载圆环"));
                assertNotNull(
                        "短暂过渡结束后必须进入登录页",
                        findByContentDescription(decor, "手机登录"));
            });
        }
    }

    private static void installEmptyLobby(MainActivity activity) {
        try {
            Class<?> rootClass = Class.forName("com.huaque.ui.MainActivity$LobbyRoot");
            Constructor<?> constructor = rootClass.getDeclaredConstructor(Activity.class);
            constructor.setAccessible(true);
            View root = (View) constructor.newInstance(activity);
            activity.setContentView(root);

            Field field = MainActivity.class.getDeclaredField("activeLobbyRoot");
            field.setAccessible(true);
            field.set(activity, root);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("无法创建账号切换测试大厅", exception);
        }
    }

    private static void invokeActivityResult(MainActivity activity) {
        try {
            Method method = MainActivity.class.getDeclaredMethod(
                    "onActivityResult", int.class, int.class, Intent.class);
            method.setAccessible(true);
            method.invoke(
                    activity,
                    4100,
                    com.nanbeiyule.game.MainActivity.RESULT_ACCOUNT_SWITCH_REQUESTED,
                    null);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("无法触发切换账号返回流程", exception);
        }
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

    private static View findByContentDescription(View view, String description) {
        CharSequence contentDescription = view.getContentDescription();
        if (contentDescription != null && description.contentEquals(contentDescription)) {
            return view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                View match = findByContentDescription(group.getChildAt(index), description);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    private static TextView findTextView(View view, String text) {
        if (view instanceof TextView && text.contentEquals(((TextView) view).getText())) {
            return (TextView) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                TextView match = findTextView(group.getChildAt(index), text);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }
}
