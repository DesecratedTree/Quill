package com.desecratedtree.quill.texture.synth;

/**
 * Type 12 — monochrome smooth radial gradient.
 * Port of {@code Class348_Sub40_Sub36}.
 */
public final class LayerSub36 extends TextureLayer {

    private int anInt9451 = 0;
    private int anInt9453 = 1;
    private int anInt9455 = 0;

    public LayerSub36() {
        super(0, true);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i_1_ = opcode;
        while_211_:
        do {
            do {
                if ((i_1_ ^ 0xffffffff) != -1) {
                    if (i_1_ != 1) {
                        if (i_1_ == 3)
                            break;
                        break while_211_;
                    }
                } else {
                    anInt9451 = r.readUnsignedByte();
                    return;
                }
                anInt9455 = r.readUnsignedByte();
                return;
            } while (false);
            anInt9453 = r.readUnsignedByte();
        } while (false);
    }

    @Override
    public int[] monoRow(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}