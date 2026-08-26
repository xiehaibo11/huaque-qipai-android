package com.nanbeiyule.game;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Immutable product copy used while the original payment-choice dialog is visible. */
record MembershipPurchaseSelection(String productCode, String productName, String priceText) {
    private static final Pattern PRICE_NUMBER = Pattern.compile("\\d+(?:\\.\\d+)?");

    MembershipPurchaseSelection {
        productCode = safeTrim(productCode);
        productName = safeTrim(productName);
        priceText = safeTrim(priceText);
    }

    boolean isValid() {
        return !productCode.isBlank() && !normalizedPrice().isBlank();
    }

    String prompt() {
        return "是否花费"
                + normalizedPrice()
                + "购买"
                + resolvedProductName()
                + "？购买后自动兑换";
    }

    private String resolvedProductName() {
        if (!productName.isBlank()) {
            return productName;
        }
        return switch (productCode) {
            case "SXVIP_CONTINUOUS_MONTH", "SXVIP_30_DAYS" -> "30天会员";
            case "SXVIP_90_DAYS" -> "90天会员";
            case "SXVIP_365_DAYS" -> "365天会员";
            case "SXVIP_7_DAYS" -> "7天会员";
            default -> "会员";
        };
    }

    private String normalizedPrice() {
        Matcher matcher = PRICE_NUMBER.matcher(priceText);
        return matcher.find() ? matcher.group() + "元" : "";
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
