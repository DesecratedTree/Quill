package com.desecratedtree.quill.texture.synth;

/**
 * Type 36 — texture layer.
 * Port of {@code Class348_Sub40_Sub29}.
 */
public final class LayerSub29 extends TextureLayer {

    private int anInt9374;
    private int[] anIntArray9375;
    private int anInt9379;
    private int anInt9380 = -1;

    public LayerSub29() {
        super(0, false);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        if ((opcode ^ 0xffffffff) == -1)
            anInt9380 = r.readUnsignedShort();
    }

    @Override
    public int auxiliaryFileId() {
        return anInt9380;
    }

    @Override
    public void allocate(int width, int height, int tag) {
        super.allocate(width, height, tag);
        anIntArray9375 = null;
        anInt9374 = width;
        anInt9379 = height;
        if (anInt9380 >= 0 && TextureRender.spriteProvider instanceof TextureProvider) {
            SpriteRaster raster = ((TextureProvider) TextureRender.spriteProvider).texture(anInt9380);
            if (raster != null && raster.pixels.length > 0) {
                anIntArray9375 = raster.pixels;
                anInt9374 = raster.width;
                anInt9379 = raster.height;
            }
        }
    }

    @Override
    public int[][] colorRows(int y, int tag) {
        int[][] row = colorBuffer.row(y);
        if (colorBuffer.fresh) {
            if (anIntArray9375 == null || anIntArray9375.length == 0) {
                for (int channel = 0; channel < 3; channel++) {
                    java.util.Arrays.fill(row[channel], 0);
                }
                return row;
            }
            int sourceY = anInt9379 == TextureRender.HEIGHT
                    ? y
                    : anInt9379 * y / Math.max(1, TextureRender.HEIGHT);
            sourceY = Math.max(0, Math.min(anInt9379 - 1, sourceY));
            for (int x = 0; x < TextureRender.WIDTH; x++) {
                int sourceX = anInt9374 == TextureRender.WIDTH
                        ? x
                        : anInt9374 * x / Math.max(1, TextureRender.WIDTH);
                sourceX = Math.max(0, Math.min(anInt9374 - 1, sourceX));
                int pixel = anIntArray9375[sourceY * anInt9374 + sourceX];
                row[0][x] = ((pixel >> 16) & 0xFF) << 4;
                row[1][x] = ((pixel >> 8) & 0xFF) << 4;
                row[2][x] = (pixel & 0xFF) << 4;
            }
        }
        return row;
    }

    @Override
    protected void onRelease() {
        anIntArray9375 = null;
    }
}