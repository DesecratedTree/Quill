package com.desecratedtree.quill.texture.synth;

/**
 * Type 18 — sprite-textured colour layer. Port of
 * {@code Class348_Sub40_Sub17_Sub1}. The sprite file id it renders comes from
 * its superclass {@code Class348_Sub40_Sub17} (anInt9243, parse opcode 0 and
 * method3037), folded in here because this port extends TextureLayer directly.
 */
public final class LayerSub17Sub1 extends TextureLayer {

    private int anInt9243 = -1;

    public LayerSub17Sub1() {
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
        sourceWidth = raster == null ? width : raster.width;
        sourceHeight = raster == null ? height : raster.height;
    }

    private int sourceWidth;
    private int sourceHeight;

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
            int sourceY = sourceHeight == TextureRender.HEIGHT
                    ? y
                    : sourceHeight * y / Math.max(1, TextureRender.HEIGHT);
            sourceY = Math.max(0, Math.min(raster.height - 1, sourceY));
            for (int x = 0; x < TextureRender.WIDTH; x++) {
                int sourceX = sourceWidth == TextureRender.WIDTH
                        ? x
                        : sourceWidth * x / Math.max(1, TextureRender.WIDTH);
                sourceX = Math.max(0, Math.min(raster.width - 1, sourceX));
                int pixel = raster.pixels[sourceY * raster.width + sourceX];
                row[0][x] = ((pixel >> 16) & 0xFF) << 4;
                row[1][x] = ((pixel >> 8) & 0xFF) << 4;
                row[2][x] = (pixel & 0xFF) << 4;
            }
        }
        return row;
    }
}