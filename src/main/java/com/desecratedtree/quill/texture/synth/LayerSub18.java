package com.desecratedtree.quill.texture.synth;

/**
 * Type 1.
 * Port of {@code Class348_Sub40_Sub18}.
 */
public final class LayerSub18 extends TextureLayer {

    private int anInt9244;
    private int anInt9250;
    private int anInt9252;

    public LayerSub18() {
        this(0);
    }

    public LayerSub18(int value) {
        super(0, false);
        method3095(-104, value);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i_21_ = opcode;
        if (i_21_ == 0) {
            method3095(-124, r.readUnsignedTriByte());
        }
    }

    @Override
    public int[][] colorRows(int y, int tag) {
        int[][] row = colorBuffer.row(y);
        if (colorBuffer.fresh) {
            for (int x = 0; x < TextureRender.WIDTH; x++) {
                row[0][x] = anInt9244;
                row[1][x] = anInt9252;
                row[2][x] = anInt9250;
            }
        }
        return row;
    }

    private void method3095(int i, int i_19_) {
        anInt9244 = 0xff0 & i_19_ >> -2106963764;
        anInt9252 = i_19_ >> 1732680260 & 0xff0;
        anInt9250 = (i_19_ & 0xff) << 1427078244;
        if (i >= -54) {
            anInt9250 = -42;
        }
    }
}