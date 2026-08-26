package com.nanbeiyule.game;

import java.util.Objects;

/**
 * Truthful routing policy for the recovered Zhejiang lobby's bottom controls.
 *
 * <p>This is deliberately separate from the visual layout: a recovered label does not imply that
 * this client has the corresponding Zhejiang server module. Each unsupported action therefore has
 * a precise status instead of being collapsed into one generic placeholder.
 */
final class ZhejiangLobbyAction {
    enum Destination {
        SHOP,
        SHOP_DECORATION,
        GAME_RECORDS,
        ACTIVITY_CENTER,
        SHARE,
        DAILY_MISSION,
        SHOP_INVENTORY,
        MAIL,
        MORE_MENU,
        SETTINGS,
        RULES,
        SCORING_ASSISTANT,
        ANNOUNCEMENTS,
        UNAVAILABLE_HEALTH_NOTICE,
        UNAVAILABLE_ZHEJIANG_NEWS,
        UNAVAILABLE_WECHAT_PUBLIC
    }

    record Route(Destination destination, String unavailableMessage) {
        Route {
            Objects.requireNonNull(destination, "destination");
            boolean unavailable = destination.name().startsWith("UNAVAILABLE_");
            if (unavailable != (unavailableMessage != null && !unavailableMessage.isBlank())) {
                throw new IllegalArgumentException(
                        "Unavailable destinations must carry one truthful status message");
            }
        }
    }

    private ZhejiangLobbyAction() {}

    static Route bottom(String key) {
        return switch (key) {
            case "STORE" -> available(Destination.SHOP);
            case "DRESS_UP" -> available(Destination.SHOP_DECORATION);
            case "RECORDS" -> available(Destination.GAME_RECORDS);
            case "ACTIVITIES" -> available(Destination.ACTIVITY_CENTER);
            case "SHARE" -> available(Destination.SHARE);
            case "BAG" -> available(Destination.SHOP_INVENTORY);
            case "MAIL" -> available(Destination.MAIL);
            case "MORE" -> available(Destination.MORE_MENU);
            default -> throw new IllegalArgumentException("Unknown Zhejiang bottom action: " + key);
        };
    }

    static Route more(MoreMenuItem item) {
        Objects.requireNonNull(item, "item");
        return switch (item) {
            case HEALTH_NOTICE ->
                    unavailable(Destination.UNAVAILABLE_HEALTH_NOTICE, "健康须知服务尚未接入");
            case RULES -> available(Destination.RULES);
            case SCORE_BOX -> available(Destination.SCORING_ASSISTANT);
            case ANNOUNCEMENT -> available(Destination.ANNOUNCEMENTS);
            case SETTINGS -> available(Destination.SETTINGS);
            case ZHEJIANG_NEWS ->
                    unavailable(Destination.UNAVAILABLE_ZHEJIANG_NEWS, "浙江新闻服务尚未接入");
            case WECHAT_PUBLIC ->
                    unavailable(Destination.UNAVAILABLE_WECHAT_PUBLIC, "公众号服务尚未接入");
        };
    }

    private static Route available(Destination destination) {
        return new Route(destination, null);
    }

    private static Route unavailable(Destination destination, String message) {
        return new Route(destination, message);
    }
}
