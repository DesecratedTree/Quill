package com.desecratedtree.quill.texture.synth;

/**
 * Type 11 — RGB-channel scale layer.
 * Port of {@code Class348_Sub40_Sub26}.
 */
public final class LayerSub26 extends TextureLayer {

    private int anInt9344 = 4096;
    private int anInt9347 = 4096;
    private int anInt9354 = 4096;

    public LayerSub26() {
        super(1, false);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        do {
            try {
                int i_24_ = opcode;
                while_201_:
                do {
                    do {
                        if (i_24_ != 0) {
                            if (i_24_ != 1) {
                                if ((i_24_ ^ 0xffffffff) == -3)
                                    break;
                                break while_201_;
                            }
                        } else {
                            anInt9344 = r.readUnsignedShort();
                            break while_201_;
                        }
                        anInt9354 = r.readUnsignedShort();
                        break while_201_;
                    } while (false);
                    anInt9347 = r.readUnsignedShort();
                } while (false);
            } catch (RuntimeException runtimeexception) {
                throw method2929(runtimeexception,
                        ("uf.F(" + (r != null ? "{...}" : "null")
                                + ',' + opcode + ',' + 31015 + ')'));
            }
            break;
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