package com.nanbeiyule.game;

import org.json.JSONException;
import org.json.JSONObject;

final class AliyunOneTapResult {
    enum Kind {
        TOKEN,
        CANCELLED,
        FAILURE
    }

    private static final String SUCCESS_CODE = "600000";
    private static final String CANCELLED_CODE = "700000";
    private static final String SWITCH_NUMBER_CODE = "700001";
    private static final String CANCELLED_BY_BUTTON_CODE = "700010";
    private static final String CANCELLED_BY_BACK_KEY_CODE = "700011";
    private static final String PARSE_ERROR_CODE = "PARSE_ERROR";

    private final Kind kind;
    private final String code;
    private final String carrier;
    private final String token;

    private AliyunOneTapResult(
            Kind kind,
            String code,
            String carrier,
            String token) {
        this.kind = kind;
        this.code = code;
        this.carrier = carrier;
        this.token = token;
    }

    static AliyunOneTapResult parse(String rawJson) {
        try {
            JSONObject json = new JSONObject(rawJson);
            String code = trimmed(json.optString("code"));
            String carrier = nullableTrimmed(json.optString("carrier"));
            if (SUCCESS_CODE.equals(code)) {
                String token = nullableTrimmed(json.optString("token"));
                if (token != null) {
                    return new AliyunOneTapResult(
                            Kind.TOKEN, code, carrier, token);
                }
                return failure(code, carrier);
            }
            if (isCancellation(code)) {
                return new AliyunOneTapResult(
                        Kind.CANCELLED, code, carrier, null);
            }
            return failure(
                    code.isEmpty() ? PARSE_ERROR_CODE : code,
                    carrier);
        } catch (JSONException | NullPointerException malformed) {
            return failure(PARSE_ERROR_CODE, null);
        }
    }

    Kind kind() {
        return kind;
    }

    String code() {
        return code;
    }

    String carrier() {
        return carrier;
    }

    String token() {
        return token;
    }

    @Override
    public String toString() {
        return "AliyunOneTapResult{"
                + "kind="
                + kind
                + ", code='"
                + code
                + '\''
                + ", carrier='"
                + carrier
                + '\''
                + ", tokenPresent="
                + (token != null)
                + '}';
    }

    private static AliyunOneTapResult failure(
            String code, String carrier) {
        return new AliyunOneTapResult(
                Kind.FAILURE, code, carrier, null);
    }

    private static boolean isCancellation(String code) {
        return CANCELLED_CODE.equals(code)
                || SWITCH_NUMBER_CODE.equals(code)
                || CANCELLED_BY_BUTTON_CODE.equals(code)
                || CANCELLED_BY_BACK_KEY_CODE.equals(code);
    }

    private static String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private static String nullableTrimmed(String value) {
        String trimmed = trimmed(value);
        return trimmed.isEmpty() ? null : trimmed;
    }
}
