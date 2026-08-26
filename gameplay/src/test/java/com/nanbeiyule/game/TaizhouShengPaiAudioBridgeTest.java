package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import com.nanbeiyule.game.gameplay.GameplayEvent;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.junit.Test;

public final class TaizhouShengPaiAudioBridgeTest {
    @Test
    public void shengPaiSoundPlaysOnlyForAuthoritativeFirstEvent() throws Exception {
        List<String> played = new ArrayList<>();
        TaizhouRoundAudioBridge bridge = new TaizhouRoundAudioBridge(played::addAll);

        bridge.onEvents(
                List.of(
                        shengPaiEvent(1, true),
                        shengPaiEvent(2, false)));

        assertEquals(List.of("taizhou_mahjong_sound_shengpai"), played);
    }

    private static GameplayEvent shengPaiEvent(int order, boolean first) throws Exception {
        return new GameplayEvent(
                "session",
                1,
                order,
                "SHENG_PAI_COUNT",
                new JSONObject().put("shengPaiCount", 23 - order).put("bFirst", first));
    }
}
