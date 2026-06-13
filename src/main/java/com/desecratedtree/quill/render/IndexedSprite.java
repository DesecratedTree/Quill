package com.desecratedtree.quill.render;

import java.awt.image.BufferedImage;

public final class IndexedSprite {

    public int offsetX;

    public int offsetY;

    public int width;

    public int height;

    public int deltaWidth;

    public int deltaHeight;

    public byte[] alpha;

    public byte[] raster;

    public int[] palette;

    public BufferedImage toBufferedImage() {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        BufferedImage image = new BufferedImage(safeWidth, safeHeight, BufferedImage.TYPE_INT_ARGB);
        if (width <= 0 || height <= 0 || raster == null || palette == null) {
            return image;
        }
        int pixelCount = width * height;
        int rasterLength = Math.min(raster.length, pixelCount);
        int alphaLength = alpha == null ? 0 : Math.min(alpha.length, pixelCount);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int index = x + y * width;
                if (index >= rasterLength) {
                    continue;
                }
                int paletteIndex = raster[index] & 0xFF;
                int color = paletteIndex < palette.length ? palette[paletteIndex] : 0;
                if (alpha == null) {
                    if (color != 0) {
                        image.setRGB(x, y, 0xFF000000 | color);
                    }
                } else if (index < alphaLength) {
                    image.setRGB(x, y, color | ((alpha[index] & 0xFF) << 24));
                }
            }
        }
        return image;
    }
}
