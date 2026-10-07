package com.desecratedtree.quill.texture.synth;

/**
 * Type 2.
 * Port of {@code Class348_Sub40_Sub19}.
 */
public final class LayerSub19 extends TextureLayer {

    public LayerSub19() {
        super(0, true);
    }

    @Override
    public int[] monoRow(int y, int tag) {
        int[] row = monoBuffer.row(y);
        if (monoBuffer.fresh) {
            System.arraycopy(TextureRender.U, 0, row, 0, TextureRender.WIDTH);
        }
        return row;
    }
}