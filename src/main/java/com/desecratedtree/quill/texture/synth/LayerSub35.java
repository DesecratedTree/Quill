package com.desecratedtree.quill.texture.synth;

/**
 * Type 32 — texture layer.
 * Port of {@code Class348_Sub40_Sub35}.
 */
public final class LayerSub35 extends TextureLayer {

    private int anInt9445 = 3216;
    private int anInt9447 = 3216;
    private int anInt9448 = 4096;
    private int[] anIntArray9449 = new int[3];

    public LayerSub35() {
        super(1, true);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i = opcode;
        while_210_:
        do {
            do {
                if ((i ^ 0xffffffff) != -1) {
                    if (i != 1) {
                        if ((i ^ 0xffffffff) == -3)
                            break;
                        break while_210_;
                    }
                } else {
                    anInt9448 = r.readUnsignedShort();
                    break while_210_;
                }
                anInt9447 = r.readUnsignedShort();
                break while_210_;
            } while (false);
            anInt9445 = r.readUnsignedShort();
        } while (false);
    }

    @Override
    public int[] monoRow(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}