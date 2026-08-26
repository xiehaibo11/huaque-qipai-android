package com.nanbeiyule.game;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** JSON value objects for the authenticated first-party announcement API. */
final class AnnouncementApiProtocol {
    record AnnouncementSummary(
            long announcementId,
            String title,
            String subtitle,
            String bodyText,
            String pageUrl,
            Long lobbyId,
            int sortOrder,
            Instant startsAt,
            Instant endsAt,
            long version,
            boolean read) {
        AnnouncementSummary withRead(boolean value) {
            return new AnnouncementSummary(
                    announcementId,
                    title,
                    subtitle,
                    bodyText,
                    pageUrl,
                    lobbyId,
                    sortOrder,
                    startsAt,
                    endsAt,
                    version,
                    value);
        }
    }

    record AnnouncementPage(long lobbyId, List<AnnouncementSummary> announcements) {
        AnnouncementPage {
            announcements = List.copyOf(announcements);
        }
    }

    record AnnouncementDetail(
            long announcementId,
            String title,
            String subtitle,
            String bodyText,
            String pageUrl,
            Long lobbyId,
            int sortOrder,
            Instant startsAt,
            Instant endsAt,
            long version,
            boolean read,
            Instant readAt) {}

    record ReadReceipt(long announcementId, long version, boolean read, Instant readAt) {}

    private AnnouncementApiProtocol() {}

    static AnnouncementPage pageFromJson(String responseText) throws JSONException {
        JSONObject body = new JSONObject(responseText);
        JSONArray array = body.getJSONArray("announcements");
        List<AnnouncementSummary> announcements = new ArrayList<>(array.length());
        for (int index = 0; index < array.length(); index++) {
            announcements.add(summaryFromJson(array.getJSONObject(index)));
        }
        return new AnnouncementPage(body.getLong("lobbyId"), announcements);
    }

    static AnnouncementDetail detailFromJson(String responseText) throws JSONException {
        JSONObject body = new JSONObject(responseText);
        return new AnnouncementDetail(
                body.getLong("announcementId"),
                requiredText(body, "title"),
                optionalText(body, "subtitle"),
                optionalText(body, "bodyText"),
                optionalText(body, "pageUrl"),
                optionalLong(body, "lobbyId"),
                body.optInt("sortOrder", 0),
                optionalInstant(body, "startsAt"),
                optionalInstant(body, "endsAt"),
                body.getLong("version"),
                body.optBoolean("read", false),
                optionalInstant(body, "readAt"));
    }

    static ReadReceipt readReceiptFromJson(String responseText) throws JSONException {
        JSONObject body = new JSONObject(responseText);
        return new ReadReceipt(
                body.getLong("announcementId"),
                body.getLong("version"),
                body.optBoolean("read", false),
                optionalInstant(body, "readAt"));
    }

    private static AnnouncementSummary summaryFromJson(JSONObject body) throws JSONException {
        return new AnnouncementSummary(
                body.getLong("announcementId"),
                requiredText(body, "title"),
                optionalText(body, "subtitle"),
                optionalText(body, "bodyText"),
                optionalText(body, "pageUrl"),
                optionalLong(body, "lobbyId"),
                body.optInt("sortOrder", 0),
                optionalInstant(body, "startsAt"),
                optionalInstant(body, "endsAt"),
                body.getLong("version"),
                body.optBoolean("read", false));
    }

    private static String requiredText(JSONObject body, String key) throws JSONException {
        String value = body.getString(key).trim();
        if (value.isEmpty()) {
            throw new JSONException(key + " must not be blank");
        }
        return value;
    }

    private static String optionalText(JSONObject body, String key) {
        if (body.isNull(key)) {
            return "";
        }
        return body.optString(key, "").trim();
    }

    private static Long optionalLong(JSONObject body, String key) throws JSONException {
        return body.isNull(key) || !body.has(key) ? null : body.getLong(key);
    }

    private static Instant optionalInstant(JSONObject body, String key) throws JSONException {
        String value = optionalText(body, key);
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException exception) {
            throw new JSONException(key + " is not an ISO-8601 instant");
        }
    }
}
