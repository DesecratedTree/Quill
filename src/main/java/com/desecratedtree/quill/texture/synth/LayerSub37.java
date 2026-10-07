package com.desecratedtree.quill.texture.synth;

/**
 * Type 5.
 * Port of {@code Class348_Sub40_Sub37}.
 */
public final class LayerSub37 extends TextureLayer {

    private int anInt9463 = 1;
    private int anInt9466 = 1;

    public LayerSub37() {
        super(1, false);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i_12_ = opcode;
        while_212_:
        do {
            do {
                if ((i_12_ ^ 0xffffffff) != -1) {
                    if ((i_12_ ^ 0xffffffff) != -2) {
                        if ((i_12_ ^ 0xffffffff) == -3)
                            break;
                        break while_212_;
                    }
                } else {
                    anInt9466 = r.readUnsignedByte();
                    return;
                }
                anInt9463 = r.readUnsignedByte();
                return;
            } while (false);
            this.mono = r.readUnsignedByte() == 1;
        } while (false);
    }

    @Override
    public int[][] colorRows(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }

    @Override
    public int[] monoRow(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}