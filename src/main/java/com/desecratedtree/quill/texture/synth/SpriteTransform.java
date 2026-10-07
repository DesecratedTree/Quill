package com.desecratedtree.quill.texture.synth;

/**
 * Affine transform for a sprite sub-quad, read by {@code Class348_Sub40.method3036}.
 * Port of {@code Class50_Sub2} / {@code Class50}. Field names match the reference
 * deobfuscation for 1:1 transliteration of render methods (method455/456/457).
 */
public final class SpriteTransform {

    /** {@code anInt5231} */
    public int anInt5231;
    /** {@code anInt5227} */
    public int anInt5227;
    /** {@code anInt5232} */
    public int anInt5232;
    /** {@code anInt5230} */
    public int anInt5230;
    /** {@code anInt864} */
    public int anInt864;
    /** {@code anInt865} */
    public int anInt865;
    /** {@code anInt862} */
    public int anInt862;

    public SpriteTransform(int anInt5231, int anInt5227, int anInt5232, int anInt5230,
                           int anInt864, int anInt865, int anInt862) {
        this.anInt5231 = anInt5231;
        this.anInt5227 = anInt5227;
        this.anInt5232 = anInt5232;
        this.anInt5230 = anInt5230;
        this.anInt864 = anInt864;
        this.anInt865 = anInt865;
        this.anInt862 = anInt862;
    }
}