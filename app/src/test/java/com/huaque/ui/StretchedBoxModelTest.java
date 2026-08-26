package com.huaque.ui;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class StretchedBoxModelTest {
    @Test
    public void mapsVirtualBoxWithIndependentFitXyScales() {
        assertArrayEquals(
                new int[]{825, 663, 250, 96},
                StretchedBoxModel.map(1600, 900, 990, 795, 300, 115));
        assertArrayEquals(
                new int[]{1299, 795, 394, 115},
                StretchedBoxModel.map(2520, 1080, 990, 795, 300, 115));
        assertArrayEquals(
                new int[]{990, 994, 300, 144},
                StretchedBoxModel.map(1920, 1350, 990, 795, 300, 115));
    }
}
