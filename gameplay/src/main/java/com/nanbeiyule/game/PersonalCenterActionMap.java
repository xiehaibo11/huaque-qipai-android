package com.nanbeiyule.game;

final class PersonalCenterActionMap {
    private PersonalCenterActionMap() {}

    static PersonalCenterAction actionFor(int tab, float x, float y) {
        return switch (tab) {
            case 0 -> profile(x, y);
            case 1 -> inside(x, y, 700, 270, 1560, 810)
                    ? PersonalCenterAction.REAL_NAME
                    : null;
            case 2 -> privacy(x, y);
            case 3 -> phone(x, y);
            case 4 -> membership(x, y);
            default -> null;
        };
    }

    private static PersonalCenterAction profile(float x, float y) {
        if (inside(x, y, 1500, 220, 1625, 320)) {
            return PersonalCenterAction.SHOP_ROOM_CARDS;
        }
        if (inside(x, y, 1500, 330, 1625, 435)) {
            return PersonalCenterAction.BOUND_ROOM_CARD_HELP;
        }
        if (inside(x, y, 1500, 445, 1625, 550)) {
            return PersonalCenterAction.SHOP_DIAMONDS;
        }
        return inside(x, y, 700, 830, 935, 920)
                ? PersonalCenterAction.ACCOUNT_DELETION
                : null;
    }

    private static PersonalCenterAction privacy(float x, float y) {
        if (!inside(x, y, 1360, 200, 1590, 820)) {
            return null;
        }
        return y >= 330 && y <= 456
                ? PersonalCenterAction.TOGGLE_CLIPBOARD_PERMISSION
                : PersonalCenterAction.OPEN_APP_PERMISSION_SETTINGS;
    }

    private static PersonalCenterAction phone(float x, float y) {
        if (inside(x, y, 1170, 500, 1530, 650)) {
            return PersonalCenterAction.PHONE_SEND_CODE;
        }
        return inside(x, y, 990, 680, 1290, 840)
                ? PersonalCenterAction.PHONE_SUBMIT
                : null;
    }

    private static PersonalCenterAction membership(float x, float y) {
        if (inside(x, y, 545, 320, 710, 660)) {
            return PersonalCenterAction.MEMBERSHIP_PREVIOUS;
        }
        if (inside(x, y, 1460, 320, 1640, 660)) {
            return PersonalCenterAction.MEMBERSHIP_NEXT;
        }
        if (inside(x, y, 1320, 780, 1540, 920)) {
            return PersonalCenterAction.SHOP_DIAMONDS;
        }
        if (inside(x, y, 650, 225, 1435, 590)) {
            return PersonalCenterAction.MEMBERSHIP_CENTER;
        }
        return inside(x, y, 650, 600, 1320, 920)
                ? PersonalCenterAction.MEMBERSHIP_GIFT
                : null;
    }

    private static boolean inside(
            float x, float y, float left, float top, float right, float bottom) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }
}
