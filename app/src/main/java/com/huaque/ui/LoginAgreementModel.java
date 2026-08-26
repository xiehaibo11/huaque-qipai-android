package com.huaque.ui;

final class LoginAgreementModel {
    static final boolean DEFAULT_ACCEPTED = false;

    private LoginAgreementModel() {
    }

    static boolean toggle(boolean accepted) {
        return !accepted;
    }

    static boolean canContinue(boolean accepted) {
        return accepted;
    }

    static boolean requiresPrompt(String acceptedVersion, String currentVersion) {
        return currentVersion == null
                || currentVersion.isBlank()
                || !currentVersion.equals(acceptedVersion);
    }
}
