package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class ZhejiangLobbyCurrencyGlintRendererTest {
    @Test
    public void rendersTheMoneyStarsInsideTheirMarkedHeaderRegions() {
        Bitmap output = render(1_600L);

        assertTrue(countVisiblePixels(output, 1240, 15, 1380, 125) > 0);
        assertTrue(countVisiblePixels(output, 1610, 10, 1760, 125) > 0);
        assertEquals(0, countVisiblePixels(output, 2000, 0, 2190, 130));
    }

    @Test
    public void rendersTheFirstCardSweepBeforeItsLongRestPhase() {
        Bitmap output = render(17L);

        assertEquals(0, countVisiblePixels(output, 1240, 0, 1380, 130));
        assertEquals(0, countVisiblePixels(output, 1610, 0, 1760, 130));
        assertTrue(countVisiblePixels(output, 2000, 0, 2190, 130) > 0);
    }

    private static Bitmap render(long elapsedMillis) {
        Context context = ApplicationProvider.getApplicationContext();
        Bitmap output = Bitmap.createBitmap(2448, 130, Bitmap.Config.ARGB_8888);
        ZhejiangLobbyCurrencyGlintRenderer renderer =
                new ZhejiangLobbyCurrencyGlintRenderer(context.getResources());
        renderer.draw(new Canvas(output), elapsedMillis);
        return output;
    }

    private static int countVisiblePixels(
            Bitmap bitmap,
            int left,
            int top,
            int right,
            int bottom) {
        int count = 0;
        for (int y = top; y < bottom; y++) {
            for (int x = left; x < right; x++) {
                if ((bitmap.getPixel(x, y) >>> 24) != 0) {
                    count++;
                }
            }
        }
        return count;
    }
}
