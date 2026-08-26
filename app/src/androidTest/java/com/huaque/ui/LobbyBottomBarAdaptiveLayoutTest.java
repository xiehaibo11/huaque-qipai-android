package com.huaque.ui;

import static org.junit.Assert.assertArrayEquals;

import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class LobbyBottomBarAdaptiveLayoutTest {
    @Test
    public void mapsBottomBarWithTheSameIndependentScalesAsTheLobbyPage() {
        Intent intent = new Intent();
        intent.setClassName(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                "org.cocos2dx.lua.AppActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Activity activity = InstrumentationRegistry.getInstrumentation().startActivitySync(intent);
        AtomicReference<int[]> actualBounds = new AtomicReference<>();

        try {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                try {
                    Class<?> rootClass = Class.forName(
                            "com.huaque.ui.MainActivity$LobbyRoot");
                    Constructor<?> constructor = rootClass.getDeclaredConstructor(Activity.class);
                    constructor.setAccessible(true);
                    ViewGroup root = (ViewGroup) constructor.newInstance(activity);

                    Method addBottomBar = MainActivity.class.getDeclaredMethod(
                            "addZhejiangLobbyBottomBar", rootClass);
                    addBottomBar.setAccessible(true);
                    addBottomBar.invoke(activity, root);
                    activity.setContentView(root);

                    int width = View.MeasureSpec.makeMeasureSpec(
                            1280, View.MeasureSpec.EXACTLY);
                    int height = View.MeasureSpec.makeMeasureSpec(
                            582, View.MeasureSpec.EXACTLY);
                    root.measure(width, height);
                    root.layout(0, 0, 1280, 582);

                    View background = root.getChildAt(0);
                    actualBounds.set(new int[]{
                            background.getLeft(),
                            background.getTop(),
                            background.getWidth(),
                            background.getHeight()
                    });
                    activity.setContentView(new View(activity));
                } catch (ReflectiveOperationException exception) {
                    throw new AssertionError(exception);
                }
            });
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(activity::finish);
        }

        assertArrayEquals(new int[]{53, 509, 867, 51}, actualBounds.get());
    }

    @Test
    public void mapsMoreMenuOutsideTapRegionWithIndependentScales() {
        Intent intent = new Intent();
        intent.setClassName(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                "org.cocos2dx.lua.AppActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Activity activity = InstrumentationRegistry.getInstrumentation().startActivitySync(intent);
        AtomicInteger outsideTapCount = new AtomicInteger();

        try {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                try {
                    Class<?> rootClass = Class.forName(
                            "com.huaque.ui.MainActivity$LobbyRoot");
                    Constructor<?> constructor = rootClass.getDeclaredConstructor(Activity.class);
                    constructor.setAccessible(true);
                    ViewGroup root = (ViewGroup) constructor.newInstance(activity);

                    Method addBottomBar = MainActivity.class.getDeclaredMethod(
                            "addZhejiangLobbyBottomBar", rootClass);
                    addBottomBar.setAccessible(true);
                    addBottomBar.invoke(activity, root);
                    activity.setContentView(root);

                    int width = View.MeasureSpec.makeMeasureSpec(
                            1280, View.MeasureSpec.EXACTLY);
                    int height = View.MeasureSpec.makeMeasureSpec(
                            582, View.MeasureSpec.EXACTLY);
                    root.measure(width, height);
                    root.layout(0, 0, 1280, 582);

                    Field outsideTapAction = rootClass.getDeclaredField("outsideTapAction");
                    outsideTapAction.setAccessible(true);
                    outsideTapAction.set(root, (Runnable) outsideTapCount::incrementAndGet);

                    MotionEvent event = MotionEvent.obtain(
                            0L, 0L, MotionEvent.ACTION_DOWN, 160.0f, 431.0f, 0);
                    root.dispatchTouchEvent(event);
                    event.recycle();
                    activity.setContentView(new View(activity));
                } catch (ReflectiveOperationException exception) {
                    throw new AssertionError(exception);
                }
            });
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(activity::finish);
        }

        assertArrayEquals(new int[]{0}, new int[]{outsideTapCount.get()});
    }
}
