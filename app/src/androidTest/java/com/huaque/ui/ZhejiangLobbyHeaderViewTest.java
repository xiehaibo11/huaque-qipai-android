package com.huaque.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.nanbeiyule.game.GameHomeState;
import com.nanbeiyule.game.ZhejiangLobbyHeaderView;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class ZhejiangLobbyHeaderViewTest {
    @Test
    public void keepsLobbyBackgroundTransparentWhileDrawingEveryCurrencyIcon() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        int backgroundColor = Color.rgb(17, 43, 91);
        int[] renderedPixels = new int[4];

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            GameHomeState state = new GameHomeState(
                    new GameHomeState.Player(
                            "user-1", 1_000_000_002L, "WhimSeeker", "avatar_default", 0),
                    new GameHomeState.Wallet(100_030L, 0L, 20_000L),
                    new GameHomeState.Region(90_0021L, "台州"),
                    List.of());
            ZhejiangLobbyHeaderView view = new ZhejiangLobbyHeaderView(
                    context,
                    state,
                    R.drawable.lobby_top_controls,
                    "https://api.nanbeiyule.com",
                    "unused-for-default-avatar");
            view.layout(0, 0, 2448, 130);
            Bitmap rendered = Bitmap.createBitmap(2448, 130, Bitmap.Config.ARGB_8888);
            rendered.eraseColor(backgroundColor);
            view.draw(new Canvas(rendered));
            renderedPixels[0] = rendered.getPixel(800, 65);
            renderedPixels[1] = rendered.getPixel(1317, 69);
            renderedPixels[2] = rendered.getPixel(1691, 66);
            renderedPixels[3] = rendered.getPixel(2091, 64);
        });

        assertEquals(backgroundColor, renderedPixels[0]);
        assertNotEquals(backgroundColor, renderedPixels[1]);
        assertNotEquals(backgroundColor, renderedPixels[2]);
        assertNotEquals(backgroundColor, renderedPixels[3]);
    }

    @Test
    public void drawsAvatarOverTheOriginalWhitePlaceholder() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Bitmap original = BitmapFactory.decodeResource(
                context.getResources(), R.drawable.lobby_top_controls);
        AtomicInteger renderedPixel = new AtomicInteger();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            GameHomeState state = new GameHomeState(
                    new GameHomeState.Player(
                            "user-1", 1_000_000_002L, "WhimSeeker", "avatar_default", 0),
                    new GameHomeState.Wallet(0L, 0L, 0L),
                    new GameHomeState.Region(90_0021L, "台州"),
                    List.of());
            ZhejiangLobbyHeaderView view = new ZhejiangLobbyHeaderView(
                    context,
                    state,
                    R.drawable.lobby_top_controls,
                    "https://api.nanbeiyule.com",
                    "unused-for-default-avatar");
            view.layout(0, 0, 2448, 130);
            Bitmap rendered = Bitmap.createBitmap(2448, 130, Bitmap.Config.ARGB_8888);
            view.draw(new Canvas(rendered));
            renderedPixel.set(rendered.getPixel(75, 50));
        });

        assertNotEquals(original.getPixel(75, 50), renderedPixel.get());
    }

    @Test
    public void preservesTheOriginalGoldAvatarFrame() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Bitmap original = BitmapFactory.decodeResource(
                context.getResources(), R.drawable.lobby_top_controls);
        int[] renderedPixels = new int[6];

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            GameHomeState state = new GameHomeState(
                    new GameHomeState.Player(
                            "user-1", 1_000_000_002L, "WhimSeeker", "avatar_default", 0),
                    new GameHomeState.Wallet(0L, 0L, 0L),
                    new GameHomeState.Region(90_0021L, "台州"),
                    List.of());
            ZhejiangLobbyHeaderView view = new ZhejiangLobbyHeaderView(
                    context,
                    state,
                    R.drawable.lobby_top_controls,
                    "https://api.nanbeiyule.com",
                    "unused-for-default-avatar");
            view.layout(0, 0, 2448, 130);
            Bitmap rendered = Bitmap.createBitmap(2448, 130, Bitmap.Config.ARGB_8888);
            view.draw(new Canvas(rendered));
            renderedPixels[0] = rendered.getPixel(75, 15);
            renderedPixels[1] = rendered.getPixel(42, 50);
            renderedPixels[2] = rendered.getPixel(140, 50);
            renderedPixels[3] = rendered.getPixel(75, 111);
            renderedPixels[4] = rendered.getPixel(130, 50);
            renderedPixels[5] = rendered.getPixel(75, 102);
        });

        assertEquals(original.getPixel(75, 15), renderedPixels[0]);
        assertEquals(original.getPixel(42, 50), renderedPixels[1]);
        assertEquals(original.getPixel(140, 50), renderedPixels[2]);
        assertEquals(original.getPixel(75, 111), renderedPixels[3]);
        assertNotEquals(original.getPixel(130, 50), renderedPixels[4]);
        assertNotEquals(original.getPixel(75, 102), renderedPixels[5]);
    }
}
