package com.nanbeiyule.game;

import java.time.LocalDate;

/**
 * Client-side pre-validation for mainland 18-digit resident identity numbers.
 * Mirrors the authoritative server rules: shape, birth date, and the
 * MOD 11-2 check digit.
 */
final class IdCardNumberValidator {
    private static final int[] WEIGHTS = {
        7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2
    };
    private static final char[] CHECK_CODES = {
        '1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'
    };

    private IdCardNumberValidator() {}

    static String normalize(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase(java.util.Locale.ROOT);
    }

    static boolean isValid(String raw) {
        String value = normalize(raw);
        if (value.length() != 18 || value.charAt(0) == '0') {
            return false;
        }
        for (int index = 0; index < 17; index++) {
            if (!Character.isDigit(value.charAt(index))) {
                return false;
            }
        }
        char checkDigit = value.charAt(17);
        if (!Character.isDigit(checkDigit) && checkDigit != 'X') {
            return false;
        }
        if (birthDate(value) == null) {
            return false;
        }
        int sum = 0;
        for (int index = 0; index < 17; index++) {
            sum += (value.charAt(index) - '0') * WEIGHTS[index];
        }
        return CHECK_CODES[sum % 11] == checkDigit;
    }

    static boolean isAdult(String raw) {
        return isAdult(raw, LocalDate.now());
    }

    static boolean isAdult(String raw, LocalDate today) {
        if (!isValid(raw) || today == null) {
            return false;
        }
        LocalDate birthDate = birthDate(normalize(raw));
        return !today.isBefore(birthDate.plusYears(18));
    }

    private static LocalDate birthDate(String value) {
        if (value.length() != 18) {
            return null;
        }
        try {
            int year = Integer.parseInt(value.substring(6, 10));
            int month = Integer.parseInt(value.substring(10, 12));
            int day = Integer.parseInt(value.substring(12, 14));
            return LocalDate.of(year, month, day);
        } catch (RuntimeException invalid) {
            return null;
        }
    }
}
