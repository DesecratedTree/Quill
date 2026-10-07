package com.desecratedtree.quill.texture.synth;

/**
 * Type 31 — texture layer.
 * Port of {@code Class348_Sub40_Sub25}.
 */
public final class LayerSub25 extends TextureLayer {

    private int anInt9338 = 0;
    private int anInt9339 = 0;
    private int anInt9340 = 1365;
    private int anInt9343 = 20;

    public LayerSub25() {
        super(0, true);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i = opcode;
        while_200_:
        do {
            while_199_:
            do {
                do {
                    if ((i ^ 0xffffffff) != -1) {
                        if (i != 1) {
                            if (i != 2) {
                                if ((i ^ 0xffffffff) != -4)
                                    break while_200_;
                            } else
                                break;
                            break while_199_;
                        }
                    } else {
                        anInt9340 = r.readUnsignedShort();
                        break while_200_;
                    }
                    anInt9343 = r.readUnsignedShort();
                    break while_200_;
                } while (false);
                anInt9339 = r.readUnsignedShort();
                break while_200_;
            } while (false);
            anInt9338 = r.readUnsignedShort();
        } while (false);
    }

    @Override
    public int[] monoRow(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}