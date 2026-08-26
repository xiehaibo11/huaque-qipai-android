package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

record TaizhouRoomToolsState(
        String roomNumber,
        List<Tool> tools,
        List<Reservation> reservations,
        List<String> quickPhrases,
        int emojiCount,
        List<Message> messages) {
    /**
     * 一个道具入口的当前定价。
     *
     * <p>{@code priceCurrency} 是服务端按原版 {@code ChangeCardModule:getShowType()} 优先级选出的
     * 支付方式（TICKET/DIAMOND/ROOM_CARD），{@code priceAmount} 是对应单价；房卡以小房卡为单位，
     * 区配置 {@code smallRoomCardRatio = 100}，因此显示成房卡时要除以 100。
     */
    record Tool(
            TaizhouRoomToolType type,
            String displayName,
            String priceCurrency,
            long priceAmount,
            boolean affordable) {
        static Tool fromJson(JSONObject json) throws JSONException {
            return new Tool(
                    TaizhouRoomToolType.valueOf(json.getString("type")),
                    json.optString("displayName", ""),
                    json.optString("priceCurrency", ""),
                    json.optLong("priceAmount", 0L),
                    json.optBoolean("affordable", false));
        }

        /** 原版 900023 区一张房卡等于一百张小房卡。 */
        static final long SMALL_ROOM_CARDS_PER_ROOM_CARD = 100L;

        /** 图标下方价格底条上的数字，与原版一样只画数量。 */
        String priceLabel() {
            if ("ROOM_CARD".equals(priceCurrency)
                    && priceAmount % SMALL_ROOM_CARDS_PER_ROOM_CARD == 0) {
                return String.valueOf(priceAmount / SMALL_ROOM_CARDS_PER_ROOM_CARD);
            }
            return String.valueOf(priceAmount);
        }
    }

    record Reservation(
            TaizhouRoomToolType type,
            int targetRound,
            boolean active,
            String updatedAt) {
        static Reservation fromJson(JSONObject json) throws JSONException {
            return new Reservation(
                    TaizhouRoomToolType.valueOf(json.getString("type")),
                    json.optInt("targetRound", 0),
                    json.optBoolean("active", false),
                    json.optString("updatedAt", ""));
        }
    }

    record Message(
            String messageId,
            String type,
            int contentIndex,
            String text,
            String senderUserId,
            int senderSeat,
            int durationMillis,
            String createdAt) {
        static Message fromJson(JSONObject json) {
            return new Message(
                    json.optString("messageId", ""),
                    json.optString("type", ""),
                    json.optInt("contentIndex", -1),
                    json.optString("text", ""),
                    json.optString("senderUserId", ""),
                    json.optInt("senderSeat", 0),
                    json.optInt("durationMillis", 0),
                    json.optString("createdAt", ""));
        }
    }

    TaizhouRoomToolsState {
        roomNumber = roomNumber == null ? "" : roomNumber;
        tools = List.copyOf(tools == null ? List.of() : tools);
        reservations = List.copyOf(reservations == null ? List.of() : reservations);
        quickPhrases = List.copyOf(quickPhrases == null ? List.of() : quickPhrases);
        messages = List.copyOf(messages == null ? List.of() : messages);
        emojiCount = Math.max(0, emojiCount);
    }

    static TaizhouRoomToolsState fromJson(JSONObject json) throws JSONException {
        return new TaizhouRoomToolsState(
                json.getString("roomNumber"),
                tools(json.optJSONArray("tools")),
                reservations(json.optJSONArray("reservations")),
                strings(json.optJSONArray("quickPhrases")),
                json.optInt("emojiCount", 0),
                messages(json.optJSONArray("messages")));
    }

    static TaizhouRoomToolsState empty(String roomNumber) {
        return new TaizhouRoomToolsState(
                roomNumber, List.of(), List.of(), List.of(), 0, List.of());
    }

    Tool tool(TaizhouRoomToolType type) {
        for (Tool tool : tools) {
            if (tool.type() == type) {
                return tool;
            }
        }
        return new Tool(
                type, type == TaizhouRoomToolType.CHANGE_CARD ? "换牌" : "洗牌", "", 0L, false);
    }

    boolean isReserved(TaizhouRoomToolType type) {
        for (Reservation reservation : reservations) {
            if (reservation.type() == type && reservation.active()) {
                return true;
            }
        }
        return false;
    }

    private static List<Tool> tools(JSONArray array) throws JSONException {
        List<Tool> values = new ArrayList<>();
        if (array != null) {
            for (int index = 0; index < array.length(); index++) {
                values.add(Tool.fromJson(array.getJSONObject(index)));
            }
        }
        return values;
    }

    private static List<Reservation> reservations(JSONArray array) throws JSONException {
        List<Reservation> values = new ArrayList<>();
        if (array != null) {
            for (int index = 0; index < array.length(); index++) {
                values.add(Reservation.fromJson(array.getJSONObject(index)));
            }
        }
        return values;
    }

    private static List<Message> messages(JSONArray array) throws JSONException {
        List<Message> values = new ArrayList<>();
        if (array != null) {
            for (int index = 0; index < array.length(); index++) {
                values.add(Message.fromJson(array.getJSONObject(index)));
            }
        }
        return values;
    }

    private static List<String> strings(JSONArray array) throws JSONException {
        List<String> values = new ArrayList<>();
        if (array != null) {
            for (int index = 0; index < array.length(); index++) {
                values.add(array.getString(index));
            }
        }
        return values;
    }
}
