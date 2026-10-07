package com.desecratedtree.quill.texture.synth;

/**
 * Type 22 — port of {@code Class348_Sub40_Sub32}.
 */
public final class LayerSub32 extends TextureLayer {

    public LayerSub32() {
        super(1, false);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        if (opcode == 0)
            this.mono = r.readUnsignedByte() == 1;
    }
}
