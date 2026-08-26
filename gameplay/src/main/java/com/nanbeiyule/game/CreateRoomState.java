package com.nanbeiyule.game;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

/** Mutable selection state with original radio, checkbox, linkage and cost semantics. */
final class CreateRoomState {
    private final long lobbyId;
    private final long gameId;
    private final CreateRoomRuleConfig config;
    private final boolean serverVerified;
    private int categoryIndex;
    private final LinkedHashSet<String> selected = new LinkedHashSet<>();
    private final Map<String, String> dropdownSelections = new LinkedHashMap<>();
    private final Map<String, CreateRoomRuleConfig.Group> groupByNode = new LinkedHashMap<>();
    private final Map<String, CreateRoomRuleConfig.Option> optionByNode = new LinkedHashMap<>();
    private final Map<Integer, List<String>> cachedSelectionsByCategory = new LinkedHashMap<>();
    private String createAttemptKey = UUID.randomUUID().toString();

    private CreateRoomState(
            long lobbyId,
            long gameId,
            CreateRoomRuleConfig config,
            int categoryIndex,
            Map<Integer, List<String>> cachedSelections,
            boolean serverVerified) {
        this.lobbyId = lobbyId;
        this.gameId = gameId;
        this.config = config;
        this.serverVerified = serverVerified;
        this.categoryIndex = categoryIndex;
        if (cachedSelections != null) {
            cachedSelections.forEach(
                    (key, value) ->
                            cachedSelectionsByCategory.put(
                                    key, value == null ? List.of() : List.copyOf(value)));
        }
        loadCategoryDefaults();
        restoreCachedCategory();
        applyLinkage();
    }

    private void loadCategoryDefaults() {
        selected.clear();
        dropdownSelections.clear();
        groupByNode.clear();
        optionByNode.clear();
        for (CreateRoomRuleConfig.Group group : groups()) {
            for (CreateRoomRuleConfig.Option option : group.options()) {
                groupByNode.put(option.nodeName(), group);
                optionByNode.put(option.nodeName(), option);
                if (!option.defaultDropdown().isBlank()) {
                    dropdownSelections.put(option.nodeName(), option.defaultDropdown());
                }
            }
            selected.addAll(group.defaults());
        }
    }

    static CreateRoomState defaults(
            long lobbyId, long gameId, CreateRoomRuleConfig config) {
        return new CreateRoomState(
                lobbyId, gameId, config, config.defaultCategoryIndex(), Map.of(), true);
    }

    static CreateRoomState restore(
            long lobbyId,
            long gameId,
            CreateRoomRuleConfig config,
            int categoryIndex,
            List<String> cached) {
        CreateRoomState state =
                new CreateRoomState(
                        lobbyId,
                        gameId,
                        config,
                        categoryIndex,
                        cached == null ? Map.of() : Map.of(categoryIndex, cached),
                        true);
        return state;
    }

    static CreateRoomState restore(
            long lobbyId,
            long gameId,
            CreateRoomRuleConfig config,
            int categoryIndex,
            Map<Integer, List<String>> cachedSelections) {
        return new CreateRoomState(
                lobbyId, gameId, config, categoryIndex, cachedSelections, true);
    }

    static CreateRoomState restore(
            long lobbyId,
            long gameId,
            CreateRoomRuleConfig config,
            int categoryIndex,
            Map<Integer, List<String>> cachedSelections,
            boolean serverCatalogVerified,
            boolean serverRuleVerified) {
        return new CreateRoomState(
                lobbyId, gameId, config, categoryIndex, cachedSelections,
                serverCatalogVerified && serverRuleVerified);
    }

    void select(String nodeName) {
        String before = logicalSelectionSignature();
        CreateRoomRuleConfig.Option option = optionByNode.get(nodeName);
        if (option == null || !isVisible(nodeName) || !isEnabled(nodeName)) {
            return;
        }
        CreateRoomRuleConfig.Group group = groupByNode.get(nodeName);
        if (group.type() == CreateRoomRuleConfig.Type.RADIO) {
            group.options().forEach(item -> selected.remove(item.nodeName()));
        }
        selected.add(nodeName);
        if (option.categoryIndex() > 0 && option.categoryIndex() != categoryIndex) {
            rememberCurrentCategory();
            categoryIndex = option.categoryIndex();
            loadCategoryDefaults();
            restoreCachedCategory();
            if (optionByNode.containsKey(nodeName)) {
                CreateRoomRuleConfig.Group targetGroup = groupByNode.get(nodeName);
                if (targetGroup.type() == CreateRoomRuleConfig.Type.RADIO) {
                    targetGroup.options().forEach(item -> selected.remove(item.nodeName()));
                }
                selected.add(nodeName);
            }
        }
        preferSelection(optionByNode.get(nodeName));
        applyLinkage();
        rotateCreateAttemptKeyIfChanged(before);
    }

    void toggle(String nodeName) {
        String before = logicalSelectionSignature();
        CreateRoomRuleConfig.Group group = groupByNode.get(nodeName);
        if (group == null || group.type() != CreateRoomRuleConfig.Type.CHECKBOX
                || !isVisible(nodeName) || !isEnabled(nodeName)) {
            return;
        }
        if (!selected.remove(nodeName)) {
            selected.add(nodeName);
            preferSelection(optionByNode.get(nodeName));
        }
        applyLinkage();
        rotateCreateAttemptKeyIfChanged(before);
    }

    void selectDropdown(String ownerNodeName, String dropdownNodeName) {
        String before = logicalSelectionSignature();
        String beforeKey = createAttemptKey;
        CreateRoomRuleConfig.Option owner = optionByNode.get(ownerNodeName);
        if (owner == null
                || owner.dropdown().stream().noneMatch(item -> item.nodeName().equals(dropdownNodeName))) {
            return;
        }
        dropdownSelections.put(ownerNodeName, dropdownNodeName);
        select(ownerNodeName);
        if (beforeKey.equals(createAttemptKey)) {
            rotateCreateAttemptKeyIfChanged(before);
        }
    }

    boolean isSelected(String nodeName) {
        return selected.contains(nodeName);
    }

    boolean isVisible(String nodeName) {
        CreateRoomRuleConfig.Option option = optionByNode.get(nodeName);
        if (option == null) {
            return false;
        }
        Set<Integer> levels = activeLinkageLevels();
        return (option.show().isEmpty() || intersects(option.show(), levels))
                && !intersects(option.hide(), levels);
    }

    boolean isEnabled(String nodeName) {
        CreateRoomRuleConfig.Option option = optionByNode.get(nodeName);
        if (option == null || !isVisible(nodeName)) {
            return false;
        }
        Set<Integer> levels = activeLinkageLevels();
        if (isProhibitMeanwhile(option, levels)
                || intersects(option.linkProhibit(), levels)) {
            return false;
        }
        if (intersects(option.linkSelect(), levels)) {
            return true;
        }
        return !intersects(option.prohibit(), levels)
                && !intersects(option.prohibitAndSelect(), levels);
    }

    String effectiveNodeName(String nodeName) {
        return dropdownSelections.getOrDefault(nodeName, nodeName);
    }

    List<String> selectedNodeNames() {
        List<String> result = new ArrayList<>();
        for (CreateRoomRuleConfig.Group group : groups()) {
            for (CreateRoomRuleConfig.Option option : group.options()) {
                if (selected.contains(option.nodeName()) && isVisible(option.nodeName())) {
                    result.add(effectiveNodeName(option.nodeName()));
                }
            }
        }
        return List.copyOf(result);
    }

    long roomFeeCenti() {
        CreateRoomRuleConfig.Option cost = selectedSemantic("costType");
        if (cost == null
                || !("allCost".equals(cost.costType()) || "aaCost".equals(cost.costType()))) {
            throw new IllegalStateException("payment type is unavailable");
        }
        String key = cost.costType();
        if (config.costRelativePlayers()) {
            String relativeKey = key + playerCount();
            if (selectedCostCarrier(relativeKey) != null) {
                key = relativeKey;
            }
        }
        CreateRoomRuleConfig.Option carrier = selectedCostCarrier(key);
        BigDecimal value = carrier == null ? null : carrier.costs().get(key);
        if (value == null || value.signum() < 0) {
            throw new IllegalStateException("room fee is unavailable");
        }
        return value.multiply(BigDecimal.valueOf(100L))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    boolean isPerPlayerCost() {
        CreateRoomRuleConfig.Option cost = selectedSemantic("costType");
        if (cost == null
                || !("allCost".equals(cost.costType()) || "aaCost".equals(cost.costType()))) {
            throw new IllegalStateException("payment type is unavailable");
        }
        return "aaCost".equals(cost.costType());
    }

    int playerCount() {
        return selectedCounterValue("playerCount", "player count");
    }

    int playCount() {
        return selectedCounterValue("playCount", "play count");
    }

    boolean hasCompleteBusinessSelection() {
        try {
            playerCount();
            playCount();
            roomFeeCenti();
            return true;
        } catch (IllegalStateException | ArithmeticException ignored) {
            return false;
        }
    }

    boolean isCreateReady() {
        return CreateRoomEntryPolicy.supportsRoomCreation(gameId)
                && serverVerified
                && hasCompleteBusinessSelection();
    }

    long lobbyId() { return lobbyId; }
    long gameId() { return gameId; }
    int categoryIndex() { return categoryIndex; }
    int version() { return config.version(); }
    String createAttemptKey() { return createAttemptKey; }
    List<CreateRoomRuleConfig.Group> groups() { return config.groups(categoryIndex); }

    void rotateCreateAttemptKey() {
        createAttemptKey = UUID.randomUUID().toString();
    }

    private CreateRoomRuleConfig.Option selectedSemantic(String semanticName) {
        for (CreateRoomRuleConfig.Group group : groups()) {
            if (semanticName.equals(group.semanticName())) {
                for (CreateRoomRuleConfig.Option option : group.options()) {
                    if (selected.contains(option.nodeName())) {
                        return option;
                    }
                }
            }
        }
        return null;
    }

    private CreateRoomRuleConfig.Option selectedCostCarrier(String key) {
        CreateRoomRuleConfig.Option preferred = selectedSemantic("playCount");
        if (preferred != null && preferred.costs().containsKey(key)) {
            return preferred;
        }
        for (CreateRoomRuleConfig.Group group : groups()) {
            for (CreateRoomRuleConfig.Option option : group.options()) {
                if (selected.contains(option.nodeName())
                        && isVisible(option.nodeName())
                        && option.costs().containsKey(key)) {
                    return option;
                }
            }
        }
        return null;
    }

    private int selectedCounterValue(String semanticName, String errorName) {
        CreateRoomRuleConfig.Option option = selectedSemantic(semanticName);
        if (option == null) {
            throw new IllegalStateException(errorName + " is unavailable");
        }
        String digits = option.text().replaceAll("\\D+", "");
        if (digits.isBlank()) {
            throw new IllegalStateException(errorName + " is unavailable");
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ignored) {
            throw new IllegalStateException(errorName + " is unavailable");
        }
    }

    private void preferSelection(CreateRoomRuleConfig.Option selectedOption) {
        if (selectedOption == null || selectedOption.linkageLevel() <= 0) return;
        int selectedLevel = selectedOption.linkageLevel();
        for (CreateRoomRuleConfig.Option option : optionByNode.values()) {
            if (!option.nodeName().equals(selectedOption.nodeName())
                    && option.linkUnselected().contains(selectedLevel)) {
                selected.remove(option.nodeName());
            }
        }
    }

    private void applyLinkage() {
        int maximumPasses = Math.max(1, optionByNode.size() + 1);
        for (int pass = 0; pass < maximumPasses; pass++) {
            LinkedHashSet<String> before = new LinkedHashSet<>(selected);
            Set<Integer> levels = activeLinkageLevels();
            for (CreateRoomRuleConfig.Option option : optionByNode.values()) {
                if (isForcedSelected(option, levels)) {
                    forceSelect(option.nodeName());
                    continue;
                }
                boolean forceUnselect = intersects(option.linkUnselected(), levels);
                boolean hidden = intersects(option.hide(), levels);
                boolean prohibited = isProhibited(option, levels);
                boolean invisible = !isVisible(option.nodeName());
                boolean wasSelected = selected.contains(option.nodeName());
                if (forceUnselect || invisible || prohibited) {
                    selected.remove(option.nodeName());
                    if (wasSelected && hidden && !option.hideSelect().isBlank()) {
                        forceSelect(option.hideSelect());
                    } else if (wasSelected
                            && prohibited
                            && !option.prohibitSelect().isBlank()) {
                        forceSelect(option.prohibitSelect());
                    }
                }
            }
            ensureRequiredRadios();
            if (before.equals(selected)) {
                break;
            }
        }
    }

    private void forceSelect(String nodeName) {
        CreateRoomRuleConfig.Option option = optionByNode.get(nodeName);
        if (option == null) {
            return;
        }
        CreateRoomRuleConfig.Group group = groupByNode.get(nodeName);
        if (group.type() == CreateRoomRuleConfig.Type.RADIO) {
            group.options().forEach(item -> selected.remove(item.nodeName()));
        }
        selected.add(nodeName);
    }

    private boolean isProhibited(
            CreateRoomRuleConfig.Option option, Set<Integer> levels) {
        if (isProhibitMeanwhile(option, levels)
                || intersects(option.linkProhibit(), levels)) {
            return true;
        }
        if (intersects(option.linkSelect(), levels)) {
            return false;
        }
        return intersects(option.prohibit(), levels);
    }

    private static boolean isProhibitMeanwhile(
            CreateRoomRuleConfig.Option option, Set<Integer> levels) {
        return !option.prohibitMeanwhile().isEmpty()
                && levels.containsAll(option.prohibitMeanwhile());
    }

    private static boolean isForcedSelected(
            CreateRoomRuleConfig.Option option, Set<Integer> levels) {
        return intersects(option.prohibitAndSelect(), levels)
                || intersects(option.linkSelect(), levels);
    }

    private void ensureRequiredRadios() {
        for (CreateRoomRuleConfig.Group group : groups()) {
            if (group.type() != CreateRoomRuleConfig.Type.RADIO) {
                continue;
            }
            boolean any = group.options().stream()
                    .anyMatch(option -> selected.contains(option.nodeName()));
            if (!any) {
                for (String candidate : group.defaults()) {
                    if (optionByNode.containsKey(candidate)
                            && isVisible(candidate) && isEnabled(candidate)) {
                        selected.add(candidate);
                        break;
                    }
                }
            }
        }
    }

    private void rememberCurrentCategory() {
        cachedSelectionsByCategory.put(categoryIndex, selectedNodeNames());
    }

    private String logicalSelectionSignature() {
        return categoryIndex
                + "|"
                + new TreeSet<>(selected)
                + "|"
                + new TreeMap<>(dropdownSelections);
    }

    private void rotateCreateAttemptKeyIfChanged(String before) {
        if (!before.equals(logicalSelectionSignature())) {
            rotateCreateAttemptKey();
        }
    }

    private void restoreCachedCategory() {
        List<String> cached = cachedSelectionsByCategory.get(categoryIndex);
        if (cached == null) {
            return;
        }
        selected.clear();
        for (String node : cached) {
            if (optionByNode.containsKey(node)) {
                selected.add(node);
                continue;
            }
            for (CreateRoomRuleConfig.Option option : optionByNode.values()) {
                if (option.dropdown().stream().anyMatch(item -> item.nodeName().equals(node))) {
                    selected.add(option.nodeName());
                    dropdownSelections.put(option.nodeName(), node);
                    break;
                }
            }
        }
        ensureRequiredRadios();
    }

    private Set<Integer> activeLinkageLevels() {
        Set<Integer> levels = new LinkedHashSet<>();
        for (CreateRoomRuleConfig.Option option : optionByNode.values()) {
            if (selected.contains(option.nodeName())) {
                if (option.linkageLevel() > 0) {
                    levels.add(option.linkageLevel());
                }
            }
        }
        Set<Integer> selectedLevels = Set.copyOf(levels);
        for (CreateRoomRuleConfig.Option option : optionByNode.values()) {
            if (!selected.contains(option.nodeName())
                    && option.unselectedLinkageLevel() > 0
                    && !intersects(option.linkSelect(), selectedLevels)
                    && !intersects(option.prohibitAndSelect(), selectedLevels)) {
                levels.add(option.unselectedLinkageLevel());
            }
        }
        return levels;
    }

    private static boolean intersects(List<Integer> conditions, Set<Integer> levels) {
        return conditions.stream().anyMatch(levels::contains);
    }
}
