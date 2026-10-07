package com.desecratedtree.quill.texture.synth;

/**
 * Type 4.
 * Port of {@code Class348_Sub40_Sub22}.
 */
public final class LayerSub22 extends TextureLayer {

    private int anInt9284 = 1024;
    private int[][] anIntArrayArray9286;
    private int[][] anIntArrayArray9287;
    private int anInt9288 = 1024;
    private int anInt9291;
    private int anInt9293 = 0;
    private int anInt9294;
    private int[] anIntArray9297;
    private int anInt9298;
    private int anInt9299 = 4;
    private int anInt9300;
    private int anInt9301;
    private int anInt9302;
    private int anInt9305;

    public LayerSub22() {
        super(0, true);
        anInt9294 = 81;
        anInt9302 = 409;
        anInt9305 = 204;
        anInt9301 = 8;
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i_9_ = opcode;
        while_189_:
        do {
            while_188_:
            do {
                while_187_:
                do {
                    while_186_:
                    do {
                        while_185_:
                        do {
                            while_184_:
                            do {
                                do {
                                    if ((i_9_ ^ 0xffffffff) != -1) {
                                        if (i_9_ != 1) {
                                            if ((i_9_ ^ 0xffffffff) != -3) {
                                                if ((i_9_ ^ 0xffffffff) != -4) {
                                                    if (i_9_ != 4) {
                                                        if (i_9_ != 5) {
                                                            if ((i_9_ ^ 0xffffffff) != -7) {
                                                                if ((i_9_ ^ 0xffffffff) != -8)
                                                                    break while_189_;
                                                            } else
                                                                break while_187_;
                                                            break while_188_;
                                                        }
                                                    } else
                                                        break while_185_;
                                                    break while_186_;
                                                }
                                            } else
                                                break;
                                            break while_184_;
                                        }
                                    } else {
                                        anInt9299 = r.readUnsignedByte();
                                        return;
                                    }
                                    anInt9301 = r.readUnsignedByte();
                                    return;
                                } while (false);
                                anInt9302 = r.readUnsignedShort();
                                return;
                            } while (false);
                            anInt9305 = r.readUnsignedShort();
                            return;
                        } while (false);
                        anInt9288 = r.readUnsignedShort();
                        return;
                    } while (false);
                    anInt9293 = r.readUnsignedShort();
                    return;
                } while (false);
                anInt9294 = r.readUnsignedShort();
                return;
            } while (false);
            anInt9284 = r.readUnsignedShort();
        } while (false);
    }

    @Override
    public int[] monoRow(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}