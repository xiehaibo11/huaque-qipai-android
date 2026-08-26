package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ScoreAssistantFormSizingTest {
    @Test
    public void formWidthKeepsLandscapeRatioButNeverExceedsNarrowWindow() {
        assertEquals(1728, ScoreAssistantFormSizing.dialogWidth(2400, 3f, 0.72f));
        assertEquals(282, ScoreAssistantFormSizing.dialogWidth(300, 3f, 0.72f));
        assertEquals(1128, ScoreAssistantFormSizing.dialogWidth(1200, 4f, 0.72f));
    }

    @Test
    public void onlyNewestRequestGenerationCanMutateVisibleTab() {
        ScoreAssistantRequestGate gate = new ScoreAssistantRequestGate();
        long activeRequest = gate.issue();
        long historyRequest = gate.issue();

        assertFalse(gate.isCurrent(activeRequest));
        assertTrue(gate.isCurrent(historyRequest));
        gate.invalidate();
        assertFalse(gate.isCurrent(historyRequest));
    }
}
