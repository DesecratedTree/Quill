package com.desecratedtree.quill.render;

import java.awt.Color;

public final class CacheColor {

    private CacheColor() {
    }

    public static Color toColor(int packed) {
        int rgb = toRgb(packed);
        return new Color(rgb);
    }

    public static String toHex(int packed) {
        return String.format("#%06X", toRgb(packed));
    }

    public static int toRgb(int packed) {
        double hue = ((packed >> 10) & 0x3F) / 64.0 + 1.0 / 128.0;
        double saturation = ((packed >> 7) & 0x07) / 8.0 + 1.0 / 16.0;
        double lightness = (packed & 0x7F) / 128.0;
        double r;
        double g;
        double b;
        if (saturation == 0.0) {
            r = lightness;
            g = lightness;
            b = lightness;
        } else {
            double q = lightness < 0.5
                    ? lightness * (1.0 + saturation)
                    : lightness + saturation - lightness * saturation;
            double p = 2.0 * lightness - q;
            r = hueToRgb(p, q, hue + 1.0 / 3.0);
            g = hueToRgb(p, q, hue);
            b = hueToRgb(p, q, hue - 1.0 / 3.0);
        }
        return (channel(r) << 16) | (channel(g) << 8) | channel(b);
    }

    /**
     * Applies the 634 client's model-face lightness adjustment.  The client
     * shades the packed HSL lightness component before looking up the RGB
     * palette; multiplying RGB channels directly makes saturated faces look
     * washed out and introduces a grey ambient colour.
     */
    public static int shadedRgb(int packed, double brightness) {
        double clampedBrightness = Math.max(0.0, Math.min(1.0, brightness));
        int lightness = (packed & 0x7F) * (int) Math.round(clampedBrightness * 128.0) >> 7;
        lightness = clamp(lightness, 2, 126);
        return toRgb((packed & 0xFF80) | lightness);
    }

    /** Applies a packed face colour while treating a grayscale texture as a brightness mask. */
    public static int tintRgb(int packed, int rgb) {
        int textureBrightness = Math.max((rgb >> 16) & 0xFF,
                Math.max((rgb >> 8) & 0xFF, rgb & 0xFF));
        int base = toRgb(packed);
        int red = ((base >> 16) & 0xFF) * textureBrightness / 255;
        int green = ((base >> 8) & 0xFF) * textureBrightness / 255;
        int blue = (base & 0xFF) * textureBrightness / 255;
        return (red << 16) | (green << 8) | blue;
    }

    public static int fromColor(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        int hue = clamp(Math.round(hsb[0] * 63.0f), 0, 63);
        int saturation = clamp(Math.round(hsb[1] * 7.0f), 0, 7);
        double lightness = (Math.max(color.getRed(), Math.max(color.getGreen(), color.getBlue()))
                + Math.min(color.getRed(), Math.min(color.getGreen(), color.getBlue()))) / 510.0;
        int packedLightness = clamp((int) Math.round(lightness * 127.0), 0, 127);
        return (hue << 10) | (saturation << 7) | packedLightness;
    }

    private static double hueToRgb(double p, double q, double t) {
        if (t < 0.0) t += 1.0;
        if (t > 1.0) t -= 1.0;
        if (t < 1.0 / 6.0) return p + (q - p) * 6.0 * t;
        if (t < 1.0 / 2.0) return q;
        if (t < 2.0 / 3.0) return p + (q - p) * (2.0 / 3.0 - t) * 6.0;
        return p;
    }

    private static int channel(double value) {
        return clamp((int) Math.round(value * 255.0), 0, 255);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
