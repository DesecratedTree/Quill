package com.desecratedtree.quill.texture.synth;

/**
 * Static render context shared by all synth layers. Ports of the client's
 * {@code Class79.method797} (render size + sample tables), the cos/sin table
 * inits and {@code Class348_Sub42_Sub13.method3232} (gamma LUT).
 */
public final class TextureRender {

    /** {@code Class348_Sub40_Sub6.anInt9139} */
    public static int WIDTH;
    /** {@code Class286_Sub2.anInt6212} */
    public static int HEIGHT;
    /** {@code Class239_Sub22.anInt6076} */
    public static int WIDTH_MASK;
    /** {@code Class299_Sub2.anInt6325} */
    public static int HEIGHT_MASK;

    /** {@code Class318_Sub6.anIntArray6432} — X sample offset {@code (x << 12) / W}. */
    public static int[] U;
    /** {@code Class239_Sub18.anIntArray6035} — Y sample offset {@code (y << 12) / H}. */
    public static int[] V;

    /** {@code Class127.anIntArray4654} */
    public static final int[] COS = new int[2048];
    /** {@code Class235.anIntArray3068} */
    public static final int[] SIN = new int[2048];

    /** {@code Class318_Sub1_Sub3_Sub3.anIntArray10266} */
    public static final int[] GAMMA = new int[256];
    /** {@code Class299_Sub2_Sub1.aDouble8713} */
    private static double gammaExponent = Double.NaN;
    /** {@code Class348.aClass45_4286} — sprite source for {sprite layers}. */
    static SpriteProvider spriteProvider;

    private TextureRender() {
    }

    static {
        double theta = 2.0 * Math.PI / 2048.0;
        for (int i = 0; i < 2048; i++) {
            COS[i] = (int) (65536.0 * Math.cos(theta * i));
            SIN[i] = (int) (65536.0 * Math.sin(theta * i));
        }
    }

    /** {@code Class79.method797(int height, int width, ...)}. */
    public static void setSize(int width, int height) {
        if (WIDTH != width) {
            U = new int[width];
            for (int i = 0; i < width; i++) {
                U[i] = (i << 12) / width;
            }
            WIDTH_MASK = width - 1;
            WIDTH = width;
        }
        if (HEIGHT != height) {
            if (WIDTH != height) {
                V = new int[height];
                for (int i = 0; i < height; i++) {
                    V[i] = (i << 12) / height;
                }
            } else {
                V = U;
            }
            HEIGHT = height;
            HEIGHT_MASK = height - 1;
        }
    }

    /** {@code Class348_Sub42_Sub13.method3232} — per-channel gamma LUT. */
    public static void setGamma(double exponent) {
        if (gammaExponent != exponent) {
            for (int i = 0; i < 256; i++) {
                int v = (int) (255.0 * Math.pow(i / 255.0, exponent));
                GAMMA[i] = Math.min(v, 255);
            }
            gammaExponent = exponent;
        }
    }

    public static double gammaExponent() {
        return gammaExponent;
    }

    /** {@code Class139.method1166} — bit mask. */
    public static int mask(int bits, int value) {
        return bits & value;
    }

    /** {@code Class135_Sub2.method1156} — fill {@code line[x2..x1)} with {@code colour}. */
    public static void fill(int x1, int[] line, int x2, int colour) {
        for (int i = x2; i < x1; i++) {
            line[i] = colour;
        }
    }

    /** {@code Class85.method831} — per-column coordinate clamp used by quad layers. */
    public static int clamp(int a, int b, int c) {
        if (c > b) {
            return c;
        }
        return Math.min(b, a);
    }
}