package com.desecratedtree.quill.texture.synth;

/**
 * Type 0 — constant monochrome layer.
 * Port of {@code Class348_Sub40_Sub15}.
 */
public final class LayerSub15 extends TextureLayer {

    private int anInt9220 = 4096;

    public LayerSub15() {
        this(4096);
    }

    public LayerSub15(int value) {
        super(0, true);
        anInt9220 = value;
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        if (opcode == 0) {
            anInt9220 = (r.readUnsignedByte() << -85536916) / 255;
        }
    }

    @Override
    public int[] monoRow(int y, int tag) {
        int[] row = monoBuffer.row(y);
        if (monoBuffer.fresh) {
            TextureRender.fill(TextureRender.WIDTH, row, 0, anInt9220);
        }
        return row;
    }
}