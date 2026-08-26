package com.nanbeiyule.game;

import android.widget.Toast;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import java.util.function.Consumer;

/** Opens the self-built authenticated game session before handing rendering to Cocos/Lua. */
final class AuthenticatedCocosSessionLauncher implements GameplaySessionCoordinator.Listener {
    private final MainActivityGameHomeDisplayFlow owner;
    private final String roomNumber;
    private GameplaySessionCoordinator coordinator;
    private boolean finished;

    private AuthenticatedCocosSessionLauncher(
            MainActivityGameHomeDisplayFlow owner, String roomNumber) {
        this.owner = owner;
        this.roomNumber = roomNumber;
    }

    static GameplaySessionCoordinator openIfRequested(
            MainActivityGameHomeDisplayFlow owner, String roomNumber) {
        if (owner == null || owner.authSessionCoordinator == null || owner.getIntent() == null) {
            return null;
        }
        CocosRuntimeBoundary.Decision runtime = CocosRuntimeBoundary.detect(owner);
        boolean explicitlyRequested =
                owner.getIntent().getBooleanExtra(CocosRuntimeLauncher.EXTRA_ENABLE, false);
        if (!CocosRuntimeLaunchPolicy.shouldLaunch(
                runtime.mode() == CocosRuntimeBoundary.Mode.COCOS_LUA,
                explicitlyRequested)) {
            return null;
        }
        AuthenticatedCocosSessionLauncher session =
                new AuthenticatedCocosSessionLauncher(owner, roomNumber);
        GameplaySessionCoordinator gate =
                new GameplaySessionCoordinator(owner.authSessionCoordinator, session);
        return publishThenOpen(gate, value -> session.coordinator = value, roomNumber);
    }

    /** Returns a stable attempt gate even when authentication completes synchronously. */
    static GameplaySessionCoordinator publishThenOpen(
            GameplaySessionCoordinator gate,
            Consumer<GameplaySessionCoordinator> publisher,
            String roomNumber) {
        publisher.accept(gate);
        gate.open(roomNumber);
        return gate;
    }

    @Override
    public void onState(GameplayTableState state) {
        if (finished) return;
        finished = true;
        destroyCoordinator();
        if (!CocosRuntimeLauncher.launchIfRequested(owner, roomNumber)) {
            Toast.makeText(owner, "Cocos 运行环境不可用", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoginRequired() {
        if (finished) return;
        finished = true;
        destroyCoordinator();
        owner.showLoginPage();
    }

    @Override
    public void onError(String message) {
        if (finished) return;
        finished = true;
        destroyCoordinator();
        Toast.makeText(owner, message, Toast.LENGTH_SHORT).show();
    }

    private void destroyCoordinator() {
        GameplaySessionCoordinator active = coordinator;
        coordinator = null;
        if (active != null) active.destroy();
    }
}
