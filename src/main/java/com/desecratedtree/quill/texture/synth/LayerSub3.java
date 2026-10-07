package com.desecratedtree.quill.texture.synth;

/**
 * Type 26 — port of {@code Class348_Sub40_Sub3}.
 */
public final class LayerSub3 extends TextureLayer {

    private int anInt9104;
    private int anInt9107 = 4096;

    public LayerSub3() {
        super(1, true);
        anInt9104 = 0;
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i = opcode;
        do {
            if (i != 0) {
                if (i != 1)
                    break;
            } else {
                anInt9104 = r.readUnsignedShort();
                break;
            }
            anInt9107 = r.readUnsignedShort();
        } while (false);
    }
}
