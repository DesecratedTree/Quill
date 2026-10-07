package com.desecratedtree.quill.texture.synth;

/**
 * Type 33 — texture layer.
 * Port of {@code Class348_Sub40_Sub34}.
 */
public final class LayerSub34 extends TextureLayer {

    private int anInt9438 = 4096;
    private boolean aBoolean9439 = true;

    public LayerSub34() {
        super(1, false);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i = opcode;
        if (i != 0) {
            if ((i ^ 0xffffffff) != -2) {
                return;
            }
        } else {
            anInt9438 = r.readUnsignedShort();
            return;
        }
        aBoolean9439 = r.readUnsignedByte() == 1;
    }

    @Override
    public int[][] colorRows(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}