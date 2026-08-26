package com.nanbeiyule.game;

import java.util.List;

/** Six-position input model recovered from JoinBoxRoom/View.lua. */
record JoinRoomInput(String roomNumber) {
    private static final List<String> PLACEHOLDERS = List.of("请", "输", "入", "房", "间", "号");

    JoinRoomInput {
        roomNumber = roomNumber == null ? "" : roomNumber;
        if (roomNumber.length() > 6 || !roomNumber.matches("[0-9]*")) {
            throw new IllegalArgumentException("roomNumber must contain at most six digits");
        }
    }

    static JoinRoomInput empty() {
        return new JoinRoomInput("");
    }

    static JoinRoomInput from(String roomNumber) {
        return new JoinRoomInput(roomNumber);
    }

    JoinRoomInput append(int digit) {
        if (digit < 0 || digit > 9 || isComplete()) {
            return this;
        }
        return new JoinRoomInput(roomNumber + digit);
    }

    JoinRoomInput deleteLast() {
        if (roomNumber.isEmpty()) {
            return this;
        }
        return new JoinRoomInput(roomNumber.substring(0, roomNumber.length() - 1));
    }

    JoinRoomInput clear() {
        return roomNumber.isEmpty() ? this : empty();
    }

    boolean isComplete() {
        return roomNumber.length() == 6;
    }

    /**
     * 闲逸圆槽在未输入时是空的，不像浙江那条号码带会显示「请输入房间号」占位字。
     * 提示文案改由标题牌承担。
     */
    String digitAt(int index) {
        if (index < 0 || index >= 6) {
            throw new IndexOutOfBoundsException("index: " + index);
        }
        return index < roomNumber.length()
                ? roomNumber.substring(index, index + 1)
                : "";
    }

    String textAt(int index) {
        if (index < 0 || index >= 6) {
            throw new IndexOutOfBoundsException("index: " + index);
        }
        return index < roomNumber.length()
                ? roomNumber.substring(index, index + 1)
                : PLACEHOLDERS.get(index);
    }
}
