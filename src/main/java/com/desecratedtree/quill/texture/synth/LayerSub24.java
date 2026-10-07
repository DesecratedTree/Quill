package com.desecratedtree.quill.texture.synth;

/**
 * Type 27 — port of {@code Class348_Sub40_Sub24}.
 */
public final class LayerSub24 extends TextureLayer {

    private int anInt9325 = 0;
    private int anInt9329 = 10;
    private int[] anIntArray9332;
    private int[] anIntArray9333;
    private int anInt9334 = 2048;

    public LayerSub24() {
        super(0, true);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i = opcode;
        while_198_:
        do {
            do {
                if (i != 0) {
                    if (i != 1) {
                        if ((i ^ 0xffffffff) == -3)
                            break;
                        break while_198_;
                    }
                } else {
                    anInt9329 = r.readUnsignedByte();
                    break while_198_;
                }
                anInt9334 = r.readUnsignedShort();
                break while_198_;
            } while (false);
            anInt9325 = r.readUnsignedByte();
        } while (false);
    }
}