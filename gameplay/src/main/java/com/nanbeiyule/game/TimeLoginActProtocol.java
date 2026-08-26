package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 定时登录 API 的 JSON 解析。字段名沿用原版 {@code Module.lua:42-57} 消费的语义
 * （{@code loginRewards}/{@code goldOver}/{@code supplementCnt}/{@code wheelReward}），
 * 便于逐字段对照原版渲染分支。解析层不做任何业务判定。
 *
 * <p>{@code active} 与 {@code activityCode} 对应原版 {@code Module.lua:47} 落库的活动 ID：
 * 原版活动关闭时服务端照常应答、只把 ID 置 0，客户端再用 {@code isValid()} 判 {@code _aid ~= 0}。
 * 这里按严格字段门控解析——缺字段一律落到无效默认值，绝不因「应答成功」就当活动开启。
 */
final class TimeLoginActProtocol {
    private TimeLoginActProtocol() {}

    static TimeLoginActState parseState(String responseText) throws JSONException {
        JSONObject payload = new JSONObject(responseText);
        List<TimeLoginActState.Slot> slots = new ArrayList<>();
        JSONArray loginRewards = payload.optJSONArray("loginRewards");
        if (loginRewards != null) {
            for (int index = 0; index < loginRewards.length(); index++) {
                slots.add(parseSlot(loginRewards.getJSONObject(index)));
            }
        }
        return new TimeLoginActState(
                payload.optBoolean("active", false),
                payload.optString("activityCode", ""),
                slots,
                parseWheel(payload.optJSONObject("wheelReward")),
                payload.optLong("goldOver", 0L),
                payload.optInt("daySecond", 0),
                payload.optLong("serverTime", 0L),
                walletCoins(payload.optJSONObject("wallet")));
    }

    static TimeLoginClaimResult parseClaim(String responseText) throws JSONException {
        JSONObject payload = new JSONObject(responseText);
        List<TimeLoginActState.Reward> rewards = new ArrayList<>();
        JSONArray props = payload.optJSONArray("props");
        if (props != null) {
            for (int index = 0; index < props.length(); index++) {
                rewards.add(parseReward(props.getJSONObject(index)));
            }
        }
        Integer sliceIndex =
                payload.isNull("wheelSliceIndex") ? null : payload.optInt("wheelSliceIndex");
        return new TimeLoginClaimResult(
                payload.optString("claimFlag", "Failed"),
                rewards,
                sliceIndex,
                walletCoins(payload.optJSONObject("wallet")));
    }

    private static TimeLoginActState.Slot parseSlot(JSONObject slot) throws JSONException {
        JSONArray props = slot.optJSONArray("props");
        long amount = 0L;
        String name = "";
        if (props != null && props.length() > 0) {
            TimeLoginActState.Reward first = parseReward(props.getJSONObject(0));
            amount = first.propCnt();
            name = first.name();
        }
        return new TimeLoginActState.Slot(
                slot.optString("rewardId", ""),
                slot.optInt("startTime", 0),
                slot.optInt("endTime", 0),
                slot.optString("rewardFlag", TimeLoginActState.STATUS_NOT_IN_TIME),
                amount,
                name);
    }

    private static TimeLoginActState.Wheel parseWheel(JSONObject wheel) throws JSONException {
        if (wheel == null) {
            return null;
        }
        List<TimeLoginActState.Reward> props = new ArrayList<>();
        JSONArray items = wheel.optJSONArray("props");
        if (items != null) {
            for (int index = 0; index < items.length(); index++) {
                props.add(parseReward(items.getJSONObject(index)));
            }
        }
        return new TimeLoginActState.Wheel(
                wheel.optString("rewardId", ""),
                wheel.optInt("curCnt", 0),
                wheel.optInt("wheelCnt", 0),
                props);
    }

    private static TimeLoginActState.Reward parseReward(JSONObject reward) {
        return new TimeLoginActState.Reward(
                reward.optString("propId", ""),
                reward.optLong("propCnt", 0L),
                reward.optString("name", ""));
    }

    private static long walletCoins(JSONObject wallet) {
        return wallet == null ? 0L : wallet.optLong("coins", 0L);
    }
}
