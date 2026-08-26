package com.nanbeiyule.game;

import java.util.regex.Pattern;

final class PhoneNumberValidator {
    private static final Pattern MAINLAND_PHONE = Pattern.compile("^1[3-9]\\d{9}$");

    private PhoneNumberValidator() {
    }

    static String normalize(String rawPhoneNumber) {
        if (rawPhoneNumber == null) {
            return null;
        }
        String normalized = rawPhoneNumber.replaceAll("[\\s-]", "");
        if (normalized.startsWith("+86")) {
            normalized = normalized.substring(3);
        } else if (normalized.startsWith("0086")) {
            normalized = normalized.substring(4);
        } else if (normalized.length() == 13 && normalized.startsWith("86")) {
            normalized = normalized.substring(2);
        }
        return MAINLAND_PHONE.matcher(normalized).matches() ? normalized : null;
    }
}
