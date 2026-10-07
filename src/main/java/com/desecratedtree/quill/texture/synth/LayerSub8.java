package com.desecratedtree.quill.texture.synth;

/**
 * Type 34 — texture layer.
 * Port of {@code Class348_Sub40_Sub8}.
 */
public final class LayerSub8 extends TextureLayer {

    int anInt9149 = 1638;
    int anInt9150;
    private byte[] aByteArray9152 = new byte[512];
    int anInt9156;
    int anInt9158;
    private short[] aShortArray9159;
    boolean aBoolean9160 = true;
    private short[] aShortArray9162;
    int anInt9164;

    public LayerSub8() {
        super(0, true);
        anInt9150 = 4;
        anInt9156 = 0;
        anInt9164 = 4;
        anInt9158 = 4;
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i = opcode;
        while_151_:
        do {
            while_150_:
            do {
                while_149_:
                do {
                    while_148_:
                    do {
                        while_147_:
                        do {
                            do {
                                if (i != 0) {
                                    if ((i ^ 0xffffffff) != -2) {
                                        if (i != 2) {
                                            if ((i ^ 0xffffffff) != -4) {
                                                if (i != 4) {
                                                    if (i != 5) {
                                                        if (i == 6)
                                                            break while_150_;
                                                        break while_151_;
                                                    }
                                                } else
                                                    break while_148_;
                                                break while_149_;
                                            }
                                        } else
                                            break;
                                        break while_147_;
                                    }
                                } else {
                                    aBoolean9160 = r.readUnsignedByte() == 1;
                                    break while_151_;
                                }
                                anInt9150 = r.readUnsignedByte();
                                break while_151_;
                            } while (false);
                            anInt9149 = r.readSignedShort();
                            if ((anInt9149 ^ 0xffffffff) > -1) {
                                aShortArray9159 = new short[anInt9150];
                                for (i = 0; anInt9150 > i; i++)
                                    aShortArray9159[i] = (short) r.readSignedShort();
                            }
                            break while_151_;
                        } while (false);
                        anInt9158 = anInt9164 = r.readUnsignedByte();
                        break while_151_;
                    } while (false);
                    anInt9156 = r.readUnsignedByte();
                    break while_151_;
                } while (false);
                anInt9158 = r.readUnsignedByte();
                break while_151_;
            } while (false);
            anInt9164 = r.readUnsignedByte();
        } while (false);
    }

    @Override
    public int[] monoRow(int y, int tag) {
        int[] row = monoBuffer.row(y);
        if (monoBuffer.fresh) {
            int scale = Math.max(1, anInt9164);
            int seed = anInt9156 * 0x45D9F3B + anInt9149 * 31 + anInt9150;
            for (int x = 0; x < TextureRender.WIDTH; x++) {
                int xx = (TextureRender.U[x] * scale) >> 12;
                int yy = (TextureRender.V[y] * anInt9158) >> 12;
                int value = seed ^ (xx * 0x1F123BB5) ^ (yy * 0x5F356495);
                value = (value ^ value >>> 16) * 0x45D9F3B;
                value = (value ^ value >>> 16) * 0x45D9F3B;
                row[x] = (value ^ value >>> 16) & 0xFFF;
            }
        }
        return row;
    }
}