package com.nanbeiyule.game;

import org.json.JSONException;
import org.json.JSONObject;

/** Server-backed privacy selection for the authenticated personal-center user. */
record PersonalCenterPrivacySettings(
        boolean allowFriendRequests,
        boolean showGameRecord,
        boolean showOnlineStatus,
        boolean chatNotifications,
        boolean personalizedRecommendations,
        boolean clipboardAccessEnabled) {
    static PersonalCenterPrivacySettings defaults() {
        return new PersonalCenterPrivacySettings(
                true, true, true, true, false, true);
    }

    static PersonalCenterPrivacySettings fromJson(JSONObject body)
            throws JSONException {
        return new PersonalCenterPrivacySettings(
                body.getBoolean("allowFriendRequests"),
                body.getBoolean("showGameRecord"),
                body.getBoolean("showOnlineStatus"),
                body.getBoolean("chatNotifications"),
                body.getBoolean("personalizedRecommendations"),
                body.optBoolean("clipboardAccessEnabled", true));
    }

    JSONObject toJson() throws JSONException {
        return new JSONObject()
                .put("allowFriendRequests", allowFriendRequests)
                .put("showGameRecord", showGameRecord)
                .put("showOnlineStatus", showOnlineStatus)
                .put("chatNotifications", chatNotifications)
                .put(
                        "personalizedRecommendations",
                        personalizedRecommendations)
                .put("clipboardAccessEnabled", clipboardAccessEnabled);
    }

    PersonalCenterPrivacySettings toggled(int row) {
        return switch (row) {
            case 0 ->
                    new PersonalCenterPrivacySettings(
                            !allowFriendRequests,
                            showGameRecord,
                            showOnlineStatus,
                            chatNotifications,
                            personalizedRecommendations,
                            clipboardAccessEnabled);
            case 1 ->
                    new PersonalCenterPrivacySettings(
                            allowFriendRequests,
                            !showGameRecord,
                            showOnlineStatus,
                            chatNotifications,
                            personalizedRecommendations,
                            clipboardAccessEnabled);
            case 2 ->
                    new PersonalCenterPrivacySettings(
                            allowFriendRequests,
                            showGameRecord,
                            !showOnlineStatus,
                            chatNotifications,
                            personalizedRecommendations,
                            clipboardAccessEnabled);
            case 3 ->
                    new PersonalCenterPrivacySettings(
                            allowFriendRequests,
                            showGameRecord,
                            showOnlineStatus,
                            !chatNotifications,
                            personalizedRecommendations,
                            clipboardAccessEnabled);
            case 4 ->
                    new PersonalCenterPrivacySettings(
                            allowFriendRequests,
                            showGameRecord,
                            showOnlineStatus,
                            chatNotifications,
                            !personalizedRecommendations,
                            clipboardAccessEnabled);
            default ->
                    throw new IllegalArgumentException(
                            "Unknown privacy row " + row);
        };
    }

    PersonalCenterPrivacySettings withClipboardAccessEnabled(boolean enabled) {
        return new PersonalCenterPrivacySettings(
                allowFriendRequests,
                showGameRecord,
                showOnlineStatus,
                chatNotifications,
                personalizedRecommendations,
                enabled);
    }
}
