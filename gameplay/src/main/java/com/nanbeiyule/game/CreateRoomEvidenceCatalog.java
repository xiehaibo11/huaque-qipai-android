package com.nanbeiyule.game;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Repository-evidence fallback for the original box-room create page.
 *
 * <p>The backend remains authoritative for the enabled game list and validated room creation. This
 * class only packages the recovered 900023 / 30109 dynamic rule file so the restored Android create
 * page can still mirror {@code CreateBoxRoomView -> configure/900023/box/30109.json} when the rule
 * endpoint is unavailable.
 *
 * <p>边界：创建房间是亲友圈——单独邀请好友、按六位房号入座、房卡结算。大厅玩法是匹配陌生玩家的
 * 金币场，两条链路不并入。因此本类只认房卡 GameID {@link #TAIZHOU_BOX_GAME_ID}；金币场 30400 属于
 * 大厅匹配链路，常量与目录都归 {@code goldroom.GoldRoomEvidenceCatalog}，不得在此重复声明。
 */
final class CreateRoomEvidenceCatalog {
    static final long TAIZHOU_LOBBY_ID = 900023L;
    static final long TAIZHOU_BOX_GAME_ID = 30109L;
    private static final String TAIZHOU_RULE_ASSET = "configure/900023/box/30109.json";

    private CreateRoomEvidenceCatalog() {}

    static List<CreateRoomGame> gamesOrEmpty(long lobbyId) {
        if (lobbyId != TAIZHOU_LOBBY_ID) {
            return List.of();
        }
        return List.of(new CreateRoomGame(TAIZHOU_BOX_GAME_ID, "台州麻将", "", 1));
    }

    static CreateRoomRuleConfig ruleConfigOrNull(Context context, long lobbyId, long gameId) {
        if (context == null || lobbyId != TAIZHOU_LOBBY_ID || gameId != TAIZHOU_BOX_GAME_ID) {
            return null;
        }
        try {
            return CreateRoomRuleConfig.fromJson(
                    new JSONObject(readAsset(context, TAIZHOU_RULE_ASSET)));
        } catch (IOException | JSONException exception) {
            return null;
        }
    }

    private static String readAsset(Context context, String path) throws IOException {
        try (InputStream input = context.getAssets().open(path)) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toString(StandardCharsets.UTF_8.name());
        }
    }
}
