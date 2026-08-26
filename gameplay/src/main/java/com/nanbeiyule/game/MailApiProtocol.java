package com.nanbeiyule.game;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** JSON protocol records for the first-party mail REST API. */
final class MailApiProtocol {

    record MailSummary(long unreadCount, long awardCount) {
        static MailSummary fromJson(JSONObject body) {
            return new MailSummary(
                    Math.max(0L, body.optLong("unreadCount", 0L)),
                    Math.max(0L, body.optLong("awardCount", 0L)));
        }

        boolean hasAttention() {
            return unreadCount > 0 || awardCount > 0;
        }
    }

    record MailEntry(
            String mailId,
            String title,
            String intro,
            String sender,
            boolean hasAttachment,
            boolean read,
            boolean claimed,
            Instant sendTime,
            Instant expireTime) {
        static MailEntry fromJson(JSONObject body) throws JSONException {
            return new MailEntry(
                    requiredString(body, "mailId"),
                    body.optString("title", ""),
                    body.optString("intro", ""),
                    body.optString("sender", ""),
                    body.optBoolean("hasAttachment", false),
                    body.optBoolean("read", false),
                    body.optBoolean("claimed", false),
                    instant(body.optString("sendTime", "")),
                    instant(body.optString("expireTime", "")));
        }

        MailEntry markRead() {
            return new MailEntry(
                    mailId, title, intro, sender,
                    hasAttachment, true, claimed, sendTime, expireTime);
        }

        MailEntry markClaimed() {
            return new MailEntry(
                    mailId, title, intro, sender,
                    hasAttachment, true, true, sendTime, expireTime);
        }
    }

    record MailPage(List<MailEntry> mails, int page, boolean hasMore) {
        MailPage {
            mails = List.copyOf(mails);
            page = Math.max(1, page);
        }
    }

    record MailAttachment(String icon, String rewardType, long amount, String description) {
        static MailAttachment fromJson(JSONObject body) {
            return new MailAttachment(
                    body.optString("icon", ""),
                    body.optString("rewardType", ""),
                    Math.max(0L, body.optLong("amount", 0L)),
                    body.optString("description", ""));
        }
    }

    record MailDetail(MailEntry entry, String content, List<MailAttachment> attachments) {
        MailDetail {
            attachments = List.copyOf(attachments);
        }

        static MailDetail fromJson(JSONObject body) throws JSONException {
            List<MailAttachment> attachments = new ArrayList<>();
            JSONArray array = body.optJSONArray("attachments");
            if (array != null) {
                for (int index = 0; index < array.length(); index++) {
                    attachments.add(MailAttachment.fromJson(array.getJSONObject(index)));
                }
            }
            return new MailDetail(
                    MailEntry.fromJson(body), body.optString("content", ""), attachments);
        }
    }

    record MailMarkedCount(long markedCount) {
        static MailMarkedCount fromJson(JSONObject body) {
            return new MailMarkedCount(Math.max(0L, body.optLong("markedCount", 0L)));
        }
    }

    record MailDeletedCount(long deletedCount, List<String> deletedMailIds) {
        MailDeletedCount {
            deletedMailIds = List.copyOf(deletedMailIds);
        }

        static MailDeletedCount fromJson(JSONObject body) {
            return new MailDeletedCount(
                    Math.max(0L, body.optLong("deletedCount", 0L)),
                    stringList(body.optJSONArray("deletedMailIds")));
        }
    }

    record MailReward(String rewardType, long amount) {
        static MailReward fromJson(JSONObject body) {
            return new MailReward(
                    body.optString("rewardType", ""),
                    Math.max(0L, body.optLong("amount", 0L)));
        }
    }

    record MailWallet(long roomCards, long coins, long diamonds, long coupons) {
        static MailWallet fromJson(JSONObject body) {
            return new MailWallet(
                    body.optLong("roomCards", 0L),
                    body.optLong("coins", 0L),
                    body.optLong("diamonds", 0L),
                    body.optLong("coupons", 0L));
        }
    }

    record MailClaimResult(
            List<String> claimedMailIds, List<MailReward> rewards, MailWallet wallet) {
        MailClaimResult {
            claimedMailIds = List.copyOf(claimedMailIds);
            rewards = List.copyOf(rewards);
        }

        static MailClaimResult fromJson(JSONObject body) throws JSONException {
            List<String> claimedIds = new ArrayList<>();
            JSONArray idArray = body.optJSONArray("claimedMailIds");
            if (idArray != null) {
                for (int index = 0; index < idArray.length(); index++) {
                    claimedIds.add(String.valueOf(idArray.get(index)));
                }
            }
            List<MailReward> rewards = new ArrayList<>();
            JSONArray rewardArray = body.optJSONArray("rewards");
            if (rewardArray != null) {
                for (int index = 0; index < rewardArray.length(); index++) {
                    rewards.add(MailReward.fromJson(rewardArray.getJSONObject(index)));
                }
            }
            JSONObject walletBody = body.optJSONObject("wallet");
            return new MailClaimResult(
                    claimedIds,
                    rewards,
                    walletBody == null
                            ? new MailWallet(0, 0, 0, 0)
                            : MailWallet.fromJson(walletBody));
        }
    }

    private MailApiProtocol() {}

    static List<MailEntry> mailListFromJson(String responseText) throws JSONException {
        return mailPageFromJson(responseText).mails();
    }

    static MailPage mailPageFromJson(String responseText) throws JSONException {
        JSONObject body = new JSONObject(responseText);
        JSONArray array = body.optJSONArray("mails");
        List<MailEntry> result = new ArrayList<>();
        if (array == null) {
            return new MailPage(result, body.optInt("page", 1), body.optBoolean("hasMore"));
        }
        for (int index = 0; index < array.length(); index++) {
            result.add(MailEntry.fromJson(array.getJSONObject(index)));
        }
        return new MailPage(result, body.optInt("page", 1), body.optBoolean("hasMore"));
    }

    static JSONArray mailIdsBody(java.util.Collection<String> mailIds) {
        JSONArray result = new JSONArray();
        for (String mailId : mailIds) {
            try {
                result.put(Long.parseLong(mailId));
            } catch (NumberFormatException exception) {
                result.put(mailId);
            }
        }
        return result;
    }

    private static String requiredString(JSONObject body, String field) throws JSONException {
        String value = String.valueOf(body.get(field)).trim();
        if (value.isEmpty()) {
            throw new JSONException(field + " must not be blank");
        }
        return value;
    }

    private static List<String> stringList(JSONArray array) {
        List<String> result = new ArrayList<>();
        if (array == null) {
            return result;
        }
        for (int index = 0; index < array.length(); index++) {
            result.add(String.valueOf(array.opt(index)));
        }
        return result;
    }

    private static Instant instant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}
