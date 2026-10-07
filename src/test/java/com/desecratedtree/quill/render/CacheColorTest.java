package com.desecratedtree.quill.render;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CacheColorTest {
    @Test
    void packed634ColorsRemainSaturated() {
        int rgb = CacheColor.toRgb(5018);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        assertTrue(red > green * 2, "expected the packed orange face to remain saturated");
        assertTrue(red > blue * 2, "expected the packed orange face to remain saturated");
    }

    @Test
    void shadingChangesLightnessWithoutAddingGrey() {
        int packed = 5018;
        int dark = CacheColor.shadedRgb(packed, 0.5);
        int base = CacheColor.toRgb(packed);
        assertTrue(((dark >> 16) & 0xFF) < ((base >> 16) & 0xFF));
        assertTrue((dark & 0xFF) < 32, "blue should not receive an ambient grey lift");
    }
}
