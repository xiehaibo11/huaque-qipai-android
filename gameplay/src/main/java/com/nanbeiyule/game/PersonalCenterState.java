package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Immutable response model for the authenticated personal-center API. */
record PersonalCenterState(
        Player player,
        Wallet wallet,
        Account account,
        Region region,
        HealthCertification healthCertification,
        Membership membership,
        Capabilities capabilities,
        PersonalCenterPrivacySettings privacy) {

    record Player(
            String userId,
            long publicPlayerId,
            String displayName,
            String avatarKey,
            int membershipLevel) {}

    record Wallet(
            long purchasedRoomCards,
            long boundRoomCards,
            long coins,
            long diamonds) {}

    record Account(
            boolean phoneBound,
            String maskedPhone,
            List<String> identityProviders) {
        Account {
            identityProviders = List.copyOf(identityProviders);
        }
    }

    record Region(long lobbyId, String areaName) {}

    record HealthCertification(
            String status,
            String realNameMasked,
            String idCardMasked,
            boolean alipayOneTapEnabled) {}

    record Membership(
            boolean active,
            int level,
            String expiresAt,
            boolean autoRenew,
            long remainingDays) {}

    record Capabilities(
            boolean avatarRefresh,
            boolean regionSwitch,
            boolean accountSwitch,
            boolean accountDeletion,
            boolean phoneRebind,
            boolean healthCertification) {}

    static PersonalCenterState fromJson(JSONObject body)
            throws JSONException {
        JSONObject playerBody = body.getJSONObject("player");
        Player player =
                new Player(
                        requiredString(playerBody, "userId"),
                        positiveLong(playerBody, "publicPlayerId"),
                        requiredString(playerBody, "displayName"),
                        requiredString(playerBody, "avatarKey"),
                        nonNegativeInt(playerBody, "membershipLevel"));

        JSONObject walletBody = body.getJSONObject("wallet");
        Wallet wallet =
                new Wallet(
                        nonNegativeLong(
                                walletBody, "purchasedRoomCards"),
                        nonNegativeLong(walletBody, "boundRoomCards"),
                        nonNegativeLong(walletBody, "coins"),
                        nonNegativeLong(walletBody, "diamonds"));

        JSONObject accountBody = body.getJSONObject("account");
        boolean phoneBound = accountBody.getBoolean("phoneBound");
        String maskedPhone = accountBody.getString("maskedPhone").trim();
        if (phoneBound && maskedPhone.isEmpty()) {
            throw new JSONException(
                    "maskedPhone must not be blank when phone is bound");
        }
        JSONArray providersBody =
                accountBody.getJSONArray("identityProviders");
        List<String> identityProviders =
                new ArrayList<>(providersBody.length());
        for (int index = 0; index < providersBody.length(); index++) {
            String provider = providersBody.getString(index).trim();
            if (provider.isEmpty()) {
                throw new JSONException(
                        "identityProviders must not contain blanks");
            }
            identityProviders.add(provider);
        }
        Account account =
                new Account(
                        phoneBound,
                        maskedPhone,
                        identityProviders);

        JSONObject regionBody = body.getJSONObject("region");
        Region region =
                new Region(
                        positiveLong(regionBody, "lobbyId"),
                        requiredString(regionBody, "areaName"));

        JSONObject healthBody = body.optJSONObject("healthCertification");
        HealthCertification healthCertification =
                healthBody == null
                        ? new HealthCertification(
                                "UNVERIFIED", "", "", false)
                        : new HealthCertification(
                                requiredString(healthBody, "status"),
                                nullableString(healthBody, "realNameMasked"),
                                nullableString(healthBody, "idCardMasked"),
                                healthBody.getBoolean("alipayOneTapEnabled"));

        JSONObject membershipBody = body.optJSONObject("membership");
        Membership membership =
                membershipBody == null
                        ? new Membership(
                                player.membershipLevel() > 0,
                                player.membershipLevel(),
                                "",
                                false,
                                0L)
                        : new Membership(
                                membershipBody.getBoolean("active"),
                                nonNegativeInt(membershipBody, "level"),
                                nullableString(membershipBody, "expiresAt"),
                                membershipBody.getBoolean("autoRenew"),
                                nonNegativeLong(membershipBody, "remainingDays"));

        JSONObject capabilitiesBody =
                body.getJSONObject("capabilities");
        Capabilities capabilities =
                new Capabilities(
                        capabilitiesBody.getBoolean("avatarRefresh"),
                        capabilitiesBody.getBoolean("regionSwitch"),
                        capabilitiesBody.getBoolean("accountSwitch"),
                        capabilitiesBody.getBoolean("accountDeletion"),
                        capabilitiesBody.getBoolean("phoneRebind"),
                        capabilitiesBody.getBoolean(
                                "healthCertification"));
        PersonalCenterPrivacySettings privacy =
                PersonalCenterPrivacySettings.fromJson(
                        body.getJSONObject("privacy"));

        return new PersonalCenterState(
                player,
                wallet,
                account,
                region,
                healthCertification,
                membership,
                capabilities,
                privacy);
    }

    private static String nullableString(
            JSONObject body, String field) throws JSONException {
        if (body.isNull(field)) {
            return "";
        }
        return body.getString(field).trim();
    }

    private static String requiredString(
            JSONObject body, String field) throws JSONException {
        String value = body.getString(field).trim();
        if (value.isEmpty()) {
            throw new JSONException(field + " must not be blank");
        }
        return value;
    }

    private static long positiveLong(
            JSONObject body, String field) throws JSONException {
        long value = body.getLong(field);
        if (value <= 0L) {
            throw new JSONException(field + " must be positive");
        }
        return value;
    }

    private static long nonNegativeLong(
            JSONObject body, String field) throws JSONException {
        long value = body.getLong(field);
        if (value < 0L) {
            throw new JSONException(field + " must not be negative");
        }
        return value;
    }

    private static int nonNegativeInt(
            JSONObject body, String field) throws JSONException {
        int value = body.getInt(field);
        if (value < 0) {
            throw new JSONException(field + " must not be negative");
        }
        return value;
    }
}
