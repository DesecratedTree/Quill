package com.desecratedtree.quill.texture.synth;

/**
 * Type 7.
 * Port of {@code Class348_Sub40_Sub16}.
 */
public final class LayerSub16 extends TextureLayer {

    private int anInt9226 = 6;

    public LayerSub16() {
        super(2, false);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        int i_1_ = opcode;
        do {
            if (i_1_ != 0) {
                if (i_1_ != 1)
                    break;
            } else {
                anInt9226 = r.readUnsignedByte();
                break;
            }
            this.mono = (r.readUnsignedByte() ^ 0xffffffff) == -2;
        } while (false);
    }

    private int[] combine(int y) {
        int[] a = childMonoRow(y, 0);
        int[] b = childMonoRow(y, 1);
        int[] row = monoBuffer.row(y);
        if (monoBuffer.fresh) {
            for (int x = 0; x < TextureRender.WIDTH; x++) {
                int left = a[x];
                int right = b[x];
                switch (anInt9226) {
                    case 1: row[x] = left + right; break;
                    case 2: row[x] = left - right; break;
                    case 3: row[x] = left * right >> 12; break;
                    case 4: row[x] = right == 0 ? 4096 : (left << 12) / right; break;
                    case 5: row[x] = 4096 - ((4096 - left) * (4096 - right) >> 12); break;
                    case 6: row[x] = right >= 2048 ? 4096 - ((4096 - left) * (4096 - right) >> 11) : right * left >> 11; break;
                    case 7: row[x] = left == 4096 ? 4096 : (right << 12) / (4096 - left); break;
                    case 8: row[x] = left == 0 ? 0 : 4096 - ((4096 - right << 12) / left); break;
                    case 9: row[x] = Math.min(left, right); break;
                    case 10: row[x] = Math.max(left, right); break;
                    case 11: row[x] = Math.abs(left - right); break;
                    case 12: row[x] = left + right - (left * right >> 11); break;
                    default: row[x] = left + right;
                }
            }
        }
        return row;
    }

    @Override
    public int[] monoRow(int y, int tag) {
        return combine(y);
    }

    @Override
    public int[][] colorRows(int y, int tag) {
        int[][] row = colorBuffer.row(y);
        if (colorBuffer.fresh) {
            int[][] a = childColorRows(y, 0);
            int[][] b = childColorRows(y, 1);
            for (int channel = 0; channel < 3; channel++) {
                for (int x = 0; x < TextureRender.WIDTH; x++) {
                    int left = a[channel][x], right = b[channel][x];
                    switch (anInt9226) {
                        case 1: row[channel][x] = left + right; break;
                        case 2: row[channel][x] = left - right; break;
                        case 3: row[channel][x] = left * right >> 12; break;
                        case 9: row[channel][x] = Math.min(left, right); break;
                        case 10: row[channel][x] = Math.max(left, right); break;
                        case 11: row[channel][x] = Math.abs(left - right); break;
                        default: row[channel][x] = left + right;
                    }
                }
            }
        }
        return row;
    }
}