package com.huaque.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.cocos2dx.lua.AppActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class LoginOverlayTransparencyTest {
    @Test
    public void phoneAndWechatLoginOverlaysKeepTheLoginPageUndimmed() {
        try (ActivityScenario<AppActivity> scenario = ActivityScenario.launch(AppActivity.class)) {
            scenario.onActivity(activity -> {
                invokePrivate(activity, "showLoginPage");
                setPrivateBoolean(activity, "agreementAccepted", true);

                View phoneLogin = findByContentDescription(
                        activity.getWindow().getDecorView(), "手机登录");
                assertNotNull("登录页必须存在手机登录入口", phoneLogin);
                phoneLogin.performClick();

                TextView title = findTextView(
                        activity.getWindow().getDecorView(), "手机登录");
                assertNotNull("手机登录弹窗必须存在", title);
                ViewGroup phoneOverlay = (ViewGroup) title.getParent();
                View phoneBackdrop = phoneOverlay.getChildAt(0);
                assertTransparent("手机登录背景", phoneBackdrop);
                assertTrue("透明背景仍需拦截弹窗外点击", phoneBackdrop.isClickable());

                invokePrivate(activity, "showLoginLoadingOverlay");
                View wechatBackdrop = findByContentDescription(
                        activity.getWindow().getDecorView(), LoginLoadingModel.LOGIN_LABEL);
                assertNotNull("微信登录等待背景必须存在", wechatBackdrop);
                assertTransparent("微信登录等待背景", wechatBackdrop);
                assertTrue("透明等待背景仍需拦截重复操作", wechatBackdrop.isClickable());
                assertTrue("透明等待背景仍需保持焦点拦截", wechatBackdrop.isFocusable());
            });
        }
    }

    private static void assertTransparent(String label, View view) {
        assertTrue(label + "必须使用纯色背景", view.getBackground() instanceof ColorDrawable);
        assertEquals(
                label + "不得压暗登录页",
                Color.TRANSPARENT,
                ((ColorDrawable) view.getBackground()).getColor());
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

    private static void setPrivateBoolean(
            MainActivity activity,
            String fieldName,
            boolean value
    ) {
        try {
            Field field = MainActivity.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setBoolean(activity, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("无法设置 " + fieldName, exception);
        }
    }

    private static View findByContentDescription(View view, String description) {
        CharSequence contentDescription = view.getContentDescription();
        if (contentDescription != null && description.contentEquals(contentDescription)) {
            return view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View match = findByContentDescription(group.getChildAt(i), description);
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
            for (int i = 0; i < group.getChildCount(); i++) {
                TextView match = findTextView(group.getChildAt(i), text);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }
}
