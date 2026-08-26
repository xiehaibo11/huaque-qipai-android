package com.nanbeiyule.game;

import android.content.Context;
import com.nanbeiyule.game.mahjong.round.TaizhouDismissState;
import java.util.List;

/** {@code Common/CSB/GameBase/DismissLayer.csb} 解散投票层的全屏宿主。 */
final class TaizhouDismissDialog extends TaizhouFullscreenDialog {
    private final TaizhouDismissView view;

    TaizhouDismissDialog(
            Context context,
            TaizhouDismissState state,
            List<TaizhouDismissView.Seat> seats,
            boolean playback,
            boolean alreadyResponded,
            TaizhouDismissView.Actions actions) {
        this(context,
                new TaizhouDismissView(context, state, seats, playback, alreadyResponded, actions));
    }

    private TaizhouDismissDialog(Context context, TaizhouDismissView view) {
        super(context, view, true);
        this.view = view;
        view.setDismissAction(this::dismiss);
        // 原版 Dismiss/View.luac 不给背景加关闭手势，投票必须走按钮或服务端结果。
        setCancelable(false);
    }

    void refresh() {
        view.refresh();
    }
}
