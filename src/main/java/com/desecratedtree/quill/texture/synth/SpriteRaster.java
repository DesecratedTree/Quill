package com.desecratedtree.quill.texture.synth;

/**
 * Decoded sprite frame used by sprite layers. Port of the client's
 * {@code Class207} interface surface for texture sources ({@code anIntArray9232}
 * / {@code anInt2696} / {@code anInt2702}).
 */
public final class SpriteRaster {

    /** ARGB pixels, row-major ({@code y * width + x}). */
    public final int[] pixels;
    public final int width;
    public final int height;

    public SpriteRaster(int[] pixels, int width, int height) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
    }
}