package com.desecratedtree.quill.texture.synth;

/**
 * Type 16 — monochrome tiling-grid layer.
 * Port of {@code Class348_Sub40_Sub2}.
 */
public final class LayerSub2 extends TextureLayer {

    private int anInt9095 = 1;
    private int anInt9098 = 204;
    private int anInt9099 = 1;

    public LayerSub2() {
        super(0, true);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i_1_ = opcode;
        while_132_:
        do {
            do {
                if (i_1_ != 0) {
                    if ((i_1_ ^ 0xffffffff) != -2) {
                        if ((i_1_ ^ 0xffffffff) == -3)
                            break;
                        break while_132_;
                    }
                } else {
                    anInt9099 = r.readUnsignedByte();
                    break while_132_;
                }
                anInt9095 = r.readUnsignedByte();
                break while_132_;
            } while (false);
            anInt9098 = r.readUnsignedShort();
        } while (false);
    }

    @Override
    public int[] monoRow(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}