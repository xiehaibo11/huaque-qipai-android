package com.huaque.ui.friend;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FriendPanelCollapsedGeometryTest {
    @Test
    public void mapsOriginalCsbNodesIntoCollapsedRailCoordinates() {
        assertEquals(new FriendPanelCollapsedGeometry.Rect(0, 0, 218, 630),
                FriendPanelCollapsedGeometry.backgroundBounds());
        assertEquals(new FriendPanelCollapsedGeometry.Rect(2, 19, 188, 75),
                FriendPanelCollapsedGeometry.titleBounds());
        assertEquals(new FriendPanelCollapsedGeometry.Rect(28, 295, 137, 39),
                FriendPanelCollapsedGeometry.emptyLabelBounds());
        assertEquals(new FriendPanelCollapsedGeometry.Rect(178, 246, 68, 139),
                FriendPanelCollapsedGeometry.openArrowBounds());
        assertEquals(33.6f, FriendPanelCollapsedGeometry.emptyLabelTextSize(), 0.001f);
    }
}
