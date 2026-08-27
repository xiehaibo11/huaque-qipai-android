package com.nanbeiyule.game;

import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;
import com.nanbeiyule.game.gameplay.GameplayActionTip;
import com.nanbeiyule.game.mahjong.round.MahjongCombType;

final class TaizhouMahjongActionEffect {
    record Anchor(float designX, float androidY) {}

    private TaizhouMahjongActionEffect() {}

    static String animationName(MahjongCombType combType) {
        if (combType == null) {
            return null;
        }
        return switch (combType) {
            case CHOW -> "chi";
            case PONG -> "peng";
            case EXPOSED_KONG, FILL_KONG -> "gang";
            case CONCEALED_KONG -> "angang";
            default -> null;
        };
    }

    static String animationName(GameplayActionTip.Kind kind) {
        if (kind == null) {
            return null;
        }
        return switch (kind) {
            case CHOW -> "chi";
            case PONG -> "peng";
            case KONG -> "gang";
            case CONCEALED_KONG -> "angang";
            case HU -> "hu";
            case FLOWER -> null;
        };
    }

    static Anchor anchor(int localSeat) {
        return switch (localSeat) {
            case TaizhouMahjongTableLayout.SEAT_LEFT -> new Anchor(550.0f, 480.0f);
            case TaizhouMahjongTableLayout.SEAT_BOTTOM -> new Anchor(960.0f, 680.0f);
            case TaizhouMahjongTableLayout.SEAT_RIGHT -> new Anchor(1370.0f, 480.0f);
            case TaizhouMahjongTableLayout.SEAT_TOP -> new Anchor(960.0f, 300.0f);
            default -> throw new IllegalArgumentException("unknown local seat " + localSeat);
        };
    }
}
