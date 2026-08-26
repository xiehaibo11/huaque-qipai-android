package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;

final class CreateRoomRows {
    record Row(CreateRoomRuleConfig.Group group, int lineIndex, List<CreateRoomRuleConfig.Option> options) {}

    private CreateRoomRows() {}

    static List<Row> flatten(CreateRoomState state) {
        if (state == null) {
            return List.of();
        }
        List<Row> rows = new ArrayList<>();
        for (CreateRoomRuleConfig.Group group : state.groups()) {
            for (int index = 0; index < group.rows().size(); index++) {
                rows.add(new Row(group, index, group.rows().get(index)));
            }
        }
        return List.copyOf(rows);
    }

    static float maxScroll(CreateRoomState state) {
        float contentHeight = flatten(state).size() * CreateRoomLayout.RULE_ROW_STRIDE;
        return Math.max(
                0.0f,
                contentHeight
                        - (CreateRoomLayout.RULE_LIST_BOTTOM
                                - CreateRoomLayout.RULE_LIST_TOP));
    }
}
