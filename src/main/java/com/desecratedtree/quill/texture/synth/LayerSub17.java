package com.desecratedtree.quill.texture.synth;

/**
 * Type 39 — texture layer.
 * Port of {@code Class348_Sub40_Sub17}.
 */
public final class LayerSub17 extends TextureLayer {

    int[] anIntArray9232;
    int anInt9237;
    int anInt9241;
    private int anInt9243 = -1;

    public LayerSub17() {
        super(0, false);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        if (opcode == 0)
            anInt9243 = r.readUnsignedShort();
    }

    @Override
    public int spriteFileId() {
        return anInt9243;
    }

    @Override
    public void allocate(int width, int height, int tag) {
        super.allocate(width, height, tag);
        SpriteRaster raster = sprite(anInt9243);
        anInt9237 = raster == null ? width : raster.width;
        anInt9241 = raster == null ? height : raster.height;
    }

    @Override
    public int[][] colorRows(int y, int tag) {
        int[][] row = colorBuffer.row(y);
        if (colorBuffer.fresh) {
            SpriteRaster raster = sprite(anInt9243);
            if (raster == null || raster.pixels.length == 0) {
                for (int channel = 0; channel < 3; channel++) {
                    java.util.Arrays.fill(row[channel], 0);
                }
                return row;
            }
            int sourceY = anInt9241 == TextureRender.HEIGHT
                    ? y
                    : anInt9241 * y / Math.max(1, TextureRender.HEIGHT);
            sourceY = Math.max(0, Math.min(raster.height - 1, sourceY));
            for (int x = 0; x < TextureRender.WIDTH; x++) {
                int sourceX = anInt9237 == TextureRender.WIDTH
                        ? x
                        : anInt9237 * x / Math.max(1, TextureRender.WIDTH);
                sourceX = Math.max(0, Math.min(raster.width - 1, sourceX));
                int pixel = raster.pixels[sourceY * raster.width + sourceX];
                row[0][x] = ((pixel >> 16) & 0xFF) << 4;
                row[1][x] = ((pixel >> 8) & 0xFF) << 4;
                row[2][x] = (pixel & 0xFF) << 4;
            }
        }
        return row;
    }

    @Override
    protected void onRelease() {
        anIntArray9232 = null;
    }
}