package com.desecratedtree.quill.texture.synth;

/**
 * Type 30 — texture layer.
 * Port of {@code Class348_Sub40_Sub10}.
 */
public final class LayerSub10 extends TextureLayer {

    private int anInt9175;
    private int anInt9176 = 1024;
    private int anInt9182;

    public LayerSub10() {
        super(1, false);
        anInt9175 = 3072;
        anInt9182 = 2048;
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i = opcode;
        while_152_:
        do {
            do {
                if (i != 0) {
                    if ((i ^ 0xffffffff) != -2) {
                        if ((i ^ 0xffffffff) == -3)
                            break;
                        break while_152_;
                    }
                } else {
                    anInt9176 = r.readUnsignedShort();
                    break while_152_;
                }
                anInt9175 = r.readUnsignedShort();
                break while_152_;
            } while (false);
            this.mono = (r.readUnsignedByte() ^ 0xffffffff) == -2;
        } while (false);
    }

    @Override
    public int[] monoRow(int y, int tag) {
        int[] row = monoBuffer.row(y);
        if (monoBuffer.fresh) {
            int[] source = childMonoRow(y, 0);
            for (int x = 0; x < TextureRender.WIDTH; x++) {
                row[x] = anInt9176 + (anInt9182 * source[x] >> 12);
            }
        }
        return row;
    }

    @Override
    public int[][] colorRows(int y, int tag) {
        int[][] row = colorBuffer.row(y);
        if (colorBuffer.fresh) {
            int[][] source = childColorRows(y, 0);
            for (int channel = 0; channel < 3; channel++) {
                for (int x = 0; x < TextureRender.WIDTH; x++) {
                    row[channel][x] = anInt9176 + (anInt9182 * source[channel][x] >> 12);
                }
            }
        }
        return row;
    }
}