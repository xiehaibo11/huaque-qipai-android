package com.nanbeiyule.game;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.json.JSONObject;
import org.junit.Test;

public class CreateRoomStateAvailabilityTest {
    @Test
    public void onlyARealServerGameplayEngineCanSubmitRoomCreation() throws Exception {
        CreateRoomRuleConfig config =
                CreateRoomRuleConfig.fromJson(
                        new JSONObject(
                                """
                                {
                                  "version": 1,
                                  "groups": [
                                    {
                                      "key": "players",
                                      "counter": "PLAYER_COUNT",
                                      "type": "radio",
                                      "defaults": ["playerCount_4"],
                                      "lines": [{"options": [
                                        {"node": "playerCount_4", "text": "4人"}
                                      ]}]
                                    },
                                    {
                                      "key": "rounds",
                                      "counter": "PLAY_COUNT",
                                      "type": "radio",
                                      "defaults": ["playCount_4"],
                                      "lines": [{"options": [
                                        {"node": "playCount_4", "text": "4局", "allCost": "2"}
                                      ]}]
                                    },
                                    {
                                      "key": "payment",
                                      "counter": "PAY_TYPE",
                                      "type": "radio",
                                      "defaults": ["ownerPay"],
                                      "lines": [{"options": [
                                        {"node": "ownerPay", "text": "房主消耗", "costType": "ALL"}
                                      ]}]
                                    }
                                  ]
                                }
                                """));

        assertTrue(CreateRoomState.restore(900023L, 30109L, config, 1, Map.of()).isCreateReady());
        assertFalse(CreateRoomState.restore(900023L, 30588L, config, 1, Map.of()).isCreateReady());
        assertFalse(CreateRoomState.restore(900023L, 30577L, config, 1, Map.of()).isCreateReady());
    }
}
