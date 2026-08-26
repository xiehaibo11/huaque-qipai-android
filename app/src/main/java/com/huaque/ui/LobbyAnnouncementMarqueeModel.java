package com.huaque.ui;

import java.util.List;

final class LobbyAnnouncementMarqueeModel {
    private static final float SPEED = 110f;

    private List<String> messages = List.of();
    private int currentIndex;
    private float currentX;

    void setMessages(List<String> values, float viewportWidth) {
        messages = List.copyOf(values);
        currentIndex = 0;
        currentX = viewportWidth;
    }

    void advance(float elapsedSeconds, float textWidth, float viewportWidth) {
        if (messages.isEmpty()) {
            return;
        }
        if (currentX < -textWidth) {
            currentIndex = (currentIndex + 1) % messages.size();
            currentX = viewportWidth;
            return;
        }
        currentX -= SPEED * Math.max(0f, elapsedSeconds);
    }

    String currentMessage() {
        return messages.isEmpty() ? "" : messages.get(currentIndex);
    }

    float currentX() {
        return currentX;
    }
}
