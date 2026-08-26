package com.nanbeiyule.game.gameplay;

import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public final class GameplayEvent {
    private final String sessionId;
    private final long revision;
    private final int eventOrder;
    private final String type;
    private final String payloadJson;

    public GameplayEvent(
            String sessionId,
            long revision,
            int eventOrder,
            String type,
            JSONObject payload) {
        this.sessionId = requireText(sessionId, "sessionId");
        if (revision <= 0 || eventOrder <= 0) {
            throw new IllegalArgumentException("event cursor must be positive");
        }
        this.revision = revision;
        this.eventOrder = eventOrder;
        this.type = requireText(type, "type");
        this.payloadJson = Objects.requireNonNull(payload, "payload").toString();
    }

    public String sessionId() {
        return sessionId;
    }

    public long revision() {
        return revision;
    }

    public int eventOrder() {
        return eventOrder;
    }

    public String type() {
        return type;
    }

    public JSONObject payload() {
        try {
            return new JSONObject(payloadJson);
        } catch (JSONException exception) {
            throw new IllegalStateException("Stored gameplay event payload is invalid", exception);
        }
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
