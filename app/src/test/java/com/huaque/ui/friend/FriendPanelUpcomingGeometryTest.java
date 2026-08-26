package com.huaque.ui.friend;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FriendPanelUpcomingGeometryTest {
    @Test
    public void mapsOriginalUpcomingNodesIntoExpandedPanelCoordinates() {
        assertEquals(new FriendPanelUpcomingGeometry.Rect(21, 135, 536, 75),
                FriendPanelUpcomingGeometry.filterHeader());
        assertEquals(new FriendPanelUpcomingGeometry.Rect(42, 157, 42, 44),
                FriendPanelUpcomingGeometry.filterIcon());
        assertEquals(new FriendPanelUpcomingGeometry.Rect(96, 149, 174, 50),
                FriendPanelUpcomingGeometry.selectedName());
        assertEquals(new FriendPanelUpcomingGeometry.Rect(434, 142, 126, 61),
                FriendPanelUpcomingGeometry.filterButton());
        assertEquals(new FriendPanelUpcomingGeometry.Rect(423, 22, 432, 132),
                FriendPanelUpcomingGeometry.guideBubble());
        assertEquals(new FriendPanelUpcomingGeometry.Rect(457, 38, 364, 84),
                FriendPanelUpcomingGeometry.guideText());
        assertEquals(new FriendPanelUpcomingGeometry.Rect(26, 215, 526, 150),
                FriendPanelUpcomingGeometry.filterList());
        assertEquals(new FriendPanelUpcomingGeometry.Rect(59, 410, 442, 141),
                FriendPanelUpcomingGeometry.emptyMessage());
        assertEquals(new FriendPanelUpcomingGeometry.Rect(183, 986, 201, 77),
                FriendPanelUpcomingGeometry.refreshButton());
    }
}
