package com.desecratedtree.quill.texture.synth;

/**
 * Type 19 — monochrome warp/displacement-sampling layer.
 * Port of {@code Class348_Sub40_Sub6}.
 */
public final class LayerSub6 extends TextureLayer {

    private int anInt9133 = 32768;

    public LayerSub6() {
        super(3, false);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i_12_ = opcode;
        do {
            if ((i_12_ ^ 0xffffffff) != -1) {
                if (i_12_ != 1)
                    break;
            } else {
                anInt9133 = r.readUnsignedShort() << -1225450108;
                break;
            }
            this.mono = (r.readUnsignedByte() ^ 0xffffffff) == -2;
        } while (false);
    }

    @Override
    public int[] monoRow(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }

    @Override
    public int[][] colorRows(int y, int tag) {
        throw new UnsupportedOperationException("render not yet ported");
    }
}