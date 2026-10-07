package com.desecratedtree.quill.texture.synth;

/**
 * Type 38 — texture layer.
 * Port of {@code Class348_Sub40_Sub28}.
 */
public final class LayerSub28 extends TextureLayer {

    private int anInt9362;
    private int anInt9364 = 4096;
    private int anInt9367;
    private int anInt9368 = 16;
    private int anInt9369 = 0;

    public LayerSub28() {
        super(0, true);
        anInt9362 = 2000;
        anInt9367 = 0;
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i = opcode;
        while_204_:
        do {
            while_203_:
            do {
                while_202_:
                do {
                    do {
                        if ((i ^ 0xffffffff) != -1) {
                            if ((i ^ 0xffffffff) != -2) {
                                if ((i ^ 0xffffffff) != -3) {
                                    if (i != 3) {
                                        if (i == 4)
                                            break while_203_;
                                        break while_204_;
                                    }
                                } else
                                    break;
                                break while_202_;
                            }
                        } else {
                            anInt9367 = r.readUnsignedByte();
                            return;
                        }
                        anInt9362 = r.readUnsignedShort();
                        return;
                    } while (false);
                    anInt9368 = r.readUnsignedByte();
                    return;
                } while (false);
                anInt9369 = r.readUnsignedShort();
                return;
            } while (false);
            anInt9364 = r.readUnsignedShort();
        } while (false);
    }

    @Override
    public int[] monoRow(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}