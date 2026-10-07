package com.desecratedtree.quill.texture.synth;

/**
 * Type 28 — port of {@code Class348_Sub40_Sub23}.
 */
public final class LayerSub23 extends TextureLayer {

    private int anInt9306;
    private int anInt9310;
    private int anInt9311 = 1024;
    private int anInt9312 = 0;
    private int anInt9314;
    private int anInt9317;
    private int anInt9318;
    private int anInt9320;
    private int anInt9322;
    private int anInt9323;

    public LayerSub23() {
        super(0, true);
        anInt9310 = 1024;
        anInt9314 = 1024;
        anInt9318 = 0;
        anInt9317 = 1024;
        anInt9320 = 2048;
        anInt9322 = 409;
        anInt9323 = 819;
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i = opcode;
        while_196_:
        do {
            while_195_:
            do {
                while_194_:
                do {
                    while_193_:
                    do {
                        while_192_:
                        do {
                            while_191_:
                            do {
                                while_190_:
                                do {
                                    do {
                                        if ((i ^ 0xffffffff) != -1) {
                                            if ((i ^ 0xffffffff) != -2) {
                                                if (i != 2) {
                                                    if (i != 3) {
                                                        if (i != 4) {
                                                            if ((i ^ 0xffffffff) != -6) {
                                                                if (i != 6) {
                                                                    if ((i ^ 0xffffffff) != -8) {
                                                                        if (i == 8)
                                                                            break while_195_;
                                                                        break while_196_;
                                                                    }
                                                                } else
                                                                    break while_193_;
                                                                break while_194_;
                                                            }
                                                        } else
                                                            break while_191_;
                                                        break while_192_;
                                                    }
                                                } else
                                                    break;
                                                break while_190_;
                                            }
                                        } else {
                                            anInt9318 = r.readUnsignedByte();
                                            return;
                                        }
                                        anInt9317 = r.readUnsignedShort();
                                        return;
                                    } while (false);
                                    anInt9320 = r.readUnsignedShort();
                                    return;
                                } while (false);
                                anInt9322 = r.readUnsignedShort();
                                return;
                            } while (false);
                            anInt9323 = r.readUnsignedShort();
                            return;
                        } while (false);
                        anInt9311 = r.readUnsignedShort();
                        return;
                    } while (false);
                    anInt9312 = r.readUnsignedByte();
                    return;
                } while (false);
                anInt9314 = r.readUnsignedShort();
                return;
            } while (false);
            anInt9310 = r.readUnsignedShort();
            return;
        } while (false);
    }
}