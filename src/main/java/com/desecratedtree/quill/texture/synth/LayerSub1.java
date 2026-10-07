package com.desecratedtree.quill.texture.synth;

/**
 * Type 25 — port of {@code Class348_Sub40_Sub1}.
 */
public final class LayerSub1 extends TextureLayer {

    private int anInt9084 = 4096;
    private int[] anIntArray9086 = new int[3];
    private int anInt9091 = 4096;
    private int anInt9092 = 4096;
    private int anInt9094 = 409;

    public LayerSub1() {
        super(1, false);
    }

    /** DEP: stub for external {@code Class139.method1166}. */
    private static int method1166(int a, int b) {
        return a & b;
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i = opcode;
        while_131_:
        do {
            while_130_:
            do {
                while_129_:
                do {
                    do {
                        if (i != 0) {
                            if ((i ^ 0xffffffff) != -2) {
                                if ((i ^ 0xffffffff) != -3) {
                                    if (i != 3) {
                                        if ((i ^ 0xffffffff) == -5)
                                            break while_130_;
                                        break while_131_;
                                    }
                                } else
                                    break;
                                break while_129_;
                            }
                        } else {
                            anInt9094 = r.readUnsignedShort();
                            return;
                        }
                        anInt9084 = r.readUnsignedShort();
                        return;
                    } while (false);
                    anInt9091 = r.readUnsignedShort();
                    return;
                } while (false);
                anInt9092 = r.readUnsignedShort();
                return;
            } while (false);
            int i_2_ = r.readUnsignedTriByte();
            anIntArray9086[2] = method1166(0, i_2_ >> -203039092);
            anIntArray9086[1] = method1166(i_2_, 65280) >> 408194532;
            anIntArray9086[0] = method1166(i_2_ << 926309764, 267386880);
        } while (false);
    }
}