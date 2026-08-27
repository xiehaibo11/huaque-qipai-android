package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayerLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Test;

public final class TaizhouRoomMessageLayoutTest {
    @Test
    public void textPresentationKeepsFullPhraseForOriginalBubbleWrapping() {
        String text = "和你合作真是太愉快了！不要走决战到天亮！";
        TaizhouRoomToolsState.Message message =
                new TaizhouRoomToolsState.Message(
                        "m1", "QUICK_PHRASE", -1, text, "u1", 1, 0, "");

        TaizhouRoomMessagePresentation presentation =
                TaizhouRoomMessagePresentation.from(message);

        assertEquals(TaizhouRoomMessagePresentation.Kind.TEXT, presentation.kind());
        assertEquals(text, presentation.text());
    }

    @Test
    public void textBubbleLayoutMatchesOriginalPlayerHeadBaseCsb() throws Exception {
        Object bottom = bubbleFor(TaizhouMahjongTableLayout.SEAT_BOTTOM);
        assertBubble(
                R.drawable.taizhou_tool_chat_speak_1,
                110.0f,
                582.0f,
                402.0f,
                710.0f,
                bottom);

        Object right = bubbleFor(TaizhouMahjongTableLayout.SEAT_RIGHT);
        assertBubble(
                R.drawable.taizhou_tool_chat_speak_2,
                1489.0f,
                191.0f,
                1781.0f,
                319.0f,
                right);

        Object top = bubbleFor(TaizhouMahjongTableLayout.SEAT_TOP);
        assertBubble(
                R.drawable.taizhou_tool_chat_speak_3,
                1099.0f,
                80.0f,
                1391.0f,
                208.0f,
                top);
    }

    @Test
    public void textBubbleStyleAndLifetimeMatchOriginalHeadNodeLua() throws Exception {
        Class<?> layout = Class.forName("com.nanbeiyule.game.TaizhouRoomMessageLayout");
        assertEquals(292.0f, floatField(layout, "SPEAK_WIDTH"), 0.001f);
        assertEquals(128.0f, floatField(layout, "SPEAK_HEIGHT"), 0.001f);
        assertEquals(36.0f, floatField(layout, "TEXT_SIZE"), 0.001f);
        assertEquals((int) 0xFF9D613E, intField(layout, "TEXT_COLOR"));
        assertEquals(1_200L, longField(layout, "VISIBLE_MILLIS"));
        assertEquals(43_200L, longMethod(layout, "visibleUntil", 42_000L));
    }

    private static Object bubbleFor(int localSeat) throws Exception {
        Class<?> layout = Class.forName("com.nanbeiyule.game.TaizhouRoomMessageLayout");
        Method method =
                layout.getDeclaredMethod("bubbleFor", int.class, float.class, float.class, float.class);
        method.setAccessible(true);
        TaizhouMahjongPlayerLayout.PlayerSlot slot =
                TaizhouMahjongPlayerLayout.forLocalSeat(localSeat);
        return method.invoke(null, localSeat, slot.centerX(), slot.centerY(), 128.0f);
    }

    private static void assertBubble(
            int backgroundResId, float left, float top, float right, float bottom, Object bubble)
            throws Exception {
        assertEquals(backgroundResId, intMethod(bubble, "backgroundResId"));
        assertEquals(left, floatMethod(bubble, "left"), 0.001f);
        assertEquals(top, floatMethod(bubble, "top"), 0.001f);
        assertEquals(right, floatMethod(bubble, "right"), 0.001f);
        assertEquals(bottom, floatMethod(bubble, "bottom"), 0.001f);
    }

    private static int intField(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(null);
    }

    private static long longField(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.getLong(null);
    }

    private static float floatField(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.getFloat(null);
    }

    private static int intMethod(Object target, String name) throws Exception {
        Method method = target.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        return (Integer) method.invoke(target);
    }

    private static float floatMethod(Object target, String name) throws Exception {
        Method method = target.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        return (Float) method.invoke(target);
    }

    private static long longMethod(Class<?> type, String name, long value) throws Exception {
        Method method = type.getDeclaredMethod(name, long.class);
        method.setAccessible(true);
        return (Long) method.invoke(null, value);
    }
}
