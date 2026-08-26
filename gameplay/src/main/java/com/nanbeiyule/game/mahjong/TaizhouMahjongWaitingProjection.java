package com.nanbeiyule.game.mahjong;

import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplaySeat;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import java.util.ArrayList;
import java.util.List;

/** Converts authoritative waiting state into original GameLayer ready sprites. */
public final class TaizhouMahjongWaitingProjection {
    public enum Action {
        NONE,
        INVITE,
        READY,
        COPY,
        RULE,
        RECORD,
        FRIENDS,
        MENU,
        CHANGE_CARD,
        SHUFFLE,
        FORTUNE,
        TING,
        CHAT,
        VOICE,
        LUCKY_MISSION,
        TREASURE_POT,
        CAISHEN,
        TRUST
    }

    /** {@code GameSub.lua} 的 {@code GoldTaiZhouMahjong}，{@code IsGoldMode="BOTYes"}。 */
    private static final long GOLD_TAIZHOU_MAHJONG_GAME_ID = 30400L;
    private static final int GOLD_ROOM_MODE = 50;

    private TaizhouMahjongWaitingProjection() {}

    public static List<TaizhouMahjongWaitingLayout.ReadyIndicator> readyIndicators(
            GameplayTableState state) {
        if (state.phase() != GameplayPhase.WAITING) {
            return List.of();
        }
        List<TaizhouMahjongWaitingLayout.ReadyIndicator> indicators = new ArrayList<>();
        for (GameplaySeat seat : state.seats()) {
            if (seat.ready()) {
                int localSeat =
                        TaizhouMahjongSeatMapper.toLocalSeat(
                                seat.seatNumber(), state.mySeat(), state.chairCount());
                indicators.add(TaizhouMahjongWaitingLayout.ready(localSeat));
            }
        }
        return List.copyOf(indicators);
    }

    /** Mirrors CenterBtns/View.lua: start is the local player's ready control. */
    public static boolean showStartButton(GameplayTableState state) {
        if (state.phase() != GameplayPhase.WAITING) {
            return false;
        }
        for (GameplaySeat seat : state.seats()) {
            if (seat.seatNumber() == state.mySeat()) {
                return !seat.ready();
            }
        }
        return false;
    }

    /** Mirrors CenterBtns/View.lua#getInviteBtnVisible for a private room player. */
    public static boolean showInviteAndCopy(GameplayTableState state) {
        return state.phase() == GameplayPhase.WAITING
                && state.roundNumber() == 0
                && state.seats().size() < state.chairCount();
    }

    /**
     * {@code GameBase/Modules/RightBtns/View.luac:63-70}：托管按钮只在
     * {@code roomData:isGoldRoom()} 时显示，麻将层没有覆盖该判定。金币入口 30400
     * 实际复用 30109 牌桌，因此运行态必须读房间 {@code venue/mode}，不能只看
     * {@code gameId}。
     */
    public static boolean showTrustButton(GameplayTableState state) {
        return isGoldRoom(state) && state.phase() != GameplayPhase.WAITING;
    }

    public static boolean isGoldRoom(GameplayTableState state) {
        return "GOLD".equals(state.roomVenue())
                || state.roomMode() == GOLD_ROOM_MODE
                || state.gameId() == GOLD_TAIZHOU_MAHJONG_GAME_ID;
    }

    public static boolean showTableActivityIcons(GameplayTableState state) {
        return state.phase() == GameplayPhase.WAITING
                && state.roundNumber() == 0
                && state.visibleRound().isEmpty();
    }

    public static Action actionAt(
            GameplayTableState state, float designX, float designY) {
        return actionAt(state, designX, designY, false);
    }

    public static Action actionAt(
            GameplayTableState state, float designX, float designY, boolean showTingButton) {
        if (showInviteAndCopy(state)
                && TaizhouMahjongWaitingLayout.INVITE_BUTTON.contains(designX, designY)) {
            return Action.INVITE;
        }
        if (showStartButton(state)
                && TaizhouMahjongWaitingLayout.START_BUTTON.contains(designX, designY)) {
            return Action.READY;
        }
        if (showInviteAndCopy(state)
                && TaizhouMahjongWaitingLayout.COPY_BUTTON.contains(designX, designY)) {
            return Action.COPY;
        }
        if (TaizhouMahjongWaitingLayout.RULE_BUTTON.contains(designX, designY)) {
            return Action.RULE;
        }
        if (TaizhouMahjongWaitingLayout.RECORD_BUTTON.contains(designX, designY)) {
            return Action.RECORD;
        }
        if (TaizhouMahjongWaitingLayout.FRIEND_BUTTON.contains(designX, designY)) {
            return Action.FRIENDS;
        }
        if (TaizhouMahjongWaitingLayout.MENU_BUTTON.contains(designX, designY)) {
            return Action.MENU;
        }
        if (showTrustButton(state)
                && TaizhouMahjongWaitingLayout.TRUST_BUTTON.contains(designX, designY)) {
            return Action.TRUST;
        }
        if (TaizhouMahjongWaitingLayout.CHANGE_CARD_BUTTON.contains(designX, designY)) {
            return Action.CHANGE_CARD;
        }
        if (TaizhouMahjongWaitingLayout.SHUFFLE_BUTTON.contains(designX, designY)) {
            return Action.SHUFFLE;
        }
        if (showTingButton && TaizhouMahjongWaitingLayout.TING_BUTTON.contains(designX, designY)) {
            return Action.TING;
        }
        if (!showTingButton
                && TaizhouMahjongWaitingLayout.FORTUNE_BUTTON.contains(designX, designY)) {
            return Action.FORTUNE;
        }
        if (TaizhouMahjongWaitingLayout.CHAT_BUTTON.contains(designX, designY)) {
            return Action.CHAT;
        }
        if (TaizhouMahjongWaitingLayout.VOICE_BUTTON.contains(designX, designY)) {
            return Action.VOICE;
        }
        if (showTableActivityIcons(state)) {
            if (TaizhouMahjongWaitingLayout.LUCKY_MISSION_BUTTON.contains(designX, designY)) {
                return Action.LUCKY_MISSION;
            }
            if (TaizhouMahjongWaitingLayout.TREASURE_POT_BUTTON.contains(designX, designY)) {
                return Action.TREASURE_POT;
            }
            if (TaizhouMahjongWaitingLayout.CAISHEN_BUTTON.contains(designX, designY)) {
                return Action.CAISHEN;
            }
        }
        return Action.NONE;
    }

    public static boolean canRequestReadyAt(
            GameplayTableState state, float designX, float designY) {
        return actionAt(state, designX, designY) == Action.READY;
    }
}
