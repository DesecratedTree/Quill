package com.desecratedtree.quill.texture.synth;

/**
 * Type 14 — monochrome rounded-mask layer.
 * Port of {@code Class348_Sub40_Sub11}.
 */
public final class LayerSub11 extends TextureLayer {

    private int anInt9187 = 585;

    public LayerSub11() {
        super(0, true);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i_9_ = opcode;
        if (i_9_ == 0)
            anInt9187 = r.readUnsignedShort();
    }

    @Override
    public int[] monoRow(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}