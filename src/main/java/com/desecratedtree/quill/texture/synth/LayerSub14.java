package com.desecratedtree.quill.texture.synth;

/**
 * Type 8.
 * Port of {@code Class348_Sub40_Sub14}.
 */
public final class LayerSub14 extends TextureLayer {

    private int[] anIntArray9208;
    private int[][] anIntArrayArray9210;
    private int anInt9211 = 0;
    private int[] anIntArray9214;
    private short[] aShortArray9215 = new short[257];

    public LayerSub14() {
        super(1, true);
    }

    @Override
    public void readParams(TextureReader r, int opcode) {
        if (opcode == 0) {
            anInt9211 = r.readUnsignedByte();
            anIntArrayArray9210 = new int[r.readUnsignedByte()][2];
            for (int i_1_ = 0; ((anIntArrayArray9210.length ^ 0xffffffff)
                                < (i_1_ ^ 0xffffffff)); i_1_++) {
                anIntArrayArray9210[i_1_][0] = r.readUnsignedShort();
                anIntArrayArray9210[i_1_][1] = r.readUnsignedShort();
            }
        }
    }

    @Override
    public void finalizeConfig() {
        if (anIntArrayArray9210 == null || anIntArrayArray9210.length < 2) {
            anIntArrayArray9210 = new int[][]{{0, 0}, {4096, 4096}};
        }
        for (int i = 0; i < aShortArray9215.length; i++) {
            int value = i << 4;
            int index = 1;
            while (index < anIntArrayArray9210.length - 1
                    && anIntArrayArray9210[index][0] <= value) {
                index++;
            }
            int[] left = anIntArrayArray9210[Math.max(0, index - 1)];
            int[] right = anIntArrayArray9210[Math.min(anIntArrayArray9210.length - 1, index)];
            int span = Math.max(1, right[0] - left[0]);
            int amount = ((value - left[0]) << 12) / span;
            amount = Math.max(0, Math.min(4096, amount));
            if (anInt9211 == 1) {
                amount = (4096 - (TextureRender.SIN[(amount * 2048 >> 12) & 2047]) >> 1);
            } else if (anInt9211 == 2) {
                int previous = anIntArrayArray9210[Math.max(0, index - 2)][1];
                int next = anIntArrayArray9210[Math.min(anIntArrayArray9210.length - 1, index + 1)][1];
                int control = previous + next - left[1] - right[1];
                int t2 = amount * amount >> 12;
                int t3 = t2 * amount >> 12;
                int valueAt = (control * t3 >> 12) + ((left[1] - previous) * t2 >> 12)
                        + ((right[1] - left[1]) * amount >> 12) + left[1];
                aShortArray9215[i] = (short) Math.max(-32767, Math.min(32767, valueAt));
                continue;
            }
            aShortArray9215[i] = (short) ((left[1] * (4096 - amount) + right[1] * amount) >> 12);
        }
    }

    @Override
    public int[] monoRow(int y, int tag) {
        int[] row = monoBuffer.row(y);
        if (monoBuffer.fresh) {
            int[] source = childMonoRow(y, 0);
            for (int x = 0; x < TextureRender.WIDTH; x++) {
                int index = Math.max(0, Math.min(256, source[x] >> 4));
                row[x] = aShortArray9215[index];
            }
        }
        return row;
    }
}