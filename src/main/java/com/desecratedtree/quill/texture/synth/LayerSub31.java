package com.desecratedtree.quill.texture.synth;

/**
 * Type 20 — port of {@code Class348_Sub40_Sub31}.
 */
public final class LayerSub31 extends TextureLayer {

    private int anInt9405 = 4;
    private int anInt9410 = 4;

    public LayerSub31() {
        super(1, false);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i = opcode;
        do {
            if (i != 0) {
                if (i != 1)
                    break;
            } else {
                anInt9405 = r.readUnsignedByte();
                break;
            }
            anInt9410 = r.readUnsignedByte();
        } while (false);
    }
}
