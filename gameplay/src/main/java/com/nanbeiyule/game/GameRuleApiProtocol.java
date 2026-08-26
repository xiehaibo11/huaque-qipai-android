package com.nanbeiyule.game;

import com.nanbeiyule.game.goldroom.GoldHallGameRuleDocument;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Parses the first-party game-rule document responses. */
final class GameRuleApiProtocol {
    private GameRuleApiProtocol() {}

    static GoldHallGameRuleDocument documentFromJson(JSONObject payload) throws JSONException {
        long gameId = payload.getLong("gameId");
        String title = payload.optString("title", "");
        JSONArray blockArray = payload.optJSONArray("blocks");
        List<GoldHallGameRuleDocument.Block> blocks = new ArrayList<>();
        if (blockArray != null) {
            for (int index = 0; index < blockArray.length(); index++) {
                JSONObject block = blockArray.getJSONObject(index);
                blocks.add(
                        new GoldHallGameRuleDocument.Block(
                                blockType(block.optString("type", "BODY")),
                                block.optString("text", "")));
            }
        }
        return new GoldHallGameRuleDocument(gameId, title, blocks);
    }

    /** 未知类型按正文处理，避免服务端新增段落类型时客户端整页失败。 */
    private static GoldHallGameRuleDocument.BlockType blockType(String raw) {
        return "HEADING".equals(raw)
                ? GoldHallGameRuleDocument.BlockType.HEADING
                : GoldHallGameRuleDocument.BlockType.BODY;
    }
}
