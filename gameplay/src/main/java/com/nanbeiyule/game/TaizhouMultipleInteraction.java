package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMultipleLayout;
import com.nanbeiyule.game.mahjong.TaizhouMultipleState;

/**
 * 加倍选择层（{@code Common/CSB/GameBase/AddMultipleLayer.csb}）NONE/ADD/SUPER 按钮的命中。
 *
 * <p>可点门槛与渲染层一致：{@code choiceActive} 且服务端 {@code allowedChoices} 含该项
 * （禁用按钮在渲染层以 alpha 116 置灰）。归档内没有 GameBase 的 AddMultiple Lua 实现
 * （只有 CSB 与一层 9 行壳），禁用态不可点按 CSB Button 的通用行为恢复并在此标注。
 */
final class TaizhouMultipleInteraction {
    private TaizhouMultipleInteraction() {}

    /** 加倍层是否处于可选择显示态（与 TaizhouMultipleRenderer 的画按钮条件一致）。 */
    static boolean choiceVisible(GameplayTableState state) {
        if (state == null || state.multipleChoice().isEmpty()) {
            return false;
        }
        TaizhouMultipleState multiple = state.multipleChoice().get();
        return multiple.goldMode()
                && multiple.choiceActive()
                && multiple.choiceForSeat(state.mySeat()).isEmpty();
    }

    /** 命中且可点时返回对应选项；否则返回 null（不拦截，触摸继续下沉）。 */
    static TaizhouMultipleState.Choice choiceAt(
            GameplayTableState state, float designX, float designY) {
        if (!choiceVisible(state)) {
            return null;
        }
        TaizhouMultipleState multiple = state.multipleChoice().get();
        TaizhouMultipleState.Choice hit = null;
        if (TaizhouMultipleLayout.BUTTON_NONE.contains(designX, designY)) {
            hit = TaizhouMultipleState.Choice.PASS;
        } else if (TaizhouMultipleLayout.BUTTON_ADD.contains(designX, designY)) {
            hit = TaizhouMultipleState.Choice.DEFAULT;
        } else if (TaizhouMultipleLayout.BUTTON_SUPER.contains(designX, designY)) {
            hit = TaizhouMultipleState.Choice.SUPER;
        }
        return hit != null && multiple.canChoose(hit) ? hit : null;
    }
}
