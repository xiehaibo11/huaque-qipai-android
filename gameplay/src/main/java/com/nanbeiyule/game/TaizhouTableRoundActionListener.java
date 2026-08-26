package com.nanbeiyule.game;

import com.nanbeiyule.game.mahjong.TaizhouMultipleState;

/**
 * 牌桌「局级动作」回调：结算页（查看牌桌除外，由视图本地处理）与加倍层点击。
 * 等待桌中央按钮沿用既有 Runnable 模式，不并入此接口。
 */
interface TaizhouTableRoundActionListener {
    /** 结算页「洗牌」或「下一局」被点击。 */
    void onSettleActionRequested(TaizhouSettleInteraction.Action action);

    /** 大结算页的返回大厅/分享动作。 */
    void onTotalResultActionRequested(TaizhouTotalResultInteraction.Action action);

    /** 加倍层 NONE/ADD/SUPER 之一被点击。 */
    void onMultipleChoiceRequested(TaizhouMultipleState.Choice choice);
}
