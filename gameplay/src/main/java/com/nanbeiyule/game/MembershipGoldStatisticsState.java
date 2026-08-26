package com.nanbeiyule.game;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class MembershipGoldStatisticsState {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final boolean membershipActive;
    private final long selectedGameId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Period today;
    private final Period yesterday;
    private final Period lastThree;
    private final Period lastSeven;

    MembershipGoldStatisticsState(boolean membershipActive, long selectedGameId,
            LocalDate startDate, LocalDate endDate, Period today, Period yesterday,
            Period lastThree, Period lastSeven) {
        this.membershipActive = membershipActive;
        this.selectedGameId = selectedGameId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.today = today == null ? Period.empty("today", "今日") : today;
        this.yesterday = yesterday == null ? Period.empty("yesterday", "昨日") : yesterday;
        this.lastThree = lastThree == null ? Period.empty("lastThree", "最近3日") : lastThree;
        this.lastSeven = lastSeven == null ? Period.empty("lastSeven", "最近7日") : lastSeven;
    }

    static MembershipGoldStatisticsState fromJson(JSONObject body) throws JSONException {
        return new MembershipGoldStatisticsState(
                body.optBoolean("membershipActive", false),
                body.optLong("selectedGameId", 0L),
                parseDate(body.optString("startDate", "")),
                parseDate(body.optString("endDate", "")),
                parsePeriod(body.optJSONObject("today"), "today", "今日"),
                parsePeriod(body.optJSONObject("yesterday"), "yesterday", "昨日"),
                parsePeriod(body.optJSONObject("lastThree"), "lastThree", "最近3日"),
                parsePeriod(body.optJSONObject("lastSeven"), "lastSeven", "最近7日"));
    }

    private static Period parsePeriod(JSONObject value, String code, String label) {
        if (value == null) {
            return Period.empty(code, label);
        }
        return new Period(
                value.optString("code", code),
                value.optString("label", label),
                value.optLong("fightCnt", 0L),
                value.optInt("winRate", 0),
                value.optLong("winScore", 0L));
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    boolean membershipActive() {
        return membershipActive;
    }

    long selectedGameId() {
        return selectedGameId;
    }

    LocalDate startDate() {
        return startDate;
    }

    LocalDate endDate() {
        return endDate;
    }

    Period today() {
        return today;
    }

    Period yesterday() {
        return yesterday;
    }

    Period lastThree() {
        return lastThree;
    }

    Period lastSeven() {
        return lastSeven;
    }

    static final class Period {
        private final String code;
        private final String label;
        private final long fightCnt;
        private final int winRate;
        private final long winScore;

        Period(String code, String label, long fightCnt, int winRate, long winScore) {
            this.code = code;
            this.label = label;
            this.fightCnt = Math.max(0L, fightCnt);
            this.winRate = Math.max(0, winRate);
            this.winScore = winScore;
        }

        static Period empty(String code, String label) {
            return new Period(code, label, 0L, 0, 0L);
        }

        String code() {
            return code;
        }

        String label() {
            return label;
        }

        long fightCnt() {
            return fightCnt;
        }

        int winRate() {
            return winRate;
        }

        long winScore() {
            return winScore;
        }
    }
}
