package com.desecratedtree.quill.texture.synth;

/**
 * Type 23 — port of {@code Class348_Sub40_Sub33}.
 */
public final class LayerSub33 extends TextureLayer {

    public LayerSub33() {
        super(1, false);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        if (opcode == 0)
            this.mono = r.readUnsignedByte() == 1;
    }
}
