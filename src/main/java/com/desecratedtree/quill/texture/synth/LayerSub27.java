package com.desecratedtree.quill.texture.synth;

/**
 * Type 21 — port of {@code Class348_Sub40_Sub27}.
 */
public final class LayerSub27 extends TextureLayer {

    public LayerSub27() {
        super(3, false);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        if (opcode == 0)
            this.mono = r.readUnsignedByte() == 1;
    }
}
