package com.nanbeiyule.game;

import org.json.JSONException;
import org.json.JSONObject;

final class AuthApiProtocol {
    private AuthApiProtocol() {
    }

    static JSONObject refreshBody(String refreshToken) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("refreshToken", refreshToken);
        return body;
    }

    static boolean isRefreshRejectedStatus(int statusCode) {
        return statusCode == 400 || statusCode == 401 || statusCode == 403;
    }
}
