package com.desecratedtree.quill.texture.synth;

/**
 * Type 35 — texture layer.
 * Port of {@code Class348_Sub40_Sub9}.
 */
public final class LayerSub9 extends TextureLayer {

    private int anInt9167 = 4096;

    public LayerSub9() {
        super(1, true);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        if (opcode == 0)
            anInt9167 = r.readUnsignedShort();
    }

    @Override
    public int[] monoRow(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}