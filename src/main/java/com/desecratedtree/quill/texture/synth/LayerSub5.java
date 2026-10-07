package com.desecratedtree.quill.texture.synth;

/**
 * Type 15 — monochrome fractal-value-noise layer.
 * Port of {@code Class348_Sub40_Sub5}.
 */
public final class LayerSub5 extends TextureLayer {

    private short[] aShortArray9116 = new short[512];
    private int anInt9117;
    private int anInt9118 = 1;
    private byte[] aByteArray9119 = new byte[512];
    private int anInt9122;
    private int anInt9124;
    private int anInt9125 = 2048;
    private int anInt9129;

    public LayerSub5() {
        super(0, true);
        anInt9124 = 2;
        anInt9122 = 0;
        anInt9117 = 5;
        anInt9129 = 5;
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i_18_ = opcode;
        while_145_:
        do {
            while_144_:
            do {
                while_143_:
                do {
                    while_142_:
                    do {
                        while_141_:
                        do {
                            do {
                                if (i_18_ != 0) {
                                    if ((i_18_ ^ 0xffffffff) != -2) {
                                        if (i_18_ != 2) {
                                            if ((i_18_ ^ 0xffffffff) != -4) {
                                                if (i_18_ != 4) {
                                                    if ((i_18_ ^ 0xffffffff) != -6) {
                                                        if ((i_18_ ^ 0xffffffff) == -7)
                                                            break while_144_;
                                                        break while_145_;
                                                    }
                                                } else
                                                    break while_142_;
                                                break while_143_;
                                            }
                                        } else
                                            break;
                                        break while_141_;
                                    }
                                } else {
                                    anInt9129 = anInt9117 = r.readUnsignedByte();
                                    break while_145_;
                                }
                                anInt9122 = r.readUnsignedByte();
                                break while_145_;
                            } while (false);
                            anInt9125 = r.readUnsignedShort();
                            break while_145_;
                        } while (false);
                        anInt9124 = r.readUnsignedByte();
                        break while_145_;
                    } while (false);
                    anInt9118 = r.readUnsignedByte();
                    break while_145_;
                } while (false);
                anInt9129 = r.readUnsignedByte();
                break while_145_;
            } while (false);
            anInt9117 = r.readUnsignedByte();
        } while (false);
    }

    @Override
    public int[] monoRow(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}