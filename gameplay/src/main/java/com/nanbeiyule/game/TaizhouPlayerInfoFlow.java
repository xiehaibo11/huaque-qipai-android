package com.nanbeiyule.game;

import android.widget.Toast;
import com.nanbeiyule.game.gameplay.GameplaySeat;
import com.nanbeiyule.game.gameplay.GameplayTableState;

/**
 * 玩家信息面板的运行期归属，还原 {@code GameBase/Modules/PlayerInfo}。
 *
 * <p>{@code PlayerInfoView.luac:16-18} 自己座位显示钻石/房卡、他人座位显示「屏蔽TA」；
 * {@code :662-676 initKickUser} 的四条隐藏规则由 {@link TaizhouPlayerInfoState#kickVisible}
 * 复刻；会员状态与钱包取自 {@code /api/v1/personal-center}，本会话内缓存一次。
 */
final class TaizhouPlayerInfoFlow {
    /** 台州包厢 GameID，{@code getRoomMode2() == BOX_ROOM} 的现代等价判定。 */
    private static final long TAIZHOU_BOX_GAME_ID = 30109L;

    private final MainActivityGameHomeDisplayFlow owner;
    private final Runnable openMembership;
    private final TaizhouPlayerBlockStore blockStore;
    private KickListener kickListener = seat -> {};
    private TaizhouPlayerInfoDialog dialog;
    private PersonalCenterState account;
    private boolean accountLoading;
    private int pendingSeat = -1;

    interface KickListener {
        void onKick(int seatNumber);
    }

    TaizhouPlayerInfoFlow(MainActivityGameHomeDisplayFlow owner, Runnable openMembership) {
        this.owner = owner;
        this.openMembership = openMembership;
        blockStore = new TaizhouPlayerBlockStore(owner);
    }

    void setKickListener(KickListener listener) {
        kickListener = listener == null ? seat -> {} : listener;
    }

    void show(GameplayTableState tableState, int seatNumber) {
        if (owner.isFinishing() || tableState == null) {
            return;
        }
        if (account == null) {
            pendingSeat = seatNumber;
            loadAccount(tableState);
            return;
        }
        open(tableState, seatNumber);
    }

    private void loadAccount(GameplayTableState tableState) {
        if (accountLoading
                || owner.personalCenterApiClient == null
                || owner.authSessionCoordinator == null) {
            open(tableState, pendingSeat);
            return;
        }
        accountLoading = true;
        owner.authSessionCoordinator.execute(
                (token, callback) ->
                        owner.personalCenterApiClient.load(
                                token,
                                new PersonalCenterApiClient.Callback() {
                                    @Override
                                    public void onSuccess(PersonalCenterState result) {
                                        callback.onSuccess(result);
                                    }

                                    @Override
                                    public void onUnauthorized() {
                                        callback.onUnauthorized();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        callback.onError(message);
                                    }
                                }),
                new AuthSessionCoordinator.Callback<PersonalCenterState>() {
                    @Override
                    public void onSuccess(PersonalCenterState result) {
                        accountLoading = false;
                        account = result;
                        open(tableState, pendingSeat);
                    }

                    @Override
                    public void onLoginRequired() {
                        accountLoading = false;
                        open(tableState, pendingSeat);
                    }

                    @Override
                    public void onError(String message) {
                        accountLoading = false;
                        // 拿不到会员与钱包时仍按原版过期态展示面板，不阻断查看。
                        open(tableState, pendingSeat);
                    }
                });
    }

    private void open(GameplayTableState tableState, int seatNumber) {
        if (owner.isFinishing() || tableState == null || seatNumber <= 0) {
            return;
        }
        TaizhouPlayerInfoState state = buildState(tableState, seatNumber);
        if (state == null) {
            return;
        }
        dismiss();
        TaizhouPlayerInfoDialog next =
                new TaizhouPlayerInfoDialog(
                        owner,
                        state,
                        null,
                        new TaizhouPlayerInfoView.Actions() {
                            @Override
                            public void onKick() {
                                kickListener.onKick(seatNumber);
                                dismiss();
                            }

                            @Override
                            public void onBuyMembership() {
                                dismiss();
                                openMembership.run();
                            }

                            @Override
                            public void onBlockChanged(
                                    TaizhouPlayerBlockStore.Type type, boolean blocked) {
                                long selfId = selfPlayerId(tableState);
                                long targetId = playerIdOf(tableState, seatNumber);
                                blockStore.setBlocked(type, selfId, targetId, blocked);
                                TaizhouPlayerInfoDialog current = dialog;
                                if (current != null) {
                                    current.update(buildState(tableState, seatNumber));
                                }
                            }
                        });
        dialog = next;
        next.setOnDismissListener(
                ignored -> {
                    if (dialog == next) {
                        dialog = null;
                    }
                });
        next.show();
    }

    private TaizhouPlayerInfoState buildState(GameplayTableState tableState, int seatNumber) {
        GameplaySeat target = seatOf(tableState, seatNumber);
        if (target == null) {
            return null;
        }
        boolean self = seatNumber == tableState.mySeat();
        GameplaySeat viewer = seatOf(tableState, tableState.mySeat());
        boolean viewerIsHost = viewer != null && viewer.host();
        boolean membershipActive =
                account != null && account.membership() != null && account.membership().active();
        long selfId = selfPlayerId(tableState);
        long targetId = target.publicPlayerId();
        return new TaizhouPlayerInfoState(
                seatNumber,
                target.displayName(),
                targetId,
                self,
                target.host(),
                TaizhouPlayerInfoState.kickVisible(
                        viewerIsHost,
                        target.host(),
                        self,
                        tableState.roundNumber(),
                        tableState.gameId() == TAIZHOU_BOX_GAME_ID),
                membershipActive,
                blockStore.stored(TaizhouPlayerBlockStore.Type.VOICE, selfId, targetId),
                blockStore.stored(TaizhouPlayerBlockStore.Type.CHAT, selfId, targetId),
                blockStore.stored(TaizhouPlayerBlockStore.Type.EMOJIS, selfId, targetId),
                account == null ? 0L : account.wallet().diamonds(),
                account == null ? 0L : account.wallet().purchasedRoomCards());
    }

    /** {@code getBlockedVoice}(:255-257)：会员过期时屏蔽一律不生效。 */
    boolean isBlocked(
            TaizhouPlayerBlockStore.Type type, GameplayTableState tableState, int seatNumber) {
        boolean membershipActive =
                account != null && account.membership() != null && account.membership().active();
        return blockStore.isBlocked(
                type, selfPlayerId(tableState), playerIdOf(tableState, seatNumber),
                membershipActive);
    }

    private static long selfPlayerId(GameplayTableState tableState) {
        return playerIdOf(tableState, tableState.mySeat());
    }

    private static long playerIdOf(GameplayTableState tableState, int seatNumber) {
        GameplaySeat seat = seatOf(tableState, seatNumber);
        return seat == null ? 0L : seat.publicPlayerId();
    }

    private static GameplaySeat seatOf(GameplayTableState tableState, int seatNumber) {
        for (GameplaySeat seat : tableState.seats()) {
            if (seat.seatNumber() == seatNumber) {
                return seat;
            }
        }
        return null;
    }

    void toast(String message) {
        Toast.makeText(owner, message, Toast.LENGTH_SHORT).show();
    }

    void dismiss() {
        TaizhouPlayerInfoDialog current = dialog;
        dialog = null;
        if (current != null && current.isShowing()) {
            current.dismiss();
        }
    }

    void close() {
        dismiss();
    }
}
