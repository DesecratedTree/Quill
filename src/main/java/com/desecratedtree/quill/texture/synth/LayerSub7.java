package com.desecratedtree.quill.texture.synth;

/**
 * Type 9.
 * Port of {@code Class348_Sub40_Sub7}.
 */
public final class LayerSub7 extends TextureLayer {

    private boolean aBoolean9140 = true;
    private boolean aBoolean9147 = true;

    public LayerSub7() {
        super(1, false);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i_73_ = opcode;
        while_146_:
        do {
            do {
                if (i_73_ != 0) {
                    if ((i_73_ ^ 0xffffffff) != -2) {
                        if (i_73_ == 2)
                            break;
                        break while_146_;
                    }
                } else {
                    aBoolean9140 = (r.readUnsignedByte() ^ 0xffffffff) == -2;
                    return;
                }
                aBoolean9147 = r.readUnsignedByte() == 1;
                return;
            } while (false);
            this.mono = (r.readUnsignedByte() ^ 0xffffffff) == -2;
            break while_146_;
        } while (false);
    }

    @Override
    public int[][] colorRows(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }

    @Override
    public int[] monoRow(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}