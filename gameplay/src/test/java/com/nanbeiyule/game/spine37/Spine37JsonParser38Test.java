package com.nanbeiyule.game.spine37;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class Spine37JsonParser38Test {
    @Test
    public void parsesVerifiedSpine38DataWithLegacyObjectSkin() {
        String json = """
                {
                  "skeleton": {"spine": "3.8.75"},
                  "bones": [{"name": "root"}],
                  "slots": [],
                  "skins": {"default": {}},
                  "animations": {"animation": {}}
                }
                """;

        Spine37Data data = Spine37JsonParser.parse(json);

        assertEquals("3.8.75", data.spineVersion());
        assertEquals(1, data.bones().size());
        assertTrue(data.animations().containsKey("animation"));
    }
}
