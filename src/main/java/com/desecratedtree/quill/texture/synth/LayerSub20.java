package com.desecratedtree.quill.texture.synth;

/**
 * Type 13 — monochrome value-noise layer.
 * Port of {@code Class348_Sub40_Sub20}.
 */
public final class LayerSub20 extends TextureLayer {

    public LayerSub20() {
        super(0, true);
    }

    @Override
    public int[] monoRow(int y, int tag) {
        int[] row = monoBuffer.row(y);
        if (monoBuffer.fresh) {
            int yCoordinate = TextureRender.V[y];
            for (int x = 0; x < TextureRender.WIDTH; x++) {
                row[x] = noise(yCoordinate, TextureRender.U[x]) % 4096;
            }
        }
        return row;
    }

    private static int noise(int y, int x) {
        int value = y + 57 * x;
        value ^= value << 1;
        return -(((789221 + 15731 * (value * value)) * value + 1376312589 & 0x7FFFFFFF) / 262144) + 4096;
    }
}