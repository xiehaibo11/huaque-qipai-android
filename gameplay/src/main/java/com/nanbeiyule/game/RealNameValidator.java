package com.nanbeiyule.game;

/**
 * Client-side real-name rules recovered from the original lobby identity
 * check module: whitespace is stripped, ASCII dots become the middle dot,
 * only letters and the middle dot are accepted, and the middle dot must not
 * lead or trail the name.
 */
final class RealNameValidator {
    private static final char MIDDLE_DOT = '·';

    private RealNameValidator() {}

    static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder normalized = new StringBuilder(raw.length());
        for (int index = 0; index < raw.length(); index++) {
            char character = raw.charAt(index);
            if (Character.isWhitespace(character)) {
                continue;
            }
            normalized.append(character == '.' ? MIDDLE_DOT : character);
        }
        return normalized.toString();
    }

    static boolean isValid(String raw) {
        String value = normalize(raw);
        if (value.isEmpty()
                || value.charAt(0) == MIDDLE_DOT
                || value.charAt(value.length() - 1) == MIDDLE_DOT) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character != MIDDLE_DOT && !Character.isLetter(character)) {
                return false;
            }
        }
        return true;
    }
}
