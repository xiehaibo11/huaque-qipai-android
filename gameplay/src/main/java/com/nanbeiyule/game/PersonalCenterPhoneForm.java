package com.nanbeiyule.game;

record PersonalCenterPhoneForm(String phoneNumber, String code) {
    static PersonalCenterPhoneForm phoneOnly(String rawPhoneNumber) {
        return new PersonalCenterPhoneForm(normalizePhone(rawPhoneNumber), "");
    }

    static PersonalCenterPhoneForm validated(
            String rawPhoneNumber, String rawCode) {
        String code = rawCode == null ? "" : rawCode.trim();
        if (!code.matches("\\d{6}")) {
            throw new IllegalArgumentException("请输入正确的短信验证码");
        }
        return new PersonalCenterPhoneForm(
                normalizePhone(rawPhoneNumber), code);
    }

    private static String normalizePhone(String rawPhoneNumber) {
        String phone =
                rawPhoneNumber == null
                        ? ""
                        : rawPhoneNumber.replaceAll("[\\s-]", "");
        if (phone.startsWith("+86")) {
            phone = phone.substring(3);
        } else if (phone.startsWith("0086")) {
            phone = phone.substring(4);
        }
        if (!phone.matches("1[3-9]\\d{9}")) {
            throw new IllegalArgumentException("请输入正确的手机号");
        }
        return phone;
    }
}
