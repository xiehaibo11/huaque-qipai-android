package com.huaque.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LobbyLayoutModelTest {
    @Test
    public void mapsLobbyPsdXCoordinatesToVirtualLayout() {
        assertEquals(0, LobbyLayoutModel.x(0));
        assertEquals(1920, LobbyLayoutModel.x(2448));
        assertEquals(909, LobbyLayoutModel.x(1159));
        assertEquals(928, LobbyLayoutModel.width(1183));
    }
}
