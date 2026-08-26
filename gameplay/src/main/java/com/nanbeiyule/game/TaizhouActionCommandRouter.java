package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayKongType;
import java.util.function.Supplier;

/**
 * 把动作条的吃 / 碰 / 杠 / 胡 / 过点击转交给当前会话协调器。
 *
 * <p>从 {@link MainActivityTaizhouMahjongFlow} 拆出的纯转发层，语义不变：协调器为空（会话已关闭
 * 或尚未建立）时静默丢弃，不伪造任何本地动作。
 */
final class TaizhouActionCommandRouter implements TaizhouActionBarHost.Listener {
    private final Supplier<GameplaySessionCoordinator> session;

    TaizhouActionCommandRouter(Supplier<GameplaySessionCoordinator> session) {
        this.session = session;
    }

    @Override
    public void onChowRequested(int tileValue, int candidateIndex, String actionToken) {
        GameplaySessionCoordinator active = session.get();
        if (active != null) {
            active.submitChow(tileValue, candidateIndex, actionToken);
        }
    }

    @Override
    public void onPungRequested(int tileValue, String actionToken) {
        GameplaySessionCoordinator active = session.get();
        if (active != null) {
            active.submitPung(tileValue, actionToken);
        }
    }

    @Override
    public void onKongRequested(int tileValue, GameplayKongType kongType, String actionToken) {
        GameplaySessionCoordinator active = session.get();
        if (active != null) {
            active.submitKong(tileValue, kongType, actionToken);
        }
    }

    @Override
    public void onHuRequested(String actionToken) {
        GameplaySessionCoordinator active = session.get();
        if (active != null) {
            active.submitHu(actionToken);
        }
    }

    @Override
    public void onPassRequested(String actionToken) {
        GameplaySessionCoordinator active = session.get();
        if (active != null) {
            active.submitPass(actionToken);
        }
    }
}
