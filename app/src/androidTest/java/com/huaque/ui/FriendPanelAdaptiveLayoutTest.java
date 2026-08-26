package com.huaque.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.huaque.ui.friend.FriendPanelView;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class FriendPanelAdaptiveLayoutTest {
    @Test
    public void anchorsCollapsedRailToTheLeftEdgeOnWideScreens() {
        int[] bounds = inspectPanel(panel -> {
            View rail = panel.getChildAt(1);
            return new int[]{
                    rail.getLeft(), rail.getTop(), rail.getWidth(), rail.getHeight()
            };
        });

        assertEquals(0, bounds[0]);
        assertEquals(121, bounds[1]);
        assertEquals(164, bounds[2]);
        assertEquals(340, bounds[3]);
    }

    @Test
    public void anchorsExpandedPanelToTheSameLeftEdge() {
        int[] bounds = inspectPanel(panel -> {
            View expanded = panel.getChildAt(2);
            return new int[]{
                    expanded.getLeft(), expanded.getTop(),
                    expanded.getWidth(), expanded.getHeight()
            };
        });

        assertEquals(0, bounds[0]);
        assertEquals(0, bounds[1]);
        assertEquals(463, bounds[2]);
        assertEquals(582, bounds[3]);
    }

    @Test
    public void keepsTitleAttachedAndEmptyCopyInsideTheFrame() {
        boolean[] results = inspectPanel(panel -> {
            ViewGroup rail = (ViewGroup) panel.getChildAt(1);
            View paper = rail.getChildAt(0);
            View title = rail.getChildAt(1);
            TextView empty = (TextView) rail.getChildAt(2);
            return new boolean[]{
                    paper.getTop() > title.getTop(),
                    title.getBottom() > paper.getTop(),
                    title.getWidth() >= paper.getWidth(),
                    empty.getPaint().measureText(empty.getText().toString()) <= empty.getWidth()
            };
        });

        assertTrue("the title must sit above the panel body", results[0]);
        assertTrue("the title and panel body must overlap", results[1]);
        assertTrue("the title plate must cover the panel width", results[2]);
        assertTrue("the complete empty-state copy must fit inside the panel", results[3]);
    }

    private static <T> T inspectPanel(Inspector<T> inspector) {
        Intent intent = new Intent();
        intent.setClassName(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                "org.cocos2dx.lua.AppActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Activity activity = InstrumentationRegistry.getInstrumentation().startActivitySync(intent);
        AtomicReference<T> result = new AtomicReference<>();

        try {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                FriendPanelView panel = new FriendPanelView(activity);
                activity.setContentView(panel);
                panel.getChildAt(2).setVisibility(View.VISIBLE);
                int width = View.MeasureSpec.makeMeasureSpec(1280, View.MeasureSpec.EXACTLY);
                int height = View.MeasureSpec.makeMeasureSpec(582, View.MeasureSpec.EXACTLY);
                panel.measure(width, height);
                panel.layout(0, 0, 1280, 582);
                result.set(inspector.inspect(panel));
                activity.setContentView(new View(activity));
            });
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(activity::finish);
        }
        return result.get();
    }

    private interface Inspector<T> {
        T inspect(ViewGroup panel);
    }
}
