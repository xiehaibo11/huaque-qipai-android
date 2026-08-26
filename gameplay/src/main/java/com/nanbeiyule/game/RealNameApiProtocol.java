package com.nanbeiyule.game;

import org.json.JSONException;
import org.json.JSONObject;

final class RealNameApiProtocol {
    private RealNameApiProtocol() {
    }

    static JSONObject verifyBody(String realName, String idCardNumber)
            throws JSONException {
        JSONObject body = new JSONObject();
        body.put("realName", realName);
        body.put("idCardNumber", idCardNumber);
        return body;
    }

    static JSONObject alipayVerifyBody(String authCode)
            throws JSONException {
        JSONObject body = new JSONObject();
        body.put("authCode", authCode);
        return body;
    }
}
