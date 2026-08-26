package com.nanbeiyule.game;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Server-delivered rule model mirroring the recovered dynamic Lua config. */
record CreateRoomRuleConfig(
        int version,
        int defaultCategoryIndex,
        boolean costRelativePlayers,
        List<List<Group>> categories) {
    enum Type { RADIO, CHECKBOX }

    record Group(
            String key,
            String title,
            String semanticName,
            Type type,
            List<String> defaults,
            List<List<Option>> rows) {
        List<Option> options() {
            List<Option> flattened = new ArrayList<>();
            rows.forEach(flattened::addAll);
            return List.copyOf(flattened);
        }
    }

    record Dropdown(String nodeName, String text, String tableText) {}

    record Option(
            String nodeName,
            String text,
            String selectRule,
            String unselectRule,
            String defaultDropdown,
            List<Dropdown> dropdown,
            String tip,
            float diffNodeX,
            int linkageLevel,
            int unselectedLinkageLevel,
            List<Integer> show,
            List<Integer> hide,
            List<Integer> prohibit,
            List<Integer> prohibitMeanwhile,
            List<Integer> linkProhibit,
            List<Integer> prohibitAndSelect,
            List<Integer> linkSelect,
            List<Integer> linkUnselected,
            String hideSelect,
            String prohibitSelect,
            String costType,
            int categoryIndex,
            Map<String, BigDecimal> costs) {}

    CreateRoomRuleConfig {
        categories = categories.stream().map(List::copyOf).toList();
    }

    List<Group> groups() {
        return groups(defaultCategoryIndex);
    }

    List<Group> groups(int categoryIndex) {
        if (categories.isEmpty()) {
            return List.of();
        }
        int safe = Math.max(0, Math.min(categoryIndex - 1, categories.size() - 1));
        return categories.get(safe);
    }

    int categoryCount() {
        return categories.size();
    }

    static CreateRoomRuleConfig fromJson(JSONObject body) throws JSONException {
        JSONObject config = body.optJSONObject("config");
        if (config == null) {
            config = body;
        }
        int version = config.optInt("gameRuleVersion", config.optInt("version", 0));
        int defaultCategory = Math.max(
                1,
                config.optInt(
                        "defaultCategoryIndex",
                        config.optInt("defaultchoose", 1)));
        boolean relative = config.optBoolean(
                "costRelativeToPlayers",
                config.optBoolean("isCostRelativePlayers", false));
        List<List<Group>> categories = new ArrayList<>();
        JSONArray directGroups = config.optJSONArray("groups");
        if (directGroups != null) {
            categories.add(parseNormalizedGroups(directGroups));
            defaultCategory = 1;
        } else {
            JSONArray categoryBodies = config.getJSONArray("categories");
            for (int categoryOffset = 0;
                    categoryOffset < categoryBodies.length();
                    categoryOffset++) {
                JSONObject category = categoryBodies.getJSONObject(categoryOffset);
                JSONArray normalizedGroups = category.optJSONArray("groups");
                if (normalizedGroups != null) {
                    categories.add(parseNormalizedGroups(normalizedGroups));
                    continue;
                }
                List<String> keys = keys(category);
                keys.sort(Comparator.comparingInt(CreateRoomRuleConfig::leadingNumber));
                List<Group> groups = new ArrayList<>();
                for (String key : keys) {
                    groups.add(parseGroup(key, category.getJSONObject(key)));
                }
                categories.add(List.copyOf(groups));
            }
        }
        return new CreateRoomRuleConfig(version, defaultCategory, relative, categories);
    }

    private static Group parseGroup(String key, JSONObject body) throws JSONException {
        Type type =
                "checkbox".equalsIgnoreCase(body.optString("type"))
                        ? Type.CHECKBOX
                        : Type.RADIO;
        List<List<Option>> rows = new ArrayList<>();
        JSONArray normalizedLines = body.optJSONArray("lines");
        if (normalizedLines != null) {
            for (int lineIndex = 0; lineIndex < normalizedLines.length(); lineIndex++) {
                JSONArray row = normalizedLines.getJSONObject(lineIndex).getJSONArray("options");
                rows.add(parseOptions(row));
            }
        } else {
            JSONObject controls = body.getJSONObject("ctrls");
            List<String> rowKeys = keys(controls);
            rowKeys.sort(Comparator.comparingInt(CreateRoomRuleConfig::leadingNumber));
            for (String rowKey : rowKeys) {
                rows.add(parseOptions(controls.getJSONArray(rowKey)));
            }
        }
        return new Group(
                body.optString("key", key),
                body.optString("title", body.optString("text")),
                normalizeCounter(body.optString("counter", body.optString("nodeName"))),
                type,
                strings(
                        body.optJSONArray("defaults") != null
                                ? body.optJSONArray("defaults")
                                : body.optJSONArray("defaultchoose"),
                        body.optString("defaultchoose")),
                List.copyOf(rows));
    }

    private static List<Option> parseOptions(JSONArray row) throws JSONException {
            List<Option> options = new ArrayList<>();
            for (int index = 0; index < row.length(); index++) {
                options.add(parseOption(row.getJSONObject(index)));
            }
        return List.copyOf(options);
    }

    private static List<Group> parseNormalizedGroups(JSONArray bodies) throws JSONException {
        List<Group> groups = new ArrayList<>();
        for (int index = 0; index < bodies.length(); index++) {
            JSONObject body = bodies.getJSONObject(index);
            groups.add(parseGroup(body.getString("key"), body));
        }
        return List.copyOf(groups);
    }

    private static Option parseOption(JSONObject body) throws JSONException {
        List<Dropdown> dropdown = new ArrayList<>();
        JSONArray dropdownBody = body.optJSONArray("dropdown");
        if (dropdownBody != null) {
            for (int index = 0; index < dropdownBody.length(); index++) {
                JSONObject item = dropdownBody.getJSONObject(index);
                dropdown.add(
                        new Dropdown(
                                item.optString("node", item.optString("nodeName")),
                                item.optString("text"),
                                item.optString("tableText")));
            }
        }
        Map<String, BigDecimal> costs = new LinkedHashMap<>();
        for (String key : keys(body)) {
            if (key.startsWith("allCost") || key.startsWith("aaCost")) {
                String value = body.optString(key);
                if (!value.isBlank()) {
                    costs.put(key, new BigDecimal(value));
                }
            }
        }
        JSONObject normalizedCosts = body.optJSONObject("costs");
        if (normalizedCosts != null) {
            parseNormalizedCosts(normalizedCosts, "ALL", "allCost", costs);
            parseNormalizedCosts(normalizedCosts, "AA", "aaCost", costs);
        }
        String costType = body.optString("costType");
        if ("ALL".equalsIgnoreCase(costType)) {
            costType = "allCost";
        } else if ("AA".equalsIgnoreCase(costType)) {
            costType = "aaCost";
        }
        return new Option(
                body.optString("node", body.optString("nodeName")),
                body.optString("text"),
                body.optString("selectRule", body.optString("select")),
                body.optString("unselectRule", body.optString("unselect")),
                body.optString("dropdownDefault", body.optString("defaultchoose")),
                List.copyOf(dropdown),
                body.optString("tips", body.optString("haveTips")),
                (float) body.optDouble("diffNodeX", 0.0),
                body.optInt("linkageLevel", 0),
                body.optInt("unSelectlinkageLevel", 0),
                ints(body.optJSONArray("show")),
                ints(body.optJSONArray("hide")),
                ints(body.optJSONArray("prohibit")),
                ints(body.optJSONArray("prohibitMeanwhile")),
                ints(body.optJSONArray("linkProhibit")),
                ints(body.optJSONArray("prohibitAndSelect")),
                ints(body.optJSONArray("linkSelect")),
                ints(
                        body.optJSONArray("linkUnSelect") != null
                                ? body.optJSONArray("linkUnSelect")
                                : body.optJSONArray("linkUnSelected")),
                body.optString("hideSelect"),
                body.optString("prohibitSelect"),
                costType,
                body.optInt("categoryIndex", body.optInt("categorieIndex", 0)),
                Map.copyOf(costs));
    }

    private static String normalizeCounter(String counter) {
        return switch (counter) {
            case "PLAYER_COUNT" -> "playerCount";
            case "PLAY_COUNT" -> "playCount";
            case "PAY_TYPE" -> "costType";
            default -> counter;
        };
    }

    private static void parseNormalizedCosts(
            JSONObject costsBody,
            String sourceKey,
            String targetPrefix,
            Map<String, BigDecimal> output) throws JSONException {
        JSONObject values = costsBody.optJSONObject(sourceKey);
        if (values == null) {
            return;
        }
        for (String playerCount : keys(values)) {
            BigDecimal centi = new BigDecimal(values.get(playerCount).toString());
            String suffix = "0".equals(playerCount) ? "" : playerCount;
            output.put(targetPrefix + suffix, centi.movePointLeft(2));
        }
    }

    private static List<String> strings(JSONArray values, String scalar) throws JSONException {
        if (values == null) {
            return scalar == null || scalar.isBlank() ? List.of() : List.of(scalar);
        }
        List<String> result = new ArrayList<>();
        for (int index = 0; index < values.length(); index++) {
            result.add(values.getString(index));
        }
        return List.copyOf(result);
    }

    private static List<Integer> ints(JSONArray values) throws JSONException {
        if (values == null) {
            return List.of();
        }
        List<Integer> result = new ArrayList<>();
        for (int index = 0; index < values.length(); index++) {
            result.add(values.getInt(index));
        }
        return List.copyOf(result);
    }

    private static int leadingNumber(String value) {
        int index = 0;
        while (index < value.length() && Character.isDigit(value.charAt(index))) {
            index++;
        }
        return index == 0 ? Integer.MAX_VALUE : Integer.parseInt(value.substring(0, index));
    }

    private static List<String> keys(JSONObject body) {
        List<String> keys = new ArrayList<>();
        Iterator<String> iterator = body.keys();
        while (iterator.hasNext()) {
            keys.add(iterator.next());
        }
        return keys;
    }
}
