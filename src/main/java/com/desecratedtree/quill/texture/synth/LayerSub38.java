package com.desecratedtree.quill.texture.synth;

/**
 * Type 6.
 * Port of {@code Class348_Sub40_Sub38}.
 */
public final class LayerSub38 extends TextureLayer {

    private int anInt9470 = 4096;
    private int anInt9474 = 0;

    public LayerSub38() {
        super(1, false);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i_1_ = opcode;
        while_213_:
        do {
            do {
                if (i_1_ != 0) {
                    if (i_1_ != 1) {
                        if (i_1_ == 2)
                            break;
                        break while_213_;
                    }
                } else {
                    anInt9474 = r.readUnsignedShort();
                    break while_213_;
                }
                anInt9470 = r.readUnsignedShort();
                break while_213_;
            } while (false);
            this.mono = r.readUnsignedByte() == 1;
        } while (false);
    }

    @Override
    public int[][] colorRows(int y, int tag) {
        int[][] row = colorBuffer.row(y);
        if (colorBuffer.fresh) {
            int[][] source = childColorRows(y, 0);
            for (int x = 0; x < TextureRender.WIDTH; x++) {
                row[0][x] = clamp(source[0][x]);
                row[1][x] = clamp(source[1][x]);
                row[2][x] = clamp(source[2][x]);
            }
        }
        return row;
    }

    @Override
    public int[] monoRow(int y, int tag) {
        int[] row = monoBuffer.row(y);
        if (monoBuffer.fresh) {
            int[] source = childMonoRow(y, 0);
            for (int x = 0; x < TextureRender.WIDTH; x++) {
                row[x] = clamp(source[x]);
            }
        }
        return row;
    }

    private int clamp(int value) {
        return Math.max(anInt9474, Math.min(anInt9470, value));
    }
}