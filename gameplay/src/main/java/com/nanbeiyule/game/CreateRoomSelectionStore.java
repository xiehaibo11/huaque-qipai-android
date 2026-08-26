package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Versioned per-game/category selection persistence matching the Lua UserDefault scope. */
final class CreateRoomSelectionStore {
    private static final String PREFERENCES = "nanbei_create_room_rules";
    private final SharedPreferences preferences;

    CreateRoomSelectionStore(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    Optional<List<String>> load(long gameId, int categoryIndex, int version) {
        return decode(preferences.getString(cacheKey(gameId, categoryIndex), ""), version);
    }

    void save(CreateRoomState state) {
        preferences.edit()
                .putString(
                        cacheKey(state.gameId(), state.categoryIndex()),
                        encode(state.version(), state.selectedNodeNames()))
                .apply();
    }

    static String cacheKey(long gameId, int categoryIndex) {
        return "create_room_" + gameId + "_" + categoryIndex;
    }

    static String encode(int version, List<String> selected) {
        JSONObject body = new JSONObject();
        try {
            body.put("version", version);
            body.put("selectedNodeNames", new JSONArray(selected));
            return body.toString();
        } catch (JSONException exception) {
            return "";
        }
    }

    static Optional<List<String>> decode(String encoded, int expectedVersion) {
        if (encoded == null || encoded.isBlank()) {
            return Optional.empty();
        }
        try {
            JSONObject body = new JSONObject(encoded);
            if (body.getInt("version") != expectedVersion) {
                return Optional.empty();
            }
            JSONArray values = body.getJSONArray("selectedNodeNames");
            List<String> selected = new ArrayList<>();
            for (int index = 0; index < values.length(); index++) {
                selected.add(values.getString(index));
            }
            return Optional.of(List.copyOf(selected));
        } catch (JSONException exception) {
            return Optional.empty();
        }
    }
}
