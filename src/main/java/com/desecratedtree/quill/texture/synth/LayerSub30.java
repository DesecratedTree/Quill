package com.desecratedtree.quill.texture.synth;

/**
 * Type 17 — tri-planar smooth colour layer.
 * Port of {@code Class348_Sub40_Sub30}.
 */
public final class LayerSub30 extends TextureLayer {

    private int anInt9386;
    private int anInt9389;
    private int anInt9390 = 0;
    private int anInt9392;
    private int anInt9396;
    private int anInt9398 = 0;
    private int anInt9400;
    private int anInt9401;
    private int anInt9402 = 0;

    public LayerSub30() {
        super(1, false);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        while_209_:
        do {
            try {
                int i_40_ = opcode;
                do {
                    if ((i_40_ ^ 0xffffffff) != -1) {
                        if ((i_40_ ^ 0xffffffff) != -2) {
                            if (i_40_ == 2)
                                break;
                            break while_209_;
                        }
                    } else {
                        anInt9402 = r.readSignedShort();
                        return;
                    }
                    anInt9390 = (r.readByte() << 1365062124) / 100;
                    return;
                } while (false);
                anInt9398 = (r.readByte() << 1792937036) / 100;
                break;
            } catch (RuntimeException runtimeexception) {
                throw method2929(runtimeexception,
                        ("vj.F(" + (r != null ? "{...}" : "null")
                                + ',' + opcode + ',' + 31015 + ')'));
            }
        } while (false);
    }

    private static RuntimeException method2929(RuntimeException runtimeexception, String string) {
        return runtimeexception;
    }

    @Override
    public int[][] colorRows(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}