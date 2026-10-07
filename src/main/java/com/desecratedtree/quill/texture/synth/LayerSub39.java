package com.desecratedtree.quill.texture.synth;

/**
 * Type 29 — port of {@code Class348_Sub40_Sub39}.
 */
public final class LayerSub39 extends TextureLayer {

    private SpriteTransform[] transforms;

    public LayerSub39() {
        super(0, true);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        if (opcode == 0) {
            transforms = new SpriteTransform[r.readUnsignedByte()];
            while_216_:
            for (int i = 0; i < transforms.length; i++) {
                int subId = r.readUnsignedByte();
                int s = subId;
                while_214_:
                do {
                    do {
                        if (s != 0) {
                            if (s != 1) {
                                if (s != 2) {
                                    if (s != 3)
                                        continue while_216_;
                                } else
                                    break;
                                break while_214_;
                            }
                        } else {
                            // DEP: sub-id 0 — Class182.method1374 -> Class50_Sub4
                            transforms[i] = null;
                            continue while_216_;
                        }
                        // DEP: sub-id 1 — Class348_Sub23_Sub1.method2970 -> Class50_Sub3
                        transforms[i] = null;
                        continue while_216_;
                    } while (false);
                    transforms[i] = TextureLayer.readSpriteTransform(r);
                    continue while_216_;
                } while (false);
                // DEP: sub-id 3 — Class265.method2022 -> Class50_Sub1
                transforms[i] = null;
            }
        } else if (opcode == 1)
            this.mono = r.readUnsignedByte() == 1;
    }
}