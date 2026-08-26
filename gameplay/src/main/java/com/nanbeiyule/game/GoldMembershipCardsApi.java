package com.nanbeiyule.game;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/** Gold-card routes layered on the existing authenticated membership transport. */
final class GoldMembershipCardsApi {
    private GoldMembershipCardsApi() {}

    static String cardsPath() {
        return "/api/v1/membership/gold-cards";
    }

    static String claimPath(String productCode) {
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("productCode must not be blank");
        }
        String encoded =
                URLEncoder.encode(productCode.trim(), StandardCharsets.UTF_8)
                        .replace("+", "%20");
        return cardsPath() + "/" + encoded + "/claim";
    }

    static void load(
            MembershipApiClient client,
            String accessToken,
            MembershipApiClient.ResponseCallback<GoldMembershipCardsState> callback) {
        client.request(
                "GET",
                cardsPath(),
                accessToken,
                null,
                null,
                null,
                callback,
                responseText -> GoldMembershipCardsState.fromJson(new JSONObject(responseText)));
    }

    static void claim(
            MembershipApiClient client,
            String accessToken,
            String productCode,
            MembershipApiClient.ResponseCallback<GoldMembershipCardsState.Card> callback) {
        try {
            client.request(
                    "POST",
                    claimPath(productCode),
                    accessToken,
                    new JSONObject(),
                    null,
                    null,
                    callback,
                    responseText ->
                            GoldMembershipCardsState.cardFromJson(
                                    new JSONObject(responseText)));
        } catch (IllegalArgumentException exception) {
            callback.onError("会员卡不存在");
        }
    }
}
