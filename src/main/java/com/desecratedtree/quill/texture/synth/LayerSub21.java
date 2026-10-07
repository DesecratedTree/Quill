package com.desecratedtree.quill.texture.synth;

/**
 * Type 37 — texture layer.
 * Port of {@code Class348_Sub40_Sub21}.
 */
public final class LayerSub21 extends TextureLayer {

    private int anInt9266 = 0;
    private int anInt9269 = 2048;
    private int anInt9276 = 4096;
    private int anInt9277;
    private int anInt9278 = 0;
    private int anInt9279 = 12288;
    private int anInt9281;

    public LayerSub21() {
        super(0, true);
        anInt9277 = 8192;
        anInt9281 = 2048;
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i = opcode;
        while_183_:
        do {
            while_182_:
            do {
                while_181_:
                do {
                    while_180_:
                    do {
                        while_179_:
                        do {
                            do {
                                if (i != 0) {
                                    if (i != 1) {
                                        if ((i ^ 0xffffffff) != -3) {
                                            if ((i ^ 0xffffffff) != -4) {
                                                if (i != 4) {
                                                    if (i != 5) {
                                                        if ((i ^ 0xffffffff) == -7)
                                                            break while_182_;
                                                        break while_183_;
                                                    }
                                                } else
                                                    break while_180_;
                                                break while_181_;
                                            }
                                        } else
                                            break;
                                        break while_179_;
                                    }
                                } else {
                                    anInt9269 = r.readUnsignedShort();
                                    return;
                                }
                                anInt9278 = r.readUnsignedShort();
                                return;
                            } while (false);
                            anInt9266 = r.readUnsignedShort();
                            return;
                        } while (false);
                        anInt9281 = r.readUnsignedShort();
                        return;
                    } while (false);
                    anInt9279 = r.readUnsignedShort();
                    return;
                } while (false);
                anInt9276 = r.readUnsignedShort();
                return;
            } while (false);
            anInt9277 = r.readUnsignedShort();
        } while (false);
    }

    @Override
    public int[] monoRow(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}